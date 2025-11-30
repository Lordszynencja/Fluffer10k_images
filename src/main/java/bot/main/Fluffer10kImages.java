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

	private static final String inviteUrl = "https://discord.com/api/oauth2/authorize?client_id=898969452006227988&permissions=277025442816&scope=bot%20applications.commands";
	private static final String inviteUrlSFW = "https://discord.com/api/oauth2/authorize?client_id=1214190714213113926&permissions=277025442816&scope=bot%20applications.commands";

	public final APIUtils apiUtils;
	public final APIUtils apiUtilsSFW;

	public final UserDataImagesUtils userDataImagesUtils;

	private final ImageCommands imageCommands;

	public Fluffer10kImages() throws IOException {
		apiUtils = new APIUtils("Fluffer 10k Images", inviteUrl, "fluffer10kImages_config.txt",
				asList(Intent.GUILD_MEMBERS));

		try {
			userDataImagesUtils = new UserDataImagesUtils(this);

			imageCommands = new ImageCommands(userDataImagesUtils, apiUtils.config.getString("imageFolderPath"));
			imageCommands.init(apiUtils, false);

			ServerRoleCommands.init(apiUtils);
			new OnEntryHandler(apiUtils);

			apiUtils.endInit();
		} catch (final Exception e) {
			apiUtils.messageUtils.sendExceptionToMe(e);

			throw e;
		}

		apiUtilsSFW = new APIUtils("Fluffer 10k Images SFW", inviteUrlSFW, "fluffer10kImagesSFW_config.txt",
				asList(Intent.GUILD_MEMBERS));
		try {
			imageCommands.init(apiUtilsSFW, true);
			apiUtilsSFW.endInit();
		} catch (final Exception e) {
			apiUtilsSFW.messageUtils.sendExceptionToMe(e);

			throw e;
		}
	}
}
