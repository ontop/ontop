package it.unibz.krdb.obda.parser;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DataDefinition;
import it.unibz.krdb.sql.api.ParsedSQLQuery;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This visitor class remove * in select clause and substitute it with the columns name
 * Gets the column names or aliases also from the subclasses.
 *
 */

public class PreprocessProjection implements SelectVisitor, SelectItemVisitor, FromItemVisitor {


    private Set<SelectItem> columns = new HashSet<>();
    private List<SelectItem> aliasColumns = new ArrayList<SelectItem>();

    private boolean selectAll = false;
    private boolean subselect = false;


    final private DBMetadata metadata;
    private Set<Variable> variables;

    private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

    public PreprocessProjection(DBMetadata metadata) throws SQLException {

        //use the metadata to get the column names
        this.metadata = metadata;

    }

    /**
     * Method to substitute * from the select query. It add the columns name that are used in the mapping
     * @param select the query with or without *
     * @param variables the variables used in the mapping, this are the needed columns for our select query
     * @return  the query with columns or functions in the projection part
     */
    public String getMappingQuery(Select select, Set<Variable> variables) {

        this.variables = variables;

        if (select.getWithItemsList() != null) {
            for (WithItem withItem : select.getWithItemsList()) {
                withItem.accept(this);
            }
        }
        select.getSelectBody().accept(this);

        return select.toString();
    }


    /*
    Create a set of selectItem (columns)
    if * add all selectItems obtained by the metadata or the subselect clause
    */

    @Override
    public void visit(PlainSelect plainSelect) {

        List<SelectItem> columnNames = new ArrayList<SelectItem>();

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


                    if(joinTable!=null){
                        joinTable.accept(this);
                        columnNames.addAll(columns);
                        columns.clear();

                    }

                    if(table!=null){
                        table.accept(this);
                        columnNames.addAll(columns);
                        columns.clear();

                }


            }

//				else add only the column
            else {

                if(!subselect) {
                    columnNames.add(expr);
                }
                else { //in case of subselects

                    //see if there is an alias
                    SelectExpressionItem mainColumn = ((SelectExpressionItem) expr);
                    Alias aliasName = mainColumn.getAlias();
                    if (aliasName != null) {

                        String aliasString = aliasName.getName();

                        //construct a column from alias name
                        if (variables.contains(fac.getVariable(aliasString)) || variables.contains(fac.getVariable(aliasString.toLowerCase())) ) {
                            columnNames.add(new SelectExpressionItem(new Column(aliasString)));
                        }

                    } else { //when there are no alias add te columns that are used in the mappings

                        String columnName = ((SelectExpressionItem) expr).getExpression().toString();

                        if (variables.contains(fac.getVariable(columnName)) || variables.contains(fac.getVariable(columnName.toLowerCase()))) {
                            columnNames.add(expr);
                        }

                    }
                }

            }


        }

        if(!subselect) {
            plainSelect.setSelectItems(columnNames);
        }
        else{
            columns.addAll(columnNames);
        }



}

    @Override
    public void visit(SetOperationList setOpList) {

    }

    @Override
    public void visit(WithItem withItem) {

    }

    @Override
    public void visit(AllColumns allColumns) {

        selectAll=true;

    }

    @Override
    public void visit(AllTableColumns allTableColumns) {


        selectAll=true;

    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {

    }

    @Override
    public void visit(Table tableName) {
        columns = obtainColumnsFromMetadata(tableName);
    }

    @Override
    public void visit(SubSelect subSelect) {

        subselect = true;
        subSelect.getSelectBody().accept(this);
        subselect = false;


    }

    @Override
    public void visit(SubJoin subjoin) {

    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {

    }

    @Override
    public void visit(ValuesList valuesList) {

    }

    /*
    Flag for the presence of the * in the query
     */

    private boolean isSelectAll(SelectItem expr){
        expr.accept(this);
        boolean result =selectAll;
        selectAll = false;
        return result;
    }


    /**
     From a table, obtain all its columns using the metadata information
     @return List<SelectItem> list of columns in JSQL SelectItem class the column is rewritten as tableName.columnName or aliasName.columnName
     */
    private Set<SelectItem> obtainColumnsFromMetadata(Table table) {

        Set<SelectItem> columnNames = new HashSet<>();

        String tableFullName= table.getFullyQualifiedName();
        if (ParsedSQLQuery.pQuotes.matcher(tableFullName).matches()) {
            tableFullName = tableFullName.substring(1, tableFullName.length()-1);
        }
        DataDefinition tableDefinition = metadata.getDefinition(tableFullName);

        if (tableDefinition == null) {
            throw new RuntimeException("Definition not found for table '" + table + "'.");
        }

        int size = tableDefinition.getNumOfAttributes();

        Table tableName;
        //use the alias if present
        if(table.getAlias()!=null){
            tableName= new Table(table.getAlias().getName());
        }
        else{
            tableName=table;
        }

        for (int pos = 1; pos <= size; pos++) {


            String columnFromMetadata= metadata.getAttributeName(tableFullName, pos);
            //construct a column as table.column
            if (variables.contains(fac.getVariable(columnFromMetadata)) || variables.contains(fac.getVariable(columnFromMetadata.toLowerCase()))) {

            SelectExpressionItem columnName = new SelectExpressionItem(new Column(tableName, columnFromMetadata));

            columnNames.add(columnName);
            }
        }
        return columnNames;

    }







}
