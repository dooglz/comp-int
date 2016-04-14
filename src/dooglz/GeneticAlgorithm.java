package dooglz;

import modelP.JSSP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dooglz.util.*;
import static java.lang.Thread.currentThread;

public class GeneticAlgorithm {
    public GenAlgParams p;
    public DProblem problem;
    private int machinecount;
    private int jobcount;
    private boolean shouldStop;

    public GeneticAlgorithm(GenAlgParams params) {
        this.p = params;
        this.problem = new DProblem(JSSP.getProblem(params.problemID));
        this.machinecount = problem.machineCount;
        this.jobcount = problem.jobCount;
    }

    public void HandleCmd(String cmd){
        switch (cmd){
            case"stop":
                shouldStop = true;
                break;
        }
    }

    public GenAlgResult Start() {
        int prevavg = 0;
        int prevavg10 = 0;
        int improvement=0 ;
        int divergence=0;
        int best = 0;
        int resets = 0;
        int[] divergenceAverage = new int[10];
        long[] generationTimeAverage = new long[10];
        int dai = 0;
        int gtai = 0;
        shouldStop = false;
        DSolution population[] = new DSolution[p.popsize];

        for (int i = 0; i < p.popsize; i++) {
            if (i % 16 == 0) {
                System.out.println(currentThread().getId() + " PreGen " + i * 100 / p.popsize + "%");
            }
            population[i] = DSolution.getRand(problem, true, p.seedRange, p.goal);
        }

        Arrays.sort(population);
        int bestever = Integer.MAX_VALUE;
        int i = 0;
        long startTime = System.currentTimeMillis();
        while (true&& !shouldStop) {

            if (i > 2 && i % p.resetTrigger == 0) {
                resets++;
                for (int j = 1; j < population.length; j++) {
                    population[j] = mutate(population[0]);
                    int whileGuard = 0;
                    while (solutionWithin(Arrays.copyOfRange(population, 0, j), population[j])) {
                        mutateMe(population[j]);
                        if (whileGuard > 4092) {
                            return new GenAlgResult("stall", bestever, i, System.currentTimeMillis() - startTime);
                        }
                        whileGuard++;
                    }
                }
            }
            long GenStartTime = System.currentTimeMillis();

            DSolution[] newChilderen = Pair(population, 0);
            DSolution[] newPop = new DSolution[population.length + newChilderen.length];
            System.arraycopy(population, 0, newPop, 0, population.length);
            System.arraycopy(newChilderen, 0, newPop, population.length, newChilderen.length);
            Arrays.sort(newPop);
            System.arraycopy(newPop, 0, population, 0, population.length);
            RemoveDupes(population);

            generationTimeAverage[gtai++] = System.currentTimeMillis() - GenStartTime;
            if (gtai >= generationTimeAverage.length) {
                gtai = 0;
            }

            int ob = best;
            best = population[0].Score(false);
            bestever = Math.min(bestever, best);

            if (bestever <= p.goal) {
                System.out.println(currentThread().getId() + " BEST SOLUTION FOUND! Generation: " + i + " score:" + bestever+ " goal: "+p.goal+ " ");
                GenAlgResult gr = new GenAlgResult("done", bestever, i, System.currentTimeMillis() - startTime);
                gr.sol = population[0];
                return gr;

            }

            if (ob != best || i % 25 == 0) {
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
                divergenceAverage[dai++] = divergence;
                if (dai >= divergenceAverage.length) {
                    dai = 0;
                }
                int da = 0;
                for (int d : divergenceAverage) {
                    da += d;
                }
                da /= divergenceAverage.length;
                long gta = 0;
                for (long d : generationTimeAverage) {
                    gta += d;
                }
                gta /= generationTimeAverage.length;
                //evaluate ending
                if (improvement == 0 && da == 0 && i > 10) {
                    GenAlgResult gr = new GenAlgResult("stall", bestever, i, System.currentTimeMillis() - startTime);
                    gr.sol = population[0];
                    return gr;
                }
                System.out.print(currentThread().getId() + " Run: " + i + " BestEver: " + bestever + " Top:" + best + " avg10:" + avg10 + " avg25:" + avg25 + " avg50:" + avg50 + " avg:" + avg);
                System.out.print(" improvement: " + improvement + "\tdivergence:" + divergence + "\tdivavg:" + da + "\tresets:" + resets + "\tGenTime:" + gta);
                System.out.println();
                i++;
            }
            //evaluate ending
            if ((i > p.maxGen) || (System.currentTimeMillis() - startTime) > p.maxTime) {
                GenAlgResult gr = new GenAlgResult("stall", bestever, i, System.currentTimeMillis() - startTime);
                gr.sol = population[0];
                return gr;
            }
        }
        GenAlgResult gr = new GenAlgResult("stall", bestever, i, System.currentTimeMillis() - startTime);
        gr.sol = population[0];
        return gr;
        //return new GenAlgResult("stall",bestever,0);
    }

    public DSolution[] Pair(final DSolution[] oldPop, int offset) {
        // return CeiliPair(oldPop);
        return TournamentPair(oldPop, offset);
        // return RandPair(oldPop);
    }

    public void Crossover(DSolution a, DSolution b) {
        switch (p.crossovermode) {
            case 1:
                SPCrossover(a, b);
                break;
            case 2:
                MPCrossover(a, b);
                break;
            case 3:
                PMXCrossover(a, b);
                break;
            case 4:
                RenQingCrossover(a, b);
                break;
            case 5:
                LiangGaoCrossover(a, b);
                break;
            default:
                break;
        }
    }

    public DSolution[] CeiliPair(final DSolution[] oldPop) {
        ArrayList<DSolution> newSolutions = new ArrayList<>();
        for (int i = 0; i < oldPop.length / 2; i++) {
            //  System.out.print("Pairing ");
            for (int j = i; j < oldPop.length - i; j++) {
                //   System.out.print(" " + i + "&" + j);
                DSolution newSol1 = new DSolution(problem, machinecount, jobcount);
                DSolution newSol2 = new DSolution(problem, machinecount, jobcount);
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

    public void ImproveMe(DSolution d, int runs) {
        // d.MakeFeasible();
        for (int j = 0; j < runs; j++) {
            DSolution mut = mutate(d);
            // mut.MakeFeasible();
            if (mut.Score(true) <= d.Score(false)) {
                d = mut;
            }
        }
    }

    public DSolution[] TournamentPair(final DSolution[] oldPop, int offset) {
        DSolution[] rndSolutions = new DSolution[p.tournamentSampleSize];
        //pick  N random solutions from old pop
        ArrayList<Integer> is = new ArrayList<>();
        for (int i = 0; i < rndSolutions.length; i++) {
            int r = (int) Math.floor(Math.random() * (double) (oldPop.length));
            int whileGuard = 0;
            while (is.contains(r)) {
                whileGuard++;
                if (whileGuard > 2048) {
                    continue;
                }
                r = (int) Math.floor(Math.random() * (double) (oldPop.length));
            }
            is.add(r);
            rndSolutions[i] = oldPop[r];
        }
        Arrays.sort(rndSolutions);
        DSolution[] newChilderen = new DSolution[p.tournamentNewChilderenCount];
        for (int i = 0; i < newChilderen.length; i++) {
            DSolution a = new DSolution(problem, machinecount, jobcount);
            DSolution b = new DSolution(problem, machinecount, jobcount);
            a.sol = cpy2D(rndSolutions[i * 2].sol);
            b.sol = cpy2D(rndSolutions[(i * 2) + 1].sol);
            a.age = Math.max(rndSolutions[i * 2].age, rndSolutions[(i * 2) + 1].age);
            b.age = a.age;
            Crossover(a, b);
            if (a.Score(true) > b.Score(true)) {
                newChilderen[i] = a;
            } else {
                newChilderen[i] = b;
            }
        }
        //local search
        for (int i = 0; i < newChilderen.length; i++) {
            //ImproveMe(newChilderen[i], 64);

            newChilderen[i] = getBestMutant(newChilderen[i], p.tournamentMutateRange);
            if (solutionWithin(oldPop, newChilderen[i])) {
                newChilderen[i] = DSolution.getRand(problem, false, 20, p.goal);
            }

        }

        return newChilderen;
    }

    public DSolution[] RandPair(final DSolution[] oldPop) {
        ArrayList<DSolution> newSolutions = new ArrayList<>();
        for (int i = 0; i < oldPop.length - 1; i++) {
            //   System.out.print(" " + i + "&" + j);
            DSolution newSol1 = new DSolution(problem, machinecount, jobcount);
            DSolution newSol2 = new DSolution(problem, machinecount, jobcount);
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

    public boolean solutionWithin(DSolution[] p, DSolution a) {
        for (int j = 0; j < p.length; j++) {
            if (DSolution.isEqual(p[j], a)) {
                return true;
            }
        }
        return false;
    }

    public void RemoveDupes(DSolution[] s) {
        for (int i = 0; i < s.length - 1; i++) {
            for (int j = i + 1; j < s.length - 1; j++) {
                if (!DSolution.isEqual(s[i], s[j])) {
                    continue;
                }
                System.out.println(i + " is dupe! " + j);
                s[i] = DSolution.getRand(problem, true, 64, p.goal);
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
        DSolution newSol = new DSolution(problem, machinecount, jobcount);
        for (int i = 0; i < machinecount; i++) {
            System.arraycopy(s.sol[i], 0, newSol.sol[i], 0, jobcount);
        }
        mutateMe(newSol);
        return newSol;
    }

    public DSolution getBestMutant(DSolution d, int runs) {
        DSolution m = mutate(d);
        //m.MakeFeasible();
        m.Score(true);
        for (int i = 0; i < runs; i++) {
            DSolution mm = mutate(d);
            //   mm.MakeFeasible();
            if (mm.Score(true) < m.Score(false)) {
                m = mm;
            }
        }
        return m;
    }

    public void LiangGaoCrossover(DSolution s1, DSolution s2) {
        final int totalJobs = s1.sol[0].length;
        final int[] setA = new int[totalJobs / 2];
        final int[] setB = new int[(totalJobs / 2) + (totalJobs % 2)];
        int ai = 0, bi = 0;
        //split jobs randomly into two sets
        for (int i = 0; i < totalJobs; i++) {
            boolean which = false;
            if (ai < setA.length && bi < setB.length) {
                if (Math.random() < 0.5) {
                    which = true;
                }
            } else if (bi < setB.length) {
                which = true;
            }

            if (!which) {
                setA[ai] = i;
                ai++;
            } else {
                setB[bi] = i;
                bi++;
            }
        }
        for (int m = 0; m < s1.sol.length; m++) {
            ai = 0;
            bi = 0;
            for (int job = 0; job < s1.sol[m].length; job++) {
                if (find(setA, s1.sol[m][job]) == -1) {
                    //replace with an item of setB, from s2, in the order of s2
                    while (find(setB, s2.sol[m][ai]) == -1) {
                        ai++;
                    }
                    s1.sol[m][job] = s2.sol[m][ai];
                    ai++;
                }
                if (find(setB, s2.sol[m][job]) == -1) {
                    //replace with an item of setA from s1, in the order of s1
                    while (find(setA, s1.sol[m][bi]) == -1) {
                        bi++;
                    }
                    s2.sol[m][job] = s1.sol[m][bi];
                    bi++;
                }
            }
        }
    }

    public void RenQingCrossover(DSolution s1, DSolution s2) {

        //pick a random set of machines
        // final int mc = (int) Math.floor(Math.random() * ((double) s1.sol.length));
        int mc = 1;
        List<Integer> mcs = IntStream.rangeClosed(0, s1.sol.length - 1).boxed().collect(Collectors.toList());
        Collections.shuffle(mcs);
        mcs = mcs.subList(0, mc);
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
        while (cxo1 == cxo2) {
            cxo2 = (int) Math.floor(Math.random() * ((double) s1.sol[m].length - 1));
        }
        int cxop = Math.min(cxo1, cxo2);
        int cxopE = Math.max(cxo1, cxo2);

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
            util.swap(tour1, tour2, start, end);

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
