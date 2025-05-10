import tage.ai.behaviortrees.BTCondition;

public class OneSecPassed extends BTCondition {
    NPC npc;
    NPCcontroller npcc;
    private long lastTime;
    
    public OneSecPassed(NPCcontroller var1, NPC var2, boolean var3) {
        super(var3);
        this.npcc = var1;
        this.npc = var2;
        this.lastTime = System.currentTimeMillis(); // Initialize timer
    }

    protected boolean check() {
        // Only return true if: 1 second passed AND no player is near
        if (!npcc.getNearFlag() && System.currentTimeMillis() - lastTime >= 1000L) {
            lastTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }
}
