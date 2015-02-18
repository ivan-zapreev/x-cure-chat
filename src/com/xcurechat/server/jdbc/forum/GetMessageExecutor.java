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
package com.xcurechat.server.jdbc.forum;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ForumMessageData;

import com.xcurechat.client.rpc.exceptions.SiteException;

/**
 * @author zapreevis
 * This executor allows to search for forum messages and to 1. count the number of replies for each
 * found message subtree, plus to count the total number of messages satisfying the search criteria.
 */
public class GetMessageExecutor extends SelectMessagesExecutorBase<ForumMessageData> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( GetMessageExecutor.class );
	
	//The id of the message we want to retrieve
	private final int messageID;
	
	/**
	 * The basic constructor
	 * @param currentUserID the id of the user performing the forum message retrieval (search)
	 * @param messageID the id of the message to be retrieved
	 */
	public GetMessageExecutor( final int messageID ) {
		this.messageID = messageID;
	}
	
	@Override
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		pstmt.setInt( counter++, messageID);
	}
	
	@Override
	public ResultSet executeQuery( PreparedStatement pstmt, ForumMessageData result ) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}
	
	@Override
	public void processResultSet( ResultSet resultSet, ForumMessageData result ) throws SQLException, SiteException {
		if( resultSet.next() ) {
			//Extract the message
			extractMessageData( resultSet, result );
			logger.warn("The forum message " + messageID + " was found" );
		} else {
			logger.warn("Could not find the forum message " + messageID );
		}
	}
	
	@Override
	protected boolean isAddLimitOffset() {
		return false;
	}
	
	@Override
	protected String getMsgWhereQueryPart() {
		return MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "=? ";
	}

	@Override
	protected boolean excludingRootMessage() {
		return true; //The root message should not be shown, ever
	}

	@Override
	protected boolean isSearchForSections() {
		return false; //Does not matter really, since we get at most one message
	}
}
