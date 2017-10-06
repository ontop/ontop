package it.unibz.inf.ontop.model.type;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public enum COL_TYPE {

    UNSUPPORTED(-1, "UNSUPPORTED"), // created only in SesameRDFIterator, ignored by SI and exceptions in all other cases
    NULL(0, "NULL"),
    OBJECT(1, "OBJECT"),
    BNODE(2, "BNODE"),
    LITERAL(3, "LITERAL"),
    LANG_STRING(-3, "LANG_STRING"), // not to be mapped from code // BC: Why not?
    INTEGER(4, "INTEGER"),
    DECIMAL(5, "DECIMAL"),
    DOUBLE(6, "DOUBLE"),
    STRING(7, "STRING"),
    DATETIME(8, "DATETIME"),
    BOOLEAN(9, "BOOLEAN"),
    DATE(10, "DATE"),
    TIME(11, "TIME"),
    YEAR(12, "YEAR"),
    LONG(13, "LONG"),
    FLOAT(14, "FLOAT"),
    NEGATIVE_INTEGER(15, "NEGATIVE_INTEGER"),
    NON_NEGATIVE_INTEGER(16, "NON_NEGATIVE_INTEGER"),
    POSITIVE_INTEGER(17, "POSITIVE_INTEGER"),
    NON_POSITIVE_INTEGER(18, "NON_POSITIVE_INTEGER"),
    INT(19, "INT"),
    UNSIGNED_INT(20, "UNSIGNED_INT"),
    DATETIME_STAMP(21, "DATETIME_STAMP");

    private static final ImmutableMap<Integer, COL_TYPE> CODE_TO_TYPE_MAP;

    static {
        ImmutableMap.Builder<Integer, COL_TYPE> mapBuilder = ImmutableMap.builder();
        for (COL_TYPE type : COL_TYPE.values()) {
            // ignore UNSUPPORTED (but not LITERAL_LANG anymore)
            if (type.code != -1)
                mapBuilder.put(type.code, type);
        }
        CODE_TO_TYPE_MAP = mapBuilder.build();
    }

    public static final ImmutableSet<COL_TYPE> INTEGER_TYPES = ImmutableSet.of(
            INTEGER, LONG, INT, NEGATIVE_INTEGER, NON_NEGATIVE_INTEGER, POSITIVE_INTEGER, NON_POSITIVE_INTEGER,
            UNSIGNED_INT);

    public static final ImmutableSet<COL_TYPE> NUMERIC_TYPES = ImmutableSet.of(
            DOUBLE, FLOAT, DECIMAL, INTEGER, LONG, INT, NEGATIVE_INTEGER, NON_NEGATIVE_INTEGER,
            POSITIVE_INTEGER, NON_POSITIVE_INTEGER, UNSIGNED_INT);

    public static final ImmutableSet<COL_TYPE> LITERAL_TYPES = ImmutableSet.<COL_TYPE>builder()
            .addAll(NUMERIC_TYPES)
            .add(LITERAL)
            .add(LANG_STRING)
            .add(STRING)
            .add(BOOLEAN)
            .add(DATETIME)
            .add(DATETIME_STAMP)
            .add(YEAR)
            .add(DATE)
            .add(TIME)
            .build();

    private final int code;
    private final String label;

    // private constructor
    private COL_TYPE(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getQuestCode() {
        return code;
    }

    @Override
    public String toString() {
        return label;
    }

    public static COL_TYPE getQuestType(int code) {
        return CODE_TO_TYPE_MAP.get(code);
    }
}
