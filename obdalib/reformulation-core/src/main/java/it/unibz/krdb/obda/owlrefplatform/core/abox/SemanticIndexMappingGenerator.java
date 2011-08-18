package it.unibz.krdb.obda.owlrefplatform.core.abox;


import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.PredicateAtom;
import it.unibz.krdb.obda.model.Query;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.AtomicConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ExistentialConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicDescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OWLAPITranslator;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate the mappings for DAG
 *
 * @author Sergejs Pugac
 */
public class SemanticIndexMappingGenerator {

    private static final Logger log = LoggerFactory.getLogger(SemanticIndexMappingGenerator.class);

    private static final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();
    private static final DescriptionFactory descFactory = new BasicDescriptionFactory();
    private static final boolean mergeUniions = true;


    /**
     * Generate mappings for DAG
     *
     * @throws DuplicateMappingException error creating mappings
     */
    public static List<MappingKey> build(DAG dag, DAG pureIsa) throws DuplicateMappingException {
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

                    if (pureIsa.equi_mappings.containsKey(role)) {
                        //  XXX: Very dirty hack, needs to be redone
                        continue;
                    }

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
        for (DAGNode node : dag.getRoles()) {

            RoleDescription nodeDesc = (RoleDescription) node.getDescription();
            if (nodeDesc.getPredicate().getName().toString().startsWith(OWLAPITranslator.AUXROLEURI)) {
                continue;
            }
            if (nodeDesc.isInverse()) {
                continue;
            }

            SemanticIndexRange range = pureIsa.getRoleNode(descFactory.getRoleDescription(nodeDesc.getPredicate(), false)).getRange();
            String projection = "URI1 as X, URI2 as Y";
            mappings.add(new BinaryMappingKey(
                    range,
                    projection,
                    ABoxSerializer.role_table,
                    nodeDesc.getPredicate().getName().toString()
            ));

            for (DAGNode equiNode : node.getEquivalents()) {

                RoleDescription equiNodeDesc = (RoleDescription) equiNode.getDescription();

                if (equiNodeDesc.isInverse()) {
                    projection = "URI1 as Y, URI2 as X";
                }
                mappings.add(new BinaryMappingKey(
                        range,
                        projection,
                        ABoxSerializer.role_table,
                        equiNodeDesc.getPredicate().getName().toString()
                ));
            }

            for (DAGNode child : node.getChildren()) {
                RoleDescription childDesc = (RoleDescription) child.getDescription();

                if (childDesc.getPredicate().getName().toString().startsWith(OWLAPITranslator.AUXROLEURI)) {
                    continue;
                }

                if (!childDesc.isInverse()) {
                    continue;
                }
                RoleDescription posChildDesc = descFactory.getRoleDescription(childDesc.getPredicate(), false);

                SemanticIndexRange childRange = pureIsa.getRoleNode(posChildDesc).getRange();

                mappings.add(new BinaryMappingKey(
                        childRange,
                        "URI1 as Y, URI2 as X",
                        ABoxSerializer.role_table,
                        nodeDesc.getPredicate().getName().toString()
                ));

                for (DAGNode equiNode : node.getEquivalents()) {

                    RoleDescription equiNodeDesc = (RoleDescription) equiNode.getDescription();
                    String equiProj = "URI1 as Y, URI2 as X";
                    if (equiNodeDesc.isInverse()) {
                        equiProj = "URI1 as X, URI2 as Y";
                    }

                    mappings.add(new BinaryMappingKey(
                            childRange,
                            equiProj,
                            ABoxSerializer.role_table,
                            equiNodeDesc.getPredicate().getName().toString()
                    ));
                }
            }
        }

        return mappings;
//        return compile(mappings);
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

    public static List<OBDAMappingAxiom> compile(List<MappingKey> mappings) throws DuplicateMappingException {

        List<OBDAMappingAxiom> rv = new ArrayList<OBDAMappingAxiom>(128);
        List<MappingKey> mergedElements = new ArrayList<MappingKey>(128);
        Collections.sort(mappings);

        List<List<MappingKey>> mergedGroup = new ArrayList<List<MappingKey>>();
        List<MappingKey> firstGroup = new ArrayList<MappingKey>();
        firstGroup.add(mappings.get(0));
        mergedGroup.add(firstGroup);

        for (MappingKey map : mappings) {
            List<MappingKey> uriTabel = mergedGroup.get(mergedGroup.size() - 1);
            MappingKey lastElem = uriTabel.get(uriTabel.size() - 1);
            if (lastElem.uri.equals(map.uri) && lastElem.projection.equals(map.projection)) {
                uriTabel.add(map);
            } else {
                List<MappingKey> group = new ArrayList<MappingKey>();
                group.add(map);
                mergedGroup.add(group);
            }
        }
        for (List<MappingKey> group : mergedGroup) {
            MappingKey cur = group.get(0);
            SemanticIndexRange curRange = new SemanticIndexRange(cur.range);
            for (MappingKey map : group) {
                curRange.addRange(map.range);
            }
            if (mergeUniions) {
                if (cur instanceof UnaryMappingKey) {
                    mergedElements.add(new UnaryMappingKey(curRange, cur.projection, cur.table, cur.uri));
                } else if (cur instanceof BinaryMappingKey) {
                    mergedElements.add(new BinaryMappingKey(curRange, cur.projection, cur.table, cur.uri));
                }
            } else {
                if (cur instanceof UnaryMappingKey) {
                    rv.add(makeUnaryMapp(cur.uri, genQuerySQL(new UnaryMappingKey(curRange, cur.projection, cur.table, cur.uri))));
                } else if (cur instanceof BinaryMappingKey) {
                    rv.add(makeBinaryMapp(cur.uri, genQuerySQL(new BinaryMappingKey(curRange, cur.projection, cur.table, cur.uri))));
                }
            }
        }
        if (mergeUniions) {

            mergedGroup = new ArrayList<List<MappingKey>>();
            firstGroup = new ArrayList<MappingKey>();
            firstGroup.add(mergedElements.get(0));
            mergedGroup.add(firstGroup);

            for (int i = 1; i < mappings.size(); ++i) {
                MappingKey map = mappings.get(i);
                List<MappingKey> uriTabel = mergedGroup.get(mergedGroup.size() - 1);
                MappingKey lastElem = uriTabel.get(uriTabel.size() - 1);
                if (lastElem.uri.equals(map.uri) &&
                        ((lastElem instanceof UnaryMappingKey && map instanceof UnaryMappingKey) ||
                                (lastElem instanceof BinaryMappingKey && map instanceof BinaryMappingKey &&
                                        lastElem.projection.equals(map.projection)))) {
                    uriTabel.add(map);
                } else {
                    List<MappingKey> group = new ArrayList<MappingKey>();
                    group.add(map);
                    mergedGroup.add(group);
                }
            }
            for (List<MappingKey> group : mergedGroup) {
                MappingKey cur = group.get(0);
                StringBuilder sql = new StringBuilder();
                sql.append(genQuerySQL(cur));

                for (int j = 1; j < group.size(); ++j) {
                    MappingKey map = group.get(j);
                    sql.append(" UNION ALL ");
                    sql.append(genQuerySQL(map));
                }
                if (cur instanceof UnaryMappingKey) {
                    rv.add(makeUnaryMapp(cur.uri, sql.toString()));
                } else if (cur instanceof BinaryMappingKey) {
                    rv.add(makeBinaryMapp(cur.uri, sql.toString()));
                }
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

        public MappingKey(SemanticIndexRange range, String projection, String table, String uri) {
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
