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
<!--                                            -->
<!-- Enable the SSI page processing by this     -->
<!--                                            -->
<%@ page contentType="text/html;charset=UTF-8" %>
<%@page language="java" import="com.xcurechat.server.utils.WebUtilities" %>
<%@page language="java" import="com.xcurechat.server.redirect.SectionRedirectHelperInt" %>
<%@page language="java" import="com.xcurechat.server.redirect.ChatRedirectHelper" %>
<%@page language="java" import="com.xcurechat.server.redirect.ForumRedirectHelper" %>
<%@page language="java" import="com.xcurechat.server.redirect.MainRedirectHelper" %>
<%@page language="java" import="com.xcurechat.server.redirect.InfoRedirectHelper" %>
<%
	final String locale = WebUtilities.getLocaleValue(request);
	
	String chatTitle, forumTitle, infoTitle, mainTitle;
	if( locale.equals("ru") ) {
		chatTitle  = "Чат";
		forumTitle = "Форум";
		infoTitle  = "О сайте";
		mainTitle  = "Новое";
	} else {
		chatTitle  = "Chat";
		forumTitle = "Forum";
		infoTitle  = "About the website";
		mainTitle  = "News";
	}

	String isChatEnabled = "_dis", isForumEnabled = "_dis", isInfoEnabled = "_dis", isNewsEnabled = "_dis";
	String section = request.getParameter( SectionRedirectHelperInt.SITE_SECTION_SERVLET_PARAM );
	if( section == null ) {
		isNewsEnabled = "";
	} else {
		if( section.equals( (new MainRedirectHelper()).getSectionName() ) ) {
			isNewsEnabled = "";
		} else {
			if( section.equals( (new ChatRedirectHelper()).getSectionName() ) ) {
				isChatEnabled = "";
			} else {
				if( section.equals( (new InfoRedirectHelper()).getSectionName() ) ) {
				    isInfoEnabled = "";
				} else {
					if( section.equals( (new ForumRedirectHelper()).getSectionName() ) ) {
						isForumEnabled = "";
					}
				}
			}
		}
	}
%>

<table cellspacing="0" cellpadding="0" style="width: 100%; " class="xcure-Chat-Main-Title-Panel-Style">
	<tbody>
		<tr>
			<td align="left" style="vertical-align: top; " width="30%">
				<table cellspacing="0" cellpadding="0">
					<tbody>
						<tr>
							<td align="left" style="vertical-align: middle; ">
								<img class="gwt-Image" style="width: 431px; height: 80px;" src="images/backgrounds/logo/logo_1.png" alt="Тайничок"/>
							</td>
						</tr>
					</tbody>
				</table>
			</td>
			<td align="center" style="vertical-align: top; " width="30%">
				<table cellspacing="0" cellpadding="0">
					<tbody>
						<tr>
							<td align="left" style="vertical-align: top; ">
								<a tabindex="0" class="gwt-Anchor xcure-Chat-Site-Section-Link"
									href="<%=SectionRedirectHelperInt.getSiteSectionServletURL( request, ChatRedirectHelper.class, null )%>">
									<img class="xcure-Chat-Site-Navigation-Title-Image" src="images/site_manager/button_chat_<%=locale%><%=isChatEnabled%>.png" alt="<%=chatTitle%>"/>
								</a>
							</td>
							<td align="left" style="vertical-align: top; ">
								<a tabindex="0" class="gwt-Anchor xcure-Chat-Site-Section-Link"
									href="<%=SectionRedirectHelperInt.getSiteSectionServletURL( request, ForumRedirectHelper.class, null )%>">
									<img class="xcure-Chat-Site-Navigation-Title-Image" src="images/site_manager/button_forum_<%=locale%><%=isForumEnabled%>.png" alt="<%=forumTitle%>"/>
								</a>
							</td>
							<td align="left" style="vertical-align: top; ">
								<a tabindex="0" class="gwt-Anchor xcure-Chat-Site-Section-Link"
									href="<%=SectionRedirectHelperInt.getSiteSectionServletURL( request, InfoRedirectHelper.class, null )%>">
									<img class="xcure-Chat-Site-Navigation-Title-Image" src="images/site_manager/button_info_<%=locale%><%=isInfoEnabled%>.png" alt="<%=infoTitle%>"/>
								</a>
							</td>
							<td align="left" style="vertical-align: top; ">
								<a tabindex="0" class="gwt-Anchor xcure-Chat-Site-Section-Link"
									href="<%=SectionRedirectHelperInt.getSiteSectionServletURL( request, MainRedirectHelper.class, null )%>">
									<img class="xcure-Chat-Site-Navigation-Title-Image" src="images/site_manager/button_main_<%=locale%><%=isNewsEnabled%>.png" alt="<%=mainTitle%>"/>
								</a>
							</td>
						</tr>
					</tbody>
				</table>
			</td>
			<td align="center" style="vertical-align: right; " width="40%">
				<%if( locale.equals("ru") ) {%>
					<img src="images/site_manager/locale_ru.png" alt="Русский"/>
				<% } else { %>
					<a style="color: yellow; font-weight: bold; font-size: 15px;" href="<%=section%>?locale=ru"><img width="36px" height="36px" src="images/site_manager/locale_ru.png" alt="Русский"/></a>
				<%}%>
				&nbsp;
				<%if( locale.equals("en") ) {%>
					<img src="images/site_manager/locale_en.png" alt="English"/>
				<% } else { %>
				<a style="color: yellow; font-weight: bold; font-size: 15px;" href="<%=section%>?locale=en"><img width="36px" height="36px" src="images/site_manager/locale_en.png" alt="English"/></a>
				<%}%>
			</td>
		</tr>
	</tbody>
</table>
