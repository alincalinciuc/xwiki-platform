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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Pops a dialog to ask after alfresco credentials.
 *
 * @version $Id$
 * @since 5.2M2
 */
public class AuthDialog extends DialogBox
{
    private String user;
    /**
     * The component used to request the credentials.
     */
    public AuthDialog() {
        setText("Auth dialog");
        Button ok = new Button("OK");
        ok.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                AuthDialog.this.hide();
            }
        });
        setWidget(ok);
    }
    /**
     * @return the user forom input
     */
    public String getUser() {
        return user;
    }
}
