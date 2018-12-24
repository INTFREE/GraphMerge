package knowledgeGraph.model;

public class InstanceOf {
    public Integer iId;
    public Integer jId;

    public InstanceOf(Integer iId, Integer jId) {
        this.iId = iId;
        this.jId = jId;
    }

    @Override
    public String toString() {
        return "iId " + this.iId.toString() + " jId " + this.jId.toString();
    }
}
