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
 * The server-side RPC package.
 * (C) Ivan S. Zapreev, 2008
 */
package com.xcurechat.server.security.captcha;

import com.octo.captcha.image.gimpy.GimpyFactory;

import com.octo.captcha.engine.image.ListImageCaptchaEngine;

import java.awt.Font;

import com.octo.captcha.component.image.backgroundgenerator.BackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.FunkyBackgroundGenerator;
import com.octo.captcha.component.image.fontgenerator.FontGenerator;
import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator;
import com.octo.captcha.component.image.textpaster.TextPaster;
import com.octo.captcha.component.image.textpaster.RandomTextPaster;
import com.octo.captcha.component.image.wordtoimage.WordToImage;
import com.octo.captcha.component.image.wordtoimage.ComposedWordToImage;
import com.octo.captcha.component.image.color.RandomRangeColorGenerator; 
import com.octo.captcha.component.word.wordgenerator.WordGenerator;
import com.octo.captcha.component.word.wordgenerator.RandomWordGenerator;

/**
 * @author zapreevis
 * Taken from some Forum, here I try to make a new Image Engine, in order to desired image sizes and fonts
 */
public class XCureImageCaptchaEngine extends ListImageCaptchaEngine {
	
	//Available letters for word generation
	//private static final String AVAILABLE_SYMBOLS = "aAbBCdDeEfFgGhHiIjJKLmMnNOpPqQeRStTuUVWXyYZ123456789#@%&!"; 
	//The simplified version of CAPTCHA symbols for easing the users' life but not too much
	private static final String AVAILABLE_SYMBOLS = "0123456789#@%&!$+"; 
	
	protected void buildInitialFactories() {
		WordGenerator wgen = new RandomWordGenerator( AVAILABLE_SYMBOLS );
		RandomRangeColorGenerator cgen = new RandomRangeColorGenerator(
												new int[] {0, 100},
												new int[] {0, 100},
												new int[] {0, 100});
		TextPaster textPaster = new RandomTextPaster( new Integer(5), new Integer(7), cgen, new Boolean(true) );
		
		BackgroundGenerator backgroundGenerator = new FunkyBackgroundGenerator(new Integer(150), new Integer(40));
		
		Font[] fontsList = new Font[] {
			new Font("Arial", 0, 10),
			new Font("Tahoma", 0, 10),
			new Font("Verdana", 0, 10),
		};
		
		FontGenerator fontGenerator = new RandomFontGenerator(new Integer(20), new Integer(25), fontsList);
		
		WordToImage wordToImage = new ComposedWordToImage(fontGenerator, backgroundGenerator, textPaster);
		this.addFactory(new GimpyFactory(wgen, wordToImage));
	}
}
