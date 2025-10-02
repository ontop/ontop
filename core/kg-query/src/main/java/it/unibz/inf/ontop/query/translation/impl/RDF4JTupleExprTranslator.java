package it.unibz.inf.ontop.query.translation.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInvalidKGQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.rdf.api.RDF;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.algebra.*;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RDF4JTupleExprTranslator {

    private final ImmutableMap<Variable, GroundTerm> externalBindings;
    private final @Nullable Dataset dataset;
    private final boolean treatBNodeAsVariable;

    private final SubstitutionFactory substitutionFactory;
    private final IntermediateQueryFactory iqFactory;
    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final FunctionSymbolFactory functionSymbolFactory;
    private final QueryRenamer queryRenamer;
    private final RDF rdfFactory;
    private final TypeFactory typeFactory;

    private final IQTreeTools iqTreeTools;
    private final IRIConstant subClassOfConstant;
    private final VariableGenerator variableGenerator;

    public RDF4JTupleExprTranslator(ImmutableMap<Variable, GroundTerm> externalBindings,
                                    @Nullable Dataset dataset,
                                    boolean treatBNodeAsVariable,
                                    CoreSingletons coreSingletons,
                                    RDF rdfFactory,
                                    IQTreeTools iqTreeTools) {
        this.externalBindings = externalBindings;
        this.dataset = dataset;
        this.treatBNodeAsVariable = treatBNodeAsVariable;
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.iqFactory = coreSingletons.getIQFactory();
        this.atomFactory = coreSingletons.getAtomFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.functionSymbolFactory = coreSingletons.getFunctionSymbolFactory();
        this.queryRenamer = coreSingletons.getQueryRenamer();
        this.typeFactory = coreSingletons.getTypeFactory();
        this.rdfFactory = rdfFactory;
        this.iqTreeTools = iqTreeTools;
        this.subClassOfConstant = termFactory.getConstantIRI(RDFS.SUBCLASSOF);
        this.variableGenerator = coreSingletons.getCoreUtilsFactory().createVariableGenerator(externalBindings.keySet());
    }

    public IQTree getTree(TupleExpr node) throws OntopUnsupportedKGQueryException, OntopInvalidKGQueryException {
        return translate(node).iqTree;
    }

    private TranslationResult translate(TupleExpr node) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException {
        if (node instanceof QueryRoot)
            return translate(((QueryRoot) node).getArg());

        if (node instanceof StatementPattern)
            return translate((StatementPattern) node);

        if (node instanceof Join)
            return translateJoinLikeNode((Join) node);

        if (node instanceof LeftJoin)
            return translateJoinLikeNode((LeftJoin) node);

        if (node instanceof Difference)
            return translate((Difference) node);

        if (node instanceof Union)
            return translate((Union) node);

        if (node instanceof Filter)
            return translate((Filter) node);

        if (node instanceof Projection)
            return translate((Projection) node);

        if (node instanceof Slice)
            return translate((Slice) node);

        if (node instanceof Distinct)
            return translate((Distinct) node);

        if (node instanceof Reduced)
            return translate((Reduced) node);

        if (node instanceof SingletonSet)
            return createTranslationResult(iqFactory.createTrueNode(), ImmutableSet.of());

        if (node instanceof Group)
            return translate((Group) node);

        if (node instanceof Extension)
            return translate((Extension) node);

        if (node instanceof BindingSetAssignment)
            return translate((BindingSetAssignment) node);

        if (node instanceof Order)
            return translate((Order) node);

        if (node instanceof ArbitraryLengthPath) {
            return translate((ArbitraryLengthPath) node);
        }

        throw new OntopUnsupportedKGQueryException("Unsupported SPARQL operator: " + node.toString());
    }

    private static Sets.SetView<Variable> getSharedVariables(TranslationResult left, TranslationResult right) {
        return Sets.intersection(left.iqTree.getVariables(), right.iqTree.getVariables());
    }

    private TranslationResult translate(Difference diff) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException {

        TranslationResult leftTranslation = translate(diff.getLeftArg());
        TranslationResult rightTranslation = translate(diff.getRightArg());

        Sets.SetView<Variable> sharedVariables = getSharedVariables(rightTranslation, leftTranslation);

        if (sharedVariables.isEmpty()) {
            return leftTranslation;
        }

        return translateMinusOperation(leftTranslation, rightTranslation, sharedVariables);
    }

    private ImmutableList<ImmutableExpression> getEqOrNullable(Variable sharedVar, Variable renamedVar, ImmutableSet<Variable> leftNullableVars,
                                                               ImmutableSet<Variable> rightNullableVars) {

        ImmutableExpression equality = termFactory.getStrictEquality(sharedVar, renamedVar);

        if (leftNullableVars.contains(sharedVar)) {
            return rightNullableVars.contains(sharedVar)
                    ? ImmutableList.of(equality, termFactory.getDBIsNull(sharedVar), termFactory.getDBIsNull(renamedVar))
                    : ImmutableList.of(equality, termFactory.getDBIsNull(sharedVar));
        }
        else {
            return rightNullableVars.contains(sharedVar)
                    ? ImmutableList.of(equality, termFactory.getDBIsNull(renamedVar))
                    : ImmutableList.of(equality);
        }
    }


    private TranslationResult translate(Group group) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException {
        TranslationResult child = translate(group.getArg());

        // Assumption: every variable used in a definition is itself defined either in the subtree of in a previous ExtensionElem
        ValueExpressionResult<ImmutableList<Substitution<ImmutableTerm>>> varDefsResult =
                getGroupVarDefs(group.getGroupElements(), child.iqTree.getVariables());

        ImmutableList<Substitution<ImmutableTerm>> mergedVarDefs = varDefsResult.getResult();
        if (mergedVarDefs.size() > 1) {
            throw new Sparql2IqConversionException("Unexpected parsed SPARQL query: nested complex projections appear " +
                    "within an RDF4J Group node: " + group);
        }

        IQTree childTree;
        if (varDefsResult.existsMap.isEmpty()) {
            childTree = child.iqTree;
        }
        else {
            childTree = translateExists(varDefsResult.getExistsMap(), child);
        }

        ImmutableSet<Variable> childVariables = childTree.getVariables();
        AggregationNode an = iqFactory.createAggregationNode(
                group.getGroupBindingNames().stream()
                        .map(termFactory::getVariable)
                        .filter(childVariables::contains)
                        .collect(ImmutableCollectors.toSet()),
                mergedVarDefs.get(0).transform(t -> (ImmutableFunctionalTerm)t)); // only one substitution guaranteed by the if

        UnaryIQTree aggregationTree = iqFactory.createUnaryIQTree(an, childTree);

        ImmutableSet<Variable> nullableVariables = Sets.union(
                        Sets.intersection(an.getGroupingVariables(), child.nullableVariables),
                        an.getSubstitution().getPreImage(t -> t.getFunctionSymbol().isNullable(ImmutableSet.of(0))))
                .immutableCopy();

        IQTree iqTree = applyExternalBindingFilter(aggregationTree, an.getSubstitution().getDomain());
        return createTranslationResult(iqTree, nullableVariables);
    }

    private ValueExpressionResult<ImmutableList<Substitution<ImmutableTerm>>> getGroupVarDefs(List<GroupElem> list,
                                                                       ImmutableSet<Variable> childVariables) throws OntopUnsupportedKGQueryException {
        List<VarDef> result = new ArrayList<>();
        Set<Variable> allowedVars = new HashSet<>(childVariables); // mutable: accumulator
        ImmutableMap.Builder<Variable, Exists> existsBuilder = ImmutableMap.builder();

        for (GroupElem elem : list) {
            RDF4JValueExprTranslator.ExtendedTerm term = getValueTranslator(allowedVars).getTerm(elem.getOperator());

            Variable definedVar = termFactory.getVariable(elem.getName());
            allowedVars.add(definedVar);

            result.add(new VarDef(definedVar, term.getTerm()));
            existsBuilder.putAll(term.getExistsMap());
        }
        return new ValueExpressionResult<>(mergeVarDefs(ImmutableList.copyOf(result)), existsBuilder.build());
    }

    private TranslationResult translate(Order order) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException {
        TranslationResult child = translate(order.getArg());
        RDF4JValueExprTranslator translator = getValueTranslator(child.iqTree.getVariables());

        ImmutableMap<OrderElem, Optional<RDF4JValueExprTranslator.ExtendedTerm>> orderElements = order.getElements().stream()
                .collect(ImmutableCollectors.toMap(
                        o -> o,
                        o -> Optional.of(translator.getTerm(o.getExpr()))
                ));

        ImmutableList<OrderByNode.OrderComparator> comparators = orderElements.entrySet().stream()
                .map(e -> e.getValue()
                        .filter(t -> t.getTerm() instanceof NonGroundTerm)
                        .map(t -> (NonGroundTerm) t.getTerm())
                        .map(t -> iqFactory.createOrderComparator(t, e.getKey().isAscending())))
                .flatMap(Optional::stream)
                .collect(ImmutableCollectors.toList());

        var existsMaps = orderElements.values().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(RDF4JValueExprTranslator.ExtendedTerm::getExistsMap)
                .filter(map -> !map.isEmpty())
                .collect(ImmutableCollectors.toList());

        IQTree childTree;
        if (existsMaps.isEmpty()) {
            childTree = child.iqTree;
        }
        else {
            ImmutableList.Builder<IQTree> builder = ImmutableList.builder();
            for (ImmutableMap<Variable, Exists> entry : existsMaps) {
                builder.add(translateExists(entry, child));
            }
            ImmutableList<IQTree> orderElementsExistsSubtrees = builder.build();

            childTree = orderElementsExistsSubtrees.get(0);
            childTree = orderElementsExistsSubtrees.stream()
                    .skip(1)
                    .reduce(childTree, this::createNonConflictingRenamingLeftJoin);
        }

        return comparators.isEmpty()
                ? createTranslationResult(childTree, child.nullableVariables)
                : createTranslationResult(
                    iqFactory.createUnaryIQTree(iqFactory.createOrderByNode(comparators), childTree),
                    child.nullableVariables);
    }

    /**
     * rdfs:subClassOf* is supported.
     */
    private TranslationResult translate(ArbitraryLengthPath arbitraryLengthPath) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException {
        var childTree = translate(arbitraryLengthPath.getPathExpression())
                            .iqTree;
        if (childTree instanceof IntensionalDataNode) {
            var childAtom = ((IntensionalDataNode) childTree).getProjectionAtom();
            var atomArguments = childAtom.getArguments();

            if ((childAtom.getPredicate() instanceof RDFAtomPredicate)
                    && childAtom.getTerm(1).equals(subClassOfConstant)) {
                var atomPredicate = (RDFAtomPredicate) childAtom.getPredicate();
                VariableOrGroundTerm subject = atomPredicate.getSubject(atomArguments);
                VariableOrGroundTerm object = atomPredicate.getObject(atomArguments);

                IQTree pathZeroDepthChild;
                if ((!subject.isGround()) && object.isGround()) {
                     pathZeroDepthChild = iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(childTree.getVariables(),
                                    substitutionFactory.getSubstitution((Variable) subject, object)),
                            iqFactory.createTrueNode());
                }
                else if (subject.isGround() && object.isGround()) {
                    pathZeroDepthChild = iqFactory.createUnaryIQTree(
                            iqFactory.createFilterNode(termFactory.getStrictEquality(subject, object)),
                            iqFactory.createTrueNode());
                }
                // Var - Var case
                else {
                    var graphTerm = atomPredicate.getGraph(atomArguments);

                    var zeroDepthAtom = graphTerm
                            .map(g -> atomFactory.getGraphNodeAtom(object, g))
                            .orElseGet(() -> atomFactory.getDefaultGraphNodeAtom(object));

                    var parentNode = graphTerm.filter(g -> g.equals(subject))
                            .<UnaryOperatorNode>map(g -> iqFactory.createFilterNode(
                                    termFactory.getStrictEquality(g, object)))
                            .orElseGet(() -> iqFactory.createConstructionNode(childTree.getVariables(),
                                    substitutionFactory.getSubstitution((Variable) subject, object)));

                    pathZeroDepthChild = iqFactory.createUnaryIQTree(parentNode,
                            iqFactory.createIntensionalDataNode(zeroDepthAtom));
                }

                var newTree = iqFactory.createUnaryIQTree(
                        iqFactory.createDistinctNode(),
                        iqTreeTools.createUnionTree(childTree.getVariables(),
                                ImmutableList.of(
                                        pathZeroDepthChild,
                                        // Depth 1. Takes advantage that Ontop computes the transitive closure of rdfs:subClassOf
                                        childTree)));
                return createTranslationResult(newTree, ImmutableSet.of());
            }
        }
        throw new OntopUnsupportedKGQueryException("Unsupported arbitrary length path: " + arbitraryLengthPath);
    }


    private TranslationResult translate(BindingSetAssignment node) {

        ImmutableSet<Variable> allVars = node.getBindingNames().stream()
                .map(termFactory::getVariable)
                .collect(ImmutableCollectors.toSet());

        RDF4JValueTranslator translator = getValueTranslator();

        ImmutableList<Substitution<ImmutableTerm>> substitutions =
                StreamSupport.stream(node.getBindingSets().spliterator(), false)
                        .map(bs -> bs.getBindingNames().stream()
                                .collect(substitutionFactory.toSubstitution(
                                        termFactory::getVariable,
                                        x -> Optional.ofNullable(bs.getBinding(x))
                                                .map(Binding::getValue)
                                                .<ImmutableTerm>map(translator::getTermForLiteralOrIri)
                                                .orElseGet(termFactory::getNullConstant))))
                        .collect(ImmutableCollectors.toList());

        ImmutableSet<Variable> nullableVars = substitutions.get(0).getDomain().stream()
                .filter(v -> substitutions.stream().anyMatch(s -> s.get(v).isNull()))
                .collect(ImmutableCollectors.toSet());

        ImmutableList<IQTree> subtrees = substitutions.stream()
                .map(substitution -> iqFactory.createUnaryIQTree(
                        iqTreeTools.createExtendingConstructionNode(ImmutableSet.of(), substitution),
                        iqFactory.createTrueNode()))
                .collect(ImmutableCollectors.toList());

        IQTree tree = subtrees.size() == 1
                ? subtrees.get(0)
                : iqTreeTools.createUnionTree(allVars, subtrees);

        IQTree iqTree = applyExternalBindingFilter(tree, allVars);
        return createTranslationResult(iqTree, nullableVars);
    }

    private TranslationResult translate(Reduced reduced) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException {
        TranslationResult child = translate(reduced.getArg());
        return createTranslationResult(
                iqFactory.createUnaryIQTree(iqFactory.createDistinctNode(), child.iqTree),
                child.nullableVariables);
    }

    private TranslationResult translate(Distinct distinct) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException {
        TranslationResult child = translate(distinct.getArg());
        return createTranslationResult(
                iqFactory.createUnaryIQTree(iqFactory.createDistinctNode(), child.iqTree),
                child.nullableVariables);
    }

    private TranslationResult translate(Slice slice) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException {
        TranslationResult child = translate(slice.getArg());

        // Assumption: at least the limit or the offset is not -1 (otherwise the rdf4j parser would not generate a slice node)
        long offset = slice.getOffset() == -1
                ? 0
                : slice.getOffset();

        return createTranslationResult(
                iqFactory.createUnaryIQTree(
                        slice.getLimit() == -1
                            ? iqFactory.createSliceNode(offset)
                            : iqFactory.createSliceNode(offset, slice.getLimit()),
                        child.iqTree),
                child.nullableVariables);
    }

    private TranslationResult translate(Filter filter) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException {
        TranslationResult child = translate(filter.getArg());

        ValueExpr condition = filter.getCondition();

        if (condition instanceof Not && ((Not) condition).getArg() instanceof Exists) {
            Exists exists = (Exists) ((Not) condition).getArg();
            return translateNotExists(exists, child);
        }
        ValueExpressionResult<ImmutableExpression> filterCondition = getFilterExpression(condition, child.iqTree.getVariables());

        if (filterCondition.getExistsMap().isEmpty()) {
            return createTranslationResult(
                    iqFactory.createUnaryIQTree(iqFactory.createFilterNode(filterCondition.getResult()), child.iqTree),
                    child.nullableVariables);
        }

        IQTree mergedExistsSubTree = translateExists(filterCondition.getExistsMap(), child);

        return createTranslationResult(
                iqFactory.createUnaryIQTree(iqFactory.createFilterNode(filterCondition.getResult()), mergedExistsSubTree),
                child.nullableVariables);
    }

    private IQTree createExistsSubtree(Exists exists, Variable rightProvenanceVar, TranslationResult leftTranslation) throws OntopUnsupportedKGQueryException, OntopInvalidKGQueryException {
        TranslationResult rightTranslation = translate(exists.getSubQuery());

        checkIfExistsIsTranslatable(leftTranslation, rightTranslation, exists.getSubQuery());

        Sets.SetView<Variable> sharedVariables = getSharedVariables(rightTranslation, leftTranslation);
        variableGenerator.registerAdditionalVariables(Sets.union(
                leftTranslation.iqTree.getKnownVariables(), rightTranslation.iqTree.getKnownVariables()));

        InjectiveSubstitution<Variable> sharedVarsRenaming = getFreshRenamingSubstitution(sharedVariables);

        ImmutableExpression ljCond = termFactory.getConjunction(Stream.concat(
                        sharedVarsRenaming.builder()
                                .toStream((v, t) -> termFactory.getDisjunction(
                                        getEqOrNullable(v, t, leftTranslation.nullableVariables, rightTranslation.nullableVariables))),
                        Stream.of(termFactory.getDisjunction(sharedVarsRenaming.builder()
                                .toStream(termFactory::getStrictEquality).collect(ImmutableCollectors.toList()))))
                .collect(ImmutableCollectors.toList()));
        InjectiveSubstitution<Variable> leftNonProjVarsRenaming = getNonProjVarsRenaming(leftTranslation.iqTree, rightTranslation.iqTree);
        InjectiveSubstitution<Variable> rightNonProjVarsRenaming = getNonProjVarsRenaming(rightTranslation.iqTree, leftTranslation.iqTree);

        IQTree tree = applyDownPropagationWithoutOptimization(rightTranslation.iqTree, sharedVarsRenaming);

        IQTree renamedRightTree = applyInDepthRenaming(
                getRightConflictingProvenanceRenaming(rightTranslation, rightProvenanceVar),
                applyInDepthRenaming(rightNonProjVarsRenaming, tree));

        ImmutableSet<Variable> projectedVariables = Sets.union(
                ImmutableSet.of(rightProvenanceVar),
                Sets.intersection(sharedVarsRenaming.getRangeSet(), renamedRightTree.getVariables())).immutableCopy();

        ConstructionNode rightConstructionNode = iqFactory.createConstructionNode(projectedVariables,
                substitutionFactory.getSubstitution(rightProvenanceVar, termFactory.getProvenanceSpecialConstant()));

        IQTree distinctRightTree = iqFactory.createUnaryIQTree(
                iqFactory.createDistinctNode(),
                iqFactory.createUnaryIQTree(rightConstructionNode, renamedRightTree));

        return iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(
                    Sets.union(leftTranslation.iqTree.getVariables(), ImmutableSet.of(rightProvenanceVar)).immutableCopy()),
                iqFactory.createBinaryNonCommutativeIQTree(iqFactory.createLeftJoinNode(ljCond),
                        applyInDepthRenaming(leftNonProjVarsRenaming, leftTranslation.iqTree),
                        distinctRightTree));
    }

    private IQTree translateExists(ImmutableMap<Variable, Exists> existsMap, TranslationResult leftTranslation) throws OntopUnsupportedKGQueryException, OntopInvalidKGQueryException {
        IQTree subtree = null;
        for (Map.Entry<Variable, Exists> entry : existsMap.entrySet()) {
            IQTree newTree = createExistsSubtree(entry.getValue(), entry.getKey(), leftTranslation);
            if (subtree == null) {
                subtree = newTree;
            } else {
                subtree = createNonConflictingRenamingLeftJoin(newTree, subtree);
            }
        }

        return subtree;
    }

    private IQTree createNonConflictingRenamingLeftJoin(IQTree leftTree, IQTree rightTree) {
        var rightNonProjVarsRenaming = getNonProjVarsRenaming(rightTree, leftTree);

        var sharedVariables = Sets.intersection(leftTree.getVariables(), rightTree.getVariables());
        var sharedVarsRenaming = getFreshRenamingSubstitution(sharedVariables);

        var ljCond = termFactory.getConjunction(sharedVarsRenaming.builder()
                                .toStream(termFactory::getStrictEquality).collect(ImmutableCollectors.toList()));

        return iqFactory.createBinaryNonCommutativeIQTree(
                iqFactory.createLeftJoinNode(ljCond),
                leftTree,
                applyInDepthRenaming(rightNonProjVarsRenaming,
                        applyDownPropagationWithoutOptimization(rightTree, sharedVarsRenaming)));
    }

    private TranslationResult translateNotExists(Exists exists, TranslationResult leftTranslation) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException {
        TranslationResult rightTranslation = translate(exists.getSubQuery());

        checkIfExistsIsTranslatable(leftTranslation, rightTranslation, exists.getSubQuery());

        Sets.SetView<Variable> sharedVariables = getSharedVariables(rightTranslation, leftTranslation);
        return translateMinusOperation(leftTranslation, rightTranslation, sharedVariables);
    }

    private TranslationResult translateMinusOperation(TranslationResult leftTranslation, TranslationResult rightTranslation, Sets.SetView<Variable> sharedVariables) {
        variableGenerator.registerAdditionalVariables(Sets.union(
                leftTranslation.iqTree.getKnownVariables(), rightTranslation.iqTree.getKnownVariables()));

        InjectiveSubstitution<Variable> sharedVarsRenaming = getFreshRenamingSubstitution(sharedVariables);

        ImmutableExpression ljCond = termFactory.getConjunction(Stream.concat(
                        sharedVarsRenaming.builder()
                                .toStream((v, t) -> termFactory.getDisjunction(
                                        getEqOrNullable(v, t, leftTranslation.nullableVariables, rightTranslation.nullableVariables))),
                        Stream.of(termFactory.getDisjunction(sharedVarsRenaming.builder()
                                .toStream(termFactory::getStrictEquality).collect(ImmutableCollectors.toList()))))
                .collect(ImmutableCollectors.toList()));

        ImmutableExpression filter = termFactory.getConjunction(sharedVarsRenaming.getRangeSet().stream()
                .map(termFactory::getDBIsNull)
                .collect(ImmutableCollectors.toList()));

        InjectiveSubstitution<Variable> leftNonProjVarsRenaming = getNonProjVarsRenaming(leftTranslation.iqTree, rightTranslation.iqTree);
        InjectiveSubstitution<Variable> rightNonProjVarsRenaming = getNonProjVarsRenaming(rightTranslation.iqTree, leftTranslation.iqTree);

        return createTranslationResult(
                iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(leftTranslation.iqTree.getVariables()),
                        iqFactory.createUnaryIQTree(iqFactory.createFilterNode(filter),
                                iqFactory.createBinaryNonCommutativeIQTree(iqFactory.createLeftJoinNode(ljCond),
                                        applyInDepthRenaming(leftNonProjVarsRenaming, leftTranslation.iqTree),
                                        applyInDepthRenaming(rightNonProjVarsRenaming,
                                                applyDownPropagationWithoutOptimization(rightTranslation.iqTree, sharedVarsRenaming))))),
                leftTranslation.nullableVariables);
    }

    private void checkIfExistsIsTranslatable(TranslationResult leftTranslation, TranslationResult rightTranslation, TupleExpr existsSubquery) throws OntopUnsupportedKGQueryException {
        Sets.SetView<Variable> sharedVariables = getSharedVariables(leftTranslation, rightTranslation);

        if (sharedVariables.isEmpty()) {
            throw new OntopUnsupportedKGQueryException("The EXISTS operator is not supported with no common variables");
        }

        if (sharedVariables.stream().anyMatch(v -> v.isNullable(leftTranslation.nullableVariables)
                || v.isNullable(rightTranslation.nullableVariables))) {
            throw new OntopUnsupportedKGQueryException("The EXISTS operator is not supported when there are non-nullable common variables");
        }

        ExistsSubtreeVisitor existsVisitor = new ExistsSubtreeVisitor(termFactory, existsSubquery);
        if (!existsVisitor.isExistsSubtreeSupported()) {
            throw new OntopUnsupportedKGQueryException("The EXISTS subquery is not supported: " + existsSubquery);
        }

        Sets.SetView<Variable> unboundVariables = Sets.difference(existsVisitor.getVariables(), rightTranslation.iqTree.getKnownVariables());
        if (!unboundVariables.isEmpty()) {
            throw new OntopUnsupportedKGQueryException("Some of the variables in the EXISTS subquery are unbound" + unboundVariables);
        }

        Sets.SetView<Variable> existsSubtreeAndLeft = Sets.intersection(leftTranslation.iqTree.getVariables(), existsVisitor.getVariables());
        if (!Sets.difference(existsSubtreeAndLeft, sharedVariables).isEmpty()) {
            throw new OntopUnsupportedKGQueryException("Some of the variables in the EXISTS subquery are shared with the left subtree but are not projected: " + existsSubtreeAndLeft);
        }
    }

    private TranslationResult translateJoinLikeNode(BinaryTupleOperator join) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException {

        TranslationResult leftTranslation = translate(join.getLeftArg());
        TranslationResult rightTranslation = translate(join.getRightArg());

        Set<Variable> nullableVariablesLeftOrRight = Sets.union(leftTranslation.nullableVariables, rightTranslation.nullableVariables);

        Set<Variable> sharedVariables = getSharedVariables(leftTranslation, rightTranslation);

        Set<Variable> toCoalesce = Sets.intersection(sharedVariables, nullableVariablesLeftOrRight);

        variableGenerator.registerAdditionalVariables(Sets.union(
                leftTranslation.iqTree.getKnownVariables(), rightTranslation.iqTree.getKnownVariables()));

        // May update the variable generator!!
        InjectiveSubstitution<Variable> leftRenamingSubstitution = getFreshRenamingSubstitution(toCoalesce);
        InjectiveSubstitution<Variable> rightRenamingSubstitution = getFreshRenamingSubstitution(toCoalesce);

        Substitution<ImmutableTerm> topSubstitution = toCoalesce.stream()
                .collect(substitutionFactory.toSubstitution(
                        v -> termFactory.getImmutableFunctionalTerm(
                                functionSymbolFactory.getRequiredSPARQLFunctionSymbol(SPARQL.COALESCE, 2),
                                leftRenamingSubstitution.get(v),
                                rightRenamingSubstitution.get(v))));

        ImmutableSet<Variable> projectedVariables = Sets.union(
                Sets.difference(
                        Sets.union(leftTranslation.iqTree.getVariables(), rightTranslation.iqTree.getVariables()),
                        toCoalesce),
                topSubstitution.getDomain()).immutableCopy();

        InjectiveSubstitution<Variable> leftNonProjVarsRenaming = getNonProjVarsRenaming(leftTranslation.iqTree, rightTranslation.iqTree);
        InjectiveSubstitution<Variable> rightNonProjVarsRenaming = getNonProjVarsRenaming(rightTranslation.iqTree, leftTranslation.iqTree);

        IQTree leftTree = applyInDepthRenaming(leftNonProjVarsRenaming,
                applyDownPropagationWithoutOptimization(leftTranslation.iqTree, leftRenamingSubstitution));
        IQTree rightTree = applyInDepthRenaming(rightNonProjVarsRenaming,
                applyDownPropagationWithoutOptimization(rightTranslation.iqTree, rightRenamingSubstitution));

        Stream<ImmutableExpression> coalescingStream = toCoalesce.stream()
                .map(v -> generateCompatibleExpression(v, leftRenamingSubstitution, rightRenamingSubstitution));

        Sets.SetView<Variable> nullableVariables;
        IQTree joinTree;
        if (join instanceof LeftJoin) {
            Set<Variable> variables = Sets.union(leftTranslation.iqTree.getVariables(), rightTranslation.iqTree.getVariables());

            LeftJoin leftJoin = (LeftJoin) join;
            Optional<ImmutableExpression> joinCondition;
            if (leftJoin.hasCondition()) {
                ValueExpressionResult<ImmutableExpression> filterResult = getFilterExpression(leftJoin.getCondition(), variables);
                ImmutableExpression filterExpression =  topSubstitution.apply(filterResult.getResult());

                if (!filterResult.getExistsMap().isEmpty()) {
                    rightTree = translateExists(filterResult.getExistsMap(), new TranslationResult(rightTree, rightTranslation.nullableVariables));
                }
                joinCondition = termFactory.getConjunction(Optional.of(filterExpression), coalescingStream);
            }
            else {
                joinCondition = termFactory.getConjunction(coalescingStream);
            }

            joinTree = iqFactory.createBinaryNonCommutativeIQTree(iqFactory.createLeftJoinNode(joinCondition), leftTree, rightTree);

            nullableVariables = Sets.union(nullableVariablesLeftOrRight, Sets.difference(rightTranslation.iqTree.getVariables(), sharedVariables));
        }
        else if (join instanceof Join) {
            Optional<ImmutableExpression> joinCondition = termFactory.getConjunction(Optional.empty(), coalescingStream);

            joinTree = iqTreeTools.createInnerJoinTree(joinCondition, ImmutableList.of(leftTree, rightTree));

            nullableVariables = Sets.difference(nullableVariablesLeftOrRight, sharedVariables);
        }
        else {
            throw new Sparql2IqConversionException("A left or inner join is expected");
        }

        var optionalConstructionNode = iqTreeTools.createOptionalConstructionNode(() -> projectedVariables, topSubstitution);
        IQTree joinQuery = iqTreeTools.unaryIQTreeBuilder()
                .append(optionalConstructionNode)
                .build(joinTree);

        return createTranslationResult(joinQuery, nullableVariables.immutableCopy());
    }

    private ImmutableExpression generateCompatibleExpression(Variable outputVariable,
                                                             InjectiveSubstitution<Variable> leftChildSubstitution,
                                                             InjectiveSubstitution<Variable> rightChildSubstitution) {

        Variable leftVariable = substitutionFactory.apply(leftChildSubstitution, outputVariable);
        Variable rightVariable = substitutionFactory.apply(rightChildSubstitution, outputVariable);

        ImmutableExpression equalityCondition = termFactory.getStrictEquality(leftVariable, rightVariable);
        ImmutableExpression isNullExpression = termFactory.getDisjunction(
                termFactory.getDBIsNull(leftVariable), termFactory.getDBIsNull(rightVariable));

        return termFactory.getDisjunction(equalityCondition, isNullExpression);
    }

    private TranslationResult translate(Projection projection) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException {
        TranslationResult child = translate(projection.getArg());

        ImmutableMap<Variable, Variable> map = projection.getProjectionElemList().getElements().stream()
                .collect(ImmutableCollectors.toMap(
                        pe -> termFactory.getVariable(pe.getName()),
                        pe -> termFactory.getVariable(pe.getProjectionAlias().orElse(pe.getName()))));

        Substitution<Variable> substitution = map.entrySet().stream()
                .collect(substitutionFactory.toSubstitutionSkippingIdentityEntries());

        ImmutableSet<Variable> projectedVars = ImmutableSet.copyOf(map.values());

        if (substitution.isEmpty() && projectedVars.equals(child.iqTree.getVariables())) {
            return child;
        }

        IQTree subQuery = applyDownPropagationWithoutOptimization(child.iqTree, substitution);

        // Substitution for possibly unbound variables
        Substitution<ImmutableTerm> newSubstitution = Sets.difference(projectedVars, subQuery.getVariables()).stream()
                .collect(substitutionFactory.toSubstitution(v -> termFactory.getNullConstant()));

        ConstructionNode projectNode = iqFactory.createConstructionNode(projectedVars, newSubstitution);
        UnaryIQTree constructTree = iqFactory.createUnaryIQTree(projectNode, subQuery);

        ImmutableSet<Variable> nullableVariables = substitutionFactory.apply(substitution, child.nullableVariables);

        IQTree iqTree = applyExternalBindingFilter(constructTree, projectNode.getSubstitution().getDomain());
        return createTranslationResult(iqTree, nullableVariables);
    }

    private TranslationResult translate(Union union) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException {
        TranslationResult leftTranslation = translate(union.getLeftArg());
        TranslationResult rightTranslation = translate(union.getRightArg());
        variableGenerator.registerAdditionalVariables(Sets.union(
                leftTranslation.iqTree.getKnownVariables(), rightTranslation.iqTree.getKnownVariables()));

        ImmutableSet<Variable> leftVariables = leftTranslation.iqTree.getVariables();
        ImmutableSet<Variable> rightVariables = rightTranslation.iqTree.getVariables();

        Sets.SetView<Variable> nullOnLeft = Sets.difference(rightVariables, leftVariables);
        Sets.SetView<Variable> nullOnRight = Sets.difference(leftVariables, rightVariables);

        ImmutableSet<Variable> nullableVariables = Sets.union(
                Sets.union(leftTranslation.nullableVariables, rightTranslation.nullableVariables),
                Sets.union(nullOnLeft, nullOnRight)).immutableCopy();

        ImmutableSet<Variable> rootVariables = Sets.union(leftVariables, rightVariables).immutableCopy();

        ConstructionNode leftCn = iqFactory.createConstructionNode(rootVariables, nullOnLeft.stream()
                .collect(substitutionFactory.toSubstitution(v -> termFactory.getNullConstant())));
        ConstructionNode rightCn = iqFactory.createConstructionNode(rootVariables, nullOnRight.stream()
                .collect(substitutionFactory.toSubstitution(v -> termFactory.getNullConstant())));

        InjectiveSubstitution<Variable> leftNonProjVarsRenaming = getNonProjVarsRenaming(leftTranslation.iqTree, rightTranslation.iqTree);
        InjectiveSubstitution<Variable> rightNonProjVarsRenaming = getNonProjVarsRenaming(rightTranslation.iqTree, leftTranslation.iqTree);

        return createTranslationResult(
                iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(rootVariables),
                        iqFactory.createNaryIQTree(iqFactory.createUnionNode(rootVariables),
                                ImmutableList.of(
                                        iqFactory.createUnaryIQTree(leftCn, applyInDepthRenaming(leftNonProjVarsRenaming, leftTranslation.iqTree)),
                                        iqFactory.createUnaryIQTree(rightCn, applyInDepthRenaming(rightNonProjVarsRenaming, rightTranslation.iqTree))))),
                nullableVariables);
    }

    private TranslationResult translate(StatementPattern pattern) {

        RDF4JValueExprTranslator translator = getValueTranslator(ImmutableSet.of());
        VariableOrGroundTerm subject = translator.translateRDF4JVar(pattern.getSubjectVar(), true);
        VariableOrGroundTerm predicate = translator.translateRDF4JVar(pattern.getPredicateVar(), true);
        VariableOrGroundTerm object = translator.translateRDF4JVar(pattern.getObjectVar(), true);

        IQTree subTree;
        if (pattern.getScope().equals(StatementPattern.Scope.NAMED_CONTEXTS))  {
            VariableOrGroundTerm graph = translator.translateRDF4JVar(pattern.getContextVar(), true);
            subTree = translateQuadPattern(subject, predicate, object, graph);
        }
        else  {
            subTree = translateTriplePattern(subject, predicate, object);
        }

        IQTree iqTree = applyExternalBindingFilter(subTree, subTree.getVariables());
        return createTranslationResult(iqTree, ImmutableSet.of());
    }

    private IQTree translateTriplePattern(VariableOrGroundTerm subject, VariableOrGroundTerm predicate, VariableOrGroundTerm object) {

        if (dataset == null || dataset.getDefaultGraphs().isEmpty() && dataset.getNamedGraphs().isEmpty()) {
            return iqFactory.createIntensionalDataNode(
                    atomFactory.getIntensionalTripleAtom(subject, predicate, object));
        }
        else {
            Set<IRI> defaultGraphs = dataset.getDefaultGraphs();
            int defaultGraphCount = defaultGraphs.size();

            // From SPARQL 1.1 "If there is no FROM clause, but there is one or more FROM NAMED clauses,
            // then the dataset includes an empty graph for the default graph."
            if (defaultGraphCount == 0) {
                return iqFactory.createEmptyNode(
                        Stream.of(subject, predicate, object)
                                .filter(t -> t instanceof Variable)
                                .map(t -> (Variable) t)
                                .collect(ImmutableCollectors.toSet()));
            }
            // NB: INSERT blocks cannot have more than 1 default graph. Important for the rest
            else if (defaultGraphCount == 1) {
                IRIConstant graph = termFactory.getConstantIRI(defaultGraphs.iterator().next().stringValue());
                return iqFactory.createIntensionalDataNode(
                        atomFactory.getIntensionalQuadAtom(subject, predicate, object, graph));
            }
            else {
                Variable graph = termFactory.getVariable("g" + UUID.randomUUID());

                IntensionalDataNode quadNode = iqFactory.createIntensionalDataNode(
                        atomFactory.getIntensionalQuadAtom(subject, predicate, object, graph));

                FilterNode filterNode = getGraphFilter(graph, defaultGraphs);

                ImmutableSet<Variable> projectedVariables = Sets.difference(quadNode.getVariables(), ImmutableSet.of(graph)).immutableCopy();

                // Merges the default trees -> removes duplicates
                return iqFactory.createUnaryIQTree(iqFactory.createDistinctNode(),
                        iqFactory.createUnaryIQTree(
                                iqFactory.createConstructionNode(projectedVariables),
                                iqFactory.createUnaryIQTree(filterNode, quadNode)));
            }
        }
    }

    private IQTree translateQuadPattern(VariableOrGroundTerm subject, VariableOrGroundTerm predicate, VariableOrGroundTerm object, VariableOrGroundTerm graph) {

        IntensionalDataNode dataNode = iqFactory.createIntensionalDataNode(
                atomFactory.getIntensionalQuadAtom(subject, predicate, object, graph));

        return (dataset == null || dataset.getNamedGraphs().isEmpty())
            ? dataNode
            : iqFactory.createUnaryIQTree(getGraphFilter(graph, dataset.getNamedGraphs()), dataNode);
    }

    private FilterNode getGraphFilter(VariableOrGroundTerm graph, Set<IRI> graphIris) {
        ImmutableExpression graphFilter = termFactory.getDisjunction(graphIris.stream()
                        .map(g -> termFactory.getConstantIRI(g.stringValue()))
                        .map(iriConstant -> termFactory.getStrictEquality(graph, iriConstant)))
                .orElseThrow(() -> new MinorOntopInternalBugException("The empty case already handled"));

        return iqFactory.createFilterNode(graphFilter);
    }

    private TranslationResult translate(Extension node) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException {

        TranslationResult childTranslation = translate(node.getArg());

        // Warning: an ExtensionElement might reference a variable appearing in a previous ExtensionElement
        // So we may need to nest them

        // Assumption: every variable used in a definition is itself defined either in the subtree of in a previous ExtensionElem
        ImmutableSet<Variable> childVars = childTranslation.iqTree.getVariables();
        ValueExpressionResult<ImmutableList<Substitution<ImmutableTerm>>> varDefsResult = getVarDefs(node.getElements(), childVars);

        ImmutableList<Substitution<ImmutableTerm>> mergedVarDefs = varDefsResult.getResult();
        if (mergedVarDefs.isEmpty()) {
            return childTranslation;
        }

        TranslationResult result = createTranslationResult(childTranslation.iqTree, childTranslation.nullableVariables);

        for (Substitution<ImmutableTerm> substitution : mergedVarDefs) {

            ImmutableSet<Variable> nullableVariables = result.nullableVariables;
            ImmutableSet<Variable> newNullableVariables = substitution
                    .getPreImage(t -> t.getVariableStream().anyMatch(nullableVariables::contains));

            ConstructionNode constructionNode = iqFactory.createConstructionNode(
                    Sets.union(result.iqTree.getVariables(), substitution.getDomain()).immutableCopy(),
                    substitution);

            IQTree subTree;
            if (!varDefsResult.getExistsMap().isEmpty()) {
                subTree = translateExists(varDefsResult.getExistsMap(), result);
            }
            else {
                subTree = result.iqTree;
            }
            UnaryIQTree tree = iqFactory.createUnaryIQTree(constructionNode, subTree);

            IQTree iqTree = applyExternalBindingFilter(tree, constructionNode.getSubstitution().getDomain());
            result = createTranslationResult(iqTree, Sets.union(nullableVariables, newNullableVariables).immutableCopy());
        }

        return result;
    }

    private ValueExpressionResult<ImmutableList<Substitution<ImmutableTerm>>> getVarDefs(List<ExtensionElem> list,
                                                                                         ImmutableSet<Variable> childVars) {
        List<VarDef> result = new ArrayList<>();
        Set<Variable> allowedVars = new HashSet<>(childVars); // mutable: accumulator
        ImmutableMap.Builder<Variable, Exists> builder = ImmutableMap.builder();

        for (ExtensionElem elem : list) {
            if (!(elem.getExpr() instanceof Var && elem.getName().equals(((Var) elem.getExpr()).getName()))) {
                RDF4JValueExprTranslator.ExtendedTerm term = getValueTranslator(allowedVars).getTerm(elem.getExpr());
                Variable definedVar = termFactory.getVariable(elem.getName());
                allowedVars.add(definedVar);

                result.add(new VarDef(definedVar, term.getTerm()));
                builder.putAll(term.getExistsMap());
            }
        }

        ImmutableList<VarDef> varDefs = result.stream()
                .filter(vd -> !childVars.contains(vd.var))
                .collect(ImmutableCollectors.toList());

        return new ValueExpressionResult<>(mergeVarDefs(varDefs), builder.build());
    }

    private ImmutableList<Substitution<ImmutableTerm>> mergeVarDefs(ImmutableList<VarDef> varDefs)  {
        Deque<Map<Variable, ImmutableTerm>> substitutionMapList = new LinkedList<>();
        substitutionMapList.add(new HashMap<>());

        for (VarDef varDef : varDefs) {
            Map<Variable, ImmutableTerm> last = substitutionMapList.getLast();
            if (varDef.term.getVariableStream().anyMatch(last::containsKey)) { // start off a new substitution
                substitutionMapList.addLast(new HashMap<>());
            }
            substitutionMapList.getLast().put(varDef.var, varDef.term);
        }

        return substitutionMapList.stream()
                .map(m -> m.entrySet().stream().collect(substitutionFactory.toSubstitution()))
                .collect(ImmutableCollectors.toList());
    }

    /** Returns the injective substitution that renames the non-projected variables from the left
     * that are also present in the right operand
     */
    private InjectiveSubstitution<Variable> getNonProjVarsRenaming(IQTree left, IQTree right) {
        return getFreshRenamingSubstitution(
                Sets.intersection(
                        Sets.difference(left.getKnownVariables(), left.getVariables()),
                        right.getKnownVariables()));
    }

    private InjectiveSubstitution<Variable> getRightConflictingProvenanceRenaming(TranslationResult translation, Variable provenanceVariable) {
        return getFreshRenamingSubstitution(Sets.intersection(translation.iqTree.getKnownVariables(), ImmutableSet.of(provenanceVariable)));
    }

    private InjectiveSubstitution<Variable> getFreshRenamingSubstitution(Set<Variable> variables) {
        return variables.stream()
                .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));
    }

    private IQTree applyDownPropagationWithoutOptimization(IQTree tree, Substitution<? extends VariableOrGroundTerm> descendingSubstitution) {
        return iqTreeTools.applyDownPropagationWithoutOptimization(tree, descendingSubstitution, variableGenerator);
    }

    private IQTree applyInDepthRenaming(InjectiveSubstitution<Variable> renaming, IQTree tree) {
        return queryRenamer.applyInDepthRenaming(renaming, tree);
    }

    /**
     * @param expr                 expression
     * @param childVariables       the set of variables that can occur in the expression
     */

    private ValueExpressionResult<ImmutableExpression> getFilterExpression(ValueExpr expr, Set<Variable> childVariables) {
        RDF4JValueExprTranslator.ExtendedTerm extendedTerm = getValueTranslator(childVariables).getTerm(expr);

        ImmutableTerm xsdBooleanTerm = extendedTerm.getTerm().inferType()
                .flatMap(TermTypeInference::getTermType)
                .filter(t -> t instanceof RDFDatatype)
                .map(t -> (RDFDatatype)t)
                .filter(t -> t.isA(XSD.BOOLEAN))
                .isPresent()
                    ? extendedTerm.getTerm()
                    : termFactory.getSPARQLEffectiveBooleanValue(extendedTerm.getTerm());

        return new ValueExpressionResult<>(termFactory.getRDF2DBBooleanFunctionalTerm(xsdBooleanTerm), extendedTerm.getExistsMap());
    }


    private TranslationResult createTranslationResult(IQTree iqTree, ImmutableSet<Variable> nullableVariables)  {
        return new TranslationResult(iqTree, nullableVariables);
    }

    private IQTree applyExternalBindingFilter(IQTree tree, ImmutableSet<Variable> variables) {
        // Most of the time
        if (externalBindings.isEmpty())
            return tree;

        Set<Variable> externallyBoundedVariables = Sets.intersection(variables, externalBindings.keySet());

        var optionalFilter = iqTreeTools.createOptionalFilterNode(termFactory.getConjunction(
                externallyBoundedVariables.stream()
                        .map(v -> termFactory.getStrictEquality(v, externalBindings.get(v)))));

        return iqTreeTools.unaryIQTreeBuilder()
                .append(optionalFilter)
                .build(tree);
    }


    private static class TranslationResult {
        private final IQTree iqTree;
        private final ImmutableSet<Variable> nullableVariables;

        /**
         * Do not call it directly, use createTranslationResult instead
         */
        private TranslationResult(IQTree iqTree, ImmutableSet<Variable> nullableVariables) {
            this.nullableVariables = nullableVariables;
            this.iqTree = iqTree;
        }
    }

    private static class Sparql2IqConversionException extends OntopInternalBugException {

        Sparql2IqConversionException(String s) {
            super(s);
        }
    }

    private static class VarDef {
        private final Variable var;
        private final ImmutableTerm term;

        private VarDef(Variable var, ImmutableTerm term) {
            this.var = var;
            this.term = term;
        }
    }

    private RDF4JValueTranslator getValueTranslator() {
        return new RDF4JValueTranslator(termFactory, rdfFactory, typeFactory);
    }

    private RDF4JValueExprTranslator getValueTranslator(Set<Variable> knownVariables) {
        return new RDF4JValueExprTranslator(knownVariables, externalBindings, treatBNodeAsVariable, termFactory, rdfFactory, typeFactory, functionSymbolFactory, variableGenerator);
    }

    protected static final class ExistsSubtreeVisitor extends AbstractQueryModelVisitor<RuntimeException> {
        private final Set<Variable> variables = new HashSet<>();
        private final TermFactory termFactory;
        private boolean isSupported = true;

        public ExistsSubtreeVisitor(TermFactory termFactory, TupleExpr expr) {
            super();
            this.termFactory = termFactory;
            expr.visit(this);
        }

        @Override
        public void meet(Var node) {
            if (!node.hasValue() && !node.isAnonymous()) {
                variables.add(termFactory.getVariable(node.getName()));
            }
            super.meet(node); // Continue traversal
        }

        @Override
        public void meet(Slice node) {
            isSupported = false;
            super.meet(node);
        }

        @Override
        public void meet(Group node) {
            isSupported = false;
            super.meet(node);
        }

        public ImmutableSet<Variable> getVariables() {
            return ImmutableSet.copyOf(variables);
        }

        public boolean isExistsSubtreeSupported() {
            return isSupported;
        }
    }

    protected static final class ValueExpressionResult<T> {
        private final T result;
        private final ImmutableMap<Variable, Exists> existsMap;

        public ValueExpressionResult(T result, ImmutableMap<Variable, Exists> existsMap) {
            this.result = result;
            this.existsMap = existsMap;
        }

        public T getResult() {
            return result;
        }

        public ImmutableMap<Variable, Exists> getExistsMap() {
            return existsMap;
        }
    }

}
