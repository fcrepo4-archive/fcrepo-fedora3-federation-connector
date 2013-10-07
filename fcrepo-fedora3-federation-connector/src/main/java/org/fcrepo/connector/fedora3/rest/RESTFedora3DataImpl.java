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
import com.yourmediashelf.fedora.client.FedoraCredentials;
import com.yourmediashelf.fedora.generated.access.FedoraRepository;
import org.fcrepo.connector.fedora3.Fedora3DataInterface;
import org.fcrepo.connector.fedora3.FedoraDatastreamRecord;
import org.fcrepo.connector.fedora3.FedoraObjectRecord;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * An implementation of Fedora3DataInterface that uses the REST API to access
 * fedora content.
 * 
 * @author Michael Durbin
 */
public class RESTFedora3DataImpl implements Fedora3DataInterface {

    private static final Logger LOGGER
        = getLogger(RESTFedora3DataImpl.class);

    private FedoraClient fc;

    /**
     * The cached size (number of objects) of the repository.  This allows
     * subsequent calls to getSize() to be cheap.
     */
    private long size = -1L;

    /**
     * Constructor with credentials necessary for a connection to fedora's REST
     * API.
     */
    public RESTFedora3DataImpl(String fedoraUrl, String username,
            String password) throws MalformedURLException,
            FedoraClientException {
        initialize(new FedoraClient(
                new FedoraCredentials(fedoraUrl, username, password)));
    }

    private void initialize(FedoraClient fc) throws FedoraClientException {
        this.fc = fc;
        FedoraRepository r = FedoraClient.describeRepository().execute(fc)
                .getRepositoryInfo();
        LOGGER.debug("Initialized connection to fedora "
                + r.getRepositoryVersion() + " at "
                + r.getRepositoryBaseURL() + " with the resource index enabled"
                + " and " + getSize() + " objects.");
    }


    /**
     * {@inheritDoc}
     */
    public FedoraObjectRecord getObjectByPid(String pid) {
        try {
            return new ObjectProfileObjectRecordImpl(
                    FedoraClient.getObjectProfile(pid)
                    .execute(fc).getObjectProfile(),
                    FedoraClient.listDatastreams(pid)
                    .execute(fc).getDatastreams());
        } catch (FedoraClientException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean doesObjectExist(String pid) {
        try {
            return FedoraClient.getObjectProfile(pid).execute(fc).getStatus()
                    == 200;
        } catch (FedoraClientException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * The current implementation relies on the following ITQL query against
     * the resource index to fetch results.
     *
     * <pre>
     *  {@code
     *    select     $object
     *    from       <#ri>
     *    where      $object <info:fedora/fedora-system:def/model#hasModel>
     *        <info:fedora/fedora-system:FedoraObject-3.0>
     *    order by   $object
     *    limit      -pageSize-
     *    offset     -offset-
     *  }
     * </pre>
     */
    public List<String> getObjectPids(int offset, int pageSize) {
        String query = "select $object"
                + " from <#ri>"
                + " where $object"
                + " <info:fedora/fedora-system:def/model#hasModel>"
                + " <info:fedora/fedora-system:FedoraObject-3.0>"
                + " order by $object"
                + " limit " + pageSize
                + " offset " + offset;
        try {
            ArrayList<String> pids = new ArrayList<String>();
            BufferedReader r = new BufferedReader(
                    new InputStreamReader(
                            FedoraClient.riSearch(query).lang("itql")
                                    .format("csv").execute(fc)
                                    .getEntityInputStream()));
            r.readLine().equals("\"object\"");
            String objectUri = null;
            while ((objectUri = r.readLine()) != null) {
                pids.add(objectUri.substring("info:fedora/".length()));
            }
            return pids;
        } catch (FedoraClientException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * The current implementation relies on the following ITQL query against
     * the resource index to determine repository size.
     *
     * <pre>
     *  {@code
     *    select count(
     *        select     $object
     *        from       <#ri>
     *        where      $object <info:fedora/fedora-system:def/model#hasModel>
     *            <info:fedora/fedora-system:FedoraObject-3.0>)
     *    from <#ri>
     *    where $a $b $c
     *  }
     * </pre>
     */
    public long getSize() {
        if (size == -1) {
            String query = "select count("
                    + " select $object"
                    + " from <#ri>"
                    + " where $object"
                    + " <info:fedora/fedora-system:def/model#hasModel>"
                    + " <info:fedora/fedora-system:FedoraObject-3.0>"
                    + ") from <#ri> where $a $b $c";
            try {
                BufferedReader r = new BufferedReader(
                        new InputStreamReader(
                                FedoraClient.riSearch(query).lang("itql")
                                        .format("csv").execute(fc)
                                        .getEntityInputStream()));
                r.readLine().equals("\"k0\"");
                size =  Long.parseLong(r.readLine());
            } catch (FedoraClientException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return size;
    }

    /**
     * {@inheritDoc}
     */
    public FedoraDatastreamRecord getDatastream(String pid, String dsid) {
        try {
            return new RESTFedoraDatastreamRecordImpl(fc, pid, dsid);
        } catch (FedoraClientException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean doesDatastreamExist(String pid, String dsid) {
        try {
            return FedoraClient.getDatastream(pid, dsid).execute(fc)
                    .getStatus() == 200;
        } catch (FedoraClientException ex) {
            throw new RuntimeException(ex);
        }
    }

}
