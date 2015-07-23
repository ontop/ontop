package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import fj.F;
import fj.data.List;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;

import java.util.ArrayList;

/**
 * Flattens JOINs and AND atoms that are found at the top of a Datalog body.
 *
 * Needed by the (weak) SQLGenerator.
 *
 */
public class FunctionFlattener {

    private final static List<Function> EMPTY_ATOM_LIST = List.nil();
    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();


    /**
     * TODO: explain
     */
    public static DatalogProgram flattenDatalogProgram(DatalogProgram program) {
        java.util.List<CQIE> newRules = new ArrayList<>();
        for(CQIE rule : program.getRules()) {
            newRules.add(flattenRule(rule));
        }

        OBDAQueryModifiers newModifiers = program.getQueryModifiers().clone();
        return DATA_FACTORY.getDatalogProgram(newModifiers, newRules);
    }

    /**
     * TODO: explain
     */
    public static CQIE flattenRule(CQIE rule) {
        CQIE newRule = rule.clone();
        return DATA_FACTORY.getCQIE(newRule.getHead(), flattenTopJoinAndConjunctionAtoms(rule.getBody()));
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

                if (predicate.equals(OBDAVocabulary.AND)) {
                    return flattenAND(atom);
                }
                else if (predicate.equals(OBDAVocabulary.SPARQL_JOIN)) {
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
        java.util.List<Term> subTerms = andAtom.getTerms();
        for (Term subTerm : subTerms) {
            if (!(subTerm instanceof Function)) {
                return List.cons(andAtom, EMPTY_ATOM_LIST);
            }
        }
        return convertToListofFunctions(subTerms);
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
