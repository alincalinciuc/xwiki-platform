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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import org.xwiki.gwt.user.client.FocusCommand;
import org.xwiki.gwt.user.client.ui.VerticalResizePanel;
import org.xwiki.gwt.user.client.ui.wizard.AbstractInteractiveWizardStep;
import org.xwiki.gwt.user.client.ui.wizard.NavigationListener;
import org.xwiki.gwt.user.client.ui.wizard.NavigationListenerCollection;
import org.xwiki.gwt.user.client.ui.wizard.SourcesNavigationEvents;
import org.xwiki.gwt.wysiwyg.client.wiki.EntityConfig;
import org.xwiki.gwt.wysiwyg.client.wiki.EntityLink;

/**
 * Wizard step that selects an Alfresco entity.
 *
 * @version $Id$
 */

public class AlfrescoCredentialGetterWizardStep extends AbstractInteractiveWizardStep
        implements SourcesNavigationEvents, KeyPressHandler
{
    /**
     * The default style of the link configuration dialog.
     */
    public static final String DEFAULT_STYLE_NAME = "xLinkConfig";

    /**
     * The text box where the user will insert the text of the link to create.
     */
    private final TextBox userTextBox = new TextBox();

    /**
     * The label to signal the error on the label field of this form.
     */
    private final Label labelErrorLabel = new Label();

    /**
     * The text box to get the link tooltip.
     */
    private final PasswordTextBox passwordTextBox = new PasswordTextBox();

    /**
     * The credentials.
     */
    private AlfrescoCredentials credentials = new AlfrescoCredentials();


    /**
     * The service used to access an Alfresco content management system.
     */
    private AlfrescoServiceAsync alfrescoService;
    /**
     * The object processed by this wizard step.
     */
    private EntityLink<EntityConfig> entityLink;
    /**
     * Navigation listeners to be notified by navigation events from this step. It generates navigation to the next step
     * when an item is double clicked in the list, or enter key is pressed on a selected item.
     */
    private final NavigationListenerCollection navigationListeners = new NavigationListenerCollection();

    /**
     * Creates a new instance.
     *
     * @param alfrescoService the error message to display.
     */
    public AlfrescoCredentialGetterWizardStep(AlfrescoServiceAsync alfrescoService)
    {
        this.alfrescoService = alfrescoService;
        Label userLabel = new Label("Alfresco Username");
        Label passwordLabel = new Label("Alfresco Password");
        display().addStyleName(DEFAULT_STYLE_NAME);
        display().add(userLabel);
        userTextBox.addKeyPressHandler(this);
        passwordTextBox.addKeyPressHandler(this);
        display().add(userTextBox);
        display().add(passwordLabel);
        display().add(passwordTextBox);
        labelErrorLabel.addStyleName("xErrorMsg");
        display().add(labelErrorLabel);
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractInteractiveWizardStep#init(Object, AsyncCallback)
     */
    @SuppressWarnings("unchecked")
    public void init(Object data, final AsyncCallback< ? > callback)
    {
        entityLink = (EntityLink<EntityConfig>) data;
        //credentials = (AlfrescoCredentials) data;
        //userTextBox.setText(credentials.getUsername());
        //passwordTextBox.setText(credentials.getPassword());
        hideErrors();
        callback.onSuccess(null);
        setFocus();
    }

    /**
     * @return the labelTextBox
     */
    protected TextBox getUserTextBox()
    {
        return userTextBox;
    }

    /**
     * @return the tooltipTextBox
     */
    public TextBox getPasswordTextBox()
    {
        return passwordTextBox;
    }
    /**
     * @return the labelTextBox
     */
    public String getUserText()
    {
        return userTextBox.getText().trim();
    }

    /**
     * @return the tooltipTextBox
     */
    public String getPasswordText()
    {
        return passwordTextBox.getText().trim();
    }
    /**
     * {@inheritDoc}
     *
     * @see AbstractInteractiveWizardStep#onSubmit(AsyncCallback)
     */
    public void onSubmit(AsyncCallback<Boolean> callback)
    {
        // first reset all error labels, consider everything's fine
        hideErrors();
        // validate and go to next step if everything's fine
        if (validate()) {
            saveForm(callback);
        } else {
            callback.onSuccess(false);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractInteractiveWizardStep#onCancel()
     */
    public void onCancel()
    {
    }
    /**
     * Hides the error message and markers for this dialog.
     */
    protected void hideErrors()
    {
        labelErrorLabel.setVisible(false);
    }
    /**
     * Sets the default focus in this wizard step.
     */
    protected void setFocus()
    {
        Scheduler.get().scheduleDeferred(new FocusCommand(userTextBox.isEnabled() ? userTextBox : passwordTextBox));
    }
    /**
     * {@inheritDoc}
     *
     * @see AbstractInteractiveWizardStep#display()
     */
    @Override
    public VerticalResizePanel display()
    {
        return (VerticalResizePanel) super.display();
    }

    @Override
    public Object getResult() {
        return entityLink;
    }

    /**
     * @return {@code true} if the current selection is valid, {@code false} otherwise
     */
    private boolean validate()
    {
        if (userTextBox.getText().trim().length() == 0) {
            displayLabelError("Invalid username");
            return false;
        }
        if (passwordTextBox.getText().trim().length() == 0) {
            displayLabelError("Invalid password");
            return false;
        }
        return true;
    }
    /**
     * Display the label error message and markers.
     *
     * @param errorMessage the error message to display.
     */
    protected void displayLabelError(String errorMessage)
    {
        labelErrorLabel.setText(errorMessage);
        labelErrorLabel.setVisible(true);
    }

    /**
     * sdadsadsa.
     *
     */
    protected void saveForm(final AsyncCallback<Boolean> callback)
    {
        credentials.setUsername(userTextBox.getText().trim());
        credentials.setPassword(passwordTextBox.getText().trim());
        // try to login
        AsyncCallback loginCallback = new AsyncCallback<Boolean>() {
            public void onFailure(Throwable caught) {
                displayLabelError("Canot access server");
            }

            public void onSuccess(Boolean has) {
                if (!has) {
                    displayLabelError("Invalid username or password");
                    callback.onSuccess(false);
                } else {
                    callback.onSuccess(true);
                }
            }
        };
        alfrescoService.doAuthenticate(
                userTextBox.getText().trim(),
                passwordTextBox.getText().trim(), loginCallback);
    }
    /**
     * @return the default navigation direction, to be fired automatically when enter is hit in an input in the form of
     *         this configuration wizard step. To be overridden by subclasses to provide the specific direction to be
     *         followed.
     */
    public NavigationListener.NavigationDirection getDefaultDirection()
    {
        return NavigationListener.NavigationDirection.NEXT;
    }
    /**
     * {@inheritDoc}
     *
     * @see SourcesNavigationEvents#addNavigationListener(NavigationListener)
     */
    public void addNavigationListener(NavigationListener listener)
    {
        navigationListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     *
     * @see SourcesNavigationEvents#removeNavigationListener(NavigationListener)
     */
    public void removeNavigationListener(NavigationListener listener)
    {
        navigationListeners.remove(listener);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.google.gwt.event.dom.client.KeyPressHandler#onKeyPress(com.google.gwt.event.dom.client.KeyPressEvent)
     */
    public void onKeyPress(KeyPressEvent event)
    {
        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
            // fire the event for the default direction
            navigationListeners.fireNavigationEvent(getDefaultDirection());
        }
    }

    /**
     * @return the data configured by this wizard step
     */
    protected AlfrescoCredentials getData()
    {
        return credentials;
    }
}