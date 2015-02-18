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

import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.client.rpc.exceptions.SiteException;
import com.xcurechat.client.rpc.exceptions.ExceptionsSerializer;

import com.xcurechat.client.data.FileData;
import com.xcurechat.client.data.MessageFileData;
import com.xcurechat.client.data.UserData;
import com.xcurechat.client.data.UserFileData;

import com.xcurechat.server.cache.ServerDataFileCache;
import com.xcurechat.server.core.UserSessionManager;

import com.xcurechat.server.files.FileServletHelper;
import com.xcurechat.server.files.FileUploadWrapper;
import com.xcurechat.server.files.ImageProcessor;

import com.xcurechat.server.security.statistics.StatisticsSecurityManager;

import com.xcurechat.server.jdbc.ConnectionWrapper;

import com.xcurechat.server.jdbc.forum.files.DeleteLostForumFilesExecutor;
import com.xcurechat.server.jdbc.forum.files.InsertForumFileExecutor;
import com.xcurechat.server.jdbc.forum.files.SelectForumFileExecutor;

import com.xcurechat.server.utils.HTTPUtils;

/**
 * @author zapreevis
 * Manages the forum files upload and viewing
 */
public class ForumFilesManager extends HttpServlet {
	
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( ForumFilesManager.class );
	
	//The file upload limit is 10 Mb
	public static final int  MAX_UPLOAD_FILE_SIZE_MB = 20;
	public static final long MAX_UPLOAD_FILE_SIZE_BYTES = MAX_UPLOAD_FILE_SIZE_MB * 1048576;
	//Limit the maximum upload request size by the max size of
	//upload file plus 5 Kb for headers and etc
	public static final long MAX_UPLOAD_FILE_REQUEST_SIZE_BYTES = MAX_UPLOAD_FILE_SIZE_BYTES + 2048;
	
	//The cache for the chat files
	private static final ServerDataFileCache filesCache = new ServerDataFileCache( 5 * MAX_UPLOAD_FILE_SIZE_MB, 6*60, 60, "forum-files");
	
	//The length of lost forum files clean up in milliseconds
	private static final long FILE_CLEAN_UP_INTERVAL_MILLISEC = DeleteLostForumFilesExecutor.AT_LEAST_N_HOURS_OLD * 60 * 1000;
	
	//The date of the next
	private static final Object cleanUpSynchObj = new Object();
	private static long lastCleanUpTimeMillisec = ( System.currentTimeMillis() - FILE_CLEAN_UP_INTERVAL_MILLISEC );
	
	/**
	 * This method, if called periodically, once a day cleans up the
	 * uploaded forum files that are more than N hours old and have the
	 * messageID set to be the invisible root forum message. These
	 * files are clearly lost and are not attached to any message.
	 */
	public static void cleanUpTheMissedFiles() {
		synchronized( cleanUpSynchObj ) {
			//If we cleaned up files more than one day ago, clean them now
			if( System.currentTimeMillis() > ( lastCleanUpTimeMillisec + FILE_CLEAN_UP_INTERVAL_MILLISEC ) ) {
				//Clean the missed and old files
				DeleteLostForumFilesExecutor deleteLostFilesExec = new DeleteLostForumFilesExecutor();
				ConnectionWrapper<Void> deleteLostFilesConnWrap = ConnectionWrapper.createConnectionWrapper( deleteLostFilesExec );
				try {
					deleteLostFilesConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
				} catch (SiteException e) {
					logger.error("An unexpected exception while deleting lost forum files", e);
				}
				
				//Update the timer, set now as the last clean up time
				lastCleanUpTimeMillisec = System.currentTimeMillis();
			}
		}
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
			
			logger.info( "Request for uploading forum file from user "+userID );
			HttpSession httpSession = request.getSession();
			final String remoteAddr = HTTPUtils.getTrueRemoteAddr(userID, request);
			
			//Validate that we are allowed to have access to the session and
			//that the login/session validation for this IP/login name are not blocked
			StatisticsSecurityManager.validateAccess( httpSession, remoteAddr, userID, false );
			
			//NOTE: DO NOT DO SYNCHRONIZATION ON USER ID, TO PREVENT USER ACTION LOCKING
			
			//Validate user session
			UserSessionManager.validateLoginVsSessionId( httpSession, remoteAddr, userID, userSessionId );
			
			//Clean up the lost files
			cleanUpTheMissedFiles();
			
			//Process the uploaded file 
			List<FileItem> fileItems = requestWrapper.getFileItems();
			Iterator<FileItem> iter = fileItems.iterator();
			while( iter.hasNext() ){
				FileItem file = iter.next();
				if( ! file.isFormField() ) {
					logger.debug( "Form field name: " + file.getFieldName() + ", File name: " + file.getName() + 
									", Content type: " + file.getContentType() + ", Is in memory: " +
									file.isInMemory() + ", Size in bytes: " + file.getSize() );
					
					final UserFileData fileDescriptor = FileServletHelper.prepareUploadedFileData( userID, file, ImageProcessor.MAX_IMAGE_HEIGHT_768,
																								   ImageProcessor.MAX_IMAGE_WIDTH_1024,
																								   ImageProcessor.THUMBNAIL_HEIGHT,
																								   ImageProcessor.THUMBNAIL_WIDTH,
																								   ImageProcessor.DEFAULT_BACKGROUND_COLOR, "forum" );
					
					//Store the file descriptor in the database, write the file ID to the response
					InsertForumFileExecutor insertFileExec = new InsertForumFileExecutor( fileDescriptor );
					ConnectionWrapper<Void> insertFileConnWrap = ConnectionWrapper.createConnectionWrapper( insertFileExec );
					insertFileConnWrap.executeQuery( ConnectionWrapper.XCURE_CHAT_DB );
					
					response.getWriter().println( fileDescriptor.fileID );
					logger.debug( "User " + userID + " has insterted a new forum-message file " + fileDescriptor.fileName +" with id " + fileDescriptor.fileID );
					
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
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
						throws ServletException, IOException {
		//Get the file id
		int fileID = FileData.UNKNOWN_FILE_ID;
		try {
			fileID = (new Integer(request.getParameter(ServerSideAccessManager.FILE_ID_SERVLET_PARAM))).intValue();
		}catch(Exception e) {
			logger.error("Unable to get the forum-file id from the request", e);
		}
		//Get the thumbnail servlet parameter, if not present then we do not need a thumbnail
		final String isThumbStr = request.getParameter( ServerSideAccessManager.IS_THUMBNAIL_SERVLET_PARAM );
		final boolean isThumbnail = (isThumbStr == null ) ? false : isThumbStr.equals("1");
		//Get the "is to download" servlet parameter, if not present then we do not need to download the file
		final String isDownloadStr = request.getParameter( ServerSideAccessManager.IS_DOWNLOAD_FILE_SERVLET_PARAM );
		final boolean isDownload =  (isDownloadStr == null) ? false : isDownloadStr.equals("1");
		
		//Here we assume a default and non-logged-in user
		final int userID = UserData.DEFAULT_UID;
		
		Log4jInit.pushDNC( request, null, userID );
		logger.debug( "Request from user: " + userID + " for retrieving forum file: " + fileID + ", is thumbnail: " + isThumbnail );
		HttpSession httpSession = request.getSession();
		final String remoteAddr = HTTPUtils.getTrueRemoteAddr(userID, request);
		
		try{
			//Validate that the user was not blocked due to too many server connections
			//DO NOT check for the validity of the user login/session statistics (whether it is blocked or not)
			StatisticsSecurityManager.validateAccess( httpSession, remoteAddr, userID, true, false, false );
			
			//NOTE: We do not need to synchronize here on anything because we allow for the anonymous user
			//      and this is just a data retrieval method, i.e. we only do DB readings and this is it
			//NOTE: We do not validate the session here because in for forum the non-logged in users can browse the files
			
			//Try to retrieve the file from the cache
			MessageFileData fileData = (MessageFileData) filesCache.get(fileID);
			if( fileData == null ) {
				//If the file is not in the cache, allocate a new data holder
				fileData =new MessageFileData();
				//Retrieve the file from the database
				ConnectionWrapper<MessageFileData> getFileDataConnWrap = ConnectionWrapper.createConnectionWrapper( new SelectForumFileExecutor( fileID ) );
				getFileDataConnWrap.executeQuery( fileData, ConnectionWrapper.XCURE_CHAT_DB );
				//Put the file into cache
				filesCache.put( fileData );
			}
			
			byte[] data = (isThumbnail ? fileData.thumbnailData : fileData.fileData);
			if( data == null ) {
				logger.error("Unable to retrieve forum file " + fileID +", is thumbnail: " + isThumbnail );
			}
			final String properFileName = ServerSideAccessManager.getURLAllowedFileName( fileData, ServerSideAccessManager.FORUM_FILE_DEFAULT_NAME_PREFIX );
			FileServletHelper.writeOutputFile( logger, response, data, "forum-file", fileData.mimeType, properFileName, true, isDownload );
		} catch( SiteException ex) {
			response.getWriter().println( ExceptionsSerializer.serialize( ex ) );
		} finally {
			Log4jInit.cleanDNC();
		}
	}
}
