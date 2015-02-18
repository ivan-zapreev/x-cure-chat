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
 * The user data objects package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.data;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UIErrorMessages;

import com.xcurechat.client.utils.StringUtils;

import com.xcurechat.client.rpc.exceptions.MessageException;
import com.xcurechat.client.rpc.exceptions.SiteException;

/**
 * @author zapreevis
 * This class represents the forum message
 */
public class ShortForumMessageData implements IsSerializable {
	public static final String MESSAGE_PATH_ID_DELIMITER = ".";
	//The escaped forum path id delimiter for being used in regular expressions
	public static final String ESC_MESSAGE_PATH_ID_DELIMITER = "\\" + MESSAGE_PATH_ID_DELIMITER;
	
	//The unknown id value of a forum message
	public static final int UNKNOWN_MESSAGE_ID = 0;
	
	//The id of the root message of the forum
	public static final int ROOT_FORUM_MESSAGE_ID = 1;
	
	//The regular expression for the path ID of the topic message, which is the first level messages within forum sections
	public static final String TOPIC_MESSAGE_PATH_ID_REGEXP = ROOT_FORUM_MESSAGE_ID +
															  ESC_MESSAGE_PATH_ID_DELIMITER +
															  "\\d*" +
															  ESC_MESSAGE_PATH_ID_DELIMITER;
	//The regular expression for the path ID of the message that is a top-level post in a topic
	public static final String POST_MESSAGE_PATH_ID_REGEXP = ROOT_FORUM_MESSAGE_ID +
															  ESC_MESSAGE_PATH_ID_DELIMITER +
															  "\\d*" +
															  ESC_MESSAGE_PATH_ID_DELIMITER +
															  "\\d*" +
															  ESC_MESSAGE_PATH_ID_DELIMITER;
	//The regular expression for the path ID of the message that is a some-level reply to a top-level post in a topic
	public static final String REPLY_MESSAGE_PATH_ID_REGEXP = ROOT_FORUM_MESSAGE_ID +
																			  ESC_MESSAGE_PATH_ID_DELIMITER +
																			  "\\d*" +
																			  ESC_MESSAGE_PATH_ID_DELIMITER +
																			  "\\d*" +
																			  ESC_MESSAGE_PATH_ID_DELIMITER + 
																			  ".+";
	
	//The maximum length of the forum message
	public static final int MAX_MESSAGE_LENGTH = 8192;
	
	//The maximum length of the forum-message title
	public static final int MAX_MESSAGE_TITLE_LENGTH = 256;
	
	//The message ID
	public int messageID = UNKNOWN_MESSAGE_ID;
	
	//The message's parent message ID
	public int parentMessageID = UNKNOWN_MESSAGE_ID;
	
	//The message was sent by the user with this id
	public int senderID = ShortUserData.UNKNOWN_UID;
	
	//The message title
	public String messageTitle = "";
	
	//The message body
	public String messageBody = "";
	
	//The message path ID, i.e. the path to the message up to and including it's closest parent
	public String messagePathID = "";
	
	//This field indicates if the forum message was approved by the administrator
	//to be a news message. It contains true if it is approved, otherwise false. 
	public boolean isApproved = false;
	
	/*The Fields related to people voting for the given forum message*/
	
	//Contains true if the user who retrieves this forum message has voted for this message
	public boolean hasVoted = false;
	//The number of times this forum messages was voted for or against it
	public int numVotes = 0;
	//The current vote value, as a sum of all votes given for this post
	public int voteValue = 0;
	
	//The list of short file descriptirs of the files attached to this message
	//WARNING: we use an array list here because this field is used in the paged 
	//list dialog, so it is important that the files are ordered there by their
	//ID, and the IDs can be retrieved from the list by their indexes
	public List<ShortFileDescriptor> attachedFileIds = new ArrayList<ShortFileDescriptor>();
	//Stores the date when this message was created
	public Date sentDate = null;
	//Stores the date when this message was updated
	public Date updateDate = null;
	
	/**
	 * Allows to create a clone of the given message
	 * @param cloneAttachedFiles if true then we also copy the attached file indeces otherwise not.
	 * @return an exact copy of the given message
	 */
	public ShortForumMessageData clone( final boolean cloneAttachedFiles ) {
		ShortForumMessageData newMessage = new ShortForumMessageData();
		newMessage.messageID = messageID;
		newMessage.parentMessageID = parentMessageID;
		newMessage.senderID = senderID;
		newMessage.messageTitle = messageTitle;
		newMessage.messageBody = messageBody;
		newMessage.messagePathID = messagePathID;
		newMessage.isApproved = isApproved;
		newMessage.hasVoted = hasVoted;
		newMessage.numVotes = numVotes;
		newMessage.voteValue = voteValue;
		if( cloneAttachedFiles ) {
			newMessage.attachedFileIds.addAll( attachedFileIds );
		}
		return newMessage;
	}
	
	/**
	 * Allows to check if this message is the forum-section.
	 * I.e. has the parent that is an invisible root messages
	 * @return true if this message is the forum section
	 */
	public boolean isForumSectionMessage() {
		return parentMessageID == ROOT_FORUM_MESSAGE_ID;
	}
	
	/**
	 * Allows to detect if this message is a forum tipic, i.e. is the first level messages within forum sections
	 * @return true if the messagePathID indicates that it is a topic message
	 */
	public boolean isForumTopicMessage() {
		return ( messagePathID != null ) ? messagePathID.matches( TOPIC_MESSAGE_PATH_ID_REGEXP ) : false;
	}
	
	/**
	 * Allows to detect if this message is a first-level messages within forum topic.
	 * @return true if the messagePathID indicates that it is a first-level post message
	 */
	public boolean isPostMessage() {
		return ( messagePathID != null ) ? messagePathID.matches( POST_MESSAGE_PATH_ID_REGEXP ) : false;
	}
	
	/**
	 * Allows to detect if this message is a reply (of some level) to the top-level post message
	 * @return true if the messagePathID indicates that it is a reply (of some level) to the top-level post message
	 */
	public boolean isReplyMessage() {
		return ( messagePathID != null ) ? messagePathID.matches( REPLY_MESSAGE_PATH_ID_REGEXP ) : false;
	}
	
	/**
	 * Allows to convert the given message into a reply message
	 * @param senderID the id of the user who relies to the message
	 * @param isQuoteSubject if true the we make a "RE:" prefix for the, otherwise keep it empty 
	 * @param isQuoteBody if true then we quote the message body otherwise not
	 */
	public void convertIntoAReplyMessage( final int senderID, final boolean isQuoteSubject, final boolean isQuoteBody ) {
		//The message will be sent by the given user
		this.senderID = senderID;
		
		//THe parent message will be the given one
		this.parentMessageID = this.messageID;
		
		//NOTE: this.branchMessageID stays the same as before!!!
		
		//Make the message path ID in case it is needed
		this.messagePathID = this.messagePathID + messageID + MESSAGE_PATH_ID_DELIMITER;
		
		//WARNING: The message id should be re-set after the messagePathID is constructed!!!
		//The current reply-message ID is not known
		this.messageID = ShortForumMessageData.UNKNOWN_MESSAGE_ID;
		
		//NOTE: The sent date will be set on the server
		//this.sentDate = null;
		
		//Make the message title to be Re:, in english, because of the internationalization
		if( isQuoteSubject ) {
			this.messageTitle = StringUtils.makeReplyMessageTitle( messageTitle );
		} else {
			this.messageTitle = "";
		}
		
		//Form the reply message body from the original message
		if( isQuoteBody ) {
			this.messageBody = StringUtils.makeReplyMessageBody( messageBody );
		} else {
			this.messageBody = "";
		}
		
		//Clean the list of attached files
		attachedFileIds.clear();
	}
	
	/**
	 * Allows to validate the content of the forum message before it is sent to the server
	 * @param allowEmptyBody if true then we allow for an empty body
	 * @throws SiteException if the message has an empty title or body.
	 */
	public void validate( final boolean allowEmptyBody ) throws SiteException {
		MessageException exception = new MessageException();
		UIErrorMessages errorsI18N = I18NManager.getErrors();
		
		messageTitle = ( messageTitle == null ) ? "" : messageTitle.trim();
		if( messageTitle.trim().isEmpty() ) {
			exception.addErrorMessage( errorsI18N.theMsgTitleIsTooEmpty() );
		} else {
			if( messageTitle.length() > MAX_MESSAGE_TITLE_LENGTH ) {
				exception.addErrorMessage( errorsI18N.theMsgTitleIsTooLong( MAX_MESSAGE_TITLE_LENGTH ) );
			}
		}
		
		messageBody = ( messageBody == null ) ? "" : messageBody.trim();
		//if we are not allowed to have an empty message body and the body are empty, we throw an exception
		if( ( ! allowEmptyBody ) && messageBody.isEmpty() && attachedFileIds.isEmpty() ) {
			exception.addErrorMessage( errorsI18N.theMsgBodyIsTooEmpty() );
		} else {
			if( messageBody.length() > MAX_MESSAGE_LENGTH ) {
				exception.addErrorMessage( errorsI18N.theMsgBodyIsTooLong( MAX_MESSAGE_LENGTH ) );
			}
		}
		
		if( exception.containsErrors() ) {
			throw exception;
		}
	}
	
	/**
	 * Allows to get the message parent ids based on the message path id.
	 * Note that the ids in the list are ordered the same way as in the path
	 * from the root message to the nearest message parent.
	 * @param msg the message to get the set of parents for
	 * @return the set of parent message ids
	 */
	public static List<Integer> getMessageParentIds( final ShortForumMessageData msg ) {
		List<Integer> parentIds = new ArrayList<Integer>();
		if( msg != null && msg.messagePathID != null ) {
			String[] ids = msg.messagePathID.split( "\\" + MESSAGE_PATH_ID_DELIMITER );
			for(int i = 0; i < ids.length; i++) {
				try{
					parentIds.add( Integer.parseInt( ids[i] ) );
				} catch(NumberFormatException e) {
					//There is nothing we can do about it
				}
			}
		}
		return parentIds;
	}

	/**
	 * Provided a forum message data, this method allows to retrieve the forum topic message ID from it
	 * @param msg the message for which we want to know the topic
	 * @return the it of the topic this message belongs to or ShortForumMessageData.UNKNOWN_MESSAGE_ID
	 * if the topic could not be detected, this can happen if the given message is a section message
	 */
	public static final int getTopicMessageID( final ShortForumMessageData msg ) {
		int topicMessageID = UNKNOWN_MESSAGE_ID, indxS, indxB, indxE;
		if( msg != null ) {
			//If this is not a forum section message, note that the root forum message
			//also has msg.parentMessageID == ROOT_FORUM_MESSAGE_ID, the same as the
			//forum section message
			if( msg.parentMessageID != ROOT_FORUM_MESSAGE_ID ) {
				if( msg.isForumTopicMessage() ) {
					//This is a topic message, thus return its id
					topicMessageID = msg.messageID;
				} else {
					//This is the case of the non-root, non-section, non-topic message, extract the topic id from the messagePathID
					if( ( indxS = msg.messagePathID.indexOf( MESSAGE_PATH_ID_DELIMITER ) ) != -1 ) {
						if( ( indxB = msg.messagePathID.indexOf( MESSAGE_PATH_ID_DELIMITER, indxS + 1 ) ) != -1 ) {
							if( ( indxE = msg.messagePathID.indexOf( MESSAGE_PATH_ID_DELIMITER, indxB + 1 ) ) != -1 ) {
								String topicIDStr = msg.messagePathID.substring( indxB + 1, indxE );
								try {
									topicMessageID = Integer.parseInt( topicIDStr );
								} catch( NumberFormatException e ) {
									//Can't help it
								}
							}
						}
					}
				}
			}
		}
		return topicMessageID;
	}
}
