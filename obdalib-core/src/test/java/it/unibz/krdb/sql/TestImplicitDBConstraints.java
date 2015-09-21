package it.unibz.krdb.sql;

import static org.junit.Assert.*;
import it.unibz.krdb.sql.api.RelationJSQL;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class TestImplicitDBConstraints {

	DBMetadata md;
	ArrayList<RelationJSQL> tables;
	
	@Before
	public void setupMetadata(){
		this.md = new DBMetadata("dummy class", null, null, DBMetadata.IdentityIdNormalizer);
		TableDefinition td = new TableDefinition("TABLENAME");
		td.addAttribute("KEYNAME", 0, null, false); // from 1
		md.add(td); 
		TableDefinition td2 = new TableDefinition("TABLE2");
		td2.addAttribute("KEY1", 0, null, false);  // from 1
		td2.addAttribute("KEY2", 0, null, false);
		md.add(td2);
	}
	
	@Before
	public void initTableList(){
		tables  = new ArrayList<>();
	}
	
	@Test
	public void testEmptyUserConstraints() {
		ImplicitDBConstraints uc = new ImplicitDBConstraints("src/test/resources/userconstraints/empty_constraints.lst");
		uc.addReferredTables(tables);
		assertTrue(tables.size() == 0);
	}


	@Test
	public void testUserPKeys() {
		ImplicitDBConstraints uc = new ImplicitDBConstraints("src/test/resources/userconstraints/pkeys.lst");
		uc.addReferredTables(tables);
		assertTrue(tables.size() == 0);
	}

	@Test
	public void testAddPrimaryKeys() {
		ImplicitDBConstraints uc = new ImplicitDBConstraints("src/test/resources/userconstraints/pkeys.lst");
		uc.addFunctionalDependencies(this.md);
		RelationDefinition dd = this.md.getDefinition("TABLENAME");
		Attribute attr = dd.getAttribute("KEYNAME");	
		assertTrue(dd.getUniqueConstraints().get(0).getAttributes().equals(ImmutableList.of(attr))); 
	}


	@Test
	public void testGetReferredTables() {
		ImplicitDBConstraints uc = new ImplicitDBConstraints("src/test/resources/userconstraints/fkeys.lst");
		uc.addReferredTables(tables);
		assertTrue(tables.size() == 1);
		assertTrue(uc.tableIsInList(tables, "TABLE2"));
	}

	@Test
	public void testAddForeignKeys() {
		ImplicitDBConstraints uc = new ImplicitDBConstraints("src/test/resources/userconstraints/fkeys.lst");
		uc.addForeignKeys(this.md);
		RelationDefinition dd = this.md.getDefinition("TABLENAME");
		ForeignKeyConstraint fk = dd.getForeignKeys().get(0);
		assertTrue(fk != null);
		assertTrue(fk.getComponents().get(0).getReference().getRelation().getName().equals("TABLE2"));
		assertTrue(fk.getComponents().get(0).getReference().getName().equals("KEY1"));
	}

	@Test
	public void testAddKeys() {
		ImplicitDBConstraints uc = new ImplicitDBConstraints("src/test/resources/userconstraints/keys.lst");
		uc.addConstraints(this.md);
		RelationDefinition dd = this.md.getDefinition("TABLENAME");
		ForeignKeyConstraint fk = dd.getForeignKeys().get(0);
		assertTrue(fk != null);
		assertTrue(fk.getComponents().get(0).getReference().getRelation().getName().equals("TABLE2"));
		assertTrue(fk.getComponents().get(0).getReference().getName().equals("KEY1"));
		assertTrue(dd.getUniqueConstraints().get(0).getAttributes().equals(ImmutableList.of(dd.getAttribute("KEYNAME")))); 
	}

	
}
