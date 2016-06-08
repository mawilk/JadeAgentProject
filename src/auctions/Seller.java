package auctions;

import java.util.ArrayList;
import java.util.Random;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;

public class Seller extends Agent {

	public static ArrayList<String> itemTypes = new ArrayList<String>() {
		{
			add("Telewizor");
			add("Laptop");
			add("Smartfon");
			add("Pralka");
		}
	};

	@Override
	protected void setup() {
		Object[] args = getArguments();
		int startIndex = (int) args[0];
		int endIndex = (int) args[1];

		Random generator = new Random();

		PlatformController container = getContainerController();
		for (int i = startIndex; i < endIndex; i++) {
			String name = "a" + i;
			AgentController auction;
			try {
				auction = container.createNewAgent(name, "auctions.Auction",
						new Object[] { generator.nextFloat() * 100, itemTypes.get(generator.nextInt(itemTypes.size())) });
				auction.start();

			} catch (ControllerException e) {
				e.printStackTrace();
			}
		}
		
		super.setup();
		doDelete();
	}
}
