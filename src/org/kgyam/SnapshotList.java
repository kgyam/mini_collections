package org.kgyam;

import java.util.*;


/**
 * @author kgyam
 * 快照列表
 */
public class SnapshotList<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable {

    private transient Object[] arr;

    private int size = 0;

    private static final Integer DEFAULT_CAPACITY = 15;

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private transient Long[] addTimeStamp;

    private transient Long[] removeTimeStamp;

    public SnapshotList() {
        init(DEFAULT_CAPACITY);
    }

    public SnapshotList(int capacity) {
        int c = capacity;
        if (c == 0) {
            c = DEFAULT_CAPACITY;
        } else if (capacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " +
                    capacity);
        }
        init(c);
    }


    private void init(int capacity) {
        arr = new Object[capacity];
        addTimeStamp = new Long[capacity];
        removeTimeStamp = new Long[capacity];
    }


    @Override
    public E get(int index) {
        return (E) arr[index];
    }


    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public E remove(int index) {
        if (removeTimeStamp[index] == Long.MAX_VALUE) {
            removeTimeStamp[index] = System.currentTimeMillis();
            return (E) arr[index];
        }
        return null;
    }

    @Override
    public boolean add(E e) {
        add(size(), e);
        return Boolean.TRUE;
    }


    @Override
    public void add(int index, E element) {
        ensureCapacityInternal(index + 1);
        addTimeStamp[index] = System.currentTimeMillis();
        removeTimeStamp[index] = Long.MAX_VALUE;
        arr[index] = element;
        size++;
    }

    private void ensureCapacityInternal(int index) {
        if (index - arr.length > 0) {
            grow(index);
        }
    }


    private void grow(int index) {
        int oldCapacity = arr.length;

        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - oldCapacity < 0) {
            newCapacity = index;
        }
        if (newCapacity > MAX_ARRAY_SIZE) {
            if (index < 0) {
                throw new OutOfMemoryError();
            }
            newCapacity = index > MAX_ARRAY_SIZE ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
        }
        addTimeStamp = Arrays.copyOf(addTimeStamp, newCapacity);
        removeTimeStamp = Arrays.copyOf(removeTimeStamp, newCapacity);
        arr = Arrays.copyOf(arr, newCapacity);
    }

    @Override
    public Iterator<E> iterator() {
        return new SnapshotIterator();
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    private class SnapshotIterator implements Iterator {
        private Long createTimeStamp;
        private int cursor;

        SnapshotIterator() {
            createTimeStamp = System.currentTimeMillis();
            cursor = 0;
        }


        @Override
        public boolean hasNext() {
            if (cursor >= size) {
                return Boolean.FALSE;
            }
            for (int i = cursor; i < size; i++) {
                if (removeTimeStamp[i] > createTimeStamp && addTimeStamp[i] < createTimeStamp) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }

        /**
         *
         * @return
         */
        @Override
        public Object next() {
            for (int i = cursor; i < size; i++) {
                if (i >= size) {
                    throw new NoSuchElementException();
                }
                cursor++;
                if (createTimeStamp > addTimeStamp[i] && createTimeStamp < removeTimeStamp[i]) {
                    return arr[i];
                }
            }
            return null;
        }
    }
}
