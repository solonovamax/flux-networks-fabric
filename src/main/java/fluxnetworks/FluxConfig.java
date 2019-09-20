package fluxnetworks;

import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class FluxConfig {

    public static Configuration config;

    public static final String GENERAL = "general";
    public static final String CLIENT = "client";
    public static final String ENERGY = "energy";
    public static final String NETWORKS = "networks";

    public static boolean enableButtonSound;
    public static boolean enableFluxRecipe, enableChunkLoading, enableSuperAdmin;
    public static int defaultLimit, basicCapacity, basicTransfer, herculeanCapacity, herculeanTransfer, gargantuanCapacity, gargantuanTransfer;
    public static int maximumPerPlayer;

    public static void init(File file) {
        config = new Configuration(new File(file.getPath(), "flux_networks.cfg"));
        config.load();
        read();
        config.save();
        generateFluxChunkConfig();
    }

    public static void generateFluxChunkConfig() {
        if(!ForgeChunkManager.getConfig().hasCategory(FluxNetworks.MODID)) {
            ForgeChunkManager.getConfig().get(FluxNetworks.MODID, "maximumChunksPerTicket", 1000000).setMinValue(0);
            ForgeChunkManager.getConfig().get(FluxNetworks.MODID, "maximumTicketCount", 1000000).setMinValue(0);
            ForgeChunkManager.getConfig().save();
        }
    }

    public static void read() {
        defaultLimit = config.getInt("Default Transfer Limit", ENERGY, 800000, 0, Integer.MAX_VALUE, "The default transfer limit of a flux connector");

        basicCapacity = config.getInt("Basic Storage Capacity", ENERGY, 1000000, 0, Integer.MAX_VALUE, "");
        basicTransfer = config.getInt("Basic Storage Transfer", ENERGY, 20000, 0, Integer.MAX_VALUE, "");
        herculeanCapacity = config.getInt("Herculean Storage Capacity", ENERGY, 8000000, 0, Integer.MAX_VALUE, "");
        herculeanTransfer = config.getInt("Herculean Storage Transfer", ENERGY, 120000, 0, Integer.MAX_VALUE, "");
        gargantuanCapacity = config.getInt("Gargantuan Storage Capacity", ENERGY, 128000000, 0, Integer.MAX_VALUE, "");
        gargantuanTransfer = config.getInt("Gargantuan Storage Transfer", ENERGY, 1440000, 0, Integer.MAX_VALUE, "");

        maximumPerPlayer = config.getInt("Maximum Networks Per Player", NETWORKS, 3, -1, Integer.MAX_VALUE, "Maximum networks each player can have. -1 = no limit");
        enableSuperAdmin = config.getBoolean("Allow Network Super Admin", NETWORKS, true, "Allows someone to be a network super admin, otherwise, no one can access or dismantle your flux devices or delete your networks without permission");

        enableFluxRecipe = config.getBoolean("Enable Flux Recipe", GENERAL, true, "Enables redstones being compressed with the bedrock and obsidian to get flux");
        enableChunkLoading = config.getBoolean("Allow Flux Chunk Loading", GENERAL, true, "Allows flux connectors to work as chunk loaders");

        enableButtonSound = config.getBoolean("Enable GUI Button Sound", CLIENT, true, "Enable navigation buttons sound when pressing it");
    }
}
