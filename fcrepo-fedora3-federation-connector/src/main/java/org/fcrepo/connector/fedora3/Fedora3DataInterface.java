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

/**
 * An base class that encapsulates the logic to access content from a fedora 3
 * repository.  This abstract class, while exposing the methods neccessary to
 * implement a federation over fedora 3 content is agnostic about the way data
 * is retrieved from said repository
 * 
 * @author Michael Durbin
 */
public interface Fedora3DataInterface {

    /**
     * Gets a FedoraObjectRecord that encapsulates a summary of the object with
     * the given pid in the fedora 3 repository exposed through this interface.
     */
    public FedoraObjectRecord getObjectByPid(String pid);

    /**
     * Determines if an object with the given pid exists in the fedora 3
     * repository exposed through this interface.
     */
    public boolean doesObjectExist(String pid);

    /**
     * Gets a page of object pids that exist in the repository.
     */
    public String[] getObjectPids(int offset, int pageSize);

    /**
     * Gets information about a given datastream for a given pid.
     */
    public FedoraDatastreamRecord getDatastream(String pid,
            String dsid);

    /**
     * Determines if an object with the given pid exists and has a datastream
     * with the given dsid.
     */
    public boolean doesDatastreamExist(String pid, String dsid);
}
