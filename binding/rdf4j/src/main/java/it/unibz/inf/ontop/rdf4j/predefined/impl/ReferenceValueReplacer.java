package it.unibz.inf.ontop.rdf4j.predefined.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.visit.impl.IQStateDefaultTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Singleton
public class ReferenceValueReplacer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceValueReplacer.class);
    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    protected ReferenceValueReplacer(IntermediateQueryFactory iqFactory, TermFactory termFactory, IQTreeTools iqTreeTools) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.iqTreeTools = iqTreeTools;
    }

    /**
     * ASSUMPTION: the query is possibly only composed of construction nodes and native nodes
     */
    public IQ replaceReferenceValues(IQ referenceIq, ImmutableMap<String, String> bindings,
                                     ImmutableMap<String, String> bindingWithReferences) {

        ImmutableMap<String, String> referenceToInputMap = bindings.entrySet().stream()
                .filter(e -> bindingWithReferences.containsKey(e.getKey()))
                .filter(e -> !e.getValue().equals(bindingWithReferences.get(e.getKey())))
                .collect(ImmutableCollectors.toMap(
                        e -> bindingWithReferences.get(e.getKey()),
                        Map.Entry::getValue));

        if (referenceToInputMap.isEmpty())
            return referenceIq;

        LOGGER.debug("Reference values to be replaced: {}", referenceToInputMap);

        IQTree newTree = referenceIq.getTree().acceptVisitor(new IQStateDefaultTransformer<>() {
            @Override
            protected IQTree done() {
                throw new MinorOntopInternalBugException("Expected only ConstructionNodes, NativeNodes and EmptyNodes");
            }

            @Override
            public IQTree transformConstruction(UnaryIQTree tree1, ConstructionNode node, IQTree child) {
                var newConstructionNode = iqTreeTools.replaceSubstitution(
                        node,
                        node.getSubstitution().transform(t -> transformTerm(t, referenceToInputMap)));

                return iqFactory.createUnaryIQTree(newConstructionNode, child.acceptVisitor(this));
            }

            @Override
            public IQTree transformNative(NativeNode node) {
                String newQueryString = replaceString(node.getNativeQueryString(), referenceToInputMap);

                return iqFactory.createNativeNode(node.getVariables(),
                        node.getTypeMap(), node.getColumnNames(), newQueryString, node.getVariableNullability());
            }

            @Override
            public IQTree transformEmpty(EmptyNode node) {
                return node;
            }
        });

        return iqFactory.createIQ(referenceIq.getProjectionAtom(), newTree);
    }

    private ImmutableTerm transformTerm(ImmutableTerm term, ImmutableMap<String, String> referenceToInputMap) {
        if (term instanceof RDFConstant) {
            RDFConstant constant = (RDFConstant) term;
            String initialValue = constant.getValue();
            String newValue = replaceString(constant.getValue(), referenceToInputMap);
            if (initialValue.equals(newValue))
                return constant;

            return termFactory.getRDFConstant(newValue, constant.getType());
        }
        else if (term instanceof DBConstant) {
            DBConstant constant = (DBConstant) term;
            String initialValue = constant.getValue();
            String newValue = replaceString(constant.getValue(), referenceToInputMap);
            if (initialValue.equals(newValue))
                return constant;

            return termFactory.getDBConstant(newValue, constant.getType());
        }
        else if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) term;

            ImmutableList<? extends ImmutableTerm> initialTerms = functionalTerm.getTerms();

            ImmutableList<ImmutableTerm> newTerms = initialTerms.stream()
                    .map(t -> transformTerm(t, referenceToInputMap))
                    .collect(ImmutableCollectors.toList());

            return initialTerms.equals(newTerms)
                    ? functionalTerm
                    : termFactory.getImmutableFunctionalTerm(functionalTerm.getFunctionSymbol(), newTerms);
        }
        else
            return term;
    }

    private String replaceString(String str, ImmutableMap<String, String> referenceToInputMap) {
        return referenceToInputMap.entrySet().stream()
                .reduce(str, (s, e) -> s.replaceAll(e.getKey(), e.getValue()),
                        (s1, s2) -> {
                            throw new MinorOntopInternalBugException("Not expected to be run in //");
                        });
    }
}
