package uk.co.thomasc.scrapbanktf.command;

public class Say extends Command {

	@Override
	public String run(CommandInfo cmdInfo) {
		return cmdInfo.getArgsStr();
	}

}
