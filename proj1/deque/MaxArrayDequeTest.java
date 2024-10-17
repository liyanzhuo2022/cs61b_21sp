package deque;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Comparator;

public class MaxArrayDequeTest {
    @Test
    public void simpleTest() {
        Comparator<Integer> c = new IntElementComparator();

        MaxArrayDeque<Integer> mad1 = new MaxArrayDeque<>(c);
        int N = 10;
        for (int i = 0; i < N; i++) {
            mad1.addLast(i);
        }
        assertEquals(N - 1, (long)mad1.max());
        assertEquals(N - 1, (long)mad1.max(c));
    }
}
