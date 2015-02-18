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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ShortUserData;

/**
 * @author zapreevis
 * This class represents a cache that stores short user data,
 * that gets dynamically updated and an be re-used. This class
 * is a singleton. This class is synchronized.
 */
public class ShortUserDataCache {
	
	//These are the allowed cache ids
	public enum CacheIds { FORUM_CACHE_ID, TOP10_CACHE_ID }
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( ShortUserDataCache.class ); 
			
	/**
	 * The private constructor
	 */
	private ShortUserDataCache() {}
	
	//The only instance of this class
	private static Map<CacheIds, ShortUserDataCache> instances = new HashMap<CacheIds, ShortUserDataCache>();
	
	/**
	 * Allows to retrieve the only instance of the cache for the given cache ID
	 * @param cacheId the id of the cache we want to retrieve
	 * @return the only instance of the given cache
	 */
	public static ShortUserDataCache getInstance(final CacheIds cacheId) {
		ShortUserDataCache instance;
		synchronized( instances ) {
			instance = instances.get( cacheId );
			if( instance == null ) {
				instance = new ShortUserDataCache();
				instances.put( cacheId, instance);
			}
		}
		return instance;
	}
	
	/**
	 * @author zapreevis
	 * This class stores the short user data and the counter for the data references.
	 * I.e. the number of times the user data object is referenced by components.
	 */
	private class ShortUserDataWrapper {
		int refCounter = 0;
		ShortUserData userData = null;
	}
	
	//Stores the mapping between the ids of the reference user data objects and the user data wrappers
	private Map<Integer,ShortUserDataWrapper> userIdToShortDataWrap = new HashMap<Integer,ShortUserDataWrapper>();
	
	/**
	 * Allows to find the short user data with the same user id, but cached,
	 * and copy the data from the argument object into the cached one.
	 * @param userData the user data object that we want to update the
	 * 					corresponding cached object from.
	 */
	public synchronized void updateCachedUserData( final ShortUserData userData ) {
		updateCachedUserData( userData, false );
	}
	
	/**
	 * Allows to find the short user data with the same user id, but cached,
	 * and copy the data from the argument object into the cached one
	 * @param userData the user data object that we want to update the
	 * 					corresponding cached object from.
	 * @param isOnlyPrank if true then we only update the prank data but not the entire data
	 */
	public synchronized void updateCachedUserData( final ShortUserData userData, final boolean isOnlyPrank ) {
		if( userData != null ) {
			final int userID = userData.getUID();
			logger.debug( "Attempting to update the cached short user data for user " + userID +
					      ( isOnlyPrank? ", we only want to update the prank data" : "" ) );
			//Try to get the object from cache
			ShortUserDataWrapper cachedUserDataWrap = userIdToShortDataWrap.get( userID );
			if( cachedUserDataWrap != null ) {
				//The object is cached, update the object with the latest data
				logger.debug( "There is cached short user data for user " + userID + ", updating it" );
				if( isOnlyPrank ) {
					//Only update the prank data
					userData.copySpoilerTo( cachedUserDataWrap.userData );
				} else {
					//Update the entire data
					userData.copyTo( cachedUserDataWrap.userData );
				}
			} else {
				logger.warn("There is no cached short user data for user " + userID );
			}
		} else {
			logger.error( "Attempting to update the cached short user data, but the data is null" );
		}
	}
	
	/**
	 * This method is needed for retrieving the cached user data from
	 * the cache and also for placing it there if it is not cached yet.
	 * WARNING: It is up to the class users how full/complete are the
	 * provided user data object. The latter is not checked here. 
	 * @param userData the user data object that we want to cache if it
	 * 					is not cached yet, or for which we want to get
	 *					the same data but in the cached object. The idea
	 *					is to actually use as few ShortUserData object
	 * 					for a single user as possible and always re-used
	 *					the cached ones.
	 * @return the same object if the corresponding used does not have
	 * 			short data cached yet or the equivalent object but from
	 *			the cache in the latter case the data from the argument
	 *			is copied into the cached ShortUserData class for data
	 * 			freshness.  
	 */
	public synchronized ShortUserData getCachedUserData( ShortUserData userData ) {
		ShortUserData result = null;
		if( userData != null ) {
			final int userID = userData.getUID();
			logger.debug("Attempting to get the cached short user data for user " + userID );
			//Try to get the object from cache
			ShortUserDataWrapper cachedUserDataWrap = userIdToShortDataWrap.get( userID );
			if( cachedUserDataWrap == null ) {
				//The object is not yet cached, create a cache entry
				logger.debug( "There is no cached short user data for user " + userID + ", adding new" );
				cachedUserDataWrap = new ShortUserDataWrapper();
				cachedUserDataWrap.userData = userData;
				userIdToShortDataWrap.put( userID, cachedUserDataWrap );
			} else {
				//The object is cached, update the object with the latest data
				logger.debug( "There is cached short user data for user " + userID + ", updating it" );
				userData.copyTo( cachedUserDataWrap.userData );
			}
			//The wrapper object is there, increase the reference counter
			cachedUserDataWrap.refCounter += 1;
			logger.debug( "Increasing reference cunter for the short used data of user " +
						   userID + ", the new value is " + cachedUserDataWrap.refCounter );
			result = cachedUserDataWrap.userData;
		} else {
			logger.error( "Attempting to get the cached short user data, but the data is null" );
		}
		//Return the cached object or null
		return result;
	}
	
	/**
	 * Allows to "release" short user data object. This means that we say
	 * that one reference to the cached object can be removed. When there are no
	 * references left, the object can be removed from the cache. 
	 * @param userData the user object for which the reference is not needed any more
	 */
	public synchronized void releaseCachedUserData( ShortUserData userData ) {
		if( userData != null ) {
			final int userID = userData.getUID();
			logger.debug("Attempting to release the cached short user data for user " + userID );
			//Try to get the object from cache
			ShortUserDataWrapper cachedUserDataWrap = userIdToShortDataWrap.get( userID );
			if( cachedUserDataWrap == null ) {
				//The object is not cached
				logger.error("The short user data for user " + userID + " is not cached!");
			} else {
				//The object is cached
				if( cachedUserDataWrap.userData != userData ) {
					//If the cached object is different from the returned one
					//Normally this should not be happening!
					logger.warn( "The short user data for user " + userID +
								 " is cached but stores another ShortUserData object!");
				}
				//Decrease the reference count
				cachedUserDataWrap.refCounter -= 1;
				logger.debug( "Decreasing reference counter for the short used data of user " +
								userID + ", the new value is " + cachedUserDataWrap.refCounter );
				//Check the current reference count, may be it is time to remove
				//the object from cache, if there is not more references left
				if( cachedUserDataWrap.refCounter <= 0  ) {
					logger.debug( "The short used data of user " + userID +
								  " is not referenced any more, we are removing it from cache" );
					userIdToShortDataWrap.remove( userID );
				}
			}
		} else {
			logger.error( "Attempting to release the cached short user data, but the data is null" );
		}
	}
}
