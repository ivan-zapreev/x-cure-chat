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

import java.util.List;
import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.CloseEvent;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HTML;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.MessageTextToFlowPanel;
import com.xcurechat.client.utils.SmileyHandler;
import com.xcurechat.client.utils.SplitLoad;

import com.xcurechat.client.chat.RoomsManagerUI;
import com.xcurechat.client.data.ShortPrivateMessageData;
import com.xcurechat.client.data.PrivateMessageData;

import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.popup.RoomInfoPopupPanel;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.RoomManagerAsync;
import com.xcurechat.client.rpc.MessageManagerAsync;

import com.xcurechat.client.dialogs.ActionGridDialog;
import com.xcurechat.client.dialogs.PagedActionGridDialog;
import com.xcurechat.client.dialogs.room.RoomDialogUI;
import com.xcurechat.client.dialogs.room.RoomUserAccessDialogUI;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This dialog is used for sending personal (private) messages
 */
public class ViewMessageDialogUI extends ActionGridDialog {
	
	//If this is the room access request then we need to show the room
	private Label roomNameLinkLabel = new Label();
	
	//The scroll panel that should contain the flow panel with the message body
	private final SimplePanel messageBodyScrollPanel = new SimplePanel();
	
	//The flow panel that will contain the message body widgets
	private FlowPanel messageBodyPanel = new FlowPanel();
	
	private void setEnabledElements(final boolean enabledLeftActionButton,
									final boolean enabledRightActionButton,
									final boolean othersEnables) {
		setLeftEnabled( enabledLeftActionButton );
		if( ! isSentByThisUser && ! isSenderRecepientUnknown ) {
			//If the message was sent by this user then the right action button
			//should stay disabled, since the user can not reply to himself  
			setRightEnabled( enabledRightActionButton );
		}
		isRoomClickEnabled = othersEnables;
	}

	//The short description of the message as provided in the class constructor 
	private ShortPrivateMessageData shortMessageData;
	//Message type markers
	private final boolean isSimpleMessage;
	private final boolean isRoomAccessRequestMessage;
	private final boolean isRoomAccessGrantedMessage;
	private final boolean isRoomAccessMessage;
	//Did this user send this message or not?
	private final boolean isSentByThisUser;
	//Contains the enabled status for the room link click listener
	private boolean isRoomClickEnabled = false;
	//Contains true if the sender or the recever of the message does not exist, i.e. it was deleted
	private boolean isSenderRecepientUnknown = false;
	
	//The instance of the rooms Manager
	private final RoomsManagerUI roomsManager;
	
	/**
	 * The constructor that has to be used when we view a message of a particular user 
	 * @param autoHide true for autohide
	 * @param modal true for a modal dialog
	 * @param parentDialog the parent dialog, i.e. the one we open this dialog from
	 * @param shortMessageData the short message data needed to initialize the dialog
	 * @param roomsManager the instance of the rooms manager 
	 */
	public ViewMessageDialogUI(final boolean autoHide, final boolean modal, final DialogBox parentDialog,
								final ShortPrivateMessageData shortMessageData, final RoomsManagerUI roomsManager ) {
		super( false, autoHide, modal, parentDialog );
		
		//Store the rooms manager
		this.roomsManager = roomsManager;
		
		//Set the message short data, and initialize markers
		this.shortMessageData = shortMessageData;
		isSimpleMessage = (shortMessageData.getMessageType() == ShortPrivateMessageData.SIMPLE_MESSAGE_TYPE) ||
						  (shortMessageData.getMessageType() == ShortPrivateMessageData.FORUM_REPLY_NOTIFICATION_MESSAGE_TYPE );
		isRoomAccessRequestMessage = (shortMessageData.getMessageType() == ShortPrivateMessageData.ROOM_ACCESS_REQUEST_MESSAGE_TYPE);
		isRoomAccessGrantedMessage = (shortMessageData.getMessageType() == ShortPrivateMessageData.ROOM_ACCESS_GRANTED_MESSAGE_TYPE);
		isRoomAccessMessage = isRoomAccessRequestMessage || isRoomAccessGrantedMessage;
		isSentByThisUser = ( shortMessageData.getFromUID() == SiteManager.getUserID() );
		
		//Set title and style
		if( isSimpleMessage ) {
			if( isSentByThisUser ) {
				this.setText(titlesI18N.viewSimplePersonalMessageOutDialogTitle() );
			} else {
				this.setText(titlesI18N.viewSimplePersonalMessageInDialogTitle() );
			}
		} else {
			if( isRoomAccessRequestMessage ) {
				if( isSentByThisUser ) {
					this.setText(titlesI18N.viewRoomAccessRequestOutDialogTitle() );
				} else {
					this.setText(titlesI18N.viewRoomAccessRequestInDialogTitle() );
				}
			} else {
				if( isRoomAccessGrantedMessage ) {
					this.setText(titlesI18N.viewRoomAccessNotificationDialogTitle() );
				}
			}
		}
		this.setStyleName( CommonResourcesContainer.USER_DIALOG_STYLE_NAME );
		
		//Update the parent dialog if it is an paged action grid dialog.
		//There ase several possible cases when the list of messages in the 
		//parent MessagesManagerDialogUI has to be updated, but instead of 
		//considering all of them in separate we just update the dialog on exit.
		this.addCloseHandler( new CloseHandler<PopupPanel>(){
			public void onClose(CloseEvent<PopupPanel> e) {
				if( ( e.getTarget() == thisDialog ) && ( parentDialog != null ) &&
					( parentDialog instanceof PagedActionGridDialog<?> ) ) {
					( (PagedActionGridDialog<?>) parentDialog).updateActualData();
				}
			}
		} );
		
		//Fill dialog with data
		populateDialog();
		
		//Load message body from the server
		loadMessageBody();
	}
	
	/**
	 * Allows to get the offline message text.
	 * @param message the private message data, can be null
	 * @param accountForURLs if true then we account for URLs by assuming that the maximum visible URL Length is URLWidget.MAX_VISIBLE_URL_LENGTH
	 * @return the resulting formatted message body, not null, if the message body was null then we return an empty string.
	 */
	public static String getFormattedMessageBody( final PrivateMessageData message, final boolean accountForURLs ) {
		String msgBody = null;
		
		//If the message is not null
		if( message != null ) {
			//Get the message body content
			switch( message.getMessageType() ) {
				case PrivateMessageData.ROOM_ACCESS_REQUEST_MESSAGE_TYPE:
					msgBody = I18NManager.getInfoMessages().pleaseAddMeToYourRoomRequest( message.getToUserName(), message.getRoomName(), message.getFromUserName() ); 
					break;
				case PrivateMessageData.ROOM_ACCESS_GRANTED_MESSAGE_TYPE:
					msgBody = I18NManager.getInfoMessages().youWereAddedToTheRoom( message.getToUserName(), message.getRoomName(), message.getFromUserName() ); 
					break;
				case PrivateMessageData.FORUM_REPLY_NOTIFICATION_MESSAGE_TYPE:
					msgBody = I18NManager.getInfoMessages().repliedToForumMessage( message.getMessageBody() ); 
					break;
				case PrivateMessageData.SIMPLE_MESSAGE_TYPE:
				default:
					msgBody = message.getMessageBody();
			}
		}
		
		return ( msgBody == null ? "" : msgBody );
	}
	
	/**
	 * Sets the full message data to the shortMessageData variable.
	 * It is then fasted to  PrivateMessageData when action buttons are used
	 * Also, here we update the interface elements with the newly arrived data.
	 * @param message the full message data
	 */
	private void setMessageData( final PrivateMessageData message ) {
		//Save the message data
		this.shortMessageData = message;
		
		//Set the message body to the text area and also if it is
		//a room access request, then update the room label
		final String messageBody = getFormattedMessageBody( message, true );
		
		//Populate the message body flow panel
		final MessageTextToFlowPanel converter = new MessageTextToFlowPanel( messageBodyPanel, messageBody,
																			 CommonResourcesContainer.REPLY_LINE_PREFIX,
																			 false );
		//Process the input, convert the text into widgets and store them in messageBodyFlow, do not allow for embedded
		converter.process( true );
		
		//messageBodyTextArea.setText( messageBody );
		
		if( isRoomAccessMessage ) {
			roomNameLinkLabel.setText( message.getRoomName() );
		}
	}

	/**
	 * Loads the message body from the server
	 */
	private void loadMessageBody() {
		//Disable the controls
		setEnabledElements( true, false, false);

		//Ensure lazy loading
		(new SplitLoad( true ){
			@Override
			public void execute() {
				//Retrieve the full message data from the server 
				CommStatusAsyncCallback<PrivateMessageData> getMessageCallBack = new CommStatusAsyncCallback<PrivateMessageData>(progressBarUI) {
					public void onSuccessAct(PrivateMessageData result) {
						setMessageData( result );
						//Enable the controls
						setEnabledElements( true, true, true);
					}
					public void onFailureAct(final Throwable caught) {
						(new SplitLoad( true ) {
							@Override
							public void execute() {
								//Open the error dialog
								ErrorMessagesDialogUI.openErrorDialog(caught);
							}
						}).loadAndExecute();
						//Use the recovery method
						recover();
					}
				};
				//Do the server call
				MessageManagerAsync msgsManager = RPCAccessManager.getMessageManagerAsync();
				msgsManager.getMessage( SiteManager.getUserID(), SiteManager.getUserSessionId(),
										shortMessageData.getMsgID(), getMessageCallBack);
			}
			@Override
			public void recover() {
				//Enable the controls
				setEnabledElements( true, true, true);
			}
		}).loadAndExecute();
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
	protected String getRightButtonText(){
		if( isSimpleMessage || isRoomAccessGrantedMessage ) {
			return titlesI18N.replyButton();
		} else {
			if( isRoomAccessRequestMessage ) {
				return titlesI18N.addButtonTitle();
			} else {
				//This should not be happening
				return "???";
			}
		}
	}
		
	@Override
	protected void actionLeftButton() {
		hide();
	}

	@Override
	protected void actionRightButton() {
		if( isSimpleMessage || isRoomAccessGrantedMessage ) {
			//Ensure lazy loading
			final SplitLoad executor = new SplitLoad( true ) {
				@Override
				public void execute() {
					//Just open a new "send message" dialog
					final SendMessageDialogUI sendMsgDialog;
					if ( isSentByThisUser ) {
						sendMsgDialog = new SendMessageDialogUI( thisDialog, shortMessageData.getToUID(),
																 shortMessageData.getToUserName(),
																 (PrivateMessageData) shortMessageData );
					} else {
						sendMsgDialog = new SendMessageDialogUI( thisDialog, shortMessageData.getFromUID(),
																 shortMessageData.getFromUserName(),
																 (PrivateMessageData) shortMessageData );
					}
					sendMsgDialog.show();
					sendMsgDialog.center();
				}
			};
			executor.loadAndExecute();
		} else {
			final int accessForUID = shortMessageData.getFromUID();
			final String accessForLogin = shortMessageData.getFromUserName();
			final int accessToRoomID = ((PrivateMessageData) shortMessageData).getRoomID();
			final String accessToRoomName = ((PrivateMessageData) shortMessageData).getRoomName();
			if( isRoomAccessRequestMessage ) {
				if( isAdmin) {
					//Ensure lazy loading
					final SplitLoad executor = new SplitLoad( true ) {
						@Override
						public void execute() {
							RoomUserAccessDialogUI accessDialog = new RoomUserAccessDialogUI( true, null, thisDialog, accessForUID,
																								accessForLogin, accessToRoomID, accessToRoomName );
							accessDialog.show();
							accessDialog.center();
						}
					};
					executor.loadAndExecute();
				} else {
					//Get the list of user IDs
					final List<Integer> userIDS = new ArrayList<Integer>();
					userIDS.add( accessForUID );
					
					//Disable the controls
					setEnabledElements( false, false, false );

					//Ensure lazy loading
					(new SplitLoad( true ){
						@Override
						public void execute() {
							//Create a new user room access on the server
							CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>(progressBarUI) {
								public void onSuccessAct(Void result) {
									//Close this message viwing dialog
									hide();
									//Enable controls
									setEnabledElements( true, true, true );
								}
								public void onFailureAct(final Throwable caught) {
									(new SplitLoad( true ) {
										@Override
										public void execute() {
											//Show error dialog
											ErrorMessagesDialogUI.openErrorDialog(caught);
										}
									}).loadAndExecute();
									//Use the recovery method
									recover();
								}
							};
							//Do the RPC call to add users
							RoomManagerAsync roomManagerObject = RPCAccessManager.getRoomManagerAsync();
							roomManagerObject.createRoomAccess(SiteManager.getUserID(), SiteManager.getUserSessionId(),
																userIDS, accessToRoomID, callback);
						}
						@Override
						public void recover() {
							//Enable controls
							setEnabledElements( true, true, true );
						}
					}).loadAndExecute();
				}
			}
		}
	}
	
	@Override
	protected void populateDialog() {
		final int TITLE_GRID_NUM_ROWS_BASIC  	   = 6;
		final int TITLE_GRID_NUM_ROWS_ROOM_ACCESS  = TITLE_GRID_NUM_ROWS_BASIC + 1;
		
		//Add message title action grid
		addNewGrid( ( isRoomAccessMessage ? TITLE_GRID_NUM_ROWS_ROOM_ACCESS : TITLE_GRID_NUM_ROWS_BASIC ) , false, "", false);
		
		//01 Add the recepient field
		HorizontalPanel recepientPanel = new HorizontalPanel();
		Label recepientLabel = InterfaceUtils.getNewFieldLabel( (isSentByThisUser ? titlesI18N.recepientFieldTitle() : titlesI18N.senderFieldTitle() ) , true );
		recepientPanel.add( recepientLabel );
		recepientPanel.add( new HTML("&nbsp;") );
		Label userNameLink = new Label();
		isSenderRecepientUnknown = MessagesManagerDialogUI.initializeSenderRecepientProfileLink( shortMessageData, userNameLink,
																									false, isSentByThisUser,
																									titlesI18N, thisDialog);
		recepientPanel.add( userNameLink ); 
		addToGrid( getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, recepientPanel, false, false );
		
		//02 Add sending/receiving date time
		HorizontalPanel sentReceivedDateTimePanel = new HorizontalPanel();
		final Label sentReceivedField = InterfaceUtils.getNewFieldLabel( (isSentByThisUser ? titlesI18N.sentAtFieldTitle() : titlesI18N.receivedAtFieldTitle() ) , false );
		sentReceivedDateTimePanel.add( sentReceivedField );
		sentReceivedDateTimePanel.add( new HTML("&nbsp;") );
		final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat( PredefinedFormat.DATE_TIME_MEDIUM );
		final Label sentReceivedFieldValue = new Label( dateTimeFormat.format( shortMessageData.getSendReceiveDate() ) );
		sentReceivedFieldValue.setStyleName( CommonResourcesContainer.VIEW_MESSAGE_DIALOG_IMPORTANT_VALUE_STYLE_NAME );
		sentReceivedDateTimePanel.add( sentReceivedFieldValue );
		addToGrid( getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, sentReceivedDateTimePanel, true, false );
		
		if( isRoomAccessMessage ) {
			//03 If this is a room access request message add the link
			HorizontalPanel roomPanel = new HorizontalPanel();
			Label roomFieldLabel = InterfaceUtils.getNewFieldLabel( titlesI18N.roomNameFieldTitle() , true );
			roomPanel.add( roomFieldLabel );
			roomPanel.add( new HTML("&nbsp;") );
			roomNameLinkLabel.setStyleName( CommonResourcesContainer.ROOM_DIALOG_MANAGEMENT_LINK );
			roomNameLinkLabel.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent e) {
					if( isRoomClickEnabled ) {
						//If the message was sent by this user and it is a room access request or
						//if it was sent to us and it is the room access granted notification
						if( ( isSentByThisUser && isRoomAccessRequestMessage ) ||
							( !isSentByThisUser && isRoomAccessGrantedMessage ) ) {
							//Ensure lazy loading
							final SplitLoad executor = new SplitLoad( true ) {
								@Override
								public void execute() {
									//Open the room's data view dialog
									RoomInfoPopupPanel.openRoomViewPopup( ((PrivateMessageData) shortMessageData).getRoomID(),
																		  roomNameLinkLabel, roomsManager);
								}
							};
							executor.loadAndExecute();
						} else {
							//Ensure lazy loading
							( new SplitLoad( true ) {
								@Override
								public void execute() {
									//Open the room's data editing dialog
									RoomDialogUI.openRoomEditDialog( ((PrivateMessageData) shortMessageData).getRoomID(),
																	 thisDialog, SiteManager.getUserID(),
																	 SiteManager.getUserLoginName(), progressBarUI, roomsManager );
								}
							}).loadAndExecute();
						}
					}
				}
			});
			roomPanel.add( roomNameLinkLabel );
			addToGrid( getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, roomPanel, true, false );
		}
		
		//03/04 Add the message title Label
		Label messageSubjectLabel = InterfaceUtils.getNewFieldLabel( titlesI18N.messageSubjectFieldLongTitle(), true );
		addToGrid( getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, messageSubjectLabel, true, false );

		//04/05 Add the message title
		final SimplePanel messageTitleScrollPanel = new SimplePanel();
		messageTitleScrollPanel.setStyleName( CommonResourcesContainer.SCROLLABLE_SIMPLE_PANEL );
		messageTitleScrollPanel.addStyleName( CommonResourcesContainer.PRIVATE_MESSAGE_TITLE_SCROLL_PANEL_STYLE );
		final FlowPanel messageTitleFlowPanel = new FlowPanel();
		//NOTE: We do smiley to smiley code substitution here because it is fast for
		//the title and we want the original smileys to be preserved for showing in
		//the list of messages.
		final String messageTitle =  SmileyHandler.substituteSmilesWithSmileCodes( MessagesManagerDialogUI.getMessageTitle( shortMessageData, titlesI18N ) );
		//Populate the message body flow panel
		final MessageTextToFlowPanel converter = new MessageTextToFlowPanel( messageTitleFlowPanel, messageTitle, null, false );
		//Process the input, convert the text into widgets and store them in messageTitleFlow, do not allow for embedded
		converter.process( true );
		//Put the title data into the scroll panel
		messageTitleScrollPanel.add( messageTitleFlowPanel );
		addToGrid( getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, messageTitleScrollPanel, true, false );
		
		//01 Add the message body
		final DisclosurePanel disclosurePanel = new DisclosurePanel( titlesI18N.messageBodyFieldTitle() );
		disclosurePanel.setOpen( true );
		messageBodyScrollPanel.setStyleName( CommonResourcesContainer.SCROLLABLE_SIMPLE_PANEL );
		messageBodyScrollPanel.addStyleName( CommonResourcesContainer.PRIVATE_MESSAGE_BODY_SCROLL_PANEL_STYLE );
		final HorizontalPanel wrapperPanel = new HorizontalPanel();
		wrapperPanel.setWidth("100%");
		wrapperPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		wrapperPanel.setStyleName( CommonResourcesContainer.PRIVATE_MESSAGE_BODY_SCROLL_PANEL_CONTENT_STYLE );
		wrapperPanel.add(  messageBodyPanel );
		messageBodyScrollPanel.add( wrapperPanel );
		disclosurePanel.add( messageBodyScrollPanel );
		addToGrid( getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, disclosurePanel, true, false );
		
		//07/08 Add action buttons and the progress grid
		addGridActionElements(true, false, true, true);
	}

}
