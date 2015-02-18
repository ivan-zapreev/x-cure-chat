<!--
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
-->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page language="java" import="com.xcurechat.server.redirect.SectionRedirectHelperInt" %>
<%@page language="java" import="com.xcurechat.server.redirect.MainRedirectHelper" %>
<%@page language="java" import="com.xcurechat.server.utils.WebUtilities" %>
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<!--                                            -->
		<!-- Enable the SSI page processing by this     -->
		<!--                                            -->
		<%@ page contentType="text/html;charset=UTF-8" %>
		
		<!--                                            -->
		<!--Include the standard part of the header(SSI)-->
		<!--                                            -->
		<!--#include virtual="./static/header.html"     -->

	    <%
			final String locale = WebUtilities.getLocaleValue(request);
			String pageTitle, siteDescription, keywords;
			if(locale.equals( WebUtilities.LOCALE_PARAMETER_VALUE_RUSSIAN )) {
				pageTitle = "Тайничок - Куча веселья";
				siteDescription = "Развлекательный сайт знакомств Тайничок: чат, форум, фото и видео приколы, серьёзные темы, общение и знакомства";
				keywords		= "чат, форум, фото, видео, приколы, развлечения, общение, знакомства";
			} else {
				pageTitle = "Tainichok - Plenty of fun";
				siteDescription = "Tainichok is an entertaining website: chat, forum, funny pictures and videos, serious topics, and making new fiends";
				keywords		= "chat, forum, funny, photos, videos, fun, communication, fiends";
			}
	    %>
	    
		<!--                                           -->
		<!-- Site's title                              -->
		<!--                                           -->
		<title><%=pageTitle%></title>
		
		<!--                                                 -->
		<!-- Description and keywords for the search engines -->
		<!--                                                 -->
		<meta name="description" content="<%=siteDescription%>"/>
		<meta name="keywords" content="<%=keywords%>"/>
		
		<!--                                            -->
		<!-- Import the site redirection script         -->
		<!--                                            -->
		<script type="text/javascript" src="./javascript/url_redirect_and_loading_check.js"></script>

		<!--                                            -->
		<!-- Import the browser and OS detection script -->
		<!--                                            -->
		<script type="text/javascript" src="./javascript/browser_detection.js"></script>

		<!--                                            -->
		<!-- Import the flash sound player script       -->
		<!--                                            -->
		<script type="text/javascript" src="./media/soundmanager.js"> </script>
		
		<%
			//Compute the redirect page in case of no Java Script
			//NOTE: The part of the URL that is after HASH does not
			//get to the server side, this is why we can not rewrite
			//the url properly to redirect the user to the required jsp
			String staticRedirectURL = SectionRedirectHelperInt.getSiteSectionServletURL( request, MainRedirectHelper.class, null );
		%>
		
		<!--                                            -->
		<!-- If the JavaScript is not enabled: redirect -->
		<!--                                            -->
		<noscript>
			<meta http-equiv="refresh" content="0;<%=staticRedirectURL%>" />
		</noscript>
	</head>

	<!--                                           -->
	<!-- The body can have arbitrary html, or      -->
	<!-- you can leave the body empty if you want  -->
	<!-- to create a completely dynamic ui         -->
	<!--                                           -->
	<body marginwidth="0" marginheight="0" leftmargin="0" topmargin="0">
		<!--                                                                   -->
		<!-- Initialize the sound playing script for the embedded sound player -->
		<!-- MUST BE THE FIRST ENTRY INSIDE THE BODY, THIS DIV MUST BE VISIBLE -->
		<!--                                                                   -->
		<div> </div>
		<script>
			soundManagerInit();
		</script>
		
		<!--                                            -->
		<!-- Import the on-load progress note	          -->
		<!--                                            -->
		<!--#include virtual="./static/loading_<%=locale%>.html"     -->
		
		<!--                                           -->
		<!-- This script loads your compiled module.   -->
		<!-- If you add any GWT meta tags, they must   -->
		<!-- be added before this line.                -->
		<!--                                           -->
		<script type="text/javascript" language='javascript' src='XCureChatMain.nocache.js'></script>
		
		<!-- In order for the browser auto-complete feature to work, -->
		<!-- we must define the login form in the original HTML markup -->
		<div id="loginDiv" style="display:none">
		<form action="javascript:__gwt_login()" id="loginForm" width="100%">
			<table id="loginTable" valign="center" width="100%">
				<tr>
					<td align="left" id="loginLabel">Username:</td>
					<td align="right"><input class="gwt-TextBox" id="loginUsername" name="u" style="margin-left:0px"></td>
				</tr>
				<tr>
					<td align="left" id="passwordLabel">Password:</td>
					<td align="right"><input class="gwt-PasswordTextBox" id="loginPassword" name="pw" type="password" style="margin-left:0px"></td>
				</tr>
			</table>
			<table valign="center" width="100%">
				<tr>
				<td align="center"><button class="xcure-UserDialog-ActionButton" id="loginCancel" type="button" onClick="javascript:__gwt_login_cancel()">Cancel</button></td>
				<td id="progressBar"></td>
				<td align="center"><button class="xcure-UserDialog-ActionButton" id="loginSubmit" type="submit">Login</button></td>
				</tr>
			</table>
		</form>
		</div>
		
		<!-- OPTIONAL: include this if you want history support -->
		<iframe src="javascript:''" id="__gwt_historyFrame" style="margin:0px; padding:0px; width:0px; height:0px; border:0px; display:none; visibility:hidden;"></iframe>
		
		<!-- Yandex.Metrika -->
		<script src="//mc.yandex.ru/resource/watch.js" type="text/javascript"></script>
		<script type="text/javascript">
		try { var yaCounter187515 = new Ya.Metrika(187515); } catch(e){}
		</script>
		<noscript><div style="position: absolute; display:none; visibility:hidden;"><img src="//mc.yandex.ru/watch/187515" alt="" /></div></noscript>
		<!-- Yandex.Metrika -->
		
		<!-- Scripting for the Google Analytics tools -->
		<script type="text/javascript">
			var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
			document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
		</script>
		<script type="text/javascript">
			try {
				var pageTracker = _gat._getTracker("UA-12232058-1");
				pageTracker._setDomainName("none");
				pageTracker._setAllowLinker(true);
				pageTracker._trackPageview();
			} catch(err) {}
		</script>
	</body>
</html>
