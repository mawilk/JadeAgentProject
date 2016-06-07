package auctions;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class Auction extends Agent {

	protected void setup() {
//		System.out.println(getLocalName() + " is up");
		ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
		msg.addReceiver(new AID("tester", AID.ISLOCALNAME));
		send(msg);
		try {
			Thread.sleep(30);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		doDelete();
	}
}
