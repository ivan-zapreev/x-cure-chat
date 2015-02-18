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
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.server.security.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.PrivateMessageData;
import com.xcurechat.client.data.ChatMessage;

import com.xcurechat.client.rpc.exceptions.AccessBlockedException;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This class is responsible for tracking users sending messages
 * and determining whether the user if sending too many messages
 * or is typing unbelievably fast. So we prevent the SPAM messages 
 * and users who abuse the chat system.
 */
public class MessageSendAbuseFilter {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( MessageSendAbuseFilter.class );
	
	//This is the name to which we bind the message filter for every servlet's session
	public static final String USER_MESSAGE_FILTER_SESSION_ATTRIBUTE = "xcure.chat.user.servlet.message.filter";

	//The regular expression pattern for the reply line prefix
	public static final String REPLY_LINE_PREFIXED_REGEXP = "^[\\" +
															CommonResourcesContainer.SINGLE_PREFIX_SYMBOL + "]{" +
															CommonResourcesContainer.REPLY_LINE_PREFIX.length() + "}.*$\\n";
	
	//The pattern for matching quoted lines
	private final static Pattern quotedLinePattern = Pattern.compile( REPLY_LINE_PREFIXED_REGEXP, Pattern.MULTILINE );
	
	/**
	 * The maximum speed in sympold/per minute that we allows. See
	 * http://imlocation.wordpress.com/2007/12/05/how-fast-do-people-type/
	 * There the statistics says:
	 * --------------------------------------------------------------------
	 * Mean speed = 40 WPM = 240 characters/minute
	 * Median = 38 WPM = 228 characters/minute
	 * Standard Deviation = 16.7-WPM = 100 characters/minute
	 * Out of the three thousand four hundred and seventy five applicants,
	 * only the top 5% of applicants could manage 70 WPM or higher.
	 * --------------------------------------------------------------------
	 * 70PM is (I guess) around 420 symbols per minute. This is indeed for
	 * English alphabet, but I guess for Russian and or German the the values
	 * should be just smaller. Now Since only 5% of people can type faster,
	 * we set this as an allowed maximum of typing speed for our site
	 */
	public static final int MAX_SYMBOLS_PER_MINUTE = 420;
	public static final double MAX_SYMBOLS_PER_MILLISECOND = ( (double) MAX_SYMBOLS_PER_MINUTE ) / 60000.0;
	
	/**
	 * Well, I guess this is as many messages as I would send
	 * After all sending a message every 6 seconds is pretty fast
	 * especially if you are trying to write something that has sense
	 * So, we find it good if a user send messages every 6 seconds
	 * i.e. 6000 milliseconds
	 */
	public static final int MIN_TIME_BETWEEN_MESSAGES_MILLISEC = 6000;
	
	/**
	 * This is the maximum number or bad points one can get after the
	 * running the "relatively sequential" send message filter. Violating
	 * either the message send rate or the speed of typing each gives
	 * you some bad points. The filter is called "relatively sequential"
	 * because each faulty attempt adds some points to the bad send msg
	 * points counter and each good attempt reduces it, but the minimum
	 * counter value will be zero.
	 */
	public static final int MAX_NUM_BAD_SEND_MSG_POINTS = 4;
	
	//The id of the user for which we filter the messages
	private final int userID;
	
	//The time in milliseconds since the user send the last message
	private long lastMessageTimeMillis;
	
	//The current number of bad send message attempts, min = 0, max = MAX_NUM_BAD_SEND_MSG_ATTEMPTS
	private int currBadSendMsgPoints = 0;
	
	private MessageSendAbuseFilter(final int userID) {
		logger.info( "Initializing the send-message filter for user " + this.userID );
		this.userID = userID;
		this.lastMessageTimeMillis = System.currentTimeMillis() - MIN_TIME_BETWEEN_MESSAGES_MILLISEC;
	}
	
	/**
	 * Allows to get the id of the user for which this message filter was created
	 * @return the id of the user
	 */
	public int getUserID() {
		return userID;
	}
	
	/**
	 * Allows to determine if the user sends messages too often or is typing too fast
	 * @param message the simple offline (private) message object
	 * @param isAReply if true then this is a reply message, then we do not take the title into acount
	 * @throws AccessBlockedException if it is detected that the user is abusing the system
	 */
	public void validateNewOfflineMessage( PrivateMessageData message, final boolean isAReply ) throws AccessBlockedException {
		String fullMessageText;
		if( isAReply ) {
			logger.debug("Initiating the spam filter for the offline mesage reply");
			//If this is a reply message then we do not consider the message title
			//and we try to only quess the new message text, but skip the cited text.
			//This check is not exact, it can be frauded, yet it is better then nothing.
			fullMessageText = message.getMessageBody();
			if( fullMessageText != null ) {
				logger.debug("The offline mesage reply has a non null body");
				//Remove all the quoted lines
				Matcher matcher = quotedLinePattern.matcher( fullMessageText );
				fullMessageText = matcher.replaceAll( "" );
				logger.debug("The offline message, excluding the cited part has length: " + fullMessageText.length() + ", the remaining text is: " + fullMessageText );
			}
		} else {
			logger.debug("Initiating the spam filter for the mesage reply");
			//This is a new message so we want to take all the text into account
			//First check that the there is no send-message abuse here
			fullMessageText = message.getMessageTitle();
			if( fullMessageText == null ) {
				fullMessageText = message.getMessageBody();
			} else {
				fullMessageText += message.getMessageBody();
			}
		}
		logger.debug("Starting the spam filter check for the mesage text");
		validateNewMessage( fullMessageText );
	}
	
	/**
	 * Allows to determine if the user sends messages too often or is typing too fast
	 * @param message the chat message object
	 * @throws AccessBlockedException if it is detected that the user is abusing the system
	 */
	public void validateNewChatMessage( ChatMessage message) throws AccessBlockedException {
		validateNewMessage( message.messageBody );
	}
	
	/**
	 * Allows to determine if the user sends messages too often or is typing too fast
	 * @param messageBody the text message body or null for a message with no body
	 * @throws AccessBlockedException if it is detected that the user is abusing the system
	 */
	private synchronized void validateNewMessage( final String messageBody ) throws AccessBlockedException {
		final long currentTimeMillisec = System.currentTimeMillis();
		try {
			//Gets set to true if either the message sending frequency or the text tiping speed it too high 
			boolean isBadMessageDetected = false;
			//The time between consequently sent messages
			final long timeBetweenMsgsMillisec = currentTimeMillisec - lastMessageTimeMillis;
			
			final double typingSpeed;
			if( messageBody != null ) {
				//Check that the speed of typing is fine
				logger.debug("The user " + userID + " send an message of length " + messageBody.length() );
				typingSpeed = ( (double) messageBody.length() ) / ( (double) timeBetweenMsgsMillisec );
			} else {
				//Well this is an empty message, there might be a message attachment, but
				//we do not care about this here, if the user send messages too often, it
				//will be still detected, during the further check 
				logger.debug("The user " + userID + " send an message with a null body");
				typingSpeed = 0;
			}
			//Test the bad send message attempt criteria, note the we test separately
			//for send message frequency and the typing speed, since these are two
			//different criteria and this way the abuse detection should be more accurate
			if( timeBetweenMsgsMillisec < MIN_TIME_BETWEEN_MESSAGES_MILLISEC ) {
				//The message was sent too soon, this is bad
				logger.warn("The message sent by the user " + userID + " is bad, the time since the" +
						" last message is " + timeBetweenMsgsMillisec + " millisec, the min time" +
						" between messages must be " + MIN_TIME_BETWEEN_MESSAGES_MILLISEC );
				//Sending messages too often gives you one bad point
				currBadSendMsgPoints += 1;
				//The bad message sending speed is detected
				isBadMessageDetected = true;
			}
			if( typingSpeed > MAX_SYMBOLS_PER_MILLISECOND ) {
				logger.warn("The message sent by the user " + userID + " is bad, the typing" + 
							" rate is " + typingSpeed + " (chars per millisec) with the" +
							" maximum typing rate " + MAX_SYMBOLS_PER_MILLISECOND );
				//Typing too fast gives you two bad points
				currBadSendMsgPoints += 2;
				//The bad message tiping speed is detected
				isBadMessageDetected = true;
			}
			
			//If the bad message is not detected then decrement the counter
			if( ! isBadMessageDetected ) {
				logger.debug("The message sent by the user " + userID + " is good.");
				currBadSendMsgPoints -= 1;
			}
			
			//Do the final checks an report the error if needed
			if( currBadSendMsgPoints >= MAX_NUM_BAD_SEND_MSG_POINTS ) {
				logger.error( "The user " +userID+" has reached the maximum number of bad " +
							  "relatively-successive chat message send points (" +
							  MAX_NUM_BAD_SEND_MSG_POINTS + ")");
				//Just reset back to MAX_NUM_BAD_SEND_MSG_ATTEMPTS,
				//to keep the values within the required interval
				currBadSendMsgPoints = MAX_NUM_BAD_SEND_MSG_POINTS;
				throw new AccessBlockedException( AccessBlockedException.SEND_MESSAGES_ABUSE_EXCEPTION_ERR );
			} else {
				if( currBadSendMsgPoints < 0 ) {
					//Just reset back to zero, to keep the
					//values within the required interval
					currBadSendMsgPoints = 0;
				}
			}
		} finally {
			logger.info( "The current number of the user " + userID +
						 " bad message send points is " + currBadSendMsgPoints );
			lastMessageTimeMillis = currentTimeMillisec;
		}
	}
	
	//The map storing the mapping from the user id to the message filter
	private static Map<Integer, MessageSendAbuseFilter> idToMessageFilter = new HashMap<Integer, MessageSendAbuseFilter>();
	
	/**
	 * Gets the user message filter from the internal mapping or creates a new one
	 * @param userID the user we want the message filter for
	 * @return the message filter for the given user
	 */
	private static MessageSendAbuseFilter getMessageFilter( final int userID ) {
		synchronized( idToMessageFilter ) {
			logger.info("Getiting a message filter for the user " + userID );
			MessageSendAbuseFilter filter = idToMessageFilter.get( userID );
			if( filter == null ) {
				filter = new MessageSendAbuseFilter( userID );
				idToMessageFilter.put( userID, filter );
			}
			return filter;
		}
	}
	
	/**
	 * Allows to get the message filter for the provided user.
	 * First we check if the filter is already inside the session listener
	 * if yes we just return it, if not then we get a one (new if needed)
	 * and put it inside the session listener
	 * @param httpSession the session this user uses
	 * @param userID the user id we want the filter for
	 * @return the message filter for the given user
	 */
	public static MessageSendAbuseFilter getMessageFilter( HttpSession httpSession, final int userID ) {
		//We synchronize on the session object here, I am not completely sure if this is good
		synchronized( httpSession ) {
			MessageSendAbuseFilter filter = (MessageSendAbuseFilter) httpSession.getAttribute( USER_MESSAGE_FILTER_SESSION_ATTRIBUTE );
			if( filter == null ) {
				logger.info( "There is no user message filter stored in the HTTP session for user " +
							 userID + " thus we populate his session with a (new) filter");
				filter = getMessageFilter( userID  );
				httpSession.setAttribute( USER_MESSAGE_FILTER_SESSION_ATTRIBUTE, filter );
			} else {
				if( filter.getUserID() == userID ) {
					logger.debug("The HTTP session contains a correct message filter for user " + userID );
				} else {
					logger.error( "It is unbelievable but the HTTP session of user " + userID +
								  " contains the message filter for user " + filter.getUserID() +
								  ", thus we are repopulating the session with a correct filter" );
					filter = getMessageFilter( userID  );
					httpSession.setAttribute( USER_MESSAGE_FILTER_SESSION_ATTRIBUTE, filter );
				}
			}
			return filter;
		}
	}
	
	/**
	 * Allows to remove the user filter, should be only called when
	 * the user is fully logged of from the site. Also we remove the
	 * message filter from the session if it is not null.
	 * @param httpSession the HTTP session assigned to the given user
	 * @param userID the id of the user we want to remove the filter for
	 */
	public static void removeMessageFilter( HttpSession httpSession, final int userID ) {
		logger.info("Removing the message filter for the user " + userID );
		if( httpSession != null ) {
			httpSession.removeAttribute( USER_MESSAGE_FILTER_SESSION_ATTRIBUTE );
		}
		synchronized( idToMessageFilter ) {
			idToMessageFilter.remove( userID );
		}
	}
}
