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
 * The user data objects package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.userstatus;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FocusPanel;

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;

import com.google.gwt.event.shared.HandlerRegistration;
import com.xcurechat.client.SiteManagerUI;

/**
 * @author zapreevis
 * This class is responsible for setting the user's status to away after a certain period of no activity
 */
public class UserStatusManager extends Timer implements MouseMoveHandler, KeyPressHandler {
	//The time out after which the 
	private static final long NO_ACTIVITY_TIME_OUT_MILLISEC = 300000;
	//We check for inactivity every given perioud of time
	private static final int NO_ACTIVITY_CHECK_INTERVAL_MILLISEC = 60000;
	//The last time the user was active in millisec
	private long lastUserActivityMillisec = System.currentTimeMillis();
	//The user status widget reference 
	private final UserStatusWidget userStatusWidget;
	//The user status queue object
	private final UserStatusQueue userStatusQueue;
	//Contains true if the user is in the forced away mode
	private boolean isInForcedAway = false;
	//The user area panel from which we will get the mouse move events
	private final FocusPanel userAreaPanel;
	//The handler registration for the mouse move listener
	private HandlerRegistration mouseModehandlerReg = null;
	//The handler registration for the key press listener
	private HandlerRegistration keyPressedHandlerReg = null;
	//The actual chat user status as comes from the server
	private UserStatusType actualChatUserStatus = null;
	
	/**
	 * The basic constructor to the status handler
	 */
	private UserStatusManager( final FocusPanel userAreaPanel ) {
		this.userAreaPanel = userAreaPanel;
		this.userStatusWidget = new UserStatusWidget();
		this.userStatusQueue = new UserStatusQueue( userStatusWidget );
	}
	
	//The only instance of the user status manager
	private static UserStatusManager instance = null;
	
	/**
	 * Allows to get an instance of the user status manager.
	 * When called for the first time hookes up the widget to the main sites
	 * focus panel so that it can start receiving the mouse move events.
	 * @return the instance of the status manager if it is created or null
	 */
	public static UserStatusManager getInstance( ) {
		if( instance == null ) {
			instance = new UserStatusManager( SiteManagerUI.getInstance().getMainFocusPanel() );
		}
		return instance;
	}
	
	/**
	 * Allows to set the actual chat user status as comes from the server
	 * @param lastKnownChatUserStatus the user status from the server as comes in the chat room updates
	 */
	public void setActualChatUserStatus( final UserStatusType lastKnownChatUserStatus ) {
		actualChatUserStatus = lastKnownChatUserStatus;
	}
	
	public void onMouseMove(MouseMoveEvent event) {
		//Store the last time the mouse was moved
		lastUserActivityMillisec = System.currentTimeMillis();
		//If the user is in the forced away mode then we make him go back to the old mode
		if( isInForcedAway ) {
			isInForcedAway = false;
			userStatusQueue.setCurrentUserStatus( userStatusQueue.getPreviousUserStatus() );
		} else {
			//In case we can do automatic status enforcement
			if( userStatusQueue.canDoUserStatusInforcement() ) {
				//The actual user status that comes from the server with the room updates is know
				if( actualChatUserStatus != null ) {
					//The actual visible user status is not the same as set in the local user interface
					if( actualChatUserStatus != userStatusQueue.getCurrentUserStatus() ) {
						//Force the server status update, forcing the update in any case
						userStatusQueue.forceUserStatusToTheServer( userStatusQueue.getCurrentUserStatus() );
					}
				}
			}
		}
	}
	
	public void run() {
		if( ( System.currentTimeMillis() - lastUserActivityMillisec ) > NO_ACTIVITY_TIME_OUT_MILLISEC ) {
			//In case there was no user activity for long enough
			if( ! UserStatusType.AWAY_EQUIVALENT_STATUS_SET.contains( userStatusQueue.getCurrentUserStatus() ) ) {
				//If the user is not in an away-equivalent mode yet then we force the away mode
				//NOTE: we do it even in case the current status is AWAY, this is done to once-in-a-while
				//to force the away status to the server, because the server communication can be faulty
				isInForcedAway = true;
				//WARNING: There are synchronization issues here because the call
				//made in this method is asynchronous we do not check that the
				//user mode will actually be changed but, since here we do not check
				//for the isInForcedAway and use userStatusWidget.getLastUserStatus()
				//if the mode is not changed then we will attempt to change it during
				//the next iteration.
				userStatusQueue.setCurrentUserStatus( UserStatusType.AWAY );
				userStatusQueue.forceUserStatusToTheServer( UserStatusType.AWAY );
			} else {
				//If the current status is the away-like status then we simply 
				//force the server update just in case, because the user could not
				//have moved the mouse since the time he set the away-like status
				userStatusQueue.forceUserStatusToTheServer( userStatusQueue.getCurrentUserStatus() );
			}
		}
	}
	
	/**
	 * Allows to get the last time the user was detected active on site in milliseconds
	 * @return the last time the user was detected active on site in milliseconds
	 */
	public long getLastUserActivityMillisec() {
		return lastUserActivityMillisec;
	}
	
	/**
	 * Provides the instance of the status widget stored in this object.
	 * @return the user status widget associated with this manager
	 */
	public static UserStatusWidget getUserStatusWidget( ) {
		return getInstance().userStatusWidget;
	}
	
	/**
	 * Provides the instance of the status queue stored in this object.
	 * @return the user status queue associated with this manager
	 */
	public static UserStatusQueue getUserStatusQueue( ) {
		return getInstance().userStatusQueue;
	}
	
	/**
	 * Provides the instance of the status widget stored in this object.
	 * The instance is initialized with the given name of the logged in user
	 * @param userLoginName the user login name
	 * @return the user status widget associated with this manager
	 */
	public UserStatusWidget getUserStatusWidget( final String userLoginName ) {
		userStatusWidget.setUserLoginName( userLoginName );
		return userStatusWidget;
	}
	
	/**
	 * Allows to start automatic user status management
	 */
	public void start() {
		//Start the time-out timer
		this.scheduleRepeating( NO_ACTIVITY_CHECK_INTERVAL_MILLISEC );
		//Register the mouse move handler
		if( mouseModehandlerReg != null ) {
			//Just a safety check
			mouseModehandlerReg.removeHandler();
		}
		mouseModehandlerReg = userAreaPanel.addMouseMoveHandler( this );
		//Register the key press handler
		if( keyPressedHandlerReg != null ) {
			//Just a safety check
			keyPressedHandlerReg.removeHandler();
		}
		keyPressedHandlerReg = userAreaPanel.addKeyPressHandler( this );		
	}
	
	/**
	 * Allows to stop automatic user status management
	 */
	public void stop() {
		//Cancel the time-out timer
		this.cancel();
		//Remove mouse move handler
		if( mouseModehandlerReg != null ) {
			mouseModehandlerReg.removeHandler();
			mouseModehandlerReg = null;
		}
		//Remove mouse move handler
		if( keyPressedHandlerReg != null ) {
			keyPressedHandlerReg.removeHandler();
			keyPressedHandlerReg = null;
		}
	}

	@Override
	public void onKeyPress(KeyPressEvent event) {
		//Store the last time the key was pressed
		lastUserActivityMillisec = System.currentTimeMillis();
	}
}
