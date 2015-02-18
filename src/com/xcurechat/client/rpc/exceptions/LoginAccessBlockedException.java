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
 * This exception is meant to indicate that the user's access to
 * the site was blocked due to a high number of failed logins or
 * session validations.
 */
public class LoginAccessBlockedException extends AccessBlockedException {
	
	public static final Integer TOO_MANU_FAILED_LOGINS_EXCEPTION_ERR = new Integer(501);
	
	static {
		//Register the error code in the super class, to avoid duplicates
		SiteLogicException.registerErrorCode( TOO_MANU_FAILED_LOGINS_EXCEPTION_ERR, true );
	}
	
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;

	public LoginAccessBlockedException() {
		super();
	}
	
	public LoginAccessBlockedException( String errorMessage ) {
		super( errorMessage );
	}
	
	public LoginAccessBlockedException( Integer errorCode ) {
		super( errorCode );
	}
	
	@Override
	public void populateLocalizedMessages(UIErrorMessages errorMsgI18N) {
		super.populateLocalizedMessages( errorMsgI18N );

		addLocalizedErrorMessage( TOO_MANU_FAILED_LOGINS_EXCEPTION_ERR,
									errorMsgI18N.tooManyFailedLoginsError( getRBTHPart(), getRBTMPart() ));
	}

}
