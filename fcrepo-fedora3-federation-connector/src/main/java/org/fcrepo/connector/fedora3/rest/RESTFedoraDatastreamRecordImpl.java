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

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.generated.management.DatastreamProfile;
import org.fcrepo.connector.fedora3.FedoraDatastreamRecord;
import org.fcrepo.connector.fedora3.FedoraDatastreamVersionRecord;
import org.modeshape.common.util.SecureHash;
import org.modeshape.common.util.SecureHash.Algorithm;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * An implementation of {@link FedoraDatastreamRecord} that gets all of its
 * information using the fedora 3 REST API.
 * 
 * @author Michael Durbin
 */
public class RESTFedoraDatastreamRecordImpl
        implements FedoraDatastreamRecord {

    private static final Logger LOGGER
        = getLogger(RESTFedoraDatastreamRecordImpl.class);

    private DatastreamProfile ds;

    private FedoraClient fc;

    private List<FedoraDatastreamVersionRecord> history;

    /**
     * A constructor that takes as a parameter the DatastreamType object that
     * was returned by the FedoraClient as its source for information about the
     * fedora 3 datastream to be described by this object.
     */
    public RESTFedoraDatastreamRecordImpl(FedoraClient fc, String pid,
            String dsid) throws FedoraClientException {
        this(fc, FedoraClient.getDatastreamHistory(pid, dsid)
                .execute(fc).getDatastreamProfile().getDatastreamProfile());
        if (!ds.getPid().equals(pid)) {
            throw new RuntimeException("Pid mismatch! " + pid + " != "
                    + ds.getPid());
        }
        if (!ds.getDsID().equals(dsid)) {
            throw new RuntimeException("DSID mismatch! " + dsid + " != "
                    + ds.getDsID());
        }
    }

    /**
     * This part of object construction is only separated into a protected
     * constructor for unit testing as it allows mock DatastreamProfile
     * objects to be provided and used (which allows most calls to be tested
     * with a null FedoraClient.
     */
    protected RESTFedoraDatastreamRecordImpl(FedoraClient fc,
            List<DatastreamProfile> dsProfiles) {
        history = new ArrayList<FedoraDatastreamVersionRecord>();
        for (DatastreamProfile version : dsProfiles) {
            history.add(new Version(version));
        }
        ds = dsProfiles.get(0);
        this.fc = fc;
    }

    /**
     * {@inheritDoc}
     */
    public String getPid() {
        return ds.getPid();
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
    public String getControlGroup() {
        return ds.getDsControlGroup();
    }

    /**
     * {@inheritDoc}
     */
    public String getState() {
        return blankToNull(ds.getDsState());
    }

    /**
     * {@inheritDoc}
     */
    public boolean getVersionable() {
        return "true".equals(ds.getDsVersionable());
    }

    /**
     * {@inheritDoc}
     */
    public List<FedoraDatastreamVersionRecord> getHistory() {
        return history;
    }

    /**
     * {@inheritDoc}
     */
    public FedoraDatastreamVersionRecord getCurrentVersion() {
        return history.get(0);
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().equals("")) {
            return null;
        }
        return value;
    }

    private class Version implements FedoraDatastreamVersionRecord {

        private byte[] sha1;

        DatastreamProfile dsVer;

        public Version(DatastreamProfile version) {
            dsVer = version;
        }

        /**
         * {@inheritDoc}
         */
        public String getVersionId() {
            return dsVer.getDsVersionID();
        }

        /**
         * {@inheritDoc}
         */
        public String getLabel() {
            return blankToNull(dsVer.getDsLabel());
        }

        /**
         * {@inheritDoc}
         */
        public Date getCreatedDate() {
            return dsVer.getDsCreateDate().toGregorianCalendar().getTime();
        }

        /**
         * {@inheritDoc}
         */
        public String getMimeType() {
            return dsVer.getDsMIME();
        }

        /**
         * {@inheritDoc}
         */
        public String getFormatURI() {
            return blankToNull(dsVer.getDsFormatURI());
        }

        /**
         * {@inheritDoc}
         */
        public List<String> getAltIDs() {
            return dsVer.getDsAltID();
        }

        /**
         * {@inheritDoc}
         */
        public String getContentDigestType() {
            return "DISABLED".equals(dsVer.getDsChecksumType())
                    ? null
                    : dsVer.getDsChecksumType();
        }

        /**
         * {@inheritDoc}
         */
        public String getContentDigest() {
            return "none".equals(dsVer.getDsChecksum())
                    ? null
                    : dsVer.getDsChecksum();
        }

        /**
         * {@inheritDoc}
         */
        public long getContentLength() {
            return dsVer.getDsSize().longValue();
        }

        /**
         * {@inheritDoc}
         * The current implementation provides an InputStream directly from an
         * authenticated request to the Fedora 3 rest API.
         */
        public InputStream getStream() throws Exception {
            return FedoraClient.getDatastreamDissemination(getPid(), getId())
                    .asOfDateTime(dsVer.getDsCreateDate().toString())
                    .execute(fc).getEntityInputStream();
        }

        /**
         * Gets a SHA1 hash of the content of the datastreams.  The current
         * implementation checks first to see if fedora 3 provides this
         * information and failing that, computes it.
         */
        public byte[] getSha1() throws Exception {
            if (sha1 != null) {
                return sha1;
            }
            if (dsVer.getDsChecksumType().equalsIgnoreCase("SHA-1")
                    && dsVer.getDsChecksum() != null) {
                sha1 = getSha1BytesFromHexString(dsVer.getDsChecksum());
                LOGGER.trace("Loaded SHA1 for " + getPid() + " " + getId()
                        + " from repository.");
                return sha1;
            } else {
                long start = System.currentTimeMillis();
                InputStream is = FedoraClient.getDatastreamDissemination(
                        getPid(), getId())
                        .asOfDateTime(dsVer.getDsCreateDate().toString())
                        .execute(fc).getEntityInputStream();
                try {
                    sha1 = SecureHash.getHash(Algorithm.SHA_1, is);
                    return sha1;
                } finally {
                    is.close();
                    LOGGER.trace("Computed SHA-1 from " + getId() + " on "
                            + getPid() + " in "
                            + (System.currentTimeMillis() - start) + "ms.");
                }
            }
        }

    }

    /**
     * Converts a String of hexidecimal digits (0-F) into the bytes that would
     * be expressed by such a String.
     */
    protected static byte[] getSha1BytesFromHexString(String hexStr) {
        int len = hexStr.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2]
                = (byte) ((Character.digit(hexStr.charAt(i), 16) << 4)
                    + Character.digit(hexStr.charAt(i + 1), 16));
        }
        return data;
    }
}
