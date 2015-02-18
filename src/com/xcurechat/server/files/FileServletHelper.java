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
 * The file processing package.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.server.files;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadBase.IOFileUploadException;
import org.apache.commons.fileupload.FileUploadBase.InvalidContentTypeException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.log4j.Logger;

import com.xcurechat.client.data.ShortFileDescriptor;
import com.xcurechat.client.data.UserFileData;
import com.xcurechat.client.rpc.exceptions.ExceptionsSerializer;
import com.xcurechat.client.rpc.exceptions.InternalSiteException;
import com.xcurechat.client.rpc.exceptions.UserFileUploadException;
import com.xcurechat.client.utils.SupportedFileMimeTypes;

/**
 * @author zapreevis
 * This class contains helper methods for the file upload/load servlets
 */
public class FileServletHelper {
	
	//The type of encoding to use for the url component encoding
	public static final String ENCODING_TYPE = "UTF-8";
	
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( FileServletHelper.class );
	
	/**
	 * This method allows to load a file provided by its absolute file
	 * name and then returned its byte data. 
	 * @param absoluteImageFileName the image file absolute name
	 * @return the file data in bytes
	 */
	public static byte[] getReadFileDataInBytes( Logger logger, final String absoluteImageFileName ) {
		//Just in case we return a non empty byte array.
		byte[] result = null;
		try {
			File file = new File( absoluteImageFileName );
			InputStream is = new FileInputStream( file );
			// Get the size of the file
			final long length = file.length();
		    
			// Create the byte array to hold the data
			result = new byte[(int)length];
		    
			// Read in the bytes
			int offset = 0; int numRead = 0;
			while ( ( offset < result.length ) && 
					( numRead=is.read(result, offset, result.length-offset)) >= 0 ) {
				offset += numRead;
			}
		    
			// Ensure all the bytes have been read in
			if ( offset < result.length) {
				throw new IOException("Could not completely read file "+file.getName());
			}
		    
			// Close the input stream and return bytes
			is.close();
		} catch (Exception e){
			logger.error( "Can not load image file: " + absoluteImageFileName, e );
		}
		return result;
	}

	/**
	 * Allows to handle the file upload exceptions and to write them into the output
	 * stream so that the client will be able to deserelize them and to show to the user.
	 * @param logger the logger object
	 * @param ex the exception that has happened
	 * @param response the servlet responce
	 * @param max_img_size_bytes the mazimum size dor the uploaded image
	 * @throws IOException if smth bad happens
	 */
	public static void handleFileUploadExceptions(Logger logger, FileUploadException ex, HttpServletResponse response,
													final long max_img_size_bytes ) throws IOException {
		String errorCode = "";
		if( ex instanceof IOFileUploadException ) {
			//Thrown to indicate an IOException
			logger.error( "An IOException exception while user-file upload." , ex);
			errorCode += ExceptionsSerializer.serialize( new InternalSiteException(InternalSiteException.IO_FILE_UPLOAD_EXCEPTION_ERR ) );
		} else if( ex instanceof InvalidContentTypeException ) {
			//Thrown to indicate that the request is not a multipart request. 
			logger.error( "An incorrect request while user-file upload." , ex);
			errorCode += ExceptionsSerializer.serialize( new InternalSiteException(InternalSiteException.INCORRECT_FILE_UPLOAD_REQUEST_EXCEPTION_ERR ) );
		} else if( ex instanceof FileSizeLimitExceededException ) {
			//Thrown to indicate that A files size
			//exceeds the configured maximum.
			logger.warn( "File size exceeded while user-file upload" );
			errorCode += ExceptionsSerializer.serialize( new UserFileUploadException( UserFileUploadException.FILE_IS_TOO_LARGE_ERR ) );
		} else if( ex instanceof SizeLimitExceededException ){
			//Thrown to indicate that the request size
			//exceeds the configured maximum.
			logger.warn( "Request size exceeded while user-file upload" );
			UserFileUploadException nex = new UserFileUploadException( UserFileUploadException.FILE_IS_TOO_LARGE_ERR );
			nex.setMaxUploadFileSize( max_img_size_bytes );
			errorCode += ExceptionsSerializer.serialize( nex );
		} else {
			//Some unknown exceptions, there should not be any else left, but just in case ...
			logger.error( "An unknown exception while user-file upload", ex);
			errorCode += ExceptionsSerializer.serialize( new InternalSiteException(InternalSiteException.UNKNOWN_INTERNAL_SITE_EXCEPTION_ERR ) );
		}
		response.getWriter().println( errorCode );
	}

	/**
	 * This method, for the provided file data, tries to write it into the servlet response
	 * @param logger the logging object
	 * @param response the servlet response 
	 * @param fileData the file data
	 * @param fileKindDesc the kind of the file that could not be handled, just some textual description needed for logging
	 * @param mimeType the mime type of the file
	 * @param fileName the file name
	 * @param doCatch if true then we mark the content as if we want to cache it.
	 * @param isDownload true if one wants to download this file as an attachment
	 * @throws ServletException is smth bad happens
	 * @throws IOException is smth bad happens
	 */
	public static void writeOutputFile(Logger logger, HttpServletResponse response,
										final byte[] fileData, final String fileKindDesc,
										final String mimeType, final String fileName,
										final boolean doCache, final boolean isDownload) throws ServletException, IOException {
		if( fileData != null ) {
			//Either do no caching or cache the file for a week
			response.setHeader( "Cache-Control", doCache ? "max-age=604800, must-revalidate" : "no-store" );
			if( ! doCache ) { 
				//Set no caching
				response.setHeader( "Pragma", "no-cache" );
				//Expires in one second
				response.setDateHeader( "Expires", 1000 );
			}
			response.setContentType( mimeType );
			
			//If one wants to download this file as an attachment, then we indicate this in the header
			if( isDownload ) {
				//Get the final file name
				final String finalFileName;
				if( fileName == null ) {
					finalFileName = ShortFileDescriptor.UNKNOWN_FILE_NAME;
				} else {
					finalFileName = URLEncoder.encode( fileName, ENCODING_TYPE );
				}
				response.setHeader( "Content-disposition", "attachment; filename=" + finalFileName );
			}
			
			ServletOutputStream responseOutputStream = response.getOutputStream();
			responseOutputStream.write( fileData );
			responseOutputStream.flush();
			responseOutputStream.close();
		} else {
			logger.error( "The servlet could not retrieve/generate "+fileKindDesc+" file with mime type \""+mimeType+"\"!" );
			throw new ServletException( "The servlet could not retrieve/generate "+fileKindDesc+" file with mime type \""+mimeType+"\"!" );
		}
	}

	/**
	 * This method allows to prepare the uploaded file, resize it if it is an image and etc.
	 * This method always creates file thumbnail and keeps image proportions, i.e. does not
	 * put it on a background.
	 * @param ownerID the user who uploads the file
	 * @param file the file data itself
	 * @param maxImgHeight the maximum height it will be resized to if it is an image, 
	 * @param maxImgWidth the maximum width it will be resized to if it is an image
	 * @param thumbHeight the maximum height of the thimbnail image if the file is an image
	 * @param thumbWidth the maximum widht of the thimbnail image if the file is an image
	 * @param bgColor the background color to use when resizing the image if it is an image
	 * @param target the string used for logging and should specify to where the file is being uploaded, e.g.: chat, forum
	 * @return the resulting complete file data
	 * @throws UserFileUploadException in case something goes wrong, either the upload or the image resizing or smth.  
	 * @throws IOException in case something goes wrong, either the upload or the image resizing or smth.
	 */
	public static UserFileData prepareUploadedFileData( final int ownerID, FileItem file,
													 final int maxImgHeight, final int maxImgWidth,
													 final int thumbHeight, final int thumbWidth,
													 final Color bgColor, final String target ) throws UserFileUploadException, IOException {
		return prepareUploadedFileData( ownerID, file, maxImgHeight, maxImgWidth,
				 						thumbHeight, thumbWidth, bgColor, target, true, true );
	}
	
	/**
	 * This method allows to prepare the uploaded file, resize it if it is an image and etc.
	 * @param ownerID the user who uploads the file
	 * @param file the file data itself
	 * @param maxImgHeight the maximum height it will be resized to if it is an image, 
	 * @param maxImgWidth the maximum width it will be resized to if it is an image
	 * @param thumbHeight the maximum height of the thimbnail image if the file is an image
	 * @param thumbWidth the maximum widht of the thimbnail image if the file is an image
	 * @param bgColor the background color to use when resizing the image if it is an image
	 * @param target the string used for logging and should specify to where the file is being uploaded, e.g.: chat, forum
	 * @param makeThumbnail if true then we make a thumbnail for the file, otherwise not
	 * @return the resulting complete file data
	 * @throws UserFileUploadException in case something goes wrong, either the upload or the image resizing or smth.  
	 * @throws IOException in case something goes wrong, either the upload or the image resizing or smth.
	 */
	public static UserFileData prepareUploadedFileData( final int ownerID, FileItem file,
													 final int maxImgHeight, final int maxImgWidth,
													 final int thumbHeight, final int thumbWidth,
													 final Color bgColor, final String target,
													 final boolean makeThumbnail,
													 final boolean keepProportions ) throws UserFileUploadException, IOException {
		final UserFileData fileDescriptor = new UserFileData();
		//NOTE: The file.getContentType() method is unreliable because on different browsers and OS it gives different mime-types!
		if( file.getName() != null ) {
			fileDescriptor.fileName = file.getName();
			fileDescriptor.mimeType = SupportedFileMimeTypes.getFileMimeTypeStringByExtension( fileDescriptor.fileName );
		}
		if( fileDescriptor.mimeType.equals( SupportedFileMimeTypes.MimeHelper.UNKNOWN_MIME_TYPE ) ) {
			fileDescriptor.mimeType = file.getContentType() ;
		}
		fileDescriptor.ownerID = ownerID;

		//If we have an image mime format for the uploaded file, then we need to
		//resize the image, create a thumbnail for it
		if( SupportedFileMimeTypes.isImageMimeType( fileDescriptor.mimeType ) ) {
			logger.debug( "Uploading an image file " + fileDescriptor.fileName +
						  " of type " + fileDescriptor.mimeType + " to the "+target+" by user " + fileDescriptor.ownerID );
			try{
				final InputStream inputStream = file.getInputStream();
				//Resize the image
				final ByteArrayOutputStream outputStream = ImageProcessor.resizeImage( inputStream, maxImgHeight, maxImgWidth, bgColor,
																					   SupportedFileMimeTypes.getMTSuffix( fileDescriptor.mimeType ),
																					   false, keepProportions, fileDescriptor );
				fileDescriptor.fileData = outputStream.toByteArray();
				//Close the input stream
				inputStream.close();
				if( makeThumbnail ) {
					//Resize to the thumbnail
					final ByteArrayOutputStream outputStreamThumb = ImageProcessor.resizeImage( outputStream, thumbHeight, thumbWidth, bgColor,
																								SupportedFileMimeTypes.getMTSuffix( fileDescriptor.mimeType ),
																								false );
					fileDescriptor.thumbnailData = outputStreamThumb.toByteArray();
					//Close the output streams
					outputStreamThumb.close();
				} else {
					fileDescriptor.thumbnailData = null;
				}
				outputStream.close();
			} catch (IOException ex){
				logger.error("Unable to resize user profile image and/or to create a thumbnail", ex);
				throw new UserFileUploadException( UserFileUploadException.UNSUPPORTED_UPLOAD_FILE_FORMAT_ERR );
			} catch ( IllegalArgumentException ex) {
				logger.error("Unable to read user profile image", ex);
				throw new UserFileUploadException( UserFileUploadException.UNSUPPORTED_UPLOAD_FILE_FORMAT_ERR );
			}
		} else{
			if( SupportedFileMimeTypes.isPlayableMimeType( fileDescriptor.mimeType ) ) {
				logger.debug( "Uploading a flash animation file " + fileDescriptor.fileName +
							  " of type " + fileDescriptor.mimeType + " to the "+target+" by user " + fileDescriptor.ownerID );
				final InputStream inputStream = file.getInputStream();
				final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				byte[] bytes = new byte[512];
				
				//Read bytes from the input stream in bytes.length-sized chunks and write them into the output stream
				int readBytes;
				while ((readBytes = inputStream.read(bytes)) > 0) {
					outputStream.write(bytes, 0, readBytes);
				}
				 
				//Convert the contents of the output stream into a byte array
				fileDescriptor.fileData = outputStream.toByteArray();
				 
				//Close the streams
				inputStream.close();
				outputStream.close();
			} else {
				logger.error( "The user " + fileDescriptor.ownerID + " tried to upload a file " + fileDescriptor.fileName +
							  " of type " + fileDescriptor.mimeType + " to the "+target+", but the file type is unrecognized" );
				//This servlet only supported images and flash animation
				throw new UserFileUploadException( UserFileUploadException.UNSUPPORTED_UPLOAD_FILE_FORMAT_ERR );
			}
		}
		
		return fileDescriptor;
	}
}
