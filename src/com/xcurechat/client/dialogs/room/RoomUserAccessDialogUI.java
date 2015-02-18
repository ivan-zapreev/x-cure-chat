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

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.data.RoomUserAccessData;

import com.xcurechat.client.dialogs.ActionGridDialog;
import com.xcurechat.client.dialogs.profile.ViewUserProfileDialogUI;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.RoomManagerAsync;
import com.xcurechat.client.rpc.exceptions.SiteException;

/**
 * @author zapreevis
 * The dialog that will manage creation and editing of the room users access rights.
 */
public class RoomUserAccessDialogUI extends ActionGridDialog {
	
	//This variable is true if we work create a new room access right
	//it is fauls if we work in editing an existing room access right
	private boolean isNew;
	
	//The chat room access data data object as provided by the constructor
	private RoomUserAccessData userAccess;
	//The chat room name
	private final String roomName;
	//The chat room ID
	private final int roomID;
	
	//The room name label
	private Label roomNameDataLabel = new Label();
	//The ownder name label
	private Label userLoginNameDataLabel = new Label();
	//The room expiration date time label
	private Label readAllExpirationLabel = new Label();
	//Is system marker CheckBox
	private CheckBox isSystemCheckBox = new CheckBox();
	//Is write marker CheckBox
	private CheckBox isWriteCheckBox = new CheckBox();
	//Is read marker CheckBox
	private CheckBox isReadCheckBox = new CheckBox();
	//Is read all marker CheckBox
	private CheckBox isReadAllCheckBox = new CheckBox();
	//The list box for selecting the read all access life time
	private ListBox readAllDurationListBox = new ListBox();
	
    //The id and login of the user we browse data for
    private final String forUserLoginName;
    private final int forUserID;
	
	/**
	 * This constructor allows to initialize the dialog
	 * to work with a new room or with an existing room.
	 * @param isNew if true then we want to create a new room.
	 * @param userAccess the room data in case isNew == false
	 * @param forUserID the user ID we create/edit user access rule for
	 * @param forUserLoginName the user login name we create/edit user access rule for
	 */
	public RoomUserAccessDialogUI(final boolean isNew, final RoomUserAccessData userAccess,
									final DialogBox parentDialog,
									final int forUserID, final String forUserLoginName,
									final int roomID, final String roomName ) {
		super( false, false, true, parentDialog );
		
		//Store the id and login of the user we manage rooms for
		this.forUserID = forUserID;
		this.forUserLoginName = forUserLoginName;
		
		//Store the values
		this.isNew = isNew;
		this.userAccess = userAccess;
		this.roomID = roomID;
		this.roomName = roomName;
		
		//Set title and style
		updateTitle();
		this.setStyleName( CommonResourcesContainer.USER_DIALOG_STYLE_NAME );
		
		//Fill dialog with data
		populateDialog();
	}

	private void updateTitle() {
		if( isNew ) {
			this.setText(titlesI18N.userRoomAccessDialogTitle() );
		} else {
			this.setText(titlesI18N.userRoomAccessDialogTitle() );
		}
	}
	
	@Override
	protected void populateDialog() {
		addNewGrid( 6, false, "", false);
		
		//Add user login name Label
		Label userNameAccesLabel = InterfaceUtils.getNewFieldLabel( titlesI18N.userFieldName(), false );
		addToGrid( FIRST_COLUMN_INDEX, userNameAccesLabel, false, false );
		final String userLoginName = ( userAccess == null ? forUserLoginName : userAccess.getUserLoginName() );
		final int userID = ( userAccess == null ? forUserID : userAccess.getUID() );
		userLoginNameDataLabel.setText( userLoginName );
		userLoginNameDataLabel.setStyleName( CommonResourcesContainer.DIALOG_LINK_BLUE_STYLE );
		userLoginNameDataLabel.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e){
				//Ensure lazy loading
				final SplitLoad executor = new SplitLoad( true ) {
					@Override
					public void execute() {
						//If it is a regular user then we only show the profile
						ViewUserProfileDialogUI userProfile = new ViewUserProfileDialogUI( userID, userLoginName,
																							thisDialog, false );
						userProfile.show();
						userProfile.center();
					}
				};
				executor.loadAndExecute();
			}
		});
		addToGrid( SECOND_COLUMN_INDEX, userLoginNameDataLabel, false, false );
		
		//Add room name TextBox
		Label roomNameLabel = InterfaceUtils.getNewFieldLabel( titlesI18N.roomNameFieldTitle(), false );
		addToGrid( FIRST_COLUMN_INDEX, roomNameLabel, true, false );
		roomNameDataLabel.setStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_STYLE_NAME );
		InterfaceUtils.populateRoomNameLabel( roomName, roomNameDataLabel);
		addToGrid( SECOND_COLUMN_INDEX, roomNameDataLabel, false, false );
		
		//Add room expiration
		Label readAllExpiresLabel = InterfaceUtils.getNewFieldLabel( titlesI18N.readAllExpiresFieldName(), false );
		addToGrid( FIRST_COLUMN_INDEX, readAllExpiresLabel, true, false );
		readAllExpirationLabel.setText( titlesI18N.unknownTextValue() );
		readAllExpirationLabel.setStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_STYLE_NAME );
		addToGrid( SECOND_COLUMN_INDEX, readAllExpirationLabel, false, false );

		//Add room duration ListBox
		Label readAllDurationLabel = InterfaceUtils.getNewFieldLabel( titlesI18N.roomDurationFieldTitle(), false );
		addToGrid( FIRST_COLUMN_INDEX, readAllDurationLabel, true, false );
		
		String name = titlesI18N.undefinedTextValue();
		String value = ""+ChatRoomData.UNKNOWN_HOURS_DURATION;
		readAllDurationListBox.addItem( name , value );
		
		name = titlesI18N.cleanDurationTextValue();
		value = ""+ChatRoomData.CLEAN_HOURS_DURATION;
		readAllDurationListBox.addItem( name , value );
		
		name = ChatRoomData.TWO_HOURS_DURATION + " " + titlesI18N.roomTimeDurationHours();
		value = ""+ChatRoomData.TWO_HOURS_DURATION;
		readAllDurationListBox.addItem( name , value );
		
		name = ChatRoomData.FOUR_HOURS_DURATION + " " + titlesI18N.roomTimeDurationHours();
		value = ""+ChatRoomData.FOUR_HOURS_DURATION;
		readAllDurationListBox.addItem( name , value );
		
		name = ChatRoomData.EIGHT_HOURS_DURATION + " " + titlesI18N.roomTimeDurationHourss();
		value = ""+ChatRoomData.EIGHT_HOURS_DURATION;
		readAllDurationListBox.addItem( name , value );
		
		name = ChatRoomData.TWENTYFOUR_HOURS_DURATION + " " + titlesI18N.roomTimeDurationHours();
		value = ""+ChatRoomData.TWENTYFOUR_HOURS_DURATION;
		readAllDurationListBox.addItem( name , value );
		readAllDurationListBox.setVisibleItemCount(1);
		
		addToGrid( SECOND_COLUMN_INDEX, readAllDurationListBox, false, false );

		//Add read/write room marker RadioBoxes
		VerticalPanel extraPanelRW = new VerticalPanel();
		extraPanelRW.setSize("100%", "100%");
		isReadCheckBox.setText(titlesI18N.readAccessFieldTitle());
		isReadCheckBox.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
		isReadCheckBox.addClickHandler( new ClickHandler(){
			public void onClick(ClickEvent e){
				//It does not make sence to write and read all if one can not read
				if( !isReadCheckBox.getValue() ) {
					isReadAllCheckBox.setValue( false );
					isReadAllCheckBox.setEnabled( false );
					readAllDurationListBox.setEnabled( false );
					isWriteCheckBox.setValue( false );
					isSystemCheckBox.setValue( false );
				}
			}
		});
		extraPanelRW.add( isReadCheckBox );
		isWriteCheckBox.addClickHandler( new ClickHandler(){
			public void onClick(ClickEvent e){
				//It does not make sence to have write access without read access
				//Also if there is a write access then this is not a system access
				if( isWriteCheckBox.getValue() ) {
					isReadCheckBox.setValue( true );
					//If we can write to the room then it can not be a system access
					isSystemCheckBox.setValue( false );
					isSystemCheckBox.setEnabled( false );
				} else {
					isSystemCheckBox.setEnabled( true );
				}
			}
		});
		isWriteCheckBox.setText( titlesI18N.writeAccessFieldTitle() );
		isWriteCheckBox.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );		
		extraPanelRW.add( isWriteCheckBox );
		addToGrid( FIRST_COLUMN_INDEX, extraPanelRW, true, false );

		//Add read all/system check boxes
		VerticalPanel extraPanelRAS = new VerticalPanel();
		extraPanelRAS.setSize("100%", "100%");
		isReadAllCheckBox.setText( titlesI18N.readAllAccessFieldTitle() );
		isReadAllCheckBox.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
		isReadAllCheckBox.addClickHandler( new ClickHandler(){
			public void onClick(ClickEvent e){
				//If the read all box is checked then we enable the read all access duration.
				readAllDurationListBox.setEnabled( isReadAllCheckBox.getValue() );
				//if one can read all (secret msgs) then he can just read normal msgs
				if( isReadAllCheckBox.getValue() ) {
					isReadCheckBox.setValue( true );
				} else {
					//If we can not read any messages then it is not a system access right
					if( ! isReadCheckBox.getValue() ) {
						isSystemCheckBox.setValue( false );
					}
				}
			}
		});
		extraPanelRAS.add( isReadAllCheckBox );
		isSystemCheckBox.setText( titlesI18N.systemAccessFieldTitle() );
		isSystemCheckBox.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
		isSystemCheckBox.addClickHandler( new ClickHandler(){
			public void onClick(ClickEvent e){
				//Enable the read all only if it is a system property
				if( isSystemCheckBox.getValue() ) {
					isReadCheckBox.setValue( true );
					readAllDurationListBox.setEnabled( true );
					isReadAllCheckBox.setValue( true );
					isReadAllCheckBox.setEnabled( true );
					//The system access right should not let one write to the room
					isWriteCheckBox.setValue( false );
					isWriteCheckBox.setEnabled( false );
				} else {
					readAllDurationListBox.setEnabled( false );
					isReadAllCheckBox.setValue( false );
					isReadAllCheckBox.setEnabled( false );
					isWriteCheckBox.setEnabled( true );
				}
			}
		});
		extraPanelRAS.add( isSystemCheckBox ); 
		addToGrid( SECOND_COLUMN_INDEX, extraPanelRAS, false, false );
		
		//Add the main Cancel/Save buttons to the grid If we are
		//creating a new room, then the Save button is enabled.
		addNewGrid( 1, false, "", false);
		addGridActionElements(true, false, true, false);
		
		//If the room data was set in the constructor, then we
		//will set the room dialog field values here
		processRoomData();
		
		//Enable the controls
		setEnabledElements(true, true, true);
	}
	
	private void processRoomData() {
		if( userAccess != null ){
			//Set the read all access life time
			for( int index = 0; index < readAllDurationListBox.getItemCount(); index ++) {
				final int roomDuration = Integer.parseInt( readAllDurationListBox.getValue( index ) );
				if( roomDuration == ChatRoomData.UNKNOWN_HOURS_DURATION ){
					readAllDurationListBox.setSelectedIndex(index);
					break;
				}
			}
			//Set the read marker
			isReadCheckBox.setValue( userAccess.isRead() );
			//Set the read all marker
			isReadAllCheckBox.setValue( userAccess.isReadAll() );
			//Set write marker
			isWriteCheckBox.setValue( userAccess.isWrite() );
			if( userAccess.isSystem() ) {
				//We can not write if this is a system access
				isWriteCheckBox.setEnabled( false ); 
			}
			//Set system marker
			isSystemCheckBox.setValue( userAccess.isSystem() );
			if( userAccess.isWrite() ) {
				//We can not have system access if we can write
				isSystemCheckBox.setEnabled( false );
			}
			//Set room expiration data
			if( userAccess.getReadAllExpires() != null ) {
				final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat( PredefinedFormat.DATE_TIME_SHORT );
				readAllExpirationLabel.setText( dateTimeFormat.format( userAccess.getReadAllExpires() ) );
			} else {
				readAllExpirationLabel.setText( titlesI18N.unknownTextValue() );
			}
		}
	}
	
	/**
	 * This method retrieves room access data from the dialog fields
	 * @throws SiteException is some access data is incorrect
	 */
	private void getTheUserRoomAccessData() throws SiteException {
		if( userAccess == null ) {
			//If we need to create a new room access data then we also need to set some extra fields 
			userAccess = new RoomUserAccessData();
			//The user login name
			userAccess.setUserLoginName( forUserLoginName );
			//The user ID name
			userAccess.setUID( forUserID );
			//The room ID
			userAccess.setRID( roomID );
		}
		//The room read marker
		userAccess.setRead( isReadCheckBox.getValue() );
		//The room read marker
		userAccess.setReadAll( isReadAllCheckBox.getValue() );
		//The room read marker
		userAccess.setWrite( isWriteCheckBox.getValue() );
		//The room read marker
		userAccess.setSystem( isSystemCheckBox.getValue() );
		//The the room's "read-all" duration
		final int readAllDurationHours = Integer.parseInt( readAllDurationListBox.getValue( readAllDurationListBox.getSelectedIndex() ) );
		userAccess.setReadAllDurationTimeHours( readAllDurationHours );
		
		//Validate the room access data
		userAccess.validate();
	}
	
	/**
	 * @return the left-button caption
	 */
	protected String getLeftButtonText(){
		return titlesI18N.cancelButton();
	}

	/**
	 * @return the right-button caption
	 */
	protected String getRightButtonText(){
		return titlesI18N.saveButton();
	}
	
	@Override
	protected void actionLeftButton() {
		hide();
	}
	
	/**
	 * Enable/Disable dialog buttons and other elements
	 */
	private void setEnabledElements(final boolean enableLeft, final boolean enableRight, final boolean other ){
		Scheduler.get().scheduleDeferred( new ScheduledCommand(){
			public void execute(){
				setLeftEnabled( enableLeft );
				setRightEnabled( enableRight );
				
				//The other room access dialog elements
				isReadCheckBox.setEnabled( other );
				isWriteCheckBox.setEnabled( other && ( ! isSystemCheckBox.getValue() ) );
				isSystemCheckBox.setEnabled( other && ( ! isWriteCheckBox.getValue() ) );					
				//Enable the read all check box only if it is a system property
				isReadAllCheckBox.setEnabled( other && isSystemCheckBox.getValue() );
				//Enable the selection list only if the read all is checked  
				readAllDurationListBox.setEnabled( other && isReadAllCheckBox.getValue() );
			}
		});
	}
	
	@Override
	protected void actionRightButton() {
		try{
			//Create or fill in the RoomUserAccessData from the dialog fields
			getTheUserRoomAccessData();
			
			//Disable the dialog elements before the request
			setEnabledElements(false, false, false);
			
			//Ensure lazy loading
			(new SplitLoad( true ){
				@Override
				public void execute() {
					//Contact to the server
					if( isNew ) {
						//Create a new user room access on the server
						CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>(progressBarUI) {
							public void onSuccessAct(Void result) {
								if( ( parentDialog != null ) && ( parentDialog instanceof RoomUsersManagerDialogUI ) ) {
									//Update the parent dialog table;
									((RoomUsersManagerDialogUI)parentDialog).updateActualData();
								} else {
									//For adding users from the user-search dialog we do
									//not do anything here because the updates are handled
									//from the AddRoomUserDialog 
								}
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
						roomManagerObject.createRoomAccess(SiteManager.getUserID(), SiteManager.getUserSessionId(), userAccess, callback);
					} else {
						//Update an old user room access data on the server
						CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>(progressBarUI) {
							public void onSuccessAct(Void result) {
								if( parentDialog != null ) {
									//Update the parent dialog table;
									((RoomUsersManagerDialogUI)parentDialog).updateActualData();
								}
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
						roomManagerObject.updateRoomAccess(SiteManager.getUserID(), SiteManager.getUserSessionId(), userAccess, callback);
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
