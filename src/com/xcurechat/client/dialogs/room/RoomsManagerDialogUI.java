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

import com.google.gwt.user.client.rpc.AsyncCallback;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.chat.RoomsManagerUI;
import com.xcurechat.client.data.ChatRoomData;
import com.xcurechat.client.data.search.OnePageViewData;
import com.xcurechat.client.dialogs.PagedActionGridDialog;
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
 * The rooms manager dialog 
 */
public class RoomsManagerDialogUI extends PagedActionGridDialog<ChatRoomData> {
	//The number of columns in the roomsTable
	private static final int NUMBER_OF_ROOM_COLUMNS = 5;
	//The number of rows in the roomsTable
	private static final int NUMBER_OF_ROWS_PER_PAGE = 5;
	
	//Delete and Create buttons for deleting and creating new rooms
	private final Button createButton = new Button();
	private final Button deleteButton = new Button();
	
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
	public RoomsManagerDialogUI( final int forUserID, final String forUserLoginName,
								 final DialogBox parentDialog, final RoomsManagerUI roomsManager ) {
		super( false, true, true, NUMBER_OF_ROWS_PER_PAGE, NUMBER_OF_ROOM_COLUMNS, forUserID, parentDialog );
		
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
	//We have to suppress warnings about custing to a generic type
	private void addDialogElements() {
		//First add the grid with the table
		addNewGrid( 2, false, "", false);

		//Add "Delete" button for the rooms
		deleteButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		deleteButton.setText( titlesI18N.deleteButton() );
		deleteButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				//If there are room IDs selected then delete the rooms 
				if( isSelectedData() ) {
					//Disable the dialog controls
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
									//Remove the deleted rooms from the interface
									roomsManager.afterLocalRoomsDelete( getSelectedDataIDs() );
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
							roomManagerObject.delete(SiteManager.getUserID(), SiteManager.getUserSessionId(), forUserID, getSelectedDataIDs(), callback);
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
							ErrorMessagesDialogUI.openErrorDialog( I18NManager.getErrors().thereIsNoSelectedRooms() );
						}
					}).loadAndExecute();
				}
			}
		} );
		this.addToGrid(FIRST_COLUMN_INDEX, deleteButton, false, false);

		//Add "Create" button for the rooms
		createButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		createButton.setText( titlesI18N.createButton() );
		createButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				//Ensure lazy loading
				( new SplitLoad( true ) {
					@Override
					public void execute() {
						RoomDialogUI roomDialog = new RoomDialogUI( true, null, thisDialog, forUserID, forUserLoginName, roomsManager );
						roomDialog.show();
						roomDialog.center();
					}
				}).loadAndExecute();
			}
		} );
		this.addToGrid(SECOND_COLUMN_INDEX, createButton, false, true);
		
		//Fill the table headings
		dataTable.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_STYLE_NAME );
		Label roomNumberLabel = new Label(titlesI18N.indexColumnTitle());
		roomNumberLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, 0, roomNumberLabel );

		//Add the select data entries column to the data table
		addSelectorColumnTitleToDataTable(0, 1);

		Label roomStatusLabel = new Label(titlesI18N.statusColumnTitle());
		roomStatusLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, 2, roomStatusLabel );

		Label roomTypeLabel = new Label(titlesI18N.accessOrTypeColumnTitle());
		roomTypeLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, 3, roomTypeLabel );
		
		Label roomNameLabel = new Label(titlesI18N.roomNameColumnTitle());
		roomNameLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, 4, roomNameLabel );
		
		dataTable.setSize("100%", "100%");
		addToGrid(this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, dataTable, true, false);
		
		//Add the next/prev buttons and the progress bar
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
		OnePageViewData<ChatRoomData> data = (OnePageViewData<ChatRoomData>) result;
		final ChatRoomData roomData = data.entries.get( index );

		int roomNumber = ( ( getCurrentPageNumber() - 1 ) * NUMBER_OF_ROWS_PER_PAGE) + ( index + 1);
		dataTable.setWidget( row, 0, new Label( Integer.toString( roomNumber ) ) );
		
		//Add the check box into the rom/column, it should 
		//mark the data entry (room) with the given ID. 
		addSelectorIntoDataTableRow(row, 1, index, new Integer(roomData.getRoomID()), "");
		
		final String statusImageURL;
		final String statusImageTitle;
		if( roomData.isExpired() ) {
			statusImageURL = ServerSideAccessManager.getClosedRoomImageURL();
			statusImageTitle = titlesI18N.roomStatusClosed();
		} else {
			statusImageURL = ServerSideAccessManager.getOpenRoomImageURL();
			statusImageTitle = titlesI18N.roomStatusOpen();
		}
		Image statusImage = new Image( statusImageURL );
		statusImage.setTitle( statusImageTitle );
		statusImage.setStyleName( CommonResourcesContainer.PAGED_DIALOG_STATUS_IMAGE_STYLE_NAME );
		dataTable.setWidget( row, 2, statusImage );
		
		Image typeImage = new Image();
		ChatRoomData.getRoomTypeImage( roomData.getRoomType(), roomData.isMain(), typeImage );
		dataTable.setWidget( row, 3, typeImage );
		
		Label roomNameLink = new Label( );
		InterfaceUtils.populateRoomNameLabel(roomData, true, roomNameLink);
		roomNameLink.setStyleName( CommonResourcesContainer.ROOM_DIALOG_MANAGEMENT_LINK );
		roomNameLink.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent e){
				RoomDialogUI roomDialog = new RoomDialogUI( false, roomData, (PagedActionGridDialog) thisDialog,
														    forUserID, forUserLoginName, roomsManager);
				roomDialog.show();
				roomDialog.center();
			}
		});
		dataTable.setWidget( row, 4, roomNameLink );
	}
	
	@Override
	protected void enableControls( final boolean enable ) {
		Scheduler.get().scheduleDeferred( new ScheduledCommand(){
			public void execute() {
				deleteButton.setEnabled( enable );
				createButton.setEnabled( enable );
			}
		});
	}

	@Override
	protected void updateDialogTitle( final int numberOfEntries, final int numberOfPages, final int currentPageNumber ) {
		if( forUserID != SiteManager.getUserID() ) {
			this.setText( titlesI18N.roomManagerDialogTitle( forUserLoginName, numberOfEntries, currentPageNumber, numberOfPages ) );
		} else {
			this.setText( titlesI18N.roomManagerDialogTitle( numberOfEntries, currentPageNumber, numberOfPages ) );
		}
 	}
	
	@Override
	protected Integer getDataEntryID(OnePageViewData<ChatRoomData> onePageData, final int index) {
		return onePageData.entries.get( index ).getRoomID();
	}

	@Override
	protected String getDataEntryName(OnePageViewData<ChatRoomData> onePageData, final int index) {
		//The room name is not used, so this is an empty implementation
		return "";
	}
	
	@Override
	protected void browse( final int userID, final String userSessionID, final int forUserID,
						   final int offset, final int number_or_rows_per_page,
						   final AsyncCallback<OnePageViewData<ChatRoomData>> callback) throws SiteException {
		//Ensure lazy loading
		(new SplitLoad(){
			@Override
			public void execute() {
				RoomManagerAsync roomManager = RPCAccessManager.getRoomManagerAsync();
				roomManager.browse(userID, userSessionID, forUserID, offset, NUMBER_OF_ROWS_PER_PAGE, callback);
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
				roomManager.count(userID, userSessionID, forUserID, callback);
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
