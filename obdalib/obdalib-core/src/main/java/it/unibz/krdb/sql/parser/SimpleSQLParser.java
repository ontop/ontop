// $ANTLR 3.1.2 /home/obda/SimpleSQL.g 2009-09-09 10:41:24

package it.unibz.krdb.sql.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.TreeAdaptor;


public class SimpleSQLParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "Number", "COMMA", "LPAREN", "RPAREN", "StringLiteral", "Identifier", "LSQUARE", "RSQUARE", "DOT", "DateLiteral", "MINUS", "TILDE", "PLUS", "STAR", "DIVIDE", "MOD", "AMPERSAND", "BITWISEOR", "BITWISEXOR", "EQUAL", "NOTEQUAL", "LESSTHANOREQUALTO", "LESSTHAN", "GREATERTHANOREQUALTO", "GREATERTHAN", "COLON", "SEMICOLON", "Letter", "Digit", "Exponent", "WS", "'select'", "'SELECT'", "'all'", "'distinct'", "'ALL'", "'DISTINCT'", "'where'", "'WHERE'", "'limit'", "'LIMIT'", "'order'", "'by'", "'ORDER'", "'BY'", "'asc'", "'desc'", "'ASC'", "'DESC'", "'and'", "'or'", "'AND'", "'OR'", "'group'", "'not'", "'NOT'", "'is not'", "'IS NOT'", "'like'", "'LIKE'", "'in'", "'IN'", "'is null'", "'IS NULL'", "'is not null'", "'IS NOT NULL'", "'as'", "'from'", "'FROM'", "'AS'", "'case'", "'when'", "'then'", "'else'", "'end'", "'any'", "'exists'", "'some'", "'ANY'", "'EXISTS'", "'SOME'", "'true'", "'false'", "'TRUE'", "'FALSE'"
    };
    public static final int T__68=68;
    public static final int T__69=69;
    public static final int T__66=66;
    public static final int T__67=67;
    public static final int STAR=17;
    public static final int T__64=64;
    public static final int LSQUARE=10;
    public static final int T__65=65;
    public static final int T__62=62;
    public static final int T__63=63;
    public static final int MOD=19;
    public static final int BITWISEXOR=22;
    public static final int Exponent=33;
    public static final int T__61=61;
    public static final int EOF=-1;
    public static final int T__60=60;
    public static final int Identifier=9;
    public static final int LPAREN=6;
    public static final int NOTEQUAL=24;
    public static final int T__55=55;
    public static final int T__56=56;
    public static final int GREATERTHANOREQUALTO=27;
    public static final int RPAREN=7;
    public static final int T__57=57;
    public static final int T__58=58;
    public static final int T__51=51;
    public static final int T__52=52;
    public static final int Number=4;
    public static final int T__53=53;
    public static final int T__54=54;
    public static final int COMMA=5;
    public static final int DateLiteral=13;
    public static final int T__59=59;
    public static final int EQUAL=23;
    public static final int TILDE=15;
    public static final int PLUS=16;
    public static final int DOT=12;
    public static final int T__50=50;
    public static final int DIVIDE=18;
    public static final int GREATERTHAN=28;
    public static final int T__42=42;
    public static final int T__43=43;
    public static final int LESSTHAN=26;
    public static final int T__40=40;
    public static final int T__41=41;
    public static final int T__80=80;
    public static final int T__46=46;
    public static final int T__81=81;
    public static final int T__47=47;
    public static final int T__82=82;
    public static final int T__44=44;
    public static final int T__83=83;
    public static final int T__45=45;
    public static final int T__48=48;
    public static final int T__49=49;
    public static final int AMPERSAND=20;
    public static final int SEMICOLON=30;
    public static final int MINUS=14;
    public static final int RSQUARE=11;
    public static final int Digit=32;
    public static final int T__85=85;
    public static final int T__84=84;
    public static final int T__87=87;
    public static final int BITWISEOR=21;
    public static final int T__86=86;
    public static final int T__88=88;
    public static final int COLON=29;
    public static final int StringLiteral=8;
    public static final int WS=34;
    public static final int T__71=71;
    public static final int T__72=72;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int T__70=70;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int LESSTHANOREQUALTO=25;
    public static final int T__76=76;
    public static final int T__75=75;
    public static final int T__74=74;
    public static final int Letter=31;
    public static final int T__73=73;
    public static final int T__79=79;
    public static final int T__78=78;
    public static final int T__77=77;

    // delegates
    // delegators


        public SimpleSQLParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public SimpleSQLParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return SimpleSQLParser.tokenNames; }
    public String getGrammarFileName() { return "/home/obda/SimpleSQL.g"; }


    public static class TableSource
    {
    private String table;
    private List<String> lstColumns = new ArrayList<String>(1);
    public void addColumn(String column) { lstColumns.add(column); }
    public List<String> getColumns() { return lstColumns; }
    public String getTable() { return table; }
    public void setTable(String table) { this.table = table; }
    }

    public static interface IConstant {}

    public static class NumberConstant implements IConstant
    {
    private double number;
    public NumberConstant(String sNumber) { this.number = Double.parseDouble(sNumber); }
    public double getNumber() { return number; }
    }

    public static class StringConstant implements IConstant
    {
    private String text;
    public StringConstant(String text) { this.text = text; }
    public String getText() { return text; }
    }

    public static class DateConstant implements IConstant
    {
    private String date;
    public DateConstant(String date) { this.date = date; }
    public String getDate() { return date; }
    }

    public static class BooleanConstant implements IConstant
    {
    private boolean value;
    public BooleanConstant(String booleanValue) { this.value = booleanValue.equalsIgnoreCase("true"); }
    public boolean getValue() { return value; }
    }


    public static class Function
    {
    private String functionId;
    private List<IExpression> lstParameter = new ArrayList<IExpression>(1);
    public void setFunctionId(String functionId) { this.functionId = functionId; }
    public void addParameter(IExpression parameter) { lstParameter.add(parameter); }
    public String getFunctionId() { return functionId; }
    public List<IExpression> getParameters() { return lstParameter; }
    }

    public static class SelectItem
    {
    private IExpression what;
    private String name;
    public SelectItem(IExpression what, String name) { this.what = what; this.name = name; }
    public SelectItem(String name) {this.what = null; this.name = name; }
    public IExpression getWhat() { return what; }
    public String getName() { return name; }
    }

    public static class TableColumn
    {
    private String table;
    private String column;
    public TableColumn(String table, String column) { this.table = table; this.column = column; }
    public String getTable() { return table; }
    public String getColumn() { return column; }
    public String toString() { return table+"."+column; }
    }

    public static class When
    {
    private TableColumn test;
    private IConstant compared;
    private String operator;
    public When(TableColumn test, IConstant compared, String operator)
    {
    this.test = test;
    this.compared = compared;
    this.operator = operator;
    }
    public TableColumn getTest() { return test; }
    public IConstant getCompared() { return compared; }
    public String getOperator() { return operator; }
    }

    public static class WhenThen
    {
    private When when;
    private IExpression then;
    public WhenThen(When when, IExpression then)
    {
    this.when = when;
    this.then = then;
    }
    public When getWhen() { return when; }
    public IExpression getThen() { return then; }
    }

    public static class CaseFunction
    {
    private TableColumn test;
    private List<WhenThen> lstWhenThen = new ArrayList<WhenThen>(1);
    private IExpression expElse;
    public void setTest(TableColumn test) { this.test = test; }
    public void addWhenThen(WhenThen whenThen) { lstWhenThen.add(whenThen); }
    public void setElse(IExpression expElse) { this.expElse = expElse; }
    public TableColumn getTest() { return test; }
    public List<WhenThen> getWhenThens() { return lstWhenThen; }
    public IExpression getElse() { return expElse; }

    }

    public static interface IExpression {}


    public static class SimpleExpression implements IExpression
    {
    private SubExpression sub;
    public SimpleExpression(SubExpression sub) { this.sub = sub; }
    public SubExpression getSubExpression() { return sub; }
    }

    public static class ComplexExpression implements IExpression
    {
    private IExpression a;
    private IExpression b;
    private String operator;
    public ComplexExpression(IExpression a, IExpression b, String operator) 
    { 
    this.a = a; 
    this.b = b;
    this.operator = operator;
    }
    public IExpression getA() { return a; }
    public IExpression getB() { return b; }
    public String getOperator() { return operator; }
    }

    public static abstract class SubExpression 
    {
    private String unaryOperator = null;
    public void setUnaryOperator(String unaryOperator) { this.unaryOperator = unaryOperator; }
    public String getUnaryOperator() { return unaryOperator; }
    }

    public static class ConstantSubExpression extends SubExpression
    {
    private IConstant constant;
    public ConstantSubExpression(IConstant constant) { this.constant = constant; }
    public IConstant getConstant() { return constant; }
    }

    public static class FunctionSubExpression extends SubExpression
    {
    private Function function;
    public FunctionSubExpression(Function function) { this.function = function; }
    public Function getFunction() { return function; }
    }

    public static class ExpressionSubExpression extends SubExpression
    {
    private IExpression expression;
    public ExpressionSubExpression(IExpression expression) { this.expression = expression; }
    public IExpression getExpression() { return expression; }
    }

    public static class TableColumnSubExpression extends SubExpression
    {
    private TableColumn tableColumn;
    public TableColumnSubExpression(TableColumn tableColumn) { this.tableColumn = tableColumn; }
    public TableColumn getTableColumn() { return tableColumn; }
    }

    public static class CaseSubExpression extends SubExpression
    {
    private CaseFunction caseFunction;
    public CaseSubExpression(CaseFunction caseFunction) { this.caseFunction = caseFunction; }
    public CaseFunction getCase() { return caseFunction; }
    }

    public static interface IPredicate {}

    public static class Comparison implements IPredicate
    {
    private IExpression test;
    private IExpression compared;
    private String operator;
    public Comparison(IExpression test, IExpression compared, String operator)
    {
    this.test = test;
    this.compared = compared;
    this.operator = operator;
    }
    public IExpression getTest() { return test; }
    public IExpression getCompared() { return compared; }
    public String getOperator() { return operator; }
    }
    public static abstract class NegatePredicate implements IPredicate
    {
    private boolean negated = false;
    public void setNegated(boolean negated) { this.negated = negated; }
    public boolean isNegated() { return negated; }
    }

    public static class Like extends NegatePredicate
    {
    private IExpression test;
    private String compared;
    public Like(IExpression test, String compared)
    {
    this.test = test;
    if(compared != null)
    this.compared = compared.substring(1, compared.length() - 1);
    }
    public IExpression getTest() { return test; }
    public String getCompared() { return compared; }
    }

    public static class In extends NegatePredicate
    {
    private IExpression test;
    private List<IConstant> lstConstants;
    public In(IExpression test, List<IConstant> lstConstants)
    {
    this.test = test;
    this.lstConstants = lstConstants;
    }
    public IExpression getTest() { return test; }
    public List<IConstant> getConstants() { return lstConstants; }
    }

    public static interface ISearchCondition {}


    public static class SimpleSearchCondition implements ISearchCondition
    {
    private SubSearchCondition sub;
    public SimpleSearchCondition(SubSearchCondition sub) { this.sub = sub; }
    public SubSearchCondition getSubSearchCondition() { return sub; }
    }

    public static class ComplexSearchCondition implements ISearchCondition
    {
    private ISearchCondition a;
    private ISearchCondition b;
    private String operator;
    public ComplexSearchCondition(ISearchCondition a, ISearchCondition b, String operator) 
    { 
    this.a = a; 
    this.b = b;
    this.operator = operator;
    }
    public ISearchCondition getA() { return a; }
    public ISearchCondition getB() { return b; }
    public String getOperator() { return operator; }
    }

    public static abstract class SubSearchCondition
    {
    private boolean negated;
    public void setNegated(boolean negated) { this.negated = negated; }
    public boolean getNegated() { return negated; }
    }

    public static class RecursiveCondition extends SubSearchCondition
    {
    private ISearchCondition condition;
    public RecursiveCondition(ISearchCondition condition) { this.condition = condition; }
    public ISearchCondition getSearchCondition() { return condition; }
    }

    public static class PredicateCondition extends SubSearchCondition
    {
    private IPredicate predicate;
    public PredicateCondition(IPredicate predicate) { this.predicate = predicate; }
    public IPredicate getPredicate() { return predicate; }
    }

    public static class OrderBy
    {
    private TableColumn orderBy;
    private boolean ascendent = true;
    public OrderBy(TableColumn orderBy)
    {
    this.orderBy = orderBy;
    }
    public TableColumn getOrderBy() { return orderBy; }
    public void setAscendent(boolean ascendent) { this.ascendent = ascendent; }
    public boolean isAscendent() { return ascendent; }
    }


    private Map<String, TableSource> mapTableSource = new TreeMap<String, TableSource>();
    private void addTableSource(TableSource tableSource) { 
    if(tableSource != null) 
    {
    String table = tableSource.getTable();
    if(table != null)
    mapTableSource.put(table, tableSource); 
    }
    }
    public Map<String, TableSource> getTableSources() { return mapTableSource; }

    private List<SelectItem> lstSelectItem = new ArrayList<SelectItem>(1);
    private void addSelectItem(SelectItem item) { lstSelectItem.add(item); }
    public List<SelectItem> getSelectItems() { return lstSelectItem; }

    private List<TableColumn> lstGroupBy = new ArrayList<TableColumn>(1);
    private void addGroupBy(TableColumn groupBy) { lstGroupBy.add(groupBy); }
    public List<TableColumn> getGroupBys() { return lstGroupBy; }

    private List<OrderBy> lstOrderBy = new ArrayList<OrderBy>(1);
    private void addOrderBy(OrderBy orderBy) { lstOrderBy.add(orderBy); }
    public List<OrderBy> getOrderBys() { return lstOrderBy; }

    private ISearchCondition where;
    public ISearchCondition getWhere() { return where; }

    private boolean distinct;
    public boolean isDistinct() { return distinct; }


    public static class parse_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "parse"
    // /home/obda/SimpleSQL.g:348:1: parse : statement ;
    public final SimpleSQLParser.parse_return parse() throws RecognitionException {
        SimpleSQLParser.parse_return retval = new SimpleSQLParser.parse_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        SimpleSQLParser.statement_return statement1 = null;



        try {
            // /home/obda/SimpleSQL.g:348:8: ( statement )
            // /home/obda/SimpleSQL.g:348:11: statement
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_statement_in_parse49);
            statement1=statement();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, statement1.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "parse"

    public static class statement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "statement"
    // /home/obda/SimpleSQL.g:351:1: statement : selectStatement EOF ;
    public final SimpleSQLParser.statement_return statement() throws RecognitionException {
        SimpleSQLParser.statement_return retval = new SimpleSQLParser.statement_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token EOF3=null;
        SimpleSQLParser.selectStatement_return selectStatement2 = null;


        Object EOF3_tree=null;

        try {
            // /home/obda/SimpleSQL.g:352:5: ( selectStatement EOF )
            // /home/obda/SimpleSQL.g:352:7: selectStatement EOF
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_selectStatement_in_statement62);
            selectStatement2=selectStatement();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, selectStatement2.getTree());
            EOF3=(Token)match(input,EOF,FOLLOW_EOF_in_statement64); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            EOF3_tree = (Object)adaptor.create(EOF3);
            adaptor.addChild(root_0, EOF3_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "statement"

    public static class selectStatement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selectStatement"
    // /home/obda/SimpleSQL.g:355:1: selectStatement : selectClause fromClause ( whereClause )? ( groupByClause )? ( orderByClause )? ;
    public final SimpleSQLParser.selectStatement_return selectStatement() throws RecognitionException {
        SimpleSQLParser.selectStatement_return retval = new SimpleSQLParser.selectStatement_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        SimpleSQLParser.selectClause_return selectClause4 = null;

        SimpleSQLParser.fromClause_return fromClause5 = null;

        SimpleSQLParser.whereClause_return whereClause6 = null;

        SimpleSQLParser.groupByClause_return groupByClause7 = null;

        SimpleSQLParser.orderByClause_return orderByClause8 = null;



        try {
            // /home/obda/SimpleSQL.g:356:5: ( selectClause fromClause ( whereClause )? ( groupByClause )? ( orderByClause )? )
            // /home/obda/SimpleSQL.g:357:5: selectClause fromClause ( whereClause )? ( groupByClause )? ( orderByClause )?
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_selectClause_in_selectStatement82);
            selectClause4=selectClause();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, selectClause4.getTree());
            pushFollow(FOLLOW_fromClause_in_selectStatement88);
            fromClause5=fromClause();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, fromClause5.getTree());
            // /home/obda/SimpleSQL.g:359:5: ( whereClause )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( ((LA1_0>=41 && LA1_0<=42)) ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // /home/obda/SimpleSQL.g:359:6: whereClause
                    {
                    pushFollow(FOLLOW_whereClause_in_selectStatement95);
                    whereClause6=whereClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, whereClause6.getTree());

                    }
                    break;

            }

            // /home/obda/SimpleSQL.g:360:5: ( groupByClause )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==57) ) {
                alt2=1;
            }
            else if ( (LA2_0==47) ) {
                int LA2_2 = input.LA(2);

                if ( (LA2_2==48) ) {
                    int LA2_4 = input.LA(3);

                    if ( (LA2_4==Identifier) ) {
                        int LA2_5 = input.LA(4);

                        if ( (LA2_5==DOT) ) {
                            int LA2_6 = input.LA(5);

                            if ( (LA2_6==Identifier) ) {
                                int LA2_7 = input.LA(6);

                                if ( (synpred2_SimpleSQL()) ) {
                                    alt2=1;
                                }
                            }
                        }
                    }
                }
            }
            switch (alt2) {
                case 1 :
                    // /home/obda/SimpleSQL.g:360:6: groupByClause
                    {
                    pushFollow(FOLLOW_groupByClause_in_selectStatement104);
                    groupByClause7=groupByClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, groupByClause7.getTree());

                    }
                    break;

            }

            // /home/obda/SimpleSQL.g:361:5: ( orderByClause )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==45||LA3_0==47) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // /home/obda/SimpleSQL.g:361:6: orderByClause
                    {
                    pushFollow(FOLLOW_orderByClause_in_selectStatement113);
                    orderByClause8=orderByClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, orderByClause8.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "selectStatement"

    public static class selectClause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selectClause"
    // /home/obda/SimpleSQL.g:365:1: selectClause : ( 'select' | 'SELECT' ) (selectHow= ( 'all' | 'distinct' | 'ALL' | 'DISTINCT' ) )? selectList ;
    public final SimpleSQLParser.selectClause_return selectClause() throws RecognitionException {
        SimpleSQLParser.selectClause_return retval = new SimpleSQLParser.selectClause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token selectHow=null;
        Token set9=null;
        SimpleSQLParser.selectList_return selectList10 = null;


        Object selectHow_tree=null;
        Object set9_tree=null;

        try {
            // /home/obda/SimpleSQL.g:366:5: ( ( 'select' | 'SELECT' ) (selectHow= ( 'all' | 'distinct' | 'ALL' | 'DISTINCT' ) )? selectList )
            // /home/obda/SimpleSQL.g:367:4: ( 'select' | 'SELECT' ) (selectHow= ( 'all' | 'distinct' | 'ALL' | 'DISTINCT' ) )? selectList
            {
            root_0 = (Object)adaptor.nil();

            set9=(Token)input.LT(1);
            if ( (input.LA(1)>=35 && input.LA(1)<=36) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set9));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            // /home/obda/SimpleSQL.g:367:25: (selectHow= ( 'all' | 'distinct' | 'ALL' | 'DISTINCT' ) )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( ((LA4_0>=37 && LA4_0<=40)) ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // /home/obda/SimpleSQL.g:367:26: selectHow= ( 'all' | 'distinct' | 'ALL' | 'DISTINCT' )
                    {
                    selectHow=(Token)input.LT(1);
                    if ( (input.LA(1)>=37 && input.LA(1)<=40) ) {
                        input.consume();
                        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(selectHow));
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    if ( state.backtracking==0 ) {
                       distinct=((selectHow!=null?selectHow.getText():null).equalsIgnoreCase("distinct")); 
                    }

                    }
                    break;

            }

            pushFollow(FOLLOW_selectList_in_selectClause175);
            selectList10=selectList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, selectList10.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "selectClause"

    public static class whereClause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "whereClause"
    // /home/obda/SimpleSQL.g:372:1: whereClause : ( 'where' | 'WHERE' ) searchCondition ( ( 'limit' | 'LIMIT' ) Number )* ;
    public final SimpleSQLParser.whereClause_return whereClause() throws RecognitionException {
        SimpleSQLParser.whereClause_return retval = new SimpleSQLParser.whereClause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set11=null;
        Token set13=null;
        Token Number14=null;
        SimpleSQLParser.searchCondition_return searchCondition12 = null;


        Object set11_tree=null;
        Object set13_tree=null;
        Object Number14_tree=null;

        try {
            // /home/obda/SimpleSQL.g:373:5: ( ( 'where' | 'WHERE' ) searchCondition ( ( 'limit' | 'LIMIT' ) Number )* )
            // /home/obda/SimpleSQL.g:374:4: ( 'where' | 'WHERE' ) searchCondition ( ( 'limit' | 'LIMIT' ) Number )*
            {
            root_0 = (Object)adaptor.nil();

            set11=(Token)input.LT(1);
            if ( (input.LA(1)>=41 && input.LA(1)<=42) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set11));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            pushFollow(FOLLOW_searchCondition_in_whereClause204);
            searchCondition12=searchCondition();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, searchCondition12.getTree());
            if ( state.backtracking==0 ) {
               where = (searchCondition12!=null?searchCondition12.c:null); 
            }
            // /home/obda/SimpleSQL.g:375:5: ( ( 'limit' | 'LIMIT' ) Number )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0>=43 && LA5_0<=44)) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // /home/obda/SimpleSQL.g:375:6: ( 'limit' | 'LIMIT' ) Number
            	    {
            	    set13=(Token)input.LT(1);
            	    if ( (input.LA(1)>=43 && input.LA(1)<=44) ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set13));
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    Number14=(Token)match(input,Number,FOLLOW_Number_in_whereClause219); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    Number14_tree = (Object)adaptor.create(Number14);
            	    adaptor.addChild(root_0, Number14_tree);
            	    }

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "whereClause"

    public static class orderByClause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "orderByClause"
    // /home/obda/SimpleSQL.g:379:1: orderByClause : ( 'order' 'by' | 'ORDER' 'BY' ) first= orderByExpression ( COMMA other= orderByExpression )* ;
    public final SimpleSQLParser.orderByClause_return orderByClause() throws RecognitionException {
        SimpleSQLParser.orderByClause_return retval = new SimpleSQLParser.orderByClause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal15=null;
        Token string_literal16=null;
        Token string_literal17=null;
        Token string_literal18=null;
        Token COMMA19=null;
        SimpleSQLParser.orderByExpression_return first = null;

        SimpleSQLParser.orderByExpression_return other = null;


        Object string_literal15_tree=null;
        Object string_literal16_tree=null;
        Object string_literal17_tree=null;
        Object string_literal18_tree=null;
        Object COMMA19_tree=null;

        try {
            // /home/obda/SimpleSQL.g:380:5: ( ( 'order' 'by' | 'ORDER' 'BY' ) first= orderByExpression ( COMMA other= orderByExpression )* )
            // /home/obda/SimpleSQL.g:381:4: ( 'order' 'by' | 'ORDER' 'BY' ) first= orderByExpression ( COMMA other= orderByExpression )*
            {
            root_0 = (Object)adaptor.nil();

            // /home/obda/SimpleSQL.g:381:4: ( 'order' 'by' | 'ORDER' 'BY' )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==45) ) {
                alt6=1;
            }
            else if ( (LA6_0==47) ) {
                alt6=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // /home/obda/SimpleSQL.g:381:6: 'order' 'by'
                    {
                    string_literal15=(Token)match(input,45,FOLLOW_45_in_orderByClause245); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    string_literal15_tree = (Object)adaptor.create(string_literal15);
                    adaptor.addChild(root_0, string_literal15_tree);
                    }
                    string_literal16=(Token)match(input,46,FOLLOW_46_in_orderByClause247); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    string_literal16_tree = (Object)adaptor.create(string_literal16);
                    adaptor.addChild(root_0, string_literal16_tree);
                    }

                    }
                    break;
                case 2 :
                    // /home/obda/SimpleSQL.g:381:20: 'ORDER' 'BY'
                    {
                    string_literal17=(Token)match(input,47,FOLLOW_47_in_orderByClause250); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    string_literal17_tree = (Object)adaptor.create(string_literal17);
                    adaptor.addChild(root_0, string_literal17_tree);
                    }
                    string_literal18=(Token)match(input,48,FOLLOW_48_in_orderByClause252); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    string_literal18_tree = (Object)adaptor.create(string_literal18);
                    adaptor.addChild(root_0, string_literal18_tree);
                    }

                    }
                    break;

            }

            pushFollow(FOLLOW_orderByExpression_in_orderByClause257);
            first=orderByExpression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, first.getTree());
            if ( state.backtracking==0 ) {
               addOrderBy((first!=null?first.o:null)); 
            }
            // /home/obda/SimpleSQL.g:382:5: ( COMMA other= orderByExpression )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==COMMA) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /home/obda/SimpleSQL.g:382:6: COMMA other= orderByExpression
            	    {
            	    COMMA19=(Token)match(input,COMMA,FOLLOW_COMMA_in_orderByClause266); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA19_tree = (Object)adaptor.create(COMMA19);
            	    adaptor.addChild(root_0, COMMA19_tree);
            	    }
            	    pushFollow(FOLLOW_orderByExpression_in_orderByClause270);
            	    other=orderByExpression();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, other.getTree());
            	    if ( state.backtracking==0 ) {
            	       addOrderBy((other!=null?other.o:null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "orderByClause"

    public static class orderByExpression_return extends ParserRuleReturnScope {
        public OrderBy o;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "orderByExpression"
    // /home/obda/SimpleSQL.g:385:1: orderByExpression returns [ OrderBy o] : orderBy= tableColumn (ascDesc= ( 'asc' | 'desc' | 'ASC' | 'DESC' ) )? ;
    public final SimpleSQLParser.orderByExpression_return orderByExpression() throws RecognitionException {
        SimpleSQLParser.orderByExpression_return retval = new SimpleSQLParser.orderByExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ascDesc=null;
        SimpleSQLParser.tableColumn_return orderBy = null;


        Object ascDesc_tree=null;

        try {
            // /home/obda/SimpleSQL.g:386:5: (orderBy= tableColumn (ascDesc= ( 'asc' | 'desc' | 'ASC' | 'DESC' ) )? )
            // /home/obda/SimpleSQL.g:387:5: orderBy= tableColumn (ascDesc= ( 'asc' | 'desc' | 'ASC' | 'DESC' ) )?
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_tableColumn_in_orderByExpression307);
            orderBy=tableColumn();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, orderBy.getTree());
            if ( state.backtracking==0 ) {
               retval.o = new OrderBy((orderBy!=null?orderBy.t:null)); 
            }
            // /home/obda/SimpleSQL.g:388:5: (ascDesc= ( 'asc' | 'desc' | 'ASC' | 'DESC' ) )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( ((LA8_0>=49 && LA8_0<=52)) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // /home/obda/SimpleSQL.g:388:6: ascDesc= ( 'asc' | 'desc' | 'ASC' | 'DESC' )
                    {
                    ascDesc=(Token)input.LT(1);
                    if ( (input.LA(1)>=49 && input.LA(1)<=52) ) {
                        input.consume();
                        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(ascDesc));
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    if ( state.backtracking==0 ) {
                        retval.o.setAscendent((ascDesc!=null?ascDesc.getText():null).equalsIgnoreCase("asc")); 
                    }

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "orderByExpression"

    public static class searchCondition_return extends ParserRuleReturnScope {
        public ISearchCondition c;;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "searchCondition"
    // /home/obda/SimpleSQL.g:392:1: searchCondition returns [ISearchCondition c;] : first= subSearchCondition (operator= ( 'and' | 'or' | 'AND' | 'OR' ) other= subSearchCondition )* ;
    public final SimpleSQLParser.searchCondition_return searchCondition() throws RecognitionException {
        SimpleSQLParser.searchCondition_return retval = new SimpleSQLParser.searchCondition_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token operator=null;
        SimpleSQLParser.subSearchCondition_return first = null;

        SimpleSQLParser.subSearchCondition_return other = null;


        Object operator_tree=null;

        try {
            // /home/obda/SimpleSQL.g:393:5: (first= subSearchCondition (operator= ( 'and' | 'or' | 'AND' | 'OR' ) other= subSearchCondition )* )
            // /home/obda/SimpleSQL.g:394:5: first= subSearchCondition (operator= ( 'and' | 'or' | 'AND' | 'OR' ) other= subSearchCondition )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_subSearchCondition_in_searchCondition364);
            first=subSearchCondition();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, first.getTree());
            if ( state.backtracking==0 ) {
               retval.c = new SimpleSearchCondition((first!=null?first.s:null)); 
            }
            // /home/obda/SimpleSQL.g:395:5: (operator= ( 'and' | 'or' | 'AND' | 'OR' ) other= subSearchCondition )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( ((LA9_0>=53 && LA9_0<=56)) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // /home/obda/SimpleSQL.g:395:7: operator= ( 'and' | 'or' | 'AND' | 'OR' ) other= subSearchCondition
            	    {
            	    operator=(Token)input.LT(1);
            	    if ( (input.LA(1)>=53 && input.LA(1)<=56) ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(operator));
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_subSearchCondition_in_searchCondition393);
            	    other=subSearchCondition();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, other.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.c = new ComplexSearchCondition(retval.c, new SimpleSearchCondition((other!=null?other.s:null)), (operator!=null?operator.getText():null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "searchCondition"

    public static class groupByClause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "groupByClause"
    // /home/obda/SimpleSQL.g:398:1: groupByClause : ( 'group' 'by' | 'ORDER' 'BY' ) groupBy= tableColumn ( COMMA tableColumn )* ;
    public final SimpleSQLParser.groupByClause_return groupByClause() throws RecognitionException {
        SimpleSQLParser.groupByClause_return retval = new SimpleSQLParser.groupByClause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal20=null;
        Token string_literal21=null;
        Token string_literal22=null;
        Token string_literal23=null;
        Token COMMA24=null;
        SimpleSQLParser.tableColumn_return groupBy = null;

        SimpleSQLParser.tableColumn_return tableColumn25 = null;


        Object string_literal20_tree=null;
        Object string_literal21_tree=null;
        Object string_literal22_tree=null;
        Object string_literal23_tree=null;
        Object COMMA24_tree=null;

        try {
            // /home/obda/SimpleSQL.g:399:5: ( ( 'group' 'by' | 'ORDER' 'BY' ) groupBy= tableColumn ( COMMA tableColumn )* )
            // /home/obda/SimpleSQL.g:400:4: ( 'group' 'by' | 'ORDER' 'BY' ) groupBy= tableColumn ( COMMA tableColumn )*
            {
            root_0 = (Object)adaptor.nil();

            // /home/obda/SimpleSQL.g:400:4: ( 'group' 'by' | 'ORDER' 'BY' )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==57) ) {
                alt10=1;
            }
            else if ( (LA10_0==47) ) {
                alt10=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // /home/obda/SimpleSQL.g:400:6: 'group' 'by'
                    {
                    string_literal20=(Token)match(input,57,FOLLOW_57_in_groupByClause426); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    string_literal20_tree = (Object)adaptor.create(string_literal20);
                    adaptor.addChild(root_0, string_literal20_tree);
                    }
                    string_literal21=(Token)match(input,46,FOLLOW_46_in_groupByClause428); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    string_literal21_tree = (Object)adaptor.create(string_literal21);
                    adaptor.addChild(root_0, string_literal21_tree);
                    }

                    }
                    break;
                case 2 :
                    // /home/obda/SimpleSQL.g:400:20: 'ORDER' 'BY'
                    {
                    string_literal22=(Token)match(input,47,FOLLOW_47_in_groupByClause431); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    string_literal22_tree = (Object)adaptor.create(string_literal22);
                    adaptor.addChild(root_0, string_literal22_tree);
                    }
                    string_literal23=(Token)match(input,48,FOLLOW_48_in_groupByClause433); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    string_literal23_tree = (Object)adaptor.create(string_literal23);
                    adaptor.addChild(root_0, string_literal23_tree);
                    }

                    }
                    break;

            }

            pushFollow(FOLLOW_tableColumn_in_groupByClause438);
            groupBy=tableColumn();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, groupBy.getTree());
            if ( state.backtracking==0 ) {
               addGroupBy((groupBy!=null?groupBy.t:null)); 
            }
            // /home/obda/SimpleSQL.g:401:5: ( COMMA tableColumn )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0==COMMA) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // /home/obda/SimpleSQL.g:401:6: COMMA tableColumn
            	    {
            	    COMMA24=(Token)match(input,COMMA,FOLLOW_COMMA_in_groupByClause447); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA24_tree = (Object)adaptor.create(COMMA24);
            	    adaptor.addChild(root_0, COMMA24_tree);
            	    }
            	    pushFollow(FOLLOW_tableColumn_in_groupByClause449);
            	    tableColumn25=tableColumn();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, tableColumn25.getTree());
            	    if ( state.backtracking==0 ) {
            	       addGroupBy((groupBy!=null?groupBy.t:null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "groupByClause"

    public static class subSearchCondition_return extends ParserRuleReturnScope {
        public SubSearchCondition s;;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "subSearchCondition"
    // /home/obda/SimpleSQL.g:407:1: subSearchCondition returns [SubSearchCondition s; ] : ( ( 'not' | 'NOT' | 'is not' | 'IS NOT' ) )? ( ( ( LPAREN searchCondition RPAREN )=> LPAREN recCondition= searchCondition RPAREN ) | (predCondition= predicate ) ) ;
    public final SimpleSQLParser.subSearchCondition_return subSearchCondition() throws RecognitionException {
        SimpleSQLParser.subSearchCondition_return retval = new SimpleSQLParser.subSearchCondition_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set26=null;
        Token LPAREN27=null;
        Token RPAREN28=null;
        SimpleSQLParser.searchCondition_return recCondition = null;

        SimpleSQLParser.predicate_return predCondition = null;


        Object set26_tree=null;
        Object LPAREN27_tree=null;
        Object RPAREN28_tree=null;

         boolean negated=false; 
        try {
            // /home/obda/SimpleSQL.g:408:5: ( ( ( 'not' | 'NOT' | 'is not' | 'IS NOT' ) )? ( ( ( LPAREN searchCondition RPAREN )=> LPAREN recCondition= searchCondition RPAREN ) | (predCondition= predicate ) ) )
            // /home/obda/SimpleSQL.g:409:5: ( ( 'not' | 'NOT' | 'is not' | 'IS NOT' ) )? ( ( ( LPAREN searchCondition RPAREN )=> LPAREN recCondition= searchCondition RPAREN ) | (predCondition= predicate ) )
            {
            root_0 = (Object)adaptor.nil();

            // /home/obda/SimpleSQL.g:409:5: ( ( 'not' | 'NOT' | 'is not' | 'IS NOT' ) )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( ((LA12_0>=58 && LA12_0<=61)) ) {
                alt12=1;
            }
            switch (alt12) {
                case 1 :
                    // /home/obda/SimpleSQL.g:409:6: ( 'not' | 'NOT' | 'is not' | 'IS NOT' )
                    {
                    set26=(Token)input.LT(1);
                    if ( (input.LA(1)>=58 && input.LA(1)<=61) ) {
                        input.consume();
                        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set26));
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    if ( state.backtracking==0 ) {
                       negated = true; 
                    }

                    }
                    break;

            }

            // /home/obda/SimpleSQL.g:409:63: ( ( ( LPAREN searchCondition RPAREN )=> LPAREN recCondition= searchCondition RPAREN ) | (predCondition= predicate ) )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==LPAREN) ) {
                int LA13_1 = input.LA(2);

                if ( (synpred29_SimpleSQL()) ) {
                    alt13=1;
                }
                else if ( (true) ) {
                    alt13=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA13_0==Number||(LA13_0>=StringLiteral && LA13_0<=Identifier)||(LA13_0>=DateLiteral && LA13_0<=TILDE)||(LA13_0>=85 && LA13_0<=88)) ) {
                alt13=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // /home/obda/SimpleSQL.g:410:11: ( ( LPAREN searchCondition RPAREN )=> LPAREN recCondition= searchCondition RPAREN )
                    {
                    // /home/obda/SimpleSQL.g:410:11: ( ( LPAREN searchCondition RPAREN )=> LPAREN recCondition= searchCondition RPAREN )
                    // /home/obda/SimpleSQL.g:410:12: ( LPAREN searchCondition RPAREN )=> LPAREN recCondition= searchCondition RPAREN
                    {
                    LPAREN27=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_subSearchCondition528); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN27_tree = (Object)adaptor.create(LPAREN27);
                    adaptor.addChild(root_0, LPAREN27_tree);
                    }
                    pushFollow(FOLLOW_searchCondition_in_subSearchCondition532);
                    recCondition=searchCondition();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, recCondition.getTree());
                    RPAREN28=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_subSearchCondition534); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN28_tree = (Object)adaptor.create(RPAREN28);
                    adaptor.addChild(root_0, RPAREN28_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.s = new RecursiveCondition((recCondition!=null?recCondition.c:null)); 
                    }

                    }


                    }
                    break;
                case 2 :
                    // /home/obda/SimpleSQL.g:411:11: (predCondition= predicate )
                    {
                    // /home/obda/SimpleSQL.g:411:11: (predCondition= predicate )
                    // /home/obda/SimpleSQL.g:411:12: predCondition= predicate
                    {
                    pushFollow(FOLLOW_predicate_in_subSearchCondition552);
                    predCondition=predicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, predCondition.getTree());
                    if ( state.backtracking==0 ) {
                       retval.s = new PredicateCondition((predCondition!=null?predCondition.p:null)); 
                    }

                    }


                    }
                    break;

            }

            if ( state.backtracking==0 ) {
               retval.s.setNegated(negated); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "subSearchCondition"

    public static class predicate_return extends ParserRuleReturnScope {
        public IPredicate p;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "predicate"
    // /home/obda/SimpleSQL.g:417:1: predicate returns [IPredicate p] : (test= whereExpression ( comparisonOperator comparedExp= whereExpression | ( 'like' | 'LIKE' ) comparedText= StringLiteral | ( 'in' | 'IN' ) LPAREN ( constantSequence ) RPAREN ) | Identifier ( 'is null' | 'IS NULL' | 'is not null' | 'IS NOT NULL' ) );
    public final SimpleSQLParser.predicate_return predicate() throws RecognitionException {
        SimpleSQLParser.predicate_return retval = new SimpleSQLParser.predicate_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token comparedText=null;
        Token set30=null;
        Token set31=null;
        Token LPAREN32=null;
        Token RPAREN34=null;
        Token Identifier35=null;
        Token set36=null;
        SimpleSQLParser.whereExpression_return test = null;

        SimpleSQLParser.whereExpression_return comparedExp = null;

        SimpleSQLParser.comparisonOperator_return comparisonOperator29 = null;

        SimpleSQLParser.constantSequence_return constantSequence33 = null;


        Object comparedText_tree=null;
        Object set30_tree=null;
        Object set31_tree=null;
        Object LPAREN32_tree=null;
        Object RPAREN34_tree=null;
        Object Identifier35_tree=null;
        Object set36_tree=null;

        try {
            // /home/obda/SimpleSQL.g:418:5: (test= whereExpression ( comparisonOperator comparedExp= whereExpression | ( 'like' | 'LIKE' ) comparedText= StringLiteral | ( 'in' | 'IN' ) LPAREN ( constantSequence ) RPAREN ) | Identifier ( 'is null' | 'IS NULL' | 'is not null' | 'IS NOT NULL' ) )
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==Number||LA15_0==LPAREN||LA15_0==StringLiteral||(LA15_0>=DateLiteral && LA15_0<=TILDE)||(LA15_0>=85 && LA15_0<=88)) ) {
                alt15=1;
            }
            else if ( (LA15_0==Identifier) ) {
                int LA15_2 = input.LA(2);

                if ( (LA15_2==DOT||(LA15_2>=MINUS && LA15_2<=GREATERTHAN)||(LA15_2>=62 && LA15_2<=65)) ) {
                    alt15=1;
                }
                else if ( ((LA15_2>=66 && LA15_2<=69)) ) {
                    alt15=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 2, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;
            }
            switch (alt15) {
                case 1 :
                    // /home/obda/SimpleSQL.g:419:7: test= whereExpression ( comparisonOperator comparedExp= whereExpression | ( 'like' | 'LIKE' ) comparedText= StringLiteral | ( 'in' | 'IN' ) LPAREN ( constantSequence ) RPAREN )
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_whereExpression_in_predicate602);
                    test=whereExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, test.getTree());
                    // /home/obda/SimpleSQL.g:419:28: ( comparisonOperator comparedExp= whereExpression | ( 'like' | 'LIKE' ) comparedText= StringLiteral | ( 'in' | 'IN' ) LPAREN ( constantSequence ) RPAREN )
                    int alt14=3;
                    switch ( input.LA(1) ) {
                    case EQUAL:
                    case NOTEQUAL:
                    case LESSTHANOREQUALTO:
                    case LESSTHAN:
                    case GREATERTHANOREQUALTO:
                    case GREATERTHAN:
                        {
                        alt14=1;
                        }
                        break;
                    case 62:
                    case 63:
                        {
                        alt14=2;
                        }
                        break;
                    case 64:
                    case 65:
                        {
                        alt14=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 14, 0, input);

                        throw nvae;
                    }

                    switch (alt14) {
                        case 1 :
                            // /home/obda/SimpleSQL.g:420:9: comparisonOperator comparedExp= whereExpression
                            {
                            pushFollow(FOLLOW_comparisonOperator_in_predicate614);
                            comparisonOperator29=comparisonOperator();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, comparisonOperator29.getTree());
                            pushFollow(FOLLOW_whereExpression_in_predicate618);
                            comparedExp=whereExpression();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, comparedExp.getTree());
                            if ( state.backtracking==0 ) {
                               retval.p = new Comparison((test!=null?test.e:null), (comparedExp!=null?comparedExp.e:null), (comparisonOperator29!=null?input.toString(comparisonOperator29.start,comparisonOperator29.stop):null)); 
                            }

                            }
                            break;
                        case 2 :
                            // /home/obda/SimpleSQL.g:421:8: ( 'like' | 'LIKE' ) comparedText= StringLiteral
                            {
                            set30=(Token)input.LT(1);
                            if ( (input.LA(1)>=62 && input.LA(1)<=63) ) {
                                input.consume();
                                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set30));
                                state.errorRecovery=false;state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                throw mse;
                            }

                            comparedText=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_predicate638); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            comparedText_tree = (Object)adaptor.create(comparedText);
                            adaptor.addChild(root_0, comparedText_tree);
                            }
                            if ( state.backtracking==0 ) {
                               retval.p = new Like((test!=null?test.e:null), (comparedText!=null?comparedText.getText():null)); 
                            }

                            }
                            break;
                        case 3 :
                            // /home/obda/SimpleSQL.g:422:13: ( 'in' | 'IN' ) LPAREN ( constantSequence ) RPAREN
                            {
                            set31=(Token)input.LT(1);
                            if ( (input.LA(1)>=64 && input.LA(1)<=65) ) {
                                input.consume();
                                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set31));
                                state.errorRecovery=false;state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                throw mse;
                            }

                            LPAREN32=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_predicate660); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            LPAREN32_tree = (Object)adaptor.create(LPAREN32);
                            adaptor.addChild(root_0, LPAREN32_tree);
                            }
                            // /home/obda/SimpleSQL.g:422:32: ( constantSequence )
                            // /home/obda/SimpleSQL.g:422:33: constantSequence
                            {
                            pushFollow(FOLLOW_constantSequence_in_predicate663);
                            constantSequence33=constantSequence();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, constantSequence33.getTree());

                            }

                            RPAREN34=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_predicate666); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            RPAREN34_tree = (Object)adaptor.create(RPAREN34);
                            adaptor.addChild(root_0, RPAREN34_tree);
                            }
                            if ( state.backtracking==0 ) {
                               retval.p = new In((test!=null?test.e:null), (constantSequence33!=null?constantSequence33.l:null));  
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /home/obda/SimpleSQL.g:425:8: Identifier ( 'is null' | 'IS NULL' | 'is not null' | 'IS NOT NULL' )
                    {
                    root_0 = (Object)adaptor.nil();

                    Identifier35=(Token)match(input,Identifier,FOLLOW_Identifier_in_predicate694); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    Identifier35_tree = (Object)adaptor.create(Identifier35);
                    adaptor.addChild(root_0, Identifier35_tree);
                    }
                    set36=(Token)input.LT(1);
                    if ( (input.LA(1)>=66 && input.LA(1)<=69) ) {
                        input.consume();
                        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set36));
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }


                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "predicate"

    public static class constantSequence_return extends ParserRuleReturnScope {
        public List<IConstant> l = new ArrayList<IConstant>(1);;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "constantSequence"
    // /home/obda/SimpleSQL.g:428:1: constantSequence returns [List<IConstant> l = new ArrayList<IConstant>(1);] : first= constant ( COMMA other= constant )* ;
    public final SimpleSQLParser.constantSequence_return constantSequence() throws RecognitionException {
        SimpleSQLParser.constantSequence_return retval = new SimpleSQLParser.constantSequence_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COMMA37=null;
        SimpleSQLParser.constant_return first = null;

        SimpleSQLParser.constant_return other = null;


        Object COMMA37_tree=null;

        try {
            // /home/obda/SimpleSQL.g:429:5: (first= constant ( COMMA other= constant )* )
            // /home/obda/SimpleSQL.g:430:5: first= constant ( COMMA other= constant )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_constant_in_constantSequence734);
            first=constant();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, first.getTree());
            if ( state.backtracking==0 ) {
                retval.l.add((first!=null?first.c:null)); 
            }
            // /home/obda/SimpleSQL.g:431:5: ( COMMA other= constant )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( (LA16_0==COMMA) ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // /home/obda/SimpleSQL.g:431:6: COMMA other= constant
            	    {
            	    COMMA37=(Token)match(input,COMMA,FOLLOW_COMMA_in_constantSequence743); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA37_tree = (Object)adaptor.create(COMMA37);
            	    adaptor.addChild(root_0, COMMA37_tree);
            	    }
            	    pushFollow(FOLLOW_constant_in_constantSequence747);
            	    other=constant();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, other.getTree());
            	    if ( state.backtracking==0 ) {
            	        retval.l.add((other!=null?other.c:null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "constantSequence"

    public static class selectList_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selectList"
    // /home/obda/SimpleSQL.g:434:1: selectList options {k=3; } : item= selectItem ( COMMA nextitem= selectItem )* ;
    public final SimpleSQLParser.selectList_return selectList() throws RecognitionException {
        SimpleSQLParser.selectList_return retval = new SimpleSQLParser.selectList_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token COMMA38=null;
        SimpleSQLParser.selectItem_return item = null;

        SimpleSQLParser.selectItem_return nextitem = null;


        Object COMMA38_tree=null;

        try {
            // /home/obda/SimpleSQL.g:435:5: (item= selectItem ( COMMA nextitem= selectItem )* )
            // /home/obda/SimpleSQL.g:436:5: item= selectItem ( COMMA nextitem= selectItem )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_selectItem_in_selectList787);
            item=selectItem();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, item.getTree());
            if ( state.backtracking==0 ) {
               addSelectItem((item!=null?item.i:null)); 
            }
            // /home/obda/SimpleSQL.g:437:5: ( COMMA nextitem= selectItem )*
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( (LA17_0==COMMA) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // /home/obda/SimpleSQL.g:437:7: COMMA nextitem= selectItem
            	    {
            	    COMMA38=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectList797); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA38_tree = (Object)adaptor.create(COMMA38);
            	    adaptor.addChild(root_0, COMMA38_tree);
            	    }
            	    pushFollow(FOLLOW_selectItem_in_selectList803);
            	    nextitem=selectItem();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, nextitem.getTree());
            	    if ( state.backtracking==0 ) {
            	       addSelectItem((nextitem!=null?nextitem.i:null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop17;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "selectList"

    public static class selectItem_return extends ParserRuleReturnScope {
        public SelectItem i;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selectItem"
    // /home/obda/SimpleSQL.g:440:1: selectItem returns [SelectItem i] : (what= selectExpression 'as' alias= Identifier | alias= Identifier );
    public final SimpleSQLParser.selectItem_return selectItem() throws RecognitionException {
        SimpleSQLParser.selectItem_return retval = new SimpleSQLParser.selectItem_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token alias=null;
        Token string_literal39=null;
        SimpleSQLParser.selectExpression_return what = null;


        Object alias_tree=null;
        Object string_literal39_tree=null;

        try {
            // /home/obda/SimpleSQL.g:441:5: (what= selectExpression 'as' alias= Identifier | alias= Identifier )
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==Number||LA18_0==LPAREN||LA18_0==StringLiteral||(LA18_0>=DateLiteral && LA18_0<=TILDE)||LA18_0==74||(LA18_0>=85 && LA18_0<=88)) ) {
                alt18=1;
            }
            else if ( (LA18_0==Identifier) ) {
                int LA18_2 = input.LA(2);

                if ( (LA18_2==LPAREN||LA18_2==DOT||(LA18_2>=MINUS && LA18_2<=BITWISEXOR)||LA18_2==70) ) {
                    alt18=1;
                }
                else if ( (LA18_2==EOF||LA18_2==COMMA||(LA18_2>=71 && LA18_2<=72)) ) {
                    alt18=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 2, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;
            }
            switch (alt18) {
                case 1 :
                    // /home/obda/SimpleSQL.g:442:5: what= selectExpression 'as' alias= Identifier
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_selectExpression_in_selectItem835);
                    what=selectExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, what.getTree());
                    string_literal39=(Token)match(input,70,FOLLOW_70_in_selectItem838); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    string_literal39_tree = (Object)adaptor.create(string_literal39);
                    adaptor.addChild(root_0, string_literal39_tree);
                    }
                    alias=(Token)match(input,Identifier,FOLLOW_Identifier_in_selectItem843); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    alias_tree = (Object)adaptor.create(alias);
                    adaptor.addChild(root_0, alias_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.i = new SelectItem((what!=null?what.e:null), (alias!=null?alias.getText():null)); 
                    }

                    }
                    break;
                case 2 :
                    // /home/obda/SimpleSQL.g:443:7: alias= Identifier
                    {
                    root_0 = (Object)adaptor.nil();

                    alias=(Token)match(input,Identifier,FOLLOW_Identifier_in_selectItem855); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    alias_tree = (Object)adaptor.create(alias);
                    adaptor.addChild(root_0, alias_tree);
                    }
                    if ( state.backtracking==0 ) {
                      retval.i = new SelectItem(null,(alias!=null?alias.getText():null));
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "selectItem"

    public static class fromClause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fromClause"
    // /home/obda/SimpleSQL.g:446:1: fromClause : ( complexFromClause | simpleFromClause );
    public final SimpleSQLParser.fromClause_return fromClause() throws RecognitionException {
        SimpleSQLParser.fromClause_return retval = new SimpleSQLParser.fromClause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        SimpleSQLParser.complexFromClause_return complexFromClause40 = null;

        SimpleSQLParser.simpleFromClause_return simpleFromClause41 = null;



        try {
            // /home/obda/SimpleSQL.g:447:4: ( complexFromClause | simpleFromClause )
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( ((LA19_0>=71 && LA19_0<=72)) ) {
                int LA19_1 = input.LA(2);

                if ( (LA19_1==Identifier) ) {
                    alt19=2;
                }
                else if ( (LA19_1==LSQUARE) ) {
                    alt19=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 19, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;
            }
            switch (alt19) {
                case 1 :
                    // /home/obda/SimpleSQL.g:448:4: complexFromClause
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_complexFromClause_in_fromClause877);
                    complexFromClause40=complexFromClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, complexFromClause40.getTree());

                    }
                    break;
                case 2 :
                    // /home/obda/SimpleSQL.g:449:6: simpleFromClause
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_simpleFromClause_in_fromClause885);
                    simpleFromClause41=simpleFromClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simpleFromClause41.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "fromClause"

    public static class complexFromClause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "complexFromClause"
    // /home/obda/SimpleSQL.g:453:1: complexFromClause : ( 'from' | 'FROM' ) fromTable= complexTableSource ( COMMA fromTable= complexTableSource )* ;
    public final SimpleSQLParser.complexFromClause_return complexFromClause() throws RecognitionException {
        SimpleSQLParser.complexFromClause_return retval = new SimpleSQLParser.complexFromClause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set42=null;
        Token COMMA43=null;
        SimpleSQLParser.complexTableSource_return fromTable = null;


        Object set42_tree=null;
        Object COMMA43_tree=null;

        try {
            // /home/obda/SimpleSQL.g:454:5: ( ( 'from' | 'FROM' ) fromTable= complexTableSource ( COMMA fromTable= complexTableSource )* )
            // /home/obda/SimpleSQL.g:455:5: ( 'from' | 'FROM' ) fromTable= complexTableSource ( COMMA fromTable= complexTableSource )*
            {
            root_0 = (Object)adaptor.nil();

            set42=(Token)input.LT(1);
            if ( (input.LA(1)>=71 && input.LA(1)<=72) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set42));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            pushFollow(FOLLOW_complexTableSource_in_complexFromClause915);
            fromTable=complexTableSource();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, fromTable.getTree());
            if ( state.backtracking==0 ) {
               addTableSource((fromTable!=null?fromTable.t:null)); 
            }
            // /home/obda/SimpleSQL.g:456:5: ( COMMA fromTable= complexTableSource )*
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( (LA20_0==COMMA) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // /home/obda/SimpleSQL.g:456:6: COMMA fromTable= complexTableSource
            	    {
            	    COMMA43=(Token)match(input,COMMA,FOLLOW_COMMA_in_complexFromClause926); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA43_tree = (Object)adaptor.create(COMMA43);
            	    adaptor.addChild(root_0, COMMA43_tree);
            	    }
            	    pushFollow(FOLLOW_complexTableSource_in_complexFromClause930);
            	    fromTable=complexTableSource();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, fromTable.getTree());
            	    if ( state.backtracking==0 ) {
            	       addTableSource((fromTable!=null?fromTable.t:null));  
            	    }

            	    }
            	    break;

            	default :
            	    break loop20;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "complexFromClause"

    public static class simpleFromClause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "simpleFromClause"
    // /home/obda/SimpleSQL.g:459:1: simpleFromClause : ( 'from' | 'FROM' ) fromTable= simpleTableSource ( COMMA NextfromTable= simpleTableSource )* ;
    public final SimpleSQLParser.simpleFromClause_return simpleFromClause() throws RecognitionException {
        SimpleSQLParser.simpleFromClause_return retval = new SimpleSQLParser.simpleFromClause_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set44=null;
        Token COMMA45=null;
        SimpleSQLParser.simpleTableSource_return fromTable = null;

        SimpleSQLParser.simpleTableSource_return NextfromTable = null;


        Object set44_tree=null;
        Object COMMA45_tree=null;

        try {
            // /home/obda/SimpleSQL.g:460:3: ( ( 'from' | 'FROM' ) fromTable= simpleTableSource ( COMMA NextfromTable= simpleTableSource )* )
            // /home/obda/SimpleSQL.g:461:4: ( 'from' | 'FROM' ) fromTable= simpleTableSource ( COMMA NextfromTable= simpleTableSource )*
            {
            root_0 = (Object)adaptor.nil();

            set44=(Token)input.LT(1);
            if ( (input.LA(1)>=71 && input.LA(1)<=72) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set44));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            pushFollow(FOLLOW_simpleTableSource_in_simpleFromClause968);
            fromTable=simpleTableSource();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, fromTable.getTree());
            if ( state.backtracking==0 ) {
               addTableSource((fromTable!=null?fromTable.t:null)); 
            }
            // /home/obda/SimpleSQL.g:462:5: ( COMMA NextfromTable= simpleTableSource )*
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( (LA21_0==COMMA) ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // /home/obda/SimpleSQL.g:462:6: COMMA NextfromTable= simpleTableSource
            	    {
            	    COMMA45=(Token)match(input,COMMA,FOLLOW_COMMA_in_simpleFromClause979); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA45_tree = (Object)adaptor.create(COMMA45);
            	    adaptor.addChild(root_0, COMMA45_tree);
            	    }
            	    pushFollow(FOLLOW_simpleTableSource_in_simpleFromClause983);
            	    NextfromTable=simpleTableSource();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, NextfromTable.getTree());
            	    if ( state.backtracking==0 ) {
            	       addTableSource((NextfromTable!=null?NextfromTable.t:null));  
            	    }

            	    }
            	    break;

            	default :
            	    break loop21;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "simpleFromClause"

    public static class complexTableSource_return extends ParserRuleReturnScope {
        public TableSource t =  new TableSource();;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "complexTableSource"
    // /home/obda/SimpleSQL.g:465:1: complexTableSource returns [ TableSource t = new TableSource(); ] : LSQUARE column= Identifier ( COMMA column= Identifier )* RSQUARE ( 'as' | 'AS' ) tableName= Identifier ;
    public final SimpleSQLParser.complexTableSource_return complexTableSource() throws RecognitionException {
        SimpleSQLParser.complexTableSource_return retval = new SimpleSQLParser.complexTableSource_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token column=null;
        Token tableName=null;
        Token LSQUARE46=null;
        Token COMMA47=null;
        Token RSQUARE48=null;
        Token set49=null;

        Object column_tree=null;
        Object tableName_tree=null;
        Object LSQUARE46_tree=null;
        Object COMMA47_tree=null;
        Object RSQUARE48_tree=null;
        Object set49_tree=null;

        try {
            // /home/obda/SimpleSQL.g:466:5: ( LSQUARE column= Identifier ( COMMA column= Identifier )* RSQUARE ( 'as' | 'AS' ) tableName= Identifier )
            // /home/obda/SimpleSQL.g:467:5: LSQUARE column= Identifier ( COMMA column= Identifier )* RSQUARE ( 'as' | 'AS' ) tableName= Identifier
            {
            root_0 = (Object)adaptor.nil();

            LSQUARE46=(Token)match(input,LSQUARE,FOLLOW_LSQUARE_in_complexTableSource1023); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LSQUARE46_tree = (Object)adaptor.create(LSQUARE46);
            adaptor.addChild(root_0, LSQUARE46_tree);
            }
            column=(Token)match(input,Identifier,FOLLOW_Identifier_in_complexTableSource1027); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            column_tree = (Object)adaptor.create(column);
            adaptor.addChild(root_0, column_tree);
            }
            if ( state.backtracking==0 ) {
               retval.t.addColumn((column!=null?column.getText():null)); 
            }
            // /home/obda/SimpleSQL.g:468:5: ( COMMA column= Identifier )*
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( (LA22_0==COMMA) ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // /home/obda/SimpleSQL.g:468:6: COMMA column= Identifier
            	    {
            	    COMMA47=(Token)match(input,COMMA,FOLLOW_COMMA_in_complexTableSource1038); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA47_tree = (Object)adaptor.create(COMMA47);
            	    adaptor.addChild(root_0, COMMA47_tree);
            	    }
            	    column=(Token)match(input,Identifier,FOLLOW_Identifier_in_complexTableSource1042); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    column_tree = (Object)adaptor.create(column);
            	    adaptor.addChild(root_0, column_tree);
            	    }
            	    if ( state.backtracking==0 ) {
            	       retval.t.addColumn((column!=null?column.getText():null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop22;
                }
            } while (true);

            RSQUARE48=(Token)match(input,RSQUARE,FOLLOW_RSQUARE_in_complexTableSource1054); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RSQUARE48_tree = (Object)adaptor.create(RSQUARE48);
            adaptor.addChild(root_0, RSQUARE48_tree);
            }
            set49=(Token)input.LT(1);
            if ( input.LA(1)==70||input.LA(1)==73 ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set49));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            tableName=(Token)match(input,Identifier,FOLLOW_Identifier_in_complexTableSource1064); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            tableName_tree = (Object)adaptor.create(tableName);
            adaptor.addChild(root_0, tableName_tree);
            }
            if ( state.backtracking==0 ) {
               retval.t.setTable((tableName!=null?tableName.getText():null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "complexTableSource"

    public static class simpleTableSource_return extends ParserRuleReturnScope {
        public TableSource t =  new TableSource();;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "simpleTableSource"
    // /home/obda/SimpleSQL.g:472:1: simpleTableSource returns [ TableSource t = new TableSource(); ] : tablename= Identifier ;
    public final SimpleSQLParser.simpleTableSource_return simpleTableSource() throws RecognitionException {
        SimpleSQLParser.simpleTableSource_return retval = new SimpleSQLParser.simpleTableSource_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token tablename=null;

        Object tablename_tree=null;

        try {
            // /home/obda/SimpleSQL.g:473:5: (tablename= Identifier )
            // /home/obda/SimpleSQL.g:474:5: tablename= Identifier
            {
            root_0 = (Object)adaptor.nil();

            tablename=(Token)match(input,Identifier,FOLLOW_Identifier_in_simpleTableSource1099); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            tablename_tree = (Object)adaptor.create(tablename);
            adaptor.addChild(root_0, tablename_tree);
            }
            if ( state.backtracking==0 ) {
               retval.t.setTable((tablename!=null?tablename.getText():null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "simpleTableSource"

    public static class tableColumn_return extends ParserRuleReturnScope {
        public TableColumn t;;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tableColumn"
    // /home/obda/SimpleSQL.g:478:1: tableColumn returns [ TableColumn t; ] : table= Identifier DOT column= Identifier ;
    public final SimpleSQLParser.tableColumn_return tableColumn() throws RecognitionException {
        SimpleSQLParser.tableColumn_return retval = new SimpleSQLParser.tableColumn_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token table=null;
        Token column=null;
        Token DOT50=null;

        Object table_tree=null;
        Object column_tree=null;
        Object DOT50_tree=null;

        try {
            // /home/obda/SimpleSQL.g:479:5: (table= Identifier DOT column= Identifier )
            // /home/obda/SimpleSQL.g:480:5: table= Identifier DOT column= Identifier
            {
            root_0 = (Object)adaptor.nil();

            table=(Token)match(input,Identifier,FOLLOW_Identifier_in_tableColumn1136); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            table_tree = (Object)adaptor.create(table);
            adaptor.addChild(root_0, table_tree);
            }
            DOT50=(Token)match(input,DOT,FOLLOW_DOT_in_tableColumn1138); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            DOT50_tree = (Object)adaptor.create(DOT50);
            adaptor.addChild(root_0, DOT50_tree);
            }
            column=(Token)match(input,Identifier,FOLLOW_Identifier_in_tableColumn1142); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            column_tree = (Object)adaptor.create(column);
            adaptor.addChild(root_0, column_tree);
            }
            if ( state.backtracking==0 ) {
               retval.t = new TableColumn((table!=null?table.getText():null), (column!=null?column.getText():null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "tableColumn"

    public static class selectExpression_return extends ParserRuleReturnScope {
        public IExpression e;;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selectExpression"
    // /home/obda/SimpleSQL.g:484:1: selectExpression returns [IExpression e; ] : subA= selectSubExpression (operator= binaryOperator subB= selectSubExpression )* ;
    public final SimpleSQLParser.selectExpression_return selectExpression() throws RecognitionException {
        SimpleSQLParser.selectExpression_return retval = new SimpleSQLParser.selectExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        SimpleSQLParser.selectSubExpression_return subA = null;

        SimpleSQLParser.binaryOperator_return operator = null;

        SimpleSQLParser.selectSubExpression_return subB = null;



        try {
            // /home/obda/SimpleSQL.g:485:5: (subA= selectSubExpression (operator= binaryOperator subB= selectSubExpression )* )
            // /home/obda/SimpleSQL.g:486:7: subA= selectSubExpression (operator= binaryOperator subB= selectSubExpression )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_selectSubExpression_in_selectExpression1175);
            subA=selectSubExpression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, subA.getTree());
            if ( state.backtracking==0 ) {
               retval.e = new SimpleExpression((subA!=null?subA.s:null)); 
            }
            // /home/obda/SimpleSQL.g:487:7: (operator= binaryOperator subB= selectSubExpression )*
            loop23:
            do {
                int alt23=2;
                int LA23_0 = input.LA(1);

                if ( ((LA23_0>=MINUS && LA23_0<=BITWISEXOR)) ) {
                    alt23=1;
                }


                switch (alt23) {
            	case 1 :
            	    // /home/obda/SimpleSQL.g:487:8: operator= binaryOperator subB= selectSubExpression
            	    {
            	    pushFollow(FOLLOW_binaryOperator_in_selectExpression1188);
            	    operator=binaryOperator();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, operator.getTree());
            	    pushFollow(FOLLOW_selectSubExpression_in_selectExpression1192);
            	    subB=selectSubExpression();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, subB.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.e = new ComplexExpression(retval.e, new SimpleExpression((subB!=null?subB.s:null)), (operator!=null?input.toString(operator.start,operator.stop):null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop23;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "selectExpression"

    public static class selectSubExpression_return extends ParserRuleReturnScope {
        public SubExpression s;;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selectSubExpression"
    // /home/obda/SimpleSQL.g:490:1: selectSubExpression returns [SubExpression s; ] : ( unaryOperator )? ( constant | function | LPAREN selectExpression RPAREN | tableColumn | caseFunction ) ;
    public final SimpleSQLParser.selectSubExpression_return selectSubExpression() throws RecognitionException {
        SimpleSQLParser.selectSubExpression_return retval = new SimpleSQLParser.selectSubExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN54=null;
        Token RPAREN56=null;
        SimpleSQLParser.unaryOperator_return unaryOperator51 = null;

        SimpleSQLParser.constant_return constant52 = null;

        SimpleSQLParser.function_return function53 = null;

        SimpleSQLParser.selectExpression_return selectExpression55 = null;

        SimpleSQLParser.tableColumn_return tableColumn57 = null;

        SimpleSQLParser.caseFunction_return caseFunction58 = null;


        Object LPAREN54_tree=null;
        Object RPAREN56_tree=null;

         String unop= null; 
        try {
            // /home/obda/SimpleSQL.g:491:6: ( ( unaryOperator )? ( constant | function | LPAREN selectExpression RPAREN | tableColumn | caseFunction ) )
            // /home/obda/SimpleSQL.g:492:5: ( unaryOperator )? ( constant | function | LPAREN selectExpression RPAREN | tableColumn | caseFunction )
            {
            root_0 = (Object)adaptor.nil();

            // /home/obda/SimpleSQL.g:492:5: ( unaryOperator )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( ((LA24_0>=MINUS && LA24_0<=TILDE)) ) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // /home/obda/SimpleSQL.g:492:6: unaryOperator
                    {
                    pushFollow(FOLLOW_unaryOperator_in_selectSubExpression1234);
                    unaryOperator51=unaryOperator();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, unaryOperator51.getTree());
                    if ( state.backtracking==0 ) {
                       unop=(unaryOperator51!=null?input.toString(unaryOperator51.start,unaryOperator51.stop):null);  
                    }

                    }
                    break;

            }

            // /home/obda/SimpleSQL.g:493:5: ( constant | function | LPAREN selectExpression RPAREN | tableColumn | caseFunction )
            int alt25=5;
            switch ( input.LA(1) ) {
            case Number:
            case StringLiteral:
            case DateLiteral:
            case 85:
            case 86:
            case 87:
            case 88:
                {
                alt25=1;
                }
                break;
            case Identifier:
                {
                switch ( input.LA(2) ) {
                case DOT:
                    {
                    alt25=4;
                    }
                    break;
                case EOF:
                case COMMA:
                case RPAREN:
                case MINUS:
                case TILDE:
                case PLUS:
                case STAR:
                case DIVIDE:
                case MOD:
                case AMPERSAND:
                case BITWISEOR:
                case BITWISEXOR:
                case 70:
                case 75:
                case 77:
                case 78:
                    {
                    alt25=1;
                    }
                    break;
                case LPAREN:
                    {
                    alt25=2;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 25, 2, input);

                    throw nvae;
                }

                }
                break;
            case LPAREN:
                {
                alt25=3;
                }
                break;
            case 74:
                {
                alt25=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 25, 0, input);

                throw nvae;
            }

            switch (alt25) {
                case 1 :
                    // /home/obda/SimpleSQL.g:494:7: constant
                    {
                    pushFollow(FOLLOW_constant_in_selectSubExpression1252);
                    constant52=constant();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, constant52.getTree());
                    if ( state.backtracking==0 ) {
                       retval.s = new ConstantSubExpression((constant52!=null?constant52.c:null)); 
                    }

                    }
                    break;
                case 2 :
                    // /home/obda/SimpleSQL.g:495:7: function
                    {
                    pushFollow(FOLLOW_function_in_selectSubExpression1262);
                    function53=function();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, function53.getTree());
                    if ( state.backtracking==0 ) {
                       retval.s = new FunctionSubExpression((function53!=null?function53.f:null)); 
                    }

                    }
                    break;
                case 3 :
                    // /home/obda/SimpleSQL.g:496:7: LPAREN selectExpression RPAREN
                    {
                    LPAREN54=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_selectSubExpression1272); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN54_tree = (Object)adaptor.create(LPAREN54);
                    adaptor.addChild(root_0, LPAREN54_tree);
                    }
                    pushFollow(FOLLOW_selectExpression_in_selectSubExpression1274);
                    selectExpression55=selectExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, selectExpression55.getTree());
                    RPAREN56=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_selectSubExpression1276); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN56_tree = (Object)adaptor.create(RPAREN56);
                    adaptor.addChild(root_0, RPAREN56_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.s = new ExpressionSubExpression((selectExpression55!=null?selectExpression55.e:null)); 
                    }

                    }
                    break;
                case 4 :
                    // /home/obda/SimpleSQL.g:497:7: tableColumn
                    {
                    pushFollow(FOLLOW_tableColumn_in_selectSubExpression1286);
                    tableColumn57=tableColumn();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, tableColumn57.getTree());
                    if ( state.backtracking==0 ) {
                       retval.s = new TableColumnSubExpression((tableColumn57!=null?tableColumn57.t:null)); 
                    }

                    }
                    break;
                case 5 :
                    // /home/obda/SimpleSQL.g:498:7: caseFunction
                    {
                    pushFollow(FOLLOW_caseFunction_in_selectSubExpression1297);
                    caseFunction58=caseFunction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, caseFunction58.getTree());
                    if ( state.backtracking==0 ) {
                       retval.s = new CaseSubExpression((caseFunction58!=null?caseFunction58.c:null)); 
                    }

                    }
                    break;

            }

            if ( state.backtracking==0 ) {
              retval.s.setUnaryOperator(unop); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "selectSubExpression"

    public static class whereExpression_return extends ParserRuleReturnScope {
        public IExpression e;;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "whereExpression"
    // /home/obda/SimpleSQL.g:504:1: whereExpression returns [IExpression e; ] : subA= whereSubExpression (operator= binaryOperator subB= whereSubExpression )* ;
    public final SimpleSQLParser.whereExpression_return whereExpression() throws RecognitionException {
        SimpleSQLParser.whereExpression_return retval = new SimpleSQLParser.whereExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        SimpleSQLParser.whereSubExpression_return subA = null;

        SimpleSQLParser.binaryOperator_return operator = null;

        SimpleSQLParser.whereSubExpression_return subB = null;



        try {
            // /home/obda/SimpleSQL.g:505:5: (subA= whereSubExpression (operator= binaryOperator subB= whereSubExpression )* )
            // /home/obda/SimpleSQL.g:506:7: subA= whereSubExpression (operator= binaryOperator subB= whereSubExpression )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_whereSubExpression_in_whereExpression1350);
            subA=whereSubExpression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, subA.getTree());
            if ( state.backtracking==0 ) {
               retval.e = new SimpleExpression((subA!=null?subA.s:null)); 
            }
            // /home/obda/SimpleSQL.g:507:7: (operator= binaryOperator subB= whereSubExpression )*
            loop26:
            do {
                int alt26=2;
                int LA26_0 = input.LA(1);

                if ( ((LA26_0>=MINUS && LA26_0<=BITWISEXOR)) ) {
                    alt26=1;
                }


                switch (alt26) {
            	case 1 :
            	    // /home/obda/SimpleSQL.g:507:8: operator= binaryOperator subB= whereSubExpression
            	    {
            	    pushFollow(FOLLOW_binaryOperator_in_whereExpression1363);
            	    operator=binaryOperator();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, operator.getTree());
            	    pushFollow(FOLLOW_whereSubExpression_in_whereExpression1367);
            	    subB=whereSubExpression();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, subB.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.e = new ComplexExpression(retval.e, new SimpleExpression((subB!=null?subB.s:null)), (operator!=null?input.toString(operator.start,operator.stop):null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop26;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "whereExpression"

    public static class whereSubExpression_return extends ParserRuleReturnScope {
        public SubExpression s;;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "whereSubExpression"
    // /home/obda/SimpleSQL.g:510:1: whereSubExpression returns [SubExpression s; ] : ( unaryOperator )? ( constant | LPAREN whereExpression RPAREN | tableColumn ) ;
    public final SimpleSQLParser.whereSubExpression_return whereSubExpression() throws RecognitionException {
        SimpleSQLParser.whereSubExpression_return retval = new SimpleSQLParser.whereSubExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token LPAREN61=null;
        Token RPAREN63=null;
        SimpleSQLParser.unaryOperator_return unaryOperator59 = null;

        SimpleSQLParser.constant_return constant60 = null;

        SimpleSQLParser.whereExpression_return whereExpression62 = null;

        SimpleSQLParser.tableColumn_return tableColumn64 = null;


        Object LPAREN61_tree=null;
        Object RPAREN63_tree=null;

         String unop= null; 
        try {
            // /home/obda/SimpleSQL.g:511:6: ( ( unaryOperator )? ( constant | LPAREN whereExpression RPAREN | tableColumn ) )
            // /home/obda/SimpleSQL.g:512:5: ( unaryOperator )? ( constant | LPAREN whereExpression RPAREN | tableColumn )
            {
            root_0 = (Object)adaptor.nil();

            // /home/obda/SimpleSQL.g:512:5: ( unaryOperator )?
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( ((LA27_0>=MINUS && LA27_0<=TILDE)) ) {
                alt27=1;
            }
            switch (alt27) {
                case 1 :
                    // /home/obda/SimpleSQL.g:512:6: unaryOperator
                    {
                    pushFollow(FOLLOW_unaryOperator_in_whereSubExpression1409);
                    unaryOperator59=unaryOperator();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, unaryOperator59.getTree());
                    if ( state.backtracking==0 ) {
                       unop=(unaryOperator59!=null?input.toString(unaryOperator59.start,unaryOperator59.stop):null);  
                    }

                    }
                    break;

            }

            // /home/obda/SimpleSQL.g:513:5: ( constant | LPAREN whereExpression RPAREN | tableColumn )
            int alt28=3;
            switch ( input.LA(1) ) {
            case Number:
            case StringLiteral:
            case DateLiteral:
            case 85:
            case 86:
            case 87:
            case 88:
                {
                alt28=1;
                }
                break;
            case Identifier:
                {
                int LA28_2 = input.LA(2);

                if ( (LA28_2==DOT) ) {
                    alt28=3;
                }
                else if ( (LA28_2==EOF||LA28_2==RPAREN||(LA28_2>=MINUS && LA28_2<=GREATERTHAN)||(LA28_2>=43 && LA28_2<=45)||LA28_2==47||(LA28_2>=53 && LA28_2<=57)||(LA28_2>=62 && LA28_2<=65)) ) {
                    alt28=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 28, 2, input);

                    throw nvae;
                }
                }
                break;
            case LPAREN:
                {
                alt28=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 28, 0, input);

                throw nvae;
            }

            switch (alt28) {
                case 1 :
                    // /home/obda/SimpleSQL.g:514:7: constant
                    {
                    pushFollow(FOLLOW_constant_in_whereSubExpression1427);
                    constant60=constant();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, constant60.getTree());
                    if ( state.backtracking==0 ) {
                       retval.s = new ConstantSubExpression((constant60!=null?constant60.c:null)); 
                    }

                    }
                    break;
                case 2 :
                    // /home/obda/SimpleSQL.g:515:7: LPAREN whereExpression RPAREN
                    {
                    LPAREN61=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_whereSubExpression1437); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN61_tree = (Object)adaptor.create(LPAREN61);
                    adaptor.addChild(root_0, LPAREN61_tree);
                    }
                    pushFollow(FOLLOW_whereExpression_in_whereSubExpression1439);
                    whereExpression62=whereExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, whereExpression62.getTree());
                    RPAREN63=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_whereSubExpression1441); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN63_tree = (Object)adaptor.create(RPAREN63);
                    adaptor.addChild(root_0, RPAREN63_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.s = new ExpressionSubExpression((whereExpression62!=null?whereExpression62.e:null)); 
                    }

                    }
                    break;
                case 3 :
                    // /home/obda/SimpleSQL.g:516:7: tableColumn
                    {
                    pushFollow(FOLLOW_tableColumn_in_whereSubExpression1451);
                    tableColumn64=tableColumn();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, tableColumn64.getTree());
                    if ( state.backtracking==0 ) {
                       retval.s = new TableColumnSubExpression((tableColumn64!=null?tableColumn64.t:null)); 
                    }

                    }
                    break;

            }

            if ( state.backtracking==0 ) {
              retval.s.setUnaryOperator(unop); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "whereSubExpression"

    public static class function_return extends ParserRuleReturnScope {
        public Function f = new Function();;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "function"
    // /home/obda/SimpleSQL.g:523:1: function returns [Function f = new Function(); ] : functionId= Identifier LPAREN (parameter= selectExpression ( COMMA parameter= selectExpression )* )? RPAREN ;
    public final SimpleSQLParser.function_return function() throws RecognitionException {
        SimpleSQLParser.function_return retval = new SimpleSQLParser.function_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token functionId=null;
        Token LPAREN65=null;
        Token COMMA66=null;
        Token RPAREN67=null;
        SimpleSQLParser.selectExpression_return parameter = null;


        Object functionId_tree=null;
        Object LPAREN65_tree=null;
        Object COMMA66_tree=null;
        Object RPAREN67_tree=null;

        try {
            // /home/obda/SimpleSQL.g:524:5: (functionId= Identifier LPAREN (parameter= selectExpression ( COMMA parameter= selectExpression )* )? RPAREN )
            // /home/obda/SimpleSQL.g:525:5: functionId= Identifier LPAREN (parameter= selectExpression ( COMMA parameter= selectExpression )* )? RPAREN
            {
            root_0 = (Object)adaptor.nil();

            functionId=(Token)match(input,Identifier,FOLLOW_Identifier_in_function1500); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            functionId_tree = (Object)adaptor.create(functionId);
            adaptor.addChild(root_0, functionId_tree);
            }
            if ( state.backtracking==0 ) {
               retval.f.setFunctionId((functionId!=null?functionId.getText():null)); 
            }
            LPAREN65=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_function1508); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN65_tree = (Object)adaptor.create(LPAREN65);
            adaptor.addChild(root_0, LPAREN65_tree);
            }
            // /home/obda/SimpleSQL.g:526:12: (parameter= selectExpression ( COMMA parameter= selectExpression )* )?
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==Number||LA30_0==LPAREN||(LA30_0>=StringLiteral && LA30_0<=Identifier)||(LA30_0>=DateLiteral && LA30_0<=TILDE)||LA30_0==74||(LA30_0>=85 && LA30_0<=88)) ) {
                alt30=1;
            }
            switch (alt30) {
                case 1 :
                    // /home/obda/SimpleSQL.g:527:11: parameter= selectExpression ( COMMA parameter= selectExpression )*
                    {
                    pushFollow(FOLLOW_selectExpression_in_function1524);
                    parameter=selectExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, parameter.getTree());
                    if ( state.backtracking==0 ) {
                       retval.f.addParameter((parameter!=null?parameter.e:null)); 
                    }
                    // /home/obda/SimpleSQL.g:528:11: ( COMMA parameter= selectExpression )*
                    loop29:
                    do {
                        int alt29=2;
                        int LA29_0 = input.LA(1);

                        if ( (LA29_0==COMMA) ) {
                            alt29=1;
                        }


                        switch (alt29) {
                    	case 1 :
                    	    // /home/obda/SimpleSQL.g:528:12: COMMA parameter= selectExpression
                    	    {
                    	    COMMA66=(Token)match(input,COMMA,FOLLOW_COMMA_in_function1540); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA66_tree = (Object)adaptor.create(COMMA66);
                    	    adaptor.addChild(root_0, COMMA66_tree);
                    	    }
                    	    pushFollow(FOLLOW_selectExpression_in_function1544);
                    	    parameter=selectExpression();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, parameter.getTree());
                    	    if ( state.backtracking==0 ) {
                    	       retval.f.addParameter((parameter!=null?parameter.e:null)); 
                    	    }

                    	    }
                    	    break;

                    	default :
                    	    break loop29;
                        }
                    } while (true);


                    }
                    break;

            }

            RPAREN67=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_function1565); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN67_tree = (Object)adaptor.create(RPAREN67);
            adaptor.addChild(root_0, RPAREN67_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "function"

    public static class constant_return extends ParserRuleReturnScope {
        public IConstant c;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "constant"
    // /home/obda/SimpleSQL.g:535:1: constant returns [IConstant c] : ( Number | StringLiteral | DateLiteral | booleanValue | Identifier );
    public final SimpleSQLParser.constant_return constant() throws RecognitionException {
        SimpleSQLParser.constant_return retval = new SimpleSQLParser.constant_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token Number68=null;
        Token StringLiteral69=null;
        Token DateLiteral70=null;
        Token Identifier72=null;
        SimpleSQLParser.booleanValue_return booleanValue71 = null;


        Object Number68_tree=null;
        Object StringLiteral69_tree=null;
        Object DateLiteral70_tree=null;
        Object Identifier72_tree=null;

        try {
            // /home/obda/SimpleSQL.g:536:5: ( Number | StringLiteral | DateLiteral | booleanValue | Identifier )
            int alt31=5;
            switch ( input.LA(1) ) {
            case Number:
                {
                alt31=1;
                }
                break;
            case StringLiteral:
                {
                alt31=2;
                }
                break;
            case DateLiteral:
                {
                alt31=3;
                }
                break;
            case 85:
            case 86:
            case 87:
            case 88:
                {
                alt31=4;
                }
                break;
            case Identifier:
                {
                alt31=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 31, 0, input);

                throw nvae;
            }

            switch (alt31) {
                case 1 :
                    // /home/obda/SimpleSQL.g:537:5: Number
                    {
                    root_0 = (Object)adaptor.nil();

                    Number68=(Token)match(input,Number,FOLLOW_Number_in_constant1593); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    Number68_tree = (Object)adaptor.create(Number68);
                    adaptor.addChild(root_0, Number68_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.c = new NumberConstant((Number68!=null?Number68.getText():null)); 
                    }

                    }
                    break;
                case 2 :
                    // /home/obda/SimpleSQL.g:538:7: StringLiteral
                    {
                    root_0 = (Object)adaptor.nil();

                    StringLiteral69=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_constant1603); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    StringLiteral69_tree = (Object)adaptor.create(StringLiteral69);
                    adaptor.addChild(root_0, StringLiteral69_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.c = new StringConstant((StringLiteral69!=null?StringLiteral69.getText():null)); 
                    }

                    }
                    break;
                case 3 :
                    // /home/obda/SimpleSQL.g:539:7: DateLiteral
                    {
                    root_0 = (Object)adaptor.nil();

                    DateLiteral70=(Token)match(input,DateLiteral,FOLLOW_DateLiteral_in_constant1613); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DateLiteral70_tree = (Object)adaptor.create(DateLiteral70);
                    adaptor.addChild(root_0, DateLiteral70_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.c = new DateConstant((DateLiteral70!=null?DateLiteral70.getText():null)); 
                    }

                    }
                    break;
                case 4 :
                    // /home/obda/SimpleSQL.g:540:7: booleanValue
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_booleanValue_in_constant1623);
                    booleanValue71=booleanValue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, booleanValue71.getTree());
                    if ( state.backtracking==0 ) {
                       retval.c = new BooleanConstant((booleanValue71!=null?input.toString(booleanValue71.start,booleanValue71.stop):null)); 
                    }

                    }
                    break;
                case 5 :
                    // /home/obda/SimpleSQL.g:541:7: Identifier
                    {
                    root_0 = (Object)adaptor.nil();

                    Identifier72=(Token)match(input,Identifier,FOLLOW_Identifier_in_constant1633); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    Identifier72_tree = (Object)adaptor.create(Identifier72);
                    adaptor.addChild(root_0, Identifier72_tree);
                    }
                    if ( state.backtracking==0 ) {
                      retval.c = new StringConstant((Identifier72!=null?Identifier72.getText():null));
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "constant"

    public static class caseWhen_return extends ParserRuleReturnScope {
        public When w;;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "caseWhen"
    // /home/obda/SimpleSQL.g:544:1: caseWhen returns [When w; ] : tableColumn comparisonOperator constant ;
    public final SimpleSQLParser.caseWhen_return caseWhen() throws RecognitionException {
        SimpleSQLParser.caseWhen_return retval = new SimpleSQLParser.caseWhen_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        SimpleSQLParser.tableColumn_return tableColumn73 = null;

        SimpleSQLParser.comparisonOperator_return comparisonOperator74 = null;

        SimpleSQLParser.constant_return constant75 = null;



        try {
            // /home/obda/SimpleSQL.g:545:5: ( tableColumn comparisonOperator constant )
            // /home/obda/SimpleSQL.g:546:5: tableColumn comparisonOperator constant
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_tableColumn_in_caseWhen1661);
            tableColumn73=tableColumn();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, tableColumn73.getTree());
            pushFollow(FOLLOW_comparisonOperator_in_caseWhen1663);
            comparisonOperator74=comparisonOperator();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, comparisonOperator74.getTree());
            pushFollow(FOLLOW_constant_in_caseWhen1665);
            constant75=constant();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, constant75.getTree());
            if ( state.backtracking==0 ) {
               retval.w = new When((tableColumn73!=null?tableColumn73.t:null), (constant75!=null?constant75.c:null), (comparisonOperator74!=null?input.toString(comparisonOperator74.start,comparisonOperator74.stop):null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "caseWhen"

    public static class caseFunction_return extends ParserRuleReturnScope {
        public CaseFunction c = new CaseFunction();;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "caseFunction"
    // /home/obda/SimpleSQL.g:552:1: caseFunction returns [ CaseFunction c = new CaseFunction(); ] : 'case' tableColumn ( 'when' caseWhen 'then' then= selectExpression )+ ( 'else' elseExp= selectExpression )? 'end' ;
    public final SimpleSQLParser.caseFunction_return caseFunction() throws RecognitionException {
        SimpleSQLParser.caseFunction_return retval = new SimpleSQLParser.caseFunction_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal76=null;
        Token string_literal78=null;
        Token string_literal80=null;
        Token string_literal81=null;
        Token string_literal82=null;
        SimpleSQLParser.selectExpression_return then = null;

        SimpleSQLParser.selectExpression_return elseExp = null;

        SimpleSQLParser.tableColumn_return tableColumn77 = null;

        SimpleSQLParser.caseWhen_return caseWhen79 = null;


        Object string_literal76_tree=null;
        Object string_literal78_tree=null;
        Object string_literal80_tree=null;
        Object string_literal81_tree=null;
        Object string_literal82_tree=null;

        try {
            // /home/obda/SimpleSQL.g:553:5: ( 'case' tableColumn ( 'when' caseWhen 'then' then= selectExpression )+ ( 'else' elseExp= selectExpression )? 'end' )
            // /home/obda/SimpleSQL.g:553:7: 'case' tableColumn ( 'when' caseWhen 'then' then= selectExpression )+ ( 'else' elseExp= selectExpression )? 'end'
            {
            root_0 = (Object)adaptor.nil();

            string_literal76=(Token)match(input,74,FOLLOW_74_in_caseFunction1687); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            string_literal76_tree = (Object)adaptor.create(string_literal76);
            adaptor.addChild(root_0, string_literal76_tree);
            }
            pushFollow(FOLLOW_tableColumn_in_caseFunction1689);
            tableColumn77=tableColumn();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, tableColumn77.getTree());
            if ( state.backtracking==0 ) {
               retval.c.setTest((tableColumn77!=null?tableColumn77.t:null)); 
            }
            // /home/obda/SimpleSQL.g:554:11: ( 'when' caseWhen 'then' then= selectExpression )+
            int cnt32=0;
            loop32:
            do {
                int alt32=2;
                int LA32_0 = input.LA(1);

                if ( (LA32_0==75) ) {
                    alt32=1;
                }


                switch (alt32) {
            	case 1 :
            	    // /home/obda/SimpleSQL.g:554:12: 'when' caseWhen 'then' then= selectExpression
            	    {
            	    string_literal78=(Token)match(input,75,FOLLOW_75_in_caseFunction1704); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    string_literal78_tree = (Object)adaptor.create(string_literal78);
            	    adaptor.addChild(root_0, string_literal78_tree);
            	    }
            	    pushFollow(FOLLOW_caseWhen_in_caseFunction1706);
            	    caseWhen79=caseWhen();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, caseWhen79.getTree());
            	    string_literal80=(Token)match(input,76,FOLLOW_76_in_caseFunction1708); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    string_literal80_tree = (Object)adaptor.create(string_literal80);
            	    adaptor.addChild(root_0, string_literal80_tree);
            	    }
            	    pushFollow(FOLLOW_selectExpression_in_caseFunction1712);
            	    then=selectExpression();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, then.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.c.addWhenThen(new WhenThen((caseWhen79!=null?caseWhen79.w:null), (then!=null?then.e:null))); 
            	    }

            	    }
            	    break;

            	default :
            	    if ( cnt32 >= 1 ) break loop32;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(32, input);
                        throw eee;
                }
                cnt32++;
            } while (true);

            // /home/obda/SimpleSQL.g:556:5: ( 'else' elseExp= selectExpression )?
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==77) ) {
                alt33=1;
            }
            switch (alt33) {
                case 1 :
                    // /home/obda/SimpleSQL.g:556:6: 'else' elseExp= selectExpression
                    {
                    string_literal81=(Token)match(input,77,FOLLOW_77_in_caseFunction1744); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    string_literal81_tree = (Object)adaptor.create(string_literal81);
                    adaptor.addChild(root_0, string_literal81_tree);
                    }
                    pushFollow(FOLLOW_selectExpression_in_caseFunction1748);
                    elseExp=selectExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, elseExp.getTree());
                    if ( state.backtracking==0 ) {
                      retval.c.setElse((elseExp!=null?elseExp.e:null)); 
                    }

                    }
                    break;

            }

            string_literal82=(Token)match(input,78,FOLLOW_78_in_caseFunction1759); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            string_literal82_tree = (Object)adaptor.create(string_literal82);
            adaptor.addChild(root_0, string_literal82_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "caseFunction"

    public static class unaryOperator_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "unaryOperator"
    // /home/obda/SimpleSQL.g:561:1: unaryOperator : ( MINUS | TILDE );
    public final SimpleSQLParser.unaryOperator_return unaryOperator() throws RecognitionException {
        SimpleSQLParser.unaryOperator_return retval = new SimpleSQLParser.unaryOperator_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set83=null;

        Object set83_tree=null;

        try {
            // /home/obda/SimpleSQL.g:562:5: ( MINUS | TILDE )
            // /home/obda/SimpleSQL.g:
            {
            root_0 = (Object)adaptor.nil();

            set83=(Token)input.LT(1);
            if ( (input.LA(1)>=MINUS && input.LA(1)<=TILDE) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set83));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "unaryOperator"

    public static class binaryOperator_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "binaryOperator"
    // /home/obda/SimpleSQL.g:566:1: binaryOperator : ( arithmeticOperator | bitwiseOperator );
    public final SimpleSQLParser.binaryOperator_return binaryOperator() throws RecognitionException {
        SimpleSQLParser.binaryOperator_return retval = new SimpleSQLParser.binaryOperator_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        SimpleSQLParser.arithmeticOperator_return arithmeticOperator84 = null;

        SimpleSQLParser.bitwiseOperator_return bitwiseOperator85 = null;



        try {
            // /home/obda/SimpleSQL.g:567:5: ( arithmeticOperator | bitwiseOperator )
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==MINUS||(LA34_0>=PLUS && LA34_0<=MOD)) ) {
                alt34=1;
            }
            else if ( (LA34_0==TILDE||(LA34_0>=AMPERSAND && LA34_0<=BITWISEXOR)) ) {
                alt34=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 34, 0, input);

                throw nvae;
            }
            switch (alt34) {
                case 1 :
                    // /home/obda/SimpleSQL.g:568:5: arithmeticOperator
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_arithmeticOperator_in_binaryOperator1812);
                    arithmeticOperator84=arithmeticOperator();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmeticOperator84.getTree());

                    }
                    break;
                case 2 :
                    // /home/obda/SimpleSQL.g:568:26: bitwiseOperator
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_bitwiseOperator_in_binaryOperator1816);
                    bitwiseOperator85=bitwiseOperator();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, bitwiseOperator85.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "binaryOperator"

    public static class arithmeticOperator_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "arithmeticOperator"
    // /home/obda/SimpleSQL.g:571:1: arithmeticOperator : ( PLUS | MINUS | STAR | DIVIDE | MOD );
    public final SimpleSQLParser.arithmeticOperator_return arithmeticOperator() throws RecognitionException {
        SimpleSQLParser.arithmeticOperator_return retval = new SimpleSQLParser.arithmeticOperator_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set86=null;

        Object set86_tree=null;

        try {
            // /home/obda/SimpleSQL.g:572:5: ( PLUS | MINUS | STAR | DIVIDE | MOD )
            // /home/obda/SimpleSQL.g:
            {
            root_0 = (Object)adaptor.nil();

            set86=(Token)input.LT(1);
            if ( input.LA(1)==MINUS||(input.LA(1)>=PLUS && input.LA(1)<=MOD) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set86));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "arithmeticOperator"

    public static class bitwiseOperator_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "bitwiseOperator"
    // /home/obda/SimpleSQL.g:576:1: bitwiseOperator : ( AMPERSAND | TILDE | BITWISEOR | BITWISEXOR );
    public final SimpleSQLParser.bitwiseOperator_return bitwiseOperator() throws RecognitionException {
        SimpleSQLParser.bitwiseOperator_return retval = new SimpleSQLParser.bitwiseOperator_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set87=null;

        Object set87_tree=null;

        try {
            // /home/obda/SimpleSQL.g:577:5: ( AMPERSAND | TILDE | BITWISEOR | BITWISEXOR )
            // /home/obda/SimpleSQL.g:
            {
            root_0 = (Object)adaptor.nil();

            set87=(Token)input.LT(1);
            if ( input.LA(1)==TILDE||(input.LA(1)>=AMPERSAND && input.LA(1)<=BITWISEXOR) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set87));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "bitwiseOperator"

    public static class comparisonOperator_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "comparisonOperator"
    // /home/obda/SimpleSQL.g:581:1: comparisonOperator : ( EQUAL | NOTEQUAL | LESSTHANOREQUALTO | LESSTHAN | GREATERTHANOREQUALTO | GREATERTHAN );
    public final SimpleSQLParser.comparisonOperator_return comparisonOperator() throws RecognitionException {
        SimpleSQLParser.comparisonOperator_return retval = new SimpleSQLParser.comparisonOperator_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set88=null;

        Object set88_tree=null;

        try {
            // /home/obda/SimpleSQL.g:582:5: ( EQUAL | NOTEQUAL | LESSTHANOREQUALTO | LESSTHAN | GREATERTHANOREQUALTO | GREATERTHAN )
            // /home/obda/SimpleSQL.g:
            {
            root_0 = (Object)adaptor.nil();

            set88=(Token)input.LT(1);
            if ( (input.LA(1)>=EQUAL && input.LA(1)<=GREATERTHAN) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set88));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "comparisonOperator"

    public static class logicalOperator_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "logicalOperator"
    // /home/obda/SimpleSQL.g:587:1: logicalOperator : ( 'all' | 'and' | 'any' | 'exists' | 'in' | 'like' | 'not' | 'or' | 'some' | 'is not' | 'ALL' | 'AND' | 'ANY' | 'EXISTS' | 'IN' | 'LIKE' | 'NOT' | 'OR' | 'SOME' | 'IS NOT' );
    public final SimpleSQLParser.logicalOperator_return logicalOperator() throws RecognitionException {
        SimpleSQLParser.logicalOperator_return retval = new SimpleSQLParser.logicalOperator_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set89=null;

        Object set89_tree=null;

        try {
            // /home/obda/SimpleSQL.g:588:5: ( 'all' | 'and' | 'any' | 'exists' | 'in' | 'like' | 'not' | 'or' | 'some' | 'is not' | 'ALL' | 'AND' | 'ANY' | 'EXISTS' | 'IN' | 'LIKE' | 'NOT' | 'OR' | 'SOME' | 'IS NOT' )
            // /home/obda/SimpleSQL.g:
            {
            root_0 = (Object)adaptor.nil();

            set89=(Token)input.LT(1);
            if ( input.LA(1)==37||input.LA(1)==39||(input.LA(1)>=53 && input.LA(1)<=56)||(input.LA(1)>=58 && input.LA(1)<=65)||(input.LA(1)>=79 && input.LA(1)<=84) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set89));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "logicalOperator"

    public static class booleanValue_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "booleanValue"
    // /home/obda/SimpleSQL.g:593:1: booleanValue : ( 'true' | 'false' | 'TRUE' | 'FALSE' );
    public final SimpleSQLParser.booleanValue_return booleanValue() throws RecognitionException {
        SimpleSQLParser.booleanValue_return retval = new SimpleSQLParser.booleanValue_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set90=null;

        Object set90_tree=null;

        try {
            // /home/obda/SimpleSQL.g:594:5: ( 'true' | 'false' | 'TRUE' | 'FALSE' )
            // /home/obda/SimpleSQL.g:
            {
            root_0 = (Object)adaptor.nil();

            set90=(Token)input.LT(1);
            if ( (input.LA(1)>=85 && input.LA(1)<=88) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set90));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "booleanValue"

    // $ANTLR start synpred2_SimpleSQL
    public final void synpred2_SimpleSQL_fragment() throws RecognitionException {   
        // /home/obda/SimpleSQL.g:360:6: ( groupByClause )
        // /home/obda/SimpleSQL.g:360:6: groupByClause
        {
        pushFollow(FOLLOW_groupByClause_in_synpred2_SimpleSQL104);
        groupByClause();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_SimpleSQL

    // $ANTLR start synpred29_SimpleSQL
    public final void synpred29_SimpleSQL_fragment() throws RecognitionException {   
        SimpleSQLParser.searchCondition_return recCondition = null;


        // /home/obda/SimpleSQL.g:410:11: ( ( ( LPAREN searchCondition RPAREN )=> LPAREN recCondition= searchCondition RPAREN ) )
        // /home/obda/SimpleSQL.g:410:11: ( ( LPAREN searchCondition RPAREN )=> LPAREN recCondition= searchCondition RPAREN )
        {
        // /home/obda/SimpleSQL.g:410:11: ( ( LPAREN searchCondition RPAREN )=> LPAREN recCondition= searchCondition RPAREN )
        // /home/obda/SimpleSQL.g:410:12: ( LPAREN searchCondition RPAREN )=> LPAREN recCondition= searchCondition RPAREN
        {
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred29_SimpleSQL528); if (state.failed) return ;
        pushFollow(FOLLOW_searchCondition_in_synpred29_SimpleSQL532);
        recCondition=searchCondition();

        state._fsp--;
        if (state.failed) return ;
        match(input,RPAREN,FOLLOW_RPAREN_in_synpred29_SimpleSQL534); if (state.failed) return ;

        }


        }
    }
    // $ANTLR end synpred29_SimpleSQL

    // Delegated rules

    public final boolean synpred29_SimpleSQL() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred29_SimpleSQL_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred2_SimpleSQL() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred2_SimpleSQL_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


 

    public static final BitSet FOLLOW_statement_in_parse49 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectStatement_in_statement62 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_statement64 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectClause_in_selectStatement82 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000180L});
    public static final BitSet FOLLOW_fromClause_in_selectStatement88 = new BitSet(new long[]{0x0200A60000000002L});
    public static final BitSet FOLLOW_whereClause_in_selectStatement95 = new BitSet(new long[]{0x0200A00000000002L});
    public static final BitSet FOLLOW_groupByClause_in_selectStatement104 = new BitSet(new long[]{0x0000A00000000002L});
    public static final BitSet FOLLOW_orderByClause_in_selectStatement113 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_selectClause141 = new BitSet(new long[]{0x000001E00000E350L,0x0000000001E00400L});
    public static final BitSet FOLLOW_set_in_selectClause151 = new BitSet(new long[]{0x000001E00000E350L,0x0000000001E00400L});
    public static final BitSet FOLLOW_selectList_in_selectClause175 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_whereClause197 = new BitSet(new long[]{0x3C0000000000E350L,0x0000000001E00000L});
    public static final BitSet FOLLOW_searchCondition_in_whereClause204 = new BitSet(new long[]{0x0000180000000002L});
    public static final BitSet FOLLOW_set_in_whereClause213 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Number_in_whereClause219 = new BitSet(new long[]{0x0000180000000002L});
    public static final BitSet FOLLOW_45_in_orderByClause245 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_46_in_orderByClause247 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_47_in_orderByClause250 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_48_in_orderByClause252 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_orderByExpression_in_orderByClause257 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_COMMA_in_orderByClause266 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_orderByExpression_in_orderByClause270 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_tableColumn_in_orderByExpression307 = new BitSet(new long[]{0x001E000000000002L});
    public static final BitSet FOLLOW_set_in_orderByExpression318 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_subSearchCondition_in_searchCondition364 = new BitSet(new long[]{0x01E0000000000002L});
    public static final BitSet FOLLOW_set_in_searchCondition376 = new BitSet(new long[]{0x3C0000000000E350L,0x0000000001E00000L});
    public static final BitSet FOLLOW_subSearchCondition_in_searchCondition393 = new BitSet(new long[]{0x01E0000000000002L});
    public static final BitSet FOLLOW_57_in_groupByClause426 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_46_in_groupByClause428 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_47_in_groupByClause431 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_48_in_groupByClause433 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_tableColumn_in_groupByClause438 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_COMMA_in_groupByClause447 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_tableColumn_in_groupByClause449 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_set_in_subSearchCondition487 = new BitSet(new long[]{0x3C0000000000E350L,0x0000000001E00000L});
    public static final BitSet FOLLOW_LPAREN_in_subSearchCondition528 = new BitSet(new long[]{0x3C0000000000E350L,0x0000000001E00000L});
    public static final BitSet FOLLOW_searchCondition_in_subSearchCondition532 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_subSearchCondition534 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_predicate_in_subSearchCondition552 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_whereExpression_in_predicate602 = new BitSet(new long[]{0xC00000001F800000L,0x0000000000000003L});
    public static final BitSet FOLLOW_comparisonOperator_in_predicate614 = new BitSet(new long[]{0x000000000000E350L,0x0000000001E00000L});
    public static final BitSet FOLLOW_whereExpression_in_predicate618 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_predicate629 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_StringLiteral_in_predicate638 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_predicate654 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_predicate660 = new BitSet(new long[]{0x0000000000002310L,0x0000000001E00000L});
    public static final BitSet FOLLOW_constantSequence_in_predicate663 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_predicate666 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_predicate694 = new BitSet(new long[]{0x0000000000000000L,0x000000000000003CL});
    public static final BitSet FOLLOW_set_in_predicate696 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constant_in_constantSequence734 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_COMMA_in_constantSequence743 = new BitSet(new long[]{0x0000000000002310L,0x0000000001E00000L});
    public static final BitSet FOLLOW_constant_in_constantSequence747 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_selectItem_in_selectList787 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_COMMA_in_selectList797 = new BitSet(new long[]{0x000001E00000E350L,0x0000000001E00400L});
    public static final BitSet FOLLOW_selectItem_in_selectList803 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_selectExpression_in_selectItem835 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_70_in_selectItem838 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_Identifier_in_selectItem843 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_selectItem855 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_complexFromClause_in_fromClause877 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simpleFromClause_in_fromClause885 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_complexFromClause907 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_complexTableSource_in_complexFromClause915 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_COMMA_in_complexFromClause926 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_complexTableSource_in_complexFromClause930 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_set_in_simpleFromClause959 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_simpleTableSource_in_simpleFromClause968 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_COMMA_in_simpleFromClause979 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_simpleTableSource_in_simpleFromClause983 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_LSQUARE_in_complexTableSource1023 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_Identifier_in_complexTableSource1027 = new BitSet(new long[]{0x0000000000000820L});
    public static final BitSet FOLLOW_COMMA_in_complexTableSource1038 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_Identifier_in_complexTableSource1042 = new BitSet(new long[]{0x0000000000000820L});
    public static final BitSet FOLLOW_RSQUARE_in_complexTableSource1054 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000240L});
    public static final BitSet FOLLOW_set_in_complexTableSource1056 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_Identifier_in_complexTableSource1064 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_simpleTableSource1099 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_tableColumn1136 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_DOT_in_tableColumn1138 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_Identifier_in_tableColumn1142 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectSubExpression_in_selectExpression1175 = new BitSet(new long[]{0x00000000007FC002L});
    public static final BitSet FOLLOW_binaryOperator_in_selectExpression1188 = new BitSet(new long[]{0x000000000000E350L,0x0000000001E00400L});
    public static final BitSet FOLLOW_selectSubExpression_in_selectExpression1192 = new BitSet(new long[]{0x00000000007FC002L});
    public static final BitSet FOLLOW_unaryOperator_in_selectSubExpression1234 = new BitSet(new long[]{0x000000000000E350L,0x0000000001E00400L});
    public static final BitSet FOLLOW_constant_in_selectSubExpression1252 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_in_selectSubExpression1262 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_selectSubExpression1272 = new BitSet(new long[]{0x000000000000E350L,0x0000000001E00400L});
    public static final BitSet FOLLOW_selectExpression_in_selectSubExpression1274 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_selectSubExpression1276 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tableColumn_in_selectSubExpression1286 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_caseFunction_in_selectSubExpression1297 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_whereSubExpression_in_whereExpression1350 = new BitSet(new long[]{0x00000000007FC002L});
    public static final BitSet FOLLOW_binaryOperator_in_whereExpression1363 = new BitSet(new long[]{0x000000000000E350L,0x0000000001E00000L});
    public static final BitSet FOLLOW_whereSubExpression_in_whereExpression1367 = new BitSet(new long[]{0x00000000007FC002L});
    public static final BitSet FOLLOW_unaryOperator_in_whereSubExpression1409 = new BitSet(new long[]{0x0000000000002350L,0x0000000001E00000L});
    public static final BitSet FOLLOW_constant_in_whereSubExpression1427 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_whereSubExpression1437 = new BitSet(new long[]{0x000000000000E350L,0x0000000001E00000L});
    public static final BitSet FOLLOW_whereExpression_in_whereSubExpression1439 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_whereSubExpression1441 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tableColumn_in_whereSubExpression1451 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_function1500 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_function1508 = new BitSet(new long[]{0x000000000000E3D0L,0x0000000001E00400L});
    public static final BitSet FOLLOW_selectExpression_in_function1524 = new BitSet(new long[]{0x00000000000000A0L});
    public static final BitSet FOLLOW_COMMA_in_function1540 = new BitSet(new long[]{0x000000000000E350L,0x0000000001E00400L});
    public static final BitSet FOLLOW_selectExpression_in_function1544 = new BitSet(new long[]{0x00000000000000A0L});
    public static final BitSet FOLLOW_RPAREN_in_function1565 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Number_in_constant1593 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_StringLiteral_in_constant1603 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DateLiteral_in_constant1613 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanValue_in_constant1623 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_constant1633 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tableColumn_in_caseWhen1661 = new BitSet(new long[]{0x000000001F800000L});
    public static final BitSet FOLLOW_comparisonOperator_in_caseWhen1663 = new BitSet(new long[]{0x0000000000002310L,0x0000000001E00000L});
    public static final BitSet FOLLOW_constant_in_caseWhen1665 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_74_in_caseFunction1687 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_tableColumn_in_caseFunction1689 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_caseFunction1704 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_caseWhen_in_caseFunction1706 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_76_in_caseFunction1708 = new BitSet(new long[]{0x000000000000E350L,0x0000000001E00400L});
    public static final BitSet FOLLOW_selectExpression_in_caseFunction1712 = new BitSet(new long[]{0x0000000000000000L,0x0000000000006800L});
    public static final BitSet FOLLOW_77_in_caseFunction1744 = new BitSet(new long[]{0x000000000000E350L,0x0000000001E00400L});
    public static final BitSet FOLLOW_selectExpression_in_caseFunction1748 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_78_in_caseFunction1759 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arithmeticOperator_in_binaryOperator1812 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bitwiseOperator_in_binaryOperator1816 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_arithmeticOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_bitwiseOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_comparisonOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_logicalOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_booleanValue0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_groupByClause_in_synpred2_SimpleSQL104 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_synpred29_SimpleSQL528 = new BitSet(new long[]{0x3C0000000000E350L,0x0000000001E00000L});
    public static final BitSet FOLLOW_searchCondition_in_synpred29_SimpleSQL532 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_synpred29_SimpleSQL534 = new BitSet(new long[]{0x0000000000000002L});

}