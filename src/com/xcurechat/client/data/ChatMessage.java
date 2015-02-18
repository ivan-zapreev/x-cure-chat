/**
 * The user data objects package.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client.data;

import java.util.LinkedHashMap;
import java.util.Date;
import java.util.LinkedHashSet;

import com.google.gwt.user.client.rpc.IsSerializable;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UIErrorMessages;
import com.xcurechat.client.rpc.exceptions.MessageException;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.utils.SmileyHandler;

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
 * @author zapreevis
 * Represents a chat message
 */
public class ChatMessage implements IsSerializable {
	//The maximum length of one word
	public static final int MAX_ONE_WORD_LENGTH = 32;
	//The maximum length of the message body
	public static final int MAX_MESSAGE_LENGTH = 1024;
	//The ID of the this message
	public static final int UNKNOWN_MESSAGE_ID = 0;
	
	/**
	 * Defined the available types of chat messages.
	 * WARNING: Only add new types at the end!
	 */
	public enum Types {
		UNKNOWN_MESSAGE_TYPE,
		SIMPLE_MESSAGE_TYPE,
		PRIVATE_MESSAGE_TYPE,
		USER_ROOM_ENTER_INFO_MESSAGE_TYPE,
		USER_ROOM_LEAVE_INFO_MESSAGE_TYPE,
		ROOM_IS_CLOSING_INFO_MESSAGE_TYPE,
		USER_STATUS_CHAGE_INFO_MESSAGE_TYPE,
		FAKE_ERROR_MESSAGE_TYPE					//This one is only used for the info site section 
	}
	
	//The maximum number of allowed message recipients
	public static final int MAXIMUM_NUMBER_MESSAGE_RECIPIENTS = 10;

	//The ID of the message
	public int messageID = UNKNOWN_MESSAGE_ID;
	//The ID of the user who sent this message
	public int senderID = MainUserData.UNKNOWN_UID;
	//The date-time when the message was sent
	public Date sentDate = null;
	//The ID of the room within which this message was sent
	public int roomID = ChatRoomData.UNKNOWN_ROOM_ID;
	//The set of user ID for the users we address the message to
	//We use the linked hash set here to preserve the order of message recipients
	public LinkedHashSet<Integer> recipientIDs = null;
	//The attached file descriptor or null if there is not attached image
	public ShortFileDescriptor fileDesc = null;
	//The message text body
	public String messageBody = "";
	//The message type
	public Types messageType = Types.UNKNOWN_MESSAGE_TYPE;
	
	//The ID of the user if this message is about the user event
	public int infoUserID = MainUserData.UNKNOWN_UID;
	//The Login name of the user if this message is about the user event
	public String infoUserLogin = "";
	
	//This is the transient field that is only used on the client side for
	//storing the mapping between the message recipient Id and Login Name
	//We usethe LinkedHashMap here to preserve the order of message recipients
	public transient LinkedHashMap<Integer, String> recepientIDToLoginName = new LinkedHashMap<Integer, String>();
	
	//The font type to be used when visualizing this message
	public int fontType = MessageFontData.DEFAULT_FONT_FAMILY;
	
	//The font size to be used when visualizing this message
	public int fontSize = MessageFontData.DEFAULT_FONT_SIZE;
	
	//The font color to be used when visualizing this message
	public int fontColor = MessageFontData.UNKNOWN_FONT_COLOR;
	
	/**
	 * The default constructor needed for serialization
	 */
	public ChatMessage() { }
	
	/**
	 * Allows to detect if the argument message is appendable to the given one.
	 * This is in a sense that if the message type is the same and the senders
	 * and receivers are the same then the argument message content can be added
	 * to this message content 
	 * @param message the message that we want to check for being appendable
	 * @return true if the message is appendable 
	 */
	public boolean isAppendable(final ChatMessage message) {
		final boolean result;
		
		if( ( message != null ) && ( this.roomID == message.roomID ) ) {
			switch( this.messageType ) {
				//The simple and private messages can only be merged if they:
				//	1. Are of the same type
				//	2. Have the same sender
				//	3. Have the same font settings
				//	4. Have the same recipients
				//	5. Have the same first recipients
				case SIMPLE_MESSAGE_TYPE:
				case PRIVATE_MESSAGE_TYPE:
					result = ( this.messageType == message.messageType ) &&
							 ( this.senderID    == message.senderID    ) &&
							 ( ( this.fontType  == message.fontType  ) &&
							   ( this.fontSize  == message.fontSize  ) &&
							   ( this.fontColor == message.fontColor ) ) &&
							 ( this.isEqualRecipients( message ) );
					break;
				
				//The status change messages can be merged with any other status change messages
				case USER_STATUS_CHAGE_INFO_MESSAGE_TYPE:
					result = ( message.messageType == Types.USER_STATUS_CHAGE_INFO_MESSAGE_TYPE );
					break;
				
				//Do not merge the following messages, with any other messages
				case UNKNOWN_MESSAGE_TYPE:
				case ROOM_IS_CLOSING_INFO_MESSAGE_TYPE:
				case USER_ROOM_ENTER_INFO_MESSAGE_TYPE:
				case USER_ROOM_LEAVE_INFO_MESSAGE_TYPE:
				default:
					result = false;
					break;
			}
		} else {
			//If the other message is null or the room ids are different
			//then clearly the messages can not be merged
			result = false;
		}
		
		return result;
	}
	
	/**
	 * Allows to check that the message recipients are the same, this includes checking
	 * that the first message recipients are the same as well the order of the subsequent
	 * recipients is not important, but the first one defines the main recipient.
	 * @param message the message to which recipients we will compare
	 * @return true if the recipients are the same and that the first recipients are the same
	 */
	private boolean isEqualRecipients( final ChatMessage message ) {
		//First we check that the recipients are just equal as sets of recipient ids
		boolean isEqual =  ( ( this.recipientIDs == null ) && ( message.recipientIDs == null ) ) ||
						   ( ( this.recipientIDs != null ) && ( message.recipientIDs != null ) && 
							 ( this.recipientIDs.equals( message.recipientIDs ) ) );
		
		//Next we check that the first recipients are the same
		if( isEqual && ( this.recipientIDs != null ) && ( this.recipientIDs.size() > 0 ) ) {
			isEqual = ( this.recipientIDs.iterator().next().intValue() ==
						message.recipientIDs.iterator().next().intValue() );
		}
		
		return isEqual;
	}
	
	/**
	 * Allows to clone the chat message data
	 */
	public ChatMessage clone() {
		ChatMessage messageCopy = new ChatMessage();
		
		messageCopy.messageID = messageID;
		messageCopy.senderID = senderID;
		messageCopy.sentDate = sentDate;
		messageCopy.roomID = roomID;
		messageCopy.recipientIDs = new LinkedHashSet<Integer>();
		if( recipientIDs != null ) {
			messageCopy.recipientIDs.addAll( recipientIDs );
		}
		messageCopy.fileDesc = (fileDesc != null) ? fileDesc.clone() : null;
		messageCopy.messageBody = messageBody;
		messageCopy.messageType = messageType;
		messageCopy.infoUserID = infoUserID;
		messageCopy.infoUserLogin = infoUserLogin;
		messageCopy.recepientIDToLoginName = new LinkedHashMap<Integer, String>();
		if( recepientIDToLoginName != null ) {
			messageCopy.recepientIDToLoginName.putAll( recepientIDToLoginName );
		}
		messageCopy.fontType = fontType;
		messageCopy.fontSize = fontSize;
		messageCopy.fontColor = fontColor;
		
		return messageCopy;
	}
	
	/**
	 * @return true if the message has recipients
	 */
	public boolean hasRecipients() {
		return ( recipientIDs != null ) && ! recipientIDs.isEmpty();
	}
	
	/**
	 * Allows to check if the given user is the first message recipient
	 * @param userID the id of the user
	 * @return true if the user is the first message recipient
	 */
	public boolean isFirstRecipient( final int userID ) {
		boolean result = false;
		if( hasRecipients() ){
			result = ( recipientIDs.iterator().next() == userID );
		}
		return result;
	}
	
	/**
	 * @return true if this is a user message, but not a system message
	 */
	public boolean isUserMessage() {
		return ( this.messageType == Types.SIMPLE_MESSAGE_TYPE ) || ( this.messageType == Types.PRIVATE_MESSAGE_TYPE );
	}
	
	/**
	 * @return true if this is an info message about some user entering the room
	 */
	public boolean isUserRoomEnterInfoMessage() {
		return this.messageType == Types.USER_ROOM_ENTER_INFO_MESSAGE_TYPE;
	}
	
	/**
	 * This method validates the data object, it is meant to be
	 * used before sending the data to the server.
	 */
	public void validateAndComplete() throws SiteException {
		//Convert the smile symbols into the smile codes
		messageBody = SmileyHandler.substituteSmilesWithSmileCodes( messageBody );
		
		//Check for errors
		UIErrorMessages errorsMsgs = I18NManager.getErrors();
		if( messageBody.trim().isEmpty() && ( fileDesc == null ) ) {
			//Neither the message body not the message image are set, i.e. it is an empty message
			throw new MessageException( errorsMsgs.chatMessageIsEmpty() );
		} else {
			if( ( messageType == Types.PRIVATE_MESSAGE_TYPE ) &&
				( ( recipientIDs == null ) || recipientIDs.isEmpty() ) ) {
				//This is a private message but it has not recipients
				throw new MessageException( errorsMsgs.chatMessageIsPrivateButHasNoRecepients() );
			} else {
				if( ( messageType != Types.PRIVATE_MESSAGE_TYPE ) && ( messageType != Types.SIMPLE_MESSAGE_TYPE ) ) {
					//The user is not allowed to send messages that have type other than simple of private
					throw new MessageException( errorsMsgs.chatMessageHasWrongType() );
				} else {
					if( ( messageBody != null ) && ( messageBody.length() > MAX_MESSAGE_LENGTH ) ) {
						//The message is too long
						throw new MessageException( errorsMsgs.chatMessageIsTooLong( messageBody.length() - MAX_MESSAGE_LENGTH ) );
					}
				}
			}
		}
	}
}