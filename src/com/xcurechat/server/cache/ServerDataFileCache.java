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
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.server.cache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.FileData;
import com.xcurechat.client.data.ShortFileDescriptor;

/**
 * @author zapreevis
 * This class is used for caching files that the user might want to
 * view in the chat, forum and other site sections.
 * THis class is synchronized.
 */
public class ServerDataFileCache {
	//The number of bytes in on megabyte
	public static final int BYTES_IN_ONE_MEGABYTE = 1048576;
	
	/**
	 * @author zapreevis
	 * This class is used to wrap around the cached file data
	 */
	private class CacheDataWrapper {
		//Stores the last time in milliseconds when this file was retrieved from the cache
		long lastUsedTimeMillisec = System.currentTimeMillis();
		//Stores the approximate amound of data stored in the file descriptor in bytes
		double fileDataSizeInBytes = 0;
		//The files descriptor storing the file data
		FileData fileData = null;
	}
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( ServerDataFileCache.class );
	
	//The name of the cache instance, is used for logging
	private final String cacheName;
	//The maximum amount of data in bytes that this cache is supposed to store
	private final int maxCacheCapacityInBytes;
	//The maximum time interval for which this cache is supposed
	//to store the files after it's last download by the client, in milliseconds
	private final long maxStoreTimeIntervalMillisec;
	//The time interval in milliseconds between the cache clean-ups
	private final long cleanUpTimeIntervalMillisec;
	
	//The last time the files were cleaned-up in milliseconds
	private long lastCleanUpTimeMillisec = System.currentTimeMillis();
	//The current memory in bytes used by the cache data
	private double usedCacheMemoryInBytes = 0;
	//The map from the file id to the file cache wrapper, we use the ordered map for the cache clean-up purposes
	private final Map<Integer, CacheDataWrapper> filesCacheMap = new LinkedHashMap<Integer, CacheDataWrapper>();
	
	/**
	 * The basic constructor
	 * @param maxCapacityMb the maximum amount of data in Mb that this cache is supposed to store
	 * @param maxStoreTimeIntervalMinutes the maximum time interval for which this cache is supposed
	 * to store the files after it's last use by the client, in minutes
	 * @param cleanUpTimeIntervalMinutes the time interval in minutes between the cache clean-ups
	 * @param cacheName the name of the cache instance, used for logging.
	 */
	public ServerDataFileCache( final int maxCapacityMb, final long maxStoreTimeIntervalMinutes,
								final long cleanUpTimeIntervalMinutes, final String cacheName) {
		this.maxCacheCapacityInBytes = BYTES_IN_ONE_MEGABYTE * maxCapacityMb; //1024 * 1024 * maxCapacityMb
		this.maxStoreTimeIntervalMillisec = 60000 * maxStoreTimeIntervalMinutes;
		this.cleanUpTimeIntervalMillisec = 60000 * cleanUpTimeIntervalMinutes;
		this.cacheName = cacheName + " cache: ";
	}
	
	/**
	 * Allows to remove the file from the cache
	 * @param iter the iterator pointing at the current file wrapper that has to be removed
	 * @param fileDataWrapper the file wrapper for the file that has to be removed
	 */
	private void removeFile(final Iterator<CacheDataWrapper> iter, final CacheDataWrapper fileDataWrapper) {
		debug( "The file " + fileDataWrapper.fileData.fileID + " is not needed any more,  removing it from " +
			   "the cache, size: " + fileDataWrapper.fileDataSizeInBytes / BYTES_IN_ONE_MEGABYTE + "Mb" );
		//Remove the file data from the map
		iter.remove();
		//Decrement the memory usage
		usedCacheMemoryInBytes -= fileDataWrapper.fileDataSizeInBytes;
	}
	
	/**
	 * Should be periodically called to initiate the cache clean-up
	 * @param forceCleanUp if true then we do the clean-up even if it is not yet time for it
	 * @param requiredFreeSpaceInBytes if forceCleanUp == true then this defines the amount of memory we need to be freed
	 */
	private synchronized void clean( final boolean forceCleanUp, final double requiredFreeSpaceInBytes ) {
		if( forceCleanUp || ( System.currentTimeMillis() > lastCleanUpTimeMillisec + cleanUpTimeIntervalMillisec ) ) {
			info( "Initiating the cache clean-up");
			final double currentUsedCacheMemory = usedCacheMemoryInBytes;
			
			//Go through the files and do the clean-up of the non-used ones
			final Iterator<CacheDataWrapper> iter = filesCacheMap.values().iterator();
			if( forceCleanUp && requiredFreeSpaceInBytes > 0.0 ) {
				debug("Performing a forced clean-up");
				//If this is a forced clean-up then we remove the files that were added
				//earlier this is simple because we use an ordered map, but we only free
				//as much space as it is required by the second method's argument
				while( iter.hasNext() ) {
					CacheDataWrapper fileDataWrapper = iter.next();
					//Remove the first file in the list
					removeFile( iter, fileDataWrapper );
					//Check if the freed memory is large enough
					if( maxCacheCapacityInBytes - usedCacheMemoryInBytes > requiredFreeSpaceInBytes ) {
						//if we acquired enough free memory then we just stop cleaning
						break;
					}
				}
			} else {
				debug("Performing a scheduled clean-up");
				//If this is not a forced clean up, then we just remove the old files
				final long currentTimeMillisec = System.currentTimeMillis();
				while( iter.hasNext() ) {
					CacheDataWrapper fileDataWrapper = iter.next();
					if( currentTimeMillisec - fileDataWrapper.lastUsedTimeMillisec > maxStoreTimeIntervalMillisec ) {
						//The file was not used for too long, remove it
						removeFile( iter, fileDataWrapper );
					}
				}
			}
			
			//Update the last clean-up time
			lastCleanUpTimeMillisec = System.currentTimeMillis();
			
			info( "The cache clean up is done, freed space is: " +
			 	  ( currentUsedCacheMemory - usedCacheMemoryInBytes ) / BYTES_IN_ONE_MEGABYTE + "Mb" );
		} else {
			debug( "The cache clean up was skipped as the current time is " + System.currentTimeMillis() +
				   " the last clean up time was " + lastCleanUpTimeMillisec +
				   " and the cleaning interval is " + cleanUpTimeIntervalMillisec );
		}
	}
	
	/**
	 * Should be periodically called to initiate the cache clean-up
	 */
	private synchronized void clean() {
		clean( false, 0.0 );
	}
	
	/**
	 * Allows to retrieve the file from the cache by its ID
	 * @param fileID the id of the file to be retrieved
	 * @return the fine data or null if the file is not in the cache
	 */
	public synchronized FileData get( final int fileID ) {
		debug( "Retrieving file " + fileID );
		//Get the file data from the map
		CacheDataWrapper fileDataWrapper = filesCacheMap.get( fileID );
		//Mark the file as being used now and return it
		if( fileDataWrapper != null ) {
			debug( "The file " + fileID + " is in cache" );
			fileDataWrapper.lastUsedTimeMillisec = System.currentTimeMillis();
			return fileDataWrapper.fileData;
		} else {
			info( "The file " + fileID + " is not in cache" );
			return null;
		}
	}
	
	/**
	 * Allows to remove the file from the cache by its ID
	 * @param fileID the id of the file to be removed
	 * @return the removed file data stored in the cache or null if the file was not found
	 */
	public synchronized FileData remove( final int fileID ) {
		debug( "We are asked to remove the file " + fileID + " from the cache" );
		//Remove the file data from the map
		CacheDataWrapper fileDataWrapper = filesCacheMap.remove( fileID );
		//Check if the file was removed, i.e. if it was even present
		if( fileDataWrapper != null ) {
			debug( "The file " + fileID + " is removed from the cache" );
			//Decrement the memory usage
			usedCacheMemoryInBytes -= fileDataWrapper.fileDataSizeInBytes;
			debug( "The removed file " + fileID + " was using " + ( fileDataWrapper.fileDataSizeInBytes / BYTES_IN_ONE_MEGABYTE ) + "Mb of cache" );
			//return the removed file data
			return fileDataWrapper.fileData;
		} else {
			info( "Could not remove the file " + fileID + " from the cache as it is not there" );
			//The file is not in the cache, we just return null
			return null;
		}
	}
	
	/**
	 * Allows to put file to cache, NOTE: the file ID must be set!
	 * @param fileData the file data to be put to cache
	 */
	public synchronized void put( FileData fileData ) {
		//First do the periodic cache cleaning
		clean();
		
		//Check if the provided file is well defined
		if( fileData != null && fileData.fileID != ShortFileDescriptor.UNKNOWN_FILE_ID ) {
			info( "Trying to put the file " + fileData.fileID + " into " );
			if( ! filesCacheMap.containsKey( fileData.fileID ) ) {
				//If the cached file is not in cache:
				
				//1. Create and initialize the cache wrapper
				CacheDataWrapper fileDataWrapper = new CacheDataWrapper();
				fileDataWrapper.fileData = fileData;
				fileDataWrapper.fileDataSizeInBytes = ( fileData.thumbnailData == null ? 0.0 : fileData.thumbnailData.length ) +
													  ( fileData.fileData == null ? 0.0 : fileData.fileData.length );
				debug( "The file " + fileData.fileID + " has size " + fileDataWrapper.fileDataSizeInBytes / BYTES_IN_ONE_MEGABYTE + "Mb");
				
				//2. Check if we are over the maximum memory for the cached files
				if( usedCacheMemoryInBytes + fileDataWrapper.fileDataSizeInBytes > maxCacheCapacityInBytes ) {
					info( "If the file " + fileData.fileID + " is added, then we are over the maximum memory. Try doing the clean-up first " );
					//Try to do the cache clean-up
					clean( true, fileDataWrapper.fileDataSizeInBytes );
					//Check one more time
					if( usedCacheMemoryInBytes + fileDataWrapper.fileDataSizeInBytes > maxCacheCapacityInBytes ) {
						error( "After the forced cache clean-up, we are still over the required maximum " +
							  "cache size, the file " + fileData.fileID + " will not be cached" );
						return;
					}
				}
				
				//3. Add the file to the cache, updating the used cache memory data
				usedCacheMemoryInBytes += fileDataWrapper.fileDataSizeInBytes;
				info("Adding file " + fileData.fileID + " to cache, the new used cache memory is about " + usedCacheMemoryInBytes / BYTES_IN_ONE_MEGABYTE + "Mb" );
				filesCacheMap.put( fileData.fileID, fileDataWrapper );
			} else {
				info( "The file " + fileData.fileID + " is already inside" );
			}
		} else {
			warn( "Trying to put a NULL file or a file with unknown id" );
		}
	}
	
	private void debug( final String text ) {
		logger.debug( cacheName + text );
	}
	
	private void info( final String text ) {
		logger.info( cacheName + text );
	}
	
	private void warn( final String text ) {
		logger.warn( cacheName + text );
	}
	
	private void error( final String text ) {
		logger.error( cacheName + text );
	}
}
