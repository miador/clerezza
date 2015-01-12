/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.clerezza.rdf.core.access;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.rdf.ImmutableGraph;
import org.apache.commons.rdf.Graph;
import org.apache.commons.rdf.BlankNodeOrIri;
import org.apache.commons.rdf.RdfTerm;
import org.apache.commons.rdf.Triple;
import org.apache.commons.rdf.Iri;
import org.apache.clerezza.rdf.core.access.debug.ReentrantReadWriteLockTracker;
import org.apache.commons.rdf.event.FilterTriple;
import org.apache.commons.rdf.event.GraphListener;

/**
 * Wrappes an Graph as a LockableMGraph, this class is used by TcManager to
 * support TcProviders that do not privide <code>LockableMGraph</code>.
 *
 * @author rbn
 */
public class LockableMGraphWrapper implements LockableMGraph {

    private static final String DEBUG_MODE = "rdfLocksDebugging";
    private final ReadWriteLock lock;

    private final Lock readLock;
    private final Lock writeLock;
    private final Graph wrapped;

    /**
     * Constructs a LocalbleMGraph for an Graph.
     *
     * @param providedMGraph a non-lockable graph
     */
    public LockableMGraphWrapper(final Graph providedMGraph) {
        this.wrapped = providedMGraph;
        {
            String debugMode = System.getProperty(DEBUG_MODE);
            if (debugMode != null && debugMode.toLowerCase().equals("true")) {
                lock = new ReentrantReadWriteLockTracker();
            } else {
                lock = new ReentrantReadWriteLock();
            }
        }
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }
    
    public LockableMGraphWrapper(final Graph providedMGraph, final ReadWriteLock lock) {
        this.wrapped = providedMGraph;
        this.lock = lock;
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    @Override
    public ReadWriteLock getLock() {
        return lock;
    }

    @Override
    public ImmutableGraph getImmutableGraph() {
        readLock.lock();
        try {
            return wrapped.getImmutableGraph();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Iterator<Triple> filter(BlankNodeOrIri subject, Iri predicate, RdfTerm object) {
        readLock.lock();
        try {
            return new LockingIterator(wrapped.filter(subject, predicate, object), lock);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int size() {
        readLock.lock();
        try {
            return wrapped.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        readLock.lock();
        try {
            return wrapped.isEmpty();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    @SuppressWarnings("element-type-mismatch")
    public boolean contains(Object o) {
        readLock.lock();
        try {
            return wrapped.contains(o);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Iterator<Triple> iterator() {
        readLock.lock();
        try {
            return new LockingIterator(wrapped.iterator(), lock);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Object[] toArray() {
        readLock.lock();
        try {
            return wrapped.toArray();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public <T> T[] toArray(T[] a) {
        readLock.lock();
        try {
            return wrapped.toArray(a);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        readLock.lock();
        try {
            return wrapped.containsAll(c);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean add(Triple e) {
        writeLock.lock();
        try {
            return wrapped.add(e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean remove(Object o) {
        writeLock.lock();
        try {
            return wrapped.remove(o);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean addAll(Collection<? extends Triple> c) {
        writeLock.lock();
        try {
            return wrapped.addAll(c);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        writeLock.lock();
        try {
            return wrapped.removeAll(c);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        writeLock.lock();
        try {
            return wrapped.retainAll(c);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            wrapped.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void addGraphListener(GraphListener listener, FilterTriple filter, long delay) {
        wrapped.addGraphListener(listener, filter, delay);
    }

    @Override
    public void addGraphListener(GraphListener listener, FilterTriple filter) {
        wrapped.addGraphListener(listener, filter);
    }

    @Override
    public void removeGraphListener(GraphListener listener) {
        wrapped.removeGraphListener(listener);
    }

    @Override
    public int hashCode() {
        return wrapped.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        LockableMGraphWrapper other = (LockableMGraphWrapper) obj;
        return wrapped.equals(other.wrapped);
    }

    @Override
    public String toString() {
        return wrapped.toString();
    }

}
