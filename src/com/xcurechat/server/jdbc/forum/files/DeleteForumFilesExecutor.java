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

package com.xcurechat.server.jdbc.forum.files;

import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This class is responsible for deleting forum files
 */
public class DeleteForumFilesExecutor extends QueryExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( DeleteForumFilesExecutor.class );
	
	//The file owner in case they are not deleted by an admin
	private final int ownerID;
	//If true then the files are deleted by an admin
	private final boolean isByAdmin;
	//Is not used, can be helpful if the images for 
	//different rooms are stored in different tables
	private final List<Integer> fileIDs;

	/**
	 * The simple constructor
	 * @param isByAdmin if true then the files are deleted regardless their owner
	 * @param ownerID the owner of the files for the case of isByAdmin==false
	 * @param fileIDs the set of file ids to be deleted
	 */
	public DeleteForumFilesExecutor( final boolean isByAdmin, final int ownerID, final List<Integer> fileIDs ) {
		this.ownerID = ownerID;
		this.fileIDs = fileIDs;
		this.isByAdmin = isByAdmin;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		String updateQuery = "DELETE FROM " + FORUM_FILES_TABLE + " WHERE " +
							 ( isByAdmin ? "" : OWNER_ID_FIELD_NAME_FORUM_FILES_TABLE + "=? AND " ) + 
							  FILE_ID_FIELD_NAME_FORUM_FILES_TABLE + createINQuerySet( fileIDs );
		logger.debug("The forum-message files deletion query is: "+updateQuery);
		return connection.prepareStatement( updateQuery );
	}
	
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		if( ! isByAdmin ) {
			pstmt.setInt( counter++, ownerID );
		}
	}
	
	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		final int numberDeletedFiles = pstmt.executeUpdate();
		if( numberDeletedFiles != fileIDs.size() ) {
			logger.warn( "The removal of forum-message files by user " + ownerID +
						 " was not quite successful, deleted " + numberDeletedFiles +
						 " files out of " + fileIDs.size() );
		} else {
			logger.debug( "The removal of forum-message files by user " + ownerID +
					 	  " was successful, deleted " + numberDeletedFiles + " files" );
		}
		return null;
	}

	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		//DO NOTHING
	}

}
