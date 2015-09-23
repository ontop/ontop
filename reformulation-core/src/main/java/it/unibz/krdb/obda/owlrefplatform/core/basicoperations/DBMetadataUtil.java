package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.Reference;
import it.unibz.krdb.sql.TableDefinition;
import it.unibz.krdb.sql.api.Attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBMetadataUtil {

	private static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	
	private static Logger log = LoggerFactory.getLogger(DBMetadataUtil.class);
	
	/*
	 * generate CQIE rules from foreign key info of db metadata
	 * TABLE1.COL1 references TABLE2.COL2 as foreign key then 
	 * construct CQIE rule TABLE2(P1, P3, COL2, P4) :- TABLE1(COL2, T2, T3).
	 */
	public static LinearInclusionDependencies generateFKRules(DBMetadata metadata) {
		LinearInclusionDependencies dependencies = new LinearInclusionDependencies();
		final boolean printouts = false;
		
		if (printouts)
			System.out.println("===FOREIGN KEY RULES");
		int count = 0;
		Collection<TableDefinition> tableDefs = metadata.getTables();
		for (TableDefinition def : tableDefs) {
			Map<String, List<Attribute>> foreignKeys = def.getForeignKeys();
			for (Entry<String, List<Attribute>> fks : foreignKeys.entrySet()) {
				List<Attribute> fkAttributes = fks.getValue();
				try {
					TableDefinition def2 = null;
					Map<Integer, Integer> positionMatch = new HashMap<>();
					for (Attribute attr : fkAttributes) {
						// Get current table and column (1)
						String column1 = attr.getName();
						
						// Get referenced table and column (2)
						Reference reference = attr.getReference();
						String table2 = reference.getTableReference();
						String column2 = reference.getColumnReference();				
						
						// Get table definition for referenced table
						def2 = (TableDefinition) metadata.getDefinition(table2);
						if (def2 == null) { // in case of broken FK
							// ROMAN: this is not necessarily broken -- the table may not be mentioned in the mappings 
							//        (which can happen in the NEW abridged metadata)
							throw new BrokenForeignKeyException(reference, "Missing table: " + table2);
						}
						// Get positions of referenced attribute
						int pos1 = def.getAttributeKey(column1);
						if (pos1 == -1) {
							throw new BrokenForeignKeyException(reference, "Missing column: " + column1);
						}
						int pos2 = def2.getAttributeKey(column2);
						if (pos2 == -1) {
							throw new BrokenForeignKeyException(reference, "Missing column: " + column2);
						}
						positionMatch.put(pos1 - 1, pos2 - 1); // keys start at 1
					}
					// Construct CQIE
					Predicate p1 = fac.getPredicate(def.getName(), def.getAttributes().size());					
					List<Term> terms1 = new ArrayList<>(p1.getArity());
					for (int i = 1; i <= p1.getArity(); i++) 
						 terms1.add(fac.getVariable("t" + i));
					
					// Roman: important correction because table2 may not be in the same case 
					// (e.g., it may be all upper-case)
					Predicate p2 = fac.getPredicate(def2.getName(), def2.getAttributes().size());
					List<Term> terms2 = new ArrayList<>(p2.getArity());
					for (int i = 1; i <= p2.getArity(); i++) 
						 terms2.add(fac.getVariable("p" + i));
					
					// do the swapping
					for (Entry<Integer,Integer> swap : positionMatch.entrySet()) 
						terms1.set(swap.getKey(), terms2.get(swap.getValue()));
					
					Function head = fac.getFunction(p2, terms2);
					Function body = fac.getFunction(p1, terms1);
					
					dependencies.addRule(head, body);				
					if (printouts)
						System.out.println("   FK_" + ++count + " " +  head + " :- " + body);
				} 
				catch (BrokenForeignKeyException e) {
					// Log the warning message
					log.warn(e.getMessage());
				}
			}
		}		
		if (printouts)
			System.out.println("===END OF FOREIGN KEY RULES");
		return dependencies;
	}
}
