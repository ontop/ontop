package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import fj.data.*;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.datalog.EQNormalizer;
import it.unibz.inf.ontop.datalog.SQLPPMapping2DatalogConverter;
import it.unibz.inf.ontop.datalog.impl.DatalogConversionTools;
import it.unibz.inf.ontop.datalog.impl.DatalogProgram2QueryConverterImpl;
import it.unibz.inf.ontop.datalog.impl.DatalogRule2QueryConverter;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.ProvenanceMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.mapping.TargetAtom;
import it.unibz.inf.ontop.iq.mapping.impl.TargetAtomImpl;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ImmutableQueryModifiers;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.IriConstants;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.spec.impl.LegacyIsNotNullDatalogMappingFiller;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;
import it.unibz.inf.ontop.spec.mapping.parser.exception.InvalidSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.parser.exception.UnsupportedSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.parser.impl.RAExpression;
import it.unibz.inf.ontop.spec.mapping.parser.impl.SelectQueryAttributeExtractor;
import it.unibz.inf.ontop.spec.mapping.parser.impl.SelectQueryParser;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.TemporalPPMappingConverter;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.temporal.datalog.TemporalDatalog2QueryMappingConverter;
import it.unibz.inf.ontop.temporal.mapping.SQLPPTemporalTriplesMap;
import it.unibz.inf.ontop.temporal.mapping.TemporalMappingInterval;
import it.unibz.inf.ontop.temporal.mapping.TemporalSQLPPMapping2DatalogConverter;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.jena.base.Sys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.Temporal;
import java.util.*;
import java.util.List;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.ATOM_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.SUBSTITUTION_FACTORY;
import static it.unibz.inf.ontop.model.term.impl.GroundTermTools.castIntoGroundTerm;
import static it.unibz.inf.ontop.model.term.impl.GroundTermTools.isGroundTerm;
import static it.unibz.inf.ontop.model.term.impl.ImmutabilityTools.convertIntoImmutableTerm;
import static it.unibz.inf.ontop.model.term.impl.PredicateImpl.QUEST_QUADRUPLE_PRED;

public class TemporalPPMappingConverterImpl implements TemporalPPMappingConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemporalPPMappingConverter.class);
    private final SpecificationFactory specificationFactory;
    private final IntermediateQueryFactory iqFactory;
    private final ProvenanceMappingFactory provMappingFactory;
    private final TemporalDatalog2QueryMappingConverter mappingConverter;



    @Inject
    private TemporalPPMappingConverterImpl(SpecificationFactory specificationFactory,
                                           IntermediateQueryFactory iqFactory,
                                           ProvenanceMappingFactory provMappingFactory, TemporalDatalog2QueryMappingConverter mappingConverter) {
        this.specificationFactory = specificationFactory;
        this.iqFactory = iqFactory;
        this.provMappingFactory = provMappingFactory;
        this.mappingConverter = mappingConverter;

    }
    @Override
    public MappingWithProvenance convert(SQLPPMapping ppMapping, DBMetadata dbMetadata, ExecutorRegistry executorRegistry) throws InvalidMappingSourceQueriesException {

        ImmutableMap<CQIE, PPMappingAssertionProvenance> datalogMap = convertIntoDatalog(ppMapping, dbMetadata);

        return mappingConverter.convertMappingRules(datalogMap, dbMetadata, executorRegistry, ppMapping.getMetadata());
//
//        try {
//            return convertIntoMappingWithProvenance(ppMapping, dbMetadata);
//        } catch (InvalidSelectQueryException e) {
//            e.printStackTrace();
//        }
//        return null;
    }

    /**
     * Assumption: one CQIE per mapping axiom (no nested union)
     */
    private ImmutableMap<CQIE, PPMappingAssertionProvenance> convertIntoDatalog(SQLPPMapping ppMapping, DBMetadata dbMetadata)
            throws InvalidMappingSourceQueriesException {

        /*
         * May also add views in the DBMetadata!
         */
        ImmutableMap<CQIE, PPMappingAssertionProvenance> datalogMap = TemporalSQLPPMapping2DatalogConverter.convert(
                ppMapping.getTripleMaps(), dbMetadata);

        LOGGER.debug("Original mapping size: {}", datalogMap.size());

        // Normalizing language tags and equalities (SIDE-EFFECT!)
        normalizeMapping(datalogMap.keySet());

        return datalogMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> LegacyIsNotNullDatalogMappingFiller.addNotNull(e.getKey(), dbMetadata),
                        Map.Entry::getValue));
    }

    /**
     * Normalize language tags (make them lower-case) and equalities
     * (remove them by replacing all equivalent terms with one representative)
     */

    private void normalizeMapping(ImmutableSet<CQIE> unfoldingProgram) {

        // Normalizing language tags. Making all LOWER CASE

        for (CQIE mapping : unfoldingProgram) {
            Function head = mapping.getHead();
            for (Term term : head.getTerms()) {
                if (!(term instanceof Function))
                    continue;

                Function typedTerm = (Function) term;
                if (typedTerm.getTerms().size() == 2 && typedTerm.getFunctionSymbol().getName().equals(IriConstants.RDFS_LITERAL_URI)) {
                    // changing the language, its always the second inner term (literal,lang)
                    Term originalLangTag = typedTerm.getTerm(1);
                    if (originalLangTag instanceof ValueConstant) {
                        ValueConstant originalLangConstant = (ValueConstant) originalLangTag;
                        Term normalizedLangTag = TERM_FACTORY.getConstantLiteral(originalLangConstant.getValue().toLowerCase(),
                                originalLangConstant.getType());
                        typedTerm.setTerm(1, normalizedLangTag);
                    }
                }
            }
        }

        // Normalizing equalities
        for (CQIE cq: unfoldingProgram)
            EQNormalizer.enforceEqualities(cq);
    }





//    private MappingWithProvenance convertIntoMappingWithProvenance(SQLPPMapping ppMapping, DBMetadata dbMetadata) throws InvalidSelectQueryException {
//        RDBMetadata metadata = (RDBMetadata)dbMetadata;
//
//        for(SQLPPTriplesMap triplesMap : ppMapping.getTripleMaps()){
//
//            SelectQueryParser sqp = new SelectQueryParser(metadata);
//            OBDASQLQuery sourceQuery = triplesMap.getSourceQuery();
//            List<Function> body = null ;
//            ImmutableMap<QualifiedAttributeID, Variable> lookupTable;
//            try {
//                RAExpression re = sqp.parse(sourceQuery.toString());
//                lookupTable = re.getAttributes();
//
//                body = new ArrayList<>(re.getDataAtoms().size() + re.getFilterAtoms().size());
//                body.addAll(re.getDataAtoms());
//                body.addAll(re.getFilterAtoms());
//
//            } catch (InvalidSelectQueryException e) {
//                e.printStackTrace();
//            } catch (UnsupportedSelectQueryException e) {
//                // WRAP UP
//                //ImmutableSet<QuotedID> variableNames = mappingAxiom.getTargetQuery().stream()
//                //        .map(f -> collectVariableNames(idfac, f))
//                //        .reduce((s1, s2) -> ImmutableSet.<QuotedID>builder().addAll(s1).addAll(s2).build())
//                //        .get();
//                ImmutableList<QuotedID> variableNames =
//                        new SelectQueryAttributeExtractor(metadata).extract(sourceQuery.toString());
//
//                ParserViewDefinition view = metadata.createParserView(sourceQuery.toString());
//                // TODO: clean up
//                boolean needsCreating = view.getAttributes().isEmpty();
//                ImmutableMap.Builder<QualifiedAttributeID, Variable> builder = ImmutableMap.builder();
//                List<Term> arguments = new ArrayList<>(variableNames.size());
//                variableNames.forEach(id -> {
//                    QualifiedAttributeID qId = new QualifiedAttributeID(null, id);
//                    if (needsCreating)
//                        view.addAttribute(qId);
//                    Variable var = TERM_FACTORY.getVariable(id.getName());
//                    builder.put(qId, var);
//                    arguments.add(var);
//                });
//
//                lookupTable = builder.build();
//
//                body = new ArrayList<>(1);
//                body.add(TERM_FACTORY.getFunction(Relation2Predicate.createPredicateFromRelation(view), arguments));
//            }
//
//            for(ImmutableFunctionalTerm ta : triplesMap.getTargetAtoms()) {
//
//                TemporalMappingInterval intervalQuery = ((SQLPPTemporalTriplesMap) triplesMap).getTemporalMappingInterval();
//                TargetAtom targetAtom = convertToQuadAtom(ta, intervalQuery);
//                DistinctVariableOnlyDataAtom projectionAtom = targetAtom.getProjectionAtom();
//                ConstructionNode rootNode = iqFactory.createConstructionNode(projectionAtom.getVariables(),
//                        targetAtom.getSubstitution());
//                List<Function> bodyAtoms = body;
////                if (bodyAtoms.isEmpty()) {
////                    return createFact(dbMetadata, rootNode, projectionAtom, executorRegistry, iqFactory);
////                }
////                else {
////                    DatalogRule2QueryConverter.AtomClassification atomClassification = new DatalogRule2QueryConverter.AtomClassification(bodyAtoms);
////
////                    return createDefinition(dbMetadata, rootNode, projectionAtom, tablePredicates,
////                            atomClassification.dataAndCompositeAtoms, atomClassification.booleanAtoms,
////                            atomClassification.optionalGroupAtom, iqFactory, executorRegistry);
////                }
//            }
//
//                System.out.println();
//            }
//
//        return null;
//    }
//
//    private Term getGraphURITemplate(TemporalMappingInterval intervalQuery){
//
//        Term graphConstrantLiteral = TERM_FACTORY.getConstantLiteral("GRAPH");
//        Term beginInc = TERM_FACTORY.getConstantLiteral(intervalQuery.isBeginInclusiveToString(), Predicate.COL_TYPE.BOOLEAN);
//        Term endInc = TERM_FACTORY.getConstantLiteral(intervalQuery.isEndInclusiveToString(), Predicate.COL_TYPE.BOOLEAN);
//
//        return TERM_FACTORY.getUriTemplate(graphConstrantLiteral, beginInc, intervalQuery.getBegin(),intervalQuery.getEnd(), endInc);
//    }
//
//    private TargetAtom convertToQuadAtom(ImmutableFunctionalTerm targetAtom, TemporalMappingInterval intervalQuery){
//        Predicate atomPred = targetAtom.getFunctionSymbol();
//
//        ImmutableList.Builder<Variable> argListBuilder = ImmutableList.builder();
//        ImmutableMap.Builder<Variable, ImmutableTerm> allBindingBuilder = ImmutableMap.builder();
//
//        ArrayList <Term> argList = new ArrayList();
//        Iterator it = targetAtom.getArguments().iterator();
//        while(it.hasNext()){
//            argList.add((Term) it.next());
//        }
//
//        if(argList.size() == 1){
//            Term rdfTypeTerm = TERM_FACTORY.getConstantURI(IriConstants.RDF_TYPE);
//            argList.add(rdfTypeTerm);
//            Term predTerm = TERM_FACTORY.getConstantURI(atomPred.getName());
//            argList.add(predTerm);
//        }else {
//            Term predTerm = TERM_FACTORY.getConstantURI(atomPred.getName());
//            argList.add(1, predTerm);
//        }
//
//        Term graphURITemplate = getGraphURITemplate(intervalQuery);
//        argList.add(graphURITemplate);
//
//        /**
//         * Replaces all the terms by variables.
//         * Makes sure these variables are unique.
//         *
//         * Creates allBindings entries if needed (in case of constant of a functional term)
//         */
//        VariableGenerator variableGenerator = new VariableGenerator(ImmutableSet.of());
//        for (Term term : argList) {
//            Variable newArgument;
//
//            /**
//             * If a variable occurs multiple times, rename it and keep track of the equivalence.
//             *
//             */
//            if (term instanceof Variable) {
//                Variable originalVariable = (Variable) term;
//                newArgument = variableGenerator.generateNewVariableIfConflicting(originalVariable);
//                if (!newArgument.equals(originalVariable)) {
//                    allBindingBuilder.put(newArgument, originalVariable);
//                }
//            }
//            /**
//             * Ground-term: replace by a variable and add a binding.
//             * (easier to merge than putting the ground term in the data atom).
//             */
//            else if (isGroundTerm(term)) {
//                Variable newVariable = variableGenerator.generateNewVariable();
//                newArgument = newVariable;
//                allBindingBuilder.put(newVariable, castIntoGroundTerm(term));
//            }
//            /**
//             * Non-ground functional term
//             */
//            else {
//                ImmutableTerm immutableTerm = convertIntoImmutableTerm(term);
//                variableGenerator.registerAdditionalVariables(immutableTerm.getVariableStream()
//                        .collect(ImmutableCollectors.toSet()));
//                Variable newVariable = variableGenerator.generateNewVariable();
//                newArgument = newVariable;
//                allBindingBuilder.put(newVariable, convertIntoImmutableTerm(term));
//            }
//            argListBuilder.add(newArgument);
//        }
//
//
//        DistinctVariableOnlyDataAtom dataAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getAtomPredicate(QUEST_QUADRUPLE_PRED), argListBuilder.build());
//        ImmutableSubstitution<ImmutableTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(allBindingBuilder.build());
//
//
//        return new TargetAtomImpl(dataAtom, substitution);
//    }
//
//
//
//
//
//    private TargetAtom convertFromAtom(ImmutableFunctionalTerm targetAtom){
//        Predicate atomPred = targetAtom.getFunctionSymbol();
//        AtomPredicate atomPredicate = (atomPred instanceof AtomPredicate)
//                ? (AtomPredicate) atomPred
//                : ATOM_FACTORY.getAtomPredicate(atomPred);
//
//        ImmutableList.Builder<Variable> argListBuilder = ImmutableList.builder();
//        ImmutableMap.Builder<Variable, ImmutableTerm> allBindingBuilder = ImmutableMap.builder();
//
//        /**
//         * Replaces all the terms by variables.
//         * Makes sure these variables are unique.
//         *
//         * Creates allBindings entries if needed (in case of constant of a functional term)
//         */
//        VariableGenerator variableGenerator = new VariableGenerator(ImmutableSet.of());
//        for (Term term : targetAtom.getArguments()) {
//            Variable newArgument;
//
//            /**
//             * If a variable occurs multiple times, rename it and keep track of the equivalence.
//             *
//             */
//            if (term instanceof Variable) {
//                Variable originalVariable = (Variable) term;
//                newArgument = variableGenerator.generateNewVariableIfConflicting(originalVariable);
//                if (!newArgument.equals(originalVariable)) {
//                    allBindingBuilder.put(newArgument, originalVariable);
//                }
//            }
//            /**
//             * Ground-term: replace by a variable and add a binding.
//             * (easier to merge than putting the ground term in the data atom).
//             */
//            else if (isGroundTerm(term)) {
//                Variable newVariable = variableGenerator.generateNewVariable();
//                newArgument = newVariable;
//                allBindingBuilder.put(newVariable, castIntoGroundTerm(term));
//            }
//            /**
//             * Non-ground functional term
//             */
//            else {
//                ImmutableTerm immutableTerm = convertIntoImmutableTerm(term);
//                variableGenerator.registerAdditionalVariables(immutableTerm.getVariableStream()
//                        .collect(ImmutableCollectors.toSet()));
//                Variable newVariable = variableGenerator.generateNewVariable();
//                newArgument = newVariable;
//                allBindingBuilder.put(newVariable, convertIntoImmutableTerm(term));
//            }
//            argListBuilder.add(newArgument);
//        }
//
//        DistinctVariableOnlyDataAtom dataAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(atomPredicate, argListBuilder.build());
//        ImmutableSubstitution<ImmutableTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(allBindingBuilder.build());
//
//
//        return new TargetAtomImpl(dataAtom, substitution);
//    }
}
