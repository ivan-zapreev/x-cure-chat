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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import com.xcurechat.client.chat.RoomsManagerUI;
import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.ShortFileDescriptor;

import com.xcurechat.client.utils.BrowserDetect;
import com.xcurechat.client.utils.widgets.Button;
import com.xcurechat.client.utils.widgets.ServerCommStatusPanel;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This is the send chat message panel
 */
public class SendChatMessagePanelUI extends Composite implements SendChatMessageManager.SendChatMsgUI,
																 MessageRecipientUI.MessageRecipientPanel {
	//The decorated panel storing its elements.
	private final SimplePanel mainContainerPanel = new SimplePanel();
	
	//The main panel storing the send chat message panel components
	private final HorizontalPanel mainHPanel = new HorizontalPanel();
	//The send button
	private final Button sendButton = new Button();
	//The dialog button
	private final Button dialogButton = new Button();
	//Stores true if the component is enabled, otherwise false
	private boolean isEnabled = true;
	
	//Stores the chat-message panel main widgets and method for working and initialization
	private final SendChatMessageWidgets commonWidgets;
	
	//The instance of the rooms manager
	private final RoomsManagerUI roomsManager;
	
	/**
	 * A constructor creates a send chat message panel with the preset recipient.
	 * @param roomID the id of the room we will be sending the message from
	 * @param smileyActionLinkClickHandler the click handler for the smiley selection action link
	 * @param roomsManager the instance of the rooms manager
	 */
	public SendChatMessagePanelUI( final int roomID, final ClickHandler smileyActionLinkClickHandler, final RoomsManagerUI roomsManager ) {
		//Store the parameters
		this.commonWidgets = new SendChatMessageWidgets( roomID, smileyActionLinkClickHandler, this, true, roomsManager );
		this.roomsManager = roomsManager;
		
		//Initialize the common components
		commonWidgets.initialize();
		
		//Populate the panel with elements
		populatePanel();
		
		//Initialize the widget composite
		initWidget( mainContainerPanel );
	}
	
	private void populatePanel() {
		mainHPanel.addStyleName( CommonResourcesContainer.SEND_CHAT_MESSAGE_PANEL_STYLE );
		mainHPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		mainHPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		
		//This panel will store the largest UI element: The recipients panel, the font
		//selection panel and the text box base for the message body 
		VerticalPanel mainVPanel = new VerticalPanel();
		mainVPanel.setSize("100%", "100%");
		mainVPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		mainVPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		
		mainVPanel.add( commonWidgets.messageRecepientsPanel );
		mainVPanel.add( commonWidgets.theFontSelectionPanel );
		//Add the key listener to the text box 
		commonWidgets.messageBodyTextBase.addKeyDownHandler( new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if( isEnabled && event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
					//Send the message
					SendChatMessageManager.getInstance().sentCurrentChatMessage( roomsManager );
				}
			}
		});
		mainVPanel.add( commonWidgets.lengthCounterComposite );
		
		if( BrowserDetect.getBrowserDetect().isOpera() ) {
			mainHPanel.add( new HTML("&nbsp;&nbsp;") ); //This will work as a padding in Opera
		}
		mainHPanel.add( mainVPanel );
		mainHPanel.add( new HTML("&nbsp;") );
		
		//This panel will store the supplementary UI elements such as action links, buttons and check boxes
		VerticalPanel suppVPanel = new VerticalPanel();
		suppVPanel.setSize("100%", "100%");
		suppVPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		suppVPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		
		//The zero horizontal panel will store the flow check box and the dialog button
		HorizontalPanel suppHPanelZero = new HorizontalPanel();
		suppHPanelZero.setWidth("100%");
		suppHPanelZero.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		suppHPanelZero.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		suppHPanelZero.add( commonWidgets.useMessageFlowsCheckBox );
		suppHPanelZero.add( new HTML("&nbsp;") );
		suppHPanelZero.add( commonWidgets.useMessageAlertsCheckBox );
		//Add the dialog button
		suppHPanelZero.add( new HTML("&nbsp;") );
		dialogButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		dialogButton.setText( commonWidgets.titlesI18N.dialogButton() );
		dialogButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//Send the current chat message
				SendChatMessageManager.getInstance().showSendChatMessageUI( commonWidgets.getCurrentRoomID(), true, roomsManager );
			}
		});
		suppHPanelZero.add( dialogButton );
		suppVPanel.add( suppHPanelZero );
		
		//The first horizontal panel will store the action links and check boxes
		HorizontalPanel suppHPanelOne = new HorizontalPanel();
		suppHPanelOne.setWidth("100%");
		suppHPanelOne.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		suppHPanelOne.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		suppHPanelOne.add( commonWidgets.isPrivateCheckBox );
		suppHPanelOne.add( new HTML("&nbsp;") );
		suppHPanelOne.add( commonWidgets.actionImageFileLink );
		suppHPanelOne.add( new HTML("&nbsp;") );
		suppHPanelOne.add( commonWidgets.smileyDialogLink );
		suppVPanel.add( suppHPanelOne );
		
		//The second horizontal panel with store the buttons and the progress bar
		HorizontalPanel suppHPanelTwo = new HorizontalPanel();
		suppHPanelTwo.setWidth("100%");
		suppHPanelTwo.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		suppHPanelTwo.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		//Add the clear button
		suppHPanelTwo.add( commonWidgets.clearButton );
		suppVPanel.add( suppHPanelTwo );
		//Add the progress bar
		suppHPanelTwo.add( new HTML("&nbsp;") );
		suppHPanelTwo.add( commonWidgets.progressBarPanel );
		//Add the sent button
		sendButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		sendButton.setText( commonWidgets.titlesI18N.sendButton() );
		sendButton.setTitle( commonWidgets.titlesI18N.hotKeyEnterButtonToolTip() );
		sendButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//Send the current chat message
				SendChatMessageManager.getInstance().sentCurrentChatMessage( roomsManager );
			}
		});
		suppHPanelTwo.add( new HTML("&nbsp;") );
		suppHPanelTwo.add( sendButton );
		
		mainHPanel.add( suppVPanel );
		if( BrowserDetect.getBrowserDetect().isOpera() ) {
			mainHPanel.add( new HTML("&nbsp;&nbsp;") ); //This will work as a padding in Opera
		}
		
		mainContainerPanel.addStyleName( CommonResourcesContainer.FLOAT_NONE_LEMENT_STYLE );
		mainContainerPanel.add( mainHPanel );
	}

	@Override
	public void cleanUIData(int localRoomID, boolean keepRecipients, boolean keepFontSettings, boolean keepMessageType) {
		commonWidgets.cleanUIData(localRoomID, keepRecipients, keepFontSettings, keepMessageType);
	}

	@Override
	public void setMessageData(ChatMessage message, boolean keepRecipients, boolean keepFontSettings, boolean keepMessageType) {
		commonWidgets.setMessageData(message, keepRecipients, keepFontSettings, keepMessageType);
	}

	@Override
	public void addSmileStringToMessage(String smileyInternalCodeString) {
		commonWidgets.addSmileStringToMessage(smileyInternalCodeString);
	}

	@Override
	public void setKeepUIOpen(boolean isKeepUIOpen) {
		//Does nothing because this is not a dialog
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
	public void removeMessageRecipient(int recepientID) {
		commonWidgets.removeMessageRecipient(recepientID);
	}

	@Override
	public void updateMessageRecipientsPanel( ) {
		commonWidgets.updateMessageRecipientsPanel();
	}

	@Override
	public ChatMessage getMessageData() {
		return commonWidgets.getMessageData();
	}
	
	@Override
	public void onFirstShowInitialize() {
		//Propagate the call
		commonWidgets.onFirstShowInitialize();
	}

	@Override
	public void showToUser(boolean show) {
		this.setVisible( show );
	}

	@Override
	public int getCurrentRoomID() {
		return commonWidgets.getCurrentRoomID();
	}

	@Override
	public void setElementsEnabled(boolean enabled) {
		isEnabled = enabled;
		sendButton.setEnabled(enabled);
		if( enabled && BrowserDetect.getBrowserDetect().isOpera() ) {
			//On Opera there is a bug, it miss-aligns the button after
			//it is disabled and then enabled again. This is a workaround.
			mainContainerPanel.clear();
			mainContainerPanel.add( mainHPanel );
		}
		//Propagate the call
		commonWidgets.setElementsEnabled( enabled );
	}

	@Override
	public void setProgressBar(ServerCommStatusPanel progressBarUI) {
		commonWidgets.setProgressBar( progressBarUI );
	}

	@Override
	public void setChatMessageAttFileDesc(final int roomID, final ShortFileDescriptor fileDesc) {
		commonWidgets.setChatMessageAttFileDesc( roomID, fileDesc );
	}

	@Override
	public void updateUIElements() {
		Scheduler.get().scheduleDeferred( new ScheduledCommand(){
			@Override
			public void execute() {
				//Adjust the width of the recipients panel, do it in a deferred
				//command just in case the element is not visible yet
				commonWidgets.addjustRecipientsScrollPanel();
			}
		});
		
		//Opera can not set the proper width for the message body wrapper. Therefore,
		//we first try to put the focus to the button and then to the text box
		if( BrowserDetect.getBrowserDetect().isOpera() ) {
			sendButton.setFocus(true);
		}
		//Propagate the call
		commonWidgets.updateUIElements();
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
