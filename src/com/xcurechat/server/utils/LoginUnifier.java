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
 */
package com.xcurechat.server.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.xcurechat.client.data.ShortUserData;

/**
 * @author zapreevis
 * This class is responsible for taking the user login name and then
 * unifying it to an abstract login name, to prevent the users with
 * similar login names.
 * 
 * For more details on the Cyrillic (+ extensions) URF-8 (HEX) codes see
 * http://tlt.its.psu.edu/suggestions/international/bylanguage/cyrchart.html
 */
public class LoginUnifier {
	
	//Stores the substitution dictionary for creating the universal login name
	private static final Map<String, String> fromToStringDictionary = new HashMap<String, String>();
	
	private static void addStringMapping( final String fromStr, final String toStr ) {
		fromToStringDictionary.put( fromStr, toStr );
	}
	
	private static void addCharMapping( final char fromChar, final char toChar ) {
		addStringMapping( ""+fromChar, ""+toChar );
	}
	
	static {
		/* А -> A */
		addCharMapping( '\u0410', 'A' );
		/* а -> a */
		addCharMapping( '\u0430', 'a' );
		/* В -> B */
		addCharMapping( '\u0412', 'B' );
		/* Е -> E */
		addCharMapping( '\u0415', 'E' );
		/* е -> e  */
		addCharMapping( '\u0435', 'e' );
		/* Ѐ -> E */
		addCharMapping( '\u0400', 'E' );
		/* Ё -> E */
		addCharMapping( '\u0401', 'E' );
		/* Ѓ -> Г */
		addCharMapping( '\u0401', '\u0413' );
		/* Ѕ -> S */
		addCharMapping( '\u0405', 'S' );
		/* І -> I */
		addCharMapping( '\u0406', 'I' );
		/* Ї -> I */
		addCharMapping( '\u0407', 'I' );
		/* Ј -> J */
		addCharMapping( '\u0408', 'J' );
		/* Ќ -> K */
		addCharMapping( '\u040C', 'K' );
		/* Ѝ -> И */
		addCharMapping( '\u040D', '\u0418' );
		/* Ў -> Y */
		addCharMapping( '\u040E', 'Y' );
		/* ѐ -> e */
		addCharMapping( '\u0450', 'e' );
		/* ё -> e */
		addCharMapping( '\u0451', 'e' );
		/* З -> 3 */
		addCharMapping( '\u0417', '3' );
		/* з -> 3 */
		addCharMapping( '\u0437', '3' );
		/* Й -> И */
		addCharMapping( '\u0419', '\u0418' );
		/* й -> и */
		addCharMapping( '\u0439', '\u0438' );
		/* К -> K */
		addCharMapping( '\u041A', 'K' );
		/* к -> k */
		addCharMapping( '\u043A', 'k' );
		/* М -> M */
		addCharMapping( '\u041C', 'M' );
		/* м -> m */
		addCharMapping( '\u043C', 'm' );
		/* Н -> H */
		addCharMapping( '\u041D', 'H' );
		/* О -> 0 */
		addCharMapping( '\u041E', '0' );
		/* о -> 0 */
		addCharMapping( '\u043E', '0' );
		/* O -> 0 */
		addCharMapping( 'O', '0' );
		/* o -> 0 */
		addCharMapping( 'o', '0' );
		/* Р -> P */
		addCharMapping( '\u0420', 'P' );
		/* р -> p */
		addCharMapping( '\u0440', 'p' );
		/* С -> C */
		addCharMapping( '\u0421', 'C' );
		/* с -> c */
		addCharMapping( '\u0441', 'c' );
		/* Т -> T */
		addCharMapping( '\u0422', 'T' );
		/* У -> Y */
		addCharMapping( '\u0423', 'Y' );
		/* у -> y */
		addCharMapping( '\u0443', 'y' );
		/* Х -> X */
		addCharMapping( '\u0425', 'X' );
		/* х -> x */
		addCharMapping( '\u0445', 'x' );
		/* Щ -> Ш */
		addCharMapping( '\u0429', '\u0428' );
		/* щ -> ш */
		addCharMapping( '\u0449', '\u0448' );
		/* Ъ -> b */
		addCharMapping( '\u042A', 'b' );
		/* ъ -> b */
		addCharMapping( '\u044A', 'b' );
		/* Ь -> b */
		addCharMapping( '\u042C', 'b' );
		/* ь -> b */
		addCharMapping( '\u044C', 'b' );
		/* l -> 1 */
		addCharMapping( 'l', '1' );
		/* ѓ -> г */
		addCharMapping( '\u0453', '\u0433' );
		/* ѕ -> s */
		addCharMapping( '\u0455', 'S' );
		/* і -> i */
		addCharMapping( '\u0456', 'i' );
		/* ї -> i */
		addCharMapping( '\u0457', 'i' );
		/* ј -> j */
		addCharMapping( '\u0458', 'j' );
		/* ќ -> k */
		addCharMapping( '\u045C', 'k' );
		/* ѝ -> и */
		addCharMapping( '\u045D', '\u0438' );
		/* ў -> y */
		addCharMapping( '\u045E', 'y' );
		/* Ѻ -> 0 */
		addCharMapping( '\u047A', '0' );
		/* ѻ -> 0 */
		addCharMapping( '\u047B', '0' );
		/* а҃ -> a */
		addCharMapping( '\u0483', 'a' );
		/* а҄ -> a */
		addCharMapping( '\u0484', 'a' );
		/* а҅ -> a */
		addCharMapping( '\u0485', 'a' );
		/* а҆ -> a */
		addCharMapping( '\u0486', 'a' );
		/* Ҍ -> b */
		addCharMapping( '\u048C', 'b' );
		/* ҍ -> b */
		addCharMapping( '\u048D', 'b' );
		/* Ҏ -> P */
		addCharMapping( '\u048E', 'P' );
		/* ҏ -> p */
		addCharMapping( '\u048F', 'p' );
		/* Ґ -> Г */
		addCharMapping( '\u0490', '\u0413' );
		/* ґ -> г */
		addCharMapping( '\u0491', '\u0433' );
		/* Ҕ -> Б */
		addCharMapping( '\u0494', '\u0411' );
		/* ҕ -> б */
		addCharMapping( '\u0495', '\u0431' );
		/* Җ -> Ж */
		addCharMapping( '\u0496', '\u0416' );
		/* җ -> ж */
		addCharMapping( '\u0497', '\u0436' );
		/* Ҙ -> 3 */
		addCharMapping( '\u0498', '3' );
		/* ҙ -> 3 */
		addCharMapping( '\u0499', '3' );
		/* Қ -> K */
		addCharMapping( '\u049A', 'K' );
		/* қ -> k */
		addCharMapping( '\u049B', 'k' );
		/* Ҝ -> K */
		addCharMapping( '\u049C', 'K' );
		/* ҝ -> k */
		addCharMapping( '\u049D', 'k' );
		/* Ҟ -> K */
		addCharMapping( '\u049E', 'K' );
		/* ҟ -> k */
		addCharMapping( '\u049F', 'k' );
		/* Ҡ -> K */
		addCharMapping( '\u04A0', 'K' );
		/* ҡ -> k */
		addCharMapping( '\u04A1', 'k' );
		/* Ң -> H */
		addCharMapping( '\u04A2', 'H' );
		/* ң -> н */
		addCharMapping( '\u04A3', '\u043D' );
		/* Ҫ -> C */
		addCharMapping( '\u04AA', 'C' );
		/* ҫ -> c */
		addCharMapping( '\u04AB', 'c' );
		/* ҩ -> a */
		addCharMapping( '\u04A9', 'a' );
		/* Ҭ -> T */
		addCharMapping( '\u04AC', 'T' );
		/* ҭ -> т */
		addCharMapping( '\u04AD', '\u0442' );
		/* Ү -> Y */
		addCharMapping( '\u04AE', 'Y' );
		/* ү -> y */
		addCharMapping( '\u04AF', 'y' );
		/* Ҳ -> X */
		addCharMapping( '\u04B2', 'X' );
		/* ҳ -> x */
		addCharMapping( '\u04B3', 'x' );
		/* Ҷ -> Ч */
		addCharMapping( '\u04B6', '\u0427' );
		/* ҷ -> ч */
		addCharMapping( '\u04B7', '\u0447' );
		/* Һ -> h */
		addCharMapping( '\u04BA', 'h' );
		/* һ -> h */
		addCharMapping( '\u04BB', 'h' );
		/* Ҽ -> e */
		addCharMapping( '\u04BC', 'e' );
		/* ҽ -> e */
		addCharMapping( '\u04BD', 'e' );
		/* Ӏ -> I */
		addCharMapping( '\u04C0', 'I' );
		/* Ӂ -> Ж */
		addCharMapping( '\u04C1', '\u0416' );
		/* ӂ -> ж */
		addCharMapping( '\u04C2', '\u0436' );
		/* Ӄ -> K */
		addCharMapping( '\u04C3', 'K' );
		/* ӄ -> k */
		addCharMapping( '\u04C4', 'k' );
		/* Ӈ -> H */
		addCharMapping( '\u04C7', 'H' );
		/* ӈ -> н */
		addCharMapping( '\u04C8', '\u043D' );
		/* Ӌ -> Ч */
		addCharMapping( '\u04CB', '\u0427' );
		/* ӌ -> ч */
		addCharMapping( '\u04CC', '\u0447' );
		/* Ӑ -> A */
		addCharMapping( '\u04D0', 'A' );
		/* ӑ -> a */
		addCharMapping( '\u04D1', 'a' );
		/* Ӓ -> A */
		addCharMapping( '\u04D2', 'A' );
		/* ӓ -> a */
		addCharMapping( '\u04D3', 'a' );
		/* Ӗ -> E */
		addCharMapping( '\u04D6', 'E' );
		/* ӗ -> e */
		addCharMapping( '\u04D7', 'e' );
		/* Ӝ -> Ж */
		addCharMapping( '\u04DC', '\u0416' );
		/* ӝ -> ж */
		addCharMapping( '\u04DD', '\u0436' );
		/* Ӟ -> 3 */
		addCharMapping( '\u04DE', '3' );
		/* ӟ -> 3 */
		addCharMapping( '\u04DF', '3' );
		/* Ӡ -> 3 */
		addCharMapping( '\u04E0', '3' );
		/* ӡ -> 3 */
		addCharMapping( '\u04E1', '3' );
		/* Ӣ -> И */
		addCharMapping( '\u04E2', '\u0418' );
		/* ӣ -> и */
		addCharMapping( '\u04E3', '\u0438' );
		/* Ӥ -> И */
		addCharMapping( '\u04E4', '\u0418' );
		/* ӥ -> и */
		addCharMapping( '\u04E5', '\u0438' );
		/* Ӧ -> 0 */
		addCharMapping( '\u04E6', '0' );
		/* ӧ -> 0 */
		addCharMapping( '\u04E7', '0' );
		/* Ӭ -> Э */
		addCharMapping( '\u04EC', '\u042D' );
		/* ӭ -> э */
		addCharMapping( '\u04ED', '\u044D' );
		/* Ӯ -> Y */
		addCharMapping( '\u04EE', 'Y' );
		/* ӯ -> y */
		addCharMapping( '\u04EF', 'y' );
		/* Ӱ -> Y */
		addCharMapping( '\u04F0', 'Y' );
		/* ӱ -> y */
		addCharMapping( '\u04F1', 'y' );
		/* Ӳ -> Y */
		addCharMapping( '\u04F2', 'Y' );
		/* ӳ -> y */
		addCharMapping( '\u04F3', 'y' );
		/* Ӵ -> Ч */
		addCharMapping( '\u04F4', '\u0427' );
		/* ӵ -> ч */
		addCharMapping( '\u04F5', '\u0447' );
		/* Ӹ -> Ы */
		addCharMapping( '\u04F8', '\u042B' );
		/* ӹ -> ы */
		addCharMapping( '\u04F9', '\u044B' );
		
		/*Map any sequence of white spaces and underscores to the single underscore symbol*/
		addStringMapping( "[\\s|\\_|\\'|\\\"|\\-|\\`|\\,|\\.]+", "_");

		/* Ы -> bl */
		addStringMapping( "\u042B", "bl" );
		/* Ѹ ->  */
		addStringMapping( "\u0478", "0y" );
		/* ѹ ->  */
		addStringMapping( "\u0479", "0y" );

		/* | -> 1 */
		addStringMapping( "\\|", "1" );
		/* ! -> 1 */
		addStringMapping( "\\!", "1" );
	}
	
	/**
	 * Allows to create a unified, similarity safe string from the user login.
	 * This method, after the translation is made, truncates to string down to the user-isible length
	 * @param actualLogin the actual user login name
	 * @return the unified login string
	 */
	public static String getUnifiedLogin( final String actualLogin ) {
		//First substitute the strings
		String result = actualLogin;
		Iterator<Entry<String,String>> iter = fromToStringDictionary.entrySet().iterator();
		while( iter.hasNext() ) {
			Entry<String, String> entry = iter.next();
			result = result.replaceAll( entry.getKey(), entry.getValue() );
		}
		
		//After we converted the login name to the unified one, make the truncation to fit into the the user-visible login name length
		return ShortUserData.getShortLoginName( result.trim() );
	}
}
