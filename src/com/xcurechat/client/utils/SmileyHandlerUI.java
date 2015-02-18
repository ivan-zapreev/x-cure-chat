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
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.utils.SmileyHandler.SmileyInfo;

/**
 * @author zapreevis
 * The part of the smiley handler class that has ties with the UI and can not be used in the server side
 */
public class SmileyHandlerUI {
	
	//The minimum windows sizes in pixels to show the large smileys
	private static final int IDEAL_CLIENT_VIEW_WIDTH = 1024;
	private static final int IDEAL_CLIENT_VIEW_HEIGHT = 768;

	//Maps the category ids to their names
	private static final Map<Integer, String> categoryIdToTitle = new HashMap<Integer, String>();
	
	//Determines the reduction factor for the smiley sizes in the interval (0, 1]
	private static double reductionFactor = 1.0;
	
	/**
	 * Allows to get the category title by the category id
	 * @param categoryId the id of the category
	 * @return the category title or null if a category with the given id does not exist
	 */
	public static String getCategoryTitle( final int categoryId ) {
		return categoryIdToTitle.get( categoryId );
	}
	
	static{
		adjustSmileySizes( );
		
		categoryIdToTitle.put( SmileyHandler.UNKNOWN_CATEGORY_ID, "Unknown" );
		categoryIdToTitle.put( SmileyHandler.FAVORITES_CATEGORY_ID, I18NManager.getTitles().favoriteSmileCagtegoryTitle() );
		categoryIdToTitle.put( SmileyHandler.PRESENTS_CATEGORY_ID, I18NManager.getTitles().presentsSmileCagtegoryTitle() );
		categoryIdToTitle.put( SmileyHandler.TEASING_CATEGORY_ID, I18NManager.getTitles().teasingSmileCagtegoryTitle() );
		categoryIdToTitle.put( SmileyHandler.TALKING_CATEGORY_ID, I18NManager.getTitles().talkingSmileCagtegoryTitle() );
		categoryIdToTitle.put( SmileyHandler.RELAXING_CATEGORY_ID, I18NManager.getTitles().relaxingSmileCagtegoryTitle() );
		categoryIdToTitle.put( SmileyHandler.EMOTIONS_CATEGORY_ID, I18NManager.getTitles().emotionsSmileCagtegoryTitle() );
		categoryIdToTitle.put( SmileyHandler.LOVE_CATEGORY_ID, I18NManager.getTitles().loveSmileCagtegoryTitle() );
		categoryIdToTitle.put( SmileyHandler.MISC_CATEGORY_ID, I18NManager.getTitles().miscSmileCagtegoryTitle() );
		//categoryIdToTitle.put( SmileyHandler.FUNNY_CATEGORY_ID, I18NManager.getTitles().funnySmileCagtegoryTitle() );
	}
	
	/**
	 * Allows to compute how much smaller the given size is than the ideal size
	 * @param idelSize the ideal size is the desired size, it is also ok if the given size is larger than the idel size.
	 * 					In the latter case the method returns 1.0
	 * @param givenSize the given size is the size we want to test against the ideal size
	 * @return is givenSize<idelSize then we return the reduction factor givenSize/idelSize otherwise we return 1.0  
	 */
	private static double getReductionFactor(final int givenSize, final int idelSize ) {
		if( ( givenSize > 0 ) && ( givenSize < idelSize ) ) {
			//Compute how much smaller the given size is, i.e. the reduction factor
			return ((double)givenSize) / ((double)idelSize);
		} else {
			//The client view is large enough to use the original smiley size;
			return 1.0;
		}
	}
	
	/**
	 * Should be invoked to notify the smiley handler about which size of smileys to use in the messages
	 */
	public static void adjustSmileySizes( ) {
		reductionFactor = Math.min( getReductionFactor( Window.getClientHeight(), IDEAL_CLIENT_VIEW_HEIGHT ),
									getReductionFactor( Window.getClientWidth(),  IDEAL_CLIENT_VIEW_WIDTH ) );
	}
	
	/**
	 * Takes the chat message string and returns a list of widgets which are
	 * labeled text and smiles to be put into the chat message view. Also it
	 * completes the message by converting all the smile symbols into the
	 * internal smile codes and splits the messages in the chunks of not
	 * longer than MAX_ONE_WORD_LENGTH characters.
	 * NOTE: We split the string into "lines" that are no longer than
	 * MAX_ONE_WORD_LENGTH symbols, i.e. basically we make sure that the
	 * words are not longer than that, we do it here, just because this way
	 * we do not spoil the smiley codes, alternatively we could have done it
	 * before the chat message is send, but then we could break down some of
	 * the smiley codes
	 * @param messageBody the message to process
	 * @param MAX_ONE_WORD_LENGTH the maximum one word length in the chat message string
	 * @param embedYoutubeVideo if set to true and a url contained in text points to 
	 *							a youtube video then we embed this video, else we make 
	 *							the weblink to open the dialog with the embedded
	 *						    youtube flash.
	 * @return the list of widgets
	 */
	public static List<Widget> getMessageViewObject( final String inMessageBody, final int MAX_ONE_WORD_LENGTH,
			  										 final boolean embedYoutubeVideo ){
		List<Widget> listOfWidgets = new ArrayList<Widget>();
		
		int beginIndex = -1, endIndex = -1;
		String messageBody = inMessageBody;
		while( ( endIndex = messageBody.indexOf( SmileyHandler.SMILEY_WRAPPER_STRING, beginIndex + 1 ) ) != -1 ) {
			if( beginIndex != -1 ) {
				final String possibleSmileyCode = messageBody.substring( beginIndex + 1, endIndex );
				if( possibleSmileyCode.matches( SmileyHandler.DIGIT_SMILEY_CODE_PATTERN ) ) {
					//If looks like we have ourselves a smile
					try{
						final Image smile = getSmileIcon( Integer.parseInt( possibleSmileyCode ) );
						if( smile != null ) {
							//This is a valid smile code, first create a label out of the prefix
							listOfWidgets.addAll( StringUtils.makeOneLineSplitLongWords( messageBody.substring( 0, beginIndex ),
																						 MAX_ONE_WORD_LENGTH, embedYoutubeVideo ) );
							//Truncate the message 
							if( ( endIndex + 1 ) < messageBody.length() ) {
								messageBody = messageBody.substring( endIndex + 1 );
								//The message string has been truncated restart the indexes
								beginIndex = -1;
								endIndex = -1;
							} else {
								//There will be no more smiles, the message ends here
								messageBody = "";
							}
							//Set the alt text for copy-paste purposes
							DOM.setElementProperty( smile.getElement(), "alt", SmileyHandler.SMILEY_WRAPPER_STRING + possibleSmileyCode + SmileyHandler.SMILEY_WRAPPER_STRING );
							listOfWidgets.add( smile );
						} else {
							//Although this looks like a valid smile code,
							//there is no such smile continue searching
							beginIndex = endIndex;
						}
					} catch ( NumberFormatException e ) {
						//This is not a valid smile integer code somehow, continue searching
						beginIndex = endIndex;
					}
				} else {
					//This was not a smile code so we move on starting from the end
					beginIndex = endIndex;
				}
			} else {
				//This is the first iteration, we need to find the so
				//we need the second boundary index for it smile index 
				beginIndex = endIndex;
			}
		}
		
		if( !messageBody.trim().equals("") ) {
			//There is something left in the message, make a label out of it since there can be no smiles
			listOfWidgets.addAll( StringUtils.makeOneLineSplitLongWords( messageBody, MAX_ONE_WORD_LENGTH, embedYoutubeVideo ) );
		}
		
		return listOfWidgets;
	}

	/**
	 * Returns the smile icon for the provided internal smile code.
	 * Note that the smiley's image size is determined by the client view.
	 * @param smileInternalCode the internal smile code
	 * @return the smile icon or null for the unknown smiley code
	 */
	public static Image getSmileIcon( final int smileInternalCode ) {
		final Image smile;
		final SmileyInfo info = SmileyHandler.getSmileyInfo( smileInternalCode );
		if( info != null ) {
			smile = new Image( info.url );
			smile.setSize( ((int) Math.ceil( reductionFactor * info.width  ) ) + "px",
					   	   ((int) Math.ceil( reductionFactor * info.height ) ) + "px" );
		} else {
			smile = null;
		}
		return smile;
	}
}
