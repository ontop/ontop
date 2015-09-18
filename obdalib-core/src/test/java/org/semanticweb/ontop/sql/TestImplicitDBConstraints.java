package org.semanticweb.ontop.sql;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.api.Attribute;
import org.semanticweb.ontop.sql.api.RelationJSQL;

public class TestImplicitDBConstraints {

	DBMetadata md;
	ArrayList<RelationJSQL> tables;
	
	@Before
	public void setupMetadata(){
		this.md = new DBMetadata("dummy class");
		TableDefinition td = new TableDefinition("TABLENAME");
		td.addAttribute(new Attribute("KEYNAME")); // from 1
		md.add(td);
		TableDefinition td2 = new TableDefinition("TABLE2");
		td2.addAttribute(new Attribute("KEY1"));  // from 1
		td2.addAttribute(new Attribute("KEY2"));
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
		uc.addFunctionalDependency(this.md);
		DataDefinition dd = this.md.getDefinition("TABLENAME");
		Attribute attr = dd.getAttribute(1);
		assertTrue(attr.isUnique());
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
		DataDefinition dd = this.md.getDefinition("TABLENAME");
		Attribute attr = dd.getAttribute(1);  // from 1
		assertTrue(attr.isForeignKey());
		Reference ref = attr.getReference();
		assertTrue(ref.getTableReference().equals("TABLE2"));
		assertTrue(ref.getColumnReference().equals("KEY1"));
	}

	@Test
	public void testAddKeys() {
		ImplicitDBConstraints uc = new ImplicitDBConstraints("src/test/resources/userconstraints/keys.lst");
		uc.addConstraints(this.md);
		DataDefinition dd = this.md.getDefinition("TABLENAME");
		Attribute attr = dd.getAttribute(1);  // from 1
		assertTrue(attr.isForeignKey());
		Reference ref = attr.getReference();
		assertTrue(ref.getTableReference().equals("TABLE2"));
		assertTrue(ref.getColumnReference().equals("KEY1"));
		assertTrue(attr.isUnique());
	}

	
}
