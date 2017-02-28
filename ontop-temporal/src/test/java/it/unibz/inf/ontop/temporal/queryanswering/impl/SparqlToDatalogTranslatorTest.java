package it.unibz.inf.ontop.temporal.queryanswering.impl;


import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.input.InputQueryFactory;
import it.unibz.inf.ontop.answering.input.impl.InputQueryFactoryImpl;
import it.unibz.inf.ontop.answering.input.impl.RDF4JInputQueryFactoryImpl;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.UriTemplateMatcher;
import it.unibz.inf.ontop.owlapi.OntopOWLException;
import it.unibz.inf.ontop.owlrefplatform.core.ExecutableQuery;
import it.unibz.inf.ontop.owlrefplatform.core.SQLExecutableQuery;
import it.unibz.inf.ontop.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator;
import it.unibz.inf.ontop.owlrefplatform.core.translator.InternalSparqlQuery;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import org.junit.Test;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.QueryParser;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.util.Optional;


public class SparqlToDatalogTranslatorTest {

    private static final String OWL_FILE = "src/test/resources/booktutorial.owl";
    private static final String OBDA_FILE = "src/test/resources/booktutorial.obda";
    private static final String PROPERTY_FILE = "src/test/resources/booktutorial.properties";

    @Test
    public void test1() throws MalformedQueryException, OntopUnsupportedInputQueryException {

        String query = "PREFIX  mw:  <http://mesowest.org/>\n"
                + "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "SELECT  ?hurr "
                + "WHERE \n"
                + "{  ?hurr rdf:type mw:Hurricane.}";

//        QueryParser parser = QueryParserUtil.createParser(QueryLanguage.SPARQL);
//        ParsedQuery pq = parser.parseQuery(query, null);
//
//        SparqlAlgebraToDatalogTranslator translator = new SparqlAlgebraToDatalogTranslator(
//                new UriTemplateMatcher(), Optional.empty());
//
//        InternalSparqlQuery program = translator.translate(pq);
//        System.out.println(program);

//        Injector injector = config.getInjector();
//
//        try {
//            QuestOWL reasoner = factory.createReasoner(config);
//            OntopOWLConnection connection = reasoner.getConnection();
//
//            //QuestOWLStatement st = null;
//            //st.getRewritingRendering(query);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//
//        QuestOWLFactory factory = new QuestOWLFactory();
//        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
//                .nativeOntopMappingFile(OBDA_FILE)
//                .ontologyFile(OWL_FILE)
//                .propertyFile(PROPERTY_FILE)
//                .build();

//        try {
//            QuestOWL reasoner = factory.createReasoner(config);
//        } catch (OWLOntologyCreationException e) {
//            e.printStackTrace();
//        }

//        // Now we are ready for querying
//        OntopOWLConnection conn = reasoner.getConnection();
//        OntopOWLStatement st = conn.createStatement();
//        String sql;
//
//        try {
//            ExecutableQuery executableQuery = st.getExecutableQuery(query);
//            if (! (executableQuery instanceof SQLExecutableQuery))
//                throw new IllegalStateException("A SQLExecutableQuery was expected");
//            sql = ((SQLExecutableQuery)executableQuery).getSQL();
//            QuestOWLResultSet rs = st.executeSelectQuery(query);
//
//        } catch (Exception e) {
//            throw e;
//        } finally {
//            conn.close();
//            reasoner.dispose();
//        }
    }


}
