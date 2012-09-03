package com.thevoxelbox.voxelsniper.brush;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;

import com.thevoxelbox.voxelsniper.vData;
import com.thevoxelbox.voxelsniper.vMessage;
import com.thevoxelbox.voxelsniper.brush.perform.PerformBrush;

/**
 * 
 * @author psanker FOR ANY BRUSH THAT USES A SPLINE, EXTEND THAT BRUSH FROM THIS BRUSH!!! That way, the spline calculations are already there. Also, the UI for
 *         the splines will be included.
 * 
 */
public class Spline extends PerformBrush {

    // Vector class for splines
    protected class Point {

        int x;
        int y;
        int z;

        public Point(final Block b) {
            this.x = b.getX();
            this.y = b.getY();
            this.z = b.getZ();
        }

        public Point(final int x, final int y, final int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public final Point add(final Point p) {
            return new Point(this.x + p.x, this.y + p.y, this.z + p.z);
        }

        public final Point multiply(final int scalar) {
            return new Point(this.x * scalar, this.y * scalar, this.z * scalar);
        }

        public final Point subtract(final Point p) {
            return new Point(this.x - p.x, this.y - p.y, this.z - p.z);
        }
    }

    private final ArrayList<Block> endPts = new ArrayList<Block>();
    private final ArrayList<Block> ctrlPts = new ArrayList<Block>();
    protected ArrayList<Point> spline = new ArrayList<Point>();
    protected boolean set;
    protected boolean ctrl;

    protected String[] sparams = { "ss", "sc", "clear" };

    private static int timesUsed = 0;

    public Spline() {
        this.name = "Spline";
    }

    public final void addToSet(final vData v, final boolean ep) {
        if (ep) {
            if (this.endPts.contains(this.tb) || this.endPts.size() == 2) {
                return;
            }

            this.endPts.add(this.tb);
            v.sendMessage(ChatColor.GRAY + "Added block " + ChatColor.RED + "(" + this.bx + ", " + this.by + ", " + this.bz + ") " + ChatColor.GRAY
                    + "to endpoint selection");
            return;
        }

        if (this.ctrlPts.contains(this.tb) || this.ctrlPts.size() == 2) {
            return;
        }

        this.ctrlPts.add(this.tb);
        v.sendMessage(ChatColor.GRAY + "Added block " + ChatColor.RED + "(" + this.bx + ", " + this.by + ", " + this.bz + ") " + ChatColor.GRAY
                + "to control point selection");
    }

    @Override
    public final int getTimesUsed() {
        return Spline.timesUsed;
    }

    @Override
    public final void info(final vMessage vm) {
        vm.brushName(this.name);

        if (this.set) {
            vm.custom(ChatColor.GRAY + "Endpoint selection mode ENABLED.");
        } else if (this.ctrl) {
            vm.custom(ChatColor.GRAY + "Control point selection mode ENABLED.");
        } else {
            vm.custom(ChatColor.AQUA + "No selection mode enabled.");
        }
    }

    @Override
    public final void parameters(final String[] par, final com.thevoxelbox.voxelsniper.vData v) {
        if (par[1].equalsIgnoreCase("info")) {
            v.sendMessage(ChatColor.GOLD + "Spline brush parameters");
            v.sendMessage(ChatColor.AQUA + "ss: Enable endpoint selection mode for desired curve");
            v.sendMessage(ChatColor.AQUA + "sc: Enable control point selection mode for desired curve");
            v.sendMessage(ChatColor.AQUA + "clear: Clear out the curve selection");
            v.sendMessage(ChatColor.AQUA + "ren: Render curve from control points");
            return;
        }

        for (int i = 1; i < par.length; i++) {
            if (par[i].equalsIgnoreCase("sc")) {
                if (!this.ctrl) {
                    this.set = false;
                    this.ctrl = true;
                    v.sendMessage(ChatColor.GRAY + "Control point selection mode ENABLED.");
                    continue;
                } else {
                    this.ctrl = false;
                    v.sendMessage(ChatColor.AQUA + "Control point selection mode disabled.");
                    continue;
                }

            } else if (par[i].equalsIgnoreCase("ss")) {
                if (!this.set) {
                    this.set = true;
                    this.ctrl = false;
                    v.sendMessage(ChatColor.GRAY + "Endpoint selection mode ENABLED.");
                    continue;
                } else {
                    this.set = false;
                    v.sendMessage(ChatColor.AQUA + "Endpoint selection mode disabled.");
                    continue;
                }

            } else if (par[i].equalsIgnoreCase("clear")) {
                this.clear(v);

            } else if (par[i].equalsIgnoreCase("ren")) {
                if (this.spline(new Point(this.endPts.get(0)), new Point(this.endPts.get(1)), new Point(this.ctrlPts.get(0)), new Point(this.ctrlPts.get(1)), v)) {
                    this.render(v);
                }
            } else {
                v.sendMessage(ChatColor.RED + "Invalid brush parameters! use the info parameter to display parameter info.");
            }
        }
    }

    public final void removeFromSet(final vData v, final boolean ep) {
        if (ep) {
            if (this.endPts.contains(this.tb) == false) {
                v.sendMessage(ChatColor.RED + "That block is not in the endpoint selection set.");
                return;
            }

            this.endPts.add(this.tb);
            v.sendMessage(ChatColor.GRAY + "Removed block " + ChatColor.RED + "(" + this.bx + ", " + this.by + ", " + this.bz + ") " + ChatColor.GRAY
                    + "from endpoint selection");
            return;
        }

        if (this.ctrlPts.contains(this.tb) == false) {
            v.sendMessage(ChatColor.RED + "That block is not in the control point selection set.");
            return;
        }

        this.ctrlPts.remove(this.tb);
        v.sendMessage(ChatColor.GRAY + "Removed block " + ChatColor.RED + "(" + this.bx + ", " + this.by + ", " + this.bz + ") " + ChatColor.GRAY
                + "from control point selection");
    }

    @Override
    public final void setTimesUsed(final int tUsed) {
        Spline.timesUsed = tUsed;
    }

    public final boolean spline(final Point start, final Point end, final Point c1, final Point c2, final vData v) {
        this.spline.clear();

        try {
            final Point c = (c1.subtract(start)).multiply(3);
            final Point b = ((c2.subtract(c1)).multiply(3)).subtract(c);
            final Point a = ((end.subtract(start)).subtract(c)).subtract(b);

            for (double t = 0.0; t < 1.0; t += 0.01) {
                final int px = (int) Math.round((a.x * (t * t * t)) + (b.x * (t * t)) + (c.x * t) + this.endPts.get(0).getX());
                final int py = (int) Math.round((a.y * (t * t * t)) + (b.y * (t * t)) + (c.y * t) + this.endPts.get(0).getY());
                final int pz = (int) Math.round((a.z * (t * t * t)) + (b.z * (t * t)) + (c.z * t) + this.endPts.get(0).getZ());

                if (!this.spline.contains(new Point(px, py, pz))) {
                    this.spline.add(new Point(px, py, pz));
                }
            }

            return true;
        } catch (final Exception e) {
            v.sendMessage(ChatColor.RED + "Not enough points selected; " + this.endPts.size() + " endpoints, " + this.ctrlPts.size() + " control points");
            return false;
        }
    }

    @Override
    protected final void arrow(final com.thevoxelbox.voxelsniper.vData v) {
        this.bx = this.tb.getX();
        this.by = this.tb.getY();
        this.bz = this.tb.getZ();

        if (this.set) {
            this.removeFromSet(v, true);
        } else if (this.ctrl) {
            this.removeFromSet(v, false);
        }
    }

    protected final void clear(final vData v) {
        this.spline.clear();
        this.ctrlPts.clear();
        this.endPts.clear();
        v.sendMessage(ChatColor.GRAY + "Bezier curve cleared.");
    }

    @Override
    protected final void powder(final com.thevoxelbox.voxelsniper.vData v) {
        this.bx = this.tb.getX();
        this.by = this.tb.getY();
        this.bz = this.tb.getZ();

        if (this.set) {
            this.addToSet(v, true);
        }
        if (this.ctrl) {
            this.addToSet(v, false);
        }
    }

    protected final void render(final vData v) {
        if (this.spline.isEmpty()) {
            return;
        }

        for (final Point pt : this.spline) {
            this.current.perform(this.clampY(pt.x, pt.y, pt.z));
        }

        v.storeUndo(this.current.getUndo());
    }
}
