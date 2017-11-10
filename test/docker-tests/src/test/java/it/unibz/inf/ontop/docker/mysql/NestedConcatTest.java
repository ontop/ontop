package it.unibz.inf.ontop.docker.mysql;

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

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import org.junit.Test;


public class NestedConcatTest extends AbstractVirtualModeTest {


    static final String owlfile = "/mysql/nestedconcat/test.owl";
    static final String obdafile = "/mysql/nestedconcat/test.obda";
    static final String propertyfile = "/mysql/nestedconcat/test.properties";

    public NestedConcatTest() {
        super(owlfile, obdafile, propertyfile);
    }

    @Test
    public void testConcat() throws Exception {

		/*
		 * Get the book information that is stored in the database
		 */
        String sparqlQuery =
//                "PREFIX : <http://www.semanticweb.org/meme/ontologies/2015/3/test#>\n" +
//                        "SELECT ?per ?yS\n" +
//                        "WHERE{\n" +
//                        "?per a :Period ; :yStart ?yS \n" +
//                        "}\n" +
//                        "LIMIT 1";
                "PREFIX : <http://www.semanticweb.org/meme/ontologies/2015/3/test#>\n" +
                            "SELECT ?per ?yS ?yE\n" +
                            "WHERE{\n" +
                            "?per a :Period ; :yStart ?yS ; :yEnd ?yE\n" +
                            "}\n" +
                            "LIMIT 1";

        runQuery(sparqlQuery);



    }
}
