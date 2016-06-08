package auctions;

import java.io.IOException;
import java.util.Random;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;

public class Market extends Agent {

	@Override
	protected void setup() {
		createSellers(5, 10);
		createBuyers(500);
		doDelete();
	}
	
	private void createSellers(int n, int auctionsNum) {
		try {
			PlatformController container = getContainerController();
			for (int i = 0; i < n; i++) {
				String name = "seller" + i;
				AgentController seller;
				seller = container.createNewAgent(name, "auctions.Seller", new Object[] {
						i * auctionsNum, i * auctionsNum + (auctionsNum - 1)
				});
				seller.start();
			}
		} catch (ControllerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createBuyers(int n) {
		try {
			Random generator = new Random();
			PlatformController container = getContainerController();
			for (int i = 0; i < n; i++) {
				String name = "buyer" + i;
				AgentController buyer;
				buyer = container.createNewAgent(name, "auctions.Buyer", new Object[] {
						Seller.itemTypes.get(generator.nextInt(Seller.itemTypes.size())), generator.nextFloat() * 10000000 + 1000
				});
				buyer.start();
			}
		} catch (ControllerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
