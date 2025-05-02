import tage.ai.behaviortrees.BTCondition;

public class GetSmall extends BTCondition {
    NPC npc;
    NPCcontroller npcc;
    GameServerUDP server;

    public GetSmall(GameServerUDP s, NPCcontroller c, NPC n, boolean toNegate) {
        super(toNegate);
        server = s;
        npcc = c;
        npc = n;
    }

    protected boolean check() {
        server.sendGetSmall();
        return true;
    }
}
