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
import java.util.List;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.RoomUserAccessData;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor tries to retrieve the (system) user room access, if any
 */
public class SelectOneUserRoomAccessExecutor extends QueryExecutor<List<RoomUserAccessData>> {

	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( SelectOneUserRoomAccessExecutor.class );
	
	private final int roomID;
	private final int userID;
	private final boolean isSystem;
	private final boolean isAll;
	
	/**
	 * The basic constructor
	 * @param roomID the room id
	 * @param userID the user id
	 * @param isSystem if true then we selecte user's system room access entries, otherwise only non-system
	 * @param isAll if true then we select all sorts of user room access entries
	 */
	public SelectOneUserRoomAccessExecutor( final int roomID, final int userID,
											final boolean isSystem, final boolean isAll ){
		this.roomID = roomID;
		this.userID = userID;
		this.isSystem = isSystem;
		this.isAll = isAll;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		String selectQuery = "SELECT * FROM " + ROOM_ACCESS_TABLE + " WHERE " +
									RID_FIELD_NAME_ROOM_ACCESS_TABLE + "=? AND "+
									UID_FIELD_NAME_ROOM_ACCESS_TABLE + "=? ";
		if( !isAll ) {
			selectQuery += " AND " + IS_SYSTEM_FIELD_NAME_ROOM_ACCESS_TABLE + "="+ (isSystem ?"true":"false");
		}
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		pstmt.setInt( 1, roomID );
		pstmt.setInt( 2, userID );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, List<RoomUserAccessData> result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	public void processResultSet(ResultSet resultSet, List<RoomUserAccessData> list) throws SQLException, SiteException {
		//Fill in the wesult with the user data
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
		}
		
		if( ! list.isEmpty() ){
			//If there is a system room-user access
			logger.debug("Found (isAll=" + isAll+", isSystem=" + isSystem +") "+list.size()+" user-room access entries for room="+roomID+" and user="+userID);
		} else {
			//If there is no system room access
			logger.debug("There is NO (isAll=" + isAll+", isSystem=" + isSystem +") "+"user-room access for room="+roomID+" and user="+userID);
		}
	}
}

