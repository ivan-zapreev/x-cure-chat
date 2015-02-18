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
 * The user data objects package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.data;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.xcurechat.client.rpc.exceptions.IncorrectRoomDataException;

/**
 * @author zapreevis
 * This class contains data needed for carrying the user access rights for a room
 */
public class RoomUserAccessData implements IsSerializable {
	//Room access entry ID
	private int raid;
	//Room ID
    private int rid;
    //User ID
    private int uid;
    //User login name
    private String login;
	//True if the user access entry is a system entry, otherwise false
	private boolean is_system;
    //True if the user can read from the room, otherwise false
	private boolean is_read;
	//True if the user can read all private messages from the room, otherwise false
	private boolean is_read_all;
	//The date-time when the read all access to the room expires
	private Date read_all_expires;
	//Stores true if the user read-all access is expired
	private boolean isReadAllExpired = false;
	//True if the user can write to the room, otherwise false
	private boolean is_write;
	//The room's "read all" duration perioud
	private int readAllDurationTimeHours = ChatRoomData.UNKNOWN_HOURS_DURATION; 
	
	public RoomUserAccessData() {
	}
	
	/**
	 * @return true if this is not a system access right and the user can read from the room
	 */
	public boolean isVisibleUser() {
		return ! is_system && ( is_read || ( is_read_all && !isReadAllExpired ) );
	}
	
	public int getRAID() {
		return raid;
	}
	
	/**
	 * Sets the id of the room-user access entry 
	 * @param raid
	 */
	public void setRAID(final int raid) {
		this.raid = raid;
	}
		
	public int getRID() {
		return rid;
	}
	
	public void setRID(final int rid) {
		this.rid = rid;
	}
	
	public int getUID() {
		return uid;
	}
	
	public void setUID(final int uid) {
		this.uid = uid;
	}
	
	public String getUserLoginName() {
		return login;
	}
	
	public void setUserLoginName(final String login) {
		this.login = login;
	}
	
	public boolean isSystem() {
		return is_system;		
	}
	
	public void setSystem(final boolean is_system) {
		this.is_system = is_system;		
	}
	
	public boolean isRead() {
		return is_read;
	}
	
	public void setRead(final boolean is_read) {
		this.is_read = is_read;
	}
	
	
	public boolean isReadAll() {
		return is_read_all;
	}
	
	public void setReadAll(final boolean is_read_all) {
		this.is_read_all = is_read_all;
	}
	
	public Date getReadAllExpires() {
		return read_all_expires;
	}
	
	public void setReadAllExpires(final Date read_all_expires) {
		this.read_all_expires = read_all_expires;
		isReadAllExpired = ( read_all_expires != null ? read_all_expires.before( new Date() ) : false );
	}
	
	public boolean isReadAllExpired() {
		return isReadAllExpired;
	}
	
	public boolean isWrite() {
		return is_write;
	}
	
	public void setWrite( final boolean is_write ) {
		this.is_write = is_write;
	}
	
	public int getReadAllDurationTimeHours() {
		return readAllDurationTimeHours;
	}
	
	public void setReadAllDurationTimeHours(int readAllDurationTimeHours) {
		this.readAllDurationTimeHours = readAllDurationTimeHours;
	}
	
	public void validate() throws IncorrectRoomDataException {
		IncorrectRoomDataException exception = new IncorrectRoomDataException();
		//Check for the valid duration time
		ChatRoomData.validateDuration( true, readAllDurationTimeHours, exception);
		//Check that the room access rights are set,  
		//i.e. at least some access should be enabled
		if( !isWrite() && !isRead() && !isReadAll() ) {
			exception.addErrorCode( IncorrectRoomDataException.ROOM_USER_ACCESS_IS_EMPTY_ERR );
		}
		//Check if there are error, if yes, then throw an exception
		if( exception.containsErrors() ){
			throw exception;
		}
	}
}
