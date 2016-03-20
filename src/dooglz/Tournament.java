package dooglz;

import modelP.JSSP;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dooglz.util.*;


public class Tournament {
    private static int machinecount;
    private static int jobcount;

    public Tournament(int machinecount, int jobcount) {
        Tournament.machinecount = machinecount;
        Tournament.jobcount = jobcount;
    }

    public <E, L extends List<E>> void swap(final L list1, final L list2,
                                            final int index) {
        final E temp = list1.get(index);
        list1.set(index, list2.get(index));
        list2.set(index, temp);

    }

    public <E, L extends List<E>> void swap(final L list1, final L list2,
                                            final int start, final int end) {
        for (int i = start; i < end; i++) {
            swap(list1, list2, i);
        }
    }

    public DSolution[] Pair(final DSolution[] oldPop, int offset) {
        // return CeiliPair(oldPop);
        return TournamentPair(oldPop, offset);
        // return RandPair(oldPop);
    }

    public void Crossover(DSolution a, DSolution b) {
        //PMXCrossover(a, b);
       // SPCrossover(a, b);
       // MPCrossover(a,b);
        RenQingCrossover(a,b);
    }

    public DSolution[] CeiliPair(final DSolution[] oldPop) {
        ArrayList<DSolution> newSolutions = new ArrayList<>();
        for (int i = 0; i < oldPop.length / 2; i++) {
            //  System.out.print("Pairing ");
            for (int j = i; j < oldPop.length - i; j++) {
                //   System.out.print(" " + i + "&" + j);
                DSolution newSol1 = new DSolution(machinecount, jobcount);
                DSolution newSol2 = new DSolution(machinecount, jobcount);
                newSol1.sol = cpy2D(oldPop[i].sol);
                newSol2.sol = cpy2D(oldPop[j].sol);
                newSol1.age = Math.max(oldPop[i].age, oldPop[j].age);
                newSol2.age = newSol1.age;
                Crossover(newSol1, newSol2);
                newSolutions.add(newSol1);
                newSolutions.add(newSol2);
            }
            //   System.out.println();
        }
        return newSolutions.toArray(new DSolution[newSolutions.size()]);
    }

    public DSolution[] TournamentPair(final DSolution[] oldPop, int offset) {
        ArrayList<DSolution> newSolutions = new ArrayList<>();
        for (int i = 0; i < offset; i++) {
            newSolutions.add(oldPop[i]);
            newSolutions.add(oldPop[oldPop.length - 1 - i]);
        }
        for (int i = offset; i < oldPop.length - 1; i += 2) {
            if (Math.random() < 0.5) {
                newSolutions.add(oldPop[i]);
                newSolutions.add(oldPop[i + 1]);
            } else {

                DSolution newSol1 = new DSolution(machinecount, jobcount);
                DSolution newSol2 = new DSolution(machinecount, jobcount);
                newSol1.sol = cpy2D(oldPop[i].sol);
                newSol2.sol = cpy2D(oldPop[i + 1].sol);
                newSol1.age = Math.max(oldPop[i].age, oldPop[i + 1].age);
                newSol2.age = newSol1.age;
                Crossover(newSol1, newSol2);
                newSolutions.add(newSol1);
                newSolutions.add(newSol2);
                // System.out.print(" " + i + "&" + (i+1) + " " + (newSol1.Score() - oldPop[i].Score())+ " " + (newSol2.Score() - oldPop[i+1].Score()) );
            }
        }
        // System.out.println();
        return newSolutions.toArray(new DSolution[newSolutions.size()]);
    }

    public DSolution[] RandPair(final DSolution[] oldPop) {
        ArrayList<DSolution> newSolutions = new ArrayList<>();
        for (int i = 0; i < oldPop.length - 1; i++) {
            //   System.out.print(" " + i + "&" + j);
            DSolution newSol1 = new DSolution(machinecount, jobcount);
            DSolution newSol2 = new DSolution(machinecount, jobcount);
            int a = (int) Math.floor(Math.random() * (double) (oldPop.length));
            int b = a;
            while (b == a) {
                b = (int) Math.floor(Math.random() * (double) (oldPop.length));
            }
            newSol1.sol = cpy2D(oldPop[a].sol);
            newSol2.sol = cpy2D(oldPop[b].sol);
            newSol1.age = Math.max(oldPop[a].age, oldPop[b].age);
            newSol2.age = newSol1.age;
            Crossover(newSol1, newSol2);
            newSolutions.add(newSol1);
            newSolutions.add(newSol2);
            newSol1.Score(true);
            newSol2.Score(true);
        }
        //   System.out.println();
        return newSolutions.toArray(new DSolution[newSolutions.size()]);
    }

    public void RemoveDupes(DSolution[] p) {
        for (int i = 0; i < p.length - 1; i++) {
            outer:
            for (int j = i + 1; j < p.length - 1; j++) {
                for (int k = 0; k < p[i].sol.length - 1; k++) {
                    for (int l = 0; l < p[i].sol[k].length - 1; l++) {
                        if (p[i].sol[k][l] != p[j].sol[k][l]) {
                            continue outer;
                        }
                    }

                }
                //must be equal
                System.out.println(i + " is dupe!");
                p[i] = new DSolution(JSSP.getRandomSolution(Main.problem.pProblem), Main.problem.machineCount, Main.problem.jobCount);
            }
        }
    }


    public void mutateMe(DSolution s) {
        int j = (int) Math.floor(Math.random() * ((double) s.sol.length));
        int r1 = (int) Math.floor(Math.random() * ((double) s.sol[0].length));
        int r2 = r1;
        while (r1 == r2) {
            r2 = (int) Math.floor(Math.random() * ((double) s.sol[0].length));
        }
        int tmp = s.sol[j][r1];
        s.sol[j][r1] = s.sol[j][r2];
        s.sol[j][r2] = tmp;
        s.Score(true);
    }

    public DSolution mutate(DSolution s) {
        DSolution newSol = new DSolution(machinecount, jobcount);
        for (int i = 0; i < machinecount; i++) {
            System.arraycopy(s.sol[i], 0, newSol.sol[i], 0, jobcount);
        }
        mutateMe(newSol);
        return newSol;
    }

    public void Churn(DSolution[] population, DProblem problem, int runs) {
        int prevavg = 0;
        int prevavg10 = 0;
        int improvement;
        int divergence;
        int best = 0;
        int popIncrease = 0;
        Arrays.sort(population);

        for (int i = 0; i < (i + 1); i++) {

            DSolution[] newChilderen = Pair(population, i % 2);
            System.arraycopy(population, 0, newChilderen, 0, newChilderen.length);
            //System.out.println();
            Arrays.sort(population);
            for (int j = 1; j < population.length - 1; j++) {
                double chance = ((double) j / (double) population.length) + 0.2;
                if (Math.random() < chance) {
               //     mutateMe(population[j]);
                }
            }
            for (int j = population.length - 10; j < population.length - 1; j++) {
                DSolution d = new DSolution(JSSP.getRandomSolution(Main.problem.pProblem), Main.problem.machineCount, Main.problem.jobCount);
                if(d.Score(true) >=population[j].Score(false) ){
                    population[j] = d;
                    //System.out.println("boop");
                }
            }

            RemoveDupes(population);
            Arrays.sort(population);


            int ob = best;
            best = population[0].Score(false);
            if (ob != best) {

                int avg = 0, avg50 = 0, avg25 = 0, avg10 = 0;
                for (int j = 0; j < population.length; j++) {
                    if (j < Math.floor(population.length * 0.1)) {
                        avg10 += population[j].Score(false);
                    }
                    if (j < Math.floor(population.length * 0.25)) {
                        avg25 += population[j].Score(false);
                    }
                    if (j < Math.floor(population.length * 0.5)) {
                        avg50 += population[j].Score(false);
                    }
                    avg += population[j].Score(false);
                }
                avg /= population.length;
                avg50 /= (population.length * 0.5);
                avg25 /= (population.length * 0.25);
                avg10 /= (population.length * 0.1);


                improvement = avg10 - prevavg10;
                divergence = avg - prevavg;
                prevavg10 = avg10;
                prevavg = avg;
                if (divergence < 200) {
                    popIncrease += 8;
                } else if (divergence > 400) {
                    popIncrease = Math.max(popIncrease - 4, 0);
                }
                popIncrease = Math.min(1024, popIncrease);
                System.out.print("Run: " + i + " Top:" + best + " avg10:" + avg10 + " avg25:" + avg25 + " avg50:" + avg50 + " avg:" + avg);
                System.out.print(" improvement: " + improvement + "\tdivergence:" + divergence + "\tpopIncrease:" + popIncrease + " " + population.length);
                System.out.println();
            }
        }
        int gg = 0;
    }

    public void RenQingCrossover(DSolution s1, DSolution s2) {

        //pick a random set of machines
       // final int mc = (int) Math.floor(Math.random() * ((double) s1.sol.length));
        int mc = 1;
        List<Integer> mcs = IntStream.rangeClosed(0, s1.sol.length-1).boxed().collect(Collectors.toList());
        Collections.shuffle(mcs);
        mcs = mcs.subList(0,mc);
        for (Integer i : mcs) {
            int[] tmp = s1.sol[i];
            s1.sol[i] = s2.sol[i];
            s2.sol[i] = tmp;
        }

    }

    public void SPCrossover(DSolution s1, DSolution s2) {
        // pick a random machine
        int m = (int) Math.floor(Math.random() * ((double) s1.sol.length));
        //pick a cxo point
        int cxop = (int) Math.floor(Math.random() * ((double) s1.sol[m].length - 1));
        //swap elememts
        for (int i = cxop; i < s1.sol[m].length; i++) {
            int tmp = s1.sol[m][i];
            s1.sol[m][i] = s2.sol[m][i];
            s2.sol[m][i] = tmp;
        }
        //now we must fix
        s1.FixPermutation(m);
        s2.FixPermutation(m);
    }

    public void MPCrossover(DSolution s1, DSolution s2) {
        // pick a random machine
        int m = (int) Math.floor(Math.random() * ((double) s1.sol.length));
        //pick a cxoS point
        int cxo1 = (int) Math.floor(Math.random() * ((double) s1.sol[m].length - 1));
        int cxo2 = cxo1;
        while(cxo1 != cxo2){
            cxo2 = (int) Math.floor(Math.random() * ((double) s1.sol[m].length - 1));
        }
        int cxop = Math.min(cxo1,cxo2);
        int cxopE = Math.max(cxo1,cxo2);

        //swap elememts
        for (int i = cxop; i < cxopE; i++) {
            int tmp = s1.sol[m][i];
            s1.sol[m][i] = s2.sol[m][i];
            s2.sol[m][i] = tmp;
        }
        //now we must fix
        s1.FixPermutation(m);
        s2.FixPermutation(m);
    }

    //Thanks to https://github.com/jfinkels/jmona
    public void PMXCrossover(DSolution s1, DSolution s2) {
        for (int machine = 0; machine < s1.sol.length; machine++) {
            //omg there goes my perf
            List<Integer> tour1 = IntArrToList(s1.sol[machine]);
            List<Integer> tour2 = IntArrToList(s2.sol[machine]);
            final int size = tour1.size();

            // choose two random numbers for the start and end indices of the slice
            // (one can be at index "size")
            final int number1 = (int) (Math.floor(Math.random() * ((float) size)));
            int number2;
            do {
                number2 = (int) (Math.floor(Math.random() * ((float) (size - 1))));
            } while (Math.abs(number2 - number1) < 2);

            // make the smaller the start and the larger the end
            final int start = Math.min(number1, number2);
            final int end = Math.max(number1, number2);

            // crossover the section in between the start and end indices
            swap(tour1, tour2, start, end);

            // get a view of the crossover over sections in each tour
            final List<Integer> swappedSectionInTour1 = tour1.subList(start, end);
            final List<Integer> swappedSectionInTour2 = tour2.subList(start, end);

            int currentCity;
            int replacementCityIndex;
            int replacementCity;

            // iterate over each city in not in the crossed over section
            for (int i = end % size; i >= end || i < start; i = (i + 1) % size) {
                //  System.out.println(i);
                // get the current city being examined in tour 1
                currentCity = tour1.get(i);

                // if that city is repeated in the crossed over section
                if (swappedSectionInTour1.contains(currentCity)) {

                    // get the index of the city to replace the repeated city (within the swapped section)
                    replacementCityIndex = swappedSectionInTour1.indexOf(currentCity);

                    // get the city that is intended to replace the repeated city
                    replacementCity = swappedSectionInTour2.get(replacementCityIndex);

                    // if the repeated city is also contained in the crossed over section
                    while (swappedSectionInTour1.contains(replacementCity)) {

                        // get the index of the city to replace the repeated city
                        replacementCityIndex = swappedSectionInTour1.indexOf(replacementCity);

                        // get the city that is intended to replace the repeated city
                        replacementCity = swappedSectionInTour2.get(replacementCityIndex);

                    }

                    // replace the current city with the replacement city
                    tour1.set(i, replacementCity);
                }

                // get the current city being examined in tour 2
                currentCity = tour2.get(i);

                // if that city is repeated in the crossed over section
                if (swappedSectionInTour2.contains(currentCity)) {

                    // get the index of the city to replace the repeated city
                    replacementCityIndex = swappedSectionInTour2.indexOf(currentCity);

                    // get the city that is intended to replace the repeated city
                    replacementCity = swappedSectionInTour1.get(replacementCityIndex);

                    // if the repeated city is also contained in the crossed over section
                    while (swappedSectionInTour2.contains(replacementCity)) {

                        // get the index of the city to replace the repeated city
                        replacementCityIndex = swappedSectionInTour2.indexOf(replacementCity);

                        // get the city that is intended to replace the repeated city
                        replacementCity = swappedSectionInTour1.get(replacementCityIndex);
                    }

                    // replace the current city with the replacement city
                    tour2.set(i, replacementCity);
                }
            }
            s1.sol[machine] = IntListToArr(tour1);
            s2.sol[machine] = IntListToArr(tour2);
        }


    }

}
