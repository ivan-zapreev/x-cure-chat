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
<%@page language="java" import="com.xcurechat.server.ForumManagerImpl" %>
<%@page language="java" import="com.xcurechat.client.data.search.OnePageViewData" %>
<%@page language="java" import="com.xcurechat.client.data.search.ForumSearchData" %>
<%@page language="java" import="com.xcurechat.client.data.ShortUserData" %>
<%@page language="java" import="com.xcurechat.client.data.ForumMessageData" %>
<%@page language="java" import="com.xcurechat.server.utils.WebUtilities" %>
<%@page language="java" import="com.xcurechat.server.jsp.ForumJSPHelper" %>
<%@page language="java" import="com.xcurechat.server.redirect.SectionRedirectHelperInt" %>
<%@page language="java" import="com.xcurechat.server.redirect.ForumRedirectHelper" %>
<%@page language="java" import="com.xcurechat.server.redirect.MainRedirectHelper" %>
<%@page language="java" import="com.xcurechat.server.utils.ServerEncoder" %>
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
				pageTitle = "Тайничок - Новое";
				siteDescription = "Добро пожаловать на Тайничок, абсолютно бесплатный сайт " +
								  "знакомств, с полным отсутствием модераторов и рекламы! " +
								  "У нас вы сможете повстречать новых друзей! К вашим услугам " +
								  "чат и форум куда вы можете загружать весёлые картинки, видео " +
								  "приколы и просто музыку! У нас вы не только заведёте новые " +
								  "знакомства но и получите массу удовольствия на нашем форуме! " +
								  "Кстати сказать ... вы можете посылать фото, видео и музыку прямо в чат!";
				keywords		= "новое, завести, друзей, знакомства, музыка, весёлые, картинки, новое, фото, видео, приколы, новости";
			} else {
				pageTitle = "Tainichok - News";
				siteDescription = "Welcome to Tainichok, a completely free website with no " +
								  "moderators and advertisements! Here you can meet new people, " +
								  "chat with them and post funny photos, videos and music to the " +
								  "forum. Thus Tainichok is not only for meeting new people but " +
								  "also for entertainment! By the way, guess what ... you can also " +
								  "post music, photos, and videos directly into the chat!";
				keywords		= "new, make, friends, meet, people, music, funny, new, pictures, funny, videos, news";
			}
			final MainRedirectHelper mainHelper = new MainRedirectHelper();
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
			window.location="<%=mainHelper.getSiteGWTURL(request)%>"
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
		<!--#include virtual="./navigator.jsp?locale=<%=locale%>&<%=SectionRedirectHelperInt.SITE_SECTION_SERVLET_PARAM%>=<%=mainHelper.getSectionName()%>"  -->
		<!--#include virtual="./static/warn_no_script_<%=locale%>.html"  -->

		<!---------------------------------------------------------------------------------------------------------->
		<!-- WARNING: Including the static description is disabled, the data is now placed in the page description-->
		<!---------------------------------------------------------------------------------------------------------->
		<!--(#)include virtual="./static/dec_pan_cent_start.html"  -->
		<!--(#)include virtual="./static/main_<%=locale%>.html"-->
		<!--(#)include virtual="./static/dec_pan_cent_end.html"  -->
		<!---------------------------------------------------------------------------------------------------------->
		<%
			final ServerEncoder encoder = new ServerEncoder();
			final ForumSearchData searchData = new ForumSearchData();
			searchData.setParametersFromMap( encoder, request.getParameterMap() );
			//Create a new clean object and fill it out with the needed data
			//This is done for the sake of avoiding other possible arguments
			final ForumSearchData searchObject = new ForumSearchData();
			searchData.baseMessageID = ForumSearchData.UNKNOWN_BASE_MESSAGE_ID_VAL;
			searchObject.pageIndex = searchData.pageIndex;
			searchObject.isApproved = true;
			
			try{
				OnePageViewData<ForumMessageData> messages = ForumManagerImpl.searchMessagesStatic( request.getSession(), request, ShortUserData.UNKNOWN_UID, "", searchObject);
		%>
		<!--#include virtual="./static/dec_pan_cent_start.html"  -->
				<%=ForumJSPHelper.getNewsSearchPageIndexesHTML( request, locale, messages.total_size )%>
		<!--#include virtual="./static/dec_pan_cent_end.html"  -->
		<!--#include virtual="./static/dec_pan_cent_start.html"  -->
		<%
				for( ForumMessageData message : messages.entries ) {
		%>
					<%=ForumJSPHelper.getForumMessageHTML( request, locale, message, false )%>
		<%
				}
				
			} catch( Exception e ) {
		%>
			<b>ERROR while retrieving the new site uploads, error msg: <%=e.getMessage()%><b>
		<%
			}
		%>
		<!--#include virtual="./static/dec_pan_cent_end.html"  -->
	</body>
</html>
