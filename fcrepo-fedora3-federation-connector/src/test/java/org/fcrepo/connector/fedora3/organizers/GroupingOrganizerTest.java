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

package org.fcrepo.connector.fedora3.organizers;

import org.fcrepo.connector.fedora3.Fedora3DataInterface;
import org.fcrepo.connector.fedora3.FedoraDatastreamRecord;
import org.fcrepo.connector.fedora3.FedoraObjectRecord;
import org.fcrepo.connector.fedora3.ID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * @author Michael Durbin
 */
public class GroupingOrganizerTest {

    @Mock
    Fedora3DataInterface smallRepo;

    @Mock
    Fedora3DataInterface largerRepo;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(smallRepo.getSize()).thenReturn(3L);
        when(smallRepo.getObjectPids(0, 1)).thenReturn(Arrays.asList(new String[] { "pid:0" }));
        when(smallRepo.getObjectPids(1, 1)).thenReturn(Arrays.asList(new String[] { "pid:1" }));
        when(smallRepo.getObjectPids(2, 1)).thenReturn(Arrays.asList(new String[] { "pid:2" }));
        when(largerRepo.getObjectPids(0, 1)).thenReturn(Arrays.asList(new String[] { "pid:0" }));
        when(largerRepo.getObjectPids(1, 1)).thenReturn(Arrays.asList(new String[] { "pid:1" }));
        when(largerRepo.getObjectPids(2, 1)).thenReturn(Arrays.asList(new String[] { "pid:2" }));
        when(largerRepo.getObjectPids(3, 1)).thenReturn(Arrays.asList(new String[] { "pid:3" }));
        when(largerRepo.getObjectPids(4, 1)).thenReturn(Arrays.asList(new String[] { "pid:4" }));


    }

    @Test
    public void testConstructor() {
        GroupingOrganizer o = new GroupingOrganizer();
        o.initialize(smallRepo);
        o.setMaxContainerSize(100);
        try {
            o = new GroupingOrganizer();
            o.initialize(smallRepo);
            o.setMaxContainerSize(1);
            fail("Groups smaller than 2 should result in an exception!");
        } catch (IllegalArgumentException ex) {
        }
        try {
            o = new GroupingOrganizer();
            o.initialize(smallRepo);
            o.initialize(smallRepo);
            fail("Initialize should not be able to be called twice!");
        } catch (IllegalStateException ex) {
        }
        try {
            o = new GroupingOrganizer();
            o.setMaxContainerSize(10);
            o.setMaxContainerSize(11);
            fail("SetMaxContainerSize should not be able to be called twice!");
        } catch (IllegalStateException ex) {
        }

    }

    @Test
    public void testGetChildrenForId() {
        GroupingOrganizer o = new GroupingOrganizer();
        o.initialize(smallRepo);
        o.setMaxContainerSize(100);
        assertEquals("There should be no grouping nodes for small repositories with large groups.", Collections.emptyList(), o.getChildrenForId(ID.ROOT_ID.getId()));
        try {
            o.getChildrenForId(ID.objectID("pid:0").getId());
            fail("GroupingOrganizer should throw an exception when asked about non-grouping nodes.");
        } catch (IllegalArgumentException ex) {
        }

        o = new GroupingOrganizer();
        o.initialize(smallRepo);
        o.setMaxContainerSize(2);
        assertEquals("There should be two grouping nodes from root when max size is 2 and there are more than 2 objects.", 2, o.getChildrenForId(ID.ROOT_ID.getId()).size());

        try {
            o.getChildrenForId("this id isn't present");
            fail("Searching for children of invalid ids should fail.");
        } catch (IllegalArgumentException ex) {
            // good
        }
    }

    @Test
    public void testGetParentForId() {
        GroupingOrganizer o = new GroupingOrganizer();
        o.initialize(smallRepo);
        o.setMaxContainerSize(2);
        assertNull("Root should have no parent.", o.getParentForId(ID.ROOT_ID.getId()));

        String group1 = null;
        try {
            group1 = o.getParentForId(ID.objectID("pid:0").getId());
            fail("Lazy loading should result in no value being found yet.");
        } catch (IllegalStateException ex) {
        }
        assertNotNull(o.getChildrenForId(ID.ROOT_ID.getId())); // force node load
        group1 = o.getParentForId(ID.objectID("pid:0").getId());
        assertNotNull("Forced load should result in a non-null return.", group1);

        String group2 = o.getParentForId(ID.objectID("pid:2").getId());
        assertEquals("pid:0 should be in the first group.", o.getChildrenForId(ID.ROOT_ID.getId()).get(0), group1);
        assertEquals("pid:2 should be in the second group.", o.getChildrenForId(ID.ROOT_ID.getId()).get(1), group2);
        assertEquals("First group of root should have root as parent.", ID.ROOT_ID.getId(), o.getParentForId(group1));
    }

    @Test
    public void testIsOrganizationalNode() {
        GroupingOrganizer o = new GroupingOrganizer();
        o.initialize(smallRepo);
        o.setMaxContainerSize(2);
        assertTrue("Root is organizational node.", o.isOrganizationalNode(ID.ROOT_ID.getId()));
        assertTrue("First child of root in a 3 level repo is an organizational node.", o.isOrganizationalNode(o.getChildrenForId(ID.ROOT_ID.getId()).get(0)));
        assertFalse("Unknown id is not an organizational node.", o.isOrganizationalNode("unknown"));
        assertFalse("Object id is not an organizational node.", o.isOrganizationalNode(ID.objectID("pid:0").getId()));
    }

    @Test
    public void testIDPattern() {
        assertTrue(GroupingOrganizer.ID_PATTERN.matcher("1-2 " + ID.objectID("pid:0").getId()).matches());
    }

    @Test
    public void testDeepHierarchies() {
        // dig until we find an object
        GroupingOrganizer o = new GroupingOrganizer();
        o.initialize(createHugeRepo(10000000));
        o.setMaxContainerSize(10);
        String firstObjectIdStr = getNthObject(o, ID.ROOT_ID.getId(), 0);

        ID objectId = new ID(firstObjectIdStr);
        String dsIdStr = ID.datastreamID(objectId.getPid(), "DC").getId();
        assertEquals(o.getParentForId(objectId.getId()), o.getParentForId(dsIdStr));
        try {
            o.getParentForId(ID.objectID("pid:00000100").getId());
            fail("Second page shouldn't be loaded.");
        } catch (IllegalStateException ex) {
        }
    }

    private String getNthObject(GroupingOrganizer o, String parent, int choice) {
        String firstChild = o.getChildrenForId(parent).get(choice);
        if (o.isOrganizationalNode(firstChild)) {
            return getNthObject(o, firstChild, choice);
        } else {
            return firstChild;
        }
    }

    protected Fedora3DataInterface createHugeRepo(final long size) {

        return new Fedora3DataInterface() {
            @Override
            public FedoraObjectRecord getObjectByPid(String pid) {
                return null;
            }

            @Override
            public boolean doesObjectExist(String pid) {
                return false;
            }

            @Override
            public List<String> getObjectPids(long offset, int pageSize) {
                DecimalFormat f = new DecimalFormat(
                        String.valueOf(size).replaceAll(".", "0"));
                List<String> result = new ArrayList<String>();
                for (long i = offset; i < offset + pageSize; i ++) {
                    result.add("pid:" + f.format(i));
                }
                return result;
            }

            @Override
            public long getSize() {
                return size;
            }

            @Override
            public FedoraDatastreamRecord getDatastream(String pid, String dsid) {
                return null;
            }

            @Override
            public boolean doesDatastreamExist(String pid, String dsid) {
                return false;
            }
        };
    }



}
