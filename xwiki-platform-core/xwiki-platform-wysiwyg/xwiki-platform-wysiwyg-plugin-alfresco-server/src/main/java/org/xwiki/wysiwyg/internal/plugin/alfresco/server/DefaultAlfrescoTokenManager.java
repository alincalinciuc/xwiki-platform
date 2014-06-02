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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.NodeList;
import org.xwiki.component.annotation.Component;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.store.XWikiHibernateBaseStore;
import com.xpn.xwiki.store.XWikiStoreInterface;
import org.xwiki.wysiwyg.plugin.alfresco.server.AlfrescoTokenManager;
import org.xwiki.wysiwyg.plugin.alfresco.server.AlfrescoConfiguration;
import org.xwiki.wysiwyg.plugin.alfresco.server.SimpleHttpClient;
import org.xwiki.wysiwyg.plugin.alfresco.server.AlfrescoResponseParser;
import org.xwiki.wysiwyg.plugin.alfresco.server.AlfrescoTiket;

import javax.inject.Provider;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Allow initializing and retrieving the tikets for alfresco autentication.
 *
 * @version $Id$
 * @since 5.2M2
 */
@Component
@Singleton
public class DefaultAlfrescoTokenManager implements AlfrescoTokenManager
{
    /**
     * The authentication query string parameter.
     */
    private static final String AUTH_TICKET_PARAM = "alf_ticket";
    /**
     * The authentication query string parameter.
     */
    private static final String TRUE_PARAM = "true";
    @Inject
    @Named("hibernate")
    private XWikiStoreInterface hibernateStore;
    @Inject
    private Provider<XWikiContext> xcontextProvider;
    @Inject
    private Logger logger;
    private String userfield = "user";
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
     * The object used to parse the responses received for Alfresco REST requests.
     */
    @Inject
    private AlfrescoResponseParser responseParser;


    @Override
    public void setTicket(String atiket) {
        final XWikiContext context = getXWikiContext();
        XWikiHibernateBaseStore store = (XWikiHibernateBaseStore) this.hibernateStore;
        String originalDatabase = context.getDatabase();
        context.setDatabase(context.getMainXWiki());
        final String usr = context.getUser();
        final AlfrescoTiket newTiket = new AlfrescoTiket(usr, atiket);
        try {
            store.executeWrite(context, new XWikiHibernateBaseStore.HibernateCallback<Object>()
            {
                @Override
                public Object doInHibernate(Session session) throws HibernateException
                {
                    AlfrescoTiket tiket = (AlfrescoTiket) session.createCriteria(AlfrescoTiket.class).add(
                            Restrictions.eq(userfield, usr)).uniqueResult();
                    if (tiket != null) {
                        session.delete(tiket);
                    }
                    session.save(newTiket);
                    logger.error("ANDREI:SAVED-" + newTiket.getTiket());
                    return null;
                }
            });
        } catch (XWikiException e) {
            this.logger.warn("Failed to save user-token to database. Reason: [{}]",
                    ExceptionUtils.getRootCauseMessage(e));
        } finally {
            context.setDatabase(originalDatabase);
        }
    }
    /**
     * @return alfresco tiket
     *
     */
    @Override
    public AlfrescoTiket getTicket() {

        XWikiContext context = getXWikiContext();
        XWikiHibernateBaseStore store = (XWikiHibernateBaseStore) this.hibernateStore;
        String originalDatabase = context.getDatabase();
        context.setDatabase(context.getMainXWiki());
        final String usr = context.getUser();
        try {
            AlfrescoTiket alfToken = store.failSafeExecuteRead(context,
                    new XWikiHibernateBaseStore.HibernateCallback<AlfrescoTiket>()
                    {
                        @Override
                        public AlfrescoTiket doInHibernate(Session session) throws HibernateException {
                            return (AlfrescoTiket) session.createCriteria(AlfrescoTiket.class).add(
                                    Restrictions.eq(userfield, usr)).uniqueResult();
                        }
                    });
            return alfToken;
        } catch (Exception e) {
            this.logger.warn("Failed to get user-token to database. Reason: [{}]",
                            ExceptionUtils.getRootCauseMessage(e));
        } finally {
            context.setDatabase(originalDatabase);
        }
        return null;
    }
    @Override
    public Boolean validateAuthenticationTicket(String ticket)
    {
        try {
            String validateURL = configuration.getServerURL() + "/alfresco/service/api/login/ticket/" + ticket;
            List<Map.Entry<String, String>> parameters =
                    Collections.<Map.Entry<String, String>> singletonList(
                            new AbstractMap.SimpleEntry<String, String>(AUTH_TICKET_PARAM, ticket));
            String myTicket = httpClient.doGet(validateURL, parameters, new SimpleHttpClient.ResponseHandler<String>()
            {
                public String read(InputStream content)
                {
                    NodeList ticket1 = responseParser.parseXML(content).getElementsByTagName("ticket");
                    if (ticket1.getLength() > 0) {
                        return TRUE_PARAM;
                    }
                    return "false";
                }
            });
            return myTicket.equals(TRUE_PARAM);
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate the authentication ticket.", e);
        }
    }
    @Override
    public String getAuthenticationTicket(String user, String password)
    {
        try {
            String loginURL = configuration.getServerURL() + "/alfresco/service/api/login";
            JSONObject content = new JSONObject();
            content.put("username", user);
            content.put("password", password);
            String myTicket = httpClient.doPost(loginURL, content.toString(), "application/json; charset=UTF-8",
                    new SimpleHttpClient.ResponseHandler<String>()
                    {
                        public String read(InputStream content)
                        {
                            return responseParser.parseAuthTicket(content);
                        }
                    });
            this.setTicket(myTicket);
            return myTicket;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    private XWikiContext getXWikiContext() {
        return this.xcontextProvider.get();
    }
}