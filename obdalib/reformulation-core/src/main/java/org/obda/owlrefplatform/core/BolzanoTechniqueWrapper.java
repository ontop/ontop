package org.obda.owlrefplatform.core;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Statement;

import org.obda.owlrefplatform.core.queryevaluation.EvaluationEngine;
import org.obda.owlrefplatform.core.reformulation.QueryRewriter;
import org.obda.owlrefplatform.core.srcquerygeneration.SourceQueryGenerator;
import org.obda.owlrefplatform.core.unfolding.UnfoldingMechanism;
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
	private SourceQueryGenerator	querygenerator		= null;
	private EvaluationEngine		evaluationEngine	= null;
	private OBDAModel			apic				= null;
	private final Logger			log					= LoggerFactory.getLogger(this.getClass());

	public BolzanoTechniqueWrapper(UnfoldingMechanism unf, QueryRewriter rew, SourceQueryGenerator gen, EvaluationEngine eng,
			OBDAModel apic) {

		this.queryRewriter = rew;
		this.unfoldingMechanism = unf;
		this.querygenerator = gen;
		this.evaluationEngine = eng;
		this.apic = apic;
	}

	/**
	 * Returns the answer statement for the given query or throws an Exception
	 * if the query syntax is not supported
	 */
	@Override
	public Statement getStatement() throws Exception {
		return new OBDAStatement(unfoldingMechanism, queryRewriter, querygenerator, evaluationEngine, apic);
	}
	
	@Override
	public void dispose() {
		evaluationEngine.dispose();
	}

}
