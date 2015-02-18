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

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import com.google.gwt.user.client.ui.TextBoxBase;
import com.xcurechat.client.utils.widgets.TextMaximumSizeProgress;

/**
 * @author zapreevis
 * This class provides transliteration for the text typed inside a text object
 */
public class Transliterator implements KeyPressHandler {
	
	//Contains true if the browser used by the user is IE
	public static final boolean isInternetExplorer = InterfaceUtils.isMicrosoftIE();
	
	//Maps the Latin unicode characters to the Russian unicode characters
	private static final Map<Character,Character> latinToCyrilicSingleLetterUpper = new HashMap<Character,Character>();
	private static final Map<Character,Character> latinToCyrilicSingleLetterLower = new HashMap<Character,Character>();
	
	//Maps the last letter of the letter combination to the map between first
	//letters of the combination and the corresponding Russian symbol
	private static final Map<Character,Map<Character,Character>> lastLetterToSetOfFirstLetterToRusSymbMappings = new HashMap<Character,Map<Character,Character>>();
	
	private static void addSingleLetterMapping(final Character key, final Character value, final boolean isCapital){
		if( isCapital ) {
			latinToCyrilicSingleLetterUpper.put( key, value );
		} else {
			latinToCyrilicSingleLetterLower.put( key, value );
		}
	}
	
	/**
	 * This allows to add the mapping from the letter combination to the Russian symbol
	 * The letter combination consists of two letters the first one is already in Russian
	 * and the second is the modifier that turns the letter into another Russian letter
	 * @param firstLetter the first letter in the combination
	 * @param lastLetter the last letter that closes the letter combination
	 * @param rusSymbol the UTF-8 code of the corresponding Russian symbol
	 */
	private static void addLetterCombinationMapping( final Character firstLetter,
													 final Character lastLetter,
													 final Character rusSymbol ) {
		Map<Character,Character> firstLettersMap = lastLetterToSetOfFirstLetterToRusSymbMappings.get( lastLetter );
		if( firstLettersMap == null ) {
			firstLettersMap = new HashMap<Character,Character>();
		}
		firstLettersMap.put( firstLetter, rusSymbol );
		lastLetterToSetOfFirstLetterToRusSymbMappings.put( lastLetter, firstLettersMap );
	}	

	static {
		//For more details see:
		//	http://www.asciitable.com/
		//	http://www.translit.ru/
		//	http://en.wikipedia.org/wiki/Cyrillic_characters_in_Unicode
		//	http://en.wikipedia.org/wiki/Latin_characters_in_Unicode
		//	http://www.utf8-chartable.de/unicode-utf8-table.pl?names=-&utf8=oct
		
		//First add single letter mappings
		addSingleLetterMapping('A', '\u0410', true); //A->А
		addSingleLetterMapping('a', '\u0430', false); //a->а
		addSingleLetterMapping('B', '\u0411', true); //B->Б
		addSingleLetterMapping('b', '\u0431', false); //b->б
		addSingleLetterMapping('V', '\u0412', true); //V->В
		addSingleLetterMapping('v', '\u0432', false); //v->в
		addSingleLetterMapping('G', '\u0413', true); //G->Г
		addSingleLetterMapping('g', '\u0433', false); //g->г
		addSingleLetterMapping('D', '\u0414', true); //D->Д
		addSingleLetterMapping('d', '\u0434', false); //d->д
		addSingleLetterMapping('E', '\u0415', true); //E->Е
		addSingleLetterMapping('e', '\u0435', false); //e->е
		addSingleLetterMapping('\u0246', '\u0451', false); //ö->ё
		addSingleLetterMapping('Z', '\u0417', true); //Z->З
		addSingleLetterMapping('z', '\u0437', false); //z->з
		addSingleLetterMapping('I', '\u0418', true); //I->И
		addSingleLetterMapping('i', '\u0438', false); //i->и
		addSingleLetterMapping('J', '\u0419', true); //J->Й
		addSingleLetterMapping('j', '\u0439', false); //j->й
		addSingleLetterMapping('K', '\u041A', true); //K->К
		addSingleLetterMapping('k', '\u043A', false); //k->к
		addSingleLetterMapping('L', '\u041B', true); //L->Л
		addSingleLetterMapping('l', '\u043B', false); //l->л
		addSingleLetterMapping('M', '\u041C', true); //M->М
		addSingleLetterMapping('m', '\u043C', false); //m->м
		addSingleLetterMapping('N', '\u041D', true); //N->Н
		addSingleLetterMapping('n', '\u043D', false); //n->н
		addSingleLetterMapping('O', '\u041E', true); //O->О
		addSingleLetterMapping('o', '\u043E', false); //o->о
		addSingleLetterMapping('P', '\u041F', true); //P->П
		addSingleLetterMapping('p', '\u043F', false); //p->п
		addSingleLetterMapping('R', '\u0420', true); //R->Р
		addSingleLetterMapping('r', '\u0440', false); //r->р
		addSingleLetterMapping('S', '\u0421', true); //S->С
		addSingleLetterMapping('s', '\u0441', false); //s->с
		addSingleLetterMapping('T', '\u0422', true); //T->Т
		addSingleLetterMapping('t', '\u0442', false); //t->т
		addSingleLetterMapping('U', '\u0423', true); //U->У
		addSingleLetterMapping('u', '\u0443', false); //u->у
		addSingleLetterMapping('F', '\u0424', true); //F->Ф
		addSingleLetterMapping('f', '\u0444', false); //f->ф
		addSingleLetterMapping('H', '\u0425', true); //H->Х
		addSingleLetterMapping('h', '\u0445', false); //x->х
		addSingleLetterMapping('X', '\u0425', true); //X->Х
		addSingleLetterMapping('x', '\u0445', false); //x->х
		addSingleLetterMapping('C', '\u0426', true); //C->Ц
		addSingleLetterMapping('c', '\u0446', false); //c->ц
		addSingleLetterMapping('W', '\u0429', true); //W->Щ
		addSingleLetterMapping('w', '\u0449', false); //w->щ
		addSingleLetterMapping('#', '\u044A', false); //#->ъ
		addSingleLetterMapping('Y', '\u042B', true); //Y->Ы
		addSingleLetterMapping('y', '\u044B', false); //y->ы
		addSingleLetterMapping('\'', '\u044C', false); //'->ь
		addSingleLetterMapping('\u00E4', '\u044D', false); //ä->э
		addSingleLetterMapping('\u00FC', '\u044E', false); //ü->ю
		addSingleLetterMapping('q', '\u044F', false); //q->я
		
		//Add mappings for letter combinations
		addLetterCombinationMapping( '\u0419', 'O', '\u0401'); //JO->ЙO -> Ё
		addLetterCombinationMapping( '\u0419', 'o', '\u0401'); //Jo->Йo -> Ё
		addLetterCombinationMapping( '\u0439', 'o', '\u0451'); //jo->йo -> ё
		addLetterCombinationMapping( '\u042B', 'O', '\u0401'); //YO->ЫO -> Ё
		addLetterCombinationMapping( '\u042B', 'o', '\u0401'); //Yo->Ыo -> Ё
		addLetterCombinationMapping( '\u044B', 'o', '\u0451'); //yo->ыo -> ё
		
		addLetterCombinationMapping( '\u0417', 'H', '\u0416'); //ZH->ЗH->Ж
		addLetterCombinationMapping( '\u0417', 'h', '\u0416'); //Zh->Зh->Ж
		addLetterCombinationMapping( '\u0437', 'h', '\u0436'); //zh->зh->ж
		
		addLetterCombinationMapping( '\u0426', 'H', '\u0427'); //CH->ЦH->Ч
		addLetterCombinationMapping( '\u0426', 'h', '\u0427'); //Ch->Цh->Ч
		addLetterCombinationMapping( '\u0446', 'h', '\u0447'); //ch->цh->ч
		
		addLetterCombinationMapping( '\u0421', 'H', '\u0428'); //SH->СH->Ш
		addLetterCombinationMapping( '\u0421', 'h', '\u0428'); //Sh->Сh->Ш
		addLetterCombinationMapping( '\u0441', 'h', '\u0448'); //sh->сh->ш
		
		addLetterCombinationMapping( '\u0428', 'H', '\u0429'); //SHH->ШH->Щ
		addLetterCombinationMapping( '\u0428', 'h', '\u0429'); //SHh->Шh->Щ
		addLetterCombinationMapping( '\u0448', 'h', '\u0449'); //shh->шh->щ
		
		addLetterCombinationMapping( '\u044A', '#', '\u042A'); //##->ъ#->Ъ
		
		addLetterCombinationMapping( '\u044C', '\'', '\u042C'); //''->ь'->Ь
		
		addLetterCombinationMapping( '\u0419', 'E', '\u042D'); //JE->ЙE->Э
		addLetterCombinationMapping( '\u0419', 'e', '\u042D'); //Je->Йe->Э
		addLetterCombinationMapping( '\u0439', 'e', '\u044D'); //je->йe->э
		
		addLetterCombinationMapping( '\u0419', 'U', '\u042E'); //JU->ЙU->Ю
		addLetterCombinationMapping( '\u0419', 'u', '\u042E'); //Ju->Йu->Ю
		addLetterCombinationMapping( '\u0439', 'u', '\u044E'); //ju->йu->ю
		
		addLetterCombinationMapping( '\u042B', 'U', '\u042E'); //YU->ЫU->Ю
		addLetterCombinationMapping( '\u042B', 'u', '\u042E'); //Yu->Ыu->Ю
		addLetterCombinationMapping( '\u044B', 'u', '\u044E'); //yu->ыu->ю
		
		addLetterCombinationMapping( '\u0419', 'A', '\u042F'); //JA->ЙA->Я
		addLetterCombinationMapping( '\u0419', 'a', '\u042F'); //Ja->Йa->Я
		addLetterCombinationMapping( '\u0439', 'a', '\u044F'); //ja->йa->я
		
		addLetterCombinationMapping( '\u042B', 'A', '\u042F'); //YA->ЫA->Я
		addLetterCombinationMapping( '\u042B', 'a', '\u042F'); //Ya->Ыa->Я
		addLetterCombinationMapping( '\u044B', 'a', '\u044F'); //ya->ыa->я
	}

	//The text input element we apply transliteration to
	private static TextBoxBase textObject = null;
	//The registration handler for the key press listener instance of the transliterator
	private static HandlerRegistration keyPressHandler = null;
	//The progress bar related to this text box base element
	private static TextMaximumSizeProgress bindedProgressBar = null;
	
	//This class should not be instantiated, ever
	private Transliterator( ){ }
	
	/**
	 * Allows to bind the transliterator to the TextBoxBase object
	 * @param bind if true then we want to bing the transliterator to the new textObject
	 * @param textObject to bind the transliterator to or null if bind==false
	 * @param progressBar the progress bar binded to this text box base element or null
	 */
	public static void bindTransliterator( final boolean bind, final TextBoxBase textObject,
											final TextMaximumSizeProgress progressBar ) {
		//First un-bind the old object if any
		if( keyPressHandler != null ) {
			keyPressHandler.removeHandler();
			keyPressHandler = null;
		}
		if( Transliterator.textObject != null ) {
			Transliterator.textObject = null;
		}
		bindedProgressBar = null;
		
		//Now if we want to bind a new object then do it
		if( bind ) {
			bindedProgressBar = progressBar;
			Transliterator.textObject = textObject;
			keyPressHandler = Transliterator.textObject.addKeyPressHandler( new Transliterator() );
		}
	}
	
	/**
	 * Allows to get the Russian symbol for the letter combination.
	 * This method reads text and uses cursor position of the textObject
	 * @param lastLetter the code of the last input letter
	 * @return the Russian symbol corresponding to the letter combination or null if no mapping is found
	 */
	private static Character getCharLetterCombination( final Character lastLetter ) {
		Character resultSymb = null;
		//The first letter position is the cursor's position - 1
		final int firstLetterPos = textObject.getCursorPos() - 1;
		//If there was a previous letter then look for the mapping
		if( firstLetterPos >= 0 ) {
			Map<Character,Character> firstLettersMap = lastLetterToSetOfFirstLetterToRusSymbMappings.get( lastLetter );
			if( firstLettersMap != null ) {
				//Get the first letter, i.e. the letter preceding the input one
				final Character firstLetter = textObject.getText().charAt( firstLetterPos );
				//Try to get the Russian symbol from the mapping
				resultSymb = firstLettersMap.get( firstLetter );
			}
		}
		return resultSymb;
	}
	
	/**
	 * Checks if the new character has to be transliterated or it completes
	 * a sequence of characters that has to be transliterated. If yes the it
	 * replaces them with a proper russian character.
	 * @param charCode the newly input characted
	 * @return true if something was transliterated, otherwise false
	 */
	private static boolean processNewCharacter(final Character charCode) {
		boolean isCombination = false;
		//First try to see if this is some sort of letter combination
		Character rusChar = getCharLetterCombination( charCode );
		if( rusChar == null ) {
			//If not then substitute with a single letter, if there is mapping
			rusChar = latinToCyrilicSingleLetterUpper.get( charCode );
			if( rusChar == null ) {
				rusChar = latinToCyrilicSingleLetterLower.get( charCode );
				if( rusChar == null ) {
					//There is no mapping for this symbol, thus we simply return
					return false;
				}
			}
		} else {
			//We found a letter combination here
			isCombination = true;
		}
		//Substitute the string and cancel the event
		final String text = textObject.getText();
		final int curPos = textObject.getCursorPos();
		
		//WARNING: Firefox has a bug that when substituting text we get scrolling
		//to the top of the text box. But this is a known problem with GWT and Firefox.
		
		//In the following we also remove the selected text, this is for 
		//the case when a user selected some text and then types a symbol,
		//then the selected text should be overwritten by the input symbol
		textObject.setText( text.substring( 0, curPos - (isCombination ? 1 : 0) ) + rusChar +
							text.substring( curPos+textObject.getSelectionLength(), text.length() ) );
		textObject.setCursorPos( curPos + (isCombination ? 0 : 1) );
		if( bindedProgressBar != null ) {
			bindedProgressBar.forceProgressUpdate();
		}
		return true;
	}
	
	/**
	 * Returns the char code of the native event. Works in Opera, Firefox,
	 * Safari and  Google Chrome.
	 * NOTE: The original method in the Native event does not work right
	 * it returns e.charCode || e.keyCode which is inappropriate because
	 * then we get a char value for a non-printable key! This way we see
	 * some of the keyboard functional keys resulting in printable character
	 * inputs. This implementation here does not account for the e.keyCode
	 * thus making the method return the char only if the printable symbol was input.
	 * For checking the actual char and key codes in browsers on can use
	 * KeyCodes.html located in the same directory with this source file.
	 * @param e the native event
	 * @return the corresponding char code
	 */
	private static native char getCharCodeOthers(NativeEvent e)/*-{
		var code = e.keyCode ? e.keyCode : e.charCode ? e.charCode : e.which ? e.which : void 0;
		if( e.which ) {
			if( code && ( code > 33 ) && ( ! ( e.ctrlKey || e.altKey ) ) ){
			  	return code;
			}
		}
		return void 0;
	}-*/;
	
	/**
	 * Returns the char code for the natice event, this on works in 
	 * Internet Explorer or at least IE 8.0
	 * @param e the native event
	 * @return the corresponding char code
	 */
	private static native char getCharCodeIE(NativeEvent e)/*-{
		if( e.keyCode && ( e.keyCode > 33 ) ){
			return e.keyCode;
		}
		return void 0;
	}-*/;
	
	/**
	 * Returns the char code for the natice event.
	 * Allows to get the event's character in a proper
	 * way, based whether it is IE or another browser
	 * @param e the native event
	 * @return the corresponding char code
	 */
	private static char getCharCode(NativeEvent e) {
		if( isInternetExplorer ) {
			return getCharCodeIE( e );
		} else {
			return  getCharCodeOthers( e );
		}
	}
	
	@Override
	public void onKeyPress(KeyPressEvent event) {
		//Window.alert("Char code: '" + (int) getCharCode( event.getNativeEvent() ) + "', key code: " + event.getNativeEvent().getKeyCode());
		//Substitute characters if the transliteration is on and the text input is enabled
		if( textObject.isEnabled() ) {
			final Character charCode = getCharCode( event.getNativeEvent() );
			if( processNewCharacter( charCode ) ) {
				event.stopPropagation();
				event.preventDefault();
			}
		}
	}
}
