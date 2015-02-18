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

package com.xcurechat.server.jdbc.chat.images;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This class is responsible for deleting the public chat message files that have the given md5 sum and do not have the given ID
 */
public class RemoveOtherFilesWithMD5Executor extends QueryExecutor<Void> {
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( RemoveOtherFilesWithMD5Executor.class );
	
	private final int fileID;
	private final String md5sum;
	
	/**
	 * Delete the files which are attached to public chat messages and have the given md5sum and the fileID other than the given one
	 * @param fileID the id of the file that we do not want to delete
	 * @param md5Sum the md5sum 
	 */
	public RemoveOtherFilesWithMD5Executor( final String md5sum, final int fileID ){
		this.fileID = fileID;
		this.md5sum = md5sum;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String updateQuery = "DELETE FROM " + CHAT_FILES_TABLE + " WHERE " +
									FILE_ID_FIELD_NAME_CHAT_FILES_TABLE + "!=?" + 
									" AND " + MD5_SUM_CHAT_FILES_TABLE + "=?" +
									" AND " + IS_PUBLIC_MESSAGE_FILE_CHAT_FILES_TABLE + "=true";
		return connection.prepareStatement( updateQuery );
	}
	
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		pstmt.setInt( counter++, fileID );
		pstmt.setString( counter++, md5sum );
	}
	
	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		final int deletedMsgsCount = pstmt.executeUpdate();
		logger.debug("We have removed " + deletedMsgsCount + " public chat message files with md5sum " + md5sum + " and fileID != " + fileID );
		return null;
	}

	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		//DO NOTHING
	}

}
