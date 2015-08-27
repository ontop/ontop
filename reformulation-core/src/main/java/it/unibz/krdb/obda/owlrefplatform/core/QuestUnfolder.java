package it.unibz.krdb.obda.owlrefplatform.core;

import com.google.common.base.Joiner;
import com.google.common.collect.Multimap;
import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.TermUtils;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.*;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.MappingDataTypeRepair;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.TMappingProcessor;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.DatalogUnfolder;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.UnfoldingMechanism;
import it.unibz.krdb.obda.parser.PreprocessProjection;
import it.unibz.krdb.obda.utils.Mapping2DatalogConverter;
import it.unibz.krdb.obda.utils.MappingSplitter;
import it.unibz.krdb.obda.utils.MetaMappingExpander;
import it.unibz.krdb.sql.DBMetadata;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

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
	
	/** Davide> Exclude specific predicates from T-Mapping approach **/
	private final TMappingExclusionConfig excludeFromTMappings;
	
	/** Davide> Whether to exclude the user-supplied predicates from the
	 *          TMapping procedure (that is, the mapping assertions for 
	 *          those predicates should not be extended according to the 
	 *          TBox hierarchies
	 */
	//private boolean applyExcludeFromTMappings = false;
	public QuestUnfolder(OBDAModel unfoldingOBDAModel, DBMetadata metadata,  Connection localConnection, URI sourceId) throws Exception{

		/** Substitute select * with column names **/
		preprocessProjection(unfoldingOBDAModel, sourceId, metadata);

		/**
		 * Split the mapping
		 */
		MappingSplitter.splitMappings(unfoldingOBDAModel, sourceId);

		/**
		 * Expand the meta mapping
		 */
		MetaMappingExpander metaMappingExpander = new MetaMappingExpander(localConnection);
		metaMappingExpander.expand(unfoldingOBDAModel, sourceId);

		List<OBDAMappingAxiom> mappings = unfoldingOBDAModel.getMappings(sourceId);
		unfoldingProgram = Mapping2DatalogConverter.constructDatalogProgram(mappings, metadata);

		this.excludeFromTMappings = TMappingExclusionConfig.empty();
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
	public QuestUnfolder(OBDAModel unfoldingOBDAModel, DBMetadata metadata, Connection localConnection, URI sourceId, TMappingExclusionConfig excludeFromTMappings)  throws Exception{
		/** Substitute select * with column names **/
		preprocessProjection(unfoldingOBDAModel, sourceId, metadata);

		/**
		 * Split the mapping
		 */
		MappingSplitter.splitMappings(unfoldingOBDAModel, sourceId);

		/**
		 * Expand the meta mapping
		 */
		MetaMappingExpander metaMappingExpander = new MetaMappingExpander(localConnection);
		metaMappingExpander.expand(unfoldingOBDAModel, sourceId);

		List<OBDAMappingAxiom> mappings = unfoldingOBDAModel.getMappings(sourceId);
		unfoldingProgram = Mapping2DatalogConverter.constructDatalogProgram(mappings, metadata);

		this.excludeFromTMappings = excludeFromTMappings;
	}


	public int getRulesSize() {
		return unfoldingProgram.size();
	}

	@Deprecated
	public List<CQIE> getRules() {
		return unfoldingProgram;
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
		
		Multimap<Predicate, List<Integer>> pkeys = DBMetadata.extractPKs(metadata, unfoldingProgram);

        log.debug("Final set of mappings: \n {}", Joiner.on("\n").join(unfoldingProgram));
//		for(CQIE rule : unfoldingProgram){
//			log.debug("{}", rule);
//		}

		unfolder = new DatalogUnfolder(unfoldingProgram, pkeys);	
	}

	public void applyTMappings(TBoxReasoner reformulationReasoner, boolean full, DBMetadata metadata, TMappingExclusionConfig excludeFromTMappings) throws OBDAException  {
		
		final long startTime = System.currentTimeMillis();

		// for eliminating redundancy from the unfolding program
		LinearInclusionDependencies foreignKeyRules = DBMetadataUtil.generateFKRules(metadata);
		CQContainmentCheckUnderLIDs foreignKeyCQC = new CQContainmentCheckUnderLIDs(foreignKeyRules);
		// Davide> Here now I put another TMappingProcessor taking
		//         also a list of Predicates as input, that represents
		//         what needs to be excluded from the T-Mappings
		//if( applyExcludeFromTMappings )
			unfoldingProgram = TMappingProcessor.getTMappings(unfoldingProgram, reformulationReasoner, full,  foreignKeyCQC, excludeFromTMappings);
		//else
		//	unfoldingProgram = TMappingProcessor.getTMappings(unfoldingProgram, reformulationReasoner, full);
		
		// Eliminating redundancy from the unfolding program
		// TODO: move the foreign-key optimisation inside t-mapping generation 
		//              -- at this point it has little effect
		
/*		
		int s0 = unfoldingProgram.size();
		Collections.sort(unfoldingProgram, CQCUtilities.ComparatorCQIE);
		CQCUtilities.removeContainedQueries(unfoldingProgram, foreignKeyCQC);		
		if (s0 != unfoldingProgram.size())
			System.err.println("CQC REMOVED: " + s0 + " - " + unfoldingProgram.size());
*/
		
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
			Set<Variable> headvars = new HashSet<>();
			TermUtils.addReferencedVariablesTo(headvars, mapping.getHead());
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
		applyTMappings(reformulationReasoner, false, metadata, TMappingExclusionConfig.empty());
		
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


	/***
	 * Expands a SELECT * into a SELECT with all columns implicit in the *
	 *
	 * @throws java.sql.SQLException
	 */
	private void preprocessProjection(OBDAModel unfoldingOBDAModel, URI sourceId, DBMetadata metadata) throws SQLException {

		List<OBDAMappingAxiom> mappings = unfoldingOBDAModel.getMappings(sourceId);


		for (OBDAMappingAxiom axiom : mappings) {
			String sourceString = axiom.getSourceQuery().toString();

			OBDAQuery targetQuery= axiom.getTargetQuery();

			Select select = null;
			try {
				select = (Select) CCJSqlParserUtil.parse(sourceString);

				Set<Variable> variables = ((CQIE) targetQuery).getReferencedVariables();
				PreprocessProjection ps = new PreprocessProjection(metadata);
				String query = ps.getMappingQuery(select, variables);
				axiom.setSourceQuery(fac.getSQLQuery(query));

			} catch (JSQLParserException e) {
				log.debug("SQL Query cannot be preprocessed by the parser");


			}
//
		}
	}




}
