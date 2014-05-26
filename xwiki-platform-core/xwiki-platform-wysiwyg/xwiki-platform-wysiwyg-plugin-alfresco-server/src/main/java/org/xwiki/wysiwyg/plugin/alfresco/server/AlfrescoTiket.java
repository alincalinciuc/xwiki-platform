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

@Unstable
public class AlfrescoTiket
{
    private String user;

    private String tiket;

    public AlfrescoTiket()
    {
    }

    public AlfrescoTiket(String _user,String _tiket){
        this.user = _user;
        this.tiket = _tiket;
    }


    public String getTiket() {
        return tiket;
    }

    public void setTiket(String tiket) {
        this.tiket = tiket;
    }

    public String getUser() {
        return user;
    }

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
