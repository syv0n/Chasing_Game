import tage.ai.behaviortrees.BTCondition;

public class GetBig extends BTCondition {
    NPC npc;

    public GetBig(NPC n) {
        super(false);
        npc = n;
    }

    protected boolean check() {
        npc.getBig();
        return true;
    }
}
