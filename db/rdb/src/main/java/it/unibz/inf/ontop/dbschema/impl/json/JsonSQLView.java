package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.AbstractRelationDefinition;
import it.unibz.inf.ontop.dbschema.impl.OntopViewDefinitionImpl;
import it.unibz.inf.ontop.dbschema.impl.RawQuotedIDFactory;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.type.NotYetTypedEqualityTransformer;
import it.unibz.inf.ontop.iq.type.UniqueTermTypeExtractor;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.spec.sqlparser.*;
import it.unibz.inf.ontop.spec.sqlparser.exception.InvalidSelectQueryException;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryException;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.JSQLParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.IntStream;

@JsonPropertyOrder({
        "relations"
})
@JsonDeserialize(as = JsonSQLView.class)
public class JsonSQLView extends JsonView {
    @Nonnull
    public final String query;
    @Nonnull
    public final UniqueConstraints uniqueConstraints;
    @Nonnull
    public final OtherFunctionalDependencies otherFunctionalDependencies;
    @Nonnull
    public final ForeignKeys foreignKeys;

    protected static final Logger LOGGER = LoggerFactory.getLogger(JsonSQLView.class);

    @JsonCreator
    public JsonSQLView(@JsonProperty("name") List<String> name,
                       @JsonProperty("query") String query,
                       @JsonProperty("uniqueConstraints") UniqueConstraints uniqueConstraints,
                       @JsonProperty("otherFunctionalDependencies") OtherFunctionalDependencies otherFunctionalDependencies,
                       @JsonProperty("foreignKeys") ForeignKeys foreignKeys) {
        super(name);
        this.query = query;
        this.uniqueConstraints = uniqueConstraints;
        this.otherFunctionalDependencies = otherFunctionalDependencies;
        this.foreignKeys = foreignKeys;
    }

    @Override
    public OntopViewDefinition createViewDefinition(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException {

        QuotedIDFactory quotedIDFactory = dbParameters.getQuotedIDFactory();
        RelationID relationId = quotedIDFactory.createRelationID(name.toArray(new String[0]));

        IQ iq = createIQ(relationId, dbParameters, parentCacheMetadataLookup);

        // For added columns the termtype, quoted ID and nullability all need to come from the IQ
        RelationDefinition.AttributeListBuilder attributeBuilder = createAttributeBuilder(iq, dbParameters);

        return new OntopViewDefinitionImpl(
                ImmutableList.of(relationId),
                attributeBuilder,
                iq,
                // TODO: consider other levels
                1,
                dbParameters.getCoreSingletons());
    }

    @Override
    public void insertIntegrityConstraints(NamedRelationDefinition relation,
                                           ImmutableList<NamedRelationDefinition> baseRelations,
                                           MetadataLookup metadataLookupForFK) throws MetadataExtractionException {
    }


    private IQ createIQ(RelationID relationId, DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException {

        CoreSingletons coreSingletons = dbParameters.getCoreSingletons();

        TermFactory termFactory = coreSingletons.getTermFactory();
        IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();
        AtomFactory atomFactory = coreSingletons.getAtomFactory();

        RAExpression2IQConverter raExpression2IQConverter = new RAExpression2IQConverter(coreSingletons);

        IQTree iqTree;
        RAExpression raExpression;
        try {
            raExpression = extractRAExpression(dbParameters, parentCacheMetadataLookup);
            iqTree = raExpression2IQConverter.convert(raExpression);
        } catch (JSQLParserException | UnsupportedSelectQueryException | InvalidSelectQueryException e) {
            throw new MetadataExtractionException("Unsupported expression for " + ":\n" + e);
        }

        ImmutableMap<Variable, Variable> map1 = raExpression.getAttributes().asMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> termFactory.getVariable(e.getValue().toString()),
                        e -> termFactory.getVariable(e.getKey().getAttribute().getName())
                ));


        SubstitutionFactory substitutionFactory = coreSingletons.getSubstitutionFactory();
        InjectiveVar2VarSubstitution injectiveVar2VarSubstitution = substitutionFactory.getInjectiveVar2VarSubstitution(map1);
        IQTree iqTreeRenamedVariables = iqTree.applyFreshRenaming(injectiveVar2VarSubstitution);
        NotYetTypedEqualityTransformer notYetTypedEqualityTransformer = coreSingletons.getNotYetTypedEqualityTransformer();
        IQTree iqTreeTransformed = notYetTypedEqualityTransformer.transform(iqTreeRenamedVariables);

        ImmutableSet<Variable> iqTreeVariables = iqTree.getVariables();
        List<Variable> targetList = Lists.newArrayList(iqTreeVariables);
        ImmutableList<Variable> projectedVariables = ImmutableList.copyOf(targetList);
        AtomPredicate tmpPredicate = createTemporaryPredicate(relationId, projectedVariables.size(), coreSingletons);
        DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(tmpPredicate, projectedVariables);
        DistinctVariableOnlyDataAtom projectionAtomWithSubstitution = injectiveVar2VarSubstitution.applyToDistinctVariableOnlyDataAtom(projectionAtom);

        return iqFactory.createIQ(projectionAtomWithSubstitution, iqTreeTransformed)
                .normalizeForOptimization();
    }

    private RAExpression extractRAExpression(DBParameters dbParameters, MetadataLookup metadataLookup)
            throws JSQLParserException, UnsupportedSelectQueryException, InvalidSelectQueryException {
        CoreSingletons coreSingletons = dbParameters.getCoreSingletons();
        SQLQueryParser sq = new SQLQueryParser(coreSingletons);
        return sq.getRAExpression(query, metadataLookup);
    }

    private AtomPredicate createTemporaryPredicate(RelationID relationId, int arity, CoreSingletons coreSingletons) {
        DBTermType dbRootType = coreSingletons.getTypeFactory().getDBTypeFactory().getAbstractRootDBType();

        return new TemporaryViewPredicate(
                relationId.getSQLRendering(),
                // No precise base DB type for the temporary predicate
                IntStream.range(0, arity)
                        .boxed()
                        .map(i -> dbRootType).collect(ImmutableCollectors.toList()));
    }

    private RelationDefinition.AttributeListBuilder createAttributeBuilder(IQ iq, DBParameters dbParameters) throws MetadataExtractionException {
        UniqueTermTypeExtractor uniqueTermTypeExtractor = dbParameters.getCoreSingletons().getUniqueTermTypeExtractor();
        QuotedIDFactory quotedIdFactory = dbParameters.getQuotedIDFactory();

        RelationDefinition.AttributeListBuilder builder = AbstractRelationDefinition.attributeListBuilder();
        IQTree iqTree = iq.getTree();

        RawQuotedIDFactory rawQuotedIqFactory = new RawQuotedIDFactory(quotedIdFactory);

        for (Variable v : iqTree.getVariables()) {
            builder.addAttribute(rawQuotedIqFactory.createAttributeID(v.getName()),
                    (DBTermType) uniqueTermTypeExtractor.extractUniqueTermType(v, iqTree)
                            // TODO: give the name of the view
                            .orElseThrow(() -> new MetadataExtractionException("No type inferred for " + v + " in " + iq)),
                    iqTree.getVariableNullability().isPossiblyNullable(v));
        }
        return builder;
    }

    @JsonPropertyOrder({
            "added"
    })
    private static class UniqueConstraints extends JsonOpenObject {
        @Nonnull
        public final List<AddUniqueConstraints> added;

        @JsonCreator
        public UniqueConstraints(@JsonProperty("added") List<AddUniqueConstraints> added) {
            this.added = added;
        }
    }

    @JsonPropertyOrder({
            "name",
            "determinants",
            "isPrimaryKey"
    })
    private static class AddUniqueConstraints extends JsonOpenObject {
        @Nonnull
        public final String name;
        @Nonnull
        public final List<String> determinants;
        @Nonnull
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

    private static class OtherFunctionalDependencies extends JsonOpenObject {
        @Nonnull
        public final List<AddFunctionalDependency> added;

        @JsonCreator
        public OtherFunctionalDependencies(@JsonProperty("added") List<AddFunctionalDependency> added) {
            this.added = added;
        }
    }

    @JsonPropertyOrder({
            "determinants",
            "dependents"
    })
    private static class AddFunctionalDependency extends JsonOpenObject {
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

    private static class ForeignKeys extends JsonOpenObject {
        @Nonnull
        public final List<AddForeignKey> added;

        @JsonCreator
        public ForeignKeys(@JsonProperty("added") List<AddForeignKey> added) {
            this.added = added;
        }
    }

    @JsonPropertyOrder({
            "determinants",
            "dependents"
    })
    private static class AddForeignKey extends JsonOpenObject {
        @Nonnull
        public final String name;
        @Nonnull
        public final String from;
        @Nonnull
        public final ForeignKeyPart to;

        public AddForeignKey(@JsonProperty("name") String name,
                             @JsonProperty("from") String from,
                             @JsonProperty("to") ForeignKeyPart to) {
            this.name = name;
            this.from = from;
            this.to = to;
        }
    }

    @JsonPropertyOrder({
            "relation",
            "columns"
    })
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
}
