# Zebedee

Simple CMS back-end: a file-based CMS with Json API and no UI.

It provides the ability to create and approve collections of change and preview these, overlaid onto existing content.

Pre-release content is encrypted until published and only shared with permitted users

David

#### Example environment variables
zebedee_root    /Users/thomasridd/Documents/onswebsite
brian_url   http://localhost:8083/
use_beta_publisher   false


## Performance tuning of indexing collections and documents
As part of the effort to index more data, the performance of the indexing processes has been reviewed.
The searching and parsing of data has been looked into and adjusted.
***
This was tested over a subset of the datasets (34K pages)
***

Initial performance tuning was completed withing the scope  of the indexing API variables, BulkSize, BulkActions and Concurrent requests.
| Shards | Concurrent Requests | Bulk Size | BulkActions | Start | End | Duration |
| ------ | ------------------- | --------- | ----------- | ----- | --- | -------- |
| 8 | 8 | 50MB | 100 | 8:51:52 | 9:02:00 | 0:10:08 |
| 8 | 8 | 50MB | 1000 | 9:15:30 | 9:25:16 | 0:09:46 |
| 8 | 8 | 500MB | 100 | 9:28:57 | 9:38:22 | 0:09:25 |
| 1 | 8 | 500MB | 1000 | 9:44:13 | 9:53:48 | 0:09:35 |
| 4 | 30 | 100MB | 1000 | 10:29:15 | 10:38:41 | 0:09:26 |
| 4 | 100 | 100MB | 1000 | 11:08:53 | 11:18:07 | 0:09:14 |

### Multi-Threaded Performance
As you can see the changes to the indexing parameters are of little benefit to the indexing speed.
Further development, enabling parallel analysis of the documents has a better effect on indexing speed.
 
Again this was tested with varying indexing parameters.
 
| Shards | Concurrent Requests | Bulk Size | BulkActions | Start | End | Duration |
| ------ | ------------------- | --------- | ----------- | ----- | --- | -------- |
| 5 | 12 | 100MB | 1000 | 12:44:01 | 12:48:54 | 0:04:53 |
| 5 | 5 | 100MB | 1000 | 12:51:19 | 12:55:36 | 0:04:17 |
| 5 | 5 | 500MB | 1000 | 12:58:44 | 13:01:29 | 0:02:45 |
| 5 | 5 | 1000MB | 2000 | 13:05:22 | 13:09:36 | 0:04:14 |
| 5 | 5 | 500MB | 2000 | 13:12:19 | 13:16:31 | 0:04:12 |
| 5 | 5 | 500MB | 1000 | 13:44:40 | 13:48:54 | 0:04:14 |
| 5 | 4 | 100MB | 1000 | 13:28:33 | 13:31:21 | 0:02:48 |
| 5 | 20 | 5000MB | 1000 | 9:01:32 | 9:05:33 | 0:04:01 |
  
  
  
As can be seen from the multi-threading solution has increased the performance by over 100%, but it also increases the variability of the timings.
 I am, not sure but as we are taking more resources it may be an artefact of contention from other processes running on the environment fighting for resources.   

So how does this compare to the _pre download content indexing_ indexing process.

I tested the original (non-attachment) indexing process alongside the Multi and single threaded attachments.  

### Original  _No Attachements_
| Start | End | Duration |
| --- | --- | ---|
| 11:13:51 | 11:14:32 | 0:00:41 |
| 11:16:11 | 11:17:01 | 0:00:50 |
| 11:18:03 | 11:18:53 | 0:00:50 |
| 11:20:07 | 11:20:57 | 0:00:50 |
_Avg 0:00:48 _


#### Attachment Parsing Multi Threaded 

| Start | End | Duration |
| --- | --- | ---|
| 10:39:30 | 10:39:53 | 0:00:23
| 10:42:59 | 10:43:19 | 0:00:20
| 11:00:05 | 11:01:04 | 0:00:59
| 11:03:20 | 11:04:18 | 0:00:58
_Avg 0:00:40_

### Attachment Parsing Single Threaded  
| Start | End | Duration |
| --- | --- | ---|
| 11:22:57 | 11:24:06 | 0:01:09 |
| 11:25:15 | 11:26:22 | 0:01:07 |
| 11:27:36 | 11:28:44 | 0:01:08 |
| 11:30:02 | 11:31:11 | 0:01:09 |
_Avg  0:01:08_