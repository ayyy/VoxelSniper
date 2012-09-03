package com.thevoxelbox.voxelsniper.brush;

import com.thevoxelbox.voxelsniper.vMessage;
import com.thevoxelbox.voxelsniper.brush.perform.PerformBrush;

/**
 * 
 * @author Voxel
 */
public class Snipe extends PerformBrush {

    private static int timesUsed = 0;

    public Snipe() {
        this.name = "Snipe";
    }

    @Override
    public final int getTimesUsed() {
        return Snipe.timesUsed;
    }

    @Override
    public final void info(final vMessage vm) {
        vm.brushName(this.name);
    }

    @Override
    public final void setTimesUsed(final int tUsed) {
        Snipe.timesUsed = tUsed;
    }

    @Override
    protected final void arrow(final com.thevoxelbox.voxelsniper.vData v) {
        this.current.perform(this.tb);
        v.storeUndo(this.current.getUndo());
    }

    @Override
    protected final void powder(final com.thevoxelbox.voxelsniper.vData v) {
        this.current.perform(this.lb);
        v.storeUndo(this.current.getUndo());
    }
}
