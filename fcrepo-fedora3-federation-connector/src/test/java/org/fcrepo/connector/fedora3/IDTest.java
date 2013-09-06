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

import org.junit.Assert;
import org.junit.Test;
import org.modeshape.jcr.api.JcrConstants;

/**
 * @author Michael Durbin
 */
public class IDTest {

    @Test
    public void testRootId() {
        Assert.assertTrue("The static root id should be the root id.",
                ID.ROOT_ID.isRootID());
        Assert.assertFalse("The root id should not be an object id.",
                ID.ROOT_ID.isObjectID());
        Assert.assertFalse("The root id should not be a datastream id.",
                ID.ROOT_ID.isDatastreamID());
        Assert.assertTrue("The root id should be \"/\".", ID.ROOT_ID.getId().equals("/"));
        Assert.assertTrue("The item with id \"/\" should be root.", new ID("/").isRootID());
        Assert.assertNull("The root id should have no parent.", ID.ROOT_ID.getParentId());
        Assert.assertTrue("The root name should have at least one character.", ID.ROOT_ID.getName().length() >= 1);
        Assert.assertNull("The root id should not be associated with any pid.", ID.ROOT_ID.getPid());
    }

    @Test
    public void testObjectId() {
        String pid = "changeme:1";
        ID id = ID.objectID(pid);
        Assert.assertTrue("The object ID should  be an object id.",
                id.isObjectID());
        Assert.assertFalse("The object ID should not be a root id.",
                id.isRootID());
        Assert.assertFalse("The object ID should not be a datastream id.",
                id.isDatastreamID());
        Assert.assertTrue("The object ID should retain the pid.",
                id.getPid().equals(pid));
        Assert.assertTrue("The object ID be the parent of the root id.",
                id.getParentId().equals(ID.ROOT_ID.getId()));
        Assert.assertNull("The object ID should not contain a dsid.",
                id.getDSID());
    }

    @Test
    public void testDatastreamId() {
        String pid = "changeme:1";
        String dsId = "RELS-EXT";
        ID objectId = ID.objectID(pid);
        ID id = ID.datastreamID(pid, dsId);
        Assert.assertTrue("The datastream ID should  be a datastream id.",
                id.isDatastreamID());
        Assert.assertFalse("The datastream ID should not be a root id.",
                id.isRootID());
        Assert.assertFalse("The datastream ID should not be an object id.",
                id.isObjectID());
        Assert.assertTrue("The datastream ID should retain the pid.",
                id.getPid().equals(pid));
        Assert.assertEquals("The datastream ID be the parent of an object id.",
                id.getParentId(), objectId.getId());
        Assert.assertEquals("The datastream ID should contain a dsid.",
                id.getDSID(), dsId);
    }

    @Test
    public void testContentId() {
        String pid = "changeme:1";
        String dsId = "RELS-EXT";
        ID id = ID.contentID(pid, dsId);
        ID datastreamId = ID.datastreamID(pid, dsId);
        Assert.assertTrue("The content ID should  be a content id.",
                id.isContentID());
        Assert.assertFalse("The content ID should not be a root id.",
                id.isRootID());
        Assert.assertFalse("The content ID should not be an object id.",
                id.isObjectID());
        Assert.assertFalse("The content ID should not be a datastream id",
                id.isDatastreamID());
        Assert.assertTrue("The content ID should retain the pid.",
                id.getPid().equals(pid));
        Assert.assertEquals("The content ID be the child of a datastream id.",
                id.getParentId(), datastreamId.getId());
        Assert.assertEquals("The content ID should contain a dsid.",
                id.getDSID(), dsId);
        Assert.assertEquals("The content ID should be the JCR constant.", id.getName(), JcrConstants.JCR_CONTENT);
    }

        /**
         * The pattern for names vs ids is such that an item's id is that
         * item's parent's id followed by a '/' and then the items name.
         */
        @Test
        public void testNamePattern() {
            ID contentId = ID.contentID("changeme:1", "RELS-EXT");
            Assert.assertEquals("An item's id should be the concatenation of its parent's id, a slash and it's name.", contentId.getId(), contentId.getParentId() + "/" + contentId.getName());
            ID dsId = new ID(contentId.getParentId());
            Assert.assertEquals("An item's id should be the concatenation of its parent's id, a slash and it's name.", dsId.getId(), dsId.getParentId() + "/" + dsId.getName());
        }
}
