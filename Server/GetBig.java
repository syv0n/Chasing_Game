import tage.ai.behaviortrees.*;

public class GetBig extends BTAction {
    NPC npc;
    
    public GetBig(NPC n) {
        npc = n;
    }

    @Override
    protected void onInitialize() {
    }

    @Override
    protected BTStatus update(float elapsedTime) {
        npc.getBig();
        return BTStatus.BH_SUCCESS;
    }

    @Override
    protected void onTerminate(BTStatus status) {
        
    }
}