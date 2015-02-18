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

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ForumMessageData;

/**
 * @author zapreevis
 * This is the simple forum message wrapper for storing the message and the query to message references counter.
 * Manages caching of the short user data descriptors for the message sender and last reply sender
 */
public class ForumMessageCacheWrapper {
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( ForumMessageCacheWrapper.class );
	
	//The short user data cache
	private static ShortUserDataCache userDataCache = ShortUserDataCache.getInstance( ShortUserDataCache.CacheIds.FORUM_CACHE_ID ); 
	
	//Contains the forum message data
	private ForumMessageData messageData;
	
	//Contains the query to message reference counter, is initialized with one, i.e. there is alway one reference
	private int queryRefCounter = 1;
	
	/**
	 * Construct the message wrapper. Does NOT do the initial increment of the reference counter.
	 * Update the message sender and last reply sender with the cached short user data object
	 * @param newMessageData the message data object
	 */
	public ForumMessageCacheWrapper( final ForumMessageData newMessageData ) {
		logger.debug("Adding new message " +  newMessageData.messageID + " to the cache");
		//Store the message
		messageData = newMessageData;
		
		//Cache the short user data for the sender and the last reply user
		messageData.senderData = userDataCache.getCachedUserData( messageData.senderData );
		if( messageData.numberOfReplies > 0 ) {
			messageData.lastReplyUser = userDataCache.getCachedUserData( messageData.lastReplyUser );
		}
	}
	
	/**
	 * Allows to update the message data, this is needed for the case when a message remains cached
	 * e.g. in the news query but it is updated in the navigation browsing query. The method does 
	 * put the new message in place of the old one only if the old and new message ids are the same
	 * Also increments the reference counter and takes care of proper handling the cached short user data.
	 * @param newMessageData the message to place instead of the old one 
	 */
	public synchronized void registerNewMessageReference( final ForumMessageData newMessageData ) {
		if( this.messageData.messageID == newMessageData.messageID ) {
			logger.debug("Registering the new message reference for message " +  newMessageData.messageID + " in the cache");
			
			//Return the short user data references for the last reply sender
			if( messageData.numberOfReplies > 0 ) {
				userDataCache.releaseCachedUserData( messageData.lastReplyUser );
			}
			//Updating the short user data cache with the new sender data
			userDataCache.updateCachedUserData( newMessageData.senderData );
			//This way messageData.senderData got updated but the object is still from cache
			//This is what we want to preserve, and thus we copy it into the new message data
			newMessageData.senderData = messageData.senderData;
			//After that we substitute the old message data with the new message data
			messageData = newMessageData;
			//And register the new last reply sender if any
			if( messageData.numberOfReplies > 0 ) {
				messageData.lastReplyUser = userDataCache.getCachedUserData( messageData.lastReplyUser );
			}
			
			//Increment the reference counter
			queryRefCounter +=1; 
			logger.debug("Incrementing the reference counter for message " +
						  getMessageData().messageID + " the new value is " + queryRefCounter );
		} else {
			logger.error( "Trying to update forum message but the old message id " +
						  messageData.messageID + " is different from the new message id " +
						  newMessageData.messageID );
		}
	}
	
	/**
	 * Allows to get the id of the wrapped forum message
	 * @return the id of the wrapped forum message
	 */
	public synchronized int getMessageID() {
		return getMessageData().messageID;
	}
	
	/**
	 * Allows to detect if the given message is being referenced by queries 
	 * @return true if the contained message is nor referenced by any forum query 
	 */
	public synchronized  boolean isNotReferenced() {
		return queryRefCounter <= 0;
	}
	
	/**
	 * Must be called before the message is being fully removed from the message cache
	 */
	public synchronized void onMessageCacheRemove() {
		//Unregister the sender and last reply sender references
		userDataCache.releaseCachedUserData( messageData.senderData );
		if( messageData.numberOfReplies > 0 ) {
			userDataCache.releaseCachedUserData( messageData.lastReplyUser );
		}
	}
	
	/**
	 * Allows to decrement the reference counter, is synchronized
	 */
	public synchronized void decrementRefCounter() {
		queryRefCounter -=1;
		
		logger.debug("Decrementing the reference counter for message " +
			  	  getMessageData().messageID + " the new value is " + queryRefCounter );
		
		if( queryRefCounter < 0 ) {
			queryRefCounter = 0;
			logger.error("The message " + getMessageData().messageID + " query referene counter is negative, resetting!" );
		}
	}
	
	/**
	 * @return the messageData
	 */
	public synchronized ForumMessageData getMessageData() {
		return messageData;
	}
}
