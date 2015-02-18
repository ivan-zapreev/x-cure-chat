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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml-transitional.dtd">
<%@page language="java" import="com.xcurechat.server.utils.WebUtilities" %>
<%@page language="java" import="com.xcurechat.server.redirect.SectionRedirectHelperInt" %>
<%@page language="java" import="com.xcurechat.server.redirect.InfoRedirectHelper" %>
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
				pageTitle = "Тайничок - Описание";
				siteDescription = "Развлекательный сайт знакомств Тайничок: чат, форум, фото и видео приколы, серьёзные темы, общение и знакомства";
				keywords		= "форум, чат, Тайничок, общение, новые знакомства, фото, видеа, музыка, слушать, радио, онлайн";
			} else {
				pageTitle = "Tainichok - Description";
				siteDescription = "Tainichok is an entertaining website: chat, forum, funny pictures and videos, serious topics, and making new fiends";
				keywords		= "forum, chat, Tainichok, talk to people, make, new, friends, share, pictures, music, videos, listen, radio, online";
			}
			final InfoRedirectHelper infoHelper = new InfoRedirectHelper();
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
		
		<SCRIPT language="JavaScript">
		<!--
			window.location="<%=infoHelper.getSiteGWTURL(request)%>"
		-->
		</SCRIPT>
	</head>

	<!--                                           -->
	<!-- The body can have arbitrary html, or      -->
	<!-- you can leave the body empty if you want  -->
	<!-- to create a completely dynamic ui         -->
	<!--                                           -->
	<body marginwidth="0" marginheight="0" leftmargin="0" topmargin="0">
		<!--                                                 -->
		<!-- Here we use the SSI include                     -->
		<!--                                                 -->
		<!--#include virtual="./navigator.jsp?locale=<%=locale%>&<%=SectionRedirectHelperInt.SITE_SECTION_SERVLET_PARAM%>=<%=infoHelper.getSectionName()%>"  -->
		<!--#include virtual="./static/warn_no_script_<%=locale%>.html"  -->

		<!--#include virtual="./static/dec_pan_cent_start.html"  -->
		<!--#include virtual="./static/info_<%=locale%>.html"-->
		<!--#include virtual="./static/dec_pan_cent_end.html"  -->
			
	</body>
</html>
