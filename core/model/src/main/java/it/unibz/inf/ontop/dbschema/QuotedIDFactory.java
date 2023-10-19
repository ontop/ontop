package it.unibz.inf.ontop.dbschema;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.ImmutableMap;
import org.eclipse.jdt.annotation.NonNullByDefault;

import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Factory for creating attribute and relation identifier from strings.
 * <p>
 * Instances of this class define the rules of transforming unquoted and quoted identifiers, and different
 * {@code QuotedIDFactory} sub-classes can be defined to address different DBMS backends.
 * </p><p>
 * Each {@code QuotedIDFactory} implementation class should be associated to a <i>factory type identifier</i> obtainable
 * via {@link #getIDFactoryType()} and generally deriving from a {@link IDFactoryType} class annotation. This identifier
 * is a human-readable string that uniquely identifies the factory type (e.g., {@code POSTGRESQL}) in data written by
 * Ontop (e.g., the DB metadata saved to file) so to allow reading back that data using the proper
 * {@code QuotedIDFactory} implementation.
 * <p></p>
 * When implementing {@code QuotedIDFactory}, add a {@link IDFactoryType} annotation to specify the factory type
 * identifier, and then add a binding for the implementation class in the {@code OntopAbstractModule} sub-class of the
 * enclosing Ontop project (see the implementation of {@code OntopModelModule} for an example of how
 * {@code SQLStandardQuotedIDFactory} is bound).
 * </p>
 *
 * @author Roman Kontchakov (original implementation)
 */
@NonNullByDefault
public interface QuotedIDFactory {

    /**
     * Creates a new attribute ID from a string.
     *
     * @param attributeId possibly quoted attribute ID (SQL rendering)
     * @return attribute ID
     */
    QuotedID createAttributeID(String attributeId);

    /**
     * Creates a new relation ID from the supplied name.
     *
     * @param tableId the name
     * @return relation ID
     */
    RelationID createRelationID(String tableId);

    /**
     * Creates a new relation ID from the component strings.
     *
     * @param components list of the possibly quoted components of relation ID,
     *                   from the catalog to the table name
     * @return relation ID
     */
    RelationID createRelationID(@Nullable String... components);

    /**
     * Returns the quotation string used in the SQL rendering.
     *
     * @return the quotation string
     */
    String getIDQuotationString();

    /**
     * Returns whether notation {@code [identifier]} for schema/table/attribute identifiers is supported.
     *
     * @return true if square bracked notation is supported
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean supportsSquareBracketQuotation();

    /**
     * Returns an identifier (such as "POSTGRESQL") useful to denote the type of factory when writing/reading database
     * metadata. The default implementation retrieves the identifier from a {@link IDFactoryType} annotation placed on
     * the java class this object is instance of. Consider adding a {@code IDFactoryType} in derived classes or, for
     * wrappers, overriding and delegating this method to the wrapped {@code QuotedIDFactory}.
     *
     * @return a string identifier of this factory type, not null
     * @throws UnsupportedOperationException if a factory type ID is not available
     */
    default String getIDFactoryType() {
        return getIDFactoryType(getClass());
    }

    static String getIDFactoryType(Class<? extends QuotedIDFactory> factoryClass) {
        //noinspection ConstantConditions
        return Stream.<Class<?>>iterate(factoryClass, Objects::nonNull, Class::getSuperclass)
                .flatMap(c -> Stream.ofNullable(c.getAnnotation(IDFactoryType.class)).map(IDFactoryType::value))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("No @IDFactoryType annotation found"));
    }

    /**
     * Supplies a human-readable string identifier of a {@link QuotedIDFactory} implementation class.
     * <p>
     * This annotation is read by default by {@link QuotedIDFactory#getIDFactoryType()} and its value can be used to
     * indicate the type of {@code QuotedIDFactory} in read/written data.
     * </p>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface IDFactoryType {

        /**
         * The string identifying the ID factory type (such as {@code POSTGRESQL}).
         *
         * @return a string identifier of the ID factory type
         */
        String value();

    }

    /**
     * Supplier of {@link QuotedIDFactory} based on factory type identifier.
     * <p>
     * Instances of this interface can be used / injected wherever it is needed to obtain a {@code QuotedIDFactory}
     * instance matching a factory type (e.g., {@code POSTGRESQL}) only known at runtime, e.g., because deserialized
     * from read data. An instance of this interface covering all available {@code QuotedIDFactory} types in Ontop can
     * be injected using Ontop configuration / injection mechanism.
     * </p>
     */
    interface Supplier {

        /**
         * Returns a {@code QuotedIDFactory} with the ID factory type specified.
         *
         * @param idFactoryType factory type identifier
         * @return a corresponding factory instance
         */
        Optional<QuotedIDFactory> get(String idFactoryType);

        /**
         * Utility method producing a {@code Supplier} whose {@code get()} method returns one of the specified
         * {@code QuotedIDFactory}.
         *
         * @param factories the factories to return
         * @return the created supplier
         */
        static Supplier wrap(QuotedIDFactory... factories) {
            ImmutableMap<String, Optional<QuotedIDFactory>> map = Stream.of(factories)
                    .collect(ImmutableMap.toImmutableMap(QuotedIDFactory::getIDFactoryType, Optional::of));
            return idFactoryType -> map.getOrDefault(idFactoryType, Optional.empty());
        }

    }

}
