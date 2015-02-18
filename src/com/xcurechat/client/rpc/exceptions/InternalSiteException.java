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
 * The superclass for the site internal exceptions
 */
public class InternalSiteException extends SiteException {
	//If the access was blocked due to insufficient gold
	private int usersGold = 0;
	private int neededGold = 0;
	
	public static final Integer DATABASE_EXCEPTION_ERR = new Integer(400);
	public static final Integer IO_FILE_UPLOAD_EXCEPTION_ERR = new Integer(401);
	public static final Integer INCORRECT_FILE_UPLOAD_REQUEST_EXCEPTION_ERR = new Integer(402);
	public static final Integer UNKNOWN_INTERNAL_SITE_EXCEPTION_ERR = new Integer(403);
	public static final Integer INSUFFICIENT_ACCESS_RIGHTS_ERROR = new Integer(404);
	public static final Integer INSUFFICIENT_GOLD_FUNDS = new Integer(405);
	
	static {
		//Register the error code in the super class, to avoid duplicates
		SiteLogicException.registerErrorCode( DATABASE_EXCEPTION_ERR );
		SiteLogicException.registerErrorCode( IO_FILE_UPLOAD_EXCEPTION_ERR );
		SiteLogicException.registerErrorCode( INCORRECT_FILE_UPLOAD_REQUEST_EXCEPTION_ERR );
		SiteLogicException.registerErrorCode( UNKNOWN_INTERNAL_SITE_EXCEPTION_ERR );
		SiteLogicException.registerErrorCode( INSUFFICIENT_ACCESS_RIGHTS_ERROR );
		SiteLogicException.registerErrorCode( INSUFFICIENT_GOLD_FUNDS );
	}
	
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;

	public InternalSiteException(){
		super();
	}
	
	public InternalSiteException( String errorMessage ){
		super( errorMessage );
	}
	
	public InternalSiteException( Integer errorCode ){
		super( errorCode );
	}
	
	public InternalSiteException( Integer errorCode, final int usersGold, final int neededGold ){
		super( errorCode );
		this.usersGold = usersGold;
		this.neededGold = neededGold;
	}

	@Override
	public void populateLocalizedMessages(UIErrorMessages errorMsgI18N) {
		//The super class has this method abstract, so we do not call the super classes' method
		
		addLocalizedErrorMessage( DATABASE_EXCEPTION_ERR, errorMsgI18N.internalDatabaseError());
		addLocalizedErrorMessage( IO_FILE_UPLOAD_EXCEPTION_ERR, errorMsgI18N.unexpectedIOFileUploadError());
		addLocalizedErrorMessage( INCORRECT_FILE_UPLOAD_REQUEST_EXCEPTION_ERR, errorMsgI18N.incorrectFileUploadRequestError());
		addLocalizedErrorMessage( UNKNOWN_INTERNAL_SITE_EXCEPTION_ERR, errorMsgI18N.unknownInternalSiteError());
		addLocalizedErrorMessage( INSUFFICIENT_ACCESS_RIGHTS_ERROR, errorMsgI18N.insufficientAccessRightsError());
		addLocalizedErrorMessage( INSUFFICIENT_GOLD_FUNDS, errorMsgI18N.insufficientGold(usersGold, neededGold ) );
	}

}
