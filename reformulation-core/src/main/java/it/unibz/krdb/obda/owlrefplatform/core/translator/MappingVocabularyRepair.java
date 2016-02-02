package it.unibz.krdb.obda.owlrefplatform.core.translator;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/***
 * This is a hack class that helps fix and OBDA model in which the mappings
 * include predicates that have not been properly typed.
 *
 * @author mariano
 */
public class MappingVocabularyRepair {

    private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

    private static final Logger log = LoggerFactory.getLogger(MappingVocabularyRepair.class);

    public static void fixOBDAModel(OBDAModel model, ImmutableOntologyVocabulary vocabulary) {
        log.debug("Fixing OBDA Model");
        for (OBDADataSource source : model.getSources()) {
            Collection<OBDAMappingAxiom> mappings = new LinkedList<>(model.getMappings(source.getSourceID()));
            model.removeAllMappings(source.getSourceID());
            //try {
            model.addMappings(source.getSourceID(), fixMappingPredicates(mappings, vocabulary));
            //}

//			catch (DuplicateMappingException e) {
//				e.printStackTrace();
//			}

//			catch (Exception e) {
//				throw new RuntimeException(e);
//			}
        }
    }

    /***
     * Makes sure that the mappings given are correctly typed w.r.t. the given
     * vocabulary.
     *
     * @param originalMappings
     * @param vocabulary
     * @return
     */
    private static Collection<OBDAMappingAxiom> fixMappingPredicates(Collection<OBDAMappingAxiom> originalMappings, ImmutableOntologyVocabulary vocabulary) {
        //		log.debug("Reparing/validating {} mappings", originalMappings.size());

        Map<String, Predicate> urimap = new HashMap<>();
        for (OClass p : vocabulary.getClasses())
            urimap.put(p.getName(), p.getPredicate());

        for (ObjectPropertyExpression p : vocabulary.getObjectProperties())
            urimap.put(p.getName(), p.getPredicate());

        for (DataPropertyExpression p : vocabulary.getDataProperties())
            urimap.put(p.getName(), p.getPredicate());

        for (AnnotationProperty p : vocabulary.getAnnotationProperties())
            urimap.put(p.getName(), p.getPredicate());


        Collection<OBDAMappingAxiom> result = new ArrayList<>();
        for (OBDAMappingAxiom mapping : originalMappings) {
            List<Function> targetQuery = mapping.getTargetQuery();
            List<Function> newbody = new LinkedList<>();

            for (Function atom : targetQuery) {
                Predicate predTarget = atom.getFunctionSymbol();

				/* Fixing terms */
                List<Term> arguments = new ArrayList<>();
//                for (Term term : atom.getTerms()) {
//                    //newTerms.add(fixTerm(term));
//                    newTerms.add(fixTerm(term));
//                }

                arguments = atom.getTerms();

                Function newatom = null;

                Predicate predicate = urimap.get(predTarget.getName());
                if (predicate == null) {
                    if (!predTarget.isTriplePredicate()) {
                        log.warn("WARNING: Mapping references an unknown class/property: " + predTarget.getName());

						/*
                         * All this part is to handle the case where the predicate or the class is defined
						 * by the mapping but not present in the ontology.
						 */
                        newatom = fixUndeclaredPredicate(mapping, predTarget, arguments);
                    } else {

                        newatom = fixTripleAtom(mapping, arguments, predicate);
                    }
                } else {

                    newatom = fixOntologyPredicate(mapping, predTarget, arguments, predicate);
                }

                newbody.add(newatom);
            } //end for

            result.add(dfac.getRDBMSMappingAxiom(mapping.getId(),
                    dfac.getSQLQuery(mapping.getSourceQuery().toString()), newbody));
        }
//		log.debug("Repair done. Returning {} mappings", result.size());
        return result;
    }

    private static Function fixTripleAtom(OBDAMappingAxiom mapping, List<Term> arguments, Predicate predicate) {
        Function newatom;
        Term t0 = arguments.get(0);
        if ((t0 instanceof Function) && ((Function) t0).getFunctionSymbol() instanceof URITemplatePredicate) {

                newatom = dfac.getTripleAtom(arguments.get(0), arguments.get(1), arguments.get(2));

        } else {
            String message = String.format("" +
                    "Error with triple atom\n" +
                    "   %s \n" +
                    "The reason is: \n" +
                    "the subject {%s} in the mapping is not an iri. Solution: Solution: use `<{val}>` for IRI retrieved from columns or `prefix:{val}` for URI template ", mapping, t0);

            throw new IllegalArgumentException(message);
        }
        return newatom;
    }

    private static Function fixOntologyPredicate(OBDAMappingAxiom mapping, Predicate predTarget, List<Term> arguments, Predicate predicate) {
        Function newatom;
        if (arguments.size() == 1) {
            Term t0 = arguments.get(0);
            if ((t0 instanceof Function) && ((Function)t0).getFunctionSymbol() instanceof URITemplatePredicate) {
                newatom = dfac.getFunction(predicate, arguments.get(0));
            } else {
                String message = String.format("" +
                        "Error with class <%s> used in the mapping\n" +
                        "%s \n" +
                        "The reason is: \n" +
                        "the subject {%s} in the mapping is not an iri. Solution: use `<{val}>` for IRI retrieved from columns or `prefix:{val}` for URI template. ", predicate, mapping, t0);
                throw new IllegalArgumentException(message);
            }

        } else if (arguments.size() == 2) {

            Term t0 = arguments.get(0);
            if ((t0 instanceof Function) && ((Function) t0).getFunctionSymbol() instanceof URITemplatePredicate) {

                //object property
                if (predicate.isObjectProperty()) {

                    Term t1 = arguments.get(1);
                    if ((t1 instanceof Function) && ((Function) t1).getFunctionSymbol() instanceof URITemplatePredicate) {
                        newatom = dfac.getFunction(predicate, arguments.get(0), arguments.get(1));

                    } else {

                        String message = String.format("" +
                                "Error with property <%s> used in the mapping\n" +
                                "   %s \n" +
                                "The reason is: \n" +
                                "the object {%s} in the mapping is not an iri. Solution: Solution: use `<{val}>` for IRI retrieved from columns or `prefix:{val}` for URI template ", predicate, mapping, t1);

                        throw new IllegalArgumentException(message);
                    }

                } else {
                    //data property
                    if (predicate.isDataProperty()) {


                        newatom = dfac.getFunction(predicate, arguments.get(0), arguments.get(1));

                    } else { //case of annotation property

                        //we understood from the mappings that the annotation property can be treated as object property
                        if (predTarget.isObjectProperty()) {

                            Term t1 = arguments.get(1);
                            if ((t1 instanceof Function) && ((Function) t1).getFunctionSymbol() instanceof URITemplatePredicate) {
                                newatom = dfac.getFunction(predTarget, arguments.get(0), arguments.get(1));

                            } else {

                                String message = String.format("" +
                                        "Error with property <%s> used in the mapping\n" +
                                        "   %s \n" +
                                        "The reason is: \n" +
                                        "the object {%s} in the mapping is not an iri. Solution: Solution: use `<{val}>` for IRI retrieved from columns or `prefix:{val}` for URI template ", predicate, mapping, t1);

                                throw new IllegalArgumentException(message);
                            }

                            //we understood from the mappings that the annotation property can be treated as data property
                        } else if (predTarget.isDataProperty()) {

                            newatom = dfac.getFunction(predTarget, arguments.get(0), arguments.get(1));

                        } else { //annotation property not clear, is treated as a data property

                            Predicate pred = dfac.getDataPropertyPredicate(predTarget.getName());
                            newatom = dfac.getFunction(pred, arguments.get(0), arguments.get(1));

                        }
                    }

                }
            } else {
                String message = String.format("" +
                        "Error with property <%s> used in the mapping\n" +
                        "%s \n" +
                        "The reason is: \n" +
                        "the subject {%s} in the mapping is not an iri. Solution: Solution: use `<{val}>` for IRI retrieved from columns or `prefix:{val}` for URI template. ", predicate, mapping, t0);

                throw new IllegalArgumentException(message);
            }
        } else {
            throw new RuntimeException("ERROR: Predicate has an incorrect arity: " + predTarget.getName());
        }
        return newatom;
    }

    private static Function fixUndeclaredPredicate(OBDAMappingAxiom mapping, Predicate undeclaredPredicate, List<Term> arguments) {
        Function fixedTarget;
        if (arguments.size() == 1) {
            Term t0 = arguments.get(0);
            if ((t0 instanceof Function) && ((Function)t0).getFunctionSymbol() instanceof URITemplatePredicate) {
                Predicate pred = dfac.getClassPredicate(undeclaredPredicate.getName());
                fixedTarget = dfac.getFunction(pred, arguments.get(0));
            } else {
                String message = String.format("" +
                        "Error with class <%s> used in the mapping\n" +
                        "%s \n" +
                        "The reason is: \n" +
                        "the subject {%s} in the mapping is not an iri. Solution: use `<{val}>` for IRI retrieved from columns or `prefix:{val}` for URI template. ", undeclaredPredicate.getName(), mapping, t0);
                throw new IllegalArgumentException(message);
            }
        } else if (arguments.size() == 2) {
            Term t0 = arguments.get(0);
            Term t1 = arguments.get(1);
            if (t0 instanceof Function) {

                if ((t1 instanceof Function)) {

                    Function ft0 = (Function) t0;
                    Function ft1 = (Function) t1;

                    boolean t0uri = (ft0.getFunctionSymbol() instanceof URITemplatePredicate);
                    boolean t1uri = (ft1.getFunctionSymbol() instanceof URITemplatePredicate);

                    if (t0uri && t1uri) {
                        Predicate pred = dfac.getObjectPropertyPredicate(undeclaredPredicate.getName());
                        fixedTarget = dfac.getFunction(pred, t0, t1);
                    } else {
                        Predicate pred = dfac.getDataPropertyPredicate(undeclaredPredicate.getName());
                        fixedTarget = dfac.getFunction(pred, t0, t1);
                    }
                } else {
                    //cases we cannot recognize

                    String message = String.format("" +
                            "Error with property <%s> used in the mapping\n" +
                            "   %s \n" +
                            "The reason is: \n" +
                            "1. the property is not declared in the ontology; and \n" +
                            "2. the object {%s} in the mapping is untyped. Solution: use `{val}^^xsd:string` for literal and `<{val}>` for IRI. ", undeclaredPredicate.getName(), mapping, t1);

                    throw new IllegalArgumentException(message);
                }
            } else {
                String message = String.format("" +
                        "Error with property <%s> used in the mapping\n" +
                        "%s \n" +
                        "The reason is: \n" +
                        "the subject {%s} in the mapping is not an iri. Solution: Solution: use `<{val}>` for IRI retrieved from columns or `prefix:{val}` for URI template. ", undeclaredPredicate.getName(), mapping, t0);

                throw new IllegalArgumentException(message);
            }
        } else {
            System.err.println("ERROR: Predicate has an incorrect arity: " + undeclaredPredicate.getName());
            throw new IllegalArgumentException("ERROR: Predicate has an incorrect arity: " + undeclaredPredicate.getName());
        }
        return fixedTarget;
    }



    /***
     * Fix functions that represent URI templates. Currently,the only fix
     * necessary is replacing the old-style template function with the new one,
     * that uses a string template and placeholders.
     *
     * @param term
     * @return
     */
    private static Term fixTerm(Term term) {
        if (term instanceof Function) {
            Function fterm = (Function) term;
            Predicate predicate = fterm.getFunctionSymbol();
            if (predicate instanceof DatatypePredicate) {
                // no fix necessary
                return term;
            }
            if (predicate instanceof URITemplatePredicate) {
                // no fix necessary
                return term;
            }
            // We have a function that is not a built-in, hence its an old-style uri
            // template function(parm1,parm2,...)
            StringBuilder newTemplate = new StringBuilder();
            newTemplate.append(predicate.getName().toString());
            for (int i = 0; i < fterm.getArity(); i++) {
                newTemplate.append("-{}");
            }

            LinkedList<Term> newTerms = new LinkedList<>();
            newTerms.add(dfac.getConstantLiteral(newTemplate.toString()));
            newTerms.addAll(fterm.getTerms());

            return dfac.getUriTemplate(newTerms);
        }
        return term;
    }

}
