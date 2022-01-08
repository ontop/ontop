package it.unibz.inf.ontop.rdf4j.predefined.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.EmptyNode;
import it.unibz.inf.ontop.iq.node.NativeNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Singleton
public class ReferenceValueReplacer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceValueReplacer.class);
    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    protected ReferenceValueReplacer(IntermediateQueryFactory iqFactory, TermFactory termFactory,
                                     SubstitutionFactory substitutionFactory) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
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

        IQTree newTree = transform(referenceIq.getTree(), referenceToInputMap);

        return iqFactory.createIQ(referenceIq.getProjectionAtom(), newTree);
    }

    /**
     * Only applies transformations to construct nodes and native nodes
     */
    private IQTree transform(IQTree tree, ImmutableMap<String, String> referenceToInputMap) {
        QueryNode rootNode = tree.getRootNode();

        if (rootNode instanceof ConstructionNode) {
            return iqFactory.createUnaryIQTree(
                    transformConstructionNode((ConstructionNode) rootNode, referenceToInputMap),
                    transform(((UnaryIQTree) tree).getChild(), referenceToInputMap)
            );
        }
        else if (rootNode instanceof NativeNode) {
            return transformNativeNode((NativeNode) rootNode, referenceToInputMap);
        }
        else if (rootNode instanceof EmptyNode) {
            return tree;
        }
        else
            throw new IllegalArgumentException("Was only expecting construction nodes and native nodes");
    }

    private ConstructionNode transformConstructionNode(ConstructionNode constructionNode,
                                                       ImmutableMap<String, String> referenceToInputMap) {
        ImmutableSubstitution<ImmutableTerm> substitution = constructionNode.getSubstitution();
        if (substitution.isEmpty())
            return constructionNode;

        ImmutableSubstitution<ImmutableTerm> newSubstitution = substitution.transform(v -> transformTerm(v, referenceToInputMap));

        return iqFactory.createConstructionNode(constructionNode.getVariables(), newSubstitution);
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

    private IQTree transformNativeNode(NativeNode nativeNode, ImmutableMap<String, String> referenceToInputMap) {
        String newQueryString = replaceString(nativeNode.getNativeQueryString(), referenceToInputMap);

        return iqFactory.createNativeNode(nativeNode.getVariables(),
                nativeNode.getTypeMap(),
                nativeNode.getColumnNames(),
                newQueryString,
                nativeNode.getVariableNullability());
    }
}
