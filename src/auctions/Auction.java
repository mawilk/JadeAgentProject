package auctions;

import java.util.HashSet;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class Auction extends Agent {

	private int maxTicks;
	private int currentTicks;
	private ACLMessage bestOffer = null;

	protected void setup() {

		Object[] args = getArguments();
		float initPrice = (float) args[0];
		String type = (String) args[1];
		this.maxTicks = 300;

		// System.out.println(String.format("Auction of type %s created: %s",
		// type, getAID()));

		registerDFA(type);

		addBehaviour(new AuctionTimeTicker(this, 50));
		addBehaviour(new AuctionBehaviour(this, initPrice));
	}

	private void registerDFA(String type) {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());

		ServiceDescription sd = new ServiceDescription();
		sd.setType(type);
		sd.setName(getLocalName());

		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

	private void resetTimer() {
		currentTicks = maxTicks / 2;
	}

	protected void takeDown() {
		try {
			DFService.deregister(this);
		} catch (Exception e) {
		}
	}

	private class AuctionTimeTicker extends TickerBehaviour {

		public AuctionTimeTicker(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			if (currentTicks >= maxTicks) {
				ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
				msg.addReceiver(getAID());
				myAgent.send(msg);
				stop();
			} else {
				currentTicks += 1;
//				if(currentTicks % 10 == 0) System.out.println("active " + myAgent.getName());
			}
		}
	}

	private class AuctionBehaviour extends Behaviour {

		private float currentOffer;
		private HashSet<AID> bidders;
		private boolean finished = false;

		public AuctionBehaviour(Agent agent, float initPrice) {
			super(agent);
			this.currentOffer = initPrice;
			this.bidders = new HashSet<AID>();
		}

		@Override
		public void action() {
			ACLMessage msg = myAgent.receive();

			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.REQUEST) {
//					System.out.println("bid " + myAgent.getName());
					AID sender = msg.getSender();
					bidders.add(sender);
					resetTimer();
					float offer = Float.parseFloat(msg.getContent());
//					System.out.println(String.format("Got offer %f with current %f", offer, currentOffer));
					if (offer > currentOffer) {
						currentOffer = offer;
						bestOffer = msg;

						for (AID aid : bidders) {
							if (aid != sender) {
								sendOverBid(aid, currentOffer);
							}
						}
					} else {
						sendOverBid(sender, currentOffer);
					}
				} else if (msg.getPerformative() == ACLMessage.CONFIRM) {
					finished = true;
				}
			} else {
				block();
			}
		}

		@Override
		public boolean done() {
			return finished || currentTicks >= maxTicks;
		}

		@Override
		public int onEnd() {
			if (bestOffer != null) {
				ACLMessage wonAuctionMsg = new ACLMessage(ACLMessage.AGREE);
				AID bestOfferSender = bestOffer.getSender();
				wonAuctionMsg.setContent(myAgent.getName());
				wonAuctionMsg.addReceiver(bestOfferSender);
				myAgent.send(wonAuctionMsg);

				for (AID aid : bidders) {
					if (aid != bestOffer.getSender()) {
						ACLMessage cancelMsg = new ACLMessage(ACLMessage.CANCEL);
						cancelMsg.addReceiver(bestOfferSender);
						cancelMsg.setContent(myAgent.getName());
						myAgent.send(cancelMsg);
					}
				}
			} else {
				System.out.println("Nobody wanted: " + getAgent().getName());
			}
			
			myAgent.doDelete();
			return super.onEnd();
		}

		private void sendOverBid(AID buyer, float offer) {
			ACLMessage overBid = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
			overBid.setContent(Float.toString(offer));
			overBid.addReceiver(buyer);
			myAgent.send(overBid);
//			System.out.println("Overbid has been sent to " + buyer.getName());
		}
	}
}
