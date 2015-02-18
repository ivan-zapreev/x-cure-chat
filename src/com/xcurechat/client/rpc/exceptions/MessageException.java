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
 * The exceptions package for exceptions that come in RPC calls.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.rpc.exceptions;

import com.xcurechat.client.i18n.UIErrorMessages;

public class MessageException extends SiteException {
	
	public static final Integer UNABLE_TO_RETRIEVE_MESSAGE = new Integer(900);
	public static final Integer UNABLE_TO_RETRIEVE_ROOM_ACCESS_REQUEST = new Integer(910);
	public static final Integer THE_MESSAGE_YOU_REPLY_TO_DOES_NOT_EXIST = new Integer(920);
	public static final Integer THE_MESSAGE_YOU_UPDATE_DOES_NOT_EXIST = new Integer(930);
	public static final Integer IMPROPER_EMBEDDED_OBJECT = new Integer(940);
	public static final Integer THE_RESULTING_MESSAGE_IS_TOO_LONG = new Integer(950);
	public static final Integer THE_MESSAGE_DOES_NOT_EXIST = new Integer(960);
	public static final Integer YOU_HAVE_ALREADY_VOTED_FOR_THIS_MESSAGE = new Integer(970);
	
	static {
		//Register the error code in the super class, to avoid duplicates
		SiteLogicException.registerErrorCode( UNABLE_TO_RETRIEVE_MESSAGE );
		SiteLogicException.registerErrorCode( UNABLE_TO_RETRIEVE_ROOM_ACCESS_REQUEST );
		SiteLogicException.registerErrorCode( THE_MESSAGE_YOU_REPLY_TO_DOES_NOT_EXIST );
		SiteLogicException.registerErrorCode( THE_MESSAGE_YOU_UPDATE_DOES_NOT_EXIST );
		SiteLogicException.registerErrorCode( IMPROPER_EMBEDDED_OBJECT );
		SiteLogicException.registerErrorCode( THE_RESULTING_MESSAGE_IS_TOO_LONG );
		SiteLogicException.registerErrorCode( THE_MESSAGE_DOES_NOT_EXIST );
		SiteLogicException.registerErrorCode( YOU_HAVE_ALREADY_VOTED_FOR_THIS_MESSAGE );
	}
	
	public MessageException(){
		super();
	}
	
	public MessageException( String errorMessage ){
		super( errorMessage );
	}
	
	public MessageException( Integer errorCode ){
		super( errorCode );
	}
	
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;

	@Override
	public void populateLocalizedMessages(UIErrorMessages errorMsgI18N) {
		addLocalizedErrorMessage( UNABLE_TO_RETRIEVE_MESSAGE, errorMsgI18N.messageDeletedOrIsNotUsers() );
		addLocalizedErrorMessage( UNABLE_TO_RETRIEVE_ROOM_ACCESS_REQUEST, errorMsgI18N.roomDeletedAccessRequestIsInvalid() );
		addLocalizedErrorMessage( THE_MESSAGE_YOU_REPLY_TO_DOES_NOT_EXIST, errorMsgI18N.forumMessageParentMessageDoesNotExist() );
		addLocalizedErrorMessage( THE_MESSAGE_YOU_UPDATE_DOES_NOT_EXIST, errorMsgI18N.forumMessageToUpdateDoesNotExist() );
		addLocalizedErrorMessage( IMPROPER_EMBEDDED_OBJECT, errorMsgI18N.improperEmbeddedObject() );
		addLocalizedErrorMessage( THE_RESULTING_MESSAGE_IS_TOO_LONG, errorMsgI18N.resultingForumMessageIsTooLongEmbeddedFlash() );
		addLocalizedErrorMessage( THE_MESSAGE_DOES_NOT_EXIST, errorMsgI18N.theMessageNoLongerExists() );
		addLocalizedErrorMessage( YOU_HAVE_ALREADY_VOTED_FOR_THIS_MESSAGE, errorMsgI18N.youHaveAlreadyVotedForThisMessage() );
	}
}
