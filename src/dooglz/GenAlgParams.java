package dooglz;

public class GenAlgParams {
    public int seedRange;
    public int tournamentSampleSize;
    public int tournamentNewChilderenCount;
    public int tournamentMutateRange;
    public int resetTrigger;
    public int goal;
    public int popsize;
    public int crossovermode;
    public int problemID;
    public int maxGen;
    public long maxTime;

    public GenAlgParams(int seedRange, int tournamentSampleSize, int tournamentNewChilderenCount,
                        int tournamentMutateRange, int resetTrigger, int goal, int popsize, int crossovermode,
                        int problemID, int maxGen, long maxTime) {
        this.seedRange = seedRange;
        this.tournamentSampleSize = tournamentSampleSize;
        this.tournamentNewChilderenCount = tournamentNewChilderenCount;
        this.tournamentMutateRange = tournamentMutateRange;
        this.resetTrigger = resetTrigger;
        this.goal = goal;
        this.popsize = popsize;
        this.crossovermode = crossovermode;
        this.problemID = problemID;
        this.maxGen = maxGen;
        this.maxTime = maxTime;
    }

    public GenAlgParams() {
        this.goal = 100;
        this.problemID = 100;
        this.maxGen = 200;
        this.tournamentNewChilderenCount = 4;
        this.tournamentSampleSize = 32;
        this.crossovermode = 5;
        this.maxTime = 400000;
        this.popsize = 128;
        this.seedRange = 32;
        this.resetTrigger = 300;
        this.tournamentMutateRange = 128;
    }

}
