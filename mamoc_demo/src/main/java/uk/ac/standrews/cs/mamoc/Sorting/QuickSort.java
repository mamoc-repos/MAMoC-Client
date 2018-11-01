package uk.ac.standrews.cs.mamoc.Sorting;

import uk.ac.st_andrews.cs.mamoc_client.Annotation.Offloadable;

@Offloadable
public class QuickSort {

    String content;
    String[] strArr;

    private static final String BLANK_SPACE=" ";

    public QuickSort(String words) {
        this.content = words;
        this.strArr = content.split(BLANK_SPACE);
    }

    public void run() {
        quickSort(strArr, 0, strArr.length - 1);
    }

    private String[] quickSort(String[] strArr, int p, int r){

        if(p<r)
        {
            int q=partition(strArr,p,r);
            quickSort(strArr,p,q);
            quickSort(strArr,q+1,r);
        }

        return strArr;
    }

    private static int partition(String[] strArr, int p, int r) {

        String x = strArr[p];
        int i = p-1 ;
        int j = r+1 ;

        while (true)
        {
            i++;
            while ( i< r && strArr[i].compareTo(x) < 0)
                i++;
            j--;
            while (j>p && strArr[j].compareTo(x) > 0)
                j--;

            if (i < j)
                swap(strArr, i, j);
            else
                return j;
        }
    }

    private static void swap(String[] strArr, int i, int j)
    {
        String temp = strArr[i];
        strArr[i] = strArr[j];
        strArr[j] = temp;
    }
}
