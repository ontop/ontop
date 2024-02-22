package it.unibz.inf.ontop.docker;

import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.answering.connection.OntopStatement;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.query.RDF4JQueryFactory;
import it.unibz.inf.ontop.query.RDF4JSelectQuery;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.algebra.Join;
import org.eclipse.rdf4j.query.algebra.Projection;
import org.eclipse.rdf4j.query.algebra.ProjectionElem;
import org.eclipse.rdf4j.query.algebra.ProjectionElemList;
import org.eclipse.rdf4j.query.algebra.QueryRoot;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.impl.MapBindingSet;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class Issue791Test {
  private static final String owlFile = "src/test/resources/issue791/ontology.ttl";
  private static final String r2rmlFile = "src/test/resources/issue791/mapping.ttl";
  private static final String propertiesFile = "src/test/resources/issue791/mapping.properties";
  private static final String databaseFile = "src/test/resources/issue791/database.sql";

  @BeforeClass
  public static void before() throws SQLException, FileNotFoundException {
    Connection sqlConnection = DriverManager.getConnection("jdbc:h2:mem:questjunitdb", "sa", "");
    try (java.sql.Statement s = sqlConnection.createStatement()) {
      String text = new Scanner(new File(databaseFile)).useDelimiter("\\A").next();
      s.execute(text);
    }
  }

  @Test
  public void testCrossJoinInObjectMap() throws Exception {
    OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
        .r2rmlMappingFile(r2rmlFile)
        .ontologyFile(owlFile)
        .propertyFile(propertiesFile)
        .enableTestMode()
        .build();

    try (OntopQueryEngine ontopQueryEngine = configuration.loadQueryEngine()) {
      ontopQueryEngine.connect();

      try (OntopConnection connection = ontopQueryEngine.getConnection()) {
        try (OntopStatement statement = connection.createStatement()) {
          String hasMeasurementUnitLabelIri = "http://purl.obolibrary.org/obo/IAO_0000039";

          String expectedCrossJoinWithConst = "ans1(sub, obj)\n"
              + "CONSTRUCT [sub, obj] [sub/RDF(http://purl.obolibrary.org/obo/IAO_0000032/{}(INTEGERToTEXT(id0m11)),IRI), obj/RDF(v1m2,IRI)]\n"
              + "   NATIVE [id0m11, v1m2]\n"
              + "SELECT DISTINCT V5.\"id0m11\" AS \"id0m11\", CASE     WHEN V11.\"v0\" = 0 THEN 'http://purl.obolibrary.org/obo/IAO_0000003/kg'\n"
              + "    WHEN V11.\"v0\" = 1 THEN ('http://purl.obolibrary.org/obo/IAO_0000003/' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(V11.\"unit2m1\", '%', '%25'), ' ', '%20'), '!', '%21'), '\"', '%22'), '#', '%23'), '$', '%24'), '&', '%26'), '''', '%27'), '(', '%28'), ')', '%29'), '*', '%2A'), '+', '%2B'), ',', '%2C'), '/', '%2F'), ':', '%3A'), ';', '%3B'), '<', '%3C'), '=', '%3D'), '>', '%3E'), '?', '%3F'), '@', '%40'), '[', '%5B'), '\\', '%5C'), ']', '%5D'), '^', '%5E'), '`', '%60'), '{', '%7B'), '|', '%7C'), '}', '%7D'))\n"
              + "    ELSE NULL \n"
              + "END AS \"v1m2\"\n"
              // The following union is the definition of both classes
              + "FROM ((SELECT V1.\"id\" AS \"id0m11\"\n"
              + "FROM \"weight_measurement\" V1\n"
              + "WHERE V1.\"weight\" IS NOT NULL\n"
              + ")UNION ALL \n"
              + "(SELECT V3.\"id\" AS \"id0m11\"\n"
              + "FROM \"height_measurement\" V3\n"
              + "WHERE V3.\"height\" IS NOT NULL\n"
              // The following query is the cross-join of V6 and the constant
              // (the constant value is added by the case in the projection)
              + ")) V5, ((SELECT V6.\"id\" AS \"id0m0\", NULL AS \"unit2m1\", 0 AS \"v0\"\n"
              + "FROM \"weight_measurement\" V6\n"
              + "WHERE V6.\"weight\" IS NOT NULL\n"
              + ")UNION ALL \n"
              // The following query is the cross-join of V8 and V9
              + "(SELECT V8.\"id\" AS \"id0m0\", V9.\"unit\" AS \"unit2m1\", 1 AS \"v0\"\n"
              + "FROM \"height_measurement\" V8, \"height_measurement\" V9\n"
              + "WHERE (V9.\"unit\" IS NOT NULL AND V8.\"height\" IS NOT NULL)\n"
              + ")) V11\n"
              + "WHERE V5.\"id0m11\" = V11.\"id0m0\"";
          String actualCrossJoinWithConst = getExecutableQuery(
              "http://purl.obolibrary.org/obo/IAO_0000032",
              "http://purl.obolibrary.org/obo/IAO_0000003",
              hasMeasurementUnitLabelIri,
              configuration,
              statement);
          Assertions.assertEquals(expectedCrossJoinWithConst, actualCrossJoinWithConst.trim());
        }
      }
    }
  }

  private static String getExecutableQuery(String subIRI, String objIRI, String opIRI,
      OntopSQLOWLAPIConfiguration configuration, OntopStatement statement)
      throws OntopReformulationException {
    SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();

    StatementPattern subStatement = new StatementPattern(new Var("sub"),
        new Var("rdf_type_uri",
            valueFactory.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), true,
            true),
        new Var("sub_uri",
            valueFactory.createIRI(subIRI), true,
            true));

    StatementPattern objStatement = new StatementPattern(new Var("obj"),
        new Var("rdf_type_uri",
            valueFactory.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), true,
            true),
        new Var("obj_uri",
            valueFactory.createIRI(objIRI), true,
            true));

    StatementPattern relStatement = new StatementPattern(new Var("sub"),
        new Var("op_uri",
            valueFactory.createIRI(opIRI), true, true),
        new Var("obj"));

    Join subAndObjJoin = new Join(subStatement, objStatement);
    Join relJoin = new Join(subAndObjJoin, relStatement);

    Projection projection = new Projection(relJoin,
        new ProjectionElemList(new ProjectionElem("sub"),
            new ProjectionElem("obj")));
    QueryRoot queryRoot = new QueryRoot(projection);

    RDF4JQueryFactory factrdf4JQueryFactory = configuration.getInjector().getInstance(
        RDF4JQueryFactory.class);
    RDF4JSelectQuery rdf4JSelectQuery = factrdf4JQueryFactory.createSelectQuery(
        String.format("<%s> <%s> <%s>", subIRI, opIRI, objIRI),
        new ParsedTupleQuery(queryRoot), new MapBindingSet());

    IQ firstExecutableQuery = statement.getExecutableQuery(rdf4JSelectQuery,
        ImmutableMultimap.of());
    return firstExecutableQuery.toString();
  }
}
