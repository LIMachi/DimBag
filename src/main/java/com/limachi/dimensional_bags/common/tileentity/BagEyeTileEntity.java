package com.limachi.dimensional_bags.common.tileentity;

import com.limachi.dimensional_bags.DimensionalBagsMod;
import com.limachi.dimensional_bags.common.blocks.BagEye;
import com.limachi.dimensional_bags.common.config.DimBagConfig;
import com.limachi.dimensional_bags.common.container.BagEyeContainer;
import com.limachi.dimensional_bags.common.init.Registries;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.UUID;

public class BagEyeTileEntity extends LockableLootTileEntity implements ITickableTileEntity {

    public int tick = 0;
    private boolean initialized = false;
    private int lvl = 0;
    private NonNullList<ItemStack> content = NonNullList.create();
    protected int numPlayerUsing = 0;
    private IItemHandlerModifiable items = createHandler();
    private LazyOptional<IItemHandlerModifiable> itemHandler = LazyOptional.of(() -> items);

    public class PlayerTPBack {
        public DimensionType type;
        public BlockPos pos;
        public UUID UID;

        public PlayerTPBack() {}

        public PlayerTPBack(PlayerEntity player) {
            type = player.dimension;
            pos = player.getPosition();
            UID = player.getUniqueID();
        }

        public CompoundNBT write(CompoundNBT compound, int index) {
            compound.putUniqueId("PTB_" + index + "_UID", UID);
            compound.putInt("PTB_" + index + "_X", pos.getX());
            compound.putInt("PTB_" + index + "_Y", pos.getY());
            compound.putInt("PTB_" + index + "_Z", pos.getZ());
            compound.putInt("PTB_" + index + "_DIM", type.getId());
            return compound;
        }

        public PlayerTPBack read(CompoundNBT compound, int index) {
            UID = compound.getUniqueId("PTB_" + index + "_UID");
            int x = compound.getInt("PTB_" + index + "_X");
            int y = compound.getInt("PTB_" + index + "_Y");
            int z = compound.getInt("PTB_" + index + "_Z");
            pos = new BlockPos(x, y, z);
            type = DimensionType.getById(compound.getInt("PTB_" + index + "_DIM"));
            return this;
        }
    }

    private ArrayList<PlayerTPBack> PTB = new ArrayList<>();

    public void newPTB(PlayerEntity player) {
        for (int i = 0; i < PTB.size(); ++i)
            if (PTB.get(i).UID == player.getUniqueID()) {
                //if there is already a PBT assigned to the player, just overide it
                PTB.set(i, new PlayerTPBack(player));
                return;
            }
        PTB.add(new PlayerTPBack(player));
    }

    public PlayerTPBack getPTBForPlayer(PlayerEntity player) {
        for (int i = 0; i < PTB.size(); ++i)
            if (PTB.get(i).UID == player.getUniqueID())
                return PTB.get(i);
        // if no TP back can be found, just send the player to the overworld (should not apen if the player entered and tried to exit the dimension as intended)
        PlayerTPBack t = new PlayerTPBack();
        t.UID = player.getUniqueID();
        t.pos = player.getServer().getWorld(DimensionType.OVERWORLD).getSpawnPoint();
        t.type = DimensionType.OVERWORLD;
        return t;
    }

    public BagEyeTileEntity() { super(Registries.BAG_EYE_TE.get()); }
    public int getRows() { return 3; } //FIXME
    public int getColumns() { return 9; } //FIXME
    public int getVolume() { return DimBagConfig.startingRadius; } //FIXME
    public int getLvl() { return this.lvl; }
    public void setLvl(int lvlIn) { if (lvlIn >= 0 && lvlIn <= 5) this.lvl = lvlIn; }

    @Override
    public int getSizeInventory() {
        return getColumns() * getRows();
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.content;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> itemsIn) {
        this.content = itemsIn; //dangerous if invalid size
    }

    @Override
    public ITextComponent getDefaultName() {
        return new TranslationTextComponent("container.bag_eye");
    }

    private void init() {
        initialized = true;
        tick = 0;
        if (lvl == 0)
            lvl = 1;
        for (int i = 0; i < getSizeInventory(); ++i) {
            content.add(ItemStack.EMPTY);
        }
    }

    public void upgrade() {
        if (lvl >= 6)
            return;
        int prevSize = getSizeInventory();
        ++lvl;
        int newSize = getSizeInventory();
        for (int i = prevSize; i < newSize; ++i) {
            content.add(ItemStack.EMPTY);
        }

    }

    @Override
    public void tick() {
        if (!initialized) init();
        tick++;
        if (tick % 40 == 0)
            execute();
    }

    private void execute() {
        //do something
        DimensionalBagsMod.LOGGER.info("ticking: " + this.pos.getX() + " " + this.pos.getY() + " " + this.pos.getZ() + " tick = " + tick);
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player) {
        return new BagEyeContainer(id, player, this);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putInt("lvl", lvl);
        compound.putInt("tick", tick);
        compound.putInt("PTB_len", PTB.size());
        for (int i = 0; i < PTB.size(); ++i)
            PTB.get(i).write(compound, i);
        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, this.content);
        }
        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        lvl = compound.getInt("lvl");
        tick = compound.getInt("tick");
        PTB = new ArrayList<>();
        int l = compound.getInt("PTB_len");
        for (int i = 0; i < l; ++i)
            PTB.add(new PlayerTPBack().read(compound, i));
        initialized = true;
        this.content = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        if (!this.checkLootAndRead(compound)) {
            ItemStackHelper.loadAllItems(compound, this.content);
        }
    }

    private void playSound(SoundEvent sound) {
        double x = (double) this.pos.getX() + 0.5d;
        double y = (double) this.pos.getY() + 0.5d;
        double z = (double) this.pos.getZ() + 0.5d;

        this.world.playSound((PlayerEntity) null, x, y, z, sound, SoundCategory.BLOCKS, 0.5f, this.world.rand.nextFloat() * 0.1f + 0.9f);
    }

    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (id == 1) {
            this.numPlayerUsing = type;
            return true;
        }
        return super.receiveClientEvent(id, type);
    }

    @Override
    public void openInventory(PlayerEntity player) {
        if (!player.isSpectator()) {
            if (this.numPlayerUsing < 0) {
                this.numPlayerUsing = 0;
            }
            ++this.numPlayerUsing;
            this.onOpenOrClose();
        }
    }

    @Override
    public void closeInventory(PlayerEntity player) {
        if (!player.isSpectator()) {
            --this.numPlayerUsing;
            this.onOpenOrClose();
        }
    }

    protected void onOpenOrClose() {
        Block block = this.getBlockState().getBlock();
        if (block instanceof BagEye) {
            this.world.addBlockEvent(this.pos, block, 1, this.numPlayerUsing);
            this.world.notifyNeighbors(this.pos, block);
        }
    }

    public static int getPlayersUsing(IBlockReader reader, BlockPos pos) {
        BlockState blockState = reader.getBlockState(pos);

        if (blockState.hasTileEntity()) {
            TileEntity tileEntity = reader.getTileEntity(pos);
            if (tileEntity instanceof BagEyeTileEntity) {
                return ((BagEyeTileEntity) tileEntity).numPlayerUsing;
            }
        }
        return 0;
    }

    public static void swapContens(BagEyeTileEntity te1, BagEyeTileEntity te2) {
        if (te1.lvl != te2.lvl)
            return;
        NonNullList<ItemStack> list = te1.getItems();
        te1.setItems(te2.getItems());
        te2.setItems(list);
    }

    @Override
    public void updateContainingBlockInfo() {
        super.updateContainingBlockInfo();
        if (this.itemHandler != null) {
            this.itemHandler.invalidate();
            this.itemHandler = null;
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nonnull Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    private IItemHandlerModifiable createHandler() {
        return new InvWrapper(this);
    }

    @Override
    public void remove() {
        super.remove();
        if (itemHandler != null) {
            itemHandler.invalidate();
        }
    }
}