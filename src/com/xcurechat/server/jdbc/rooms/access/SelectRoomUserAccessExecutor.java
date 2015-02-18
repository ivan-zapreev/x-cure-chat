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
 * The server-side RPC package, managing DB queries.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.server.jdbc.rooms.access;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.RoomUserAccessData;
import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor retrieves the user profile data from the database
 */
public class SelectRoomUserAccessExecutor extends QueryExecutor<Void> {

	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( SelectRoomUserAccessExecutor.class );
	
	private final OnePageViewData<RoomUserAccessData> usersAccessData;
	private final int roomID;
	private final boolean isAdmin; 	
	private final int size;
	private final int offset;
	
	public SelectRoomUserAccessExecutor( final OnePageViewData<RoomUserAccessData> usersAccessData, final int roomID,
										final boolean isAdmin, final int offset, final int size ){
		this.usersAccessData = usersAccessData;
		this.roomID = roomID;
		this.isAdmin = isAdmin; 
		this.size = size;
		this.offset = offset;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		String selectQuery = "SELECT * FROM " + ROOM_ACCESS_TABLE + " WHERE " +
									RID_FIELD_NAME_ROOM_ACCESS_TABLE+" =?";
		if( !isAdmin ){
			selectQuery += " AND " + IS_SYSTEM_FIELD_NAME_ROOM_ACCESS_TABLE + " =false";  
		}
		selectQuery += " ORDER BY "+ LOGIN_FIELD_NAME_ROOM_ACCESS_TABLE +" LIMIT ? OFFSET ?";
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		pstmt.setInt( 1, roomID );
		pstmt.setInt( 2, size );
		pstmt.setInt( 3, offset );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		//Store the offset in the result
		usersAccessData.offset = offset;
		
		//Fill in the wesult with the user data
		List<RoomUserAccessData> list = new ArrayList<RoomUserAccessData>();
		while( resultSet.next() ){
			RoomUserAccessData userAccessData = new RoomUserAccessData();		
			userAccessData.setRAID( resultSet.getInt( RAID_FIELD_NAME_ROOM_ACCESS_TABLE ) );
			userAccessData.setRID( resultSet.getInt( RID_FIELD_NAME_ROOM_ACCESS_TABLE ) );
			userAccessData.setUID( resultSet.getInt( UID_FIELD_NAME_ROOM_ACCESS_TABLE ) );
			
			userAccessData.setUserLoginName( resultSet.getString( LOGIN_FIELD_NAME_ROOM_ACCESS_TABLE ) );
			userAccessData.setSystem( resultSet.getBoolean( IS_SYSTEM_FIELD_NAME_ROOM_ACCESS_TABLE ) );
			userAccessData.setRead( resultSet.getBoolean( IS_READ_FIELD_NAME_ROOM_ACCESS_TABLE ) );
			userAccessData.setReadAll( resultSet.getBoolean( IS_READ_ALL_FIELD_NAME_ROOM_ACCESS_TABLE ) );
			
			userAccessData.setReadAllExpires( QueryExecutor.getTime( resultSet, READ_ALL_EXP_DATE_FIELD_NAME_ROOM_ACCESS_TABLE ) );
			
			userAccessData.setWrite( resultSet.getBoolean( IS_WRITE_FIELD_NAME_ROOM_ACCESS_TABLE ) );
			list.add(userAccessData);
			logger.debug("The DB reports that the user " + userAccessData.getUID() + " at offset " + offset + " is allowed to access the room "+roomID);
		}
		
		if( ! list.isEmpty() ){
			usersAccessData.entries = list;
			logger.debug( "Retrieved the users allowed to access room '" + roomID +
							"', size = " + (usersAccessData.entries != null ? ""+usersAccessData.entries.size() : "null") );
		} else {
			logger.warn("There is no user for room'"+roomID+"' at offset "+offset);
			usersAccessData.entries = null;
		}
	}

}

