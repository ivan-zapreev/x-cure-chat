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
 * 
 * This file was derived from:
 *	http://www.javapractices.com/topic/TopicAction.do?Id=221
 * License:
 *	http://creativecommons.org/licenses/by/3.0/
 */
package com.xcurechat.server.files;

import java.util.*;

import java.io.File;

import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileCleaningTracker;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.FileCleanerCleanup;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

/**
* Wrapper for a file upload request.
* 
* <P>This class uses the Apache Commons 
* <a href='http://commons.apache.org/fileupload/'>File Upload tool</a>.
* The generous Apache License will very likely allow you to use it in your 
* applications as well. 
*/
public class FileUploadWrapper extends HttpServletRequestWrapper {
	
	private static DiskFileItemFactory factory = null;
	
	private static DiskFileItemFactory getDiskFileItemFactory( final ServletContext context ) {
		if( factory == null ){
			File repository = new File(System.getProperty("java.io.tmpdir"));
			FileCleaningTracker fileCleaningTracker = FileCleanerCleanup.getFileCleaningTracker(context);
			factory = new DiskFileItemFactory( DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD, repository );
			factory.setFileCleaningTracker(fileCleaningTracker);
		}
		return factory; 
	}
	
	/** Constructor.  */
	@SuppressWarnings("unchecked")
	public FileUploadWrapper(HttpServletRequest aRequest, final ServletContext context,
							final long maxFileSizeBytes, final long maxRequestSizeBytes)
							throws FileUploadException {
		super(aRequest);
		ServletFileUpload upload = new ServletFileUpload( getDiskFileItemFactory(context) );
		
		//Limit the request size and the file size
		upload.setFileSizeMax( maxFileSizeBytes );
		upload.setSizeMax( maxRequestSizeBytes );
		
		List<FileItem> fileItems = upload.parseRequest(aRequest);
		convertToMaps(fileItems);
	}

	/**
	 * Return all request parameter names, for both regular controls and file upload 
	 * controls.
	 */
	@Override
	public Enumeration<String> getParameterNames() {
		Set<String> allNames = new LinkedHashSet<String>();
		allNames.addAll(fRegularParams.keySet());
		allNames.addAll(fFileParams.keySet());
		return Collections.enumeration(allNames);
	}

	/**
	 * Return the parameter value. Applies only to regular parameters, not to 
	 * file upload parameters. 
	 * 
	 * <P>If the parameter is not present in the underlying request, 
	 * then <tt>null</tt> is returned.
	 * <P>If the parameter is present, but has no  associated value, 
	 * then an empty string is returned.
	 * <P>If the parameter is multivalued, return the first value that 
	 * appears in the request.  
	 */
	@Override
	public String getParameter(String aName) {
		String result = null;
		List<String> values = fRegularParams.get(aName);
		if (values == null) {
			//you might try the wrappee, to see if it has a value 
		} else if (values.isEmpty()) {
			//param name known, but no values present
			result = "";
		} else {
			//return first value in list
			result = values.get(FIRST_VALUE);
		}
		return result;
	}

	/**
	 * Return the parameter values. Applies only to regular parameters, 
	 * not to file upload parameters.
	 */
	@Override
	public String[] getParameterValues(String aName) {
		String[] result = null;
		List<String> values = fRegularParams.get(aName);
		if (values != null) {
			result = values.toArray(new String[values.size()]);
		}
		return result;
	}

	/**
	 * Return a {@code Map<String, String>} for all regular parameters.
	 * Does not return any file upload paramters at all. 
	 */
	@Override
	public Map<String, List<String>> getParameterMap() {
		return Collections.unmodifiableMap(fRegularParams);
	}

	/**
	 * Return a {@code List<FileItem>}, in the same order as they appear
	 *  in the underlying request.
	 */
	public List<FileItem> getFileItems() {
		return new ArrayList<FileItem>(fFileParams.values());
	}

	/**
	 * Return the {@link FileItem} of the given name.
	 * <P>If the name is unknown, then return <tt>null</tt>.
	 */
	public FileItem getFileItem(String aFieldName) {
		return fFileParams.get(aFieldName);
	}

	// PRIVATE //

	/** Store regular params only. May be multivalued (hence the List).  */
	private final Map<String, List<String>> fRegularParams = new LinkedHashMap<String, List<String>>();

	/** Store file params only. */
	private final Map<String, FileItem> fFileParams = new LinkedHashMap<String, FileItem>();

	private static final int FIRST_VALUE = 0;

	private void convertToMaps(List<FileItem> aFileItems) {
		for (FileItem item : aFileItems) {
			if (isFileUploadField(item)) {
				fFileParams.put(item.getFieldName(), item);
			} else {
				if (alreadyHasValue(item)) {
					addMultivaluedItem(item);
				} else {
					addSingleValueItem(item);
				}
			}
		}
	}

	private boolean isFileUploadField(FileItem aFileItem) {
		return !aFileItem.isFormField();
	}

	private boolean alreadyHasValue(FileItem aItem) {
		return fRegularParams.get(aItem.getFieldName()) != null;
	}

	private void addSingleValueItem(FileItem aItem) {
		List<String> list = new ArrayList<String>();
		list.add(aItem.getString());
		fRegularParams.put(aItem.getFieldName(), list);
	}

	private void addMultivaluedItem(FileItem aItem) {
		List<String> values = fRegularParams.get(aItem.getFieldName());
		values.add(aItem.getString());
	}
} 
