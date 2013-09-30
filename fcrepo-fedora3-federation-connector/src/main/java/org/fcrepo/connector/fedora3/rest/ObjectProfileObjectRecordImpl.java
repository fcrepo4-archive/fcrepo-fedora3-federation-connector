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

import com.yourmediashelf.fedora.generated.access.DatastreamType;
import com.yourmediashelf.fedora.generated.access.ObjectProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Durbin
 */
public class ObjectProfileObjectRecordImpl
        extends AbstractFedoraObjectRecord {

    /**
     * A constructor that populates all required fields from the responses of
     * a FedoraClient calls.
     */
    public ObjectProfileObjectRecordImpl(ObjectProfile p,
            List<DatastreamType> dsTypes) {
        super(p.getPid(), p.getObjCreateDate().toGregorianCalendar().getTime(),
                p.getObjLastModDate().toGregorianCalendar().getTime());
        if (p.getObjState() != null && p.getObjState().trim().length() > 0) {
            super.state = p.getObjState();
        }
        if (p.getObjLabel() != null && p.getObjLabel().trim().length() > 0) {
            super.label = p.getObjLabel();
        }
        String ownerIdStr = p.getObjOwnerId();
        if (ownerIdStr != null && ownerIdStr.contains(",")) {
            super.ownerIds = Arrays.asList(ownerIdStr.split(","));
        } else if (ownerIdStr != null && ownerIdStr.trim().length() > 0) {
            super.ownerIds = Collections.singletonList(ownerIdStr);
        }
        super.datastreams = new ArrayList<String>();
        for (DatastreamType ds : dsTypes) {
            super.datastreams.add(ds.getDsid());
        }
    }
}
