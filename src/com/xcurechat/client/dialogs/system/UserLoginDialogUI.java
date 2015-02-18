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

import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;

import com.xcurechat.client.SiteManagerUI;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.data.MainUserData;
import com.xcurechat.client.dialogs.ActionGridDialog;
import com.xcurechat.client.dialogs.system.messages.ErrorMessagesDialogUI;

import com.xcurechat.client.rpc.RPCAccessManager;
import com.xcurechat.client.rpc.UserManagerAsync;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.UserLoginException;

import com.xcurechat.client.utils.CommStatusAsyncCallback;
import com.xcurechat.client.utils.InterfaceUtils;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

/**
 * @author zapreevis
 * The login dialog.
 */
public class UserLoginDialogUI extends ActionGridDialog {
	
    // Login ids. These MUST match the ids in TeamScape.html!
    private static final String LOGINFORM_ID           = "loginForm";
    private static final String CANCELBUTTON_ID        = "loginCancel";
    private static final String LOGINBUTTON_ID         = "loginSubmit";
    private static final String USERNAME_ID            = "loginUsername";
    private static final String PASSWORD_ID            = "loginPassword";
    private static final String LOGIN_LABEL_CELL_ID    = "loginLabel";
    private static final String PASSWORD_LABEL_CELL_ID = "passwordLabel";
    private static final String PROGRESS_BAR_CELL_ID   = "progressBar";
    
    /*The required elements derived from the html*/
    private final FormPanel form;
    private final ButtonElement cancel;
    private final ButtonElement submit;
    private final InputElement userLoginInput;
    private final InputElement userPasswordInput;
    /*Stores the info about the left and right buttons being enabled*/
    private boolean isLoginCancelActionEnabled = false;
    private boolean isLoginActionEnabled = false;
    
	private UserLoginDialogUI(){
		super( false, true, true, null);
		
		//Set the dialog's caption.
		this.setText( titlesI18N.userLoginDialogTitle() );
		
		//Set a style name so we can style it with CSS.
		this.setStyleName( CommonResourcesContainer.USER_DIALOG_STYLE_NAME );
		
        //Get a handle to the form and set its action to our jsni method
        form = FormPanel.wrap(Document.get().getElementById(LOGINFORM_ID), false);
        
        //Get the cancel button for text localization
        cancel = (ButtonElement) Document.get().getElementById(CANCELBUTTON_ID);
        cancel.setInnerText( getLeftButtonText() );
        
        //Get the submit button for text localization
        submit = (ButtonElement) Document.get().getElementById(LOGINBUTTON_ID);
        submit.setInnerText( getRightButtonText() );
        
        //Get the user login input
        userLoginInput = (InputElement) Document.get().getElementById(USERNAME_ID);
        
        //Get the user password input
        userPasswordInput = (InputElement) Document.get().getElementById(PASSWORD_ID);
        
		//Fill dialog with data
		populateDialog();
		
		//Enable the action buttons and hot key
		setLeftEnabled( true );
		setRightEnabled( true );
	}
	
	@Override
	public void setLeftEnabled(boolean isLeftEnabled){
		super.setLeftEnabled( isLeftEnabled );
		this.isLoginCancelActionEnabled = isLeftEnabled;
	}
	
	@Override
	public void setRightEnabled(boolean isRightEnabled){
		super.setRightEnabled( isRightEnabled );
		this.isLoginActionEnabled = isRightEnabled;
	}
	
	/**
	 * @return the left-button caption
	 */
	@Override
	protected String getLeftButtonText(){
		return titlesI18N.cancelButton();
	}

	/**
	 * @return the right-button caption
	 */
	@Override
	protected String getRightButtonText(){
		return titlesI18N.loginButton();
	}
	
	/**
	 * Fills the main grid data
	 */
	protected void populateDialog(){
		
		addNewGrid( 1, false, "", false);
        
        //Set the login label
        final TableCellElement loginLabelCell = (TableCellElement) Document.get().getElementById(LOGIN_LABEL_CELL_ID);
        Label loginField = InterfaceUtils.getNewFieldLabel( titlesI18N.loginNameField(), true );
        loginLabelCell.setInnerHTML( loginField.toString() );

        //Set the password label
        final TableCellElement passwordLabelCell = (TableCellElement) Document.get().getElementById(PASSWORD_LABEL_CELL_ID);
        Label passwordField = InterfaceUtils.getNewFieldLabel( titlesI18N.passwordField(), true );
        passwordLabelCell.setInnerHTML( passwordField.toString() );
        
        //Set the progress bar element
        final TableCellElement progressBarCell = (TableCellElement) Document.get().getElementById(PROGRESS_BAR_CELL_ID);
        progressBarCell.appendChild( progressBarUI.getElement() );
        
		addToGrid( FIRST_COLUMN_INDEX, form, false, false );
	}
    
    /**
     * Retrieve the value of the password field
     * @return the value of the password field
     */
    private String getPassword() {
        return userPasswordInput.getValue();
    }
    
    /**
     * Retrieve the value of the login field
     * @return the value of the login field
     */
    private String getUsername() {
        return userLoginInput.getValue();
    }
    
    /**
     * Clear the password and login values
     */
    private void clearFormData() {
    	userPasswordInput.setValue("");
        userLoginInput.setValue("");
    }
    
    /**
     * Initiates the cancel action if it is enabled
     */
    protected void submitLoginCancel() {
		if( isLoginCancelActionEnabled ) {
			//Enable the action buttons
			setLeftEnabled(true);
			setRightEnabled(true);
			//hide the dialog
			hide();
		}
    }
    
	@Override
	protected void actionLeftButton(){
		submitLoginCancel();
	}
	
	/**
	 * Initiates the login action if it is enabled
	 */
	protected void submitLogin() {
		if( isLoginActionEnabled ) {
			//Disable the action buttons
			setLeftEnabled(false);
			setRightEnabled(false);
	        submit.setDisabled(true);
			
			final String password = getPassword();
			final String login = getUsername();
						
			try{
				//Validate the form data
				validate( login, password );
				
				//Ensure lazy loading
				(new SplitLoad( true ){
					@Override
					public void execute() {
						//Call the server login procedure only if the form data is valid
						CommStatusAsyncCallback<MainUserData> loginCallBack = new CommStatusAsyncCallback<MainUserData>(progressBarUI) {
							public void onSuccessAct(MainUserData result) {
								//Hide the dialog
								hide();
								//Clear the user login and password
								clearFormData();
								//Enable the action buttons
								setLeftEnabled(true);
								setRightEnabled(true);
						        submit.setDisabled(false);
								//Set the logged in user
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
								//Update captcha image
								updateCaptchaImage();
								//Use the recovery method
								recover();
							}
						};
						UserManagerAsync userManager = RPCAccessManager.getUserManagerAsync();
						userManager.login(login, password, loginCallBack);
					}
					@Override
					public void recover() {
						//Enable the action buttons
						setLeftEnabled(true);
						setRightEnabled(true);
				        submit.setDisabled(false);
					}
				}).loadAndExecute();
			} catch ( final UserLoginException e ){
				//Enable the action buttons
				setLeftEnabled(true);
				setRightEnabled(true);
		        submit.setDisabled(false);
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
	
	@Override
	protected void actionRightButton(){
		//Submit the form, this will trigger the login and make the browser remember the user name and password
        form.submit();
	}
	
	private void validate( final String login, final String password ) throws UserLoginException {
		UserLoginException exception = new UserLoginException();
		try{
			UserData.validatePassword(password, false );
		} catch( SiteException e){
			//This can happen only if the password is empty
			exception.addErrorCode(UserLoginException.EMPTY_USER_PASSWORD_ERR);
		}

		if( login.trim().isEmpty() ){
			exception.addErrorCode(UserLoginException.EMPTY_USER_LOGIN_ERR);
		}
		
		if( !exception.getErrorCodes().isEmpty() ){
			throw exception;
		}
	}
	
	@Override
	protected boolean isRightButtonModKeyDown(KeyDownEvent event) {
		//Here we only initiate login on enter if the login and password are set
		//We actually do it here instead of checking for the right button modifier
		//because this is our indicator for doing login on enter. This allows to
		//avoid error caused by the Enter pressed by the user during the login and
		//password field's auto completion. 
		boolean result = true;
		try{
			//Validate the form data
			validate( getUsername(), getPassword() );
		} catch ( UserLoginException e ){
			result = false;
		}
		return result;
	}
	
	//The only instance of the user login dialog
	private static UserLoginDialogUI instance = null;
	
	public static void openLoginDialog() {
		//If the dialog does not exist
		if( instance == null ) {
			//Create the dialog instance
			instance = new UserLoginDialogUI();
	        // Now, inject the jsni methods for handling the form submit and cancel
	        injectLoginFunctions(instance);
		}
		//Show the dialog instance
		instance.show();
		instance.center();
		//Put focus on the user login field
		instance.userLoginInput.focus();
	}
	
    // This is our JSNI methods that will be called on form submit and cancel
    private static native void injectLoginFunctions(UserLoginDialogUI view) /*-{
        $wnd.__gwt_login_cancel = function(){
        view.@com.xcurechat.client.dialogs.system.UserLoginDialogUI::submitLoginCancel()();
        }
        $wnd.__gwt_login = function(){
        view.@com.xcurechat.client.dialogs.system.UserLoginDialogUI::submitLogin()();
        }
    }-*/;

}
