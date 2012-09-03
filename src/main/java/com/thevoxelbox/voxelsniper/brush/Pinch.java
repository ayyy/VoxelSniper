package com.thevoxelbox.voxelsniper.brush;

import org.bukkit.block.Block;

import com.thevoxelbox.voxelsniper.vMessage;

/**
 * 
 * @author Voxel
 */
public class Pinch extends Brush {

    private class pBlock {

        public Block b;
        public int i;
        public byte d;

        public pBlock(final Block bl) {
            this.b = bl;
            this.i = bl.getTypeId();
            this.d = bl.getData();
        }
    }

    private boolean[][][] area;

    private static int timesUsed = 0;

    public Pinch() {
        this.name = "Pinch";
    }

    @Override
    public final int getTimesUsed() {
        return Pinch.timesUsed;
    }

    @Override
    public final void info(final vMessage vm) {
        vm.brushName(this.name);
    }

    @Override
    public final void setTimesUsed(final int tUsed) {
        Pinch.timesUsed = tUsed;
    }
}
