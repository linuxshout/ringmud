package ring.commands.admin;

import ring.commands.Command;
import ring.commands.CommandArguments;
import ring.commands.CommandResult;
import ring.commands.CommandSender;
import ring.world.Ticker;

public class TickerList extends AbstractAdminCommand implements Command {

	public void execute(CommandSender sender, CommandArguments params) {
		CommandResult res = new CommandResult();
		
		if (super.isAccessAllowed(sender)) {
			String resText = "";
	
			Ticker ticker = Ticker.getTicker();
			resText = "[R][WHITE]Ticker Information for **WORLD TICKER**:\n";
			resText += ticker.tickerList();
	
			res.setText(resText);
			res.setSuccessful(true);
		}
		
		res.send();
	}

	public String getCommandName() {
		return "tickerlist";
	}

	public void rollback() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCommandName(String name) {
		// TODO Auto-generated method stub
		
	}

}
