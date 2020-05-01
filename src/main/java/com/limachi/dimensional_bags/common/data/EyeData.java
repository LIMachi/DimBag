package com.limachi.dimensional_bags.common.data;

import com.limachi.dimensional_bags.DimensionalBagsMod;
import com.limachi.dimensional_bags.common.data.actions.IBagAction;
import com.limachi.dimensional_bags.common.data.actions.IBagTrigger;
import com.limachi.dimensional_bags.common.data.actions.StandardActionFactory;
import com.limachi.dimensional_bags.common.data.actions.StandardTriggerFactory;
import com.limachi.dimensional_bags.common.data.inventory.BagInventory;
import com.limachi.dimensional_bags.common.data.inventory.UpgradesInventory;
import com.limachi.dimensional_bags.common.dimensions.BagDimension;
import com.limachi.dimensional_bags.common.items.Bag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.UUID;

import static com.limachi.dimensional_bags.common.config.DimBagConfig.COLUMNS_ID;
import static com.limachi.dimensional_bags.common.config.DimBagConfig.RADIUS_ID;
import static com.limachi.dimensional_bags.common.config.DimBagConfig.ROWS_ID;

public class EyeData /*implements IInventory*/ { //all information about an eye (accessed through bag item, bag entity, bag eye and other tiles in the bag

    private int id;
    @Nullable
    private TileEntity te; //used for world manipulation, only set server side
    private UUID owner;
    private BlockPos position;
    private DimensionType dimension;

    public BagInventory items; //public for now
    public UpgradesInventory upgrades; //public for now

    /*
    private int[] upgradesManager;
    private ItemStack[] items;
    */

    private IBagTrigger[] possibleTriggers = { //the order and quantity of triggers is always the same
            StandardTriggerFactory.specific(false, false, false), //0
            StandardTriggerFactory.specific(false, false, true), //1
            StandardTriggerFactory.specific(false, true, false), //2
            StandardTriggerFactory.specific(false, true, true), //3
            StandardTriggerFactory.specific(true, false, false), //4
            StandardTriggerFactory.specific(true, false, true) //5
    };

    private ArrayList<IBagAction> possibleActions = new ArrayList<>();

    public EyeData(DimBagData dataManager, ServerPlayerEntity owner, int id) {
        this.upgrades = new UpgradesInventory(this);
        this.init();
        this.dataManager = dataManager;
        this.id = id;
        if (owner != null) {
            this.owner = owner.getUniqueID();
            if (id != 0 && owner.getServer() != null)
                this.te = BagDimension.get(owner.getServer()).getTileEntity(new BlockPos(8 + 1024 * (id - 1), 128, 8));
        }
        this.items = new BagInventory(this);
//        if (this.id != 0)
//            this.markDirty();
    }

    public EyeData(DimBagData dataManager) { this(dataManager, null, 0); }

    public EyeData(PacketBuffer buff) {
        this((DimBagData)null); //dangerous, dataManager should be attached asap
        this.readBytes(buff);
    }

    public void registerActions(IBagAction action) {
        this.possibleActions.add(action);
        this.markDirty();
    }

    public final IBagTrigger[] getTriggers() { return this.possibleTriggers; }
    public final ArrayList<IBagAction> getActions() { return this.possibleActions; }

    public int getTrigger(Bag.BagEvent event) {
        for (int i = 0; i < this.possibleTriggers.length; ++i) {
            IBagTrigger trigger = this.possibleTriggers[i];
            if (trigger.match(event))
                DimensionalBagsMod.LOGGER.info(trigger.printable());
            if (trigger.mappedAction() != -1 && trigger.match(event))
                return trigger.mappedAction();
        }
        return -1;
    }

    public void runAction(int action, ServerPlayerEntity player, Hand hand) {
        this.possibleActions.get(action).execute(player, hand, this);
    }

    public IBagAction getAction(int action) {
        if (action >= 0 && action < this.possibleActions.size())
            return this.possibleActions.get(action);
        return new IBagAction() {
            @Override
            public String getName() {
                return "not set";
            }

            @Override
            public void execute(ServerPlayerEntity player, Hand hand, EyeData data) {
            }

            @Override
            public ResourceLocation getIcon() {
                return new ResourceLocation("textures/item/stone.png");
            }
        };
    }

    public void mapDefaultActions() {
        this.possibleActions.add(StandardActionFactory.inventoryGUI());
        this.possibleActions.add(StandardActionFactory.teleportPlayer());
        this.possibleActions.add(StandardActionFactory.actionsGUI());
        this.possibleActions.add(StandardActionFactory.upgradesGUI());
        this.possibleTriggers[0].mapAction(0);
        this.possibleTriggers[1].mapAction(1);
        this.possibleTriggers[2].mapAction(0);
        this.possibleTriggers[3].mapAction(1);
        this.possibleTriggers[4].mapAction(3);
        this.possibleTriggers[5].mapAction(2);
    }

    public boolean dirty;

    private DimBagData dataManager;

    public final DimBagData getDataManager() { return this.dataManager; }

    public BlockPos getPosition() { return this.position; }
    public DimensionType getDimension() { return this.dimension; }

    public void updateBagPosition(BlockPos newPos, DimensionType newDim) {
        this.position = newPos;
        this.dimension = newDim;
    }

    private void init() {
        if (this.possibleActions.size() == 0)
            this.mapDefaultActions();
//        this.upgradesManager = new int[DimBagConfig.upgradesManager.length];
//        for (int i = 0; i < DimBagConfig.upgradesManager.length; ++i)
//            this.upgradesManager[i] = DimBagConfig.upgradesManager[i].start;
        this.dimension = DimensionType.OVERWORLD;
        this.position = new BlockPos(0, 64, 0);
    }

    /*
    public final ItemStack getUpgrade(int id) { return this.upgradesManager.getStackInSlot(id); }

    public void addUpgrades(int id, int count) {
        if (id >= this.upgradesManager.length) {
            int newLength = max(id + 1, DimBagConfig.upgradesManager.length);
            int[] newUpgrades = new int[newLength];
            for (int i = 0; i < this.upgradesManager.length; ++i)
                newUpgrades[i] = this.upgradesManager[i];
            for (int i = this.upgradesManager.length; i < newLength; ++i)
                newUpgrades[i] = i < DimBagConfig.upgradesManager.length ? DimBagConfig.upgradesManager[i].start : 0;
            this.upgradesManager = newUpgrades;
        }
        this.upgradesManager[id] += count;
    }
    */

    public void attachDataManager(DimBagData dataManager) { this.dataManager = dataManager; }

    /*
    private void newItems() {
        this.items = new ItemStack[this.getSizeInventory()];
        for (int i = 0; i < this.getSizeInventory(); ++i)
            this.items[i] = ItemStack.EMPTY;
    }
    */

    public IdHandler getId() { return new IdHandler(this.id); }

    /*
    public int getRows() { return this.upgradesManager[ROWS_ID]; }
    public int getColumns() { return this.upgradesManager[COLUMNS_ID]; }
    public int getRadius() { return this.upgradesManager[RADIUS_ID]; }
    */

    public int getRows() { return this.upgrades.getStackInSlot(ROWS_ID).getCount(); }
    public int getColumns() { return this.upgrades.getStackInSlot(COLUMNS_ID).getCount(); }
    public int getRadius() { return this.upgrades.getStackInSlot(RADIUS_ID).getCount(); }

    /*
    public CompoundNBT write(CompoundNBT nbt) {
        DimensionalBagsMod.LOGGER.info("Storing data for eye " + this.id);
        nbt.putInt("Id", this.id);
        nbt.putUniqueId("Owner", this.owner);
        CompoundNBT position = new CompoundNBT();
        position.putInt("x", this.position.getX());
        position.putInt("y", this.position.getY());
        position.putInt("z", this.position.getZ());
        position.putInt("dim", this.dimension.getId());
        nbt.put("position", position);
        CompoundNBT upgradesManager = new CompoundNBT();
        for (int i = 0; i < this.upgradesManager.length; ++i)
            upgradesManager.putInt(DimBagConfig.upgradesManager[i].id, this.upgradesManager[i]);
        nbt.put("upgradesManager", upgradesManager);
        ListNBT list = new ListNBT();
        for (int i = 0; i < this.items.length; ++i) {
            if (this.items[i].isEmpty()) continue;
            CompoundNBT stack = new CompoundNBT();
            stack.putInt("ItemStackIndex", i);
            this.items[i].write(stack);
            list.add(stack);
        }
        nbt.put("Items", list);
        return nbt;
    }*/

    public CompoundNBT write(CompoundNBT nbt) {
        DimensionalBagsMod.LOGGER.info("Storing data for eye " + this.id);
        nbt.putInt("Id", this.id);
        nbt.putUniqueId("Owner", this.owner);
        CompoundNBT position = new CompoundNBT();
        position.putInt("x", this.position.getX());
        position.putInt("y", this.position.getY());
        position.putInt("z", this.position.getZ());
        position.putInt("dim", this.dimension.getId());
        nbt.put("position", position);
        CompoundNBT upgrades = new CompoundNBT();
        this.upgrades.write(upgrades);
        nbt.put("upgradesManager", upgrades);
        CompoundNBT inventory = new CompoundNBT();
        this.items.write(inventory);
        nbt.put("inventory", inventory);
        return nbt;
    }

    public void read(CompoundNBT nbt) {
        DimensionalBagsMod.LOGGER.info("Loadding data for eye " + this.id);
        this.id = nbt.getInt("Id");
        this.owner = nbt.getUniqueId("Owner");
        CompoundNBT position = nbt.getCompound("position");
        this.position = new BlockPos(position.getInt("x"), position.getInt("y"), position.getInt("z"));
        this.dimension = DimensionType.getById(position.getInt("dim"));
        CompoundNBT upgrades = nbt.getCompound("upgradesManager");
        this.upgrades.read(upgrades);
        CompoundNBT inventory = nbt.getCompound("inventory");
        this.items.read(inventory);
    }

    /*
    public void read(CompoundNBT nbt) {
        this.id = nbt.getInt("Id");
        this.owner = nbt.getUniqueId("Owner");
        CompoundNBT position = nbt.getCompound("position");
        this.position = new BlockPos(position.getInt("x"), position.getInt("y"), position.getInt("z"));
        this.dimension = DimensionType.getById(position.getInt("dim"));
        CompoundNBT upgradesManager = nbt.getCompound("upgradesManager");
        this.upgradesManager = new int[DimBagConfig.upgradesManager.length];
        for (int i = 0; i < DimBagConfig.upgradesManager.length; ++i)
            this.upgradesManager[i] = upgradesManager.getInt(DimBagConfig.upgradesManager[i].id);
        this.newItems();
        ListNBT list = nbt.getList("Items", 10); //type 10 == CompoundNBT
        for (int i = 0; i < list.size(); ++i) {
            CompoundNBT stack = list.getCompound(i);
            int index = stack.getInt("ItemStackIndex");
            if (index < items.length)
                this.items[index] = ItemStack.read(stack);
        }
    }
    */

    public PacketBuffer toBytes(PacketBuffer buff) {
        DimensionalBagsMod.LOGGER.info("preparing eye packet");
        buff.writeInt(this.id);
        buff.writeUniqueId(this.owner);
        buff.writeInt(this.position.getX());
        buff.writeInt(this.position.getY());
        buff.writeInt(this.position.getZ());
        buff.writeInt(this.dimension.getId());
//        buff.writeBoolean(this.upgradesManager.isDirty());
//        if (this.upgradesManager.isDirty())
            this.upgrades.toBytes(buff);
//        buff.writeBoolean(this.items.isDirty());
//        if (this.items.isDirty())
            this.items.toBytes(buff);
        return buff;
    }

    public void readBytes(PacketBuffer buff) {
        DimensionalBagsMod.LOGGER.info("got eye packet");
        this.id = buff.readInt();
        this.owner = buff.readUniqueId();
        int x = buff.readInt();
        int y = buff.readInt();
        int z = buff.readInt();
        this.init();
        this.position = new BlockPos(x, y, z);
        this.dimension = DimensionType.getById(buff.readInt());
//        if (buff.readBoolean())
            this.upgrades.readBytes(buff);
//        if (buff.readBoolean())
            this.items.readBytes(buff);
//        this.markDirty(); //should not probably not mark dirty since it just got sync
    }

    /*
    public PacketBuffer toBytes(PacketBuffer buff) { //exact order matters
        buff.writeInt(this.id);
        buff.writeUniqueId(this.owner);
        buff.writeInt(this.position.getX());
        buff.writeInt(this.position.getY());
        buff.writeInt(this.position.getZ());
        buff.writeInt(this.dimension.getId());
        buff.writeInt(this.upgradesManager.length);
        for (int i = 0; i < this.upgradesManager.length; ++i)
            buff.writeInt(this.upgradesManager[i]);
        for (int i = 0; i < this.getSizeInventory(); ++i)
            buff.writeItemStack(this.items[i]);
        return buff;
    }
    */

    /*
    public void readBytes(PacketBuffer buff) { //exact order matters
        this.id = buff.readInt();
        this.owner = buff.readUniqueId();
        int x = buff.readInt();
        int y = buff.readInt();
        int z = buff.readInt();
        this.init();
        this.position = new BlockPos(x, y, z);
        this.dimension = DimensionType.getById(buff.readInt());
        int l = buff.readInt();
        for (int i = 0; i < l; ++i)
            this.upgradesManager[i] = buff.readInt();
        this.newItems();
        for (int i = 0; i < this.getSizeInventory(); ++i)
            this.items[i] = buff.readItemStack();
        this.markDirty();
    }
    */

    /*
    @Override
    public int getSizeInventory() {
        return this.upgradesManager[ROWS_ID] * this.upgradesManager[COLUMNS_ID];
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack: this.items)
            if (!stack.isEmpty())
                return false;
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        synchronized (this) {
            return this.items[index];
        }
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        synchronized (this) {
            ItemStack item = this.items[index];
            if (!item.isEmpty()) {
                if (item.getCount() <= count) {
                    this.items[index] = ItemStack.EMPTY;
                    this.markDirty();
                    return item;
                }
                ItemStack split = item.split(count);
                if (item.getCount() == 0)
                    this.items[index] = ItemStack.EMPTY;
                else
                    this.items[index] = item;
                this.markDirty();
                return split;
            }
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        synchronized (this) {
            ItemStack stack = this.items[index];
            this.items[index] = ItemStack.EMPTY;
            this.markDirty();
            return stack;
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        synchronized (this) {
            this.items[index] = stack;
            this.markDirty();
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }
    */

//    @Override
    public void markDirty() {
        DimensionalBagsMod.LOGGER.info("eye " + this.id + " is now dirty"); //send the game in an infinite loop... oops; should rework the data sync
        if (this.dataManager != null) {
            this.dirty = true;
            this.dataManager.update(true);
        }
    }

    /*
    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return true; //missing logic
    }

    @Override
    public void openInventory(PlayerEntity player) {

    }

    @Override
    public void closeInventory(PlayerEntity player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return false;
    }

    @Override
    public int count(Item itemIn) {
        return 0;
    }

    @Override
    public boolean hasAny(Set<Item> set) {
        return false;
    }

    @Override
    public void clear() {

    }
    */
}
