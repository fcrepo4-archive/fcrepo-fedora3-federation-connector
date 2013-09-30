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

/**
 * An abstract base class for the FedoraObjectRecord with convenient member
 * variables and accessor methods for all of the interface methods.  Because
 * this object is expected to be immutable, subclasses need only implement a
 * constructor.
 * 
 * @author Michael Durbin
 */
public abstract class AbstractFedoraObjectRecord implements FedoraObjectRecord {

    private String pid;

    protected String state;

    protected String label;

    protected List<String> ownerIds;

    private Date createdDate;

    private Date lastModDate;

    protected List<String> datastreams;

    /**
     * A constructor that accepts the three required properties of a fedora 3
     * object.
     */
    protected AbstractFedoraObjectRecord(String pid, Date createdDate,
                                         Date lastModifiedDate) {
        this.pid = pid;
        this.createdDate = createdDate;
        this.lastModDate = lastModifiedDate;
    }

    /**
     * {@inheritDoc}
     */
    public String getPid() {
        return pid;
    }

    /**
     * {@inheritDoc}
     */
    public String getState() {
        return state;
    }

    /**
     * {@inheritDoc}
     */
    public String getLabel() {
        return label;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getOwnerIds() {
        return ownerIds;
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
