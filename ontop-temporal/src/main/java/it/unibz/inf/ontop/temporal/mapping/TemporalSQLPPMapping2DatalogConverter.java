package it.unibz.inf.ontop.temporal.mapping;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.SQLPPMapping2DatalogConverter;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;
import it.unibz.inf.ontop.spec.mapping.parser.exception.InvalidSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.parser.exception.UnsupportedSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.parser.impl.RAExpression;
import it.unibz.inf.ontop.spec.mapping.parser.impl.SelectQueryAttributeExtractor;
import it.unibz.inf.ontop.spec.mapping.parser.impl.SelectQueryParser;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static it.unibz.inf.ontop.model.OntopModelSingletons.DATALOG_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;

public class TemporalSQLPPMapping2DatalogConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(it.unibz.inf.ontop.datalog.SQLPPMapping2DatalogConverter.class);

    public static ImmutableMap<CQIE, PPMappingAssertionProvenance> convert(Collection<SQLPPTriplesMap> triplesMaps,
                                                                           RDBMetadata metadata) throws InvalidMappingSourceQueriesException {
        Map<CQIE, PPMappingAssertionProvenance> mutableMap = new HashMap<>();

        List<String> errorMessages = new ArrayList<>();

        QuotedIDFactory idfac = metadata.getQuotedIDFactory();

        for (SQLPPTriplesMap mappingAxiom : triplesMaps) {
            try {
                OBDASQLQuery sourceQuery = mappingAxiom.getSourceQuery();

                List<Function> body;
                ImmutableMap<QualifiedAttributeID, Variable> lookupTable;

                try {
                    SelectQueryParser sqp = new SelectQueryParser(metadata);
                    RAExpression re = sqp.parse(sourceQuery.toString());
                    lookupTable = re.getAttributes();

                    body = new ArrayList<>(re.getDataAtoms().size() + re.getFilterAtoms().size());
                    body.addAll(re.getDataAtoms());
                    body.addAll(re.getFilterAtoms());
                }
                catch (UnsupportedSelectQueryException e) {
                    ImmutableList<QuotedID> attributes = new SelectQueryAttributeExtractor(metadata)
                            .extract(sourceQuery.toString());
                    ParserViewDefinition view = metadata.createParserView(sourceQuery.toString(), attributes);

                    // this is required to preserve the order of the variables
                    ImmutableList<Map.Entry<QualifiedAttributeID,Variable>> list = view.getAttributes().stream()
                            .map(att -> new AbstractMap.SimpleEntry<>(
                                    new QualifiedAttributeID(null, att.getID()), // strip off the ParserViewDefinitionName
                                    TERM_FACTORY.getVariable(att.getID().getName())))
                            .collect(ImmutableCollectors.toList());

                    lookupTable = list.stream().collect(ImmutableCollectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    List<Term> arguments = list.stream().map(Map.Entry::getValue).collect(ImmutableCollectors.toList());

                    body = new ArrayList<>(1);
                    body.add(TERM_FACTORY.getFunction(Relation2Predicate.createPredicateFromRelation(view), arguments));
                }

                for (ImmutableFunctionalTerm atom : mappingAxiom.getTargetAtoms()) {
                    PPMappingAssertionProvenance provenance = mappingAxiom.getMappingAssertionProvenance(atom);
                    try {
                        Function head = renameVariables(atom, lookupTable, idfac);
                        CQIE rule = DATALOG_FACTORY.getCQIE(head, body);

                        PPMappingAssertionProvenance previous = mutableMap.put(rule, provenance);
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

        return ImmutableMap.copyOf(mutableMap);
    }

    /**
     * Returns a new function by renaming variables occurring in the {@code function}
     *  according to the {@code attributes} lookup table
     */
    private static Function renameVariables(Function function, ImmutableMap<QualifiedAttributeID, Variable> attributes,
                                            QuotedIDFactory idfac) throws AttributeNotFoundException {
        List<Term> terms = function.getTerms();
        List<Term> newTerms = new ArrayList<>(terms.size());
        for (Term term : terms) {
            Term newTerm;
            if (term instanceof Variable) {
                Variable var = (Variable) term;
                QuotedID attribute = idfac.createAttributeID(var.getName());
                newTerm = attributes.get(new QualifiedAttributeID(null, attribute));

                if (newTerm == null) {
                    QuotedID quotedAttribute = QuotedID.createIdFromDatabaseRecord(idfac, var.getName());
                    newTerm = attributes.get(new QualifiedAttributeID(null, quotedAttribute));

                    if (newTerm == null)
                        throw new AttributeNotFoundException("The source query does not provide the attribute " + attribute
                                + " (variable " + var.getName() + ") required by the target atom.");
                }
            }
            else if (term instanceof Function)
                newTerm = renameVariables((Function) term, attributes, idfac);
            else if (term instanceof Constant)
                newTerm = term.clone();
            else
                throw new RuntimeException("Unknown term type: " + term);

            newTerms.add(newTerm);
        }

        return TERM_FACTORY.getFunction(function.getFunctionSymbol(), newTerms);
    }


    private static class UnboundVariableException extends Exception {
        UnboundVariableException(String message) {
            super(message);
        }
    }

    private static class AttributeNotFoundException extends Exception {
        AttributeNotFoundException(String message) {
            super(message);
        }
    }

//    private static Term getGraphURITemplate(TemporalMappingInterval intervalQuery){
//
//        Term graphConstrantLiteral = TERM_FACTORY.getConstantLiteral("GRAPH");
//        Term beginInc = TERM_FACTORY.getConstantLiteral(intervalQuery.isBeginInclusiveToString(), Predicate.COL_TYPE.BOOLEAN);
//        Term endInc = TERM_FACTORY.getConstantLiteral(intervalQuery.isEndInclusiveToString(), Predicate.COL_TYPE.BOOLEAN);
//
//        return TERM_FACTORY.getUriTemplate(graphConstrantLiteral, beginInc, intervalQuery.getBegin(),intervalQuery.getEnd(), endInc);
//    }
//
//
//    private static TargetAtom convertToQuadAtom(ImmutableFunctionalTerm targetAtom, TemporalMappingInterval intervalQuery){
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

}
