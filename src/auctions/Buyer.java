package auctions;

import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class Buyer extends Agent {

	private float money;
	private float moneyOnBid;
	private String favouriteType;

	@Override
	protected void setup() {
		Object[] args = getArguments();
		favouriteType = (String) args[0];
		money = (float) args[1];
		moneyOnBid = 0;

		SequentialBehaviour sb = new SequentialBehaviour(this);
		
		sb.addSubBehaviour(new FindAuctionBehaviour(this));
		sb.addSubBehaviour(new BiddingBehaviour(this));

		addBehaviour(sb);

		super.setup();
	}

	private class FindAuctionBehaviour extends OneShotBehaviour {

		public FindAuctionBehaviour(Buyer buyer) {
			super(buyer);
		}

		@Override
		public void action() {
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType(favouriteType);
			dfd.addServices(sd);

			try {
				Random generator = new Random();
				DFAgentDescription[] result = DFService.search(myAgent, dfd);
				// System.out.println(String.format("Found %d maching auctions",
				// result.length));
				if (result.length == 0) {
					myAgent.doDelete();

				} else {
					int index = generator.nextInt(result.length);
					AID auction = result[index].getName();

					ACLMessage bid = new ACLMessage(ACLMessage.REQUEST);
					bid.setContent(Float.toString(moneyOnBid));
					bid.addReceiver(auction);
					myAgent.send(bid);
				}
			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private class BiddingBehaviour extends Behaviour {

		private boolean wonOrFinished = false;

		public BiddingBehaviour(Buyer buyer) {
			super(buyer);
		}

		@Override
		public void action() {
			ACLMessage msg = myAgent.receive();

			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
					float overBid = Float.parseFloat(msg.getContent());
					// System.out.println("got overbidded!");
					if ((overBid + 2) < (money - moneyOnBid)) {
						ACLMessage response = new ACLMessage(ACLMessage.REQUEST);
						moneyOnBid += overBid + 2;
						response.addReceiver(msg.getSender());
						response.setContent(Float.toString(overBid + 2));
						myAgent.send(response);
						// System.out.println(String.format("Bidding %s with
						// %f", msg.getSender().getName(), moneyOnBid));
					} else {
						// System.out.println("Run out of money!");
					}

				} else if (msg.getPerformative() == ACLMessage.AGREE) {
					System.out.println("Hurray! I won auction: " + msg.getContent());
					money -= moneyOnBid;
					moneyOnBid = 0;
					wonOrFinished = true;

				} else if (msg.getPerformative() == ACLMessage.CANCEL) {
					System.out.println("I lost auction: " + msg.getContent());
					moneyOnBid = 0;
					wonOrFinished = true;
				}

			} else {
				block();
			}
		}

		@Override
		public boolean done() {
			return wonOrFinished;
		}
		
		@Override
		public int onEnd() {
			doDelete();
			return super.onEnd();
		}
	}
}
