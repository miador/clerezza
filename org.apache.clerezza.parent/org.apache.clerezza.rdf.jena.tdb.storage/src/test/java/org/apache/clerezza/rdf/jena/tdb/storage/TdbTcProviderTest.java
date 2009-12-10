/*
 * Copyright (c) 2009 Clerezza AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.clerezza.rdf.jena.tdb.storage;

import java.io.File;
import java.io.IOException;


import org.junit.After;
import org.junit.Before;
import org.apache.clerezza.rdf.core.access.TcProvider;
import org.apache.clerezza.rdf.core.test.TcProviderTest;

/**
 *
 * @author mir
 */
public class TdbTcProviderTest extends TcProviderTest {

	File tempFile;

	@Before
	public void setupDirectory() throws IOException {
		tempFile = File.createTempFile("tdbtest", null);
		tempFile.delete();
		tempFile.mkdirs();
	}

	@After
	public void cleanUp() {
		TdbTcProvider.delete(tempFile);
	}


	@Override
	protected TcProvider getInstance() {
		return new TdbTcProvider(tempFile);
	}
}
