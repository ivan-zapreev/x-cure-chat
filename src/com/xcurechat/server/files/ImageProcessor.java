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
 * The image processing package.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.server.files;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.xcurechat.client.data.ShortFileDescriptor;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.Color;

/**
 * @author zapreevis
 * This class is supposed to supply methods required for creating thumbnails and resizing images
 */
public final class ImageProcessor {
	//Get the Log4j logger object
	private static final Logger logger = Logger.getLogger( ImageProcessor.class );
	
	//The default background color to put the images on
	public static final Color DEFAULT_BACKGROUND_COLOR = new Color(149, 166, 208);
	
	//The maximum image height and width for 1024x768
	public static final int MAX_IMAGE_WIDTH_1024 = 1024;
	public static final int MAX_IMAGE_HEIGHT_768 = 768;
	
	//The maximum image height and width for 800x600
	public static final int MAX_IMAGE_WIDTH_800 = 800;
	public static final int MAX_IMAGE_HEIGHT_600 = 600;
	
	//The maximum image height and width for 640x480
	public static final int MAX_IMAGE_WIDTH_640 = 640;
	public static final int MAX_IMAGE_HEIGHT_480 = 480;
	
	//The width and height for the thumbnail images
	public static final int THUMBNAIL_WIDTH = 100;
	public static final int THUMBNAIL_HEIGHT = 75;
	
	//The width and height for the avatar images
	public static final int AVATAR_WIDTH = 60;
	public static final int AVATAR_HEIGHT = 60;

	/**
	 * Allows to center the image whose size is within the given maximum bounds and then write it to a new image with a black background.
	 * @param image the image to resie and center and write into a new image
	 * @param new_height the height to which the image will be resized
	 * @param new_width the width to which the image will be resized
	 * @param max_height the maximum allowed height of the image
	 * @param max_width the maximum allowed width of the image
	 * @param bgcolor the background color on to which the resied image will be placed
	 * @param isHighQuality if true then we use high quality resizing mechanism
	 * @param keepProportions if true then whether we resize the image or not, we do not place it on the background
	 * just reduce the image dimensions proportionally to make it fit into the maximum dimensions
	 * @return the newly created image to be placed in a database or smth
	 * @throws IOException if some problem occured while working with the image
	 */
	private static BufferedImage positionAndDrawImage( BufferedImage image, final int new_height,
													final int new_width, final int max_height,
													final int max_width, final Color bgcolor,
													final boolean isHighQuality, final boolean keepProportions ) throws IOException{
		//At this point we should expect that image has height and width <= then we set for new_image
		int top_left_x = 0, top_left_y = 0; 
		BufferedImage new_image;
		
		//Based on whether we want to keep the image proportional or not
		if( ! keepProportions ) {
			//We do not want to keep the image proportional and put it on a background
			new_image = new BufferedImage( max_width, max_height, BufferedImage.TYPE_INT_RGB);
			//In case image.height < new_width or image.width < new_height we should center the printing area 
			if( new_width <  max_width ) {
				top_left_x =  ( max_width - new_width ) / 2 ;
			}
			if( new_height <  max_height ) {
				top_left_y =  ( max_height - new_height ) / 2 ;
			}
		} else {
			//Just create the image of the same new size and thus do not do any centering
			new_image = new BufferedImage( new_width, new_height, BufferedImage.TYPE_INT_RGB);
		}

		//Initialize the 2D graphics
		Graphics2D graphics_new_img = new_image.createGraphics();
		if( isHighQuality ) {
			graphics_new_img.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		} else {
			graphics_new_img.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		}
		
		//Draw the image to the "canvas"
		logger.debug( "Putting image to top_left_x = " + top_left_x + ", top_left_y = " + top_left_y +
						", new_width = " + new_width + ", new_height = " + new_height + ", bgcolor = " + bgcolor );
		graphics_new_img.setColor( bgcolor );					/*Put the background color*/
		graphics_new_img.fillRect(0, 0, max_width, max_height);	/*Put the background color*/
		graphics_new_img.drawImage(image, top_left_x, top_left_y, new_width, new_height, null);
		
		return new_image;
	}
	
	/**
	 * Allows to check if the image is of a proper size then if keepOriginal == true
	 * it is kept intact, otherwise it is resizes and fit it into the prescribed
	 * frame with a given background.
	 * @param data the input image data
	 * @param max_height the maximum allowed height of the image
	 * @param max_width the maximum allowed width of the image
	 * @param bgcolor the background color to put the picture on
	 * @param imageMimeType the mime type format in which the picture will be saved
	 * @param isHighQuality if true then we use the high quality when resizing the image
	 * @param keepProportions if true then whether we resize the image or not, we do not place it on the background
	 * just reduce the image dimensions proportionally to make it fit into the maximum dimensions
	 * @param fileDesc the descriptor used for storing the resulting image width and height, can be null
	 * @return the scaled image in the specified mime-type format
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	private static ByteArrayOutputStream resizeImage( BufferedImage image, final int max_height,
													  final int max_width, final Color bgcolor,
													  final String imageMimeType, final boolean isHighQuality,
													  final boolean keepProportions, final ShortFileDescriptor fileDesc ) throws IOException {
		BufferedImage newImage = image;
		//Check if the image needs to be resized if yes, resize
		int curr_width = image.getWidth(), curr_height = image.getHeight();
		
		logger.debug( "Resizing image, current width = " + curr_width + ", current height = " + curr_height +
						", max width = " + max_width + ", max height = " + max_height );
		
		//Resize the image and put it on a background if needed
		if( ( curr_width > max_width ) || ( curr_height > max_height ) ) {
			final double w_h_ratio = (double) curr_width / (double) curr_height;  
			final double h_w_ratio = (double) curr_height / (double) curr_width;  
			if( curr_width > max_width ) {
				curr_width = max_width;
				curr_height = (int) ( h_w_ratio * curr_width ); 
			}
			if( curr_height > max_height ) {
				curr_height = max_height;
				curr_width = (int) ( w_h_ratio * curr_height );
			}
			newImage = positionAndDrawImage( image, curr_height, curr_width, max_height, max_width, bgcolor, isHighQuality, keepProportions );
		} else {
			if( ! keepProportions ) {
				//If we are here then the image fits into the maximum dimensions and we do not want it to be proportionally resized
				//I. e. if it is not an exact fit for the maximum dimensions, then we position it on a background image 
				if( ( curr_height < max_width ) || ( curr_width < max_height ) ) {
					newImage = positionAndDrawImage( image, curr_height, curr_width, max_height, max_width, bgcolor, isHighQuality, false );
				}
			}
		}
		
		//If the file descriptor is set then
		if( fileDesc != null ) {
			//If the image is kept proportional set the re-calculated proportional size
			//otherwise set the images new size, as defined by the maximum size values
			if( keepProportions ) {
				fileDesc.widthPixels = curr_width;
				fileDesc.heightPixels = curr_height;
			} else {
				fileDesc.widthPixels = max_width;
				fileDesc.heightPixels = max_height;
			}
		}
		
		//Encode image into jpeg
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
		ImageIO.write( newImage, imageMimeType, byteOutput );
		logger.debug( "Has written the image as a " + imageMimeType + " file, the output is of length " + byteOutput.size() );
		
		return byteOutput;
	} 
	
	/**
	 * Allows to check if the image is of a proper size then if keepOriginal == true
	 * it is kept intact, otherwise it is resizes and fit it into the prescribed
	 * frame with a given background.
	 * @param data the input image data
	 * @param max_height the maximum allowed height of the image
	 * @param max_width the maximum allowed width of the image
	 * @param bgcolor the background color to put the picture on
	 * @param imageMimeType the mime type format in which the picture will be saved
	 * @param isHighQuality if true then we use the high quality when resizing the image
	 * @param keepProportions if true then whether we resize the image or not, we do not place it on the background
	 * just reduce the image dimensions proportionally to make it fit into the maximum dimensions
	 * @return the scaled image in the specified mime-type format
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	private static ByteArrayOutputStream resizeImage( ByteArrayOutputStream image_data, final int max_height,
													 final int max_width, final Color bgcolor,
													 final String imageMimeType, final boolean isHighQuality,
													 final boolean keepProportions )
													 throws IOException, IllegalArgumentException {
		return resizeImage( ImageIO.read( new ByteArrayInputStream( image_data.toByteArray() ) ),
							max_height, max_width, bgcolor, imageMimeType, isHighQuality, keepProportions, null);
	}

	/**
	 * Allows to resize the image and fit it into the prescribed frame with a given background
	 * @param data the input image data
	 * @param max_height the maximum allowed height of the image
	 * @param max_width the maximum allowed width of the image
	 * @param bgcolor the background color to put the picture on
	 * @param imageMimeType the mime type format in which the picture will be saved
	 * @param isHighQuality if true then we use the high quality when resizing the image
	 * @return the scaled image in the specified mime-type format
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public static ByteArrayOutputStream resizeImage( ByteArrayOutputStream image_data, final int max_height,
													 final int max_width, final Color bgcolor,
													 final String imageMimeType, final boolean isHighQuality )
													 throws IOException, IllegalArgumentException {
		return resizeImage( image_data, max_height, max_width, bgcolor, imageMimeType, isHighQuality, false );
	}
	
	/**
	 * Allows to check if the image is of a proper size then if keepOriginal == true
	 * it is kept intact, otherwise it is resizes and fit it into the prescribed
	 * frame with a given background.
	 * @param data the input image data
	 * @param max_height the maximum allowed height of the image
	 * @param max_width the maximum allowed width of the image
	 * @param bgcolor the background color to put the picture on
	 * @param imageMimeType the mime type format in which the picture will be saved
	 * @param isHighQuality if true then we use the high quality when resizing the image
	 * @param keepProportions if true then whether we resize the image or not, we do not place it on the background
	 * just reduce the image dimensions proportionally to make it fit into the maximum dimensions
	 * @param fileDesc the descriptor used for storing the resulting image width and height, can be null
	 * @return the scaled image in the specified mime-type format
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public static ByteArrayOutputStream resizeImage( InputStream data, final int max_height,
													final int max_width, final Color bgcolor,
													final String imageMimeType, final boolean isHighQuality,
													final boolean keepProportions, final ShortFileDescriptor fileDesc )
													throws IOException, IllegalArgumentException { 
		return resizeImage( ImageIO.read( data ), max_height, max_width, bgcolor, imageMimeType, isHighQuality, keepProportions, fileDesc);
	}
	
	/**
	 * Allows to resize the image and fit it into the prescribed frame with a given background
	 * @param data the input image data
	 * @param max_height the maximum allowed height of the image
	 * @param max_width the maximum allowed width of the image
	 * @param bgcolor the background color to put the picture on
	 * @param imageMimeType the mime type format in which the picture will be saved
	 * @param isHighQuality if true then we use the high quality when resizing the image
	 * @return the scaled image in the specified mime-type format
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public static ByteArrayOutputStream resizeImage( InputStream data, final int max_height,
													final int max_width, final Color bgcolor,
													final String imageMimeType, final boolean isHighQuality )
													throws IOException, IllegalArgumentException {
		return resizeImage( data, max_height, max_width, bgcolor, imageMimeType, isHighQuality, false, null );
	}
}
