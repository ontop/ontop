package it.unibz.krdb.obda.parser;

import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DataDefinition;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This visitor class remove * in select clause and substitute it with the columns name
 */

public class PreprocessProjection implements SelectVisitor, SelectItemVisitor, FromItemVisitor {


    private List<SelectItem> columns = new ArrayList<SelectItem>();
    private List<SelectItem> aliasColumns = new ArrayList<SelectItem>();

    private boolean selectAll = false;


    final private DBMetadata metadata;

    public PreprocessProjection(DBMetadata metadata) throws SQLException {

        this.metadata = metadata;

    }

    private List<SelectItem> obtainColumnsFromMetadata(Table table) {

        List<SelectItem> columnNames = new ArrayList<>();

        DataDefinition tableDefinition = metadata.getDefinition(table.getFullyQualifiedName());

        if (tableDefinition == null) {
            throw new RuntimeException("Definition not found for table '" + table + "'.");
        }

        int size = tableDefinition.getNumOfAttributes();

        for (int pos = 1; pos <= size; pos++) {

            //construct a column as table.column

            Table tableName;
        if(table.getAlias()!=null){
            tableName= new Table(table.getAlias().getName());
        }
            else{
            tableName=table;
        }
            SelectExpressionItem columnName = new SelectExpressionItem(new Column(tableName, metadata.getAttributeName(table.getFullyQualifiedName(), pos)));

            columnNames.add(columnName);
        }
        return columnNames;

    }

    public String getMappingQuery(Select select) throws JSQLParserException {

        if (select.getWithItemsList() != null) {
            for (WithItem withItem : select.getWithItemsList()) {
                withItem.accept(this);
            }
        }
        select.getSelectBody().accept(this);

        return select.toString();
    }

    private List<SelectItem> obtainColumns(PlainSelect select) {

        List<SelectItem> columnNames = new ArrayList<SelectItem>();

        FromItem table = select.getFromItem();

        table.accept(this);

        FromItem joinTable = null;

        if (select.getJoins() != null) {
            for (Join join : select.getJoins()) {
                joinTable = join.getRightItem();
                joinTable.accept(this);
            }
        }



        for (SelectItem expr : select.getSelectItems()) {
            expr.accept(this);

            //create a list of selectItem
            //if * add all selectItems obtained

            if (selectAll) {


                if (joinTable instanceof Table) {
                    columnNames.addAll(obtainColumnsFromMetadata((Table) joinTable));
                }
                if (table instanceof Table)
                    columnNames.addAll(obtainColumnsFromMetadata((Table) table));


            }

//				else add only the column
            else {

                columnNames.add(expr);
            }

            selectAll = false;
        }


        return columnNames;


    }

    private void buildAliasColumns(List<SelectItem> subSelectColumns, String alias) {

        for (SelectItem column : subSelectColumns) {

            SelectExpressionItem mainColumn = ((SelectExpressionItem) column);
            Alias aliasName=  mainColumn.getAlias();
            if(aliasName==null) {
                aliasName = new Alias (alias + "_" + mainColumn.getExpression().toString());
//            Column maincolumn = new Column (new Table(alias), columnName);

                mainColumn.setAlias(aliasName);
            }
//            mainColumn.setExpression(maincolumn);
//            Table table = new Table(alias);
//            SelectExpressionItem mainColumn = new SelectExpressionItem(new Column(table, ));
//            mainColumn.setAlias(new Alias(table + "_" + column.toString()));
//            aliasColumns.add(mainColumn);
            aliasColumns.add(new SelectExpressionItem(new Column(aliasName.getName())));
        }
    }


    @Override
    public void visit(PlainSelect plainSelect) {


        for (SelectItem expr : plainSelect.getSelectItems()) {
            expr.accept(this);

            //create a list of selectItem
            //if * add all selectItems obtained

//            if (!selectAll) {
//                columns.add(expr);
//            }

            if (selectAll) {

                selectAll = false;


                columns.addAll(obtainColumns(plainSelect));


            }


    }

    if(!aliasColumns.isEmpty())

    {

        columns.addAll(aliasColumns);
    }

    if(!columns.isEmpty())

    {
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

            if (!(subSelBody.getJoins() != null) ) { // we do not support if the subqueries have joins

                subSelectColumns = obtainColumns(subSelBody);

                if(!aliasColumns.isEmpty()) {
                    subSelectColumns.addAll(aliasColumns);
                    aliasColumns.clear();
                }
                    subSelBody.setSelectItems(subSelectColumns);

            }
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
