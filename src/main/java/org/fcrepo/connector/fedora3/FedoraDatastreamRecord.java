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

import org.modeshape.jcr.value.BinaryValue;

/**
 * An interface to expose enough information about a Fedora 3 datastream to
 * import it into fedora 4.
 * 
 * @author Michael Durbin
 */
public interface FedoraDatastreamRecord {

    /**
     * Gets the DSID.
     */
    public String getId();

    /**
     * Gets the MIME type.
     */
    public String getMimeType();

    /**
     * Gets the modification date for the datastream described by this record.
     */
    public Date getModificationDate();

    /**
     * Gets the creation date for the datastream described by this record.
     */
    public Date getCreatedDate();

    /**
     * Gets a JCR BinaryValue representing the content of the datastream.
     */
    public BinaryValue getContent() throws Exception;

    // TODO: expose other properties
}
