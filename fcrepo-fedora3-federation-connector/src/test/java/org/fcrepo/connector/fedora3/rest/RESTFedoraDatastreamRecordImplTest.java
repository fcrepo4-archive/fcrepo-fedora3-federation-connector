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

import com.yourmediashelf.fedora.generated.management.DatastreamHistory;
import org.fcrepo.connector.fedora3.FedoraDatastreamVersionRecord;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.util.Arrays;
import java.util.Date;

/**
 * Because the RESTFedoraDatastreamRecordImpl class is a quick and dirty
 * implementation that is intended to be a proof of concept, and because
 * future integration tests will fully cover the code, these unit tests
 * only cover the small amount of code that represents logic that could
 * be reused.
 *
 * @author Michael Durbin
 */
public class RESTFedoraDatastreamRecordImplTest {

    public static DatastreamHistory parseDatastreamHistory(String resourceName) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(DatastreamHistory.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (DatastreamHistory) unmarshaller.unmarshal(RESTFedoraDatastreamRecordImplTest.class.getClassLoader().getResource(resourceName));
    }

    public static RESTFedoraDatastreamRecordImpl getDSRecord(String resourceName) throws JAXBException {
        RESTFedoraDatastreamRecordImpl ds = new RESTFedoraDatastreamRecordImpl(null, parseDatastreamHistory(resourceName).getDatastreamProfile());
        return ds;
    }

    @Test
    public void testMockedRecord() throws Exception {
        RESTFedoraDatastreamRecordImpl testRecord = getDSRecord("mocked-responses/mock_1/dc-datastream-history.xml");
        Assert.assertEquals("Pid is preserved.", "mock:1", testRecord.getPid());
        Assert.assertEquals("DSID is preserved.", "DC", testRecord.getId());
        Assert.assertEquals("Control group is preserved.", "X",
                testRecord.getControlGroup());
        Assert.assertEquals("State is preserved.", "A", testRecord.getState());
        Assert.assertTrue("Versionable is preserved.", testRecord.getVersionable());

        FedoraDatastreamVersionRecord c = testRecord.getCurrentVersion();
        Assert.assertEquals("Label is preserved.", "Dublin Core Record for this object", c.getLabel());
        Assert.assertEquals("Version ID is preserved.", "DC.3", c.getVersionId());
        Assert.assertEquals("Created Date is preserved.", new Date(1380634413335L), c.getCreatedDate());
        Assert.assertEquals("Mime type is preserved.", "text/xml", c.getMimeType());
        Assert.assertEquals("Format URI is preserved.", "http://www.openarchives.org/OAI/2.0/oai_dc/", c.getFormatURI());
        Assert.assertEquals("No alt ids are present.", Arrays.asList(new String[] { "oai_dc", "dublin_core" }), c.getAltIDs());
        Assert.assertEquals("Content digest type is preserved.", "SHA-1", c.getContentDigestType());
        Assert.assertEquals("Content digest is preserved.", "920a51eb1824168e986bbb0ad7d14f81db28d8e1", c.getContentDigest());
        Assert.assertEquals("Content length is preserved.", 423, c.getContentLength());
        Assert.assertArrayEquals("SHA-1 needs not be computed.", RESTFedoraDatastreamRecordImpl.getSha1BytesFromHexString(c.getContentDigest()), c.getSha1());
        try {
            FedoraDatastreamVersionRecord p = testRecord.getHistory().get(3);
            Assert.assertNull("Content digest does not exist on first version.", p.getContentDigest());
            p.getSha1(); // should throw exception with this mock object
            Assert.fail("SHA-1 cannot be computed on this mock object.");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testGetSha1() {
        byte[] bytes = new byte[] { 0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF };
        String string = "0123456789ABCDEF";
        Assert.assertTrue(Arrays.equals(RESTFedoraDatastreamRecordImpl.getSha1BytesFromHexString(string), bytes));
    }
}
