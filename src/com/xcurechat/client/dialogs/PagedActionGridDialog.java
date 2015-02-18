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
package com.xcurechat.client.dialogs;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

import com.google.gwt.user.client.rpc.AsyncCallback;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.data.search.OnePageViewData;

import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.Button;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * This class allows for adding a per-page view on some data
 */
public abstract class PagedActionGridDialog<T> extends ActionGridDialog {
	
	//The group name for the radio button selectors of the dialog
	private static final String RADIO_BUTTONS_GROUP_NAME = "DataSelectors"; 

	//The number of columns in the tableData
	protected int NUMBER_OF_COLUMNS;
	//The number of table entries that will be shown on one page
	private int NUMBER_OF_ROWS_PER_PAGE;

    //The statistics data table
    protected FlexTable dataTable = new FlexTable();

    //The next/previous buttons to navigate through and the close button the results
    private Button previousButton = new Button();
    private Button nextButton = new Button();
    private Button closeButton = new Button();
	
	//The number of pages to show
	private int numberOfPages = 1;
	//The current page
	private int currentPageNumber = 1;
	//The current number of found entries
	private int numberOfEntries = 0;
	
	//The if of the user we manage data for
	private final int forUserID;
	
	//The select/deselect all check box in the table header
	private final CheckBox selectAllCheckBox = new CheckBox();
    //The list of checkboxes for the rooms
	private List<Integer> dataIDS;
	private List<String> dataNames;
	//We can have a single data field selector or several,
	//i.e. using radio buttons or check boxes
	final boolean isSelectSingle;
	private CheckBox[] selectorWidgetList;
	
	/**
	 * The constructor of the data navigation dialog. Does not initialize the data table.
	 * @param hasSmileyTarget must be true if the dialog contains smiley selection targets, instances. 
	 * @param autoHide if the dialog is autohide then true
	 * @param modal if the dialog is modal then true
	 * @param forUserID the id of the user we look data for
	 * @param parentDialog the parent dialog if any
	 * @param isSelectSingle true if we need only a single row
	 * selector in the data table, otherwise false  
	 */
	public PagedActionGridDialog( final boolean hasSmileyTarget, boolean autoHide, boolean modal,
								  final int forUserID, final DialogBox parentDialog,
								  final boolean isSelectSingle ) {
		super( hasSmileyTarget, autoHide, modal, parentDialog );
		
		//Save the kind of selectors we use
		this.isSelectSingle = isSelectSingle;

		//Save the ID of the user we brouse data for
		this.forUserID = forUserID;
		
		//Initialize the navigation buttons
		//Add navigation button "Previous"
		previousButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		previousButton.setText( titlesI18N.previousButton() );
		previousButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				navigateData( currentPageNumber - 1 );
			}
		} );
		
		//Add navigation button "Next"
		nextButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		nextButton.setText( titlesI18N.nextButton() );
		nextButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				navigateData( currentPageNumber + 1 );
			}
		} );
		
		//Add the close button "Close"
		closeButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		closeButton.setText( titlesI18N.closeButtonTitle() );
		closeButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				hide();
			}
		} );
	}
	
	/**
	 * Allows to add the default control panel with the Close, Next, Previous and progress bar.
	 */
	protected void addDefaultControlPanel() {
		//First add the navigation buttons and the progress bar panel
		addNewGrid( 1, 4, false, "", false);
		
		//(ROW 01) Add navigation buttons: "Previous"/"Next"
		this.addToGrid( FIRST_COLUMN_INDEX, closeButton, false, false );
		this.addToGrid( SECOND_COLUMN_INDEX, progressBarUI, false, false );
		this.addToGrid( THIRD_COLUMN_INDEX, previousButton, false, false );
		this.addToGrid( FOURTH_COLUMN_INDEX, nextButton, false, false );
	}
	
	/**
	 * The constructor of the data navigation dialog. Does not initialize
	 * the data table. Assumes a multiple data row selector (check boxes).
	 * @param hasSmileyTarget must be true if the dialog contains smiley selection targets, instances. 
	 * @param autoHide if the dialog is autohide then true
	 * @param modal if the dialog is modal then true
	 * @param forUserID the id of the user we look data for
	 * @param parentDialog the parent dialog if any
	 */
	public PagedActionGridDialog( final boolean hasSmileyTarget, boolean autoHide,
								  boolean modal, final int forUserID,
								  final DialogBox parentDialog ) {
		this( hasSmileyTarget, autoHide, modal, forUserID, parentDialog, false );
	}
	
	/**
	 * The constructor of the data navigation dialog. Initializes the data table.
	 * @param hasSmileyTarget must be true if the dialog contains smiley selection targets, instances. 
	 * @param autoHide if the dialog is autohide then true
	 * @param modal if the dialog is modal then true
	 * @param numberOfRows the number of rows per page
	 * @param numberOfColumns the number of columns in the data table
	 * @param forUserID the id of the user we look data for
	 * @pram parentDialog the parent dialog if any
	 * @param isSelectSingle true if we need only a single row
	 * selector in the data table, otherwise false  
	 */
	public PagedActionGridDialog( final boolean hasSmileyTarget, final boolean autoHide,
								  final boolean modal, final int numberOfRows,
								  final int numberOfColumns, final int forUserID,
								  final DialogBox parentDialog, final boolean isSelectSingle ) {
		this( hasSmileyTarget, autoHide, modal, forUserID, parentDialog, isSelectSingle );
		allocateDataTable( numberOfRows, numberOfColumns);
	}
	
	/**
	 * The constructor of the data navigation dialog. Initializes the
	 * data table. Assumes a multiple data row selector (check boxes).
	 * @param hasSmileyTarget must be true if the dialog contains smiley selection targets, instances. 
	 * @param autoHide if the dialog is autohide then true
	 * @param modal if the dialog is modal then true
	 * @param numberOfRows the number of rows per page
	 * @param numberOfColumns the number of columns in the data table
	 * @param forUserID the id of the user we look data for
	 * @pram parentDialog the parent dialog if any
	 */
	public PagedActionGridDialog( final boolean hasSmileyTarget, final boolean autoHide,
								  final boolean modal, final int numberOfRows,
								  final int numberOfColumns, final int forUserID,
								  final DialogBox parentDialog) {
		this( hasSmileyTarget, autoHide, modal, numberOfRows, numberOfColumns, forUserID, parentDialog, false );
	}
	
	/**
	 * This method allocates the data table
	 * @param numberOfRows the number of rows per page
	 * @param numberOfColumns the number of columns in the data table
	 */
	public void allocateDataTable(final int numberOfRows, final int numberOfColumns){
		NUMBER_OF_COLUMNS = numberOfColumns;
		NUMBER_OF_ROWS_PER_PAGE = numberOfRows;
		//Make the blank table with the requires number of columns and rows
		//The latter gets on extra row for the titles
		dataTable.setSize("100%", "100%");
		dataTable.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_STYLE_NAME );
		for(int row = 0; row <= NUMBER_OF_ROWS_PER_PAGE; row++) {
			dataTable.insertRow( row );
			for( int column=0; column < NUMBER_OF_COLUMNS; column++ ) {
				dataTable.insertCell( row, column );
				dataTable.setWidget(row, column, new HTML("&nbsp;") );
			}
			dataTable.setWidget(row, 0, new HTML("&nbsp;"));
		}
	}
	
	/**
	 * @return the total number of data pages
	 */
	protected int getNumberOfPages(){
		return numberOfPages;
	}
	
	/**
	 * @return the current data page number
	 */
	protected int getCurrentPageNumber() {
		return currentPageNumber;
	}
	
	/**
	 * @return allows to return the current number of the entries
	 * that can be displayes in the dialog, page by page
	 */
	protected int getNumberOfEntries() {
		return numberOfEntries;
	}
	
	/**
	 * @param numberOfPages the total number of data pages
	 */
	protected void setNumberOfPages(final int numberOfPages){
		this.numberOfPages = numberOfPages;
	}
	
	/**
	 * @param currentPageNumber the current data page number
	 */
	protected void setCurrentPageNumber(final int currentPageNumber){
		this.currentPageNumber = currentPageNumber;
	}
	
	/**
	 * @param numberOfEntries the new number of entries to browse
	 * WARNING: Does update change the page count and the current page if needed!!!
	 */
	protected void setNumberOfEntries( final int numberOfEntries ) {
		this.numberOfEntries = numberOfEntries;
		//Update pages count and the current page
		numberOfPages = (int) (numberOfEntries / NUMBER_OF_ROWS_PER_PAGE);
		if( numberOfPages == 0 ) {
			numberOfPages += 1;
		} else {
			numberOfPages += ( (numberOfPages < ((double) numberOfEntries / (double) NUMBER_OF_ROWS_PER_PAGE) ) ? 1 : 0 );
		}
		//Check if the current page is within the page limit
		//Since we update the curent page after adding and deleting
		//rooms, we have to make sure we stay within the pages limit
		if( currentPageNumber > numberOfPages ){
			currentPageNumber = numberOfPages;
		}
	}

	/**
	 * Enable/Disable Next/Previous dialog buttons
	 */
	protected void setEnabledNPButtons( final boolean enablePrevious, final boolean enableNext ){
		Scheduler.get().scheduleDeferred( new ScheduledCommand(){
			public void execute() {
				previousButton.setEnabled( enablePrevious );
				nextButton.setEnabled( enableNext );
			}
		});
	}
	
	/**
	 * This method is called when the controls should be enabled again
	 */
	protected void enableAllControls() {
		//NOTE: the Close button should always be enabled
		setEnabledNPButtons( isPreviousEnbl(), isNextEnbl() );
		enableDataRowSelectors(true);
		enableControls( true );
	}
	
	/**
	 * This method is called when the controls should be disabled again
	 */
	protected void disableAllControls() {
		setEnabledNPButtons( false, false );
		enableDataRowSelectors(false);
		enableControls( false );
	}
	
	/**
	 * This method allows to check if there were any data
	 * elements selected by the check boxes of the dialog
	 * @return true if there are selected data elements
	 */
	protected boolean isSelectedData(){
		return !dataIDS.isEmpty();
	}
	
	/**
	 * Returns the list of IDs of the selected data entries
	 * @return the list with the integer IDs of the selected data entries
	 */
	protected List<Integer> getSelectedDataIDs(){
		return dataIDS;
	}
	
	/**
	 * Returns the list of names of the selected data entries
	 * @return the list with the string names of the selected data entries
	 */
	protected List<String> getSelectedDataNames(){
		return dataNames;
	}
	
	/**
	 * Adds a selector column into the data table
	 * @param row the data table row to add it to
	 * @param column the data table column to add it to
	 */
	protected void addSelectorColumnTitleToDataTable(final int row, final int column) {
		if ( isSelectSingle ) {
			Label selectLabel = new Label( titlesI18N.selectorColumnTitle() );
			selectLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
			dataTable.setWidget( row, column, selectLabel );
		} else {
			selectAllCheckBox.setText(titlesI18N.selectorColumnTitle());
			selectAllCheckBox.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
			selectAllCheckBox.setValue(false);
			selectAllCheckBox.setEnabled(false);
			//Add the click listener that add remove room ids to the list 
			selectAllCheckBox.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent e){
					if( (selectorWidgetList != null) && (dataIDS != null) && (dataNames != null) && ( pageData != null) ) {
						if( selectAllCheckBox.getValue() ) {
							//Check all rooms
							dataIDS.clear();
							dataNames.clear();
							if( (pageData != null) && (pageData.entries != null) ) {
								for(int i = 0 ; i < pageData.entries.size(); i++ ) {
									selectorWidgetList[i].setValue(true);
									dataIDS.add( getDataEntryID( pageData, i ) );
									dataNames.add( getDataEntryName( pageData, i ) );
								}
							}
						} else {
							//Uncheck all rooms 
							for(int i=0; i < selectorWidgetList.length; i++) {
								//The number of elements in the checkbox array equals 
								//to the max number of entries in on the page, thus it
								//can be larger than the actual number of boxes
								if( selectorWidgetList[i] != null ) {
									selectorWidgetList[i].setValue(false);
								}
							}
							dataIDS.clear();
							dataNames.clear();
						}
					}
				}
			});
			dataTable.setWidget( row, column, selectAllCheckBox );
		}
	}
	
	/**
	 * Adds a new selector into the data table at the given position row/column
	 * Also, we should know the index of the selector which is the index of the
	 * data row, when counting starting at zerro.
	 * @param row the row in the data table
	 * @param column the column in the data table
	 * @param index the index of the selector, the meaningful data row index minus one 
	 * @param dataID the id of the data entry, to be added/deleted to/from dataIDS array
	 * @param dataName the name of the given data entry, e.g. a user login name
	 */
	protected void addSelectorIntoDataTableRow(final int row, final int column, final int index,
										final Integer dataID, final String dataName) {
		final CheckBox selector;
		if ( isSelectSingle ) {
			selector = new RadioButton( RADIO_BUTTONS_GROUP_NAME, "" );
		} else {
			selector = new CheckBox();
		}
		//Add the click listener that add/remove ids to/from the data list 
		selector.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) {
				if( isSelectSingle && selector.getValue() ) {
					dataIDS.clear();
					dataNames.clear();
				}
				//Now either add or remove the id/name to the lists of selected ids/names 
				if( selector.getValue() ){
					dataIDS.add( dataID );
					dataNames.add( dataName );
				} else {
					if( ! isSelectSingle ) {
						//If we do not select one then we should uncheck the select
						//all check box in the table header.
						selectAllCheckBox.setValue(false);
					}
					dataIDS.remove( (Object) dataID );
					dataNames.remove( (Object) dataName );
				}
			}
		});
		this.selectorWidgetList[index] = selector;
		dataTable.setWidget( row, column, selector );
	}

	
	/**
	 * @return true if the previous button should be enabled
	 */
	private boolean isPreviousEnbl() {
		return currentPageNumber > 1;
	}

	/**
	 * @return true if the next button should be enabled
	 */
	private boolean isNextEnbl() {
		return currentPageNumber < numberOfPages;
	}
	
	/**
	 * Allows to navigate to a particular page of data
	 * @param nextPageExp the page to navigate to
	 */
	private void navigateData( final int nextPageExp ){
		//In Opera the buttons somehow do not get disabled, so I add an
		//exra check for going to the next and previous pages here
		final int nextPage;
		if( nextPageExp <= 0 ) {
			nextPage = 1;
		} else {
			if ( nextPageExp > this.numberOfPages ) {
				nextPage = this.numberOfPages;
			} else {
				nextPage = nextPageExp;
			}
		}
		//Disable buttons for now
		disableAllControls();
		
		//Ensure lazy loading
		(new SplitLoad( true ){
			@Override
			public void execute() {
				CommStatusAsyncCallback<OnePageViewData<T>> callback = new CommStatusAsyncCallback<OnePageViewData<T>>(progressBarUI) {
					public void onSuccessAct(OnePageViewData<T> result) {
						//Update the page number
						currentPageNumber = nextPage;
						//Before table data update
						beforeTableDataUpdateInternal();
						//Update the page title
						updateDialogTitle();
						//Update the table
						updateDataTable(result);
						//Enable the buttons back
						enableAllControls();
					}
					public void onFailureAct(final Throwable caught) {
						(new SplitLoad( true ) {
							@Override
							public void execute() {
								//But if there were some errors, we will still show them
								ErrorMessagesDialogUI.openErrorDialog( caught );
							}
						}).loadAndExecute();
						//Use the recovery action
						recover();
					};
				};
				
				try{
					int offset = ( nextPage - 1 ) * NUMBER_OF_ROWS_PER_PAGE;
					browse( SiteManager.getUserID(), SiteManager.getUserSessionId(), forUserID, offset, NUMBER_OF_ROWS_PER_PAGE, callback);
				} catch ( SiteException e ){
					//Use the on failure method
					callback.onFailure( e );
				}
			}
			@Override
			public void recover() {
				//Enable the buttons back
				enableAllControls();
			}
		}).loadAndExecute();
	}
	
	/**
	 * Allows to update the actual data of the paged dialog.
	 * Retrieves the number of data pages and navigates to the currently selected page, if it is possible
	 */
	public void updateActualData() {
		//Disable buttons for now
		disableAllControls();
		
		//Ensure lazy loading
		(new SplitLoad( true ){
			@Override
			public void execute() {
				//Create the call-back objects
				CommStatusAsyncCallback<Integer> callbackCount = new CommStatusAsyncCallback<Integer>(progressBarUI) {
					public void onSuccessAct(Integer size) {
						//Set the number of found entries
						setNumberOfEntries( size );
						//Update the dialog title
						updateDialogTitle();
						//Retrieve the first page data
						navigateData( currentPageNumber );
					}
					public void onFailureAct(final Throwable caught) {
						(new SplitLoad( true ) {
							@Override
							public void execute() {
								//Report the error
								ErrorMessagesDialogUI.openErrorDialog( caught );
							}
						}).loadAndExecute();
						//Use the recover action
						recover();
					};
				};

				try {
					//Get the count and then call the browse for the first page view
					count( SiteManager.getUserID(), SiteManager.getUserSessionId(), forUserID, callbackCount);
				} catch ( SiteException e){
					//Use the on failure method
					callbackCount.onFailure( e );
				}
			}
			@Override
			public void recover() {
				//Enable the buttons back
				enableAllControls();
			}
		}).loadAndExecute();
	}
	
	//Stores the current number of rows used in the table. We 
	//only count the rows with data, heading row is not included. 
	private int currentNumberOfRows = 0; 
	//The current page data
	protected OnePageViewData<T> pageData = null;
	
	/**
	 * This method is called when a new data arrives and needs to be displayed in the dataTable
	 * @param result the resulting object that needs to be custed to a concrete type
	 */
	protected void updateDataTable(final OnePageViewData<T> result){
		pageData = result;
		
		//In case we need to say something if there are no browsing results
		if( (pageData != null) && ( currentPageNumber == 1 ) &&
				( pageData.entries == null || pageData.entries.size() == 0 ) ) {
			warnUserNoDataToDisplay();
		}
		
		//Get the new table size
		int requiredNumberOfRows;
		if( ( pageData == null ) || ( pageData.entries == null ) ){
			//In this case we do not need any rows
			requiredNumberOfRows = 0;
		} else {
			requiredNumberOfRows = pageData.entries.size();
			//The next is just a safety check
			if( requiredNumberOfRows > NUMBER_OF_ROWS_PER_PAGE ){
				requiredNumberOfRows = NUMBER_OF_ROWS_PER_PAGE;
			}
		}
		
		//Check if the table needs cleaning
		if( currentNumberOfRows > requiredNumberOfRows ) {
			//Clear the unneeded rows
			for( int row = currentNumberOfRows; row > requiredNumberOfRows  ; row--  ){
				for( int column = 0; column < NUMBER_OF_COLUMNS; column++ ) {
					dataTable.setWidget(row, column, new HTML("&nbsp;") );
				}
			}
		}
		
		//Re-fill the rows with the data if it is present
		if( ( pageData != null ) && ( pageData.entries != null ) && (!pageData.entries.isEmpty()) ) {
			for(int row = 1; row <= requiredNumberOfRows; row++) {
				addRowToTable( row, row - 1, result );
			}
		}
		
		//Store the current number of rows
		currentNumberOfRows = requiredNumberOfRows;
		
		//Center the dialog only if it is shown and is visible, because otherwise
		//it can be closed already but then we re-open it by calling center!
		if( this.isVisible() && this.isShowing() ) {
			center();
		}
	}

	/**
	 * Allows to enable/disable all of the current page's check boxes
	 * @param enabled true to enable, otherwise false
	 */
	private void enableDataRowSelectors(boolean enabled){
		selectAllCheckBox.setEnabled( enabled );
		//In case this method is called before there are any checkboxes
		if( selectorWidgetList != null ) {
			for(int i=0; i < selectorWidgetList.length; i++){
				if( selectorWidgetList[i] != null ) {
					selectorWidgetList[i].setEnabled(enabled);
				}
			}
		}
	}
	
	private final void beforeTableDataUpdateInternal(){
		//Uncheck the select/deselect all checkbox, because we are at a new page now 
		selectAllCheckBox.setValue(false);
		//Reinitialize the array of check boxes (radio buttons)
		this.selectorWidgetList = new CheckBox[NUMBER_OF_ROWS_PER_PAGE];
		//Reinitialize the list of checked rooms
		this.dataIDS = new ArrayList<Integer>();
		//Reinitialize the list of checked data-entry names (e.g. user login names)
		this.dataNames = new ArrayList<String>();
		//Now it is time to do something that we want to be done before the table data is updated
		beforeTableDataUpdate();
	}
	
	/**
	 * Allows to update the dialog title using the current data, i.e.
	 * the number of entries and the page cont with the current page index
	 */
	protected final void updateDialogTitle() {
		updateDialogTitle( numberOfEntries, numberOfPages, currentPageNumber );
	}
	
	/**
	 * This method is called when the first page contains no data.
	 * This is a chance to notify the user that there is not data to display.
	 * If the latter has to be done then the method should be overriden in a subclass. 
	 */
	protected void warnUserNoDataToDisplay(){
	}

	/**
	 * This method should be implemented to allow retrieval of the id of the data entry with the
	 * given index. Has to have a non empty implementation only if the check boxes column is added.
	 * @param onePageData the one page data entries
	 * @param index the index of the data entry we need to get ID of
	 * @return the ID of the data entry
	 */
	protected abstract Integer getDataEntryID(OnePageViewData<T> onePageData, final int index);
	
	/**
	 * This method should be implemented to allow retrieval of the name of the data entry with the
	 * given index. Has to have a non empty implementation only if the names are used.
	 * @param onePageData the one page data entries
	 * @param index the index of the data entry we need to get ID of
	 * @return the ID of the data entry
	 */
	protected abstract String getDataEntryName(OnePageViewData<T> onePageData, final int index);
	
	/**
	 * This virtual call back method allows to retrieve the user data for provided parameters
	 * @param userID the id of the logged in user who uses the interface
	 * @param userSessionID the session id of the logged in user who uses the interface
	 * @param forUserID the id of the user we want to browse data for.
	 * @param offset the offset of the data
	 * @param number_or_rows_per_page the max number of data elements that can be returned
	 * @param callback the call back object for the remote call
	 * @throws SiteException if something goes wrong
	 */
	protected abstract void browse(final int userID, final String userSessionID,
								final int forUserID, final int offset,
								final int number_or_rows_per_page,
								AsyncCallback<OnePageViewData<T>> callback) throws SiteException;

	/**
	 * This virtual call back method allows to retrieve count of the entries
	 * of data, which we want to navigate through.
	 * @param userID the id of the logged in user who uses the interface
	 * @param userSessionID the session id of the logged in user who uses the interface
	 * @param forUserID the id of the user we want to browse data for.
	 * @param callback the call back object for the remote call
	 * @throws SiteException if something goes wrong
	 */
	protected abstract void count( final int userID, final String userSessionID, final int forUserID,
									AsyncCallback<Integer> callback) throws SiteException;

	/**
	 * This method is called when the child dialog controls have to be distabled or enabled
	 * @param enabled true if the controls should be enabled, otherwise false
	 */
	protected abstract void enableControls( final boolean enabled );
	
	/**
	 * This method is called when we navigate to a new page or
	 * in a similar case, when the dialog title need an update
	 * @param numberOfEntries the total number of selected entries
	 * @param numberOfPages the total number of pages
	 * @param currentPageNumber the current page index
	 * @param numberOfEntries the total number of found entries
	 */
	protected abstract void updateDialogTitle( final int numberOfEntries, final int numberOfPages, final int currentPageNumber );
	
	/**
	 * This method is called when a new row has to be added to the table
	 * @param row the row index to work with
	 * @param index the index of the object in the OnePageViewData.entries
	 * list that has to  be displayed
	 * @param the OnePageViewData object 
	 */
	protected abstract void addRowToTable( final int row, final int index, final Object result);
	
	/**
	 * This method is called then the new page data is obtained
	 * and we are about to rewrite the table data
	 */
	protected abstract void beforeTableDataUpdate();
}
