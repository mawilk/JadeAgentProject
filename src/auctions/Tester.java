package auctions;

import java.io.FileWriter;
import java.io.IOException;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;

public class Tester extends Agent {

	private static final String DELIMITER = ";";
	private static final String NEW_LINE = "\n";
	private FileWriter fileWriter;
	
	private final int N = 1000;
	private final int incr = 1000;
	private final int maxN = 50000;
	private int count = 0;
	private int currentN = N; 
	private int waitToSetup = 300;

	private long start, end;

	protected void setup() {
		
		try {
			fileWriter = new FileWriter("test2.txt");
			
			System.out.println(getLocalName() + " is up");
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			DFService.register(this, dfd);

			addBehaviour(new CyclicBehaviour(this) {

				@Override
				public void action() {
					ACLMessage msg = receive();

					if (msg != null) {
						if (msg.getPerformative() == ACLMessage.CONFIRM) {
							count += 1;
							// System.out.println(count + " received");
							if (count == currentN) {
								end = System.nanoTime();
								count = 0;
								
								try {
									fileWriter.append(String.valueOf(currentN));
									fileWriter.append(DELIMITER);
									fileWriter.append(String.valueOf((double) (end - start) / 1000000000.0));
									fileWriter.append(NEW_LINE);
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								
								System.out.println(currentN + " " + (double) (end - start) / 1000000000.0);
								if(currentN == maxN) {
									terminate();
								} else {
									currentN += incr;
									createAuctions(currentN);
									try {
										Thread.sleep(waitToSetup);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									start = System.nanoTime();
									sendRequests(currentN);
								}
							}
						}
					} else {
						block();
					}

				}
			});

			createAuctions(currentN);
			Thread.sleep(waitToSetup);
			start = System.nanoTime();
			sendRequests(currentN);

		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createAuctions(int n) {
		try {
			PlatformController container = getContainerController();
			for (int i = 0; i < n; i++) {
				String name = "a" + (i + currentN);
				AgentController auction;
				auction = container.createNewAgent(name, "auctions.Auction", null);
				auction.start();
			}
		} catch (ControllerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void sendRequests(int n) {
		start = System.nanoTime();
		for (int i = 0; i < n; i++) {
			String name = "a" + (i + currentN);
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(new AID(name, AID.ISLOCALNAME));
			send(msg);
		}
	}
	
	protected void terminate() {
        try {
        	fileWriter.flush();
			fileWriter.close();
			DFService.deregister(this);
			doDelete();
		} catch (FIPAException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
