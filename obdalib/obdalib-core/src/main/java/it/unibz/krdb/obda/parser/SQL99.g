grammar SQL99;

@header {
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
}

@lexer::header {
package it.unibz.krdb.obda.parser;
}

@members {
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
private Projection createProjection(ArrayList<DerivedColumn> columnList) {
  Projection prj = new Projection();
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
        
    if ((flag \% 2) == 1) {  // right child
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
}

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/

parse returns [QueryTree value]
  : query EOF {
      $value = $query.value;
    }
  ;
  
query returns [QueryTree value]
@init {
int quantifier = 0;
}
  : a=query_specification { 
      queryTree = $a.value; 
      $value = queryTree;
    }
    (UNION set_quantifier? b=query_specification {
      quantifier = $set_quantifier.value;
      SetUnion union = new SetUnion(quantifier);
          
      QueryTree parent = new QueryTree(union);      
      parent.attachLeft(queryTree);
      parent.attachRight($b.value);
       
      queryTree = parent.root();
      $value = queryTree;
    })*
  ;
  
query_specification returns [QueryTree value]
@init {
int quantifier = 0;
}
  : SELECT set_quantifier? select_list table_expression {
  
      TableExpression te = $table_expression.value;
      
      // Construct the projection
      ArrayList<TablePrimary> tableList = te.getFromClause();
      ArrayList<DerivedColumn> columnList = $select_list.value;
      Projection prj = createProjection(columnList);
            
      quantifier = $set_quantifier.value;
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
      $value = constructQueryTree(root);   
    }
  ;

/**
 * The types of quantifier are based on a particular ordering system.
 * Therefore, the integers given here are not arbitrary.
 * 
 * {@link Projection, SetUnion}
 */
set_quantifier returns [int value]
  : ALL { $value = 1; }
  | DISTINCT { $value = 2; }
  ;
  
select_list returns [ArrayList<DerivedColumn> value]
@init {
  $value = new ArrayList<DerivedColumn>();
}
  : a=select_sublist { $value.add($a.value); } (COMMA b=select_sublist { $value.add($b.value); })*
  ;
  
select_sublist returns [DerivedColumn value]
  : qualified_asterisk { $value = null; }
  | derived_column { $value = $derived_column.value; }
  ;
  
qualified_asterisk
  : table_identifier PERIOD ASTERISK
  ;
  
derived_column returns [DerivedColumn value]
@init {
  $value = new DerivedColumn();
}
  : value_expression (AS? alias_name)? {
      $value.setValueExpression($value_expression.value);
      String alias = $alias_name.value;
      if (alias != null) {
        $value.setAlias($alias_name.value);
      }
    }
  ;  
 
value_expression returns [AbstractValueExpression value]
  : numeric_value_expression { $value = $numeric_value_expression.value; }
  | string_value_expression { $value = $string_value_expression.value; }
  | reference_value_expression { $value = $reference_value_expression.value; }
  | collection_value_expression { $value = $collection_value_expression.value; }
  ;

numeric_value_expression returns [NumericValueExpression value]
@init {
  numericExp = new NumericValueExpression();
}
  : LPAREN numeric_operation RPAREN {
      $value = numericExp;
    }
  ;

numeric_operation
  : term 
    (
      (t=PLUS|t=MINUS) { numericExp.putSpecification($t.text); } 
       term
    )*
  ;

term
  : a=factor { numericExp.putSpecification($a.value); } 
    (
      (t=ASTERISK|t=SOLIDUS) { numericExp.putSpecification($t.text); }
       b=factor { numericExp.putSpecification($b.value); }
    )*
  ;
  
factor returns [Object value]
  : column_reference { $value = $column_reference.value; }
  | numeric_literal { $value = $numeric_literal.value; }
  ;

sign
  : PLUS
  | MINUS
  ;

string_value_expression returns [StringValueExpression value]
@init {
  stringExp = new StringValueExpression();
}
  : LPAREN concatenation RPAREN {
      $value = stringExp;
    }
  ;
  
concatenation
  : a=character_factor { stringExp.putSpecification($a.value); } (
      CONCATENATION { stringExp.putSpecification(StringValueExpression.CONCAT_OP); } 
      b=character_factor { stringExp.putSpecification($b.value); })+
  ;

character_factor returns [Object value]
  : column_reference { $value = $column_reference.value; }
  | general_literal { $value = $general_literal.value; }
  ;

reference_value_expression returns [ReferenceValueExpression value]
@init {
  referenceExp = new ReferenceValueExpression();
}
  : column_reference { 
      referenceExp.add($column_reference.value);
      $value = referenceExp;
    }
  ;

column_reference returns [ColumnReference value]
  : (t=table_identifier PERIOD)? column_name {
      String table = "";
      if (t != null) {
        table = $t.value;
      }
      $value = new ColumnReference(table, $column_name.value);
    }
  ;  
  
collection_value_expression returns [CollectionValueExpression value]
@init {
  collectionExp = new CollectionValueExpression();
}
  : set_function_specification { 
      $value = collectionExp;
    }
  ;

set_function_specification
  : COUNT LPAREN ASTERISK RPAREN {
      collectionExp.putSpecification($COUNT.text);
      collectionExp.putSpecification($ASTERISK.text);
    }
  | general_set_function
  ;
 
// Limitation: only accept one column reference as the parameter!
general_set_function
  : set_function_op LPAREN column_reference RPAREN {
      collectionExp.putSpecification($set_function_op.value);
      collectionExp.add($column_reference.value);
    }
  ;
  
set_function_op returns [String value]
  : (t=AVG | t=MAX | t=MIN | t=SUM | t=EVERY | t=ANY | t=SOME | t=COUNT) {
      $value = $t.text;
    }
  ;  

row_value_expression returns [IValueExpression value]
  : literal { $value = $literal.value; }
  | value_expression { $value = $value_expression.value; }
  ;

literal returns [Literal value]
  : numeric_literal { $value = $numeric_literal.value; }
  | general_literal { $value = $general_literal.value; }
  ;

table_expression returns [TableExpression value]
  : from_clause {
      $value = new TableExpression($from_clause.value);
    }
    (where_clause { $value.setWhereClause($where_clause.value); })? 
    (group_by_clause { $value.setGroupByClause($group_by_clause.value); })?
  ;
  
from_clause returns [ArrayList<TablePrimary> value]
  : FROM table_reference_list {
      $value = $table_reference_list.value;
    }
  ;  
  
table_reference_list returns [ArrayList<TablePrimary> value]
@init {
  $value = new ArrayList<TablePrimary>();
}
  : a=table_reference { $value.add($a.value); } 
    (
      COMMA b=table_reference {
        JoinOperator joinOp = new JoinOperator(JoinOperator.CROSS_JOIN);
        relationStack.push(joinOp);
        
        $value.add($b.value);
      })*    
  ;
  
table_reference returns [TablePrimary value]
  : table_primary { $value = $table_primary.value; }
    (joined_table { $value = $joined_table.value; })? 
  ;

where_clause returns [BooleanValueExpression value]
  : WHERE search_condition {
      $value = $search_condition.value;
    }
  ;

search_condition returns [BooleanValueExpression value]
  : boolean_value_expression {
      $value = $boolean_value_expression.value;
    }
  ;
  
boolean_value_expression returns [BooleanValueExpression value]
@init {
  booleanExp = new BooleanValueExpression();
}
  : boolean_term (OR { booleanExp.putSpecification(new OrOperator()); } boolean_term)* {
      $value = booleanExp;
    }
  ;
  
boolean_term
  : boolean_factor (AND { booleanExp.putSpecification(new AndOperator()); } boolean_factor)*
  ;

// Limitation: No support for parenthesis!
boolean_factor
  : predicate { booleanExp.putSpecification($predicate.value); }
  ;
 
predicate returns [IPredicate value]
  : comparison_predicate { $value = $comparison_predicate.value; }
//  | null_predicate
//  | in_predicate
  ;
  
comparison_predicate returns [ComparisonPredicate value]
  : a=row_value_expression comp_op b=row_value_expression {
      $value = new ComparisonPredicate($a.value, $b.value, $comp_op.value);
    }
  ;

comp_op returns [ComparisonPredicate.Operator value]
  : EQUALS { $value = ComparisonPredicate.Operator.EQ; }
  | LESS GREATER { $value = ComparisonPredicate.Operator.NE; }
  | LESS { $value = ComparisonPredicate.Operator.LT; }
  | GREATER { $value = ComparisonPredicate.Operator.GT; }
  | LESS EQUALS { $value = ComparisonPredicate.Operator.LE; }
  | GREATER EQUALS { $value = ComparisonPredicate.Operator.GE; }
  ;

null_predicate
  : column_reference IS (NOT)? NULL
  ;

in_predicate
  : column_reference (NOT)? IN in_predicate_value
  ;
  
in_predicate_value
  : table_subquery
  | LPAREN in_value_list RPAREN
  ;

table_subquery
  : subquery
  ;

subquery
  : LPAREN query RPAREN
  ;
  
in_value_list
  : row_value_expression (COMMA row_value_expression)*
  ;

group_by_clause returns [ArrayList<GroupingElement> value]
  : GROUP BY grouping_element_list {
      $value = $grouping_element_list.value;
    }
  ;

grouping_element_list returns [ArrayList<GroupingElement> value]
@init {
  $value = new ArrayList<GroupingElement>();
}
  : a=grouping_element { $value.add($a.value); } 
    (COMMA b=grouping_element { $value.add($b.value); })*
  ;
  
grouping_element returns [GroupingElement value]
@init {
  $value = new GroupingElement();
}
  : grouping_column_reference { $value.add($grouping_column_reference.value); }
  | LPAREN grouping_column_reference_list RPAREN { $value.update($grouping_column_reference_list.value); }
  ;
  
grouping_column_reference returns [ColumnReference value]
  : column_reference { $value = $column_reference.value; }
  ;  

grouping_column_reference_list returns [ArrayList<ColumnReference> value]
@init {
  $value = new ArrayList<ColumnReference>();
}
  : a=column_reference { $value.add($a.value); }
    (COMMA b=column_reference { $value.add($b.value); })*
  ;  

joined_table returns [TablePrimary value]
@init {
  int joinType = JoinOperator.JOIN; // by default
}
  : ((join_type { joinType = $join_type.value; })? JOIN table_reference join_specification {
      JoinOperator joinOp = new JoinOperator(joinType);
      joinOp.copy($join_specification.value.getSpecification());
      relationStack.push(joinOp);
      $value = $table_reference.value;
    })+
  ;
  catch [Exception e] {
     // Does nothing.
  }  

join_type returns [int value]
@init {
  boolean bHasOuter = false;
}
  : INNER { $value = JoinOperator.INNER_JOIN; }
  | outer_join_type (OUTER { bHasOuter = true; })? {
      if (bHasOuter) {
        switch($outer_join_type.value) {
          case JoinOperator.LEFT_JOIN: $value = JoinOperator.LEFT_OUTER_JOIN; break;
          case JoinOperator.RIGHT_JOIN: $value = JoinOperator.RIGHT_OUTER_JOIN; break;
          case JoinOperator.FULL_JOIN: $value = JoinOperator.FULL_OUTER_JOIN; break;
        }
      }
      else {
        $value = $outer_join_type.value;
      }
    }
  ;
  
outer_join_type returns [int value]
  : LEFT { $value = JoinOperator.LEFT_JOIN; }
  | RIGHT { $value = JoinOperator.RIGHT_JOIN; }
  | FULL { $value = JoinOperator.FULL_JOIN; }
  ;

join_specification returns [BooleanValueExpression value]
  : join_condition { $value = $join_condition.value; }
//  | named_columns_join
  ;

join_condition returns [BooleanValueExpression value]
  : ON search_condition {
      $value = $search_condition.value;
    }
  ;

named_columns_join
  : USING LPAREN join_column_list RPAREN
  ;

join_column_list
  : column_name (COMMA column_name)*
  ;

// Limitation: nested table is not supported yet.
table_primary returns [TablePrimary value]
  : table_name
    (AS? alias_name)? {
      $value = $table_name.value; 
      $value.setAlias($alias_name.value);
      Relation table = new Relation($value);      
      relationStack.push(table);
    }
  | derived_table
    AS? alias_name {
      $value = null;
    }
  ; 
 
table_name returns [TablePrimary value]
  : (schema_name PERIOD)? table_identifier {
      String schema = $schema_name.value;      
      if (schema != null && schema != "") {
        $value = new TablePrimary(schema, $table_identifier.value);
      }
      else {
        $value = new TablePrimary($table_identifier.value);
      }      
    }
  ;  

alias_name returns [String value]
  : identifier  { $value = $identifier.value; }
  ;

derived_table
  : table_subquery
  ;
    
table_identifier returns [String value]
  : identifier { $value = $identifier.value; }
  ;
  
schema_name returns [String value]
  : identifier { $value = $identifier.value; }
  ;
    
column_name returns [String value]
  : identifier { $value = $identifier.value; }
  ;
  
identifier returns [String value]
  : (t=regular_identifier | t=delimited_identifier) { $value = $t.value; }
  ;

regular_identifier returns [String value]
  : VARNAME { $value = $VARNAME.text; }
  ;

delimited_identifier returns [String value]
  : STRING_WITH_QUOTE_DOUBLE { 
      $value = $STRING_WITH_QUOTE_DOUBLE.text;
      $value = $value.substring(1, $value.length()-1);
    }
  ;

general_literal returns [Literal value]
  : string_literal { $value = $string_literal.value; }
  | boolean_literal { $value = $boolean_literal.value; }
  ;

string_literal returns [StringLiteral value]
  : STRING_WITH_QUOTE {
      String str = $STRING_WITH_QUOTE.text;
      str = str.substring(1, str.length()-1);
      $value = new StringLiteral(str);
    }
  ;

boolean_literal returns [BooleanLiteral value]
  : (t=TRUE | t=FALSE) { $value = new BooleanLiteral(Boolean.getBoolean($t.text)); }
  ;

numeric_literal returns [NumericLiteral value]
  : numeric_literal_unsigned { $value = $numeric_literal_unsigned.value; }
  | numeric_literal_positive { $value = $numeric_literal_positive.value; }
  | numeric_literal_negative { $value = $numeric_literal_negative.value; }
  ;

numeric_literal_unsigned returns [NumericLiteral value]
  : INTEGER { $value = new IntegerLiteral($INTEGER.text); }
  | DECIMAL { $value = new DecimalLiteral($DECIMAL.text); }
  ;

numeric_literal_positive returns [NumericLiteral value]
  : INTEGER_POSITIVE { $value = new IntegerLiteral($INTEGER_POSITIVE.text); }
  | DECIMAL_POSITIVE { $value = new DecimalLiteral($DECIMAL_POSITIVE.text); }
  ;
  
numeric_literal_negative returns [NumericLiteral value]
  : INTEGER_NEGATIVE { $value = new IntegerLiteral($INTEGER_NEGATIVE.text); }
  | DECIMAL_NEGATIVE { $value = new DecimalLiteral($DECIMAL_NEGATIVE.text); }
  ;
  
truth_value returns [boolean value]
  : (t=TRUE | t=FALSE) { $value = Boolean.getBoolean($t.text); }
  ;

/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/

SELECT: ('S'|'s')('E'|'e')('L'|'l')('E'|'e')('C'|'c')('T'|'t');

DISTINCT: ('D'|'d')('I'|'i')('S'|'s')('T'|'t')('I'|'i')('N'|'n')('C'|'c')('T'|'t');

ALL: ('A'|'a')('L'|'l')('L'|'l');

AVG: ('A'|'a')('V'|'v')('G'|'g');

MAX: ('M'|'m')('A'|'a')('X'|'x');

MIN: ('M'|'m')('I'|'i')('N'|'n');

SUM: ('S'|'s')('U'|'u')('M'|'m');

EVERY: ('E'|'e')('V'|'v')('E'|'e')('R'|'r')('Y'|'y');

ANY: ('A'|'a')('N'|'n')('Y'|'y');

SOME: ('S'|'s')('O'|'o')('M'|'m')('E'|'e');

COUNT: ('C'|'c')('O'|'o')('U'|'u')('N'|'n')('T'|'t');

FROM: ('F'|'f')('R'|'r')('O'|'o')('M'|'m');

WHERE: ('W'|'w')('H'|'h')('E'|'e')('R'|'r')('E'|'e');

AND: ('A'|'a')('N'|'n')('D'|'d');

OR: ('O'|'o')('R'|'r');

NOT: ('N'|'n')('O'|'o')('T'|'t');

ORDER: ('O'|'o')('R'|'r')('D'|'d')('E'|'e')('R'|'r');

GROUP: ('G'|'g')('R'|'r')('O'|'o')('U'|'u')('P'|'p');

BY: ('B'|'b')('Y'|'y');

AS: ('A'|'a')('S'|'s');

JOIN: ('J'|'j')('O'|'o')('I'|'i')('N'|'n');

INNER: ('I'|'i')('N'|'n')('N'|'n')('E'|'e')('R'|'r');

OUTER: ('O'|'o')('U'|'u')('T'|'t')('E'|'e')('R'|'r');

LEFT: ('L'|'l')('E'|'e')('F'|'f')('T'|'t');

RIGHT: ('R'|'r')('I'|'i')('G'|'g')('H'|'h')('T'|'t');

FULL: ('F'|'f')('U'|'u')('L'|'l')('L'|'l');

UNION: ('U'|'u')('N'|'n')('I'|'i')('O'|'o')('N'|'n');

USING: ('U'|'u')('S'|'s')('I'|'i')('N'|'n')('G'|'g');

ON: ('O'|'o')('N'|'n');

IN: ('I'|'i')('N'|'n');

IS: ('I'|'i')('S'|'s');

NULL: ('N'|'n')('U'|'u')('L'|'l')('L'|'l');

FALSE: ('F'|'f')('A'|'a')('L'|'l')('S'|'s')('E'|'e');

TRUE: ('T'|'t')('R'|'r')('U'|'u')('E'|'e');

SEMI:          ';';
PERIOD:        '.';
COMMA:         ',';
LSQ_BRACKET:   '[';
RSQ_BRACKET:   ']';
LPAREN:        '(';
RPAREN:        ')';
QUESTION:      '?';
DOLLAR:        '$';
QUOTE_DOUBLE:  '"';
QUOTE_SINGLE:  '\'';
APOSTROPHE:    '`';
UNDERSCORE:    '_';
MINUS:         '-';
ASTERISK:      '*';
AMPERSAND:     '&';
AT:            '@';
EXCLAMATION:   '!';
HASH:          '#';
PERCENT:       '%';
PLUS:          '+';
EQUALS:        '=';
COLON:         ':';
LESS:          '<';
GREATER:       '>';
SOLIDUS:       '/';
DOUBLE_SLASH:  '//';
BACKSLASH:     '\\';
TILDE:         '~';
CARET:         '^';
CONCATENATION: '||';

fragment ALPHA
  : 'a'..'z'
  | 'A'..'Z'
  ;

fragment DIGIT
  : '0'..'9'
  ; 

fragment ALPHANUM
  : ALPHA
  | DIGIT
  ;

fragment CHAR
  : ALPHANUM
  | UNDERSCORE
  | MINUS
  ;

fragment ECHAR
  : '\\' ('t' | 'b' | 'n' | 'r' | 'f' | '\\' | '"' | '\'')
  ;

INTEGER
  : DIGIT+
  ;

DECIMAL
  : DIGIT+ PERIOD DIGIT+
  | PERIOD DIGIT+
  ;
  
INTEGER_POSITIVE
  : PLUS INTEGER
  ;

INTEGER_NEGATIVE
  : MINUS INTEGER
  ;   

DECIMAL_POSITIVE
  : PLUS DECIMAL
  ;
  
DECIMAL_NEGATIVE
  : MINUS DECIMAL
  ;

VARNAME
  : ALPHA CHAR*
  ;

STRING_WITH_QUOTE
  : '\'' ( options {greedy=false  ;} : ~('\u0027' | '\u005C' | '\u000A' | '\u000D') | ECHAR )* '\''
  ;

STRING_WITH_QUOTE_DOUBLE
  : '"'  ( options {greedy=false  ;} : ~('\u0022' | '\u005C' | '\u000A' | '\u000D') | ECHAR )* '"'
  ;

WS: (' '|'\t'|('\n'|'\r'('\n')))+ {$channel=HIDDEN;};