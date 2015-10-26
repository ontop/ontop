package it.unibz.krdb.sql;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import com.google.common.collect.ImmutableList;

public class TestImplicitDBConstraints {

	DBMetadata md;
	QuotedIDFactory idfac;
	
	@Before
	public void setupMetadata(){
		this.md = DBMetadataExtractor.createDummyMetadata();
		this.idfac = md.getQuotedIDFactory();

		DatabaseRelationDefinition td = md.createDatabaseRelation(idfac.createRelationID(null, "TABLENAME"));
		td.addAttribute(idfac.createAttributeID("KEYNAME"), 0, null, false); // from 1

		DatabaseRelationDefinition td2 = md.createDatabaseRelation(idfac.createRelationID(null, "TABLE2"));
		td2.addAttribute(idfac.createAttributeID("KEY1"), 0, null, false);  // from 1
		td2.addAttribute(idfac.createAttributeID("KEY2"), 0, null, false);
	}
	
	@Test
	public void testEmptyUserConstraints() {
		ImplicitDBConstraints uc = new ImplicitDBConstraints("src/test/resources/userconstraints/empty_constraints.lst");
		Set<RelationID> refs = uc.getReferredTables(idfac);
		assertTrue(refs.size() == 0);
	}


	@Test
	public void testUserPKeys() {
		ImplicitDBConstraints uc = new ImplicitDBConstraints("src/test/resources/userconstraints/pkeys.lst");
		Set<RelationID> refs = uc.getReferredTables(idfac);
		assertTrue(refs.size() == 0);
	}

	@Test
	public void testAddPrimaryKeys() {
		ImplicitDBConstraints uc = new ImplicitDBConstraints("src/test/resources/userconstraints/pkeys.lst");
		uc.addFunctionalDependencies(this.md);
		DatabaseRelationDefinition dd = this.md.getDatabaseRelation(idfac.createRelationID(null, "TABLENAME"));
		Attribute attr = dd.getAttribute(idfac.createAttributeID("KEYNAME"));	
		assertTrue(dd.getUniqueConstraints().get(0).getAttributes().equals(ImmutableList.of(attr))); 
	}


	@Test
	public void testGetReferredTables() {
		ImplicitDBConstraints uc = new ImplicitDBConstraints("src/test/resources/userconstraints/fkeys.lst");
		Set<RelationID> refs = uc.getReferredTables(idfac);
		assertTrue(refs.size() == 1);
		assertTrue(refs.contains(idfac.createRelationID(null, "TABLE2")));
	}

	@Test
	public void testAddForeignKeys() {
		ImplicitDBConstraints uc = new ImplicitDBConstraints("src/test/resources/userconstraints/fkeys.lst");
		uc.addForeignKeys(this.md);
		DatabaseRelationDefinition dd = this.md.getDatabaseRelation(idfac.createRelationID(null, "TABLENAME"));
		ForeignKeyConstraint fk = dd.getForeignKeys().get(0);
		assertTrue(fk != null);
		assertEquals(fk.getComponents().get(0).getReference().getRelation().getID(), 
				idfac.createRelationID(null, "TABLE2"));
		assertEquals(fk.getComponents().get(0).getReference().getID(), idfac.createAttributeID("KEY1"));
	}

	@Test
	public void testAddKeys() {
		ImplicitDBConstraints uc = new ImplicitDBConstraints("src/test/resources/userconstraints/keys.lst");
		uc.addFunctionalDependencies(this.md);
		uc.addForeignKeys(this.md);
		DatabaseRelationDefinition dd = this.md.getDatabaseRelation(idfac.createRelationID(null, "TABLENAME"));
		ForeignKeyConstraint fk = dd.getForeignKeys().get(0);
		assertTrue(fk != null);
		assertEquals(fk.getComponents().get(0).getReference().getRelation().getID(), 
						idfac.createRelationID(null, "TABLE2"));
		assertEquals(fk.getComponents().get(0).getReference().getID(), idfac.createAttributeID("KEY1"));
		assertEquals(dd.getUniqueConstraints().get(0).getAttributes(),
							ImmutableList.of(dd.getAttribute(idfac.createAttributeID("KEYNAME")))); 
	}

	
}
