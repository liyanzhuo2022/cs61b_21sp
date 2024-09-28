package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove(){
        AListNoResizing<Integer> alst = new AListNoResizing<>();
        BuggyAList<Integer> blst = new BuggyAList<>();
        alst.addLast(4);
        blst.addLast(4);
        alst.addLast(5);
        blst.addLast(5);
        alst.addLast(6);
        blst.addLast(6);

        assertEquals(alst.removeLast(), blst.removeLast());
        assertEquals(alst.removeLast(), blst.removeLast());
        assertEquals(alst.removeLast(), blst.removeLast());
    }

    @Test
    public void randomizedTest(){
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> B = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                B.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int a = L.size();
                int b = B.size();
                assertEquals(a, b);
            } else if (operationNumber == 2 & L.size() > 0) {
                int a = L.getLast();
                int b = B.getLast();
                assertEquals(a, b);
            } else if (operationNumber == 3 & L.size() > 0) {
                int a = L.removeLast();
                int b = B.removeLast();
                assertEquals(a, b);
            }
        }
    }
}
