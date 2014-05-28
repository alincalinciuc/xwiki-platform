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

import org.xwiki.gwt.wysiwyg.client.wiki.Entity;

/**
 * An AlfrescoCredentials entity (e.g. a space or a document).
 *
 * @version $Id$
 */
public class AlfrescoCredentials extends Entity
{
    /**
     * The username.
     */
    private String username;

    /**
     * The password.
     */
    private String password;
    /**
     * @return the username of this entity
     */
    public String getUsername() {
        return username;
    }
    /**
     * Sets the username of this entity.
     *
     * @param username the new entity name
     */
    public void setUsername(String username) {
        this.username = username;
    }
    /**
     * @return the password of this entity
     */
    public String getPassword() {
        return password;
    }
    /**
     * Sets the password of this entity.
     *
     * @param password the new entity name
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
