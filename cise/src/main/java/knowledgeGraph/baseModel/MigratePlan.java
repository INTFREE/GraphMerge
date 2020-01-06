package knowledgeGraph.baseModel;

import java.util.ArrayList;
import java.util.Collection;

public class MigratePlan {
    private ArrayList<Plan> planArrayList;

    public MigratePlan() {
        this.planArrayList = new ArrayList<>();
    }

    public ArrayList<Plan> getPlanArrayList() {
        return planArrayList;
    }

    public void setPlanArrayList(ArrayList<Plan> planArrayList) {
        this.planArrayList = planArrayList;
    }

    public void addPlan(Plan plan) {
        this.planArrayList.add(plan);
    }

    public void addPlans(Collection<? extends Plan> plans) {
        planArrayList.addAll(plans);
    }
}
