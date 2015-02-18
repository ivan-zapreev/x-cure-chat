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

import java.util.Locale;
import java.util.HashMap;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import com.octo.captcha.service.image.ImageCaptchaService;
import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.image.DefaultManageableImageCaptchaService;
import com.octo.captcha.service.captchastore.FastHashMapCaptchaStore;

import com.xcurechat.client.utils.SupportedFileMimeTypes;

/**
 * @author zapreevis
 * This singleton is supposed to manage access to the unigue
 *  instance of the ImageCaptchaService class object.  
 */
public class CaptchaServiceManager {
	
	//Get the Log4j logger object
	private static Logger logger = Logger.getLogger( CaptchaServiceManager.class );
	
	public static final SupportedFileMimeTypes CAPTCHA_IMAGE_MIME_FORMAT = SupportedFileMimeTypes.JPEG_IMAGE_MIME;
	
	//This Mash Map contains the captchaId to captchaImage mapping.
	//Since the Map is not synchronized, we need to add synchronization
	//sections when we work with it.
	private static HashMap<String, byte[]> theCaptchaProblemsMap = new HashMap<String, byte[]>();

	//An instance of the captcha problem generator/validator
	//Just in case, I believe we should synchronize it's access
	private static ImageCaptchaService imageCaptchaService;
	
	static{
		imageCaptchaService = new DefaultManageableImageCaptchaService( new FastHashMapCaptchaStore(), new XCureImageCaptchaEngine(), 180, 100000, 75000);
	}
	
	/**
	 * This method is used to create an instance of a JCaptcha problem
	 * @param sessionId the id of the Http session or another UID that
	 * is only known to the server and the client and is the same during
	 * generation of the CAPTCHA problem and its answer validation.
	 * Note that, these is done via different servlets   
	 * @param theClientLocale the client's application locale
	 * @return the byte array with the jpeg encoded image representing the captcha problem
	 */
	public static byte[] getCaptchaProblem( final String sessionId, final Locale theClientLocale ){
		logger.debug( "Retrieving the CAPTCHA problem." );
		//The resulting jpeg image
		byte[] result = null;
		//This will store the initially generated image, before it is encoded into a jpeg
		BufferedImage challenge = null;
		//The output stream to render the captcha image as jpeg into
		ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
		
		try {
			//Get the session id that will identify the generated captcha.
			//call the ImageCaptchaService getChallenge method
			synchronized ( imageCaptchaService ) {
				challenge = imageCaptchaService.getImageChallengeForID( sessionId, theClientLocale );
			}
			//Write into output stream with predefined encoding
			ImageIO.write( challenge, CAPTCHA_IMAGE_MIME_FORMAT.getMainMTSuffix(), jpegOutputStream );
			
			//Get the byte array of the problem's image
			result = jpegOutputStream.toByteArray();
			
			//Close the stream;
			jpegOutputStream.close();
		} catch ( Exception e) {
			logger.error( "An exception while retrieving a CAPTCHA problem",  e );
		}
		return result;
	}
	
	/**
	 * This method is used to create an instance of a JCaptcha problem.
	 * The problem is stored in the internal hash map and can be retrieved
	 * by valling removeCaptchaProblem( String ) method
	 * @param sessionId the id of the Http session or another UID that
	 * is only known to the server and the client and is the same during
	 * generation of the CAPTCHA problem and its answer validation.
	 * Note that, these is done via different servlets   
	 * @param theClientLocale the client's application locale
	 */
	public static void generateCaptchaProblem( final String sessionId, final Locale theClientLocale ){
		logger.debug( "Generating new CAPTCHA problem." );
		//Obtain the captcha problem
		byte[] result = getCaptchaProblem( sessionId, theClientLocale );
		//If the problem was successfully generated, then we store it in the hash map
		if( result != null ) {
			//Store the new Captcha problem in the theCaptchaProblemsMap HashMap and return its key
			storeCaptchaProblem( sessionId, result );
		}
	}
	
	/**
	 * Adds the new problem to the list of generated CAPTCHA problems and
	 * stores it under the session ID.
	 * @param sessionId the id of the Http session or another UID that
	 * is only known to the server and the client and is the same during
	 * generation of the CAPTCHA problem and its answer validation.
	 * Note that, these is done via different servlets
	 * @param captchaChallengeAsJpeg the image representing CAPTCHA problem   
	 */
	private static void storeCaptchaProblem( final String sessionId, byte[] captchaChallengeAsJpeg ) {
		logger.debug( "Storing CAPTCHA problem in a hash." );
		synchronized( theCaptchaProblemsMap ){
				//The old image is discarded, this allows to prevent overload by
				//Images generated within one session
				theCaptchaProblemsMap.put( sessionId, captchaChallengeAsJpeg );
		}
	}
	
	/**
	 * Validates the the answer against the CAPTCHA problem.
	 * @param sessionId the id of the Http session or another UID that
	 * is only known to the server and the client and is the same during
	 * generation of the CAPTCHA problem and its answer validation.
	 * Note that, these is done via different servlets
	 * @param response the answer to the CAPTCHA problem
	 * @return true if the answer is correct, otherwise false
	 */
	public static boolean validateCaptchaProblem( final String sessionId, final String response ){
		boolean isResponseCorrect = false;
		try {
			synchronized( imageCaptchaService ){
				isResponseCorrect = imageCaptchaService.validateResponseForID( sessionId , response );
			}
		} catch (CaptchaServiceException e) {
			logger.error( "An exception while validating CAPTCHA problem", e );
		}
		
		if( isResponseCorrect ) {
			logger.debug( "The CAPTCHA test was passed." );
		} else {
			logger.warn( "The CAPTCHA test was failed." );
		}

		return isResponseCorrect;
	}

	/**
	 * Delete the instance of the CAPTCHA problem from the internal HashMap.
	 * @param sessionId the id of the Http session or another UID that
	 * is only known to the server and the client and is the same during
	 * generation of the CAPTCHA problem and its answer validation.
	 * Note that, these is done via different servlets   
	 * @return the data representing the CAPTHCA problem, associated with captchaId 
	 */
	public static byte[] removeCaptchaProblem( final String sessionId ){
		logger.debug( "Removing CAPTCHA problem from the hash." );
		synchronized( theCaptchaProblemsMap ){
			return (byte[]) theCaptchaProblemsMap.remove( sessionId );
		}
	}
}