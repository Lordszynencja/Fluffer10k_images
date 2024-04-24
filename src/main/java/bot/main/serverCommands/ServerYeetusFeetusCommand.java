package bot.main.serverCommands;

import java.util.Set;

import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

import bot.util.apis.APIUtils;

public class ServerYeetusFeetusCommand {
	private static final String imageUrl = "https://tenor.com/view/punt-kick-baby-grandma-gif-8217719";

	private static void addHandlingForServer(final APIUtils apiUtils, final long serverId) {
		apiUtils.commandHandlers.addServerSlashCommandHandler(serverId, "yeetus_feetus",
				ServerYeetusFeetusCommand::handle);

		final SlashCommandBuilder scb = SlashCommand.with("yeetus_feetus", "Yeetus feetus!");

		final Server server = apiUtils.api.getServerById(serverId).get();
		final Set<SlashCommand> serverCommands = server.getSlashCommands().join();
		if (!serverCommands.stream().filter(
				cmd -> cmd.getApplicationId() == apiUtils.api.getClientId() && cmd.getName().equals("yeetus_feetus"))
				.findAny().isPresent()) {
			scb.createForServer(apiUtils.api.getServerById(serverId).get()).join();
		}
	}

	private static void handle(final SlashCommandInteraction interaction) {
		interaction.createImmediateResponder().setContent(imageUrl).respond();
	}

	public static void init(final APIUtils apiUtils) {
		final Server server = apiUtils.api.getServerById(886993994284945468L).orElse(null);
		if (server == null) {
			return;
		}

		final Set<SlashCommand> serverCommands = server.getSlashCommands().join();
		for (final SlashCommand cmd : serverCommands) {
			if (cmd.getName().equals("yeetus_feetus")) {
				cmd.delete();
			}
		}

		addHandlingForServer(apiUtils, 795397082956693546L);
	}
}
