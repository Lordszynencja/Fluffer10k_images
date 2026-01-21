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
	private static UserDataImagesUtils userDataImagesUtils = null;

	public final APIUtils apiUtils;

	private final ImageCommands imageCommands;

	public Fluffer10kImages(final boolean sfw) throws IOException {
		final String name = "Fluffer 10k Images" + (sfw ? " SFW" : "");
		final String configFileName = "fluffer10kImages" + (sfw ? "SFW" : "") + "_config.txt";

		apiUtils = new APIUtils(name, configFileName, asList(Intent.GUILD_MEMBERS));

		try {
			if (userDataImagesUtils == null) {
				userDataImagesUtils = new UserDataImagesUtils(this);
			}

			imageCommands = new ImageCommands(userDataImagesUtils, apiUtils.config.getString("imageFolderPath"), sfw);
			imageCommands.init(apiUtils, sfw);

			if (!sfw) {
				ServerRoleCommands.init(apiUtils);
				new OnEntryHandler(apiUtils);
			}

			apiUtils.endInit();
		} catch (final Exception e) {
			apiUtils.messageUtils.sendExceptionToMe(e);
			throw e;
		}
	}
}
