package dooglz;

import modelP.JSSP;
import modelP.Problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

class Sol implements Comparator<Sol> {
  public  int[][] sol;
    public  int age;
    public int score;
    @Override
    public int compare(Sol a, Sol b) {
        return a.score - b.score;
    }
}

public class Tournament {
    public static void Churn(int[][][] population, Problem problem, int runs){
        Sol[] main = new Sol[population.length];
        for (int i = 0; i < population.length; i++) {
            main[i].age =0;
            main[i].sol = population[i];
            main[i].score = JSSP.getFitness(main[i].sol,problem);
        }

        Arrays.sort(main);
        //calcualte score
                //pair everybody


    }
}
