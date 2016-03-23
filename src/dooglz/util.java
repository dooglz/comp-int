package dooglz;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class util {
    public static ArrayList<Integer> IntArrToList(final int[] ints) {
        ArrayList<Integer> intList = new ArrayList<Integer>();
        for (int index = 0; index < ints.length; index++) {
            intList.add(ints[index]);
        }
        return intList;
    }

    public static int[] IntListToArr(final List<Integer> integers) {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++) {
            ret[i] = iterator.next().intValue();
        }
        return ret;
    }

    public static int[][] cpy2D(int[][] old) {
        int[][] current = new int[old.length][old[0].length];
        for (int i = 0; i < old.length; i++)
            for (int j = 0; j < old[i].length; j++)
                current[i][j] = old[i][j];
        return current;
    }

    public static int find(final int[]ar, int a){
        for (int i = 0; i < ar.length; i++) {
            if(ar[i] == a){
                return i;
            }
        }
        return -1;
    }
}
