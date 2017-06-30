package it.unibz.inf.ontop.spec.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.TermUtils;
import it.unibz.inf.ontop.sql.DatabaseRelationDefinition;
import it.unibz.inf.ontop.sql.Relation2DatalogPredicate;
import it.unibz.inf.ontop.sql.RelationID;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

public class LegacyIsNotNullDatalogMappingFiller {

    /***
     * Adding NOT NULL conditions to the variables used in the head
     * to preserve SQL-RDF semantics
     */
    public static CQIE addNotNull(CQIE rule, DBMetadata dbMetadata) {
        Set<Variable> headvars = new HashSet<>();
        TermUtils.addReferencedVariablesTo(headvars, rule.getHead());
        for (Variable var : headvars) {
            List<Function> body = rule.getBody();
            if (isNullable(var, body, dbMetadata)) {
                Function notnull = DATA_FACTORY.getFunctionIsNotNull(var);
                if (!body.contains(notnull))
                    body.add(notnull);
            }
        }
        return rule;
    }

    /**
     * Returns false if it detects that the variable is guaranteed not being null.
     */
    private static boolean isNullable(Variable variable, List<Function> bodyAtoms, DBMetadata metadata) {
        /*
         * NB: only looks for data atoms in a flat mapping (no algebraic (meta-)predicate such as LJ).
         */
        ImmutableList<Function> definingAtoms = bodyAtoms.stream()
                .filter(Function::isDataFunction)
                .filter(a -> a.containsTerm(variable))
                .collect(ImmutableCollectors.toList());

        switch(definingAtoms.size()) {
            case 0:
                // May happen if a meta-predicate is used
                return true;
            case 1:
                break;
            /*
             * Implicit joining conditions so not nullable.
             *
             * Rare.
             */
            default:
                return false;
        }

        Function definingAtom = definingAtoms.get(0);

        /*
         * Look for non-null
         */
        if (hasNonNullColumnForVariable(definingAtom, variable, metadata))
            return false;

        /*
         * TODO: check filtering conditions
         */

        /*
         * Implicit equality inside the data atom.
         *
         * Rare.
         */
        if (definingAtom.getTerms().stream()
                .filter(t -> t.equals(variable))
                .count() > 1) {
            return false;
        }

        /*
         * No constraint found --> may be null
         */
        return true;
    }

    private static boolean hasNonNullColumnForVariable(Function atom, Variable variable, DBMetadata metadata) {
        RelationID relationId = Relation2DatalogPredicate.createRelationFromPredicateName(metadata.getQuotedIDFactory(),
                atom.getFunctionSymbol());
        DatabaseRelationDefinition relation = metadata.getDatabaseRelation(relationId);

        if (relation == null)
            return false;

        List<Term> arguments = atom.getTerms();

        // NB: DB column indexes start at 1.
        return IntStream.range(1, arguments.size() + 1)
                .filter(i -> arguments.get(i - 1).equals(variable))
                .mapToObj(relation::getAttribute)
                .anyMatch(att -> !att.canNull());
    }
}
