package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Albañil extends Agent {

    private double precioJornada;

    @Override
    protected void setup() {

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.precioJornada = Double.parseDouble((String) args[0]);
        } else {
            System.out.println("No tiene el precio de sus jornadas");
        }
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("albañil");
        sd.setName("contrato-albañil");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        this.Contratar();
    }

    public void Contratar() {
        addBehaviour(new BuscarContrato());
    }

    public class BuscarContrato extends OneShotBehaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CFP),
                    MessageTemplate.MatchConversationId("contrato-albañil"));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // cfp de Arquitecto recibido
                addBehaviour(new OneShotBehaviour() {
                    @Override
                    public void action() {
                        addBehaviour(new ContruirCimientos());
                    }
                });
            }
        }
    }

    public class ContruirCimientos extends OneShotBehaviour {

        @Override
        public void action() {
            System.out.println("Contruyendo cimientos");
            addBehaviour(new ContruirPilares());
        }
    }

    public class ContruirPilares extends OneShotBehaviour {

        @Override
        public void action() {
            System.out.println("Construyendo Pilares");
            addBehaviour(new ContruirParedes());
        }
    }

    public class ContruirParedes extends OneShotBehaviour {

        @Override
        public void action() {
            System.out.println("Contruyendo paredes");
            addBehaviour(new ContruirTejado());
        }
    }

    public class ContruirTejado extends Behaviour {

        private boolean finish = false;
        private int etapa = 0;

        @Override
        public void action() {

            switch (etapa) {
                case 0:
                    System.out.println("Construyendo Tejados");

                    ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                    msg.setContent("Obra gruesa terminada");
                    msg.addReceiver(new AID("arquitecto", AID.ISLOCALNAME));
                    send(msg);
                    etapa = 1;
                    break;
                case 1:
                    MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CFP),
                            MessageTemplate.MatchConversationId("contrato-albañil"));
                    ACLMessage msgRecive = myAgent.receive(mt);
                    if (msgRecive != null) {
                        addBehaviour(new PonderMarcosYVidrios());
                        finish = true;
                    } else {
                        block();
                    }
                    break;
            }
        }

        @Override
        public boolean done() {
            return finish;
        }
    }

    public class PonderMarcosYVidrios extends OneShotBehaviour {

        @Override
        public void action() {
            System.out.println("Colocando Marcos de las puertas y vidrios");
            addBehaviour(new PonderCeramica());
        }
    }

    public class PonderCeramica extends OneShotBehaviour {

        @Override
        public void action() {
            System.out.println("Colocando ceramicas en los pisos");
            addBehaviour(new EnyesarParedes());
        }
    }

    public class EnyesarParedes extends OneShotBehaviour {

        @Override
        public void action() {
            System.out.println("Colocando ceramicas en los pisos");
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
            msg.createReply();
            msg.setContent("Obra fina terminada");
            msg.addReceiver(new AID("arquitecto", AID.ISLOCALNAME));
            send(msg);
        }
    }
}
