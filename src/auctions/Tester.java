package auctions;

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

	private final int N = 1000;
	private final int incr = 1000;
	private final int maxN = 80000;
	private int count = 0;
	private int currentN = N; 

	private long start, end;

	protected void setup() {
		try {
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
								System.out.println(currentN + " " + (double) (end - start) / 1000000000.0);
								if(currentN == maxN) {
									terminate();
								} else {
									currentN += incr;
									createAuctions(currentN);
								}
							}
						}
					} else {
						block();
					}

				}
			});

			createAuctions(N);

		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createAuctions(int n) {
		try {
			PlatformController container = getContainerController();
			start = System.nanoTime();
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

	protected void terminate() {
		try {
			DFService.deregister(this);
			doDelete();
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
