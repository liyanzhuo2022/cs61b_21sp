package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private int size;
    private T[] items;
    private int nextFirst;
    private int nextLast;
    private static final int LONGARRAY = 16;

    public ArrayDeque() {
        this.size = 0;
        this.items = (T[]) new Object[8];
        this.nextFirst = 4;
        this.nextLast = 5;
    }

    /**This helper method would return the previous index of the current index.
     * It mainly for preventing out of index errors.
     * @param i : the current index
     * @return int: the prev index of the current index
     * */
    private int prevIndex(int i) {
        int prev = i - 1;
        if (prev >= 0) {
            return prev;
        } else {
            return items.length - 1;
        }
    }

    // Works very similar to private int prevIndex(int i), but returns the next index.
    private int nextIndex(int i) {
        int next = i + 1;
        if (next <= items.length - 1) {
            return next;
        } else {
            return 0;
        }
    }

    /**This helper method resizes the array T[] items,
     * copies the items in items[] into the new array and returns it,
     * and updates nextFirst, nextLast.
     * @param capacity: the length of the new array
     * @return T[]: it returns the new array
     * */
    private T[] resize(int capacity) {
        T[] newArr = (T[]) new Object[capacity];

        int newIndex = capacity / 4;
        int itemsIndex = nextIndex(nextFirst);
        nextFirst = newIndex - 1;
        int counter = size;
        while (counter > 0) {
            newArr[newIndex] = items[itemsIndex];
            newIndex++;
            itemsIndex = nextIndex(itemsIndex);
            counter--;
        }
        nextLast = newIndex;
        return newArr;
    }

    public void addFirst(T item) {
        // check if the array is full
        if (items[nextFirst] != null) {
            items = resize(size * 2);
        }

        items[nextFirst] = item;
        nextFirst = prevIndex(nextFirst);
        size++;
    }
    public void addLast(T item) {
        // check if the array is full
        if (items[nextLast] != null) {
            items = resize(size * 2);
        }

        items[nextLast] = item;
        nextLast = nextIndex(nextLast);
        size++;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        int counter = size;
        int index = nextIndex(nextFirst);
        while (counter > 0) {
            System.out.print(items[index] + " ");
            index = nextIndex(index);
            counter--;
        }
        System.out.println();
    }

    public T removeFirst() {
        // check whether the array is empty
        if (this.isEmpty()) {
            return null;
        }

        // check would remove operation make the array too big for current size
        if (items.length >= LONGARRAY && (size - 1) / items.length < 0.25) {
            items = resize(size * 2);
        }

        int firstIndex = nextIndex(nextFirst);
        T returnItem = items[firstIndex];
        items[firstIndex] = null;
        nextFirst = firstIndex;
        size--;
        return returnItem;
    }

    public T removeLast() {
        if (this.isEmpty()) {
            return null;
        }

        if (items.length >= LONGARRAY && (size - 1) / items.length < 0.25) {
            items = resize(size * 2);
        }

        int lastIndex = prevIndex(nextLast);
        T returnItem = items[lastIndex];
        items[lastIndex] = null;
        nextLast = lastIndex;
        size--;
        return returnItem;
    }

    public T get(int index) {
        int counter = index;
        int p = nextIndex(nextFirst);
        while (counter > 0) {
            p = nextIndex(p);
            counter--;
        }
        return items[p];
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int index;

        ArrayDequeIterator() {
            index = nextIndex(nextFirst);
        }

        public boolean hasNext() {
            return index != nextLast;
        }

        public T next() {
            T returnItem = items[index];
            index = nextIndex(index);
            return returnItem;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof Deque) {
            Deque<T> other = (Deque<T>) o;
            if (other.size() != this.size()) {
                return false;
            }
            for (int i = 0; i < this.size(); i++) {
                if (!(this.get(i).equals(other.get(i)))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
