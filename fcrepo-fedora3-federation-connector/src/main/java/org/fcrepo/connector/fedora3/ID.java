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

import org.modeshape.common.text.Jsr283Encoder;
import org.modeshape.jcr.api.JcrConstants;

/**
 * Encapsualtes the logic associated with mapping ids within the
 * {@link Fedora3FederationConnector} to the types of objects each represents.
 * 
 * Objects within the federation can fall into 4 types:
 * <ul>
 *   <li>root - the ID for the federation node itself</li>
 *   <li>
 *     object - the ID of a node representing an object in the fedora 3
 *     repository that is being federated over
 *   </li>
 *   <li>
 *     datastream - the ID of a node representing a datastream from an object
 *     in the fedora 3 repository that is being federated over
 *   </li>
 *   <li>content - the ID of content from a datastream</li>
 * </ul>
 * <p>
 *   The IDs are meant to be opaque and the implementation may change in later
 *   versions of this class, but for reference, the current implementation
 *   creates ids in the following pattern. /, /pid, /pid/dsid, /pid/dsid/content
 * </p>
 * 
 * @author Michael Durbin
 */
public class ID {

    private String id;

    /**
     * A constructor that accepts a known id.
     */
    public ID(String id) {
        this.id = id;
    }

    /**
     * Gets a name for the node with this id.
     */
    public String getName() {
        if (isRootID()) {
            return "/";
        } else if (isContentID()) {
            return JcrConstants.JCR_CONTENT;
        } else {
            return id.substring(id.lastIndexOf('/') + 1);
        }
    }

    /**
     * Gets the ID within the federation.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the id for the parent within the federation of the node with
     * this id.
     */
    public String getParentId() {
        if (isRootID()) {
            return null;
        } else if (isObjectID()) {
            return ROOT_ID.id;
        } else if (isDatastreamID()) {
            return objectID(getPid()).id;
        } else {
            assert(isContentID());
            return datastreamID(getPid(), getDSID()).id;
        }
    }

    /**
     * Determines if the id is the root id.
     */
    public boolean isRootID() {
        return "/".equals(id);
    }

    /**
     * Determines if the id is for a fedora 3 object.
     */
    public boolean isObjectID() {
        return id.split("/").length == 1;
    }

    /**
     * Gets the PID associated with this id.  For "object" IDs this will be the
     * PID of the object, for "datastream" IDs this will be the pid to which
     * the datastream belongs and for "content" IDs this will be the PID of the
     * object that contains the datastream whose content node has the given id.
     * @return the pid, if one can be determined from the id.  This should
     * return a non-null value unless this id is the root id.
     */
    public String getPid() {
        if (isRootID()) {
            return null;
        } else {
            String[] path = id.split("/");
            return new Jsr283Encoder().decode(path[0]);
        }
    }

    /**
     * Gets the DSID associated with this id or null if the object represented
     * by the node with this id does not pertain to a datastream or its
     * content.
     */
    public String getDSID() {
        String[] path = id.split("/");
        if (path.length < 2) {
            return null;
        } else {
            return new Jsr283Encoder().decode(path[1]);
        }
    }

    /**
     * Determines if the id is for a fedora 3 datastream.
     */
    public boolean isDatastreamID() {
        return id.split("/").length == 2;
    }

    /**
     * Determines if the id is for a fedora 3 datastream content node.
     */
    public boolean isContentID() {
        return id.split("/").length == 3;
    }

    /**
     * The ID for the root of the federation.
     */
    public static final ID ROOT_ID = new ID("/");

    /**
     * Gets the ID for the node within the federation representing an object
     * with the given pid.
     */
    public static ID objectID(String pid) {
        return new ID(new Jsr283Encoder().encode(pid));
    }

    /**
     * Gets the ID for the node within the federation representing a datastream
     * with the given dsid on the object in the federation having the given
     * pid.
     */
    public static ID datastreamID(String pid, String datastream) {
        return new ID(new Jsr283Encoder().encode(pid) + "/"
                + new Jsr283Encoder().encode(datastream));
    }

    /**
     * Gets the ID for the node within the federation representing datastream
     * content with the given dsid on the object in the federation having the
     * given pid.
     */
    public static ID contentID(String pid, String datastream) {
        return new ID(new Jsr283Encoder().encode(pid) + "/"
                + new Jsr283Encoder().encode(datastream) + "/"
                + JcrConstants.JCR_CONTENT);
    }
}
