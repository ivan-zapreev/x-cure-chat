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
 * The profile interface package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.dialogs.profile;

import java.util.List;
import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.dialogs.PagedActionGridDialog;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.UserManagerAsync;

import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.Button;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This class is a dialog that allows user to manage the files that are attached to his profile
 */
public class ProfileFilesManagerUI extends PagedActionGridDialog<ShortFileDescriptor> {
	
	private static final int NUMBER_OF_FILES_PER_PAGE = 7;
	private static final int NUMBER_OF_COLUMNS = 4;
	
	//The user profile data
	private final UserData userData;
	
	//Delete and Add buttons for deleting and adding files
	private final Button addButton = new Button();
	private final Button deleteButton = new Button();

	/**
	 * The constructor
	 * @param forumMessage the forum message we will manage filed for
	 * @param parentDialog the parent dialog
	 */
	public ProfileFilesManagerUI( final UserData userData, final DialogBox parentDialog ) {
		super( false, true, true, NUMBER_OF_FILES_PER_PAGE, NUMBER_OF_COLUMNS, SiteManager.getUserID(), parentDialog );
		
		//Store the profile data
		this.userData = userData;
		
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
	 * Allows to add a new file
	 * @param fileDesc the descriptor of the uploaded file
	 */
	void addNewProfileFile( final ShortFileDescriptor fileDesc ) {
		disableAllControls();
		this.userData.addUserProfileFileDescr( fileDesc );
		this.updateActualData();
		//Indicate that there are new attached files
		setAttachedFilesIndicator( );
		//Enable the controls
		enableAllControls();
	}
	
	/**
	 * Allows to set update the list of uploaded files dialog if it is an instance of UserProfileDialogUI
	 */
	private void setAttachedFilesIndicator() {
		//This dialog should be opened from UserProfileDialogUI but we check for it just in case
		if( parentDialog instanceof UserProfileDialogUI ) {
			//Update the list of uploaded files
			( (UserProfileDialogUI) parentDialog ).updateUserFilesView();
		}
	}
	
	/**
	 * Adds the dialog elements, such as the files table and the buttons.
	 */
	private void addDialogElements() {
		//First add the grid with the table
		addNewGrid( 2, false, "", false);
		
		//Add "Delete" button for the rooms
		deleteButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		deleteButton.setText( titlesI18N.deleteButton() );
		deleteButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				//If there are message IDs selected then delete those messages 
				if( isSelectedData() ) {
					//Disable controls
					disableAllControls();
					
					//Ensure lazy loading
					(new SplitLoad( true ){
						@Override
						public void execute() {
							//Construct the call back object
							CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>(progressBarUI) {
								public void onSuccessAct(Void result) {
									//Remove the deleted files from the list of files
									List<Integer> deletedFileIds = getSelectedDataIDs();
									for(int index = 0; index < deletedFileIds.size(); index++ ) {
										userData.getFileDescriptors().remove( deletedFileIds.get(index) );
									}
									//Update the list of files in the aller
									setAttachedFilesIndicator( );
									//We do not enable check boxes here because we update the
									//entire page and the check boxes are re initialized 
									updateActualData();
								}
								public void onFailureAct(final Throwable caught) {
									(new SplitLoad( true ) {
										@Override
										public void execute() {
											//Remove the error
											ErrorMessagesDialogUI.openErrorDialog(caught);
										}
									}).loadAndExecute();
									//Use the recovery method
									recover();
								}
							};
							//Perform the server call
							UserManagerAsync userManagerObj = RPCAccessManager.getUserManagerAsync();
							userManagerObj.deleteProfileFiles( SiteManager.getUserID(), SiteManager.getUserSessionId(),
									 						   getSelectedDataIDs(), callback );
						}
						@Override
						public void recover() {
							enableAllControls();
						}
					}).loadAndExecute();
				} else {
					//Ensure lazy loading
					(new SplitLoad(){
						@Override
						public void execute() {
							//Report the error
							ErrorMessagesDialogUI.openErrorDialog( I18NManager.getErrors().thereIsNoSelectedFilesToDelete() );
						}
					}).loadAndExecute();
				}
			}
		} );
		this.addToGrid(FIRST_COLUMN_INDEX, deleteButton, false, false);

		//Add "Create" button for the rooms
		addButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		addButton.setText( titlesI18N.addButtonTitle() );
		addButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				ProfileFileUploadDialogUI dialog = new ProfileFileUploadDialogUI( thisDialog );
				dialog.show();
				dialog.center();
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
		
		//Add the file type column
		Label roomReadAllExpiredLabel = new Label( titlesI18N.fileTypeColumnTitle() );
		roomReadAllExpiredLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, column++, roomReadAllExpiredLabel );
		
		//Add the file name column
		Label userNameLabel = new Label( titlesI18N.fileNameColumnTitle() );
		userNameLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, column++, userNameLabel );
		
		dataTable.setSize("100%", "100%");
		addToGrid(this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, dataTable, true, false);
		
		//Add the next/prev and progress bar elements grid
		addDefaultControlPanel();
	}
	
	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.PagedActionGridDialog#addRowToTable(int, int, java.lang.Object)
	 */
	@Override
	//We have to suppress warnings about custing to a generic type
	@SuppressWarnings("unchecked")
	protected void addRowToTable(int row, int index, Object result) {
		final ShortFileDescriptor fileDescriptor = ((OnePageViewData<ShortFileDescriptor>) result).entries.get(index);
		
		//Column counter
		int column = 0;
		
		//01 Add the file index 
		int messageNumber = ( ( getCurrentPageNumber() - 1 ) * NUMBER_OF_FILES_PER_PAGE) + ( index + 1);
		dataTable.setWidget( row, column++, new Label( Integer.toString( messageNumber ) ) );
		
		//02 Add the check box into the messages column, it should 
		//mark the data entry (message) with the given ID. 
		addSelectorIntoDataTableRow( row, column++, index, new Integer( fileDescriptor.fileID ), fileDescriptor.fileName );
		
		//03 Add the file type label
		Label mimeType =  new Label( fileDescriptor.mimeType );
		dataTable.setWidget( row, column++, mimeType );
		
		//04 Add the file name
		Label fileName =  new Label( ShortFileDescriptor.getShortFileName( fileDescriptor.fileName ) );
		fileName.setTitle( fileDescriptor.fileName );
		fileName.setStyleName( CommonResourcesContainer.DIALOG_LINK_BLUE_STYLE );
		fileName.addClickHandler( new ClickHandler(){
			public void onClick(ClickEvent e){
				//Ensure lazy loading
				final SplitLoad executor = new SplitLoad( true ) {
					@Override
					public void execute() {
						//Open the image view pop up
						ViewUserProfileFilesDialogUI viewDialog = new ViewUserProfileFilesDialogUI( ProfileFilesManagerUI.this, fileDescriptor,
																									userData.getShortLoginName(), userData.getUID() );
						viewDialog.show();
						viewDialog.center();
					}
				};
				executor.loadAndExecute();
				//Stop the event from being propagated, prevent default
				e.stopPropagation(); e.preventDefault();
			}
		});
		dataTable.setWidget( row, column++, fileName );
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.PagedActionGridDialog#beforeTableDataUpdate()
	 */
	@Override
	protected void beforeTableDataUpdate() {
		//Do nothing
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.PagedActionGridDialog#browse(int, java.lang.String, int, int, int, com.google.gwt.user.client.rpc.AsyncCallback)
	 */
	@Override
	protected void browse( final int userID, final String userSessionID, final int forUserID,
						   final int offset, final int numberOfRowsPerPage,
						   final AsyncCallback<OnePageViewData<ShortFileDescriptor>> callback) throws SiteException {
		final int maximumRequestedFileIdex = (offset + numberOfRowsPerPage);
		final int maximumAvailableFileIndex = this.userData.getNumberOfFiles();
		final int maximumFileIndex = ( maximumRequestedFileIdex > maximumAvailableFileIndex ? maximumAvailableFileIndex : maximumRequestedFileIdex );
		
		OnePageViewData<ShortFileDescriptor> pageData = new OnePageViewData<ShortFileDescriptor>();
		pageData.entries = new ArrayList<ShortFileDescriptor>();
		pageData.offset = offset;
		
		List<ShortFileDescriptor> fileDescList = new ArrayList<ShortFileDescriptor>( this.userData.getFileDescriptors().values() );
		for( int index = offset; index < maximumFileIndex; index++ ) {
			pageData.entries.add( fileDescList.get(index) );
		}
		
		callback.onSuccess( pageData );
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.PagedActionGridDialog#count(int, java.lang.String, int, com.google.gwt.user.client.rpc.AsyncCallback)
	 */
	@Override
	//We have to suppress warnings about custing to a generic type
	protected void count(int userID, String userSessionID, int forUserID, AsyncCallback<Integer> callback) throws SiteException {
		callback.onSuccess( new Integer( this.userData.getNumberOfFiles() ) );
	}
	
	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.PagedActionGridDialog#enableControls(boolean)
	 */
	@Override
	protected void enableControls( boolean enable ) {
		deleteButton.setEnabled( enable );
		//The add button is enabled only if the number of files does not exceed the maximum allowed value and
		//the file can only be added by the person who created the message
		addButton.setEnabled( enable && ( this.userData.getNumberOfFiles() < UserData.MAXIMUM_NUMBER_OF_FILES ) &&
							  				userData.getUID() == SiteManager.getUserID() );
	}
	
	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.PagedActionGridDialog#getDataEntryID(com.xcurechat.client.data.OnePageViewData, int)
	 */
	@Override
	protected Integer getDataEntryID( OnePageViewData<ShortFileDescriptor> onePageData, int index ) {
		return onePageData.entries.get(index).fileID ;
	}
	
	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.PagedActionGridDialog#getDataEntryName(com.xcurechat.client.data.OnePageViewData, int)
	 */
	@Override
	protected String getDataEntryName( OnePageViewData<ShortFileDescriptor> onePageData, int index ) {
		return onePageData.entries.get(index).fileName ;
	}
	
	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.PagedActionGridDialog#updateDialogTitle(int, int, int)
	 */
	@Override
	protected void updateDialogTitle( int numberOfEntries, int numberOfPages, int currentPageNumber ) {
		this.setText( titlesI18N.addRemoveProfileFilesDialogTitle( numberOfEntries, currentPageNumber, numberOfPages ) );
	}
	
	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.ActionGridDialog#actionLeftButton()
	 */
	@Override
	protected void actionLeftButton() {
		hide();
	}
	
	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.ActionGridDialog#actionRightButton()
	 */
	@Override
	protected void actionRightButton() {
		hide();
	}

	/* (non-Javadoc)
	 * @see com.xcurechat.client.dialogs.ActionGridDialog#populateDialog()
	 */
	@Override
	protected void populateDialog() {
		addDialogElements();
		updateActualData();
	}

}
