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

package org.fcrepo.connector.fedora3.rest;

import org.fcrepo.connector.fedora3.FedoraDatastreamRecord;

import com.yourmediashelf.fedora.generated.management.DatastreamProfile;

public class RESTFedoraDatastreamRecord
        implements FedoraDatastreamRecord {

    private DatastreamProfile ds;

    /**
     * A constructor that takes as a parameter the DatastreamType object that
     * was returned by the FedoraClient as its source for information about the
     * fedora 3 datastream to be described by this object.
     */
    public RESTFedoraDatastreamRecord(DatastreamProfile ds) {
        this.ds = ds;
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return ds.getDsID();
    }

    /**
     * {@inheritDoc}
     */
    public String getMimeType() {
        return ds.getDsMIME();
    }

}
