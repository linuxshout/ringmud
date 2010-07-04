package ring.commands.movement;

import ring.commands.Command;
import ring.commands.CommandArguments;
import ring.commands.CommandResult;
import ring.commands.CommandSender;
import ring.mobiles.Mobile;
import ring.movement.LocationManager;
import ring.movement.Portal;
import ring.movement.PortalNotFoundException;

public class South implements Command {

	public void execute(CommandSender sender, CommandArguments params) {
		CommandResult res = new CommandResult();
		res.setFailText("[GREEN]You can't go that way.[WHITE]");
		Mobile mob = (Mobile) sender;
		Portal destination;
		
		try {
			destination = LocationManager.getPortal(mob.getLocation(), LocationManager.SOUTH);
			LocationManager.move(mob, destination);
		}
		catch (PortalNotFoundException e) {
			res.setSuccessful(false);
			res.setReturnableData(true);
			res.send();
		}
	}

	public String getCommandName() {
		return "south";
	}

	public void rollback() {

	}

	@Override
	public void setCommandName(String name) {
		// TODO Auto-generated method stub
		
	}

}
