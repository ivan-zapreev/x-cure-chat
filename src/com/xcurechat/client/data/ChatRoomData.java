/**
 * The user data objects package.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client.data;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Image;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UIErrorMessages;
import com.xcurechat.client.i18n.UITitlesI18N;

import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.IncorrectRoomDataException;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

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
 * This class represents the chat room that is available for the client.
 */
public class ChatRoomData  implements IsSerializable {
	
	//Any english or any cyrillic letter (plus some cyrillic extensions)
	private static final String ANY_LETTER = "[.,?!:;'\"( )a-zA-Z\u0400-\u045F\u048A-\u04F9]";
	
	//The max/min lengths of room's name
	public static final int MIN_ROOM_NAME_LENGTH = 3;
	public static final int MAX_ROOM_NAME_LENGTH = 30;
	//Format for the room's name
	public static final String ROOM_NAME_PATTERN = "(" + ANY_LETTER + "|[0-9]){"+MIN_ROOM_NAME_LENGTH+","+MAX_ROOM_NAME_LENGTH+"}";
	
	//The max/min lengths of room's description
	public static final int MIN_ROOM_DESC_LENGTH = 0;
	public static final int MAX_ROOM_DESC_LENGTH = 120;
	//Format for the room's description
	public static final String ROOM_DESC_PATTERN = "(" + ANY_LETTER + "|[0-9]){"+MIN_ROOM_DESC_LENGTH+","+MAX_ROOM_DESC_LENGTH+"}";
	
	//The maximum number of rooms to be created by a user/admin
	public static final int MAX_ROOMS_NUMBER_USER = 3;
	public static final int MAX_ROOMS_NUMBER_ADMIN = 10;
	
	//The default name on the main chat server room
	public static final String DEFAULT_ROOM_NAME = "Main";
	
	//If rid == DEFAULT_ROOM_NAME then this access is for the main chat room
	public static final int UNKNOWN_ROOM_ID = 0;
	
	//Various room types
	public static final int PUBLIC_ROOM_TYPE = 0;
	public static final int PROTECTED_ROOM_TYPE = 1;
	public static final int PRIVATE_ROOM_TYPE = 2;
	
	//Roomduration in hours
	public static final int CLEAN_HOURS_DURATION = -2;
	public static final int UNKNOWN_HOURS_DURATION = -1;
	public static final int TWO_HOURS_DURATION = 2;
	public static final int FOUR_HOURS_DURATION = 4;
	public static final int EIGHT_HOURS_DURATION = 8;
	public static final int TWENTYFOUR_HOURS_DURATION = 24;

	//The room id, is assigned after the room is created on the server
	private int RID = UNKNOWN_ROOM_ID;
	//The room name
	private String theRoomName;
	//The room description
	private String theRoomDesc;
	//The id of the user who created the room
	private int UID = UserData.UNKNOWN_UID;
	//The user login name
	private String ownerLoginName;
	//True if the room is permanent
	private boolean is_permanent = false;
	//True if this is the main chat room of the server
	private boolean is_main = false;
	//The date when the non-permanent room expires
	private Date expires;
	//The room type: Public, Private, Protected
	private int roomType = PUBLIC_ROOM_TYPE;
	//The room duration perioud, this has to be send to the server 
	//in case a new room is created or the old one is changed. 
	private int roomDurationTimeHours = UNKNOWN_HOURS_DURATION; 
	
	/**
	 * The default constructor needed for serialization
	 */
	public ChatRoomData( ){
	}
			
	/**
	 * The basic constructor
	 */
	public ChatRoomData( int RID, String theRoomName, String theRoomDesc, int UID, String ownerLoginName,
						boolean is_permanent, boolean is_main, Date expires, int roomType ) throws IncorrectRoomDataException {
		this.RID = RID;
		this.theRoomName = theRoomName;
		this.theRoomDesc = theRoomDesc;
		this.UID = UID;
		this.ownerLoginName = ownerLoginName;
		this.is_permanent = is_permanent;
		this.is_main = is_main;
		this.expires = expires;
		setRoomType(roomType);
	}
	
	public int getRoomDurationTimeHours() {
		return roomDurationTimeHours;
	}
	
	public void setRoomDurationTimeHours(int roomDurationTimeHours) {
		this.roomDurationTimeHours = roomDurationTimeHours;
	}
	
	/**
	 * This method should work only if the expiration date is set
	 * @return true if the given expiration date-time is before the current date-time
	 * and the room is not permanent and it is not main
	 */
	public boolean isExpired(){
		//&& !roomData.isMain() && !roomData.isPermanent()
		final boolean isExpired;
		final Date exp_date = this.getExpirationDate();
		if( exp_date != null ) {
			isExpired = exp_date.before( new Date() );
		} else {
			isExpired = false;
		}
		return isExpired && !( this.isPermanent() || this.isMain() ) ;
	}
	
	public int getRoomID(){
		return RID;
	}
	
	public void setRoomID(final int roomID){
		RID = roomID;
	}
	
	public String getOwnerName(){
		return this.ownerLoginName;
	}
	
	public void setOwnerName(String ownerLoginName){
		this.ownerLoginName = ownerLoginName;
	}
	
	public String getRoomName(){
		return this.theRoomName;
	}
	
	public void setRoomName(String theRoomName){
		this.theRoomName = theRoomName;
	}
	
	public String getRoomDesc(){
		return this.theRoomDesc;
	}
	
	public void setRoomDesc(String theRoomDesc){
		this.theRoomDesc = theRoomDesc;
	}
	
	public int getOwnerID(){
		return UID;
	}
	
	public void setOwnerID(int UID){
		this.UID = UID;
	}
	
	public boolean isPermanent() {
		return is_permanent;
	}
	
	public void setPermanent(boolean permanent) {
		is_permanent = permanent;
	}

	public boolean isMain() {
		return is_main;
	}

	public void setMain(boolean main) {
		is_main = main;
	}

	public Date getExpirationDate() {
		return expires;
	}
	
	public void setExpirationDate(final Date date){
		expires = date;
	}
	
	public int getRoomType(){
		return roomType;
	}
	
	public void setRoomType(int roomType) throws IncorrectRoomDataException {
		if( ( roomType != PUBLIC_ROOM_TYPE ) && ( roomType != PROTECTED_ROOM_TYPE ) && ( roomType != PRIVATE_ROOM_TYPE ) ) {
			//Unless something goes really wrong, this should not be happenning
			throw new IncorrectRoomDataException("Unknown room type "+roomType);
		} else {
			this.roomType = roomType;
		}
	}
	
	/**
	 * @return if the room is public or main
	 */
	public boolean isPublic(){
		return ( roomType == PUBLIC_ROOM_TYPE ) || isMain() ;
	}

	public boolean isProtected(){
		return roomType == PROTECTED_ROOM_TYPE;
	}

	public boolean isPrivate(){
		return roomType == PRIVATE_ROOM_TYPE;
	}
	
	/**
	 * The max number of room that can be created 
	 * @param userProfileType
	 * @return
	 */
	public static int getMaxNumberOfRooms(final int userProfileType ) {
		if( userProfileType == MainUserData.ADMIN_USER_TYPE ){
			return MAX_ROOMS_NUMBER_ADMIN;
		} else {
			return MAX_ROOMS_NUMBER_USER;
		}
	}
	
	public static void validateDuration(final boolean can_be_undefined, final int durationHours, SiteException exception ) {
		UIErrorMessages errorsI18N = I18NManager.getErrors();
		if( ( durationHours != TWO_HOURS_DURATION ) &&  ( durationHours != FOUR_HOURS_DURATION ) && 
			( durationHours != EIGHT_HOURS_DURATION ) && ( durationHours != TWENTYFOUR_HOURS_DURATION ) &&
			( durationHours != CLEAN_HOURS_DURATION ) ) {
			if( ! can_be_undefined ) {
				exception.addErrorMessage( errorsI18N.unknownRoomLifeTime() );
			} else {
				if ( durationHours != UNKNOWN_HOURS_DURATION ) {
					exception.addErrorMessage( "The time duration that you have provided is not in the required range: "+durationHours );
				}
			}
		}
	}
	
	/**
	 * This method validates the data object, it is meant to be
	 * used before sending the data to the server. Also every client
	 * can run this method, to validate 
	 * @param compulsoryDuration true if the room life time has to be set
	 */
	public void validate(final boolean compulsoryDuration) throws IncorrectRoomDataException {
		IncorrectRoomDataException exception = new IncorrectRoomDataException();
		UIErrorMessages errorsI18N = I18NManager.getErrors();
		UITitlesI18N titlesI18N = I18NManager.getTitles();
		
		if( !theRoomName.matches( ROOM_NAME_PATTERN ) ){
			exception.addErrorMessage(errorsI18N.incorrectRoomNameFormat(MIN_ROOM_NAME_LENGTH, MAX_ROOM_NAME_LENGTH));
		}
		
		if( !theRoomDesc.matches(ROOM_DESC_PATTERN) ){
			exception.addErrorMessage( errorsI18N.incorrectTextFieldFormat( titlesI18N.roomDescriptionFieldTitle(), MAX_ROOM_DESC_LENGTH ) );
		}
		
		if( compulsoryDuration ) {
			validateDuration( is_permanent, roomDurationTimeHours, exception);
		}
		
		//Check if there are error, if yes, then throw an exception
		if( exception.containsErrors() ){
			throw exception;
		}
	}
	
	/**
	 * This method allows to retrieve the room description, taking care about
	 * the room description internatianalizations. The latter is only valid for
	 * the main room.
	 * @param roomData the room-data object
	 * @return the room description
	 */
	public static String getRoomDescription( ChatRoomData roomData ) {
		if( roomData.isMain() ) {
			return I18NManager.getTitles().mainRoomDescription();
		} else {
			return roomData.getRoomDesc();
		}
		
	}

	/**
	 * This method allows to retrieve the room name, taking care about
	 * the room name internatianalizations. The latter is only valid for
	 * the main room.
	 * @param roomData the room-data object
	 * @return the room name
	 */
	public static String getRoomName( final ChatRoomData roomData ){
		if( roomData != null ) {
			if( roomData.isMain() ) {
				return I18NManager.getTitles().mainRoomTitle();
			} else {
				return roomData.getRoomName();
			}
		} else {
			return I18NManager.getTitles().unknownTextValue();
		}
	}

	public static String getRoomTypeImageURL(final ChatRoomData roomData) {
		if( roomData.isMain() ) {
			return ServerSideAccessManager.getMainRoomImageURL();
		} else {
			if( roomData.isPublic() ) {
				return ServerSideAccessManager.getPublicRoomImageURL();
			} else {
				if( roomData.isProtected() ) {
					return ServerSideAccessManager.getProtectedRoomImageURL();
				} else {
					if( roomData.isPrivate() ) {
						return ServerSideAccessManager.getPrivateRoomImageURL();
					} else {
						return "Unknown Image URL for this room type!";
					}
				}
			}
		}
	}
	
	public static String getRoomShortTipMsg(final ChatRoomData roomData) {
		return I18NManager.getTitles().roomNameFieldTitle() + ": " + ChatRoomData.getRoomName( roomData );
	}
	
	public static String getRoomType( final ChatRoomData roomData ) {
		String roomType;
		if( roomData.isPublic() ) {
			roomType = I18NManager.getTitles().roomTypeNamePublic();
		} else {
			if( roomData.isProtected() ) {
				roomType = I18NManager.getTitles().roomTypeNameProtected();
			} else {
				if( roomData.isPrivate() ) {
					roomType = I18NManager.getTitles().roomTypeNamePrivate();
				} else {
					roomType = "Unknown";
				}
			}
		}
		return roomType;
	}
	
	public static String getRoomLongTipMsg(final ChatRoomData roomData) {
		return I18NManager.getTitles().roomNameFieldTitle() + ": " + ChatRoomData.getRoomName( roomData ) + ", " +
				I18NManager.getTitles().accessOrTypeColumnTitle() + ": " + getRoomType( roomData );
	}

	public static String getRoomTypeImage(final int type, boolean isMain, final Image roomImage) {
		String roomImageURL = "";
		String roomTypeName = "";
		if( isMain ) {
			roomImageURL = ServerSideAccessManager.getMainRoomImageURL();
			roomTypeName = I18NManager.getTitles().roomTypeNamePublic();
		} else {
			switch( type ){
				default:
				case PUBLIC_ROOM_TYPE :
					roomImageURL = ServerSideAccessManager.getPublicRoomImageURL();
					roomTypeName = I18NManager.getTitles().roomTypeNamePublic();
					break;
				case PROTECTED_ROOM_TYPE :
					roomImageURL = ServerSideAccessManager.getProtectedRoomImageURL();
					roomTypeName = I18NManager.getTitles().roomTypeNameProtected();
					break;
				case PRIVATE_ROOM_TYPE : 
					roomImageURL = ServerSideAccessManager.getPrivateRoomImageURL();
					roomTypeName = I18NManager.getTitles().roomTypeNamePrivate();
					break;
			}
		}
		
		//Just in case, we check that the room image is provided
		if( roomImage != null ) {
			roomImage.setUrl( roomImageURL );
			roomImage.setTitle( I18NManager.getTitles().accessOrTypeColumnTitle() + ": " + roomTypeName );
			roomImage.setStyleName( CommonResourcesContainer.CHAT_ROOM_IMAGE_STYLE_NAME );
		}
		
		return roomTypeName;
	}
}
