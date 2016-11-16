package it.unibz.inf.ontop.owlrefplatform.core.translator;

import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extracted by Roman Kontchakov on 30/06/2016.
 */
public class SameAsRewriter {

    private final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

    private final Set<Predicate> dataPropertiesAndClassesSameAs;
    private final Set<Predicate> objectPropertiesSameAs;
    private int bnode; //count for bnode created in sameAsmap
    private int rules;

    public SameAsRewriter(Set<Predicate> dataPropertiesAndClassesSameAs, Set<Predicate> objectPropertiesSameAs ) {
        this.dataPropertiesAndClassesSameAs = dataPropertiesAndClassesSameAs;
        this.objectPropertiesSameAs = objectPropertiesSameAs;
        bnode = 0;
        rules = 0;
    }

    public DatalogProgram getSameAsRewriting(DatalogProgram pr) {
        DatalogProgram result = ofac.getDatalogProgram(pr.getQueryModifiers());

        for (CQIE q: pr.getRules()) {
            List<Function> body = new ArrayList<>(q.getBody().size());
            for (Function a : q.getBody()) {
                Function ap = addSameAs(a, result, "sameAs" + (rules++));
                body.add(ap);
            }
            result.appendRule(ofac.getCQIE(q.getHead(), body));
        }
        return result;
    }

    public Function addSameAs(Function atom, DatalogProgram pr, String newHeadName) {

        //case of class and data properties need as join only on the left
        if (dataPropertiesAndClassesSameAs.contains(atom.getFunctionSymbol()) ){
            Function rightAtomUnion = createJoinWithSameAsOnLeft(atom, pr, newHeadName + "1");
            //create union between the first statement and join
            //between hasProperty(x,y) and owl:sameAs(x, anon-x) hasProperty (anon-x, y)

            return createUnion(atom, rightAtomUnion, pr, newHeadName);
        }
        //case of object properties need as join only on the left and on the right
        else if (objectPropertiesSameAs.contains(atom.getFunctionSymbol())){
            //create union between the first join on the left and join on the right
            Function union2 = createUnionObject(atom, pr, newHeadName + "1");
            //create union between the first statement  and the union
            return createUnion(atom, union2, pr, newHeadName);
        }

        return atom;
    }

    private static Set<Variable> getVariables(Function atom) {
        Set<Variable> set = new HashSet<>();
        for (Term t : atom.getTerms())
            if (t instanceof Variable)
                set.add((Variable)t);
        return set;
    }

    private static List<Term> getUnion(Set<Variable> s1, Set<Variable> s2) {
        // take the union of the *sets* of variables
        Set<Term> vars = new HashSet<>();
        vars.addAll(s1);
        vars.addAll(s2);
        // order is chosen arbitrarily but this is not a problem
        // because it is chosen once and for all
        List<Term> varList = new ArrayList<>(vars);
        return varList;
    }

    private CQIE createRule(DatalogProgram pr, String headName, List<Term> headParameters, Function... body) {
        Predicate pred = ofac.getPredicate(headName, headParameters.size());
        Function head = ofac.getFunction(pred, headParameters);
        CQIE rule = ofac.getCQIE(head, body);
        pr.appendRule(rule);
        return rule;
    }

    private Function createUnionObject(Function leftAtom, DatalogProgram pr, String newHeadName) {
        Function union1 = createUnionUnbound (leftAtom, pr, newHeadName + "1");
        Function leftAtomUnion2 = createJoinWithSameAsOnRight(leftAtom, pr, newHeadName + "0");
        return createUnion(leftAtomUnion2, union1 ,pr, newHeadName);
    }

    private Function createUnionUnbound(Function leftAtom, DatalogProgram pr, String newHeadName){
        Function rightAtomUnionDouble = createJoinWithSameAsOnLeftAndRight(leftAtom, pr, newHeadName + "1");
        Function leftAtomUnion1 = createJoinWithSameAsOnLeft(leftAtom, pr, newHeadName + "0");
        return createUnion(leftAtomUnion1, rightAtomUnionDouble, pr, newHeadName);
    }

    private Function createJoinWithSameAsOnLeftAndRight(Function leftAtom, DatalogProgram pr, String newHeadName) {

        //ON THE RIGHT

        //create right atom of the join between the data property and same as
        //given a data property as hasProperty (x, y)

        //create an unbound  hasProperty (anon-x1, anon-y1)

        Function unboundleftAtom = ofac.getFunction(leftAtom.getFunctionSymbol());
        unboundleftAtom.updateTerms(leftAtom.getTerms());
        unboundleftAtom.setTerm(0, ofac.getVariable("anon-"+bnode+ leftAtom.getTerm(0)));
        unboundleftAtom.setTerm(1, ofac.getVariable("anon-"+bnode +leftAtom.getTerm(1)));

        //create statement pattern for same as create owl:sameAs(anon-y1, y)
        //it will be the right atom of the join
        Predicate sameAs = ofac.getOWLSameASPredicate();
        Term sTerm2 = unboundleftAtom.getTerm(1);
        Term oTerm2 = leftAtom.getTerm(1);
        Function rightAtomJoin2 = ofac.getFunction(sameAs, sTerm2, oTerm2);

        //create join rule
        List<Term> varListJoin2 = getUnion(getVariables(unboundleftAtom), getVariables(rightAtomJoin2));
        CQIE joinRule2 = createRule(pr, newHeadName + "0" , varListJoin2, unboundleftAtom, rightAtomJoin2);

        Function joinRight = joinRule2.getHead();

        //ON THE LEFT

        //given a data property ex hasProperty (x, y)
        //create statement pattern for same as create owl:sameAs( x, anon-x1)
        //it will be the left atom of the join

        Term sTerm = leftAtom.getTerm(0);
        Term oTerm = unboundleftAtom.getTerm(0);
        Function leftAtomJoin = ofac.getFunction(sameAs, sTerm, oTerm);

        //create join rule
        List<Term> varListJoin = getUnion(getVariables(leftAtomJoin), getVariables(joinRight));
        CQIE joinRule = createRule(pr, newHeadName , varListJoin, leftAtomJoin, joinRight);

        return joinRule.getHead();

    }

    private Function createUnion(Function leftAtom, Function rightAtom, DatalogProgram pr, String newHeadName) {
        Set<Variable> leftVars = getVariables(leftAtom);
        Set<Variable> rightVars = getVariables(rightAtom);
        List<Term> varListUnion = getUnion(leftVars, rightVars  );

        // left atom rule
        List<Term> leftTermList = new ArrayList<>(varListUnion.size());
        for (Term t : varListUnion) {
            Term lt =  (leftVars.contains(t)) ? t : OBDAVocabulary.NULL;
            leftTermList.add(lt);
        }
        CQIE leftRule = createRule(pr, newHeadName, leftTermList, leftAtom);

        // right atom rule
        List<Term> rightTermList = new ArrayList<>(varListUnion.size());
        for (Term t : varListUnion) {
            Term lt =  (rightVars.contains(t)) ? t : OBDAVocabulary.NULL;
            rightTermList.add(lt);
        }
        CQIE rightRule = createRule(pr, newHeadName, rightTermList, rightAtom);

        return ofac.getFunction(rightRule.getHead().getFunctionSymbol(), varListUnion);
    }

    private Function createJoinWithSameAsOnLeft(Function leftAtom, DatalogProgram pr, String newHeadName) {

        //create left atom of the join between the data property and same as
        //given a data property as hasProperty (x, y)
        //create the left atom hasProperty (anon-x, y)

        Function leftAtomJoin =  ofac.getFunction(leftAtom.getFunctionSymbol());
        leftAtomJoin.updateTerms(leftAtom.getTerms());
        leftAtomJoin.setTerm(0, ofac.getVariable("anon-" +bnode +leftAtom.getTerm(0)));

        //given a data property ex hasProperty (x, y)
        //create statement pattern for same as create owl:sameAs( anon-x, y)
        //it will be the right atom of the join
        Predicate predicate = ofac.getOWLSameASPredicate();
        Term sTerm = leftAtom.getTerm(0);
        Term oTerm = ofac.getVariable("anon-"+ bnode +leftAtom.getTerm(0));
        Function rightAtomJoin = ofac.getFunction(predicate, sTerm, oTerm);

        //create join rule
        List<Term> varListJoin = getUnion(getVariables(leftAtomJoin), getVariables(rightAtomJoin));
        CQIE joinRule = createRule(pr, newHeadName  , varListJoin, leftAtomJoin, rightAtomJoin);

        bnode++;
        return joinRule.getHead();
    }

    private Function createJoinWithSameAsOnRight(Function leftAtom, DatalogProgram pr, String newHeadName) {

        //create right atom of the join between the data property and same as
        //given a data property as hasProperty (x, y)
        //create the left atom hasProperty (x, anon-y)

        Function leftAtomJoin2 =  ofac.getFunction(leftAtom.getFunctionSymbol());
        leftAtomJoin2.updateTerms(leftAtom.getTerms());
        leftAtomJoin2.setTerm(1, ofac.getVariable("anon-"+bnode +leftAtom.getTerm(1)));

        //create statement pattern for same as create owl:sameAs(anon-y, y)
        //it will be the right atom of the join

        Predicate predicate = ofac.getOWLSameASPredicate();
        Term sTerm2 = ofac.getVariable("anon-"+ bnode +leftAtom.getTerm(1));
        Term oTerm2 = leftAtom.getTerm(1);
        Function rightAtomJoin2 = ofac.getFunction(predicate, sTerm2, oTerm2);

        //create join rule
        List<Term> varListJoin2 = getUnion(getVariables(leftAtomJoin2), getVariables(rightAtomJoin2));
        CQIE joinRule2 = createRule(pr, newHeadName , varListJoin2, leftAtomJoin2, rightAtomJoin2);

        bnode++;
        return joinRule2.getHead();
    }
}
