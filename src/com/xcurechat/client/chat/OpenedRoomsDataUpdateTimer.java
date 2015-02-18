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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.xcurechat.client.NewMessageAlertWidget;
import com.xcurechat.client.SiteManager;

import com.xcurechat.client.data.ChatRoomDataUpdate;
import com.xcurechat.client.data.ShortUserData;

import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.RoomManagerAsync;
import com.xcurechat.client.rpc.exceptions.UserStateException;

import com.xcurechat.client.userstatus.UserStatusManager;

import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.UserTreasureWidget;

/**
 * @author zapreevis
 * This class is used for periodically retrieving the list of opened room's users and messages
 */
class OpenedRoomsDataUpdateTimer extends Timer {
	//The periods with which we probe the server for updating the list of visible room users and the new messages
	private static final int OPENED_ROOMS_DATA_UPDATE_SHORT_INTERVAL_MILLISEC = 1000;
	private static final int OPENED_ROOMS_DATA_UPDATE_LONG_INTERVAL_MILLISEC = 10 * OPENED_ROOMS_DATA_UPDATE_SHORT_INTERVAL_MILLISEC;
	//The immediate update time interval
	private static final int IMMEDIATE_UPDATE_TIME_INTERVAL_MILLISEC = 10;
	
	//True if the room list updates should be done
	private boolean doUpdates = true;
	
	//True if the timer is already running repeatedly
	private boolean isRepeated = false;
	
	//The room ID is mapped to the ID of the last chat message retrieved from this room
	private Map<Integer, Integer> nextUpdateOldestMsgIDs = new HashMap<Integer, Integer>();
	
	//The set of opened room IDs for which we had errors when getting data
	private Set<Integer> problematicRoomIDs = new HashSet<Integer>();
	
	//Stores the current update time interval for the rooms data
	private int currentRoomsUpdateInterval = OPENED_ROOMS_DATA_UPDATE_LONG_INTERVAL_MILLISEC;
	
	//The instance of the current rooms manager
	private final RoomsManagerUI roomsManager;
	
	/**
	 * The basic constructor
	 * @param roomsManager the current rooms manager
	 */
	public OpenedRoomsDataUpdateTimer(final RoomsManagerUI roomsManager){
		super();
		//Store the reference to the rooms manager
		this.roomsManager = roomsManager;
	}
	
	/**
	 * Resets the object and schedules the next update to be almost immediate
	 */
	void start() {
		doUpdates = true;
		isRepeated = false;
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
	 * Removes the given room Id from the list of rooms for which we got
	 * errors while retrieving their actual data. Also removed it from
	 * the mapping of the room id to the oldest message id 
	 * @param roomID the room ID to be removed
	 */
	public void removeRoomFromDataUpdate( final int roomID ) {
		nextUpdateOldestMsgIDs.remove( roomID );
		problematicRoomIDs.remove( roomID );
	}
	
	/**
	 * Allows to schedule a single slow room update and then go back to repeating (fast) updates
	 */
	private void scheduleSlowUpdate() {
		if( currentRoomsUpdateInterval != OPENED_ROOMS_DATA_UPDATE_LONG_INTERVAL_MILLISEC ) {
			schedule( OPENED_ROOMS_DATA_UPDATE_LONG_INTERVAL_MILLISEC );
			isRepeated = false; //This is needed to go back to repeating (fast) updates 
		}
	}
	
	/**
	 * Schedules the repeating room updates with the currently set update time interval.
	 */
	private void scheduleRepeatingUpdates() {
		scheduleRepeating( currentRoomsUpdateInterval );
	}
	
	/**
	 * Allows to set the short rooms update interval, DOES reschedule the timer.
	 */
	public void doFastRoomDataUpdates() {
		currentRoomsUpdateInterval = OPENED_ROOMS_DATA_UPDATE_SHORT_INTERVAL_MILLISEC;
		scheduleRepeatingUpdates();
	}
	
	/**
	 * Allows to set the long rooms update interval, DOES reschedule the timer
	 */
	public void doSlowRoomDataUpdates() {
		currentRoomsUpdateInterval = OPENED_ROOMS_DATA_UPDATE_LONG_INTERVAL_MILLISEC;
		scheduleRepeatingUpdates();
	}
	
	public void run(){
		//Get the list of rooms and open the default room if needed
		final List<Integer> openedRoomIDs = new ArrayList<Integer>();
		openedRoomIDs.addAll( roomsManager.getOpenRoomIdToChatRoomUI().keySet() );
		openedRoomIDs.removeAll( problematicRoomIDs );
		if( ! openedRoomIDs.isEmpty() ) {
			//Ensure lazy loading
			(new SplitLoad(){
				@Override
				public void execute() {
					//The call-back object for getting the list of all available rooms
					final AsyncCallback<ChatRoomDataUpdate> callback = new AsyncCallback<ChatRoomDataUpdate>() {
						public void onSuccess(ChatRoomDataUpdate updateData) {
							if( doUpdates ) {
								nextUpdateOldestMsgIDs = updateData.nextUpdateOldestMsgIDs;
								
								//True if the actual user status was checked in this data array
								boolean wasUserChecked = false;
								
								//We initiate the new chat messages update, notify the message notifier
								NewMessageAlertWidget.getInstance().initiateChatMessagesUpdate();
								
								final Map<Integer, ChatRoomUI>  openRoomIdToChatRoomUI = roomsManager.getOpenRoomIdToChatRoomUI();
								final Iterator<Integer> roomIDIter = openRoomIdToChatRoomUI.keySet().iterator();
								while( roomIDIter.hasNext() ) {
									final int roomID = roomIDIter.next();
									//Add the problematic rooms to problematicRoomIDs
									if( updateData.roomIDToException.keySet().contains( roomID ) ) {
										problematicRoomIDs.add( roomID );
									}
									
									//Set the actual user online status and its gold pieces 
									if( ! wasUserChecked ) {
										final Map<Integer,ShortUserData> userIDToShortData = updateData.roomIDToVisibleUsers.get( roomID );
										if( userIDToShortData != null ) {
											final ShortUserData theLoggedInUser = userIDToShortData.get( SiteManager.getUserID() );
											if( theLoggedInUser != null ) {
												//Ensure delayed loading the this java script code
												final SplitLoad loader = new SplitLoad(){
													@Override
													public void execute() {
														SiteManager.setShortUserData( theLoggedInUser );
														UserTreasureWidget.getInstance().setGoldPieceCount( theLoggedInUser.getGoldPiecesCount() );
														UserStatusManager.getInstance().setActualChatUserStatus( theLoggedInUser.getUserStatus() );
													}
												};
												loader.execute();
												
												wasUserChecked = false;
											}
										}
									}
									
									//Update the lists of users in opened rooms and also add new messages
									ChatRoomUI roomInterface = openRoomIdToChatRoomUI.get( roomID );
									if( roomInterface != null ) {
										roomInterface.updateRoomActualData( updateData.roomIDToException.get( roomID ),
																			updateData.roomIDToVisibleUsers.get( roomID ),
																			updateData.roomIDToChatMessages.get( roomID ) );
									}
								}
								
								//Update the active room visitors
								ChatRoomUI.updateActiveRoomVisitors( updateData.activeRoomVisitors );
							}
						}
						
						public void onFailure(Throwable caught) {
							if( doUpdates ) {
								if ( caught instanceof UserStateException ) {
									//Report, that the user is not logged in, this will also log-out the interface
									(new SplitLoad( true ) {
										@Override
										public void execute() {
											//Report the error
											ErrorMessagesDialogUI.reportUserLoggedOut();
										}
									}).loadAndExecute();
								} else {
									//Re-schedule the next update, but take some longer time
									scheduleSlowUpdate();
								}
							}
						}
					};

					//Perform the update
					RoomManagerAsync roomMNGAsync = RPCAccessManager.getRoomManagerAsync();
					roomMNGAsync.getOpenedRoomsData( SiteManager.getUserID(), SiteManager.getUserSessionId(),
													 openedRoomIDs, nextUpdateOldestMsgIDs, callback);
				}
				@Override
				public void recover() {
					if( doUpdates ) {
						//Re-schedule the next update, but take some longer time
						scheduleSlowUpdate();
					}
				}
			}).loadAndExecute();
		}
		//For safety, just in case the request will be lost somehow, we schedule
		//an extra update, this update will be canceled if not needed.
		if( ! isRepeated ) {
			scheduleRepeatingUpdates();
			isRepeated = true;
		}
	}
}
