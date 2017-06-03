package hw1.pojos;

/**
 * Created by Abhishek Mulay on 5/22/17.
 */
public class TermStatistics {

    private String term;
    private String documentId;
    private int termFrequency;
    private int documentFrequency;
    private int documentLength;
    private int ttf;

    public TermStatistics(String term, String documentId, int documentLength, int termFrequency, int documentFrequency, int ttf) {
        this.term = term;
        this.documentId = documentId;
        this.documentLength = documentLength;
        this.termFrequency = termFrequency;
        this.documentFrequency = documentFrequency;
        this.ttf = ttf;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public int getTermFrequency() {
        return termFrequency;
    }

    public void setTermFrequency(int termFrequency) {
        this.termFrequency = termFrequency;
    }

    public int getDocumentFrequency() {
        return documentFrequency;
    }

    public void setDocumentFrequency(int documentFrequency) {
        this.documentFrequency = documentFrequency;
    }

    public int getTtf() {
        return ttf;
    }

    public void setTtf(int ttf) {
        this.ttf = ttf;
    }

    public int getDocumentLength() {
        return documentLength;
    }

    public void setDocumentLength(int documentLength) {
        this.documentLength = documentLength;
    }

    @Override
    public String toString() {
        return "TermStatistics{" +
                "term='" + term + '\'' +
                ", documentId='" + documentId + '\'' +
                ", termFrequency=" + termFrequency +
                ", documentFrequency=" + documentFrequency +
                ", ttf=" + ttf +
                '}';
    }
}
