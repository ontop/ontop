package org.semanticweb.ontop.owlrefplatform.core;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.owlrefplatform.core.abox.ABoxToFactRuleConverter;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.CQCUtilities;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.DBMetadataUtil;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.DatalogNormalizer;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.UriTemplateMatcher;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import org.semanticweb.ontop.owlrefplatform.core.mappingprocessing.MappingDataTypeRepair;
import org.semanticweb.ontop.owlrefplatform.core.mappingprocessing.TMappingProcessor;
import org.semanticweb.ontop.owlrefplatform.core.unfolding.DatalogUnfolder;
import org.semanticweb.ontop.owlrefplatform.core.unfolding.UnfoldingMechanism;
import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.utils.Mapping2DatalogConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

public class QuestUnfolder {

	/* The active unfolding engine */
	private UnfoldingMechanism unfolder;

	private DBMetadata metadata;
	
	/* As unfolding OBDAModel, but experimental */
	private DatalogProgram unfoldingProgram;

     /* The list of predicates that are defined in multiple mappings */

    private  Multimap<Predicate,Integer> multiplePredIdx = ArrayListMultimap.create();
	/*
	 * These are pattern matchers that will help transforming the URI's in
	 * queries into Functions, used by the SPARQL translator.
	 */
	private final UriTemplateMatcher uriTemplateMatcher = new UriTemplateMatcher();

	private final HashSet<String> templateStrings = new HashSet<String>();
	
	
	private static final Logger log = LoggerFactory.getLogger(QuestUnfolder.class);
	
	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();




	public QuestUnfolder(List<OBDAMappingAxiom> mappings, DBMetadata metadata)
	{
		this.metadata = metadata;
		
		Mapping2DatalogConverter analyzer = new Mapping2DatalogConverter(mappings, metadata);

		unfoldingProgram = analyzer.constructDatalogProgram();
	}
	
	public List<CQIE> getRules() {
		return unfoldingProgram.getRules();
	}
	
	/**
	 * Setting up the unfolder and SQL generation
	 */

	public void setupUnfolder() {
		
		// Collecting URI templates
		generateURITemplateMatchers();

		// Adding "triple(x,y,z)" mappings for support of unbounded
		// predicates and variables as class names (implemented in the
		// sparql translator)
		unfoldingProgram.appendRule(generateTripleMappings());		
		
		Map<Predicate, List<Integer>> pkeys = DBMetadata.extractPKs(metadata, unfoldingProgram);

        log.debug("Final set of mappings: \n{}", unfoldingProgram);

        unfolder = new DatalogUnfolder(unfoldingProgram, pkeys, multiplePredIdx);




    }

    public Multimap<Predicate, Integer> processMultipleTemplatePredicates() {

        multiplePredIdx = unfolder.processMultipleTemplatePredicates(unfoldingProgram);

        return multiplePredIdx;


    }
	public void applyTMappings(boolean optimizeMap, TBoxReasoner reformulationReasoner, boolean full) throws OBDAException  {
		
		final long startTime = System.currentTimeMillis();

		TMappingProcessor tmappingProc = new TMappingProcessor(reformulationReasoner, optimizeMap);
		unfoldingProgram = tmappingProc.getTMappings(unfoldingProgram, full);

		/*
		 * Eliminating redundancy from the unfolding program
		 */
		unfoldingProgram = DatalogNormalizer.enforceEqualities(unfoldingProgram);
		List<CQIE> foreignKeyRules = DBMetadataUtil.generateFKRules(metadata);

		if (optimizeMap) {
			CQCUtilities.removeContainedQueriesSorted(unfoldingProgram, true);
			unfoldingProgram = CQCUtilities.removeContainedQueriesSorted(unfoldingProgram, true, foreignKeyRules);
		}

		final long endTime = System.currentTimeMillis();

		log.debug("TMapping size: {}", unfoldingProgram.getRules().size());
		log.debug("TMapping processing time: {} ms", (endTime - startTime));
	}

	/***
	 * Adding data typing on the mapping axioms.
	 */
	
	public void extendTypesWithMetadata() throws OBDAException {
		MappingDataTypeRepair typeRepair = new MappingDataTypeRepair(metadata);
		typeRepair.insertDataTyping(unfoldingProgram);
	}

	/***
	 * Adding NOT NULL conditions to the variables used in the head
	 * of all mappings to preserve SQL-RDF semantics
	 */
	
	public void addNOTNULLToMappings() {

		for (CQIE mapping : unfoldingProgram.getRules()) {
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
		for (CQIE mapping : unfoldingProgram.getRules()) {
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
		unfoldingProgram = DatalogNormalizer.enforceEqualities(unfoldingProgram);
	}
	
	/***
	 * Adding ontology assertions (ABox) as rules (facts, head with no body).
	 * @param assertions
	 */
	public void addABoxAssertionsAsFacts(Iterable<Assertion> assertions) {
		
		int count = 0;
		for (Assertion a : assertions) {
			CQIE fact = ABoxToFactRuleConverter.getRule(a);
			if (fact != null) {
				unfoldingProgram.appendRule(fact);
				count++;
			}
		}
		log.debug("Appended {} ABox assertions as fact rules", count);		
	}		
	

	
	
	public void generateURITemplateMatchers() {

		templateStrings.clear();
		uriTemplateMatcher.clear();

		for (CQIE mapping : unfoldingProgram.getRules()) { // int i = 0; i < unfoldingProgram.getRules().size(); i++) {

			// Looking for mappings with exactly 2 data atoms
			// CQIE mapping = unfoldingProgram.getRules().get(i);
			Function head = mapping.getHead();

			/*
			 * Collecting URI templates and making pattern matchers for them.
			 */
			for (Term term : head.getTerms()) {
				if (!(term instanceof Function)) {
					continue;
				}
				Function fun = (Function) term;
				if (!(fun.getFunctionSymbol().toString().equals(OBDAVocabulary.QUEST_URI))) {
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
					 * URI without tempalte, we get it direclty from the column
					 * of the table, and the function is only f(x)
					 */
					if (templateStrings.contains("(.+)")) {
						continue;
					}
					Function templateFunction = fac.getFunction(fac.getUriTemplatePredicate(1), fac.getVariable("x"));
					Pattern matcher = Pattern.compile("(.+)");
					uriTemplateMatcher.put(matcher, templateFunction);
					templateStrings.add("(.+)");
				} else {
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
	}
	
	
	public void updateSemanticIndexMappings(List<OBDAMappingAxiom> mappings, TBoxReasoner reformulationReasoner) throws OBDAException {

		Mapping2DatalogConverter analyzer = new Mapping2DatalogConverter(mappings, metadata);

		unfoldingProgram = analyzer.constructDatalogProgram();

		applyTMappings(true, reformulationReasoner, false);
		
		setupUnfolder();

		log.debug("Final set of mappings: \n{}", unfoldingProgram);	
		log.debug("Mappings and unfolder have been updated after inserts to the semantic index DB");
	}

	
	/***
	 * Creates mappings with heads as "triple(x,y,z)" from mappings with binary
	 * and unary atoms"
	 *
	 * @return
	 */
	private List<CQIE> generateTripleMappings() {
		List<CQIE> newmappings = new LinkedList<CQIE>();

		for (CQIE mapping : unfoldingProgram.getRules()) {
			Function newhead = null;
			Function currenthead = mapping.getHead();
			Predicate pred = OBDAVocabulary.QUEST_TRIPLE_PRED;
			LinkedList<Term> terms = new LinkedList<Term>();
			if (currenthead.getArity() == 1) {
				/*
				 * head is Class(x) Forming head as triple(x,uri(rdf:type),
				 * uri(Class))
				 */
				terms.add(currenthead.getTerm(0));
				Function rdfTypeConstant = fac.getFunction(fac.getUriTemplatePredicate(1),
						fac.getConstantLiteral(OBDAVocabulary.RDF_TYPE));
				terms.add(rdfTypeConstant);

				String classname = currenthead.getFunctionSymbol().getName();
				terms.add(fac.getFunction(fac.getUriTemplatePredicate(1), fac.getConstantLiteral(classname)));
				newhead = fac.getFunction(pred, terms);

			} else if (currenthead.getArity() == 2) {
				/*
				 * head is Property(x,y) Forming head as triple(x,uri(Property),
				 * y)
				 */
				terms.add(currenthead.getTerm(0));

				String propname = currenthead.getFunctionSymbol().getName();
				Function propconstant = fac.getFunction(fac.getUriTemplatePredicate(1), fac.getConstantLiteral(propname));
				terms.add(propconstant);
				terms.add(currenthead.getTerm(1));
				newhead = fac.getFunction(pred, terms);
			}
			CQIE newmapping = fac.getCQIE(newhead, mapping.getBody());
			newmappings.add(newmapping);
		}
		return newmappings;
	}

	public UriTemplateMatcher getUriTemplateMatcher() {
		return uriTemplateMatcher;
	}
	
	public DatalogProgram unfold(DatalogProgram query, String targetPredicate) throws OBDAException {
		return unfolder.unfold(query, targetPredicate);
	}


    public UnfoldingMechanism getDatalogUnfolder(){
        return unfolder;
    }
}
