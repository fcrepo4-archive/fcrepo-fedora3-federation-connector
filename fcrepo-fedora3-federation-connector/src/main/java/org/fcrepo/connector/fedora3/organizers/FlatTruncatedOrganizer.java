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

package org.fcrepo.connector.fedora3.organizers;

import org.fcrepo.connector.fedora3.Fedora3DataInterface;
import org.fcrepo.connector.fedora3.ID;
import org.fcrepo.connector.fedora3.RepositoryOrganizer;

import java.util.List;

/**
 * This is a dummy implementation that does exactly what the hard-coded
 * organization from previous versions of Fedora3FederationConnector did.
 * It's useful only a step on the way towards a more useful implementation.
 *
 * @author Michael Durbin
 */
public class FlatTruncatedOrganizer implements RepositoryOrganizer {

    private Fedora3DataInterface f3;

    /**
     * A constructor that accepts the Fedora3DataInterface whose fedora objects
     * will be organized by this instance.
     */
    public FlatTruncatedOrganizer(Fedora3DataInterface fedora3Data) {
        f3 = fedora3Data;
    }

    /**
     * Returns the first 10 objects in the repository when invoked for the
     * root ID; otherwise throws an IllegalArgumentException.
     */
    public List<String> getChildrenForId(String id) {
        if (id.equals(ID.ROOT_ID.getId())) {
            return f3.getObjectPids(0, 10);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns null if the id isn't the root ID, and otherwise the root id.
     */
    public String getParentForId(String id) {
        if (ID.ROOT_ID.getId().equals(id)) {
            return null;
        } else {
            return ID.ROOT_ID.getId();
        }
    }

    /**
     * Returns true for the root id, false otherwise.
     */
    public boolean isOrganizationalNode(String id) {
        return ID.ROOT_ID.getId().equals(id);
    }
}
