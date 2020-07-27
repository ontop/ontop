package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.RawQuotedIDFactory;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.sqlparser.*;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.InvalidSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.UnsupportedSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.transformer.impl.IQ2CQ;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.TokenMgrException;
import net.sf.jsqlparser.statement.select.SelectBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * SQLPPMapping -> MappingAssertion
 */
public class SQLPPMappingConverterImpl implements SQLPPMappingConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLPPMappingConverterImpl.class);

    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final CoreSingletons coreSingletons;
    private final DBTypeFactory dbTypeFactory;

    @Inject
    private SQLPPMappingConverterImpl(CoreSingletons coreSingletons) {
        this.termFactory = coreSingletons.getTermFactory();
        this.iqFactory = coreSingletons.getIQFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.coreSingletons = coreSingletons;
        this.dbTypeFactory = coreSingletons.getTypeFactory().getDBTypeFactory();
    }

    @Override
    public ImmutableList<MappingAssertion> convert(ImmutableList<SQLPPTriplesMap> mapping, MetadataLookup metadataLookup) throws InvalidMappingSourceQueriesException {

        ImmutableList.Builder<MappingAssertion> builder = ImmutableList.builder();
        for (SQLPPTriplesMap assertion : mapping) {
            RAExpression re = getRAExpression(assertion, metadataLookup);
            IQTree tree = IQ2CQ.toIQTree(
                    re.getDataAtoms().stream()
                            .collect(ImmutableCollectors.toList()),
                    termFactory.getConjunction(re.getFilterAtoms().stream()),
                    iqFactory);

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
                    .map(v -> target.getSubstitution().apply(v))
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

        ImmutableSubstitution<ImmutableTerm> targetRenamingPart = substitutionFactory.getSubstitution(targetMap.entrySet().stream()
                .filter(e -> e.getValue() instanceof Variable)
                .filter(e -> !e.getValue().equals(e.getKey()))
                .collect(ImmutableCollectors.toMap()));

        ImmutableSubstitution<ImmutableTerm> spoSubstitution = substitutionFactory.getSubstitution(target.getSubstitution().getImmutableMap().entrySet().stream()
                        .collect(ImmutableCollectors.toMap(Map.Entry::getKey,
                                e -> targetRenamingPart.apply(e.getValue()))));

        ImmutableSubstitution<ImmutableTerm> selectSubstitution = substitutionFactory.getSubstitution(
                targetMap.entrySet().stream()
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

    private RAExpression getRAExpression(SQLPPTriplesMap mappingAssertion, MetadataLookup metadataLookup) throws InvalidMappingSourceQueriesException {
        String sourceQuery = mappingAssertion.getSourceQuery().getSQL();
        SelectQueryParser sqp = new SelectQueryParser(metadataLookup, coreSingletons);
        try {
            ImmutableList<QuotedID> attributes;
            try {
                SelectBody selectBody = JSqlParserTools.parse(sourceQuery);
                try {
                    return sqp.parse(selectBody);
                }
                catch (UnsupportedSelectQueryException e) {
                    DefaultSelectQueryAttributeExtractor sqae = new DefaultSelectQueryAttributeExtractor(metadataLookup, coreSingletons);
                    ImmutableMap<QuotedID, ImmutableTerm> attrs = sqae.getRAExpressionAttributes(selectBody).getUnqualifiedAttributes();
                    attributes = ImmutableList.copyOf(attrs.keySet());
                }
            }
            catch (JSQLParserException e) {
                // TODO: LOGGER.warn() should be instead after revising the logging policy
                System.out.println(String.format("FAILED TO PARSE: %s %s", sourceQuery, getJSQLParserErrorMessage(sourceQuery, e)));
                
                ApproximateSelectQueryAttributeExtractor sqae = new ApproximateSelectQueryAttributeExtractor(metadataLookup.getQuotedIDFactory());
                attributes = sqae.getAttributes(sourceQuery);
            }
            catch (UnsupportedSelectQueryException e) {
                ApproximateSelectQueryAttributeExtractor sqae = new ApproximateSelectQueryAttributeExtractor(metadataLookup.getQuotedIDFactory());
                attributes = sqae.getAttributes(sourceQuery);
            }
            // TODO: LOGGER.warn() should be instead after revising the logging policy
            System.out.println("PARSER VIEW FOR " + sourceQuery);
            ParserViewDefinition view = new ParserViewDefinition(attributes, sourceQuery, dbTypeFactory);
            return sqp.translateParserView(view);
        }
        catch (InvalidSelectQueryException e) {
            throw new InvalidMappingSourceQueriesException("Error: " + e.getMessage()
                    + " \nProblem location: source query of triplesMap \n["
                    +  mappingAssertion.getTriplesMapProvenance().getProvenanceInfo() + "]");
        }
    }

    private static String getJSQLParserErrorMessage(String sourceQuery, JSQLParserException e) {
        try {
            // net.sf.jsqlparser.parser.TokenMgrException: Lexical error at line 1, column 165.
            if (e.getCause() instanceof TokenMgrException) {
                Pattern pattern = Pattern.compile("at line (\\d+), column (\\d+)");
                Matcher matcher = pattern.matcher(e.getCause().getMessage());
                if (matcher.find()) {
                    int line = Integer.parseInt(matcher.group(1));
                    int col = Integer.parseInt(matcher.group(2));
                    String sourceQueryLine = sourceQuery.split("\n")[line - 1];
                    final int MAX_LENGTH = 40;
                    if (sourceQueryLine.length() > MAX_LENGTH) {
                        sourceQueryLine = sourceQueryLine.substring(sourceQueryLine.length() - MAX_LENGTH);
                        if (sourceQueryLine.length() > 2 * MAX_LENGTH)
                            sourceQueryLine = sourceQueryLine.substring(0, 2 * MAX_LENGTH);
                        col = MAX_LENGTH;
                    }
                    return "FAILED TO PARSE: " + sourceQueryLine + "\n" +
                            Strings.repeat(" ", "FAILED TO PARSE: ".length() + col - 2) + "^\n" + e.getCause();
                }
            }
        }
        catch (Exception e1) {
            // NOP
        }
        return e.getCause().toString();
    }
}
