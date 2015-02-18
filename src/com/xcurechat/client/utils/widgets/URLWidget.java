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
package com.xcurechat.client.utils.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import com.xcurechat.client.dialogs.ActionGridDialog;
import com.xcurechat.client.dialogs.system.AskForURLFollowingDialogUI;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.utils.SplitLoad;
import com.xcurechat.client.utils.StringUtils;
import com.xcurechat.client.utils.YoutubeLinksHandlerUtils;

/**
 * @author zapreevis
 * This class Represents the URL Widget, i.e. a clickable image or link
 * Klicking on a URL should warn the user about following it.
 */
public class URLWidget extends Composite {
	private static final int MAX_URL_LENGTH = 45;   /*The length of the URL exceeding which the URL will get trancated*/
	private static final int URL_SUFFIX_LENGTH = 10; /*The length of the URL suffix that will be made visible*/
	private static final String URL_DOTS = "...";
	private static final int URL_DOTS_LENGTH = URL_DOTS.length();
	
	/**
	 * Allows to get the length of the visible URL link. The URL can be too long to put as is and then it is abbreviated
	 * @param actualURLLength the actual length of the URL string as in text before the abbreviation
	 * @return the length of the URL string that will be visible for a user 
	 */
	public static int getVisibleURLTextLength(final int actualURLLength) {
		if( actualURLLength > MAX_URL_LENGTH ) {
			return MAX_URL_LENGTH;
		} else {
			return actualURLLength;
		}
	}
	
	/**
	 * Allows to get the URL text as it should be shown to the user
	 * @param actualURL the actual URL string
	 * @return the possibly truncated URL string as it should be shown to the user
	 */
	private static String getVisibleURLText( final String actualURL ) {
		if( actualURL.length() > MAX_URL_LENGTH ) {
			final int actualURLLength = actualURL.length();
			final String suffix = actualURL.substring( actualURLLength - URL_SUFFIX_LENGTH, actualURLLength );
			final String prefix = actualURL.substring(0, MAX_URL_LENGTH - ( URL_SUFFIX_LENGTH + URL_DOTS_LENGTH ) );
			return prefix + URL_DOTS + suffix;
		} else {
			return actualURL;
		}
	}
	
	/**
	 * The basic constructor, converts the links starting wih www. into http://www.
	 * The latter is required for a proper opening of the web link in a new window
	 * @param url the provided url
	 * @param handleYoutubeVideo if true then we handle youtube videos by embedding
	 *							 them or opening them in a separate dialog view
	 * @param embedYoutubeVideo has effect only if "handleYoutubeVideo == true ".
	 *							Then, if set to true and the url points to a youtube
	 *							video then we embed this video, else we make the weblink
	 *							to open the dialog with the embedded youtube flash.
	 */
	public URLWidget( final String url, final boolean handleYoutubeVideo, final boolean embedYoutubeVideo ) {
		final String validUrl;
		if( url.startsWith( StringUtils.WWW_URL_PREFIX ) ) {
			validUrl = StringUtils.HTTP_URL_PREFIX + url;
		} else {
			validUrl = url;
		}
		
		//Create the URL image or link label
		final Widget urlWidget;
		final String ourSiteURL = isOurSiteURLThenRewrite( validUrl );
		
		if( ourSiteURL != null ) {
			//If this our's site URL then make it an anchor
			final Anchor anchor = new Anchor( );
			anchor.addStyleName( CommonResourcesContainer.DIALOG_LINK_BLUE_STYLE );
			anchor.setText( getVisibleURLText( ourSiteURL ) );
			anchor.setTitle( ourSiteURL );
			//If this is GWT version url we have, the locale must have been removed by now
			final int hashIndex = ourSiteURL.indexOf( ServerSideAccessManager.URI_HISTORY_TOKEN_SYMBOL );
			if( hashIndex != -1 ) {
				//If there is a hash symbol then just make a short anchor
				anchor.setHref( ourSiteURL.substring( ourSiteURL.indexOf( ServerSideAccessManager.URI_HISTORY_TOKEN_SYMBOL ) ) );
			} else {
				//If there is no hash symbol then keep the entire link but open it in a new page
				anchor.setHref( ourSiteURL );
				anchor.setTarget("_blank");
			}
			
			//Make the link to be the anchor
			urlWidget = anchor;
		} else {
			final String utubeVideoUrl = YoutubeLinksHandlerUtils.getUtubeVideoUrl( validUrl );
			if( ( utubeVideoUrl != null ) && embedYoutubeVideo && handleYoutubeVideo ) {
				//If this a yutube video url and we need to get an embedded object then make it
				urlWidget = YoutubeLinksHandlerUtils.getYoutubeEmbeddedFlashObject( utubeVideoUrl, validUrl, true );
			} else {
				//If this some other's site url then make is a label with the reqirect confirmation dialog.
				Label label = new Label( );
				label.addStyleName( CommonResourcesContainer.FORCE_INLINE_DISPLAY_STYLE );
				label.addStyleName( CommonResourcesContainer.DIALOG_LINK_BLUE_STYLE );
				//Set text, the url we want to show
				label.setText( getVisibleURLText( validUrl ) );
				label.setTitle( validUrl );
				//Add the URL click handler
				label.addClickHandler( new ClickHandler(){
					public void onClick( ClickEvent e ) {
						//Ensure lazy loading
						( new SplitLoad( true ) {
							@Override
							public void execute() {
								final ActionGridDialog dialog;
								if( ( utubeVideoUrl != null ) && handleYoutubeVideo ) {
									//This is a youtube video url but we want to handle 
									//it but we did not want to embed it. Open this youtube
									//video in a dialog then.
									dialog = new ViewYoutubeVideoDialogUI( utubeVideoUrl, validUrl );
								} else {
									//Open the URL following warning dialog
									dialog = new AskForURLFollowingDialogUI( validUrl );
								}
								dialog.show();
								dialog.center();
							}
						}).loadAndExecute();
						
						//Stop the event from being propagated
						e.stopPropagation();
						e.preventDefault();
					}
				});
				
				//Make the link to be the label
				urlWidget = label;
			}
		}
		
		//Initialize the widget
		initWidget( urlWidget );
	}
	
	/**
	 * Tries to detect if this URL is out site url, if so then it rewrites it to the GWT version URL
	 * @param url the original url
	 * @return null if this URL does not seem to be ours, or otherwise the GWT version URL
	 */
	private String isOurSiteURLThenRewrite( final String url ) {
		String ourSiteUrl = null;
		final String moduleBase = GWT.getModuleBaseURL();
		
		if( url.startsWith( moduleBase ) && ! ServerSideAccessManager.isOurFileServletURL( url, moduleBase ) ) {
			//First remove any locale thing specific for the GWT url
			ourSiteUrl = url.replaceFirst( "\\" + ServerSideAccessManager.URL_QUERY_DELIMITER +
										   CommonResourcesContainer.LOCALE_PARAMETER_NAME + "=[a-zA-Z]*\\" +
										   ServerSideAccessManager.URI_HISTORY_TOKEN_SYMBOL,
										   ServerSideAccessManager.URI_HISTORY_TOKEN_SYMBOL );
			if( ! ourSiteUrl.startsWith( moduleBase + ServerSideAccessManager.URI_HISTORY_TOKEN_SYMBOL ) ) {
				//In case this does not seem to be a GWT URL then we try to remove 
				//the locale related stuff from the beginning of a JSP-like URL
				ourSiteUrl = ourSiteUrl.replaceFirst( "\\" + ServerSideAccessManager.URL_QUERY_DELIMITER +
													  CommonResourcesContainer.LOCALE_PARAMETER_NAME + "=[a-zA-Z]*\\" +
													  ServerSideAccessManager.SERVLET_PARAMETERS_DELIMITER,
													  ServerSideAccessManager.URL_QUERY_DELIMITER );
				ourSiteUrl = ourSiteUrl.replaceFirst( "\\" + ServerSideAccessManager.URL_QUERY_DELIMITER +
													  CommonResourcesContainer.LOCALE_PARAMETER_NAME + "=[a-zA-Z]*$", "" );
				//Now check if this URL is indeed can be interpreted as a JSP URL
				if( ourSiteUrl.contains( ServerSideAccessManager.URL_QUERY_DELIMITER ) ) {
					//This URL seems to be ours but a JSP version url
					ourSiteUrl = ourSiteUrl.replace( moduleBase, moduleBase + ServerSideAccessManager.URI_HISTORY_TOKEN_SYMBOL );
					ourSiteUrl = ourSiteUrl.replace( ServerSideAccessManager.URL_QUERY_DELIMITER,
													 ServerSideAccessManager.SERVER_CONTEXT_DELIMITER );
				}
			}
		}
		
		return ourSiteUrl;
	}
}
