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
package com.xcurechat.server.utils;

import com.xcurechat.client.utils.FlashEmbeddedObject;
import com.xcurechat.client.utils.FlashEmbeddedObject.ObjectTagSearchHelper;

import com.xcurechat.client.rpc.exceptions.MessageException;

/**
 * @author zapreevis
 * This class is responsible for splitting the message text into the strings where all non-embed object strings are escaped.
 * The embed-object strings are considered to be safe.
 */
public class MessageSplitHTMLEscape implements ObjectTagSearchHelper<String> {
	
	//The resulting string storing the output text
	private String resultingString = "";
	//The message body text to turn into widgets
	private final String messageBodyText;
	/**
	 * THe basic constructor
	 * @param messageBodyText the message body text to turn into widgets
	 */
	public MessageSplitHTMLEscape(final String messageBodyText) {
		this.messageBodyText = messageBodyText;
	}
	
	/**
	 * Processes the provided text message and turns it into the
	 * strings where all non-embed object strings are escaped.
	 */
	public String process( ) {
		try {
			if( messageBodyText != null ) {
				//Process the embedded object tags, there should be no stand alone EMBEDDED tags, only enclosed by OBJECT
				FlashEmbeddedObject.processEmbeddedTags( messageBodyText, this );
			}
		} catch ( MessageException e ) {
			//Should not be happening, but if it does do not process the flash objects!
			try {
				processObjectFreeSubstring( messageBodyText );
			} catch ( MessageException ex ) {
				//The exception is never thrown
			}
		}
		return resultingString;
	}
	
	@Override
	public void processObjectFreeSubstring( final String substring ) throws MessageException {
		if( substring != null ) {
			//1. Take the string and split it into separate lines
			String[] lines = substring.split("\\n");
			for( String line : lines ) {
				resultingString += " " + EscapeHTMLHelper.escapeHTML( line ) + "<br/>";
			}
		}
	}
	
	@Override
	public void processObjectTagSubstring( final String substring ) throws MessageException {
		resultingString += " " + substring;
	}
	
	@Override
	public void onObjectCloseTagNotFound() throws MessageException {
		throw new MessageException( MessageException.IMPROPER_EMBEDDED_OBJECT );
	}
	
	@Override
	public String getResult() {
		return resultingString;
	}

}
