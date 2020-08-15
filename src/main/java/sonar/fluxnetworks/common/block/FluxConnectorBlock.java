package sonar.fluxnetworks.common.block;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.world.IWorldReader;
import sonar.fluxnetworks.common.tileentity.TileFluxConnector;
import sonar.fluxnetworks.common.tileentity.TileFluxDevice;
import sonar.fluxnetworks.common.misc.FluxUtils;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.minecraft.state.properties.BlockStateProperties.*;

/**
 * Defines the block base class for any flux device that can connect to adjacent blocks
 */
public abstract class FluxConnectorBlock extends FluxDeviceBlock {

    // the indices are equal to that of Direction.values[]
    public static final BooleanProperty[] SIDES_CONNECTED = new BooleanProperty[]{DOWN, UP, NORTH, SOUTH, WEST, EAST};

    public FluxConnectorBlock(Properties props) {
        super(props);
        this.setDefaultState(getDefaultState()
                .with(DOWN, true)
                .with(UP, true)
                .with(NORTH, true)
                .with(SOUTH, true)
                .with(WEST, true)
                .with(EAST, true)); //inventory
    }

    @Override
    public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, world, pos, neighbor);
        TileFluxConnector tile = (TileFluxConnector) world.getTileEntity(pos);
        if (tile != null && !tile.getFluxWorld().isRemote) {
            tile.updateTransfers(FluxUtils.getBlockDirection(pos, neighbor));
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getConnectedState(getDefaultState(), context.getWorld(), context.getPos());
    }

    public static BlockState getConnectedState(BlockState state, World world, BlockPos pos){
        TileFluxDevice tile = (TileFluxDevice) world.getTileEntity(pos);
        for(int i = 0 ; i < Direction.values().length; i++){
            state = state.with(SIDES_CONNECTED[i], tile != null && tile.connections[i] == 1);
        }
        return state;
    }

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        builder.add(SIDES_CONNECTED);
    }
}
