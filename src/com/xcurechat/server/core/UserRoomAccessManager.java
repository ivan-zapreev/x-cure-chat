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
package com.xcurechat.server.core;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.RoomUserAccessData;

/**
 * @author zapreevis
 * This class stores the general room access information for the user,
 * as combined from the fact that the user is the room's owner, and
 * the room access rights, the regular and the system one.
 * WARNING: This class has synchronized methods only!
 */
public class UserRoomAccessManager {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( UserRoomAccessManager.class );
	
	//The time out, after which if user does not get the room's data we decide that he has left the room.
	public static final long USER_IDLE_TIME_OUT_MILLISEC = 40000;
	
	private boolean isRead = false;
	private boolean isReadAll = false;
	private Date readAllExpires = null;
	private boolean isWrite = false;
	private final boolean isRoomOwner;
	private Map<Integer, RoomUserAccessData> accessIDToAccessObj = new HashMap<Integer, RoomUserAccessData>();
	//The user ID of the user we store the access rights for
	private final int userID;
	//The chat room to which this object belongs, i.e. defined access rights for a user in the room
	private final ActiveChatRoom parentRoom;
	//The last time the person tried to access the room
	private long lastAccessTime = System.currentTimeMillis();
	
	public UserRoomAccessManager( final int userID, final boolean isRoomOwner, final ActiveChatRoom parentRoom ){
		this.userID = userID;
		this.parentRoom = parentRoom;
		this.isRoomOwner = isRoomOwner;
	}
	
	/**
	 * @return the ID of the corresponding user
	 */
	public int getUserID() {
		return userID;
	}
	
	/**
	 * Allows to check for the user being IDLE, if he is then he has to be removed from the room
	 * @return true if the user is Idle for more than USER_IDLE_TIME_OUT_MILLISEC
	 */
	public synchronized boolean isIdle(){
		return ( lastAccessTime < ( System.currentTimeMillis() - USER_IDLE_TIME_OUT_MILLISEC ) );
	}
	
	/**
	 * Allows to check if the user has access to the room.
	 * Also updates the last room access time
	 * @return true if he has read or read all access or if the room is public or the user is the room's owner 
	 */
	public synchronized boolean hasAccessToTheRoom() {
		lastAccessTime = System.currentTimeMillis();
		return ( parentRoom.isPublic() || isRoomOwner ) ||
				( isRead || hasReadAllAccess() );
	}
	
	/**
	 * Allows to check if the user is allowed to write into this room
	 * @return true if the user has write access
	 */
	public synchronized boolean hasWriteAcces() {
		return isWrite;
	}
	
	/**
	 * Allows to check if the user has a valid read all access
	 * @return true if the user has a currently valid read all access
	 */
	public synchronized boolean hasReadAllAccess() {
		return ( isReadAll && ( ( readAllExpires == null ) || (new Date()).before( readAllExpires ) ) );
	}
	
	/**
	 * Allows to re-process the the holder data based on the stored user-room access rights
	 */
	protected synchronized void reprocessAccessRights( ) {	
		//Rre-process the user-access rights information
		Iterator<RoomUserAccessData> iter = accessIDToAccessObj.values().iterator();
		//If the user is the room's owner then he can always write and read
		isWrite = ( isRoomOwner || parentRoom.isPublic() );
		isRead = ( isRoomOwner || parentRoom.isPublic() );
		isReadAll = false; readAllExpires = null;
		//If the room is public or the user is the room's owner then the user is always visible 
		boolean isVisibleUser = ( isRoomOwner || parentRoom.isPublic() );
		while( iter.hasNext() ) {
			final RoomUserAccessData access = iter.next();
			isWrite = isWrite || access.isWrite() && !access.isSystem(); //The system access right should not allow to write  
			isRead  = isRead  || access.isRead();
			isVisibleUser = isVisibleUser || access.isVisibleUser();
			//Unless this is a system property, the read all does not matter
			if( access.isSystem() ) {
				//If either the read All is not enabled yet or it is
				//enabled but has an expiration Date set 
				if( !( isReadAll && ( readAllExpires == null ) ) && access.isReadAll() ) {
					isReadAll = true;
					if( readAllExpires == null ) {
						readAllExpires = access.getReadAllExpires();
					} else {
						final Date newReadAllExpires = access.getReadAllExpires();
						if( newReadAllExpires != null ) {
							if( newReadAllExpires.after( readAllExpires ) ) {
								readAllExpires = newReadAllExpires; 
							}
						} else {
							readAllExpires = null;
						}
					}
				}
			}
		}
		
		if( isVisibleUser ) {
			parentRoom.addRoomUserToVisibleUsers( userID );
		} else {
			parentRoom.removeRoomUserFromVisibleUsers( userID );
		}
	}
	
	/**
	 * Allows to update the user room access rights and then re-process the the holder data
	 * @param accesses the newl room access object or null if we only need to recompute the
	 * access rights from the existing ones
	 */
	public synchronized void addUserAccessRights( final List<RoomUserAccessData> accesses ) {
		//Update the stored access objects with the new ones, if any
		if( accesses != null ) {
			Iterator<RoomUserAccessData> iter = accesses.iterator(); 
			while( iter.hasNext() ) {
				final RoomUserAccessData access = iter.next();
				final Integer roomAccessID = access.getRAID();
				accessIDToAccessObj.put( roomAccessID, access );
				parentRoom.addRoomUserAccessToUserIDMapping( roomAccessID, userID );
			}
		}
		
		//Rre-process the user-access rights information
		reprocessAccessRights( );
	}
	
	/**
	 * This method allows to delete the user access ID and then to 
	 * @param userAccessID
	 */
	public synchronized void deleteUserAccess( final int userAccessID ) {
		RoomUserAccessData accessData = accessIDToAccessObj.remove( userAccessID );
		if( accessData != null ) {
			//Remove the access ID fro the mapping
			parentRoom.removeRoomUserAccessToUserIDMapping( userAccessID );
			//Rre-process the user-access rights information
			reprocessAccessRights( );
		} else {
			logger.warn( " The user access " + userAccessID + " for user " + userID +
							" was not found in the active room " ); 
		}
	}
	
	/**
	 * Allows to update/add the room user access right
	 * @param userAccess the updated user-room access right
	 */
	public synchronized void updateAddUserAccess( final RoomUserAccessData userAccess ) {
		//Update the access right in the hash map and register
		//the user access in the parent active room
		final Integer roomAccessID = userAccess.getRAID();
		accessIDToAccessObj.put( roomAccessID, userAccess );
		parentRoom.addRoomUserAccessToUserIDMapping( roomAccessID, userID );
		
		//Rre-process the user-access rights information
		reprocessAccessRights( );
	}
	
	/**
	 * This method should be called when the user is removed from the room.
	 * Then we have to unregister all user access rights and remove the user
	 * from the list of visible room users.
	 * NOTE: There is not need for doing the local cleaning
	 */
	public synchronized void unregisterAllUserAccessRights() {
		//Remove the user from the list of visible users
		parentRoom.removeRoomUserFromVisibleUsers( userID );
		//Unregister the user access rights
		Iterator<RoomUserAccessData> iter = accessIDToAccessObj.values().iterator();
		while( iter.hasNext() ) {
			parentRoom.removeRoomUserAccessToUserIDMapping( iter.next().getRAID() );
		}
	}
}
