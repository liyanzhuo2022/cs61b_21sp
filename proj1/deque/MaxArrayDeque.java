package deque;

import java.util.Comparator;
import java.util.Iterator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> cd;
    public MaxArrayDeque(Comparator<T> c) {
        this.cd = c;
    }

    public T max() {
        if (this.size() == 0) {
            return null;
        } else {
            T maxItem = this.get(0);
            Iterator<T> it = this.iterator();
            while (it.hasNext()) {
                T nextItem = it.next();
                if (cd.compare(nextItem, maxItem) > 0) {
                    maxItem = nextItem;
                }
            }
            return maxItem;
        }
    }

    public T max(Comparator<T> c) {
        if (this.size() == 0) {
            return null;
        } else {
            T maxItem = this.get(0);
            Iterator<T> it = this.iterator();
            while (it.hasNext()) {
                T nextItem = it.next();
                if (c.compare(nextItem, maxItem) > 0) {
                    maxItem = nextItem;
                }
            }
            return maxItem;
        }
    }
}
