package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.AbstractRelationDefinition;
import it.unibz.inf.ontop.dbschema.impl.RawQuotedIDFactory;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.model.atom.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@JsonDeserialize(using = JsonView.JSONViewDeSerializer.class)
public abstract class JsonView extends JsonOpenObject {
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

    public JsonView(List<String> name, @Nullable UniqueConstraints uniqueConstraints,
                    @Nullable OtherFunctionalDependencies otherFunctionalDependencies, @Nullable ForeignKeys foreignKeys,
                    @Nullable NonNullConstraints nonNullConstraints) {
        this.name = name;
        this.uniqueConstraints = uniqueConstraints;
        this.otherFunctionalDependencies = otherFunctionalDependencies;
        this.foreignKeys = foreignKeys;
        this.nonNullConstraints = nonNullConstraints;
    }

    public abstract OntopViewDefinition createViewDefinition(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException;

    public abstract void insertIntegrityConstraints(OntopViewDefinition relation,
                                                    ImmutableList<NamedRelationDefinition> baseRelations,
                                                    MetadataLookup metadataLookup, DBParameters dbParameters) throws MetadataExtractionException;

    /**
     * May be incomplete, but must not produce any false positive.
     *
     * Returns the attributes for which it can be proved that the projection over them includes the results
     * of the projection of the parent relation over the parent attributes under set semantics (no concern for duplicates).
     *
     * Parent attributes are expected to all come from the same parent.
     */
    public abstract ImmutableList<ImmutableList<Attribute>> getAttributesIncludingParentOnes(
            OntopViewDefinition ontopViewDefinition, ImmutableList<Attribute> parentAttributes);

    protected RelationDefinition.AttributeListBuilder createAttributeBuilder(IQ iq, DBParameters dbParameters) throws MetadataExtractionException {
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
         * Ovverride equals method to ensure we can check for object equality
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
         * Ovverride hashCode method to ensure we can check for object equality
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
         * Ovverride equals method to ensure we can check for object equality
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
         * Ovverride hashCode method to ensure we can check for object equality
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

    protected static class TemporaryViewPredicate extends AtomPredicateImpl {

        protected TemporaryViewPredicate(String name, ImmutableList<TermType> baseTypesForValidation) {
            super(name, baseTypesForValidation);
        }
    }

    public static class JSONViewDeSerializer extends JsonDeserializer<JsonView> {

        @Override
        public JsonView deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException {
            ObjectMapper mapper = (ObjectMapper) jp.getCodec();
            JsonNode node = mapper.readTree(jp);
            String type = node.get("type").asText();

            Class<? extends JsonView> instanceClass;
            switch (type) {
                case "BasicViewDefinition":
                    instanceClass = JsonBasicView.class;
                    break;
                case "SQLViewDefinition":
                    instanceClass = JsonSQLView.class;
                    break;
                case "JoinViewDefinition":
                    instanceClass = JsonJoinView.class;
                    break;
                default:
                    // TODO: throw proper exception
                    throw new RuntimeException("Unsupported type of Ontop views: " + type);
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
}
