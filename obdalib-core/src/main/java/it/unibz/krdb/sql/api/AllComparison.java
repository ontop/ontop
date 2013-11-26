package it.unibz.krdb.sql.api;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * Auxiliary Class used to visualize AnyComparison in string format.
 * Any and Some are the same in SQL so we consider always the case of ANY
 *
 */

public class AllComparison extends AllComparisonExpression{

	public AllComparison(SubSelect subSelect) {
		super(subSelect);
	}
	
	@Override
	public String toString(){
		return "ALL "+getSubSelect();
	}

}
