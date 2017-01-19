package it.unibz.inf.ontop.owlrefplatform.core;


import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.QuestCoreSettings;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.model.impl.TermUtils;
import it.unibz.inf.ontop.ontology.*;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.EQNormalizer;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.LinearInclusionDependencies;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.VocabularyValidator;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.*;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.DatalogUnfolder;
import it.unibz.inf.ontop.pivotalrepr.MetadataForQueryOptimization;
import it.unibz.inf.ontop.pivotalrepr.impl.MetadataForQueryOptimizationImpl;
import it.unibz.inf.ontop.sql.DatabaseRelationDefinition;
import it.unibz.inf.ontop.sql.RDBMetadata;
import it.unibz.inf.ontop.sql.Relation2DatalogPredicate;
import it.unibz.inf.ontop.sql.RelationID;
import it.unibz.inf.ontop.utils.IMapping2DatalogConverter;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.JSQLParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

public class QuestUnfolder {


	/*
	 * These are pattern matchers that will help transforming the URI's in
	 * queries into Functions, used by the SPARQL translator.
	 */
	private UriTemplateMatcher uriTemplateMatcher = new UriTemplateMatcher();

	protected List<CQIE> ufp; // for TESTS ONLY

	private static final Logger log = LoggerFactory.getLogger(QuestUnfolder.class);

	private ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> uniqueConstraints;
	private final IMapping2DatalogConverter mapping2DatalogConvertor;
	private MetadataForQueryOptimization metadataForQueryOptimization;

	private Set<Predicate> dataPropertiesAndClassesMapped = new HashSet<>();
	private Set<Predicate> objectPropertiesMapped = new HashSet<>();
	private ImmutableMultimap<Predicate, CQIE> mappingIndex;
	private ImmutableList<Predicate> extensionalPredicates;

	/** Davide> Whether to exclude the user-supplied predicates from the
	 *          TMapping procedure (that is, the mapping assertions for
	 *          those predicates should not be extended according to the
	 *          TBox hierarchies
	 */
	//private boolean applyExcludeFromTMappings = false;
	public QuestUnfolder(NativeQueryLanguageComponentFactory nativeQLFactory,
						 IMapping2DatalogConverter mapping2DatalogConvertor,
						 QuestCoreSettings preferences) throws Exception{
		this.nativeQLFactory = nativeQLFactory;
		this.mapping2DatalogConvertor = mapping2DatalogConvertor;
		this.preferences = preferences;
	}


    /**
	 * Setting up the unfolder and SQL generation
	 */
	public void setupInVirtualMode(DBMetadata metadata)
			throws SQLException, JSQLParserException, OBDAException {

		uniqueConstraints = metadata.extractUniqueConstraints();
		metadataForQueryOptimization = new MetadataForQueryOptimizationImpl(metadata, uniqueConstraints,
				uriTemplateMatcher);
		DatalogUnfolder unfolder = new DatalogUnfolder(unfoldingProgram, uniqueConstraints);
		mappingIndex = unfolder.getMappings();
		extensionalPredicates = unfolder.getExtensionalPredicates();
		
		this.ufp = unfoldingProgram;
	}

	public void setupInSemanticIndexMode(Collection<OBDAMappingAxiom> mappings,
										 TBoxReasoner reformulationReasoner,
										 DBMetadata metadata) throws OBDAException {


		List<CQIE> unfoldingProgram = mapping2DatalogConvertor.constructDatalogProgram(mappings,
				metadata);

		// this call is required to complete the T-mappings by rules taking account of
		// existential quantifiers and inverse roles
		unfoldingProgram = applyTMappings(unfoldingProgram, reformulationReasoner, false, metadata,
				TMappingExclusionConfig.empty());

		// Collecting URI templates
		uriTemplateMatcher = UriTemplateMatcher.create(unfoldingProgram);

		// Adding "triple(x,y,z)" mappings for support of unbounded
		// predicates and variables as class names (implemented in the
		// sparql translator)
		unfoldingProgram.addAll(generateTripleMappings(unfoldingProgram));

		log.debug("Final set of mappings: \n {}", Joiner.on("\n").join(unfoldingProgram));


		uniqueConstraints = metadata.extractUniqueConstraints();
		/**
		 * TODO: refactor this !!!
		 */
		metadataForQueryOptimization = new MetadataForQueryOptimizationImpl(metadata, uniqueConstraints, uriTemplateMatcher);
		unfolder = new DatalogUnfolder(unfoldingProgram, uniqueConstraints);

		this.ufp = unfoldingProgram;
	}

	/**
	 * Store information about owl:sameAs
	 */
	public void addSameAsMapping(List<CQIE> unfoldingProgram) throws OBDAException{


		MappingSameAs msa = new MappingSameAs(unfoldingProgram);

		dataPropertiesAndClassesMapped = msa.getDataPropertiesAndClassesWithSameAs();
		objectPropertiesMapped =  msa.getObjectPropertiesWithSameAs();


	}

	/**
	 * Specific to the Classic A-box mode!
	 */
	public void changeMappings(Collection<OBDAMappingAxiom> mappings, TBoxReasoner reformulationReasoner) {
		setupInSemanticIndexMode(mappings, reformulationReasoner, metadataForQueryOptimization.getDBMetadata());
	}


	public Set<Predicate> getSameAsDataPredicatesAndClasses(){

		return dataPropertiesAndClassesMapped;
	}

	public Set<Predicate> getSameAsObjectPredicates(){

		return objectPropertiesMapped;
	}

	public UriTemplateMatcher getUriTemplateMatcher() {
		return uriTemplateMatcher;
	}

	public ImmutableMultimap<Predicate, CQIE> getMappings(){
		return mappingIndex;
	}

	public ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> getUniqueConstraints() {
		return uniqueConstraints;
	}

	public ImmutableList<Predicate> getExtensionalPredicates() {
		return extensionalPredicates;
	}

	public MetadataForQueryOptimization getMetadataForQueryOptimization() {
		return metadataForQueryOptimization;
	}
}
