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

import com.xcurechat.client.data.FileData;
import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.data.UserFileData;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.ExceptionsSerializer;
import com.xcurechat.client.utils.SupportedFileMimeTypes;

import com.xcurechat.server.cache.ServerDataFileCache;
import com.xcurechat.server.cache.Top10UserDataCache;
import com.xcurechat.server.core.SynchFactory;
import com.xcurechat.server.core.UserSessionManager;
import com.xcurechat.server.files.FileServletHelper;
import com.xcurechat.server.files.FileUploadWrapper;
import com.xcurechat.server.files.ImageProcessor;

import com.xcurechat.server.jdbc.ConnectionWrapper;
import com.xcurechat.server.jdbc.images.SelectProfileFileExecutor;
import com.xcurechat.server.jdbc.images.InsertNewProfileFileExecutor;
import com.xcurechat.server.security.statistics.StatisticsSecurityManager;
import com.xcurechat.server.utils.HTTPUtils;

import java.util.List;
import java.util.Iterator;

/**
 * @author zapreevis
 * This servlet is used to upload and retrieve the user-profile files
 */
public class ProfileFilesManager extends HttpServlet {
	
	//Get the synchronization factory for users
	private static final SynchFactory userSynchFactory = SynchFactory.getSynchFactory( SynchFactory.USER_FACTORY_NAME );
	
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( ProfileFilesManager.class );
	
	//The file upload limit is 10 Mb
	public static final int  MAX_UPLOAD_FILE_SIZE_MB = 10;
	public static final long MAX_UPLOAD_FILE_SIZE_BYTES = MAX_UPLOAD_FILE_SIZE_MB * 1048576;
	//Limit the maximum upload request size by the max
	//size of upload file plus 2 Kb for headers and etc
	public static final long MAX_UPLOAD_FILE_REQUEST_SIZE_BYTES = MAX_UPLOAD_FILE_SIZE_BYTES + 2048; 
	
	//The cache for the chat files
	public static final ServerDataFileCache filesCache = new ServerDataFileCache( 5 * MAX_UPLOAD_FILE_SIZE_MB, 6*60, 60, "profile-files");
	
	//This is the data for the default image
	private byte[] defaultImageData = null;
	//This is the data for the default non-image file thumbnail
	private byte[] defaultVideoThumb = null;
	
	public void init() {
		//Load default thumbnail image
		logger.info("Starting to load default profile image thumbnails.");
		String imagePath =  getServletContext().getRealPath("/") + getInitParameter("relative-images-path");
		defaultImageData = FileServletHelper.getReadFileDataInBytes( logger, imagePath + "default_image.jpg" ); 
		defaultVideoThumb = FileServletHelper.getReadFileDataInBytes( logger, imagePath + "default_non_image.jpg" );
		logger.info("Loadig of default profile image thumbnails is complete.");
	}
	
	/**
	 * Manages the upload of the user profile images
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
						throws ServletException, IOException {
		//Make sure that the response is of the right type
		response.setContentType("text/html");
		try{
			//Create the FileUploadWrapper, at this point we check that
			//the file and the request are not too big
			FileUploadWrapper requestWrapper = new FileUploadWrapper( request, this.getServletContext(),
																	  MAX_UPLOAD_FILE_SIZE_BYTES,
																	  MAX_UPLOAD_FILE_REQUEST_SIZE_BYTES );
			
			//Validate that the user login-sessionId match
			final String userIDStr = requestWrapper.getParameter( ServerSideAccessManager.USER_ID_SERVLET_PARAM );
			final int userID = (new Integer(userIDStr)).intValue(); 
			final String userSessionId = requestWrapper.getParameter( ServerSideAccessManager.SESSION_ID_SERVLET_PARAM );
			Log4jInit.pushDNC( requestWrapper, userSessionId, userID );
			
			logger.info( "Request for uploading profile file from user "+userID );
			HttpSession httpSession = request.getSession();
			final String remoteAddr = HTTPUtils.getTrueRemoteAddr(userID, request);
			
			//Validate that we are allowed to have access to the session and
			//that the login/session validation for this IP/login name are not blocked
			StatisticsSecurityManager.validateAccess( httpSession, remoteAddr, userID, false );
			
			//NOTE: DO NOT DO SYNCHRONIZATION ON USER ID, IN THE BEGINNING, TO PREVENT USER ACTION LOCKING
			
			//Validate user session
			UserSessionManager.validateLoginVsSessionId( httpSession, remoteAddr, userID, userSessionId );
			
			//Process the uploaded file 
			List<FileItem> fileItems = requestWrapper.getFileItems();
			Iterator<FileItem> iter = fileItems.iterator();
			while( iter.hasNext() ){
				FileItem file = iter.next();
				if( ! file.isFormField() ) {
					logger.debug( "Form field name: " + file.getFieldName() + ", File name: " + file.getName() + 
									", Content type: " + file.getContentType() + ", Is in memory: " +
									file.isInMemory() + ", Size in bytes: " + file.getSize() );
					
					final UserFileData fileDescriptor = FileServletHelper.prepareUploadedFileData( userID, file, UserData.MAX_USER_PROFILE_IMAGE_PIXEL_HEIGHT,
																								   UserData.MAX_USER_PROFILE_IMAGE_PIXEL_WIDTH,
																								   ImageProcessor.THUMBNAIL_HEIGHT,
																								   ImageProcessor.THUMBNAIL_WIDTH,
																								   ImageProcessor.DEFAULT_BACKGROUND_COLOR, "profile" );
					
					//Here we synchronize on the user ID to prevent the collisions
					Object synchObj = userSynchFactory.getSynchObject( userID );
					try {
						synchronized( synchObj ) {
							
							//First check on the number of files in the user profile
							UserData userData = UserSessionManager.getUserDataObject(userID);
							if( userData != null ) {
								if( userData.getNumberOfFiles() >= UserData.MAXIMUM_NUMBER_OF_FILES ) {
									throw new InternalSiteException( InternalSiteException.INSUFFICIENT_ACCESS_RIGHTS_ERROR );
								}
							}
							
							//Store the file descriptor in the database, this will set the file's ID
							ConnectionWrapper<Void> insertImagesConnWrap = ConnectionWrapper.createConnectionWrapper( new InsertNewProfileFileExecutor( fileDescriptor ) );
							insertImagesConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
							
							//Update the user profile with the new file descriptor
							UserSessionManager.addUserProfileFile(userID, ((ShortFileDescriptor) fileDescriptor).clone());
							
							//Flush the TOP10 user files cache in order to update the cached data
							Top10UserDataCache.getInstance().flushUserFilesCachedData();
							
							//Write the uploaded file ID to the response
							response.getWriter().println( fileDescriptor.fileID );
							logger.debug( "User " + userID + " has insterted a new profile-message file " + fileDescriptor.fileName +" with id " + fileDescriptor.fileID );
						}
					} finally {
						userSynchFactory.releaseSynchObject( userID );
					}
					
					//We are not processing more than one file!
					break;
				}
			}
		}catch( FileUploadException ex ) {
			FileServletHelper.handleFileUploadExceptions( logger, ex, response, MAX_UPLOAD_FILE_SIZE_BYTES );
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
		//Get the file id
		int fileID = FileData.UNKNOWN_FILE_ID;
		try {
			fileID = (new Integer(request.getParameter(ServerSideAccessManager.FILE_ID_SERVLET_PARAM))).intValue();
		}catch(Exception e) {
			logger.error("Unable to get the profile-file id from the request", e);
		}
		//Check if what we need is a thumbnail image
		final String thumbStr = request.getParameter(ServerSideAccessManager.IS_THUMBNAIL_SERVLET_PARAM);
		final boolean isThumbnail = (thumbStr == null) ? true : thumbStr.equals("1");
		//Check if what we need is an image file
		final String isImageFileStr = request.getParameter( ServerSideAccessManager.IS_IMAGE_SERVLET_PARAM );
		final boolean isImageFile = (isImageFileStr == null) ? true : isImageFileStr.equals("1");
		//Retrieve the user login, this is the user from which we need the image
		final String forUserIDStr = request.getParameter(ServerSideAccessManager.FOR_USER_ID_IMAGE_SERVLET_PARAM);
		final int forUserID = forUserIDStr == null ? ShortUserData.UNKNOWN_UID : (new Integer(forUserIDStr)).intValue();
		//Get the "is to download" servlet parameter, if not present then we do not need to download the file
		final String isDownloadStr = request.getParameter( ServerSideAccessManager.IS_DOWNLOAD_FILE_SERVLET_PARAM );
		final boolean isDownload =  (isDownloadStr == null) ? false : isDownloadStr.equals("1");
		
		//Here we assume a default and non-logged-in user
		final int userID = UserData.DEFAULT_UID;
		
		Log4jInit.pushDNC( request, null, userID );
		logger.debug( "Request from user="+userID+" for retrieving an image of user="+forUserID+": thumbnail="+isThumbnail+", fileID="+fileID );
		HttpSession httpSession = request.getSession();
		final String remoteAddr = HTTPUtils.getTrueRemoteAddr(userID, request);
		
		try{
			//Validate that the user was not blocked due to too many server connections
			//DO NOT check for the validity of the user login/session statistics (whether it is blocked or not)
			StatisticsSecurityManager.validateAccess( httpSession, remoteAddr, userID, true, false, false );
			
			//NOTE: We do not need to synchronize here on anything because we allow for the anonymous user
			//      and this is just a data retrieval method, i.e. we only do DB readings and this is it
			//NOTE: We do not validate the session here because in for forum the non-logged in users can browse the files
			
			//Try to retrieve the file either from the database or a default one from here
			byte[] data; String mimeType; String fileName;
			//If we are retrieving an image file or we are retrieving a file but not its thumbnail then we search the database
			if( isImageFile || !isThumbnail ) {
				//Try to retrieve the image either from the database or a default one from here
				UserFileData fileData = (UserFileData) filesCache.get(fileID);
				if( fileData == null ) {
					//If the file is not in the cache, allocate a new data holder
					fileData = new UserFileData();
					//Retrieve the file from the database
					ConnectionWrapper<UserFileData> registerUserConnWrap = ConnectionWrapper.createConnectionWrapper( new SelectProfileFileExecutor( forUserID, fileID ) );
					registerUserConnWrap.executeQuery( fileData, ConnectionWrapper.XCURE_CHAT_DB );
					//Put the file into cache
					filesCache.put( fileData );
				}
				//Set the initial data and mime type
				mimeType = fileData.mimeType;
				fileName = ServerSideAccessManager.getURLAllowedFileName( fileData, ServerSideAccessManager.PROFILE_FILE_DEFAULT_NAME_PREFIX );
				data = ( isThumbnail ) ? fileData.thumbnailData : fileData.fileData;
			} else {
				//If the opposite is true i.e. ( !isImageFile && isThumbnail) then there is no thumbnail for such file 
				//and we should load the default one, i.e. the database search is not actually needed for this case
				data = defaultVideoThumb;
				mimeType = SupportedFileMimeTypes.JPEG_IMAGE_MIME.getMainMimeType();
				fileName = ShortFileDescriptor.UNKNOWN_FILE_NAME;
			}
			
			//If the data is null then set the default image data
			if( data == null ) {
				data = ( isThumbnail && SupportedFileMimeTypes.isPlayableMimeType( mimeType ) ) ? defaultVideoThumb : defaultImageData;
				mimeType = SupportedFileMimeTypes.JPEG_IMAGE_MIME.getMainMimeType();
				fileName = ShortFileDescriptor.UNKNOWN_FILE_NAME;
				logger.warn("Unable to retrieve profile file " + fileID +", is thumbnail: " + isThumbnail + " for user " + forUserID );
			}
			
			FileServletHelper.writeOutputFile( logger, response, data, "user-profile", mimeType, fileName, true, isDownload);
		} catch( SiteException ex) {
			response.getWriter().println( ExceptionsSerializer.serialize( ex ) );
		} finally {
			Log4jInit.cleanDNC();
		}
	}
}
