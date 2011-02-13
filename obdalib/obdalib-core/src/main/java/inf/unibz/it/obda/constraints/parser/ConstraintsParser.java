// $ANTLR 3.3 Nov 30, 2010 12:50:56 Constraints.g 2011-01-19 14:13:15

package inf.unibz.it.obda.constraints.parser;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.constraints.AbstractConstraintAssertion;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSCheckConstraint;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSForeignKeyConstraint;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSPrimaryKeyConstraint;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSUniquenessConstraint;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.ucq.typing.CheckOperationTerm;
import inf.unibz.it.ucq.typing.UnknownXSDTypeException;
import inf.unibz.it.ucq.typing.XSDTypingController;

import java.net.URI;
import java.util.Vector;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.obda.query.domain.Term;
import org.obda.query.domain.ValueConstant;
import org.obda.query.domain.Variable;
import org.obda.query.domain.imp.TermFactoryImpl;

import com.sun.msv.datatype.xsd.XSDatatype;

public class ConstraintsParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "CHECK", "LPAREN", "RPAREN", "COMMA", "UNIQUE", "REFERENCES", "PRIMARY_KEY", "STRING_LITERAL", "STRING_LITERAL2", "NUMBER", "TRUE", "FALSE", "DOLLAR", "STRING", "LESS", "GREATER", "EQUALS", "LESS_OR_EQUAL", "GREATER_OR_EQUAL", "DOT", "QUOTE_DOUBLE", "QUOTE_SINGLE", "UNDERSCORE", "DASH", "ALPHA", "DIGIT", "ALPHANUM", "WS"
    };
    public static final int EOF=-1;
    public static final int CHECK=4;
    public static final int LPAREN=5;
    public static final int RPAREN=6;
    public static final int COMMA=7;
    public static final int UNIQUE=8;
    public static final int REFERENCES=9;
    public static final int PRIMARY_KEY=10;
    public static final int STRING_LITERAL=11;
    public static final int STRING_LITERAL2=12;
    public static final int NUMBER=13;
    public static final int TRUE=14;
    public static final int FALSE=15;
    public static final int DOLLAR=16;
    public static final int STRING=17;
    public static final int LESS=18;
    public static final int GREATER=19;
    public static final int EQUALS=20;
    public static final int LESS_OR_EQUAL=21;
    public static final int GREATER_OR_EQUAL=22;
    public static final int DOT=23;
    public static final int QUOTE_DOUBLE=24;
    public static final int QUOTE_SINGLE=25;
    public static final int UNDERSCORE=26;
    public static final int DASH=27;
    public static final int ALPHA=28;
    public static final int DIGIT=29;
    public static final int ALPHANUM=30;
    public static final int WS=31;

    // delegates
    // delegators


        public ConstraintsParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public ConstraintsParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return ConstraintsParser.tokenNames; }
    public String getGrammarFileName() { return "Constraints.g"; }


    /** The API controller */
    private APIController apic = null;

    /** A factory to construct the subject and object terms */
    private TermFactoryImpl termFactory = TermFactoryImpl.getInstance();

    public void setController(APIController apic) {
      this.apic = apic;
    }



    // $ANTLR start "parse"
    // Constraints.g:77:1: parse returns [AbstractConstraintAssertion constraint] : prog EOF ;
    public final AbstractConstraintAssertion parse() throws RecognitionException {
        AbstractConstraintAssertion constraint = null;

        AbstractConstraintAssertion prog1 = null;


        try {
            // Constraints.g:78:3: ( prog EOF )
            // Constraints.g:78:5: prog EOF
            {
            pushFollow(FOLLOW_prog_in_parse52);
            prog1=prog();

            state._fsp--;

            match(input,EOF,FOLLOW_EOF_in_parse54); 

                  constraint = prog1;
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return constraint;
    }
    // $ANTLR end "parse"


    // $ANTLR start "prog"
    // Constraints.g:83:1: prog returns [AbstractConstraintAssertion value] : expression ;
    public final AbstractConstraintAssertion prog() throws RecognitionException {
        AbstractConstraintAssertion value = null;

        AbstractConstraintAssertion expression2 = null;


        try {
            // Constraints.g:84:3: ( expression )
            // Constraints.g:84:5: expression
            {
            pushFollow(FOLLOW_expression_in_prog75);
            expression2=expression();

            state._fsp--;


                  value = expression2;
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "prog"


    // $ANTLR start "expression"
    // Constraints.g:89:1: expression returns [AbstractConstraintAssertion value] : ( check_constraint | unique_constraint | foreign_key_constraint | primary_key_constraint );
    public final AbstractConstraintAssertion expression() throws RecognitionException {
        AbstractConstraintAssertion value = null;

        RDBMSCheckConstraint check_constraint3 = null;

        RDBMSUniquenessConstraint unique_constraint4 = null;

        RDBMSForeignKeyConstraint foreign_key_constraint5 = null;

        RDBMSPrimaryKeyConstraint primary_key_constraint6 = null;


        try {
            // Constraints.g:90:3: ( check_constraint | unique_constraint | foreign_key_constraint | primary_key_constraint )
            int alt1=4;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==STRING) ) {
                switch ( input.LA(2) ) {
                case CHECK:
                    {
                    alt1=1;
                    }
                    break;
                case UNIQUE:
                    {
                    alt1=2;
                    }
                    break;
                case LPAREN:
                    {
                    alt1=3;
                    }
                    break;
                case PRIMARY_KEY:
                    {
                    alt1=4;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 1, 1, input);

                    throw nvae;
                }

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }
            switch (alt1) {
                case 1 :
                    // Constraints.g:90:5: check_constraint
                    {
                    pushFollow(FOLLOW_check_constraint_in_expression96);
                    check_constraint3=check_constraint();

                    state._fsp--;

                     value = check_constraint3; 

                    }
                    break;
                case 2 :
                    // Constraints.g:91:5: unique_constraint
                    {
                    pushFollow(FOLLOW_unique_constraint_in_expression111);
                    unique_constraint4=unique_constraint();

                    state._fsp--;

                     value = unique_constraint4; 

                    }
                    break;
                case 3 :
                    // Constraints.g:92:5: foreign_key_constraint
                    {
                    pushFollow(FOLLOW_foreign_key_constraint_in_expression125);
                    foreign_key_constraint5=foreign_key_constraint();

                    state._fsp--;

                     value = foreign_key_constraint5; 

                    }
                    break;
                case 4 :
                    // Constraints.g:93:5: primary_key_constraint
                    {
                    pushFollow(FOLLOW_primary_key_constraint_in_expression134);
                    primary_key_constraint6=primary_key_constraint();

                    state._fsp--;

                     value = primary_key_constraint6; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "expression"


    // $ANTLR start "check_constraint"
    // Constraints.g:96:1: check_constraint returns [RDBMSCheckConstraint value] : table_name CHECK LPAREN c1= condition RPAREN ( COMMA LPAREN c2= condition RPAREN )* ;
    public final RDBMSCheckConstraint check_constraint() throws RecognitionException {
        RDBMSCheckConstraint value = null;

        CheckOperationTerm c1 = null;

        CheckOperationTerm c2 = null;

        ConstraintsParser.table_name_return table_name7 = null;



          Vector<CheckOperationTerm> conditions = new Vector<CheckOperationTerm>();
          MappingController mc = apic.getMappingController();
          DatasourcesController dc = apic.getDatasourcesController();
          URI sourceUri = dc.getCurrentDataSource().getSourceID();
          OBDAMappingAxiom axiom = null;

        try {
            // Constraints.g:104:3: ( table_name CHECK LPAREN c1= condition RPAREN ( COMMA LPAREN c2= condition RPAREN )* )
            // Constraints.g:104:5: table_name CHECK LPAREN c1= condition RPAREN ( COMMA LPAREN c2= condition RPAREN )*
            {
            pushFollow(FOLLOW_table_name_in_check_constraint161);
            table_name7=table_name();

            state._fsp--;

            match(input,CHECK,FOLLOW_CHECK_in_check_constraint163); 
            match(input,LPAREN,FOLLOW_LPAREN_in_check_constraint165); 
            pushFollow(FOLLOW_condition_in_check_constraint169);
            c1=condition();

            state._fsp--;

             conditions.add(c1); 
            match(input,RPAREN,FOLLOW_RPAREN_in_check_constraint173); 
            // Constraints.g:105:9: ( COMMA LPAREN c2= condition RPAREN )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==COMMA) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // Constraints.g:105:10: COMMA LPAREN c2= condition RPAREN
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_check_constraint185); 
            	    match(input,LPAREN,FOLLOW_LPAREN_in_check_constraint187); 
            	    pushFollow(FOLLOW_condition_in_check_constraint191);
            	    c2=condition();

            	    state._fsp--;

            	     conditions.add(c2); 
            	    match(input,RPAREN,FOLLOW_RPAREN_in_check_constraint195); 

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);



                  axiom = mc.getMapping(sourceUri, (table_name7!=null?input.toString(table_name7.start,table_name7.stop):null));
                  value = new RDBMSCheckConstraint(
                      (table_name7!=null?input.toString(table_name7.start,table_name7.stop):null), (RDBMSSQLQuery)axiom.getSourceQuery(), conditions);
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "check_constraint"


    // $ANTLR start "unique_constraint"
    // Constraints.g:113:1: unique_constraint returns [RDBMSUniquenessConstraint value] : table_name UNIQUE LPAREN parameter RPAREN ;
    public final RDBMSUniquenessConstraint unique_constraint() throws RecognitionException {
        RDBMSUniquenessConstraint value = null;

        ConstraintsParser.table_name_return table_name8 = null;

        Vector<Variable> parameter9 = null;



          MappingController mc = apic.getMappingController();
          DatasourcesController dc = apic.getDatasourcesController();
          URI sourceUri = dc.getCurrentDataSource().getSourceID();
          OBDAMappingAxiom axiom = null;

        try {
            // Constraints.g:120:3: ( table_name UNIQUE LPAREN parameter RPAREN )
            // Constraints.g:120:5: table_name UNIQUE LPAREN parameter RPAREN
            {
            pushFollow(FOLLOW_table_name_in_unique_constraint223);
            table_name8=table_name();

            state._fsp--;

            match(input,UNIQUE,FOLLOW_UNIQUE_in_unique_constraint225); 
            match(input,LPAREN,FOLLOW_LPAREN_in_unique_constraint227); 
            pushFollow(FOLLOW_parameter_in_unique_constraint229);
            parameter9=parameter();

            state._fsp--;

            match(input,RPAREN,FOLLOW_RPAREN_in_unique_constraint231); 

                  
                  axiom = mc.getMapping(sourceUri, (table_name8!=null?input.toString(table_name8.start,table_name8.stop):null));
                  value = new RDBMSUniquenessConstraint(
                      (table_name8!=null?input.toString(table_name8.start,table_name8.stop):null), (RDBMSSQLQuery)axiom.getSourceQuery(), 
                      parameter9);
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "unique_constraint"


    // $ANTLR start "foreign_key_constraint"
    // Constraints.g:129:1: foreign_key_constraint returns [RDBMSForeignKeyConstraint value] : t1= table_name LPAREN p1= parameter RPAREN REFERENCES t2= table_name ( LPAREN p2= parameter RPAREN )? ;
    public final RDBMSForeignKeyConstraint foreign_key_constraint() throws RecognitionException {
        RDBMSForeignKeyConstraint value = null;

        ConstraintsParser.table_name_return t1 = null;

        Vector<Variable> p1 = null;

        ConstraintsParser.table_name_return t2 = null;

        Vector<Variable> p2 = null;



          MappingController mc = apic.getMappingController();
          DatasourcesController dc = apic.getDatasourcesController();
          URI sourceUri = dc.getCurrentDataSource().getSourceID();
          OBDAMappingAxiom axiom1 = null;
          OBDAMappingAxiom axiom2 = null;

        try {
            // Constraints.g:137:3: (t1= table_name LPAREN p1= parameter RPAREN REFERENCES t2= table_name ( LPAREN p2= parameter RPAREN )? )
            // Constraints.g:137:5: t1= table_name LPAREN p1= parameter RPAREN REFERENCES t2= table_name ( LPAREN p2= parameter RPAREN )?
            {
            pushFollow(FOLLOW_table_name_in_foreign_key_constraint259);
            t1=table_name();

            state._fsp--;

            match(input,LPAREN,FOLLOW_LPAREN_in_foreign_key_constraint261); 
            pushFollow(FOLLOW_parameter_in_foreign_key_constraint265);
            p1=parameter();

            state._fsp--;

            match(input,RPAREN,FOLLOW_RPAREN_in_foreign_key_constraint267); 
            match(input,REFERENCES,FOLLOW_REFERENCES_in_foreign_key_constraint269); 
            pushFollow(FOLLOW_table_name_in_foreign_key_constraint273);
            t2=table_name();

            state._fsp--;

            // Constraints.g:137:71: ( LPAREN p2= parameter RPAREN )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==LPAREN) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // Constraints.g:137:72: LPAREN p2= parameter RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_foreign_key_constraint276); 
                    pushFollow(FOLLOW_parameter_in_foreign_key_constraint280);
                    p2=parameter();

                    state._fsp--;

                    match(input,RPAREN,FOLLOW_RPAREN_in_foreign_key_constraint282); 

                    }
                    break;

            }


              
                  axiom1 = mc.getMapping(sourceUri, (t1!=null?input.toString(t1.start,t1.stop):null));
                  axiom2 = mc.getMapping(sourceUri, (t2!=null?input.toString(t2.start,t2.stop):null));
                  value = new RDBMSForeignKeyConstraint(
                    (t1!=null?input.toString(t1.start,t1.stop):null), (t2!=null?input.toString(t2.start,t2.stop):null), 
                    (RDBMSSQLQuery)axiom1.getSourceQuery(), 
                    (RDBMSSQLQuery)axiom2.getSourceQuery(), 
                    p1, p2);
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "foreign_key_constraint"


    // $ANTLR start "primary_key_constraint"
    // Constraints.g:149:1: primary_key_constraint returns [RDBMSPrimaryKeyConstraint value] : table_name PRIMARY_KEY LPAREN parameter RPAREN ;
    public final RDBMSPrimaryKeyConstraint primary_key_constraint() throws RecognitionException {
        RDBMSPrimaryKeyConstraint value = null;

        ConstraintsParser.table_name_return table_name10 = null;

        Vector<Variable> parameter11 = null;



          MappingController mc = apic.getMappingController();
          DatasourcesController dc = apic.getDatasourcesController();
          URI sourceUri = dc.getCurrentDataSource().getSourceID();
          OBDAMappingAxiom axiom = null;

        try {
            // Constraints.g:156:3: ( table_name PRIMARY_KEY LPAREN parameter RPAREN )
            // Constraints.g:156:5: table_name PRIMARY_KEY LPAREN parameter RPAREN
            {
            pushFollow(FOLLOW_table_name_in_primary_key_constraint310);
            table_name10=table_name();

            state._fsp--;

            match(input,PRIMARY_KEY,FOLLOW_PRIMARY_KEY_in_primary_key_constraint312); 
            match(input,LPAREN,FOLLOW_LPAREN_in_primary_key_constraint314); 
            pushFollow(FOLLOW_parameter_in_primary_key_constraint316);
            parameter11=parameter();

            state._fsp--;

            match(input,RPAREN,FOLLOW_RPAREN_in_primary_key_constraint318); 

              
                  axiom = mc.getMapping(sourceUri, (table_name10!=null?input.toString(table_name10.start,table_name10.stop):null));
                  value = new RDBMSPrimaryKeyConstraint(
                      (table_name10!=null?input.toString(table_name10.start,table_name10.stop):null), (RDBMSSQLQuery)axiom.getSourceQuery(), 
                      parameter11);
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "primary_key_constraint"


    // $ANTLR start "condition"
    // Constraints.g:165:1: condition returns [CheckOperationTerm value] : variable op range ;
    public final CheckOperationTerm condition() throws RecognitionException {
        CheckOperationTerm value = null;

        Variable variable12 = null;

        ConstraintsParser.op_return op13 = null;

        Term range14 = null;


        try {
            // Constraints.g:166:3: ( variable op range )
            // Constraints.g:166:5: variable op range
            {
            pushFollow(FOLLOW_variable_in_condition339);
            variable12=variable();

            state._fsp--;

            pushFollow(FOLLOW_op_in_condition341);
            op13=op();

            state._fsp--;

            pushFollow(FOLLOW_range_in_condition343);
            range14=range();

            state._fsp--;


                  value = new CheckOperationTerm(variable12, (op13!=null?input.toString(op13.start,op13.stop):null), range14);
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "condition"


    // $ANTLR start "parameter"
    // Constraints.g:171:1: parameter returns [Vector<Variable> values] : v1= variable ( COMMA v2= variable )* ;
    public final Vector<Variable> parameter() throws RecognitionException {
        Vector<Variable> values = null;

        Variable v1 = null;

        Variable v2 = null;



          values = new Vector<Variable>();

        try {
            // Constraints.g:175:3: (v1= variable ( COMMA v2= variable )* )
            // Constraints.g:175:5: v1= variable ( COMMA v2= variable )*
            {
            pushFollow(FOLLOW_variable_in_parameter371);
            v1=variable();

            state._fsp--;

             values.add(v1); 
            // Constraints.g:175:45: ( COMMA v2= variable )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==COMMA) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // Constraints.g:175:46: COMMA v2= variable
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_parameter376); 
            	    pushFollow(FOLLOW_variable_in_parameter380);
            	    v2=variable();

            	    state._fsp--;

            	     values.add(v2); 

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return values;
    }
    // $ANTLR end "parameter"


    // $ANTLR start "range"
    // Constraints.g:178:1: range returns [Term value] : ( variable | string_constant | numeric_constant | boolean_constant );
    public final Term range() throws RecognitionException {
        Term value = null;

        Variable variable15 = null;

        ValueConstant string_constant16 = null;

        ValueConstant numeric_constant17 = null;

        ValueConstant boolean_constant18 = null;


        try {
            // Constraints.g:179:3: ( variable | string_constant | numeric_constant | boolean_constant )
            int alt5=4;
            switch ( input.LA(1) ) {
            case DOLLAR:
                {
                alt5=1;
                }
                break;
            case STRING_LITERAL:
            case STRING_LITERAL2:
                {
                alt5=2;
                }
                break;
            case NUMBER:
                {
                alt5=3;
                }
                break;
            case TRUE:
            case FALSE:
                {
                alt5=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }

            switch (alt5) {
                case 1 :
                    // Constraints.g:179:5: variable
                    {
                    pushFollow(FOLLOW_variable_in_range403);
                    variable15=variable();

                    state._fsp--;

                     value = variable15; 

                    }
                    break;
                case 2 :
                    // Constraints.g:180:5: string_constant
                    {
                    pushFollow(FOLLOW_string_constant_in_range419);
                    string_constant16=string_constant();

                    state._fsp--;

                     value = string_constant16; 

                    }
                    break;
                case 3 :
                    // Constraints.g:181:5: numeric_constant
                    {
                    pushFollow(FOLLOW_numeric_constant_in_range428);
                    numeric_constant17=numeric_constant();

                    state._fsp--;

                     value = numeric_constant17; 

                    }
                    break;
                case 4 :
                    // Constraints.g:182:5: boolean_constant
                    {
                    pushFollow(FOLLOW_boolean_constant_in_range436);
                    boolean_constant18=boolean_constant();

                    state._fsp--;

                     value = boolean_constant18; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "range"


    // $ANTLR start "string_constant"
    // Constraints.g:185:1: string_constant returns [ValueConstant value] : ( STRING_LITERAL | STRING_LITERAL2 );
    public final ValueConstant string_constant() throws RecognitionException {
        ValueConstant value = null;

        Token STRING_LITERAL19=null;
        Token STRING_LITERAL220=null;


          XSDatatype type = null;
          try {
            type = XSDTypingController.getInstance().getType("xsd:string");
          }
          catch (UnknownXSDTypeException e) {
            // Do nothing.
          }  

        try {
            // Constraints.g:195:3: ( STRING_LITERAL | STRING_LITERAL2 )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==STRING_LITERAL) ) {
                alt6=1;
            }
            else if ( (LA6_0==STRING_LITERAL2) ) {
                alt6=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // Constraints.g:195:5: STRING_LITERAL
                    {
                    STRING_LITERAL19=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_string_constant462); 

                          value = termFactory.createValueConstant((STRING_LITERAL19!=null?STRING_LITERAL19.getText():null), type);
                        

                    }
                    break;
                case 2 :
                    // Constraints.g:198:5: STRING_LITERAL2
                    {
                    STRING_LITERAL220=(Token)match(input,STRING_LITERAL2,FOLLOW_STRING_LITERAL2_in_string_constant470); 

                          value = termFactory.createValueConstant((STRING_LITERAL220!=null?STRING_LITERAL220.getText():null), type);
                        

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "string_constant"


    // $ANTLR start "numeric_constant"
    // Constraints.g:203:1: numeric_constant returns [ValueConstant value] : NUMBER ;
    public final ValueConstant numeric_constant() throws RecognitionException {
        ValueConstant value = null;

        Token NUMBER21=null;


          String constant = "";
          XSDatatype type = null;
          try {
        	  type = (constant.contains(".")) ? 
        	     XSDTypingController.getInstance().getType("xsd:double") :
        		   XSDTypingController.getInstance().getType("xsd:int");
          }
          catch (UnknownXSDTypeException e) {
            // Do nothing.
          }  

        try {
            // Constraints.g:216:3: ( NUMBER )
            // Constraints.g:216:5: NUMBER
            {
            NUMBER21=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_numeric_constant496); 

                  constant = (NUMBER21!=null?NUMBER21.getText():null);
                  value = termFactory.createValueConstant(constant, type);
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "numeric_constant"


    // $ANTLR start "boolean_constant"
    // Constraints.g:222:1: boolean_constant returns [ValueConstant value] : ( TRUE | FALSE );
    public final ValueConstant boolean_constant() throws RecognitionException {
        ValueConstant value = null;


          XSDatatype type = null;
          try {
            type = XSDTypingController.getInstance().getType("xsd:boolean");
          }
          catch (UnknownXSDTypeException e) {
            // Do nothing.
          }  

        try {
            // Constraints.g:232:3: ( TRUE | FALSE )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==TRUE) ) {
                alt7=1;
            }
            else if ( (LA7_0==FALSE) ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // Constraints.g:232:5: TRUE
                    {
                    match(input,TRUE,FOLLOW_TRUE_in_boolean_constant522); 

                          value = termFactory.createValueConstant("true", type);
                        

                    }
                    break;
                case 2 :
                    // Constraints.g:235:5: FALSE
                    {
                    match(input,FALSE,FOLLOW_FALSE_in_boolean_constant530); 

                          value = termFactory.createValueConstant("false", type);
                        

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "boolean_constant"


    // $ANTLR start "variable"
    // Constraints.g:240:1: variable returns [Variable value] : DOLLAR var_name ;
    public final Variable variable() throws RecognitionException {
        Variable value = null;

        ConstraintsParser.var_name_return var_name22 = null;


        try {
            // Constraints.g:241:3: ( DOLLAR var_name )
            // Constraints.g:241:5: DOLLAR var_name
            {
            match(input,DOLLAR,FOLLOW_DOLLAR_in_variable552); 
            pushFollow(FOLLOW_var_name_in_variable554);
            var_name22=var_name();

            state._fsp--;


                  value = termFactory.createVariable((var_name22!=null?input.toString(var_name22.start,var_name22.stop):null));
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "variable"

    public static class var_name_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "var_name"
    // Constraints.g:246:1: var_name : STRING ;
    public final ConstraintsParser.var_name_return var_name() throws RecognitionException {
        ConstraintsParser.var_name_return retval = new ConstraintsParser.var_name_return();
        retval.start = input.LT(1);

        try {
            // Constraints.g:247:3: ( STRING )
            // Constraints.g:247:5: STRING
            {
            match(input,STRING,FOLLOW_STRING_in_var_name569); 

            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "var_name"

    public static class table_name_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "table_name"
    // Constraints.g:250:1: table_name : STRING ;
    public final ConstraintsParser.table_name_return table_name() throws RecognitionException {
        ConstraintsParser.table_name_return retval = new ConstraintsParser.table_name_return();
        retval.start = input.LT(1);

        try {
            // Constraints.g:251:3: ( STRING )
            // Constraints.g:251:5: STRING
            {
            match(input,STRING,FOLLOW_STRING_in_table_name582); 

            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "table_name"

    public static class op_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "op"
    // Constraints.g:254:1: op : ( LESS | GREATER | EQUALS | LESS_OR_EQUAL | GREATER_OR_EQUAL );
    public final ConstraintsParser.op_return op() throws RecognitionException {
        ConstraintsParser.op_return retval = new ConstraintsParser.op_return();
        retval.start = input.LT(1);

        try {
            // Constraints.g:255:3: ( LESS | GREATER | EQUALS | LESS_OR_EQUAL | GREATER_OR_EQUAL )
            // Constraints.g:
            {
            if ( (input.LA(1)>=LESS && input.LA(1)<=GREATER_OR_EQUAL) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "op"

    // Delegated rules


 

    public static final BitSet FOLLOW_prog_in_parse52 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_parse54 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_prog75 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_check_constraint_in_expression96 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unique_constraint_in_expression111 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_foreign_key_constraint_in_expression125 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primary_key_constraint_in_expression134 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_name_in_check_constraint161 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_CHECK_in_check_constraint163 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_LPAREN_in_check_constraint165 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_condition_in_check_constraint169 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_RPAREN_in_check_constraint173 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_check_constraint185 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_LPAREN_in_check_constraint187 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_condition_in_check_constraint191 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_RPAREN_in_check_constraint195 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_table_name_in_unique_constraint223 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_UNIQUE_in_unique_constraint225 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_LPAREN_in_unique_constraint227 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_parameter_in_unique_constraint229 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_RPAREN_in_unique_constraint231 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_name_in_foreign_key_constraint259 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_LPAREN_in_foreign_key_constraint261 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_parameter_in_foreign_key_constraint265 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_RPAREN_in_foreign_key_constraint267 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_REFERENCES_in_foreign_key_constraint269 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_table_name_in_foreign_key_constraint273 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_LPAREN_in_foreign_key_constraint276 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_parameter_in_foreign_key_constraint280 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_RPAREN_in_foreign_key_constraint282 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_name_in_primary_key_constraint310 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_PRIMARY_KEY_in_primary_key_constraint312 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_LPAREN_in_primary_key_constraint314 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_parameter_in_primary_key_constraint316 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_RPAREN_in_primary_key_constraint318 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_condition339 = new BitSet(new long[]{0x00000000007C0000L});
    public static final BitSet FOLLOW_op_in_condition341 = new BitSet(new long[]{0x000000000001F800L});
    public static final BitSet FOLLOW_range_in_condition343 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_parameter371 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_parameter376 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_variable_in_parameter380 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_variable_in_range403 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_string_constant_in_range419 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numeric_constant_in_range428 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_constant_in_range436 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_string_constant462 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL2_in_string_constant470 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_numeric_constant496 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_boolean_constant522 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_boolean_constant530 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOLLAR_in_variable552 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_var_name_in_variable554 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_var_name569 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_table_name582 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_op0 = new BitSet(new long[]{0x0000000000000002L});

}