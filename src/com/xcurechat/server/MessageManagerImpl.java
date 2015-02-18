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

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.data.PrivateMessageData;
import com.xcurechat.client.data.ShortPrivateMessageData;
import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.rpc.MessageManager;

import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.utils.SmileyHandler;

import com.xcurechat.server.core.SecureServerAccess;
import com.xcurechat.server.core.UserSessionManager;

import com.xcurechat.server.jdbc.ConnectionWrapper;
import com.xcurechat.server.jdbc.messages.CountNewUserMessagesExecutor;
import com.xcurechat.server.jdbc.messages.HideMessagesExecutor;
import com.xcurechat.server.jdbc.messages.CountUserMessagesExecutor;
import com.xcurechat.server.jdbc.messages.SelectUserMessagesExecutor;
import com.xcurechat.server.jdbc.messages.InsertMessageExecutor;
import com.xcurechat.server.jdbc.messages.DeleteDeletedMessagesExecutor;
import com.xcurechat.server.jdbc.messages.GetMessageExecutor;
import com.xcurechat.server.jdbc.messages.MarkMessageAsReadExecutor;
import com.xcurechat.server.security.statistics.MessageSendAbuseFilter;

/**
 * @author zapreevis
 * The RPC servlet that allows for to manage personal messages:
 * send, receive, delete and etc.
 */
public class MessageManagerImpl extends RemoteServiceServlet implements MessageManager {
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( MessageManagerImpl.class );

	static {
		//Delete the private (personal) messages that are not accessible by anyone
		try{
			DeleteDeletedMessagesExecutor deleteDeletedMsgsExec = new DeleteDeletedMessagesExecutor();
			ConnectionWrapper<Void> deleteDeletedMsgsConnWrap = ConnectionWrapper.createConnectionWrapper( deleteDeletedMsgsExec );
			deleteDeletedMsgsConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
		} catch (Exception e) {
			logger.error("An unexpected exception when trying to delete unaccessible personal messages from the DB", e);
		}
	}
	
	/**
	 * Retrieves the HttpSession object if any.
	 * @return the HttpSession object, either an old or a newly created one
	 */
	private HttpSession getLocalHttpSession() {
		return this.getThreadLocalRequest().getSession( true );
	}

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
	//We have to suppress warnings about custing to a generic type
	public OnePageViewData<ShortPrivateMessageData> browse( final int userID, final String userSessionId, final int forUserID,
															final boolean isAll, final boolean isReceived, final int offset,
															final int size) throws SiteException {
		return (new SecureServerAccess<OnePageViewData<ShortPrivateMessageData>>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected OnePageViewData<ShortPrivateMessageData> action() throws SiteException {
				logger.info( "Browsing the rooms of user " + forUserID + " by user " + userID );
				
				//Make sure that the user is an admin or it is browsing its own messages 
				final MainUserData userDataObject = UserSessionManager.getUserDataObject(userID);
				if( !userDataObject.isAdmin() && userID != forUserID ) {
					throw new InternalSiteException(InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR);
				}
				//Make sure that the number of requested messages is not exceeding the maximum allowed
				int local_size; 
				if( size > OnePageViewData.MAX_NUMBER_OF_ENTRIES_PER_PAGE ) {
					local_size = OnePageViewData.MAX_NUMBER_OF_ENTRIES_PER_PAGE;
				} else {
					local_size = size;
				}
				
				//Get the private messages
				OnePageViewData<ShortPrivateMessageData> messages = new OnePageViewData<ShortPrivateMessageData>();
				ConnectionWrapper<OnePageViewData<ShortPrivateMessageData>> countRoomsConnWrap = ConnectionWrapper.createConnectionWrapper( new SelectUserMessagesExecutor(forUserID, isAll, isReceived, offset, local_size) );
				countRoomsConnWrap.executeQuery( messages, ConnectionWrapper.XCURE_CHAT_DB );
				
				return messages;
			}
		}).execute();
	}

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
						final boolean isAll, final boolean isReceived ) throws SiteException {
		return (new SecureServerAccess<Integer>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Integer action() throws SiteException {
				logger.info( "Counting the private messages of user " + forUserID + " by user " + userID );
				
				//Make sure that the user is an admin or it is conting its own rooms 
				final MainUserData userDataObject = UserSessionManager.getUserDataObject(userID);
				if( !userDataObject.isAdmin() && userID != forUserID ) {
					throw new InternalSiteException(InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR);
				}
				
				//Get the avalable chat rooms
				OnePageViewData<ShortPrivateMessageData> messages = new OnePageViewData<ShortPrivateMessageData>();
				ConnectionWrapper<OnePageViewData<ShortPrivateMessageData>> countMsgsConnWrap = ConnectionWrapper.createConnectionWrapper( new CountUserMessagesExecutor(forUserID, isAll, isReceived) );
				countMsgsConnWrap.executeQuery( messages, ConnectionWrapper.XCURE_CHAT_DB );
				
				return messages.total_size;
			}
		}).execute();
	}
	
	/**
	 * This method allows count the number of unread user's personal messages
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public int countNewMessages( final int userID, final String userSessionId ) throws SiteException {
		return (new SecureServerAccess<Integer>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Integer action() throws SiteException {
				logger.debug( "Counting the newly received private messages of user " + userID );
				
				//Get the avalable chat rooms
				OnePageViewData<ShortPrivateMessageData> messages = new OnePageViewData<ShortPrivateMessageData>();
				ConnectionWrapper<OnePageViewData<ShortPrivateMessageData>> countNewMsgsConnWrap = ConnectionWrapper.createConnectionWrapper( new CountNewUserMessagesExecutor(userID) );
				countNewMsgsConnWrap.executeQuery( messages, ConnectionWrapper.XCURE_CHAT_DB );
				
				return messages.total_size;
			}
		}).execute( false, false );
	}
	
	/**
	 * Allows to get make the user pay for the payed smiles used in the message
	 * @param userData the data of the user who sent the message. 
	 * @param message the offline message to be priced.
	 * @throws InternalSiteException the exception is thrown in case the user can not pay for the message
	 */
	private void payForSimpleMessageSmileys( final UserData userData, final PrivateMessageData message ) throws InternalSiteException {
		String messageTitle = message.getMessageTitle();
		String messageBody = message.getMessageBody();
		if( messageTitle == null ){
			messageTitle = "";
		}
		if( messageBody == null ){
			messageBody = "";
		}
		//Account for the payed smileys
		final int price = SmileyHandler.countTextPriceWatchCategory( messageBody + messageTitle );
		
		//If the user can not pay for the used smiles, then throw an exception
		userData.decrementGoldPiecesCount( price );
	}
	
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
									final PrivateMessageData message, final boolean isAReply ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Sending a personal message from user " + userID + " to user " + message.getToUID() );
				
				MessageSendAbuseFilter.getMessageFilter(httpSession, userID).validateNewOfflineMessage( message, isAReply );
				
				//Get the user data object
				final UserData userData = UserSessionManager.getUserDataObject(userID);
				
				//Account for the payed smiles
				payForSimpleMessageSmileys( userData, message );
				
				//Put the message to the database
				InsertMessageExecutor sendSmplMsgExec = new InsertMessageExecutor(userID, message.getToUID(), ChatRoomData.UNKNOWN_ROOM_ID,
																					PrivateMessageData.SIMPLE_MESSAGE_TYPE, message.getMessageTitle(),
																					message.getMessageBody() );
				ConnectionWrapper<Void> sendSimpleMessageConnWrap = ConnectionWrapper.createConnectionWrapper( sendSmplMsgExec );
				sendSimpleMessageConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				
				//Account for the sent message
				userData.sentAnotherOfflineMessage();
				
				//Nothing the be returned here
				return null;
			}
		}).execute();
	}
	
	
	/**
	 * This method allows to retrieve a full message data
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param messageID the id of the message we want to retrieve
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public PrivateMessageData getMessage( final int userID, final String userSessionId,
											final int messageID ) throws SiteException {
		return (new SecureServerAccess<PrivateMessageData>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected PrivateMessageData action() throws SiteException {
				logger.info( "Retrieving the private message " + messageID + " by user " + userID );
				
				//Get the message data
				PrivateMessageData message = new PrivateMessageData();
				ConnectionWrapper<PrivateMessageData> getMsgConnWrap = ConnectionWrapper.createConnectionWrapper( new GetMessageExecutor(userID, messageID) );
				getMsgConnWrap.executeQuery( message, ConnectionWrapper.XCURE_CHAT_DB );
				
				//If this was an incoming message we read and it is also unread then mark it as read
				if( ( message.getToUID() == userID ) && ( ! message.isRead() ) ) {
					ConnectionWrapper<Void> markMsgReadConnWrap = ConnectionWrapper.createConnectionWrapper( new MarkMessageAsReadExecutor(userID, messageID) );
					markMsgReadConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				}
				
				return message;
			}
		}).execute();
	}
	
	/**
	 * This method allows to delete user messages, as a matter of fact we
	 * do not really delete them, but rather make them disappear from the
	 * browsing results.
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param forUserID the user we want to delete messages for
	 * @param messageIDS the list of message ID for the messages we want to delete
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	public void delete(final int userID, String userSessionId, final int forUserID,
						final List<Integer> messageIDS) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Deleting the messages " + messageIDS.toString() + " of user " + forUserID + " by user " + userID );
				
				//Check that the user deletes his messages or that he is an admin
				final MainUserData userDataObject = UserSessionManager.getUserDataObject(userID);
				if( !userDataObject.isAdmin() && userID != forUserID ) {
					throw new InternalSiteException(InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR);
				}
				
				//Delete/Hide the messages if they are his, Actually we do not really delete 
				//the messages but rather make them disappear from the browsing results 
				ConnectionWrapper<Void> hideMessagesConnWrap = ConnectionWrapper.createConnectionWrapper( new HideMessagesExecutor(forUserID, messageIDS, true) );
				hideMessagesConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				hideMessagesConnWrap = ConnectionWrapper.createConnectionWrapper( new HideMessagesExecutor(forUserID, messageIDS, false) );
				hideMessagesConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				
				//Nothing the be returned here
				return null;
			}
		}).execute();
	}
}
