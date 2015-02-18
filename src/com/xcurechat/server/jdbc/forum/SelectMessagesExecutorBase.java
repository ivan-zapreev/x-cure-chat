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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.client.data.ShortUserData;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.server.jdbc.QueryExecutor;

/**
 * @author zapreevis
 * This executor allows to search for forum messages and to 1. count the number of replies for each
 * found message subtree, plus to count the total number of messages satisfying the search criteria.
 */
public abstract class SelectMessagesExecutorBase<ParamReturnObjectType> extends QueryExecutor<ParamReturnObjectType> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( SelectMessagesExecutorBase.class );
	
	protected static final String COMPLETE_RESULT_TABLE_NAME = "main";
	protected static final String SENDER_TABLE_NAME = "sender";
	protected static final String REPLY_SENDER_TABLE_NAME = "last_reply_sender";
	protected static final String TOTAL_TABLE_NAME = "tot";
	protected static final String TOTAL_SEARCH_RESULTS = "total";
	protected static final String LOCAL_MSG_TABLE_NAME = "local_messages";
	
	/**
	 * The basic constructor
	 */
	SelectMessagesExecutorBase( ) {
	}
	
	protected String getAsName( final String subTableName, final String columnName ) {
		return subTableName + "_" + columnName;
	}
	
	protected String makeAsNameQueryPart( final String subTableName, final String columnName ) {
		return subTableName + "." + columnName + " AS " + getAsName( subTableName, columnName );
	}
	
	@Override
	public PreparedStatement prepareStatement(Connection connection) throws SQLException {
		//Construct the first part of the query
		String selectQuery = "SELECT SQL_CALC_FOUND_ROWS " + COMPLETE_RESULT_TABLE_NAME + ".*, " +
							 TOTAL_TABLE_NAME + "." + TOTAL_SEARCH_RESULTS + " FROM ( " +
							 "SELECT " + LOCAL_MSG_TABLE_NAME + ".*, " +
							 makeAsNameQueryPart( SENDER_TABLE_NAME, UID_FIELD_NAME_USERS_TABLE ) + ", " + 
							 makeAsNameQueryPart( SENDER_TABLE_NAME, LOGIN_FIELD_NAME_USERS_TABLE ) + ", " +
							 makeAsNameQueryPart( SENDER_TABLE_NAME, GENDER_FIELD_NAME_USERS_TABLE ) + ", " +
							 makeAsNameQueryPart( SENDER_TABLE_NAME, LAST_ONLINE_FIELD_NAME_USERS_TABLE ) + ", " +
							 makeAsNameQueryPart( SENDER_TABLE_NAME, IS_ONLINE_FIELD_NAME_USERS_TABLE ) + ", " +
							 makeAsNameQueryPart( SENDER_TABLE_NAME, SPOILER_ID_FIELD_NAME_USERS_TABLE ) + ", " +
							 makeAsNameQueryPart( SENDER_TABLE_NAME, SPOILER_EXP_DATE_FIELD_NAME_USERS_TABLE ) + ", " +
							 makeAsNameQueryPart( REPLY_SENDER_TABLE_NAME, UID_FIELD_NAME_USERS_TABLE ) + ", " + 
							 makeAsNameQueryPart( REPLY_SENDER_TABLE_NAME, LOGIN_FIELD_NAME_USERS_TABLE ) + ", " +
							 makeAsNameQueryPart( REPLY_SENDER_TABLE_NAME, GENDER_FIELD_NAME_USERS_TABLE ) + ", " +
							 makeAsNameQueryPart( REPLY_SENDER_TABLE_NAME, LAST_ONLINE_FIELD_NAME_USERS_TABLE ) + ", " +
							 makeAsNameQueryPart( REPLY_SENDER_TABLE_NAME, IS_ONLINE_FIELD_NAME_USERS_TABLE ) +", " +
							 makeAsNameQueryPart( REPLY_SENDER_TABLE_NAME, SPOILER_ID_FIELD_NAME_USERS_TABLE ) + ", " +
							 makeAsNameQueryPart( REPLY_SENDER_TABLE_NAME, SPOILER_EXP_DATE_FIELD_NAME_USERS_TABLE ) +
							 " FROM ";
		
		//Form the messages conditions query part
		String messagesQueryPart = getMsgWhereQueryPart();
		//Make sure we do not show the invisible root message
		if( excludingRootMessage() ) {
			messagesQueryPart += (messagesQueryPart.isEmpty() ? "" : " AND " ) +
								  MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE +
								  	"!=" +
								  ForumMessageData.ROOT_FORUM_MESSAGE_ID;
		}
		
		//Continue building the search query with adding the messages search sub-table plus joining the sender etc tables
		selectQuery += 		 "( SELECT * FROM " + FORUM_MESSAGES_TABLE + 
							    ( messagesQueryPart.isEmpty() ? "" : " WHERE "+messagesQueryPart ) + " ) AS " + LOCAL_MSG_TABLE_NAME +
							 //Left join the short user data for the message sender
							 " LEFT JOIN (" + 
							 	" SELECT  " + UID_FIELD_NAME_USERS_TABLE + ", " +
							 				 LOGIN_FIELD_NAME_USERS_TABLE + ", " +
								 			 GENDER_FIELD_NAME_USERS_TABLE + ", " +
								 			 LAST_ONLINE_FIELD_NAME_USERS_TABLE + ", " +
								 			 IS_ONLINE_FIELD_NAME_USERS_TABLE  + ", " +
								 			 SPOILER_ID_FIELD_NAME_USERS_TABLE + ", " +
								 			 SPOILER_EXP_DATE_FIELD_NAME_USERS_TABLE + 
								 " FROM " + USERS_TABLE +
							 " ) AS " +  SENDER_TABLE_NAME + " ON " +
							 				 LOCAL_MSG_TABLE_NAME + "." + SENDER_ID_FIELD_NAME_FORUM_MESSAGES_TABLE +
							 				 	"=" + 
							 				 SENDER_TABLE_NAME + "." + UID_FIELD_NAME_USERS_TABLE +
							 //Left join the short user data for the last reply sender
							 " LEFT JOIN (" + 
							 	" SELECT  " + UID_FIELD_NAME_USERS_TABLE + ", " +
							 				 LOGIN_FIELD_NAME_USERS_TABLE + ", " +
								 			 GENDER_FIELD_NAME_USERS_TABLE + ", " +
								 			 LAST_ONLINE_FIELD_NAME_USERS_TABLE + ", " +
								 			 IS_ONLINE_FIELD_NAME_USERS_TABLE + ", " +
								 			 SPOILER_ID_FIELD_NAME_USERS_TABLE + ", " +
								 			 SPOILER_EXP_DATE_FIELD_NAME_USERS_TABLE +
								 " FROM " + USERS_TABLE +
							 " ) AS " +  REPLY_SENDER_TABLE_NAME + " ON " +
							 				 LOCAL_MSG_TABLE_NAME + "." + LAST_SENDER_ID_FIELD_NAME_FORUM_MESSAGES_TABLE +
							 				 	"=" + 
							 				 REPLY_SENDER_TABLE_NAME + "." + UID_FIELD_NAME_USERS_TABLE;
		
		//Order messages by the number of replies if it is the search for topics, otherwise
		//Order by the last reply and then by the update date, all descending.
		String orderQueryPart = " ORDER BY ";
		if( isSearchForSections() ) {
			orderQueryPart += NUM_REPLIES_FIELD_NAME_FORUM_MESSAGES_TABLE + " DESC ";
		} else {
			orderQueryPart += LAST_REPLY_DATE_FIELD_NAME_FORUM_MESSAGES_TABLE + " DESC, ";
			orderQueryPart += UPDATE_DATE_FIELD_NAME_FORUM_MESSAGES_TABLE + " DESC ";
		}
		
		//Add the selecting part of the query and complete it with the total count
		//Left join the total number of the search results, i.e. to avoid doing a separate counting query
		selectQuery += orderQueryPart + ") AS " + COMPLETE_RESULT_TABLE_NAME +
					   " LEFT JOIN ( SELECT FOUND_ROWS() AS " + TOTAL_SEARCH_RESULTS + ") " + TOTAL_TABLE_NAME + " ON TRUE ";

		if( isAddLimitOffset() ) {
			//Construct the limiting part of the query
			selectQuery += " LIMIT ? OFFSET ?";
		}
		
		logger.debug( "The new forum messages retrieval query is " + selectQuery );
		return connection.prepareStatement( selectQuery );
	}
	
	/**
	 * Allows to detect if we only search for sections. Then we order
	 * messages by the number of replies and the update date, both DESCENDING.
	 * @return if true we are searching for the site sections.
	 */
	protected abstract boolean isSearchForSections();
	
	/**
	 * Allows to detect if we should exclude the invisible root message from the search results
	 * @return true if the invisible root message should be excluded
	 */
	protected abstract boolean excludingRootMessage();
	
	/**
	 * Allows to indicate whether the limit and offset query substring should be added to to end of the query
	 * @return true if the "LIMIT ? OFFSET ?" substring should be added to the end of the query
	 */
	protected abstract boolean isAddLimitOffset();

	/**
	 * This method should return the conditions for selecting the forum messages
	 * @return the string that is the conditions for selecting forum messages
	 */
	protected abstract String getMsgWhereQueryPart();
	
	/**
	 * Allows to extract the fields of the forum message from the result set
	 * @param resultSet the result set pointing at a row with a message data
	 * @return the extracted message data
	 * @throws SQLException if smth goes wrong
	 */
	protected ForumMessageData extractMessageData( ResultSet resultSet ) throws SQLException {
		return extractMessageData( resultSet, new ForumMessageData() );
	}
	
	/**
	 * Allows to extract the short user data from the sear results subtable
	 * @param resultSet the result set
	 * @param tableName the name of the sub table
	 * @return the short user data
	 * @throws SQLException if smth goes wrong
	 */
	protected ShortUserData getShortUserData( ResultSet resultSet, final String tableName ) throws SQLException {
		ShortUserData userData = new ShortUserData();
		userData.setUID( resultSet.getInt( getAsName( tableName, UID_FIELD_NAME_USERS_TABLE ) ) );
		userData.setUserLoginName( resultSet.getString( getAsName( tableName, LOGIN_FIELD_NAME_USERS_TABLE ) ) );
		userData.setMale( resultSet.getBoolean( getAsName( tableName, GENDER_FIELD_NAME_USERS_TABLE ) ) );
		userData.setOnline( resultSet.getBoolean( getAsName( tableName, IS_ONLINE_FIELD_NAME_USERS_TABLE ) ) );
		userData.setUserLastOnlineDate( QueryExecutor.getTime(resultSet, getAsName( tableName, LAST_ONLINE_FIELD_NAME_USERS_TABLE ) ) );
		userData.setAvatarSpoilerId( resultSet.getInt( getAsName( tableName, SPOILER_ID_FIELD_NAME_USERS_TABLE ) ) );
		userData.setAvatarSpoilerExpDate( QueryExecutor.getTime( resultSet, getAsName( tableName, SPOILER_EXP_DATE_FIELD_NAME_USERS_TABLE ) ) );
		return userData;
	}
	
	/**
	 * Allows to extract the fields of the forum message from the result set
	 * @param resultSet the result set pointing at a row with a message data
	 * @param message the created message to fill out with data
	 * @return the extracted message data
	 * @throws SQLException if smth goes wrong
	 */
	protected ForumMessageData extractMessageData( ResultSet resultSet, ForumMessageData message ) throws SQLException {
		message.messageID = resultSet.getInt( MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE );
		message.parentMessageID = resultSet.getInt( PARENT_MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE );
		message.senderID = resultSet.getInt( SENDER_ID_FIELD_NAME_FORUM_MESSAGES_TABLE );
		message.sentDate = QueryExecutor.getTime(resultSet, SENT_DATE_FIELD_NAME_FORUM_MESSAGES_TABLE );
		message.updateDate = QueryExecutor.getTime(resultSet, UPDATE_DATE_FIELD_NAME_FORUM_MESSAGES_TABLE );
		message.messagePathID = resultSet.getString( MESSAGE_PATH_ID_FIELD_NAME_FORUM_MESSAGES_TABLE );
		message.messageTitle = resultSet.getString( MESSAGE_TITLE_FIELD_NAME_FORUM_MESSAGES_TABLE );
		message.messageBody = resultSet.getString( MESSAGE_BODY_FIELD_NAME_FORUM_MESSAGES_TABLE );
		message.isApproved = resultSet.getBoolean( IS_APPROVED_FIELD_NAME_FORUM_MESSAGES_TABLE );
		message.numVotes = resultSet.getInt( NUMBER_OF_VOTES_FIELD_NAME_FORUM_MESSAGES_TABLE );
		message.voteValue = resultSet.getInt( VOTE_VALUE_FIELD_NAME_FORUM_MESSAGES_TABLE );
		//We do not retrieve the indicator of whether the user has voted any more, so we assume that he did not
		message.hasVoted = false;
		
		//Add the sender's data
		message.senderData = getShortUserData( resultSet, SENDER_TABLE_NAME );
		
		//Allocate the array for attached files, it will be filled out elsewhere
		message.attachedFileIds = new ArrayList<ShortFileDescriptor>();
		
		//Get the information about the replies
		message.numberOfReplies = resultSet.getInt( NUM_REPLIES_FIELD_NAME_FORUM_MESSAGES_TABLE );
		//If there are replies, then initialize the latest reply sender and the latest reply date
		if( message.numberOfReplies > 0 ) {
			message.lastReplyDate = QueryExecutor.getTime(resultSet, LAST_REPLY_DATE_FIELD_NAME_FORUM_MESSAGES_TABLE );
			message.lastReplyUser = getShortUserData( resultSet, REPLY_SENDER_TABLE_NAME );
		}
		return message;
	}

	@Override
	public ResultSet executeQuery( PreparedStatement pstmt, ParamReturnObjectType result ) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}
}
