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
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.InvalidSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.UnsupportedSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.sqlparser.ParserViewDefinition;
import it.unibz.inf.ontop.spec.mapping.sqlparser.RAExpression;
import it.unibz.inf.ontop.spec.mapping.sqlparser.SelectQueryAttributeExtractor;
import it.unibz.inf.ontop.spec.mapping.sqlparser.SelectQueryParser;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.transformer.impl.IQ2CQ;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


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
    public ImmutableList<MappingAssertion> convert(SQLPPMapping ppMapping, MetadataLookup dbMetadata, QuotedIDFactory idFactory,
                                         ExecutorRegistry executorRegistry) throws InvalidMappingSourceQueriesException {

        List<MappingAssertion> assertionsList = new ArrayList<>();

        List<String> errorMessages = new ArrayList<>();

        for (SQLPPTriplesMap mappingAxiom : ppMapping.getTripleMaps()) {
            try {
                String sourceQuery = mappingAxiom.getSourceQuery().getSQL();

                ImmutableList<DataAtom<RelationPredicate>> dataAtoms;
                Optional<ImmutableExpression> filter;
                ImmutableMap<QuotedID, ImmutableTerm> lookupTable;

                try {
                    SelectQueryParser sqp = new SelectQueryParser(dbMetadata, idFactory, coreSingletons);
                    RAExpression re = sqp.parse(sourceQuery);
                    lookupTable = re.getAttributes().entrySet().stream()
                        .filter(e -> e.getKey().getRelation() == null)
                        .collect(ImmutableCollectors.toMap(e -> e.getKey().getAttribute(), Map.Entry::getValue));
                    dataAtoms = re.getDataAtoms();
                    filter = re.getFilterAtoms().reverse().stream()
                                        .reduce((a, b) -> termFactory.getConjunction(b, a));
                }
                catch (UnsupportedSelectQueryException e) {
                    ImmutableList<QuotedID> attributes = new SelectQueryAttributeExtractor(dbMetadata, idFactory, termFactory)
                            .extract(sourceQuery);
                    ParserViewDefinition view = createParserView(dbTypeFactory, sourceQuery, attributes);

                    // this is required to preserve the order of the variables
                    ImmutableList<Map.Entry<QuotedID, Variable>> list = view.getAttributes().stream()
                            .map(att -> Maps.immutableEntry(att.getID(), termFactory.getVariable(att.getID().getName())))
                            .collect(ImmutableCollectors.toList());

                    lookupTable = list.stream().collect(ImmutableCollectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    ImmutableList<Variable> arguments = list.stream().map(Map.Entry::getValue).collect(ImmutableCollectors.toList());

                    dataAtoms = ImmutableList.of(atomFactory.getDataAtom(view.getAtomPredicate(), arguments));
                    filter = Optional.empty();
                }

                final IQTree tree = IQ2CQ.toIQTree(dataAtoms.stream()
                                .map(iqFactory::createExtensionalDataNode)
                                .collect(ImmutableCollectors.toList()),
                        filter, iqFactory);

                for (TargetAtom atom : mappingAxiom.getTargetAtoms()) {
                    PPMappingAssertionProvenance provenance = mappingAxiom.getMappingAssertionProvenance(atom);
                    try {
                        ImmutableMap.Builder<Variable, ImmutableTerm> builder = ImmutableMap.builder();
                        ImmutableList.Builder<Variable> varBuilder2 = ImmutableList.builder();
                        for (Variable v : atom.getProjectionAtom().getArguments()) {
                            ImmutableTerm t = atom.getSubstitution().get(v);
                            if (t != null) {
                                builder.put(v, renameVariables(t, lookupTable, idFactory));
                                varBuilder2.add(v);
                            }
                            else {
                                ImmutableTerm tt = renameVariables(v, lookupTable, idFactory);
                                if (tt instanceof Variable) { // avoids Var -> Var
                                    Variable v2 = (Variable) tt;
                                    varBuilder2.add(v2);
                                }
                                else {
                                    builder.put(v, tt);
                                    varBuilder2.add(v);
                                }
                            }
                        }
                        ImmutableList<Variable> varList = varBuilder2.build();
                        ImmutableSubstitution<ImmutableTerm> substitution = substitutionFactory.getSubstitution(builder.build());

                        IQ iq0 = iqFactory.createIQ(
                                    atomFactory.getDistinctVariableOnlyDataAtom(atom.getProjectionAtom().getPredicate(), varList),
                                    iqFactory.createUnaryIQTree(
                                            iqFactory.createConstructionNode(ImmutableSet.copyOf(varList), substitution), tree));

                        IQ iq = noNullValueEnforcer.transform(iq0).normalizeForOptimization();

                        assertionsList.add(new MappingAssertion(MappingTools.extractRDFPredicate(iq), iq,  provenance));
                        //if (previous != null)
                        //    LOGGER.warn("Redundant triples maps: \n" + provenance + "\n and \n" + previous);
                    }
                    catch (AttributeNotFoundException e) {
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

        LOGGER.debug("Original mapping size: {}", assertionsList.size());

        return ImmutableList.copyOf(assertionsList);
    }



    /**
     * Returns a new function by renaming variables occurring in the {@code function}
     *  according to the {@code attributes} lookup table
     */
    private ImmutableTerm renameVariables(ImmutableTerm term,
                                          ImmutableMap<QuotedID, ImmutableTerm> attributes,
                                          QuotedIDFactory idfac) throws AttributeNotFoundException {

            if (term instanceof Variable) {
                Variable var = (Variable) term;
                QuotedID attribute = idfac.createAttributeID(var.getName());
                ImmutableTerm newTerm = attributes.get(attribute);

                if (newTerm == null) {
                    QuotedIDFactory rawIdFactory = new RawQuotedIDFactory(idfac);
                    QuotedID quotedAttribute = rawIdFactory.createAttributeID(var.getName());
                    newTerm = attributes.get(quotedAttribute);

                    if (newTerm == null)
                        throw new AttributeNotFoundException("The source query does not provide the attribute " + attribute
                                + " (variable " + var.getName() + ") required by the target atom.");
                }
                return newTerm;
            }
            else if (term instanceof ImmutableFunctionalTerm) {
                ImmutableFunctionalTerm f = (ImmutableFunctionalTerm)term;
                ImmutableList.Builder<ImmutableTerm> builder = ImmutableList.builder();
                for (ImmutableTerm t : f.getTerms()) // for exception handling
                    builder.add(renameVariables(t, attributes, idfac));
                return termFactory.getImmutableFunctionalTerm(f.getFunctionSymbol(), builder.build());
            }
            else if (term instanceof Constant)
                return term;
            else
                throw new RuntimeException("Unknown term type: " + term);
    }

    private static class AttributeNotFoundException extends Exception {
        AttributeNotFoundException(String message) {
            super(message);
        }
    }

    private ParserViewDefinition createParserView(DBTypeFactory dbTypeFactory, String sql, ImmutableList<QuotedID> attributes) {
        return new ParserViewDefinition(attributes, sql, dbTypeFactory);
    }

}
