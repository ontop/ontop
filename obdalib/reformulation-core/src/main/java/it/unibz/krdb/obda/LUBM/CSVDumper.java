package it.unibz.krdb.obda.LUBM;


import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.abox.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.abox.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.abox.DAGNode;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicDescriptionFactory;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

public class CSVDumper {
    private final Logger log = LoggerFactory.getLogger(CSVDumper.class);
    private static final int maxFilePerUni = 30;

    public String classFile;
    public String roleFile;

    private final FileWriter clsWriter;
    private final FileWriter roleWriter;
    private final DAG dag;

    private long clsCount = 0;
    private long roleCount = 0;
    private String dataDir;

    private static final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();
    private static final DescriptionFactory descFactory = new BasicDescriptionFactory();

    public CSVDumper(DAG dag, String dataDir) throws IOException {
        classFile = dataDir + "classes.csv";
        roleFile = dataDir + "rolles.csv";

        clsWriter = new FileWriter(classFile);
        roleWriter = new FileWriter(roleFile);
        this.dag = dag;
        this.dataDir = dataDir;
    }


    public void dump(int uniCount) throws Exception {


        final long startTime = System.nanoTime();
        final long endTime;

        String aboxfmt = dataDir + "University%s_%s.owl";
        String aboxpath;
        File f;

        for (int i = 0; i < uniCount; ++i) {
            for (int j = 0; j < maxFilePerUni; ++j) {
                aboxpath = String.format(aboxfmt, i, j);
                f = new File(aboxpath);
                if (f.exists()) {
                    loadRDF(f);
                } else {
                    break;
                }
            }
            clsWriter.flush();
            roleWriter.flush();
        }


        endTime = System.nanoTime();
        final long duration = endTime - startTime;

        log.info("Dumping ABox took: {}", duration * 1.0e-9);
        log.info("Total number of classes: {}, rolles: {}.", clsCount, roleCount);

    }

    private void loadRDF(File f) throws Exception {


        final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        final OWLOntology ontology = manager.loadOntologyFromPhysicalURI((f).toURI());

        for (OWLIndividualAxiom axiom : ontology.getIndividualAxioms()) {

            if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
                OWLObjectPropertyAssertionAxiom objAxiom = (OWLObjectPropertyAssertionAxiom) axiom;

                OWLIndividual s = objAxiom.getSubject();
                OWLObjectPropertyExpression p = objAxiom.getProperty();
                OWLIndividual o = objAxiom.getObject();

                Predicate propPred = predicateFactory.getPredicate(URI.create(p.asOWLObjectProperty().getURI().toString()), 2);
                RoleDescription propDesc = descFactory.getRoleDescription(propPred);
                DAGNode node = dag.getRoleNode(propDesc);

                if (node == null) {
                    continue;
                }
                int idx = node.getIndex();

                dumpRole(s.getURI().toString(), o.getURI().toString(), idx);
                roleCount++;

            } else if (axiom instanceof OWLDataPropertyAssertionAxiom) {
//                OWLDataPropertyAssertionAxiom dataAxiom = (OWLDataPropertyAssertionAxiom) axiom;
//
//                OWLIndividual s = dataAxiom.getSubject();
//                OWLDataPropertyExpression p = dataAxiom.getProperty();
//                OWLConstant o = dataAxiom.getObject();
//                log.info(p.asOWLDataProperty().getURI().toString());
//                int idx = dag.getRoleNode(p.asOWLDataProperty().getURI().toString()).getIndex();
//
//                dumpRole(s.getURI().toString(), o.getLiteral(), idx);
//                roleCount++;

            } else if (axiom instanceof OWLClassAssertionAxiom) {
                OWLClassAssertionAxiom clsAxiom = (OWLClassAssertionAxiom) axiom;

                OWLIndividual ind = clsAxiom.getIndividual();
                OWLDescription cls = clsAxiom.getDescription();

                Predicate clsPred = predicateFactory.getPredicate(URI.create(cls.asOWLClass().getURI().toString()), 1);
                ConceptDescription clsDesc = descFactory.getAtomicConceptDescription(clsPred);
                DAGNode clsNode = dag.getClassNode(clsDesc);

                if (clsNode == null) {
                    continue;
                }
                int idx = clsNode.getIndex();

                dumpClass(ind.getURI().toString(), idx);
                clsCount++;
            }
        }
    }

    private void dumpRole(String s, String o, int idx) throws IOException {
        roleWriter.write(String.format("%s\t%s\t%d\n", s, o, idx));
    }

    private void dumpClass(String s, int idx) throws IOException {
        clsWriter.write(String.format("%s\t%d\n", s, idx));
    }

    public static void main(String args[]) throws Exception {
        String path = args[0];
        int universities = Integer.valueOf(args[1]);

        TBoxLoader helper = new TBoxLoader(path);
        DAG isa = new DAG(helper.loadOnto());
//        isa.index();
        DAG pureIsa = DAGConstructor.filterPureISA(isa);
        pureIsa.index();
        CSVDumper dumper = new CSVDumper(pureIsa, path);
        dumper.dump(universities);
    }


}
