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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.xcurechat.client.data.PrivateMessageData;
import com.xcurechat.client.data.ShortPrivateMessageData;
import com.xcurechat.client.data.search.OnePageViewData;

/**
 * @author zapreevis
 * The RPC interface that allows for to manage personal messages:
 * send, receive, delete and etc.
 */
public interface MessageManagerAsync {
	
	/**
	 * This method allows count the number of unread user's personal messages
	 */
	public void countNewMessages( final int userID, final String userSessionId,
								  AsyncCallback<Integer> callback );
	
	/**
	 * This method allows count the number of user's personal messages
	 */
	public void count( final int userID, final String userSessionId,
						final int forUserID, final boolean isAll,
						final boolean isReceived, AsyncCallback<Integer> callback );

	/**
	 * This method allows to browse user's private messages
	 */
	public void browse( final int userID, final String userSessionId,
						final int forUserID, final boolean isAll,
						final boolean isReceived, final int offset,
						final int size,
						AsyncCallback<OnePageViewData<ShortPrivateMessageData>> callback );
	/**
	 * This method allows to send a simple personal message from userID  
	 */
	public void sendSimpleMessage( final int userID, final String userSessionId,
									PrivateMessageData message, final boolean isAReply,
									AsyncCallback<Void> callback );
	
	/**
	 * This method allows to retrieve a full message data
	 */
	public void getMessage( final int userID, final String userSessionId,
								final int messageID, AsyncCallback<PrivateMessageData> callback );

	/**
	 * This method allows to delete user messages
	 */
	public void delete( final int userID, final String userSessionId, final int forUserID,
						final List<Integer> messageIDS, AsyncCallback<Void> callback );

}
