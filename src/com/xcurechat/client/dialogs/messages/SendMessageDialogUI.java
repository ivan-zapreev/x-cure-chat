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
package com.xcurechat.client.dialogs.messages;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HTML;

import com.xcurechat.client.SiteManager;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.data.PrivateMessageData;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.MessageManagerAsync;
import com.xcurechat.client.rpc.ServerSideAccessManager;

import com.xcurechat.client.rpc.exceptions.SiteException;


import com.xcurechat.client.dialogs.ActionGridDialog;
import com.xcurechat.client.dialogs.PagedActionGridDialog;
import com.xcurechat.client.dialogs.profile.ViewUserProfileDialogUI;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.StringUtils;
import com.xcurechat.client.utils.widgets.ActionLinkPanel;
import com.xcurechat.client.utils.widgets.SmileSelectionDialogUI;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.TextBaseTranslitAndProgressBar;

/**
 * @author zapreevis
 * This dialog is used for sending personal (private) messages
 */
public class SendMessageDialogUI extends ActionGridDialog implements SmileSelectionDialogUI.SmileySelectionTarget {
	
	//The maximum length of the message body
	private static final int MAXIMUM_MESSAGE_BODY_VISIBLE_LINES_LENGTH = 10;
	
	//The coefficient for the length of the message body that we will keep in case the message body gets too long 
	private static final double MESSAGE_BODY_LENGTH_TRIMMING_COEFFICIENT = ( 2.0 / 3.0);
	
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
	
	//The name of the person we send the message to
	private final Label messageRecepientLabel = new Label();
	//The title of the message
	private final TextBox messageSubjectTextBox = new TextBox();
	//The object that wraps around the text box of the off-line message subject
	private final TextBaseTranslitAndProgressBar messageTextSubjWrapper = new TextBaseTranslitAndProgressBar( messageSubjectTextBox, PrivateMessageData.MAXIMUM_MESSAGE_SUBJECT_LENGTH);
	
	//The body of the message
	private final TextArea messageBodyTextArea = new TextArea();
	//The object that wraps around the text box of the off-line message body
	private final TextBaseTranslitAndProgressBar messageTextBodyWrapper = new TextBaseTranslitAndProgressBar( messageBodyTextArea, PrivateMessageData.MAXIMUM_MESSAGE_BODY_LENGTH );
	//True if the user selection for the recipient is active
	private boolean isRecepientSelectEnabled = true;
	
	private void setEnabledElements(final boolean enabledLeftActionButton,
									final boolean enabledRightActionButton,
									final boolean enabledOthers){
		setLeftEnabled( enabledLeftActionButton );
		setRightEnabled( enabledRightActionButton );
		messageSubjectTextBox.setEnabled( enabledOthers );
		messageBodyTextArea.setEnabled( enabledOthers );
		isRecepientSelectEnabled = enabledOthers;
	}

	//The ID of the user we send the message to
	private int toUserID;
	//The login name of the user we send the message to 
	private String toUserLoginName;
	//The data of the message we reply to, if any
	private PrivateMessageData replyMsgData = null;
	
	//Contains the counter for the number of opened send message dialogs.
	//This is used to prevent sending private messages to users through
	//the chain of send msg dialogs
	private static int openSendMessageDialogCounter = 0;
	
	/**
	 * Allows to detect if a send message dialog is open
	 * @return true if there is a send message dialog is open,
	 *              even if it is currently invisible.
	 */
	public static boolean isSendMessageDialogOpen() {
		return openSendMessageDialogCounter > 0;
	}
	
	/**
	 * The main constructor
	 * @param parentDialog the parent dialog, i.e. the one we open this dialog from
	 */
	public SendMessageDialogUI(final DialogBox parentDialog) {
		this(parentDialog, UserData.UNKNOWN_UID, null, null);
	}

	/**
	 * The constructor that has to be used when we send
	 * a message to a particular user 
	 * @param parentDialog the parent dialog, i.e. the one we open this dialog from
	 * @param toUserID the ID of the user we send the message to
	 * @param toUserLoginName the login name of the user we send the message to
	 * @param replyMsgData if not null then this is the message we reply to
	 */
	public SendMessageDialogUI( final DialogBox parentDialog, final int toUserID,
								final String toUserLoginName, final PrivateMessageData replyMsgData) {
		super( true, false, true, parentDialog );
		
		//Store the local value of the message we reply to
		this.replyMsgData = replyMsgData;
		
		//Increment the number of opened send message dialogs
		openSendMessageDialogCounter++;
		this.addCloseHandler( new CloseHandler<PopupPanel>(){
			public void onClose( CloseEvent<PopupPanel> e ) {
				if( e.getTarget() == thisDialog ) {
					//The send message is closed, decrement the number of
					//opened send message dialogs
					if( openSendMessageDialogCounter > 0 ) {
						openSendMessageDialogCounter--;
					}
				}
			}
		} );
		
		//Set the message recepient
		setMessageRecepient(toUserID, toUserLoginName);
		
		//Set title and style
		this.setText(titlesI18N.sendPersonalMessageDialogTitle() );
		this.setStyleName( CommonResourcesContainer.USER_DIALOG_STYLE_NAME );
		
		//Fill dialog with data
		populateDialog();		
	}
	
	/**
	 * Sets the message recipient, or resets it to "undefined"
	 * @param toUserID the ID of the user we send the message to
	 * @param toUserLoginName the login name of the user we send the message to 
	 */
	public void setMessageRecepient(final int toUserID, final String toUserLoginName) {
		this.toUserID = toUserID;
		this.toUserLoginName = toUserLoginName;
		
		if( this.toUserID != UserData.UNKNOWN_UID && this.toUserLoginName != null ) {
			messageRecepientLabel.setStyleName( CommonResourcesContainer.DEFINED_MESSAGE_RECEPIENT_LINK );
			messageRecepientLabel.setText( toUserLoginName );
		} else {
			messageRecepientLabel.setStyleName( CommonResourcesContainer.UNDEFINED_MESSAGE_RECEPIENT_LINK );
			messageRecepientLabel.setText( titlesI18N.undefinedRecepientFieldValue() );
		}
	}
	
	/**
	 * @return the left-button caption
	 */
	@Override
	protected String getLeftButtonText(){
		return titlesI18N.cancelButton();
	}

	/**
	 * @return the right-button caption
	 */
	@Override
	protected String getRightButtonText(){
		return titlesI18N.sendButton();
	}
		
	@Override
	protected void actionLeftButton() {
		hide();
	}

	@Override
	protected void actionRightButton() {
		//Disable the controls
		setEnabledElements( false, false, false );
		
		//Send a personal message
		final PrivateMessageData message = new PrivateMessageData();
		message.setToUID( toUserID );
		//We do not need to set the recepient's login name here
		message.setMessageTitle( messageSubjectTextBox.getText() );
		message.setMessageBody( messageBodyTextArea.getText() );
		try{
			//Validate the message before sending it
			message.validateAndComplete();
			
			//Ensure lazy loading
			(new SplitLoad( true ){
				@Override
				public void execute() {
					//Do Asynchronous call
					CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>(progressBarUI) {
						public void onSuccessAct(Void result) {
							//Update the parent dialog if it is a PagedActionGridDialog
							if( (parentDialog != null) && ( parentDialog instanceof PagedActionGridDialog<?> ) ) {
								( (PagedActionGridDialog<?>) parentDialog).updateActualData(); 
							} 
							//Hide the dialog window
							hide();
						}
						public void onFailureAct(final Throwable caught) {
							(new SplitLoad( true ) {
								@Override
								public void execute() {
									ErrorMessagesDialogUI.openErrorDialog(caught);
								}
							}).loadAndExecute();
							//Use the recovery method
							recover();
						}
					};
					
					MessageManagerAsync msgManagerAsync = RPCAccessManager.getMessageManagerAsync();
					msgManagerAsync.sendSimpleMessage( SiteManager.getUserID(), SiteManager.getUserSessionId(), message, replyMsgData != null, callback);
				}
				@Override
				public void recover() {
					setEnabledElements(true, true, true);
				}
			}).loadAndExecute();
		} catch ( final SiteException caught ){
			(new SplitLoad( true ) {
				@Override
				public void execute() {
					ErrorMessagesDialogUI.openErrorDialog(caught);
				}
			}).loadAndExecute();
			setEnabledElements(true, true, true);
		}
	}
	
	@Override
	protected void populateDialog() {
		//01 Add the recipient fields
		addNewGrid( 1, 1, false, "", false);
		Label recepientLabel = InterfaceUtils.getNewFieldLabel( titlesI18N.recepientFieldTitle(), true );
		HorizontalPanel recepientPanel = new HorizontalPanel();
		recepientPanel.add( recepientLabel );
		recepientPanel.add( new HTML("&nbsp;") );
		if( replyMsgData != null ) {
			//View the recepient's profile link tool tip
			messageRecepientLabel.setTitle( titlesI18N.clickToViewRecepientProfileTip() );
		} else {
			//Select the recipient link tool tip
			messageRecepientLabel.setTitle( titlesI18N.clickToSelectTheRecepientTip() );
		}
		messageRecepientLabel.addClickHandler( new ClickHandler(){
			public void onClick(ClickEvent e) {
				if( replyMsgData != null ) {
					//Ensure lazy loading
					final SplitLoad executor = new SplitLoad( true ) {
						@Override
						public void execute() {
								//View user profile in case this is a reply message
								ViewUserProfileDialogUI userProfile = new ViewUserProfileDialogUI( toUserID, toUserLoginName,
																									thisDialog, true);
								userProfile.show();
								userProfile.center();
						}
					};
					executor.loadAndExecute();
				} else {
					if( isRecepientSelectEnabled ) {
						//Ensure lazy loading
						final SplitLoad executor = new SplitLoad( true ) {
							@Override
							public void execute() {
								SelectMessageRecepientDialogUI userSelectDialog = new SelectMessageRecepientDialogUI( thisDialog );
								userSelectDialog.show();
								userSelectDialog.center();
							}
						};
						executor.loadAndExecute();
					}
				}
			}
		});
		recepientPanel.add( messageRecepientLabel ); 
		addToGrid( FIRST_COLUMN_INDEX, recepientPanel, false, false );

		//02 Add the message title TextBox
		addNewGrid( 1, false, "", false);
		Label messageSubjectLabel = InterfaceUtils.getNewFieldLabel( titlesI18N.messageSubjectFieldTitle(), false );
		addToGrid( FIRST_COLUMN_INDEX, messageSubjectLabel, false, false );
		messageSubjectTextBox.setStyleName( CommonResourcesContainer.MESSAGE_TITLE_TEXT_BOX_STYLE_NAME );
		messageSubjectTextBox.setMaxLength( PrivateMessageData.MAXIMUM_MESSAGE_SUBJECT_LENGTH );
		/*messageSubjectTextBox.setVisibleLength( MAXIMUM_MESSAGE_SUBJECT_VISIBLE_LENGTH );*/
		//Set the reply message title, if applicable
		messageSubjectTextBox.setText( getReplyMessageTitle() );
		addToGrid( SECOND_COLUMN_INDEX, messageTextSubjWrapper, false, true );
		
		//03 Add the message body Text Area
		addNewGrid( 1, 1, false, "", false);
		/*messageBodyTextArea.setCharacterWidth( PrivateMessageData.MESSAGE_BODY_ONE_LINE_LENGTH );*/
		messageBodyTextArea.setVisibleLines( MAXIMUM_MESSAGE_BODY_VISIBLE_LINES_LENGTH );
		messageBodyTextArea.addStyleName( CommonResourcesContainer.MESSAGE_BODY_STYLE );
		//Set the reply message body, if applicable
		messageBodyTextArea.setText( getReplyMessageBody() );
		addToGrid( FIRST_COLUMN_INDEX, messageTextBodyWrapper, false, false );
		
		//04 Add action buttons, the smiley dialog link and the progress bar
		addNewGrid( 1, false, "", false);
		FlexTable table = addGridActionElements(1, true,SECOND_COLUMN_INDEX, true, FOURTH_COLUMN_INDEX, true, THIRD_COLUMN_INDEX, false);
		table.setWidget( 0, FIRST_COLUMN_INDEX, actionSmileysLink);
	}
	
	/**
	 * @return the reply message title if it is a reply message, otherwise an empty string
	 */
	private String getReplyMessageTitle() {
		String replyMsgTitle = StringUtils.makeReplyMessageTitle( MessagesManagerDialogUI.getMessageTitle( replyMsgData, titlesI18N ) );
		if( replyMsgTitle.length() > PrivateMessageData.MAXIMUM_MESSAGE_SUBJECT_LENGTH ) {
			replyMsgTitle = replyMsgTitle.substring(0, PrivateMessageData.MAXIMUM_MESSAGE_SUBJECT_LENGTH);
		}
		return replyMsgTitle;
	}
	
	/**
	 * @return the reply message body if it is a reply message, otherwise an empty string
	 */
	private String getReplyMessageBody() {
		final String messageBody = StringUtils.makeReplyMessageBody( ViewMessageDialogUI.getFormattedMessageBody( replyMsgData, true ) );
		String replyMsgBody = ( messageBody.trim().isEmpty() ? "" : "\n\n" ) + messageBody;
		if( replyMsgBody.length() > PrivateMessageData.MAXIMUM_MESSAGE_BODY_LENGTH ) {
			replyMsgBody = replyMsgBody.substring(0, (int) ( PrivateMessageData.MAXIMUM_MESSAGE_BODY_LENGTH * MESSAGE_BODY_LENGTH_TRIMMING_COEFFICIENT ) );
		}
		return replyMsgBody;
	}

	@Override
	public void addSmileStringToMessage(String smileyInternalCodeString) {
		TextBaseTranslitAndProgressBar.insertStringIntoTextBoxWrapper(smileyInternalCodeString, messageTextBodyWrapper);
	}

}
