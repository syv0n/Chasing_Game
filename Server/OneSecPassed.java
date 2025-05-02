import tage.ai.behaviortrees.BTCondition;

public class OneSecPassed extends BTCondition {
    NPC npc; 
    NPCcontroller npcc;
    private long lastTime;

    public OneSecPassed(NPCcontroller c, NPC n, boolean toNegate) {
        super(toNegate);
        npcc = c;
        npc = n;
    }

    // checks to see if one sec has passed
    protected boolean check() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime >= 1000) {
            lastTime = currentTime;
            return true;
        }
        return false;
    }
}
