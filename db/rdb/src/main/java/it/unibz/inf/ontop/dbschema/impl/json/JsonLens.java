package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.AbstractRelationDefinition;
import it.unibz.inf.ontop.dbschema.impl.RawQuotedIDFactory;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.model.atom.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRISafenessDeclarationFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import it.unibz.inf.ontop.utils.impl.VariableGeneratorImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

@JsonDeserialize(using = JsonLens.JSONLensDeserializer.class)
public abstract class JsonLens extends JsonOpenObject {
    @Nonnull
    public final List<String> name;

    @Nullable
    public final UniqueConstraints uniqueConstraints;

    @Nullable
    public final OtherFunctionalDependencies otherFunctionalDependencies;

    @Nullable
    public final ForeignKeys foreignKeys;

    @Nullable
    public final NonNullConstraints nonNullConstraints;

    @Nullable
    public final IRISafeConstraints iriSafeConstraints;

    public JsonLens(List<String> name, @Nullable UniqueConstraints uniqueConstraints,
                    @Nullable OtherFunctionalDependencies otherFunctionalDependencies, @Nullable ForeignKeys foreignKeys,
                    @Nullable NonNullConstraints nonNullConstraints,
                    @Nullable IRISafeConstraints iriSafeConstraints) {
        this.name = name;
        this.uniqueConstraints = uniqueConstraints;
        this.otherFunctionalDependencies = otherFunctionalDependencies;
        this.foreignKeys = foreignKeys;
        this.nonNullConstraints = nonNullConstraints;
        this.iriSafeConstraints = iriSafeConstraints;
    }

    public abstract Lens createViewDefinition(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException;

    public abstract void insertIntegrityConstraints(Lens relation,
                                                    ImmutableList<NamedRelationDefinition> baseRelations,
                                                    MetadataLookup metadataLookup, DBParameters dbParameters) throws MetadataExtractionException;

    /**
     * Propagates unique constraints of this lens to its parents, if possible. Returns true if at least one constraint was propagated.
     */
    public boolean propagateUniqueConstraintsUp(Lens relation, ImmutableList<NamedRelationDefinition> parents, QuotedIDFactory idFactory) throws MetadataExtractionException {
        //Does nothing by default, but is implemented by JsonBasicLens. May also be implemented by other lenses under certain conditions.
        return false;
    }

    /**
     * May be incomplete, but must not produce any false positive.
     *
     * Returns the attributes for which it can be proved that the projection over them includes the results
     * of the projection of the parent relation over the parent attributes under set semantics (no concern for duplicates).
     *
     * Parent attributes are expected to all come from the same parent.
     */
    public abstract ImmutableList<ImmutableList<Attribute>> getAttributesIncludingParentOnes(
            Lens lens, ImmutableList<Attribute> parentAttributes);

    protected RelationDefinition.AttributeListBuilder createAttributeBuilder(IQ iq, DBParameters dbParameters)  {
        SingleTermTypeExtractor uniqueTermTypeExtractor = dbParameters.getCoreSingletons().getUniqueTermTypeExtractor();
        QuotedIDFactory quotedIdFactory = dbParameters.getQuotedIDFactory();

        RelationDefinition.AttributeListBuilder builder = AbstractRelationDefinition.attributeListBuilder();
        IQTree iqTree = iq.getTree();

        ImmutableSet<QuotedID> addedNonNullAttributes = nonNullConstraints == null
                ? ImmutableSet.of()
                : nonNullConstraints.added.stream()
                .map(quotedIdFactory::createAttributeID)
                .collect(ImmutableCollectors.toSet());

        RawQuotedIDFactory rawQuotedIqFactory = new RawQuotedIDFactory(quotedIdFactory);

        for (Variable v : iq.getProjectionAtom().getVariables()) {
            QuotedID attributeId = rawQuotedIqFactory.createAttributeID(v.getName());

            boolean isNullable = (!addedNonNullAttributes.contains(attributeId))
                    && iqTree.getVariableNullability().isPossiblyNullable(v);

            builder.addAttribute(attributeId,
                    (DBTermType) uniqueTermTypeExtractor.extractSingleTermType(v, iqTree)
                            .orElseGet(() -> dbParameters.getDBTypeFactory().getAbstractRootDBType()),
                    isNullable);
        }
        return builder;
    }

    protected IQTree addIRISafeConstraints(IQTree iqTreeBeforeIRISafeConstraints, DBParameters dbParameters) {
        if (iriSafeConstraints == null || iriSafeConstraints.added.isEmpty())
            return iqTreeBeforeIRISafeConstraints;

        ImmutableSet<Variable> initialProjectedVariables = iqTreeBeforeIRISafeConstraints.getVariables();

        QuotedIDFactory quotedIdFactory = dbParameters.getQuotedIDFactory();
        RawQuotedIDFactory rawQuotedIqFactory = new RawQuotedIDFactory(quotedIdFactory);

        ImmutableSet<Variable> iriSafeVariables = iriSafeConstraints.added.stream()
                .map(quotedIdFactory::createAttributeID)
                .map(a -> initialProjectedVariables.stream()
                        .filter(v -> rawQuotedIqFactory.createAttributeID(v.getName()).equals(a))
                        .findAny())
                .flatMap(Optional::stream)
                .collect(ImmutableCollectors.toSet());

        if (iriSafeVariables.isEmpty())
            // TODO: issue a warning
            return iqTreeBeforeIRISafeConstraints;

        CoreSingletons coreSingletons = dbParameters.getCoreSingletons();
        VariableGenerator variableGenerator = coreSingletons.getCoreUtilsFactory()
                .createVariableGenerator(iqTreeBeforeIRISafeConstraints.getKnownVariables());

        TermFactory termFactory = coreSingletons.getTermFactory();
        IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();
        SubstitutionFactory substitutionFactory = coreSingletons.getSubstitutionFactory();


        InjectiveSubstitution<Variable> renaming = iriSafeVariables.stream()
                .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));

        IRISafenessDeclarationFunctionSymbol iriSafenessDeclarationFunctionSymbol = coreSingletons.getDBFunctionsymbolFactory()
                .getIRISafenessDeclaration();

        Substitution<ImmutableTerm> substitution = iriSafeVariables.stream()
                .collect(substitutionFactory.toSubstitution(
                        v -> termFactory.getImmutableFunctionalTerm(iriSafenessDeclarationFunctionSymbol, renaming.get(v))));

        ConstructionNode newConstructionNode = iqFactory.createConstructionNode(initialProjectedVariables, substitution);

        return iqFactory.createUnaryIQTree(
                newConstructionNode,
                iqTreeBeforeIRISafeConstraints.applyFreshRenaming(renaming))
                .normalizeForOptimization(variableGenerator);
    }

    protected void insertTransitiveFunctionalDependencies(ImmutableSet<FunctionalDependencyConstruct> previousDependencies, NamedRelationDefinition relation, CoreSingletons coreSingletons) throws AttributeNotFoundException, MetadataExtractionException {
        var uselessVariableGenerator = new VariableGeneratorImpl(ImmutableSet.of(), coreSingletons.getTermFactory());
        var var2Attr = relation.getAttributes().stream()
                .collect(ImmutableCollectors.toMap(
                        attr -> uselessVariableGenerator.generateNewVariable(attr.getID().getName()),
                        attr -> attr
                ));
        var id2Var = var2Attr.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        entry -> entry.getValue().getID(),
                        Map.Entry::getKey)
                );
        if(previousDependencies.stream()
                .anyMatch(fd ->
                                fd.getDeterminants().stream()
                                        .anyMatch(id -> !id2Var.containsKey(id))
                                || fd.getDeterminants().stream()
                                        .anyMatch(id -> !id2Var.containsKey(id))
                        ))
            throw new MetadataExtractionException(String.format(
                    "Cannot find attribute for Functional Dependency of %s.", relation.getID()));
        FunctionalDependencies allDependencies = previousDependencies.stream()
                .map(fd -> Maps.immutableEntry(
                        fd.getDeterminants().stream()
                                .map(id2Var::get).collect(ImmutableCollectors.toSet()),
                        fd.getDependents().stream()
                                .map(id2Var::get).collect(ImmutableCollectors.toSet())
                ))
                .collect(FunctionalDependencies.toFunctionalDependencies());
        for(var entry : allDependencies.stream().collect(ImmutableCollectors.toList())) {
            addTransitiveDependency(
                    relation,
                    entry.getKey().stream()
                            .map(var2Attr::get)
                            .collect(ImmutableCollectors.toSet()),
                    entry.getValue().stream()
                            .map(var2Attr::get)
                            .collect(ImmutableCollectors.toSet()));
        }
    }

    private void addTransitiveDependency(NamedRelationDefinition relation, ImmutableSet<Attribute> determinants, Set<Attribute> dependents) throws AttributeNotFoundException {
        var builder = FunctionalDependency.defaultBuilder(relation);
        for (Attribute determinant : determinants) {
            builder.addDeterminant(determinant.getID());
        }
        for (Attribute attribute : dependents) {
            builder.addDependent(attribute.getID());
        }
        builder.build();
    }


    protected static class UniqueConstraints extends JsonOpenObject {
        @Nonnull
        public final List<AddUniqueConstraints> added;

        @JsonCreator
        public UniqueConstraints(@JsonProperty("added") List<AddUniqueConstraints> added) {
            this.added = added;
        }
    }

    protected static class AddUniqueConstraints extends JsonOpenObject {
        @Nonnull
        public final String name;
        @Nonnull
        public final List<String> determinants;
        public final Boolean isPrimaryKey;


        @JsonCreator
        public AddUniqueConstraints(@JsonProperty("name") String name,
                                    @JsonProperty("determinants") List<String> determinants,
                                    @JsonProperty("isPrimaryKey") Boolean isPrimaryKey) {
            this.name = name;
            this.determinants = determinants;
            this.isPrimaryKey = isPrimaryKey;
        }

        /*
         * Override equals method to ensure we can check for object equality
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            AddUniqueConstraints other = (AddUniqueConstraints) obj;
            return Objects.equals(determinants, other.determinants);
        }

        /*
         * Override hashCode method to ensure we can check for object equality
         */
        @Override
        public int hashCode() {
            return Objects.hash(determinants);
        }
    }

    protected static class OtherFunctionalDependencies extends JsonOpenObject {
        @Nonnull
        public final List<AddFunctionalDependency> added;

        @JsonCreator
        public OtherFunctionalDependencies(@JsonProperty("added") List<AddFunctionalDependency> added) {
            this.added = added;
        }
    }

    protected static class AddFunctionalDependency extends JsonOpenObject {
        @Nonnull
        public final List<String> determinants;
        @Nonnull
        public final List<String> dependents;

        public AddFunctionalDependency(@JsonProperty("determinants") List<String> determinants,
                                       @JsonProperty("dependents") List<String> dependents) {
            this.determinants = determinants;
            this.dependents = dependents;
        }

        /*
         * Override equals method to ensure we can check for object equality
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            AddFunctionalDependency other = (AddFunctionalDependency) obj;
            return Objects.equals(ImmutableMap.of(determinants, dependents),
                    ImmutableMap.of(other.determinants, other.dependents));
        }

        /*
         * Override hashCode method to ensure we can check for object equality
         */
        @Override
        public int hashCode() {
            return Objects.hash(ImmutableMap.of(determinants, dependents));
        }
    }

    protected static class ForeignKeys extends JsonOpenObject {
        @Nonnull
        public final List<AddForeignKey> added;

        @JsonCreator
        public ForeignKeys(@JsonProperty("added") List<AddForeignKey> added) {
            this.added = added;
        }
    }

    protected static class AddForeignKey extends JsonOpenObject {
        // TODO: make it nullable
        @Nonnull
        public final String name;
        @Nonnull
        public final List<String> from;
        @Nonnull
        public final ForeignKeyPart to;

        public AddForeignKey(@JsonProperty("name") String name,
                             @JsonProperty("from") List<String> from,
                             @JsonProperty("to") ForeignKeyPart to) {
            this.name = name;
            this.from = from;
            this.to = to;
        }

        /**
         * Name is not considered
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AddForeignKey that = (AddForeignKey) o;
            return from.equals(that.from) && to.equals(that.to);
        }

        /**
         * Name is not considered
         */
        @Override
        public int hashCode() {
            return Objects.hash(from, to);
        }
    }

    public static class ForeignKeyPart extends JsonOpenObject {
        public final List<String> relation;
        public final List<String> columns;

        @JsonCreator
        public ForeignKeyPart(@JsonProperty("relation") List<String> relation,
                              @JsonProperty("columns") List<String> columns) {
            this.relation = relation;
            this.columns = columns;
        }
    }

    protected static class TemporaryLensPredicate extends AtomPredicateImpl {

        protected TemporaryLensPredicate(String name, ImmutableList<TermType> baseTypesForValidation) {
            super(name, baseTypesForValidation);
        }
    }

    public static class JSONLensDeserializer extends JsonDeserializer<JsonLens> {

        @Override
        public JsonLens deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException {
            ObjectMapper mapper = (ObjectMapper) jp.getCodec();
            JsonNode node = mapper.readTree(jp);
            String type = node.get("type").asText();

            Class<? extends JsonLens> instanceClass;
            switch (type) {
                case "BasicLens":
                // Deprecated
                case "BasicViewDefinition":
                    instanceClass = JsonBasicLens.class;
                    break;
                case "SQLLens":
                // Deprecated
                case "SQLViewDefinition":
                    instanceClass = JsonSQLLens.class;
                    break;
                case "JoinLens":
                // Deprecated
                case "JoinViewDefinition":
                    instanceClass = JsonJoinLens.class;
                    break;
                case "FlattenLens":
                // Deprecated
                case "FlattenedViewDefinition":
                    instanceClass = JsonFlattenLens.class;
                    break;
                case "UnionLens":
                    instanceClass = JsonUnionLens.class;
                    break;
                default:
                    // TODO: throw proper exception
                    throw new RuntimeException("Unsupported type of lens: " + type);
            }
            return mapper.treeToValue(node, instanceClass);
        }
    }

    protected static class NonNullConstraints extends JsonOpenObject {
        @Nonnull
        public final List<String> added;

        @JsonCreator
        public NonNullConstraints(@JsonProperty("added") List<String> added) {
            this.added = added;
        }
    }

    protected static class IRISafeConstraints extends JsonOpenObject {
        @Nonnull
        public final List<String> added;

        @JsonCreator
        public IRISafeConstraints(@JsonProperty("added") List<String> added) {
            this.added = added;
        }
    }
}
