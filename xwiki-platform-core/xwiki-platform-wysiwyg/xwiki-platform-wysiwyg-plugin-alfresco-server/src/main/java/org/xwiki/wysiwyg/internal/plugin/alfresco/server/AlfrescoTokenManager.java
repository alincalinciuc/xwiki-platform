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
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.store.XWikiHibernateBaseStore;
import com.xpn.xwiki.store.XWikiStoreInterface;
import org.xwiki.wysiwyg.plugin.alfresco.server.AlfrescoTiket;
import org.xwiki.wysiwyg.plugin.alfresco.server.AlfrescoTokenManagerInterface;
/**
 * Allow initializing and retrieving the tikets for alfresco autentication.
 *
 * @version $Id$
 * @since 5.2M2
 */
@Component
public class AlfrescoTokenManager implements AlfrescoTokenManagerInterface
{
    @Inject
    @Named("hibernate")
    private XWikiStoreInterface hibernateStore;
    @Inject
    private Execution execution;
    @Inject
    private Logger logger;
    /**
     * @param xuser the user on xwiki
     * @param atiket the tiket on alfresco
     */
    public void setTicket(String xuser, String atiket) {
        XWikiContext context = getXWikiContext();
        XWikiHibernateBaseStore store = (XWikiHibernateBaseStore) this.hibernateStore;
        String originalDatabase = context.getDatabase();
        context.setDatabase(context.getMainXWiki());
        final AlfrescoTiket newTiket = new AlfrescoTiket(xuser, atiket);
        try {
            store.executeWrite(context, new XWikiHibernateBaseStore.HibernateCallback<Object>()
            {
                @Override
                public Object doInHibernate(Session session) throws HibernateException
                {
                    session.save(newTiket);
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
     * @param user the user on xwiki
     * @return alfresco tiket
     *
     */
    public AlfrescoTiket getTicket(String user) {
        final String usr = user;
        XWikiContext context = getXWikiContext();
        XWikiHibernateBaseStore store = (XWikiHibernateBaseStore) this.hibernateStore;
        String originalDatabase = context.getDatabase();
        context.setDatabase(context.getMainXWiki());
        try {
            AlfrescoTiket alfToken = store.failSafeExecuteRead(context,
                    new XWikiHibernateBaseStore.HibernateCallback<AlfrescoTiket>()
                    {
                        @Override
                        public AlfrescoTiket doInHibernate(Session session) throws HibernateException {
                            return (AlfrescoTiket) session.createCriteria(AlfrescoTiket.class).add(
                                    Restrictions.eq("user", usr)).uniqueResult();
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
    private XWikiContext getXWikiContext() {
        ExecutionContext context = this.execution.getContext();
        return (XWikiContext) context.getProperty("xwikicontext");
    }
}