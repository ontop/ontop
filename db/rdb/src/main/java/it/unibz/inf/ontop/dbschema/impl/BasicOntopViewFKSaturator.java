package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.json.JsonView;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Only looks at the target relations of existing FKs, does not consider transitive closure of FKs.
 */
public class BasicOntopViewFKSaturator implements OntopViewFKSaturator {

    @Inject
    protected BasicOntopViewFKSaturator() {
    }

    @Override
    public void saturateForeignKeys(ImmutableList<OntopViewDefinition> viewDefinitions,
                                    ImmutableMultimap<RelationID, RelationID> childrenMultimap,
                                    ImmutableMap<RelationID, JsonView> jsonViewMap) {
        ImmutableMap<RelationID, OntopViewDefinition> viewDefinitionMap = viewDefinitions.stream()
                .collect(ImmutableCollectors.toMap(
                        NamedRelationDefinition::getID,
                        d -> d));

        viewDefinitions
                .forEach(v -> saturate(v, childrenMultimap, jsonViewMap, viewDefinitionMap));
    }

    private void saturate(OntopViewDefinition view, ImmutableMultimap<RelationID, RelationID> childrenMultimap,
                          ImmutableMap<RelationID, JsonView> jsonViewMap,
                          ImmutableMap<RelationID, OntopViewDefinition> viewDefinitionMap) {
        view.getForeignKeys()
                .forEach(fk -> deriveFK(view, fk, childrenMultimap, jsonViewMap, viewDefinitionMap));

    }

    private void deriveFK(OntopViewDefinition view, ForeignKeyConstraint foreignKey,
                          ImmutableMultimap<RelationID, RelationID> childrenMultimap,
                          ImmutableMap<RelationID, JsonView> jsonViewMap,
                          ImmutableMap<RelationID, OntopViewDefinition> viewDefinitionMap) {
        NamedRelationDefinition targetRelation = foreignKey.getReferencedRelation();
        RelationID targetRelationId = targetRelation.getID();
        if (childrenMultimap.containsKey(targetRelationId)) {
            ImmutableList<Attribute> targetAttributes = foreignKey.getComponents().stream()
                    .map(ForeignKeyConstraint.Component::getReferencedAttribute)
                    .collect(ImmutableCollectors.toList());

            childrenMultimap.get(targetRelationId)
                    .forEach(c -> deriveFKTarget(view, foreignKey, c, targetAttributes, childrenMultimap, jsonViewMap,
                            viewDefinitionMap));
        }
    }

    /**
     * TODO: make it recursive so as to look for the higher level views
     */
    private void deriveFKTarget(OntopViewDefinition sourceView, ForeignKeyConstraint foreignKey, RelationID childIdOfTarget,
                                ImmutableList<Attribute> targetAttributes,
                                ImmutableMultimap<RelationID, RelationID> childrenMultimap,
                                ImmutableMap<RelationID, JsonView> jsonViewMap,
                                ImmutableMap<RelationID, OntopViewDefinition> viewDefinitionMap) {
        if ((!jsonViewMap.containsKey(childIdOfTarget)) || !viewDefinitionMap.containsKey(childIdOfTarget))
            // TODO: log a warning, because children are expected to be views
            return;

        OntopViewDefinition childRelation = viewDefinitionMap.get(childIdOfTarget);

        jsonViewMap.get(childIdOfTarget).getAttributesIncludingParentOnes(
                childRelation,
                targetAttributes)
                .forEach(as -> addForeignKey(sourceView, foreignKey, childRelation, as));
    }

    private void addForeignKey(OntopViewDefinition sourceView, ForeignKeyConstraint initialFK,
                               OntopViewDefinition targetRelation, ImmutableList<Attribute> targetAttributes) {
        // Check if already existing
        if (sourceView.getForeignKeys().stream()
                .filter(fk -> fk.getReferencedRelation().equals(targetRelation))
                .filter(fk -> fk.getComponents().size() == (targetAttributes.size()))
                .anyMatch(fk -> fk.getComponents().stream()
                        .map(ForeignKeyConstraint.Component::getReferencedAttribute)
                        .collect(ImmutableCollectors.toList()).equals(targetAttributes)))
            return;

        ForeignKeyConstraint.Builder builder = ForeignKeyConstraint.builder(UUID.randomUUID().toString(), sourceView,
                targetRelation);

        ImmutableList<ForeignKeyConstraint.Component> initialComponents = initialFK.getComponents();

        IntStream.range(0, targetAttributes.size())
                .boxed()
                .forEach(i -> builder.add(initialComponents.get(i).getAttribute().getIndex(),
                        targetAttributes.get(i).getIndex()));

        builder.build();
    }
}
