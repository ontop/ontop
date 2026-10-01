package it.unibz.inf.ontop.query.unfolding.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractQueryMergingTransformer;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.template.Template;
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
        if (!hasPrefixCompatibleWithConstant(template, objectConstant))
            return false;

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
     * Sound but incomplete: only considers the leading component.
     * Needed for non-injective templates, for which the strict equality cannot be evaluated.
     */
    private boolean hasPrefixCompatibleWithConstant(ObjectStringTemplateFunctionSymbol template,
                                                    ObjectConstant objectConstant) {
        ImmutableList<Template.Component> components = template.getTemplateComponents();
        if (components.isEmpty())
            return true;

        Template.Component firstComponent = components.get(0);

        return firstComponent.isColumn()
                || objectConstant.getValue().startsWith(firstComponent.getComponent());
    }

    /**
     * TODO: introduce some cache?
     * TODO: use an index data structure based on prefixes and/or suffixes?
     *
     */
    private ImmutableSet<ObjectStringTemplateFunctionSymbol> selectCompatibleTemplatesWithConstant(ObjectConstant objectConstant) {
        ImmutableSet<ObjectStringTemplateFunctionSymbol> templates = (objectConstant instanceof IRIConstant)
                ? iriTemplates
                : bnodeTemplates;

        return templates.stream()
                .filter(t -> isTemplateCompatibleWithConstant(t, objectConstant))
                .collect(ImmutableSet.toImmutableSet());
    }

    protected Optional<IQ> getDefinitionCompatibleWithConstant(RDFAtomPredicate rdfAtomPredicate,
                                                               Mapping.RDFAtomIndexPattern indexPattern,
                                                               ObjectConstant objectConstant) {
        ImmutableSet<ObjectStringTemplateFunctionSymbol> compatibleTemplates = selectCompatibleTemplatesWithConstant(objectConstant);

        // NB: restricting to one template would lose the definitions of the other compatible ones
        if (compatibleTemplates.size() == 1)
            return mapping.getCompatibleDefinitions(rdfAtomPredicate, indexPattern,
                    compatibleTemplates.iterator().next(), variableGenerator);

        return indexPattern == SUBJECT_OF_ALL_CLASSES
                ? mapping.getMergedClassDefinitions(rdfAtomPredicate)
                : mapping.getMergedDefinitions(rdfAtomPredicate);
    }
}
