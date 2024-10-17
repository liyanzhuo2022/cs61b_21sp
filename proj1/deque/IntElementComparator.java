package deque;

import java.util.Comparator;

public class IntElementComparator implements Comparator<Integer> {
    @Override
    public int compare(Integer a, Integer b) {
        return a - b;
    }
}
