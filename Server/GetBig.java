import tage.ai.behaviortrees.BTCondition;

public class GetBig extends BTCondition {
    NPC npc;
    NPCcontroller npcc;
    GameServerUDP server;

    public GetBig(GameServerUDP s, NPCcontroller c, NPC n, boolean toNegate) {
        super(toNegate);
        server = s;
        npcc = c;
        npc = n;
    }

    protected boolean check() {
        server.sendGetBig();
        return true;
    }
}
