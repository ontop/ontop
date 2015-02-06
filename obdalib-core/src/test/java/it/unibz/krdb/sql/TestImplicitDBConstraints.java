package it.unibz.krdb.sql;

import static org.junit.Assert.*;
import it.unibz.krdb.sql.api.Attribute;
import it.unibz.krdb.sql.api.RelationJSQL;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestImplicitDBConstraints {

	DBMetadata md;
	ArrayList<RelationJSQL> tables;
	
	@Before
	public void setupMetadata(){
		this.md = new DBMetadata();
		TableDefinition td = new TableDefinition("TABLENAME");
		td.setAttribute(0, new Attribute("KEYNAME"));
		md.add(td);
		TableDefinition td2 = new TableDefinition("TABLE2");
		td2.setAttribute(0, new Attribute("KEY1"));
		td2.setAttribute(1, new Attribute("KEY2"));
		md.add(td2);
	}
	
	@Before
	public void initTableList(){
		tables  = new ArrayList<RelationJSQL>();
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
		Attribute attr = dd.getAttribute(0);
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
		Attribute attr = dd.getAttribute(0);
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
		Attribute attr = dd.getAttribute(0);
		assertTrue(attr.isForeignKey());
		Reference ref = attr.getReference();
		assertTrue(ref.getTableReference().equals("TABLE2"));
		assertTrue(ref.getColumnReference().equals("KEY1"));
		assertTrue(attr.isUnique());
	}

	
}
