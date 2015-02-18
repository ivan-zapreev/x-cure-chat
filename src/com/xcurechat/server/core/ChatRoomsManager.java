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
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.server.core;

import java.lang.management.ThreadInfo;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Date;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.data.ChatRoomDataUpdate;
import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.MessageFileData;
import com.xcurechat.client.data.RoomUserAccessData;
import com.xcurechat.client.data.ShortPrivateMessageData;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.UserFileData;

import com.xcurechat.client.rpc.exceptions.IncorrectRoomDataException;
import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.rpc.exceptions.RoomAccessException;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.userstatus.UserStatusType;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

import com.xcurechat.server.security.ThreadWarningSystem;

import com.xcurechat.server.jdbc.ConnectionWrapper;
import com.xcurechat.server.jdbc.messages.InsertMessageExecutor;
import com.xcurechat.server.jdbc.rooms.DeleteRoomExecutor;
import com.xcurechat.server.jdbc.rooms.GetRoomExecutor;
import com.xcurechat.server.jdbc.rooms.InsertNewRoomExecutor;
import com.xcurechat.server.jdbc.rooms.SelectAllActualRoomsExecutor;
import com.xcurechat.server.jdbc.rooms.UpdateRoomExecutor;
import com.xcurechat.server.jdbc.rooms.ResetRoomVisitorsExecutor;
import com.xcurechat.server.jdbc.rooms.access.DeleteRoomUsersExecutor;
import com.xcurechat.server.jdbc.rooms.access.SelectOneUserRoomAccessExecutor;
import com.xcurechat.server.jdbc.rooms.access.UpdateRoomUserAccessExecutor;
import com.xcurechat.server.jdbc.rooms.access.InsertNewUserRoomAccessExecutor;
import com.xcurechat.server.jdbc.chat.RemoveOldChatMessagesExecutor;
import com.xcurechat.server.jdbc.chat.RemoveOldChatMessagesRecepientsExecutor;
import com.xcurechat.server.jdbc.chat.images.CanViewChatFileExecutor;
import com.xcurechat.server.jdbc.chat.images.ReAssignOldPublicFilesToBotExecutor;
import com.xcurechat.server.jdbc.chat.images.RemoveOldChatFilesExecutor;
import com.xcurechat.server.jdbc.chat.images.SecureSelectChatFileExecutor;
import com.xcurechat.server.jdbc.chat.images.SelectChatFileExecutor;

/**
 * @author zapreevis
 * This class is responsible for managing chat rooms: 
 * users are entering/leaving the chat rooms, the rooms
 * get closed or deleted, the messages are being sent to
 * the rooms and read by users and etc. 
 */
public class ChatRoomsManager {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( ChatRoomsManager.class );
	
	//Get the factory of the synchronization objects for rooms
	private static final SynchFactory roomAccessSynchFactory = SynchFactory.getSynchFactory( SynchFactory.ROOM_ACCESS_FACTORY_NAME ); 
	
	/******************************************************************************************/
	/*******METHODS REQUIRED TO OPENING, CLOSING, AND ACCESSING THE ROOMS, ASLO MANAGING*******/
	/*******THE USER-ROOM ACCES****************************************************************/
	/******************************************************************************************/
	
	//The mapping of the room id to the ActiveChatRoom
	private Map<Integer, ActiveChatRoom> roomIdToActiveChatRoom = Collections.synchronizedMap(  new HashMap<Integer, ActiveChatRoom>() );
	
	/**
	 * The private constructor since this class is a singleton
	 */
	private ChatRoomsManager(){}
	
	/**
	 * Close all the active chat rooms belonging to this user.
	 * This method also removes the online rooms from the list of online rooms.
	 * WARNING: We do not check that the operation is done by the user who is the rooms owner.
	 * @param userID the user ID of the room's owner
	 */
	public void removeOnlineAndCloseUserRooms( final int userID ) {
		Set<Integer> roomIDS = ActiveChatRoom.getUserRooms(userID);
		if( roomIDS != null ) {
			synchronized( roomIDS ) {
				Iterator<Integer> iter = roomIDS.iterator();
				while( iter.hasNext() ) {
					final int roomID = iter.next();
					onlineChatRooms.remove( roomID );
					closeUserRoom( roomID, false );
				}
			}
		}
	}
		
	/**
	 * Close the active chat room by ID
	 * WARNING: We do not check that the operation is done by the user who is the rooms owner.
	 * @param roomIDS the ids of the rooms that are to be closed
	 * @param resetVisitors if true then we reset the active room's visitors in the DB (to zero)
	 */
	private void closeUserRoom( final int roomID, final boolean resetVisitors ) {
		ActiveChatRoom activeRoom = roomIdToActiveChatRoom.remove( roomID );
		//De-initialize the active room
		if( activeRoom != null ) {
			activeRoom.closeRoom( resetVisitors );
		}
	}
	
	/**
	 * Allows to clean-up idle users inside a chat room, this is needed for
	 * removing the users that left the room by closing the browser window,
	 * with no other users left inside the room.
	 * @param roomID the id of the room for which we do the clean up
	 */
	private void cleanUpIdleUsers(final int roomID) {
		ActiveChatRoom activeRoom = roomIdToActiveChatRoom.get( roomID );
		if( activeRoom != null ) {
			//Force the room's clean-up
			activeRoom.cleanUpIdleUsers( true );
		}
	}
	
	/**
	 * This method allows to delete room access right for the given room.
	 * At this moment we have to be sure that the person who deletes the room access rights
	 * either is an admin or is the room's owner. So this is assumed in the following.
	 * Here we check that we do not remove access rights that do not belong to the room.
	 * WARNING: We do not check that the operation is done by the user who is the rooms owner.
	 * @param userID the user unique ID
	 * @param roomID the room we want to delete users from
	 * @param roomAccessIDS the list of user access entrie IDs which we want to delete from the room
	 */
	public void deleteRoomUserAccess( final int roomID, final List<Integer> roomAccessIDS ) throws SiteException {
		try{
			synchronized( roomAccessSynchFactory.getSynchObject( roomID ) ) {
				//Delete the room's users from the DB
				ConnectionWrapper<Void> deleteRoomUsersConnWrap = ConnectionWrapper.createConnectionWrapper( new DeleteRoomUsersExecutor(roomID, roomAccessIDS) );
				deleteRoomUsersConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				
				//Update the acces right in the active room, if any, and if the user is inside
				ActiveChatRoom activeRoom = roomIdToActiveChatRoom.get( roomID );
				if( activeRoom != null ) {
					activeRoom.deleteRoomUserAccess( roomAccessIDS );
				} else {
					logger.warn( "The room " + roomID + " is not active, thus the user access rights are only removed from the DB.");
				}
			}
		}finally{
			roomAccessSynchFactory.releaseSynchObject( roomID );
		}
	}
	
	/**
	 * Allows to update the room-user access right 
	 * WARNING: We do not check that the operation is done by the user who is the rooms owner.
	 * @param userAccess the user-room access data object
	 */
	public void updateRoomUserAccess( final RoomUserAccessData userAccess ) throws SiteException {
		final int roomID = userAccess.getRID();
		try{
			synchronized( roomAccessSynchFactory.getSynchObject( roomID ) ) {
				//If we save a room access (system/regular) then we check that there is no such
				//room access (system/regular) or that we are updating the old access rule.
				ConnectionWrapper<List<RoomUserAccessData>> selectRoomUserAccessConnWrap = ConnectionWrapper.createConnectionWrapper( new SelectOneUserRoomAccessExecutor(userAccess.getRID(), userAccess.getUID(), userAccess.isSystem(), false) );
				List<RoomUserAccessData> list = new ArrayList<RoomUserAccessData>();
				selectRoomUserAccessConnWrap.executeQuery( list, ConnectionWrapper.XCURE_CHAT_DB );
				if( !list.isEmpty() && ( (list.size() > 1) || ( list.get(0).getRAID() != userAccess.getRAID() ) ) ){
					//There is already user-room access so we report failure
					if( userAccess.isSystem() ) {
						throw new IncorrectRoomDataException(IncorrectRoomDataException.SYSTEM_ROOM_USER_ACCESS_EXISTS_ERR);
					} else {
						throw new IncorrectRoomDataException(IncorrectRoomDataException.REGULAR_ROOM_USER_ACCESS_EXISTS_ERR);
					}
				}
				
				//Update the room's user access right
				ConnectionWrapper<Void> updateRoomUsersConnWrap = ConnectionWrapper.createConnectionWrapper( new UpdateRoomUserAccessExecutor(userAccess) );
				updateRoomUsersConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				
				//Update the acces right in the active room, if any, and if the user is inside
				ActiveChatRoom activeRoom = roomIdToActiveChatRoom.get( roomID );
				if( activeRoom != null ) {
					activeRoom.updateAddRoomUserAccess( userAccess );
				}
			}
		}finally{
			roomAccessSynchFactory.releaseSynchObject( roomID );
		}
	}
	
	/**
	 * Allows to add non-system read/write access rights 
	 * WARNING: We do not check that the operation is done by the user who is the rooms owner.
	 * This is done in the place where the method is called!
	 * @param userID the user who performs the operation
	 * @param roomUserIDs the new room users with the regular access rights
	 * @param roomID the room id
	 * TODO: Perhaps this could be optimized by using one query to insert all
	 */
	public void addRoomUserAccess( final int userID, final List<Integer> roomUserIDs, final int roomID ) throws SiteException {		
		//Allocate the default room-user access objects
		//WARNING: No synchronization here because there is one in the calles addRoomUserAccess!!!
		for(int i=0; i < roomUserIDs.size(); i++){
			RoomUserAccessData userAccess = new RoomUserAccessData();
			userAccess.setUID( roomUserIDs.get( i ) );
			userAccess.setRID( roomID );
			userAccess.setSystem( false );	//The simple user can not add a system entry
			userAccess.setRead( true );
			userAccess.setReadAll( false );	//The simple user can not grant the read all access
			userAccess.setWrite( true );
			
			//Add the user to the room
			addRoomUserAccess( userID, userAccess );
		}
	}
	
	/**
	 * Allows to add non-system read/write access right, this method also send the message notification
	 * for the user to whoem the access was granted, in case the user does not grant the access to himself.
	 * @param userID the id of the user who adds the room acces rights  
	 * WARNING: We do not check that the operation is done by the user who is allowed to add room access
	 * to the room. This is checked prior to calling this method.
	 */
	public void addRoomUserAccess( final int userID, final RoomUserAccessData userAccess ) throws SiteException {
		final int roomID = userAccess.getRID();
		try{
			synchronized( roomAccessSynchFactory.getSynchObject( roomID ) ) {
				//Check that we do not have duplicates here
				ConnectionWrapper<List<RoomUserAccessData>> selectRoomUserAccessConnWrap = ConnectionWrapper.createConnectionWrapper( new SelectOneUserRoomAccessExecutor( roomID, userAccess.getUID(), userAccess.isSystem(), false) );
				List<RoomUserAccessData> list = new ArrayList<RoomUserAccessData>();
				selectRoomUserAccessConnWrap.executeQuery( list, ConnectionWrapper.XCURE_CHAT_DB );
				if( !list.isEmpty() ){
					if( UserSessionManager.getUserDataObject(userID).isAdmin() ) {
						//There is already user-room access so we report failure
						if( userAccess.isSystem() ) {
							throw new IncorrectRoomDataException(IncorrectRoomDataException.SYSTEM_ROOM_USER_ACCESS_EXISTS_ERR);
						} else {
							throw new IncorrectRoomDataException(IncorrectRoomDataException.REGULAR_ROOM_USER_ACCESS_EXISTS_ERR);
						}
					} else {
						throw new IncorrectRoomDataException(IncorrectRoomDataException.ROOM_USER_ACCESS_EXISTS_ERR);
					}
				}
				
				//Insert the new user-room access
				ConnectionWrapper<Void> insertNewRoomAccesConnWrap = ConnectionWrapper.createConnectionWrapper( new InsertNewUserRoomAccessExecutor(userAccess) );
				insertNewRoomAccesConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				
				//Send a notification message that the user-room access was granted
				//This is done if the user did not create an access fight for himself
				if( userID != userAccess.getUID() ) {
					InsertMessageExecutor sendSmplMsgExec = new InsertMessageExecutor( userID, userAccess.getUID(), userAccess.getRID(),
																						ShortPrivateMessageData.ROOM_ACCESS_GRANTED_MESSAGE_TYPE, "", "" );
					ConnectionWrapper<Void> sendSimpleMessageConnWrap = ConnectionWrapper.createConnectionWrapper( sendSmplMsgExec );
					sendSimpleMessageConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				}
			}
			
			//Update the acces right in the active room, if any, and if the user is inside
			ActiveChatRoom activeRoom = roomIdToActiveChatRoom.get( roomID );
			if( activeRoom != null ) {
				activeRoom.updateAddRoomUserAccess( userAccess );
			}
		}finally{
			roomAccessSynchFactory.releaseSynchObject( roomID );
		}
	}
	
	//The time interval before the room expires when new users are no longer accepted 
	public static final long ENTER_ROOM_MARGIN_MILLISEC = 60000;
	
	/**
	 * Allows to notify the rooms' users about the user changing his status
	 * @param userData the user data of the user whose status is changed
	 * @param newUserStatus the new status value
	 */
	public void notifyUserStatusChange( final ShortUserData userData, final UserStatusType newUserStatus ) {
		synchronized( roomIdToActiveChatRoom ) {
			for( ActiveChatRoom activeRoom : roomIdToActiveChatRoom.values() ) {
				activeRoom.notifyUserStatusChange( userData, newUserStatus );
			}
		}
	}
	
	/**
	 * Allows the user to enter the given room
	 * @param userID the user that wants to enter the room
	 * @param roomID the ID of the room that he want to enter
	 */
	public void enterRoom( final int userID, final int roomID ) throws SiteException {
		ChatRoomData roomData = null;
		//Just to make sure that the room does not go offline while some one enters it
		synchronized( onlineChatRooms ) {
			//0. Check if the room is active
			roomData = onlineChatRooms.get( roomID );
			if( roomData == null ) {
				logger.error("The user " + userID + " tries to enter the room " + roomID + " but this room is offline!");
				throw new RoomAccessException( RoomAccessException.THE_ROOM_IS_NOT_ONLINE_ERROR, ChatRoomData.getRoomName( getChatRoomData( roomID ) ) );
			}
			
			//1. Check if we can enter the room, because of the time, for non admin users
			MainUserData userData = UserSessionManager.getUserDataObject( userID );
			if( !userData.isAdmin() ) {
				if( !roomData.isMain() && !roomData.isPermanent() ) {
					//If the room is not main and is not permanent, then check that we can enter
					//The latter if the time before it is going to close is not small
					Date expDate = roomData.getExpirationDate();
					if( (new Date( System.currentTimeMillis() + ENTER_ROOM_MARGIN_MILLISEC )).after( expDate ) ){
						logger.error("The user " + userID + " tries to enter the room " + roomID + " but this room is about to be closed, closes at " + expDate + "!");
						throw new RoomAccessException( RoomAccessException.THE_ROOM_IS_ABOUT_TO_BE_CLOSED_ERROR, roomData.getRoomName() );
					}
				}
			}
		}
		
		//Make sure that the room's user access rights do not change while we register the user to the room
		try{
			synchronized( roomAccessSynchFactory.getSynchObject( roomID ) ) {
				//2. Check if we can enter the room because of the user-room access rights
				List<RoomUserAccessData> accesses = new ArrayList<RoomUserAccessData>();  
				ConnectionWrapper<List<RoomUserAccessData>> selectUserRoomAccessConnWrap = ConnectionWrapper.createConnectionWrapper( new SelectOneUserRoomAccessExecutor(roomID, userID, false, true) );
				selectUserRoomAccessConnWrap.executeQuery( accesses, ConnectionWrapper.XCURE_CHAT_DB );
				
				//The following check is only for the users who are not the room owners or the room is not public
				if( ( roomData.getOwnerID() != userID ) && ! roomData.isPublic() ) {
					//After retrieving all user-room access rights we should check what we have
					if( accesses.isEmpty() ) {
						logger.error("The user " + userID + " tries to enter the room " + roomID + " but he does not have the room access!");
						throw new RoomAccessException( RoomAccessException.THE_USER_DOES_NOT_HAVE_ROOM_ACCESS_ERROR, roomData.getRoomName() );
					}
					//If there are some access rights then check is they are valid
					boolean isReadOrReadAll = false;
					for(int i=0; i < accesses.size(); i++) {
						RoomUserAccessData accessRight = accesses.get( i );
						//If we can read from the room or this is a system access right and we can still read all
						if( accessRight.isRead() || ( accessRight.isSystem() && accessRight.isReadAll() && !accessRight.isReadAllExpired() ) ) {
							isReadOrReadAll = true; break;
						}
					}
					if( ! isReadOrReadAll ) {
						logger.error("The user " + userID + " tries to enter the room " + roomID + " but he does not have the room access!");
						throw new RoomAccessException( RoomAccessException.THE_USER_DOES_NOT_HAVE_ROOM_ACCESS_ERROR, roomData.getRoomName() );
					}
				}
				
				//3. So far so good, the user can enter the room, since it is not
				//closing yet and he has at least some reading rights for the room 
				registerUserForTheRoom( userID, accesses, roomData );
			}
		} finally {
			roomAccessSynchFactory.releaseSynchObject( roomID );
		}
	}
	
	/**
	 * Registers the user as the one who entered the room.
	 * Here we do not do any checks. The method is partially synchronized.
	 * @param userID the user that want to enter the room
	 * @param accesses the list of user's access object for this room
	 * @param roomData the relevant chat room Data
	 */
	private void registerUserForTheRoom( final int userID,
										final List<RoomUserAccessData> accesses,
										final ChatRoomData roomData ) {
		//If the room has no users yet, then we initialize the mappings
		ActiveChatRoom activeRoom = null;
		final int roomID = roomData.getRoomID();
		synchronized( roomIdToActiveChatRoom ) {
			activeRoom = roomIdToActiveChatRoom.get( roomID  );
			if( activeRoom == null ) {
				activeRoom = new ActiveChatRoom( roomData );
				roomIdToActiveChatRoom.put( roomID, activeRoom );
			}
		}
		activeRoom.addUserToRoom(userID, accesses);
	} 
	
	/**
	 * Allows the user to leave the given room
	 * @param userID the user that want to leave the room
	 * @param roomID the ID of the room that he want to leave
	 */
	public void leaveRoom( final int userID, final int roomID ) {
		//If the room has no users yet, then we initialize the mappings
		ActiveChatRoom activeRoom = roomIdToActiveChatRoom.get( roomID  );
		if( activeRoom != null ) {
			activeRoom.removeUserFromRoom( userID );
		}
	}
	
	/**
	 * Allows the user to leave all rooms. This should be used on user logout
	 * @param userID the user that want to leave the rooms
	 */
	public void leaveAllRooms( final int userID ) {
		//Here we synchronize because we do not want any break downs 
		//due to tooms being opened or closed while we remove the user
		synchronized( roomIdToActiveChatRoom ) {
			Iterator<ActiveChatRoom> iter = roomIdToActiveChatRoom.values().iterator();
			while( iter.hasNext() ){
				iter.next().removeUserFromRoom( userID );
			}
		}
	}
	
	/******************************************************************************************/
	/****UPDATE CHAT ROOM, GET CHAT ROOM DATA, GET THE LIST OF CHAT ROOM, DELETE CHAT ROOMS****/
	/******************************************************************************************/

	//This holds the list of all available and active rooms on the server. Notice that 
	//we allocate a synchronized HashMap, just for a change, without using the Hash table. 
	private Map<Integer, ChatRoomData> onlineChatRooms = Collections.synchronizedMap(  new HashMap<Integer, ChatRoomData>() );
	
	//The time period for refreshing the active rooms from the DB
	private static final long ACTIVE_ROOMS_TREE_UPDATE_INTERVAL_MILLISEC = 6 * CommonResourcesContainer.ACTIVE_ROOMS_TREE_UPDATE_INTERVAL_MILLISEC; 
	
	//The next time we will update the active rooms list from the DB
	//The updates are needed to exclude expired rooms from the list 
	private static long nextRoomListUpdateTimeMillis = System.currentTimeMillis() - 2 * ACTIVE_ROOMS_TREE_UPDATE_INTERVAL_MILLISEC;

	/**
	 * This method allows to return the data object for a room.
	 * @param roomId the id of the room we want to get the data object for
	 * @return the room's data object if the room exists, otherwise an empty room object
	 * @throws SiteException if smth goes wrong while the retrieval of the room's data from the DB
	 */
	public ChatRoomData getChatRoomData( final int roomId ) throws SiteException {
		//NOTE: I do not see any need to have any synchronizations here,
		//except for the existing synchronized allActiveChatRooms map
		ChatRoomData roomData = onlineChatRooms.get( roomId );
		
		if( roomData == null ) {
			//If this is not an active room, then check the database
			roomData = new ChatRoomData(); roomData.setRoomID(roomId);
			ConnectionWrapper<Void> updateRoomConnWrap = ConnectionWrapper.createConnectionWrapper( new GetRoomExecutor(roomData) );
			updateRoomConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
		}
		return roomData;
	}
	
	//This is the synchronization objects needed for updating the list of
	//active chat rooms from the DB and to protect the thing from
	//updating/deleting the rooms in/from the DB
	private Object onlineRoomsUpdateSynchObj = new Object();
	private Object roomsDBSynchObj = new Object();
	
	/**
	 * Allows to update the list of active chat rooms, the updates are done periodically
	 * once in a certain period of time.
	 * @param forceUpdate if true then the list is updated even if it is no yet time to do it
	 */
	public void updateActiveRooms( final boolean forceUpdate ) {
		try {
			synchronized( onlineRoomsUpdateSynchObj ) {
				//If it is time to update the list of active rooms from the DB then
				if( ( System.currentTimeMillis() > nextRoomListUpdateTimeMillis ) || forceUpdate ) {
					//Mark the next update time in milliseconds
					nextRoomListUpdateTimeMillis = System.currentTimeMillis() + ACTIVE_ROOMS_TREE_UPDATE_INTERVAL_MILLISEC;
					
					logger.debug("Updating the room local active rooms cash from the DB.");
					Map<Integer, ChatRoomData> newOnlineChatRooms = Collections.synchronizedMap(  new HashMap<Integer, ChatRoomData>() );
					synchronized( roomsDBSynchObj ) {
						ConnectionWrapper<Map<Integer, ChatRoomData>> selectRoomsConnWrap = ConnectionWrapper.createConnectionWrapper( new SelectAllActualRoomsExecutor() );
						selectRoomsConnWrap.executeQuery( newOnlineChatRooms, ConnectionWrapper.XCURE_CHAT_DB );
					}
					
					//Clean-up idle users and close the chat rooms that are not online any more
					synchronized( onlineChatRooms ){
						Iterator<Integer> iter = onlineChatRooms.keySet().iterator();
						while( iter.hasNext() ) {
							final int roomID = iter.next();
							//First: clean-up the room's ide users
							cleanUpIdleUsers(roomID);
							//Second: remove the room from the list of active rooms 
							if( !newOnlineChatRooms.keySet().contains( roomID ) ) {
								closeUserRoom( roomID, true );
							}
						}
					}
					
					//Assign the new active chat rooms mapping  
					onlineChatRooms = newOnlineChatRooms;
					
					//Clean up the room's synchronization objects
					roomAccessSynchFactory.cleanUp();
				}
			}		
		} catch( SiteException e) {
			//In case some error occurred during the active chat room's update we just log it
			logger.error("An unexpected SiteException while updating the list of active chat rooms", e);
		}
	}
	
	/**
	 * This method returns the list of available rooms from the server
	 * which is cashed here and is updated from the DB only once every
	 * ACTIVE_ROOMS_TREE_UPDATE_INTERVAL_MILLISEC milliseconds.
	 * It then also cleans up the room's synchronization objects.
	 * @return the map from room IDs to the roomData objects
	 * @throws SiteException if smth goes wrong while working with the DB
	 */
	public Map<Integer, ChatRoomData> getAllActiveRooms() throws SiteException {
		//First update the online chat rooms if needed
		updateActiveRooms( false );
		
		//We synchronize on the hashmap, which is also synchronized
		//This is done to avoid interruptions with deleting/updating/adding rooms
			
		//I am not sure how fast this is, but GWT can not return neither the Hashtable that is
		//synchronized by default nor the synchronized wrapper of the Map provided by Collections
		Map<Integer, ChatRoomData> result = new HashMap<Integer, ChatRoomData>();
		synchronized( onlineChatRooms ) {
			result.putAll( onlineChatRooms );
		}
		return result;
	}
	
	/**
	 * This method allows to update the chat room data both in the catch and in the DB.
	 * The local cash is updated only if the room is not expired, if it is then it is
	 * removed from the cash.
	 * @param userID the id of the user who tries to perform the operation
	 * @param roomData the complete room data object to add as a new room or to update
	 * @param insertNew if true then we insert a new room 
	 * @throws SiteException if smth goes wrong while connecting to the DB
	 * @throws InternalSiteException if some one is trying to update the room
	 * that is not his own and he is not an admin.
	 */
	public void updateChatRoomData( final int userID, final ChatRoomData roomData,
									final boolean insertNew ) throws SiteException {
		logger.debug("Updating/Adding the room " + roomData.getRoomID() + " in/into the DB and the local active rooms cash.");
		//Validate the user rights and the room data. The Admin can change any ones room
		//The regular user has who tries to update the room has to be set as the room's owner
		ChatRoomsManager.validateUserRoomData(userID, roomData);
		
		//Do not allow to insert a new room or to udate it when we can
		//be reading/writing room's data from/to the DB
		synchronized( roomsDBSynchObj ) {
			//If it is a new room then insert it into the DB and so obtain the room ID
			if( insertNew ) {
				ConnectionWrapper<Void> createRoomConnWrap = ConnectionWrapper.createConnectionWrapper( new InsertNewRoomExecutor( roomData ) );
				createRoomConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
			} else {
				//This does the update if only if the room ID corresponds to the room owner ID
				//The owner ID and the owner's name are not updated in the DB
				ConnectionWrapper<Void> updateRoomConnWrap = ConnectionWrapper.createConnectionWrapper( new UpdateRoomExecutor(roomData) );
				updateRoomConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
			}
		}
		//At this point the room ID and its expiration date should be set, retrieve the room ID
		final int roomID = roomData.getRoomID();
		
		//If the room is not expired, then we update the list of active rooms, but first 
		//we have to check that the previously announced room owner and the owner's name
		//are the same! The latter is not yet known, becase it did not matter.
		if( !roomData.isExpired() ) {
			//We want to make this operation atomic
			synchronized( onlineChatRooms ) {
				ChatRoomData oldRoomData = getChatRoomData( roomID );
				if( ( roomData.getOwnerID() != oldRoomData.getOwnerID() ) ||
						( ! oldRoomData.getOwnerName().equals( roomData.getOwnerName() ) ) ) {
					//Some one is trying to change the visible name of the room's owner or his ID 
					throw new InternalSiteException(InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR);
				}
				//If everything is fine then we update the room in the list of active rooms
				onlineChatRooms.put( roomID, roomData );
				//Update the active room status, i.e. may be the room has became private or the other way around
				ActiveChatRoom activeRoom = roomIdToActiveChatRoom.get( roomID );
				if( activeRoom != null ) {
					activeRoom.setPublicStatus( roomData.isPublic() );
				}
			}
		} else {
			//Just in case the room was online, we have to send it offline
			onlineChatRooms.remove( roomID );
			this.closeUserRoom( roomID, true );
		}
	}
	
	/**
	 * This method allows to remove a set of rooms not in the DB but in the local cash.
	 * @param userID the id of the user whoes' rooms we are trying to delete, NOT necessarily
	 * the user who tries to perform the operation, and NOT necessarily the user owning the rooms!
	 * FOR a non-admin user performing the operation his ID should be the same as userID.
	 * @param roomIDS the list of room ids that we want to delete
	 * @param SiteException if something goes wrong while connecting to DB
	 * @param isAdmin must be true if the user invocing this request is an admin 
	 * @throws InternalSiteException if a non-admin user tried to delete an active room that is not his.
	 */
	public void deleteChatRooms( final int userID, final List<Integer> roomIDS, final boolean isAdmin ) throws SiteException {
		logger.debug("Removing rooms " + roomIDS + " from the local active rooms cash.");
		
		synchronized( roomsDBSynchObj ) {
			//Delete the rooms if it is owned by the user, we check for the owner
			//ID not to delete some one elses rooms
			ConnectionWrapper<Void> deleteRoomsConnWrap = ConnectionWrapper.createConnectionWrapper( new DeleteRoomExecutor(userID, roomIDS) );
			deleteRoomsConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
		}
		
		//Take care of the active rooms list
		final boolean isNotAnAdmin = ! isAdmin;
		Iterator<Integer> iter = roomIDS.iterator();
		while( iter.hasNext() ) {
			synchronized( onlineChatRooms ) {
				final int roomID = iter.next();
				if( isNotAnAdmin ) {
					//Check that the one who deletes the rooms is their owner
					//NOTE: Here we only check active rooms because the rooms which are offline 
					//were either deleted or do not belong to the given user (userID).
					ChatRoomData roomData = onlineChatRooms.get( roomID );
					if( ( roomData != null ) && ( roomData.getOwnerID() != userID ) ) {
						//The user can not delete the room of another user
						throw new InternalSiteException( InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR );
					}
				}
				//Remove the room from the list of active rooms
				onlineChatRooms.remove( roomID );
				//Make sure that the users in the room are notified that the room is expired
				closeUserRoom( roomID, false );
			}
		}
	}
	
	/**
	 * THis supplementary method vaidates the room data
	 * @param userID the user who submitted the room operations
	 * @param roomData the room data object provided by the user
	 * @throws SiteException if there are some inconsistencies, e.g
	 * the user can not modify the room or the room data is incorrect
	 */
	private static void validateUserRoomData(final int userID, ChatRoomData roomData) throws SiteException{
		final MainUserData userDataObject = UserSessionManager.getUserDataObject(userID);
		
		//If the user that submitted the query is not an admin.
		if( !userDataObject.isAdmin() ) {
			//The user ID in the roomData should be the same as userID.
			if( (roomData.getOwnerID() != userID) ) {
				throw new IncorrectRoomDataException( IncorrectRoomDataException.USER_CAN_NOT_UPDATE_OTHERS_ROOM_ERR );
			}
			//Check that a non-admin user does not create a permanent room
			if( roomData.isMain() || roomData.isPermanent() ) {
				throw new IncorrectRoomDataException( IncorrectRoomDataException.USER_CAN_NOT_CREATE_ROOM_TYPE_ERR );
			} else {
				if( roomData.getRoomDurationTimeHours() > ChatRoomData.TWENTYFOUR_HOURS_DURATION ) {
					logger.warn("Trying to set a longer than 24 hours duration by user "+userID+" room "+roomData.getRoomID());
					roomData.setRoomDurationTimeHours( ChatRoomData.TWENTYFOUR_HOURS_DURATION );
				} else {
					if( roomData.getRoomDurationTimeHours() < ChatRoomData.CLEAN_HOURS_DURATION ) {
						logger.warn("Trying to set a wrong by user "+userID+" room "+roomData.getRoomID());
						roomData.setRoomDurationTimeHours( ChatRoomData.UNKNOWN_HOURS_DURATION );
					}
				}
			}
		} else {
			//Check that the admin does not create a room with a bad duration
			if( ! roomData.isMain() && ! roomData.isPermanent() ) {
				if( roomData.getRoomDurationTimeHours() > ChatRoomData.TWENTYFOUR_HOURS_DURATION ) {
					logger.warn("Trying to set a longer than 24 hours duration by user "+userID+" room "+roomData.getRoomID());
					roomData.setRoomDurationTimeHours( ChatRoomData.TWENTYFOUR_HOURS_DURATION );
				} else {
					if( roomData.getRoomDurationTimeHours() < ChatRoomData.CLEAN_HOURS_DURATION ) {
						logger.warn("Trying to set a wrong by user "+userID+" room "+roomData.getRoomID());
						roomData.setRoomDurationTimeHours( ChatRoomData.UNKNOWN_HOURS_DURATION );
					}
				}
			}
			//TODO: Check that if the room is marked as main, then there is either
			//no main room yet, or we are just updating the old main room.
		}
	}

	/******************************************************************************************/
	/***************************SENDING A CHAT MESSAGE TO AN OPEN CHAT ROOM********************/
	/******************************************************************************************/
	
	private Object messageCleanUpSynchObject = new Object();
	//We clean up the old chat messages and images once in 10 minutes
	private static final long OLD_MESSAGES_CLEAN_UP_INTERVAL_MILLISEC = 10 * 60 * 1000;
	//The next time we are going to clean up the chat room messages and images
	private long nextMessagesCleanUpInMillisec = System.currentTimeMillis() - OLD_MESSAGES_CLEAN_UP_INTERVAL_MILLISEC;
	
	/**
	 * This method is periodically cleaning up the old chat room messages and images
	 */
	public void cleanUpOldChatMessages() {
		try {
			synchronized( messageCleanUpSynchObject ) {
				if( System.currentTimeMillis() >= nextMessagesCleanUpInMillisec ) {
					nextMessagesCleanUpInMillisec = System.currentTimeMillis() + OLD_MESSAGES_CLEAN_UP_INTERVAL_MILLISEC;
					logger.info("Cleaning up the old char room messages and images");
					//Well, it is not related to active chat rooms
					//but let us clean the chat room messages here
					RemoveOldChatMessagesExecutor msgsCleanUpExecutor = new RemoveOldChatMessagesExecutor( RemoveOldChatMessagesExecutor.REMOVE_CHAT_MESSAGES_OLDER_THAN_MINUTES );
					ConnectionWrapper<Void> removeOldChatMessagesConnWrap = ConnectionWrapper.createConnectionWrapper( msgsCleanUpExecutor );
					removeOldChatMessagesConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
					
					//Remove old chat message recipients
					RemoveOldChatMessagesRecepientsExecutor recepCleanUpExecutor = new RemoveOldChatMessagesRecepientsExecutor( RemoveOldChatMessagesRecepientsExecutor.REMOVE_CHAT_MESSAGES_RECIPIENTS_OLDER_THAN_MINUTES );
					ConnectionWrapper<Void> removeOldChatMsgRecepientsConnWrap = ConnectionWrapper.createConnectionWrapper( recepCleanUpExecutor );
					removeOldChatMsgRecepientsConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
					
					//Get the chat bot manager instance to synchronize on it
					ChatBotManager chatBotManagerInst = ChatBotManager.getInstance();
					synchronized( chatBotManagerInst ) {
						//Well, it is not related to active chat rooms but let us clean the chat room message files here
						RemoveOldChatFilesExecutor executor = new RemoveOldChatFilesExecutor( RemoveOldChatFilesExecutor.REMOVE_CHAT_MESSAGE_FILES_OLDER_THAN_MINUTES, ! chatBotManagerInst.isCurrentChatBotSet() );
						ConnectionWrapper<Void> removeOldChatMessageImagesConnWrap = ConnectionWrapper.createConnectionWrapper( executor );
						removeOldChatMessageImagesConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
						
						//Re-assign the other public chat message files for the bot if it is set
						if( chatBotManagerInst.isCurrentChatBotSet() ) {
							ReAssignOldPublicFilesToBotExecutor reAssignExecutor = new ReAssignOldPublicFilesToBotExecutor( RemoveOldChatFilesExecutor.REMOVE_CHAT_MESSAGE_FILES_OLDER_THAN_MINUTES, chatBotManagerInst.getCurrentChatBotID() );
							ConnectionWrapper<Void> reAssignConnWrap = ConnectionWrapper.createConnectionWrapper( reAssignExecutor );
							reAssignConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
						}
					}
				}
			}
		} catch ( SiteException e) {
			logger.error("An unexpected exception when cleaning up the old chat room messages and images", e);
		}
	}
	
	/**
	 * Allows to send a chat message to the chat room
	 * @param message the complete chat room message
	 */
	public void sendChatMessage( ChatMessage message ) throws SiteException {
		//First clean up the chat room messages
		cleanUpOldChatMessages();
		
		logger.info( "The user " + message.senderID + " attempts to send a chat message to room " + message.roomID );
		ActiveChatRoom activeRoom = roomIdToActiveChatRoom.get( message.roomID );
		if( activeRoom != null ) {
			try {
				activeRoom.sendChatMessage( message );
			} catch( RoomAccessException exception ) {
				//If the user is not in the room
				if( exception.getErrorCodes().contains( RoomAccessException.THE_USER_IS_NOT_IN_THE_ROOM_ERROR ) ) {
					logger.warn( "The user " + message.senderID + " is not in the room " + message.roomID + " trying to re-enter" );
					//Try to let the user inside the room
					try{
						enterRoom( message.senderID, message.roomID );
						activeRoom.sendChatMessage( message );
					} catch ( Throwable ex ) {
						//Failed to re-enter throwing the old exception
						logger.warn( "The user " + message.senderID + " was not in the room " + message.roomID + " and he could not re-enter" );
						throw exception;
					}
				} else {
					//This is not the problem with the user not being in the room, throw the exception again
					logger.error( "The chat message of user " + message.senderID + " to the room " + message.roomID +
								  " could not be sent since the user is not in the room and he can not re-enter" );
					throw exception;
				}
			}
		} else {
			logger.warn( "The user " + message.senderID + " tried to send the message to room " + message.roomID + " but it is not online" );
			throw new RoomAccessException( RoomAccessException.THE_ROOM_WAS_CLOSED_ERROR, ""+message.roomID );
		}
	}
	
	/******************************************************************************************/
	/******************************THE OPENED ROOM ACTUAL DATA RETRIEVAL***********************/
	/******************************************************************************************/
	
	/**
	 * This method allows to check if the user is allowed to see the chat-room message file
	 * Note, if any problem occurs then we simply return false
	 * @param userID the id of the user retrieving the file
	 * @param roomID the id of the room to which the file was sent
	 * @param isMsgAtt true if the file is already attached to the chat message
	 * @param fileData the object that already stores the requested file data
	 * @return true if the user can view the file
	 */
	public boolean canGetChatRoomMessageFile( final int userID, final int roomID,
											  final boolean isMsgAtt, UserFileData fileData ) {
		try {
			//If the file is already attached to the chat message then...
			if( isMsgAtt ) {
				//There is no need to test is the MainUserData object is null, because the user is online
				if( UserSessionManager.getUserDataObject(userID).isAdmin() ) {
					//The admin can view any files he likes
					return true;
				} else {
					//if the user is not an admin then let us check if the user
					//has a read all access, this also checks that the room is online
					ActiveChatRoom activeRoom = roomIdToActiveChatRoom.get( roomID );
					if( activeRoom != null ) {
						//If the room is online
						if( activeRoom.isUserInsideTheRoom( userID ) ) {
							//If user is inside the chat room
							if( activeRoom.hasReadAllAccess( userID ) ) {
								return true;
							} else {
								//If the user does not have the read all access then we need to check if
								//He is allowed to view this file, this is done by the following
								CanViewChatFileExecutor canViewChatFileExec = new CanViewChatFileExecutor(userID, roomID, fileData.fileID);
								ConnectionWrapper<Void> canViewChatFileConnWrap = ConnectionWrapper.createConnectionWrapper( canViewChatFileExec );
								canViewChatFileConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
								//If there was no exception thrown in the above, then the user can view the file
								return true;
							}
						} else {
							//The user is not inside the room
							return false;
						}
					} else {
						//The room is not online
						return false;
					}
				}
			} else {
				//If the file is not yet attached to the chat message, then we the user must be the file owner
				return ( userID == fileData.ownerID );
			}
		} catch ( SiteException e ) {
			//If we get a site exception at some point then we just log in the information
			logger.warn("A SiteException while checking if the user " + userID + " can view a chat message image "+fileData.fileID+" from room "+roomID , e);
			//And of course the user is not allowed to view the file then
			return false;
		}
	}
	
	/**
	 * This method allows to retrieve chat-room message file
	 * Note, if any problem occurs then we simply return no file
	 * @param userID the id of the user retrieving the file
	 * @param roomID the id of the room to which the file was sent
	 * @param fileID the id of the file that was sent
	 * @param isThumbnail true if we need a thumbnail, false for the entire file
	 * @param isMsgAtt true if the file is already attached to the chat message
	 * @return the object storing the required file data
	 */
	public MessageFileData getChatRoomMessageFile( final int userID, final int roomID,
													final int fileID, final boolean isThumbnail,
													final boolean isMsgAtt ) {
		MessageFileData fileData = new MessageFileData();
		try {
			if( isMsgAtt ) {
				//If the file is already attached to the chat message then...
				//There is no need to test is the MainUserData object is null, because the user is online
				if( UserSessionManager.getUserDataObject(userID).isAdmin() ) {
					//If the user is admin then he can get anything he wants
					ConnectionWrapper<MessageFileData> selectImageConnWrap = ConnectionWrapper.createConnectionWrapper( new SelectChatFileExecutor( roomID, fileID, isThumbnail ) );
					selectImageConnWrap.executeQuery( fileData, ConnectionWrapper.XCURE_CHAT_DB );
				} else {
					//if the user is not an admin then let us check if the user
					//has a read all access, this also checks that the room is online
					ActiveChatRoom activeRoom = roomIdToActiveChatRoom.get( roomID );
					if( activeRoom != null ) {
						//If the room is online
						if( activeRoom.isUserInsideTheRoom( userID ) ) {
							//If user is inside the chat room
							if( activeRoom.hasReadAllAccess( userID ) ) {
								//If the user has read all access then give him all he needs
								ConnectionWrapper<MessageFileData> selectImageConnWrap = ConnectionWrapper.createConnectionWrapper( new SelectChatFileExecutor( roomID, fileID, isThumbnail ) );
								selectImageConnWrap.executeQuery( fileData, ConnectionWrapper.XCURE_CHAT_DB );
							} else {
								//If the user does not have the read all access then we need to check if
								//He is allowed to view this image, this is done by the following
								SecureSelectChatFileExecutor secureImageSelectorExec = new SecureSelectChatFileExecutor(userID, roomID, fileID, isThumbnail);
								ConnectionWrapper<MessageFileData> secureSelectImageConnWrap = ConnectionWrapper.createConnectionWrapper( secureImageSelectorExec );
								secureSelectImageConnWrap.executeQuery( fileData, ConnectionWrapper.XCURE_CHAT_DB );
							}
						}
					}
				}
			} else {
				//If the image is not yet attached to the chat message, then we
				//Simply try to retrieve it based on the ownerID stored in the chat image record
				ConnectionWrapper<MessageFileData> selectImageConnWrap = ConnectionWrapper.createConnectionWrapper( new SelectChatFileExecutor( roomID, fileID, userID, isThumbnail ) );
				selectImageConnWrap.executeQuery( fileData, ConnectionWrapper.XCURE_CHAT_DB );
			}
		} catch ( SiteException e ) {
			//If we get a site exception at some point then we just log in the information
			logger.warn("A SiteException while retrieving a chat message image "+fileID+" from room "+roomID+" for user "+userID, e);
		}
		return fileData;
	}
	
	/**
	 * This method allows retrieve the list of users in the rooms and new messages
	 * @param userID the user unique ID
	 * @param openedRoomIDS the list of opened room IDs
	 * @param lastUpdateNewestMsgIDs the room ID is mapped to the ID of the last chat message retrieved from this room
	 * @return the requested data
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public ChatRoomDataUpdate getOpenedRoomDataUpdate(final int userID, final List<Integer> openedRoomIDS,
														final Map<Integer, Integer> lastUpdateNewestMsgIDs) throws SiteException {
		ChatRoomDataUpdate data = new ChatRoomDataUpdate();
		data.roomIDToException = new HashMap<Integer, SiteException>();
		data.roomIDToVisibleUsers = new HashMap<Integer, Map<Integer, ShortUserData>>();
		//Copy the last retrieved message IDs the values will be used and then updated
		//THis will happen inside of the activeRoom.getActualRoomData( ... ) method
		data.nextUpdateOldestMsgIDs = lastUpdateNewestMsgIDs;
		data.roomIDToChatMessages = new HashMap<Integer, List<ChatMessage>>();
		
		Iterator<Integer> iter = openedRoomIDS.iterator();
		while( iter.hasNext() ) {
			final int roomID = iter.next();
			ActiveChatRoom activeRoom = roomIdToActiveChatRoom.get( roomID );
			if( activeRoom != null ) {
				//Try to get the room data update
				activeRoom.getActualRoomData( userID, data );
				SiteException exception = data.roomIDToException.get(roomID);
				//If the attempt failed and this is because the user is not in the room, then
				//try to re-enter the room for this user as he could have just been off-line.
				if( exception != null && ( exception instanceof RoomAccessException ) ) {
					if( exception.getErrorCodes().contains( RoomAccessException.THE_USER_IS_NOT_IN_THE_ROOM_ERROR ) ) {
						logger.info("The user " + userID + " was removed from the room " + roomID + ", trying to re-enter");
						//Try to enter the room again and then get the room-data update
						try{
							enterRoom( userID, roomID );
							logger.info("The user " + userID + " has successfully re-entered the room " + roomID);
							data.roomIDToException.remove(roomID);
							logger.info("The user " + userID + " has gotten the room-access exception removed from the room-data upate of the room " + roomID);
							activeRoom.getActualRoomData( userID, data );
							logger.info("The user " + userID + " has successfully obtained the room-data upate of the room " + roomID);
						} catch (SiteException ex){
							//Since we could not re-enter the room, we just log this event
							//and the old error message about the user not-being in the room
							//will be sent to the user
							logger.warn( "The user " + userID + " was not in the room " + roomID + " and he could not re-enter" );
						}
					}
				}
			} else {
				logger.warn("User " + userID + " tries to retrieve data update for room " + roomID + " but the room is not online");
				data.roomIDToException.put( roomID, new RoomAccessException( RoomAccessException.THE_ROOM_WAS_CLOSED_ERROR, ""+roomID ) );
			}
		}
		
		return data;
	}

	
	/******************************************************************************************/
	/*******************************THE STATIC OPERATIONS**************************************/
	/******************************************************************************************/
	
	//The only instance of the chat room's manager 
	private static ChatRoomsManager instance = new ChatRoomsManager();
	
	private static ThreadWarningSystem deadlockChecker = new ThreadWarningSystem(100);
	
	static {
		//Initialize the logger for the thread safety listener
		deadlockChecker.addListener( new ThreadWarningSystem.Listener() {
			//Get the Log4j logger object
			private final Logger localLogger = Logger.getLogger( ThreadWarningSystem.class );
			
			public void deadlockDetected(ThreadInfo deadlockedThread) {
				String error = "";
				error += "Deadlocked Thread:\n";
				error += "------------------\n";
				error += deadlockedThread;
		        for (StackTraceElement ste : deadlockedThread.getStackTrace()) {
		        	error += "\n\t" + ste;
		        }
		        localLogger.error( error );
			}
			
			public void thresholdExceeded(ThreadInfo[] allThreads) {
				localLogger.error( "Threshold Exceeded! threads.length = " + allThreads.length );
			}
		} );
		//Reset the number of room visitors to zero
		try {
			ConnectionWrapper<Void> resetVisitorsConnWrap = ConnectionWrapper.createConnectionWrapper( new ResetRoomVisitorsExecutor( ChatRoomData.UNKNOWN_ROOM_ID ) );
			resetVisitorsConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
		} catch (Throwable e){
			logger.error( "Exception while resetting room visitors to zero (on start up)", e);
		}
	}

	/**
	 * THis method is synchronized, so it is safe.
	 * @return returns the instance of the chat room's manager object
	 */
	public static final ChatRoomsManager getInstance() {
		return instance;
	}
}
