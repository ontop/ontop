package it.unibz.inf.ontop.temporal.mapping;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.iq.mapping.TargetAtom;
import it.unibz.inf.ontop.iq.mapping.impl.TargetAtomImpl;
import it.unibz.inf.ontop.model.IriConstants;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;

import java.util.*;

import it.unibz.inf.ontop.spec.mapping.parser.impl.RAExpression;
import it.unibz.inf.ontop.spec.mapping.parser.impl.SelectQueryAttributeExtractor;
import it.unibz.inf.ontop.spec.mapping.parser.impl.SelectQueryParser;
import it.unibz.inf.ontop.spec.mapping.parser.exception.InvalidSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.parser.exception.UnsupportedSelectQueryException;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.temporal.mapping.impl.SQLPPTemporalTriplesMapImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibz.inf.ontop.model.OntopModelSingletons.*;
import static it.unibz.inf.ontop.model.term.impl.PredicateImpl.QUEST_QUADRUPLE_PRED;

import static it.unibz.inf.ontop.model.term.impl.GroundTermTools.castIntoGroundTerm;
import static it.unibz.inf.ontop.model.term.impl.GroundTermTools.isGroundTerm;
import static it.unibz.inf.ontop.model.term.impl.ImmutabilityTools.convertIntoImmutableTerm;

public class TemporalSQLPPMapping2DatalogConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(it.unibz.inf.ontop.datalog.SQLPPMapping2DatalogConverter.class);

    /**
     * returns a Datalog representation of the mappings
     */
    public static ImmutableList<CQIE> constructDatalogProgram(Collection<SQLPPTriplesMap> triplesMaps,
                                                              DBMetadata metadata) throws InvalidMappingSourceQueriesException {
        return ImmutableList.copyOf(convert(triplesMaps, metadata).keySet());
    }

    public static ImmutableMap<CQIE, PPMappingAssertionProvenance> convert(Collection<SQLPPTriplesMap> triplesMaps,
                                                                           DBMetadata metadata0) throws InvalidMappingSourceQueriesException {
        Map<CQIE, PPMappingAssertionProvenance> mutableMap = new HashMap<>();

        RDBMetadata metadata = (RDBMetadata)metadata0;

        List<String> errorMessages = new ArrayList<>();

        QuotedIDFactory idfac = metadata.getQuotedIDFactory();

        for (SQLPPTriplesMap mappingAxiom : triplesMaps) {
            try {
                OBDASQLQuery sourceQuery = mappingAxiom.getSourceQuery();

                SelectQueryParser sqp = new SelectQueryParser(metadata);
                List<Function> body;
                ImmutableMap<QualifiedAttributeID, Variable> lookupTable;

                try {
                    RAExpression re = sqp.parse(sourceQuery.toString());
                    lookupTable = re.getAttributes();

                    body = new ArrayList<>(re.getDataAtoms().size() + re.getFilterAtoms().size());
                    body.addAll(re.getDataAtoms());
                    body.addAll(re.getFilterAtoms());
                }
                catch (UnsupportedSelectQueryException e) {
                    // WRAP UP
                    //ImmutableSet<QuotedID> variableNames = mappingAxiom.getTargetQuery().stream()
                    //        .map(f -> collectVariableNames(idfac, f))
                    //        .reduce((s1, s2) -> ImmutableSet.<QuotedID>builder().addAll(s1).addAll(s2).build())
                    //        .get();
                    ImmutableList<QuotedID> variableNames =
                            new SelectQueryAttributeExtractor(metadata).extract(sourceQuery.toString());

                    ParserViewDefinition view = metadata.createParserView(sourceQuery.toString());
                    // TODO: clean up
                    boolean needsCreating = view.getAttributes().isEmpty();
                    ImmutableMap.Builder<QualifiedAttributeID, Variable> builder = ImmutableMap.builder();
                    List<Term> arguments = new ArrayList<>(variableNames.size());
                    variableNames.forEach(id -> {
                        QualifiedAttributeID qId = new QualifiedAttributeID(null, id);
                        if (needsCreating)
                            view.addAttribute(qId);
                        Variable var = TERM_FACTORY.getVariable(id.getName());
                        builder.put(qId, var);
                        arguments.add(var);
                    });

                    lookupTable = builder.build();

                    body = new ArrayList<>(1);
                    body.add(TERM_FACTORY.getFunction(Relation2Predicate.createPredicateFromRelation(view), arguments));
                }

                for (ImmutableFunctionalTerm atom : mappingAxiom.getTargetAtoms()) {
                    PPMappingAssertionProvenance provenance = mappingAxiom.getMappingAssertionProvenance(atom);
                    try {
                        Function head = renameVariables(atom, lookupTable, idfac);
                        CQIE rule = DATALOG_FACTORY.getCQIE(head, body);

                        if (mutableMap.containsKey(rule)) {
                            LOGGER.warn("Redundant triples maps: \n" + provenance + "\n and \n" + mutableMap.get(rule));
                        } else {
                            mutableMap.put(rule, provenance);
                        }
                    } catch (TemporalSQLPPMapping2DatalogConverter.UnboundVariableException e) {
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
                                            QuotedIDFactory idfac) throws TemporalSQLPPMapping2DatalogConverter.UnboundVariableException {
        List<Term> terms = function.getTerms();
        List<Term> newTerms = new ArrayList<>(terms.size());
        for (Term t : terms)
            newTerms.add(renameTermVariables(t, attributes, idfac));

        return TERM_FACTORY.getFunction(function.getFunctionSymbol(), newTerms);
    }

    /**
     * Returns a new term by renaming variables occurring in the {@code term}
     *  according to the {@code attributes} lookup table
     */
    private static Term renameTermVariables(Term term, ImmutableMap<QualifiedAttributeID, Variable> attributes,
                                            QuotedIDFactory idfac) throws TemporalSQLPPMapping2DatalogConverter.UnboundVariableException {

        if (term instanceof Variable) {
            Variable var = (Variable) term;
            String varName = var.getName();
            // TODO: remove this code
            // chop off the qualifying table name
            if (varName.contains("."))
                varName = varName.substring(varName.indexOf(".") + 1);
            QuotedID attribute = idfac.createAttributeID(varName);
            Variable newVar = attributes.get(new QualifiedAttributeID(null, attribute));

            if (newVar == null) {
                QuotedID quotedAttribute = QuotedID.createIdFromDatabaseRecord(idfac, varName);
                newVar = attributes.get(new QualifiedAttributeID(null, quotedAttribute));

                if (newVar == null)
                    throw new TemporalSQLPPMapping2DatalogConverter.UnboundVariableException("The source query does not provide the attribute " + attribute
                            + " (variable " + var.getName() + ") required by the target atom.");
            }

            return newVar;
        }
        else if (term instanceof Function)
            return renameVariables((Function) term, attributes, idfac);

        else if (term instanceof Constant)
            return term.clone();

        throw new RuntimeException("Unknown term type: " + term);
    }

    private static class UnboundVariableException extends Exception {
        UnboundVariableException(String message) {
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
