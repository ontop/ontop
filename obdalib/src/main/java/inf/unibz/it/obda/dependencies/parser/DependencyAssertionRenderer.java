package inf.unibz.it.obda.dependencies.parser;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.dependencies.AbstractDependencyAssertion;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSDisjointnessDependency;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSFunctionalDependency;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSInclusionDependency;
import inf.unibz.it.obda.dependencies.miner.exception.InvalidSyntaxException;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.parser.exception.QueryParseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Vector;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

/**
 * The dependency assertion renderer dependency assertion from different
 * from a string.
 * 
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy 
 *
 */

public class DependencyAssertionRenderer {
	
	/**
	 * the API conroller
	 */
	private APIController apic = null;
	/**
	 * instance of itself
	 */
	private static DependencyAssertionRenderer instance = null;
	
	/**
	 * creates a new DependencyAssertionRenderer object
	 * @param apic
	 */
	public DependencyAssertionRenderer(APIController apic){
		this.apic = apic;
		instance = this;
	}
	
	/**
	 * Returns the current instance of the Dependency Assertion Renderer
	 * @return an instance
	 */
	public static DependencyAssertionRenderer getInstance(){
		return instance;
	}
	
	/**
	 * Renders a functional dependency assertion of the given string, if possible
	 * 
	 * @param input the string
	 * @param uri the data source URI to which the assertion is associated
	 * @return a functional dependency assertion
	 * @throws Exception if the String does not represent a valid functional dependency assertion
	 */
	public RDBMSFunctionalDependency renderSingleRDBMSFunctionalDependency(String input, URI uri) throws Exception{
		
		DatasourcesController con = apic.getDatasourcesController();
		DataSource ds = con.getAllSources().get(uri);
		con.setCurrentDataSource(ds.getSourceID());
		Vector<AbstractDependencyAssertion> aux = parse(input);
		if(aux != null){
			AbstractDependencyAssertion ass = aux.get(0);
			if(ass instanceof RDBMSFunctionalDependency){
				return (RDBMSFunctionalDependency)ass;
			}else{
				throw new InvalidSyntaxException("Input string does not represent a valid RDBMSDisjoinednessAssertion");
			}
		}else{
			throw new InvalidSyntaxException("Input string does not represent a valid RDBMSDisjoinednessAssertion");
		}
	}
	
	/**
	 * Renders a inclusion dependency assertion of the given string, if possible
	 * 
	 * @param input the string
	 * @param uri the data source URI to which the assertion is associated
	 * @return a inclusion dependency assertion
	 * @throws Exception if the String does not represent a valid inclusion dependency assertion
	 */
	public RDBMSInclusionDependency renderSingleRBMSInclusionDependency(String input, URI uri) throws Exception{
		DatasourcesController con = apic.getDatasourcesController();
		DataSource ds = con.getAllSources().get(uri);
		con.setCurrentDataSource(ds.getSourceID());
		Vector<AbstractDependencyAssertion> aux = parse(input);
		if(aux != null){
			AbstractDependencyAssertion ass = aux.get(0);
			if(ass instanceof RDBMSInclusionDependency){
				return (RDBMSInclusionDependency)ass;
			}else{
				throw new InvalidSyntaxException("Input string does not represent a valid RDBMSDisjoinednessAssertion");
			}
		}else{
			throw new InvalidSyntaxException("Input string does not represent a valid RDBMSDisjoinednessAssertion");
		}
	}

	/**
	 * Renders a disjointness dependency assertion of the given string, if possible
	 * 
	 * @param input the string
	 * @param uri the data source URI to which the assertion is associated
	 * @return a disjointness dependency assertion
	 * @throws Exception if the String does not represent a valid disjointness dependency assertion
	 */
	public RDBMSDisjointnessDependency renderSingleRDBMSDisjoinednessAssertion(String input, URI uri) throws Exception{
		DatasourcesController con = apic.getDatasourcesController();
		DataSource ds = con.getAllSources().get(uri);
		con.setCurrentDataSource(ds.getSourceID());
		Vector<AbstractDependencyAssertion> aux = parse(input);
		if(aux != null){
			AbstractDependencyAssertion ass = aux.get(0);
			if(ass instanceof RDBMSDisjointnessDependency){
				return (RDBMSDisjointnessDependency)ass;
			}else{
				throw new InvalidSyntaxException("Input string does not represent a valid RDBMSDisjoinednessAssertion");
			}
		}else{
			throw new InvalidSyntaxException("Input string does not represent a valid RDBMSDisjoinednessAssertion");
		}
		
	}
	
	/**
	 * Renders a list of functional dependency assertion of the given string, if possible
	 * 
	 * @param input the string
	 * @param uri the data source URI to which the assertion is associated
	 * @return a list of functional dependency assertion
	 * @throws Exception if the String does not represent a valid list of functional dependency assertion
	 */
	public Vector<RDBMSFunctionalDependency> renderRDBMSFunctionalDependency(String input) throws Exception{
		
		Vector<AbstractDependencyAssertion> aux = parse(input);
		if(aux != null){
			Iterator<AbstractDependencyAssertion> it = aux.iterator();
			Vector<RDBMSFunctionalDependency> vec = new Vector<RDBMSFunctionalDependency>();
			while (it.hasNext()){
				AbstractDependencyAssertion ass = it.next();
				if(ass instanceof RDBMSFunctionalDependency){
					vec.add((RDBMSFunctionalDependency)ass);
				}else{
					throw new InvalidSyntaxException("Input string does not represent a valid RDBMSDisjoinednessAssertion");
				}
			}
			return vec;
		}else{
			throw new InvalidSyntaxException("Input string does not represent a valid RDBMSDisjoinednessAssertion");
		}
	}
	
	/**
	 * Renders a list of inclusion dependency assertion of the given string, if possible
	 * 
	 * @param input the string
	 * @param uri the data source URI to which the assertion is associated
	 * @return a list of inclusion dependency assertion
	 * @throws Exception if the String does not represent a valid list of inclusion dependency assertion
	 */
	public Vector<RDBMSInclusionDependency> renderRBMSInclusionDependency(String input) throws Exception{
		Vector<AbstractDependencyAssertion> aux = parse(input);
		if(aux != null){
			Iterator<AbstractDependencyAssertion> it = aux.iterator();
			Vector<RDBMSInclusionDependency> vec = new Vector<RDBMSInclusionDependency>();
			while (it.hasNext()){
				AbstractDependencyAssertion ass = it.next();
				if(ass instanceof RDBMSInclusionDependency){
					vec.add((RDBMSInclusionDependency)ass);
				}else{
					throw new InvalidSyntaxException("Input string does not represent a valid RDBMSDisjoinednessAssertion");
				}
			}
			return vec;
		}else{
			throw new InvalidSyntaxException("Input string does not represent a valid RDBMSDisjoinednessAssertion");
		}
	}
	/**
	 * Renders a list of disjointness dependency assertion of the given string, if possible
	 * 
	 * @param input the string
	 * @param uri the data source URI to which the assertion is associated
	 * @return a list of disjointness dependency assertion
	 * @throws Exception if the String does not represent a valid list of disjointness dependency assertion
	 */
	public Vector<RDBMSDisjointnessDependency> renderRDBMSDisjoinednessAssertion(String input) throws Exception{
		Vector<AbstractDependencyAssertion> aux = parse(input);
		if(aux != null){
			Iterator<AbstractDependencyAssertion> it = aux.iterator();
			Vector<RDBMSDisjointnessDependency> vec = new Vector<RDBMSDisjointnessDependency>();
			while (it.hasNext()){
				AbstractDependencyAssertion ass = it.next();
				if(ass instanceof RDBMSDisjointnessDependency){
					vec.add((RDBMSDisjointnessDependency)ass);
				}else{
					throw new InvalidSyntaxException("Input string does not represent a valid RDBMSDisjoinednessAssertion");
				}
			}
			return vec;
		}else{
			throw new InvalidSyntaxException("Input string does not represent a valid RDBMSDisjoinednessAssertion");
		}
	}
	
	/**
	 * Check whether the given string can be rendered into the given dependency assertion
	 * 
	 * @param input a string 
	 * @param dependency the dependency assertion
	 * @return true if the string can be rendered into the given assertion, false otherwise 
	 */
	public boolean isValid(String input, String dependency){
		
		try {
			Vector<AbstractDependencyAssertion> v =parse(input);
			AbstractDependencyAssertion ass = v.get(0);
			if(dependency.equals(RDBMSInclusionDependency.INCLUSIONDEPENDENCY) && ass instanceof RDBMSInclusionDependency){
				return true;
			}else if(dependency.equals(RDBMSDisjointnessDependency.DISJOINEDNESSASSERTION) && ass instanceof RDBMSDisjointnessDependency){
				return true;
			}else if(dependency.equals(RDBMSFunctionalDependency.FUNCTIONALDEPENDENCY)&& ass instanceof RDBMSFunctionalDependency){
				return true;
			}else{
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	public RDBMSInclusionDependency createAndValidateRDBMSInclusionDependency(String id1, String id2, Vector<QueryTerm> t1, Vector<QueryTerm> t2){
		
		DatasourcesController dscon = apic.getDatasourcesController();
		URI currentds = dscon.getCurrentDataSource().getSourceID();
		
		MappingController mapcon = apic.getMappingController();
		OBDAMappingAxiom axiom1 = mapcon.getMapping(currentds, id1);
		OBDAMappingAxiom axiom2 = mapcon.getMapping(currentds, id2);
		if(axiom1 == null || axiom2 == null){
			return null;
		}else{
			RDBMSInclusionDependency inc = new RDBMSInclusionDependency(
					currentds, id1, id2, (RDBMSSQLQuery)axiom1.getSourceQuery(), (RDBMSSQLQuery)axiom2.getSourceQuery(), t1, t2);
			if(isValid(inc.toString(), RDBMSInclusionDependency.INCLUSIONDEPENDENCY)){
				return inc;
			}else{
				return null;
			}
		}
	}
	
	public RDBMSFunctionalDependency createAndValidateRDBMSFunctionalDependency(String id1, String id2, Vector<QueryTerm> t1, Vector<QueryTerm> t2){
		
		DatasourcesController dscon = apic.getDatasourcesController();
		URI currentds = dscon.getCurrentDataSource().getSourceID();
		
		MappingController mapcon = apic.getMappingController();
		OBDAMappingAxiom axiom1 = mapcon.getMapping(currentds, id1);
		OBDAMappingAxiom axiom2 = mapcon.getMapping(currentds, id2);
		if(axiom1 == null || axiom2 == null){
			return null;
		}else{
			RDBMSFunctionalDependency func = new RDBMSFunctionalDependency(
					currentds, id1, id2, (RDBMSSQLQuery)axiom1.getSourceQuery(), (RDBMSSQLQuery)axiom2.getSourceQuery(), t1, t2);
			if(isValid(func.toString(), RDBMSFunctionalDependency.FUNCTIONALDEPENDENCY)){
				return func;
			}else{
				return null;
			}
		}
	}
	
	public RDBMSDisjointnessDependency createAndValidateRDBMSDisjointnessDependency(String id1, String id2, Vector<QueryTerm> t1, Vector<QueryTerm> t2){
		
		DatasourcesController dscon = apic.getDatasourcesController();
		URI currentds = dscon.getCurrentDataSource().getSourceID();
		
		MappingController mapcon = apic.getMappingController();
		OBDAMappingAxiom axiom1 = mapcon.getMapping(currentds, id1);
		OBDAMappingAxiom axiom2 = mapcon.getMapping(currentds, id2);
		if(axiom1 == null || axiom2 == null){
			return null;
		}else{
			RDBMSDisjointnessDependency dis = new RDBMSDisjointnessDependency(
					currentds, id1, id2, (RDBMSSQLQuery)axiom1.getSourceQuery(), (RDBMSSQLQuery)axiom2.getSourceQuery(), t1, t2);
			if(isValid(dis.toString(), RDBMSDisjointnessDependency.DISJOINEDNESSASSERTION)){
				return dis;
			}else{
				return null;
			}
		}
	}
	
	/**
	 * parses the String, and tries to render one ore more dependency assertion out of it 
	 * 
	 * @param input the string
	 * @return a list of dependency assertions
	 * @throws Exception if the were errors during the parsing of the string
	 */
	private Vector<AbstractDependencyAssertion> parse (String input) throws Exception{
		
		DependencyAssertionParser parser = null;
		byte currentBytes[] = input.getBytes();
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(currentBytes);
		ANTLRInputStream inputst = null;
		try {
			inputst = new ANTLRInputStream(byteArrayInputStream);

		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
		DependencyAssertionLexer lexer = new DependencyAssertionLexer(inputst);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		parser = new DependencyAssertionParser(tokens);
		parser.setController(apic);
		try {
			parser.parse();
		} catch (RecognitionException e) {
			e.printStackTrace();
		}
		if ((parser.getErrors().size() == 0) && (lexer.getErrors().size() == 0)) {
			return parser.getDependencyAssertion(); 
		} else {
				throw new QueryParseException(parser.getErrors().toString());
		}
	}
}
