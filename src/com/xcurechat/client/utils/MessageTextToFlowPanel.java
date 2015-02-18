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

import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import com.xcurechat.client.utils.FlashEmbeddedObject;
import com.xcurechat.client.utils.FlashEmbeddedObject.ObjectTagSearchHelper;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.FlashObjectWrapperUI;


import com.xcurechat.client.data.ChatMessage;
import com.xcurechat.client.rpc.exceptions.MessageException;

/**
 * @author zapreevis
 * This class is responsible for converting the message text into the list of widgets that are then added to the provided flow panel.
 * It takes the message text, parse out the embedded plash animations, then process split the text line by line and process smileys
 * along with the lines that are ">>" citations.
 */
public class MessageTextToFlowPanel implements ObjectTagSearchHelper<FlowPanel> {
	//The provided message body flow panel, to add the message widgets
	private final FlowPanel messageBodyFlowPanel;
	//The message body text to turn into widgets
	private final String messageBodyText;
	//The quote line prefix string
	private final String messageLineQuotePrefix;
	//True if we can have quotations 
	private final boolean needToQuote;
	//True if the youtube video should be embedded by the link
	private final boolean embedYoutubeVideo;
	/**
	 * The basic constructor
	 * @param messageBodyFlowPanel the provided message body flow panel, to add the message widgets
	 * @param messageBodyText the message body text to turn into widgets
	 * @param messageLineQuotePrefix the line prefix that indicates the quoted line
	 * @param embedYoutubeVideo if set to true and a url contained in text points to 
	 *							a youtube video then we embed this video, else we make 
	 *							the weblink to open the dialog with the embedded
	 *						    youtube flash.
	 */
	public MessageTextToFlowPanel( final FlowPanel messageBodyFlowPanel, final String messageBodyText,
								   final String messageLineQuotePrefix, final boolean embedYoutubeVideo ) {
		this.messageBodyFlowPanel = messageBodyFlowPanel;
		this.messageBodyText = messageBodyText;
		this.messageLineQuotePrefix = messageLineQuotePrefix;
		this.needToQuote = messageLineQuotePrefix != null;
		this.embedYoutubeVideo = embedYoutubeVideo;
	}
	
	/**
	 * Processes the provided text message and turns it into the list of widgets that are then added to the provided FlowPanel.
	 * Also works with the embedded Flash, i.e. allows to place a widget for it.
	 * @return the provided FlowPanel inhabited by the message body widgets
	 */
	public FlowPanel process() {
		return process( false );
	}
	
	/**
	 * Processes the provided text message and turns it into the list of widgets that are then added to the provided FlowPanel
	 * @param doNotProcessEmbedded if true then the embed and object tags are treated as simple text
	 * @return the provided FlowPanel inhabited by the message body widgets
	 */
	public FlowPanel process( boolean doNotProcessEmbedded ) {
		try {
			if( messageBodyText != null ) {
				if( doNotProcessEmbedded ) {
					//Treat everything as text with smileys and urls
					processObjectFreeSubstring( messageBodyText );
				} else {
					//Process the embedded object tags, there should be no stand alone EMBEDDED tags, only enclosed by OBJECT
					FlashEmbeddedObject.processEmbeddedTags( messageBodyText, this );
				}
			}
		} catch ( MessageException e ) {
			//Should not be happening, but if it does do not process the flash objects!
			try {
				processObjectFreeSubstring( messageBodyText );
			} catch ( MessageException ex ) {
				//The exception is never thrown
			}
		}
		return messageBodyFlowPanel;
	}
	
	@Override
	public void processObjectFreeSubstring( final String substring ) throws MessageException {
		if( substring != null ) {
			//1. Take the string and split it into separate lines
			String[] lines = substring.split("\\n");
			for( String line : lines ) {
				//2. For each line check if it starts with ">>" or some other substring that determines the quote, if any
				final boolean isQuote = needToQuote && line.startsWith( messageLineQuotePrefix );
				//3. For each line substitute smileys
				List<Widget> messageLineWidgets = SmileyHandlerUI.getMessageViewObject( line, ChatMessage.MAX_ONE_WORD_LENGTH,
																						embedYoutubeVideo );
				//4. For each line starting with ">>" make sure that a distinctive text syle is set
				FlowPanel linePanel = new FlowPanel();
				for( Widget w : messageLineWidgets ) {
					if( isQuote ) {
						w.addStyleName( CommonResourcesContainer.QUOTE_MESSAGE_TEXT_STYLE );
					} else {
						w.addStyleName( CommonResourcesContainer.BASIC_MESSAGE_TEXT_STYLE );
					}
					MessageTextToFlowPanel.addContentWidget( linePanel, w, true);
				}
				//If there are no widgets then this is just an empty line, so ass the spacing
				if( linePanel.getWidgetCount() == 0 ) {
					MessageTextToFlowPanel.addContentWidget( linePanel, new HTML("&nbsp;"), false);
				}
				//5. Add each line widgets to a separate flow panel, each of which
				//   will not be inline but the widgets inside it will be
				messageBodyFlowPanel.add( linePanel );
			}
		}
	}
	
	@Override
	public void processObjectTagSubstring( final String substring ) throws MessageException {
		if( substring != null ) {
			messageBodyFlowPanel.add( new FlashObjectWrapperUI( substring, true, null ) );
		}
	}
	
	@Override
	public void onObjectCloseTagNotFound() throws MessageException {
		throw new MessageException( MessageException.IMPROPER_EMBEDDED_OBJECT );
	}
	
	@Override
	public FlowPanel getResult() {
		return messageBodyFlowPanel;
	}

	/**
	 * Adds a widget to the message content, makes sure that the widget has an inline display
	 * @param content the flow panel to add the content to
	 * @param w the widget to add
	 * @param addSpacing if true then adds spacing between the old widgets and the new one
	 */
	public static void addContentWidget( final FlowPanel content, final Widget w, final boolean addSpacing ) {
		if( ( content != null ) && ( w != null ) ) {
			if( addSpacing && ( content.getWidgetCount() > 0 ) ) {
				//If there are already widgets inside then add a separator
				//We add a white space label because opera does not break
				//lines in the flow panel if it is an HTML("&nbsp;")
				Label separator = new Label(" "); 
				separator.addStyleName( CommonResourcesContainer.FORCE_INLINE_DISPLAY_STYLE );
				content.add( separator );
			}
			
			//Make sure that the content element has the inline display
			w.addStyleName( CommonResourcesContainer.FORCE_INLINE_DISPLAY_STYLE );
			content.add( w );
		}
	}

}
