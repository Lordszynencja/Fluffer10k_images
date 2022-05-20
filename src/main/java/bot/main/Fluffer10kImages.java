package bot.main;

import static java.util.Arrays.asList;

import java.io.IOException;

import org.javacord.api.entity.intent.Intent;

import bot.main.images.ImageCommands;
import bot.main.serverCommands.ServerRoleCommands;
import bot.main.specialEvents.OnEntryHandler;
import bot.main.util.userData.UserDataImagesUtils;
import bot.util.apis.APIUtils;

public class Fluffer10kImages {
	@SuppressWarnings("unused")
	private static final String inviteUrl = "https://discord.com/api/oauth2/authorize?client_id=898969452006227988&permissions=277025442816&scope=bot%20applications.commands";

	public final APIUtils apiUtils;

	public final UserDataImagesUtils userDataImagesUtils;

	public final ImageCommands imageCommands;
	public final ServerRoleCommands serverRoleCommands;
	public final OnEntryHandler onEntryHandler;

	public Fluffer10kImages() throws IOException {
		apiUtils = new APIUtils("fluffer10kImages_config.txt", asList(Intent.GUILD_MEMBERS));

		userDataImagesUtils = new UserDataImagesUtils(this);

		imageCommands = new ImageCommands(this);
		serverRoleCommands = new ServerRoleCommands(apiUtils);
		onEntryHandler = new OnEntryHandler(apiUtils);

		apiUtils.endInit();
		apiUtils.messageUtils.sendMessageToMe("10k image Bot started");
	}
}
