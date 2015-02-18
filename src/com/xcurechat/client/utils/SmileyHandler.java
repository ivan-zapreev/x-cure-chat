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

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;



import com.xcurechat.client.rpc.ServerSideAccessManager;

/**
 * @author zapreevis
 * This class is responsible for handling Smiles in the chat messages.
 * First when the message is sent it converts the smile into the internal smile code.
 * Second when the message is received by the user it converts the message into the
 * list of Widgets, text and images for smiles.
 */
public class SmileyHandler {
	
	/**
	 * @author zapreevis
	 * Represents basic info about the smiley category
	 */
	public class SmileyCategoryInfo {
		public final int categoryID;
		public final int minGold;
		public SmileyCategoryInfo(final int categoryID, final int minGold) {
			this.categoryID = categoryID;
			this.minGold = minGold;
		}
	}
	
	/**
	 * @author zapreevis
	 * Represents basic info about a single smile
	 */
	public class SmileyInfo {
		public final String url;
		public final int smileyCodeID;
		public int width;
		public int height;
		public final int price;
		public final SmileyCategoryInfo category;
		
		public SmileyInfo( final int fileID, final int width,
						   final int height, final int price,
						   final SmileyCategoryInfo category ) {
			this.url = ServerSideAccessManager.SMILEY_IMAGES_LOCATION + fileID + ".gif";
			this.smileyCodeID = fileID;
			setSmileySize( width, height );
			this.price = price;
			this.category = category;
		}
		
		public void setSmileySize( final int width, final int height ) {
			this.width = width;
			this.height = height;
		}
	}
	
	//This maps the internal smile code to the smile info
	private static final Map<Integer, SmileyInfo> smileyCodeToInfo = new HashMap<Integer, SmileyInfo>();
	
	//This maps the internal smile code to the corresponding Strings
	private static final Map<Integer, List<String>> smileyCodeToSmileStrings = new HashMap<Integer, List<String>>();
	
	//Mapping regular smiles to internal smile ids
	private static final Map<String,Integer> smileyToSmileyCode = new HashMap<String, Integer>();
	
	//Mapping of the category name to the list of smile info objects
	private static final Map<SmileyCategoryInfo, List<SmileyInfo>> categoryToInfo = new HashMap<SmileyCategoryInfo, List<SmileyInfo>>();
	
	//Stores the sorted, by length, list of smile strings, the longer
	//strings are stored in the beginning i.e. the order is descending
	private static String[] sortedSmileyStrings = null;
	
	//The class instance needed for static initialized
	private static SmileyHandler instance = new SmileyHandler();
	
	//Current smiley code, needed for registering the smiley codes
	private static int currentSmileyCode = 0;
	
	/**
	 * This method allows to sort the smile strings by length in descending order
	 * This is needed to resolve cases when one smile string is a substring of another one
	 */
	private static void sortSmileyStrings() {
		sortedSmileyStrings = new String[ smileyToSmileyCode.keySet().size() ];
		sortedSmileyStrings = smileyToSmileyCode.keySet().toArray( sortedSmileyStrings );
		Arrays.sort( sortedSmileyStrings, new Comparator<String>(){
			@Override
			public int compare(String arg0, String arg1) {
				//NOTE: we say that arg0 is smaller than arg1 if arg0 is longer than arg1
				//This is done because we need a descending order by string lengths
				return arg1.length() - arg0.length();
			}});
	}
	
	/**
	 * Allows to register the mapping between the smile string representation and its internal site code
	 * @param smileString the smile representation as a string
	 * @param smileCode the internal smile code corresponding to the smile
	 */
	private static void registerSmileToSmileCode(final String smileString ) {
		smileyToSmileyCode.put( smileString, currentSmileyCode );
		List<String> smileStrings = smileyCodeToSmileStrings.get( currentSmileyCode );
		if( smileStrings == null ) {
			smileStrings = new ArrayList<String>();
		}
		smileStrings.add( smileString );
		smileyCodeToSmileStrings.put( currentSmileyCode, smileStrings );
	}
	
	/**
	 * Returns the smile strings mapped to the given internal smile code
	 * @param smileCode the internal smile code
	 * @return the list of corresponding strings, or null if the is none
	 */
	public static List<String> getSmileStrings( final int smileCode ) {
		return smileyCodeToSmileStrings.get( smileCode );
	}
	
	/**
	 * Return the string representation of the internal smile code
	 * @param smileCode the internal smile code
	 * @return the string representation of the internal smile code
	 */
	public static String getSmileCodeString( final int smileCode ) {
		return SMILEY_WRAPPER_STRING + smileCode + SMILEY_WRAPPER_STRING;
	}
	
	//The smite category ids
	public static final int UNKNOWN_CATEGORY_ID = 0;
	public static final int FAVORITES_CATEGORY_ID = 1;
	public static final int TEASING_CATEGORY_ID = 3;
	public static final int TALKING_CATEGORY_ID = 4;
	public static final int RELAXING_CATEGORY_ID = 5;
	public static final int EMOTIONS_CATEGORY_ID = 6;
	public static final int LOVE_CATEGORY_ID = 7;
	public static final int MISC_CATEGORY_ID = 8;
	/*The presents smiley category*/
	public static final int PRESENTS_CATEGORY_ID = 2;
	public static final int PRESENTS_SMILEYS_ONE_SMILE_PRICE_GOLD = 1;
	private final static int PRESENTS_SMILEYS_MIN_GOLD = PRESENTS_SMILEYS_ONE_SMILE_PRICE_GOLD;
	/*Funny animated smileys*/
	public static final int FUNNY_CATEGORY_ID = 9;
	private final static int FUNNY_SMILEYS_ONE_SMILE_PRICE_GOLD = 1;
	private final static int FUNNY_SMILEYS_MIN_GOLD = FUNNY_SMILEYS_ONE_SMILE_PRICE_GOLD;
	/*Faces smileys*/
	public final static int FACES_CATEGORY_ID = 12;
	private final static int FACES_SMILEYS_ONE_SMILE_PRICE_GOLD = 1; 
	private final static int FACES_SMILEYS_MIN_GOLD = FACES_SMILEYS_ONE_SMILE_PRICE_GOLD; 
	/*Tits smileys*/
	public final static int TITS_CATEGORY_ID = 18;
	private final static int TITS_SMILEYS_ONE_SMILE_PRICE_GOLD = 1; 
	private final static int TITS_SMILEYS_MIN_GOLD = TITS_SMILEYS_ONE_SMILE_PRICE_GOLD; 
	/*Ass smileys*/
	public final static int ASS_CATEGORY_ID = 11;
	private final static int ASS_SMILEYS_ONE_SMILE_PRICE_GOLD = 2; 
	private final static int ASS_SMILEYS_MIN_GOLD = ASS_SMILEYS_ONE_SMILE_PRICE_GOLD; 
	/*Oral smileys*/
	public final static int ORAL_CATEGORY_ID = 17;
	private final static int ORAL_SMILEYS_ONE_SMILE_PRICE_GOLD = 2; 
	private final static int ORAL_SMILEYS_MIN_GOLD = ORAL_SMILEYS_ONE_SMILE_PRICE_GOLD; 
	/*Masturbate smileys*/
	public final static int MASTURBATE_CATEGORY_ID = 16;
	private final static int MASTURBATE_SMILEYS_ONE_SMILE_PRICE_GOLD = 2; 
	private final static int MASTURBATE_SMILEYS_MIN_GOLD = MASTURBATE_SMILEYS_ONE_SMILE_PRICE_GOLD; 
	/*Hardcore smileys*/
	public final static int HARDCORE_CATEGORY_ID = 14;
	private final static int HARDCORE_SMILEYS_ONE_SMILE_PRICE_GOLD = 3; 
	private final static int HARDCORE_SMILEYS_MIN_GOLD = HARDCORE_SMILEYS_ONE_SMILE_PRICE_GOLD; 
	/*Anal smileys*/
	public final static int ANAL_CATEGORY_ID = 10;
	private final static int ANAL_SMILEYS_ONE_SMILE_PRICE_GOLD = 3; 
	private final static int ANAL_SMILEYS_MIN_GOLD = ANAL_SMILEYS_ONE_SMILE_PRICE_GOLD; 
	/*GAY smileys*/
	public final static int GAY_CATEGORY_ID = 13;
	private final static int GAY_SMILEYS_ONE_SMILE_PRICE_GOLD = 3; 
	private final static int GAY_SMILEYS_MIN_GOLD = GAY_SMILEYS_ONE_SMILE_PRICE_GOLD; 
	
	//The current smile category being filled with smiles
	private static SmileyCategoryInfo currentSmileCategory = instance.new SmileyCategoryInfo( UNKNOWN_CATEGORY_ID, 0 );
	
	/**
	 * Allows to start filling a new smile category
	 * @param smileCategory the smiley category name
	 */
	private static void startNewSmyleCategory( final int categoryID ) {
		startNewSmyleCategory( categoryID, 0 );
	}
	
	/**
	 * Allows to start filling a new smile category
	 * @param categoryID the smiley category id
	 * @param minNumGold the access to this category starts from this minimum amount of gold pieces in the user's treasure wallet 
	 */
	private static void startNewSmyleCategory( final int categoryID, final int minNumGold ) {
		currentSmileCategory = instance.new SmileyCategoryInfo( categoryID, minNumGold);
		categoryToInfo.put( currentSmileCategory, new ArrayList<SmileyInfo>() );
	}
	
	public static final SmileyCategoryInfo SMILE_FAVORITES_CATEGORY_INFO = instance.new SmileyCategoryInfo( FAVORITES_CATEGORY_ID , 0 );
	
	/**
	 * Allows to initialize the favorites category for the smiles
	 */
	private static void startFavoritesCategory() {
		categoryToInfo.put( SMILE_FAVORITES_CATEGORY_INFO, new ArrayList<SmileyInfo>() );
	}
	
	/**
	 * Sorts the smileys inside the categories for a better layout
	 */
	private static void sortSmileysInCategories() {
		Iterator<SmileyCategoryInfo> iter = categoryToInfo.keySet().iterator();
		final QuickSort<SmileyInfo> sort = new QuickSort<SmileyInfo>( new Comparator<SmileyInfo>(){
			@Override
			public int compare(SmileyInfo o1, SmileyInfo o2) {
				if( o1.height < o2.height ) {
					return -1;
				} else {
					if( o1.height > o2.height ) {
						return 1;
					} else {
						return 0;
					}
				}
			}
		});
		while( iter.hasNext() ) {
			sort.quicksort( categoryToInfo.get( iter.next() ) );
		}
	}
	
	/**
	 * Returns the mapping between the smile category names and the internal smile
	 * coded for the smiles that belong to the category.
	 * @return the category to smile internal codes mapping
	 */
	public static Map<SmileyCategoryInfo,List<SmileyInfo>> getCategoryToSmileInternalCodesMapping() {
		return categoryToInfo;
	}
	
	/**
	 * Returns the list of smile information objects for the given category or null if the category is not known.
	 * @return the list of smile information objects for the given category or null
	 */
	public static List<SmileyInfo> getSmileInfoByCategory( final SmileyCategoryInfo category ) {
		return categoryToInfo.get( category );
	}
	
	/**
	 * Allows to get the collection of known smiley categories 
	 * @return the collection of known smiley categories 
	 */
	public static Collection<SmileyCategoryInfo> getCategories() {
		return categoryToInfo.keySet();
	}
	
	/**
	 * Allows to return the smile code strings for the internal smile code
	 * The result does not include the string representation of the internal smile code
	 * @param smileInternalCode the internal smile code
	 * @return the list of smile strings mapped to this code or null if there is none
	 */
	public static List<String> getSmileCodeStrings( final int smileInternalCode ) {
		return smileyCodeToSmileStrings.get( smileInternalCode );
	}
	
	/**
	 * Allows to update the size of the registered smiley. Does nothing if the smiley is not registered.
	 * @param smileFileId he smiley's file id
	 * @param width the new smiley width
	 * @param height the new smiley height
	 */
	public static void updateRegisteredSmileySize( final int smileFileId, final int width, final int height ) {
		SmileyInfo smileInfo = smileyCodeToInfo.get( smileFileId );
		if ( smileInfo != null ) {
			smileInfo.setSmileySize( width, height );
		}
	}
	
	/**
	 * Allows to get the smiley info for the given smile code, or null if it is not found
	 * @param smileyCode the smile code
	 * @return the smile info or null if such smile code is not known
	 */
	public static SmileyInfo getSmileyInfo( final int smileyCode ) {
		return smileyCodeToInfo.get( smileyCode );
	}
	
	/**
	 * Register a new smile that can be a favorite or not
	 */
	private static void registerNewSmileIcon( final int fileID, final int width, final int height,
											  final int price, boolean isFavorite ) {
		final SmileyInfo smileInfo = instance.new SmileyInfo( fileID, width, height, price, currentSmileCategory );
		//Set the current smiley code to be the smiley file ID
		currentSmileyCode = smileInfo.smileyCodeID;
		//Register the info object
		smileyCodeToInfo.put( currentSmileyCode, smileInfo );
		//Add to the current smile category
		List<SmileyInfo> categorySmileList = categoryToInfo.get( currentSmileCategory );
		categorySmileList.add( smileInfo );
		categoryToInfo.put( currentSmileCategory, categorySmileList );
		//Add to the Favorites category, if needed
		if( isFavorite ) {
			List<SmileyInfo> favoriteSmileCategory = categoryToInfo.get( SMILE_FAVORITES_CATEGORY_INFO );
			if( favoriteSmileCategory == null ) {
				favoriteSmileCategory = new ArrayList<SmileyInfo>();
			}
			favoriteSmileCategory.add( smileInfo );
			categoryToInfo.put( SMILE_FAVORITES_CATEGORY_INFO, favoriteSmileCategory );
		}
	}
	
	/**
	 * Add a new non-favorite smile with a price
	 */
	private static void registerNewSmileIcon( final int fileID, final int width,
											  final int height, final int price ) {
		registerNewSmileIcon( fileID, width, height, price, false );
	}
	
	/**
	 * Add a new favorite smile with no price
	 */
	private static void registerNewSmileIcon( final int fileID, final int width,
											  final int height, boolean isFavorite ) {
		registerNewSmileIcon( fileID, width, height, 0, isFavorite );
	}
	
	/**
	 * Add a new non-favorite smile with no price
	 */
	private static void registerNewSmileIcon( final int fileID, final int width, final int height ) {
		registerNewSmileIcon( fileID, width, height, 0, false );
	}

	static {
		/*Initialize the favorites category*/
		startFavoritesCategory();		
		
		relaxingSmileys();
		
		teasingSmileys();
		
		talkingSmileys();

		emotionSmileys();
		
		loveSmileys();
		
		miscSmileys();
		
		addPresentsSmileys();
		
		funnySmileys();

		facesSmyleys();

		titsSmileys();

		assSmileys();

		oralSmileys();

		masturbateSmileys();

		hardcoreSmileys();
		
		gaySmileys();

		analSmyles();

		//Sort the smile strings
		sortSmileyStrings();
		//Sort the smileys in the categories for a better layour;
		sortSmileysInCategories();
	}

	private static void gaySmileys() {
		/*Gay&lesbo smileys*/
		startNewSmyleCategory( GAY_CATEGORY_ID, GAY_SMILEYS_MIN_GOLD );
		for( int index=464; index<=484; index++ ) {
			registerNewSmileIcon( index, 50, 50, GAY_SMILEYS_ONE_SMILE_PRICE_GOLD );
		}
	}
	
	private static void hardcoreSmileys() {
		/*Hardcore smileys*/
		startNewSmyleCategory( HARDCORE_CATEGORY_ID, HARDCORE_SMILEYS_MIN_GOLD );
		for( int index=445; index<=463; index++ ) {
			registerNewSmileIcon( index, 50, 50, HARDCORE_SMILEYS_ONE_SMILE_PRICE_GOLD );
		}
	}

	private static void masturbateSmileys() {
		/*Masturbate smileys*/
		startNewSmyleCategory( MASTURBATE_CATEGORY_ID, MASTURBATE_SMILEYS_MIN_GOLD );
		for( int index=419; index<=444; index++ ) {
			registerNewSmileIcon( index, 50, 50, MASTURBATE_SMILEYS_ONE_SMILE_PRICE_GOLD );
		}
		//Adjust some of the selected resolution values
		updateRegisteredSmileySize( 341, 50, 47 );
	}

	private static void oralSmileys() {
		/*Oral smileys*/
		startNewSmyleCategory( ORAL_CATEGORY_ID, ORAL_SMILEYS_MIN_GOLD );
		for( int index=386; index<=418; index++ ) {
			registerNewSmileIcon( index, 50, 50, ORAL_SMILEYS_ONE_SMILE_PRICE_GOLD );
		}
	}

	private static void assSmileys() {
		/*Ass smileys*/
		startNewSmyleCategory( ASS_CATEGORY_ID, ASS_SMILEYS_MIN_GOLD );
		for( int index=354; index<=385; index++ ) {
			registerNewSmileIcon( index, 50, 50, ASS_SMILEYS_ONE_SMILE_PRICE_GOLD );
		}
	}

	private static void titsSmileys() {
		/*TITS smileys*/
		startNewSmyleCategory( TITS_CATEGORY_ID, TITS_SMILEYS_MIN_GOLD );
		for( int index=332; index<=353; index++ ) {
			registerNewSmileIcon( index, 50, 50, TITS_SMILEYS_ONE_SMILE_PRICE_GOLD );
		}
		//Adjust some of the selected resolution values
		updateRegisteredSmileySize( 341, 50, 38 );
	}

	private static void facesSmyleys() {
		/*Faces smileys*/
		startNewSmyleCategory( FACES_CATEGORY_ID, FACES_SMILEYS_MIN_GOLD );
		for( int index=311; index<=331; index++ ) {
			registerNewSmileIcon( index, 50, 50, FACES_SMILEYS_ONE_SMILE_PRICE_GOLD );
		}
	}

	private static void analSmyles() {
		/*Anal smileys*/
		startNewSmyleCategory( ANAL_CATEGORY_ID, ANAL_SMILEYS_MIN_GOLD );
		for( int index=284; index<=310; index++ ) {
			registerNewSmileIcon( index, 50, 50, ANAL_SMILEYS_ONE_SMILE_PRICE_GOLD );
		}

		//Adjust some of the selected resolution values
		updateRegisteredSmileySize( 287, 50, 51 );
		updateRegisteredSmileySize( 293, 50, 46 );
	}

	private static void funnySmileys() {
		/*Funny-based smileys*/
		startNewSmyleCategory( FUNNY_CATEGORY_ID, FUNNY_SMILEYS_MIN_GOLD );
		for( int index=133; index<=150; index++ ) {
			registerNewSmileIcon( index, 50, 50, FUNNY_SMILEYS_ONE_SMILE_PRICE_GOLD );
		}
		/*Put other smileys like that*/
		for( int index=214; index<=237; index++ ) {
			registerNewSmileIcon( index, 50, 50, FUNNY_SMILEYS_ONE_SMILE_PRICE_GOLD );
		}
		//Adjust some of the selected resolution values
		updateRegisteredSmileySize( 136, 48, 48 );
		updateRegisteredSmileySize( 142, 50, 38 );
		updateRegisteredSmileySize( 233, 60, 47 );
	}

	private static void miscSmileys() {
		/*Miscellaneous smiles*/
		startNewSmyleCategory( MISC_CATEGORY_ID );
		registerNewSmileIcon( 18, 45, 37 ); /*Finger inside the nose*/
		registerNewSmileIcon( 42, 69, 32, true ); /*Spinning head*/
		registerNewSmileIcon( 51, 58, 32 ); /*Did something bad*/
		registerSmileToSmileCode("}:>");
		registerSmileToSmileCode("}:->");
		registerNewSmileIcon( 68, 80, 32 ); /*Oh no no no !!!*/
		registerSmileToSmileCode("|:o");
		registerNewSmileIcon( 75, 49, 32 ); /*Thinking hard*/
		registerSmileToSmileCode(":-\\");
		registerNewSmileIcon( 188, 49, 32 ); /*Thinking hard with a tongue out*/
		registerSmileToSmileCode(":~/");
		registerNewSmileIcon( 95, 42, 32 ); /*Ah this is boring or stupid or unimportant*/
		registerSmileToSmileCode("\\-o");
		registerNewSmileIcon( 28, 42, 35 ); /*Smoking weed*/
		registerNewSmileIcon( 44, 50, 43 ); /*Smoking hard*/
		registerNewSmileIcon( 110, 32, 32 ); /*I am sort of dumb*/
		registerNewSmileIcon( 83, 41, 32 ); /*Chewing smile*/
		registerNewSmileIcon( 107, 32, 32, true ); /*Thinking and moving eyes*/
		registerNewSmileIcon( 108, 37, 32 ); /*Thinking and touching the chin*/
		registerNewSmileIcon( 184, 58, 32, true ); /*Smiling and waving hello*/
		//registerNewSmileIcon( 132, 28, 30 ); /*X sign*/
		registerNewSmileIcon( 60, 209, 32 ); /*A vampire*/
		registerNewSmileIcon( 8, 113, 49 ); /*High five!!!*/
		registerSmileToSmileCode("(h5)");
		registerSmileToSmileCode("^5");
		registerNewSmileIcon( 69, 61, 48 ); /*It is cold here*/
		registerNewSmileIcon( 37, 57, 51 ); /*Putting a crown on myself*/
		//registerNewSmileIcon( 204, 79, 67 ); /*I am ill*/
		registerNewSmileIcon( 196, 36, 50 ); /*Shitting hard*/
		registerNewSmileIcon( 194, 67, 38 ); /*I am programming*/
		registerNewSmileIcon( 128, 50, 50 ); /*Middle finger*/
		registerNewSmileIcon( 129, 50, 50 ); /*Gun*/
		registerNewSmileIcon( 130, 50, 50 ); /*Skull*/
		//registerNewSmileIcon( 131, 60, 55 ); /*Question mark*/
		registerNewSmileIcon( 246, 78, 61 ); /**/
		registerNewSmileIcon( 247, 138, 50 ); /**/
		registerNewSmileIcon( 248, 89, 41 ); /**/
		registerNewSmileIcon( 249, 134, 62 ); /**/
		registerNewSmileIcon( 250, 64, 81 ); /**/
		registerNewSmileIcon( 251, 32, 32, true ); /**/
		registerNewSmileIcon( 252, 92, 46, true ); /**/
		registerNewSmileIcon( 253, 60, 61, true ); /**/
		registerNewSmileIcon( 254, 53, 85 ); /**/
		registerNewSmileIcon( 255, 107, 84 ); /**/
		registerNewSmileIcon( 256, 70, 56 ); /**/
		//registerNewSmileIcon( 257, 264, 104 ); /**/
		registerNewSmileIcon( 258, 74, 32 ); /**/
		//registerNewSmileIcon( 259, 140, 140 ); /**/
		registerNewSmileIcon( 260, 108, 63 ); /**/
		registerNewSmileIcon( 261, 118, 50 ); /**/
		registerNewSmileIcon( 262, 43, 43 ); /**/
		registerNewSmileIcon( 263, 72, 55 ); /**/
		registerNewSmileIcon( 264, 49, 32 ); /**/
		registerNewSmileIcon( 265, 43, 69 ); /**/
		//registerNewSmileIcon( 266, 179, 91 ); /**/
		registerNewSmileIcon( 267, 104, 58 ); /**/
		registerNewSmileIcon( 268, 90, 64, true ); /**/
		registerNewSmileIcon( 269, 94, 53, true ); /**/
		registerNewSmileIcon( 270, 66, 63 ); /**/
		registerNewSmileIcon( 511, 76, 56 ); /**/
		registerNewSmileIcon( 512, 100, 56 ); /**/
		registerNewSmileIcon( 513, 86, 50 ); /**/
		registerNewSmileIcon( 514, 65, 40, true ); /**/
		registerNewSmileIcon( 515, 99, 55 ); /**/
		registerNewSmileIcon( 516, 83, 35 ); /**/
		registerNewSmileIcon( 517, 49, 49 ); /**/
		registerNewSmileIcon( 518, 78, 41 ); /**/
		registerNewSmileIcon( 519, 82, 32 ); /**/
		registerNewSmileIcon( 520, 75, 70 ); /**/
		registerNewSmileIcon( 521, 33, 39 ); /**/
		registerNewSmileIcon( 522, 120, 45 ); /**/
		registerNewSmileIcon( 523, 74, 55 ); /**/
		
		/*Sound smiles*/
		/*startNewSmyleCategory( I18NManager.getTitles().soundSmileCagtegoryTitle() );*/
		
		/*Silence smiles*/
		registerNewSmileIcon( 12, 45, 32 ); /*Mouth zapped, I am not telling anyone*/
		registerSmileToSmileCode(":-X");
		registerSmileToSmileCode(":X");
		//registerNewSmileIcon( 67, 32, 32 ); /*Tsss do not talk, finger-mouth*/
		registerNewSmileIcon( 109, 32, 38 ); /*Tsss do not talk, finger-mouth expressive smile*/
		registerSmileToSmileCode(":-x");
		registerSmileToSmileCode(":x");
		//registerNewSmileIcon( 50, 32, 32 ); /*Patch on the mouth, not talking*/
		//registerNewSmileIcon( 45, 32, 32, true ); /*No no, I will not say anything*/
		registerNewSmileIcon( 66, 32, 32 ); /*Hm, don't know what to say*/
		registerSmileToSmileCode(":-|");
		registerSmileToSmileCode(":-/");
		registerNewSmileIcon( 192, 75, 32 ); /*I am too cool to reply to that*/
		registerSmileToSmileCode(":|");
		registerNewSmileIcon( 198, 45, 37 ); /*Big non-talking smile*/
		
		/*Yelling, Screaming, shouting smiles and surprised*/
		registerNewSmileIcon( 71, 32, 32 ); /*Yelling in horror*/
		registerSmileToSmileCode(":o");
		registerSmileToSmileCode(":-o");
		registerNewSmileIcon( 91, 49, 32, true ); /*Yelling in anger*/
		registerSmileToSmileCode(":O");
		registerSmileToSmileCode(":-O");
		registerNewSmileIcon( 103, 60, 42 ); /*Hooray smile*/
		registerNewSmileIcon( 106, 32, 32 ); /*Wooow smile, big eyes*/
	}

	private static void loveSmileys() {
		/*Love smiles*/
		startNewSmyleCategory( LOVE_CATEGORY_ID  );
		registerNewSmileIcon( 27, 76, 32 ); /*A girl kissing a boy*/
		registerSmileToSmileCode(":-*");
		registerSmileToSmileCode(":-)*(-:");
		registerNewSmileIcon( 40, 92, 32 ); /*Hearts and hugs*/
		registerSmileToSmileCode("(H)");
		registerSmileToSmileCode("(( )):**");
		registerNewSmileIcon( 47, 37, 31 ); /*Just a hug*/
		registerSmileToSmileCode("(h)");
		registerNewSmileIcon( 58, 87, 41 ); /*A heart shaped present*/
		registerSmileToSmileCode("(l)");
		registerSmileToSmileCode("<3");
		registerNewSmileIcon( 79, 85, 32 ); /*I am in love with you in the PC*/
		registerNewSmileIcon( 74, 59, 32 ); /*Hearts in both eyes*/
		registerSmileToSmileCode("(L)");
		registerNewSmileIcon( 88, 69, 32 ); /*Kissing with heart*/
		registerSmileToSmileCode(":*");
		registerSmileToSmileCode(":-{}");
		registerSmileToSmileCode(":{}");
		registerSmileToSmileCode(":-#");
		registerNewSmileIcon( 186, 69, 32, true ); /*I am in love*/
		registerNewSmileIcon( 99, 98, 32 ); /*Here is a flower, I love you*/
		registerSmileToSmileCode("@-}--");
		registerSmileToSmileCode("@->--");
		registerSmileToSmileCode("@>--");
		registerNewSmileIcon( 178, 66, 32 ); /*Here is a flower for you*/
		registerNewSmileIcon( 187, 72, 32, true ); /*Here is a flowers for you*/
		registerNewSmileIcon( 171, 73, 32 ); /*Hocus-focus with the heart*/
		registerNewSmileIcon( 118, 45, 35, true ); /*Heart-smiley*/
		//registerNewSmileIcon( 119, 49, 45 ); /*With a flower*/
		registerNewSmileIcon( 120, 46, 41 ); /*With a rose*/
		registerNewSmileIcon( 121, 50, 35 ); /*In bed*/
		registerNewSmileIcon( 21, 200, 42 ); /*Pushing heart*/
		registerNewSmileIcon( 98, 189, 48 ); /*Happy valentines day*/
		registerNewSmileIcon( 112, 50, 50 ); /*Sweet love heart*/
		registerNewSmileIcon( 113, 46, 51 ); /*Taddy bear*/
		registerNewSmileIcon( 122, 50, 50 ); /*Love text*/
		registerNewSmileIcon( 123, 46, 41 ); /*Rings*/
		registerNewSmileIcon( 124, 46, 46 ); /*Rose*/
		registerNewSmileIcon( 115, 50, 50 ); /*Devil heart*/
		registerNewSmileIcon( 126, 65, 57 ); /*Heart*/
		registerNewSmileIcon( 127, 46, 39 ); /*Heart*/
		registerNewSmileIcon( 125, 50, 50 ); /*Heart*/
		registerNewSmileIcon( 114, 47, 40 ); /*Broken heart*/
		//registerNewSmileIcon( 116, 42, 36 ); /*Rotating heart*/
		registerNewSmileIcon( 117, 26, 35 ); /*Two Hearts*/
		registerNewSmileIcon( 271, 68, 35, true ); /**/
		registerNewSmileIcon( 272, 109, 49 ); /**/
		registerNewSmileIcon( 273, 225, 32 ); /**/
		registerNewSmileIcon( 274, 96, 32, true ); /**/
		registerNewSmileIcon( 275, 86, 58, true ); /**/
		registerNewSmileIcon( 276, 105, 34 ); /**/
		registerNewSmileIcon( 277, 115, 32 ); /**/
		registerNewSmileIcon( 278, 86, 45 ); /**/
		registerNewSmileIcon( 500, 102, 32 ); /**/
		registerNewSmileIcon( 501, 115, 32 ); /**/
		registerNewSmileIcon( 502, 130, 38 ); /**/
		registerNewSmileIcon( 503, 78, 32 ); /**/
		registerNewSmileIcon( 504, 83, 38 ); /**/
		registerNewSmileIcon( 505, 106, 67 ); /**/
		registerNewSmileIcon( 506, 77, 43 ); /**/
		registerNewSmileIcon( 507, 83, 32 ); /**/
		registerNewSmileIcon( 508, 38, 36 ); /**/
		registerNewSmileIcon( 509, 142, 42 ); /**/
		registerNewSmileIcon( 510, 122, 38 ); /**/
		
		registerNewSmileIcon( 548, 95, 32 ); /**/
		registerNewSmileIcon( 549, 73, 51 ); /**/
		registerNewSmileIcon( 550, 132, 32 ); /**/
		registerNewSmileIcon( 551, 53, 33, true ); /**/
		registerNewSmileIcon( 552, 53, 33, true ); /**/
		registerNewSmileIcon( 553, 127, 32 ); /**/
	}

	private static void emotionSmileys() {
		/*Crying and sad smiles*/
		startNewSmyleCategory( EMOTIONS_CATEGORY_ID  );
		//registerNewSmileIcon( 3, 32, 32 ); /*Crying a little*/
		//registerSmileToSmileCode(":'-(");
		//registerSmileToSmileCode("T__T");
		registerNewSmileIcon( 26, 32, 32 ); /*Crying nice*/
		registerSmileToSmileCode(";-(");
		registerSmileToSmileCode("((");
		registerNewSmileIcon( 46, 32, 32 ); /*Crying loud*/
		registerSmileToSmileCode(":'(");
		registerSmileToSmileCode("(((");
		registerNewSmileIcon( 73, 32, 32 ); /*Almost Crying*/
		registerSmileToSmileCode(";(");
		//registerNewSmileIcon( 80, 60, 35 ); /*Crying really hard*/
		//registerSmileToSmileCode("TT_TT");
		//registerSmileToSmileCode("((((");
		/*Sad smiles*/
		registerNewSmileIcon( 84, 32, 32 ); /*Just a sad face*/
		registerSmileToSmileCode(":(");
		registerSmileToSmileCode("u_u");
		registerNewSmileIcon( 104, 32, 32 ); /*Just a very sad face*/
		registerSmileToSmileCode(":-(");
		registerNewSmileIcon( 211, 63, 32 ); /*I am sad, some one hit me*/
				
		/*Angry smiles*/
		registerNewSmileIcon( 11, 48, 32 ); /*Angry read and steaming*/
		registerSmileToSmileCode(":@");
		registerNewSmileIcon( 14, 72, 32 ); /*Angry and pointing finger, go out*/
		registerSmileToSmileCode(":-@");
		registerNewSmileIcon( 41, 71, 32 ); /*Angry tearing heir*/
		registerSmileToSmileCode(">:-(");
		registerNewSmileIcon( 96, 46, 32 ); /*Angry pointing finger up, making a point*/
		registerNewSmileIcon( 78, 84, 32 ); /*Angry face shaking a PC*/
		registerNewSmileIcon( 23, 109, 36, true ); /*Angry smashing the table*/
		registerSmileToSmileCode(">:-(");
		registerNewSmileIcon( 90, 82, 32 ); /*Angry slapping face*/
		registerNewSmileIcon( 33, 125, 32 ); /*Angry smashing the computer*/
		registerNewSmileIcon( 199, 122, 32, true ); /*One beating another with a stick*/
		registerNewSmileIcon( 189, 78, 32 ); /*One smiley beating another*/
		registerNewSmileIcon( 97, 31, 36 ); /*Angry face rubbing hands*/
		registerSmileToSmileCode(">:-]");
		registerNewSmileIcon( 167, 32, 32 ); /*Angry face*/
		registerNewSmileIcon( 170, 39, 32 ); /*Angry face with hands*/
		registerNewSmileIcon( 173, 59, 32 ); /*Angry shouting and bumping with hands*/
		registerNewSmileIcon( 174, 72, 32 ); /*Boiling Angry*/
		registerNewSmileIcon( 193, 42, 32 ); /*A sudden burst of anger*/
		registerNewSmileIcon( 182, 32, 37 ); /*The devil face*/
		//registerNewSmileIcon( 206, 74, 36 ); /*Big smile knocking with hands*/
		
		/*Laughing and smiling smiles*/
		registerNewSmileIcon( 111, 32, 32 ); /*Just smiling to you*/
		registerSmileToSmileCode(":)");
		registerNewSmileIcon( 177, 49, 32 ); /*Smiling with thumbs up*/
		//registerNewSmileIcon( 86, 32, 32 ); /*Just a big open smile*/
		registerNewSmileIcon( 77, 37, 32 ); /*Smiling, almost laughing*/
		registerSmileToSmileCode(":-)");
		registerNewSmileIcon( 22, 73, 32 ); /*Laughing pointing finger*/
		registerSmileToSmileCode("^ ^");
		//registerNewSmileIcon( 55, 48, 32 ); /*Clamping hands*/
		registerNewSmileIcon( 176, 66, 32 ); /*Clamping hands*/
		registerNewSmileIcon( 57, 46, 32 ); /*Laughing, holding head*/
		registerSmileToSmileCode(":-D");
		registerSmileToSmileCode("))");
		registerNewSmileIcon( 59, 32, 32 ); /*Nodding and smiling*/
		registerSmileToSmileCode("^-^");
		registerNewSmileIcon( 61, 32, 32 ); /*Just laughing with open mouth*/
		registerSmileToSmileCode(":D");
		registerSmileToSmileCode("))))");
		registerNewSmileIcon( 72, 32, 32, true ); /*Big smile nodding*/
		registerSmileToSmileCode("^^");
		registerNewSmileIcon( 76, 42, 40, true ); /*Dark force, smile*/
		registerSmileToSmileCode("^__^");
		registerNewSmileIcon( 179, 58, 32 ); /*I am not watching and a smile*/
		registerNewSmileIcon( 85, 51, 32 ); /*Pointing finger, laughing loud, open mouth*/
		registerSmileToSmileCode("LOL");
		registerSmileToSmileCode(")))))");
		//registerNewSmileIcon( 93, 32, 32 ); /*Big smile teeth with brackets*/
		registerNewSmileIcon( 166, 32, 32 ); /*Smiling with a golden tooth*/
		registerSmileToSmileCode("=)");
		registerSmileToSmileCode(")))");
		registerNewSmileIcon( 161, 32, 32 ); /*Make a smile*/
		/*registerSmileToSmileCode(")");*/ //NOTE: This one messes up the braces
		registerNewSmileIcon( 175, 37, 32 ); /*Just a big crazy smile*/
		registerNewSmileIcon( 180, 33, 32 ); /*Another big crazy smile*/
		registerNewSmileIcon( 185, 32, 32 ); /*Another big crazy smile*/
		registerNewSmileIcon( 201, 90, 32 ); /*Everything is going to be all right*/
		registerNewSmileIcon( 210, 69, 32 ); /*I am magically happy*/
		
		/*Shy smiles*/
		registerNewSmileIcon( 24, 32, 32, true ); /*Clamping eyes and blushing*/
		registerSmileToSmileCode("o:-)");
		registerSmileToSmileCode("o:)");
		registerNewSmileIcon( 25, 80, 41, true ); /*Smiling and blushing angel*/
		registerSmileToSmileCode("O:-)");
		registerSmileToSmileCode("O:)");
		registerNewSmileIcon( 64, 32, 32 ); /*Just blushing*/
		registerSmileToSmileCode(":-[");
		registerNewSmileIcon( 65, 54, 32 ); /*Coming from underground and then smiling with blush*/
		registerSmileToSmileCode(":[");
		registerNewSmileIcon( 105, 32, 32 ); /*A sort of sorry-blushing face*/
		//registerNewSmileIcon( 205, 32, 32 ); /*A shy flirting smile*/
		
		registerNewSmileIcon( 279, 82, 42 ); /**/
		registerNewSmileIcon( 280, 71, 32 ); /**/
		registerNewSmileIcon( 281, 71, 32 ); /**/
		registerNewSmileIcon( 282, 97, 86 ); /**/
		registerNewSmileIcon( 283, 80, 59 ); /**/
		registerNewSmileIcon( 485, 100, 37, true ); /**/
		registerNewSmileIcon( 486, 39, 32 ); /**/
		registerNewSmileIcon( 487, 107, 42, true ); /**/
		registerNewSmileIcon( 488, 80, 42 ); /**/
		registerNewSmileIcon( 489, 36, 37 ); /**/
		registerNewSmileIcon( 490, 64, 41 ); /**/
		registerNewSmileIcon( 491, 38, 39 ); /**/
		registerNewSmileIcon( 492, 32, 32 ); /**/
		registerNewSmileIcon( 493, 102, 32, true ); /**/
		registerNewSmileIcon( 494, 33, 36 ); /**/
		registerNewSmileIcon( 495, 57, 32 ); /**/
		registerNewSmileIcon( 496, 64, 40, true ); /**/
		registerNewSmileIcon( 497, 48, 32 ); /**/
		registerNewSmileIcon( 498, 103, 38 ); /**/
		registerNewSmileIcon( 499, 79, 32 ); /**/
	}

	private static void relaxingSmileys() {
		/*Relaxing smiles*/
		startNewSmyleCategory( RELAXING_CATEGORY_ID );
		registerNewSmileIcon( 5, 117, 38 ); /*Sun, Fan, Me relaxing*/
		registerNewSmileIcon( 9, 89, 40 ); /*Hammock rest*/
		registerSmileToSmileCode("^__~"); /*Wink reluctant*/
		registerNewSmileIcon( 34, 32, 32 );
		registerSmileToSmileCode("@_@");
		registerNewSmileIcon( 43, 109, 38 ); /*Swimming in the sea*/
		registerNewSmileIcon( 49, 94, 76, true ); /*On the island*/
		registerSmileToSmileCode("(ip)");
		registerNewSmileIcon( 172, 69, 34 ); /*Sniffing a flower*/
		registerNewSmileIcon( 190, 76, 44 ); /*Eating ice cream*/
		registerNewSmileIcon( 191, 71, 43 ); /*Drinking juice*/
		//registerNewSmileIcon( 195, 53, 49 ); /*Relaxing in a chair*/
		registerNewSmileIcon( 532, 103, 38 ); /**/
		registerNewSmileIcon( 533, 113, 60 ); /**/
		registerNewSmileIcon( 534, 113, 53 ); /**/
		registerNewSmileIcon( 535, 97, 32 ); /**/
		registerNewSmileIcon( 536, 112, 43 ); /**/
		registerNewSmileIcon( 537, 106, 46 ); /**/
		registerNewSmileIcon( 538, 116, 48 ); /**/
		registerNewSmileIcon( 539, 116, 47 ); /**/
		registerNewSmileIcon( 540, 73, 32 ); /**/
		registerNewSmileIcon( 541, 99, 37 ); /**/
		
		/*The former party smileys*/
		registerNewSmileIcon( 1, 58, 32 );   /*Drunk dance*/
		registerSmileToSmileCode(":*)");
		registerNewSmileIcon( 102, 58, 32 );  /*Dancing in honor*/
		registerSmileToSmileCode("<:O)");
		registerNewSmileIcon( 168, 41, 39 );  /*Dancing smiley, clamping*/
		registerNewSmileIcon(  200, 54, 32 );  /*Cool dancing wearing glasses*/
		//registerNewSmileIcon(  203, 64, 48 );  /*Dancing with a ring around the waist*/
		registerNewSmileIcon(  169, 99, 48 );  /*Two smileys from Ireland are dancing*/
		registerNewSmileIcon(  81, 72, 32 );   /*Drinking wine*/
		registerNewSmileIcon( 82, 64, 32 );   /*Drinking beer*/
		registerNewSmileIcon( 4, 101, 32 );  /*Party small*/
		registerSmileToSmileCode("<:o)");
		registerNewSmileIcon( 63, 94, 52 );   /*Give a gift*/
		registerSmileToSmileCode("(G)");
		//registerNewSmileIcon( "06", 98, 59 );   /*Let's party, big*/
		registerNewSmileIcon( 100, 122, 42 ); /*Happy new year, two people*/
		registerNewSmileIcon( 101, 129, 32 ); /*Happy new year rainbow*/
		registerNewSmileIcon( 208, 129, 32 ); /*Happy rainbow*/
		registerNewSmileIcon( 48, 132, 68 );  /*Happy birthday*/
		registerSmileToSmileCode("(^)");
		//registerNewSmileIcon( 151, 160, 62 );  /*Walking in circles*/
		registerNewSmileIcon( 244, 103, 42, true ); /**/
		registerNewSmileIcon( 245, 128, 45 ); /**/
		registerNewSmileIcon( 524, 107, 76 ); /**/
		registerNewSmileIcon( 525, 61, 62 ); /**/
		registerNewSmileIcon( 526, 210, 48 ); /**/
		registerNewSmileIcon( 527, 105, 63 ); /**/
		registerNewSmileIcon( 528, 107, 58 ); /**/
		registerNewSmileIcon( 529, 86, 45 ); /**/
		registerNewSmileIcon( 530, 133, 73 ); /**/
		registerNewSmileIcon( 531, 66, 57 ); /**/
	}

	private static void talkingSmileys() {
		/*Talking smiles*/
		startNewSmyleCategory( TALKING_CATEGORY_ID );
		registerNewSmileIcon( 7, 81, 32 );
		registerNewSmileIcon( 16, 97, 32 );
		//registerNewSmileIcon( 17, 72, 32 );
		registerNewSmileIcon( 30, 94, 32 );
		registerNewSmileIcon( 62, 97, 32 );
		registerNewSmileIcon( 15, 172, 32 );
		registerNewSmileIcon( 31, 174, 32 );
		registerNewSmileIcon( 52, 105, 32 );
		registerNewSmileIcon( 32, 98, 32 );
		registerNewSmileIcon( 38, 112, 32 );
		registerNewSmileIcon( 35, 166, 32 );
		registerNewSmileIcon( 53, 91, 32 );
		registerNewSmileIcon( 54, 126, 32 );
		registerNewSmileIcon( 36, 187, 32 );
		registerNewSmileIcon( 87, 114, 32 );
		registerNewSmileIcon( 202, 126, 32 ); /*Help*/
		registerNewSmileIcon( 209, 143, 32 ); /*I love you text*/
		registerNewSmileIcon( 212, 97, 32 ); /*Thanks*/
		registerNewSmileIcon( 213, 108, 32 ); /*What!?*/
		registerNewSmileIcon( 243, 177, 32 ); /**/
		registerNewSmileIcon( 542, 187, 32 ); /**/
		registerNewSmileIcon( 554, 176, 45 ); /**/
	}

	private static void teasingSmileys() {
		/*Teasing smiles*/
		startNewSmyleCategory(TEASING_CATEGORY_ID );
		registerNewSmileIcon( 89, 32, 32 ); /*Do it with eyebrows*/
		registerSmileToSmileCode(";-)");
		registerNewSmileIcon( 10, 32, 32 ); /*Simple winking*/
		registerSmileToSmileCode(";)");
		registerNewSmileIcon( 20, 32, 32 ); /*Winking with dark glasses*/
		registerSmileToSmileCode("!o)");
		registerNewSmileIcon( 2, 32, 32 );  /*Tongue*/
		registerSmileToSmileCode(":p");
		registerNewSmileIcon( 13, 121, 31, true ); /*Tongue*/
		registerSmileToSmileCode(":-p");
		registerNewSmileIcon( 183, 36, 32 ); /*Lying on the side sticking the tongue out*/
		registerNewSmileIcon( 19, 91, 32 );  /*Tongue*/
		registerSmileToSmileCode(":P");
		//registerNewSmileIcon( 29, 69, 39 ); /*Tongue*/
		registerNewSmileIcon( 39, 82, 38 ); /*Kick me*/
		registerNewSmileIcon( 56, 32, 32 ); /*Uuuuuu*/
		registerNewSmileIcon( 70, 53, 32 ); /*Tongue*/
		registerNewSmileIcon( 92, 78, 32 ); /*Nose-hands-fingers*/
		registerNewSmileIcon( 94, 67, 35 ); /*Tongue*/
		registerSmileToSmileCode(":-P");
		registerNewSmileIcon( 152, 32, 32 ); /**/
		registerNewSmileIcon( 153, 40, 32 ); /**/
		registerNewSmileIcon( 207, 44, 32 ); /*A one with sucking on a sucking thing*/
		registerNewSmileIcon( 154, 32, 32 ); /*A smart ass*/
		registerNewSmileIcon( 155, 32, 32 ); /**/
		registerNewSmileIcon( 156, 32, 32 ); /**/
		registerNewSmileIcon( 157, 32, 32 ); /**/
		registerNewSmileIcon( 158, 32, 32 ); /**/
		registerNewSmileIcon( 159, 32, 32 ); /**/
		registerNewSmileIcon( 160, 32, 31 ); /**/
		registerNewSmileIcon( 162, 41, 32 ); /*A crazy one in a blue cap*/
		registerNewSmileIcon( 181, 52, 32 ); /*A crazy one in a red cap*/
		registerNewSmileIcon( 163, 32, 32 ); /**/
		//registerNewSmileIcon( 164, 32, 32 ); /**/
		registerNewSmileIcon( 165, 32, 32 ); /**/
		//registerNewSmileIcon( 197, 70, 56 ); /*Put horns on another smiley*/
		registerNewSmileIcon( 238, 91, 40 ); /**/
		registerNewSmileIcon( 239, 93, 69 ); /**/
		registerNewSmileIcon( 240, 83, 42 ); /**/
		registerNewSmileIcon( 241, 66, 40 ); /**/
		registerNewSmileIcon( 242, 50, 35 ); /**/
		registerNewSmileIcon( 543, 125, 45 ); /**/
		registerNewSmileIcon( 544, 46, 30 ); /**/
		registerNewSmileIcon( 545, 91, 37 ); /**/
		registerNewSmileIcon( 546, 114, 34 ); /**/
		registerNewSmileIcon( 547, 67, 56 ); /*XoXoXo*/
	}

	private static void addPresentsSmileys() {
		/*Presents category*/
		startNewSmyleCategory( PRESENTS_CATEGORY_ID, PRESENTS_SMILEYS_MIN_GOLD );
		for( int index=555; index<=639; index++ ) {
			registerNewSmileIcon( index, 70, 70, PRESENTS_SMILEYS_ONE_SMILE_PRICE_GOLD );
		}
	}
	
	//This symbol is placed on both sides of the internal smile code inside the message
	public static final String SMILEY_WRAPPER_STRING = "$";
	//The pattern for an internal smile code
	public static final String DIGIT_SMILEY_CODE_PATTERN = "\\d+";
	//The complete pattern from the smiley code that is the digit wrapped arround with the wrapper strings
	public static final String INTERNAL_SMILEY_CODE_PATTERN = "\\" + SMILEY_WRAPPER_STRING + DIGIT_SMILEY_CODE_PATTERN + "\\" + SMILEY_WRAPPER_STRING;
	
	/**
	 * Allows to remove all smiley codes from the text
	 * @param text the test to remove smiley codes from
	 * @return the resulting text without smiley codes, if the input text was null we return an empty string
	 */
	public static String removeAllSmileyCodes( final String text ) {
		String resultText = "";
		
		if( ( text != null ) && ( !text.trim().isEmpty() ) ) {
			resultText = text.replaceAll( INTERNAL_SMILEY_CODE_PATTERN , "");
		}
		
		return resultText;
	}
	
	/**
	 * Allows to count the price of the message by considering the price of the payed smileys in it.
	 * @param originalText the text to analyze
	 * @param userGold the current gold in the user's wallet 
	 * @return the price in gold pieces.
	 */
	public static int countTextPriceWatchCategory( final String originalText ) {
		int price = 0;
		String text = originalText;
		if( text != null ) {
			int beginIndex = -1, endIndex = -1;
			while( ( endIndex = text.indexOf( SMILEY_WRAPPER_STRING, beginIndex + 1 ) ) != -1 ) {
				if( beginIndex != -1 ) {
					final String possibleSmileyCode = text.substring( beginIndex + 1, endIndex );
					if( possibleSmileyCode.matches( DIGIT_SMILEY_CODE_PATTERN ) ) {
						//If looks like we have ourselves a smile
						try{
							SmileyInfo info = getSmileyInfo( Integer.parseInt( possibleSmileyCode ) );
							if( info != null ) {
								//Account for this smileys price
								price += info.price;
								//Truncate the message 
								if( ( endIndex + 1 ) < text.length() ) {
									text = text.substring( endIndex + 1 );
									//The message string has been truncated restart the indexes
									beginIndex = -1;
									endIndex = -1;
								} else {
									//There will be no more smiles, the text ends here
									text = "";
								}
							} else {
								//Although this looks like a valid smile code,
								//there is no such smile continue searching
								beginIndex = endIndex;
							}
						} catch ( NumberFormatException e ) {
							//This is not a valid smile integer code somehow, continue searching
							beginIndex = endIndex;
						}
					} else {
						//This was not a smile code so we move on starting from the end
						beginIndex = endIndex;
					}
				} else {
					//This is the first iteration, we need to find the so
					//we need the second boundary index for it smile index 
					beginIndex = endIndex;
				}
			}
		}
		return price;
	} 
	
	/**
	 * Allows to substitute the smiles in the message string with the smile codes.
	 * This should be done before sending the message to the server.
	 * This method avoids substituting smiley simbols in URLs
	 * NOTE: This method not very efficient but there should not be much
	 * slow down because the messahes are typically small
	 * @param message the original message
	 * @param removeFormatting if true then new lines and tab symbols are removed
	 * @return the messages with the smiles substituted with the smile codes
	 */
	public static String substituteSmilesWithSmileCodes( final String message, final boolean removeFormatting ) {
		String updatedMessage = "";
		String currentMessage = ( message == null ) ? "" : message; //Check that the message is not null
		if( removeFormatting ) {
			currentMessage.replace('\n', ' ');
			currentMessage.replace('\t', ' ');
		}
		String[] allLines = currentMessage.split("\\n");
		for( String singleLine : allLines ) {
			String[] allTokens = singleLine.split("\\s");
			for(int i = 0; i < allTokens.length; i++) {
				String token = allTokens[i];
				if( ! StringUtils.isAWebLink(token) ) {
					for( int index = 0; index < sortedSmileyStrings.length; index++ ) {
						String smileyString = sortedSmileyStrings[index];
						token = token.replace( smileyString, getSmileCodeString( smileyToSmileyCode.get( smileyString ) ) );
					}
				}
				updatedMessage += token + " ";
			}
			updatedMessage += "\n";
		}
		return updatedMessage.trim();
	}
	
	/**
	 * Allows to substitute the smiles in the message string with the smile codes.
	 * This should be done before sending the message to the server.
	 * This method avoids substituting smiley simbols in URLs
	 * NOTE: This method not very efficient but there should not be much
	 * slow down because the messahes are typically small
	 * WARNING: The message formatting is removed, i.e. new lines and bad symbols are removed
	 * @param message the original message
	 * @return the messages with the smiles substituted with the smile codes
	 */
	public static String substituteSmilesWithSmileCodes( final String message ) {
		return substituteSmilesWithSmileCodes( message, true );
	}
}
