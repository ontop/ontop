package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.type.NotYetTypedEqualityTransformer;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

@JsonDeserialize(as = JsonJoinView.class)
public class JsonJoinView extends JsonBasicOrJoinView {

    @Nonnull
    public final JoinPart joinPart;

    protected JsonJoinView(@JsonProperty("columns") JsonBasicOrJoinView.Columns columns, @JsonProperty("name") List<String> name,
                           @JsonProperty("join") JoinPart joinPart,
                           @JsonProperty("filterExpression") String filterExpression,
                           @JsonProperty("uniqueConstraints") UniqueConstraints uniqueConstraints,
                           @JsonProperty("otherFunctionalDependencies") OtherFunctionalDependencies otherFunctionalDependencies,
                           @JsonProperty("foreignKeys") ForeignKeys foreignKeys,
                           @JsonProperty("nonNullConstraints") NonNullConstraints nonNullConstraints) {
        super(name, uniqueConstraints, otherFunctionalDependencies, foreignKeys, nonNullConstraints, columns, filterExpression);
        this.joinPart = joinPart;
    }

    @Override
    protected ImmutableMap<NamedRelationDefinition, String> extractParentDefinitions(
            DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup) throws MetadataExtractionException {
        QuotedIDFactory quotedIDFactory = dbParameters.getQuotedIDFactory();

        if (joinPart.columnPrefixes.size() != joinPart.relations.size()){
            throw new MetadataExtractionException("Exactly one column prefix must be defined per parent relation");
        }

        if (joinPart.relations.size() < 2)
            throw new MetadataExtractionException("At least two relations are expected");

        ImmutableMap.Builder<NamedRelationDefinition, String> builder = ImmutableMap.builder();
        for(int i=0 ; i < joinPart.relations.size(); i++) {
            NamedRelationDefinition parentDefinition = parentCacheMetadataLookup.getRelation(quotedIDFactory.createRelationID(
                    joinPart.relations.get(i).toArray(new String[0])));

            builder.put(parentDefinition, joinPart.columnPrefixes.get(i));
        }

        return builder.build();
    }

    /**
     * Inferred from the tree
     */
    @Override
    protected ImmutableList<AddUniqueConstraints> inferInheritedConstraints(OntopViewDefinition relation,
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

    protected static class JoinPart extends JsonOpenObject {
        public final List<List<String>> relations;
        public final List<String> columnPrefixes;

        @JsonCreator
        public JoinPart(@JsonProperty("relations") List<List<String>> relations,
                        @JsonProperty("columnPrefixes") List<String> columnPrefixes) {
            this.relations = relations;
            this.columnPrefixes = columnPrefixes;
        }
    }
}
