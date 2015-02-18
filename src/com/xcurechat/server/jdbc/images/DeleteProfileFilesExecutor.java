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

package com.xcurechat.server.jdbc.images;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This class is responsible for deleting one user-profile files
 */
public class DeleteProfileFilesExecutor extends QueryExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( DeleteProfileFilesExecutor.class );
	
	private final int userID;
	private final List<Integer> fileIDs;

	public DeleteProfileFilesExecutor( final int userID, final List<Integer> fileIDs ){
		this.userID = userID;
		this.fileIDs = fileIDs;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String updateQuery = "DELETE FROM " + PROFILE_FILES_TABLE + " WHERE " +
									OWNER_ID_PROFILE_FILES_TABLE + "=? AND " +
									FILE_ID_FIELD_PROFILE_FILES_TABLE + createINQuerySet( fileIDs );
		return connection.prepareStatement( updateQuery );
	}
	
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		pstmt.setInt( counter++, userID );
	}
	
	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		final int numberDeletedFiles = pstmt.executeUpdate();
		if( numberDeletedFiles != fileIDs.size() ) {
			logger.warn( "The removal of profile files by user " + userID +
						 " was not quite successful, deleted " + numberDeletedFiles +
						 " files out of " + fileIDs.size() );
		} else {
			logger.debug( "The removal of profile files by user " + userID +
					 	  " was successful, deleted " + numberDeletedFiles + " files" );
		}
		return null;
	}

	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		//DO NOTHING
	}

}
