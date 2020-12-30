package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Arquitecto extends Agent {

    private MessageTemplate mt; // plantilla para recibir mensajes
    private AID[] electricistas;
    private AID[] plomeros;
    private AID[] albañiles;
    private double dinero = 10000;

    @Override
    protected void setup() {

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("arquitecto");
        sd.setName("constructora");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        this.activar();
    }

    public void activar() {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                DFAgentDescription templateA = new DFAgentDescription();
                ServiceDescription sdA = new ServiceDescription();
                sdA.setType("albañil");
                templateA.addServices(sdA);
                try {
                    DFAgentDescription[] resultA = DFService.search(myAgent, templateA);
                    albañiles = new AID[resultA.length];
                    for (int i = 0; i < resultA.length; ++i) {
                        albañiles[i] = resultA[i].getName();
                    }
                    addBehaviour(new ControllarAlbañiles());
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });
    }

    private class ControllarAlbañiles extends Behaviour {

        private int etapa = 0;

        @Override
        public void action() {
            switch (etapa) {
                case 0:
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < albañiles.length; ++i) {
                        cfp.addReceiver(albañiles[i]); //destinatarios
                    }
                    cfp.setContent("1000");// mensaje solo para verificar
                    cfp.setConversationId("contrato-albañil");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis());//codigo unico
                    myAgent.send(cfp);
                    // Prepara la plantilla para obtener propuestas
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("contrato-albañil"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    etapa = 1;
                    break;
                case 1:
                    ACLMessage respuesta = myAgent.receive(mt);
                    if (respuesta != null) {
                        // respuesta recibida
                        if (respuesta.getPerformative() == ACLMessage.PROPOSE) {
                            DFAgentDescription templateE = new DFAgentDescription();
                            ServiceDescription sdE = new ServiceDescription();
                            sdE.setType("electricista");
                            templateE.addServices(sdE);
                            try {
                                DFAgentDescription[] resultE = DFService.search(myAgent, templateE);
                                electricistas = new AID[resultE.length];
                                for (int i = 0; i < resultE.length; ++i) {
                                    electricistas[i] = resultE[i].getName();
                                }
                                addBehaviour(new ControllarElectricista());
                            } catch (FIPAException fe) {
                                fe.printStackTrace();
                            }
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    DFAgentDescription templateP = new DFAgentDescription();
                    ServiceDescription sdP = new ServiceDescription();
                    sdP.setType("plomero");
                    templateP.addServices(sdP);
                    try {
                        DFAgentDescription[] resultP = DFService.search(myAgent, templateP);
                        plomeros = new AID[resultP.length];
                        for (int i = 0; i < resultP.length; ++i) {
                            plomeros[i] = resultP[i].getName();
                        }
                        addBehaviour(new ControllarPlomero());
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }
                    etapa = 3;
                    break;
                case 3:
                    respuesta = myAgent.receive(mt);
                    if (respuesta != null) {
                        etapa = 4;
                    }
                    break;
            }
        }

        @Override
        public boolean done() {
            return etapa == 4;
        }
    }

    private class ControllarPlomero extends Behaviour {

        private int etapa = 0;
        private int numRespuestas = 0;
        private double mejorEficienciaElectricista;
        private AID mejorElectricista;

        @Override
        public void action() {

            switch (etapa) {
                case 0:
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < plomeros.length; ++i) {
                        cfp.addReceiver(plomeros[i]); //destinatarios
                    }
                    cfp.setContent("1000");// mensaje solo para verificar
                    cfp.setConversationId("contrato-plomero");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis());//codigo unico
                    myAgent.send(cfp);
                    // Prepara la plantilla para obtener propuestas
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("contrato-plomero"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    etapa = 1;
                    break;
                case 1:
                    // Recibe todas las propuestas / rechazos de los agentes del plomero.
                    ACLMessage respuesta = myAgent.receive(mt);
                    if (respuesta != null) {
                        // respuesta recibida
                        if (respuesta.getPerformative() == ACLMessage.PROPOSE) {
                            double precio = Double.parseDouble(respuesta.getContent());
                            if (mejorElectricista == null || precio < mejorEficienciaElectricista) {//guardamos el mejor precio
                                // mejor oferta
                                mejorEficienciaElectricista = precio;
                                mejorElectricista = respuesta.getSender();
                            }
                        }
                        numRespuestas++;
                        if (numRespuestas >= plomeros.length) {
                            // cuando recibimos todas las ofertas pasamos a la etapa 2
                            etapa = 2;
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    // Envíe la orden de contrato al plomero que proporcionó la mejor oferta.
                    ACLMessage ordenDeCompra = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    ordenDeCompra.addReceiver(mejorElectricista);
                    ordenDeCompra.setContent("contratado");
                    ordenDeCompra.setConversationId("contrato-plomero");
                    ordenDeCompra.setReplyWith("order" + System.currentTimeMillis());
                    myAgent.send(ordenDeCompra);
                    // Prepara la plantilla para obtener el Contrato respuesta
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("contrato-plomero"),
                            MessageTemplate.MatchInReplyTo(ordenDeCompra.getReplyWith()));
                    etapa = 3;
                    break;
                case 3:
                    // Recibe el contrato
                    respuesta = myAgent.receive(mt);
                    if (respuesta != null) {
                        System.out.println(respuesta.getConversationId());
                        if (respuesta.getPerformative() == ACLMessage.INFORM) {
                            // Contrato exitoso. Empieza a trabajar
                            System.out.println("Agente " + respuesta.getSender().getName() + " contratado");
                            System.out.println("Precio Total = " + mejorEficienciaElectricista);
                            dinero = dinero - mejorEficienciaElectricista;
                            etapa = 4;
                        }
                    } else {
                        block();
                    }
                    break;
            }
        }

        @Override
        public boolean done() {
            return etapa == 4;
        }
    }

    private class ControllarElectricista extends Behaviour {

        private int etapa = 0;
        private int numRespuestas = 0;
        private double mejorEficienciaElectricista;
        private AID mejorElectricista;

        @Override
        public void action() {

            switch (etapa) {
                case 0:
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < electricistas.length; ++i) {
                        cfp.addReceiver(electricistas[i]);//destinatarios
                    }
                    cfp.setContent("1000");// mensaje solo para verificar
                    cfp.setConversationId("contrato-electricista");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis());//codigo unico
                    myAgent.send(cfp);
                    // Prepara la plantilla para obtener propuestas
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("contrato-electricista"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    etapa = 1;
                    break;
                case 1:
                    // Recibe todas las propuestas / rechazos de los agentes del electricista.
                    ACLMessage respuesta = myAgent.receive(mt);
                    if (respuesta != null) {
                        // respuesta recibida
                        if (respuesta.getPerformative() == ACLMessage.PROPOSE) {
                            double precio = Double.parseDouble(respuesta.getContent());
                            if (mejorElectricista == null || precio < mejorEficienciaElectricista) {//guardamos el mejor precio
                                // mejor oferta
                                mejorEficienciaElectricista = precio;
                                mejorElectricista = respuesta.getSender();
                            }
                        }
                        numRespuestas++;
                        if (numRespuestas >= electricistas.length) {
                            // cuando recibimos todas las ofertas pasamos a la etapa 2
                            etapa = 2;
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    // Envíe la orden de contrato al electricista que proporcionó la mejor oferta.
                    ACLMessage ordenDeCompra = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    ordenDeCompra.addReceiver(mejorElectricista);
                    ordenDeCompra.setContent("contratado");
                    ordenDeCompra.setConversationId("contrato-electricista");
                    ordenDeCompra.setReplyWith("order" + System.currentTimeMillis());
                    myAgent.send(ordenDeCompra);
                    // Prepara la plantilla para obtener el Contrato respuesta
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("contrato-electricista"),
                            MessageTemplate.MatchInReplyTo(ordenDeCompra.getReplyWith()));
                    etapa = 3;
                    break;
                case 3:
                    // Recibe el contrato
                    respuesta = myAgent.receive(mt);
                    if (respuesta != null) {
                        System.out.println(respuesta.getConversationId());
                        if (respuesta.getPerformative() == ACLMessage.INFORM) {
                            // Contrato exitoso. Podemos terminar
                            System.out.println("Agente " + respuesta.getSender().getName() + " contratado");
                            System.out.println("Precio Total = " + mejorEficienciaElectricista);
                            dinero = dinero - mejorEficienciaElectricista;
                            etapa = 4;
                        }
                    } else {
                        block();
                    }
                    break;
            }
        }

        @Override
        public boolean done() {
            return etapa == 4;
        }
    }
}
