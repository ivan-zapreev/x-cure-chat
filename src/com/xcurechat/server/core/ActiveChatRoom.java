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
 * The server core package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.server.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.data.ChatRoomDataUpdate;
import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.RoomUserAccessData;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.rpc.exceptions.RoomAccessException;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.userstatus.UserStatusType;

import com.xcurechat.server.jdbc.ConnectionWrapper;
import com.xcurechat.server.jdbc.rooms.ResetRoomVisitorsExecutor;
import com.xcurechat.server.jdbc.rooms.UpdateRoomVisitorsExecutor;
import com.xcurechat.server.jdbc.rooms.SelectRoomVisirotsExecutor;
import com.xcurechat.server.jdbc.chat.GetRoomMessagesUpdateExecutor;
import com.xcurechat.server.jdbc.chat.InsertNewChatMessageExecutor;

/**
 * @author zapreevis
 * This class is responsible for storing information about an active chat room.
 * For example, we store the visible room users, the user's access rights and alike 
 */
public class ActiveChatRoom {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( ActiveChatRoom.class );
	
	//The mapping of the room owner's ID to the list of his rooms
	protected static final Map<Integer, Set<Integer> > ownerIdToRoomId = Collections.synchronizedMap(  new HashMap<Integer, Set<Integer>>() );
	
	//The mapping of room access ids to the room user ID
	private Map<Integer, Integer> roomAccessIdToUserID = Collections.synchronizedMap( new HashMap<Integer, Integer>() );
	//The set of visible room users
	private Map<Integer, ShortUserData> visibleUsers = Collections.synchronizedMap( new HashMap<Integer, ShortUserData>() );
	//The mapping between the user IDs and their access-rights holders
	private Map<Integer, UserRoomAccessManager> userIDToUserRoomAccessManager = Collections.synchronizedMap( new HashMap<Integer, UserRoomAccessManager>() ); 
	
	/**
	 * Registers the mappig from the room-user access ID to the user ID for which the access is granted
	 * @param roomAccessID the room-user access ID
	 * @param userID the user ID for which the access is granted
	 */
	protected void addRoomUserAccessToUserIDMapping(final int roomAccessID, final int userID) {
		roomAccessIdToUserID.put( roomAccessID, userID );
	}
	
	/**
	 * Unregisters the mappig from the room-user access ID to the user ID for
	 * which the access is granted.
	 * @param roomAccessID the room-user access ID
	 */
	protected void removeRoomUserAccessToUserIDMapping( final int roomAccessID ) {
		roomAccessIdToUserID.remove( roomAccessID );
	}
	
	/**
	 * Allows to add an info message about the user leaving or entering the chat room or the user status change
	 * @param userData the user data object
	 * @param messageType the info message type
	 */
	protected void addUserRoomInfoMessage( final ShortUserData userData, final ChatMessage.Types messageType ) {
		addUserRoomInfoMessage( userData, messageType, "" );
	}
	
	/**
	 * Allows to add an info message about the user leaving or entering the chat room or the user status change
	 * @param userData the user data object
	 * @param messageType the info message type
	 * @param msg the optional message, can be used to store supplementary data
	 */
	protected void addUserRoomInfoMessage( final ShortUserData userData, final ChatMessage.Types messageType, final String msg ) {
		try {
			ChatMessage message = new ChatMessage();
			//To avoid uncertainty and introducing the system user
			//we mark this message as sent by the user himself
			message.senderID = userData.getUID();
			message.roomID = roomID;
			message.infoUserID = userData.getUID();
			message.infoUserLogin = userData.getUserLoginName();
			message.messageType = messageType;
			message.messageBody = msg;
			ConnectionWrapper<Void> insertChatMessageConnWrap = ConnectionWrapper.createConnectionWrapper( new InsertNewChatMessageExecutor( message ) );
			insertChatMessageConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
		} catch (Throwable e){
			logger.error( "Exception while adding a chat-room info message of type " + messageType +
						  " for room " + roomID + " and user " + userData.getUID(), e);
		}
	}
	
	/**
	 * Add the user to the list of visible users
	 * @param userID the userID
	 */
	protected void addRoomUserToVisibleUsers( final int userID ) {
		final ShortUserData userData = UserSessionManager.getUserDataObject(userID);
		if( userData != null ) {
			//If the user is not yet visible then make it visible and
			//increment the number of visible room users
			synchronized( visibleUsers ) {
				if( !visibleUsers.containsKey( userData.getUID() ) ) {
					visibleUsers.put( userData.getUID(), userData );
					//Increment the number of room visitors
					try {
						ConnectionWrapper<Void> updateVisitorsConnWrap = ConnectionWrapper.createConnectionWrapper( new UpdateRoomVisitorsExecutor( roomID, true ) );
						updateVisitorsConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
					} catch (Throwable e){
						logger.error( "Exception while incrementing the number of visitors in room " + roomID, e);
					}
					//Place the chat message about the user entering the room
					addUserRoomInfoMessage( userData, ChatMessage.Types.USER_ROOM_ENTER_INFO_MESSAGE_TYPE );
				}
			}
		} else {
			logger.error("Unable to add the user " + userID +
						" to the list of visible users of the room " + roomID +
						", the user is not online" );
		}
	}
	
	/**
	 * Allows to notify the room users about the user changing his status
	 * @param userData the user data of the user whose status is changed
	 * @param newUserStatus the new status value
	 */
	public void notifyUserStatusChange( final ShortUserData userData, final UserStatusType newUserStatus ) {
		//If the user is in the list of visible users then do the notification
		synchronized( visibleUsers ) {
			if( visibleUsers.containsKey( userData.getUID() ) ) {
				//Place the chat message about the user changing his status
				addUserRoomInfoMessage( userData, ChatMessage.Types.USER_STATUS_CHAGE_INFO_MESSAGE_TYPE, "" + newUserStatus.getId() );
			}
		}
	}
	
	/**
	 * Remove the user from the list of visible users.
	 * In this method we remove the user from the list of visible users even if he is not logged in.
	 * This is done because there are cases when the user is logged out already but the old request
	 * from the client somehow brings the user back into the room, and then the user hands in there
	 * for until the server is rebooted. Although this happens rarely, but I think that this is the
	 * problem. And this is how this method tries to fix it.
	 * @param userID the userID
	 */
	protected void removeRoomUserFromVisibleUsers( final int userID ) {
		//If the user is visible then make it invisible and
		//decrement the number of visible room users
		synchronized( visibleUsers ) {
			if( visibleUsers.containsKey( userID ) ) { 
				visibleUsers.remove( userID );
				//Decrement the number of visible room visitors
				try {
					ConnectionWrapper<Void> updateVisitorsConnWrap = ConnectionWrapper.createConnectionWrapper( new UpdateRoomVisitorsExecutor( roomID, false ) );
					updateVisitorsConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				} catch (Throwable e){
					logger.error( "Exception while decrementing the number of visitors in room " + roomID, e);
				}
				final ShortUserData userData = UserSessionManager.getUserDataObject(userID);
				if( userData != null ) {
					//Place the chat message about the user leaving the room
					addUserRoomInfoMessage( userData, ChatMessage.Types.USER_ROOM_LEAVE_INFO_MESSAGE_TYPE );
				} else {
					logger.error("Unable to send the user-has-left-the message for the user " + userID +
								" from the list of visible users of the room " + roomID +
								", because this user is not online" );
				}
			}
		}
	}
	
	//The owner ID and the room ID do not change so we store them here
	//for completeness and debug purposes. 
	private final int roomID;
	private final int ownerID;
	private boolean isPublic;
	
	/**
	 * Simple constructor
	 * @param roomData this data is not stored, but we use the OwnerID, the roomID and the isPublic status 
	 */
	public ActiveChatRoom(final ChatRoomData roomData){
		//Store the Key room data
		ownerID = roomData.getOwnerID();
		roomID = roomData.getRoomID();
		isPublic = roomData.isPublic();
		//Make sure that the owner-to room mapping is set
		Set<Integer> activeOwnerRoomIds = null;
		synchronized( ownerIdToRoomId ) {
			activeOwnerRoomIds = ownerIdToRoomId.get( ownerID );
			if( activeOwnerRoomIds == null ) {
				activeOwnerRoomIds = Collections.synchronizedSet( new HashSet<Integer>());
				ownerIdToRoomId.put( ownerID, activeOwnerRoomIds );
			}
		}
		
		//Put the room ID into the list of owner's rooms
		activeOwnerRoomIds.add( roomData.getRoomID() );
	}
	
	/**
	 * @return Returns the ID of the room owner
	 */
	public int getRoomOwnerID(){
		return ownerID;
	}
	
	/**
	 * The method is synchronized to preserve the status while it is being updated
	 * @return true if this room is public
	 */
	public synchronized boolean isPublic() {
		return isPublic;
	}
	
	/**
	 * Allows to update the room's public status and thus to update the list of visible room users;
	 * NOTE: The method is synchronized to preserve the status while theings are being updated.
	 * NOTE: Here we synchronize on the list of users who are inside the room to prevent the users
	 * from entering or leaving the room until the update is over.
	 * @param isPublic true if the room is set to be public, false if private or protected
	 */
	public synchronized void setPublicStatus( final boolean isPublic ) {
		//No users are allowed to enter or exit the room while we change the status
		synchronized( userIDToUserRoomAccessManager ) {
			//If the status has changed then store the new status
			//and update the list of visible room's users
			if( this.isPublic != isPublic ){
				this.isPublic = isPublic;
				Iterator<UserRoomAccessManager> iter = userIDToUserRoomAccessManager.values().iterator();
				while( iter.hasNext() ) {
					iter.next().reprocessAccessRights( );
				}
			}
		}
	}
	
	/**
	 * Registers the user as the one who entered the room. Here we do not do any checks.
	 * @param userID the user that want to enter the room
	 * @param accesses the list of user's access object for this room
	 */
	public void addUserToRoom( final int userID, final List<RoomUserAccessData> accesses ) {
		//Check if the user is already inside the room
		UserRoomAccessManager accessHolder = null;
		synchronized( userIDToUserRoomAccessManager ) {
			accessHolder = userIDToUserRoomAccessManager.get( userID );
			if( accessHolder == null ) {
				accessHolder = new UserRoomAccessManager( userID, (userID == ownerID), this );
				userIDToUserRoomAccessManager.put( userID, accessHolder);
				//Store and process user access rights
				accessHolder.addUserAccessRights( accesses );
			} else {
				logger.warn( "The user " + userID + " is alread inside of the room " + roomID +
								" but he atempts to re-enter the room, we do no thing.");
			}
		}
	}
	
	/**
	 * Allows to test if the given user is present inside the room
	 * @param userID the id of the user we want to know things about
	 * @return true if the user is present inside the room
	 */
	public boolean isUserInsideTheRoom( final int userID ) {
		synchronized( userIDToUserRoomAccessManager ) {
			return userIDToUserRoomAccessManager.keySet().contains( userID );
		}
	}
	
	/**
	 * Allows to test if the user has the read all access inside this room
	 * @param userID the id of the user we want to know things about
	 * @return true if the user has a read all access, false if he does not
	 *              or if he is not inside this room. 
	 */
	public boolean hasReadAllAccess( final int userID ) {
		UserRoomAccessManager userAccessManager = userIDToUserRoomAccessManager.get( userID );
		if( userAccessManager != null ) {
			return userAccessManager.hasReadAllAccess();
		} else {
			return false;
		}
	}
	
	/**
	 * Unregister the user from being inside the room. Here we do not do any checks.
	 * @param userID the user that want to leave the room
	 */
	public void removeUserFromRoom( final int userID ) {
		//Synchronize here to avoid an unexpected user adding/removal
		//while we remove the user. So this is just for safety.
		synchronized( userIDToUserRoomAccessManager ) {
			UserRoomAccessManager accessHolder = userIDToUserRoomAccessManager.get( userID );
			if( accessHolder != null ) {
				//Unregister all user access rights and remove the
				//mappings from: roomAccessIdToUserID and visibleUsers
				accessHolder.unregisterAllUserAccessRights();
				//Remove the user's access holder from the room
				userIDToUserRoomAccessManager.remove( userID );
			} else {
				logger.warn("Trying to remove the user " + userID + " from the room " +
							roomID + ", but the user is not present inside the room");
			}
		}
	}
	
	/**
	 * This method alows to delete the given user-room access IDs and update the
	 * room-user access rights.
	 * @param roomAccessIDS the list of roomAccess IDS
	 */
	public void deleteRoomUserAccess( final List<Integer> roomAccessIDS ) {
		Iterator<Integer> iter = roomAccessIDS.iterator();
		while( iter.hasNext() ) {
			final Integer userAccessID = iter.next();
			final Integer userID  = roomAccessIdToUserID.get( userAccessID );
			if( userID != null ) {
				UserRoomAccessManager accessManager = userIDToUserRoomAccessManager.get( userID );
				if( accessManager != null ) {
					accessManager.deleteUserAccess( userAccessID );
				} else {
					logger.error( "The user " + userID + " does not have user access in the room " + roomID );
				}
			} else {
				logger.warn( "The user access ID " + userAccessID + " was not found in the room " + roomID );
			}
		}
	}
	
	/**
	 * Allows to update/add the room user access right
	 * WARNING: If the access right with the RAID provided in the object
	 * does not exist, then the update is not performed!
	 * @param userAccess the updated user-room access right
	 */
	public void updateAddRoomUserAccess( final RoomUserAccessData userAccess ) {
		Integer userID  = roomAccessIdToUserID.get( userAccess.getRAID() );
		//If the user with the given access is inside the room
		if( userID == null ) {
			userID = userAccess.getUID();
			logger.info("Adding a new room access right for user " + userID );
		} else {
			logger.info("Updating an old room access right for user " + userID );
		}
		
		UserRoomAccessManager accessManager = userIDToUserRoomAccessManager.get( userID );
		if( accessManager != null ) {
			accessManager.updateAddUserAccess( userAccess );
		} else {
			logger.info("The user " + userID + " is not present in the room " + roomID );
		}
	}
	
	/**
	 * @return the set of short user data objects for the visible users residing in the room
	 */
	private Map<Integer, ShortUserData> getVisibleRoomUsers() {
		Map<Integer, ShortUserData> result = new HashMap<Integer, ShortUserData>();
		synchronized( visibleUsers ){
			result.putAll( visibleUsers );
		}
		return result;
	}
	
	//The time in millisec when we checked for idle users
	private long lastIdleUsersCheck = System.currentTimeMillis();
	
	/**
	 * Allows to detect and remove idle users from the chat room.
	 * @param forceClean if true then the clean-up is forced
	 */
	public void cleanUpIdleUsers(final boolean forceClean) {
		//This set will be filled with the users scheduled for removal
		Set<Integer> usersToRemove = null;
		synchronized( userIDToUserRoomAccessManager ) {
			if( forceClean || System.currentTimeMillis() > ( lastIdleUsersCheck + UserRoomAccessManager.USER_IDLE_TIME_OUT_MILLISEC ) ) {
				logger.debug("Cleaning up IDLE users in room " + roomID );
				usersToRemove = new HashSet<Integer>();
				Iterator<UserRoomAccessManager> iter = userIDToUserRoomAccessManager.values().iterator();
				while( iter.hasNext() ) {
					final UserRoomAccessManager userAccessManager = iter.next();
					final boolean isUserIdle = userAccessManager.isIdle();
					logger.debug("The user " + userAccessManager.getUserID() + " from room " + roomID + " is " + ( isUserIdle? "" : "not " ) + "idle!");
					if( isUserIdle ) {
						usersToRemove.add( userAccessManager.getUserID() );
					}
				}
				lastIdleUsersCheck = System.currentTimeMillis(); 
			}
		}
		
		//Remove the Idle users if any
		if( usersToRemove != null ) {
			Iterator<Integer> iter = usersToRemove.iterator();
			while( iter.hasNext() ) {
				final int userID = iter.next(); 
				logger.debug("Removing IDLE user " + userID + " from room " + roomID);
				removeUserFromRoom( userID );
			}
		}
			
	}
	
	/**
	 * Allows to send a chat message to the chat room
	 * @param message the complete chat room message
	 * @param throws site exception if smth goes wrong while sending the message
	 */
	public void sendChatMessage( ChatMessage message ) throws SiteException {
		UserRoomAccessManager userAccessManager = userIDToUserRoomAccessManager.get( message.senderID );
		if( userAccessManager != null ) {
			if( userAccessManager.hasWriteAcces() ) {
				ConnectionWrapper<Void> insertChatMessageConnWrap = ConnectionWrapper.createConnectionWrapper( new InsertNewChatMessageExecutor( message ) );
				insertChatMessageConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );				
			} else {
				logger.info("User " + message.senderID + " tries to send a message but he does not have right to write to room " + roomID );
				throw new RoomAccessException( RoomAccessException.THE_USER_IS_NOT_ALLOWED_TO_WRITE_INTO_THE_ROOM_ERROR, ""+roomID );
			}
		} else {
			logger.info("User " + message.senderID + " tries to send a message but he is not present in the room " + roomID );
			throw new RoomAccessException( RoomAccessException.THE_USER_IS_NOT_IN_THE_ROOM_ERROR, ""+roomID );
		}
	}
	
	/**
	 * Allows to get the actual room data, such as the list of visible users and the new messages.
	 * NOTE: data.nextUpdateOldestMsgIDs should store the mapping from the roomID to the id of
	 * the last retrieved message, this value is used here and then updated with the new one.
	 * @param userID the user unique ID
	 * @param data the object to fill the actual data into
	 */
	public void getActualRoomData( final int userID, final ChatRoomDataUpdate data ) {
		//Clean up Idle users
		cleanUpIdleUsers( false );
		
		//Retrieve the data
		UserRoomAccessManager userAccessManager = userIDToUserRoomAccessManager.get( userID );
		if( userAccessManager != null ) {
			if( userAccessManager.hasAccessToTheRoom() ) {
				data.roomIDToVisibleUsers.put( roomID, getVisibleRoomUsers() );
				
				//Retrieve the new room messages within the interval
				//from lastUpdateNewestMsgIDs.get( roomID ) on and then
				//update the value by setting lastUpdateNewestMsgIDs.put( roomID, ... )
				
				//Get the last retrieved message ID
				Integer lastMessageID = data.nextUpdateOldestMsgIDs.get( roomID );
				if( lastMessageID == null ) { lastMessageID = ChatMessage.UNKNOWN_MESSAGE_ID; }
				
				//Get the new messages from the DB
				GetRoomMessagesUpdateExecutor executor;
				if( userAccessManager.hasReadAllAccess() ) {
					//if the user does have a read all access
					executor = new GetRoomMessagesUpdateExecutor( roomID, lastMessageID );
				} else {
					//If the user does not have a read all access then he must have a regular
					//read access because userAccessManager.hasAccessToTheRoom() returned true
					executor = new GetRoomMessagesUpdateExecutor( roomID, userID, lastMessageID);
				}
				//Execute the messages retrieval
				try {
					List<ChatMessage> messages = new ArrayList<ChatMessage>();
					ConnectionWrapper<List<ChatMessage>> getNewChatMsgsConnWrap = ConnectionWrapper.createConnectionWrapper( executor );
					getNewChatMsgsConnWrap.executeQuery( messages, ConnectionWrapper.XCURE_CHAT_DB );
					//Store the retrieved messages in the result
					data.roomIDToChatMessages.put( roomID, messages);
					//Update the value of the last retrieved message ID in the result
					if( messages.size() > 0 ) {
						//Get the last message and store its id 
						ChatMessage newestMsg = messages.get( messages.size() - 1 );
						data.nextUpdateOldestMsgIDs.put( roomID, newestMsg.messageID );
					}
				} catch ( SiteException e) {
					//Place the exception into the error messages
					data.roomIDToException.put( roomID , e );
				}
			} else {
				//In principle it means that the user has no access rights to stay in the room any more.
				//This is why we force him to leave the room by calling the following method
				removeUserFromRoom( userID );
				//So now the user is not inside the room and we complain :)
				data.roomIDToException.put( roomID , new RoomAccessException( RoomAccessException.THE_USER_WAS_REMOVED_FROM_THE_ROOM_ERROR, ""+roomID ) );
			}
		} else {
			//The user was removed from the room
			data.roomIDToException.put( roomID , new RoomAccessException( RoomAccessException.THE_USER_IS_NOT_IN_THE_ROOM_ERROR, ""+roomID ) );
		}
	}
	
	/**
	 * Should be called when the room is being closed. It allows to. e.g. reset
	 * the room visitors counter to zero also might to other necessary operations.
	 * @param resetVisitors if true then we reset the active room's visitors in the DB (to zero)
	 */
	public void closeRoom( final boolean resetVisitors ) {
		if( resetVisitors ) {
			//Reset the number of room visitors to zero
			try {
				ConnectionWrapper<Void> resetVisitorsConnWrap = ConnectionWrapper.createConnectionWrapper( new ResetRoomVisitorsExecutor( roomID ) );
				resetVisitorsConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
			} catch (Throwable e){
				logger.error( "Exception while resetting room " + roomID + " visitors to zero (on room close)", e);
			}
		}
	}
	
	/**
	 * Allows to retrieve the active rooms belonging to the user with the given ID
	 * @param userID the room owner's ID
	 * @return the set of active room ID or null if the user does not have active rooms
	 */
	public static Set<Integer> getUserRooms( final int userID ) {
		return ownerIdToRoomId.get( userID );
	}
	
	//This is the mapping between the active room IDs and the number of visitors in these rooms
	//In fact we do not only consider rooms with users but all online rooms plus the rooms that
	//are expired but there are still users in them, so the rooms are still online
	private static Map<Integer, Integer> activeRoomVisitors = new HashMap<Integer,Integer>();
	//The synchronization object for getting the DB update
	private static Object activeRoomVisitorsSynchObj = new Object();
	//The next time in milliseconds to do the update from the DB
	private static long nextActiveRoomVisitorsUpdate = System.currentTimeMillis() - 10000;
	//The interval in milliseconds to the the updates from the DB for the number of active room visitors
	private static final long ACTIVE_ROOM_VISITORS_UPDATE_INTERVAL_MILLISEC = 5000; 
	
	/**
	 * Allows to retrieve the number of visitors in the currently active rooms i.e. the rooms
	 * that are permanent or that have an online owner and the room is not expired or still
	 * have visitors in it.
	 * @return the mapping from active room IDs to the number of visitors
	 */
	public static Map<Integer, Integer> getActiveRoomVisitors() {
		synchronized( activeRoomVisitorsSynchObj ) {
			if( nextActiveRoomVisitorsUpdate < System.currentTimeMillis() ) {
				//Get the actual number of room visitors from the DB
				try {
					//Retrieve the data from the DB
					Map<Integer, Integer> newActiveRoomVisitors = new HashMap<Integer,Integer>();
					ConnectionWrapper<Map<Integer, Integer>> roomVisitorsConnWrap = ConnectionWrapper.createConnectionWrapper( new SelectRoomVisirotsExecutor( ) );
					roomVisitorsConnWrap.executeQuery( newActiveRoomVisitors, ConnectionWrapper.XCURE_CHAT_DB );
					//Update the time
					nextActiveRoomVisitorsUpdate = System.currentTimeMillis() + ACTIVE_ROOM_VISITORS_UPDATE_INTERVAL_MILLISEC;
					//Set the new data
					activeRoomVisitors = newActiveRoomVisitors;
				} catch (Throwable e) {
					logger.error( "Exception while retrieveing the active room visitors", e);
				}
			}
		}
		return activeRoomVisitors;
	}
}
