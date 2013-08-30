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

import java.util.Date;
import java.util.List;

import org.fcrepo.connector.fedora3.FedoraObjectRecord;

public class DefaultFedoraObjectRecord implements FedoraObjectRecord {

    String pid;

    Date lastModDate;

    Date createdDate;

    List<String> datastreams;

    /**
     * {@inheritDoc}
     */
    public String getPid() {
        return pid;
    }

    /**
     * {@inheritDoc}
     */
    public Date getModificationDate() {
        return lastModDate;
    }

    /**
     * {@inheritDoc}
     */
    public Date getCreatedDate() {
        return createdDate;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> listDatastreamIds() {
        return datastreams;
    }

}
