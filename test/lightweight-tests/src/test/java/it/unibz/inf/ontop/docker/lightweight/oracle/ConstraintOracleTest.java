package it.unibz.inf.ontop.docker.lightweight.oracle;

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

import it.unibz.inf.ontop.docker.lightweight.AbstractConstraintTest;
import it.unibz.inf.ontop.docker.lightweight.OracleLightweightTest;

@OracleLightweightTest
public class ConstraintOracleTest extends AbstractConstraintTest {

    private static final String propertyFile = "/dbconstraints/dbconstraints-oracle.properties";

    // TODO: CHECK THAT THE DATABASE CONTAINS THE TABLES
    public ConstraintOracleTest(String method) {
        super(method, propertyFile);
    }

}
