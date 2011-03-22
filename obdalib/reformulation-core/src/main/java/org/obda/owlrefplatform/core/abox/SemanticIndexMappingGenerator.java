package org.obda.owlrefplatform.core.abox;


import inf.unibz.it.obda.api.controller.APIController;
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
 * Generate the mappings for th
 *
 * @author Sergejs Pugac
 */
public class SemanticIndexMappingGenerator {

    private final static Logger log = LoggerFactory.getLogger(SemanticIndexMappingGenerator.class);

    private final static TermFactoryImpl termFactory = TermFactoryImpl.getInstance();
    private final static PredicateFactory predicateFactory = BasicPredicateFactoryImpl.getInstance();

    /**
     * Generate mappings for
     *
     * @param dag
     */
    public static void GenMapping(DAG dag, APIController apic) {
        int mapcounter = 1;

        for (DAGNode node : dag.getClassIndex().values()) {

            String uri;
            String tablename;
            String projection;
            SemanticIndexRange range;
            String where = "";

            if (node.getUri().startsWith(DAG.owl_exists_obj)) {
                uri = node.getUri().substring(DAG.owl_exists_obj.length());
                tablename = ABoxSerializer.objectprop_table;
                projection = "URI1 as X";
                range = dag.getObjectPropertyIndex().get(uri).getRange();
            } else if (node.getUri().startsWith(DAG.owl_inverse_exists_obj)) {
                uri = node.getUri().substring(DAG.owl_inverse_exists_obj.length());
                tablename = ABoxSerializer.objectprop_table;
                projection = "URI2 as X";
                range = dag.getObjectPropertyIndex().get(uri).getRange();
            } else if (node.getUri().startsWith(DAG.owl_exists_data)) {
                uri = node.getUri().substring(DAG.owl_exists_data.length());
                tablename = ABoxSerializer.dataprop_table;
                projection = "URI as X";
                range = dag.getDataPropertyIndex().get(uri).getRange();
            } else if (node.getUri().startsWith(DAG.owl_inverse_exists_data)) {
                uri = node.getUri().substring(DAG.owl_inverse_exists_data.length());
                tablename = ABoxSerializer.dataprop_table;
                projection = "LITERAL as X";
                range = dag.getDataPropertyIndex().get(uri).getRange();
            } else {
                uri = node.getUri();
                tablename = ABoxSerializer.class_table;
                projection = "URI as X";
                range = node.getRange();
            }
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

            String sql = "SELECT " + projection + "FROM" + tablename;
            if (where_clause.length() != 0) {
                sql += " WHERE " + where_clause.toString();
            }

            OBDAMappingAxiom ax = new RDBMSOBDAMappingAxiom("id" + mapcounter++);
            ax.setTargetQuery(cq);
            ax.setSourceQuery(new RDBMSSQLQuery(sql));

            // FIXME: how to obtaint dsUri
            //apic.getMappingController().insertMapping(dsUri, ax);
        }
        for (DAGNode node : dag.getObjectPropertyIndex().values()) {

        }

        for (DAGNode node : dag.getDataPropertyIndex().values()) {

        }
    }

}
