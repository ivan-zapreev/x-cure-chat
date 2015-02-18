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

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.data.RoomUserAccessData;
import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.dialogs.PagedActionGridDialog;
import com.xcurechat.client.dialogs.profile.ViewUserProfileDialogUI;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.rpc.RoomManagerAsync;
import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.Button;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * The manager dialog for the room's users
 */
public class RoomUsersManagerDialogUI extends PagedActionGridDialog<RoomUserAccessData> {
	
	//The number of columns in the enclosed table
	private static final int NUMBER_OF_COLUMNS_USER = 3;
	private static final int NUMBER_OF_COLUMNS_ADMIN = 4;
	//The number of rows in the enclosed table
	private static final int NUMBER_OF_ROWS_PER_PAGE = 5;
	
	//Delete and Add buttons for deleting and adding users
	private final Button addButton = new Button();
	private final Button deleteButton = new Button();
	
    //The chat room data object
    private final ChatRoomData thisRoomData;
	
	//The reference fo this dialog, is needed when opening new dialogs
	private final DialogBox thisDialogRef;
    
    /**
     * A simple sialog for viewing the rooms of a user
	 * @param roomData the room object we manage users for
	 * @param parentDialog the reference to the parent dialog
     */
	public RoomUsersManagerDialogUI( ChatRoomData roomData, final DialogBox parentDialog ) {
		super( false, true, true, NUMBER_OF_ROWS_PER_PAGE,
				( (SiteManager.getUserProfileType() == MainUserData.ADMIN_USER_TYPE) ? NUMBER_OF_COLUMNS_ADMIN : NUMBER_OF_COLUMNS_USER ),
				SiteManager.getUserID(), parentDialog );
				
		this.thisRoomData = roomData;
		this.thisDialogRef = this;
		
		//Enable the action buttons and hot key, even though we will not be adding these buttons
		//to the grid, we will use the hot keys associated with them to close this dialog
		setLeftEnabled( true );
		setRightEnabled( true );
		
		//Set the dialog's caption.
		updateDialogTitle();
		
		//Disable the actual dialog buttons for now
		disableAllControls();
		
		//Fill dialog with data
		populateDialog();	    
	}
	
	/**
	 * Adds the dialog elements, such as the users table and the buttons.
	 */
	//We have to suppress warnings about custing to a generic type
	private void addDialogElements() {
		//First add the grid with the table
		addNewGrid( 3, false, "", false);

		//Add the rom name for the extra info
		HorizontalPanel roomNameFieldAndValuePanel = new HorizontalPanel();
		Label roomNameFieldLabel = InterfaceUtils.getNewFieldLabel( titlesI18N.roomNameFieldTitle(), false );
		roomNameFieldAndValuePanel.add( roomNameFieldLabel );
		roomNameFieldAndValuePanel.add( new HTML( "&nbsp;" ) );
		Label roomNameValueLabel = new Label( );
		InterfaceUtils.populateRoomNameLabel( thisRoomData, true, roomNameValueLabel );
		roomNameValueLabel.setStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_IMP_STYLE_NAME );
		roomNameFieldAndValuePanel.add( roomNameValueLabel );
		this.addToGrid(this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, roomNameFieldAndValuePanel, false, false );
		
		//Add "Delete" button for the rooms
		deleteButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		deleteButton.setText( titlesI18N.deleteButton() );
		deleteButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				//If there are room IDs selected then delete the rooms 
				if( isSelectedData() ) {
					//Disable the dialog controls and the check boxes
					disableAllControls();
					
					//Ensure lazy loading
					(new SplitLoad( true ){
						@Override
						public void execute() {
							//Call the deletion procedure
							CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>(progressBarUI) {
								public void onSuccessAct(Void result) {
									//Update the parent dialog table;
									updateActualData();
									//We do not enable check boxes here because we update the
									//entire page and the check boxes are re initialized 
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
							roomManagerObject.deleteRoomUsers(SiteManager.getUserID(), SiteManager.getUserSessionId(), thisRoomData.getRoomID(), getSelectedDataIDs(), callback);
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
							ErrorMessagesDialogUI.openErrorDialog( I18NManager.getErrors().thereIsNoSelectedUsers() );
						}
					}).loadAndExecute();
				}
			}
		} );
		this.addToGrid(FIRST_COLUMN_INDEX, deleteButton, true, false);

		//Add "Create" button for the rooms
		addButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		addButton.setText( titlesI18N.addButtonTitle() );
		addButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				//Ensure lazy loading
				( new SplitLoad( true ) {
					@Override
					public void execute() {
						AddRoomUserDialogUI dialog = new AddRoomUserDialogUI(thisRoomData.getRoomID(), thisRoomData.getRoomName(), thisDialogRef);
						dialog.show();
						dialog.center();
					}
				}).loadAndExecute();
			}
		} );
		this.addToGrid(SECOND_COLUMN_INDEX, addButton, false, true);
		
		//Fill the table headings
		int column = 0;
		dataTable.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_STYLE_NAME );
		Label roomNumberLabel = new Label(titlesI18N.indexColumnTitle());
		roomNumberLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, column++, roomNumberLabel );
		
		//Add the select data entries column to the data table
		addSelectorColumnTitleToDataTable(0, column++);
		
		if( isAdmin ) {
			//If the user is admin then we show an extra field
			Label roomReadAllExpiredLabel = new Label( titlesI18N.accessOrTypeColumnTitle() );
			roomReadAllExpiredLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
			dataTable.setWidget(0, column++, roomReadAllExpiredLabel );
		}
		
		Label userNameLabel = new Label(titlesI18N.usersTitle());
		userNameLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, column++, userNameLabel );
		
		dataTable.setSize("100%", "100%");
		addToGrid(this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, dataTable, true, false);
		
		//Add the next/prev and progress bar elements grid
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
		//The column index
		int column = 0;
		
		//Convert the object data
		OnePageViewData<RoomUserAccessData> data = (OnePageViewData<RoomUserAccessData>) result;
		final RoomUserAccessData userAccessData = data.entries.get( index );

		//Set the index of the entry
		int roomNumber = ( ( getCurrentPageNumber() - 1 ) * NUMBER_OF_ROWS_PER_PAGE) + ( index + 1);
		dataTable.setWidget( row, column++, new Label( Integer.toString( roomNumber ) ) );
		
		//Add the check box into the rom/column, it should 
		//mark the data entry (user access) with the given ID. 
		addSelectorIntoDataTableRow(row, column++, index, new Integer(userAccessData.getRAID()), "");
		
		//Set the read-all access status here if the interface is sed by the admin
		if( isAdmin ) {
			Image readAllStatusImage = getReadAllStatusImage( userAccessData.isReadAll(), userAccessData.isReadAllExpired() );
			dataTable.setWidget( row, column++, readAllStatusImage );
		}
		
		//Set the user name/link here, depending o whether we browse by and admin or not
		Label userNameLink = new Label( userAccessData.getUserLoginName() );
		if( isAdmin && userAccessData.isSystem() ){
			userNameLink.setStyleName( CommonResourcesContainer.DIALOG_LINK_RED_STYLE );
		} else {
			userNameLink.setStyleName( CommonResourcesContainer.DIALOG_LINK_BLUE_STYLE );
		}
		userNameLink.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e){
				//If it is an admin we work with then he can fine tune the access rights
				if( isAdmin ) {
					//Ensure lazy loading
					final SplitLoad executor = new SplitLoad( true ) {
						@Override
						public void execute() {
							RoomUserAccessDialogUI accessDialog = new RoomUserAccessDialogUI(false, userAccessData, thisDialog, MainUserData.UNKNOWN_UID, null, thisRoomData.getRoomID(), ChatRoomData.getRoomName(thisRoomData) );
							accessDialog.show();
							accessDialog.center();
						}
					};
					executor.loadAndExecute();
				} else {
					//Ensure lazy loading
					final SplitLoad executor = new SplitLoad( true ) {
						@Override
						public void execute() {
							//If it is a regular user then we only show the profile
							ViewUserProfileDialogUI userProfile = new ViewUserProfileDialogUI( userAccessData.getUID(),
																								userAccessData.getUserLoginName(),
																								thisDialog, false );
							userProfile.show();
							userProfile.center();
						}
					};
					executor.loadAndExecute();
				}
			}
		});
		dataTable.setWidget( row, column++, userNameLink );
	}
	
	@Override
	protected void enableControls( final boolean enable ) {
		deleteButton.setEnabled( enable );
		addButton.setEnabled( enable );
	}
	
	@Override
	protected Integer getDataEntryID(OnePageViewData<RoomUserAccessData> onePageData, final int index){
		return ((RoomUserAccessData) onePageData.entries.get( index )).getRAID();
	}
	
	@Override
	protected String getDataEntryName(OnePageViewData<RoomUserAccessData> onePageData, final int index) {
		//The user-room access name is not used, so this is an empty implementation
		return "";
	}
	
	@Override
	protected void updateDialogTitle( final int numberOfEntries, final int numberOfPages, final int currentPageNumber ) {
		this.setText( titlesI18N.roomUsersManagerDialogTitle( numberOfEntries, currentPageNumber, numberOfPages ) );
 	}

	@Override
	protected void browse( final int userID, final String userSessionID, final int forUserID,
						   final int offset, final int number_or_rows_per_page,
						   final AsyncCallback<OnePageViewData<RoomUserAccessData>> callback) throws SiteException {
		//Ensure lazy loading
		(new SplitLoad(){
			@Override
			public void execute() {
				RoomManagerAsync roomManager = RPCAccessManager.getRoomManagerAsync();
				roomManager.browseRoomUsers( userID, userSessionID, thisRoomData.getRoomID(),
											 offset, NUMBER_OF_ROWS_PER_PAGE, callback);
			}
			@Override
			public void recover() {
				callback.onFailure( new InternalSiteException( I18NManager.getErrors().serverDataLoadingFalied() ) );
			}
		}).loadAndExecute();
	}

	@Override
	protected void count( final int userID, final String userSessionID, final int forUserID,
						  final AsyncCallback<Integer> callback) throws SiteException {
		//Ensure lazy loading
		(new SplitLoad(){
			@Override
			public void execute() {
				RoomManagerAsync roomManager = RPCAccessManager.getRoomManagerAsync();
				roomManager.countRoomUsers(userID, userSessionID, thisRoomData.getRoomID(), callback);
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

	/**
	 * This method creates the room-user read-all access status image for the admin interface
	 * @param isReadAll if the read all access is on
	 * @param isReadAllExpired if the read all access is expired
	 * @return the proper Image to put into the room-users table  
	 */
	protected Image getReadAllStatusImage( final boolean isReadAll, final boolean isReadAllExpired ) {
		String url, title;
		if( isReadAll ) {
			if( isReadAllExpired ) {
				url = ServerSideAccessManager.getReadAllExpiredImageURL();
				title = titlesI18N.readAllExpiredHintTitle();
			} else {
				url = ServerSideAccessManager.getReadAllActiveImageURL();
				title = titlesI18N.readAllActiveHintTitle();
			}
		} else {
			url = ServerSideAccessManager.getReadAllNotOnImageURL();
			title = titlesI18N.readAllNotOnHintTitle();
		}
		
		Image readAllStatusImage = new Image( url );
		readAllStatusImage.setTitle( title );
		readAllStatusImage.setStyleName( CommonResourcesContainer.PAGED_DIALOG_STATUS_IMAGE_STYLE_NAME );
				
		return readAllStatusImage;
	}

}
