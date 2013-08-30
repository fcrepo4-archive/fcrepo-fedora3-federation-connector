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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.InputStream;
import java.util.Date;

import javax.jcr.RepositoryException;

import org.fcrepo.connector.fedora3.FedoraDatastreamRecord;
import org.fcrepo.connector.fedora3.ID;
import org.modeshape.common.util.SecureHash;
import org.modeshape.common.util.SecureHash.Algorithm;
import org.modeshape.jcr.value.BinaryKey;
import org.modeshape.jcr.value.BinaryValue;
import org.modeshape.jcr.value.binary.ExternalBinaryValue;
import org.slf4j.Logger;

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.generated.management.DatastreamProfile;

// TODO: add a date-time to ensure that the content is the same as
// the date the hash was computed

public class RESTFedoraDatastreamRecord
        implements FedoraDatastreamRecord {

    private static final Logger LOGGER
        = getLogger(RESTFedoraDatastreamRecord.class);

    private DatastreamProfile ds;

    private FedoraClient fc;

    private String pid;

    private String dsid;

    private byte[] sha1;

    /**
     * A constructor that takes as a parameter the DatastreamType object that
     * was returned by the FedoraClient as its source for information about the
     * fedora 3 datastream to be described by this object.
     */
    public RESTFedoraDatastreamRecord(FedoraClient fc, String pid, String dsId)
        throws FedoraClientException {
        this.fc = fc;
        this.pid = pid;
        this.dsid = dsId;
        ds = FedoraClient.getDatastream(pid, dsId).execute(fc)
                .getDatastreamProfile();
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

    /**
     * {@inheritDoc}
     */
    public Date getModificationDate() {
        return ds.getDsCreateDate().toGregorianCalendar().getTime();
    }

    /**
     * {@inheritDoc}
     */
    public Date getCreatedDate() {
        return ds.getDsCreateDate().toGregorianCalendar().getTime();
    }

    /**
     * {@inheritDoc}
     */
    public BinaryValue getContent() throws Exception {
        return new Fedora3DatastreamBinaryValue();
    }

    /**
     * Gets a SHA1 hash of the content of the datastreams.  The current
     * implementation checks first to see if fedora 3 provides this information
     * and failing that, computes it.
     * @throws FedoraClientException
     */
    public byte[] getSha1() throws Exception {
        if (sha1 != null) {
            return sha1;
        }
        if (ds.getDsChecksumType().equalsIgnoreCase("SHA-1")
                && ds.getDsChecksum() != null) {
            String sha1Str = ds.getDsChecksum();
            int len = sha1Str.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2]
                    = (byte) ((Character.digit(sha1Str.charAt(i), 16) << 4)
                            + Character.digit(sha1Str.charAt(i + 1), 16));
            }
            sha1 = data;
            LOGGER.trace("Loaded SHA1 for " + pid + " " + dsid
                    + " from repository.");
            return data;
        } else {
            long start = System.currentTimeMillis();
            InputStream is = FedoraClient.getDatastreamDissemination(pid, dsid)
                    .execute(fc).getEntityInputStream();
            try {
                sha1 = SecureHash.getHash(Algorithm.SHA_1, is);
                return sha1;
            } finally {
                is.close();
                LOGGER.trace("Computed SHA-1 from " + dsid + " on " + pid
                        + " in " + (System.currentTimeMillis() - start)
                        + "ms.");
            }
        }
    }

    public class Fedora3DatastreamBinaryValue extends ExternalBinaryValue {

        private FedoraClient fc;

        private Fedora3DatastreamBinaryValue() throws Exception {
            super(new BinaryKey(getSha1()), ds.getDsID(), ID.contentID(pid,
                    dsid).getId(), ds.getDsSize().longValue(), null, null);
        }

        /**
         * Gets the InputStream for the content.
         */
        public InputStream getStream() throws RepositoryException {
            try {
                return FedoraClient.getDatastreamDissemination(pid, dsid)
                        .execute(fc).getEntityInputStream();
            } catch (FedoraClientException e) {
                throw new RepositoryException(e);
            }
        }

    }

}
