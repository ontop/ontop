package inf.unibz.it.obda.constraints.parser;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.constraints.AbstractConstraintAssertion;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSCheckConstraint;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSForeignKeyConstraint;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSPrimaryKeyConstraint;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSUniquenessConstraint;
import inf.unibz.it.obda.dependencies.miner.exception.InvalidSyntaxException;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.ucq.typing.CheckOperationTerm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.obda.query.domain.Variable;

public class ConstraintsRenderer {
/**
 * the API controller
 */
private APIController apic = null;
/**
 * instance of itself
 */
private static ConstraintsRenderer instance = null;

/**
 * creates a new DependencyAssertionRenderer object
 * @param apic
 */
protected ConstraintsRenderer(APIController apic){
	this.apic = apic;
}

/**
 * Returns the current instance of the Dependency Assertion Renderer
 * @return an instance
 */
public static ConstraintsRenderer getInstance(APIController apic){
	if (instance == null) {
		instance = new ConstraintsRenderer(apic);
	}
	return instance;
}

/**
 * Renders a check constraint assertion of the given string, if possible
 *
 * @param input the string
 * @param uri the data source URI to which the assertion is associated
 * @return a functional dependency assertion
 * @throws Exception if the String does not represent a valid functional dependency assertion
 */
public RDBMSCheckConstraint renderSingleCeckConstraint(String input, URI uri) throws Exception{

	DatasourcesController con = apic.getDatasourcesController();
	DataSource ds =con.getAllSources().get(uri);
	con.setCurrentDataSource(ds.getSourceID());
	AbstractConstraintAssertion aux = parse(input);
	if(aux != null){
		if(aux instanceof RDBMSCheckConstraint){
			return (RDBMSCheckConstraint)aux;
		}else{
			throw new InvalidSyntaxException("Input string does not represent a valid RDBMSDisjoinednessAssertion");
		}
	}else{
		throw new InvalidSyntaxException("Input string does not represent a valid RDBMSDisjoinednessAssertion");
	}
}

/**
 * Renders a primary key constraint assertion of the given string, if possible
 *
 * @param input the string
 * @param uri the data source URI to which the assertion is associated
 * @return a inclusion dependency assertion
 * @throws Exception if the String does not represent a valid inclusion dependency assertion
 */
public RDBMSPrimaryKeyConstraint renderSingleRDBMSPrimaryKeyConstraint(String input, URI uri) throws Exception{
	DatasourcesController con = apic.getDatasourcesController();
	DataSource ds =con.getAllSources().get(uri);
	con.setCurrentDataSource(ds.getSourceID());
	AbstractConstraintAssertion aux = parse(input);
	if(aux != null){
		if(aux instanceof RDBMSPrimaryKeyConstraint){
			return (RDBMSPrimaryKeyConstraint)aux;
		}else{
			throw new InvalidSyntaxException("Input string does not represent a valid RDBMSDisjoinednessAssertion");
		}
	}else{
		throw new InvalidSyntaxException("Input string does not represent a valid RDBMSDisjoinednessAssertion");
	}
}

/**
 * Renders a foreign key constraint assertion of the given string, if possible
 *
 * @param input the string
 * @param uri the data source URI to which the assertion is associated
 * @return a disjointness dependency assertion
 * @throws Exception if the String does not represent a valid disjointness dependency assertion
 */
public RDBMSForeignKeyConstraint renderSingleRDBMSForeignKeyConstraint(String input, URI uri) throws Exception{
	DatasourcesController con = apic.getDatasourcesController();
	DataSource ds =con.getAllSources().get(uri);
	con.setCurrentDataSource(ds.getSourceID());
	AbstractConstraintAssertion aux = parse(input);
	if(aux != null){
		if(aux instanceof RDBMSForeignKeyConstraint){
			return (RDBMSForeignKeyConstraint)aux;
		}else{
			throw new InvalidSyntaxException("Input string does not represent a valid RDBMSDisjoinednessAssertion");
		}
	}else{
		throw new InvalidSyntaxException("Input string does not represent a valid RDBMSDisjoinednessAssertion");
	}

}

public RDBMSCheckConstraint createRDBMSCheckConstraint(String id, List<CheckOperationTerm> list) throws Exception{

	DatasourcesController dscon = apic.getDatasourcesController();
	URI currentds = dscon.getCurrentDataSource().getSourceID();

	MappingController con = apic.getMappingController();
	OBDAMappingAxiom axiom = con.getMapping(currentds, id);
	if(axiom == null){
		throw new Exception("No mapping found with id: " + id);
	}else{
		return new RDBMSCheckConstraint(id,(RDBMSSQLQuery)axiom.getSourceQuery(), list);
	}
}

public RDBMSForeignKeyConstraint createRDBMSForeignKeyConstraint(String id1, String id2, List<Variable> l1, List<Variable> l2) throws Exception{

	DatasourcesController dscon = apic.getDatasourcesController();
	URI currentds = dscon.getCurrentDataSource().getSourceID();

	MappingController con = apic.getMappingController();
	OBDAMappingAxiom axiom1 = con.getMapping(currentds, id1);
	OBDAMappingAxiom axiom2 = con.getMapping(currentds, id2);
	if(axiom1 == null || axiom2 == null){
		throw new Exception("Mapping not found");
	}else{
		return new RDBMSForeignKeyConstraint(id1, id2,(RDBMSSQLQuery)axiom1.getSourceQuery(),(RDBMSSQLQuery)axiom2.getSourceQuery(), l1,l2);
	}
}

public RDBMSPrimaryKeyConstraint createRDBMSPrimaryKeyConstraint(String id, List<Variable> list) throws Exception{
	DatasourcesController dscon = apic.getDatasourcesController();
	URI currentds = dscon.getCurrentDataSource().getSourceID();

	MappingController con = apic.getMappingController();
	OBDAMappingAxiom axiom1 = con.getMapping(currentds, id);

	if(axiom1 == null ){
		throw new Exception("Mapping not found");
	}else{
		return new RDBMSPrimaryKeyConstraint(id,(RDBMSSQLQuery)axiom1.getSourceQuery(), list);
	}
}

public RDBMSUniquenessConstraint createRDBMSUniquenessConstraint(String id, List<Variable> list) throws Exception{
	DatasourcesController dscon = apic.getDatasourcesController();
	URI currentds = dscon.getCurrentDataSource().getSourceID();

	MappingController con = apic.getMappingController();
	OBDAMappingAxiom axiom1 = con.getMapping(currentds, id);

	if(axiom1 == null ){
		throw new Exception("Mapping not found");
	}else{
		return new RDBMSUniquenessConstraint(id,(RDBMSSQLQuery)axiom1.getSourceQuery(), list);
	}
}

/**
 * Renders a uniqueness constraint assertion of the given string, if possible
 *
 * @param input the string
 * @param uri the data source URI to which the assertion is associated
 * @return a disjointness dependency assertion
 * @throws Exception if the String does not represent a valid disjointness dependency assertion
 */
public RDBMSUniquenessConstraint renderSingleRDBMSUniquenessConstraint(String input, URI uri) throws Exception{
	DatasourcesController con = apic.getDatasourcesController();
	DataSource ds =con.getAllSources().get(uri);
	con.setCurrentDataSource(ds.getSourceID());
	AbstractConstraintAssertion aux = parse(input);
	if(aux != null){
		if(aux instanceof RDBMSUniquenessConstraint){
			return (RDBMSUniquenessConstraint)aux;
		}else{
			throw new InvalidSyntaxException("Input string does not represent a valid RDBMSDisjoinednessAssertion");
		}
	}else{
		throw new InvalidSyntaxException("Input string does not represent a valid RDBMSDisjoinednessAssertion");
	}

}

/**
 * Check whether the given string can be rendered into the given dependency assertion
 *
 * @param input a string
 * @param constraint the dependency assertion
 * @return true if the string can be rendered into the given assertion, false otherwise
 */
public boolean isValid(String input, String constraint){

	try {
		AbstractConstraintAssertion con =parse(input);
		if(constraint.equals(RDBMSCheckConstraint.RDBMSCHECKSONSTRAINT) && con instanceof RDBMSCheckConstraint){
			return true;
		}else if(constraint.equals(RDBMSForeignKeyConstraint.RDBMSFOREIGNKEYCONSTRAINT) && con instanceof RDBMSForeignKeyConstraint){
			return true;
		}else if(constraint.equals(RDBMSPrimaryKeyConstraint.RDBMSPRIMARYKEYCONSTRAINT)&& con instanceof RDBMSPrimaryKeyConstraint){
			return true;
		}else if(constraint.equals(RDBMSUniquenessConstraint.RDBMSUNIQUENESSCONSTRAINT)&& con instanceof RDBMSUniquenessConstraint){
			return true;
		}else{
			return false;
		}
	} catch (Exception e) {
		return false;
	}
}

/**
 * parses the String, and tries to render one ore more dependency assertion out of it
 *
 * @param input the string
 * @return a list of dependency assertions
 * @throws Exception if the were errors during the parsing of the string
 */
private AbstractConstraintAssertion parse (String input) throws Exception{

	ConstraintsParser parser = null;
	byte currentBytes[] = input.getBytes();
	ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(currentBytes);
	ANTLRInputStream inputst = null;
	try {
		inputst = new ANTLRInputStream(byteArrayInputStream);

	} catch (IOException e) {
		e.printStackTrace(System.err);
	}
	ConstraintsLexer lexer = new ConstraintsLexer(inputst);
	CommonTokenStream tokens = new CommonTokenStream(lexer);
	parser = new ConstraintsParser(tokens);
	parser.setController(apic);

	AbstractConstraintAssertion constraint = parser.parse();

	if (parser.getNumberOfSyntaxErrors() != 0)
		throw new RecognitionException();

	return constraint;
}
}
