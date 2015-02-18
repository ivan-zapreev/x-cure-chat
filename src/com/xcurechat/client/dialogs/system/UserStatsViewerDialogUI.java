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
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client.dialogs.system;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

import com.google.gwt.user.client.rpc.AsyncCallback;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.data.UserData;
import com.xcurechat.client.data.UserStatsEntryData;
import com.xcurechat.client.data.search.OnePageViewData;
import com.xcurechat.client.dialogs.PagedActionGridDialog;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;
import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.rpc.UserStatisticsAsync;
import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.Button;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * Allows to browse through the user login/logout statistics
 */
public class UserStatsViewerDialogUI extends PagedActionGridDialog<UserStatsEntryData> {
	private static final int NUMBER_OF_STAT_TABLE_COLUMNS = 4;
	private static final int NUMBER_OF_ROWS_PER_PAGE = 10;

	//The scroll panel that will store the table data
    private ScrollPanel scrollPanel = new ScrollPanel();
    
    //Action buttons of the dialog
	protected Button clearButton = new Button();

	//The password text field
    private final PasswordTextBox passwordTextBox = new PasswordTextBox();
	
    //The login of the user we browse data for
    private final String forUserLoginName;
    
	/**
	 * A simple constructor for the user statistics management dialog
	 * @param forUserID the user ID we brows statistics for
	 * @param forUserLoginName the login name of the user we brows data for
	 * @param parentDialog the parent dialog if any
	 */
	public UserStatsViewerDialogUI( final int forUserID, final String forUserLoginName, final DialogBox parentDialog ){
		super( false, true, true, NUMBER_OF_ROWS_PER_PAGE, NUMBER_OF_STAT_TABLE_COLUMNS, forUserID, parentDialog );
		
		this.forUserLoginName = forUserLoginName;
		
		//Set the dialog's caption.
		updateDialogTitle( );
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
	
	private void addTheMainStatTable() {
		//First add the grid with the table
		addNewGrid( 1, false, "", false);
		
		//Fill in the data table which should be created
		//by now in the super class constructor.
		Label logTypeLabel = new Label(titlesI18N.userStatsEntryType());
		logTypeLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, 0, logTypeLabel );
		Label dateLabel = new Label(titlesI18N.userStatsDateTitle());
		dateLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, 1, dateLabel );
		Label hostLabel = new Label(titlesI18N.userStatsHostTitle());
		hostLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, 2, hostLabel );
		Label locationLabel = new Label(titlesI18N.userStatsLocationTitle());
		locationLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, 3, locationLabel );
		dataTable.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_STYLE_NAME );
		scrollPanel.add(dataTable);
		addToGrid(this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, scrollPanel, false, false);
		
		//Add the next/prev and progress bar elements grid
		addDefaultControlPanel();
	}

	private void addClearStatsGrid() {
		addNewGrid( 1, true, titlesI18N.clearUserStatisticsTitle(), false );
		
		final HorizontalPanel panel = new HorizontalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		
		//Add the password label and the password box
		final Label passwordLabel = new Label(titlesI18N.currentPasswordField() + CommonResourcesContainer.FIELD_LABEL_SUFFIX);
		passwordLabel.setWordWrap(false);
		passwordLabel.setStyleName( CommonResourcesContainer.USER_STATS_COMPULSORY_FIELD_LABEL );
		panel.add( passwordLabel );
		panel.add( new HTML("&nbsp;"));
		panel.add(passwordTextBox);
		panel.add( new HTML("&nbsp;"));

		//Add navigation button "Next"
		clearButton.setStyleName( CommonResourcesContainer.USER_STATS_CLEAR_BUTTON_STYLE );
		clearButton.setText( titlesI18N.userStatsClearButton() );
		clearButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				//Disable buttons for now
				disableAllControls();
				
				try{
					final String password = passwordTextBox.getText();
					UserData.validatePassword( password, true );
					
					//Ensure lazy loading
					(new SplitLoad( true ){
						@Override
						public void execute() {
							//Create the call-back object
							CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>(progressBarUI) {
								public void onSuccessAct(Void result) {
									//Update the statistics
									updateDataTable(null);
									//Reset the number of entries
									setNumberOfEntries( 0 );
									//Update the dialog title
									updateDialogTitle();
									//Clear the password text box
									passwordTextBox.setText("");
									//Enable the buttons back
									enableAllControls();
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
								};
							};
							UserStatisticsAsync userStatsService = RPCAccessManager.getUserStatisticsAsync();
							userStatsService.delete( SiteManager.getUserID(), SiteManager.getUserSessionId(), password, callback);
						}
						@Override
						public void recover() {
							//Enable the buttons back
							enableAllControls();
						}
					}).loadAndExecute();
				} catch ( final SiteException exception ){
					//Enable the controls
					enableAllControls();
					(new SplitLoad( true ) {
						@Override
						public void execute() {
							//Report the error
							ErrorMessagesDialogUI.openErrorDialog( exception );
						}
					}).loadAndExecute();
				}
			}
		} );
		panel.add( clearButton );
		
		this.addToGrid(FIRST_COLUMN_INDEX, panel, false, false);
	}

	//We have to suppress warnings about custing to a generic type
	@SuppressWarnings("unchecked")
	protected void addRowToTable(final int row, final int index, final Object result) {
		//Convert the object data
		OnePageViewData<UserStatsEntryData> data = (OnePageViewData<UserStatsEntryData>) result;
		UserStatsEntryData entry =  data.entries.get( index );
		Image image;
		if( entry.isLogin ) {
			image = new Image( ServerSideAccessManager.getUserStatsLogInImageURL() );
			image.setStyleName( CommonResourcesContainer.USER_STATS_DIALOG_IMAGE_STYLE );
			image.setTitle( titlesI18N.userStatisticsLogInImageTip() );
		} else {
			if( entry.isAuto ) {
				image = new Image( ServerSideAccessManager.getUserStatsLogOutAutoImageURL() );
				image.setStyleName( CommonResourcesContainer.USER_STATS_DIALOG_IMAGE_STYLE );
				image.setTitle( titlesI18N.userStatisticsLogOutAutoImageTip() );
			} else {
				image = new Image( ServerSideAccessManager.getUserStatsLogOutImageURL() );
				image.setStyleName( CommonResourcesContainer.USER_STATS_DIALOG_IMAGE_STYLE );
				image.setTitle( titlesI18N.userStatisticsLogOutImageTip() );
			}
		}
		dataTable.setWidget(row, 0, image );
		final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat( PredefinedFormat.DATE_TIME_MEDIUM );
		dataTable.setWidget(row, 1, new Label( dateTimeFormat.format(entry.date) ) );
		dataTable.setWidget(row, 2, new Label(entry.host) );
		
		if( (entry.location != null) && !entry.location.isEmpty() ) {
			dataTable.setWidget(row, 3, new Label( entry.location ) );
		} else {
			dataTable.setWidget(row, 3, new Label( titlesI18N.unknownTextValue() ) );
		}
	}
	
	@Override
	protected void populateDialog() {
		addTheMainStatTable();
		addClearStatsGrid();
		updateActualData();
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
	protected void enableControls( final boolean enable ) {
		Scheduler.get().scheduleDeferred( new ScheduledCommand(){
			public void execute() {
				clearButton.setEnabled( enable );
			}
		});
	}

	@Override
	protected void updateDialogTitle( int numberOfEntries, final int numberOfPages, final int currentPageNumber ) {
		this.setText( titlesI18N.userStatisticsDialogTitle( forUserLoginName, currentPageNumber, numberOfPages ) );
 	}
	
	@Override
	protected Integer getDataEntryID(OnePageViewData<UserStatsEntryData> onePageData, final int index){
		//The statistics data table does not have selectors for row entries,
		//therefore this method does not need to be implemented
		return -1;
	}
	
	@Override
	protected String getDataEntryName(OnePageViewData<UserStatsEntryData> onePageData, final int index) {
		//The statistics data table does not have selectors for row entries,
		//therefore this method does not need to be implemented
		return null;
	}
	
	@Override
	protected void browse( final int userID, final String userSessionID, final int forUserID,
						   final int offset, final int number_or_rows_per_page,
						   final AsyncCallback<OnePageViewData<UserStatsEntryData>> callback) throws SiteException {
		//Ensure lazy loading
		(new SplitLoad(){
			@Override
			public void execute() {
				UserStatisticsAsync userStatsService = RPCAccessManager.getUserStatisticsAsync();
				userStatsService.browse( userID, userSessionID, offset, NUMBER_OF_ROWS_PER_PAGE, callback );
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
				UserStatisticsAsync userStatsCountService = RPCAccessManager.getUserStatisticsAsync();
				userStatsCountService.count( userID, userSessionID, callback);
			}
			@Override
			public void recover() {
				callback.onFailure( new InternalSiteException( I18NManager.getErrors().serverDataLoadingFalied() ) );
			}
		}).loadAndExecute();
	}
	
	@Override
	protected void beforeTableDataUpdate(){
		//There is othing to be done here
	}

}
