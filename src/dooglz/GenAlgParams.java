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

}
