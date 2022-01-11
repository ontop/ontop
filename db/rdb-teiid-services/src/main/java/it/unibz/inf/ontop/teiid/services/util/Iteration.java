package it.unibz.inf.ontop.teiid.services.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Iteration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Iteration.class);

    public static <T> Iterator<T> sort(final Iterator<T> iterator,
            final Comparator<? super T> comparator) {

        class SortIterator implements Iterator<T>, AutoCloseable {

            private Iterator<T> sortedIterator = null;

            @Override
            public boolean hasNext() {
                if (this.sortedIterator == null) {
                    final List<T> sortedElements = Lists.newArrayList(iterator);
                    Collections.sort(sortedElements, comparator);
                    this.sortedIterator = sortedElements.iterator();
                }
                return this.sortedIterator.hasNext();
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return this.sortedIterator.next();
            }

            @Override
            public void close() throws Exception {
                closeQuietly(iterator);
            }

        }

        return new SortIterator();
    }

    public static <T> Iterator<T> filter(final Iterator<T> iterator,
            final Predicate<? super T> predicate) {

        class FilterIterator implements Iterator<T>, AutoCloseable {

            @Nullable
            Optional<T> next = null;

            @Override
            public boolean hasNext() {

                if (this.next != null) {
                    return true;
                }

                while (iterator.hasNext()) {
                    final T elem = iterator.next();
                    if (predicate.test(elem)) {
                        this.next = Optional.ofNullable(elem);
                        return true;
                    }
                }

                return false;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                final T elem = this.next.orElse(null);
                this.next = null;
                return elem;
            }

            @Override
            public void close() throws Exception {
                closeQuietly(iterator);
            }

        }

        return new FilterIterator();
    }

    public static <I, O> Iterator<O> transform(final Iterator<I> iterator,
            final Function<? super I, ? extends O> transformer) {

        class TransformIterator implements Iterator<O>, AutoCloseable {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public O next() {
                return transformer.apply(iterator.next());
            }

            @Override
            public void close() throws Exception {
                closeQuietly(iterator);
            }

        }

        return new TransformIterator();
    }

    public static <T> Iterator<T> slice(final Iterator<T> iterator, final int offset,
            final int limit) {

        if (offset <= 0 && limit < 0) {
            return iterator;
        }

        class SliceIterator implements Iterator<T>, AutoCloseable {

            private int index = 0;

            @Override
            public boolean hasNext() {
                while (this.index < offset) {
                    if (!iterator.hasNext()) {
                        return false;
                    }
                    iterator.next();
                    ++this.index;
                }
                if (limit >= 0 && this.index >= offset + limit) {
                    return false;
                }
                return iterator.hasNext();
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                final T element = iterator.next();
                ++this.index;
                return element;
            }

            @Override
            public void close() throws Exception {
                closeQuietly(iterator);
            }

        }

        return new SliceIterator();
    }

    public static <T> Iterator<T> concat(
            final Iterator<? extends Iterator<? extends T>> iterators) {

        class ConcatIterator implements Iterator<T>, AutoCloseable {

            private Iterator<? extends T> iterator = Collections.emptyIterator();

            @Override
            public boolean hasNext() {
                while (!this.iterator.hasNext()) {
                    if (!iterators.hasNext()) {
                        return false;
                    }
                    closeQuietly(this.iterator);
                    this.iterator = iterators.next();
                }
                return true;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return this.iterator.next();
            }

            @Override
            public void close() {
                closeQuietly(this.iterator);
                closeQuietly(iterators);
            }

        }

        return new ConcatIterator();
    }

    public static void close(@Nullable final Iterator<?> iterator) throws Exception {
        if (iterator instanceof AutoCloseable) {
            ((AutoCloseable) iterator).close();
        }
    }

    public static void closeQuietly(@Nullable final Iterator<?> iterator) {
        if (iterator instanceof AutoCloseable) {
            try {
                ((AutoCloseable) iterator).close();
            } catch (final Throwable ex) {
                LOGGER.warn("Ignoring error closing " + iterator.getClass().getSimpleName(), ex);
            }
        }
    }

    private Iteration() {
        throw new Error();
    }

}
