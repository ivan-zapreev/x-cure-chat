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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.search.OnePageViewData;
import com.xcurechat.client.data.search.Top10SearchData;

/**
 * @author zapreevis
 * The cache for the Top10 section of the site, stores some user data objects
 */
public class Top10UserDataCache {
	
	//The maximum index of the cached page for each search type
	public static final int MAXIMUM_CACHED_PAGE_INDEX = 100;
	
	//The interval of time after which the cache will be flushed
	private static final long CACHE_CLEAN_UP_INTERVAL_MILLISEC = 60 * 60 * 1000; 

	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( Top10UserDataCache.class );
	
	//The top10 user data cache
	private final ShortUserDataCache userDataCache = ShortUserDataCache.getInstance( ShortUserDataCache.CacheIds.TOP10_CACHE_ID );
	
	/**
	 * The private constructor
	 */
	private Top10UserDataCache() { }
	
	//The only instance of this class
	private static Top10UserDataCache instance = new Top10UserDataCache();

	/**
	 * Allows to retrieve the only instance of this lass
	 * @return
	 */
	public static Top10UserDataCache getInstance() {
		return instance;
	}
	
	//Stores the mapping between the cache type and the per-page cached data
	public Map<Top10SearchData.SearchTypes, Map<Integer,OnePageViewData<?>>> dataHolder = new HashMap<Top10SearchData.SearchTypes, Map<Integer,OnePageViewData<?>>>();
	//The next time after which the cache will be cleaned up
	private long nextCacheCleanUpMillisec = System.currentTimeMillis() + CACHE_CLEAN_UP_INTERVAL_MILLISEC;
	
	/**
	 * Allows to retrieve the search results data for the given top10 search object
	 * @param searchParams the top10 search data object 
	 * @return the cached data or null if the data is not cached
	 */
	public OnePageViewData<?> getSearchResults( final Top10SearchData searchParams ) {
		final String infoStr = " for TOP10 search: search type " + searchParams.search_type + " page index " + searchParams.pageIndex;
		logger.info("Retrieving user data" + infoStr );
		
		//Flush the cache data if needed
		flushCachedData( false );
		
		//Get the search type specific cached data
		final Map<Integer,OnePageViewData<?>> specificCache;
		synchronized( dataHolder ) {
			specificCache = dataHolder.get( searchParams.search_type );
		}
		
		//Get the page specific cached data for the given search type
		final OnePageViewData<?> pageData;
		if( specificCache != null ) {
			logger.debug("Found the cache for the given search type" + infoStr );
			synchronized( specificCache ) {
				pageData = specificCache.get( searchParams.pageIndex );
			}
		} else {
			logger.debug("Found no cache for the given search type" + infoStr );
			pageData = null;
		}
		
		logger.info( "Found " + ( ( ( pageData != null ) && ( pageData.entries != null ) ) ? pageData.entries.size()  : 0 ) + " user data objects" + infoStr );
		
		return pageData;
	}
	
	/**
	 * Allows to update the ShortUserData object instances with the new prank data
	 * @param userData the user data update
	 */
	public void updateUserDataPrank( final ShortUserData userData ) {
		if( userData != null ) {
			final int userID = userData.getUID();
			logger.debug("Attempting to update the prank for the cached short user data of user " + userID );
			
			//Update the prank if the short user data is cached
			userDataCache.updateCachedUserData( userData, true );
		} else {
			logger.error( "Attempting to update the cached short user data, but the data is null" );
		}
	}
	
	/**
	 * Allows to place new search results into the cache
	 * @param searchParams the search parameters used to obtain the search results
	 * @param searchResults the search resulta corresponding to the search parameters
	 */
	@SuppressWarnings("unchecked")
	public void placeSearchResults( final Top10SearchData searchParams, OnePageViewData<?> searchResults ) {
		final String infoStr = " for TOP10 search: search type " + searchParams.search_type + " page index " + searchParams.pageIndex;
		logger.info("Caching new user data" + infoStr );
		
		if( searchResults != null ) {
			//First get the data storage for this specific search
			Map<Integer,OnePageViewData<?>> specificCache;
			synchronized( dataHolder ) {
				logger.debug( "Retrieving the search results data storage" + infoStr );
				specificCache = dataHolder.get( searchParams.search_type );
				if( specificCache == null ) {
					logger.debug( "The search results data storage does not exist, creating a new one" + infoStr );
					specificCache = new HashMap<Integer,OnePageViewData<?>>();
					dataHolder.put( searchParams.search_type, specificCache );
				}
			}
			
			//Retrieve the page specific data storage for this type data storage
			synchronized(specificCache) {
				OnePageViewData<?> pageData = specificCache.get( searchParams.pageIndex );
				if( pageData == null ) {
					if( searchParams.pageIndex < MAXIMUM_CACHED_PAGE_INDEX  ) {
						//Update the short user data objects if this is the search results storing them
						if( ( searchResults.entries != null ) && ( searchResults.entries.size() > 0 ) &&
							( searchResults.entries.get(0) instanceof ShortUserData ) ) {
							//Cast the reference of the object to the known now type
							List<ShortUserData> entries = (List<ShortUserData>) searchResults.entries;
							//Iterate through the user data entries and place them into the cache
							//Substituting the given entries with the ones from the cache
							for( int index = 0; index < entries.size(); index++ ) {
								entries.set( index, userDataCache.getCachedUserData( entries.get( index ) ) );
							}
						}
						//Place the (possibly updated) search results into the cache
						specificCache.put( searchParams.pageIndex, searchResults );
					} else {
						logger.info("Caching of the new user data is cancelled, the page index is too high" + infoStr );
					}
				} else {
					logger.info("Caching of the new user data is not needed, this data is already cached" + infoStr );
				}
			}
		} else {
			logger.error("Unable to store the results in the cache, because the results are null!" + infoStr );
		}
	}
	
	/**
	 * Allows to clean the entire cache up, should be called
	 * periodically to re-fill the cache with the new data.
	 * @param isForce if true then the cache clean up is forced
	 */
	public void flushCachedData( final boolean isForce ) {
		logger.info( "Checking the TOP10 user data cache for the need of being cleaned up, is forced = " + isForce );
		synchronized( this ) {
			final long currentTimeMillised = System.currentTimeMillis();
			if( isForce || ( nextCacheCleanUpMillisec > currentTimeMillised ) ) {
				//If it is time to clean-up the cache
				logger.debug("Starting the TOP10 user data cache clean up" );
				synchronized( dataHolder ) {
					//Unregister all the instances of the ShortUserData class in the internal cache
					Set<Top10SearchData.SearchTypes> vacheTypes = dataHolder.keySet();
					for( Top10SearchData.SearchTypes cacheType : vacheTypes ) {
						flushCachedData( cacheType );
					}
					//Note: we do not remove the sub-cached from the mappings, we only remove their data
				}
				logger.debug("The TOP10 user data cache clean up is complete" );
			} else {
				//It is not time yet to do the clean up
				logger.debug( "The TOP10 user data cache is cancelled, next clean up time is " +
							  nextCacheCleanUpMillisec + " now it is " + currentTimeMillised );
			}
		}
	}
	
	/**
	 * Allows to clean the specific search type cache up.
	 */
	@SuppressWarnings("unchecked")
	public void flushCachedData( final Top10SearchData.SearchTypes cacheType ) {
		logger.info("Attempting to clean up the TOP10 cache of type: " + cacheType );
		
		//Retrieve the specific cache
		final Map<Integer,OnePageViewData<?>> specificCache;
		synchronized( dataHolder ) {
			specificCache = dataHolder.get( cacheType );
		}
		
		//If the cache exists then clean it up
		if( specificCache != null ) {
			synchronized( specificCache ) {
				Iterator<OnePageViewData<?>> pagesIterator = specificCache.values().iterator();
				while( pagesIterator.hasNext() ) {
					OnePageViewData<?> page = pagesIterator.next();
					if( ( page.entries != null ) && ( page.entries.size() > 0 ) &&
						( page.entries.get(0) instanceof ShortUserData ) ) {
						//Cast the reference of the object to the known now type
						List<ShortUserData> entries = (List<ShortUserData>) page.entries;
						//Iterate through the user data entries and release them in the cache
						for( int index = 0; index < entries.size(); index++ ) {
							userDataCache.releaseCachedUserData( entries.get( index ) );
						}
					}
				}
				//Remove all of the cached pages
				specificCache.clear();
			}
			logger.info( "The TOP10 user data cache of type: " + cacheType + " was cleaned" );
		} else {
			logger.info( "The TOP10 user data cache of type: " + cacheType + " is not cleaned as it is not present" );
		}
	}
	
	/**
	 * Allows to clean the user registrations cache.
	 */
	public void flushUserRegistrationsCachedData( ) {
		flushCachedData( Top10SearchData.SearchTypes.TOP_REGISTRATIONS_SEARH_TYPE );
	}
	
	/**
	 * Allows to clean the user visits cache.
	 */
	public void flushUserVisitsCachedData( ) {
		flushCachedData( Top10SearchData.SearchTypes.TOP_USER_VISITS_SEARCH_TYPE );
	}
	
	/**
	 * Allows to clean the user files cache.
	 */
	public void flushUserFilesCachedData( ) {
		flushCachedData( Top10SearchData.SearchTypes.TOP_USER_FILES_SEARH_TYPE );
	}
}
