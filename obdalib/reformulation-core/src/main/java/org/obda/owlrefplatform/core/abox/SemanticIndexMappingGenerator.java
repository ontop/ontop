package org.obda.owlrefplatform.core.abox;


import inf.unibz.it.obda.exception.DuplicateMappingException;
import inf.unibz.it.obda.model.DataSource;
import inf.unibz.it.obda.model.OBDAModel;
import inf.unibz.it.obda.model.Atom;
import inf.unibz.it.obda.model.OBDADataFactory;
import inf.unibz.it.obda.model.OBDAMappingAxiom;
import inf.unibz.it.obda.model.Predicate;
import inf.unibz.it.obda.model.Query;
import inf.unibz.it.obda.model.Term;
import inf.unibz.it.obda.model.impl.AtomImpl;
import inf.unibz.it.obda.model.impl.CQIEImpl;
import inf.unibz.it.obda.model.impl.OBDADataFactoryImpl;
import inf.unibz.it.obda.model.impl.RDBMSMappingAxiomImpl;
import inf.unibz.it.obda.model.impl.SQLQueryImpl;

import java.net.URI;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate the mappings for DAG
 *
 * @author Sergejs Pugac
 */
public class SemanticIndexMappingGenerator {

    private final Logger log = LoggerFactory.getLogger(SemanticIndexMappingGenerator.class);

    private final static OBDADataFactory termFactory = OBDADataFactoryImpl.getInstance();
    private final static OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();

    private int mapcounter;
    private final OBDAModel apic;
    private final DAG dag;
    private final DataSource ds;

    public SemanticIndexMappingGenerator(DataSource ds, OBDAModel apic, DAG dag) {
    	this.ds = ds;
        this.apic = apic;
        this.dag = dag;
        mapcounter = 1;
    }

    /**
     * Generate mappings for DAG
     *
     * @throws DuplicateMappingException error creating mappings
     */
    public void build() throws DuplicateMappingException {
        log.debug("Generating mappings for DAG {}", dag);
        //Map<String, Set<DAGNode>> class_descdendants = DAGOperations.buildDescendants(dag.getClassIndex());
        for (DAGNode node : dag.getClassIndex().values()) {

            if (node.getUri().startsWith(DAG.owl_exists) || node.getUri().startsWith(DAG.owl_inverse_exists)) {
                continue;
            }

            String uri = node.getUri();
            String tablename = ABoxSerializer.class_table;
            String projection = "URI as X";
            SemanticIndexRange range = node.getRange();
            insert_unary_mapping(uri, projection, tablename, range);

            // Handle equivalent nodes
            for (DAGNode equi : node.getEquivalents()) {
                insert_unary_mapping(equi.getUri(), projection, tablename, range);
            }

            // check if has child exists(R)
            for (DAGNode descendant : node.descendans) {
                String child_uri;
                String projection_inverse;
                SemanticIndexRange range_inverse;

                if (descendant.getUri().startsWith(DAG.owl_exists_obj)) {
                    child_uri = descendant.getUri().substring(DAG.owl_exists_obj.length());
                    tablename = ABoxSerializer.objectprop_table;

                    projection = "URI1 as X";
                    projection_inverse = "URI2 as X";

                    range = dag.getObjectPropertyIndex().get(child_uri).getRange();
                    range_inverse = dag.getObjectPropertyIndex().get(DAG.owl_inverse + child_uri).getRange();
                } else if (descendant.getUri().startsWith(DAG.owl_inverse_exists_obj)) {
                    child_uri = descendant.getUri().substring(DAG.owl_inverse_exists_obj.length());
                    tablename = ABoxSerializer.objectprop_table;

                    projection = "URI2 as X";
                    projection_inverse = "URI1 as X";

                    range = dag.getObjectPropertyIndex().get(child_uri).getRange();
                    range_inverse = dag.getObjectPropertyIndex().get(DAG.owl_inverse + child_uri).getRange();
                } else if (descendant.getUri().startsWith(DAG.owl_exists_data)) {
                    child_uri = descendant.getUri().substring(DAG.owl_exists_data.length());
                    tablename = ABoxSerializer.dataprop_table;

                    projection = "URI as X";
                    projection_inverse = "LITERAL as X";

                    range = dag.getDataPropertyIndex().get(child_uri).getRange();
                    range_inverse = dag.getDataPropertyIndex().get(DAG.owl_inverse + child_uri).getRange();
                } else if (descendant.getUri().startsWith(DAG.owl_inverse_exists_data)) {
                    child_uri = descendant.getUri().substring(DAG.owl_inverse_exists_data.length());
                    tablename = ABoxSerializer.dataprop_table;

                    projection = "LITERAL as X";
                    projection_inverse = "LITERAL as X";

                    range = dag.getDataPropertyIndex().get(child_uri).getRange();
                    range_inverse = dag.getDataPropertyIndex().get(DAG.owl_inverse + child_uri).getRange();
                } else {
                    // Ignore concept descendants
                    continue;
                }

                insert_unary_mapping(uri, projection, tablename, range);
                insert_unary_mapping(uri, projection_inverse, tablename, range_inverse);

            }
        }
        for (DAGNode node : dag.getObjectPropertyIndex().values()) {
            String uri = node.getUri();
            String projection = " URI1 as X, URI2 as Y ";

            if (uri.startsWith(DAG.owl_inverse)) {
                uri = uri.substring(DAG.owl_inverse.length());
                projection = "URI2 as X, URI1 as Y";
            }
            String table = ABoxSerializer.objectprop_table;
            SemanticIndexRange range = node.getRange();

            insert_binary_mapping(uri, projection, table, range);

            // Handle equivalent nodes
            for (DAGNode equi : node.getEquivalents()) {
                insert_binary_mapping(equi.getUri(), projection, table, range);
            }
        }

        for (DAGNode node : dag.getDataPropertyIndex().values()) {

            String uri = node.getUri();
            String projection = " URI as X, LITERAL as Y ";

            if (uri.startsWith(DAG.owl_inverse)) {
                uri = uri.substring(DAG.owl_inverse.length());
                projection = "URI2 as X, URI1 as Y";
            }
            String table = ABoxSerializer.dataprop_table;
            SemanticIndexRange range = node.getRange();

            insert_binary_mapping(uri, projection, table, range);

            // Handle equivalent nodes
            for (DAGNode equi : node.getEquivalents()) {
                insert_binary_mapping(equi.getUri(), projection, table, range);
            }
        }
    }

    private void insert_unary_mapping(String uri, String projection, String table, SemanticIndexRange range) throws DuplicateMappingException {
        // Generate the WHERE clause
        StringBuffer where_clause = new StringBuffer();
        for (SemanticIndexRange.Interval it : range.getIntervals()) {
            int st = it.getStart();
            int end = it.getEnd();
            where_clause.append(String.format(" (IDX >= %d) AND ( IDX <= %d) OR ", st, end));
        }
        if (where_clause.length() != 0) {
            // remove the last OR
            where_clause.delete(where_clause.length() - 3, where_clause.length());
        }

        Term qt = termFactory.getVariable("x");
        Predicate predicate = predicateFactory.getPredicate(URI.create(uri), 1);
        Atom bodyAtom = termFactory.getAtom(predicate, qt);
        predicate = predicateFactory.getPredicate(URI.create("q"), 1);
        Atom head = termFactory.getAtom(predicate, qt);
        Query cq = termFactory.getCQIE(head, bodyAtom);

        String sql = "SELECT " + projection + " FROM " + table;
        if (where_clause.length() != 0) {
            sql += " WHERE " + where_clause.toString();
        }

        OBDAMappingAxiom ax = predicateFactory.getRDBMSMappingAxiom("id" + mapcounter++,cq,predicateFactory.getSQLQuery(sql));

//        URI dsUri = apic.getDatasourcesController().getCurrentDataSource().getSourceID();
        apic.getMappingController().insertMapping(ds.getSourceID(), ax);
    }

    private void insert_binary_mapping(String uri, String projection, String table, SemanticIndexRange range) throws DuplicateMappingException {

        // Generate the WHERE clause
        StringBuffer where_clause = new StringBuffer();
        for (SemanticIndexRange.Interval it : range.getIntervals()) {
            int st = it.getStart();
            int end = it.getEnd();
            where_clause.append(String.format(" (IDX >= %d) AND ( IDX <= %d) OR ", st, end));
        }
        if (where_clause.length() != 0) {
            // remove the last AND
            where_clause.delete(where_clause.length() - 4, where_clause.length());
        }

        Term qtx = termFactory.getVariable("X");
        Term qty = termFactory.getVariable("Y");
        Predicate predicate = predicateFactory.getPredicate(URI.create(uri), 2);
        Atom bodyAtom = termFactory.getAtom(predicate, qtx,qty);
        predicate = predicateFactory.getPredicate(URI.create("q"), 2);
        Atom head = termFactory.getAtom(predicate, qtx,qty);
        Query cq = termFactory.getCQIE(head, bodyAtom);

        String sql = "SELECT " + projection + " FROM " + table;
        if (where_clause.length() != 0) {
            sql += " WHERE " + where_clause.toString();
        }

        OBDAMappingAxiom ax = predicateFactory.getRDBMSMappingAxiom("id" + mapcounter++,cq,predicateFactory.getSQLQuery(sql));

//        URI dsUri = apic.getDatasourcesController().getCurrentDataSource().getSourceID();
        apic.getMappingController().insertMapping(ds.getSourceID(), ax);

    }


}
