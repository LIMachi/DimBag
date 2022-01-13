package com.limachi.dimensional_bags.lib.common.container;

//import com.limachi.dimensional_bags.common.container.slot.InvWrapperSlot;
//import com.limachi.dimensional_bags.common.inventory.PlayerInvWrapper;
//import com.limachi.dimensional_bags.common.inventory.Wrapper;
/*
public class BaseWrappedInventoryContainer extends Container {

    protected PlayerInvWrapper playerInv;
    protected Wrapper openInv;
    protected boolean client;

    public BaseWrappedInventoryContainer(ContainerType<? extends BaseWrappedInventoryContainer> type, int windowId) { //common constructor
        super(type, windowId);
    }

    protected BaseWrappedInventoryContainer(ContainerType<? extends BaseWrappedInventoryContainer> type, int windowId, PlayerInventory playerInv, PacketBuffer extraData) { //client side/registry constructor
        super(type, windowId);
        this.playerInv = new PlayerInvWrapper(playerInv);
        this.openInv = new Wrapper(extraData);
        this.client = true;
    }

    protected BaseWrappedInventoryContainer(ContainerType<? extends BaseWrappedInventoryContainer> type, int windowId, ServerPlayerEntity player, Wrapper openInv) { //server side constructor
        super(type, windowId);
        this.playerInv = new PlayerInvWrapper(player.inventory);
        this.openInv = openInv;
        this.client = false;
    }

    public Wrapper.IORights getRights(int slot) {
        if (slot < 36 || slot >= inventorySlots.size()) return new Wrapper.IORights();
        return openInv.getRights(slot - 36);
    }

    public void changeRights(int slot, Wrapper.IORights rights) {
        openInv.setRights(slot, rights);
    }

    protected void addPlayerSlots(int px, int py) {
        int dx = px + PLAYER_INVENTORY_FIRST_SLOT_X + 1;
        int dy = py + PLAYER_BELT_FIRST_SLOT_Y + 1;
        for (int x = 0; x < PLAYER_INVENTORY_COLUMNS; ++x)
            addSlot(new InvWrapperSlot(playerInv, x, dx + x * SLOT_SIZE_X, dy));
        dy = py + PLAYER_INVENTORY_FIRST_SLOT_Y + 1;
        for (int y = 0; y < PLAYER_INVENTORY_ROWS; ++y)
            for (int x = 0; x < PLAYER_INVENTORY_COLUMNS; ++x)
                addSlot(new InvWrapperSlot(playerInv, x + (y + 1) * PLAYER_INVENTORY_COLUMNS, dx + x * SLOT_SIZE_X, dy + y * SLOT_SIZE_Y));
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int position)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(position);

        if(slot != null && slot.hasItem())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if(position >= 36 && !Wrapper.mergeItemStack(inventorySlots, itemstack1, 0, 36, false, new ArrayList<>()))
                return ItemStack.EMPTY;
            else if(!Wrapper.mergeItemStack(inventorySlots, itemstack1, 36, this.inventorySlots.size(), false, new ArrayList<>()))
                return ItemStack.EMPTY;
            if(itemstack1.getCount() == 0)
                slot.set(ItemStack.EMPTY);
            else
                slot.setChanged();
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) { return true; }
}
*/