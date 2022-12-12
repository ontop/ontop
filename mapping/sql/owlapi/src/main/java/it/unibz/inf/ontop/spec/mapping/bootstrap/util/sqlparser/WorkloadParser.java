package it.unibz.inf.ontop.spec.mapping.bootstrap.util.sqlparser;

import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.JoinPairs;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.Pair;
import it.unibz.inf.ontop.spec.sqlparser.JSqlParserTools;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class WorkloadParser {
    Logger logger = LoggerFactory.getLogger("workloadParserLogger");
    public JoinPairs parseQuery(String sql) throws JSQLParserException, InvalidQueryException {

        Map<String, String> alias2TableMap = new HashMap<>(); // CONTRACT: alias2TableMap Given a table of name "t", should return "t" if such table is not aliased

        JoinPairs joinPairs = new JoinPairs();

        SelectBody parsed = JSqlParserTools.parse(sql).getSelectBody();
        if( !(parsed instanceof PlainSelect) ){
            logger.debug("Complex SELECT statements are not supported " + parsed);
            return joinPairs; // return empty joinPairs
        }
        PlainSelect plainSelect = JSqlParserTools.getPlainSelect(parsed);

        if( plainSelect.getJoins() == null ){
            return joinPairs;
        }
        if( plainSelect.getFromItem().getAlias() == null ){
            // TODO> Handle non-aliased case
            handleNonAliased(plainSelect, alias2TableMap, joinPairs);
        }else{
            handleAliased(plainSelect, alias2TableMap, joinPairs);
        }
        return joinPairs;
    }

    private void handleNonAliased(PlainSelect plainSelect, Map<String, String> alias2TableMap, JoinPairs joinPairs) {
        String fromTable = fromTableName(plainSelect.getFromItem().toString());
        String fromAlias = fromTable;

        alias2TableMap.put(fromAlias, fromTable);

        JoinConditionAdapter visitor = new JoinConditionAdapter(joinPairs, alias2TableMap);
        for( Join j : plainSelect.getJoins() ){
            if( j.getRightItem() instanceof Table ){
                String tableName = ((Table) j.getRightItem()).getFullyQualifiedName();
                alias2TableMap.put(tableName, tableName);
            }
            if( j.getOnExpressions().size() > 0 ){
                List<Expression> onExps = new ArrayList<>(j.getOnExpressions());
                for( Expression onExp : onExps ){
                    onExp.accept(visitor);  // side-effect on visitor
                }
            }
        }

        if(plainSelect.getWhere() != null ){
            plainSelect.getWhere().accept(visitor);
        }
        visitor.populateJoinPairs();
    }

    private void handleAliased(PlainSelect plainSelect, Map<String, String> alias2TableMap, JoinPairs joinPairs) {
        String fromAlias = plainSelect.getFromItem().getAlias().getName();
        String fromTable = fromTableName(plainSelect.getFromItem().toString());

        alias2TableMap.put(fromAlias, fromTable);

        JoinConditionAdapter visitor = new JoinConditionAdapter(joinPairs, alias2TableMap);
        for( Join j : plainSelect.getJoins() ){
            if( j.getRightItem() instanceof Table ){
                String tableName = ((Table) j.getRightItem()).getFullyQualifiedName();
                String alias = j.getRightItem().getAlias().getName();
                alias2TableMap.put(alias, tableName);
            }

            if( j.getOnExpressions().size() > 0 ){
                List<Expression> onExps = new ArrayList<>(j.getOnExpressions());
                for( Expression onExp : onExps ){
                     onExp.accept(visitor);  // side-effect on visitor
                }
            }
        }

        if(plainSelect.getWhere() != null ){
            plainSelect.getWhere().accept(visitor);
        }
        visitor.populateJoinPairs();
    }

    private String fromTableName(String fromTable) {
        String[] splits = fromTable.split("AS"); // TABLE AS ALIAS
        if(splits.length == 1){
            splits = fromTable.split(" "); // TABLE ALIAS
        }
        return splits[0].trim();
    }

    class JoinConditionAdapter extends ExpressionVisitorAdapter {

        private final Map<String, String> alias2TableMap;
        private JoinPairs joinPairs;
        private List<Equality> equalities = new ArrayList<>(); // AT THE END OF THE VISITING< THIS WILL CONTAIN
        // ALL THE EQUALITIES IN THE QUERY

        private class Equality {
            Pair<Pair<String, String>, Pair<String, String>> equality;

            Equality(Column leftColumn, Column rightColumn) {
                String leftColumnColumnName = leftColumn.getColumnName();
                String leftColumnTableName = leftColumn.getTable().getFullyQualifiedName(); // Alias or not?

                String rightColumnColumnName = rightColumn.getColumnName();
                String rightColumnTableName = rightColumn.getTable().getFullyQualifiedName();

                this.equality = new Pair<>(new Pair<>(leftColumnTableName, leftColumnColumnName), new Pair<>(rightColumnTableName, rightColumnColumnName));
            }

            Equality(Equality eq) {
                this.equality = new Pair<>(new Pair<>(eq.getLeftTable(), eq.getLeftAttribute()), new Pair<>(eq.getRightTable(), eq.getRightAttribute()));
            }

            private Equality(Pair<String, String> left, Pair<String, String> right){
                this.equality = new Pair<>(left, right);
            }

            String getLeftTable() {
                return this.equality.first().first();
            }

            String getLeftAttribute() {
                return this.equality.first().second();
            }

            String getRightTable() {
                return this.equality.second().first();
            }

            String getRightAttribute() {
                return this.equality.second().second();
            }

            // Sort left-right according to lexicographic order
            private Equality sortAndRenameEquality(Map<String, String> alias2TableMap) {

                String leftTable = alias2TableMap.get(this.getLeftTable()); // Was stripQuotesAndLower(...)
                String rightTable = alias2TableMap.get(this.getRightTable()); // Was stripQuotesAndLower(...)

                Equality copy = new Equality(this);

                if (leftTable.compareTo(rightTable) > 0) {
                    // Sort
                    copy.equality = new Pair<>(new Pair<>(rightTable, getRightAttribute())
                            , new Pair<>(leftTable, getLeftAttribute()));
                } else {
                    // Rename-only
                    copy.equality = new Pair<>(new Pair<>(leftTable, getLeftAttribute())
                            , new Pair<>(rightTable, getRightAttribute()));
                }
                return copy;
            }

            Equality getInvertedEquality(){
                return new Equality(this.equality.second(), this.equality.first());
            }

            private String stripQuotesAndLower(String identifier) {
                return identifier.replaceAll("\"", "").toLowerCase();
            }
        }

        public JoinConditionAdapter(JoinPairs joinPairs, Map<String, String> alias2TableMap) {
            super();
            this.joinPairs = joinPairs;
            this.alias2TableMap = alias2TableMap;
        }

        /**
         * Strategy: Sort equalities (left-right) according to a global order over the tables.
         *           Case 1) T1.C1 = T2.C2 AND T1.C3 = T2.C4 -> Then produce (C1,C2) = (C3,C4)
         *           Case 2) T1.C1 = T2.C3 AND T1.C2 = T3.C4 -> Then produce C1=C3 AND C2=C4 (not a multi-attribute "dependency")
         *
         *           For case 1), having an order means processing either T1 or T2 first. This makes no difference at this level,
         *                        however the order will be important for outside methods. SO OK. TODO Check where the sort is used.
         *           For case 2), having an order means processing things that, in any case, are meant to be kept separated. SO OK.
         */
        public void populateJoinPairs() {
            // Sort equalities (left-right), and lower-case everything
            List<Equality> sortedAndRenamedEqualities = this.equalities.stream()
                    .map(eq -> eq.sortAndRenameEquality(alias2TableMap))
                    .collect(Collectors.toList());

            // 1) Group together equalities where left and right are on the same table (but different attributes)
            List<List<Equality>> groupedEqualities = groupEqualities(sortedAndRenamedEqualities);

            // 2) Create a joinPair for each group
            for( List<Equality> group : groupedEqualities ){
                List<String> lefts = new ArrayList<>();
                List<String> rights = new ArrayList<>();
                for( Equality eq : group ) {
                    lefts.add(eq.getLeftTable() + "." + eq.getLeftAttribute());
                    rights.add(eq.getRightTable() + "." + eq.getRightAttribute());
                };
                joinPairs.addJoinPair(lefts, rights);
            }
        }

        // Group together equalities where left AND right are the same
        // Reason: In order to recognize the following case:
        // Case 1) T1.C1 = T2.C2 AND T1.C3 = T2.C4 -> Then produce (C1,C2) = (C3,C4)
        // However, ensure different attributes! E.g.,
        // T1.C1 = T2.C2 AND T1.C1 = T2.C4 -> Then produce (C3,C4) = (C1,C2) (reversed)
        private List<List<Equality>> groupEqualities(List<Equality> sortedAndRenamedEqualities) {
            List<Integer> tabooList = new ArrayList<>(); // contains all equalities that have been added to a group
            List<List<Equality>> groupedEqualities = new ArrayList<>();
            for( int i = 0; i < sortedAndRenamedEqualities.size(); ++i ){
                if(tabooList.contains(i)) continue;
                tabooList.add(i);
                String left = sortedAndRenamedEqualities.get(i).getLeftTable();
                String right = sortedAndRenamedEqualities.get(i).getRightTable();
                groupedEqualities.add(new ArrayList<>());
                groupedEqualities.get(groupedEqualities.size()-1).add(sortedAndRenamedEqualities.get(i));
                for( int j = i+1; j < sortedAndRenamedEqualities.size(); ++j ){
                    if( tabooList.contains(j) ) continue;
                    String leftJ = sortedAndRenamedEqualities.get(j).getLeftTable();
                    String rightJ = sortedAndRenamedEqualities.get(j).getRightTable();
                    if( left.equals(leftJ) && right.equals(rightJ) ){
                        groupedEqualities.get(groupedEqualities.size()-1).add(sortedAndRenamedEqualities.get(j));
                        tabooList.add(j);
                    }
                }
            }
            // At this point, if T1.C1 = T2.C2 AND T1.C1 = T2.C4, then the previous loop produced (C1,C2) = (C3,C4).
            // Instead, we want the inverse (C3,C4) = (C1,C2)
            List<List<Equality>> result = invertNonCompliantEqualities(groupedEqualities);
            return result;
        }

        private List<List<Equality>> invertNonCompliantEqualities(List<List<Equality>> groupedEqualities) {
            List<List<Equality>> result = new ArrayList<>();
            for( List<Equality> group : groupedEqualities ){
                List<String> encounteredAtts = new ArrayList<>();
                boolean reverseGroup = false;
                for( Equality eq : group ){
                    if( encounteredAtts.contains(eq.getLeftAttribute()) ) {
                        reverseGroup = true;
                    }
                    encounteredAtts.add(eq.getLeftAttribute());
                }
                if(reverseGroup){
                    List<Equality> reversedEqs = new ArrayList<>();
                    for( Equality eq : group ){
                        reversedEqs.add(eq.getInvertedEquality());
                    }
                    result.add(reversedEqs);
                }
                else result.add(group);
            }
            return result;
        }

        public void populateJoinPairs_bak() {
            // Sort by table
            // Sort equalities (left-right)
            // Populate
            List<Equality> sortedAndRenamedEqualities = this.equalities.stream()
                    .map(eq -> eq.sortAndRenameEquality(alias2TableMap))
                    .collect(Collectors.toList());

            String lastTable = sortedAndRenamedEqualities.get(0).getLeftTable();
            List<String> lefts = new ArrayList<>();
            List<String> rights = new ArrayList<>();
            for (Equality eq : sortedAndRenamedEqualities) {
                if (eq.getLeftTable().equals(lastTable)) {
                    // Still populating the same joinPair
                    lefts.add(eq.getLeftTable() + "." + eq.getLeftAttribute());
                    rights.add(eq.getRightTable() + "." + eq.getRightAttribute());
                } else {
                    // 1) Create JoinPair
                    joinPairs.addJoinPair(lefts, rights);
                    // 2) reset
                    lefts = new ArrayList<>();
                    rights = new ArrayList<>();
                }
                lastTable = eq.getLeftTable();
            }
            // Create latest joinPair
            joinPairs.addJoinPair(lefts, rights);
        }

        private List<String> sortTablesByOccurrences(List<Equality> sortedAndRenamedEqualities) {
            Map<String, Integer> occurrencesPerTable = new HashMap<>();

            for( Equality eq : sortedAndRenamedEqualities ){
                if( occurrencesPerTable.containsKey(eq.getLeftTable()) ){
                    occurrencesPerTable.put(eq.getLeftTable(), occurrencesPerTable.get(eq.getLeftTable()+1));
                }
                else{
                    occurrencesPerTable.put(eq.getLeftTable(), 0);
                }
                if( occurrencesPerTable.containsKey(eq.getRightTable()) ){
                    occurrencesPerTable.put(eq.getRightTable(), occurrencesPerTable.get(eq.getRightTable()+1));
                }
                else{
                    occurrencesPerTable.put(eq.getRightTable(), 0);
                }
            }

            List<String> sortedTables = occurrencesPerTable.keySet().stream().sorted(
                    new Comparator<String>() {
                        @Override
                        public int compare(String s, String t) {
                            return occurrencesPerTable.get(s).compareTo(occurrencesPerTable.get(t));
                        }
                    }
            ).collect(Collectors.toList());
            return sortedTables;
        }

        @Override
        protected void visitBinaryExpression(BinaryExpression expr) {

            if (expr instanceof ComparisonOperator) {
                if (expr.getLeftExpression() instanceof Column && expr.getRightExpression() instanceof Column) {
                    Column leftColumn = (Column) expr.getLeftExpression();
                    Column rightColumn = (Column) expr.getRightExpression();

                    if (leftColumn.getTable() != null && rightColumn.getTable() != null) {
                        // For the moment, I only consider joins over fully-qualified conditions.
                        this.equalities.add(new Equality(leftColumn, rightColumn));
                    }
                }
            } else if (expr instanceof AndExpression) {
                AndExpression andExpression = (AndExpression) expr;
                andExpression.getLeftExpression().accept(this);
                andExpression.getRightExpression().accept(this);
            }
        }

        @Override
        public String toString() {
            return this.equalities.toString();
        }
    }
}
