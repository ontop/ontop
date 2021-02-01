package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.AbstractRelationDefinition;
import it.unibz.inf.ontop.dbschema.impl.OntopViewDefinitionImpl;
import it.unibz.inf.ontop.dbschema.impl.RawQuotedIDFactory;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.type.UniqueTermTypeExtractor;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.spec.sqlparser.ExpressionParser;
import it.unibz.inf.ontop.spec.sqlparser.RAExpressionAttributes;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@JsonPropertyOrder({
        "relations"
})
@JsonDeserialize(as = JsonBasicView.class)
public class JsonBasicView extends JsonView {
    @Nonnull
    public final Columns columns;
    @Nonnull
    public final List<String> baseRelation;
    @Nonnull
    public final UniqueConstraints uniqueConstraints;
    @Nonnull
    public final OtherFunctionalDependencies otherFunctionalDependencies;
    @Nonnull
    public final ForeignKeys foreignKeys;

    protected static final Logger LOGGER = LoggerFactory.getLogger(JsonBasicView.class);

    @JsonCreator
    public JsonBasicView(@JsonProperty("columns") Columns columns, @JsonProperty("name") List<String> name,
                         @JsonProperty("baseRelation") List<String> baseRelation,
                         @JsonProperty("uniqueConstraints") UniqueConstraints uniqueConstraints,
                         @JsonProperty("otherFunctionalDependencies") OtherFunctionalDependencies otherFunctionalDependencies,
                         @JsonProperty("foreignKeys") ForeignKeys foreignKeys) {
        super(name);
        this.columns = columns;
        this.baseRelation = baseRelation;
        this.uniqueConstraints = uniqueConstraints;
        this.otherFunctionalDependencies = otherFunctionalDependencies;
        this.foreignKeys = foreignKeys;
    }

    @Override
    public OntopViewDefinition createViewDefinition(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException {

        QuotedIDFactory quotedIDFactory = dbParameters.getQuotedIDFactory();
        RelationID relationId = quotedIDFactory.createRelationID(name.toArray(new String[0]));

        NamedRelationDefinition parentDefinition = parentCacheMetadataLookup.getRelation(quotedIDFactory.createRelationID(
                baseRelation.toArray(new String[0])));

        IQ iq = createIQ(relationId, parentDefinition, dbParameters);

        // For added columns the termtype, quoted ID and nullability all need to come from the IQ
        RelationDefinition.AttributeListBuilder attributeBuilder = createAttributeBuilder(iq, dbParameters);

        return new OntopViewDefinitionImpl(
                ImmutableList.of(relationId),
                attributeBuilder,
                iq,
                // TODO: consider other levels
                1,
                dbParameters.getCoreSingletons());
    }

    @Override
    public void insertIntegrityConstraints(NamedRelationDefinition relation,
                                           ImmutableList<NamedRelationDefinition> baseRelations,
                                           MetadataLookup metadataLookupForFK) throws MetadataExtractionException {

        QuotedIDFactory idFactory = metadataLookupForFK.getQuotedIDFactory();

        insertUniqueConstraints(relation, idFactory, uniqueConstraints.added, baseRelations);

        insertFunctionalDependencies(relation, idFactory, otherFunctionalDependencies.added, baseRelations);

        for (AddForeignKey fk : foreignKeys.added) {
            insertForeignKeys(relation, metadataLookupForFK, fk);
        }
    }


    private IQ createIQ(RelationID relationId, NamedRelationDefinition parentDefinition, DBParameters dbParameters)
            throws MetadataExtractionException {

        CoreSingletons coreSingletons = dbParameters.getCoreSingletons();

        TermFactory termFactory = coreSingletons.getTermFactory();
        IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();
        AtomFactory atomFactory = coreSingletons.getAtomFactory();
        QuotedIDFactory quotedIdFactory = dbParameters.getQuotedIDFactory();

        ImmutableSet<Variable> addedVariables = columns.added.stream()
                .map(a -> a.name)
                .map(attributeName -> normalizeAttributeName(attributeName, quotedIdFactory))
                .map(termFactory::getVariable)
                .collect(ImmutableCollectors.toSet());

        ImmutableList<Variable> projectedVariables = extractRelationVariables(addedVariables, columns.hidden, parentDefinition,
                dbParameters);

        ImmutableMap<Integer, Variable> parentArgumentMap = createParentArgumentMap(addedVariables, parentDefinition,
                coreSingletons.getCoreUtilsFactory());
        ExtensionalDataNode parentDataNode = iqFactory.createExtensionalDataNode(parentDefinition, parentArgumentMap);

        ConstructionNode constructionNode = createConstructionNode(projectedVariables, parentDefinition,
                parentArgumentMap, dbParameters);

        IQTree iqTree = iqFactory.createUnaryIQTree(
                constructionNode,
                parentDataNode);

        AtomPredicate tmpPredicate = createTemporaryPredicate(relationId, projectedVariables.size(), coreSingletons);
        DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(tmpPredicate, projectedVariables);

        return iqFactory.createIQ(projectionAtom, iqTree)
                .normalizeForOptimization();
    }

    private ImmutableList<Variable> extractRelationVariables(ImmutableSet<Variable> addedVariables, List<String> hidden,
                                                             NamedRelationDefinition parentDefinition, DBParameters dbParameters) {
        TermFactory termFactory = dbParameters.getCoreSingletons().getTermFactory();
        QuotedIDFactory quotedIdFactory = dbParameters.getQuotedIDFactory();

        ImmutableList<String> hiddenColumnNames = hidden.stream()
                .map(attributeName -> normalizeAttributeName(attributeName, quotedIdFactory))
                .collect(ImmutableCollectors.toList());

        ImmutableList<Variable> inheritedVariableStream = parentDefinition.getAttributes().stream()
                .map(a -> a.getID().getName())
                .filter(n -> !hiddenColumnNames.contains(n))
                .map(termFactory::getVariable)
                .filter(v -> !addedVariables.contains(v))
                .collect(ImmutableCollectors.toList());

        return Stream.concat(
                addedVariables.stream(),
                inheritedVariableStream.stream())
                .collect(ImmutableCollectors.toList());
    }

    private String normalizeAttributeName(String attributeName, QuotedIDFactory quotedIdFactory) {
        return quotedIdFactory.createAttributeID(attributeName).getName();
    }

    private AtomPredicate createTemporaryPredicate(RelationID relationId, int arity, CoreSingletons coreSingletons) {
        DBTermType dbRootType = coreSingletons.getTypeFactory().getDBTypeFactory().getAbstractRootDBType();

        return new TemporaryViewPredicate(
                relationId.getSQLRendering(),
                // No precise base DB type for the temporary predicate
                IntStream.range(0, arity)
                        .boxed()
                        .map(i -> dbRootType).collect(ImmutableCollectors.toList()));
    }

    private ImmutableMap<Integer, Variable> createParentArgumentMap(ImmutableSet<Variable> addedVariables,
                                                                    NamedRelationDefinition parentDefinition,
                                                                    CoreUtilsFactory coreUtilsFactory) {
        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(addedVariables);

        ImmutableList<Attribute> parentAttributes = parentDefinition.getAttributes();

        // NB: the non-necessary variables will be pruned out by normalizing the IQ
        return IntStream.range(0, parentAttributes.size())
                .boxed()
                .collect(ImmutableCollectors.toMap(
                        i -> i,
                        i -> variableGenerator.generateNewVariable(
                                parentAttributes.get(i).getID().getName())));

    }

    private ConstructionNode createConstructionNode(ImmutableList<Variable> projectedVariables,
                                                    NamedRelationDefinition parentDefinition,
                                                    ImmutableMap<Integer, Variable> parentArgumentMap,
                                                    DBParameters dbParameters) throws MetadataExtractionException {

        QuotedIDFactory quotedIdFactory = dbParameters.getQuotedIDFactory();
        CoreSingletons coreSingletons = dbParameters.getCoreSingletons();
        TermFactory termFactory = coreSingletons.getTermFactory();
        IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();
        SubstitutionFactory substitutionFactory = coreSingletons.getSubstitutionFactory();

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> parentAttributeMap = parentArgumentMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> new QualifiedAttributeID(null, parentDefinition.getAttributes().get(e.getKey()).getID()),
                        Map.Entry::getValue));


        ImmutableMap.Builder<Variable, ImmutableTerm> substitutionMapBuilder = ImmutableMap.builder();
        for (AddColumns a : columns.added) {
            Variable v = termFactory.getVariable(normalizeAttributeName(a.name, quotedIdFactory));
            try {
                ImmutableTerm value = extractExpression(a.expression, parentAttributeMap, quotedIdFactory, coreSingletons);
                substitutionMapBuilder.put(v, value);
            } catch (JSQLParserException e) {
                throw new MetadataExtractionException("Unsupported expression for " + a.name + " in " + name + ":\n" + e);
            }
        }

        return iqFactory.createConstructionNode(
                ImmutableSet.copyOf(projectedVariables),
                substitutionFactory.getSubstitution(substitutionMapBuilder.build()));
    }

    private ImmutableTerm extractExpression(String partialExpression,
                                            ImmutableMap<QualifiedAttributeID, ImmutableTerm> parentAttributeMap,
                                            QuotedIDFactory quotedIdFactory, CoreSingletons coreSingletons) throws JSQLParserException {
        String sqlQuery = "SELECT " + partialExpression + " FROM fakeTable";
        ExpressionParser parser = new ExpressionParser(quotedIdFactory, coreSingletons);
        Statement statement = CCJSqlParserUtil.parse(sqlQuery);
        SelectItem si = ((PlainSelect) ((Select) statement).getSelectBody()).getSelectItems().get(0);
        net.sf.jsqlparser.expression.Expression exp = ((SelectExpressionItem) si).getExpression();
        return parser.parseTerm(exp, new RAExpressionAttributes(parentAttributeMap, null));
    }

    private RelationDefinition.AttributeListBuilder createAttributeBuilder(IQ iq, DBParameters dbParameters) throws MetadataExtractionException {
        UniqueTermTypeExtractor uniqueTermTypeExtractor = dbParameters.getCoreSingletons().getUniqueTermTypeExtractor();
        QuotedIDFactory quotedIdFactory = dbParameters.getQuotedIDFactory();

        RelationDefinition.AttributeListBuilder builder = AbstractRelationDefinition.attributeListBuilder();
        IQTree iqTree = iq.getTree();

        RawQuotedIDFactory rawQuotedIqFactory = new RawQuotedIDFactory(quotedIdFactory);

        for (Variable v : iqTree.getVariables()) {
            builder.addAttribute(rawQuotedIqFactory.createAttributeID(v.getName()),
                    (DBTermType) uniqueTermTypeExtractor.extractUniqueTermType(v, iqTree)
                            // TODO: give the name of the view
                            .orElseThrow(() -> new MetadataExtractionException("No type inferred for " + v + " in " + iq)),
                    iqTree.getVariableNullability().isPossiblyNullable(v));
        }
        return builder;
    }

    private void insertUniqueConstraints(NamedRelationDefinition relation,
                                         QuotedIDFactory idFactory,
                                         List<AddUniqueConstraints> addUniqueConstraints,
                                         ImmutableList<NamedRelationDefinition> baseRelations) {


        List<AddUniqueConstraints> list = extractUniqueConstraints(addUniqueConstraints, baseRelations, idFactory);

        for (AddUniqueConstraints addUC : list) {
            try {
                FunctionalDependency.Builder builder = addUC.isPrimaryKey
                        ? UniqueConstraint.primaryKeyBuilder(relation, addUC.name)
                        : UniqueConstraint.builder(relation, addUC.name);
                JsonMetadata.deserializeAttributeList(idFactory, addUC.determinants, builder::addDeterminant);
                builder.build();
            }
            // If the determinant column has been hidden or does not exist
            catch (MetadataExtractionException e) {
                LOGGER.warn("Cannot find correct unique constraint determinant {} for relation {}", addUC.name, relation.getID());
            }
        }
    }

    /**
     * Infer unique constraints from the parent
     * Streams of lists have an intrinsic order and added constraints come first in case of duplicates thus:
     * 1. Added constraints take precedence over inferred (i.e. can overwrite PK true/false definition)
     * 2. No precedence defined for duplicated added constraints (i.e. same constraint defined with PK true/false returns random result)
     */
    private List<AddUniqueConstraints> extractUniqueConstraints(List<AddUniqueConstraints> addUniqueConstraints,
                                                                ImmutableList<NamedRelationDefinition> baseRelations,
                                                                QuotedIDFactory idFactory){

        // List of constraints added
        ImmutableList<String> addedConstraintsColumns = uniqueConstraints.added.stream()
                .map(a -> a.determinants)
                .flatMap(Collection::stream)
                .collect(ImmutableCollectors.toList());

        // List of columns added
        ImmutableList<String> addedNewColumns = columns.added.stream()
                .map(a -> a.name)
                .collect(ImmutableCollectors.toList());

        // List of hidden columns
        ImmutableList<String> hiddenColumnNames = columns.hidden.stream()
                .map(attributeName -> normalizeAttributeName(attributeName, idFactory))
                .collect(ImmutableCollectors.toList());

        // Filter inherited constraints
        ImmutableList<UniqueConstraint> inheritedConstraints = baseRelations.stream()
                .map(RelationDefinition::getUniqueConstraints)
                .flatMap(Collection::stream)
                .filter(c -> c.getAttributes().stream()
                        .map(a -> a.getID().getName())
                        .noneMatch(addedConstraintsColumns::contains))
                .filter(c -> c.getAttributes().stream()
                        .map(a -> a.getID().getName())
                        .noneMatch(addedNewColumns::contains))
                .filter(c -> c.getAttributes().stream()
                        .map(a -> a.getID().getName())
                        .noneMatch(hiddenColumnNames::contains))
                .collect(ImmutableCollectors.toList());

        // Create unique constraints
        List<AddUniqueConstraints> inferredUniqueConstraints = inheritedConstraints.stream()
                .map(i -> new AddUniqueConstraints(
                        i.getName(),
                        i.getDeterminants().stream()
                                .map(c -> c.getID().getSQLRendering())
                                .collect(Collectors.toList()),
                        // If no PK defined in added columns keep parent PK
                        i.isPrimaryKey() && addUniqueConstraints.stream()
                                .noneMatch(k -> k.isPrimaryKey.equals(true))
                            ))
                .collect(Collectors.toList());

        // Throw a warning if duplicate unique constraints are added
        if (!Stream.concat(addUniqueConstraints.stream(), inferredUniqueConstraints.stream()).allMatch(new HashSet<>()::add))
            LOGGER.warn("Duplicate unique constraints defined in schema");

        // Return full list of added and inherited constraints, remove duplicates based on constraint attribute
        return Stream.concat(addUniqueConstraints.stream(), inferredUniqueConstraints.stream())
                .distinct()
                .collect(Collectors.toList());
    }

    private void insertFunctionalDependencies(NamedRelationDefinition relation,
                                              QuotedIDFactory idFactory,
                                              List<AddFunctionalDependency> addFunctionalDependency,
                                              ImmutableList<NamedRelationDefinition> baseRelations) {

        List<AddFunctionalDependency> list = extractOtherFunctionalDependencies(addFunctionalDependency, baseRelations, idFactory);

        for (AddFunctionalDependency addFD : list) {
            FunctionalDependency.Builder builder = FunctionalDependency.defaultBuilder(relation);

            try {
                JsonMetadata.deserializeAttributeList(idFactory, addFD.determinants, builder::addDeterminant);
            }
            // If the determinant column has been hidden or does not exist
            catch (MetadataExtractionException e) {
                LOGGER.warn("Cannot find determinant {} for Functional Dependency", addFD.determinants);
            }

            try {
                JsonMetadata.deserializeAttributeList(idFactory, addFD.dependents, builder::addDependent);
            }
            // If the dependent column has been hidden or does not exist
            catch (MetadataExtractionException e) {
                LOGGER.warn("Cannot find dependent {} for Functional Dependency", addFD.dependents);
            }

            builder.build();
        }
    }

    /**
     * Infer functional dependencies from the parent
     */
    private List<AddFunctionalDependency> extractOtherFunctionalDependencies(List<AddFunctionalDependency> addFunctionalDependencies,
                                                                ImmutableList<NamedRelationDefinition> baseRelations,
                                                                             QuotedIDFactory idFactory){

        // List of added columns
        ImmutableList<String> addedNewColumns = columns.added.stream()
                .map(a -> a.name)
                .collect(ImmutableCollectors.toList());

        // List of hidden columns
        ImmutableList<String> hiddenColumnNames = columns.hidden.stream()
                .map(attributeName -> normalizeAttributeName(attributeName, idFactory))
                .collect(ImmutableCollectors.toList());

        // Filter inherited functional dependencies
        ImmutableList<FunctionalDependency> inheritedFunctionalDependencies = baseRelations.stream()
                .map(RelationDefinition::getOtherFunctionalDependencies)
                .flatMap(Collection::stream)
                .filter(f -> f.getDeterminants().stream()
                        .map(d -> d.getID().getName())
                        .noneMatch(addedNewColumns::contains))
                .filter(f -> f.getDeterminants().stream()
                        .map(d -> d.getID().getName())
                        .noneMatch(hiddenColumnNames::contains))
                .filter(f -> f.getDependents().stream()
                        .map(d -> d.getID().getName())
                        .noneMatch(addedNewColumns::contains))
                .filter(f -> f.getDependents().stream()
                        .map(d -> d.getID().getName())
                        .noneMatch(hiddenColumnNames::contains))
                .collect(ImmutableCollectors.toList());

        List<AddFunctionalDependency> inferredFunctionalDependencies = inheritedFunctionalDependencies.stream()
                .map(i -> new AddFunctionalDependency(
                        i.getDeterminants().stream()
                                .map(d -> d.getID().getSQLRendering())
                                .collect(Collectors.toList()),
                        i.getDependents().stream()
                                .map(d -> d.getID().getSQLRendering())
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());

        // Throw a warning if duplicate functional dependencies are added
        if (!Stream.concat(addFunctionalDependencies.stream(), inferredFunctionalDependencies.stream()).allMatch(new HashSet<>()::add))
            LOGGER.warn("Duplicate functional dependencies defined in schema");

        // Return the unique list of functional dependencies (apply distinct)
        return Stream.concat(addFunctionalDependencies.stream(), inferredFunctionalDependencies.stream()).distinct()
                .collect(Collectors.toList());
    }

    public void insertForeignKeys(NamedRelationDefinition relation, MetadataLookup lookup, AddForeignKey addForeignKey) throws MetadataExtractionException {

        RelationID targetRelationId = JsonMetadata.deserializeRelationID(lookup.getQuotedIDFactory(), addForeignKey.to.relation);
        NamedRelationDefinition targetRelation;
        try {
            targetRelation = lookup.getRelation(targetRelationId);
        }
        // If the target relation has not
        catch (MetadataExtractionException e) {
            LOGGER.info("Cannot find relation {} for FK {}", targetRelationId, addForeignKey.name);
            return ;
        }

        ForeignKeyConstraint.Builder builder = ForeignKeyConstraint.builder(addForeignKey.name, relation, targetRelation);

        try {
            for (String column : addForeignKey.to.columns)
                builder.add(
                        lookup.getQuotedIDFactory().createAttributeID(addForeignKey.from),
                        lookup.getQuotedIDFactory().createAttributeID(column));
        }
        catch (AttributeNotFoundException e) {
            throw new MetadataExtractionException(e);
        }

        builder.build();
    }

    @JsonPropertyOrder({
            "added",
            "hidden"
    })
    private static class Columns extends JsonOpenObject {
        @Nonnull
        public final List<AddColumns> added;
        @Nonnull
        public final List<String> hidden;

        @JsonCreator
        public Columns(@JsonProperty("added") List<AddColumns> added,
                       @JsonProperty("hidden") List<String> hidden) {
            this.added = added;
            this.hidden = hidden;
        }
    }

    @JsonPropertyOrder({
            "name",
            "expression",
    })
    private static class AddColumns extends JsonOpenObject {
        @Nonnull
        public final String name;
        @Nonnull
        public final String expression;


        @JsonCreator
        public AddColumns(@JsonProperty("name") String name,
                          @JsonProperty("expression") String expression) {
            this.name = name;
            this.expression = expression;
        }
    }

    @JsonPropertyOrder({
            "added"
    })
    private static class UniqueConstraints extends JsonOpenObject {
        @Nonnull
        public final List<AddUniqueConstraints> added;

        @JsonCreator
        public UniqueConstraints(@JsonProperty("added") List<AddUniqueConstraints> added) {
            this.added = added;
        }
    }

    @JsonPropertyOrder({
            "name",
            "determinants",
            "isPrimaryKey"
    })
    private static class AddUniqueConstraints extends JsonOpenObject {
        @Nonnull
        public final String name;
        @Nonnull
        public final List<String> determinants;
        @Nonnull
        public final Boolean isPrimaryKey;


        @JsonCreator
        public AddUniqueConstraints(@JsonProperty("name") String name,
                                    @JsonProperty("determinants") List<String> determinants,
                                    @JsonProperty("isPrimaryKey") Boolean isPrimaryKey) {
            this.name = name;
            this.determinants = determinants;
            this.isPrimaryKey = isPrimaryKey;
        }

        /*
         * Ovverride equals method to ensure we can check for object equality
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            AddUniqueConstraints other = (AddUniqueConstraints) obj;
            return Objects.equals(determinants, other.determinants);
        }

        /*
         * Ovverride hashCode method to ensure we can check for object equality
         */
        @Override
        public int hashCode() {
            return Objects.hash(determinants);
        }
    }

    private static class OtherFunctionalDependencies extends JsonOpenObject {
        @Nonnull
        public final List<AddFunctionalDependency> added;

        @JsonCreator
        public OtherFunctionalDependencies(@JsonProperty("added") List<AddFunctionalDependency> added) {
            this.added = added;
        }
    }

    @JsonPropertyOrder({
            "determinants",
            "dependents"
    })
    private static class AddFunctionalDependency extends JsonOpenObject {
        @Nonnull
        public final List<String> determinants;
        @Nonnull
        public final List<String> dependents;

        public AddFunctionalDependency(@JsonProperty("determinants") List<String> determinants,
                                       @JsonProperty("dependents") List<String> dependents) {
            this.determinants = determinants;
            this.dependents = dependents;
        }

        /*
         * Ovverride equals method to ensure we can check for object equality
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            AddFunctionalDependency other = (AddFunctionalDependency) obj;
            return Objects.equals(ImmutableMap.of(determinants, dependents),
                    ImmutableMap.of(other.determinants, other.dependents));
        }

        /*
        * Ovverride hashCode method to ensure we can check for object equality
        */
        @Override
        public int hashCode() {
            return Objects.hash(ImmutableMap.of(determinants, dependents));
        }
    }

    private static class ForeignKeys extends JsonOpenObject {
        @Nonnull
        public final List<AddForeignKey> added;

        @JsonCreator
        public ForeignKeys(@JsonProperty("added") List<AddForeignKey> added) {
            this.added = added;
        }
    }

    @JsonPropertyOrder({
            "determinants",
            "dependents"
    })
    private static class AddForeignKey extends JsonOpenObject {
        @Nonnull
        public final String name;
        @Nonnull
        public final String from;
        @Nonnull
        public final ForeignKeyPart to;

        public AddForeignKey(@JsonProperty("name") String name,
                             @JsonProperty("from") String from,
                             @JsonProperty("to") ForeignKeyPart to) {
            this.name = name;
            this.from = from;
            this.to = to;
        }
    }

    @JsonPropertyOrder({
            "relation",
            "columns"
    })
    public static class ForeignKeyPart extends JsonOpenObject {
        public final List<String> relation;
        public final List<String> columns;

        @JsonCreator
        public ForeignKeyPart(@JsonProperty("relation") List<String> relation,
                              @JsonProperty("columns") List<String> columns) {
            this.relation = relation;
            this.columns = columns;
        }
    }
}
