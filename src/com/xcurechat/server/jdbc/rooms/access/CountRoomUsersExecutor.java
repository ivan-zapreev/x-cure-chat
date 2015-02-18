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
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.server.jdbc.rooms.access;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.RoomUserAccessData;
import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This function counts the total number of the room's users
 */
public class CountRoomUsersExecutor extends QueryExecutor<OnePageViewData<RoomUserAccessData>> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( CountRoomUsersExecutor.class );
	
	private final int roomID;
	private final boolean isAdmin; 
	
	public CountRoomUsersExecutor( final int roomID, final boolean isAdmin ){
		this.roomID = roomID;
		this.isAdmin = isAdmin;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		String selectQuery = "SELECT COUNT(*) FROM " + ROOM_ACCESS_TABLE + " WHERE " +
									RID_FIELD_NAME_ROOM_ACCESS_TABLE + "=?";
		if( !isAdmin ){
			selectQuery += " AND " + IS_SYSTEM_FIELD_NAME_ROOM_ACCESS_TABLE + " =false";  
		}
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		pstmt.setInt( 1, roomID );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, OnePageViewData<RoomUserAccessData> result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	public void processResultSet(ResultSet resultSet, OnePageViewData<RoomUserAccessData> dataObj) throws SQLException, SiteException {
		if( resultSet.first() ){
			dataObj.total_size = resultSet.getInt( 1 );
		} else {
			logger.warn("Unable to retrieve the number room's users for romo'"+roomID+"'");
			dataObj.total_size = 0;
		}
	}
}
