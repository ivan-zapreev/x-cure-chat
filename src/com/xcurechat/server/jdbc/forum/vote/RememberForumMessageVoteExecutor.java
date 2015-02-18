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

package com.xcurechat.server.jdbc.forum.vote;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.rpc.exceptions.MessageException;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

/**
 * @author zapreevis
 * This executor class is responsible for storing the information about the given user voting for the given forum message
 */
public class RememberForumMessageVoteExecutor extends QueryExecutor<Void> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( RememberForumMessageVoteExecutor.class );
	
	private final int messageID;
	private final int userID;
	private final boolean voteFor;
	
	public RememberForumMessageVoteExecutor( final int messageID, final int userID, final boolean voteFor ){
		this.messageID = messageID;
		this.userID = userID;
		this.voteFor = voteFor;
	}

	public PreparedStatement prepareStatement( Connection connection ) throws SQLException{
		final String insertQuery = "INSERT INTO " + FORUM_MESSAGE_VOTES_TABLE +
									" SET " + MESSAGE_ID_FIELD_FORUM_MESSAGE_VOTES_TABLE + " = ?, " +
									SENDER_ID_FIELD_FORUM_MESSAGE_VOTES_TABLE + " = ?, " +
									VOTE_VALUE_FIELD_FORUM_MESSAGE_VOTES_TABLE + "= ?";
		return connection.prepareStatement( insertQuery, PreparedStatement.RETURN_GENERATED_KEYS );
	}
	
	public void bindParameters( PreparedStatement pstmt ) throws SQLException{
		int counter = 1;
		pstmt.setInt( counter++, messageID );
		pstmt.setInt( counter++, userID );
		pstmt.setInt( counter++, (voteFor ? 1 : 0) );
	}
	
	public ResultSet executeQuery( PreparedStatement pstmt, Void result ) throws SQLException, SiteException {
		try{
			//Execute the insert
			pstmt.executeUpdate();
		} catch( MySQLIntegrityConstraintViolationException e ) {
			logger.error( "An unhandled exception when voting for a forum message " + messageID + " by " + userID, e);
			throw new MessageException( MessageException.THE_MESSAGE_DOES_NOT_EXIST );
		} catch( SQLException e ){
			logger.error( "An unhandled exception when voting for a forum message " + messageID + " by " + userID, e);
			throw new InternalSiteException(InternalSiteException.UNKNOWN_INTERNAL_SITE_EXCEPTION_ERR);
		}
		return null;
	}
	
	public void processResultSet( ResultSet resultSet, Void result ) throws SQLException, SiteException {
		//DO NOTHING
	}
}
