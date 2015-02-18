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

import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.data.ShortForumMessageData;
import com.xcurechat.client.data.search.ForumSearchData;
import com.xcurechat.client.data.search.OnePageViewData;

/**
 * @author zapreevis
 * This interface provides methods for working with the forum.
 */
public interface ForumManagerAsync {
	
	/**
	 * This method allows to delete files attached to the forum message
	 */
	public void deleteFiles( final int userID, final String userSessionId,
							 final List<Integer> fileIDS, AsyncCallback<Void> callback );
	
	/**
	 * This method allows to send a new forum message to the server.
	 */
	public void sendMessage( final int userID, final String userSessionId,
							 final ShortForumMessageData message, final String captchaResponse,
							 AsyncCallback<Void> callback );
	
	/**
	 * This method allows to update the forum message on the server.
	 */
	public void updateMessage( final int userID, final String userSessionId,
							   final ShortForumMessageData message, AsyncCallback<Void> callback  );
	
	/**
	 * Allows to delete a forum message
	 */
	public void deleteMessage( final int userID, final String userSessionId,
							   final int messageID, AsyncCallback<Void> callback );
	
	/**
	 * This method allows to perform forum search, not that the forum search is available
	 * even if the user is not logged in, thus the userId 	and userSessionId can be undefined
	 */
	public void searchMessages( final int userID, final String userSessionId,
								final ForumSearchData searchParams,
								AsyncCallback<OnePageViewData<ForumMessageData>> callback );
	
	/**
	 * This method allows to retrieve the forum message with the given id.
	 */
	public void getForumMessage( final int userID, final String userSessionId,
								 final int messageID, AsyncCallback<ForumMessageData> callback );
	/**
	 * This method allows to move the forum message to be rooted to another forum message.
	 */
	public void moveForumMessage( final int userID, final String userSessionId,
					 			  final int messageID, final int newParentMessageID,
					 			  AsyncCallback<Void> callback );
	/**
	 * This method allows to approve/disapprove the forum message to be shown in the main site section.
	 */
	public void approveForumMessage( final int userID, final String userSessionId,
					 			  final int messageID, final boolean approve,
					 			  AsyncCallback<Void> callback );
	
	/**
	 * This method allows to vote for the forum message
	 */
	public void voteForForumMessage( final int userID, final String userSessionId,
					 			     final int messageID, final boolean voteFor,
						 			  AsyncCallback<Void> callback ); 
}
