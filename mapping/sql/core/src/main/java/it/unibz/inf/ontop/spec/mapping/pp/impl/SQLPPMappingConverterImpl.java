package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.RawQuotedIDFactory;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopOBDASettings;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.sqlparser.*;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
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

    private final boolean ignoreInvalidMappingEntries;

    @Inject
    private SQLPPMappingConverterImpl(CoreSingletons coreSingletons, SQLQueryParser sqlQueryParser) {
        this.iqFactory = coreSingletons.getIQFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.sqlQueryParser = sqlQueryParser;

        ignoreInvalidMappingEntries = ((OntopOBDASettings)coreSingletons.getSettings()).ignoreInvalidMappingEntries();

    }

    @Override
    public ImmutableList<MappingAssertion> convert(ImmutableList<SQLPPTriplesMap> mapping, MetadataLookup metadataLookup) throws InvalidMappingSourceQueriesException, MetadataExtractionException {
        ImmutableList.Builder<MappingAssertion> builder = ImmutableList.builder();
        for (SQLPPTriplesMap assertion : mapping) {

            try {
                RAExpression re = getRAExpression(assertion, metadataLookup);
                IQTree tree = sqlQueryParser.convert(re);

                Function<Variable, Optional<ImmutableTerm>> lookup = placeholderLookup(assertion, metadataLookup.getQuotedIDFactory(), re.getUnqualifiedAttributes());

                for (TargetAtom target : assertion.getTargetAtoms()) {
                    try {
                        PPMappingAssertionProvenance provenance = assertion.getMappingAssertionProvenance(target);
                        builder.add(convert(target, lookup, provenance, tree));
                    } catch (InvalidMappingSourceQueriesException e) {
                        if(!ignoreInvalidMappingEntries)
                            throw e;
                        LOGGER.warn("Target atom {} was ignored due to an issue: {}", target.toString(), e.getMessage());
                    }
                }
            }
            /*
             * NB: runtime exceptions are also caught due to some JDBC drivers throwing them instead of SQLException-s
             */
            catch(InvalidMappingSourceQueriesException | MetadataExtractionException | RuntimeException e) {
                if(!ignoreInvalidMappingEntries)
                    throw e;
                LOGGER.warn("Mapping entry {} was ignored due to an issue: {}", assertion.getId(), e.getMessage());
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

        Substitution<ImmutableTerm> targetSubstitution = target.getSubstitution();

        ImmutableMap<Variable, Optional<ImmutableTerm>> targetPreMap =
                targetSubstitution.apply(target.getProjectionAtom().getArguments()).stream()
                        .flatMap(ImmutableTerm::getVariableStream)
                        .distinct()
                        .collect(ImmutableCollectors.toMap(v -> v, lookup));

        ImmutableList<String> missingPlaceholders = targetPreMap.entrySet().stream()
                .filter(e -> e.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .map(Variable::getName)
                .collect(ImmutableCollectors.toList());

        if (!missingPlaceholders.isEmpty())
            throw new InvalidMappingSourceQueriesException(missingPlaceholders.stream()
                    .collect(Collectors.joining(", ",
                            "The placeholder(s) ",
                            " in the target do(es) not occur in source query of the mapping assertion\n["
                                    + provenance.getProvenanceInfo() + "]")));

        //noinspection OptionalGetWithoutIsPresent
        Substitution<ImmutableTerm> substitution = targetPreMap.entrySet().stream()
                .collect(substitutionFactory.toSubstitutionSkippingIdentityEntries(Map.Entry::getKey, e -> e.getValue().get()));

        Substitution<Variable> targetRenamingPart = substitution.restrictRangeTo(Variable.class);
        Substitution<ImmutableTerm> spoSubstitution = targetSubstitution.transform(targetRenamingPart::applyToTerm);

        Substitution<? extends ImmutableTerm> selectSubstitution = substitution.restrictRangeTo(NonVariableTerm.class);

        IQTree selectTree = iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(spoSubstitution.getRangeVariables(), selectSubstitution),
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
