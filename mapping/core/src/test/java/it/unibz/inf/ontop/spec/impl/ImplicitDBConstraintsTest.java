package it.unibz.inf.ontop.spec.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.ImmutableMetadataLookup;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.spec.dbschema.ImplicitDBConstraintsProviderFactory;
import it.unibz.inf.ontop.dbschema.MetadataProvider;
import it.unibz.inf.ontop.spec.dbschema.impl.ImplicitDBConstraintsProviderFactoryImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;
import static org.junit.Assert.*;

public class ImplicitDBConstraintsTest {

	private static final String DIR = "src/test/resources/userconstraints/";

	private static final MetadataLookup md;
	private static final QuotedIDFactory idfac;

	private static final ImplicitDBConstraintsProviderFactory CONSTRAINT_EXTRACTOR = Guice.createInjector()
			.getInstance(ImplicitDBConstraintsProviderFactoryImpl.class);

	private static final DatabaseRelationDefinition TABLENAME, TABLE2;

	static {
		idfac = DEFAULT_DUMMY_DB_METADATA.getQuotedIDFactory();

		DBTermType stringDBType = DEFAULT_DUMMY_DB_METADATA.getDBTypeFactory().getDBStringType();

		TABLENAME = DEFAULT_DUMMY_DB_METADATA.createDatabaseRelation("TABLENAME",
			"KEYNAME", stringDBType, false);

		TABLE2 = DEFAULT_DUMMY_DB_METADATA.createDatabaseRelation( "TABLE2",
			"KEY1", stringDBType, false,
			"KEY2", stringDBType, false);

		md = new ImmutableMetadataLookup(DEFAULT_DUMMY_DB_METADATA.getDBParameters(),
				ImmutableList.of(TABLENAME, TABLE2));
	}
	
	@Test
	public void testEmptyUserConstraints() throws MetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(
				Optional.of(new File(DIR + "empty_constraints.lst")), idfac);

		List<RelationID> refs = uc.getRelationIDs();
		assertEquals(0, refs.size());
	}

	@Test
	public void testUserPKeys() throws MetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(
				Optional.of(new File(DIR + "pkeys.lst")), idfac);
		List<RelationID> refs = uc.getRelationIDs();
		assertEquals(0, refs.size());
	}

	@Test
	public void testAddPrimaryKeys() throws MetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(
				Optional.of(new File(DIR + "pkeys.lst")), idfac);
		uc.insertIntegrityConstraints(md);
		Attribute attr = TABLENAME.getAttribute(idfac.createAttributeID("KEYNAME"));
		assertEquals(ImmutableList.of(attr), TABLENAME.getUniqueConstraints().get(0).getAttributes());
	}


	@Test
	public void testGetReferredTables() throws MetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(
				Optional.of(new File(DIR + "fkeys.lst")), idfac);
		List<RelationID> refs = uc.getRelationIDs();
		assertEquals(1, refs.size());
		assertTrue(refs.contains(idfac.createRelationID(null, "TABLE2")));
	}

	@Test
	public void testAddForeignKeys() throws MetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(
				Optional.of(new File(DIR + "fkeys.lst")), idfac);
		uc.insertIntegrityConstraints(md);
		ForeignKeyConstraint fk = TABLENAME.getForeignKeys().get(0);
		assertNotNull(fk);
		Attribute ref = fk.getComponents().get(0).getReference();
		assertEquals(ref.getRelation().getID(), idfac.createRelationID(null, "TABLE2"));
		assertEquals(ref.getID(), idfac.createAttributeID("KEY1"));
	}

	@Test
	public void testAddKeys() throws MetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(
				Optional.of(new File(DIR + "keys.lst")), idfac);
		uc.insertIntegrityConstraints(md);
		ForeignKeyConstraint fk = TABLENAME.getForeignKeys().get(0);
		assertNotNull(fk);
		Attribute ref = fk.getComponents().get(0).getReference();
		assertEquals(ref.getRelation().getID(), idfac.createRelationID(null, "TABLE2"));
		assertEquals(ref.getID(), idfac.createAttributeID("KEY1"));
		assertEquals(ImmutableList.of(TABLENAME.getAttribute(idfac.createAttributeID("KEYNAME"))),
				TABLENAME.getUniqueConstraints().get(0).getAttributes());
	}
}
