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
 * The user interface package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.chat;

import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.chat.messages.SendChatMessageManager;
import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.MessageFontData;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * Represents the chat message, and info message, a private message, a simple message and etc.
 */
public class ChatMessageUI extends ChatMessageBaseUI {
	/**
	 * @author zapreevis
	 * This class represents a click handler that allows to reply to the message
	 */
	private class ReplyMessageClickHandler implements ClickHandler {
		//Message recipient ID to login names, we use a linked map to preserve the proper order of recipients
		private final LinkedHashMap<Integer, String> recipientIDToLoginName = new LinkedHashMap<Integer, String>();
		//The stored message data
		private final ChatMessage message;
		
		/**
		 * Adds the recipient to recipientIDToLoginName is the
		 * recipient is visible in the room and he is not the
		 * current user using the client interface.
		 * @param visibleUsers room's visible users
		 * @param recipientID the id of potential reply-message recipient
		 */
		private void addReplyMessageRecipient( final Map<Integer, ShortUserData> visibleUsers, final int recipientID) {
			if( recipientID != SiteManager.getUserID() ) {
				ShortUserData userData = visibleUsers.get( recipientID );
				if( userData != null  ) {
					//If the recipient is present in the room then we add it to the map
					recipientIDToLoginName.put( recipientID , userData.getUserLoginName() );
				}
			}
		}
		
		public ReplyMessageClickHandler( final ChatMessage message, final Map<Integer, ShortUserData> visibleUsers ) {
			this.message = message;
			//Add the sender
			addReplyMessageRecipient( visibleUsers, message.senderID );
			//In case it is an info message, we add the user this info is about.
			addReplyMessageRecipient( visibleUsers, message.infoUserID );
			//Add the former message recipients
			if( message.recipientIDs != null ) {
				Iterator<Integer> recepientsIter = message.recipientIDs.iterator();
				while( recepientsIter.hasNext() ) {
					addReplyMessageRecipient( visibleUsers, recepientsIter.next() );
				}
			}
		}
		
		@Override
		public void onClick(ClickEvent event) {
			//Open the new reply dialog
			SendChatMessageManager.getInstance().replyToChatMessage( message, recipientIDToLoginName );
			//Do not let the event go further
			event.stopPropagation();
		}
	}
	
	protected ChatMessageUI( final String leftMessageStyleName,
							 final String rightMessageStyleName,
							 final boolean isLeft, final ChatMessage message,
							 final Map<Integer, ShortUserData> visibleUsers,
							 final int roomID, final boolean isWithReplyClickHandler ) {
		super( leftMessageStyleName, rightMessageStyleName, isLeft, roomID,
			   message.infoUserID, visibleUsers, isWithReplyClickHandler );
		
		//Set the click handler
		if( isWithReplyClickHandler ) {
			setReplyMessageClickHandler( this.new ReplyMessageClickHandler( message, visibleUsers ) );
		}
		
		//Set the message style
		addStyleNameToContent( MessageFontData.getFontTypeStyle( message.fontType ) );
		addStyleNameToContent( MessageFontData.getFontSizeStyle( message.fontSize ) );
		if( message.fontColor != MessageFontData.UNKNOWN_FONT_COLOR ) {
			addStyleNameToContent( MessageFontData.getFontColorStyle( message.fontColor ) );
		}
		
		//Add message title panel
		addMessageTitlePanel( message );
		
		//Process the message body by message type
		addMessageContent( message, visibleUsers, roomID, true );
	}
	
	/**
	 * Allows to get the chat message UI object
	 * @param isLeft true if the message has the comes from the left, false from the right
	 * @param message the message data itself
	 * @param visibleUsers the mapping of visible IDs to their short data for the users present in the room
	 * @param roomID the id of the room this message belongs to
	 * @return the chat message UI object
	 */
	public static ChatMessageUI getChatMessageUI( final boolean isLeft, final ChatMessage message,
													final Map<Integer, ShortUserData> visibleUsers, final int roomID ) {
		//For all of these message types we just use different styles
		switch( message.messageType ) {
			case PRIVATE_MESSAGE_TYPE:
				return new ChatMessageUI( CommonResourcesContainer.PRIVATE_MESSAGE_STYLE_LEFT, CommonResourcesContainer.PRIVATE_MESSAGE_STYLE_RIGHT,
										  isLeft, message, visibleUsers, roomID, true );
			case USER_STATUS_CHAGE_INFO_MESSAGE_TYPE:
			case USER_ROOM_ENTER_INFO_MESSAGE_TYPE:
				return new ChatMessageUI( CommonResourcesContainer.USER_ROOM_ENTER_INFO_MESSAGE_STYLE_LEFT,
										  CommonResourcesContainer.USER_ROOM_ENTER_INFO_MESSAGE_STYLE_RIGHT,
										  isLeft, message, visibleUsers, roomID, true );
			case USER_ROOM_LEAVE_INFO_MESSAGE_TYPE:
				return new ChatMessageUI( CommonResourcesContainer.USR_ROOM_LEAVE_INFO_MESSAGE_STYLE_LEFT,
										  CommonResourcesContainer.USR_ROOM_LEAVE_INFO_MESSAGE_STYLE_RIGHT,
										  isLeft, message, visibleUsers, roomID, false );
			case ROOM_IS_CLOSING_INFO_MESSAGE_TYPE:
				return new ChatMessageUI( CommonResourcesContainer.ROOM_IS_CLOSING_INFO_MESSAGE_STYLE_LEFT,
										  CommonResourcesContainer.ROOM_IS_CLOSING_INFO_MESSAGE_STYLE_RIGHT,
										  isLeft, message, visibleUsers, roomID, false );
			default: 
				//The unknown message type is treated the same as the simple message type
				return new ChatMessageUI( CommonResourcesContainer.SIMPLE_MESSAGE_STYLE_LEFT, CommonResourcesContainer.SIMPLE_MESSAGE_STYLE_RIGHT,
										  isLeft, message, visibleUsers, roomID, true );
		}
	}
}
