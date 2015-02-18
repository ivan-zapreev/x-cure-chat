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
 * The user interface package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.forum;

import java.util.List;
import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.data.ShortForumMessageData;
import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.client.data.ShortUserData;

import com.xcurechat.client.data.search.ForumSearchData;

import com.xcurechat.client.dialogs.ActionGridDialog;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.CaptchaTestFailedException;

import com.xcurechat.client.rpc.ForumManagerAsync;
import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.ServerSideAccessManager;

import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SmileyHandler;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.UserForumActivity;
import com.xcurechat.client.utils.widgets.ActionLinkPanel;
import com.xcurechat.client.utils.widgets.Button;
import com.xcurechat.client.utils.widgets.SmileSelectionDialogUI;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.TextBaseTranslitAndProgressBar;

/**
 * @author zapreevis
 * This is the send forum message dialog
 */
public class SendForumMessageDialogUI extends ActionGridDialog implements SmileSelectionDialogUI.SmileySelectionTarget {
	
	private static final int MAXIMUM_MESSAGE_BODY_VISIBLE_LINES_LENGTH = 7;
	
	//The title of the message
	private TextBox messageSubjectTextBox = new TextBox();
	//The body of the message
	private TextArea messageBodyTextArea = new TextArea();
	//The wrapper composite with the progress bar 
	private TextBaseTranslitAndProgressBar messageBodyWrapper = new TextBaseTranslitAndProgressBar( messageBodyTextArea, ShortForumMessageData.MAX_MESSAGE_LENGTH );

	//The forum message provided to us in the constructor of the class, might be a message we edit or a message we reply to
	private ForumMessageData originalForumMessage;
	
	//The forum message for the case of editing the existing forum message, otherwise a new forum message instance
	private ShortForumMessageData forumMessage;
	
	//Add the clear message button
	private final Button clearButton = new Button();
	
	//The action smiley link
	private ActionLinkPanel actionSmileysLink = new ActionLinkPanel( ServerSideAccessManager.getChatMessageSmilesLinkImageURL( ),
																	 titlesI18N.smilesLinkToolTip(),
																	 ServerSideAccessManager.getChatMessageSmilesLinkImageURL( ), "",
																	 titlesI18N.smilesLinkTitle(), new ClickHandler(){
																		@Override
																		public void onClick(ClickEvent event) {
																			SmileSelectionDialogUI.open();
																		}},
																	 true, true);
	
	//The action files link
	private ActionLinkPanel actionFilesLink = new ActionLinkPanel( ServerSideAccessManager.getChatMessageFileUnsetURL( ),
																  titlesI18N.filesLinkToolTip(),
																  ServerSideAccessManager.getChatMessageFileUnsetURL( ), "",
																  titlesI18N.filesLinkTitle(), new ClickHandler(){
																	 @Override
																	 public void onClick(ClickEvent event) {
																			//Ensure lazy loading
																			( new SplitLoad( true ) {
																				@Override
																				public void execute() {
																					 ForumFilesManagerUI dialog = new ForumFilesManagerUI( forumMessage, thisDialog );
																					 dialog.show();
																					 dialog.center();
																				}
																			}).loadAndExecute();
																	 }},
																  true, true);
	
	//if true then this is a reply message dialog
	private final boolean isReplyMessage;
	//if true then this is a new-section message dialog
	private final boolean isNewSection;
	//if true then this is an edit-section message dialog
	private final boolean isEditSection;
	//if true then we have a non-section message
	private final boolean isNonSectionMessage;
	//if true then this is a topic message dialog, i.e. we are replying to the Section message
	private final boolean isATopicMessage;
	
	//if true then we quote the message body in case of (isReplyMessage==true) otherwise not
	private final boolean isQuoteBody;
	
	//if true then we quote the message title, the latter is applicable if this is not a new section or topic message
	private final boolean isQuoteTitle;
	
	//If true then: Files link, Smileys link and clean button, of the dialog controls
	//should be enabled, otherwise false
	private boolean enabled = true;
	
	//True if this is the first time the instance of this dialog is shown
	private boolean isFirstShow = true;
	
	/**
	 * This constructor should be used for replying to a forum message or editing a forum message
	 * @param isReplyMessage if true then we reply to the given forum message, otherwise edit it
	 * @param forumMessage the forum message we reply to or edit
	 * @param isQuoteBody if true then we quote the message body in case of (isReplyMessage==true) otherwise not
	 */
	public SendForumMessageDialogUI( final ForumMessageData forumMessage, final boolean isReplyMessage, final boolean isQuoteBody ) {
		this( forumMessage, isReplyMessage, false, isQuoteBody );
	}
	
	/**
	 * A constructor creates a send forum message dialog.
	 * There are three types of messages:
	 * 1. A new topic message ( isNewTopic == true )
	 * 2. A new reply message ( isReplyMessage == true )
	 * 3. A message we edit ( isReplyMessage == false && isNewTopic == false )
	 * Note, ( isNewTopic == true && isReplyMessage == true ) is an invalid combination
	 * @param forumMessage the forum message we reply to
	 * @param isReplyMessage if true then we want to reply to a message
	 * @param isNewSection if true then this is a new section message
	 * @param isQuoteBody if true then we quote the message body in case of (isReplyMessage==true) otherwise not
	 */
	public SendForumMessageDialogUI( final ForumMessageData localForumMessage, final boolean isReplyMessage,
									  final boolean isNewSection, final boolean isQuoteBody ) {
		super( true, false, true, null );
		
		//Store the provided message type data
		this.isReplyMessage = isReplyMessage;
		this.isNewSection = isNewSection;
		this.isQuoteBody = isQuoteBody;
		this.isEditSection = !isNewSection && !isReplyMessage && localForumMessage.isForumSectionMessage();
		this.isNonSectionMessage = !isNewSection && !isEditSection;
		//Store the original message we create/edit/reply to
		originalForumMessage = localForumMessage;
		//Check if the original message is a section and we are replying to it or we are not replying to a message and this message is a topic
		this.isATopicMessage = ( isReplyMessage ) ? originalForumMessage.isForumSectionMessage() : originalForumMessage.isForumTopicMessage() ;
		this.isQuoteTitle = originalForumMessage.messageID != ShortForumMessageData.ROOT_FORUM_MESSAGE_ID && ! originalForumMessage.isForumSectionMessage();
		
		//Update the dialog's title
		updateDialogTitle();
		
		//Fill dialog with data
		populateDialog();
		
		//Set enabled elements
		setEnabledElements( true );
		
		//Take the copy of the message 
		final ShortForumMessageData forumMessage = localForumMessage.clone( true );
		if( isReplyMessage ) {
			//For the replying this message convert it in to a reply message, do not quote subject if it is a reply to a section message
			forumMessage.convertIntoAReplyMessage( SiteManager.getUserID(), isQuoteTitle, isQuoteBody );
		}
		
		//Set the current forum message data
		setMessageData( forumMessage );
	}
	
	/**
	 * @return true if the user needs CAPTCHA
	 */
	private boolean isCaptchaNeeded () {
		//Check if the user needs CAPTCHA
		final ShortUserData userData = SiteManager.getShortUserData();
		if( userData == null || UserForumActivity.isCaptchaNeeded( userData.getUserForumActivity() ) ) {
			//Add the robot protection in case this is a new topic or a reply message
			if( isReplyMessage || isNewSection ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Allows to re-set the interface components based on the new message data.
	 * The message is used as is, but in case the new-topic or reply message
	 * the sender ID is enforced.
	 * @param localForumMessage the message data to be set
	 */
	private void setMessageData( final ShortForumMessageData localForumMessage ) {
		 //Save the message data
		forumMessage = localForumMessage;
		
		if( isNewSection || isReplyMessage ) {
			//Enforce the sender ID, just in case
			forumMessage.senderID = SiteManager.getUserID();
		} 
		
		//Update the UI components
		messageSubjectTextBox.setText( localForumMessage.messageTitle );
		messageBodyTextArea.setText( localForumMessage.messageBody );
		setAttachedFiles( ! localForumMessage.attachedFileIds.isEmpty() );
	}
	
	/**
	 * Allows to set the attached message files indicator on/off
	 * @param exist true if there are attached file, otherwise false
	 */
	void setAttachedFiles( final boolean exist ) {
		if( exist ) {
			actionFilesLink.setActionImageUrlDisbl( ServerSideAccessManager.getChatMessageFileSetURL( ) );
			actionFilesLink.setActionImageUrlEnbl( ServerSideAccessManager.getChatMessageFileSetURL( ) );
		} else {
			actionFilesLink.setActionImageUrlDisbl( ServerSideAccessManager.getChatMessageFileUnsetURL( ) );
			actionFilesLink.setActionImageUrlEnbl( ServerSideAccessManager.getChatMessageFileUnsetURL( ) );
		}
	}
	
	/**
	 * Allows to place the smile's internal code string representation to the current
	 * cursor position in the messages TextBox. Works only if the send-message dialog
	 * components are not disabled.
	 * @param smileyInternalCodeString the code string to insert into the message
	 */
	@Override
	public void addSmileStringToMessage(final String smileyInternalCodeString){
		if( enabled ) {
			TextBaseTranslitAndProgressBar.insertStringIntoTextBoxWrapper(smileyInternalCodeString, messageBodyWrapper);
		}
	}
	
	@Override
	protected void populateDialog() {
		//Add the title grid
		addNewGrid( 1, false, "", false);
		Label messageSubjectLabel = InterfaceUtils.getNewFieldLabel( titlesI18N.messageSubjectFieldTitle(), false );
		addToGrid( FIRST_COLUMN_INDEX, messageSubjectLabel, false, false );
		messageSubjectTextBox.setMaxLength( ShortForumMessageData.MAX_MESSAGE_TITLE_LENGTH );
		messageSubjectTextBox.addStyleName( CommonResourcesContainer.MESSAGE_TITLE_STYLE );
		/*messageSubjectTextBox.setVisibleLength( MAXIMUM_MESSAGE_SUBJECT_VISIBLE_LENGTH );*/
		addToGrid( SECOND_COLUMN_INDEX, new TextBaseTranslitAndProgressBar( messageSubjectTextBox, ShortForumMessageData.MAX_MESSAGE_TITLE_LENGTH), false, true );
		
		//Add the message body grid
		addNewGrid( 1, 1, false, "", false);
		/*messageBodyTextArea.setCharacterWidth( MESSAGE_BODY_ONE_LINE_LENGTH );*/
		messageBodyTextArea.addStyleName( CommonResourcesContainer.FORUM_MESSAGE_BODY_STYLE );
		messageBodyTextArea.setVisibleLines( MAXIMUM_MESSAGE_BODY_VISIBLE_LINES_LENGTH );
		addToGrid( FIRST_COLUMN_INDEX, messageBodyWrapper, false, false );
		
		//Check if the user needs CAPTCHA, if yes then add it,
		//note that here we get the first captcha image
		if( isCaptchaNeeded() ) {
			this.addCaptchaTestGrid( true, true );
		}
		
		//Add the grid with the smiley and attached files links and also close, clean and save buttons

		//05 Add action buttons and links
		FlexTable actionGrid = addGridActionElements(4, true, FOURTH_COLUMN_INDEX, true, SIXTH_COLUMN_INDEX, false, 0, true);
		
		//Add the action attached files link
		actionGrid.setWidget( 0, FIRST_COLUMN_INDEX, actionFilesLink );
		
		//Add the action smiley dialog link
		actionGrid.setWidget( 0, SECOND_COLUMN_INDEX, actionSmileysLink );
		
		//Add the progress indicator
		actionGrid.setWidget( 0, THIRD_COLUMN_INDEX, progressBarUI);
		//Add the clear message button, is not enabled in the edit message dialog
		clearButton.setEnabled( isNewSection || isReplyMessage );
		clearButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		clearButton.setText( titlesI18N.resetButtonTitle() );
		clearButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//Clean the dialog's data, delete attached files on the server;
				cleanForumMessage( false, isNewSection || isReplyMessage );
			}
		});
		actionGrid.setWidget( 0, FIFTH_COLUMN_INDEX, clearButton );
	}
	
	/**
	 * Allows to update the dialog title, this should be called after the room ID is set
	 */
	private void updateDialogTitle() {
		if( isNewSection ) {
			this.setText( titlesI18N.newForumSectionDialogTitle() );
		} else {
			if( isReplyMessage ) {
				if( originalForumMessage.isForumSectionMessage() ) {
					//Otherwise we are replying to a message or writing into a topic
					this.setText( titlesI18N.newForumTopicDialogTitle() );
				} else {
					//If the original message is a section message then we are creating a new topic
					this.setText( titlesI18N.replyForumMessageDialogTitle() );
				}
			} else {
				this.setText( titlesI18N.editForumMessageDialogTitle() );
			}
		}
	}
	
	/**
	 * Allows to collect the message's data which is currently set in this dialog
	 * Also substitute the smiley codes in the title and in the body
	 * @return the chat message data
	 */
	ShortForumMessageData collectMessageData( ) {
		this.forumMessage.messageTitle = SmileyHandler.substituteSmilesWithSmileCodes( messageSubjectTextBox.getValue().trim() );
		//Do not remove formatting, i.e. new lines in the message body
		this.forumMessage.messageBody = SmileyHandler.substituteSmilesWithSmileCodes( messageBodyTextArea.getValue().trim(), false );
		return this.forumMessage;
	}
	
	private void setEnabledElements( final boolean enabled ){
		this.enabled = enabled;
		//Enable action buttons
		setLeftEnabled( enabled );
		setRightEnabled( enabled );
		messageSubjectTextBox.setEnabled( enabled );
		//Only enable the clean button if it is a new topic or a reply message
		clearButton.setEnabled( enabled && ( isNewSection || isReplyMessage ) );
		if( !enabled ) {
			//Hide the smiley selection dialog, since we are saving the message
			SmileSelectionDialogUI.unbindAndHide();
		}
		//Make the smileys and the attached files links disabled
		//If it is the new section then we do not have attached
		//files and do not need the smileys dialog
		actionSmileysLink.setEnabled( enabled && this.isNonSectionMessage );
		//The files link should not be enbled if this is a section message, and if this is a topic message not browsed by the admin
		actionFilesLink.setEnabled( enabled && this.isNonSectionMessage && ( !this.isATopicMessage || SiteManager.isAdministrator() ) );
		//If it is the new section then we do not have to specify any message body
		messageBodyTextArea.setEnabled( enabled );
	}
	
	/**
	 * Allows to clean up the send message dialog UI elements, closes the dialog when done, if needed.
	 * @param closeOnceCleaned true for closing the dialog after cleaning, i.e. this is not triggered
	 * by pressing the clean button in the dialog, but rather a close or a send/save button
	 * @param cleanedAttFiles if true then the attached files were removed from the server before
	 *  calling this message, this is only applicable for a reply message and for a new-topic message
	 */
	private void cleanForumMessageDialogUI( final boolean closeOnceCleaned, final boolean cleanedAttFiles ) {
		final ShortForumMessageData newData;
		if( isNewSection || closeOnceCleaned ) {
			//Writing a new message
			newData = new ShortForumMessageData();
			newData.senderID = SiteManager.getUserID();
		} else {
			//Take the original message, do not copy the attached files, they
			//updated list thereof should be in forumMessage.attachedFileIds
			newData = originalForumMessage.clone( false );
			if( isReplyMessage ) {
				//Convert the message into a reply message, do not quote subject if it is a reply to a section message
				newData.convertIntoAReplyMessage( SiteManager.getUserID(), isQuoteTitle, isQuoteBody );
			}
		}
		//If the attached files of the former message were not 
		//cleaned then copy them into the new message, for safety
		if( !cleanedAttFiles ) {
			newData.attachedFileIds.addAll( forumMessage.attachedFileIds );
		}
		//Set the cleaned up message data
		setMessageData( newData );
		//Clean up the captha field
		cleanCaptchaTextBox();
		//Clean up the progress bar
		progressBarUI.cleanProgressBar();
		//Hide the dialog
		if( closeOnceCleaned ) {
			hide();
		}
	}
	
	/**
	 * Allows to clean the Forum message dialog's data
	 * and delete message-attached files on the server;
	 * @param closeOnceCleaned if true then once the data is cleaned we close the dialog, 
	 * e.g. it is either on a succesful message send or the dialog close
	 * @param cleanServer if true we clean the client and the server (delete the attached
	 *  files), otherwise we only clean up the dialog data
	 */
	private void cleanForumMessage( final boolean closeOnceCleaned, final boolean cleanServer ) {
		//Disable all, we can not even close this dialog while cleaning
		setEnabledElements( false );
		
		if( ! cleanServer ) {
			//Clean up the interface only, if the message is closed after cleaning then we
			//force removing of the attached files from the interface.
			cleanForumMessageDialogUI( closeOnceCleaned, closeOnceCleaned );
			//Enable it's components
			setEnabledElements( true );
		} else {
			//Get the list of file IDs to delete from the server
			final List<Integer> fileIDs = new ArrayList<Integer>();
			for( ShortFileDescriptor fileDesc: forumMessage.attachedFileIds ){
				fileIDs.add( fileDesc.fileID );
			}
			if( fileIDs.isEmpty() ) {
				//If there are no files to delete, then just clean the interface
				cleanForumMessageDialogUI( closeOnceCleaned, true );
				setEnabledElements( true );
			} else {
				//Ensure lazy loading
				(new SplitLoad( true ){
					@Override
					public void execute() {
						//Perform the server call if there are any files to be deleted
						CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>(progressBarUI) {
							public void onSuccessAct(Void result) {
								cleanForumMessageDialogUI( closeOnceCleaned, true );
								setEnabledElements( true );
							}
							public void onFailureAct(final Throwable caught) {
								(new SplitLoad( true ) {
									@Override
									public void execute() {
										//Report the error
										ErrorMessagesDialogUI.openErrorDialog( caught );
									}
								}).loadAndExecute();
								//Clean-up the dialog, not all files have been removed
								cleanForumMessageDialogUI( false, false );
								//Use the recovery method
								recover();
							}
						};
						ForumManagerAsync forumManagerObj = RPCAccessManager.getForumManagerAsync();
						forumManagerObj.deleteFiles( SiteManager.getUserID(), SiteManager.getUserSessionId(), fileIDs, callback );
					}
					@Override
					public void recover() {
						//Enable control elements
						setEnabledElements( true );
					}
				}).loadAndExecute();
			}
		}
	}
	
	/**
	 * @return the left-button caption
	 */
	@Override
	protected String getLeftButtonText(){
		return titlesI18N.closeButtonTitle();
	}
	
	/**
	 * @return the right-button caption
	 */
	@Override
	protected String getRightButtonText() {
		//If we edit a message then the title should be "Save"
		if( !isReplyMessage && ! isNewSection ) {
			return titlesI18N.saveButton();
		} else {
			return titlesI18N.sendButton();
		}
	}
	
	@Override
	protected void actionLeftButton() {
		//Here we close the dialog due to the user pushing the close button
		//Clean the dialog's data, delete attached files on the server;
		cleanForumMessage( true, isNewSection || isReplyMessage );
	}
	
	@Override
	protected void actionRightButton() {
		//Send the new message or save the old one, first validate the message content
		setEnabledElements( false );
		final ShortForumMessageData completeMessage = collectMessageData( );
		try {
			//Check that the captha response is set
			final String captchaAnswer = getCaptchaAnswerText();
			//Validate that the forum message is ready for being sent to the server
			SiteException exception = null;
			try {
				//In case of a new section or topic there can be no message body
				completeMessage.validate( this.isNewSection || this.isEditSection || this.isATopicMessage );
			} catch ( SiteException ex ) {
				exception = ex;
			} finally {
				//The CAPTCHA message is only needed when one replies to a message or creates a new topic
				if( isCaptchaNeeded() ) {
					//Check for the empty CAPTCHA FIELD
					if( captchaAnswer.trim().isEmpty() ) {
						//If the CAPTCHA is not set then
						if( exception != null ) {
							//If there are other exceptions, just add another one
							exception.addErrorMessage( I18NManager.getErrors().captchaEmptyAnswerError() );
						} else {
							//If there are no other exceptions then create one
							exception = new CaptchaTestFailedException( CaptchaTestFailedException.EMPTY_CAPTCHA_RESPONSE_ERR );
						}
					}
				}
				//Throw the exception if any
				if( exception != null ) {
					throw exception;
				}
			}

			//Ensure lazy loading
			(new SplitLoad( true ){
				@Override
				public void execute() {
					//Perform the server call if there are any files to be deleted
					CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>(progressBarUI) {
						public void onSuccessAct(Void result) {
							cleanForumMessage( true, false );
							setEnabledElements( true );
							//If we have created a new new topic, then we perform the search all over again to show it in the search results
							if( isNewSection ) {
								//Just do the initial search to see the list of topics
								ForumSearchManager.doSearch( MessagesStackNavigator.getInitialSearchStackElement() );
							} else {
								if( isReplyMessage ) {
									//Get to view the replies for the given message
									 final ForumSearchData viewRepliesData = new ForumSearchData();
									 viewRepliesData.baseMessageID = originalForumMessage.messageID;
									ForumSearchManager.doSearch( new MessageStackElement( viewRepliesData, originalForumMessage ) );
								} else {
									//Go to the first page of the current view, because this
									//is where the latest updated message will come
									ForumSearchManager.doFirstPageSearch( );
								}
							}
						}
						public void onFailureAct(final Throwable caught) {
							(new SplitLoad( true ) {
								@Override
								public void execute() {
									//Report the error
									ErrorMessagesDialogUI.openErrorDialog( caught );
								}
							}).loadAndExecute();
							if( isCaptchaNeeded() ) {
								//Update captcha image
								updateCaptchaImage();
							}
							//Use the recovery method
							recover();
						}
					};
					//Perform the server call for sending the forum message
					ForumManagerAsync forumManagerObj = RPCAccessManager.getForumManagerAsync();
					if( isNewSection || isReplyMessage ) {
						forumManagerObj.sendMessage( SiteManager.getUserID(), SiteManager.getUserSessionId(),
													 completeMessage, captchaAnswer, callback );
					} else {
						forumManagerObj.updateMessage( SiteManager.getUserID(), SiteManager.getUserSessionId(),
													   completeMessage, callback );
					}
				}
				@Override
				public void recover() {
					//Enable the controls
					setEnabledElements( true );
				}
			}).loadAndExecute();
		} catch(final SiteException e) {
			setEnabledElements( true );
			(new SplitLoad( true ) {
				@Override
				public void execute() {
					//Report the error
					ErrorMessagesDialogUI.openErrorDialog( e );
				}
			}).loadAndExecute();
		}
	}
	
	@Override
	public void show() {
		//Update the captha test image if needed, and if it is not
		//the first show because then the image is already here
		if( ! isFirstShow && isCaptchaNeeded() ) {
			updateCaptchaImage();
		}
		//Show the dialog
		super.show();
		//Mark this show as not the first one
		isFirstShow = false;
	}

}
