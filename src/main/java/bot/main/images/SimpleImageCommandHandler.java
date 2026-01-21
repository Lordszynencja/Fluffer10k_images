package bot.main.images;

import java.io.IOException;
import java.util.List;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

import bot.main.util.userData.UserDataImages;
import bot.main.util.userData.UserDataImagesUtils;
import bot.util.FileUtils;
import bot.util.RandomUtils;
import bot.util.apis.APIUtils;
import bot.util.apis.CommandHandlers.SlashCommandHandler;
import bot.util.apis.MessageUtils;

public class SimpleImageCommandHandler implements SlashCommandHandler {
	private static String[] loadImageUrlsFromFileForCmd(final String dir, final String cmd, final boolean nsfw) {
		try {
			final String path = dir + "imgCommands/" + cmd.replace(" ", "_") + "/"
					+ (nsfw ? "linksNSFW.txt" : "links.txt");

			return FileUtils.readFileLines(path);
		} catch (final IOException e) {
			return new String[0];
		}
	}

	private final APIUtils apiUtils;
	private final MessageUtils messageUtils;
	private final UserDataImagesUtils userDataImagesUtils;

	private final String cmd;
	private final String answerWithoutParam;
	private final String answerWithParam;
	private final boolean isNSFW;

	private final String[] sfwLinks;
	private final String[] nsfwLinks;

	public SimpleImageCommandHandler(final APIUtils apiUtils, final MessageUtils messageUtils,
			final UserDataImagesUtils userDataImagesUtils, final String cmd, final String answerWithoutParam,
			final String answerWithParam, final boolean isNSFW, final String[] sfwLinks, final String[] nsfwLinks) {
		this.apiUtils = apiUtils;
		this.messageUtils = messageUtils;
		this.userDataImagesUtils = userDataImagesUtils;

		this.cmd = cmd;
		this.answerWithoutParam = answerWithoutParam;
		this.answerWithParam = answerWithParam;
		this.isNSFW = isNSFW;
		this.sfwLinks = sfwLinks;
		this.nsfwLinks = nsfwLinks == null || nsfwLinks.length == 0 ? sfwLinks : nsfwLinks;
	}

	public SimpleImageCommandHandler(final APIUtils apiUtils, final MessageUtils messageUtils,
			final UserDataImagesUtils userDataImagesUtils, final String imageDir, final String cmd,
			final String answerWithoutParam, final String answerWithParam, final boolean isNSFW, final boolean sfwBot) {
		this(apiUtils, messageUtils, userDataImagesUtils, cmd, answerWithoutParam, answerWithParam, isNSFW,
				isNSFW ? null : loadImageUrlsFromFileForCmd(imageDir, cmd, false),
				loadImageUrlsFromFileForCmd(imageDir, cmd, true && !sfwBot));
	}

	public SimpleImageCommandHandler(final APIUtils apiUtils, final MessageUtils messageUtils,
			final UserDataImagesUtils userDataImagesUtils, final String cmd, final String answerWithoutParam,
			final String answerWithParam, final boolean isNSFW, final String urlSFW, final String urlNSFW) {
		this(apiUtils, messageUtils, userDataImagesUtils, cmd, answerWithoutParam, answerWithParam, isNSFW,
				isNSFW ? null : new String[] { urlNSFW }, urlNSFW == null ? null : new String[] { urlNSFW });
	}

	public SimpleImageCommandHandler(final APIUtils apiUtils, final MessageUtils messageUtils,
			final UserDataImagesUtils userDataImagesUtils, final String cmd, final String answerWithoutParam,
			final String answerWithParam, final String url) {
		this(apiUtils, messageUtils, userDataImagesUtils, cmd, answerWithoutParam, answerWithParam, false, url, url);
	}

	private int addInteractionForMentions(final Long authorId, final List<Long> mentionIds) {
		int min = Integer.MAX_VALUE;
		for (final long userId : mentionIds) {
			final UserDataImages userData = userDataImagesUtils.getUserData(userId);
			final int amount = userData.addInteraction(cmd, authorId);
			if (amount < min) {
				min = amount;
			}
		}

		return min;
	}

	@Override
	public void handle(final SlashCommandInteraction interaction) {
		final boolean isNSFWChannel = MessageUtils.isNSFWChannel(interaction);
		if (!isNSFWChannel && isNSFW) {
			MessageUtils.sendEphemeralMessage(interaction, "This command cannot be used here");
			return;
		}

		final InteractionImmediateResponseBuilder responder = interaction.createImmediateResponder();

		final EmbedBuilder embed = new EmbedBuilder();
		final String userName = apiUtils.getUserName(interaction.getUser(), interaction.getServer().orElse(null));
		final String argument = interaction.getArgumentStringValueByIndex(0).orElse(null);
		if (argument != null) {
			if (answerWithParam != null) {
				embed.setDescription(String.format(answerWithParam, userName,
						messageUtils.replaceMentionsWithUserNames(argument, interaction.getServer().orElse(null))));
			}

			final List<Long> mentionIds = MessageUtils.getUserMentionIds(argument);
			if (!mentionIds.isEmpty()) {
				final int amount = addInteractionForMentions(interaction.getUser().getId(), mentionIds);
				final String footerText = "You did it " + (amount == 1 ? "1 time" : amount + " times");
				embed.setFooter(footerText);
			}

			responder.append(String.join(" ", MessageUtils.getMentions(argument)));
		} else {
			if (answerWithoutParam != null) {
				embed.setDescription(String.format(answerWithoutParam, userName));
			}
		}

		final String[] picsList = isNSFWChannel ? nsfwLinks : sfwLinks;
		if (picsList != null && picsList.length > 0) {
			final String imgUrl = RandomUtils.getRandom(picsList);
			embed.setImage(imgUrl);
		}

		responder.addEmbed(embed).respond();
	}
}