package IntList;

import static org.junit.Assert.*;
import org.junit.Test;

public class SquarePrimesTest {

    /**
     * Here is a test for isPrime method. Try running it.
     * It passes, but the starter code implementation of isPrime
     * is broken. Write your own JUnit Test to try to uncover the bug!
     */
    @Test
    public void testSquarePrimesSimple() {
        IntList lst = IntList.of(14, 15, 16, 17, 18);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("14 -> 15 -> 16 -> 289 -> 18", lst.toString());
        assertTrue(changed);

        IntList lst_2 = IntList.of(14, 15, 16, 20, 18);
        boolean changed_2 = IntListExercises.squarePrimes(lst_2);
        assertEquals("14 -> 15 -> 16 -> 20 -> 18", lst_2.toString());
        assertFalse(changed_2);
    }

    @Test
    public void testSquarePrimesHead() {
        IntList lst_1 = IntList.of(2);
        boolean changed_1 = IntListExercises.squarePrimes(lst_1);
        assertEquals("4", lst_1.toString());
        assertTrue(changed_1);

        IntList lst_2 = IntList.of(4);
        boolean changed_2 = IntListExercises.squarePrimes(lst_2);
        assertEquals("4", lst_2.toString());
        assertFalse(changed_2);
    }

    @Test
    public void testSquarePrimesLast() {
        IntList lst = IntList.of(14, 15, 16, 17, 19); // starter code failed when there are multiple primes
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("14 -> 15 -> 16 -> 289 -> 361", lst.toString());
        assertTrue(changed);
    }

}
