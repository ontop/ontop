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

import java.sql.SQLException;
import java.util.*;

/**
 * This visitor class remove * in select clause and substitute it with the columns name
 * Gets the column names also from the subclasses.
 *
 */

public class PreprocessProjection implements SelectVisitor, SelectItemVisitor, FromItemVisitor {


    private List<SelectItem> columns = new ArrayList<SelectItem>();
    private List<SelectItem> aliasColumns = new ArrayList<SelectItem>();

    private boolean selectAll = false;


    final private DBMetadata metadata;
    final private Set<Variable> variables;
    private Map<Variable, Variable> variableNewAlias;

    private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

    public PreprocessProjection(DBMetadata metadata, Set<Variable> variables) throws SQLException {

        //use the metadata to get the column names
        this.metadata = metadata;
        this.variables = variables;
        variableNewAlias = new HashMap<>();

    }


    /**
    From a table, obtain all its columns using the metadata information
     @return List<SelectItem> list of columns in JSQL SelectItem class the column is rewritten as tableName.columnName
     */
    private List<SelectItem> obtainColumnsFromMetadata(Table table) {

        List<SelectItem> columnNames = new ArrayList<>();

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
//            if (variables.contains(fac.getVariable(columnFromMetadata))) {

                SelectExpressionItem columnName = new SelectExpressionItem(new Column(tableName, columnFromMetadata));

                columnNames.add(columnName);
//            }
        }
        return columnNames;

    }

    /**
     * Method to substitute * from the select query.
     * @param select the query with or without *
     * @return  the query with columns or functions in the projection part
     */
    public String getMappingQuery(Select select) {

        if (select.getWithItemsList() != null) {
            for (WithItem withItem : select.getWithItemsList()) {
                withItem.accept(this);
            }
        }
        select.getSelectBody().accept(this);

        return select.toString();
    }

    private boolean isSelectAll(SelectItem expr){
        expr.accept(this);
        boolean result =selectAll;
        selectAll = false;
        return result;
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

                        if(!aliasColumns.isEmpty()) {
                            columnNames.addAll(aliasColumns);
                            aliasColumns.clear();
                        }
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



    @Override
    public void visit(PlainSelect plainSelect) {


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

    }

    @Override
    public void visit(SubSelect subSelect) {

        List<SelectItem> subSelectColumns = new ArrayList<>();

        if (subSelect.getSelectBody() instanceof PlainSelect) {

            PlainSelect subSelBody = (PlainSelect) (subSelect.getSelectBody());

                subSelectColumns = obtainColumns(subSelBody);

                if(!aliasColumns.isEmpty()) {
                    subSelectColumns.addAll(aliasColumns);
                    aliasColumns.clear();
                }
                    subSelBody.setSelectItems(subSelectColumns);
        }

        String alias = subSelect.getAlias().getName();
        buildAliasColumns(subSelectColumns, alias);


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



}
