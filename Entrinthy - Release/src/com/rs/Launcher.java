package com.rs;

import java.util.concurrent.TimeUnit;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.cores.CoresManager;
import com.rs.game.Region;
import com.rs.game.RegionBuilder;
import com.rs.game.World;
import com.rs.game.npc.combat.CombatScriptsHandler;
import com.rs.game.player.Player;
import com.rs.game.player.content.FishingSpotsHandler;
import com.rs.game.player.content.FriendChatsManager;
import com.rs.game.player.controlers.ControlerHandler;
import com.rs.game.player.cutscenes.CutscenesHandler;
import com.rs.game.player.dialogues.DialogueHandler;
import com.rs.game.worldlist.WorldList;
import com.rs.net.ServerChannelHandler;
import com.rs.utils.DTRank;
import com.rs.utils.EconomyPrices;
import com.rs.utils.IPBanL;
import com.rs.utils.ItemBonuses;
import com.rs.utils.ItemExamines;
import com.rs.utils.Logger;
import com.rs.utils.MapAreas;
import com.rs.utils.NPCBonuses;
import com.rs.utils.NPCCombatDefinitionsL;
import com.rs.utils.NPCDrops;
import com.rs.utils.NPCSpawning;
import com.rs.utils.NPCSpawns;
import com.rs.utils.ObjectSpawns;
import com.rs.utils.PkRank;
import com.rs.game.player.content.Shop;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.ShopsHandler;
import com.rs.utils.Text;
import com.rs.utils.Utils;
import com.rs.utils.huffman.Huffman;
import com.rs.cache.loaders.ItemsEquipIds;
import com.rs.utils.DisplayNames;
import com.rs.utils.MapArchiveKeys;
import com.rs.utils.MusicHints;
import com.rs.Panel;

public final class Launcher {

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("USE: guimode(boolean) debug(boolean) hosted(boolean)");
		    return;
		}
		Settings.HOSTED = Boolean.parseBoolean(args[2]);
		Settings.DEBUG = Boolean.parseBoolean(args[1]);
		long currentTime = Utils.currentTimeMillis();
		if (Settings.HOSTED) {
		}
		Logger.log("Launcher",
				"Running Server...");
		Cache.init();
		ItemsEquipIds.init();
		Huffman.init();
		EconomyPrices.init();
		WorldList.init();
		ObjectSpawns.init();		
		NPCCombatDefinitionsL.init();
		NPCDrops.init();
		NPCSpawns.init();
		Text.init();
		ItemExamines.init();
		ItemBonuses.init();
		DisplayNames.init();
		IPBanL.init();
		PkRank.init();
		DTRank.init();
		MapArchiveKeys.init();
		MapAreas.init();
		NPCSpawns.init();
		NPCCombatDefinitionsL.init();
		NPCBonuses.init();
		ItemExamines.init();
		ItemBonuses.init();
		MusicHints.init();
		ShopsHandler.init();
		FishingSpotsHandler.init();
		CombatScriptsHandler.init();
		DialogueHandler.init();
		ControlerHandler.init();
		CutscenesHandler.init();
		FriendChatsManager.init();
		CoresManager.init();
		World.init();
		RegionBuilder.init();		RegionBuilder.init();
		//Logger.log("Launcher", "Initing Auto Donation...");
		//DonationManager.createConnection();
		Panel frame = new Panel();
		frame.setVisible(true);
		try {
			ServerChannelHandler.init();
			NPCSpawning.spawnNPCS();
		} catch (Throwable e) {
			Logger.handle(e);
			Logger.log("Launcher",
					"Failed initing Server Channel Handler. Shutting down...");
			System.exit(1);
			return;
		}
		Logger.log("Launcher", "Server took "
				+ (Utils.currentTimeMillis() - currentTime)
				+ " milli seconds to launch.");
		addAccountsSavingTask();
		if (Settings.HOSTED)
		addCleanMemoryTask();
	}

	private static void addCleanMemoryTask() {
		CoresManager.slowExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					cleanMemory(Runtime.getRuntime().freeMemory() < Settings.MIN_FREE_MEM_ALLOWED);
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 0, 10, TimeUnit.MINUTES);
	}

	private static void addAccountsSavingTask() {
		CoresManager.slowExecutor.scheduleWithFixedDelay(new Runnable() {
		@Override
		public void run() {
		try {
		saveFiles();
		} catch (Throwable e) {
		Logger.handle(e);
		}

		}
		}, 5, 30, TimeUnit.SECONDS);
		}

	public static void saveFiles() {
		for (Player player : World.getPlayers()) {
			if (player == null || !player.hasStarted() || player.hasFinished())
				continue;
			SerializableFilesManager.savePlayer(player);
		}
		DisplayNames.save();
		IPBanL.save();
		PkRank.save();
		DTRank.save();
	}

	public static void cleanMemory(boolean force) {
		if (force) {
			ItemDefinitions.clearItemsDefinitions();
			NPCDefinitions.clearNPCDefinitions();
			ObjectDefinitions.clearObjectDefinitions();
			for (Region region : World.getRegions().values())
				region.removeMapFromMemory();
		}
	}

	public static void shutdown() {
		try {
			closeServices();
		} finally {
			System.exit(0);
		}
	}

	public static void closeServices() {
		ServerChannelHandler.shutdown();
		CoresManager.shutdown();
		if (Settings.HOSTED) {
			try {
				//setWebsitePlayersOnline(0);
			} catch (Throwable e) {
				Logger.handle(e);
			}
		}
	}

	public static void restart() {
		closeServices();
		System.gc();
		try {
			Runtime.getRuntime().exec("java -server -Xms2048m -Xmx20000m -cp bin;/data/libs/netty-3.2.6.Final.jar;/data/libs/FileStore.jar Launcher false false true false");
			System.exit(0);
		} catch (Throwable e) {
			Logger.handle(e);
		}

	}

	private Launcher() {

	}

	public static Object getDBC() {
		// TODO Auto-generated method stub
		return null;
	}

}
