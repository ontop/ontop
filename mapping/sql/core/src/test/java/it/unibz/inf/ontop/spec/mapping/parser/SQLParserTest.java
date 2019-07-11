package it.unibz.inf.ontop.spec.mapping.parser;

/*
 * #%L
 * ontop-obdalib-core
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

import it.unibz.inf.ontop.dbschema.DatabaseRelationDefinition;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RDBMetadata;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.spec.mapping.parser.exception.InvalidSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.parser.exception.UnsupportedSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.parser.impl.RAExpression;
import it.unibz.inf.ontop.spec.mapping.parser.impl.SelectQueryParser;
import org.junit.Before;
import org.junit.Test;

import static it.unibz.inf.ontop.utils.SQLMappingTestingTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SQLParserTest {

	private RDBMetadata metadata;
	private SelectQueryParser sqp;

	@Before
	public void beforeEachTest() {
		metadata = createDummyMetadata();
		QuotedIDFactory idfac = metadata.getQuotedIDFactory();

		DBTypeFactory dbTypeFactory = TYPE_FACTORY.getDBTypeFactory();

		DatabaseRelationDefinition r = metadata.createDatabaseRelation(idfac.createRelationID(null, "student"));
		r.addAttribute(idfac.createAttributeID("id"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r.addAttribute(idfac.createAttributeID("name"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);
		r.addAttribute(idfac.createAttributeID("birth_year"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r.addAttribute(idfac.createAttributeID("birth_date"), "DATE", dbTypeFactory.getDBTermType(5, "DATE"), false);
		r.addAttribute(idfac.createAttributeID("semester"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r.addAttribute(idfac.createAttributeID("nationality"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);
		r.addAttribute(idfac.createAttributeID("grade"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r.addAttribute(idfac.createAttributeID("class"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r.addAttribute(idfac.createAttributeID("address"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);

		DatabaseRelationDefinition r2 = metadata.createDatabaseRelation(idfac.createRelationID(null, "QUEST_DATA_PROPERTY_LITERAL_ASSERTION"));
		r2.addAttribute(idfac.createAttributeID("URI"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);
		r2.addAttribute(idfac.createAttributeID("ISBNODE"), "BOOLEAN", dbTypeFactory.getDBTermType(2, "BOOLEAN"), false);
		r2.addAttribute(idfac.createAttributeID("IDX"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r2.addAttribute(idfac.createAttributeID("VALUE"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);
		r2.addAttribute(idfac.createAttributeID("LANG"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);

		DatabaseRelationDefinition r5 = metadata.createDatabaseRelation(idfac.createRelationID(null, "QUEST_OBJECT_PROPERTY_ASSERTION"));
		r5.addAttribute(idfac.createAttributeID("URI1"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);
		r5.addAttribute(idfac.createAttributeID("URI2"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);
		r5.addAttribute(idfac.createAttributeID("ISBNODE"), "BOOLEAN", dbTypeFactory.getDBTermType(2, "BOOLEAN"), false);
		r5.addAttribute(idfac.createAttributeID("ISBNODE2"), "BOOLEAN", dbTypeFactory.getDBTermType(2, "BOOLEAN"), false);
		r5.addAttribute(idfac.createAttributeID("IDX"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);

		DatabaseRelationDefinition r3 = metadata.createDatabaseRelation(idfac.createRelationID(null, "table1"));
		r3.addAttribute(idfac.createAttributeID("id"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r3.addAttribute(idfac.createAttributeID("name"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);
		r3.addAttribute(idfac.createAttributeID("value"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);

		DatabaseRelationDefinition r4 = metadata.createDatabaseRelation(idfac.createRelationID("HR", "REGIONS"));
		r4.addAttribute(idfac.createAttributeID("REGION_ID"), "INT)", dbTypeFactory.getDBTermType(0, "INT)"), false);

		DatabaseRelationDefinition r6 = metadata.createDatabaseRelation(idfac.createRelationID(null, "tableName"));
		r6.addAttribute(idfac.createAttributeID("cast"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r6.addAttribute(idfac.createAttributeID("do"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r6.addAttribute(idfac.createAttributeID("extract"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r6.addAttribute(idfac.createAttributeID("siblings"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r6.addAttribute(idfac.createAttributeID("first"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r6.addAttribute(idfac.createAttributeID("following"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r6.addAttribute(idfac.createAttributeID("last"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r6.addAttribute(idfac.createAttributeID("materialized"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r6.addAttribute(idfac.createAttributeID("nulls"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r6.addAttribute(idfac.createAttributeID("partition"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r6.addAttribute(idfac.createAttributeID("range"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r6.addAttribute(idfac.createAttributeID("row"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r6.addAttribute(idfac.createAttributeID("rows"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r6.addAttribute(idfac.createAttributeID("value"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r6.addAttribute(idfac.createAttributeID("xml"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);

		DatabaseRelationDefinition r7 = metadata.createDatabaseRelation(idfac.createRelationID(null, "grade"));
		r7.addAttribute(idfac.createAttributeID("st_id"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r7.addAttribute(idfac.createAttributeID("class_id"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r7.addAttribute(idfac.createAttributeID("grade"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r7.addAttribute(idfac.createAttributeID("score"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r7.addAttribute(idfac.createAttributeID("course"), "VARCHAR(10)", dbTypeFactory.getDBTermType(1, "VARCHAR(10)"), false);
		r7.addAttribute(idfac.createAttributeID("mark"), "VARCHAR(10)", dbTypeFactory.getDBTermType(1, "VARCHAR(10)"), false);
		r7.addAttribute(idfac.createAttributeID("pass"), "BOOLEAN", dbTypeFactory.getDBTermType(3, "BOOLEAN"), false);
		r7.addAttribute(idfac.createAttributeID("sm_id"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);

		DatabaseRelationDefinition r9 = metadata.createDatabaseRelation(idfac.createRelationID(null, "semester"));
		r9.addAttribute(idfac.createAttributeID("id"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);

		DatabaseRelationDefinition r8 = metadata.createDatabaseRelation(idfac.createRelationID(null, "tax"));
		r8.addAttribute(idfac.createAttributeID("payee"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);
		r8.addAttribute(idfac.createAttributeID("amount"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);

		DatabaseRelationDefinition r10 = metadata.createDatabaseRelation(idfac.createRelationID(null, "all_tables"));
		r10.addAttribute(idfac.createAttributeID("table_name"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);
		r10.addAttribute(idfac.createAttributeID("owner"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);

		DatabaseRelationDefinition r11 = metadata.createDatabaseRelation(idfac.createRelationID(null, "all_views"));
		r11.addAttribute(idfac.createAttributeID("owner"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);

		DatabaseRelationDefinition r12 = metadata.createDatabaseRelation(idfac.createRelationID(null, "people"));
		r12.addAttribute(idfac.createAttributeID("\"id\""), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);
		r12.addAttribute(idfac.createAttributeID("\"nick2\""), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);

		DatabaseRelationDefinition r13 = metadata.createDatabaseRelation(idfac.createRelationID(null, "pet"));
		r13.addAttribute(idfac.createAttributeID("name"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);
		r13.addAttribute(idfac.createAttributeID("testcol"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);


		DatabaseRelationDefinition r14 = metadata.createDatabaseRelation(idfac.createRelationID(null, "despatch"));
		r14.addAttribute(idfac.createAttributeID("des_date"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);
		r14.addAttribute(idfac.createAttributeID("des_amount"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r14.addAttribute(idfac.createAttributeID("ord_amount"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);

		DatabaseRelationDefinition r15 = metadata.createDatabaseRelation(idfac.createRelationID(null, "Product"));
		r15.addAttribute(idfac.createAttributeID("maker"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);
		r15.addAttribute(idfac.createAttributeID("type"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);
		r15.addAttribute(idfac.createAttributeID("model"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);

		DatabaseRelationDefinition r16 = metadata.createDatabaseRelation(idfac.createRelationID(null, "PC"));
		r16.addAttribute(idfac.createAttributeID("model"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);

		DatabaseRelationDefinition r17 = metadata.createDatabaseRelation(idfac.createRelationID("dbo", "TEMPERATURE_DEVIATION"));
		r17.addAttribute(idfac.createAttributeID("ID"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);
		r17.addAttribute(idfac.createAttributeID("DATETIME"), "DATETIME", dbTypeFactory.getDBTermType(4, "DATETIME"), false);
		r17.addAttribute(idfac.createAttributeID("SCALE"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r17.addAttribute(idfac.createAttributeID("INTERVAL"), "DATETIME", dbTypeFactory.getDBTermType(4, "DATETIME"), false);

		DatabaseRelationDefinition r18 = metadata.createDatabaseRelation(idfac.createRelationID("northwind", "Suppliers"));
		r18.addAttribute(idfac.createAttributeID("Region"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);
		r18.addAttribute(idfac.createAttributeID("PostalCode"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);
		r18.addAttribute(idfac.createAttributeID("Address"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);
		r18.addAttribute(idfac.createAttributeID("Country"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);


		DatabaseRelationDefinition r19 = metadata.createDatabaseRelation(idfac.createRelationID("oreda", "pm_maint_items"));
		r19.addAttribute(idfac.createAttributeID("owner_id"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r19.addAttribute(idfac.createAttributeID("inst_id"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r19.addAttribute(idfac.createAttributeID("i_id"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r19.addAttribute(idfac.createAttributeID("ec_code"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);
		r19.addAttribute(idfac.createAttributeID("mi_code"), "VARCHAR(10)", dbTypeFactory.getDBTermType(1, "VARCHAR(10)"), false);
		r19.addAttribute(idfac.createAttributeID("su_code"), "VARCHAR(10)", dbTypeFactory.getDBTermType(1, "VARCHAR(10)"), false);
		r19.addAttribute(idfac.createAttributeID("mc_code"), "VARCHAR(8)", dbTypeFactory.getDBTermType(1, "VARCHAR(8)"), false);
		r19.addAttribute(idfac.createAttributeID("mac_code"), "VARCHAR(8)", dbTypeFactory.getDBTermType(1, "VARCHAR(8)"), false);
		r19.addAttribute(idfac.createAttributeID("pm_interval"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);

		DatabaseRelationDefinition r20 = metadata.createDatabaseRelation(idfac.createRelationID("oreda", "pm_program"));
		r20.addAttribute(idfac.createAttributeID("owner_id"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r20.addAttribute(idfac.createAttributeID("inst_id"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r20.addAttribute(idfac.createAttributeID("i_id"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);
		r20.addAttribute(idfac.createAttributeID("ec_code"), "VARCHAR(20)", dbTypeFactory.getDBTermType(1, "VARCHAR(20)"), false);
		r20.addAttribute(idfac.createAttributeID("su_code"), "VARCHAR(10)", dbTypeFactory.getDBTermType(1, "VARCHAR(10)"), false);
		r20.addAttribute(idfac.createAttributeID("mc_code"), "VARCHAR(8)", dbTypeFactory.getDBTermType(1, "VARCHAR(8)"), false);
		r20.addAttribute(idfac.createAttributeID("mac_code"), "VARCHAR(8)", dbTypeFactory.getDBTermType(1, "VARCHAR(8)"), false);
		r20.addAttribute(idfac.createAttributeID("pm_interval"), "INT", dbTypeFactory.getDBTermType(0, "INT"), false);

		sqp = new SelectQueryParser(metadata, CORE_SINGLETONS);
	}

	@Test
	public void test_1_1_1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT * FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(9, re.getAttributes().size());
	}


	@Test
	public void test_1_1_2() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT student.* FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(9, re.getAttributes().size());
	}

	@Test
	public void test_1_2_1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
	}

	@Test
	public void test_1_2_2() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id, name FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to DISTINCT
	public void test_1_3_1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT DISTINCT name FROM student");
	}

	// @Test
	// the SQL is not valid
	// yet JSQLParser accepts it in the form of SELECT name FROM student
	public void test_1_3_2() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT ALL name FROM student");
	}

	@Test(expected = UnsupportedSelectQueryException.class)
	// due to DISTINCT ON (PostgreSQL-specific) is not supported
	public void test_1_3_3() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("select DISTINCT ON (name,age,year) name,age FROM student");
	}

	@Test
	public void test_1_4() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT student.id FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
	}

	@Test
	public void test_1_5() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT student.id, student.name FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test
	public void test_1_5_extra() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT \"URI\" as X, VALUE as Y, LANG as Z\n" +
				"FROM QUEST_DATA_PROPERTY_LITERAL_ASSERTION\n" +
				"WHERE ISBNODE = FALSE AND LANG IS NULL AND IDX = 1");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(3, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().size());
	}

	@Test
	public void test_1_5_extra_2() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id, name as alias1, value as alias2 FROM table1");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().size());
	}

	@Test
	public void test_1_5_extra_3() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		// to_char (Oracle specific cast)
		RAExpression re = sqp.parse("select to_char(REGION_ID) as RID FROM HR.REGIONS");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
	}

	@Test
	public void test_1_5_extra_4() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT \"URI1\" as X, \"URI2\" as Y\n" +
				"FROM QUEST_OBJECT_PROPERTY_ASSERTION\n" +
				"WHERE ISBNODE = FALSE AND ISBNODE2 = FALSE AND IDX = 2");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(3, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test
	// all of these are legal column names in SQL server;
	// 'row' and 'rows' are not legal on Oracle;
	public void test_1_5_extra_5() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT cast, do, extract, first, following,\n" +
				"last, materialized, nulls, partition, range,\n" +
				"row, rows, siblings, value, xml\n" +
				"FROM tableName");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(15, re.getAttributes().size());
	}

	@Test
	public void test_1_6_1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT undergraduate.* FROM student as undergraduate");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(9, re.getAttributes().size());
	}

	@Test
	public void test_1_6_2() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT undergraduate.id FROM student as undergraduate");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
	}

	@Test
	public void test_1_7() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT alias.id, alias.name FROM student as alias");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test(expected = InvalidSelectQueryException.class) // alias does not exist
	public void test_1_7_1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT alias.id, alias.name FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test
	public void test_1_8() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id FROM \"STUDENT\"");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
	}

	@Test
	public void test_1_9() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		// ROMAN (Feb 2017): changed to upper-case STUDENT (otherwise invalid)
		RAExpression re = sqp.parse("SELECT id FROM \"public\".\"STUDENT\"");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
	}

	@Test
	public void test_1_10() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		// ROMAN (Feb 2017): changed t2 to t1 (otherwise invalid)
		RAExpression re = sqp.parse("SELECT t1.id, t1.name FROM \"public\".\"STUDENT\" as t1");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test
	public void test_2_1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id FROM student WHERE id=1");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
	}

	@Test
	public void test_2_2() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id, name FROM student WHERE id=1 AND name='John'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test
	public void test_2_3() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id, name, semester, birth_year, nationality "
				+ "FROM student "
				+ "WHERE name<>'John' AND semester>2 AND semester<7 AND "
				+ "birth_year>=1984 AND birth_year<=1990 AND "
				+ "(nationality='IT' OR nationality='DE')");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(6, re.getFilterAtoms().size()); // OR has precedence over AND
		assertEquals(5, re.getAttributes().size());
	}

	@Test
	public void test_2_4() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT graduate.id, graduate.name FROM student as graduate WHERE graduate.name<>'John'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test
	public void test_2_5() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id, name, grade FROM student WHERE grade is null");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().size());
	}

	@Test
	public void test_2_6() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id, name, grade FROM student WHERE grade is not null");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().size());
	}

	@Test
	public void test_2_7() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id, name, grade FROM student WHERE grade is null AND name<>'John'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().size());
	}

	@Test
	public void test_2_8() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		// ROMAN (Feb 2017): changed to upper-case STUDENT (otherwise invalid)
		RAExpression re = sqp.parse("SELECT id, name FROM \"public\".\"STUDENT\" WHERE name<>'John'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test
	public void test_2_9() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		// ROMAN (Feb 2017): changed to upper-case STUDENT (otherwise invalid)
		RAExpression re = sqp.parse("SELECT t1.id, t1.name FROM \"public\".\"STUDENT\" as t1 "
				+ "WHERE t1.name<>'John'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test
	public void test_2_10() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		// ROMAN (Feb 2017): changed to upper-case STUDENT (otherwise invalid)
		RAExpression re = sqp.parse("SELECT t1.id, t1.name, t1.grade FROM \"public\".\"STUDENT\" as t1 "
				+ "WHERE t1.grade is not null AND t1.name<>'John'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().size());
	}

	@Test
	public void test_2_11() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id, name FROM student WHERE class IN (7, 8, 9)");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test
	public void test_2_12() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id, name, grade FROM student WHERE name IN ('John', 'Jack', 'Clara')");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().size());
	}

	@Test
	public void test_3_1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT MAX(score) as max_score FROM grade");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
	}

	@Test
	public void test_3_2() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT MIN(score) as min_score FROM grade");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
	}

	@Test
	public void test_3_3() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT AVG(score) as avg_score FROM grade");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
	}

	@Test
	public void test_3_4() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT SUM(amount) as total_amount FROM tax");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to COUNT(*)
	public void test_3_5() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT COUNT(*) as student_count FROM student");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to COUNT(id)
	public void test_3_6() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT COUNT(id) as student_count FROM student");
	}

	@Test
	public void test_3_7() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT EVERY(id) as student_count FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
	}

	// @Test(expected = UnsupportedSelectQueryException.class)
	// not a valid SQL query - ANY is a keyword and used here in a position of the function
	public void test_3_8() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT ANY(id) FROM student");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to DISTINCT
	public void test_3_8_1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT DISTINCT maker FROM Product "
				+ "WHERE type = 'PC' AND NOT model = ANY (SELECT model FROM PC)");
	}

	// @Test(expected = UnsupportedSelectQueryException.class)
	// not a valid SQL query - SOME is a keyword and used here in a position of the function
	public void test_3_9() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT SOME(id) FROM student");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to DISTINCT
	public void test_3_9_1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT DISTINCT maker FROM Product "
				+ "WHERE type = 'PC' AND NOT model = SOME (SELECT model FROM PC)");
	}

	@Test(expected = UnsupportedSelectQueryException.class)
	public void test_4_1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT nationality, COUNT(id) as num_nat FROM student GROUP BY nationality");
	}

	@Test(expected = UnsupportedSelectQueryException.class)
	public void test_4_2() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT nationality, COUNT(id) num_nat FROM student WHERE birth_year>2000 GROUP BY nationality");
	}

	@Test
	public void test_4_3() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT name as student_name, address as student_address FROM student WHERE id >= 66 AND id <= 69");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to ALL
	public void test_4_4() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT des_date,des_amount,ord_amount FROM despatch WHERE des_amount > ALL("
				+ "SELECT ord_amount FROM orders WHERE ord_amount=2000)");
	}

	@Test
	public void test_5_1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 JOIN grade t2 ON t1.id=t2.st_id");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(4, re.getAttributes().size());
	}

	@Test
	public void test_5_1_1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT t1.id as sid, t1.name as fullname FROM student t1 JOIN grade t2 ON t1.id=t2.st_id AND t2.mark='A'");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test
	public void test_5_1_2() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT t1.id, name FROM student t1 JOIN grade t2 ON t1.id=t2.st_id AND t2.\"SCORE\">=25");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test
	public void test_5_1_3() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT t1.id, name FROM student t1 JOIN grade t2 ON t1.id=t2.st_id AND t2.pass=true");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test
	public void test_5_2() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 INNER JOIN grade t2 ON t1.id=t2.st_id");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(4, re.getAttributes().size());
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to LEFT JOIN
	public void test_5_3() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 LEFT JOIN grade t2 ON t1.id=t2.st_id");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to RIGHT JOIN
	public void test_5_4() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 RIGHT JOIN grade t2 ON t1.id=t2.st_id");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to FULL JOIN
	public void test_5_5() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id, name, class_id, grade FROM student t1 FULL JOIN grade t2 ON t1.id=t2.st_id");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to LEFT JOIN
	public void test_5_6() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id, name, class_id, grade FROM student t1 LEFT OUTER JOIN grade t2 ON t1.id=t2.st_id");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to RIGTH JOIN
	public void test_5_7() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id, name, class_id, grade FROM student t1 RIGHT OUTER JOIN grade t2 ON t1.id=t2.st_id");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to FULL JOIN
	public void test_5_8() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id, name, class_id, grade FROM student t1 FULL OUTER JOIN grade t2 ON t1.id=t2.st_id");
	}

	@Test
	public void test_5_9() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT t1.id, name, score FROM student t1 JOIN grade t2 ON t1.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id");
		assertEquals(3, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().size());
	}

	@Test
	public void test_5_10() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT t1.id, t1.name, t2.score FROM (SELECT id, name FROM student WHERE student.name='John') AS t1 JOIN grade as t2 ON t1.id=t2.st_id");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().size());
	}

	// TODO: check the intention
	public void test_5_11() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id, name, score FROM student JOIN grade USING (id)");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().size());
	}

	@Test
	public void test_6_1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT t1.id, t1.name, t2.grade FROM (SELECT id, name FROM student) t1, (SELECT st_id as sid, grade FROM grade) t2 WHERE t1.id=t2.sid");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().size());
	}

	@Test
	public void test_6_2() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT * FROM (SELECT id, name, score FROM student JOIN grade ON student.id=grade.st_id) t1");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().size());
	}

	@Test
	public void test_6_3() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT * FROM (SELECT id, name, score FROM student JOIN grade ON student.id=grade.st_id) t1 WHERE t1.score>=25");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().size());
	}

	@Test
	public void test_7_1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT ('ID-' || student.id) as sid FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
		assertFalse(re.getAttributes().keySet().iterator().next() instanceof Variable);
	}

	@Test
	public void test_7_1_b() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT CONCAT('ID-', student.id, 'b') as sid FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
		assertFalse(re.getAttributes().keySet().iterator().next() instanceof Variable);
	}


	@Test
	public void test_7_2() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT (grade.score * 30 / 100) as percentage from grade");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
		assertFalse(re.getAttributes().keySet().iterator().next() instanceof Variable);
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to UNION ALL
	public void test_8_1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT name FROM student UNION ALL SELECT name FROM erasmus");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to UNION ALL and UNION
	public void test_8_2() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT name FROM student UNION ALL SELECT name FROM erasmus UNION SELECT DISTINCT payee FROM tax");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to UNION ALL
	public void test_8_3() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT name FROM student WHERE id = 20 UNION ALL SELECT name FROM erasmus WHERE id = 20");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to UNION
	public void test_8_4() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT name FROM student JOIN grade on student.id=grade.st_id AND grade.score>=25 UNION SELECT name FROM erasmus");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to UNION ALL
	public void test_8_5() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id, name, course, score, semester FROM student t1 JOIN grade t2 ON t1.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id "
				+ "UNION ALL SELECT id, name, course, score, semester FROM erasmus t4 JOIN grade t2 ON t4.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id");
	}

	@Test
	public void test_9_1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id, name, address from student where name = 'John'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().size());
	}

	@Test
	public void test_9_2() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id, name, address from student where id = 20");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().size());
	}

	@Test
	public void test_9_3() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT payee, amount from tax where amount = 12.345");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test
	public void test_9_4_1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id, name, address from student where birth_date = '1984-01-22 00:02:01.234'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().size());
	}

	@Test
	public void test_9_4_2() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id, name, address from student where birth_date = '1984-01-22 00:02:01'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().size());
	}

	@Test
	public void test_9_4_3() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT id, name, address from student where birth_date = '1984-01-22'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().size());
	}

	@Test
	public void test_9_5() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		// ROMAN (Feb 2017): changed passed to pass
		RAExpression re = sqp.parse("SELECT st_id, course, score from grade where pass = TRUE");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().size());
	}

	@Test
	public void test_10_1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		// ROMAN (Feb 2017): changed passed to pass
		RAExpression re = sqp.parse("SELECT name from grade, student where pass = TRUE AND course = 'CS001' AND ( (score = 8 AND mark = 'B') OR (score = 7 AND mark = 'C') OR (score >= 9 AND mark = 'A') )");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(3, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
	}

	@Test
	public void test_10_2() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		// ROMAN (Feb 2017): changed passed to pass
		RAExpression re = sqp.parse("SELECT name from grade, student where pass = FALSE AND ( course = 'CS001' OR ( (score = 6 AND mark = 'D') OR (score <= 5 AND mark = 'E') ) )");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
	}

	@Test
	public void test_11() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		// ROMAN (Feb 2017): changed passed to pass
		RAExpression re = sqp.parse("SELECT \"NAME\" from grade, student where pass = FALSE AND ( \"COURSE\" = 'CS001' OR ( (score = 6 AND mark = 'D') OR (score <= 5 AND mark = 'E') ) )");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
	}

	@Test
	public void test_11_1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("select t1.owner NAME from all_tables t1, all_tables t2, ALL_VIEWS where t1.table_name = t2.table_name and t1.owner = t2.owner and t1.owner = ALL_VIEWS.OWNER");
		assertEquals(3, re.getDataAtoms().size());
		assertEquals(3, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
	}

	@Test
	public void test_12() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("select name from grade, student where score BETWEEN 6 AND 8");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
	}

	@Test
	public void test_13() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("select REGEXP_REPLACE(name, ' +', ' ') as reg from student ");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
		assertFalse(re.getAttributes().keySet().iterator().next() instanceof Variable);
	}


	@Test(expected = UnsupportedSelectQueryException.class) // due to DISTINCT
	public void testUnquoted0() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT DISTINCT 3 AS \"v0QuestType\", NULL AS \"v0Lang\", CAST(\"QpeopleVIEW0\".\"nick2\" AS CHAR) AS \"v0\", 1 AS \"v1QuestType\", NULL AS \"v1Lang\", QpeopleVIEW0.id AS \"v1\""
				+ "FROM people \"QpeopleVIEW0\" "
				+ "WHERE \"QpeopleVIEW0\".\"id\" IS NOT NULL AND \"QpeopleVIEW0\".\"nick2\" IS NOT NULL");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to NULL
	//add support for CAST also in unquoted visited query
	public void testUnquoted1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT 3 AS \"v0QuestType\", NULL AS \"v0Lang\", CAST(\"QpeopleVIEW0\".\"nick2\" AS CHAR) AS \"v0\", 1 AS \"v1QuestType\", NULL AS \"v1Lang\", QpeopleVIEW0.id AS \"v1\""
				+ "FROM people \"QpeopleVIEW0\" "
				+ "WHERE \"QpeopleVIEW0\".\"id\" IS NOT NULL AND \"QpeopleVIEW0\".\"nick2\" IS NOT NULL");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to DISTINCT
	// Does not parse SELECT DISTINCT (on purpose)
	public void testUnquoted2() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT DISTINCT 3 AS \"v0QuestType\", NULL AS \"v0Lang\", CAST(\"QpeopleVIEW0\".\"nick2\" AS CHAR) AS \"v0\", 1 AS \"v1QuestType\", NULL AS \"v1Lang\", QpeopleVIEW0.id AS \"v1\""
				+ "FROM people \"QpeopleVIEW0\" "
				+ "WHERE \"QpeopleVIEW0\".\"id\" IS NOT NULL AND \"QpeopleVIEW0\".\"nick2\" IS NOT NULL");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to CAST
	public void testCast1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT CAST(`view0`.`nick2` AS CHAR (8000) CHARACTER SET utf8) AS `v0` FROM people `view0` WHERE `view0`.`nick2` IS NOT NULL");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to DISTINCT
	public void testCast2() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT DISTINCT CAST(`view0`.`nick2` AS CHAR (8000) CHARACTER SET utf8) AS `v0` FROM people `view0` WHERE `view0`.`nick2` IS NOT NULL");
	}

	/* Regex in MySQL, Oracle and Postgres*/

	@Test
	public void testRegexMySQL() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT * FROM pet WHERE name REGEXP '^b'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test
	public void testRegexBinaryMySQL() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT * FROM pet WHERE name REGEXP BINARY '^b'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test
	public void testRegexPostgres() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT * FROM pet WHERE name ~ 'foo'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to SIMILAR (PostgreSQL)
	public void testRegexPostgresSimilarTo() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT * FROM pet WHERE 'abc' SIMILAR TO 'abc'");
	}

	@Test
	public void testRegexOracle() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT * FROM pet WHERE REGEXP_LIKE(testcol, '[[:alpha:]]')");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to NOT without parenthesis
	public void testRegexNotOracle() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT * FROM pet WHERE NOT REGEXP_LIKE(testcol, '[[:alpha:]]')");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test
	public void testRegexNotOracle_1() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT * FROM pet WHERE NOT (REGEXP_LIKE(testcol, '[[:alpha:]]'))");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().size());
	}

	@Test (expected = UnsupportedSelectQueryException.class) // due to COALESCE
	public void test_md5() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT MD5(CONCAT(COALESCE(Address, RAND()), COALESCE(City, RAND()),\n" +
				"COALESCE(Region, RAND()), COALESCE(PostalCode, RAND()), COALESCE(Country,\n" +
				"RAND()) )) AS locationID FROM northwind.Suppliers");
	}

	@Test
	public void test_concatOracle() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT ('ID-' || student.id || 'type1') \"sid\" FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
		assertFalse(re.getAttributes().keySet().iterator().next() instanceof Variable);
	}

	@Test
	public void test_RegexpReplace() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT REGEXP_REPLACE('Hello World', ' +', ' ') as reg FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
		assertFalse(re.getAttributes().keySet().iterator().next() instanceof Variable);
	}

	@Test(expected = UnsupportedSelectQueryException.class)
	// due to CONVERT(varchar(50), ...), where varchar(50) is treated as a function calls
	public void test_2_p() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT [ID] as \"KEYID\"\n" +
				"      ,CONVERT(varchar(50), [DATETIME], 0) as \"DATETIMEH\"\n" +
				"      ,[SCALE] as \"SCALE\"\n" +
				"      ,[INTERVAL] as \"TEMPINTERVAL\"\n" +
				"  FROM [CIM].[dbo].[TEMPERATURE_DEVIATION] where [INTERVAL] = '0-10'");
	}

	@Test
	public void test_2() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT [ID]\n" +
				"      ,[DATETIME]\n" +
				"      ,[SCALE]\n" +
				"      ,[INTERVAL]\n" +
				"  FROM [CIM].[dbo].[TEMPERATURE_DEVIATION] where [INTERVAL] = '0-10'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(4, re.getAttributes().size());
	}

	@Test
	public void test_double_subquery() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT * FROM (SELECT * FROM oreda.pm_maint_items) AS child, (SELECT * FROM oreda.pm_program) AS parent  WHERE child.i_id=parent.i_id AND child.inst_id=parent.inst_id AND child.su_code=parent.su_code AND child.pm_interval=parent.pm_interval AND child.mc_code=parent.mc_code AND child.mac_code=parent.mac_code AND child.owner_id=parent.owner_id");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(7, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
	}

	@Test(expected = UnsupportedSelectQueryException.class) //due to IN with subselect
	public void test_IN() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
		RAExpression re = sqp.parse("SELECT * FROM oreda.pm_maint_items  WHERE (i_id,  pm_interval) IN (SELECT i_id, MAX(pm_interval) FROM oreda.pm_program GROUP BY i_id)");
		assertEquals(7, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().size());
	}

    @Test
    public void test_lower() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
        RAExpression re = sqp.parse("select id, name from student where lower(name)=lower('ColleeN')");
        assertEquals(1, re.getDataAtoms().size());
        assertEquals(1, re.getFilterAtoms().size());
        assertEquals(2, re.getAttributes().size());
    }

    @Test
    public void test_lower2() throws UnsupportedSelectQueryException, InvalidSelectQueryException {
        RAExpression re = sqp.parse("select id, lower(name) as lower_name from student");
        assertEquals(1, re.getDataAtoms().size());
        assertEquals(0, re.getFilterAtoms().size());
        assertEquals(2, re.getAttributes().size());
    }

}
