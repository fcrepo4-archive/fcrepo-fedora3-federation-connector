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

import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.generated.access.ObjectDatastreams;
import com.yourmediashelf.fedora.generated.access.ObjectProfile;
import org.fcrepo.connector.fedora3.FedoraObjectRecord;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

/**
 * @Michael Durbin
 */
public class ObjectProfileObjectRecordImplTest {

    public static ObjectProfile parseObjectProfile(String resourceName) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(ObjectProfile.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (ObjectProfile) unmarshaller.unmarshal(ObjectProfileObjectRecordImplTest.class.getClassLoader().getResource(resourceName));
    }

    public static ObjectDatastreams parseListDatastreamsResponse(String resourceName) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(ObjectDatastreams.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (ObjectDatastreams) unmarshaller.unmarshal(ObjectProfileObjectRecordImplTest.class.getClassLoader().getResource(resourceName));
    }

    @Test
    public void testObjectProfile() throws JAXBException, FedoraClientException, ParseException {
        FedoraObjectRecord rec = new ObjectProfileObjectRecordImpl(parseObjectProfile("mocked-responses/test_1/object-profile.xml"), parseListDatastreamsResponse("mocked-responses/test_1/list-datastreams.xml").getDatastream());
        Assert.assertEquals("PID should be parsed properly.", "test:1", rec.getPid());
        Assert.assertEquals("State should be parsed properly.", "A", rec.getState());
        Assert.assertEquals("Label should be parsed properly.", "page 1", rec.getLabel());
        Assert.assertEquals("OwnerID should be parsed properly.", Collections.singletonList("fedoraAdmin"), rec.getOwnerIds());
        Assert.assertEquals("Modification date should be parsed properly.", new Date(1368637379464L), rec.getModificationDate());
        Assert.assertEquals("Creation date should be parsed properly.", new Date(1304029580911L), rec.getCreatedDate());
        Assert.assertEquals("Datastream id list should be parsed properly.", Arrays.asList(new String[]{"DC", "RELS-EXT"}), rec.listDatastreamIds());
    }



}
