package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

public class Albañil extends Agent {

    @Override
    protected void setup() {
        this.Contratar();
    }

    public void Contratar() {
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                addBehaviour(new ContruirCimientos());
            }
        });
    }

    public class ContruirCimientos extends CyclicBehaviour {

        @Override
        public void action() {
            System.out.println("Contruyendo cimientos");
        }
    }

    public class ContruirPilares extends CyclicBehaviour {

        @Override
        public void action() {
        }
    }

    public class ContruirParedes extends CyclicBehaviour {

        @Override
        public void action() {
        }
    }

    public class ContruirTejado extends CyclicBehaviour {

        @Override
        public void action() {
        }
    }
    
    
}
