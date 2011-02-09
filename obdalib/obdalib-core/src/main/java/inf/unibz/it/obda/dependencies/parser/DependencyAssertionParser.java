// $ANTLR 3.3 Nov 30, 2010 12:50:56 DependencyAssertion.g 2011-01-19 16:47:22

package inf.unibz.it.obda.dependencies.parser;

import java.net.URI;
import java.util.Vector;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.TokenStream;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.dependencies.AbstractDependencyAssertion;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSDisjointnessDependency;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSFunctionalDependency;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSInclusionDependency;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;

import org.obda.query.domain.imp.TermFactoryImpl;
import org.obda.query.domain.Variable;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class DependencyAssertionParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "SEMI", "LPAREN", "COMMA", "RPAREN", "LSQ_BRACKET", "RSQ_BRACKET", "BODY", "HEAD", "DOT", "DOLLAR", "STRING", "UNDERSCORE", "DASH", "ALPHA", "DIGIT", "ALPHANUM", "WS", "'includedIn'", "'disjoint'", "'functionOf'"
    };
    public static final int EOF=-1;
    public static final int T__21=21;
    public static final int T__22=22;
    public static final int T__23=23;
    public static final int SEMI=4;
    public static final int LPAREN=5;
    public static final int COMMA=6;
    public static final int RPAREN=7;
    public static final int LSQ_BRACKET=8;
    public static final int RSQ_BRACKET=9;
    public static final int BODY=10;
    public static final int HEAD=11;
    public static final int DOT=12;
    public static final int DOLLAR=13;
    public static final int STRING=14;
    public static final int UNDERSCORE=15;
    public static final int DASH=16;
    public static final int ALPHA=17;
    public static final int DIGIT=18;
    public static final int ALPHANUM=19;
    public static final int WS=20;

    // delegates
    // delegators


        public DependencyAssertionParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public DependencyAssertionParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return DependencyAssertionParser.tokenNames; }
    public String getGrammarFileName() { return "DependencyAssertion.g"; }


    /** The API controller */
    private APIController apic = null;

    /** A factory to construct the subject and object terms */
    private TermFactoryImpl termFactory = TermFactoryImpl.getInstance();

    public void setController(APIController apic) {
      this.apic = apic;
    }

    public class MappingException extends RecognitionException {
      private String msg = "";
      
      public MappingException(String msg) {
        this.msg = msg;
      }
      
      public String getErrorMessage() {
        return msg;
      }
    }



    // $ANTLR start "parse"
    // DependencyAssertion.g:79:1: parse returns [Vector<AbstractDependencyAssertion> assertions] : prog EOF ;
    public final Vector<AbstractDependencyAssertion> parse() throws RecognitionException {
        Vector<AbstractDependencyAssertion> assertions = null;

        Vector<AbstractDependencyAssertion> prog1 = null;


        try {
            // DependencyAssertion.g:80:3: ( prog EOF )
            // DependencyAssertion.g:80:5: prog EOF
            {
            pushFollow(FOLLOW_prog_in_parse50);
            prog1=prog();

            state._fsp--;

            match(input,EOF,FOLLOW_EOF_in_parse52); 

                  assertions = prog1;
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return assertions;
    }
    // $ANTLR end "parse"


    // $ANTLR start "prog"
    // DependencyAssertion.g:85:1: prog returns [Vector<AbstractDependencyAssertion> values] : e1= expression ( SEMI e2= expression )* ;
    public final Vector<AbstractDependencyAssertion> prog() throws RecognitionException {
        Vector<AbstractDependencyAssertion> values = null;

        AbstractDependencyAssertion e1 = null;

        AbstractDependencyAssertion e2 = null;



          values = new Vector<AbstractDependencyAssertion>();

        try {
            // DependencyAssertion.g:89:3: (e1= expression ( SEMI e2= expression )* )
            // DependencyAssertion.g:89:5: e1= expression ( SEMI e2= expression )*
            {
            pushFollow(FOLLOW_expression_in_prog81);
            e1=expression();

            state._fsp--;

             values.add(e1); 
            // DependencyAssertion.g:90:5: ( SEMI e2= expression )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==SEMI) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // DependencyAssertion.g:90:6: SEMI e2= expression
            	    {
            	    match(input,SEMI,FOLLOW_SEMI_in_prog91); 
            	    pushFollow(FOLLOW_expression_in_prog95);
            	    e2=expression();

            	    state._fsp--;

            	     values.add(e2); 

            	    }
            	    break;

            	default :
            	    break loop1;
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
    // $ANTLR end "prog"


    // $ANTLR start "expression"
    // DependencyAssertion.g:93:1: expression returns [AbstractDependencyAssertion value] : ( inclusion_dependency | disjointness_dependency | functional_dependency );
    public final AbstractDependencyAssertion expression() throws RecognitionException {
        AbstractDependencyAssertion value = null;

        RDBMSInclusionDependency inclusion_dependency2 = null;

        RDBMSDisjointnessDependency disjointness_dependency3 = null;

        RDBMSFunctionalDependency functional_dependency4 = null;


        try {
            // DependencyAssertion.g:94:3: ( inclusion_dependency | disjointness_dependency | functional_dependency )
            int alt2=3;
            switch ( input.LA(1) ) {
            case 21:
                {
                alt2=1;
                }
                break;
            case 22:
                {
                alt2=2;
                }
                break;
            case 23:
                {
                alt2=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }

            switch (alt2) {
                case 1 :
                    // DependencyAssertion.g:94:5: inclusion_dependency
                    {
                    pushFollow(FOLLOW_inclusion_dependency_in_expression117);
                    inclusion_dependency2=inclusion_dependency();

                    state._fsp--;

                     value = inclusion_dependency2; 

                    }
                    break;
                case 2 :
                    // DependencyAssertion.g:95:5: disjointness_dependency
                    {
                    pushFollow(FOLLOW_disjointness_dependency_in_expression128);
                    disjointness_dependency3=disjointness_dependency();

                    state._fsp--;

                     value = disjointness_dependency3; 

                    }
                    break;
                case 3 :
                    // DependencyAssertion.g:96:5: functional_dependency
                    {
                    pushFollow(FOLLOW_functional_dependency_in_expression136);
                    functional_dependency4=functional_dependency();

                    state._fsp--;

                     value = functional_dependency4; 

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


    // $ANTLR start "inclusion_dependency"
    // DependencyAssertion.g:99:1: inclusion_dependency returns [RDBMSInclusionDependency value] : 'includedIn' LPAREN p1= parameter COMMA p2= parameter RPAREN ;
    public final RDBMSInclusionDependency inclusion_dependency() throws RecognitionException {
        RDBMSInclusionDependency value = null;

        ArrayList<Object> p1 = null;

        ArrayList<Object> p2 = null;


        try {
            // DependencyAssertion.g:100:3: ( 'includedIn' LPAREN p1= parameter COMMA p2= parameter RPAREN )
            // DependencyAssertion.g:100:5: 'includedIn' LPAREN p1= parameter COMMA p2= parameter RPAREN
            {
            match(input,21,FOLLOW_21_in_inclusion_dependency159); 
            match(input,LPAREN,FOLLOW_LPAREN_in_inclusion_dependency161); 
            pushFollow(FOLLOW_parameter_in_inclusion_dependency165);
            p1=parameter();

            state._fsp--;

            match(input,COMMA,FOLLOW_COMMA_in_inclusion_dependency167); 
            pushFollow(FOLLOW_parameter_in_inclusion_dependency171);
            p2=parameter();

            state._fsp--;

            match(input,RPAREN,FOLLOW_RPAREN_in_inclusion_dependency173); 

                  String mapId1 = (String) p1.get(0);
                  String mapId2 = (String) p2.get(0);
                  Vector<Variable> mapTerms1 = (Vector<Variable>) p1.get(1);
                  Vector<Variable> mapTerms2 = (Vector<Variable>) p2.get(1);     
                  
                  if (mapTerms1.size() != mapTerms2.size()) {
                    throw new MappingException("Both mappings in the assertion must " +
                        "have the same number of terms!");
                  }
                  else {
                    MappingController mc = apic.getMappingController();
                    DatasourcesController dc = apic.getDatasourcesController();
                    URI sourceUri = dc.getCurrentDataSource().getSourceID();
                    OBDAMappingAxiom mapAxiom1 = mc.getMapping(sourceUri, mapId1);
                    OBDAMappingAxiom mapAxiom2 = mc.getMapping(sourceUri, mapId2);
                    
                    if (mapAxiom1 != null && mapAxiom2 != null) {
                      value = new RDBMSInclusionDependency(sourceUri, mapId1, mapId2,
                        (RDBMSSQLQuery) mapAxiom1.getSourceQuery(),
                        (RDBMSSQLQuery) mapAxiom2.getSourceQuery(), 
                        mapTerms1, mapTerms2);
                    }
                    else {
                      throw new MappingException("Invalid mapping id!");
                    }
                  }
                

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
    // $ANTLR end "inclusion_dependency"


    // $ANTLR start "disjointness_dependency"
    // DependencyAssertion.g:130:1: disjointness_dependency returns [RDBMSDisjointnessDependency value] : 'disjoint' LPAREN p1= parameter COMMA p2= parameter RPAREN ;
    public final RDBMSDisjointnessDependency disjointness_dependency() throws RecognitionException {
        RDBMSDisjointnessDependency value = null;

        ArrayList<Object> p1 = null;

        ArrayList<Object> p2 = null;


        try {
            // DependencyAssertion.g:131:3: ( 'disjoint' LPAREN p1= parameter COMMA p2= parameter RPAREN )
            // DependencyAssertion.g:131:5: 'disjoint' LPAREN p1= parameter COMMA p2= parameter RPAREN
            {
            match(input,22,FOLLOW_22_in_disjointness_dependency194); 
            match(input,LPAREN,FOLLOW_LPAREN_in_disjointness_dependency196); 
            pushFollow(FOLLOW_parameter_in_disjointness_dependency200);
            p1=parameter();

            state._fsp--;

            match(input,COMMA,FOLLOW_COMMA_in_disjointness_dependency202); 
            pushFollow(FOLLOW_parameter_in_disjointness_dependency206);
            p2=parameter();

            state._fsp--;

            match(input,RPAREN,FOLLOW_RPAREN_in_disjointness_dependency208); 

                  String mapId1 = (String) p1.get(0);
                  String mapId2 = (String) p2.get(0);
                  Vector<Variable> mapTerms1 = (Vector<Variable>) p1.get(1);
                  Vector<Variable> mapTerms2 = (Vector<Variable>) p2.get(1);
                  
                  if (mapTerms1.size() != mapTerms2.size()) {
                    throw new MappingException("Both mappings in the assertion must " +
                        "have the same number of terms!");
                  }
                  else {
                    MappingController mc = apic.getMappingController();
                    DatasourcesController dc = apic.getDatasourcesController();
                    URI sourceUri = dc.getCurrentDataSource().getSourceID();
                    OBDAMappingAxiom mapAxiom1 = mc.getMapping(sourceUri, mapId1);
                    OBDAMappingAxiom mapAxiom2 = mc.getMapping(sourceUri, mapId2);
                    
                    if (mapAxiom1 != null && mapAxiom2 != null) {
                      value = new RDBMSDisjointnessDependency(sourceUri, mapId1, mapId2,
                        (RDBMSSQLQuery) mapAxiom1.getSourceQuery(),
                        (RDBMSSQLQuery) mapAxiom2.getSourceQuery(), 
                        mapTerms1, mapTerms2);
                    }
                    else {
                      throw new MappingException("Invalid mapping id!");
                    }
                  }
                

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
    // $ANTLR end "disjointness_dependency"


    // $ANTLR start "functional_dependency"
    // DependencyAssertion.g:161:1: functional_dependency returns [RDBMSFunctionalDependency value] : 'functionOf' LPAREN p1= parameter COMMA p2= parameter RPAREN ;
    public final RDBMSFunctionalDependency functional_dependency() throws RecognitionException {
        RDBMSFunctionalDependency value = null;

        ArrayList<Object> p1 = null;

        ArrayList<Object> p2 = null;


        try {
            // DependencyAssertion.g:162:3: ( 'functionOf' LPAREN p1= parameter COMMA p2= parameter RPAREN )
            // DependencyAssertion.g:162:5: 'functionOf' LPAREN p1= parameter COMMA p2= parameter RPAREN
            {
            match(input,23,FOLLOW_23_in_functional_dependency229); 
            match(input,LPAREN,FOLLOW_LPAREN_in_functional_dependency231); 
            pushFollow(FOLLOW_parameter_in_functional_dependency235);
            p1=parameter();

            state._fsp--;

            match(input,COMMA,FOLLOW_COMMA_in_functional_dependency237); 
            pushFollow(FOLLOW_parameter_in_functional_dependency241);
            p2=parameter();

            state._fsp--;

            match(input,RPAREN,FOLLOW_RPAREN_in_functional_dependency243); 

                  String mapId1 = (String) p1.get(0);
                  String mapId2 = (String) p2.get(0);
                  Vector<Variable> mapTerms1 = (Vector<Variable>) p1.get(1);
                  Vector<Variable> mapTerms2 = (Vector<Variable>) p2.get(1);
                  
                  if (mapTerms1.size() != mapTerms2.size()) {
                    throw new MappingException("Both mappings in the assertion must " +
                        "have the same number of terms!");
                  }
                  else {
                    MappingController mc = apic.getMappingController();
                    DatasourcesController dc = apic.getDatasourcesController();
                    URI sourceUri = dc.getCurrentDataSource().getSourceID();
                    OBDAMappingAxiom mapAxiom1 = mc.getMapping(sourceUri, mapId1);
                    OBDAMappingAxiom mapAxiom2 = mc.getMapping(sourceUri, mapId2);
                    
                    if (mapAxiom1 != null && mapAxiom2 != null) {
                      value = new RDBMSFunctionalDependency(sourceUri, mapId1, mapId2,
                        (RDBMSSQLQuery) mapAxiom1.getSourceQuery(),
                        (RDBMSSQLQuery) mapAxiom2.getSourceQuery(), 
                        mapTerms1, mapTerms2);
                    }
                    else {
                      throw new MappingException("Invalid mapping id!");
                    }
                  }
                

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
    // $ANTLR end "functional_dependency"


    // $ANTLR start "parameter"
    // DependencyAssertion.g:192:1: parameter returns [ArrayList<Object> values] : rule LSQ_BRACKET v1= variable ( COMMA v2= variable )* RSQ_BRACKET ;
    public final ArrayList<Object> parameter() throws RecognitionException {
        ArrayList<Object> values = null;

        Variable v1 = null;

        Variable v2 = null;

        String rule5 = null;



          values = new ArrayList<Object>();
          Vector<Variable> terms = new Vector<Variable>();

        try {
            // DependencyAssertion.g:197:3: ( rule LSQ_BRACKET v1= variable ( COMMA v2= variable )* RSQ_BRACKET )
            // DependencyAssertion.g:197:5: rule LSQ_BRACKET v1= variable ( COMMA v2= variable )* RSQ_BRACKET
            {
            pushFollow(FOLLOW_rule_in_parameter267);
            rule5=rule();

            state._fsp--;

             values.add(rule5); 
            match(input,LSQ_BRACKET,FOLLOW_LSQ_BRACKET_in_parameter271); 
            pushFollow(FOLLOW_variable_in_parameter282);
            v1=variable();

            state._fsp--;

             terms.add(v1); 
            // DependencyAssertion.g:199:7: ( COMMA v2= variable )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==COMMA) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // DependencyAssertion.g:199:8: COMMA v2= variable
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_parameter294); 
            	    pushFollow(FOLLOW_variable_in_parameter298);
            	    v2=variable();

            	    state._fsp--;

            	     terms.add(v2); 

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            match(input,RSQ_BRACKET,FOLLOW_RSQ_BRACKET_in_parameter304); 

                  values.add(terms);  
                

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


    // $ANTLR start "rule"
    // DependencyAssertion.g:204:1: rule returns [String value] : ( BODY | HEAD ) DOT mapping_id ;
    public final String rule() throws RecognitionException {
        String value = null;

        DependencyAssertionParser.mapping_id_return mapping_id6 = null;


        try {
            // DependencyAssertion.g:205:3: ( ( BODY | HEAD ) DOT mapping_id )
            // DependencyAssertion.g:205:5: ( BODY | HEAD ) DOT mapping_id
            {
            if ( (input.LA(1)>=BODY && input.LA(1)<=HEAD) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            match(input,DOT,FOLLOW_DOT_in_rule332); 
            pushFollow(FOLLOW_mapping_id_in_rule334);
            mapping_id6=mapping_id();

            state._fsp--;


                  value = (mapping_id6!=null?input.toString(mapping_id6.start,mapping_id6.stop):null);
                

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
    // $ANTLR end "rule"


    // $ANTLR start "variable"
    // DependencyAssertion.g:210:1: variable returns [Variable value] : DOLLAR var_name ;
    public final Variable variable() throws RecognitionException {
        Variable value = null;

        DependencyAssertionParser.var_name_return var_name7 = null;


        try {
            // DependencyAssertion.g:211:3: ( DOLLAR var_name )
            // DependencyAssertion.g:211:5: DOLLAR var_name
            {
            match(input,DOLLAR,FOLLOW_DOLLAR_in_variable354); 
            pushFollow(FOLLOW_var_name_in_variable356);
            var_name7=var_name();

            state._fsp--;


                  value = termFactory.createVariable((var_name7!=null?input.toString(var_name7.start,var_name7.stop):null));
                

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

    public static class mapping_id_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "mapping_id"
    // DependencyAssertion.g:216:1: mapping_id : STRING ;
    public final DependencyAssertionParser.mapping_id_return mapping_id() throws RecognitionException {
        DependencyAssertionParser.mapping_id_return retval = new DependencyAssertionParser.mapping_id_return();
        retval.start = input.LT(1);

        try {
            // DependencyAssertion.g:217:3: ( STRING )
            // DependencyAssertion.g:217:5: STRING
            {
            match(input,STRING,FOLLOW_STRING_in_mapping_id371); 

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
    // $ANTLR end "mapping_id"

    public static class var_name_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "var_name"
    // DependencyAssertion.g:220:1: var_name : STRING ;
    public final DependencyAssertionParser.var_name_return var_name() throws RecognitionException {
        DependencyAssertionParser.var_name_return retval = new DependencyAssertionParser.var_name_return();
        retval.start = input.LT(1);

        try {
            // DependencyAssertion.g:221:3: ( STRING )
            // DependencyAssertion.g:221:5: STRING
            {
            match(input,STRING,FOLLOW_STRING_in_var_name385); 

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

    // Delegated rules


 

    public static final BitSet FOLLOW_prog_in_parse50 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_parse52 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_prog81 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_SEMI_in_prog91 = new BitSet(new long[]{0x0000000000E00000L});
    public static final BitSet FOLLOW_expression_in_prog95 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_inclusion_dependency_in_expression117 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_disjointness_dependency_in_expression128 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functional_dependency_in_expression136 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_21_in_inclusion_dependency159 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_LPAREN_in_inclusion_dependency161 = new BitSet(new long[]{0x0000000000000C00L});
    public static final BitSet FOLLOW_parameter_in_inclusion_dependency165 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_inclusion_dependency167 = new BitSet(new long[]{0x0000000000000C00L});
    public static final BitSet FOLLOW_parameter_in_inclusion_dependency171 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_inclusion_dependency173 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_22_in_disjointness_dependency194 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_LPAREN_in_disjointness_dependency196 = new BitSet(new long[]{0x0000000000000C00L});
    public static final BitSet FOLLOW_parameter_in_disjointness_dependency200 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_disjointness_dependency202 = new BitSet(new long[]{0x0000000000000C00L});
    public static final BitSet FOLLOW_parameter_in_disjointness_dependency206 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_disjointness_dependency208 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_23_in_functional_dependency229 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_LPAREN_in_functional_dependency231 = new BitSet(new long[]{0x0000000000000C00L});
    public static final BitSet FOLLOW_parameter_in_functional_dependency235 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_functional_dependency237 = new BitSet(new long[]{0x0000000000000C00L});
    public static final BitSet FOLLOW_parameter_in_functional_dependency241 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_functional_dependency243 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule_in_parameter267 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LSQ_BRACKET_in_parameter271 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_variable_in_parameter282 = new BitSet(new long[]{0x0000000000000240L});
    public static final BitSet FOLLOW_COMMA_in_parameter294 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_variable_in_parameter298 = new BitSet(new long[]{0x0000000000000240L});
    public static final BitSet FOLLOW_RSQ_BRACKET_in_parameter304 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_rule326 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_DOT_in_rule332 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_mapping_id_in_rule334 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOLLAR_in_variable354 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_var_name_in_variable356 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_mapping_id371 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_var_name385 = new BitSet(new long[]{0x0000000000000002L});

}