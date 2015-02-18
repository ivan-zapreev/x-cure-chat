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
 * The exceptions package for exceptions that come in RPC calls.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client.rpc.exceptions;

import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * @author zapreevis
 * This class should be used for serialization and deserialization of site esceptions
 * that need to be sent from server to client as a part of a regular servlet output.
 */
final public class ExceptionsSerializer {

	//This will be used in toString serializations in servlet's responces, when non RPC servlets are used
	public static final char TOP_DELIMITER_LEFT = '(';
	public static final char TOP_DELIMITER_RIGHT = ')';
	public static final char ELEMENT_DELIMITER = '|';
	public static final char NAME_VALUE_DELIMITER = '=';
	public static final String CLASS_NAME_FIELD_NAME = "CLASS_NAME";
	public static final String ERROR_CODE_FIELD_NAME = "ERROR_CODES";
	public static final char ERROR_CODES_DELIMITER = ',';
	public static final String MAX_FILE_SIZE_FIELD_NAME = "MAX_FILE_SIZE";
	public static final String REMAINING_BLOCKING_FIELD_NAME = "REM_BLOCK_TIME";
	public static final String BLOCKED_LOGIN_FIELD_NAME = "BLOCK_USER_NAME";
	
	/**
	 * This methos should be used to serialize nono-text-message data into a string 
	 */
	static public String serialize( SiteException exception ) {
		String result = "";
		result += TOP_DELIMITER_LEFT + CLASS_NAME_FIELD_NAME + NAME_VALUE_DELIMITER +
				exception.getClass().getName() + ELEMENT_DELIMITER + ERROR_CODE_FIELD_NAME +
				NAME_VALUE_DELIMITER;
		
		//Add all error codes
		Iterator<Integer> iter = exception.getErrorCodes().iterator();
		while( iter.hasNext() ){
			result += iter.next() + ( iter.hasNext() ? ""+ERROR_CODES_DELIMITER : "" );
		}
		
		//Add parameters and return the result
		return  result + serialize_params( exception ) + TOP_DELIMITER_RIGHT;
	}
	
	/**
	 * This method should be overriden by sub classes to provide serialization of extra parameters
	 */
	static protected String serialize_params( SiteException exception ) {
		String super_params = "";
		
		if( exception instanceof SiteLogicException ) {
			if( exception instanceof UserFileUploadException ) {
				super_params += ELEMENT_DELIMITER + MAX_FILE_SIZE_FIELD_NAME + NAME_VALUE_DELIMITER + 
								( ( UserFileUploadException ) exception ).getMaxUploadFileSize();
			}
		} else if ( exception instanceof InternalSiteException ) {
			if ( exception instanceof AccessBlockedException ) {
				super_params += ELEMENT_DELIMITER + REMAINING_BLOCKING_FIELD_NAME + NAME_VALUE_DELIMITER +
								( ( AccessBlockedException ) exception).getRemainingBlockingTime();
			}
		}
		
		return super_params;
	}
	
	/**
	 * This method allows to convert a serialized exception data back into an exception  
	 * @param data the serialized site exception
	 * @return an exception created from the ex_data, an unknown exception 
	 * InternalSiteException.UNKNOWN_INTERNAL_SITE_EXCEPTION_ERR if the data
	 * is not parsable, null if the ex_data is null or an empty string.
	 */
	public static SiteException restoreExceptionFromString( final String data ) {
		SiteException exception = new InternalSiteException( InternalSiteException.UNKNOWN_INTERNAL_SITE_EXCEPTION_ERR ); 
		
		//The hash map into which we will put all the parametes
		HashMap<String, String> param_values = new HashMap<String, String>(); 
		
		//Remove the new line symbols in the responce, if any
		final String exception_data = data.replaceAll("\n", "").trim();
		
		//Parse the response
		if( ( exception_data != null ) && ! exception_data.trim().isEmpty() ) {
			int last_symb_index = exception_data.length() - 1;
			if( ( exception_data.charAt(0) == TOP_DELIMITER_LEFT ) && ( exception_data.charAt( last_symb_index ) == TOP_DELIMITER_RIGHT ) ) {
				String parameters = exception_data.substring( 1, last_symb_index );
				if( ( parameters != null ) && !parameters.trim().isEmpty() ) {
					//Split the string into parameter name/value pairs
					ArrayList<String> params = split( parameters , ELEMENT_DELIMITER );
					if( ( params != null ) && ( params.size() != 0 ) ) {
						for( int i = 0; i < params.size() ; i++ ) {
							ArrayList<String> name_val = split( params.get(i) , NAME_VALUE_DELIMITER );
							if( name_val.size() == 2 ) {
								param_values.put( name_val.get(0), name_val.get(1)  );
							}
						}
					}
					//Process the parameters
					exception = processParameterMappings( param_values );
				} else {
					//If we could not parse the exception then return an unknown one
				}
			} else {
				//If we could not parse the exception then return an unknown one
			}
		} else {
			//If we could not parse the exception then return an unknown one
		}
		
		return exception;
	}
	
	private static SiteException processParameterMappings( HashMap<String, String> param_values ){
		SiteException exception = null;
		
		String className = param_values.get( CLASS_NAME_FIELD_NAME );
		if( className != null ) {
			exception = getExceptionByName( className );
			if( exception != null ) {
				//Fill in error codes
				String errorCodes = param_values.get( ERROR_CODE_FIELD_NAME );
				if( errorCodes != null ) {
					ArrayList<String> codes = split( errorCodes, ERROR_CODES_DELIMITER );
					for( int j = 0 ; j < codes.size(); j++ ) {
						exception.addErrorCode( Integer.parseInt( codes.get(j) ) );
					}
				}
				//Fill in other specific exception parameters
				if( !exception.getErrorCodes().isEmpty() ){
					if( exception instanceof SiteLogicException ) {
						if( exception instanceof UserFileUploadException ) {
							( ( UserFileUploadException ) exception ).setMaxUploadFileSize( Long.parseLong( param_values.get( MAX_FILE_SIZE_FIELD_NAME ) ) );
						}
					} else if ( exception instanceof InternalSiteException ) {
						if ( exception instanceof AccessBlockedException ) {
							( ( AccessBlockedException ) exception ).setRemainingBlockingTime( Integer.parseInt( param_values.get( REMAINING_BLOCKING_FIELD_NAME ) ) );
						}
					}
				}
			}
		}
		
		return exception;
	}
	
	/**
	 * Splitting the string str ito sub strings, using delimiter.
	 * @param str the string to split
	 * @param delimiter the string's delimiter
	 * @return an array of substrings
	 */
	private static ArrayList<String> split( final String str, final char delimiter ) {
		ArrayList<String> result = new ArrayList<String>(); 
		if( ( str != null ) && ! str.trim().isEmpty() ) {
			String word = "";
			for( int index = 0; index < str.length() ; index++ ) {
				char last_symb = str.charAt( index );
				//Add any non-delimiter symbol to the word
				if( last_symb != delimiter ) {
					word += last_symb;
				}
				//If we have a delimiter or the next iteration will not happen, then
				//we add the word we have so far to the array of resulting tokens
				if( ( last_symb == delimiter ) || ( index + 1 >= str.length() ) ) {
					result.add( word.trim() );
					word = "";
				}
			}
		}
		return result;
	}
	
	//The name of this package, made it a string because this exceptions are also converted to AJAX
	//where with obfuscation al class names and packages and etc are removed and changed.
	private static final String EXCEPITON_PACKAGE_NAME = "com.xcurechat.client.rpc.exceptions."; 
	
	/**
	 * Creates an exception instance for the given exception class name
	 * @param exceptionClassName the exception class name
	 * @return null if can not recognize the exception class
	 */
	private static SiteException getExceptionByName( final String exceptionClassName ) {
		if( exceptionClassName.equals( EXCEPITON_PACKAGE_NAME + "InternalSiteException" ) ) {
			return new InternalSiteException();
		} else if( exceptionClassName.equals( EXCEPITON_PACKAGE_NAME + "AccessBlockedException" ) ) {
			return new AccessBlockedException();
		} else if( exceptionClassName.equals( EXCEPITON_PACKAGE_NAME + "LoginAccessBlockedException" ) ) {
			return new LoginAccessBlockedException();
		} else if( exceptionClassName.equals( EXCEPITON_PACKAGE_NAME + "CaptchaTestFailedException" ) ) {
			return new CaptchaTestFailedException();
		} else if( exceptionClassName.equals( EXCEPITON_PACKAGE_NAME + "UserStateException" ) ) {
			return new UserStateException();
		} else if( exceptionClassName.equals( EXCEPITON_PACKAGE_NAME + "UserLoginException" ) ) {
			return new UserLoginException();
		} else if( exceptionClassName.equals( EXCEPITON_PACKAGE_NAME + "IncorrectUserDataException" ) ) {
			return new IncorrectUserDataException();
		} else if( exceptionClassName.equals( EXCEPITON_PACKAGE_NAME + "UserFileUploadException" ) ) {
			return new UserFileUploadException();
		} else if( exceptionClassName.equals( EXCEPITON_PACKAGE_NAME + "UserStateException" ) ) {
			return new UserStateException();
		} else {
			return null;
		}
	}

}
