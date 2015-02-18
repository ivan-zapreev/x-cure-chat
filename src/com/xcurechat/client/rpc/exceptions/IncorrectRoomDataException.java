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
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client.rpc.exceptions;

import com.xcurechat.client.i18n.UIErrorMessages;

/**
 * @author zapreevis
 * Any kind of user registration exception
 */
public class IncorrectRoomDataException extends UserLoginException {
	
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;

	//The server side reported that the room does not exist
	public static final Integer ROOM_DOES_NOT_EXIST_ERR = new Integer(200);
	//The server side reported that the room is closed
	public static final Integer ROOM_IS_CLOSED_ERR = new Integer(201);
	//The server side reported that the user can not enter the room due to access restrictions
	public static final Integer CAN_NOT_ENTER_THE_ROOM_ERR = new Integer(202);
	//The maximum allowed number of rooms for a user was exceeded
	public static final Integer TOO_MANY_USER_ROOMS_ERR = new Integer(203);
	//The user is not allowed to create permanent/main rooms
	public static final Integer USER_CAN_NOT_CREATE_ROOM_TYPE_ERR = new Integer(204);
	//The user is not allowed to update rooms of other users
	public static final Integer USER_CAN_NOT_UPDATE_OTHERS_ROOM_ERR = new Integer(205);
	//The user can not create a room for another user
	public static final Integer USER_CAN_NOT_CREATE_ROOM_FOR_ANOTHER_USER_ERR = new Integer(206); 
	//The regular user can not change the room-user access object
	public static final Integer SIMPLE_USER_CAN_NOT_CHANGE_ROOM_USER_ACCESS_ERR = new Integer(207); 
	//The regular user can not insert a detailed room-user access object
	public static final Integer SIMPLE_USER_CAN_NOT_INSERT_DETAILED_ROOM_USER_ACCESS_ERR = new Integer(208); 
	//The the system room-user access already exists
	public static final Integer SYSTEM_ROOM_USER_ACCESS_EXISTS_ERR = new Integer(209); 
	//The the regular room-user access already exists
	public static final Integer REGULAR_ROOM_USER_ACCESS_EXISTS_ERR = new Integer(210);
	//The the room-user access already exists
	public static final Integer ROOM_USER_ACCESS_EXISTS_ERR = new Integer(211);
	//The the room-user access right does not have any access permissions on
	public static final Integer ROOM_USER_ACCESS_IS_EMPTY_ERR = new Integer(212);
	
	//The maximum allowed number of rooms;
	private int max_room_number = 0;
	
	static {
		//Register the error code in the super class, to avoid duplicates
		SiteLogicException.registerErrorCode( ROOM_DOES_NOT_EXIST_ERR );
		SiteLogicException.registerErrorCode( ROOM_IS_CLOSED_ERR );
		SiteLogicException.registerErrorCode( CAN_NOT_ENTER_THE_ROOM_ERR );
		SiteLogicException.registerErrorCode( TOO_MANY_USER_ROOMS_ERR );
		SiteLogicException.registerErrorCode( USER_CAN_NOT_CREATE_ROOM_TYPE_ERR );
		SiteLogicException.registerErrorCode( USER_CAN_NOT_UPDATE_OTHERS_ROOM_ERR );
		SiteLogicException.registerErrorCode( USER_CAN_NOT_CREATE_ROOM_FOR_ANOTHER_USER_ERR );
		SiteLogicException.registerErrorCode( SIMPLE_USER_CAN_NOT_CHANGE_ROOM_USER_ACCESS_ERR );
		SiteLogicException.registerErrorCode( SIMPLE_USER_CAN_NOT_INSERT_DETAILED_ROOM_USER_ACCESS_ERR );
		SiteLogicException.registerErrorCode( SYSTEM_ROOM_USER_ACCESS_EXISTS_ERR );
		SiteLogicException.registerErrorCode( REGULAR_ROOM_USER_ACCESS_EXISTS_ERR );
		SiteLogicException.registerErrorCode( ROOM_USER_ACCESS_EXISTS_ERR );
		SiteLogicException.registerErrorCode( ROOM_USER_ACCESS_IS_EMPTY_ERR );
	}

	public IncorrectRoomDataException(){
		super();
	}
	
	public IncorrectRoomDataException( String errorMessage ){
		super( errorMessage );
	}
	
	public IncorrectRoomDataException( Integer errorCode ){
		super( errorCode );
	}
	
	public IncorrectRoomDataException( Integer errorCode, final int max_room_number ){
		super( errorCode );
		this.max_room_number = max_room_number;
	}

	@Override
	public void populateLocalizedMessages( UIErrorMessages errorMsgI18N ) {
		super.populateLocalizedMessages( errorMsgI18N );
		
		addLocalizedErrorMessage( ROOM_DOES_NOT_EXIST_ERR, errorMsgI18N.roomDoesNotExistError() );
		addLocalizedErrorMessage( ROOM_IS_CLOSED_ERR, errorMsgI18N.roomIsClosedError() );
		addLocalizedErrorMessage( CAN_NOT_ENTER_THE_ROOM_ERR, errorMsgI18N.canNotEnterTheRoomError() );
		addLocalizedErrorMessage( TOO_MANY_USER_ROOMS_ERR, errorMsgI18N.tooManyRooms(max_room_number) );
		addLocalizedErrorMessage( USER_CAN_NOT_CREATE_ROOM_TYPE_ERR, errorMsgI18N.unallowedRoomTypeForUser() );
		addLocalizedErrorMessage( USER_CAN_NOT_UPDATE_OTHERS_ROOM_ERR, errorMsgI18N.canNotUpdateOthersRooms() );
		addLocalizedErrorMessage( USER_CAN_NOT_CREATE_ROOM_FOR_ANOTHER_USER_ERR, errorMsgI18N.canNotCreateRoomForOthers() );
		addLocalizedErrorMessage( SIMPLE_USER_CAN_NOT_CHANGE_ROOM_USER_ACCESS_ERR, errorMsgI18N.canNotUpdateRoomUserAccess() );
		addLocalizedErrorMessage( SIMPLE_USER_CAN_NOT_INSERT_DETAILED_ROOM_USER_ACCESS_ERR, errorMsgI18N.canNotInsertDetailedRoomUserAccess() );
		addLocalizedErrorMessage( SYSTEM_ROOM_USER_ACCESS_EXISTS_ERR, errorMsgI18N.systemRoomUserAccessAlreadyExists() );
		addLocalizedErrorMessage( REGULAR_ROOM_USER_ACCESS_EXISTS_ERR, errorMsgI18N.regularRoomUserAccessAlreadyExists() );
		addLocalizedErrorMessage( ROOM_USER_ACCESS_EXISTS_ERR, errorMsgI18N.roomUserAccessAlreadyExists() );
		addLocalizedErrorMessage( ROOM_USER_ACCESS_IS_EMPTY_ERR, errorMsgI18N.roomUserAccessIsEmpty() );
	}

}
