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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.xcurechat.client.rpc.ServerSideAccessManager;

import com.xcurechat.client.utils.widgets.FlashObjectWrapperUI;

/**
 * @author zapreevis
 * This class extends the embedded object class with the embedded flash player
 * JW Player for Flash Version 5.1 that allows to play FLV, MP4, ACC, MP3 and others
 * URL: http://developer.longtailvideo.com/trac/wiki/
 */
public class FlashObjectWithJWPlayer extends FlashEmbeddedObject {
	
	//The url of the embedded flash player
	public static final String LOCAL_FLV_PLAYER_URL = ServerSideAccessManager.SITE_MEDIA_SUPPORT_LOCATION + "player-viral.swf";
	
	public static final String FLASH_VAR_ATTRIBUTE_DELIM = "&";
	public static final String FLASH_VAR_ATTRIBUTE_NAME_VAL_DELIM = "=";
	public static final String FILE_FLASH_VAR_ATTRIBUTE = "file";
	public static final String PLUGINS_FLASH_VAR_ATTRIBUTE = "plugins";
	public static final String PROVIDER_FLASH_VAR_ATTRIBUTE = "provider";
	public static final String DURATION_FLASH_VAR_ATTRIBUTE = "duration";
	public static final String DURATION_FLASH_VAR_ATT_MAX_VALUE = "3600";	/*This is a 60 minutes length, should be enough for a 10mb file*/
	public static final String PROVIDER_FLASH_VAR_ATT_VIDEO_VALUE = "video";
	public static final String PROVIDER_FLASH_VAR_ATT_SOUND_VALUE = "sound";
	public static final String HEIGHT_FLASH_VAR_ATT_PLAYER_SIZE = "height";
	public static final String SKIN_FLASH_VAR_ATTRIBUTE = "skin";
	public static final String SEAWAVE_SKIN_FLASH_VAR_VALUE = "unlimblue.zip";
	public static final String HEIGHT_FLASH_VAR_ATT_PLAYER_SIZE_MIN_VAL = "33";
	
	public static final String VIRAL_PLUGIN_NAME = "viral-2";
	public static final String VIRAL_ONPAUSE_FLASH_VAR_ATTRIBUTE = "viral.onpause";
	public static final String VIRAL_ONCOMPLETE_FLASH_VAR_ATTRIBUTE = "viral.oncomplete";
	public static final String VIRAL_VIRAL_LINK_FLASH_VAR_ATTRIBUTE = "viral.link";
	
	//Contains extra encodings for the URL delimiters, this is required for the FLV player, the file URL should be fully encoded
	private static Map<String,String> urlDelimitersEncodingTable = new HashMap<String, String>();
	
	//The link to the forum message post or null
	private final String theForumMessageLinkURL;
	
	static {
		urlDelimitersEncodingTable.put("\\?", "%3F");
		urlDelimitersEncodingTable.put("\\=", "%3D");
		urlDelimitersEncodingTable.put("\\&", "%26");
	}
	
	/**
	 * The value returned by this method is used for enforcing the minimum player height.
	 * Can be overridden to change the minimum height.
	 * @return by default returns HEIGHT_FLASH_VAR_ATT_PLAYER_SIZE_MIN_VAL
	 */
	protected String getMinimumPlayerHeight() {
		return HEIGHT_FLASH_VAR_ATT_PLAYER_SIZE_MIN_VAL;
	}
	
	/**
	 * Provides extra encoding for the URL, i.e. allows to encode the special delimiter symbols: ?, =, &
	 * @param url the urls to encode
	 * @return the resulting url
	 */
	public static final String doURLDelimitersEncoding( final String url ) {
		String resultingURL = url;
		if( resultingURL != null ) {
			Set<Entry<String,String>> entries = urlDelimitersEncodingTable.entrySet();
			for( Entry<String,String> entry : entries) {
				resultingURL = resultingURL.replaceAll( entry.getKey(), entry.getValue() );
			}
		} else {
			resultingURL = "";
		}
		return resultingURL;
	}
	
	//The URL of the media stream/file that we want to play
	private String mediaFileURL = "";
	
	//The base site url to be used, note that it can be set to null then the object uses site-context-relative urls.
	private final String baseSiteURL;
	
	/**
	 * @param siteDomainPattern
	 * @param theForumMessageLinkURL the link to the forum message post or null
	 */
	public FlashObjectWithJWPlayer(final String siteDomainPattern, final String theForumMessageLinkURL, final String baseSiteURL) {
		super(siteDomainPattern);
		
		this.theForumMessageLinkURL = theForumMessageLinkURL;
		this.baseSiteURL = ( ( ( baseSiteURL != null ) && !baseSiteURL.trim().isEmpty() ) ? baseSiteURL : "" ) + ServerSideAccessManager.SERVER_CONTEXT_DELIMITER;
	}
	
	/**
	 * Allows to add a new parameter to the flash vars string
	 * @param name the name of the parameter
	 * @param value the value of the parameter
	 * @param isURL if true then the value gets an extra encoding for the url delimiters
	 */
	public void addFlashVarAttribute( final String name, final String value, final boolean isURL ) {
		String currentValue = commonParamsObjAttrsEmb.get( FLASH_VARS_OBJECT_PARAM_COMMON );
		if( currentValue == null ) {
			currentValue = "";
		} else {
			if( ! currentValue.trim().isEmpty() ) {
				currentValue += FLASH_VAR_ATTRIBUTE_DELIM;
			}
		}
		currentValue += name + FLASH_VAR_ATTRIBUTE_NAME_VAL_DELIM + (isURL ? doURLDelimitersEncoding( value ) : value);
		commonParamsObjAttrsEmb.put( FLASH_VARS_OBJECT_PARAM_COMMON, currentValue );
	}
	
	/**
	 * Allows to add a new "file" parameter to the flash vars string
	 * @param value the value of the file parameter
	 */
	public void addFileFlashVarAttribute( final String value ) {
		addFlashVarAttribute( FILE_FLASH_VAR_ATTRIBUTE, value, true );
	}
	
	/**
	 * Allows to add a new "provider" parameter to the flash vars string
	 * @param value the value of the provider parameter
	 */
	public void addProviderFlashVarAttribute( final String value ) {
		addFlashVarAttribute( PROVIDER_FLASH_VAR_ATTRIBUTE, value, false );
	}
	
	/**
	 * Forces the maximum duration of the played video/audio
	 * NOTE: Add the duration attribute to avoid the premature play back
	 * termination, this happens in case of a bad browser support:
	 * http://www.longtailvideo.com/support/forum/General-Chat/18664/Icecast-stream-plays-only-first-few-seconds#msg125815
	 */
	public void forceMaximumDuration() {
		addFlashVarAttribute( DURATION_FLASH_VAR_ATTRIBUTE, DURATION_FLASH_VAR_ATT_MAX_VALUE, false );
	}
	
	/**
	 * Allows to force the minimum player height, is useful for when playing mp3 files
	 */
	public void forceMinimumPlayerHeight() {
		addFlashVarAttribute( HEIGHT_FLASH_VAR_ATT_PLAYER_SIZE, getMinimumPlayerHeight(), false );
		this.setHeight( getMinimumPlayerHeight() );
	}
	
	/**
	 * Allows to get a skin plugin for the player, can be overriden for another plugin
	 * @return the default value is SEAWAVE_SKIN_FLASH_VAR_VALUE 
	 */
	protected String getSkinPluginFileName() {
		return SEAWAVE_SKIN_FLASH_VAR_VALUE;
	}
	
	/**
	 * Currently properly handles only supports SWF,FLV,MP4,MP3 files!
	 * @param value the url o the file to play
	 * @param fileMimeType the mime-type of the file
	 */
	public void setMediaUrl( final String value, final String fileMimeType ) {
		mediaFileURL = value;
		if( SupportedFileMimeTypes.isSWFMimeType( fileMimeType ) ) {
			//This object knows how to play swf
			setMovieUrl( value );
		} else {
			if( SupportedFileMimeTypes.isFLVMimeType( fileMimeType ) ||
				SupportedFileMimeTypes.isMP4MimeType( fileMimeType ) ||
				SupportedFileMimeTypes.isMP3MimeType( fileMimeType ) ) {
				//To play flv, mp4 we need to use a player and then to provide it with the url of the file to play
				setMovieUrl( baseSiteURL + LOCAL_FLV_PLAYER_URL ); //Set the player
				addFileFlashVarAttribute( value ); //Set the file to play
				
				//Add the skin
				addFlashVarAttribute( SKIN_FLASH_VAR_ATTRIBUTE, baseSiteURL +
									  ServerSideAccessManager.SITE_MEDIA_SUPPORT_LOCATION +
									  getSkinPluginFileName(), false);
				
				//Distinguish between the mp3 files and others
				if( SupportedFileMimeTypes.isMP3MimeType( fileMimeType ) ) {
					//Indicate that this is a sound file
					addProviderFlashVarAttribute( PROVIDER_FLASH_VAR_ATT_SOUND_VALUE );
					
					//NOTE: Add the duration attribute to avoid the premature play back
					//termination, this happens in case of a bad browser support.
					//Seems to work fine without it since we switched to JW version 5.1
					//forceMaximumDuration();
					
					//Add the viral flash-var attibutes, do not share mps files
					addFlashVarAttribute( VIRAL_ONPAUSE_FLASH_VAR_ATTRIBUTE, "false", false);
					addFlashVarAttribute( VIRAL_ONCOMPLETE_FLASH_VAR_ATTRIBUTE, "false", false);
					
					//For the mp3 files set the screen height to minimum
					forceMinimumPlayerHeight();
 				} else {
 					//Indicate that this is a video file
					addProviderFlashVarAttribute( PROVIDER_FLASH_VAR_ATT_VIDEO_VALUE );
					
					//We do not share MP3 files, only the videos, and only from the forum
					//NOTE: The sharing plugin will only work for the approved forum messages
					if( theForumMessageLinkURL != null ) {
						//Add the VARAL files sharing plugin
						addFlashVarAttribute( PLUGINS_FLASH_VAR_ATTRIBUTE, VIRAL_PLUGIN_NAME, false);
						
						//Add the viral flash-var attibutes
						addFlashVarAttribute( VIRAL_ONPAUSE_FLASH_VAR_ATTRIBUTE, "true", false);
						addFlashVarAttribute( VIRAL_ONCOMPLETE_FLASH_VAR_ATTRIBUTE, "true", false);
						addFlashVarAttribute( VIRAL_VIRAL_LINK_FLASH_VAR_ATTRIBUTE, theForumMessageLinkURL , true);
					}
				}
			} else {
				//WARNING: It is some other type of file, but we just set the url, since we do not know what to do!
				setMovieUrl( value );
			}
		}	
	}
	
	/**
	 * Allows to get the widget representation of the Flash object, in case the user is logged in,
	 * we also provide a link for downloading the file played by the player
	 * NOTE: This method appends the file URL with the extra servlet parameter indicating that we want to download the given file.
	 * @param isWithDownloadLink if true then we add a file download link to the widget
	 * @param isInitiallyBlocked if true then the flash will not be loaded until the user clicks on the widget
	 */
	public FlashObjectWrapperUI getEmbeddedObjectWidget( final boolean isWithDownloadLink, final boolean isInitiallyBlocked ) {
		final FlashObjectWrapperUI flashWrapperWidget = new FlashObjectWrapperUI( super.toString(), isInitiallyBlocked, ( isWithDownloadLink ? mediaFileURL : null ) );
		final String width  = commonAttributes.get( WIDTH_OBJECT_ATT_COMMON )  + "px";
		final String height = commonAttributes.get( HEIGHT_OBJECT_ATT_COMMON ) + "px";
		flashWrapperWidget.setSize(width, height);
		
		return flashWrapperWidget;
	}
}
