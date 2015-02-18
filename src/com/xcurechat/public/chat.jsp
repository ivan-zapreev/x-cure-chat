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
<%@page language="java" import="com.xcurechat.server.utils.WebUtilities" %>
<%@page language="java" import="com.xcurechat.server.redirect.SectionRedirectHelperInt" %>
<%@page language="java" import="com.xcurechat.server.redirect.ChatRedirectHelper" %>
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
				pageTitle = "Тайничок - Чат";
				siteDescription = "Чат на Тайничке, здесь вы можете: Прикреплять фотографии " +
								  "(jpg, png, ...) музыку (mp3) и видео (mp4, flv, ...) к вашим " +
								  "сообщениям в чате. Выбирать из более 500 различных прикольных " +
								  "смайликов! Использовать транслитерацию (перевод латиницы в " +
								  "русские буквы) практически в любом поле и окне! Использовать " +
								  "потоки сообщений в чате для лучшей визуализации бесед! Создавать " +
								  "общие и личные чат комнаты, а так же управлять пользователями в " +
								  "последних. Отправлять общие/личные сообщения в чате, и оставлять " +
								  "оффлайн сообщения тем, кого нет на сайте. У нас совершенно нет " +
								  "модераторов, потому что здесь каждый пользователь-модератор! " +
								  "Вы можете скрывать сообщения сворачивая в чате их, а так же " +
								  "блокировать пользователей! Во время чата, вы можете слушать различные " +
								  "радио станции, у нас есть Топ 40, Русское радио, Танцевальная и " +
								  "Альтернативная музыка и многое другое!";
				keywords		= "чат, Тайничок, общение, новые знакомства, фото, видеа, музыка, слушать, радио, онлайн";
			} else {
				pageTitle = "Tainichok - Chat";
				siteDescription = "Chat at Tainichok, here you can: Attach images (jpg, png, ...) music " +
								  "(mp3) and videos (mp4, flv, ...) to your chat messages. Choose from " +
								  "more than 500 funny smiles! Transliterate text, i.e. to convert from " +
								  "Latin to Russian characters, almost in every field and dialog window! " +
								  "Take advantage of message flows that allow for a better visualization of " +
								  "chat conversations! Create public and private chat rooms, and manage " +
								  "users in the latter ones. Send public and private chat messages as well " +
								  "as offline messages to users who are offline. We do not have any moderators " +
								  "because each of us is a moderator for his own! You can hide message content " +
								  "by clicking on the chat message's title, and block messages from users!" +
								  "While chatting, you can also listen to various radio stations, we have Top " +
								  "40, Russian radio, Dance, Alternative and many others!";
				keywords		= "chat, Tainichok, talk to people, make, new, friends, share, pictures, music, videos, listen, radio, online";
			}
			final ChatRedirectHelper chatHelper = new ChatRedirectHelper();
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
			window.location="<%=chatHelper.getSiteGWTURL(request)%>"
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
		<!--#include virtual="./navigator.jsp?locale=<%=locale%>&<%=SectionRedirectHelperInt.SITE_SECTION_SERVLET_PARAM%>=<%=chatHelper.getSectionName()%>"  -->
		<!--#include virtual="./static/warn_no_script_<%=locale%>.html"  -->

		<!--#include virtual="./static/dec_pan_cent_start.html"  -->
		<!--#include virtual="./static/chat_<%=locale%>.html"-->
		<!--#include virtual="./static/dec_pan_cent_end.html"  -->
	</body>
</html>
