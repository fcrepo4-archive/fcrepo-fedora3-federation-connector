/**
 * Copyright 2013 DuraSpace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fcrepo.connector.fedora3;

import org.fcrepo.connector.fedora3.organizers.FlatTruncatedOrganizer;
import org.fcrepo.connector.fedora3.rest.RESTFedora3DataImpl;
import org.fcrepo.jcr.FedoraJcrTypes;
import org.fcrepo.kernel.utils.ContentDigest;
import org.infinispan.schematic.document.Document;
import org.modeshape.jcr.api.JcrConstants;
import org.modeshape.jcr.api.nodetype.NodeTypeManager;
import org.modeshape.jcr.federation.spi.DocumentWriter;
import org.modeshape.jcr.federation.spi.ReadOnlyConnector;
import org.modeshape.jcr.value.BinaryKey;
import org.modeshape.jcr.value.BinaryValue;
import org.modeshape.jcr.value.DateTimeFactory;
import org.modeshape.jcr.value.binary.ExternalBinaryValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * A ReadOnly connector to a fedora 3 repository.
 * 
 * @author Michael Durbin
 */
public class Fedora3FederationConnector extends ReadOnlyConnector
         implements FedoraJcrTypes {

    private static final Logger LOGGER
        = LoggerFactory.getLogger(Fedora3FederationConnector.class);

    private static final String NT_F3_REPOSITORY = "f3:repository";
    private static final String NT_F3_OBJECT = "f3:object";
    private static final String NT_F3_DATASTREAM = "f3:datastream";
    private static final String NT_F3_GROUP = "f3:group";

    private static final String F3_PID = "f3:pid";
    private static final String F3_OBJ_STATE = "f3:objState";
    private static final String F3_OBJ_LABEL = "f3:objlabel";
    private static final String F3_OBJ_OWNER_ID = "f3:objOwnerId";
    private static final String F3_OBJ_CREATED_DATE = "f3:objCreatedDate";
    private static final String F3_OBJ_LAST_MODIFIED_DATE
        = "f3:objLastModifiedDate";

    private static final String F3_DSID = "f3:dsid";
    private static final String F3_DS_CONTROL_GROUP = "f3:dsCongtrolGroup";
    private static final String F3_DS_STATE = "f3:dsState";
    private static final String F3_DS_VERSIONABLE = "f3:versionable";
    private static final String F3_DS_VERSION_ID = "f3:dsVersionId";
    private static final String F3_DS_LABEL = "f3:dsLabel";
    private static final String F3_DS_CREATED = "f3:dsCreated";
    private static final String F3_DS_MIME_TYPE = "f3:dsMimeType";
    private static final String F3_DS_FORMAT_URI = "f3:dsFormatURI";
    private static final String F3_DS_ALT_IDS = "f3:dsAltIds";
    private static final String F3_DS_SIZE = "f3:dsSize";
    private static final String F3_DS_CONTENT_DIGEST_TYPE
        = "f3:dsContentDigestType";
    private static final String F3_DS_CONTENT_DIGEST = "f3:dsCongtentDigest";

    protected Fedora3DataInterface f3;

    /**
     * Set by reflection to the value in the ModeShape repository configuration
     * json file, this is the URL for the fedora repository over which
     * this connector federates.
     */
    protected String fedoraUrl;

    /**
     * Set by reflection to the value in the ModeShape repository configuration
     * json file, this is the username to access the fedora 3 repository over
     * which this connector federates.  This user should have access to all
     * content that is meant to be exposed in the federation.
     */

    protected String username;

    /**
     * Set by reflection to the value in the ModeShape repository configuration
     * json file, this is the password to access the fedora 3 repository over
     * which this connector federates.
     */
    protected String password;

    protected RepositoryOrganizer organizer;

    /**
     * {@inheritDoc}
     */
    public void initialize(NamespaceRegistry registry,
            NodeTypeManager nodeTypeManager)
        throws RepositoryException, IOException {
        super.initialize(registry, nodeTypeManager);

        LOGGER.trace("Initializing");

        if (nodeTypeManager != null) {  // only null for unit tests
            nodeTypeManager.registerNodeTypes(
                    getClass().getClassLoader()
                            .getResourceAsStream("fedora3-node-types.cnd"),
                    true);
            LOGGER.debug("Loaded node types from {}.",
                    getClass().getClassLoader()
                            .getResource("fedora3-node-types.cnd"));
        }

        try {
            if (fedoraUrl != null && username != null && password != null) {
                f3 = new RESTFedora3DataImpl(fedoraUrl, username,
                        password);
            } else {
                throw new RepositoryException("Requred parameters missing, "
                        + "ensure that \"fedoraUrl\", \"username\" and "
                        + " \"password\" are set!");
            }
        } catch (Throwable t) {
            throw new RepositoryException("Error starting fedora connector!",
                    t);
        }
        organizer = new FlatTruncatedOrganizer(f3);
        LOGGER.trace("Initialized");
    }

    /**
     * {@inheritDoc}
     */
    public Document getDocumentById(String idStr) {
        LOGGER.trace("getDocumentById {}", idStr);
        ID id = new ID(idStr);
        DocumentWriter writer = newDocument(idStr);
        writer.setNotQueryable();
        if (organizer.isOrganizationalNode(idStr)) {
            writer.setPrimaryType(JcrConstants.NT_FOLDER);
            writer.addMixinType(NT_F3_GROUP);
            for (String childId : organizer.getChildrenForId(idStr)) {
                if (organizer.isOrganizationalNode(childId)) {
                    writer.addChild(childId, childId);
                } else {
                    writer.addChild(ID.objectID(childId).getId(),
                            ID.objectID(childId).getName());
                }
            }
            return writer.document();
        } else if (id.isRootID()) {
            // return a root object
            writer.setPrimaryType(JcrConstants.NT_FOLDER);
            writer.addMixinType(NT_F3_REPOSITORY);
            addRepositoryChildren(writer, idStr);
            return writer.document();
        } else if (id.isObjectID()) {
            // return an object node
            FedoraObjectRecord o = f3.getObjectByPid(id.getPid());
            writer.setPrimaryType(JcrConstants.NT_FOLDER);
            writer.setParent(ID.ROOT_ID.getId());
            addObjectProperties(writer, o);
            addObjectChildren(writer, o);
            return writer.document();
        } else if (id.isDatastreamID()) {
            // return a datastream node
            writer.setPrimaryType(JcrConstants.NT_FILE);
            writer.setParent(id.getParentId());
            FedoraDatastreamRecord ds
                = f3.getDatastream(id.getPid(), id.getDSID());
            addDatastreamProperties(writer, ds);
            ID contentId = ID.contentID(id.getPid(), id.getDSID());
            writer.addChild(contentId.getId(), contentId.getName());
            return writer.document();
        } else if (id.isContentID()) {
            // return a content node
            FedoraDatastreamRecord ds = f3.getDatastream(id.getPid(),
                    id.getDSID());
            writer.setPrimaryType(JcrConstants.NT_RESOURCE);
            writer.setParent(id.getParentId());
            addDatastreamContentProperties(writer, ds);
            return writer.document();
        } else {
            return null;
        }
    }

    private void addRepositoryChildren(DocumentWriter writer, String idStr) {
        ID id = new ID(idStr);
        List<String> childPids = organizer.getChildrenForId(idStr);
        for (String childPid : childPids) {
            ID childId = ID.objectID(childPid);
            LOGGER.trace("Added child " + childId.getId());
            writer.addChild(childId.getId(), childId.getName());
        }
    }

    /**
     * Adds the Fedora 3 object mixin type and Fedora 3 properties for the
     * given obejct.  This method also adds any JCR base or Fedora 4 properties
     * to which the existing Fedora 3 properties map.
     *
     * This method doesn't expose versioning from fedora 3 and instead only
     * includes information from the latest version.
     */
    private void addObjectProperties(DocumentWriter writer,
            FedoraObjectRecord o) {
        // Fedora 3 Properties
        writer.addMixinType(NT_F3_OBJECT);
        writer.addProperty(F3_PID, o.getPid());
        addOptionalProperty(writer, F3_OBJ_STATE, o.getState());
        addOptionalProperty(writer, F3_OBJ_LABEL, o.getLabel());
        addOptionalProperty(writer, F3_OBJ_OWNER_ID, o.getOwnerIds());
        addOptionalProperty(writer, F3_OBJ_CREATED_DATE, o.getCreatedDate());
        addOptionalProperty(writer, F3_OBJ_LAST_MODIFIED_DATE,
                o.getModificationDate());

        // JCR Properties
        addOptionalProperty(writer, JCR_CREATED, o.getCreatedDate());
        addOptionalProperty(writer, JCR_LASTMODIFIED, o.getModificationDate());
    }

    /**
     * Adds the Fedora 3 datastream mixin type and Fedora 3 datastream
     * properties for the given datastream.  This method also adds the fedora 4
     * "datastream" mixin and relevant properties.
     *
     * This method does not include version information and only exposes the
     * latest version of a datastream.
     */
    private void addDatastreamProperties(DocumentWriter writer,
            FedoraDatastreamRecord ds) {
        // Fedora 3 Datastream Properties
        writer.addMixinType(NT_F3_DATASTREAM);
        writer.addProperty(F3_DSID, ds.getId());
        writer.addProperty(F3_DS_CONTROL_GROUP, ds.getControlGroup());
        writer.addProperty(F3_DS_STATE, ds.getState());
        writer.addProperty(F3_DS_VERSIONABLE, ds.getVersionable());
        FedoraDatastreamVersionRecord dsVer = ds.getCurrentVersion();
        writer.addProperty(F3_DS_VERSION_ID, dsVer.getVersionId());
        addOptionalProperty(writer, F3_DS_CREATED, dsVer.getCreatedDate());
        addOptionalProperty(writer, F3_DS_LABEL, dsVer.getLabel());
        writer.addProperty(F3_DS_MIME_TYPE, dsVer.getMimeType());
        addOptionalProperty(writer, F3_DS_FORMAT_URI, dsVer.getFormatURI());
        addOptionalProperty(writer, F3_DS_ALT_IDS, dsVer.getAltIDs());
        writer.addProperty(F3_DS_SIZE, dsVer.getContentLength());
        addOptionalProperty(writer, F3_DS_CONTENT_DIGEST_TYPE,
                dsVer.getContentDigestType());
        addOptionalProperty(writer, F3_DS_CONTENT_DIGEST,
                dsVer.getContentDigest());

        // Fedora 4 Datastream Properties
        writer.addMixinType(FEDORA_DATASTREAM);
        if (getContext() != null) { //only null for unit tests
            DateTimeFactory f = factories().getDateFactory();
            writer.addProperty(JCR_LASTMODIFIED,
                    f.create(dsVer.getCreatedDate()));
            FedoraDatastreamVersionRecord firstVer
                = ds.getHistory().get(ds.getHistory().size() - 1);
            writer.addProperty(JCR_CREATED,
                        f.create(firstVer.getCreatedDate()));
        }
    }

    private void addDatastreamContentProperties(DocumentWriter writer,
            FedoraDatastreamRecord ds) {
        writer.addMixinType(FEDORA_BINARY);
        try {
            BinaryValue binary = new Fedora3DatastreamBinaryValue(ds);
            writer.addProperty(JcrConstants.JCR_DATA, binary);
            LOGGER.trace("{} size: {}", ds.getId(), binary.getSize());
            LOGGER.trace("{} hash: {}", ds.getId(), binary.getHexHash());
            writer.addProperty(CONTENT_DIGEST, ContentDigest.
                    asURI("SHA-1", binary.getHexHash()));
            writer.addProperty(CONTENT_SIZE, binary.getSize());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        writer.addProperty(JcrConstants.JCR_MIME_TYPE,
                ds.getCurrentVersion().getMimeType());

    }

    /**
     * A helper method that adds a property (or list of properties) if the
     * value isn't null.  This method also conveniently converts Date values to
     * DateTime values.
     */
    private void addOptionalProperty(DocumentWriter writer, String name,
            Object value) {
        if (value != null) {
            if (value instanceof List) {
                writer.addProperty(name, ((List) value).toArray());
            } else if (value instanceof Date) {
                if (getContext() != null) { //only null for unit tests
                    DateTimeFactory f = factories().getDateFactory();
                    writer.addProperty(name, f.create((Date) value));
                }
            } else {
                writer.addProperty(name, value);
            }
        }
    }


    private void addObjectChildren(DocumentWriter writer,
            FedoraObjectRecord object) {
        for (String dsidStr : object.listDatastreamIds()) {
            ID dsid = ID.datastreamID(object.getPid(), dsidStr);
            writer.addChild(dsid.getId(), dsid.getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getDocumentId(String externalPath) {
        LOGGER.info("getDocumentId {}", externalPath);
        return getIdFromPath(externalPath);
    }

    /**
     * The root path is always '/', organizational node paths are made up up
     * any number of organizational node ids concatenated together, while all
     * other nodes are made up of any number of organizational node paths
     * concatenated together followed by an object, datastream or content ID.
     *
     * This method uses that to determine the id of the node at the given path.
     */
    protected String getIdFromPath(String path) {
        int nextBreak = path.indexOf('/', 1);
        if (nextBreak == -1) {
            return path;
        } else {
            String nextChunk = path.substring(0, nextBreak);
            if (organizer.isOrganizationalNode(nextChunk)) {
                return getIdFromPath(path.substring(nextBreak));
            } else {
                return path;
            }
        }
    }

    @Override
    public Collection<String> getDocumentPathsById(String id) {
        LOGGER.info("getDocumentPathsById {}", id);
        return Collections.singletonList(buildPath(id, id));
    }

    /**
     * The root path is always '/', organizational node paths are made up up
     * any number of organizational node ids concatenated together, while all
     * other nodes are made up of any number of organizational node paths
     * concatenated together followed by an object, datastream or content ID.
     *
     * This method uses that to build a path from an ID.
     */
    protected String buildPath(String path, String currentId) {
        String parentId = organizer.getParentForId(currentId);
        if (parentId == null) {
            return path;
        } else {
            return buildPath(parentId + path, parentId);
        }
    }



    /**
     * Checks if a document with the given id exists.
     * @param idStr a {@code non-null} string representing the identifier
     *              within the system whose existence is being queried in this
     *              federation.
     */
    public boolean hasDocument(String idStr) {
        LOGGER.info("hasDocument {}", idStr);
        if (organizer.isOrganizationalNode(idStr)) {
            return true;
        }
        ID id = new ID(idStr);
        return (id.isRootID()
                || (id.isObjectID() && f3.doesObjectExist(id.getPid())
                || ((id.isDatastreamID() || id.isContentID())
                && f3.doesDatastreamExist(id.getPid(), id.getDSID()))));
    }

    /**
     * Gets an ExternalBinaryValue that exposes access to content nodes in
     * the federation.
     */
    public ExternalBinaryValue getBinaryValue(String idStr) {
        ID id = new ID(idStr);
        FedoraDatastreamRecord ds = f3.getDatastream(id.getPid(),
                id.getDSID());
        try {
            return new Fedora3DatastreamBinaryValue(ds);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public class Fedora3DatastreamBinaryValue extends ExternalBinaryValue {

        private static final long serialVersionUID = 1L;

        private FedoraDatastreamRecord ds;

        Fedora3DatastreamBinaryValue(FedoraDatastreamRecord ds)
            throws Exception {
            super(new BinaryKey(ds.getCurrentVersion().getSha1()),
                    Fedora3FederationConnector.this.getSourceName(),
                    ID.contentID(ds.getPid(), ds.getId()).getId(),
                    ds.getCurrentVersion().getContentLength(), null, null);
            this.ds = ds;
        }

        /**
         * Gets the InputStream for the content.
         */
        public InputStream getStream() throws RepositoryException {
            try {
                return ds.getCurrentVersion().getStream();
            } catch (Exception e) {
                throw new RepositoryException(e);
            }
        }

        /**
         * Overrides the superclass to return the MIME type declared on the
         * fedora 3 datastream whose content is exposed by this BinaryValue.
         */
        public String getMimeType() {
            return ds.getCurrentVersion().getMimeType();
        }
    }
}
