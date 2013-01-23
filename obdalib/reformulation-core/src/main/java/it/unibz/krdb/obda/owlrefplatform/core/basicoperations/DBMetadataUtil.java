package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.iri.IRIFactory;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.Reference;
import it.unibz.krdb.sql.TableDefinition;
import it.unibz.krdb.sql.api.Attribute;

public class DBMetadataUtil {

	private final DBMetadata metadata;
	private static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	private static IRIFactory ifac = OBDADataFactoryImpl.getIRIFactory();
	
	public  DBMetadataUtil(DBMetadata metadata)
	{
		this.metadata = metadata;
	}
	
	/*
	 * genereate CQIE rules from foreign key info of db metadata
	 * TABLE1.COL1 references TABLE2.COL2 as foreign key then 
	 * construct CQIE rule TABLE2(P1, P3, COL2, P4) :- TABLE1(COL2, T2, T3).
	 */
	public static List<CQIE> generateFKRules(DBMetadata metadata)
	{
		List<CQIE> rules = new ArrayList<CQIE>();
		
		List<TableDefinition> tableDefs = metadata.getTableList();
		for (TableDefinition def : tableDefs)
		{
			
			List<Attribute> foreignKeys = def.getForeignKeys();
			for (Attribute foreignKey : foreignKeys)
			{
				//get current table and column (1)
				String table1 = def.getName();
				String column1 = foreignKey.getName();
				//get referenced table and column (2)
				Reference reference = foreignKey.getReference();
				String table2 = reference.getTableReference();
				String column2 = reference.getColumnReference();				
				//get table definition for referenced table
				TableDefinition def2 = (TableDefinition) metadata.getDefinition(table2);
				
				//get positions of referenced attribute
				int pos1 = def.getAttributePosition(column1);
				int pos2 = def2.getAttributePosition(column2);
				
				//construct CQIE
				Predicate p1 = fac.getPredicate(ifac.construct(table1), def.countAttribute());
				Predicate p2 = fac.getPredicate(ifac.construct(table2), def2.countAttribute());
				
				List<NewLiteral> terms1 = new ArrayList<NewLiteral>();
				for (int i=0; i<def.countAttribute(); i++)
				{
					 terms1.add(fac.getVariable("t"+(i+1)));
				}
				List<NewLiteral> terms2 = new ArrayList<NewLiteral>();
				for (int i=0; i<def2.countAttribute(); i++)
				{
					 terms2.add(fac.getVariable("p"+(i+1)));
				}
				terms1.set(pos1, terms2.get(pos2));
				
				Atom head = fac.getAtom(p2, terms2);
				Atom body1 = fac.getAtom(p1, terms1);
				List<Atom> body = new ArrayList<Atom>();
				body.add(body1);
				
				CQIE rule = fac.getCQIE(head, body);
				System.out.println(rule.toString());
				rules.add(rule);
			}
		}
		
		
		return rules;
	}
}
