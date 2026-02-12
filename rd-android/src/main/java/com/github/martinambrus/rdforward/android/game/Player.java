package com.github.martinambrus.rdforward.android.game;

import com.github.martinambrus.rdforward.render.RDInput;
import java.util.List;

public class Player {
    private Level level;
    private RDInput input;
    public float xo, yo, zo;
    public float x, y, z;
    public float xd, yd, zd;
    public float yRot, xRot;
    public AABB bb;
    public boolean onGround = false;

    public Player(Level level, RDInput input) {
        this.level = level;
        this.input = input;
        resetPos();
    }

    private void resetPos() {
        float px = (float) Math.random() * (float) level.width;
        float py = (float) (level.depth + 10);
        float pz = (float) Math.random() * (float) level.height;
        setPos(px, py, pz);
    }

    private void setPos(float x, float y, float z) {
        this.x = x; this.y = y; this.z = z;
        float w = 0.3F, h = 0.9F;
        this.bb = new AABB(x - w, y - h, z - w, x + w, y + h, z + w);
    }

    public void turn(float xo, float yo) {
        this.yRot += xo * 0.15F;
        this.xRot -= yo * 0.15F;
        if (this.xRot < -90.0F) this.xRot = -90.0F;
        if (this.xRot > 90.0F) this.xRot = 90.0F;
    }

    public void tick() {
        this.xo = this.x; this.yo = this.y; this.zo = this.z;
        float xa = 0.0F, ya = 0.0F;

        // R = reset (key code 82)
        if (input.isKeyDown(82)) resetPos();

        // W/UP
        if (input.isKeyDown(87) || input.isKeyDown(265)) ya--;
        // S/DOWN
        if (input.isKeyDown(83) || input.isKeyDown(264)) ya++;
        // A/LEFT
        if (input.isKeyDown(65) || input.isKeyDown(263)) xa--;
        // D/RIGHT
        if (input.isKeyDown(68) || input.isKeyDown(262)) xa++;
        // SPACE/SUPER
        if ((input.isKeyDown(32) || input.isKeyDown(343)) && this.onGround) {
            this.yd = 0.12F;
        }

        moveRelative(xa, ya, onGround ? 0.02F : 0.005F);
        this.yd -= 0.005F;
        move(this.xd, this.yd, this.zd);
        this.xd *= 0.91F; this.yd *= 0.98F; this.zd *= 0.91F;
        if (onGround) { this.xd *= 0.8F; this.zd *= 0.8F; }
    }

    public void move(float xa, float ya, float za) {
        float xaOrg = xa, yaOrg = ya, zaOrg = za;
        List<AABB> cubes = level.getCubes(bb.expand(xa, ya, za));
        for (AABB c : cubes) ya = c.clipYCollide(bb, ya);
        bb.move(0, ya, 0);
        for (AABB c : cubes) xa = c.clipXCollide(bb, xa);
        bb.move(xa, 0, 0);
        for (AABB c : cubes) za = c.clipZCollide(bb, za);
        bb.move(0, 0, za);
        this.onGround = yaOrg != ya && yaOrg < 0.0F;
        if (xaOrg != xa) this.xd = 0;
        if (yaOrg != ya) this.yd = 0;
        if (zaOrg != za) this.zd = 0;
        this.x = (bb.x0 + bb.x1) / 2.0F;
        this.y = bb.y0 + 1.62F;
        this.z = (bb.z0 + bb.z1) / 2.0F;
    }

    public void moveRelative(float xa, float za, float speed) {
        float dist = xa * xa + za * za;
        if (dist < 0.01F) return;
        dist = speed / (float) Math.sqrt(dist);
        xa *= dist; za *= dist;
        float sin = (float) Math.sin(yRot * Math.PI / 180.0);
        float cos = (float) Math.cos(yRot * Math.PI / 180.0);
        this.xd += xa * cos - za * sin;
        this.zd += za * cos + xa * sin;
    }
}
