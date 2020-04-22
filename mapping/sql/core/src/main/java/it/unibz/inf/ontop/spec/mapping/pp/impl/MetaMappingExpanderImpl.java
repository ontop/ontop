package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.exception.MetaMappingExpansionException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.generation.algebra.IQTree2SelectFromWhereConverter;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.injection.OptimizerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.PostProcessableFunctionLifter;
import it.unibz.inf.ontop.iq.optimizer.TermTypeTermLifter;
import it.unibz.inf.ontop.iq.transformer.BooleanExpressionPushDownTransformer;
import it.unibz.inf.ontop.iq.type.UniqueTermTypeExtractor;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.NullRejectingDBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.RDFTermTypeConstantImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingEqualityTransformer;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.LocalJDBCConnectionUtils;
import it.unibz.inf.ontop.utils.Templates;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MetaMappingExpanderImpl implements MetaMappingExpander {

    private final OptimizerFactory optimizerFactory;
    private final SelectFromWhereSerializer serializer;
    private final IQTree2SelectFromWhereConverter converter;
    private final BooleanExpressionPushDownTransformer pushDownTransformer;
    private final TermTypeTermLifter rdfTypeLifter;
    private final PostProcessableFunctionLifter functionLifter;
    private final SubstitutionFactory substitutionFactory;
    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final org.apache.commons.rdf.api.RDF rdfFactory;
    private final AtomFactory atomFactory;
    private final MappingEqualityTransformer mappingEqualityTransformer;
    private final UniqueTermTypeExtractor uniqueTermTypeExtractor;

    @Inject
    private MetaMappingExpanderImpl(OptimizerFactory optimizerFactory, SelectFromWhereSerializer serializer, IQTree2SelectFromWhereConverter converter, BooleanExpressionPushDownTransformer pushDownTransformer, TermTypeTermLifter rdfTypeLifter, PostProcessableFunctionLifter functionLifter, MappingEqualityTransformer mappingEqualityTransformer, SubstitutionFactory substitutionFactory, IntermediateQueryFactory iqFactory, TermFactory termFactory, org.apache.commons.rdf.api.RDF rdfFactory, AtomFactory atomFactory, MappingEqualityTransformer mappingEqualityTransformer1, UniqueTermTypeExtractor uniqueTermTypeExtractor) {
        this.optimizerFactory = optimizerFactory;
        this.serializer = serializer;
        this.converter = converter;
        this.pushDownTransformer = pushDownTransformer;
        this.rdfTypeLifter = rdfTypeLifter;
        this.functionLifter = functionLifter;
        this.substitutionFactory = substitutionFactory;
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.rdfFactory = rdfFactory;
        this.atomFactory = atomFactory;
        this.mappingEqualityTransformer = mappingEqualityTransformer1;
        this.uniqueTermTypeExtractor = uniqueTermTypeExtractor;
    }


    private static final class Expansion {
        private final MappingAssertion assertion;
        private final Variable templateVariable;
        private final ImmutableFunctionalTerm templateTerm;

        Expansion(MappingAssertion assertion, Function<ImmutableList<ImmutableTerm>, ImmutableTerm> getTerm) {
            this.assertion = assertion;
            //DistinctVariableOnlyDataAtom target = assertion.getQuery().getProjectionAtom();
            this.templateTerm = (ImmutableFunctionalTerm)getTerm.apply(assertion.getTerms());
            this.templateVariable = (Variable) getTerm.apply((ImmutableList)assertion.getProjectionAtom().getArguments());
        }
    }

    private Optional<Expansion> getExpansion(MappingAssertion assertion) {
        RDFAtomPredicate predicate = assertion.getRDFAtomPredicate();

        Function<ImmutableList<ImmutableTerm>, ImmutableTerm> getTerm =
                predicate.getPropertyIRI(assertion.getTerms())
                        .filter(p -> p.equals(RDF.TYPE))
                        .isPresent()
                        ? predicate::getObject
                        : predicate::getProperty;

        return (!getTerm.apply(assertion.getTerms()).isGround())
                ? Optional.of(new Expansion(assertion, getTerm))
                : Optional.empty();
    }


    @Override
    public ImmutableList<MappingAssertion> transform(ImmutableList<MappingAssertion> mappings, OntopSQLCredentialSettings settings, DBParameters dbParameters) throws MetaMappingExpansionException {
        ImmutableList.Builder<MappingAssertion> result = ImmutableList.builder();
        ImmutableList.Builder<Expansion> builder2 = ImmutableList.builder();

        for (MappingAssertion mapping : mappings) {

            // search for non-ground elements in the target atom of each mapping (in class or property positions)
            Optional<Expansion> nonGroundCPs = getExpansion(mapping);
            if (!nonGroundCPs.isPresent()) {
                result.add(mapping);
            }
            else {
                builder2.add(nonGroundCPs.get());
            }
        }
        ImmutableList<Expansion> expansions = builder2.build();

        if (expansions.isEmpty())
            return mappings;

        List<String> errorMessages = new LinkedList<>();
        try (Connection connection = LocalJDBCConnectionUtils.createConnection(settings)) {
            for (Expansion m : expansions) {
                try {
                    System.out.println("START WITH " + m.assertion.getQuery());
                    ImmutableList<Variable> templateVariables = m.templateTerm.getVariableStream()
                            .distinct()
                            .collect(ImmutableCollectors.toList());

                    IQ query = getTemplateValuesQuery(m.assertion, templateVariables);
                    System.out.println("QUERY " + query);
                    SelectFromWhereSerializer.QuerySerialization sqlQuery = translateToSQL(query, dbParameters);
                    final int size = templateVariables.size();
                    try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sqlQuery.getString())) {
                        while (rs.next()) {
                            ImmutableMap.Builder<Variable, DBConstant> builder = ImmutableMap.builder();
                            for (int i = 0; i < size; i++) { // exceptions, no streams
                                Variable variable = templateVariables.get(i);
                                String column = sqlQuery.getColumnIDs().get(variable).getAttribute().getName();
                                builder.put(variable, termFactory.getDBStringConstant(rs.getString(column)));
                            }
                            ImmutableMap<Variable, DBConstant> values = builder.build();

                            System.out.println("VALUES: " + values);

                            IRIConstant predicateTerm = termFactory.getConstantIRI(rdfFactory.createIRI(
                                    getPredicateName(m.templateTerm, values)));

                            ImmutableSubstitution<ImmutableTerm> newSubstitution = m.assertion.getTopNode().getSubstitution()
                                    .composeWith(substitutionFactory.getSubstitution(m.templateVariable, predicateTerm));

                            IQ newIq = iqFactory.createIQ(m.assertion.getProjectionAtom(),
                                    iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(
                                            m.assertion.getTopNode().getVariables(), newSubstitution),
                                            iqFactory.createUnaryIQTree(iqFactory.createFilterNode(
                                                    termFactory.getConjunction(values.entrySet().stream()
                                                            .map(e -> termFactory.getNotYetTypedEquality(
                                                                    e.getKey(),
                                                                    e.getValue()))).get()),
                                            m.assertion.getTopChild())));

                            // TODO: see how to keep the provenance
                            MappingAssertion expandedAssertion = m.assertion.copyOf(newIq);

                            System.out.println("MME: " + expandedAssertion.getQuery());

                            result.addAll(mappingEqualityTransformer.transform(ImmutableList.of(expandedAssertion)));
                        }
                    }
                }
                catch (Exception e) {
                    errorMessages.add(e.getMessage());
                }
            }
        }
        catch (SQLException e) {
            errorMessages.add(e.getMessage());
        }

        if (!errorMessages.isEmpty())
            throw new MetaMappingExpansionException(Joiner.on("\n").join(errorMessages));

        return result.build();
    }


    /**
     * 	does not remove duplicates
     */
    private static Stream<Variable> getVariablePositionsStream(ImmutableTerm t) {
        if (t instanceof Variable)
            return Stream.of((Variable) t);
        if (t instanceof ImmutableFunctionalTerm)
            return ((ImmutableFunctionalTerm) t).getTerms().stream()
                    .flatMap(MetaMappingExpanderImpl::getVariablePositionsStream);
        return Stream.empty();
    }


    private static String getPredicateName(ImmutableTerm term, ImmutableMap<Variable, DBConstant> values) {
        System.out.println("TTT " + term + " " + term.getClass().getName());
        if (term instanceof Variable) {
            return values.get(term).getValue();
        }
        else if (term instanceof Constant) {
            return ((Constant) term).getValue();
        }
        else if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm function = (ImmutableFunctionalTerm) term;
            FunctionSymbol functionSymbol = function.getFunctionSymbol();
            System.out.println("FFF " + functionSymbol + " " + functionSymbol.getClass().getName());
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
                return getPredicateName(function.getTerm(0), values);
            }
            else if (functionSymbol instanceof RDFTermFunctionSymbol) {
                return getPredicateName(function.getTerm(0), values);
            }
            else if (functionSymbol instanceof DBConcatFunctionSymbol) {
                return function.getTerms().stream()
                        .map(t -> getPredicateName(t, values))
                        .collect(Collectors.joining());
            }
        }
        throw new MinorOntopInternalBugException("Unexpected lexical template term: " + term);
    }

    private IQ getTemplateValuesQuery(MappingAssertion assertion, ImmutableList<Variable> templateVariables) {
        return iqFactory.createIQ(atomFactory.getDistinctVariableOnlyDataAtom(
                atomFactory.getRDFAnswerPredicate(templateVariables.size()), templateVariables),
                iqFactory.createUnaryIQTree(iqFactory.createDistinctNode(),
                iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(
                        ImmutableSet.copyOf(templateVariables), substitutionFactory.getSubstitution()),
                        assertion.getTopChild())));
    }

    private SelectFromWhereSerializer.QuerySerialization translateToSQL(IQ query, DBParameters dbParameters)
    {
        VariableGenerator variableGenerator = query.getVariableGenerator();
        IQ rdfTypeLiftedIQ = rdfTypeLifter.optimize(query);
        IQ liftedIQ = functionLifter.optimize(rdfTypeLiftedIQ);
        IQTree flattenSubTree = liftedIQ.getTree(); //.getChildren().get(0);
        IQTree pushedDownSubTree = pushDownTransformer.transform(flattenSubTree);
        IQTree tree = optimizerFactory.createEETransformer(variableGenerator).transform(pushedDownSubTree);

        System.out.println("TREEEE: " + tree);

        ImmutableSortedSet<Variable> signature = ImmutableSortedSet.copyOf(tree.getVariables());
        SelectFromWhereWithModifiers selectFromWhere = converter.convert(tree, signature);
        SelectFromWhereSerializer.QuerySerialization serializedQuery = serializer.serialize(selectFromWhere, dbParameters);
        System.out.println("MMMP: " + serializedQuery.getString());
        return serializedQuery;
    }



}
