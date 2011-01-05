// $ANTLR 3.3 Nov 30, 2010 12:50:56 Datalog.g 2011-01-04 16:36:54

package org.obda.query.tools.parser;

import java.net.URI;
import java.util.Vector;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.TokenStream;

import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.Predicate;
import org.obda.query.domain.Term;
import org.obda.query.domain.imp.AtomImpl;
import org.obda.query.domain.imp.BasicPredicateFactoryImpl;
import org.obda.query.domain.imp.CQIEImpl;
import org.obda.query.domain.imp.DatalogProgramImpl;
import org.obda.query.domain.imp.TermFactoryImpl;
import org.obda.query.domain.imp.VariableImpl;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
public class DatalogParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "PREFIX", "LESS", "GREATER", "BASE", "INV_IMPLIES", "IMPLIES", "COMMA", "CARET", "LPAREN", "RPAREN", "DOLLAR", "QUESTION", "ASTERISK", "STRING_URI", "ID_PLAIN", "STRING_PREFIX", "COLON", "STRING_LITERAL", "STRING_LITERAL2", "REFERENCE", "SEMI", "DOT", "LSQ_BRACKET", "RSQ_BRACKET", "QUOTE_DOUBLE", "QUOTE_SINGLE", "APOSTROPHE", "UNDERSCORE", "DASH", "AMPERSAND", "AT", "EXCLAMATION", "HASH", "PERCENT", "PLUS", "EQUALS", "SLASH", "DOUBLE_SLASH", "BACKSLASH", "TILDE", "ALPHA", "DIGIT", "ALPHANUM", "ID_START", "ID_CORE", "SCHEMA", "URI_PATH", "ID", "WS"
    };
    public static final int EOF=-1;
    public static final int PREFIX=4;
    public static final int LESS=5;
    public static final int GREATER=6;
    public static final int BASE=7;
    public static final int INV_IMPLIES=8;
    public static final int IMPLIES=9;
    public static final int COMMA=10;
    public static final int CARET=11;
    public static final int LPAREN=12;
    public static final int RPAREN=13;
    public static final int DOLLAR=14;
    public static final int QUESTION=15;
    public static final int ASTERISK=16;
    public static final int STRING_URI=17;
    public static final int ID_PLAIN=18;
    public static final int STRING_PREFIX=19;
    public static final int COLON=20;
    public static final int STRING_LITERAL=21;
    public static final int STRING_LITERAL2=22;
    public static final int REFERENCE=23;
    public static final int SEMI=24;
    public static final int DOT=25;
    public static final int LSQ_BRACKET=26;
    public static final int RSQ_BRACKET=27;
    public static final int QUOTE_DOUBLE=28;
    public static final int QUOTE_SINGLE=29;
    public static final int APOSTROPHE=30;
    public static final int UNDERSCORE=31;
    public static final int DASH=32;
    public static final int AMPERSAND=33;
    public static final int AT=34;
    public static final int EXCLAMATION=35;
    public static final int HASH=36;
    public static final int PERCENT=37;
    public static final int PLUS=38;
    public static final int EQUALS=39;
    public static final int SLASH=40;
    public static final int DOUBLE_SLASH=41;
    public static final int BACKSLASH=42;
    public static final int TILDE=43;
    public static final int ALPHA=44;
    public static final int DIGIT=45;
    public static final int ALPHANUM=46;
    public static final int ID_START=47;
    public static final int ID_CORE=48;
    public static final int SCHEMA=49;
    public static final int URI_PATH=50;
    public static final int ID=51;
    public static final int WS=52;

    // delegates
    // delegators


        public DatalogParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public DatalogParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return DatalogParser.tokenNames; }
    public String getGrammarFileName() { return "Datalog.g"; }


    /** Constants */
    private static final String OBDA_DEFAULT_URI = "obda-default";
    private static final String OBDA_BASE_URI = "obda-base";
    private static final String OBDA_SELECT_ALL = "obda-select-all";

    /** Map of directives */
    private HashMap<String, String> directives = new HashMap<String, String>();

    /** A factory to construct the subject and object terms */
    private TermFactoryImpl termFactory = TermFactoryImpl.getInstance();

    /** A factory to construct the predicates */
    private BasicPredicateFactoryImpl predicateFactory = BasicPredicateFactoryImpl.getInstance();

    /** Select all flag */
    private boolean isSelectAll = false;

    public HashMap<String, String> getDirectives() {
        return directives;
    }

    private CQIE identifyAllVariables(CQIE rule) {
        List<Term> variableTerms = new Vector<Term>();
        List<Atom> body = rule.getBody();
        for (Atom atom : body) {
            List<Term> terms = atom.getTerms();
            for (Term term : terms) {
                if (term instanceof VariableImpl)
                    variableTerms.add(term);
            }
        }
        
        Atom head = rule.getHead();
        head.updateTerms(variableTerms);
        rule.updateHead(head);
         
        return rule; 
    }



    // $ANTLR start "parse"
    // Datalog.g:113:1: parse returns [DatalogProgramImpl value] : prog EOF ;
    public final DatalogProgramImpl parse() throws RecognitionException {
        DatalogProgramImpl value = null;

        DatalogProgramImpl prog1 = null;


        try {
            // Datalog.g:114:3: ( prog EOF )
            // Datalog.g:114:5: prog EOF
            {
            pushFollow(FOLLOW_prog_in_parse52);
            prog1=prog();

            state._fsp--;
            if (state.failed) return value;
            match(input,EOF,FOLLOW_EOF_in_parse54); if (state.failed) return value;
            if ( state.backtracking==0 ) {

                    value = prog1;
                  
            }

            }

        }
        catch (RecognitionException e) {
             
                  throw e;
                
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "parse"


    // $ANTLR start "prog"
    // Datalog.g:121:1: prog returns [DatalogProgramImpl value] : ( base )? ( directive )* ( rule )+ ;
    public final DatalogProgramImpl prog() throws RecognitionException {
        DatalogProgramImpl value = null;

        CQIE rule2 = null;



          value = new DatalogProgramImpl();
          CQIE rule = null;

        try {
            // Datalog.g:126:3: ( ( base )? ( directive )* ( rule )+ )
            // Datalog.g:126:5: ( base )? ( directive )* ( rule )+
            {
            // Datalog.g:126:5: ( base )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==BASE) ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // Datalog.g:126:5: base
                    {
                    pushFollow(FOLLOW_base_in_prog86);
                    base();

                    state._fsp--;
                    if (state.failed) return value;

                    }
                    break;

            }

            // Datalog.g:127:5: ( directive )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==PREFIX) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // Datalog.g:127:5: directive
            	    {
            	    pushFollow(FOLLOW_directive_in_prog93);
            	    directive();

            	    state._fsp--;
            	    if (state.failed) return value;

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            // Datalog.g:128:5: ( rule )+
            int cnt3=0;
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>=INV_IMPLIES && LA3_0<=IMPLIES)||(LA3_0>=STRING_URI && LA3_0<=COLON)) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // Datalog.g:128:6: rule
            	    {
            	    pushFollow(FOLLOW_rule_in_prog102);
            	    rule2=rule();

            	    state._fsp--;
            	    if (state.failed) return value;
            	    if ( state.backtracking==0 ) {

            	            rule = rule2;
            	            if (isSelectAll) {
            	              rule = identifyAllVariables(rule);
            	              isSelectAll = false;  
            	            }
            	              
            	            value.appendRule(rule);
            	          
            	    }

            	    }
            	    break;

            	default :
            	    if ( cnt3 >= 1 ) break loop3;
            	    if (state.backtracking>0) {state.failed=true; return value;}
                        EarlyExitException eee =
                            new EarlyExitException(3, input);
                        throw eee;
                }
                cnt3++;
            } while (true);


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


    // $ANTLR start "directive"
    // Datalog.g:139:1: directive : PREFIX prefix LESS uriref GREATER ;
    public final void directive() throws RecognitionException {
        DatalogParser.prefix_return prefix3 = null;

        DatalogParser.uriref_return uriref4 = null;



          String prefix = "";
          String uriref = "";

        try {
            // Datalog.g:144:3: ( PREFIX prefix LESS uriref GREATER )
            // Datalog.g:144:5: PREFIX prefix LESS uriref GREATER
            {
            match(input,PREFIX,FOLLOW_PREFIX_in_directive126); if (state.failed) return ;
            pushFollow(FOLLOW_prefix_in_directive128);
            prefix3=prefix();

            state._fsp--;
            if (state.failed) return ;
            match(input,LESS,FOLLOW_LESS_in_directive130); if (state.failed) return ;
            pushFollow(FOLLOW_uriref_in_directive132);
            uriref4=uriref();

            state._fsp--;
            if (state.failed) return ;
            match(input,GREATER,FOLLOW_GREATER_in_directive134); if (state.failed) return ;
            if ( state.backtracking==0 ) {

                    prefix = (prefix3!=null?input.toString(prefix3.start,prefix3.stop):null);
                    prefix = prefix.substring(0, prefix.length()-1);  
                    if (prefix == null || prefix == "") // if there is no prefix id put the default name.
                      prefix = OBDA_DEFAULT_URI;

                    uriref = (uriref4!=null?input.toString(uriref4.start,uriref4.stop):null);
                    
                    directives.put(prefix, uriref);
                  
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "directive"


    // $ANTLR start "base"
    // Datalog.g:156:1: base : BASE LESS uriref GREATER ;
    public final void base() throws RecognitionException {
        DatalogParser.uriref_return uriref5 = null;



          String prefix = OBDA_BASE_URI; // prefix name for the base
          String uriref = "";

        try {
            // Datalog.g:161:3: ( BASE LESS uriref GREATER )
            // Datalog.g:161:5: BASE LESS uriref GREATER
            {
            match(input,BASE,FOLLOW_BASE_in_base154); if (state.failed) return ;
            match(input,LESS,FOLLOW_LESS_in_base156); if (state.failed) return ;
            pushFollow(FOLLOW_uriref_in_base158);
            uriref5=uriref();

            state._fsp--;
            if (state.failed) return ;
            match(input,GREATER,FOLLOW_GREATER_in_base160); if (state.failed) return ;
            if ( state.backtracking==0 ) {

                    uriref = (uriref5!=null?input.toString(uriref5.start,uriref5.stop):null);      
                    directives.put(prefix, uriref);
                  
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "base"


    // $ANTLR start "rule"
    // Datalog.g:167:1: rule returns [CQIE value] : ( ( ( head )? INV_IMPLIES )=> datalog_syntax_rule | ( ( body )? IMPLIES )=> swirl_syntax_rule );
    public final CQIE rule() throws RecognitionException {
        CQIE value = null;

        CQIE datalog_syntax_rule6 = null;

        CQIE swirl_syntax_rule7 = null;


        try {
            // Datalog.g:168:3: ( ( ( head )? INV_IMPLIES )=> datalog_syntax_rule | ( ( body )? IMPLIES )=> swirl_syntax_rule )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==INV_IMPLIES) && (synpred1_Datalog())) {
                alt4=1;
            }
            else if ( (LA4_0==STRING_URI) ) {
                int LA4_2 = input.LA(2);

                if ( (synpred1_Datalog()) ) {
                    alt4=1;
                }
                else if ( (synpred2_Datalog()) ) {
                    alt4=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return value;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 2, input);

                    throw nvae;
                }
            }
            else if ( (LA4_0==ID_PLAIN) ) {
                int LA4_3 = input.LA(2);

                if ( (synpred1_Datalog()) ) {
                    alt4=1;
                }
                else if ( (synpred2_Datalog()) ) {
                    alt4=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return value;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 3, input);

                    throw nvae;
                }
            }
            else if ( ((LA4_0>=STRING_PREFIX && LA4_0<=COLON)) ) {
                int LA4_4 = input.LA(2);

                if ( (synpred1_Datalog()) ) {
                    alt4=1;
                }
                else if ( (synpred2_Datalog()) ) {
                    alt4=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return value;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 4, input);

                    throw nvae;
                }
            }
            else if ( (LA4_0==IMPLIES) && (synpred2_Datalog())) {
                alt4=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return value;}
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // Datalog.g:168:5: ( ( head )? INV_IMPLIES )=> datalog_syntax_rule
                    {
                    pushFollow(FOLLOW_datalog_syntax_rule_in_rule189);
                    datalog_syntax_rule6=datalog_syntax_rule();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {
                       
                            value = datalog_syntax_rule6;
                          
                    }

                    }
                    break;
                case 2 :
                    // Datalog.g:171:5: ( ( body )? IMPLIES )=> swirl_syntax_rule
                    {
                    pushFollow(FOLLOW_swirl_syntax_rule_in_rule205);
                    swirl_syntax_rule7=swirl_syntax_rule();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {

                            value = swirl_syntax_rule7;
                          
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
        }
        return value;
    }
    // $ANTLR end "rule"


    // $ANTLR start "datalog_syntax_rule"
    // Datalog.g:176:1: datalog_syntax_rule returns [CQIE value] : ( INV_IMPLIES body | datalog_syntax_alt );
    public final CQIE datalog_syntax_rule() throws RecognitionException {
        CQIE value = null;

        Vector<Atom> body8 = null;

        CQIE datalog_syntax_alt9 = null;


        try {
            // Datalog.g:177:3: ( INV_IMPLIES body | datalog_syntax_alt )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==INV_IMPLIES) ) {
                alt5=1;
            }
            else if ( ((LA5_0>=STRING_URI && LA5_0<=COLON)) ) {
                alt5=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return value;}
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // Datalog.g:177:5: INV_IMPLIES body
                    {
                    match(input,INV_IMPLIES,FOLLOW_INV_IMPLIES_in_datalog_syntax_rule224); if (state.failed) return value;
                    pushFollow(FOLLOW_body_in_datalog_syntax_rule226);
                    body8=body();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {

                            value = new CQIEImpl(null, body8, true);
                          
                    }

                    }
                    break;
                case 2 :
                    // Datalog.g:180:5: datalog_syntax_alt
                    {
                    pushFollow(FOLLOW_datalog_syntax_alt_in_datalog_syntax_rule234);
                    datalog_syntax_alt9=datalog_syntax_alt();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {

                            value = datalog_syntax_alt9;
                          
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
        }
        return value;
    }
    // $ANTLR end "datalog_syntax_rule"


    // $ANTLR start "datalog_syntax_alt"
    // Datalog.g:185:1: datalog_syntax_alt returns [CQIE value] : ( ( head INV_IMPLIES body )=> head INV_IMPLIES body | head INV_IMPLIES );
    public final CQIE datalog_syntax_alt() throws RecognitionException {
        CQIE value = null;

        Atom head10 = null;

        Vector<Atom> body11 = null;

        Atom head12 = null;


        try {
            // Datalog.g:186:3: ( ( head INV_IMPLIES body )=> head INV_IMPLIES body | head INV_IMPLIES )
            int alt6=2;
            switch ( input.LA(1) ) {
            case STRING_URI:
                {
                int LA6_1 = input.LA(2);

                if ( (synpred3_Datalog()) ) {
                    alt6=1;
                }
                else if ( (true) ) {
                    alt6=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return value;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 6, 1, input);

                    throw nvae;
                }
                }
                break;
            case ID_PLAIN:
                {
                int LA6_2 = input.LA(2);

                if ( (synpred3_Datalog()) ) {
                    alt6=1;
                }
                else if ( (true) ) {
                    alt6=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return value;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 6, 2, input);

                    throw nvae;
                }
                }
                break;
            case STRING_PREFIX:
            case COLON:
                {
                int LA6_3 = input.LA(2);

                if ( (synpred3_Datalog()) ) {
                    alt6=1;
                }
                else if ( (true) ) {
                    alt6=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return value;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 6, 3, input);

                    throw nvae;
                }
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return value;}
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }

            switch (alt6) {
                case 1 :
                    // Datalog.g:186:5: ( head INV_IMPLIES body )=> head INV_IMPLIES body
                    {
                    pushFollow(FOLLOW_head_in_datalog_syntax_alt262);
                    head10=head();

                    state._fsp--;
                    if (state.failed) return value;
                    match(input,INV_IMPLIES,FOLLOW_INV_IMPLIES_in_datalog_syntax_alt264); if (state.failed) return value;
                    pushFollow(FOLLOW_body_in_datalog_syntax_alt266);
                    body11=body();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {

                            value = new CQIEImpl(head10, body11, true);
                          
                    }

                    }
                    break;
                case 2 :
                    // Datalog.g:189:5: head INV_IMPLIES
                    {
                    pushFollow(FOLLOW_head_in_datalog_syntax_alt274);
                    head12=head();

                    state._fsp--;
                    if (state.failed) return value;
                    match(input,INV_IMPLIES,FOLLOW_INV_IMPLIES_in_datalog_syntax_alt276); if (state.failed) return value;
                    if ( state.backtracking==0 ) {

                            value = new CQIEImpl(head12, null, true);
                          
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
        }
        return value;
    }
    // $ANTLR end "datalog_syntax_alt"


    // $ANTLR start "swirl_syntax_rule"
    // Datalog.g:194:1: swirl_syntax_rule returns [CQIE value] : ( IMPLIES head | swirl_syntax_alt );
    public final CQIE swirl_syntax_rule() throws RecognitionException {
        CQIE value = null;

        Atom head13 = null;

        CQIE swirl_syntax_alt14 = null;


        try {
            // Datalog.g:195:3: ( IMPLIES head | swirl_syntax_alt )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==IMPLIES) ) {
                alt7=1;
            }
            else if ( ((LA7_0>=STRING_URI && LA7_0<=COLON)) ) {
                alt7=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return value;}
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // Datalog.g:195:5: IMPLIES head
                    {
                    match(input,IMPLIES,FOLLOW_IMPLIES_in_swirl_syntax_rule295); if (state.failed) return value;
                    pushFollow(FOLLOW_head_in_swirl_syntax_rule297);
                    head13=head();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {

                            value = new CQIEImpl(head13, null, true);
                          
                    }

                    }
                    break;
                case 2 :
                    // Datalog.g:198:5: swirl_syntax_alt
                    {
                    pushFollow(FOLLOW_swirl_syntax_alt_in_swirl_syntax_rule305);
                    swirl_syntax_alt14=swirl_syntax_alt();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {

                            value = swirl_syntax_alt14;
                          
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
        }
        return value;
    }
    // $ANTLR end "swirl_syntax_rule"


    // $ANTLR start "swirl_syntax_alt"
    // Datalog.g:203:1: swirl_syntax_alt returns [CQIE value] : ( ( body IMPLIES head )=> body IMPLIES head | body IMPLIES );
    public final CQIE swirl_syntax_alt() throws RecognitionException {
        CQIE value = null;

        Atom head15 = null;

        Vector<Atom> body16 = null;

        Vector<Atom> body17 = null;


        try {
            // Datalog.g:204:3: ( ( body IMPLIES head )=> body IMPLIES head | body IMPLIES )
            int alt8=2;
            switch ( input.LA(1) ) {
            case STRING_URI:
                {
                int LA8_1 = input.LA(2);

                if ( (synpred4_Datalog()) ) {
                    alt8=1;
                }
                else if ( (true) ) {
                    alt8=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return value;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 1, input);

                    throw nvae;
                }
                }
                break;
            case ID_PLAIN:
                {
                int LA8_2 = input.LA(2);

                if ( (synpred4_Datalog()) ) {
                    alt8=1;
                }
                else if ( (true) ) {
                    alt8=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return value;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 2, input);

                    throw nvae;
                }
                }
                break;
            case STRING_PREFIX:
            case COLON:
                {
                int LA8_3 = input.LA(2);

                if ( (synpred4_Datalog()) ) {
                    alt8=1;
                }
                else if ( (true) ) {
                    alt8=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return value;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 3, input);

                    throw nvae;
                }
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return value;}
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }

            switch (alt8) {
                case 1 :
                    // Datalog.g:204:5: ( body IMPLIES head )=> body IMPLIES head
                    {
                    pushFollow(FOLLOW_body_in_swirl_syntax_alt333);
                    body16=body();

                    state._fsp--;
                    if (state.failed) return value;
                    match(input,IMPLIES,FOLLOW_IMPLIES_in_swirl_syntax_alt335); if (state.failed) return value;
                    pushFollow(FOLLOW_head_in_swirl_syntax_alt337);
                    head15=head();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {

                            value = new CQIEImpl(head15, body16, true);
                          
                    }

                    }
                    break;
                case 2 :
                    // Datalog.g:207:5: body IMPLIES
                    {
                    pushFollow(FOLLOW_body_in_swirl_syntax_alt345);
                    body17=body();

                    state._fsp--;
                    if (state.failed) return value;
                    match(input,IMPLIES,FOLLOW_IMPLIES_in_swirl_syntax_alt347); if (state.failed) return value;
                    if ( state.backtracking==0 ) {

                            value = new CQIEImpl(null, body17, true);
                          
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
        }
        return value;
    }
    // $ANTLR end "swirl_syntax_alt"


    // $ANTLR start "head"
    // Datalog.g:212:1: head returns [Atom value] : atom ;
    public final Atom head() throws RecognitionException {
        Atom value = null;

        Atom atom18 = null;



          value = null;

        try {
            // Datalog.g:216:3: ( atom )
            // Datalog.g:216:5: atom
            {
            pushFollow(FOLLOW_atom_in_head371);
            atom18=atom();

            state._fsp--;
            if (state.failed) return value;
            if ( state.backtracking==0 ) {

                    value = atom18;
                  
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
    // $ANTLR end "head"


    // $ANTLR start "body"
    // Datalog.g:221:1: body returns [Vector<Atom> value] : a1= atom ( ( COMMA | CARET ) a2= atom )* ;
    public final Vector<Atom> body() throws RecognitionException {
        Vector<Atom> value = null;

        Atom a1 = null;

        Atom a2 = null;



          value = new Vector<Atom>();

        try {
            // Datalog.g:225:3: (a1= atom ( ( COMMA | CARET ) a2= atom )* )
            // Datalog.g:225:5: a1= atom ( ( COMMA | CARET ) a2= atom )*
            {
            pushFollow(FOLLOW_atom_in_body397);
            a1=atom();

            state._fsp--;
            if (state.failed) return value;
            if ( state.backtracking==0 ) {
               value.add(a1); 
            }
            // Datalog.g:225:40: ( ( COMMA | CARET ) a2= atom )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( ((LA9_0>=COMMA && LA9_0<=CARET)) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // Datalog.g:225:41: ( COMMA | CARET ) a2= atom
            	    {
            	    if ( (input.LA(1)>=COMMA && input.LA(1)<=CARET) ) {
            	        input.consume();
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return value;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_atom_in_body410);
            	    a2=atom();

            	    state._fsp--;
            	    if (state.failed) return value;
            	    if ( state.backtracking==0 ) {
            	       value.add(a2); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop9;
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
        return value;
    }
    // $ANTLR end "body"


    // $ANTLR start "atom"
    // Datalog.g:228:1: atom returns [Atom value] : predicate LPAREN ( terms )? RPAREN ;
    public final Atom atom() throws RecognitionException {
        Atom value = null;

        String predicate19 = null;

        Vector<Term> terms20 = null;


        try {
            // Datalog.g:229:3: ( predicate LPAREN ( terms )? RPAREN )
            // Datalog.g:229:5: predicate LPAREN ( terms )? RPAREN
            {
            pushFollow(FOLLOW_predicate_in_atom431);
            predicate19=predicate();

            state._fsp--;
            if (state.failed) return value;
            match(input,LPAREN,FOLLOW_LPAREN_in_atom433); if (state.failed) return value;
            // Datalog.g:229:22: ( terms )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( ((LA10_0>=DOLLAR && LA10_0<=STRING_LITERAL2)) ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // Datalog.g:229:22: terms
                    {
                    pushFollow(FOLLOW_terms_in_atom435);
                    terms20=terms();

                    state._fsp--;
                    if (state.failed) return value;

                    }
                    break;

            }

            match(input,RPAREN,FOLLOW_RPAREN_in_atom438); if (state.failed) return value;
            if ( state.backtracking==0 ) {

                    URI uri = URI.create(predicate19);
                    
                    Vector<Term> elements = terms20;
                    if (elements == null)
                      elements = new Vector<Term>();
                    Predicate predicate = predicateFactory.createPredicate(uri, elements.size());
                    
                    Vector<Term> terms = terms20;
                    if (terms == null)
                      terms = new Vector<Term>();
                      
                    value = new AtomImpl(predicate, terms);
                  
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
    // $ANTLR end "atom"


    // $ANTLR start "predicate"
    // Datalog.g:245:1: predicate returns [String value] : ( full_name | plain_name | qualified_name );
    public final String predicate() throws RecognitionException {
        String value = null;

        String full_name21 = null;

        String plain_name22 = null;

        String qualified_name23 = null;


        try {
            // Datalog.g:246:3: ( full_name | plain_name | qualified_name )
            int alt11=3;
            switch ( input.LA(1) ) {
            case STRING_URI:
                {
                alt11=1;
                }
                break;
            case ID_PLAIN:
                {
                alt11=2;
                }
                break;
            case STRING_PREFIX:
            case COLON:
                {
                alt11=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return value;}
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }

            switch (alt11) {
                case 1 :
                    // Datalog.g:246:5: full_name
                    {
                    pushFollow(FOLLOW_full_name_in_predicate458);
                    full_name21=full_name();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {
                       value = full_name21; 
                    }

                    }
                    break;
                case 2 :
                    // Datalog.g:247:5: plain_name
                    {
                    pushFollow(FOLLOW_plain_name_in_predicate471);
                    plain_name22=plain_name();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {
                       value = plain_name22; 
                    }

                    }
                    break;
                case 3 :
                    // Datalog.g:248:5: qualified_name
                    {
                    pushFollow(FOLLOW_qualified_name_in_predicate483);
                    qualified_name23=qualified_name();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {
                       value = qualified_name23; 
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
        }
        return value;
    }
    // $ANTLR end "predicate"


    // $ANTLR start "terms"
    // Datalog.g:251:1: terms returns [Vector<Term> elements] : t1= term ( COMMA t2= term )* ;
    public final Vector<Term> terms() throws RecognitionException {
        Vector<Term> elements = null;

        Term t1 = null;

        Term t2 = null;



          elements = new Vector<Term>();

        try {
            // Datalog.g:255:3: (t1= term ( COMMA t2= term )* )
            // Datalog.g:255:5: t1= term ( COMMA t2= term )*
            {
            pushFollow(FOLLOW_term_in_terms509);
            t1=term();

            state._fsp--;
            if (state.failed) return elements;
            if ( state.backtracking==0 ) {
               elements.add(t1); 
            }
            // Datalog.g:255:43: ( COMMA t2= term )*
            loop12:
            do {
                int alt12=2;
                int LA12_0 = input.LA(1);

                if ( (LA12_0==COMMA) ) {
                    alt12=1;
                }


                switch (alt12) {
            	case 1 :
            	    // Datalog.g:255:44: COMMA t2= term
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_terms514); if (state.failed) return elements;
            	    pushFollow(FOLLOW_term_in_terms518);
            	    t2=term();

            	    state._fsp--;
            	    if (state.failed) return elements;
            	    if ( state.backtracking==0 ) {
            	       elements.add(t2); 
            	    }

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
        }
        return elements;
    }
    // $ANTLR end "terms"


    // $ANTLR start "term"
    // Datalog.g:258:1: term returns [Term value] : ( variable_term | literal_term | object_term | uri_term );
    public final Term term() throws RecognitionException {
        Term value = null;

        Term variable_term24 = null;

        Term literal_term25 = null;

        Term object_term26 = null;

        Term uri_term27 = null;


        try {
            // Datalog.g:259:3: ( variable_term | literal_term | object_term | uri_term )
            int alt13=4;
            switch ( input.LA(1) ) {
            case DOLLAR:
            case QUESTION:
            case ASTERISK:
                {
                alt13=1;
                }
                break;
            case STRING_LITERAL:
            case STRING_LITERAL2:
                {
                alt13=2;
                }
                break;
            case STRING_URI:
                {
                int LA13_3 = input.LA(2);

                if ( (LA13_3==LPAREN) ) {
                    alt13=3;
                }
                else if ( (LA13_3==COMMA||LA13_3==RPAREN) ) {
                    alt13=4;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return value;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 3, input);

                    throw nvae;
                }
                }
                break;
            case ID_PLAIN:
            case STRING_PREFIX:
            case COLON:
                {
                alt13=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return value;}
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }

            switch (alt13) {
                case 1 :
                    // Datalog.g:259:5: variable_term
                    {
                    pushFollow(FOLLOW_variable_term_in_term541);
                    variable_term24=variable_term();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {
                       value = variable_term24; 
                    }

                    }
                    break;
                case 2 :
                    // Datalog.g:260:5: literal_term
                    {
                    pushFollow(FOLLOW_literal_term_in_term549);
                    literal_term25=literal_term();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {
                       value = literal_term25; 
                    }

                    }
                    break;
                case 3 :
                    // Datalog.g:261:5: object_term
                    {
                    pushFollow(FOLLOW_object_term_in_term558);
                    object_term26=object_term();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {
                       value = object_term26; 
                    }

                    }
                    break;
                case 4 :
                    // Datalog.g:262:5: uri_term
                    {
                    pushFollow(FOLLOW_uri_term_in_term568);
                    uri_term27=uri_term();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {
                       value = uri_term27; 
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
        }
        return value;
    }
    // $ANTLR end "term"


    // $ANTLR start "variable_term"
    // Datalog.g:265:1: variable_term returns [Term value] : ( ( DOLLAR | QUESTION ) id | ASTERISK );
    public final Term variable_term() throws RecognitionException {
        Term value = null;

        DatalogParser.id_return id28 = null;


        try {
            // Datalog.g:266:3: ( ( DOLLAR | QUESTION ) id | ASTERISK )
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( ((LA14_0>=DOLLAR && LA14_0<=QUESTION)) ) {
                alt14=1;
            }
            else if ( (LA14_0==ASTERISK) ) {
                alt14=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return value;}
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;
            }
            switch (alt14) {
                case 1 :
                    // Datalog.g:266:5: ( DOLLAR | QUESTION ) id
                    {
                    if ( (input.LA(1)>=DOLLAR && input.LA(1)<=QUESTION) ) {
                        input.consume();
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return value;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    pushFollow(FOLLOW_id_in_variable_term599);
                    id28=id();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {
                       
                            value = termFactory.createVariable((id28!=null?input.toString(id28.start,id28.stop):null));
                          
                    }

                    }
                    break;
                case 2 :
                    // Datalog.g:269:5: ASTERISK
                    {
                    match(input,ASTERISK,FOLLOW_ASTERISK_in_variable_term607); if (state.failed) return value;
                    if ( state.backtracking==0 ) {

                            value = termFactory.createVariable(OBDA_SELECT_ALL);
                            isSelectAll = true;
                          
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
        }
        return value;
    }
    // $ANTLR end "variable_term"


    // $ANTLR start "literal_term"
    // Datalog.g:275:1: literal_term returns [Term value] : string ;
    public final Term literal_term() throws RecognitionException {
        Term value = null;

        DatalogParser.string_return string29 = null;



          String literal = "";

        try {
            // Datalog.g:279:3: ( string )
            // Datalog.g:279:5: string
            {
            pushFollow(FOLLOW_string_in_literal_term631);
            string29=string();

            state._fsp--;
            if (state.failed) return value;
            if ( state.backtracking==0 ) {

                    literal = (string29!=null?input.toString(string29.start,string29.stop):null);
                    literal = literal.substring(1, literal.length()-1); // removes the quote signs.
                    value = termFactory.createValueConstant(literal);
                  
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
    // $ANTLR end "literal_term"


    // $ANTLR start "object_term"
    // Datalog.g:286:1: object_term returns [Term value] : function LPAREN terms RPAREN ;
    public final Term object_term() throws RecognitionException {
        Term value = null;

        String function30 = null;

        Vector<Term> terms31 = null;


        try {
            // Datalog.g:287:3: ( function LPAREN terms RPAREN )
            // Datalog.g:287:5: function LPAREN terms RPAREN
            {
            pushFollow(FOLLOW_function_in_object_term653);
            function30=function();

            state._fsp--;
            if (state.failed) return value;
            match(input,LPAREN,FOLLOW_LPAREN_in_object_term655); if (state.failed) return value;
            pushFollow(FOLLOW_terms_in_object_term657);
            terms31=terms();

            state._fsp--;
            if (state.failed) return value;
            match(input,RPAREN,FOLLOW_RPAREN_in_object_term659); if (state.failed) return value;
            if ( state.backtracking==0 ) {

                    URI uri = URI.create(function30);
                    Predicate fs = predicateFactory.createPredicate(uri, terms31.size());
                    value = termFactory.createFunctionalTerm(fs, terms31);
                  
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
    // $ANTLR end "object_term"


    // $ANTLR start "uri_term"
    // Datalog.g:294:1: uri_term returns [Term value] : uri ;
    public final Term uri_term() throws RecognitionException {
        Term value = null;

        DatalogParser.uri_return uri32 = null;



          String uriText = "";

        try {
            // Datalog.g:298:3: ( uri )
            // Datalog.g:298:5: uri
            {
            pushFollow(FOLLOW_uri_in_uri_term685);
            uri32=uri();

            state._fsp--;
            if (state.failed) return value;
            if ( state.backtracking==0 ) {
               
                    uriText = (uri32!=null?input.toString(uri32.start,uri32.stop):null);      
                    URI uri = URI.create(uriText);
                    value = termFactory.createURIConstant(uri);
                  
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
    // $ANTLR end "uri_term"


    // $ANTLR start "function"
    // Datalog.g:305:1: function returns [String value] : ( full_name | plain_name | qualified_name );
    public final String function() throws RecognitionException {
        String value = null;

        String full_name33 = null;

        String plain_name34 = null;

        String qualified_name35 = null;


        try {
            // Datalog.g:306:3: ( full_name | plain_name | qualified_name )
            int alt15=3;
            switch ( input.LA(1) ) {
            case STRING_URI:
                {
                alt15=1;
                }
                break;
            case ID_PLAIN:
                {
                alt15=2;
                }
                break;
            case STRING_PREFIX:
            case COLON:
                {
                alt15=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return value;}
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;
            }

            switch (alt15) {
                case 1 :
                    // Datalog.g:306:5: full_name
                    {
                    pushFollow(FOLLOW_full_name_in_function706);
                    full_name33=full_name();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {
                       value = full_name33; 
                    }

                    }
                    break;
                case 2 :
                    // Datalog.g:307:5: plain_name
                    {
                    pushFollow(FOLLOW_plain_name_in_function719);
                    plain_name34=plain_name();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {
                       value = plain_name34; 
                    }

                    }
                    break;
                case 3 :
                    // Datalog.g:308:5: qualified_name
                    {
                    pushFollow(FOLLOW_qualified_name_in_function731);
                    qualified_name35=qualified_name();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {
                       value = qualified_name35; 
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
        }
        return value;
    }
    // $ANTLR end "function"


    // $ANTLR start "qualified_name"
    // Datalog.g:311:1: qualified_name returns [String value] : prefix id ;
    public final String qualified_name() throws RecognitionException {
        String value = null;

        DatalogParser.prefix_return prefix36 = null;

        DatalogParser.id_return id37 = null;



          String prefix = "";
          String uriref = "";

        try {
            // Datalog.g:316:3: ( prefix id )
            // Datalog.g:316:5: prefix id
            {
            pushFollow(FOLLOW_prefix_in_qualified_name755);
            prefix36=prefix();

            state._fsp--;
            if (state.failed) return value;
            pushFollow(FOLLOW_id_in_qualified_name757);
            id37=id();

            state._fsp--;
            if (state.failed) return value;
            if ( state.backtracking==0 ) {

                    prefix = (prefix36!=null?input.toString(prefix36.start,prefix36.stop):null);
                    prefix = prefix.substring(0, prefix.length()-1);
                    if (prefix != null)
                      uriref = directives.get(prefix);
                    else
                      uriref = directives.get(OBDA_DEFAULT_URI);
                    
                    String uri = uriref + (id37!=null?input.toString(id37.start,id37.stop):null); // creates the complete Uri string
                    value = uri;
                  
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
    // $ANTLR end "qualified_name"


    // $ANTLR start "plain_name"
    // Datalog.g:329:1: plain_name returns [String value] : id ;
    public final String plain_name() throws RecognitionException {
        String value = null;

        DatalogParser.id_return id38 = null;


        try {
            // Datalog.g:330:3: ( id )
            // Datalog.g:330:5: id
            {
            pushFollow(FOLLOW_id_in_plain_name776);
            id38=id();

            state._fsp--;
            if (state.failed) return value;
            if ( state.backtracking==0 ) {

                    String uriref = directives.get(OBDA_BASE_URI);      
                    String uri = uriref + (id38!=null?input.toString(id38.start,id38.stop):null); // creates the complete Uri string
                    value = uri;
                  
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
    // $ANTLR end "plain_name"


    // $ANTLR start "full_name"
    // Datalog.g:337:1: full_name returns [String value] : uri ;
    public final String full_name() throws RecognitionException {
        String value = null;

        DatalogParser.uri_return uri39 = null;


        try {
            // Datalog.g:338:3: ( uri )
            // Datalog.g:338:5: uri
            {
            pushFollow(FOLLOW_uri_in_full_name795);
            uri39=uri();

            state._fsp--;
            if (state.failed) return value;
            if ( state.backtracking==0 ) {

                    value = (uri39!=null?input.toString(uri39.start,uri39.stop):null);
                  
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
    // $ANTLR end "full_name"

    public static class uri_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "uri"
    // Datalog.g:343:1: uri : STRING_URI ;
    public final DatalogParser.uri_return uri() throws RecognitionException {
        DatalogParser.uri_return retval = new DatalogParser.uri_return();
        retval.start = input.LT(1);

        try {
            // Datalog.g:344:3: ( STRING_URI )
            // Datalog.g:344:5: STRING_URI
            {
            match(input,STRING_URI,FOLLOW_STRING_URI_in_uri810); if (state.failed) return retval;

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
    // $ANTLR end "uri"

    public static class uriref_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "uriref"
    // Datalog.g:347:1: uriref : STRING_URI ;
    public final DatalogParser.uriref_return uriref() throws RecognitionException {
        DatalogParser.uriref_return retval = new DatalogParser.uriref_return();
        retval.start = input.LT(1);

        try {
            // Datalog.g:348:3: ( STRING_URI )
            // Datalog.g:348:5: STRING_URI
            {
            match(input,STRING_URI,FOLLOW_STRING_URI_in_uriref823); if (state.failed) return retval;

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
    // $ANTLR end "uriref"

    public static class id_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "id"
    // Datalog.g:351:1: id : ID_PLAIN ;
    public final DatalogParser.id_return id() throws RecognitionException {
        DatalogParser.id_return retval = new DatalogParser.id_return();
        retval.start = input.LT(1);

        try {
            // Datalog.g:352:3: ( ID_PLAIN )
            // Datalog.g:352:5: ID_PLAIN
            {
            match(input,ID_PLAIN,FOLLOW_ID_PLAIN_in_id836); if (state.failed) return retval;

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
    // $ANTLR end "id"

    public static class prefix_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "prefix"
    // Datalog.g:355:1: prefix : ( STRING_PREFIX | COLON );
    public final DatalogParser.prefix_return prefix() throws RecognitionException {
        DatalogParser.prefix_return retval = new DatalogParser.prefix_return();
        retval.start = input.LT(1);

        try {
            // Datalog.g:356:3: ( STRING_PREFIX | COLON )
            // Datalog.g:
            {
            if ( (input.LA(1)>=STRING_PREFIX && input.LA(1)<=COLON) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
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
    // $ANTLR end "prefix"

    public static class string_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "string"
    // Datalog.g:360:1: string : ( STRING_LITERAL | STRING_LITERAL2 );
    public final DatalogParser.string_return string() throws RecognitionException {
        DatalogParser.string_return retval = new DatalogParser.string_return();
        retval.start = input.LT(1);

        try {
            // Datalog.g:361:3: ( STRING_LITERAL | STRING_LITERAL2 )
            // Datalog.g:
            {
            if ( (input.LA(1)>=STRING_LITERAL && input.LA(1)<=STRING_LITERAL2) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
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
    // $ANTLR end "string"

    // $ANTLR start synpred1_Datalog
    public final void synpred1_Datalog_fragment() throws RecognitionException {   
        // Datalog.g:168:5: ( ( head )? INV_IMPLIES )
        // Datalog.g:168:6: ( head )? INV_IMPLIES
        {
        // Datalog.g:168:6: ( head )?
        int alt16=2;
        int LA16_0 = input.LA(1);

        if ( ((LA16_0>=STRING_URI && LA16_0<=COLON)) ) {
            alt16=1;
        }
        switch (alt16) {
            case 1 :
                // Datalog.g:168:6: head
                {
                pushFollow(FOLLOW_head_in_synpred1_Datalog182);
                head();

                state._fsp--;
                if (state.failed) return ;

                }
                break;

        }

        match(input,INV_IMPLIES,FOLLOW_INV_IMPLIES_in_synpred1_Datalog185); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Datalog

    // $ANTLR start synpred2_Datalog
    public final void synpred2_Datalog_fragment() throws RecognitionException {   
        // Datalog.g:171:5: ( ( body )? IMPLIES )
        // Datalog.g:171:6: ( body )? IMPLIES
        {
        // Datalog.g:171:6: ( body )?
        int alt17=2;
        int LA17_0 = input.LA(1);

        if ( ((LA17_0>=STRING_URI && LA17_0<=COLON)) ) {
            alt17=1;
        }
        switch (alt17) {
            case 1 :
                // Datalog.g:171:6: body
                {
                pushFollow(FOLLOW_body_in_synpred2_Datalog198);
                body();

                state._fsp--;
                if (state.failed) return ;

                }
                break;

        }

        match(input,IMPLIES,FOLLOW_IMPLIES_in_synpred2_Datalog201); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Datalog

    // $ANTLR start synpred3_Datalog
    public final void synpred3_Datalog_fragment() throws RecognitionException {   
        // Datalog.g:186:5: ( head INV_IMPLIES body )
        // Datalog.g:186:6: head INV_IMPLIES body
        {
        pushFollow(FOLLOW_head_in_synpred3_Datalog254);
        head();

        state._fsp--;
        if (state.failed) return ;
        match(input,INV_IMPLIES,FOLLOW_INV_IMPLIES_in_synpred3_Datalog256); if (state.failed) return ;
        pushFollow(FOLLOW_body_in_synpred3_Datalog258);
        body();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_Datalog

    // $ANTLR start synpred4_Datalog
    public final void synpred4_Datalog_fragment() throws RecognitionException {   
        // Datalog.g:204:5: ( body IMPLIES head )
        // Datalog.g:204:6: body IMPLIES head
        {
        pushFollow(FOLLOW_body_in_synpred4_Datalog325);
        body();

        state._fsp--;
        if (state.failed) return ;
        match(input,IMPLIES,FOLLOW_IMPLIES_in_synpred4_Datalog327); if (state.failed) return ;
        pushFollow(FOLLOW_head_in_synpred4_Datalog329);
        head();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_Datalog

    // Delegated rules

    public final boolean synpred4_Datalog() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred4_Datalog_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred1_Datalog() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_Datalog_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred3_Datalog() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred3_Datalog_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred2_Datalog() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred2_Datalog_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


 

    public static final BitSet FOLLOW_prog_in_parse52 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_parse54 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_base_in_prog86 = new BitSet(new long[]{0x00000000001E0310L});
    public static final BitSet FOLLOW_directive_in_prog93 = new BitSet(new long[]{0x00000000001E0310L});
    public static final BitSet FOLLOW_rule_in_prog102 = new BitSet(new long[]{0x00000000001E0312L});
    public static final BitSet FOLLOW_PREFIX_in_directive126 = new BitSet(new long[]{0x00000000001E0000L});
    public static final BitSet FOLLOW_prefix_in_directive128 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_LESS_in_directive130 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_uriref_in_directive132 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_GREATER_in_directive134 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BASE_in_base154 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_LESS_in_base156 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_uriref_in_base158 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_GREATER_in_base160 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_datalog_syntax_rule_in_rule189 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_swirl_syntax_rule_in_rule205 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INV_IMPLIES_in_datalog_syntax_rule224 = new BitSet(new long[]{0x00000000001E0000L});
    public static final BitSet FOLLOW_body_in_datalog_syntax_rule226 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_datalog_syntax_alt_in_datalog_syntax_rule234 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_head_in_datalog_syntax_alt262 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_INV_IMPLIES_in_datalog_syntax_alt264 = new BitSet(new long[]{0x00000000001E0000L});
    public static final BitSet FOLLOW_body_in_datalog_syntax_alt266 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_head_in_datalog_syntax_alt274 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_INV_IMPLIES_in_datalog_syntax_alt276 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPLIES_in_swirl_syntax_rule295 = new BitSet(new long[]{0x00000000001E0000L});
    public static final BitSet FOLLOW_head_in_swirl_syntax_rule297 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_swirl_syntax_alt_in_swirl_syntax_rule305 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_body_in_swirl_syntax_alt333 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_IMPLIES_in_swirl_syntax_alt335 = new BitSet(new long[]{0x00000000001E0000L});
    public static final BitSet FOLLOW_head_in_swirl_syntax_alt337 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_body_in_swirl_syntax_alt345 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_IMPLIES_in_swirl_syntax_alt347 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_atom_in_head371 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_atom_in_body397 = new BitSet(new long[]{0x0000000000000C02L});
    public static final BitSet FOLLOW_set_in_body402 = new BitSet(new long[]{0x00000000001E0000L});
    public static final BitSet FOLLOW_atom_in_body410 = new BitSet(new long[]{0x0000000000000C02L});
    public static final BitSet FOLLOW_predicate_in_atom431 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LPAREN_in_atom433 = new BitSet(new long[]{0x00000000007FE000L});
    public static final BitSet FOLLOW_terms_in_atom435 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RPAREN_in_atom438 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_full_name_in_predicate458 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_plain_name_in_predicate471 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualified_name_in_predicate483 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_terms509 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_COMMA_in_terms514 = new BitSet(new long[]{0x00000000007FC000L});
    public static final BitSet FOLLOW_term_in_terms518 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_variable_term_in_term541 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_term_in_term549 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_object_term_in_term558 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_uri_term_in_term568 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_variable_term593 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_id_in_variable_term599 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASTERISK_in_variable_term607 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_string_in_literal_term631 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_in_object_term653 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LPAREN_in_object_term655 = new BitSet(new long[]{0x00000000007FC000L});
    public static final BitSet FOLLOW_terms_in_object_term657 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RPAREN_in_object_term659 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_uri_in_uri_term685 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_full_name_in_function706 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_plain_name_in_function719 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualified_name_in_function731 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_prefix_in_qualified_name755 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_id_in_qualified_name757 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_plain_name776 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_uri_in_full_name795 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_URI_in_uri810 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_URI_in_uriref823 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_PLAIN_in_id836 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_prefix0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_string0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_head_in_synpred1_Datalog182 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_INV_IMPLIES_in_synpred1_Datalog185 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_body_in_synpred2_Datalog198 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_IMPLIES_in_synpred2_Datalog201 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_head_in_synpred3_Datalog254 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_INV_IMPLIES_in_synpred3_Datalog256 = new BitSet(new long[]{0x00000000001E0000L});
    public static final BitSet FOLLOW_body_in_synpred3_Datalog258 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_body_in_synpred4_Datalog325 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_IMPLIES_in_synpred4_Datalog327 = new BitSet(new long[]{0x00000000001E0000L});
    public static final BitSet FOLLOW_head_in_synpred4_Datalog329 = new BitSet(new long[]{0x0000000000000002L});

}