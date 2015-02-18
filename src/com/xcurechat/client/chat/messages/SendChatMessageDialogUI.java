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
package com.xcurechat.client.chat.messages;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import com.xcurechat.client.chat.RoomsManagerUI;
import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.ShortFileDescriptor;

import com.xcurechat.client.dialogs.ActionGridDialog;

import com.xcurechat.client.utils.widgets.ServerCommStatusPanel;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.TextBaseTranslitAndProgressBar;

/**
 * @author zapreevis
 * This is the send chat message dialog
 */
public class SendChatMessageDialogUI extends ActionGridDialog implements SendChatMessageManager.SendChatMsgUI,
																		 MessageRecipientUI.MessageRecipientPanel {
	
	//Stores the disclosure panel with the main dialog elements
	private DisclosurePanel disclosirePanel;
	
	//The check box that is used to indicate whether we need
	//to keep the send message dialog open at all times
	private final CheckBox keepDialogOpenCheckBox = new CheckBox();
	
	//Stores the chat-message panel main widgets and method for working and initialization
	private final SendChatMessageWidgets commonWidgets;
	
	//The instance of the rooms manager
	private final RoomsManagerUI roomsManager;
	
	/**
	 * A constructor creates a send chat message dialog with the preset recipient.
	 * @param roomID the id of the room we will be sending the message from
	 * @param recepientID the recepient's ID
	 * @param recepientLoginName the recipient's login name
	 * @param roomsManager the instane of the rooms manager
	 */
	SendChatMessageDialogUI( final int roomID, final ClickHandler smileyActionLinkClickHandler, final RoomsManagerUI roomsManager ) {
		super( true, false, false, null );
		
		//Store the parameters
		this.commonWidgets = new SendChatMessageWidgets( roomID, smileyActionLinkClickHandler, this, false, roomsManager );
		this.roomsManager = roomsManager;
		
		//Add style
		this.addStyleName( CommonResourcesContainer.SEND_CHAT_MESSAGE_DIALOG_EXTRA_STYLE );
		
		//Update the dialog's title
		updateDialogTitle();
		
		//Call the onClose method to clean up data
		this.addCloseHandler( new CloseHandler<PopupPanel>(){
			public void onClose(CloseEvent<PopupPanel> e) {
				if( e.getTarget() == thisDialog ) {
					//Save the current dialog data
					SendChatMessageManager.getInstance().saveChatMessageDataFromCurrentUI();
				}
			}
		} );
		
		//Initialize the common components
		commonWidgets.initialize();
		
		//Fill dialog with data
		populateDialog();
		
		//Update the list of recipients for the case of no recipients set
		updateMessageRecipientsPanel( );
	}
	
	@Override
	protected void populateDialog() {
		final int NUMBER_OF_GRID_COLUMNS = 6;
		disclosirePanel = addNewGrid( 4, NUMBER_OF_GRID_COLUMNS, true, titlesI18N.disclosurePanelSendChatMessageDialogTitle(), true );
		
		//01 Add the recipients panel
		addToGrid( getCurrentGridIndex(), FIRST_COLUMN_INDEX, NUMBER_OF_GRID_COLUMNS, commonWidgets.messageRecepientsPanel, false, false );

		//02 Add chat message content
		//Add title panel for the chat message content
		HorizontalPanel contentTitlePanel = new HorizontalPanel();
		contentTitlePanel.setWidth("100%");
		contentTitlePanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		//Add the font selection panel
		contentTitlePanel.add( commonWidgets.theFontSelectionPanel );
		addToGrid( getCurrentGridIndex(), FIRST_COLUMN_INDEX, NUMBER_OF_GRID_COLUMNS, contentTitlePanel, true, false );

		//Add message text box component
		HorizontalPanel contentBotyPanel = new HorizontalPanel();
		contentBotyPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		contentBotyPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		
		//Make the Ctrl-Enter to go to the next line
		commonWidgets.messageBodyTextBase.addKeyDownHandler( new KeyDownHandler(){
			public void onKeyDown( KeyDownEvent event ){
				if( event.isControlKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
					TextBaseTranslitAndProgressBar.insertStringIntoTextBoxWrapper("\n", commonWidgets.lengthCounterComposite);
				} else {
					if( ! event.isAnyModifierKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
						//Do not allow to put a new line by pressing just enter
						event.preventDefault();
					}
				}
			}
		} );
		
		contentBotyPanel.add( commonWidgets.lengthCounterComposite );
		contentBotyPanel.add( new HTML("&nbsp;") );
		
		VerticalPanel rightHelperPanel = new VerticalPanel();
		rightHelperPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		rightHelperPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		//Add the Smiled dialog link
		rightHelperPanel.add( commonWidgets.smileyDialogLink );
		//Add the Chat message image link
		//Store the image indicator
		rightHelperPanel.add( commonWidgets.actionImageFileLink );
		//Add the "is private" message check box
		rightHelperPanel.add( commonWidgets.isPrivateCheckBox );
		contentBotyPanel.add( rightHelperPanel );
		addToGrid( getCurrentGridIndex(), FIRST_COLUMN_INDEX, NUMBER_OF_GRID_COLUMNS, contentBotyPanel, true, false );
		
		//05 Add action buttons, the keep dialog open, and use message flows check boxes
		FlexTable actionGrid = addGridActionElements(5, true, FIFTH_COLUMN_INDEX, true, SEVENTH_COLUMN_INDEX, false, 0, true);
		
		keepDialogOpenCheckBox.setText( titlesI18N.keepDialogOpenTitle() );
		keepDialogOpenCheckBox.setValue( true );
		keepDialogOpenCheckBox.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
		keepDialogOpenCheckBox.addClickHandler( new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				//Store the value inside the manager
				SendChatMessageManager.getInstance().setKeepUIOpen( keepDialogOpenCheckBox.getValue() );
			}
		});
		actionGrid.setWidget( 0, FIRST_COLUMN_INDEX, keepDialogOpenCheckBox );
		//Add the use flow check box 
		actionGrid.setWidget( 0, SECOND_COLUMN_INDEX, commonWidgets.useMessageFlowsCheckBox );
		//Add the use alerts check box 
		actionGrid.setWidget( 0, THIRD_COLUMN_INDEX, commonWidgets.useMessageAlertsCheckBox );
		//Add the progress indicator panel
		actionGrid.setWidget( 0, FOURTH_COLUMN_INDEX, commonWidgets.progressBarPanel);
		//Add the clear message button
		actionGrid.setWidget( 0, SIXTH_COLUMN_INDEX, commonWidgets.clearButton );
		
		//Here the right action button title is not the same as for the other dialogs, it should be just ENTER
		setRightActionButtonTitle( titlesI18N.hotKeyEnterButtonToolTip() );
	}
	
	/**
	 * Allows to update the dialog title, this should be called after the room ID is set
	 */
	private void updateDialogTitle() {
		this.setText( titlesI18N.sendMessageToCurrentRoomDialogTitle( roomsManager.getOpenedRoomName( getCurrentRoomID() ) ) );
	}
	
	/**
	 * Allows to maximize and minimize the dialog view
	 * @param maximize true for maximizing and false for minimizing
	 */
	public void maximize(final boolean maximize ){
		//If the disclosure panel is present
		if( disclosirePanel != null ) {
			//If the panel is not already minimized/maximized
			if( disclosirePanel.isOpen() != maximize ) {
				//With the enabled animation the disclosure panel
				//does not show all it's content after being open
				//It is a sort of problem in GWT 1.6
				disclosirePanel.setAnimationEnabled(false);
				disclosirePanel.setOpen( maximize );
				disclosirePanel.setAnimationEnabled(true);
			}
		}
	}
	
	/**
	 * Allows to place the smile's internal code string representation to the current
	 * cursor position in the messages TextBox.
	 * @param smileyInternalCodeString the code string to insert into the chat message
	 */
	@Override
	public void addSmileStringToMessage(final String smileyInternalCodeString){
		commonWidgets.addSmileStringToMessage( smileyInternalCodeString );
	}
	
	/**
	 * Returns the id of the room the dialog is set to send messages to
	 * @return the id of the room
	 */
	@Override
	public int getCurrentRoomID() {
		return commonWidgets.getCurrentRoomID();
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
		//Maximize the panel
		maximize( true );
		
		//Set the message data
		commonWidgets.setMessageData(message, keepRecipients, keepFontSettings, keepMessageType);
		
		//Update the dialog's title
		updateDialogTitle();
	}
	
	/**
	 * Allows to collect the message's data which is currently set in this dialog
	 * @return the chat message data
	 */
	@Override
	public ChatMessage getMessageData( ) {
		return commonWidgets.getMessageData();
	}
	
	/**
	 * Allows to remove all of the currently set message data. I.e. it resets the dialog.
	 * @param localRoomID the id of the current chat room
	 * @param keepRecipients if true then the message's recipients are kept intact
	 * @param keepFontSettings if true then the font settings are preserved
	 * @param keepMessageType if true then we preserve the message type
	 */
	@Override
	public void cleanUIData( final int localRoomID, final boolean keepRecipients,
								 final boolean keepFontSettings, final boolean keepMessageType ) {
		commonWidgets.cleanUIData(localRoomID, keepRecipients, keepFontSettings, keepMessageType);
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
		return titlesI18N.sendButton();
	}
		
	@Override
	protected void actionLeftButton() {
		//Open the send-chat-message panel instead  
		SendChatMessageManager.getInstance().showSendChatMessageUI( commonWidgets.getCurrentRoomID(), false, roomsManager );
	}

	@Override
	protected void actionRightButton() {
		//Send the current chat message
		SendChatMessageManager.getInstance().sentCurrentChatMessage( roomsManager );
	}
	
	@Override
	protected boolean centerOnReShow() {
		//We do not want the dialog to be re-centered because the user 
		//could have put it to a nice and comfortable location on the screen
		return false;
	}

	@Override
	public void setKeepUIOpen(boolean isKeepUIOpen) {
		keepDialogOpenCheckBox.setValue( isKeepUIOpen, false );
	}

	@Override
	public void setMessageFlowsOn(boolean isMessageFlowOn) {
		commonWidgets.setMessageFlowsOn(isMessageFlowOn);
	}

	@Override
	public void setMessageAlertsOn(boolean isMessageAlertsOn) {
		commonWidgets.setMessageAlertsOn(isMessageAlertsOn);
	}
	@Override
	public void onFirstShowInitialize() {
		//Center if the first time
		center();
		//Propagate the call
		commonWidgets.onFirstShowInitialize();
	}

	@Override
	public void showToUser(boolean show) {
		if( show ) {
			//Show the dialog
			show();
			//Maximize the dialog view, if it was not maximized yet
			maximize(true);
		} else {
			//Hide the dialog
			hide();
		}
	}
	
	@Override
	public void setElementsEnabled(boolean enabled) {
		//Enable/disable the send button
		setRightEnabled( enabled );
		//Propagate the call
		commonWidgets.setElementsEnabled( enabled );
	}

	@Override
	public void setProgressBar(final ServerCommStatusPanel progressBarUI) {
		commonWidgets.setProgressBar(progressBarUI);
	}

	@Override
	public void removeMessageRecipient(int recepientID) {
		commonWidgets.removeMessageRecipient( recepientID );
	}

	@Override
	public void updateMessageRecipientsPanel() {
		commonWidgets.updateMessageRecipientsPanel();
	}

	@Override
	public void setChatMessageAttFileDesc( final int roomID, final ShortFileDescriptor fileDesc ) {
		commonWidgets.setChatMessageAttFileDesc( roomID, fileDesc );
	}

	@Override
	public void updateUIElements() {
		//Propagate the call
		commonWidgets.updateUIElements();
	}
	
	@Override
	protected boolean isRightButtonModKeyDown(KeyDownEvent event) {
		//NOTE: we want to be able to send the chat message by just pressing enter
		//with Ctrl-Enter we do a line shift here in the text area
		return !event.isControlKeyDown();
	}

	@Override
	public void rememberChatMessageFonts() {
		commonWidgets.rememberChatMessageFonts();
	}

	@Override
	public void restoreChatMessageFonts() {
		commonWidgets.restoreChatMessageFonts();
	}

	@Override
	public void setFocus() {
		commonWidgets.setFocus();
	}

}
