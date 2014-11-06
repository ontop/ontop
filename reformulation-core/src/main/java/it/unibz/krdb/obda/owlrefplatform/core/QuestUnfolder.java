package it.unibz.krdb.obda.owlrefplatform.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import it.unibz.krdb.config.tmappings.parser.TMappingExclusionConfiguration;
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
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.PropertyAssertion;
import it.unibz.krdb.obda.owlrefplatform.core.abox.ABoxToFactRuleConverter;
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

	private final DBMetadata metadata;
	private final Mapping2DatalogConverter analyzer;
	
	/* As unfolding OBDAModel, but experimental */
	private List<CQIE> unfoldingProgram;

	/*
	 * These are pattern matchers that will help transforming the URI's in
	 * queries into Functions, used by the SPARQL translator.
	 */
	private final UriTemplateMatcher uriTemplateMatcher = new UriTemplateMatcher();

	private final HashSet<String> templateStrings = new HashSet<String>();
	
	
	private static final Logger log = LoggerFactory.getLogger(QuestUnfolder.class);
	
	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	
	/** Davide> Exclude specific predicates from T-Mapping approach **/
	private final TMappingExclusionConfiguration excludeFromTMappings;
	
	/** Davide> Whether to exclude the user-supplied predicates from the
	 *          TMapping procedure (that is, the mapping assertions for 
	 *          those predicates should not be extended according to the 
	 *          TBox hierarchies
	 */
	//private boolean applyExcludeFromTMappings = false;

	public QuestUnfolder(List<OBDAMappingAxiom> mappings, DBMetadata metadata)
	{
		this.metadata = metadata;	
		
		analyzer = new Mapping2DatalogConverter(metadata);

		unfoldingProgram = analyzer.constructDatalogProgram(mappings);

        this.excludeFromTMappings = TMappingExclusionConfiguration.empty();

    }
	
	/**
	 * The extra parameter <b>excludeFromTMappings</b> defines a list
	 * of predicates for which the T-Mappings procedure should be 
	 * disabled.
	 *  
	 * @author Davide
	 * @param mappings
	 * @param metadata
	 * @param analyzer
	 * @param excludeFromTMappings
	 */
	public QuestUnfolder(List<OBDAMappingAxiom> mappings, DBMetadata metadata, Mapping2DatalogConverter analyzer, TMappingExclusionConfiguration excludeFromTMappings)
	{
		this.metadata = metadata;
		
		this.analyzer = new Mapping2DatalogConverter(metadata);

		unfoldingProgram = analyzer.constructDatalogProgram(mappings);
		
		// Davide>T-Mappings handling
		this.excludeFromTMappings = excludeFromTMappings;
	//	this.applyExcludeFromTMappings = true;
	}
	
	public List<CQIE> getRules() {
		return unfoldingProgram;
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
		unfoldingProgram.addAll(generateTripleMappings());
		
		Map<Predicate, List<Integer>> pkeys = DBMetadata.extractPKs(metadata, unfoldingProgram);

        log.debug("Final set of mappings: \n{}", unfoldingProgram);

		unfolder = new DatalogUnfolder(unfoldingProgram, pkeys);	
	}

	public void applyTMappings(TBoxReasoner reformulationReasoner, boolean full, TMappingExclusionConfiguration excludeFromTMappings) throws OBDAException  {
		
		final long startTime = System.currentTimeMillis();
		
		// Davide> Here now I put another TMappingProcessor taking
		//         also a list of Predicates as input, that represents
		//         what needs to be excluded from the T-Mappings
		//if( applyExcludeFromTMappings )
			unfoldingProgram = TMappingProcessor.getTMappings(unfoldingProgram, reformulationReasoner, full, excludeFromTMappings);
		//else
		//	unfoldingProgram = TMappingProcessor.getTMappings(unfoldingProgram, reformulationReasoner, full);
		
		// Eliminating redundancy from the unfolding program
		// TODO: move the foreign-key optimisation inside t-mapping generation 
		//              -- at this point it has little effect
		LinearInclusionDependencies foreignKeyRules = DBMetadataUtil.generateFKRules(metadata);	
		CQContainmentCheckUnderLIDs foreignKeyCQC = new CQContainmentCheckUnderLIDs(foreignKeyRules);

		Collections.sort(unfoldingProgram, CQCUtilities.ComparatorCQIE);
		CQCUtilities.removeContainedQueries(unfoldingProgram, foreignKeyCQC);

		final long endTime = System.currentTimeMillis();
		log.debug("TMapping size: {}", unfoldingProgram.size());
		log.debug("TMapping processing time: {} ms", (endTime - startTime));
	}

	/***
	 * Adding data typing on the mapping axioms.
	 */
	
	public void extendTypesWithMetadata(TBoxReasoner tBoxReasoner) throws OBDAException {

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
	public <T extends Assertion>  void addABoxAssertionsAsFacts(Iterable<T> assertions) {
		
		int count = 0;
		for (T a : assertions) {
			CQIE fact;
			if (a instanceof ClassAssertion)
				fact = ABoxToFactRuleConverter.getRule((ClassAssertion)a);
			else
				fact = ABoxToFactRuleConverter.getRule((PropertyAssertion)a);
			if (fact != null) {
				unfoldingProgram.add(fact);
				count++;
			}
		}
		log.debug("Appended {} ABox assertions as fact rules", count);		
	}		
		

	
	
	private void generateURITemplateMatchers() {

		templateStrings.clear();
		uriTemplateMatcher.clear();

		for (CQIE mapping : unfoldingProgram) { // int i = 0; i < unfoldingProgram.getRules().size(); i++) {

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

		unfoldingProgram = analyzer.constructDatalogProgram(mappings);


		applyTMappings(/*true, */reformulationReasoner, false, TMappingExclusionConfiguration.empty());
		
		setupUnfolder();

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

		for (CQIE mapping : unfoldingProgram) {
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


}
