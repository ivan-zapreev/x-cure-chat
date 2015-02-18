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

import com.xcurechat.client.i18n.UIErrorMessages;

/**
 * @author zapreevis
 * This exception is thrown if the file upload fails 
 */
public class UserFileUploadException extends SiteLogicException {
	
	//The server side reported that file upload failed because
	//the file/request are too large
	public static final Integer FILE_IS_TOO_LARGE_ERR = new Integer(600);
	public static final Integer FILE_IS_NOT_SELECTED_ERR = new Integer(601);
	public static final Integer UNSUPPORTED_UPLOAD_FILE_FORMAT_ERR = new Integer(602);
	
	static {
		//Register the error code in the super class, to avoid duplicates
		SiteLogicException.registerErrorCode( FILE_IS_TOO_LARGE_ERR );
		SiteLogicException.registerErrorCode( FILE_IS_NOT_SELECTED_ERR );
		SiteLogicException.registerErrorCode( UNSUPPORTED_UPLOAD_FILE_FORMAT_ERR );
	}
	
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 1L;
	
	public UserFileUploadException(){
		super();
	}
	
	public UserFileUploadException( String errorMessage ){
		super( errorMessage );
	}
	
	public UserFileUploadException( Integer errorCode ){
		super( errorCode );
	}
	
	private long max_upload_file_size_kb = 0;
	
	/**
	 * Sets the maximum upload files size in bytes
	 * @param size
	 */
	public void setMaxUploadFileSize( long size_bytes ){
		max_upload_file_size_kb = size_bytes / 1024;
	}
	
	/**
	 * @return the maximum upload files size in bytes
	 */
	public long getMaxUploadFileSize( ){
		return max_upload_file_size_kb * 1024;
	}
	
	protected long getMUFSMb() {
		return max_upload_file_size_kb / 1024;
	}
	
	protected long getMUFSKb() {
		return max_upload_file_size_kb % 1024;
	}
	
	@Override
	public void populateLocalizedMessages( UIErrorMessages errorMsgI18N ) {
		super.populateLocalizedMessages( errorMsgI18N );
		
		addLocalizedErrorMessage( FILE_IS_TOO_LARGE_ERR, errorMsgI18N.fileIsTooLargeInError( getMUFSMb(), getMUFSKb() ) );
		addLocalizedErrorMessage( FILE_IS_NOT_SELECTED_ERR, errorMsgI18N.fileIsNotSelectedError( ) );
		addLocalizedErrorMessage( UNSUPPORTED_UPLOAD_FILE_FORMAT_ERR, errorMsgI18N.incorectUploadFileFormat( ) );
	}
}
