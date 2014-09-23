package it.unibz.krdb.obda.testsuite;

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

import it.unibz.krdb.obda.quest.datatypes.MssqlDatatypeTest;
import it.unibz.krdb.obda.quest.datatypes.MysqlDatatypeTest;
import it.unibz.krdb.obda.quest.datatypes.OracleDatatypeTest;
import it.unibz.krdb.obda.quest.datatypes.PgsqlDatatypeTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class TestDatatypeSuite extends TestSuite {
		
	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("Database Data-type Test Suite");
		suite.addTest(MysqlDatatypeTest.suite());
		suite.addTest(PgsqlDatatypeTest.suite());
		suite.addTest(MssqlDatatypeTest.suite());
		suite.addTest(OracleDatatypeTest.suite());
//		suite.addTest(Db2DatatypeTest.suite());
		return suite;
	}
}
