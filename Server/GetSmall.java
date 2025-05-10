import tage.ai.behaviortrees.*;

public class GetSmall extends BTAction {
    NPC npc;
    
    public GetSmall(NPC n) {
        npc = n;
    }

    @Override
    protected void onInitialize() {

    }

    @Override
    protected BTStatus update(float elapsedTime) {
        // Main logic goes here
        npc.getSmall();
        return BTStatus.BH_SUCCESS;
    }

    @Override
    protected void onTerminate(BTStatus status) {

    }
}