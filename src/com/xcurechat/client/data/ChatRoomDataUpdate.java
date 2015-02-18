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
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client.data;

import java.util.Map;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import com.xcurechat.client.rpc.exceptions.SiteException;

/**
 * @author zapreevis
 * This class should store the update information for the list of opened rooms.
 * This information comes from the server to the client. 
 */
public class ChatRoomDataUpdate  implements IsSerializable {
	
	public ChatRoomDataUpdate(){}
	
	//Contains the mapping between the room ID and the exception that occured during the data retrieval
	public Map<Integer, SiteException> roomIDToException = null;
	
	//This map contains the mapping from the room ID to the set of the visible room visitors
	public Map<Integer, Map<Integer, ShortUserData>> roomIDToVisibleUsers = null;
	
	//This map contains the mapping between the roomID and the list of new room messages
	public Map<Integer, List<ChatMessage>> roomIDToChatMessages = null;
	
	//The room ID is mapped to the ID of the last chat message retrieved from this room
	public Map<Integer, Integer> nextUpdateOldestMsgIDs = null;
	
	//The mapping from the active room ID to the number of the chat room's visitors
	public Map<Integer, Integer> activeRoomVisitors = null;
}
