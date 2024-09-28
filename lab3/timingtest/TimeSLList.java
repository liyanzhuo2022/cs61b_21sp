package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        int[] sizes = {1000, 2000, 4000, 8000, 16000, 32000, 64000};
        int N = 1000;
        AList<Integer> Ns = new AList<>();
        AList<Double> optimes = new AList<>();
        AList<Integer> opCounts = new AList<>();

        for (int size : sizes) {
            Ns.addLast(size);
            opCounts.addLast(N);
            SLList<Integer> lst = new SLList<>();
            for (int i = 0; i < size; i++) {
                lst.addLast(1);
            }
            Stopwatch sw = new Stopwatch();
            for (int i = 0; i < N; i++) {
                lst.getLast();
            }
            double timeInSeconds = sw.elapsedTime();
            optimes.addLast(timeInSeconds);
        }
        printTimingTable(Ns, optimes,opCounts);

    }

}
