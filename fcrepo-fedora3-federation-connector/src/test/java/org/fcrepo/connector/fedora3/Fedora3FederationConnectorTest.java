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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import javax.jcr.RepositoryException;

import org.fcrepo.connector.fedora3.rest.DefaultFedoraObjectRecordImpl;
import org.infinispan.schematic.document.EditableDocument;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modeshape.jcr.federation.spi.DocumentWriter;
import org.modeshape.jcr.federation.spi.PageKey;
import org.modeshape.jcr.value.BinaryValue;

/**
 * @author Michael Durbin
 */
public class Fedora3FederationConnectorTest {

    @Mock Fedora3DataInterface mockF3;

    @Mock BinaryValue mockBinaryValue;

    @Mock FedoraDatastreamRecord mockDCDatastream;

    @Mock EditableDocument mockument;

    @Mock DocumentWriter mockumentWriter;

    Fedora3FederationConnector c;

    /**
     * Sets up a Fedora3DataInterface that pretends to be a fedora repository
     * that contains two objects, "changeme:1" and "changeme:2".  The object
     * "changeme:2" has two datastreams: "DC" and "RELS-EXT".
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(mockF3.getObjectPids(anyInt(), anyInt())).thenReturn(new String[] { "changeme:1", "changeme:2" });
        DefaultFedoraObjectRecordImpl changeme1 = new DefaultFedoraObjectRecordImpl();
        changeme1.pid = "changeme:1";
        when(mockF3.getObjectByPid("changeme:1")).thenReturn(changeme1);
        changeme1.datastreams = new ArrayList<String>();
        DefaultFedoraObjectRecordImpl changeme2 = new DefaultFedoraObjectRecordImpl();
        changeme2.pid = "changeme:2";
        changeme2.datastreams = Arrays.asList(new String[] { "DC", "RELS-EXT"});
        when(mockF3.getObjectByPid("changeme:2")).thenReturn(changeme2);
        when(mockF3.getDatastream("changeme:2", "DC")).thenReturn(mockDCDatastream);
        when(mockDCDatastream.getContent()).thenReturn(mockBinaryValue);
        when(mockBinaryValue.getHexHash()).thenReturn("00000000");
        when(mockBinaryValue.getSize()).thenReturn((long) 100);
        when(mockF3.doesObjectExist("changeme:1")).thenReturn(true);
        when(mockF3.doesObjectExist("changeme:2")).thenReturn(true);
        when(mockF3.doesDatastreamExist("changeme:2", "DC")).thenReturn(true);
        when(mockF3.doesDatastreamExist("changeme:2", "RELS-EXT")).thenReturn(true);

        c = new MockedFedora3FederationConnector();
        c.f3 = mockF3;
    }

    @Test
    public void testInitialization() throws Exception {
        try {
            new Fedora3FederationConnector().initialize(null, null);
            Assert.fail("Initialization should fail without having set required properties.");
        } catch (RepositoryException e) {
        }

        Fedora3FederationConnector c = new Fedora3FederationConnector();
        c.fedoraUrl = "malformed-url";
        c.username = "username";
        c.password = "password";
        try {
            c.initialize(null, null);
        } catch (RepositoryException e) {
            Assert.assertEquals("Initialization should fail because of bad URL.", e.getCause().getClass(), java.net.MalformedURLException.class);
        }
    }

    @Test
    public void testGetDocumentById() throws Exception {
        Assert.assertNotNull("The root object is exposed by the federation.", c.getDocumentById(ID.ROOT_ID.getId()));
        Assert.assertNotNull("The object \"changeme:1\" is exposed by the federation.", c.getDocumentById(ID.objectID("changeme:1").getId()));
        Assert.assertNotNull("The object \"changeme:2\" is exposed by the federation.", c.getDocumentById(ID.objectID("changeme:2").getId()));
        Assert.assertNotNull("The datastream \"DC\" on \"changeme:2\" exists.", c.getDocumentById(ID.datastreamID("changeme:2", "DC").getId()));
        Assert.assertNotNull("The content of datastream \"DC\" on \"changeme:2\" exists.", c.getDocumentById(ID.contentID("changeme:2", "DC").getId()));

    }

    @Test
    public void testHasDocument() {
        Assert.assertTrue("Root document should exist.", c.hasDocument(ID.ROOT_ID.getId()));
        Assert.assertTrue("Document for \"changeme:1\" should exist.", c.hasDocument(ID.objectID("changeme:1").getId()));
        Assert.assertTrue("Document for \"DC\" datastream on \"changeme:2\" should exist.", c.hasDocument(ID.datastreamID("changeme:2", "DC").getId()));
        Assert.assertTrue("Document for content of \"DC\" datastream on \"changeme:2\" should exist.", c.hasDocument(ID.contentID("changeme:2", "DC").getId()));
    }

    @Test
    public void testGetChildren() {
        c.getChildren(new PageKey(ID.ROOT_ID.getId(), "0", 100));
    }

    /**
     * Overrides certain methods to allow for unit testing.
     */
    private class MockedFedora3FederationConnector extends Fedora3FederationConnector {

        /**
         * Overrides the implementation in org.modeshape.jcr.federation.spi.Connector to
         * return a dummy implementation.
         */
        public DocumentWriter newDocument(String id) {
            when(mockumentWriter.document()).thenReturn(mockument);
            return mockumentWriter;
        }
    }
}
