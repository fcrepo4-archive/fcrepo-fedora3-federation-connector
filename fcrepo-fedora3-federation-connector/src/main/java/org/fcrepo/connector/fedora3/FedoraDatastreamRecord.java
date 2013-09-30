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
 * An interface to expose enough information about a Fedora 3 datastream to
 * import it into fedora 4.  All metadata is reprsented here, even those fields
 * that are meaningless in the Fedora 4 architecture.
 * 
 * @author Michael Durbin
 */
public interface FedoraDatastreamRecord {

    /**
     * Gets the pid of the object whose datastream is described by this record.
     */
    public String getPid();

    /**
     * Gets the DSID for the datastream described by this record.
     */
    public String getId();

    /**
     * Get the control group for the datastream described by this record.  This
     * value, while significant in Fedora 3, is only present as an indicator of
     * historic status, since storage management is handled through another
     * mechanism in Fedora 4.
     */
    public String getControlGroup();

    /**
     * Gets the state for the datastream described by this record.
     */
    public String getState();

    /**
     * Gets the 'versionable' value for the datastream described by this
     * record.  This value does not imply anything about whether or how many
     * versions of this datastream exists (there is always at least one) but
     * instead was an indicator of whether an update to the described
     * datastream in Fedora 3 would overwrite the current version or create a
     * new one.  In the context of fedora 4, this value is just to preserve
     * the historical record.
     */
    public boolean getVersionable();

    /**
     * Returns a non-null, non-empty list of FedoraDatastreamVersionRecord
     * objects representing the ordered history of this datastream from most
     * recent to oldest.
     */
    public List<FedoraDatastreamVersionRecord> getHistory();

    /**
     * Gets the most recent version of the datastream.  This method is just a
     * shortcut for getHistory().get(0).
     */
    public FedoraDatastreamVersionRecord getCurrentVersion();

}
