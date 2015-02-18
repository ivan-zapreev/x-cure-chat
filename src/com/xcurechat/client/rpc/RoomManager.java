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
 * The server side (RPC, servlet) access package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;

import java.util.List;
import java.util.Map;

import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.RoomUserAccessData;
import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.data.ChatRoomDataUpdate;
import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.rpc.exceptions.SiteException;

/**
 * @author zapreevis
 * The RPC interface that allows for to manage rooms: create, delete
 * edit, add users to the room and etc.
 */
public interface RoomManager extends RemoteService {
	
	/**
	 * Creates a new room.
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @param roomData the room data object
	 * @return the updated ChatRoomData object
	 * @throws SiteException in case something goes wrong
	 */
	public ChatRoomData create( final int userID, final String userSessionId, ChatRoomData roomData ) throws SiteException;
	
	/**
	 * Update the room.
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @param roomData the room data object
	 * @return the updated room data object
	 * @throws SiteException in case smthing goes wrong
	 */
	public ChatRoomData update( final int userID, final String userSessionId, ChatRoomData roomData ) throws SiteException;
	
	/**
	 * Allows to retrieve a single room data
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @param roomId the id of the room we want to retrieve the data of
	 * @return the updated room data object
	 * @throws SiteException in case smthing goes wrong
	 */
	public ChatRoomData getRoomData( final int userID, final String userSessionId, final int roomId ) throws SiteException;
	
	/**
	 * Allows a user to enter the room.
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @param roomId the id of the room to enter
	 * @throws SiteException in case smthing goes wrong
	 */
	public void enterRoom( final int userID, final String userSessionId, final int roomId ) throws SiteException;
	
	/**
	 * Allows a user to leave the room.
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @param roomId the id of the room to leave
	 * @throws SiteException in case smthing goes wrong
	 */
	public void leaveRoom( final int userID, final String userSessionId, final int roomId )  throws SiteException;
	
	/**
	 * Get the list of available rooms.
	 * @param userID the user unique ID
	 * @param userSessionId the id of the user session
	 * @return the hash map that maps rooms to their ids
	 * @throws SiteException in case something goes wrong
	 */
	public Map<Integer, ChatRoomData> getAllRooms( final int userID, final String userSessionId ) throws SiteException;
	
	/**
	 * This method allows count the number of rooms of the given user
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param forUserID the user we want to count rooms for
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public int count( final int userID, final String userSessionId, final int forUserID ) throws SiteException;

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
												final int size ) throws SiteException;
	
	/**
	 * This method allows to delete a room of a user
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param forUserID the user we want to delete room for
	 * @param roomIDS the list of room ID for the rooms we want to delete
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void delete( final int userID, final String userSessionId,
						final int forUserID, final List<Integer> roomIDS ) throws SiteException;
	
	/**
	 * This method allows count the number of users of some given protected/private room
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param roomID the room we want to count users for
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public int countRoomUsers(final int userID, final String userSessionId,
								final int roomID) throws SiteException;
	

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
	public OnePageViewData<RoomUserAccessData> browseRoomUsers( final int userID, final String userSessionId,
																final int roomID, final int offset,
																final int size ) throws SiteException;
	
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
									final RoomUserAccessData userAccess ) throws SiteException;
	
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
									final List<Integer> roomUserIDs, final int roomID ) throws SiteException;
	
	/**
	 * This method allows to update one room-user access data object
	 * It only works for an administrator, regular users are not allowed
	 * to fine-tune the user access object. 
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param userAccess the user-room access data object
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void updateRoomAccess( final int userID, final String userSessionId,
									final RoomUserAccessData userAccess) throws SiteException;
	
	/**
	 * This method allows to delete a room of a user
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param roomID the room we want to delete users of
	 * @param roomAccessIDS the list of user access entry IDs which we want to delete from the room
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void deleteRoomUsers(final int userID, final String userSessionId,
								final int roomID, final List<Integer> roomAccessIDS) throws SiteException;

	/**
	 * This method allows retrieve the list of users in the rooms and new messages
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param openedRoomIDS the list of opened room IDs
	 * @param nextUpdateOldestMsgIDs the room ID is mapped to the ID of the last chat message retrieved from this room
	 * @return the requested data
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public ChatRoomDataUpdate getOpenedRoomsData( final int userID, final String userSessionId,
													final List<Integer> openedRoomIDS,
													final Map<Integer, Integer> nextUpdateOldestMsgIDs )
													throws SiteException;
	
	/**
	 * Sends the request to the room owner for letting the user access the room
	 * @param userID the ID of the user who sends the request
	 * @param userSessionId the user's session id
	 * @param roomID the ID of the room we want to access
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void sendRoomAccessRequest(final int userID, final String userSessionId,
										final int roomID) throws SiteException;
	
	/**
	 * Allows to send the message to the chat room
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param message the chat message to be sent
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void sendChatMessage(final int userID, final String userSessionId, final ChatMessage message ) throws SiteException;
	
	/**
	 * Allows to delete a chat message image
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param roomID the ID of the room the chat message belongs to
	 * @param imageID the ID of the image that is attached to the chat message
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void deleteChatMessageImage(final int userID, final String userSessionId, final int roomID, final int imageID ) throws SiteException;
	
}
