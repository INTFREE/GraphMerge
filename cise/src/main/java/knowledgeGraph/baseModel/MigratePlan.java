package knowledgeGraph.baseModel;

import java.util.ArrayList;

public class MigratePlan {
    private ArrayList<Plan> planArrayList;

    public ArrayList<Plan> getPlanArrayList() {
        return planArrayList;
    }

    public void setPlanArrayList(ArrayList<Plan> planArrayList) {
        this.planArrayList = planArrayList;
    }

    public void addPlan(Plan plan) {
        this.planArrayList.add(plan);
    }
}
