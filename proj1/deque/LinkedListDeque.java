package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {

    public class Node {
        Node prev;
        T item;
        Node next;

        Node(Node p, T i, Node n) {
            this.prev = p;
            this.item = i;
            this.next = n;
        }

    }

    private Node sentinel;
    private int size;

    public LinkedListDeque() {
        sentinel = new Node(null, null, null);
        sentinel.prev = sentinel;
        sentinel.next = sentinel;

        size = 0;
    }

    public void addFirst(T x) {
        Node first = new Node(sentinel, x, sentinel.next);
        sentinel.next.prev = first;
        sentinel.next = first;
        size += 1;
    }

    public void addLast(T x) {
        Node last = new Node(sentinel.prev, x, sentinel);
        sentinel.prev.next = last;
        sentinel.prev = last;
        size += 1;
    }

    public boolean isEmpty() {
        return sentinel.next == sentinel;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        Node p = sentinel.next;
        while (p != sentinel) {
            System.out.print(p.item + " ");
            p = p.next;
        }
        System.out.println();
    }

    public T removeFirst() {
        Node p = sentinel.next;
        if (p == sentinel) {
            return null;
        }

        sentinel.next = p.next;
        p.next.prev = sentinel;
        // not necessary but gpt said it might be a good habit
        p.prev = null;
        p.next = null;
        size -= 1;
        return p.item;
    }

    public T removeLast() {
        Node p = sentinel.prev;
        if (p == sentinel) {
            return null;
        }

        sentinel.prev = p.prev;
        p.prev.next = sentinel;
        p.prev = null;
        p.next = null;
        size -= 1;
        return p.item;
    }

    public T get(int index) {
        Node p = sentinel.next;
        for (int i = 0; i < index; i++) {
            if (p == sentinel) {
                return null;
            }
            p = p.next;
        }
        return p.item;
    }

    public T getRecursive(int index) {
        Node p = sentinel.next;
        return getRecHelper(p, index);
    }

    private T getRecHelper(Node p, int index) {
        if (p == sentinel) {
            return null;
        }

        if (index == 0) {
            return p.item;
        }

        return getRecHelper(p.next, index - 1);
    }

    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private Node p;

        public LinkedListDequeIterator() {
            p = sentinel.next;
        }

        public boolean hasNext() {
            return (p != sentinel);
        }

        public T next() {
            T returnItem = p.item;
            p = p.next;
            return returnItem;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Deque) {
            Deque<T> other = (Deque<T>) o; // java 15 must do the casting
            if (this.size() == other.size()) {
                return false;
            }
            for (int i = 0; i < this.size(); i++) {
                if (other.get(i) != this.get(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
