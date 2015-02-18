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

import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This class is responsible for deleting one or more room users
 */
public class DeleteRoomUsersExecutor extends QueryExecutor<Void> {

	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( DeleteRoomUsersExecutor.class );
	
	private final int roomID;
	private final List<Integer> roomAccessIDS;

	/**
	 * Deletes the rooms belonging to the user
	 * @param roomID the room's ID
	 * @param roomAccessIDS the list of ID's of the users access entries we want to remove from the room
	 */
	public DeleteRoomUsersExecutor( final int roomID, final List<Integer> roomAccessIDS ){
		this.roomID = roomID;
		this.roomAccessIDS = roomAccessIDS;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		String deleteQuery = "DELETE FROM " + ROOM_ACCESS_TABLE + " WHERE " +
									RID_FIELD_NAME_ROOM_ACCESS_TABLE + " = ? AND " + 
									RAID_FIELD_NAME_ROOM_ACCESS_TABLE + createINQuerySet( roomAccessIDS );
		logger.debug("The delete query for the room users is: "+deleteQuery);
		return connection.prepareStatement( deleteQuery );
	}
	
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		pstmt.setInt( counter++, roomID );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		final int count = pstmt.executeUpdate();
		if( count == 0) {
			throw new InternalSiteException("Unable to delete the room users, for room "+roomID+"!");
		} else {
			logger.debug("Deleted "+count+" user access entrie(s) in room "+roomID);
		}
		return null;
	}

	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		//DO NOTHING
	}

}
