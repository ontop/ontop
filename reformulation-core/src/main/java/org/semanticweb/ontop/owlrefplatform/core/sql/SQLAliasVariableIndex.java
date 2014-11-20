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
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * 
 * An object of this class is created for a CQ with the purpose to relate 
 * atoms and their variables to tables and column names.
 * 
 * E.g., assume a CQ
 *   
 * 		ans1(t1) :- student(t1, t2, t3, t4), EQ(t4,2)
 * 
 * and a DB table 
 * 
 * 		student(id, name, email, type).
 * 
 * 
 * The following information would be created/stored for the atom student(t1, t2, t3, t4):
 *   
 *  -- the data definition is the table student(id, name, email, type),
 *   
 *  -- the view name (rather, alias in this case) QstudentView0 is created
 *  
 *  -- each variable is linked to the following column references:
 *  
 *      ** variable t1 to "QstudentView0"."id", 
 *      ** variable t2 to "QstudentView0"."name", 
 *      ** variable t3 to "QstudentView0"."email", and 
 *      ** variable t4 to "QstudentView0"."type".
 * 
 * 
 * All this information is used to obtain an SQL query of this form
 * 
 *       select "QstudentView0"."id" from student QstudentView0 where "QstudentView0"."type"=4;
 *       
 *       
 * View names are needed for selfjoins, e.g., 
 *
 * 		ans1(t1) :- student(t1, t2, t3, t4), student(s1, s2, s3, s4), EQ(t4,s4), NEQ(t1,s1)
 * 
 */
public class SQLAliasVariableIndex extends QueryVariableIndex{

	static final String VIEW_NAME = "Q%sVIEW%s";
	
	/**
	 * View name for each data atom (not table name) 
	 */
	private final ImmutableMap<Function, String> viewNames;
	
	/**
	 * Table definitions from metadata and view definitions from queryInfo
	 * for atoms (not table names) 
	 */
	private final ImmutableMap<Function, DataDefinition> dataDefinitions;
	
	/**
	 * The names of columns to which variables refer, more precisely strings of the form
	 * TableOrViewName.ColumnName.
	 * Determined by the position of the variable in atoms.
	 */
	private final ImmutableMultimap<Variable, String> columnReferences;

	private final SQLDialectAdapter sqlAdapter;
	
	/**
	 * Contains some meta information about the main datalog program query:
	 *  - isDistinct
	 *  - isOrderBy
	 *  - view definitions for ans predicates other than ans1
	 */
	private final QueryInfo queryInfo;
	

	/**
	 * All indexing is done in the constructor. First mutable maps and multimaps are constructed,
	 * then their immutable versions are saved.
	 * 
	 * Here we take into account not only table definitions from metadata, but also view definition in queryInfo.
	 */
	public SQLAliasVariableIndex(CQIE cq, DBMetadata metadata, QueryInfo queryInfo, SQLDialectAdapter sqlDialectAdapter) {
		super(cq, metadata);
		
		Map<Function, String> viewNames = new HashMap<Function, String>(); 
		Map<Function, DataDefinition> dataDefinitions = new HashMap<Function, DataDefinition>();
		Multimap<Variable, String> columnReferences = HashMultimap.create();

		generateViews(cq.getBody(), metadata, queryInfo, viewNames, dataDefinitions, 
				columnReferences, sqlDialectAdapter);

		this.sqlAdapter = sqlDialectAdapter;
		this.queryInfo = queryInfo;
		
		this.viewNames = ImmutableMap.copyOf(viewNames);
		this.dataDefinitions = ImmutableMap.copyOf(dataDefinitions);
		this.columnReferences = ImmutableMultimap.copyOf(columnReferences);
	}

	private static void generateViews(List<Function> atoms, DBMetadata metadata, QueryInfo queryInfo, 
									Map<Function, String> viewNames, 
									Map<Function, DataDefinition> dataDefinitions,  
									Multimap<Variable, String> columnReferences, 
									SQLDialectAdapter sqlDialectAdapter) {
		for (Function atom : atoms) {
			/*
			 * This will be called recursively if necessary
			 */
			generateViewsIndexVariables(atom, metadata, queryInfo, viewNames,
					dataDefinitions, columnReferences, sqlDialectAdapter);
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
	 */
	private static void generateViewsIndexVariables(Function atom, DBMetadata metadata, QueryInfo queryInfo, 
								Map<Function, String> viewNames, 
								Map<Function, DataDefinition> dataDefinitions, 
								Multimap<Variable, String> columnReferences, 
								SQLDialectAdapter sqlDialectAdapter) {
		
		if (atom.getFunctionSymbol() instanceof BooleanOperationPredicate) {
			return;
		} 
		
		else if (atom.getFunctionSymbol() instanceof AlgebraOperatorPredicate) {
			List<Term> terms = atom.getTerms();
			for (Term subatom : terms) {
				if (subatom instanceof Function) {
					generateViewsIndexVariables((Function) subatom, metadata, queryInfo, 
							viewNames, dataDefinitions, columnReferences, sqlDialectAdapter);
				}
			}
		}

		String tableName = atom.getFunctionSymbol().getName();
		DataDefinition dataDefinition = metadata.getDefinition(tableName);

		String viewName = null;
		if (dataDefinition == null) {
			/**
			 * There is no definition for this atom, so its not a database
			 * predicate. We check whether it has a view definition in queryInfo:
			 */
			viewName = NewSQLGenerator.createAnsViewName(tableName);
			dataDefinition = queryInfo.getViewDefinition(viewName);
			if (dataDefinition == null) {
				//TODO: throw an Exception?
				return;
			}
		} else {
			/**
			 * For database predicates we give a different view name. 
			 */
			viewName = createViewName(tableName, String.valueOf(dataDefinitions.size()));
		}
		
		/**
		 * For each atom we record the view name and 
		 * the data definition of the view
		 */
		viewNames.put(atom, viewName);
		dataDefinitions.put(atom, dataDefinition);

		indexVariables(atom, viewName, dataDefinition, columnReferences, sqlDialectAdapter);
	}

	/**
	 * Creates column references for each variable in atom. 
	 * The references are of the form ViewName.ColumnName
	 * 
	 * @param atom
	 * @param viewName
	 * 		the name of the view corresponding to atom
	 * @param dataDefinition
	 * 		table definition corresponding to atom
	 * @param columnReferences
	 * @param sqlDialectAdapter 
	 */
	private static void indexVariables(final Function atom,  
									final String viewName, 
									final DataDefinition dataDefinition, 
									Multimap<Variable, String> columnReferences, 
									final SQLDialectAdapter sqlDialectAdapter) {

		/**
		 * if atom is a database predicate, then the viewName is different from 
		 * the name of dataDefinition. Otherwise it is an ans predicate.
		 */
		boolean isAnsPredicate = ( viewName.equals(dataDefinition.getName()) );

		String quotedViewName = sqlDialectAdapter.sqlQuote(viewName);

		for (int index = 0; index < atom.getTerms().size(); index++) {
			Term term = atom.getTerm(index);

			if (term instanceof Variable) {
				
				int attPos;
				if (isAnsPredicate) {
					/**
					 * dataDefinition is a View Definition of an ans predicate.
					 * There 3 times more attributes in view definitions.
					 */
					attPos = 3 * (index + 1);
				} else {
					/**
					 * the index of attributes of the definition starts from 1
					 */
					attPos = index + 1;
				}
				String columnName = NewSQLGenerator.removeAllQuotes( dataDefinition.getAttributeName(attPos) );
				
				String reference = sqlDialectAdapter.sqlQualifiedColumn(quotedViewName, columnName);
				columnReferences.put((Variable) term, reference);
			}
		}
	}

	/***
	 * Returns for an atom either
	 *  - the table alias, i.e., tableName viewName, 
	 * or
	 *  - the view definition and alias, i.e., "(View Definition Query) viewName".
	 */
	public String getViewDefinition(Function atom) {
		DataDefinition dataDefinition = dataDefinitions.get(atom);
		String viewName = sqlAdapter.sqlQuote(viewNames.get(atom));

		if (dataDefinition instanceof TableDefinition) {
			return sqlAdapter.sqlTableName(dataDefinition.getName(), viewName);
		}
		
		/**
		 * We have an ans atom.
		 */
		String viewDefinition = ((ViewDefinition) dataDefinition).getStatement();
		if (viewDefinition != null) {
			String formatView = String.format("(%s) %s", viewDefinition, viewName);
			return formatView;
		}

		throw new RuntimeException("Impossible to get data definition for: "
				+ atom + ", type: " + dataDefinition);
	}

	@Override
	public String getColumnName(Variable var) {
		Collection<String> columnRefs = getColumnReferences(var);
		return columnRefs.iterator().next();
	}

	public Collection<String> getColumnReferences(Variable var) {
		Collection<String> columnRefs = columnReferences.get(var);
        if (columnRefs == null || columnRefs.size() == 0) {
            throw new RuntimeException(
                    "Unbound variable found in WHERE clause: " + var);
        }
        return columnRefs;
	}

	public static String createViewName(String tableName, String number) {
		return String.format(VIEW_NAME, tableName, number);
	}

	/**
	 * Returns the original table name (that is, predicate's name)
	 * of a view that is not an ans view. 
	 */
	public String findTableName(String viewName) {
		for (Map.Entry<Function, String> pair : viewNames.entrySet()) {
			if (pair.getValue().equals(viewName)) {
				Function function = pair.getKey();
				return function.getFunctionSymbol().toString();
			}
		}
		return viewName;
	}

	public DataDefinition getViewDefinition(String tableName) {
		return queryInfo.getViewDefinition(tableName);
	}

	public boolean isDistinct() {
		return queryInfo.isDistinct();
	}

	public boolean isOrderBy() {
		return queryInfo.isOrderBy();
	}
	


}
