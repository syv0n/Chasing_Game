import tage.ai.behaviortrees.BTCondition;


public class AvatarNear extends BTCondition {
    NPC npc;
    NPCcontroller npcc;
    GameServerUDP server;
    private long lastCheckTime;  // Add this

    public AvatarNear(GameServerUDP s, NPCcontroller c, NPC n, boolean toNegate) {
        super(toNegate);
        server = s;
        npcc = c;
        npc = n;
        lastCheckTime = System.currentTimeMillis(); // Initialize
    }

    protected boolean check() {
        // Only check proximity max once every 250ms
        if (System.currentTimeMillis() - lastCheckTime >= 250L) {
            server.sendCheckForAvatarNear();
            lastCheckTime = System.currentTimeMillis();
        }
        return npcc.getNearFlag();
    }
}
