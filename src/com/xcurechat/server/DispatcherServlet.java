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
 * (C) Ivan S. Zapreev, 2010
 */
package com.xcurechat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.xcurechat.client.rpc.ServerSideAccessManager;
import com.xcurechat.server.redirect.ForumRedirectHelper;
import com.xcurechat.server.redirect.InfoRedirectHelper;
import com.xcurechat.server.redirect.MainRedirectHelper;
import com.xcurechat.server.redirect.SectionRedirectHelperInt;
import com.xcurechat.server.utils.WebUtilities;

/**
 * @author zapreevis
 * @obsolete
 * This class is responsible for providing a simple redirect page
 * that allows to distinguish between the JavaScript enabled and
 * disabled clients. Currently this servlet is obsolete and is
 * only used for backwards compatibility. The new site pages do
 * not reference this servlet.
 */
public class DispatcherServlet extends HttpServlet {
	
	//The UID of the service, is needed for serialization
	private static final long serialVersionUID = 2L;
	
	//Get the Log4j logger object
	public static final Logger logger = Logger.getLogger( DispatcherServlet.class );
	
	//This map stores a mapping between the site section names and 
    //the helper objects for creating the proper redirect URLs
	public static Map<String, SectionRedirectHelperInt> helpersMap = new HashMap<String, SectionRedirectHelperInt>();
	
	/**
	 * Allows to register the site section redirect helper
	 * @param helper the helper object
	 */
	private static void registerSiteSectionHelper( SectionRedirectHelperInt helper ) {
		helpersMap.put( helper.getSectionName(),  helper);
	}
	
	static {
		//Here we should register all of the site redirect helper objects
		registerSiteSectionHelper( new ForumRedirectHelper() );
		registerSiteSectionHelper( new InfoRedirectHelper() );
		registerSiteSectionHelper( new MainRedirectHelper() );
	}
	
	/**
	 * Allows to get the site section helper for the targeted site section
	 * @param siteSectionName the site section name
	 * @return the section helper object or null if the helper is not found
	 */
	private static SectionRedirectHelperInt getSiteSectionHelper( final String siteSectionName ) {
		if( siteSectionName != null ) {
			//Get the helper object
			return helpersMap.get( siteSectionName );
		} else {
			return null;
		}
	}
	
	/**
	 * Allows to get the site section name for the targeted site section
	 * @param request the http request we came to the site with
	 * @return the section name
	 */
	private static String getSiteSectionName( final HttpServletRequest request ) {
		//Get the site section parameter value
		String siteSection = request.getParameter( SectionRedirectHelperInt.SITE_SECTION_SERVLET_PARAM );
		
		//Avoid having a null site section name
		if( siteSection == null || siteSection.trim().isEmpty() ) {
			siteSection = (new MainRedirectHelper()).getSectionName();
			logger.info("Got a request with an undefined site section, setting it to: " + siteSection );
		} else {
			logger.info("Got a request for the site section: " + siteSection );
		}
		
		return siteSection;
	}

	/**
	 * Takes the input parameters, checks the site section and creates a web-page with
	 * two redirects, one for the case of a no JavaScript and another for the case of
     * JavaScript. The redirects are pointing, correspondingly to the JSP and GWT pages. 
	 */
	@Override
	public void doGet( HttpServletRequest request, HttpServletResponse response )
						throws ServletException, IOException {
		//Get the site section name
		final String siteSection = getSiteSectionName( request );
		
		//Get the appropriate site section helper
		SectionRedirectHelperInt helper = getSiteSectionHelper( siteSection );
		
		//If the helper is not null
		if( helper != null ) {
			final String urlGWT = helper.getSiteGWTURL( request );
			final String urlJSP = helper.getSiteServletURL( request );
			logger.debug( "Computed urlGWT = '" + urlGWT + "' and urlJSP = '" + urlJSP + "'" );
				
			//Use the helper to get proper redirect URLs and generate the webpage for the redirect
			response.setContentType("text/html;charset=UTF-8");
			//response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
			out.println("<HTML xmlns=\"http://www.w3.org/1999/xhtml\">");
			out.println("\t<HEAD>");
			out.println("\t\t<META http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>");
			out.println("\t\t<LINK rel=\"shortcut icon\" type=\"image/ico\" href=\"images/favicon.ico\"/>");
			out.println("\t\t<TITLE>\u0422\u0430\u0439\u043D\u0438\u0447\u043E\u043A</TITLE>");
			out.println("\t\t<META http-equiv=\"pragma\" content=\"no-cache\" />");
			out.println("\t\t<LINK rel=\"stylesheet\" type=\"text/css\" href=\"./css/common.css\" />");
			out.println("\t\t<meta name=\"description\" content=\"Добро пожаловать на Тайничок, абсолютно бесплатный" +
															  " развлекательный сайт знакомств, с полным" +
															  " отсутствием модераторов и рекламы!" +
															  "(Welcome to Tainichok, a completely free entertaining" +
															  " website with absolutely no moderators" +
															  " and advertisements!)\"/>");
			out.println("\t\t<meta name=\"keywords\" content=\"чат, форум, видео приколы, фото приколы," +
					                                              " развлечения, общение, chat, forum, funny" +
					                                              " photos, funny videos, fun, communication\" locale=ru\"/>");
			
			//Add the redirect for the case of JavaScript
			out.println("\t\t<SCRIPT language=\"JavaScript\">");
			out.println("\t\t<!--");
			out.println("\t\t\twindow.location=\"" + urlGWT + "\"");
			out.println("\t\t//-->");
			out.println("\t\t</SCRIPT>");
			
			//Add the redirect for the case of no JavaScript
			out.println("\t\t<NOSCRIPT>");
			out.println("\t\t\t<meta http-equiv=\"refresh\" content=\"0;"+ urlJSP +"\" />");
			out.println("\t\t</NOSCRIPT>");

			out.println("\t</HEAD>");
			out.println("\t<BODY>");
			//Add a progress bar and two site version links, just in case
			final String locale = WebUtilities.getLocaleValue( request ), scriptOn, scriptOff, waitALittle;
			if( locale.equals( WebUtilities.LOCALE_PARAMETER_VALUE_RUSSIAN ) ) {
				waitALittle = "Вас перенаправляют на нужную страницу сайта. Если в " +
							  "течении 2-х минут вы не будете перенаправлены, то:";
				scriptOn = "Нажмите если Ява Скрипт ВКЛЮЧЕН";
				scriptOff = "Нажмите если Ява Скрипт ВЫКЛЮЧЕН";
			} else {
				waitALittle = "You are being redirected to the requeted site page. " +
							  "If you are not redirected within 2 minutes, then:";
				scriptOn = "Click here if JavaScript is ON";
				scriptOff = "Click here if JavaScript is OFF";
			}
			out.println("\t\t<div style=\"text-align: center; color:white; font-size:130%; font-weight:bold;\">");
			out.println("\t\t\t" + waitALittle );
			out.println("\t\t</div>");
			
			//Do the include of the html via the request dispatcher
			ServletContext sc = this.getServletContext();
			RequestDispatcher r = sc.getRequestDispatcher("/loading.jsp"+ ServerSideAccessManager.URL_QUERY_DELIMITER + WebUtilities.getLocaleParamString(request) );
			r.include(request,response);
			
			out.print("\t\t<div style=\"position: absolute; width:100%; text-align: center; top:120px;\"><b>");
			out.print("<a style=\"color:lightgreen;font-size:20px;\" href=\"" + urlGWT + "\">"+scriptOn+"</a>");
			out.print("&nbsp;&nbsp; OR &nbsp;&nbsp;");
			out.print("<a style=\"color:lightgray;font-size:20px;\" href=\"" + urlJSP + "\">"+scriptOff+"</a>");
			out.println("</b></div>");
			out.println("\t</BODY>");
			out.println("</HTML>");
		} else {
			logger.error( "Could not find the site section redirect helper object for the section: " + siteSection + ", redirecting to " + WebUtilities.getSiteURL( request ) );
			response.setContentType("text/plain");
			response.sendRedirect( WebUtilities.getSiteURL( request ) );
		}
	}
}
