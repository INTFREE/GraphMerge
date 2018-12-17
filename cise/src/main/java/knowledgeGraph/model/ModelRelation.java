package knowledgeGraph.model;

import java.util.ArrayList;
import java.util.Map;

class Roles {
    public Integer id;
    public String roleName;

    Roles(Integer id, String roleName) {
        this.id = id;
        this.roleName = roleName;
    }
}

public class ModelRelation {
    public Integer id;
    public String value;
    public ArrayList<Roles> rolesArrayList;

    public ModelRelation(Integer id, String value, Map<Integer, String> roles) {
        this.id = id;
        this.value = value;
        this.rolesArrayList = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : roles.entrySet()) {
            this.rolesArrayList.add(new Roles(entry.getKey(), entry.getValue()));
        }
    }
}
