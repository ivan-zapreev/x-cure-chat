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
<%@page language="java" import="com.xcurechat.server.utils.ServerEncoder" %>
<%@page language="java" import="com.xcurechat.server.cache.ForumQueriesCache" %>
<%@page language="java" import="com.xcurechat.server.utils.EscapeHTMLHelper" %>
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
			//Set the initial values for the title, description and keywords
			String pageTitle, siteDescription, keywords;
			if(locale.equals( WebUtilities.LOCALE_PARAMETER_VALUE_RUSSIAN )) {
				pageTitle = "Тайничок - Форум";
				siteDescription = "Форум на Тайничке, здесь вы найдёте множество фото и видео приколов, " +
								  "а так же музыки и песен! У нас есть темы с демотиваторами, новостями " +
								  "и многое другое! Вы можете загружать и скачивать видео, фото и музыку, " +
								  "а так же добавлять в посты видео с различных сайтов таких как Youtube! " +
								  "Плюс ко всему, вы можете посылать линки на лучшие сообщения с форума в " +
								  "ваш профиль на Twitter, Mail.ru, или Facebook. И всё это лишь одним нажатием кнопки!";
				keywords		= "форум, фото, видео, приколы, музыка, песени, демотиваторы, новости";
			} else {
				pageTitle = "Tainichok - Forum";
				siteDescription = "Forum at Tainichok, here you can find plenty of funny pictures, videos, " +
								  "demotivators, serious news and also discuss any topics your like. You " +
								  "can upload and download pictures, videos and music. You can also embed " +
								  "videos from other web sites, such as Youtube! In addition you can add links " +
								  "to your favorite forum posts to your Twitter, Mail.ru, or Facebook account " +
								  "by just one click of a mouse!";
				keywords		= "forum, photo, video, fun, music, songs, demotivators, news";
			}
			
			//Update the forum title and the forum description with the topic or section title and description
			final ServerEncoder encoder = new ServerEncoder();
			final ForumSearchData searchData = new ForumSearchData();
			searchData.setParametersFromMap( encoder, request.getParameterMap() );
			if( searchData.isForumNavigation() ) {
				final ForumQueriesCache forumCache = ForumQueriesCache.getInstane();
				ForumMessageData msgData = forumCache.getForumMessage( searchData.baseMessageID, ShortUserData.UNKNOWN_UID );
				if( ( msgData != null ) &&
					( ( msgData.isForumSectionMessage() ) || ( msgData.isForumTopicMessage() ) )  ) {
					if( ( msgData.messageTitle != null ) && ! msgData.messageTitle.trim().isEmpty() ) {
						pageTitle += ": " + EscapeHTMLHelper.escapeHTML( msgData.messageTitle );
					}
					if( ( msgData.messageBody != null ) && ! msgData.messageBody.trim().isEmpty() ) {
						siteDescription = EscapeHTMLHelper.escapeHTML( msgData.messageBody );
					}
				}
			}
			final ForumRedirectHelper forumHelper = new ForumRedirectHelper();
	    %>
	    
		<!--                                           -->
		<!-- Forum pages title                         -->
		<!--                                           -->
		<title><%=pageTitle%></title>
		
		<!--                                                 -->
		<!-- Description and keywords for the search engines -->
		<!--                                                 -->
		<meta name="description" content="<%=siteDescription%>"/>
		<meta name="keywords" content="<%=keywords%>"/>
		
		<SCRIPT language="JavaScript">
		<!--
			window.location="<%=forumHelper.getSiteGWTURL(request)%>"
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
		<!--#include virtual="./navigator.jsp?locale=<%=locale%>&<%=SectionRedirectHelperInt.SITE_SECTION_SERVLET_PARAM%>=<%=forumHelper.getSectionName()%>"  -->
		<!--#include virtual="./static/warn_no_script_<%=locale%>.html"  -->

		<!---------------------------------------------------------------------------------------------------------->
		<!-- WARNING: Including the static description is disabled, the data is now placed in the page description-->
		<!---------------------------------------------------------------------------------------------------------->
		<!--(#)include virtual="./static/dec_pan_cent_start.html"  -->
		<!--(#)include virtual="./static/forum_<%=locale%>.html"-->
		<!--(#)include virtual="./static/dec_pan_cent_end.html"  -->
		<!---------------------------------------------------------------------------------------------------------->
		
		<%
			try{
				OnePageViewData<ForumMessageData> messages = ForumManagerImpl.searchMessagesStatic( request.getSession(), request, ShortUserData.UNKNOWN_UID, "", searchData);
		%>
		<!--#include virtual="./static/dec_pan_cent_start.html"  -->
				<%=ForumJSPHelper.getForumSearchPageIndexesHTML( request, locale, messages.total_size )%>
		<!--#include virtual="./static/dec_pan_cent_end.html"  -->
		<!--#include virtual="./static/dec_pan_cent_start.html"  -->
		<%
				for( ForumMessageData message : messages.entries ) {
		%>
					<%=ForumJSPHelper.getForumMessageHTML( request, locale, message, true )%>
		<%
				}
		%>
		<!--#include virtual="./static/dec_pan_cent_end.html"  -->
		<%
			} catch( Exception e ) {
		%>
		<!--#include virtual="./static/dec_pan_cent_start.html"  -->
			<b>ERROR while retrieving the new site uploads, error msg: <%=e.getMessage()%><b>
		<!--#include virtual="./static/dec_pan_cent_end.html"  -->
		<%
			}
		%>
	</body>
</html>
