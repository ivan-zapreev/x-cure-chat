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

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.RoomUserAccessData;
import com.xcurechat.client.data.ChatRoomDataUpdate;
import com.xcurechat.client.data.search.OnePageViewData;

/**
 * @author zapreevis
 * The RPC interface that allows for to manage rooms: create, delete
 * edit, add users to the room and etc.
 */
public interface RoomManagerAsync {

	/**
	 * Creates a new room.
	 */
	public void create( final int userID, final String userSessionId ,
							ChatRoomData roomData, AsyncCallback<ChatRoomData> callback );
	
	/**
	 * Update the room.
	 */
	public void update( final int userID, final String userSessionId,
						ChatRoomData roomData, AsyncCallback<ChatRoomData> callback );
	
	/**
	 * Allows to retrieve a single room data
	 */
	public void getRoomData( final int userID, final String userSessionId,
							 final int roomId, AsyncCallback<ChatRoomData> callback );
	
	/**
	 * Allows a user to enter the room.
	 */
	public void enterRoom( final int userID, final String userSessionId,
							final int roomId, AsyncCallback<Void> callback );
	
	/**
	 * Allows a user to leave the room.
	 */
	public void leaveRoom( final int userID, final String userSessionId,
							final int roomId, AsyncCallback<Void> callback );

	/**
	 * Get the list of available rooms.
	 */
	public void getAllRooms( final int userID, final String userSessionId,
							AsyncCallback< Map<Integer, ChatRoomData> > callback);
	
	/**
	 * This method allows count the number of rooms of the given user
	 */
	public void count( final int userID, final String userSessionId,
						final int forUserID, AsyncCallback<Integer> callback );

	/**
	 * This method allows to browse user's rooms
	 */
	public void browse( final int userID, final String userSessionId,
						final int forUserID, final int offset,
						final int size, AsyncCallback<OnePageViewData<ChatRoomData>> callback );
	
	/**
	 * This method allows to delete a room of a user
	 */
	public void delete( final int userID, final String userSessionId,
						final int forUserID, final List<Integer> roomIDS,
						AsyncCallback<Void> callback );
	
	/**
	 * This method allows count the number of users of some given protected/private room
	 */
	public void countRoomUsers(final int userID, final String userSessionId,
								final int roomID, AsyncCallback<Integer> callback);
	
	/**
	 * This method allows to browse  room's users
	 */
	public void browseRoomUsers( final int userID, final String userSessionId,
								final int roomID, final int offset, final int size,
								AsyncCallback<OnePageViewData<RoomUserAccessData>> callback);
	
	/**
	 * This method allows to create a new room-user access object.
	 * It only works for an administrator, regular users are not allowed
	 * to fine-tune the user access object. 
	 */
	public void createRoomAccess( final int userID, final String userSessionId,
									RoomUserAccessData userAccess,
									AsyncCallback<Void> callback );
	
	/**
	 * This method allows to create a new room-user access objects.
	 * The user can only be added by the room owner or the admin. 
	 */
	public void createRoomAccess( final int userID, final String userSessionId,
									List<Integer> roomUserIDs, final int roomID,
									AsyncCallback<Void> callback);
	
	/**
	 * This method allows to update one room-user access data object
	 */
	public void updateRoomAccess( final int userID, final String userSessionId,
									final RoomUserAccessData userAccess,
									AsyncCallback<Void> callback );
	
	/**
	 * This method allows to delete a room of a user
	 */
	public void deleteRoomUsers(final int userID, final String userSessionId,
								final int roomID, final List<Integer> roomAccessIDS,
								AsyncCallback<Void> callback);

	/**
	 * This method allows retrieve the list of users in the rooms and new messages
	 */
	public void getOpenedRoomsData( final int userID, final String userSessionId,
									final List<Integer> openedRoomIDS,
									final Map<Integer, Integer> nextUpdateOldestMsgID,
									AsyncCallback<ChatRoomDataUpdate> callback );
	
	/**
	 * Sends the request to the room owner for letting the user access the room
	 */
	public void sendRoomAccessRequest(final int userID, final String userSessionId,
										final int roomID, AsyncCallback<Void> callback );
	
	/**
	 * Allows to send the message to the chat room
	 */
	public void sendChatMessage(final int userID, final String userSessionId,
								final ChatMessage message, AsyncCallback<Void> callback );
	
	/**
	 * Allows to delete a chat message image
	 */
	public void deleteChatMessageImage(final int userID, final String userSessionId,
										final int roomID, final int imageID,
										AsyncCallback<Void> callback );
}
