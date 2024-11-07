package bstmap;

import edu.princeton.cs.algs4.Queue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private Node root;

    private class Node {
        private K key;
        private V value;
        private Node left;
        private Node right;
        private int size; // number of nodes in the subtree

        public Node(K key, V value, int size) {
            this.key = key;
            this.value = value;
            this.size = size;
        }
    }


    /** Removes all of the mappings from this map. */
    public void clear() {
        root = null;
    }

    /* Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    public V get(K key) {
        return get(root, key);
    }


    /** Find the value in the key in the Node x subtree, return null if it doesn't exit*/
    private V get(Node x, K key) {
        if (key == null) {
            throw new IllegalArgumentException("calls get() with a null key");
        }
        if (x == null) {
            return null;
        }

        int cmp = key.compareTo(x.key);
        // key < x.value
        if (cmp < 0) {
            return get(x.left, key);
        } else if (cmp > 0) {
            return get(x.right, key);
        } else {
            return x.value;
        }
    }

    /* Returns true if this map contains a mapping for the specified key. */
    public boolean containsKey(K key) {
        if (key == null) {
            throw new IllegalArgumentException("calls on cotainsKey() on null");
        }
        return containsKey(root, key);
    }

    private boolean containsKey(Node x, K key) {
        if (x == null) {
            return false;
        }

        int cmp = key.compareTo(x.key);
        if (cmp < 0) {
            return containsKey(x.left, key);
        } else if (cmp > 0) {
            return containsKey(x.right, key);
        } else {
            return true;
        }
    }

    /* Returns a Set view of the keys contained in this map. Not required for Lab 7.
     * If you don't implement this, throw an UnsupportedOperationException. */
    public Set<K> keySet() {
        Queue<K> kQueue = (Queue<K>) keys();
        Set<K> kSet = new HashSet<>();
        for (K key : kQueue) {
            kSet.add(key);
        }
        return kSet;
    }

    private Iterable<K> keys() {
        Queue<K> kQueue = new Queue<K>();
        keys(root, kQueue);
        return kQueue;
    }

    private void keys(Node x, Queue<K> kQueue) {
        if (x == null) {
            return;
        }

        keys(x.left, kQueue);
        kQueue.enqueue(x.key);
        keys(x.right, kQueue);
    }

    /** prints out your BSTMap in order of increasing Key */
    public void printInOrder() {
        Iterable<K> kQueue = this.keys();
        for (K key : kQueue) {
            System.out.println(key);
        }
    }
    @Override
    public Iterator<K> iterator() {
        return new BSTMapIterator();
    }

    private class BSTMapIterator implements Iterator<K> {
        private Queue<K> kQueue = (Queue<K>) keys();

        public boolean hasNext() {
            return kQueue.isEmpty();
        }

        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more keys");
            }
            return kQueue.dequeue();
        }
    }


    /* Returns the number of key-value mappings in this map. */
    public int size() {
        return size(root);
    }

    private int size(Node x) {
        if (x == null) {
            return 0;
        } else {
            return x.size;
        }
    }


    /* Associates the specified value with the specified key in this map. */
    public void put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("calls put() with a null key");
        }
        // if (value == null) {remove(key);}
        root = put(root, key, value);

    }

    private Node put(Node x, K key, V value) {
        if (x == null) {
            return new Node(key, value, 1);
        }

        int cmp = key.compareTo(x.key);
        if (cmp < 0) {
            x.left = put(x.left, key, value);
        } else if (cmp > 0) {
            x.right = put(x.right, key, value);
        } else {
            x.value = value;
        }
        x.size = 1 + size(x.left) + size(x.right);
        return x;
    }


    /* Removes the mapping for the specified key from this map if present.
     * Not required for Lab 7. If you don't implement this, throw an
     * UnsupportedOperationException. */
    public V remove(K key) {
        if (key == null) {
            throw new NoSuchElementException("calls min() with empty symbol table");
        }
        V value = get(key);
        root = remove(root, key);
        return value;
    }

    private Node remove(Node x, K key) {
        if (x == null) {
            return null;
        }

        int cmp = key.compareTo(x.key);
        if (cmp < 0) {
            x.left = remove(x.left, key);
        } else if (cmp > 0) {
            x.right = remove(x.right, key);
        } else {
            if (x.right == null) {
                return x.left;
            }
            if (x.left == null) {
                return x.right;
            }
            // when the node has 2 children
            Node t = x;
            x = min(t.right);
            x.right = deleteMin(t.right);
            x.left = t.left;
        }
        x.size = size(x.right) + size(x.left) + 1;
        return x;
    }


    /* Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 7. If you don't implement this,
     * throw an UnsupportedOperationException.*/
    public V remove(K key, V value) {
        if (get(key) == value) {
            remove(key);
            return value;
        } else {
            return null;
        }
    }

    private K min() {
        if (keySet().isEmpty()) {
            throw new NoSuchElementException("calls min() with empty symbol table");
        }
        return min(root).key;
    }

    private Node min(Node x) {
        if (x.left == null) {
            return x;
        } else {
            return min(x.left);
        }
    }

    private Node deleteMin(Node x) {
        if (x.left == null) {
            return x.right;
        }
        x.left = deleteMin(x.left);
        x.size = size(x.left) + size(x.right) + 1;
        return x;
    }

}
