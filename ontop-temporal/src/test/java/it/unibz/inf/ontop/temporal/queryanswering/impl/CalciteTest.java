package it.unibz.inf.ontop.temporal.queryanswering.impl;


import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.materialize.MaterializationKey;
import org.apache.calcite.materialize.MaterializationService;
import org.apache.calcite.plan.*;
import org.apache.calcite.prepare.Prepare;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.tools.*;
import org.apache.calcite.util.Util;
import org.apache.commons.dbcp.BasicDataSource;

import java.util.List;

/**
 // * Example that uses {@link RelBuilder}
 * to create various relational expressions.
 */
public class CalciteTest {
    private final boolean verbose;
    private Planner planner;

    public CalciteTest(boolean verbose) {
        this.verbose = verbose;
    }

    public static SchemaPlus createSchema (SchemaPlus rootSchema) throws ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl("jdbc:postgresql://obdalin.inf.unibz.it:5433/siemens_exp");
        dataSource.setUsername("postgres");
        dataSource.setPassword("postgres");
        Schema schema = JdbcSchema.create(rootSchema, "public", dataSource,
                null, "public");
        SchemaPlus result = rootSchema.add("public", schema);
        return result;
    }

    public static Frameworks.ConfigBuilder config() throws ClassNotFoundException {
        final SchemaPlus rootSchema = Frameworks.createRootSchema(true);
        SchemaPlus schema = createSchema(rootSchema);
        return Frameworks.newConfigBuilder()
                .parserConfig(SqlParser.Config.DEFAULT)
                .defaultSchema(schema)
                .traitDefs(ConventionTraitDef.INSTANCE);

    }

    public static void main(String[] args) throws ClassNotFoundException, RelConversionException, SqlParseException, ValidationException {
        CalciteTest exampler = new CalciteTest(true);
        exampler.runExamplesFromSQL();
    }

    private void runExamplesFromSQL() throws ClassNotFoundException, RelConversionException, SqlParseException, ValidationException {
        final FrameworkConfig config = config().build();
        this.planner = Frameworks.getPlanner(config);

        String postgresql = "WITH C1_AP_1 AS (\n" +
                "SELECT dFrom, dTo \n" +
                "FROM (\n" +
                "SELECT \"timestamp\" AS dFrom,\n" +
                "LEAD(\"timestamp\", 1) OVER (ORDER BY  \"timestamp\") AS dTo, \"value\" \n" +
                "FROM \"public\".\"tb_measurement\") F\n" +
                "WHERE \"value\" > 1.5 AND dTo IS NOT NULL AND (dTo - interval \'1 \' DAY) >= dFrom " +
                "),\n" +
                "AP_2 AS (\n" +
                "SELECT dFrom, dTo \n" +
                "FROM C1_AP_1 \n" +
                ") \n" +
                "SELECT\n" +
                "CASE \n" +
                "WHEN C1_AP_1.dFrom > AP_2.dFrom AND AP_2.dTo > C1_AP_1.dFrom THEN C1_AP_1.dFrom\n" +
                "WHEN AP_2.dFrom > C1_AP_1.dFrom AND C1_AP_1.dTo > AP_2.dFrom THEN AP_2.dFrom\n" +
                "WHEN C1_AP_1.dFrom = AP_2.dFrom THEN C1_AP_1.dFrom\n" +
                "END AS dFrom,\n" +
                "CASE \n" +
                "WHEN C1_AP_1.dTo < AP_2.dTo AND C1_AP_1.dTo > AP_2.dFrom THEN C1_AP_1.dTo\n" +
                "WHEN AP_2.dTo < C1_AP_1.dTo AND AP_2.dTo > C1_AP_1.dFrom THEN AP_2.dTo\n" +
                "WHEN C1_AP_1.dTo = AP_2.dTo THEN C1_AP_1.dTo\n" +
                "END AS dTo\n" +
                "FROM C1_AP_1, AP_2\n" +
                "WHERE\n" +
                "((C1_AP_1.dFrom > AP_2.dFrom AND AP_2.dTo > C1_AP_1.dFrom) OR (AP_2.dFrom > C1_AP_1.dFrom AND C1_AP_1.dTo > AP_2.dFrom) OR (C1_AP_1.dFrom = AP_2.dFrom)) AND\n" +
                "((C1_AP_1.dTo < AP_2.dTo AND C1_AP_1.dTo > AP_2.dFrom) OR (AP_2.dTo < C1_AP_1.dTo AND AP_2.dTo > C1_AP_1.dFrom) OR (C1_AP_1.dTo = AP_2.dTo))";

//        String postgresql = "WITH C1 AS (\n" +
//                "SELECT dFrom, (dTo + interval '2' MINUTE) AS dTo \n" +
//                "FROM (\n" +
//                "SELECT \"timestamp\" AS dFrom,\n" +
//                "LEAD(\"timestamp\", 1) OVER (ORDER BY  \"timestamp\") AS dTo, \"value\" \n" +
//                "FROM \"public\".\"tb_measurement\") F\n" +
//                "WHERE \"value\" < 1.5 AND dTo IS NOT NULL AND (dTo - interval \'1 \' DAY) >= dFrom " +
//                "),\n" +
//                "C2 AS (\n" +
//                "SELECT dFrom, dTo \n" +
//                "FROM (\n" +
//                "SELECT \"timestamp\" AS dFrom,\n" +
//                "LEAD(\"timestamp\", 1) OVER (ORDER BY  \"timestamp\") AS dTo, \"value\" \n" +
//                "FROM \"public\".\"tb_measurement\") F\n" +
//                "WHERE \"value\" > 5 AND dTo IS NOT NULL AND (dTo - interval \'1 \' DAY) >= dFrom " +
//                ") \n" +
//                "SELECT C2.dFrom, C1.dTo \n" +
//                "FROM C1, C2\n" +
//                "WHERE\n" +
//                "C2.dFrom > C1.dFrom AND C1.dTo > C2.dFrom AND C1.dTo < C2.dTo AND C1.dTo > C2.dFrom";

//       String postgresql = "SELECT \"timestamp\" + interval '1' DAY AS dFrom FROM \"public\".\"tb_measurement_1\"";

//        String postgresql = "SELECT \"timestamp\" FROM \"public\".\"tb_measurement\"";

//        MaterializationService mService = MaterializationService.instance();
//        CalciteSchema calciteSchema = CalciteSchema.from(config.getDefaultSchema());
//        MaterializationKey key = mService.defineMaterialization(calciteSchema, null,
//                postgresql, null, null, true, false);
//        CalciteSchema.TableEntry entry = mService.checkValid(key);
//        RelBuilder relBuilder = RelBuilder.create(config);
//        relBuilder.scan(entry.name);
//        final RelNode node = relBuilder.build();
//        final RelToSqlConverter converter = new RelToSqlConverter(SqlDialect.DatabaseProduct.POSTGRESQL.getDialect());
//        final SqlNode sqlNode1 = converter.visitChild(0, node).asStatement();
//        System.out.println(sqlNode1.toSqlString(SqlDialect.DatabaseProduct.POSTGRESQL.getDialect()).getSql());

        System.out.println(postgresql);
//        b.getTypeFactory().getTypeSystem()
//                .getMaxPrecision(SqlTypeName.TIMESTAMP) >= 9;
        SqlNode sqlNode = this.planner.parse(postgresql);
        SqlNode validatedSqlNode = planner.validate(sqlNode);
        RelNode logicalPlan = planner.rel(validatedSqlNode).project();
        //RelNode transformedPlan = planner.transform(0, planner.getEmptyTraitSet().replace(EnumerableConvention.INSTANCE), logicalPlan);
        System.out.println("Logical Plan: ");
        System.out.println(RelOptUtil.toString(logicalPlan));

        String reverseSQL = convert(logicalPlan);
        System.out.println(reverseSQL);
    }


    private String convert(RelNode node){
        SqlDialect dialect = SqlDialect.DatabaseProduct.POSTGRESQL.getDialect();
        RelToSqlConverter converter = new RelToSqlConverter(dialect);
        SqlNode sqlNode = converter.visitChild(0, node).asStatement();
        String result = sqlNode.toSqlString(dialect).getSql();
        //SqlImplementor.Result res = converter.visit(node);
        return result;
    }

}