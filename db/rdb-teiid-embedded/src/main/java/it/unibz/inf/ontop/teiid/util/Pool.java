package it.unibz.inf.ontop.teiid.util;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A pool of reusable objects identified by a key, which are 'leased' to client code and whose
 * allocation and deallocation are managed by the pool.
 *
 * @param <K>
 *            the type of keys identifying objects in the pool
 * @param <T>
 *            the type of objects in the pool
 */
public final class Pool<K, T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pool.class);

    private static final Set<Entry<?, ?>> SHUTDOWN_ENTRIES = Sets.newLinkedHashSet();

    private static final Thread SHUTDOWN_HOOK = new Thread(() -> shutdown(),
            "pool-shutdown-thread");

    private static volatile boolean shutdownInProgress = false;

    private final Function<K, T> allocator;

    private final BiConsumer<K, T> deallocator;

    private final Map<K, Entry<K, T>> entries;

    private final AtomicBoolean closed; // to be checked holding Pool.entries lock

    private Pool(final Function<K, T> allocator, final BiConsumer<K, T> deallocator) {
        this.allocator = allocator;
        this.deallocator = deallocator;
        this.entries = Maps.newHashMap();
        this.closed = new AtomicBoolean(false);
    }

    /**
     * Creates a {@code Pool} using the supplied allocator and a default deallocator that calls
     * {@link AutoCloseable#close()} of deallocated objects, if possible.
     *
     * @param <K>
     *            the type of key identifying objects in the pool
     * @param <T>
     *            the type of objects in the pool
     * @param allocator
     *            a function allocating the object for a supplied key, not null
     * @return the created pool
     * @see #create(Function, BiConsumer)
     */
    public static <K, T> Pool<K, T> create(final Function<K, T> allocator) {

        return create(allocator, (key, object) -> {
            if (object instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) object).close();
                } catch (final Throwable ex) {
                    LOGGER.warn("Ignoring error closing object", ex);
                }
            }
        });
    }

    /**
     * Creates a {@code Pool} using the supplied allocator and deallocator callbacks to manage
     * instantiated objects.
     *
     * @param <K>
     *            the type of key identifying objects in the pool
     * @param <T>
     *            the type of objects in the pool
     * @param allocator
     *            a function allocating the object for a supplied key, not null
     * @param deallocator
     *            a callback called with the key and the object to deallocate, not null
     * @return the created pool
     */
    public static <K, T> Pool<K, T> create(final Function<K, T> allocator,
            final BiConsumer<K, T> deallocator) {

        Objects.requireNonNull(allocator);
        Objects.requireNonNull(deallocator);
        return new Pool<>(allocator, deallocator);
    }

    /**
     * Leases the object in the pool for the key specified. The object is newly allocated if
     * necessary, otherwise a previously allocated object is returned. The method returns a
     * {@link Lease}. The leased object can be obtained via {@link Lease#get()}. The leased object
     * <b>must be released</b> after use by calling {@link Lease#close()}.
     *
     * @param key
     *            the key identifying the requested pooled object
     * @return a {@code Lease} providing access to the pooled object
     * @throws IllegalStateException
     *             if the pool has been closed, or if JVM shutdown is in progress, which prevents
     *             the allocation of new objects in the pool
     */
    public Lease<T> get(final K key) {
        Objects.requireNonNull(key);
        return new Lease<T>(allocate(key));
    }

    private Entry<K, T> allocate(final K key) {

        // We might need to iterate until a valid entry for the key is obtained, due to concurrent
        // object deallocation, pool being closed, JVM shutdown
        Entry<K, T> entry = null;
        while (true) {

            if (entry != null) {
                deallocate(entry, true); // Discard any illegal (at this point) entry
            }

            synchronized (SHUTDOWN_ENTRIES) {
                if (shutdownInProgress) {
                    throw new IllegalStateException("Cannot allocate object for key " + key
                            + " due to JVM shutdown in progress");
                }
            }

            synchronized (this.entries) {
                if (this.closed.get()) {
                    throw new IllegalStateException(
                            "Cannot allocate object for key " + key + " due to pool being closed");
                }
                entry = this.entries.computeIfAbsent(key, k -> new Entry<>(this, key));
                if (entry.refCount < 0) {
                    continue; // Cannot proceed: entry is being discarded
                }
                ++entry.refCount;
            }

            synchronized (entry) {
                if (entry.key == null) {
                    continue; // Cannot proceed: entry is being forcibly discarded
                }
                if (entry.object == null) {

                    LOGGER.debug("Pooled entry '{}' creation started", entry.key);
                    final long ts = System.currentTimeMillis();
                    entry.object = this.allocator.apply(key); // This may take a while
                    Objects.requireNonNull(entry.object); // Sanity check
                    LOGGER.debug("Pooled entry '{}' creation completed, {} ms", entry.key,
                            System.currentTimeMillis() - ts);

                    synchronized (SHUTDOWN_ENTRIES) {
                        if (shutdownInProgress) {
                            continue; // Cannot proceed, need to discard entity ourselves
                        }
                        if (SHUTDOWN_ENTRIES.isEmpty()) {
                            Runtime.getRuntime().addShutdownHook(SHUTDOWN_HOOK);
                        }
                        SHUTDOWN_ENTRIES.add(entry);
                    }
                }
            }

            return entry; // If we arrive here, the entry contains a valid allocated object
        }
    }

    private void deallocate(final Entry<K, T> entry, final boolean discard) {

        synchronized (this.entries) {
            if (entry.refCount == Integer.MIN_VALUE) {
                return; // Already being discarded somewhere else
            }
            if (!discard && entry.refCount > 1) {
                --entry.refCount;
                return; // No need to discard the entry
            }
            entry.refCount = Integer.MIN_VALUE; // Need to discard, use negative value as marker
        }

        K keyToDiscard;
        T objectToDiscard;

        synchronized (entry) {

            keyToDiscard = entry.key;
            objectToDiscard = entry.object;

            entry.key = null; // This signals allocate() that it should not call the allocator
            entry.object = null; // This prevents multiple deallocator calls (should not happen)

            if (keyToDiscard == null || objectToDiscard == null) {
                return; // Entry has been discarded by some other thread
            }

            LOGGER.debug("Pooled entry '{}' discard started", keyToDiscard);
            final long ts = System.currentTimeMillis();
            try {
                this.deallocator.accept(keyToDiscard, objectToDiscard); // This may take a while
            } catch (final Throwable ex) {
                LOGGER.warn("Ignoring deallocator exception for entry " + keyToDiscard, ex);
            }
            LOGGER.debug("Pooled entry '{}' discard completed, {} ms", keyToDiscard,
                    System.currentTimeMillis() - ts);

            synchronized (SHUTDOWN_ENTRIES) {
                if (!shutdownInProgress) { // No need changing data structures during shutdown
                    SHUTDOWN_ENTRIES.remove(entry);
                    if (SHUTDOWN_ENTRIES.isEmpty()) {
                        Runtime.getRuntime().removeShutdownHook(SHUTDOWN_HOOK);
                    }
                }
            }
        }

        synchronized (this.entries) {
            this.entries.remove(keyToDiscard, entry);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void shutdown() {

        List<Entry<?, ?>> entriesToDeallocate;
        synchronized (SHUTDOWN_ENTRIES) {
            entriesToDeallocate = ImmutableList.copyOf(SHUTDOWN_ENTRIES).reverse();
            SHUTDOWN_ENTRIES.clear();
            shutdownInProgress = true;
        }

        LOGGER.debug("Shutting down pools ({} objects to discard)", entriesToDeallocate.size());
        for (final Entry<?, ?> entry : entriesToDeallocate) {
            entry.pool.deallocate((Entry) entry, true);
        }
    }

    /**
     * Closes the {@code Pool}, deallocating all the objects currently in the pool and
     * invalidating previously returned {@link Lease} instances.
     */
    public void close() {

        synchronized (this.closed) {

            final List<Entry<K, T>> entriesToDeallocate;
            synchronized (this.entries) {
                if (!this.closed.compareAndSet(false, true)) {
                    return;
                }
                entriesToDeallocate = ImmutableList.copyOf(this.entries.values());
            }

            LOGGER.debug("Closing pool ({} entries to discard)", entriesToDeallocate.size());
            for (final Entry<K, T> entry : entriesToDeallocate) {
                deallocate(entry, true);
            }
        }
    }

    private static final class Entry<K, T> {

        final Pool<K, T> pool;

        K key; // null after deallocation

        T object; // null before allocation and after deallocation

        int refCount; // to be manipulated by holding lock on 'Pool.entries'

        Entry(final Pool<K, T> pool, final K key) {
            this.pool = pool;
            this.key = key;
            this.object = null;
            this.refCount = 0;
        }

    }

    /**
     * Closeable 'lease' providing access to / release of pooled object. A {@code Lease } object
     * <b>must be released</b> after use of the pooled object by calling {@link Lease#close()}.
     *
     * @param <T>
     *            the type of object from the pool
     */
    public static final class Lease<T> implements AutoCloseable {

        private Entry<?, T> entry;

        private Lease(final Entry<?, T> entry) {
            this.entry = entry;
        }

        /**
         * Returns the pooled object associated to this {@code Lease}.
         *
         * @return the pooled object, not null
         * @throws IllegalStateException
         *             if the {@code Lease} has been closed, possibly as a result of the pool
         *             being closed or due to JVM shutdown
         */
        public synchronized T get() {
            Preconditions.checkState(this.entry != null, "Lease has been closed");
            final T object = this.entry.object;
            Preconditions.checkState(object != null,
                    "Lease has been closed as result of pool being closed or JVM shutdown");
            return object;
        }

        /**
         * Tests whether the {@code Lease} is closed.
         *
         * @return true, if the lease is closed
         */
        public synchronized boolean isClosed() {
            return this.entry != null && this.entry.object != null;
        }

        /**
         * Closes this {@code Lease}, releasing the object back to the pool.
         */
        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public synchronized void close() {
            final Entry<?, T> entryToDeallocate = this.entry;
            this.entry = null; // must be cleared immediately to handle reentrant calls
            if (entryToDeallocate != null) {
                entryToDeallocate.pool.deallocate((Entry) entryToDeallocate, false);
            }
        }

    }

}
