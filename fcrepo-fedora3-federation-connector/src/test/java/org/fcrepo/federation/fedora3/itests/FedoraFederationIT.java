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

import static java.lang.System.getProperty;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.yourmediashelf.fedora.client.FedoraCredentials;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.fcrepo.connector.fedora3.ID;
import org.fcrepo.connector.fedora3.rest.RESTFedoraDatastreamRecordImpl;
import org.fcrepo.kernel.services.ObjectService;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.response.IngestResponse;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This integration test is incomplete but does include sufficient testing to
 * verify that objects, datastreams and contents are accessible (though
 * possibly not complete).
 *
 * @author Michael Durbin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring-test/test-container.xml"})
public class FedoraFederationIT {

    protected static final int SERVER_PORT = Integer.parseInt(System.getProperty("test.port", "8080"));

    protected static final String HOSTNAME = "localhost";

    protected static final String serverAddress = "http://" + HOSTNAME + ":" + SERVER_PORT + "/";

	private static final Logger logger = getLogger(FedoraFederationIT.class);

    protected static HttpClient client;

    private static String pid;
    private static String dsid;

    private static FedoraClient fc;

    @BeforeClass
    public static void ingestTestObjects() throws FedoraClientException, MalformedURLException {
        PoolingClientConnectionManager connectionManager =
                new PoolingClientConnectionManager();
        connectionManager.setMaxTotal(Integer.MAX_VALUE);
        connectionManager.setDefaultMaxPerRoute(5);
        connectionManager.closeIdleConnections(3, TimeUnit.SECONDS);
        client = new DefaultHttpClient(connectionManager);

        String fedoraUrl = "http://localhost:" + System.getProperty("servlet.port") + "/fedora";
        fc = new FedoraClient(new FedoraCredentials(fedoraUrl, "fedoraAdmin", "fc"));

        pid = "it:1";
        ingestFoxml(pid);
        dsid = "SIMPLE_TEXT";

    }

    @Test
    public void testRestConnection() throws Exception {
        HttpGet testRest = new HttpGet(serverAddress + "rest/");
        assertEquals("The rest service should be accessible", 200, getStatus(testRest));
    }

    @Test
    public void testFederationConnection() throws Exception {
        HttpGet testFederation = new HttpGet(serverAddress + "rest/f3");
        assertEquals("The federation should be accessible", 200, getStatus(testFederation));
    }

    @Test
    public void testObject() throws Exception {
        String url = serverAddress + "rest/f3/" + ID.objectID(pid).getId();
        HttpGet testContent = new HttpGet(url);
        assertEquals("Fetching object at " + url, 200, getStatus(testContent));
    }

    @Test
    public void testDatastream() throws Exception {
        String url = serverAddress + "rest/f3/" + ID.datastreamID(pid, dsid).getId();
        HttpGet testDatastream = new HttpGet(url);
        assertEquals("Fetching datastream at " + url, 200, getStatus(testDatastream));
    }

    @Test
    public void testDatastreamContent() throws Exception {
        String url = serverAddress + "rest/f3/" + ID.contentID(pid, dsid).getURLPath();
        HttpGet testContent = new HttpGet(url);
        assertEquals("Fetching datastream content at " + url, 200, getStatus(testContent));
        final HttpResponse response = client.execute(testContent);
        logger.debug("Returned from HTTP GET, now checking content...");
        assertEquals("Content must be identical to that in fedora 3!", "simple text\n",
                EntityUtils.toString(response.getEntity()));
    }

    public static void ingestFoxml(String pid) throws FedoraClientException {
        FedoraClient.ingest(pid).content(FedoraFederationIT.class.getClassLoader().getResourceAsStream("foxml/" + pid.replace(':', '_') + ".xml")).execute(fc);
    }

    protected HttpResponse execute(final HttpUriRequest method)
            throws ClientProtocolException, IOException {
        logger.debug("Executing: " + method.getMethod() + " to " +
                method.getURI());
        return client.execute(method);
    }

    protected int getStatus(final HttpUriRequest method)
            throws ClientProtocolException, IOException {
        HttpResponse response = execute(method);
        int result = response.getStatusLine().getStatusCode();
        if (!(result > 199) || !(result < 400)) {
            logger.warn(EntityUtils.toString(response.getEntity()));
        }
        return result;
    }
}
