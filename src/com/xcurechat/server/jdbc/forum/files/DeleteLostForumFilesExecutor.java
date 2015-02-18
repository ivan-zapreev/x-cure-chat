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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ShortForumMessageData;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This class is responsible for deleting forum files that are more than N
 * hours old and have the messageID set to be the invisible root forum message.
 * These files are clearly lost and are not attached to any message.
 */
public class DeleteLostForumFilesExecutor extends QueryExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( DeleteLostForumFilesExecutor.class );
	
	//How many hours at least should be the deleted files old
	public static final int AT_LEAST_N_HOURS_OLD = 8;
	
	public DeleteLostForumFilesExecutor( ) {
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		String deleteQuery = "DELETE FROM " + FORUM_FILES_TABLE + " WHERE " +
							  MESSAGE_ID_FIELD_NAME_FORUM_FILES_TABLE + "=? AND " + 
							  UPLOAD_DATE_FIELD_NAME_FORUM_FILES_TABLE + " <= DATE_SUB( NOW(), INTERVAL ? HOUR )";
		logger.debug("The lost forum-message files deletion query is: "+deleteQuery);
		return connection.prepareStatement( deleteQuery );
	}
	
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		pstmt.setInt( counter++, ShortForumMessageData.ROOT_FORUM_MESSAGE_ID );
		pstmt.setInt( counter++, AT_LEAST_N_HOURS_OLD );
	}
	
	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		final int numberDeletedFiles = pstmt.executeUpdate();
		logger.debug( "We have deleted " + numberDeletedFiles + " lost files from the forum, " +
					  "the files were older than " + AT_LEAST_N_HOURS_OLD + " hours" );
		return null;
	}

	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		//DO NOTHING
	}

}
