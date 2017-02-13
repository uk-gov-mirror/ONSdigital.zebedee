FROM onsdigital/java-component

# Add the repo source
WORKDIR /usr/src
ADD ./target/dependency /usr/src/target/dependency
ADD ./target/classes /usr/src/target/classes

# Temporary: expose Elasticsearch
EXPOSE 9200

# Update the entry point script
ENTRYPOINT java -Xms2g -Xmx2g \
          -Drestolino.classes=target/classes \
          -Drestolino.packageprefix=com.github.onsdigital.zebedee.reader.api \
          -Djava.util.concurrent.ForkJoinPool.common.parallelism=2 \
          -cp "target/dependency/*:target/classes/" \
          com.github.davidcarboni.restolino.Main
