/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.wysiwyg.internal.plugin.alfresco.server;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.container.Container;
import org.xwiki.gwt.wysiwyg.client.plugin.alfresco.AlfrescoService;
import org.xwiki.wysiwyg.plugin.alfresco.server.Authenticator;
import org.xwiki.wysiwyg.plugin.alfresco.server.AlfrescoConfiguration;
import org.xwiki.wysiwyg.plugin.alfresco.server.SimpleHttpClient;
import org.xwiki.wysiwyg.plugin.alfresco.server.AlfrescoResponseParser;
import org.xwiki.wysiwyg.plugin.alfresco.server.AlfrescoTokenManager;
import org.xwiki.wysiwyg.plugin.alfresco.server.AlfrescoTiket;
import org.xwiki.wysiwyg.plugin.alfresco.server.SimpleHttpClient.ResponseHandler;

/**
 * Ticket-based Alfresco authentication.
 * 
 * @version $Id$
 */
@Component
@Named("ticket")
public class TicketAuthenticator implements Authenticator
{
    /**
     * The encoding used for query string parameters.
     */
    private static final String QUERY_STRING_ENCODING = "UTF-8";
    /**
     * The encoding used for query string parameters.
     */
    private static final String APP_ENCODING = "application/json; charset=UTF-8";
    /**
     * The encoding used for query string parameters.
     */
    private static final String APP_USER = "username";
    /**
     * The encoding used for query string parameters.
     */
    private static final String APP_PASSWORD = "password";
    /**
     * The encoding used for query string parameters.
     */
    private static final String APP_LINK = "/alfresco/service/api/login";
    /**
     * The authentication query string parameter.
     */
    private static final String AUTH_TICKET_PARAM = "alf_ticket";
    /**
     * The session attribute used to cache the authentication ticket.
     */
    private static final String AUTH_TICKET_SESSION_ATTRIBUTE = AlfrescoService.class.getPackage().getName()
        + ".ticket";

    /**
     * The component used to access the HTTP session to store the authentication ticket.
     */
    @Inject
    private Container container;
    @Inject
    private Logger logger;
    /**
     * The component that controls the Alfresco access configuration.
     */
    @Inject
    private AlfrescoConfiguration configuration;

    /**
     * The component used to request the authentication ticket.
     */
    @Inject
    @Named("noauth")
    private SimpleHttpClient httpClient;

    /**
     * The component used to parse the ticket response.
     */
    @Inject
    private AlfrescoResponseParser responseParser;
    /**
     * The component used to parse the ticket response.
     */
    @Inject
    private AlfrescoTokenManager ticketManager;

    @Override
    public void authenticate(HttpRequestBase request)
    {
        String ticket = null;
        //get ticket from DB
        AlfrescoTiket dbTiket = ticketManager.getTicket();
        //if there is a ticket for current user,use it
        if (dbTiket != null) {
            ticket = dbTiket.getTiket();
        }
        /*if (ticket != null) {
            if (!ticketManager.validateAuthenticationTicket(ticket)) {
                //if ticket is not valid on alfresco, perform authentication
                ticket = getAuthenticationTicket();
            }
        } else {
            //if there is no ticket in DB, perform authentication
            ticket = getAuthenticationTicket();
        }*/

        // Add the ticket to the query string.
        URI uri = request.getURI();
        List<NameValuePair> parameters = URLEncodedUtils.parse(uri, QUERY_STRING_ENCODING);
        parameters.add(new BasicNameValuePair(AUTH_TICKET_PARAM, ticket));
        String query = URLEncodedUtils.format(parameters, QUERY_STRING_ENCODING);
        try {
            request.setURI(URIUtils.createURI(uri.getScheme(), uri.getHost(), uri.getPort(), uri.getRawPath(), query,
                uri.getRawFragment()));
        } catch (URISyntaxException e) {
            // This shouldn't happen.
        }
    }

    /**
     * @return the authentication ticket
     */
    private String getAuthenticationTicket()
    {
        try {
            String loginURL = configuration.getServerURL() + APP_LINK;
            JSONObject content = new JSONObject();
            //AuthDialog auth = new AuthDialog();
            //auth.show();
            content.put(APP_USER, configuration.getUserName());
            content.put(APP_PASSWORD, configuration.getPassword());
            String myTicket = httpClient.doPost(loginURL, content.toString(), APP_ENCODING,
                new ResponseHandler<String>()
                {
                    public String read(InputStream content)
                    {
                        return responseParser.parseAuthTicket(content);
                    }
                });
            ticketManager.setTicket(myTicket);
            return myTicket;
        } catch (Exception e) {
            throw new RuntimeException("Failed to request the authentication ticket.", e);
        }
    }
}
