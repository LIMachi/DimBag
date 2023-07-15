package com.limachi.dim_bag.bag_modules.block_entity;

import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.dim_bag.entities.utils.EntityObserver;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.registries.annotations.RegisterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class ObserverModuleBlockEntity extends BlockEntity {

    @RegisterBlockEntity(blocks = "observer_module")
    public static RegistryObject<BlockEntityType<ObserverModuleBlockEntity>> R_TYPE;

    protected EntityObserver command = new EntityObserver(null);
    protected int bagId;
    private BagInstance bag = null; //only present server side!!
    protected int tickRate = 4;
    protected int tick = -1;

    public ObserverModuleBlockEntity(BlockPos pos, BlockState state) { super(R_TYPE.get(), pos, state); }

    public void install(BagInstance bag, ListTag list) {
        this.command = new EntityObserver(list);
        this.bagId = bag.bagId();
        this.tick = -1;
    }

    public ListTag uninstall() { return command.getCommands(); }

    public void setCommand(ListTag cmd) {
        if (!command.getCommands().equals(cmd)) {
            command = new EntityObserver(cmd);
            setChanged();
        }
    }

    public ListTag getCommand() { return command.getCommands(); }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("commands", command.getCommands());
        tag.putInt("tickRate", tickRate);
        if (level != null && !level.isClientSide)
            tag.putInt("bag", bag.bagId());
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
        ListTag cmd = tag.getList("commands", Tag.TAG_COMPOUND);
        if (!command.getCommands().equals(cmd))
            command = new EntityObserver(cmd);
        if (level != null && !level.isClientSide)
            bagId = tag.getInt("bag");
        if (tag.contains("tickRate"))
            tickRate = tag.getInt("tickRate");
    }

    public void tick(BlockState state) {
        if (level != null && !level.isClientSide && ++tick % tickRate == 0 && getBag() != null) {
            int currentPower = state.getValue(BlockStateProperties.POWER);
            int newPower = bag.getHolder(false).map(e->Mth.clamp((int)Math.round(command.run(e)), 0, 15)).orElse(0);
            if (currentPower != newPower) {
                level.setBlock(worldPosition, state.setValue(BlockStateProperties.POWER, newPower), 2);
                level.updateNeighborsAt(worldPosition, state.getBlock());
            }
        }
    }
}
