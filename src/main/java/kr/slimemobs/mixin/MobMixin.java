package kr.slimemobs.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {
    @Unique private int slimeMobs$hopCooldown = 0;
    @Unique private int slimeMobs$generation = 0;

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void slimeMobs$hopLikeSlime(CallbackInfo ci) {
        Mob self = (Mob)(Object)this;
        if (slimeMobs$hopCooldown > 0) slimeMobs$hopCooldown--;

        if (self.onGround() && slimeMobs$hopCooldown <= 0 && !self.isPassenger()) {
            Vec3 look = self.getLookAngle();
            Vec3 v = self.getDeltaMovement();
            double horizontal = 0.28D;
            self.setDeltaMovement(v.x + look.x * horizontal, 0.48D, v.z + look.z * horizontal);
            slimeMobs$hopCooldown = 7 + self.getRandom().nextInt(7);
        }
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void slimeMobs$splitOnDeath(Entity.RemovalReason reason, CallbackInfo ci) {
        Mob self = (Mob)(Object)this;
        if (reason != Entity.RemovalReason.KILLED || self.level().isClientSide()) return;
        if (!(self.level() instanceof ServerLevel level)) return;
        if (slimeMobs$generation >= 2) return;

        int count = 2 + self.getRandom().nextInt(3);
        for (int i = 0; i < count; i++) {
            Entity made = self.getType().create(level, MobSpawnType.MOB_SUMMONED);
            if (!(made instanceof Mob child)) continue;

            ((MobMixin)(Object)child).slimeMobs$generation = slimeMobs$generation + 1;
            double ox = (self.getRandom().nextDouble() - 0.5D) * 1.4D;
            double oz = (self.getRandom().nextDouble() - 0.5D) * 1.4D;
            child.setPos(self.getX() + ox, self.getY() + 0.2D, self.getZ() + oz);
            child.setDeltaMovement(ox * 0.2D, 0.4D, oz * 0.2D);
            level.addFreshEntity(child);
        }
    }
}
