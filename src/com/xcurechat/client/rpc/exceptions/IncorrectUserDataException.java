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
 * Any kind of user registration exception
 */
public class IncorrectUserDataException extends UserLoginException {
	
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;

	//The server side reported that a user with the same login name exists
	public static final Integer LOGIN_NAME_IN_USE_ERR = new Integer(300);
	//The user agreement is not accepter
	public static final Integer USER_AGREEMENT_IS_NOT_ACCEPTED_ERR = new Integer(301);
	
	static {
		//Register the error code in the super class, to avoid duplicates
		SiteLogicException.registerErrorCode( LOGIN_NAME_IN_USE_ERR );
		SiteLogicException.registerErrorCode( USER_AGREEMENT_IS_NOT_ACCEPTED_ERR );
	}

	public IncorrectUserDataException(){
		super();
	}
	
	public IncorrectUserDataException( String errorMessage ){
		super( errorMessage );
	}
	
	public IncorrectUserDataException( Integer errorCode ){
		super( errorCode );
	}

	@Override
	public void populateLocalizedMessages( UIErrorMessages errorMsgI18N ) {
		super.populateLocalizedMessages( errorMsgI18N );
		
		addLocalizedErrorMessage( LOGIN_NAME_IN_USE_ERR, errorMsgI18N.userLoginInUseError() );
		addLocalizedErrorMessage( USER_AGREEMENT_IS_NOT_ACCEPTED_ERR, errorMsgI18N.userAgreementIsNotAcceptedError() );
	}

}
