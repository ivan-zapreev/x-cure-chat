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
package com.xcurechat.server.jdbc.messages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ShortPrivateMessageData;
import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This class counts the total number of the messages belonging to the user
 */
public class CountUserMessagesExecutor extends QueryExecutor<OnePageViewData<ShortPrivateMessageData>> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( CountUserMessagesExecutor.class );
	
	private final int userID;
	private final boolean isAll;
	private final boolean isReceived;
	
	public CountUserMessagesExecutor( final int userID, final boolean isAll, final boolean isReceived ){
		this.userID = userID;
		this.isAll = isAll;
		this.isReceived = isReceived;
	}

	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		String selectQuery = "SELECT COUNT("+MSG_ID_FIELD_NAME_MESSAGES_TABLE+") FROM " +
									MESSAGES_TABLE + " WHERE ";
		selectQuery += ( ( isAll || !isReceived ) ? "( " + FROM_UID_FIELD_NAME_MESSAGES_TABLE + "=? AND " +
									FROM_HIDDEN_FIELD_NAME_MESSAGES_TABLE + "=false )" : "" );
		selectQuery += ( isAll ? " OR " : "" );
		selectQuery += ( ( isAll || isReceived ) ? "( " + TO_UID_FIELD_NAME_MESSAGES_TABLE + "=? AND " +
									TO_HIDDEN_FIELD_NAME_MESSAGES_TABLE + "=false )" : "" );
		return connection.prepareStatement( selectQuery );
	}

	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		//There will always be one used ID needed
		pstmt.setInt( counter++, userID );
		//If we get all messages then we need two user IDs
		if( isAll ) {
			pstmt.setInt( counter++, userID );
		}
	}

	public ResultSet executeQuery(PreparedStatement pstmt, OnePageViewData<ShortPrivateMessageData> result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	@Override
	public void processResultSet(ResultSet resultSet, OnePageViewData<ShortPrivateMessageData> dataObj) throws SQLException, SiteException {
		if( resultSet.first() ){
			dataObj.total_size = resultSet.getInt( 1 );
		} else {
			logger.warn("Unable to retrieve the number of messages for user '"+userID+"'");
			dataObj.total_size = 0;
		}
	}
}
