package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Plomero extends Agent {

    private int jornadas;
    private double precioJornada;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.jornadas = Integer.parseInt((String) args[0]);
            this.precioJornada = Double.parseDouble((String) args[1]);
        } else {
            System.out.println("No tiene sus jornadas");
        }
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("plomero");
        sd.setName("contrato-plomero");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        this.activar();
    }

    public void activar() {
        addBehaviour(new OfertarContrato());
        addBehaviour(new AceptarContrato());
    }

    private class OfertarContrato extends CyclicBehaviour {

        @Override
        public void action() {
//            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CFP),
                    MessageTemplate.MatchConversationId("contrato-plomero"));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // cfp de Arquitecto recibido
                ACLMessage respuesta = msg.createReply();
                respuesta.setPerformative(ACLMessage.PROPOSE);
                respuesta.setContent(String.valueOf(precioJornada * jornadas));
                respuesta.setConversationId(msg.getConversationId());
                myAgent.send(respuesta);
            } else {
                block();
            }
        }
    }

    private class AceptarContrato extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null && msg.getContent() != null) {
                //mensaje de contrato recibido
                ACLMessage respuesta = msg.createReply();
                respuesta.setPerformative(ACLMessage.INFORM);
                myAgent.send(respuesta);
                addBehaviour(new TickerBehaviour(myAgent, 500) {
                    private int jornadaActual = 1;

                    @Override
                    protected void onTick() {
                        if (jornadaActual == jornadas) {
                            System.out.println("Trabajo de plomeria Finalizado");
                            doDelete();
                        } else {
                            jornadaActual++;
                        }
                    }
                });
            } else {
                block();
            }
        }
    }
}
