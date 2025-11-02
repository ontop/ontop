package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.DatabaseTableDefinition;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.spec.sqlparser.exception.QueryParseException;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.*;
import static it.unibz.inf.ontop.spec.sqlparser.SQLTestingTools.*;
import static org.junit.jupiter.api.Assertions.*;

public class SQLParserTest {

	private SelectQueryParser sqp;
    private QuotedIDFactory idfac;
    private DBTypeFactory dbTypeFactory;

    private NamedRelationDefinition student, pet, grade, data_property, object_property, name_with_many_many_so_many_components,
            name_with_dots_many_many_so_many_components, schema_with_dots_table_with_dots, table_with_dots, tableName, tax, semester,
            table1, regions, temperature_deviation, suppliers, people, all_tables, all_views, oreda_items, oreda_program;

    @BeforeEach
	public void beforeEachTest() {
		OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
		idfac = builder.getQuotedIDFactory();
		dbTypeFactory = builder.getDBTypeFactory();

		DBTermType integerDBType = dbTypeFactory.getDBLargeIntegerType();
		DBTermType booleanDBType = dbTypeFactory.getDBBooleanType();
		DBTermType dateDBType = dbTypeFactory.getDBDateType();
		DBTermType varchar20DBType = dbTypeFactory.getDBTermType("VARCHAR", 20);
		DBTermType varchar10DBType = dbTypeFactory.getDBTermType("VARCHAR", 10);
		DBTermType varchar8DBType = dbTypeFactory.getDBTermType("VARCHAR", 8);

		student = builder.createDatabaseRelation(createRelationIdWithDefaultSchema(idfac,"\"public\"", "student"), DatabaseTableDefinition.attributeListBuilder()
			.addAttribute(idfac.createAttributeID("id"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("name"), varchar20DBType, false)
			.addAttribute(idfac.createAttributeID("birth_year"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("birth_date"), dateDBType, false)
			.addAttribute(idfac.createAttributeID("semester"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("nationality"), varchar20DBType, false)
			.addAttribute(idfac.createAttributeID("grade"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("class"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("address"), varchar20DBType, false));

		data_property = builder.createDatabaseRelation( "QUEST_DATA_PROPERTY_LITERAL_ASSERTION",
			"URI", varchar20DBType, false,
			"ISBNODE", booleanDBType, false,
			"IDX", integerDBType, false,
			"VALUE", varchar20DBType, false,
			"LANG", varchar20DBType, false);

		object_property = builder.createDatabaseRelation("QUEST_OBJECT_PROPERTY_ASSERTION",
			"URI1", varchar20DBType, false,
			"URI2", varchar20DBType, false,
			"ISBNODE", booleanDBType, false,
			"ISBNODE2", booleanDBType, false,
			"IDX", integerDBType, false);

		table1 = builder.createDatabaseRelation("table1",
			"id", integerDBType, false,
			"name", varchar20DBType, false,
			"value", varchar20DBType, false);

		regions = builder.createDatabaseRelation(createRelationIdWithDefaultSchema(idfac,"HR", "REGIONS"), DatabaseTableDefinition.attributeListBuilder()
			.addAttribute(idfac.createAttributeID("REGION_ID"), dbTypeFactory.getDBLargeIntegerType(), false));

		tableName = builder.createDatabaseRelation(ImmutableList.of(idfac.createRelationID("tableName")), DatabaseTableDefinition.attributeListBuilder()
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

		grade = builder.createDatabaseRelation(ImmutableList.of(idfac.createRelationID("grade")), DatabaseTableDefinition.attributeListBuilder()
			.addAttribute(idfac.createAttributeID("st_id"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("class_id"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("grade"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("score"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("course"), varchar10DBType, false)
			.addAttribute(idfac.createAttributeID("mark"), varchar10DBType, false)
			.addAttribute(idfac.createAttributeID("pass"), booleanDBType, false)
			.addAttribute(idfac.createAttributeID("sm_id"), integerDBType, false));

		semester = builder.createDatabaseRelation("semester",
			"id", integerDBType, false);

		tax = builder.createDatabaseRelation("tax",
			"payee", varchar20DBType, false,
			"amount", integerDBType, false);

		all_tables = builder.createDatabaseRelation("all_tables",
			"table_name", varchar20DBType, false,
			"owner", varchar20DBType, false);

		all_views = builder.createDatabaseRelation("all_views",
			"owner", varchar20DBType, false);

		people = builder.createDatabaseRelation("people",
			"\"id\"", varchar20DBType, false,
			"\"nick2\"", varchar20DBType, false);

		pet = builder.createDatabaseRelation("pet",
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

		temperature_deviation = builder.createDatabaseRelation(createRelationIdWithDefaultSchema(idfac,"\"CIM\"", "\"dbo\"", "TEMPERATURE_DEVIATION"), DatabaseTableDefinition.attributeListBuilder()
			.addAttribute(idfac.createAttributeID("ID"), varchar20DBType, false)
			.addAttribute(idfac.createAttributeID("DATETIME"), dbTypeFactory.getDBDateTimestampType(), false)
			.addAttribute(idfac.createAttributeID("SCALE"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("INTERVAL"), dbTypeFactory.getDBDateTimestampType(), false));

		suppliers = builder.createDatabaseRelation(createRelationIdWithDefaultSchema(idfac,"northwind", "Suppliers"), DatabaseTableDefinition.attributeListBuilder()
			.addAttribute(idfac.createAttributeID("Region"), varchar20DBType, false)
			.addAttribute(idfac.createAttributeID("City"), varchar20DBType, false)
			.addAttribute(idfac.createAttributeID("PostalCode"), varchar20DBType, false)
			.addAttribute(idfac.createAttributeID("Address"), varchar20DBType, false)
			.addAttribute(idfac.createAttributeID("Country"), varchar20DBType, false));

		oreda_items = builder.createDatabaseRelation(createRelationIdWithDefaultSchema(idfac,"oreda", "pm_maint_items"), DatabaseTableDefinition.attributeListBuilder()
			.addAttribute(idfac.createAttributeID("owner_id"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("inst_id"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("i_id"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("ec_code"), varchar20DBType, false)
			.addAttribute(idfac.createAttributeID("mi_code"), varchar10DBType, false)
			.addAttribute(idfac.createAttributeID("su_code"), varchar10DBType, false)
			.addAttribute(idfac.createAttributeID("mc_code"), varchar8DBType, false)
			.addAttribute(idfac.createAttributeID("mac_code"), varchar8DBType, false)
			.addAttribute(idfac.createAttributeID("pm_interval"), integerDBType, false));

		oreda_program = builder.createDatabaseRelation(createRelationIdWithDefaultSchema(idfac, "oreda", "pm_program"), DatabaseTableDefinition.attributeListBuilder()
			.addAttribute(idfac.createAttributeID("owner_id"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("inst_id"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("i_id"), integerDBType, false)
			.addAttribute(idfac.createAttributeID("ec_code"), varchar20DBType, false)
			.addAttribute(idfac.createAttributeID("su_code"), varchar10DBType, false)
			.addAttribute(idfac.createAttributeID("mc_code"), varchar8DBType, false)
			.addAttribute(idfac.createAttributeID("mac_code"), varchar8DBType, false)
			.addAttribute(idfac.createAttributeID("pm_interval"), integerDBType, false));

		table_with_dots = builder.createDatabaseRelation("\"table.with.dots\"",
				"id", varchar20DBType, false);

		schema_with_dots_table_with_dots = builder.createDatabaseRelation(ImmutableList.of(idfac.createRelationID("schema.with.dots", "\"table.with.dots\"")), DatabaseTableDefinition.attributeListBuilder()
				.addAttribute(idfac.createAttributeID("id"), varchar20DBType, false));

		name_with_many_many_so_many_components = builder.createDatabaseRelation(ImmutableList.of(idfac.createRelationID("name", "many", "many", "so", "many", "components")), DatabaseTableDefinition.attributeListBuilder()
				.addAttribute(idfac.createAttributeID("id"), varchar20DBType, false));

		name_with_dots_many_many_so_many_components = builder.createDatabaseRelation(ImmutableList.of(idfac.createRelationID("\"name.with.dots\"", "many", "many", "\"so.so\"", "many", "components")), DatabaseTableDefinition.attributeListBuilder()
				.addAttribute(idfac.createAttributeID("id"), varchar20DBType, false));

		MetadataLookup metadataLookup = builder.build();
		sqp = new SelectQueryParser(metadataLookup, CORE_SINGLETONS);
	}

	private ImmutableList<RelationID> createRelationIdWithDefaultSchema(QuotedIDFactory idfac, String schema, String table) {
		return ImmutableList.of(idfac.createRelationID(table), idfac.createRelationID(schema, table));
	}

	private ImmutableList<RelationID> createRelationIdWithDefaultSchema(QuotedIDFactory idfac, String catalog, String schema, String table) {
		return ImmutableList.of(idfac.createRelationID(table), idfac.createRelationID(catalog, schema, table));
	}

	private RAExpression parse(String sql) throws QueryParseException, InvalidQueryException, UnsupportedSelectQueryException {
		RAExpression rae = sqp.parse(sql);
		System.out.println(rae);
		return rae;
	}

    private ExtensionalDataNode students() {
        return students(1);
    }

    private ExtensionalDataNode students(int idx) {
        return IQ_FACTORY.createExtensionalDataNode(student, ImmutableMap.of(
                0, TERM_FACTORY.getVariable("ID" + idx),
                1, TERM_FACTORY.getVariable("NAME" + idx),
                2, TERM_FACTORY.getVariable("BIRTH_YEAR" + idx),
                3, TERM_FACTORY.getVariable("BIRTH_DATE" + idx),
                4, TERM_FACTORY.getVariable("SEMESTER" + idx),
                5, TERM_FACTORY.getVariable("NATIONALITY" + idx),
                6, TERM_FACTORY.getVariable("GRADE" + idx),
                7, TERM_FACTORY.getVariable("CLASS" + idx),
                8, TERM_FACTORY.getVariable("ADDRESS" + idx)));
    }

    private ExtensionalDataNode pets() {
        return IQ_FACTORY.createExtensionalDataNode(pet, ImmutableMap.of(
                0, TERM_FACTORY.getVariable("NAME1"),
                1, TERM_FACTORY.getVariable("TESTCOL1")));
    }

    private ExtensionalDataNode grades() {
        return grades(1);
    }
    private ExtensionalDataNode grades(int idx) {
        return IQ_FACTORY.createExtensionalDataNode(grade, ImmutableMap.of(
                0, TERM_FACTORY.getVariable("ST_ID" + idx),
                1, TERM_FACTORY.getVariable("CLASS_ID" + idx),
                2, TERM_FACTORY.getVariable("GRADE" + idx),
                3, TERM_FACTORY.getVariable("SCORE" + idx),
                4, TERM_FACTORY.getVariable("COURSE" + idx),
                5, TERM_FACTORY.getVariable("MARK" + idx),
                6, TERM_FACTORY.getVariable("PASS" + idx),
                7, TERM_FACTORY.getVariable("SM_ID" + idx)));
    }

    private ExtensionalDataNode people() {
        return IQ_FACTORY.createExtensionalDataNode(people, ImmutableMap.of(
                0, TERM_FACTORY.getVariable("id1"),
                1, TERM_FACTORY.getVariable("nick21")));
    }

    private ExtensionalDataNode all_tables(int idx) {
        return IQ_FACTORY.createExtensionalDataNode(all_tables, ImmutableMap.of(
                0, TERM_FACTORY.getVariable("TABLE_NAME" + idx),
                1, TERM_FACTORY.getVariable("OWNER" + idx)));
    }

    private ExtensionalDataNode all_views(int idx) {
        return IQ_FACTORY.createExtensionalDataNode(all_views, ImmutableMap.of(
                0, TERM_FACTORY.getVariable("OWNER" + idx)));
    }

    private ExtensionalDataNode oreda_items(int idx) {
        return IQ_FACTORY.createExtensionalDataNode(oreda_items, ImmutableMap.of(
                0, TERM_FACTORY.getVariable("OWNER_ID" + idx),
                1, TERM_FACTORY.getVariable("INST_ID" + idx),
                2, TERM_FACTORY.getVariable("I_ID" + idx),
                3, TERM_FACTORY.getVariable("EC_CODE" + idx),
                4, TERM_FACTORY.getVariable("MI_CODE" + idx),
                5, TERM_FACTORY.getVariable("SU_CODE" + idx),
                6, TERM_FACTORY.getVariable("MC_CODE" + idx),
                7, TERM_FACTORY.getVariable("MAC_CODE" + idx),
                8, TERM_FACTORY.getVariable("PM_INTERVAL" + idx)));
    }

    private ExtensionalDataNode oreda_program(int idx) {
        return IQ_FACTORY.createExtensionalDataNode(oreda_program, ImmutableMap.of(
                0, TERM_FACTORY.getVariable("OWNER_ID" + idx),
                1, TERM_FACTORY.getVariable("INST_ID" + idx),
                2, TERM_FACTORY.getVariable("I_ID" + idx),
                3, TERM_FACTORY.getVariable("EC_CODE" + idx),
                4, TERM_FACTORY.getVariable("SU_CODE" + idx),
                5, TERM_FACTORY.getVariable("MC_CODE" + idx),
                6, TERM_FACTORY.getVariable("MAC_CODE" + idx),
                7, TERM_FACTORY.getVariable("PM_INTERVAL" + idx)));
    }

    private IQTree filter(ImmutableExpression exp, IQTree tree) {
        return IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(exp), tree);
    }

    private RAExpressionAttributes getSelectAttributes(String... ids) {
        ImmutableMap.Builder<QuotedID, ImmutableTerm> builder = ImmutableMap.builder();
        for (String id : ids)
            builder.put(idfac.createAttributeID(id.toUpperCase()), TERM_FACTORY.getVariable(id.toUpperCase() + "1"));
        return RAExpressionAttributes.ofUnqualifiedAttributesMap(builder.build());
    }

    private RAExpressionAttributes getSelectAttributes(String id1, ImmutableTerm t1) {
        return RAExpressionAttributes.ofUnqualifiedAttributesMap(ImmutableMap.of(idfac.createAttributeID(id1), t1));
    }

    private RAExpressionAttributes getSelectAttributes(String id1, ImmutableTerm t1, String id2, ImmutableTerm t2) {
        return RAExpressionAttributes.ofUnqualifiedAttributesMap(ImmutableMap.of(idfac.createAttributeID(id1), t1, idfac.createAttributeID(id2), t2));
    }

    private RAExpressionAttributes getSelectAttributes(String id1, ImmutableTerm t1, String id2, ImmutableTerm t2, String id3, ImmutableTerm t3) {
        return RAExpressionAttributes.ofUnqualifiedAttributesMap(ImmutableMap.of(
                idfac.createAttributeID(id1), t1,
                idfac.createAttributeID(id2), t2,
                idfac.createAttributeID(id3), t3));
    }


    @Test
	public void test_0() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("WITH  temp (n) AS (SELECT DISTINCT name FROM student) SELECT * FROM temp"));

        assertEquals("WITH is not supported in SELECT statements [temp (n) AS (SELECT DISTINCT name FROM student)]", ex.getMessage());
	}


	@Test
	public void test_1_1_1() throws Exception {
		RAExpression re = parse("SELECT * FROM student");

        assertEquals(students(), re.getIQTree());
        assertEquals(getSelectAttributes("ID", "NAME", "BIRTH_YEAR", "BIRTH_DATE",
                        "SEMESTER", "NATIONALITY", "GRADE", "CLASS", "ADDRESS"), re.getAttributes());
    }

	@Test
	public void test_1_1_2() throws Exception {
		RAExpression re = parse("SELECT student.* FROM student");

        assertEquals(students(), re.getIQTree());
        assertEquals(getSelectAttributes("ID", "NAME", "BIRTH_YEAR", "BIRTH_DATE", "SEMESTER",
                        "NATIONALITY", "GRADE", "CLASS", "ADDRESS"), re.getAttributes());
    }

	@Test
	public void test_1_2_1() throws Exception {
		RAExpression re = parse("SELECT id FROM student");

        assertEquals(students(), re.getIQTree());
        assertEquals(getSelectAttributes("id"), re.getAttributes());
    }

	@Test
	public void test_1_2_2() throws Exception {
		RAExpression re = parse("SELECT id, name FROM student");

        assertEquals(students(), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name"), re.getAttributes());
	}

	@Test
	public void test_1_3_1() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT DISTINCT name FROM student"));

        assertEquals("DISTINCT is not supported SELECT DISTINCT name FROM student", ex.getMessage());
	}

	@Test
	@Disabled("SQL is not valid, yet JSQLParser accepts it in the form of SELECT name FROM student")
	public void test_1_3_2() throws Exception {
		RAExpression re = parse("SELECT ALL name FROM student");
	}

	@Test
	public void test_1_3_3() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("select DISTINCT ON (name,age,year) name,age FROM student"));

        // DISTINCT ON is PostgreSQL-specific
        assertEquals("DISTINCT is not supported SELECT DISTINCT ON (name, age, year) name, age FROM student", ex.getMessage());
	}

	@Test
	public void test_1_4() throws Exception {
		RAExpression re = parse("SELECT student.id FROM student");

        assertEquals(students(), re.getIQTree());
        assertEquals(getSelectAttributes("id"), re.getAttributes());
	}

	@Test
	public void test_1_5() throws Exception {
		RAExpression re = parse("SELECT student.id, student.name FROM student");

        assertEquals(students(), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name"), re.getAttributes());
	}

	@Test
	public void test_1_5_extra() throws Exception {
		RAExpression re = parse("SELECT \"URI\" as X, VALUE as Y, LANG as Z\n" +
				"FROM QUEST_DATA_PROPERTY_LITERAL_ASSERTION\n" +
				"WHERE ISBNODE = FALSE AND LANG IS NULL AND IDX = 1");

        assertEquals(filter(TERM_FACTORY.getConjunction(TERM_FACTORY.getConjunction(
                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("ISBNODE1"), TERM_FACTORY.getDBBooleanConstant(false)),
                                TERM_FACTORY.getDBIsNull(TERM_FACTORY.getVariable("LANG1"))),
                        TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("IDX1"), TERM_FACTORY.getDBIntegerConstant(1))),
                IQ_FACTORY.createExtensionalDataNode(data_property, ImmutableMap.of(
                        0, TERM_FACTORY.getVariable("URI1"),
                        1, TERM_FACTORY.getVariable("ISBNODE1"),
                        2, TERM_FACTORY.getVariable("IDX1"),
                        3, TERM_FACTORY.getVariable("VALUE1"),
                        4, TERM_FACTORY.getVariable("LANG1")))), re.getIQTree());
        assertEquals(getSelectAttributes(
                "X", TERM_FACTORY.getVariable("URI1"),
                "Y", TERM_FACTORY.getVariable("VALUE1"),
                "Z", TERM_FACTORY.getVariable("LANG1")), re.getAttributes());

    }

	@Test
	public void test_1_5_extra_2() throws Exception {
		RAExpression re = parse("SELECT id, name as alias1, value as alias2 FROM table1");

        assertEquals(IQ_FACTORY.createExtensionalDataNode(table1,  ImmutableMap.of(
                0, TERM_FACTORY.getVariable("ID1"),
                1, TERM_FACTORY.getVariable("NAME1"),
                2, TERM_FACTORY.getVariable("VALUE1"))), re.getIQTree());
        assertEquals(getSelectAttributes(
                "ID", TERM_FACTORY.getVariable("ID1"),
                "ALIAS1", TERM_FACTORY.getVariable("NAME1"),
                "ALIAS2", TERM_FACTORY.getVariable("VALUE1")), re.getAttributes());

    }

	@Test
	public void test_1_5_extra_3() throws Exception {
		// to_char (Oracle specific cast)
		RAExpression re = parse("select to_char(REGION_ID) as RID FROM HR.REGIONS");

        assertEquals(IQ_FACTORY.createExtensionalDataNode(regions, ImmutableMap.of(
                0, TERM_FACTORY.getVariable("REGION_ID1"))), re.getIQTree());
        assertEquals(getSelectAttributes(
                "rid", TERM_FACTORY.getImmutableFunctionalTerm(
                        TERM_FACTORY.getDBFunctionSymbolFactory().getRegularDBFunctionSymbol("TO_CHAR", 1),
                        TERM_FACTORY.getVariable("REGION_ID1"))), re.getAttributes());

    }

	@Test
	public void test_1_5_extra_4() throws Exception {
		RAExpression re = parse("SELECT \"URI1\" as X, \"URI2\" as Y\n" +
				"FROM QUEST_OBJECT_PROPERTY_ASSERTION\n" +
				"WHERE ISBNODE = FALSE AND ISBNODE2 = FALSE AND IDX = 2");

        assertEquals(filter(TERM_FACTORY.getConjunction(TERM_FACTORY.getConjunction(
                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("ISBNODE1"), TERM_FACTORY.getDBBooleanConstant(false)),
                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("ISBNODE21"), TERM_FACTORY.getDBBooleanConstant(false))),
                        TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("IDX1"), TERM_FACTORY.getDBIntegerConstant(2))),
                IQ_FACTORY.createExtensionalDataNode(object_property, ImmutableMap.of(
                        0, TERM_FACTORY.getVariable("URI11"),
                        1, TERM_FACTORY.getVariable("URI21"),
                        2, TERM_FACTORY.getVariable("ISBNODE1"),
                        3, TERM_FACTORY.getVariable("ISBNODE21"),
                        4, TERM_FACTORY.getVariable("IDX1")))), re.getIQTree());
        assertEquals(getSelectAttributes(
                "X", TERM_FACTORY.getVariable("URI11"),
                "Y", TERM_FACTORY.getVariable("URI21")), re.getAttributes());

    }

	@Test
	// all of these are legal column names in SQL server;
	// 'row' and 'rows' are not legal on Oracle;
	public void test_1_5_extra_5() throws Exception {
		RAExpression re = parse("SELECT cast, do, extract, first, following,\n" +
				"last, materialized, nulls, partition, range,\n" +
				"row, rows, siblings, value, xml\n" +
				"FROM tableName");

        assertEquals(IQ_FACTORY.createExtensionalDataNode(tableName, ImmutableMap.<Integer, Variable>builder()
                .put(0, TERM_FACTORY.getVariable("CAST1"))
                .put(1, TERM_FACTORY.getVariable("DO1"))
                .put(2, TERM_FACTORY.getVariable("EXTRACT1"))
                .put(3, TERM_FACTORY.getVariable("SIBLINGS1"))
                .put(4, TERM_FACTORY.getVariable("FIRST1"))
                .put(5, TERM_FACTORY.getVariable("FOLLOWING1"))
                .put(6, TERM_FACTORY.getVariable("LAST1"))
                .put(7, TERM_FACTORY.getVariable("MATERIALIZED1"))
                .put(8, TERM_FACTORY.getVariable("NULLS1"))
                .put(9, TERM_FACTORY.getVariable("PARTITION1"))
                .put(10, TERM_FACTORY.getVariable("RANGE1"))
                .put(11, TERM_FACTORY.getVariable("ROW1"))
                .put(12, TERM_FACTORY.getVariable("ROWS1"))
                .put(13, TERM_FACTORY.getVariable("VALUE1"))
                .put(14, TERM_FACTORY.getVariable("XML1")).build()), re.getIQTree());
        assertEquals(getSelectAttributes("CAST", "DO", "EXTRACT", "FIRST",
                "FOLLOWING", "LAST", "MATERIALIZED", "NULLS", "PARTITION", "RANGE", "ROW",
                "ROWS", "SIBLINGS", "VALUE", "XML"), re.getAttributes());
    }

	@Test
	public void test_1_6_1() throws Exception {
		RAExpression re = parse("SELECT undergraduate.* FROM student as undergraduate");

        assertEquals(students(), re.getIQTree());
        assertEquals(getSelectAttributes("ID", "NAME", "BIRTH_YEAR", "BIRTH_DATE",
                "SEMESTER", "NATIONALITY", "GRADE", "CLASS", "ADDRESS"), re.getAttributes());
    }

	@Test
	public void test_1_6_2() throws Exception {
		RAExpression re = parse("SELECT undergraduate.id FROM student as undergraduate");

        assertEquals(students(), re.getIQTree());
        assertEquals(getSelectAttributes("id"), re.getAttributes());
	}

	@Test
	public void test_1_7() throws Exception {
		RAExpression re = parse("SELECT alias.id, alias.name FROM student as alias");

        assertEquals(students(), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name"), re.getAttributes());
	}

	@Test
	public void test_1_7_1() throws Exception {
        var ex = assertThrows(InvalidQueryException.class, () ->
		        parse("SELECT alias.id, alias.name FROM student"));

        assertEquals("Unable to find attribute alias.id", ex.getMessage().substring(0, ex.getMessage().indexOf(" (")));
	}

	@Test
	public void test_1_8() throws Exception {
		RAExpression re = parse("SELECT id FROM \"STUDENT\"");

        assertEquals(students(), re.getIQTree());
        assertEquals(getSelectAttributes("id"), re.getAttributes());
    }

	@Test
	public void test_1_9() throws Exception {
		RAExpression re = parse("SELECT id FROM \"public\".\"STUDENT\"");

        assertEquals(students(), re.getIQTree());
        assertEquals(getSelectAttributes("id"), re.getAttributes());
    }

	@Test
	public void test_1_10() throws Exception {
		RAExpression re = parse("SELECT t1.id, t1.name FROM \"public\".\"STUDENT\" as t1");

        assertEquals(students(), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name"), re.getAttributes());
	}

	@Test
	public void test_2_1() throws Exception {
		RAExpression re = parse("SELECT id FROM student WHERE id=1");

        assertEquals(filter(
                TERM_FACTORY.getNotYetTypedEquality(
                        TERM_FACTORY.getVariable("ID1"), TERM_FACTORY.getDBIntegerConstant(1)), students()), re.getIQTree());
        assertEquals(getSelectAttributes("id"), re.getAttributes());
	}

	@Test
	public void test_2_2() throws Exception {
		RAExpression re = parse("SELECT id, name FROM student WHERE id=1 AND name='John'");

        assertEquals(filter(TERM_FACTORY.getConjunction(
                TERM_FACTORY.getNotYetTypedEquality(
                        TERM_FACTORY.getVariable("ID1"), TERM_FACTORY.getDBIntegerConstant(1)),
                TERM_FACTORY.getNotYetTypedEquality(
                        TERM_FACTORY.getVariable("NAME1"), TERM_FACTORY.getDBStringConstant("John"))), students()), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name"), re.getAttributes());
	}

	@Test
	public void test_2_3() throws Exception {
		RAExpression re = parse("SELECT id, name, semester, birth_year, nationality "
				+ "FROM student "
				+ "WHERE name<>'John' AND semester>2 AND semester<7 AND "
				+ "birth_year>=1984 AND birth_year<=1990 AND "
				+ "(nationality='IT' OR nationality='DE')");

        assertEquals(filter(TERM_FACTORY.getConjunction(TERM_FACTORY.getConjunction(TERM_FACTORY.getConjunction(TERM_FACTORY.getConjunction(TERM_FACTORY.getConjunction(
                                                TERM_FACTORY.getDBNot(TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("NAME1"), TERM_FACTORY.getDBStringConstant("John"))),
                                                TERM_FACTORY.getDBDefaultInequality(GT, TERM_FACTORY.getVariable("SEMESTER1"), TERM_FACTORY.getDBIntegerConstant(2))),
                                        TERM_FACTORY.getDBDefaultInequality(LT, TERM_FACTORY.getVariable("SEMESTER1"), TERM_FACTORY.getDBIntegerConstant(7))),
                                TERM_FACTORY.getDBDefaultInequality(GTE, TERM_FACTORY.getVariable("BIRTH_YEAR1"), TERM_FACTORY.getDBIntegerConstant(1984))),
                        TERM_FACTORY.getDBDefaultInequality(LTE, TERM_FACTORY.getVariable("BIRTH_YEAR1"), TERM_FACTORY.getDBIntegerConstant(1990))),
                TERM_FACTORY.getDisjunction(
                        TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("NATIONALITY1"), TERM_FACTORY.getDBStringConstant("IT")),
                        TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("NATIONALITY1"), TERM_FACTORY.getDBStringConstant("DE")))), students()), re.getIQTree());

        assertEquals(getSelectAttributes("ID", "NAME", "SEMESTER", "BIRTH_YEAR", "NATIONALITY"), re.getAttributes());
	}

	@Test
	public void test_2_4() throws Exception {
		RAExpression re = parse("SELECT graduate.id, graduate.name FROM student as graduate WHERE graduate.name<>'John'");

        assertEquals(filter(
                TERM_FACTORY.getDBNot(
                        TERM_FACTORY.getNotYetTypedEquality(
                                TERM_FACTORY.getVariable("NAME1"), TERM_FACTORY.getDBStringConstant("John"))), students()), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name"), re.getAttributes());

    }

	@Test
	public void test_2_5() throws Exception {
		RAExpression re = parse("SELECT id, name, grade FROM student WHERE grade is null");

        assertEquals(filter(
                TERM_FACTORY.getDBIsNull(TERM_FACTORY.getVariable("GRADE1")), students()), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name", "grade"), re.getAttributes());
	}

	@Test
	public void test_2_6() throws Exception {
		RAExpression re = parse("SELECT id, name, grade FROM student WHERE grade is not null");

        assertEquals(filter(
                TERM_FACTORY.getDBNot(
                        TERM_FACTORY.getDBIsNull(TERM_FACTORY.getVariable("GRADE1"))), students()), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name", "grade"), re.getAttributes());
	}

	@Test
	public void test_2_7() throws Exception {
		RAExpression re = parse("SELECT id, name, grade FROM student WHERE grade is null AND name<>'John'");

        assertEquals(filter(TERM_FACTORY.getConjunction(
                TERM_FACTORY.getDBIsNull(
                        TERM_FACTORY.getVariable("GRADE1")),
                TERM_FACTORY.getDBNot(
                        TERM_FACTORY.getNotYetTypedEquality(
                                TERM_FACTORY.getVariable("NAME1"), TERM_FACTORY.getDBStringConstant("John")))), students()), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name", "grade"), re.getAttributes());
	}

	@Test
	public void test_2_8() throws Exception {
		RAExpression re = parse("SELECT id, name FROM \"public\".\"STUDENT\" WHERE name<>'John'");

        assertEquals(filter(
                TERM_FACTORY.getDBNot(
                        TERM_FACTORY.getNotYetTypedEquality(
                                TERM_FACTORY.getVariable("NAME1"), TERM_FACTORY.getDBStringConstant("John"))), students()), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name"), re.getAttributes());
	}

	@Test
	public void test_2_9() throws Exception {
		RAExpression re = parse("SELECT t1.id, t1.name FROM \"public\".\"STUDENT\" as t1 "
				+ "WHERE t1.name<>'John'");

        assertEquals(filter(
                TERM_FACTORY.getDBNot(
                        TERM_FACTORY.getNotYetTypedEquality(
                                TERM_FACTORY.getVariable("NAME1"), TERM_FACTORY.getDBStringConstant("John"))), students()), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name"), re.getAttributes());
	}

	@Test
	public void test_2_10() throws Exception {
		RAExpression re = parse("SELECT t1.id, t1.name, t1.grade FROM \"public\".\"STUDENT\" as t1 "
				+ "WHERE t1.grade is not null AND t1.name<>'John'");

        assertEquals(filter(TERM_FACTORY.getConjunction(
                TERM_FACTORY.getDBNot(
                        TERM_FACTORY.getDBIsNull(
                                TERM_FACTORY.getVariable("GRADE1"))),
                TERM_FACTORY.getDBNot(
                        TERM_FACTORY.getNotYetTypedEquality(
                                TERM_FACTORY.getVariable("NAME1"), TERM_FACTORY.getDBStringConstant("John")))), students()), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name", "grade"), re.getAttributes());
	}

	@Test
	public void test_2_11() throws Exception {
		RAExpression re = parse("SELECT id, name FROM student WHERE class IN (7, 8, 9)");

        assertEquals(filter(TERM_FACTORY.getDisjunction(
                TERM_FACTORY.getNotYetTypedEquality(
                        TERM_FACTORY.getVariable("CLASS1"), TERM_FACTORY.getDBIntegerConstant(7)),
                TERM_FACTORY.getNotYetTypedEquality(
                        TERM_FACTORY.getVariable("CLASS1"), TERM_FACTORY.getDBIntegerConstant(8)),
                TERM_FACTORY.getNotYetTypedEquality(
                        TERM_FACTORY.getVariable("CLASS1"), TERM_FACTORY.getDBIntegerConstant(9))), students()), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name"), re.getAttributes());
	}

	@Test
	public void test_2_12() throws Exception {
		RAExpression re = parse("SELECT id, name, grade FROM student WHERE name IN ('John', 'Jack', 'Clara')");

        assertEquals(filter(TERM_FACTORY.getDisjunction(
                TERM_FACTORY.getNotYetTypedEquality(
                        TERM_FACTORY.getVariable("NAME1"), TERM_FACTORY.getDBStringConstant("John")),
                TERM_FACTORY.getNotYetTypedEquality(
                        TERM_FACTORY.getVariable("NAME1"), TERM_FACTORY.getDBStringConstant("Jack")),
                TERM_FACTORY.getNotYetTypedEquality(
                        TERM_FACTORY.getVariable("NAME1"), TERM_FACTORY.getDBStringConstant("Clara"))), students()), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name", "grade"), re.getAttributes());
	}

	@Test
	public void max_test() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT MAX(score) AS max_score FROM grade"));

        assertEquals("Unsupported SQL function MAX(score)", ex.getMessage());
	}

	@Test
	public void min_test() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT MIN(score) AS min_score FROM grade"));

        assertEquals("Unsupported SQL function MIN(score)", ex.getMessage());
	}

	@Test
	public void avg_test() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT AVG(score) AS avg_score FROM grade"));

        assertEquals("Unsupported SQL function AVG(score)", ex.getMessage());
	}

	@Test
	public void sum_test() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT SUM(amount) AS total_amount FROM tax"));

        assertEquals("Unsupported SQL function SUM(amount)", ex.getMessage());
	}

	@Test
	public void count_star_test() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT COUNT(*) AS student_count FROM student"));

        assertEquals("Unsupported SQL function COUNT(*)", ex.getMessage());
	}

	@Test
	public void count_test() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT COUNT(id) AS student_count FROM student"));

        assertEquals("Unsupported SQL function COUNT(id)", ex.getMessage());
	}

	@Test
	@Disabled("SQL:1999 aggregation not supported by JSQLParser: it treats EVERY as a function name here" +
            " see https://blog.jooq.org/2014/12/18/a-true-sql-gem-you-didnt-know-yet-the-every-aggregate-function/")
	public void every_test() throws Exception {
		RAExpression re = parse("SELECT EVERY(id < 10) AS student_id FROM student");
	}

	@Test
	@Disabled("SQL:1999 aggregation not supported by JSQLParser: it treats ANY as a function name here")
	public void any_test() throws Exception {
		RAExpression re = parse("SELECT ANY(id < 10) AS student_id FROM student");
	}

	@Test
	@Disabled("SQL:1999 aggregation not supported by JSQLParser: it treats SOME as a function name here")
	public void some_test() throws Exception {
		RAExpression re = parse("SELECT SOME(id < 10) AS student_id FROM student");
	}


	@Test
	public void test_3_8_1() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT DISTINCT maker FROM Product "
				+ "WHERE type = 'PC' AND NOT model = ANY (SELECT model FROM PC)"));

        assertEquals("DISTINCT is not supported SELECT DISTINCT maker FROM Product WHERE type = 'PC' AND NOT model = ANY (SELECT model FROM PC )", ex.getMessage());
	}


	@Test
	public void test_3_9_1() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT DISTINCT maker FROM Product "
				+ "WHERE type = 'PC' AND NOT model = SOME (SELECT model FROM PC)"));

        assertEquals("DISTINCT is not supported SELECT DISTINCT maker FROM Product WHERE type = 'PC' AND NOT model = SOME (SELECT model FROM PC )", ex.getMessage());
	}

	@Test
	public void test_4_1() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT nationality, COUNT(id) as num_nat FROM student GROUP BY nationality"));

        assertEquals("GROUP BY / HAVING are not supported SELECT nationality, COUNT(id) AS num_nat FROM student GROUP BY nationality", ex.getMessage());
	}

	@Test
	public void test_4_2() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT nationality, COUNT(id) num_nat FROM student WHERE birth_year>2000 GROUP BY nationality"));

        assertEquals("GROUP BY / HAVING are not supported SELECT nationality, COUNT(id) num_nat FROM student WHERE birth_year > 2000 GROUP BY nationality", ex.getMessage());
	}

	@Test
	public void test_4_3() throws Exception {
		RAExpression re = parse("SELECT name as student_name, address as student_address FROM student WHERE id >= 66 AND id <= 69");

        assertEquals(filter(TERM_FACTORY.getConjunction(
                TERM_FACTORY.getDBDefaultInequality(GTE,
                        TERM_FACTORY.getVariable("ID1"), TERM_FACTORY.getDBIntegerConstant(66)),
                TERM_FACTORY.getDBDefaultInequality(LTE,
                        TERM_FACTORY.getVariable("ID1"), TERM_FACTORY.getDBIntegerConstant(69))), students()), re.getIQTree());
        assertEquals(getSelectAttributes(
                "STUDENT_NAME", TERM_FACTORY.getVariable("NAME1"),
                "STUDENT_ADDRESS", TERM_FACTORY.getVariable("ADDRESS1")), re.getAttributes());

    }

	@Test
	public void test_4_4() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT des_date,des_amount,ord_amount FROM despatch WHERE des_amount > ALL("
				+ "SELECT ord_amount FROM orders WHERE ord_amount=2000)"));

        assertEquals("ALL is not supported yet ALL (SELECT ord_amount FROM orders WHERE ord_amount = 2000 )", ex.getMessage());
	}

	@Test
	public void test_5_1() throws Exception {
		RAExpression re = parse("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 JOIN grade t2 ON t1.id=t2.st_id");

        assertEquals(filter(TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("ID1"), TERM_FACTORY.getVariable("ST_ID2")),
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(students(1), grades(2)))), re.getIQTree());
        assertEquals(RAExpressionAttributes.ofUnqualifiedAttributesMap(ImmutableMap.of(
                idfac.createAttributeID("ID"), SQLTestingTools.TERM_FACTORY.getVariable("ID1"),
                idfac.createAttributeID("NAME"), SQLTestingTools.TERM_FACTORY.getVariable("NAME1"),
                idfac.createAttributeID("CLASS_ID"), SQLTestingTools.TERM_FACTORY.getVariable("CLASS_ID2"),
                idfac.createAttributeID("GRADE"), SQLTestingTools.TERM_FACTORY.getVariable("GRADE2"))), re.getAttributes());

    }

	@Test
	public void test_5_1_1() throws Exception {
		RAExpression re = parse("SELECT t1.id as sid, t1.name as fullname FROM student t1 JOIN grade t2 ON t1.id=t2.st_id AND t2.mark='A'");

        assertEquals(filter(TERM_FACTORY.getConjunction(
                        TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("ID1"), TERM_FACTORY.getVariable("ST_ID2")),
                        TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("MARK2"), TERM_FACTORY.getDBStringConstant("A"))),
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(students(1), grades(2)))), re.getIQTree());
        assertEquals(getSelectAttributes(
                "SID", TERM_FACTORY.getVariable("ID1"),
                "FULLNAME", TERM_FACTORY.getVariable("NAME1")), re.getAttributes());

    }

	@Test
	public void test_5_1_2() throws Exception {
		RAExpression re = parse("SELECT t1.id, name FROM student t1 JOIN grade t2 ON t1.id=t2.st_id AND t2.\"SCORE\">=25");

        assertEquals(filter(TERM_FACTORY.getConjunction(
                        TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("ID1"), TERM_FACTORY.getVariable("ST_ID2")),
                        TERM_FACTORY.getDBDefaultInequality(GTE, TERM_FACTORY.getVariable("SCORE2"), TERM_FACTORY.getDBIntegerConstant(25))),
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(students(1), grades(2)))), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name"), re.getAttributes());
	}

	@Test
	public void test_5_1_3() throws Exception {
		RAExpression re = parse("SELECT t1.id, name FROM student t1 JOIN grade t2 ON t1.id=t2.st_id AND t2.pass=true");

        assertEquals(filter(TERM_FACTORY.getConjunction(
                        TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("ID1"), TERM_FACTORY.getVariable("ST_ID2")),
                        TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("PASS2"), TERM_FACTORY.getDBBooleanConstant(true))),
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(students(1), grades(2)))), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name"), re.getAttributes());
	}

	@Test
	public void test_5_2() throws Exception {
		RAExpression re = parse("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 INNER JOIN grade t2 ON t1.id=t2.st_id");

        assertEquals(filter(TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("ID1"), TERM_FACTORY.getVariable("ST_ID2")),
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(students(1), grades(2)))), re.getIQTree());
        assertEquals(RAExpressionAttributes.ofUnqualifiedAttributesMap(ImmutableMap.of(
                idfac.createAttributeID("ID"), SQLTestingTools.TERM_FACTORY.getVariable("ID1"),
                idfac.createAttributeID("NAME"), SQLTestingTools.TERM_FACTORY.getVariable("NAME1"),
                idfac.createAttributeID("CLASS_ID"), SQLTestingTools.TERM_FACTORY.getVariable("CLASS_ID2"),
                idfac.createAttributeID("GRADE"), SQLTestingTools.TERM_FACTORY.getVariable("GRADE2"))), re.getAttributes());
    }

	@Test
	public void test_5_3() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 LEFT JOIN grade t2 ON t1.id=t2.st_id"));

        assertEquals("LEFT/RIGHT/FULL OUTER JOINs are not supported LEFT JOIN grade t2 ON t1.id = t2.st_id", ex.getMessage());
    }

	@Test
	public void test_5_4() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT t1.id, t1.name, t2.class_id, t2.grade FROM student t1 RIGHT JOIN grade t2 ON t1.id=t2.st_id"));

        assertEquals("LEFT/RIGHT/FULL OUTER JOINs are not supported RIGHT JOIN grade t2 ON t1.id = t2.st_id", ex.getMessage());
	}

	@Test
	public void test_5_5() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT id, name, class_id, grade FROM student t1 FULL JOIN grade t2 ON t1.id=t2.st_id"));

        assertEquals("LEFT/RIGHT/FULL OUTER JOINs are not supported FULL JOIN grade t2 ON t1.id = t2.st_id", ex.getMessage());
	}

	@Test
	public void test_5_6() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT id, name, class_id, grade FROM student t1 LEFT OUTER JOIN grade t2 ON t1.id=t2.st_id"));

        assertEquals("LEFT/RIGHT/FULL OUTER JOINs are not supported LEFT OUTER JOIN grade t2 ON t1.id = t2.st_id", ex.getMessage());
	}

	@Test
	public void test_5_7() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT id, name, class_id, grade FROM student t1 RIGHT OUTER JOIN grade t2 ON t1.id=t2.st_id"));

        assertEquals("LEFT/RIGHT/FULL OUTER JOINs are not supported RIGHT OUTER JOIN grade t2 ON t1.id = t2.st_id", ex.getMessage());
	}

	@Test
	public void test_5_8() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT id, name, class_id, grade FROM student t1 FULL OUTER JOIN grade t2 ON t1.id=t2.st_id"));

        assertEquals("LEFT/RIGHT/FULL OUTER JOINs are not supported FULL OUTER JOIN grade t2 ON t1.id = t2.st_id", ex.getMessage());
	}

	@Test
	public void test_5_9() throws Exception {
		RAExpression re = parse("SELECT t1.id, name, score FROM student t1 JOIN grade t2 ON t1.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id");

        assertEquals(filter(TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("SM_ID2"), TERM_FACTORY.getVariable("ID3")),
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                        filter(TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("ID1"), TERM_FACTORY.getVariable("ST_ID2")),
                                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                                        students(1),
                                        grades(2)))),
                        IQ_FACTORY.createExtensionalDataNode(semester, ImmutableMap.of(
                                0, TERM_FACTORY.getVariable("ID3")))))), re.getIQTree());
        assertEquals(getSelectAttributes(
                "ID", TERM_FACTORY.getVariable("ID1"),
                "NAME", TERM_FACTORY.getVariable("NAME1"),
                "SCORE", TERM_FACTORY.getVariable("SCORE2")), re.getAttributes());
    }

	@Test
	public void test_5_10() throws Exception {
		RAExpression re = parse("SELECT t1.id, t1.name, t2.score FROM (SELECT id, name FROM student WHERE student.name='John') AS t1 JOIN grade as t2 ON t1.id=t2.st_id");

        assertEquals(filter(TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("ID1"), TERM_FACTORY.getVariable("ST_ID2")),
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                        filter(TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("NAME1"), TERM_FACTORY.getDBStringConstant("John")), students(1)),
                        grades(2)))), re.getIQTree());
        assertEquals(getSelectAttributes(
                "ID", TERM_FACTORY.getVariable("ID1"),
                "NAME", TERM_FACTORY.getVariable("NAME1"),
                "SCORE", TERM_FACTORY.getVariable("SCORE2")), re.getAttributes());
    }

	@Test
	@Disabled("check the intention")
	public void test_5_11() throws Exception {
		RAExpression re = parse("SELECT id, name, score FROM student JOIN grade USING (id)");
	}

	@Test
	public void test_6_1() throws Exception {
		RAExpression re = parse("SELECT t1.id, t1.name, t2.grade FROM (SELECT id, name FROM student) t1, (SELECT st_id as sid, grade FROM grade) t2 WHERE t1.id=t2.sid");

        assertEquals(filter(TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("ID1"), TERM_FACTORY.getVariable("ST_ID2")),
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(students(1), grades(2)))), re.getIQTree());
        assertEquals(getSelectAttributes(
                "ID", TERM_FACTORY.getVariable("ID1"),
                "NAME", TERM_FACTORY.getVariable("NAME1"),
                "GRADE", TERM_FACTORY.getVariable("GRADE2")), re.getAttributes());
    }

	@Test
	public void test_6_2() throws Exception {
		RAExpression re = parse("SELECT * FROM (SELECT id, name, score FROM student JOIN grade ON student.id=grade.st_id) t1");

        assertEquals(filter(TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("ID1"), TERM_FACTORY.getVariable("ST_ID2")),
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(students(1), grades(2)))), re.getIQTree());
        assertEquals(getSelectAttributes(
                "ID", TERM_FACTORY.getVariable("ID1"),
                "NAME", TERM_FACTORY.getVariable("NAME1"),
                "SCORE", TERM_FACTORY.getVariable("SCORE2")), re.getAttributes());
    }

	@Test
	public void test_6_3() throws Exception {
		RAExpression re = parse("SELECT * FROM (SELECT id, name, score FROM student JOIN grade ON student.id=grade.st_id) t1 WHERE t1.score>=25");

        assertEquals(filter(TERM_FACTORY.getDBDefaultInequality(GTE, TERM_FACTORY.getVariable("SCORE2"), TERM_FACTORY.getDBIntegerConstant(25)),
                filter(TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("ID1"), TERM_FACTORY.getVariable("ST_ID2")),
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(students(1), grades(2))))), re.getIQTree());
        assertEquals(getSelectAttributes(
                "ID", TERM_FACTORY.getVariable("ID1"),
                "NAME", TERM_FACTORY.getVariable("NAME1"),
                "SCORE", TERM_FACTORY.getVariable("SCORE2")), re.getAttributes());
	}

	@Test
	public void test_7_1() throws Exception {
		RAExpression re = parse("SELECT ('ID-' || student.id) as sid FROM student");

        assertEquals(students(), re.getIQTree());
        assertEquals(getSelectAttributes(
                "SID", TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(ImmutableList.of(
                        TERM_FACTORY.getDBStringConstant("ID-"), TERM_FACTORY.getVariable("ID1")))), re.getAttributes());
	}

	@Test
	public void test_7_1_b() throws Exception {
		RAExpression re = parse("SELECT CONCAT('ID-', student.id, 'b') as sid FROM student");

        assertEquals(students(), re.getIQTree());
        assertEquals(getSelectAttributes(
                "SID", TERM_FACTORY.getImmutableFunctionalTerm(TERM_FACTORY.getDBFunctionSymbolFactory().getRegularDBFunctionSymbol("CONCAT", 3),
                        TERM_FACTORY.getDBStringConstant("ID-"), TERM_FACTORY.getVariable("ID1"), TERM_FACTORY.getDBStringConstant("b"))), re.getAttributes());
    }


	@Test
	public void test_7_2() throws Exception {
		RAExpression re = parse("SELECT (grade.score * 30 / 100) as percentage from grade");

        assertEquals(grades(), re.getIQTree());
        assertEquals(getSelectAttributes(
                "PERCENTAGE", TERM_FACTORY.getImmutableFunctionalTerm(
                        TERM_FACTORY.getDBFunctionSymbolFactory().getUntypedDBMathBinaryOperator("/"),
                        TERM_FACTORY.getImmutableFunctionalTerm(TERM_FACTORY.getDBFunctionSymbolFactory().getUntypedDBMathBinaryOperator("*"),
                                TERM_FACTORY.getVariable("SCORE1"), TERM_FACTORY.getDBIntegerConstant(30)), TERM_FACTORY.getDBIntegerConstant(100))), re.getAttributes());
    }

	@Test
	public void test_8_1() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT name FROM student UNION ALL SELECT name FROM erasmus"));

        assertEquals("Complex SELECT statements are not supported SELECT name FROM student UNION ALL SELECT name FROM erasmus", ex.getMessage());
	}

	@Test
	public void test_8_2() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT name FROM student UNION ALL SELECT name FROM erasmus UNION SELECT DISTINCT payee FROM tax"));

        assertEquals("Complex SELECT statements are not supported SELECT name FROM student UNION ALL SELECT name FROM erasmus UNION SELECT DISTINCT payee FROM tax", ex.getMessage());
	}

	@Test
	public void test_8_3() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT name FROM student WHERE id = 20 UNION ALL SELECT name FROM erasmus WHERE id = 20"));

        assertEquals("Complex SELECT statements are not supported SELECT name FROM student WHERE id = 20 UNION ALL SELECT name FROM erasmus WHERE id = 20", ex.getMessage());
	}

	@Test
	public void test_8_4() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT name FROM student JOIN grade on student.id=grade.st_id AND grade.score>=25 UNION SELECT name FROM erasmus"));

        assertEquals("Complex SELECT statements are not supported SELECT name FROM student JOIN grade ON student.id = grade.st_id AND grade.score >= 25 UNION SELECT name FROM erasmus", ex.getMessage());
	}

	@Test
	public void test_8_5() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT id, name, course, score, semester FROM student t1 JOIN grade t2 ON t1.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id "
				+ "UNION ALL SELECT id, name, course, score, semester FROM erasmus t4 JOIN grade t2 ON t4.id=t2.st_id JOIN semester t3 ON t2.sm_id=t3.id"));

        assertEquals("Complex SELECT statements are not supported", ex.getMessage().substring(0, ex.getMessage().indexOf(" SELECT id")));
	}

	@Test
	public void test_9_1() throws Exception {
		RAExpression re = parse("SELECT id, name, address from student where name = 'John'");

        assertEquals(filter(
                TERM_FACTORY.getNotYetTypedEquality(
                        TERM_FACTORY.getVariable("NAME1"), TERM_FACTORY.getDBStringConstant("John")), students()), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name", "address"), re.getAttributes());
	}

	@Test
	public void test_9_2() throws Exception {
		RAExpression re = parse("SELECT id, name, address from student where id = 20");

        assertEquals(filter(
                TERM_FACTORY.getNotYetTypedEquality(
                        TERM_FACTORY.getVariable("ID1"), TERM_FACTORY.getDBIntegerConstant(20)), students()), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name", "address"), re.getAttributes());
	}

	@Test
	public void test_9_3() throws Exception {
		RAExpression re = parse("SELECT payee, amount from tax where amount = 12.345");

        assertEquals(filter(TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("AMOUNT1"), TERM_FACTORY.getDBConstant("12.345", CORE_SINGLETONS.getTypeFactory().getDBTypeFactory().getDBDoubleType())),
                IQ_FACTORY.createExtensionalDataNode(tax, ImmutableMap.of(0, TERM_FACTORY.getVariable("PAYEE1"),
                        1, TERM_FACTORY.getVariable("AMOUNT1")))), re.getIQTree());
        assertEquals(getSelectAttributes("payee", "amount"), re.getAttributes());
	}

	@Test
	public void test_9_4_1() throws Exception {
		RAExpression re = parse("SELECT id, name, address from student where birth_date = '1984-01-22 00:02:01.234'");

        assertEquals(filter(
                TERM_FACTORY.getNotYetTypedEquality(
                        TERM_FACTORY.getVariable("BIRTH_DATE1"), TERM_FACTORY.getDBStringConstant("1984-01-22 00:02:01.234")), students()), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name", "address"), re.getAttributes());
	}

	@Test
	public void test_9_4_2() throws Exception {
		RAExpression re = parse("SELECT id, name, address from student where birth_date = '1984-01-22 00:02:01'");

        assertEquals(filter(
                TERM_FACTORY.getNotYetTypedEquality(
                        TERM_FACTORY.getVariable("BIRTH_DATE1"), TERM_FACTORY.getDBStringConstant("1984-01-22 00:02:01")), students()), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name", "address"), re.getAttributes());
	}

	@Test
	public void test_9_4_3() throws Exception {
		RAExpression re = parse("SELECT id, name, address from student where birth_date = '1984-01-22'");

        assertEquals(filter(
                TERM_FACTORY.getNotYetTypedEquality(
                        TERM_FACTORY.getVariable("BIRTH_DATE1"), TERM_FACTORY.getDBStringConstant("1984-01-22")), students()), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name", "address"), re.getAttributes());
	}

	@Test
	public void test_9_5() throws Exception {
		RAExpression re = parse("SELECT st_id, course, score from grade where pass = TRUE");

        assertEquals(filter(TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("PASS1"), TERM_FACTORY.getDBBooleanConstant(true)), grades()), re.getIQTree());
        assertEquals(getSelectAttributes("st_id", "course", "score"), re.getAttributes());
	}

	@Test
	public void test_10_1() throws Exception {
		RAExpression re = parse("SELECT name from grade, student where pass = TRUE AND course = 'CS001' AND ( (score = 8 AND mark = 'B') OR (score = 7 AND mark = 'C') OR (score >= 9 AND mark = 'A') )");

        assertEquals(filter(TERM_FACTORY.getConjunction(TERM_FACTORY.getConjunction(
                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("PASS1"), TERM_FACTORY.getDBBooleanConstant(true)),
                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("COURSE1"), TERM_FACTORY.getDBStringConstant("CS001"))),
                        TERM_FACTORY.getDisjunction(TERM_FACTORY.getDisjunction(
                                        TERM_FACTORY.getConjunction(
                                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("SCORE1"), TERM_FACTORY.getDBIntegerConstant(8)),
                                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("MARK1"), TERM_FACTORY.getDBStringConstant("B"))),
                                        TERM_FACTORY.getConjunction(
                                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("SCORE1"), TERM_FACTORY.getDBIntegerConstant(7)),
                                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("MARK1"), TERM_FACTORY.getDBStringConstant("C")))),
                                TERM_FACTORY.getConjunction(
                                        TERM_FACTORY.getDBDefaultInequality(GTE, TERM_FACTORY.getVariable("SCORE1"), TERM_FACTORY.getDBIntegerConstant(9)),
                                        TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("MARK1"), TERM_FACTORY.getDBStringConstant("A"))))),
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(grades(), students(2)))), re.getIQTree());
        assertEquals(getSelectAttributes("NAME",  TERM_FACTORY.getVariable("NAME2")), re.getAttributes());
	}

	@Test
	public void test_10_2() throws Exception {
		RAExpression re = parse("SELECT name from grade, student where pass = FALSE AND ( course = 'CS001' OR ( (score = 6 AND mark = 'D') OR (score <= 5 AND mark = 'E') ) )");

        assertEquals(filter(TERM_FACTORY.getConjunction(
                        TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("PASS1"), TERM_FACTORY.getDBBooleanConstant(false)),
                        TERM_FACTORY.getDisjunction(
                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("COURSE1"), TERM_FACTORY.getDBStringConstant("CS001")),
                                TERM_FACTORY.getDisjunction(
                                        TERM_FACTORY.getConjunction(
                                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("SCORE1"), TERM_FACTORY.getDBIntegerConstant(6)),
                                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("MARK1"), TERM_FACTORY.getDBStringConstant("D"))),
                                        TERM_FACTORY.getConjunction(
                                                TERM_FACTORY.getDBDefaultInequality(LTE, TERM_FACTORY.getVariable("SCORE1"), TERM_FACTORY.getDBIntegerConstant(5)),
                                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("MARK1"), TERM_FACTORY.getDBStringConstant("E")))))),
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(grades(), students(2)))), re.getIQTree());
        assertEquals(getSelectAttributes("NAME",  TERM_FACTORY.getVariable("NAME2")), re.getAttributes());
	}

	@Test
	public void test_11() throws Exception {
		RAExpression re = parse("SELECT \"NAME\" from grade, student where pass = FALSE AND ( \"COURSE\" = 'CS001' OR ( (score = 6 AND mark = 'D') OR (score <= 5 AND mark = 'E') ) )");

        assertEquals(filter(TERM_FACTORY.getConjunction(
                        TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("PASS1"), TERM_FACTORY.getDBBooleanConstant(false)),
                        TERM_FACTORY.getDisjunction(
                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("COURSE1"), TERM_FACTORY.getDBStringConstant("CS001")),
                                TERM_FACTORY.getDisjunction(
                                        TERM_FACTORY.getConjunction(
                                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("SCORE1"), TERM_FACTORY.getDBIntegerConstant(6)),
                                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("MARK1"), TERM_FACTORY.getDBStringConstant("D"))),
                                        TERM_FACTORY.getConjunction(
                                                TERM_FACTORY.getDBDefaultInequality(LTE, TERM_FACTORY.getVariable("SCORE1"), TERM_FACTORY.getDBIntegerConstant(5)),
                                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("MARK1"), TERM_FACTORY.getDBStringConstant("E")))))),
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(grades(), students(2)))), re.getIQTree());
        assertEquals(getSelectAttributes("NAME", TERM_FACTORY.getVariable("NAME2")), re.getAttributes());
	}

	@Test
	public void test_11_1() throws Exception {
		RAExpression re = parse("select t1.owner NAME from all_tables t1, all_tables t2, ALL_VIEWS where t1.table_name = t2.table_name and t1.owner = t2.owner and t1.owner = ALL_VIEWS.OWNER");

        assertEquals(filter(TERM_FACTORY.getConjunction(TERM_FACTORY.getConjunction(
                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("TABLE_NAME1"), TERM_FACTORY.getVariable("TABLE_NAME2")),
                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("OWNER1"), TERM_FACTORY.getVariable("OWNER2"))),
                        TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("OWNER1"), TERM_FACTORY.getVariable("OWNER3"))),
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(all_tables(1), all_tables(2))), all_views(3)
                ))), re.getIQTree());
        assertEquals(getSelectAttributes("NAME",  TERM_FACTORY.getVariable("OWNER1")), re.getAttributes());
	}

	@Test
	public void test_12() throws Exception {
		RAExpression re = parse("select name from grade, student where score BETWEEN 6 AND 8");

        assertEquals(filter(TERM_FACTORY.getConjunction(
                        TERM_FACTORY.getDBDefaultInequality(GTE, TERM_FACTORY.getVariable("SCORE1"), TERM_FACTORY.getDBIntegerConstant(6)),
                        TERM_FACTORY.getDBDefaultInequality(LTE, TERM_FACTORY.getVariable("SCORE1"), TERM_FACTORY.getDBIntegerConstant(8))),
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(grades(), students(2)))), re.getIQTree());
        assertEquals(getSelectAttributes("NAME",  TERM_FACTORY.getVariable("NAME2")), re.getAttributes());
	}

	@Test
	public void test_13() throws Exception {
		RAExpression re = parse("select REGEXP_REPLACE(name, ' +', ' ') as reg from student ");
        assertEquals(students(), re.getIQTree());
        assertEquals(getSelectAttributes(
                "REG", TERM_FACTORY.getDBRegexpReplace(
                        TERM_FACTORY.getVariable("NAME1"), TERM_FACTORY.getDBStringConstant(" +"), TERM_FACTORY.getDBStringConstant(" "))), re.getAttributes());

    }


	@Test
	public void testUnquoted0() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT DISTINCT 3 AS \"v0QuestType\", NULL AS \"v0Lang\", CAST(\"QpeopleVIEW0\".\"nick2\" AS CHAR) AS \"v0\", 1 AS \"v1QuestType\", NULL AS \"v1Lang\", QpeopleVIEW0.id AS \"v1\""
				+ "FROM people \"QpeopleVIEW0\" "
				+ "WHERE \"QpeopleVIEW0\".\"id\" IS NOT NULL AND \"QpeopleVIEW0\".\"nick2\" IS NOT NULL"));

        assertEquals("DISTINCT is not supported", ex.getMessage().substring(0, ex.getMessage().indexOf(" SELECT DISTINCT")));
	}

	@Test
	public void testUnquoted1() throws Exception {
		RAExpression re = parse("SELECT 3 AS \"v0QuestType\", NULL AS \"v0Lang\", CAST(\"QpeopleVIEW0\".\"nick2\" AS CHAR) AS \"v0\", 1 AS \"v1QuestType\", NULL AS \"v1Lang\", \"QpeopleVIEW0\".\"id\" AS \"v1\""
				+ "FROM people \"QpeopleVIEW0\" "
				+ "WHERE \"QpeopleVIEW0\".\"id\" IS NOT NULL AND \"QpeopleVIEW0\".\"nick2\" IS NOT NULL");
	}

	@Test
	public void testUnquoted2() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT DISTINCT 3 AS \"v0QuestType\", NULL AS \"v0Lang\", CAST(\"QpeopleVIEW0\".\"nick2\" AS CHAR) AS \"v0\", 1 AS \"v1QuestType\", NULL AS \"v1Lang\", QpeopleVIEW0.id AS \"v1\""
				+ "FROM people \"QpeopleVIEW0\" "
				+ "WHERE \"QpeopleVIEW0\".\"id\" IS NOT NULL AND \"QpeopleVIEW0\".\"nick2\" IS NOT NULL"));

        assertEquals("DISTINCT is not supported", ex.getMessage().substring(0, ex.getMessage().indexOf(" SELECT DISTINCT")));
	}

	@Test
	public void testCast1() throws Exception {
		RAExpression re = parse("SELECT CAST(\"view0\".\"nick2\" AS CHAR (8000) CHARACTER SET utf8) AS \"v0\" FROM people \"view0\" WHERE \"view0\".\"nick2\" IS NOT NULL");

        assertEquals(filter(TERM_FACTORY.getDBNot(TERM_FACTORY.getDBIsNull(TERM_FACTORY.getVariable("nick21"))), people()), re.getIQTree());
        assertEquals(getSelectAttributes("\"v0\"", TERM_FACTORY.getDBCastFunctionalTerm(dbTypeFactory.getDBTermType("CHAR", 8000), TERM_FACTORY.getVariable("nick21"))), re.getAttributes());
	}

	@Test
	public void testCast2() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT DISTINCT CAST(`view0`.`nick2` AS CHAR (8000) CHARACTER SET utf8) AS `v0` FROM people `view0` WHERE `view0`.`nick2` IS NOT NULL"));

        assertEquals("DISTINCT is not supported", ex.getMessage().substring(0, ex.getMessage().indexOf(" SELECT DISTINCT")));
	}

	/* Regex in MySQL, Oracle and Postgres*/

	@Test
	public void testRegexMySQL() throws Exception {
		RAExpression re = parse("SELECT * FROM pet WHERE name REGEXP '^b'");

        assertEquals(filter(
                TERM_FACTORY.getDBRegexpMatches(
                        ImmutableList.of(TERM_FACTORY.getVariable("NAME1"), TERM_FACTORY.getDBStringConstant("^b"), TERM_FACTORY.getDBStringConstant("i"))),
                pets()), re.getIQTree());
        assertEquals(getSelectAttributes("name", "testcol"), re.getAttributes());
    }

	@Test
	public void testRegexBinaryMySQL() throws Exception {
		RAExpression re = parse("SELECT * FROM pet WHERE name REGEXP BINARY '^b'");

        assertEquals(filter(
                TERM_FACTORY.getDBRegexpMatches(
                        ImmutableList.of(TERM_FACTORY.getVariable("NAME1"), TERM_FACTORY.getDBStringConstant("^b"))),
                pets()), re.getIQTree());
        assertEquals(getSelectAttributes("name", "testcol"), re.getAttributes());
	}

	@Test
	public void testRegexPostgres() throws Exception {
		RAExpression re = parse("SELECT * FROM pet WHERE name ~ 'foo'");

        assertEquals(filter(
                TERM_FACTORY.getDBRegexpMatches(
                        ImmutableList.of(TERM_FACTORY.getVariable("NAME1"), TERM_FACTORY.getDBStringConstant("foo"))),
                pets()), re.getIQTree());
        assertEquals(getSelectAttributes("name", "testcol"), re.getAttributes());
	}

	@Test
	public void testRegexPostgresSimilarTo() throws Exception {
		RAExpression re = parse("SELECT * FROM pet WHERE 'abc' SIMILAR TO 'abc'");

        assertEquals(filter(
                TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBSimilarTo(),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("abc"), TERM_FACTORY.getDBStringConstant("abc"))),
                pets()), re.getIQTree());
        assertEquals(getSelectAttributes("name", "testcol"), re.getAttributes());
	}

	@Test
	public void testRegexOracle() throws Exception {
		RAExpression re = parse("SELECT * FROM pet WHERE REGEXP_LIKE(testcol, '[[:alpha:]]')");

        assertEquals(filter(
                TERM_FACTORY.getDBRegexpMatches(
                        ImmutableList.of(TERM_FACTORY.getVariable("TESTCOL1"), TERM_FACTORY.getDBStringConstant("[[:alpha:]]"))),
                pets()), re.getIQTree());
        assertEquals(getSelectAttributes("name", "testcol"), re.getAttributes());
	}

	@Test
	public void testRegexNotOracle() throws Exception {
		RAExpression re = parse("SELECT * FROM pet WHERE NOT REGEXP_LIKE(testcol, '[[:alpha:]]')");

        assertEquals(filter(TERM_FACTORY.getDBNot(
                        TERM_FACTORY.getDBRegexpMatches(
                                ImmutableList.of(TERM_FACTORY.getVariable("TESTCOL1"), TERM_FACTORY.getDBStringConstant("[[:alpha:]]")))),
                pets()), re.getIQTree());
        assertEquals(getSelectAttributes("name", "testcol"), re.getAttributes());
	}

	@Test
	public void testRegexNotOracle_1() throws Exception {
		RAExpression re = parse("SELECT * FROM pet WHERE NOT (REGEXP_LIKE(testcol, '[[:alpha:]]'))");

        assertEquals(filter(TERM_FACTORY.getDBNot(
                        TERM_FACTORY.getDBRegexpMatches(
                                ImmutableList.of(TERM_FACTORY.getVariable("TESTCOL1"), TERM_FACTORY.getDBStringConstant("[[:alpha:]]")))),
                pets()), re.getIQTree());
        assertEquals(getSelectAttributes("name", "testcol"), re.getAttributes());
	}

	@Test
	public void test_md5() throws Exception {
		RAExpression re = parse("SELECT MD5(CONCAT(COALESCE(Address, RAND()), COALESCE(City, RAND()),\n" +
				"COALESCE(Region, RAND()), COALESCE(PostalCode, RAND()), COALESCE(Country,\n" +
				"RAND()) )) AS locationID FROM northwind.Suppliers");

        assertEquals(IQ_FACTORY.createExtensionalDataNode(suppliers, ImmutableMap.of(
                0, TERM_FACTORY.getVariable("REGION1"),
                1, TERM_FACTORY.getVariable("CITY1"),
                2, TERM_FACTORY.getVariable("POSTALCODE1"),
                3, TERM_FACTORY.getVariable("ADDRESS1"),
                4, TERM_FACTORY.getVariable("COUNTRY1"))), re.getIQTree());
        // impossible to test re.getAttributes() as it contains a random component
	}

	@Test
	public void test_concatOracle() throws Exception {
		RAExpression re = parse("SELECT ('ID-' || student.id || 'type1') \"sid\" FROM student");

        assertEquals(students(), re.getIQTree());
        assertEquals(getSelectAttributes(
                "\"sid\"",  TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(ImmutableList.of(
                        TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(
                                ImmutableList.of(TERM_FACTORY.getDBStringConstant("ID-"), TERM_FACTORY.getVariable("ID1"))),
                        TERM_FACTORY.getDBStringConstant("type1")))), re.getAttributes());
    }

	@Test
	public void test_RegexpReplace() throws Exception {
		RAExpression re = parse("SELECT REGEXP_REPLACE('Hello World', ' +', ' ') as reg FROM student");

        assertEquals(students(), re.getIQTree());
        assertEquals(getSelectAttributes(
                "REG",  TERM_FACTORY.getDBRegexpReplace(
                        TERM_FACTORY.getDBStringConstant("Hello World"),TERM_FACTORY.getDBStringConstant(" +"),
                        TERM_FACTORY.getDBStringConstant(" "))), re.getAttributes());
    }

	@Test
	public void test_2_p() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT \"ID\" as \"KEYID\"\n" +
				"      ,CONVERT(varchar(50), \"DATETIME\", 0) as \"DATETIMEH\"\n" +
				"      ,\"SCALE\" as \"SCALE\"\n" +
				"      ,\"INTERVAL\" as \"TEMPINTERVAL\"\n" +
				"  FROM \"CIM\".\"dbo\".\"TEMPERATURE_DEVIATION\" where \"INTERVAL\" = '0-10'"));

        assertEquals("Unsupported SQL function CONVERT(varchar(50), \"DATETIME\", 0)", ex.getMessage());
	}

	@Test
	public void test_2() throws Exception {
		RAExpression re = parse("SELECT \"ID\"\n" +
				"      ,\"DATETIME\"\n" +
				"      ,\"SCALE\"\n" +
				"      ,\"INTERVAL\"\n" +
				"  FROM \"CIM\".\"dbo\".\"TEMPERATURE_DEVIATION\" where \"INTERVAL\" = '0-10'");

        assertEquals(filter(TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("INTERVAL1"), TERM_FACTORY.getDBStringConstant("0-10")),
                IQ_FACTORY.createExtensionalDataNode(temperature_deviation, ImmutableMap.of(
                        0, TERM_FACTORY.getVariable("ID1"),
                        1, TERM_FACTORY.getVariable("DATETIME1"),
                        2, TERM_FACTORY.getVariable("SCALE1"),
                        3, TERM_FACTORY.getVariable("INTERVAL1")))), re.getIQTree());
        assertEquals(getSelectAttributes("ID", "DATETIME", "SCALE", "INTERVAL"), re.getAttributes());
    }

	@Test
	public void test_double_subquery() throws Exception {
		RAExpression re = parse("SELECT * FROM (SELECT * FROM oreda.pm_maint_items) AS child, (SELECT * FROM oreda.pm_program) AS parent  WHERE child.i_id=parent.i_id AND child.inst_id=parent.inst_id AND child.su_code=parent.su_code AND child.pm_interval=parent.pm_interval AND child.mc_code=parent.mc_code AND child.mac_code=parent.mac_code AND child.owner_id=parent.owner_id");

        assertEquals(filter(TERM_FACTORY.getConjunction(TERM_FACTORY.getConjunction(TERM_FACTORY.getConjunction(TERM_FACTORY.getConjunction(TERM_FACTORY.getConjunction(TERM_FACTORY.getConjunction(
                                                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("I_ID1"), TERM_FACTORY.getVariable("I_ID2")),
                                                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("INST_ID1"), TERM_FACTORY.getVariable("INST_ID2"))),
                                                        TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("SU_CODE1"), TERM_FACTORY.getVariable("SU_CODE2"))),
                                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("PM_INTERVAL1"), TERM_FACTORY.getVariable("PM_INTERVAL2"))),
                                        TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("MC_CODE1"), TERM_FACTORY.getVariable("MC_CODE2"))),
                                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("MAC_CODE1"), TERM_FACTORY.getVariable("MAC_CODE2"))),
                        TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable("OWNER_ID1"), TERM_FACTORY.getVariable("OWNER_ID2"))),
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(oreda_items(1), oreda_program(2)))), re.getIQTree());
        assertEquals(getSelectAttributes("MI_CODE"), re.getAttributes());
	}

	@Test //due to IN with subselect
	public void test_IN() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("SELECT * FROM oreda.pm_maint_items  WHERE (i_id,  pm_interval) IN (SELECT i_id, MAX(pm_interval) FROM oreda.pm_program GROUP BY i_id)"));

        assertEquals("Expression on the right in IN is not supported (i_id, pm_interval) IN (SELECT i_id, MAX(pm_interval) FROM oreda.pm_program GROUP BY i_id)", ex.getMessage());
	}

    @Test
    public void test_lower() throws Exception {
        RAExpression re = parse("select id, name from student where lower(name)=lower('ColleeN')");

        assertEquals(filter(
                TERM_FACTORY.getNotYetTypedEquality(
                        TERM_FACTORY.getDBLower(TERM_FACTORY.getVariable("NAME1")),
                        TERM_FACTORY.getDBLower(TERM_FACTORY.getDBStringConstant("ColleeN"))), students()), re.getIQTree());
        assertEquals(getSelectAttributes("id", "name"), re.getAttributes());
    }

    @Test
    public void test_lower2() throws Exception {
        RAExpression re = parse("select id, lower(name) as lower_name from student");

        assertEquals(students(), re.getIQTree());
        assertEquals(getSelectAttributes(
                "id", TERM_FACTORY.getVariable("ID1"),
                "lower_name", TERM_FACTORY.getDBLower(TERM_FACTORY.getVariable("NAME1"))), re.getAttributes());
    }

	@Test // issue 157
	public void test_locate() throws Exception {
		RAExpression re = parse("select id, locate('A', name, 2) as pos from student");

        assertEquals(students(), re.getIQTree());
        assertEquals(getSelectAttributes(
                        "id", TERM_FACTORY.getVariable("ID1"),
                        "POS", TERM_FACTORY.getImmutableFunctionalTerm(
                                TERM_FACTORY.getDBFunctionSymbolFactory().getRegularDBFunctionSymbol("LOCATE", 3),
                                TERM_FACTORY.getDBStringConstant("A"), TERM_FACTORY.getVariable("NAME1"), TERM_FACTORY.getDBIntegerConstant(2))), re.getAttributes());
    }

	@Test // issue 157
	public void test_position() throws Exception {
		RAExpression re = parse("select id, position('A', name) as pos from student");

        assertEquals(students(), re.getIQTree());
        assertEquals(getSelectAttributes(
                "id", TERM_FACTORY.getVariable("ID1"),
                "POS", TERM_FACTORY.getImmutableFunctionalTerm(
                        TERM_FACTORY.getDBFunctionSymbolFactory().getRegularDBFunctionSymbol("POSITION", 2),
                        TERM_FACTORY.getDBStringConstant("A"), TERM_FACTORY.getVariable("NAME1"))), re.getAttributes());
    }

	@Test // issue 184
	public void test_limit() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
		        parse("select STUDY_ID, patient_name(STUDY_ID) as label from demographics order by STUDY_ID limit 50"));

        assertEquals("ORDER BY is not supported SELECT STUDY_ID, patient_name(STUDY_ID) AS label FROM demographics ORDER BY STUDY_ID LIMIT 50", ex.getMessage());
	}

	@Test
	public void test_table_with_dots() throws Exception {
		RAExpression re = parse("select * from \"table.with.dots\"");

        assertEquals(IQ_FACTORY.createExtensionalDataNode(table_with_dots, ImmutableMap.of(
                0, TERM_FACTORY.getVariable("ID1"))), re.getIQTree());
        assertEquals(getSelectAttributes("id"), re.getAttributes());
	}


	@Test
	public void test_schema_with_dots() throws Exception {
		RAExpression re = parse("select * from \"SCHEMA.WITH.DOTS\".\"table.with.dots\"");

        assertEquals(IQ_FACTORY.createExtensionalDataNode(schema_with_dots_table_with_dots, ImmutableMap.of(
                0, TERM_FACTORY.getVariable("ID1"))), re.getIQTree());
        assertEquals(getSelectAttributes("id"), re.getAttributes());
	}

	@Test
	public void test_multiple_components() throws Exception {
		RAExpression re = parse("select * from name.many.many.so.many.components");

        assertEquals(IQ_FACTORY.createExtensionalDataNode(name_with_many_many_so_many_components, ImmutableMap.of(
                0, TERM_FACTORY.getVariable("ID1"))), re.getIQTree());
        assertEquals(getSelectAttributes("id"), re.getAttributes());
	}

	@Test
	public void test_multiple_dotted_components() throws Exception {
		RAExpression re = parse("select * from \"name.with.dots\".many.many.\"so.so\".many.components");

        assertEquals(IQ_FACTORY.createExtensionalDataNode(name_with_dots_many_many_so_many_components, ImmutableMap.of(
                0, TERM_FACTORY.getVariable("ID1"))), re.getIQTree());
        assertEquals(getSelectAttributes("id"), re.getAttributes());
	}
}
