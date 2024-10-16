package deque;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class ArrayDequeTest {
    @Test
    // print no resize array and see if add and remove works
    public void addRemoveSimpleTest() {
        ArrayDeque<Integer> ard1 = new ArrayDeque<>();
        ArrayDeque<Integer> ard2 = new ArrayDeque<>();
        int N = 8;
        for (int i = 0; i < N; i++) {
            ard1.addLast(i);
            ard2.addFirst(i);
        }
        assertEquals(N, ard1.size());
        assertEquals(N, ard2.size());

        for (int i = 0; i < N; i++) {
            assertEquals(i, (long)ard1.get(i));
        }

        for (int i = 0; i < N; i++) {
            assertEquals(i, (long)ard1.removeFirst());
            assertEquals(i, (long)ard2.removeLast());
        }
        assertTrue(ard1.isEmpty());
        assertTrue(ard2.isEmpty());

    }

    @Test
    /* Add more than 8 elements to deque; check if order is correct. */
    public void addRemoveBigTest() {
        ArrayDeque<Integer> ard1 = new ArrayDeque<>();
        ArrayDeque<Integer> ard2 = new ArrayDeque<>();
        int N = 100;
        for (int i = 0; i < N; i++) {
            ard1.addLast(i);
            ard2.addFirst(i);
        }
        assertEquals(N, ard1.size());
        assertEquals(N, ard2.size());

        for (int i = 0; i < N; i++) {
            assertEquals(i, (long)ard1.get(i));
        }

        for (int i = 0; i < N; i++) {
            assertEquals(i, (long)ard1.removeFirst());
            assertEquals(i, (long)ard2.removeLast());
        }
        assertTrue(ard1.isEmpty());
        assertTrue(ard2.isEmpty());
    }

    @Test
    public void IteratorTest() {
        int N = 100;
        ArrayDeque<Integer> ard1 = new ArrayDeque<>();
        for (int i = 0; i < N; i++) {
            ard1.addLast(i);
        }
        Iterator<Integer> it = ard1.iterator();
        int i = 0;
        while (it.hasNext()) {
            assertEquals((long)it.next(), i);
            i++;
        }
    }

}
