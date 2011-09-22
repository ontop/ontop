// $ANTLR 3.4 C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g 2011-09-20 11:44:07

package it.unibz.krdb.obda.parser;

import java.util.Stack;
import java.util.Queue;
import java.util.LinkedList;

import java.lang.Number;

import it.unibz.krdb.sql.DBMetadata;

import it.unibz.krdb.sql.api.IValueExpression;
import it.unibz.krdb.sql.api.IPredicate;

import it.unibz.krdb.sql.api.QueryTree;
import it.unibz.krdb.sql.api.Projection;
import it.unibz.krdb.sql.api.Selection;
import it.unibz.krdb.sql.api.Aggregation;

import it.unibz.krdb.sql.api.Attribute;
import it.unibz.krdb.sql.api.JoinOperator;
import it.unibz.krdb.sql.api.SetUnion;
import it.unibz.krdb.sql.api.Relation;
import it.unibz.krdb.sql.api.RelationalAlgebra;

import it.unibz.krdb.sql.api.TableExpression;
import it.unibz.krdb.sql.api.AbstractValueExpression;
import it.unibz.krdb.sql.api.NumericValueExpression;
import it.unibz.krdb.sql.api.StringValueExpression;
import it.unibz.krdb.sql.api.ReferenceValueExpression;
import it.unibz.krdb.sql.api.CollectionValueExpression;
import it.unibz.krdb.sql.api.BooleanValueExpression;

import it.unibz.krdb.sql.api.TablePrimary;
import it.unibz.krdb.sql.api.DerivedColumn;
import it.unibz.krdb.sql.api.GroupingElement;
import it.unibz.krdb.sql.api.ComparisonPredicate;
import it.unibz.krdb.sql.api.AndOperator;
import it.unibz.krdb.sql.api.OrOperator;
import it.unibz.krdb.sql.api.ColumnReference;

import it.unibz.krdb.sql.api.Literal;
import it.unibz.krdb.sql.api.StringLiteral;
import it.unibz.krdb.sql.api.BooleanLiteral;
import it.unibz.krdb.sql.api.NumericLiteral;
import it.unibz.krdb.sql.api.IntegerLiteral;
import it.unibz.krdb.sql.api.DecimalLiteral;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class SQL99Parser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ALL", "ALPHA", "ALPHANUM", "AMPERSAND", "AND", "ANY", "APOSTROPHE", "AS", "ASTERISK", "AT", "AVG", "BACKSLASH", "BY", "CARET", "CHAR", "COLON", "COMMA", "CONCATENATION", "COUNT", "DECIMAL", "DECIMAL_NEGATIVE", "DECIMAL_POSITIVE", "DIGIT", "DISTINCT", "DOLLAR", "DOUBLE_SLASH", "ECHAR", "EQUALS", "EVERY", "EXCLAMATION", "FALSE", "FROM", "FULL", "GREATER", "GROUP", "HASH", "IN", "INNER", "INTEGER", "INTEGER_NEGATIVE", "INTEGER_POSITIVE", "IS", "JOIN", "LEFT", "LESS", "LPAREN", "LSQ_BRACKET", "MAX", "MIN", "MINUS", "NOT", "NULL", "ON", "OR", "ORDER", "OUTER", "PERCENT", "PERIOD", "PLUS", "QUESTION", "QUOTE_DOUBLE", "QUOTE_SINGLE", "RIGHT", "RPAREN", "RSQ_BRACKET", "SELECT", "SEMI", "SOLIDUS", "SOME", "STRING_WITH_QUOTE", "STRING_WITH_QUOTE_DOUBLE", "SUM", "TILDE", "TRUE", "UNDERSCORE", "UNION", "USING", "VARNAME", "WHERE", "WS"
    };

    public static final int EOF=-1;
    public static final int ALL=4;
    public static final int ALPHA=5;
    public static final int ALPHANUM=6;
    public static final int AMPERSAND=7;
    public static final int AND=8;
    public static final int ANY=9;
    public static final int APOSTROPHE=10;
    public static final int AS=11;
    public static final int ASTERISK=12;
    public static final int AT=13;
    public static final int AVG=14;
    public static final int BACKSLASH=15;
    public static final int BY=16;
    public static final int CARET=17;
    public static final int CHAR=18;
    public static final int COLON=19;
    public static final int COMMA=20;
    public static final int CONCATENATION=21;
    public static final int COUNT=22;
    public static final int DECIMAL=23;
    public static final int DECIMAL_NEGATIVE=24;
    public static final int DECIMAL_POSITIVE=25;
    public static final int DIGIT=26;
    public static final int DISTINCT=27;
    public static final int DOLLAR=28;
    public static final int DOUBLE_SLASH=29;
    public static final int ECHAR=30;
    public static final int EQUALS=31;
    public static final int EVERY=32;
    public static final int EXCLAMATION=33;
    public static final int FALSE=34;
    public static final int FROM=35;
    public static final int FULL=36;
    public static final int GREATER=37;
    public static final int GROUP=38;
    public static final int HASH=39;
    public static final int IN=40;
    public static final int INNER=41;
    public static final int INTEGER=42;
    public static final int INTEGER_NEGATIVE=43;
    public static final int INTEGER_POSITIVE=44;
    public static final int IS=45;
    public static final int JOIN=46;
    public static final int LEFT=47;
    public static final int LESS=48;
    public static final int LPAREN=49;
    public static final int LSQ_BRACKET=50;
    public static final int MAX=51;
    public static final int MIN=52;
    public static final int MINUS=53;
    public static final int NOT=54;
    public static final int NULL=55;
    public static final int ON=56;
    public static final int OR=57;
    public static final int ORDER=58;
    public static final int OUTER=59;
    public static final int PERCENT=60;
    public static final int PERIOD=61;
    public static final int PLUS=62;
    public static final int QUESTION=63;
    public static final int QUOTE_DOUBLE=64;
    public static final int QUOTE_SINGLE=65;
    public static final int RIGHT=66;
    public static final int RPAREN=67;
    public static final int RSQ_BRACKET=68;
    public static final int SELECT=69;
    public static final int SEMI=70;
    public static final int SOLIDUS=71;
    public static final int SOME=72;
    public static final int STRING_WITH_QUOTE=73;
    public static final int STRING_WITH_QUOTE_DOUBLE=74;
    public static final int SUM=75;
    public static final int TILDE=76;
    public static final int TRUE=77;
    public static final int UNDERSCORE=78;
    public static final int UNION=79;
    public static final int USING=80;
    public static final int VARNAME=81;
    public static final int WHERE=82;
    public static final int WS=83;

    // delegates
    public Parser[] getDelegates() {
        return new Parser[] {};
    }

    // delegators


    public SQL99Parser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }
    public SQL99Parser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

    public String[] getTokenNames() { return SQL99Parser.tokenNames; }
    public String getGrammarFileName() { return "C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g"; }


    /** Global stack for keeping the projection column list */
    private Stack<Projection> projectionStack = new Stack<Projection>();

    /** Global stack for keeping the select all projection */
    private Stack<Boolean> AsteriskStack = new Stack<Boolean>();

    /** Global stack for keeping the relations */
    private Stack<RelationalAlgebra> relationStack = new Stack<RelationalAlgebra>();

    /** Temporary cache for keeping the numeric value expression */
    private NumericValueExpression numericExp;

    /** Temporary cache for keeping the string value expression */
    private StringValueExpression stringExp;

    /** Temporary cache for keeping the reference value expression */
    private ReferenceValueExpression referenceExp;

    /** Temporary cache for keeping the collection value expression */
    private CollectionValueExpression collectionExp;

    /** Temporary cache for keeping the boolean value expression */
    private BooleanValueExpression booleanExp;

    /** The root of the query tree */
    private QueryTree queryTree;

    /** The metadata of the datasource (i.e., database) */
    private DBMetadata metadata;

    /** Asterisk select all flag */
    private boolean bSelectAll = false;


    public SQL99Parser(TokenStream input, DBMetadata metadata) {
      this(input);
      this.metadata = metadata;
    }

    /**
     * Retrieves the query tree object. The tree represents
     * the data structure of the SQL statement.
     *
     * @return Returns a query tree.
     */
    public QueryTree getQueryTree() {
      return queryTree;
    }

    /**
     * A helper method to construct the projection. A projection
     * object holds the information about the table columns in
     * the SELECT keyword.
     */
    private Projection createProjection(ArrayList<TablePrimary> tableList, ArrayList<DerivedColumn> columnList) {

      Projection prj = new Projection();
      
      if (bSelectAll) { // If Asterisk is identified
        if (columnList == null) {
          columnList = new ArrayList<DerivedColumn>();
        }
        for (TablePrimary tableObj : tableList) {
          String schema = tableObj.getSchema();
          String table = tableObj.getName();
          ArrayList<Attribute> attributeList = tableObj.getAttributes();
          for (Attribute attr : attributeList) {
            String column = attr.name;
            ReferenceValueExpression referenceExp = new ReferenceValueExpression();
            referenceExp.add(schema, table, column);
            columnList.add(new DerivedColumn(referenceExp));
          }
        }
      }
      prj.addAll(columnList);
      return prj;
    }

    /**
     * A helper method to construct the selection. A selection object
     * holds the information about the comparison predicate (e.g., A = B)
     * in the WHERE statment.
     */
    private Selection createSelection(BooleanValueExpression booleanExp) {
      if (booleanExp == null) {
        return null;
      }
      Selection slc = new Selection();
      
      try {
    	  Queue<Object> specification = booleanExp.getSpecification();
    	  slc.copy(specification);
    	}
      catch(Exception e) {
        // Does nothing.
      }
      return slc;
    }

    /**
     * A helper method to constuct the aggregation. An aggregation object
     * holds the information about the table attributes that are used
     * to group the data records. They appear in the GROUP BY statement.
     */
    private Aggregation createAggregation(ArrayList<GroupingElement> groupingList) {
      if (groupingList == null) {
        return null;
      }
      Aggregation agg = new Aggregation();
      agg.addAll(groupingList);
      return agg;
    }

    /**
     * Another helper method to construct the query tree. This method
     * constructs the sub-tree taken the information from a query 
     * specification.
     *
     * @param relation
     *           The root of this sub-tree.
     * @return Returns the query sub-tree.
     */
    private QueryTree constructQueryTree(RelationalAlgebra relation) {

      QueryTree parent = new QueryTree(relation);
      
      int flag = 1;
      while (!relationStack.isEmpty()) {
        relation = relationStack.pop();
        QueryTree node = new QueryTree(relation);
            
        if ((flag % 2) == 1) {  // right child
          parent.attachRight(node);
        }
        else {  // left child
          parent.attachLeft(node);
          parent = node;
        }
        flag++;
      }
      return parent.root();
    }



    // $ANTLR start "parse"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:205:1: parse returns [QueryTree value] : query EOF ;
    public final QueryTree parse() throws RecognitionException {
        QueryTree value = null;


        QueryTree query1 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:206:3: ( query EOF )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:206:5: query EOF
            {
            pushFollow(FOLLOW_query_in_parse40);
            query1=query();

            state._fsp--;


            match(input,EOF,FOLLOW_EOF_in_parse42); 


                  value = query1;
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "parse"



    // $ANTLR start "query"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:211:1: query returns [QueryTree value] : a= query_specification ( UNION ( set_quantifier )? b= query_specification )* ;
    public final QueryTree query() throws RecognitionException {
        QueryTree value = null;


        QueryTree a =null;

        QueryTree b =null;

        int set_quantifier2 =0;



        int quantifier = 0;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:215:3: (a= query_specification ( UNION ( set_quantifier )? b= query_specification )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:215:5: a= query_specification ( UNION ( set_quantifier )? b= query_specification )*
            {
            pushFollow(FOLLOW_query_specification_in_query70);
            a=query_specification();

            state._fsp--;


             
                  queryTree = a; 
                  value = queryTree;
                

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:219:5: ( UNION ( set_quantifier )? b= query_specification )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==UNION) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:219:6: UNION ( set_quantifier )? b= query_specification
            	    {
            	    match(input,UNION,FOLLOW_UNION_in_query79); 

            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:219:12: ( set_quantifier )?
            	    int alt1=2;
            	    int LA1_0 = input.LA(1);

            	    if ( (LA1_0==ALL||LA1_0==DISTINCT) ) {
            	        alt1=1;
            	    }
            	    switch (alt1) {
            	        case 1 :
            	            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:219:12: set_quantifier
            	            {
            	            pushFollow(FOLLOW_set_quantifier_in_query81);
            	            set_quantifier2=set_quantifier();

            	            state._fsp--;


            	            }
            	            break;

            	    }


            	    pushFollow(FOLLOW_query_specification_in_query86);
            	    b=query_specification();

            	    state._fsp--;



            	          quantifier = set_quantifier2;
            	          SetUnion union = new SetUnion(quantifier);
            	              
            	          QueryTree parent = new QueryTree(union);      
            	          parent.attachLeft(queryTree);
            	          parent.attachRight(b);
            	           
            	          queryTree = parent.root();
            	          value = queryTree;
            	        

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "query"



    // $ANTLR start "query_specification"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:232:1: query_specification returns [QueryTree value] : SELECT ( set_quantifier )? select_list table_expression ;
    public final QueryTree query_specification() throws RecognitionException {
        QueryTree value = null;


        TableExpression table_expression3 =null;

        ArrayList<DerivedColumn> select_list4 =null;

        int set_quantifier5 =0;



        int quantifier = 0;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:236:3: ( SELECT ( set_quantifier )? select_list table_expression )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:236:5: SELECT ( set_quantifier )? select_list table_expression
            {
            match(input,SELECT,FOLLOW_SELECT_in_query_specification114); 

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:236:12: ( set_quantifier )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==ALL||LA3_0==DISTINCT) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:236:12: set_quantifier
                    {
                    pushFollow(FOLLOW_set_quantifier_in_query_specification116);
                    set_quantifier5=set_quantifier();

                    state._fsp--;


                    }
                    break;

            }


            pushFollow(FOLLOW_select_list_in_query_specification119);
            select_list4=select_list();

            state._fsp--;


            pushFollow(FOLLOW_table_expression_in_query_specification121);
            table_expression3=table_expression();

            state._fsp--;



              
                  TableExpression te = table_expression3;
                  
                  // Construct the projection
                  ArrayList<TablePrimary> tableList = te.getFromClause();
                  ArrayList<DerivedColumn> columnList = select_list4;
                  Projection prj = createProjection(tableList, columnList);
                  
                  quantifier = set_quantifier5;
                  prj.setType(quantifier);
                  
                  // Construct the selection
                  BooleanValueExpression booleanExp = te.getWhereClause();
                  Selection slc = createSelection(booleanExp);
                  
                  // Construct the aggregation
                  ArrayList<GroupingElement> groupingList = te.getGroupByClause();
                  Aggregation agg = createAggregation(groupingList);
                  
                  // Construct the query tree
                  RelationalAlgebra root = relationStack.pop();
                  root.setProjection(prj);
                  if (slc != null) {
                    root.setSelection(slc);
                  }
                  if (agg != null) {
                    root.setAggregation(agg);
                  }
                  value = constructQueryTree(root);   
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "query_specification"



    // $ANTLR start "set_quantifier"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:275:1: set_quantifier returns [int value] : ( ALL | DISTINCT );
    public final int set_quantifier() throws RecognitionException {
        int value = 0;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:276:3: ( ALL | DISTINCT )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==ALL) ) {
                alt4=1;
            }
            else if ( (LA4_0==DISTINCT) ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;

            }
            switch (alt4) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:276:5: ALL
                    {
                    match(input,ALL,FOLLOW_ALL_in_set_quantifier142); 

                     value = 1; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:277:5: DISTINCT
                    {
                    match(input,DISTINCT,FOLLOW_DISTINCT_in_set_quantifier150); 

                     value = 2; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "set_quantifier"



    // $ANTLR start "select_list"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:280:1: select_list returns [ArrayList<DerivedColumn> value] : ( ASTERISK |a= select_sublist ( COMMA b= select_sublist )* );
    public final ArrayList<DerivedColumn> select_list() throws RecognitionException {
        ArrayList<DerivedColumn> value = null;


        DerivedColumn a =null;

        DerivedColumn b =null;



          bSelectAll = false;
          value = new ArrayList<DerivedColumn>();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:285:3: ( ASTERISK |a= select_sublist ( COMMA b= select_sublist )* )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==ASTERISK) ) {
                alt6=1;
            }
            else if ( (LA6_0==ANY||LA6_0==AVG||LA6_0==COUNT||LA6_0==EVERY||LA6_0==LPAREN||(LA6_0 >= MAX && LA6_0 <= MIN)||LA6_0==SOME||(LA6_0 >= STRING_WITH_QUOTE_DOUBLE && LA6_0 <= SUM)||LA6_0==VARNAME) ) {
                alt6=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;

            }
            switch (alt6) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:285:5: ASTERISK
                    {
                    match(input,ASTERISK,FOLLOW_ASTERISK_in_select_list176); 

                     bSelectAll = true; value = null; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:286:5: a= select_sublist ( COMMA b= select_sublist )*
                    {
                    pushFollow(FOLLOW_select_sublist_in_select_list186);
                    a=select_sublist();

                    state._fsp--;


                     value.add(a); 

                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:286:48: ( COMMA b= select_sublist )*
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( (LA5_0==COMMA) ) {
                            alt5=1;
                        }


                        switch (alt5) {
                    	case 1 :
                    	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:286:49: COMMA b= select_sublist
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_select_list191); 

                    	    pushFollow(FOLLOW_select_sublist_in_select_list195);
                    	    b=select_sublist();

                    	    state._fsp--;


                    	     value.add(b); 

                    	    }
                    	    break;

                    	default :
                    	    break loop5;
                        }
                    } while (true);


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "select_list"



    // $ANTLR start "select_sublist"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:289:1: select_sublist returns [DerivedColumn value] : ( qualified_asterisk | derived_column );
    public final DerivedColumn select_sublist() throws RecognitionException {
        DerivedColumn value = null;


        DerivedColumn derived_column6 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:290:3: ( qualified_asterisk | derived_column )
            int alt7=2;
            switch ( input.LA(1) ) {
            case VARNAME:
                {
                int LA7_1 = input.LA(2);

                if ( (LA7_1==PERIOD) ) {
                    int LA7_4 = input.LA(3);

                    if ( (LA7_4==ASTERISK) ) {
                        alt7=1;
                    }
                    else if ( (LA7_4==STRING_WITH_QUOTE_DOUBLE||LA7_4==VARNAME) ) {
                        alt7=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 7, 4, input);

                        throw nvae;

                    }
                }
                else if ( (LA7_1==AS||LA7_1==COMMA||LA7_1==FROM||LA7_1==STRING_WITH_QUOTE_DOUBLE||LA7_1==VARNAME) ) {
                    alt7=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 7, 1, input);

                    throw nvae;

                }
                }
                break;
            case STRING_WITH_QUOTE_DOUBLE:
                {
                int LA7_2 = input.LA(2);

                if ( (LA7_2==PERIOD) ) {
                    int LA7_4 = input.LA(3);

                    if ( (LA7_4==ASTERISK) ) {
                        alt7=1;
                    }
                    else if ( (LA7_4==STRING_WITH_QUOTE_DOUBLE||LA7_4==VARNAME) ) {
                        alt7=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 7, 4, input);

                        throw nvae;

                    }
                }
                else if ( (LA7_2==AS||LA7_2==COMMA||LA7_2==FROM||LA7_2==STRING_WITH_QUOTE_DOUBLE||LA7_2==VARNAME) ) {
                    alt7=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 7, 2, input);

                    throw nvae;

                }
                }
                break;
            case ANY:
            case AVG:
            case COUNT:
            case EVERY:
            case LPAREN:
            case MAX:
            case MIN:
            case SOME:
            case SUM:
                {
                alt7=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;

            }

            switch (alt7) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:290:5: qualified_asterisk
                    {
                    pushFollow(FOLLOW_qualified_asterisk_in_select_sublist218);
                    qualified_asterisk();

                    state._fsp--;


                     value = null; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:291:5: derived_column
                    {
                    pushFollow(FOLLOW_derived_column_in_select_sublist226);
                    derived_column6=derived_column();

                    state._fsp--;


                     value = derived_column6; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "select_sublist"



    // $ANTLR start "qualified_asterisk"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:294:1: qualified_asterisk : table_identifier PERIOD ASTERISK ;
    public final void qualified_asterisk() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:295:3: ( table_identifier PERIOD ASTERISK )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:295:5: table_identifier PERIOD ASTERISK
            {
            pushFollow(FOLLOW_table_identifier_in_qualified_asterisk243);
            table_identifier();

            state._fsp--;


            match(input,PERIOD,FOLLOW_PERIOD_in_qualified_asterisk245); 

            match(input,ASTERISK,FOLLOW_ASTERISK_in_qualified_asterisk247); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "qualified_asterisk"



    // $ANTLR start "derived_column"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:298:1: derived_column returns [DerivedColumn value] : value_expression ( ( AS )? alias_name )? ;
    public final DerivedColumn derived_column() throws RecognitionException {
        DerivedColumn value = null;


        AbstractValueExpression value_expression7 =null;

        String alias_name8 =null;



          value = new DerivedColumn();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:302:3: ( value_expression ( ( AS )? alias_name )? )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:302:5: value_expression ( ( AS )? alias_name )?
            {
            pushFollow(FOLLOW_value_expression_in_derived_column271);
            value_expression7=value_expression();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:302:22: ( ( AS )? alias_name )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==AS||LA9_0==STRING_WITH_QUOTE_DOUBLE||LA9_0==VARNAME) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:302:23: ( AS )? alias_name
                    {
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:302:23: ( AS )?
                    int alt8=2;
                    int LA8_0 = input.LA(1);

                    if ( (LA8_0==AS) ) {
                        alt8=1;
                    }
                    switch (alt8) {
                        case 1 :
                            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:302:23: AS
                            {
                            match(input,AS,FOLLOW_AS_in_derived_column274); 

                            }
                            break;

                    }


                    pushFollow(FOLLOW_alias_name_in_derived_column277);
                    alias_name8=alias_name();

                    state._fsp--;


                    }
                    break;

            }



                  value.setValueExpression(value_expression7);
                  String alias = alias_name8;
                  if (alias != null) {
                    value.setAlias(alias_name8);
                  }
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "derived_column"



    // $ANTLR start "value_expression"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:311:1: value_expression returns [AbstractValueExpression value] : ( numeric_value_expression | string_value_expression | reference_value_expression | collection_value_expression );
    public final AbstractValueExpression value_expression() throws RecognitionException {
        AbstractValueExpression value = null;


        NumericValueExpression numeric_value_expression9 =null;

        StringValueExpression string_value_expression10 =null;

        ReferenceValueExpression reference_value_expression11 =null;

        CollectionValueExpression collection_value_expression12 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:312:3: ( numeric_value_expression | string_value_expression | reference_value_expression | collection_value_expression )
            int alt10=4;
            switch ( input.LA(1) ) {
            case LPAREN:
                {
                switch ( input.LA(2) ) {
                case VARNAME:
                    {
                    switch ( input.LA(3) ) {
                    case PERIOD:
                        {
                        int LA10_8 = input.LA(4);

                        if ( (LA10_8==VARNAME) ) {
                            int LA10_9 = input.LA(5);

                            if ( (LA10_9==ASTERISK||LA10_9==MINUS||LA10_9==PLUS||LA10_9==RPAREN||LA10_9==SOLIDUS) ) {
                                alt10=1;
                            }
                            else if ( (LA10_9==CONCATENATION) ) {
                                alt10=2;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 10, 9, input);

                                throw nvae;

                            }
                        }
                        else if ( (LA10_8==STRING_WITH_QUOTE_DOUBLE) ) {
                            int LA10_10 = input.LA(5);

                            if ( (LA10_10==ASTERISK||LA10_10==MINUS||LA10_10==PLUS||LA10_10==RPAREN||LA10_10==SOLIDUS) ) {
                                alt10=1;
                            }
                            else if ( (LA10_10==CONCATENATION) ) {
                                alt10=2;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 10, 10, input);

                                throw nvae;

                            }
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 10, 8, input);

                            throw nvae;

                        }
                        }
                        break;
                    case ASTERISK:
                    case MINUS:
                    case PLUS:
                    case RPAREN:
                    case SOLIDUS:
                        {
                        alt10=1;
                        }
                        break;
                    case CONCATENATION:
                        {
                        alt10=2;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 10, 4, input);

                        throw nvae;

                    }

                    }
                    break;
                case STRING_WITH_QUOTE_DOUBLE:
                    {
                    switch ( input.LA(3) ) {
                    case PERIOD:
                        {
                        int LA10_8 = input.LA(4);

                        if ( (LA10_8==VARNAME) ) {
                            int LA10_9 = input.LA(5);

                            if ( (LA10_9==ASTERISK||LA10_9==MINUS||LA10_9==PLUS||LA10_9==RPAREN||LA10_9==SOLIDUS) ) {
                                alt10=1;
                            }
                            else if ( (LA10_9==CONCATENATION) ) {
                                alt10=2;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 10, 9, input);

                                throw nvae;

                            }
                        }
                        else if ( (LA10_8==STRING_WITH_QUOTE_DOUBLE) ) {
                            int LA10_10 = input.LA(5);

                            if ( (LA10_10==ASTERISK||LA10_10==MINUS||LA10_10==PLUS||LA10_10==RPAREN||LA10_10==SOLIDUS) ) {
                                alt10=1;
                            }
                            else if ( (LA10_10==CONCATENATION) ) {
                                alt10=2;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 10, 10, input);

                                throw nvae;

                            }
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 10, 8, input);

                            throw nvae;

                        }
                        }
                        break;
                    case ASTERISK:
                    case MINUS:
                    case PLUS:
                    case RPAREN:
                    case SOLIDUS:
                        {
                        alt10=1;
                        }
                        break;
                    case CONCATENATION:
                        {
                        alt10=2;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 10, 5, input);

                        throw nvae;

                    }

                    }
                    break;
                case DECIMAL:
                case DECIMAL_NEGATIVE:
                case DECIMAL_POSITIVE:
                case INTEGER:
                case INTEGER_NEGATIVE:
                case INTEGER_POSITIVE:
                    {
                    alt10=1;
                    }
                    break;
                case FALSE:
                case STRING_WITH_QUOTE:
                case TRUE:
                    {
                    alt10=2;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 10, 1, input);

                    throw nvae;

                }

                }
                break;
            case STRING_WITH_QUOTE_DOUBLE:
            case VARNAME:
                {
                alt10=3;
                }
                break;
            case ANY:
            case AVG:
            case COUNT:
            case EVERY:
            case MAX:
            case MIN:
            case SOME:
            case SUM:
                {
                alt10=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;

            }

            switch (alt10) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:312:5: numeric_value_expression
                    {
                    pushFollow(FOLLOW_numeric_value_expression_in_value_expression301);
                    numeric_value_expression9=numeric_value_expression();

                    state._fsp--;


                     value = numeric_value_expression9; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:313:5: string_value_expression
                    {
                    pushFollow(FOLLOW_string_value_expression_in_value_expression309);
                    string_value_expression10=string_value_expression();

                    state._fsp--;


                     value = string_value_expression10; 

                    }
                    break;
                case 3 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:314:5: reference_value_expression
                    {
                    pushFollow(FOLLOW_reference_value_expression_in_value_expression317);
                    reference_value_expression11=reference_value_expression();

                    state._fsp--;


                     value = reference_value_expression11; 

                    }
                    break;
                case 4 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:315:5: collection_value_expression
                    {
                    pushFollow(FOLLOW_collection_value_expression_in_value_expression325);
                    collection_value_expression12=collection_value_expression();

                    state._fsp--;


                     value = collection_value_expression12; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "value_expression"



    // $ANTLR start "numeric_value_expression"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:318:1: numeric_value_expression returns [NumericValueExpression value] : LPAREN numeric_operation RPAREN ;
    public final NumericValueExpression numeric_value_expression() throws RecognitionException {
        NumericValueExpression value = null;



          numericExp = new NumericValueExpression();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:322:3: ( LPAREN numeric_operation RPAREN )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:322:5: LPAREN numeric_operation RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_numeric_value_expression349); 

            pushFollow(FOLLOW_numeric_operation_in_numeric_value_expression351);
            numeric_operation();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_numeric_value_expression353); 


                  value = numericExp;
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "numeric_value_expression"



    // $ANTLR start "numeric_operation"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:327:1: numeric_operation : term ( (t= PLUS |t= MINUS ) term )* ;
    public final void numeric_operation() throws RecognitionException {
        Token t=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:328:3: ( term ( (t= PLUS |t= MINUS ) term )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:328:5: term ( (t= PLUS |t= MINUS ) term )*
            {
            pushFollow(FOLLOW_term_in_numeric_operation368);
            term();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:329:5: ( (t= PLUS |t= MINUS ) term )*
            loop12:
            do {
                int alt12=2;
                int LA12_0 = input.LA(1);

                if ( (LA12_0==MINUS||LA12_0==PLUS) ) {
                    alt12=1;
                }


                switch (alt12) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:330:7: (t= PLUS |t= MINUS ) term
            	    {
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:330:7: (t= PLUS |t= MINUS )
            	    int alt11=2;
            	    int LA11_0 = input.LA(1);

            	    if ( (LA11_0==PLUS) ) {
            	        alt11=1;
            	    }
            	    else if ( (LA11_0==MINUS) ) {
            	        alt11=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 11, 0, input);

            	        throw nvae;

            	    }
            	    switch (alt11) {
            	        case 1 :
            	            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:330:8: t= PLUS
            	            {
            	            t=(Token)match(input,PLUS,FOLLOW_PLUS_in_numeric_operation386); 

            	            }
            	            break;
            	        case 2 :
            	            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:330:15: t= MINUS
            	            {
            	            t=(Token)match(input,MINUS,FOLLOW_MINUS_in_numeric_operation390); 

            	            }
            	            break;

            	    }


            	     numericExp.putSpecification((t!=null?t.getText():null)); 

            	    pushFollow(FOLLOW_term_in_numeric_operation403);
            	    term();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop12;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "numeric_operation"



    // $ANTLR start "term"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:335:1: term : a= factor ( (t= ASTERISK |t= SOLIDUS ) b= factor )* ;
    public final void term() throws RecognitionException {
        Token t=null;
        Object a =null;

        Object b =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:336:3: (a= factor ( (t= ASTERISK |t= SOLIDUS ) b= factor )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:336:5: a= factor ( (t= ASTERISK |t= SOLIDUS ) b= factor )*
            {
            pushFollow(FOLLOW_factor_in_term425);
            a=factor();

            state._fsp--;


             numericExp.putSpecification(a); 

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:337:5: ( (t= ASTERISK |t= SOLIDUS ) b= factor )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==ASTERISK||LA14_0==SOLIDUS) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:338:7: (t= ASTERISK |t= SOLIDUS ) b= factor
            	    {
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:338:7: (t= ASTERISK |t= SOLIDUS )
            	    int alt13=2;
            	    int LA13_0 = input.LA(1);

            	    if ( (LA13_0==ASTERISK) ) {
            	        alt13=1;
            	    }
            	    else if ( (LA13_0==SOLIDUS) ) {
            	        alt13=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 13, 0, input);

            	        throw nvae;

            	    }
            	    switch (alt13) {
            	        case 1 :
            	            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:338:8: t= ASTERISK
            	            {
            	            t=(Token)match(input,ASTERISK,FOLLOW_ASTERISK_in_term445); 

            	            }
            	            break;
            	        case 2 :
            	            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:338:19: t= SOLIDUS
            	            {
            	            t=(Token)match(input,SOLIDUS,FOLLOW_SOLIDUS_in_term449); 

            	            }
            	            break;

            	    }


            	     numericExp.putSpecification((t!=null?t.getText():null)); 

            	    pushFollow(FOLLOW_factor_in_term463);
            	    b=factor();

            	    state._fsp--;


            	     numericExp.putSpecification(b); 

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "term"



    // $ANTLR start "factor"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:343:1: factor returns [Object value] : ( column_reference | numeric_literal );
    public final Object factor() throws RecognitionException {
        Object value = null;


        ColumnReference column_reference13 =null;

        NumericLiteral numeric_literal14 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:344:3: ( column_reference | numeric_literal )
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==STRING_WITH_QUOTE_DOUBLE||LA15_0==VARNAME) ) {
                alt15=1;
            }
            else if ( ((LA15_0 >= DECIMAL && LA15_0 <= DECIMAL_POSITIVE)||(LA15_0 >= INTEGER && LA15_0 <= INTEGER_POSITIVE)) ) {
                alt15=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;

            }
            switch (alt15) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:344:5: column_reference
                    {
                    pushFollow(FOLLOW_column_reference_in_factor491);
                    column_reference13=column_reference();

                    state._fsp--;


                     value = column_reference13; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:345:5: numeric_literal
                    {
                    pushFollow(FOLLOW_numeric_literal_in_factor499);
                    numeric_literal14=numeric_literal();

                    state._fsp--;


                     value = numeric_literal14; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "factor"



    // $ANTLR start "sign"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:348:1: sign : ( PLUS | MINUS );
    public final void sign() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:349:3: ( PLUS | MINUS )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:
            {
            if ( input.LA(1)==MINUS||input.LA(1)==PLUS ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "sign"



    // $ANTLR start "string_value_expression"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:353:1: string_value_expression returns [StringValueExpression value] : LPAREN concatenation RPAREN ;
    public final StringValueExpression string_value_expression() throws RecognitionException {
        StringValueExpression value = null;



          stringExp = new StringValueExpression();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:357:3: ( LPAREN concatenation RPAREN )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:357:5: LPAREN concatenation RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_string_value_expression542); 

            pushFollow(FOLLOW_concatenation_in_string_value_expression544);
            concatenation();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_string_value_expression546); 


                  value = stringExp;
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "string_value_expression"



    // $ANTLR start "concatenation"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:362:1: concatenation : a= character_factor ( CONCATENATION b= character_factor )+ ;
    public final void concatenation() throws RecognitionException {
        Object a =null;

        Object b =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:363:3: (a= character_factor ( CONCATENATION b= character_factor )+ )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:363:5: a= character_factor ( CONCATENATION b= character_factor )+
            {
            pushFollow(FOLLOW_character_factor_in_concatenation565);
            a=character_factor();

            state._fsp--;


             stringExp.putSpecification(a); 

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:363:66: ( CONCATENATION b= character_factor )+
            int cnt16=0;
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( (LA16_0==CONCATENATION) ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:364:7: CONCATENATION b= character_factor
            	    {
            	    match(input,CONCATENATION,FOLLOW_CONCATENATION_in_concatenation577); 

            	     stringExp.putSpecification(StringValueExpression.CONCAT_OP); 

            	    pushFollow(FOLLOW_character_factor_in_concatenation590);
            	    b=character_factor();

            	    state._fsp--;


            	     stringExp.putSpecification(b); 

            	    }
            	    break;

            	default :
            	    if ( cnt16 >= 1 ) break loop16;
                        EarlyExitException eee =
                            new EarlyExitException(16, input);
                        throw eee;
                }
                cnt16++;
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "concatenation"



    // $ANTLR start "character_factor"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:368:1: character_factor returns [Object value] : ( column_reference | general_literal );
    public final Object character_factor() throws RecognitionException {
        Object value = null;


        ColumnReference column_reference15 =null;

        Literal general_literal16 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:369:3: ( column_reference | general_literal )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==STRING_WITH_QUOTE_DOUBLE||LA17_0==VARNAME) ) {
                alt17=1;
            }
            else if ( (LA17_0==FALSE||LA17_0==STRING_WITH_QUOTE||LA17_0==TRUE) ) {
                alt17=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;

            }
            switch (alt17) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:369:5: column_reference
                    {
                    pushFollow(FOLLOW_column_reference_in_character_factor611);
                    column_reference15=column_reference();

                    state._fsp--;


                     value = column_reference15; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:370:5: general_literal
                    {
                    pushFollow(FOLLOW_general_literal_in_character_factor619);
                    general_literal16=general_literal();

                    state._fsp--;


                     value = general_literal16; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "character_factor"



    // $ANTLR start "reference_value_expression"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:373:1: reference_value_expression returns [ReferenceValueExpression value] : column_reference ;
    public final ReferenceValueExpression reference_value_expression() throws RecognitionException {
        ReferenceValueExpression value = null;


        ColumnReference column_reference17 =null;



          referenceExp = new ReferenceValueExpression();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:377:3: ( column_reference )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:377:5: column_reference
            {
            pushFollow(FOLLOW_column_reference_in_reference_value_expression643);
            column_reference17=column_reference();

            state._fsp--;


             
                  referenceExp.add(column_reference17);
                  value = referenceExp;
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "reference_value_expression"



    // $ANTLR start "column_reference"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:383:1: column_reference returns [ColumnReference value] : (t= table_identifier PERIOD )? column_name ;
    public final ColumnReference column_reference() throws RecognitionException {
        ColumnReference value = null;


        String t =null;

        String column_name18 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:384:3: ( (t= table_identifier PERIOD )? column_name )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:384:5: (t= table_identifier PERIOD )? column_name
            {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:384:5: (t= table_identifier PERIOD )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==VARNAME) ) {
                int LA18_1 = input.LA(2);

                if ( (LA18_1==PERIOD) ) {
                    alt18=1;
                }
            }
            else if ( (LA18_0==STRING_WITH_QUOTE_DOUBLE) ) {
                int LA18_2 = input.LA(2);

                if ( (LA18_2==PERIOD) ) {
                    alt18=1;
                }
            }
            switch (alt18) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:384:6: t= table_identifier PERIOD
                    {
                    pushFollow(FOLLOW_table_identifier_in_column_reference665);
                    t=table_identifier();

                    state._fsp--;


                    match(input,PERIOD,FOLLOW_PERIOD_in_column_reference667); 

                    }
                    break;

            }


            pushFollow(FOLLOW_column_name_in_column_reference671);
            column_name18=column_name();

            state._fsp--;



                  String table = "";
                  if (t != null) {
                    table = t;
                  }
                  value = new ColumnReference(table, column_name18);
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "column_reference"



    // $ANTLR start "collection_value_expression"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:393:1: collection_value_expression returns [CollectionValueExpression value] : set_function_specification ;
    public final CollectionValueExpression collection_value_expression() throws RecognitionException {
        CollectionValueExpression value = null;



          collectionExp = new CollectionValueExpression();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:397:3: ( set_function_specification )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:397:5: set_function_specification
            {
            pushFollow(FOLLOW_set_function_specification_in_collection_value_expression699);
            set_function_specification();

            state._fsp--;


             
                  value = collectionExp;
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "collection_value_expression"



    // $ANTLR start "set_function_specification"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:402:1: set_function_specification : ( COUNT LPAREN ASTERISK RPAREN | general_set_function );
    public final void set_function_specification() throws RecognitionException {
        Token COUNT19=null;
        Token ASTERISK20=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:403:3: ( COUNT LPAREN ASTERISK RPAREN | general_set_function )
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==COUNT) ) {
                int LA19_1 = input.LA(2);

                if ( (LA19_1==LPAREN) ) {
                    int LA19_3 = input.LA(3);

                    if ( (LA19_3==ASTERISK) ) {
                        alt19=1;
                    }
                    else if ( (LA19_3==STRING_WITH_QUOTE_DOUBLE||LA19_3==VARNAME) ) {
                        alt19=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 19, 3, input);

                        throw nvae;

                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 19, 1, input);

                    throw nvae;

                }
            }
            else if ( (LA19_0==ANY||LA19_0==AVG||LA19_0==EVERY||(LA19_0 >= MAX && LA19_0 <= MIN)||LA19_0==SOME||LA19_0==SUM) ) {
                alt19=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;

            }
            switch (alt19) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:403:5: COUNT LPAREN ASTERISK RPAREN
                    {
                    COUNT19=(Token)match(input,COUNT,FOLLOW_COUNT_in_set_function_specification714); 

                    match(input,LPAREN,FOLLOW_LPAREN_in_set_function_specification716); 

                    ASTERISK20=(Token)match(input,ASTERISK,FOLLOW_ASTERISK_in_set_function_specification718); 

                    match(input,RPAREN,FOLLOW_RPAREN_in_set_function_specification720); 


                          collectionExp.putSpecification((COUNT19!=null?COUNT19.getText():null));
                          collectionExp.putSpecification((ASTERISK20!=null?ASTERISK20.getText():null));
                        

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:407:5: general_set_function
                    {
                    pushFollow(FOLLOW_general_set_function_in_set_function_specification728);
                    general_set_function();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "set_function_specification"



    // $ANTLR start "general_set_function"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:411:1: general_set_function : set_function_op LPAREN column_reference RPAREN ;
    public final void general_set_function() throws RecognitionException {
        String set_function_op21 =null;

        ColumnReference column_reference22 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:412:3: ( set_function_op LPAREN column_reference RPAREN )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:412:5: set_function_op LPAREN column_reference RPAREN
            {
            pushFollow(FOLLOW_set_function_op_in_general_set_function743);
            set_function_op21=set_function_op();

            state._fsp--;


            match(input,LPAREN,FOLLOW_LPAREN_in_general_set_function745); 

            pushFollow(FOLLOW_column_reference_in_general_set_function747);
            column_reference22=column_reference();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_general_set_function749); 


                  collectionExp.putSpecification(set_function_op21);
                  collectionExp.add(column_reference22);
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "general_set_function"



    // $ANTLR start "set_function_op"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:418:1: set_function_op returns [String value] : (t= AVG |t= MAX |t= MIN |t= SUM |t= EVERY |t= ANY |t= SOME |t= COUNT ) ;
    public final String set_function_op() throws RecognitionException {
        String value = null;


        Token t=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:419:3: ( (t= AVG |t= MAX |t= MIN |t= SUM |t= EVERY |t= ANY |t= SOME |t= COUNT ) )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:419:5: (t= AVG |t= MAX |t= MIN |t= SUM |t= EVERY |t= ANY |t= SOME |t= COUNT )
            {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:419:5: (t= AVG |t= MAX |t= MIN |t= SUM |t= EVERY |t= ANY |t= SOME |t= COUNT )
            int alt20=8;
            switch ( input.LA(1) ) {
            case AVG:
                {
                alt20=1;
                }
                break;
            case MAX:
                {
                alt20=2;
                }
                break;
            case MIN:
                {
                alt20=3;
                }
                break;
            case SUM:
                {
                alt20=4;
                }
                break;
            case EVERY:
                {
                alt20=5;
                }
                break;
            case ANY:
                {
                alt20=6;
                }
                break;
            case SOME:
                {
                alt20=7;
                }
                break;
            case COUNT:
                {
                alt20=8;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;

            }

            switch (alt20) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:419:6: t= AVG
                    {
                    t=(Token)match(input,AVG,FOLLOW_AVG_in_set_function_op773); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:419:14: t= MAX
                    {
                    t=(Token)match(input,MAX,FOLLOW_MAX_in_set_function_op779); 

                    }
                    break;
                case 3 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:419:22: t= MIN
                    {
                    t=(Token)match(input,MIN,FOLLOW_MIN_in_set_function_op785); 

                    }
                    break;
                case 4 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:419:30: t= SUM
                    {
                    t=(Token)match(input,SUM,FOLLOW_SUM_in_set_function_op791); 

                    }
                    break;
                case 5 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:419:38: t= EVERY
                    {
                    t=(Token)match(input,EVERY,FOLLOW_EVERY_in_set_function_op797); 

                    }
                    break;
                case 6 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:419:48: t= ANY
                    {
                    t=(Token)match(input,ANY,FOLLOW_ANY_in_set_function_op803); 

                    }
                    break;
                case 7 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:419:56: t= SOME
                    {
                    t=(Token)match(input,SOME,FOLLOW_SOME_in_set_function_op809); 

                    }
                    break;
                case 8 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:419:65: t= COUNT
                    {
                    t=(Token)match(input,COUNT,FOLLOW_COUNT_in_set_function_op815); 

                    }
                    break;

            }



                  value = (t!=null?t.getText():null);
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "set_function_op"



    // $ANTLR start "row_value_expression"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:424:1: row_value_expression returns [IValueExpression value] : ( literal | value_expression );
    public final IValueExpression row_value_expression() throws RecognitionException {
        IValueExpression value = null;


        Literal literal23 =null;

        AbstractValueExpression value_expression24 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:425:3: ( literal | value_expression )
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( ((LA21_0 >= DECIMAL && LA21_0 <= DECIMAL_POSITIVE)||LA21_0==FALSE||(LA21_0 >= INTEGER && LA21_0 <= INTEGER_POSITIVE)||LA21_0==STRING_WITH_QUOTE||LA21_0==TRUE) ) {
                alt21=1;
            }
            else if ( (LA21_0==ANY||LA21_0==AVG||LA21_0==COUNT||LA21_0==EVERY||LA21_0==LPAREN||(LA21_0 >= MAX && LA21_0 <= MIN)||LA21_0==SOME||(LA21_0 >= STRING_WITH_QUOTE_DOUBLE && LA21_0 <= SUM)||LA21_0==VARNAME) ) {
                alt21=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 21, 0, input);

                throw nvae;

            }
            switch (alt21) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:425:5: literal
                    {
                    pushFollow(FOLLOW_literal_in_row_value_expression837);
                    literal23=literal();

                    state._fsp--;


                     value = literal23; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:426:5: value_expression
                    {
                    pushFollow(FOLLOW_value_expression_in_row_value_expression845);
                    value_expression24=value_expression();

                    state._fsp--;


                     value = value_expression24; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "row_value_expression"



    // $ANTLR start "literal"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:429:1: literal returns [Literal value] : ( numeric_literal | general_literal );
    public final Literal literal() throws RecognitionException {
        Literal value = null;


        NumericLiteral numeric_literal25 =null;

        Literal general_literal26 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:430:3: ( numeric_literal | general_literal )
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( ((LA22_0 >= DECIMAL && LA22_0 <= DECIMAL_POSITIVE)||(LA22_0 >= INTEGER && LA22_0 <= INTEGER_POSITIVE)) ) {
                alt22=1;
            }
            else if ( (LA22_0==FALSE||LA22_0==STRING_WITH_QUOTE||LA22_0==TRUE) ) {
                alt22=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;

            }
            switch (alt22) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:430:5: numeric_literal
                    {
                    pushFollow(FOLLOW_numeric_literal_in_literal864);
                    numeric_literal25=numeric_literal();

                    state._fsp--;


                     value = numeric_literal25; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:431:5: general_literal
                    {
                    pushFollow(FOLLOW_general_literal_in_literal872);
                    general_literal26=general_literal();

                    state._fsp--;


                     value = general_literal26; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "literal"



    // $ANTLR start "table_expression"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:434:1: table_expression returns [TableExpression value] : from_clause ( where_clause )? ( group_by_clause )? ;
    public final TableExpression table_expression() throws RecognitionException {
        TableExpression value = null;


        ArrayList<TablePrimary> from_clause27 =null;

        BooleanValueExpression where_clause28 =null;

        ArrayList<GroupingElement> group_by_clause29 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:435:3: ( from_clause ( where_clause )? ( group_by_clause )? )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:435:5: from_clause ( where_clause )? ( group_by_clause )?
            {
            pushFollow(FOLLOW_from_clause_in_table_expression891);
            from_clause27=from_clause();

            state._fsp--;



                  value = new TableExpression(from_clause27);
                

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:438:5: ( where_clause )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==WHERE) ) {
                alt23=1;
            }
            switch (alt23) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:438:6: where_clause
                    {
                    pushFollow(FOLLOW_where_clause_in_table_expression900);
                    where_clause28=where_clause();

                    state._fsp--;


                     value.setWhereClause(where_clause28); 

                    }
                    break;

            }


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:439:5: ( group_by_clause )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==GROUP) ) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:439:6: group_by_clause
                    {
                    pushFollow(FOLLOW_group_by_clause_in_table_expression912);
                    group_by_clause29=group_by_clause();

                    state._fsp--;


                     value.setGroupByClause(group_by_clause29); 

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "table_expression"



    // $ANTLR start "from_clause"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:442:1: from_clause returns [ArrayList<TablePrimary> value] : FROM table_reference_list ;
    public final ArrayList<TablePrimary> from_clause() throws RecognitionException {
        ArrayList<TablePrimary> value = null;


        ArrayList<TablePrimary> table_reference_list30 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:443:3: ( FROM table_reference_list )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:443:5: FROM table_reference_list
            {
            match(input,FROM,FOLLOW_FROM_in_from_clause935); 

            pushFollow(FOLLOW_table_reference_list_in_from_clause937);
            table_reference_list30=table_reference_list();

            state._fsp--;



                  value = table_reference_list30;
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "from_clause"



    // $ANTLR start "table_reference_list"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:448:1: table_reference_list returns [ArrayList<TablePrimary> value] : a= table_reference ( COMMA b= table_reference )* ;
    public final ArrayList<TablePrimary> table_reference_list() throws RecognitionException {
        ArrayList<TablePrimary> value = null;


        TablePrimary a =null;

        TablePrimary b =null;



          value = new ArrayList<TablePrimary>();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:452:3: (a= table_reference ( COMMA b= table_reference )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:452:5: a= table_reference ( COMMA b= table_reference )*
            {
            pushFollow(FOLLOW_table_reference_in_table_reference_list967);
            a=table_reference();

            state._fsp--;


             value.add(a); 

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:453:5: ( COMMA b= table_reference )*
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( (LA25_0==COMMA) ) {
                    alt25=1;
                }


                switch (alt25) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:454:7: COMMA b= table_reference
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_table_reference_list984); 

            	    pushFollow(FOLLOW_table_reference_in_table_reference_list988);
            	    b=table_reference();

            	    state._fsp--;



            	            JoinOperator joinOp = new JoinOperator(JoinOperator.CROSS_JOIN);
            	            relationStack.push(joinOp);
            	            
            	            value.add(b);
            	          

            	    }
            	    break;

            	default :
            	    break loop25;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "table_reference_list"



    // $ANTLR start "table_reference"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:462:1: table_reference returns [TablePrimary value] : table_primary ( joined_table )? ;
    public final TablePrimary table_reference() throws RecognitionException {
        TablePrimary value = null;


        TablePrimary table_primary31 =null;

        TablePrimary joined_table32 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:463:3: ( table_primary ( joined_table )? )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:463:5: table_primary ( joined_table )?
            {
            pushFollow(FOLLOW_table_primary_in_table_reference1015);
            table_primary31=table_primary();

            state._fsp--;


             value = table_primary31; 

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:464:5: ( joined_table )?
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==FULL||LA26_0==INNER||(LA26_0 >= JOIN && LA26_0 <= LEFT)||LA26_0==RIGHT) ) {
                alt26=1;
            }
            switch (alt26) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:464:6: joined_table
                    {
                    pushFollow(FOLLOW_joined_table_in_table_reference1024);
                    joined_table32=joined_table();

                    state._fsp--;


                     value = joined_table32; 

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "table_reference"



    // $ANTLR start "where_clause"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:467:1: where_clause returns [BooleanValueExpression value] : WHERE search_condition ;
    public final BooleanValueExpression where_clause() throws RecognitionException {
        BooleanValueExpression value = null;


        BooleanValueExpression search_condition33 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:468:3: ( WHERE search_condition )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:468:5: WHERE search_condition
            {
            match(input,WHERE,FOLLOW_WHERE_in_where_clause1046); 

            pushFollow(FOLLOW_search_condition_in_where_clause1048);
            search_condition33=search_condition();

            state._fsp--;



                  value = search_condition33;
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "where_clause"



    // $ANTLR start "search_condition"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:473:1: search_condition returns [BooleanValueExpression value] : boolean_value_expression ;
    public final BooleanValueExpression search_condition() throws RecognitionException {
        BooleanValueExpression value = null;


        BooleanValueExpression boolean_value_expression34 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:474:3: ( boolean_value_expression )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:474:5: boolean_value_expression
            {
            pushFollow(FOLLOW_boolean_value_expression_in_search_condition1067);
            boolean_value_expression34=boolean_value_expression();

            state._fsp--;



                  value = boolean_value_expression34;
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "search_condition"



    // $ANTLR start "boolean_value_expression"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:479:1: boolean_value_expression returns [BooleanValueExpression value] : boolean_term ( OR boolean_term )* ;
    public final BooleanValueExpression boolean_value_expression() throws RecognitionException {
        BooleanValueExpression value = null;



          booleanExp = new BooleanValueExpression();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:483:3: ( boolean_term ( OR boolean_term )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:483:5: boolean_term ( OR boolean_term )*
            {
            pushFollow(FOLLOW_boolean_term_in_boolean_value_expression1093);
            boolean_term();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:483:18: ( OR boolean_term )*
            loop27:
            do {
                int alt27=2;
                int LA27_0 = input.LA(1);

                if ( (LA27_0==OR) ) {
                    alt27=1;
                }


                switch (alt27) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:483:19: OR boolean_term
            	    {
            	    match(input,OR,FOLLOW_OR_in_boolean_value_expression1096); 

            	     booleanExp.putSpecification(new OrOperator()); 

            	    pushFollow(FOLLOW_boolean_term_in_boolean_value_expression1100);
            	    boolean_term();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop27;
                }
            } while (true);



                  value = booleanExp;
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "boolean_value_expression"



    // $ANTLR start "boolean_term"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:488:1: boolean_term : boolean_factor ( AND boolean_factor )* ;
    public final void boolean_term() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:489:3: ( boolean_factor ( AND boolean_factor )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:489:5: boolean_factor ( AND boolean_factor )*
            {
            pushFollow(FOLLOW_boolean_factor_in_boolean_term1119);
            boolean_factor();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:489:20: ( AND boolean_factor )*
            loop28:
            do {
                int alt28=2;
                int LA28_0 = input.LA(1);

                if ( (LA28_0==AND) ) {
                    alt28=1;
                }


                switch (alt28) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:489:21: AND boolean_factor
            	    {
            	    match(input,AND,FOLLOW_AND_in_boolean_term1122); 

            	     booleanExp.putSpecification(new AndOperator()); 

            	    pushFollow(FOLLOW_boolean_factor_in_boolean_term1126);
            	    boolean_factor();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop28;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "boolean_term"



    // $ANTLR start "boolean_factor"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:493:1: boolean_factor : predicate ;
    public final void boolean_factor() throws RecognitionException {
        IPredicate predicate35 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:494:3: ( predicate )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:494:5: predicate
            {
            pushFollow(FOLLOW_predicate_in_boolean_factor1142);
            predicate35=predicate();

            state._fsp--;


             booleanExp.putSpecification(predicate35); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "boolean_factor"



    // $ANTLR start "predicate"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:497:1: predicate returns [IPredicate value] : ( comparison_predicate | null_predicate | in_predicate );
    public final IPredicate predicate() throws RecognitionException {
        IPredicate value = null;


        ComparisonPredicate comparison_predicate36 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:498:3: ( comparison_predicate | null_predicate | in_predicate )
            int alt29=3;
            switch ( input.LA(1) ) {
            case ANY:
            case AVG:
            case COUNT:
            case DECIMAL:
            case DECIMAL_NEGATIVE:
            case DECIMAL_POSITIVE:
            case EVERY:
            case FALSE:
            case INTEGER:
            case INTEGER_NEGATIVE:
            case INTEGER_POSITIVE:
            case LPAREN:
            case MAX:
            case MIN:
            case SOME:
            case STRING_WITH_QUOTE:
            case SUM:
            case TRUE:
                {
                alt29=1;
                }
                break;
            case VARNAME:
                {
                switch ( input.LA(2) ) {
                case PERIOD:
                    {
                    int LA29_4 = input.LA(3);

                    if ( (LA29_4==VARNAME) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case GREATER:
                        case LESS:
                            {
                            alt29=1;
                            }
                            break;
                        case IS:
                            {
                            alt29=2;
                            }
                            break;
                        case IN:
                        case NOT:
                            {
                            alt29=3;
                            }
                            break;
                        default:
                            NoViableAltException nvae =
                                new NoViableAltException("", 29, 7, input);

                            throw nvae;

                        }

                    }
                    else if ( (LA29_4==STRING_WITH_QUOTE_DOUBLE) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case GREATER:
                        case LESS:
                            {
                            alt29=1;
                            }
                            break;
                        case IS:
                            {
                            alt29=2;
                            }
                            break;
                        case IN:
                        case NOT:
                            {
                            alt29=3;
                            }
                            break;
                        default:
                            NoViableAltException nvae =
                                new NoViableAltException("", 29, 8, input);

                            throw nvae;

                        }

                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 29, 4, input);

                        throw nvae;

                    }
                    }
                    break;
                case EQUALS:
                case GREATER:
                case LESS:
                    {
                    alt29=1;
                    }
                    break;
                case IS:
                    {
                    alt29=2;
                    }
                    break;
                case IN:
                case NOT:
                    {
                    alt29=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 29, 2, input);

                    throw nvae;

                }

                }
                break;
            case STRING_WITH_QUOTE_DOUBLE:
                {
                switch ( input.LA(2) ) {
                case PERIOD:
                    {
                    int LA29_4 = input.LA(3);

                    if ( (LA29_4==VARNAME) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case GREATER:
                        case LESS:
                            {
                            alt29=1;
                            }
                            break;
                        case IS:
                            {
                            alt29=2;
                            }
                            break;
                        case IN:
                        case NOT:
                            {
                            alt29=3;
                            }
                            break;
                        default:
                            NoViableAltException nvae =
                                new NoViableAltException("", 29, 7, input);

                            throw nvae;

                        }

                    }
                    else if ( (LA29_4==STRING_WITH_QUOTE_DOUBLE) ) {
                        switch ( input.LA(4) ) {
                        case EQUALS:
                        case GREATER:
                        case LESS:
                            {
                            alt29=1;
                            }
                            break;
                        case IS:
                            {
                            alt29=2;
                            }
                            break;
                        case IN:
                        case NOT:
                            {
                            alt29=3;
                            }
                            break;
                        default:
                            NoViableAltException nvae =
                                new NoViableAltException("", 29, 8, input);

                            throw nvae;

                        }

                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 29, 4, input);

                        throw nvae;

                    }
                    }
                    break;
                case EQUALS:
                case GREATER:
                case LESS:
                    {
                    alt29=1;
                    }
                    break;
                case IS:
                    {
                    alt29=2;
                    }
                    break;
                case IN:
                case NOT:
                    {
                    alt29=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 29, 3, input);

                    throw nvae;

                }

                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 29, 0, input);

                throw nvae;

            }

            switch (alt29) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:498:5: comparison_predicate
                    {
                    pushFollow(FOLLOW_comparison_predicate_in_predicate1162);
                    comparison_predicate36=comparison_predicate();

                    state._fsp--;


                     value = comparison_predicate36; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:499:5: null_predicate
                    {
                    pushFollow(FOLLOW_null_predicate_in_predicate1170);
                    null_predicate();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:500:5: in_predicate
                    {
                    pushFollow(FOLLOW_in_predicate_in_predicate1176);
                    in_predicate();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "predicate"



    // $ANTLR start "comparison_predicate"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:503:1: comparison_predicate returns [ComparisonPredicate value] : a= row_value_expression comp_op b= row_value_expression ;
    public final ComparisonPredicate comparison_predicate() throws RecognitionException {
        ComparisonPredicate value = null;


        IValueExpression a =null;

        IValueExpression b =null;

        ComparisonPredicate.Operator comp_op37 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:504:3: (a= row_value_expression comp_op b= row_value_expression )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:504:5: a= row_value_expression comp_op b= row_value_expression
            {
            pushFollow(FOLLOW_row_value_expression_in_comparison_predicate1197);
            a=row_value_expression();

            state._fsp--;


            pushFollow(FOLLOW_comp_op_in_comparison_predicate1199);
            comp_op37=comp_op();

            state._fsp--;


            pushFollow(FOLLOW_row_value_expression_in_comparison_predicate1203);
            b=row_value_expression();

            state._fsp--;



                  value = new ComparisonPredicate(a, b, comp_op37);
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "comparison_predicate"



    // $ANTLR start "comp_op"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:509:1: comp_op returns [ComparisonPredicate.Operator value] : ( EQUALS | LESS GREATER | LESS | GREATER | LESS EQUALS | GREATER EQUALS );
    public final ComparisonPredicate.Operator comp_op() throws RecognitionException {
        ComparisonPredicate.Operator value = null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:510:3: ( EQUALS | LESS GREATER | LESS | GREATER | LESS EQUALS | GREATER EQUALS )
            int alt30=6;
            switch ( input.LA(1) ) {
            case EQUALS:
                {
                alt30=1;
                }
                break;
            case LESS:
                {
                switch ( input.LA(2) ) {
                case GREATER:
                    {
                    alt30=2;
                    }
                    break;
                case EQUALS:
                    {
                    alt30=5;
                    }
                    break;
                case ANY:
                case AVG:
                case COUNT:
                case DECIMAL:
                case DECIMAL_NEGATIVE:
                case DECIMAL_POSITIVE:
                case EVERY:
                case FALSE:
                case INTEGER:
                case INTEGER_NEGATIVE:
                case INTEGER_POSITIVE:
                case LPAREN:
                case MAX:
                case MIN:
                case SOME:
                case STRING_WITH_QUOTE:
                case STRING_WITH_QUOTE_DOUBLE:
                case SUM:
                case TRUE:
                case VARNAME:
                    {
                    alt30=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 30, 2, input);

                    throw nvae;

                }

                }
                break;
            case GREATER:
                {
                int LA30_3 = input.LA(2);

                if ( (LA30_3==EQUALS) ) {
                    alt30=6;
                }
                else if ( (LA30_3==ANY||LA30_3==AVG||(LA30_3 >= COUNT && LA30_3 <= DECIMAL_POSITIVE)||LA30_3==EVERY||LA30_3==FALSE||(LA30_3 >= INTEGER && LA30_3 <= INTEGER_POSITIVE)||LA30_3==LPAREN||(LA30_3 >= MAX && LA30_3 <= MIN)||(LA30_3 >= SOME && LA30_3 <= SUM)||LA30_3==TRUE||LA30_3==VARNAME) ) {
                    alt30=4;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 30, 3, input);

                    throw nvae;

                }
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                throw nvae;

            }

            switch (alt30) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:510:5: EQUALS
                    {
                    match(input,EQUALS,FOLLOW_EQUALS_in_comp_op1222); 

                     value = ComparisonPredicate.Operator.EQ; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:511:5: LESS GREATER
                    {
                    match(input,LESS,FOLLOW_LESS_in_comp_op1230); 

                    match(input,GREATER,FOLLOW_GREATER_in_comp_op1232); 

                     value = ComparisonPredicate.Operator.NE; 

                    }
                    break;
                case 3 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:512:5: LESS
                    {
                    match(input,LESS,FOLLOW_LESS_in_comp_op1240); 

                     value = ComparisonPredicate.Operator.LT; 

                    }
                    break;
                case 4 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:513:5: GREATER
                    {
                    match(input,GREATER,FOLLOW_GREATER_in_comp_op1248); 

                     value = ComparisonPredicate.Operator.GT; 

                    }
                    break;
                case 5 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:514:5: LESS EQUALS
                    {
                    match(input,LESS,FOLLOW_LESS_in_comp_op1256); 

                    match(input,EQUALS,FOLLOW_EQUALS_in_comp_op1258); 

                     value = ComparisonPredicate.Operator.LE; 

                    }
                    break;
                case 6 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:515:5: GREATER EQUALS
                    {
                    match(input,GREATER,FOLLOW_GREATER_in_comp_op1266); 

                    match(input,EQUALS,FOLLOW_EQUALS_in_comp_op1268); 

                     value = ComparisonPredicate.Operator.GE; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "comp_op"



    // $ANTLR start "null_predicate"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:518:1: null_predicate : column_reference IS ( NOT )? NULL ;
    public final void null_predicate() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:519:3: ( column_reference IS ( NOT )? NULL )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:519:5: column_reference IS ( NOT )? NULL
            {
            pushFollow(FOLLOW_column_reference_in_null_predicate1283);
            column_reference();

            state._fsp--;


            match(input,IS,FOLLOW_IS_in_null_predicate1285); 

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:519:25: ( NOT )?
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==NOT) ) {
                alt31=1;
            }
            switch (alt31) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:519:26: NOT
                    {
                    match(input,NOT,FOLLOW_NOT_in_null_predicate1288); 

                    }
                    break;

            }


            match(input,NULL,FOLLOW_NULL_in_null_predicate1292); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "null_predicate"



    // $ANTLR start "in_predicate"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:522:1: in_predicate : column_reference ( NOT )? IN in_predicate_value ;
    public final void in_predicate() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:523:3: ( column_reference ( NOT )? IN in_predicate_value )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:523:5: column_reference ( NOT )? IN in_predicate_value
            {
            pushFollow(FOLLOW_column_reference_in_in_predicate1305);
            column_reference();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:523:22: ( NOT )?
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==NOT) ) {
                alt32=1;
            }
            switch (alt32) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:523:23: NOT
                    {
                    match(input,NOT,FOLLOW_NOT_in_in_predicate1308); 

                    }
                    break;

            }


            match(input,IN,FOLLOW_IN_in_in_predicate1312); 

            pushFollow(FOLLOW_in_predicate_value_in_in_predicate1314);
            in_predicate_value();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "in_predicate"



    // $ANTLR start "in_predicate_value"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:526:1: in_predicate_value : ( table_subquery | LPAREN in_value_list RPAREN );
    public final void in_predicate_value() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:527:3: ( table_subquery | LPAREN in_value_list RPAREN )
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==LPAREN) ) {
                int LA33_1 = input.LA(2);

                if ( (LA33_1==SELECT) ) {
                    alt33=1;
                }
                else if ( (LA33_1==ANY||LA33_1==AVG||(LA33_1 >= COUNT && LA33_1 <= DECIMAL_POSITIVE)||LA33_1==EVERY||LA33_1==FALSE||(LA33_1 >= INTEGER && LA33_1 <= INTEGER_POSITIVE)||LA33_1==LPAREN||(LA33_1 >= MAX && LA33_1 <= MIN)||(LA33_1 >= SOME && LA33_1 <= SUM)||LA33_1==TRUE||LA33_1==VARNAME) ) {
                    alt33=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 33, 1, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 33, 0, input);

                throw nvae;

            }
            switch (alt33) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:527:5: table_subquery
                    {
                    pushFollow(FOLLOW_table_subquery_in_in_predicate_value1329);
                    table_subquery();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:528:5: LPAREN in_value_list RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_in_predicate_value1335); 

                    pushFollow(FOLLOW_in_value_list_in_in_predicate_value1337);
                    in_value_list();

                    state._fsp--;


                    match(input,RPAREN,FOLLOW_RPAREN_in_in_predicate_value1339); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "in_predicate_value"



    // $ANTLR start "table_subquery"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:531:1: table_subquery : subquery ;
    public final void table_subquery() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:532:3: ( subquery )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:532:5: subquery
            {
            pushFollow(FOLLOW_subquery_in_table_subquery1352);
            subquery();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "table_subquery"



    // $ANTLR start "subquery"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:535:1: subquery : LPAREN query RPAREN ;
    public final void subquery() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:536:3: ( LPAREN query RPAREN )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:536:5: LPAREN query RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_subquery1365); 

            pushFollow(FOLLOW_query_in_subquery1367);
            query();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_subquery1369); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "subquery"



    // $ANTLR start "in_value_list"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:539:1: in_value_list : row_value_expression ( COMMA row_value_expression )* ;
    public final void in_value_list() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:540:3: ( row_value_expression ( COMMA row_value_expression )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:540:5: row_value_expression ( COMMA row_value_expression )*
            {
            pushFollow(FOLLOW_row_value_expression_in_in_value_list1384);
            row_value_expression();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:540:26: ( COMMA row_value_expression )*
            loop34:
            do {
                int alt34=2;
                int LA34_0 = input.LA(1);

                if ( (LA34_0==COMMA) ) {
                    alt34=1;
                }


                switch (alt34) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:540:27: COMMA row_value_expression
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_in_value_list1387); 

            	    pushFollow(FOLLOW_row_value_expression_in_in_value_list1389);
            	    row_value_expression();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop34;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "in_value_list"



    // $ANTLR start "group_by_clause"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:543:1: group_by_clause returns [ArrayList<GroupingElement> value] : GROUP BY grouping_element_list ;
    public final ArrayList<GroupingElement> group_by_clause() throws RecognitionException {
        ArrayList<GroupingElement> value = null;


        ArrayList<GroupingElement> grouping_element_list38 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:544:3: ( GROUP BY grouping_element_list )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:544:5: GROUP BY grouping_element_list
            {
            match(input,GROUP,FOLLOW_GROUP_in_group_by_clause1408); 

            match(input,BY,FOLLOW_BY_in_group_by_clause1410); 

            pushFollow(FOLLOW_grouping_element_list_in_group_by_clause1412);
            grouping_element_list38=grouping_element_list();

            state._fsp--;



                  value = grouping_element_list38;
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "group_by_clause"



    // $ANTLR start "grouping_element_list"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:549:1: grouping_element_list returns [ArrayList<GroupingElement> value] : a= grouping_element ( COMMA b= grouping_element )* ;
    public final ArrayList<GroupingElement> grouping_element_list() throws RecognitionException {
        ArrayList<GroupingElement> value = null;


        GroupingElement a =null;

        GroupingElement b =null;



          value = new ArrayList<GroupingElement>();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:553:3: (a= grouping_element ( COMMA b= grouping_element )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:553:5: a= grouping_element ( COMMA b= grouping_element )*
            {
            pushFollow(FOLLOW_grouping_element_in_grouping_element_list1438);
            a=grouping_element();

            state._fsp--;


             value.add(a); 

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:554:5: ( COMMA b= grouping_element )*
            loop35:
            do {
                int alt35=2;
                int LA35_0 = input.LA(1);

                if ( (LA35_0==COMMA) ) {
                    alt35=1;
                }


                switch (alt35) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:554:6: COMMA b= grouping_element
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_grouping_element_list1448); 

            	    pushFollow(FOLLOW_grouping_element_in_grouping_element_list1452);
            	    b=grouping_element();

            	    state._fsp--;


            	     value.add(b); 

            	    }
            	    break;

            	default :
            	    break loop35;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "grouping_element_list"



    // $ANTLR start "grouping_element"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:557:1: grouping_element returns [GroupingElement value] : ( grouping_column_reference | LPAREN grouping_column_reference_list RPAREN );
    public final GroupingElement grouping_element() throws RecognitionException {
        GroupingElement value = null;


        ColumnReference grouping_column_reference39 =null;

        ArrayList<ColumnReference> grouping_column_reference_list40 =null;



          value = new GroupingElement();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:561:3: ( grouping_column_reference | LPAREN grouping_column_reference_list RPAREN )
            int alt36=2;
            int LA36_0 = input.LA(1);

            if ( (LA36_0==STRING_WITH_QUOTE_DOUBLE||LA36_0==VARNAME) ) {
                alt36=1;
            }
            else if ( (LA36_0==LPAREN) ) {
                alt36=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 36, 0, input);

                throw nvae;

            }
            switch (alt36) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:561:5: grouping_column_reference
                    {
                    pushFollow(FOLLOW_grouping_column_reference_in_grouping_element1480);
                    grouping_column_reference39=grouping_column_reference();

                    state._fsp--;


                     value.add(grouping_column_reference39); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:562:5: LPAREN grouping_column_reference_list RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_grouping_element1488); 

                    pushFollow(FOLLOW_grouping_column_reference_list_in_grouping_element1490);
                    grouping_column_reference_list40=grouping_column_reference_list();

                    state._fsp--;


                    match(input,RPAREN,FOLLOW_RPAREN_in_grouping_element1492); 

                     value.update(grouping_column_reference_list40); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "grouping_element"



    // $ANTLR start "grouping_column_reference"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:565:1: grouping_column_reference returns [ColumnReference value] : column_reference ;
    public final ColumnReference grouping_column_reference() throws RecognitionException {
        ColumnReference value = null;


        ColumnReference column_reference41 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:566:3: ( column_reference )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:566:5: column_reference
            {
            pushFollow(FOLLOW_column_reference_in_grouping_column_reference1513);
            column_reference41=column_reference();

            state._fsp--;


             value = column_reference41; 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "grouping_column_reference"



    // $ANTLR start "grouping_column_reference_list"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:569:1: grouping_column_reference_list returns [ArrayList<ColumnReference> value] : a= column_reference ( COMMA b= column_reference )* ;
    public final ArrayList<ColumnReference> grouping_column_reference_list() throws RecognitionException {
        ArrayList<ColumnReference> value = null;


        ColumnReference a =null;

        ColumnReference b =null;



          value = new ArrayList<ColumnReference>();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:573:3: (a= column_reference ( COMMA b= column_reference )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:573:5: a= column_reference ( COMMA b= column_reference )*
            {
            pushFollow(FOLLOW_column_reference_in_grouping_column_reference_list1541);
            a=column_reference();

            state._fsp--;


             value.add(a); 

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:574:5: ( COMMA b= column_reference )*
            loop37:
            do {
                int alt37=2;
                int LA37_0 = input.LA(1);

                if ( (LA37_0==COMMA) ) {
                    alt37=1;
                }


                switch (alt37) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:574:6: COMMA b= column_reference
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_grouping_column_reference_list1550); 

            	    pushFollow(FOLLOW_column_reference_in_grouping_column_reference_list1554);
            	    b=column_reference();

            	    state._fsp--;


            	     value.add(b); 

            	    }
            	    break;

            	default :
            	    break loop37;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "grouping_column_reference_list"



    // $ANTLR start "joined_table"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:577:1: joined_table returns [TablePrimary value] : ( ( join_type )? JOIN table_reference join_specification )+ ;
    public final TablePrimary joined_table() throws RecognitionException {
        TablePrimary value = null;


        int join_type42 =0;

        BooleanValueExpression join_specification43 =null;

        TablePrimary table_reference44 =null;



          int joinType = JoinOperator.JOIN; // by default

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:581:3: ( ( ( join_type )? JOIN table_reference join_specification )+ )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:581:5: ( ( join_type )? JOIN table_reference join_specification )+
            {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:581:5: ( ( join_type )? JOIN table_reference join_specification )+
            int cnt39=0;
            loop39:
            do {
                int alt39=2;
                int LA39_0 = input.LA(1);

                if ( (LA39_0==FULL||LA39_0==INNER||(LA39_0 >= JOIN && LA39_0 <= LEFT)||LA39_0==RIGHT) ) {
                    alt39=1;
                }


                switch (alt39) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:581:6: ( join_type )? JOIN table_reference join_specification
            	    {
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:581:6: ( join_type )?
            	    int alt38=2;
            	    int LA38_0 = input.LA(1);

            	    if ( (LA38_0==FULL||LA38_0==INNER||LA38_0==LEFT||LA38_0==RIGHT) ) {
            	        alt38=1;
            	    }
            	    switch (alt38) {
            	        case 1 :
            	            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:581:7: join_type
            	            {
            	            pushFollow(FOLLOW_join_type_in_joined_table1584);
            	            join_type42=join_type();

            	            state._fsp--;


            	             joinType = join_type42; 

            	            }
            	            break;

            	    }


            	    match(input,JOIN,FOLLOW_JOIN_in_joined_table1590); 

            	    pushFollow(FOLLOW_table_reference_in_joined_table1592);
            	    table_reference44=table_reference();

            	    state._fsp--;


            	    pushFollow(FOLLOW_join_specification_in_joined_table1594);
            	    join_specification43=join_specification();

            	    state._fsp--;



            	          JoinOperator joinOp = new JoinOperator(joinType);
            	          joinOp.copy(join_specification43.getSpecification());
            	          relationStack.push(joinOp);
            	          value = table_reference44;
            	        

            	    }
            	    break;

            	default :
            	    if ( cnt39 >= 1 ) break loop39;
                        EarlyExitException eee =
                            new EarlyExitException(39, input);
                        throw eee;
                }
                cnt39++;
            } while (true);


            }

        }
        catch (Exception e) {

                 // Does nothing.
              
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "joined_table"



    // $ANTLR start "join_type"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:592:1: join_type returns [int value] : ( INNER | outer_join_type ( OUTER )? );
    public final int join_type() throws RecognitionException {
        int value = 0;


        int outer_join_type45 =0;



          boolean bHasOuter = false;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:596:3: ( INNER | outer_join_type ( OUTER )? )
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==INNER) ) {
                alt41=1;
            }
            else if ( (LA41_0==FULL||LA41_0==LEFT||LA41_0==RIGHT) ) {
                alt41=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 41, 0, input);

                throw nvae;

            }
            switch (alt41) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:596:5: INNER
                    {
                    match(input,INNER,FOLLOW_INNER_in_join_type1630); 

                     value = JoinOperator.INNER_JOIN; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:597:5: outer_join_type ( OUTER )?
                    {
                    pushFollow(FOLLOW_outer_join_type_in_join_type1638);
                    outer_join_type45=outer_join_type();

                    state._fsp--;


                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:597:21: ( OUTER )?
                    int alt40=2;
                    int LA40_0 = input.LA(1);

                    if ( (LA40_0==OUTER) ) {
                        alt40=1;
                    }
                    switch (alt40) {
                        case 1 :
                            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:597:22: OUTER
                            {
                            match(input,OUTER,FOLLOW_OUTER_in_join_type1641); 

                             bHasOuter = true; 

                            }
                            break;

                    }



                          if (bHasOuter) {
                            switch(outer_join_type45) {
                              case JoinOperator.LEFT_JOIN: value = JoinOperator.LEFT_OUTER_JOIN; break;
                              case JoinOperator.RIGHT_JOIN: value = JoinOperator.RIGHT_OUTER_JOIN; break;
                              case JoinOperator.FULL_JOIN: value = JoinOperator.FULL_OUTER_JOIN; break;
                            }
                          }
                          else {
                            value = outer_join_type45;
                          }
                        

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "join_type"



    // $ANTLR start "outer_join_type"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:611:1: outer_join_type returns [int value] : ( LEFT | RIGHT | FULL );
    public final int outer_join_type() throws RecognitionException {
        int value = 0;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:612:3: ( LEFT | RIGHT | FULL )
            int alt42=3;
            switch ( input.LA(1) ) {
            case LEFT:
                {
                alt42=1;
                }
                break;
            case RIGHT:
                {
                alt42=2;
                }
                break;
            case FULL:
                {
                alt42=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 42, 0, input);

                throw nvae;

            }

            switch (alt42) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:612:5: LEFT
                    {
                    match(input,LEFT,FOLLOW_LEFT_in_outer_join_type1666); 

                     value = JoinOperator.LEFT_JOIN; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:613:5: RIGHT
                    {
                    match(input,RIGHT,FOLLOW_RIGHT_in_outer_join_type1674); 

                     value = JoinOperator.RIGHT_JOIN; 

                    }
                    break;
                case 3 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:614:5: FULL
                    {
                    match(input,FULL,FOLLOW_FULL_in_outer_join_type1682); 

                     value = JoinOperator.FULL_JOIN; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "outer_join_type"



    // $ANTLR start "join_specification"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:617:1: join_specification returns [BooleanValueExpression value] : ( join_condition | named_columns_join );
    public final BooleanValueExpression join_specification() throws RecognitionException {
        BooleanValueExpression value = null;


        BooleanValueExpression join_condition46 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:618:3: ( join_condition | named_columns_join )
            int alt43=2;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==ON) ) {
                alt43=1;
            }
            else if ( (LA43_0==USING) ) {
                alt43=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 43, 0, input);

                throw nvae;

            }
            switch (alt43) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:618:5: join_condition
                    {
                    pushFollow(FOLLOW_join_condition_in_join_specification1701);
                    join_condition46=join_condition();

                    state._fsp--;


                     value = join_condition46; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:619:5: named_columns_join
                    {
                    pushFollow(FOLLOW_named_columns_join_in_join_specification1709);
                    named_columns_join();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "join_specification"



    // $ANTLR start "join_condition"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:622:1: join_condition returns [BooleanValueExpression value] : ON search_condition ;
    public final BooleanValueExpression join_condition() throws RecognitionException {
        BooleanValueExpression value = null;


        BooleanValueExpression search_condition47 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:623:3: ( ON search_condition )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:623:5: ON search_condition
            {
            match(input,ON,FOLLOW_ON_in_join_condition1726); 

            pushFollow(FOLLOW_search_condition_in_join_condition1728);
            search_condition47=search_condition();

            state._fsp--;



                  value = search_condition47;
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "join_condition"



    // $ANTLR start "named_columns_join"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:628:1: named_columns_join : USING LPAREN join_column_list RPAREN ;
    public final void named_columns_join() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:629:3: ( USING LPAREN join_column_list RPAREN )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:629:5: USING LPAREN join_column_list RPAREN
            {
            match(input,USING,FOLLOW_USING_in_named_columns_join1743); 

            match(input,LPAREN,FOLLOW_LPAREN_in_named_columns_join1745); 

            pushFollow(FOLLOW_join_column_list_in_named_columns_join1747);
            join_column_list();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_named_columns_join1749); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "named_columns_join"



    // $ANTLR start "join_column_list"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:632:1: join_column_list : column_name ( COMMA column_name )* ;
    public final void join_column_list() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:633:3: ( column_name ( COMMA column_name )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:633:5: column_name ( COMMA column_name )*
            {
            pushFollow(FOLLOW_column_name_in_join_column_list1762);
            column_name();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:633:17: ( COMMA column_name )*
            loop44:
            do {
                int alt44=2;
                int LA44_0 = input.LA(1);

                if ( (LA44_0==COMMA) ) {
                    alt44=1;
                }


                switch (alt44) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:633:18: COMMA column_name
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_join_column_list1765); 

            	    pushFollow(FOLLOW_column_name_in_join_column_list1767);
            	    column_name();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop44;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "join_column_list"



    // $ANTLR start "table_primary"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:637:1: table_primary returns [TablePrimary value] : ( table_name ( ( AS )? alias_name )? | derived_table ( AS )? alias_name );
    public final TablePrimary table_primary() throws RecognitionException {
        TablePrimary value = null;


        TablePrimary table_name48 =null;

        String alias_name49 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:638:3: ( table_name ( ( AS )? alias_name )? | derived_table ( AS )? alias_name )
            int alt48=2;
            int LA48_0 = input.LA(1);

            if ( (LA48_0==STRING_WITH_QUOTE_DOUBLE||LA48_0==VARNAME) ) {
                alt48=1;
            }
            else if ( (LA48_0==LPAREN) ) {
                alt48=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 48, 0, input);

                throw nvae;

            }
            switch (alt48) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:638:5: table_name ( ( AS )? alias_name )?
                    {
                    pushFollow(FOLLOW_table_name_in_table_primary1787);
                    table_name48=table_name();

                    state._fsp--;


                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:639:5: ( ( AS )? alias_name )?
                    int alt46=2;
                    int LA46_0 = input.LA(1);

                    if ( (LA46_0==AS||LA46_0==STRING_WITH_QUOTE_DOUBLE||LA46_0==VARNAME) ) {
                        alt46=1;
                    }
                    switch (alt46) {
                        case 1 :
                            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:639:6: ( AS )? alias_name
                            {
                            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:639:6: ( AS )?
                            int alt45=2;
                            int LA45_0 = input.LA(1);

                            if ( (LA45_0==AS) ) {
                                alt45=1;
                            }
                            switch (alt45) {
                                case 1 :
                                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:639:6: AS
                                    {
                                    match(input,AS,FOLLOW_AS_in_table_primary1794); 

                                    }
                                    break;

                            }


                            pushFollow(FOLLOW_alias_name_in_table_primary1797);
                            alias_name49=alias_name();

                            state._fsp--;


                            }
                            break;

                    }



                          value = table_name48; 
                          value.setAlias(alias_name49);
                          Relation table = new Relation(value);      
                          relationStack.push(table);
                        

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:645:5: derived_table ( AS )? alias_name
                    {
                    pushFollow(FOLLOW_derived_table_in_table_primary1807);
                    derived_table();

                    state._fsp--;


                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:646:5: ( AS )?
                    int alt47=2;
                    int LA47_0 = input.LA(1);

                    if ( (LA47_0==AS) ) {
                        alt47=1;
                    }
                    switch (alt47) {
                        case 1 :
                            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:646:5: AS
                            {
                            match(input,AS,FOLLOW_AS_in_table_primary1813); 

                            }
                            break;

                    }


                    pushFollow(FOLLOW_alias_name_in_table_primary1816);
                    alias_name();

                    state._fsp--;



                          value = null;
                        

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "table_primary"



    // $ANTLR start "table_name"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:651:1: table_name returns [TablePrimary value] : ( schema_name PERIOD )? table_identifier ;
    public final TablePrimary table_name() throws RecognitionException {
        TablePrimary value = null;


        String schema_name50 =null;

        String table_identifier51 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:652:3: ( ( schema_name PERIOD )? table_identifier )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:652:5: ( schema_name PERIOD )? table_identifier
            {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:652:5: ( schema_name PERIOD )?
            int alt49=2;
            int LA49_0 = input.LA(1);

            if ( (LA49_0==VARNAME) ) {
                int LA49_1 = input.LA(2);

                if ( (LA49_1==PERIOD) ) {
                    alt49=1;
                }
            }
            else if ( (LA49_0==STRING_WITH_QUOTE_DOUBLE) ) {
                int LA49_2 = input.LA(2);

                if ( (LA49_2==PERIOD) ) {
                    alt49=1;
                }
            }
            switch (alt49) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:652:6: schema_name PERIOD
                    {
                    pushFollow(FOLLOW_schema_name_in_table_name1838);
                    schema_name50=schema_name();

                    state._fsp--;


                    match(input,PERIOD,FOLLOW_PERIOD_in_table_name1840); 

                    }
                    break;

            }


            pushFollow(FOLLOW_table_identifier_in_table_name1844);
            table_identifier51=table_identifier();

            state._fsp--;



                  String schema = schema_name50;
                  if (metadata != null) {
            	      if (schema != null && schema != "") {
            	        value = metadata.getTable(schema, table_identifier51);
            	      }
            	      else {
            	        value = metadata.getTable(table_identifier51);
            	      }
                  }
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "table_name"



    // $ANTLR start "alias_name"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:665:1: alias_name returns [String value] : identifier ;
    public final String alias_name() throws RecognitionException {
        String value = null;


        String identifier52 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:666:3: ( identifier )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:666:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_alias_name1865);
            identifier52=identifier();

            state._fsp--;


             value = identifier52; 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "alias_name"



    // $ANTLR start "derived_table"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:669:1: derived_table : table_subquery ;
    public final void derived_table() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:670:3: ( table_subquery )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:670:5: table_subquery
            {
            pushFollow(FOLLOW_table_subquery_in_derived_table1881);
            table_subquery();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "derived_table"



    // $ANTLR start "table_identifier"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:673:1: table_identifier returns [String value] : identifier ;
    public final String table_identifier() throws RecognitionException {
        String value = null;


        String identifier53 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:674:3: ( identifier )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:674:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_table_identifier1902);
            identifier53=identifier();

            state._fsp--;


             value = identifier53; 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "table_identifier"



    // $ANTLR start "schema_name"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:677:1: schema_name returns [String value] : identifier ;
    public final String schema_name() throws RecognitionException {
        String value = null;


        String identifier54 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:678:3: ( identifier )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:678:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_schema_name1923);
            identifier54=identifier();

            state._fsp--;


             value = identifier54; 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "schema_name"



    // $ANTLR start "column_name"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:681:1: column_name returns [String value] : identifier ;
    public final String column_name() throws RecognitionException {
        String value = null;


        String identifier55 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:682:3: ( identifier )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:682:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_column_name1946);
            identifier55=identifier();

            state._fsp--;


             value = identifier55; 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "column_name"



    // $ANTLR start "identifier"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:685:1: identifier returns [String value] : (t= regular_identifier |t= delimited_identifier ) ;
    public final String identifier() throws RecognitionException {
        String value = null;


        String t =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:686:3: ( (t= regular_identifier |t= delimited_identifier ) )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:686:5: (t= regular_identifier |t= delimited_identifier )
            {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:686:5: (t= regular_identifier |t= delimited_identifier )
            int alt50=2;
            int LA50_0 = input.LA(1);

            if ( (LA50_0==VARNAME) ) {
                alt50=1;
            }
            else if ( (LA50_0==STRING_WITH_QUOTE_DOUBLE) ) {
                alt50=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 50, 0, input);

                throw nvae;

            }
            switch (alt50) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:686:6: t= regular_identifier
                    {
                    pushFollow(FOLLOW_regular_identifier_in_identifier1970);
                    t=regular_identifier();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:686:29: t= delimited_identifier
                    {
                    pushFollow(FOLLOW_delimited_identifier_in_identifier1976);
                    t=delimited_identifier();

                    state._fsp--;


                    }
                    break;

            }


             value = t; 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "identifier"



    // $ANTLR start "regular_identifier"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:689:1: regular_identifier returns [String value] : VARNAME ;
    public final String regular_identifier() throws RecognitionException {
        String value = null;


        Token VARNAME56=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:690:3: ( VARNAME )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:690:5: VARNAME
            {
            VARNAME56=(Token)match(input,VARNAME,FOLLOW_VARNAME_in_regular_identifier1996); 

             value = (VARNAME56!=null?VARNAME56.getText():null); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "regular_identifier"



    // $ANTLR start "delimited_identifier"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:693:1: delimited_identifier returns [String value] : STRING_WITH_QUOTE_DOUBLE ;
    public final String delimited_identifier() throws RecognitionException {
        String value = null;


        Token STRING_WITH_QUOTE_DOUBLE57=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:694:3: ( STRING_WITH_QUOTE_DOUBLE )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:694:5: STRING_WITH_QUOTE_DOUBLE
            {
            STRING_WITH_QUOTE_DOUBLE57=(Token)match(input,STRING_WITH_QUOTE_DOUBLE,FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_delimited_identifier2015); 

             
                  value = (STRING_WITH_QUOTE_DOUBLE57!=null?STRING_WITH_QUOTE_DOUBLE57.getText():null);
                  value = value.substring(1, value.length()-1);
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "delimited_identifier"



    // $ANTLR start "general_literal"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:700:1: general_literal returns [Literal value] : ( string_literal | boolean_literal );
    public final Literal general_literal() throws RecognitionException {
        Literal value = null;


        StringLiteral string_literal58 =null;

        BooleanLiteral boolean_literal59 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:701:3: ( string_literal | boolean_literal )
            int alt51=2;
            int LA51_0 = input.LA(1);

            if ( (LA51_0==STRING_WITH_QUOTE) ) {
                alt51=1;
            }
            else if ( (LA51_0==FALSE||LA51_0==TRUE) ) {
                alt51=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 51, 0, input);

                throw nvae;

            }
            switch (alt51) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:701:5: string_literal
                    {
                    pushFollow(FOLLOW_string_literal_in_general_literal2034);
                    string_literal58=string_literal();

                    state._fsp--;


                     value = string_literal58; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:702:5: boolean_literal
                    {
                    pushFollow(FOLLOW_boolean_literal_in_general_literal2042);
                    boolean_literal59=boolean_literal();

                    state._fsp--;


                     value = boolean_literal59; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "general_literal"



    // $ANTLR start "string_literal"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:705:1: string_literal returns [StringLiteral value] : STRING_WITH_QUOTE ;
    public final StringLiteral string_literal() throws RecognitionException {
        StringLiteral value = null;


        Token STRING_WITH_QUOTE60=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:706:3: ( STRING_WITH_QUOTE )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:706:5: STRING_WITH_QUOTE
            {
            STRING_WITH_QUOTE60=(Token)match(input,STRING_WITH_QUOTE,FOLLOW_STRING_WITH_QUOTE_in_string_literal2061); 

             value = new StringLiteral((STRING_WITH_QUOTE60!=null?STRING_WITH_QUOTE60.getText():null)); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "string_literal"



    // $ANTLR start "boolean_literal"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:709:1: boolean_literal returns [BooleanLiteral value] : (t= TRUE |t= FALSE ) ;
    public final BooleanLiteral boolean_literal() throws RecognitionException {
        BooleanLiteral value = null;


        Token t=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:710:3: ( (t= TRUE |t= FALSE ) )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:710:5: (t= TRUE |t= FALSE )
            {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:710:5: (t= TRUE |t= FALSE )
            int alt52=2;
            int LA52_0 = input.LA(1);

            if ( (LA52_0==TRUE) ) {
                alt52=1;
            }
            else if ( (LA52_0==FALSE) ) {
                alt52=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 52, 0, input);

                throw nvae;

            }
            switch (alt52) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:710:6: t= TRUE
                    {
                    t=(Token)match(input,TRUE,FOLLOW_TRUE_in_boolean_literal2083); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:710:15: t= FALSE
                    {
                    t=(Token)match(input,FALSE,FOLLOW_FALSE_in_boolean_literal2089); 

                    }
                    break;

            }


             value = new BooleanLiteral(Boolean.getBoolean((t!=null?t.getText():null))); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "boolean_literal"



    // $ANTLR start "numeric_literal"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:713:1: numeric_literal returns [NumericLiteral value] : ( numeric_literal_unsigned | numeric_literal_positive | numeric_literal_negative );
    public final NumericLiteral numeric_literal() throws RecognitionException {
        NumericLiteral value = null;


        NumericLiteral numeric_literal_unsigned61 =null;

        NumericLiteral numeric_literal_positive62 =null;

        NumericLiteral numeric_literal_negative63 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:714:3: ( numeric_literal_unsigned | numeric_literal_positive | numeric_literal_negative )
            int alt53=3;
            switch ( input.LA(1) ) {
            case DECIMAL:
            case INTEGER:
                {
                alt53=1;
                }
                break;
            case DECIMAL_POSITIVE:
            case INTEGER_POSITIVE:
                {
                alt53=2;
                }
                break;
            case DECIMAL_NEGATIVE:
            case INTEGER_NEGATIVE:
                {
                alt53=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 53, 0, input);

                throw nvae;

            }

            switch (alt53) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:714:5: numeric_literal_unsigned
                    {
                    pushFollow(FOLLOW_numeric_literal_unsigned_in_numeric_literal2109);
                    numeric_literal_unsigned61=numeric_literal_unsigned();

                    state._fsp--;


                     value = numeric_literal_unsigned61; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:715:5: numeric_literal_positive
                    {
                    pushFollow(FOLLOW_numeric_literal_positive_in_numeric_literal2117);
                    numeric_literal_positive62=numeric_literal_positive();

                    state._fsp--;


                     value = numeric_literal_positive62; 

                    }
                    break;
                case 3 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:716:5: numeric_literal_negative
                    {
                    pushFollow(FOLLOW_numeric_literal_negative_in_numeric_literal2125);
                    numeric_literal_negative63=numeric_literal_negative();

                    state._fsp--;


                     value = numeric_literal_negative63; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "numeric_literal"



    // $ANTLR start "numeric_literal_unsigned"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:719:1: numeric_literal_unsigned returns [NumericLiteral value] : ( INTEGER | DECIMAL );
    public final NumericLiteral numeric_literal_unsigned() throws RecognitionException {
        NumericLiteral value = null;


        Token INTEGER64=null;
        Token DECIMAL65=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:720:3: ( INTEGER | DECIMAL )
            int alt54=2;
            int LA54_0 = input.LA(1);

            if ( (LA54_0==INTEGER) ) {
                alt54=1;
            }
            else if ( (LA54_0==DECIMAL) ) {
                alt54=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 54, 0, input);

                throw nvae;

            }
            switch (alt54) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:720:5: INTEGER
                    {
                    INTEGER64=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_numeric_literal_unsigned2144); 

                     value = new IntegerLiteral((INTEGER64!=null?INTEGER64.getText():null)); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:721:5: DECIMAL
                    {
                    DECIMAL65=(Token)match(input,DECIMAL,FOLLOW_DECIMAL_in_numeric_literal_unsigned2152); 

                     value = new DecimalLiteral((DECIMAL65!=null?DECIMAL65.getText():null)); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "numeric_literal_unsigned"



    // $ANTLR start "numeric_literal_positive"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:724:1: numeric_literal_positive returns [NumericLiteral value] : ( INTEGER_POSITIVE | DECIMAL_POSITIVE );
    public final NumericLiteral numeric_literal_positive() throws RecognitionException {
        NumericLiteral value = null;


        Token INTEGER_POSITIVE66=null;
        Token DECIMAL_POSITIVE67=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:725:3: ( INTEGER_POSITIVE | DECIMAL_POSITIVE )
            int alt55=2;
            int LA55_0 = input.LA(1);

            if ( (LA55_0==INTEGER_POSITIVE) ) {
                alt55=1;
            }
            else if ( (LA55_0==DECIMAL_POSITIVE) ) {
                alt55=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 55, 0, input);

                throw nvae;

            }
            switch (alt55) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:725:5: INTEGER_POSITIVE
                    {
                    INTEGER_POSITIVE66=(Token)match(input,INTEGER_POSITIVE,FOLLOW_INTEGER_POSITIVE_in_numeric_literal_positive2171); 

                     value = new IntegerLiteral((INTEGER_POSITIVE66!=null?INTEGER_POSITIVE66.getText():null)); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:726:5: DECIMAL_POSITIVE
                    {
                    DECIMAL_POSITIVE67=(Token)match(input,DECIMAL_POSITIVE,FOLLOW_DECIMAL_POSITIVE_in_numeric_literal_positive2179); 

                     value = new DecimalLiteral((DECIMAL_POSITIVE67!=null?DECIMAL_POSITIVE67.getText():null)); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "numeric_literal_positive"



    // $ANTLR start "numeric_literal_negative"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:729:1: numeric_literal_negative returns [NumericLiteral value] : ( INTEGER_NEGATIVE | DECIMAL_NEGATIVE );
    public final NumericLiteral numeric_literal_negative() throws RecognitionException {
        NumericLiteral value = null;


        Token INTEGER_NEGATIVE68=null;
        Token DECIMAL_NEGATIVE69=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:730:3: ( INTEGER_NEGATIVE | DECIMAL_NEGATIVE )
            int alt56=2;
            int LA56_0 = input.LA(1);

            if ( (LA56_0==INTEGER_NEGATIVE) ) {
                alt56=1;
            }
            else if ( (LA56_0==DECIMAL_NEGATIVE) ) {
                alt56=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 56, 0, input);

                throw nvae;

            }
            switch (alt56) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:730:5: INTEGER_NEGATIVE
                    {
                    INTEGER_NEGATIVE68=(Token)match(input,INTEGER_NEGATIVE,FOLLOW_INTEGER_NEGATIVE_in_numeric_literal_negative2200); 

                     value = new IntegerLiteral((INTEGER_NEGATIVE68!=null?INTEGER_NEGATIVE68.getText():null)); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:731:5: DECIMAL_NEGATIVE
                    {
                    DECIMAL_NEGATIVE69=(Token)match(input,DECIMAL_NEGATIVE,FOLLOW_DECIMAL_NEGATIVE_in_numeric_literal_negative2208); 

                     value = new DecimalLiteral((DECIMAL_NEGATIVE69!=null?DECIMAL_NEGATIVE69.getText():null)); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "numeric_literal_negative"



    // $ANTLR start "truth_value"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:734:1: truth_value returns [boolean value] : (t= TRUE |t= FALSE ) ;
    public final boolean truth_value() throws RecognitionException {
        boolean value = false;


        Token t=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:735:3: ( (t= TRUE |t= FALSE ) )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:735:5: (t= TRUE |t= FALSE )
            {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:735:5: (t= TRUE |t= FALSE )
            int alt57=2;
            int LA57_0 = input.LA(1);

            if ( (LA57_0==TRUE) ) {
                alt57=1;
            }
            else if ( (LA57_0==FALSE) ) {
                alt57=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 57, 0, input);

                throw nvae;

            }
            switch (alt57) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:735:6: t= TRUE
                    {
                    t=(Token)match(input,TRUE,FOLLOW_TRUE_in_truth_value2232); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:735:15: t= FALSE
                    {
                    t=(Token)match(input,FALSE,FOLLOW_FALSE_in_truth_value2238); 

                    }
                    break;

            }


             value = Boolean.getBoolean((t!=null?t.getText():null)); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "truth_value"

    // Delegated rules


 

    public static final BitSet FOLLOW_query_in_parse40 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_parse42 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_query_specification_in_query70 = new BitSet(new long[]{0x0000000000000002L,0x0000000000008000L});
    public static final BitSet FOLLOW_UNION_in_query79 = new BitSet(new long[]{0x0000000008000010L,0x0000000000000020L});
    public static final BitSet FOLLOW_set_quantifier_in_query81 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_query_specification_in_query86 = new BitSet(new long[]{0x0000000000000002L,0x0000000000008000L});
    public static final BitSet FOLLOW_SELECT_in_query_specification114 = new BitSet(new long[]{0x001A000108405210L,0x0000000000020D00L});
    public static final BitSet FOLLOW_set_quantifier_in_query_specification116 = new BitSet(new long[]{0x001A000100405200L,0x0000000000020D00L});
    public static final BitSet FOLLOW_select_list_in_query_specification119 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_table_expression_in_query_specification121 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALL_in_set_quantifier142 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DISTINCT_in_set_quantifier150 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASTERISK_in_select_list176 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_select_sublist_in_select_list186 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_select_list191 = new BitSet(new long[]{0x001A000100404200L,0x0000000000020D00L});
    public static final BitSet FOLLOW_select_sublist_in_select_list195 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_qualified_asterisk_in_select_sublist218 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_derived_column_in_select_sublist226 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_identifier_in_qualified_asterisk243 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_PERIOD_in_qualified_asterisk245 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_ASTERISK_in_qualified_asterisk247 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expression_in_derived_column271 = new BitSet(new long[]{0x0000000000000802L,0x0000000000020400L});
    public static final BitSet FOLLOW_AS_in_derived_column274 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_alias_name_in_derived_column277 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numeric_value_expression_in_value_expression301 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_string_value_expression_in_value_expression309 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_reference_value_expression_in_value_expression317 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_collection_value_expression_in_value_expression325 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_numeric_value_expression349 = new BitSet(new long[]{0x00001C0003800000L,0x0000000000020400L});
    public static final BitSet FOLLOW_numeric_operation_in_numeric_value_expression351 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_numeric_value_expression353 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_numeric_operation368 = new BitSet(new long[]{0x4020000000000002L});
    public static final BitSet FOLLOW_PLUS_in_numeric_operation386 = new BitSet(new long[]{0x00001C0003800000L,0x0000000000020400L});
    public static final BitSet FOLLOW_MINUS_in_numeric_operation390 = new BitSet(new long[]{0x00001C0003800000L,0x0000000000020400L});
    public static final BitSet FOLLOW_term_in_numeric_operation403 = new BitSet(new long[]{0x4020000000000002L});
    public static final BitSet FOLLOW_factor_in_term425 = new BitSet(new long[]{0x0000000000001002L,0x0000000000000080L});
    public static final BitSet FOLLOW_ASTERISK_in_term445 = new BitSet(new long[]{0x00001C0003800000L,0x0000000000020400L});
    public static final BitSet FOLLOW_SOLIDUS_in_term449 = new BitSet(new long[]{0x00001C0003800000L,0x0000000000020400L});
    public static final BitSet FOLLOW_factor_in_term463 = new BitSet(new long[]{0x0000000000001002L,0x0000000000000080L});
    public static final BitSet FOLLOW_column_reference_in_factor491 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numeric_literal_in_factor499 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_string_value_expression542 = new BitSet(new long[]{0x0000000400000000L,0x0000000000022600L});
    public static final BitSet FOLLOW_concatenation_in_string_value_expression544 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_string_value_expression546 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_character_factor_in_concatenation565 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_CONCATENATION_in_concatenation577 = new BitSet(new long[]{0x0000000400000000L,0x0000000000022600L});
    public static final BitSet FOLLOW_character_factor_in_concatenation590 = new BitSet(new long[]{0x0000000000200002L});
    public static final BitSet FOLLOW_column_reference_in_character_factor611 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_general_literal_in_character_factor619 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_reference_value_expression643 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_identifier_in_column_reference665 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_PERIOD_in_column_reference667 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_column_name_in_column_reference671 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_function_specification_in_collection_value_expression699 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNT_in_set_function_specification714 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_set_function_specification716 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_ASTERISK_in_set_function_specification718 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_set_function_specification720 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_general_set_function_in_set_function_specification728 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_function_op_in_general_set_function743 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_general_set_function745 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_column_reference_in_general_set_function747 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_general_set_function749 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AVG_in_set_function_op773 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MAX_in_set_function_op779 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MIN_in_set_function_op785 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUM_in_set_function_op791 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EVERY_in_set_function_op797 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ANY_in_set_function_op803 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOME_in_set_function_op809 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNT_in_set_function_op815 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_row_value_expression837 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expression_in_row_value_expression845 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numeric_literal_in_literal864 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_general_literal_in_literal872 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_from_clause_in_table_expression891 = new BitSet(new long[]{0x0000004000000002L,0x0000000000040000L});
    public static final BitSet FOLLOW_where_clause_in_table_expression900 = new BitSet(new long[]{0x0000004000000002L});
    public static final BitSet FOLLOW_group_by_clause_in_table_expression912 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_from_clause935 = new BitSet(new long[]{0x0002000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_table_reference_list_in_from_clause937 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_reference_in_table_reference_list967 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_table_reference_list984 = new BitSet(new long[]{0x0002000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_table_reference_in_table_reference_list988 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_table_primary_in_table_reference1015 = new BitSet(new long[]{0x0000C21000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_joined_table_in_table_reference1024 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHERE_in_where_clause1046 = new BitSet(new long[]{0x001A1C0503C04200L,0x0000000000022F00L});
    public static final BitSet FOLLOW_search_condition_in_where_clause1048 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_value_expression_in_search_condition1067 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_term_in_boolean_value_expression1093 = new BitSet(new long[]{0x0200000000000002L});
    public static final BitSet FOLLOW_OR_in_boolean_value_expression1096 = new BitSet(new long[]{0x001A1C0503C04200L,0x0000000000022F00L});
    public static final BitSet FOLLOW_boolean_term_in_boolean_value_expression1100 = new BitSet(new long[]{0x0200000000000002L});
    public static final BitSet FOLLOW_boolean_factor_in_boolean_term1119 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_AND_in_boolean_term1122 = new BitSet(new long[]{0x001A1C0503C04200L,0x0000000000022F00L});
    public static final BitSet FOLLOW_boolean_factor_in_boolean_term1126 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_predicate_in_boolean_factor1142 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparison_predicate_in_predicate1162 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_null_predicate_in_predicate1170 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_in_predicate_in_predicate1176 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_row_value_expression_in_comparison_predicate1197 = new BitSet(new long[]{0x0001002080000000L});
    public static final BitSet FOLLOW_comp_op_in_comparison_predicate1199 = new BitSet(new long[]{0x001A1C0503C04200L,0x0000000000022F00L});
    public static final BitSet FOLLOW_row_value_expression_in_comparison_predicate1203 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_comp_op1222 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_comp_op1230 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_GREATER_in_comp_op1232 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_comp_op1240 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_comp_op1248 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_comp_op1256 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_EQUALS_in_comp_op1258 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_comp_op1266 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_EQUALS_in_comp_op1268 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_null_predicate1283 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_IS_in_null_predicate1285 = new BitSet(new long[]{0x00C0000000000000L});
    public static final BitSet FOLLOW_NOT_in_null_predicate1288 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_NULL_in_null_predicate1292 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_in_predicate1305 = new BitSet(new long[]{0x0040010000000000L});
    public static final BitSet FOLLOW_NOT_in_in_predicate1308 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_IN_in_in_predicate1312 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_in_predicate_value_in_in_predicate1314 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_subquery_in_in_predicate_value1329 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_in_predicate_value1335 = new BitSet(new long[]{0x001A1C0503C04200L,0x0000000000022F00L});
    public static final BitSet FOLLOW_in_value_list_in_in_predicate_value1337 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_in_predicate_value1339 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_subquery_in_table_subquery1352 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_subquery1365 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_query_in_subquery1367 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_subquery1369 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_row_value_expression_in_in_value_list1384 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_in_value_list1387 = new BitSet(new long[]{0x001A1C0503C04200L,0x0000000000022F00L});
    public static final BitSet FOLLOW_row_value_expression_in_in_value_list1389 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_GROUP_in_group_by_clause1408 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_BY_in_group_by_clause1410 = new BitSet(new long[]{0x0002000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_grouping_element_list_in_group_by_clause1412 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_grouping_element_in_grouping_element_list1438 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_grouping_element_list1448 = new BitSet(new long[]{0x0002000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_grouping_element_in_grouping_element_list1452 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_grouping_column_reference_in_grouping_element1480 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_grouping_element1488 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_grouping_column_reference_list_in_grouping_element1490 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_grouping_element1492 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference1513 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference_list1541 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_grouping_column_reference_list1550 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference_list1554 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_join_type_in_joined_table1584 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_JOIN_in_joined_table1590 = new BitSet(new long[]{0x0002000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_table_reference_in_joined_table1592 = new BitSet(new long[]{0x0100000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_join_specification_in_joined_table1594 = new BitSet(new long[]{0x0000C21000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_INNER_in_join_type1630 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_outer_join_type_in_join_type1638 = new BitSet(new long[]{0x0800000000000002L});
    public static final BitSet FOLLOW_OUTER_in_join_type1641 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_in_outer_join_type1666 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RIGHT_in_outer_join_type1674 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FULL_in_outer_join_type1682 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_join_condition_in_join_specification1701 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_named_columns_join_in_join_specification1709 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ON_in_join_condition1726 = new BitSet(new long[]{0x001A1C0503C04200L,0x0000000000022F00L});
    public static final BitSet FOLLOW_search_condition_in_join_condition1728 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_USING_in_named_columns_join1743 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_named_columns_join1745 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_join_column_list_in_named_columns_join1747 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_named_columns_join1749 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_name_in_join_column_list1762 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_join_column_list1765 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_column_name_in_join_column_list1767 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_table_name_in_table_primary1787 = new BitSet(new long[]{0x0000000000000802L,0x0000000000020400L});
    public static final BitSet FOLLOW_AS_in_table_primary1794 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_alias_name_in_table_primary1797 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_derived_table_in_table_primary1807 = new BitSet(new long[]{0x0000000000000800L,0x0000000000020400L});
    public static final BitSet FOLLOW_AS_in_table_primary1813 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_alias_name_in_table_primary1816 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_schema_name_in_table_name1838 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_PERIOD_in_table_name1840 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_table_identifier_in_table_name1844 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_alias_name1865 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_subquery_in_derived_table1881 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_table_identifier1902 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_schema_name1923 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_column_name1946 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_regular_identifier_in_identifier1970 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_delimited_identifier_in_identifier1976 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VARNAME_in_regular_identifier1996 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_delimited_identifier2015 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_string_literal_in_general_literal2034 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_literal_in_general_literal2042 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_WITH_QUOTE_in_string_literal2061 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_boolean_literal2083 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_boolean_literal2089 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numeric_literal_unsigned_in_numeric_literal2109 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numeric_literal_positive_in_numeric_literal2117 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numeric_literal_negative_in_numeric_literal2125 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_in_numeric_literal_unsigned2144 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_in_numeric_literal_unsigned2152 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_POSITIVE_in_numeric_literal_positive2171 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_POSITIVE_in_numeric_literal_positive2179 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_NEGATIVE_in_numeric_literal_negative2200 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_NEGATIVE_in_numeric_literal_negative2208 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_truth_value2232 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_truth_value2238 = new BitSet(new long[]{0x0000000000000002L});

}