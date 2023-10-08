package it.unibz.inf.ontop.constraints.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.constraints.Homomorphism;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

import java.util.*;

public class ExtensionalDataNodeHomomorphismIteratorImpl extends AbstractHomomorphismIterator<ExtensionalDataNode, ExtensionalDataNodeListContainmentCheck.ChasedExtensionalDataNode> {
    public ExtensionalDataNodeHomomorphismIteratorImpl(Homomorphism baseHomomorphism, ImmutableList<ExtensionalDataNode> from, ImmutableCollection<ExtensionalDataNodeListContainmentCheck.ChasedExtensionalDataNode> to) {
        super(baseHomomorphism, from, to);
    }

    @Override
    boolean equalPredicates(ExtensionalDataNode from, ExtensionalDataNodeListContainmentCheck.ChasedExtensionalDataNode to) {
        return from.getRelationDefinition().equals(to.getRelationDefinition());
    }

    @Override
    void extendHomomorphism(Homomorphism.Builder builder, ExtensionalDataNode from, ExtensionalDataNodeListContainmentCheck.ChasedExtensionalDataNode to) {
        for (Map.Entry<Integer, ? extends VariableOrGroundTerm> e : from.getArgumentMap().entrySet())
            if (!builder.extend(e.getValue(), to.getArgument(e.getKey())).isValid())
                return;
    }
}


