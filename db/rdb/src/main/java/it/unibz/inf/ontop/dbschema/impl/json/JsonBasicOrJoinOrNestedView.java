package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OntopViewDefinitionImpl;
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
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class JsonBasicOrJoinOrNestedView extends JsonView {

    protected static final Logger LOGGER = LoggerFactory.getLogger(JsonBasicOrJoinOrNestedView.class);


    protected JsonBasicOrJoinOrNestedView(List<String> name, @Nullable UniqueConstraints uniqueConstraints,
                                          @Nullable OtherFunctionalDependencies otherFunctionalDependencies,
                                          @Nullable ForeignKeys foreignKeys, @Nullable NonNullConstraints nonNullConstraints) {
        super(name, uniqueConstraints, otherFunctionalDependencies, foreignKeys, nonNullConstraints);
    }

    @Override
    public OntopViewDefinition createViewDefinition(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException {

        ImmutableMap<NamedRelationDefinition, String> parentDefinitionMap = extractParentDefinitions(dbParameters, parentCacheMetadataLookup);

        Integer maxParentLevel = parentDefinitionMap.keySet().stream()
                .map(r -> (r instanceof OntopViewDefinition)
                        ? ((OntopViewDefinition) r).getLevel()
                        : 0)
                .reduce(0, Math::max, Math::max);

        QuotedIDFactory quotedIDFactory = dbParameters.getQuotedIDFactory();
        RelationID relationId = quotedIDFactory.createRelationID(name.toArray(new String[0]));

        IQ iq = createIQ(relationId, parentDefinitionMap, dbParameters);

        // For added columns the termtype, quoted ID and nullability all need to come from the IQ
        RelationDefinition.AttributeListBuilder attributeBuilder = createAttributeBuilder(iq, dbParameters);

        return new OntopViewDefinitionImpl(
                ImmutableList.of(relationId),
                attributeBuilder,
                iq,
                maxParentLevel + 1,
                dbParameters.getCoreSingletons());
    }

    protected abstract IQ createIQ(RelationID relationId, ImmutableMap<NamedRelationDefinition, String> parentDefinitionMap, DBParameters dbParameters)
            throws MetadataExtractionException;

    @Override
    public void insertIntegrityConstraints(OntopViewDefinition relation,
                                           ImmutableList<NamedRelationDefinition> baseRelations,
                                           MetadataLookup metadataLookupForFK, DBParameters dbParameters) throws MetadataExtractionException {

        QuotedIDFactory idFactory = metadataLookupForFK.getQuotedIDFactory();

        CoreSingletons coreSingletons = dbParameters.getCoreSingletons();

        insertUniqueConstraints(relation, idFactory,
                (uniqueConstraints != null) ? uniqueConstraints.added : ImmutableList.of(),
                baseRelations, coreSingletons);

        insertFunctionalDependencies(relation, idFactory,
                (otherFunctionalDependencies != null) ? otherFunctionalDependencies.added : ImmutableList.of(),
                baseRelations);

        insertForeignKeys(relation, metadataLookupForFK,
                (foreignKeys != null) ? foreignKeys.added : ImmutableList.of(),
                baseRelations);
    }

    abstract protected void insertFunctionalDependencies(NamedRelationDefinition relation,
                                                         QuotedIDFactory idFactory,
                                                         List<AddFunctionalDependency> addFunctionalDependencies,
                                                         ImmutableList<NamedRelationDefinition> baseRelations)
            throws MetadataExtractionException;

    abstract protected ImmutableMap<NamedRelationDefinition, String> extractParentDefinitions(DBParameters dbParameters,
                                                                                              MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException;

    protected ImmutableTable<NamedRelationDefinition, Integer, Variable> createParentArgumentTable(ImmutableSet<Variable> addedVariables,
                                                                                                   ImmutableMap<NamedRelationDefinition, String> parentDefinitionMap,
                                                                                                   CoreUtilsFactory coreUtilsFactory) {
        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(addedVariables);

        // NB: the non-necessary variables will be pruned out by normalizing the IQ
        return parentDefinitionMap.entrySet().stream()
                .flatMap(e -> {
                    ImmutableList<Attribute> parentAttributes = e.getKey().getAttributes();
                    return IntStream.range(0, parentAttributes.size())
                            .mapToObj(i -> Tables.immutableCell(e.getKey(), i, variableGenerator.generateNewVariable(
                                    e.getValue() + parentAttributes.get(i).getID().getName())));
                })
                .collect(ImmutableCollectors.toTable());
    }

    protected String normalizeAttributeName(String attributeName, QuotedIDFactory quotedIdFactory) {
        return quotedIdFactory.createAttributeID(attributeName).getName();
    }

    protected AtomPredicate createTemporaryPredicate(RelationID relationId, int arity, CoreSingletons coreSingletons) {
        DBTermType dbRootType = coreSingletons.getTypeFactory().getDBTypeFactory().getAbstractRootDBType();

        return new TemporaryViewPredicate(
                relationId.getSQLRendering(),
                // No precise base DB type for the temporary predicate
                IntStream.range(0, arity)
                        .mapToObj(i -> dbRootType).collect(ImmutableCollectors.toList()));
    }

    private void insertUniqueConstraints(OntopViewDefinition relation, QuotedIDFactory idFactory,
                                         List<AddUniqueConstraints> addUniqueConstraints,
                                         ImmutableList<NamedRelationDefinition> baseRelations,
                                         CoreSingletons coreSingletons)
            throws MetadataExtractionException {


        List<AddUniqueConstraints> list = extractUniqueConstraints(relation, addUniqueConstraints, baseRelations, idFactory,
                coreSingletons);

        for (AddUniqueConstraints addUC : list) {
            if (addUC.isPrimaryKey != null && addUC.isPrimaryKey) LOGGER.warn("Primary key set in the view file for " + addUC.name);

            FunctionalDependency.Builder builder = UniqueConstraint.builder(relation, addUC.name);

            JsonMetadata.deserializeAttributeList(idFactory, addUC.determinants, builder::addDeterminant);
            builder.build();
        }
    }

    private List<AddUniqueConstraints> extractUniqueConstraints(OntopViewDefinition relation, List<AddUniqueConstraints> addUniqueConstraints,
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
    protected ImmutableList<AddUniqueConstraints> inferInheritedUniqueConstraints(OntopViewDefinition relation,
                                                                                  ImmutableList<NamedRelationDefinition> baseRelations,
                                                                                  ImmutableList<QuotedID> addedConstraintsColumns,
                                                                                  QuotedIDFactory idFactory,
                                                                                  CoreSingletons coreSingletons) {
        IQ relationIQ = relation.getIQ();

        NotYetTypedEqualityTransformer eqTransformer = coreSingletons.getNotYetTypedEqualityTransformer();
        IQTree tree = eqTransformer.transform(relationIQ.getTree())
                .normalizeForOptimization(relationIQ.getVariableGenerator());

        ImmutableSet<ImmutableSet<Variable>> variableUniqueConstraints = tree.inferUniqueConstraints();

        ImmutableList<Attribute> attributes = relation.getAttributes();
        DistinctVariableOnlyDataAtom projectedAtom = relationIQ.getProjectionAtom();

        ImmutableMap<Variable, QuotedID> variableIds = IntStream.range(0, attributes.size())
                .boxed()
                .collect(ImmutableCollectors.toMap(
                        projectedAtom::getTerm,
                        i -> attributes.get(i).getID()
                ));

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

    protected void addFunctionalDependency(NamedRelationDefinition viewDefinition,
                                           QuotedIDFactory idFactory,
                                           FunctionalDependencyConstruct fdConstruct) throws MetadataExtractionException {

        FunctionalDependency.Builder builder = FunctionalDependency.defaultBuilder(viewDefinition);

        try {
            for (QuotedID determinant : fdConstruct.getDeterminants())
                builder.addDeterminant(determinant);

            for (QuotedID dependent : fdConstruct.getDependents())
                builder.addDependent(dependent);
        }
        catch (AttributeNotFoundException e) {
            throw new MetadataExtractionException(String.format(
                    "Cannot find attribute %s for Functional Dependency %s", e.getAttributeID(), fdConstruct));
        }
        builder.build();
    }

    protected void insertForeignKeys(OntopViewDefinition relation, MetadataLookup lookup,
                                     List<AddForeignKey> addForeignKeys,
                                     ImmutableList<NamedRelationDefinition> baseRelations)
            throws MetadataExtractionException {

        List<AddForeignKey> list = extractForeignKeys(relation, addForeignKeys, baseRelations);

        for (AddForeignKey fk : list) {
            insertForeignKey(relation, lookup, fk);
        }
    }

    private ImmutableList<AddForeignKey> extractForeignKeys(OntopViewDefinition relation, List<AddForeignKey> addForeignKeys,
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
    protected Stream<AddForeignKey> inferForeignKeys(OntopViewDefinition relation,
                                                     ImmutableList<NamedRelationDefinition> baseRelations) {
        return baseRelations.stream()
                .flatMap(p -> inferForeignKeysFromParent(relation, p));
    }

    protected Stream<AddForeignKey> inferForeignKeysFromParent(OntopViewDefinition relation,
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

        RelationID targetRelationId = JsonMetadata.deserializeRelationID(lookup.getQuotedIDFactory(), addForeignKey.to.relation);
        NamedRelationDefinition targetRelation;
        try {
            targetRelation = lookup.getRelation(targetRelationId);
        }
        // If the target relation has not
        catch (MetadataExtractionException e) {
            LOGGER.info("Cannot find relation {} for FK {}", targetRelationId, addForeignKey.name);
            return ;
        }

        ForeignKeyConstraint.Builder builder = ForeignKeyConstraint.builder(addForeignKey.name, relation, targetRelation);

        int columnCount = addForeignKey.to.columns.size();
        if (addForeignKey.from.size() != columnCount)
            throw new MetadataExtractionException("Not the same number of from and to columns in FK definition");

        try {
            for (int i=0; i < columnCount; i++ ) {
                builder.add(
                        lookup.getQuotedIDFactory().createAttributeID(addForeignKey.from.get(i)),
                        lookup.getQuotedIDFactory().createAttributeID(addForeignKey.to.columns.get(i)));
            }
        }
        catch (AttributeNotFoundException e) {
            throw new MetadataExtractionException(e);
        }

        builder.build();
    }

    /**
     * Parent attributes are expected to all come from the same parent
     */
    protected ImmutableList<ImmutableList<Attribute>> getDerivedFromParentAttributes(
            OntopViewDefinition ontopViewDefinition, ImmutableList<Attribute> parentAttributes) {
        IQ viewIQ = ontopViewDefinition.getIQ();

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
                        .map(i -> ontopViewDefinition.getAttribute(i + 1))
                        .collect(ImmutableCollectors.toList()));
    }

    protected static class Columns extends JsonOpenObject {
        @Nonnull
        public final List<AddColumns> added;
        @Nonnull
        public final List<String> hidden;

        @JsonCreator
        public Columns(@JsonProperty("added") List<AddColumns> added,
                       @JsonProperty("hidden") List<String> hidden) {
            this.added = added;
            this.hidden = hidden;
        }
    }

    protected static class AddColumns extends JsonOpenObject {
        @Nonnull
        public final String name;
        @Nonnull
        public final String expression;


        @JsonCreator
        public AddColumns(@JsonProperty("name") String name,
                          @JsonProperty("expression") String expression) {
            this.name = name;
            this.expression = expression;
        }
    }
}
