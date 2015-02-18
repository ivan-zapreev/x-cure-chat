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

import java.util.List;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.FlexTable; 
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.xcurechat.client.SiteManager;

import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.data.search.OnePageViewData;
import com.xcurechat.client.data.search.UserSearchData;

import com.xcurechat.client.dialogs.profile.ViewUserProfileDialogUI;
import com.xcurechat.client.dialogs.system.messages.InfoMessageDialogUI;

import com.xcurechat.client.i18n.I18NManager;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.rpc.UserManagerAsync;

import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.Button;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.TextBaseTranslitAndProgressBar;
import com.xcurechat.client.utils.widgets.TextBoxWithSuggText;

/**
 * @author zapreevis
 * This class is supposed to be the super class for all user search dialogs
 */
public abstract class UserSearchDialog extends PagedActionGridDialog<ShortUserData> {
	
	//The maximum length of the search text box in characters 
	private static final int SEARCH_TEXT_BOX_LENGTH_CHARACTERS = 45;
	
	//The number of columns in the data table
	private static final int NUMBER_OF_DATA_COLUMNS = 6;
	
	//The search text box
	private TextBoxWithSuggText searchTextBox = new TextBoxWithSuggText( titlesI18N.searchAnyTextHelperMessage() );
	//The age selector
	private final ListBox ageListBox = new ListBox();
	//The gender selector
	private final ListBox genderListBox = new ListBox();
	
	//The user gender strings
	private final String USER_GENDER_UNKNOWN_STR;
	private final String USER_GENDER_MALE_STR;
	private final String USER_GENDER_FEMALE_STR;

	//Delete and Add buttons for deleting and adding users
	private final Button actionButton = new Button();
	private final Button searchButton = new Button();

	//The check boxes for what to search
	protected CheckBox loginCheckBox = new CheckBox();
	protected CheckBox firstNameCheckBox = new CheckBox();
	protected CheckBox lastNameCheckBox = new CheckBox();
	protected CheckBox cityCheckBox = new CheckBox();
	protected CheckBox countryCheckBox = new CheckBox();
	protected CheckBox aboutMeCheckBox = new CheckBox();
	protected CheckBox onlineStatusCheckBox = new CheckBox();
	protected CheckBox friendCheckBox = new CheckBox();
	protected CheckBox picturesCheckBox = new CheckBox();
	
	//The number of result rows per page
	private final int NUMBER_OF_ROWS_PER_PAGE;
	
	//If there action button text is null, i.e. there is no extra
	//action in the search dialog, then the variable is set to
	//false. Then there is no select column in the data table.
	private boolean doNeedSelectColumn = ( actionButtonText() != null );

	/**
	 * The user search constructor. Allows to provide the type of
	 * user selection: single or multiple users. The latter is needed
	 * if we select users when adding them to rooms, or writing messages.
	 * @param numberOfRows the number of result rows in the user rearch, per page.
	 * @param parentDialog the parent dialog, from which we open this one.
	 * @param isSelectSingle true if we allow for a single user selection only
	 */
	public UserSearchDialog(final int numberOfRows, final DialogBox parentDialog, final boolean isSelectSingle) {
		super(true, true, true, SiteManager.getUserID(), parentDialog, isSelectSingle);
		
		//Store the needed param values
		NUMBER_OF_ROWS_PER_PAGE = numberOfRows;

		USER_GENDER_UNKNOWN_STR = titlesI18N.genderUnknownValue();
		USER_GENDER_MALE_STR = titlesI18N.genderMaleValue();
		USER_GENDER_FEMALE_STR = titlesI18N.genderFemaleValue();

		//Enable the default action buttons it will be used to close the dialog
		setLeftEnabled( true );
		
		this.setStyleName( CommonResourcesContainer.USER_DIALOG_STYLE_NAME );
	}

	/**
	 * The user search constructor. Assumes selection of multiple users.
	 * @param numberOfRows the number of result rows in the user rearch, per page.
	 * @param parentDialog the parent dialog, from which we open this one.
	 */
	public UserSearchDialog(final int numberOfRows, final DialogBox parentDialog ) {
		this( numberOfRows, parentDialog, false );
	}
	
	@Override
	protected void populateDialog() {
		//Allocate the appropriate data table
		allocateDataTable( NUMBER_OF_ROWS_PER_PAGE, ( doNeedSelectColumn ? NUMBER_OF_DATA_COLUMNS : NUMBER_OF_DATA_COLUMNS-1 ) );

		//First add the grid with the search text box and the query selectors
		addNewGrid( 2, false, "", false);
		
		//(ROW 01) Add the search field
		HorizontalPanel zeroPanel = new HorizontalPanel();
		zeroPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		zeroPanel.setWidth("100%");
		Label searchFieldTitle = InterfaceUtils.getNewFieldLabel( titlesI18N.queryStringFieldTitle(), true );
		zeroPanel.add( searchFieldTitle );
		searchTextBox.addStyleName( CommonResourcesContainer.SEARCH_QUERY_TEXT_BOX_STYLE_NAME );
		searchTextBox.setVisibleLength( SEARCH_TEXT_BOX_LENGTH_CHARACTERS );
		searchTextBox.addKeyDownHandler( new KeyDownHandler(){
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
					//Activate search on pressing enter in the search string text box
					doSearch();
					//Prevent the event from being propagated and from its default action
					event.preventDefault();
					event.stopPropagation();
				}
			}
		});
		zeroPanel.add( new TextBaseTranslitAndProgressBar( searchTextBox, UserSearchData.MAX_SEARCH_STRING_LENGTH ) );
		addToGrid( this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, zeroPanel, false, false );
		
		//(ROW 02) Add the Login, Name, Last Name, City, Country
		HorizontalPanel firstPanel = new HorizontalPanel();
		firstPanel.setWidth("100%");
		firstPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		loginCheckBox.setText( titlesI18N.loginNameField() );
		loginCheckBox.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
		loginCheckBox.setValue(true);
		firstPanel.add( loginCheckBox );
		firstNameCheckBox.setText( titlesI18N.firstNameField() );
		firstNameCheckBox.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
		firstNameCheckBox.setValue(true);
		firstPanel.add( firstNameCheckBox );
		lastNameCheckBox.setText( titlesI18N.lastNameField() );
		lastNameCheckBox.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
		lastNameCheckBox.setValue(true);
		firstPanel.add( lastNameCheckBox );
		cityCheckBox.setText( titlesI18N.cityField() );
		cityCheckBox.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
		cityCheckBox.setValue(true);
		firstPanel.add( cityCheckBox );
		countryCheckBox.setText( titlesI18N.countryField() );
		countryCheckBox.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
		countryCheckBox.setValue(true);
		firstPanel.add( countryCheckBox );
		aboutMeCheckBox.setText( titlesI18N.aboutMeField() );
		aboutMeCheckBox.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
		aboutMeCheckBox.setValue(true);
		firstPanel.add( aboutMeCheckBox );
		addToGrid( this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, firstPanel, true, false );
		
		//First add the grid with the age/gender selectors
		addNewGrid( 1, 5, false, "", false);
		
		//(ROW 01) Add the gender and age selectors
		HorizontalPanel secondPanel = new HorizontalPanel();
		secondPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		Label ageFieldTitle = InterfaceUtils.getNewFieldLabel( titlesI18N.ageField(), false );
		secondPanel.add( ageFieldTitle );
		ageListBox.addItem(UserData.AGE_UNKNOWN_STR);
		ageListBox.addItem(UserData.AGE_UNDER_TO_18_STR);
		ageListBox.addItem(UserData.AGE_18_TO_21_STR);
		ageListBox.addItem(UserData.AGE_21_TO_25_STR);
		ageListBox.addItem(UserData.AGE_25_TO_30_STR);
		ageListBox.addItem(UserData.AGE_30_TO_35_STR);
		ageListBox.addItem(UserData.AGE_35_TO_45_STR);
		ageListBox.addItem(UserData.AGE_45_TO_55_STR);
		ageListBox.addItem(UserData.AGE_ABOVE_55_STR);
		ageListBox.setVisibleItemCount(1);
		secondPanel.add( ageListBox );
		addToGrid( FIRST_COLUMN_INDEX, secondPanel, false, false );
		
		HorizontalPanel thirdPanel = new HorizontalPanel();
		thirdPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		Label genderFieldTitle = InterfaceUtils.getNewFieldLabel( titlesI18N.genderField(), false );
		thirdPanel.add( genderFieldTitle );
		genderListBox.addItem( USER_GENDER_UNKNOWN_STR );
		genderListBox.addItem( USER_GENDER_MALE_STR );
		genderListBox.addItem( USER_GENDER_FEMALE_STR );
		genderListBox.setVisibleItemCount(1);
		thirdPanel.add( genderListBox );
		addToGrid( SECOND_COLUMN_INDEX, thirdPanel, false, false );
		
		//Add the friend check box
		friendCheckBox.setText( titlesI18N.friendCheckBoxTitleName() );
		friendCheckBox.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
		addToGrid( THIRD_COLUMN_INDEX, friendCheckBox, false, false );

		//Add the pictures check box
		picturesCheckBox.setText( titlesI18N.picturesCheckBoxTitleName() );
		picturesCheckBox.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
		addToGrid( FOURTH_COLUMN_INDEX, picturesCheckBox, false, false );

		//Add the pictures check box
		onlineStatusCheckBox.setText( titlesI18N.onlineCheckBoxTitleName() );
		onlineStatusCheckBox.setStyleName( CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE );
		addToGrid( FIFTH_COLUMN_INDEX, onlineStatusCheckBox, false, false );

		//First add the grid with the rest of the elements
		addNewGrid( 2, false, "", false);
		
		//(ROW 01) Add the "action after search" and "search" buttons
		if( doNeedSelectColumn ) {
			actionButton.setText( actionButtonText() );
			actionButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
			actionButton.addClickHandler( new ClickHandler() {
				public void onClick(ClickEvent e) {
					actionButtonAction( getSelectedDataIDs(), getSelectedDataNames() );
				}
			});
			addToGrid( FIRST_COLUMN_INDEX, actionButton, false, false );
		} else {
			addToGrid( FIRST_COLUMN_INDEX, new Label(""), false, false );
		}
		searchButton.setText( titlesI18N.searchButtonText() );
		searchButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		searchButton.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent e) {
				doSearch();
			}
		});
		addToGrid( SECOND_COLUMN_INDEX, searchButton, false, true );
		
		//(ROW 02) Add the search result table
		dataTable.setSize("100%", "100%");
		addDataTableColumnTitles( dataTable );
		addToGrid(this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, dataTable, true, false);
		
		//First add the navigation buttons and the progress bar panel
		addDefaultControlPanel();
		
		//Add the click listeners to the check boxes, we should disable the
		//search textbox when there are no search checkboxes checked
		addSearchModifiersClickListeners();
		
		//Disable all controls because there will be default search first
		disableAllControls();
		//Retrieve initial data
		updateActualData();
	}

	/**
	 * We add click listeners to the checkboxes that make sure that if
	 * we do not specify search fields, then we disable the query TextBox
	 */
	public void addSearchModifiersClickListeners(){
		ClickHandler listener = new ClickHandler(){
			public void onClick( ClickEvent e ){
				boolean enabled = loginCheckBox.getValue() ||
									firstNameCheckBox.getValue() ||
									lastNameCheckBox.getValue() ||
									cityCheckBox.getValue() ||
									countryCheckBox.getValue() ||
									aboutMeCheckBox.getValue();
				searchTextBox.setEnabled(enabled);
			}
		};
		loginCheckBox.addClickHandler(listener);
		firstNameCheckBox.addClickHandler(listener);
		lastNameCheckBox.addClickHandler(listener);
		cityCheckBox.addClickHandler(listener);
		countryCheckBox.addClickHandler(listener);
		aboutMeCheckBox.addClickHandler(listener);
	}
	
	/**
	 * Initiate search, based on the data set in the dialog fields;
	 */
	private void doSearch() {
		//We simply retrieve the dialog's initial data as on its first load
		updateActualData();
	}
	
	/**
	 * This method is supposed to update the search object data with
	 * the search parameters set in the dialog.
	 * @param searchData the user search query data object
	 */
	private void completeSearchData(  UserSearchData searchData ) {
		searchData.searchString = searchTextBox.getText();
		searchData.isLogin = loginCheckBox.getValue();
		searchData.isFirstName = firstNameCheckBox.getValue();
		searchData.isLastName = lastNameCheckBox.getValue();
		searchData.isCity = cityCheckBox.getValue();
		searchData.isCountry = countryCheckBox.getValue();
		searchData.isAboutMyself = aboutMeCheckBox.getValue();
		
		searchData.isOnline = onlineStatusCheckBox.getValue();
		searchData.isFriend = friendCheckBox.getValue();
		searchData.hasPictures = picturesCheckBox.getValue();
		
		//Get the user age for the search
		int index = ageListBox.getSelectedIndex();
		searchData.userAgeIntervalID = UserData.stringToUserAge( ageListBox.getItemText(index) );

		//Get user gender for the search
		index = genderListBox.getSelectedIndex();
		final String genderStr = genderListBox.getItemText(index);
		if( genderStr.equals( USER_GENDER_MALE_STR ) ) {
			searchData.userGender = UserSearchData.USER_GENDER_MALE;
		} else {
			if( genderStr.equals( USER_GENDER_FEMALE_STR ) ) {
				searchData.userGender = UserSearchData.USER_GENDER_FEMALE;
			} else {
				searchData.userGender = UserSearchData.USER_GENDER_UNKNOWN;
			}
		}
	}
	
	@Override
	protected void enableControls( final boolean enabled ) {
		//The right action button of the dialog, although
		//not added, will be used to initiate search query
		setRightEnabled( enabled );
		
		//Enable this dialog's TextBoxes, CheckBoxes and ListBoxes
		searchTextBox.setEnabled(enabled);
		ageListBox.setEnabled(enabled);
		genderListBox.setEnabled(enabled);
		actionButton.setEnabled(enabled);
		searchButton.setEnabled(enabled);
		loginCheckBox.setEnabled(enabled);
		firstNameCheckBox.setEnabled(enabled);
		lastNameCheckBox.setEnabled(enabled);
		cityCheckBox.setEnabled(enabled);
		countryCheckBox.setEnabled(enabled);
		onlineStatusCheckBox.setEnabled(enabled);
		friendCheckBox.setEnabled(enabled);
		picturesCheckBox.setEnabled(enabled);
	}
	
	@Override
	protected void beforeTableDataUpdate(){
		//Do nothing
	}

	@Override
	protected void actionLeftButton() {
		//close the dialog
		hide();
	}

	@Override
	protected void actionRightButton() {
		//Search users
		doSearch();
	}
	
	/**
	 * This method is called when it is time to fill in the column titles of the data table 
	 * @param dataTable the data table to add columns to
	 */
	protected void addDataTableColumnTitles(FlexTable dataTable) {
		//Fill the table headings
		int column = 0;
		dataTable.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_STYLE_NAME );
		
		//Add the entry index column title
		Label roomNumberLabel = new Label(titlesI18N.indexColumnTitle());
		roomNumberLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, column++, roomNumberLabel );
		
		//Add the select data entries column to the data table
		if( doNeedSelectColumn ) {
			addSelectorColumnTitleToDataTable(0, column++);
		}
		
		//Add the gender column title
		Label genderLabel = new Label(titlesI18N.genderColumnTitle());
		genderLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, column++, genderLabel );
		
		//Add the online/offline status column title
		Label statusLabel = new Label( titlesI18N.statusColumnTitle() );
		statusLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, column++, statusLabel );
		
		//Add the last online date column title
		Label lastOnlineLabel = new Label( titlesI18N.lastOnlineFieldTitle() );
		lastOnlineLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, column++, lastOnlineLabel );
		
		//Add the user login name column title
		Label userLoginLabel = new Label(titlesI18N.userLoginColumnTitle());
		userLoginLabel.setStyleName( CommonResourcesContainer.PAGED_DIALOG_TABLE_LABEL_STYLE );
		dataTable.setWidget(0, column++, userLoginLabel );
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected void addRowToTable(int row, int index, Object result) {
		//The column index
		int column = 0;
		
		//Convert the object data
		OnePageViewData<ShortUserData> data = (OnePageViewData<ShortUserData>) result;
		final ShortUserData shortUserData = data.entries.get( index );

		//Set the index of the entry
		int userNumber = ( ( getCurrentPageNumber() - 1 ) * NUMBER_OF_ROWS_PER_PAGE) + ( index + 1);
		dataTable.setWidget( row, column++, new Label( Integer.toString( userNumber ) ) );
		
		//Add the check box into the rom/column, it should mark the 
		//data entry (short user data) with the given ID and name. 
		if( doNeedSelectColumn ) {
			addSelectorIntoDataTableRow(row, column++, index, new Integer(shortUserData.getUID()), shortUserData.getUserLoginName());
		}

		//Add the gender column
		final String genderURL, genderTipStr;
		if( shortUserData.isMale() ) {
			genderURL = ServerSideAccessManager.getUserMaleGenderImageURL();
			genderTipStr = titlesI18N.genderMaleValue();
		} else {
			genderURL = ServerSideAccessManager.getUserFemaleGenderImageURL();
			genderTipStr = titlesI18N.genderFemaleValue();
		}
		Image genderImage = new Image(genderURL);
		genderImage.setTitle( genderTipStr );
		genderImage.setStylePrimaryName( CommonResourcesContainer.USER_SEARCH_DIALOG_STATUS_IMAGE_STYLE_NAME );
		dataTable.setWidget( row, column++, genderImage );
		
		//Add the online/offline status column
		final String statusURL, statusTipStr;
		if( shortUserData.isOnline() ) {
			statusURL = ServerSideAccessManager.getUserOnlineStatusImageURL();
			statusTipStr = titlesI18N.userOnlineStatus();
		} else {
			statusURL = ServerSideAccessManager.getUserOfflineStatusImageURL();
			statusTipStr = titlesI18N.userOfflineStatus();
		}
		Image statusImage = new Image(statusURL);
		statusImage.setTitle( statusTipStr );
		statusImage.setStylePrimaryName( CommonResourcesContainer.USER_SEARCH_DIALOG_STATUS_IMAGE_STYLE_NAME );
		dataTable.setWidget( row, column++, statusImage );
		
		//Add the last online column value
		Label lastOnlineDataLabel = new Label();
		if( shortUserData.isOnline() ) {
			lastOnlineDataLabel.setText( titlesI18N.userOnlineStatus() );
		} else {
			setDateLabelValue( lastOnlineDataLabel, shortUserData.getUserLastOnlineDate() );
		}
		lastOnlineDataLabel.setStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_STYLE_NAME );
		dataTable.setWidget( row, column++, lastOnlineDataLabel );
		
		//Set the user name/link here, depending o whether we browse by and admin or not we open different dialogs
		Label userNameLink = new Label( shortUserData.getUserLoginName() );
		userNameLink.setStyleName( CommonResourcesContainer.DIALOG_LINK_BLUE_STYLE );
		userNameLink.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e){
				//Ensure lazy loading
				final SplitLoad executor = new SplitLoad( true ) {
					@Override
					public void execute() {
						//Show the user profile
						ViewUserProfileDialogUI userProfile = new ViewUserProfileDialogUI( shortUserData.getUID(),
																							shortUserData.getUserLoginName(),
																							thisDialog, false );
						userProfile.show();
						userProfile.center();
					}
				};
				executor.loadAndExecute();
			}
		});
		dataTable.setWidget( row, column++, userNameLink );
	}
	
	@Override
	protected Integer getDataEntryID(OnePageViewData<ShortUserData> onePageData, final int index){
		return ((ShortUserData) onePageData.entries.get( index )).getUID();
	}
	
	@Override
	protected String getDataEntryName(OnePageViewData<ShortUserData> onePageData, final int index){
		return ((ShortUserData) onePageData.entries.get( index )).getUserLoginName();
	}
	
	@Override
	protected void browse( final int userID, final String userSessionID, final int forUserID,
						   final int offset, final int number_or_rows_per_page,
						   final AsyncCallback<OnePageViewData<ShortUserData>> callback ) throws SiteException {
		final UserSearchData searchData = getExtraUserSearchData();
		completeSearchData( searchData );
		//Ensure lazy loading
		(new SplitLoad(){
			@Override
			public void execute() {
				UserManagerAsync manager = RPCAccessManager.getUserManagerAsync();
				manager.browse( userID, userSessionID, searchData, offset, number_or_rows_per_page, callback );
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
		final UserSearchData searchData = getExtraUserSearchData();
		completeSearchData( searchData );
		searchData.validate();
		//Ensure lazy loading
		(new SplitLoad(){
			@Override
			public void execute() {
				UserManagerAsync manager = RPCAccessManager.getUserManagerAsync();
				manager.count( userID, userSessionID, searchData, callback );
			}
			@Override
			public void recover() {
				callback.onFailure( new InternalSiteException( I18NManager.getErrors().serverDataLoadingFalied() ) );
			}
		}).loadAndExecute();
	}
	
	@Override
	protected void warnUserNoDataToDisplay(){
		(new SplitLoad( true ) {
			@Override
			public void execute() {
				InfoMessageDialogUI.openInfoDialog( I18NManager.getInfoMessages().noSearchResultsForTheQuery() );
			}
		}).loadAndExecute();
	}

	/**
	 * This method has to be overriden by the child class, it's aim
	 * is to return the object with the extra user search parameters.
	 * It should be the object of a class inherited from the UserSearchData
	 * In the implementation of the method user has to set extra parameters.
	 * Within this class the parameters of UserSearchData will be set   
	 * @return the object with the extra parameters for the user search
	 */
	protected abstract UserSearchData getExtraUserSearchData();
	
	/**
	 * This method should provide proper text to the action button
	 * If it returns null then the button is not added to the dialog
	 * @return the title for the action button
	 */
	protected abstract String actionButtonText();
	
	/**
	 * This method should realize the action for the action button of the search dialog
	 * @param userIDS the list of user IDs, since this dialog searches for users.
	 * 					These are the ids of users that are checked by the check boxes.
	 * @param The corresponding user login names, we assume that both lists are ordered
	 */
	protected abstract void actionButtonAction(List<Integer> userIDS, List<String> userLoginNames);

}
