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
 * The server-side cache package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.server.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.data.ShortForumMessageData;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.search.ForumSearchData;
import com.xcurechat.client.data.search.OnePageViewData;
import com.xcurechat.server.utils.ServerEncoder;

/**
 * @author zapreevis
 * This class is responsible for caching the forum queries
 */
public class ForumQueriesCache {
	//The encoder that has to be used in the server side
	private static final ServerEncoder encoder = new ServerEncoder(); 
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( ForumQueriesCache.class );
	
	//The minimum time interval between the successive cache clean-ups
	private static final int MIN_CACHE_CLEAN_UP_INTERVAL_MILLISEC = 5000;
	
	//The maximum number of cached messages
	private static final int MAX_NUMBER_CACHED_MESSAGES = 2500; //About 40 Mb of data with the max message sizes
	
	//The only instance of this object
	private static final ForumQueriesCache instance = new ForumQueriesCache();
	
	//Stores mapping from the serialized forum search data to the query result
	private Map<String, ForumQueryResultCache> queryToResult = new HashMap<String, ForumQueryResultCache>();
	
	//Known messages map, it maps the message id to the message wrapper
	private Map<Integer, ForumMessageCacheWrapper> idToMessageWrap = new HashMap<Integer, ForumMessageCacheWrapper>();
	
	//Known message votes map, it maps the message id to the set of user ID of the users who voted for this message
	private Map<Integer, Set<Integer>> idToVoterIds = new HashMap<Integer, Set<Integer>>();
	
	//The last cache clean up
	private long lastCachCleanUp = System.currentTimeMillis();
	
	/**
	 * The class is used a a singleton 
	 */
	private ForumQueriesCache() {}
	
	/**
	 * @return the only instane of the query cache object
	 */
	public static ForumQueriesCache getInstane() {
		return instance;
	}
	
	/**
	 * Allows to update the message in the cache with a new vote
	 * NOTE: This method is synchronized
	 * @param messageID the id of the message to be voted for
	 * @param voteFor true to say that the message is good, otherwise false (bad)
	 * @param userID the id of the user who voted for the message
	 */
	public synchronized void voteForMessage(final int messageID, final boolean voteFor, final int userID) {
		ForumMessageCacheWrapper messageWrap = idToMessageWrap.get( messageID );
		if( messageWrap != null ) {
			ForumMessageData messageData = messageWrap.getMessageData();
			messageData.numVotes++;
			if( voteFor ) {
				messageData.voteValue++;
			}
			//Mark that the user has voted for this message
			registerUserVote( messageID, userID );
		}
	}
	
	/**
	 * Just mark that the user has voted for this message, no counters are incremented
	 * The method marks the vote if the user ID is not equal to
	 * ShortUserData.UNKNOWN_UID or ShortUserData.DEFAULT_UID
	 * @param messageID the message we vote for
	 * @param userID the user who votes 
	 */
	public synchronized void registerUserVote( final int messageID, final int userID ) {
		if( ( userID != ShortUserData.UNKNOWN_UID ) && ( userID != ShortUserData.DEFAULT_UID ) ) {
			Set<Integer> voters = idToVoterIds.get( messageID );
			if( voters == null ) {
				voters = new HashSet<Integer>();
				idToVoterIds.put( messageID, voters );
			}
			voters.add( userID );
		}
	}
	
	/**
	 * Allows to retrieve the forum search result object based on the request, in case this object is cached 
	 * NOTE: This method is synchronized
	 * @param searchData the search query for which we try to retrieve the results from the cache
	 * @param userID the id of the user retrieving these messages
	 * @return the query result or null if nothing is cached
	 */
	public synchronized OnePageViewData<ForumMessageData> getQueryResult( final ForumSearchData searchData, final int userID ) {
		//Serialize the message for proper hashing
		final String query = searchData.serialize(encoder);
		logger.debug("Searching for the cached query " + query);
		final ForumQueryResultCache resultSet = queryToResult.get( query );
		
		OnePageViewData<ForumMessageData> result = null;
		if( resultSet != null ) {
			logger.debug("The cached query " + query + " is found");
			//Construct the resulting object
			result = new OnePageViewData<ForumMessageData>();
			result.total_size = resultSet.getTotalSize();
			result.offset = resultSet.getOffset();
			result.entries = resultSet.getMessages( idToMessageWrap, idToVoterIds, userID );
		} else {
			logger.debug("The cached query " + query + " is not found");
		}
		
		return result;
	}
	
	/**
	 * Allows to get a forum message from the cache
	 * @param messageID the id of the forum message to get
	 * @param userID the id of the user who gets the message
	 * @return the found cached message or null
	 */
	public synchronized ForumMessageData getForumMessage( final int messageID, final int userID ) {
		logger.debug("Attempting to get a single forum message " + messageID + " for the user " + userID );
		ForumMessageData message = null;
		ForumMessageCacheWrapper messageWrap = idToMessageWrap.get( messageID );
		if( messageWrap != null ) {
			message = messageWrap.getMessageData(); 
			Set<Integer> msgVoters = idToVoterIds.get( messageID );
			if( msgVoters != null && msgVoters.contains( userID ) ) {
				message.hasVoted = true;
			} else {
				message.hasVoted = false;
			}
		}
		
		return message;
	}
	
	/**
	 * Allows to put the query result into the cache, in case it is not yet there
	 * NOTE: This method is synchronized
	 * @param searchData the query search object
	 * @param userID the id of the user retrieving these messages
	 * @param result the search query result set.
	 */
	public synchronized void putQueryResult( final ForumSearchData searchData, OnePageViewData<ForumMessageData> result, final int userID ) {
		//Serialize the message for proper hashing
		final String query = searchData.serialize(encoder);
		logger.debug("Attempting to put results for query " + query + " into the cache");
		if( ! queryToResult.containsKey( query ) ) {
			//Compute the number of messages
			final int numberOfMsgs = (result.entries != null ? result.entries.size() : 0 );
			logger.debug("The query " + query + " results in " + numberOfMsgs + " messages each of which should be cahed");
			
			//Check if there is space for storing more query results
			if( canCacheMoreQueries( numberOfMsgs ) ) {
				//If the query results are not cached yet
				final List<ForumMessageData> messages = new ArrayList<ForumMessageData>();
				
				//Get the messages and place them into the pool and store their ids
				for( ForumMessageData messageData : result.entries ) {
					ForumMessageCacheWrapper cachedMessageWrap = idToMessageWrap.get( messageData.messageID );
					if ( cachedMessageWrap == null ) {
						//The message is not cached yet
						cachedMessageWrap = new ForumMessageCacheWrapper( messageData );
						//Put the message into the pool
						idToMessageWrap.put( messageData.messageID, cachedMessageWrap );
					} else {
						//Update the cached message with the current data
						//Just in case, the same message could have changes
						//attributes such are replies count and the message
						//can still be in cache because it is e.g. in the
						//news results 
						cachedMessageWrap.registerNewMessageReference( messageData );
					}
					
					//Mark if the user has voted for this message
					if( messageData.hasVoted ) {
						registerUserVote( messageData.messageID, userID );
					}
					
					//Store the message id in the result set, re-use the message from the pool!!!
					//Do not store the original message in order to reduce memory consumption 
					messages.add( cachedMessageWrap.getMessageData() );
				}
				
				//Construct the wrapper and put it to the query to result set mapping
				queryToResult.put(query, new ForumQueryResultCache( result.total_size, result.offset, searchData, query, messages ) );
			} else {
				logger.warn("There is no space left in cache, storing the results of query " + query + " is cancelled");
			}
		} else {
			logger.debug("The results for query " + query + " are already cached");
		}
	}
	
	/**
	 * Allows to approve/disapprove the messages in the cache
	 * NOTE: This method is not synchronized
	 * @param messageIds the ids of the messages we want to approve/disapprove
	 * @param approve true for approve, false for disapprove
	 */
	private void approveMessage( final Set<Integer> messageIds, final boolean approve ) {
		//Distinguish between approving and disapproving the messages
		logger.debug("The cache wants to process the fact that the approval of messages " + messageIds + " became: " + approve );
		if( approve ) {
			//If we approve a new message, then remove all of the news browsing queries
			logger.debug( "Sine we approve new messages" + messageIds + ", we remove all of the news browsing queries" );
			removeAllNewsQueries();
		} else {
			//If we disapprove the message then remove only the news queries containing this message
			logger.debug( "Since we disapprove the messages" + messageIds + ", we remove only the news queries containing these messages" );
			removeNewsQueriesWithTheMessages( messageIds );
		}
		
		//If the messages are still cached, then set their new status
		for( Integer messageID : messageIds) {
			ForumMessageCacheWrapper cachedMessageWrap = idToMessageWrap.get( messageID );
			if( ( cachedMessageWrap != null ) ) {
				logger.debug( "The message " + messageID + ", is still in the cache so we mark that is approval is: " + approve );
				cachedMessageWrap.getMessageData().isApproved = approve;
			}
		}
	}

	/**
	 * Allows to approve/disapprove the messages in the cache
	 * @param messageID the id of the message we want to approve/disapprove
	 * @param approve true for approve, false for disapprove
	 */
	public synchronized void approveMessage( final int messageID, final boolean approve ) {
		Set<Integer> ids = new HashSet<Integer>();
		ids.add( messageID );
		approveMessage( ids, approve );
	}
	
	/**
	 * Allows to remove all news queries that contain at least one of the given messages
	 * @param messageIds the list of message ids to consider
	 */
	private void removeNewsQueriesWithTheMessages( final Set<Integer> messageIds ) {
		final Iterator<ForumQueryResultCache> iter = queryToResult.values().iterator();
		while( iter.hasNext() ) {
			ForumQueryResultCache cachedQueryWrap = iter.next();
			for( Integer messageID : messageIds) {
				if( cachedQueryWrap.isNewsPageBrowsing() && cachedQueryWrap.containsMessage( messageID ) ) {
					//Remove all of the queries related to the news browsing
					removeMessagesFromTheMappings( cachedQueryWrap.getMessages( idToMessageWrap ) );
					iter.remove();
					//Go to the next query
					break;
				}
			}
		}
	}
	
	/**
	 * Allows to remove all news queries
	 */
	private void removeAllNewsQueries() {
		final Iterator<ForumQueryResultCache> iter = queryToResult.values().iterator();
		while( iter.hasNext() ) {
			ForumQueryResultCache cachedQueryWrap = iter.next();
			if( cachedQueryWrap.isNewsPageBrowsing()) {
				//Remove all of the queries related to the news browsing
				removeMessagesFromTheMappings( cachedQueryWrap.getMessages( idToMessageWrap ) );
				iter.remove();
			}
		}
	}
	
	/**
	 * Allows to remove forum navigation queries with the baseMessageID from
	 * the given set and all of the custom search queries and also single
	 * message views if needed.
	 * 
	 * WARNING: This method is not synchronized
	 * 
	 * @param ids the base message ids to consider
	 * @param removeOneMsgViews if true then also all one-message view queries are removed
	 */
	private void removeCustomAndNavBaseQueries( final List<Integer> ids, final boolean removeOneMsgViews ) {
		logger.debug( "Removing the CUSTOM SEARCH queries and NAVIGATION queries " +
					  "with the given base message ids: " + ids + (removeOneMsgViews ?  " and also NEWS queries" : "" ));
		final Iterator<ForumQueryResultCache> iter = queryToResult.values().iterator();
		while( iter.hasNext() ) {
			ForumQueryResultCache cachedQueryWrap = iter.next();
			if( cachedQueryWrap.isCustomBrowsing() || ids.contains( cachedQueryWrap.getBaseMessageID() ) || 
				( removeOneMsgViews && cachedQueryWrap.isOneMessageView() ) ) {
				//Remove all of the queries related to the news browsing
				removeMessagesFromTheMappings( cachedQueryWrap.getMessages( idToMessageWrap ) );
				iter.remove();
			}
		}		
	}
	
	/**
	 * Should be called in case a new forum message is sent
	 * @param message the forum message that was sent
	 */
	public synchronized void sendNewForumMessage( final ShortForumMessageData message ) {
		logger.debug( "A new forum message with the pathID '" + message.messagePathID + "' has been sent, updating the cache" );
		//NOTE: we do not clean up the news results although we could have because the
		//message on the news page could be the parent of this one, so its reply count
		//etc change. This is not a big deal, that data will be just updated later, but
		//what we want here is that the news page works as fast as possible.
		removeCustomAndNavBaseQueries( ShortForumMessageData.getMessageParentIds( message ), false );
	}
	
	/**
	 * Must be called when the forum message is moved.
	 * @param message the forum message that we will move
	 * @param newParent the new parent of the forum message
	 */
	public synchronized void moveForumMessage( final ShortForumMessageData message, final ShortForumMessageData newParent ) {
		logger.debug( "Moving the forum message " + message.messageID + " to the new parent " + newParent.messageID + ", updating the cache" );
		List<Integer> changeSet = ShortForumMessageData.getMessageParentIds( message );
		changeSet.addAll( ShortForumMessageData.getMessageParentIds( newParent ) );
		changeSet.add( newParent.messageID );
		removeCustomAndNavBaseQueries( changeSet, false ); 
	}
	
	/**
	 * Should be called in case a new forum message is updated
	 * @param message the forum message that was updated
	 */
	public synchronized void updateForumMessage( final ShortForumMessageData messageIn ) {
		logger.debug( "A forum message "+messageIn.messageID+" has been updated, updating the cache" );
		
		//First try finding the message in the cache, this is done for safety
		ShortForumMessageData message = messageIn;
		ForumMessageCacheWrapper messageWrap = idToMessageWrap.get( messageIn.messageID );
		if( messageWrap != null ) {
			message = messageWrap.getMessageData();
		}
		
		//Remove the custom search queries and the navigation queries
		removeCustomAndNavBaseQueries( ShortForumMessageData.getMessageParentIds( message ), true );
		
		//If the message is approved, then remove all the news queries
		if( message.isApproved ) {
			removeAllNewsQueries();
		}
	}
	
	/**
	 * Should be called in case a new forum message is deleted
	 * 
	 * NOTE: Takes case about the approved messages
	 * 
	 * @param message the forum message that was deleted
	 */
	public synchronized void deleteForumMessage( final ForumMessageData message ) {
		logger.debug( "A forum message "+ message.messageID +" has been deleted, updating the cache" );
		
		//Delete the cache results for this message and the messages above
		List<Integer> targetMsgIds = ShortForumMessageData.getMessageParentIds( message );
		targetMsgIds.add( message.messageID );
		removeCustomAndNavBaseQueries( targetMsgIds, true );
		
		//NOTE: Here, we do not really clean up the messages below this one
		//it is not so important because they will eventually be cleaned up
		//TODO: It might be possible to find them by using the message path id
		
		//In case the message has replies then there might be approved messages in them also 
		//some of them are cached for the news page but are not cached in navigation queries.
		if( message.numberOfReplies > 0 ) {
			removeAllNewsQueries();
		} else {
			if( message.isApproved ) {
				final Set<Integer> messageIds = new HashSet<Integer>();
				messageIds.add( message.messageID );
				removeNewsQueriesWithTheMessages( messageIds );
			}
		}
	}
	
	/**
	 * Should be called when a forum message file was deleted
	 * @param fileID the id of the file was was removed
	 */
	public synchronized void removeFormMessageFile( final int fileID ) {
		logger.debug("A forum file  " + fileID + " was deleted, updating cache" );
		final Iterator<ForumQueryResultCache> iter = queryToResult.values().iterator();
		while( iter.hasNext() ) {
			ForumQueryResultCache cachedQueryWrap = iter.next();
			if( cachedQueryWrap.containsFile( fileID ) ) {
				//Remove all of the queries related to the news browsing
				removeMessagesFromTheMappings( cachedQueryWrap.getMessages( idToMessageWrap ) );
				iter.remove();
			}
		}
	}
	
	/**
	 * Allows to check if there is an empty space for caching
	 * more queries if not, attempts to do some clean up.
	 * 
	 * WARNING: this method is not synchronized
	 * 
	 * @param numberOfMsgs the number of messages for the new query we want to cache
	 * @return true if we have space for caching more messages
	 */
	private boolean canCacheMoreQueries(final int numberOfMsgs) {
		boolean canCache = true;
		final int newMsgsCount = numberOfMsgs + idToMessageWrap.size();
		
		//Check if after adding more messages the cache capacity will be exceeded.
		if( newMsgsCount > MAX_NUMBER_CACHED_MESSAGES ) {
			logger.warn( "The new query can not be cached without cache clean-up, the " + 
					     "cach is full, it contains " + idToMessageWrap.size() + " messages." );
			//The cache capacity will be exceeded, a clean-up is required
			if( ( lastCachCleanUp + MIN_CACHE_CLEAN_UP_INTERVAL_MILLISEC ) < System.currentTimeMillis() ) {
				//The last cache lean up was long ago
				logger.info( "The last clean-up was done at " + lastCachCleanUp + " milliseconds, initiating new cache clean up" );
				
				//Do the clean up here
				canCache = cacheCleanUp( newMsgsCount - MAX_NUMBER_CACHED_MESSAGES );
				
				//Update the last cache clean-up time
				lastCachCleanUp = System.currentTimeMillis();
			} else {
				//The last clean-up was done recently, the message can not be cached
				logger.warn( "The last clean-up was done recently, at " + lastCachCleanUp + " milliseconds, the new messages can not be cached" );
				canCache = false;
			}
		}
		return canCache;
	}
	
	/**
	 * Does the cache clean up. In case of "numExtraMsgs <= 0" we only aim at removing
	 * one query regardless to the number of messages that it stores in the result.
	 * In case  "numExtraMsgs > 0" we remove as many queries as needed to remove
	 * at least numExtraMsgs messages from the cache.
	 * 
	 * WARNING: this method is not synchronized
	 * 
	 * @param numExtraMsgs the number of messages we have to remove from the pool to 
	 *                      have a successful clean-up 
	 * @return true if the clean-up was successful, i.e. we either removed one cached
	 *              query (in case of numExtraMsgs <= 0) or we removed at least 
	 *              numExtraMsgs messages (in case of numExtraMsgs > 0)
	 */
	private boolean cacheCleanUp( final int numExtraMsgs ) {
		boolean isSuccessful = false;
		
		//Construct the iterator object
		Iterator<ForumQueryResultCache> iter = queryToResult.values().iterator();
		if( numExtraMsgs <= 0 ) {
			//If we just need to remove some old query
			logger.debug("Cleaning up the cache, we only need to remove some cached query");
			while( iter.hasNext() ) {
				ForumQueryResultCache cachedQueryWrap = iter.next();
				if( cachedQueryWrap.isLowPriorityQuery() ) {
					logger.debug("One unimportant query was successfully removed from the cache");
					removeMessagesFromTheMappings( cachedQueryWrap.getMessages( idToMessageWrap ) );
					iter.remove();
					isSuccessful = true;
					break;
				}
			}
		} else {
			//We need to remove a certain amount of messages
			logger.debug("Cleaning up the cache, we need to remove " + numExtraMsgs + " messages");
			int msgsRemoved = 0;
			while( iter.hasNext() ) {
				ForumQueryResultCache cachedQueryWrap = iter.next();
				if( cachedQueryWrap.isLowPriorityQuery() ) {
					final int numberRemovedMsgs = removeMessagesFromTheMappings( cachedQueryWrap.getMessages( idToMessageWrap ) ); 
					logger.debug("An unimportant query and " + numberRemovedMsgs + " of its messages was successfully removed from the cache");
					msgsRemoved += numberRemovedMsgs;
					iter.remove();
					if( msgsRemoved >= numExtraMsgs ) {
						isSuccessful = true;
						break;
					}
				}
			}
		}
		return isSuccessful;
	}
	
	/**
	 * Allows to remove messages from the message pool, or decrement the
	 * message reference counter if the message is used by another query
	 * 
	 * WARNING: this method is not synchronized
	 * 
	 * @param messages the list of messages for the messages that
	 *                   we want to remove, NOT NULL
	 * @return the number of messages that was removed from the message pool
	 */
	private int removeMessagesFromTheMappings( final List<ForumMessageData> messages ) {
		int removedMsgCount = 0;
		
		for(ForumMessageData message : messages ) {
			final int messageID = message.messageID;
			final ForumMessageCacheWrapper msgWrap = idToMessageWrap.get( messageID );
			if( msgWrap != null ) {
				//First decrement the reference counter
				msgWrap.decrementRefCounter();
				//Check if the message is still in use
				if( msgWrap.isNotReferenced() ) {
					//The message is not referenced by any one else, remove it
					ForumMessageCacheWrapper wrapper = idToMessageWrap.remove( messageID );
					if( wrapper != null ) {
						//Notify the message that it is removed from the cache
						wrapper.onMessageCacheRemove();
					}
					//Remove the voters mapping
					idToVoterIds.remove( messageID);
					//Increment the counter for the number of removed messages
					removedMsgCount++;
				}
			} else {
				logger.error("The forum message " + messageID + " is expected to be in the cache but it is not!");
			}
		}
		
		return removedMsgCount;
	}
}
