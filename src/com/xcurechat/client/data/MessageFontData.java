/**
 * The user data objects package.
 * (C) Ivan S. Zapreev, 2009
 */
package com.xcurechat.client.data;

import java.util.HashMap;
import java.util.Map;

import com.xcurechat.client.i18n.I18NManager;
import com.xcurechat.client.i18n.UITitlesI18N;

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
 * @author zapreevis
 * Contains the chat message's font data
 */
public class MessageFontData {
	
	//The available font types, the default font type is serif
	public static final int SERIF_FONT_TYPE = 0;
	public static final int SANS_SERIF_FONT_TYPE = SERIF_FONT_TYPE + 1;
	public static final int FANTASY_SERIF_FONT_TYPE = SANS_SERIF_FONT_TYPE + 1;
	public static final int CURSIVE_FONT_TYPE = FANTASY_SERIF_FONT_TYPE + 1;
	public static final int MONOSPACE_FONT_TYPE = CURSIVE_FONT_TYPE + 1;
	public static final int DEFAULT_FONT_FAMILY = SANS_SERIF_FONT_TYPE;
	//The mapping from the font types to corresponding css classes
	public static final Map<Integer, String> fontTypeToCssClass = new HashMap<Integer, String>();
	//The mapping from the font types to corresponding font family names
	public static final Map<Integer, String> fontTypeToFontFamilyName = new HashMap<Integer, String>();

	//The available font sizes, the default font size is medium
	public static final int X_SMALL_FONT_SIZE = 0;
	public static final int SMALL_FONT_SIZE = X_SMALL_FONT_SIZE + 1;
	public static final int MEDIUM_FONT_SIZE = SMALL_FONT_SIZE + 1;
	public static final int LARGE_FONT_SIZE = MEDIUM_FONT_SIZE + 1;
	public static final int X_LARGE_FONT_SIZE = LARGE_FONT_SIZE + 1;
	public static final int DEFAULT_FONT_SIZE = MEDIUM_FONT_SIZE;
	//The mapping from the font sizes to corresponding css classes
	public static final Map<Integer, String> fontSizeToCssClass = new HashMap<Integer, String>();
	//The mapping from the font sizes to corresponding size name
	public static final Map<Integer, String> fontSizeToSizeName = new HashMap<Integer, String>();
	
	//The available font colors, the default font color means that we user the message's default CSS
	public static final int UNKNOWN_FONT_COLOR = 0;
	public static final int FONT_COLOR_THREE = UNKNOWN_FONT_COLOR + 1;
	public static final int FONT_COLOR_FOUR = FONT_COLOR_THREE + 1;
	public static final int FONT_COLOR_TWO = FONT_COLOR_FOUR + 1;
	public static final int FONT_COLOR_SIX = FONT_COLOR_TWO + 1;
	public static final int FONT_COLOR_ONE = FONT_COLOR_SIX + 1;
	public static final int FONT_COLOR_FIVE = FONT_COLOR_ONE + 1;
	public static final int FONT_COLOR_SEVEN = FONT_COLOR_FIVE + 1;
	public static final int DEFAULT_FONT_COLOR = FONT_COLOR_ONE;
	//The mapping from the font colors to corresponding css classes
	public static final Map<Integer, String> fontColorToCssClass = new HashMap<Integer, String>();
	//The mapping from the font colors to corresponding font color name
	public static final Map<Integer, String> fontColorToColorName = new HashMap<Integer, String>();
	
	static {
		UITitlesI18N titlesI18N = I18NManager.getTitles();
		
		//Initialize font types
		fontTypeToCssClass.put( SERIF_FONT_TYPE, "xcure-Chat-Chat-Message-Font-Serif" );
		fontTypeToFontFamilyName.put( SERIF_FONT_TYPE, titlesI18N.fontSerifTitile() );
		fontTypeToCssClass.put( SANS_SERIF_FONT_TYPE, "xcure-Chat-Chat-Message-Font-Sans-Serif" );
		fontTypeToFontFamilyName.put( SANS_SERIF_FONT_TYPE, titlesI18N.fontSansSerifTitile() );
		fontTypeToCssClass.put( FANTASY_SERIF_FONT_TYPE, "xcure-Chat-Chat-Message-Font-Fantasy" );
		fontTypeToFontFamilyName.put( FANTASY_SERIF_FONT_TYPE, titlesI18N.fontFantasyTitile() );
		fontTypeToCssClass.put( CURSIVE_FONT_TYPE, "xcure-Chat-Chat-Message-Font-Cursive" );
		fontTypeToFontFamilyName.put( CURSIVE_FONT_TYPE, titlesI18N.fontCursiveTitile() );
		fontTypeToCssClass.put( MONOSPACE_FONT_TYPE, "xcure-Chat-Chat-Message-Font-Monospace" );
		fontTypeToFontFamilyName.put( MONOSPACE_FONT_TYPE, titlesI18N.fontMonospaceTitile() );
		
		//Initialize font sizes
		fontSizeToCssClass.put( MEDIUM_FONT_SIZE, "xcure-Chat-Chat-Message-Font-Medium" );
		fontSizeToSizeName.put( MEDIUM_FONT_SIZE, titlesI18N.fontMediumTitile() );
		fontSizeToCssClass.put( X_SMALL_FONT_SIZE, "xcure-Chat-Chat-Message-Font-X-Small" );
		fontSizeToSizeName.put( X_SMALL_FONT_SIZE, titlesI18N.fontXSmallTitile() );
		fontSizeToCssClass.put( SMALL_FONT_SIZE, "xcure-Chat-Chat-Message-Font-Small" );
		fontSizeToSizeName.put( SMALL_FONT_SIZE, titlesI18N.fontSmallTitile() );
		fontSizeToCssClass.put( LARGE_FONT_SIZE, "xcure-Chat-Chat-Message-Font-Large" );
		fontSizeToSizeName.put( LARGE_FONT_SIZE, titlesI18N.fontLargeTitile() );
		fontSizeToCssClass.put( X_LARGE_FONT_SIZE, "xcure-Chat-Chat-Message-Font-X-Large" );
		fontSizeToSizeName.put( X_LARGE_FONT_SIZE, titlesI18N.fontXLargeTitile() );

		//Initialize font colors
		fontColorToCssClass.put( FONT_COLOR_THREE, "xcure-Chat-Chat-Message-Font-Color-Three");
		fontColorToColorName.put( FONT_COLOR_THREE, titlesI18N.fontColorThreeTitile() );
		fontColorToCssClass.put( FONT_COLOR_FOUR, "xcure-Chat-Chat-Message-Font-Color-Four");
		fontColorToColorName.put( FONT_COLOR_FOUR, titlesI18N.fontColorFourTitile() );
		fontColorToCssClass.put( FONT_COLOR_TWO, "xcure-Chat-Chat-Message-Font-Color-Two");
		fontColorToColorName.put( FONT_COLOR_TWO, titlesI18N.fontColorTwoTitile() );
		fontColorToCssClass.put( FONT_COLOR_SIX, "xcure-Chat-Chat-Message-Font-Color-Six");
		fontColorToColorName.put( FONT_COLOR_SIX, titlesI18N.fontColorSixTitile() );
		fontColorToCssClass.put( FONT_COLOR_SEVEN, "xcure-Chat-Chat-Message-Font-Color-Seven");
		fontColorToColorName.put( FONT_COLOR_SEVEN, titlesI18N.fontColorSevenTitile() );
		fontColorToCssClass.put( FONT_COLOR_ONE, "xcure-Chat-Chat-Message-Font-Color-One");
		fontColorToColorName.put( FONT_COLOR_ONE, titlesI18N.fontColorOneTitile() );
		fontColorToCssClass.put( FONT_COLOR_FIVE, "xcure-Chat-Chat-Message-Font-Color-Five");
		fontColorToColorName.put( FONT_COLOR_FIVE, titlesI18N.fontColorFiveTitile() );
	}
	
	public static String getFontTypeStyle( final int fontType ) { 
		return fontTypeToCssClass.get( fontType );
	}
	
	public static String getFontSizeStyle( final int fontSize ) {
		return fontSizeToCssClass.get( fontSize );
	}
	
	public static String getFontColorStyle( final int fontColor ) {
		return fontColorToCssClass.get( fontColor );
	}

}
