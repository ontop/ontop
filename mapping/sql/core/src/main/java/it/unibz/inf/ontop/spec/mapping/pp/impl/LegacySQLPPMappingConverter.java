package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.datalog.EQNormalizer;
import it.unibz.inf.ontop.datalog.impl.DatalogRule2QueryConverter;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.ProvenanceMappingFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;
import it.unibz.inf.ontop.spec.mapping.parser.exception.InvalidSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.parser.exception.UnsupportedSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.parser.impl.RAExpression;
import it.unibz.inf.ontop.spec.mapping.parser.impl.SelectQueryAttributeExtractor;
import it.unibz.inf.ontop.spec.mapping.parser.impl.SelectQueryParser;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * SQLPPMapping -> Datalog -> MappingWithProvenance
 */
public class LegacySQLPPMappingConverter implements SQLPPMappingConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacySQLPPMappingConverter.class);

    private final TermFactory termFactory;
    private final EQNormalizer eqNormalizer;
    private final ProvenanceMappingFactory provMappingFactory;
    private final NoNullValueEnforcer noNullValueEnforcer;
    private final DatalogRule2QueryConverter datalogRule2QueryConverter;
    private final IntermediateQueryFactory iqFactory;
    private final TypeFactory typeFactory;
    private final DatalogFactory datalogFactory;
    private final ImmutabilityTools immutabilityTools;
    private final AtomFactory atomFactory;

    @Inject
    private LegacySQLPPMappingConverter(TermFactory termFactory,
                                        EQNormalizer eqNormalizer, ProvenanceMappingFactory provMappingFactory, NoNullValueEnforcer noNullValueEnforcer, DatalogRule2QueryConverter datalogRule2QueryConverter, IntermediateQueryFactory iqFactory, TypeFactory typeFactory, DatalogFactory datalogFactory, ImmutabilityTools immutabilityTools, AtomFactory atomFactory) {
        this.termFactory = termFactory;
        this.eqNormalizer = eqNormalizer;
        this.provMappingFactory = provMappingFactory;
        this.noNullValueEnforcer = noNullValueEnforcer;
        this.datalogRule2QueryConverter = datalogRule2QueryConverter;
        this.iqFactory = iqFactory;
        this.typeFactory = typeFactory;
        this.datalogFactory = datalogFactory;
        this.immutabilityTools = immutabilityTools;
        this.atomFactory = atomFactory;
    }

    @Override
    public MappingWithProvenance convert(SQLPPMapping ppMapping, RDBMetadata dbMetadata,
                                         ExecutorRegistry executorRegistry) throws InvalidMappingSourceQueriesException {

        return provMappingFactory.create(convert(ppMapping.getTripleMaps(), dbMetadata), ppMapping.getMetadata());
    }


    /**
     * Normalize language tags (make them lower-case)
     */

    private void normalizeMapping(Function head) {
        for (Term term : head.getTerms()) {
            if (!(term instanceof Function))
                continue;

            Function typedTerm = (Function) term;
            if (typedTerm.getTerms().size() == 2 && typedTerm.getFunctionSymbol().getName().equals(RDF.LANGSTRING.getIRIString())) {
                // changing the language, its always the second inner term (literal,lang)
                Term originalLangTag = typedTerm.getTerm(1);
                if (originalLangTag instanceof ValueConstant) {
                    ValueConstant originalLangConstant = (ValueConstant) originalLangTag;
                    Term normalizedLangTag = termFactory.getConstantLiteral(originalLangConstant.getValue().toLowerCase(),
                            originalLangConstant.getType());
                    typedTerm.setTerm(1, normalizedLangTag);
                }
            }
        }
    }

    /**
     * returns a Datalog representation of the mappings
     *
     * Assumption: one CQIE per mapping axiom (no nested union)
     *
     * May also add views in the DBMetadata!
     */

    public ImmutableMap<IQ, PPMappingAssertionProvenance> convert(Collection<SQLPPTriplesMap> triplesMaps,
                                                                    RDBMetadata metadata) throws InvalidMappingSourceQueriesException {
        Map<IQ, PPMappingAssertionProvenance> mutableMap = new HashMap<>();

        List<String> errorMessages = new ArrayList<>();

        QuotedIDFactory idfac = metadata.getQuotedIDFactory();

        for (SQLPPTriplesMap mappingAxiom : triplesMaps) {
            try {
                OBDASQLQuery sourceQuery = mappingAxiom.getSourceQuery();

                List<Function> body;
                ImmutableMap<QualifiedAttributeID, ImmutableTerm> lookupTable;

                try {
                    SelectQueryParser sqp = new SelectQueryParser(metadata, termFactory, typeFactory, atomFactory);
                    RAExpression re = sqp.parse(sourceQuery.toString());
                    lookupTable = re.getAttributes();

                    body = new ArrayList<>(re.getDataAtoms().size() + re.getFilterAtoms().size());
                    body.addAll(re.getDataAtoms().stream()
                            .map(t -> immutabilityTools.convertToMutableFunction(t))
                            .collect(ImmutableCollectors.toList()));
                    body.addAll(re.getFilterAtoms().stream()
                            .map(t -> immutabilityTools.convertToMutableFunction(t))
                            .collect(ImmutableCollectors.toList()));
                }
                catch (UnsupportedSelectQueryException e) {
                    ImmutableList<QuotedID> attributes = new SelectQueryAttributeExtractor(metadata, termFactory)
                            .extract(sourceQuery.toString());
                    ParserViewDefinition view = metadata.createParserView(sourceQuery.toString(), attributes);

                    // this is required to preserve the order of the variables
                    ImmutableList<Map.Entry<QualifiedAttributeID, Variable>> list = view.getAttributes().stream()
                            .map(att -> new AbstractMap.SimpleEntry<>(
                                    new QualifiedAttributeID(null, att.getID()), // strip off the ParserViewDefinitionName
                                    termFactory.getVariable(att.getID().getName())))
                            .collect(ImmutableCollectors.toList());

                    lookupTable = list.stream().collect(ImmutableCollectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    List<Term> arguments = list.stream().map(Map.Entry::getValue).collect(ImmutableCollectors.toList());

                    body = new ArrayList<>(1);
                    body.add(termFactory.getFunction(view.getAtomPredicate(), arguments));
                }

                for (TargetAtom atom : mappingAxiom.getTargetAtoms()) {
                    PPMappingAssertionProvenance provenance = mappingAxiom.getMappingAssertionProvenance(atom);
                    try {
                        ImmutableList<ImmutableTerm> headTerms = renameVariables(atom.getSubstitutedTerms(), lookupTable, idfac);
                        Function head = immutabilityTools.convertToMutableFunction(atom.getProjectionAtom().getPredicate(), headTerms);
                        normalizeMapping(head); // Normalizing language tags (SIDE-EFFECT!)

                        CQIE rule = datalogFactory.getCQIE(head, body);
                        eqNormalizer.enforceEqualities(rule); // Normalizing equalities (SIDE-EFFECT!)

                        IQ directlyConvertedIQ = datalogRule2QueryConverter.extractPredicatesAndConvertDatalogRule(rule, iqFactory);

                        IQ iq = noNullValueEnforcer.transform(directlyConvertedIQ).liftBinding();

                        PPMappingAssertionProvenance previous = mutableMap.put(iq, provenance);
                        if (previous != null)
                            LOGGER.warn("Redundant triples maps: \n" + provenance + "\n and \n" + previous);
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

        LOGGER.debug("Original mapping size: {}", mutableMap.size());

        return ImmutableMap.copyOf(mutableMap);
    }


    /**
     * Returns a new function by renaming variables occurring in the {@code function}
     *  according to the {@code attributes} lookup table
     */
    private ImmutableList<ImmutableTerm> renameVariables(ImmutableList<? extends ImmutableTerm> terms,
                                                         ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes,
                                                         QuotedIDFactory idfac) throws AttributeNotFoundException {

        ImmutableList.Builder<ImmutableTerm> builder = ImmutableList.builder();

        for (ImmutableTerm term : terms) {
            if (term instanceof Variable) {
                Variable var = (Variable) term;
                QuotedID attribute = idfac.createAttributeID(var.getName());
                ImmutableTerm newTerm = attributes.get(new QualifiedAttributeID(null, attribute));

                if (newTerm == null) {
                    QuotedID quotedAttribute = QuotedID.createIdFromDatabaseRecord(idfac, var.getName());
                    newTerm = attributes.get(new QualifiedAttributeID(null, quotedAttribute));

                    if (newTerm == null)
                        throw new AttributeNotFoundException("The source query does not provide the attribute " + attribute
                                + " (variable " + var.getName() + ") required by the target atom.");
                }
                builder.add(newTerm);
            }
            else if (term instanceof ImmutableFunctionalTerm) {
                ImmutableFunctionalTerm f = (ImmutableFunctionalTerm)term;
                builder.add(termFactory.getImmutableFunctionalTerm(f.getFunctionSymbol(), renameVariables(f.getTerms(), attributes, idfac)));
            }
            else if (term instanceof Constant)
                builder.add(term);
            else
                throw new RuntimeException("Unknown term type: " + term);
        }

        return builder.build();
    }


    private static class AttributeNotFoundException extends Exception {
        AttributeNotFoundException(String message) {
            super(message);
        }
    }

}
