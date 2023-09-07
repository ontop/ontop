package it.unibz.inf.ontop.dbschema.impl.json;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.type.NotYetTypedEqualityTransformer;
import it.unibz.inf.ontop.iq.visit.impl.RelationExtractor;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class JsonBasicOrJoinOrNestedLens extends JsonLens {

    protected static final Logger LOGGER = LoggerFactory.getLogger(JsonBasicOrJoinOrNestedLens.class);


    protected JsonBasicOrJoinOrNestedLens(List<String> name, @Nullable UniqueConstraints uniqueConstraints,
                                          @Nullable OtherFunctionalDependencies otherFunctionalDependencies,
                                          @Nullable ForeignKeys foreignKeys, @Nullable NonNullConstraints nonNullConstraints,
                                          @Nullable IRISafeConstraints iriSafeConstraints) {
        super(name, uniqueConstraints, otherFunctionalDependencies, foreignKeys, nonNullConstraints, iriSafeConstraints);
    }

    protected String normalizeAttributeName(String attributeName, QuotedIDFactory quotedIdFactory) {
        return quotedIdFactory.createAttributeID(attributeName).getName();
    }

    protected AtomPredicate createTemporaryPredicate(RelationID relationId, int arity, CoreSingletons coreSingletons) {
        DBTermType dbRootType = coreSingletons.getTypeFactory().getDBTypeFactory().getAbstractRootDBType();

        return new TemporaryLensPredicate(
                relationId.getSQLRendering(),
                // No precise base DB type for the temporary predicate
                IntStream.range(0, arity)
                        .mapToObj(i -> dbRootType).collect(ImmutableCollectors.toList()));
    }

    protected void insertUniqueConstraints(Lens relation, QuotedIDFactory idFactory,
                                           List<AddUniqueConstraints> addUniqueConstraints,
                                           ImmutableList<NamedRelationDefinition> baseRelations,
                                           CoreSingletons coreSingletons)
            throws MetadataExtractionException {


        List<AddUniqueConstraints> list = extractUniqueConstraints(relation, addUniqueConstraints, baseRelations, idFactory,
                coreSingletons);

        for (AddUniqueConstraints addUC : list) {
            if (addUC.isPrimaryKey != null && addUC.isPrimaryKey)
                LOGGER.warn("Primary key set in the view file for " + addUC.name);

            FunctionalDependency.Builder builder = UniqueConstraint.builder(relation, addUC.name);
            JsonMetadata.deserializeAttributeList(idFactory, addUC.determinants, builder::addDeterminant);
            builder.build();
        }
    }

    private List<AddUniqueConstraints> extractUniqueConstraints(Lens relation, List<AddUniqueConstraints> addUniqueConstraints,
                                                                ImmutableList<NamedRelationDefinition> baseRelations,
                                                                QuotedIDFactory idFactory, CoreSingletons coreSingletons) {

        // List of constraints added
        ImmutableList<QuotedID> addedConstraintsColumns = (uniqueConstraints == null)
                ? ImmutableList.of()
                : uniqueConstraints.added.stream()
                .map(a -> a.determinants)
                .flatMap(Collection::stream)
                .map(idFactory::createAttributeID)
                .collect(ImmutableCollectors.toList());

        // Filter inherited constraints
        ImmutableList<AddUniqueConstraints> inheritedConstraints = inferInheritedUniqueConstraints(relation, baseRelations,
                // TODO: refactor this parameter (should preserve composite UCs)
                addedConstraintsColumns, idFactory, coreSingletons);

        // Throw a warning if duplicate unique constraints are added
        if (!(addUniqueConstraints.stream())
                .allMatch(new HashSet<>()::add))
            LOGGER.warn("Duplicate unique constraints found in the viewfile");


        // Return full list of added and inherited constraints, remove duplicates based on constraint attribute
        return Stream.concat(addUniqueConstraints.stream(), inheritedConstraints.stream())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Inferred from the tree
     */
    protected ImmutableList<AddUniqueConstraints> inferInheritedUniqueConstraints(Lens relation,
                                                                                  ImmutableList<NamedRelationDefinition> baseRelations,
                                                                                  ImmutableList<QuotedID> addedConstraintsColumns,
                                                                                  QuotedIDFactory idFactory,
                                                                                  CoreSingletons coreSingletons) {
        IQ relationIQ = relation.getIQ();

        NotYetTypedEqualityTransformer eqTransformer = coreSingletons.getNotYetTypedEqualityTransformer();
        IQTree tree = eqTransformer.transform(relationIQ.getTree())
                .normalizeForOptimization(relationIQ.getVariableGenerator());

        ImmutableSet<ImmutableSet<Variable>> variableUniqueConstraints = tree.inferUniqueConstraints();

        DistinctVariableOnlyDataAtom projectedAtom = relationIQ.getProjectionAtom();

        ImmutableMap<Variable, QuotedID> variableIds = relation.getAttributes().stream()
                .collect(ImmutableCollectors.toMap(
                        a -> projectedAtom.getTerm(a.getIndex() - 1),
                        Attribute::getID));

        return variableUniqueConstraints.stream()
                .map(vs -> new AddUniqueConstraints(
                        UUID.randomUUID().toString(),
                        vs.stream()
                                .map(v -> Optional.ofNullable(variableIds.get(v))
                                        .orElseThrow(() -> new MinorOntopInternalBugException(
                                                "The variables of the unique constraints should be projected")))
                                .map(QuotedID::getSQLRendering)
                                .collect(ImmutableCollectors.toList()),
                        // PK by default false
                        false
                ))
                .collect(ImmutableCollectors.toList());
    }

    /**
     * Infer functional dependencies from the parent
     *
     * TODO: for joins, we could convert the non-inherited unique constraints into general functional dependencies.
     */
    protected void insertFunctionalDependencies(NamedRelationDefinition relation,
                                                QuotedIDFactory idFactory,
                                                ImmutableSet<QuotedID> hiddenColumns,
                                                ImmutableSet<QuotedID> addedColumns,
                                                List<AddFunctionalDependency> addFunctionalDependencies,
                                                List<FunctionalDependencyConstruct> inferredFunctionalDependencies,
                                                ImmutableList<NamedRelationDefinition> baseRelations,
                                                CoreSingletons coreSingletons)
            throws MetadataExtractionException {

        Stream<FunctionalDependencyConstruct> inheritedFDConstructs = baseRelations.stream()
                .map(RelationDefinition::getOtherFunctionalDependencies)
                .flatMap(Collection::stream)
                .filter(f -> canFDBeInherited(f, hiddenColumns, addedColumns))
                .map(f -> new FunctionalDependencyConstruct(
                        f.getDeterminants().stream()
                                .map(Attribute::getID)
                                .collect(ImmutableCollectors.toSet()),
                        f.getDependents().stream()
                                .map(Attribute::getID)
                                .flatMap(d -> extractNewDependents(d, addedColumns, hiddenColumns))
                                .collect(ImmutableCollectors.toSet())));

        Stream<FunctionalDependencyConstruct> declaredFdDependencies = addFunctionalDependencies.stream()
                .map(jsonFD -> new FunctionalDependencyConstruct(
                        jsonFD.determinants.stream()
                                .map(idFactory::createAttributeID)
                                .collect(ImmutableCollectors.toSet()),
                        jsonFD.dependents.stream()
                                .map(idFactory::createAttributeID)
                                .collect(ImmutableCollectors.toSet())));

        ImmutableMultimap<ImmutableSet<QuotedID>, FunctionalDependencyConstruct> fdMultimap = Stream.concat(
                    Stream.concat(declaredFdDependencies, inheritedFDConstructs),
                    inferredFunctionalDependencies.stream())
                .collect(ImmutableCollectors.toMultimap(
                        FunctionalDependencyConstruct::getDeterminants,
                        fd -> fd));

        ImmutableSet<FunctionalDependencyConstruct> fdConstructs = fdMultimap.asMap().values().stream()
                .map(fds -> fds.stream()
                        .reduce((f1, f2) -> f1.merge(f2)
                                .orElseThrow(() -> new MinorOntopInternalBugException(
                                        "Should be mergeable as they are having the same determinants")))
                        // Guaranteed by the multimap structure
                        .get())
                .collect(ImmutableCollectors.toSet());

        // Insert the FDs with transitive closure
        try {
            insertTransitiveFunctionalDependencies(fdConstructs, relation, coreSingletons);
        } catch (AttributeNotFoundException e) {
            throw new MetadataExtractionException(String.format(
                    "Cannot find attribute %s for Functional Dependency.", e.getAttributeID()));
        }
    }


    /**
     * At the moment, only consider mirror attribute (same as in the parent)
     *
     * TODO: enrich the stream so as to include all derived attributes id
     */
    private Stream<QuotedID> extractNewDependents(QuotedID parentDependentId,
                                                  ImmutableSet<QuotedID> addedColumns,
                                                  ImmutableSet<QuotedID> hiddenColumns) {
        return isInherited(parentDependentId, addedColumns, hiddenColumns)
                ? Stream.of(parentDependentId)
                : Stream.empty();
    }

    /**
     * The selecting criteria could be relaxed in the future
     */
    private boolean canFDBeInherited(FunctionalDependency fd, ImmutableSet<QuotedID> hiddenColumns,
                                     ImmutableSet<QuotedID> addedColumns) {
        return fd.getDeterminants().stream()
                .map(Attribute::getID)
                .allMatch(d -> isInherited(d, addedColumns, hiddenColumns));
    }

    private boolean isInherited(QuotedID id, ImmutableSet<QuotedID> addedColumns, ImmutableSet<QuotedID> hiddenColumns) {
        return !addedColumns.contains(id) && !hiddenColumns.contains(id);
    }

    protected void insertForeignKeys(Lens relation, MetadataLookup lookup,
                                     List<AddForeignKey> addForeignKeys,
                                     ImmutableList<NamedRelationDefinition> baseRelations)
            throws MetadataExtractionException {

        List<AddForeignKey> list = extractForeignKeys(relation, addForeignKeys, baseRelations);

        for (AddForeignKey fk : list) {
            insertForeignKey(relation, lookup, fk);
        }
    }

    private ImmutableList<AddForeignKey> extractForeignKeys(Lens relation, List<AddForeignKey> addForeignKeys,
                                                            ImmutableList<NamedRelationDefinition> baseRelations) {
        return Stream.concat(
                        addForeignKeys.stream(),
                        inferForeignKeys(relation, baseRelations))
                .distinct()
                .collect(ImmutableCollectors.toList());
    }

    /**
     * TODO: add FKs towards the base relations
     */
    protected Stream<AddForeignKey> inferForeignKeys(Lens relation,
                                                     ImmutableList<NamedRelationDefinition> baseRelations) {
        return baseRelations.stream()
                .flatMap(p -> inferForeignKeysFromParent(relation, p));
    }

    protected Stream<AddForeignKey> inferForeignKeysFromParent(Lens relation,
                                                               NamedRelationDefinition baseRelation) {
        return baseRelation.getForeignKeys().stream()
                .flatMap(fk -> getDerivedFromParentAttributes(
                        relation,
                        fk.getComponents().stream()
                                .map(ForeignKeyConstraint.Component::getAttribute)
                                .collect(ImmutableCollectors.toList())).stream()
                        .map(as -> new AddForeignKey(
                                UUID.randomUUID().toString(),
                                JsonMetadata.serializeAttributeList(as.stream()),
                                new ForeignKeyPart(
                                        JsonMetadata.serializeRelationID(fk.getReferencedRelation().getID()),
                                        JsonMetadata.serializeAttributeList(
                                                fk.getComponents().stream()
                                                        .map(ForeignKeyConstraint.Component::getReferencedAttribute))))));

    }

    protected void insertForeignKey(NamedRelationDefinition relation, MetadataLookup lookup, AddForeignKey addForeignKey) throws MetadataExtractionException {

        QuotedIDFactory idFactory = lookup.getQuotedIDFactory();

        RelationID targetRelationId = JsonMetadata.deserializeRelationID(idFactory, addForeignKey.to.relation);
        NamedRelationDefinition targetRelation;
        try {
            targetRelation = lookup.getRelation(targetRelationId);
        }
        catch (MetadataExtractionException e) {
            LOGGER.info("Cannot find relation {} for FK {}", targetRelationId, addForeignKey.name);
            return;
        }


        int columnCount = addForeignKey.to.columns.size();
        if (addForeignKey.from.size() != columnCount)
            throw new MetadataExtractionException("Not the same number of from and to columns in FK definition");

        try {
            ForeignKeyConstraint.Builder builder = ForeignKeyConstraint.builder(addForeignKey.name, relation, targetRelation);
            for (int i = 0; i < columnCount; i++) {
                builder.add(
                        idFactory.createAttributeID(addForeignKey.from.get(i)),
                        idFactory.createAttributeID(addForeignKey.to.columns.get(i)));
            }

            builder.build();
        }
        catch (AttributeNotFoundException e) {
            throw new MetadataExtractionException(e);
        }
    }

    /**
     * Parent attributes are expected to all come from the same parent
     */
    protected ImmutableList<ImmutableList<Attribute>> getDerivedFromParentAttributes(
            Lens lens, ImmutableList<Attribute> parentAttributes) {
        IQ viewIQ = lens.getIQ();

        ImmutableList<RelationDefinition> parentRelations = parentAttributes.stream()
                .map(Attribute::getRelation)
                .distinct()
                .collect(ImmutableCollectors.toList());

        RelationDefinition parentRelation;
        switch (parentRelations.size()) {
            case 0:
                return ImmutableList.of();
            case 1:
                parentRelation = parentRelations.get(0);
                break;
            default:
                throw new MinorOntopInternalBugException("Was expecting all the attributes to come from the same parent");
        }

        Optional<ExtensionalDataNode> optionalParentNode = viewIQ.getTree()
                .acceptVisitor(new RelationExtractor())
                .filter(n -> n.getRelationDefinition().equals(parentRelation))
                .findAny();

        if (!optionalParentNode.isPresent())
            // TODO: log a warning
            return ImmutableList.of();

        ImmutableMap<Integer, ? extends VariableOrGroundTerm> parentNodeArgumentMap = optionalParentNode.get().getArgumentMap();

        ImmutableList.Builder<Variable> parentVariableBuilder = ImmutableList.builder();
        for (Attribute parentAttribute : parentAttributes) {
            ImmutableTerm argument = parentNodeArgumentMap.get(parentAttribute.getIndex() - 1);
            if (!(argument instanceof Variable))
                // Unused or filtering column (the latter should not happen)
                return ImmutableList.of();
            parentVariableBuilder.add((Variable) argument);
        }

        ImmutableList<Variable> parentVariables = parentVariableBuilder.build();
        ImmutableList<Variable> projectedVariables = viewIQ.getProjectionAtom().getArguments();

        ImmutableList<Integer> parentVariableIndexes = parentVariables.stream()
                .map(projectedVariables::indexOf)
                .collect(ImmutableCollectors.toList());

        if (parentVariableIndexes.stream().anyMatch(i -> i < 0))
            // Non-projected parent variable
            return ImmutableList.of();

        return ImmutableList.of(
                parentVariableIndexes.stream()
                        .map(i -> lens.getAttribute(i + 1))
                        .collect(ImmutableCollectors.toList()));
    }
}
