package kr.slimemobs.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
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

}
