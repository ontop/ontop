package it.unibz.inf.ontop.query.unfolding.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractQueryMergingTransformer;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.spec.mapping.Mapping.RDFAtomIndexPattern.SUBJECT_OF_ALL_CLASSES;

public abstract class AbstractMultiPhaseQueryMergingTransformer extends AbstractQueryMergingTransformer {

    protected final TermFactory termFactory;
    protected final Mapping mapping;

    private final ImmutableSet<ObjectStringTemplateFunctionSymbol> iriTemplates;
    private final ImmutableSet<ObjectStringTemplateFunctionSymbol> bnodeTemplates;

    protected AbstractMultiPhaseQueryMergingTransformer(Mapping mapping, VariableGenerator variableGenerator, CoreSingletons coreSingletons) {
        super(variableGenerator, coreSingletons);
        this.mapping = mapping;
        this.termFactory = coreSingletons.getTermFactory();
        var objectTemplates = this.termFactory.getDBFunctionSymbolFactory().getObjectTemplates();
        this.iriTemplates = objectTemplates.stream()
                .filter(t -> t instanceof IRIStringTemplateFunctionSymbol)
                .map(t -> (IRIStringTemplateFunctionSymbol)t)
                .collect(ImmutableSet.toImmutableSet());
        this.bnodeTemplates = objectTemplates.stream()
                .filter(t -> t instanceof BnodeStringTemplateFunctionSymbol)
                .map(t -> (BnodeStringTemplateFunctionSymbol)t)
                .collect(ImmutableSet.toImmutableSet());
    }

    protected boolean isTemplateCompatibleWithConstant(ObjectStringTemplateFunctionSymbol template, ObjectConstant objectConstant) {
        ImmutableExpression strictEquality = termFactory.getStrictEquality(
                objectConstant,
                termFactory.getRDFFunctionalTerm(
                        termFactory.getImmutableFunctionalTerm(
                                template,
                                IntStream.range(0, template.getArity())
                                        .mapToObj(i -> variableGenerator.generateNewVariable())
                                        .collect(ImmutableCollectors.toList())),
                        termFactory.getRDFTermTypeConstant(objectConstant.getType())));

        return strictEquality.evaluate2VL(termFactory.createDummyVariableNullability(strictEquality))
                .getValue()
                .filter(v -> v.equals(ImmutableExpression.Evaluation.BooleanValue.FALSE))
                .isEmpty();
    }

    /**
     * TODO: introduce some cache?
     * TODO: use an index data structure based on prefixes and/or suffixes?
     *
     */
    private Optional<ObjectStringTemplateFunctionSymbol> selectCompatibleTemplateWithConstant(ObjectConstant objectConstant) {
        if (objectConstant instanceof IRIConstant)
            return iriTemplates.stream()
                    .filter(t -> isTemplateCompatibleWithConstant(t, objectConstant))
                    .findAny();

        return bnodeTemplates.stream()
                .filter(t -> isTemplateCompatibleWithConstant(t, objectConstant))
                .findAny();
    }

    protected Optional<IQ> getDefinitionCompatibleWithConstant(RDFAtomPredicate rdfAtomPredicate,
                                                               Mapping.RDFAtomIndexPattern indexPattern,
                                                               ObjectConstant objectConstant) {
        Optional<ObjectStringTemplateFunctionSymbol> selectedTemplate = selectCompatibleTemplateWithConstant(objectConstant);

        if (selectedTemplate.isPresent())
            return mapping.getCompatibleDefinitions(rdfAtomPredicate, indexPattern, selectedTemplate.get(), variableGenerator);

        return indexPattern == SUBJECT_OF_ALL_CLASSES
                ? mapping.getMergedClassDefinitions(rdfAtomPredicate)
                : mapping.getMergedDefinitions(rdfAtomPredicate);
    }
}
