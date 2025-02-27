package com.github.onsdigital.zebedee.session.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Represents a user login session.
 * Created by david on 16/03/2015.
 */
public class Session {

    /**
     * The ID of this session.
     */
    private String id;

    /**
     * The user this session represents.
     */
    private String email;

    /**
     * The date-time at which the session started. This is useful for general information. Defaults to the current date.
     */
    private Date start = new Date();

    /**
     * The date-time at which the session was last accessed. This is useful for timeouts. Defaults to the current date.
     */
    private Date lastAccess = new Date();

    /**
     * Construct a new empty session.
     */
    public Session() {
        // default constructor required to maintain existing functionality
    }

    /**
     * Construct a new Session from the details provided.
     *
     * @param id         the unqiue ID of the session.
     * @param email      the user email the session belongs to.
     * @param start      the time the session was created
     * @param lastAccess the time the session was last accessed
     */
    public Session(final String id, final String email, final Date start, final Date lastAccess) {
        this.id = id;
        this.email = email;
        this.start = start;
        this.lastAccess = lastAccess;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Date getStart() {
        return start;
    }

    public Date getLastAccess() {
        return lastAccess;
    }

    public void setId(String id) {

        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public void setLastAccess(Date lastAccess) {
        this.lastAccess = lastAccess;
    }

    @Override
    public String toString() {
        return email + " (" + StringUtils.abbreviate(id, 8) + ")";
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null &&
                Session.class.isAssignableFrom(obj.getClass()) &&
                StringUtils.equals(id, ((Session) obj).id);
    }

    /**
     * Construct a Zebedee {@link Session} object from the external Session API
     * {@link com.github.onsdigital.session.service.Session} model.
     *
     * @param sess the sesison to use.
     * @return a Session with details provided.
     */
    public static Session fromAPIModel(com.github.onsdigital.session.service.Session sess) {
        return new Session(sess.getId(), sess.getEmail(), sess.getStart(), sess.getLastAccess());
    }
}
