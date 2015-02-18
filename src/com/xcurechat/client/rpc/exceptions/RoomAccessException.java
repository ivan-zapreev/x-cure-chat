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

/**
 * @author zapreevis
 * This class represents various room access errors
 */
public class RoomAccessException extends SiteException {
	
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;
	
	public static final Integer THE_ROOM_IS_NOT_ONLINE_ERROR = new Integer(1000);
	public static final Integer THE_ROOM_IS_ABOUT_TO_BE_CLOSED_ERROR = new Integer(1001);
	public static final Integer THE_ROOM_WAS_CLOSED_ERROR = new Integer(1002);
	public static final Integer THE_USER_DOES_NOT_HAVE_ROOM_ACCESS_ERROR = new Integer(1003);
	public static final Integer THE_USER_WAS_REMOVED_FROM_THE_ROOM_ERROR = new Integer(1004);
	public static final Integer THE_USER_IS_NOT_ALLOWED_TO_WRITE_INTO_THE_ROOM_ERROR = new Integer(1005);
	public static final Integer THE_USER_IS_NOT_IN_THE_ROOM_ERROR = new Integer(1006);
	
	static {
		//Register the error code in the super class, to avoid duplicates
		SiteLogicException.registerErrorCode( THE_ROOM_IS_NOT_ONLINE_ERROR );
		SiteLogicException.registerErrorCode( THE_ROOM_IS_ABOUT_TO_BE_CLOSED_ERROR );
		SiteLogicException.registerErrorCode( THE_ROOM_WAS_CLOSED_ERROR );
		SiteLogicException.registerErrorCode( THE_USER_DOES_NOT_HAVE_ROOM_ACCESS_ERROR );
		SiteLogicException.registerErrorCode( THE_USER_WAS_REMOVED_FROM_THE_ROOM_ERROR );
		SiteLogicException.registerErrorCode( THE_USER_IS_NOT_ALLOWED_TO_WRITE_INTO_THE_ROOM_ERROR );
		SiteLogicException.registerErrorCode( THE_USER_IS_NOT_IN_THE_ROOM_ERROR );
	}
	
	public RoomAccessException(){
		super();
	}
	
	public RoomAccessException( String errorMessage ){
		super( errorMessage );
	}
	
	public RoomAccessException( Integer errorCode, String roomName ){
		super( errorCode );
		this.roomName = roomName;
	}
	
	private String roomName = "";
	
	public String getRoomName() {
		return roomName;
	}
	
	public void setRoomName( String roomName ) {
		this.roomName = roomName;
	}

	@Override
	public void populateLocalizedMessages(UIErrorMessages errorMsgI18N) {		
		addLocalizedErrorMessage( THE_ROOM_IS_NOT_ONLINE_ERROR, errorMsgI18N.roomIsNotOnline( getRoomName() ) );
		addLocalizedErrorMessage( THE_ROOM_IS_ABOUT_TO_BE_CLOSED_ERROR, errorMsgI18N.roomIsAboutToBeClosed( getRoomName() ) );
		addLocalizedErrorMessage( THE_ROOM_WAS_CLOSED_ERROR, errorMsgI18N.roomWasClosed( getRoomName() ) );
		addLocalizedErrorMessage( THE_USER_DOES_NOT_HAVE_ROOM_ACCESS_ERROR, errorMsgI18N.userDoesNotHaveRoomAccess( getRoomName() ) );
		addLocalizedErrorMessage( THE_USER_WAS_REMOVED_FROM_THE_ROOM_ERROR, errorMsgI18N.userWasRemovedFromTheRoom( getRoomName() ) );
		addLocalizedErrorMessage( THE_USER_IS_NOT_ALLOWED_TO_WRITE_INTO_THE_ROOM_ERROR, errorMsgI18N.userIsNotAllowedToWriteIntoTheRoom( getRoomName() ) );
		addLocalizedErrorMessage( THE_USER_IS_NOT_IN_THE_ROOM_ERROR, errorMsgI18N.userIsNotInsideTheRoom( getRoomName() ) );
	}

}
