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

import java.util.LinkedHashMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ShortForumMessageData;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.data.search.ForumSearchData;
import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.rpc.exceptions.SiteException;

/**
 * @author zapreevis
 * This executor allows to search for forum messages and to 1. count the number of replies for each
 * found message subtree, plus to count the total number of messages satisfying the search criteria.
 */
public class SearchMessagesExecutor extends SelectMessagesExecutorBase<LinkedHashMap<Integer,ForumMessageData>> {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( SearchMessagesExecutor.class );
	
	//Stores the search parameters
	private final ForumSearchData searchParams;
	//Is used here to store the total number of search result entries
	private final OnePageViewData<ForumMessageData> countTotal;
	
	public SearchMessagesExecutor(  final ForumSearchData searchParams,
									final OnePageViewData<ForumMessageData> countTotal ) {
		super( );
		this.searchParams = searchParams;
		this.countTotal = countTotal;
	}

	@Override
	public void bindParameters(PreparedStatement pstmt) throws SQLException {
		int counter = 1;
		
		if( !searchParams.isOnlyMessage ) {
			//If we are not looking for a single message with a given ID
			if( searchParams.searchString != null && !searchParams.searchString.trim().isEmpty() ) {
				pstmt.setString( counter++, searchParams.searchString);
				pstmt.setString( counter++, searchParams.searchString);
			}
			if( searchParams.byUserID != ShortUserData.UNKNOWN_UID ) {
				pstmt.setInt( counter++, searchParams.byUserID );
			}
			if( ! searchParams.isOnlyTopics ) {
				if( searchParams.baseMessageID != ForumMessageData.UNKNOWN_MESSAGE_ID ) {
					pstmt.setInt( counter++, searchParams.baseMessageID );
					if( searchParams.isOnlyInCurrentTopic ) {
						pstmt.setInt( counter++, searchParams.baseMessageID );
					}
				}
			}
			if( searchParams.isApproved ) {
				//If we are looking for the approved messages
				pstmt.setBoolean( counter++, true );
			}
		} else {
			//Here we are only searching for a single message with a given ID			
			pstmt.setInt( counter++, searchParams.baseMessageID );
		}
		
		///Set the offset and the number of search results per page
		pstmt.setInt( counter++, searchParams.MAX_NUMBER_OF_MESSAGES_PER_PAGE );
		pstmt.setInt( counter++, ( searchParams.pageIndex - 1 ) * searchParams.MAX_NUMBER_OF_MESSAGES_PER_PAGE );
	}

	@Override
	public ResultSet executeQuery( PreparedStatement pstmt, LinkedHashMap<Integer,ForumMessageData> result ) throws SQLException, SiteException {
		return pstmt.executeQuery();
	}

	@Override
	public void processResultSet( ResultSet resultSet, LinkedHashMap<Integer,ForumMessageData> result ) throws SQLException, SiteException {
		boolean isFirst = true;
		
		//Process the search results
		while( resultSet.next() ) {
			if( isFirst ) {
				//Get the total number of found entries satisfying the search query
				countTotal.total_size = resultSet.getInt( TOTAL_SEARCH_RESULTS );
				isFirst = false;
			}
			
			//Extract the message
			ForumMessageData message = extractMessageData( resultSet );
			
			result.put( message.messageID, message );  
		}
		logger.debug("Searching for forum messages resulted in " + result.size() + " hits.");
	}

	@Override
	protected String getMsgWhereQueryPart() {
		//Construct the selecting part of the query
		String selectingQueryPart = "";
		
		if( !searchParams.isOnlyMessage ) {
			//If we are not looking for a single message with a given ID
			if( searchParams.searchString != null && !searchParams.searchString.trim().isEmpty() ) {
				searchParams.searchString = searchParams.searchString.trim();
				selectingQueryPart += " ( " +
											  MESSAGE_BODY_FIELD_NAME_FORUM_MESSAGES_TABLE + " LIKE CONCAT(\"%\",?,\"%\") OR " +
											  MESSAGE_TITLE_FIELD_NAME_FORUM_MESSAGES_TABLE + " LIKE CONCAT(\"%\",?,\"%\")" + 
									  " ) ";
			} else {
				searchParams.searchString = null;
			}
			
			if( searchParams.byUserID != ShortUserData.UNKNOWN_UID ) {
				selectingQueryPart += selectingQueryPart.isEmpty() ? " " : " AND ";
				selectingQueryPart += SENDER_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "=? ";
			}
			
			if( searchParams.isOnlyTopics ) {
				selectingQueryPart += selectingQueryPart.isEmpty() ? " " : " AND ";
				selectingQueryPart += MESSAGE_PATH_ID_FIELD_NAME_FORUM_MESSAGES_TABLE +
									  " RLIKE \"^" + ForumMessageData.ROOT_FORUM_MESSAGE_ID + "\\" +
									  ShortForumMessageData.MESSAGE_PATH_ID_DELIMITER +
									  "[0-9]+" + "\\" + ShortForumMessageData.MESSAGE_PATH_ID_DELIMITER +
						  			  "$\"";				
			} else {
				if( searchParams.baseMessageID != ForumMessageData.UNKNOWN_MESSAGE_ID ) {
					selectingQueryPart += selectingQueryPart.isEmpty() ? " " : " AND ";
					//In case the base message is set it can be only for
					if( searchParams.isOnlyInCurrentTopic ) {
						//Searching in the topic defined by this message
						selectingQueryPart += "(" + MESSAGE_PATH_ID_FIELD_NAME_FORUM_MESSAGES_TABLE +
											  " LIKE CONCAT( \"" + ForumMessageData.ROOT_FORUM_MESSAGE_ID +
											  ShortForumMessageData.MESSAGE_PATH_ID_DELIMITER + "%" +
											  ShortForumMessageData.MESSAGE_PATH_ID_DELIMITER +
											  "\", " + " ?, \"" + ShortForumMessageData.MESSAGE_PATH_ID_DELIMITER + "%\" ) OR " +
											  MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "=? ) ";
					} else {
						//Search for the direct replies of the given message, basically here we are viewing the direct
						//replies and there should be no other conditions like the sender id or smth like that. At least
						//currently the front-end does not search for specific message replies, but just looks for all
						selectingQueryPart += PARENT_MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "=?";
					}
				}
			}
			
			if( ! isSearchForSections() ) {
				//When searching for non-sections we do not allow sections in the search results
				selectingQueryPart += selectingQueryPart.isEmpty() ? " " : " AND ";
				selectingQueryPart += PARENT_MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "!=" + ForumMessageData.ROOT_FORUM_MESSAGE_ID;
			}
			
			if( searchParams.isApproved ) {
				selectingQueryPart += selectingQueryPart.isEmpty() ? " " : " AND ";
				selectingQueryPart += IS_APPROVED_FIELD_NAME_FORUM_MESSAGES_TABLE + "= ?";
			}
		} else {
			//Here we are only searching for a single message with a given ID
			selectingQueryPart += selectingQueryPart.isEmpty() ? " " : " AND ";
			selectingQueryPart += MESSAGE_ID_FIELD_NAME_FORUM_MESSAGES_TABLE + "=?";
		}

		return selectingQueryPart;
	}
	
	@Override
	protected boolean isAddLimitOffset() {
		return true;
	}

	@Override
	protected boolean excludingRootMessage() {
		return true;
	}

	@Override
	protected boolean isSearchForSections() {
		//If we are searching for sections then we should order by the number of replies
		return searchParams.isBrowsingSectionsSearch();
	}
}
