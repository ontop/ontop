package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.functionsymbol.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.URITemplates;

import java.util.stream.IntStream;


public class IRIStringTemplateFunctionSymbolImpl extends FunctionSymbolImpl implements IRIStringTemplateFunctionSymbol {
    private final TermType returnType;
    private final String iriTemplate;

    private IRIStringTemplateFunctionSymbolImpl(String iriTemplate, ImmutableList<TermType> expectedBaseTypes,
                                                TermType returnType) {
        super(iriTemplate, expectedBaseTypes.size(), expectedBaseTypes);
        this.returnType = returnType;
        this.iriTemplate = iriTemplate;
    }

    protected static IRIStringTemplateFunctionSymbol createFunctionSymbol(String iriTemplate,
                                                                          TypeFactory typeFactory) {
        int arity = URITemplates.getArity(iriTemplate);

        // TODO: require DB string instead
        TermType stringType = typeFactory.getXsdStringDatatype();

        ImmutableList<TermType> expectedBaseTypes = IntStream.range(0, arity)
                .boxed()
                .map(i -> stringType)
                .collect(ImmutableCollectors.toList());

        return new IRIStringTemplateFunctionSymbolImpl(iriTemplate, expectedBaseTypes, stringType);
    }

    @Override
    public String getIRITemplate() {
        return iriTemplate;
    }
}
