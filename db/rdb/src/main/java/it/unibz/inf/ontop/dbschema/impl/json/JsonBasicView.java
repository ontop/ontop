package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.visit.impl.AbstractPredicateExtractor;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonDeserialize(as = JsonBasicView.class)
public class JsonBasicView extends JsonBasicOrJoinView {

    @Nonnull
    public final List<String> baseRelation;

    @JsonCreator
    public JsonBasicView(@JsonProperty("columns") JsonBasicOrJoinView.Columns columns, @JsonProperty("name") List<String> name,
                         @JsonProperty("baseRelation") List<String> baseRelation,
                         @JsonProperty("filterExpression") String filterExpression,
                         @JsonProperty("uniqueConstraints") UniqueConstraints uniqueConstraints,
                         @JsonProperty("otherFunctionalDependencies") OtherFunctionalDependencies otherFunctionalDependencies,
                         @JsonProperty("foreignKeys") ForeignKeys foreignKeys,
                         @JsonProperty("nonNullConstraints") NonNullConstraints nonNullConstraints) {
        super(name, uniqueConstraints, otherFunctionalDependencies, foreignKeys, nonNullConstraints, columns, filterExpression);
        this.baseRelation = baseRelation;
    }

    @Override
    protected ImmutableMap<NamedRelationDefinition, String> extractParentDefinitions(DBParameters dbParameters,
                                                                                     MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException {
        QuotedIDFactory quotedIDFactory = dbParameters.getQuotedIDFactory();
        NamedRelationDefinition parentDefinition = parentCacheMetadataLookup.getRelation(quotedIDFactory.createRelationID(
                baseRelation.toArray(new String[0])));

        return ImmutableMap.of(parentDefinition, "");
    }

    /**
     * Infer unique constraints from the parent
     *
     * TODO: support renamings
     */
    @Override
    protected ImmutableList<AddUniqueConstraints> inferInheritedConstraints(OntopViewDefinition relation, ImmutableList<NamedRelationDefinition> baseRelations,
                                                                            ImmutableList<QuotedID> addedConstraintsColumns,
                                                                            QuotedIDFactory idFactory,
                                                                            CoreSingletons coreSingletons) {
        // List of added columns
        ImmutableList<QuotedID> addedNewColumns = columns.added.stream()
                .map(a -> idFactory.createAttributeID(a.name))
                .collect(ImmutableCollectors.toList());

        // List of hidden columns
        ImmutableList<QuotedID> hiddenColumnNames = columns.hidden.stream()
                .map(idFactory::createAttributeID)
                .collect(ImmutableCollectors.toList());

        ImmutableList<UniqueConstraint> inheritedConstraints = baseRelations.stream()
                .map(RelationDefinition::getUniqueConstraints)
                .flatMap(Collection::stream)
                .filter(c -> c.getAttributes().stream()
                        .map(Attribute::getID)
                        .noneMatch(addedConstraintsColumns::contains))
                .filter(c -> c.getAttributes().stream()
                        .map(Attribute::getID)
                        .noneMatch(addedNewColumns::contains))
                .filter(c -> c.getAttributes().stream()
                        .map(Attribute::getID)
                        .noneMatch(hiddenColumnNames::contains))
                .collect(ImmutableCollectors.toList());

        // Create unique constraints
        return inheritedConstraints.stream()
                .map(i -> new AddUniqueConstraints(
                        i.getName(),
                        i.getDeterminants().stream()
                                .map(c -> c.getID().getSQLRendering())
                                .collect(Collectors.toList()),
                        // PK by default false
                        false
                ))
                .collect(ImmutableCollectors.toList());
    }

    /**
     * TODO: support duplication of the column (not just renamings)
     */
    @Override
    public ImmutableList<ImmutableList<Attribute>> getAttributesIncludingParentOnes(OntopViewDefinition ontopViewDefinition,
                                                                                    ImmutableList<Attribute> parentAttributes) {
        if (filterExpression != null && (!filterExpression.isEmpty()))
            // TODO: log a warning
            return ImmutableList.of();

        IQ viewIQ = ontopViewDefinition.getIQ();

        Optional<ExtensionalDataNode> optionalParentNode = viewIQ.getTree()
                .acceptVisitor(new ExtensionalNodeExtractor())
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


    private static class ExtensionalNodeExtractor extends AbstractPredicateExtractor<ExtensionalDataNode> {
        @Override
        public Stream<ExtensionalDataNode> visitIntensionalData(IntensionalDataNode dataNode) {
            return Stream.empty();
        }

        @Override
        public Stream<ExtensionalDataNode> visitExtensionalData(ExtensionalDataNode dataNode) {
            return Stream.of(dataNode);
        }
    }
}
