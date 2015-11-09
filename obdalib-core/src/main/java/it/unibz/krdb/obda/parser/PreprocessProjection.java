package it.unibz.krdb.obda.parser;

import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.sql.*;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.sql.SQLException;
import java.util.*;

/**
 * This visitor class replaces * in SELECT clause with the columns name
 * 
 * Gets the column names or aliases also from the subclasses.
 *
 */

public class PreprocessProjection {

    private List<SelectItem> columns = new ArrayList<>();

    private final DBMetadata metadata;
    private final QuotedIDFactory idfac;
    
    public PreprocessProjection(DBMetadata metadata) throws SQLException {
        // we use the metadata to get the column names
        this.metadata = metadata;
        this.idfac = metadata.getQuotedIDFactory();
    }

    /**
     * Method to substitute * from the select query. It add the columns name that are used in the mapping
     * @param select the query with or without *
     * @param variables the variables used in the mapping, this are the needed columns for our select query
     * @return  the query with columns or functions in the projection part
     */
    public String getMappingQuery(Select select, Set<Variable> variables) {

    	VariableSet variableNames = new VariableSet(variables);
    	
         if (select.getWithItemsList() != null) {
            for (WithItem withItem : select.getWithItemsList()) 
                withItem.accept(new ReplaceStarSelectVisitor(false, null, variableNames));
        }
        select.getSelectBody().accept(new ReplaceStarSelectVisitor(false, null, variableNames));

        return select.toString();
    }

    
    /**
     * implements the case-insensitive comparison 
     * (to be replaced in the future)
     */
    private static class VariableSet {

    	private Set<String> variableNames = new HashSet<>();

    	VariableSet(Set<Variable> variables) {
        	for (Variable var : variables) 
        		variableNames.add(var.getName().toLowerCase());
    	}

    	boolean contains(String qualifiedColumnName, String columnName) {
    	       return variableNames.contains(qualifiedColumnName.toLowerCase())
    	               || variableNames.contains(columnName.toLowerCase());
    	}
    }
    
    
    private class ReplaceStarSelectVisitor implements SelectVisitor {
    
        private final boolean subselect;
        private final String aliasSubselect;
        private final VariableSet variables; // referenced variables from the target query
        
        ReplaceStarSelectVisitor(boolean subselect, String aliasSubselect, VariableSet variables) {
        	this.subselect = subselect;
        	this.aliasSubselect = aliasSubselect;
        	this.variables = variables;
        }
    	
        /*
        Create a set of selectItem (columns)
        if * add all selectItems obtained by the metadata or the subselect clause
        */

        @Override
        public void visit(PlainSelect plainSelect) {

            List<SelectItem> columnNames = new LinkedList<>();

            //get the from clause (can have subselect)
            FromItem table = plainSelect.getFromItem();

            FromItem joinTable = null;

            //get the join clause (can have subselect)
            if (plainSelect.getJoins() != null) {
                for (Join join : plainSelect.getJoins()) {
                    joinTable = join.getRightItem();
                }
            }

            // look at the projection clause
            for (SelectItem expr : plainSelect.getSelectItems()) {

                //create a set of selectItem (columns)
                //if * add all selectItems obtained by the metadata or the subselect clause

                if (isSelectAll(expr)) {

                    if (joinTable != null) {
                        joinTable.accept(new ReplaceStarFromItemVisitor(aliasSubselect, variables));
                        columnNames.addAll(columns);
                        columns.clear();
                    }

                    if (table != null) {
                        table.accept(new ReplaceStarFromItemVisitor(aliasSubselect, variables));
                        columnNames.addAll(columns);
                        columns.clear();
                    }
                }
                else {
                	// else add only the column

                    if (!subselect) {
                        columnNames.add(expr);
                    }
                    else { //in case of subselects

                        Table tableName;
                        if (aliasSubselect != null) // if there is an alias for the subquery
                            tableName = new Table(aliasSubselect);
                        else if (table.getAlias() != null) // if there is an alias for the table
                            tableName = new Table(table.getAlias().getName());
                        else 
                            tableName = (Table)table;

                        SelectExpressionItem selectExpression = (SelectExpressionItem) expr;
                        Alias alias = selectExpression.getAlias();
                        String columnName;
                        if (alias != null) 
                        	columnName = alias.getName();
                        else  // when there are no alias add the columns that are used in the mappings
                            columnName = ((Column)selectExpression.getExpression()).getColumnName();
                            
                        Column column = new Column(tableName,  columnName);
                            
                        if (variables.contains(column.getFullyQualifiedName(), columnName))
                        	columns.add(new SelectExpressionItem(column));
                    }
                }
            }

            if (!subselect) {
                if (!columnNames.isEmpty())
                	plainSelect.setSelectItems(columnNames);
            }
            else {
                columns.addAll(columnNames);
            }
        }

    	@Override
    	public void visit(SetOperationList setOpList) {
    		// ??
    	}

    	@Override
    	public void visit(WithItem withItem) {
    		// ??
    	}
    }
    
    private class ReplaceStarFromItemVisitor implements FromItemVisitor {
    
    	private final String aliasSubselect;
    	private final VariableSet variables;
    	
    	ReplaceStarFromItemVisitor(String aliasSubselect, VariableSet variables) {
    		this.aliasSubselect = aliasSubselect;
    		this.variables = variables;
    	}
    	
        @Override
        public void visit(Table table) {
            //obtain the column names from the metadata
     	   RelationID tableID = idfac.createRelationID(table.getSchemaName(), table.getName());
           RelationDefinition tableDefinition = metadata.getRelation(tableID);
           if (tableDefinition == null)
               throw new RuntimeException("Definition not found for table '" + table + "'.");

           Table tableName;
           if (aliasSubselect != null) 
               tableName = new Table(aliasSubselect);
           else if (table.getAlias() != null)  //use the alias if present
               tableName = new Table(table.getAlias().getName());
           else 
               tableName = table;

           for (Attribute att : tableDefinition.getAttributes()) {
               // ROMAN (9 Oct 2015)
               // the unquoted name is used for comparisons
               Column columnNameUnquoted = new Column(tableName, att.getID().getSQLRendering());
              
               if (variables.contains(columnNameUnquoted.getFullyQualifiedName(), att.getID().getName())) {
            	   // properly quoted name if necessary 
                   Column columnName = new Column(tableName, att.getID().getSQLRendering());
                   columns.add(new SelectExpressionItem(columnName));
               }
           }
        }

        @Override
        public void visit(SubSelect subSelect) {
            subSelect.getSelectBody().accept(new ReplaceStarSelectVisitor(true, subSelect.getAlias().getName(), variables));
        }

        @Override
        public void visit(SubJoin subjoin) {
        	// ??
        }

        @Override
        public void visit(LateralSubSelect lateralSubSelect) {
        	// NO-OP
        }

        @Override
        public void visit(ValuesList valuesList) {
        	// NO-OP
        }   
    }
        
    /*
    Flag for the presence of the * in the query
     */

    private static boolean isSelectAll(SelectItem expr) {
    	ReplaceStarSelectItemVisitor visitor = new ReplaceStarSelectItemVisitor();
        expr.accept(visitor);
        return visitor.selectAll;
    }

    private static class ReplaceStarSelectItemVisitor implements SelectItemVisitor {

        boolean selectAll = false;
   	
        @Override
        public void visit(AllColumns allColumns) {
            selectAll = true;
        }

        @Override
        public void visit(AllTableColumns allTableColumns) {
            selectAll = true;
        }

        @Override
        public void visit(SelectExpressionItem selectExpressionItem) {
        	// NO-OP
        }
    }
    
   
    


}
