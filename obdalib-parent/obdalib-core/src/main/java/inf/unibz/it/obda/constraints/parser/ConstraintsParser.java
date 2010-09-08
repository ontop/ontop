// $ANTLR 3.1.2 /home/obda/Constraints.g 2009-11-14 10:44:45

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
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.domain.TypedConstantTerm;
import inf.unibz.it.ucq.domain.VariableTerm;
import inf.unibz.it.ucq.typing.CheckOperationTerm;
import inf.unibz.it.ucq.typing.UnknownXSDTypeException;
import inf.unibz.it.ucq.typing.XSDTypingController;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.DFA;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

import com.sun.msv.datatype.xsd.XSDatatype;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class ConstraintsParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "NUMBER", "BOOL", "ALPHAVAR", "ALPHA", "DIGIT", "CHAR", "INTEGER", "WS", "'CHECK'", "','", "'UNIQUE ('", "')'", "'('", "') REFERENCES'", "'PRIMARY KEY ('", "'\\''", "'$'", "'<'", "'>'", "'='", "'<='", "'>='"
    };
    public static final int INTEGER=10;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int T__22=22;
    public static final int T__21=21;
    public static final int T__20=20;
    public static final int NUMBER=4;
    public static final int BOOL=5;
    public static final int CHAR=9;
    public static final int EOF=-1;
    public static final int ALPHA=7;
    public static final int T__19=19;
    public static final int WS=11;
    public static final int T__16=16;
    public static final int T__15=15;
    public static final int T__18=18;
    public static final int T__17=17;
    public static final int T__12=12;
    public static final int T__14=14;
    public static final int T__13=13;
    public static final int ALPHAVAR=6;
    public static final int DIGIT=8;

    // delegates
    // delegators


        public ConstraintsParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public ConstraintsParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return ConstraintsParser.tokenNames; }
    public String getGrammarFileName() { return "/home/obda/Constraints.g"; }



        	    private List<String> errors = new LinkedList<String>();
        	    public void displayRecognitionError(String[] tokenNames,
        	                                        RecognitionException e) {
        	        String hdr = getErrorHeader(e);
        	        String msg = getErrorMessage(e, tokenNames);
        	        errors.add(hdr + " " + msg);
        	    }
        	    public List<String> getErrors() {
        	        return errors;
        	    }


            	boolean error1 = false;

            	ArrayList<QueryTerm> term_collector = new ArrayList<QueryTerm>();
            	ArrayList<QueryTerm> terms_ofMappingOne= new ArrayList<QueryTerm>();
            	ArrayList<QueryTerm>terms_ofMappingTwo = new ArrayList<QueryTerm>();
            	String tmpId = null;
            	String idForMappingOne = null;
            	String idForMappingTwo = null;
            	QueryTerm firstTerm = null;
            	QueryTerm secondTerm = null;
            	Vector<CheckOperationTerm> checks = new Vector<CheckOperationTerm>();
            	
            	private APIController apic = null;
            	
            	AbstractConstraintAssertion constraint = null;

            	public void resetErrorFlag() {
            		error1 = false;
            	}
            	
            	public boolean getErrorFlag() {
            		return error1;
            	}
            	
            	public AbstractConstraintAssertion getConstraintAssertion() {
            		return constraint;
            	}
            	
            	public void setController(APIController con){
            		apic = con;
            	}

            	private void resetValues(){
            		terms_ofMappingOne = null;
            		terms_ofMappingTwo =null;
            		idForMappingOne = null;
            		idForMappingTwo = null;
            	}



    // $ANTLR start "parse"
    // /home/obda/Constraints.g:120:1: parse returns [boolean value] : prog EOF ;
    public final boolean parse() throws RecognitionException {
        boolean value = false;

        try {
            // /home/obda/Constraints.g:121:1: ( prog EOF )
            // /home/obda/Constraints.g:121:3: prog EOF
            {
            pushFollow(FOLLOW_prog_in_parse47);
            prog();

            state._fsp--;

            match(input,EOF,FOLLOW_EOF_in_parse49); 
             
            		value = !error1; 
            		

            }

        }
        catch (RecognitionException ex) {
             
            		value = false; 
            		throw ex; 
            	
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "parse"


    // $ANTLR start "prog"
    // /home/obda/Constraints.g:129:1: prog : exp ;
    public final void prog() throws RecognitionException {
        try {
            // /home/obda/Constraints.g:129:6: ( exp )
            // /home/obda/Constraints.g:129:9: exp
            {
            pushFollow(FOLLOW_exp_in_prog69);
            exp();

            state._fsp--;


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
    // $ANTLR end "prog"


    // $ANTLR start "exp"
    // /home/obda/Constraints.g:130:1: exp : ( exp1 | exp2 | exp3 | exp4 );
    public final void exp() throws RecognitionException {
        try {
            // /home/obda/Constraints.g:130:6: ( exp1 | exp2 | exp3 | exp4 )
            int alt1=4;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==ALPHAVAR) ) {
                switch ( input.LA(2) ) {
                case 12:
                    {
                    alt1=1;
                    }
                    break;
                case 16:
                    {
                    alt1=3;
                    }
                    break;
                case 14:
                    {
                    alt1=2;
                    }
                    break;
                case 18:
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
                    // /home/obda/Constraints.g:130:8: exp1
                    {
                    pushFollow(FOLLOW_exp1_in_exp77);
                    exp1();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /home/obda/Constraints.g:130:13: exp2
                    {
                    pushFollow(FOLLOW_exp2_in_exp79);
                    exp2();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // /home/obda/Constraints.g:130:18: exp3
                    {
                    pushFollow(FOLLOW_exp3_in_exp81);
                    exp3();

                    state._fsp--;


                    }
                    break;
                case 4 :
                    // /home/obda/Constraints.g:130:23: exp4
                    {
                    pushFollow(FOLLOW_exp4_in_exp83);
                    exp4();

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
        }
        return ;
    }
    // $ANTLR end "exp"


    // $ANTLR start "exp1"
    // /home/obda/Constraints.g:131:1: exp1 : cc ;
    public final void exp1() throws RecognitionException {
        try {
            // /home/obda/Constraints.g:131:6: ( cc )
            // /home/obda/Constraints.g:131:8: cc
            {
            pushFollow(FOLLOW_cc_in_exp190);
            cc();

            state._fsp--;


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
    // $ANTLR end "exp1"


    // $ANTLR start "exp2"
    // /home/obda/Constraints.g:132:1: exp2 : uni ;
    public final void exp2() throws RecognitionException {
        try {
            // /home/obda/Constraints.g:132:6: ( uni )
            // /home/obda/Constraints.g:132:8: uni
            {
            pushFollow(FOLLOW_uni_in_exp297);
            uni();

            state._fsp--;


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
    // $ANTLR end "exp2"


    // $ANTLR start "exp3"
    // /home/obda/Constraints.g:133:1: exp3 : fkc ;
    public final void exp3() throws RecognitionException {
        try {
            // /home/obda/Constraints.g:133:6: ( fkc )
            // /home/obda/Constraints.g:133:8: fkc
            {
            pushFollow(FOLLOW_fkc_in_exp3104);
            fkc();

            state._fsp--;


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
    // $ANTLR end "exp3"


    // $ANTLR start "exp4"
    // /home/obda/Constraints.g:134:1: exp4 : pkc ;
    public final void exp4() throws RecognitionException {
        try {
            // /home/obda/Constraints.g:134:6: ( pkc )
            // /home/obda/Constraints.g:134:8: pkc
            {
            pushFollow(FOLLOW_pkc_in_exp4111);
            pkc();

            state._fsp--;


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
    // $ANTLR end "exp4"


    // $ANTLR start "cc"
    // /home/obda/Constraints.g:135:1: cc : id 'CHECK' check ( ',' check )* ;
    public final void cc() throws RecognitionException {
        String id1 = null;


        try {
            // /home/obda/Constraints.g:135:4: ( id 'CHECK' check ( ',' check )* )
            // /home/obda/Constraints.g:135:6: id 'CHECK' check ( ',' check )*
            {
            pushFollow(FOLLOW_id_in_cc118);
            id1=id();

            state._fsp--;

            match(input,12,FOLLOW_12_in_cc120); 
            pushFollow(FOLLOW_check_in_cc122);
            check();

            state._fsp--;

            // /home/obda/Constraints.g:135:23: ( ',' check )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==13) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /home/obda/Constraints.g:135:24: ',' check
            	    {
            	    match(input,13,FOLLOW_13_in_cc125); 
            	    pushFollow(FOLLOW_check_in_cc126);
            	    check();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            			MappingController con = apic.getMappingController();
            	                        	DatasourcesController dscon = apic.getDatasourcesController();
            	                        	URI source_uri = dscon.getCurrentDataSource().getSourceID();
            	                      	 OBDAMappingAxiom map1 = con.getMapping(source_uri, id1);
            	                      	 if(map1 != null){	
            				constraint = new RDBMSCheckConstraint(id1,(RDBMSSQLQuery)map1.getSourceQuery(),  checks);
            			}else{
            				error1 = true;
            				try{
            					throw new Exception("Invalid Mapping ID");
            				}catch (Exception e){
            					e.printStackTrace();
            				}
            			}
            		

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "cc"


    // $ANTLR start "uni"
    // /home/obda/Constraints.g:155:1: uni : id 'UNIQUE (' parameter1 ')' ;
    public final void uni() throws RecognitionException {
        String id2 = null;


        try {
            // /home/obda/Constraints.g:155:5: ( id 'UNIQUE (' parameter1 ')' )
            // /home/obda/Constraints.g:155:7: id 'UNIQUE (' parameter1 ')'
            {
            pushFollow(FOLLOW_id_in_uni147);
            id2=id();

            state._fsp--;

            match(input,14,FOLLOW_14_in_uni149); 
            pushFollow(FOLLOW_parameter1_in_uni151);
            parameter1();

            state._fsp--;

            match(input,15,FOLLOW_15_in_uni153); 

            			MappingController con = apic.getMappingController();
            	                        	DatasourcesController dscon = apic.getDatasourcesController();
            	                        	URI source_uri = dscon.getCurrentDataSource().getSourceID();
            	                      	 OBDAMappingAxiom map1 = con.getMapping(source_uri, id2);
            	                      	 if(map1 != null){	
            				constraint = new RDBMSUniquenessConstraint(id2,(RDBMSSQLQuery)map1.getSourceQuery(),  terms_ofMappingOne);
            			}else{
            				error1 = true;
            				try{
            					throw new Exception("Invalid Mapping ID");
            				}catch (Exception e){
            					e.printStackTrace();
            				}
            			}
            		

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "uni"


    // $ANTLR start "fkc"
    // /home/obda/Constraints.g:175:1: fkc : id1 '(' parameter1 ') REFERENCES' id2 ( '(' parameter2 ')' )? ;
    public final void fkc() throws RecognitionException {
        String id13 = null;

        String id24 = null;


        try {
            // /home/obda/Constraints.g:175:5: ( id1 '(' parameter1 ') REFERENCES' id2 ( '(' parameter2 ')' )? )
            // /home/obda/Constraints.g:175:7: id1 '(' parameter1 ') REFERENCES' id2 ( '(' parameter2 ')' )?
            {
            pushFollow(FOLLOW_id1_in_fkc171);
            id13=id1();

            state._fsp--;

            match(input,16,FOLLOW_16_in_fkc173); 
            pushFollow(FOLLOW_parameter1_in_fkc174);
            parameter1();

            state._fsp--;

            match(input,17,FOLLOW_17_in_fkc175); 
            pushFollow(FOLLOW_id2_in_fkc177);
            id24=id2();

            state._fsp--;

            // /home/obda/Constraints.g:175:43: ( '(' parameter2 ')' )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==16) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // /home/obda/Constraints.g:175:45: '(' parameter2 ')'
                    {
                    match(input,16,FOLLOW_16_in_fkc181); 
                    pushFollow(FOLLOW_parameter2_in_fkc183);
                    parameter2();

                    state._fsp--;

                    match(input,15,FOLLOW_15_in_fkc185); 

                    }
                    break;

            }


            			MappingController con = apic.getMappingController();
            	                        	DatasourcesController dscon = apic.getDatasourcesController();
            	                        	URI source_uri = dscon.getCurrentDataSource().getSourceID();
            	                      	 OBDAMappingAxiom map1 = con.getMapping(source_uri, id13);
            	                      	 OBDAMappingAxiom map2 = con.getMapping(source_uri, id24);
            	                      	 if(map1 != null && map2 != null){	
            				constraint = new RDBMSForeignKeyConstraint(id13,id24,(RDBMSSQLQuery)map1.getSourceQuery(), (RDBMSSQLQuery)map2.getSourceQuery(), terms_ofMappingOne,terms_ofMappingTwo);
            			}else{
            				error1 = true;
            				try{
            					throw new Exception("Invalid Mapping ID");
            				}catch (Exception e){
            					e.printStackTrace();
            				}
            			}
            		

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "fkc"


    // $ANTLR start "pkc"
    // /home/obda/Constraints.g:197:1: pkc : id 'PRIMARY KEY (' parameter1 ')' ;
    public final void pkc() throws RecognitionException {
        String id5 = null;


        try {
            // /home/obda/Constraints.g:197:5: ( id 'PRIMARY KEY (' parameter1 ')' )
            // /home/obda/Constraints.g:197:7: id 'PRIMARY KEY (' parameter1 ')'
            {
            pushFollow(FOLLOW_id_in_pkc207);
            id5=id();

            state._fsp--;

            match(input,18,FOLLOW_18_in_pkc209); 
            pushFollow(FOLLOW_parameter1_in_pkc211);
            parameter1();

            state._fsp--;

            match(input,15,FOLLOW_15_in_pkc213); 

            			 MappingController con = apic.getMappingController();
            	                        	DatasourcesController dscon = apic.getDatasourcesController();
            	                        	URI source_uri = dscon.getCurrentDataSource().getSourceID();
            	                      	 OBDAMappingAxiom map = con.getMapping(source_uri, id5);
            	                      	 if(map != null){	
            				constraint = new RDBMSPrimaryKeyConstraint(id5,(RDBMSSQLQuery)map.getSourceQuery(), terms_ofMappingOne);
            			}else{
            				error1 = true;
            				try{
            					throw new Exception("Invalid Mapping ID");
            				}catch (Exception e){
            					e.printStackTrace();
            				}
            			}
            		

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "pkc"


    // $ANTLR start "check"
    // /home/obda/Constraints.g:217:1: check : '(' var op aux ')' ;
    public final void check() throws RecognitionException {
        String var6 = null;

        ConstraintsParser.op_return op7 = null;


        try {
            // /home/obda/Constraints.g:217:7: ( '(' var op aux ')' )
            // /home/obda/Constraints.g:217:9: '(' var op aux ')'
            {
            match(input,16,FOLLOW_16_in_check231); 
            pushFollow(FOLLOW_var_in_check233);
            var6=var();

            state._fsp--;

            pushFollow(FOLLOW_op_in_check235);
            op7=op();

            state._fsp--;

            pushFollow(FOLLOW_aux_in_check237);
            aux();

            state._fsp--;

            match(input,15,FOLLOW_15_in_check239); 

            		CheckOperationTerm op = new CheckOperationTerm(new VariableTerm(var6), (op7!=null?input.toString(op7.start,op7.stop):null), term_collector.get(0)); 
            		checks.add(op);
            		term_collector = new ArrayList<QueryTerm>(); 
            		

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
    // $ANTLR end "check"


    // $ANTLR start "parameter1"
    // /home/obda/Constraints.g:222:1: parameter1 : parameter ;
    public final void parameter1() throws RecognitionException {
        try {
            // /home/obda/Constraints.g:222:12: ( parameter )
            // /home/obda/Constraints.g:222:14: parameter
            {
            pushFollow(FOLLOW_parameter_in_parameter1247);
            parameter();

            state._fsp--;

            terms_ofMappingOne = term_collector; term_collector =  new ArrayList<QueryTerm>(); idForMappingOne = tmpId; tmpId = null;

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "parameter1"


    // $ANTLR start "parameter2"
    // /home/obda/Constraints.g:227:1: parameter2 : parameter ;
    public final void parameter2() throws RecognitionException {
        try {
            // /home/obda/Constraints.g:227:12: ( parameter )
            // /home/obda/Constraints.g:227:14: parameter
            {
            pushFollow(FOLLOW_parameter_in_parameter2264);
            parameter();

            state._fsp--;

            terms_ofMappingTwo = term_collector; term_collector =  new ArrayList<QueryTerm>(); idForMappingTwo = tmpId; tmpId = null; 

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "parameter2"


    // $ANTLR start "parameter"
    // /home/obda/Constraints.g:232:1: parameter : term ( ',' term )* ;
    public final void parameter() throws RecognitionException {
        try {
            // /home/obda/Constraints.g:232:11: ( term ( ',' term )* )
            // /home/obda/Constraints.g:232:13: term ( ',' term )*
            {
            pushFollow(FOLLOW_term_in_parameter281);
            term();

            state._fsp--;

            // /home/obda/Constraints.g:232:18: ( ',' term )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==13) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /home/obda/Constraints.g:232:19: ',' term
            	    {
            	    match(input,13,FOLLOW_13_in_parameter284); 
            	    pushFollow(FOLLOW_term_in_parameter286);
            	    term();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "parameter"


    // $ANTLR start "aux"
    // /home/obda/Constraints.g:237:1: aux : ( auxterm | constant );
    public final void aux() throws RecognitionException {
        try {
            // /home/obda/Constraints.g:237:5: ( auxterm | constant )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==20) ) {
                alt5=1;
            }
            else if ( ((LA5_0>=NUMBER && LA5_0<=BOOL)||LA5_0==19) ) {
                alt5=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // /home/obda/Constraints.g:237:7: auxterm
                    {
                    pushFollow(FOLLOW_auxterm_in_aux304);
                    auxterm();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /home/obda/Constraints.g:237:17: constant
                    {
                    pushFollow(FOLLOW_constant_in_aux308);
                    constant();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "aux"


    // $ANTLR start "constant"
    // /home/obda/Constraints.g:242:1: constant : ( stringconstant | numericconstant | booleanconstant );
    public final void constant() throws RecognitionException {
        try {
            // /home/obda/Constraints.g:242:10: ( stringconstant | numericconstant | booleanconstant )
            int alt6=3;
            switch ( input.LA(1) ) {
            case 19:
                {
                alt6=1;
                }
                break;
            case NUMBER:
                {
                alt6=2;
                }
                break;
            case BOOL:
                {
                alt6=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }

            switch (alt6) {
                case 1 :
                    // /home/obda/Constraints.g:242:12: stringconstant
                    {
                    pushFollow(FOLLOW_stringconstant_in_constant324);
                    stringconstant();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /home/obda/Constraints.g:242:29: numericconstant
                    {
                    pushFollow(FOLLOW_numericconstant_in_constant328);
                    numericconstant();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // /home/obda/Constraints.g:242:47: booleanconstant
                    {
                    pushFollow(FOLLOW_booleanconstant_in_constant332);
                    booleanconstant();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "constant"


    // $ANTLR start "auxterm"
    // /home/obda/Constraints.g:247:1: auxterm : var ;
    public final void auxterm() throws RecognitionException {
        String var8 = null;


        try {
            // /home/obda/Constraints.g:248:2: ( var )
            // /home/obda/Constraints.g:248:4: var
            {
            pushFollow(FOLLOW_var_in_auxterm351);
            var8=var();

            state._fsp--;

            VariableTerm t = new VariableTerm(var8); secondTerm = t;term_collector.add(t);

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "auxterm"


    // $ANTLR start "stringconstant"
    // /home/obda/Constraints.g:254:1: stringconstant : '\\'' string '\\'' ;
    public final void stringconstant() throws RecognitionException {
        String string9 = null;


        try {
            // /home/obda/Constraints.g:254:15: ( '\\'' string '\\'' )
            // /home/obda/Constraints.g:254:17: '\\'' string '\\''
            {
            match(input,19,FOLLOW_19_in_stringconstant370); 
            pushFollow(FOLLOW_string_in_stringconstant371);
            string9=string();

            state._fsp--;

            match(input,19,FOLLOW_19_in_stringconstant372); 
            XSDatatype type = XSDTypingController.getInstance().getType("xsd:string"); 
            		TypedConstantTerm tct = new TypedConstantTerm(string9, type); term_collector.add(tct); secondTerm = tct;

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        catch (UnknownXSDTypeException e) {

            			e.printStackTrace();
            		
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "stringconstant"


    // $ANTLR start "numericconstant"
    // /home/obda/Constraints.g:262:1: numericconstant : number ;
    public final void numericconstant() throws RecognitionException {
        String number10 = null;


        try {
            // /home/obda/Constraints.g:262:16: ( number )
            // /home/obda/Constraints.g:262:18: number
            {
            pushFollow(FOLLOW_number_in_numericconstant393);
            number10=number();

            state._fsp--;


            		String n = number10;
            		if(n.contains(".")){
            		XSDatatype type = XSDTypingController.getInstance().getType("xsd:double"); 
            		TypedConstantTerm tct = new TypedConstantTerm(n, type); term_collector.add(tct); secondTerm = tct;
            		}else{
            		XSDatatype type = XSDTypingController.getInstance().getType("xsd:int"); 
            		TypedConstantTerm tct = new TypedConstantTerm(n, type); term_collector.add(tct); secondTerm = tct;
            		}
            		

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        catch (UnknownXSDTypeException e) {

            			e.printStackTrace();
            		
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "numericconstant"


    // $ANTLR start "booleanconstant"
    // /home/obda/Constraints.g:279:1: booleanconstant : boolean_ ;
    public final void booleanconstant() throws RecognitionException {
        boolean boolean_11 = false;


        try {
            // /home/obda/Constraints.g:279:16: ( boolean_ )
            // /home/obda/Constraints.g:279:18: boolean_
            {
            pushFollow(FOLLOW_boolean__in_booleanconstant417);
            boolean_11=boolean_();

            state._fsp--;

            XSDatatype type = XSDTypingController.getInstance().getType("xsd:boolean"); 	
            		TypedConstantTerm tct = new TypedConstantTerm(boolean_11 +"", type); term_collector.add(tct); secondTerm = tct;

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        catch (UnknownXSDTypeException e) {

            			e.printStackTrace();
            		
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "booleanconstant"


    // $ANTLR start "term"
    // /home/obda/Constraints.g:287:1: term : var ;
    public final void term() throws RecognitionException {
        String var12 = null;


        try {
            // /home/obda/Constraints.g:287:6: ( var )
            // /home/obda/Constraints.g:287:8: var
            {
            pushFollow(FOLLOW_var_in_term441);
            var12=var();

            state._fsp--;

            VariableTerm t = new VariableTerm(var12); term_collector.add(t);

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "term"


    // $ANTLR start "number"
    // /home/obda/Constraints.g:292:1: number returns [String d] : NUMBER ;
    public final String number() throws RecognitionException {
        String d = null;

        Token NUMBER13=null;

        try {
            // /home/obda/Constraints.g:292:27: ( NUMBER )
            // /home/obda/Constraints.g:292:29: NUMBER
            {
            NUMBER13=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_number465); 
            d = (NUMBER13!=null?NUMBER13.getText():null);

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return d;
    }
    // $ANTLR end "number"


    // $ANTLR start "boolean_"
    // /home/obda/Constraints.g:297:1: boolean_ returns [boolean b] : BOOL ;
    public final boolean boolean_() throws RecognitionException {
        boolean b = false;

        Token BOOL14=null;

        try {
            // /home/obda/Constraints.g:297:30: ( BOOL )
            // /home/obda/Constraints.g:297:32: BOOL
            {
            BOOL14=(Token)match(input,BOOL,FOLLOW_BOOL_in_boolean_486); 
            b = new Boolean((BOOL14!=null?BOOL14.getText():null)).booleanValue();

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return b;
    }
    // $ANTLR end "boolean_"


    // $ANTLR start "string"
    // /home/obda/Constraints.g:302:1: string returns [String s] : ALPHAVAR ;
    public final String string() throws RecognitionException {
        String s = null;

        Token ALPHAVAR15=null;

        try {
            // /home/obda/Constraints.g:302:27: ( ALPHAVAR )
            // /home/obda/Constraints.g:302:29: ALPHAVAR
            {
            ALPHAVAR15=(Token)match(input,ALPHAVAR,FOLLOW_ALPHAVAR_in_string507); 
            s = (ALPHAVAR15!=null?ALPHAVAR15.getText():null);

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return s;
    }
    // $ANTLR end "string"


    // $ANTLR start "var"
    // /home/obda/Constraints.g:307:1: var returns [String v] : '$' ALPHAVAR ;
    public final String var() throws RecognitionException {
        String v = null;

        Token ALPHAVAR16=null;

        try {
            // /home/obda/Constraints.g:307:24: ( '$' ALPHAVAR )
            // /home/obda/Constraints.g:307:26: '$' ALPHAVAR
            {
            match(input,20,FOLLOW_20_in_var529); 
            ALPHAVAR16=(Token)match(input,ALPHAVAR,FOLLOW_ALPHAVAR_in_var531); 
            v = (ALPHAVAR16!=null?ALPHAVAR16.getText():null);

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return v;
    }
    // $ANTLR end "var"


    // $ANTLR start "id"
    // /home/obda/Constraints.g:312:1: id returns [String mid] : ALPHAVAR ;
    public final String id() throws RecognitionException {
        String mid = null;

        Token ALPHAVAR17=null;

        try {
            // /home/obda/Constraints.g:312:25: ( ALPHAVAR )
            // /home/obda/Constraints.g:312:27: ALPHAVAR
            {
            ALPHAVAR17=(Token)match(input,ALPHAVAR,FOLLOW_ALPHAVAR_in_id553); 
            mid = (ALPHAVAR17!=null?ALPHAVAR17.getText():null); 

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return mid;
    }
    // $ANTLR end "id"


    // $ANTLR start "id1"
    // /home/obda/Constraints.g:317:1: id1 returns [String mid] : ALPHAVAR ;
    public final String id1() throws RecognitionException {
        String mid = null;

        Token ALPHAVAR18=null;

        try {
            // /home/obda/Constraints.g:317:26: ( ALPHAVAR )
            // /home/obda/Constraints.g:317:28: ALPHAVAR
            {
            ALPHAVAR18=(Token)match(input,ALPHAVAR,FOLLOW_ALPHAVAR_in_id1575); 
            mid = (ALPHAVAR18!=null?ALPHAVAR18.getText():null);

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return mid;
    }
    // $ANTLR end "id1"


    // $ANTLR start "id2"
    // /home/obda/Constraints.g:322:1: id2 returns [String mid] : ALPHAVAR ;
    public final String id2() throws RecognitionException {
        String mid = null;

        Token ALPHAVAR19=null;

        try {
            // /home/obda/Constraints.g:322:26: ( ALPHAVAR )
            // /home/obda/Constraints.g:322:28: ALPHAVAR
            {
            ALPHAVAR19=(Token)match(input,ALPHAVAR,FOLLOW_ALPHAVAR_in_id2597); 
            mid = (ALPHAVAR19!=null?ALPHAVAR19.getText():null); 

            }

        }
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return mid;
    }
    // $ANTLR end "id2"

    public static class op_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "op"
    // /home/obda/Constraints.g:327:1: op : ( '<' | '>' | '=' | '<=' | '>=' );
    public final ConstraintsParser.op_return op() throws RecognitionException {
        ConstraintsParser.op_return retval = new ConstraintsParser.op_return();
        retval.start = input.LT(1);

        try {
            // /home/obda/Constraints.g:327:4: ( '<' | '>' | '=' | '<=' | '>=' )
            // /home/obda/Constraints.g:
            {
            if ( (input.LA(1)>=21 && input.LA(1)<=25) ) {
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
        catch (RecognitionException ex) {
             
            		error1 = true; 
            		throw ex; 
            		
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "op"

    // Delegated rules


 

    public static final BitSet FOLLOW_prog_in_parse47 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_parse49 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_exp_in_prog69 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_exp1_in_exp77 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_exp2_in_exp79 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_exp3_in_exp81 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_exp4_in_exp83 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cc_in_exp190 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_uni_in_exp297 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fkc_in_exp3104 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pkc_in_exp4111 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_cc118 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_12_in_cc120 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_check_in_cc122 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_13_in_cc125 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_check_in_cc126 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_id_in_uni147 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_uni149 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_parameter1_in_uni151 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_15_in_uni153 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id1_in_fkc171 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_16_in_fkc173 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_parameter1_in_fkc174 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_17_in_fkc175 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_id2_in_fkc177 = new BitSet(new long[]{0x0000000000010002L});
    public static final BitSet FOLLOW_16_in_fkc181 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_parameter2_in_fkc183 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_15_in_fkc185 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_pkc207 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_18_in_pkc209 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_parameter1_in_pkc211 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_15_in_pkc213 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_16_in_check231 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_var_in_check233 = new BitSet(new long[]{0x0000000003E00000L});
    public static final BitSet FOLLOW_op_in_check235 = new BitSet(new long[]{0x0000000000180030L});
    public static final BitSet FOLLOW_aux_in_check237 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_15_in_check239 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parameter_in_parameter1247 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parameter_in_parameter2264 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_parameter281 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_13_in_parameter284 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_term_in_parameter286 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_auxterm_in_aux304 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constant_in_aux308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stringconstant_in_constant324 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericconstant_in_constant328 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanconstant_in_constant332 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_in_auxterm351 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_19_in_stringconstant370 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_string_in_stringconstant371 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_19_in_stringconstant372 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_number_in_numericconstant393 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean__in_booleanconstant417 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_in_term441 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_number465 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOOL_in_boolean_486 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALPHAVAR_in_string507 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_20_in_var529 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_ALPHAVAR_in_var531 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALPHAVAR_in_id553 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALPHAVAR_in_id1575 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALPHAVAR_in_id2597 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_op0 = new BitSet(new long[]{0x0000000000000002L});

}