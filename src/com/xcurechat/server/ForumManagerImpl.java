/**
 * The server-side RPC package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.server;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.data.ShortPrivateMessageData;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.data.ShortForumMessageData;
import com.xcurechat.client.data.search.ForumSearchData;
import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.rpc.ForumManager;

import com.xcurechat.client.rpc.exceptions.CaptchaTestFailedException;
import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.rpc.exceptions.MessageException;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.utils.SmileyHandler;
import com.xcurechat.client.utils.UserForumActivity;

import com.xcurechat.server.cache.ForumQueriesCache;
import com.xcurechat.server.core.SecureServerAccess;
import com.xcurechat.server.core.ServerSideUserManager;
import com.xcurechat.server.core.UserSessionManager;

import com.xcurechat.server.jdbc.ConnectionWrapper;

import com.xcurechat.server.jdbc.forum.DeleteSenderForumMessageExecutor;
import com.xcurechat.server.jdbc.forum.InsertForumMessageExecutor;
import com.xcurechat.server.jdbc.forum.MoveForumMessageExecutor;
import com.xcurechat.server.jdbc.forum.SearchMessagesExecutor;
import com.xcurechat.server.jdbc.forum.MsgParentsNotifyExecutor;
import com.xcurechat.server.jdbc.forum.UpdateForumMessageExecutor;
import com.xcurechat.server.jdbc.forum.DeleteForumMessageExecutor;
import com.xcurechat.server.jdbc.forum.GetMessageExecutor;
import com.xcurechat.server.jdbc.forum.ApproveForumMessageExecutor;

import com.xcurechat.server.jdbc.forum.files.DeleteForumFilesExecutor;
import com.xcurechat.server.jdbc.forum.files.SetMessageIDExecutor;
import com.xcurechat.server.jdbc.forum.files.SelectMessageFilesExecutor;
import com.xcurechat.server.jdbc.forum.vote.CheckIfTheUserVotedExecutor;
import com.xcurechat.server.jdbc.forum.vote.RememberForumMessageVoteExecutor;
import com.xcurechat.server.jdbc.forum.vote.VoteForForumMessageExecutor;
import com.xcurechat.server.jdbc.messages.InsertMessageExecutor;

import com.xcurechat.server.redirect.ForumRedirectHelper;
import com.xcurechat.server.security.captcha.CaptchaServiceManager;

import com.xcurechat.server.utils.Configurator;
import com.xcurechat.server.utils.FlashEmbeddedParser;
import com.xcurechat.server.utils.ServerEncoder;

/**
 * @author zapreevis
 * The RPC servlet that allows to manage the forum
 */
public class ForumManagerImpl extends RemoteServiceServlet implements ForumManager {
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( ForumManagerImpl.class );
	//The forum cache
	private static final ForumQueriesCache forumCache = ForumQueriesCache.getInstane();
	//The xcure-chat domain name pattern
	private static String xcureDomainPattern = null;
	//The encoder that has to be used in the server side
	private static final ServerEncoder encoder = new ServerEncoder();
	//Get the forum redirect helper for making the forum message url
	private static final ForumRedirectHelper forumHelper = new ForumRedirectHelper(); 
	//The amount in gold pieces which is added to the account of the user
	//who created the message in case some one gave a positive vote for it.
	private static final int POSITIVE_FORUM_VOTE_INCREMENT_GOLD = 1;
    //This is the object used for synchronizing the forum modifications that
	//consist of more than one query, or need more than one DB connection or
	//are not transactional due to technical reasons.
	private static final Object forumModifSynch = new Object();
	
	@Override
	public void init() {
	    // Read properties file.
	    xcureDomainPattern = Configurator.getSiteDomainPattern( getServletContext() );
	}
	
	/**
	 * Retrieves the HttpSession object if any.
	 * @return the HttpSession object, either an old or a newly created one
	 */
	private HttpSession getLocalHttpSession() {
		return this.getThreadLocalRequest().getSession( true );
	}
	
	/* (non-Javadoc)
	 * @see com.xcurechat.client.rpc.ForumManager#deleteFiles(int, java.lang.String, java.util.List)
	 */
	@Override
	public void deleteFiles( final int userID, final String userSessionId, final List<Integer> fileIDS ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Deleting forum-message files by user " + userID );
				
				DeleteForumFilesExecutor deleteFilesExec = new DeleteForumFilesExecutor( UserSessionManager.getUserDataObject(userID).isAdmin(),
																						 userID, fileIDS );
				ConnectionWrapper<Void> deleteFilesConnWrap = ConnectionWrapper.createConnectionWrapper( deleteFilesExec );
				deleteFilesConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				
				//Notify cache about the file removal, just take one file
				//id because one file can be only attached to one message
				//and here we delete files from one message only
				if( fileIDS != null && fileIDS.size() > 0 ) {
					forumCache.removeFormMessageFile( fileIDS.get(0) );
				}
				
				//Nothing the be returned here
				return null;
			}
		}).execute();
	}
	
	/**
	 * Allows to get make the user pay for the payed smiles used in the forum message
	 * @param userData the data of the user who sent the message. 
	 * @param message the forum message to be priced.
	 * @throws InternalSiteException the exception is thrown in case the user can not pay for the message
	 */
	private void payForForumMessageSmileys( final UserData userData, final ShortForumMessageData message ) throws InternalSiteException {
		if( message.messageTitle == null ){
			message.messageTitle = "";
		}
		if( message.messageBody == null ){
			message.messageBody = "";
		}
		//Account for the payed smileys
		final int price = SmileyHandler.countTextPriceWatchCategory( message.messageBody + message.messageTitle );
		
		//If the user can not pay for the used smiles, then throw an exception
		userData.decrementGoldPiecesCount( price );
	}
	
	/* (non-Javadoc)
	 * @see com.xcurechat.client.rpc.ForumManager#sendMessage(int, java.lang.String, com.xcurechat.client.data.ForumMessageData, java.lang.String)
	 */
	@Override
	public void sendMessage( final int userID, final String userSessionId,
							 final ShortForumMessageData message,
							 final String captchaResponse ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Sending/Updating a forum message by user " + userID );
				if( logger.isDebugEnabled() ) {
					logger.debug( "Sending/Updating a forum message by user " + userID +
								  ", with the title: \"" + message.messageTitle +
								  "\", and the body: \"" + message.messageBody + "\"" );
				}
				
				//Get the user data object
				final MainUserData userData = UserSessionManager.getUserDataObject(userID);
				
				//Check if the user needs to pass CAPTCHA or validate the correct answer for CAPTCHA 
				if( ! UserForumActivity.isCaptchaNeeded( userData.getUserForumActivity() ) ||
					CaptchaServiceManager.validateCaptchaProblem( getLocalHttpSession().getId(), captchaResponse)) {
					//Pre-process embedded objects in the message body, parses, validates and completes them
					message.messageBody = (new FlashEmbeddedParser(xcureDomainPattern)).processEmbeddedTags( message.messageBody );
					
					//WARNING: For the forum messages we DO NOT UPDATE the sent message statistics
					//(the spam fileter) because they are protected by the CAPTCHA problem
					
					//Force the proper sender ID
					message.senderID = userID;
					//If the parent message ID is not set then it must be a new section
					//message! Thus we set the parent ID to be the root forum message ID.
					if( message.parentMessageID == ShortForumMessageData.UNKNOWN_MESSAGE_ID ) {
						message.parentMessageID = ShortForumMessageData.ROOT_FORUM_MESSAGE_ID;
					}
					
					//If the parent message id is not set then we assume this is a new forum section
					//and there fore it was to be an administrator who adds the message
					if( message.parentMessageID == ShortForumMessageData.ROOT_FORUM_MESSAGE_ID ) {
						if( ! userData.isAdmin() ) {
							throw new InternalSiteException( InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR );
						}
					}
					
					//Check if the user has to pay for the smiles
					payForForumMessageSmileys( userData, message );
					
					//Update the forum messages statistics, regardless to the attached files
					//NOTE: Here we just increment this value, and do not decrement it because
					//it will be re-set when the user logs in again
					UserSessionManager.getUserDataObject(userID).sentAnotherForumMessage();
					
					//This is a forum modification, has to be synchronized
					synchronized( forumModifSynch ) {
						//Add the forum message to the database and also retrieve it's newly created ID
						InsertForumMessageExecutor insertMsgExec = new InsertForumMessageExecutor( message );
						ConnectionWrapper<Void> insertMsgConnWrap = ConnectionWrapper.createConnectionWrapper( insertMsgExec );
						insertMsgConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
						
						//Update the last reply date and time for the parent messages, also the replies count
						MsgParentsNotifyExecutor updateParentsExec = new MsgParentsNotifyExecutor( message, true );
						ConnectionWrapper<Void> updateParentsConnWrap = ConnectionWrapper.createConnectionWrapper( updateParentsExec );
						updateParentsConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
					}
					
					//If this is a reply to some post we are posting, then send a notification to the post owner 
					if( message.isReplyMessage() ) {
						final ForumMessageData parentMessage = getForumMessageInternal( userID, message.parentMessageID, false );
						if( ( parentMessage != null ) && ( parentMessage.senderID != userID ) ) {
							final ForumSearchData searchObject = new ForumSearchData();
							searchObject.baseMessageID = parentMessage.messageID;
							searchObject.isOnlyMessage = true;
							final String originalPostURL = forumHelper.completeGWTUrl( getThreadLocalRequest(), searchObject.serialize( encoder ) );
							//If we successfully found the parent message we reply to, notify the user
							InsertMessageExecutor sendSmplMsgExec = new InsertMessageExecutor( userID, parentMessage.senderID,
																							   ChatRoomData.UNKNOWN_ROOM_ID,
																							   ShortPrivateMessageData.FORUM_REPLY_NOTIFICATION_MESSAGE_TYPE,
																							   parentMessage.messageTitle, originalPostURL );
							ConnectionWrapper<Void> sendSimpleMessageConnWrap = ConnectionWrapper.createConnectionWrapper( sendSmplMsgExec );
							sendSimpleMessageConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
						}
					}
					
					//If the message was created, update the attached files with the proper message ID
					if( message.attachedFileIds != null && ! message.attachedFileIds.isEmpty() ) {
						SetMessageIDExecutor fileUpdateExec = new SetMessageIDExecutor( message.messageID, userID, message.attachedFileIds );
						ConnectionWrapper<Void> fileUpdateConnWrap =  ConnectionWrapper.createConnectionWrapper( fileUpdateExec );
						fileUpdateConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
					}
					
					//Update the forum cache
					forumCache.sendNewForumMessage( message );
				} else {
					throw new CaptchaTestFailedException(CaptchaTestFailedException.CAPTCHA_TEST_FAILED_ERR);
				}
				
				//Nothing the be returned here
				return null;
			}
		}).execute();
	}
	
	/* (non-Javadoc)
	 * @see com.xcurechat.client.rpc.ForumManager#updateMessage(int, java.lang.String, com.xcurechat.client.data.ForumMessageData)
	 */
	@Override
	public void updateMessage( final int userID, final String userSessionId, final ShortForumMessageData message ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Updating a forum message " + message.messageID + " by user " + userID );
				
				//Get the user data object
				final UserData userData = UserSessionManager.getUserDataObject(userID);
				
				//Check if the user has to pay for the smiles, note that this way the user
				//can pay again for the smiles he payed for, but tracking this is too much
				//work, plus the expensive smiles are of an adult nature, thus it is ok.
				payForForumMessageSmileys( userData, message );
				
				//Processes embedded objects in the message body, parses, validates and completes them
				message.messageBody = (new FlashEmbeddedParser(xcureDomainPattern)).processEmbeddedTags( message.messageBody );
				
				//WARNING: For the forum messages we DO NOT UPDATE the sent message statistics
				//(the spam filter) because they are protected by the CAPTCHA problem
				
				//True if the message is being updated by the admin
				final boolean isUpdatedByAdmin = userData.isAdmin(); 
				
				//Force the proper sender, for everyone except the admin
				if( message.senderID != userID ) {
					if( ! isUpdatedByAdmin ) {
						throw new InternalSiteException( InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR );
					}
				}
					
				//This is a forum modification, has to be synchronized
				synchronized( forumModifSynch ) {
					//Update the message
					UpdateForumMessageExecutor updateMsgExec = new UpdateForumMessageExecutor( message, ! isUpdatedByAdmin );
					ConnectionWrapper<Void> updateMsgConnWrap = ConnectionWrapper.createConnectionWrapper( updateMsgExec );
					//NOTE: Throws an exception if the message could not be updated, e.g. if the
					// current user is not the message's sender or the message does not exist
					updateMsgConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
					
					//Update the last reply date and time for the parent messages
					MsgParentsNotifyExecutor updateParentsExec = new MsgParentsNotifyExecutor( message, false );
					ConnectionWrapper<Void> updateParentsConnWrap = ConnectionWrapper.createConnectionWrapper( updateParentsExec );
					updateParentsConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				}
				
				if( message.attachedFileIds != null && ! message.attachedFileIds.isEmpty() ) {
					//If the update was successful, i.e. the userID is the owner of the 
					//message, then we update the attached files with the proper message ID
					SetMessageIDExecutor fileUpdateExec = new SetMessageIDExecutor( message.messageID, userID, message.attachedFileIds );
					ConnectionWrapper<Void> fileUpdateConnWrap =  ConnectionWrapper.createConnectionWrapper( fileUpdateExec );
					fileUpdateConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				}
				
				//Update the forum cache
				forumCache.updateForumMessage( message );
				
				//Nothing the be returned here
				return null;
			}
		}).execute();
	}
	
	/**
	 * Allows to delete a forum message
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param messageId the id of the message to be deleted
	 * @throws SiteException if the user is not logged in or we
	 *			try to validate the session too often or smth else!
	 */
	@Override
	public void deleteMessage( final int userID, final String userSessionId, final int messageID ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Deleting forum message " + messageID + " by user " + userID );
				final ForumMessageData messageToDelete;
				
				//This is a forum modification, has to be synchronized
				synchronized( forumModifSynch ) {
					//Get the latest message data
					messageToDelete = getForumMessageInternal( userID, messageID, false );
					
					//Delete the message in the database
					if( messageID != ForumMessageData.UNKNOWN_MESSAGE_ID &&
						messageID != ForumMessageData.ROOT_FORUM_MESSAGE_ID ) {
						//If we are not trying to delete the root forum message or the unknown message then
						final MainUserData userDataObject = UserSessionManager.getUserDataObject(userID);
						DeleteForumMessageExecutor deleteMsgExec = new DeleteForumMessageExecutor( userID, messageToDelete, (userDataObject == null ? false : userDataObject.isAdmin()) );
						ConnectionWrapper<Void> deleteMsgConnWrap = ConnectionWrapper.createConnectionWrapper( deleteMsgExec );
						deleteMsgConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
					} else {
						logger.warn("User " + userID + " attempts to delete an unknon or root forum message " + messageID );
					}
				}
				
				//Notify the cache
				forumCache.deleteForumMessage( messageToDelete );
			
				//Nothing the be returned here
				return null;
			}
		}).execute();		
	}
	
	private static OnePageViewData<ForumMessageData> searchMessagesStatic( final HttpSession session, final HttpServletRequest request,
																		  final int userID, final String userSessionId,
																		  final ForumSearchData searchParams ) throws SiteException {
		return (new SecureServerAccess<OnePageViewData<ForumMessageData>>( session, request, userID, userSessionId ) {
			protected OnePageViewData<ForumMessageData> action() throws SiteException {
				logger.info( "Searching the forum by user " + userID + ", he is " +
							 ( userID == UserData.UNKNOWN_UID || userID == UserData.DEFAULT_UID ? "<anonimous>" : "<regular>" ) );
				
				//Try to get the query result from the cache
				OnePageViewData<ForumMessageData> result = forumCache.getQueryResult( searchParams, userID );
				//NOTE: If the query result is not cached we could synch on the ForumQueriesCache.getInstane() while getting
				// the result from the DB, that could reduce the workload in certain cases, but would also block other
				//forum users for until the query is finished, therefore we do not do that here
				
				//If the cached query is not found, then check the database
				if( result == null ) {
					//Prepare the temporary and the resulting data sets
					result = new OnePageViewData<ForumMessageData>();
					LinkedHashMap<Integer,ForumMessageData> tmpResult = new  LinkedHashMap<Integer,ForumMessageData>();
					
					if( searchParams.MAX_NUMBER_OF_MESSAGES_PER_PAGE > result.MAX_NUMBER_OF_ENTRIES_PER_PAGE ) {
						//Well it is of course up to developer to decide how much data to return, but still
						//At least we make a warning, but it is just for feeling better :)
						logger.warn( " The maximum number of retrieved forum messages should not exceed " +
									  result.MAX_NUMBER_OF_ENTRIES_PER_PAGE + " but it is set to " +
									  searchParams.MAX_NUMBER_OF_MESSAGES_PER_PAGE );
					}
					
					//1) Get the list of messages
					SearchMessagesExecutor searchMessageExec = new SearchMessagesExecutor( searchParams, result );
					ConnectionWrapper<LinkedHashMap<Integer,ForumMessageData>> searchMessageConnWrap =  ConnectionWrapper.createConnectionWrapper( searchMessageExec );
					searchMessageConnWrap.executeQuery( tmpResult, ConnectionWrapper.XCURE_CHAT_DB );
					
					//If there are actually some search results then make them complete
					if( tmpResult.keySet().size() != 0 ) {
						//2) Get the list of attached files for each message
						SelectMessageFilesExecutor selectMsgFilesExec = new SelectMessageFilesExecutor( tmpResult );
						ConnectionWrapper<Void> selectMsgFilesConnWrap = ConnectionWrapper.createConnectionWrapper( selectMsgFilesExec );
						selectMsgFilesConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
					}
					
					//3) Complete the result object
					result.offset = ( searchParams.pageIndex - 1 ) * ForumSearchData.MAX_NUMBER_OF_MESSAGES_PER_PAGE;
					result.entries = new ArrayList<ForumMessageData>();
					result.entries.addAll( tmpResult.values() );
					
					//4) Add the result to the cache
					forumCache.putQueryResult( searchParams, result, userID );
				}
				
				return result;
			}	
		}).execute( true, true, false);
	}
	
	/* (non-Javadoc)
	 * @see com.xcurechat.client.rpc.ForumManager#searchMessages(int, java.lang.String, com.xcurechat.client.data.ForumMessageData)
	 */
	@Override
	public OnePageViewData<ForumMessageData> searchMessages( final int userID, final String userSessionId, final ForumSearchData searchParams ) throws SiteException {
		return searchMessagesStatic(getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId, searchParams );
	}
	
	/**
	 * Allows to get the forum message by its id from the forum cache or the database 
	 * @param userID the id of the user who wants to retrieve the message
	 * @param messageID the id of the message that has to be retrieved
	 * @param isNeedComplete if true then we can be sure that this method returns a complete forum message data.
	 * 				   if false then it is possible that the list of attached message files and the name
	 *				   and the last date of the reply in this message's branch might not be set.
	 *				   The number of message replies is sure to be set in both cases.
	 * @return the forum message
	 * @throws SiteException an exception of a forum message could not be found
	 */
	private ForumMessageData getForumMessageInternal( final int userID, final int messageID, final boolean isNeedComplete ) throws SiteException {
		logger.info( "The user " + userID + ", here as " +
				 ( userID == UserData.UNKNOWN_UID || userID == UserData.DEFAULT_UID ? "<anonimous>" : "<regular>" ) +
				 ", tries to retrieve a forum message " + messageID );
		
		//Try getting the message from the cache
		ForumMessageData result = forumCache.getForumMessage( messageID, userID );
		
		if( result == null  ) {
			//If the message is not in the cache, then get the message from the DB
			result = new ForumMessageData();
			
			//1) Try to retrieve the forum message from the DB
			GetMessageExecutor getMessageExec = new GetMessageExecutor( messageID );
			ConnectionWrapper<ForumMessageData> getMessageConnWrap = ConnectionWrapper.createConnectionWrapper( getMessageExec );
			getMessageConnWrap.executeQuery( result, ConnectionWrapper.XCURE_CHAT_DB );
			
			//Check that the message was found
			if( result.messageID == ForumMessageData.UNKNOWN_MESSAGE_ID ) {
				//If the message was not found, throw an exception
				throw new MessageException( MessageException.UNABLE_TO_RETRIEVE_MESSAGE );
			} else {
				if( isNeedComplete ) {
					logger.debug("The user " + userID + " requested a complete forum message " + messageID );
					//If the message was found, complete it
					LinkedHashMap<Integer,ForumMessageData> tmpResult = new  LinkedHashMap<Integer,ForumMessageData>();
					tmpResult.put( result.messageID , result );
					
					//2) Get the list of attached files for each message
					SelectMessageFilesExecutor selectMsgFilesExec = new SelectMessageFilesExecutor( tmpResult );
					ConnectionWrapper<Void> selectMsgFilesConnWrap = ConnectionWrapper.createConnectionWrapper( selectMsgFilesExec );
					selectMsgFilesConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				} else {
					logger.debug("The user " + userID + " requested an incomplete forum message " + messageID );
				}
			}
		}
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see com.xcurechat.client.rpc.ForumManager#getForumMessage(int, java.lang.String, int)
	 */
	@Override
	public ForumMessageData getForumMessage( final int userID, final String userSessionId,
								 			 final int messageID ) throws SiteException {
		return (new SecureServerAccess<ForumMessageData>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected ForumMessageData action() throws SiteException {
				return getForumMessageInternal( userID, messageID, true );
			}	
		}).execute( true, true, false);		
	}
	
	/**
	 * This method allows to move the forum message to be rooted to another forum message.
	 * NOTE: The admin CAN move the message to make it a section and move the forum section
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param messageID the id of the message to be moved
	 * @param newParentMessageID the id of the new parent message
	 * @throws SiteException in case something goes wrong on the server, e.g. the message does not exist
	 */
	@Override
	public void moveForumMessage( final int userID, final String userSessionId,
								  final int messageID, final int newParentMessageID ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Moving the forum message " + messageID + " by user " + userID  + " to the new parent message " + newParentMessageID );
				
				//Only an admin can move the messages around but even he can not move the root message or to itself
				if( ( ! UserSessionManager.getUserDataObject(userID).isAdmin() ) ||
					messageID == ShortForumMessageData.ROOT_FORUM_MESSAGE_ID ||
					messageID == newParentMessageID ) {
					throw new InternalSiteException( InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR );
				}
				
				ForumMessageData newParentForumMessage;
				ForumMessageData forumMessage;
				
				//This is a forum modification, has to be synchronized
				synchronized( forumModifSynch ) {
					//Get the new parent message from the cache
					newParentForumMessage = getForumMessageInternal( userID, newParentMessageID, false );
					//Get the message we want to move from the cache
					forumMessage = getForumMessageInternal( userID, messageID, false );
					
					//Check that the message was found
					if( ( forumMessage.messageID == ForumMessageData.UNKNOWN_MESSAGE_ID ) ||
						( newParentForumMessage.messageID == ForumMessageData.UNKNOWN_MESSAGE_ID ) ) {
						//One of the messages could not be found, throw an exception
						throw new MessageException( MessageException.UNABLE_TO_RETRIEVE_MESSAGE );
					} else {
						//It does not make sense to move the message to its current parent
						if( forumMessage.parentMessageID != newParentMessageID ) {
							//Do the actual move of the messages
							MoveForumMessageExecutor moveMsgExec = new MoveForumMessageExecutor( forumMessage, newParentForumMessage );
							ConnectionWrapper<Void> moveMessageConnWrap = ConnectionWrapper.createConnectionWrapper( moveMsgExec );
							moveMessageConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
						}
					}
				}
				
				//Notify the cache about the message being moved
				forumCache.moveForumMessage( forumMessage, newParentForumMessage );
				
				//Nothing the be returned here
				return null;
			}
		}).execute();		
	}
	
	/**
	 * This method allows to approve/disapprove the forum message to be shown in the main site section.
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param messageID the id of the message to be approved/disapproved
	 * @param approve true to approve the message false to disapprove
	 * @throws SiteException in case something goes wrong on the server, e.g. the message does not exist
	 */
	@Override
	public void approveForumMessage( final int userID, final String userSessionId,
									 final int messageID, final boolean approve ) throws SiteException {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( (approve ? "Approving" : "Disapproving" ) +" the forum message " + messageID + " by user " + userID );
				
				//Only an admin can approve the message but even he can not approve the root message
				if( ( ! UserSessionManager.getUserDataObject(userID).isAdmin() ) ||
					messageID == ShortForumMessageData.ROOT_FORUM_MESSAGE_ID ) {
					throw new InternalSiteException( InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR );
				}
				
				ApproveForumMessageExecutor approveMsgExec = new ApproveForumMessageExecutor( messageID, approve );
				ConnectionWrapper<Void> approveMessageConnWrap = ConnectionWrapper.createConnectionWrapper( approveMsgExec );
				approveMessageConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				
				//Mark the user message as approved/disapproved
				forumCache.approveMessage( messageID, approve );
				
				//Nothing the be returned here
				return null;
			}
		}).execute();		
	}
	
	/**
	 * This method allows to vote for the forum message
	 * @param userID the user unique ID
	 * @param userSessionId the user's session id
	 * @param messageID the id of the message to be voted for
	 * @param voteFor true to say that the message is good, otherwise false (bad)
	 * @throws SiteException in case something goes wrong on the server, e.g. the message does not exist
	 */
	@Override
	public void voteForForumMessage( final int userID, final String userSessionId,
					 			     final int messageID, final boolean voteFor ) throws SiteException  {
		(new SecureServerAccess<Void>( getLocalHttpSession(), getThreadLocalRequest(), userID, userSessionId ) {
			protected Void action() throws SiteException {
				logger.info( "Voting " + (voteFor ? "for" : "against" ) +" the forum message " + messageID + " by user " + userID );
				
				//First check that the user has not voted yet, the following throws and exception if the user has voted
				try {
					CheckIfTheUserVotedExecutor checkIfVotedExec = new CheckIfTheUserVotedExecutor( messageID, userID );
					ConnectionWrapper<Void> checkIfVotedConnWrap = ConnectionWrapper.createConnectionWrapper( checkIfVotedExec );
					checkIfVotedConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				} catch ( MessageException ex ) {
					List<Integer> errorCodes = ex.getErrorCodes(); 
					if( errorCodes != null && errorCodes.contains( MessageException.YOU_HAVE_ALREADY_VOTED_FOR_THIS_MESSAGE ) ) {
						//The user has voted for this message, mark this in the cache
						forumCache.registerUserVote( messageID, userID );
					}
					
					//Throw the exception further
					throw ex;
				}
				
				//If the user has not voted yet, then vote for the given message
				VoteForForumMessageExecutor voteForMsgExec = new VoteForForumMessageExecutor( messageID, voteFor );
				ConnectionWrapper<Void> voteForMsgConnWrap = ConnectionWrapper.createConnectionWrapper( voteForMsgExec );
				voteForMsgConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				
				//The exception was not thrown, the user has not voted yet, update the cache, count his vote
				forumCache.voteForMessage( messageID, voteFor, userID );
				
				RememberForumMessageVoteExecutor rememberUserVoteExec = new RememberForumMessageVoteExecutor( messageID, userID, voteFor );
				ConnectionWrapper<Void> rememberUserVoteConnWrap = ConnectionWrapper.createConnectionWrapper( rememberUserVoteExec );
				rememberUserVoteConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				
				//If the vote was positive then give the user more money, in case the user does not vote for himself
				if( voteFor ) {
					try {
						ForumMessageData message = getForumMessageInternal( userID, messageID, false );
						if( userID != message.senderID ) {
							ServerSideUserManager.incrementUserGoldCount( message.senderID, POSITIVE_FORUM_VOTE_INCREMENT_GOLD );
						}
					} catch ( Exception e) {
						logger.error( "An error while trying to account (increment gold) for a positive " +
									  "vote for the forum message " + messageID );
					}
				}
				
				//Nothing the be returned here
				return null;
			}
		}).execute();
	}
	
	/**
	 * Allows to mark the forum message sender as deleted in case the user gets deleted
	 * @param userID the if of the user who is deleted
	 * @throws SiteException if smth bad happened
	 */
	public static void deleteMessageSender( final int userID ) throws SiteException {
		//This is a forum modification, has to be synchronized
		synchronized( forumModifSynch ) {
			DeleteSenderForumMessageExecutor deleteForumMsgSenderExec = new DeleteSenderForumMessageExecutor( userID );
			ConnectionWrapper<Void> deleteForumMsgSenderConnWrap = ConnectionWrapper.createConnectionWrapper( deleteForumMsgSenderExec ); 
			deleteForumMsgSenderConnWrap.executeQuery(ConnectionWrapper.XCURE_CHAT_DB);
		}
	}
}
