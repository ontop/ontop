package it.unibz.inf.ontop.spec.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.DBMetadataExtractionException;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.spec.dbschema.ImplicitDBConstraintsProviderFactory;
import it.unibz.inf.ontop.spec.dbschema.MetadataProvider;
import it.unibz.inf.ontop.spec.dbschema.impl.ImplicitDBConstraintsProviderFactoryImpl;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;
import static org.junit.Assert.*;

public class ImplicitDBConstraintsTest {

	private static final String DIR = "src/test/resources/userconstraints/";

	private BasicDBMetadata md;
	private QuotedIDFactory idfac;

	private static final ImplicitDBConstraintsProviderFactory CONSTRAINT_EXTRACTOR = Guice.createInjector()
			.getInstance(ImplicitDBConstraintsProviderFactoryImpl.class);
	
	@Before
	public void setupMetadata(){
		md = DEFAULT_DUMMY_DB_METADATA;
		idfac = md.getDBParameters().getQuotedIDFactory();

		DBTermType stringDBType = md.getDBParameters().getDBTypeFactory().getDBStringType();

		DatabaseRelationDefinition td = md.createDatabaseRelation(new RelationDefinition.AttributeListBuilder(idfac.createRelationID(null, "TABLENAME"))
			.addAttribute(idfac.createAttributeID("KEYNAME"), stringDBType, false));

		DatabaseRelationDefinition td2 = md.createDatabaseRelation(new RelationDefinition.AttributeListBuilder(idfac.createRelationID(null, "TABLE2"))
			.addAttribute(idfac.createAttributeID("KEY1"), stringDBType, false)
			.addAttribute(idfac.createAttributeID("KEY2"), stringDBType, false));
	}
	
	@Test
	public void testEmptyUserConstraints() throws DBMetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(
				Optional.of(new File(DIR + "empty_constraints.lst")), idfac);

		List<RelationID> refs = uc.getRelationIDs();
		assertEquals(0, refs.size());
	}

	@Test
	public void testUserPKeys() throws DBMetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(
				Optional.of(new File(DIR + "pkeys.lst")), idfac);
		List<RelationID> refs = uc.getRelationIDs();
		assertEquals(0, refs.size());
	}

	@Test
	public void testAddPrimaryKeys() throws DBMetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(
				Optional.of(new File(DIR + "pkeys.lst")), idfac);
		uc.insertIntegrityConstraints(this.md);
		DatabaseRelationDefinition dd = this.md.getDatabaseRelation(idfac.createRelationID(null, "TABLENAME"));
		Attribute attr = dd.getAttribute(idfac.createAttributeID("KEYNAME"));
		assertEquals(ImmutableList.of(attr), dd.getUniqueConstraints().get(0).getAttributes());
	}


	@Test
	public void testGetReferredTables() throws DBMetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(
				Optional.of(new File(DIR + "fkeys.lst")), idfac);
		List<RelationID> refs = uc.getRelationIDs();
		assertEquals(1, refs.size());
		assertTrue(refs.contains(idfac.createRelationID(null, "TABLE2")));
	}

	@Test
	public void testAddForeignKeys() throws DBMetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(
				Optional.of(new File(DIR + "fkeys.lst")), idfac);
		uc.insertIntegrityConstraints(this.md);
		DatabaseRelationDefinition dd = this.md.getDatabaseRelation(idfac.createRelationID(null, "TABLENAME"));
		ForeignKeyConstraint fk = dd.getForeignKeys().get(0);
		assertNotNull(fk);
		Attribute ref = fk.getComponents().get(0).getReference();
		assertEquals(ref.getRelation().getID(), idfac.createRelationID(null, "TABLE2"));
		assertEquals(ref.getID(), idfac.createAttributeID("KEY1"));
	}

	@Test
	public void testAddKeys() throws DBMetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(
				Optional.of(new File(DIR + "keys.lst")), idfac);
		uc.insertIntegrityConstraints(this.md);
		DatabaseRelationDefinition dd = this.md.getDatabaseRelation(idfac.createRelationID(null, "TABLENAME"));
		ForeignKeyConstraint fk = dd.getForeignKeys().get(0);
		assertNotNull(fk);
		Attribute ref = fk.getComponents().get(0).getReference();
		assertEquals(ref.getRelation().getID(), idfac.createRelationID(null, "TABLE2"));
		assertEquals(ref.getID(), idfac.createAttributeID("KEY1"));
		assertEquals(ImmutableList.of(dd.getAttribute(idfac.createAttributeID("KEYNAME"))),
						dd.getUniqueConstraints().get(0).getAttributes());
	}
}
