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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;

import com.xcurechat.client.SiteManagerUI;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.dialogs.ActionGridDialog;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.UserManagerAsync;
import com.xcurechat.client.rpc.exceptions.CaptchaTestFailedException;
import com.xcurechat.client.rpc.exceptions.IncorrectUserDataException;
import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.TextBaseTranslitAndProgressBar;

/**
 * @author zapreevis
 * The new-user registration dialog 
 */
public class UserRegistrationDialogUI extends ActionGridDialog {
	
	private final TextBox loginTextBox = new TextBox(); 
	private final PasswordTextBox passwordTextBox = new PasswordTextBox();
	private final PasswordTextBox passwordRepeatTextBox = new PasswordTextBox();
	private final ListBox ageListBox = new ListBox();
	private RadioButton genderMale = null;
	private RadioButton genderFemale = null;

	private final TextBox firstNameTextBox = new TextBox(); 
	private final TextBox lastNameTextBox = new TextBox(); 
	private final TextBox countryTextBox = new TextBox();
	private final TextBox cityTextBox = new TextBox();
	private final CheckBox userAgreement = new CheckBox();
	
	//Number of rows in the dialog-form grid
	public static final int NUMBER_OF_ROWS_MAIN = 8; 
	//Number of rows in the dialog-form grid
	public static final int NUMBER_OF_ROWS_OPT = 5; 
	
	public UserRegistrationDialogUI() {
		//Autohide and modal
		super( true, true, true, null);

		//Enable the action buttons and hot key
		setLeftEnabled( true );
		setRightEnabled( true );
		
		//Fill dialog with data
		populateDialog();
		
		//Set the dialog's caption.
		setText( titlesI18N.userRegistrationDialogTitle() );
		
		//Set a style name so we can style it with CSS.
		this.setStyleName(CommonResourcesContainer.USER_DIALOG_STYLE_NAME);
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
		return titlesI18N.registerButton();
	}
	
	/**
	 * Fills the grids and the data fields
	 */
	protected void populateDialog(){
		//ADD THE MAIN FIELDS
		addNewGrid(NUMBER_OF_ROWS_MAIN, false, "", false);
		
		Label loginField = InterfaceUtils.getNewFieldLabel( titlesI18N.loginNameField(), true );
		addToGrid( FIRST_COLUMN_INDEX, loginField, false, false );
		addToGrid( SECOND_COLUMN_INDEX, new TextBaseTranslitAndProgressBar(loginTextBox, UserData.MAX_LOGIN_LENGTH, true), false, true );
		
		Label passwordField = InterfaceUtils.getNewFieldLabel( titlesI18N.passwordField(), true );
		addToGrid( FIRST_COLUMN_INDEX, passwordField, true, false );
		addToGrid( SECOND_COLUMN_INDEX, passwordTextBox, false, false );

		Label passwordRepeatField = InterfaceUtils.getNewFieldLabel( titlesI18N.passwordRepeatField(), true );
		addToGrid( FIRST_COLUMN_INDEX, passwordRepeatField, true, false );
		addToGrid( SECOND_COLUMN_INDEX, passwordRepeatTextBox, false, false );

		Label genderFieldTitle = InterfaceUtils.getNewFieldLabel( titlesI18N.genderField(), true );
		FlowPanel radioButtonPanel = new FlowPanel();
		genderMale = new RadioButton(titlesI18N.genderField(), titlesI18N.genderMaleValue());
		genderMale.setValue(true);
		genderFemale = new RadioButton(titlesI18N.genderField(), titlesI18N.genderFemaleValue());
		radioButtonPanel.add(genderMale);
		radioButtonPanel.add(genderFemale);
		addToGrid( FIRST_COLUMN_INDEX, genderFieldTitle, true, false );
		addToGrid( SECOND_COLUMN_INDEX, radioButtonPanel, false, false );
		
		//Add the user agreement check box
		HorizontalPanel userAgreementPanel = new HorizontalPanel();
		userAgreementPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		userAgreementPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
		userAgreement.setStyleName( CommonResourcesContainer.USER_DIALOG_COMPULSORY_FIELD_STYLE );
		userAgreement.setValue( true );
		userAgreement.setText( titlesI18N.iAcceptCheckBoxText() );
		Label userAgreementLink = new Label();
		userAgreementLink.setText( titlesI18N.userAgreementText() );
		userAgreementLink.setStyleName( CommonResourcesContainer.DIALOG_LINK_RED_STYLE );
		userAgreementLink.addClickHandler( new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				( new SplitLoad( true ) {
					@Override
					public void execute() {
						UserAgreementDialogUI userAgreementDialog = new UserAgreementDialogUI( thisDialog, userAgreement ); 
						userAgreementDialog.show();
						userAgreementDialog.center();
					}
				}).loadAndExecute();
			}
		});
		userAgreementPanel.add( userAgreement );
		userAgreementPanel.add( new HTML("&nbsp;") );
		userAgreementPanel.add( userAgreementLink );
		addToGrid( this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, userAgreementPanel, true, false );
		
		this.addCaptchaTestRows();
		this.addGridActionElements( true, true, true, true );

		//ADD THE OPTIONAL FIELDS
		addNewGrid(NUMBER_OF_ROWS_OPT, true, titlesI18N.optionalUserDataFields(), false);
		
		Label firstNameFieldTitle = InterfaceUtils.getNewFieldLabel( titlesI18N.firstNameField(), false );
		addToGrid( FIRST_COLUMN_INDEX, firstNameFieldTitle, false, false );
		addToGrid( SECOND_COLUMN_INDEX, new TextBaseTranslitAndProgressBar(firstNameTextBox, UserData.MAX_OPTIONAL_LENGTH), false, false );
		
		Label lastNameFieldTitle = InterfaceUtils.getNewFieldLabel( titlesI18N.lastNameField(), false );
		addToGrid( FIRST_COLUMN_INDEX, lastNameFieldTitle, true, false );
		addToGrid( SECOND_COLUMN_INDEX, new TextBaseTranslitAndProgressBar(lastNameTextBox, UserData.MAX_OPTIONAL_LENGTH), false, false );
		
		Label ageFieldTitle = InterfaceUtils.getNewFieldLabel( titlesI18N.ageField(), false );
		addToGrid( FIRST_COLUMN_INDEX, ageFieldTitle, true, false );
		ageListBox.addItem( UserData.AGE_UNKNOWN_STR );
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
		
		Label cityFieldTitle = InterfaceUtils.getNewFieldLabel( titlesI18N.cityField(), false );
		addToGrid( FIRST_COLUMN_INDEX, cityFieldTitle, true, false );
		addToGrid( SECOND_COLUMN_INDEX, new TextBaseTranslitAndProgressBar(cityTextBox, UserData.MAX_OPTIONAL_LENGTH), false, false );

		Label countryFieldTitle = InterfaceUtils.getNewFieldLabel( titlesI18N.countryField(), false );
		addToGrid( FIRST_COLUMN_INDEX, countryFieldTitle, true, false );
		addToGrid( SECOND_COLUMN_INDEX, new TextBaseTranslitAndProgressBar(countryTextBox, UserData.MAX_OPTIONAL_LENGTH), false, false );
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
		//Disable the action buttons
		setLeftEnabled(false);
		setRightEnabled(false);
		
		final String userLoginName = this.loginTextBox.getText();
		
		final String captchaAnswer = getCaptchaAnswerText();
		final String password = this.passwordTextBox.getText();
		final String passwordRep = this.passwordRepeatTextBox.getText();
		
		final MainUserData registeringUserData = new MainUserData();
		registeringUserData.setUserLoginName( userLoginName );
		registeringUserData.setFirstName(this.firstNameTextBox.getText());
		registeringUserData.setLastName(this.lastNameTextBox.getText());
		final int selectedAge = this.ageListBox.getSelectedIndex();
		registeringUserData.setUserAge(this.ageListBox.getItemText(selectedAge));
		registeringUserData.setCityName(this.cityTextBox.getText());
		registeringUserData.setCountryName(this.countryTextBox.getText());
		registeringUserData.setMale(this.genderMale.getValue());
		
		try{
			//Call the data object, validation procedure
			registeringUserData.validate( true );
			UserData.validatePasswords( password, passwordRep );
			
			if( ! userAgreement.getValue() ) {
				//If the user agreement is not accepted
				throw new IncorrectUserDataException( IncorrectUserDataException.USER_AGREEMENT_IS_NOT_ACCEPTED_ERR );
			}
			
			if( captchaAnswer.trim().isEmpty() ){
				//If the captcha value is not input
				throw new CaptchaTestFailedException(CaptchaTestFailedException.EMPTY_CAPTCHA_RESPONSE_ERR);
			}
			
			//Ensure lazy loading
			(new SplitLoad( true ){
				@Override
				public void execute() {
					//Call the server registration procedure, happens only if userProfileData is valid 
					CommStatusAsyncCallback<MainUserData> callback = new CommStatusAsyncCallback<MainUserData>(progressBarUI) {
						public void onSuccessAct(MainUserData result) {
							hide();
							SiteManagerUI.getInstance().setLoggedInUser( result );
						}
						public void onFailureAct(final Throwable caught) {
							(new SplitLoad( true ) {
								@Override
								public void execute() {
									//Report the error
									ErrorMessagesDialogUI.openErrorDialog( caught );
								}
							}).loadAndExecute();
							//Update the Captcha image
							updateCaptchaImage();
							//Use the recovery method
							recover();
						}
					};
					UserManagerAsync userManagerObject = RPCAccessManager.getUserManagerAsync();
					userManagerObject.register( registeringUserData , password, captchaAnswer, callback );
				}
				@Override
				public void recover() {
					//Enable the action buttons
					setLeftEnabled(true);
					setRightEnabled(true);
				}
			}).loadAndExecute();
		} catch ( final SiteException e ){
			//Enable the action buttons
			setLeftEnabled(true);
			setRightEnabled(true);
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
