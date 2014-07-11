package it.unibz.krdb.sql;

import static org.junit.Assert.*;
import it.unibz.krdb.sql.api.Attribute;

import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UserConstraintTest {

	DBMetadata md;
	
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
	
	@Test
	public void testEmptyUserConstraints() {
		UserConstraints uc = new UserConstraints("src/test/resources/userconstraints/empty_constraints.lst");
		Iterator<String> it = uc.getReferredTables();
		assertFalse(it.hasNext());
	}


	@Test
	public void testUserPKeys() {
		UserConstraints uc = new UserConstraints("src/test/resources/userconstraints/pkeys.lst");
		Iterator<String> it = uc.getReferredTables();
		assertFalse(it.hasNext());
	}

	@Test
	public void testAddPrimaryKeys() {
		UserConstraints uc = new UserConstraints("src/test/resources/userconstraints/pkeys.lst");
		uc.addPrimaryKeys(this.md);
		DataDefinition dd = this.md.getDefinition("TABLENAME");
		Attribute attr = dd.getAttribute(0);
		assertTrue(attr.isPrimaryKey());
	}


	@Test
	public void testGetReferredTables() {
		UserConstraints uc = new UserConstraints("src/test/resources/userconstraints/fkeys.lst");
		Iterator<String> it = uc.getReferredTables();
		assertTrue(it.hasNext());
		assertTrue(it.next().equals("TABLE2"));
	}

	@Test
	public void testAddForeignKeys() {
		UserConstraints uc = new UserConstraints("src/test/resources/userconstraints/fkeys.lst");
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
		UserConstraints uc = new UserConstraints("src/test/resources/userconstraints/keys.lst");
		uc.addConstraints(this.md);
		DataDefinition dd = this.md.getDefinition("TABLENAME");
		Attribute attr = dd.getAttribute(0);
		assertTrue(attr.isForeignKey());
		Reference ref = attr.getReference();
		assertTrue(ref.getTableReference().equals("TABLE2"));
		assertTrue(ref.getColumnReference().equals("KEY1"));
		assertTrue(attr.isPrimaryKey());
	}

	
}
