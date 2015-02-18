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
import java.util.ArrayList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.xcurechat.client.data.ShortFileDescriptor;

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor class is responsible for updating user forum-files with the proper messageID.
 * I.e. the messageID is set to be the message to which this files belong.
 */
public class SetMessageIDExecutor extends QueryExecutor<Void> {
	
	private final int messageID;
	private final int messageSender;
	private final List<Integer> messageFileIds = new ArrayList<Integer>();
	
	/**
	 * The simple consructor
	 * @param messageID the id of the message to which the files should belong
	 * @param messageSender the id of the user who sends/edits the message and who should also be the files owner
	 * @param messageFiles the list of file descriptor for the files which we update
	 */
	public SetMessageIDExecutor( final int messageID, final int messageSender, List<ShortFileDescriptor> messageFiles ){
		this.messageID = messageID;
		this.messageSender = messageSender;
		
		//Collect the message file IDs
		for( ShortFileDescriptor fileDesc : messageFiles ) {
			messageFileIds.add( fileDesc.fileID );
		}
	}

	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		final String updateQuery = "UPDATE " + FORUM_FILES_TABLE + " SET " +
									MESSAGE_ID_FIELD_NAME_FORUM_FILES_TABLE + "=? WHERE " +
									OWNER_ID_FIELD_NAME_FORUM_FILES_TABLE + "=? AND " +
									FILE_ID_FIELD_NAME_FORUM_FILES_TABLE + createINQuerySet(messageFileIds);
		
		return connection.prepareStatement( updateQuery );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		pstmt.setInt( counter++, messageID );
		pstmt.setInt( counter++, messageSender );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		pstmt.executeUpdate();
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
