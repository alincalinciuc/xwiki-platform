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

package com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.BooleanClass;

/**
 * Abstract implementation of XClassManager.
 * <p>
 * This class has to be extended with at least :
 * <ul>
 * <li>overload {@link #updateBaseClass(BaseClass)}
 * <li>in constructor call AbstractXClassManager constructor with a name that will be used to generate all the documents
 * and spaces needed.
 * </ul>
 * 
 * @param <T> the item class extending {@link XObjectDocument}.
 * @version $Id: $
 * @see XClassManager
 * @since Application Manager 1.0RC1
 */
public abstract class AbstractXClassManager<T extends XObjectDocument> implements XClassManager<T>
{
    /**
     * FullName of the default parent page for a document containing xwiki class.
     */
    private static final String DEFAULT_XWIKICLASS_PARENT = "XWiki.XWikiClasses";

    /**
     * The resource file extension containing pages contents.
     */
    private static final String DOCUMENTCONTENT_EXT = ".vm";

    /**
     * Resource path prefix for the class sheets documents content.
     */
    private static final String DOCUMENTCONTENT_SHEET_PREFIX = "sheets/";

    /**
     * Resource path prefix for the class templates documents content.
     */
    private static final String DOCUMENTCONTENT_TEMPLATE_PREFIX = "templates/";

    /**
     * Symbol used in HQL request to insert and protect value when executing the request.
     */
    private static final String HQL_PARAMETER_STRING = "?";

    /**
     * Space prefix of class document.
     * 
     * @see #getClassSpace()
     */
    private final String classSpacePrefix;

    /**
     * Prefix of class document.
     * 
     * @see #getClassPrefix()
     */
    private final String classPrefix;

    /**
     * Space of class document.
     * 
     * @see #getClassSpace()
     */
    private final String classSpace;

    /**
     * Name of class document.
     * 
     * @see #getClassName()
     */
    private final String className;

    /**
     * Full name of class document.
     * 
     * @see #getClassFullName()
     */
    private final String classFullName;

    /**
     * Space of class sheet document.
     * 
     * @see #getClassSpace()
     */
    private final String classSheetSpace;

    /**
     * Name of class sheet document.
     * 
     * @see #getClassSheetName()
     */
    private final String classSheetName;

    /**
     * Full name of class sheet document.
     * 
     * @see #getClassSheetFullName()
     */
    private final String classSheetFullName;

    /**
     * Space of class template document.
     * 
     * @see #getClassSpace()
     */
    private final String classTemplateSpace;

    /**
     * Name of class template document.
     * 
     * @see #getClassTemplateName()
     */
    private final String classTemplateName;

    /**
     * Full name of class template document.
     * 
     * @see #getClassTemplateFullName()
     */
    private final String classTemplateFullName;

    /**
     * Default content of class template document.
     */
    private final String classSheetDefaultContent;

    /**
     * Default content of class sheet document.
     */
    private final String classTemplateDefaultContent;

    /**
     * Base class managed.
     */
    private BaseClass baseClass;

    /**
     * Constructor for AbstractXClassManager.
     * 
     * @param prefix the prefix of class document.
     * @see #AbstractXClassManager(String, String)
     * @see #AbstractXClassManager(String, String, boolean)
     */
    protected AbstractXClassManager(String prefix)
    {
        this(XWIKI_CLASS_SPACE_PREFIX, prefix);
    }

    /**
     * Constructor for AbstractXClassManager.
     * 
     * @param spaceprefix the space prefix of class document.
     * @param prefix the prefix of class document.
     * @see #AbstractXClassManager(String)
     * @see #AbstractXClassManager(String, String, boolean)
     */
    protected AbstractXClassManager(String spaceprefix, String prefix)
    {
        this(spaceprefix, prefix, true);
    }

    /**
     * Constructor for AbstractXClassManager.
     * 
     * @param spaceprefix the space of class document.
     * @param prefix the prefix of class document.
     * @param dispatch Indicate if it had to use standard XWiki applications space names.
     * @see #AbstractXClassManager(String)
     * @see #AbstractXClassManager(String, String)
     */
    protected AbstractXClassManager(String spaceprefix, String prefix, boolean dispatch)
    {
        this.classSpacePrefix = spaceprefix;
        this.classPrefix = prefix;

        this.classSpace = dispatch ? classSpacePrefix + XWIKI_CLASS_SPACE_SUFFIX : classSpacePrefix;
        this.className = classPrefix + XWIKI_CLASS_SUFFIX;
        this.classFullName = classSpace + XObjectDocument.SPACE_DOC_SEPARATOR + className;

        this.classSheetSpace = dispatch ? classSpacePrefix + XWIKI_CLASSSHEET_SPACE_SUFFIX : classSpacePrefix;
        this.classSheetName = classPrefix + XWIKI_CLASSSHEET_SUFFIX;
        this.classSheetFullName = classSheetSpace + XObjectDocument.SPACE_DOC_SEPARATOR + classSheetName;

        this.classTemplateSpace = dispatch ? classSpacePrefix + XWIKI_CLASSTEMPLATE_SPACE_SUFFIX : classSpacePrefix;
        this.classTemplateName = classPrefix + XWIKI_CLASSTEMPLATE_SUFFIX;
        this.classTemplateFullName = classTemplateSpace + XObjectDocument.SPACE_DOC_SEPARATOR + classTemplateName;

        this.classSheetDefaultContent =
            "## you can modify this page to customize the presentation of your object\n\n"
                + "1 Document $doc.name\n\n#set($class = $doc.getObject(\"" + classFullName + "\").xWikiClass)\n"
                + "\n" + "<dl>\n" + "  #foreach($prop in $class.properties)\n" + "    <dt> ${prop.prettyName} </dt>\n"
                + "    <dd>$doc.display($prop.getName())</dd>\n  #end\n" + "</dl>\n";

        this.classTemplateDefaultContent = "#includeForm(\"" + classSheetFullName + "\")\n";
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.XClassManager#getClassSpacePrefix()
     */
    public String getClassSpacePrefix()
    {
        return this.classSpacePrefix;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#getClassSpace()
     */
    public String getClassSpace()
    {
        return this.classSpace;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#getClassPrefix()
     */
    public String getClassPrefix()
    {
        return this.classPrefix;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#getClassName()
     */
    public String getClassName()
    {
        return this.className;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#getClassFullName()
     */
    public String getClassFullName()
    {
        return this.classFullName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#getClassTemplateName()
     */
    public String getClassTemplateSpace()
    {
        return this.classTemplateSpace;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#getClassTemplateName()
     */
    public String getClassTemplateName()
    {
        return this.classTemplateName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#getClassTemplateFullName()
     */
    public String getClassTemplateFullName()
    {
        return this.classTemplateFullName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#getClassSheetName()
     */
    public String getClassSheetSpace()
    {
        return this.classSheetSpace;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#getClassSheetName()
     */
    public String getClassSheetName()
    {
        return this.classSheetName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#getClassSheetFullName()
     */
    public String getClassSheetFullName()
    {
        return this.classSheetFullName;
    }

    /**
     * Check if all necessary documents for manage this class in this context exists and update. Create if not exists.
     * Thread safe.
     * 
     * @param context the XWiki context.
     * @throws XWikiException error when saving documents.
     * @see #checkClassDocument(XWikiContext)
     */
    protected void check(XWikiContext context) throws XWikiException
    {
        checkClassDocument(context);
        checkClassSheetDocument(context);
        checkClassTemplateDocument(context);
    }

    /**
     * Check if class document exists in this context and update. Create if not exists.
     * 
     * @param context the XWiki context.
     * @throws XWikiException error when saving document.
     */
    private void checkClassDocument(XWikiContext context) throws XWikiException
    {
        XWikiDocument doc;
        XWiki xwiki = context.getWiki();
        boolean needsUpdate = false;

        try {
            doc = xwiki.getDocument(getClassFullName(), context);
        } catch (Exception e) {
            doc = new XWikiDocument();
            doc.setSpace(getClassSpace());
            doc.setName(getClassName());
            doc.setParent(DEFAULT_XWIKICLASS_PARENT);
            needsUpdate = true;
        }

        this.baseClass = doc.getxWikiClass();

        needsUpdate |= updateBaseClass(this.baseClass);

        if (doc.isNew() || needsUpdate) {
            xwiki.saveDocument(doc, context);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#getClassSheetDefaultContent()
     */
    public String getClassSheetDefaultContent()
    {
        return this.classSheetDefaultContent;
    }

    /**
     * Load an entire resource text file into {@link String}.
     * 
     * @param path the path to the resource file.
     * @return the entire content of the resource text file.
     */
    private String getResourceDocumentContent(String path)
    {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(path);

        if (in != null) {
            try {
                StringBuffer content = new StringBuffer(in.available());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                for (String str = reader.readLine(); str != null; str = reader.readLine()) {
                    content.append(str);
                    content.append('\n');
                }

                return content.toString();
            } catch (IOException e) {
                // No resource file as been found or there is a problem when read it.
            }
        }

        return null;
    }

    /**
     * Check if class sheet document exists in this context and update. Create if not exists.
     * 
     * @param context the XWiki context.
     * @throws XWikiException error when saving document.
     */
    private void checkClassSheetDocument(XWikiContext context) throws XWikiException
    {
        XWikiDocument doc;
        XWiki xwiki = context.getWiki();
        boolean needsUpdate = false;

        try {
            doc = xwiki.getDocument(getClassSheetFullName(), context);
        } catch (Exception e) {
            doc = new XWikiDocument();
            doc.setSpace(getClassSheetSpace());
            doc.setName(getClassSheetName());
            doc.setParent(getClassFullName());
            needsUpdate = true;
        }

        if (doc.isNew()) {
            String documentContentPath = DOCUMENTCONTENT_SHEET_PREFIX + getClassSheetFullName() + DOCUMENTCONTENT_EXT;
            String content = getResourceDocumentContent(documentContentPath);
            doc.setContent(content != null ? content : getClassSheetDefaultContent());
        }

        if (doc.isNew() || needsUpdate) {
            xwiki.saveDocument(doc, context);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#getClassTemplateDefaultContent()
     */
    public String getClassTemplateDefaultContent()
    {
        return this.classTemplateDefaultContent;
    }

    /**
     * Check if class template document exists in this context and update. Create if not exists.
     * 
     * @param context the XWiki context.
     * @throws XWikiException error when saving document.
     */
    private void checkClassTemplateDocument(XWikiContext context) throws XWikiException
    {
        XWikiDocument doc;
        XWiki xwiki = context.getWiki();
        boolean needsUpdate = false;

        try {
            doc = xwiki.getDocument(getClassTemplateFullName(), context);
        } catch (Exception e) {
            doc = new XWikiDocument();
            doc.setSpace(getClassTemplateSpace());
            doc.setName(getClassTemplateName());
            needsUpdate = true;
        }

        if (doc.getObject(getClassFullName()) == null) {
            doc.createNewObject(getClassFullName(), context);

            needsUpdate = true;
        }

        if (doc.isNew()) {
            String content =
                getResourceDocumentContent(DOCUMENTCONTENT_TEMPLATE_PREFIX + getClassTemplateFullName()
                    + DOCUMENTCONTENT_EXT);
            doc.setContent(content != null ? content : getClassTemplateDefaultContent());

            doc.setParent(getClassFullName());
        }

        needsUpdate |= updateClassTemplateDocument(doc);

        if (doc.isNew() || needsUpdate) {
            xwiki.saveDocument(doc, context);
        }
    }

    /**
     * @param value the {@link Boolean} value to convert.
     * @return the converted <code>int</code> value.
     */
    protected int intFromBoolean(Boolean value)
    {
        return value == null ? -1 : (value.booleanValue() ? 1 : 0);
    }

    /**
     * Initialize template document with default content.
     * 
     * @param doc the class template document that will be saved.
     * @return true if <code>doc</code> modified.
     */
    protected boolean updateClassTemplateDocument(XWikiDocument doc)
    {
        return false;
    }

    /**
     * Set the value of a boolean field in a document.
     * 
     * @param doc the document to modify.
     * @param fieldName the name of the field.
     * @param value the value.
     * @return true if <code>doc</code> modified.
     */
    protected boolean updateDocStringValue(XWikiDocument doc, String fieldName, String value)
    {
        boolean needsUpdate = false;

        if (value.equals(doc.getStringValue(getClassFullName(), fieldName))) {
            doc.setStringValue(getClassFullName(), fieldName, value);
            needsUpdate = true;
        }

        return needsUpdate;
    }

    /**
     * Set the value of a boolean field in a document.
     * 
     * @param doc the document to modify.
     * @param fieldName the name of the field.
     * @param value the value.
     * @return true if <code>doc</code> modified.
     */
    protected boolean updateDocBooleanValue(XWikiDocument doc, String fieldName, Boolean value)
    {
        boolean needsUpdate = false;

        int intvalue = intFromBoolean(value);

        if (intvalue != doc.getIntValue(getClassFullName(), fieldName)) {
            doc.setIntValue(getClassFullName(), fieldName, intvalue);
            needsUpdate = true;
        }

        return needsUpdate;
    }

    /**
     * Configure BaseClass.
     * 
     * @param baseClass the baseClass to configure.
     * @return true if <code>baseClass</code> modified.
     */
    protected boolean updateBaseClass(BaseClass baseClass)
    {
        boolean needUpdate = false;

        if (!baseClass.getName().equals(getClassFullName())) {
            baseClass.setName(getClassFullName());
            needUpdate = true;
        }

        return needUpdate;
    }

    /**
     * Set the default value of a boolean field of a XWiki class.
     * 
     * @param baseClass the XWiki class.
     * @param fieldName the name of the field.
     * @param value the default value.
     * @return true if <code>baseClass</code> modified.
     */
    protected boolean updateBooleanClassDefaultValue(BaseClass baseClass, String fieldName, Boolean value)
    {
        boolean needsUpdate = false;

        BooleanClass bc = (BooleanClass) baseClass.get(fieldName);

        int old = bc.getDefaultValue();
        int intvalue = intFromBoolean(value);

        if (intvalue != old) {
            bc.setDefaultValue(intvalue);
            needsUpdate = true;
        }

        return needsUpdate;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#getBaseClass()
     */
    public BaseClass getBaseClass()
    {
        if (this.baseClass == null) {
            this.baseClass = new BaseClass();
            updateBaseClass(this.baseClass);
        }

        return this.baseClass;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#getClassDocument(com.xpn.xwiki.XWikiContext)
     */
    public Document getClassDocument(XWikiContext context) throws XWikiException
    {
        check(context);

        return context.getWiki().getDocument(getClassFullName(), context).newDocument(context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#getClassSheetDocument(com.xpn.xwiki.XWikiContext)
     */
    public Document getClassSheetDocument(XWikiContext context) throws XWikiException
    {
        check(context);

        return context.getWiki().getDocument(getClassSheetFullName(), context).newDocument(context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#getClassTemplateDocument(com.xpn.xwiki.XWikiContext)
     */
    public Document getClassTemplateDocument(XWikiContext context) throws XWikiException
    {
        check(context);

        return context.getWiki().getDocument(getClassTemplateFullName(), context).newDocument(context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#isInstance(com.xpn.xwiki.doc.XWikiDocument)
     */
    public boolean isInstance(XWikiDocument doc)
    {
        return doc.getObjectNumbers(getClassFullName()) > 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#isInstance(com.xpn.xwiki.doc.XWikiDocument)
     */
    public boolean isInstance(Document doc)
    {
        return doc.getObjectNumbers(getClassFullName()) > 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#getItemDocumentDefaultName(java.lang.String, XWikiContext)
     */
    public String getItemDocumentDefaultName(String itemName, XWikiContext context)
    {
        String name = context.getWiki().clearName(itemName, true, true, context);

        return getClassPrefix() + name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#getItemDocumentDefaultFullName(java.lang.String, XWikiContext)
     */
    public String getItemDocumentDefaultFullName(String itemName, XWikiContext context)
    {
        return getClassSpacePrefix() + XObjectDocument.SPACE_DOC_SEPARATOR
            + getItemDocumentDefaultName(itemName, context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.XClassManager#getItemDefaultName(java.lang.String)
     */
    public String getItemDefaultName(String docFullName)
    {
        return docFullName.substring(
            (getClassSpacePrefix() + XObjectDocument.SPACE_DOC_SEPARATOR + getClassPrefix()).length()).toLowerCase();
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.XClassManager#getXObjectDocument(java.lang.String,
     *      int, boolean, com.xpn.xwiki.XWikiContext)
     */
    public T getXObjectDocument(String itemName, int objectId, boolean validate, XWikiContext context)
        throws XWikiException
    {
        XWikiDocument doc = context.getWiki().getDocument(getItemDocumentDefaultFullName(itemName, context), context);

        if (doc.isNew() || !isInstance(doc)) {
            throw new XObjectDocumentDoesNotExistException(itemName + " object does not exist");
        }

        return newXObjectDocument(doc, objectId, context);
    }

    /**
     * Construct HQL where clause to use with {@link com.xpn.xwiki.store.XWikiStoreInterface} "searchDocuments" methods.
     * 
     * @param fieldDescriptors the list of fields name/value constraints. Format : [[fieldName1, typeField1,
     *            valueField1][fieldName2, typeField2, valueField2]].
     * @param parameterValues the where clause values that replace the question marks (?).
     * @return a HQL where clause.
     */
    public String createWhereClause(Object[][] fieldDescriptors, List<Object> parameterValues)
    {
        StringBuffer from = new StringBuffer(", BaseObject as obj");

        StringBuffer where = new StringBuffer(" where doc.fullName=obj.name and obj.className=" + HQL_PARAMETER_STRING);
        parameterValues.add(getClassFullName());

        where.append(" and obj.name<>" + HQL_PARAMETER_STRING);
        parameterValues.add(getClassTemplateFullName());

        String andSymbol = " and ";

        if (fieldDescriptors != null) {
            for (int i = 0; i < fieldDescriptors.length; ++i) {
                String fieldName = (String) fieldDescriptors[i][0];
                String type = (String) fieldDescriptors[i][1];
                Object value = fieldDescriptors[i][2];

                if (type != null) {
                    String fieldPrefix = "field" + i;

                    from.append(", " + type + " as " + fieldPrefix);

                    where.append(andSymbol + "obj.id=" + fieldPrefix + ".id.id");

                    where.append(andSymbol + fieldPrefix + ".name=" + HQL_PARAMETER_STRING);
                    parameterValues.add(fieldName);

                    if (value instanceof String) {
                        where.append(andSymbol + "lower(" + fieldPrefix + ".value)=" + HQL_PARAMETER_STRING);
                        parameterValues.add(((String) value).toLowerCase());
                    } else {
                        where.append(andSymbol + "" + fieldPrefix + ".value=" + HQL_PARAMETER_STRING);
                        parameterValues.add(value);
                    }
                } else {
                    if (value instanceof String) {
                        where.append(" and lower(doc." + fieldName + ")=" + HQL_PARAMETER_STRING);
                        parameterValues.add(((String) value).toLowerCase());
                    } else {
                        where.append(" and doc." + fieldName + "=" + HQL_PARAMETER_STRING);
                        parameterValues.add(value);
                    }
                }
            }
        }

        return from.append(where).toString();
    }

    /**
     * Find all XWikiDocument containing object of this XWiki class.
     * 
     * @param context the XWiki context.
     * @return the list of found {@link XObjectDocument}.
     * @throws XWikiException error when searching for document in database.
     * @see #getClassFullName()
     */
    public List<T> searchXObjectDocuments(XWikiContext context) throws XWikiException
    {
        return searchXObjectDocumentsByFields(null, context);
    }

    /**
     * Search in instances of this document class.
     * 
     * @param fieldName the name of field.
     * @param fieldValue the value of field.
     * @param fieldType the type of field.
     * @param context the XWiki context.
     * @return the list of found {@link T}.
     * @throws XWikiException error when searching for documents from in database.
     */
    public List<T> searchXObjectDocumentsByField(String fieldName, Object fieldValue, String fieldType,
        XWikiContext context) throws XWikiException
    {
        Object[][] fieldDescriptors = new Object[][] {{fieldName, fieldType, fieldValue}};

        return searchXObjectDocumentsByFields(fieldDescriptors, context);
    }

    /**
     * Search in instances of this document class.
     * 
     * @param fieldDescriptors the list of fields name/value constraints. Format : [[fieldName1, typeField1,
     *            valueField1][fieldName2, typeField2, valueField2]].
     * @param context the XWiki context.
     * @return the list of found {@link XObjectDocument}.
     * @throws XWikiException error when searching for documents from in database.
     */
    public List<T> searchXObjectDocumentsByFields(Object[][] fieldDescriptors, XWikiContext context)
        throws XWikiException
    {
        check(context);

        List<Object> parameterValues = new ArrayList<Object>();
        String where = createWhereClause(fieldDescriptors, parameterValues);

        return newXObjectDocumentList(context.getWiki().getStore().searchDocuments(where, parameterValues, context),
            context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.XClassManager#newXObjectDocument(com.xpn.xwiki.doc.XWikiDocument,
     *      int, com.xpn.xwiki.XWikiContext)
     */
    public T newXObjectDocument(XWikiDocument doc, int objId, XWikiContext context) throws XWikiException
    {
        return (T) new DefaultXObjectDocument(this, doc, objId, context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.XClassManager#newXObjectDocument(java.lang.String,
     *      int, com.xpn.xwiki.XWikiContext)
     */
    public T newXObjectDocument(String docFullName, int objId, XWikiContext context) throws XWikiException
    {
        return newXObjectDocument(context.getWiki().getDocument(docFullName, context), objId, context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see XClassManager#newXObjectDocument(com.xpn.xwiki.XWikiContext)
     */
    public T newXObjectDocument(XWikiContext context) throws XWikiException
    {
        return newXObjectDocument(new XWikiDocument(), 0, context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.XClassManager#newXObjectDocumentList(com.xpn.xwiki.doc.XWikiDocument,
     *      com.xpn.xwiki.XWikiContext)
     */
    public List<T> newXObjectDocumentList(XWikiDocument document, XWikiContext context) throws XWikiException
    {
        List<XWikiDocument> documents = new ArrayList<XWikiDocument>(1);
        documents.add(document);

        return newXObjectDocumentList(documents, context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.XClassManager#newXObjectDocumentList(java.util.List,
     *      com.xpn.xwiki.XWikiContext)
     */
    public List<T> newXObjectDocumentList(List<XWikiDocument> documents, XWikiContext context) throws XWikiException
    {
        List<T> list = new ArrayList<T>(documents.size());

        for (XWikiDocument doc : documents) {
            List<BaseObject> objects = doc.getObjects(getClassFullName());

            for (BaseObject bobject : objects) {
                if (bobject != null) {
                    list.add(newXObjectDocument(doc, bobject.getNumber(), context));
                }
            }
        }

        return list;
    }
}
