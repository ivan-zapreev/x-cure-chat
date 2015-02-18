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
 * The exceptions package for exceptions that come in RPC calls.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client.rpc.exceptions;

import com.xcurechat.client.i18n.UIErrorMessages;

/**
 * @author zapreevis
 * Any kind of user login exception
 */
public class UserLoginException extends CaptchaTestFailedException {

	//The server side reported an empty login name
	public static final Integer EMPTY_USER_LOGIN_ERR = new Integer(700);
	//The captchas field is not filled on the dialog submission
	public static final Integer EMPTY_USER_PASSWORD_ERR = new Integer(701);
	//The server side reported that on the login/password were incorrect
	public static final Integer INCORRECT_LOGIN_PASSWORD_ERR = new Integer(702);
	//The server side reported that on the old password is incorrect
	//This error happens when we change the user passford
	public static final Integer INCORRECT_OLD_PASSWORD_ERR = new Integer(703);
	
	static {
		//Register the error code in the super class, to avoid duplicates
		SiteLogicException.registerErrorCode( EMPTY_USER_LOGIN_ERR );
		SiteLogicException.registerErrorCode( EMPTY_USER_PASSWORD_ERR );
		SiteLogicException.registerErrorCode( INCORRECT_LOGIN_PASSWORD_ERR );
		SiteLogicException.registerErrorCode( INCORRECT_OLD_PASSWORD_ERR );
	}
	
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;
	
	public UserLoginException(){
		super();
	}
	
	public UserLoginException( String errorMessage ){
		super( errorMessage );
	}
	
	public UserLoginException( Integer errorCode ){
		super( errorCode );
	}

	@Override
	public void populateLocalizedMessages( UIErrorMessages errorMsgI18N ) {
		super.populateLocalizedMessages( errorMsgI18N );
		
		addLocalizedErrorMessage( EMPTY_USER_LOGIN_ERR, errorMsgI18N.emptyUserLoginError() );
		addLocalizedErrorMessage( EMPTY_USER_PASSWORD_ERR, errorMsgI18N.emptyUserPasswordError() );
		addLocalizedErrorMessage( INCORRECT_LOGIN_PASSWORD_ERR, errorMsgI18N.incorrectLoginPasswordError() );
		addLocalizedErrorMessage( INCORRECT_OLD_PASSWORD_ERR, errorMsgI18N.incorrectOldPassword() );
	}

}
