package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

public class Arquitecto extends Agent {

    @Override
    protected void setup() {
        System.out.println("hola");
        addBehaviour(new ControllarAlbañiles());
        addBehaviour(new DiseñarPlanos());
        addBehaviour(new ControllarPlomeria());
        addBehaviour(new ControllarElectrisista());
    }

    public void activar() {
//        addBehaviour(new ControllarAlbañiles());
    }

    private class DiseñarPlanos extends CyclicBehaviour {

        @Override
        public void action() {
            System.out.println("Estoy diseñando");
        }
    }

    private class ControllarAlbañiles extends CyclicBehaviour {

        @Override
        public void action() {
            System.out.println("Trabajen albañiles");
        }
    }
    
    private class ControllarPlomeria extends CyclicBehaviour {

        @Override
        public void action() {
        }
    }
    
    private class ControllarElectrisista extends CyclicBehaviour {

        @Override
        public void action() {
        }
    }

    @Override
    protected void takeDown() {
        System.out.println("Muere");
    }
}
