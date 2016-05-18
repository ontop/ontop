package it.unibz.inf.ontop.planning;

import com.google.common.base.Joiner;
import com.google.common.collect.LinkedListMultimap;
import it.unibz.krdb.obda.exception.InvalidMappingException;
import it.unibz.krdb.obda.exception.InvalidPredicateDeclarationException;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConfiguration;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserUtil;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OntopPlanning {

    QuestOWLStatement st;


    public OntopPlanning(String owlfile, String obdafile) throws OWLException, IOException, InvalidMappingException, InvalidPredicateDeclarationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

        OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
        OBDAModel obdaModel = fac.getOBDAModel();
        ModelIOManager ioManager = new ModelIOManager(obdaModel);
        ioManager.load(obdafile);

        QuestPreferences pref = new QuestPreferences();

        // pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
        // pref.setCurrentValueOf(QuestPreferences.REWRITE, QuestConstants.TRUE);

        QuestOWLFactory factory = new QuestOWLFactory();
        QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).preferences(pref).build();
        QuestOWL reasoner = factory.createReasoner(ontology, config);

        QuestOWLConnection qconn = reasoner.getConnection();
        st = qconn.createStatement();

    }

    public String getSQLForFragments(List<String> fragments) throws OWLException, MalformedQueryException {

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ");
        QueryParser parser = QueryParserUtil.createParser(QueryLanguage.SPARQL);

        int fragCounter = 0;

        LinkedListMultimap<String, Integer> variableOccurrences = LinkedListMultimap.create();

        for (String f : fragments) {
            fragCounter++;
            if (fragCounter != 1) {
                sqlBuilder.append(",");
            }

            String s = st.getUnfolding(f);

            final String FRAGMENT_PREFIX = "f_";
            sqlBuilder.append("(").append(s).append(") " + FRAGMENT_PREFIX).append(fragCounter);

            ParsedQuery pq = parser.parseQuery(f, null);
            Set<String> bindingNames = pq.getTupleExpr().getBindingNames();

            int finalI = fragCounter;
            bindingNames.forEach(name -> variableOccurrences.put(name, finalI));
        }


        List<String> joinConditions = new ArrayList<>();

        for (String v : variableOccurrences.keySet()) {
            List<Integer> occurrences = variableOccurrences.get(v);
            if (occurrences.size() > 1) {
                for (int i = 0; i < occurrences.size(); i++) {
                    for (int j = i + 1; j < occurrences.size(); j++){
                        joinConditions.add(String.format("f_%d.%s = f_%d.%s", occurrences.get(i), v, occurrences.get(j),v));
                    }
                }
            }
        }

        if(joinConditions.size() > 0){
            sqlBuilder.append("\n WHERE ");
            String condition = Joiner.on(" AND ").join(joinConditions);
            sqlBuilder.append(condition);
        }

        return sqlBuilder.toString();
    }

    public static void main(String[] args) throws InvalidMappingException, OWLException, InvalidPredicateDeclarationException, IOException {


//        st.close();
//
//        reasoner.dispose();

    }


}
