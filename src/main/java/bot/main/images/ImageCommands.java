package bot.main.images;

import java.io.IOException;
import java.util.List;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

import bot.main.Fluffer10kImages;
import bot.main.util.userData.UserDataImages;
import bot.util.FileUtils;
import bot.util.RandomUtils;
import bot.util.apis.CommandHandlers.SlashCommandHandler;
import bot.util.apis.MessageUtils;

public class ImageCommands {
	private final Fluffer10kImages fluffer;

	private String[] loadImageUrlsFromFileForCmd(final String cmd, final boolean nsfw) {
		try {
			final String path = fluffer.apiUtils.config.getString("imageFolderPath") + "imgCommands/"
					+ cmd.replace(" ", "_") + "/" + (nsfw ? "linksNSFW.txt" : "links.txt");

			return FileUtils.readFileLines(path);
		} catch (final IOException e) {
			return new String[0];
		}
	}

	private class SimpleImageCommandHandler implements SlashCommandHandler {
		private final String cmd;
		private final String answerWithoutParam;
		private final String answerWithParam;
		private final boolean isNSFW;

		private final String[] sfwLinks;
		private String[] nsfwLinks;

		public SimpleImageCommandHandler(final String cmd, final String answerWithoutParam,
				final String answerWithParam, final boolean isNSFW) {
			this.cmd = cmd;
			this.answerWithoutParam = answerWithoutParam;
			this.answerWithParam = answerWithParam;
			this.isNSFW = isNSFW;

			if (isNSFW) {
				sfwLinks = null;
				nsfwLinks = loadImageUrlsFromFileForCmd(cmd, true);
			} else {
				sfwLinks = loadImageUrlsFromFileForCmd(cmd, false);
				nsfwLinks = loadImageUrlsFromFileForCmd(cmd, true);

				if (nsfwLinks.length == 0) {
					nsfwLinks = sfwLinks;
				}
			}
		}

		public SimpleImageCommandHandler(final String cmd, final String answerWithoutParam,
				final String answerWithParam, final String urlSFW, final String urlNSFW, final boolean isNSFW) {
			this.cmd = cmd;
			this.answerWithoutParam = answerWithoutParam;
			this.answerWithParam = answerWithParam;
			this.isNSFW = isNSFW;

			if (isNSFW) {
				sfwLinks = null;
				nsfwLinks = new String[] { urlNSFW };
			} else {
				sfwLinks = new String[] { urlSFW };
				nsfwLinks = new String[] { urlNSFW };

				if (nsfwLinks[0] == null) {
					nsfwLinks = sfwLinks;
				}
			}
		}

		private int addInteractionForMentions(final Long authorId, final List<Long> mentionIds) {
			int min = Integer.MAX_VALUE;
			for (final long userId : mentionIds) {
				final UserDataImages userData = fluffer.userDataImagesUtils.getUserData(userId);
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
			final String userName = interaction.getUser().getDisplayName(interaction.getServer().get());
			final String argument = interaction.getOptionStringValueByIndex(0).orElse(null);
			if (argument != null) {
				if (answerWithParam != null) {
					embed.setDescription(String.format(answerWithParam, userName, fluffer.apiUtils.messageUtils
							.replaceMentionsWithUserNames(argument, interaction.getServer().get())));
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
			final String imgUrl = RandomUtils.getRandom(picsList);
			embed.setImage(imgUrl);

			responder.addEmbed(embed).respond();
		}
	}

	private void addImageCommand(final String cmd, final String description, final String answerWithoutParam,
			final String answerWithParam, final boolean isNSFW, final String... aliases) {
		final SlashCommandHandler handler = new SimpleImageCommandHandler(cmd, answerWithoutParam, answerWithParam,
				isNSFW);

		final SlashCommandBuilder scb = SlashCommand.with(cmd, description)//
				.addOption(SlashCommandOption.create(SlashCommandOptionType.STRING, "target", "target"));
		fluffer.apiUtils.commandHandlers.addSlashCommandHandler(cmd, handler, scb);
		for (final String alias : aliases) {
			final SlashCommandBuilder scbAlias = SlashCommand.with(alias, description)//
					.addOption(SlashCommandOption.create(SlashCommandOptionType.STRING, "target", "target"));
			fluffer.apiUtils.commandHandlers.addCommandAlias(cmd, alias, scbAlias);
		}
	}

	private void addTargetlessImageCommand(final String cmd, final String description, final String answerWithoutParam,
			final String answerWithParam, final String urlSFW, final String urlNSFW, final boolean isNSFW,
			final String... aliases) {
		final SlashCommandHandler handler = new SimpleImageCommandHandler(cmd, answerWithoutParam, answerWithParam,
				urlSFW, urlNSFW, isNSFW);

		final SlashCommandBuilder scb = SlashCommand.with(cmd, description);
		fluffer.apiUtils.commandHandlers.addSlashCommandHandler(cmd, handler, scb);
		for (final String alias : aliases) {
			final SlashCommandBuilder scbAlias = SlashCommand.with(alias, description);
			fluffer.apiUtils.commandHandlers.addCommandAlias(cmd, alias, scbAlias);
		}
	}

	public ImageCommands(final Fluffer10kImages fluffer) {
		this.fluffer = fluffer;

		if (fluffer.apiUtils.config.getString("imageFolderPath") == null) {
			throw new RuntimeException("imageFolderPath is missing!");
		}

		addImageCommand("angry_stare", "Stare at someone angrily", null, "%1$s stares angrily at %2$s", false);
		addImageCommand("assgrab", "Grab some ass", null, "%1$s grabs ass of %2$s", true);
		addImageCommand("birthday", "Wish someone happy birthday", null, "%1$s wishes %2$s happy birthday", false);
		addImageCommand("bite", "Bite someone", null, "%1$s bites %2$s", false);
		addImageCommand("blowjob", "Give someone pleasure", null, "%1$s sucks off %2$s", true, "suck");
		addImageCommand("bonk", "Bonk someone on the head", null, "%1$s bonks %2$s", false);
		addImageCommand("boobgrab", "Grab something soft", null, "%1$s grabs boobs of %2$s", true, "breastgrab",
				"grope", "titgrab");
		addImageCommand("boobhug", "Give someone happiness", null, "%1$s boobhugs %2$s", true);
		addImageCommand("boop", "Boop someone", null, "%1$s boops %2$s", false);
		addImageCommand("bootyshake", "Shake that ass", "%1$s shakes the booty", "%1$s shakes the booty for %2$s",
				true);
		addImageCommand("brazil", "Send someone to Brazil", "%1$s goes to Brazil", "%1$s sends %2$s to Brazil", false);
		addImageCommand("coil", "Coil around someone", null, "%1$s coils around %2$s", true);
		addImageCommand("cringe", "Criiinge", "%1$s cringes", "%1$s cringes at %2$s", false);
		addImageCommand("cuddle", "Cuddle with someone", null, "%1$s cuddles with %2$s", false);
		addImageCommand("dance", "Dance dance", "%1$s dances", "%1$s dances for %2$s", false);
		addImageCommand("drool", "heheeeee~", "%1$s drools", "%1$s drools over %2$s", false);
		addImageCommand("everyone", "Everyone!", null, null, false);
		addImageCommand("facesit", "Sit on someone's face", null, "%1$s sat on the face of %2$s", true);
		addImageCommand("fuck", "You can guess what this does~", null, "%1$s fucks with %2$s", true);
		addImageCommand("fuck_gif", "You can guess what this does~", null, "%1$s fucks with %2$s", true);
		addImageCommand("gay", "Ha! GAYYYYYYYYYYYY", null, null, false);
		addImageCommand("gibhug", "Demand a hug", "%1$s wants a hug", "%1$s wants to be hugged by %2$s", false);
		addImageCommand("gibpat", "Demand a headpat", "%1$s demands a pat", "%1$s demands to be pat by %2$s", false);
		addImageCommand("glomp", "Give someone a surprise hug!", null, "%1$s glomps %2$s", false);
		addImageCommand("good_night", "Sleep well", null, null, false);
		addImageCommand("handjob", "Pleasure someone", null, "%1$s gives a handjob to %2$s", true);
		addImageCommand("hehe", "Smile", null, null, false);
		addImageCommand("hora", "Hora hora~", null, null, false);
		addImageCommand("hug", "Hug someone", null, "%1$s hugs %2$s", false);
		addImageCommand("kiss", "Kiss someone", null, "%1$s kisses %2$s", false);
		addImageCommand("lap", "Lap pillow!", "%1$s lays on a lap pillow", "%1$s lets %2$s lay on their lap pillow",
				false);
		addImageCommand("lick", "Lick someone", null, "%1$s licks %2$s", false);
		addImageCommand("massage", "Massage someone", null, "%1$s massages %2$s", false);
		addImageCommand("no", "Say no", null, null, false);
		addImageCommand("nom", "Nom someone", "%1$s noms", "%1$s noms %2$s", false);
		addImageCommand("nuzzle", "Nuzzle someone", null, "%1$s nuzzles %2$s", false);
		addImageCommand("padoru", "Hashire sori yo, Kaze no you ni, Tsukimihara wo, PADORU PADORU", null, null, false);
		addImageCommand("pat", "Pat someone", null, "%1$s pats %2$s", false);
		addImageCommand("peck", "Peck someone", null, "%1$s pecked %2$s on the cheek", false);
		addImageCommand("peg", "Peg someone", null, "%1$s pegs %2$s", true);
		addImageCommand("pizza", "Give someone pizza", null, "%1$s gave pizza to %2$s", false);
		addImageCommand("poke", "Poke someone", null, "%1$s pokes %2$s", false);
		addImageCommand("pout", "Pout", "%1$s pouts", "%1$s pouts at %2$s", false);
		addImageCommand("shrug", "Shrug", "%1$s shrugs", "%1$s shrugs", false);
		addImageCommand("sip", "Drink some tea", "%1$s sips", "%1$s sips with %2$s", false, "drink");
		addImageCommand("sit", "Sit down", null, "%1$s sat on %2$s", false);
		addImageCommand("slap", "Slap someone", null, "%1$s slaps %2$s", false);
		addImageCommand("sleepcuddle", "Cuddle in bed with someone", null, "%1$s cuddles in bed with %2$s", false);
		addImageCommand("smoosh", "Smoosh someone's face", null, "%1$s smooshes %2$s", false);
		addImageCommand("smug", "Ara ara~", "%1$s smugs", "%1$s smugs at %2$s", false);
		addImageCommand("snuggle", "Snuggle with someone", null, "%1$s snuggles with %2$s", false);
		addImageCommand("space", "S P A A A C E", "%1$s floats in space", "%1$s floats in space with %2$s", false);
		addImageCommand("spank", "Spank someone", null, "%1$s spanks %2$s", true);
		addImageCommand("spoderman", "Spider-man would save the world but he's busy making memes", null, null, false);
		addImageCommand("spray", "Pshhhh", null, null, false);
		addImageCommand("stare", "Stare at someone", "*じーーー*", "*じーーー* %2$s", false);
		addImageCommand("step", "Step on someone", null, "%1$s steps on %2$s", true);
		addImageCommand("sus", "Something is sus", "%1$s thinks something is suspicious",
				"%1$s thinks %2$s is suspicious", false);
		addImageCommand("tickle", "Tickle someone", null, "%1$s tickles %2$s", false);
		addImageCommand("titfuck", "Milk someone with your milkers", null, "%1$s titfucks %2$s", true, "boobjob");
		addImageCommand("whip", "Whip someone", null, "%1$s whips %2$s", true);
		addImageCommand("yawn", "Yaaaawn", "%1$s yawns", "%1$s yawns", false);
		addImageCommand("yes", "Say yes", null, null, false);

		addTargetlessImageCommand("feelbonacci", "The feels don't stop", null, null,
				"https://cdn.discordapp.com/attachments/831093717376172032/831280567776706600/feelbonacci.jpg", null,
				false);
		addTargetlessImageCommand("feelsgood", "Mhmmmm~", null, null,
				"https://cdn.discordapp.com/attachments/831093717376172032/831280729404211250/feelsgood.png", null,
				false);
		addTargetlessImageCommand("gaskelly", "GAS GAS GAS", null, null,
				"https://media.discordapp.net/attachments/456149873507565568/585316940193595392/image0_2.gif?comment=DO_YOU_LIKE_MY_CAR?_GUESS_YOU%27RE_READY_CAUSE_IM_WAITING_FOR_YOU._IT%27S_GONNA_BE_EXCITING!_GOT_THIS_FEELING_REALLY_DEEP_IN_MY_SOUL._LETS_GET_OUT_I_WANNA_GO_COME_ALONG_GET_IT_ON._GONNA_TAKE_MY_CAR_GONNA_DRIVE_IT._GONNA_DRIVE_ALONE_TILL_I_GET_YOU_CAUSE_IM_CRAZY_HOT_AND_READY_BUT_YOULL_LIKE_IT._I_WANNA_RACE_FOR_YOU_SHALL_I_GO_NOW._GAS_GAS_GAS_IM_GONNA_STEP_ON_THE_GAS_TONIGHT_ILL_FLY_AND_BE_YOUR_LOVER._YEAH_YEAH_YEAH_ILL_BE_SO_QUICK_AS_A_FLASH_AND_ILL_BE_YOUR_HERO._GAS_GAS_GAS_IM_GONNA_RUN_AS_A_FLASH_TONIGHT_ILL_FIGHT_TO_BE_THE_WINNER_YEAH_YEAH_YEAH_IM_GONNA_STEP_ON_THE_GAS_AND_YOULL_SEE_THE_BIG_SHOW._DONT_BE_LAZY_CAUSE_IM_BURNING_FOR_YOU._ITS_LIKE_A_HOT_SENSATION_GOT_THIS_POWER_THAT_IS_TAKING_ME_OUT._YES_IVE_GOT_A_CRASH_ON_YOU_READY_NOW_READY_GO._GONNA_TAKE_MY_CAR_GONNA_DRIVE_IT._GONNA_DRIVE_ALONE_TILL_I_GET_YOU_CAUSE_IM_CRAZY_HOT_AND_READY_BUT_YOULL_LIKE_IT._I_WANNA_RACE_FOR_YOU_SHALL_I_GO_NOW_GAS_GAS_GAS_IM_GONNA_RUN_AS_A_FLASH_TONIGHT_ILL_FIGHT_TO_BE_THE_WINNER_YEAH_YEAH_YEAH_IM_GONNA_STEP_ON_THE_GAS_AND_YOULL_SEE_THE_BIG_SHOW._GAS_GAS_GAS_IM_GONNA_STEP_ON_THE_GAS_TONIGHT_ILL_FLY_AND_BE_YOUR_LOVER._YEAH_YEAH_YEAH_ILL_BE_SO_QUICK_AS_A_FLASH_AND_ILL_BE_YOUR_HERO._GAS_GAS_GAS_IM_GONNA_RUN_AS_A_FLASH_TONIGHT_ILL_FIGHT_TO_BE_THE_WINNER_YEAH_YEAH_YEAH_IM_GONNA_STEP_ON_THE_GAS_ANY_YOULL_SEE_THE_BIG_SHOW",
				null, false);
		addTargetlessImageCommand("n", "NNNNNNNNNNNNNNNNNNNN-", null, null,
				"https://cdn.discordapp.com/attachments/831093717376172032/831278978693857280/n.gif", null, false);
		addTargetlessImageCommand("out", "Show someone exit", null, null,
				"https://cdn.discordapp.com/attachments/831093717376172032/831280225617707058/out.jpg", null, false);
		addTargetlessImageCommand("respect", "Pay respects", null, null,
				"https://cdn.discordapp.com/attachments/831093717376172032/831280368782409798/f.gif", null, false);
		addTargetlessImageCommand("saved", "Saved", null, null,
				"https://cdn.discordapp.com/attachments/831093717376172032/831279675858223124/saved.jpg", null, false);
		addTargetlessImageCommand("vsauce", "Michael here. Or am I?", null, null,
				"https://cdn.discordapp.com/attachments/831093717376172032/831279875871735888/vsauce.png", null, false);
		addTargetlessImageCommand("whoping", "WHO PINGED ME?", null, null,
				"https://cdn.discordapp.com/attachments/397923444072644610/439953147738193920/image.gif", null, false);
		addTargetlessImageCommand("work", "Work work", null, null,
				"https://cdn.discordapp.com/attachments/831093717376172032/831451303489699850/work_work.jpg", null,
				false);
	}
}
