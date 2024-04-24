package bot.main.serverCommands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;

import bot.util.apis.APIUtils;
import bot.util.apis.MessageUtils;

public class ServerRoleCommands {
	private static final Map<Long, Set<Long>> serverAvailableRoles = new HashMap<>();

	private static void addRoles(final long serverId, final long... roleIds) {
		final Set<Long> roleIdsSet = new HashSet<>();
		for (final long roleId : roleIds) {
			roleIdsSet.add(roleId);
		}
		serverAvailableRoles.put(serverId, roleIdsSet);
	}

	private static void addHandlingForServer(final APIUtils apiUtils, final long serverId, final long... roleIds) {
		final Server server = apiUtils.api.getServerById(serverId).orElse(null);
		if (server == null) {
			return;
		}

		addRoles(serverId, roleIds);

		apiUtils.commandHandlers.addServerSlashCommandHandler(serverId, "toggle_role", ServerRoleCommands::handle);

		final SlashCommandBuilder scb = SlashCommand.with("toggle_role", "Add/remove a role")//
				.addOption(SlashCommandOption.create(SlashCommandOptionType.ROLE, "role",
						"Role that you want to add/remove", true));
		final Set<SlashCommand> serverCommands = server.getSlashCommands().join();
		if (!serverCommands.stream().filter(
				cmd -> cmd.getApplicationId() == apiUtils.api.getClientId() && cmd.getName().equals("toggle_role"))
				.findAny().isPresent()) {
			scb.createForServer(apiUtils.api.getServerById(serverId).get()).join();
		}
	}

	private static void handle(final SlashCommandInteraction interaction) {
		final Role role = interaction.getArgumentRoleValueByName("role").get();
		if (!serverAvailableRoles.get(interaction.getServer().get().getId()).contains(role.getId())) {
			MessageUtils.sendEphemeralMessage(interaction, "You can't change this role");
			return;
		}

		final User user = interaction.getUser();

		if (user.getRoles(interaction.getServer().get()).contains(role)) {
			user.removeRole(role, "Removed by user using the bot").join();
			MessageUtils.sendEphemeralMessage(interaction, "Role " + role.getMentionTag() + " has been removed");
		} else {
			user.addRole(role, "Added by user using the bot").join();
			MessageUtils.sendEphemeralMessage(interaction, "Role " + role.getMentionTag() + " has been added");
		}
	}

	public static void init(final APIUtils apiUtils) {
		addHandlingForServer(apiUtils, 886993994284945468L, // Kennecs Frenland
				913777223541071962L, // dominant
				913777137830465557L, // submissive
				913777294873624606L, // pred
				913777379330101280L, // switch
				913777440516608001L, // prey
				913777606250344518L, // horny
				913777685434613761L, // vorny
				913777832054886410L, // non-vore
				913777928662298665L, // wholesome
				913777993271345162L, // female
				913778002305904650L, // male
				913778012242206760L, // non-binary
				913778247412625460L, // drawer
				913778345395765249L, // writer
				913778400596987965L, // animator
				933875630339162193L // anarchist
		);
	}
}
