package it.unibz.inf.ontop.datalog.impl;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.datalog.exception.UnsupportedFeatureForDatalogConversionException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Stream;

/***
 * Translate a intermediate queries expression into a Datalog program that has the
 * same semantics. We use the built-int predicates Join and Left join. The rules
 * in the program have always 1 or 2 operator atoms, plus (in)equality atoms
 * (due to filters).
 *
 *
 * @author mrezk
 */
public class IQ2DatalogTranslatorImpl implements IQ2DatalogTranslator {

    private final IntermediateQueryFactory iqFactory;
    private final AtomFactory atomFactory;
    private final SubstitutionFactory substitutionFactory;
    private final DatalogFactory datalogFactory;
    private final ImmutabilityTools immutabilityTools;
    private final TermFactory termFactory;
    private final OrderByLifter orderByLifter;
    private final SliceLifter sliceLifter;
    private final DistinctLifter distinctLifter;

    private static class RuleHead {
        public final ImmutableSubstitution<ImmutableTerm> substitution;
        public final DataAtom atom;
        public final Optional<IQTree> optionalChildNode;

        private RuleHead(ImmutableSubstitution<ImmutableTerm> substitution, DataAtom atom, Optional<IQTree> optionalChildNode) {
            this.atom = atom;
            this.substitution = substitution;
            this.optionalChildNode = optionalChildNode;
        }
    }

    // Incremented
    private int subQueryCounter;
    private int dummyPredCounter;

    @Inject
    private IQ2DatalogTranslatorImpl(IntermediateQueryFactory iqFactory, AtomFactory atomFactory,
                                     SubstitutionFactory substitutionFactory, DatalogFactory datalogFactory,
                                     ImmutabilityTools immutabilityTools, TermFactory termFactory,
                                     OrderByLifter orderByLifter, SliceLifter sliceLifter, DistinctLifter distinctLifter) {
        this.iqFactory = iqFactory;
        this.atomFactory = atomFactory;
        this.substitutionFactory = substitutionFactory;
        this.datalogFactory = datalogFactory;
        this.immutabilityTools = immutabilityTools;
        this.termFactory = termFactory;
        this.orderByLifter = orderByLifter;
        this.sliceLifter = sliceLifter;
        this.distinctLifter = distinctLifter;
        this.subQueryCounter = 0;
        this.dummyPredCounter = 0;
    }

    /**
     * Translate an intermediate query tree into a Datalog program.
     * <p>
     * Each (strict) subquery will be translated as a rule with head Pred(var_1, .., var_n),
     * where the string for Pred is of the form SUBQUERY_PRED_PREFIX + y,
     * with y > subqueryCounter.
     */
    @Override
    public DatalogProgram translate(IQ initialQuery) throws UnsupportedFeatureForDatalogConversionException {
        return translate(initialQuery.getTree(), initialQuery.getProjectionAtom());
    }

    @Override
    public DatalogProgram translate(IQTree iqTree, ImmutableList<Variable> childSignature) throws UnsupportedFeatureForDatalogConversionException {
        return translate(iqTree, generateProjectionAtom(childSignature));
    }

    private DatalogProgram translate(IQTree initialTree, DistinctVariableOnlyDataAtom projectionAtom) throws UnsupportedFeatureForDatalogConversionException {

        checkQueryModifiers(initialTree, initialTree, 0, 0, 0, 0);

        IQTree distinctLiftedTree = liftDistinct(initialTree);
        IQTree sliceLiftedTree = liftSlice(distinctLiftedTree);
        IQTree orderLiftedTree = liftOrderBy(sliceLiftedTree);
        Optional<MutableQueryModifiers> optionalModifiers = extractTopQueryModifiers(orderLiftedTree);

        // Mutable
        DatalogProgram dProgram;
        if (optionalModifiers.isPresent()) {
            MutableQueryModifiers mutableModifiers = optionalModifiers.get();

            dProgram = datalogFactory.getDatalogProgram(mutableModifiers);
        } else {
            dProgram = datalogFactory.getDatalogProgram();
        }

        normalizeIQTree(orderLiftedTree)
                .forEach(t -> translate(t, dProgram, projectionAtom));

        // CQIEs are mutable
        dProgram.getRules().forEach(q -> unfoldJoinTrees(q.getBody()));

        return dProgram;
    }

    private void checkQueryModifiers(IQTree tree, IQTree fullTree, int slice, int distinct, int order, int nonCnOrQm) throws UnsupportedFeatureForDatalogConversionException {
        QueryNode root = tree.getRootNode();
        if (!(root instanceof LeafIQTree)) {

            if (root instanceof QueryModifierNode) {
                if(nonCnOrQm != 0){
                    throw new UnsupportedFeatureForDatalogConversionException(
                            String.format("Query modifier %s appears in the scope of an operator that is neither" +
                                    " a query modifier nor a Construction Node in tree %s", root, fullTree));
                }
                if(distinct != 0 && root instanceof DistinctNode){
                    throw new UnsupportedFeatureForDatalogConversionException(
                            String.format("Two Distinct operators in the tree %s",fullTree));
                }
                if(slice != 0 && root instanceof SliceNode){
                    throw new UnsupportedFeatureForDatalogConversionException(
                            String.format("Two Slice operators in the tree %s",fullTree));
                }
                if(order != 0 && root instanceof OrderByNode){
                    throw new UnsupportedFeatureForDatalogConversionException(
                            String.format("Two OrderBy operators in the tree %s",fullTree));
                }
                if(order != 0 && root instanceof SliceNode){
                    throw new UnsupportedFeatureForDatalogConversionException(
                            String.format("Slice in the scope of an OrderBy in the tree %s",fullTree));
                }
                if(order != 0 && root instanceof DistinctNode){
                    throw new UnsupportedFeatureForDatalogConversionException(
                            String.format("Distinct in the scope of an OrderBy in the tree %s",fullTree));
                }
                if(distinct != 0 && root instanceof SliceNode){
                    throw new UnsupportedFeatureForDatalogConversionException(
                            String.format("Slice in the scope of a Distinct in the tree %s",fullTree));
                }
                if(root instanceof DistinctNode){
                    distinct++;
                }else if(root instanceof OrderByNode){
                    order++;
                }else if(root instanceof SliceNode){
                    slice++;
                }
              checkQueryModifiers(((UnaryIQTree) tree).getChild(),fullTree,  slice, distinct, order, nonCnOrQm);
            } else {
                if (!(root instanceof ConstructionNode)) {
                    nonCnOrQm++;
                }
                for (IQTree child : tree.getChildren()) {
                    checkQueryModifiers(child, fullTree, slice, distinct, order, nonCnOrQm);
                }
            }
        }
    }


    /***
     * This expands all Join that can be directly added as conjuncts to a
     * query's body. Nested Join trees inside left joins are not touched.
     * <p>
     * In addition, we will remove any Join atoms that only contain one single
     * data atom, i.e., the join is not a join, but a table reference with
     * conditions. These kind of atoms can result from the partial evaluation
     * process and should be eliminated. The elimination takes all the atoms in
     * the join (the single data atom plus possibly extra boolean conditions and
     * adds them to the node that is the parent of the join).
     *
     * NEW: DOES NOT LOOK FOR fake JOINS INSIDE LJ "atoms"
     *
     */
    private void unfoldJoinTrees(List body) {
        for (int i = 0; i < body.size(); i++) {
            Function currentAtom = (Function) body.get(i);
            if (currentAtom.getFunctionSymbol().equals(datalogFactory.getSparqlJoinPredicate())) {
                unfoldJoinTrees(currentAtom.getTerms());
                body.remove(i);
                for (int j = currentAtom.getTerms().size() - 1; j >= 0; j--) {
                    Term term = currentAtom.getTerm(j);
                    if (!body.contains(term))
                        body.add(i, term);
                }
                i -= 1;
            }
        }
    }


    /**
     * Assumes that:
     * - modifiers are above the first construction node
     * - the order between modifiers (if present is) is Distinct - Slice - OrderBy
     * - each modifier appears at most once
     */
    private Optional<MutableQueryModifiers> extractTopQueryModifiers(IQTree tree) {
        QueryNode rootNode = tree.getRootNode();
        if (rootNode instanceof QueryModifierNode) {
            Optional<SliceNode> sliceNode = Optional.of(rootNode)
                    .filter(n -> n instanceof SliceNode)
                    .map(n -> (SliceNode) n);

            IQTree firstNonSliceTree = sliceNode
                    .map(n -> ((UnaryIQTree) tree).getChild())
                    .orElse(tree);

            Optional<DistinctNode> distinctNode = Optional.of(firstNonSliceTree)
                    .map(IQTree::getRootNode)
                    .filter(n -> n instanceof DistinctNode)
                    .map(n -> (DistinctNode) n);

            IQTree firstNonSliceDistinctTree = distinctNode
                    .map(n -> ((UnaryIQTree) firstNonSliceTree).getChild())
                    .orElse(firstNonSliceTree);

            Optional<OrderByNode> orderByNode = Optional.of(firstNonSliceDistinctTree)
                    .map(IQTree::getRootNode)
                    .filter(n -> n instanceof OrderByNode)
                    .map(n -> (OrderByNode) n);

            MutableQueryModifiers mutableQueryModifiers = new MutableQueryModifiersImpl();

            sliceNode.ifPresent(n -> {
                n.getLimit()
                        .ifPresent(mutableQueryModifiers::setLimit);
                long offset = n.getOffset();
                if (offset > 0)
                    mutableQueryModifiers.setOffset(offset);
            });

            if (distinctNode.isPresent())
                mutableQueryModifiers.setDistinct();

            orderByNode
                    .ifPresent(n -> n.getComparators()
                            .forEach(c -> convertOrderComparator(c, mutableQueryModifiers)));

            return Optional.of(mutableQueryModifiers);
        } else
            return Optional.empty();
    }

    private static void convertOrderComparator(OrderByNode.OrderComparator comparator,
                                               MutableQueryModifiers queryModifiers) {
        NonGroundTerm term = comparator.getTerm();
        if (term instanceof Variable)
            queryModifiers.addOrderCondition((Variable) term,
                    comparator.isAscending() ? OrderCondition.ORDER_ASCENDING : OrderCondition.ORDER_DESCENDING);
        else
            // TODO: throw a better exception
            throw new IllegalArgumentException("The Datalog representation only supports variable in order conditions");
    }

    /**
     * Assumes that ORDER BY is ABOVE the first construction node
     */
    private IQTree getFirstNonQueryModifierTree(IQTree initialTree) {
        // Non-final
        IQTree iqTree = initialTree;
        while (iqTree.getRootNode() instanceof QueryModifierNode) {
            iqTree = ((UnaryIQTree) iqTree).getChild();
        }
        return iqTree;
    }

    /**
     * Translate a given IntermediateQuery query object to datalog program.
     * Note that (the object ref of the) datalog program is passed as argument
     * <p>
     * Assumption: the root is a construction node
     *
     * @return Datalog program that represents the construction of the SPARQL
     * query.
     */
    private void translate(IQTree tree, DatalogProgram pr, DistinctVariableOnlyDataAtom projectionAtom) {
        QueryNode root = tree.getRootNode();

        Queue<RuleHead> heads = new LinkedList<>();

        ImmutableSubstitution<ImmutableTerm> topSubstitution = Optional.of(root)
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode) n)
                .map(ConstructionNode::getSubstitution)
                .orElseGet(substitutionFactory::getSubstitution);

        IQTree bodyTree = (tree.getRootNode() instanceof ConstructionNode)
                ? ((UnaryIQTree) tree).getChild()
                : tree;

        heads.add(new RuleHead(topSubstitution, projectionAtom, Optional.of(bodyTree)));

        // Mutable (append-only)
        Map<QueryNode, DataAtom> subQueryProjectionAtoms = new HashMap<>();
        subQueryProjectionAtoms.put(root, projectionAtom);

        //In heads we keep the heads of the sub-rules in the program, e.g. ans5() :- LeftJoin(....)
        while (!heads.isEmpty()) {

            RuleHead head = heads.poll();

            //Applying substitutions in the head.
            ImmutableList<? extends ImmutableTerm> substitutedHeadAtomArguments = head.substitution.apply(head.atom.getArguments());

            List<Function> atoms = new LinkedList<>();

            Function newHead = immutabilityTools.convertToMutableFunction(head.atom.getPredicate(),
                    substitutedHeadAtomArguments);

            //Constructing the rule
            CQIE newrule = datalogFactory.getCQIE(newHead, atoms);

            pr.appendRule(newrule);

            head.optionalChildNode.ifPresent(t -> {
                List<Function> uAtoms = getAtomFrom(t, heads, subQueryProjectionAtoms, false);
                newrule.getBody().addAll(uAtoms);
            });

        }
    }


    /**
     * This is the MAIN recursive method in this class!!
     * Takes a node and return the list of functions (atoms) that it represents.
     * Usually it will be a single atom, but it is different for the filter case.
     */
    private List<Function> getAtomFrom(IQTree tree, Queue<RuleHead> heads,
                                       Map<QueryNode, DataAtom> subQueryProjectionAtoms,
                                       boolean isNested) {

        List<Function> body = new ArrayList<>();

        /*
         * Basic Atoms
         */
        final QueryNode node = tree.getRootNode();

        if (node instanceof ConstructionNode) {
            ConstructionNode constructionNode = (ConstructionNode) node;
            DataAtom projectionAtom = Optional.ofNullable(
                    subQueryProjectionAtoms.get(constructionNode))
                    .orElseGet(() -> generateProjectionAtom(constructionNode.getVariables()));

            heads.add(new RuleHead(constructionNode.getSubstitution(), projectionAtom, Optional.of(((UnaryIQTree) tree).getChild())));
            subQueryProjectionAtoms.put(constructionNode, projectionAtom);
            Function mutAt = immutabilityTools.convertToMutableFunction(projectionAtom);
            body.add(mutAt);
            return body;

        } else if (node instanceof FilterNode) {
            ImmutableExpression filter = ((FilterNode) node).getFilterCondition();
            List<IQTree> children = tree.getChildren();
            body.addAll(getAtomFrom(children.get(0), heads, subQueryProjectionAtoms, true));

            filter.flattenAND()
                    .map(immutabilityTools::convertToMutableBooleanExpression)
                    .forEach(body::add);

            return body;


        } else if (node instanceof DataNode) {
            DataAtom atom = ((DataNode) node).getProjectionAtom();
            Function mutAt = immutabilityTools.convertToMutableFunction(atom);
            body.add(mutAt);
            return body;


            /**
             * Nested Atoms
             */
        } else if (node instanceof InnerJoinNode) {
            return getAtomsFromJoinNode((InnerJoinNode) node, tree, heads, subQueryProjectionAtoms, isNested);

        } else if (node instanceof LeftJoinNode) {
            Optional<ImmutableExpression> filter = ((LeftJoinNode) node).getOptionalFilterCondition();
            BinaryNonCommutativeIQTree ljTree = (BinaryNonCommutativeIQTree) tree;

            List<Function> atomsListLeft = getAtomFrom(ljTree.getLeftChild(), heads, subQueryProjectionAtoms, true);
            List<Function> atomsListRight = getAtomFrom(ljTree.getRightChild(), heads, subQueryProjectionAtoms, true);

            if (filter.isPresent()) {
                ImmutableExpression filter2 = filter.get();
                Expression mutFilter = immutabilityTools.convertToMutableBooleanExpression(filter2);
                Function newLJAtom = datalogFactory.getSPARQLLeftJoin(atomsListLeft, atomsListRight, Optional.of(mutFilter));
                body.add(newLJAtom);
                return body;
            } else {
                Function newLJAtom = datalogFactory.getSPARQLLeftJoin(atomsListLeft, atomsListRight, Optional.empty());
                body.add(newLJAtom);
                return body;
            }

        } else if (node instanceof UnionNode) {

//			Optional<ConstructionNode> parentNode = te.getParent(node)
//					.filter(p -> p instanceof ConstructionNode)
//					.map(p -> (ConstructionNode) p);
//
//			DistinctVariableOnlyDataAtom freshHeadAtom;
//			if(parentNode.isPresent()) {
//				freshHeadAtom = generateProjectionAtom(parentNode.get().getChildVariables());
//			}
//			else{
//				freshHeadAtom = generateProjectionAtom(((UnionNode) node).getVariables());
//			}
            DistinctVariableOnlyDataAtom freshHeadAtom = generateProjectionAtom(((UnionNode) node).getVariables());

            for (IQTree child : tree.getChildren()) {

                QueryNode childRoot = child.getRootNode();

                if (childRoot instanceof ConstructionNode) {
                    ConstructionNode cn = (ConstructionNode) childRoot;
                    Optional<IQTree> grandChild = Optional.of(((UnaryIQTree) child).getChild());
                    subQueryProjectionAtoms.put(cn, freshHeadAtom);
                    heads.add(new RuleHead(cn.getSubstitution(), freshHeadAtom, grandChild));
                } else {
                    ConstructionNode cn = iqFactory.createConstructionNode(((UnionNode) node).getVariables());
                    subQueryProjectionAtoms.put(cn, freshHeadAtom);
                    heads.add(new RuleHead(cn.getSubstitution(), freshHeadAtom, Optional.of(child)));
                }


            } //end for

            Function bodyAtom = immutabilityTools.convertToMutableFunction(freshHeadAtom);
            body.add(bodyAtom);
            return body;

        } else if (node instanceof TrueNode) {

            /**
             *
             * TODO: what should we do when it is the left child of a LJ?
             *
             * Add a 0-ary atom
             */
            //DataAtom projectionAtom = generateProjectionAtom(ImmutableSet.of());
            //heads.add(new RuleHead(new ImmutableSubstitutionImpl<>(ImmutableMap.of()), projectionAtom,Optional.empty()));
            //return body;
            if (isNested) {
                body.add(termFactory.getFunction(
                        datalogFactory.getDummyPredicate(++dummyPredCounter),
                        new ArrayList<>()));
            }
            // Otherwise, ignores it
            return body;

        } else {
            throw new UnsupportedOperationException("Unexpected type of node in the intermediate tree: " + node);
        }

    }

    private List<Function> getAtomsFromJoinNode(InnerJoinNode node, IQTree tree, Queue<RuleHead> heads,
                                                Map<QueryNode, DataAtom> subQueryProjectionAtoms,
                                                boolean isNested) {
        List<Function> body = new ArrayList<>();
        Optional<ImmutableExpression> filter = node.getOptionalFilterCondition();
        List<Function> atoms = new ArrayList<>();
        List<IQTree> listnode = tree.getChildren();
        for (IQTree child : listnode) {
            List<Function> atomsList = getAtomFrom(child, heads, subQueryProjectionAtoms, true);
            atoms.addAll(atomsList);
        }

        if (atoms.size() <= 1) {
            throw new IllegalArgumentException("Inconsistent IQ: an InnerJoinNode must have at least two children");
        }

        if (filter.isPresent()) {
            if (isNested) {
                ImmutableExpression filter2 = filter.get();
                Function mutFilter = immutabilityTools.convertToMutableBooleanExpression(filter2);
                Function newJ = getSPARQLJoin(atoms, Optional.of(mutFilter));
                body.add(newJ);
                return body;
            } else {
                body.addAll(atoms);
                filter.get().flattenAND()
                        .map(immutabilityTools::convertToMutableBooleanExpression)
                        .forEach(body::add);
                return body;
            }
        } else {
            Function newJ = getSPARQLJoin(atoms, Optional.empty());
            body.add(newJ);
            return body;
        }
    }

    private DistinctVariableOnlyDataAtom generateProjectionAtom(ImmutableSet<Variable> projectedVariables) {
        return generateProjectionAtom(ImmutableList.copyOf(projectedVariables));
    }

    private DistinctVariableOnlyDataAtom generateProjectionAtom(ImmutableList<Variable> projectedVariables) {
        AtomPredicate newPredicate = datalogFactory.getSubqueryPredicate("" + ++subQueryCounter, projectedVariables.size());
        return atomFactory.getDistinctVariableOnlyDataAtom(newPredicate, ImmutableList.copyOf(projectedVariables));
    }

    private Function getSPARQLJoin(List<Function> atoms, Optional<Function> optionalCondition) {
        int atomCount = atoms.size();
        Function rightTerm;

        switch (atomCount) {
            case 0:
            case 1:
                throw new IllegalArgumentException("A join requires at least two atoms");
            case 2:
                rightTerm = atoms.get(1);
                break;
            default:
                rightTerm = getSPARQLJoin(atoms.subList(1, atomCount), Optional.empty());
                break;
        }

        return optionalCondition.isPresent()
                ? datalogFactory.getSPARQLJoin(atoms.get(0), rightTerm, optionalCondition.get())
                : datalogFactory.getSPARQLJoin(atoms.get(0), rightTerm);
    }

    private ImmutableList<IQTree> normalizeIQTree(IQTree tree) {
        while (tree.getRootNode() instanceof QueryModifierNode) {
            tree = ((UnaryIQTree) tree).getChild();
        }
        ImmutableSet<Variable> projectedVariables = tree.getVariables();
        return splitRootUnion(tree)
                .map(t -> enforceRootCn(t, projectedVariables))
                .collect(ImmutableCollectors.toList());
    }

    private Stream<IQTree> splitRootUnion(IQTree tree) {
        return (tree.getRootNode() instanceof UnionNode) ?
                tree.getChildren().stream() :
                Stream.of(tree);
    }

    private IQTree enforceRootCn(IQTree tree, ImmutableSet<Variable> projectedVariables) {
        if (tree.getRootNode() instanceof ConstructionNode) {
            ConstructionNode currentRootNode = (ConstructionNode) tree.getRootNode();

            if (currentRootNode.getVariables().equals(projectedVariables))
                return tree;

            ConstructionNode newRootNode = iqFactory.createConstructionNode(projectedVariables,
                    currentRootNode.getSubstitution()
                            .reduceDomainToIntersectionWith(projectedVariables));

            return iqFactory.createUnaryIQTree(newRootNode, ((UnaryIQTree) tree).getChild());
        } else
            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(projectedVariables),
                    tree);
    }

    /**
     * Move ORDER BY above the highest construction node (required by Datalog)
     */
    private IQTree liftOrderBy(IQTree tree) {
        return orderByLifter.liftOrderBy(tree);
//        IQTree topNonQueryModifierTree = getFirstNonQueryModifierTree(tree);
//        if ((topNonQueryModifierTree instanceof UnaryIQTree)
//                && (((UnaryIQTree) topNonQueryModifierTree).getChild().getRootNode() instanceof OrderByNode)) {
//            return orderByLifter.liftOrderBy(tree);
//        }
//        return tree;
    }

    private IQTree liftSlice(IQTree iqTree) {
        return sliceLifter.liftSlice(iqTree);
    }

    private IQTree liftDistinct(IQTree iqTree) {
        return distinctLifter.liftDistinct(iqTree);
    }
}
