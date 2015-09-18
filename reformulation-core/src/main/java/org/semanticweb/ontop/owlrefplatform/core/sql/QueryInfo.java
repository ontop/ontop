package org.semanticweb.ontop.owlrefplatform.core.sql;

import java.util.List;

import org.semanticweb.ontop.model.DatalogProgram;
import org.semanticweb.ontop.model.OrderCondition;
import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.DataDefinition;
import org.semanticweb.ontop.sql.ViewDefinition;

/**
 * A structure to keep meta information of a query, such as
 *  - isDistinct
 *  - isOrderBy
 *  - view definitions of ans predicates other than ans1 in form of metadata 
 */
public class QueryInfo {

	/**
	 * A flag whether the query has distinct modifier
	 */
	private final boolean isDistinct;
	
	/**
	 * A flag whether the query has order by modifier
	 */
	private final boolean isOrderBy;
	
	/**
	 * View definitions of ans predicates
	 */
	private final DBMetadata viewMetadata; 
	

	
	public QueryInfo(DatalogProgram query) {
		this.isDistinct = hasSelectDistinctStatement(query);
		this.isOrderBy = hasOrderByClause(query);
		this.viewMetadata = new DBMetadata("dummy class");
	}

	private QueryInfo(boolean isDistinct, boolean isOrderBy, DBMetadata metadata) {
		this.isDistinct = isDistinct;
		this.isOrderBy = isOrderBy;
		this.viewMetadata = metadata.clone();
	}

	public static boolean hasOrderByClause(DatalogProgram query) {
		boolean toReturn = false;
		if (query.getQueryModifiers().hasModifiers()) {
			final List<OrderCondition> conditions = query.getQueryModifiers().getSortConditions();
			toReturn = (!conditions.isEmpty());
		}
		return toReturn;
	}

	public static boolean hasSelectDistinctStatement(DatalogProgram query) {
		boolean toReturn = false;
		if (query.getQueryModifiers().hasModifiers()) {
			toReturn = query.getQueryModifiers().isDistinct();
		}
		return toReturn;
	}

	public static QueryInfo addAnsViewDefinition(QueryInfo queryInfo, ViewDefinition viewDefinition) {
		DBMetadata metadata = queryInfo.viewMetadata.clone();
		metadata.add(viewDefinition);

		return new QueryInfo(queryInfo.isDistinct, queryInfo.isOrderBy, metadata);
	}


	public DataDefinition getViewDefinition(String tableName) {
		return viewMetadata.getDefinition(tableName);
	}

	public boolean isDistinct() {
		return isDistinct;
	}

	public boolean isOrderBy() {
		return isOrderBy;
	}



}
