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
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.userstatus;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.UserManagerAsync;

import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.SplitLoad;

/**
 * @author zapreevis
 * This class represents the user status queue, it communicates
 * to the server and sets the statuses in the proper order
 */
public class UserStatusQueue {
	
	//The minimum period for automatic re-set of the user status (sending the actual status to the server) in millisec
	private static final long MIN_AUTO_STATUS_SET_PERIOD_MILLISEC = 10000;
	//This is the safety multiplier used to prevent/enforce the server status update
	private static final int SAFETY_TIME_MULTIPLIER = 1000;
	
	//Stores the instance of the user status widget
	private final UserStatusWidget statusWidget;
	//The last time the user status was forced to the server
	private long lastUserStatusInforcementMillisec = System.currentTimeMillis() + MIN_AUTO_STATUS_SET_PERIOD_MILLISEC;
	
	//The old user status that was successfully retrieved or set to the server
	private UserStatusType oldUserStatus = UserStatusType.FREE_FOR_CHAT;
	//The current user status that was successfully retrieved or set to the server
	private UserStatusType currentUserStatus = UserStatusType.FREE_FOR_CHAT;
	
	public UserStatusQueue( final UserStatusWidget statusWidget ) {
		this.statusWidget = statusWidget;
	}
	
	/**
	 * @return true if we can do the automatic enforcement of the user status to the server
	 */
	public boolean canDoUserStatusInforcement() {
		return (System.currentTimeMillis() - lastUserStatusInforcementMillisec) > MIN_AUTO_STATUS_SET_PERIOD_MILLISEC;
	}
	
	/**
	 * Allows to retrieve the previous user status.
	 * @return the previous user status
	 */
	public UserStatusType getPreviousUserStatus() {
		return this.oldUserStatus;
	}
	
	/**
	 * Allows to retrieve the current user status
	 * @return the current user status
	 */
	public UserStatusType getCurrentUserStatus() {
		return this.currentUserStatus;
	}
	
	/**
	 * Allows to force the user status. This method updates the status on the server
	 * Does nothing if the user is not logged in or the current status equals to the one we want to set
	 * @param userStatus the new user status
	 */
	void forceUserStatusToTheServer( final UserStatusType userStatus ) {
		if( SiteManager.isUserLoggedIn() ) {
			//Update the time when the user status was forced to be sent to the server
			lastUserStatusInforcementMillisec = System.currentTimeMillis();
			//Prepare for the server communication
			statusWidget.prepareForServerCommunication();
			
			//Ensure lazy loading
			(new SplitLoad(){
				@Override
				public void execute() {
					//Construct the call back object
					CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>(statusWidget.getProgressBarUI()) {
						public void onSuccessAct(Void result) {
							//Set the user status as the server update was successful
							//Nothing to be done here, this method only forces the user status to the server
							statusWidget.finishServerCommunication();
						}
						public void onFailureAct(Throwable caught) {
							//Use the recovery method
							recover();
						}
					};
					//Perform the server call
					UserManagerAsync userManagerObject = RPCAccessManager.getUserManagerAsync();
					userManagerObject.setUserStatus( SiteManager.getUserID(), SiteManager.getUserSessionId(), userStatus, callback );
				}
				@Override
				public void recover() {
					//The user status failed to be set on the server this time
					//There is not need to notify the user, also the
					//UserStatusManager will try to re-set the status
					statusWidget.finishServerCommunication();
				}
			}).loadAndExecute();
		}
	}
	
	/**
	 * Allows to set the new user status which will be forced to the server later by the UserStatusManager
	 * @param userStatus the new user statu to set
	 */
	public void setCurrentUserStatus( final UserStatusType userStatus ) {
		//Prevent the server status from the immediate update
		lastUserStatusInforcementMillisec = System.currentTimeMillis() + SAFETY_TIME_MULTIPLIER * MIN_AUTO_STATUS_SET_PERIOD_MILLISEC;
		
		//Save the old user status, if the new user status is the same as the current one then the old status should stay intact
		oldUserStatus = (userStatus != currentUserStatus ) ? currentUserStatus : oldUserStatus ; 
		//Set the new current user status
		currentUserStatus = userStatus;
		
		//Set the user status inside the widget
		statusWidget.setCurrentUserStatus( userStatus );
		
		//Force the server status to the immediate update
		lastUserStatusInforcementMillisec = System.currentTimeMillis() - SAFETY_TIME_MULTIPLIER * MIN_AUTO_STATUS_SET_PERIOD_MILLISEC;
	}
}
