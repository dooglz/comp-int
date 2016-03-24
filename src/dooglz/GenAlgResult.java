package dooglz;


public class GenAlgResult {
    public String result;
    public int bestScore;
    public int generation;
    public long runtime;

    public GenAlgResult(String result, int bestScore, int generation, long runtime) {
        this.result = result;
        this.bestScore = bestScore;
        this.generation = generation;
        this.runtime = runtime;
    }
}
