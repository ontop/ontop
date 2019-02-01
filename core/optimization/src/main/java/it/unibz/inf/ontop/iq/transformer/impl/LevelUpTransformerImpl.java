package it.unibz.inf.ontop.iq.transformer.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DatabaseRelationDefinition;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.transformer.LevelUpTransformer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;


/**
 */
public class LevelUpTransformerImpl implements LevelUpTransformer {

    @Override
    public IQTree transform(IQTree tree) {
       // Select the relation to expand
        Optional<DatabaseRelationDefinition> leafRelations = selectLeafRelation(tree);
        return leafRelations.isPresent()?
                unfoldRelation(tree, leafRelations.get()):
                tree;
    }

    private IQTree unfoldRelation(IQTree tree, DatabaseRelationDefinition dR) {

    }

    private Optional<DatabaseRelationDefinition> selectLeafRelation(IQTree tree) {
        ImmutableSet<DatabaseRelationDefinition> rDefs  = retrieveAllRelations(Stream.of(), tree)
                .collect(ImmutableCollectors.toSet());
        return rDefs.stream()
                .filter(r -> !hasChildIn(r, rDefs))
                .findFirst();
    }

    private Stream<DatabaseRelationDefinition> retrieveAllRelations(Stream<DatabaseRelationDefinition> rDefs, IQTree tree) {
        if(tree instanceof LeafIQTree){
            return Stream.of();
        }
        if(tree instanceof ExtensionalDataNode){
            DatabaseRelationDefinition rDef = ((DatabaseRelationDefinition)((ExtensionalDataNode) tree).getDataAtom().getPredicate().getRelationDefinition());
            return Stream.concat(
                    rDefs,
                    getAncestorRelations(Optional.of(rDef))
            );
        }
        for(IQTree child: tree.getChildren()){
            rDefs = retrieveAllRelations(rDefs, child);
        }
        return rDefs;
    }

    // includes the input relation
    private Stream<DatabaseRelationDefinition> getAncestorRelations(Optional<DatabaseRelationDefinition> rDef) {
        if(rDef.isPresent()){
            return Stream.concat(
                    Stream.of(rDef.get()),
                    getAncestorRelations(rDef.get().getParentRelation()));
        }
        return Stream.of();
    }

    private boolean hasChildIn(DatabaseRelationDefinition rDef, ImmutableSet<DatabaseRelationDefinition> rDefs) {
        return rDefs.stream()
                .map(r -> r.getParentRelation())
                .filter(o -> o.isPresent())
                .map(o -> o.get())
                .anyMatch(r -> r.equals(rDef));
    }

    @Inject
    private LevelUpTransformerImpl() {


    }










}
