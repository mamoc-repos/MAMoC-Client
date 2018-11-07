package uk.ac.standrews.cs.mamoc.NQueens;

import uk.ac.st_andrews.cs.mamoc_client.Annotation.Offloadable;

@Offloadable
public class Queens {

    int n;

    public Queens(int N){
        this.n = N;
    }

    public void run() {
        int[] a = new int[n];
        enumerate(a, 0);
    }

    private static boolean isConsistent(int[] q, int n) {
        for (int i = 0; i < n; i++) {
            if (q[i] == q[n]) return false;
            if ((q[i] - q[n]) == (n - i)) return false;
            if ((q[n] - q[i]) == (n - i)) return false;
        }
        return true;
    }

    public static void enumerate(int[] q, int n) {
        int N = q.length;
        if (n == N) ;
        else {
            for (int i = 0; i < N; i++) {
                q[n] = i;
                if (isConsistent(q, n)) enumerate(q, n + 1);
            }
        }
    }
}
