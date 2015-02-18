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
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.utils;

import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;

/**
 * @author zapreevis
 * This class handles the uncought exceptions in the client side of the site
 * Currently it will track if some exception accures consequently more than
 * several times within a certain interval of time and if yes ten relads the
 * site. In the fure it shuld also report to the server about all errors in
 * the client.
 */
public class XCureUncaughtExceptionHandler implements UncaughtExceptionHandler {
	//The maximum number of consequent (equivalent) exceptions before the site reloads
	private static final int MAX_CONSEQUENT_EXCEPTIONS = 5;
	//The interval of time within which we monitore consequent excetpions
	private static final int MAX_TIME_FRAME_MILLISEC = 60000;
	
	//The time at which the first exeption in the sequence occured
	private long timeFirstConseqException = 0;
	//The string representation of the exception
	private String exceptionString = "";
	//The number of consequet (equivalent) exceptions
	private int consequentExceptionCounter = 0;
	
	/**
	 * Currently what I obsere is that GWT 1.6 with Firefox 3.0.11 on Linux
	 * some times breaks down with random exceptions, these are typically
	 * not influencing the work of the web site, but, some times things break
	 * down really bad and then GWT starts throwing sequence of the same kind
	 * of exceptions constantly and the web site stops working, this is what
	 * we try to prevent here, if such thing hapen then we reload the website
	 */
	public void onUncaughtException(Throwable e) {
		final long currentTimeMillisec = System.currentTimeMillis();
		if( exceptionString.equals( e.toString() ) && ( ( currentTimeMillisec - timeFirstConseqException ) < MAX_TIME_FRAME_MILLISEC ) ) {
			//If we got the same exception as the last time and
			//if the time since this exception occured the
			//first time in the present sequence is less than
			//the onsidered time frame, then
			if( consequentExceptionCounter >= MAX_CONSEQUENT_EXCEPTIONS ) {
				//If we had more than allowed consequent exception of this kind
				InterfaceUtils.reloadWebSitePages();
			} else {
				//If we still did not exceed the maximum number of exceptions
				//of this kind then simply increment the counter
				consequentExceptionCounter++;
			}
		} else {
			//The exception is not consequent or we are outside of the time frame
			//Reset the values to treat this exception as the first in sequence
			consequentExceptionCounter = 1;
			timeFirstConseqException = currentTimeMillisec;
			exceptionString = e.toString();
		}
	}
};
