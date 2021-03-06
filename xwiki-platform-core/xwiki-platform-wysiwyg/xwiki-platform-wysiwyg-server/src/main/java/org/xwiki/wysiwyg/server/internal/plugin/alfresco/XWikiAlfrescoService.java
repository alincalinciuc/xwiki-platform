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
package org.xwiki.wysiwyg.server.internal.plugin.alfresco;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.gwt.wysiwyg.client.plugin.alfresco.AlfrescoEntity;
import org.xwiki.gwt.wysiwyg.client.plugin.alfresco.AlfrescoService;
import org.xwiki.gwt.wysiwyg.client.wiki.Attachment;
import org.xwiki.gwt.wysiwyg.client.wiki.EntityReference;
import org.xwiki.gwt.wysiwyg.client.wiki.WikiService;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.wysiwyg.plugin.alfresco.server.AlfrescoTokenManager;
import org.xwiki.wysiwyg.plugin.alfresco.server.AlfrescoConfiguration;
import org.xwiki.wysiwyg.plugin.alfresco.server.SimpleHttpClient;
import org.xwiki.wysiwyg.plugin.alfresco.server.AlfrescoResponseParser;
import org.xwiki.wysiwyg.plugin.alfresco.server.AlfrescoTiket;
import org.xwiki.wysiwyg.server.wiki.EntityReferenceConverter;

/**
 * XWiki specific implementation of {@link AlfrescoService}.
 * 
 * @version $Id$
 */
@Component
@Singleton
public class XWikiAlfrescoService implements AlfrescoService
{
    /**
     * The component used to access the wiki.
     */
    @Inject
    private WikiService wikiService;
    /**
     * The component used to parse the ticket response.
     */
    @Inject
    private AlfrescoTokenManager ticketManager;
    /**
     * The object used to convert between client references and server references.
     */
    private final EntityReferenceConverter entityReferenceConverter = new EntityReferenceConverter();
    /**
     * The component that controls the Alfresco access configuration.
     */
    @Inject
    private AlfrescoConfiguration configuration;
    /**
     * The HTTP client used to make REST requests to Alfresco.
     */
    @Inject
    private SimpleHttpClient httpClient;
    /**
     * The object used to parse the responses received for Alfresco REST requests.
     */
    @Inject
    private AlfrescoResponseParser responseParser;
    /**
     * The object used to parse the responses received for Alfresco REST requests.
     */
    @Inject
    private Logger logger;

    @Override
    public List<AlfrescoEntity> getChildren(EntityReference clientParentReference)
    {
        org.xwiki.model.reference.EntityReference parentReference =
            entityReferenceConverter.convert(clientParentReference);
        if (parentReference == null) {
            return getWikis();
        }
        List<AlfrescoEntity> children = Collections.emptyList();
        switch (parentReference.getType()) {
            case WIKI:
                children = getSpaces(new WikiReference(parentReference));
                break;
            case SPACE:
                children = getDocuments(new SpaceReference(parentReference));
                break;
            case DOCUMENT:
                children = getAttachments(new DocumentReference(parentReference));
                break;
            default:
        }
        return children;
    }

    @Override
    public AlfrescoEntity getParent(EntityReference clientChildReference)
    {
        org.xwiki.model.reference.EntityReference childReference =
            entityReferenceConverter.convert(clientChildReference);
        AlfrescoEntity parent = new AlfrescoEntity();
        if (childReference == null || childReference.getType() == EntityType.WIKI) {
            parent.setReference(new EntityReference());
            parent.setPath(getPath(null));
        } else {
            org.xwiki.model.reference.EntityReference parentReference = childReference.getParent();
            parent.setReference(entityReferenceConverter.convert(parentReference));
            parent.setName(parentReference.getName());
            parent.setPath(getPath(parentReference));
        }
        return parent;
    }
    @Override
    public Boolean hasValidTicket(String ticket1) {
        String ticket = null;
        //get ticket from DB
        AlfrescoTiket dbTiket = ticketManager.getTicket();
        //if there is a ticket for current user,use it
        if (dbTiket != null) {
            ticket = dbTiket.getTiket();
        }
        if (ticket != null) {
            if (ticketManager.validateAuthenticationTicket(ticket)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public Boolean doAuthenticate(String user, String password) {
        logger.error("ANDREI:" + user + "-" + password);
        String tiket = getAuthenticationTicket(user, password);
        return (tiket != null);
    }

    /**
     * @param user the error message to display.
     * @param password the error message to display.
     * @return the authentication ticket
     */
    private String getAuthenticationTicket(String user, String password)
    {
        try {
            String loginURL = configuration.getServerURL() + "/alfresco/service/api/login";
            JSONObject content = new JSONObject();
            logger.error("LOGINURL:" + loginURL);
            content.put("username", user);
            content.put("password", password);
            logger.error("CONTENTRR:" + content.toString());
            String myTicket = httpClient.doPost(loginURL, content.toString(), "application/json; charset=UTF-8",
                    new SimpleHttpClient.ResponseHandler<String>()
                    {
                        public String read(InputStream content)
                        {
                            logger.error("Response:" + content.toString());
                            return responseParser.parseAuthTicket(content);
                        }
                    });
            logger.error("Myticket:" + myTicket);
            ticketManager.setTicket(myTicket);
            logger.error("Myticke22:" + myTicket);
            return myTicket;
        } catch (Exception e) {
            throw new RuntimeException("Failed to request the authentication ticket.", e);
        }
    }
    /**
     * @param entityReference an entity reference
     * @return the path to the specified entity reference
     */
    private String getPath(org.xwiki.model.reference.EntityReference entityReference)
    {
        StringBuilder path = new StringBuilder();
        org.xwiki.model.reference.EntityReference parent = entityReference;
        while (parent != null) {
            path.insert(0, parent.getName()).insert(0, '/');
            parent = parent.getParent();
        }
        if (path.length() == 0) {
            path.append('/');
        }
        return path.toString();
    }

    /**
     * @return the list of wikis
     */
    private List<AlfrescoEntity> getWikis()
    {
        List<AlfrescoEntity> wikis = new ArrayList<AlfrescoEntity>();
        for (String wikiName : wikiService.getVirtualWikiNames()) {
            AlfrescoEntity wiki = new AlfrescoEntity();
            WikiReference wikiReference = new WikiReference(wikiName);
            wiki.setReference(entityReferenceConverter.convert(wikiReference));
            wiki.setName(wikiName);
            wiki.setPath(getPath(wikiReference));
            wikis.add(wiki);
        }
        return wikis;
    }

    /**
     * @param wikiReference a wiki reference
     * @return the list of spaces inside the specified wiki
     */
    private List<AlfrescoEntity> getSpaces(WikiReference wikiReference)
    {
        List<AlfrescoEntity> spaces = new ArrayList<AlfrescoEntity>();
        for (String spaceName : wikiService.getSpaceNames(wikiReference.getName())) {
            AlfrescoEntity space = new AlfrescoEntity();
            SpaceReference spaceReference = new SpaceReference(spaceName, wikiReference);
            space.setReference(entityReferenceConverter.convert(spaceReference));
            space.setName(spaceName);
            space.setPath(getPath(spaceReference));
            spaces.add(space);
        }
        return spaces;
    }

    /**
     * @param spaceReference a space reference
     * @return the list of documents inside the specified space
     */
    private List<AlfrescoEntity> getDocuments(SpaceReference spaceReference)
    {
        List<AlfrescoEntity> pages = new ArrayList<AlfrescoEntity>();
        String wikiName = spaceReference.getParent().getName();
        for (String pageName : wikiService.getPageNames(wikiName, spaceReference.getName())) {
            AlfrescoEntity page = new AlfrescoEntity();
            DocumentReference documentReference = new DocumentReference(pageName, spaceReference);
            page.setReference(entityReferenceConverter
                .convert((org.xwiki.model.reference.EntityReference) documentReference));
            page.setName(pageName);
            page.setPath(getPath(documentReference));
            pages.add(page);
        }
        return pages;
    }

    /**
     * @param documentReference a document reference
     * @return the list of files attached to the specified document
     */
    private List<AlfrescoEntity> getAttachments(DocumentReference documentReference)
    {
        List<AlfrescoEntity> files = new ArrayList<AlfrescoEntity>();
        for (Attachment attachment : wikiService.getAttachments(entityReferenceConverter.convert(documentReference))) {
            AlfrescoEntity file = new AlfrescoEntity();
            String fileName = entityReferenceConverter.convert(attachment.getReference()).getName();
            AttachmentReference attachmentReference = new AttachmentReference(fileName, documentReference);
            file.setReference(entityReferenceConverter
                .convert((org.xwiki.model.reference.EntityReference) attachmentReference));
            file.setName(fileName);
            file.setMediaType(attachment.getMimeType());
            file.setPreviewURL(attachment.getUrl() + "?width=135");
            file.setPath(getPath(attachmentReference));
            files.add(file);
        }
        return files;
    }
}
