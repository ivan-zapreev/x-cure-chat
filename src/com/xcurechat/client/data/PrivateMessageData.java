/**
 * The user data objects package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.data;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UIErrorMessages;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.MessageException;
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
 * This class represents the private message
 */
public class PrivateMessageData extends ShortPrivateMessageData {
	
	//The maximum length of the message body
	public static final int MAXIMUM_MESSAGE_BODY_LENGTH = 2048;
	
	//The message body string
	private String messageBody = null;
	
	//The room ID for the room access related messages
	private int roomID = ChatRoomData.UNKNOWN_ROOM_ID;
	
	public PrivateMessageData() {
		//All of the fields are initialized in their definitions
		super();
	}

	/**
	 * @param messageBody the messageBody to set
	 */
	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	/**
	 * @return the messageBody
	 */
	public String getMessageBody() {
		return messageBody;
	}

	/**
	 * @param roomID the roomID to set
	 */
	public void setRoomID(int roomID) {
		this.roomID = roomID;
	}

	/**
	 * @return the roomID
	 */
	public int getRoomID() {
		return roomID;
	}
	
	/**
	 * This method is responsible for validating the message content before sending.
	 * It checks that:
	 * 1. The recepient ID is set
	 * 2. The body and the title are not longer than allowed
	 * 3. Either the body or the title are not empty
	 * Also, it does smiley to smiley codes substitution in the beginning
	 */
	public void validateAndComplete() throws SiteException {
		MessageException exception = new MessageException();
		UIErrorMessages errorsI18N = I18NManager.getErrors();
		
		if( this.getToUID() == UserData.UNKNOWN_UID ) {
			exception.addErrorMessage( errorsI18N.undefinedMsgRecepient() );
		}
		
		//NOTE: We do not convert the smileys to smiley codes here because it is only needed when the full message is viewed. 
		
		//Convert the smile symbols into the smile codes
		messageBody = SmileyHandler.substituteSmilesWithSmileCodes( messageBody );
		
		final boolean isTitleEmpty = ( this.messageTitle == null) || ( this.messageTitle.trim().length() == 0 );
		final boolean isBobyEmpty = ( this.messageBody == null) || ( this.messageBody.trim().length() == 0 );
		if( isTitleEmpty && isBobyEmpty ) {
			exception.addErrorMessage( errorsI18N.bothMsgTitleAndBodyAreEmpty() );
		} else {
			if( ! isTitleEmpty && this.messageTitle.length() > MAXIMUM_MESSAGE_SUBJECT_LENGTH ) {
				exception.addErrorMessage( errorsI18N.theMsgTitleIsTooLong(MAXIMUM_MESSAGE_SUBJECT_LENGTH) );
			}
			if( ! isBobyEmpty && this.messageBody.length() > MAXIMUM_MESSAGE_BODY_LENGTH ) {
				exception.addErrorMessage( errorsI18N.theMsgBodyIsTooLong(MAXIMUM_MESSAGE_BODY_LENGTH) );
			}
		}
		
		if( exception.containsErrors() ) {
			throw exception;
		}
	}
}
