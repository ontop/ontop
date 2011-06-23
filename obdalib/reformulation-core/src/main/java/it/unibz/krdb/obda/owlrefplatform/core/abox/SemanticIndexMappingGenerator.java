package it.unibz.krdb.obda.owlrefplatform.core.abox;


import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.GraphGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.*;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicDescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OWLAPITranslator;
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
    private static final boolean mergeUniions = false;


    /**
     * Generate mappings for DAG
     *
     * @throws DuplicateMappingException error creating mappings
     */
    public static List<OBDAMappingAxiom> build(DAG dag, DAG pureIsa) throws DuplicateMappingException {
        log.debug("Generating mappings for DAG {}", pureIsa);

        List<MappingKey> mappings = new ArrayList<MappingKey>();

        for (DAGNode node : pureIsa.getClasses()) {

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

                mappings.add(new UnaryMappingKey(range, projection, tablename, equiUri));
            }


            // check if has child exists(R) in the general ISA DAG
            DAGNode genNode = dag.getClassNode((ConceptDescription) node.getDescription());
            for (DAGNode descendant : genNode.descendans) {

                if (descendant.getDescription() instanceof ExistentialConceptDescription) {
                    SemanticIndexRange descRange;

                    Predicate p = ((ExistentialConceptDescription) descendant.getDescription()).getPredicate();
                    if (p.getName().toString().startsWith(OWLAPITranslator.AUXROLEURI)) {
                        continue;
                    }
                    boolean isInverse = ((ExistentialConceptDescription) descendant.getDescription()).isInverse();

                    RoleDescription role = descFactory.getRoleDescription(p, false);

                    if (isInverse) {
                        projection = "URI2 as X";
                    } else {
                        projection = "URI1 as X";
                    }

                    descRange = pureIsa.getRoleNode(role).getRange();

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
        for (DAGNode node : pureIsa.getRoles()) {

            List<DAGNode> equiNodes = new ArrayList<DAGNode>(node.getEquivalents().size() + 1);
            equiNodes.add(node);
            equiNodes.addAll(node.getEquivalents());

            for (DAGNode equiNode : equiNodes) {

                RoleDescription equiNodeDesc = (RoleDescription) equiNode.getDescription();

                if (equiNodeDesc.isInverse()) {
                    continue;
                }

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

                    mappings.add(new BinaryMappingKey(
                            child.getRange(),
                            "URI1 AS Y, URI2 AS X",
                            ABoxSerializer.role_table,
                            equiNodeDesc.getPredicate().getName().toString()
                    ));
                }
            }
        }

        return filterRedundancy(mappings);
    }


    private static String genQuerySQL(MappingKey map) {
        // Generate the WHERE clause
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(map.projection);
        sql.append(" FROM ");
        sql.append(map.table);
        sql.append(" WHERE ");

        for (SemanticIndexRange.Interval it : map.range.getIntervals()) {
            int st = it.getStart();
            int end = it.getEnd();
            String interval;
            if (st == end) {
                interval = String.format("(IDX = %d) OR ", st);
            } else {
                interval = String.format("((IDX >= %d) AND ( IDX <= %d)) OR ", st, end);
            }
            sql.append(interval);
        }
        if (map.range.getIntervals().size() != 0) {
            // remove the last AND
            sql.delete(sql.length() - 4, sql.length());
        }

        return sql.toString();
    }

    private static List<OBDAMappingAxiom> filterRedundancy(List<MappingKey> mappings) throws DuplicateMappingException {

        List<OBDAMappingAxiom> rv = new ArrayList<OBDAMappingAxiom>(128);
        List<MappingKey> merged = new ArrayList<MappingKey>(128);
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
                if (mergeUniions) {
                    merged.add(new UnaryMappingKey(curRange, cur.projection, cur.table, cur.uri));
                } else {
                    rv.add(makeUnaryMapp(cur.uri, genQuerySQL(cur)));
                }
            } else if (cur instanceof BinaryMappingKey) {
                if (mergeUniions) {
                    merged.add(new BinaryMappingKey(curRange, cur.projection, cur.table, cur.uri));
                } else {
                    rv.add(makeBinaryMapp(cur.uri, genQuerySQL(cur)));
                }
            }
            cur = next;
        }

        if (GraphGenerator.debugInfoDump) {
            GraphGenerator.dumpMappings(mappings);
        }

        if (mergeUniions) {
            Collections.sort(merged);
            int k = 1;
            MappingKey curMap = merged.get(0);
            StringBuffer sql = new StringBuffer();
            while (k < merged.size()) {
                sql = new StringBuffer();
                sql.append(genQuerySQL(curMap));
                MappingKey nextMap = merged.get(k);
                while (curMap.uri.equals(nextMap.uri) &&
                        ((curMap instanceof UnaryMappingKey && nextMap instanceof UnaryMappingKey) ||
                                (curMap instanceof BinaryMappingKey && nextMap instanceof BinaryMappingKey &&
                                        curMap.projection.equals(nextMap.projection))
                        )) {
                    sql.append(" UNION ALL ");
                    sql.append(genQuerySQL(nextMap));
                    ++k;
                    if (k < merged.size()) {
                        nextMap = merged.get(k);
                    } else {
                        break;
                    }
                }
                if (curMap instanceof UnaryMappingKey) {
                    rv.add(makeUnaryMapp(curMap.uri, sql.toString()));
                } else if (curMap instanceof BinaryMappingKey) {
                    rv.add(makeBinaryMapp(curMap.uri, sql.toString()));
                }
                curMap = nextMap;
                ++k;
            }
            // last mapping
            if (curMap instanceof UnaryMappingKey) {
                rv.add(makeUnaryMapp(curMap.uri, genQuerySQL(curMap)));
            } else if (curMap instanceof BinaryMappingKey) {
                rv.add(makeBinaryMapp(curMap.uri, genQuerySQL(curMap)));
            }
        }
        return rv;
    }

    private static OBDAMappingAxiom makeBinaryMapp(String uri, String sql) {
        Term qtx = predicateFactory.getVariable("X");
        Term qty = predicateFactory.getVariable("Y");
        Predicate predicate = predicateFactory.getPredicate(URI.create(uri), 2);
        PredicateAtom bodyAtom = predicateFactory.getAtom(predicate, qtx, qty);
        predicate = predicateFactory.getPredicate(URI.create("q"), 2);
        PredicateAtom head = predicateFactory.getAtom(predicate, qtx, qty);
        Query cq = predicateFactory.getCQIE(head, bodyAtom);

        return predicateFactory.getRDBMSMappingAxiom(sql, cq);

    }

    private static OBDAMappingAxiom makeUnaryMapp(String uri, String sql) {
        Term qt = predicateFactory.getVariable("x");
        Predicate predicate = predicateFactory.getPredicate(URI.create(uri), 1);
        PredicateAtom bodyAtom = predicateFactory.getAtom(predicate, qt);
        predicate = predicateFactory.getPredicate(URI.create("q"), 1);
        PredicateAtom head = predicateFactory.getAtom(predicate, qt);
        Query cq = predicateFactory.getCQIE(head, bodyAtom);

        return predicateFactory.getRDBMSMappingAxiom(sql, cq);

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
