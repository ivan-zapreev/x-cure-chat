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
 * The client utilities package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.utils;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.xcurechat.client.data.ShortUserData;
import com.xcurechat.client.rpc.ServerSideAccessManager;

/**
 * @author zapreevis
 * Is the helper class for the avatar spoilers that contains all of the required data
 */
public class AvatarSpoilersHelper {
	//The undefined avatar spoiler
	public static final int UNDEFILED_AVATAR_SPOILER_ID = 0;
	//The first avatar spoiler index/id
	public static final int MIN_AVATAR_SPOILER_INDEX = 1;
	
	//Spoiler prices 
	private static final int SPOILER_LOW_PRICE = 5;
	private static final int SPOILER_MEDIUM_PRICE = 10;
	private static final int SPOILER_HIGH_PRICE = 15;
	
	//The relative avatar spoilers location
	private static final String RELATIVE_AVATAR_SPOILERS_LOCATION = ServerSideAccessManager.USERAVATAR_RELATED_IMAGES_LOCATION +
																   "spoilers" + ServerSideAccessManager.SERVER_CONTEXT_DELIMITER;
	
	private static final AvatarSpoilersHelper instance = new AvatarSpoilersHelper();
	
	/**
	 * @author zapreevis
	 * Represents the avatar spoiler descriptor object
	 */
	public class AvatarSpoilerDescriptor {
		public final String relativeURL;
		public final int price;
		public final SupportedFileMimeTypes mimeType;
		public final int index;
		public AvatarSpoilerDescriptor( final int index, final int price, final SupportedFileMimeTypes mimeType ) {
			this.relativeURL = RELATIVE_AVATAR_SPOILERS_LOCATION + index +
							  ( mimeType == SupportedFileMimeTypes.PNG_IMAGE_MIME ? ".png" :
								( ( mimeType == SupportedFileMimeTypes.GIF_IMAGE_MIME ) ? ".gif" : ".jpg" ) );
			this.price = price;
			this.mimeType = mimeType;
			this.index = index;
		}
	}
	
	//Contains mapping from the known spoiler ids to the spoiler descriptor
	public static final Map<Integer, AvatarSpoilerDescriptor> spoilerIdToDescriptor = new LinkedHashMap<Integer, AvatarSpoilerDescriptor>();
	
	static {
		int index = MIN_AVATAR_SPOILER_INDEX;
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //01
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );	     //02
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //03
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //04
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //05
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //06
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //07
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //08
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //09
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //10
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //11
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //12
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.GIF_IMAGE_MIME );      //13
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.GIF_IMAGE_MIME );   //14
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.GIF_IMAGE_MIME );   //15
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.GIF_IMAGE_MIME );     //16
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.GIF_IMAGE_MIME );     //17
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.GIF_IMAGE_MIME );     //18
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.GIF_IMAGE_MIME );   //19
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.GIF_IMAGE_MIME );      //20
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.GIF_IMAGE_MIME );      //21
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.GIF_IMAGE_MIME );   //22
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //23
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //24
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //25
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //26
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //27
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //28
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //29
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //30
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //31
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //32
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //33
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //34
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //35
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //36
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //37
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //38
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //39
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //40
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //41
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //42
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //43
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.GIF_IMAGE_MIME );   //44
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //45
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //46
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //47
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //48
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //49
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //50
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //51
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //52
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //53
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //54
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //55
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //56
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //57
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //58
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //59
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //60
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //61
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //62
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //63
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //64
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //65
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //66
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //67
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //68
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //69
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //70
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //71
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //72
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //73
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //74
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //75
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //76
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //77
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //78
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //79
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //80
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //81
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //82
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //83
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //84
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //85
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //86
		registerAvatarSpoiler( index++, SPOILER_MEDIUM_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );   //87
		registerAvatarSpoiler( index++, SPOILER_LOW_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );      //88
		registerAvatarSpoiler( index++, SPOILER_HIGH_PRICE, SupportedFileMimeTypes.PNG_IMAGE_MIME );     //89
	}
	
	private static void registerAvatarSpoiler( final int id, final int price, final SupportedFileMimeTypes mimeType ) {
		spoilerIdToDescriptor.put( id , instance.new AvatarSpoilerDescriptor( id, price, mimeType ) );
	}
	
	/**
	 * Allows to determine if the avatar spoiler is active or not
	 * @param userData the user data contining the spoiler information
	 * @return true if the user spoiler is active, otherwise false
	 */
	public static boolean isAvatarSpoilerActive( ShortUserData userData ) {
		return isAvatarSpoilerActive( userData.getAvatarSpoilerId(), userData.getAvatarSpoilerExpDate() );
	}
	
	/**
	 * Allows to determine if the avatar spoiler is active or not
	 * @param avatarSpoilerId the avatar spoiler id
	 * @param avatarSpoilerExpDate the avatar expiration date
	 * @return true if the user spoiler is active, otherwise false
	 */
	public static boolean isAvatarSpoilerActive( final int avatarSpoilerId, final Date avatarSpoilerExpDate ) {
		return ( avatarSpoilerId != UNDEFILED_AVATAR_SPOILER_ID ) &&
			   ( avatarSpoilerExpDate != null ) && 
			   ( avatarSpoilerExpDate.after( new Date() ) );
	}
	
	/**
	 * Allows to get a relative url for the spoiler image, or a string with the id  if the provided spoiler id is not known
	 * @param spoilerID the spoiler id
	 * @return the spoiler-image relative url or a string with the id if the spoiler id is not known
	 */
	public static String getSpoilerRelativeURL( final int spoilerID ) {
		AvatarSpoilerDescriptor descriptor = spoilerIdToDescriptor.get( spoilerID );
		return  ( descriptor == null) ? ""+spoilerID : descriptor.relativeURL;
	}
	
	/**
	 * Allows to get price of the spoiler image, or zero if the provided spoiler id is not known
	 * @param spoilerID the spoiler id
	 * @return the spoiler-image price or zero if the spoiler id is not known
	 */
	public static int getSpoilerPrice( final int spoilerID ) {
		AvatarSpoilerDescriptor descriptor = spoilerIdToDescriptor.get( spoilerID );
		return  ( descriptor == null) ? 0 : descriptor.price;
	}
	
	/**
	 * Allows to get the new spoiler expiration date
	 * @return the new spoiler expiration date
	 */
	public static Date getNewSpoilerExpirationDate() {
		//Currently sets to 24 hours
		return new Date( System.currentTimeMillis() + 24 * 60 * 60 * 1000 );
	}
}
