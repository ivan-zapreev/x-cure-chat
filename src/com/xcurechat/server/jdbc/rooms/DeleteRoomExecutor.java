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

package com.xcurechat.server.jdbc.rooms;

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
 * This class is responsible for deleting one or more rooms, with all of it's member, if any
 */
public class DeleteRoomExecutor extends QueryExecutor<Void> {

	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( DeleteRoomExecutor.class );
	
	private final int userID;
	private final List<Integer> roomIDS;

	/**
	 * Deletes the rooms belonging to the user
	 * @param userID the owner of the room
	 * @param roomIDS the room's to be deleted
	 */
	public DeleteRoomExecutor( final int userID, final List<Integer> roomIDS ){
		this.userID = userID;
		this.roomIDS = roomIDS;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		String deleteQuery = "DELETE FROM " + ROOMS_TABLE + " WHERE " +
									UID_FIELD_NAME_ROOMS_TABLE + " = ? AND " + 
									RID_FIELD_NAME_ROOMS_TABLE + createINQuerySet( roomIDS );
		logger.debug("The delete query for the rooms is: "+deleteQuery);
		return connection.prepareStatement( deleteQuery );
	}
	
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		pstmt.setInt( counter++, userID );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		final int count = pstmt.executeUpdate();
		if( count == 0) {
			throw new InternalSiteException("Unable to delete the rooms, for user "+userID+"!");
		} else {
			logger.debug("Deleted "+count+" room(s) of user "+userID);
		}
		return null;
	}

	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		//DO NOTHING
	}

}
