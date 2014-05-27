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

package org.xwiki.wysiwyg.plugin.alfresco.server;
import org.xwiki.stability.Unstable;
/**
 * Allow initializing and retrieving the tikets for alfresco autentication.
 *
 * @version $Id$
 * @since 5.2M2
 */
@Unstable
public class AlfrescoTiket
{
    private String id;
    private String user;
    private String tiket;
    /**
     * Default constructor. It is need for Hibernate.
     */
    public AlfrescoTiket() { }
    /**
     * @param xuser the user on xwiki
     * @param atiket the tiket on alfresco
     */
    public AlfrescoTiket(String xuser, String atiket) {
        this.user = xuser;
        this.tiket = atiket;
    }
    /**
     * @return the id for alfresco
     */
    public String getId() {
        return id;
    }
    /**
     * @param id the tiket for alfresco as a String
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * @return the tiket for alfresco
     */
    public String getTiket() {
        return tiket;
    }
    /**
     * @param tiket the tiket for alfresco as a String
     */
    public void setTiket(String tiket) {
        this.tiket = tiket;
    }
    /**
     * @return the xwiki user
     */
    public String getUser() {
        return user;
    }
    /**
     * @param user the xwiki user
     */
    public void setUser(String user) {
        this.user = user;
    }
    @Override
    public String toString()
    {
        return this.tiket.toString();
    }
    @Override
    public int hashCode()
    {
        return this.tiket.hashCode();
    }
    @Override public boolean equals(Object o)
    {
        return this.equals(o);
    }
}
