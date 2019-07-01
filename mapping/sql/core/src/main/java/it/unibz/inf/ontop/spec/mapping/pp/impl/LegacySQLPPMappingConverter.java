package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.OperationPredicate;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.parser.exception.InvalidSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.parser.exception.UnsupportedSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.parser.impl.RAExpression;
import it.unibz.inf.ontop.spec.mapping.parser.impl.SelectQueryAttributeExtractor;
import it.unibz.inf.ontop.spec.mapping.parser.impl.SelectQueryParser;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private LegacySQLPPMappingConverter(TermFactory termFactory,
                                        EQNormalizer eqNormalizer, ProvenanceMappingFactory provMappingFactory, NoNullValueEnforcer noNullValueEnforcer, DatalogRule2QueryConverter datalogRule2QueryConverter, IntermediateQueryFactory iqFactory, TypeFactory typeFactory, DatalogFactory datalogFactory, ImmutabilityTools immutabilityTools, AtomFactory atomFactory, SubstitutionFactory substitutionFactory) {
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
        this.substitutionFactory = substitutionFactory;
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
                String sourceQuery = mappingAxiom.getSourceQuery().getSQLQuery();

                ImmutableList<DataAtom<RelationPredicate>> dataAtoms;
                ImmutableList<ImmutableExpression> filters;
                ImmutableMap<QualifiedAttributeID, ImmutableTerm> lookupTable;

                try {
                    SelectQueryParser sqp = new SelectQueryParser(metadata, termFactory, typeFactory, atomFactory);
                    RAExpression re = sqp.parse(sourceQuery);
                    //lookupTable = re.getAttributes();

                    //dataAtoms = re.getDataAtoms();
                    //filters = re.getFilterAtoms();

                    ImmutableMap.Builder<Variable, VariableOrGroundTerm> s = ImmutableMap.builder();
                    ImmutableList.Builder<ImmutableExpression> builder = ImmutableList.builder();

                    for (ImmutableExpression e : re.getFilterAtoms())
                        if (e.getFunctionSymbol() == ExpressionOperation.EQ && e.getTerm(0 ) instanceof Variable && e.getTerm(1) instanceof Constant)
                            s.put((Variable)e.getTerm(0), (Constant)e.getTerm(1));
                        else if (e.getFunctionSymbol() == ExpressionOperation.EQ && e.getTerm(1 ) instanceof Variable && e.getTerm(0) instanceof Constant)
                            s.put((Variable)e.getTerm(1), (Constant)e.getTerm(0));
                        else
                            builder.add(e);

                    ImmutableSubstitution<VariableOrGroundTerm> sub = substitutionFactory.getSubstitution(s.build());

                    filters = builder.build().stream()
                            .map(e -> sub.applyToBooleanExpression(e))
                            .collect(ImmutableCollectors.toList());

                    dataAtoms = re.getDataAtoms().stream()
                            .map(a -> (DataAtom<RelationPredicate>)sub.applyToDataAtom(a))
                            .collect(ImmutableCollectors.toList());

                    lookupTable = re.getAttributes().entrySet().stream()
                            .collect(ImmutableCollectors.toMap(Map.Entry::getKey, e -> sub.apply(e.getValue())));
                }
                catch (UnsupportedSelectQueryException e) {
                    ImmutableList<QuotedID> attributes = new SelectQueryAttributeExtractor(metadata, termFactory)
                            .extract(sourceQuery);
                    ParserViewDefinition view = metadata.createParserView(sourceQuery, attributes);

                    // this is required to preserve the order of the variables
                    ImmutableList<Map.Entry<QualifiedAttributeID, Variable>> list = view.getAttributes().stream()
                            .map(att -> new AbstractMap.SimpleEntry<>(
                                    new QualifiedAttributeID(null, att.getID()), // strip off the ParserViewDefinitionName
                                    termFactory.getVariable(att.getID().getName())))
                            .collect(ImmutableCollectors.toList());

                    lookupTable = list.stream().collect(ImmutableCollectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    ImmutableList<Variable> arguments = list.stream().map(Map.Entry::getValue).collect(ImmutableCollectors.toList());

                    dataAtoms = ImmutableList.of(atomFactory.getDataAtom(view.getAtomPredicate(), arguments));
                    filters = ImmutableList.of();
                }

                for (TargetAtom atom : mappingAxiom.getTargetAtoms()) {
                    PPMappingAssertionProvenance provenance = mappingAxiom.getMappingAssertionProvenance(atom);
                    try {
                        ImmutableList<ImmutableTerm> headTerms = renameVariables(atom.getSubstitutedTerms(), lookupTable, idfac);
                        Function head = immutabilityTools.convertToMutableFunction(atom.getProjectionAtom().getPredicate(), headTerms);
                        normalizeMapping(head); // Normalizing language tags (SIDE-EFFECT!)

                        List<Function> body = Stream.concat(
                                dataAtoms.stream().map(t -> immutabilityTools.convertToMutableFunction(t)),
                                filters.stream().map(t -> immutabilityTools.convertToMutableFunction(t)))
                                .collect(Collectors.toList());

                        CQIE rule = datalogFactory.getCQIE(head, body);
                        eqNormalizer.enforceEqualities(rule); // Normalizing equalities (SIDE-EFFECT!)

                        IQ directlyConvertedIQ = datalogRule2QueryConverter.extractPredicatesAndConvertDatalogRule(rule, iqFactory);

                        IQTree child;
                        if (dataAtoms.size() == 1) {
                                child = iqFactory.createExtensionalDataNode(dataAtoms.get(0));
                        }
                        else {
                            child = iqFactory.createNaryIQTree(iqFactory.createInnerJoinNode(),
                                    dataAtoms.stream()
                                            .map(a -> iqFactory.createExtensionalDataNode(a))
                                            .collect(ImmutableCollectors.toList()));
                        }
                        IQTree qn;
                        if (!filters.isEmpty()) {
                            FilterNode filterNode = iqFactory.createFilterNode(
                                    filters.reverse().stream().reduce(null,
                                                    (a, b) -> (a == null)
                                                            ? b
                                                            : termFactory.getImmutableExpression(ExpressionOperation.AND, b, a)));
                            qn = iqFactory.createUnaryIQTree(filterNode, child);
                        }
                        else
                            qn = child;

                        ImmutableMap.Builder<Variable, ImmutableTerm> builder = ImmutableMap.builder();
                        ImmutableList.Builder<Variable> varBuilder2 = ImmutableList.builder();
                        //System.out.println(atom.getSubstitution() + " AT "  + atom.getProjectionAtom());
                        for (Variable v : atom.getProjectionAtom().getArguments()) {
                            ImmutableTerm t = atom.getSubstitution().get(v);
                            if (t != null) {
                                builder.put(v, renameVariables(t, lookupTable, idfac));
                                varBuilder2.add(v);
                            }
                            else {
                                ImmutableTerm tt = renameVariables(v, lookupTable, idfac);
                                if (tt instanceof Variable) {
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
                        ImmutableSubstitution substitution = substitutionFactory.getSubstitution(builder.build());
                        ConstructionNode cn = iqFactory.createConstructionNode(ImmutableSet.copyOf(varList), substitution);

                        IQ iq0 = iqFactory.createIQ(atomFactory.getDistinctVariableOnlyDataAtom(atom.getProjectionAtom().getPredicate(), varList),
                                iqFactory.createUnaryIQTree(cn, qn));

                        String iq0s = iq0.toString();
                        String iq1s = directlyConvertedIQ.toString()
                                .replace("v0", "s")
                                .replace("v1", "p")
                                .replace("v2", "o")
                                .replace("npd-o", "npd-v2");
                        if (!iq0s.equals(iq1s))
                            System.out.println("IQ0: " + iq0 + " VS " + iq1s);

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
        for (ImmutableTerm term : terms)
            builder.add(renameVariables(term, attributes, idfac));
        return builder.build();
    }


    /**
     * Returns a new function by renaming variables occurring in the {@code function}
     *  according to the {@code attributes} lookup table
     */
    private ImmutableTerm renameVariables(ImmutableTerm term,
                                          ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes,
                                          QuotedIDFactory idfac) throws AttributeNotFoundException {

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
                return newTerm;
            }
            else if (term instanceof ImmutableFunctionalTerm) {
                ImmutableFunctionalTerm f = (ImmutableFunctionalTerm)term;
                ImmutableList.Builder<ImmutableTerm> builder = ImmutableList.builder();
                for (ImmutableTerm t : f.getTerms())
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

}
