package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class Plomero extends Agent {

    private int jornadasAsignadas;
    private int jornadaActual = 1;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.jornadasAsignadas = Integer.parseInt((String) args[0]);
        } else {
            System.out.println("No tiene sus jornadas asignadas asignado");
        }
        this.activar();
    }

    public void activar() {
        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            public void onTick() {
                if (jornadaActual == jornadasAsignadas) {
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setContent("Plomeria acabada");
                    msg.addReceiver(new AID("arquitecto", AID.ISLOCALNAME));
                    send(msg);
                    System.out.println("Ya acabe mi trabajo plomero");
                    this.stop();
                } else {
                    System.out.println(jornadaActual);
                    jornadaActual++;
                }
            }
        });
    }
}
