package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.RawQuotedIDFactory;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
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
    public ImmutableList<MappingAssertion> convert(ImmutableList<SQLPPTriplesMap> mappingAssertions, MetadataLookup dbMetadata) throws InvalidMappingSourceQueriesException {

        ImmutableList.Builder<MappingAssertion> builder = ImmutableList.builder();

        List<String> errorMessages = new ArrayList<>();

        for (SQLPPTriplesMap mappingAxiom : mappingAssertions) {
            try {
                String sourceQuery = mappingAxiom.getSourceQuery().getSQL();
                RAExpression re;
                try {
                    SelectQueryParser sqp = new SelectQueryParser(dbMetadata, coreSingletons);
                    re = sqp.parse(sourceQuery);
                }
                catch (UnsupportedSelectQueryException e) {
                    SelectQueryAttributeExtractor sqae = new SelectQueryAttributeExtractor(dbMetadata, termFactory);
                    re = createParserView(sqae.extract(sourceQuery), sourceQuery);
                }

                ImmutableMap<QuotedID, ImmutableTerm> lookupTable =  re.getAttributes().entrySet().stream()
                        .filter(e -> e.getKey().getRelation() == null)
                        .collect(ImmutableCollectors.toMap(e -> e.getKey().getAttribute(), Map.Entry::getValue));;

                IQTree tree = IQ2CQ.toIQTree(
                        re.getDataAtoms().stream()
                                .map(iqFactory::createExtensionalDataNode)
                                .collect(ImmutableCollectors.toList()),
                        re.getFilterAtoms().reverse().stream()
                                .reduce((a, b) -> termFactory.getConjunction(b, a)),
                        iqFactory);

                BiFunction<Map<QuotedID, ImmutableTerm>, Variable, ImmutableTerm> resolver = placeholderResolver(mappingAxiom, dbMetadata.getQuotedIDFactory());

                for (TargetAtom target : mappingAxiom.getTargetAtoms()) {

                    ImmutableSet<Variable> placeholders = target.getSubstitutedTerms().stream()
                            .flatMap(ImmutableTerm::getVariableStream)
                            .collect(ImmutableCollectors.toSet());

                    PPMappingAssertionProvenance provenance = mappingAxiom.getMappingAssertionProvenance(target);
                    try {
                        ImmutableSubstitution<ImmutableTerm> sub = substitutionFactory.getSubstitution(
                                placeholders.stream()
                                    .map(v -> Maps.immutableEntry(v, resolver.apply(lookupTable, v)))
                                    .filter(e -> !e.getKey().equals(e.getValue()))
                                    .collect(ImmutableCollectors.toMap()));

                        ConstructionNode node =  iqFactory.createConstructionNode(target.getProjectionAtom().getVariables(),
                                substitutionFactory.getSubstitution(target.getSubstitution().getImmutableMap().entrySet().stream()
                                        .collect(ImmutableCollectors.toMap(Map.Entry::getKey,
                                                e -> sub.apply(e.getValue())))));

                        IQ iq0 = iqFactory.createIQ(target.getProjectionAtom(), iqFactory.createUnaryIQTree(node, tree));

                        IQ iq = noNullValueEnforcer.transform(iq0);

                        builder.add(new MappingAssertion(iq,  provenance));
                    }
                    catch (NullPointerException e) { // attribute not found, part of resolver
                        errorMessages.add("Error: " + e.getMessage()
                                + " \nProblem location: source query of the mapping assertion \n["
                                + provenance.getProvenanceInfo() + "]");
                    }
                }
            }
            catch (InvalidSelectQueryException e) {
                errorMessages.add("Error: " + e.getMessage()
                        + " \nProblem location: source query of triplesMap \n["
                        +  mappingAxiom.getTriplesMapProvenance().getProvenanceInfo() + "]");
            }
        }

        if (!errorMessages.isEmpty())
            throw new InvalidMappingSourceQueriesException(Joiner.on("\n\n").join(errorMessages));

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
