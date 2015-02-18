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
 * The user interface utils package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.client.utils;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.utils.widgets.FlashObjectWrapperUI;
import com.xcurechat.client.utils.widgets.URLWidget;

/**
 * @author zapreevis
 * This class contains helper method fot handling youtube links
 */
public class YoutubeLinksHandlerUtils {

	/**
	 * This slass only provides static methods
	 */
	private YoutubeLinksHandlerUtils() { }

	
	/**
	 * Allows to check if the given link is a utube link and if it is then returns the utube video url for it
	 * @param validUrl the original url
	 * @return the utube video url or null if this is not a utube video url
	 */
	public static String getUtubeVideoUrl( final String validUrl ) {
		String utubeVideoUrl = null;
		final String UTUBE_URL_PREFIX = "http://www.youtube.com/watch?v=";
		if( validUrl.startsWith( UTUBE_URL_PREFIX ) ) {
			//Change the prefix
			utubeVideoUrl = validUrl.replace( UTUBE_URL_PREFIX , "http://www.youtube.com/v/" );
			//Remove any other parameters
			utubeVideoUrl = utubeVideoUrl.replaceAll("\\"+ServerSideAccessManager.SERVLET_PARAMETERS_DELIMITER+".*", "");
			//Add extra parameters
			utubeVideoUrl += "&amp;hl=" + InterfaceUtils.getCurrentLocale() + "&amp;fs=1";
		}
		return utubeVideoUrl;
	}
	
	/**
	 * Allows to get a flash object for the youtube video url
	 * @param yutubeMediaUrl the youtube video url
	 * @param youtubeURL the original youtube link url
	 * @param isInitiallyBlocked is true if the flash should be initially blocked
	 * @return the corresponding flash object widget
	 */
	public static Widget getYoutubeEmbeddedFlashObject( final String yutubeMediaUrl, final String youtubeURL, final boolean isInitiallyBlocked ) {
		//Construct the youtube embedded object descriptor
		FlashEmbeddedObject flashObject = new FlashEmbeddedObject(  null );
		flashObject.setMovieUrl( yutubeMediaUrl );
		flashObject.completeEmbedFlash();
		
		//Make the widget out of a wrapped youtube video plus the video URL.
		VerticalPanel mainPanel = new VerticalPanel();
		mainPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
		mainPanel.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE );
		mainPanel.add( new FlashObjectWrapperUI( flashObject.toString(), isInitiallyBlocked, null ) );
		mainPanel.add( new URLWidget( youtubeURL, false, false ) );

		return mainPanel;
	}
}
