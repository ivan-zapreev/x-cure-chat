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
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client.chat;

import java.util.Map;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.xcurechat.client.SiteManager;
import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;
import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.RoomManagerAsync;
import com.xcurechat.client.rpc.exceptions.RoomAccessException;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.UserStateException;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This class is used for periodically retrieving the list
 * of active rooms from the server. 
 */
public class RoomListUpdateTimer extends Timer {
	//The immediate update time interval
	private static final int IMMEDIATE_UPDATE_TIME_INTERVAL_MILLISEC = 10;
	
	//True if the room list updates should be done
	private boolean doUpdates = true;
	
	//True if the previously opened rooms, stored in the cookies, were already re-opened
	private boolean wereRoomsReOpened = false;
	
	//True if the timer is already running repeatedly
	private boolean isRepeated = false;
	
	//The instance of the current rooms manager
	private final RoomsManagerUI roomsManager;
	
	/**
	 * The basic constructor
	 * @param roomsManager the current rooms manager
	 */
	public RoomListUpdateTimer(final RoomsManagerUI roomsManager){
		super();
		//Store the reference to the rooms manager
		this.roomsManager = roomsManager;
	}
	
	/**
	 * Resets the object data, has to be called after the object is stopped and before it is re-started
	 */
	private void reset() {
		doUpdates = true;
		wereRoomsReOpened = false;
		isRepeated = false;
	}
	
	/**
	 * Resets the object and schedules the next update to be almost immediate
	 */
	void start() {
		reset();
		schedule( IMMEDIATE_UPDATE_TIME_INTERVAL_MILLISEC );
	}
	
	/**
	 * Cancels the timer, stops the updates
	 */
	void stop() {
		doUpdates = false;
		cancel();
	}
	
	/**
	 * Retrieves the list of opened rooms from the cookies and tries to re-open these rooms
	 */
	private void reopenRooms() {
		//Re-open all of the other previously-opened rooms, if not done yet. If after 
		//opening the saved rooms there is no opened room then open the default one.s
		if( ! wereRoomsReOpened ) {
			roomsManager.openNewRooms( SiteManager.getOpenedRoomIds(), true );
			wereRoomsReOpened = true;
		}
	}
	
	public void run(){
		//Ensure lazy loading
		(new SplitLoad(){
			@Override
			public void execute() {
				//Get the list of all available rooms callback
				AsyncCallback<Map<Integer, ChatRoomData>> callback = new AsyncCallback<Map<Integer, ChatRoomData>>() {
					public void onSuccess(Map<Integer, ChatRoomData> availableRooms) {
						if( doUpdates ) {
							//Update the list of rooms
							roomsManager.registerAvailableRooms( availableRooms );
							
							//Update the opened tab's room trees
							ChatRoomUI.updateRoomsTree( availableRooms, roomsManager );
							
							//Update the opened rooms data
							roomsManager.updateOpenedRoomsData();
							
							//Open the default chat room and/or other previously opened rooms, if any
							reopenRooms();
						}
					}
					 
					public void onFailure(final Throwable caught) {
						if( doUpdates ) {
							if ( caught instanceof UserStateException ) {
								//Report, that the user is not logged in
								(new SplitLoad( true ) {
									@Override
									public void execute() {
										//Report the error
										ErrorMessagesDialogUI.reportUserLoggedOut();
									}
								}).loadAndExecute();
							} else {
								//If this exception is NOT DUE to the user not being logged in
								if( ! ( caught instanceof SiteException ) ) {
									//If this NOT A SITE EXCEPTION then we report it into the chat room
									RoomAccessException roomAccEx = new RoomAccessException( ErrorMessagesDialogUI.getNonSiteExceptionMessage( caught ) );
									//Put the error message into the selected chat room tab
									final Map<Integer, ChatRoomUI> openRoomIdToChatRoomUI = roomsManager.getOpenRoomIdToChatRoomUI();
									for( ChatRoomUI roomUI : openRoomIdToChatRoomUI.values() ) {
										if( roomUI.isTabSelected() ) {
											roomUI.appendRoomErrorMessage( roomAccEx );
											break;
										}
									}
								} else {
									(new SplitLoad( true ) {
										@Override
										public void execute() {
											//If it is SOME OTHER SITE EXCEPTION, but not the user status
											//one, then we open an error message dialog, but this should not be happening
											ErrorMessagesDialogUI.openErrorDialog( caught );
										}
									}).loadAndExecute();
								}
							}
						}
					}
				};
				
				//Get the list of rooms and open the default room if needed
				RoomManagerAsync roomMNGAsync = RPCAccessManager.getRoomManagerAsync();
				roomMNGAsync.getAllRooms( SiteManager.getUserID(), SiteManager.getUserSessionId(), callback);
			}
		}).loadAndExecute();
		
		//Initiate the repeated timer action here, because the
		//first execution should happen with no delay
		if( ! isRepeated ) {
			this.scheduleRepeating( CommonResourcesContainer.ACTIVE_ROOMS_TREE_UPDATE_INTERVAL_MILLISEC );
			isRepeated = true;
		}
	}
}
