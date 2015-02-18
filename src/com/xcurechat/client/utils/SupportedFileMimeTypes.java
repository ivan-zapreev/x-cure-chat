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
package com.xcurechat.client.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zapreevis
 * Contains the mime types of the supported files
 */
public enum SupportedFileMimeTypes {
	MP3_FLASH_MIME ( MimeHelper.AUDIO_MIME_TYPE_PREFIX, new String[] {"mpeg"} ),
	MP4_FLASH_MIME ( MimeHelper.VIDEO_MIME_TYPE_PREFIX, new String[] {"mp4"} ),
	FLV_FLASH_MIME ( MimeHelper.VIDEO_MIME_TYPE_PREFIX, new String[] {"x-flv", "flv"}),
	SWF_FLASH_MIME ( MimeHelper.APPLICATION_MIME_TYPE_PREFIX, new String[] {"x-shockwave-flash"} ),
	JPEG_IMAGE_MIME ( MimeHelper.IMAGE_MIME_TYPE_PREFIX, new String[] {"jpeg"} ),
	GIF_IMAGE_MIME ( MimeHelper.IMAGE_MIME_TYPE_PREFIX, new String[] {"gif"} ),
	PNG_IMAGE_MIME ( MimeHelper.IMAGE_MIME_TYPE_PREFIX, new String[] {"png"} );
	
	//The delimiter between the file name and the file extension
	public static final String FILE_NAME_EXTENSION_DELIMITER = ".";
	
	//The mapping from the file extension to the MIME type name
	private static final Map<String, SupportedFileMimeTypes> fileExtToMimeTypeStr = new HashMap<String, SupportedFileMimeTypes>();
	
	static {
		fileExtToMimeTypeStr.put( MimeHelper.MP4_FILE_SUFIX, MP4_FLASH_MIME );
		fileExtToMimeTypeStr.put( MimeHelper.MP3_FILE_SUFIX, MP3_FLASH_MIME );
		fileExtToMimeTypeStr.put( MimeHelper.FLV_FILE_SUFIX, FLV_FLASH_MIME );
		fileExtToMimeTypeStr.put( MimeHelper.SWF_FILE_SUFIX, SWF_FLASH_MIME );
		fileExtToMimeTypeStr.put( MimeHelper.JPEG_FILE_SUFIX, JPEG_IMAGE_MIME );
		fileExtToMimeTypeStr.put( MimeHelper.JPG_FILE_SUFIX, JPEG_IMAGE_MIME );
		fileExtToMimeTypeStr.put( MimeHelper.PNG_FILE_SUFIX, PNG_IMAGE_MIME );
		fileExtToMimeTypeStr.put( MimeHelper.GIF_FILE_SUFIX, GIF_IMAGE_MIME );
	}
	
	/**
	 * Allows to retrieve the file extension from the file name
	 * @param fileName the complete file name, with an extension
	 * @return the file extension of null if no extension is set
	 *         or the file name is null, the value is lower-cased
	 */
	public static String getFileExtension( final String fileName ) {
		String fileExt = null;
		if( fileName != null ) {
			final int index = fileName.lastIndexOf( FILE_NAME_EXTENSION_DELIMITER );
			if( index != -1 ) {
				fileExt = fileName.substring( index );
				if( fileExt != null ) {
					fileExt = fileExt.trim().toLowerCase();
				}
			}
		}
		return fileExt;
	}
	
	/**
	 * Allows to get the file's mime type by the file name (with the proper file-name extention)
	 * @param fileName the file name
	 * @return the mime type or null if the file format is not recognized
	 */
	public static SupportedFileMimeTypes getFileMimeTypeByExtension( final String fileName ) {
		final String fileExt = getFileExtension( fileName );
		if( fileExt != null ) {
			return fileExtToMimeTypeStr.get( fileExt );
		}
		return null;
	}
	
	/**
	 * Allows to get the file's mime type by the file name (with the proper file-name extention)
	 * @param fileName the file name
	 * @return the mime type string or "unknown mime type string" if the file format is not recognized
	 */
	public static String getFileMimeTypeStringByExtension( final String fileName ) {
		SupportedFileMimeTypes mimeType = getFileMimeTypeByExtension( fileName );
		if( mimeType == null ) {
			return MimeHelper.UNKNOWN_MIME_TYPE;
		} else {
			return mimeType.getMainMimeType();
		}
	}
	
	/**
	 * Allows to retrieve file type, i.e. mime-type suffix, from the mime-type string
	 * @param fileMimeType the mime-type string
	 * @return the suffix of the mime type, e.g., for "image/jpeg" it returns "jpeg" or an empty string if the mime-type is null
	 */
	public static String getMTSuffix( final String fileMimeType ) {
		String suffix = "";
		if( fileMimeType != null ) {
			final int index = fileMimeType.lastIndexOf( MimeHelper.MIME_TYPE_DELIMITER );
			if( ( index >= 0 ) && ( index < (fileMimeType.length() - 1 ) ) ) {
				suffix = fileMimeType.substring( index + 1 );
			}
		}
		return suffix;
	}
	
	/**
	 * Allows to detect whether the given mime type corresponds to an image file
	 * @param fileMimeType the file's mime type
	 * @return true if it is an image mime type
	 */
	public static boolean isImageMimeType( final String fileMimeType ) {
		if( fileMimeType != null ) {
			return fileMimeType.trim().toLowerCase().startsWith( MimeHelper.IMAGE_MIME_TYPE_PREFIX );
		} else {
			return false;
		}
	}
	
	/**
	 * Allows to detect whether the given mime type corresponds some playable mime-type (video, audio)
	 * @param fileMimeType the file's mime type
	 * @return true if it is a playable mime-type (video, audio)
	 */
	public static boolean isPlayableMimeType( final String fileMimeType ) {
		return isSWFMimeType( fileMimeType ) || isFLVMimeType( fileMimeType ) || isMP4MimeType( fileMimeType ) ||  isMP3MimeType( fileMimeType );
	}
	
	/**
	 * Allows to detect whether the given mime type corresponds to an swf (shockwave-flash) file format
	 * @param fileMimeType the file's mime type
	 * @return true if it is a shockwave-flash/flash video mime type
	 */
	public static boolean isSWFMimeType( final String fileMimeType ) {
		return isEqualMimeTypes( SWF_FLASH_MIME, fileMimeType );
	}
	
	/**
	 * Allows to detect whether the given mime type corresponds to an flv (flash video) file format
	 * @param fileMimeType the file's mime type
	 * @return true if it is a flash video mime type
	 */
	public static boolean isFLVMimeType( final String fileMimeType ) {
		return isEqualMimeTypes( FLV_FLASH_MIME, fileMimeType );
	}
	
	/**
	 * Allows to detect whether the given mime type corresponds to an mp3 (mp3 audio) file format
	 * @param fileMimeType the file's mime type
	 * @return true if it is a mp3 audio mime type
	 */
	public static boolean isMP3MimeType( final String fileMimeType ) {
		return isEqualMimeTypes( MP3_FLASH_MIME, fileMimeType );
	}
	
	/**
	 * Allows to detect whether the given mime type corresponds to an mp4 (mp4 video) file format
	 * @param fileMimeType the file's mime type
	 * @return true if it is a mp4 video mime type
	 */
	public static boolean isMP4MimeType( final String fileMimeType ) {
		return isEqualMimeTypes( MP4_FLASH_MIME, fileMimeType );
	}
	
	/**
	 * Allows to detect whether the given mime types are the same
	 * @param mimeTypeOne the first mime type
	 * @param mimeTypeTwo the second mime type
	 * @return true if the two mime-types are equal
	 */
	private static boolean isEqualMimeTypes( final SupportedFileMimeTypes mimeTypeOne, final String mimeTypeTwo ) {
		if( mimeTypeTwo != null ) {
			return mimeTypeOne.getMimeTypeStrings().contains( mimeTypeTwo.trim().toLowerCase() );
		} else {
			return false;
		}
	}
	
	//The prefix of the mime type with a slash, e.g. "image/"
	private final String mimeTypePrefix;
	//The suffix of the mime type, e.g. "jpeg"
	private final String[] mimeTypeSuffixes;
	//The list of the mime-type strings corresponding to this mime-type
	private final List<String> mimeTypeStrings;
	//The main mime-type string
	private final String mainMimeType;
	//The main mime-type suffix
	private final String mainMTSuffix;
	
	/**
	 * The simple constructor
	 * @param mimeTypePrefix the prefix of the mime type with a slash, e.g. "image/"
	 * @param mimeTypeSuffixes the suffix of the mime type, e.g. "jpeg"
	 */
	private SupportedFileMimeTypes(final String mimeTypePrefix, final String[] mimeTypeSuffixes) {
		this.mimeTypePrefix = mimeTypePrefix;
		this.mimeTypeSuffixes = mimeTypeSuffixes;
		
		//Initialize the maim mime-type string
		mainMTSuffix = this.mimeTypeSuffixes[0];
		mainMimeType = mimeTypePrefix + mainMTSuffix;
		
		//Create and initialize the mime-type strings
		this.mimeTypeStrings = new ArrayList<String>();
		for( int i=0; i < mimeTypeSuffixes.length; i++) {
			this.mimeTypeStrings.add( mimeTypePrefix + mimeTypeSuffixes[i] );
		}
	}
	
	/**
	 * Allows to get the mime-type prefix, e.g., for "image/jpeg" it returns "image/"
	 * @return the prefix of the mime type
	 */
	public String getMTPrefix() {
		return mimeTypePrefix;
	}
	
	/**
	 * Allows to get the main mime-type suffix, e.g., for "image/jpeg" it returns "jpeg"
	 * Note that for the same mime type there might be several equivalent suffixes,
	 * so here we return the first one with thich we initialized this mime type.
	 * @return the main suffix of the mime type
	 */
	public String getMainMTSuffix() {
		return mainMTSuffix;
	}
	
	/**
	 * Allows to get the list of the mime-type strings corresponding to this mime-type
	 * @return the list of the mime-type strings corresponding to this mime-type
	 */
	public List<String> getMimeTypeStrings() {
		return mimeTypeStrings;
	}
	
	/**
	 * Allows to return the mime-type string, e.g., "image/jpeg"
	 * @return the mime-type string
	 */
	public String getMainMimeType() {
		return mainMimeType;
	}
	
	/**
	 * The class that stores some constants
	 */
	public class MimeHelper {
		public static final String UNKNOWN_MIME_TYPE = "unknown";
		
		public static final String MP4_FILE_SUFIX = FILE_NAME_EXTENSION_DELIMITER + "mp4";
		public static final String MP3_FILE_SUFIX = FILE_NAME_EXTENSION_DELIMITER + "mp3";
		public static final String FLV_FILE_SUFIX = FILE_NAME_EXTENSION_DELIMITER + "flv";
		public static final String SWF_FILE_SUFIX = FILE_NAME_EXTENSION_DELIMITER + "swf";
		public static final String JPEG_FILE_SUFIX = FILE_NAME_EXTENSION_DELIMITER + "jpeg";
		public static final String JPG_FILE_SUFIX = FILE_NAME_EXTENSION_DELIMITER + "jpg";
		public static final String PNG_FILE_SUFIX = FILE_NAME_EXTENSION_DELIMITER + "png";
		public static final String GIF_FILE_SUFIX = FILE_NAME_EXTENSION_DELIMITER + "gif";
		
		public static final String MIME_TYPE_DELIMITER = "/";
		
		public static final String AUDIO_MIME_TYPE_PREFIX = "audio" + MIME_TYPE_DELIMITER;
		public static final String VIDEO_MIME_TYPE_PREFIX = "video" + MIME_TYPE_DELIMITER;
		public static final String APPLICATION_MIME_TYPE_PREFIX = "application" + MIME_TYPE_DELIMITER;
		public static final String IMAGE_MIME_TYPE_PREFIX = "image" + MIME_TYPE_DELIMITER;
		public static final String IMAGE_FILE_TYPE_JPEG = "jpeg";
	}

}
