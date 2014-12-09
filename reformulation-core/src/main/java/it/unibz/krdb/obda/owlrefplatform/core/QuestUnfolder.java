package it.unibz.krdb.obda.owlrefplatform.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.URITemplatePredicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQContainmentCheckUnderLIDs;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.DBMetadataUtil;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.EQNormalizer;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.LinearInclusionDependencies;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.UriTemplateMatcher;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.MappingDataTypeRepair;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.TMappingProcessor;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.DatalogUnfolder;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.UnfoldingMechanism;
import it.unibz.krdb.obda.utils.Mapping2DatalogConverter;
import it.unibz.krdb.sql.DBMetadata;

public class QuestUnfolder {

	/* The active unfolding engine */
	private UnfoldingMechanism unfolder;

	/* As unfolding OBDAModel, but experimental */
	private List<CQIE> unfoldingProgram;

	/*
	 * These are pattern matchers that will help transforming the URI's in
	 * queries into Functions, used by the SPARQL translator.
	 */
	private UriTemplateMatcher uriTemplateMatcher = new UriTemplateMatcher();
	
	private static final Logger log = LoggerFactory.getLogger(QuestUnfolder.class);
	
	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	
	public QuestUnfolder(List<OBDAMappingAxiom> mappings, DBMetadata metadata) {
		unfoldingProgram = Mapping2DatalogConverter.constructDatalogProgram(mappings, metadata);
	}
		
	public int getRulesSize() {
		return unfoldingProgram.size();
	}
	
	/**
	 * Setting up the unfolder and SQL generation
	 */

	public void setupUnfolder(DBMetadata metadata) {
		
		// Collecting URI templates
		uriTemplateMatcher = createURITemplateMatcher(unfoldingProgram);

		// Adding "triple(x,y,z)" mappings for support of unbounded
		// predicates and variables as class names (implemented in the
		// sparql translator)
		unfoldingProgram.addAll(generateTripleMappings(unfoldingProgram));
		
		Map<Predicate, List<Integer>> pkeys = DBMetadata.extractPKs(metadata, unfoldingProgram);

        log.debug("Final set of mappings: \n {}", Joiner.on("\n").join(unfoldingProgram));
//		for(CQIE rule : unfoldingProgram){
//			log.debug("{}", rule);
//		}

		unfolder = new DatalogUnfolder(unfoldingProgram, pkeys);	
	}

	public void applyTMappings(TBoxReasoner reformulationReasoner, boolean full, DBMetadata metadata) throws OBDAException  {
		
		final long startTime = System.currentTimeMillis();

		// for eliminating redundancy from the unfolding program
		LinearInclusionDependencies foreignKeyRules = DBMetadataUtil.generateFKRules(metadata);	
		CQContainmentCheckUnderLIDs foreignKeyCQC = new CQContainmentCheckUnderLIDs(foreignKeyRules);
		
		unfoldingProgram = TMappingProcessor.getTMappings(unfoldingProgram, reformulationReasoner, full, foreignKeyCQC);

		int s0 = unfoldingProgram.size();
		Collections.sort(unfoldingProgram, CQCUtilities.ComparatorCQIE);
		CQCUtilities.removeContainedQueries(unfoldingProgram, foreignKeyCQC);		
		if (s0 != unfoldingProgram.size())
			System.err.println("CQC REMOVED: " + s0 + " - " + unfoldingProgram.size());
		
		final long endTime = System.currentTimeMillis();
		log.debug("TMapping size: {}", unfoldingProgram.size());
		log.debug("TMapping processing time: {} ms", (endTime - startTime));
	}

	/***
	 * Adding data typing on the mapping axioms.
	 */
	
	public void extendTypesWithMetadata(TBoxReasoner tBoxReasoner, DBMetadata metadata) throws OBDAException {

		MappingDataTypeRepair typeRepair = new MappingDataTypeRepair(metadata);
		typeRepair.insertDataTyping(unfoldingProgram, tBoxReasoner);
	}

	/***
	 * Adding NOT NULL conditions to the variables used in the head
	 * of all mappings to preserve SQL-RDF semantics
	 */
	
	public void addNOTNULLToMappings() {

		for (CQIE mapping : unfoldingProgram) {
			Set<Variable> headvars = mapping.getHead().getReferencedVariables();
			for (Variable var : headvars) {
				Function notnull = fac.getFunctionIsNotNull(var);
				   List<Function> body = mapping.getBody();
				if (!body.contains(notnull)) {
					body.add(notnull);
				}
			}
		}
	}
	
	/**
	 * Normalizing language tags. Making all LOWER CASE
	 */

	public void normalizeLanguageTagsinMappings() {
		for (CQIE mapping : unfoldingProgram) {
			Function head = mapping.getHead();
			for (Term term : head.getTerms()) {
				if (!(term instanceof Function)) {
					continue;
				}
				Function typedTerm = (Function) term;
				Predicate type = typedTerm.getFunctionSymbol();

				if (typedTerm.getTerms().size() != 2 || !type.getName().toString().equals(OBDAVocabulary.RDFS_LITERAL_URI))
					continue;
				/*
				 * changing the language, its always the second inner term
				 * (literal,lang)
				 */
				Term originalLangTag = typedTerm.getTerm(1);
				Term normalizedLangTag = null;

				if (originalLangTag instanceof Constant) {
					ValueConstant originalLangConstant = (ValueConstant) originalLangTag;
					normalizedLangTag = fac.getConstantLiteral(originalLangConstant.getValue().toLowerCase(), originalLangConstant.getType());
				} else {
					normalizedLangTag = originalLangTag;
				}
				typedTerm.setTerm(1, normalizedLangTag);
			}
		}
	}

	/**
	 * Normalizing equalities
	 */

	public void normalizeEqualities() {
		
		for (CQIE cq: unfoldingProgram)
			EQNormalizer.enforceEqualities(cq);
		
	}
	
	/***
	 * Adding ontology assertions (ABox) as rules (facts, head with no body).
	 */
	public void addClassAssertionsAsFacts(Iterable<ClassAssertion> assertions) {
		
		int count = 0;
		for (ClassAssertion ca : assertions) {
			// no blank nodes are supported here
			URIConstant c = (URIConstant)ca.getIndividual();
			Predicate p = ca.getConcept().getPredicate();
			Function head = fac.getFunction(p, 
							fac.getUriTemplate(fac.getConstantLiteral(c.getURI())));
			CQIE rule = fac.getCQIE(head, Collections.<Function> emptyList());
				
			unfoldingProgram.add(rule);
			count++;
		}
		log.debug("Appended {} class assertions from ontology as fact rules", count);
	}		
	
	public void addObjectPropertyAssertionsAsFacts(Iterable<ObjectPropertyAssertion> assertions) {
		
		int count = 0;
		for (ObjectPropertyAssertion pa : assertions) {
			// no blank nodes are supported here
			URIConstant s = (URIConstant)pa.getSubject();
			URIConstant o = (URIConstant)pa.getObject();
			Predicate p = pa.getProperty().getPredicate();
			Function head = fac.getFunction(p, 
							fac.getUriTemplate(fac.getConstantLiteral(s.getURI())), 
							fac.getUriTemplate(fac.getConstantLiteral(o.getURI())));
			CQIE rule = fac.getCQIE(head, Collections.<Function> emptyList());
				
			unfoldingProgram.add(rule);
			count++;
		}
		log.debug("Appended {} object property assertions as fact rules", count);
	}		
	
	public void addDataPropertyAssertionsAsFacts(Iterable<DataPropertyAssertion> assertions) {
		
//		int count = 0;
//		for (DataPropertyAssertion a : assertions) {
			// WE IGNORE DATA PROPERTY ASSERTIONS UNTIL THE NEXT RELEASE
//			DataPropertyAssertion ca = (DataPropertyAssertion) assertion;
//			ObjectConstant s = ca.getObject();
//			ValueConstant o = ca.getValue();
//			String typeURI = getURIType(o.getType());
//			Predicate p = ca.getPredicate();
//			Predicate urifuction = factory.getUriTemplatePredicate(1);
//			head = factory.getFunction(p, factory.getFunction(urifuction, s), factory.getFunction(factory.getPredicate(typeURI,1), o));
//			rule = factory.getCQIE(head, new LinkedList<Function>());
//		} 	
				
//		}
//		log.debug("Appended {} ABox assertions as fact rules", count);		
	}		
		

	
	
	private static UriTemplateMatcher createURITemplateMatcher(List<CQIE> unfoldingProgram) {

		HashSet<String> templateStrings = new HashSet<String>();
		
		UriTemplateMatcher uriTemplateMatcher  = new UriTemplateMatcher();

		for (CQIE mapping : unfoldingProgram) { 
			
			Function head = mapping.getHead();

			 // Collecting URI templates and making pattern matchers for them.
			for (Term term : head.getTerms()) {
				if (!(term instanceof Function)) {
					continue;
				}
				Function fun = (Function) term;
				if (!(fun.getFunctionSymbol() instanceof URITemplatePredicate)) {
					continue;
				}
				/*
				 * This is a URI function, so it can generate pattern matchers
				 * for the URIS. We have two cases, one where the arity is 1,
				 * and there is a constant/variable. <p> The second case is
				 * where the first element is a string template of the URI, and
				 * the rest of the terms are variables/constants
				 */
				if (fun.getTerms().size() == 1) {
					/*
					 * URI without template, we get it directly from the column
					 * of the table, and the function is only f(x)
					 */
					if (templateStrings.contains("(.+)")) {
						continue;
					}
					Function templateFunction = fac.getUriTemplate(fac.getVariable("x"));
					Pattern matcher = Pattern.compile("(.+)");
					uriTemplateMatcher.put(matcher, templateFunction);
					templateStrings.add("(.+)");
				} 
				else {
					ValueConstant template = (ValueConstant) fun.getTerms().get(0);
					String templateString = template.getValue();
					templateString = templateString.replace("{}", "(.+)");

					if (templateStrings.contains(templateString)) {
						continue;
					}
					Pattern mattcher = Pattern.compile(templateString);
					uriTemplateMatcher.put(mattcher, fun);
					templateStrings.add(templateString);
				}
			}
		}
		return uriTemplateMatcher;
	}
	
	
	public void updateSemanticIndexMappings(List<OBDAMappingAxiom> mappings, TBoxReasoner reformulationReasoner, DBMetadata metadata) throws OBDAException {

		unfoldingProgram = Mapping2DatalogConverter.constructDatalogProgram(mappings, metadata);

		// this call is required to complete the T-mappings by rules taking account of 
		// existential quantifiers and inverse roles
		applyTMappings(reformulationReasoner, false, metadata);
		
		setupUnfolder(metadata);

		log.debug("Mappings and unfolder have been updated after inserts to the semantic index DB");
	}

	
	/***
	 * Creates mappings with heads as "triple(x,y,z)" from mappings with binary
	 * and unary atoms"
	 *
	 * @return
	 */
	private static List<CQIE> generateTripleMappings(List<CQIE> unfoldingProgram) {
		List<CQIE> newmappings = new LinkedList<CQIE>();

		for (CQIE mapping : unfoldingProgram) {
			Function newhead = null;
			Function currenthead = mapping.getHead();
			if (currenthead.getArity() == 1) {
				/*
				 * head is Class(x) Forming head as triple(x,uri(rdf:type),
				 * uri(Class))
				 */
				Function rdfTypeConstant = fac.getUriTemplate(fac.getConstantLiteral(OBDAVocabulary.RDF_TYPE));

				String classname = currenthead.getFunctionSymbol().getName();
				Term classConstant = fac.getUriTemplate(fac.getConstantLiteral(classname));
				
				newhead = fac.getTripleAtom(currenthead.getTerm(0), rdfTypeConstant, classConstant);
			} 
			else if (currenthead.getArity() == 2) {
				/*
				 * head is Property(x,y) Forming head as triple(x,uri(Property),
				 * y)
				 */
				String propname = currenthead.getFunctionSymbol().getName();
				Function propConstant = fac.getUriTemplate(fac.getConstantLiteral(propname));
				
				newhead = fac.getTripleAtom(currenthead.getTerm(0), propConstant, currenthead.getTerm(1));
			}
			CQIE newmapping = fac.getCQIE(newhead, mapping.getBody());
			newmappings.add(newmapping);
		}
		return newmappings;
	}

	public UriTemplateMatcher getUriTemplateMatcher() {
		return uriTemplateMatcher;
	}
	
	public DatalogProgram unfold(DatalogProgram query) throws OBDAException {
		return unfolder.unfold(query, OBDAVocabulary.QUEST_QUERY);
	}


}
