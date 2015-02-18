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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;

import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.DecoratedTabPanel;

import com.google.gwt.user.client.rpc.AsyncCallback;

import com.xcurechat.client.SiteBodySectionContent;
import com.xcurechat.client.SiteManager;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.rpc.RoomManagerAsync;
import com.xcurechat.client.rpc.exceptions.RoomAccessException;

import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.UserAvatarImageWidget;

import com.xcurechat.client.chat.messages.SendChatMessageManager;

import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.data.ShortUserData;

import com.xcurechat.client.dialogs.messages.MessagesManagerDialogUI;
import com.xcurechat.client.dialogs.room.AskForRoomAccessDialogUI;
import com.xcurechat.client.dialogs.room.RoomsManagerDialogUI;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.i18n.I18NManager;

/**
 * @author zapreevis
 * This class is responsible for managing the chat rooms.
 */
public class RoomsManagerUI extends Composite implements SiteBodySectionContent, UserAvatarImageWidget.AvatarSpoilerChangeListener {
	
	//The maximum allowed number of opened chat rooms
	private static final int MAX_ALLOWED_OPEN_ROOMS = 5;
	
	//The main tab panel with all the opened rooms
	private DecoratedTabPanel theOpenRoomTabs = new DecoratedTabPanel();
	//First room opening progress bar image
	private Image progressImage = new Image( ServerSideAccessManager.getActivityImageURL() ); 
	//Maps the open room ids to the room UI that is in the tab 
	private Map<Integer, ChatRoomUI> openRoomIdToChatRoomUI = new HashMap<Integer, ChatRoomUI>();
	
	//Maps the room id to the list of room-data objects that came from the server
	private Map<Integer, ChatRoomData> availableRooms = new HashMap<Integer, ChatRoomData>();

	//The room list update timer, we can use the same instance without creating a new
	//timer, as we do in MainSiteMenu, because on login/logout we create/discared the
	//RoomsManagerUI so we always get a fresh instance of RoomListUpdateTimer.
	private RoomListUpdateTimer roomListUpdateTimer = null;
	
	//The timer responsible for the periodic updates of the opened rooms' data
	private OpenedRoomsDataUpdateTimer openedRoomUpdateTimer = null;
	//The site section history prefix
	private final String siteSectionPrefix; 

	/**
	 * The simple constructor that opens the main room of the chat system.
	 * This basic constructor should be provided with the site section url prefix
	 * @param siteSectionPrefix the history token site section prefix
	 */
	RoomsManagerUI( final String siteSectionPrefix ){
		super();
		
		//Store the data
		this.siteSectionPrefix = siteSectionPrefix;
		
		//Set the room's manager size. We want to take as much space as possible
		//also leave some (20) pixels left for the UI decorations, paddings and margins
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setSize( "100%", "100%");
		verticalPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER);
		verticalPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE);
		
		//Add the rooms tab
		theOpenRoomTabs.setWidth( "100%");
		theOpenRoomTabs.setAnimationEnabled(false);
		theOpenRoomTabs.addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>(){
			public void onBeforeSelection( BeforeSelectionEvent<Integer> e ){
				final int tabIndex = e.getItem();
				//Disable the tab title for the opened room title which is to be
				//unselected, enable the tab which has to be selected
				for( int index = 0; index < theOpenRoomTabs.getWidgetCount(); index++ ) {
					//Do not un-select the tab which we are about to select
					if( tabIndex != index ) {
						ChatRoomUI chatRoomUI = getSelectedRoomUI( index );
						if( ( chatRoomUI != null ) && ( chatRoomUI.isTabSelected() ) ) {
							chatRoomUI.onBeforeTabUnselected();
						}
					} else {
						//Do required preparations for the tab that is about to be selected
						//E.g. enable its close button and put the rooms tree into it
						getSelectedRoomUI( tabIndex ).onBeforeTabSelected();
					}
				}
			}
		});
		theOpenRoomTabs.addSelectionHandler(new SelectionHandler<Integer>(){
			public void onSelection( SelectionEvent<Integer> e ){
				//Update the splitter position
				getSelectedRoomUI( e.getSelectedItem() ).updateUIElements();
			}
		});
		verticalPanel.add( theOpenRoomTabs );
		
		//Add the progress bar image
		verticalPanel.add( progressImage );
		
		//All composites must call initWidget() in their constructors.
		initWidget(verticalPanel);

		//Give the a style name to the chat-room composite.
		setStyleName(CommonResourcesContainer.OPEN_CHAT_ROOM_STYLE_NAME);
	}
	
	/**
	 * Allows to update the list of available rooms
	 * @param availableRooms the currently available rooms
	 */
	public void registerAvailableRooms( Map<Integer, ChatRoomData> availableRooms ) {
		this.availableRooms = availableRooms;
	}
	
	/**
	 * Allows to start the chat rooms updates
	 * Should be called when the user is logged into the chat system.
	 */
	private void startRoomUpdates() {
		//Stop the current timers if any
		stopRoomUpdates();
		
		//Ensure delayed loading the this java script code
		final SplitLoad loader = new SplitLoad(){
			@Override
			public void execute() {
				//Instantiate the timer
				roomListUpdateTimer = new RoomListUpdateTimer( RoomsManagerUI.this );
				//Re-Initiate the room-list update sequence, and also open the main chat room
				roomListUpdateTimer.start();
		
				//Instantiate the timer
				openedRoomUpdateTimer = new OpenedRoomsDataUpdateTimer( RoomsManagerUI.this );
				//Start the timer that updates the opened room's data
				openedRoomUpdateTimer.start();
			}
		};
		loader.loadAndExecute();
	}
	
	/**
	 * Stops the available room list updates and the opened rooms' data updates done by timer.
	 * Should be called when the user is logged out from the chat system.
	 */
	private void stopRoomUpdates() {
		if( roomListUpdateTimer != null ) {
			//Stop the available room list updates
			roomListUpdateTimer.stop();
			//Remove the timer
			roomListUpdateTimer = null;
		}
		
		if( openedRoomUpdateTimer != null ) {
			//Stop the opened rooms' data updates 
			openedRoomUpdateTimer.stop();
			//Remove the timer
			openedRoomUpdateTimer = null;
		}
	}
	
	/**
	 * Allows to close all room tabs without calling the server
	 * for exiting the room. Also does not make the default room to be reopen.
	 * This method should be called on the user logout.
	 */
	private void closeAllRoomTabs() {
		Iterator<Integer> idsOfOpenRooms = openRoomIdToChatRoomUI.keySet().iterator();
		while( idsOfOpenRooms.hasNext() ) {
			//Close the room tab without reopening the default room
			//in case of no open room tabs left
			closeRoomTab( idsOfOpenRooms.next(), false );
		}
	}
	
	/**
	 * Allows to get the mapping between the currently open room ids and their UI widgets
	 * @return a copy map of the internally stored hash map, not null!
	 */
	Map<Integer, ChatRoomUI> getOpenRoomIdToChatRoomUI() {
		Map<Integer, ChatRoomUI> result = new HashMap<Integer, ChatRoomUI>();
		result.putAll( openRoomIdToChatRoomUI );
		return result;
	}
	
	/**
	 * This method updates the char room data in the room's
	 * tree and the opened room's tab. If the room is not in
	 * the list then the it automatically is added.
	 * This allows to make an instanteneous appearing/update
	 * of the room in the local interface.
	 * @param roomData the updated data object
	 */
	public void afterLocalRoomAddUpdate(ChatRoomData roomData) {
		if( (availableRooms != null) && (roomData != null) &&
			( !roomData.isExpired() || roomData.getRoomDurationTimeHours() != ChatRoomData.UNKNOWN_HOURS_DURATION ) ) {
			final int roomID = roomData.getRoomID();
			//If the room is created or updated and is online  
			if( roomData.getRoomDurationTimeHours() != ChatRoomData.CLEAN_HOURS_DURATION ) {
				//Update the array of rooms
				availableRooms.put( roomID, roomData );
				//Update the opened room's tab title
				if( openRoomIdToChatRoomUI.keySet().contains( roomID ) ) {
					openRoomIdToChatRoomUI.get( roomID).updateRoomData( roomData );
				}
			} else {
				//If we clean up rooms duration, i.e. send it offline then we remove the room
				availableRooms.remove( roomID );
			}
			//Update the opened tab's room trees
			ChatRoomUI.updateRoomsTree( availableRooms, this );
		}
	}
	
	/**
	 * @return the if of the main room, if it is found in the list of available 
	 * 			rooms otherwise it returns ChatRoomData.UNKNOWN_ROOM_ID. 
	 */
	private int getMainRoomID() {
		Iterator<ChatRoomData> iter = availableRooms.values().iterator();
		while( iter.hasNext() ) {
			ChatRoomData roomData = iter.next();
			//If we found the main room
			if( roomData.isMain() ){
				return roomData.getRoomID();
			}
		}
		return ChatRoomData.UNKNOWN_ROOM_ID;
	}
	
	/**
	 * Goes through the opened rooms and updates their
	 * data from the list of available rooms.
	 */
	void updateOpenedRoomsData( ) {
		Iterator<Integer> iter = openRoomIdToChatRoomUI.keySet().iterator();
		while( iter.hasNext() ){
			int roomId = iter.next();
			openRoomIdToChatRoomUI.get( roomId ).updateRoomData( availableRooms.get( roomId ) );
		}
	}
	
	/**
	 * Returns the set of opened room's visible users
	 * @param roomID the id of an opened room to get the users from
	 * @return the mapping from the users ID to the short user data
	 * or null if the room is not open
	 */
	public Map<Integer, ShortUserData> getRoomVisibleUsers(final int roomID) {
		ChatRoomUI roomUI = openRoomIdToChatRoomUI.get( roomID );
		if( roomUI != null ) {
			return roomUI.getRoomVisibleUsers();
		}
		return null;
	}
	
	/**
	 * Allows to get the name of the opened chat room
	 * @param roomID the id of the room we want to get the name for
	 * @return the opened room name or an empty string if the room is not open
	 */
	public String getOpenedRoomName( final int roomID ) {
		ChatRoomUI roomUI = openRoomIdToChatRoomUI.get( roomID );
		if( roomUI != null ) {
			ChatRoomData roomData = roomUI.getChatRoomData();
			if( roomData != null ) {
				return ChatRoomData.getRoomName( roomData );
			} else {
				return "";
			}
		} else {
			return "";
		}
	}
	
	/**
	 * Allows to retrieve a room's data by id, if it is in the list of active rooms
	 * @param roomId the id of the room we want to get data for
	 * @return the ChatRoomData of the needed room or null if the room is not active is not stored here.
	 */
	public ChatRoomData getActiveRoomData( final int roomId ) {
		return availableRooms.get( roomId );
	}
	
	/**
	 * Allows to put a received exception into the chat room.
	 * If the room is not open then nothing happens.
	 * If the provided exception is not an instance of the RoomAccessException
	 * then the error message is reported in the dialog window otherwise it is
	 * placed into the list of chat room messages.
	 * @param roomID the room ID
	 * @param exception the exception 
	 */
	public void appendRoomErrorMessage( final int roomID, final Throwable exception) {
		ChatRoomUI roomInterface = openRoomIdToChatRoomUI.get( roomID );
		if( roomInterface != null ) {
			roomInterface.appendRoomErrorMessage( exception );
		}
	}
	
	/**
	 * Get the ChatRoomUI object for the selected room index.
	 * @param tabIndex the selected room index
	 * @return the corresponding CharRoomUI object
	 */
	private ChatRoomUI getSelectedRoomUI( int tabIndex ) {
		return (ChatRoomUI) ( (FocusPanel) theOpenRoomTabs.getWidget(tabIndex) ).getWidget();
	}
	
	/**
	 * This method can be used when the room is deleted from the
	 * server. Here we make the removal look like being instanteneous.
	 * @param roomId the id of the room to close.
	 */
	public void afterLocalRoomsDelete(final List<Integer> roomIDs) {
		if( ! roomIDs.isEmpty() ) {
			for(int i=0; i < roomIDs.size(); i++) {
				final int roomId = roomIDs.get( i ); 
				//Since this method is called if the room was
				//deleted from the server, we delete the room
				//from the list of available rooms right away
				availableRooms.remove( roomId );
			}
			//Update the opened tab's room trees
			ChatRoomUI.updateRoomsTree( availableRooms, this );
		}
	}
	
	/**
	 * Closes the room includes the RPC call to the server to exit the room.
	 * @param roomId the id of the room to close.
	 */
	public void closeRoom(final int roomId) {
		//Ensure lazy loading
		(new SplitLoad(){
			@Override
			public void execute() {
				AsyncCallback<Void> callback = new AsyncCallback<Void>() {
					public void onSuccess(Void result) {
						//Close the room's tab
						closeRoomTab(roomId);
					}
					public void onFailure(final Throwable caught) {
						//Report the error
						(new SplitLoad( true ) {
							@Override
							public void execute() {
								ErrorMessagesDialogUI.openErrorDialog(caught);
							}
						}).loadAndExecute();
						//Do the recovery
						recover();
					}
				};
				
				RoomManagerAsync roomManager = RPCAccessManager.getRoomManagerAsync();
				roomManager.leaveRoom( SiteManager.getUserID(), SiteManager.getUserSessionId(), roomId, callback );
			}
			@Override
			public void recover() {
				//Close the room's tab
				closeRoomTab(roomId);
			}
		}).loadAndExecute();
	}
	/**
	 * Closes the opened room tab. Always checks that there are mote than zero open 
	 * rooms left, if not, then it opend the default room.
	 * @param roomId the id of the room that is opened in the tab we want to close
	 */
	private void closeRoomTab( final int roomId ) {
		closeRoomTab( roomId, true );
	}
	
	/**
	 * Closes the opened room tab
	 * @param roomId the id of the room that is opened in the tab we want to close
	 * @param openDefaultIfNone if set to true then the default room is opened
	 * in case we closed the last open room.
	 */
	private void closeRoomTab( final int roomId, final  boolean openDefaultIfNone ) {
		//Get the widget tab to remove
		final Widget tab = openRoomIdToChatRoomUI.get(roomId).getParent();
		
		//Remove the room id from the open-room mappings
		openRoomIdToChatRoomUI.remove(roomId);
		
		//Remove the room from the set of problematic rooms and the room id to the oldest msg id mapping
		if( openedRoomUpdateTimer != null ) {
			//Note that we check here for the timer being present because if
			//the room updates are stopped then there is no timer any more
			openedRoomUpdateTimer.removeRoomFromDataUpdate( roomId );
		}
		
		//Remove the opened room from the list of opened rooms stored in the coockies
		SiteManager.removeOpenedRoomId(roomId);
		
		//Close the room's tab
		theOpenRoomTabs.remove( tab );
		
		//Select the last opened room if there are tabs left and there is no selected tab.
		if( theOpenRoomTabs.getWidgetCount() > 0  ) {
			theOpenRoomTabs.selectTab( theOpenRoomTabs.getWidgetCount()-1 );
		}
		
		if( openDefaultIfNone ) {
			//Just in case we check that if there is no opened room left, then we open the default one
			if( openRoomIdToChatRoomUI.keySet().size() == 0 ) {
				openNewRoom( getMainRoomID() );
			}
		}
	}
	
	/**
	 * Opens the room and creates its UI elements.
	 * @param roomId the id of the room to open.
	 */
	public void openNewRoom(final int roomId) {
		List<Integer> roomIds = new ArrayList<Integer>();
		roomIds.add( roomId );
		openNewRooms( roomIds, false );
	}
	
	/**
	 * Opens the rooms and creates its UI elements.
	 * The rooms are opened in a sequential manners. I.e. we 
	 * do not open a room unless the previous room is opened.
	 * @param roomIds the ids of the room to open
	 * @param openMain if true then we open the main room,
	 * after opening the rooms from roomIds. The latter happens
	 * if none of the rooms from roomIds could be opened.  
	 */
	public void openNewRooms(final List<Integer> roomIds, final boolean openMain) {
		openNewRooms( roomIds, openMain, 0);
	}
	
	/**
	 * Opens the rooms and creates its UI elements. The rooms are opened in a sequential
	 * manners. I.e. we do not open a room unless the previous room is opened.
	 * This method is recursive, since we want to open rooms in the roomIds one by one 
	 * @param roomIds the ids of the room to open
	 * @param openMain if true then we open the main room,
	 * after opening the rooms from roomIds. The latter happens
	 * if none of the rooms from roomIds could be opened.
	 * @param index the index of the room that we will try to open  
	 */
	private void openNewRooms(final List<Integer> roomIds, final boolean openMain, final int index) {
		if( index < roomIds.size() ) {
			//Get the rooms list and look for the room we need.
			final int roomID = roomIds.get( index );
			final ChatRoomData theRoomDataObject = (ChatRoomData) availableRooms.get( roomID );
			//Check that this room is online, i.e. is in the list of available rooms
			if( theRoomDataObject != null ) {
				//Check that this room is not opened yet
				if( !openRoomIdToChatRoomUI.keySet().contains( roomID) ) {
					//Check that we do not open more than maximum allowed number of rooms
					if( openRoomIdToChatRoomUI.size() < MAX_ALLOWED_OPEN_ROOMS ) {
						//Ensure lazy loading
						(new SplitLoad(){
							@Override
							public void execute() {
								AsyncCallback<Void> callback = new AsyncCallback<Void>() {
									public void onSuccess( Void result ) {
										//Create the room's title 
										final OpenedRoomTitlePanel roomTitleWidget = new OpenedRoomTitlePanel( theRoomDataObject, RoomsManagerUI.this );
										//Create the room interface from the room data
										final ChatRoomUI theRequiredRoom = new ChatRoomUI( theRoomDataObject, roomTitleWidget, RoomsManagerUI.this );
										
										//Add the room to the list of opened rooms.
										openRoomIdToChatRoomUI.put( theRoomDataObject.getRoomID(), theRequiredRoom );
										
										//Add the new opened room to the tabs
										Widget tab = new FocusPanel( theRequiredRoom );
										tab.setSize("100%", "100%");
										
										//Hide the progress bar image
										progressImage.setVisible(false);
										
										//Add the room's tab to the tabbed panel
										theOpenRoomTabs.add( tab, roomTitleWidget );
										
										//Select the newly opened room tab
										theOpenRoomTabs.selectTab( theOpenRoomTabs.getWidgetIndex( tab ) );
										
										//Store the opened room in the cookie
										SiteManager.addOpenedRoomId( roomID );
										
										//Continue opening the rooms
										openNewRooms( roomIds, openMain, index+1 );
									}
									
									public void onFailure( final Throwable caught ) {
										//If there is no room access right then we open the
										//dialog so that the user can send a room-access request 
										if( ( caught instanceof RoomAccessException ) &&
											( (RoomAccessException) caught ).getErrorCodes().contains( RoomAccessException.THE_USER_DOES_NOT_HAVE_ROOM_ACCESS_ERROR ) ) {
											//Ensure lazy loading
											( new SplitLoad( true ) {
												@Override
												public void execute() {
													AskForRoomAccessDialogUI requestDialog = new AskForRoomAccessDialogUI( theRoomDataObject );
													requestDialog.show();
													requestDialog.center();
												}
											}).loadAndExecute();
										} else {
											(new SplitLoad( true ) {
												@Override
												public void execute() {
													//Report the error
													ErrorMessagesDialogUI.openErrorDialog( caught );
												}
											}).loadAndExecute();
										}
										//Continue opening the rooms
										openNewRooms( roomIds, openMain, index+1 );
									}
								};
								RoomManagerAsync roomManager = RPCAccessManager.getRoomManagerAsync();
								roomManager.enterRoom( SiteManager.getUserID(), SiteManager.getUserSessionId(), roomID, callback );
							}
							@Override
							public void recover() {
								//Continue opening the rooms
								openNewRooms( roomIds, openMain, index+1 );
							}
						}).loadAndExecute();
					} else {
						//We already have the maximum amount of opened rooms so we do not try to 
						//open the remaining ones. We remove them from the rooms stored in coockies
						for( int i = index; i < roomIds.size(); i++ ) {
							SiteManager.removeOpenedRoomId( roomIds.get( i ) );
						}
						//Open the error dialog
						(new SplitLoad( true ) {
							@Override
							public void execute() {
								//Report the error
								ErrorMessagesDialogUI.openErrorDialog( I18NManager.getErrors().maximumAllowedNumberOfOpenRoomsError( MAX_ALLOWED_OPEN_ROOMS ) );
							}
						}).loadAndExecute();
					}
				} else {
					theOpenRoomTabs.selectTab( theOpenRoomTabs.getWidgetIndex( openRoomIdToChatRoomUI.get( roomID ).getParent() ) );
					//Continue opening the rooms
					openNewRooms( roomIds, openMain, index+1 );
				}
			} else {
				//If the room is not online, then we remove it from
				//the list of opened rooms in the coockies 
				SiteManager.removeOpenedRoomId( roomID );
				//Continue opening the rooms
				openNewRooms( roomIds, openMain, index+1 );
			}
		} else {
			//All of the rooms from the list are opened, now it is time to check how many are 
			//these and if there are no opened rooms, then we open the default one, if needed.
			if( openMain && ( openRoomIdToChatRoomUI.keySet().size() == 0 ) ) {
				openNewRoom( getMainRoomID() );
			}
		}
	}

	/**
	 * Allows to set on the alerts on the user avatars, the alerts are set on the
	 * avatars which belong to the messages directly addressing the current user.
	 * @param isOn true if we want to set the avatars constantly on, false for "constantly" off
	 */
	public void setAvatarAlerts( final boolean isOn ) {
		Collection<ChatRoomUI> openRoomUIs = openRoomIdToChatRoomUI.values();
		for( ChatRoomUI room : openRoomUIs ) {
			room.setAvatarAlerts( isOn );
		}
	}
	
	/**
	 * Allows to check if there is more than one open room
	 * @return true if there is more than one open room, otherwise false
	 */
	boolean hasMoreThanOneRoomOpened() {
		return (openRoomIdToChatRoomUI.keySet().size() > 1);
	}
	
	/**
	 * Allows to open the rooms manager dialog. The reason we do it here is
	 * because of the instance of the rooms manager that has to be used.
	 */
	public void openRoomsManagerDialog() {
		(new SplitLoad( true ) {
			@Override
			public void execute() {
				RoomsManagerDialogUI roomsDialog = new RoomsManagerDialogUI( SiteManager.getUserID(),
																			 SiteManager.getUserLoginName(),
																			 null, RoomsManagerUI.this );
				roomsDialog.show();
				roomsDialog.center();
			}
		}).loadAndExecute();
	}
	
	/**
	 * Allows to open the messages manager. The reason we do it here is
	 * because of the instance of the rooms manager that has to be used.
	 */
	public void openMessagesManagerDialog() {
		(new SplitLoad( true ) {
			@Override
			public void execute() {
				MessagesManagerDialogUI messagesDialog = new MessagesManagerDialogUI( SiteManager.getUserID(),
																					  SiteManager.getUserLoginName(),
																					  null, RoomsManagerUI.this );
				messagesDialog.show();
				messagesDialog.center();
			}
		}).loadAndExecute();
	}
	
	@Override
	public void onBeforeComponentIsAdded() {
		//Show the send-chat message dialog if is is used for sending messages
		SendChatMessageManager.getInstance().sendMessageDialogsShow(true);
		//If the user is logged in, set the fast chat-room data updates
		if( SiteManager.isUserLoggedIn() ) {
			if( openedRoomUpdateTimer != null ) {
				openedRoomUpdateTimer.doFastRoomDataUpdates();
			} else {
				//This should not be happening, ever
				Window.alert("The room update timer is not ready!");
			}
		}
	}
	
	@Override
	public void onBeforeComponentIsRemoved() {
		//Hide the send-chat message dialog if it is used for sending messages
		SendChatMessageManager.getInstance().sendMessageDialogsShow(false);
		//If the user is logged in, set the slow chat room data updates
		if( SiteManager.isUserLoggedIn() ) {
			if( openedRoomUpdateTimer != null ) {
				openedRoomUpdateTimer.doSlowRoomDataUpdates();
			} else {
				//This should not be happening, ever
				Window.alert("The room update timer is not ready!");
			}
		}
		
		//Needed for Opera, because it resets the UI font settings
		SendChatMessageManager.getInstance().rememberChatMessageFonts();
	}

	@Override
	public void onAfterComponentIsAdded() {
		//1. Scroll down all of the chat message panels, i.e. for all of the open chat rooms
		//2. Update the chat room UI elements to prevent size breaking after viewing the
		//   forum or other site sections
		for( ChatRoomUI roomUI : openRoomIdToChatRoomUI.values() ) {
			roomUI.scrollMessagesPanelDown();
			roomUI.updateUIElements();
		}
		//Update the current history item with the selected section item
		History.newItem( siteSectionPrefix, false );
		
		//Update the UI elements of the send-chat-message UI
		SendChatMessageManager.getInstance().updateUIElements();
		
		//Needed for Opera, because it resets the UI font settings
		SendChatMessageManager.getInstance().restoreChatMessageFonts();
	}
	
	@Override
	public void processHistoryToken(String historyToken) {
		//INFO: Currently there is nothing to be done here,
		//but perhaps we should select one of the opened
		//rooms, in case this is decided, for now do nothing
	}
	
	@Override
	public void updateUIElements() {
		for( ChatRoomUI roomUI : openRoomIdToChatRoomUI.values() ) {
			roomUI.updateUIElements();
		}
	}

	@Override
	public void updateTargetHistoryToken(Anchor anchorLink) {
		anchorLink.setHref( CommonResourcesContainer.URI_HASH_SYMBOL + siteSectionPrefix );
	}

	@Override
	public void avatarSpoilerChanged(int userID, int spoilerID, Date spoilerExpDate) {
		Collection<ChatRoomUI> openRoomUIs = openRoomIdToChatRoomUI.values();
		for( ChatRoomUI room : openRoomUIs ) {
			room.avatarSpoilerChanged( userID, spoilerID, spoilerExpDate);
		}
	}
	
	@Override
	public void setUserLoggedIn() {
		//Starts all of the rooms updates
		startRoomUpdates();
	}
	
	@Override
	public void setUserLoggedOut() {
		//Stop all of the rooms updates
		stopRoomUpdates();
		//Close all of the room tabs
		closeAllRoomTabs();
	}

	@Override
	public void setEnabled(boolean enabled) {
		//NOTE: Nothing to be done here
	}
}
