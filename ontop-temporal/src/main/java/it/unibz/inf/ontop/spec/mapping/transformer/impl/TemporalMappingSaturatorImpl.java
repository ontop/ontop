package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.TreeTraverser;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.transformer.TemporalMappingSaturator;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;
import it.unibz.inf.ontop.temporal.model.TemporalExpression;
import it.unibz.inf.ontop.temporal.model.tree.TemporalTreeNode;

public class TemporalMappingSaturatorImpl implements TemporalMappingSaturator{
    @Override
    public Mapping saturate(Mapping mapping, DBMetadata dbMetadata, TBoxReasoner saturatedTBox) {
        return null;
    }

    public Mapping saturate(Mapping mapping, DBMetadata dbMetadata, Mapping temporalMapping, DBMetadata temporalDBMetadata){

        TreeTraverser treeTraverser = getTreeTraverser();

        return null;
    }

    //TODO: move this function into where it will be used
    public TreeTraverser<TemporalExpression> getTreeTraverser(){
        return TreeTraverser.using(TemporalExpression::getChildNodes);
    }
}
