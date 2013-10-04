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

import java.util.List;

/**
 * An interface that defines methods to expose an arbitrary organization of
 * objects (from a fedora 3 repository) in a federation.
 *
 * @author Michael Durbin
 */
public interface RepositoryOrganizer {

    /**
     * Gets the id values of all children nodes for the node with the given id.
     * @throws IllegalArgumentException if the id is not an organizational node
     */
    public List<String> getChildrenForId(String id);

    /**
     * Gets the id of the parent of the node with the given id.
     * @throws IllegalArgumentException if the id has never been returned by a
     * call to getChildrenForId.
     */
    public String getParentForId(String id);

    /**
     * Determine whether a given node (identified by id) is an organizational
     * node fabricated by this RepositoryOrganizer implementation.
     * @throws IllegalArgumentException if the id has never been returned by a
     * call to getChildrenForId.
     */
    public boolean isOrganizationalNode(String id);

}
