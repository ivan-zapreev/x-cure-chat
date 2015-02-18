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

import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Iterator;

import com.xcurechat.client.rpc.exceptions.MessageException;

/**
 * @author zapreevis
 * This class represents the embedded flash object, contains the appropriate data and can be serialized into HTML.
 */
public class FlashEmbeddedObject {
	/*The following are the common parameters of the OBJECT tag and the attributes of the EMBED tag*/
	public static final String ALLOW_SCRIPT_ACCESS_OBJECT_PARAM_COMMON = "allowScriptAccess".toLowerCase();
	public static final String ALLOW_SCRIPT_ACCESS_EMBED_ATT_COMMON = ALLOW_SCRIPT_ACCESS_OBJECT_PARAM_COMMON;
	
	//The SWF file can communicate with the HTML page in which it is embedded only when the SWF file
	//is from the same domain as the HTML page. This is the default value for AllowScriptAccess. Use
	//this setting, or do not set a value for AllowScriptAccess, to prevent a SWF file hosted from one
	//domain from accessing a script in an HTML page that comes from another domain.
	//Should be used for the external (linked) flash movies
	public static final String ALLOW_SCRIPT_ACCESS_SAME_DOMAIN_VALUE = "sameDomain".toLowerCase();
	//The SWF file cannot communicate with any HTML page. Using this value is deprecated and not
	//recommended, and shouldn’t be necessary if you don’t serve untrusted SWF files from your own
	//domain. If you do need to serve untrusted SWF files, Adobe recommends that you create a
	//distinct subdomain and place all untrusted content there.
	//Should be used for the flash uploaded to the this web site as the content is not checked
	public static final String ALLOW_SCRIPT_ACCESS_NEVER_VALUE = "never".toLowerCase();
	//The SWF file can communicate with the HTML page in which it is embedded even when the SWF
	//file is from a different domain than the HTML page.
	public static final String ALLOW_SCRIPT_ACCESS_ALWAYS_VALUE = "always".toLowerCase(); 
	
	public static final String ALLOW_NETWORKING_OBJECT_PARAM_COMMON = "AllowNetworking".toLowerCase();
	//The SWF file may not call any networking APIs, listed below. Also, it cannot use any
	//SWF-to-SWF communication APIs, also included in the list below.
	public static final String ALLOW_NETWORKING_NONE_VALUE = "none".toLowerCase();
	//All networking APIs are permitted in the SWF.
	public static final String ALLOW_NETWORKING_ALL_VALUE = "all".toLowerCase();
	//The SWF file may not call browser navigation or browser interaction APIs,
	//but it may call any other networking APIs.
	public static final String ALLOW_NETWORKING_INTERNAL_VALUE = "internal".toLowerCase();
	
	public static final String ALLOWS_FULL_SCREEN_OBJECT_PARAM_COMMON = "allowFullScreen".toLowerCase();
	public static final String ALLOWS_FULL_SCREEN_EMBED_ATT_COMMON = ALLOWS_FULL_SCREEN_OBJECT_PARAM_COMMON;
	public static final String ALLOWS_FULL_SCREEN_TRUE_VALUE = "true".toLowerCase();
	public static final String ALLOWS_FULL_SCREEN_FALSE_VALUE = "false".toLowerCase();
	public static final String ALLOWS_FULL_SCREEN_DEFAULT_VALUE = ALLOWS_FULL_SCREEN_TRUE_VALUE;
	
	public static final String BACKGROUND_COLOR_OBJECT_PARAM_COMMON = "bgcolor".toLowerCase();
	public static final String BACKGROUND_COLOR_EMBED_ATT_COMMON = BACKGROUND_COLOR_OBJECT_PARAM_COMMON;
	public static final String BACKGROUND_COLOR_DFAULT_VALUE = "#000000".toLowerCase();
	
	public static final String QUALITY_OBJECT_PARAM_COMMON = "quality".toLowerCase();
	public static final String QUALITY_EMBED_ATT_COMMON = QUALITY_OBJECT_PARAM_COMMON;
	public static final String QUALITY_DEFAULT_VALUE = "best".toLowerCase();
	
	public static final String FLASH_VARS_OBJECT_PARAM_COMMON = "FlashVars".toLowerCase();
	public static final String FLASH_VARS_EMBED_ATT_COMMON = FLASH_VARS_OBJECT_PARAM_COMMON;
	
	public static final String WMODE_OBJECT_PARAM_COMMON = "WMode".toLowerCase();
	public static final String WMODE_EMBED_ATT_COMMON = WMODE_OBJECT_PARAM_COMMON;
	public static final String WMODE_DEFAULT_VALUE = "transparent".toLowerCase();
	
	/*The following are the common attributes of the OBJECT tag and the EMBED tag*/
	public static final String WIDTH_OBJECT_ATT_COMMON = "width".toLowerCase();
	public static final String WIDTH_EMBED_ATT_COMMON = WIDTH_OBJECT_ATT_COMMON;
	public static final int MAX_ALLOWED_WIDTH = 500;
	public static final int MIN_ALLOWED_WIDTH = 5;

	public static final String HEIGHT_OBJECT_ATT_COMMON = "height".toLowerCase();
	public static final String HEIGHT_EMBED_ATT_COMMON = HEIGHT_OBJECT_ATT_COMMON;
	public static final int MAX_ALLOWED_HEIGHT = 375;
	public static final int MIN_ALLOWED_HEIGHT = 5;
	
	public static final String ELEMENT_TYPE_OBJECT_ATT_COMMON = "type".toLowerCase();
	public static final String ELEMENT_TYPE_EMBED_ATT_COMMON = ELEMENT_TYPE_OBJECT_ATT_COMMON;
	public static final String ELEMENT_TYPE_DEFAULT_VALUE = "application/x-shockwave-flash".toLowerCase();
	
	/*The following are the unique parameters of the OBJECT tag*/
	public static final String MOVIE_URL_OBJECT_PARAM_UNIQUE = "movie".toLowerCase();
	
	/*The following are the unique attributes of the OBJECT tag*/
	public static final String CODEBASE_OBJECT_ATT_UNIQUE = "codebase".toLowerCase();
	public static final String CODEBASE_DEFAULT_VALUE_BASE = "http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=".toLowerCase();
	public static final String CODEBASE_DEFAULT_VALUE = (CODEBASE_DEFAULT_VALUE_BASE + "9,0,0,0").toLowerCase();
	
	public static final String CLASSID_OBJECT_ATT_UNIQUE = "classid".toLowerCase();
	public static final String CLASSID_DEFAULT_VALUE = "clsid:D27CDB6E-AE6D-11cf-96B8-444553540000".toLowerCase();
	
	/*The following are the unique parameters of the EMBED tag*/
	
	/*The following are the unique attributes of the EMBED tag*/
	public static final String PLUGINSPACE_EMBED_ATT_UNIQUE = "pluginspage";
	public static final String PLUGINSPACE_EMBED_ATT_DEFAULT_VALUE = "http://www.macromedia.com/go/getflashplayer";
	
	public static final String MOVIE_URL_EMBED_ATT_UNIQUE = "src".toLowerCase();
	
	public static final String OBJECT_TAG_NAME = "OBJECT".toLowerCase();
	public static final String OBJECT_OPEN_TAG = ("<"+OBJECT_TAG_NAME).toLowerCase();
	public static final String OBJECT_CLOSE_TAG = ("</"+OBJECT_TAG_NAME+">").toLowerCase();
	public static final String EMBED_TAG_NAME = "EMBED"; 
	public static final String EMBED_OPEN_TAG = ("<"+EMBED_TAG_NAME).toLowerCase();
	public static final String EMBED_CLOSE_TAG_ONE = ("</"+EMBED_TAG_NAME+">").toLowerCase();
	public static final String EMBED_CLOSE_TAG_TWO = ("/>").toLowerCase();
	public static final String PARAM_TAG_NAME = "PARAM".toLowerCase();
	public static final String PARAM_NAME_ATTR = "NAME";
	public static final String PARAM_VALUE_ATT = "VALUE";
	
	//Stores the pattern of the XCure-chat domain, or null if it is not set
	public final String xcureDomainPattern;
	
	//Stores the common things: parameters of the OBJECT and attributes of the EMBED
	protected final Map<String,String> commonParamsObjAttrsEmb = new HashMap<String,String>();
	//Stores the common attibutes of the OBJECT and attributes of the EMBED
	protected final Map<String,String> commonAttributes = new HashMap<String,String>();
	//Stores the unique attributes of the OBJECT
	protected final Map<String,String> unqiueAttributesObject = new HashMap<String,String>();
	//Stores the unique parameters of the OBJECT
	protected final Map<String,String> uniqueParametersObject = new HashMap<String,String>();
	//Stores the unique attributes of the EMBED
	protected final Map<String,String> uniqueAttributesEmbed = new HashMap<String,String>();
	
	/**
	 * The basic constructor of the Flash object, here we DO NOT ALLOW for allowScriptAccess = always, for security reasons.
	 * @param xcureDomainPattern stores the pattern of the XCure-chat domain, or null if it is not set
	 * 							  if the embedded object URL matches the XCure-Chat domain pattern then
	 * 							  the allowScriptAccess is set to NEVER, the same happens if this param
	 * 							  is null, otherwise the allowScriptAccess is set to SAMEDOMAIN
	 */
	public FlashEmbeddedObject( final String xcureDomainPattern ) {
		this.xcureDomainPattern = xcureDomainPattern;
	}
	
	/**
	 * Allows to serialize the completed embedded flash object into an html string
	 */
	@Override
	public String toString() {
		return OBJECT_OPEN_TAG + " " + getAttributes( commonAttributes ) + getAttributes( unqiueAttributesObject ) + ">" +
			   getParameters( commonParamsObjAttrsEmb ) + getParameters( uniqueParametersObject ) +
			   EMBED_OPEN_TAG + " " + getAttributes( commonParamsObjAttrsEmb ) + getAttributes( uniqueAttributesEmbed ) +
			   getAttributes( commonAttributes ) + ">" + EMBED_CLOSE_TAG_ONE + OBJECT_CLOSE_TAG;
	}
	
	/**
	 * Allows to validate the constructed object and detect if it is a flash animation instance or not.
	 * This method should be called before the completeEmbedFlash method is called.
	 * @return true if the object is detected to be a flash animation, otherwise false
	 */
	public boolean isValidEmbedFlash() {
		//Check if the type, classid, codebase or pluginspace or flashvars is set and the flash movie url is set
		return ( isEqualToString( commonAttributes.get( ELEMENT_TYPE_OBJECT_ATT_COMMON ), ELEMENT_TYPE_DEFAULT_VALUE ) ||
			   isEqualToString( unqiueAttributesObject.get( CLASSID_OBJECT_ATT_UNIQUE ), CLASSID_DEFAULT_VALUE ) ||
			   (unqiueAttributesObject.get( CODEBASE_OBJECT_ATT_UNIQUE ) != null ? unqiueAttributesObject.get( CODEBASE_OBJECT_ATT_UNIQUE ).startsWith(CODEBASE_DEFAULT_VALUE_BASE) : false ) || 
			   isEqualToString( uniqueAttributesEmbed.get( PLUGINSPACE_EMBED_ATT_UNIQUE ), PLUGINSPACE_EMBED_ATT_DEFAULT_VALUE ) ||
			   ( commonParamsObjAttrsEmb.get( FLASH_VARS_OBJECT_PARAM_COMMON ) != null ) ) &&
			   ( uniqueParametersObject.get( MOVIE_URL_OBJECT_PARAM_UNIQUE ) != null &&
				 !uniqueParametersObject.get( MOVIE_URL_OBJECT_PARAM_UNIQUE ).trim().equals("") ) ;
	}
	
	public void setElementType( final String value ) {
		commonAttributes.put( ELEMENT_TYPE_OBJECT_ATT_COMMON, value );
	}
	
	public void setClassID( final String value ) {
		unqiueAttributesObject.put( CLASSID_OBJECT_ATT_UNIQUE, value );
	}
	
	public void setCodeBase( final String value ) {
		unqiueAttributesObject.put( CODEBASE_OBJECT_ATT_UNIQUE, value );
	}
	
	public void setPluginSpace( final String value ) {
		uniqueAttributesEmbed.put( PLUGINSPACE_EMBED_ATT_UNIQUE, value );
	}
	
	public void setAllowScriptAccess( final String value ) {
		commonParamsObjAttrsEmb.put( ALLOW_SCRIPT_ACCESS_OBJECT_PARAM_COMMON, value );
	}
	
	public void setAllowNetworking( final String value ) {
		commonParamsObjAttrsEmb.put( ALLOW_NETWORKING_OBJECT_PARAM_COMMON, value );
	}
	
	public void setAllowFullScreen( final String value ) {
		commonParamsObjAttrsEmb.put( ALLOWS_FULL_SCREEN_OBJECT_PARAM_COMMON, value );
	}
	
	public void setBGColor( final String value ) {
		commonParamsObjAttrsEmb.put( BACKGROUND_COLOR_OBJECT_PARAM_COMMON, value );
	}
	
	public void setQuality( final String value ) {
		commonParamsObjAttrsEmb.put( QUALITY_OBJECT_PARAM_COMMON, value );
	}
	
	public void setFlashVars( final String value ) {
		commonParamsObjAttrsEmb.put( FLASH_VARS_OBJECT_PARAM_COMMON, value );
	}
	
	public void setWMode( final String value ) {
		commonParamsObjAttrsEmb.put( WMODE_OBJECT_PARAM_COMMON, value );
	}
	
	public void setMovieUrl( final String value ) {
		uniqueParametersObject.put( MOVIE_URL_OBJECT_PARAM_UNIQUE, value );
		uniqueAttributesEmbed.put( MOVIE_URL_EMBED_ATT_UNIQUE, value );
	}
	
	public void setWidth( final String value ) {
		commonAttributes.put( WIDTH_OBJECT_ATT_COMMON, value );
	}
	
	public void setHeight( final String value ) {
		commonAttributes.put( HEIGHT_OBJECT_ATT_COMMON, value );
	}
	
	/**
	 * Allows to set a recognized flash embedded object parameter/attribute value
	 * This works for OBJECT and EMBED tags
	 * @param name the name of the parameter/attribute
	 * @param value the value of the parameter/attribute
	 * @return true if the parameter was recognized and set, otherwise false
	 */
	public boolean setNameValue( final String name, final String value ) {
		boolean isFound = true;
		if( name != null && value != null ) {
			final String nameLowerCase = name.trim().toLowerCase();
			if( nameLowerCase.equals( ALLOW_SCRIPT_ACCESS_OBJECT_PARAM_COMMON ) ) {
				setAllowScriptAccess( value );
			} else {
				if( nameLowerCase.equals( ALLOWS_FULL_SCREEN_OBJECT_PARAM_COMMON ) ) {
					setAllowFullScreen( value );
				} else {
					if( nameLowerCase.equals( BACKGROUND_COLOR_OBJECT_PARAM_COMMON ) ) {
						setBGColor( value );
					} else {
						if( nameLowerCase.equals( QUALITY_OBJECT_PARAM_COMMON ) ) {
							setQuality( value );
						} else {
							if( nameLowerCase.equals( FLASH_VARS_OBJECT_PARAM_COMMON ) ) {
								setFlashVars( value );
							} else {
								if( nameLowerCase.equals( WIDTH_OBJECT_ATT_COMMON ) ) {
									setWidth( value );
								} else {
									if( nameLowerCase.equals( HEIGHT_OBJECT_ATT_COMMON ) ) {
										setHeight( value );
									} else {
										if( nameLowerCase.equals( ELEMENT_TYPE_OBJECT_ATT_COMMON ) ) {
											setElementType( value );
										} else {
											if( nameLowerCase.equals( MOVIE_URL_OBJECT_PARAM_UNIQUE ) ) {
												setMovieUrl( value );
											} else {
												if( nameLowerCase.equals( CODEBASE_OBJECT_ATT_UNIQUE ) ) {
													setCodeBase( value );
												} else {
													if( nameLowerCase.equals( CLASSID_OBJECT_ATT_UNIQUE ) ) {
														setClassID( value );
													} else {
														if( nameLowerCase.equals( PLUGINSPACE_EMBED_ATT_UNIQUE ) ) {
															setPluginSpace( value );
														} else {
															if( nameLowerCase.equals( MOVIE_URL_EMBED_ATT_UNIQUE ) ) {
																setMovieUrl( value );
															} else {
																if( nameLowerCase.equals( WMODE_OBJECT_PARAM_COMMON ) ) {
																	setWMode( value );
																} else {
																	if( nameLowerCase.equals( ALLOW_NETWORKING_OBJECT_PARAM_COMMON ) ) {
																		setAllowNetworking( value );
																	} else {
																		isFound = false;
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		} else {
			isFound = false;
		}
		return isFound;
	}
	
	/**
	 * Allows to complete the embedded flash object by setting all the required
	 * (proper and secure) values of the parameters and attributes. Ensures that
	 * the embedded object has the tightest sripting and network rights, possible. 
	 */
	public void completeEmbedFlash() {
		completeEmbedFlash( false );
	}
	/**
	 * Allows to complete the embedded flash object by setting all the required
	 * (proper and secure) values of the parameters and attributes.
	 * @param allowAllAccess if true then the flash object is allowed to have
	 * all script and network rights, otherwise they are set to be the tightest
	 */
	public void completeEmbedFlash( final boolean allowAllAccess ) {
		//Set the element type
		setElementType( ELEMENT_TYPE_DEFAULT_VALUE  );
		
		//Set the classid
		setClassID( CLASSID_DEFAULT_VALUE );
		
		//Set the codebase
		setCodeBase( CODEBASE_DEFAULT_VALUE );
		
		//Set the plugin space
		setPluginSpace( PLUGINSPACE_EMBED_ATT_DEFAULT_VALUE );
		
		//Set the script access and networking
		if( allowAllAccess ) {
			//WARNING: Is too loose because we upload untrusted content!!!
			setAllowNetworking( ALLOW_NETWORKING_ALL_VALUE );
			setAllowScriptAccess( ALLOW_SCRIPT_ACCESS_ALWAYS_VALUE );
		} else {
			setAllowNetworking( ALLOW_NETWORKING_INTERNAL_VALUE );
			if( xcureDomainPattern == null ) {
				setAllowScriptAccess( ALLOW_SCRIPT_ACCESS_NEVER_VALUE );
			} else {
				String embeddedObjectURL = 	uniqueParametersObject.get( MOVIE_URL_OBJECT_PARAM_UNIQUE );
				if( ( embeddedObjectURL == null ) || embeddedObjectURL.matches( xcureDomainPattern ) ) {
					setAllowScriptAccess( ALLOW_SCRIPT_ACCESS_NEVER_VALUE );
				} else {
					setAllowScriptAccess( ALLOW_SCRIPT_ACCESS_SAME_DOMAIN_VALUE );
				}
			}
		}
		
		//Set the full screen permissions
		setAllowFullScreen( ALLOWS_FULL_SCREEN_DEFAULT_VALUE );
		
		//Set the background color
		setBGColor( BACKGROUND_COLOR_DFAULT_VALUE );
		
		//Set the quality parameter
		setQuality( QUALITY_DEFAULT_VALUE );
		
		//Set the WMode in order to prevent embedded flash to be shown above dialog diwnows and alike
		setWMode( WMODE_DEFAULT_VALUE );
		
		//Set the proper width and height
		scaleAndSetNewFlashSizeIfNeeded( commonAttributes.get( HEIGHT_OBJECT_ATT_COMMON ), commonAttributes.get( WIDTH_OBJECT_ATT_COMMON ), MAX_ALLOWED_HEIGHT, MAX_ALLOWED_WIDTH );
	}
	
	/**
	 * Takes the string size values (width, height) of the Embedded object and checks
	 * if they exceed the maximum allowed ones, if yes then it rescales the object
	 * following the proprtions of the original object. In case the current values of
	 * height and width can not be parsed the width and height are set to be half of
	 * the maximum allowed values. If the height and the width are withing the proper
	 * bounds then no rescaling.
	 * @param height the current height of the embedded flash object as a string
	 * @param width the current width of the embedded flash object as a string
	 * @param max_height the maximum allowed height of the embedded object
	 * @param max_width the maximum allowed width of the embedded object
	 */
	private void scaleAndSetNewFlashSizeIfNeeded( final String height, final String width,
										 	  final int max_height, final int max_width ) {
		//Take the initial size to be zero and try to parse the currently set values
		int curr_width = 0;
		int curr_height = 0;
		try{
			curr_width = Integer.parseInt( width );
		} catch ( Exception e ) {}
		try{
			curr_height = Integer.parseInt( height );
		} catch ( Exception e ) {}
		
		//Check if the currently set values are larger than the maximum, then scale
		if( ( curr_width > max_width ) || ( curr_height > max_height ) ) {
			final double w_h_ratio = (double) curr_width / (double) curr_height;  
			final double h_w_ratio = (double) curr_height / (double) curr_width;  
			if( curr_width > max_width ) {
				curr_width = max_width;
				curr_height = (int) ( h_w_ratio * curr_width ); 
			}
			if( curr_height > max_height ) {
				curr_height = max_height;
				curr_width = (int) ( w_h_ratio * curr_height );
			}
		}
		
		//In case the resulting width or the height are less than minimal. Then
		//we just set them to 3/4 of default, because there is nothing else we can do
		if( curr_width < MIN_ALLOWED_WIDTH ) {
			setWidth( "" + (int) ( max_width * 3.0/4.0) );
		} else {
			setWidth( "" + curr_width );
		}
		if( curr_height < MIN_ALLOWED_HEIGHT ) {
			setHeight( "" + (int) ( max_height * 3.0/4.0) );
		} else {
			setHeight( "" + curr_height );
		}
	}
	
	private boolean isEqualToString( final String value, final String expectedValue ) {
		return (value != null) && value.trim().toLowerCase().equals( expectedValue );
	}
	
	private String getAttributes( Map<String,String> mapping ) {
		String result = "";
		if( mapping != null ) {
			Iterator<Entry<String,String>> iter = mapping.entrySet().iterator();
			while( iter.hasNext() ) {
				Entry<String, String> entry = iter.next();
				result += entry.getKey() + "=\"" + entry.getValue() + "\" ";
			}
		}
		return result;
	}
	
	private String getParameters( Map<String,String> mapping ) {
		String result = "";
		if( mapping != null ) {
			Iterator<Entry<String,String>> iter = mapping.entrySet().iterator();
			while( iter.hasNext() ) {
				Entry<String, String> entry = iter.next();
				result += "<"+PARAM_TAG_NAME+" "+PARAM_NAME_ATTR+"=\"" + entry.getKey() +"\" "+PARAM_VALUE_ATT+"=\"" + entry.getValue() + "\"></"+PARAM_TAG_NAME+"> ";
			}
		}
		return result;
	}
	
	/**
	 * @author zapreevis
	 * Provides a helper interface when parsing the provided text and splitting
	 * it into chanks of regular text and the HTML code of the embedded OBJECTs
	 */
	public interface ObjectTagSearchHelper<T> {
		/**
		 * This method is triggered when going through the the text string
		 * we find the substring that is not a part of the OBJECT tag
		 * @param substring the found substring that is not a part of the OBJECT tag
		 * @throws MessageException in case the substring is somewhat malformed
		 */
		public void processObjectFreeSubstring( final String substring ) throws MessageException;
		/**
		 * This method is triggered when going through the the text string
		 * we find the substring that is an OBJECT tag, from its beginning till end.
		 * @param substring the found substring that is an OBJECT tag, from its beginning till end.
		 * 					Here, in this substring all the new line symbols are removed.
		 * @throws MessageException in case the substring is somewhat malformed
		 */
		public void processObjectTagSubstring( final String substring ) throws MessageException;
		
		/**
		 * Is called if the OBJECT's tag does not have the closing part, i.e. it is invalid
		 * This should not be happening, but if it does this method is called.
		 * @throws MessageException can throws this exception if needed
		 */
		public void onObjectCloseTagNotFound() throws MessageException;
		
		/**
		 * Allows to obtain a result of the search for the embedded OBJECT tag
		 * @return the result is of some predefined type T
		 */
		public T getResult();
	}

	/**
	 * Allows o parse the provided string search for the embedded OBJECT tags in it.
	 * Here we do not look for stand-alone EMBEDDED tags. The search is done in a
	 * linear manner from the beginning of the provided string till its end. If the
	 * Object tag does not have a closing tag then the <OBJECT part is removed and the
	 * rest of the provided text input is treated as a regular string.
	 * @param inputText the text to parse
	 * @param helper this object's methods are triggered in a SAX parser manner it allows
	 * to get the processed substrings.
	 * @throws MessageException in case the closing tag of the OBJECT is not found, might
	 * not be throw, then the <OBJECT tag is just skipped and the rest of the sring is
	 * treated as text. Is thrown only if the provided helper object throws this exception
	 */
	public static void processEmbeddedTags( final String inputText, final ObjectTagSearchHelper<?> helper ) throws MessageException {
		if( inputText != null ) {
			final String inputTextLowerCase = inputText.toLowerCase();
			
			int begin_index = 0, old_begin_index = 0;
			while( ( begin_index = inputTextLowerCase.indexOf( FlashEmbeddedObject.OBJECT_OPEN_TAG, old_begin_index ) ) != -1 ) {
				//Try to find the simple embedded text 
				helper.processObjectFreeSubstring( inputText.substring( old_begin_index, begin_index ) );
				
				//Search for the closing of the OBJECT tag
				int object_end_tag_index = inputTextLowerCase.indexOf( FlashEmbeddedObject.OBJECT_CLOSE_TAG, begin_index );
				if( object_end_tag_index != -1 ) {
					//We have found where the OBJECT ends
					object_end_tag_index = object_end_tag_index + FlashEmbeddedObject.OBJECT_CLOSE_TAG.length();
					
					//Get the object string and parse it, then validate the flash if it is valid include it into the text
					helper.processObjectTagSubstring( inputText.substring( begin_index, object_end_tag_index ).replace('\n', ' ') );
					
					//Update the old begin index
					old_begin_index = object_end_tag_index;
				} else {
					//We could not find the end of this object! Further parsing for the 
					//OBJECT tags is useless, skip on the false <OBJECT beginning tag
					helper.onObjectCloseTagNotFound();
					old_begin_index = begin_index + FlashEmbeddedObject.OBJECT_OPEN_TAG.length();
					break;
				}
			}
			
			//The remainder of the string does not have any OBJECT tags in it
			helper.processObjectFreeSubstring( inputText.substring(old_begin_index, inputText.length() ) );
		}
	}
}
