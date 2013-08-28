package it.unibz.krdb.obda.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDARDBMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.parser.SQLQueryTranslator;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.api.AndOperator;
import it.unibz.krdb.sql.api.ColumnReference;
import it.unibz.krdb.sql.api.ComparisonPredicate;
import it.unibz.krdb.sql.api.DerivedColumn;
import it.unibz.krdb.sql.api.IValueExpression;
import it.unibz.krdb.sql.api.Projection;
import it.unibz.krdb.sql.api.QueryTree;
import it.unibz.krdb.sql.api.ReferenceValueExpression;
import it.unibz.krdb.sql.api.RelationalAlgebra;
import it.unibz.krdb.sql.api.Selection;
import it.unibz.krdb.sql.api.StringLiteral;

public class MetaMappingExpander {

	private Connection connection;
	private SQLQueryTranslator translator;
	private List<OBDAMappingAxiom> expandedMappings;

	public MetaMappingExpander(Connection connection, DBMetadata metadata) {
		this.connection = connection;
		translator = new SQLQueryTranslator(metadata);
		expandedMappings = new ArrayList<OBDAMappingAxiom>();

	}

	public List<OBDAMappingAxiom> expand(ArrayList<OBDAMappingAxiom> mappings) {
		
		for (OBDAMappingAxiom mapping : mappings) {

			CQIE targetQuery = (CQIE) mapping.getTargetQuery();
			List<Function> body = targetQuery.getBody();
			Function bodyAtom = targetQuery.getBody().get(0);

			OBDASQLQuery sourceQuery = (OBDASQLQuery)mapping.getSourceQuery();

			Function firstBodyAtom = body.get(0);
			
			if (!firstBodyAtom.getFunctionSymbol().equals(OBDAVocabulary.QUEST_TRIPLE_PRED)){
				/**
				 * for normal mappings, we do not need to expand it.
				 */
				expandedMappings.add(mapping);
				System.err.println("Normal mapping: " + mapping);
				
			} else {
				List<Variable> varsInTemplate = getVariablesInTemplate(bodyAtom);
				
				System.err.println("Meta mapping: " + mapping);
			
				
				// Construct the SQL query tree from the source query
				QueryTree sourceQueryTree = translator.contructQueryTree(sourceQuery.toString());
				
				ArrayList<DerivedColumn> columnList = sourceQueryTree.getProjection().getColumnList();
				
				Projection distinctClassesProject = new Projection();
				
				distinctClassesProject.setType(Projection.SELECT_DISTINCT);
				
				List<DerivedColumn> columnsForTemplate = new ArrayList<DerivedColumn>();

				columnsForTemplate = getColumnsForTemplate(varsInTemplate, columnList);
				
				distinctClassesProject.addAll(columnsForTemplate);
				
				List<DerivedColumn> columnsForValues = new ArrayList<DerivedColumn>(columnList);

				columnsForValues.removeAll(columnsForTemplate);
				
				RelationalAlgebra ra = sourceQueryTree.value().clone();
				ra.setProjection(distinctClassesProject);
				
				QueryTree distinctQueryTree = new QueryTree(ra, sourceQueryTree.left(), sourceQueryTree.right());
				
				String distinctClassesSQL = distinctQueryTree.toString();

				List<List<String>> paramsForClassTemplate = new ArrayList<List<String>>();
				
				Statement st;
				try {
					st = connection.createStatement();
					ResultSet rs = st.executeQuery(distinctClassesSQL);
					while(rs.next()){
						ArrayList<String> row = new ArrayList<String>(varsInTemplate.size());
						for(int i = 1 ; i <= varsInTemplate.size(); i++){
							 row.add(rs.getString(i));
						}
						paramsForClassTemplate.add(row);
						
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				
				OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();  
				
				for(List<String> params : paramsForClassTemplate) {
					Function newTargetHead = targetQuery.getHead();
					
					Function newTargetBody = expandHigherOrderAtom(bodyAtom, params);
					
					CQIE newTargetQuery = dfac.getCQIE(newTargetHead, newTargetBody);
					
					Selection selection = sourceQueryTree.getSelection();
					Selection newSelection;
					if(selection != null){
						newSelection = selection.clone();
					} else {
						newSelection = new Selection();
					}
					
					
					try {
						int j = 0;
						
						for(DerivedColumn column : columnsForTemplate){
							IValueExpression columnRefExpression;
							columnRefExpression = column.getValueExpression();
							
							StringLiteral clsStringLiteral = new StringLiteral(params.get(j));
							if(j != 0){
								newSelection.addOperator(new AndOperator());
							}
							newSelection.addCondition(new ComparisonPredicate(columnRefExpression, clsStringLiteral, ComparisonPredicate.Operator.EQ));
							j++;
							
						}
						
						
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					
					ra = sourceQueryTree.value().clone();
					ra.setSelection(newSelection);
					
					Projection valueProject = new Projection();
					
					/**
					 * 'X' is at position 0 of 'q(X, Class)'.
					 */
					//valueProject.add(columnList.get(0));
					valueProject.addAll(columnsForValues);
					
					ra = sourceQueryTree.value().clone();
					ra.setProjection(valueProject);
					ra.setSelection(newSelection);
					
					QueryTree newTargetQueryTree = new QueryTree(ra, sourceQueryTree.left(), sourceQueryTree.right());
					
					// TODO we may want a proper id
					OBDARDBMappingAxiom newMapping = dfac.getRDBMSMappingAxiom(newTargetQueryTree.toString(), newTargetQuery);
					
					System.err.println(newMapping);
					
					expandedMappings.add(newMapping);
					
				}
				
			}

		}
		
	
		return expandedMappings;
	}

	private List<DerivedColumn> getColumnsForTemplate(List<Variable> varsInTemplate,
			ArrayList<DerivedColumn> columnList) {
		List<DerivedColumn> columnsForTemplate = new ArrayList<DerivedColumn>();

		for (Variable var : varsInTemplate) {
			boolean found = false;
			for (DerivedColumn column : columnList) {
				if ((column.hasAlias() && column.getAlias().equals(var.getName())) //
						|| (!column.hasAlias() && column.getName().equals(var.getName()))) {
					//distinctClassesProject.add(column);
					columnsForTemplate.add(column);
					found = true;
					break;
				}
			}
			if(!found){
				throw new IllegalStateException();
			}
		}
		
		return columnsForTemplate;
		
		
	}

	/**
	 * 
	 * This method extracts the variables in the template from the atom 
	 * 
	 * Example:
	 * Input Atom:
	 * <pre>triple(t1, 'rdf:type', URI("http://example.org/{}/{}", X, Y))</pre>
	 * 
	 * Output: [X, Y]
	 * 
	 * @param atom
	 * @return
	 */
	private List<Variable> getVariablesInTemplate(Function atom) {
		Function funcTerm = (Function)atom.getTerm(2);
		List<Variable> vars = new ArrayList<Variable>();
		for(int i = 1; i < funcTerm.getArity(); i++){
			vars.add((Variable) funcTerm.getTerm(i));
		}
		return vars;
	}

	
	/***
	 * This method expands the higher order atom 
	 * <pre>triple(t1, 'rdf:type', URI("http://example.org/{}", X))</pre>
	 *  to 
	 *  <pre>http://example.org/cls(t1)</pre>, if X is t1
	 * 
	 * @param atom 
	 * 			a Function in form of triple(t1, 'rdf:type', X)
	 * @param values
	 * 			the concrete name of the X 
	 * @return
	 * 			expanded atom in form of <pre>http://example.org/cls(t1)</pre>
	 */
	private Function expandHigherOrderAtom(Function atom, List<String> values) {
		// if(term2 instanceof Function){
		Term term2 = atom.getTerm(2);
		OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

		Function functionTerm2 = (Function) term2;
		String uriTemplate = ((ValueConstant) functionTerm2.getTerm(0))
				.getValue();

		String predName = uriTemplate;
		
		int j = 0;

		for (int i = 1; i < functionTerm2.getArity(); i++) {
			predName = predName.replace("{}", values.get(j));
			j++;
		}

		Predicate p = dfac.getPredicate(predName, 1);

		return dfac.getFunction(p, atom.getTerm(0));

	}

}
