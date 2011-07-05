package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Statement;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DLLiterOntology;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.EvaluationEngine;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryVocabularyValidator;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SourceQueryGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.UnfoldingMechanism;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Is the currently used technique wrapper of our reformulation platform
 * reasoner
 * 
 * @author Manfred Gerstgrasser
 * 
 */

public class BolzanoTechniqueWrapper implements TechniqueWrapper {

	private QueryRewriter			queryRewriter		= null;
	private UnfoldingMechanism		unfoldingMechanism	= null;
	private SourceQueryGenerator	queryGenerator		= null;
	private EvaluationEngine		evaluationEngine	= null;
	private QueryVocabularyValidator queryValidator = null;
	private OBDAModel			apic				= null;
	private final Logger			log					= LoggerFactory.getLogger(this.getClass());

	public BolzanoTechniqueWrapper(UnfoldingMechanism unf, QueryRewriter rew, SourceQueryGenerator gen, QueryVocabularyValidator val,
	    EvaluationEngine eng, OBDAModel apic) {

		this.queryRewriter = rew;
		this.unfoldingMechanism = unf;
		this.queryGenerator = gen;
		this.evaluationEngine = eng;
		this.queryValidator = val;
		this.apic = apic;
	}
	
	public QueryRewriter getRewriter() {
		return queryRewriter;
	}
	
	public void setRewriter(QueryRewriter rew) {
		this.queryRewriter = rew;
	}

	/**
	 * Returns the answer statement for the given query or throws an Exception
	 * if the query syntax is not supported
	 */
	@Override
	public Statement getStatement() throws Exception {
		return new OBDAStatement(unfoldingMechanism, queryRewriter, queryGenerator, queryValidator, evaluationEngine, apic);
	}

	@Override
	public void dispose() {
		evaluationEngine.dispose();
	}

	@Override
	public void loadDependencies(DLLiterOntology onto) {
		queryRewriter.setABoxDependencies(onto);
		
	}

}
