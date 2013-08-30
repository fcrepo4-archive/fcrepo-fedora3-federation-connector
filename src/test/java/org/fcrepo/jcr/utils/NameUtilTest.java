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

package org.fcrepo.jcr.utils;

import org.junit.Assert;
import org.junit.Test;

public class NameUtilTest {

    @Test
    public void testEncodeDecode() {
        String [] examplesToEncode = new String[] { "test:" + (char) 12510 + (char) 12452 + (char) 12463  + (char) 12523, "what/will:this[look]like|*?"};
        for (String pid : examplesToEncode) {
            String encodedName = NameUtil.encodeForLocalName(pid);
            Assert.assertEquals("Name encoding must be reversable.", pid, NameUtil.decodeLocalName(encodedName));
        }
    }
}
