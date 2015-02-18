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
 * The server side (RPC, servlet) access package.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.client.rpc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;

import com.google.gwt.http.client.URL;

import com.xcurechat.client.SiteManager;
import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.client.utils.ClientEncoder;
import com.xcurechat.client.utils.SupportedFileMimeTypes;
import com.xcurechat.client.utils.EncoderInt;

/**
 * @author zapreevis
 * This class is supposed to simplify accessing the references
 * to the server side access-objects, such as RPC interfaces and
 * other servlet URLs.
 */
public class ServerSideAccessManager {
	//The pattern for the allowed file names
	private static final String ALLOWED_FILE_NAME_PATTERN = "(\u0020|[a-zA-Z]|[0-9]|\\_|\\-|\\.|\\(|\\))+";
	//Some constants for forming the servler/jsp parameters
	public static final String URL_QUERY_DELIMITER = "?";
	public static final String SERVLET_PARAMETERS_DELIMITER = "&";
	public static final String SERVER_PARAM_NAME_VAL_DELIM = "=";
	public static final String URI_HISTORY_TOKEN_SYMBOL = "#";
	//The server context delimiter symbol
	public static final String SERVER_CONTEXT_DELIMITER = "/";
	
	//The name of the captcha (regular) servlet context
	public static final String CAPTCHA_SERVLET_CONTEXT = "captcha";
	//The name of the profile image retrieval servlet
	public static final String USER_PROFILE_FILES_SERVLET_CONTEXT = "profilefiles";
	//The name of the chat-message files management servlet
	public static final String CHAT_MESSAGE_FILES_SERVLET_CONTEXT = "chatfiles";
	//The name of the profile avatar manager servlet
	public static final String USER_PROFILE_AVATAR_SERVLET_CONTEXT = "avatars";
	//The name of the forum-files management servlet
	public static final String FORUM_FILES_SERVLET_CONTEXT = "forumfiles";
	//The name of the dispatcher management servlet
	public static final String DISPATCHER_SERVLET_CONTEXT = "dispatcher"; /*This servlet is obsolete*/
	
	//The list of servlet names for which the url should stay unchanged
	private static final List<String> fileServletNames = new ArrayList<String>();
	
	static {
		fileServletNames.add( ServerSideAccessManager.CAPTCHA_SERVLET_CONTEXT );
		fileServletNames.add( ServerSideAccessManager.USER_PROFILE_FILES_SERVLET_CONTEXT );
		fileServletNames.add( ServerSideAccessManager.CHAT_MESSAGE_FILES_SERVLET_CONTEXT );
		fileServletNames.add( ServerSideAccessManager.USER_PROFILE_AVATAR_SERVLET_CONTEXT );
		fileServletNames.add( ServerSideAccessManager.FORUM_FILES_SERVLET_CONTEXT );
		fileServletNames.add( ServerSideAccessManager.DISPATCHER_SERVLET_CONTEXT );
	}
	
	/**
	 * Allows to detect if this is a known site file servlet-based URL.
	 * @param siteUrl this site URL to check
	 * @return true if the URL is recognized to be our site servlet URL
	 */
	public static boolean isOurFileServletURL( final String siteUrl, final String moduleBase ) {
		boolean isTrue = false;
		if( ( siteUrl != null ) && ( moduleBase != null ) ) {
			Iterator<String> iter = fileServletNames.iterator();
			while( ( !isTrue ) && iter.hasNext() ) {
				final String servletContextPrefix = moduleBase + iter.next();
				isTrue |=  siteUrl.startsWith( servletContextPrefix + URL_QUERY_DELIMITER ) ||
						   siteUrl.startsWith( servletContextPrefix + SERVER_CONTEXT_DELIMITER );
			}
		}
		return isTrue;
	}
	
	//The chat servlet context, corresponds to the section's history-token identifier in the GWT version of the website 
	public static final String CHAT_SECTION_SERVLET_CONTEXT = "chat";
	//The forum servlet context, corresponds to the section's history-token identifier in the GWT version of the website
	public static final String FORUM_SECTION_SERVLET_CONTEXT = "forum";
	//The forum servlet context, corresponds to the section's history-token identifier in the GWT version of the website
	public static final String TOP10_SECTION_SERVLET_CONTEXT = "rating";
	//The news servlet context, corresponds to the section's history-token identifier in the GWT version of the website
	public static final String MAIN_SECTION_SERVLET_CONTEXT = "main";
	//The info servlet context, corresponds to the section's history-token identifier in the GWT version of the website
	public static final String INFO_SECTION_SERVLET_CONTEXT = "info";
	//The client side encoder
	private static final ClientEncoder encoder = new ClientEncoder();
	
	//The default file name prefix for the forum file
	public static final String FORUM_FILE_DEFAULT_NAME_PREFIX = "forumfile";
	
	//The default file name prefix for the chat file
	public static final String CHAT_FILE_DEFAULT_NAME_PREFIX = "chatfile";
	
	//The default file name prefix for the user-profile file
	public static final String PROFILE_FILE_DEFAULT_NAME_PREFIX = "profile";
	
	/**
	 * Given the file description allows to construct an allowed file name for the file.
	 * @param fileName the original file name
	 * @param the id of the file, in case it is in the database, in case the file id is not known then ShortFileDescriptor.UNKNOWN_FILE_ID
	 * @param altFileNamePrefix the alternative file name prefix, in case the original file name can not be allowed
	 * @return the file name or ShortFileDescriptor.UNKNOWN_FILE_NAME if the file name could not be constructed
	 */
	public static String getURLAllowedFileName( final String fileName, final int fileID, final String altFileNamePrefix ) {
		String resultingFileName = ShortFileDescriptor.UNKNOWN_FILE_NAME;
		if( fileName != null && ! fileName.trim().isEmpty() ) {
			if( fileName.matches( ALLOWED_FILE_NAME_PATTERN ) ) {
				resultingFileName = fileName.trim(); 
			} else {
				final String ext = SupportedFileMimeTypes.getFileExtension( fileName );
				if( ext != null ) {
					if( fileID != ShortFileDescriptor.UNKNOWN_FILE_ID ) {
						resultingFileName = altFileNamePrefix + fileID + ext;
					} else {
						resultingFileName = altFileNamePrefix + ext;
					}
				}
			}
		}
		return resultingFileName;
	}
	
	/**
	 * Given the file descriptor allows to construct an allowed file name for the file.
	 * @param fileDescr the file descriptor
	 * @param altFileNamePrefix the alternative file name prefix, in case the original file name can not be allowed
	 * @return the file name or ShortFileDescriptor.UNKNOWN_FILE_NAME if the file name could not be constructed
	 */
	public static String getURLAllowedFileName( final ShortFileDescriptor fileDescr, final String altFileNamePrefix ) {
		String resultingFileName = ShortFileDescriptor.UNKNOWN_FILE_NAME;
		if( fileDescr != null ) {
			resultingFileName = getURLAllowedFileName( fileDescr.fileName, fileDescr.fileID, altFileNamePrefix );
		}
		return resultingFileName;
	}
	
	/**
	 * @return returns the URL of the captcha servlet
	 */
	public static String getCaptchaProblemServletURL(){
		//The time is added in order to force image reloading, the servlet itself does not read this parameter
		return GWT.getModuleBaseURL() + CAPTCHA_SERVLET_CONTEXT + URL_QUERY_DELIMITER + System.currentTimeMillis();
	}
	
	/**
	 * @return returns the URL of the profile-file uploading servlet
	 */
	public static String getProfileFilesManagerURL(){
		return GWT.getModuleBaseURL() + USER_PROFILE_FILES_SERVLET_CONTEXT;
	}
	
	/**
	 * @return returns the URL of the chat-message files uploading servlet
	 */
	public static String getChatMessageFileManagerURL() {
		return GWT.getModuleBaseURL() + CHAT_MESSAGE_FILES_SERVLET_CONTEXT ;
	}
	
	/**
	 * @return returns the URL of the avatar uploading servlet
	 */
	public static String getProfileAvatarManagerURL(){
		return GWT.getModuleBaseURL() + USER_PROFILE_AVATAR_SERVLET_CONTEXT;
	}
	
	/**
	 * @return returns the URL of the forum's file uploading servlet
	 */
	public static String getForumFileUploadServletURL(){
		return GWT.getModuleBaseURL() + FORUM_FILES_SERVLET_CONTEXT;
	}
	
	/**
	 * Allows to get the forum message URL based on the Dispatcher servlet, but not on the GWT #-based link
	 * @param messageQuery the selector query for the forum message
	 * @return the complete URL
	 */
	public static String getForumMessageServletBasedURL(final String messageQuery) {
		return GWT.getModuleBaseURL() + FORUM_SECTION_SERVLET_CONTEXT + URL_QUERY_DELIMITER + messageQuery;
	}

	//Parameters common for the file upload/retrieval servlets
	public static final String USER_ID_SERVLET_PARAM = "userID";
	public static final String SESSION_ID_SERVLET_PARAM = "sess";
	public static final String FILE_UPLOAD_SERVLET_PARAM = "upload";
	
	//Parameters of the user file retrieval servlet(s)
	public static final String FOR_USER_ID_IMAGE_SERVLET_PARAM = "forUserID";
	public static final String IS_THUMBNAIL_SERVLET_PARAM = "thumb";
	public static final String IS_IMAGE_SERVLET_PARAM = "isImg";
	public static final String DUMMY_TIME_SERVLET_PARAM = "time";
	public static final String ROOM_ID_CHAT_FILES_SERVLET_PARAM = "roomID";
	public static final String FILE_ID_CHAT_FILES_SERVLET_PARAM = "imageID";
	public static final String FILE_ID_SERVLET_PARAM = "fileID";
	public static final String IS_MESSAGE_ATTACHED_SERVLET_PARAM = "isMsgAtt";
	//Is the parameter that allows to indicate if the given file needs to be downloaded or not
	public static final String IS_DOWNLOAD_FILE_SERVLET_PARAM = "isDwnld";
	
	/**
	 * Allows to get a URL suffix that should indicate the servlet about us wanting to download/or just view the linked file.
	 * @param isDownload true to download the file, false fot just viewing or playing it
	 * @return the "&"+IS_DOWNLOAD_FILE_SERVLET_PARAM+"="+((isDownload) ? "1" : "0") string
	 */
	public static String getDownloadFileURLSuffix( final boolean isDownload ) {
		return SERVLET_PARAMETERS_DELIMITER + IS_DOWNLOAD_FILE_SERVLET_PARAM+ SERVER_PARAM_NAME_VAL_DELIM + ( (isDownload) ? "1" : "0" );
	}
	
	/**
	 * Return the URL for the default user profile file
	 * @param fileDescr the user file descriptor
	 * @param isThumbnail true if we need a thumbnail, otherwise false
	 * @return the required URL
	 */
	public static String getProfileFileURL( final int userID, final ShortFileDescriptor fileDescr, final boolean isThumbnail ) {
		final int fileID, ownerID = userID;
		final String fileName = getURLAllowedFileName( fileDescr, PROFILE_FILE_DEFAULT_NAME_PREFIX );
		final boolean isImageFile;
		if( fileDescr == null ) {
			fileID = ShortFileDescriptor.UNKNOWN_FILE_ID;
			isImageFile = true;
		} else {
			fileID = fileDescr.fileID;
			isImageFile = SupportedFileMimeTypes.isImageMimeType( fileDescr.mimeType ) ||
			  			  SupportedFileMimeTypes.isImageMimeType( SupportedFileMimeTypes.getFileMimeTypeStringByExtension( fileName ) );
		}
		
		return	URL.encode( GWT.getModuleBaseURL() +
				USER_PROFILE_FILES_SERVLET_CONTEXT +
				SERVER_CONTEXT_DELIMITER + URL.encodeQueryString( fileName ) + URL_QUERY_DELIMITER +
				FOR_USER_ID_IMAGE_SERVLET_PARAM + SERVER_PARAM_NAME_VAL_DELIM + ownerID + SERVLET_PARAMETERS_DELIMITER +
				FILE_ID_SERVLET_PARAM + SERVER_PARAM_NAME_VAL_DELIM + fileID + SERVLET_PARAMETERS_DELIMITER +
				IS_THUMBNAIL_SERVLET_PARAM + SERVER_PARAM_NAME_VAL_DELIM + (isThumbnail ? "1" : "0") + SERVLET_PARAMETERS_DELIMITER +
				IS_IMAGE_SERVLET_PARAM + SERVER_PARAM_NAME_VAL_DELIM + (isImageFile ? "1" : "0") );
	}
	
	/**
	 * Return the URL for the chat-room's message file.
	 * NOTE: Here we do not add dummy time servlet param because
	 * we do not update the image record in the database. I.e. when
	 * uploading a new file we create a new row and issue a new file ID.
	 * @param roomID the id of the room the chat message belongs to
	 * @param fileDescr the file descriptor of the file we want to retrieve or null if the file is not set
	 * @param isThumbnail if true then we need a thumbnail
	 * @param isMsgAtt if true then the file is already attached to the chat message
	 * @return the required URL
	 */
	public static String getChatMessageFileURL( final int roomID, final ShortFileDescriptor fileDescr,
												 final boolean isThumbnail, final boolean isMsgAtt ) {
		final int fileID;
		final String fileName = getURLAllowedFileName( fileDescr, CHAT_FILE_DEFAULT_NAME_PREFIX );
		final boolean isImageFile;
		if( fileDescr == null ) {
			fileID = ShortFileDescriptor.UNKNOWN_FILE_ID;
			isImageFile = true;
		} else {
			fileID = fileDescr.fileID;
			isImageFile = SupportedFileMimeTypes.isImageMimeType( fileDescr.mimeType ) ||
						  SupportedFileMimeTypes.isImageMimeType( SupportedFileMimeTypes.getFileMimeTypeStringByExtension( fileName ) );
		}
		
		return	URL.encode( GWT.getModuleBaseURL() +
				CHAT_MESSAGE_FILES_SERVLET_CONTEXT +
				SERVER_CONTEXT_DELIMITER + URL.encodeQueryString( fileName ) + URL_QUERY_DELIMITER +
				ROOM_ID_CHAT_FILES_SERVLET_PARAM + SERVER_PARAM_NAME_VAL_DELIM + roomID + SERVLET_PARAMETERS_DELIMITER +
				FILE_ID_CHAT_FILES_SERVLET_PARAM + SERVER_PARAM_NAME_VAL_DELIM + fileID + SERVLET_PARAMETERS_DELIMITER +
				IS_IMAGE_SERVLET_PARAM + SERVER_PARAM_NAME_VAL_DELIM + (isImageFile ? "1" : "0") + SERVLET_PARAMETERS_DELIMITER +
				IS_THUMBNAIL_SERVLET_PARAM + SERVER_PARAM_NAME_VAL_DELIM + (isThumbnail ? "1" : "0") + SERVLET_PARAMETERS_DELIMITER +
				IS_MESSAGE_ATTACHED_SERVLET_PARAM + SERVER_PARAM_NAME_VAL_DELIM + (isMsgAtt ? "1" : "0") + SERVLET_PARAMETERS_DELIMITER +
				USER_ID_SERVLET_PARAM + SERVER_PARAM_NAME_VAL_DELIM + SiteManager.getUserID() + SERVLET_PARAMETERS_DELIMITER +
				SESSION_ID_SERVLET_PARAM + SERVER_PARAM_NAME_VAL_DELIM + SiteManager.getUserSessionId() );
		//NOTE: The last parameter is only used for updating images
	}
	
	/**
	 * Allows to retrieve file attached to the forum message.
	 * Returns the relative url.
	 * @param encoder the proper encoder for the server or client side
	 * @param fileDescr the forum file descriptor
	 * @param isThumbnail if it is an image file then if true, we get a thumbnail
	 * @return the complete URL to use for the file retrieval
	 */
	public static String getRelativeForumFileURL( final EncoderInt encoder,
												  final ShortFileDescriptor fileDescr,
												  final boolean isThumbnail ) {
		return	FORUM_FILES_SERVLET_CONTEXT + SERVER_CONTEXT_DELIMITER +
				encoder.encodeURLComponent( getURLAllowedFileName( fileDescr, FORUM_FILE_DEFAULT_NAME_PREFIX ) ) + URL_QUERY_DELIMITER +
				IS_THUMBNAIL_SERVLET_PARAM + SERVER_PARAM_NAME_VAL_DELIM + (isThumbnail ? "1" : "0") + SERVLET_PARAMETERS_DELIMITER +
				FILE_ID_SERVLET_PARAM + SERVER_PARAM_NAME_VAL_DELIM + fileDescr.fileID;
	}
	
	/**
	 * Allows to retrieve file attached to the forum message.
	 * Returns the absolute URL, for the GWT site version only. 
	 * @param fileDescr the forum file descriptor
	 * @param isThumbnail if it is an image file then if true, we get a thumbnail
	 * @return the complete URL to use for the file retrieval
	 */
	public static String getForumFileURL( final ShortFileDescriptor fileDescr, final boolean isThumbnail ) {
		return	URL.encode( GWT.getModuleBaseURL() + getRelativeForumFileURL( encoder, fileDescr, isThumbnail) );
	}

	//Parameters of the user profile avatar retrieval servlet
	public static final String FOR_USER_ID_AVATAR_SERVLET_PARAM = "forUserID";
	public static final String FOR_USER_GENDER_AVATAR_SERVLET_PARAM = "gender";
	
	//COnstants that devfine path prefixes for various site images
	public static final String SITE_IMAGES_LOCATION = "images" + SERVER_CONTEXT_DELIMITER;
	//The sound and video flash and JavaScript support folder
	public static final String SITE_MEDIA_SUPPORT_LOCATION = "media" + SERVER_CONTEXT_DELIMITER;
	public static final String SMILEY_IMAGES_LOCATION = SITE_IMAGES_LOCATION + "smileys" + SERVER_CONTEXT_DELIMITER;
	public static final String ROOM_RELATED_IMAGES_LOCATION = SITE_IMAGES_LOCATION + "rooms" + SERVER_CONTEXT_DELIMITER;
	public static final String USER_RELATED_IMAGES_LOCATION = SITE_IMAGES_LOCATION + "users" + SERVER_CONTEXT_DELIMITER;
	public static final String USER_INFO_RELATED_IMAGES_LOCATION = USER_RELATED_IMAGES_LOCATION + "info" + SERVER_CONTEXT_DELIMITER;
	public static final String USER_STATS_RELATED_IMAGES_LOCATION = USER_RELATED_IMAGES_LOCATION + "statistics" + SERVER_CONTEXT_DELIMITER;
	public static final String MESSAGES_RELATED_IMAGES_LOCATION = SITE_IMAGES_LOCATION + "messages" + SERVER_CONTEXT_DELIMITER;
	public static final String FORUM_MESSAGES_VOTE_IMAGES_LOCATION = MESSAGES_RELATED_IMAGES_LOCATION + "forum_message_voting" + SERVER_CONTEXT_DELIMITER;
	public static final String USERAVATAR_RELATED_IMAGES_LOCATION = SITE_IMAGES_LOCATION + "avatars" + SERVER_CONTEXT_DELIMITER + "profile" + SERVER_CONTEXT_DELIMITER;
	public static final String BACKGROUND_IMAGES_LOCATION = SITE_IMAGES_LOCATION+"backgrounds" + SERVER_CONTEXT_DELIMITER;
	public static final String INTRODUCTION_IMAGES_LOCATION = SITE_IMAGES_LOCATION+"introduction" + SERVER_CONTEXT_DELIMITER;
	
	/**
	 * Allows to get the URL of the image rotate icon
	 * @param colored determines whether we need the image with the colors or not 
	 * @return the url of the image rotate icon
	 */
	public static String getRotateImageIconURL( final boolean colored ) {
		return SITE_IMAGES_LOCATION + "rotate" + ( colored ? "" : "_dis" ) + ".png";
	}
	
	/**
	 * Allows to get the URL of the download icon
	 * @param colored determines whether we need the image with the colors or not 
	 * @return the url of the download icon
	 */
	public static String getDownloadIconURL( final boolean colored ) {
		return SITE_IMAGES_LOCATION + "download" + ( colored ? "" : "_dis" ) + ".png";
	}
	
	/**
	 * Allows to get the URL of the help image
	 * @return the url of the help image
	 */
	public static String getHelpImageURL() {
		return SITE_IMAGES_LOCATION + "help.png";
	}
	
	/**
	 * Allows to get the absolute URL for the site introduction images location
	 * @return
	 */
	public static String getIntroductionImagesLocation() {
		return GWT.getModuleBaseURL() + INTRODUCTION_IMAGES_LOCATION;
	}
	
	/**
	 * @return the url giving the location of the background images
	 */
	public static String getBackgroundImagesLocation(){
		return GWT.getModuleBaseURL() + BACKGROUND_IMAGES_LOCATION;
	}
	
	/**
	 * @return the base context for the preset avatar images
	 */
	public static String getPresetAvatarImagesBase() {
		return GWT.getModuleBaseURL() + USERAVATAR_RELATED_IMAGES_LOCATION;
	}
	
	/**
	 * @return the enabled image of the close room button, used in the selected opened room tab title
	 */
	public static String getEnabledCloseButtonImage() {
		return GWT.getModuleBaseURL() + ROOM_RELATED_IMAGES_LOCATION + "close_on.png";
	}
	
	/**
	 * @return the disabled image of the close room button, used in the unselected
	 *         selected opened room tab title.
	 */
	public static String getDisabledCloseButtonImage() {
		return GWT.getModuleBaseURL() + ROOM_RELATED_IMAGES_LOCATION + "close_off.png";
	}
	
	/**
	 * @return the enabled image of the room info button, used in the selected
	 * opened room tab title
	 */
	public static String getEnabledRoomInfoButtonImage() {
		return GWT.getModuleBaseURL() + ROOM_RELATED_IMAGES_LOCATION + "room_info_on.png";
	}
	
	/**
	 * @return the disabled image of the room info button, used in the unselected
	 *         selected opened room tab title.
	 */
	public static String getDisabledRoomInfoButtonImage() {
		return GWT.getModuleBaseURL() + ROOM_RELATED_IMAGES_LOCATION + "room_info_off.png";
	}
	
	/**
	 * @return the enabled image of the new room message, used in the selected
	 * opened room tab title
	 */
	public static String getEnabledRoomMsgButtonImage() {
		return GWT.getModuleBaseURL() + ROOM_RELATED_IMAGES_LOCATION + "new_chat_on.png";
	}
	
	/**
	 * @return the disabled image of the new room message, used in the selected
	 * opened room tab title
	 */
	public static String getDisabledRoomMsgButtonImage() {
		return GWT.getModuleBaseURL() + ROOM_RELATED_IMAGES_LOCATION + "new_chat_off.gif";
	}
	
	/**
	 * @return the image of the rooms tree title bar in the Stack Panel
	 */
	public static String getRoomsTreeImageURL() {
		return GWT.getModuleBaseURL() + ROOM_RELATED_IMAGES_LOCATION + "rooms_tree.png";
	}
	
	/**
	 * @return the image of the closed room
	 */
	public static String getClosedRoomImageURL() {
		return GWT.getModuleBaseURL() + ROOM_RELATED_IMAGES_LOCATION + "closed_room.png";
	}
	
	/**
	 * @return the image of the open room
	 */
	public static String getOpenRoomImageURL() {
		return GWT.getModuleBaseURL() + ROOM_RELATED_IMAGES_LOCATION + "open_room.png";
	}
	
	/**
	 * @return the image for the main public room
	 */
	public static String getMainRoomImageURL() {
		return GWT.getModuleBaseURL() + ROOM_RELATED_IMAGES_LOCATION + "main_room.png";
	} 
	
	/**
	 * @return the image for the public room
	 */
	public static String getPublicRoomImageURL() {
		return GWT.getModuleBaseURL() + ROOM_RELATED_IMAGES_LOCATION + "public_room.png";
	} 

	/**
	 * @return the image for the protected room
	 */
	public static String getProtectedRoomImageURL() {
		return GWT.getModuleBaseURL() + ROOM_RELATED_IMAGES_LOCATION + "protected_room.png";
	} 

	/**
	 * @return the image for the private room
	 */
	public static String getPrivateRoomImageURL() {
		return GWT.getModuleBaseURL() + ROOM_RELATED_IMAGES_LOCATION + "private_room.png";
	} 
	
	/**
	 * @return the image for the login statistics 
	 */
	public static String getUserStatsLogInImageURL(){
		return GWT.getModuleBaseURL() + USER_STATS_RELATED_IMAGES_LOCATION + "login_stats.png";
	}
	
	/**
	 * @return the image for the logout statistics 
	 */
	public static String getUserStatsLogOutImageURL(){
		return GWT.getModuleBaseURL() + USER_STATS_RELATED_IMAGES_LOCATION + "logout_stats.png";
	}
	
	/**
	 * @return the image for the logout auto statistics 
	 */
	public static String getUserStatsLogOutAutoImageURL(){
		return GWT.getModuleBaseURL() + USER_STATS_RELATED_IMAGES_LOCATION + "logout_auto_stats.png";
	}
	
	/**
	 * @return the users tree header bar for the stack panel
	 */
	public static String getUsersTreeImageURL(){
		return GWT.getModuleBaseURL() + USER_RELATED_IMAGES_LOCATION + "users_tree.png";
	}
	
	/**
	 * @return the online status image
	 */
	public static String getUserOnlineStatusImageURL(){
		return GWT.getModuleBaseURL() + USER_RELATED_IMAGES_LOCATION + "user_status_online.png";
	}
	
	/**
	 * @return the offline status image
	 */
	public static String getUserOfflineStatusImageURL(){
		return GWT.getModuleBaseURL() + USER_RELATED_IMAGES_LOCATION + "user_status_offline.png";
	}
	
	/**
	 * @return the female gender image
	 */
	public static String getUserFemaleGenderImageURL(){
		return GWT.getModuleBaseURL() + USER_RELATED_IMAGES_LOCATION + "gender_female.png";
	}
	
	/**
	 * @return the male gender image
	 */
	public static String getUserMaleGenderImageURL(){
		return GWT.getModuleBaseURL() + USER_RELATED_IMAGES_LOCATION + "gender_male.png";
	}
	
	/**
	 * @return the female image url for the non-blocked user
	 */
	public static String getFemaleBlockOffImageIconURL() {
		return GWT.getModuleBaseURL() + USER_RELATED_IMAGES_LOCATION + "female.png";
	}
	
	/**
	 * @return the female image url for the blocked user
	 */
	public static String getFemaleBlockOnImageIconURL() {
		return GWT.getModuleBaseURL() + USER_RELATED_IMAGES_LOCATION + "female_blocked.png";
	}
	
	/**
	 * @return the hidden user image url
	 */
	public static String getHiddenUserImageIconURL() {
		return GWT.getModuleBaseURL() + USER_RELATED_IMAGES_LOCATION + "hidden_user.png";
	}
	
	/**
	 * @return the male image url for the non-blocked user
	 */
	public static String getMaleBlockOffImageIconURL() {
		return GWT.getModuleBaseURL() + USER_RELATED_IMAGES_LOCATION + "male.png";
	}
	
	/**
	 * @return the male image url for the blocked user
	 */
	public static String getMaleBlockOnImageIconURL() {
		return GWT.getModuleBaseURL() + USER_RELATED_IMAGES_LOCATION + "male_blocked.png";
	}
	
	/**
	 * @return the block user image url
	 */
	public static String getBlockUserImageIconURL() {
		return GWT.getModuleBaseURL() + USER_RELATED_IMAGES_LOCATION + "block_user.png";
	}	
	
	/**
	 * @return the un-block user image url
	 */
	public static String getUnBlockUserImageIconURL() {
		return GWT.getModuleBaseURL() + USER_RELATED_IMAGES_LOCATION + "unblock_user.png";
	}	
	
	/**
	 * @return the chat-message image url
	 */
	public static String getChatMessageImageIconURL() {
		return GWT.getModuleBaseURL() + USER_RELATED_IMAGES_LOCATION + "chat_message.png";
	}
	
	/**
	 * Allows to get the image url for enable/disable bot image 
	 * @param isBotOn true if we get the disable image, otherwise false
	 * @return the url
	 */
	public static String getBotActionLinkImageIconURL( final boolean isBotOn ) {
		return GWT.getModuleBaseURL() + USER_RELATED_IMAGES_LOCATION + "bot_" + (isBotOn? "on" : "off") + ".png";
	}
	
	/**
	 * @return the url of the image that says that the read all room-user access has expired
	 */
	public static String getReadAllExpiredImageURL( ) {
		return GWT.getModuleBaseURL() + ROOM_RELATED_IMAGES_LOCATION + "read_all_expired.png";
	}
	
	/**
	 * @return the url of the image that says that the read all room-user access is active
	 */
	public static String getReadAllActiveImageURL( ) {
		return GWT.getModuleBaseURL() + ROOM_RELATED_IMAGES_LOCATION + "read_all_active.png";
	}
	
	/**
	 * @return the url of the image that says that the read all room-user access is not on
	 */
	public static String getReadAllNotOnImageURL( ) {
		return GWT.getModuleBaseURL() + ROOM_RELATED_IMAGES_LOCATION + "read_all_not_on.png";
	}
	
	/**
	 * @return the url of the sent message image
	 */
	public static String getSendMessageImageURL( ) {
		return GWT.getModuleBaseURL() + MESSAGES_RELATED_IMAGES_LOCATION + "send_message.png";
	}
	
	/**
	 * @return the url of the "sent message is disabled" image
	 */
	public static String getDisabledSendMessageImageURL() {
		return GWT.getModuleBaseURL() + MESSAGES_RELATED_IMAGES_LOCATION + "send_message_disabled.png";
	}
	
	/**
	 * @return the url of the sent-read message image
	 */
	public static String getSentReadMessageImageURL() {
		return GWT.getModuleBaseURL() + MESSAGES_RELATED_IMAGES_LOCATION + "read_message.png";
	}
	
	/**
	 * @return the url of the sent-unread message image
	 */
	public static String getSentUnreadMessageImageURL() {
		return GWT.getModuleBaseURL() + MESSAGES_RELATED_IMAGES_LOCATION + "sent_unread_message.png";
	}
	
	/**
	 * @return the url of the received-read message image
	 */
	public static String getReceivedReadMessageImageURL( ) {
		return GWT.getModuleBaseURL() + MESSAGES_RELATED_IMAGES_LOCATION + "read_message.png";
	}
	
	/**
	 * @return the url of the received-unread message image
	 */
	public static String getReceivedUnreadMessageImageURL( ) {
		return GWT.getModuleBaseURL() + MESSAGES_RELATED_IMAGES_LOCATION + "received_unread_message.png";
	}
	
	/**
	 * @return the add chat message recipient image button
	 */
	public static String getAddChatMessageRecepientImageButtonURL() {
		return GWT.getModuleBaseURL() + MESSAGES_RELATED_IMAGES_LOCATION + "add_message_recepient.png";
	}
	
	/**
	 * @return the remove image button
	 */
	public static String getRemoveImageButtonURL() {
		return GWT.getModuleBaseURL() + SITE_IMAGES_LOCATION + "remove_image_button.png";
	}
	
	/**
	 * @return the remove image button, disabled
	 */
	public static String getRemoveImageButtonDisabledURL() {
		return GWT.getModuleBaseURL() + SITE_IMAGES_LOCATION + "remove_image_button_disabled.png";
	}
	
	/**
	 * @param isOn if true then we return the url for the "on" image, otherwise for the "off" image
	 * @return the sound notification image url
	 */
	public static String getSoundNotificationImageURL( final boolean isOn ) {
		return GWT.getModuleBaseURL() + SITE_IMAGES_LOCATION + "sound_" + ( isOn ? "on" : "off" )  + ".png";
	}
	
	/**
	 * @return the default chat message image URL
	 */
	public static String getDefaultChatMessageImageURL( ) {
		return GWT.getModuleBaseURL() + MESSAGES_RELATED_IMAGES_LOCATION + "chat_message_picture.jpg";
	}
	
	/**
	 * @return the default URL to the indicator of the chat image being unset
	 */
	public static String getChatMessageFileUnsetURL( ) {
		return GWT.getModuleBaseURL() + MESSAGES_RELATED_IMAGES_LOCATION + "chat_message_picture_unset.png";
	}
	
	/**
	 * @return the default URL to the indicator of the chat image being set
	 */
	public static String getChatMessageFileSetURL( ) {
		return GWT.getModuleBaseURL() + MESSAGES_RELATED_IMAGES_LOCATION + "chat_message_picture_set.png";
	}
	
	/**
	 * @return the default URL to the image of the smiles link
	 */
	public static String getChatMessageSmilesLinkImageURL( ) {
		return GWT.getModuleBaseURL() + MESSAGES_RELATED_IMAGES_LOCATION + "chat_message_smiles_link_image.png";
	}
	
	/**
	 * @return the forum's write here image URL
	 */
	public static String getForumWriteHereImageURL() {
		return GWT.getModuleBaseURL() + MESSAGES_RELATED_IMAGES_LOCATION + "forum_write_here.png";
	}
	
	/**
	 * @return the forum's New forum section image URL
	 */
	public static String getForumNewSectionImageURL() {
		return GWT.getModuleBaseURL() + MESSAGES_RELATED_IMAGES_LOCATION + "new_forum_section.png";
	}
	
	/**
	 * @return the forum's disabled action panel image URL
	 */
	public static String getForumDisabledActionHintImageURL() {
		return GWT.getModuleBaseURL() + MESSAGES_RELATED_IMAGES_LOCATION + "disabled_action_hint.png";
	}
	
	/**
	 * @return the forum's new topic image URL
	 */
	public static String getForumNewTopicImageURL() {
		return GWT.getModuleBaseURL() + MESSAGES_RELATED_IMAGES_LOCATION + "new_forum_topic.png";
	}
	
	private static final String MESSAGE_ACTION_IMAGE_DISABLED_SUFFIX = "_dis";
	private static final String MESSAGE_ACTION_IMAGE_EXT = ".png";
	
	/**
	 * Constructs the proper URL for the message action panel images
	 */
	private static String getMessageActionImage( final String actionImageName, final boolean isEnabled ) {
		return GWT.getModuleBaseURL() + MESSAGES_RELATED_IMAGES_LOCATION + actionImageName +
				( isEnabled ? "" : MESSAGE_ACTION_IMAGE_DISABLED_SUFFIX ) + MESSAGE_ACTION_IMAGE_EXT;
	}
	
	/**
	 * Allows to get a forum message voting image
	 * @param isGood true if this is for saying that the message is good, otherwise bad 
	 * @param isEnabled if the image should be enabled/disabled
	 * @return the url to the image
	 */
	public static String getForumMessageVotingImage(final boolean isGood, final boolean isEnabled) {
		return 	GWT.getModuleBaseURL() + FORUM_MESSAGES_VOTE_IMAGES_LOCATION + (isGood ? "" : "dis_") +
				"like" + (isEnabled ? "" : MESSAGE_ACTION_IMAGE_DISABLED_SUFFIX) + ".png";
	}
	
	//The indexes for the star images
	public static final int EMPTY_STAR_IMAGE = 0;
	public static final int HALF_STAR_IMAGE = 1;
	public static final int FULL_STAR_IMAGE = 2;
	
	/**
	 * Allows to get the star image of three types: empty star, half star, full star
	 */
	public static String getForumMessageVotingStarImage(final int index) {
		return 	GWT.getModuleBaseURL() + FORUM_MESSAGES_VOTE_IMAGES_LOCATION + "star_" + index + ".png";
	}
	
	/**
	 * Returns the URL to the enter-forum-topic image
	 * @param isEnabled true if we want the "enabled" version of the image or otherwise false
	 * @return the url to the required image
	 */
	public static String getEnterForumTopicURL( final boolean isEnabled ) {
		return getMessageActionImage( "enter_forum_topic", isEnabled );
	}
	
	/**
	 * Returns the URL to the delete-message action image url
	 * @param isEnabled true if we want the "enabled" version of the image or otherwise false
	 * @return the url to the required image
	 */
	public static String getDeleteMessageURL( final boolean isEnabled ) {
		return getMessageActionImage( "delete_message", isEnabled );
	}
	
	/**
	 * Returns the URL to the move-message action image url
	 * @param isEnabled true if we want the "enabled" version of the image or otherwise false
	 * @return the url to the required image
	 */
	public static String getMoveMessageURL( final boolean isEnabled ) {
		return getMessageActionImage( "move_forum_message", isEnabled );
	}
	
	/**
	 * Returns the URL to the approve-message action image url
	 * @param isEnabled true if we want the "enabled" version of the image or otherwise false
	 * @return the url to the required image
	 */
	public static String getApproveMessageURL( final boolean isEnabled ) {
		return getMessageActionImage( "approve_forum_message", isEnabled );
	}
	
	/**
	 * Returns the URL to the disapprove-message action image url
	 * @param isEnabled true if we want the "enabled" version of the image or otherwise false
	 * @return the url to the required image
	 */
	public static String getDisApproveMessageURL( final boolean isEnabled ) {
		return getMessageActionImage( "disapprove_forum_message", isEnabled );
	}
	
	/**
	 * Returns the URL to the edit-message action image url
	 * @param isEnabled true if we want the "enabled" version of the image or otherwise false
	 * @return the url to the required image
	 */
	public static String getEditMessageURL( final boolean isEnabled ) {
		return getMessageActionImage( "edit_message", isEnabled );
	}
	
	/**
	 * Returns the URL to the view-message topic action image url
	 * @param isEnabled true if we want the "enabled" version of the image or otherwise false
	 * @return the url to the required image
	 */
	public static String getViewMessageTopicURL( final boolean isEnabled ) {
		return getMessageActionImage( "view_topic", isEnabled );
	}
	
	/**
	 * Returns the URL to the view-message replies action image url
	 * @param isEnabled true if we want the "enabled" version of the image or otherwise false
	 * @return the url to the required image
	 */
	public static String getViewRepliesMessageURL( final boolean isEnabled ) {
		return getMessageActionImage( "view_replies", isEnabled );
	}
	
	/**
	 * Returns the URL to the reply-to-message action image url
	 * @param isEnabled true if we want the "enabled" version of the image or otherwise false
	 * @return the url to the required image
	 */
	public static String getReplyToMessageURL( final boolean isEnabled ) {
		return getMessageActionImage( "reply_to_message", isEnabled );
	}
	
	/**
	 * @return the url of the activity indicator image
	 */
	public static String getActivityImageURL( ) {
		return GWT.getModuleBaseURL() + SITE_IMAGES_LOCATION + "progress.gif";
	}
	
	/**
	 * @return the url of the error image
	 */
	public static String getErrorImageURL( ) {
		return GWT.getModuleBaseURL() + SITE_IMAGES_LOCATION + "error.png";
	}
	
	/**
	 * @return the url of the error image
	 */
	public static String getSuccessImageURL( ) {
		return GWT.getModuleBaseURL() + SITE_IMAGES_LOCATION + "success.png";
	}
	
	/**
	 * @return the url of the add friend image
	 */
	public static String getAddFriendImageURL( ) {
		return GWT.getModuleBaseURL() + USER_RELATED_IMAGES_LOCATION + "add_friend.png";
	}
	
	/**
	 * @return the url of the remove friend image
	 */
	public static String getRemoveFriendImageURL( ) {
		return GWT.getModuleBaseURL() + USER_RELATED_IMAGES_LOCATION + "remove_friend.png";
	}
	
	/**
	 * @return the url of the choose an image
	 */
	public static String getChooseImageURL( ) {
		return GWT.getModuleBaseURL() + USERAVATAR_RELATED_IMAGES_LOCATION + "choose.png";
	}
	
	/**
	 * @return the url of the upload an image
	 */
	public static String getUploadImageURL( ) {
		return GWT.getModuleBaseURL() + USERAVATAR_RELATED_IMAGES_LOCATION + "upload.png";
	}
	
	/**
	 * @return the url of the delete an image
	 */
	public static String getDeleteImageURL( ) {
		return GWT.getModuleBaseURL() + USERAVATAR_RELATED_IMAGES_LOCATION + "delete.png";
	}
	
	/**
	 * @param isLeftMessage if true returns the avatar facing right, if false then left
	 * @return the administrator's avatar for the fake messages on the site's title page
	 */
	public static String getAdministratorAvatarImageURL( final boolean isLeftMessage) {
		if( isLeftMessage ) {
			return GWT.getModuleBaseURL() + USERAVATAR_RELATED_IMAGES_LOCATION + "administrator_message_left.png";
		} else {
			return GWT.getModuleBaseURL() + USERAVATAR_RELATED_IMAGES_LOCATION + "administrator_message_right.png";
		}
	}
	
	/**
	 * @return the "user room enter" avatar image url
	 */
	public static String getUserEnterMessageAvatarImageURL( ) {
		return GWT.getModuleBaseURL() + USERAVATAR_RELATED_IMAGES_LOCATION + "user_enter_message.png";
	}
	
	/**
	 * @return the "user room leave" avatar image url
	 */
	public static String getUserLeaveMessageAvatarImageURL( ) {
		return GWT.getModuleBaseURL() + USERAVATAR_RELATED_IMAGES_LOCATION + "user_leave_message.png";
	}
	
	/**
	 * @return the "default info message" avatar image url
	 */
	public static String getDefaultInfoMessageAvatarImageURL( ) {
		return GWT.getModuleBaseURL() + USERAVATAR_RELATED_IMAGES_LOCATION + "default_info_message.png";
	}
	
	/**
	 * @return the url of the info message avatar image
	 */
	public static String getErrorMessageAvatarImageURL( ) {
		return GWT.getModuleBaseURL() + USERAVATAR_RELATED_IMAGES_LOCATION + "error_message.png";
	}

	public static final String USER_STATUS_IMAGES_BASE_URL = USER_RELATED_IMAGES_LOCATION + "status" + SERVER_CONTEXT_DELIMITER;
}
