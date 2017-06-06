package hw2.indexing;

import java.util.List;

/**
 * Created by Abhishek Mulay on 6/2/17.
 */
public class IndexingUnit {
    private final String type = "UNIGRAM";
    private final String term;
    private final double termFrequency;
    private final List<Double> position;

    public IndexingUnit(String term, double termFrequency, List<Double> position) {
        this.term = term;
        this.termFrequency = termFrequency;
        this.position = position;
    }

    public String getTerm() {
        return term;
    }

    public double getTermFrequency() {
        return termFrequency;
    }

    public List<Double> getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "IndexingUnit{" + term + '|' +", tf=" + termFrequency + ", positions=" + position + '}';
    }
}
