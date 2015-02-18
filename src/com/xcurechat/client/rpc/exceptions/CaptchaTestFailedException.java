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
 * This exception is thrown if the CAPTCHA test was failed
 */
public class CaptchaTestFailedException extends SiteLogicException {
	
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;

	//The server side reported a wrong captcha response
	public static final Integer CAPTCHA_TEST_FAILED_ERR = new Integer(100);
	//The captchas field is not filled on the dialog submission
	public static final Integer EMPTY_CAPTCHA_RESPONSE_ERR = new Integer(101);
	
	static {
		//Register the error code in the super class, to avoid duplicates
		SiteLogicException.registerErrorCode( CAPTCHA_TEST_FAILED_ERR );
		SiteLogicException.registerErrorCode( EMPTY_CAPTCHA_RESPONSE_ERR );
	}

	public CaptchaTestFailedException(){
		super();
	}
	
	public CaptchaTestFailedException( String errorMessage ){
		super( errorMessage );
	}
	
	public CaptchaTestFailedException( Integer errorCode ){
		super( errorCode );
	}
	
	@Override
	public void populateLocalizedMessages( UIErrorMessages errorMsgI18N ) {
		super.populateLocalizedMessages( errorMsgI18N );
		
		addLocalizedErrorMessage( CAPTCHA_TEST_FAILED_ERR, errorMsgI18N.captchaTestFailedError() );
		addLocalizedErrorMessage( EMPTY_CAPTCHA_RESPONSE_ERR, errorMsgI18N.captchaEmptyAnswerError() );
	}

}