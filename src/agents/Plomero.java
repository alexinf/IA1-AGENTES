package agents;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class Plomero extends Agent {

    @Override
    protected void setup() {
        
    }
    
    public void activar(){
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
            }
        });
    }
}
