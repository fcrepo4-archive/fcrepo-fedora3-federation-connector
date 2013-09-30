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

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * @Author Michael Durbin
 */
public interface FedoraDatastreamVersionRecord {

    /**
     * Gets the ID of the datastream version described by this record.
     */
    public String getVersionId();

    /**
     * Gets the label of the datastream version described by this record.
     * Since this value wasn't required metadata in Fedora 3, this method
     * may return null.
     */
    public String getLabel();

    /**
     * Gets the creation date for the datastream version described by this
     * record.  Since this a version of the datastream, this should really be
     * considered the modification date when present on the latest version.
     */
    public Date getCreatedDate();

    /**
     * Gets the MIME type of the datastream version described by this record.
     */
    public String getMimeType();

    /**
     * Gets the format URI of the datastream version described by this record.
     * Since this value wasn't required metadata in Fedora 3, this method may
     * return null.
     */
    public String getFormatURI();

    /**
     * Gets the alternate IDs of the datastream version described by this
     * record.  Since this value wasn't required metadata in Fedora 3, this
     * method may return null.
     */
    public List<String> getAltIDs();

    /**
     * Gets the content digest type of the datastream version described by this
     * record.  This may be null if checksumming is disabled.
     */
    public String getContentDigestType();

    /**
     * Gets the content digest of the datastream version described by this
     * record.  This may be null since this is not a required metadata field
     * in Fedora 3.
     */
    public String getContentDigest();

    /**
     * Gets the length in bytes of the content of the datastream version
     * described by this record.
     */
    public long getContentLength();

    /**
     * Gets a new InputStream to access the content of the datastream version
     * described by this record.
     */
    public InputStream getStream() throws Exception;

    /**
     * Gets (or computes) a SHA-1 hash of the content of the datastream.
     */
    public byte[] getSha1() throws Exception;

}
