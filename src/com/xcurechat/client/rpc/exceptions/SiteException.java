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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.xcurechat.client.i18n.UIErrorMessages;

/**
 * @author zapreevis
 * The universal site exception, whet it happens, it means that something went wrong
 * either on the logical level or on programming level, the latter unly in case we do
 * not want to annoy the user with java exception. 
 */
public abstract class SiteException extends Exception implements IsSerializable {

	//	The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;

	//The list of all registered error codes
	private static List<Integer> listErrorCodes = new ArrayList<Integer>();

	//The list of error codes that should cause the interface to swith to the logged out mode
	private static List<Integer> listLogOutErrorCodes = new ArrayList<Integer>();
	
	private HashMap<Integer, String> errorCodeToMessage = new HashMap<Integer, String>();

	/**
	 * Allows to registed the error code
	 * @param errorCode the error code to registed
	 * @param isLogOutError should be true if this exception
	 * in the client should cause the UI to go into the logged out mode
	 */
	protected static void registerErrorCode(final Integer errorCode, final boolean isLogOutError) {
		if( listErrorCodes.contains(errorCode) ){
			throw new RuntimeException("Duplicate error code!");
		}
		if( isLogOutError ) {
			listLogOutErrorCodes.add( errorCode );
		}
		listErrorCodes.add( errorCode );
	}

	/**
	 * Allows to registed a non-log-out error code, i.e. the one that does not
	 * make UI to be reset into a user-logged out mode
	 * @param errorCode the error code to registes
	 */
	protected static void registerErrorCode(final Integer errorCode) {
		registerErrorCode( errorCode, false );
	}
	
	/**
	 * Allows to check if the given exception requires the user client to go into the logged out mode
	 * @return true if the user client needs to go into the logged out mode
	 */
	public boolean isLogOutForced() {
		boolean result = false;
		Iterator<Integer> iter = errorCodes.iterator();
		while( iter.hasNext() ) {
			if( listLogOutErrorCodes.contains( iter.next() ) ) {
				result = true;
				break;
			}
		}
		return result;
	}

	//The following two array contain error codes and error messages put
	//into this exception, the former ones are created on the client side
	//of the application and the latter ones are from the server side
	protected List<String> errorMessages = new ArrayList<String>();
	protected List<Integer> errorCodes = new ArrayList<Integer>();
	
	public SiteException(){
		super();
	}
	
	public SiteException( String errorMessage ){
		this();
		addErrorMessage( errorMessage );
	}
	
	public SiteException( Integer errorCode ){
		this();
		addErrorCode( errorCode );
	}

	public void addErrorMessage(String message) {
		errorMessages.add( message );
	}

	public void addErrorCode(Integer errorCode) {
		errorCodes.add( errorCode );
	}

	public List<String> getErrorMessages() {
		return errorMessages;
	}

	public List<Integer> getErrorCodes() {
		return errorCodes;
	}

	/**
	 * This method is supposed to be called on the client side to go
	 * through all of the errorCodes and to add them errorMEssages.
	 * @param errorMsgI18N the error messages iternationalization object
	 */
	public final void processErrorCodes( UIErrorMessages errorMsgI18N ){
		//This method call populates errorCodeToMessage with localized messages
		populateLocalizedMessages( errorMsgI18N );
		//Iterate through all the error codes
		Iterator<Integer> it = errorCodes.iterator();
		while( it.hasNext() ){
			String errorMessage = errorCodeToMessage.get( it.next() );
			if( errorMessage != null  ){
				errorMessages.add( errorMessage );
			} else {
				errorMessages.add( "UNKNOWN ERROR CODE" );
			}
			it.remove();
		}
	}
	
	/**
	 * Adds localized messages to the hash map errorCodeToMessage
	 * @param errorCode the error code
	 * @param localizedMessage the localized error message
	 */
	protected void addLocalizedErrorMessage( Integer errorCode, String localizedMessage ){
		errorCodeToMessage.put( errorCode, localizedMessage );
	}
			
	/**
	 * This method populates errorCodeToMessage with localized messages
	 * @param errorMsgI18N the error messages iternationalization object
	 */
	public abstract void populateLocalizedMessages( UIErrorMessages errorMsgI18N );
	
	/**
	 * @return true if this exception contains error messages or error codes
	 */
	public boolean containsErrors() {
		return !this.errorCodes.isEmpty() || !this.errorMessages.isEmpty();
	}
}