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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

import com.xcurechat.client.SiteManager;
import com.xcurechat.client.chat.ChatFileUploadDialogUI;
import com.xcurechat.client.chat.RoomsManagerUI;
import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.MessageFontData;
import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;
import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.RoomManagerAsync;
import com.xcurechat.client.rpc.exceptions.RoomAccessException;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.utils.BrowserDetect;
import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.ServerCommStatusPanel;
import com.xcurechat.client.utils.widgets.SmileSelectionDialogUI;

/**
 * @author zapreevis
 * This class contains the functionality required for managing the send-chat-message related actions
 * It allows for two UI representations: a dialog based one and a build-in panel based
 */
public final class SendChatMessageManager {
	
	/**
	 * @author zapreevis
	 * This class should be used to sent a chat message to the chat room, with a time out
	 */
	private class TimeredChatMessageSender extends CommStatusAsyncCallback<Void> {
		//The time out for sending a chat message
		private static final int SEND_MSG_TIME_OUT_MILLISEC = 5000;
		
		//If true then the message sending was sent/aborted by a time-out
		private boolean isReady = false;
		
		//Stores the local room id of the room to which the message is being sent
		private final int localRoomID;
		
		//The chat message to be sent
		final ChatMessage message;
		
		//The used instane of the progress bar
		final ServerCommStatusPanel loadingProgressBar;
		
		//The instance of the rooms manager
		private final RoomsManagerUI roomsManager;
		
		//The message sending Time-out object
		Timer timeOutTimer = new Timer() {
			@Override
			public void run() {
				//Set the progress-bar as if we stopped by an error
				loadingProgressBar.stopProgressBar( true );
				//Call the on failure method with the time-out error message, here we use
				//the room access exception because:
				//	1. we could not send the message to the room, i.e. there
				//	   is smth wrong with accessing it.
				//  2. the room access exceptions are placed into the chat.
				onFailureAct( new RoomAccessException(I18NManager.getErrors().chatMessageSendTimeOut(SEND_MSG_TIME_OUT_MILLISEC/1000)) );
			}
		};
		
		/**
		 * The basic constructor
		 * @param message the chat message to be sent
		 * @param loadingProgressBar the progress bar to use for indicating the message sending
		 */
		public TimeredChatMessageSender( final ChatMessage message, final ServerCommStatusPanel loadingProgressBar,
										 final RoomsManagerUI roomsManager ) {
			super(loadingProgressBar);
			this.localRoomID = message.roomID;
			this.message = message;
			this.loadingProgressBar = loadingProgressBar;
			this.roomsManager = roomsManager;
		}
		
		/**
		 * Should be called in order to send the chat message
		 */
		public void sendChatMessage() {
			RoomManagerAsync roomManagerObject = RPCAccessManager.getRoomManagerAsync();
			isReady = false;
			timeOutTimer.schedule( SEND_MSG_TIME_OUT_MILLISEC );
			roomManagerObject.sendChatMessage(  SiteManager.getUserID(),
												SiteManager.getUserSessionId(),
												message, this);
		}
		
		/**
		 * Should be called when the message sending is complete, i.e. either it was sent or aborted
		 */
		private void messageSendDone() {
			//Set the flag indicating that sending is done in this or that way
			isReady = true;
			//Cancel the timer
			timeOutTimer.cancel();
		}
		
		@Override
		public void onSuccessAct(Void result) {
			if( ! isReady ) {
				//Mark that the message was sent
				messageSendDone();
				//First we clean up only the message content: text and an attached image
				if( ( curentUIInstance.getCurrentRoomID() == localRoomID ) ) {
					//If the dialog is still opened for the chat room
					curentUIInstance.cleanUIData( localRoomID, true, true, true );
					
					//If the dialog does not have to be kept open then close it
					if( ! isKeepUIOpen && ( curentUIInstance instanceof SendChatMessageDialogUI ) ) {
						//Switch to the panel UI
						showSendChatMessageUI( localRoomID, false, roomsManager );
					}
				} else {
					//We are currently browsing another room's send message dialog
					cleanUpStoredMessageData( localRoomID, true, true, true);
				}
				//Enable the Send button
				setElementsEnabled( true );
				//Restore the fonts
				restoreChatMessageFonts();
			}
		}
		
		@Override
		public void onFailureAct(Throwable caught) {
			if( ! isReady ) {
				//Mark that the message was aborted
				messageSendDone();
				//Report the chat room's error
				roomsManager.appendRoomErrorMessage( localRoomID, caught);
				//Enable the Send button
				setElementsEnabled( true );
				//Restore the fonts
				restoreChatMessageFonts();
			}
		}
	}
	
	/**
	 * @author zapreevis
	 * Contains the common methods that should be implemented by the UI repreentations of the send chat message dialog
	 */
	public interface SendChatMsgUI extends SmileSelectionDialogUI.SmileySelectionTarget {
		/**
		 * Allows to collect the message's data which is currently set in this UI
		 * @return the chat message data
		 */
		public ChatMessage getMessageData( );
		/**
		 * Allows to initialize the UI with the chat message data
		 * @param message the stored message data
		 * @param keepRecipients if true then the message's recipients are kept intact
		 * @param keepFontSettings if true then the font settings are preserved
		 * @param keepMessageType if true then we preserve the message type
		 */
		public void setMessageData( final ChatMessage message, final boolean keepRecipients,
				 					final boolean keepFontSettings, final boolean keepMessageType );
		/**
		 * Allows to remove all of the currently set message data. I.e. it resets the UI.
		 * @param localRoomID the id of the current chat room
		 * @param keepRecipients if true then the message's recipients are kept intact
		 * @param keepFontSettings if true then the font settings are preserved
		 * @param keepMessageType if true then we preserve the message type
		 */
		public void cleanUIData( final int localRoomID, final boolean keepRecipients,
									 final boolean keepFontSettings, final boolean keepMessageType );
		/**
		 * Allows to initialize the UI after the UI was shown
		 */
		public void onFirstShowInitialize();
		/**
		 * Allows to get the ID of the current room set in the UI
		 */
		public int getCurrentRoomID();
		/**
		 * Hide the the element
		 * @param show true for showing, false for hiding
		 */
		public void showToUser( final boolean show );
		/**
		 * Allows to set the message flows on/off
		 * @param isMessageFlowOn true to turn the message flows on, otherwise false
		 */
		public void setMessageFlowsOn( final boolean isMessageFlowOn );
		/**
		 * Allows to set the message alerts on/off
		 * @param isMessageAlertOn true to turn the message alerts on, otherwise false
		 */
		public void setMessageAlertsOn( final boolean isMessageAlertOn );
		/**
		 * Allows to set the keep send-chat-message UI open after the message is sent.
		 * This is just an advice or the manager, the particular instance of SendChatMsgUI
		 * might decide not to follow it.
		 * @param isKeepUIOpen true to keep the UI open.
		 */
		public void setKeepUIOpen( final boolean isKeepUIOpen );
		/**
		 * Allows to enable/disable the UI components
		 */
		public void setElementsEnabled( final boolean enabled );
		/**
		 * Allows to set the progress bar to the given UI
		 * @param progressBarUI the progress bar to be set
		 */
		public void setProgressBar( ServerCommStatusPanel progressBarUI );
		/**
		 * Allows to set the chat message file descriptor
		 * @param roomID the id of the room of the message we attach the image to 
		 * @param fileDec the file descriptor to set or null if the file was removed
		 */
		public void setChatMessageAttFileDesc( final int roomID, final ShortFileDescriptor fileDesc );
		/**
		 * Allow to update UI elements
		 */
		public void updateUIElements();
		
		/**
		 * Allows to save the font settings in the internal variables.
		 * Is used for Opera that for some reason resets the fonts
		 * after the message is sent.
		 */
		public void rememberChatMessageFonts();
		
		/**
		 * Allows to restore the font setting from the internal variables.
		 * Is used for Opera that for some reason resets the fonts
		 * after the message is sent.
		 */
		public void restoreChatMessageFonts();
		
		/**
		 * Allows to set focus to the main text object, this will make it bind to the chat selection dialog
		 */
		public void setFocus();
	}
	
	//If true then the message flows are on
	private boolean isMessageFlowOn = true;
	//If true then the message alerts are on
	private boolean isMessageAlertsOn = false;
	//True if we want to keep the send-chat-message UI open
	private boolean isKeepUIOpen = true;
	
	//Store indicators for detecting if this is the first time the UI instances are shown
	private boolean wasDialogFirstShown = true;
	private boolean wasPanelFirstShown = true;
	
	//The send-chat-message UI instances
	private SendChatMessageDialogUI instanceDialog = null;
	private SendChatMessagePanelUI instancePanel = null;
	private SendChatMsgUI curentUIInstance = null;
	
	//The click handler for the smiley action link
	private ClickHandler smileyActionLinkClickHandler = new ClickHandler(){
		@Override
		public void onClick(ClickEvent event) {
			if( curentUIInstance != null ) {
				//Set focus to the main text object in order to
				//bind it to the smiley selection dialog we do it
				//as a safety measure in order to always have the
				//text object binded on opening the smiley dialog. 
				curentUIInstance.setFocus();
			}
			SmileSelectionDialogUI.open();
		}
	};
	
	//The loading progress bar
	private final ServerCommStatusPanel progressBarUI = new ServerCommStatusPanel();
	
	//The mapping between the room IDs and corresponding unsent chat messages
	private Map<Integer, ChatMessage> roomIDToMessage = new HashMap<Integer, ChatMessage>();
	
	//The mapping for the rooms and the opened file upload dialogs
	private Map<Integer, ChatFileUploadDialogUI> roomIDToFileUploadDialog = new HashMap<Integer, ChatFileUploadDialogUI>();
	
	/**
	 * The basic constructor
	 */
	private SendChatMessageManager() {
		
	}
	
	//The only instance of the manager
	private static SendChatMessageManager instanceManager = null;
	
	/**
	 * Allows to get the instance of the manager
	 */
	public static SendChatMessageManager getInstance() {
		if( instanceManager == null ) {
			instanceManager = new  SendChatMessageManager();
		}
		return instanceManager;
	}
	
	/**
	 * Allows to retrieve the send chat message panel instance
	 * @param roomID the id of the room we need this widget for
	 * @param roomsManager the instane of the rooms manager
	 * @return the send chat message panel instance 
	 */
	public SendChatMessagePanelUI getSendChatMessagePanelUI( final int roomID, final RoomsManagerUI roomsManager ) {
		if( curentUIInstance == null ) {
			//No send chat message UI is initialized yet, get the UI panel version
			return (SendChatMessagePanelUI) showSendChatMessageUI( roomID, false, roomsManager );
		} else {
			if( curentUIInstance != instancePanel ) {
				//We have an open send chat message dialog, just return the instance of the panel
				if( instancePanel == null ) {
					//The panel was not created yet, and we currently have the dialog
					instancePanel = new SendChatMessagePanelUI( roomID, smileyActionLinkClickHandler, roomsManager);
					instancePanel.setVisible( false ); //Thus make the panel invisible
				}
			} else {
				//The panel is set to be current and is not null, so it should be visible already
			}
			//Update the UI Elements
			instancePanel.updateUIElements();
			return instancePanel;
		}
	}
		
	/**
	 * Allows to open the send chat message dialog with the chat message data stored in the manager
	 * @param roomID the id of the room for which we open this panel
	 * @param isDialogMode true if we want to show the dialog, false if we want to show the panel
	 * @param roomsManager the instane of the rooms manager
	 * @return the current instance of the UI
	 */
	public SendChatMsgUI showSendChatMessageUI( final int roomID, final boolean isDialogMode, final RoomsManagerUI roomsManager ) {
		//Save the chat message data from the current UI
		saveChatMessageDataFromCurrentUI();
		
		//Hide the old current instance from the user
		if( curentUIInstance != null ) {
			curentUIInstance.showToUser(false);
		}
		
		//Instantiate and set the current UI element if needed
		if( isDialogMode ) {
			if( instanceDialog == null ) {
				instanceDialog = new SendChatMessageDialogUI( roomID, smileyActionLinkClickHandler, roomsManager );
			}
			curentUIInstance = instanceDialog;
		} else {
			if( instancePanel == null ) {
				instancePanel = new SendChatMessagePanelUI( roomID, smileyActionLinkClickHandler, roomsManager );
			}
			curentUIInstance = instancePanel;
		}
		
		//Set the progress bar
		curentUIInstance.setProgressBar( progressBarUI );
		
		//Update the UI Elements
		curentUIInstance.updateUIElements();
		
		//Re-attach the smiley selection dialog, if any
		SmileSelectionDialogUI.bind( curentUIInstance );
		
		//Restore the main UI settings
		curentUIInstance.setMessageFlowsOn( isMessageFlowOn );
		curentUIInstance.setKeepUIOpen( isKeepUIOpen );
		curentUIInstance.setMessageAlertsOn( isMessageAlertsOn );
		
		//Set the chat message data if any
		final ChatMessage roomMessage = roomIDToMessage.get( roomID );
		if( roomMessage != null ) {
			//If we were writing a chat message in this room then we set it back to the dialog
			curentUIInstance.setMessageData( roomMessage, false, false, false );
		} else {
			//Otherwise we get the default message, i.e. a blank one
			curentUIInstance.cleanUIData( roomID, false, false, false );
		}
		
		//Show the new current instance to the user
		curentUIInstance.showToUser(true);
		
		//Do the initial initializations in case the object was just created
		if( wasPanelFirstShown && !isDialogMode ) {
			curentUIInstance.onFirstShowInitialize( );
			wasPanelFirstShown = false;
		}
		if( wasDialogFirstShown && isDialogMode ) {
			curentUIInstance.onFirstShowInitialize( );
			wasDialogFirstShown = false;
		}
		
		//Update the UI elements of the send-chat-message UI
		updateUIElements();
		
		//Update the UI elements of the rooms manager
		roomsManager.updateUIElements();
		
		//The current instance is returned
		return curentUIInstance;
	}
	
	/**
	 * Allows to hide/show the send-chat-message dialog if it is the current sent-chat-message widget.
	 * Allows to show and hide the chat-message file upload dialog if it is currently applicable.
	 * Allows to hide the smiley dialog, but does not show it back.
	 */
	public void sendMessageDialogsShow( final boolean show ) {
		if( curentUIInstance != null ) {
			//Show the dialog instance to the user if it is the current UI
			if( curentUIInstance == instanceDialog ) {
				curentUIInstance.showToUser( show );
			}
			//Show and hide the associated file upload dialog if any
			final int roomID = curentUIInstance.getCurrentRoomID();
			ChatFileUploadDialogUI dialog = roomIDToFileUploadDialog.get(roomID);
			if( dialog != null ) {
				dialog.setVisible( show );
			}
		}
		//Hide the smiley selection dialog, but do not show it back,
		//the user must click on the action link to see the dialog again 
		if( ! show ) {
			SmileSelectionDialogUI.unbindAndHide();
		}
	}
	
	/**
	 * Allows to clean up stored message data if any.
	 * @param roomID the id of the room for which the stored message data is retrieved
	 * @param keepRecipients if true then the message's recipients are kept intact
	 * @param keepFontSettings if true then the font settings are preserved
	 * @param keepMessageType if true then we preserve the message type
	 */
	private void cleanUpStoredMessageData( final int roomID, final boolean keepRecipients,
												  final boolean keepFontSettings, final boolean keepMessageType ) {
		ChatMessage storedMessageData = roomIDToMessage.get( roomID );
		if( storedMessageData != null ) {
			//Reset the attached file 
			storedMessageData.fileDesc = null;
			//Reset the message text
			storedMessageData.messageBody = "";
			//Reset the message type
			if( ! keepMessageType ) {
				storedMessageData.messageType = ChatMessage.Types.SIMPLE_MESSAGE_TYPE;				
			}
			//Reset the recipients
			if( ! keepRecipients ) {
				storedMessageData.recipientIDs = null;
				storedMessageData.recepientIDToLoginName = new LinkedHashMap<Integer, String>();
			}
			//Reset the font settings
			if( ! keepFontSettings ) {
				storedMessageData.fontType = MessageFontData.DEFAULT_FONT_FAMILY;
				storedMessageData.fontSize = MessageFontData.DEFAULT_FONT_SIZE;
				storedMessageData.fontColor = MessageFontData.DEFAULT_FONT_COLOR;
			}
			//Put the message back
			roomIDToMessage.put( roomID, storedMessageData );
		}
	}
	
	/**
	 * Allows to save the current chat message data from the currently visible UI, if any
	 */
	public void saveChatMessageDataFromCurrentUI() {
		if( curentUIInstance != null ) {
			final ChatMessage message = curentUIInstance.getMessageData();
			//Save the message into the map
			roomIDToMessage.put( message.roomID, message);
		}
	}
	
	/**
	 * This method should be called before we switch to the new chat room.
	 * It allows to update the send message dialog with the necessary data.
	 * @param roomID the id of the room we are switching to.
	 */
	public void onRoomChangeSendDialogModification( final int roomID ) {
		if( curentUIInstance != null ) {
			//Save the current room data
			saveChatMessageDataFromCurrentUI();
			//Load the next opened room's dialog data from the mapping
			ChatMessage storedMessage = roomIDToMessage.get( roomID );
			if( storedMessage != null ) {
				//If we were writing a chat message in this new room then we set it back to the dialog
				curentUIInstance.setMessageData( storedMessage, false, false, false );
			} else {
				//Get the default message, a blank one
				curentUIInstance.cleanUIData( roomID, false, false, false );
			}
			
			//Show the file upload dialog for this room if it was open
			showMessageFileUploadDialog( roomID );
			
			//Update the UI elements of the send-chat-message UI
			updateUIElements();
		} else {
			Window.alert("Switching the room but the current send-chat-message UI instance is null!");
		}
	}
	
	/**
	 * Allows to set the message flows on/off
	 * @param isMessageFlowOn true to turn the message flows on, otherwise false
	 */
	public void setMessageFlowsOn( final boolean isMessageFlowOn ) {
		this.isMessageFlowOn = isMessageFlowOn;
	}
	
	/**
	 * Allows to set the message alerts on/off
	 * @param isMessageFlowOn true to turn the message flows on, otherwise false
	 */
	public void setMessageAlertsOn( final boolean isMessageAlertsOn ) {
		this.isMessageAlertsOn = isMessageAlertsOn;
	}
	
	/**
	 * Allows to set the keep send-chat-message UI open after the message is sent.
	 * This is just an advice or the manager, the particular instance of SendChatMsgUI
	 * might decide not to follow it.
	 * @param isKeepUIOpen true to keep the UI open.
	 */
	public void setKeepUIOpen( final boolean isKeepUIOpen ) {
		this.isKeepUIOpen = isKeepUIOpen;
	}
	
	/**
	 * Allows to send the current chat message from the current UI
	 * @param roomsManager the instance of the rooms manager
	 */
	public void sentCurrentChatMessage( final RoomsManagerUI roomsManager ) {
		//Disable the Send button
		setElementsEnabled(false);
		
		//Take the message data set in this dialog
		ChatMessage message = curentUIInstance.getMessageData( );
		//Complete the message data
		message.senderID = SiteManager.getUserID();
		//We have to process the message recipients in this way
		//Because otherwise GWT complains
		message.recipientIDs = new LinkedHashSet<Integer>();
		message.recipientIDs.addAll( message.recepientIDToLoginName.keySet() );
		message.recepientIDToLoginName = null;
		try{
			//Validate the message content
			message.validateAndComplete();
			//Remember the message font settings
			rememberChatMessageFonts();
			//Create the message sender object
			TimeredChatMessageSender sender = new TimeredChatMessageSender( message, progressBarUI, roomsManager );
			//Send the message
			sender.sendChatMessage();
		} catch ( final SiteException e) {
			//Enable the Send button
			setElementsEnabled( true );
			(new SplitLoad( true ) {
				@Override
				public void execute() {
					//Report the error
					ErrorMessagesDialogUI.openErrorDialog( e );
				}
			}).loadAndExecute();
		}
	}
	
	/**
	 * Is needed for opera that re-sets the fonts after the message was sent or the site section was re-selected
	 */
	public void restoreChatMessageFonts() {
		if( BrowserDetect.getBrowserDetect().isOpera() && ( curentUIInstance != null ) ) {
			curentUIInstance.restoreChatMessageFonts();
		}
	}
	
	/**
	 * Is needed for opera that re-sets the fonts after the message was sent or the site section was re-selected
	 */
	public void rememberChatMessageFonts() {
		if( BrowserDetect.getBrowserDetect().isOpera() && ( curentUIInstance != null ) ) {
			curentUIInstance.rememberChatMessageFonts();
		}
	}
	
	/**
	 * Allows to enable/disable the UI components
	 */
	private void setElementsEnabled( final boolean enabled ) {
		if( instanceDialog != null ) {
			instanceDialog.setElementsEnabled( enabled );
		}
		if( instancePanel != null ) {
			instancePanel.setElementsEnabled( enabled );
		}
	}
	
	/**
	 * Allows to detect if the current message set into the UI is private or not
	 * If the current UI instance is not initialized yet, then the method returns false
	 * @return true if the message is private, otherwise false
	 */
	public boolean isPrivateMessageUI() {
		return ( curentUIInstance != null ) ? (curentUIInstance.getMessageData().messageType == ChatMessage.Types.PRIVATE_MESSAGE_TYPE ) : false;
	}
	
	/**
	 * Allows to write a reply to the given chat message. If there is already
	 * a message data for this room then the it is lost, we only
	 * preserve the font settings.
	 * @param message the message we are going to reply to
	 * @param recipientIDToLoginName the message recipients mapping, the linked map that preserves the order of recipients
	 */
	public void replyToChatMessage( final ChatMessage message, final LinkedHashMap<Integer, String> recipientIDToLoginName ) {
		//Convert the given message to the reply message
		ChatMessage replyMessage = message.clone();
		replyMessage.senderID = SiteManager.getUserID();
		replyMessage.fileDesc = null;
		replyMessage.messageBody = "";
		replyMessage.recepientIDToLoginName.clear();
		replyMessage.recepientIDToLoginName.putAll( recipientIDToLoginName );
		replyMessage.recipientIDs.clear();
		
		//Set the message to be the reply message
		curentUIInstance.setMessageData( replyMessage, false, !isMessageFlowOn, false );
	}
	
	/**
	 * Allows to send a simple message to the specified user.
	 * The recipient is just added to the list of recipients.
	 * @param roomID the room id to which we will be sending messages
	 * @param recepientID the id of the recipient we are adding
	 * @param recepientLoginName the login of the recipient we are adding
	 */
	public void writeToChatRoomUser( final int roomID, final int recepientID,
												  final String recepientLoginName ) {
		ChatMessage message = curentUIInstance.getMessageData().clone();
		//Force the room id, although this should not be necessary
		message.roomID = roomID;
		//Add one extra recipient
		message.recepientIDToLoginName.put(recepientID, recepientLoginName);
		
		//Set the updated message back to the UI
		curentUIInstance.setMessageData( message, false, false, false );
	}
	
	/**
	 * Allows to update UI elements
	 */
	public void updateUIElements() {
		//Update the UI element if it is set
		if( curentUIInstance != null ) {
			curentUIInstance.updateUIElements();
		}
	}
	
	/**
	 * Show the file upload dialog for this room if it was open
	 * @param roomID the room for which we attempt to show the dialog
	 */
	private void showMessageFileUploadDialog( final int roomID ) {
		//First we set all of the other dialogs to be invisible
		for( ChatFileUploadDialogUI dialog : roomIDToFileUploadDialog.values() ) {
			dialog.setVisible( false );
		}
		//Next if there is an opened file upload dialog for this room then we show it
		ChatFileUploadDialogUI dialog = roomIDToFileUploadDialog.get( roomID );
		if( dialog != null ) {
			dialog.setVisible( true );
		}
	}
	
	/**
	 * Allows to open the file upload dialog for the chat mesage to the given room
	 * @param roomID the id of the room
	 * @param fileDescr the file descriptor of the currently attached file or null if none
	 */
	public void openFileUploadDialog(final int roomID, final ShortFileDescriptor fileDescr) {
		ChatFileUploadDialogUI dialog = roomIDToFileUploadDialog.get(roomID);
		if( dialog == null ) {
			(new SplitLoad( true ) {
				@Override
				public void execute() {
					ChatFileUploadDialogUI newDialog = new ChatFileUploadDialogUI( roomID, fileDescr, SendChatMessageManager.this);
					roomIDToFileUploadDialog.put(roomID, newDialog);
					newDialog.show();
					newDialog.center();
				}
			}).loadAndExecute();
		} else {
			dialog.show();
			dialog.center();
		}
	}
	
	/**
	 * Allows to set the chat message file descriptor and close the file upload dialog
	 * @param roomID the id of the room of the message we attach the image to 
	 * @param fileDec the file descriptor to set or null if the file was removed
	 */
	public void onChatRoomMessageFileUploadDilalogClose( final int roomID, final ShortFileDescriptor fileDesc ) {
		if( curentUIInstance.getCurrentRoomID() == roomID ) {
			//If he current send-chat-message widget instance is for sending
			//the message to the room to which the file was uploaded
			
			//Set the attached file
			curentUIInstance.setChatMessageAttFileDesc(roomID, fileDesc);
		} else {
			//If the current send-chat-message widget instance is NOT for 
			//sending the message to the room to which the file was uploaded
			
			//NOTE: This should not be happening, but for safety the following must work
			ChatMessage message = roomIDToMessage.get( roomID );
			if( message == null ) {
				message = new ChatMessage();
				message.roomID = roomID;
				roomIDToMessage.put( roomID, message );
			}
			message.fileDesc = fileDesc;
		}
		
		//Remove the dialog from the mapping
		roomIDToFileUploadDialog.remove( roomID );
	}
}
