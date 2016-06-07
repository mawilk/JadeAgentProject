package auctions;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class Auction extends Agent {

	protected void setup() {
		addBehaviour(new CyclicBehaviour(this) {

			@Override
			public void action() {
				ACLMessage msg = receive();

				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.REQUEST) {
						ACLMessage response = new ACLMessage(ACLMessage.CONFIRM);
						response.addReceiver(new AID("tester", AID.ISLOCALNAME));
						send(response);
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						doDelete();
					}
				} else {
					block();
				}
			}
		});
	}
}
