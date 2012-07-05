package it.unibz.krdb.obda.parser;

import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.querymanager.QueryController;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;


public class SQL99ParserFileTest extends TestCase
{
  private static final String ROOT = "src/test/resources/scenario/";

  final static Logger log = LoggerFactory.getLogger(SQL99ParserFileTest.class);

  //@Test
  public void testStockExchange_Pgsql() throws URISyntaxException {
    OBDAModel model = load(ROOT + "virtual/stockexchange-pgsql.owl");
    execute(model, new URI("RandBStockExchange"));
  }

  //@Test
  public void testImdbGroup4_Pgsql() throws URISyntaxException {
    OBDAModel model = load(ROOT + "virtual/imdb-group4-pgsql.owl");
    execute(model, new URI("kbdb_imdb"));
  }

  //@Test
  public void testImdbGroup4_Oracle() throws URISyntaxException {
    OBDAModel model = load(ROOT + "virtual/imdb-group4-oracle.owl");
    execute(model, new URI("kbdb_imdb"));
  }

  //@Test
  public void testAdolenaSlim_Pgsql() throws URISyntaxException {
    OBDAModel model = load(ROOT + "virtual/adolena-slim-pgsql.owl");
    execute(model, new URI("nap"));
  }

  //@Test
  public void testBooksApril20_Pgsql() throws URISyntaxException {
    OBDAModel model = load(ROOT + "virtual/books-april20-pgsql.owl");
    execute(model, new URI("datasource"));
  }

  //@Test
  public void testHgt090303_Mysql() throws URISyntaxException {
    OBDAModel model = load(ROOT + "virtual/hgt-090303-mysql.owl");
    execute(model, new URI("HGT"));
  }

  //@Test
  public void testHgt090324_Pgsql() throws URISyntaxException {
    OBDAModel model = load(ROOT + "virtual/hgt-090324-pgsql.owl");
    execute(model, new URI("HGT"));
  }

  //@Test
  public void testHgt091007_Oracle() throws URISyntaxException {
    OBDAModel model = load(ROOT + "virtual/hgt-091007-oracle.owl");
    execute(model, new URI("HGT"));
  }

  //@Test
  public void testMpsOntologiaGcc_DB2() throws URISyntaxException {
    OBDAModel model = load(ROOT + "virtual/mps-ontologiagcc-db2.owl");
    execute(model, new URI("sourceGCC"));
  }

  //@Test
  public void testOperationNoyauV5_Oracle() throws URISyntaxException {
    OBDAModel model = load(ROOT + "virtual/operation-noyau-v5-oracle.owl");
    execute(model, new URI("PgmOpe"));
  }

  //@Test
  public void testOperationNoyauV6_Oracle() throws URISyntaxException {
    OBDAModel model = load(ROOT + "virtual/operation-noyau-v6-oracle.owl");
    execute(model, new URI("CORIOLIS-CRAQ"));
    execute(model, new URI("PROGOS-CRAQ"));
  }

  //------- Utility methods

  private void execute(OBDAModel model, URI identifier) {

    OBDAModel controller = model;
    Hashtable<URI, ArrayList<OBDAMappingAxiom>> mappingList = controller.getMappings();

    ArrayList<OBDAMappingAxiom> mappings = mappingList.get(identifier);

    log.debug("=========== " + identifier + " ===========");
    for(OBDAMappingAxiom axiom : mappings) {
      String query = axiom.getSourceQuery().toString();
      boolean result = parse(query);

      if(!result) {
        log.error("Unsupported query: " + query);
      }
      assertTrue(result);
    }
  }

  private OBDAModel load(String file) {

    final URI ontologyUri = new File(file).toURI();
    final String obdafile = file.substring(0, file.length()-3) + "obda";
    final URI obdaUri = new File(obdafile).toURI();

    OBDADataFactory factory = OBDADataFactoryImpl.getInstance();
    OBDAModel model = factory.getOBDAModel();
    DataManager dataManager = new DataManager(model, new QueryController());
    PrefixManager prefixManager = model.getPrefixManager();
    try {
        dataManager.loadOBDADataFromURI(obdaUri, ontologyUri, prefixManager);
    } catch (IOException e) {
        log.debug(e.toString());
    } catch (SAXException e) {
        log.debug(e.toString());
    }
    return model;
  }

  private static boolean parse(String input) {
    ANTLRStringStream inputStream = new ANTLRStringStream(input);
    SQL99Lexer lexer = new SQL99Lexer(inputStream);
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    SQL99Parser parser = new SQL99Parser(tokenStream);

    try {
      parser.parse();
    }
    catch (RecognitionException e) {
      log.debug(e.getMessage());
    }

    if (parser.getNumberOfSyntaxErrors() != 0) {
      return false;
    }
    return true;
  }
}
