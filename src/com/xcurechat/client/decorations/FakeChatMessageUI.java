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
package com.xcurechat.client.decorations;

import java.util.Date;
import java.util.Map;

import com.xcurechat.client.chat.ChatMessageBaseUI;

import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.MessageFontData;
import com.xcurechat.client.data.ShortUserData;

import com.xcurechat.client.utils.SmileyHandler;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * Represents the chat message, and info message, a private message, a simple message and etc.
 */
public class FakeChatMessageUI extends ChatMessageBaseUI {
		
	private FakeChatMessageUI( final String leftMessageStyleName,
							 final String rightMessageStyleName,
							 final FakeMessage message ) {
		super( leftMessageStyleName, rightMessageStyleName, message.isLeftMessage,
				ChatRoomData.UNKNOWN_ROOM_ID, ShortUserData.UNKNOWN_UID, null, false );
		
		//Set the message style
		addStyleNameToContent( MessageFontData.getFontTypeStyle( message.fontType ) );
		addStyleNameToContent( MessageFontData.getFontSizeStyle( message.fontSize ) );
		if( message.fontColor != MessageFontData.UNKNOWN_FONT_COLOR ) {
			addStyleNameToContent( MessageFontData.getFontColorStyle( message.fontColor ) );
		}
		
		//Add message title panel
		if( message.messageType == ChatMessage.Types.FAKE_ERROR_MESSAGE_TYPE ) {
			addMessageTitlePanel( new Date(), i18nTitles.chatMessageTypeError(), false, null );
		} else {
			addMessageTitlePanel( message );
		}
		
		//Process the message body by message type
		addMessageContent( message, null, ChatRoomData.UNKNOWN_ROOM_ID, true );
	}
	
	/**
	 * Allows to add the chat message image to the chat message content if the image is set
	 * @param thumbnailURL the url to the thumbnail of the chat-message image
	 * @param originalURL the url of the original chat-message image
	 */
	protected void addChatMessageImageContent( final String thumbnailURL, final String originalURL ) {
		addMessageContentWidget( createChatFileThumbnail( thumbnailURL, originalURL, ChatRoomData.UNKNOWN_ROOM_ID, null ) );
	}
	
	/**
	 * Is overridden here to allow for mimicking the
	 * click-open close personal message behavior.
	 */
	@Override
	protected void addMessageContent( final ChatMessage message,
									   final Map<Integer, ShortUserData> visibleUsers,
									   final int roomID, final boolean isAddRecepients ) {
		//Add message body
		addMessageTextContent( SmileyHandler.substituteSmilesWithSmileCodes( message.messageBody ) );
		
		//Add message image, with a hover over enlarge effect
		if( message instanceof FakeMessage ) {
			FakeMessage fakeMessage = (FakeMessage) message;
			if( ( fakeMessage.thumbnailURL != null ) && ( fakeMessage.originalURL != null ) ) {
				addChatMessageImageContent( fakeMessage.thumbnailURL, fakeMessage.originalURL );
			}
		}
	}
	
	/**
	 * Allows to get the chat message UI object
	 * @param message the message data itself
	 * @return the chat message UI object
	 */
	public static FakeChatMessageUI getChatMessageUI( final FakeMessage  message ) {
		//For all of these message types we just use different styles
		switch( message.messageType ) {
			case FAKE_ERROR_MESSAGE_TYPE:
				return  new FakeChatMessageUI( CommonResourcesContainer.ERROR_MESSAGE_LEFT_UI_STYLE,
												CommonResourcesContainer.ERROR_MESSAGE_RIGHT_UI_STYLE, message );
			case PRIVATE_MESSAGE_TYPE:
				return new FakeChatMessageUI( CommonResourcesContainer.PRIVATE_MESSAGE_STYLE_LEFT,
											  CommonResourcesContainer.PRIVATE_MESSAGE_STYLE_RIGHT, message );
			case USER_ROOM_ENTER_INFO_MESSAGE_TYPE:
				return new FakeChatMessageUI( CommonResourcesContainer.USER_ROOM_ENTER_INFO_MESSAGE_STYLE_LEFT,
											  CommonResourcesContainer.USER_ROOM_ENTER_INFO_MESSAGE_STYLE_RIGHT, message );
			case USER_ROOM_LEAVE_INFO_MESSAGE_TYPE:
				return new FakeChatMessageUI( CommonResourcesContainer.USR_ROOM_LEAVE_INFO_MESSAGE_STYLE_LEFT,
											  CommonResourcesContainer.USR_ROOM_LEAVE_INFO_MESSAGE_STYLE_RIGHT, message );
			case ROOM_IS_CLOSING_INFO_MESSAGE_TYPE:
				return new FakeChatMessageUI( CommonResourcesContainer.ROOM_IS_CLOSING_INFO_MESSAGE_STYLE_LEFT,
											  CommonResourcesContainer.ROOM_IS_CLOSING_INFO_MESSAGE_STYLE_RIGHT, message );
			default: 
				//The unknown message type is treated the same as the simple message type
				return new FakeChatMessageUI( CommonResourcesContainer.SIMPLE_MESSAGE_STYLE_LEFT,
											  CommonResourcesContainer.SIMPLE_MESSAGE_STYLE_RIGHT, message );
		}
	}
}
