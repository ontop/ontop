package it.unibz.inf.ontop.iq.impl.tree;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.DatabaseRelationDefinition;
import it.unibz.inf.ontop.dbschema.MetadataProvider;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.NonUniqueTermTypeException;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.type.UniqueTermTypeExtractor;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.type.TermType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class IQAttributeExtraction implements IQTree {

    //private final QueryNode qNode;
    private final IQTree newTree;
    private final UniqueTermTypeExtractor utte;

    public IQAttributeExtraction(IQTree newTree, UniqueTermTypeExtractor utte) { this.newTree = newTree; this.utte = utte;}

    public QueryNode getRootNode() {return newTree.getRootNode();}

    public ImmutableList<IQTree> getChildren() {return newTree.getChildren();}

    public VariableNullability getVn () {

        VariableNullability vn = newTree.getVariableNullability();
        return vn;
    }

    public Optional<TermType> getTermType (ImmutableTerm term) {

        return utte.extractUniqueTermType(term, newTree);
    }
}
