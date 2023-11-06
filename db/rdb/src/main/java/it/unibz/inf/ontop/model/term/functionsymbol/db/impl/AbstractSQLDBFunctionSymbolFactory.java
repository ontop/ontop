package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.type.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractSQLDBFunctionSymbolFactory extends AbstractDBFunctionSymbolFactory {

    protected static final String UPPER_STR = "UPPER";
    protected static final String UCASE_STR = "UCASE";
    protected static final String LOWER_STR = "LOWER";
    protected static final String LCASE_STR = "LCASE";
    protected static final String CONCAT_STR = "CONCAT";
    protected static final String REPLACE_STR = "REPLACE";
    protected static final String REGEXP_REPLACE_STR = "REGEXP_REPLACE";
    protected static final String REGEXP_LIKE_STR = "REGEXP_LIKE";
    protected static final String AND_STR = "AND";
    protected static final String OR_STR = "OR";
    protected static final String NOT_STR = "NOT";
    protected static final String SUBSTR_STR = "SUBSTR";
    protected static final String SUBSTRING_STR = "SUBSTRING";
    protected static final String CHAR_LENGTH_STR = "CHAR_LENGTH";
    protected static final String LENGTH_STR = "LENGTH";
    protected static final String RIGHT_STR = "RIGHT";
    protected static final String MULTIPLY_STR = "*";
    protected static final String DIVIDE_STR = "/";
    protected static final String ADD_STR = "+";
    protected static final String SUBTRACT_STR = "-";
    protected static final String ABS_STR = "ABS";
    protected static final String CEIL_STR = "CEIL";
    protected static final String ROUND_STR = "ROUND";
    protected static final String FLOOR_STR = "FLOOR";
    protected static final String RAND_STR = "RAND";
    protected static final String CURRENT_TIMESTAMP_STR = "CURRENT_TIMESTAMP";
    protected static final String COALESCE_STR = "COALESCE";
    protected static final String CONCAT_OP_STR = "||";
    protected static final String NULLIF_STR = "NULLIF";

    // Geographic Boolean Relation Functions
    protected static final String ST_WITHIN = "ST_WITHIN";
    protected static final String ST_CONTAINS = "ST_CONTAINS";
    protected static final String ST_CROSSES = "ST_CROSSES";
    protected static final String ST_DISJOINT = "ST_DISJOINT";
    protected static final String ST_EQUALS = "ST_EQUALS";
    protected static final String ST_OVERLAPS = "ST_OVERLAPS";
    protected static final String ST_INTERSECTS = "ST_INTERSECTS";
    protected static final String ST_TOUCHES = "ST_TOUCHES";
    protected static final String ST_COVERS = "ST_COVERS";
    protected static final String ST_COVEREDBY = "ST_COVEREDBY";
    protected static final String ST_CONTAINSPROPERLY = "ST_CONTAINSPROPERLY";

    // Geographic Boolean Relation Functions

    protected static final String ST_DISTANCE = "ST_DISTANCE";

    protected static final String ST_DISTANCE_SPHERE = "ST_DISTANCESPHERE";

    protected static final String ST_DISTANCE_SPHEROID = "ST_DISTANCESPHEROID";

    protected static final String ST_TRANSFORM = "ST_TRANSFORM";
    protected static final String ST_GEOMFROMTEXT = "ST_GEOMFROMTEXT";
    protected static final String ST_MAKEPOINT = "ST_MAKEPOINT";
    protected static final String ST_SETSRID = "ST_SETSRID";

    protected static final String ST_FLIP_COORDINATES = "ST_FLIPCOORDINATES";

    protected static final String ST_ASTEXT = "ST_ASTEXT";
    private static final String ST_BUFFER = "ST_BUFFER";
    private static final String ST_INTERSECTION = "ST_INTERSECTION";
    private static final String ST_CONVEXHULL = "ST_CONVEXHULL";
    private static final String ST_BOUNDARY = "ST_BOUNDARY";
    private static final String ST_ENVELOPE = "ST_ENVELOPE";
    private static final String ST_DIFFERENCE = "ST_DIFFERENCE";
    private static final String ST_SYMDIFFERENCE = "ST_SYMDIFFERENCE";
    private static final String ST_UNION = "ST_UNION";
    private static final String ST_RELATE = "ST_RELATE";
    private static final String ST_SRID = "ST_SRID";

    protected final DBTypeFactory dbTypeFactory;
    protected final TypeFactory typeFactory;
    protected final DBTermType dbStringType;
    protected final DBTermType dbBooleanType;
    protected final DBTermType dbDoubleType;
    protected final DBTermType dbIntegerType;
    protected final DBTermType dbDecimalType;

    protected final DBTermType abstractRootDBType;
    protected final TermType abstractRootType;
    private final Map<Integer, DBConcatFunctionSymbol> nullRejectingConcatMap;
    private final Map<Integer, DBConcatFunctionSymbol> concatOperatorMap;

    // Created in init()
    private DBFunctionSymbol ifThenElse;
    // Created in init()
    private DBBooleanFunctionSymbol isStringEmpty;
    // Created in init()
    private DBIsNullOrNotFunctionSymbol isNull;
    // Created in init()
    private DBIsNullOrNotFunctionSymbol isNotNull;
    // Created in init()
    private DBIsTrueFunctionSymbol isTrue;
    // XSD cast patterns
    protected static final String numericPattern = "'^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$'";
    protected static final String numericNonFPPattern = "'^[-+]?[0-9]*\\.?[0-9]*$'";
    protected static final String datePattern1 = "'^[0-9]{1,2}/[0-9]{1,2}/[0-9]{4}$'";
    protected static final String datePattern2 = "'^[0-9]{4}/[0-9]{1,2}/[0-9]{1,2}$'";
    protected static final String datePattern3 = "'^[0-9]{1,2}-[0-9]{1,2}-[0-9]{4}$'";
    protected static final String datePattern4 = "'^[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}$'";

    protected AbstractSQLDBFunctionSymbolFactory(ImmutableTable<String, Integer, DBFunctionSymbol> regularFunctionTable,
                                                 TypeFactory typeFactory) {
        super(regularFunctionTable, typeFactory);
        this.dbTypeFactory = typeFactory.getDBTypeFactory();
        this.typeFactory = typeFactory;
        this.dbStringType = dbTypeFactory.getDBStringType();
        this.dbBooleanType = dbTypeFactory.getDBBooleanType();
        this.dbDoubleType = dbTypeFactory.getDBDoubleType();
        this.dbIntegerType = dbTypeFactory.getDBLargeIntegerType();
        this.dbDecimalType = dbTypeFactory.getDBDecimalType();
        this.abstractRootDBType = dbTypeFactory.getAbstractRootDBType();
        this.abstractRootType = typeFactory.getAbstractAtomicTermType();
        this.nullRejectingConcatMap = Maps.newConcurrentMap();
        this.concatOperatorMap = Maps.newConcurrentMap();
    }

    @Override
    protected void init() {
        // Always call it first
        super.init();
        ifThenElse = createDBIfThenElse(dbBooleanType, abstractRootDBType);
        isStringEmpty = createIsStringEmpty(dbBooleanType, abstractRootDBType);
        isNull = createDBIsNull(dbBooleanType, abstractRootDBType);
        isNotNull = createDBIsNotNull(dbBooleanType, abstractRootDBType);
        isTrue = createDBIsTrue(dbBooleanType);
    }

    @Override
    protected DBFunctionSymbol createDBCount(boolean isUnary, boolean isDistinct) {
        DBTermType integerType = dbTypeFactory.getDBLargeIntegerType();
        return isUnary
                ? new DBCountFunctionSymbolImpl(abstractRootDBType, integerType, isDistinct)
                : new DBCountFunctionSymbolImpl(integerType, isDistinct);
    }

    @Override
    protected DBFunctionSymbol createDBSum(DBTermType termType, boolean isDistinct) {
        return new NullIgnoringDBSumFunctionSymbol(termType, isDistinct);
    }

    @Override
    protected DBFunctionSymbol createDBAvg(DBTermType inputType, boolean isDistinct) {
        DBTermType targetType = inputType.equals(dbIntegerType) ? dbDecimalType : inputType;
        return new NullIgnoringDBAvgFunctionSymbol(inputType, targetType, isDistinct);
    }

    @Override
    protected DBFunctionSymbol createDBStdev(DBTermType inputType, boolean isPop, boolean isDistinct) {
        DBTermType targetType = inputType.equals(dbIntegerType) ? dbDecimalType : inputType;
        return new NullIgnoringDBStdevFunctionSymbol(inputType, targetType, isPop, isDistinct);
    }

    @Override
    protected DBFunctionSymbol createDBVariance(DBTermType inputType, boolean isPop, boolean isDistinct) {
        DBTermType targetType = inputType.equals(dbIntegerType) ? dbDecimalType : inputType;
        return new NullIgnoringDBVarianceFunctionSymbol(inputType, targetType, isPop, isDistinct);
    }

    @Override
    protected DBFunctionSymbol createDBMin(DBTermType termType) {
        return new DBMinFunctionSymbolImpl(termType);
    }

    @Override
    protected DBFunctionSymbol createDBMax(DBTermType termType) {
        return new DBMaxFunctionSymbolImpl(termType);
    }

    @Override
    protected DBFunctionSymbol createDBSample(DBTermType termType) {
        return new DBSampleFunctionSymbolImpl(termType, "MIN");
    }

    protected static ImmutableTable<String, Integer, DBFunctionSymbol> createDefaultRegularFunctionTable(TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType dbStringType = dbTypeFactory.getDBStringType();
        DBTermType dbIntType = dbTypeFactory.getDBLargeIntegerType();
        DBTermType dbDateTimestamp = dbTypeFactory.getDBDateTimestampType();
        DBTermType abstractRootDBType = dbTypeFactory.getAbstractRootDBType();
        DBTermType dbBooleanType = dbTypeFactory.getDBBooleanType();
        DBTermType dbDoubleType = dbTypeFactory.getDBDoubleType();
        DBTermType dbIntegerType = dbTypeFactory.getDBLargeIntegerType();

        ImmutableTable.Builder<String, Integer, DBFunctionSymbol> builder = ImmutableTable.builder();

        // TODO: provide the base input types
        DBFunctionSymbol upperFunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(UPPER_STR, 1, dbStringType,
                false, abstractRootDBType);
        builder.put(UPPER_STR, 1, upperFunctionSymbol);
        builder.put(UCASE_STR, 1, upperFunctionSymbol);

        DBFunctionSymbol lowerFunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(LOWER_STR, 1, dbStringType,
                false, abstractRootDBType);
        builder.put(LOWER_STR, 1, lowerFunctionSymbol);
        builder.put(LCASE_STR, 1, lowerFunctionSymbol);


        DBFunctionSymbol replace3FunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(REPLACE_STR, 3, dbStringType,
                false, abstractRootDBType);
        builder.put(REPLACE_STR, 3, replace3FunctionSymbol);

        DBFunctionSymbol regexpReplace3FunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(REGEXP_REPLACE_STR, 3, dbStringType,
                false, abstractRootDBType);
        builder.put(REGEXP_REPLACE_STR, 3, regexpReplace3FunctionSymbol);

        DBFunctionSymbol regexpReplace4FunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(REGEXP_REPLACE_STR, 4, dbStringType,
                false, abstractRootDBType);
        builder.put(REGEXP_REPLACE_STR, 4, regexpReplace4FunctionSymbol);

        DBFunctionSymbol subString2FunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(SUBSTRING_STR, 2, dbStringType,
                false, abstractRootDBType);
        builder.put(SUBSTRING_STR, 2, subString2FunctionSymbol);
        DBFunctionSymbol subStr2FunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(SUBSTR_STR, 2, dbStringType,
                false, abstractRootDBType);
        builder.put(SUBSTR_STR, 2, subStr2FunctionSymbol);

        DBFunctionSymbol subString3FunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(SUBSTRING_STR, 3, dbStringType,
                false, abstractRootDBType);
        builder.put(SUBSTRING_STR, 3, subString3FunctionSymbol);
        DBFunctionSymbol subStr3FunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(SUBSTR_STR, 3, dbStringType,
                false, abstractRootDBType);
        builder.put(SUBSTR_STR, 3, subStr3FunctionSymbol);

        DBFunctionSymbol rightFunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(RIGHT_STR, 2, dbStringType,
                false, abstractRootDBType);
        builder.put(RIGHT_STR, 2, rightFunctionSymbol);

        // TODO: check precise output type
        DBFunctionSymbol strlenFunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(CHAR_LENGTH_STR, 1, dbIntType,
                false, abstractRootDBType);
        builder.put(CHAR_LENGTH_STR, 1, strlenFunctionSymbol);
        //TODO: move away this synonym as it is non-standard
        DBFunctionSymbol lengthFunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(LENGTH_STR, 1, dbIntType,
                false, abstractRootDBType);
        builder.put(LENGTH_STR, 1, lengthFunctionSymbol);

        DBFunctionSymbol nowFunctionSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(CURRENT_TIMESTAMP_STR, 0,
                dbDateTimestamp, true, abstractRootDBType);
        builder.put(CURRENT_TIMESTAMP_STR, 0, nowFunctionSymbol);

        // Common for many dialects
        DBBooleanFunctionSymbol regexpLike2 = new DefaultSQLSimpleDBBooleanFunctionSymbol(REGEXP_LIKE_STR, 2, dbBooleanType,
                abstractRootDBType);
        builder.put(REGEXP_LIKE_STR, 2, regexpLike2);

        DBBooleanFunctionSymbol regexpLike3 = new RegexpLike3FunctionSymbol(dbBooleanType, abstractRootDBType);
        builder.put(REGEXP_LIKE_STR, 3, regexpLike3);

        DBFunctionSymbol nullIfFunctionSymbol = new NullIfDBFunctionSymbolImpl(abstractRootDBType);
        builder.put(NULLIF_STR, 2, nullIfFunctionSymbol);

        // GEO Functions
        DBFunctionSymbol withinFunctionSymbol = new GeoDBBooleanFunctionSymbol(ST_WITHIN, 2, dbBooleanType,
                abstractRootDBType);
        builder.put(ST_WITHIN, 2, withinFunctionSymbol);

        DBFunctionSymbol containsFunctionSymbol = new GeoDBBooleanFunctionSymbol(ST_CONTAINS, 2, dbBooleanType,
                abstractRootDBType);
        builder.put(ST_CONTAINS, 2, containsFunctionSymbol);

        DBFunctionSymbol crossesFunctionSymbol = new GeoDBBooleanFunctionSymbol(ST_CROSSES, 2, dbBooleanType,
                abstractRootDBType);
        builder.put(ST_CROSSES, 2, crossesFunctionSymbol);

        DBFunctionSymbol disjointFunctionSymbol = new GeoDBBooleanFunctionSymbol(ST_DISJOINT, 2, dbBooleanType,
                abstractRootDBType);
        builder.put(ST_DISJOINT, 2, disjointFunctionSymbol);

        DBFunctionSymbol equalsFunctionSymbol = new GeoDBBooleanFunctionSymbol(ST_EQUALS, 2, dbBooleanType,
                abstractRootDBType);
        builder.put(ST_EQUALS, 2, equalsFunctionSymbol);

        DBFunctionSymbol intersectsFunctionSymbol = new GeoDBBooleanFunctionSymbol(ST_INTERSECTS, 2, dbBooleanType,
                abstractRootDBType);
        builder.put(ST_INTERSECTS, 2, intersectsFunctionSymbol);

        DBFunctionSymbol overlapsFunctionSymbol = new GeoDBBooleanFunctionSymbol(ST_OVERLAPS, 2, dbBooleanType,
                abstractRootDBType);
        builder.put(ST_OVERLAPS, 2, overlapsFunctionSymbol);

        DBFunctionSymbol touchesFunctionSymbol = new GeoDBBooleanFunctionSymbol(ST_TOUCHES, 2, dbBooleanType,
                abstractRootDBType);
        builder.put(ST_TOUCHES, 2, touchesFunctionSymbol);

        DBFunctionSymbol coversFunctionSymbol = new GeoDBBooleanFunctionSymbol(ST_COVERS, 2, dbBooleanType,
                abstractRootDBType);
        builder.put(ST_COVERS, 2, coversFunctionSymbol);

        DBFunctionSymbol coveredbyFunctionSymbol = new GeoDBBooleanFunctionSymbol(ST_COVEREDBY, 2, dbBooleanType,
                abstractRootDBType);
        builder.put(ST_COVEREDBY, 2, coveredbyFunctionSymbol);

        DBFunctionSymbol containsproperlyFunctionSymbol = new GeoDBBooleanFunctionSymbol(ST_CONTAINSPROPERLY, 2, dbBooleanType,
                abstractRootDBType);
        builder.put(ST_CONTAINSPROPERLY, 2, containsproperlyFunctionSymbol);

        DBFunctionSymbol distanceFunctionSymbol = new GeoDBTypedFunctionSymbol(ST_DISTANCE, 2, dbDoubleType, false,
                abstractRootDBType);
        builder.put(ST_DISTANCE, 2, distanceFunctionSymbol);

        DBFunctionSymbol distanceSphereFunctionSymbol = new GeoDBTypedFunctionSymbol(ST_DISTANCE_SPHERE, 2, dbDoubleType, false,
                abstractRootDBType);
        builder.put(ST_DISTANCE_SPHERE, 2, distanceSphereFunctionSymbol);

        DBFunctionSymbol distanceSpheroidFunctionSymbol = new GeoDBTypedFunctionSymbol(ST_DISTANCE_SPHEROID, 3, dbDoubleType, false,
                abstractRootDBType);
        builder.put(ST_DISTANCE_SPHEROID, 3, distanceSpheroidFunctionSymbol);

        DBFunctionSymbol asTextSymbol = new DefaultSQLSimpleTypedDBFunctionSymbol(ST_ASTEXT, 1, dbStringType, true,
                abstractRootDBType);
        builder.put(ST_ASTEXT, 1, asTextSymbol);

        DBFunctionSymbol bufferSymbol = new GeoDBTypedFunctionSymbol(ST_BUFFER, 2, dbStringType, false,
                abstractRootDBType);
        builder.put(ST_BUFFER, 2, bufferSymbol);

        DBFunctionSymbol intersectionSymbol = new GeoDBTypedFunctionSymbol(ST_INTERSECTION, 2, dbStringType, false,
                abstractRootDBType);
        builder.put(ST_INTERSECTION, 2, intersectionSymbol);

        DBFunctionSymbol boundarySymbol = new GeoDBTypedFunctionSymbol(ST_BOUNDARY, 1, dbStringType, false,
                abstractRootDBType);
        builder.put(ST_BOUNDARY, 1, boundarySymbol);

        DBFunctionSymbol convexhullSymbol = new GeoDBTypedFunctionSymbol(ST_CONVEXHULL, 1, dbStringType, false,
                abstractRootDBType);
        builder.put(ST_CONVEXHULL, 1, convexhullSymbol);

        DBFunctionSymbol differenceSymbol = new GeoDBTypedFunctionSymbol(ST_DIFFERENCE, 2, dbStringType, false,
                abstractRootDBType);
        builder.put(ST_DIFFERENCE, 2, differenceSymbol);

        DBFunctionSymbol symdifferenceSymbol = new GeoDBTypedFunctionSymbol(ST_SYMDIFFERENCE, 2, dbStringType, false,
                abstractRootDBType);
        builder.put(ST_SYMDIFFERENCE, 2, symdifferenceSymbol);

        DBFunctionSymbol envelopeSymbol = new GeoDBTypedFunctionSymbol(ST_ENVELOPE, 1, dbStringType, false,
                abstractRootDBType);
        builder.put(ST_ENVELOPE, 1, envelopeSymbol);

        DBFunctionSymbol unionSymbol = new GeoDBTypedFunctionSymbol(ST_UNION, 2, dbStringType, false,
                abstractRootDBType);
        builder.put(ST_UNION, 2, unionSymbol);

        DBFunctionSymbol relateSymbol = new GeoDBBooleanFunctionSymbol(ST_RELATE, 3, dbBooleanType,
                abstractRootDBType);
        builder.put(ST_RELATE, 3, relateSymbol);

        DBFunctionSymbol relatematrixSymbol = new GeoDBTypedFunctionSymbol(ST_RELATE, 2, dbStringType, false,
                abstractRootDBType);
        builder.put(ST_RELATE, 2, relatematrixSymbol);

        DBFunctionSymbol getsridSymbol = new GeoDBTypedFunctionSymbol(ST_SRID, 1, dbIntType, false,
                abstractRootDBType);
        builder.put(ST_SRID, 1, getsridSymbol);

        DBFunctionSymbol setsridSymbol = new GeoDBTypedFunctionSymbol(ST_SETSRID, 2, dbStringType, false,
                abstractRootDBType);
        builder.put(ST_SETSRID, 2, setsridSymbol);

        DBFunctionSymbol transformSymbol = new GeoDBTypedFunctionSymbol(ST_TRANSFORM, 2, dbStringType, false,
                abstractRootDBType);
        builder.put(ST_TRANSFORM, 2, transformSymbol);

        DBFunctionSymbol geomfromtextSymbol = new GeoDBTypedFunctionSymbol(ST_GEOMFROMTEXT, 1, dbStringType, false,
                abstractRootDBType);
        builder.put(ST_GEOMFROMTEXT, 1, geomfromtextSymbol);

        DBFunctionSymbol makepointSymbol = new GeoDBTypedFunctionSymbol(ST_MAKEPOINT, 2, dbStringType, false,
                abstractRootDBType);
        builder.put(ST_MAKEPOINT, 2, makepointSymbol);

        return builder.build();
    }

    @Override
    public DBConcatFunctionSymbol getNullRejectingDBConcat(int arity) {
        if (arity < 2)
            throw new IllegalArgumentException("Arity of CONCAT must be >= 2");
        return nullRejectingConcatMap
                .computeIfAbsent(arity, a -> createNullRejectingDBConcat(arity));
    }

    @Override
    public DBConcatFunctionSymbol getDBConcatOperator(int arity) {
        if (arity < 2)
            throw new IllegalArgumentException("Arity of CONCAT must be >= 2");
        return concatOperatorMap
                .computeIfAbsent(arity, a -> createDBConcatOperator(arity));
    }

    protected abstract DBConcatFunctionSymbol createNullRejectingDBConcat(int arity);

    protected abstract DBConcatFunctionSymbol createDBConcatOperator(int arity);

    @Override
    protected DBFunctionSymbol createRegularUntypedFunctionSymbol(String nameInDialect, int arity) {
        // TODO: avoid if-then-else
        if (isAnd(nameInDialect))
            return createDBAnd(arity);
        else if (isOr(nameInDialect))
            return createDBOr(arity);
        else if (isConcat(nameInDialect))
            return createRegularDBConcat(arity);
        // TODO: allow its recognition in the mapping. Challenging for detecting that NULLs are fitered out.
//        else if (isCoalesce(nameInDialect))
//            return getDBCoalesce(arity);
        return new DefaultUntypedDBFunctionSymbol(nameInDialect, arity, dbTypeFactory.getAbstractRootDBType());
    }

    @Override
    protected DBBooleanFunctionSymbol createRegularBooleanFunctionSymbol(String nameInDialect, int arity) {
        return new DefaultSQLSimpleDBBooleanFunctionSymbol(nameInDialect, arity, dbBooleanType, abstractRootDBType);
    }

    protected boolean isConcat(String nameInDialect) {
        return nameInDialect.equals(CONCAT_STR);
    }

    protected boolean isAnd(String nameInDialect) {
        return nameInDialect.equals(AND_STR);
    }

    protected boolean isOr(String nameInDialect) {
        return nameInDialect.equals(OR_STR);
    }

    protected boolean isCoalesce(String nameInDialect) {
        return nameInDialect.equals(COALESCE_STR);
    }

    /**
     * CONCAT regular function symbol, not an operator (like || or +)
     */
    protected abstract DBConcatFunctionSymbol createRegularDBConcat(int arity);

    protected DBBooleanFunctionSymbol createDBAnd(int arity) {
        return new DefaultDBAndFunctionSymbol(AND_STR, arity, dbBooleanType);
    }

    protected DBBooleanFunctionSymbol createDBOr(int arity) {
        return new DefaultDBOrFunctionSymbol(OR_STR, arity, dbBooleanType);
    }

    @Override
    protected DBNotFunctionSymbol createDBNotFunctionSymbol(DBTermType dbBooleanType) {
        return new DefaultDBNotFunctionSymbol(NOT_STR, dbBooleanType);
    }

    protected DBFunctionSymbol createDBIfThenElse(DBTermType dbBooleanType, DBTermType abstractRootDBType) {
        return new DefaultSQLIfThenElseFunctionSymbol(dbBooleanType, abstractRootDBType);
    }

    protected DBBooleanFunctionSymbol createIsStringEmpty(DBTermType dbBooleanType, DBTermType abstractRootDBType) {
        return new DefaultSQLIsStringEmptyFunctionSymbol(dbBooleanType, abstractRootDBType);
    }

    protected DBIsNullOrNotFunctionSymbol createDBIsNull(DBTermType dbBooleanType, DBTermType rootDBTermType) {
        return new DefaultSQLDBIsNullOrNotFunctionSymbol(true, dbBooleanType, rootDBTermType);
    }

    protected DBIsNullOrNotFunctionSymbol createDBIsNotNull(DBTermType dbBooleanType, DBTermType rootDBTermType) {
        return new DefaultSQLDBIsNullOrNotFunctionSymbol(false, dbBooleanType, rootDBTermType);
    }

    protected DBIsTrueFunctionSymbol createDBIsTrue(DBTermType dbBooleanType) {
        return new DefaultDBIsTrueFunctionSymbol(dbBooleanType);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType targetType) {
        return new DefaultSimpleDBCastFunctionSymbol(dbTypeFactory.getAbstractRootDBType(), targetType,
                Serializers.getCastSerializer(targetType));
    }

    @Override
    protected DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType inputType, DBTermType targetType) {
        if (targetType.equals(dbBooleanType))
            return new DefaultSimpleDBBooleanCastFunctionSymbol(inputType, targetType,
                    Serializers.getCastSerializer(targetType));

        DBTermType.Category inputCategory = inputType.getCategory();
        if (inputCategory.equals(targetType.getCategory())) {
            switch (inputCategory) {
                case INTEGER:
                    return createIntegerToIntegerCastFunctionSymbol(inputType, targetType);
                case DECIMAL:
                    return createDecimalToDecimalCastFunctionSymbol(inputType, targetType);
                case FLOAT_DOUBLE:
                    return createFloatDoubleToFloatDoubleCastFunctionSymbol(inputType, targetType);
                case STRING:
                    return createStringToStringCastFunctionSymbol(inputType, targetType);
                case DATETIME:
                    return createDatetimeToDatetimeCastFunctionSymbol(inputType, targetType);
                default:
                    return new DefaultSimpleDBCastFunctionSymbol(inputType, targetType,
                            Serializers.getCastSerializer(targetType));
            }
        }

        if (targetType.equals(dbStringType)) {
            switch (inputCategory) {
                case INTEGER:
                    return createIntegerToStringCastFunctionSymbol(inputType);
                case DECIMAL:
                    return createDecimalToStringCastFunctionSymbol(inputType);
                case FLOAT_DOUBLE:
                    return createFloatDoubleToStringCastFunctionSymbol(inputType);
                default:
                    return createDefaultCastToStringFunctionSymbol(inputType);
            }
        }


        return new DefaultSimpleDBCastFunctionSymbol(inputType, targetType,
                Serializers.getCastSerializer(targetType));
    }

    /**
     * Implicit
     */
    protected DBTypeConversionFunctionSymbol createIntegerToIntegerCastFunctionSymbol(DBTermType inputType, DBTermType targetType) {
        return new DefaultImplicitDBCastFunctionSymbol(inputType, targetType);
    }

    /**
     * TODO: make it implicit by default?
     */
    protected DBTypeConversionFunctionSymbol createDecimalToDecimalCastFunctionSymbol(DBTermType inputType, DBTermType targetType) {
        return new DefaultSimpleDBCastFunctionSymbol(inputType, targetType,
                Serializers.getCastSerializer(targetType));
    }

    /**
     * TODO: make it implicit by default?
     */
    protected DBTypeConversionFunctionSymbol createFloatDoubleToFloatDoubleCastFunctionSymbol(DBTermType inputType, DBTermType targetType) {
        return new DefaultSimpleDBCastFunctionSymbol(inputType, targetType,
                Serializers.getCastSerializer(targetType));
    }

    protected DBTypeConversionFunctionSymbol createStringToStringCastFunctionSymbol(DBTermType inputType,
                                                                                    DBTermType targetType) {
        return new DefaultImplicitDBCastFunctionSymbol(inputType, targetType);
    }

    /**
     * By default explicit
     */
    protected DBTypeConversionFunctionSymbol createDatetimeToDatetimeCastFunctionSymbol(DBTermType inputType,
                                                                                        DBTermType targetType) {
        return new DefaultSimpleDBCastFunctionSymbol(inputType, targetType, Serializers.getCastSerializer(targetType));
    }

    /**
     * The returned function symbol can apply additional optimizations
     */
    protected DBTypeConversionFunctionSymbol createIntegerToStringCastFunctionSymbol(DBTermType inputType) {
        return new DefaultCastIntegerToStringFunctionSymbol(inputType, dbStringType,
                Serializers.getCastSerializer(dbStringType));
    }

    /**
     * Hook
     */
    protected DBTypeConversionFunctionSymbol createDecimalToStringCastFunctionSymbol(DBTermType inputType) {
        return createDefaultCastToStringFunctionSymbol(inputType);
    }

    /**
     * Hook
     */
    protected DBTypeConversionFunctionSymbol createFloatDoubleToStringCastFunctionSymbol(DBTermType inputType) {
        return createDefaultCastToStringFunctionSymbol(inputType);
    }

    protected DBTypeConversionFunctionSymbol createDefaultCastToStringFunctionSymbol(DBTermType inputType) {
        return new DefaultSimpleDBCastFunctionSymbol(inputType, dbStringType,
                Serializers.getCastSerializer(dbStringType));
    }

    @Override
    protected DBFunctionSymbol createDBCase(int arity, boolean doOrderingMatter) {
        return new DefaultDBCaseFunctionSymbol(arity, dbBooleanType, abstractRootDBType, doOrderingMatter);
    }

    @Override
    protected DBBooleanFunctionSymbol createDBBooleanCase(int arity, boolean doOrderingMatter) {
        return new DBBooleanCaseFunctionSymbolImpl(arity, dbBooleanType, abstractRootDBType, doOrderingMatter);
    }

    @Override
    protected DBFunctionSymbol createCoalesceFunctionSymbol(int arity) {
        return new DefaultDBCoalesceFunctionSymbol(COALESCE_STR, arity, abstractRootDBType,
                Serializers.getRegularSerializer(COALESCE_STR));
    }

    @Override
    protected DBBooleanFunctionSymbol createBooleanCoalesceFunctionSymbol(int arity) {
        return new DefaultDBBooleanCoalesceFunctionSymbol(COALESCE_STR, arity, abstractRootDBType,
                dbBooleanType,
                (terms, termConverter, termFactory) -> {
                    String parameterString = terms.stream()
                            .map(termConverter)
                            .collect(Collectors.joining(","));
                    return String.format("COALESCE(%s) IS TRUE", parameterString);
                });
    }

    @Override
    protected DBStrictEqFunctionSymbol createDBStrictEquality(int arity) {
        return new DefaultDBStrictEqFunctionSymbol(arity, abstractRootType, dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createDBStrictNEquality(int arity) {
        return new DefaultDBStrictNEqFunctionSymbol(arity, abstractRootType, dbBooleanType);
    }

    @Override
    protected DBFunctionSymbol createEncodeURLorIRI(boolean preserveInternationalChars) {
        return new DefaultSQLEncodeURLorIRIFunctionSymbol(dbStringType, preserveInternationalChars);
    }

    @Override
    protected Optional<DBFunctionSymbol> createAbsFunctionSymbol(DBTermType dbTermType) {
        // TODO: check the term type
        return Optional.of(new DefaultSQLSimpleMultitypedDBFunctionSymbolImpl(ABS_STR, 1, dbTermType, false));
    }

    @Override
    protected Optional<DBFunctionSymbol> createCeilFunctionSymbol(DBTermType dbTermType) {
        // TODO: check the term type
        return Optional.of(new DefaultSQLSimpleMultitypedDBFunctionSymbolImpl(CEIL_STR, 1, dbTermType, false));
    }

    @Override
    protected Optional<DBFunctionSymbol> createFloorFunctionSymbol(DBTermType dbTermType) {
        // TODO: check the term type
        return Optional.of(new DefaultSQLSimpleMultitypedDBFunctionSymbolImpl(FLOOR_STR, 1, dbTermType, false));
    }

    @Override
    protected Optional<DBFunctionSymbol> createRoundFunctionSymbol(DBTermType dbTermType) {
        // TODO: check the term type
        return Optional.of(new DefaultSQLSimpleMultitypedDBFunctionSymbolImpl(ROUND_STR, 1, dbTermType, false));
    }

    @Override
    protected String serializeYearFromDatetime(ImmutableList<? extends ImmutableTerm> terms,
                                               Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeYear(terms, termConverter, termFactory);
    }

    @Override
    protected String serializeYearFromDate(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeYear(terms, termConverter, termFactory);
    }

    /**
     * By default, we assume that this function works both for TIMESTAMP and DATE
     */
    protected String serializeYear(ImmutableList<? extends ImmutableTerm> terms,
                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(YEAR FROM %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeMonthFromDatetime(ImmutableList<? extends ImmutableTerm> terms,
                                                Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeMonth(terms, termConverter, termFactory);
    }

    @Override
    protected String serializeMonthFromDate(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeMonth(terms, termConverter, termFactory);
    }

    /**
     * By default, we assume that this function works both for TIMESTAMP and DATE
     */
    protected String serializeMonth(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(MONTH FROM %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeDayFromDatetime(ImmutableList<? extends ImmutableTerm> terms,
                                              Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeDay(terms, termConverter, termFactory);
    }

    @Override
    protected String serializeDayFromDate(ImmutableList<? extends ImmutableTerm> terms,
                                          Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeDay(terms, termConverter, termFactory);
    }

    /**
     * By default, we assume that this function works both for TIMESTAMP and DATE
     */
    protected String serializeDay(ImmutableList<? extends ImmutableTerm> terms,
                                  Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(DAY FROM %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeHours(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(HOUR FROM %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeMinutes(ImmutableList<? extends ImmutableTerm> terms,
                                      Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(MINUTE FROM %s)", termConverter.apply(terms.get(0)));
    }

    /**
     * TODO: is it returning an integer or a decimal?
     */
    @Override
    protected String serializeSeconds(ImmutableList<? extends ImmutableTerm> terms,
                                      Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(SECOND FROM %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeWeek(ImmutableList<? extends ImmutableTerm> terms,
                                      Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(WEEK FROM %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeQuarter(ImmutableList<? extends ImmutableTerm> terms,
                                      Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(QUARTER FROM %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeDecade(ImmutableList<? extends ImmutableTerm> terms,
                                      Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(DECADE FROM %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeCentury(ImmutableList<? extends ImmutableTerm> terms,
                                      Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(CENTURY FROM %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeMillennium(ImmutableList<? extends ImmutableTerm> terms,
                                      Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(MILLENNIUM FROM %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeMilliseconds(ImmutableList<? extends ImmutableTerm> terms,
                                      Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(MILLISECONDS FROM %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeMicroseconds(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("EXTRACT(MICROSECONDS FROM %s)", termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeDateTrunc(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("DATE_TRUNC(%s, %s)", termConverter.apply(terms.get(1)), termConverter.apply(terms.get(0)));
    }

    @Override
    protected DBTypeConversionFunctionSymbol createDateTimeNormFunctionSymbol(DBTermType dbDateTimestampType) {
        // TODO: check if it is safe to allow the decomposition
        return new DecomposeStrictEqualitySQLTimestampISONormFunctionSymbol(
                dbDateTimestampType,
                dbStringType,
                this::serializeDateTimeNorm);
    }


    protected abstract String serializeDateTimeNorm(ImmutableList<? extends ImmutableTerm> terms,
                                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory);

    @Override
    protected DBTypeConversionFunctionSymbol createBooleanNormFunctionSymbol(DBTermType booleanType) {
        return new DefaultBooleanNormFunctionSymbol(booleanType, dbStringType);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createHexBinaryNormFunctionSymbol(DBTermType binaryType) {
        return new DefaultHexBinaryNormFunctionSymbol(binaryType, dbStringType, this::serializeHexBinaryNorm);
    }

    protected String serializeHexBinaryNorm(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return termConverter.apply(
                termFactory.getDBUpper(
                        termFactory.getDBCastFunctionalTerm(dbStringType, terms.get(0))));
    }

    protected String serializeHexBinaryDenorm(ImmutableList<? extends ImmutableTerm> terms,
                                              Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return termConverter.apply(
                        termFactory.getDBCastFunctionalTerm(dbTypeFactory.getDBHexBinaryType(), terms.get(0)));
    }

    @Override
    protected DBTypeConversionFunctionSymbol createDateTimeDenormFunctionSymbol(DBTermType timestampType) {
        return new DefaultSQLTimestampISODenormFunctionSymbol(timestampType, dbStringType);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createBooleanDenormFunctionSymbol() {
        return new DefaultBooleanDenormFunctionSymbol(dbBooleanType, dbStringType);
    }

    @Override
    protected DBTypeConversionFunctionSymbol createGeometryNormFunctionSymbol(DBTermType geoType) {
        return new DefaultSimpleDBCastFunctionSymbol(geoType, geoType, Serializers.getCastSerializer(geoType));
    }

    @Override
    protected DBTypeConversionFunctionSymbol createHexBinaryDenormFunctionSymbol(DBTermType binaryType) {
        return new DefaultHexBinaryDenormFunctionSymbol(binaryType, dbStringType, this::serializeHexBinaryDenorm);
    }

    @Override
    protected DBMathBinaryOperator createMultiplyOperator(DBTermType dbNumericType) {
        return new DefaultTypedDBMathBinaryOperator(MULTIPLY_STR, dbNumericType);
    }

    @Override
    protected DBMathBinaryOperator createDivideOperator(DBTermType dbNumericType) {
        return new DefaultTypedDBMathBinaryOperator(DIVIDE_STR, dbNumericType);
    }

    @Override
    protected DBMathBinaryOperator createAddOperator(DBTermType dbNumericType) {
        return new DefaultTypedDBMathBinaryOperator(ADD_STR, dbNumericType);
    }

    @Override
    protected DBMathBinaryOperator createSubtractOperator(DBTermType dbNumericType) {
        return new DefaultTypedDBMathBinaryOperator(SUBTRACT_STR, dbNumericType);
    }

    @Override
    protected DBMathBinaryOperator createUntypedMultiplyOperator() {
        return new DefaultUntypedDBMathBinaryOperator(MULTIPLY_STR, abstractRootDBType);
    }

    @Override
    protected DBMathBinaryOperator createUntypedDivideOperator() {
        return new DefaultUntypedDBMathBinaryOperator(DIVIDE_STR, abstractRootDBType);
    }

    @Override
    protected DBMathBinaryOperator createUntypedAddOperator() {
        return new DefaultUntypedDBMathBinaryOperator(ADD_STR, abstractRootDBType);
    }

    @Override
    protected DBMathBinaryOperator createUntypedSubtractOperator() {
        return new DefaultUntypedDBMathBinaryOperator(SUBTRACT_STR, abstractRootDBType);
    }

    @Override
    protected DBBooleanFunctionSymbol createNonStrictNumericEquality() {
        return new DefaultDBNonStrictNumericEqOperator(abstractRootDBType, dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createNonStrictStringEquality() {
        return new DefaultDBNonStrictStringEqOperator(abstractRootDBType, dbBooleanType);

    }

    @Override
    protected DBBooleanFunctionSymbol createNonStrictDatetimeEquality() {
        return new DefaultDBNonStrictDatetimeEqOperator(abstractRootDBType, dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createNonStrictDateEquality() {
        return new DefaultDBNonStrictDateEqOperator(abstractRootDBType, dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createNonStrictDefaultEquality() {
        return new DefaultDBNonStrictDefaultEqOperator(abstractRootDBType, dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createNumericInequality(InequalityLabel inequalityLabel) {
        return new DefaultDBNumericInequalityOperator(inequalityLabel, abstractRootDBType, dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createBooleanInequality(InequalityLabel inequalityLabel) {
        return new DefaultDBBooleanInequalityOperator(inequalityLabel, abstractRootDBType, dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createStringInequality(InequalityLabel inequalityLabel) {
        return new DefaultDBStringInequalityOperator(inequalityLabel, abstractRootDBType, dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createDatetimeInequality(InequalityLabel inequalityLabel) {
        return new DefaultDBDatetimeInequalityOperator(inequalityLabel, abstractRootDBType, dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createDateInequality(InequalityLabel inequalityLabel) {
        return new DefaultDBDateInequalityOperator(inequalityLabel, abstractRootDBType, dbBooleanType);
    }

    @Override
    protected DBBooleanFunctionSymbol createDefaultInequality(InequalityLabel inequalityLabel) {
        return new DefaultDBDefaultInequalityOperator(inequalityLabel, abstractRootDBType, dbBooleanType);
    }

    @Override
    public DBFunctionSymbol getDBIfThenElse() {
        return ifThenElse;
    }

    @Override
    public DBFunctionSymbol getDBNullIf() {
        return getRegularDBFunctionSymbol(NULLIF_STR, 2);
    }

    @Override
    public DBFunctionSymbol getDBUpper() {
        return getRegularDBFunctionSymbol(UPPER_STR, 1);
    }

    @Override
    public DBFunctionSymbol getDBLower() {
        return getRegularDBFunctionSymbol(LOWER_STR, 1);
    }

    @Override
    public DBFunctionSymbol getDBReplace() {
        return getRegularDBFunctionSymbol(REPLACE_STR, 3);
    }

    @Override
    public DBFunctionSymbol getDBRegexpReplace3() {
        return getRegularDBFunctionSymbol(REGEXP_REPLACE_STR, 3);
    }

    @Override
    public DBFunctionSymbol getDBRegexpReplace4() {
        return getRegularDBFunctionSymbol(REGEXP_REPLACE_STR, 4);
    }

    @Override
    public DBFunctionSymbol getDBSubString2() {
        return getRegularDBFunctionSymbol(SUBSTRING_STR, 2);
    }

    @Override
    public DBFunctionSymbol getDBSubString3() {
        return getRegularDBFunctionSymbol(SUBSTRING_STR, 3);
    }

    @Override
    public DBFunctionSymbol getDBRight() {
        return getRegularDBFunctionSymbol(RIGHT_STR, 2);
    }

    @Override
    public DBFunctionSymbol getDBCharLength() {
        return getRegularDBFunctionSymbol(CHAR_LENGTH_STR, 1);
    }

    @Override
    public DBAndFunctionSymbol getDBAnd(int arity) {
        if (arity < 2)
            throw new IllegalArgumentException("Arity of AND must be >= 2");
        return (DBAndFunctionSymbol) getRegularDBFunctionSymbol(AND_STR, arity);
    }

    @Override
    public DBOrFunctionSymbol getDBOr(int arity) {
        if (arity < 2)
            throw new IllegalArgumentException("Arity of OR must be >= 2");
        return (DBOrFunctionSymbol) getRegularDBFunctionSymbol(OR_STR, arity);
    }

    @Override
    public DBIsNullOrNotFunctionSymbol getDBIsNull() {
        return isNull;
    }

    @Override
    public DBIsNullOrNotFunctionSymbol getDBIsNotNull() {
        return isNotNull;
    }

    @Override
    public DBBooleanFunctionSymbol getDBIsStringEmpty() {
        return isStringEmpty;
    }

    @Override
    public DBIsTrueFunctionSymbol getIsTrue() {
        return isTrue;
    }

    @Override
    public NonDeterministicDBFunctionSymbol getDBRand(UUID uuid) {
        return new DefaultNonDeterministicNullaryFunctionSymbol(getRandNameInDialect(), uuid, dbDoubleType);
    }

    @Override
    public NonDeterministicDBFunctionSymbol getDBUUID(UUID uuid) {
        return new DefaultNonDeterministicNullaryFunctionSymbol(getUUIDNameInDialect(), uuid, dbStringType);
    }

    @Override
    public DBFunctionSymbol getDBNow() {
        return getRegularDBFunctionSymbol(CURRENT_TIMESTAMP_STR, 0);
    }

    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches2() {
        return (DBBooleanFunctionSymbol) getRegularDBFunctionSymbol(REGEXP_LIKE_STR, 2);
    }

    @Override
    public DBBooleanFunctionSymbol getDBRegexpMatches3() {
        return (DBBooleanFunctionSymbol) getRegularDBFunctionSymbol(REGEXP_LIKE_STR, 3);
    }

    // Topological functions
    @Override
    public DBBooleanFunctionSymbol getDBSTWithin() {
        return (DBBooleanFunctionSymbol) getRegularDBFunctionSymbol(ST_WITHIN, 2);
    }

    @Override
    public DBBooleanFunctionSymbol getDBSTContains() {
        return (DBBooleanFunctionSymbol) getRegularDBFunctionSymbol(ST_CONTAINS, 2);
    }

    @Override
    public DBBooleanFunctionSymbol getDBSTCrosses() {
        return (DBBooleanFunctionSymbol) getRegularDBFunctionSymbol(ST_CROSSES, 2);
    }

    @Override
    public DBBooleanFunctionSymbol getDBSTDisjoint() {
        return (DBBooleanFunctionSymbol) getRegularDBFunctionSymbol(ST_DISJOINT, 2);
    }

    @Override
    public DBBooleanFunctionSymbol getDBSTEquals() {
        return (DBBooleanFunctionSymbol) getRegularDBFunctionSymbol(ST_EQUALS, 2);
    }

    @Override
    public DBBooleanFunctionSymbol getDBSTIntersects() {
        return (DBBooleanFunctionSymbol) getRegularDBFunctionSymbol(ST_INTERSECTS, 2);
    }

    @Override
    public DBBooleanFunctionSymbol getDBSTOverlaps() {
        return (DBBooleanFunctionSymbol) getRegularDBFunctionSymbol(ST_OVERLAPS, 2);
    }

    @Override
    public DBBooleanFunctionSymbol getDBSTTouches() {
        return (DBBooleanFunctionSymbol) getRegularDBFunctionSymbol(ST_TOUCHES, 2);
    }

    @Override
    public DBBooleanFunctionSymbol getDBSTCoveredBy() {
        return (DBBooleanFunctionSymbol) getRegularDBFunctionSymbol(ST_COVEREDBY, 2);
    }

    @Override
    public DBBooleanFunctionSymbol getDBSTCovers() {
        return (DBBooleanFunctionSymbol) getRegularDBFunctionSymbol(ST_COVERS, 2);
    }

    @Override
    public DBBooleanFunctionSymbol getDBSTContainsProperly() {
        return (DBBooleanFunctionSymbol) getRegularDBFunctionSymbol(ST_CONTAINSPROPERLY, 2);
    }

    // Non-topological and common form functions
    @Override
    public DBFunctionSymbol getDBSTDistance() {
        return getRegularDBFunctionSymbol(ST_DISTANCE, 2);
    }

    @Override
    public DBFunctionSymbol getDBSTDistanceSphere() {
        return getRegularDBFunctionSymbol(ST_DISTANCE_SPHERE, 2);
    }

    @Override
    public DBFunctionSymbol getDBSTDistanceSpheroid() {
        return getRegularDBFunctionSymbol(ST_DISTANCE_SPHEROID, 3);
    }

    @Override
    public FunctionSymbol getDBAsText() {
        return getRegularDBFunctionSymbol(ST_ASTEXT, 1);
    }

    @Override
    public FunctionSymbol getDBSTFlipCoordinates() {
        return getRegularDBFunctionSymbol(ST_FLIP_COORDINATES, 1);
    }

    @Override
    public FunctionSymbol getDBBuffer() {
        return getRegularDBFunctionSymbol(ST_BUFFER, 2);
    }

    @Override
    public FunctionSymbol getDBIntersection() {
        return getRegularDBFunctionSymbol(ST_INTERSECTION, 2);
    }

    @Override
    public FunctionSymbol getDBBoundary() {
        return getRegularDBFunctionSymbol(ST_BOUNDARY, 1);
    }

    @Override
    public FunctionSymbol getDBConvexHull() {
        return getRegularDBFunctionSymbol(ST_CONVEXHULL, 1);
    }

    @Override
    public FunctionSymbol getDBDifference() {
        return getRegularDBFunctionSymbol(ST_DIFFERENCE, 2);
    }

    @Override
    public FunctionSymbol getDBEnvelope() {
        return getRegularDBFunctionSymbol(ST_ENVELOPE, 1);
    }

    @Override
    public FunctionSymbol getDBSymDifference() {
        return getRegularDBFunctionSymbol(ST_SYMDIFFERENCE, 2);
    }

    @Override
    public FunctionSymbol getDBUnion() {
        return getRegularDBFunctionSymbol(ST_UNION, 2);
    }

    @Override
    public DBBooleanFunctionSymbol getDBRelate() {
        return (DBBooleanFunctionSymbol) getRegularDBFunctionSymbol(ST_RELATE, 3);
    }

    @Override
    public FunctionSymbol getDBRelateMatrix() {
        return getRegularDBFunctionSymbol(ST_RELATE, 2);
    }

    @Override
    public DBFunctionSymbol getDBGetSRID() {
        return getRegularDBFunctionSymbol(ST_SRID, 1);
    }

    @Override
    public DBFunctionSymbol getDBSTTransform() {
        return getRegularDBFunctionSymbol(ST_TRANSFORM, 2);
    }

    @Override
    public DBFunctionSymbol getDBSTSetSRID() {
        return getRegularDBFunctionSymbol(ST_SETSRID, 2);
    }

    @Override
    public DBFunctionSymbol getDBSTGeomFromText() { return getRegularDBFunctionSymbol(ST_GEOMFROMTEXT, 1); }

    @Override
    public DBFunctionSymbol getDBSTMakePoint() { return getRegularDBFunctionSymbol(ST_MAKEPOINT, 2); }

    /**
     * Time extension - duration arithmetic
     */

    @Override
    protected String serializeWeeksBetweenFromDateTime(ImmutableList<? extends ImmutableTerm> terms,
                                                       Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeWeeksBetween(terms, termConverter, termFactory);
    }

    @Override
    protected String serializeWeeksBetweenFromDate(ImmutableList<? extends ImmutableTerm> terms,
                                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeWeeksBetween(terms, termConverter, termFactory);
    }

    protected String serializeWeeksBetween(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TIMESTAMPDIFF(WEEK, %s, %s)",
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeDaysBetweenFromDateTime(ImmutableList<? extends ImmutableTerm> terms,
                                                      Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeDaysBetween(terms, termConverter, termFactory);
    }

    @Override
    protected String serializeDaysBetweenFromDate(ImmutableList<? extends ImmutableTerm> terms,
                                                  Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeDaysBetween(terms, termConverter, termFactory);
    }

    protected String serializeDaysBetween(ImmutableList<? extends ImmutableTerm> terms,
                                          Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TIMESTAMPDIFF(DAY, %s, %s)",
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeHoursBetween(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TIMESTAMPDIFF(HOUR, %s, %s)",
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeMinutesBetween(ImmutableList<? extends ImmutableTerm> terms,
                                             Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TIMESTAMPDIFF(MINUTE, %s, %s)",
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeSecondsBetween(ImmutableList<? extends ImmutableTerm> terms,
                                             Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TIMESTAMPDIFF(SECOND, %s, %s)",
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }

    @Override
    protected String serializeMillisBetween(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format("TIMESTAMPDIFF(MILLISECOND, %s, %s)",
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0)));
    }

    /**
     * Can be overridden.
     * <p>
     * Not an official SQL function
     */
    protected String getRandNameInDialect() {
        return RAND_STR;
    }

    protected abstract String getUUIDNameInDialect();

    @Override
    protected String serializeDBRowNumber(Function<ImmutableTerm, String> converter, TermFactory termFactory) {
        return "ROW_NUMBER() OVER ()";
    }

    @Override
    protected String serializeDBRowNumberWithOrderBy(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> converter, TermFactory termFactory) {
        String conditionString = IntStream.range(0, terms.size())
                .boxed()
                .map(i -> converter.apply(terms.get(i)))
                .collect(Collectors.joining(","));
        return String.format("ROW_NUMBER() OVER (ORDER BY %s)",
                conditionString);
    }

    /**
     * XSD CAST functions
     */
    // https://www.w3.org/TR/xpath-functions/#casting-boolean
    @Override
    protected String serializeCheckAndConvertBoolean(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("(CASE WHEN CAST(%1$s AS DECIMAL) = 0 THEN 'false' " +
                        "WHEN %1$s = '' THEN 'false' " +
                        "WHEN %1$s = 'NaN' THEN 'false' " +
                        "ELSE 'true' " +
                        "END)",
                term);
    }

    // https://www.w3.org/TR/xmlschema-2/#boolean
    @Override
    protected String serializeCheckAndConvertBooleanFromString(ImmutableList<? extends ImmutableTerm> terms,
                                                               Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN %1$s='1' THEN 'true' " +
                        "WHEN UPPER(%1$s) LIKE 'TRUE' THEN 'true' " +
                        "WHEN %1$s='0' THEN 'false' " +
                        "WHEN UPPER(%1$s) LIKE 'FALSE' THEN 'false' " +
                        "ELSE NULL " +
                        "END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDouble(ImmutableList<? extends ImmutableTerm> terms,
                                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN %1$s !~ " + numericPattern +
                        " THEN NULL ELSE CAST(%1$s AS DOUBLE) END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertFloat(ImmutableList<? extends ImmutableTerm> terms,
                                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN %1$s !~ " + numericPattern + " THEN NULL " +
                        "WHEN (CAST(%1$s AS FLOAT) NOT BETWEEN -3.40E38 AND -1.18E-38 AND " +
                        "CAST(%1$s AS FLOAT) NOT BETWEEN 1.18E-38 AND 3.40E38 AND CAST(%1$s AS FLOAT) != 0) THEN NULL " +
                        "ELSE CAST(%1$s AS FLOAT) END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertFloatFromBoolean(ImmutableList<? extends ImmutableTerm> terms,
                                                              Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN UPPER(%1$s) LIKE 'TRUE' THEN 1.0E0 " +
                        "WHEN UPPER(%1$s) LIKE 'FALSE' THEN 0.0E0 " +
                        "ELSE NULL " +
                        "END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertFloatFromNonFPNumeric(ImmutableList<? extends ImmutableTerm> terms,
                                                                   Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN (CAST(%1$s AS FLOAT) NOT BETWEEN -3.40E38 AND -1.18E-38 AND " +
                        "CAST(%1$s AS FLOAT) NOT BETWEEN 1.18E-38 AND 3.40E38 AND CAST(%1$s AS FLOAT) != 0) THEN NULL " +
                        "ELSE CAST(%1$s AS FLOAT) END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertFloatFromDouble(ImmutableList<? extends ImmutableTerm> terms,
                                                             Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return termConverter.apply(
                termFactory.getDBCastFunctionalTerm(dbDoubleType, terms.get(0)));
    }

    @Override
    protected String serializeCheckAndConvertDecimal(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN %1$s !~ " + numericNonFPPattern + " THEN NULL " +
                        "ELSE CAST(%1$s AS DECIMAL) END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDecimalFromBoolean(ImmutableList<? extends ImmutableTerm> terms,
                                                                Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN %1$s='1' THEN 1.0 " +
                        "WHEN UPPER(%1$s) LIKE 'TRUE' THEN 1.0 " +
                        "WHEN %1$s='0' THEN 0.0 " +
                        "WHEN UPPER(%1$s) LIKE 'FALSE' THEN 0.0 " +
                        "ELSE NULL " +
                        "END",
                term);
    }

    @Override
    protected String serializeCheckAndConvertInteger(ImmutableList<? extends ImmutableTerm> terms,
                                                     Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return termConverter.apply(
                termFactory.getDBCastFunctionalTerm(dbIntegerType, terms.get(0)));
    }

    @Override
    protected String serializeCheckAndConvertIntegerFromBoolean(ImmutableList<? extends ImmutableTerm> terms,
                                                                Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN %1$s='1' THEN 1 " +
                        "WHEN %1$s THEN 1 " +
                        "WHEN %1$s='0' THEN 0 " +
                        "WHEN NOT %1$s THEN 0 " +
                        "ELSE NULL " +
                        "END",
                term);
    }

    // Per the standard, if only trailing 0-s and number can be represented as integer, drop them
    // https://www.w3.org/TR/xpath-functions/#casting-to-string
    @Override
    protected String serializeCheckAndConvertStringFromDecimal(ImmutableList<? extends ImmutableTerm> terms,
                                                               Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("(CASE WHEN MOD(%1$s,1) = 0 THEN CAST((%1$s) AS INTEGER) " +
                        "ELSE %1$s " +
                        "END)",
                term);
    }

    @Override
    protected String serializeCheckAndConvertDateTimeFromDate(ImmutableList<? extends ImmutableTerm> terms,
                                                              Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return termConverter.apply(
                termFactory.getDBCastFunctionalTerm(dbTypeFactory.getDBDateTimestampType(), terms.get(0)));
    }

    @Override
    protected String serializeCheckAndConvertDateTimeFromString(ImmutableList<? extends ImmutableTerm> terms,
                                                                Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializeCheckAndConvertDateTimeFromDate(terms, termConverter, termFactory);
    }

    @Override
    protected String serializeCheckAndConvertDateFromDateTime(ImmutableList<? extends ImmutableTerm> terms,
                                                              Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return termConverter.apply(
                termFactory.getDBCastFunctionalTerm(dbTypeFactory.getDBDateType(), terms.get(0)));
    }

    @Override
    protected String serializeCheckAndConvertDateFromString(ImmutableList<? extends ImmutableTerm> terms,
                                                            Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        String term = termConverter.apply(terms.get(0));
        return String.format("CASE WHEN (%1$s !~ " + datePattern1 + " AND " +
                        "%1$s !~ " + datePattern2 +" AND " +
                        "%1$s !~ " + datePattern3 +" AND " +
                        "%1$s !~ " + datePattern4 +" ) " +
                        " THEN NULL ELSE CAST(%1$s AS DATE) END",
                term);
    }
}
