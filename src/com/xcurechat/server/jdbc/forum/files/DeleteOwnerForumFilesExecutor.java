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

package com.xcurechat.server.jdbc.forum.files;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.MainUserData;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor class is responsible for substituting the given owner ID to the DELETED sender.
 * I.e. we do not want to remove he forum files of the deleted user, but just mark the owner as unknown.
 */
public class DeleteOwnerForumFilesExecutor extends QueryExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( DeleteOwnerForumFilesExecutor.class );
	
	private final int ownerID;
	
	public DeleteOwnerForumFilesExecutor( final int ownerID ){
		this.ownerID = ownerID;
	}
	
	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		String updateQuery = "UPDATE " + FORUM_FILES_TABLE + " SET " +
							OWNER_ID_FIELD_NAME_FORUM_FILES_TABLE + "= ( SELECT " + UID_FIELD_NAME_USERS_TABLE + " FROM " +
							USERS_TABLE + " WHERE " + TYPE_FIELD_NAME_USERS_TABLE + "=" + MainUserData.DELETED_USER_TYPE + " ) WHERE "+
							OWNER_ID_FIELD_NAME_FORUM_FILES_TABLE + "=?";
		
		return connection.prepareStatement( updateQuery );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		pstmt.setInt( counter++, ownerID );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		logger.info("Deleting owner " + ownerID + " who owns " + pstmt.executeUpdate() + " forum message files" );
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
