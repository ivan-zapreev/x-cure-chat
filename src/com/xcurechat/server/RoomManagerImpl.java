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
 * The server-side RPC package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.server;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.data.ChatRoomDataUpdate;
import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.PrivateMessageData;
import com.xcurechat.client.data.RoomUserAccessData;
import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.rpc.RoomManager;
import com.xcurechat.client.rpc.exceptions.IncorrectRoomDataException;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.utils.SmileyHandler;

import com.xcurechat.server.core.ChatRoomsManager;
import com.xcurechat.server.core.SecureServerAccess;
import com.xcurechat.server.core.UserSessionManager;
import com.xcurechat.server.core.ActiveChatRoom;
import com.xcurechat.server.jdbc.ConnectionWrapper;
import com.xcurechat.server.jdbc.chat.images.DeleteChatFileExecutor;
import com.xcurechat.server.jdbc.chat.images.MarkMessageFileAsPublicExecutor;
import com.xcurechat.server.jdbc.messages.InsertMessageExecutor;
import com.xcurechat.server.jdbc.rooms.CountUserRoomsExecutor;
import com.xcurechat.server.jdbc.rooms.SelectUserRoomsExecutor;
import com.xcurechat.server.jdbc.rooms.VerifyUserRoomOwnershipExecutor;
import com.xcurechat.server.jdbc.rooms.access.CountRoomUsersExecutor;
import com.xcurechat.server.jdbc.rooms.access.SelectRoomUserAccessExecutor;
import com.xcurechat.server.security.statistics.MessageSendAbuseFilter;

/**
 * @author zapreevis
 * The RPC interface that allows for to manage rooms: create, delete
 * edit, add users to the room and etc.
 */
public class RoomManagerImpl extends RemoteServiceServlet implements RoomManager {
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( RoomManagerImpl.class );

	/**
	 * Retrieves the HttpSession object if any.
	 * @return the HttpSession object, either an old or a newly created one
	 */
	private HttpSession getLocalHttpSession() {
		return this.getThreadLocalRequest().getSession( true );
	}
	
	/**
	 * Creates a new room.
	 * @param roomData the room data object
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @return the updated ChatRoomData data object
	 * @throws SiteException in case smthing goes wrong
	 */
	public ChatRoomData create( final int userID, final String userSessionId, final ChatRoomData roomData) throws SiteException {
		return (new SecureServerAccess<ChatRoomData>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected ChatRoomData action() throws SiteException {
				logger.info( "Creating a new room by user " + userID );
				
				final MainUserData userDataObject = UserSessionManager.getUserDataObject(userID);
				
				//Check that the user does not have more than allowed rooms
				//Get the available chat rooms
				OnePageViewData<ChatRoomData> chatRooms = new OnePageViewData<ChatRoomData>();
				ConnectionWrapper<OnePageViewData<ChatRoomData>> countRoomsConnWrap = ConnectionWrapper.createConnectionWrapper( new CountUserRoomsExecutor(userID) );
				countRoomsConnWrap.executeQuery( chatRooms, ConnectionWrapper.XCURE_CHAT_DB );
				final int max_allowed_rooms = ChatRoomData.getMaxNumberOfRooms( userDataObject.getUserProfileType() );
				if( chatRooms.total_size >= max_allowed_rooms ) {
					throw new IncorrectRoomDataException( IncorrectRoomDataException.TOO_MANY_USER_ROOMS_ERR, max_allowed_rooms);
				}
				
				//Add the newly created chat room to the DB and cash
				//WARNING: The user can only create his own rooms
				roomData.setOwnerID( userID );
				roomData.setOwnerName( userDataObject.getUserLoginName() );
				ChatRoomsManager.getInstance().updateChatRoomData( userID, roomData, true );
				
				//Return the updated room data
				return roomData;
			}
		}).execute();
	}
	
	/**
	 * Update the room.
	 * @param roomData the room data object
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @return the updated room data object
	 * @throws SiteException in case smthing goes wrong
	 */
	public ChatRoomData update( final int userID, final String userSessionId, final ChatRoomData roomData )  throws SiteException {
		return (new SecureServerAccess<ChatRoomData>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected ChatRoomData action() throws SiteException {
				logger.info( "Updating the room "+roomData.getRoomID()+" by user " + userID );
				
				//Update the room in the DB and cash
				//WARNING: The admin can update other user's rooms
				ChatRoomsManager.getInstance().updateChatRoomData( userID, roomData, false );
				
				//Return the update chat room data
				return roomData; 
			}
		}).execute();
	}
	
	/**
	 * Allows to retrieve a single room data
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @param roomId the id of the room we want to retrieve the data of
	 * @return the updated room data object
	 * @throws SiteException in case smthing goes wrong
	 */
	public ChatRoomData getRoomData( final int userID, final String userSessionId, final int roomId ) throws SiteException {
		return (new SecureServerAccess<ChatRoomData>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected ChatRoomData action() throws SiteException {
				logger.info( "Retrieving the room data for " + roomId + " by user " + userID );
				
				//Return the chat room data object
				return ChatRoomsManager.getInstance().getChatRoomData( roomId ); 
			}
		}).execute();
	}
	
	/**
	 * Allows a user to enter the room.
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @param roomId the id of the room to enter
	 * @throws SiteException in case smthing goes wrong
	 */
	public void enterRoom( final int userID, final String userSessionId, final int roomId )  throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Entering the room " + roomId + " by user " + userID );
				
				//The user enters the room here
				ChatRoomsManager.getInstance().enterRoom(userID, roomId);
				
				//Nothing the be returned here
				return null;
			}
		}).execute();
	}
	
	/**
	 * Allows a user to leave the room.
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @param roomId the id of the room to enter
	 * @throws SiteException in case smthing goes wrong
	 */
	public void leaveRoom( final int userID, final String userSessionId, final int roomId )  throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Leaving the room " + roomId + " by user " + userID );
				
				//The user leaves the room here
				ChatRoomsManager.getInstance().leaveRoom(userID, roomId);
				
				//Nothing the be returned here
				return null;
			}
		}).execute();
	}
	
	/**
	 * Get the list of available rooms.
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @return the hash map that maps roomd by their ids
	 * @throws SiteException in case smthing goes wrong
	 */
	public Map<Integer, ChatRoomData> getAllRooms( final int userID, String userSessionId) throws SiteException {
		return (new SecureServerAccess<Map<Integer, ChatRoomData>>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Map<Integer, ChatRoomData> action() throws SiteException {
				logger.debug( "Getting list of available rooms for user " + userID );
				
				//Get the avalable chat rooms
				return ChatRoomsManager.getInstance().getAllActiveRooms();
			}
		}).execute( false, false );
	}
	
	/**
	 * This method allows count the number of rooms of the given user
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param forUserID the user we want to count rooms for
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public int count( final int userID, final String userSessionId, final int forUserID ) throws SiteException {
		return (new SecureServerAccess<Integer>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Integer action() throws SiteException {
				logger.info( "Counting the rooms of user " + forUserID + " by user " + userID );
				
				//Make sure that the user is an admin or it is browsing its own rooms 
				final MainUserData userDataObject = UserSessionManager.getUserDataObject(userID);
				if( !userDataObject.isAdmin() && userID != forUserID ) {
					throw new InternalSiteException(InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR);
				}
				
				//Get the avalable chat rooms
				OnePageViewData<ChatRoomData> chatRooms = new OnePageViewData<ChatRoomData>();
				ConnectionWrapper<OnePageViewData<ChatRoomData>> countRoomsConnWrap = ConnectionWrapper.createConnectionWrapper( new CountUserRoomsExecutor(forUserID) );
				countRoomsConnWrap.executeQuery( chatRooms, ConnectionWrapper.XCURE_CHAT_DB );
				
				return chatRooms.total_size;
			}
		}).execute();
	}

	/**
	 * This method allows to browse user's rooms
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param forUserID the user we want to browse rooms for
	 * @param offset the offset for the first statistical entry
	 * @param size the max number of entries to retrieve 
	 * @return an object containing requested statistical data
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public OnePageViewData<ChatRoomData> browse( final int userID, final String userSessionId,
												final int forUserID, final int offset,
												final int size ) throws SiteException {
		return (new SecureServerAccess<OnePageViewData<ChatRoomData>>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected OnePageViewData<ChatRoomData> action() throws SiteException {
				logger.info( "Browsing the rooms of user " + forUserID + " by user " + userID );
				
				//Make sure that the user is an admin or it is browsing its own rooms 
				final MainUserData userDataObject = UserSessionManager.getUserDataObject(userID);
				if( !userDataObject.isAdmin() && userID != forUserID ) {
					throw new InternalSiteException(InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR);
				}
				//Make sure that the number of requested rooms is not exceeding the maximum allowed
				int local_size; 
				if( size > OnePageViewData.MAX_NUMBER_OF_ENTRIES_PER_PAGE ) {
					local_size = OnePageViewData.MAX_NUMBER_OF_ENTRIES_PER_PAGE;
				} else {
					local_size = size;
				}
				
				//Get the avalable chat rooms
				OnePageViewData<ChatRoomData> chatRooms = new OnePageViewData<ChatRoomData>();
				ConnectionWrapper<OnePageViewData<ChatRoomData>> countRoomsConnWrap = ConnectionWrapper.createConnectionWrapper( new SelectUserRoomsExecutor(forUserID, offset, local_size) );
				countRoomsConnWrap.executeQuery( chatRooms, ConnectionWrapper.XCURE_CHAT_DB );
				
				return chatRooms;
			}
		}).execute();
	}
	
	/**
	 * This method allows to delete a room of a user
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param forUserID the user we want to delete room for
	 * @param roomIDS the list of room ID for the rooms we want to relete
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void delete( final int userID, final String userSessionId,
						final int forUserID, final List<Integer> roomIDS ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Deleting the rooms " + roomIDS.toString() + " of user " + forUserID + " by user " + userID );
				
				final boolean isAdmin = UserSessionManager.getUserDataObject(userID).isAdmin();
				
				if( ! isAdmin && ( userID != forUserID ) ) {
					throw new InternalSiteException(InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR);
				}
				
				//Delete the rooms from the hashed active rooms
				ChatRoomsManager.getInstance().deleteChatRooms( forUserID, roomIDS, isAdmin );
				
				//Nothing the be returned here
				return null;
			}
		}).execute();
	}

	
	/**
	 * This method allows count the number of users of some given protected/private room
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param roomID the room we want to counr users for
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public int countRoomUsers(final int userID, final String userSessionId,
								final int roomID) throws SiteException {
		return (new SecureServerAccess<Integer>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Integer action() throws SiteException {
				logger.info( "Counting the users of room " + roomID + " by user " + userID );
				
				//Make sure that the user is an admin or he is counting the people from his room 
				final MainUserData userDataObject = UserSessionManager.getUserDataObject(userID);
				if( !userDataObject.isAdmin() ) {
					//Make sure that the user is the room's owner
					ConnectionWrapper<Void> verifyOwnershipConnWrap = ConnectionWrapper.createConnectionWrapper( new VerifyUserRoomOwnershipExecutor( userID, roomID ) );
					verifyOwnershipConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				}
				
				//Count the room's users
				OnePageViewData<RoomUserAccessData> chatRoomUsers = new OnePageViewData<RoomUserAccessData>();
				ConnectionWrapper<OnePageViewData<RoomUserAccessData>> countRoomsConnWrap = ConnectionWrapper.createConnectionWrapper( new CountRoomUsersExecutor( roomID, userDataObject.isAdmin() ) );
				countRoomsConnWrap.executeQuery( chatRoomUsers, ConnectionWrapper.XCURE_CHAT_DB );
				
				return chatRoomUsers.total_size;
			}
		}).execute();
	}
	
	/**
	 * This method allows to browse  room's users
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param roomID the id of the room we want to browse users for
	 * @param offset the offset for the first statistical entry
	 * @param size the max number of entries to retrieve 
	 * @return an object containing requested statistical data
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	//We have to suppress warnings about custing to a generic type
	@SuppressWarnings("unchecked")
	public OnePageViewData<RoomUserAccessData> browseRoomUsers( final int userID, final String userSessionId,
																final int roomID, final int offset,
																final int size ) throws SiteException {
		return (new SecureServerAccess<OnePageViewData<RoomUserAccessData>>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected OnePageViewData<RoomUserAccessData> action() throws SiteException {
				logger.info( "Browsing the users of room " + roomID + " by user " + userID );
				
				//Make sure that the user is an admin or he is browsing the people from his room 
				final MainUserData userDataObject = UserSessionManager.getUserDataObject(userID);
				if( !userDataObject.isAdmin() ) {
					//Make sure that the user is the room's owner
					ConnectionWrapper<Void> verifyOwnershipConnWrap = ConnectionWrapper.createConnectionWrapper( new VerifyUserRoomOwnershipExecutor( userID, roomID ) );
					verifyOwnershipConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				}
				
				//Retrieve the room's users
				OnePageViewData<RoomUserAccessData> chatRoomUserAccess = new OnePageViewData<RoomUserAccessData>();
				ConnectionWrapper selectRoomUsersConnWrap = ConnectionWrapper.createConnectionWrapper( new SelectRoomUserAccessExecutor(chatRoomUserAccess, roomID, userDataObject.isAdmin(), offset, size) );
				selectRoomUsersConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				
				return chatRoomUserAccess;
			}
		}).execute();
	}
	
	/**
	 * This method allows to create a new room-user access object.
	 * It only works for an administrator, regular users are not allowed
	 * to fine-tune the user access object. 
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param userAccess the user-room access data object
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void createRoomAccess( final int userID, final String userSessionId,
									final RoomUserAccessData userAccess ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Adding (admin) the room-user access entry for user " + userAccess.getUID() + " and room " + userAccess.getRID() + " by admin " + userID );
				
				final MainUserData userDataObject = UserSessionManager.getUserDataObject(userID);
				if( !userDataObject.isAdmin() ) {
					//Only the admin can insert the detailed room access object.
					throw new IncorrectRoomDataException(IncorrectRoomDataException.SIMPLE_USER_CAN_NOT_INSERT_DETAILED_ROOM_USER_ACCESS_ERR);
				}
				
				//Add the new room-user access right the chat room
				ChatRoomsManager.getInstance().addRoomUserAccess( userID, userAccess );
				
				//Nothing to be returned here
				return null;
			}
		}).execute();
	}
	
	/**
	 * This method allows to create a new room-user access objects.
	 * The user can only be added by the room owner or the admin. 
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param roomUserIDs the list of user ids of the users that have to be added to the room.
	 * @param roomID the id of the room to which we want to add the user.
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void createRoomAccess( final int userID, final String userSessionId,
									final List<Integer> roomUserIDs, final int roomID ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Adding the room-user access entry for users " + roomUserIDs + " and room " + roomID + " by user " + userID );
				
				//Make sure that the user is an admin or he is adding the access rules for his room 
				final MainUserData userDataObject = UserSessionManager.getUserDataObject(userID);
				if( !userDataObject.isAdmin() ) {
					//Make sure that the user is the room's owner
					ConnectionWrapper<Void> verifyOwnershipConnWrap = ConnectionWrapper.createConnectionWrapper( new VerifyUserRoomOwnershipExecutor( userID, roomID ) );
					verifyOwnershipConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				}
				
				//Add the new room-user access rights to the chat room
				ChatRoomsManager.getInstance().addRoomUserAccess( userID, roomUserIDs, roomID );
				
				//Nothing to be returned here
				return null;
			}
		}).execute();
	}
	
	/**
	 * This method allows to update one user access data object
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param userAccess the user-room access data object
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void updateRoomAccess( final int userID, final String userSessionId,
									final RoomUserAccessData userAccess ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Update the room-user access entry for user " + userAccess.getUID() + " and room " + userAccess.getRID() + " by user " + userID );
				
				final MainUserData userDataObject = UserSessionManager.getUserDataObject(userID);
				if( !userDataObject.isAdmin() ) {
					//Only the admin can manage user access rights in details.
					throw new IncorrectRoomDataException(IncorrectRoomDataException.SIMPLE_USER_CAN_NOT_CHANGE_ROOM_USER_ACCESS_ERR);
				}
				
				//We have to update the user's access right for this room
				ChatRoomsManager.getInstance().updateRoomUserAccess( userAccess );
				
				//Nothing to be returned here
				return null;
			}
		}).execute();
	}
	
	/**
	 * This method allows to delete a room of a user
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param roomID the room we want to delete users of
	 * @param roomAccessIDS the list of user access entrie IDs which we want to delete from the room
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void deleteRoomUsers(final int userID, final String userSessionId,
								final int roomID, final List<Integer> roomAccessIDS) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Deleting the user access entrie(s) " + roomAccessIDS.toString() + " of room " + roomID + " by user " + userID );
				
				//Make sure that the user is an admin or he is deleting the access rules for his room 
				final MainUserData userDataObject = UserSessionManager.getUserDataObject(userID);
				if( !userDataObject.isAdmin() ) {
					//Make sure that the user is the room's owner
					ConnectionWrapper<Void> verifyOwnershipConnWrap = ConnectionWrapper.createConnectionWrapper( new VerifyUserRoomOwnershipExecutor( userID, roomID ) );
					verifyOwnershipConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				}
				
				//We have to remove users from the room manager so that they stop receiving msgs
				ChatRoomsManager.getInstance().deleteRoomUserAccess(roomID, roomAccessIDS);
				
				//Nothing to be returned here
				return null;
			}
		}).execute();
	}
	
	/**
	 * This method allows retrieve the list of users in the rooms and new messages
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param openedRoomIDS the list of opened room IDs
	 * @param lastUpdateNewestMsgIDs the room ID is mapped to the ID of the last chat message retrieved from this room
	 * @return the requested data
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public ChatRoomDataUpdate getOpenedRoomsData( final int userID, final String userSessionId,
									final List<Integer>  openedRoomIDS,
									final Map<Integer, Integer> lastUpdateNewestMsgIDs ) throws SiteException {
		return (new SecureServerAccess<ChatRoomDataUpdate>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected ChatRoomDataUpdate action() throws SiteException {
				logger.debug( "Retrieving the new data for the opened rooms: " + openedRoomIDS.toString() +
							  " with room ID to last retrieved msg ID mapping: " + lastUpdateNewestMsgIDs +
							  ", by user: " + userID );
				
				//Get the actual room's data
				ChatRoomDataUpdate result = ChatRoomsManager.getInstance().getOpenedRoomDataUpdate( userID, openedRoomIDS, lastUpdateNewestMsgIDs );
				//Get the active chat room visitors
				result.activeRoomVisitors = ActiveChatRoom.getActiveRoomVisitors();
				//Return the result
				return result;
			}
		}).execute(false, false);
	}
	
	/**
	 * Sends the request to the room owner for letting the user access the room
	 * @param userID the ID of the user who sends the request
	 * @param userSessionId the user's session id
	 * @param roomID the ID of the room we want to access
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void sendRoomAccessRequest(final int userID, final String userSessionId,
										final int roomID) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Sending a request from user " + userID + " for accessing the room " + roomID );
				
				//Get the room's data
				final ChatRoomData roomData = ChatRoomsManager.getInstance().getChatRoomData( roomID );

				//Send the request message
				InsertMessageExecutor sendAccRequestMsgExec = new InsertMessageExecutor( userID, roomData.getOwnerID(), roomID,
																						PrivateMessageData.ROOM_ACCESS_REQUEST_MESSAGE_TYPE,
																						"", "" );
				ConnectionWrapper<Void> sendAccRequestMsgConnWrap = ConnectionWrapper.createConnectionWrapper( sendAccRequestMsgExec );
				sendAccRequestMsgConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				
				//Nothing to be returned here
				return null;
			}
		}).execute();		
	}

	/**
	 * Allows to send the message to the chat room
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param message the chat message to be sent
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void sendChatMessage(final int userID, final String userSessionId, final ChatMessage message ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Sending a chat message by user " + userID + " to room " + message.roomID );
				
				//First check that the there is no send-message abuse here
				MessageSendAbuseFilter.getMessageFilter(httpSession, userID).validateNewChatMessage( message );
				
				//Get the user data object
				final UserData userData = UserSessionManager.getUserDataObject(userID);
				
				//Account for the payed smiles
				final int price = SmileyHandler.countTextPriceWatchCategory( message.messageBody );
				userData.decrementGoldPiecesCount( price );
				
				//Account for the sent chat message with or without attached file.
				//Also if this is the public message with an attached file then mark
				//the file as public in the DB.
				final boolean isPublic = ( message.messageType == ChatMessage.Types.SIMPLE_MESSAGE_TYPE );
				//The message body should not be empty
				final boolean isHasText = ( message.messageBody != null && !message.messageBody.trim().isEmpty() );
				//The message has an attached file
				final boolean isHasFile = ( ( message.fileDesc != null ) && ( message.fileDesc.fileID != ShortFileDescriptor.UNKNOWN_FILE_ID ) );
				
				//If the message is public and it has an attached file then we should mark it as public
				if( isPublic && isHasFile ) {
					//Mark the file as being public
					ConnectionWrapper<Void> markFileAsPublicConnWrap = ConnectionWrapper.createConnectionWrapper( new MarkMessageFileAsPublicExecutor( message.fileDesc.fileID ) );
					markFileAsPublicConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				}
				
				//Account for the sent message
				userData.sentAnotherChatMessage( isPublic,  isHasText, isHasFile );
				
				//Send the message, but first make sure that the sender ID is
				//correct. The latter is done by simple re-setting the sender ID.
				message.senderID = userID;
				//Also make sure that the message type is right
				if( ( message.messageType != ChatMessage.Types.SIMPLE_MESSAGE_TYPE ) &&
					( message.messageType != ChatMessage.Types.PRIVATE_MESSAGE_TYPE ) ) {
					logger.warn("The user " + userID + " tried to send a message with type " + message.messageType + ", resetting type to simple");
					message.messageType = ChatMessage.Types.SIMPLE_MESSAGE_TYPE;
				}
				ChatRoomsManager.getInstance().sendChatMessage( message );
				
				//Nothing to be returned here
				return null;
			}
		}).execute();
	}

	/**
	 * Allows to delete a chat message image
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param roomID the ID of the room the chat message belongs to
	 * @param imageID the ID of the image that is attached to the chat message
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void deleteChatMessageImage(final int userID, final String userSessionId, final int roomID, final int imageID ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Deleting a chat message image "+ imageID +" owned by user " + userID + " in room " + roomID );
				
				ConnectionWrapper<Void> deleteChatImageConnWrap = ConnectionWrapper.createConnectionWrapper( new DeleteChatFileExecutor( userID, roomID, imageID ) );
				deleteChatImageConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				
				//Nothing to be returned here
				return null;
			}
		}).execute();
	}
}