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

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ChangeEvent;

import com.google.gwt.user.client.rpc.AsyncCallback;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.HorizontalPanel;

import com.xcurechat.client.MainSiteMenuUI;
import com.xcurechat.client.SiteManager;

import com.xcurechat.client.chat.RoomsManagerUI;
import com.xcurechat.client.data.ShortPrivateMessageData;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.search.OnePageViewData;
import com.xcurechat.client.dialogs.PagedActionGridDialog;
import com.xcurechat.client.dialogs.profile.ViewUserProfileDialogUI;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.rpc.MessageManagerAsync;
import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.Button;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * The persona-messages management dialog
 */
public class MessagesManagerDialogUI extends PagedActionGridDialog<ShortPrivateMessageData> {
	//The number of columns in the dataTable
	private static final int NUMBER_OF_COLUMNS_IN_MESSAGES_TABLE = 6;
	//The number of rows in the dataTable
	private static final int NUMBER_OF_ROWS_PER_PAGE = 10;
	//Maximum displayed title length
	private static final int MAX_DISPLAYED_TITLE_LENGTH = 25;
	
	//Delete and Write buttons for deleting and creating new rooms
	private final Button writeButton = new Button();
	private final Button deleteButton = new Button();
	//This select box will be used for worting messages
	private final ListBox sortMsgsListBox = new ListBox();
	private final int RECEIVED_MESSAGES = 0;
	private final int SENT_MESSAGES = 1;
	private final int ALL_MESSAGES = 2;
	//This reflects the type of messages we are browsing as set in sortMsgsListBox
	private int messageSelectionType = RECEIVED_MESSAGES;
	//This is the column title which will change depending on the type of messages that we browse
	private Label fromToUserNameLabel = new Label(); 
	
    //The id and login of the user we browse data for
    private final String forUserLoginName;
    private final int forUserID;
    
    //The instance of the rooms manager
    private final RoomsManagerUI roomsManager;
	
    /**
     * A simple sialog for viewing the rooms of a user
	 * @param forUserID the user ID we browse statistics for
	 * @param forUserLoginName the login name of the user we brows data for
	 * @param roomsManager the instance of the rooms manager
     */
	public MessagesManagerDialogUI( final int forUserID, final String forUserLoginName,
									final DialogBox parentDialog, final RoomsManagerUI roomsManager ) {
		super( false, true, true, NUMBER_OF_ROWS_PER_PAGE, NUMBER_OF_COLUMNS_IN_MESSAGES_TABLE, forUserID, parentDialog );
		
		//Store the data
		this.forUserID = forUserID;
		this.forUserLoginName = forUserLoginName;
		this.roomsManager = roomsManager;
		
		//Set the dialog's caption.
		updateDialogTitle();
		
		this.setStyleName( CommonResourcesContainer.USER_DIALOG_STYLE_NAME );
		
		//Enable the action buttons and hot key, even though we will not be adding these buttons
		//to the grid, we will use the hot keys associated with them to close this dialog
		setLeftEnabled( true );
		setRightEnabled( true );
		
		//Disable the actual dialog buttons for now
		disableAllControls();
		
		//Fill dialog with data
		populateDialog();	    
	}
	
	/**
	 * Adds the dialog elements, such as the rooms table and the buttons.
	 */
	private void addDialogElements() {
		//The user for which we browse messages is only important it is some one else
		if( forUserID != SiteManager.getUserID() ) {
			//First add the grid with the table
			addNewGrid( 1, 1, false, "", false);
			
			//Add the user login name we browse messages for
			HorizontalPanel userNamePanel = new HorizontalPanel();
			Label loginNameFieldTitle = InterfaceUtils.getNewFieldLabel( titlesI18N.userFieldName(), true );
			userNamePanel.add( loginNameFieldTitle );
			userNamePanel.add( new HTML("&nbsp;") );
			Label userLoginNameLabel = new Label();
			userLoginNameLabel.setText( forUserLoginName );
			userLoginNameLabel.setStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_IMP_STYLE_NAME );
			userNamePanel.add( userLoginNameLabel );
			addToGrid( FIRST_COLUMN_INDEX, userNamePanel, false, false );
		}
		
		//First add the grid with the table
		addNewGrid( 1, 3, false, "", false);
		
		//Add the selector for messages sorting
		HorizontalPanel selectMessageTypePanel = new HorizontalPanel();
		Label messageTypeSelectorLabel = InterfaceUtils.getNewFieldLabel( titlesI18N.viewMessagesTypeField(), false );
		selectMessageTypePanel.add( messageTypeSelectorLabel );
		selectMessageTypePanel.add( new HTML("&nbsp;") );
		sortMsgsListBox.addItem(titlesI18N.receivedMessagesListBoxItem(), "" + RECEIVED_MESSAGES);
		sortMsgsListBox.addItem(titlesI18N.sentMessagesListBoxItem(), "" + SENT_MESSAGES);
		sortMsgsListBox.addItem(titlesI18N.allMessagesListBoxItem(), "" + ALL_MESSAGES);
		sortMsgsListBox.setSelectedIndex(0);
		sortMsgsListBox.addChangeHandler( new ChangeHandler(){
			public void onChange( ChangeEvent e ) {
				int newMessageSelectionType = Integer.parseInt( sortMsgsListBox.getValue( sortMsgsListBox.getSelectedIndex() ) ); 
				if( messageSelectionType != newMessageSelectionType ) {
					//Retrieve messages from the server, based on the message type
					messageSelectionType = newMessageSelectionType;
					//Update the from/to column title
					switch( newMessageSelectionType ){
						case RECEIVED_MESSAGES:
							fromToUserNameLabel.setText( titlesI18N.fromUserColumnTitle() );
							break;
						case SENT_MESSAGES:
							fromToUserNameLabel.setText( titlesI18N.toUserColumnTitle() );
							break;
						case ALL_MESSAGES:
							fromToUserNameLabel.setText( titlesI18N.fromToUserColumnTitle() );
							break;
					}
					//Update the whole dialog, we can not update the current page because
					//then we do not update the number of pages!!! 
					updateActualData();
				}
			}
		});
		selectMessageTypePanel.add( sortMsgsListBox );
		this.addToGrid(FIRST_COLUMN_INDEX, selectMessageTypePanel, false, false);
		
		//Add "Delete" button for the rooms
		deleteButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		deleteButton.setText( titlesI18N.deleteButton() );
		deleteButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				//If there are message IDs selected then delete those messages 
				if( isSelectedData() ) {
					//Disable the dialog controls
					disableAllControls();
					//Ensure lazy loading
					(new SplitLoad( true ){
						@Override
						public void execute() {
							//Construct the call back object
							CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>(progressBarUI) {
								public void onSuccessAct(Void result) {
									//Update the parent dialog table;
									updateActualData();
									//Speed up the updates of the messages notification in the main menu
									if( messageSelectionType == RECEIVED_MESSAGES ) {
										MainSiteMenuUI.getMainSiteMenuUI().speedUpMessageUpdates();
									}
									//We do not enable check boxes here because we update the
									//entire page and the check boxes are re initialized 
								}
								public void onFailureAct( final Throwable caught ) {
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
							//Call the deletion procedure
							MessageManagerAsync messageManagerObject = RPCAccessManager.getMessageManagerAsync();
							messageManagerObject.delete(SiteManager.getUserID(), SiteManager.getUserSessionId(),
														forUserID, getSelectedDataIDs(), callback);
						}
						@Override
						public void recover() {
							enableAllControls();
						}
					}).loadAndExecute();
				} else {
					//Report the error
					(new SplitLoad( true ) {
						@Override
						public void execute() {
							//Report the error
							ErrorMessagesDialogUI.openErrorDialog( I18NManager.getErrors().thereIsNoSelectedMessages() );
						}
					}).loadAndExecute();
				}
			}
		} );
		this.addToGrid(SECOND_COLUMN_INDEX, deleteButton, false, false);
		
		//Add "Create" button for the rooms
		writeButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		writeButton.setText( titlesI18N.writeButton() );
		writeButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				//Ensure lazy loading
				final SplitLoad executor = new SplitLoad( true ) {
					@Override
					public void execute() {
						//Open a new send message dialog
						SendMessageDialogUI sendDialog = new SendMessageDialogUI( thisDialog );
						sendDialog.show();
						sendDialog.center();
					}
				};
				executor.loadAndExecute();
			}
		} );
		this.addToGrid(THIRD_COLUMN_INDEX, writeButton, false, true);

		//First add the grid
		addNewGrid( 1, 1, false, "", false);
		
		//Fill the table headings
		int column = 0;
		dataTable.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_STYLE_NAME );
		Label msgNumberLabel = new Label(titlesI18N.indexColumnTitle());
		msgNumberLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, column++, msgNumberLabel );

		//Add the select data entries column to the data table
		addSelectorColumnTitleToDataTable(0, column++);

		Label msgDateTimeLabel = new Label(titlesI18N.dateTimeColumnTitle());
		msgDateTimeLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, column++, msgDateTimeLabel );

		Label msgFromToLabel = new Label( titlesI18N.fromToColumnTitle() );
		msgFromToLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, column++, msgFromToLabel );

		fromToUserNameLabel.setText( titlesI18N.fromUserColumnTitle() );
		fromToUserNameLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, column++, fromToUserNameLabel );
		
		Label msgTitleLabel = new Label(titlesI18N.messageTitleColumnTitle());
		msgTitleLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, column++, msgTitleLabel );
		
		dataTable.setSize("100%", "100%");
		addToGrid(FIRST_COLUMN_INDEX, dataTable, false, false);
		
		//Add the navigation buttons with the progress bar
		addDefaultControlPanel();
	}
	
	@Override
	protected void actionLeftButton() {
		hide();
	}

	@Override
	protected void actionRightButton() {
		hide();
	}

	@Override
	protected void populateDialog() {
		addDialogElements();
		updateActualData();
	}

	//We have to suppress warnings about custing to a generic type
	@SuppressWarnings("unchecked")
	protected void addRowToTable(final int row, final int index, final Object result) {
		//Convert the object data
		OnePageViewData<ShortPrivateMessageData> data = (OnePageViewData<ShortPrivateMessageData>) result;
		final ShortPrivateMessageData messageData = data.entries.get( index );
		final boolean isSentByThisUser = (messageData.getFromUID() == forUserID);
		
		//Column counter
		int column = 0;
		
		//01 Add the message index 
		int messageNumber = ( ( getCurrentPageNumber() - 1 ) * NUMBER_OF_ROWS_PER_PAGE) + ( index + 1);
		dataTable.setWidget( row, column++, new Label( Integer.toString( messageNumber ) ) );
		
		//02 Add the check box into the messages column, it should 
		//mark the data entry (message) with the given ID. 
		addSelectorIntoDataTableRow(row, column++, index, new Integer(messageData.getMsgID()), "");
		
		//03 Add the date/time when the message was received/sent
		final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat( PredefinedFormat.DATE_TIME_SHORT );
		Label dateTime =  new Label( dateTimeFormat.format( messageData.getSendReceiveDate() ) );
		dataTable.setWidget( row, column++, dateTime );
		
		//04 Add the message status: Sent or received message
		final String statusImageTitle;
		final String statusImageURL;
		if( isSentByThisUser ) {
			if( messageData.isRead() ) {
				statusImageURL = ServerSideAccessManager.getSentReadMessageImageURL();
				statusImageTitle = titlesI18N.sentReadMessageImageTip();
			} else {
				statusImageURL = ServerSideAccessManager.getSentUnreadMessageImageURL();
				statusImageTitle = titlesI18N.sentUnreadMessageImageTip();
			}
		} else {
			if( messageData.isRead() ) {
				statusImageURL = ServerSideAccessManager.getReceivedReadMessageImageURL( );
				statusImageTitle = titlesI18N.receivedReadMessageImageTip();
			} else {
				statusImageURL = ServerSideAccessManager.getReceivedUnreadMessageImageURL();
				statusImageTitle = titlesI18N.receivedUnreadMessageImageTip();
			}
		}
		Image statusImage = new Image( statusImageURL );
		statusImage.setTitle( statusImageTitle );
		dataTable.setWidget( row, column++, statusImage );
		
		//05 Add the link to the pofile of the user we received the message from or sent it to
		Label userNameLink = new Label( );
		initializeSenderRecepientProfileLink( messageData, userNameLink, true, isSentByThisUser, titlesI18N, thisDialog );
		dataTable.setWidget( row, column++, userNameLink );
		
		//06 Add title of the message that links to the dialog
		String title = getMessageTitle( messageData, titlesI18N );
		String shortTitle = (title.length() > MAX_DISPLAYED_TITLE_LENGTH ? title.substring(0, MAX_DISPLAYED_TITLE_LENGTH )+"...": title); 
		Label msgTitleLink = new Label( shortTitle );
		//Get the true unread status, considering who sent the message
		msgTitleLink.setTitle( titlesI18N.fullMessageTitleTip(title) );
		//Set the title link style based on whether this message it to the user and is unread
		if( ( ! isSentByThisUser ) && ( ! messageData.isRead() ) ) {
			msgTitleLink.setStyleName( CommonResourcesContainer.MESSAGE_TITLE_UNREAD_LINK_STYLE );
		} else {
			msgTitleLink.setStyleName( CommonResourcesContainer.MESSAGE_TITLE_READ_LINK_STYLE );
		}
		msgTitleLink.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent e){
				//Speed up the updates of the messages notification in the main menu
				if( (messageSelectionType == RECEIVED_MESSAGES) && !messageData.isRead() ) {
					MainSiteMenuUI.getMainSiteMenuUI().speedUpMessageUpdates();
				}
				//Ensure lazy loading
				final SplitLoad executor = new SplitLoad( true ) {
					@Override
					public void execute() {
						//Open the message viewing gialog
						ViewMessageDialogUI viewMsgDialog = new ViewMessageDialogUI( true, true, thisDialog, messageData, roomsManager );
						viewMsgDialog.show();
						viewMsgDialog.center();
					}
				};
				executor.loadAndExecute();
			}
		});
		dataTable.setWidget( row, column++, msgTitleLink );
	}
	
	/**
	 * This method allows to return a proper message title for the given message
	 * @param messageData the short message data with the title returned from the server or null
	 * @param titlesI18N the internationalization object
	 * @return the proper message title or an empty string if the messageData is null
	 */
	public static String getMessageTitle( final ShortPrivateMessageData messageData, final UITitlesI18N titlesI18N ) {
		String title = null;
		if( messageData != null ) {
			title = messageData.getMessageTitle();
			if( ( title != null ) && !title.trim().isEmpty() ) {
				if( messageData.getMessageType() == ShortPrivateMessageData.FORUM_REPLY_NOTIFICATION_MESSAGE_TYPE ) {
					title = titlesI18N.replyMessageTitlePrefix() + " " + title;
				}
			} else {
				//We need a title to have a link on it
				switch( messageData.getMessageType() ){
					case ShortPrivateMessageData.ROOM_ACCESS_REQUEST_MESSAGE_TYPE:
						title = titlesI18N.roomAccessRequestMessageTitle(messageData.getRoomName()); break;
					case ShortPrivateMessageData.ROOM_ACCESS_GRANTED_MESSAGE_TYPE:
						title = titlesI18N.roomAccessGrantedMessageTitle(messageData.getRoomName()); break;
					default:
						title = titlesI18N.undefinedMessageTitleText();
				}
			}
		}
		return ( title == null ? "" : title );
	}
	
	/**
	 * Allows to initialize the user profile link for message viewing
	 * @param messageData the short messge data object
	 * @param userNameLink the label that will be the user profile link
	 * @param isShortUserLogin if true then the user login name is shortened
	 * @param isSentByThisUser true if the message is sent by the user who is logged on
	 * @param titlesI18N the internationalization object
	 * @param thisDialog the dialog we initialize the link for
	 * @return true if the recipient/sender is unknown, i.e. the profile was deleted
	 */
	public static boolean initializeSenderRecepientProfileLink( final ShortPrivateMessageData messageData, final Label userNameLink,
																final boolean isShortUserLogin, final boolean isSentByThisUser,
																final UITitlesI18N titlesI18N, final DialogBox thisDialog ) {
		boolean isSenderRecepientUnknown = false;
		final int viewUserID;
		final String viewUserLoginName;
		final String loginNameTip;
		if( isSentByThisUser ) {
			viewUserLoginName = messageData.getToUserName();
			viewUserID = messageData.getToUID();
			loginNameTip = titlesI18N.messageRecepientProfileTip( viewUserLoginName );
		} else {
			viewUserLoginName = messageData.getFromUserName();
			viewUserID = messageData.getFromUID();
			loginNameTip = titlesI18N.messageSenderProfileTip( viewUserLoginName );
		}
		if( viewUserLoginName.equals( ShortUserData.DELETED_USER_LOGIN_NAME ) ) {
			//In case the user was deleted, we do not make it a link
			userNameLink.setText( titlesI18N.unknownMsgSenderReceiver() );
			userNameLink.setStyleName( CommonResourcesContainer.DELETED_MESSAGE_SENDER_RECEPIENT_LINK_STYLE );
			if( isSentByThisUser ) {
				userNameLink.setTitle( titlesI18N.recepientHasDeletedHisProfile() );
			} else {
				userNameLink.setTitle( titlesI18N.senderHasDeletedHisProfile() );
			}
			isSenderRecepientUnknown = true;
		} else {
			userNameLink.setText( ( isShortUserLogin? ShortUserData.getShortLoginName( viewUserLoginName ) : viewUserLoginName ) );
			userNameLink.setTitle( loginNameTip );
			userNameLink.setStyleName( CommonResourcesContainer.DIALOG_LINK_BLUE_STYLE );
			userNameLink.addClickHandler( new ClickHandler() {
				public void onClick(ClickEvent e) {
					//Ensure lazy loading
					final SplitLoad executor = new SplitLoad( true ) {
						@Override
						public void execute() {
							ViewUserProfileDialogUI userViewDialog = new ViewUserProfileDialogUI( viewUserID, viewUserLoginName,
																									thisDialog, false );
							userViewDialog.show();
							userViewDialog.center();
						}
					};
					executor.loadAndExecute();
				}
			});
		}
		return isSenderRecepientUnknown;
	}
	
	@Override
	protected void enableControls( final boolean enable ) {
		Scheduler.get().scheduleDeferred( new ScheduledCommand(){
			public void execute() {
				deleteButton.setEnabled( enable );
				writeButton.setEnabled( enable );
			}
		});
	}

	@Override
	protected void updateDialogTitle( final int numberOfEntries, final int numberOfPages, final int currentPageNumber ) {
		this.setText( titlesI18N.messagesManagerDialogTitle( numberOfEntries, currentPageNumber, numberOfPages ) );
 	}
	
	@Override
	protected Integer getDataEntryID(OnePageViewData<ShortPrivateMessageData> onePageData, final int index) {
		return onePageData.entries.get( index ).getMsgID();
	}

	@Override
	protected String getDataEntryName(OnePageViewData<ShortPrivateMessageData> onePageData, final int index) {
		//The room name is not used, so this is an empty implementation
		return "";
	}
	
	@Override
	protected void browse( final int userID, final String userSessionID, final int forUserID,
							final int offset, final int number_or_rows_per_page,
							final AsyncCallback<OnePageViewData<ShortPrivateMessageData>> callback ) throws SiteException {
		final boolean isAll = ( messageSelectionType == ALL_MESSAGES );
		final boolean isReceived = ( messageSelectionType == RECEIVED_MESSAGES );
		//Ensure lazy loading
		(new SplitLoad(){
			@Override
			public void execute() {
				MessageManagerAsync messageManager = RPCAccessManager.getMessageManagerAsync();
				messageManager.browse( userID, userSessionID, forUserID, isAll,
									   isReceived, offset, NUMBER_OF_ROWS_PER_PAGE, callback );
			}
			@Override
			public void recover() {
				callback.onFailure( new InternalSiteException( I18NManager.getErrors().serverDataLoadingFalied() ) );
			}
		}).loadAndExecute();
	}

	@Override
	protected void count( final int userID, final String userSessionID, final int forUserID,
						  final AsyncCallback<Integer> callback ) throws SiteException {
		final boolean isAll = ( messageSelectionType == ALL_MESSAGES );
		final boolean isReceived = ( messageSelectionType == RECEIVED_MESSAGES );
		//Ensure lazy loading
		(new SplitLoad(){
			@Override
			public void execute() {
				MessageManagerAsync messageManager = RPCAccessManager.getMessageManagerAsync();
				messageManager.count(userID, userSessionID, forUserID, isAll, isReceived, callback);
			}
			@Override
			public void recover() {
				callback.onFailure( new InternalSiteException( I18NManager.getErrors().serverDataLoadingFalied() ) );
			}
		}).loadAndExecute();
	}
	
	@Override
	protected void beforeTableDataUpdate(){
		//Do nothing
	}

}
