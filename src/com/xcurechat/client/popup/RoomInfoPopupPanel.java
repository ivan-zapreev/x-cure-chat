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
package com.xcurechat.client.popup;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.xcurechat.client.SiteManager;
import com.xcurechat.client.chat.RoomsManagerUI;
import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.data.UserData;

import com.xcurechat.client.dialogs.profile.ViewUserProfileDialogUI;
import com.xcurechat.client.dialogs.room.RoomDialogUI;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.RoomManagerAsync;

import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.StringUtils;
import com.xcurechat.client.utils.widgets.Button;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This class is responsible for showing the room-info pop-up panel
 */
public class RoomInfoPopupPanel extends InfoPopupPanel {
	
	//The maximum width (in symbols) of the room descrption area 
	private static final int MAX_DESCRIPTION_AREA_WIDTH_SYMB = 30;
	//The delimiter between the field names and the field values
	private static final String FIELD_NAME_VS_VALUE_DELIMITER = ":";

	private FlexTable bodyGrid;
	private int row = 0;
	private int column = 0;

	//The chat room data to be displayed 
	private final ChatRoomData roomDataObject;
	//The chat room image icon URL
	private final String roomImageURL;
	//The chat room type string
	private final String roomTypeName;
	//The instance of the rooms manager
	private final RoomsManagerUI roomsManager;
	
	/**
	 * Allows to open a room's view popup with the given room's data 
	 * @param roomData the data we want to show in the room's view popup
	 * @param opener the ui element relative to which the popup will be opened
	 * @param showEnterButton if true then we show the enter-room button otherwise not
	 * @param roomsManager the instance of the rooms manager UI
	 */
	public static void openRoomViewPopup( final ChatRoomData roomData, final Widget opener,
										  final boolean showEnterButton, final RoomsManagerUI roomsManager ) {
		//Create the popup panel object
		RoomInfoPopupPanel panel = new RoomInfoPopupPanel( roomData, ChatRoomData.getRoomTypeImageURL(roomData),
														   ChatRoomData.getRoomType(roomData), showEnterButton,
														   roomsManager );
		//Show the pop-up panel at some proper position, in such a way that
		//it does not go outside the window area, also make the popup modal
		panel.setPopupPositionAndShow( panel.new InfoPopUpPositionCallback( opener ) );
	}
	
	/**
	 * Tries to retrieve the room's data and then opens the panel view of the room
	 * @param roomId the id of the room we want to open
	 * @param opener the ui element relative to which the popup will be opened
	 * @param roomsManager the instance of the rooms manager UI
	 */
	public static void openRoomViewPopup(final int roomId, final Widget opener, final RoomsManagerUI roomsManager ) {
		//Try to get the required data from the local storage, if it is not there, call the server
		ChatRoomData roomData = roomsManager.getActiveRoomData(roomId);
		if( roomData != null ) {
			openRoomViewPopup( roomData, opener, true, roomsManager );
		} else {
			//Ensure lazy loading
			(new SplitLoad(){
				@Override
				public void execute() {
					//Construct the call back object
					AsyncCallback<ChatRoomData> callback = new AsyncCallback<ChatRoomData>() {
						public void onSuccess(ChatRoomData result) {
							openRoomViewPopup( result, opener, true, roomsManager );
						}
						public void onFailure(final Throwable caught) {
							//Report the error
							(new SplitLoad( true ) {
								@Override
								public void execute() {
									ErrorMessagesDialogUI.openErrorDialog( caught );
								}
							}).loadAndExecute();
						}
					};
					//Get the room's data from the server and open the popup
					RoomManagerAsync roomsManagerAsynch = RPCAccessManager.getRoomManagerAsync();
					roomsManagerAsynch.getRoomData( SiteManager.getUserID(), SiteManager.getUserSessionId(), roomId, callback);
				}
			}).loadAndExecute();
		}
	}
	
	/**
	 * The constructor that accepts a pre-retrieved room data
	 * @param roomDataObject the chat room data to be displayed
	 * @param roomImageURL the chat room image icon URL
	 * @param roomTypeName the chat room type string
	 * @param showEnterButton if true then we show the enter-room button otherwise not
	 * @param roomsManager the instance of the rooms manager UI
	 */
	private RoomInfoPopupPanel(final ChatRoomData roomDataObject, final String roomImageURL,
								final String roomTypeName, final boolean showEnterButton,
								final RoomsManagerUI roomsManager ){
		//Call the super constructor
		super(true,true);
		
		//Store the data
		this.roomDataObject = roomDataObject;
		this.roomImageURL = roomImageURL;
		this.roomTypeName = roomTypeName;
		this.roomsManager = roomsManager;
		
		//Populate the panel with elements
		populatePanel( showEnterButton );
	}
	
	private void populatePanel(final boolean showEnterButton) {
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setSize( "100%", "100%");
		this.addContentWidget( verticalPanel );
		
		//Add the title bodyGrid
		FlexTable titleGrid = new FlexTable();
		titleGrid.setSize( "100%", "100%");
		row = 0; column = 0;
		
		//Add the title: status image, room name, enter button
		//Add image and the room name and the enter button
		HorizontalPanel horizontalNamePanel = new HorizontalPanel();
		horizontalNamePanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		Image roomImage = new Image( roomImageURL );
		roomImage.setStyleName( CommonResourcesContainer.CHAT_ROOM_POPUP_IMAGE_STYLE_NAME );
		horizontalNamePanel.add( roomImage );
		//Add some spacing
		horizontalNamePanel.add( new HTML("&nbsp;") );
		
		Label titleLabel = addLabel( horizontalNamePanel, "", CommonResourcesContainer.CHAT_ROOM_POPUP_TITLE_STYLE_NAME );
		InterfaceUtils.populateRoomNameLabel(roomDataObject, true, titleLabel);
		
		//Add the following things 
		titleGrid.insertRow( row );
		titleGrid.insertCell( row, column );
		titleGrid.setWidget( row, column, horizontalNamePanel );
		titleGrid.getCellFormatter().setHorizontalAlignment( row, column++, HasHorizontalAlignment.ALIGN_LEFT );
		
		//Add the enter button if needed and the room is not expired 
		//and the room is in the list of active chat rooms
		final boolean isRoomActive = ( !roomDataObject.isExpired() ) &&
		( roomsManager.getActiveRoomData( roomDataObject.getRoomID() ) != null );
		if( showEnterButton && isRoomActive ) {
			//Add the enter button
			Button enterButton = new Button( titlesI18N.roomEnterButton() );
			enterButton.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent e){
					//Hide this popup
					hide();
					//Do the room opening
					roomsManager.openNewRoom( roomDataObject.getRoomID() );
				}
			});
			enterButton.setStyleName( CommonResourcesContainer.CHAT_ROOM_POPUP_BUTTON_STYLE_NAME );
			titleGrid.insertCell( row, column );
			titleGrid.setWidget( row, column, enterButton );
			titleGrid.getCellFormatter().setHorizontalAlignment( row, column, HasHorizontalAlignment.ALIGN_RIGHT );
		}
		
		verticalPanel.add(titleGrid);
		
		//Add the main bodyGrid
		bodyGrid = new FlexTable();
		bodyGrid.setSize( "100%", "100%");
		row = 0; column = 0;
		
		//The room owner login name
		addFieldLabel( titlesI18N.roomOwnerFieldName(), true, false, false, null );
		ClickHandler listener = new ClickHandler(){
			public void onClick(ClickEvent e){
				//Just in case the room's data is incomplete
				if( roomDataObject.getOwnerID() != UserData.UNKNOWN_UID ) {
					//Ensure lazy loading
					final SplitLoad executor = new SplitLoad( true ) {
						@Override
						public void execute() {
							//Hide this popup
							hide();
							//Show the user profile dialog
							ViewUserProfileDialogUI profileView = new ViewUserProfileDialogUI( roomDataObject.getOwnerID(),
																								roomDataObject.getOwnerName(),
																								null, false );
							profileView.show();
							profileView.center();
						}
					};
					executor.loadAndExecute();
				}
			}
		};
		addFieldLabel( roomDataObject.getOwnerName(), false, false, false, listener );
		
		//The room Description
		addFieldLabel( titlesI18N.roomDescFieldName(), true, true, false, null );
		addFieldLabel( ChatRoomData.getRoomDescription( roomDataObject ), false, false, true, null );
	
		//Is the room Private/Public/Protected?
		addFieldLabel( titlesI18N.accessOrTypeColumnTitle(), true, false, false, null );
		addFieldLabel( roomTypeName, false, false, false, null );
	
		//Is the room permanent or temporary?
		if( roomDataObject.isPermanent() ){
			//Is the room Private/Public/Protected?
			addFieldLabel( titlesI18N.roomTypeFieldName(), true, false, false, null );
			addFieldLabel( titlesI18N.roomPermanentType(), false, false, false, null );
		} else {
			//Is the room Private/Public/Protected?
			addFieldLabel( titlesI18N.roomTypeFieldName(), true, false, false, null );
			addFieldLabel( titlesI18N.roomTemporaryType(), false, false, false, null );
			//Add the room closing date/time
			addFieldLabel( titlesI18N.roomClosingTimeFieldName(), true, false, false, null );
			if( isRoomActive  ) {
				final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat( PredefinedFormat.DATE_TIME_SHORT );
				addFieldLabel( dateTimeFormat.format( roomDataObject.getExpirationDate() ), false, false, false, null );
			} else {
				Label field = (Label) addFieldLabel( titlesI18N.alreadyClosedRoomFieldValue() , false, false, false, null );
				field.setStyleName( CommonResourcesContainer.INFO_POPUP_VALUE_IMP_STYLE_NAME );
			}
		}
		//Add room manager for the admin or the room's owner
		final int forUserID = SiteManager.getUserID();
		if( ( SiteManager.getUserProfileType() == MainUserData.ADMIN_USER_TYPE ) ||
			( forUserID == roomDataObject.getOwnerID() ) ) {
			addFieldLabel( titlesI18N.managementTitle(), true, false, false, null );
			addFieldLabel( titlesI18N.editTitle(), false, false, false, new ClickHandler(){
				public void onClick(ClickEvent e) {
					//Ensure lazy loading
					( new SplitLoad( true ) {
						@Override
						public void execute() {
							//Hide this popup
							hide();
							//Open the room manager dialog
							RoomDialogUI roomManager = new RoomDialogUI( false, roomDataObject, null, forUserID,
																		 SiteManager.getUserLoginName(), roomsManager );
							roomManager.show();
							roomManager.center();
						}
					}).loadAndExecute();
				}
			} );
		}
		verticalPanel.add( bodyGrid );
	}
	
	/**
	 * Add a field name/field value label to the bodyGrid
	 * @param text the text to insert
	 * @param isName true if the text is the name of the field
	 * @partam isAlognNameTop true if we need to align the name to the top of the cell
	 * this parameter only works if isName == true
	 * @param isLongValue true if it is a long value, then is it
	 * inserted into TextArea.of width 16 characters
	 * @param listener the click listener for the hyperlink (user name)
	 * @return the created widget that is the name of value field
	 */
	private Widget addFieldLabel(final String text, final boolean isName,
								final boolean isAlognNameTop,
								final boolean isLongValue,
								final ClickHandler listener) {
		Widget element = null;
		if( isName ){
			bodyGrid.insertRow( row );
			bodyGrid.insertCell( row, column );
			Label lab  = new Label( text + FIELD_NAME_VS_VALUE_DELIMITER );
			lab.setWordWrap(false);
			lab.setStyleName( CommonResourcesContainer.INFO_POPUP_FIELD_NAME_STYLE_NAME );
			bodyGrid.setWidget( row, column, lab );
			if( isAlognNameTop ) {
				bodyGrid.getCellFormatter().setVerticalAlignment( row, column, HasVerticalAlignment.ALIGN_TOP);
			}
			column += 1;
			//Assign the return value
			element = lab;
		} else {
			//The long value is placed into a disabled textarea
			if( isLongValue ){
				//We will insert the long data on the new row, but place an empty cell for the old position
				bodyGrid.insertCell( row, column );
				bodyGrid.insertRow( ++row ); column = 0;
				bodyGrid.insertCell( row, column++ );
				bodyGrid.insertCell( row, column ); column = 0;
				TextArea area = new TextArea();
				//Set the line length in characters
				area.setCharacterWidth( MAX_DESCRIPTION_AREA_WIDTH_SYMB );
				area.setReadOnly( true );
				final String roomDescText = StringUtils.formatTextWidth( MAX_DESCRIPTION_AREA_WIDTH_SYMB, text, true );
				area.setText( roomDescText );
				//Set the number of visible lines
				area.setVisibleLines( StringUtils.countLines( roomDescText ) );
				element = area;
				bodyGrid.getFlexCellFormatter().setColSpan( row , column, 2);
			} else {
				bodyGrid.insertCell( row, column );
				element = new Label( text );
			}
			bodyGrid.getCellFormatter().setHorizontalAlignment( row, column, HasHorizontalAlignment.ALIGN_RIGHT);
			if( (listener != null) && ( element instanceof Label ) ){
				Label label = (Label) element;
				label.setWordWrap(false);
				label.addClickHandler( listener );
				label.setStyleName( CommonResourcesContainer.INFO_POPUP_VALUE_LINK_STYLE_NAME );
			} else {
				element.setStyleName( CommonResourcesContainer.INFO_POPUP_VALUE_STYLE_NAME );
			}
			bodyGrid.setWidget( row, column, element );
			row +=1;
			column = 0;
		}
		return element;
	}
}
