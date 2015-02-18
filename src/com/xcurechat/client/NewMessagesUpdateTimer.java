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
 * The user interface package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.rpc.MessageManagerAsync;
import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.exceptions.UserStateException;
import com.xcurechat.client.utils.SplitLoad;

/**
 * @author zapreevis
 * This class is responsible for checking for the new messages
 * and updating the messages menu entry
 */
public class NewMessagesUpdateTimer extends Timer {
	
	//The minimum and the maximum server probing time for new messages
	private static final int MIN_SERVER_PROBING_INTERVAL_MILLISEC = 3000;
	private static final int MAX_SERVER_PROBING_INTERVAL_MILLISEC = 30000;
	
	//True if the new messages checks should be done
	private boolean doUpdates = true;
	
	//The last number of new messages
	private int lastNumberOfNewMsgs = 0;
	//The current interval for probing the server for new messages
	private int probingIntMillisec = MIN_SERVER_PROBING_INTERVAL_MILLISEC;
	//The menu manager that should be notified in an event of a new message
	private final MainSiteMenuUI siteMenu;
	
	/**
	 * The new messages update times is used for the new offline messages
	 * updates that are indicated in the main site when the user is logged in 
	 * @param siteMenu the main site menu instance, not null!
	 */
	public NewMessagesUpdateTimer( final MainSiteMenuUI siteMenu ){
		super();
		this.siteMenu = siteMenu;
	}
	
	/**
	 * Allows to increase/decrease the server probing interval
	 * in case we receive/read unread msgs.
	 * @param newNumberOfNewMsgs the new number of msgs from the server
	 */
	private void updateProbingInterval( final int newNumberOfNewMsgs ){
		//Notify the user about the new off-line message if we got a new message
		if( lastNumberOfNewMsgs < newNumberOfNewMsgs ) {
			NewMessageAlertWidget.getInstance().newOfflineMessage();
		}
		
		//Adjust the periodicity for getting the server updates
		if( lastNumberOfNewMsgs != newNumberOfNewMsgs ){
			//Decrease the probing interval
			probingIntMillisec /= 2;
		} else {
			//Increase the probing interval
			probingIntMillisec *= 2;
		}
		if( probingIntMillisec < MIN_SERVER_PROBING_INTERVAL_MILLISEC ) {
			probingIntMillisec = MIN_SERVER_PROBING_INTERVAL_MILLISEC;
		} else {
			if( probingIntMillisec > MAX_SERVER_PROBING_INTERVAL_MILLISEC ) {
				probingIntMillisec = MAX_SERVER_PROBING_INTERVAL_MILLISEC;
			}
		}
		lastNumberOfNewMsgs = newNumberOfNewMsgs;
	}
	
	/**
	 * Allows to initiate fast updates for some time
	 */
	public void speedUpUpdates() {
		//Cancel any scheduled activity
		cancel();
		//Increase the update rate to maximum
		probingIntMillisec = MIN_SERVER_PROBING_INTERVAL_MILLISEC;
		//Schedule the next update from the server
		schedule( probingIntMillisec );
	}
	
	/**
	 * Allows to start new message updates
	 */
	public void startUpdates() {
		doUpdates = true;
		schedule(10);
	}
	
	/**
	 * Allows to stop new message updates
	 */
	public void stopUpdates() {
		doUpdates = false;
		cancel();
	}
	
	public void run() {
		//Ensure lazy loading
		(new SplitLoad(){
			@Override
			public void execute() {
				AsyncCallback<Integer> callback = new AsyncCallback<Integer>() {
					public void onSuccess(Integer newNumberOfNewMsgs) {
						if( doUpdates ) {
							//Set the new number of unread messages for the menu
							siteMenu.setNewNumberOfMessages( newNumberOfNewMsgs );
							
							//Update the interval for probing the server
							updateProbingInterval( newNumberOfNewMsgs );
							
							//Re-schedule the next update from the server
							cancel(); schedule( probingIntMillisec );
						}
					}
					 
					public void onFailure(Throwable caught) {
						if( doUpdates ) {
							if ( caught instanceof UserStateException ) {
								//If somehow the user was logged out then we do not
								//show any error messages, but simply stop updates
								siteMenu.stopNewMsgChecks();
								//Report, that the user is not logged in
								(new SplitLoad( true ) {
									@Override
									public void execute() {
										//Report the error
										ErrorMessagesDialogUI.reportUserLoggedOut();
									}
								}).loadAndExecute();
							} else {
								//Re-schedule the next update from the server
								cancel(); schedule( probingIntMillisec );
							}
						}
					}
				};
				//Get the number of new messages
				MessageManagerAsync messageManagerAsync = RPCAccessManager.getMessageManagerAsync();
				messageManagerAsync.countNewMessages( SiteManager.getUserID(), SiteManager.getUserSessionId(), callback);
			}
			@Override
			public void recover() {
				if( doUpdates ) {
					//Re-schedule the next update from the server
					cancel(); schedule( probingIntMillisec );
				}
			}
		}).loadAndExecute();
		//For safety, just in case the request will be lost somehow, we schedule
		//an extra update, this update will be cancelled if not needed.
		if( doUpdates ) {
			cancel(); schedule( MAX_SERVER_PROBING_INTERVAL_MILLISEC );
		}
	}
}
