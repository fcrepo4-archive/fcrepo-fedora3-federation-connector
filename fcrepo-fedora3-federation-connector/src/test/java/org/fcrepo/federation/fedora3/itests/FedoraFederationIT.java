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

import com.hp.hpl.jena.graph.Node;
import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.FedoraCredentials;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.fcrepo.connector.fedora3.ID;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static org.slf4j.LoggerFactory.getLogger;

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

    public static final String F3 = "http://fedora.info/definitions/v3/rest-api#";
    public static final String FCREPO = "http://fedora.info/definitions/v4/repository#";

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
        ID object = ID.objectID(pid);
        String url = serverAddress + "rest/f3/" + object.getId();
        HttpGet testObject = new HttpGet(url);
        HttpResponse response = execute(testObject);
        assertEquals("Object request should return status 200.", 200, response.getStatusLine().getStatusCode());
        /*  These tests would be great, but aren't yet functional...
        Model m = ModelFactory.createDefaultModel().read(response.getEntity().getContent(), serverAddress + "rest/","TURTLE");
        Resource r = m.getResource(url);

        assertNotNull("Resource should be described.", r);
        assertEquals("Check parent", serverAddress + "rest/f3", r.getProperty(m.getProperty(FCREPO, "hasParent")).getObject().toString());
        assertEquals("Check object pid", "it:1", r.getProperty(m.getProperty(F3, "pid")).getString());
        assertEquals("Check object state.", "A", r.getProperty(m.getProperty(F3, "objState")).getString());
        assertEquals("Check object label.", "Integration Test Example 1", r.getProperty(m.getProperty(F3, "objLabel")).getString());
        assertEquals("Check object ownerId.", "fedoraAdmin", r.getProperty(m.getProperty(F3, "objOwnerId")).getString());
        assertNodeIsDate(r.getProperty(m.getProperty(F3, "objCreatedDate")).getObject().asNode(), "2013-09-23T21:25:35.35Z", "createdDate");
        assertNodeIsDate(r.getProperty(m.getProperty(F3, "objLastModifiedDate")).getObject().asNode(), "2013-10-01T18:56:56.998Z", "lastModifiedDate");
        */
    }

    private void assertNodeIsDate(Node node, String dateStr, String label) {
        assertEquals("Check object " + label + " type.", "http://www.w3.org/2001/XMLSchema#dateTime", node.getLiteralDatatypeURI());
        assertEquals("Check object " + label + " literal.", dateStr, node.getLiteralValue().toString());
    }

    @Test
    public void testDatastream() throws Exception {
        ID ds = ID.datastreamID(pid, dsid);
        String url = serverAddress + "rest/f3/" + ds.getId();
        HttpGet testDatastream = new HttpGet(url);
        HttpResponse response = execute(testDatastream);
        assertEquals("Datastream request should return status 200.", 200, response.getStatusLine().getStatusCode());
        /*  These tests would be great, but aren't yet functional...
        Model m = ModelFactory.createDefaultModel().read(response.getEntity().getContent(), serverAddress + "rest/","TURTLE");
        Resource r = m.getResource(url);
        assertNotNull("Resource should be described.", r);
        assertEquals("Check parent", serverAddress + "rest/f3/" + ds.getParentId(), r.getProperty(m.getProperty(FCREPO, "hasParent")).getObject().toString());
        assertEquals("Check datastream dsid", "SIMPLE_TEXT", r.getProperty(m.getProperty(F3, "dsid")).getString());
        assertEquals("Check datastream control group.", "M", r.getProperty(m.getProperty(F3, "controlGroup")).getString());
        assertEquals("Check datastream state.", "A", r.getProperty(m.getProperty(F3, "dsState")).getString());
        assertEquals("Check datastream versionable.", false, r.getProperty(m.getProperty(F3, "dsVersionable")).getBoolean());

        assertEquals("Check datastream version id.", "SIMPLE_TEXT.1", r.getProperty(m.getProperty(F3, "dsVersionId")).getString());
        assertEquals("Check datastream label.", "", r.getProperty(m.getProperty(F3, "dsLabel")).getString());
        assertNodeIsDate(r.getProperty(m.getProperty(F3, "dsCreatedDate")).getObject().asNode(), "2013-10-01T18:12:37.126Z", "createdDate");

        assertEquals("Check datastream mime type.", "text/plain", r.getProperty(m.getProperty(F3, "dsMimeType")).getString());
        assertNull("Check datastream formatURI.", r.getProperty(m.getProperty(F3, "dsFormatURI")));
        assertNull("Check datastream altIds.", r.getProperty(m.getProperty(F3, "dsAltIds")));
        //assertEquals("Check datastream size.", "12", r.getProperty(m.getProperty(F3, "dsSize")).getString());
        assertEquals("Check datastream contentDigestType.", "SHA-1", r.getProperty(m.getProperty(F3, "dsContentDigestType")).getString());
        //assertEquals("Check datastream contentDigestLabel.", "", r.getProperty(m.getProperty(F3, "dsContentDigest")).getString());
        */
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
