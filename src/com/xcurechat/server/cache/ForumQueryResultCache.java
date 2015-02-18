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
 * The server-side utilities package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.server.cache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.search.ForumSearchData;
import com.xcurechat.server.utils.ServerEncoder;

/**
 * @author zapreevis
 * This class is used to wrap the search query results 
 */
public class ForumQueryResultCache {
	//The encoder that has to be used in the server side
	private static final ServerEncoder encoder = new ServerEncoder(); 
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( ForumQueryResultCache.class ); 
	
	//The time out after the last use of the query when it can be considered old and unimportant
	private static final int QUERY_TIME_OUT_MILLISEC = 30*60*1000; //30 minutes
	//The minimum allowed message usage frequency per hour.
	private static final double MINIMUM_QUERY_USAGE_FREQUENCY_PER_HOUR = 0.5; //14 usages per 24 hours
	
	//Possible types of the forum queries
	public static final int NEWS_PAGE_FORUM_QUERY_TYPE = 1;
	public static final int FORUM_NAVIGATION_QUERY_TYPE = NEWS_PAGE_FORUM_QUERY_TYPE + 1;
	public static final int FORUM_ONE_MESSAGE_QUERY_TYPE = FORUM_NAVIGATION_QUERY_TYPE + 1;
	public static final int FORUM_CUSTOM_QUERY_TYPE = FORUM_ONE_MESSAGE_QUERY_TYPE + 1;
	
	//The serialized search query corresponding to the search data, is used for logging
	private final String query;
	
	//The time the query was cached.
	private final long queryCacheTime;
	
	//The last time the query was used in millisec
	private long lastResultAccess;
	
	//The number of times the query was used
	private long useCount = 0;
	
	//The total number of objects in the database. This data field
	//is not necessarily set up on the server, thus one has to be careful!
	private final int total_size;
	
	//The offset for the selected data, this object contains
	//data starting from the entry "offset + 1"
	private final int offset;
	
	//Stores the list of message ids
	private final List<Integer> messageIds = new ArrayList<Integer>();
	
	//Stores the set of file ids of the files attached to the messages of this request
	private final Set<Integer> fileIds = new HashSet<Integer>();
	
	//Stores the search data
	private final ForumSearchData searchData;
	
	//Stores the serialized query string
	private final String queryStr;
	
	//Stores the search query type
	private final int queryType;
	
	/**
	 * Here is the basic constructor with the two parameters
	 * from the OnePageViewData object being arguments
	 * @param total_size the total number of results for this query
	 * @param offset the offset for the page of the results we are viewing
	 * @param searchData the query data object
	 * @param query the query corresponding to the search data
	 * @param message the list of messages that are results for the
	 *                given search query MUST BE NOT NULL
	 */
	public ForumQueryResultCache( final int total_size, final int offset,
								  final ForumSearchData searchData,
								  final String query,
								  final List<ForumMessageData> messages ) {
		//Store the data
		this.total_size = total_size;
		this.offset = offset;
		this.searchData = searchData;
		this.query = query;
		this.queryStr = searchData.serialize(encoder);
		this.queryType = getForumQueryType( searchData, queryStr );
		this.queryCacheTime = System.currentTimeMillis();
		
		//Fill out the set with the message ids
		for( ForumMessageData message : messages ) {
			messageIds.add( message.messageID );
			//Store all the file ids for the files attached to these message
			if( message.attachedFileIds != null ) {
				for( ShortFileDescriptor fileDesc : message.attachedFileIds ) {
					fileIds.add( fileDesc.fileID );
				}
			}
		}
		
		//This counts as a first access
		markQueryAccess();
	}
	
	/**
	 * Allows to check if the given query contains the given message in its results
	 * @param messageID the id of the message to check
	 * @return true if the message is contained in the results of this query
	 */
	public boolean containsMessage( final int messageID ) {
		return messageIds.contains( messageID );
	}
	
	/**
	 * Allows to check if the given query contains a message in its results
	 * such that if contains a file with the given id 
	 * @param fileID the id of the message file to check
	 * @return true if there is a message containing the file with the given id
	 */
	public boolean containsFile( final int fileID ) {
		return fileIds.contains( fileID );
	}
	
	/**
	 * Must be called every time the object is accessed, i.e. we need the results of this query
	 */
	private void markQueryAccess() {
		lastResultAccess = System.currentTimeMillis();
		useCount += 1;
	}
	
	/**
	 * Decides whether this query is important based on the time of the last query access and other metrics
	 * @return true if the query is considered to be unimportant and can be removed from the cache
	 */
	public boolean isLowPriorityQuery() {
		//Get the current time, milliseconds
		final long currentTime = System.currentTimeMillis();
		
		//Check if the last access of this query was sufficiently long ago
		if( ( lastResultAccess + QUERY_TIME_OUT_MILLISEC ) < currentTime ) {
			logger.debug( "The query: '" + query + "' was last used more than " + QUERY_TIME_OUT_MILLISEC +
						  " milliseconds ago, let us check if it is still relevant" );
			
			//Get the time since the query was cached, milliseconds
			final long queryUsageTimeInterval  = currentTime - queryCacheTime;
			//Compute the number of times, per hour
			final double queryUseFrequency = useCount / ( queryUsageTimeInterval / 60*60*1000 );
			logger.debug( "The query: '" + query + "' has been used " + useCount + " times during the period of " +
						  queryUsageTimeInterval + " milliseconds, the usage frequency is " + queryUseFrequency +
						  " times hour" );
			
			return queryUseFrequency < MINIMUM_QUERY_USAGE_FREQUENCY_PER_HOUR;
		} else {
			logger.debug("The query: '" + query + "' was last used " + (currentTime - lastResultAccess) + " milliseconds ago, it is still fresh" );
			return false;
		}
	}
	
	/**
	 * @return the total number of the search results 
	 */
	public int getTotalSize() {
		//The object is clearly being accessed
		markQueryAccess();
		
		return total_size;
	}

	/**
	 * @return the offset for the page of the results we are viewing
	 */
	public int getOffset() {
		return offset;
	}
	
	/**
	 * Allows to determine the forum query type
	 * @param searchData the forum query object
	 * @param query, the serialization string of the searchData, is used for logging
	 * @return one of: NEWS_PAGE_FORUM_QUERY_TYPE, FORUM_NAVIGATION_QUERY_TYPE, FORUM_CUSTOM_QUERY_TYPE
	 */
	private static int getForumQueryType( final ForumSearchData searchData, final String query ) {
		if( searchData.isNewsPageBrowsing() ) {
			logger.debug("The query " + query + " is for browsing the news from the forum, i.e. approved messages");
			return ForumQueryResultCache.NEWS_PAGE_FORUM_QUERY_TYPE;
		} else {
			if( searchData.isForumNavigation() ) {
				logger.debug("The query " + query + " is for browsing the forum");
				return ForumQueryResultCache.FORUM_NAVIGATION_QUERY_TYPE;
			} else {
				if( searchData.isForumMessageView() ) {
					logger.debug("The query " + query + " is for searching for a one known message");
					return ForumQueryResultCache.FORUM_ONE_MESSAGE_QUERY_TYPE;
				} else {
					logger.debug("The query " + query + " is a custom forum search");
					return ForumQueryResultCache.FORUM_CUSTOM_QUERY_TYPE;
				}
			}
		}
	}

	/**
	 * @return true if this is the query for browsing the news
	 */
	public boolean isNewsPageBrowsing() {
		boolean result = ( queryType == NEWS_PAGE_FORUM_QUERY_TYPE );
		logger.debug("Checking the query " + queryStr + " for being the NEWS_PAGE_FORUM_QUERY_TYPE, the outcome is " + result );
		return result;
	}
	
	/**
	 * @return true if this is the forum-navigation query
	 */
	public boolean isForumNavigation() {
		boolean result = ( queryType == FORUM_NAVIGATION_QUERY_TYPE );
		logger.debug("Checking the query " + queryStr + " for being the FORUM_NAVIGATION_QUERY_TYPE, the outcome is " + result );
		return result;
	}
	
	/**
	 * @return true if this is the one-message-view query
	 */
	public boolean isOneMessageView() {
		boolean result = ( queryType == FORUM_ONE_MESSAGE_QUERY_TYPE );
		logger.debug("Checking the query " + queryStr + " for being the FORUM_ONE_MESSAGE_QUERY_TYPE, the outcome is " + result );
		return result;
	}
	
	/**
	 * @return true if this is the query for custom browsing
	 */
	public boolean isCustomBrowsing() {
		boolean result = ( queryType == FORUM_CUSTOM_QUERY_TYPE );
		logger.debug("Checking the query " + queryStr + " for being the FORUM_CUSTOM_QUERY_TYPE, the outcome is " + result );
		return result;
	}
	
	/**
	 * Allows to get the messages for the given user, with the indication of whether
	 * the user has voted for the messages yet. If retrieved with idToVoterIds == null
	 * or userID == ShortUserData.UNKNOWN_UID,  ShortUserData.DEFAULT_UID then the
	 * messages provided by this method should not be shown to the user that is logged in!
	 * @param idToMessageWrap the map of message ids to message wrappers
	 * @param idToVoterIds the mapping from the message ID to the set of users who voted for this message
	 * @param userID the id of the user we retrieve the messages for 
	 * @return gets the messages the list of messages
	 */
	public List<ForumMessageData> getMessages(  final Map<Integer, ForumMessageCacheWrapper> idToMessageWrap,
												final Map<Integer, Set<Integer>> idToVoterIds, final int userID ) {
		List<ForumMessageData> messages = new ArrayList<ForumMessageData>();
		
		for( Integer messageID : messageIds ) {
			ForumMessageCacheWrapper messageWrap = idToMessageWrap.get( messageID );
			if( messageWrap != null ) {
				ForumMessageData message = messageWrap.getMessageData();
				if( ( idToVoterIds != null ) && ( userID != ShortUserData.UNKNOWN_UID ) && ( userID != ShortUserData.DEFAULT_UID ) ) {
					Set<Integer> voters = idToVoterIds.get( messageID );
					if( voters != null ) {
						//Check if the user is known to have been voted for this message
						message.hasVoted = voters.contains( userID );
					} else {
						//Let the user to attempt voting, we do not know if he voted yet
						message.hasVoted = false;
					}
				}
				messages.add( message );
			} else {
				logger.error("Trying to retrieve the message " + messageID + " from the cache but it does not have id to msg wrap mapping!");
			}
		}
		return messages;
	}
	
	/**
	 * Allows to get messages without the proper handling of the votes.
	 * Messages retrieved with this method are for internal use only!
	 * @param idToMessageWrap the map of message ids to message wrappers
	 * @return gets the messages
	 */
	public List<ForumMessageData> getMessages(final Map<Integer, ForumMessageCacheWrapper> idToMessageWrap) {
		return getMessages( idToMessageWrap, null, ShortUserData.UNKNOWN_UID );
	}

	/**
	 * @return the number of messages for this query
	 */
	public int getNumberOfMsgs() {
		return messageIds.size();
	}

	/**
	 * @return the base message id, i.e. the message we are browsing replies for 
	 */
	public int getBaseMessageID() {
		return searchData.baseMessageID;
	}
	
}
