package com.limachi.dim_bag.bag_modules.block_entity;

import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.dim_bag.bag_modules.ObserverModule;
import com.limachi.dim_bag.entities.utils.TagOperation;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.registries.annotations.RegisterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.extensions.IForgeEntity;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class ObserverModuleBlockEntity extends BlockEntity {

    @RegisterBlockEntity(blocks = "observer_module")
    public static RegistryObject<BlockEntityType<ObserverModuleBlockEntity>> R_TYPE;

    protected TagOperation command = new TagOperation(null);
    protected int bagId;
    private BagInstance bag = null; //only present server side!!
    protected int tickRate = 1; //FIXME: should add a way to tweak this value
    protected int tick = -1;

    public ObserverModuleBlockEntity(BlockPos pos, BlockState state) { super(R_TYPE.get(), pos, state); }

    public void install(BagInstance bag, CompoundTag command) {
        this.command = new TagOperation(command);
        this.bagId = bag.bagId();
        tick = -1;
    }

    public void replaceCommand(CompoundTag command) {
        this.command = new TagOperation(command);
        tick = -1;
    }

    public CompoundTag uninstall() { return command.getOriginal(); }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(ObserverModule.COMMAND_KEY, command.getOriginal());
        tag.putInt("tickRate", tickRate);
        if (level != null && !level.isClientSide)
            tag.putInt("bag", bagId);
    }

    public BagInstance getBag() {
        if (bag == null)
            bag = BagsData.getBagHandle(bagId, ()->bag = null);
        return bag;
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        bag = null;
        CompoundTag cmd = tag.getCompound(ObserverModule.COMMAND_KEY);
        if (!command.getOriginal().equals(cmd))
            command = new TagOperation(cmd);
        bagId = tag.getInt("bag");
        if (tag.contains("tickRate"))
            tickRate = tag.getInt("tickRate");
    }

    public CompoundTag getTargetData() {
        return getBag() != null ? bag.getHolder(false).map(IForgeEntity::serializeNBT).orElse(new CompoundTag()) : new CompoundTag();
    }

    public void tick(BlockState state) {
        if (level != null && !level.isClientSide && ++tick % tickRate == 0 && getBag() != null) {
            int currentPower = state.getValue(BlockStateProperties.POWER);
            int newPower = bag.getHolder(false).map(e->command.run(e.serializeNBT())).orElse(0);
            if (currentPower != newPower) {
                level.setBlock(worldPosition, state.setValue(BlockStateProperties.POWER, newPower), 2);
                level.updateNeighborsAt(worldPosition, state.getBlock());
            }
        }
    }
}
