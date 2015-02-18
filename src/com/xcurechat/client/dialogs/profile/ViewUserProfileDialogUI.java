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
package com.xcurechat.client.dialogs.profile;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.CloseEvent;

import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;

import com.xcurechat.client.SiteManager;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.dialogs.ActionGridDialog;
import com.xcurechat.client.dialogs.PagedActionGridDialog;
import com.xcurechat.client.dialogs.messages.SendMessageDialogUI;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.UserManagerAsync;
import com.xcurechat.client.rpc.ServerSideAccessManager;

import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.StringUtils;
import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.widgets.ActionLinkPanel;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.UserAvatarImageWidget;
import com.xcurechat.client.utils.widgets.UserProfileFilesView;

/**
 * @author zapreevis
 * The new-user registration dialog 
 */
public class ViewUserProfileDialogUI extends ActionGridDialog {
		
	//The user id for which the profile info will be retrieved
	final int userID;
	//The user login name, to be used, if the userProfile is not yet there
	final String userLoginName;
	
	//The user profile data, that should be first retrieved from the server
	private UserData userProfileData = null;
	
	private Label ageFieldLabel = null;
	private final Label age = new Label();
	
	private final Label gender = new Label();
	
	private Label firstNameFieldLabel = null;
	private final Label firstName = new Label(); 
	private Label lastNameFieldLabel = null;
	private final Label lastName = new Label(); 
	private Label countryFieldLabel = null;
	private final Label country = new Label();
	private Label cityFieldLabel = null;
	private final Label city = new Label();
	private final Label registrationDate = new Label();
	private final Label onlineStatus = new Label();
	private final Label lastOnlineDate = new Label();
	private DisclosurePanel aboutMyselfPanel = null;
	private final TextArea aboutMyselfDesc = new TextArea();
	
	//The user avatar image widget
	private UserAvatarImageWidget avatarImage = null;
	
	//The action/add friend/remove friend image for loading/adding/removing friend status
	private final AddRemoveFriendManager friendManager;
	
	//The manager for turning the bot on and off
	private TurnBotOnOffManager botActionPanel;
	
	//Is the widget used for viewing profile files
	private final UserProfileFilesView userFilesView;
	
	//If true then the send message link closes the dialog
	//This is done for not letting recursion in send message dialogs
	private final boolean onSendMsgClose;
	
	/**
	 * The main constructor
	 * @param userID the id of the user we want to browse profile for
	 * @param userLoginName the login name of the user we want to browse profile for
	 * @param parentDialog the dialog we opn this dialog from
	 * @param onSendMsgClose close this dialog if some one presses the send message link
	 */
	public ViewUserProfileDialogUI(final int userID, final String userLoginName,
									final DialogBox parentDialog, final boolean onSendMsgClose ) {
		//No autohide, and the dialog is modal
		super( false, true, true, parentDialog );
		
		this.onSendMsgClose = onSendMsgClose;
		//Initialize the user files view
		this.userFilesView = new UserProfileFilesView( this, null );
		
		//In case we were searching for friends it's better
		//if we update the parent's search dialog page on exit. 
		if( ( parentDialog != null ) && ( parentDialog instanceof PagedActionGridDialog<?> ) ){
			this.addCloseHandler( new CloseHandler<PopupPanel>(){
				public void onClose( CloseEvent<PopupPanel> e ) {
					if( e.getTarget() == thisDialog ) {
						((PagedActionGridDialog<?>) parentDialog).updateActualData();
					}
				}
			} );
		}
		
		//Save the user ID and user Login Name
		this.userID = userID;
		this.userLoginName = userLoginName;
		this.friendManager = new AddRemoveFriendManager( userID );
		
		//Set the dialog's caption.
		setText( titlesI18N.viewUserProfileDialogTitle() );
		
		//Set a style name so we can style it with CSS.
		this.setStyleName(CommonResourcesContainer.USER_DIALOG_STYLE_NAME);

		//Enable the action buttons and hot key, even though we will not be adding these buttons
		//to the grid, we will use the hot keys associated with them to close this dialog
		setLeftEnabled( true );
		setRightEnabled( true );
		
		//Fill dialog with data
		populateDialog();
		
		//Get the current MainUserData object from the server
		retrieveUserData();
	}
	
	/**
	 * Allows to update (set) the user's avatar image for the given user profile. 
	 * If the user profile is not loaded yet, then this method sets the default male avatar. 
	 */
	private void updateAvatarImage() {
		final boolean update = (userID == SiteManager.getUserID());
		if( userProfileData != null ) {
			avatarImage.updateAvatarData( userProfileData, update );
		} else {
			avatarImage.updateAvatarImage(userID, true, update);
		}
	}
	
	private void addMainDataFields() {
		//ADD THE MAIN DATA FIELDS
		addNewGrid( 1, 1, false, "", true);
		
		HorizontalPanel mainInfoHPanel = new HorizontalPanel();
		mainInfoHPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE);
		mainInfoHPanel.setSize("100%", "100%");
		
		VerticalPanel mainFieldsPanel = new VerticalPanel();
		mainFieldsPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		mainFieldsPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT);
		
		VerticalPanel mainValuesPanel = new VerticalPanel();
		mainValuesPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		mainValuesPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT);

		//Add the login name field
		Label userLoginNameLabel = new Label(); userLoginNameLabel.setText( userLoginName );
		InterfaceUtils.addFieldValueToPanels( mainFieldsPanel, true, titlesI18N.userFieldName(),
								mainValuesPanel, true, userLoginNameLabel );

		//Add the online status field
		InterfaceUtils.addFieldValueToPanels( mainFieldsPanel, true, titlesI18N.statusColumnTitle(),
								mainValuesPanel, true, onlineStatus );
		
		//Add last online date field
		InterfaceUtils.addFieldValueToPanels( mainFieldsPanel, true, titlesI18N.lastOnlineFieldTitle(),
								mainValuesPanel, true, lastOnlineDate );
		
		//Add the avatar's image
		mainInfoHPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		mainInfoHPanel.add( mainFieldsPanel );
		mainInfoHPanel.add( new HTML("&nbsp;") );
		mainInfoHPanel.add( mainValuesPanel );
		mainInfoHPanel.add( new HTML("&nbsp;") );
		mainInfoHPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT );
		avatarImage = new UserAvatarImageWidget();
		avatarImage.setTitle( titlesI18N.avatarUserFieldName() );
		mainInfoHPanel.add( avatarImage );

		addToGrid( FIRST_COLUMN_INDEX, mainInfoHPanel, false, false );
	}
	
	private void addOptionalDataFields(){
		//ADD THE OPTIONAL FIELDS
		addNewGrid( 1, 1, true, titlesI18N.optionalUserDataFields(), false);
		
		HorizontalPanel optionalHorizPanel = new HorizontalPanel();
		optionalHorizPanel.setSize("80%", "100%");
		optionalHorizPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		VerticalPanel optionalFieldsPanel = new VerticalPanel();
		optionalFieldsPanel.setSize("100%", "100%");
		optionalFieldsPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		VerticalPanel optionalValuesPanel = new VerticalPanel();
		optionalValuesPanel.setSize("100%", "100%");
		optionalValuesPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		optionalHorizPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT);
		optionalFieldsPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT);
		optionalHorizPanel.add( optionalFieldsPanel );
		optionalHorizPanel.add( new HTML("&nbsp;") );
		optionalHorizPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT);
		optionalValuesPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT);
		optionalHorizPanel.add( optionalValuesPanel );
		
		//Add the registration date/time field
		InterfaceUtils.addFieldValueToPanels( optionalFieldsPanel, false, titlesI18N.registrationDateFieldName(),
								optionalValuesPanel, true, registrationDate );
		
		//Add the first name field
		firstNameFieldLabel = InterfaceUtils.addFieldValueToPanels( optionalFieldsPanel, false,
													 titlesI18N.firstNameField(),
													 optionalValuesPanel, false, firstName );
		
		//Add the last name field
		lastNameFieldLabel = InterfaceUtils.addFieldValueToPanels( optionalFieldsPanel, false,
													titlesI18N.lastNameField(),
													optionalValuesPanel, false, lastName );
		
		//Add the age name field
		ageFieldLabel = InterfaceUtils.addFieldValueToPanels( optionalFieldsPanel, false,
											   titlesI18N.ageField(),
											   optionalValuesPanel, false, age );
		
		//Add the gender name field
		InterfaceUtils.addFieldValueToPanels( optionalFieldsPanel, false, titlesI18N.genderField(),
								optionalValuesPanel, false, gender );
		
		//Add the city name field
		cityFieldLabel = InterfaceUtils.addFieldValueToPanels( optionalFieldsPanel, false,
												titlesI18N.cityField(),
												optionalValuesPanel, false, city );
		
		//Add the country name field
		countryFieldLabel = InterfaceUtils.addFieldValueToPanels( optionalFieldsPanel, false,
												   titlesI18N.countryField(),
												   optionalValuesPanel, false, country );
		
		addToGrid( FIRST_COLUMN_INDEX, optionalHorizPanel, false, false );
	}
	
	private void addAboutMyselfPanel() {
		//ADD THE OPTIONAL FIELDS
		aboutMyselfPanel = addNewGrid( 1, 1, true, titlesI18N.aboutMeUserProfilePanel(), false);
		aboutMyselfDesc.setCharacterWidth( UserProfileDialogUI.ABOUT_MYSELF_ONE_LINE_LENGTH );
		aboutMyselfDesc.setVisibleLines( UserProfileDialogUI.ABOUT_MYSELF_VISIBLE_LINES_LENGTH );
		aboutMyselfDesc.addStyleName( CommonResourcesContainer.USER_PROFILE_ABOUT_ME_TEXT_AREA_STYLE );
		aboutMyselfDesc.setReadOnly( true );
		addToGrid( FIRST_COLUMN_INDEX, aboutMyselfDesc, false, false );
		
	}
	
	private void addImagesDataGrid() {
		//Add the grid for the images
		addNewGrid( 1, 1, false, titlesI18N.imagesUserProfilePanel(), false);
		
		//Add the files-view widget
		addToGrid( FIRST_COLUMN_INDEX, userFilesView, false, false );
	}

	private void addSendMsgAddRemoveFriendActionFields() {
		addNewGrid( SiteManager.isAdministrator() ? 2 : 1, 3, false, null, true);
		
		//Check if the user profile view dialog is already opened for some user
		if( SendMessageDialogUI.isSendMessageDialogOpen() ) {
			//Add the disabled send message related elements
			addToGrid( FIRST_COLUMN_INDEX, new ActionLinkPanel( ServerSideAccessManager.getDisabledSendMessageImageURL(), "",
																ServerSideAccessManager.getDisabledSendMessageImageURL(),
																titlesI18N.sendMessageDisabledTipText(),
																titlesI18N.sendMessageText(), null, true, true ), false, false );
		} else {
			//Add the enabled send message related elements
			addToGrid( FIRST_COLUMN_INDEX, new ActionLinkPanel( ServerSideAccessManager.getSendMessageImageURL(), titlesI18N.sendMessageTipText(),
																ServerSideAccessManager.getSendMessageImageURL(), "", titlesI18N.sendMessageText(),
												   				new ClickHandler(){
																	public void onClick(ClickEvent e){
																		if( onSendMsgClose ) {
																			hide();
																		} else {
																			//Ensure lazy loading
																			final SplitLoad executor = new SplitLoad( true ) {
																				@Override
																				public void execute() {
																					SendMessageDialogUI sendDialog = new SendMessageDialogUI( thisDialog, userID, userLoginName, null );
																					sendDialog.show();
																					sendDialog.center();
																				}
																			};
																			executor.loadAndExecute();
																		}
																	}
																}, true, true ), false, false );
		}
		
		//Add the progress bar
		final HorizontalPanel progressWrapperPanel = new HorizontalPanel();
		progressWrapperPanel.setWidth("100%");
		progressWrapperPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		progressWrapperPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		progressWrapperPanel.add( progressBarUI );
		addToGrid( SECOND_COLUMN_INDEX, progressWrapperPanel, false, false );
		
		//Add the load friend status/ add, remove friend related elements 
		final HorizontalPanel friendWrapperPanel = new HorizontalPanel();
		friendWrapperPanel.setWidth("100%");
		friendWrapperPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT );
		friendWrapperPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );

		final HorizontalPanel friendPanel = new HorizontalPanel();
		friendPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_BOTTOM );
		friendPanel.add( friendManager.getFriendImage() );
		friendPanel.add( new HTML("&nbsp;") );
		friendPanel.add( friendManager.getFriendLinkLabel() );
		friendWrapperPanel.add( friendPanel );
		
		addToGrid( THIRD_COLUMN_INDEX, friendWrapperPanel, false, false );
		
		if( SiteManager.isAdministrator() ) {
			//Add the enable/disable bot action panel
			botActionPanel = new TurnBotOnOffManager( userID );
			HorizontalPanel botPanel = new HorizontalPanel();
			botPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
			botPanel.add( botActionPanel.getIsBotImage() );
			botPanel.add( new HTML("&nbsp;") );
			botPanel.add( botActionPanel.getIsBotLinkLabel() );
			addToGrid( SECOND_COLUMN_INDEX, botPanel, true, false );
		}
	}
	
	/**
	 * Fills the main grid data
	 */
	protected void populateDialog(){
		addMainDataFields();
		addImagesDataGrid();
		addOptionalDataFields();
		addAboutMyselfPanel();
		//Add send message and is friend grid if we are not browsing ourselves
		if( SiteManager.getUserID() != userID ) { 
			addSendMsgAddRemoveFriendActionFields();
		} else {
			//Add the progress bar for viewing ourselve's profile
			addNewGrid( 1, 1, false, "", false);
			HorizontalPanel progressBarPanel = new HorizontalPanel();
			progressBarPanel.setWidth("100%");
			progressBarPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
			progressBarPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
			progressBarPanel.add( progressBarUI );
			addToGrid( FIRST_COLUMN_INDEX, progressBarPanel, false , false );
		}
	}
	
	/**
	 * The left button's action
	 *
	 */
	protected void actionLeftButton(){
		hide();
	}
	
	/**
	 * The right button's action
	 */
	protected void actionRightButton(){
		hide();
	}
	
	/**
	 * This method updates and enables fields, after the seter side has supplied the user data;
	 */
	private void setMainUserData( final UserData userData ){
		//Set the user data
		this.userProfileData = userData;
		
		//Set whether the user is a not
		if( botActionPanel != null ) {
			botActionPanel.initializeIsBot( userData.isBot() );
		}

		//Set user gender
		if( userData.isMale() ) {
			gender.setText( titlesI18N.genderMaleValue() );
		} else {
			gender.setText( titlesI18N.genderFemaleValue() );
		}
		//Set user age, process the special case of the unknown age
		String ageString = userData.getAgeString();
		if( ageString.equals( UserData.AGE_UNKNOWN_STR) ) {
			ageString = "";
		}
		setLabelValue( ageFieldLabel, age, ageString );
		//Set other field data
		setLabelValue( firstNameFieldLabel, firstName, userData.getFirstName() );
		setLabelValue( lastNameFieldLabel, lastName, userData.getLastName() );
		setLabelValue( countryFieldLabel, country, userData.getCountryName() );
		setLabelValue( cityFieldLabel, city, userData.getCityName() );
		//Set the user profile registration date
		setDateLabelValue( registrationDate, userData.getUserRegistrationDate() );
		//Set the user online status and the user online field
		if( userData.isOnline() ) {
			setLabelValue( null, onlineStatus, titlesI18N.userOnlineStatus() );
		} else {
			setLabelValue( null, onlineStatus, titlesI18N.userOfflineStatus() );
		}
		setDateLabelValue( lastOnlineDate, userData.getUserLastOnlineDate() );
		
		//Set the about myself description
		if( ( userData.getAboutMe() == null ) || (userData.getAboutMe().trim().isEmpty()) ) {
			aboutMyselfPanel.setVisible(false);
		} else {
			//Set the formatted "about myself" description
			aboutMyselfDesc.setText( StringUtils.formatTextWidth( UserProfileDialogUI.ABOUT_MYSELF_ONE_LINE_LENGTH, userData.getAboutMe(), false ) );
		}
		
		//Update the user's avatar
		updateAvatarImage();
		
		//Set the user profile images
		userFilesView.setUserProfile( userData );
	}
	
	/**
	 * Retrieves the user data from the server side
	 */
	protected void retrieveUserData() {
		//Ensure lazy loading
		(new SplitLoad( true ){
			@Override
			public void execute() {
				//Retrieve the user data from the server 
				CommStatusAsyncCallback<UserData> getProfileCallBack = new CommStatusAsyncCallback<UserData>(progressBarUI) {
					public void onSuccessAct(UserData userData) {
						setMainUserData( userData );
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
				UserManagerAsync userManagerObject = RPCAccessManager.getUserManagerAsync();
				userManagerObject.profile( SiteManager.getUserID(), SiteManager.getUserSessionId(), userID, getProfileCallBack );
			}
		}).loadAndExecute();
		
		//Retrieve the user-friend related data
		friendManager.populateFriendData( false );
	}

}
