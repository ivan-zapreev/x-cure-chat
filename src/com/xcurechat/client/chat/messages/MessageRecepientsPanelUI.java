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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.xcurechat.client.SiteManager;
import com.xcurechat.client.chat.RoomsManagerUI;
import com.xcurechat.client.chat.messages.MessageRecipientUI.MessageRecipientPanel;
import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;
import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This class represents the chat-message recipients panel
 */
public class MessageRecepientsPanelUI extends Composite implements MessageRecipientPanel {
	
	//The row in the table to which we add the recipient-related elements
	private final static int RPTS_TABLE_ROW_IDX = 0;
	private final static int RPTS_TABLE_ROW_LAB_COL_IDX = 0;
	private final static int RPTS_TABLE_ROW_IMG_COL_IDX = 1;
	private final static int RPTS_TABLE_ROW_SELECT_COL_IDX = 2;
	private final static int RPTS_TABLE_ROW_RCPS_COL_IDX = 3;
	
	//The panel that stores the list of message recipients
	private final HorizontalPanel recipientsPanel = new HorizontalPanel();
	//The label storing the recipients label and add recipients image, needed for offset computations
	private Label recipientsLabel = null;
	private Image addRecipientImage = null;
	//The table storing the recepients related widgets
	final FlexTable mainRecipientsTable = new FlexTable();
	//The scroll panel containing the message recipients
	private final ScrollPanel recepientsScrollPanel = new ScrollPanel();
	//This object will store the list of all visible chat users
	//that can be selected to become message recipients.
	private final ListBox visibleChatMsgRecipients = new ListBox();
	//The list of message recipient ids
	private final LinkedHashMap<Integer, String> recepientIDToLoginName = new LinkedHashMap<Integer, String>();
	//The parent dialog box, i.e. the dialog that stores this panel, or null
	private final DialogBox parentDialog;
	//The maximum desired (offset) width for the recipients  panel
	private final int maxOffsetWidth;
	//The current room id, i.e. the id of the room to which we want to write 
	private int currentRoomID;
	
	//The localization for the text
	protected static final UITitlesI18N titlesI18N = I18NManager.getTitles();
	
	//The instance of the rooms manager
	private final RoomsManagerUI roomsManager;
	
	/**
	 * THe basic constructor
	 * @param parentDialog the dialog which contains this panel or null
	 * @param maxOffsetWidth the maximum width of the recipients panel in pixels
	 * @param currentRoomID the id of the room we are currently in
	 * @param roomsManager the instance of the rooms manager
	 */
	public MessageRecepientsPanelUI( final DialogBox parentDialog, final int maxOffsetWidth,
									 final int currentRoomID, final RoomsManagerUI roomsManager ) {
		//Store the parameters
		this.parentDialog = parentDialog;
		this.maxOffsetWidth = maxOffsetWidth;
		this.currentRoomID = currentRoomID;
		this.roomsManager = roomsManager;
		
		//Populate the panel
		populatePanel();
		
		//Set the style name
		mainRecipientsTable.setStyleName( CommonResourcesContainer.RECEPIENTS_PANEL_STYLE );
		
		//Initialize the composite
		initWidget( mainRecipientsTable );
	}
	
	protected void populatePanel() {
		//01 Add chat message recipients
		mainRecipientsTable.insertRow( RPTS_TABLE_ROW_IDX );
		mainRecipientsTable.insertCell( RPTS_TABLE_ROW_IDX, RPTS_TABLE_ROW_LAB_COL_IDX );
		mainRecipientsTable.insertCell( RPTS_TABLE_ROW_IDX, RPTS_TABLE_ROW_IMG_COL_IDX );
		mainRecipientsTable.insertCell( RPTS_TABLE_ROW_IDX, RPTS_TABLE_ROW_SELECT_COL_IDX );
		mainRecipientsTable.insertCell( RPTS_TABLE_ROW_IDX, RPTS_TABLE_ROW_RCPS_COL_IDX );
		
		//Add the recipient field title
		HorizontalPanel labelPanel = new HorizontalPanel();
		labelPanel.setHeight( "100%" );
		labelPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		labelPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		recipientsLabel = InterfaceUtils.getNewFieldLabel( titlesI18N.recipientsFieldTitle(), true );
		labelPanel.add( recipientsLabel );
		//Add a space delimiter
		labelPanel.add( new HTML( "&nbsp;" ) );
		mainRecipientsTable.setWidget( RPTS_TABLE_ROW_IDX, RPTS_TABLE_ROW_LAB_COL_IDX, labelPanel);

		//Add the add recipient image
		addRecipientImage = new Image( ServerSideAccessManager.getAddChatMessageRecepientImageButtonURL() );
		addRecipientImage.setStyleName( CommonResourcesContainer.ADD_CHAT_MSG_RECEPIENT_IMAGE_STYLE );
		addRecipientImage.setTitle( titlesI18N.addRecipientToolTip() );
		addRecipientImage.addClickHandler( new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				if( mainRecipientsTable.getCellFormatter().isVisible( RPTS_TABLE_ROW_IDX, RPTS_TABLE_ROW_SELECT_COL_IDX ) ) {
					//Clean up the list of recipients in the list box then 
					//make it hidden and adjust the size of the scroll panel
					hideRecipientsListBox();
				} else {
					//Fill out the list of recipients in the list box then 
					//make it visible and adjust the size of the scroll panel
					showRecipientsListBox();
				}
			}
		});
		mainRecipientsTable.setWidget( RPTS_TABLE_ROW_IDX, RPTS_TABLE_ROW_IMG_COL_IDX, addRecipientImage);
		
		//Add an invisible list box for selecting message recipients
		visibleChatMsgRecipients.setVisibleItemCount(1);
		visibleChatMsgRecipients.addChangeHandler( new ChangeHandler(){
			@Override
			public void onChange(ChangeEvent event) {
				final int index = visibleChatMsgRecipients.getSelectedIndex();
				if(  index > 0 ) {
					//If there is a user selected other then add the user
					try {
						addMessageRecipient( Integer.parseInt( visibleChatMsgRecipients.getValue( index ) ),
											 visibleChatMsgRecipients.getItemText(index) );
						visibleChatMsgRecipients.setSelectedIndex( 0 );
					} catch (NumberFormatException e) {
						//Well there is nothing we can do about it ...
					}
				}
			}
		});
		//Make the selection list box cell hidden
		mainRecipientsTable.getCellFormatter().setVisible( RPTS_TABLE_ROW_IDX, RPTS_TABLE_ROW_SELECT_COL_IDX, false);
		mainRecipientsTable.setWidget( RPTS_TABLE_ROW_IDX, RPTS_TABLE_ROW_SELECT_COL_IDX, visibleChatMsgRecipients );
		
		//Add the scroll panel around the panel of recipients
		recepientsScrollPanel.setStyleName( CommonResourcesContainer.RECEPIENTS_SCROLL_PANEL_STYLE );
		recipientsPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		recepientsScrollPanel.add( recipientsPanel );
		mainRecipientsTable.setWidget( RPTS_TABLE_ROW_IDX, RPTS_TABLE_ROW_RCPS_COL_IDX, recepientsScrollPanel );
	}
	
	/**
	 * Allows to get the map of the currently selected recipients
	 * @return the map of the currently selected recipients
	 */
	public LinkedHashMap<Integer, String> getCurrentRecipients() {
		return this.recepientIDToLoginName;
	}
	
	/**
	 * Allows to keep the current room id up to date
	 * @param currentRoomID the current room id, i.e. the id of the room to which we want to write 
	 */
	public void setCurrentRoomID( final int currentRoomID ) {
		this.currentRoomID = currentRoomID;
	}
	
	/* (non-Javadoc)
	 * @see com.xcurechat.client.chat.messages.MessageRecipientUI.MessageRecipientPanel#removeMessageRecipient(int)
	 */
	@Override
	public void removeMessageRecipient(int recepientID) {
		if( recepientIDToLoginName.keySet().contains( recepientID ) ) {
			for( int index = 0; index < recipientsPanel.getWidgetCount(); index++ ) {
				Widget w = recipientsPanel.getWidget( index );
				if( ( w instanceof MessageRecipientUI ) && 
					( ( (MessageRecipientUI) w ).getRecepientID() == recepientID ) ) {
					//We found the recipient
					if( index < recipientsPanel.getWidgetCount() - 1  ) {
						//If this not the last recipient then remove the delimiter
						recipientsPanel.remove( index + 1 );
						//Remove the recipient itself from the list of recipient UI elements
						recipientsPanel.remove( index );					
					} else {
						//Remove the recipient itself from the list of recipient UI elements
						recipientsPanel.remove( index );					
						if( index > 0 ) {
							//If this is the last user in the list and there are
							//more users then remove the up front delimiter
							recipientsPanel.remove( index - 1 );
						}
					}
					//Remove the recepient's ID from the set of recipients
					recepientIDToLoginName.remove( recepientID );
					//Stop iterations
					break;
				}
			}
			//Update the list of potential recipients
			updateAddRecipientsListBox();
		}
	}
		
	/**
	 * Allows to remove all of the the message recipients
	 */
	public void removeMessageRecipients( ) {
		//Clean up the recipients
		recepientIDToLoginName.clear();
		recipientsPanel.clear();
		//Update the list of potential recipients
		updateAddRecipientsListBox();
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.chat.messages.MessageRecipientUI.MessageRecipientPanel#updateMessageRecipientsPanel()
	 */
	@Override
	public void updateMessageRecipientsPanel() {
		//If there are no recipients set
		if( recepientIDToLoginName.isEmpty() ) {
			//Clear the panel from the previously set data
			recipientsPanel.clear();
			//If this message is private then 
			if( SendChatMessageManager.getInstance().isPrivateMessageUI() ) {
				//This message will be to no one
				recipientsPanel.add( new Label( titlesI18N.recepientsAreNoone() ) );
			} else {
				//If not private then it is for every one
				recipientsPanel.add( new Label( titlesI18N.recepientsAreEveryone() ) );
			}
		}
	}
	
	/**
	 * Allows to add the message recipient
	 * @param recepientID the recepient's ID
	 * @param recepientLoginName the recipient's login name
	 */
	public void addMessageRecipient( final int recepientID, final String recepientLoginName ) {
		//First check that the list of recipients is empty
		if( recepientIDToLoginName.isEmpty() ) {
			//If so then clean up the recipients panel from any possible text in it
			recipientsPanel.clear();
		}
		//Now add the recipients
		if( ! recepientIDToLoginName.keySet().contains( recepientID ) ) {
			if( recepientIDToLoginName.size() < ChatMessage.MAXIMUM_NUMBER_MESSAGE_RECIPIENTS ) {
				if( ! recepientIDToLoginName.isEmpty() ) {
					//Add the delimiter if this is not the first recipient we are adding
					recipientsPanel.add( new HTML(",&nbsp;") );
				}
				
				//Add the message recipient widget
				recipientsPanel.add( new MessageRecipientUI( recepientID, recepientLoginName, this, parentDialog ) );
				
				//Add the recepient's ID to the set of recipients
				recepientIDToLoginName.put( recepientID, recepientLoginName );
				
				//Update the list of potential recipients
				updateAddRecipientsListBox();
			} else {
				//Report an error, that the maximum number of message recipients is reached
				(new SplitLoad( true ) {
					@Override
					public void execute() {
						//Report the error
						ErrorMessagesDialogUI.openErrorDialog( I18NManager.getErrors().maximumAllowedNumberOfMessageRecipients( ChatMessage.MAXIMUM_NUMBER_MESSAGE_RECIPIENTS ) );
					}
				}).loadAndExecute();
			}
		}
	}
	
	/**
	 * This method allows to set the proper width of the recipients' scroll panel.
	 * Should be called only when the send chat message dialog is displayed (visible).
	 */
	public void addjustRecipientsScrollPanel() {
		int allowedMaxWidthInPixels = maxOffsetWidth -
									  recipientsLabel.getOffsetWidth() -
									  addRecipientImage.getOffsetWidth();
		//if the recipients selection list box is visible then account for it
		if( mainRecipientsTable.getCellFormatter().isVisible( RPTS_TABLE_ROW_IDX, RPTS_TABLE_ROW_SELECT_COL_IDX ) ) {
			allowedMaxWidthInPixels -= visibleChatMsgRecipients.getOffsetWidth();
		}
		recepientsScrollPanel.setWidth( allowedMaxWidthInPixels + "px" );
	}
	
	/**
	 * Allows to hide the list box with the possible message recipients
	 */
	public void hideRecipientsListBox() {
		if( mainRecipientsTable.getCellFormatter().isVisible( RPTS_TABLE_ROW_IDX, RPTS_TABLE_ROW_SELECT_COL_IDX ) ) {
			//Make the list box disappear
			mainRecipientsTable.getCellFormatter().setVisible(RPTS_TABLE_ROW_IDX, RPTS_TABLE_ROW_SELECT_COL_IDX, false);
			//Clean up the list box
			visibleChatMsgRecipients.clear();
			//Adjust the recipient's scroll panel
			addjustRecipientsScrollPanel( );
		}
	}
	
	/**
	 * Allows to fill out a non-filled List Box with the possible message recipients.
	 * NOTE: the method assumes that this list box is empty
	 */
	private void showRecipientsListBox() {
		//Fill the list box with potential message recipients
		Map<Integer, ShortUserData> visibleUsers = roomsManager.getRoomVisibleUsers( currentRoomID );
		visibleChatMsgRecipients.addItem( titlesI18N.selectButtonTitle(), ShortUserData.UNKNOWN_UID + "" );
		visibleChatMsgRecipients.setSelectedIndex(0);
		if( visibleUsers != null ) {
			Iterator<ShortUserData> iter = visibleUsers.values().iterator();
			final int currentUserID = SiteManager.getUserID();
			final Set<Integer> usedRecipients = recepientIDToLoginName.keySet();
			while( iter.hasNext() ) {
				ShortUserData userData = iter.next();
				int userID = userData.getUID();
				if( ( currentUserID != userID ) && ( ! usedRecipients.contains( userID ) ) ) {
					visibleChatMsgRecipients.addItem( userData.getUserLoginName(), userID + "" );
				}
			}
		}
		//Make the selection list box appear
		mainRecipientsTable.getCellFormatter().setVisible( RPTS_TABLE_ROW_IDX, RPTS_TABLE_ROW_SELECT_COL_IDX, true);
		//Adjust the recipient's scroll panel
		addjustRecipientsScrollPanel( );
	}
	
	/**
	 * Allows to update the list of potential message recipients in the ListBox
	 * This method only does changes if the ListBox is visible, and it also cleans
	 * up the list box before adding recipients to it
	 */
	private void updateAddRecipientsListBox() {
		if( mainRecipientsTable.getCellFormatter().isVisible( RPTS_TABLE_ROW_IDX, RPTS_TABLE_ROW_SELECT_COL_IDX ) ) {
			hideRecipientsListBox();
			showRecipientsListBox();
		}
	}
}
