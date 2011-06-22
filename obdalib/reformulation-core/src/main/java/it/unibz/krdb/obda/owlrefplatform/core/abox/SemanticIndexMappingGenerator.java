package it.unibz.krdb.obda.owlrefplatform.core.abox;


import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.GraphGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.AtomicConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ExistentialConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicDescriptionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generate the mappings for DAG
 *
 * @author Sergejs Pugac
 */
public class SemanticIndexMappingGenerator {

    private static final Logger log = LoggerFactory.getLogger(SemanticIndexMappingGenerator.class);

    private static final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();
    private static final DescriptionFactory descFactory = new BasicDescriptionFactory();


    /**
     * Generate mappings for DAG
     *
     * @throws DuplicateMappingException error creating mappings
     */
    public static List<OBDAMappingAxiom> build(DAG dag) throws DuplicateMappingException {
        log.debug("Generating mappings for DAG {}", dag);

//        List<OBDAMappingAxiom> rv = new ArrayList<OBDAMappingAxiom>(dag.getClasses().size() + dag.getRoles().size());
        List<MappingKey> mappings = new ArrayList<MappingKey>();

        for (DAGNode node : dag.getClasses()) {

            if (!(node.getDescription() instanceof AtomicConceptDescription) ||
                    node.getDescription().equals(DAG.thingConcept)) {
                continue;
            }

            List<DAGNode> equiNodes = new ArrayList<DAGNode>(node.getEquivalents().size() + 1);
            equiNodes.add(node);
            equiNodes.addAll(node.getEquivalents());

            String tablename = ABoxSerializer.class_table;
            String projection = "URI as X";
            SemanticIndexRange range = node.getRange();

            for (DAGNode equiNode : equiNodes) {
                if (!(equiNode.getDescription() instanceof AtomicConceptDescription) ||
                        equiNode.getDescription().equals(DAG.thingConcept)) {
                    continue;
                }
                AtomicConceptDescription equiDesc = (AtomicConceptDescription) equiNode.getDescription();
                String equiUri = equiDesc.getPredicate().getName().toString();

//                rv.add(get_unary_mapping(equiUri, projection, tablename, range));
                mappings.add(new UnaryMappingKey(range, projection, tablename, equiUri));


            }


            // check if has child exists(R)
            for (DAGNode descendant : node.descendans) {

                if (descendant.getDescription() instanceof ExistentialConceptDescription) {
                    SemanticIndexRange descRange;

                    Predicate p = ((ExistentialConceptDescription) descendant.getDescription()).getPredicate();
                    boolean isInverse = ((ExistentialConceptDescription) descendant.getDescription()).isInverse();

                    RoleDescription role = descFactory.getRoleDescription(p, false);

                    if (isInverse) {
                        projection = "URI2 as X";
                    } else {
                        projection = "URI1 as X";
                    }

                    descRange = dag.getRoleNode(role).getRange();

                    for (DAGNode equiNode : equiNodes) {
                        if (!(equiNode.getDescription() instanceof AtomicConceptDescription) ||
                                equiNode.getDescription().equals(DAG.thingConcept)) {
                            continue;
                        }
                        AtomicConceptDescription equiDesc = (AtomicConceptDescription) equiNode.getDescription();
                        String equiUri = equiDesc.getPredicate().getName().toString();

                        if (isInverse) {
                            mappings.add(new UnaryMappingKey(descRange, projection, ABoxSerializer.role_table, equiUri));
                        } else {
                            mappings.add(new UnaryMappingKey(descRange, projection, ABoxSerializer.role_table, equiUri));
                        }
                    }

                }
            }
        }
        for (DAGNode node : dag.getRoles()) {

            List<DAGNode> equiNodes = new ArrayList<DAGNode>(node.getEquivalents().size() + 1);
            equiNodes.add(node);
            equiNodes.addAll(node.getEquivalents());

            for (DAGNode equiNode : equiNodes) {

                RoleDescription equiNodeDesc = (RoleDescription) equiNode.getDescription();

                if (equiNodeDesc.isInverse()) {
                    continue;
                }

//                rv.add(get_binary_mapping(
//                        equiNodeDesc.getPredicate().getName().toString(),
//                        " URI1 as X, URI2 as Y ",
//                        ABoxSerializer.role_table,
//                        node.getRange()));
                mappings.add(new BinaryMappingKey(
                        node.getRange(),
                        "URI1 as X, URI2 as Y",
                        ABoxSerializer.role_table,
                        equiNodeDesc.getPredicate().getName().toString()
                ));
            }

            for (DAGNode child : node.getChildren()) {
                RoleDescription childDesc = (RoleDescription) child.getDescription();

                if (childDesc.isInverse()) {
                    continue;
                }

                for (DAGNode equiNode : equiNodes) {

                    RoleDescription equiNodeDesc = (RoleDescription) equiNode.getDescription();
                    if (!equiNodeDesc.isInverse()) {
                        continue;
                    }

//                    rv.add(get_binary_mapping(
//                            equiNodeDesc.getPredicate().getName().toString(),
//                            " URI1 AS Y, URI2 AS X ",
//                            ABoxSerializer.role_table,
//                            child.getRange()));
                    mappings.add(new BinaryMappingKey(
                            child.getRange(),
                            "URI1 AS Y, URI2 AS X",
                            ABoxSerializer.role_table,
                            equiNodeDesc.getPredicate().getName().toString()
                    ));
                }
            }
        }

        return fillterRedundancy(mappings);
    }


    private static OBDAMappingAxiom get_unary_mapping(String uri, String projection, String table, SemanticIndexRange range) throws DuplicateMappingException {
        // Generate the WHERE clause
        StringBuilder where_clause = new StringBuilder();
        for (SemanticIndexRange.Interval it : range.getIntervals()) {
            int st = it.getStart();
            int end = it.getEnd();
            String interval;
            if (st == end) {
                interval = String.format("(IDX = %d) OR ", st);
            } else {
                interval = String.format("((IDX >= %d) AND ( IDX <= %d)) OR ", st, end);
            }
            where_clause.append(interval);
        }
        if (where_clause.length() != 0) {
            // remove the last OR
            where_clause.delete(where_clause.length() - 3, where_clause.length());
        }

        Term qt = predicateFactory.getVariable("x");
        Predicate predicate = predicateFactory.getPredicate(URI.create(uri), 1);
        PredicateAtom bodyAtom = predicateFactory.getAtom(predicate, qt);
        predicate = predicateFactory.getPredicate(URI.create("q"), 1);
        PredicateAtom head = predicateFactory.getAtom(predicate, qt);
        Query cq = predicateFactory.getCQIE(head, bodyAtom);

        String sql = "SELECT " + projection + " FROM " + table;
        if (where_clause.length() != 0) {
            sql += " WHERE " + where_clause.toString();
        }

        return predicateFactory.getRDBMSMappingAxiom(sql, cq);
    }

    private static OBDAMappingAxiom get_binary_mapping(String uri, String projection, String table, SemanticIndexRange range) throws DuplicateMappingException {

        // Generate the WHERE clause
        StringBuilder where_clause = new StringBuilder();
        for (SemanticIndexRange.Interval it : range.getIntervals()) {
            int st = it.getStart();
            int end = it.getEnd();
            String interval;
            if (st == end) {
                interval = String.format("(IDX = %d) OR ", st);
            } else {
                interval = String.format("((IDX >= %d) AND ( IDX <= %d)) OR ", st, end);
            }
            where_clause.append(interval);
        }
        if (where_clause.length() != 0) {
            // remove the last AND
            where_clause.delete(where_clause.length() - 4, where_clause.length());
        }

        Term qtx = predicateFactory.getVariable("X");
        Term qty = predicateFactory.getVariable("Y");
        Predicate predicate = predicateFactory.getPredicate(URI.create(uri), 2);
        PredicateAtom bodyAtom = predicateFactory.getAtom(predicate, qtx, qty);
        predicate = predicateFactory.getPredicate(URI.create("q"), 2);
        PredicateAtom head = predicateFactory.getAtom(predicate, qtx, qty);
        Query cq = predicateFactory.getCQIE(head, bodyAtom);

        String sql = "SELECT " + projection + " FROM " + table;
        if (where_clause.length() != 0) {
            sql += " WHERE " + where_clause.toString();
        }

        return predicateFactory.getRDBMSMappingAxiom(sql, cq);
    }

    private static List<OBDAMappingAxiom> fillterRedundancy(List<MappingKey> mappings) throws DuplicateMappingException {

        List<OBDAMappingAxiom> rv = new ArrayList<OBDAMappingAxiom>(128);
        Collections.sort(mappings);

        MappingKey cur = mappings.get(0);
        int i = 0;
        while (i < mappings.size()) {

            SemanticIndexRange curRange = new SemanticIndexRange(cur.range);
            MappingKey next = mappings.get(i);
            while (cur.uri.equals(next.uri) &&
                    cur.projection.equals(next.projection)) {

                if (!mappings.get(i).uri.startsWith("ER.A-AUX")) {
                    curRange.addRange(next.range);
                }
                ++i;

                if (i < mappings.size()) {
                    next = mappings.get(i);
                } else {
                    break;
                }
            }
            if (cur instanceof UnaryMappingKey) {
                rv.add(get_unary_mapping(cur.uri, cur.projection, cur.table, curRange));
            } else if (cur instanceof BinaryMappingKey) {
                rv.add(get_binary_mapping(cur.uri, cur.projection, cur.table, curRange));
            }
            cur = next;
        }

        if (GraphGenerator.debugInfoDump) {
            GraphGenerator.dumpMappings(mappings);
        }
        return rv;
    }

    public static class MappingKey implements Comparable<MappingKey> {

        public final SemanticIndexRange range;
        public final String projection;
        public final String table;
        public final String uri;

        MappingKey(SemanticIndexRange range, String projection, String table, String uri) {
            this.range = range;
            this.projection = projection;
            this.table = table;
            this.uri = uri;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MappingKey that = (MappingKey) o;

            if (projection != null ? !projection.equals(that.projection) : that.projection != null) return false;
            if (range != null ? !range.equals(that.range) : that.range != null) return false;
            if (table != null ? !table.equals(that.table) : that.table != null) return false;
            if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = range != null ? range.hashCode() : 0;
            result = 31 * result + (projection != null ? projection.hashCode() : 0);
            result = 31 * result + (table != null ? table.hashCode() : 0);
            result = 31 * result + (uri != null ? uri.hashCode() : 0);
            return result;
        }

        @Override
        public int compareTo(MappingKey mappingKey) {
            int i = this.uri.compareTo(mappingKey.uri);
            if (i != 0) {
                return i;
            }
            return this.projection.compareTo(mappingKey.projection);
        }
    }

    public static class UnaryMappingKey extends MappingKey {

        UnaryMappingKey(SemanticIndexRange range, String projection, String table, String uri) {
            super(range, projection, table, uri);
        }
    }

    public static class BinaryMappingKey extends MappingKey {

        BinaryMappingKey(SemanticIndexRange range, String projection, String table, String uri) {
            super(range, projection, table, uri);
        }
    }


}
