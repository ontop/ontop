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
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
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
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
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

    private final CoreUtilsFactory coreUtilsFactory;
    private final SubstitutionFactory substitutionFactory;
    private final IntermediateQueryFactory iqFactory;
    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final FunctionSymbolFactory functionSymbolFactory;
    private final QueryTransformerFactory queryTransformerFactory;
    private final RDF rdfFactory;
    private final TypeFactory typeFactory;

    private final IQTreeTools iqTreeTools;
    private final IRIConstant subClassOfConstant;

    public RDF4JTupleExprTranslator(ImmutableMap<Variable, GroundTerm> externalBindings,
                                    @Nullable Dataset dataset,
                                    boolean treatBNodeAsVariable,
                                    CoreSingletons coreSingletons,
                                    RDF rdfFactory) {
        this.externalBindings = externalBindings;
        this.dataset = dataset;
        this.treatBNodeAsVariable = treatBNodeAsVariable;
        this.coreUtilsFactory = coreSingletons.getCoreUtilsFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.iqFactory = coreSingletons.getIQFactory();
        this.atomFactory = coreSingletons.getAtomFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.functionSymbolFactory = coreSingletons.getFunctionSymbolFactory();
        this.queryTransformerFactory = coreSingletons.getQueryTransformerFactory();
        this.typeFactory = coreSingletons.getTypeFactory();
        this.rdfFactory = rdfFactory;
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.subClassOfConstant = termFactory.getConstantIRI(RDFS.SUBCLASSOF);
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

    private static Set<Variable> getSharedVariables(TranslationResult left, TranslationResult right) {
        return Sets.intersection(left.iqTree.getVariables(), right.iqTree.getVariables());
    }

    private VariableGenerator getVariableGenerator(TranslationResult left, TranslationResult right) {
        return coreUtilsFactory.createVariableGenerator(Sets.union(left.iqTree.getKnownVariables(), right.iqTree.getKnownVariables()));
    }

    private TranslationResult translate(Difference diff) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException {

        TranslationResult leftTranslation = translate(diff.getLeftArg());
        TranslationResult rightTranslation = translate(diff.getRightArg());

        Set<Variable> sharedVariables = getSharedVariables(rightTranslation, leftTranslation);

        if (sharedVariables.isEmpty()) {
            return leftTranslation;
        }

        return translateMinusOperation(leftTranslation, rightTranslation, sharedVariables);
    }

    private IQTree applyInDepthRenaming(IQTree tree, InjectiveSubstitution<Variable> renaming) {
        return queryTransformerFactory.createRenamer(renaming).transform(tree);
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
        ImmutableList<Substitution<ImmutableTerm>> mergedVarDefs =
                getGroupVarDefs(group.getGroupElements(), child.iqTree.getVariables());

        if (mergedVarDefs.size() > 1) {
            throw new Sparql2IqConversionException("Unexpected parsed SPARQL query: nested complex projections appear " +
                    "within an RDF4J Group node: " + group);
        }
        ImmutableSet<Variable> childVariables = child.iqTree.getVariables();

        AggregationNode an = iqFactory.createAggregationNode(
                group.getGroupBindingNames().stream()
                        .map(termFactory::getVariable)
                        .filter(childVariables::contains)
                        .collect(ImmutableCollectors.toSet()),
                mergedVarDefs.get(0).transform(t -> (ImmutableFunctionalTerm)t)); // only one substitution guaranteed by the if

        UnaryIQTree aggregationTree = iqFactory.createUnaryIQTree(an, child.iqTree);

        ImmutableSet<Variable> nullableVariables = Sets.union(
                        Sets.intersection(an.getGroupingVariables(), child.nullableVariables),
                        an.getSubstitution().getPreImage(t -> t.getFunctionSymbol().isNullable(ImmutableSet.of(0))))
                .immutableCopy();

        IQTree iqTree = applyExternalBindingFilter(aggregationTree, an.getSubstitution().getDomain());
        return createTranslationResult(iqTree, nullableVariables);
    }

    private ImmutableList<Substitution<ImmutableTerm>> getGroupVarDefs(List<GroupElem> list,
                                                                       ImmutableSet<Variable> childVariables) {
        List<VarDef> result = new ArrayList<>();
        Set<Variable> allowedVars = new HashSet<>(childVariables); // mutable: accumulator

        for (GroupElem elem : list) {
            RDF4JValueExprTranslator translator = getValueTranslator(allowedVars);
            ImmutableTerm term = translator.getTerm(elem.getOperator());
            Variable definedVar = termFactory.getVariable(elem.getName());
            allowedVars.add(definedVar);

            result.add(new VarDef(definedVar, term));
        }
        return mergeVarDefs(ImmutableList.copyOf(result));
    }

    private TranslationResult translate(Order order) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException {
        TranslationResult child = translate(order.getArg());
        RDF4JValueExprTranslator translator = getValueTranslator(child.iqTree.getVariables());

        ImmutableList<OrderByNode.OrderComparator> comparators = order.getElements().stream()
                .map(o -> Optional.of(translator.getTerm(o.getExpr()))
                        .filter(t -> t instanceof NonGroundTerm)
                        .map(t -> (NonGroundTerm)t)
                        .map(t -> iqFactory.createOrderComparator(t, o.isAscending())))
                .flatMap(Optional::stream)
                .collect(ImmutableCollectors.toList());

        return comparators.isEmpty()
                ? child
                : createTranslationResult(
                    iqFactory.createUnaryIQTree(iqFactory.createOrderByNode(comparators), child.iqTree),
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
                .map(iqTreeTools::createConstructionNode)
                .map(cn -> iqFactory.createUnaryIQTree(cn, iqFactory.createTrueNode()))
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
        return createTranslationResult(
                iqFactory.createUnaryIQTree(
                        iqFactory.createFilterNode(getFilterExpression(condition, child.iqTree.getVariables())),
                        child.iqTree),
                child.nullableVariables);
    }

    private TranslationResult translateNotExists(Exists exists, TranslationResult leftTranslation) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException {
        TranslationResult rightTranslation = translate(exists.getSubQuery());

        Set<Variable> sharedVariables = getSharedVariables(rightTranslation, leftTranslation);

        if (sharedVariables.isEmpty()) {
            throw new OntopUnsupportedKGQueryException("The NOT EXISTS operator is not supported with no common variables");
        }

        if (sharedVariables.stream().allMatch(v -> v.isNullable(leftTranslation.nullableVariables)
                || v.isNullable(rightTranslation.nullableVariables))) {
            throw new OntopUnsupportedKGQueryException("The NOT EXISTS operator is not supported when there is no non-nullable common variable");
        }

        ImmutableSet<Variable> allVars = new RDF4JVarsCollector(termFactory).collectVariables(exists.getSubQuery());
        if (!Sets.difference(allVars, rightTranslation.iqTree.getKnownVariables()).isEmpty()) {
            throw new OntopUnsupportedKGQueryException("Some of the variables in the NOT EXISTS subquery are unbound");
        }

        return translateMinusOperation(leftTranslation, rightTranslation, sharedVariables);
    }

    private TranslationResult translateMinusOperation(TranslationResult leftTranslation, TranslationResult rightTranslation, Set<Variable> sharedVariables) {
        VariableGenerator vGen = getVariableGenerator(leftTranslation, rightTranslation);

        InjectiveSubstitution<Variable> sharedVarsRenaming = sharedVariables.stream()
                .collect(substitutionFactory.toFreshRenamingSubstitution(vGen));

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

        InjectiveSubstitution<Variable> leftNonProjVarsRenaming = getNonProjVarsRenaming(leftTranslation, rightTranslation, vGen);
        InjectiveSubstitution<Variable> rightNonProjVarsRenaming = getNonProjVarsRenaming(rightTranslation, leftTranslation, vGen);

        return createTranslationResult(
                iqTreeTools.createUnaryIQTree(
                        iqFactory.createConstructionNode(leftTranslation.iqTree.getVariables()),
                        iqFactory.createFilterNode(filter),
                        iqFactory.createBinaryNonCommutativeIQTree(iqFactory.createLeftJoinNode(ljCond),
                                applyInDepthRenaming(leftTranslation.iqTree, leftNonProjVarsRenaming),
                                applyInDepthRenaming(
                                        rightTranslation.iqTree.applyDescendingSubstitutionWithoutOptimizing(sharedVarsRenaming, vGen),
                                        rightNonProjVarsRenaming))),
                leftTranslation.nullableVariables);
    }

    private TranslationResult translateJoinLikeNode(BinaryTupleOperator join) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException {

        TranslationResult leftTranslation = translate(join.getLeftArg());
        TranslationResult rightTranslation = translate(join.getRightArg());

        Set<Variable> nullableVariablesLeftOrRight = Sets.union(leftTranslation.nullableVariables, rightTranslation.nullableVariables);

        Set<Variable> sharedVariables = getSharedVariables(leftTranslation, rightTranslation);

        Set<Variable> toCoalesce = Sets.intersection(sharedVariables, nullableVariablesLeftOrRight);

        VariableGenerator variableGenerator = getVariableGenerator(leftTranslation, rightTranslation);

        // May update the variable generator!!
        InjectiveSubstitution<Variable> leftRenamingSubstitution = toCoalesce.stream()
                .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));

        InjectiveSubstitution<Variable> rightRenamingSubstitution = toCoalesce.stream()
                .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));

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

        InjectiveSubstitution<Variable> leftNonProjVarsRenaming = getNonProjVarsRenaming(leftTranslation, rightTranslation, variableGenerator);
        InjectiveSubstitution<Variable> rightNonProjVarsRenaming = getNonProjVarsRenaming(rightTranslation, leftTranslation, variableGenerator);

        IQTree leftTree = applyInDepthRenaming(
                leftTranslation.iqTree.applyDescendingSubstitutionWithoutOptimizing(leftRenamingSubstitution, variableGenerator),
                leftNonProjVarsRenaming);
        IQTree rightTree = applyInDepthRenaming(
                rightTranslation.iqTree.applyDescendingSubstitutionWithoutOptimizing(rightRenamingSubstitution, variableGenerator),
                rightNonProjVarsRenaming);

        Stream<ImmutableExpression> coalescingStream = toCoalesce.stream()
                .map(v -> generateCompatibleExpression(v, leftRenamingSubstitution, rightRenamingSubstitution));

        ImmutableSet<Variable> nullableVariables;
        IQTree joinTree;
        if (join instanceof LeftJoin) {
            Set<Variable> variables = Sets.union(leftTranslation.iqTree.getVariables(), rightTranslation.iqTree.getVariables());
            Optional<ImmutableExpression> filterExpression = Optional.ofNullable(((LeftJoin) join).getCondition())
                    .map(c -> topSubstitution.apply(getFilterExpression(c, variables)));
            Optional<ImmutableExpression> joinCondition = termFactory.getConjunction(filterExpression, coalescingStream);

            joinTree = iqFactory.createBinaryNonCommutativeIQTree(iqFactory.createLeftJoinNode(joinCondition), leftTree, rightTree);

            nullableVariables = Sets.union(nullableVariablesLeftOrRight, Sets.difference(rightTranslation.iqTree.getVariables(), sharedVariables)).immutableCopy();
        }
        else if (join instanceof Join) {
            Optional<ImmutableExpression> joinCondition = termFactory.getConjunction(Optional.empty(), coalescingStream);

            joinTree = iqTreeTools.createInnerJoinTree(joinCondition, ImmutableList.of(leftTree, rightTree));

            nullableVariables = Sets.difference(nullableVariablesLeftOrRight, sharedVariables).immutableCopy();
        }
        else {
            throw new Sparql2IqConversionException("A left or inner join is expected");
        }

        var optionalConstructionNode = iqTreeTools.createOptionalConstructionNode(() -> projectedVariables, topSubstitution);
        IQTree joinQuery = iqTreeTools.createOptionalUnaryIQTree(optionalConstructionNode, joinTree);

        return createTranslationResult(joinQuery, nullableVariables);
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

        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(
                Sets.union(child.iqTree.getKnownVariables(), projectedVars));

        IQTree subQuery = child.iqTree.applyDescendingSubstitutionWithoutOptimizing(substitution, variableGenerator);

        // Substitution for possibly unbound variables
        Substitution<ImmutableTerm> newSubstitution = Sets.difference(projectedVars, subQuery.getVariables()).stream()
                .collect(substitutionFactory.toSubstitution(v -> termFactory.getNullConstant()));

        UnaryIQTree constructTree = iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(projectedVars, newSubstitution),
                subQuery);

        ImmutableSet<Variable> nullableVariables = substitutionFactory.apply(substitution, child.nullableVariables);

        IQTree iqTree = applyExternalBindingFilter(constructTree, newSubstitution.getDomain());
        return createTranslationResult(iqTree, nullableVariables);
    }

    private TranslationResult translate(Union union) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException {
        TranslationResult leftTranslation = translate(union.getLeftArg());
        TranslationResult rightTranslation = translate(union.getRightArg());

        VariableGenerator variableGenerator = getVariableGenerator(leftTranslation, rightTranslation);

        ImmutableSet<Variable> leftVariables = leftTranslation.iqTree.getVariables();
        ImmutableSet<Variable> rightVariables = rightTranslation.iqTree.getVariables();

        Set<Variable> nullOnLeft = Sets.difference(rightVariables, leftVariables);
        Set<Variable> nullOnRight = Sets.difference(leftVariables, rightVariables);

        ImmutableSet<Variable> nullableVariables = Sets.union(
                Sets.union(leftTranslation.nullableVariables, rightTranslation.nullableVariables),
                Sets.union(nullOnLeft, nullOnRight)).immutableCopy();

        ImmutableSet<Variable> rootVariables = Sets.union(leftVariables, rightVariables).immutableCopy();

        ConstructionNode leftCn = iqFactory.createConstructionNode(rootVariables, nullOnLeft.stream()
                .collect(substitutionFactory.toSubstitution(v -> termFactory.getNullConstant())));
        ConstructionNode rightCn = iqFactory.createConstructionNode(rootVariables, nullOnRight.stream()
                .collect(substitutionFactory.toSubstitution(v -> termFactory.getNullConstant())));

        InjectiveSubstitution<Variable> leftNonProjVarsRenaming = getNonProjVarsRenaming(leftTranslation, rightTranslation, variableGenerator);
        InjectiveSubstitution<Variable> rightNonProjVarsRenaming = getNonProjVarsRenaming(rightTranslation, leftTranslation, variableGenerator);

        return createTranslationResult(
                iqFactory.createUnaryIQTree(
                        iqFactory.createConstructionNode(rootVariables),
                        iqTreeTools.createUnionTree(rootVariables,
                                ImmutableList.of(
                                        iqFactory.createUnaryIQTree(leftCn, applyInDepthRenaming(leftTranslation.iqTree, leftNonProjVarsRenaming)),
                                        iqFactory.createUnaryIQTree(rightCn, applyInDepthRenaming(rightTranslation.iqTree, rightNonProjVarsRenaming))))),
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

                // Merges the default trees -> removes duplicates
                return iqTreeTools.createUnaryIQTree(
                        iqFactory.createDistinctNode(),
                        iqTreeTools.createProjectingConstructionNode(quadNode.getVariables(), ImmutableSet.of(graph)),
                        filterNode,
                        quadNode);
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
        ImmutableList<Substitution<ImmutableTerm>> mergedVarDefs = getVarDefs(node.getElements(), childVars);

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

            UnaryIQTree tree = iqFactory.createUnaryIQTree(constructionNode, result.iqTree);

            IQTree iqTree = applyExternalBindingFilter(tree, constructionNode.getSubstitution().getDomain());
            result = createTranslationResult(iqTree, Sets.union(nullableVariables, newNullableVariables).immutableCopy());
        }

        return result;
    }

    private ImmutableList<Substitution<ImmutableTerm>> getVarDefs(List<ExtensionElem> list,
                                                                  ImmutableSet<Variable> childVars) {
        List<VarDef> result = new ArrayList<>();
        Set<Variable> allowedVars = new HashSet<>(childVars); // mutable: accumulator

        for (ExtensionElem elem : list) {
            if (!(elem.getExpr() instanceof Var && elem.getName().equals(((Var) elem.getExpr()).getName()))) {
                ImmutableTerm term = getValueTranslator(allowedVars).getTerm(elem.getExpr());
                Variable definedVar = termFactory.getVariable(elem.getName());
                allowedVars.add(definedVar);

                result.add(new VarDef(definedVar, term));
            }
        }

        ImmutableList<VarDef> varDefs = result.stream()
                .filter(vd -> !childVars.contains(vd.var))
                .collect(ImmutableCollectors.toList());

        return mergeVarDefs(varDefs);
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
    private InjectiveSubstitution<Variable> getNonProjVarsRenaming(TranslationResult left, TranslationResult right,
                                                                   VariableGenerator variableGenerator) {
        return Sets.intersection(
                        Sets.difference(left.iqTree.getKnownVariables(), left.iqTree.getVariables()),
                        right.iqTree.getKnownVariables()).stream()
                .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));
    }


    /**
     * @param expr                 expression
     * @param childVariables       the set of variables that can occur in the expression
     */

    private ImmutableExpression getFilterExpression(ValueExpr expr, Set<Variable> childVariables) {

        ImmutableTerm term = getValueTranslator(childVariables).getTerm(expr);

        ImmutableTerm xsdBooleanTerm = term.inferType()
                .flatMap(TermTypeInference::getTermType)
                .filter(t -> t instanceof RDFDatatype)
                .map(t -> (RDFDatatype)t)
                .filter(t -> t.isA(XSD.BOOLEAN))
                .isPresent()
                    ? term
                    : termFactory.getSPARQLEffectiveBooleanValue(term);

        return termFactory.getRDF2DBBooleanFunctionalTerm(xsdBooleanTerm);
    }


    private TranslationResult createTranslationResult(IQTree iqTree, ImmutableSet<Variable> nullableVariables)  {
        return new TranslationResult(iqTree, nullableVariables);
    }

    private IQTree applyExternalBindingFilter(IQTree tree, ImmutableSet<Variable> variables) {
        // Most of the time
        if (externalBindings.isEmpty())
            return tree;

        Set<Variable> externallyBoundedVariables = Sets.intersection(variables, externalBindings.keySet());

        Optional<ImmutableExpression> conjunction = termFactory.getConjunction(
                externallyBoundedVariables.stream()
                        .map(v -> termFactory.getStrictEquality(v, externalBindings.get(v))));

        return iqTreeTools.createFilterTree(conjunction, tree);
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
        return new RDF4JValueExprTranslator(knownVariables, externalBindings, treatBNodeAsVariable, termFactory, rdfFactory, typeFactory, functionSymbolFactory);
    }

    private static class RDF4JVarsCollector extends AbstractQueryModelVisitor<RuntimeException> {
        private final Set<Variable> variables = new HashSet<>();
        private final TermFactory termFactory;

        public RDF4JVarsCollector(TermFactory termFactory) {
            super();
            this.termFactory = termFactory;
        }

        @Override
        public void meet(Var node) {
            if (!node.hasValue() && !node.isAnonymous()) {
                variables.add(termFactory.getVariable(node.getName()));
            }
            super.meet(node); // Continue traversal
        }

        public ImmutableSet<Variable> collectVariables(TupleExpr expr) {
            expr.visit(this);
            return ImmutableSet.copyOf(variables);
        }
    }

}
