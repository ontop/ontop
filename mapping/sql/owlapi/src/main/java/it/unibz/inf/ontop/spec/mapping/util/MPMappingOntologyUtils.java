package it.unibz.inf.ontop.spec.mapping.util;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.BootConf;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.MPMappingAssertionProducer;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.Pair;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.dictionary.Dictionary;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MPMappingOntologyUtils {

    private static final int PROPERTY_INDEX = 2;

    /**
     * Extract domain and range axioms for object properties
     */
    public static Set<OWLObjectPropertyAxiom> extractObjectPropertyAxioms(OWLOntologyManager manager,
                                                                          SQLPPMapping newPPMapping,
                                                                          TypeFactory typeFactory,
                                                                          boolean bootstrappedMapping,
                                                                          ImmutableList<NamedRelationDefinition> tables,
                                                                          MPMappingAssertionProducer dmap, BootConf bootConf, MetadataProvider metadataProvider){

        OWLDataFactory dataFactory = manager.getOWLDataFactory();

        Set<OWLObjectPropertyAxiom> axiomsSet = new HashSet<>();

        if( metadataProvider == null ){
            throw new RuntimeException("Metadata provider not provided.");
        }

        for (SQLPPTriplesMap triplesMap : newPPMapping.getTripleMaps()) {
            if( !isBootstrapped(triplesMap) ) continue;
            List<TargetAtom> tAList = triplesMap.getTargetAtoms();
            Set<OWLDeclarationAxiom> axioms = DirectMappingOntologyUtils.extractDeclarationAxioms(manager, tAList.stream(), typeFactory, bootstrappedMapping);
            List<Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>>> joinPairs = bootConf.getJoinPairs(metadataProvider);
            Set<Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>>> allPairs =
                    Stream.concat(
                            joinPairs.stream(),
                            joinPairs.stream().map(pair -> new Pair<>(pair.second(), pair.first()))
                    ).collect(Collectors.toSet());

            for( OWLDeclarationAxiom axiom : axioms ){
                if( axiom.getEntity() instanceof OWLObjectProperty ){
                    OWLObjectProperty objProp = (OWLObjectProperty) axiom.getEntity();
                    final String tableFullName = extractTableName(triplesMap); // It could also be a qualified name, e.g. public.tableName
                    String tableSchema = tableFullName.contains(".") ? tableFullName.substring(0, tableFullName.indexOf(".")) : "";

                    NamedRelationDefinition tableDef = null;

                    if( tableSchema.equals("") ) {
                        tableDef = tables.stream()
                                .filter(table -> table.getID().getComponents().get(RelationID.TABLE_INDEX).getName().equalsIgnoreCase(tableFullName))
                                .findFirst().orElse(null);
                    } else{
                        // tableFullName is a qualified name, e.g., public.tableName
                        String tableName = tableFullName.substring(tableFullName.indexOf(".")+1);
                        tableDef = tables.stream()
                                .filter(table -> table.getID().getComponents().get(RelationID.SCHEMA_INDEX).getName().equalsIgnoreCase(tableSchema) &&
                                        table.getID().getComponents().get(RelationID.TABLE_INDEX).getName().equalsIgnoreCase(tableName))
                                .findFirst().orElse(null);
                    }

                    assert( tableDef != null );

                    if( objProp.getIRI().toString().contains("#ref-") ){
                        for (ForeignKeyConstraint fkey : tableDef.getForeignKeys()){
                            axiomsSet.addAll(getDomRangeAxiomsForFkey(objProp, dmap, tableDef, bootConf, fkey, dataFactory));
                        }
                    }
                    else if( objProp.getIRI().toString().contains("#join-") ){
                        // it is an object-property induced by a joinPair
                        // Generate dom/range for the joinPair
                        for( Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>> joinPair : allPairs ){
                            NamedRelationDefinition joinPairLeftTable = MPMappingAssertionProducer.retrieveDatabaseTableDefinition(tables, joinPair.first().get(0).getRelation());
                            if( tableDef.equals(joinPairLeftTable) ){
                                // The current table is the left-table in the joinPair
                                axiomsSet.addAll(getDomRangeAxiomsForJoinPair(objProp, dmap, tables, tableDef, bootConf, joinPair, dataFactory));
                            }
                        }
                    }
                    else{
                        // Renamed object property (hence, corresponding to a fkey)
                        for (ForeignKeyConstraint fkey : tableDef.getForeignKeys()){
                            axiomsSet.addAll(getDomRangeAxiomsForFkey(objProp, dmap, tableDef, bootConf, fkey, dataFactory));
                        }
                    }
                }
            }
        }
        return axiomsSet;
    }

    /** Similar to the method for fkeys, with the difference that the referenced table becomes
     * now the (unique) table occurring in the RHS of a joinPair. **/
    private static Collection<? extends OWLObjectPropertyAxiom> getDomRangeAxiomsForJoinPair(OWLObjectProperty objProp, MPMappingAssertionProducer dmap, ImmutableList<NamedRelationDefinition> tables,
                                                                                             NamedRelationDefinition tableDef,
                                                                                             BootConf bootConf,
                                                                                             Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>> joinPair,
                                                                                             OWLDataFactory dataFactory) {
        Set<OWLObjectPropertyAxiom> axiomsSet = new HashSet<>();

        org.apache.commons.rdf.api.IRI referencedPropIRI = dmap.getReferencePropertyIRI(joinPair, tableDef);

        if (referencedPropIRI.getIRIString().equals(objProp.getIRI().toString())) {
            NamedRelationDefinition rightTable = MPMappingAssertionProducer.retrieveDatabaseTableDefinition(tables, joinPair.second().get(0).getRelation());
            String tableIRI = dmap.getTableIRIString(tableDef, bootConf.getDictionary());
            String referencedTableIRI = dmap.getTableIRIString(rightTable, bootConf.getDictionary());

            OWLClass domain = dataFactory.getOWLClass(IRI.create(tableIRI));
            OWLClass range = dataFactory.getOWLClass(IRI.create(referencedTableIRI));

            axiomsSet.add(dataFactory.getOWLObjectPropertyDomainAxiom(objProp, domain));
            axiomsSet.add(dataFactory.getOWLObjectPropertyRangeAxiom(objProp, range));
        }
        return axiomsSet;
    }

    // Warnings:
    //  1) this function assumes that the sql query is bootstrapped;
    //  2) this function assumes that the table name to retrieve
    //     is the first argument of the comma-separated join
    //     (SELECT ... FROM tableName, refTableName WHERE ...)
    // FIXME> Is there a safer way to extract a table name from a triplesMap?
    private static String extractTableName(SQLPPTriplesMap triplesMap) {
        String source = triplesMap.getSourceQuery().getSQL();
        String fromStart = source.substring(source.indexOf("FROM "));
        String tableName = fromStart.substring("FROM ".length(), fromStart.indexOf(","));

        return tableName.replaceAll("\"|\"", ""); // Unquote
    }

    // FIXME> Render this safer, probably by adding a boolean in SQLPPTriplesMap.
    private static boolean isBootstrapped(SQLPPTriplesMap triplesMap) {
        String id = triplesMap.getId();
        boolean result = id.matches("BOOTSTRAPPED-MAPPING-ID[0-9]+");
        return result;
    }

    private static Collection<? extends OWLObjectPropertyAxiom> getDomRangeAxiomsForFkey(OWLObjectProperty objProp, MPMappingAssertionProducer dmap, NamedRelationDefinition tableDef, BootConf bootConf, ForeignKeyConstraint fkey, OWLDataFactory dataFactory) {

        Set<OWLObjectPropertyAxiom> axiomsSet = new HashSet<>();

        org.apache.commons.rdf.api.IRI referencedPropIRI = dmap.getReferencePropertyIRI(fkey,bootConf);

        if (referencedPropIRI.getIRIString().equals(objProp.getIRI().toString())) {
            NamedRelationDefinition referencedTable = fkey.getReferencedRelation();
            String tableIRI = dmap.getTableIRIString(tableDef, bootConf.getDictionary());
            String referencedTableIRI = dmap.getTableIRIString(referencedTable, bootConf.getDictionary());

            OWLClass domain = dataFactory.getOWLClass(IRI.create(tableIRI));
            OWLClass range = dataFactory.getOWLClass(IRI.create(referencedTableIRI));

            axiomsSet.add(dataFactory.getOWLObjectPropertyDomainAxiom(objProp, domain));
            axiomsSet.add(dataFactory.getOWLObjectPropertyRangeAxiom(objProp, range));
        }
        return axiomsSet;
    }

    /**
     * It creates domain and range OWL axioms
     */
    public static Set<OWLDataPropertyAxiom> extractDataPropertyAxioms(OWLOntologyManager manager,
                                                                            List<ImmutableList<TargetAtom>> mappingsTargetsList,
                                                                            TypeFactory typeFactory,
                                                                            boolean bootstrappedMapping) {

        OWLDataFactory dataFactory = manager.getOWLDataFactory();

        Set<OWLDataPropertyAxiom> axiomsSet = new HashSet<>();

        for (List<TargetAtom> tAList : mappingsTargetsList) {
            Set<OWLDeclarationAxiom> axioms = DirectMappingOntologyUtils.extractDeclarationAxioms(manager, tAList.stream(), typeFactory, bootstrappedMapping);
            Optional<OWLDeclarationAxiom> classDeclarationAxiom = axioms.stream()
                    .filter(ax -> ax.getEntity() instanceof OWLClass)
                    .findFirst();

            // If not present, then we are dealing with an object-property mapping
            OWLEntity classEntity = classDeclarationAxiom.map(OWLDeclarationAxiom::getEntity).orElse(null);

            axiomsSet.addAll(
                    axioms.stream()
                            .filter(ax -> ax.getEntity() instanceof OWLDataProperty)
                            .map(axiom -> dataFactory.getOWLDataPropertyDomainAxiom((OWLDataPropertyExpression) axiom.getEntity(), (OWLClassExpression) classEntity))
                            .collect(Collectors.toList()));

            axiomsSet.addAll(treatDataPropertiesRanges(manager, tAList, dataFactory, typeFactory, bootstrappedMapping));
        }
        return axiomsSet;
    }

    private static Collection<? extends OWLDataPropertyRangeAxiom> treatDataPropertiesRanges(OWLOntologyManager manager, List<TargetAtom> tAList, OWLDataFactory dataFactory, TypeFactory typeFactory, boolean bootstrappedMapping) {
        Set<OWLDataPropertyRangeAxiom> rangeAxiomSet = new HashSet<>();

        OWLDataFactory factory = manager.getOWLDataFactory();

        for( TargetAtom tA : tAList ){
            OWLEntity entity = extractEntity(tA, dataFactory, typeFactory, bootstrappedMapping);
            OWLDeclarationAxiom axiom = dataFactory.getOWLDeclarationAxiom(entity);

            if( entity instanceof OWLDataProperty ){
                // Get object and infer type
                Optional<TermTypeInference> type = tA.getSubstitutedTerm(PROPERTY_INDEX).inferType();
                if( type.isPresent() ){
                    RDFDatatype rdfDatatype = (RDFDatatype) type.get().getTermType().get();
                    OWLDataRange owlDataRange = factory.getOWLDatatype(IRI.create(rdfDatatype.getIRI().getIRIString()));
                    rangeAxiomSet.add(dataFactory.getOWLDataPropertyRangeAxiom((OWLDataPropertyExpression) axiom.getEntity(), owlDataRange));
                }
            }
        }
        return rangeAxiomSet;
    }

    private static OWLEntity extractEntity(TargetAtom targetAtom, OWLDataFactory dataFactory,
                                           TypeFactory typeFactory, boolean bootstrappedMapping) {

        ImmutableList<ImmutableTerm> terms = targetAtom.getSubstitutedTerms();
        RDFAtomPredicate predicate = (RDFAtomPredicate) targetAtom.getProjectionAtom().getPredicate();

        Optional<org.apache.commons.rdf.api.IRI> classIRI = predicate.getClassIRI(terms);
        Optional<org.apache.commons.rdf.api.IRI> propertyIRI = predicate.getPropertyIRI(terms);

        if (classIRI.isPresent()) {
            return dataFactory.getOWLClass(IRI.create(classIRI.get().getIRIString()));
        }
        if (!propertyIRI.isPresent()) {
            throw new MinorOntopInternalBugException("No IRI could be extracted from " + targetAtom);
        }

        IRI iri = IRI.create(propertyIRI.get().getIRIString());

        ImmutableTerm objectTerm = predicate.getObject(terms);

        if (objectTerm instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm objectFunctionalTerm = (ImmutableFunctionalTerm) objectTerm;

            TermType termType = objectFunctionalTerm.inferType()
                    .flatMap(TermTypeInference::getTermType)
                    .filter(t -> t.isA(typeFactory.getAbstractRDFTermType()))
                    .orElseThrow(() -> new MinorOntopInternalBugException(
                            "Could not infer the RDF type of " + objectFunctionalTerm));

            return (termType.isA(typeFactory.getAbstractRDFSLiteral()))
                    ? dataFactory.getOWLDataProperty(iri)
                    : dataFactory.getOWLObjectProperty(iri);
        }
        if (bootstrappedMapping) {
            throw new MinorOntopInternalBugException("A functional term was expected for the object: " + objectTerm);
        }
        return dataFactory.getOWLDataProperty(iri);
    }

    // This method cycles through all tables, and checks whether there is a "parent table"
    //  (i.e., if there is a fkey -> pkey situation)
    // If there is, it adds an inclusion in the ontology
    public static Set<OWLSubClassOfAxiom> extractSubclassAxioms(OWLOntologyManager manager, String baseIRI, ImmutableList<NamedRelationDefinition> tables, BootConf bootConf) {

        if( !bootConf.isEnableSH() ) return new HashSet<>();

        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        Set<OWLSubClassOfAxiom> axiomsSet = new HashSet<>();
        Dictionary dictionary = bootConf.getDictionary();

        // Add the subsumption A \sqsubsteq B only if
        // 1) both A and B appear in the dictionary, or
        // 2) if the dictionary is empty
        for( NamedRelationDefinition table : tables ){
            if( dictionary.isEmpty() || dictionary.containsTable(table.getID().getComponents().get(RelationID.TABLE_INDEX).getName())) {
                List<NamedRelationDefinition> parentTables = MPMappingAssertionProducer.retrieveParentTables(table);
                for (NamedRelationDefinition pT : parentTables) {
                    if (dictionary.isEmpty() || dictionary.containsTable(pT.getID().getComponents().get(RelationID.TABLE_INDEX).getName())) {
                        String tableAlias = dictionary.isEmpty() ? table.getID().getComponents().get(RelationID.TABLE_INDEX).getName() : dictionary.getTableAlias(table.getID().getComponents().get(RelationID.TABLE_INDEX).getName());
                        String parentAlias = dictionary.isEmpty() ? pT.getID().getComponents().get(RelationID.TABLE_INDEX).getName() : dictionary.getTableAlias(pT.getID().getComponents().get(RelationID.TABLE_INDEX).getName());

                        // Create axioms:
                        OWLClass subclass = dataFactory.getOWLClass(IRI.create(baseIRI, tableAlias));
                        OWLClass superclass = dataFactory.getOWLClass(IRI.create(baseIRI, parentAlias));

                        axiomsSet.add(dataFactory.getOWLSubClassOfAxiom(subclass, superclass));
                    }
                }
            }
        }
        return axiomsSet;
    }

    public static Set<OWLAxiom> createAnnotations(OWLOntologyManager manager, String baseIRI, SQLPPMapping newPPMapping, TypeFactory typeFactory, boolean bootstrappedMapping, BootConf bootConf) {
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        Dictionary dict = bootConf.getDictionary();

        Set<OWLAxiom> result = new HashSet<>();

        for (SQLPPTriplesMap triplesMap : newPPMapping.getTripleMaps()) {
            if (!isBootstrapped(triplesMap)) continue;
            List<TargetAtom> tAList = triplesMap.getTargetAtoms();
            Set<OWLDeclarationAxiom> axioms = DirectMappingOntologyUtils.extractDeclarationAxioms(manager, tAList.stream(), typeFactory, bootstrappedMapping);
            for (OWLDeclarationAxiom axiom : axioms) {
                if (axiom.getEntity() instanceof OWLClass) {
                    result.addAll(generateAnnotationsForClass(axiom, dataFactory, dict));
                } else if (axiom.getEntity() instanceof OWLDataProperty) {
                    result.addAll(generateAnnotationsForDataProperty(axiom, dataFactory, dict));
                } else if (axiom.getEntity() instanceof OWLObjectProperty ) {
                    result.addAll(generateAnnotationsForObjectProperty(axiom, dataFactory, dict));
                }
            }
        }
        return result;
    }

    private static Collection<? extends OWLAxiom> generateAnnotationsForObjectProperty(OWLDeclarationAxiom axiom, OWLDataFactory dataFactory, Dictionary dict) {
        OWLAnnotationProperty labelProp = dataFactory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
        OWLAnnotationProperty commentProp = dataFactory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());
        IRI iri = ((OWLObjectProperty) axiom.getEntity()).getIRI();
        String tableAlias = iri.toString().substring(iri.getNamespace().lastIndexOf("/") + 1, iri.getNamespace().indexOf("#"));
        String joinAlias = iri.getFragment();
        // TODO Add object property comments
        return getJoinLabelsForTableAndJoinAlias(tableAlias, joinAlias, dict)
                .stream()
                .map(label -> dataFactory.getOWLAnnotationAssertionAxiom(iri, dataFactory.getOWLAnnotation(labelProp, dataFactory.getOWLLiteral(label))))
                .collect(Collectors.toSet());
    }

    private static Set<OWLAxiom> generateAnnotationsForDataProperty(OWLDeclarationAxiom axiom, OWLDataFactory dataFactory, Dictionary dict) {
        OWLAnnotationProperty labelProp = dataFactory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
        OWLAnnotationProperty commentProp = dataFactory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());
        IRI iri = ((OWLDataProperty) axiom.getEntity()).getIRI();
        String tableAlias = iri.toString().substring(iri.getNamespace().lastIndexOf("/") + 1, iri.getNamespace().indexOf("#"));
        String attAlias = iri.getFragment();
        String comment = getCommentForAttribute(tableAlias, attAlias, dict);
        Set<OWLAxiom> result = new HashSet<>();
        if( !comment.equals("") ){
            result.add(dataFactory.getOWLAnnotationAssertionAxiom(iri, dataFactory.getOWLAnnotation(commentProp, dataFactory.getOWLLiteral(comment))));
        }
        result.addAll(getLabelsForAttribute(tableAlias, attAlias, dict)
                .stream()
                .map(label -> dataFactory.getOWLAnnotationAssertionAxiom(iri, dataFactory.getOWLAnnotation(labelProp, dataFactory.getOWLLiteral(label))))
                .collect(Collectors.toSet()));
        return result;
    }

    private static String getCommentForAttribute(String tableAlias, String attAlias, Dictionary dict) {
        for( Dictionary.DictEntry de : dict.getDictEntries() ){
            if(de.getTableAlias().equals(tableAlias)){
                for(Dictionary.DictEntry.AttAlias alias : de.getAttAliases() ){
                    if( attAlias.equals(alias.getAttAlias()) ){ // FIXME: Potential problem: If two attributes have the same alios, this can become problematic
                        return alias.getAttComment();
                    }
                }
            }
        }
        return "";
    }

    private static Set<OWLAxiom> generateAnnotationsForClass(OWLDeclarationAxiom axiom, OWLDataFactory dataFactory, Dictionary dict) {
        OWLAnnotationProperty labelProp = dataFactory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
        OWLAnnotationProperty commentProp = dataFactory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());
        IRI iri = ((OWLClass) axiom.getEntity()).getIRI();
        String tableAlias = iri.getFragment();
        String comment = getCommentForTable(tableAlias, dict);
        Set<OWLAxiom> result = new HashSet<>();
        if( !comment.equals("") ){
            result.add(dataFactory.getOWLAnnotationAssertionAxiom(iri, dataFactory.getOWLAnnotation(commentProp, dataFactory.getOWLLiteral(comment))));
        }
        result.addAll(getLabelsForTable(tableAlias, dict)
                .stream()
                .map(label -> dataFactory
                        .getOWLAnnotationAssertionAxiom(iri, dataFactory.getOWLAnnotation(labelProp, dataFactory.getOWLLiteral(label))))
                .collect(Collectors.toSet()));
        return result;
    }

    private static List<String> getLabelsForTable(String tableAlias, Dictionary dict){
        for(Dictionary.DictEntry de : dict.getDictEntries() ){
            if(de.getTableAlias().equals(tableAlias))
                return de.getTableLabels();
        }
        return new LinkedList<>();
    }

    private static String getCommentForTable(String tableAlias, Dictionary dict){
        for( Dictionary.DictEntry de : dict.getDictEntries() ){
            if(de.getTableAlias().equals(tableAlias))
                return de.getTableComment();
        }
        return "";
    }

    private static List<String> getJoinLabelsForTableAndJoinAlias(String tableAlias, String joinAlias, Dictionary dict){
        for(Dictionary.DictEntry de : dict.getDictEntries() ){
            if(de.getTableAlias().equals(tableAlias))
                for( Dictionary.DictEntry.Reference ref : de.getReferences() ){
                    if( ref.getJoinAlias().equals(joinAlias) ){
                        return ref.getJoinLabels();
                    }
                }
        }
        return new LinkedList<>();
    }

    private static List<String> getLabelsForAttribute(String tableAlias, String attAlias, Dictionary dict){
        for( Dictionary.DictEntry de : dict.getDictEntries() ){
            if( de.getTableAlias().equals(tableAlias) ){
                for( Dictionary.DictEntry.AttAlias attAlias1 : de.getAttAliases() ){
                    if( attAlias1.getAttAlias().equals(attAlias) ){
                        return attAlias1.getAttLabels();
                    }
                }
            }
        }
        return new LinkedList<>();
    }
}
