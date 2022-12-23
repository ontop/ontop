package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.json.JsonLens;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Only looks at the target relations of existing FKs, does not consider transitive closure of FKs.
 */
public class BasicLensFKSaturator implements LensFKSaturator {

    @Inject
    protected BasicLensFKSaturator() {
    }

    @Override
    public void saturateForeignKeys(ImmutableList<Lens> lenses,
                                    ImmutableMultimap<RelationID, RelationID> childrenMultimap,
                                    ImmutableMap<RelationID, JsonLens> jsonLensMap) {
        ImmutableMap<RelationID, Lens> lensMap = lenses.stream()
                .collect(ImmutableCollectors.toMap(
                        NamedRelationDefinition::getID,
                        d -> d));

        lenses
                .forEach(v -> saturate(v, childrenMultimap, jsonLensMap, lensMap));
    }

    private void saturate(Lens view, ImmutableMultimap<RelationID, RelationID> childrenMultimap,
                          ImmutableMap<RelationID, JsonLens> jsonLensMap,
                          ImmutableMap<RelationID, Lens> lensMap) {
        view.getForeignKeys()
                .forEach(fk -> deriveFK(view, fk, childrenMultimap, jsonLensMap, lensMap));

    }

    private void deriveFK(Lens view, ForeignKeyConstraint foreignKey,
                          ImmutableMultimap<RelationID, RelationID> childrenMultimap,
                          ImmutableMap<RelationID, JsonLens> jsonLensMap,
                          ImmutableMap<RelationID, Lens> lensMap) {
        NamedRelationDefinition targetRelation = foreignKey.getReferencedRelation();
        RelationID targetRelationId = targetRelation.getID();
        if (childrenMultimap.containsKey(targetRelationId)) {
            ImmutableList<Attribute> targetAttributes = foreignKey.getComponents().stream()
                    .map(ForeignKeyConstraint.Component::getReferencedAttribute)
                    .collect(ImmutableCollectors.toList());

            childrenMultimap.get(targetRelationId)
                    .forEach(c -> deriveFKTarget(view, foreignKey, c, targetAttributes, childrenMultimap, jsonLensMap,
                            lensMap));
        }
    }

    /**
     * TODO: make it recursive so as to look for the higher level views
     */
    private void deriveFKTarget(Lens sourceView, ForeignKeyConstraint foreignKey, RelationID childIdOfTarget,
                                ImmutableList<Attribute> targetAttributes,
                                ImmutableMultimap<RelationID, RelationID> childrenMultimap,
                                ImmutableMap<RelationID, JsonLens> jsonViewMap,
                                ImmutableMap<RelationID, Lens> viewDefinitionMap) {
        if ((!jsonViewMap.containsKey(childIdOfTarget)) || !viewDefinitionMap.containsKey(childIdOfTarget))
            // TODO: log a warning, because children are expected to be views
            return;

        Lens childRelation = viewDefinitionMap.get(childIdOfTarget);

        jsonViewMap.get(childIdOfTarget).getAttributesIncludingParentOnes(
                childRelation,
                targetAttributes)
                .forEach(as -> addForeignKey(sourceView, foreignKey, childRelation, as));
    }

    private void addForeignKey(Lens sourceView, ForeignKeyConstraint initialFK,
                               Lens targetRelation, ImmutableList<Attribute> targetAttributes) {
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
