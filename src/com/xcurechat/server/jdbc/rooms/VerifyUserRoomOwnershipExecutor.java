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
package com.xcurechat.server.jdbc.rooms;

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
 * This class allows to verify that the user is the room's owner.
 * If it is not then throws InternalSiteException(InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR)
 */
public class VerifyUserRoomOwnershipExecutor extends QueryExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( VerifyUserRoomOwnershipExecutor.class );
	
	private final int userID;
	private final int roomID;
	
	/**
	 * A simple constructor
	 * @param userID the unique user ID
	 * @param roomID the room Id we want to check the ownership of the user
	 */
	public VerifyUserRoomOwnershipExecutor( final int userID, final int roomID ){
		this.userID = userID;
		this.roomID = roomID;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT " + RID_FIELD_NAME_ROOMS_TABLE + " FROM " + ROOMS_TABLE +
									" WHERE " + UID_FIELD_NAME_ROOMS_TABLE + "=?" +
									" AND " + RID_FIELD_NAME_ROOMS_TABLE + "=?";
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int index = 1;
		pstmt.setInt( index++, userID );
		pstmt.setInt( index++, roomID );
	}

	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	@Override
	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		if( resultSet.first() ) {
			logger.debug("User " + userID + " owns the room " + roomID);
		} else {
			logger.warn("User " + userID + " DOES NOT own the room " + roomID);
			throw new InternalSiteException( InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR );
		}
	}
}
