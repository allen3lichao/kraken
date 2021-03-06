// **********************************************************************
//
// Copyright (c) 2003-2010 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

// Ice version 3.4.1

package Glacier2;

// <auto-generated>
//
// Generated from file `Session.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>


/**
 * The session manager for username/password authenticated users that
 * is responsible for managing {@link Session} objects. New session objects
 * are created by the {@link Router} object calling on an application-provided
 * session manager. If no session manager is provided by the application,
 * no client-visible sessions are passed to the client.
 * 
 * @see Router
 * @see Session
 * 
 **/
public interface SessionManagerPrx extends Ice.ObjectPrx
{
    /**
     * Create a new session.
     * 
     * @param userId The user id for the session.
     * 
     * @param control A proxy to the session control object.
     * 
     * @return A proxy to the newly created session.
     * 
     * @throws CannotCreateSessionException Raised if the session
     * cannot be created.
     * 
     **/
    public SessionPrx create(String userId, SessionControlPrx control)
        throws CannotCreateSessionException;

    /**
     * Create a new session.
     * 
     * @param userId The user id for the session.
     * 
     * @param control A proxy to the session control object.
     * 
     * @param __ctx The Context map to send with the invocation.
     * @return A proxy to the newly created session.
     * 
     * @throws CannotCreateSessionException Raised if the session
     * cannot be created.
     * 
     **/
    public SessionPrx create(String userId, SessionControlPrx control, java.util.Map<String, String> __ctx)
        throws CannotCreateSessionException;

    /**
     * Create a new session.
     * 
     * @param userId The user id for the session.
     * 
     * @param control A proxy to the session control object.
     * 
     * @return The asynchronous result object.
     **/
    public Ice.AsyncResult begin_create(String userId, SessionControlPrx control);

    /**
     * Create a new session.
     * 
     * @param userId The user id for the session.
     * 
     * @param control A proxy to the session control object.
     * 
     * @param __ctx The Context map to send with the invocation.
     * @return The asynchronous result object.
     **/
    public Ice.AsyncResult begin_create(String userId, SessionControlPrx control, java.util.Map<String, String> __ctx);

    /**
     * Create a new session.
     * 
     * @param userId The user id for the session.
     * 
     * @param control A proxy to the session control object.
     * 
     * @param __cb The asynchronous callback object.
     * @return The asynchronous result object.
     **/
    public Ice.AsyncResult begin_create(String userId, SessionControlPrx control, Ice.Callback __cb);

    /**
     * Create a new session.
     * 
     * @param userId The user id for the session.
     * 
     * @param control A proxy to the session control object.
     * 
     * @param __ctx The Context map to send with the invocation.
     * @param __cb The asynchronous callback object.
     * @return The asynchronous result object.
     **/
    public Ice.AsyncResult begin_create(String userId, SessionControlPrx control, java.util.Map<String, String> __ctx, Ice.Callback __cb);

    /**
     * Create a new session.
     * 
     * @param userId The user id for the session.
     * 
     * @param control A proxy to the session control object.
     * 
     * @param __cb The asynchronous callback object.
     * @return The asynchronous result object.
     **/
    public Ice.AsyncResult begin_create(String userId, SessionControlPrx control, Callback_SessionManager_create __cb);

    /**
     * Create a new session.
     * 
     * @param userId The user id for the session.
     * 
     * @param control A proxy to the session control object.
     * 
     * @param __ctx The Context map to send with the invocation.
     * @param __cb The asynchronous callback object.
     * @return The asynchronous result object.
     **/
    public Ice.AsyncResult begin_create(String userId, SessionControlPrx control, java.util.Map<String, String> __ctx, Callback_SessionManager_create __cb);

    /**
     * Create a new session.
     * 
     * @param __result The asynchronous result object.
     * @return A proxy to the newly created session.
     * 
     * @throws CannotCreateSessionException Raised if the session
     * cannot be created.
     * 
     **/
    public SessionPrx end_create(Ice.AsyncResult __result)
        throws CannotCreateSessionException;

    /**
     * Create a new session.
     * 
     * @param __cb The callback object for the operation.
     * @param userId The user id for the session.
     * 
     * @param control A proxy to the session control object.
     * 
     **/
    public boolean create_async(AMI_SessionManager_create __cb, String userId, SessionControlPrx control);

    /**
     * Create a new session.
     * 
     * @param __cb The callback object for the operation.
     * @param userId The user id for the session.
     * 
     * @param control A proxy to the session control object.
     * 
     * @param __ctx The Context map to send with the invocation.
     **/
    public boolean create_async(AMI_SessionManager_create __cb, String userId, SessionControlPrx control, java.util.Map<String, String> __ctx);
}
