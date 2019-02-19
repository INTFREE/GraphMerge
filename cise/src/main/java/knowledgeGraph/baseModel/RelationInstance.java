package knowledgeGraph.baseModel;

public class RelationInstance {
    public Integer id;
    public Integer roleId;
    public String type;
    public String roleName;

    public RelationInstance(Integer id, Integer roleId, String roleName) {
        this.id = id;
        this.roleId = roleId;
        this.roleName = roleName;
        if (roleName.isEmpty()) {
            this.type = "Entity";
        } else {
            this.type = "Value";
        }
    }

    @Override
    public String toString() {
        return "id " + id.toString() + " roleId " + this.roleId.toString() + " type " + type + " roleName " + roleName;
    }
}
