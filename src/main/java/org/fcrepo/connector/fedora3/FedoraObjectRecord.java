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

import java.util.Date;
import java.util.List;

/**
 * An interface that exposes information about an object in fedora 3.
 * Implementations may vary about how they determine this information, but
 * all methods are expected to be implemented such that they return meaningful
 * values.
 * 
 * @author Michael Durbin
 */
public interface FedoraObjectRecord {

    /**
     * Gets the pid for the object described by this record.
     */
    public String getPid();

    /**
     * Gets the modification date for the object described by this record.
     */
    public Date getModificationDate();

    /**
     * Gets the creation date for the object described by this record.
     */
    public Date getCreatedDate();

    /**
     * Gets a list ids (fedora DSID) for the datastreams that exist on the
     * object described by this record.
     */
    public List<String> listDatastreamIds();

}
