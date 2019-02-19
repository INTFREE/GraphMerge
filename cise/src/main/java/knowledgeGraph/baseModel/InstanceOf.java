package knowledgeGraph.baseModel;

public class InstanceOf {
    private Integer iId;
    private Integer jId;

    public InstanceOf(Integer iId, Integer jId) {
        this.iId = iId;
        this.jId = jId;
    }

    @Override
    public String toString() {
        return "iId " + this.iId.toString() + " jId " + this.jId.toString();
    }

    public Integer getiId() {
        return iId;
    }

    public Integer getjId() {
        return jId;
    }

    public void setiId(Integer iId) {
        this.iId = iId;
    }

    public void setjId(Integer jId) {
        this.jId = jId;
    }
}
