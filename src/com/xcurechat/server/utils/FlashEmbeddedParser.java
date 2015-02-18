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
 * The server-side utilities package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.server.utils;

import java.util.Vector;

import org.htmlparser.Parser;
import org.htmlparser.util.NodeList;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.Attribute;

import org.apache.log4j.Logger;

import com.xcurechat.client.utils.FlashEmbeddedObject;
import com.xcurechat.client.utils.FlashEmbeddedObject.ObjectTagSearchHelper;

import com.xcurechat.client.rpc.exceptions.MessageException;

/**
 * @author zapreevis
 * This class searches for the embedded objects in the provided
 * string and then tries to parse them and construct an object
 * that is then expeted to be a Flash embedded movie.
 */
public class FlashEmbeddedParser {
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( FlashEmbeddedParser.class );
	
	private final String xcureDomainPattern;
	
	/**
	 * A trivial constructor
	 * @param xcureDomainPattern stores the pattern of the XCure-chat domain, or null if it is not set
	 * 							  if the embedded object URL matches the XCure-Chat domain pattern then
	 * 							  the allowScriptAccess is set to NEVER, the same happens if this param
	 * 							  is null, otherwise the allowScriptAccess is set to SAMEDOMAIN
	 */
	public FlashEmbeddedParser(final String xcureDomainPattern){
		this.xcureDomainPattern = xcureDomainPattern;
	}
	
	/**
	 * Parses the proided string for OBJECT and EMBED tags, parses the found tags and makes sure that
	 * the EMBED tag is always wrapped around with an OBJECT tag, and that their parameters and attributes
	 * are set to correct values, also checks that the objets repreent embedded flash animation.
	 * @param inputText the input text to pase
	 * @return the updated string with properly configured embedded objects
	 * @throws MessageException in case an embedded object is malformed, e.g. there is no closing tag
	 * or it is not a valid HTML is some other way or it is not a embedded flash animation
	 */
	public String processEmbeddedTags( final String inputText ) throws MessageException {
		ObjectTagSearchHelper<String> helper = new ObjectTagSearchHelper<String>() {
			//The string that accumulates the output string
			private String outputText = "";
			
			public void processObjectFreeSubstring( final String substring ) throws MessageException {
				if( substring != null ) {
					outputText += searchForEmbeddedTag( substring, substring.toLowerCase());
				}
			}
			public void processObjectTagSubstring( final String substring ) throws MessageException {
				if( substring != null ) {
					outputText += parseTheEmbeddedObject( substring );
				}
			}
			public void onObjectCloseTagNotFound() throws MessageException {
				logger.debug( "We could not find the end of this object tag in '"+inputText+"'! Further parsing for the OBJECT tags is useless!" );
				throw new MessageException( MessageException.IMPROPER_EMBEDDED_OBJECT );
			}
			public String getResult() {
				return outputText;
			}
		};
		
		//Process the embedded object tags
		FlashEmbeddedObject.processEmbeddedTags(inputText, helper);
		
		//Return the processed string
		return helper.getResult();
	}
	
	private String searchForEmbeddedTag( final String inputText, final String inputTextLowerCase) throws MessageException {
		String outputText = "";
		
		logger.debug("Searching for embedded tags in the string: " + inputText);
		
		int begin_index = 0, old_begin_index = 0;
		while( ( begin_index = inputTextLowerCase.indexOf( FlashEmbeddedObject.EMBED_OPEN_TAG, old_begin_index ) ) != -1 ) {
			outputText += inputText.substring( old_begin_index, begin_index );
			
			int embed_end_tag_one_index = inputTextLowerCase.indexOf( FlashEmbeddedObject.EMBED_CLOSE_TAG_ONE, begin_index );
			int embed_end_tag_two_index = inputTextLowerCase.indexOf( FlashEmbeddedObject.EMBED_CLOSE_TAG_TWO, begin_index );
			
			if( embed_end_tag_one_index != -1 || embed_end_tag_two_index != -1 ) {
				//We have found where the OBJECT ends
				int embed_end_tag_index = (embed_end_tag_one_index != -1) ? embed_end_tag_one_index + FlashEmbeddedObject.EMBED_CLOSE_TAG_ONE.length() :
																			embed_end_tag_two_index + FlashEmbeddedObject.EMBED_CLOSE_TAG_TWO.length();
				
				//Get the object string and parse it, then validate the flash if it is valid include it into the text
				final String embedNodeString = inputText.substring( begin_index, embed_end_tag_index ).replace('\n', ' ');
				logger.debug("Found and embedded node substring: " + embedNodeString + ", attempting to parse.");
				outputText += parseTheEmbeddedObject( embedNodeString );
				
				//Update the old begin index
				old_begin_index = embed_end_tag_index;
			} else {
				logger.debug( "We could not find the end of this embed tag in '"+inputText+"'! Further parsing for the EMBED tags is useless!" );
				throw new MessageException( MessageException.IMPROPER_EMBEDDED_OBJECT );
				//We could not find the end of this embed tag! Further
				//parsing for the EMBED tags is useless, skip on
				//the false <EMBED beginning tag
				//old_begin_index = begin_index + FlashEmbeddedObject.EMBED_OPEN_TAG.length();
				//break;
			}
		}
		//Add the remaining substring
		outputText += inputText.substring( old_begin_index, inputText.length() );
		
		return outputText;
	}
	
	/**
	 * Processes the OBJECT node that should contain the Flash animation:
	 * @param objects the list of object tag nodes, should not be more than two, the begin and the end tags!
	 * @param flashObjToFill the flash obect to fill in with data
	 * @return the updated flash object
	 * @throws MessageException if there are more than one EMBED subtags
	 */
	@SuppressWarnings("unchecked")
	private FlashEmbeddedObject parseFlashObjectTag( final NodeList objects, final FlashEmbeddedObject flashObjToFill ) throws MessageException {
		if( objects != null ) {
			for( int j = 0; j < objects.size() ; j++ ) {
				Node objectNode = objects.elementAt( j );
				if( objectNode instanceof Tag ) {
					Tag objectTag = (Tag) objectNode;
					//If it is not an end node then we process its attributes, if it is an empty 
					//XML tag then we do the same I believe an empty XML tag is smth like: <TAG />
					if( !objectTag.isEndTag() || objectTag.isEmptyXmlTag() ) {
						//Process the attributes
						logger.debug("Processing object node's '" + objectTag + "' attributes");
						Vector<Attribute> atts = (Vector<Attribute>) objectTag.getAttributesEx();
						if( atts != null ) {
							for( Attribute att : atts ) {
								String nameValue = att.getName();
								String valueValue = att.getValue();
								if( ! flashObjToFill.setNameValue( nameValue, valueValue ) ) {
									logger.warn("An unknown OBJECT attribute, name='" + nameValue + "' value='" + valueValue + "'" );
								} else {
									logger.debug("Set the OBJECT attribute, name='" + nameValue + "' value='" + valueValue + "'");
								}
							}
						}
						
						//Process the parameters
						logger.debug("Processing object node's '" + objectTag + "' parameters");
						NodeList parameters = objectTag.getChildren().extractAllNodesThatMatch( new TagNameFilter( FlashEmbeddedObject.PARAM_TAG_NAME ) );
						for( int i = 0 ; i < parameters.size(); i++) {
							Node node = parameters.elementAt( i );
							if( node instanceof Tag ){
								String nameValue = ((Tag) node).getAttribute( FlashEmbeddedObject.PARAM_NAME_ATTR );
								String valueValue = ((Tag) node).getAttribute( FlashEmbeddedObject.PARAM_VALUE_ATT );
								if( nameValue != null && valueValue != null ) {
									if( ! flashObjToFill.setNameValue( nameValue, valueValue ) ) {
										logger.warn("An unknown OBJECT parameter, name='" + nameValue + "' value='" + valueValue + "'" );
									} else {
										logger.debug("Set the OBJECT tag parameter, name='" + nameValue + "' value='" + valueValue + "'");
									}
								}
							} else {
								logger.warn( "Encountered a PARAM node: " + node + " that is not a PARAM tag!" );
							}
						}
						
						//Process the embed tag if any
						logger.debug("Processing object node's '" + objectTag + "' embed child tags");
						NodeList embeds = objectTag.getChildren().extractAllNodesThatMatch( new TagNameFilter( FlashEmbeddedObject.EMBED_TAG_NAME ) );
						//Note we might have at most two tags, one opening and one closing
						if( embeds.size() <= 2 ) {
							parseFlashEmbedTag( embeds, flashObjToFill );
						} else {
							logger.error("The number of the EMBED subtags of the OBJECT tag is greater than two (" + embeds.size() + ")");
							throw new MessageException( MessageException.IMPROPER_EMBEDDED_OBJECT );
						}
					} else {
						logger.warn( "Encountered an OBJECT node: " + objectTag + " that is an end tag!" );
					}
				}
			}
		}
		return flashObjToFill;
	}
	
	/**
	 * Processes the EMBED node that should contain the Flash animation:
	 * @param embedTag the Root object tag to tackle
	 * @param flashObjToFill the flash obect to fill in with data
	 * @return the updated flash object
	 */
	@SuppressWarnings("unchecked")
	private FlashEmbeddedObject parseFlashEmbedTag( NodeList embeds, final FlashEmbeddedObject flashObjToFill ) {
		if( embeds != null ) {
			logger.debug( "The number of embed-tag nodes is " + embeds.size() );
			for( int i = 0; i < embeds.size() ; i++ ) {
				Node embedNode = embeds.elementAt( i );
				if( embedNode instanceof Tag ) {
					Tag embedTag = (Tag) embedNode;
					//If it is not an end node then we process its attributes, if it is an empty 
					//XML tag then we do the same I believe an empty XML tag is smth like: <TAG />
					if( !embedTag.isEndTag() || embedTag.isEmptyXmlTag() ) {
						//Process the attributes
						logger.debug("Processing embed node's '" + embedTag + "' attributes");
						Vector<Attribute> atts = (Vector<Attribute>) embedTag.getAttributesEx();
						if( atts != null ) {
							for( Attribute att : atts ) {
								String nameValue = att.getName();
								String valueValue = att.getValue();
								if( ! flashObjToFill.setNameValue( nameValue, valueValue ) ) {
									logger.warn("An unknown EMBED attribute, name='" + nameValue + "' value='" + valueValue + "'" );
								} else {
									logger.debug("Set the EMBED attribute, name='" + nameValue + "' value='" + valueValue + "'");
								}
							}
						}
					} else {
						logger.warn( "Encountered an EMBED node: " + embedTag + " that is an end tag!" );
					}
				} else {
					logger.warn( "Encountered a EMBED node: " + embedNode + " that is not an EMBED tag!" );
				}
			}
		} else {
			logger.debug( "The list of embed-tag nodes is null" );
		}
		return flashObjToFill;
	}
	
	/**
	 * Parses the embedded object, creates the Flash embedded object out of it,
	 * if possible, then serializes it into string and returns the string.
	 * If the object could not be parseed or it turnes out to be a non Flash
	 * embedded object, then an exception is thrown
	 * @param textToParse the text to parse
	 * @return the string with the filtered, verified and completed embedded
	 *         Flash animation embedding code. Creates Flash with the sameDomain
	 *         security level.
	 * @throws MessageException if the provided HTML code is broken or the animation was detected to be not a flash movie
	 */
	private String parseTheEmbeddedObject( final String textToParse ) throws MessageException {
		String result = "";
		try{
			logger.debug("Trying to parse the found message-embedded object: " + textToParse );
			Parser parser = new Parser( new Lexer( textToParse ) );
			NodeList nodes = parser.parse( null );
			//Process the nodes in the result
			NodeList objects = nodes.extractAllNodesThatMatch( new TagNameFilter( FlashEmbeddedObject.OBJECT_TAG_NAME ) );
			/* Create Flash with the never security level, to prevent Flash injection,
			   the user can have a url pointing to XCure itself but not an external
			   flash with the getURL exevuting malicius JavaScript that, e.g. reads
			   the user's session coockies */
			FlashEmbeddedObject flashObject = new FlashEmbeddedObject( xcureDomainPattern );
			if( (objects.size() <= 2 ) && ( objects.size() > 0 ) ) {
				//If there are OBJECT tags then parse them
				parseFlashObjectTag( objects, flashObject );
			} else {
				//If there are no OBJECT tags then parse the EMBED tags
				NodeList embeds = nodes.extractAllNodesThatMatch( new TagNameFilter( FlashEmbeddedObject.EMBED_TAG_NAME ) );
				if( embeds.size() <= 2 ) {
					//There should not be more than two EMBED tags because one is the open and another is the close tags
					parseFlashEmbedTag( embeds, flashObject );
				} else {
					logger.error("An improper number of the object (" + objects.size() +
								 ") and embed (" + embeds.size() + ") tags in the string: " + textToParse);
					throw new MessageException( MessageException.IMPROPER_EMBEDDED_OBJECT );
				}
			}
			//Validate the obtained flash object
			if( flashObject.isValidEmbedFlash() ) {
				//Complete the flash object
				flashObject.completeEmbedFlash();
				//Serialize the object into String
				result = flashObject.toString();
			} else {
				logger.error( "The parsed embedded object '" + textToParse +
							  "' was not recognized as a valid flash animation, we got:" + flashObject.toString() );
				throw new MessageException( MessageException.IMPROPER_EMBEDDED_OBJECT );
			}
		} catch( Exception e ) {
			logger.error("Unable to parse the embedded object from the user's message: " + textToParse, e);
			throw new MessageException( MessageException.IMPROPER_EMBEDDED_OBJECT );
		}
		return result;
	}
}
