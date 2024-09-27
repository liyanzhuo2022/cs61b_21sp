package IntList;

public class IntListExercises {

    /**
     * Part A: (Buggy) mutative method that adds a constant C to each
     * element of an IntList
     *
     * @param lst IntList from Lecture
     */
    public static void addConstant(IntList lst, int c) {
        IntList head = lst;
        while (head != null) {
            head.first += c;
            head = head.rest;
        }
    }

    /**
     * Part B: Buggy method that sets node.first to zero if
     * the max value in the list starting at node has the same
     * first and last digit, for every node in L
     *
     * @param L IntList from Lecture
     */
    public static void setToZeroIfMaxFEL(IntList L) {
        IntList p = L;
        while (p != null) {
            int currentMax = max(p);
            boolean firstEqualsLast = firstDigitEqualsLastDigit(currentMax);
            if (firstEqualsLast) {
                p.first = 0;
            }
            p = p.rest;
        }
    }

    /** Returns the max value in the IntList starting at L. */
    public static int max(IntList L) {
        int max = L.first;
        IntList p = L.rest;
        while (p != null) {
            if (p.first > max) {
                max = p.first;
            }
            p = p.rest;
        }
        return max;
    }

    /** Returns true if the last digit of x is equal to
     *  the first digit of x.
     */
    public static boolean firstDigitEqualsLastDigit(int x) {
        int lastDigit = x % 10;
        while (x >= 10) {
            x = x / 10;
        }
        int firstDigit = x % 10;
        return firstDigit == lastDigit;
    }

    /**
     * Part C: (Buggy) mutative method that squares each prime
     * element of the IntList.
     *
     * @param lst IntList from Lecture
     * @return True if there was an update to the list
     */
    public static boolean squarePrimes(IntList lst) {
        IntList p = lst;
        boolean hasPrimes = false;

        while (p != null) {
            if (Primes.isPrime(p.first)) {
                p.first *= p.first;
                hasPrimes = true;
            }
            p = p.rest;
        }

        return hasPrimes;
    }

    public static boolean squarePrimes_rec(IntList lst) {
        // Base Case: we have reached the end of the list
        if (lst == null) {
            return false;
        }

        // 先递归处理剩余部分
        boolean restUpdated = squarePrimes(lst.rest);

        // 检查当前元素是否是质数
        boolean currElemIsPrime = Primes.isPrime(lst.first);

        // 如果是质数，将其平方
        if (currElemIsPrime) {
            lst.first *= lst.first;
        }

        // 返回是否有更新
        return currElemIsPrime || restUpdated;
    }

}
