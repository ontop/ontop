package it.unibz.krdb.obda.owlrefplatform.core;

import com.google.common.base.Joiner;
import com.google.common.collect.Multimap;
import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.TermUtils;
import it.unibz.krdb.obda.ontology.*;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.*;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.MappingDataTypeRepair;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.TMappingProcessor;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.DatalogUnfolder;
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class QuestUnfolder {

	/* The active unfolding engine */
	private DatalogUnfolder unfolder;

	private final DBMetadata metadata;
	private final Multimap<Predicate, List<Integer>> pkeys;
	private final CQContainmentCheckUnderLIDs foreignKeyCQC;
	
	/*
	 * These are pattern matchers that will help transforming the URI's in
	 * queries into Functions, used by the SPARQL translator.
	 */
	private UriTemplateMatcher uriTemplateMatcher = new UriTemplateMatcher();

	protected List<CQIE> ufp; // for TESTS ONLY
	
	private static final Logger log = LoggerFactory.getLogger(QuestUnfolder.class);
	
	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	public QuestUnfolder(DBMetadata metadata)  {

		this.metadata = metadata;
		this.pkeys = DBMetadataUtil.extractPKs(metadata);
		
		// for eliminating redundancy from the unfolding program
		LinearInclusionDependencies foreignKeyRules = DBMetadataUtil.generateFKRules(metadata);
		this.foreignKeyCQC = new CQContainmentCheckUnderLIDs(foreignKeyRules);
	}

	public void setupInVirtualMode(Collection<OBDAMappingAxiom> mappings,  Connection localConnection, VocabularyValidator vocabularyValidator, TBoxReasoner reformulationReasoner, Ontology inputOntology, TMappingExclusionConfig excludeFromTMappings, boolean queryingAnnotationsInOntology)
					throws SQLException, JSQLParserException, OBDAException {

		mappings = vocabularyValidator.replaceEquivalences(mappings);
		
		/** 
		 * Substitute select * with column names  (performs the operation `in place')
		 */
		preprocessProjection(mappings, metadata);

		/**
		 * Split the mapping (creates a new set of mappings)
		 */
		Collection<OBDAMappingAxiom> splittedMappings = MappingSplitter.splitMappings(mappings);
		
		/**
		 * Expand the meta mapping (creates a new set of mappings)
		 */
		MetaMappingExpander metaMappingExpander = new MetaMappingExpander(localConnection, metadata.getQuotedIDFactory());
		Collection<OBDAMappingAxiom> expandedMappings = metaMappingExpander.expand(splittedMappings);
		
		List<CQIE> unfoldingProgram = Mapping2DatalogConverter.constructDatalogProgram(expandedMappings, metadata);
		
		
		log.debug("Original mapping size: {}", unfoldingProgram.size());
		
		 // Normalizing language tags and equalities
		normalizeMappings(unfoldingProgram);

		// Apply TMappings
		unfoldingProgram = applyTMappings(unfoldingProgram, reformulationReasoner, true, excludeFromTMappings);

		// Adding data typing on the mapping axioms.
		 // Adding NOT NULL conditions to the variables used in the head
		 // of all mappings to preserve SQL-RDF semantics
		extendTypesWithMetadataAndAddNOTNULL(unfoldingProgram, reformulationReasoner, vocabularyValidator);

		// Adding ontology assertions (ABox) as rules (facts, head with no body).
		List<AnnotationAssertion> annotationAssertions;
		if (queryingAnnotationsInOntology) {
			annotationAssertions = inputOntology.getAnnotationAssertions();
		}
		else{
			annotationAssertions = Collections.emptyList();
		}
		addAssertionsAsFacts(unfoldingProgram, inputOntology.getClassAssertions(),
				inputOntology.getObjectPropertyAssertions(), inputOntology.getDataPropertyAssertions(), annotationAssertions);

		// Collecting URI templates
		uriTemplateMatcher = UriTemplateMatcher.create(unfoldingProgram);

		// Adding "triple(x,y,z)" mappings for support of unbounded
		// predicates and variables as class names (implemented in the
		// sparql translator)
		unfoldingProgram.addAll(generateTripleMappings(unfoldingProgram));

		if(log.isDebugEnabled()) {
			String finalMappings = Joiner.on("\n").join(unfoldingProgram);
			log.debug("Final set of mappings: \n {}", finalMappings);
		}
		
		unfolder = new DatalogUnfolder(unfoldingProgram, pkeys);
		
		this.ufp = unfoldingProgram;
	}
	
		
	/**
	 * Setting up the unfolder and SQL generation
	 * @param reformulationReasoner 
	 * @param mappings
	 * @throws OBDAException 
	 */

	public void setupInSemanticIndexMode(Collection<OBDAMappingAxiom> mappings, TBoxReasoner reformulationReasoner) throws OBDAException {
	
		List<CQIE> unfoldingProgram = Mapping2DatalogConverter.constructDatalogProgram(mappings, metadata);
		
		// this call is required to complete the T-mappings by rules taking account of 
		// existential quantifiers and inverse roles
		unfoldingProgram = applyTMappings(unfoldingProgram, reformulationReasoner, false, TMappingExclusionConfig.empty());
		
		// Collecting URI templates
		uriTemplateMatcher = UriTemplateMatcher.create(unfoldingProgram);

		// Adding "triple(x,y,z)" mappings for support of unbounded
		// predicates and variables as class names (implemented in the
		// sparql translator)
		unfoldingProgram.addAll(generateTripleMappings(unfoldingProgram));

		if(log.isDebugEnabled()) {
			String finalMappings = Joiner.on("\n").join(unfoldingProgram);
			log.debug("Final set of mappings: \n {}", finalMappings);
		}
		
		unfolder = new DatalogUnfolder(unfoldingProgram, pkeys);	
		
		this.ufp = unfoldingProgram;
	}

	
	private List<CQIE> applyTMappings(List<CQIE>  unfoldingProgram, TBoxReasoner reformulationReasoner, boolean full, TMappingExclusionConfig excludeFromTMappings) throws OBDAException  {
		
		final long startTime = System.currentTimeMillis();

		unfoldingProgram = TMappingProcessor.getTMappings(unfoldingProgram, reformulationReasoner, full,  foreignKeyCQC, excludeFromTMappings);
		
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
		
		return unfoldingProgram;
	}

	/***
	 * Adding data typing on the mapping axioms.
	 * Adding NOT NULL conditions to the variables used in the head
	 * of all mappings to preserve SQL-RDF semantics
	 */
	
	private void extendTypesWithMetadataAndAddNOTNULL(List<CQIE> unfoldingProgram, TBoxReasoner tboxReasoner, VocabularyValidator qvv) throws OBDAException {
		MappingDataTypeRepair typeRepair = new MappingDataTypeRepair(metadata, tboxReasoner, qvv);
		for (CQIE mapping : unfoldingProgram)  {
			typeRepair.insertDataTyping(mapping);

			Set<Variable> headvars = new HashSet<>();
			TermUtils.addReferencedVariablesTo(headvars, mapping.getHead());
			for (Variable var : headvars) {
				Function notnull = fac.getFunctionIsNotNull(var);
				   List<Function> body = mapping.getBody();
				   if (!body.contains(notnull)) 
					   body.add(notnull);
			}
		}
	}

	/**
	 * Normalize language tags (make them lower-case) and equalities 
	 * (remove them by replacing all equivalent terms with one representative)
	 */
	
	private void normalizeMappings(List<CQIE> unfoldingProgram) {
	
		// Normalizing language tags. Making all LOWER CASE

		for (CQIE mapping : unfoldingProgram) {
			Function head = mapping.getHead();
			for (Term term : head.getTerms()) {
				if (!(term instanceof Function)) 
					continue;
				
				Function typedTerm = (Function) term;
				if (typedTerm.getTerms().size() == 2 && typedTerm.getFunctionSymbol().getName().equals(OBDAVocabulary.RDFS_LITERAL_URI)) {
					 // changing the language, its always the second inner term (literal,lang)
					Term originalLangTag = typedTerm.getTerm(1);
					if (originalLangTag instanceof ValueConstant) {
						ValueConstant originalLangConstant = (ValueConstant) originalLangTag;
						Term normalizedLangTag = fac.getConstantLiteral(originalLangConstant.getValue().toLowerCase(), 
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
	
	/***
	 * Adding ontology assertions (ABox) as rules (facts, head with no body).
	 */
	private void addAssertionsAsFacts(List<CQIE> unfoldingProgram, Iterable<ClassAssertion> cas,
									  Iterable<ObjectPropertyAssertion> pas, Iterable<DataPropertyAssertion> das, List<AnnotationAssertion> aas) {
		
		int count = 0;
		for (ClassAssertion ca : cas) {
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
		
		count = 0;
		for (ObjectPropertyAssertion pa : pas) {
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
			
		
		count = 0;
		for (DataPropertyAssertion da : das) {
			// no blank nodes are supported here
			URIConstant s = (URIConstant)da.getSubject();
			ValueConstant o = da.getValue();
			Predicate p = da.getProperty().getPredicate();

			Function head;
			if(o.getLanguage()!=null){
				head = fac.getFunction(p, fac.getUriTemplate(fac.getConstantLiteral(s.getURI())), fac.getTypedTerm(fac.getConstantLiteral(o.getValue()),o.getLanguage()));
			}
			else {

				head = fac.getFunction(p, fac.getUriTemplate(fac.getConstantLiteral(s.getURI())), fac.getTypedTerm(o, o.getType()));
			}
			CQIE rule = fac.getCQIE(head, Collections.<Function> emptyList());

			unfoldingProgram.add(rule);
			count ++;
		}

		log.debug("Appended {} data property assertions as fact rules", count);

		count = 0;
		for (AnnotationAssertion aa : aas) {
			// no blank nodes are supported here

			URIConstant s = (URIConstant) aa.getSubject();
			Constant v = aa.getValue();
			Predicate p = aa.getProperty().getPredicate();

			Function head;
			if (v instanceof ValueConstant) {

				ValueConstant o = (ValueConstant) v;

				if (o.getLanguage() != null) {
					head = fac.getFunction(p, fac.getUriTemplate(fac.getConstantLiteral(s.getURI())), fac.getTypedTerm(fac.getConstantLiteral(o.getValue()), o.getLanguage()));
				} else {

					head = fac.getFunction(p, fac.getUriTemplate(fac.getConstantLiteral(s.getURI())), fac.getTypedTerm(o, o.getType()));
				}
			} else {

				URIConstant o = (URIConstant) v;
				head = fac.getFunction(p,
						fac.getUriTemplate(fac.getConstantLiteral(s.getURI())),
						fac.getUriTemplate(fac.getConstantLiteral(o.getURI())));


			}
			CQIE rule = fac.getCQIE(head, Collections.<Function>emptyList());

			unfoldingProgram.add(rule);
			count++;
		}

		log.debug("Appended {} annotation assertions as fact rules", count);
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
				// head is Class(x) Forming head as triple(x, uri(rdf:type), uri(Class))
				Function rdfTypeConstant = fac.getUriTemplate(fac.getConstantLiteral(OBDAVocabulary.RDF_TYPE));
				String classname = currenthead.getFunctionSymbol().getName();
				Term classConstant = fac.getUriTemplate(fac.getConstantLiteral(classname));
				newhead = fac.getTripleAtom(currenthead.getTerm(0), rdfTypeConstant, classConstant);
			} 
			else if (currenthead.getArity() == 2) {
				 //head is Property(x,y) Forming head as triple(x, uri(Property), y)
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
		return unfolder.unfold(query);
	}


	/***
	 * Expands a SELECT * into a SELECT with all columns implicit in the *
	 *
	 * @throws java.sql.SQLException
	 */
	private static void preprocessProjection(Collection<OBDAMappingAxiom> mappings, DBMetadata metadata) throws SQLException {

		for (OBDAMappingAxiom axiom : mappings) {
			try {
				String sourceString = axiom.getSourceQuery().toString();

				Select select = (Select) CCJSqlParserUtil.parse(sourceString);

				List<Function> targetQuery = axiom.getTargetQuery();
				Set<Variable> variables = new HashSet<>();
				for (Function atom : targetQuery) 
					TermUtils.addReferencedVariablesTo(variables, atom);
				
				PreprocessProjection ps = new PreprocessProjection(metadata);
				String query = ps.getMappingQuery(select, variables);
				axiom.setSourceQuery(fac.getSQLQuery(query));
			} 
			catch (JSQLParserException e) {
				log.debug("SQL Query cannot be preprocessed by the parser");
			}
		}
	}
}
