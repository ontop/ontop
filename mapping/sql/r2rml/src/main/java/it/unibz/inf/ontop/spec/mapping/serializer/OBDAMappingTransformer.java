package it.unibz.inf.ontop.spec.mapping.serializer;

/*
 * #%L
 * ontop-obdalib-sesame
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


import com.google.common.collect.ImmutableList;
import eu.optique.r2rml.api.R2RMLMappingManager;
import eu.optique.r2rml.api.binding.rdf4j.RDF4JR2RMLMappingManager;
import eu.optique.r2rml.api.model.*;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.type.LanguageTag;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.impl.SQLQueryImpl;
import it.unibz.inf.ontop.spec.mapping.parser.impl.R2RMLVocabulary;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.utils.URITemplates;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Transform OBDA mappings in R2rml mappings
 * @author Sarah, Mindas, Timi, Guohui, Martin
 *
 */
public class OBDAMappingTransformer {

    private OWLOntology ontology;
	private Set<OWLObjectProperty> objectProperties;
    private Set<OWLDataProperty> dataProperties;

	private final RDF rdfFactory;
    private String baseIRIString;

	OBDAMappingTransformer(RDF rdfFactory) {
        this("urn:", rdfFactory);
	}

    OBDAMappingTransformer(String baseIRIString, RDF rdfFactory) {
        this.baseIRIString = baseIRIString;
		this.rdfFactory = rdfFactory;
	}

    /**
	 * Get R2RML TriplesMaps from OBDA mapping axiom
	 */
	public TriplesMap getTriplesMap(SQLPPTriplesMap axiom,
                                    PrefixManager prefixmng) {

		SQLQueryImpl squery = (SQLQueryImpl) axiom.getSourceQuery();
		ImmutableList<TargetAtom> tquery = axiom.getTargetAtoms();

		//triplesMap node
		String mapping_id = axiom.getId();

		// check if mapping id is an iri
		if (!mapping_id.contains(":")) {
            mapping_id = baseIRIString + mapping_id;
        }
		BlankNodeOrIRI mainNode = rdfFactory.createIRI(mapping_id);

        R2RMLMappingManager mm = RDF4JR2RMLMappingManager.getInstance();
		eu.optique.r2rml.api.MappingFactory mfact = mm.getMappingFactory();
		
		//Table
		LogicalTable lt = mfact.createR2RMLView(squery.getSQLQuery());
		
		//SubjectMap
		ImmutableFunctionalTerm uriTemplate = (ImmutableFunctionalTerm) tquery.get(0).getSubstitutedTerm(0); //URI("..{}..", , )
		String subjectTemplate =  URITemplates.getUriTemplateString(uriTemplate, prefixmng);		
		Template templs = mfact.createTemplate(subjectTemplate);
		SubjectMap sm = mfact.createSubjectMap(templs);

		TriplesMap tm = mfact.createTriplesMap(lt, sm, mainNode);
		
		//process target query
		for (TargetAtom func : tquery) {

			IRI predUri = null;

			Optional<Template> templp = Optional.empty();

			//triple
			ImmutableFunctionalTerm predf = (ImmutableFunctionalTerm)func.getSubstitutedTerm(1);

			if (predf.getFunctionSymbol() instanceof URITemplatePredicate) {
					if (predf.getTerms().size() == 1) { //fixed string
						predUri = rdfFactory.createIRI(((ValueConstant)(predf.getTerm(0))).getValue());
					}
					else {
						//template
						predUri = rdfFactory.createIRI(URITemplates.getUriTemplateString(predf, prefixmng));
                        templp = Optional.of(mfact.createTemplate(subjectTemplate));
					}
				}

			//term 0 is always the subject,  term 1 is the predicate, we check term 2 to have the object

			ImmutableTerm object = func.getSubstitutedTerm(2);

			//if the class IRI is constant
			if (predUri.equals(it.unibz.inf.ontop.model.vocabulary.RDF.TYPE)
					&& object instanceof ImmutableFunctionalTerm
					&& ((ImmutableFunctionalTerm) object).getFunctionSymbol() instanceof URITemplatePredicate
					&& ((ImmutableFunctionalTerm) object).getTerms().size() == 1) {

				IRI classIRI = rdfFactory.createIRI(((ValueConstant)(((ImmutableFunctionalTerm) object).getTerm(0))).getValue());
					// The term is actually a SubjectMap (class)
					//add class declaration to subject Map node
					sm.addClass(classIRI);

			} else {

				String predURIString = predUri.getIRIString();

				PredicateMap predM = templp.isPresent()?
				mfact.createPredicateMap(templp.get()):
				mfact.createPredicateMap(predUri);
				ObjectMap obm = null; PredicateObjectMap pom = null;

				org.semanticweb.owlapi.model.IRI propname = org.semanticweb.owlapi.model.IRI.create(predURIString);
				OWLDataFactory factory =  OWLManager.getOWLDataFactory();
				OWLObjectProperty objectProperty = factory.getOWLObjectProperty(propname);
				OWLDataProperty dataProperty = factory.getOWLDataProperty(propname);

				//add object declaration to predObj node
 				if (object instanceof Variable){
					if(ontology!= null && objectProperties.contains(objectProperty)){
                        //we create an rr:column
						obm = mfact.createObjectMap((((Variable) object).getName()));
						obm.setTermType(R2RMLVocabulary.iri);
					} else {
                        if (ontology != null && dataProperties.contains(dataProperty)) {

                            // column valued
                            obm = mfact.createObjectMap(((Variable) object).getName());
                            //set the datatype for the typed literal

                            //Set<OWLDataRange> ranges = dataProperty.getRanges(ontology);
                            Collection<OWLDataRange> ranges = EntitySearcher.getRanges(dataProperty, ontology);
                            //assign the datatype if present
                            if (ranges.size() == 1) {
                                org.semanticweb.owlapi.model.IRI dataRange = ranges.iterator().next().asOWLDatatype().getIRI();
                                obm.setDatatype(rdfFactory.createIRI(dataRange.toString()));
                            }

                        } else {
                            // column valued
                            obm = mfact.createObjectMap(((Variable) object).getName());
                        }
                    }
                    //we add the predicate object map in case of literal
					pom = mfact.createPredicateObjectMap(predM, obm);
					tm.addPredicateObjectMap(pom);
				} 
 				else if (object instanceof ImmutableFunctionalTerm) { //we create a template
					//check if uritemplate we create a template, in case of datatype with single variable we create a column
 					ImmutableFunctionalTerm o = (ImmutableFunctionalTerm) object;
 					Predicate objectPred = o.getFunctionSymbol();
					if (objectPred instanceof URITemplatePredicate || objectPred instanceof BNodePredicate) {
						IRI termType = objectPred instanceof URITemplatePredicate?
								R2RMLVocabulary.iri:
								R2RMLVocabulary.blankNode;

						ImmutableTerm objectTerm = o.getTerm(0);

						if(objectTerm instanceof Variable)
						{
							obm = mfact.createObjectMap(((Variable) objectTerm).getName());
						}
						else {

							String objectURI = URITemplates.getUriTemplateString(o, prefixmng);
							//add template object
							//statements.add(rdfFactory.createTriple(objNode, R2RMLVocabulary.template, rdfFactory.createLiteral(objectURI)));
							//obm.setTemplate(mfact.createTemplate(objectURI));
							obm = mfact.createObjectMap(mfact.createTemplate(objectURI));
						}
						obm.setTermType(termType);
					} else if (objectPred instanceof DatatypePredicate) {
						ImmutableTerm objectTerm = o.getTerm(0);
						
						if (objectTerm instanceof Variable) {

							// column valued
							obm = mfact.createObjectMap(((Variable) objectTerm).getName());
							//set the datatype for the typed literal
							obm.setTermType(R2RMLVocabulary.literal);
							
							RDFDatatype objectDatatype = ((DatatypePredicate) objectPred).getReturnedType();
							Optional<LanguageTag> optionalLangTag = objectDatatype.getLanguageTag();
							if (optionalLangTag.isPresent()) {
								obm.setLanguageTag(optionalLangTag.get().getFullString());
							}
							else {
								obm.setDatatype(objectDatatype.getIRI());
							}
						} else if (objectTerm instanceof Constant) {
							//statements.add(rdfFactory.createTriple(objNode, R2RMLVocabulary.constant, rdfFactory.createLiteral(((Constant) objectTerm).getValue())));
							//obm.setConstant(rdfFactory.createLiteral(((Constant) objectTerm).getValue()).stringValue());
							obm = mfact.createObjectMap(rdfFactory.createLiteral(((Constant) objectTerm).getValue(), rdfFactory.createIRI(objectPred.getName())));
							
						} else if(objectTerm instanceof ImmutableFunctionalTerm){
							ImmutableFunctionalTerm functionalObjectTerm = (ImmutableFunctionalTerm) objectTerm;
							
							StringBuilder sb = new StringBuilder();
							Predicate functionSymbol = functionalObjectTerm.getFunctionSymbol();
							
							if (functionSymbol == ExpressionOperation.CONCAT) { //concat
								ImmutableList<? extends ImmutableTerm> terms = functionalObjectTerm.getTerms();
								TargetQueryRenderer.getNestedConcats(sb, terms.get(0),terms.get(1));
								obm = mfact.createObjectMap(mfact.createTemplate(sb.toString()));
								obm.setTermType(R2RMLVocabulary.literal);

								RDFDatatype objectDatatype = ((DatatypePredicate) objectPred).getReturnedType();
								Optional<LanguageTag> optionalLangTag = objectDatatype.getLanguageTag();
								if (optionalLangTag.isPresent()) {
									obm.setLanguageTag(optionalLangTag.get().getFullString());
								}
							}
						}
						
					}
					pom = mfact.createPredicateObjectMap(predM, obm);
					tm.addPredicateObjectMap(pom);
				} else {
					System.out.println("FOUND UNKNOWN: "+object.toString());
				}
			}
			
		}

		return tm;
	}
	
	public OWLOntology getOntology() {
		return ontology;
	}
	
	public void setOntology(OWLOntology ontology) {
		this.ontology = ontology;
		if(ontology != null){
            //gets all object properties from the ontology
			objectProperties = ontology.getObjectPropertiesInSignature();

            //gets all data properties from the ontology
            dataProperties = ontology.getDataPropertiesInSignature();
		}
	}
	

}
