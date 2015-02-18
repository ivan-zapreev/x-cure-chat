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
package com.xcurechat.client.dialogs.system.messages;

import java.util.List;
import java.util.ArrayList;

import com.google.gwt.user.client.rpc.StatusCodeException;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

import com.google.gwt.user.client.ui.HTML;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.widgets.Button;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

import com.xcurechat.client.dialogs.ActionGridDialog;
import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;

import com.xcurechat.client.rpc.exceptions.SiteException;

import com.xcurechat.client.SiteManager;
import com.xcurechat.client.SiteManagerUI;

/**
 * @author zapreevis
 * Displays error messages
 */
public class ErrorMessagesDialogUI extends ActionGridDialog {
	//The list of error messages
	private List<String> errorMessages = new ArrayList<String>();

	private ErrorMessagesDialogUI( ) {
		//Auto hide and modal
		super( false, true, true, null, false);
		
		final UITitlesI18N titlesI18N = I18NManager.getTitles();
		
		this.setText(titlesI18N.errorMessagesDialog());
		this.setStyleName(CommonResourcesContainer.ERROR_MESSAGES_DIALOG_STYLE);
		
		setLeftEnabled(true);
		setRightEnabled(true);
	}
	
	/**
	 * Made it private for specific reasons
	 * @param e the throwable object to display
	 */
	private ErrorMessagesDialogUI( Throwable e ) {
		this();
		
		if(e instanceof SiteException){
			SiteException siteException = (SiteException) e;
			//Get error messages
			siteException.processErrorCodes( I18NManager.getErrors() );
			errorMessages = siteException.getErrorMessages();
			//There is a certain set of exceptions that should cause the site to log the user out
			if( siteException.isLogOutForced() ) {
				//This is when we should check that the user interfae is set to the 
				//logged in mode. If yes then we re-set it to the logged out mode
				if( SiteManager.isUserLoggedIn() ) {
					SiteManagerUI.getInstance().removeLoggedInUser();
				}
			}
		} else {
			errorMessages.add( getNonSiteExceptionMessage( e ) );
		}
		
		//Fill dialog with data
		populateDialog();
	}
	
	/**
	 * Allows to get an exception message for the case the provided Throwable was tested to be a non SiteException.
	 * @param e a Throwable object that is not child of the SiteException 
	 * @return the string corresponding to the proper error message
	 */
	public static String getNonSiteExceptionMessage( final Throwable e ) {
		if( e instanceof StatusCodeException ) {
			//In this case some of the RPC requests failed, and the exception is reported
			//In principle this means that a connection to the server was lost
			return I18NManager.getErrors().serverConnectionHasBeenLost();
		} else {
			//Just report an unknown site error
			return I18NManager.getErrors().unknownInternalSiteError();
		}
	}
	
	private ErrorMessagesDialogUI( String message ) {
		this();

		errorMessages.add( message );
		
		//Fill dialog with data
		populateDialog();		
	}
	
	@Override
	protected void populateDialog(){
		final int number_of_errors = errorMessages.size(); 
		addNewGrid( number_of_errors + 1, false, "", false);
		
		//Add error messages
		boolean hasMany = ( number_of_errors > 1 );
		for(int i = 0; i < number_of_errors; i++ ) {
			HTML message = new HTML( ( hasMany ? (i+1) + ") " : "" ) + errorMessages.get( i ) );
			addToGrid( this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, message, ( i > 0 ), false );
		}
		
		//Add the close button
		final Button closeButton = new Button(titlesI18N.closeButtonTitle());
		closeButton.setStyleName(CommonResourcesContainer.ERROR_MESSAGES_DIALOG_BUTTON_STYLE);
		closeButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent e){
				hide();
			}
		});
		addToGrid( this.getCurrentGridIndex(), FIRST_COLUMN_INDEX, 2, closeButton, true, true );
	}
	
	protected void actionLeftButton() {
		hide();
	}

	protected void actionRightButton() {
		hide();
	}
	
	public static void openErrorDialog( final Throwable e){
		(new SplitLoad( true ) {
			@Override
			public void execute() {
				ErrorMessagesDialogUI errMsgDialog = new ErrorMessagesDialogUI(e);
				errMsgDialog.show();
				errMsgDialog.center();
			}
		}).loadAndExecute();
	}

	public static void openErrorDialog( final String message ) {
		(new SplitLoad( true ) {
			@Override
			public void execute() {
				ErrorMessagesDialogUI errMsgDialog = new ErrorMessagesDialogUI(message);
				errMsgDialog.show();
				errMsgDialog.center();
			}
		}).loadAndExecute();
	}

	//Report that the user is not logged in, only once
	public static void reportUserLoggedOut() {
		//This is when we should check that the user interface is set to the 
		//logged in mode. If yes then we re-set it to the logged out mode
		//and then report the error. Otherwise if we are in the logged out
		//mode then we should not really react to this exception.
		if( SiteManager.isUserLoggedIn() ) {
			SiteManagerUI.getInstance().removeLoggedInUser();
			openErrorDialog( I18NManager.getErrors().userIsNotLoggedInError() );
		}
	}

}
