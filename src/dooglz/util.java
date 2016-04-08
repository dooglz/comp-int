package dooglz;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class util {

    public static String msToString(long ms) {
        long totalSecs = ms/1000;
        long hours = (totalSecs / 3600);
        long mins = (totalSecs / 60) % 60;
        long secs = totalSecs % 60;
        String minsString = (mins == 0)
                ? "00"
                : ((mins < 10)
                ? "0" + mins
                : "" + mins);
        String secsString = (secs == 0)
                ? "00"
                : ((secs < 10)
                ? "0" + secs
                : "" + secs);
        if (hours > 0)
            return hours + ":" + minsString + ":" + secsString;
        else if (mins > 0)
            return mins + ":" + secsString;
        else return ":" + secsString;
    }

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

    public static int find(final int[] ar, int a) {
        for (int i = 0; i < ar.length; i++) {
            if (ar[i] == a) {
                return i;
            }
        }
        return -1;
    }

    public static <E, L extends List<E>> void swap(final L list1, final L list2,
                                                   final int index) {
        final E temp = list1.get(index);
        list1.set(index, list2.get(index));
        list2.set(index, temp);

    }

    public static <E, L extends List<E>> void swap(final L list1, final L list2,
                                                   final int start, final int end) {
        for (int i = start; i < end; i++) {
            swap(list1, list2, i);
        }
    }

}
