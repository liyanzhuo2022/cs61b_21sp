package hashmap;

import org.w3c.dom.Node;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private int n; // number of items(key-value pairs)
    private int m; // size of the hash table
    private static final int initialSize = 16;
    private double maxLoadFactor = 0.75;

    /** Constructors */
    public MyHashMap() {
        this(initialSize);
    }

    public MyHashMap(int initialSize) {
        this.m = initialSize;
        buckets = createTable(m);
        for (int i = 0; i < m; i++) {
            buckets[i] = createBucket();
        }
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this.maxLoadFactor = maxLoad;
        this.m = initialSize;
        buckets = createTable(m);
        for (int i = 0; i < m; i++) {
            buckets[i] = createBucket();
        }
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!
    public void clear() {
        this.n = 0;
        this.m = initialSize;
        buckets = createTable(m);
    }

    // @see https://chatgpt.com/c/6732493a-a94c-8003-8a2d-cff947d63089
    private int hash(K key) {
        int h = key.hashCode();
        h ^= (h >>> 20) ^ (h >>> 12) ^ (h >>> 7) ^ (h >>> 4);
        return h & (m-1);
    }

    /** Returns true if this map contains a mapping for the specified key. */
    public boolean containsKey(K key) {
        int index = hash(key);
        Collection<Node> bucket = buckets[index];
        if (bucket == null) return false;
        Iterator<Node> bucketIterator = bucket.iterator();
        while (bucketIterator.hasNext()) {
            if (bucketIterator.next().key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    public V get(K key) {
        int index = hash(key);
        Collection<Node> bucket = buckets[index];
        if (bucket == null) return null;
        Iterator<Node> bucketIterator = bucket.iterator();
        while (bucketIterator.hasNext()) {
            Node n = bucketIterator.next();
            if (n.key.equals(key)) {
                return n.value;
            }
        }
        return null;
    }

    /** Returns the number of key-value mappings in this map. */
    public int size() {
        return n;
    }

    private void resize(int capacity) {
        MyHashMap<K,V> temp = new MyHashMap<>(capacity);

        for (int i = 0; i < m; i++) {
            for (Node node : buckets[i]) {
                temp.put(node.key, node.value);
            }
        }
        this.m = temp.m;
        this.n = temp.n;
        this.buckets = temp.buckets;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key,
     * the old value is replaced.
     */
    public void put(K key, V value) {
        if (key == null) throw new IllegalArgumentException("first argument to put() is null");

        double curLoadFactor = (double) n / m;
        if (curLoadFactor >= this.maxLoadFactor) {
            resize(this.m * 2);
        }

        int index = hash(key);
        Collection<Node> bucket = buckets[index];
        Iterator<Node> bucketIterator = bucket.iterator();
        while (bucketIterator.hasNext()) {
            Node n = bucketIterator.next();
            if (n.key.equals(key)) {
                n.value = value;
                return;
            }
        }
        Node n = createNode(key, value);
        bucket.add(n);
        this.n++;
    }

    /** Returns a Set view of the keys contained in this map. */
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        for (int i = 0; i < m; i++) {
            for (Node n : buckets[i]) {
                keys.add(n.key);
            }
        }
        return keys;
    }

    public V remove(K key) {
        int index = hash(key);
        Collection<Node> bucket = buckets[index];
        Iterator<Node> bucketIterator = bucket.iterator();
        while (bucketIterator.hasNext()) {
            Node node = bucketIterator.next();
            if (node.key.equals(key)) {
                V value = node.value;
                bucket.remove(node);
                return value;
            }
        }
        return null;
    }

    /**
     * Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 8. If you don't implement this,
     * throw an UnsupportedOperationException.
     */
    public V remove(K key, V value) {
        int index = hash(key);
        Collection<Node> bucket = buckets[index];
        Iterator<Node> bucketIterator = bucket.iterator();
        while (bucketIterator.hasNext()) {
            Node node = bucketIterator.next();
            if (node.key.equals(key) & node.value.equals(value)) {
                bucket.remove(node);
                return value;
            }
        }
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return new HashMapIterator();
    }

    private class HashMapIterator implements Iterator<K> {
        Queue<K> kQueue = new LinkedList<>(keySet());

        public boolean hasNext() {
            return kQueue.isEmpty();
        }

        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more keys");
            }
            return kQueue.remove();
        }
    }
}
