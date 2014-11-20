package org.semanticweb.ontop.owlrefplatform.core.sql;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.ontop.model.AlgebraOperatorPredicate;
import org.semanticweb.ontop.model.BooleanOperationPredicate;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDAQueryModifiers;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.owlrefplatform.core.queryevaluation.SQLDialectAdapter;
import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.DataDefinition;
import org.semanticweb.ontop.sql.TableDefinition;
import org.semanticweb.ontop.sql.ViewDefinition;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * 
 * An object of this class is created for a CQ with the purpose to relate 
 * atoms and their variables to tables and column names.
 * 
 * E.g., for a CQ
 *   
 * 		ans1(t1) :- student(t1, t2, t3, t4), EQ(t4,2)
 * 
 * and a DB table 
 * 
 * 		student(id, name, email, type)
 * 
 * it would relate atom student(t1, t2, t3, t4) to the table student(id, name, email, type), and
 * variable t1 to "id", t2 to "name", t3 to "email" and t4 to "type".
 *
 */
public class SQLVariableIndex extends QueryVariableIndex{

	private final ImmutableMap<Function, String> viewNames; 
	private final ImmutableMap<Function, String> tableNames;
	private final ImmutableMap<Function, DataDefinition> dataDefinitions;
	private final Multimap<Variable, String> columnReferences;

	private final SQLDialectAdapter sqlAdapter;
	
	/**
	 * Contains some meta information about the main datalog program query:
	 *  - isDistinct
	 *  - isOrderBy
	 *  - view definitions for ans predicates other than ans1
	 */
	private final QueryInfo queryInfo;
	

	public SQLVariableIndex(CQIE cq, DBMetadata metadata, QueryInfo queryInfo, SQLDialectAdapter sqlDialectAdapter) {
		super(cq, metadata);
		
		Map<Function, String> viewNames = new HashMap<Function, String>(); 
		Map<Function, String> tableNames = new HashMap<Function, String>();
		Map<Function, DataDefinition> dataDefinitions = new HashMap<Function, DataDefinition>();
		Multimap<Variable, String> columnReferences = HashMultimap.create();

		int[] dataTableCount = {0};

		generateViews(cq.getBody(), metadata, queryInfo, viewNames, tableNames, dataDefinitions, 
				dataTableCount, columnReferences, sqlDialectAdapter);

		this.sqlAdapter = sqlDialectAdapter;
		this.queryInfo = queryInfo;
		
		this.viewNames = ImmutableMap.copyOf(viewNames);
		this.tableNames = ImmutableMap.copyOf(tableNames);
		this.dataDefinitions = ImmutableMap.copyOf(dataDefinitions);
		this.columnReferences = ImmutableMultimap.copyOf(columnReferences);
	}

	private static void generateViews(List<Function> atoms, DBMetadata metadata, QueryInfo queryInfo, 
									Map<Function, String> viewNames, Map<Function, String> tableNames, 
									Map<Function, DataDefinition> dataDefinitions, int[] dataTableCount, 
									Multimap<Variable, String> columnReferences, SQLDialectAdapter sqlDialectAdapter) {
		for (Function atom : atoms) {
			/*
			 * This will be called recursively if necessary
			 */
			generateViewsIndexVariables(atom, metadata, queryInfo, 
								viewNames, tableNames, dataDefinitions, dataTableCount, columnReferences, sqlDialectAdapter);
		}
	}

	/***
	 * We associate each atom to a view definition. This will be
	 * <p>
	 * "tablename" as "viewX" or
	 * <p>
	 * (some nested sql view) as "viewX"
	 * 
	 * <p>
	 * View definitions are only done for data atoms. Join/LeftJoin and
	 * boolean atoms are not associated to view definitions.
	 * 
	 * @param atom
	 * @param metadata
	 * @param queryInfo
	 * @param viewNames
	 * @param tableNames
	 * @param dataDefinitions
	 * @param dataTableCount
	 * @param columnReferences
	 * @param sqlDialectAdapter
	 */
	private static void generateViewsIndexVariables(Function atom, DBMetadata metadata, QueryInfo queryInfo, 
								Map<Function, String> viewNames, Map<Function, String> tableNames, 
								Map<Function, DataDefinition> dataDefinitions, int[] dataTableCount, 
								Multimap<Variable, String> columnReferences, SQLDialectAdapter sqlDialectAdapter) {
		
		if (atom.getFunctionSymbol() instanceof BooleanOperationPredicate) {
			return;
		} 
		
		else if (atom.getFunctionSymbol() instanceof AlgebraOperatorPredicate) {
			List<Term> terms = atom.getTerms();
			for (Term subatom : terms) {
				if (subatom instanceof Function) {
					generateViewsIndexVariables((Function) subatom, metadata, queryInfo, 
							viewNames, tableNames, dataDefinitions, dataTableCount, columnReferences, sqlDialectAdapter);
				}
			}
		}

		Predicate tablePredicate = atom.getFunctionSymbol();
		String tableName = tablePredicate.getName();
		DataDefinition dataDefinition = metadata.getDefinition(tableName);

		boolean isAnsPredicate = false;
		String viewName = null;
		
		if (dataDefinition == null) {
			/*
			 * There is no definition for this atom, its not a database
			 * predicate. We check if it is an ans predicate and it has a
			 * view:
			 */
			// viewName = "Q"+tableName+"View";
			viewName = String.format(NewSQLGenerator.VIEW_ANS_NAME, tableName);
			dataDefinition = queryInfo.getViewDefinition(viewName);
			if (dataDefinition == null) {
				return;
			}
			
			viewNames.put(atom, viewName);
			isAnsPredicate = true;
		} else {
			viewName = String.format(NewSQLGenerator.VIEW_NAME, tableName, String.valueOf(dataTableCount[0]));
			viewNames.put(atom, viewName);
		}
		dataTableCount[0] = dataTableCount[0] + 1;
		
		tableNames.put(atom, dataDefinition.getName());

		dataDefinitions.put(atom, dataDefinition);

		indexVariables(atom, isAnsPredicate, viewName, dataDefinition, columnReferences, sqlDialectAdapter);
	}

	/**
	 * Creates column references for each variable in atom
	 * 
	 * @param atom
	 * @param isAnsPredicate
	 * @param viewName
	 * 		the name of the view corresponding to atom
	 * @param dataDefinition
	 * 		table definition corresponding to atom
	 * @param columnReferences
	 * @param sqlDialectAdapter 
	 */
	private static void indexVariables(final Function atom, final boolean isAnsPredicate, 
									final String viewName, final DataDefinition dataDefinition, 
									Multimap<Variable, String> columnReferences, 
									final SQLDialectAdapter sqlDialectAdapter) {
		String quotedViewName = sqlDialectAdapter.sqlQuote(viewName);
		for (int index = 0; index < atom.getTerms().size(); index++) {
			Term term = atom.getTerm(index);

			if (term instanceof Variable) {
				/*
				 * the index of attributes of the definition starts from 1
				 */
				String columnName;

				//TODO need a proper method for checking whether something is an ans predicate
				if (isAnsPredicate) {
					// If I am here it means that it is not a database table
					// but a view from an Ans predicate
					int attPos = 3 * (index + 1);
					columnName = dataDefinition.getAttributeName(attPos);
				} else {
					columnName = dataDefinition.getAttributeName(index + 1);
				}

				columnName = NewSQLGenerator.removeAllQuotes(columnName);

				String reference = sqlDialectAdapter.sqlQualifiedColumn(quotedViewName, columnName);
				columnReferences.put((Variable) term, reference);
			}
		}
	}

	/***
	 * Generates the view definition, i.e., "tablename viewname".
	 */
	public String getViewDefinition(Function atom) {
		DataDefinition def = dataDefinitions.get(atom);
		String viewname = viewNames.get(atom);
		viewname = sqlAdapter.sqlQuote(viewname);

		if (def instanceof TableDefinition) {
			return sqlAdapter.sqlTableName(tableNames.get(atom), viewname);
		}
		
		else if (def instanceof ViewDefinition) {
			String viewdef = ((ViewDefinition) def).getStatement();
			String formatView = String.format("(%s) %s", viewdef, viewname);
			return formatView;
		}

		// Should be an ans atom.
		Predicate pred = atom.getFunctionSymbol();
		String view = queryInfo.getSQLAnsViewMap().get(pred);
		viewname = "Q" + pred + "View";
		viewname = sqlAdapter.sqlQuote(viewname);

		if (view != null) {
			String formatView = String.format("(%s) %s", view, viewname);
			return formatView;

		}

		throw new RuntimeException(
				"Impossible to get data definition for: " + atom
						+ ", type: " + def);
	}

	public Collection<String> getColumnReferences(Variable var) {
		return columnReferences.get(var);
	}

	@Override
	public String getColumnName(Variable var) {
		//TODO: why??? 
		Collection<String> posList = getColumnReferences(var);
		if (posList == null || posList.size() == 0) {
			throw new RuntimeException(
					"Unbound variable found in WHERE clause: " + var);
		}
		return posList.iterator().next();
	}

	public Map<Function, String> getViewNames() {
		return viewNames;
	}

	public QueryInfo getQueryInfo() {
		return queryInfo;
	}

}
