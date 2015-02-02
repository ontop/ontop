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
 * Gets the column names also from the subclasses.
 *
 */

public class PreprocessProjection implements SelectVisitor, SelectItemVisitor, FromItemVisitor {


    private Set<SelectItem> columns = new HashSet<>();
    private List<SelectItem> aliasColumns = new ArrayList<SelectItem>();

    private boolean selectAll = false;
    private boolean subselect = false;


    final private DBMetadata metadata;
    private Set<Variable> variables;
    Logger log = LoggerFactory.getLogger(this.getClass());
//    private Map<Variable, Variable> variableNewAlias;

    private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

    public PreprocessProjection(DBMetadata metadata) throws SQLException {

        //use the metadata to get the column names
        this.metadata = metadata;

//        variableNewAlias = new HashMap<>();

    }

    /**
     * Method to substitute * from the select query.
     * @param select the query with or without *
     * @param variables see the variables used in the mapping, this are the needed column for our select query
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






    @Override
    public void visit(PlainSelect plainSelect) {

        List<SelectItem> columnNames = new ArrayList<SelectItem>();

        FromItem table = plainSelect.getFromItem();

//        table.accept(this);

        FromItem joinTable = null;

        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                joinTable = join.getRightItem();
//                joinTable.accept(this);
            }
        }


        for (SelectItem expr : plainSelect.getSelectItems()) {


            //create a list of selectItem
            //if * add all selectItems obtained

            if (isSelectAll(expr)) {


//                if (joinTable instanceof Table) {
//                    columnNames.addAll(obtainColumnsFromMetadata((Table) joinTable));
//                }
//                else{
                    if(joinTable!=null){
                        joinTable.accept(this);
                        columnNames.addAll(columns);
                        columns.clear();
//                        if(!aliasColumns.isEmpty()) {
//                            columnNames.addAll(aliasColumns);
//                            aliasColumns.clear();
//                        }
                    }
//                }
//                if (table instanceof Table)
//                    columnNames.addAll(obtainColumnsFromMetadata((Table) table));
//                else{
                    if(table!=null){
                        table.accept(this);
                        columnNames.addAll(columns);
                        columns.clear();
//                        if(!aliasColumns.isEmpty()) {
//                            columnNames.addAll(aliasColumns);
//                            aliasColumns.clear();
//                        }
//                    }
                }


            }

//				else add only the column
            else {

                if(!subselect) {
                    columnNames.add(expr);
                }
                else {
                    //see if there is an alias
                    SelectExpressionItem mainColumn = ((SelectExpressionItem) expr);
                    Alias aliasName = mainColumn.getAlias();
                    if (aliasName != null) {

                        String aliasString = aliasName.getName();

                        //construct a column from alias
                        if (variables.contains(fac.getVariable(aliasString))) {
                            columnNames.add(new SelectExpressionItem(new Column(aliasString)));
                        }
                    } else {

                        if (variables.contains(fac.getVariable(((SelectExpressionItem) expr).getExpression().toString()))) {
                            columnNames.add(expr);
                        }

                    }
                }

            }





        }


        if(columnNames.isEmpty()) {
            log.error("wierd " + columns);
        }
        if(!subselect) {
            plainSelect.setSelectItems(columnNames);
        }
        else{
            columns.addAll(columnNames);
        }


        /**Old


        for (SelectItem expr : plainSelect.getSelectItems()) {


            //create a list of selectItem
            //if * add all selectItems obtained

            if (isSelectAll(expr)) {

                columns.addAll(obtainColumns(plainSelect));


            }


    }

    if(!aliasColumns.isEmpty())

    {

        columns.addAll(aliasColumns);
    }

    if(!columns.isEmpty())

    {

        //substitute * with the column names or aliases
        plainSelect.setSelectItems(columns);
    }

         *
         */

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
//        List<SelectItem> subSelectColumns = new ArrayList<>();
//
//        if (subSelect.getSelectBody() instanceof PlainSelect) {
//
//            PlainSelect subSelBody = (PlainSelect) (subSelect.getSelectBody());
//
//                subSelectColumns = obtainColumns(subSelBody);
//
//                if(!aliasColumns.isEmpty()) {
//                    subSelectColumns.addAll(aliasColumns);
//                    aliasColumns.clear();
//                }
//                    subSelBody.setSelectItems(subSelectColumns);
//        }
//
//        String alias = subSelect.getAlias().getName();
//        buildAliasColumns(subSelectColumns, alias);


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
     @return List<SelectItem> list of columns in JSQL SelectItem class the column is rewritten as tableName.columnName
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
            if (variables.contains(fac.getVariable(columnFromMetadata))) {

            SelectExpressionItem columnName = new SelectExpressionItem(new Column(tableName, columnFromMetadata));

            columnNames.add(columnName);
            }
        }
        return columnNames;

    }





    /**
     * Main method search for * in the select query and in the subselect
     * @param select
     * @return
     */
    private List<SelectItem> obtainColumns(PlainSelect select) {

        List<SelectItem> columnNames = new ArrayList<SelectItem>();

        FromItem table = select.getFromItem();

//        table.accept(this);

        FromItem joinTable = null;

        if (select.getJoins() != null) {
            for (Join join : select.getJoins()) {
                joinTable = join.getRightItem();
//                joinTable.accept(this);
            }
        }


        for (SelectItem expr : select.getSelectItems()) {


            //create a list of selectItem
            //if * add all selectItems obtained

            if (isSelectAll(expr)) {


                if (joinTable instanceof Table) {
                    columnNames.addAll(obtainColumnsFromMetadata((Table) joinTable));
                }
                else{
                    if(joinTable!=null){
                        joinTable.accept(this);

//                        if(!aliasColumns.isEmpty()) {
//                            columnNames.addAll(aliasColumns);
//                            aliasColumns.clear();
//                        }
                    }
                }
                if (table instanceof Table)
                    columnNames.addAll(obtainColumnsFromMetadata((Table) table));
                else{
                    if(table!=null){
                        table.accept(this);

                        if(!aliasColumns.isEmpty()) {
                            columnNames.addAll(aliasColumns);
                            aliasColumns.clear();
                        }
                    }
                }

            }

//				else add only the column
            else {

                columnNames.add(expr);
            }


        }


        return columnNames;


    }

    /** method that given a list of columns in a subselect query s , generate an alias for each of the columns if an alias is not already present.
     * The aliases of each columns are stored in aliasColumns that will be used by the select query containing s.
     *
     * @param subSelectColumns
     * @param alias
     */

    private void buildAliasColumns(List<SelectItem> subSelectColumns, String alias) {

        for (SelectItem column : subSelectColumns) {

            SelectExpressionItem mainColumn = ((SelectExpressionItem) column);
            Alias aliasName = mainColumn.getAlias();
            Column completeColumn = (Column) mainColumn.getExpression();

//            if (aliasName == null) {
//                String columnString = completeColumn.getColumnName();
//                aliasName = new Alias(alias + "_" + completeColumn.getTable() + columnString);
//
//                mainColumn.setAlias(aliasName);
//
//                if (variables.contains(fac.getVariable(columnString))) {
//
//                    variableNewAlias.put(fac.getVariable(columnString), fac.getVariable(aliasName.getName()));
//
//
//                }
//            }

            if (aliasName != null ) {

                String aliasString = aliasName.getName();


                aliasColumns.add(new SelectExpressionItem(new Column(aliasString)));

            }


        }
    }



}
