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

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

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

    @Test
    public void testGetSha1() {
        byte[] bytes = new byte[] { 0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF };
        String string = "0123456789ABCDEF";
        Assert.assertTrue(Arrays.equals(RESTFedoraDatastreamRecordImpl.getSha1BytesFromHexString(string), bytes));
    }
}
