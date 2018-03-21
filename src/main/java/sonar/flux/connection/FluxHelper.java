package sonar.flux.connection;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.FMLCommonHandler;
import sonar.core.SonarCore;
import sonar.core.api.SonarAPI;
import sonar.core.api.energy.EnergyType;
import sonar.core.api.energy.ISonarEnergyContainerHandler;
import sonar.core.api.energy.ISonarEnergyHandler;
import sonar.core.api.utils.ActionType;
import sonar.core.integration.SonarLoader;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.flux.FluxConfig;
import sonar.flux.FluxNetworks;
import sonar.flux.api.AdditionType;
import sonar.flux.api.FluxListener;
import sonar.flux.api.RemovalType;
import sonar.flux.api.energy.IEnergyTransfer;
import sonar.flux.api.network.FluxPlayer;
import sonar.flux.api.network.IFluxNetwork;
import sonar.flux.api.tiles.IFlux;
import sonar.flux.api.tiles.IFluxController;
import sonar.flux.api.tiles.IFluxController.PriorityMode;
import sonar.flux.api.tiles.IFluxController.TransferMode;
import sonar.flux.api.tiles.IFluxController.TransmitterMode;
import sonar.flux.api.tiles.IFluxListenable;
import sonar.flux.api.tiles.IFluxPlug;
import sonar.flux.api.tiles.IFluxPoint;
import sonar.flux.common.tileentity.TileFlux;
import sonar.flux.common.tileentity.TileStorage;
import sonar.flux.network.FluxNetworkCache;
import sonar.flux.network.PacketFluxConnectionsList;
import sonar.flux.network.PacketFluxNetworkList;
import sonar.flux.network.PacketNetworkStatistics;

public class FluxHelper {

	public static void addConnection(IFluxListenable flux, AdditionType type) {
		FluxNetworkCache.instance().getListenerList().addSubListenable(flux);
		if (flux.getNetworkID() != -1) {
			IFluxNetwork network = FluxNetworks.getServerCache().getNetwork(flux.getNetworkID());
			if (!network.isFakeNetwork()) {
				network.addConnection(flux, type);
			}
		}
	}

	public static void removeConnection(IFluxListenable flux, RemovalType type) {
		FluxNetworkCache.instance().getListenerList().removeSubListenable(flux);
		if (flux.getNetworkID() != -1) {
			IFluxNetwork network = FluxNetworks.getServerCache().getNetwork(flux.getNetworkID());
			if (!network.isFakeNetwork()) {
				network.removeConnection(flux, type);
			}
		}
	}

	public static UUID getOwnerUUID(EntityPlayer player) {
		return player.getGameProfile().getId();
	}

	public static boolean isPlayerAdmin(EntityPlayer player) {
		return player.isCreative();
	}

	public static void sortConnections(List<IFlux> flux, PriorityMode mode) {
		switch (mode) {
		case DEFAULT:
			break;
		case LARGEST:
			flux.sort((o1, o2) -> o2.getCurrentPriority() - o1.getCurrentPriority());
			break;
		case SMALLEST:
			flux.sort(Comparator.comparingInt(IFlux::getCurrentPriority));
			break;
		default:
			break;
		}
	}

	public static void sendPacket(IFluxNetwork network, TileFlux flux, ListenerTally<PlayerListener> tally) {
		for (int i = 0; i < tally.tallies.length; i++) {
			if (tally.tallies[i] > 0) {
				
			}
		}
	}

	public static long transferEnergy(IFluxPlug plug, List<IFluxPoint> points, TransferMode mode) {
		long currentLimit = Long.MAX_VALUE;
		for (IFluxPoint point : points) {
			if (currentLimit <= 0) {
				break;
			}
			if (point.getConnectionType() != plug.getConnectionType()) {// storages can be both
				long toTransfer = addEnergyToNetwork(plug, removeEnergyFromNetwork(point, currentLimit, ActionType.SIMULATE), ActionType.SIMULATE);
				if (toTransfer > 0) {
					long pointRec = removeEnergyFromNetwork(point, toTransfer, ActionType.PERFORM);
					currentLimit -= addEnergyToNetwork(plug, pointRec, ActionType.PERFORM);
				}
			}
		}
		return Long.MAX_VALUE - currentLimit;
	}

	public static long addEnergyToNetwork(IFlux from, long maxTransferRF, ActionType actionType) {
		return from.getTransferHandler().addToNetwork(maxTransferRF, actionType);
	}

	public static long removeEnergyFromNetwork(IFlux from, long maxTransferRF, ActionType actionType) {
		return from.getTransferHandler().removeFromNetwork(maxTransferRF, actionType);
	}

	/* @Deprecated public static long pullEnergy(IFlux from, long maxTransferRF, ActionType actionType) { long extracted = 0; maxTransferRF = Math.min(maxTransferRF, from.getCurrentTransferLimit()); if (from != null && maxTransferRF != 0) { switch (from.getConnectionType()) { case PLUG: extracted += from.getTransferHandler().addToNetwork(maxTransferRF - extracted, actionType); break; case STORAGE: break; default: break; } } return extracted; }
	 * @Deprecated public static long pushEnergy(IFlux to, long maxTransferRF, ActionType actionType) { long received = 0; maxTransferRF = Math.min(maxTransferRF, to.getCurrentTransferLimit()); if (to != null && maxTransferRF != 0 && to.hasTransfers()) { switch (to.getConnectionType()) { case POINT: received += to.getTransferHandler().removeFromNetwork(maxTransferRF - received, actionType); break; case STORAGE: break; case CONTROLLER: break; default: break; } } return received; } */

	public static boolean canConnect(TileEntity tile, EnumFacing dir) {
		return tile != null && !(tile instanceof IFlux) && canTransferEnergy(tile, dir) != null;// || SonarLoader.rfLoaded && tile instanceof IEnergyConnection && FluxConfig.transfers.get(EnergyType.RF).a);
	}

	public static List<ISonarEnergyHandler> getEnergyHandlers() {
		List<ISonarEnergyHandler> handlers = Lists.newArrayList();
		for (ISonarEnergyHandler handler : SonarCore.energyHandlers) {
			if (FluxConfig.transfers.get(handler.getProvidedType()).a) {
				handlers.add(handler);
			}
		}
		return handlers;
	}

	public static List<ISonarEnergyContainerHandler> getEnergyContainerHandlers() {
		List<ISonarEnergyContainerHandler> handlers = Lists.newArrayList();
		for (ISonarEnergyContainerHandler handler : SonarCore.energyContainerHandlers) {
			if (FluxConfig.transfers.get(handler.getProvidedType()).b) {
				handlers.add(handler);
			}
		}
		return handlers;
	}

	public static ISonarEnergyHandler canTransferEnergy(TileEntity tile, EnumFacing dir) {
		if(tile instanceof IFlux){
			return null;
		}
		List<ISonarEnergyHandler> handlers = FluxNetworks.energyHandlers;
		for (ISonarEnergyHandler handler : handlers) {
			if (handler.canProvideEnergy(tile, dir)) {
				return handler;
			}
		}
		return null;
	}

	public static ISonarEnergyContainerHandler canTransferEnergy(ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}
		List<ISonarEnergyContainerHandler> handlers = FluxNetworks.energyContainerHandlers;
		for (ISonarEnergyContainerHandler handler : handlers) {
			if (handler.canHandleItem(stack)) {
				return handler;
			}
		}
		return null;
	}
	/* /** gets all the TileEntities which can send/receive energy adjacent to the given IFlux */
	/* public Map<TileEntity, EnumFacing> getConnections(IFlux flux) { Map<TileEntity, EnumFacing> tiles = new HashMap<>(); for (EnumFacing face : EnumFacing.VALUES) { World world = flux.getDimension(); TileEntity tile = world.getTileEntity(flux.getCoords().getBlockPos().offset(face)); if (tile == null || tile.isInvalid()) { continue; } if (SonarAPI.getEnergyHelper().canTransferEnergy(tile, face) != null) { tiles.put(tile, face); } } return tiles; } */
}
