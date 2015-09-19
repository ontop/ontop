package it.unibz.krdb.obda.parser;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.sql.Attribute;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.RelationDefinition;
import it.unibz.krdb.sql.api.ParsedSQLQuery;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This visitor class remove * in select clause and substitute it with the columns name
 * Gets the column names or aliases also from the subclasses.
 *
 */

public class PreprocessProjection implements SelectVisitor, SelectItemVisitor, FromItemVisitor {


    private List<SelectItem> columns = new ArrayList<>();


    private boolean selectAll = false;
    private boolean subselect = false;

    private String aliasSubselect ;

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
                    Table tableName;
                    if(aliasSubselect != null){
                        tableName= new Table(aliasSubselect);
                    }
                    else {
                        //use the alias if present
                        if (table.getAlias() != null) {
                            tableName = new Table(table.getAlias().getName());
                        } else {
                            tableName = (Table)table;
                        }
                    }
                    Alias aliasName = ((SelectExpressionItem) expr).getAlias();
                    if (aliasName != null) {

                        String aliasString = aliasName.getName();
                        SelectExpressionItem columnAlias;
                        if(ParsedSQLQuery.pQuotes.matcher(aliasString).matches()) {
                            columnAlias = new SelectExpressionItem(new Column(tableName, aliasString));
                        }
                        else{
                            columnAlias = new SelectExpressionItem(new Column(tableName, "\"" + aliasString + "\""));
                        }

                        //construct a column from alias name
                        if (variables.contains(fac.getVariable(aliasString)) || variables.contains(fac.getVariable(aliasString.toLowerCase()))
                                || variables.contains(fac.getVariable(columnAlias.toString())) || variables.contains(fac.getVariable(aliasString.toString().toLowerCase()))) {

                            columnNames.add(columnAlias);

                        }

                    } else { //when there are no alias add the columns that are used in the mappings

                        String columnName = ((Column)((SelectExpressionItem) expr).getExpression()).getColumnName();
                        SelectExpressionItem column;
                        if(ParsedSQLQuery.pQuotes.matcher(columnName).matches()) {
                            column = new SelectExpressionItem(new Column(tableName, columnName));
                        }
                        else{
                            column = new SelectExpressionItem(new Column(tableName, "\"" + columnName + "\""));
                        }
                        if (variables.contains(fac.getVariable(columnName)) || variables.contains(fac.getVariable(columnName.toLowerCase()))
                                || variables.contains(fac.getVariable(column.toString())) || variables.contains(fac.getVariable(columnName.toString().toLowerCase()))) {
                            columnNames.add(column);

                        }

                    }
                }

            }


        }

        if(!subselect) {
            if(!columnNames.isEmpty())
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

        //obtain the column names from the metadata
        obtainColumnsFromMetadata(tableName);

    }

    @Override
    public void visit(SubSelect subSelect) {

        subselect = true;
        aliasSubselect = subSelect.getAlias().getName();
        subSelect.getSelectBody().accept(this);

        subselect = false;
        aliasSubselect = null;

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
    private void obtainColumnsFromMetadata(Table table) {


        String tableFullName= table.getFullyQualifiedName();
        if (ParsedSQLQuery.pQuotes.matcher(tableFullName).matches()) {
            tableFullName = tableFullName.substring(1, tableFullName.length()-1);
        }
        RelationDefinition tableDefinition = metadata.getDefinition(tableFullName);

        if (tableDefinition == null)
            throw new RuntimeException("Definition not found for table '" + table + "'.");

        Table tableName;
        if (aliasSubselect != null) {
            tableName= new Table(aliasSubselect);
        }
        else if (table.getAlias() != null) { //use the alias if present
            tableName = new Table(table.getAlias().getName());
        }
        else {
            tableName = table;
        }

        for (Attribute att : tableDefinition.getAttributes()) {
            String columnFromMetadata = att.getName();
            SelectExpressionItem columnName;
            if(ParsedSQLQuery.pQuotes.matcher(columnFromMetadata).matches()){
                columnName = new SelectExpressionItem(new Column(tableName,  columnFromMetadata ));
            }
            else {
                columnName = new SelectExpressionItem(new Column(tableName, "\"" + columnFromMetadata + "\""));
            }
            //construct a column as table.column
            if (variables.contains(fac.getVariable(columnFromMetadata))
                    || variables.contains(fac.getVariable(columnFromMetadata.toLowerCase()))
                    || variables.contains(fac.getVariable(columnName.toString()))
                    || variables.contains(fac.getVariable(columnName.toString().toLowerCase()))) {

                columns.add(columnName);

            }
        }
    }







}
