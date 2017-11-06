package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.datalog.DatalogProgram;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.answering.reformulation.rewriting.SameAsRewriter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extracted by Roman Kontchakov on 30/06/2016.
 *
 * Only applies to unary and binary atoms (no support yet for atoms of the form triple(s, p, o))
 */
public class SameAsRewriterImpl implements SameAsRewriter{

    private final MappingSameAsPredicateExtractor predicateExtractor;
    private final Mapping saturatedMapping;
    private int bnode; //count for bnode created in sameAsmap
    private int rules;
    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final DatalogFactory datalogFactory;

    @AssistedInject
    private SameAsRewriterImpl(@Assisted Mapping saturatedMapping, MappingSameAsPredicateExtractor predicateExtractor,
                               AtomFactory atomFactory, TermFactory termFactory, DatalogFactory datalogFactory) {
        this.predicateExtractor = predicateExtractor;
        this.saturatedMapping = saturatedMapping;
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.datalogFactory = datalogFactory;
        bnode = 0;
        rules = 0;
    }

    @Override
    public DatalogProgram getSameAsRewriting(DatalogProgram pr) {

        MappingSameAsPredicateExtractor.Result targetPredicates = predicateExtractor.extract(saturatedMapping);
        DatalogProgram result = datalogFactory.getDatalogProgram(pr.getQueryModifiers());

        for (CQIE q: pr.getRules()) {
            List<Function> body = new ArrayList<>(q.getBody().size());
            for (Function a : q.getBody()) {
                Function ap = addSameAs(a, result, "sameAs" + (rules++), targetPredicates);
                body.add(ap);
            }
            result.appendRule(datalogFactory.getCQIE(q.getHead(), body));
        }
        return result;
    }

    private Function addSameAs(Function atom, DatalogProgram pr, String newHeadName, MappingSameAsPredicateExtractor.Result targetPredicates) {

        //case of class and data properties need as join only on the left
        if (targetPredicates.isSubjectOnlySameAsRewritingTarget(atom.getFunctionSymbol()) ){
            Function rightAtomUnion = createJoinWithSameAsOnLeft(atom, pr, newHeadName + "1");
            //create union between the first statement and join
            //between hasProperty(x,y) and owl:sameAs(x, anon-x) hasProperty (anon-x, y)
            return createUnion(atom, rightAtomUnion, pr, newHeadName);
        }

        //case of object properties need as join only on the left and on the right
        if (targetPredicates.isTwoArgumentsSameAsRewritingTarget(atom.getFunctionSymbol())){
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
        Predicate pred = termFactory.getPredicate(headName, headParameters.size());
        Function head = termFactory.getFunction(pred, headParameters);
        CQIE rule = datalogFactory.getCQIE(head, body);
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

        Function unboundleftAtom = termFactory.getFunction(leftAtom.getFunctionSymbol());
        unboundleftAtom.updateTerms(leftAtom.getTerms());
        unboundleftAtom.setTerm(0, termFactory.getVariable("anon-"+bnode+ leftAtom.getTerm(0)));
        unboundleftAtom.setTerm(1, termFactory.getVariable("anon-"+bnode +leftAtom.getTerm(1)));

        //create statement pattern for same as create owl:sameAs(anon-y1, y)
        //it will be the right atom of the join
        Predicate sameAs = atomFactory.getOWLSameAsPredicate();
        Term sTerm2 = unboundleftAtom.getTerm(1);
        Term oTerm2 = leftAtom.getTerm(1);
        Function rightAtomJoin2 = termFactory.getFunction(sameAs, sTerm2, oTerm2);

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
        Function leftAtomJoin = termFactory.getFunction(sameAs, sTerm, oTerm);

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
            Term lt =  (leftVars.contains(t)) ? t : termFactory.getNullConstant();
            leftTermList.add(lt);
        }
        CQIE leftRule = createRule(pr, newHeadName, leftTermList, leftAtom);

        // right atom rule
        List<Term> rightTermList = new ArrayList<>(varListUnion.size());
        for (Term t : varListUnion) {
            Term lt =  (rightVars.contains(t)) ? t : termFactory.getNullConstant();
            rightTermList.add(lt);
        }
        CQIE rightRule = createRule(pr, newHeadName, rightTermList, rightAtom);

        return termFactory.getFunction(rightRule.getHead().getFunctionSymbol(), varListUnion);
    }

    private Function createJoinWithSameAsOnLeft(Function leftAtom, DatalogProgram pr, String newHeadName) {

        //create left atom of the join between the data property and same as
        //given a data property as hasProperty (x, y)
        //create the left atom hasProperty (anon-x, y)

        Function leftAtomJoin =  termFactory.getFunction(leftAtom.getFunctionSymbol());
        leftAtomJoin.updateTerms(leftAtom.getTerms());
        leftAtomJoin.setTerm(0, termFactory.getVariable("anon-" +bnode +leftAtom.getTerm(0)));

        //given a data property ex hasProperty (x, y)
        //create statement pattern for same as create owl:sameAs( anon-x, y)
        //it will be the right atom of the join
        Predicate predicate = atomFactory.getOWLSameAsPredicate();
        Term sTerm = leftAtom.getTerm(0);
        Term oTerm = termFactory.getVariable("anon-"+ bnode +leftAtom.getTerm(0));
        Function rightAtomJoin = termFactory.getFunction(predicate, sTerm, oTerm);

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

        Function leftAtomJoin2 =  termFactory.getFunction(leftAtom.getFunctionSymbol());
        leftAtomJoin2.updateTerms(leftAtom.getTerms());
        leftAtomJoin2.setTerm(1, termFactory.getVariable("anon-"+bnode +leftAtom.getTerm(1)));

        //create statement pattern for same as create owl:sameAs(anon-y, y)
        //it will be the right atom of the join

        Predicate predicate = atomFactory.getOWLSameAsPredicate();
        Term sTerm2 = termFactory.getVariable("anon-"+ bnode +leftAtom.getTerm(1));
        Term oTerm2 = leftAtom.getTerm(1);
        Function rightAtomJoin2 = termFactory.getFunction(predicate, sTerm2, oTerm2);

        //create join rule
        List<Term> varListJoin2 = getUnion(getVariables(leftAtomJoin2), getVariables(rightAtomJoin2));
        CQIE joinRule2 = createRule(pr, newHeadName , varListJoin2, leftAtomJoin2, rightAtomJoin2);

        bnode++;
        return joinRule2.getHead();
    }
}
