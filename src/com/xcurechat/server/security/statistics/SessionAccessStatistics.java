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
 * The server-side security package for access statistics.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.server.security.statistics;

import org.apache.log4j.Logger;

/**
 * @author zapreevis
 * This class is supposed to help out preventing intrusion by guessing login-session mappings.
 * Here we observ the performance every other time the session is validated. The statistics is
 * gathered over the period defined by MAX_OBSERVATION_FRAME_IN_MINUTES, after this period it
 * is reset. Hopefully this will allow to prevet session guessing by a third party and also
 * floods. Note that, if the number of session validation requests goes above
 * 2*MAX_ACCESS_NUMBER within MAX_OBSERVATION_FRAME_IN_MINUTES or if
 * the validation rate per minute goed above 2*MAX_ACCESS_NUMBER_PER_MINUTE,
 * then we assume that we are under an attacked.
 */
public class SessionAccessStatistics {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( SessionAccessStatistics.class );
	
	//Maximum number of session validations withing pre-defined time frame
	private static final long MAX_NUMBER_SESSION_ACCESSES = 150;
	//The number of invalid login/session validations, the real value is this * 2
	private static final long MAX_NUMBER_INVALID_VALIDATIONS = 3;
	private final long MAX_ACCESS_NUMBER;
	
	//Maximum number of session validations per minute
	private static final double MAX_NUMBER_SESSION_ACCESSES_PER_MINUTE = 60;
	private static final double MAX_NUMBER_INVALID_VALIDATIONS_PER_MINUTE = 15;
	private final double MAX_ACCESS_NUMBER_PER_MINUTE;

	
	//Maximum period of time over which observe the statistics 
	private static final double MAX_OBSERVATION_FRAME_IN_MINUTES_ACCESS = 5.0; //Just five minutes
	private static final double MAX_OBSERVATION_FRAME_IN_MINUTES_VALIDATIONS = 30.0; //Just 30 minutes
	private final double MAX_OBSERVATION_FRAME_IN_MINUTES;

	//Minimum period of time over which observe the statistics 
	private static final double MIN_OBSERVATION_FRAME_IN_MINUTES = 0.2;
	
	//Millis in one day 24*60*60*1000
	private static final long NUMBER_MILLIS_IN_ONE_DAY = 86400000;
	//Millis in one minute 60*1000
	private static final long NUMBER_MILLIS_IN_ONE_MINUTE = 60000;
	
	//True if we did validate the data last time
	private boolean validatedLastTime;
	
	//True if the user's session is blocked for the next time frame
	private boolean isBlockedNextTimeFrame;
	
	private long totalNumberValidations;
	private long lastValidationInMillis;
	private double totalTimeMinutes;
	
	//True if this statistics is for the logged in user
	private final String debugPrefix;
	
	//The id of the user we perform statistics validation for
	private int userID;
	
	/**
	 * Represents statistical objects for the number of session accesses
	 * and also the number of wrong logins/session validations
	 * @param isSessionStatistics if true then this object represents session access
	 * statistics, but not the session user login/sessionId validation statistics.
	 * @param userID the id of the user we perform statistics validation for,
	 * the latter is used only for the logging purpose so far
	 */
	public SessionAccessStatistics(boolean isSessionStatistics, final int userID ) {
		this.userID = userID;
		
		if( isSessionStatistics ){
			debugPrefix = "Session Access statistics: ";
			MAX_ACCESS_NUMBER = MAX_NUMBER_SESSION_ACCESSES;
			MAX_ACCESS_NUMBER_PER_MINUTE = MAX_NUMBER_SESSION_ACCESSES_PER_MINUTE;
			MAX_OBSERVATION_FRAME_IN_MINUTES = MAX_OBSERVATION_FRAME_IN_MINUTES_ACCESS;
		} else {
			debugPrefix = "Faulty login use statistics: ";
			MAX_ACCESS_NUMBER = MAX_NUMBER_INVALID_VALIDATIONS;
			MAX_ACCESS_NUMBER_PER_MINUTE = MAX_NUMBER_INVALID_VALIDATIONS_PER_MINUTE;
			MAX_OBSERVATION_FRAME_IN_MINUTES = MAX_OBSERVATION_FRAME_IN_MINUTES_VALIDATIONS; 
		}
		initialize();
	}
	
	/**
	 * Allows to set/re-set the user ID, the latter is used only for the logging purpose so far
	 * @param userID the user ID
	 */
	public void setUserID( final int userID ) {
		logger.info(debugPrefix + "Re-setting the user ID from " + this.userID + " to " + userID);
		this.userID = userID;
	}
	
	private void initialize() {
		logger.debug(debugPrefix + "Re-initialization of the session validator.");
		validatedLastTime = false;
		totalNumberValidations = 1;
		lastValidationInMillis = System.currentTimeMillis();
		totalTimeMinutes = MIN_OBSERVATION_FRAME_IN_MINUTES;
		isBlockedNextTimeFrame = false;
	}
	
	private void updateFrameTime(){
		//First we update the statistics, get the current time
		final long currentValidationInMillis = System.currentTimeMillis();
		//Get the delta between the updates
		long delta  = currentValidationInMillis - lastValidationInMillis;
		if( delta < 0 ){
			//if the new day started then we want to add a 24 hour shift
			delta += NUMBER_MILLIS_IN_ONE_DAY;
		}
		//Update the total time from the first validation
		totalTimeMinutes += ( (double) delta ) / NUMBER_MILLIS_IN_ONE_MINUTE;
		
		if( totalTimeMinutes > MAX_OBSERVATION_FRAME_IN_MINUTES ) {
			logger.info(debugPrefix + "Starting new validation time frame.");
			//Reset all data, mark that there was no validation last
			//time. The latter is done inside the initialize method.
			initialize();
		} else {
			if( totalTimeMinutes < 0.0 ) {
				logger.error(debugPrefix + "PANIC: the current validation time is negative: " + totalTimeMinutes + " minutes!");
				logger.info(debugPrefix + "Forcing the reinitialization of the statistics for user " + userID + " since the validation time was negative.");
				initialize();
			} else {
				//Update the last validation time
				lastValidationInMillis = currentValidationInMillis;
			}
		}
	}
	
	/**
	 * @return the time (in minutes) left until the block will be released
	 */
	public synchronized double getRemainingBlockingTime() {
		//Return the difference between the time spent in the frame and the frame duration.
		return MAX_OBSERVATION_FRAME_IN_MINUTES - totalTimeMinutes;
	}
	
	/**
	 * Checks if the access statistics recomments to block the access
	 * @return true if the statistics recommends to block the access
	 */
	public synchronized boolean isAccessBlocked() {
		//Update the curent time, spent since the present time frame started
		updateFrameTime();
		
		//Return the current blocking result
		return isBlockedNextTimeFrame;
	}
	
	/**
	 * In this method we actually do not do validation every time it is called 
	 */
	public synchronized void updateCounter() {
		logger.info(debugPrefix + "Initializing the attack detection sequence.");
		
		if( ! validatedLastTime ) {
			//If the last time we did not actually validate the data, then may be do it now
			validatedLastTime = true;
			
			//Update the current time, spent since the present time frame started
			updateFrameTime();
			
			//If we are within the time frame, do the checks
			if( ! isBlockedNextTimeFrame ) {
				
				//Update the number of validations so far
				++totalNumberValidations;
				
				//Print some extended debug information
				if( logger.isDebugEnabled() ) {
					printDebugSummary();
				}
				
				//Check the current validation rate is too high or we used all available validations for the time frame
				isBlockedNextTimeFrame = ( totalNumberValidations > MAX_ACCESS_NUMBER ) ||
										( ( totalNumberValidations / totalTimeMinutes ) > MAX_ACCESS_NUMBER_PER_MINUTE );
				if( isBlockedNextTimeFrame ) {
					//If we block the session then we put the time spent in this session to zero
					//This allows us to block user access for the next observation frame
					totalTimeMinutes = 0;
				}
			}
		} else {
			//Mark that this time we did not do data validation because we did it the last time
			validatedLastTime = false;
		}
		
		if( logger.isInfoEnabled() ) {
			printInfoSummary();
		}
	}
	
	private void printDebugSummary(){
		//Do some debug logging
		logger.debug( debugPrefix + "#validations: " + totalNumberValidations + ", time interval: " + totalTimeMinutes + " minute(s)");
		logger.debug( debugPrefix + "Number of validations, Current: " + totalNumberValidations + ", Maximum: " + MAX_ACCESS_NUMBER );
		logger.debug( debugPrefix + "Validation rate, Current: " + ( totalNumberValidations / totalTimeMinutes ) + ", Maximum: " + MAX_ACCESS_NUMBER_PER_MINUTE );
	}
		
	private void printInfoSummary(){
		if( isBlockedNextTimeFrame ) {
			logger.info(debugPrefix + "There seems to be an attack, and we just ignore " + 
						"the attacker for the next " + getRemainingBlockingTime() + " minute(s)");
		} else {
			if( validatedLastTime ){
				logger.info(debugPrefix + "There seems to be no attack, working as planned");
			} else {
				logger.info(debugPrefix + "The attack detection was skipped, we skip every other check");
			}
		}
	}
}
