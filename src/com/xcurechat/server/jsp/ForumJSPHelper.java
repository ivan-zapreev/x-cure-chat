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
 * The server-side RPC package.
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.server.jsp;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;


import com.xcurechat.client.data.ForumMessageData;
import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.client.data.search.ForumSearchData;


import com.xcurechat.client.forum.sharing.ShareMessageLinkBase;

import com.xcurechat.client.rpc.ServerSideAccessManager;

import com.xcurechat.client.utils.FlashObjectWithJWPlayer;
import com.xcurechat.client.utils.PageIndexesGenerator;
import com.xcurechat.client.utils.SupportedFileMimeTypes;
import com.xcurechat.client.utils.widgets.CommonResourcesContainer;

import com.xcurechat.server.redirect.ForumRedirectHelper;
import com.xcurechat.server.redirect.ForumSearchRedirectHelperInt;
import com.xcurechat.server.redirect.MainRedirectHelper;
import com.xcurechat.server.redirect.SectionRedirectHelperInt;

import com.xcurechat.server.utils.EscapeHTMLHelper;
import com.xcurechat.server.utils.MessageSplitHTMLEscape;
import com.xcurechat.server.utils.ServerEncoder;
import com.xcurechat.server.utils.WebUtilities;

/**
 * @author zapreevis
 * This class contains several helper methods for generating forum elements in JSP pages
 */
@SuppressWarnings("unused")
public class ForumJSPHelper {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( ForumJSPHelper.class );
			
	//The encoder that has to be used in the server side
	private static final ServerEncoder encoder = new ServerEncoder(); 
	
	private static final String DISABLED_DOWNLOAD_LINK_STYLE = "xcure-UserDialog-Link-Disabled";

	/**
	 * This class only has static methods so the constructor is made private
	 */
	private ForumJSPHelper() {}

	/**
	 * Allows to generate html for the given forum message
	 * @param req the servlet request
	 * @param locale the locale we are using
	 * @param message the forum message data
	 * @param isForum true if we are generating the message html for 
	 *                the forum, if false then it is for the new page 
	 * @return the forum message html
	 */
	public static String getForumMessageHTML( final HttpServletRequest req, final String locale,
											  ForumMessageData message, final boolean isForum ) {
		String titleStr, linkToPostTitle, viewRepliesStr, downloadFileStr;
		if(locale.equals( WebUtilities.LOCALE_PARAMETER_VALUE_RUSSIAN )) {
			if( message.isForumSectionMessage() ) {
				titleStr = "Раздел:";
				viewRepliesStr = "Войти в раздел";
				linkToPostTitle = "Ссылка на раздел";
			} else {
				if( message.isForumTopicMessage() ) {
					titleStr = "Тема:";
					viewRepliesStr = "Войти в тему";
					linkToPostTitle = "Ссылка на тему";
				} else {
					titleStr = "";
					viewRepliesStr = "Смотреть ответы";
					linkToPostTitle = "Ссылка на пост";
				}
			}
			downloadFileStr = "Скачать файл";
		} else {
			if( message.isForumSectionMessage() ) {
				titleStr = "Section:";
				viewRepliesStr = "Enter section";
				linkToPostTitle = "Link to section";
			} else {
				if( message.isForumTopicMessage() ) {
					titleStr = "Topic:";
					viewRepliesStr = "Enter topic";
					linkToPostTitle = "Link to topic";
				} else {
					titleStr = "";
					viewRepliesStr = "View replies";
					linkToPostTitle = "Link to post";
				}
			}
			downloadFileStr = "Download file";
		}
		
		ForumSearchData messageSearchData = new ForumSearchData();
		messageSearchData.isOnlyMessage = true;
		messageSearchData.baseMessageID = message.messageID;
		final String baseURL = WebUtilities.getSiteURL( req );
		final String linkToPost = SectionRedirectHelperInt.getSiteSectionServletURL( req, ForumRedirectHelper.class, messageSearchData.serialize( encoder ) );
		
		String result =  "<table cellspacing=\"0\" cellpadding=\"0\" class=\"" + CommonResourcesContainer.FORUM_MESSAGE_UI_ODD_STYLE +
						 "\" style=\"width: 100%;\">" +
				  		 "<tbody><tr class=\"top\"><td class=\"topLeft\"><div class=\"topLeftInner\"></div></td><td class=\"topCenter\">" +
				  		 "<div class=\"topCenterInner\"></div></td><td class=\"topRight\"><div class=\"topRightInner\"></div></td></tr>" +
				  		 "<tr class=\"middle\"><td class=\"middleLeft\"><div class=\"middleLeftInner\"></div></td><td class=\"middleCenter\">" +
				  		 "<div class=\"middleCenterInner\">" +
				  		 "<table cellspacing=\"0\" cellpadding=\"0\" style=\"width: 100%;\" class=\"" +
				  		 CommonResourcesContainer.FORUM_MESSAGE_BODY_CONTENT_STYLE + "\">" +
				  		 "<tbody><tr><td align=\"left\" style=\"vertical-align: top;\"><table><tbody><tr><td>";
		
		//Get the title's html prefix and suffix
		final String titlePrefix, titleSuffix;
		if( message.isForumSectionMessage() ) {
			titlePrefix = "<H1>";
			titleSuffix = "</H1>";
		} else {
			if( message.isForumTopicMessage() ) {
				titlePrefix = "<H2>";
				titleSuffix = "</H2>";
			} else {
				if( message.isPostMessage() ) {
					titlePrefix = "<H3>";
					titleSuffix = "</H3>";
				} else {
					titlePrefix = "<H4>";
					titleSuffix = "</H4>";
				}
			}
		}
		//Start the title H tag
		result += titlePrefix;
		
		//Get and escape the message title
		final String escapedMessageTitle = (message.messageTitle == null) ? "" : EscapeHTMLHelper.escapeHTML( message.messageTitle ); 
		
		//Begin view replies wrapper
		if( message.numberOfReplies > 0 ) {
			final ForumSearchData viewRepliesData = new ForumSearchData();
			viewRepliesData.baseMessageID = message.messageID;
			result += "<a title=\"" + viewRepliesStr +": " + escapedMessageTitle + "\" href=\"" +
					  SectionRedirectHelperInt.getSiteSectionServletURL( req, ForumRedirectHelper.class, viewRepliesData.serialize( encoder ) ) + "\">";
		}
		
		//Add message title
		if( message.isForumSectionMessage() || message.isForumTopicMessage() ) {
			/*Add section/topic message title*/
			result += "<div class=\"" + CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_IMP_STYLE_NAME + " " +
					  CommonResourcesContainer.FORUM_TITLE_EXTRA_STYLE + "\">" +
					  "<div class=\"gwt-Label " + CommonResourcesContainer.FORCE_INLINE_DISPLAY_STYLE +
					  " " + CommonResourcesContainer.COMPULSORY_FIELD_STYLE + " " + CommonResourcesContainer.FORUM_TITLE_EXTRA_STYLE + "\"" +
					  " style=\"white-space: nowrap;\">" + titleStr + " </div>" +
					  "<div class=\"gwt-Label " + CommonResourcesContainer.FORCE_INLINE_DISPLAY_STYLE + "\">";
		} else {
			/*Add regular forum message title*/
	  		result += "<div class=\"" + CommonResourcesContainer.SIMPLE_FORUM_MESSAGE_SUBJECT_STYLE + " " +
	  				  CommonResourcesContainer.FORUM_MESSAGE_TITLE_STYLE + "\">" + "<div class=\"gwt-Label " +
	  				  CommonResourcesContainer.FORCE_INLINE_DISPLAY_STYLE + " " + CommonResourcesContainer.USER_DIALOG_REGULAR_FIELD_STYLE +
					  "\" style=\"white-space: nowrap; \">" + titleStr + " </div><div class=\"gwt-Label " +
					  CommonResourcesContainer.FORCE_INLINE_DISPLAY_STYLE + " " + CommonResourcesContainer.CONST_FIELD_VALUE_DEFAULT_STYLE_NAME + "\">";
		}
		result += escapedMessageTitle + "</div></div>";
		
		//End view replies wrapper
		if( message.numberOfReplies > 0 ) {
			result += "</a>";
		}
		
		//End the title H tag
		result += titleSuffix;
  		
		result += "</td></tr></tbody></table></td></tr><tr><td align=\"left\" style=\"vertical-align: top;\">" +
				"<div class=\"" + CommonResourcesContainer.FORUM_MESSAGE_TITLE_DELIMITER_PANEL_STYLE + "\"></div></td></tr><tr>";
		
		//Get the escaped message body EscapeHTMLHelper.escapeHTML( message.messageBody );
		final String escapedMessageBody;
		if( message.messageBody != null ) {
			MessageSplitHTMLEscape escaper = new MessageSplitHTMLEscape( message.messageBody );
			escapedMessageBody = escaper.process();
		} else {
			escapedMessageBody = "";
		}
		
		result += "<td align=\"left\" style=\"vertical-align: top;\">"+
				  "<div class=\"gwt-Label\">" + escapedMessageBody.replaceAll("\\n", "<br/>") + "</div></td></tr>";
		if( message.attachedFileIds != null && message.attachedFileIds.size() > 0 ) {
			//Sort out the images and the flash files
			List<ShortFileDescriptor> images = new ArrayList<ShortFileDescriptor>(), flash = new ArrayList<ShortFileDescriptor>();
			for( ShortFileDescriptor fileDesc: message.attachedFileIds ) {
				if( SupportedFileMimeTypes.isImageMimeType( fileDesc.mimeType ) ) {
					images.add( fileDesc );
				} else {
					if( SupportedFileMimeTypes.isPlayableMimeType( fileDesc.mimeType ) ) {
						flash.add( fileDesc );
					} else {
						//An unknown file type, do nothing
					}
				}
			}
			//Add images
			if(images.size() > 0 ) {
				result += "<tr><td><div>";
				for( ShortFileDescriptor imageFileDesc : images ) {
					result += "<a href=\"" + baseURL + ServerSideAccessManager.getRelativeForumFileURL(encoder, imageFileDesc, false) +
							  "\" target=\"_blank\">";
					result += "<image class=\"" + CommonResourcesContainer.ATTACHED_IMAGE_PREVIEW_STYLE + " " + CommonResourcesContainer.ZOOME_IN_IMAGE_STYLE +
							  " " + CommonResourcesContainer.FORCE_INLINE_DISPLAY_STYLE + "\" alt=\"" + escapedMessageTitle + "\" src=\"" +
							  baseURL + ServerSideAccessManager.getRelativeForumFileURL(encoder, imageFileDesc, true) + "\" />";
					result += "</a> &nbsp; ";
				}
				result += "</div></td><tr>";
			}
			//Add flash
			if(flash.size() > 0 ) {
				result += "<tr><td>";
				for( ShortFileDescriptor mediaFileDesc : flash ) {
					//Construct the embedded flash object
					FlashObjectWithJWPlayer flashObject = new FlashObjectWithJWPlayer( null, linkToPost, baseURL );
					flashObject.setMediaUrl( baseURL + ServerSideAccessManager.getRelativeForumFileURL( encoder,  mediaFileDesc, false ), mediaFileDesc.mimeType );
					flashObject.completeEmbedFlash( message.isApproved );
					result += "<div style=\"float: left;\" title=\"" + escapedMessageTitle +
						      "\" class=\"" + CommonResourcesContainer.FORCE_INLINE_DISPLAY_STYLE + " " + 
						      CommonResourcesContainer.FLASH_PLAYER_WIDGET_STYLE + "\">" + flashObject +
						      "<div class=\"" + DISABLED_DOWNLOAD_LINK_STYLE + "\">&#060;&#060;" +
						      downloadFileStr + "&#062;&#062;</div>" + "</div> &nbsp; "; 
				}
				
				result += "</td><tr>";
			}
		}
		
		result += "<tr><td align=\"left\" style=\"vertical-align: top;\">" +
				  "<table cellspacing=\"0\" cellpadding=\"0\" style=\"width: 100%;\">" +
				  "<tbody><tr><td align=\"right\" style=\"vertical-align: bottom;\">" +
				  "<table cellspacing=\"0\" cellpadding=\"0\"><tbody><tr>" +
				  "<td align=\"right\" style=\"vertical-align: bottom;\"><div class=\"gwt-HTML\">";
		//
		//Do not add the direct link to the post to the jsp version of the website because this will
		//cause many single-message queries to be cached when the SEO is indexing the website.
		//
		//result += "<a class=\"" + ShareMessageLinkBase.SHARE_FORUM_MESSAGE_LINK_STYLE + "\" href=\"" + linkToPost +
		//			"\"target=\"_blank\">" + "<img class=\"gwt-Image " + ShareMessageLinkBase.SHARE_FORUM_MESSAGE_LINK_IMAGE_STYLE +
		//			"\"title=\"" + linkToPostTitle + ": " + escapedMessageTitle + "\" src=\"images/messages/weblink.png\"></a>";
		//
		result += "</div></td></tr></tbody></table></td></tr></tbody></table></td></tr></tbody></table>" +
				"</div></td><td class=\"middleRight\"><div class=\"middleRightInner\"></div></td></tr>" +
				"<tr class=\"bottom\"><td class=\"bottomLeft\"><div class=\"bottomLeftInner\"></div></td>" +
				"<td class=\"bottomCenter\"><div class=\"bottomCenterInner\"></div></td><td class=\"bottomRight\">" +
				"<div class=\"bottomRightInner\"></div></td></tr></tbody></table>";
		return result;
	}
	
	/**
	 * Allows to get the string with the HTML for the forum/news page indexes
	 * @param request the request data with the search parameters in it
	 * @param locale the locale
	 * @param number_of_messages the total number of messages in the search results 
	 * @param redirectHelperClass a class that implements ForumSearchRedirectHelperInt
	 * @return the html string with paging
	 */
	@SuppressWarnings("unchecked")
	public static String getSearchPageIndexesHTML( final HttpServletRequest request, final String locale,
												   final int number_of_messages, final Class redirectHelperClass ) {
		String result = "";
		final int numberOfPages = PageIndexesGenerator.getNumberOfPages( number_of_messages, ForumSearchData.MAX_NUMBER_OF_MESSAGES_PER_PAGE );
		
		//Check that the class we got is a child of the right class
		ForumSearchRedirectHelperInt helper = null;
		try{
			helper = (ForumSearchRedirectHelperInt) redirectHelperClass.newInstance();
		}catch( Exception e ){
			logger.error("Unable to provide page string", e);
			throw new RuntimeException( e );
		}
		
		//Provide the string with pages
		if( numberOfPages > 0 ) {
			final ForumSearchData searchObject = helper.getSearchDataObject( request );
			final int currentPageIdx = searchObject.pageIndex;
			
			//Generate page indexes
			List<Integer> indexes = PageIndexesGenerator.getPageIndexes( ForumSearchData.MINIMUM_PAGE_INDEX,
										     							 currentPageIdx, numberOfPages );
			if(locale.equals( WebUtilities.LOCALE_PARAMETER_VALUE_RUSSIAN )) {
				result += "Страницы: &nbsp;";
			} else {
				result += "Pages: &nbsp;";
			}
			
			for(Integer index : indexes) {
				if( index == PageIndexesGenerator.DUMMY_PAGE_INDEX ) {
					result += "...&nbsp;";
				} else {
					if( index == currentPageIdx  ) {
						result += "<b>" + index + "</b>&nbsp;";
					} else {
						searchObject.pageIndex = index;
						result +=  "<a href=\"" + 
									SectionRedirectHelperInt.getSiteSectionServletURL( request, redirectHelperClass, searchObject.serialize( encoder ) ) +
								   "\">" + index + "</a>&nbsp;";
					}
				}
			}
		}
		
		return result;
	}
	
	public static String getForumSearchPageIndexesHTML( final HttpServletRequest request, final String locale, final int number_of_messages ) {
		return getSearchPageIndexesHTML( request, locale, number_of_messages, ForumRedirectHelper.class );
	}
	
	public static String getNewsSearchPageIndexesHTML( final HttpServletRequest request, final String locale, final int number_of_messages ) {
		return getSearchPageIndexesHTML( request, locale, number_of_messages, MainRedirectHelper.class );
	}
}
