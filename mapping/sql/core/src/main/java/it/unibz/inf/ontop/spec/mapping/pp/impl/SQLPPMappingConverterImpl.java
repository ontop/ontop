package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.RawQuotedIDFactory;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.sqlparser.*;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.Var2VarSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * SQLPPMapping -> MappingAssertion
 */
public class SQLPPMappingConverterImpl implements SQLPPMappingConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLPPMappingConverterImpl.class);

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final SQLQueryParser sqlQueryParser;
    private final RAExpression2IQConverter raExpression2IQConverter;

    @Inject
    private SQLPPMappingConverterImpl(CoreSingletons coreSingletons, SQLQueryParser sqlQueryParser,
                                      RAExpression2IQConverter raExpression2IQConverter) {
        this.iqFactory = coreSingletons.getIQFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.sqlQueryParser = sqlQueryParser;
        this.raExpression2IQConverter = raExpression2IQConverter;
    }

    @Override
    public ImmutableList<MappingAssertion> convert(ImmutableList<SQLPPTriplesMap> mapping, MetadataLookup metadataLookup) throws InvalidMappingSourceQueriesException, MetadataExtractionException {

        ImmutableList.Builder<MappingAssertion> builder = ImmutableList.builder();
        for (SQLPPTriplesMap assertion : mapping) {
            RAExpression re = getRAExpression(assertion, metadataLookup);
            IQTree tree = raExpression2IQConverter.convert(re);

            Function<Variable, Optional<ImmutableTerm>> lookup = placeholderLookup(assertion, metadataLookup.getQuotedIDFactory(), re.getUnqualifiedAttributes());

            for (TargetAtom target : assertion.getTargetAtoms()) {
                PPMappingAssertionProvenance provenance = assertion.getMappingAssertionProvenance(target);
                builder.add(convert(target, lookup, provenance, tree));
            }
        }

        ImmutableList<MappingAssertion> result = builder.build();
        LOGGER.debug("Original mapping size: {}", result.size());
        return result;
    }


    private static <T> Function<Variable, Optional<T>> placeholderLookup(SQLPPTriplesMap mappingAssertion, QuotedIDFactory idFactory, ImmutableMap<QuotedID, T> lookup) {
        Function<Variable, Optional<T>> standard =
                v -> Optional.ofNullable(lookup.get(idFactory.createAttributeID(v.getName())));

        if (mappingAssertion instanceof OntopNativeSQLPPTriplesMap) {
            QuotedIDFactory rawIdFactory = new RawQuotedIDFactory(idFactory);
            return v -> Optional.ofNullable(standard.apply(v)
                            .orElse(lookup.get(rawIdFactory.createAttributeID(v.getName()))));
        }
        else
            return standard;
    }


    private MappingAssertion convert(TargetAtom target, Function<Variable, Optional<ImmutableTerm>> lookup, PPMappingAssertionProvenance provenance, IQTree tree) throws InvalidMappingSourceQueriesException {

        ImmutableMap<Variable, Optional<ImmutableTerm>> targetPreMap = target.getProjectionAtom().getArguments().stream()
                    .map(v -> target.getSubstitution().applyToVariable(v))
                    .flatMap(ImmutableTerm::getVariableStream)
                    .distinct()
                    .collect(ImmutableCollectors.toMap(Function.identity(), lookup));

        if (targetPreMap.values().stream().anyMatch(t -> !t.isPresent()))
            throw new InvalidMappingSourceQueriesException(targetPreMap.entrySet().stream()
                    .filter(e -> !e.getValue().isPresent())
                    .map(Map.Entry::getKey)
                    .map(Variable::getName)
                    .collect(Collectors.joining(", ",
                            "The placeholder(s) ",
                            " in the target do(es) not occur in source query of the mapping assertion\n["
                                    + provenance.getProvenanceInfo() + "]")));

        ImmutableMap<Variable, ImmutableTerm> targetMap = targetPreMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(Map.Entry::getKey, e -> e.getValue().orElseThrow(() -> new MinorOntopInternalBugException("Impossible"))));

        Var2VarSubstitution targetRenamingPart = substitutionFactory.getVar2VarSubstitution(targetMap.entrySet().stream()
                .filter(e -> e.getValue() instanceof Variable) // (NON-INJECTIVE)
                .filter(e -> !e.getValue().equals(e.getKey()))
                .map(e -> Maps.immutableEntry(e.getKey(), (Variable)e.getValue()))
                .collect(ImmutableCollectors.toMap()));

        ImmutableSubstitution<ImmutableTerm> spoSubstitution = target.getSubstitution().transform(targetRenamingPart::apply);

        ImmutableSubstitution<ImmutableTerm> selectSubstitution = substitutionFactory.getSubstitution(
                targetMap.entrySet().stream() // getNonVariableFragment
                        .filter(e -> !(e.getValue() instanceof Variable))
                        .collect(ImmutableCollectors.toMap()));

        IQTree selectTree = iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(spoSubstitution.getImmutableMap().values().stream()
                        .flatMap(ImmutableTerm::getVariableStream).collect(ImmutableCollectors.toSet()), selectSubstitution),
                tree);

        IQTree mappingTree = iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(
                target.getProjectionAtom().getVariables(), spoSubstitution), selectTree);

        return new MappingAssertion(iqFactory.createIQ(target.getProjectionAtom(), mappingTree), provenance);
    }

    public RAExpression getRAExpression(SQLPPTriplesMap mappingAssertion, MetadataLookup metadataLookup) throws InvalidMappingSourceQueriesException, MetadataExtractionException {
        String sourceQuery = mappingAssertion.getSourceQuery().getSQL();
        try {
            return sqlQueryParser.getRAExpression(sourceQuery, metadataLookup);
        }
        catch (InvalidQueryException e) {
            throw new InvalidMappingSourceQueriesException("Error: " + e.getMessage()
                    + " \nProblem location: source query of triplesMap \n["
                    +  mappingAssertion.getTriplesMapProvenance().getProvenanceInfo() + "]");
        }
    }
}
