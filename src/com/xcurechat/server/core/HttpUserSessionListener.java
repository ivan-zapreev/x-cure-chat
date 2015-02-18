/**
 * X-Cure-Chat
 * Copyright (C) 2013  Dr. Ivan S. Zapreev
 * www: https://nl.linkedin.com/in/zapreevis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.#
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The server-side package, core.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.server.core;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.client.data.UserData;

import com.xcurechat.server.security.statistics.SessionAccessStatistics;

import com.xcurechat.server.security.statistics.StatisticsSecurityManager;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.log4j.Logger;

/**
 * @author zapreevis
 * This class is supposed to be binded to the user session to manage
 * automatic user's logout and also to store session statistics.  
 */
public class HttpUserSessionListener implements HttpSessionBindingListener {

	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( SessionAccessStatistics.class );

	//This is the name to which we bind the session listener for every servlet's session
	public static final String USER_SERVER_SESSION_LISTENER = "xcure.chat.user.servlet.session.listener";
	
	private SessionAccessStatistics statistics = new SessionAccessStatistics(true, UserData.UNKNOWN_UID);
	
	private int userID = UserData.UNKNOWN_UID;
	private String userSessionId = null;
	private String remoteAddr = null;
	
	public HttpUserSessionListener( ){
		super();
	}
	
	void setUser(final int userID, final String userSessionId, final String remoteAddr) {
		logger.debug( "Setting user ID to '" + userID +
						"' and user session id to '" + userSessionId + "'"+
						" with remote address '"+remoteAddr+"'");
		this.userID = userID;
		this.userSessionId = userSessionId;
		this.remoteAddr = remoteAddr;
		this.statistics.setUserID( userID );
	}
	
	boolean isUserSet() {
		return ( userID != UserData.UNKNOWN_UID ) && ( userSessionId != null );
	}
	
	int getUserID() {
		return this.userID;
	}
	
	String getUserSessionId() {
		return this.userSessionId;
	}
	
	String getRemoteAddr() {
		return this.remoteAddr;
	}
	
	public SessionAccessStatistics getAccessStatistics() {
		return statistics;
	}
	
	@Override
	public void valueBound(HttpSessionBindingEvent sEvent) {
		logger.debug("The session listener has been added to session '" + sEvent.getSession().getId() + "'" );
		
		//Increment the counter for the active session listeners
		addNewSessionListener();
	}
	
	//Get the synchronization factory for users
	private static final SynchFactory userSynchFactory = SynchFactory.getSynchFactory( SynchFactory.USER_FACTORY_NAME );

	@Override
	public void valueUnbound(HttpSessionBindingEvent sEvent) {
		//Put save the local user ID because otherwise when we do the logout
		//the userID gets re-set to an unknown user ID and then the synchronization
		//object for this user can not be released. 
		final int localUserID = userID;
		logger.debug( "The session '" + sEvent.getSession().getId() +
						"' created for user '" + localUserID + "' has expited." );
		//Log out the user if (s)he was logged in
		if( localUserID != UserData.UNKNOWN_UID && userSessionId != null ) {
			logger.info( "Automatically loging out user '" + localUserID + "'." );
			try{
				logger.debug( "Automatic logout of user '" + localUserID +
								"' from the http session: " + sEvent.getSession().getId() );
				try {
					//Do actions requiring synchronization of the user
					synchronized( userSynchFactory.getSynchObject( localUserID ) ) {
						ServerSideUserManager.logout( this, localUserID, userSessionId, remoteAddr, null, true );
					}
				}finally{
					userSynchFactory.releaseSynchObject( localUserID );
				}
			} catch( SiteException ex ) {
				logger.error( "An exception while automatic logout of user '" + localUserID +
								"' from the http session '" + sEvent.getSession().getId() + "'.", ex);
			}
		}
		
		//Decrement the counter for the active session listeners
		deleteOldSessionListener();
		
		//Clean up synchronization mappings
		userSynchFactory.cleanUp();
		//Clean up the statistics manager
		StatisticsSecurityManager.cleanUp();
	}
	
	/*********************************************************************************************************************/
	
	//The synchronization object for the static methods
	private static final Object synchObject = new Object();
	
	//The number of currently used server session listeners which represent users
	private static int count = 0;
	
	/**
	 * Allows to get the total number of currently available user sessions
	 * @return the total number of currently available user sessions
	 */
	public static final int getCurrentSessionsCount() {
		synchronized( synchObject ) {
			return count;
		}
	}
	
	/**
	 * Allows to increment the counter of the session listeners which represent users
	 */
	public static final void addNewSessionListener() {
		synchronized( synchObject ) {
			if( count < Integer.MAX_VALUE - 1 ) {
				//Prevent overflow
				count++;
			}
		}
	}
	
	/**
	 * Allows to decrement the counter of the session listeners which represent users
	 */
	public static final void deleteOldSessionListener() {
		synchronized( synchObject ) {
			if( count > 0 ) {
				//Prevent negative vlues
				count--;
			}
		}
	}
}
