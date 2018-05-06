package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.answering.resultset.OntopBinding;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.term.Constant;

import javax.annotation.Nullable;
import java.sql.ResultSet;

public class PostProcessedIriSQLBindingSet extends AbstractOntopBindingSet implements OntopBindingSet {

    private final ResultSet rs;
    private final ConstructionNode constructionNode;

    protected PostProcessedIriSQLBindingSet(ResultSet rs, ImmutableList<String> signature,
                                            ConstructionNode constructionNode) {
        super(signature);
        this.rs = rs;
        this.constructionNode = constructionNode;
    }

    @Nullable
    @Override
    public Constant getConstant(int column) throws OntopResultConversionException {
        throw new RuntimeException("TODO: implement");
    }

    @Nullable
    @Override
    public Constant getConstant(String var) {
        throw new RuntimeException("TODO: implement");
    }

    @Nullable
    @Override
    public OntopBinding getBinding(int column) {
        throw new RuntimeException("TODO: implement");
    }

    @Nullable
    @Override
    protected OntopBinding computeBinding(String variableName) {
        throw new RuntimeException("TODO: implement");
    }

    @Nullable
    @Override
    public OntopBinding getBinding(String name) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public boolean hasBinding(String bindingName) {
        throw new RuntimeException("TODO: implement");
    }
}
