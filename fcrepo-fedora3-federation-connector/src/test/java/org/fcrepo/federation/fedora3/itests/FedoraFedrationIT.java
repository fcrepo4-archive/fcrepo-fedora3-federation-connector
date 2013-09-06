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

import static java.lang.System.getProperty;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;

import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.fcrepo.kernel.services.ObjectService;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.response.IngestResponse;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This placeholder integration test exists only to show that the
 * test rigging is complete.
 *
 * @author Michael Durbin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring-test/repo.xml", "/spring-test/eventing.xml",  "/spring-test/test-container.xml"})
public class FedoraFedrationIT {

    @Autowired
    Repository repo;
    
	@Autowired
	ObjectService objectService;

	private static final Logger logger = getLogger(FedoraFedrationIT.class);

	@Test
	public void testFederationObject() throws LoginException, RepositoryException {
		final Session session = repo.login();
        objectService.getObjectNode(session, "/f3");
        session.save();
        session.logout();
	}
}
