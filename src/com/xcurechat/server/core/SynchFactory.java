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
 * The server-side RPC package.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.server.core;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Date;

import org.apache.log4j.Logger;

import com.xcurechat.server.security.statistics.SessionAccessStatistics;

/**
 * @author zapreevis
 * This class issues synchronization objects for user data.
 * The methods of this class are synchronized!
 */
public final class SynchFactory {
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( SessionAccessStatistics.class );
	
	//The number of milliseconds between clean ups of the StatisticsSecurityManager
	public static final long MIN_CLEAN_UP_INTERVAL_MILLISEC = 1800000; //30*60*1000
	
	//The map that will store the mapping from data ID to synchronization objects
	private final Map<Integer, Object> dataIDToSynchObjMap = new HashMap<Integer, Object>();  
	
	//The map that stores the mapping from data ID to the number of methods that are still 
	//using this synchronization object, i.e. took the object but did not put it back
	private final Map<Integer, Integer> dataIDToInUseMap = new HashMap<Integer, Integer>();
	
	//Stores the time of the last clean up
	private Date lastCleanUpTime = new Date();
	
	//The name of this factory object instance, e.g. "users" for the factory that issues the 
	private final String factoryName;
	
	/**
	 * The simple constructor of the factory object
	 * @param factoryName The name of this factory object instance,
	 * 						e.g. "users" for the factory that issues
	 *						the synchronization objects for users
	 *						This name is only used for logging purposes. 
	 */
	private SynchFactory(final String factoryName){
		this.factoryName = factoryName;
	}
	
	/**
	 * Get the synchronization object for the data object that needs synchronous access
	 * @param dataID the unique ID of some data object
	 * @return the data synchronization object, never null!
	 */
	public synchronized Object getSynchObject( final int dataID ){
		//Get the synchronization object from the existing mappings
		Object synchObject = dataIDToSynchObjMap.get( dataID );
		Integer inUseCunt = dataIDToInUseMap.get( dataID );
		if( synchObject == null ){
			//If there is no synchronization object yet then create them
			synchObject = new Object();
			inUseCunt = 0;
			//Put the mappings for them
			dataIDToSynchObjMap.put( dataID , synchObject );
		}
		//Increase the in use counter
		dataIDToInUseMap.put( dataID , inUseCunt + 1 );
		
		if( logger.isDebugEnabled() ) {
			logger.debug( "Getting synchronization object for " + factoryName + ": " + dataID +
							", the new reference count: " + dataIDToInUseMap.get( dataID ) );
		}
		
		//Return the synchronization object
		return synchObject;
	}
	
	/**
	 * Removes the synchronization for the given data ID from the mapping
	 * @param dataID the unique data ID
	 */
	public synchronized void releaseSynchObject( final int dataID  ){
		if( dataIDToSynchObjMap.keySet().contains( dataID ) ) {
			//Decrease the counter
			dataIDToInUseMap.put( dataID , dataIDToInUseMap.get( dataID ) - 1 );

			if( logger.isDebugEnabled() ) {
				logger.debug( "Releasing synchronization object for " + factoryName + ": " + dataID +
								", new reference count: " + dataIDToInUseMap.get( dataID ) );
			}
		} else {
			logger.error("Trying to release synchronization object for " + factoryName + ": " +
							dataID + "but the mapping does not exist.");
		}
	}
	
	/**
	 * This metho removes all the synchronization object
	 * mappings with the "in use" counters equal to zero.
	 */
	public synchronized void cleanUp() {
		logger.info( "Starting clean up of the synchronization mappings");
		if( ( System.currentTimeMillis() - MIN_CLEAN_UP_INTERVAL_MILLISEC ) > lastCleanUpTime.getTime() ) {
			Iterator<Integer> dataIDsIter = dataIDToSynchObjMap.keySet().iterator();
			while( dataIDsIter.hasNext() ){
				int dataID = dataIDsIter.next();
				if( dataIDToInUseMap.get( dataID ) == 0 ) {
					logger.debug( "Removing synchronization object mappings for " + factoryName + ": " + dataID );
					dataIDsIter.remove();
					dataIDToInUseMap.remove( dataID );
				}
			}
			lastCleanUpTime = new Date();
		}
	}
	
	//Some constant factory names that are used through out the implementation
	public static final String USER_FACTORY_NAME = "user";
	public static final String ROOM_ACCESS_FACTORY_NAME = "'user access' for room";
	public static final String AVATAR_PRANK_FACTORY_NAME = "user avatar prank";
	
	//The object to synchronize the static methods of this class
	private static final Object synchObj = new Object();
	
	//This map stores the mapping from the factory names to the corresponding factory objects
	private static Map<String, SynchFactory> factoryNameToFactoryObject = new HashMap<String, SynchFactory>(); 

	public static SynchFactory getSynchFactory( final String factoryName ) {
		synchronized( synchObj ) {
			SynchFactory factoryObj = factoryNameToFactoryObject.get( factoryName );
			if( factoryObj == null ) {
				factoryObj = new SynchFactory( factoryName );
				factoryNameToFactoryObject.put( factoryName, factoryObj );
			}
			return factoryObj; 
		}
	}
}
