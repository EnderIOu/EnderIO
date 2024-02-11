package crazypants.enderio.base.item.travelstaff;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import crazypants.enderio.api.IModObject;
import crazypants.enderio.api.capacitor.ICapacitorKey;
import crazypants.enderio.api.teleport.TravelSource;
import crazypants.enderio.api.upgrades.IEquipmentData;
import crazypants.enderio.base.EnderIOTab;
import crazypants.enderio.base.teleport.TravelController;
import info.loenwind.autoconfig.factory.IValue;

public class ItemTeleportationStaff extends ItemTravelStaff {

    protected ItemTeleportationStaff(@NotNull IModObject modObject) {
        super(modObject);
        setMaxStackSize(1);
        setHasSubtypes(false);
        setCreativeTab(EnderIOTab.tabEnderIOItems);
    }

    public static ItemTeleportationStaff create(@NotNull IModObject modObject, @Nullable Block block) {
        return new ItemTeleportationStaff(modObject);
    }

    @NotNull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@NotNull World world, @NotNull EntityPlayer player,
                                                    @NotNull EnumHand hand) {
        ItemStack equipped = player.getHeldItem(hand);
        if (world.isRemote) {
            if (player.isSneaking()) {
                TravelController.activateTravelAccessable(equipped, hand, world, player, TravelSource.TELEPORT_STAFF);
            } else {
                TravelController.doTeleport(equipped, hand, player);
            }
        }
        player.swingArm(hand);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, equipped);
    }

    @Override
    public void getSubItems(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> list) {
        ItemStack is = new ItemStack(this);
        list.add(is);
    }

    @Override
    public void addCommonEntries(@Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer,
                                 @Nonnull List<String> list, boolean flag) {}

    @Override
    public void addBasicEntries(@Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer,
                                @Nonnull List<String> list, boolean flag) {}

    @Override
    public void addDetailedEntries(@Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer,
                                   @Nonnull List<String> list, boolean flag) {}

    @Override
    public void renderItemOverlayIntoGUI(@NotNull ItemStack stack, int xPosition, int yPosition) {}

    @Override
    public boolean isActive(@NotNull EntityPlayer ep, @NotNull ItemStack equipped) {
        return true;
    }

    @Override
    public void extractInternal(@NotNull ItemStack item, int power) {
        // do nothing, infinite energy
    }

    @Override
    public void extractInternal(@NotNull ItemStack item, IValue<Integer> power) {
        // do nothing, infinite energy
    }

    @Override
    public int getEnergyStored(@NotNull ItemStack item) {
        return 0; // infinite energy
    }

    @NotNull
    @Override
    public IEquipmentData getEquipmentData() {
        return null;
    }

    @NotNull
    @Override
    public ICapacitorKey getEnergyStorageKey(@NotNull ItemStack stack) {
        return null;
    }

    @NotNull
    @Override
    public ICapacitorKey getEnergyInputKey(@NotNull ItemStack stack) {
        return null;
    }

    @NotNull
    @Override
    public ICapacitorKey getEnergyUseKey(@NotNull ItemStack stack) {
        return null;
    }

    @NotNull
    @Override
    public ICapacitorKey getAbsorptionRatioKey(@NotNull ItemStack stack) {
        return null;
    }
}
