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
package org.xwiki.gwt.wysiwyg.client.plugin.alfresco;

import java.util.List;

import org.xwiki.gwt.wysiwyg.client.wiki.EntityReference;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The service used to access an Alfresco content management system.
 * 
 * @version $Id$
 */
public interface AlfrescoServiceAsync
{
    /**
     * Retrieve the list of children of the specified Alfresco entity.
     * 
     * @param parentReference specifies the parent entity
     * @param callback the object to be notified when the list of children is ready
     */
    void getChildren(EntityReference parentReference, AsyncCallback<List<AlfrescoEntity>> callback);

    /**
     * Retrieve the parent of the specified Alfresco entity.
     * 
     * @param childReference the Alfresco entity whose parent needs to be retrieved
     * @param callback the object to be notified when the parent is available
     */
    void getParent(EntityReference childReference, AsyncCallback<AlfrescoEntity> callback);
    /**
     * Retrieve the parent of the specified Alfresco entity.
     *
     * @param ticket the Alfresco entity whose parent needs to be retrieved
     * @param callback the object to be notified when the parent is available
     */
    void hasValidTicket(String ticket, AsyncCallback<Boolean> callback);
    /**
     * Retrieve the parent of the specified Alfresco entity.
     *
     * @param user the Alfresco entity whose parent needs to be retrieved
     * @param password the Alfresco entity whose parent needs to be retrieved
     * @param callback the object to be notified when the parent is available
     */
    void doAuthenticate(String user, String password, AsyncCallback<Boolean> callback);
}
