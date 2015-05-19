package it.unibz.krdb.obda.owlrefplatform.core.abox;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import junit.framework.TestCase;

public class NTripleAssertionIteratorTest extends TestCase {
	public void testIteratorTest() throws IOException {
		File testFile = new File("src/test/resources/test/lubm-data.n3");
		URI fileURI = testFile.toURI();
		NTripleAssertionIterator iterator = new NTripleAssertionIterator(fileURI);
		
		int typeCount = 0;
		int objPropCount = 0;
		int datPropCount = 0;
		
		while (iterator.hasNext()) {
			Assertion ass = iterator.next();
			if (ass instanceof ClassAssertion) {
				typeCount +=1;
			} 
			else if (ass instanceof ObjectPropertyAssertion) {
				objPropCount +=1;
			}
			else if (ass instanceof DataPropertyAssertion) {
				datPropCount +=1;
			}  
		}
		
		assertEquals(2, typeCount);
		assertEquals(3, datPropCount);
		assertEquals(6, objPropCount);
		
	}
}
