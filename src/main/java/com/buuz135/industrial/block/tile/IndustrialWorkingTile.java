package com.buuz135.industrial.block.tile;

import com.buuz135.industrial.proxy.client.IndustrialAssetProvider;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.block.tile.MachineTile;
import com.hrznstudio.titanium.client.screen.addon.EnergyBarScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.component.progress.ProgressBarComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;

public abstract class IndustrialWorkingTile<T extends IndustrialWorkingTile<T>> extends MachineTile<T> {

    @Save
    private ProgressBarComponent<T> workingBar;

    public IndustrialWorkingTile(BasicTileBlock<T> basicTileBlock) {
        super(basicTileBlock);
        this.addGuiAddonFactory(() -> new EnergyBarScreenAddon(10, 20, getEnergyStorage()));
        this.addProgressBar(workingBar = new ProgressBarComponent<T>(30, 20, 0, getMaxProgress())
                .setComponentHarness(this.getSelf())
                .setBarDirection(ProgressBarComponent.BarDirection.VERTICAL_UP)
                .setIncreaseType(false)
                .setOnFinishWork(() -> {
                    if (isServer()) {
                        WorkAction work = work();
                        workingBar.setProgress((int) (workingBar.getMaxProgress() * work.getWorkAmount()));
                        this.getEnergyStorage().extractEnergyForced(work.getEnergyConsumed());
                    }
                })
                .setOnStart(() -> {
                    workingBar.setMaxProgress(getMaxProgress());
                })
                .setCanReset(tileEntity -> true)
                .setCanIncrease(tileEntity -> true)
                .setColor(DyeColor.LIME));
    }

    @Override
    public ActionResultType onActivated(PlayerEntity playerIn, Hand hand, Direction facing, double hitX, double hitY, double hitZ) {
        if (super.onActivated(playerIn, hand, facing, hitX, hitY, hitZ) == ActionResultType.SUCCESS)
            return ActionResultType.SUCCESS;
        openGui(playerIn);
        return ActionResultType.PASS;
    }

    public abstract WorkAction work();

    public boolean hasEnergy(int amount) {
        return this.getEnergyStorage().getEnergyStored() >= amount;
    }

    public int getMaxProgress() {
        return 100;
    }

    @Override
    public IAssetProvider getAssetProvider() {
        return IndustrialAssetProvider.INSTANCE;
    }

    public class WorkAction {
        private final float workAmount;
        private final int energyConsumed;

        public WorkAction(float workAmount, int energyConsumed) {
            this.workAmount = workAmount;
            this.energyConsumed = energyConsumed;
        }

        public float getWorkAmount() {
            return workAmount;
        }

        public int getEnergyConsumed() {
            return energyConsumed;
        }
    }
}
