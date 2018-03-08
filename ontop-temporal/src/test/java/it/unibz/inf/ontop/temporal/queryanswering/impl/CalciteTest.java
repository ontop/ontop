package it.unibz.inf.ontop.temporal.queryanswering.impl;


import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.plan.*;
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
                .traitDefs((List<RelTraitDef>) null);

    }

    public static void main(String[] args) throws ClassNotFoundException, RelConversionException, SqlParseException, ValidationException {
        CalciteTest exampler = new CalciteTest(true);
        exampler.runExamplesFromSQL();
    }

    private void runExamplesFromSQL() throws ClassNotFoundException, RelConversionException, SqlParseException, ValidationException {
        final FrameworkConfig config = config().build();
        this.planner = Frameworks.getPlanner(config);

//        String postgresql = "WITH C1_AP_1 AS (\n" +
//                "SELECT dFrom, dTo \n" +
//                "FROM (\n" +
//                "SELECT \"timestamp\" AS dFrom,\n" +
//                "LEAD(\"timestamp\", 1) OVER (ORDER BY  \"timestamp\") AS dTo, \"value\" \n" +
//                "FROM \"public\".\"tb_measurement\") F\n" +
//                "WHERE \"value\" > 1.5 AND dTo IS NOT NULL " +
//                "),\n" +
//                "C2_AP_1 (Start_ts, End_ts, ts) AS (\n" +
//                "SELECT 1, 0 , dFrom\n" +
//                "FROM C1_AP_1 \n" +
//                "UNION ALL\n" +
//                "SELECT 0, 1, dTo\n" +
//                "FROM C1_AP_1  \n" +
//                "),\n" +
//                "C3_AP_1 AS (\n" +
//                "SELECT \n" +
//                "SUM(Start_ts) OVER (ORDER BY ts, End_ts ROWS UNBOUNDED PRECEDING) AS Crt_Total_ts_1,\n" +
//                "SUM(End_ts) OVER (ORDER BY ts, End_ts ROWS UNBOUNDED PRECEDING) AS Crt_Total_ts_2,\n" +
//                "SUM(Start_ts) OVER (ORDER BY ts, End_ts ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING) AS Prv_Total_ts_1,\n" +
//                "SUM(End_ts) OVER (ORDER BY ts, End_ts ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING) AS Prv_Total_ts_2,\n" +
//                "ts\n" +
//                "FROM C2_AP_1\n" +
//                "),\n" +
//                "\n" +
//                "C4_AP_1 AS (\n" +
//                "SELECT (Crt_Total_ts_1 - Crt_Total_ts_2) AS Crt_Total_ts, (Prv_Total_ts_1 - Prv_Total_ts_2) AS Prv_Total_ts, ts\n" +
//                "FROM C3_AP_1\n" +
//                "WHERE (Crt_Total_ts_1 - Crt_Total_ts_2) = 0 OR (Prv_Total_ts_1 - Prv_Total_ts_2) = 0 OR (Prv_Total_ts_1 - Prv_Total_ts_2) IS NULL\n" +
//                "),\n" +
//                "DIAMOND_AP_1 AS ( \n" +
//                "SELECT prevTs  as dFrom, ts AS dTo FROM (\n" +
//                "SELECT LAG(ts,1) OVER (ORDER BY ts, crt_total_ts) As prevTs,\n" +
//                "ts,\n" +
//                "Crt_Total_ts\n" +
//                "FROM C4_AP_1) F \n" +
//                "WHERE Crt_Total_ts = 0 ), \n" +
//                "AP_2 AS (\n" +
//                "SELECT dFrom, dTo \n" +
//                "FROM C1_AP_1 \n" +
//                ") \n" +
//                "SELECT\n" +
//                "CASE \n" +
//                "WHEN DIAMOND_AP_1.dFrom > AP_2.dFrom AND AP_2.dTo > DIAMOND_AP_1.dFrom THEN DIAMOND_AP_1.dFrom\n" +
//                "WHEN AP_2.dFrom > DIAMOND_AP_1.dFrom AND DIAMOND_AP_1.dTo > AP_2.dFrom THEN AP_2.dFrom\n" +
//                "WHEN DIAMOND_AP_1.dFrom = AP_2.dFrom THEN DIAMOND_AP_1.dFrom\n" +
//                "END AS dFrom,\n" +
//                "CASE \n" +
//                "WHEN DIAMOND_AP_1.dTo < AP_2.dTo AND DIAMOND_AP_1.dTo > AP_2.dFrom THEN DIAMOND_AP_1.dTo\n" +
//                "WHEN AP_2.dTo < DIAMOND_AP_1.dTo AND AP_2.dTo > DIAMOND_AP_1.dFrom THEN AP_2.dTo\n" +
//                "WHEN DIAMOND_AP_1.dTo = AP_2.dTo THEN DIAMOND_AP_1.dTo\n" +
//                "END AS dTo\n" +
//                "FROM DIAMOND_AP_1, AP_2\n" +
//                "WHERE\n" +
//                "((DIAMOND_AP_1.dFrom > AP_2.dFrom AND AP_2.dTo > DIAMOND_AP_1.dFrom) OR (AP_2.dFrom > DIAMOND_AP_1.dFrom AND DIAMOND_AP_1.dTo > AP_2.dFrom) OR (DIAMOND_AP_1.dFrom = AP_2.dFrom)) AND\n" +
//                "((DIAMOND_AP_1.dTo < AP_2.dTo AND DIAMOND_AP_1.dTo > AP_2.dFrom) OR (AP_2.dTo < DIAMOND_AP_1.dTo AND AP_2.dTo > DIAMOND_AP_1.dFrom) OR (DIAMOND_AP_1.dTo = AP_2.dTo))";


        String postgresql = "SELECT \"timestamp\" + interval '1' DAY AS dFrom FROM \"public\".\"tb_measurement_1\"";

//        String postgresql = "SELECT \"timestamp\" FROM \"public\".\"tb_measurement\"";

        System.out.println(postgresql);
//        b.getTypeFactory().getTypeSystem()
//                .getMaxPrecision(SqlTypeName.TIMESTAMP) >= 9;
        assert planner.getTypeFactory().getTypeSystem().getMaxPrecision(SqlTypeName.TIMESTAMP) >= 3;
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