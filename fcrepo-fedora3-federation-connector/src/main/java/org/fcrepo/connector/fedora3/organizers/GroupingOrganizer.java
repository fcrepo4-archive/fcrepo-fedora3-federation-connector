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
import org.fcrepo.connector.fedora3.ID;
import org.fcrepo.connector.fedora3.RepositoryOrganizer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Groups the pid-ordered list of objects in the fedora 3 repository in groups
 * that are no larger than a configurable size.  These grouping nodes will
 * have IDs that are "1-100 [firstPid]", which will allow a user to easily
 * find a particular pid (in much the same way he or she might find a word in a
 * dictionary).
 *
 * @author Michael Durbin
 */
public class GroupingOrganizer implements RepositoryOrganizer {

    public static final Pattern ID_PATTERN
        = Pattern.compile("(\\d)+\\-(\\d)+ (.*)");

    private int maxContainerSize = -1;

    private Fedora3DataInterface f3;

    private ContainerInfo rootContainer;

    /**
     * {@inheritDoc}
     */
    public void initialize(Fedora3DataInterface fedora) {
        if (f3 != null) {
            throw new IllegalStateException(
                    "Initialize must only be called once!");
        }
        f3 = fedora;
        if (maxContainerSize != -1) {
            rootContainer = new ContainerInfo();
        }
    }

    /**
     * Sets the maximum number of nodes that will be a child of a given node
     * in the federation (excludes datastream nodes).  This must be called
     * as part of the initialization of this class.
     * @param size a value 2 or greater that represents the maximum number of
     *             children desired for nodes in this organization
     */
    public void setMaxContainerSize(int size) {
        if (maxContainerSize != -1) {
            throw new IllegalStateException(
                    "MAX container size is immutable once set!");
        }
        if (size < 2) {
            throw new IllegalArgumentException();
        }
        maxContainerSize = size;
        if (f3 != null) {
            rootContainer = new ContainerInfo();
        }
    }

    /**
     * Gets the ids for the children of the given grouping node.  This method
     * forces the slow loading of children via queries to the
     * Fedora3DataInterface unless a similar call has already been cached.
     */
    public List<String> getChildrenForId(String id) {
        ContainerInfo c = rootContainer.findContainer(id);
        if (c == null) {
            throw new IllegalArgumentException();
        } else {
            List<String> result = new ArrayList<String>();
            if (!c.isLeafContainer()) {
                for (ContainerInfo cc : c.loadChildren()) {
                    result.add(cc.id);
                }
            } else {
                for (String pid
                    : f3.getObjectPids(c.offset, maxContainerSize)) {
                    result.add(ID.objectID(pid).getId());
                }
            }
            return result;
        }
    }

    /**
     * Gets the id of the parent node for the node indicated by the provided
     * id.
     */
    public String getParentForId(String id) {
        if (id.equals(ID.ROOT_ID.getId())) {
            return null;
        } else {
            return rootContainer.getParent(id);
        }
    }

    /**
     * Determines if an is is for one of the organizational nodes defined by
     * this RepositoryOrganizer.
     */
    public boolean isOrganizationalNode(String id) {
        return rootContainer.isOrContains(id);
    }

    /**
     * A class that represents an ordered tree of containers whose members
     * follow the conventions of the organization for this GroupOrganizer.
     * Actual fedora objects aren't represented in this tree, but there is
     * a methods to quickly determine to which leaf a given pid would belong.
     */
    private class ContainerInfo {

        public ContainerInfo() {
            this((int) Math.ceil(
                    logBaseN(f3.getSize(), maxContainerSize)), 0,
                    null);
        }




        public ContainerInfo(int depth, long offset, ContainerInfo parent) {
            this.offset = offset;
            this.size = (long) Math.pow(maxContainerSize, depth);
            long realLast = Math.min(f3.getSize(), this.offset + this.size);
            this.firstObjectId
                = ID.objectID(f3.getObjectPids(offset, 1).get(0)).getId();
            this.lastObjectId
                = ID.objectID(f3.getObjectPids(realLast - 1, 1).get(0))
                    .getId();
            id = (parent == null
                    ? "/" : offset + "-" + realLast + " " + firstObjectId);
            this.depth = depth;
        }

        public long offset;

        public long size;

        public String firstObjectId;

        public String lastObjectId;

        public List<ContainerInfo> children;

        public int depth;

        public String id;

        private List<ContainerInfo> getCachedChildren() {
            return getChildren(true);
        }

        private List<ContainerInfo> loadChildren() {
            return getChildren(false);
        }

        private List<ContainerInfo> getChildren(boolean onlyCachedResults) {
            if (children == null && depth > 1 && !onlyCachedResults) {
                children = new ArrayList<ContainerInfo>();
                for (long co = offset;
                     co < offset + size && co < f3.getSize();
                     co += (size / maxContainerSize)) {
                    children.add(new ContainerInfo(depth - 1, co, this));
                }
            }
            return children;
        }


        public boolean isContainerNodeId(String id) {
            return ID.ROOT_ID.getId().equals(id)
                    || ID_PATTERN.matcher(id).matches();
        }

        public ContainerInfo findContainer(String id) {
            if (this.id.equals(id)) {
                return this;
            }
            if (haveChildrenBeenCached()) {
                for (ContainerInfo c : getCachedChildren()) {
                    ContainerInfo match = c.findContainer(id);
                    if (match != null) {
                        return match;
                    }
                }
            }
            return null;
        }

        public String getParent(String id) {
            ContainerInfo p = null;
            if (isContainerNodeId(id)) {
                p = findParentContainer(id);
            } else {
                p = getContainingContainer(id);
            }
            return (p == null ? null : p.id);
        }

        /**
         * Only searches already lazily loaded nodes.
         */
        private ContainerInfo findParentContainer(String id) {
            if (haveChildrenBeenCached()) {
                for (ContainerInfo cc : getCachedChildren()) {
                    if (cc.id.equals(id)) {
                        return this;
                    } else {
                        ContainerInfo p = cc.findParentContainer(id);
                        if (p != null) {
                            return p;
                        }
                    }
                }
            }
            return null;
        }

        /**
         * Only searches already lazily loaded nodes.
         */
        private ContainerInfo getContainingContainer(String id) {
            if (isLeafContainer()) {
                return this;
            }
            if (!haveChildrenBeenCached()) {
                throw new IllegalStateException(id + " is probably a child of "
                        + this.id + " but hasn't been loaded.");
            }
            for (ContainerInfo c : getCachedChildren()) {
                if (c.firstObjectId.compareTo(id) <= 0
                        && c.lastObjectId.compareTo(id) >= 0) {
                    return c.getContainingContainer(id);
                }
            }
            return null;
        }

        private boolean isLeafContainer() {
            return depth == 1;
        }

        private boolean haveChildrenBeenCached() {
            return children != null;
        }


        /**
         * Only searches already lazily loaded nodes.
         */
        public boolean isOrContains(String id) {
            if (!isContainerNodeId(id)) {
                return false;
            }
            if (this.id.equals((id))) {
                return true;
            } else if (!haveChildrenBeenCached()) {
                return false;
            } else {
                for (ContainerInfo c :getCachedChildren()) {
                    if (c.isOrContains(id)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private static int logBaseN(long x, int n) {
        double log = Math.log(x) / Math.log(n);
        return (int) Math.ceil(log);

    }
}
