package it.unibz.krdb.sql;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class TestImplicitDBConstraints {

	DBMetadata md;
	Set<RelationID> tables;
	
	private final QuotedIDFactory idfac = new QuotedIDFactoryStandardSQL();
	
	@Before
	public void setupMetadata(){
		this.md = new DBMetadata("dummy class", null, null, idfac);
		TableDefinition td = new TableDefinition(idfac.createRelationFromString("TABLENAME"));
		td.addAttribute(idfac.createFromString("KEYNAME"), 0, null, false); // from 1
		md.add(td); 
		TableDefinition td2 = new TableDefinition(idfac.createRelationFromString("TABLE2"));
		td2.addAttribute(idfac.createFromString("KEY1"), 0, null, false);  // from 1
		td2.addAttribute(idfac.createFromString("KEY2"), 0, null, false);
		md.add(td2);
	}
	
	@Before
	public void initTableList(){
		tables  = new HashSet<>();
	}
	
	@Test
	public void testEmptyUserConstraints() {
		ImplicitDBConstraints uc = new ImplicitDBConstraints("src/test/resources/userconstraints/empty_constraints.lst", idfac);
		uc.addReferredTables(tables);
		assertTrue(tables.size() == 0);
	}


	@Test
	public void testUserPKeys() {
		ImplicitDBConstraints uc = new ImplicitDBConstraints("src/test/resources/userconstraints/pkeys.lst", idfac);
		uc.addReferredTables(tables);
		assertTrue(tables.size() == 0);
	}

	@Test
	public void testAddPrimaryKeys() {
		ImplicitDBConstraints uc = new ImplicitDBConstraints("src/test/resources/userconstraints/pkeys.lst", idfac);
		uc.addFunctionalDependencies(this.md);
		RelationDefinition dd = this.md.getDefinition(idfac.createRelationFromString("TABLENAME"));
		Attribute attr = dd.getAttribute(idfac.createFromString("KEYNAME"));	
		assertTrue(dd.getUniqueConstraints().get(0).getAttributes().equals(ImmutableList.of(attr))); 
	}


	@Test
	public void testGetReferredTables() {
		ImplicitDBConstraints uc = new ImplicitDBConstraints("src/test/resources/userconstraints/fkeys.lst", idfac);
		uc.addReferredTables(tables);
		assertTrue(tables.size() == 1);
		assertTrue(tables.contains(new RelationID(new QuotedID(null, ""), new QuotedID("TABLE2", ""))));
	}

	@Test
	public void testAddForeignKeys() {
		ImplicitDBConstraints uc = new ImplicitDBConstraints("src/test/resources/userconstraints/fkeys.lst", idfac);
		uc.addForeignKeys(this.md);
		RelationDefinition dd = this.md.getDefinition(idfac.createRelationFromString("TABLENAME"));
		ForeignKeyConstraint fk = dd.getForeignKeys().get(0);
		assertTrue(fk != null);
		assertEquals(fk.getComponents().get(0).getReference().getRelation().getName(), 
				idfac.createRelationFromString("TABLE2"));
		assertEquals(fk.getComponents().get(0).getReference().getName(), idfac.createFromString("KEY1"));
	}

	@Test
	public void testAddKeys() {
		ImplicitDBConstraints uc = new ImplicitDBConstraints("src/test/resources/userconstraints/keys.lst", idfac);
		uc.addFunctionalDependencies(this.md);
		uc.addForeignKeys(this.md);
		RelationDefinition dd = this.md.getDefinition(idfac.createRelationFromString("TABLENAME"));
		ForeignKeyConstraint fk = dd.getForeignKeys().get(0);
		assertTrue(fk != null);
		assertEquals(fk.getComponents().get(0).getReference().getRelation().getName(), 
						idfac.createRelationFromString("TABLE2"));
		assertEquals(fk.getComponents().get(0).getReference().getName(), idfac.createFromString("KEY1"));
		assertEquals(dd.getUniqueConstraints().get(0).getAttributes(),
							ImmutableList.of(dd.getAttribute(idfac.createFromString("KEYNAME")))); 
	}

	
}
