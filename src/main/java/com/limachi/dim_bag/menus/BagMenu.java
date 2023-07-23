package com.limachi.dim_bag.menus;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.bag_data.SlotData;
import com.limachi.dim_bag.bag_data.TankData;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.menus.slots.*;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.dim_bag.utils.Menus;
import com.limachi.lim_lib.menus.IAcceptUpStreamNBT;
import com.limachi.lim_lib.registries.annotations.RegisterMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class BagMenu extends AbstractContainerMenu implements IAcceptUpStreamNBT {

    @RegisterMenu
    public static RegistryObject<MenuType<BagMenu>> R_TYPE;

    public final int bagId;
    private LazyOptional<SlotData> slotsHandle;
    private LazyOptional<TankData> tanksHandle;

    public static final Component TITLE = Component.literal("test");

    public static void open(Player player, int bagId, int page) {
        if (!player.level().isClientSide && bagId > 0)
            NetworkHooks.openScreen((ServerPlayer)player, new SimpleMenuProvider((id, inv, p)->new BagMenu(id, inv, bagId, page), TITLE), buff->{buff.writeInt(page); buff.writeNbt(BagsData.runOnBag(bagId, b->b.getModeData("Settings").orElse(new CompoundTag()), new CompoundTag()));});
    }

    private double ds = 0.;
    private int scroll = 0;
    public CompoundTag settingsData;

    @Override
    public void upstreamNBTMessage(int group, CompoundTag compoundTag) {
        if (group == 0) { //common message
            if (compoundTag.contains("page")) {
                page.set(compoundTag.getInt("page"));

                int[] initStates = new int[]{0, 0, 0, 0, 0, 0};
                for (int i = 0; i < 36; ++i)
                    if (slots.get(i) instanceof BagSlot slot && slot.isActive())
                        initStates[i / 16] |= 1 << (i % 16);
                for (int i = 36; i < 72; ++i)
                    if (slots.get(i) instanceof TankSlot slot && slot.isActive())
                        initStates[i / 16] |= 1 << (i % 16);
                for (int i = 0; i < 6; ++i)
                    slotStates.set(i, initStates[i]);
            }
            if (compoundTag.contains("scroll")) {
                ds = compoundTag.getDouble("scroll");
                updateScroll();
            }
            if (compoundTag.contains("select")) {
                tankDataHandle().ifPresent(d -> {
                    int s = compoundTag.getInt("select");
                    if (s < 0 || selectedTank.get() == s + scroll || s + scroll > d.getTanks())
                        selectedTank.set(-1);
                    else
                        selectedTank.set(s + scroll);
                    d.select(selectedTank.get());
                });
            }
        }
        if (group == 1) { //settings message
            settingsData.merge(compoundTag);
        }
    }

    private LazyOptional<SlotData> slotDataHandle() {
        if (slotsHandle == null)
            slotsHandle = LazyOptional.empty();
        if (!slotsHandle.isPresent())
            BagsData.runOnBag(bagId, b-> slotsHandle = b.slotsHandle());
        return slotsHandle;
    }

    private LazyOptional<TankData> tankDataHandle() {
        if (tanksHandle == null)
            tanksHandle = LazyOptional.empty();
        if (!tanksHandle.isPresent())
            BagsData.runOnBag(bagId, b-> tanksHandle = b.tanksHandle());
        return tanksHandle;
    }

    void updateScroll() {
        if (page.page == 0)
            slotDataHandle().ifPresent(bagSlots->{
                int l = bagSlots.getSlots();
                int t = 0;
                if (l > 36)
                    t = 9 * (int) Math.round((double) ((l - 36) / 9 + 1) * ds);
                if (t != scroll) {
                    scroll = t;
                    int i;
                    for (i = 0; i + scroll < bagSlots.getSlots() && i < 36; ++i)
                        if (slots.get(i) instanceof BagSlot s)
                            s.changeSlotServerSide(bagSlots.getSlot(i + scroll));
                    for (; i < slots.size(); ++i)
                        if (slots.get(i) instanceof BagSlot s)
                            s.changeSlotServerSide(null);
                    slotStates.reload(slots);
                }
            });
        if (page.page == 1) {
            tankDataHandle().ifPresent(bagTanks->{
                int l = bagTanks.getTanks();
                int t = 0;
                if (l > 36)
                    t = 9 * (int) Math.round((double) ((l - 36) / 9 + 1) * ds);
                if (t != scroll) {
                    scroll = t;
                    int i;
                    for (i = 0; i + scroll < bagTanks.getTanks() && i < 36; ++i)
                        if (slots.get(i + 36) instanceof BagTankSlot s)
                            s.changeTankServerSide(bagTanks.getTank(i + scroll));
                    for (; i < slots.size(); ++i)
                        if (slots.get(i + 36) instanceof BagTankSlot s)
                            s.changeTankServerSide(null);
                    slotStates.reload(slots);
                }
            });
        }
    }

    private static class SlotStates implements ContainerData {
        int []data = {0, 0, 0, 0, 0, 0};

        @Override
        public int get(int index) { return data[index]; }

        @Override
        public void set(int index, int value) { data[index] = value; }

        @Override
        public int getCount() { return 6; }

        public boolean getActive(int index) { return (data[index / 16] & (1 << (index % 16))) != 0; }

        public void setActive(int index, boolean state) {
            if (getActive(index) != state)
                data[index / 16] ^= 1 << (index % 16);
        }

        public void reload(NonNullList<Slot> slots) {
            for (int i = 0; i < 72; ++i)
                if (slots.get(i) instanceof BagSlot s)
                    setActive(i, s.isActive());
        }
    }

    public static class Page extends DataSlot {

        private int page;
        private final BagMenu menu;

        Page(BagMenu parent) { menu = parent; }

        @Override
        public int get() { return page; }

        @Override
        public void set(int newPage) { page = newPage; }
    }

    private final SlotStates slotStates = new SlotStates();
    public final Page page = new Page(this);
    public final DataSlot selectedTank = DataSlot.standalone();

    public BagMenu(int id, Inventory playerInventory, FriendlyByteBuf buff) {
        this(id, playerInventory, 0, buff.readInt());
        settingsData = buff.readAnySizeNbt();
    }

    ArrayList<Menus.SlotSection> sections = new ArrayList<>();

    public BagMenu(int id, Inventory playerInventory, int bagId, int index) {
        super(R_TYPE.get(), id);
        this.bagId = bagId;
        page.set(index);
        settingsData = BagsData.runOnBag(bagId, b->b.getModeData("Settings").orElse(new CompoundTag()), new CompoundTag());

        LazyOptional<SlotData> bagSlots = slotDataHandle();
        if (bagSlots.isPresent())
            for (int l = 0; l < 4; ++l)
                for (int c = 0; c < 9; ++c)
                    addSlot(new BagSlot(bagId, bagSlots.resolve().get().getSlot(l * 9 + c), 29 + c * 18, 17 + l * 18, s -> page.page == 0 && s.getItemHandler().getSlots() > 0));
        else
            for (int l = 0; l < 4; ++l)
                for (int c = 0; c < 9; ++c) {
                    final int i = l * 9 + c;
                    addSlot(new BagSlot(29 + c * 18, 17 + l * 18, s -> page.page == 0 && slotStates.getActive(i)));
                }
        sections.add(new Menus.SlotSection(0, 35, false, false));

        LazyOptional<TankData> bagTanks = tankDataHandle();
        bagTanks.ifPresent(d->{
            selectedTank.set(d.getSelected());
            for (int l = 0; l < 4; ++l)
                for (int c = 0; c < 9; ++c) {
                    final int i = l * 9 + c;
                    addSlot(new BagTankSlot(bagId, d.getTank(l * 9 + c), 29 + c * 18, 17 + l * 18, s -> page.page == 1 && s.getTankHandler().getCapacity() > 0, () -> selectedTank.get() == i));
                }
        });
        if (!bagTanks.isPresent())
            for (int l = 0; l < 4; ++l)
                for (int c = 0; c < 9; ++c) {
                    final int i = l * 9 + c;
                    addSlot(new BagTankSlot(29 + c * 18, 17 + l * 18, s -> page.page == 1 && slotStates.getActive(i + 36), () -> selectedTank.get() == i));
                }
        sections.add(new Menus.SlotSection(36, 71, false, false));

        for (int i = 0; i < 4; ++i)
            addSlot(EquipSlot.armor(playerInventory.player, i, 7, 71 - 18 * i));

        for (int l = 0; l < 3; ++l)
            for (int j1 = 0; j1 < 9; ++j1)
                addSlot(new TogglableSlot(playerInventory, j1 + l * 9 + 9, 29 + j1 * 18, 102 + l * 18, s->page.get() < 2));

        sections.add(new Menus.SlotSection(72, slots.size() - 1, false, true));

        for(int i1 = 0; i1 < 9; ++i1)
            this.addSlot(new Slot(playerInventory, i1, 29 + i1 * 18, 160));

        addSlot(EquipSlot.shield(playerInventory.player, 7, 102));

        sections.add(new Menus.SlotSection(sections.get(2).toInclusive() + 1, slots.size() - 1, false, false));

        addDataSlots(slotStates);

        if (bagId != 0) {
            int[] initStates = new int[]{0, 0, 0, 0, 0, 0};

            for (int i = 0; i < 36; ++i)
                if (slots.get(i) instanceof BagSlot slot && slot.isActive())
                    initStates[i / 16] |= 1 << (i % 16);
            for (int i = 36; i < 72; ++i)
                if (slots.get(i) instanceof TankSlot slot && slot.isActive())
                    initStates[i / 16] |= 1 << (i % 16);

            for (int i = 0; i < 6; ++i)
                slotStates.set(i, initStates[i]);
        }

        addDataSlot(page);
        addDataSlot(selectedTank);
    }

    boolean fluidsAreStackable(FluidStack f1, FluidStack f2) {
        return f1.isEmpty() || f2.isEmpty() || f1.isFluidEqual(f2);
    }

    @Override
    public void clicked(int slot, int button, @Nonnull ClickType type, @Nonnull Player player) {
        if (slot >= 0 && slot < slots.size() && slots.get(slot) instanceof TankSlot tank) {
            if (!tank.isActive())
                return;
            if (getCarried().isEmpty()) {
                FluidStack cmp = tank.getFluid();
                for (int i = 0; i < slots.size(); ++i) {
                    if (i == slot) continue;
                    Slot search = slots.get(i);
                    if (search.isActive() && !(search instanceof TankSlot) && !search.getItem().isEmpty()) {
                        if (search.getItem().getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map(h -> fluidsAreStackable(h.getFluidInTank(0), cmp)).orElse(false)) {
                            setCarried(search.remove(1));
                            break;
                        }
                    }
                }
            }
            if (getCarried().getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent())
                Menus.interactWithFluidSlot(player, getCarried(), tank, new InvWrapper(player.getInventory())).ifPresent(this::setCarried);
            return;
        }
        super.clicked(slot, button, type, player);
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(@Nonnull Player player, int index) {
        return Menus.quickMoveStack(this, player, index, sections);
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return player.containerMenu.containerId == containerId && bagId != 0 && (DimBag.getBagAccess(player, bagId, false, true, true) == bagId || (getCarried().getItem() instanceof BagItem && BagItem.getBagId(getCarried()) == bagId));
    }
}
