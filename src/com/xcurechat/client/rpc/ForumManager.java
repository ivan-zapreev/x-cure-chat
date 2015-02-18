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

import com.xcurechat.client.data.ShortForumMessageData;
import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.data.search.ForumSearchData;
import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.rpc.exceptions.SiteException;

/**
 * @author zapreevis
 * This interface provides methods for working with the forum.
 */
public interface ForumManager extends RemoteService {
	
	/**
	 * This method allows to delete files attached to the forum message
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param fileIDS the list of file ID for the files we want to delete
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void deleteFiles( final int userID, final String userSessionId,
							 final List<Integer> fileIDS ) throws SiteException;
	
	/**
	 * This method allows to send a new forum message to the server.
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param message the forum message to send
	 * @param captchaResponse the response of the captcha problem, needed for the new messages
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void sendMessage( final int userID, final String userSessionId,
							 final ShortForumMessageData message, final String captchaResponse )  throws SiteException;
	
	/**
	 * This method allows to update the forum message on the server.
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param message the updated forum message
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void updateMessage( final int userID, final String userSessionId,
							   final ShortForumMessageData message ) throws SiteException;
	
	/**
	 * Allows to delete a forum message
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param messageId the id of the message to be deleted
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void deleteMessage( final int userID, final String userSessionId,
			   				   final int messageID ) throws SiteException;
	
	/**
	 * This method allows to perform forum search, not that the forum search is available
	 * even if the user is not logged int, thus the userId 	and userSessionId can be undefined
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param searchParams the object with the search parameters
	 * @return the search results
	 * @throws SiteException in case something goes wrong on the server
	 */
	public OnePageViewData<ForumMessageData> searchMessages( final int userID, final String userSessionId,
															 final ForumSearchData searchParams ) throws SiteException;
	
	/**
	 * This method allows to retrieve the forum message with the given id.
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param messageID the id of the message to retrieve
	 * @throws SiteException in case something goes wrong on the server, e.g. the message does not exist
	 */
	public ForumMessageData getForumMessage( final int userID, final String userSessionId,
								 			 final int messageID ) throws SiteException;
	
	/**
	 * This method allows to move the forum message to be rooted to another forum message.
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param messageID the id of the message to be moved
	 * @param newParentMessageID the id of the new parent message
	 * @throws SiteException in case something goes wrong on the server, e.g. the message does not exist
	 */
	public void moveForumMessage( final int userID, final String userSessionId,
					 			  final int messageID, final int newParentMessageID ) throws SiteException;
	
	/**
	 * This method allows to approve/disapprove the forum message to be shown in the main site section.
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param messageID the id of the message to be approved/disapproved
	 * @param approve true to approve the message false to disapprove
	 * @throws SiteException in case something goes wrong on the server, e.g. the message does not exist
	 */
	public void approveForumMessage( final int userID, final String userSessionId,
					 			  final int messageID, final boolean approve ) throws SiteException;
	
	/**
	 * This method allows to vote for the forum message
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param messageID the id of the message to be voted for
	 * @param approve true to say that the message is good, otherwise false (bad)
	 * @throws SiteException in case something goes wrong on the server, e.g. the message does not exist
	 */
	public void voteForForumMessage( final int userID, final String userSessionId,
					 			     final int messageID, final boolean voteFor ) throws SiteException;
}
