package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDARDBMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.parser.SQLQueryTranslator;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.api.AndOperator;
import it.unibz.krdb.sql.api.ComparisonPredicate;
import it.unibz.krdb.sql.api.DerivedColumn;
import it.unibz.krdb.sql.api.IValueExpression;
import it.unibz.krdb.sql.api.Projection;
import it.unibz.krdb.sql.api.QueryTree;
import it.unibz.krdb.sql.api.RelationalAlgebra;
import it.unibz.krdb.sql.api.Selection;
import it.unibz.krdb.sql.api.StringLiteral;

import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author xiao
 *
 */
public class MetaMappingExpander {

	private Connection connection;
	private SQLQueryTranslator translator;
	private List<OBDAMappingAxiom> expandedMappings;
	private OBDADataFactory dfac;

	/**
	 * TODO
	 * 
	 * @param connection
	 * @param metadata
	 */
	public MetaMappingExpander(Connection connection, DBMetadata metadata) {
		this.connection = connection;
		translator = new SQLQueryTranslator(metadata);
		expandedMappings = new ArrayList<OBDAMappingAxiom>();
		dfac = OBDADataFactoryImpl.getInstance();
	}

	/**
	 * this method expand the input mappings, which may include meta mappings, to the concrete mappings
	 * 
	 * @param mappings
	 * 		a list of mappings, which may include meta mappings
	 * @return
	 * 		expanded normal mappings
	 */
	private List<OBDAMappingAxiom> expand(ArrayList<OBDAMappingAxiom> mappings) {
		
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
				
			} else {
				List<Variable> varsInTemplate = getVariablesInTemplate(bodyAtom);
				
				// Construct the SQL query tree from the source query
				QueryTree sourceQueryTree = translator.contructQueryTree(sourceQuery.toString());
				
				Projection distinctParamsProjection = new Projection();
				
				distinctParamsProjection.setType(Projection.SELECT_DISTINCT);
				
				ArrayList<DerivedColumn> columnList = sourceQueryTree.getProjection().getColumnList();
				
				List<DerivedColumn> columnsForTemplate = getColumnsForTemplate(varsInTemplate, columnList);
				
				distinctParamsProjection.addAll(columnsForTemplate);
				
				/**
				 * The query for params is almost the same with the original source query, except that
				 * we only need to distinct project the columns needed for the template expansion 
				 */
				RelationalAlgebra ra = sourceQueryTree.value().clone();
				ra.setProjection(distinctParamsProjection);
				
				QueryTree distinctQueryTree = new QueryTree(ra, sourceQueryTree.left(), sourceQueryTree.right());
				
				String distinctParamsSQL = distinctQueryTree.toString();
				List<List<String>> paramsForClassTemplate = new ArrayList<List<String>>();
				
				
				Statement st;
				try {
					st = connection.createStatement();
					ResultSet rs = st.executeQuery(distinctParamsSQL);
					while(rs.next()){
						ArrayList<String> params = new ArrayList<String>(varsInTemplate.size());
						for(int i = 1 ; i <= varsInTemplate.size(); i++){
							 params.add(rs.getString(i));
						}
						paramsForClassTemplate.add(params);
						
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				List<DerivedColumn> columnsForValues = new ArrayList<DerivedColumn>(columnList);
				columnsForValues.removeAll(columnsForTemplate);
				
				String id = mapping.getId();
				
				for(List<String> params : paramsForClassTemplate) {
					String newId = IDGenerator.getNextUniqueID(id + "##");
					OBDARDBMappingAxiom newMapping = instantiateMapping(newId, targetQuery,
							bodyAtom, sourceQueryTree, columnsForTemplate,
							columnsForValues, params);
										
					expandedMappings.add(newMapping);	
				}
				
			}

		}
		
	
		return expandedMappings;
	}

	/**
	 * This method instantiate a meta mapping by the concrete parameters
	 * 
	 * @param targetQuery
	 * @param bodyAtom
	 * @param sourceQueryTree
	 * @param columnsForTemplate
	 * @param columnsForValues
	 * @param params
	 * @return
	 */
	private OBDARDBMappingAxiom instantiateMapping(String id, CQIE targetQuery,
			Function bodyAtom, QueryTree sourceQueryTree,
			List<DerivedColumn> columnsForTemplate,
			List<DerivedColumn> columnsForValues,
			List<String> params) {
		
		/*
		 * First construct new Target Query 
		 */
		Function newTargetHead = targetQuery.getHead();
		Function newTargetBody = expandHigherOrderAtom(bodyAtom, params);
		CQIE newTargetQuery = dfac.getCQIE(newTargetHead, newTargetBody);
		
		/*
		 * Then construct new Source Query
		 */
		
		/*
		 * new Selection 
		 */
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
				IValueExpression columnRefExpression = column.getValueExpression();
				
				StringLiteral clsStringLiteral = new StringLiteral(params.get(j));
				if(j != 0){
					newSelection.addOperator(new AndOperator());
				}
				ComparisonPredicate condition = new ComparisonPredicate(columnRefExpression, clsStringLiteral, ComparisonPredicate.Operator.EQ);
				newSelection.addCondition(condition);
				j++;
				
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		/*
		 * new Projection
		 */
		Projection newProjection = new Projection();
		newProjection.addAll(columnsForValues);
		
		/*
		 * new algebra for the source query
		 */
		RelationalAlgebra ra = sourceQueryTree.value().clone();
		ra.setSelection(newSelection);
		ra.setProjection(newProjection);
		
		
		QueryTree newSourceQueryTree = new QueryTree(ra, sourceQueryTree.left(), sourceQueryTree.right());
		String newSourceQuerySQL = newSourceQueryTree.toString();
		OBDASQLQuery newSourceQuery =  dfac.getSQLQuery(newSourceQuerySQL);

		OBDARDBMappingAxiom newMapping = dfac.getRDBMSMappingAxiom(id, newSourceQuery, newTargetQuery);
		return newMapping;
	}

	/**
	 * This method get the columns which will be used for the predicate template 
	 * 
	 * @param varsInTemplate
	 * @param columnList
	 * @return
	 */
	private List<DerivedColumn> getColumnsForTemplate(List<Variable> varsInTemplate,
			ArrayList<DerivedColumn> columnList) {
		List<DerivedColumn> columnsForTemplate = new ArrayList<DerivedColumn>();

		for (Variable var : varsInTemplate) {
			boolean found = false;
			for (DerivedColumn column : columnList) {
				if ((column.hasAlias() && column.getAlias().equals(var.getName())) //
						|| (!column.hasAlias() && column.getName().equals(var.getName()))) {
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

		Term term2 = atom.getTerm(2);
		Function functionTerm2 = (Function) term2;
		String uriTemplate = ((ValueConstant) functionTerm2.getTerm(0))
				.getValue();

		String predName = uriTemplate;
		
		for (int i = 1; i < functionTerm2.getArity(); i++) {
			predName = predName.replace("{}", values.get(i - 1));
		}

		Predicate p = dfac.getPredicate(predName, 1, new COL_TYPE[]{COL_TYPE.OBJECT});

		return dfac.getFunction(p, atom.getTerm(0));
	}

	/**
	 * this method expand the input mappings, which may include meta mappings, to the concrete mappings
	 * 
	 * @param mappings
	 * 		a list of mappings, which may include meta mappings
	 * @return
	 * 		expanded normal mappings
	 */
	public void expand(OBDAModel obdaModel, URI sourceURI) {
		List<OBDAMappingAxiom> expandedMappings = expand(obdaModel.getMappings(sourceURI));
		
		obdaModel.removeAllMappings();
		for(OBDAMappingAxiom mapping : expandedMappings){
			try {
				obdaModel.addMapping(sourceURI, mapping);
			} catch (DuplicateMappingException e) {
				throw new RuntimeException("Error: Duplicate Mappings generated by the MetaMappingExpander");
			}
		}
		
	}

}
