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
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ForumMessageData;

import com.xcurechat.client.data.ShortFileDescriptor;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor allows to retrieve forum files attache to the provided forum messages
 */
public class SelectMessageFilesExecutor extends QueryExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( SelectMessageFilesExecutor.class );
			
	//The mapping of the message ids to the messages for the messages we want to get the attached files for
	private final LinkedHashMap<Integer,ForumMessageData> forumMessagesById;
	
	/**
	 * Allows to set the attached files for the provided forum messages
	 * @param forumMessagesById the forum message Ids mapped to the forum messages
	 */
	public SelectMessageFilesExecutor( final LinkedHashMap<Integer,ForumMessageData> forumMessagesById ) {
		this.forumMessagesById = forumMessagesById;
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.server.jdbc.QueryExecutor#prepareStatement(java.sql.Connection)
	 */
	@Override
	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT " + MESSAGE_ID_FIELD_NAME_FORUM_FILES_TABLE + ", " +
												FILE_ID_FIELD_NAME_FORUM_FILES_TABLE + ", " +
												MIME_TYPE_FIELD_NAME_FORUM_FILES_TABLE + ", " +
												FILE_NAME_FIELD_NAME_FORUM_FILES_TABLE + ", " +
												IMG_WIDTH_PIXELS_NAME_FORUM_FILES_TABLE + ", " +
												IMG_HEIGHT_PIXELS_NAME_FORUM_FILES_TABLE + " " +
									"FROM " + FORUM_FILES_TABLE + " WHERE " +
												MESSAGE_ID_FIELD_NAME_FORUM_FILES_TABLE +
												createINQuerySet( forumMessagesById.keySet() ) +
									" ORDER BY " + MIME_TYPE_FIELD_NAME_FORUM_FILES_TABLE + " DESC";

		logger.debug( "The new forum-message files retrieval query is " + selectQuery );
		return connection.prepareStatement( selectQuery );
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.server.jdbc.QueryExecutor#bindParameters(java.sql.PreparedStatement)
	 */
	@Override
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		//NOTE: Do nothing
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.server.jdbc.QueryExecutor#executeQuery(java.sql.PreparedStatement, java.lang.Object)
	 */
	@Override
	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.server.jdbc.QueryExecutor#processResultSet(java.sql.ResultSet, java.lang.Object)
	 */
	@Override
	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		while( resultSet.next() ) {
			int messageID = resultSet.getInt( MESSAGE_ID_FIELD_NAME_FORUM_FILES_TABLE );
			ForumMessageData message = forumMessagesById.get( messageID );
			if( message != null ) {
				ShortFileDescriptor fileDesc = new ShortFileDescriptor();
				fileDesc.fileID = resultSet.getInt( FILE_ID_FIELD_NAME_FORUM_FILES_TABLE );
				fileDesc.fileName = resultSet.getString( FILE_NAME_FIELD_NAME_FORUM_FILES_TABLE );
				fileDesc.mimeType = resultSet.getString( MIME_TYPE_FIELD_NAME_FORUM_FILES_TABLE );
				fileDesc.widthPixels = resultSet.getInt( IMG_WIDTH_PIXELS_NAME_FORUM_FILES_TABLE );
				fileDesc.heightPixels = resultSet.getInt( IMG_HEIGHT_PIXELS_NAME_FORUM_FILES_TABLE );
				message.attachedFileIds.add( fileDesc );
			} else {
				logger.error( "Somehow we have files for the forum message with ID " + messageID + " but we did not ask for them!" );
			}
		}
	}

}
