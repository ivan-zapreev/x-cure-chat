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
 * This executor class is responsible for marking the chat-message file as public.
 */
public class MarkMessageFileAsPublicExecutor extends QueryExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( MarkMessageFileAsPublicExecutor.class );

	private final int fileID;
	
	/**
	 * The basic constructor
	 * @param fileID the id of the chat message file.
	 */
	public MarkMessageFileAsPublicExecutor( final int fileID ){
		this.fileID = fileID;
	}

	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		String updateQuery = "UPDATE " + CHAT_FILES_TABLE + " SET " +
							IS_PUBLIC_MESSAGE_FILE_CHAT_FILES_TABLE + "=true " +
							" WHERE "+
							FILE_ID_FIELD_NAME_CHAT_FILES_TABLE + "=?";
		
		return connection.prepareStatement( updateQuery );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		pstmt.setInt( counter++, fileID );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		if( pstmt.executeUpdate() == 0 ) {
			logger.warn("Could not mark the chat message file " +  fileID + " as public" );
		}
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
