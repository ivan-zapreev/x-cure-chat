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
package com.xcurechat.server.jdbc.forum.vote;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.rpc.exceptions.MessageException;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor tries to retrieve the user vote for the given forum message, if the vote is found then an exception is thrown
 */
public class CheckIfTheUserVotedExecutor extends QueryExecutor<Void> {
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( CheckIfTheUserVotedExecutor.class );
	
	private final int messageID;
	private final int userID;
	
	public CheckIfTheUserVotedExecutor( final int messageID, final int userID ){
		this.messageID = messageID;
		this.userID = userID;
	}
	
	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		final String selectQuery = "SELECT * FROM " + FORUM_MESSAGE_VOTES_TABLE + " WHERE " +
										MESSAGE_ID_FIELD_FORUM_MESSAGE_VOTES_TABLE + "=?" +
									" AND " + 
										SENDER_ID_FIELD_FORUM_MESSAGE_VOTES_TABLE + "=?";
		return connection.prepareStatement( selectQuery );
	}
	
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		pstmt.setInt( counter++, messageID );
		pstmt.setInt( counter++, userID );
	}
	
	public ResultSet executeQuery(PreparedStatement pstmt, Void result) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}
	
	public void processResultSet(ResultSet resultSet, Void result) throws SQLException, SiteException {
		if( resultSet.first() ) {
			logger.warn("The user " + userID + " tries to vote for the forum message " + messageID + " but he alredy voted, action skipped!");
			throw new MessageException( MessageException.YOU_HAVE_ALREADY_VOTED_FOR_THIS_MESSAGE );
		} else {
			logger.debug("The user " + userID + " tries to vote for the forum message " + messageID + " and he did not vote, yet!");
		}
	}
}
