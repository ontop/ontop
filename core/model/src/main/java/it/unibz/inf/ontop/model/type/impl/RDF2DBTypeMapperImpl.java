package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

public class RDF2DBTypeMapperImpl implements RDF2DBTypeMapper {

    private final ImmutableMap<IRI, DBTermType> map;

    @Inject
    private RDF2DBTypeMapperImpl(TypeFactory typeFactory) {
        this(createDefaultMap(typeFactory));
    }

    protected RDF2DBTypeMapperImpl(ImmutableMap<IRI, DBTermType> map) {
        this.map = map;
    }

    protected static ImmutableMap<IRI, DBTermType> createDefaultMap(TypeFactory typeFactory) {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();

        ImmutableMap.Builder<IRI, DBTermType> builder = ImmutableMap.builder();

        builder.put(XSD.HEXBINARY, dbTypeFactory.getDBHexBinaryType());
        builder.put(XSD.DECIMAL, dbTypeFactory.getDBDecimalType());
        builder.put(XSD.INTEGER, dbTypeFactory.getDBLargeIntegerType());
        builder.put(XSD.DOUBLE, dbTypeFactory.getDBDoubleType());
        builder.put(XSD.BOOLEAN, dbTypeFactory.getDBBooleanType());
        builder.put(XSD.DATE, dbTypeFactory.getDBDateType());
        builder.put(XSD.TIME, dbTypeFactory.getDBTimeType());
        builder.put(XSD.DATETIME, dbTypeFactory.getDBDateTimestampType());

        return builder.build();
    }

    @Override
    public Optional<DBTermType> getNaturalNonStringDBType(RDFTermType rdfTermType) {
        return Optional.of(rdfTermType)
                .filter(t -> t instanceof RDFDatatype)
                .map(t -> ((RDFDatatype) t).getIRI())
                .flatMap(i -> Optional.ofNullable(map.get(i)));
    }
}
