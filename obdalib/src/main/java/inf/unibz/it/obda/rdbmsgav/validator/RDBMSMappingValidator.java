/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro.
 * All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public
 * License v.3 (see OBDAAPI_LICENSE.txt for details). The components of this
 * work include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, 
 * b) third-party components licensed under terms that may be different from 
 *   those of the LGPL.  Information about such licenses can be found in the 
 *   file named OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.obda.rdbmsgav.validator;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.gui.swing.datasource.panels.ResultSetTableModel;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.obda.validator.MappingValidator;
import inf.unibz.it.ucq.domain.BinaryQueryAtom;
import inf.unibz.it.ucq.domain.ConceptQueryAtom;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.FunctionTerm;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.domain.VariableTerm;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

public class RDBMSMappingValidator extends MappingValidator {

	private RDBMSSQLQuery sqlQuery = null;
	private ConjunctiveQuery conjunciveQuery = null;
	private DataSource dsc;
	
	public RDBMSMappingValidator (APIController apic, DataSource dsc, RDBMSSQLQuery sql, ConjunctiveQuery con){
		
		super(apic, sql, con);
		this.dsc = dsc;
		sqlQuery = sql;
		conjunciveQuery = con;
	}
	
//	public RDBMSMappingValidator (){
//		super(null, null, null);
//	}
	
	@Override
	public Enumeration<String> validate() {
		
		SQLQueryValidator v = new SQLQueryValidator(dsc, sqlQuery);
		Vector<String> errors = new Vector<String>();
		
		if(!v.validate() || !conjunciveQuery.isInputQueryValid(apic)){
			
			errors.add("CRITICAL ERROR: one of the queries is invalid!");
			return errors.elements();
		}
		
		Vector<String> conjunciveQuery_variables = getVariables(conjunciveQuery);
		Vector<String> sqlQuery_variables = getVariables(v);
		
		for (int i=0;i< conjunciveQuery_variables.size();i++){
			
			String var = conjunciveQuery_variables.elementAt(i);
			if(!sqlQuery_variables.contains(var)){
				errors.add("CRITICAL ERROR: unknown variable in head!");
				return errors.elements();
			}
		}
		for (int i=0;i< sqlQuery_variables.size();i++){
			
			String var = sqlQuery_variables.elementAt(i);
			if(!conjunciveQuery_variables.contains(var)){
				errors.add("NONCRITICAL ERROR: unused variable in body!");
			}
		}
		if (sqlQuery.getInputQuString().toLowerCase().contains("select *")){
			errors.add("NONCRITICAL ERROR: you should not use SELECT *!");
		}
		
		while(!sqlQuery_variables.isEmpty()){
			
			String str = sqlQuery_variables.firstElement();
			sqlQuery_variables.remove(0);
			if (sqlQuery_variables.contains(str)){
				errors.add("NONCRITICAL ERROR: variable names should be unique!");
			}
		}
		
		return errors.elements();
	}
	
	private Vector<String> getVariables(ConjunctiveQuery q){
		
		Vector<String> v = new Vector<String>();
		
		ArrayList<QueryAtom> atoms = q.getAtoms();
		Iterator it = atoms.iterator();
		while (it.hasNext()){
			
			QueryAtom at = (QueryAtom) it.next();
			
			if (at instanceof ConceptQueryAtom){
				ConceptQueryAtom a = (ConceptQueryAtom) at;
				ArrayList<QueryTerm> terms = a.getTerms();				
				QueryTerm t = (QueryTerm) terms.get(0);
				if(t instanceof FunctionTerm){
					
					FunctionTerm f = (FunctionTerm) t;
					ArrayList<QueryTerm> para = f.getParameters();
					Iterator para_it = para.iterator();
					while (para_it.hasNext()){
						
						QueryTerm p = (VariableTerm)para_it.next();
						String str = p.getVariableName();
						v.add(str);
						
					}
				}else if(t instanceof VariableTerm){
					
					VariableTerm vt = (VariableTerm) t;
					String str= vt.getVariableName();
					v.add(str);
					
				}
			}else if(at instanceof BinaryQueryAtom){
				
				BinaryQueryAtom a = (BinaryQueryAtom) at;
				ArrayList<QueryTerm> terms = a.getTerms();
				Iterator terms_it = terms.iterator();
				while (terms_it.hasNext()){
					
					QueryTerm t = (QueryTerm) terms_it.next();
					if(t instanceof FunctionTerm){
						
						FunctionTerm f = (FunctionTerm) t;	
						ArrayList<QueryTerm> para = f.getParameters();
						Iterator para_it = para.iterator();
						while (para_it.hasNext()){
							
							QueryTerm p = (VariableTerm)para_it.next();
							String str = p.getVariableName();
							v.add(str);
							
						}
					}else if(t instanceof VariableTerm){
						
						VariableTerm vt = (VariableTerm) t;
						String str = vt.getVariableName();
						v.add(str);
						
					}
				}
			}
		}
		return v;
		
	}
	
	private Vector<String> getVariables(SQLQueryValidator v){
		
		Vector <String> str = new Vector<String>();
		ResultSetTableModel model = (ResultSetTableModel) v.execute();
		int colums =model.getColumnCount();
		for (int i=0; i<colums; i++){
			
			str.add(model.getColumnName(i));
		}
		
		return str;
	}

}
