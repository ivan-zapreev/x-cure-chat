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
 * The user data objects package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.data;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.xcurechat.client.utils.SupportedFileMimeTypes;

/**
 * @author zapreevis
 * This file respresents the short descriptor of the file stored in the database
 * For mime types and file extensions see http://www.webmaster-toolkit.com/mime-types.shtml
 */
public class ShortFileDescriptor implements IsSerializable {
	
	/**
	 * @author zapreevis
	 * Represents possible image orientation, rotating to the right from the origin
	 */
	public enum ImageOrientation {
		DEFAULT(     0.0 ),	//The original image orientation
		RIGHT_90(   90.0 ),	//The image rotated by  90 degrees to the right
		RIGHT_180( 180.0 ),	//The image rotated by 180 degrees to the right
		RIGHT_270( 270.0 );	//The image rotated by 270 degrees to the right
		
		//The number of degrees from the origin
		private final double degrees;
		
		/**
		 * The basic constructor
		 * @param degrees the alternation in degrees from the origin
		 */
		ImageOrientation( final double degrees ) {
			this.degrees = degrees;
		}
		
		/**
		 * Allows to get the degree equivalent of the given orientation.
		 * Counted in degrees the right from the origin.
		 * @return the corresponding angle in radiant
		 */
		public double getRadians() {
			return degrees * ( Math.PI / 180.0 );
		}
	}
	
	//The images of the 0 < sizes <= the given ones are displayed with stretching
	//Except for the images with at least one dimension size set to zero
	public static final int MINIMUM_VALID_IMAGE_WIDTH = 5;
	public static final int MINIMUM_VALID_IMAGE_HEIGHT = 5;
	
	//The values for the unknown image height and width
	public static final int ZERO_IMAGE_WIDTH  = 0;
	public static final int ZERO_IMAGE_HEIGHT = 0;
	
	//The maximum visible length of the file name
	public static final int MAX_VISIBLE_FILE_NAME_LENGTH = 30;
	
	//Unknown file ID constant
	public static final int UNKNOWN_FILE_ID = 0;
	//Unknown file name constant
	public static final String UNKNOWN_FILE_NAME = "image.jpeg";
	
	//The id of the file
	public int fileID = UNKNOWN_FILE_ID;
	
	//The MIME type of the file, the default time type is the jpeg image, this is required to view default server images 
	public String mimeType = SupportedFileMimeTypes.JPEG_IMAGE_MIME.getMainMimeType();
	
	//The file name of the file
	public String fileName = UNKNOWN_FILE_NAME;
	
	//The width of the image file in pixels, is only valid for image files,
	//is determined by the server side, the default value is ZERO_IMAGE_WIDTH
	public int widthPixels = ZERO_IMAGE_WIDTH;
	
	//The height of the image file in pixels, is only valid for image files,
	//is determined by the server side, the default value is ZERO_IMAGE_HEIGHT
	public int heightPixels = ZERO_IMAGE_HEIGHT;
	
	//Currently this field is only used on the GWT client in order to store the current image orientation
	public transient ImageOrientation imageOrient = ImageOrientation.DEFAULT;
	
	//The following store the GWT client-related image file width
	//and height for the case the image is zoomed out
	public transient int widthScaledPixels = ZERO_IMAGE_WIDTH;
	public transient int heightScaledPixels = ZERO_IMAGE_HEIGHT;
	
	public ShortFileDescriptor() {
	}
	
	public ShortFileDescriptor( final int widthPixels, final int heightPixels  ) {
		this.widthPixels = widthPixels;
		this.heightPixels = heightPixels;
	}
	
	/**
	 * In case the given file descriptor corresponds to an image file then this method
	 * allows to change the current orientation data of the image by 90 degrees to the
	 * right. If this is not an image file descriptor then this method does nothing.
	 */
	public void rotateImageDataRight() {
		if( SupportedFileMimeTypes.isImageMimeType( mimeType ) ) {
			switch( imageOrient ){
				case DEFAULT:
					imageOrient = ImageOrientation.RIGHT_90;
					break;
				case RIGHT_90:
					imageOrient = ImageOrientation.RIGHT_180;
					break;
				case RIGHT_180:
					imageOrient = ImageOrientation.RIGHT_270;
					break;
				case RIGHT_270:
				default:
					imageOrient = ImageOrientation.DEFAULT;
					break;
			}
			//Update the width and the height, both the original and the zoomed out ones
			final int storedHeight = heightPixels;
			heightPixels = widthPixels;
			widthPixels = storedHeight;
			final int storedViewHeight = heightScaledPixels;
			heightScaledPixels = widthScaledPixels;
			widthScaledPixels = storedViewHeight;
		}
	}
	
	/**
	 * Allows to copy the given instance of the file descriptor into the given object
	 * @param result the object into which we want to copy the data
	 * @return the same object as was provided for an argument, this is just for simplicity
	 */
	protected ShortFileDescriptor copyTo(final ShortFileDescriptor target) {
		target.fileID = fileID;
		target.mimeType = mimeType;
		target.fileName = fileName;
		target.widthPixels = widthPixels;
		target.heightPixels = heightPixels;
		return target;
	}
	
	/**
	 * Allows to clone the given instance of the file descriptor
	 */
	public ShortFileDescriptor clone() {
		return copyTo( new ShortFileDescriptor() );
	}
	
	/**
	 * Allows to get a short file name from the long file name.
	 * The maximum length of the file name is bounded by MAX_VISIBLE_FILE_NAME_LENGTH
	 * @param fileName the long file name
	 * @return the short file name if it is longer that MAX_VISIBLE_FILE_NAME_LENGTH
	 */
	public static String getShortFileName( final String fileName) {
		if( fileName != null ) {
			if( fileName.length() > MAX_VISIBLE_FILE_NAME_LENGTH ) {
				final String suffix = "...";
				return fileName.substring(0, MAX_VISIBLE_FILE_NAME_LENGTH - suffix.length() ) + suffix;
			} else {
				return fileName;
			}
		} else {
			return "Unknown file name";
		}
	}
}
