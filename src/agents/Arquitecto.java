package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class Arquitecto extends Agent {

    @Override
    protected void setup() {
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
//            System.out.println("Estoy diseñando");
        }
    }

    private class ControllarAlbañiles extends CyclicBehaviour {

        @Override
        public void action() {
//            System.out.println("Trabajen albañiles");
        }
    }

    private class ControllarPlomeria extends CyclicBehaviour {

        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null && msg.getContent().equals("Plomeria acabada")) {
                System.out.println("El Electrico acabo con su trabajo");
            } else {
                block();
            }
        }
    }

    private class ControllarElectrisista extends CyclicBehaviour {

        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null && msg.getContent().equals("Electricidad acabada")) {
                System.out.println("El Electrico acabo con su trabajo");
            } else {
                block();
            }
        }
    }

    @Override
    protected void takeDown() {
        System.out.println("Muere");
    }
}
