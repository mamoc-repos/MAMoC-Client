package uk.ac.standrews.cs.mamoc.SearchText;

import uk.ac.st_andrews.cs.mamoc_client.Model.Offloadable;
import static java.lang.Thread.dumpStack;

@Offloadable
public class KMP {

//    private int[] build_pi(String str){
//        int n = str.length();
//        int[] pi = new int[n+1];
//        int k = -1;
//
//        pi[0] = -1;
//
//        for (int i=0; i< n; i++){
//            while (k>=0 && str.charAt(k) != str.charAt(i)){
//                k = pi[k];
//            }
//            k += 1;
//            pi[i+1] = k;
//        }
//
//        return pi;
//    }
//
//    public ArrayList<Integer> searchKMP(String text, String pattern ){
//        ArrayList<Integer> matches = new ArrayList<Integer>();
//        int n = text.length();
//        int m = pattern.length();
//
//        int k = 0;
//        int[] pi = build_pi(pattern);
//
//        for (int i=0; i<n ; i++){
//            while (k >= 0 && (k == m || pattern.charAt(k) != text.charAt(i))){
//                k = pi[k];
//            }
//            k += 1;
//            if (k == m){
//                matches.add( i-m + 1);
//            }
//        }
//
//        return matches;
//    }

    public int run(String txt, String pat) {
        int matches = 0;
        int M = pat.length();
        int N = txt.length();

        // create lps[] that will hold the longest
        // prefix suffix values for pattern
        int lps[] = new int[M];
        int j = 0; // index for pat[]

        // Preprocess the pattern (calculate lps[] array)
        computeLPSArray(pat, M, lps);

        int i = 0; // index for txt[]
        while (i < N) {
            if (pat.charAt(j) == txt.charAt(i)) {
                j++;
                i++;
            }
            if (j == M) {
//                System.out.println("Found pattern "
//                        + "at index " + (i - j));
                matches++;
                j = lps[j - 1];
            }

            // mismatch after j matches
            else if (i < N && pat.charAt(j) != txt.charAt(i)) {
                // Do not match lps[0..lps[j-1]] characters,
                // they will match anyway
                if (j != 0)
                    j = lps[j - 1];
                else
                    i = i + 1;
            }
        }

        return matches;
    }

    private void computeLPSArray(String pat, int M, int lps[])
    {
        // length of the previous longest prefix suffix
        int len = 0;
        int i = 1;
        lps[0] = 0; // lps[0] is always 0

        // the loop calculates lps[i] for i = 1 to M-1
        while (i < M) {
            if (pat.charAt(i) == pat.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            }
            else // (pat[i] != pat[len])
            {
                // This is tricky. Consider the example.
                // AAACAAAA and i = 7. The idea is similar
                // to search step.
                if (len != 0) {
                    len = lps[len - 1];

                    // Also, note that we do not increment
                    // i here
                }
                else // if (len == 0)
                {
                    lps[i] = len;
                    i++;
                }
            }
        }

        dumpStack();
    }
}
