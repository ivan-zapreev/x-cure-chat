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

import com.xcurechat.client.data.MessageFileData;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor checks that a user with the given login and password exists
 * TODO: Currently it is a simple select, but we need to be sure that the used
 * has a right to view this file
 */
public class SelectChatFileExecutor extends QueryExecutor<MessageFileData> {
	
	//Currently not used, might become handy when we
	//store images of different rooms in different tables
	@SuppressWarnings("unused")
	private final int roomID;
	//The id of the image
	private final int fileID;
	//The id of the user who owns the image
	private final int ownerID;
	//True if we need a thumbnail false if we need a whole image
	private final boolean isThumbnail;

	/**
	 * Simply retrieves the chat-message file by its id,
	 * either the entire file or the full one, also the
	 * chat-message file should belong to the given room.
	 * The method makes sure that this file is owned by
	 * the provided owner. If not then we get no file
	 * @param roomID the id of the room the chat-message file belongs to
	 * @param fileID the id of the chat-message file
	 * @param ownerID the id of the user who owns the file
	 * @param isThumbnail true if we need a thumbnail, otherwise false
	 */
	public SelectChatFileExecutor( final int roomID, final int fileID,
										   final int ownerID, final boolean isThumbnail ){
		this.roomID = roomID;
		this.fileID = fileID;
		this.ownerID = ownerID;
		this.isThumbnail = isThumbnail;
	}
	
	/**
	 * Simply retrieves the chat-message file by its id,
	 * either the entire file or the full one, also the
	 * chat-message file should belong to the given room
	 * @param roomID the id of the room the chat-message file belongs to
	 * @param fileID the id of the chat-message file
	 * @param isThumbnail true if we need a thumbnail, otherwise false
	 */
	public SelectChatFileExecutor( final int roomID, final int fileID, final boolean isThumbnail ){
		this(roomID, fileID, ShortUserData.UNKNOWN_UID, isThumbnail);
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT "+ ( isThumbnail ? THUMBNAIL_FIELD_NAME_CHAT_FILES_TABLE : DATA_FIELD_NAME_CHAT_FILES_TABLE ) +
									", " + MIME_TYPE_CHAT_FILES_TABLE + " FROM " + CHAT_FILES_TABLE + " WHERE " +
									FILE_ID_FIELD_NAME_CHAT_FILES_TABLE + "=?" +
									(ownerID != ShortUserData.UNKNOWN_UID ? " AND " + OWNER_ID_FIELD_NAME_CHAT_FILES_TABLE + "=?" : "");
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		pstmt.setInt(counter++, fileID);
		if( ownerID != ShortUserData.UNKNOWN_UID ) {
			pstmt.setInt(counter++, ownerID);
		}
	}

	public ResultSet executeQuery(PreparedStatement pstmt, MessageFileData result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	public void processResultSet(ResultSet resultSet, MessageFileData fileData) throws SQLException, SiteException {
		//Check if there is required image for the given user
		if( resultSet.first() ){
			fileData.mimeType = resultSet.getString( MIME_TYPE_CHAT_FILES_TABLE );
			if( isThumbnail ) {
				fileData.thumbnailData = resultSet.getBytes( THUMBNAIL_FIELD_NAME_CHAT_FILES_TABLE );
			} else {
				fileData.fileData = resultSet.getBytes( DATA_FIELD_NAME_CHAT_FILES_TABLE );
			}
		} else {
			//There are no rows in the result set, the required file was not found
			fileData.thumbnailData = null;
			fileData.fileData = null;
		}
	}
}
