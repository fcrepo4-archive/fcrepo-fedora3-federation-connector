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

package org.fcrepo.federation.fedora3.itests;

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.FedoraCredentials;
import org.apache.poi.util.IOUtils;
import org.fcrepo.connector.fedora3.rest.RESTFedoraDatastreamRecordImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;

/**
 * This set of integration tests is meant to test the functionality of
 * RESTFedoraDatastreamRecordImpl and requires a fedora 3 repository to be
 * running.
 *
 * @author Michael Durbin
 */
public class RESTFedoraDatastreamRecordImplIT {

    String pid;

    private FedoraClient fc;

    @Before
    public void before() throws MalformedURLException, FedoraClientException {
        String fedoraUrl = "http://localhost:" + System.getProperty("servlet.port") + "/fedora";
        fc = new FedoraClient(new FedoraCredentials(fedoraUrl, "fedoraAdmin", "fc"));
        pid = FedoraClient.ingest().label("Sample Object for Test").execute(fc).getPid();
    }

    @Test
    public void testContent() throws Exception {
        String dsid = "DC";
        RESTFedoraDatastreamRecordImpl ds
                = new RESTFedoraDatastreamRecordImpl(fc, pid, dsid);

        ByteArrayOutputStream content = new ByteArrayOutputStream();
        IOUtils.copy(ds.getStream(), content);
        content.close();

        ByteArrayOutputStream disseminatedContent
                = new ByteArrayOutputStream();
        IOUtils.copy(FedoraClient.getDatastreamDissemination(pid,
                dsid).execute(fc).getEntityInputStream(), disseminatedContent);
        disseminatedContent.close();

        Assert.assertArrayEquals("Content exposed through RESTFedoraDatastreamRecordImpl should match the content disseminated directly from fedora 3!", content.toByteArray(), disseminatedContent.toByteArray());
    }
}
