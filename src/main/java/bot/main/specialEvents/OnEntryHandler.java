package bot.main.specialEvents;

import org.javacord.api.event.server.member.ServerMemberJoinEvent;

import bot.util.apis.APIUtils;

public class OnEntryHandler {
	private final APIUtils apiUtils;

	public OnEntryHandler(final APIUtils apiUtils) {
		this.apiUtils = apiUtils;
		apiUtils.api.addServerMemberJoinListener(this::handle);
	}

	private void handle(final ServerMemberJoinEvent event) {
		if (event.getServer().getId() == 886993994284945468L) {
			event.getUser().addRole(apiUtils.api.getRoleById(913862299104727102L).get());
		}
	}
}
