package org.obda.owlrefplatform.core.abox;


import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.exception.DuplicateMappingException;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.domain.Query;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSOBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import org.obda.query.domain.Atom;
import org.obda.query.domain.Predicate;
import org.obda.query.domain.PredicateFactory;
import org.obda.query.domain.Term;
import org.obda.query.domain.imp.AtomImpl;
import org.obda.query.domain.imp.BasicPredicateFactoryImpl;
import org.obda.query.domain.imp.CQIEImpl;
import org.obda.query.domain.imp.TermFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Vector;

/**
 * Generate the mappings for DAG
 *
 * @author Sergejs Pugac
 */
public class SemanticIndexMappingGenerator {

    private final Logger log = LoggerFactory.getLogger(SemanticIndexMappingGenerator.class);

    private final static TermFactoryImpl termFactory = TermFactoryImpl.getInstance();
    private final static PredicateFactory predicateFactory = BasicPredicateFactoryImpl.getInstance();

    private int mapcounter;
    private final APIController apic;
    private final DAG dag;

    public SemanticIndexMappingGenerator(APIController apic, DAG dag) {
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
        for (DAGNode node : dag.getClassIndex().values()) {
            if (node.getUri().startsWith(DAG.owl_exists) || node.getUri().startsWith(DAG.owl_inverse_exists)) {
                continue;
            }

            String uri = node.getUri();
            String tablename = ABoxSerializer.class_table;
            String projection = "URI as X";
            SemanticIndexRange range = node.getRange();
            insert_unary_mapping(uri, projection, tablename, range);

            // check if has child exists(R)
            for (DAGNode child : node.getChildren()) {

                if (child.getUri().startsWith(DAG.owl_exists_obj)) {
                    uri = child.getUri().substring(DAG.owl_exists_obj.length());
                    tablename = ABoxSerializer.objectprop_table;
                    projection = "URI1 as X";
                    range = dag.getObjectPropertyIndex().get(uri).getRange();
                } else if (child.getUri().startsWith(DAG.owl_inverse_exists_obj)) {
                    uri = child.getUri().substring(DAG.owl_inverse_exists_obj.length());
                    tablename = ABoxSerializer.objectprop_table;
                    projection = "URI2 as X";
                    range = dag.getObjectPropertyIndex().get(uri).getRange();
                } else if (child.getUri().startsWith(DAG.owl_exists_data)) {
                    uri = child.getUri().substring(DAG.owl_exists_data.length());
                    tablename = ABoxSerializer.dataprop_table;
                    projection = "URI as X";
                    range = dag.getDataPropertyIndex().get(uri).getRange();
                } else if (child.getUri().startsWith(DAG.owl_inverse_exists_data)) {
                    uri = child.getUri().substring(DAG.owl_inverse_exists_data.length());
                    tablename = ABoxSerializer.dataprop_table;
                    projection = "LITERAL as X";
                    range = dag.getDataPropertyIndex().get(uri).getRange();
                }
                insert_unary_mapping(uri, projection, tablename, range);
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

        Term qt = termFactory.createVariable("x");
        List<Term> terms = new Vector<Term>();
        terms.add(qt);
        Predicate predicate = predicateFactory.createPredicate(URI.create(uri), terms.size());
        Atom bodyAtom = new AtomImpl(predicate, terms);
        List<Atom> body = new Vector<Atom>();
        body.add(bodyAtom);
        predicate = predicateFactory.createPredicate(URI.create("q"), terms.size());

        Atom head = new AtomImpl(predicate, terms);
        Query cq = new CQIEImpl(head, body, false);

        String sql = "SELECT " + projection + " FROM " + table;
        if (where_clause.length() != 0) {
            sql += " WHERE " + where_clause.toString();
        }

        OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id" + mapcounter++);
        ax.setTargetQuery(cq);
        ax.setSourceQuery(new RDBMSSQLQuery(sql));

        URI dsUri = apic.getDatasourcesController().getCurrentDataSource().getSourceID();
        apic.getMappingController().insertMapping(dsUri, ax);
    }

    private void insert_binary_mapping(String uri, String projection, String table, SemanticIndexRange range) throws DuplicateMappingException {

        // Generate the WHERE clause
        StringBuffer where_clause = new StringBuffer();
        for (SemanticIndexRange.Interval it : range.getIntervals()) {
            int st = it.getStart();
            int end = it.getEnd();
            where_clause.append(String.format(" (IDX >= %d) AND ( IDX <= %d) AND ", st, end));
        }
        if (where_clause.length() != 0) {
            // remove the last AND
            where_clause.delete(where_clause.length() - 4, where_clause.length());
        }

        Term qtx = termFactory.createVariable("X");
        Term qty = termFactory.createVariable("Y");
        List<Term> terms = new Vector<Term>();
        terms.add(qtx);
        terms.add(qty);
        Predicate predicate = predicateFactory.createPredicate(URI.create(uri), terms.size());
        Atom bodyAtom = new AtomImpl(predicate, terms);
        List<Atom> body = new Vector<Atom>();
        body.add(bodyAtom);
        predicate = predicateFactory.createPredicate(URI.create("q"), terms.size());

        Atom head = new AtomImpl(predicate, terms);
        Query cq = new CQIEImpl(head, body, false);

        String sql = "SELECT " + projection + " FROM " + table;
        if (where_clause.length() != 0) {
            sql += " WHERE " + where_clause.toString();
        }

        OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id" + mapcounter++);
        ax.setTargetQuery(cq);
        ax.setSourceQuery(new RDBMSSQLQuery(sql));

        URI dsUri = apic.getDatasourcesController().getCurrentDataSource().getSourceID();
        apic.getMappingController().insertMapping(dsUri, ax);

    }


}
