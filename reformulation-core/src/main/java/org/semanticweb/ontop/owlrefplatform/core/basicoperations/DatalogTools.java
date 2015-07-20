package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import fj.F;
import fj.F2;
import fj.data.List;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;

import java.util.ArrayList;

/**
 * Tool methods when manipulate some Datalog programs and their rules.
 */
public class DatalogTools {

    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private final static Function TRUE_EQ = DATA_FACTORY.getFunctionEQ(OBDAVocabulary.TRUE, OBDAVocabulary.TRUE);

    public static Boolean isDataOrLeftJoinOrJoinAtom(Function atom) {
        return atom.isDataFunction() || isLeftJoinOrJoinAtom(atom);
    }

    public static Boolean isLeftJoinOrJoinAtom(Function atom) {
        Predicate predicate = atom.getFunctionSymbol();
        return predicate.equals(OBDAVocabulary.SPARQL_LEFTJOIN) ||
                predicate.equals(OBDAVocabulary.SPARQL_JOIN);
    }

    /**
     * Folds a list of boolean atoms into one AND(AND(...)) boolean atom.
     */
    public static Function foldBooleanConditions(List<Function> booleanAtoms) {
        if (booleanAtoms.length() == 0)
            return TRUE_EQ;

        Function firstBooleanAtom = booleanAtoms.head();
        return booleanAtoms.tail().foldLeft(new F2<Function, Function, Function>() {
            @Override
            public Function f(Function previousAtom, Function currentAtom) {
                return DATA_FACTORY.getFunctionAND(previousAtom, currentAtom);
            }
        }, firstBooleanAtom);
    }

    /**
     * Folds a list of data/composite atoms and joining conditions into a JOIN(...) with a 3-arity.
     *
     */
    public static Function foldJoin(List<Function> dataOrCompositeAtoms, Function joiningCondition) {
        int length = dataOrCompositeAtoms.length();
        if (length < 2) {
            throw new IllegalArgumentException("At least two atoms should be given");
        }

        Function firstAtom = dataOrCompositeAtoms.head();
        Function secondAtom = foldJoin(dataOrCompositeAtoms.tail());

        return DATA_FACTORY.getSPARQLJoin(firstAtom, secondAtom, joiningCondition);
    }

    /**
     * Folds a list of data/composite atoms into a JOIN (if necessary)
     * by adding EQ(t,t) as a joining condition.
     */
    private static Function foldJoin(List<Function> dataOrCompositeAtoms) {
        int length = dataOrCompositeAtoms.length();
        if (length == 1)
            return dataOrCompositeAtoms.head();
        else if (length == 0)
            throw new IllegalArgumentException("At least one atom should be given.");
        else {
            Function firstAtom = dataOrCompositeAtoms.head();
            /**
             * Folds the data/composite atom list into a JOIN(JOIN(...)) meta-atom.
             */
            return dataOrCompositeAtoms.tail().foldLeft(new F2<Function, Function, Function>() {
                @Override
                public Function f(Function firstAtom, Function secondAtom) {
                    return DATA_FACTORY.getSPARQLJoin(firstAtom, secondAtom, TRUE_EQ);
                }
            }, firstAtom);
        }
    }

    /**
     * Practical criterion for detecting a real join: having more data/composite atoms.
     *
     * May produces some false negatives for crazy abusive nested joins of boolean atoms (using JOIN instead of AND).
     */
    public static boolean isRealJoin(List<Function> subAtoms) {
        //TODO: reuse a shared static filter fct object.
        List<Function> dataAndCompositeAtoms = subAtoms.filter(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return isDataOrLeftJoinOrJoinAtom(atom);
            }
        });

        return dataAndCompositeAtoms.length() > 1;
    }

    public static Function constructNewFunction(List<Function> subAtoms, Predicate functionSymbol) {
        List<Term> subTerms = (List<Term>)(List<?>) subAtoms;
        return constructNewFunction(functionSymbol, subTerms);
    }

    public static Function constructNewFunction(Predicate functionSymbol, List<Term> subTerms) {
        return DATA_FACTORY.getFunction(functionSymbol, new ArrayList<>(subTerms.toCollection()));
    }
}
