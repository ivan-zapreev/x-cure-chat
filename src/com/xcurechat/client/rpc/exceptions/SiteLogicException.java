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
 * The exceptions package for exceptions that come
 * in RPC calls and also happen on the client side.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client.rpc.exceptions;

import com.xcurechat.client.i18n.UIErrorMessages;

/**
 * @author zapreevis
 * Any kind of universal site exception, that happens
 * of the logical level of the application
 */
public abstract class SiteLogicException extends SiteException {
	
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;

	public SiteLogicException(){
		super();
	}
	
	public SiteLogicException( String errorMessage ){
		super( errorMessage );
	}
	
	public SiteLogicException( Integer errorCode ){
		super( errorCode );
	}

	public void populateLocalizedMessages( UIErrorMessages errorMsgI18N ){
		//No error codes are defined here so far ... 
	}

}
