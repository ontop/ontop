package it.unibz.inf.ontop.spec.mapping.serializer;

import com.google.common.collect.ImmutableList;
import eu.optique.r2rml.api.R2RMLMappingManager;
import eu.optique.r2rml.api.binding.rdf4j.RDF4JR2RMLMappingManager;
import eu.optique.r2rml.api.model.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.type.LanguageTag;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.impl.SQLQueryImpl;
import it.unibz.inf.ontop.spec.mapping.parser.impl.R2RMLVocabulary;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.utils.IRIPrefixes;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
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
	private final RDFTermTypeConstant iriTypeConstant;
	private final RDFTermTypeConstant bnodeTypeConstant;
	private final RDFDatatype abstractLiteralType;

	OBDAMappingTransformer(TermFactory termFactory, TypeFactory typeFactory, RDF rdfFactory) {
        this("urn:", termFactory, typeFactory, rdfFactory);
	}

    OBDAMappingTransformer(String baseIRIString, TermFactory termFactory, TypeFactory typeFactory, RDF rdfFactory) {
        this.baseIRIString = baseIRIString;
		this.iriTypeConstant = termFactory.getRDFTermTypeConstant(typeFactory.getIRITermType());
		this.bnodeTypeConstant = termFactory.getRDFTermTypeConstant(typeFactory.getBlankNodeType());
		this.abstractLiteralType = typeFactory.getAbstractRDFSLiteral();
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
		String subjectTemplate =  IRIPrefixes.getUriTemplateString(uriTemplate, prefixmng);
		Template templs = mfact.createTemplate(subjectTemplate);
		SubjectMap sm = mfact.createSubjectMap(templs);

		TriplesMap tm = mfact.createTriplesMap(lt, sm, mainNode);
		
		//process target query
		for (TargetAtom func : tquery) {

			IRI predUri = null;

			Optional<Template> templp = Optional.empty();

			RDFAtomPredicate rdfAtomPredicate = Optional.of(func.getProjectionAtom().getPredicate())
					.filter(p -> p instanceof RDFAtomPredicate)
					.map(p -> (RDFAtomPredicate)p)
					.orElseThrow(() -> new MinorOntopInternalBugException(
							"A target atom in a OBDA mapping must use a RDFAtomPredicate"));

			//triple
			ImmutableFunctionalTerm predf = (ImmutableFunctionalTerm)func.getSubstitutedTerm(1);

			if (predf.getFunctionSymbol() instanceof RDFTermFunctionSymbol) {
					ImmutableTerm lexicalTerm = predf.getTerm(0);
					if (lexicalTerm instanceof DBConstant) { //fixed string
						predUri = rdfFactory.createIRI(((DBConstant) lexicalTerm).getValue());
					}
					else if (lexicalTerm instanceof Variable) {
						throw new RuntimeException("TODO: support the OBDA->R2RML conversion for variables (IRIs)");
					}
					else {
						//template
						predUri = rdfFactory.createIRI(IRIPrefixes.getUriTemplateString(predf, prefixmng));
                        templp = Optional.of(mfact.createTemplate(subjectTemplate));
					}
				}

			//term 0 is always the subject,  term 1 is the predicate, we check term 2 to have the object
            ImmutableTerm object = func.getSubstitutedTerm(2);

			Optional<IRI> objectClassIRI = rdfAtomPredicate.getClassIRI(func.getSubstitutedTerms());

			//if the class IRI is constant
			if (objectClassIRI.isPresent()) {
				// The term is actually a SubjectMap (class)
				//add class declaration to subject Map node
				sm.addClass(objectClassIRI.get());
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
					if (objectPred instanceof RDFTermFunctionSymbol) {
						RDFTermTypeConstant typeConstant =  Optional.of(o.getTerm(1))
								.filter(t -> t instanceof RDFTermTypeConstant)
								.map(t -> (RDFTermTypeConstant)t)
								.orElseThrow(() -> new MinorOntopInternalBugException(
										"Was expecting a RDFTermTypeConstant in the mapping assertion, not "
												+ o.getTerm(1)));
						ImmutableTerm lexicalTerm = o.getTerm(0);

						if (typeConstant.equals(iriTypeConstant)) {
							if (lexicalTerm instanceof Variable) {
								obm = mfact.createObjectMap(((Variable) lexicalTerm).getName());
							} else {
								String objectURI = IRIPrefixes.getUriTemplateString(o, prefixmng);
								obm = mfact.createObjectMap(mfact.createTemplate(objectURI));
							}
							obm.setTermType(R2RMLVocabulary.iri);
						}
						else if (typeConstant.equals(bnodeTypeConstant)) {
							if (lexicalTerm instanceof Variable) {
								obm = mfact.createObjectMap(((Variable) lexicalTerm).getName());
							} else {
								// TODO: check
								String objectURI = IRIPrefixes.getUriTemplateString(o, prefixmng);
								obm = mfact.createObjectMap(mfact.createTemplate(objectURI));
							}
							obm.setTermType(R2RMLVocabulary.blankNode);
						}
						else if (typeConstant.getRDFTermType().isA(abstractLiteralType)) {
							RDFDatatype datatype = (RDFDatatype) typeConstant.getRDFTermType();
							ImmutableTerm uncastLexicalTerm = uncast(lexicalTerm);

							if (uncastLexicalTerm instanceof Variable) {
								obm = mfact.createObjectMap(((Variable) uncastLexicalTerm).getName());
							}
							else if (uncastLexicalTerm instanceof Constant) {
								String lexicalString = ((Constant) uncastLexicalTerm).getValue();
								Literal literal = datatype.getLanguageTag()
										.map(lang -> rdfFactory.createLiteral(lexicalString, lang.getFullString()))
										.orElseGet(() -> rdfFactory.createLiteral(lexicalString, datatype.getIRI()));
								obm = mfact.createObjectMap(literal);
							}
							else {
								ImmutableFunctionalTerm functionalLexicalTerm = (ImmutableFunctionalTerm) uncastLexicalTerm;
								Predicate functionSymbol = functionalLexicalTerm.getFunctionSymbol();

								if (functionSymbol == ExpressionOperation.CONCAT) { //concat
									StringBuilder sb = new StringBuilder();
									ImmutableList<? extends ImmutableTerm> terms = functionalLexicalTerm.getTerms();
									TargetQueryRenderer.getNestedConcats(sb, terms.get(0), terms.get(1));
									obm = mfact.createObjectMap(mfact.createTemplate(sb.toString()));
								} else
									throw new MinorOntopInternalBugException("Unexpected function symbol: " + functionSymbol);
							}

							obm.setTermType(R2RMLVocabulary.literal);
							Optional<LanguageTag> optionalLangTag = datatype.getLanguageTag();
							if (optionalLangTag.isPresent())
								obm.setLanguageTag(optionalLangTag.get().getFullString());
							else
								obm.setDatatype(datatype.getIRI());
						}
						else {
							throw new MinorOntopInternalBugException("Unexpected typeConstant: " + typeConstant);
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

	private ImmutableTerm uncast(ImmutableTerm lexicalTerm) {
		return Optional.of(lexicalTerm)
				.filter(t -> t instanceof ImmutableFunctionalTerm)
				.map(t -> (ImmutableFunctionalTerm) t)
				.filter(t -> (t.getFunctionSymbol() instanceof CastFunctionSymbol)
						&& t.getFunctionSymbol().getArity() == 1)
				.map(t -> t.getTerm(0))
				.orElse(lexicalTerm);
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
