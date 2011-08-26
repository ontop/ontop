package it.unibz.krdb.obda.SemanticIndex;


import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.SubClassAxiomImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.SubPropertyAxiomImpl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YAGOTest {
    private final static String dataFile = "yago2core_20110315.n3";
    private static final Logger log = LoggerFactory.getLogger(YAGOTest.class);

    private static final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();
    private static final OntologyFactory descFactory = new OntologyFactoryImpl();


    public static void main(String[] args) throws IOException, URISyntaxException {

        Ontology onto = parse_tbox(dataFile);
        DAG dag = new DAG(onto);

    }

    private static Ontology parse_tbox(String filename) throws IOException, URISyntaxException {
        BufferedReader triples = new BufferedReader
                (new InputStreamReader(new FileInputStream(filename), "UTF-8"));

        String line;
        long tbox_count = 0;

        String subject;
        String object;
        String predicate;

        Pattern pattern = Pattern.compile("<(.+?)>\\s(.+?)\\s[<\"](.+?)[>\"]\\s\\.");
        Matcher matcher;

        Ontology onto = OntologyFactoryImpl.getInstance().createOntology(URI.create(""));

        while ((line = triples.readLine()) != null) {
//            String result = URLDecoder.decode(line, "UTF-8");
            if (line.startsWith("@")) {
                log.debug(line);
                continue;
            }
            matcher = pattern.matcher(line);

            boolean matchFound = matcher.find();

            if (matchFound) {
                subject = matcher.group(1);
                predicate = matcher.group(2);
                object = matcher.group(3);

                if ("rdfs:range".equals(predicate)) {
                    tbox_count++;
                    Predicate ps = predicateFactory.getPredicate(new URI(subject), 2);
                    Predicate po = predicateFactory.getPredicate(new URI(object), 1);

                    ClassDescription rs = descFactory.getPropertySomeRestriction(ps, true);
                    ClassDescription co = descFactory.createClass(po);

                    onto.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(rs, co));

                } else if ("rdfs:domain".equals(predicate)) {
                    tbox_count++;
                    Predicate ps = predicateFactory.getPredicate(new URI(subject), 2);
                    Predicate po = predicateFactory.getPredicate(new URI(object), 1);

                    ClassDescription rs = descFactory.getPropertySomeRestriction(ps, false);
                    ClassDescription co = descFactory.createClass(po);

                    onto.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(rs, co));

                } else if ("rdf:type".equals(predicate)) {
                    // a rdf:type A |= A(a)
                    Predicate po = predicateFactory.getPredicate(new URI(object), 1);
                    ClassDescription co = descFactory.createClass(po);

                    onto.addConcept(co);

                } else if ("rdfs:subClassOf".equals(predicate)) {
                    tbox_count++;
//                    log.debug("{} {}", subject, object);
                    Predicate ps = predicateFactory.getPredicate(new URI(subject), 1);
                    Predicate po = predicateFactory.getPredicate(new URI(object), 1);
                    ClassDescription cs = descFactory.createClass(ps);
                    ClassDescription co = descFactory.createClass(po);
                    onto.addAssertion(OntologyFactoryImpl.getInstance().createSubClassAxiom(cs, co));

                } else if ("rdfs:subPropertyOf".equals(predicate)) {
                    tbox_count++;
//                    log.debug("{} {}", subject, object);
                    Predicate ps = predicateFactory.getPredicate(new URI(subject), 1);
                    Predicate po = predicateFactory.getPredicate(new URI(object), 1);
                    Property rs = descFactory.createProperty(ps);
                    Property ro = descFactory.createProperty(po);
                    onto.addAssertion(OntologyFactoryImpl.getInstance().createSubPropertyAxiom(rs, ro));

                } else {
//                    log.debug(predicate);

                }


            } else {
                log.debug("Not matched line {}", line);
            }

        }
        return onto;
    }

    private void parse_abox(String filenname) {

    }


}
