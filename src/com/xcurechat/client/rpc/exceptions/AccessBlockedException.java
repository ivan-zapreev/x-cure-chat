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
 * the site was blocked due to some automated site protection.
 * This is also used to block message sending if the user is suspected
 * to be sending too many messages or typing too fast and etc
 */
public class AccessBlockedException extends InternalSiteException {
	
	public static final Integer TOO_MANU_SITE_REQUESTS_EXCEPTION_ERR = new Integer(0);
	public static final Integer SEND_MESSAGES_ABUSE_EXCEPTION_ERR = new Integer(1);
	
	static {
		//Register the error code in the super class, to avoid duplicates
		SiteLogicException.registerErrorCode( TOO_MANU_SITE_REQUESTS_EXCEPTION_ERR, true );
		SiteLogicException.registerErrorCode( SEND_MESSAGES_ABUSE_EXCEPTION_ERR );
	}
	
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;

	public AccessBlockedException(){
		super();
	}
	
	public AccessBlockedException( String errorMessage ){
		super( errorMessage );
	}
	
	public AccessBlockedException( Integer errorCode ){
		super( errorCode );
	}

	//The remaining time while the request is going to be blocked in minutes
	private int remainingBlockingTime = 0;
	
	/**
	 * Sets the remaining blocking time in minutes
	 * @param rbt remaining blocking time in minutes
	 */
	public void setRemainingBlockingTime( double rbt ){
		//Compute the rounded off time in minutes, needed to wait until blocking ends
		remainingBlockingTime = (new Double(rbt)).intValue();
		if( remainingBlockingTime < rbt ) {
			remainingBlockingTime++;
		}
	}
	
	/**
	 * Sets the remaining blocking time in minutes
	 * @param rbt remaining blocking time in minutes
	 */
	public void setRemainingBlockingTime( int rbt ) {
		this.remainingBlockingTime = rbt;
	}
	
	/**
	 * @return the remaining blocking time in minutes
	 */
	public int getRemainingBlockingTime( ) {
		return remainingBlockingTime; 
	}
	
	/**
	 * @return the number of complete hours 
	 */
	protected int getRBTHPart() {
		return remainingBlockingTime / 60;
	}
	
	/**
	 * @return the number of minutes, minus complete hours, 
	 */
	protected int getRBTMPart() {
		return remainingBlockingTime % 60;
	}
	
	@Override
	public void populateLocalizedMessages(UIErrorMessages errorMsgI18N) {
		super.populateLocalizedMessages( errorMsgI18N );

		addLocalizedErrorMessage( TOO_MANU_SITE_REQUESTS_EXCEPTION_ERR,
									errorMsgI18N.tooFrequentRequestsError( getRBTHPart(), getRBTMPart() ));
		addLocalizedErrorMessage( SEND_MESSAGES_ABUSE_EXCEPTION_ERR, errorMsgI18N.sendMessageAbuseDetected() );
	}

}
