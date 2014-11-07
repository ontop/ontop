package org.semanticweb.ontop.owlrefplatform.core.abox;

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

import junit.framework.TestCase;
import org.semanticweb.ontop.model.ValueConstant;
import org.semanticweb.ontop.ontology.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;

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
			else if (ass instanceof PropertyAssertion) {
				PropertyAssertion ass2 = (PropertyAssertion)ass;
				if (ass2.getValue2() instanceof ValueConstant)
					datPropCount +=1;
				else
					objPropCount +=1;
			}  
		}
		
		assertEquals(2, typeCount);
		assertEquals(3, datPropCount);
		assertEquals(6, objPropCount);
		
	}
}
