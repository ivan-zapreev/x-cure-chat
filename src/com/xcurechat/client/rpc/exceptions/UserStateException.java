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
 * This exception is thrown if the user login related interactions fail
 */
public class UserStateException extends SiteLogicException {
	
	//The server side reported that on the user is not logged in
	public static final Integer USER_IS_NOT_LOGGED_IN_ERR = new Integer(800);
	public static final Integer USER_DOES_NOT_EXIST = new Integer(801);
	public static final Integer SITE_SECTION_NO_ACCESS_USER_IS_NOT_LOGGED_IN_ERR = new Integer(802);
	
	static {
		//Register the error code in the super class, to avoid duplicates
		SiteLogicException.registerErrorCode( USER_IS_NOT_LOGGED_IN_ERR, true );
		SiteLogicException.registerErrorCode( USER_DOES_NOT_EXIST );
		SiteLogicException.registerErrorCode( SITE_SECTION_NO_ACCESS_USER_IS_NOT_LOGGED_IN_ERR );
	}
	
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;
	
	public UserStateException(){
		super();
	}
	
	public UserStateException( String errorMessage ){
		super( errorMessage );
	}
	
	public UserStateException( Integer errorCode ){
		super( errorCode );
	}

	@Override
	public void populateLocalizedMessages( UIErrorMessages errorMsgI18N ) {
		super.populateLocalizedMessages( errorMsgI18N );
		
		addLocalizedErrorMessage( USER_IS_NOT_LOGGED_IN_ERR, errorMsgI18N.userIsNotLoggedInError() );
		addLocalizedErrorMessage( USER_DOES_NOT_EXIST, errorMsgI18N.userDoesNotExistError() );
		addLocalizedErrorMessage( SITE_SECTION_NO_ACCESS_USER_IS_NOT_LOGGED_IN_ERR, errorMsgI18N.noAccessToTheSiteSectionNotLoggedIn() );
	}

}
