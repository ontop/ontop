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
import java.util.function.Function;
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
        private final ImmutableTerm template;

        ExpansionPosition(MappingAssertion assertion, Function<ImmutableList<ImmutableTerm>, ImmutableTerm> termExtractor) {
            this.assertion = assertion;
            this.template = termExtractor.apply(assertion.getTerms());
            this.topVariable = (Variable) termExtractor.apply((ImmutableList)assertion.getProjectionAtom().getArguments());
        }

        NativeNode getDatabaseQuery(DBParameters dbParameters) {
            System.out.println("START WITH " + assertion.getQuery());
            IQTree tree = iqFactory.createUnaryIQTree(iqFactory.createDistinctNode(),
                    iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(
                            template.getVariableStream()
                                    .collect(ImmutableCollectors.toSet()),
                            substitutionFactory.getSubstitution()),
                            assertion.getTopChild()));

            System.out.println("QQQQ: " + tree);
            NativeNode nativeNode = nativeNodeGenerator.generate(tree, dbParameters);
            System.out.println("MMMP: " + nativeNode.getNativeQueryString());
            return nativeNode;
        }

        MappingAssertion createExpansion(ImmutableMap<Variable, DBConstant> values) {
            System.out.println("VALUES: " + values);
            String stringIri = instantiateTemplate(template, values);
            ImmutableSubstitution<ImmutableTerm> instantiatedSub = assertion.getTopNode().getSubstitution()
                    .composeWith(substitutionFactory.getSubstitution(
                            topVariable,
                            termFactory.getConstantIRI(rdfFactory.createIRI(stringIri))));

            IQTree filterTree = iqFactory.createUnaryIQTree(iqFactory.createFilterNode(
                    termFactory.getConjunction(values.entrySet().stream()
                            .map(e -> termFactory.getNotYetTypedEquality(
                                    e.getKey(),
                                    e.getValue()))).get()),
                    assertion.getTopChild());

            IQTree transformedFilterTree = mappingEqualityTransformer.transform(filterTree);

            IQ iq = iqFactory.createIQ(assertion.getProjectionAtom(),
                    iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(
                            instantiatedSub.getDomain(), instantiatedSub),
                            transformedFilterTree));

            System.out.println("MME: " + iq);
            return assertion.copyOf(iq);
        }

    }

    private Optional<ExpansionPosition> getExpansionPosition(MappingAssertion assertion) {
        RDFAtomPredicate predicate = assertion.getRDFAtomPredicate();

        Function<ImmutableList<ImmutableTerm>, ImmutableTerm> termExtractor =
                predicate.getPropertyIRI(assertion.getTerms())
                        .filter(p -> p.equals(RDF.TYPE))
                        .isPresent()
                        ? predicate::getObject
                        : predicate::getProperty;

        return (!termExtractor.apply(assertion.getTerms()).isGround())
                ? Optional.of(new ExpansionPosition(assertion, termExtractor))
                : Optional.empty();
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
