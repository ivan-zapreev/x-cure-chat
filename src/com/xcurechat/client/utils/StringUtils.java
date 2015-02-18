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
 * The client-side utilities package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.utils;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.xcurechat.client.rpc.exceptions.MessageException;
import com.xcurechat.client.utils.FlashEmbeddedObject.ObjectTagSearchHelper;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;
import com.xcurechat.client.utils.widgets.URLWidget;

/**
 * @author zapreevis
 * This class should contain the common string utility functions
 */
public class StringUtils {
	//A reply title prefix string
	private static final String REPLY_MESSAGE_TITLE_PREFIX = "Re:";
	//Contains the possible url prefixes that are used in the web links
	public static final Set<String> urlPrefixes = new HashSet<String>();
	//The simple www prefix of the url
	public static final String WWW_URL_PREFIX = "www.";
	//The simple http:// prefix of the url
	public static final String HTTP_URL_PREFIX = "http://";
	
	static {
		urlPrefixes.add( HTTP_URL_PREFIX );
		urlPrefixes.add( "https://" );
		urlPrefixes.add( "ftp://" );
		urlPrefixes.add( WWW_URL_PREFIX );
	}
	
	/**
	 * Get a string representation of the header that includes an image and some text.
	 * @param text the header text
	 * @param imageURL the image URL
	 * @return the header as a string
	 */
	public static String getHeaderString(String text, String imageURL) {
		return getHeaderString( text, new Image( imageURL ) );
	}
	
	/**
	 * Get a string representation of the header that includes an image and some text.
	 * @param text the header text
	 * @param imagePrototype the {@link AbstractImagePrototype} to add next to the header
	 * @return the header as a string
	 */
	public static String getHeaderString(String text, AbstractImagePrototype imagePrototype) {
		return getHeaderString( text, imagePrototype.createImage() );
	}
	
	/**
	 * Get a string representation of the header that includes an image and some text.
	 * @param text the header text
	 * @param image the {@link Image} to add next to the header
	 * @return the header as a string
	 */
	public static String getHeaderString(String text, Image image) {
		// Add the image and text to a horizontal panel
		HorizontalPanel hPanel = new HorizontalPanel();
		hPanel.setSpacing(0);
		hPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		hPanel.add(image);
		HTML headerText = new HTML(text);
		headerText.setStyleName("cw-StackPanelHeader");
		hPanel.add(headerText);
		
		// Return the HTML string for the panel
		return hPanel.getElement().getString();
	}
	
	/**
	 * This method tries to guess if a given string is a URL
	 * @param token the string to be tested
	 * @return true if we think that this string is a URL
	 */
	public static boolean isAWebLink(String token) {
		Iterator<String> iter = urlPrefixes.iterator();
		while( iter.hasNext() ) {
			if( token.startsWith( iter.next() ) ) {
				return true;
			}
		}
		return false;
	}
	
	//The maximum text length to be placed in one label
	public static final int MAXIMUM_SINGLE_LABEL_LENGTH_SYMBOLS = 100;
	
	/**
	 * Allows to take some text, convert it to a single line, i.e. with no new line
	 * symbols in it and then to split all words in the text that are longer than
	 * the given maximum length. Also replace tabulation symbols with spaces. Note
	 * that this method does try to recognize URL's and not to split them.
	 * @param text the text to format
	 * @param MAX_WORD_LENGTH the maximum word length
	 * @param embedYoutubeVideo if set to true and a url contained in text points to 
	 *							a youtube video then we embed this video, else we make 
	 *							the weblink to open the dialog with the embedded
	 *						    youtube flash.
	 * @return the method returns list of widgets. labels for
	 * text and URLs for the found web links.
	 */
	public static List<Widget> makeOneLineSplitLongWords( final String text, final int MAX_WORD_LENGTH,
														  final boolean embedYoutubeVideo ) {
		List<Widget> result = new ArrayList<Widget>();
		text.replace('\n', ' ');
		text.replace('\t', ' ');
		String[] allTokens = text.split("\\s");
		String oneLabelString = "";
		for(int i = 0; i < allTokens.length; i++) {
			String token = allTokens[i];
			//Test the token for being a URL
			if( !isAWebLink( token ) ) {
				while( token.length() > MAX_WORD_LENGTH ) {
					oneLabelString = completeLabelOrAccumulateString( oneLabelString, token.substring(0, MAX_WORD_LENGTH) + " ", result);
					token = token.substring( MAX_WORD_LENGTH );
				}
				oneLabelString = completeLabelOrAccumulateString( oneLabelString, token + " ", result);
			} else {
				//It is time to complete the label because we are about to add a link widget
				addLabelIfNeeded( oneLabelString, result );
				//Reset the one label string to an empty one
				oneLabelString = "";
				//Add the URL widget, handle the youtube videos
				result.add( new URLWidget( token, true, embedYoutubeVideo ) );
			}
		}
		//In case there is something left in the oneLabelString, we make a label out of it here
		addLabelIfNeeded( oneLabelString, result );
		return result;
	}
	
	/**
	 * Allows to add a new label with the oneLabelString content in case it is not an empty string (when trimmed)
	 * @param oneLabelString the string to create a label from
	 * @param result the list of widgets to which the label will be added
	 */
	private static void addLabelIfNeeded(final String oneLabelString, List<Widget> result) {
		if( !oneLabelString.trim().isEmpty() ) {
			//Add the Label Widget
			result.add( new  Label( oneLabelString ) );
		}
	}
	
	/**
	 * Allows to construct the longest label possible
	 * @param oneLabelString the current string that will become a one label
	 * @param newToken the new token to be added to the current one label string
	 * @param result the list of widgets to which we add a new label as soon as
	 * we accumulated a one label string of a maximum possible length 
	 * @return either the extended one label string or the newToken, if
	 * the label was created our of the oneLabelString
	 */
	private static String completeLabelOrAccumulateString( final String oneLabelString, final String newToken, List<Widget> result) {
		if( ( oneLabelString.length() < MAXIMUM_SINGLE_LABEL_LENGTH_SYMBOLS ) &&
			( ( oneLabelString.length() + newToken.length() ) < MAXIMUM_SINGLE_LABEL_LENGTH_SYMBOLS ) ) {
			//If one label string allows for more text to be put in and 
			//the one label string plus the new token is not too long
			//we concatenate them and return without creating a new Label
			return oneLabelString + newToken;
		} else {
			//If the one label string or the one label string plus
			//the new token is too long then we create a label out
			//of the current label string and make a new one to be
			//the new token, this way we will keep the process continuous
			result.add( new  Label( oneLabelString ) );
			return newToken;
		}
	}
	
	/**
	 * This is a simple formatting function, at the mooment we only split things
	 * in rows of MAX_WIDTH  symbols each. Accounts for URLs.
	 * @param MAX_TEXT_WIDTH the width of the row in symbols
	 * @param text the text to be formatted
	 * @param isDiscardNewLines if true then all new line symbols in the original text are discarded
	 * @return the reformatted string
	 */
	public static String formatTextWidth(final int MAX_TEXT_WIDTH, final String text, final boolean isDiscardNewLines) {
		return formatTextWidth(MAX_TEXT_WIDTH, text, isDiscardNewLines, false);
	}
	
	/**
	 * This is a simple formatting function, at the mooment we only split things in rows of MAX_WIDTH  symbols each.
	 * Accounts for URLs in a sense that we take the maximum visible URL length and try to assume
     * it if the actual URL's length is bigger. If we can not fit the URL with the assumed length
	 * on one line, we still split it.
	 * @param MAX_TEXT_WIDTH the width of the row in symbols
	 * @param text the text to be formatted
	 * @param isDiscardNewLines if true then all new line symbols in the original text are discarded
	 * @param accountForURLs if true then we account for URLs by assuming that the maximum visible URL Length is URLWidget.MAX_VISIBLE_URL_LENGTH
	 * @return the reformatted string
	 */
	public static String formatTextWidth(final int MAX_TEXT_WIDTH, final String text, final boolean isDiscardNewLines, final boolean accountForURLs) {
		//Remove all of the new line and tabulation symbols
		if( isDiscardNewLines ) {
			text.replace('\n', ' ');
		}
		text.replace('\t', ' ');
		//Split the string into several lines based on current new lines and then process each line
		String new_text = "";
		int last_new_line_index = -1, next_new_line_index = 0;
		while( last_new_line_index < text.length() ) {
			next_new_line_index = text.indexOf('\n', ++last_new_line_index);
			if( next_new_line_index == -1 ) {
				next_new_line_index = text.length();
			}
			new_text += formatOneLineTextWidth( MAX_TEXT_WIDTH, text.substring(last_new_line_index, next_new_line_index ), accountForURLs ) + "\n";
			last_new_line_index = next_new_line_index; 
		}
		return removeEndOfLineAtTheEnd( new_text );
	}
	
	/**
	 * This method allows to format one long line that does not contain new lines and tab symbols
	 * Accounts for URLs in a sense that we take the maximum visible URL length and try to assume
     * it if the actual URL's length is bigger. If we can not fit the URL with the assumed length
	 * on one line, we still split it.
	 * @param MAX_LINE_WIDTH the width of the row in symbols
	 * @param text the text to be formatted, we assume that there are no new line symbols in the text
	 * @param accountForURLs if true then we account for URLs by assuming that the maximum visible URL Length is URLWidget.MAX_VISIBLE_URL_LENGTH
	 * @return the reformatted string
	 */
	private static String formatOneLineTextWidth( final int MAX_LINE_WIDTH, final String text, final boolean accountForURLs ) {
		String[] allTokens = text.split("\\s");
		String new_text = "";
		String current_line = "";
		for( int i = 0; i < allTokens.length; i++ ) {
			String token = allTokens[i].trim();
			//If it is a weblink then we assume the length that is used to visualize it 
			int tokenLength = accountForURLs && isAWebLink(token) ? URLWidget.getVisibleURLTextLength( token.length() ) : token.length();
			
			//Check if the current line + one space + next token is not exceeding the max length.
			//We also take into account that the current line can be empty at the moment.
			final int est_line_length = tokenLength + (current_line.isEmpty() ? 0 : (current_line.length() + 1) ) ;
			if( est_line_length > MAX_LINE_WIDTH ){
				if( tokenLength > MAX_LINE_WIDTH ){
					//The token is too long to fit on one line, we have to split it.
					final int remained_space = MAX_LINE_WIDTH - ( current_line.length() + ( current_line.isEmpty() ? 0 : 1 ) );
					if( remained_space > 0 ) {
						//If there is still some space to use on this line then use it
						new_text += (current_line.isEmpty() ? "" : (current_line + " ") ) + token.substring(0, remained_space) + '\n';
						//Keep only the remainder of the token
						token = token.substring( remained_space, token.length() );
					} else {
						//There is not space left so we complete the line
						new_text += current_line + '\n';
					}
					//By now there should be nothing to be put on the current_line. The following 
					//cycle fills the entire lines and then puts the remainder on a new line.
					//NOTE: Here we do not care if it s URL or not any more because it has been split any ways
					while( token.length() > MAX_LINE_WIDTH ){
						//Take another substring from the token  
						boolean isRemained = (token.length() > MAX_LINE_WIDTH );
						new_text += token.substring(0, ( isRemained ? MAX_LINE_WIDTH : token.length() ) ) + '\n';
						token = ( isRemained ? token.substring( MAX_LINE_WIDTH , token.length()) : "");
					}
					//Add the remaining text onto the next line
					current_line = token + " ";
				} else {
					//Simply start the new line
					new_text += current_line + '\n';
					current_line = token + " ";
				}
			} else {
				//If we still can add the token to the line then add it
				current_line += token + " ";
			}
		}
		//if there is smth left, which we did not append to the result text, then do it now.
		if( !current_line.isEmpty() ){
			new_text += current_line;
		}
		//Remove the extra end of line symbol if any
		return removeEndOfLineAtTheEnd(new_text);
	}

	/**
	 * Remove the end of line symbol if any
	 * @param a non null text to process
	 * @param the same text but with no end of line at its end
	 */
	private static String removeEndOfLineAtTheEnd(final String text) {
		String resultText = text;
		final int last_symbol_index = resultText.length() - 1;
		if( resultText.charAt( last_symbol_index ) == '\n' ) {
			resultText = resultText.substring( 0 , last_symbol_index );
		}
		return resultText;
	}

	/**
	 * This method counts the number of lines needed to put the string
	 * If the string is null then the nuber of lines is 0, if the
	 * string is empty then the number of lines is one.
	 * @param text the string to count new lines in 
	 * @return the number of new line symbols in the string 
	 */
	public static int countLines(final String text) {
		int count = 1;
		if( text != null ) {
			for( int i = 0 ; i < text.length() ; i ++){
				if( text.charAt(i) == '\n' ) {
					count ++;
				}
			}
		}
		return count;
	}
	
	/**
	 * Allows to convert the given title into a "reply" title
	 * @param messageTitle the title to add a reply prefix to
	 * @return the title prefixed with the reply prefix if the
	 *		   title is null or is an empty string then we 
	 *		   return an empty string with no prefix.
	 */
	public static String makeReplyMessageTitle( final String title ) {
		String resultTitle = "";
		
		if( ( title != null ) && ( ! title.trim().isEmpty() ) ) {
			String workTitle = title.trim();
			
			//Remove smileys
			workTitle = SmileyHandler.removeAllSmileyCodes( workTitle );
			
			resultTitle = REPLY_MESSAGE_TITLE_PREFIX + " " + workTitle;
		}
		
		return resultTitle;
	}
	
	/**
	 * Allows to convert the given message text body into a "reply" message body
	 * @param messageBody the message body to convert
	 * @return the converted reply-style body, if the body is null then we return an empty string
	 */
	public static String makeReplyMessageBody( final String body ) {
		String resultBody = "";
		
		//Form the reply message body from the original message
		if( ( body != null ) && ( ! body.trim().isEmpty() ) ) {
			String workBody = body.trim();
			
			//Remove smileys
			workBody = SmileyHandler.removeAllSmileyCodes( workBody );
			
			//This helper object is used to remove the embedded object tags from a string
			final ObjectTagSearchHelper<String> removeEmbeddedObjectHelper = new ObjectTagSearchHelper<String>(){
				//The string that accumulates the output string
				private String outputText = "";
				
				public void processObjectFreeSubstring( final String substring ) throws MessageException {
					//Append the regular text, add one space in the end just in case
					outputText += substring + " ";
				}
				public void processObjectTagSubstring( final String substring ) throws MessageException {
					//The embedded object has to be removed
				}
				public void onObjectCloseTagNotFound() throws MessageException {
					//Let this <OBJECT tag to be removed and the rest of the string to be treated as a simple text
				}
				public String getResult() {
					return outputText;
				}
			};
			
			//Remove the embedded object tags from the message body
			try {
				FlashEmbeddedObject.processEmbeddedTags( workBody , removeEmbeddedObjectHelper);
				workBody = removeEmbeddedObjectHelper.getResult();
			} catch ( MessageException e ) {
				//This should not be happening because our helper object does not throw any exceptions
			}
			
			//Prefix each line in the stripped message with the ">>" symbol
			String[] allLines = workBody.split("\\n");
			for( int i = 0; i < allLines.length; i ++) {
				resultBody += CommonResourcesContainer.REPLY_LINE_PREFIX + allLines[i] + "\n";
			}
		}
		
		return resultBody;
	}
}
