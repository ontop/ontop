package it.unibz.inf.ontop.docker.mysql;

/*
 * #%L
 * ontop-test
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

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import org.junit.Test;


/**
 * Class to check the translation of the combination of Optional/Union in SPARQL into Datalog, and finally 
 * SQL
 * @author Minda, Guohui, mrezk
 */
public class LeftJoin3VirtualTest extends AbstractVirtualModeTest {

	private static final String owlfile = "/mysql/person/person.owl";
	private static final String obdafile = "/mysql/person/person3.obda";
	private static final String propertyfile = "/mysql/person/person3.properties";

	public LeftJoin3VirtualTest() {
		super(owlfile, obdafile, propertyfile);
	}

	@Test
	public void testLeftJoin() throws Exception {
		String query_multi1 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {"
				+ "  ?p a :Person . "
				+ "  ?p :name ?name . "
				+ "  OPTIONAL {?p :nick ?nick} }";
		countResults(query_multi1, 5);
	}

}
