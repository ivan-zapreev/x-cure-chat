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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import com.xcurechat.client.SiteManager;
import com.xcurechat.client.SiteManagerUI;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.dialogs.ActionGridDialog;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.UserManagerAsync;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.ActionLinkPanel;
import com.xcurechat.client.utils.widgets.Button;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.TextBaseTranslitAndProgressBar;
import com.xcurechat.client.utils.widgets.UserAvatarImageWidget;
import com.xcurechat.client.utils.widgets.UserProfileFilesView;

/**
 * @author zapreevis
 * The new-user registration dialog 
 */
public class UserProfileDialogUI extends ActionGridDialog {
	
	//The user profile data, that should be first retrieved from the server
	private MainUserData userProfileData = null;
	
	//The one line length of the about myself text area
	public static final int ABOUT_MYSELF_ONE_LINE_LENGTH = 40;
	//The amount of visible lines for the about me description field
	public static final int ABOUT_MYSELF_VISIBLE_LINES_LENGTH = 4;
	
	private final PasswordTextBox oldPasswordTextBox = new PasswordTextBox(); 
	private final PasswordTextBox newPasswordTextBox = new PasswordTextBox();
	private final PasswordTextBox newPasswordRepeatTextBox = new PasswordTextBox();
	private final PasswordTextBox passwordTextBox = new PasswordTextBox();
	private final ListBox ageListBox = new ListBox();
	
	private RadioButton genderMale = null;
	private RadioButton genderFemale = null;

	private final TextBox firstNameTextBox = new TextBox(); 
	private final TextBox lastNameTextBox = new TextBox(); 
	private final TextBox countryTextBox = new TextBox();
	private final TextBox cityTextBox = new TextBox();
	private final TextArea aboutMyselfDesc = new TextArea();
	
	//The action link to open files management dialog
	private ActionLinkPanel manageFilesLink; 
	
	//The avatar image widget
	private UserAvatarImageWidget avatarImage = new UserAvatarImageWidget();
	
	//Is the widget used for viewing profile files
	private final UserProfileFilesView userFilesView;
	
	//The profile deletion button
	private Button deleteButton = new Button( );
	
	public UserProfileDialogUI(final DialogBox parendDialog) {
		//No autohide, and the dialog is modal
		super( true, false, true, parendDialog );
		
		//Initialize the user files view
		this.userFilesView = new UserProfileFilesView( this, null);
		
		//Set the dialog's caption.
		setText( titlesI18N.userProfileDialogTitle( ) );
		
		//Set a style name so we can style it with CSS.
		this.setStyleName(CommonResourcesContainer.USER_DIALOG_STYLE_NAME);
		
		//Fill dialog with data
		populateDialog();
		
		//Get the current MainUserData object from the server
		retrieveUserData();
	}
	
	/**
	 * Should be called in case the list of user file descriptors has changed.
	 */
	public void updateUserFilesView() {
		userFilesView.setUserProfile( userProfileData );
	}
	
	/**
	 * Enable/Disable dialog buttons and profile image editing
	 */
	private void setEnabledButtons( final boolean enableLeft, final boolean enableRight,
									final boolean enableDelete, final boolean enableImageEdit ){
		manageFilesLink.setEnabled( enableImageEdit );
		Scheduler.get().scheduleDeferred( new ScheduledCommand(){
			public void execute(){
				setLeftEnabled( enableLeft );
				setRightEnabled( enableRight );
				deleteButton.setEnabled( enableDelete );
			}
		});
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
	
	//The click listener that opens the dialog for choosing the avatar
	private ClickHandler avatarChooseAction = new ClickHandler() {
		public void onClick( ClickEvent e) {
			//Ensure lazy loading
			final SplitLoad executor = new SplitLoad( true ) {
				@Override
				public void execute() {
					ChooseAvatarDialogUI chooseAvatarDialog = new ChooseAvatarDialogUI( thisDialog );
					chooseAvatarDialog.show();
					chooseAvatarDialog.center();
				}
			};
			executor.loadAndExecute();
		}
	}; 
	
	//The click listener that opens the dialog for uploading the avatar
	private ClickHandler avatarUploadAction = new ClickHandler() {
		public void onClick( ClickEvent e) {
			//Ensure lazy loading
			final SplitLoad executor = new SplitLoad( true ) {
				@Override
				public void execute() {
					AvatarUploadDialogUI uploadDialog = new AvatarUploadDialogUI( thisDialog );
					uploadDialog.show();
					uploadDialog.center();
				}
			};
			executor.loadAndExecute();
		}
	}; 
	
	//The click listener that initiates the avatar's deletion
	private ClickHandler avatarDeleteAction = new ClickHandler() {
		public void onClick( ClickEvent e ) {
			//Disable all buttons except the cancel one
			setEnabledButtons(true, false, false, false);
			
			//Ensure lazy loading
			(new SplitLoad( true ){
				@Override
				public void execute() {
					//Retrieve the user data from the server 
					CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>(progressBarUI) {
						public void onSuccessAct(Void result) {
							setEnabledButtons(true, true, true, true);
							updateAvatarImage();
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
					
					UserManagerAsync userManagerObject = RPCAccessManager.getUserManagerAsync();
					userManagerObject.deleteAvatar( SiteManager.getUserID(), SiteManager.getUserSessionId(), callback );
				}
				@Override
				public void recover() {
					setEnabledButtons(true, true, true, true);
				}
			}).loadAndExecute();
		}
	}; 
	
	/**
	 * Allows to update (set) the user's avatar image for the given user profile. 
	 * If the user profile is not loaded yet, then this method sets the default male avatar. 
	 */
	public void updateAvatarImage() {
		if( userProfileData != null ) {
			avatarImage.updateAvatarData( userProfileData, true );
		} else {
			ShortUserData localData = SiteManager.getShortUserData();
			if( localData != null ) {
				avatarImage.updateAvatarData( localData, true );
			} else {
				avatarImage.updateAvatarImage( SiteManager.getUserID(), true, true );
			}
		}
	}
	
	private void addAvatarDataFields() {
		//ADD THE MAIN DATA FIELDS
		addNewGrid( 1, true, titlesI18N.avatarUserProfilePanel(), true);
		
		//Initialize the avatar's image actions
		VerticalPanel avatarActionPanel = new VerticalPanel();
		avatarActionPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		avatarActionPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		ActionLinkPanel chooseAvatarActionLink = new ActionLinkPanel( ServerSideAccessManager.getChooseImageURL(), "",
																	  ServerSideAccessManager.getChooseImageURL(), "",
																	  titlesI18N.chooseLinkTitle(), avatarChooseAction, true, true ); 
		chooseAvatarActionLink.setImportant();
		avatarActionPanel.add( chooseAvatarActionLink );
		avatarActionPanel.add( new ActionLinkPanel( ServerSideAccessManager.getUploadImageURL(), "",
													ServerSideAccessManager.getUploadImageURL(), "",
													titlesI18N.uploadLinkTitle(), avatarUploadAction, true, true ) );
		avatarActionPanel.add( new ActionLinkPanel( ServerSideAccessManager.getDeleteImageURL(), "",
													ServerSideAccessManager.getDeleteImageURL(), "",
													titlesI18N.deleteLinkTitle(), avatarDeleteAction, true, true ) );
		
		HorizontalPanel avatarPanel = new HorizontalPanel();
		avatarPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		avatarPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		avatarPanel.add( avatarImage );
		avatarPanel.add( new HTML("&nbsp;") );
		avatarPanel.add( avatarActionPanel );
		addToGrid( FIRST_COLUMN_INDEX, avatarPanel, false, false );
	}
	
	private void addMainDataFields() {
		//ADD THE MAIN DATA FIELDS
		addNewGrid( 5, true, titlesI18N.mainDataUserProfilePanel(), true);
		
		Label loginNameFieldTitle = InterfaceUtils.getNewFieldLabel( titlesI18N.userFieldName(), true );
		addToGrid( FIRST_COLUMN_INDEX, loginNameFieldTitle, false, false );
		Label userLoginNameLabel = new Label();
		userLoginNameLabel.setText( SiteManager.getUserLoginName() );
		userLoginNameLabel.setStyleName( CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_IMP_STYLE_NAME );
		addToGrid( SECOND_COLUMN_INDEX, userLoginNameLabel, false, false );
		
		Label firstNameFieldTitle = InterfaceUtils.getNewFieldLabel( titlesI18N.firstNameField(), false );
		addToGrid( FIRST_COLUMN_INDEX, firstNameFieldTitle, true, false );
		addToGrid( SECOND_COLUMN_INDEX, new TextBaseTranslitAndProgressBar(firstNameTextBox, UserData.MAX_OPTIONAL_LENGTH), false, true );
		firstNameTextBox.setEnabled(false);
		
		Label lastNameFieldTitle = InterfaceUtils.getNewFieldLabel( titlesI18N.lastNameField(), false );
		addToGrid( FIRST_COLUMN_INDEX, lastNameFieldTitle, true, false );
		addToGrid( SECOND_COLUMN_INDEX, new TextBaseTranslitAndProgressBar(lastNameTextBox, UserData.MAX_OPTIONAL_LENGTH), false, false );
		lastNameTextBox.setEnabled(false);
		
		Label ageFieldTitle = InterfaceUtils.getNewFieldLabel( titlesI18N.ageField(), false );
		addToGrid( FIRST_COLUMN_INDEX, ageFieldTitle, true, false );
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
		addToGrid( SECOND_COLUMN_INDEX, ageListBox, false, false );
		ageListBox.setEnabled(false);

		Label genderFieldTitle = InterfaceUtils.getNewFieldLabel( titlesI18N.genderField(), false );
		FlowPanel radioButtonPanel = new FlowPanel();
		genderMale = new RadioButton(titlesI18N.genderField(), titlesI18N.genderMaleValue());
		genderMale.setEnabled(false);
		genderFemale = new RadioButton(titlesI18N.genderField(), titlesI18N.genderFemaleValue());
		genderFemale.setEnabled(false);
		radioButtonPanel.add(genderMale);
		radioButtonPanel.add(genderFemale);
		addToGrid( FIRST_COLUMN_INDEX, genderFieldTitle, true, false );
		addToGrid( SECOND_COLUMN_INDEX, radioButtonPanel, false, false );
	}
	
	private void addAboutMeTextArea() {
		//ADD THE MAIN DATA FIELDS
		addNewGrid( 4, true, titlesI18N.optionalUserDataFields(), false );
		
		Label cityFieldTitle = InterfaceUtils.getNewFieldLabel( titlesI18N.cityField(), false );
		addToGrid( FIRST_COLUMN_INDEX, cityFieldTitle, false, false );
		addToGrid( SECOND_COLUMN_INDEX, new TextBaseTranslitAndProgressBar(cityTextBox, UserData.MAX_OPTIONAL_LENGTH), false, false );
		cityTextBox.setEnabled(false);
		
		Label countryFieldTitle = InterfaceUtils.getNewFieldLabel( titlesI18N.countryField(), false );
		addToGrid( FIRST_COLUMN_INDEX, countryFieldTitle, true, false );
		addToGrid( SECOND_COLUMN_INDEX, new TextBaseTranslitAndProgressBar(countryTextBox, UserData.MAX_OPTIONAL_LENGTH), false, false );
		countryTextBox.setEnabled(false);
		
		Label aboutMysefDescFieldTitle = InterfaceUtils.getNewFieldLabel( titlesI18N.aboutMeUserProfilePanel(), false );
		addToGrid( this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, aboutMysefDescFieldTitle, true, false );
		
		addToGrid( this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2,
					new TextBaseTranslitAndProgressBar(aboutMyselfDesc, UserData.MAX_ABOUT_ME_LENGTH), true, false );
		aboutMyselfDesc.setCharacterWidth( ABOUT_MYSELF_ONE_LINE_LENGTH );
		aboutMyselfDesc.setVisibleLines( ABOUT_MYSELF_VISIBLE_LINES_LENGTH );
		aboutMyselfDesc.setEnabled(false);
	}

	private void addPasswordDataFields() {
		//ADD THE PASSWORD DATA FIELDS
		addNewGrid( 3, true, titlesI18N.passwordUserProfilePanel(), false);
		
		Label oldPasswordField = InterfaceUtils.getNewFieldLabel( titlesI18N.currentPasswordField(), false );
		addToGrid( FIRST_COLUMN_INDEX, oldPasswordField, false, false );
		addToGrid( SECOND_COLUMN_INDEX, oldPasswordTextBox, false, false );
		
		Label newPasswordField = InterfaceUtils.getNewFieldLabel( titlesI18N.newPasswordField(), false );
		addToGrid( FIRST_COLUMN_INDEX, newPasswordField, true, false );
		addToGrid( SECOND_COLUMN_INDEX, newPasswordTextBox, false, false );

		Label newPasswordRepeatField = InterfaceUtils.getNewFieldLabel( titlesI18N.newPasswordRepeatField(), false );
		addToGrid( FIRST_COLUMN_INDEX, newPasswordRepeatField, true, false );
		addToGrid( SECOND_COLUMN_INDEX, newPasswordRepeatTextBox, false, false );
	}
	
	private void addProfileFilesDataGrid() {
		//Add the grid for storing images
		addNewGrid( 2, 2, true, titlesI18N.imagesUserProfilePanel(), true);
		
		//Add the files-view widget
		addToGrid( this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, userFilesView, false, false );
		
		//Add the files management link
		manageFilesLink = new ActionLinkPanel( null, "", null, "",
  												titlesI18N.addRemoveProfileFilesLink(),
  												new ClickHandler(){
													@Override
													public void onClick( ClickEvent event) {
														//Ensure lazy loading
														final SplitLoad executor = new SplitLoad( true ) {
															@Override
															public void execute() {
																ProfileFilesManagerUI dialog = new ProfileFilesManagerUI( userProfileData, thisDialog );
																dialog.show();
																dialog.center();
															}
														};
														executor.loadAndExecute();
													}
												}, false, false );
		manageFilesLink.setImportant();
		//The following panel is needed to force centering of the action link
		HorizontalPanel panel = new HorizontalPanel();
		panel.add( manageFilesLink );
		addToGrid( this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, panel, true, false );
	}
	
	@Override
	protected boolean canCenterInCell( final int collspan, final Widget w ) {
		//The following is needed to force centering of the action link
		return super.canCenterInCell( collspan, w ) || ( ( w instanceof HorizontalPanel ) && ( ( ( HorizontalPanel ) w ).getWidget(0) instanceof ActionLinkPanel ) );
	}

	private void addDeleteProfile() {
		//ADD THE DELETE PROFILE SUB-DIALOG
		addNewGrid( 2, true, titlesI18N.deleteUserProfilePanel(), false);
		
		Label passwordField = InterfaceUtils.getNewFieldLabel( titlesI18N.currentPasswordField(), true );
		addToGrid( FIRST_COLUMN_INDEX, passwordField, false, false );
		addToGrid( SECOND_COLUMN_INDEX, passwordTextBox, false, false );
		
		Label deleteProfileQuestion = InterfaceUtils.getNewFieldLabel( titlesI18N.deleteUserProfileQuestion(), true );
		addToGrid( FIRST_COLUMN_INDEX, deleteProfileQuestion, true, false );
		deleteButton.setText( titlesI18N.deleteButton() );
		deleteButton.setStyleName( CommonResourcesContainer.USER_DIALOG_ACTION_BUTTON_STYLE );
		addToGrid( SECOND_COLUMN_INDEX, deleteButton, false, false );
		
		//Add the delete action handler
		deleteButton.addClickHandler( new ClickHandler() {
			public void onClick( ClickEvent e ) {
				//Disable the left/right button such that we can not do anything there
				setEnabledButtons(false, false, false, false);
				
				try{
					//Validate the password first
					final String password = passwordTextBox.getText();
					UserData.validatePassword( password, true );
					
					//Ensure lazy loading
					(new SplitLoad( true ){
						@Override
						public void execute() {
							//Create the call-back object
							CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>(progressBarUI) {
								public void onSuccessAct(Void result) {
									//Remove the logged in user, since it does not exist any ways
									SiteManagerUI.getInstance().removeLoggedInUser();
									//Hide the modal dialog, the user is deleted
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
								};
							};
							
							//Perform the server call
							UserManagerAsync userMNGService = RPCAccessManager.getUserManagerAsync();
							userMNGService.delete( SiteManager.getUserID(), password, SiteManager.getUserSessionId(), callback);
						}
						@Override
						public void recover() {
							//Enable the buttons back
							setEnabledButtons(true, true, true, true);
						}
					}).loadAndExecute();
				} catch ( final SiteException exception ){
					setEnabledButtons(true, true, true, true);
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

	}

	private void addButtons() {
		//ADD THE LEFT AND RIGHT BUTTONS
		addNewGrid( 1, false, "", false);

		addGridActionElements(true, false, true, false);
	}

	/**
	 * Fills the main grid data
	 */
	protected void populateDialog(){
		addAvatarDataFields();
		addMainDataFields();
		addProfileFilesDataGrid();
		addAboutMeTextArea();
		addPasswordDataFields();
		addDeleteProfile();
		addButtons();
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
		setEnabledButtons(false, false, false, false);
		
		final String oldPassword = this.oldPasswordTextBox.getText();
		final String newPassword = this.newPasswordTextBox.getText();
		final String newPasswordRep = this.newPasswordRepeatTextBox.getText();
		
		//NOTE: The user login name is not set, since it must not be changed.
		userProfileData.setFirstName(this.firstNameTextBox.getText());
		userProfileData.setLastName(this.lastNameTextBox.getText());
		int selectedAge = this.ageListBox.getSelectedIndex();
		userProfileData.setUserAge(this.ageListBox.getItemText(selectedAge));
		userProfileData.setCityName(this.cityTextBox.getText());
		userProfileData.setCountryName(this.countryTextBox.getText());
		userProfileData.setAboutMe(this.aboutMyselfDesc.getText());
		userProfileData.setMale(this.genderMale.getValue());
		
		try{
			//Check if we are changing the password
			if( !oldPassword.isEmpty() || !newPassword.isEmpty() || !newPasswordRep.isEmpty() ){
				UserData.validatePassword( oldPassword, true ); 
				UserData.validatePasswords( newPassword, newPasswordRep );
			}
			//Call the data object, validation procedure
			userProfileData.validate( false );
			
			//Ensure lazy loading
			(new SplitLoad( true ){
				@Override
				public void execute() {
					//Call the server update procedure, happens only if userProfileData is valid
					CommStatusAsyncCallback<Void> callback = new CommStatusAsyncCallback<Void>(progressBarUI) {
						public void onSuccessAct(Void result) {
							hide();
						}
						public void onFailureAct(final Throwable caught) {
							(new SplitLoad( true ) {
								@Override
								public void execute() {
									//Report the error
									ErrorMessagesDialogUI.openErrorDialog(caught);
								}
							}).loadAndExecute();
							//Use the recovery method
							recover();
						}
					};
					UserManagerAsync userManagerObject = RPCAccessManager.getUserManagerAsync();
					userManagerObject.update( userProfileData , oldPassword, newPassword,
												SiteManager.getUserSessionId(), callback );
				}
				@Override
				public void recover() {
					setEnabledButtons(true, true, true, true);
				}
			}).loadAndExecute();
		} catch ( final SiteException e ){
			setEnabledButtons(true, true, true, false);
			(new SplitLoad( true ) {
				@Override
				public void execute() {
					//Report the error
					ErrorMessagesDialogUI.openErrorDialog(e);
				}
			}).loadAndExecute();
		}
	}
	
	/**
	 * This method updates and enables fields, after the seter side has supplied the user data;
	 */
	private void setMainUserData( final MainUserData userData ){
		//Set the user data
		this.userProfileData = userData;
		
		//Set user gender
		genderFemale.setEnabled( true );
		genderFemale.setValue( ! userData.isMale() );
		genderMale.setEnabled( true );
		genderMale.setValue(   userData.isMale() );

		//Set user age
		for(int index = 0; index < ageListBox.getItemCount(); index++ ){
			String itemText = ageListBox.getItemText( index );
			if( UserData.getAgeFromString( itemText ).equals( userData.getUserAge() ) ){
				ageListBox.setSelectedIndex( index );
				break;
			}
		}
		ageListBox.setEnabled( true );
		
		//Set other field data
		firstNameTextBox.setText( userData.getFirstName() );
		firstNameTextBox.setEnabled( true );
		lastNameTextBox.setText( userData.getLastName() );
		lastNameTextBox.setEnabled( true );
		countryTextBox.setText( userData.getCountryName() );
		countryTextBox.setEnabled( true );
		aboutMyselfDesc.setText( userData.getAboutMe() );
		aboutMyselfDesc.setEnabled(true);
		cityTextBox.setText( userData.getCityName() );
		cityTextBox.setEnabled( true );
		
		//Update the avatar image
		updateAvatarImage();
		
		//Set the user profile images
		userFilesView.setUserProfile( userData );
	}
	
	/**
	 * Retrieves the user data from the server side
	 */
	protected void retrieveUserData() {
		//Disable all buttons except the cancel one
		setEnabledButtons(true, false, false, false);
		
		//Ensure lazy loading
		(new SplitLoad( true ){
			@Override
			public void execute() {
				//Retrieve the user data from the server 
				CommStatusAsyncCallback<MainUserData> getProfileCallBack = new CommStatusAsyncCallback<MainUserData>(progressBarUI) {
					public void onSuccessAct( MainUserData userData ) {
						setEnabledButtons(true, true, true, true);
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
						//Use the recovery method
						recover();
					}
				};
				UserManagerAsync userManagerObject = RPCAccessManager.getUserManagerAsync();
				userManagerObject.profile( SiteManager.getUserID(), SiteManager.getUserSessionId(), getProfileCallBack );
			}
			@Override
			public void recover() {
				setEnabledButtons(true, false, false, false);
				//Set the default male avatar
				updateAvatarImage();
			}
		}).loadAndExecute();
	}

}
