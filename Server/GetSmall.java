import tage.ai.behaviortrees.BTCondition;

public class GetSmall extends BTCondition {
    NPC npc;

    public GetSmall(NPC n) {
        super(false);
        npc = n;
    }

    protected boolean check() {
        npc.getSmall();
        return true;
    }
}
