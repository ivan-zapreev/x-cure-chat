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

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.MessageException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor class is responsible for adding the vote to the forum message
 */
public class VoteForForumMessageExecutor extends QueryExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( VoteForForumMessageExecutor.class );

	private final int messageID;
	private final boolean voteFor;
	
	/**
	 * The basic constructor
	 * @param messageID the message to vote for
	 * @param voteFor if true then we vote for the message, otherwise against it
	 */
	public VoteForForumMessageExecutor( final int messageID, final boolean voteFor ){
		this.messageID = messageID;
		this.voteFor = voteFor;
	}

	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		String updateQuery = "UPDATE " + FORUM_MESSAGES_TABLE + " SET " +
							NUMBER_OF_VOTES_FIELD_NAME_FORUM_MESSAGES_TABLE + "=" + NUMBER_OF_VOTES_FIELD_NAME_FORUM_MESSAGES_TABLE + "+1" +
							( voteFor ? ", " + VOTE_VALUE_FIELD_NAME_FORUM_MESSAGES_TABLE + "=" + VOTE_VALUE_FIELD_NAME_FORUM_MESSAGES_TABLE + "+1" : "" ) +
							" WHERE "+ MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "=?";
		
		return connection.prepareStatement( updateQuery );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		pstmt.setInt( counter++, messageID );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		if( pstmt.executeUpdate() == 0 ) {
			logger.error("Could not account the vote for the forum message " +  messageID );
			throw new MessageException( MessageException.THE_MESSAGE_DOES_NOT_EXIST );
		}
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
