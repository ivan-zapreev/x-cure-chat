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

import java.util.List;
import java.util.Iterator;

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
import com.xcurechat.client.data.MessageFileData;
import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.client.data.UserFileData;
import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.ExceptionsSerializer;
import com.xcurechat.client.utils.SupportedFileMimeTypes;

import com.xcurechat.server.cache.ServerDataFileCache;
import com.xcurechat.server.core.ChatRoomsManager;
import com.xcurechat.server.core.UserSessionManager;

import com.xcurechat.server.files.FileServletHelper;
import com.xcurechat.server.files.FileUploadWrapper;
import com.xcurechat.server.files.ImageProcessor;


import com.xcurechat.server.jdbc.ConnectionWrapper;
import com.xcurechat.server.jdbc.chat.images.InsertChatFileExecutor;
import com.xcurechat.server.jdbc.chat.images.RemoveOtherFilesWithMD5Executor;

import com.xcurechat.server.security.statistics.StatisticsSecurityManager;

import com.xcurechat.server.utils.HTTPUtils;
import com.xcurechat.server.utils.MD5;

public class ChatFilesManager extends HttpServlet {
	
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( ChatFilesManager.class );
	
	//The file upload limit is 10 Mb
	public static final int  MAX_UPLOAD_FILE_SIZE_MB = 10;
	public static final long MAX_UPLOAD_FILE_SIZE_BYTES = MAX_UPLOAD_FILE_SIZE_MB * 1048576;
	//Limit the maximum upload request size by the max size of
	//upload file plus 2 Kb for headers and etc
	public static final long MAX_UPLOAD_FILE_REQUEST_SIZE_BYTES = MAX_UPLOAD_FILE_SIZE_BYTES + 2048;
	
	//The cache for the chat files
	private static final ServerDataFileCache filesCache = new ServerDataFileCache( 5 * MAX_UPLOAD_FILE_SIZE_MB, 20, 1, "chat-files");
	
	//The default chat message image stored as a byte array 
	private byte[] defaultImageThumb = null;
	
	//The default chat message video stored as a byte array 
	private byte[] defaultVideoThumb = null;
	
	public void init() {
		//Load default thumbnail images
		logger.info("Starting to load default chat message's image thumbnail.");
		String imagePath =  getServletContext().getRealPath("/") + getInitParameter("relative-images-path");
		defaultImageThumb = FileServletHelper.getReadFileDataInBytes( logger, imagePath + "chat_message_picture.jpg" );
		defaultVideoThumb = FileServletHelper.getReadFileDataInBytes( logger, imagePath + "chat_message_video.jpg" );
		logger.info("Loadig of default profile image thumbnails is complete.");
	}
	
	/**
	 * Manages the upload of the chat message files
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
						throws ServletException, IOException {
		//Make sure that the response is of the right type
		response.setContentType("text/html");
		try{
			//Create the FileUploadWrapper, at this point we check that
			//the file and the request are not too big
			FileUploadWrapper requestWrapper = new FileUploadWrapper( request, this.getServletContext(),
									MAX_UPLOAD_FILE_SIZE_BYTES, MAX_UPLOAD_FILE_REQUEST_SIZE_BYTES );
			
			//Validate that the user login-sessionId match
			final int userID = Integer.parseInt( requestWrapper.getParameter(ServerSideAccessManager.USER_ID_SERVLET_PARAM) ); 
			final String userSessionId = requestWrapper.getParameter(ServerSideAccessManager.SESSION_ID_SERVLET_PARAM);
			final int roomID = Integer.parseInt( requestWrapper.getParameter(ServerSideAccessManager.ROOM_ID_CHAT_FILES_SERVLET_PARAM) ); 
			Log4jInit.pushDNC( requestWrapper, userSessionId, userID );
			
			logger.debug( "Request for uploading a chat message file by user " + userID + ", room " + roomID );
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
					
					final UserFileData fileData = FileServletHelper.prepareUploadedFileData( userID, file, ImageProcessor.MAX_IMAGE_HEIGHT_768,
																							 ImageProcessor.MAX_IMAGE_WIDTH_1024,
																							 ImageProcessor.THUMBNAIL_HEIGHT,
																							 ImageProcessor.THUMBNAIL_WIDTH,
																							 ImageProcessor.DEFAULT_BACKGROUND_COLOR, "chat" );
					
					//First clean up the old chat room messages and images
					ChatRoomsManager.getInstance().cleanUpOldChatMessages();
					
					//Store files in the database
					logger.debug("Inserting a new file for a chat message by user " + userID + ", room " + roomID );
					
					//Get the MD5 sum for the privided data
					final String md5Sum = MD5.getMD5( fileData.fileData );
					//Insert the file data
					InsertChatFileExecutor insertFileExec = new InsertChatFileExecutor( roomID, fileData, md5Sum );
					ConnectionWrapper<Void> insertFileConnWrap = ConnectionWrapper.createConnectionWrapper( insertFileExec );
					insertFileConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
					
					//Remove all the other files with the same MD5 sum, if any, this is how we optimize files storage
					//NOTE: This is not 100% safe because we delete files all of such files i.e. if some one just sent
					//a file with the same MD5 sum then it will be deleted, but it will probably remain in the cache
					//for a while; 2. we delete the old files that are already owned by the bots and thus these files
					//disappear from the files they can see for some time.
					RemoveOtherFilesWithMD5Executor removeOldMd5Files = new RemoveOtherFilesWithMD5Executor( md5Sum, fileData.fileID );
					ConnectionWrapper<Void> removeOldMd5FilesConnWrap = ConnectionWrapper.createConnectionWrapper( removeOldMd5Files );
					removeOldMd5FilesConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
					
					//Write the message ID to the response
					response.getWriter().print( fileData.fileID );
					//Add the file to cache
					filesCache.put( fileData );
					
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
		//Check if what we need is a thumbnail
		final String thumbStr = request.getParameter(ServerSideAccessManager.IS_THUMBNAIL_SERVLET_PARAM);
		final boolean isThumbnail = (thumbStr == null) ? true : thumbStr.equals("1");
		//Check if what we need is an image file
		final String isImageFileStr = request.getParameter( ServerSideAccessManager.IS_IMAGE_SERVLET_PARAM );
		final boolean isImageFile = (isImageFileStr == null) ? true : isImageFileStr.equals("1");
		
		//Get the is to download servlet parameter, if not present then we do not need to download the file
		final String isDownloadStr = request.getParameter( ServerSideAccessManager.IS_DOWNLOAD_FILE_SERVLET_PARAM );
		final boolean isDownload = (isDownloadStr == null) ? false : isDownloadStr.equals("1");
		
		//Checks if the requested file is already attached to the sent chat message
		//or it is the file previewing that we do when attaching the chat message
		final String isMsgAttStr = request.getParameter(ServerSideAccessManager.IS_MESSAGE_ATTACHED_SERVLET_PARAM);
		//If the parameter is not present then we do a simple search for the file with the given ID
		//This is why we assign false to isMsgAtt when isMsgAttStr == null
		final boolean isMsgAtt = (isMsgAttStr == null) ? false : isMsgAttStr.equals("1");
		
		//Retrieve the file ID and room ID
		final int fileID = Integer.parseInt(request.getParameter(ServerSideAccessManager.FILE_ID_CHAT_FILES_SERVLET_PARAM) ); 
		final int roomID = Integer.parseInt(request.getParameter(ServerSideAccessManager.ROOM_ID_CHAT_FILES_SERVLET_PARAM) ); 

		final int userID = Integer.parseInt( request.getParameter(ServerSideAccessManager.USER_ID_SERVLET_PARAM) ); 
		final String userSessionId = request.getParameter(ServerSideAccessManager.SESSION_ID_SERVLET_PARAM);
		
		Log4jInit.pushDNC( request, userSessionId, userID );
		logger.debug( "Request from user "+userID+" for retrieving a file "+fileID+": thumbnail="+isThumbnail+", for room "+roomID );
		HttpSession httpSession = request.getSession();
		final String remoteAddr = HTTPUtils.getTrueRemoteAddr(userID, request);
		
		try{
			//Validate that we are allowed to have access to the session and
			//that the login/session validation for this IP/login name are not blocked
			StatisticsSecurityManager.validateAccess( httpSession, remoteAddr, userID, false );
			
			//NOTE: DO NOT DO SYNCHRONIZATION ON USER ID, TO PREVENT USER ACTION LOCKING
			
			//Validate user session
			UserSessionManager.validateLoginVsSessionId( httpSession, remoteAddr, userID, userSessionId );
			
			//Try to retrieve the file either from the database or a default one from here
			FileData fileDescriptor = new MessageFileData();
			byte[] data; String mimeType; String fileName;
			//If we are retrieving an image file or we are retrieving a file but not its thumbnail then we search the database
			if( isImageFile || !isThumbnail ) {
				UserFileData fileData = (UserFileData) filesCache.get( fileID );
				if( fileData != null ) {
					//The file is inside the cache
					if( ChatRoomsManager.getInstance().canGetChatRoomMessageFile(userID, roomID, isMsgAtt, fileData) ) {
						//If the user can view the file then we are done
						fileDescriptor = fileData;
					} else {
						//NOTE: Here we do nothing because if the user is not allowed to view the file then we return a dummy file data
					}
				} else {
					//If the file was not cached, then retrieve it from the DB
					fileDescriptor = ChatRoomsManager.getInstance().getChatRoomMessageFile( userID, roomID, fileID, isThumbnail, isMsgAtt );
					//NOTE: After the file was retrieved we do not place it into cache because this is most likely 
					//to be a retrieval of the old file by a used who was late on viewing this file on time.
				}
				
				//Set the initial data and mime type
				mimeType = fileDescriptor.mimeType;
				fileName = ServerSideAccessManager.getURLAllowedFileName( fileDescriptor, ServerSideAccessManager.CHAT_FILE_DEFAULT_NAME_PREFIX );
				data = ( isThumbnail ) ? fileDescriptor.thumbnailData : fileDescriptor.fileData;
			} else {
				//If the opposite is true i.e. ( !isImageFile && isThumbnail) then there is no thumbnail for such file 
				//and we should load the default one, i.e. the database search is not actually needed for this case
				data = defaultVideoThumb;
				mimeType = SupportedFileMimeTypes.JPEG_IMAGE_MIME.getMainMimeType();
				fileName = ShortFileDescriptor.UNKNOWN_FILE_NAME;
			}
			
			//If the data is null then set the default image data
			if( data == null ) {
				data = ( isThumbnail && SupportedFileMimeTypes.isPlayableMimeType( fileDescriptor.mimeType ) ) ? defaultVideoThumb : defaultImageThumb;
				mimeType = SupportedFileMimeTypes.JPEG_IMAGE_MIME.getMainMimeType();
				fileName = ShortFileDescriptor.UNKNOWN_FILE_NAME;
				logger.warn("Unable to retrieve chat file " + fileID +", is thumbnail: " + isThumbnail );
			}
			
			FileServletHelper.writeOutputFile( logger, response, data, "chat-file", mimeType, fileName, true, isDownload );
		} catch( SiteException ex) {
			response.getWriter().println( ExceptionsSerializer.serialize( ex ) );
		} finally {
			Log4jInit.cleanDNC();
		}
	}
}
