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
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import com.xcurechat.client.data.UserData;
import com.xcurechat.client.data.UserFileData;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.rpc.exceptions.UserFileUploadException;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.ExceptionsSerializer;
import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.utils.SupportedFileMimeTypes;

import com.xcurechat.server.core.UserSessionManager;
import com.xcurechat.server.files.FileServletHelper;
import com.xcurechat.server.files.FileUploadWrapper;
import com.xcurechat.server.files.ImageProcessor;


import com.xcurechat.server.jdbc.ConnectionWrapper;
import com.xcurechat.server.jdbc.images.avatars.SelectProfileAvatarExecutor;
import com.xcurechat.server.jdbc.images.avatars.InsertNewProfileAvatarExecutor;
import com.xcurechat.server.jdbc.images.avatars.UpdateProfileAvatarExecutor;

import com.xcurechat.server.security.statistics.StatisticsSecurityManager;

import com.xcurechat.server.utils.HTTPUtils;

import java.util.List;
import java.util.Iterator;

/**
 * @author zapreevis
 * This servlet allows to upload and retrieve user profile avatars
 */
public class ProfileAvatarManager extends HttpServlet {
	
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( ProfileAvatarManager.class );
	
	//The file upload limit is 200 Kb, should be enough for an avatar
	public static final long MAX_UPLOAD_IMAGE_FILE_SIZE_BYTES = 204800;
	//Limit the maximum upload request size by the max size of
	//upload file plus 1 Kb for headers and etc
	public static final long MAX_UPLOAD_IMAGE_FILE_REQUEST_SIZE_BYTES = MAX_UPLOAD_IMAGE_FILE_SIZE_BYTES + 1024;
	
	private final String PROFILE_AVATAR_IMAGE_FILE_TYPE = SupportedFileMimeTypes.JPEG_IMAGE_MIME.getMainMTSuffix();
	
	//The image data for default male and female avatars 
	private byte[] maleAvatarImageData = null;
	private byte[] femaleAvatarImageData = null;
	
	public void init() {
		logger.info("Starting to load default profile avatar images.");
		//Load default thumbnail images
		final String avatarPathPrefix =  getServletContext().getRealPath("/") + getInitParameter("relative-avatar-images-path");
		
		//Load the two default avatars, one for male another for the female.
		maleAvatarImageData = FileServletHelper.getReadFileDataInBytes( logger, avatarPathPrefix + "male.jpg" ); 
		femaleAvatarImageData = FileServletHelper.getReadFileDataInBytes( logger, avatarPathPrefix + "female.jpg" );
		
		logger.info("Loadig of default profile avatar images is complete.");
	}
	
	/**
	 * This method allows to insert the new avatar into the database, or update it if it exists
	 * @param userID the user id for which we insert/update the avatar
	 * @param fileData the avatar's image data
	 * @throws SiteException if smth goes wrong
	 */
	public static void insertUpdateAvatar( UserFileData fileData ) throws SiteException {
		//Here we try to insert the image and if we can not, i.e. the image is already there, we update it
		try {
			logger.info("Trying to insert a new avatar for user '" + fileData.ownerID + "'");
			ConnectionWrapper<Void> insertImagesConnWrap = ConnectionWrapper.createConnectionWrapper( new InsertNewProfileAvatarExecutor( fileData ) );
			insertImagesConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
		} catch ( InternalSiteException e ) {
			logger.info("The avatar exists, updating it for user '" + fileData.ownerID + "'");
			ConnectionWrapper<Void> updateImagesConnWrap = ConnectionWrapper.createConnectionWrapper( new UpdateProfileAvatarExecutor( fileData ) );
			updateImagesConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
		}
	}
	
	/**
	 * Manages the upload of the user profile images
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
						throws ServletException, IOException {
		//Make sure that the responce is of the right type
		response.setContentType("text/html");
		try{
			//Create the FileUploadWrapper, at this point we check that
			//the file and the request are not too big
			FileUploadWrapper requestWrapper = new FileUploadWrapper( request, this.getServletContext(),
									MAX_UPLOAD_IMAGE_FILE_SIZE_BYTES, MAX_UPLOAD_IMAGE_FILE_REQUEST_SIZE_BYTES );
			
			//Validate that the user login-sessionId match
			final String userIDStr = requestWrapper.getParameter(ServerSideAccessManager.USER_ID_SERVLET_PARAM);
			final int userID = (new Integer(userIDStr)).intValue(); 
			final String userSessionId = requestWrapper.getParameter(ServerSideAccessManager.SESSION_ID_SERVLET_PARAM);
			Log4jInit.pushDNC( requestWrapper, userSessionId, userID );
			
			logger.info( "Request for uploading an avatar for login="+userID );
			HttpSession httpSession = request.getSession();
			final String remoteAddr = HTTPUtils.getTrueRemoteAddr(userID, request);
			
			//Validate that we are allowed to have access to the session and
			//that the login/session validation for this IP/login name are not blocked
			StatisticsSecurityManager.validateAccess( httpSession, remoteAddr, userID, false );
			
			//NOTE: DO NOT DO SYNCHRONIZATION ON USER ID, TO PREVENT USER ACTION LOCKING
			
			//Validate user session
			UserSessionManager.validateLoginVsSessionId( httpSession, remoteAddr, userID, userSessionId );
			
			//Process the uploaded file 
			List<FileItem> fileItems = requestWrapper.getFileItems();
			Iterator<FileItem> iter = fileItems.iterator();
			while( iter.hasNext() ){
				FileItem file = iter.next();
				if( ! file.isFormField() ){
					logger.debug( "Form field name: " + file.getFieldName() + ", File name: " + file.getName() + 
									", Content type: " + file.getContentType() + ", Is in memory: " +
									file.isInMemory() + ", Size in bytes: " + file.getSize() );
					
					//If we have a proper mime format for the uploaded file, then we need to
					//resize the image, create a thumbnail for it and then put them into the DB
					final UserFileData fileData;
					if( SupportedFileMimeTypes.isImageMimeType( file.getContentType() ) ) {
						try{
							fileData = FileServletHelper.prepareUploadedFileData( userID, file, ImageProcessor.AVATAR_HEIGHT,
																				  ImageProcessor.AVATAR_WIDTH,
																				  ImageProcessor.AVATAR_HEIGHT,
																				  ImageProcessor.AVATAR_WIDTH,
																				  ImageProcessor.DEFAULT_BACKGROUND_COLOR,
																				  "avatar", false, false );
							
						} catch ( IOException ex ){
							logger.error("Unable to resize user avatar image", ex);
							throw new UserFileUploadException( UserFileUploadException.UNSUPPORTED_UPLOAD_FILE_FORMAT_ERR );
						} catch ( IllegalArgumentException ex) {
							logger.error("Unable to resize user avatar image", ex);
							throw new UserFileUploadException( UserFileUploadException.UNSUPPORTED_UPLOAD_FILE_FORMAT_ERR );
						}
					} else{
						throw new UserFileUploadException( UserFileUploadException.UNSUPPORTED_UPLOAD_FILE_FORMAT_ERR );
					}
					
					//Store files in the database, first check if the (thumbnail) image already exist
					insertUpdateAvatar( fileData );
					
					//We are not processing more than one file!
					break;
				}
			}
		}catch( FileUploadException ex ) {
			FileServletHelper.handleFileUploadExceptions( logger, ex, response, MAX_UPLOAD_IMAGE_FILE_SIZE_BYTES );
		} catch( SiteException ex) {
			response.getWriter().println( ExceptionsSerializer.serialize( ex ) );
		} finally {
			Log4jInit.cleanDNC();
			response.flushBuffer();
		}
	}
	
	/**
	 * Manages the retrieval of the user profile images
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
						throws ServletException, IOException {
		//Retrieve the user login, this is the user from which we need the image
		final String forUserIDStr = request.getParameter(ServerSideAccessManager.FOR_USER_ID_AVATAR_SERVLET_PARAM);
		final int forUserID = (new Integer(forUserIDStr)).intValue(); 
		
		//Check if this is a male of female avatar that is needed
		final String isMaleStr = request.getParameter(ServerSideAccessManager.FOR_USER_GENDER_AVATAR_SERVLET_PARAM);
		final boolean isMale = (isMaleStr == null) ? true : isMaleStr.equals("1");
		
		//Here we assume a default and non-logged-in user
		final int userID = UserData.DEFAULT_UID;
		
		Log4jInit.pushDNC( request, null, userID );
		logger.debug( "Request from user="+userID+" for retrieving an avatar of user="+forUserID+": isMale="+isMale );
		HttpSession httpSession = request.getSession();
		final String remoteAddr = HTTPUtils.getTrueRemoteAddr(userID, request);
		
		try{
			//Validate that the user was not blocked due to too many server connections
			//DO NOT check for the validity of the user login/session statistics (whether it is bloked or not)
			StatisticsSecurityManager.validateAccess( httpSession, remoteAddr, userID, true, false, false );
			
			//NOTE: We do not need to synchronize here on anything because we allow for the anonimus user
			//      and this is just a data retrieval mehtod, i.e. we only do DB readings and this is it
			//NOTE: We do not validate the session here because in for forum the non-logged inusers can browse the avatars
			
			//Try to retrieve the image either from the database or a default one from here
			UserFileData fileData = new UserFileData();
			ConnectionWrapper<UserFileData> getUserAvatarConnWrap = ConnectionWrapper.createConnectionWrapper( new SelectProfileAvatarExecutor( forUserID ) );
			getUserAvatarConnWrap.executeQuery( fileData, ConnectionWrapper.XCURE_CHAT_DB );
			if( fileData.fileData == null ) {
				fileData.fileData = ( isMale ? maleAvatarImageData : femaleAvatarImageData );
				fileData.mimeType = PROFILE_AVATAR_IMAGE_FILE_TYPE; 
				fileData.ownerID = userID;
			}
			
			FileServletHelper.writeOutputFile( logger, response, fileData.fileData, "user-avatar",
												fileData.mimeType, null, true, false );
		} catch( SiteException ex) {
			response.getWriter().println( ExceptionsSerializer.serialize( ex ) );
		} finally {
			Log4jInit.cleanDNC();
		}
	}
}
