package knowledgeGraph.model;

import java.util.ArrayList;
import java.util.Map;

class Role {
    public Integer id;
    public String roleName;

    Role(Integer id, String roleName) {
        this.id = id;
        this.roleName = roleName;
    }

    @Override
    public String toString() {
        return "role id " + this.id.toString() + " roleName " + this.roleName + "\n";
    }
}

public class RelationModel {
    public Integer id;
    public String value;
    public ArrayList<Role> rolesArrayList;

    public RelationModel(Integer id, String value, Map<Integer, String> roles) {
        this.id = id;
        this.value = value;
        this.rolesArrayList = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : roles.entrySet()) {
            this.rolesArrayList.add(new Role(entry.getKey(), entry.getValue()));
        }
    }
    public void addRole(Map<Integer, String> roles){
        for (Map.Entry<Integer, String> entry : roles.entrySet()) {
            this.rolesArrayList.add(new Role(entry.getKey(), entry.getValue()));
        }
    }

    public ArrayList<Role> getRolesArrayList() {
        return this.rolesArrayList;
    }

    @Override
    public String toString() {
        String res = "relation_id " + this.id.toString();
        res += " value " + this.value + "\n";
        for(Role role : this.rolesArrayList){
            res += role.toString();
        }
        return res;
    }
}
