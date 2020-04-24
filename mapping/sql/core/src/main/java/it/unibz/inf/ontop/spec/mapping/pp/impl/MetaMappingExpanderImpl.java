package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.exception.MetaMappingExpansionException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.NativeNode;
import it.unibz.inf.ontop.iq.transform.IQTree2NativeNodeGenerator;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.vocabulary.Ontop;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingEqualityTransformer;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.LocalJDBCConnectionUtils;
import it.unibz.inf.ontop.utils.Templates;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetaMappingExpanderImpl implements MetaMappingExpander {

    private final SubstitutionFactory substitutionFactory;
    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final org.apache.commons.rdf.api.RDF rdfFactory;
    private final MappingEqualityTransformer mappingEqualityTransformer;
    private final IQTree2NativeNodeGenerator nativeNodeGenerator;

    @Inject
    private MetaMappingExpanderImpl(SubstitutionFactory substitutionFactory,
                                    IntermediateQueryFactory iqFactory,
                                    TermFactory termFactory,
                                    org.apache.commons.rdf.api.RDF rdfFactory,
                                    MappingEqualityTransformer mappingEqualityTransformer,
                                    IQTree2NativeNodeGenerator nativeNodeGenerator) {
        this.substitutionFactory = substitutionFactory;
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.rdfFactory = rdfFactory;
        this.mappingEqualityTransformer = mappingEqualityTransformer;
        this.nativeNodeGenerator = nativeNodeGenerator;
    }

    @Override
    public ImmutableList<MappingAssertion> transform(ImmutableList<MappingAssertion> mapping, OntopSQLCredentialSettings settings, DBParameters dbParameters) throws MetaMappingExpansionException {
        ImmutableList.Builder<MappingAssertion> resultBuilder = ImmutableList.builder();
        ImmutableList.Builder<ExpansionPosition> positionsBuilder = ImmutableList.builder();

        for (MappingAssertion assertion : mapping) {
            Optional<ExpansionPosition> position = getExpansionPosition(assertion);
            if (!position.isPresent())
                resultBuilder.add(assertion);
            else
                positionsBuilder.add(position.get());
        }

        ImmutableList<ExpansionPosition> positions = positionsBuilder.build();
        if (positions.isEmpty())
            return mapping;

        try (Connection connection = LocalJDBCConnectionUtils.createConnection(settings)) {
            for (ExpansionPosition position : positions) {
                NativeNode nativeNode = position.getDatabaseQuery(dbParameters);
                try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(nativeNode.getNativeQueryString())) {
                    while (rs.next()) {
                        ImmutableMap.Builder<Variable, DBConstant> builder = ImmutableMap.builder();
                        for (Variable variable : nativeNode.getVariables()) { // exceptions, no streams
                            String column = nativeNode.getColumnNames().get(variable).getName();
                            builder.put(variable,
                                    termFactory.getDBConstant(rs.getString(column),
                                            nativeNode.getTypeMap().get(variable)));
                        }
                        resultBuilder.add(position.createExpansion(builder.build()));
                    }
                }
            }
        }
        catch (Exception e) {
            throw new MetaMappingExpansionException(e);
        }

        return resultBuilder.build();
    }

    private final class ExpansionPosition {
        private final MappingAssertion assertion;
        private final Variable topVariable;

        ExpansionPosition(MappingAssertion assertion, Variable topVariable) {
            this.assertion = assertion;
            this.topVariable = topVariable;
        }

        ImmutableTerm getTemplate() {
            return assertion.getTopSubstitution().get(topVariable);
        }

        NativeNode getDatabaseQuery(DBParameters dbParameters) {

            IQTree topChildNotNull = termFactory.getConjunction(assertion.getTopChild().getVariables().stream()
                    .map(termFactory::getDBIsNotNull))
                    .map(iqFactory::createFilterNode)
                    .map(n -> (IQTree)iqFactory.createUnaryIQTree(n, assertion.getTopChild()))
                    .orElse(assertion.getTopChild());

            IQTree constructionTree = iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(
                    getTemplate().getVariableStream().collect(ImmutableCollectors.toSet()),
                    substitutionFactory.getSubstitution()),
                    topChildNotNull);

            IQTree tree = iqFactory.createUnaryIQTree(iqFactory.createDistinctNode(), constructionTree);

            IQTree transformedTree = mappingEqualityTransformer.transform(tree);
            return nativeNodeGenerator.generate(transformedTree, dbParameters);
        }

        MappingAssertion createExpansion(ImmutableMap<Variable, DBConstant> values) {
            String stringIri = instantiateTemplate(getTemplate(), values);
            ImmutableSubstitution<ImmutableTerm> instantiatedSub = assertion.getTopSubstitution()
                    .composeWith(substitutionFactory.getSubstitution(
                            topVariable,
                            termFactory.getConstantIRI(rdfFactory.createIRI(stringIri))));

            IQTree filterTree = termFactory.getConjunction(values.entrySet().stream()
                    .map(e -> termFactory.getNotYetTypedEquality(
                            e.getKey(),
                            e.getValue())))
                    .map(iqFactory::createFilterNode)
                    .map(n -> iqFactory.createUnaryIQTree(n, assertion.getTopChild()))
                    .orElseThrow(() -> new MinorOntopInternalBugException("The generated filter condition is empty for " + assertion + " with " + values));

            IQTree tree = iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(
                            instantiatedSub.getDomain(), instantiatedSub),
                            filterTree);

            return assertion.copyOf(tree, iqFactory);
        }

    }

    private Optional<ExpansionPosition> getExpansionPosition(MappingAssertion assertion) {
        RDFAtomPredicate predicate = assertion.getRDFAtomPredicate();

        RDFAtomPredicate.ComponentGetter componentGetter = predicate.getPropertyIRI(assertion.getTerms())
                        .filter(p -> p.equals(RDF.TYPE))
                        .isPresent()
                        ? predicate::getObject
                        : predicate::getProperty;

        return (componentGetter.get(assertion.getTerms()).isGround())
                ? Optional.empty()
                : Optional.of(new ExpansionPosition(assertion,
                        componentGetter.get(assertion.getProjectionAtom().getArguments())));
    }



    /**
     * 	preserves order and duplicates
     */
    private static Stream<Variable> getVariablePositionsStream(ImmutableTerm term) {
        if (term instanceof Variable)
            return Stream.of((Variable) term);
        if (term instanceof ImmutableFunctionalTerm)
            return ((ImmutableFunctionalTerm) term).getTerms().stream()
                    .flatMap(MetaMappingExpanderImpl::getVariablePositionsStream);
        return Stream.empty();
    }


    private static String instantiateTemplate(ImmutableTerm term, ImmutableMap<Variable, DBConstant> values) {
        if (term instanceof Variable) {
            return values.get(term).getValue();
        }
        else if (term instanceof Constant) {
            return ((Constant) term).getValue();
        }
        else if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm function = (ImmutableFunctionalTerm) term;
            FunctionSymbol functionSymbol = function.getFunctionSymbol();
            if (functionSymbol instanceof ObjectStringTemplateFunctionSymbol) {
                String iriTemplate = ((ObjectStringTemplateFunctionSymbol)functionSymbol).getTemplate();
                return Templates.format(iriTemplate,
                        getVariablePositionsStream(function)
                        .map(values::get)
                        .map(Constant::getValue)
                        .collect(ImmutableCollectors.toList()));
            }
            else if ((functionSymbol instanceof DBTypeConversionFunctionSymbol)
                    && ((DBTypeConversionFunctionSymbol) functionSymbol).isTemporary()) {
                return instantiateTemplate(function.getTerm(0), values);
            }
            else if (functionSymbol instanceof RDFTermFunctionSymbol) {
                return instantiateTemplate(function.getTerm(0), values);
            }
            else if (functionSymbol instanceof DBConcatFunctionSymbol) {
                return function.getTerms().stream()
                        .map(t -> instantiateTemplate(t, values))
                        .collect(Collectors.joining());
            }
        }
        throw new MinorOntopInternalBugException("Unexpected lexical template term: " + term);
    }
}
