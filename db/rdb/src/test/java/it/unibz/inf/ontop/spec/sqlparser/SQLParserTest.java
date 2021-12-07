package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.DatabaseTableDefinition;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryException;
import net.sf.jsqlparser.JSQLParserException;
import org.junit.Before;
import org.junit.Test;


import static it.unibz.inf.ontop.spec.sqlparser.SQLTestingTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SQLParserTest {

	private SelectQueryParser sqp;

	@Before
	public void beforeEachTest() {
		OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
		QuotedIDFactory idfac = builder.getQuotedIDFactory();
		DBTypeFactory dbTypeFactory = builder.getDBTypeFactory();

		DBTermType integerDBType = dbTypeFactory.getDBLargeIntegerType();
		DBTermType booleanDBType = dbTypeFactory.getDBBooleanType();
		DBTermType dateDBType = dbTypeFactory.getDBDateType();
		DBTermType varchar20DBType = dbTypeFactory.getDBTermType("VARCHAR", 20);
		DBTermType varchar10DBType = dbTypeFactory.getDBTermType("VARCHAR", 10);
		DBTermType varchar8DBType = dbTypeFactory.getDBTermType("VARCHAR", 8);

		builder.createDatabaseRelation(createRelationIdWithDefaultSchema(idfac,"\"public\"", "student"), DatabaseTableDefinition.attributeListBuilder()
			.addAttribute(idfac.createAttributeID("id"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("name"), varchar20DBType, false)
			.addAttribute(idfac.createAttributeID("birth_year"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("birth_date"), dateDBType, false)
			.addAttribute(idfac.createAttributeID("semester"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("nationality"), varchar20DBType, false)
			.addAttribute(idfac.createAttributeID("grade"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("class"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("address"), varchar20DBType, false));

		builder.createDatabaseRelation( "QUEST_DATA_PROPERTY_LITERAL_ASSERTION",
			"URI", varchar20DBType, false,
			"ISBNODE", booleanDBType, false,
			"IDX", integerDBType, false,
			"VALUE", varchar20DBType, false,
			"LANG", varchar20DBType, false);

		builder.createDatabaseRelation("QUEST_OBJECT_PROPERTY_ASSERTION",
			"URI1", varchar20DBType, false,
			"URI2", varchar20DBType, false,
			"ISBNODE", booleanDBType, false,
			"ISBNODE2", booleanDBType, false,
			"IDX", integerDBType, false);

		builder.createDatabaseRelation("table1",
			"id", integerDBType, false,
			"name", varchar20DBType, false,
			"value", varchar20DBType, false);

		builder.createDatabaseRelation(createRelationIdWithDefaultSchema(idfac,"HR", "REGIONS"), DatabaseTableDefinition.attributeListBuilder()
			.addAttribute(idfac.createAttributeID("REGION_ID"), dbTypeFactory.getDBLargeIntegerType(), false));

		builder.createDatabaseRelation(ImmutableList.of(idfac.createRelationID("tableName")), DatabaseTableDefinition.attributeListBuilder()
			.addAttribute(idfac.createAttributeID("cast"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("do"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("extract"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("siblings"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("first"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("following"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("last"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("materialized"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("nulls"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("partition"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("range"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("row"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("rows"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("value"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("xml"), integerDBType, false));

		builder.createDatabaseRelation(ImmutableList.of(idfac.createRelationID("grade")), DatabaseTableDefinition.attributeListBuilder()
			.addAttribute(idfac.createAttributeID("st_id"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("class_id"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("grade"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("score"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("course"), varchar10DBType, false)
			.addAttribute(idfac.createAttributeID("mark"), varchar10DBType, false)
			.addAttribute(idfac.createAttributeID("pass"), booleanDBType, false)
			.addAttribute(idfac.createAttributeID("sm_id"), integerDBType, false));

		builder.createDatabaseRelation("semester",
			"id", integerDBType, false);

		builder.createDatabaseRelation("tax",
			"payee", varchar20DBType, false,
			"amount", integerDBType, false);

		builder.createDatabaseRelation("all_tables",
			"table_name", varchar20DBType, false,
			"owner", varchar20DBType, false);

		builder.createDatabaseRelation("all_views",
			"owner", varchar20DBType, false);

		builder.createDatabaseRelation("people",
			"\"id\"", varchar20DBType, false,
			"\"nick2\"", varchar20DBType, false);

		builder.createDatabaseRelation("pet",
			"name", varchar20DBType, false,
			"testcol", varchar20DBType, false);

		builder.createDatabaseRelation("despatch",
			"des_date", varchar20DBType, false,
			"des_amount", integerDBType, false,
			"ord_amount", integerDBType, false);

		builder.createDatabaseRelation("Product",
			"maker", varchar20DBType, false,
			"type", varchar20DBType, false,
			"model", varchar20DBType, false);

		builder.createDatabaseRelation("PC",
			"model", varchar20DBType, false);

		builder.createDatabaseRelation(createRelationIdWithDefaultSchema(idfac,"\"CIM\"", "\"dbo\"", "TEMPERATURE_DEVIATION"), DatabaseTableDefinition.attributeListBuilder()
			.addAttribute(idfac.createAttributeID("ID"), varchar20DBType, false)
			.addAttribute(idfac.createAttributeID("DATETIME"), dbTypeFactory.getDBDateTimestampType(), false)
			.addAttribute(idfac.createAttributeID("SCALE"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("INTERVAL"), dbTypeFactory.getDBDateTimestampType(), false));

		builder.createDatabaseRelation(createRelationIdWithDefaultSchema(idfac,"northwind", "Suppliers"), DatabaseTableDefinition.attributeListBuilder()
			.addAttribute(idfac.createAttributeID("Region"), varchar20DBType, false)
			.addAttribute(idfac.createAttributeID("City"), varchar20DBType, false)
			.addAttribute(idfac.createAttributeID("PostalCode"), varchar20DBType, false)
			.addAttribute(idfac.createAttributeID("Address"), varchar20DBType, false)
			.addAttribute(idfac.createAttributeID("Country"), varchar20DBType, false));

		builder.createDatabaseRelation(createRelationIdWithDefaultSchema(idfac,"oreda", "pm_maint_items"), DatabaseTableDefinition.attributeListBuilder()
			.addAttribute(idfac.createAttributeID("owner_id"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("inst_id"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("i_id"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("ec_code"), varchar20DBType, false)
			.addAttribute(idfac.createAttributeID("mi_code"), varchar10DBType, false)
			.addAttribute(idfac.createAttributeID("su_code"), varchar10DBType, false)
			.addAttribute(idfac.createAttributeID("mc_code"), varchar8DBType, false)
			.addAttribute(idfac.createAttributeID("mac_code"), varchar8DBType, false)
			.addAttribute(idfac.createAttributeID("pm_interval"), integerDBType, false));

		builder.createDatabaseRelation(createRelationIdWithDefaultSchema(idfac, "oreda", "pm_program"), DatabaseTableDefinition.attributeListBuilder()
			.addAttribute(idfac.createAttributeID("owner_id"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("inst_id"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("i_id"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("ec_code"), varchar20DBType, false)
			.addAttribute(idfac.createAttributeID("su_code"), varchar10DBType, false)
			.addAttribute(idfac.createAttributeID("mc_code"), varchar8DBType, false)
			.addAttribute(idfac.createAttributeID("mac_code"), varchar8DBType, false)
			.addAttribute(idfac.createAttributeID("pm_interval"), integerDBType, false));

		MetadataLookup metadataLookup = builder.build();
		sqp = new SelectQueryParser(metadataLookup, CORE_SINGLETONS);
	}

	private ImmutableList<RelationID> createRelationIdWithDefaultSchema(QuotedIDFactory idfac, String schema, String table) {
		return ImmutableList.of(idfac.createRelationID(table), idfac.createRelationID(schema, table));
	}

	private ImmutableList<RelationID> createRelationIdWithDefaultSchema(QuotedIDFactory idfac, String catalog, String schema, String table) {
		return ImmutableList.of(idfac.createRelationID(table), idfac.createRelationID(catalog, schema, table));
	}

	private RAExpression parse(String sql) throws InvalidQueryException, UnsupportedSelectQueryException {
		try {
			RAExpression rae = sqp.parse(JSqlParserTools.parse(sql));
			System.out.println(rae);
			return rae;
		}
		catch (JSQLParserException e) {
			throw new InvalidQueryException(e.getCause().getMessage(), sql);
		}
	}

	@Test
	public void test_1_1_1() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT * FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(9, re.getAttributes().asMap().size());
	}

	@Test
	public void test_1_1_2() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT student.* FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(9, re.getAttributes().asMap().size());
	}

	@Test
	public void test_1_2_1() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
	}

	@Test
	public void test_1_2_2() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id, name FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to DISTINCT
	public void test_1_3_1() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT DISTINCT name FROM student");
	}

	// @Test
	// the SQL is not valid
	// yet JSQLParser accepts it in the form of SELECT name FROM student
	public void test_1_3_2() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT ALL name FROM student");
	}

	@Test(expected = UnsupportedSelectQueryException.class)
	// due to DISTINCT ON (PostgreSQL-specific) is not supported
	public void test_1_3_3() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("select DISTINCT ON (name,age,year) name,age FROM student");
	}

	@Test
	public void test_1_4() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT student.id FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
	}

	@Test
	public void test_1_5() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT student.id, student.name FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test
	public void test_1_5_extra() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT \"URI\" as X, VALUE as Y, LANG as Z\n" +
				"FROM QUEST_DATA_PROPERTY_LITERAL_ASSERTION\n" +
				"WHERE ISBNODE = FALSE AND LANG IS NULL AND IDX = 1");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(3, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().asMap().size());
	}

	@Test
	public void test_1_5_extra_2() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id, name as alias1, value as alias2 FROM table1");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().asMap().size());
	}

	@Test
	public void test_1_5_extra_3() throws UnsupportedSelectQueryException, InvalidQueryException {
		// to_char (Oracle specific cast)
		RAExpression re = parse("select to_char(REGION_ID) as RID FROM HR.REGIONS");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
	}

	@Test
	public void test_1_5_extra_4() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT \"URI1\" as X, \"URI2\" as Y\n" +
				"FROM QUEST_OBJECT_PROPERTY_ASSERTION\n" +
				"WHERE ISBNODE = FALSE AND ISBNODE2 = FALSE AND IDX = 2");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(3, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test
	// all of these are legal column names in SQL server;
	// 'row' and 'rows' are not legal on Oracle;
	public void test_1_5_extra_5() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT cast, do, extract, first, following,\n" +
				"last, materialized, nulls, partition, range,\n" +
				"row, rows, siblings, value, xml\n" +
				"FROM tableName");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(15, re.getAttributes().asMap().size());
	}

	@Test
	public void test_1_6_1() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT undergraduate.* FROM student as undergraduate");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(9, re.getAttributes().asMap().size());
	}

	@Test
	public void test_1_6_2() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT undergraduate.id FROM student as undergraduate");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
	}

	@Test
	public void test_1_7() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT alias.id, alias.name FROM student as alias");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test(expected = InvalidQueryException.class) // alias does not exist
	public void test_1_7_1() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT alias.id, alias.name FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test
	public void test_1_8() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id FROM \"STUDENT\"");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
	}

	@Test
	public void test_1_9() throws UnsupportedSelectQueryException, InvalidQueryException {
		// ROMAN (Feb 2017): changed to upper-case STUDENT (otherwise invalid)
		RAExpression re = parse("SELECT id FROM \"public\".\"STUDENT\"");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
	}

	@Test
	public void test_1_10() throws UnsupportedSelectQueryException, InvalidQueryException {
		// ROMAN (Feb 2017): changed t2 to t1 (otherwise invalid)
		RAExpression re = parse("SELECT t1.id, t1.name FROM \"public\".\"STUDENT\" as t1");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test
	public void test_2_1() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id FROM student WHERE id=1");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
	}

	@Test
	public void test_2_2() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id, name FROM student WHERE id=1 AND name='John'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test
	public void test_2_3() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id, name, semester, birth_year, nationality "
				+ "FROM student "
				+ "WHERE name<>'John' AND semester>2 AND semester<7 AND "
				+ "birth_year>=1984 AND birth_year<=1990 AND "
				+ "(nationality='IT' OR nationality='DE')");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(6, re.getFilterAtoms().size()); // OR has precedence over AND
		assertEquals(5, re.getAttributes().asMap().size());
	}

	@Test
	public void test_2_4() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT graduate.id, graduate.name FROM student as graduate WHERE graduate.name<>'John'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test
	public void test_2_5() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id, name, grade FROM student WHERE grade is null");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().asMap().size());
	}

	@Test
	public void test_2_6() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id, name, grade FROM student WHERE grade is not null");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().asMap().size());
	}

	@Test
	public void test_2_7() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id, name, grade FROM student WHERE grade is null AND name<>'John'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().asMap().size());
	}

	@Test
	public void test_2_8() throws UnsupportedSelectQueryException, InvalidQueryException {
		// ROMAN (Feb 2017): changed to upper-case STUDENT (otherwise invalid)
		RAExpression re = parse("SELECT id, name FROM \"public\".\"STUDENT\" WHERE name<>'John'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test
	public void test_2_9() throws UnsupportedSelectQueryException, InvalidQueryException {
		// ROMAN (Feb 2017): changed to upper-case STUDENT (otherwise invalid)
		RAExpression re = parse("SELECT t1.id, t1.name FROM \"public\".\"STUDENT\" as t1 "
				+ "WHERE t1.name<>'John'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test
	public void test_2_10() throws UnsupportedSelectQueryException, InvalidQueryException {
		// ROMAN (Feb 2017): changed to upper-case STUDENT (otherwise invalid)
		RAExpression re = parse("SELECT t1.id, t1.name, t1.grade FROM \"public\".\"STUDENT\" as t1 "
				+ "WHERE t1.grade is not null AND t1.name<>'John'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().asMap().size());
	}

	@Test
	public void test_2_11() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id, name FROM student WHERE class IN (7, 8, 9)");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test
	public void test_2_12() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id, name, grade FROM student WHERE name IN ('John', 'Jack', 'Clara')");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().asMap().size());
	}

	@Test(expected = UnsupportedSelectQueryException.class) // aggregation is not supported
	public void max_test() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT MAX(score) AS max_score FROM grade");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // aggregation is not supported
	public void min_test() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT MIN(score) AS min_score FROM grade");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // aggregation is not supported
	public void avg_test() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT AVG(score) AS avg_score FROM grade");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // aggregation is not supported
	public void sum_test() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT SUM(amount) AS total_amount FROM tax");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // aggregation is not supported
	public void count_star_test() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT COUNT(*) AS student_count FROM student");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // aggregation is not supported
	public void count_test() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT COUNT(id) AS student_count FROM student");
	}

	//@Test(expected = UnsupportedSelectQueryException.class)
	// SQL:1999 aggregation not supported by JSQLParser
	// see https://blog.jooq.org/2014/12/18/a-true-sql-gem-you-didnt-know-yet-the-every-aggregate-function/
	public void every_test() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT EVERY(id < 10) AS student_id FROM student");
	}

	// @Test(expected = UnsupportedSelectQueryException.class)
	public void any_test() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT ANY(id < 10) AS student_id FROM student");
	}

	// @Test(expected = UnsupportedSelectQueryException.class)
	public void some_test() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT SOME(id < 10) AS student_id FROM student");
	}


	@Test(expected = UnsupportedSelectQueryException.class) // due to DISTINCT
	public void test_3_8_1() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT DISTINCT maker FROM Product "
				+ "WHERE type = 'PC' AND NOT model = ANY (SELECT model FROM PC)");
	}


	@Test(expected = UnsupportedSelectQueryException.class) // due to DISTINCT
	public void test_3_9_1() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT DISTINCT maker FROM Product "
				+ "WHERE type = 'PC' AND NOT model = SOME (SELECT model FROM PC)");
	}

	@Test(expected = UnsupportedSelectQueryException.class)
	public void test_4_1() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT nationality, COUNT(id) as num_nat FROM student GROUP BY nationality");
	}

	@Test(expected = UnsupportedSelectQueryException.class)
	public void test_4_2() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT nationality, COUNT(id) num_nat FROM student WHERE birth_year>2000 GROUP BY nationality");
	}

	@Test
	public void test_4_3() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT name as student_name, address as student_address FROM student WHERE id >= 66 AND id <= 69");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to ALL
	public void test_4_4() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT des_date,des_amount,ord_amount FROM despatch WHERE des_amount > ALL("
				+ "SELECT ord_amount FROM orders WHERE ord_amount=2000)");
	}

	@Test
	public void test_5_1() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 JOIN grade t2 ON t1.id=t2.st_id");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(4, re.getAttributes().asMap().size());
	}

	@Test
	public void test_5_1_1() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT t1.id as sid, t1.name as fullname FROM student t1 JOIN grade t2 ON t1.id=t2.st_id AND t2.mark='A'");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test
	public void test_5_1_2() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT t1.id, name FROM student t1 JOIN grade t2 ON t1.id=t2.st_id AND t2.\"SCORE\">=25");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test
	public void test_5_1_3() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT t1.id, name FROM student t1 JOIN grade t2 ON t1.id=t2.st_id AND t2.pass=true");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test
	public void test_5_2() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 INNER JOIN grade t2 ON t1.id=t2.st_id");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(4, re.getAttributes().asMap().size());
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to LEFT JOIN
	public void test_5_3() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 LEFT JOIN grade t2 ON t1.id=t2.st_id");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to RIGHT JOIN
	public void test_5_4() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 RIGHT JOIN grade t2 ON t1.id=t2.st_id");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to FULL JOIN
	public void test_5_5() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id, name, class_id, grade FROM student t1 FULL JOIN grade t2 ON t1.id=t2.st_id");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to LEFT JOIN
	public void test_5_6() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id, name, class_id, grade FROM student t1 LEFT OUTER JOIN grade t2 ON t1.id=t2.st_id");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to RIGTH JOIN
	public void test_5_7() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id, name, class_id, grade FROM student t1 RIGHT OUTER JOIN grade t2 ON t1.id=t2.st_id");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to FULL JOIN
	public void test_5_8() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id, name, class_id, grade FROM student t1 FULL OUTER JOIN grade t2 ON t1.id=t2.st_id");
	}

	@Test
	public void test_5_9() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT t1.id, name, score FROM student t1 JOIN grade t2 ON t1.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id");
		assertEquals(3, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().asMap().size());
	}

	@Test
	public void test_5_10() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT t1.id, t1.name, t2.score FROM (SELECT id, name FROM student WHERE student.name='John') AS t1 JOIN grade as t2 ON t1.id=t2.st_id");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().asMap().size());
	}

	// TODO: check the intention
	public void test_5_11() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id, name, score FROM student JOIN grade USING (id)");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().asMap().size());
	}

	@Test
	public void test_6_1() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT t1.id, t1.name, t2.grade FROM (SELECT id, name FROM student) t1, (SELECT st_id as sid, grade FROM grade) t2 WHERE t1.id=t2.sid");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().asMap().size());
	}

	@Test
	public void test_6_2() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT * FROM (SELECT id, name, score FROM student JOIN grade ON student.id=grade.st_id) t1");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().asMap().size());
	}

	@Test
	public void test_6_3() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT * FROM (SELECT id, name, score FROM student JOIN grade ON student.id=grade.st_id) t1 WHERE t1.score>=25");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().asMap().size());
	}

	@Test
	public void test_7_1() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT ('ID-' || student.id) as sid FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
		assertFalse(re.getAttributes().asMap().keySet().iterator().next() instanceof Variable);
	}

	@Test
	public void test_7_1_b() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT CONCAT('ID-', student.id, 'b') as sid FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
		assertFalse(re.getAttributes().asMap().keySet().iterator().next() instanceof Variable);
	}


	@Test
	public void test_7_2() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT (grade.score * 30 / 100) as percentage from grade");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
		assertFalse(re.getAttributes().asMap().keySet().iterator().next() instanceof Variable);
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to UNION ALL
	public void test_8_1() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT name FROM student UNION ALL SELECT name FROM erasmus");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to UNION ALL and UNION
	public void test_8_2() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT name FROM student UNION ALL SELECT name FROM erasmus UNION SELECT DISTINCT payee FROM tax");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to UNION ALL
	public void test_8_3() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT name FROM student WHERE id = 20 UNION ALL SELECT name FROM erasmus WHERE id = 20");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to UNION
	public void test_8_4() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT name FROM student JOIN grade on student.id=grade.st_id AND grade.score>=25 UNION SELECT name FROM erasmus");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to UNION ALL
	public void test_8_5() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id, name, course, score, semester FROM student t1 JOIN grade t2 ON t1.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id "
				+ "UNION ALL SELECT id, name, course, score, semester FROM erasmus t4 JOIN grade t2 ON t4.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id");
	}

	@Test
	public void test_9_1() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id, name, address from student where name = 'John'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().asMap().size());
	}

	@Test
	public void test_9_2() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id, name, address from student where id = 20");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().asMap().size());
	}

	@Test
	public void test_9_3() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT payee, amount from tax where amount = 12.345");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test
	public void test_9_4_1() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id, name, address from student where birth_date = '1984-01-22 00:02:01.234'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().asMap().size());
	}

	@Test
	public void test_9_4_2() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id, name, address from student where birth_date = '1984-01-22 00:02:01'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().asMap().size());
	}

	@Test
	public void test_9_4_3() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT id, name, address from student where birth_date = '1984-01-22'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().asMap().size());
	}

	@Test
	public void test_9_5() throws UnsupportedSelectQueryException, InvalidQueryException {
		// ROMAN (Feb 2017): changed passed to pass
		RAExpression re = parse("SELECT st_id, course, score from grade where pass = TRUE");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(3, re.getAttributes().asMap().size());
	}

	@Test
	public void test_10_1() throws UnsupportedSelectQueryException, InvalidQueryException {
		// ROMAN (Feb 2017): changed passed to pass
		RAExpression re = parse("SELECT name from grade, student where pass = TRUE AND course = 'CS001' AND ( (score = 8 AND mark = 'B') OR (score = 7 AND mark = 'C') OR (score >= 9 AND mark = 'A') )");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(3, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
	}

	@Test
	public void test_10_2() throws UnsupportedSelectQueryException, InvalidQueryException {
		// ROMAN (Feb 2017): changed passed to pass
		RAExpression re = parse("SELECT name from grade, student where pass = FALSE AND ( course = 'CS001' OR ( (score = 6 AND mark = 'D') OR (score <= 5 AND mark = 'E') ) )");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
	}

	@Test
	public void test_11() throws UnsupportedSelectQueryException, InvalidQueryException {
		// ROMAN (Feb 2017): changed passed to pass
		RAExpression re = parse("SELECT \"NAME\" from grade, student where pass = FALSE AND ( \"COURSE\" = 'CS001' OR ( (score = 6 AND mark = 'D') OR (score <= 5 AND mark = 'E') ) )");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
	}

	@Test
	public void test_11_1() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("select t1.owner NAME from all_tables t1, all_tables t2, ALL_VIEWS where t1.table_name = t2.table_name and t1.owner = t2.owner and t1.owner = ALL_VIEWS.OWNER");
		assertEquals(3, re.getDataAtoms().size());
		assertEquals(3, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
	}

	@Test
	public void test_12() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("select name from grade, student where score BETWEEN 6 AND 8");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(2, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
	}

	@Test
	public void test_13() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("select REGEXP_REPLACE(name, ' +', ' ') as reg from student ");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
		assertFalse(re.getAttributes().asMap().keySet().iterator().next() instanceof Variable);
	}


	@Test(expected = UnsupportedSelectQueryException.class) // due to DISTINCT
	public void testUnquoted0() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT DISTINCT 3 AS \"v0QuestType\", NULL AS \"v0Lang\", CAST(\"QpeopleVIEW0\".\"nick2\" AS CHAR) AS \"v0\", 1 AS \"v1QuestType\", NULL AS \"v1Lang\", QpeopleVIEW0.id AS \"v1\""
				+ "FROM people \"QpeopleVIEW0\" "
				+ "WHERE \"QpeopleVIEW0\".\"id\" IS NOT NULL AND \"QpeopleVIEW0\".\"nick2\" IS NOT NULL");
	}

	@Test
	public void testUnquoted1() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT 3 AS \"v0QuestType\", NULL AS \"v0Lang\", CAST(\"QpeopleVIEW0\".\"nick2\" AS CHAR) AS \"v0\", 1 AS \"v1QuestType\", NULL AS \"v1Lang\", \"QpeopleVIEW0\".\"id\" AS \"v1\""
				+ "FROM people \"QpeopleVIEW0\" "
				+ "WHERE \"QpeopleVIEW0\".\"id\" IS NOT NULL AND \"QpeopleVIEW0\".\"nick2\" IS NOT NULL");
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to DISTINCT
	// Does not parse SELECT DISTINCT (on purpose)
	public void testUnquoted2() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT DISTINCT 3 AS \"v0QuestType\", NULL AS \"v0Lang\", CAST(\"QpeopleVIEW0\".\"nick2\" AS CHAR) AS \"v0\", 1 AS \"v1QuestType\", NULL AS \"v1Lang\", QpeopleVIEW0.id AS \"v1\""
				+ "FROM people \"QpeopleVIEW0\" "
				+ "WHERE \"QpeopleVIEW0\".\"id\" IS NOT NULL AND \"QpeopleVIEW0\".\"nick2\" IS NOT NULL");
	}

	@Test
	public void testCast1() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT CAST(\"view0\".\"nick2\" AS CHAR (8000) CHARACTER SET utf8) AS \"v0\" FROM people \"view0\" WHERE \"view0\".\"nick2\" IS NOT NULL");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
	}

	@Test(expected = UnsupportedSelectQueryException.class) // due to DISTINCT
	public void testCast2() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT DISTINCT CAST(`view0`.`nick2` AS CHAR (8000) CHARACTER SET utf8) AS `v0` FROM people `view0` WHERE `view0`.`nick2` IS NOT NULL");
	}

	/* Regex in MySQL, Oracle and Postgres*/

	@Test
	public void testRegexMySQL() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT * FROM pet WHERE name REGEXP '^b'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test
	public void testRegexBinaryMySQL() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT * FROM pet WHERE name REGEXP BINARY '^b'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test
	public void testRegexPostgres() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT * FROM pet WHERE name ~ 'foo'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test
	public void testRegexPostgresSimilarTo() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT * FROM pet WHERE 'abc' SIMILAR TO 'abc'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test
	public void testRegexOracle() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT * FROM pet WHERE REGEXP_LIKE(testcol, '[[:alpha:]]')");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test
	public void testRegexNotOracle() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT * FROM pet WHERE NOT REGEXP_LIKE(testcol, '[[:alpha:]]')");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test
	public void testRegexNotOracle_1() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT * FROM pet WHERE NOT (REGEXP_LIKE(testcol, '[[:alpha:]]'))");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	// TODO: expand
	@Test //(expected = UnsupportedSelectQueryException.class) // due to COALESCE
	public void test_md5() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT MD5(CONCAT(COALESCE(Address, RAND()), COALESCE(City, RAND()),\n" +
				"COALESCE(Region, RAND()), COALESCE(PostalCode, RAND()), COALESCE(Country,\n" +
				"RAND()) )) AS locationID FROM northwind.Suppliers");
	}

	@Test
	public void test_concatOracle() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT ('ID-' || student.id || 'type1') \"sid\" FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
		assertFalse(re.getAttributes().asMap().keySet().iterator().next() instanceof Variable);
	}

	@Test
	public void test_RegexpReplace() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT REGEXP_REPLACE('Hello World', ' +', ' ') as reg FROM student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
		assertFalse(re.getAttributes().asMap().keySet().iterator().next() instanceof Variable);
	}

	@Test(expected = UnsupportedSelectQueryException.class)
	// due to CONVERT(varchar(50), ...), where varchar(50) is treated as a function calls
	public void test_2_p() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT \"ID\" as \"KEYID\"\n" +
				"      ,CONVERT(varchar(50), \"DATETIME\", 0) as \"DATETIMEH\"\n" +
				"      ,\"SCALE\" as \"SCALE\"\n" +
				"      ,\"INTERVAL\" as \"TEMPINTERVAL\"\n" +
				"  FROM \"CIM\".\"dbo\".\"TEMPERATURE_DEVIATION\" where \"INTERVAL\" = '0-10'");
	}

	@Test
	public void test_2() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT \"ID\"\n" +
				"      ,\"DATETIME\"\n" +
				"      ,\"SCALE\"\n" +
				"      ,\"INTERVAL\"\n" +
				"  FROM \"CIM\".\"dbo\".\"TEMPERATURE_DEVIATION\" where \"INTERVAL\" = '0-10'");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(1, re.getFilterAtoms().size());
		assertEquals(4, re.getAttributes().asMap().size());
	}

	@Test
	public void test_double_subquery() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT * FROM (SELECT * FROM oreda.pm_maint_items) AS child, (SELECT * FROM oreda.pm_program) AS parent  WHERE child.i_id=parent.i_id AND child.inst_id=parent.inst_id AND child.su_code=parent.su_code AND child.pm_interval=parent.pm_interval AND child.mc_code=parent.mc_code AND child.mac_code=parent.mac_code AND child.owner_id=parent.owner_id");
		assertEquals(2, re.getDataAtoms().size());
		assertEquals(7, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
	}

	@Test(expected = UnsupportedSelectQueryException.class) //due to IN with subselect
	public void test_IN() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("SELECT * FROM oreda.pm_maint_items  WHERE (i_id,  pm_interval) IN (SELECT i_id, MAX(pm_interval) FROM oreda.pm_program GROUP BY i_id)");
		assertEquals(7, re.getFilterAtoms().size());
		assertEquals(1, re.getAttributes().asMap().size());
	}

    @Test
    public void test_lower() throws UnsupportedSelectQueryException, InvalidQueryException {
        RAExpression re = parse("select id, name from student where lower(name)=lower('ColleeN')");
        assertEquals(1, re.getDataAtoms().size());
        assertEquals(1, re.getFilterAtoms().size());
        assertEquals(2, re.getAttributes().asMap().size());
    }

    @Test
    public void test_lower2() throws UnsupportedSelectQueryException, InvalidQueryException {
        RAExpression re = parse("select id, lower(name) as lower_name from student");
        assertEquals(1, re.getDataAtoms().size());
        assertEquals(0, re.getFilterAtoms().size());
        assertEquals(2, re.getAttributes().asMap().size());
    }

	@Test // issue 157
	public void test_locate() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("select id, locate('A', name, 2) as pos from student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test // issue 157
	public void test_position() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("select id, position('A', name) as pos from student");
		assertEquals(1, re.getDataAtoms().size());
		assertEquals(0, re.getFilterAtoms().size());
		assertEquals(2, re.getAttributes().asMap().size());
	}

	@Test(expected = UnsupportedSelectQueryException.class) // issue 184
	public void test_limit() throws UnsupportedSelectQueryException, InvalidQueryException {
		RAExpression re = parse("select STUDY_ID, patient_name(STUDY_ID) as label from demographics order by STUDY_ID limit 50");
	}

}
