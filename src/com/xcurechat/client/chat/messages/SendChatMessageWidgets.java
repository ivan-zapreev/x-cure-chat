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
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.chat.messages;

import java.util.Iterator;
import java.util.Map.Entry;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;

import com.xcurechat.client.chat.FontSelectorPanelUI;
import com.xcurechat.client.chat.RoomsManagerUI;

import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.MessageFontData;
import com.xcurechat.client.data.ShortFileDescriptor;


import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;

import com.xcurechat.client.rpc.ServerSideAccessManager;

import com.xcurechat.client.utils.widgets.ActionLinkPanel;
import com.xcurechat.client.utils.widgets.Button;
import com.xcurechat.client.utils.widgets.ServerCommStatusPanel;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.TextBaseTranslitAndProgressBar;

/**
 * @author zapreevis
 * This class simply store the send chat message dialog/panel common elements and method for working with them
 */
public class SendChatMessageWidgets implements SendChatMessageManager.SendChatMsgUI {
	//The width in pixels, do not set via css because it is used for computations
	private static final int MAXIMUM_MESAGE_TEXT_BASE_ELEMENT_WIDTH = 560;
	
	//The localization for the text
	protected static final UITitlesI18N titlesI18N = I18NManager.getTitles();
	
	//The body of the message
	TextBoxBase messageBodyTextBase;
	//The wrapper composite with the progress bar 
	TextBaseTranslitAndProgressBar lengthCounterComposite;
	
	//The font selection panel
	final FontSelectorPanelUI theFontSelectionPanel = new FontSelectorPanelUI();
	
	//The check box that is used to indicate whether the user wants to use
	//message flows, i.e. inherit font settings when replying to a chat message
	final CheckBox useMessageFlowsCheckBox = new CheckBox();
	
	//The check box that is used to indicate whether the user wants to see
	//the visual alerts for the messages that are addressed to him or not
	final CheckBox useMessageAlertsCheckBox = new CheckBox();
	
	//The check box that indicates whether this message is private or not
	final CheckBox isPrivateCheckBox = new CheckBox();
	
	//The clear button
	final Button clearButton = new Button();
	
	//The simple panel that stores the progress bar UI
	final SimplePanel progressBarPanel = new SimplePanel();
	
	//Stores the message's file ID
	int messageFileID = ShortFileDescriptor.UNKNOWN_FILE_ID;
	//The descriptor of the chat-message files attached to the message
	private ShortFileDescriptor messageFileDesc = null;
	
	//The action attached image file link
	ActionLinkPanel smileyDialogLink;
	
	//The action attached image file link
	ActionLinkPanel actionImageFileLink;
	
	//The panel that stores and works with message recepients
	final MessageRecepientsPanelUI messageRecepientsPanel;
	
	//The current chat room id
	int roomID;
	
	//The click handler for the smiley dialo action link
	final ClickHandler smileyActionLinkClickHandler;
	
	//The widget that encloses this objects or null
	final SendChatMessageManager.SendChatMsgUI parentWidget;
	
	//True if we are create the object for the send-chat-message Panel UI, otherwise for the dialog
	private final boolean isPanelMode;
	
	//The instance of the rooms Manager
	private final RoomsManagerUI roomsManager;
	 
	public SendChatMessageWidgets( final int roomID, final ClickHandler smileyActionLinkClickHandler,
									final SendChatMessageManager.SendChatMsgUI parentWidget,
									final boolean isPanelMode, final RoomsManagerUI roomsManager ) {
		//Store the rooms manager
		this.roomsManager = roomsManager;
		//Store the room ID
		this.roomID = roomID;
		//Store the click handler
		this.smileyActionLinkClickHandler = smileyActionLinkClickHandler;
		//Initialize the message recipients panel
		this.messageRecepientsPanel = new MessageRecepientsPanelUI( ( parentWidget instanceof DialogBox) ? (DialogBox) parentWidget : null,
																	  SendChatMessageWidgets.MAXIMUM_MESAGE_TEXT_BASE_ELEMENT_WIDTH,
																	  this.roomID, roomsManager );
		//Store the mode
		this.isPanelMode = isPanelMode;
		
		//Store the wrapping dialog link
		this.parentWidget = parentWidget;
	}
	
	public void initialize() {
		//Initialize the text box or the text area and its wrapper
		if( isPanelMode ) {
			messageBodyTextBase = new TextBox();
			messageBodyTextBase.setStyleName( CommonResourcesContainer.MESSAGE_BODY_TEXT_BASE_PANEL_STYLE );
		} else {
			messageBodyTextBase = new TextArea();
			messageBodyTextBase.setStyleName( CommonResourcesContainer.MESSAGE_BODY_TEXT_BASE_DIALOG_STYLE );
		}
		//Set the width for the text box area to be the same as the width of the recipients panel
		messageBodyTextBase.setWidth(  SendChatMessageWidgets.MAXIMUM_MESAGE_TEXT_BASE_ELEMENT_WIDTH + "px" );
		lengthCounterComposite = new TextBaseTranslitAndProgressBar( messageBodyTextBase, ChatMessage.MAX_MESSAGE_LENGTH );
		
		smileyDialogLink = new ActionLinkPanel( ServerSideAccessManager.getChatMessageSmilesLinkImageURL( ),
				   titlesI18N.smilesLinkToolTip(),
				   ServerSideAccessManager.getChatMessageSmilesLinkImageURL( ),
				   titlesI18N.smilesLinkToolTip(), titlesI18N.smilesLinkTitle(), 
				   smileyActionLinkClickHandler, true, true );
		
		actionImageFileLink = new ActionLinkPanel( ServerSideAccessManager.getChatMessageFileUnsetURL( ),
				   titlesI18N.chatMessageAddFileToolTip(),
				   ServerSideAccessManager.getChatMessageFileUnsetURL( ),
				   "", titlesI18N.chatMessageAddFileTitle(),
				   new ClickHandler(){
						@Override
						public void onClick(ClickEvent event) {
							SendChatMessageManager.getInstance().openFileUploadDialog( getCurrentRoomID(), getCurrentlyAttachedFileDesc() );
						}
					}, true, true );

		isPrivateCheckBox.setText( titlesI18N.privateMessageCheckBoxTitle() );
		isPrivateCheckBox.setTitle( titlesI18N.privateMessageCheckBoxToolTip() );
		isPrivateCheckBox.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
		isPrivateCheckBox.addClickHandler( new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				//Update the list of recipients for the case of no recipients set
				updateMessageRecipientsPanel( );
			}
		});
		
		//Initialize the message flows check box
		if( isPanelMode ) {
			useMessageFlowsCheckBox.setText( titlesI18N.messageFlowsTitleShort() );
		} else {
			useMessageFlowsCheckBox.setText( titlesI18N.messageFlowsTitle() );
		}
		useMessageFlowsCheckBox.setTitle( titlesI18N.messageFlowsToolTip() );
		useMessageFlowsCheckBox.setValue( true );
		useMessageFlowsCheckBox.addClickHandler( new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				//Store the value inside the manager
				SendChatMessageManager.getInstance().setMessageFlowsOn( useMessageFlowsCheckBox.getValue() );
			}
		});
		useMessageFlowsCheckBox.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
		
		//Initialize the message alerts check box
		if( isPanelMode ) {
			useMessageAlertsCheckBox.setText( titlesI18N.messageAlertTitleShort() );
		} else {
			useMessageAlertsCheckBox.setText( titlesI18N.messageAlertTitle() );
		}
		useMessageAlertsCheckBox.setTitle( titlesI18N.messageAlertToolTip() );
		useMessageAlertsCheckBox.setValue( false );
		useMessageAlertsCheckBox.addClickHandler( new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				roomsManager.setAvatarAlerts( useMessageAlertsCheckBox.getValue() );
				//Store the value inside the manager
				SendChatMessageManager.getInstance().setMessageAlertsOn( useMessageAlertsCheckBox.getValue() );
			}
		});
		useMessageAlertsCheckBox.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );

		clearButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		clearButton.setText( titlesI18N.resetButtonTitle() );
		clearButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				cleanUIData( getCurrentRoomID(), false, true, false );
			}
		});
	}
	
	@Override
	public int getCurrentRoomID() {
		return roomID;
	}
	
	/**
	 * @return the file descriptor of the file attached to this chat message or null if the file is not attached
	 */
	public ShortFileDescriptor getCurrentlyAttachedFileDesc() {
		return messageFileDesc;
	}
	
	/**
	 * Allows to set/remove the chat-message file id. This call also updates the dialog's file indicator
	 * @param fileDesc the descriptor for the file to set, or null to delete the file
	 */
	@Override
	public void setChatMessageAttFileDesc(final int roomID, final ShortFileDescriptor fileDesc) {
		messageFileDesc = fileDesc;
		if( fileDesc == null ) {
			//The image was deleted
			actionImageFileLink.setActionImageUrlEnbl( ServerSideAccessManager.getChatMessageFileUnsetURL( ) );
		} else {
			//The new image was added
			actionImageFileLink.setActionImageUrlEnbl( ServerSideAccessManager.getChatMessageFileSetURL( ) );
		}
	}
	
	/**
	 * Allows to initialize the dialog with the chat message data
	 * @param message the stored message data
	 * @param keepRecipients if true then the message's recipients are kept intact
	 * @param keepFontSettings if true then the font settings are preserved
	 * @param keepMessageType if true then we preserve the message type
	 */
	@Override
	public void setMessageData( final ChatMessage message, final boolean keepRecipients,
			 					final boolean keepFontSettings, final boolean keepMessageType ) {
		//Set message body
		messageBodyTextBase.setText( message.messageBody );
		//Update the composite
		lengthCounterComposite.setFocusAndUpdate();
		//Set message type
		if( ! keepMessageType ) {
			this.isPrivateCheckBox.setValue( message.messageType == ChatMessage.Types.PRIVATE_MESSAGE_TYPE );
		}
		//Update the list of recipients
		if( ! keepRecipients ) {
			messageRecepientsPanel.removeMessageRecipients();
			Iterator< Entry<Integer,String> > entrySetIter = message.recepientIDToLoginName.entrySet().iterator();
			while( entrySetIter.hasNext() ) {
				Entry<Integer, String> entry = entrySetIter.next();
				messageRecepientsPanel.addMessageRecipient( entry.getKey(), entry.getValue() );
			}
		}
		//Update the fonts
		if( ! keepFontSettings ) {
			theFontSelectionPanel.setSelectedFontType( message.fontType );
			theFontSelectionPanel.setSelectedFontSize( message.fontSize );
			theFontSelectionPanel.setSelectedFontColor( message.fontColor );
		}
		//Update the room ID
		roomID = message.roomID;
		
		//Update the chat message image
		setChatMessageAttFileDesc( roomID, message.fileDesc );
		
		//Set the current room id into the recipients panel
		messageRecepientsPanel.setCurrentRoomID( roomID );
		
		//Hide the "add recipients" list box
		messageRecepientsPanel.hideRecipientsListBox();
		
		//Update the list of recipients for the case of no recipients set
		updateMessageRecipientsPanel( );
	}

	@Override
	public ChatMessage getMessageData() {
		ChatMessage message = new ChatMessage();
		message.fileDesc = messageFileDesc;
		message.messageBody = messageBodyTextBase.getText();
		message.messageType = ( this.isPrivateCheckBox.getValue() ? ChatMessage.Types.PRIVATE_MESSAGE_TYPE : ChatMessage.Types.SIMPLE_MESSAGE_TYPE );
		message.recepientIDToLoginName.putAll( messageRecepientsPanel.getCurrentRecipients() );
		message.fontType = this.theFontSelectionPanel.getSelectedFontType();
		message.fontSize = this.theFontSelectionPanel.getSelectedFontSize();
		message.fontColor = this.theFontSelectionPanel.getSelectedFontColor();
		message.roomID = getCurrentRoomID();
		return message;
	}
	
	//Is used for Opera that for some reason resets the fonts after the message is sent
	private int fontType = MessageFontData.DEFAULT_FONT_FAMILY;
	private int fontSize = MessageFontData.DEFAULT_FONT_SIZE;
	private int fontColor = MessageFontData.DEFAULT_FONT_COLOR;
	
	@Override
	public void rememberChatMessageFonts() {
		fontType = this.theFontSelectionPanel.getSelectedFontType();
		fontSize = this.theFontSelectionPanel.getSelectedFontSize();
		fontColor = this.theFontSelectionPanel.getSelectedFontColor();
	}
	
	@Override
	public void restoreChatMessageFonts() {
		theFontSelectionPanel.setSelectedFontType( fontType );
		theFontSelectionPanel.setSelectedFontSize( fontSize );
		theFontSelectionPanel.setSelectedFontColor( fontColor );
	}
	
	@Override
	public void cleanUIData(int localRoomID, boolean keepRecipients, boolean keepFontSettings, boolean keepMessageType) {
		//Update the dialog data
		ChatMessage message = new ChatMessage();
		message.roomID = localRoomID;
		message.messageType = ChatMessage.Types.SIMPLE_MESSAGE_TYPE;
		setMessageData( message, keepRecipients, keepFontSettings, keepMessageType );
	}
	
	public void setFocus() {
		//Set focus and update the progress bar, just in case
		lengthCounterComposite.setFocus(true);
	}
	
	@Override
	public void setMessageFlowsOn(boolean isMessageFlowOn) {
		this.useMessageFlowsCheckBox.setValue( isMessageFlowOn, false );
	}
	
	@Override
	public void setMessageAlertsOn(boolean isMessageAlertsOn) {
		this.useMessageAlertsCheckBox.setValue( isMessageAlertsOn, false );
	}
	
	@Override
	public void addSmileStringToMessage(String smileyInternalCodeString) {
		TextBaseTranslitAndProgressBar.insertStringIntoTextBoxWrapper(smileyInternalCodeString, lengthCounterComposite);
	}

	@Override
	public void setProgressBar(final ServerCommStatusPanel progressBarUI) {
		progressBarPanel.clear();				//Clean the panel, just in case
		progressBarPanel.add( progressBarUI );	//Set the progress bar
	}

	public void removeMessageRecipient(int recepientID) {
		messageRecepientsPanel.removeMessageRecipient( recepientID );
	}

	public void updateMessageRecipientsPanel() {
		messageRecepientsPanel.updateMessageRecipientsPanel();
	}
	
	/**
	 * This method allows to set the proper width of the recipients' scroll panel.
	 * Should be called only when the send chat message dialog is displayed (visible).
	 */
	public void addjustRecipientsScrollPanel() {
		messageRecepientsPanel.addjustRecipientsScrollPanel();
	}

	@Override
	public void onFirstShowInitialize() {
		//Set the proper size the the recipients scroll panel
		addjustRecipientsScrollPanel( );
	}

	@Override
	public void setElementsEnabled(boolean enabled) {
		//Set the focus, just in case it has been lost
		setFocus();
	}

	@Override
	public void setKeepUIOpen(boolean isKeepUIOpen) {
		//Does nothing, THIS METHOD IS NOT INVOKED
	}

	@Override
	public void showToUser(boolean show) {
		//Does nothing, THIS METHOD IS NOT INVOKED
	}

	@Override
	public void updateUIElements() {
		//Just set the focus, because it could habe been lost
		setFocus();
	}
}
