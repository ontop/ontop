package it.unibz.inf.ontop.owlrefplatform.core.basicoperations;

import fj.F;
import fj.data.List;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.DatalogProgram;
import it.unibz.inf.ontop.datalog.MutableQueryModifiers;

import it.unibz.inf.ontop.datalog.impl.DatalogAlgebraOperatorPredicates;
import it.unibz.inf.ontop.model.term.TermConstants;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;

import java.util.ArrayList;

import static it.unibz.inf.ontop.model.OntopModelSingletons.DATALOG_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;

/**
 * Flattens JOINs and AND atoms that are found at the top of a Datalog body.
 *
 * Needed by the (weak) SQLGenerator.
 *
 */
public class FunctionFlattener {

    private final static List<Function> EMPTY_ATOM_LIST = List.nil();


    /**
     * TODO: explain
     */
    public static DatalogProgram flattenDatalogProgram(DatalogProgram program) {
        java.util.List<CQIE> newRules = new ArrayList<>();
        for(CQIE rule : program.getRules()) {
            newRules.add(flattenRule(rule));
        }

        MutableQueryModifiers newModifiers = program.getQueryModifiers().clone();
        return DATALOG_FACTORY.getDatalogProgram(newModifiers, newRules);
    }

    /**
     * TODO: explain
     */
    public static CQIE flattenRule(CQIE rule) {
        CQIE newRule = rule.clone();
        return DATALOG_FACTORY.getCQIE(newRule.getHead(), flattenTopJoinAndConjunctionAtoms(rule.getBody()));
    }


    /**
     * TODO: explain
     */
    public static java.util.List<Function> flattenTopJoinAndConjunctionAtoms(java.util.List<Function> topLevelBodyAtoms) {
        List<Function> flattenAtoms = flattenSomeAtoms(List.iterableList(topLevelBodyAtoms));
        return new ArrayList<>(flattenAtoms.toCollection());
    }

    /**
     * Flattens some atoms:
     *    - AND conjunctions
     *    - JOIN atoms
     */
    private static List<Function> flattenSomeAtoms(final List<Function> atoms) {
        return atoms.bind(new F<Function, List<Function>>() {
            @Override
            public List<Function> f(Function atom) {
                Predicate predicate = atom.getFunctionSymbol();

                if (predicate.equals(ExpressionOperation.AND)) {
                    return flattenAND(atom);
                }
                else if (predicate.equals(DatalogAlgebraOperatorPredicates.SPARQL_JOIN)) {
                    return flattenJoinAtom(atom);
                }
                else {
                    return List.cons(atom, EMPTY_ATOM_LIST);
                }
            }
        });
    }

    /**
     * TODO: improve it and make it recursive
     */
    private static List<Function> flattenAND(Function andAtom) {

        // Non-final
        List<Function> flattenedAtoms = List.nil();

        for (Term subTerm : andAtom.getTerms()) {
            if (subTerm instanceof Function) {
                Function subAtom = (Function) subTerm;
                /**
                 * Recursive call for nested AND(...)
                 */
                if (subAtom.getFunctionSymbol().equals(ExpressionOperation.AND)) {
                    flattenedAtoms = flattenedAtoms.append(flattenAND(subAtom));
                }
                else {
                    flattenedAtoms = flattenedAtoms.append(List.cons(subAtom, EMPTY_ATOM_LIST));
                }
            }
            else {
                Function newAtom = TERM_FACTORY.getFunctionAND(subTerm, TermConstants.TRUE);
               flattenedAtoms = flattenedAtoms.append(List.cons(newAtom, EMPTY_ATOM_LIST));
            }
        }
        return flattenedAtoms;
    }

    /**
     * TODO: explain
     */
    private static List<Function> flattenJoinAtom(Function joinAtom) {
        List<Function> subFunctionalTerms = convertToListofFunctions(joinAtom.getTerms());
        /**
         * Indirectly recursive
         */
        return flattenSomeAtoms(subFunctionalTerms);
    }

    private static List<Function> convertToListofFunctions(java.util.List<Term> functionalSubTerms) {
        return List.iterableList((java.util.List<Function>) (java.util.List<?>) functionalSubTerms);
    }
}
