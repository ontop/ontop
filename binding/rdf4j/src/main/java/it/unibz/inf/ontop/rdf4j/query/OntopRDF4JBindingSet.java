package it.unibz.inf.ontop.rdf4j.query;

/*
 * #%L
 * ontop-quest-sesame
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

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.OntopBindingSet;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.rdf4j.RDF4JHelper;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.AbstractBindingSet;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.impl.SimpleBinding;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class OntopRDF4JBindingSet extends AbstractBindingSet implements BindingSet {

    private static final long serialVersionUID = -8455466574395305166L;

    private OntopBindingSet ontopBindingSet;

    public OntopRDF4JBindingSet(OntopBindingSet ontopBindingSet) {
        this.ontopBindingSet = ontopBindingSet;
    }

    @Override
    @Nullable
    public Binding getBinding(String bindingName) {
        if (!hasBinding(bindingName)) {
            return null;
        } else {
            final Value value = getValue(bindingName);
            return new SimpleBinding(bindingName, value);
        }
    }

    @Override
    public Set<String> getBindingNames() {
        return new LinkedHashSet<>(ontopBindingSet.getBindingNames());
    }

    @Override
    @Nullable
    public Value getValue(String bindingName) {
        if (!hasBinding(bindingName)) {
            return null;
        } else {
            try {
                final Constant constant = ontopBindingSet.getConstant(bindingName);
                return RDF4JHelper.getValue(constant);
            } catch (OntopResultConversionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean hasBinding(String bindingName) {
        return ontopBindingSet.hasBinding(bindingName);
    }

    @Override
    @Nonnull
    public Iterator<Binding> iterator() {
        List<Binding> allBindings = new LinkedList<>();
        List<String> names = ontopBindingSet.getBindingNames();
        for (String s : names) {
            if (ontopBindingSet.hasBinding(s)) {
                allBindings.add(getBinding(s));
            }
        }
        return allBindings.iterator();
    }

    @Override
    public int size() {
        return ontopBindingSet.getBindingNames().size();
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }
}
