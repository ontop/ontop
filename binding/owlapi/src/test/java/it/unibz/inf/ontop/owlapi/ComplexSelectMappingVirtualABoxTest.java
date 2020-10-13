package it.unibz.inf.ontop.owlapi;

/*
 * #%L
 * ontop-quest-owlapi
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

import com.google.common.collect.ImmutableList;
import org.junit.*;

/**
 * Test for CONCAT and REPLACE in SQL query
 */

public class ComplexSelectMappingVirtualABoxTest extends AbstractOWLAPITest {

	@BeforeClass
	public static void setUp() throws Exception {
		initOBDA("/test/complexmapping-create-h2.sql",
				"/test/complexmapping.obda",
				"/test/complexmapping.owl");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		release();
	}

    @Test
    public void testReplaceValue() throws Exception {
		String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> " +
				"SELECT * WHERE { ?x :U4 ?z . }";

        checkReturnedValues(query, "z", ImmutableList.of(
        		"\"ualue1\"^^xsd:string"));
    }

    @Test
	public void testConcat() throws Exception {
		String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> " +
				"SELECT * WHERE { ?x :U2 ?z. }";

		checkReturnedValues(query, "z", ImmutableList.of(
        		"\"NO value1\"^^xsd:string"));
	}

    @Test
	public void testDoubleConcat() throws Exception {

		String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> " +
				"SELECT * WHERE { ?x :U2 ?z; :U3 ?w. }";

		checkReturnedValues(query, "z", ImmutableList.of(
        		"\"NO value1\"^^xsd:string"));
	}

    @Test
    public void testConcat2() throws Exception {
		String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> " +
				"SELECT * WHERE { ?x :U5 ?z. }";

		checkReturnedValues(query, "z", ImmutableList.of(
        		"\"value1test\"^^xsd:string"));
    }

    @Test
    public void testConcat3() throws Exception {
        String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> " +
				"SELECT * WHERE { ?x :U6 ?z. }";

		checkReturnedValues(query, "z", ImmutableList.of(
        		"\"value1test\"^^xsd:string"));
    }

    @Test
    public void testConcat4() throws Exception {
        String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> " +
				"SELECT * WHERE { ?x :U7 ?z. }";

		checkReturnedValues(query, "z", ImmutableList.of(
        		"\"value1touri 1\"^^xsd:string"));
    }

    @Test
    public void testConcat5() throws Exception {
        String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> " +
				"SELECT * WHERE { ?x :U8 ?z. }";

		checkReturnedValues(query, "z", ImmutableList.of(
        		"\"value1test\"^^xsd:string"));
    }

    @Test
    public void testConcatAndReplaceUri() throws Exception {
        String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> " +
				"SELECT * WHERE { ?x :U9 ?z. }";

		checkReturnedValues(query, "z", ImmutableList.of(
        		"\"value1\"^^xsd:string"));
    }
}
