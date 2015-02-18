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
 * The search related package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.data.search;

import java.util.Map;
import java.util.Map.Entry;

import  com.xcurechat.client.data.ShortUserData;
import  com.xcurechat.client.data.ShortForumMessageData;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UIErrorMessages;

import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.utils.EncoderInt;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author zapreevis
 * Contains the search request data for the forum
 */
public class ForumSearchData implements IsSerializable {
	//The maximum length of the search string
	public static final int MAX_SEARCH_STRING_LENGTH = 90;
	
	//The maximum allowed number of forum messages per page for the search results
	public static final int MAX_NUMBER_OF_MESSAGES_PER_PAGE = 10;

	//The minimum page index
	public static final int MINIMUM_PAGE_INDEX = 1;
	
	//The default parameter values
	private static final String UNDEFINED_SEARCH_STRING_VAL = "";
	private static final String UNDEFINED_USER_LOGIN_VAL = "";
	private static final int UNDEFINED_USER_ID_VAL = ShortUserData.UNKNOWN_UID;
	private static final int FIRST_PAGE_INDEX_VAL = MINIMUM_PAGE_INDEX;
	public static final int UNKNOWN_BASE_MESSAGE_ID_VAL = ShortForumMessageData.UNKNOWN_MESSAGE_ID;
	private static final boolean NOT_ONLY_TOPICS_VAL = false;
	private static final boolean NOT_ONLY_IN_CURR_TOPIC_VAL = false;
	private static final boolean NOT_ONLY_ONE_MESSAGE_VAL = false;
	private static final boolean NOT_ONLY_APPROVED_MESSAGES_VAL = false;
	
	//The search string, if empty or null then search for all messages
	public String searchString = UNDEFINED_SEARCH_STRING_VAL;
	
	//If set then we search for posts by the given user
	public int byUserID = UNDEFINED_USER_ID_VAL;

	//The following is transient and is used to store the user's login name on the client
	public transient String byUserLoginName = UNDEFINED_USER_LOGIN_VAL;
	
	//The search page that we are going to look at
	public int pageIndex = FIRST_PAGE_INDEX_VAL;
	
	//The id of the forum message that is used as a base message either
	//to retrieve replies for or it is the ID of the root topic message
	public int baseMessageID = UNKNOWN_BASE_MESSAGE_ID_VAL;
	
	//If set to true then we only search for the topic messages
	public boolean isOnlyTopics = NOT_ONLY_TOPICS_VAL;
	
	//If set to true then we only search within the current topic
	public boolean isOnlyInCurrentTopic = NOT_ONLY_IN_CURR_TOPIC_VAL;
	
	//If true then we are searching for the message defined
	//by the ID stored in baseMessageID
	public boolean isOnlyMessage = NOT_ONLY_ONE_MESSAGE_VAL;
	
	//If set to true then we search for the forum messages that were approved to be shown on the news page
	//NOTE: This field does not get serialized in the search parameters
	public boolean isApproved = NOT_ONLY_APPROVED_MESSAGES_VAL;
	
	/**
	 * Allows to clone the given object
	 */
	public ForumSearchData clone() {
		ForumSearchData copyData = new ForumSearchData();
		
		copyData.searchString = searchString;
		copyData.byUserLoginName = byUserLoginName;
		copyData.byUserID = byUserID;
		copyData.pageIndex = pageIndex;
		copyData.baseMessageID = baseMessageID;
		copyData.isOnlyTopics = isOnlyTopics;
		copyData.isOnlyInCurrentTopic = isOnlyInCurrentTopic;
		copyData.isOnlyMessage = isOnlyMessage;
		copyData.isApproved = isApproved; 
		
		return copyData;
	}
	
	/**
	 * Removes tabulation and new lines symbols from the search string, validated that the search object is complete
	 * @throws InternalSiteException if the search object is incomplete or inconsistent
	 */
	public void validate() throws InternalSiteException {
		UIErrorMessages errorsI18N = I18NManager.getErrors();
		searchString = ( searchString == null ? "" : searchString.trim() );
		if( ( searchString != null ) && ( searchString.length() > MAX_SEARCH_STRING_LENGTH ) ) {
			throw new InternalSiteException( errorsI18N.searchQueryIsTooLong( MAX_SEARCH_STRING_LENGTH ) );
		}
	}
	
	//The parameter name/value delimiter symbol
	private static final String NAME_VALUE_DELIMITER_SYMBOL = "="; //WARNING: DO NOT CHANGE IT HAS TO BE AS IN A SERVLET REQUEST
	//The symbol used for delimiting the different name/value pairs in the serialization string
	private static final String FIELDS_DELIMITER_SYMBOL = "&";     //WARNING: DO NOT CHANGE IT HAS TO BE AS IN A SERVLET REQUEST
	
	private static final String SEARCH_STRING_PAR_NAME = "ss";
	private static final String BY_USER_LOGIN_PAR_NAME = "ul";
	private static final String BY_USER_ID_PAR_NAME = "uis";
	private static final String PAGE_INDEX_PAR_NAME = "pi";
	private static final String BASE_MESSAGE_ID_PAR_NAME = "bmid";
	private static final String IS_ONLY_TOPICS_PAR_NAME = "iot"; 
	private static final String IS_ONLY_IN_CURR_TOPIC_PAR_NAME = "ioict";
	private static final String IS_ONLY_MESSAGE_PAR_NAME = "iom";
	private static final String IS_APPROVED_MESSAGE_PAR_NAME = "iap";
	
	/**
	 * Allows to copy the values of the request parameters into the given object from the provided map
	 * @param the encoder to use for the de-serialization of the string parameters
	 * @param params the map with the values of some or all of the request parameters, that comes from a HTTP Request
	 */
	public void setParametersFromMap( final EncoderInt encoder, final Map<String, String[]> params ) {
		//Go through the map and take the parameters for the forum search
		boolean hasParameters = false;
		for( Entry<String,String[]> entry : params.entrySet() ) {
			//The map actually contains names mapped to the arrays of values
			//But here for the forum search we do not consider values other
			//than having a single value. Thus we take [0] below. 
			hasParameters = setNameValuePare( encoder, entry.getKey(), entry.getValue()[0], this ) || hasParameters ;
		}
		
		//Check if there we any forum parameters set 
		if( ! hasParameters ) {
			//If not, then set the parameters for the default forum search
			initWithBrowseSectionsParams(this);
		}
	}
	
	/**
	 * Seriazlies the provided data into the string:
	 *    "name" + NAME_VALUE_DELIMITER_SYMBOL + URL.encodeComponent(value) + FIELDS_DELIMITER_SYMBOL
	 * Unless the provided value is null or is a default one, i.e. defaultValue.
	 * @param the encoder to use for the serialization of the string parameters
	 * @param name the name of the field
	 * @param value the value of the field
	 * @param defaultValue the default value for the given parameter
	 * @return the serialized and encoded string, if value is null or is equal to the default one then this method returns an empty string
	 */
	private String serializeStringParam(final EncoderInt encoder, final String name, final String value, final String defaultValue) {
		if( value != null && !value.trim().equals(defaultValue) ) {
			return name + NAME_VALUE_DELIMITER_SYMBOL + encoder.encodeURLComponent(value);
		} else {
			return "";
		}
	}
	
	/**
	 * Allows to deserialize the provided string value
     * @param the encoder to use for the de-serialization of the string parameters
	 * @param value the value to be deserialized
	 * @param defaultValue the value that will re returned if the value parameter is null
	 * @return
	 */
	private String deserializeStringValue( final EncoderInt encoder, final String value, final String defaultValue ) {
		return (value != null && !value.trim().equals(defaultValue)) ? encoder.decodeURLComponent( value ) : defaultValue;
	}
	
	/**
	 * Seriazlies the provided data into the string:
	 *    "name" + NAME_VALUE_DELIMITER_SYMBOL + value + FIELDS_DELIMITER_SYMBOL
	 * Unless the provided value is a default one, i.e. defaultValue.
	 * @param name the name of the field
	 * @param value the value of the field
	 * @param defaultValue the default value for the given parameter
	 * @return the serialized and encoded string if value is the default one then this method returns an empty string
	 */
	private String serializeIntegerParam(final String name, final int value, final int defaultValue) {
		if( value != defaultValue ) {
			return name + NAME_VALUE_DELIMITER_SYMBOL + value;
		} else {
			return "";
		}
	}
	
	private int deserializeIntegerValue( final String value, final int defaultValue ) {
		int result = defaultValue;
		try{
			result = Integer.parseInt( value );
		} catch (NumberFormatException e) {
			//Could not parse, then just use the default value 
		}
		return result;
	}
	
	/**
	 * Seriazlies the provided data into the string:
	 *    "name" + NAME_VALUE_DELIMITER_SYMBOL + value + FIELDS_DELIMITER_SYMBOL
	 * Unless the provided value is a default one, i.e. defaultValue.
	 * @param name the name of the field
	 * @param value the value of the field
	 * @param defaultValue the default value for the given parameter
	 * @return the serialized and encoded string if value is the default one then this method returns an empty string
	 */
	private String serializeBooleanParam(final String name, final boolean value, final boolean defaultValue) {
		if( value != defaultValue ) {
			return name + NAME_VALUE_DELIMITER_SYMBOL + (value? 1 : 0 );
		} else {
			return "";
		}
	}
	
	private static boolean deserializeBooleanValue( final String value, final boolean defaultValue ) {
		boolean result = defaultValue;
		try{
			result = Integer.parseInt( value ) == 1;
		} catch (NumberFormatException e) {
			//Could not parse, then just use the default value 
		}
		return result;
	}
	
	private String addParameterToParametersString( final String parameters, final String param ) {
		if( parameters != null && !parameters.trim().equals("") ) {
			if( param != null && !param.trim().equals("") ) {
				return parameters + FIELDS_DELIMITER_SYMBOL + param;
			} else {
				return parameters;
			}
		} else {
			return param != null ? param : "";
		}
	}
	
	/**
	 * Allows to serialize this search element into a URL safe string
	 * The serialization is URL safe.
	 * @param the encoder to use for the serialization of the string parameters
	 * @return the serialized data string
	 */
	public final String serialize(final EncoderInt encoder) {
		String params = "";
		params = addParameterToParametersString( params, serializeStringParam( encoder, SEARCH_STRING_PAR_NAME, searchString, UNDEFINED_SEARCH_STRING_VAL ) );
		params = addParameterToParametersString( params, serializeStringParam( encoder, BY_USER_LOGIN_PAR_NAME, byUserLoginName, UNDEFINED_USER_LOGIN_VAL ) );
		params = addParameterToParametersString( params, serializeIntegerParam( BY_USER_ID_PAR_NAME, byUserID, UNDEFINED_USER_ID_VAL ) );
		params = addParameterToParametersString( params, serializeIntegerParam( PAGE_INDEX_PAR_NAME, pageIndex, FIRST_PAGE_INDEX_VAL ) );
		params = addParameterToParametersString( params, serializeIntegerParam( BASE_MESSAGE_ID_PAR_NAME, baseMessageID, UNKNOWN_BASE_MESSAGE_ID_VAL ) );
		params = addParameterToParametersString( params, serializeBooleanParam( IS_ONLY_TOPICS_PAR_NAME, isOnlyTopics, NOT_ONLY_TOPICS_VAL ) );
		params = addParameterToParametersString( params, serializeBooleanParam( IS_ONLY_IN_CURR_TOPIC_PAR_NAME, isOnlyInCurrentTopic, NOT_ONLY_IN_CURR_TOPIC_VAL ) );
		params = addParameterToParametersString( params, serializeBooleanParam( IS_ONLY_MESSAGE_PAR_NAME, isOnlyMessage, NOT_ONLY_ONE_MESSAGE_VAL ) );
		params = addParameterToParametersString( params, serializeBooleanParam( IS_APPROVED_MESSAGE_PAR_NAME, isApproved, NOT_ONLY_APPROVED_MESSAGES_VAL ) );
		return params;
	}
	
	/**
	 * Allows to deserialize a URL safe string encoding the search settings into a search object
	 * The serialization is URL safe.
	 * @param the encoder to use for the de-serialization of the string parameters
	 * @param serString a URL safe string encoding the search settings
	 * @return the deserialized search object
	 */
	public static final ForumSearchData deserialize( final EncoderInt encoder, final String serString) {
		ForumSearchData searchObject = new ForumSearchData();
		
		if( serString != null && !serString.trim().isEmpty() ) {
			String[] nameValueTokens = serString.split( FIELDS_DELIMITER_SYMBOL );
			for( int i = 0; i < nameValueTokens.length; i++ ) {
				searchObject.setNameValuePare( encoder, nameValueTokens[i], searchObject );
			}
		} else {
			//If the serialized string is null or is empty then search for sections
			initWithBrowseSectionsParams( searchObject );
		}
		
		return searchObject;
	}
	
	/**
	 * This method should be used when we try to init the forum search data object from a history string or 
	 * a servlet parameters map, but we fail to get any valid search parameters from there. In this case
	 * one should use this method to ensure that the search object is initialized with the default parameters.
	 * NOTE: This method does not reset the object, it just sets several parameters, i.e. the initially
	 * provided object is expected to be blank. The latter is not checked   
	 * @param searchObject the object to initialize with the default search parameters
	 */
	public static final void initWithBrowseSectionsParams( final ForumSearchData searchObject ) {
		searchObject.baseMessageID = ShortForumMessageData.ROOT_FORUM_MESSAGE_ID;
	}
	
	/**
	 * Allows to set the provided object's field by its name and value.
	 * if the name is not recognized as a valid name of an object field,
	 * then this method does nothing.
	 * @param the encoder to use for the de-serialization of the string parameters
	 * @param name a string that is a name of the parameter
	 * @param value a string that is a value of the parameter
	 * @param searchObject the search object to set the data into
	 * @return true if the parameter was recognized and set, otherwise false
	 */
	private boolean setNameValuePare( final EncoderInt encoder, final String name, final String value, final ForumSearchData searchObject) {
		final String NAME  = (name == null ?  "" : name.trim() );
		final String VALUE = (value == null ? "" : value.trim() );
		boolean isRecognized = true;
		if( NAME.equals( SEARCH_STRING_PAR_NAME ) ) {
			searchObject.searchString = deserializeStringValue( encoder, VALUE, UNDEFINED_SEARCH_STRING_VAL );
		} else {
			if( NAME.equals( BY_USER_LOGIN_PAR_NAME ) ) {
				searchObject.byUserLoginName = deserializeStringValue( encoder, VALUE, UNDEFINED_USER_LOGIN_VAL );
			} else {
				if( NAME.equals( BY_USER_ID_PAR_NAME ) ) {
					searchObject.byUserID = deserializeIntegerValue( VALUE, UNDEFINED_USER_ID_VAL );
				} else {
					if( NAME.equals( PAGE_INDEX_PAR_NAME ) ) {
						searchObject.pageIndex = deserializeIntegerValue( VALUE, FIRST_PAGE_INDEX_VAL );
					} else {
						if( NAME.equals( BASE_MESSAGE_ID_PAR_NAME ) ) {
							searchObject.baseMessageID = deserializeIntegerValue( VALUE, UNKNOWN_BASE_MESSAGE_ID_VAL );
						} else {
							if( NAME.equals( IS_ONLY_TOPICS_PAR_NAME ) ) {
								searchObject.isOnlyTopics = deserializeBooleanValue( VALUE, NOT_ONLY_TOPICS_VAL );
							} else {
								if( NAME.equals( IS_ONLY_IN_CURR_TOPIC_PAR_NAME ) ) {
									searchObject.isOnlyInCurrentTopic = deserializeBooleanValue( VALUE, NOT_ONLY_IN_CURR_TOPIC_VAL );
								} else {
									if( NAME.equals( IS_ONLY_MESSAGE_PAR_NAME ) ) {
										searchObject.isOnlyMessage = deserializeBooleanValue( VALUE, NOT_ONLY_ONE_MESSAGE_VAL );
									} else {
										if( NAME.equals( IS_APPROVED_MESSAGE_PAR_NAME ) ){
											searchObject.isApproved = deserializeBooleanValue( VALUE, NOT_ONLY_APPROVED_MESSAGES_VAL );
										} else {
											//NOTE: The name was not recognized, so we just skip it.
											isRecognized = false;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return isRecognized;
	}
	
	/**
	 * Allows to set the provided object's field from the given string that is a "name:value" pair
	 * @param the encoder to use for the de-serialization of the string parameters
	 * @param nameValueToken a tring that is a "name:value" pair
	 * @param searchObject the search object to set the data into
	 */
	private void setNameValuePare( final EncoderInt encoder, final String nameValueToken, final ForumSearchData searchObject) {
		String[] nameValueArr = nameValueToken.split( NAME_VALUE_DELIMITER_SYMBOL );
		if( nameValueArr.length == 2 ) {
			//If the name and the value are properly defined
			final String NAME = nameValueArr[0];
			final String VALUE = nameValueArr[1];
			
			//Set the field value by name
			setNameValuePare( encoder, NAME, VALUE , searchObject);
		}
	}
	
	/**
	 * @return if this object is for browsing approved messages for the news page 
	 */
	public boolean isNewsPageBrowsing() {
		//Note: the page index is not important
		//Only the isApproved has to be set to true
		return ( isApproved != NOT_ONLY_APPROVED_MESSAGES_VAL ) &&
			   ( baseMessageID == UNKNOWN_BASE_MESSAGE_ID_VAL ) &&
			   ( isOnlyMessage == NOT_ONLY_ONE_MESSAGE_VAL ) &&
			   isMainCustomSearchFieldsBlank();
	}
	
	/**
	 * @return true if this object is a forum navigation search request 
	 */
	public boolean isForumNavigation() {
		//Note: the page index is not important
		//In case we are looking for sections only or when only the base messageID is set
		return ( baseMessageID != UNKNOWN_BASE_MESSAGE_ID_VAL ) &&
			   ( isOnlyMessage == NOT_ONLY_ONE_MESSAGE_VAL ) &&
			   ( isApproved == NOT_ONLY_APPROVED_MESSAGES_VAL ) &&
			   isMainCustomSearchFieldsBlank();
	}
	
	/**
	 * @return true if this search request is for viewing one particular forum message
	 */
	public boolean isForumMessageView() {
		//Note: the page index is not important
		//In case we are looking for one particular message, then only the base
		//message is is set and the "is-only-message" marker is on 
		return ( baseMessageID != UNKNOWN_BASE_MESSAGE_ID_VAL ) &&
			   ( isOnlyMessage != NOT_ONLY_ONE_MESSAGE_VAL ) &&
			   ( isApproved == NOT_ONLY_APPROVED_MESSAGES_VAL ) &&
			   isMainCustomSearchFieldsBlank();
	}
	
	/**
	 * True if the data searches for the forum sections with no extra conditions,
	 * EXCEPT FOR THE the page number.
	 * @return true if this is the data for searching the forum sections with no extra conditions.
	 */
	public boolean isBrowsingSectionsSearch() {
		return isForumNavigation() && ( baseMessageID == ShortForumMessageData.ROOT_FORUM_MESSAGE_ID );
	}
	
	/**
	 * @return true if the main fields for the custom search are set to blank, i.e. it is not a custom search.
	 */
	private boolean isMainCustomSearchFieldsBlank() {
		return ( ( searchString == null ) || searchString.trim().equals( UNDEFINED_SEARCH_STRING_VAL ) ) &&
			   ( byUserID == UNDEFINED_USER_ID_VAL) &&
			   ( ( byUserLoginName == null ) || byUserLoginName.trim().equals( UNDEFINED_USER_LOGIN_VAL ) ) &&
			   ( isOnlyTopics == NOT_ONLY_TOPICS_VAL ) &&
			   ( isOnlyInCurrentTopic == NOT_ONLY_IN_CURR_TOPIC_VAL );
	}
}
