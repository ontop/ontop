package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.UniqueConstraintImpl;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@JsonDeserialize(as = JsonBasicLens.class)
public class JsonBasicLens extends JsonBasicOrJoinLens {

    @Nonnull
    public final List<String> baseRelation;

    @JsonCreator
    public JsonBasicLens(@JsonProperty("columns") JsonBasicOrJoinLens.Columns columns, @JsonProperty("name") List<String> name,
                         @JsonProperty("baseRelation") List<String> baseRelation,
                         @JsonProperty("filterExpression") String filterExpression,
                         @JsonProperty("uniqueConstraints") UniqueConstraints uniqueConstraints,
                         @JsonProperty("otherFunctionalDependencies") OtherFunctionalDependencies otherFunctionalDependencies,
                         @JsonProperty("foreignKeys") ForeignKeys foreignKeys,
                         @JsonProperty("nonNullConstraints") NonNullConstraints nonNullConstraints,
                         @JsonProperty("iriSafeConstraints") IRISafeConstraints iriSafeConstraints) {
        super(name, uniqueConstraints, otherFunctionalDependencies, foreignKeys, nonNullConstraints, iriSafeConstraints, columns, filterExpression);
        this.baseRelation = baseRelation;
    }

    @Override
    protected ImmutableList<ParentDefinition> extractParentDefinitions(DBParameters dbParameters,
                                                                                     MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException {
        QuotedIDFactory quotedIDFactory = dbParameters.getQuotedIDFactory();
        NamedRelationDefinition parentDefinition = parentCacheMetadataLookup.getRelation(quotedIDFactory.createRelationID(
                baseRelation.toArray(new String[0])));

        return ImmutableList.of(new ParentDefinition(parentDefinition,""));
    }

    /**
     * TODO: support duplication of the column (not just renamings)
     */
    @Override
    public ImmutableList<ImmutableList<Attribute>> getAttributesIncludingParentOnes(Lens lens,
                                                                                    ImmutableList<Attribute> parentAttributes) {
        if (filterExpression != null && (!filterExpression.isEmpty()))
            // TODO: log a warning
            return ImmutableList.of();

        return getDerivedFromParentAttributes(lens, parentAttributes);
    }

    public boolean propagateUniqueConstraintsUp(Lens relation, ImmutableList<NamedRelationDefinition> parents, QuotedIDFactory idFactory) throws MetadataExtractionException {
        //There is no guarantee a UC will hold in the parent if the lens performed a filter.
        if(filterExpression != null)
            return false;

        var ucs = relation.getUniqueConstraints();
        var propagableConstraints = ucs.stream().filter(this::noDeterminantOverridden).filter(u -> this.ucNotInParent(u, parents.get(0))).collect(Collectors.toList());
        if(propagableConstraints.isEmpty())
            return false;
        for(var uc : propagableConstraints) {
            var builder = UniqueConstraint.builder(parents.get(0), uc.getName());
            JsonMetadata.deserializeAttributeList(idFactory, uc.getDeterminants().stream().map(d -> d.getID().getSQLRendering()).collect(Collectors.toList()), builder::addDeterminant);
            builder.build();
        }
        return true;
    }

    private boolean noDeterminantOverridden(UniqueConstraint uc) {
        return Sets.intersection(
                uc.getDeterminants().stream().map(a -> a.getID().getName()).collect(Collectors.toSet()),
                columns.added.stream().map(a -> a.name).collect(Collectors.toSet())
        ).isEmpty();
    }

    private boolean ucNotInParent(UniqueConstraint uc, NamedRelationDefinition parent) {
        return !parent.getUniqueConstraints().stream()
                .anyMatch(u -> this.ucEquals(uc, u));
    }

    private boolean ucEquals(UniqueConstraint uc1, UniqueConstraint uc2) {
        return uc1.getDeterminants().stream()
                .map(a -> a.getID().getSQLRendering())
                .collect(Collectors.toSet()).equals(
                        uc2.getDeterminants().stream()
                                .map(a -> a.getID().getSQLRendering())
                                .collect(Collectors.toSet())
                );
    }

    @Override
    protected void insertUniqueConstraints(Lens relation, QuotedIDFactory idFactory,
                                           List<AddUniqueConstraints> addUniqueConstraints,
                                           ImmutableList<NamedRelationDefinition> baseRelations,
                                           CoreSingletons coreSingletons) throws MetadataExtractionException {
        super.insertUniqueConstraints(relation, idFactory, addUniqueConstraints, baseRelations, coreSingletons);
    }

}
