package it.unibz.krdb.obda.owlrefplatform.core;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
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
import it.unibz.krdb.sql.Attribute;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.RelationID;
import it.unibz.krdb.sql.Relation2DatalogPredicate;
import it.unibz.krdb.sql.DatabaseRelationDefinition;
import it.unibz.krdb.sql.UniqueConstraint;
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
	private UnfoldingMechanism unfolder;

	/* As unfolding OBDAModel, but experimental */
	private List<CQIE> unfoldingProgram;

	private final DBMetadata metadata;
	
	/*
	 * These are pattern matchers that will help transforming the URI's in
	 * queries into Functions, used by the SPARQL translator.
	 */
	private UriTemplateMatcher uriTemplateMatcher = new UriTemplateMatcher();
	
	private static final Logger log = LoggerFactory.getLogger(QuestUnfolder.class);
	
	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	
	
	/**
	 * @throws SQLException 
	 * @throws JSQLParserException 
	 */
	public QuestUnfolder(Collection<OBDAMappingAxiom> mappings, DBMetadata metadata,  Connection localConnection) throws SQLException, JSQLParserException {

		this.metadata = metadata;
		
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
		
		unfoldingProgram = Mapping2DatalogConverter.constructDatalogProgram(expandedMappings, metadata);
	}


	public int getRulesSize() {
		return unfoldingProgram.size();
	}

	// USED ONLY IN TESTS
	@Deprecated
	public List<CQIE> getRules() {
		return unfoldingProgram;
	}
	
	/**
	 * Setting up the unfolder and SQL generation
	 */

	public void setupUnfolder() {
		
		// Collecting URI templates
		uriTemplateMatcher = UriTemplateMatcher.create(unfoldingProgram);

		// Adding "triple(x,y,z)" mappings for support of unbounded
		// predicates and variables as class names (implemented in the
		// sparql translator)
		unfoldingProgram.addAll(generateTripleMappings(unfoldingProgram));

        log.debug("Final set of mappings: \n {}", Joiner.on("\n").join(unfoldingProgram));
		
		unfolder = createDatalogUnfolder(unfoldingProgram, metadata);	
	}

	public static DatalogUnfolder createDatalogUnfolder(List<CQIE> unfoldingProgram, DBMetadata metadata) {
		Multimap<Predicate, List<Integer>> pkeys = extractPKs(metadata, unfoldingProgram);
		return new DatalogUnfolder(unfoldingProgram, pkeys);	
	}
	
	/***
	 * Generates a map for each predicate in the body of the rules in 'program'
	 * that contains the Primary Key data for the predicates obtained from the
	 * info in the metadata.
     *
     * It also returns the columns with unique constraints
     *
     * For instance, Given the table definition
     *   Tab0[col1:pk, col2:pk, col3, col4:unique, col5:unique],
     *
     * The methods will return the following Multimap:
     *  { Tab0 -> { [col1, col2], [col4], [col5] } }
     *
	 * 
	 * @param metadata
	 * @param program
	 */
	private static Multimap<Predicate, List<Integer>> extractPKs(DBMetadata metadata,
			List<CQIE> program) {
		
		Multimap<Predicate, List<Integer>> pkeys = HashMultimap.create();
		for (CQIE mapping : program) {
			for (Function newatom : mapping.getBody()) {
				Predicate newAtomPredicate = newatom.getFunctionSymbol();
				if (newAtomPredicate instanceof BooleanOperationPredicate) 
					continue;
				
				if (pkeys.containsKey(newAtomPredicate))
					continue;
				
				RelationID newAtomName = Relation2DatalogPredicate.createRelationFromPredicateName(newAtomPredicate);
				DatabaseRelationDefinition def = metadata.getDatabaseRelation(newAtomName);
				if (def != null) {
					// primary key and unique constraints
					for (UniqueConstraint uc : def.getUniqueConstraints()) {
						List<Integer> pkeyIdx = new ArrayList<>(uc.getAttributes().size());
						for (Attribute att : def.getAttributes()) {
							if (uc.getAttributes().contains(att)) 
								pkeyIdx.add(att.getIndex());
						}
						pkeys.put(newAtomPredicate, pkeyIdx);
					}
				}
			}
		}
		return pkeys;
	}
	
	
	public void applyTMappings(TBoxReasoner reformulationReasoner, boolean full, TMappingExclusionConfig excludeFromTMappings) throws OBDAException  {
		
		final long startTime = System.currentTimeMillis();

		// for eliminating redundancy from the unfolding program
		LinearInclusionDependencies foreignKeyRules = DBMetadataUtil.generateFKRules(metadata);
		CQContainmentCheckUnderLIDs foreignKeyCQC = new CQContainmentCheckUnderLIDs(foreignKeyRules);

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
	}

	/***
	 * Adding data typing on the mapping axioms.
	 */
	
	public void extendTypesWithMetadata(TBoxReasoner tboxReasoner, VocabularyValidator qvv, DBMetadata metadata) throws OBDAException {
		MappingDataTypeRepair typeRepair = new MappingDataTypeRepair(metadata, tboxReasoner, qvv);
		for (CQIE mapping : unfoldingProgram) 
			typeRepair.insertDataTyping(mapping);
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
				   if (!body.contains(notnull)) 
					   body.add(notnull);
			}
		}
	}
	
	/**
	 * Normalize language tags (make them lower-case) and equalities 
	 * (remove them by replacing all equivalent terms with one representative)
	 */
	
	public void normalizeMappings() {
	
		// Normalizing language tags. Making all LOWER CASE

		for (CQIE mapping : unfoldingProgram) {
			Function head = mapping.getHead();
			for (Term term : head.getTerms()) {
				if (!(term instanceof Function)) 
					continue;
				
				Function typedTerm = (Function) term;
				Predicate type = typedTerm.getFunctionSymbol();

				if (typedTerm.getTerms().size() != 2 || !type.getName().equals(OBDAVocabulary.RDFS_LITERAL_URI))
					continue;
				
				 // changing the language, its always the second inner term (literal,lang)
				Term originalLangTag = typedTerm.getTerm(1);
				if (originalLangTag instanceof ValueConstant) {
					ValueConstant originalLangConstant = (ValueConstant) originalLangTag;
					Term normalizedLangTag = fac.getConstantLiteral(originalLangConstant.getValue().toLowerCase(), originalLangConstant.getType());
					typedTerm.setTerm(1, normalizedLangTag);
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
		

	
	
	
	
	public void updateSemanticIndexMappings(Collection<OBDAMappingAxiom> mappings, TBoxReasoner reformulationReasoner) throws OBDAException {

		unfoldingProgram = Mapping2DatalogConverter.constructDatalogProgram(mappings, metadata);
		
		// this call is required to complete the T-mappings by rules taking account of 
		// existential quantifiers and inverse roles
		applyTMappings(reformulationReasoner, false, TMappingExclusionConfig.empty());
		
		setupUnfolder();

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
