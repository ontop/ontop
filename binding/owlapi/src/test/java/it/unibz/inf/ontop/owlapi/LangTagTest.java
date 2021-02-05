package it.unibz.inf.ontop.owlapi;
/*
 * #%L
 * ontop-quest-owlapi3
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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


/***
 * Reproduces Issue #242.
 *
 * Checks that language tags are handled correctly if they appear in assertions in the .owl file.
 *
 */
public class LangTagTest extends AbstractOWLAPITest {

    @BeforeClass
    public static void setUp() throws Exception {
        initOBDA("/test/langTag/create-h2.sql",
                "/test/langTag/langTag.obda",
                "/test/langTag/langTag.owl",
                "/test/langTag/langTag.properties");
    }

    @After
    public void tearDown() throws Exception {
        release();
    }

    @Test
    public void testLangTag() throws Exception {
        String query = "SELECT ?instancia ?comment  WHERE {\n" +
                "VALUES ?instancia { <http://www.basecia.es/ontologia#CuentaContableActivos> } .\n" +
                "?instancia <http://www.w3.org/2000/01/rdf-schema#comment> ?comment .\n" +
                "}";

        checkReturnedValues(query, "comment", ImmutableList.of(
                "\"Cuenta bancaria interna de activos.\"@es"));
    }
}
