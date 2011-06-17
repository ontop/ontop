package it.unibz.krdb.obda.owlrefplatform.core.abox;


import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.AtomicConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ExistentialConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicDescriptionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
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
        List<OBDAMappingAxiom> rv = new ArrayList<OBDAMappingAxiom>(dag.getClasses().size() + dag.getRoles().size());

        for (DAGNode node : dag.getClasses()) {

            if (!(node.getDescription() instanceof AtomicConceptDescription) ||
                    node.getDescription().equals(DAG.thingConcept)) {
                continue;
            }
            AtomicConceptDescription nodeDescription = (AtomicConceptDescription) node.getDescription();

            String uri = nodeDescription.getPredicate().getName().toString();
            String tablename = ABoxSerializer.class_table;
            String projection = " URI as X ";
            SemanticIndexRange range = node.getRange();

            rv.add(get_unary_mapping(uri, projection, tablename, range));

            // Handle equivalent nodes
            for (DAGNode equi : node.getEquivalents()) {
                if (equi.getDescription() instanceof AtomicConceptDescription) {
                    String equiUri = ((AtomicConceptDescription) equi.getDescription()).getPredicate().getName().toString();
                    rv.add(get_unary_mapping(equiUri, projection, tablename, range));
                }
            }

            // check if has child exists(R)
            for (DAGNode descendant : node.descendans) {

                if (descendant.getDescription() instanceof ExistentialConceptDescription) {
                    String projection_inverse;
                    SemanticIndexRange descRange;
                    SemanticIndexRange descRangeInv;

                    Predicate p = ((ExistentialConceptDescription) descendant.getDescription()).getPredicate();
                    boolean isInverse = ((ExistentialConceptDescription) descendant.getDescription()).isInverse();

                    RoleDescription role = descFactory.getRoleDescription(p, false);
                    RoleDescription roleInv = descFactory.getRoleDescription(p, true);

                    if (isInverse) {
                        projection = " URI2 as X ";
                        projection_inverse = " URI1 as X ";
                    } else {
                        projection = " URI1 as X ";
                        projection_inverse = " URI2 as X ";
                    }

                    descRange = dag.getRoleNode(role).getRange();
                    descRangeInv = dag.getRoleNode(roleInv).getRange();

                    rv.add(get_unary_mapping(uri, projection, ABoxSerializer.role_table, descRange));
                    rv.add(get_unary_mapping(uri, projection_inverse, ABoxSerializer.role_table, descRangeInv));
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

                rv.add(get_binary_mapping(
                        equiNodeDesc.getPredicate().getName().toString(),
                        " URI1 as X, URI2 as Y ",
                        ABoxSerializer.role_table,
                        node.getRange()));
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

                    rv.add(get_binary_mapping(
                            equiNodeDesc.getPredicate().getName().toString(),
                            " URI1 AS Y, URI2 AS X ",
                            ABoxSerializer.role_table,
                            child.getRange()));
                }
            }
        }

        return rv;
    }

    private static OBDAMappingAxiom get_unary_mapping(String uri, String projection, String table, SemanticIndexRange range) throws DuplicateMappingException {
        // Generate the WHERE clause
        StringBuilder where_clause = new StringBuilder();
        for (SemanticIndexRange.Interval it : range.getIntervals()) {
            int st = it.getStart();
            int end = it.getEnd();
            where_clause.append(String.format(" (IDX >= %d) AND ( IDX <= %d) OR ", st, end));
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

        OBDAMappingAxiom ax = predicateFactory.getRDBMSMappingAxiom(sql, cq);

        return ax;
    }

    private static OBDAMappingAxiom get_binary_mapping(String uri, String projection, String table, SemanticIndexRange range) throws DuplicateMappingException {

        // Generate the WHERE clause
        StringBuilder where_clause = new StringBuilder();
        for (SemanticIndexRange.Interval it : range.getIntervals()) {
            int st = it.getStart();
            int end = it.getEnd();
            where_clause.append(String.format(" (IDX >= %d) AND ( IDX <= %d) OR ", st, end));
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

        OBDAMappingAxiom ax = predicateFactory.getRDBMSMappingAxiom(sql, cq);
        return ax;
    }


}
