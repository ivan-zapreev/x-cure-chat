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
 * The user interface package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zapreevis
 * This class is used solely for storing the preset avatar images
 */
public final class PresetAvatarImages {
	
	private static final String RELATIVE_AVATAR_LOCATION = "user/";
	
	private static final int FREE_AVATAR_COST = 0;
	private static final int LOW_PRICE_AVATAR_COST = 50;
	private static final int MEDIUM_PRICE_AVATAR_COST = 100;
	private static final int HIGH_PRICE_AVATAR_COST = 150;
	
	public class AvatarSectionDescriptor {
		public final int price;
		public final Map<Integer, AvatarDescriptor> avatars;
		public AvatarSectionDescriptor( final int price, final Map<Integer, AvatarDescriptor> avatars ) {
			this.price = price;
			this.avatars = avatars;
		}
	}
	
	public class AvatarDescriptor {
		public final String relativeURL;
		public final int price;
		public final SupportedFileMimeTypes mimeType;
		public AvatarDescriptor( final int index, final int price, final SupportedFileMimeTypes mimeType ) {
			this.relativeURL = RELATIVE_AVATAR_LOCATION + index +
							  ( mimeType == SupportedFileMimeTypes.GIF_IMAGE_MIME ? ".gif" : ".jpg");
			this.price = price;
			this.mimeType = mimeType;
		}
	}
	
	private static final PresetAvatarImages instance = new PresetAvatarImages();
	
	//The data storage for the list of preset avatar sections
	public static final List<AvatarSectionDescriptor> avatarSections = new ArrayList<AvatarSectionDescriptor>();
	
	static {
		//Add free avatar sections first
		AvatarSectionDescriptor currentSection = startNewAvatarSection( FREE_AVATAR_COST );
		addAvatarsToSection( currentSection, 1, 60, SupportedFileMimeTypes.JPEG_IMAGE_MIME );    /*60*/
		currentSection = startNewAvatarSection( FREE_AVATAR_COST );
		addAvatarsToSection( currentSection, 61, 120, SupportedFileMimeTypes.JPEG_IMAGE_MIME );  /*60*/
		currentSection = startNewAvatarSection( FREE_AVATAR_COST );
		addAvatarsToSection( currentSection, 121, 180, SupportedFileMimeTypes.JPEG_IMAGE_MIME ); /*60*/
		currentSection = startNewAvatarSection( FREE_AVATAR_COST );
		addAvatarsToSection( currentSection, 298, 356, SupportedFileMimeTypes.JPEG_IMAGE_MIME ); /*59*/
		currentSection = startNewAvatarSection( FREE_AVATAR_COST );
		addAvatarsToSection( currentSection, 181, 184, SupportedFileMimeTypes.JPEG_IMAGE_MIME ); /* 4*/
		addAvatarsToSection( currentSection, 357, 369, SupportedFileMimeTypes.JPEG_IMAGE_MIME ); /*13*/
		addAvatarsToSection( currentSection, 540, 544, SupportedFileMimeTypes.JPEG_IMAGE_MIME ); /* 5*/
		
		//Add low price avatars
		currentSection = startNewAvatarSection( LOW_PRICE_AVATAR_COST );
		addAvatarsToSection( currentSection, 185, 214, SupportedFileMimeTypes.GIF_IMAGE_MIME ); /*30*/
		currentSection = startNewAvatarSection( LOW_PRICE_AVATAR_COST );
		addAvatarsToSection( currentSection, 215, 225, SupportedFileMimeTypes.GIF_IMAGE_MIME ); /*11*/
		addAvatarsToSection( currentSection, 370, 388, SupportedFileMimeTypes.GIF_IMAGE_MIME ); /*19*/
		currentSection = startNewAvatarSection( LOW_PRICE_AVATAR_COST );
		addAvatarsToSection( currentSection, 389, 393, SupportedFileMimeTypes.GIF_IMAGE_MIME ); /* 5*/
		addAvatarsToSection( currentSection, 451, 474, SupportedFileMimeTypes.GIF_IMAGE_MIME ); /*24*/
		
		//Add medium price avatars
		currentSection = startNewAvatarSection( MEDIUM_PRICE_AVATAR_COST );
		addAvatarsToSection( currentSection, 226, 255, SupportedFileMimeTypes.GIF_IMAGE_MIME ); /*30*/
		currentSection = startNewAvatarSection( MEDIUM_PRICE_AVATAR_COST );
		addAvatarsToSection( currentSection, 256, 265, SupportedFileMimeTypes.GIF_IMAGE_MIME ); /*10*/
		addAvatarsToSection( currentSection, 394, 413, SupportedFileMimeTypes.GIF_IMAGE_MIME ); /*20*/
		currentSection = startNewAvatarSection( MEDIUM_PRICE_AVATAR_COST );
		addAvatarsToSection( currentSection, 414, 420, SupportedFileMimeTypes.GIF_IMAGE_MIME ); /*7*/
		addAvatarsToSection( currentSection, 450, 450, SupportedFileMimeTypes.GIF_IMAGE_MIME ); /*1*/
		addAvatarsToSection( currentSection, 475, 496, SupportedFileMimeTypes.GIF_IMAGE_MIME ); /*22*/
		currentSection = startNewAvatarSection( MEDIUM_PRICE_AVATAR_COST );
		addAvatarsToSection( currentSection, 497, 509, SupportedFileMimeTypes.GIF_IMAGE_MIME ); /*13*/
		addAvatarsToSection( currentSection, 545, 545, SupportedFileMimeTypes.GIF_IMAGE_MIME ); /* 1*/
		
		//Add high price avatars
		currentSection = startNewAvatarSection( HIGH_PRICE_AVATAR_COST );
		addAvatarsToSection( currentSection, 266, 295, SupportedFileMimeTypes.GIF_IMAGE_MIME ); /*30*/
		currentSection = startNewAvatarSection( HIGH_PRICE_AVATAR_COST );
		addAvatarsToSection( currentSection, 296, 297, SupportedFileMimeTypes.GIF_IMAGE_MIME ); /* 2*/
		addAvatarsToSection( currentSection, 421, 448, SupportedFileMimeTypes.GIF_IMAGE_MIME ); /*28*/
		currentSection = startNewAvatarSection( HIGH_PRICE_AVATAR_COST );
		addAvatarsToSection( currentSection, 449, 449, SupportedFileMimeTypes.GIF_IMAGE_MIME ); /*1*/
		addAvatarsToSection( currentSection, 511, 539, SupportedFileMimeTypes.GIF_IMAGE_MIME ); /*29*/
	}

	private static AvatarSectionDescriptor startNewAvatarSection( final int price ) {
		//Create new section descriptor 
		AvatarSectionDescriptor section = instance.new AvatarSectionDescriptor( price, new HashMap<Integer, AvatarDescriptor>() );
		//Register the section descriptor
		avatarSections.add( section );
		//Return the section descriptor
		return section;
	}
	
	private static void addAvatarsToSection( AvatarSectionDescriptor section,
											 final int startAvatarIndex, final int endAvatarsIndex, 
											 final SupportedFileMimeTypes mimeType ) {
		//Initialize the set of avatars
		for( int index = startAvatarIndex; index <= endAvatarsIndex; index++) {
			section.avatars.put( index, instance.new AvatarDescriptor( index, section.price, mimeType ) );
		}
	}
	
	/**
	 * Allows to get the avatar descriptor by the avatar's index
	 * @param index the avatar's index
	 * @return the avatar's descriptor or null if an avatar with the given index is unknown
	 */
	public static AvatarDescriptor getAvatarDescriptor( final int index ) {
		AvatarDescriptor descriptor = null;
		for( AvatarSectionDescriptor section :  avatarSections ) {
			descriptor = section.avatars.get( index );
			if( descriptor != null ) {
				break;
			}
		}
		return descriptor;
	}
	
}
