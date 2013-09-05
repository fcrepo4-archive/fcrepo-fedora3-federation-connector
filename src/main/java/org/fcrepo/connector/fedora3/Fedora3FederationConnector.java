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


import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;

import org.fcrepo.connector.fedora3.rest.RESTFedora3DataImpl;
import org.fcrepo.jcr.FedoraJcrTypes;
import org.fcrepo.kernel.utils.ContentDigest;
import org.infinispan.schematic.document.Document;
import org.modeshape.jcr.api.JcrConstants;
import org.modeshape.jcr.api.nodetype.NodeTypeManager;
import org.modeshape.jcr.federation.spi.DocumentWriter;
import org.modeshape.jcr.federation.spi.PageKey;
import org.modeshape.jcr.federation.spi.Pageable;
import org.modeshape.jcr.federation.spi.ReadOnlyConnector;
import org.modeshape.jcr.value.BinaryValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ReadOnly connector to a fedora 3 repository.
 * 
 * @author Michael Durbin
 */
public class Fedora3FederationConnector extends ReadOnlyConnector
         implements Pageable, FedoraJcrTypes {

    private static final Logger LOGGER
        = LoggerFactory.getLogger(Fedora3FederationConnector.class);

    public static final String NT_F3_REPOSITORY = "fedora:repository";

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

    /**
     * Set by reflection to the value in the ModeShape repository configuration
     * json file, this member variables controls the size of the pages
     * requested from the fedora 3 instance when listing children.
     */
    protected int pageSize = 20;

    /**
     * {@inheritDoc}
     */
    public void initialize(NamespaceRegistry registry,
            NodeTypeManager nodeTypeManager)
        throws RepositoryException, IOException {
        super.initialize(registry, nodeTypeManager);

        LOGGER.trace("Initializing");
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
        LOGGER.trace("Initialized");
    }

    /**
     * {@inheritDoc}
     */
    public Document getDocumentById(String idStr) {
        LOGGER.trace("getDocumentById {}", idStr);
        ID id = new ID(idStr);
        DocumentWriter writer = newDocument(idStr);
        if (id.isRootID()) {
            // return a root object
            writer.setPrimaryType(JcrConstants.NT_FOLDER);
            writer.addMixinType(NT_F3_REPOSITORY);
            addRepositoryChildren(writer, idStr, 0, pageSize);
            return writer.document();
        } else if (id.isObjectID()) {
            // return an object node
            FedoraObjectRecord o = f3.getObjectByPid(id.getPid());
            writer.setPrimaryType(JcrConstants.NT_FOLDER);
            writer.setParent(ID.ROOT_ID.getId());
            writer.addMixinType(FEDORA_OBJECT);
            if (o.getModificationDate() != null) {
                writer.addProperty(JCR_LASTMODIFIED,
                        factories().getDateFactory().create(
                                o.getModificationDate()));
            }
            if (o.getCreatedDate() != null) {
                writer.addProperty(JCR_CREATED,
                        factories().getDateFactory().create(
                                o.getCreatedDate()));
            }
            addObjectChildren(writer, o);
            return writer.document();
        } else if (id.isDatastreamID()) {
            // return a datastream node
            writer.setPrimaryType(JcrConstants.NT_FILE);
            writer.setParent(id.getParentId());
            writer.addMixinType(FEDORA_DATASTREAM);
            FedoraDatastreamRecord ds
                = f3.getDatastream(id.getPid(), id.getDSID());
            if (ds.getModificationDate() != null) {
                writer.addProperty(JCR_LASTMODIFIED,
                        factories().getDateFactory().create(
                                ds.getModificationDate()));
            }
            if (ds.getCreatedDate() != null) {
                writer.addProperty(JCR_CREATED,
                        factories().getDateFactory().create(
                                ds.getCreatedDate()));
            }
            ID contentId = ID.contentID(id.getPid(), id.getDSID());
            writer.addChild(contentId.getId(), contentId.getName());
            return writer.document();
        } else if (id.isContentID()) {
            // return a content node
            FedoraDatastreamRecord ds = f3.getDatastream(id.getPid(),
                    id.getDSID());
            writer.setPrimaryType(JcrConstants.NT_RESOURCE);
            writer.setParent(id.getParentId());
            writer.addMixinType(FEDORA_BINARY);
            try {
                BinaryValue binary = ds.getContent();
                LOGGER.trace("{} size: {}", ds.getId(), binary.getSize());
                LOGGER.trace("{} hash: {}", ds.getId(), binary.getHexHash());
                writer.addProperty(CONTENT_DIGEST, ContentDigest.
                        asURI("SHA-1", binary.getHexHash()).toString());
                writer.addProperty(CONTENT_SIZE, binary.getSize());
                writer.addProperty(JcrConstants.JCR_DATA, binary);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            writer.addProperty(JcrConstants.JCR_MIME_TYPE, ds.getMimeType());
            return writer.document();
        } else {
            return null;
        }
    }

    private void addRepositoryChildren(DocumentWriter writer, String idStr,
            int offset, int pageSize) {
        ID id = new ID(idStr);
        String[] childPids = f3.getObjectPids(offset, pageSize + 1);
        for (String childPid : childPids) {
            ID childId = ID.objectID(childPid);
            LOGGER.trace("Added child " + childId.getId());
            writer.addChild(childId.getId(), childId.getName());
        }
        // FIXME
        // this is commented out because the UI requests all objects,
        // which for most fedora repositories is too big a set.
        //if (childPids.length <= pageSize + 1) {
        //    writer.addPage(id, offset + pageSize, pageSize,
        //            PageWriter.UNKNOWN_TOTAL_SIZE);
        //}
    }

    private void addObjectChildren(DocumentWriter writer,
            FedoraObjectRecord object) {
        for (String dsidStr : object.listDatastreamIds()) {
            ID dsid = ID.datastreamID(object.getPid(), dsidStr);
            writer.addChild(dsid.getId(), dsid.getName());
        }
    }

    @Override
    public String getDocumentId(String externalPath) {
        LOGGER.info("getDocumentId {}", externalPath);
        return externalPath;
    }

    @Override
    public Collection<String> getDocumentPathsById(String id) {
        LOGGER.info("getDocumentPathsById {}", id);
        return Collections.singletonList(id);
    }

    /**
     * Checks if a document with the given id exists.
     * @param idStr a {@code non-null} string representing the identifier within
     * the system whose existence is being queried in this federation.
     */
    public boolean hasDocument(String idStr) {
        LOGGER.info("hasDocument {}", idStr);
        ID id = new ID(idStr);
        return (id.isRootID()
                || (id.isObjectID() && f3.doesObjectExist(id.getPid())
                || ((id.isDatastreamID() || id.isContentID())
                && f3.doesDatastreamExist(id.getPid(), id.getDSID()))));
    }

    @Override
    public Document getChildren(PageKey pageKey) {
        LOGGER.info("getChildren {}", pageKey);
        ID parentId = new ID(pageKey.getParentId());
        if (parentId.isRootID()) {
            DocumentWriter writer = newDocument(parentId.getId());
            writer.setPrimaryType(NT_F3_REPOSITORY);
            addRepositoryChildren(writer, parentId.getId(),
                    pageKey.getOffsetInt(), (int) pageKey.getBlockSize());
            return writer.document();
        } else {
            // get the datastreams
            throw new UnsupportedOperationException();
        }
    }
}
