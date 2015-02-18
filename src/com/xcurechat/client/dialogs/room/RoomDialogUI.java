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
package com.xcurechat.client.dialogs.room;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ChangeEvent;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.chat.RoomsManagerUI;
import com.xcurechat.client.dialogs.ActionGridDialog;
import com.xcurechat.client.dialogs.PagedActionGridDialog;
import com.xcurechat.client.dialogs.room.RoomUsersManagerDialogUI;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;
import com.xcurechat.client.dialogs.system.messages.InfoMessageDialogUI;

import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.Button;
import com.xcurechat.client.utils.widgets.ServerCommStatusPanel;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.TextBaseTranslitAndProgressBar;

import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.data.MainUserData;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.RoomManagerAsync;
import com.xcurechat.client.rpc.exceptions.SiteException;

/**
 * @author zapreevis
 * The dialog that will manage creation and editing of the rooms.
 */
public class RoomDialogUI extends ActionGridDialog {
	
	//The parameters for the room description TextArea
	private static final int ROOM_DESCRIPTION_ONE_LINE_LENGTH = 40;
	private static final int ROOM_DESCRIPTION_VISIBLE_LINES_LENGTH = 3; 
	
	/**
	 * @author zapreevis
	 * This class allows to manage the enabling/disabling fields login for
	 * the Name and Description fields of the room.
	 */
	private class MainCheckBox extends CheckBox {
		@Override
		public void setValue(Boolean value) {
			//Call the super class method
			super.setValue(value);
			
			//Process the input
			processInput();
		}
		
		/**
		 * If this is the main room then one can not edit the room's name and description
		 */
		public void processInput() {
			if( this.getValue() ) {
				//Disable the fields to prevent editing
				roomNameTextBox.setEnabled(false);
				roomDescription.setEnabled(false);
				//Reset the text to the default
				ChatRoomData localRoomData =  new ChatRoomData();
				localRoomData.setMain(true);
				roomNameTextBox.setText( ChatRoomData.getRoomName(localRoomData) );
				roomDescription.setText( ChatRoomData.getRoomDescription(localRoomData) );
				
				//The main room can only be public
				setRoomTypeListBox( ChatRoomData.PUBLIC_ROOM_TYPE );
				roomTypeListBox.setEnabled( false );
			} else {
				//If there is room data then get the values from it
				if( roomData != null ) {
					roomNameTextBox.setText( roomData.getRoomName() );
					roomDescription.setText( roomData.getRoomDesc() );
				} else {
					roomNameTextBox.setText( "" );
					roomDescription.setText( "" );
				}
				//Enable the fields to allow editing
				roomNameTextBox.setEnabled(true);
				roomDescription.setEnabled(true);
				roomTypeListBox.setEnabled( true );
			}
		}
	}
	
	//This variable is true if we work create a new room
	//it is fauls if we work in editing an existing room
	private boolean isNew;
	
	//The chat room data object as provided by the constructor
	private ChatRoomData roomData;
	
	//The room name TextBox
	private TextBox roomNameTextBox = new TextBox();
	//The list box for selecting the room type
	private ListBox roomTypeListBox = new ListBox();
	//The list box for selecting the room life time
	private ListBox roomDurationListBox = new ListBox();
	//The room description TextArea
	private TextArea roomDescription = new TextArea();
	//The room permanent marker CheckBox
	private CheckBox permanentRoomCheckBox = new CheckBox();
	//The room main marker CheckBox
	private MainCheckBox mainRoomCheckBox = new MainCheckBox();
	//The room expiration date time label
	private Label roomExpirationLabel = new Label();
	//The ownder name label
	private Label roomOwnerNameLabel = new Label();
	//The room-users editing button
	private Button usersButton = new Button();
	//The edit users field label, we need to store it here because
	//we change it's styles when we want to tell people that they
	//need to add users to protected and private rooms
	private Label editRoomUsers = null;
	
    //The id and login of the user we browse data for
    private final String forUserLoginName;
    private final int forUserID;
    //The instance of the rooms manager
    private final RoomsManagerUI roomsManager;
    
    //True if the interface is used by the administrator
    private final boolean isAdmin;
	
	/**
	 * Tries to retrieve the room's data and then opens the panel view of the room
	 * @param roomId the id of the room we want to open
	 * @param parentDialog the dialog we open the room's editing dialog from
	 * @param forUserID the user ID we create/edit room for
	 * @param forUserLoginName the user login name we create/edit room for
	 * @param progressBarUI the progress bar to use for indicating the loading of the room data
	 * @param roomsManager the instance of the rooms manager
	 */
	public static void openRoomEditDialog(  final int roomId, final DialogBox parentDialog,
											final int forUserID, final String forUserLoginName,
											final ServerCommStatusPanel progressBarUI,
											final RoomsManagerUI roomsManager ){
		//Ensure lazy loading
		(new SplitLoad( true ){
			@Override
			public void execute() {
				CommStatusAsyncCallback<ChatRoomData> callback = new CommStatusAsyncCallback<ChatRoomData>(progressBarUI) {
					public void onSuccessAct( ChatRoomData result) {
						RoomDialogUI roomEditDialog = new RoomDialogUI( false, result, parentDialog, forUserID,
																		forUserLoginName, roomsManager);
						roomEditDialog.show();
						roomEditDialog.center();
					}
					public void onFailureAct(final Throwable caught) {
						(new SplitLoad( true ) {
							@Override
							public void execute() {
								//Report the error
								ErrorMessagesDialogUI.openErrorDialog( caught );
							}
						}).loadAndExecute();
					}
				};
				
				//Get the room's data from the server and open the room editing dialog
				RoomManagerAsync roomsManagerAsync = RPCAccessManager.getRoomManagerAsync();
				roomsManagerAsync.getRoomData( SiteManager.getUserID(), SiteManager.getUserSessionId(), roomId, callback);
			}
		}).loadAndExecute();
	}
	
	/**
	 * This constructor allows to initialize the dialog
	 * to work with a new room or with an existing room.
	 * @param isNew if true then we want to create a new room.
	 * @param roomData the room data in case isNew == false
	 * @param forUserID the user ID we create/edit room for
	 * @param forUserLoginName the user login name we create/edit room for
	 * @param roomsManager the instane of the rooms manager
	 */
	public RoomDialogUI(final boolean isNew, ChatRoomData roomData,
						final DialogBox parentDialog, final int forUserID,
						final String forUserLoginName, final RoomsManagerUI roomsManager ) {
		super( true, false,true, parentDialog );
		
		//Store the data
		this.forUserID = forUserID;
		this.forUserLoginName = forUserLoginName;
		this.roomsManager = roomsManager;
		
		//Store the values
		this.isNew = isNew;
		this.roomData = roomData;
		
		//Set the amin marker
		isAdmin = (SiteManager.getUserProfileType() == MainUserData.ADMIN_USER_TYPE);
		
		//Set title and style
		updateTitle();
		this.setStyleName( CommonResourcesContainer.USER_DIALOG_STYLE_NAME );
		
		//Fill dialog with data
		populateDialog();
	}

	private void updateTitle() {
		if( isNew ) {
			this.setText(titlesI18N.createRoomDialogTitle() );
		} else {
			this.setText(titlesI18N.updateRoomDialogTitle() );
		}
	}
	
	@Override
	protected void populateDialog() {
		if( isAdmin ){
			addNewGrid( 8, false, "", false);
		} else {
			addNewGrid( 9, false, "", false);
		}
		//Add room name TextBox
		Label roomNameLabel = InterfaceUtils.getNewFieldLabel( titlesI18N.roomNameFieldTitle(), true );
		addToGrid( FIRST_COLUMN_INDEX, roomNameLabel, false, false );
		roomNameTextBox.setEnabled(isNew);
		addToGrid( SECOND_COLUMN_INDEX, new TextBaseTranslitAndProgressBar(roomNameTextBox, ChatRoomData.MAX_ROOM_NAME_LENGTH ), false, true );

		//Add room owner Label
		Label roomOwnerLabel = InterfaceUtils.getNewFieldLabel( titlesI18N.roomOwnerFieldName(), false );
		roomOwnerNameLabel.setStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_STYLE_NAME );
		addToGrid( FIRST_COLUMN_INDEX, roomOwnerLabel, true, false );
		if( roomData == null ) {
			roomOwnerNameLabel.setText( SiteManager.getUserLoginName() );
		} else {
			roomOwnerNameLabel.setText( roomData.getOwnerName() );
		}
		addToGrid( SECOND_COLUMN_INDEX, roomOwnerNameLabel, false, false );
		
		//Add room expiration
		Label roomExpiresLabel = InterfaceUtils.getNewFieldLabel( titlesI18N.roomClosingTimeFieldName(), false );
		addToGrid( FIRST_COLUMN_INDEX, roomExpiresLabel, true, false );
		roomExpirationLabel.setText( titlesI18N.unknownTextValue() );
		roomExpirationLabel.setStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_STYLE_NAME );
		addToGrid( SECOND_COLUMN_INDEX, roomExpirationLabel, false, false );

		//Add room duration ListBox
		Label roomDurationLabel = InterfaceUtils.getNewFieldLabel( titlesI18N.roomDurationFieldTitle(), true );
		addToGrid( FIRST_COLUMN_INDEX, roomDurationLabel, true, false );
		String name = titlesI18N.undefinedTextValue();
		String value = ""+ChatRoomData.UNKNOWN_HOURS_DURATION;
		roomDurationListBox.addItem( name , value );
		if( !isNew ) {
			//If we are not creating a new room then we can reset the duration/close the room
			name = titlesI18N.cleanDurationTextValue();
			value = ""+ChatRoomData.CLEAN_HOURS_DURATION;
			roomDurationListBox.addItem( name , value );
		}
		name = ChatRoomData.TWO_HOURS_DURATION + " " + titlesI18N.roomTimeDurationHours();
		value = ""+ChatRoomData.TWO_HOURS_DURATION;
		roomDurationListBox.addItem( name , value );
		name = ChatRoomData.FOUR_HOURS_DURATION + " " + titlesI18N.roomTimeDurationHours();
		value = ""+ChatRoomData.FOUR_HOURS_DURATION;
		roomDurationListBox.addItem( name , value );
		name = ChatRoomData.EIGHT_HOURS_DURATION + " " + titlesI18N.roomTimeDurationHourss();
		value = ""+ChatRoomData.EIGHT_HOURS_DURATION;
		roomDurationListBox.addItem( name , value );
		name = ChatRoomData.TWENTYFOUR_HOURS_DURATION + " " + titlesI18N.roomTimeDurationHours();
		value = ""+ChatRoomData.TWENTYFOUR_HOURS_DURATION;
		roomDurationListBox.addItem( name , value );
		roomDurationListBox.setVisibleItemCount(1);
		roomDurationListBox.setEnabled(isNew);
		addToGrid( SECOND_COLUMN_INDEX, roomDurationListBox, false, false );
		
		//Add room type/access ListBox
		Label roomTypeLabel = InterfaceUtils.getNewFieldLabel( titlesI18N.roomTypeFieldTitle(), false );
		addToGrid( FIRST_COLUMN_INDEX, roomTypeLabel, true, false );
		roomTypeListBox.addItem( titlesI18N.roomTypeNamePublic(), ""+ChatRoomData.PUBLIC_ROOM_TYPE );
		roomTypeListBox.addItem( titlesI18N.roomTypeNameProtected(), ""+ChatRoomData.PROTECTED_ROOM_TYPE );
		roomTypeListBox.addItem( titlesI18N.roomTypeNamePrivate(), ""+ChatRoomData.PRIVATE_ROOM_TYPE );
		roomTypeListBox.setVisibleItemCount(1);
		roomTypeListBox.setEnabled(isNew);
		roomTypeListBox.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent e){
				if( !isNew ) {
					if( roomTypeListBox.getValue( roomTypeListBox.getSelectedIndex() ).equals(""+ChatRoomData.PUBLIC_ROOM_TYPE) ){
						editRoomUsers.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
						if( isAdmin ) {
							//The admin can manage users of any room type: public, private or protected
							usersButton.setEnabled(true);
						} else {
							usersButton.setEnabled(false);
						}
					} else {
						editRoomUsers.setStyleName( CommonResourcesContainer.USER_DIALOG_COMPULSORY_FIELD_STYLE );
						usersButton.setEnabled(true);
					}
				}
			}
		});
		addToGrid( SECOND_COLUMN_INDEX, roomTypeListBox, false, false );
		
		//Add the users management button
		editRoomUsers = InterfaceUtils.getNewFieldLabel( titlesI18N.usersTitle(), false );
		addToGrid( FIRST_COLUMN_INDEX, editRoomUsers, true, false );
		//just disable the button in the beginning
		usersButton.setEnabled(false);
		usersButton.setText( titlesI18N.editTitle() );
		usersButton.setStyleName( CommonResourcesContainer.ROOM_DIALOG_ACTION_BUTTON_STYLE );
		usersButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) {
				//Ensure lazy loading
				( new SplitLoad( true ) {
					@Override
					public void execute() {
						RoomUsersManagerDialogUI roomUsersManager = new RoomUsersManagerDialogUI( roomData, thisDialog );
						roomUsersManager.show();
						roomUsersManager.center();
					}
				}).loadAndExecute();
			}
		});
		addToGrid( SECOND_COLUMN_INDEX, usersButton, false, false );
		
		//Add admin related fields
		if( isAdmin ){
			//Add permanent room marker RadioBoxes
			HorizontalPanel extraPanel = new HorizontalPanel();
			extraPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
			extraPanel.setSize("100%", "100%");
			permanentRoomCheckBox.setEnabled(isNew);
			permanentRoomCheckBox.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
			permanentRoomCheckBox.setText( titlesI18N.roomPermanentFieldTitle() );
			permanentRoomCheckBox.addClickHandler( new ClickHandler(){
				public void onClick(ClickEvent e){
					//Simply, if the permanent box is checked then 
					//we can add that the room can be the main one.
					if( permanentRoomCheckBox.isEnabled() ){
						if( permanentRoomCheckBox.getValue() ) {
							mainRoomCheckBox.setEnabled( true );
							roomDurationListBox.setEnabled( false );
							//There is not room duration for the permanent room.
							//The default index is zero.
							setRoomDurationListBox( ChatRoomData.UNKNOWN_HOURS_DURATION );
							setRoomExpirationLabelValue( false );
						} else {
							//Only if the room is marked as main we reset it to no-main
							//Because in this case the room's name is reset, and we do
							//not want this reset to happen for a non-main room.
							if( mainRoomCheckBox.getValue() ) {
								mainRoomCheckBox.setValue( false );
							}
							mainRoomCheckBox.setEnabled( false );
							roomDurationListBox.setEnabled( true );
							setRoomExpirationLabelValue( true );
						}
					}
				}
			});
			extraPanel.add( permanentRoomCheckBox );
			
			//Add main room marker check box
			mainRoomCheckBox.setEnabled(false);
			mainRoomCheckBox.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
			mainRoomCheckBox.setText( titlesI18N.roomMainFieldTitle() );
			mainRoomCheckBox.addClickHandler( new ClickHandler() {
				public void onClick(ClickEvent e) {
					mainRoomCheckBox.processInput();
				}
			});
			
			extraPanel.add( mainRoomCheckBox ); 
			
			addToGrid( getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, extraPanel, true, false );
		}
		
		//Add room description Label
		Label roomDescriptionLabel = InterfaceUtils.getNewFieldLabel( titlesI18N.roomDescriptionFieldTitle(), false );
		addToGrid( getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, roomDescriptionLabel, true, false );
		
		//Add the room description TextArea
		//Set the line length in characters
		roomDescription.setCharacterWidth( ROOM_DESCRIPTION_ONE_LINE_LENGTH );
		//Set 4 lines to be visible
		roomDescription.setVisibleLines( ROOM_DESCRIPTION_VISIBLE_LINES_LENGTH );
		roomDescription.setEnabled( isNew );
		addToGrid( getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, new TextBaseTranslitAndProgressBar(roomDescription, ChatRoomData.MAX_ROOM_DESC_LENGTH ), true, false );
		
		//Add the main Cancel/Save buttons to the grid If we are
		//creating a new room, then the Save button is enabled.
		addNewGrid( 1, false, "", false);
		addGridActionElements(true, isNew, true, false);
		
		//If the room data was set in the constructor, then we
		//will set the room dialog field values here
		processRoomData();
		
		//Enable the controls
		setEnabledElements(true, true, true);
	}
	
	/**
	 * Allows to set the room type in the ListBox based on the provided type
	 * @param providedRoomType the provided room type
	 */
	private void setRoomTypeListBox( final int providedRoomType ){
		for( int index = 0; index < roomTypeListBox.getItemCount(); index ++) {
			final int roomType = Integer.parseInt( roomTypeListBox.getValue( index ) );
			if( roomType == providedRoomType ){
				roomTypeListBox.setSelectedIndex(index);
				if( roomType == ChatRoomData.PUBLIC_ROOM_TYPE ) {
					editRoomUsers.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
				} else {
					editRoomUsers.setStyleName( CommonResourcesContainer.USER_DIALOG_COMPULSORY_FIELD_STYLE );
				}
				break;
			}
		}
	}
	
	/**
	 * Set the room duration ListBox item based on the duration in hours
	 * @param roomDurationHours the duration in hours
	 */
	private void setRoomDurationListBox(final int roomDurationHours){
		for( int index = 0; index < roomDurationListBox.getItemCount(); index ++) {
			final int roomDuration = Integer.parseInt( roomDurationListBox.getValue( index ) );
			if( roomDuration == roomDurationHours ){
				roomDurationListBox.setSelectedIndex(index);
				break;
			}
		}
	}
	
	private void processRoomData() {
		if( roomData != null ){
			if ( ! isAdmin ) {
				//In case the admin has changed these to be true, we 
				//reset them here to follow the normal cource of actions
				roomData.setPermanent(false);
				roomData.setMain(false);
			}
			//Set the room's name and description
			if( roomData.isMain() ) {
				//If this is the main room then we can not change the room's name
				roomNameTextBox.setEnabled(false);
				roomDescription.setEnabled(false);
			}
			roomNameTextBox.setText( ChatRoomData.getRoomName(roomData) );
			roomDescription.setText( ChatRoomData.getRoomDescription(roomData) );
			
			//Set the room's type
			setRoomTypeListBox( roomData.getRoomType() );
			
			//Set the room's life time
			setRoomDurationListBox( roomData.getRoomDurationTimeHours() );
			
			//Set the permanence marker
			permanentRoomCheckBox.setValue(roomData.isPermanent());
			//Set the main marker
			mainRoomCheckBox.setValue( roomData.isMain() );
			//Set room expiration data
			setRoomExpirationLabelValue( true );
		}
	}

	/**
	 * Set the room expration time label based on the available room data
	 * @param showDate if true we want to show the real date if possible,
	 * if false we want to show that the expiration time is unknown 
	 */
	private void setRoomExpirationLabelValue( final boolean showDate ) {
		if( showDate && ( roomData != null ) && (roomData.getExpirationDate() != null ) && !roomData.isExpired() ) {
			final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat( PredefinedFormat.DATE_TIME_SHORT );
			roomExpirationLabel.setText( dateTimeFormat.format( roomData.getExpirationDate() ) );
		} else {
			roomExpirationLabel.setText( titlesI18N.unknownTextValue() );
		}
	}
	
	/**
	 * This method retrieves room data from the dialog fields
	 * @throws SiteException is some room data is incorrect
	 */
	private void getTheChatRoomData() throws SiteException {
		//The room name
		final String roomName = roomNameTextBox.getText(); 
		//The room description
		final String roomDesc = roomDescription.getText();
		//The room permanent marker
		final boolean isPermanent = permanentRoomCheckBox.getValue();
		//The room main marker
		final boolean isMain = mainRoomCheckBox.getValue();
		//The room type
		final int roomType = Integer.parseInt( roomTypeListBox.getValue( roomTypeListBox.getSelectedIndex() ) );
		//The the room life time
		final int roomDuration = Integer.parseInt( roomDurationListBox.getValue( roomDurationListBox.getSelectedIndex() ) );
		
		if( roomData == null ) {
			roomData = new ChatRoomData();
			//The owner's login and id are set only in case of
			//creating the new room, otherwise they are already known
			roomData.setOwnerName(this.forUserLoginName);
			roomData.setOwnerID(this.forUserID);
		}
		
		roomData.setRoomName(roomName);
		roomData.setRoomDesc(roomDesc);
		roomData.setRoomType(roomType);
		roomData.setPermanent(isPermanent);
		roomData.setMain(isMain);
		roomData.setRoomDurationTimeHours( roomDuration );
		
		//Validate the object
		roomData.validate(isNew);
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
		return titlesI18N.saveButton();
	}
	
	@Override
	protected void actionLeftButton() {
		hide();
	}
	
	/**
	 * Enable/Disable dialog buttons and other elements.
	 * The care is taken that is the room is main then we
	 * do not enable/disable it's name and description.
	 * The latter is because the fields are disabled by
	 * default. 
	 */
	private void setEnabledElements(final boolean enableLeft, final boolean enableRight, final boolean other ){
		Scheduler.get().scheduleDeferred( new ScheduledCommand(){
			public void execute(){
				setLeftEnabled( enableLeft );
				setRightEnabled( enableRight );
				//Check if it is the main room because then we do not need to enable 
				//the name and descprition fields, by default they should be disabled
				if( ( ( roomData == null ) || !roomData.isMain() ) && !mainRoomCheckBox.getValue() ) {
					//The room name TextBox
					roomNameTextBox.setEnabled(other);
					//The room description TextArea
					roomDescription.setEnabled(other);
				}
				
				//The room permanent marker CheckBox
				//The room main marker CheckBox
				//The list box for selecting the room life time
				if( other ) {
					//The main room can not be anything but public, so
					//we should not be able to change the room type.
					roomTypeListBox.setEnabled( !mainRoomCheckBox.getValue() );
					
					permanentRoomCheckBox.setEnabled(true);
					if( permanentRoomCheckBox.getValue() ) {
						//If it is the permanent room then since
						//we enable the controls we need to enable
						//the main marker CheckBox
						mainRoomCheckBox.setEnabled(true);
					} else {
						//If it is not a permanent room then since
						//we enable the controls we need to enable
						//the room's life time ListBox
						roomDurationListBox.setEnabled(true);
					}
					//If the room is new then we do not enable the button
					if( !isNew ) {
						//Check if the room is not public, then enable the users button
						if( ( roomData.getRoomType() != ChatRoomData.PUBLIC_ROOM_TYPE) || isAdmin ) {
							//NOTE: The admin can manage users of any room type: public, private or protected
							usersButton.setEnabled(true);
						}
					}
				} else {
					permanentRoomCheckBox.setEnabled(false);
					mainRoomCheckBox.setEnabled(false);
					roomDurationListBox.setEnabled(false);
					roomTypeListBox.setEnabled(false);
					usersButton.setEnabled(false);
				}
			}
		});
	}
	
	@Override
	protected void actionRightButton() {
		try{
			//Create or fill in the ChatRoomData from the dialog fields
			getTheChatRoomData();
			
			//Disable the dialog elements before the request
			setEnabledElements(false, false, false);
			
			//Ensure lazy loading
			(new SplitLoad( true ){
				@Override
				public void execute() {
					//Contact to the server
					if( isNew ) {
						//Create a new room on the server
						CommStatusAsyncCallback<ChatRoomData> callback = new CommStatusAsyncCallback<ChatRoomData>(progressBarUI) {
							public void onSuccessAct(ChatRoomData result) {
								//Save the data
								roomData = result;
								//Mark that this is not longer a new room dilog
								isNew = false;
								//Update the title.
								updateTitle();
								//Set the room data from the returned object
								processRoomData();
								//Update the parent dialog table;
								if( ( parentDialog != null ) && ( parentDialog instanceof PagedActionGridDialog<?> ) ) {
									( (PagedActionGridDialog<?>) parentDialog).updateActualData();
								}
								//Update the room's trees and the opened room's data
								roomsManager.afterLocalRoomAddUpdate(roomData);
								//If we have created a non-public room then show the info message
								if( !roomData.isPublic() ) {
									//Highlight the room-users editing field
									editRoomUsers.setStyleName( CommonResourcesContainer.USER_DIALOG_COMPULSORY_FIELD_STYLE );
									//Show a message about adding users to a new non-public rooms
									(new SplitLoad( true ) {
										@Override
										public void execute() {
											InfoMessageDialogUI.openInfoDialog( I18NManager.getInfoMessages().roomCreatedAddUsers());
										}
									}).loadAndExecute();
									//Enable the buttons
									setEnabledElements(true,true,true);
								} else {
									//A public room was created, we just close the dialog
									hide();
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
								//Use the recovery method
								recover();
							}
						};
						RoomManagerAsync roomManagerObject = RPCAccessManager.getRoomManagerAsync();
						roomManagerObject.create(SiteManager.getUserID(), SiteManager.getUserSessionId(), roomData, callback);
					} else {
						//Update an old room data on the server
						CommStatusAsyncCallback<ChatRoomData> callback = new CommStatusAsyncCallback<ChatRoomData>(progressBarUI) {
							public void onSuccessAct(ChatRoomData result) {
								//Save the data
								roomData = result;
								//Update the parent dialog table;
								if( ( parentDialog != null ) && ( parentDialog instanceof PagedActionGridDialog<?> ) ) {
									( (PagedActionGridDialog<?>) parentDialog).updateActualData();
								}
								//Update the room's trees and the opened room's data
								roomsManager.afterLocalRoomAddUpdate(roomData);
								//Hide the dialog window
								hide();
							}
							public void onFailureAct(final Throwable caught) {
								(new SplitLoad( true ) {
									@Override
									public void execute() {
										//Report the error
										ErrorMessagesDialogUI.openErrorDialog( caught );
									}
								}).loadAndExecute();
								//Use the recovery method
								recover();
							}
						};
						RoomManagerAsync roomManagerObject = RPCAccessManager.getRoomManagerAsync();
						roomManagerObject.update(SiteManager.getUserID(), SiteManager.getUserSessionId(), roomData, callback);
					}
				}
				@Override
				public void recover() {
					setEnabledElements(true, true, true);
				}
			}).loadAndExecute();
		} catch ( final SiteException e ){
			(new SplitLoad( true ) {
				@Override
				public void execute() {
					//Report the error
					ErrorMessagesDialogUI.openErrorDialog( e );
				}
			}).loadAndExecute();
		}
	}

}
