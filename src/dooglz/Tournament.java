package dooglz;

import modelP.JSSP;
import modelP.Problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import dooglz.util;

import static dooglz.util.*;


public class Tournament {
    public static int machinecount;
    public static int jobcount;

    public Tournament(int machinecount, int jobcount) {
        this.machinecount = machinecount;
        this.jobcount = jobcount;
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

    public DSolution[] Pair(final DSolution[] oldPop) {
       // return CeiliPair(oldPop);
        return NeighbourPair(oldPop);
    }

    public void Crossover(DSolution a, DSolution b) {
        PMXCrossover(a, b);
    }

    public DSolution[] CeiliPair(final DSolution[] oldPop) {
        ArrayList<DSolution> newSolutions = new ArrayList<DSolution>();
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

    public DSolution[] NeighbourPair(final DSolution[] oldPop) {
        ArrayList<DSolution> newSolutions = new ArrayList<DSolution>();
        for (int i = 0; i < oldPop.length-1; i++) {
                //   System.out.print(" " + i + "&" + j);
                DSolution newSol1 = new DSolution(machinecount, jobcount);
                DSolution newSol2 = new DSolution(machinecount, jobcount);
                newSol1.sol = cpy2D(oldPop[i].sol);
                newSol2.sol = cpy2D(oldPop[i+1].sol);
                newSol1.age = Math.max(oldPop[i].age, oldPop[i+1].age);
                newSol2.age = newSol1.age;
                Crossover(newSol1, newSol2);
                newSolutions.add(newSol1);
                newSolutions.add(newSol2);
            }
            //   System.out.println();
        return newSolutions.toArray(new DSolution[newSolutions.size()]);
    }


    public void Churn(DSolution[] population, DProblem problem, int runs) {
        int prevavg = 0;
        int prevavg10 = 0;
        int improvement = 0;
        int divergence = 0;
        int popIncrease =0;
        for (int i = 0; i < runs; i++) {
            Arrays.sort(population);
            DSolution[] newChilderen = Pair(population);
            DSolution[] newPop = new DSolution[newChilderen.length + population.length];
            System.arraycopy(population, 0, newPop, 0, population.length);
            System.arraycopy(newChilderen, 0, newPop, population.length, newChilderen.length);
            Arrays.sort(newPop);
            //slice and set
            if(popIncrease > 0) {
                int newRandoms = Math.max((popIncrease + population.length) - newPop.length, 0);
                if(newRandoms > 0){
                    population = new DSolution[population.length+popIncrease];
                    System.arraycopy(newPop, 0, population, 0, newPop.length);
                    for (int j = 0; j < newRandoms; j++) {
                        population[ newPop.length+j] = new DSolution(JSSP.getRandomSolution(problem.pProblem), problem.machineCount, problem.jobCount);
                    }
                }else{
                    population = Arrays.copyOf(newPop, population.length+popIncrease);
                }

            }else {
                population = Arrays.copyOf(newPop, population.length);
            }
            int avg = 0, avg50 = 0, avg25 = 0, avg10 = 0;
            for (int j = 0; j < population.length; j++) {
                if (j < Math.floor(population.length * 0.1)) {
                    avg10 += population[j].Score();
                }
                if (j <  Math.floor(population.length * 0.25)) {
                    avg25 += population[j].Score();
                }
                if (j <  Math.floor(population.length * 0.5)) {
                    avg50 += population[j].Score();
                }
                avg += population[j].Score();
            }
            avg /= population.length;
            avg50 /= (population.length * 0.5);
            avg25 /= (population.length * 0.25);
            avg10 /= (population.length * 0.1);


            improvement = avg10 - prevavg10;
            divergence = avg - prevavg;
            prevavg10 = avg10;
            prevavg = avg;
            if(divergence < 200){
                popIncrease += 8;
            }else if(divergence > 400){
                popIncrease = Math.max(popIncrease-4, 0);
            }
            popIncrease = Math.min(1024, popIncrease);
            System.out.print("Run: " + i + " Top:" + population[0].Score() + " avg10:" + avg10 + " avg25:" + avg25 + " avg50:" + avg50 + " avg:" + avg);
            System.out.print("improvement: " + improvement + " divergence:" + divergence + " popIncrease:" + popIncrease);
            System.out.println();
        }
        int gg =0;
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

            int currentCity = 0;
            int replacementCityIndex = 0;
            int replacementCity = 0;

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
