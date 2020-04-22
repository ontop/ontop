package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.RawQuotedIDFactory;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.exception.MetaMappingExpansionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * SQLPPMapping -> MappingAssertion
 */
public class SQLPPMappingConverterImpl implements SQLPPMappingConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLPPMappingConverterImpl.class);

    private final TermFactory termFactory;
    private final NoNullValueEnforcer noNullValueEnforcer;
    private final IntermediateQueryFactory iqFactory;
    private final AtomFactory atomFactory;
    private final SubstitutionFactory substitutionFactory;
    private final CoreSingletons coreSingletons;
    private final DBTypeFactory dbTypeFactory;

    @Inject
    private SQLPPMappingConverterImpl(NoNullValueEnforcer noNullValueEnforcer,
                                      CoreSingletons coreSingletons) {
        this.termFactory = coreSingletons.getTermFactory();
        this.noNullValueEnforcer = noNullValueEnforcer;
        this.iqFactory = coreSingletons.getIQFactory();
        this.atomFactory = coreSingletons.getAtomFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.coreSingletons = coreSingletons;
        this.dbTypeFactory = coreSingletons.getTypeFactory().getDBTypeFactory();
    }

    @Override
    public ImmutableList<MappingAssertion> convert(ImmutableList<SQLPPTriplesMap> mappingAssertions, MetadataLookup metadataLookup) throws InvalidMappingSourceQueriesException {

        ImmutableList.Builder<MappingAssertion> builder = ImmutableList.builder();

        for (SQLPPTriplesMap mappingAssertion : mappingAssertions) {
            RAExpression re = getRAExpression(mappingAssertion, metadataLookup);
            IQTree tree = IQ2CQ.toIQTree(
                    re.getDataAtoms().stream()
                            .map(iqFactory::createExtensionalDataNode)
                            .collect(ImmutableCollectors.toList()),
                    re.getFilterAtoms().isEmpty()
                            ? Optional.empty()
                            : Optional.of(termFactory.getConjunction(re.getFilterAtoms())),
                    iqFactory);

            ImmutableMap<QuotedID, ImmutableTerm> lookupTable =  re.getAttributes().entrySet().stream()
                    .filter(e -> e.getKey().getRelation() == null)
                    .collect(ImmutableCollectors.toMap(e -> e.getKey().getAttribute(), Map.Entry::getValue));
            Function<Variable, ImmutableTerm> lookup = placeholderLookup(mappingAssertion, metadataLookup.getQuotedIDFactory(), lookupTable);

            for (TargetAtom target : mappingAssertion.getTargetAtoms()) {
                PPMappingAssertionProvenance provenance = mappingAssertion.getMappingAssertionProvenance(target);
                builder.add(convert(target, lookup, provenance, tree));
            }
        }

        ImmutableList<MappingAssertion> list = builder.build();
        LOGGER.debug("Original mapping size: {}", list.size());
        return list;
    }

    public static <T> BiFunction<Map<QuotedID, T>, Variable, T> placeholderResolver(SQLPPTriplesMap triplesMap, QuotedIDFactory idFactory) {
        if (triplesMap instanceof OntopNativeSQLPPTriplesMap) {
            QuotedIDFactory rawIdFactory = new RawQuotedIDFactory(idFactory);
            return (map, placeholder) -> {
                String name = placeholder.getName();
                QuotedID attribute1 = idFactory.createAttributeID(name);
                T item1 = map.get(attribute1);
                if (item1 != null)
                    return item1;

                QuotedID attribute2 = rawIdFactory.createAttributeID(name);
                return map.get(attribute2);
            };
        }
        else
            return (map, placeholder) -> map.get(idFactory.createAttributeID(placeholder.getName()));
    }

    public static <T> Function<Variable, T> placeholderLookup(SQLPPTriplesMap mappingAssertion, QuotedIDFactory idFactory, ImmutableMap<QuotedID, T> lookup) {
        BiFunction<Map<QuotedID, T>, Variable, T> resolver = placeholderResolver(mappingAssertion, idFactory);
        return v -> resolver.apply(lookup, v);
    }


    private MappingAssertion convert(TargetAtom target, Function<Variable, ImmutableTerm> lookup, PPMappingAssertionProvenance provenance, IQTree tree) throws InvalidMappingSourceQueriesException {

        ImmutableSubstitution<ImmutableTerm> sub;
        try {
            sub = substitutionFactory.getSubstitution(
                    target.getSubstitutedTerms().stream()
                            .flatMap(ImmutableTerm::getVariableStream)
                            .distinct()
                            .map(v -> Maps.immutableEntry(v, lookup.apply(v)))
                            .filter(e -> !e.getKey().equals(e.getValue()))
                            .collect(ImmutableCollectors.toMap()));
        }
        catch (NullPointerException e) { // attribute not found, part of resolver
            throw new InvalidMappingSourceQueriesException(target.getSubstitutedTerms().stream()
                    .flatMap(ImmutableTerm::getVariableStream)
                    .distinct()
                    .filter(v ->lookup.apply(v) == null)
                    .map(Variable::getName)
                    .collect(Collectors.joining(", ",
                            "The placeholder(s) ",
                            " in the target do(es) not occur in source query of the mapping assertion\n["
                    + provenance.getProvenanceInfo() + "]")));
        }

        ConstructionNode constructionNode = iqFactory.createConstructionNode(target.getProjectionAtom().getVariables(),
                substitutionFactory.getSubstitution(target.getSubstitution().getImmutableMap().entrySet().stream()
                        .collect(ImmutableCollectors.toMap(Map.Entry::getKey,
                                e -> sub.apply(e.getValue())))));

        IQ iq0 = iqFactory.createIQ(target.getProjectionAtom(),
                iqFactory.createUnaryIQTree(constructionNode, tree));

        IQ iq = noNullValueEnforcer.transform(iq0);

        return new MappingAssertion(iq, provenance);
    }

    private RAExpression getRAExpression(SQLPPTriplesMap mappingAssertion, MetadataLookup metadataLookup) throws InvalidMappingSourceQueriesException {
        String sourceQuery = mappingAssertion.getSourceQuery().getSQL();
        try {
            try {
                SelectQueryParser sqp = new SelectQueryParser(metadataLookup, coreSingletons);
                return sqp.parse(sourceQuery);
            }
            catch (UnsupportedSelectQueryException e) {
                SelectQueryAttributeExtractor sqae = new SelectQueryAttributeExtractor(metadataLookup, termFactory);
                return createParserView(sqae.extract(sourceQuery), sourceQuery);
            }
        }
        catch (InvalidSelectQueryException e) {
            throw new InvalidMappingSourceQueriesException("Error: " + e.getMessage()
                    + " \nProblem location: source query of triplesMap \n["
                    +  mappingAssertion.getTriplesMapProvenance().getProvenanceInfo() + "]");
        }
    }

    private RAExpression createParserView(ImmutableList<QuotedID> attributes,  String sql) {
        ParserViewDefinition view = new ParserViewDefinition(attributes, sql, dbTypeFactory);

        // this is required to preserve the order of the variables
        ImmutableList<Map.Entry<QualifiedAttributeID, Variable>> list = view.getAttributes().stream()
                .map(att -> Maps.immutableEntry(new QualifiedAttributeID(null, att.getID()), termFactory.getVariable(att.getID().getName())))
                .collect(ImmutableCollectors.toList());

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> lookupTable = list.stream()
                .collect(ImmutableCollectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        ImmutableList<Variable> arguments = list.stream()
                .map(Map.Entry::getValue)
                .collect(ImmutableCollectors.toList());

        return new RAExpression(ImmutableList.of(atomFactory.getDataAtom(view.getAtomPredicate(), arguments)),
                ImmutableList.of(), new RAExpressionAttributes(lookupTable, null));
    }

}
