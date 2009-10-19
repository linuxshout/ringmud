package ring.commands.interaction;

import ring.commands.Command;
import ring.commands.CommandParameters;
import ring.commands.CommandResult;
import ring.commands.CommandSender;
import ring.commands.CommandParameters.CommandType;
import ring.mobiles.Mobile;
import ring.world.World;

public class Emote implements Command {

	public CommandResult execute(CommandSender sender, CommandParameters params) {
		CommandResult res = new CommandResult();
		res.setFailText("Emote error.");
		params.init(CommandType.TEXT);

		String emoteText = params.paramString();

		// did they actually type something to emote?
		if (emoteText == null) {
			res.setFailText("[R][GREEN]What do you want to emote?[WHITE]");
			return res;
		}

		// so they did.
		Mobile mob = (Mobile) sender;
		emoteText = mob.getName() + " " + emoteText;

		// broadcast to the world and player.
		World.notifyPlayersAtLocation(mob, emoteText);
		res.setText(emoteText);
		res.setSuccessful(true);
		return res;
	}

	public String getCommandName() {
		return "emote";
	}

	public void rollback() {
		throw new UnsupportedOperationException();
	}

}