package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * "Default" implementation for an extensional data node.
 *
 * Most likely (but not necessarily) will be overwritten by native query language specific implementations.
 */
public class ExtensionalDataNodeImpl extends DataNodeImpl<RelationPredicate> implements ExtensionalDataNode {

    private static final String EXTENSIONAL_NODE_STR = "EXTENSIONAL";

    private final RelationPredicate relationPredicate;
    private final ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap;


    // LAZY
    @Nullable
    private VariableNullability variableNullability;
    //LAZY
    @Nullable
    private ImmutableSet<ImmutableSet<Variable>> uniqueConstraints;

    private final CoreUtilsFactory coreUtilsFactory;

    @AssistedInject
    private ExtensionalDataNodeImpl(@Assisted DataAtom<RelationPredicate> atom,
                                    IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory,
                                    CoreUtilsFactory coreUtilsFactory) {
        super(atom, iqTreeTools, iqFactory);
        this.coreUtilsFactory = coreUtilsFactory;
        this.relationPredicate = atom.getPredicate();
        this.argumentMap = extractArgumentMap(atom);
    }

    @AssistedInject
    private ExtensionalDataNodeImpl(@Assisted RelationPredicate relationPredicate,
                                    @Assisted ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap,
                                    IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory,
                                    CoreUtilsFactory coreUtilsFactory,
                                    TermFactory termFactory,
                                    AtomFactory atomFactory) {
        super(convertIntoDataAtom(relationPredicate, argumentMap, termFactory, atomFactory), iqTreeTools, iqFactory);
        this.coreUtilsFactory = coreUtilsFactory;
        this.relationPredicate = relationPredicate;
        this.argumentMap = argumentMap;
    }

    @AssistedInject
    private ExtensionalDataNodeImpl(@Assisted RelationPredicate relationPredicate,
                                    @Assisted ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap,
                                    @Assisted VariableNullability variableNullability,
                                    IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory,
                                    CoreUtilsFactory coreUtilsFactory,
                                    TermFactory termFactory,
                                    AtomFactory atomFactory) {
        super(convertIntoDataAtom(relationPredicate, argumentMap, termFactory, atomFactory), iqTreeTools, iqFactory);
        this.coreUtilsFactory = coreUtilsFactory;
        this.relationPredicate = relationPredicate;
        this.argumentMap = argumentMap;
        this.variableNullability = variableNullability;
    }

    /**
     * TEMPORARY
     */
    private ImmutableMap<Integer, ? extends VariableOrGroundTerm> extractArgumentMap(DataAtom<RelationPredicate> atom) {
        return IntStream.range(0, atom.getArity())
                .boxed()
                .collect(ImmutableCollectors.toMap(
                        i -> i,
                        atom::getTerm));
    }

    /**
     * TEMPORARY
     */
    private static DataAtom<RelationPredicate> convertIntoDataAtom(RelationPredicate predicate,
                                                                   ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap,
                                                                   TermFactory termFactory,
                                                                   AtomFactory atomFactory) {
        ImmutableList<? extends VariableOrGroundTerm> newArguments = IntStream.range(0, predicate.getArity())
                .boxed()
                .map(i -> Optional.ofNullable(argumentMap.get(i))
                        .map(a -> (VariableOrGroundTerm)a)
                        .orElseGet(() -> termFactory.getVariable("v" + UUID.randomUUID().toString())))
                .collect(ImmutableCollectors.toList());

        return atomFactory.getDataAtom(predicate, newArguments);
    }

    @Override
    public RelationPredicate getRelationPredicate() {
        return relationPredicate;
    }

    @Override
    public ImmutableMap<Integer, ? extends VariableOrGroundTerm> getArgumentMap() {
        return argumentMap;
    }


    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ExtensionalDataNode clone() {
        return iqFactory.createExtensionalDataNode(getProjectionAtom());
    }

    @Override
    public ExtensionalDataNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public ExtensionalDataNode newAtom(DataAtom<RelationPredicate> newAtom) {
        return iqFactory.createExtensionalDataNode(newAtom);
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        if (!getVariables().contains(variable))
            throw new IllegalArgumentException("The variable " + variable + " is not projected by " + this);

        DataAtom<RelationPredicate> atom = getProjectionAtom();

        RelationDefinition relation = atom.getPredicate().getRelationDefinition();

        ImmutableList<? extends VariableOrGroundTerm> arguments = atom.getArguments();

        // NB: DB column indexes start at 1.
        return IntStream.range(1, arguments.size() + 1)
                .filter(i -> arguments.get(i - 1).equals(variable))
                .mapToObj(relation::getAttribute)
                .allMatch(Attribute::canNull);
    }

    @Override
    public IQTree acceptTransformer(IQTreeVisitingTransformer transformer) {
        return transformer.transformExtensionalData(this);
    }

    /**
     * Is distinct if it has at least one unique constraint
     */
    @Override
    public boolean isDistinct() {
        return !getProjectionAtom().getPredicate().getRelationDefinition().getUniqueConstraints().isEmpty();
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor) {
        return visitor.visitExtensionalData(this);
    }

    /**
     * Optimized to re-use the variable nullability.
     * Useful the data node has a lot of columns.
     */
    @Override
    public IQTree applyFreshRenaming(InjectiveVar2VarSubstitution freshRenamingSubstitution) {
        ImmutableMap<Integer, ? extends VariableOrGroundTerm> newArgumentMap = freshRenamingSubstitution.applyToArgumentMap(argumentMap);
        return (variableNullability == null)
                ? iqFactory.createExtensionalDataNode(relationPredicate, newArgumentMap)
                : iqFactory.createExtensionalDataNode(relationPredicate, newArgumentMap,
                variableNullability.applyFreshRenaming(freshRenamingSubstitution));
    }

    @Override
    public VariableNullability getVariableNullability() {
        if (variableNullability == null) {
            DataAtom<RelationPredicate> atom = getProjectionAtom();
            RelationDefinition relation = atom.getPredicate().getRelationDefinition();

            ImmutableList<? extends VariableOrGroundTerm> arguments = atom.getArguments();
            ImmutableMultiset<? extends VariableOrGroundTerm> argMultiset = ImmutableMultiset.copyOf(arguments);

            // NB: DB column indexes start at 1.
            ImmutableSet<ImmutableSet<Variable>> nullableGroups = IntStream.range(0, arguments.size())
                    .filter(i -> arguments.get(i) instanceof Variable)
                    .filter(i -> relation.getAttribute(i + 1).canNull())
                    .mapToObj(arguments::get)
                    .map(a -> (Variable) a)
                    // An implicit filter condition makes them non-nullable
                    .filter(a -> argMultiset.count(a) < 2)
                    .map(ImmutableSet::of)
                    .collect(ImmutableCollectors.toSet());

            variableNullability = coreUtilsFactory.createVariableNullability(nullableGroups, getVariables());
        }

        return variableNullability;
    }

    @Override
    public void validate() throws InvalidIntermediateQueryException {
    }

    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints() {
        if (uniqueConstraints == null) {
            ImmutableList<? extends VariableOrGroundTerm> arguments = getProjectionAtom().getArguments();

            uniqueConstraints = getProjectionAtom().getPredicate().getRelationDefinition().getUniqueConstraints().stream()
                    .map(uc -> uc.getAttributes().stream()
                            .map(a ->  arguments.get(a.getIndex() -1))
                            .filter(t -> t instanceof Variable)
                            .map(v -> (Variable)v)
                            .collect(ImmutableCollectors.toSet()))
                    .collect(ImmutableCollectors.toSet());
        }
        return uniqueConstraints;
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof ExtensionalDataNode)
                && ((ExtensionalDataNode) node).getProjectionAtom().equals(this.getProjectionAtom());
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return (queryNode instanceof ExtensionalDataNode)
                && getProjectionAtom().equals(((ExtensionalDataNode) queryNode).getProjectionAtom());
    }


    @Override
    public String toString() {
        return EXTENSIONAL_NODE_STR + " " + getProjectionAtom();
    }
}
