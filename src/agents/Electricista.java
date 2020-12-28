package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class Electricista extends Agent {
    private int jornadasAsignadas;
    private int jornadaActual = 1;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.jornadasAsignadas = (int) args[0];
        } else {
            System.out.println("No tiene sus jornadas asignadas asignado");
        }
    }

    public void activar() {
        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            public void onTick() {
                if(jornadaActual == jornadasAsignadas){
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setContent("Electrisidad acabada");
                    msg.addReceiver(new AID("arquitecto", AID.ISLOCALNAME));
                    send(msg);
                    block();
                }else{
                    jornadaActual++;
                }
            }
        });
    }
}
