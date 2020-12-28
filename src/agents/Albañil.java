package agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

public class Albañil extends Agent {

    @Override
    protected void setup() {

    }

    public void active() {
        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
            }
        });
    }
}
