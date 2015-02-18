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

import com.google.gwt.user.client.rpc.RemoteService;

import com.xcurechat.client.data.ShortPrivateMessageData;
import com.xcurechat.client.data.PrivateMessageData;
import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.rpc.exceptions.SiteException;

/**
 * @author zapreevis
 * The RPC interface that allows for to manage personal messages:
 * send, receive, delete and etc.
 */
public interface MessageManager extends RemoteService {
	
	/**
	 * This method allows count the number of unread user's personal messages
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public int countNewMessages( final int userID, final String userSessionId ) throws SiteException;
	
	/**
	 * This method allows count the number of user's personal messages
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param forUserID the user we want to count messages for
	 * @param isAll if we want to count all messages
	 * @param isReceived if not isAll then if true, we count received messages, otherwise sent
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public int count( final int userID, final String userSessionId, final int forUserID,
						final boolean isAll, final boolean isReceived ) throws SiteException;

	/**
	 * This method allows to browse user's private messages
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param forUserID the user we want to browse messages for
	 * @param isAll if we want to browse all messages
	 * @param isReceived if not isAll then if true, we browse received messages, otherwise sent
	 * @param offset the offset for the first statistical entry
	 * @param size the max number of entries to retrieve 
	 * @return an object containing requested statistical data
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public OnePageViewData<ShortPrivateMessageData> browse( final int userID, final String userSessionId,
														final int forUserID, final boolean isAll,
														final boolean isReceived, final int offset,
														final int size ) throws SiteException;
	
	/**
	 * This method allows to send a simple personal message from userID  
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param message the simple type message that has to be sent
	 * @param isAReply should be true if this is a reply message
	 *                 this is only needed for the anti spam protection
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void sendSimpleMessage( final int userID, final String userSessionId,
									PrivateMessageData message, final boolean isAReply ) throws SiteException;
	
	/**
	 * This method allows to retrieve a full message data
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param messageID the id of the message we want to retrieve
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public PrivateMessageData getMessage( final int userID, final String userSessionId,
											final int messageID ) throws SiteException;

	/**
	 * This method allows to delete user messages
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param forUserID the user we want to delete messages for
	 * @param messageIDS the list of message ID for the messages we want to delete
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void delete( final int userID, final String userSessionId,
						final int forUserID, final List<Integer> messageIDS ) throws SiteException;
}
