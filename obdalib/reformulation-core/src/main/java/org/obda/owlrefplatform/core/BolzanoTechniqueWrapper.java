package org.obda.owlrefplatform.core;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.queryanswering.Statement;

import java.net.URI;
import java.util.Set;

import org.antlr.runtime.RecognitionException;
import org.obda.query.domain.DatalogProgram;
import org.obda.query.tools.parser.DatalogProgramParser;
import org.obda.query.tools.parser.SPARQLDatalogTranslator;
import org.obda.reformulation.dllite.QueryRewriter;
import org.obda.reformulation.domain.DLLiterOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryException;


/**
 * Is the currently used technique wrapper of our reformulation
 * platform reasoner 
 * 
 * @author Manfred Gerstgrasser
 *
 */

public class BolzanoTechniqueWrapper implements TechniqueWrapper {

	private QueryRewriter queryRewriter = null;
	private UnfoldingMechanism unfoldingMechanism = null;
	private SourceQueryGenerator querygenerator = null;
	private EvaluationEngine evaluationEngine = null;
	private APIController apic = null;
	private DatalogProgram queryProgram = null;
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public BolzanoTechniqueWrapper(UnfoldingMechanism unf,
	QueryRewriter rew , SourceQueryGenerator gen, EvaluationEngine eng,
	APIController apic){

		this.queryRewriter = rew;
		this.unfoldingMechanism = unf;
		this.querygenerator = gen;
		this.evaluationEngine = eng;
		this.apic = apic;
	}

	/**
	 * Returns the answer statement for the given query or throws an 
	 * Exception if the query syntax is not supported
	 */
	public Statement getStatement(String query) throws Exception {

		SPARQLDatalogTranslator sparqlTranslator = new SPARQLDatalogTranslator();

		queryProgram = null;
		try {
			queryProgram = sparqlTranslator.parse(query);
		}
		catch (QueryException e) {
			log.warn(e.getMessage());
		}

		if (queryProgram == null) {  // if the SPARQL translator doesn't work, use the Datalog parser.
			DatalogProgramParser datalogParser = new DatalogProgramParser();
			try {
				queryProgram = datalogParser.parse(query);
			}
			catch (RecognitionException e) {
				log.warn(e.getMessage());
				queryProgram = null;
			}
		}

		if (queryProgram == null)  // if it is still null
			throw new Exception("Unsupported syntax");

		return new OBDAStatement(unfoldingMechanism, queryRewriter, querygenerator, evaluationEngine, queryProgram, apic);
	}

	/**
	 * Updates the data source of wrapper if it changed
	 */
	@Override
	public void updateDataSource(DataSource ds) {
		evaluationEngine.update(ds);
	}

	/**
	 * Updates the ontologies previously given to the wrapper if they changed
	 */
	@Override
	public void updateOntology(DLLiterOntology onto, Set<URI> uris) {
		queryRewriter.updateAssertions(onto.getAssertions());
		querygenerator.update(apic.getIOManager().getPrefixManager(),onto, uris);
	}

}
