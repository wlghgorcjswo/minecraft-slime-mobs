package kr.slimemobs.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique private int slimeMobs$generation = 0;
    @Unique private boolean slimeMobs$alreadySplit = false;

    @Inject(method = "die", at = @At("HEAD"))
    private void slimeMobs$splitOnDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof Mob)) return;
        if (self.level().isClientSide()) return;
        if (slimeMobs$alreadySplit || slimeMobs$generation >= 2) return;
        if (!(self.level() instanceof ServerLevel level)) return;

        slimeMobs$alreadySplit = true;
        int count = 2 + self.getRandom().nextInt(3);

        for (int i = 0; i < count; i++) {
            Entity made = self.getType().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (!(made instanceof Mob child)) continue;

            if ((Object) child instanceof LivingEntityMixin childMixin) {
                childMixin.slimeMobs$generation = slimeMobs$generation + 1;
            }

            double ox = (self.getRandom().nextDouble() - 0.5D) * 1.4D;
            double oz = (self.getRandom().nextDouble() - 0.5D) * 1.4D;
            child.setPos(self.getX() + ox, self.getY() + 0.2D, self.getZ() + oz);
            child.setYRot(self.getYRot() + self.getRandom().nextFloat() * 90.0F - 45.0F);
            child.setDeltaMovement(ox * 0.25D, 0.42D, oz * 0.25D);
            level.addFreshEntity(child);
        }
    }
}
