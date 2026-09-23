package kr.slimemobs.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
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

            int childGeneration = slimeMobs$generation + 1;
            if ((Object) child instanceof LivingEntityMixin childMixin) {
                childMixin.slimeMobs$generation = childGeneration;
            }

            // Shrink hard on every split, like vanilla slimes.
            // First children are about half size, second generation about quarter size.
            if (child.getAttribute(Attributes.SCALE) != null) {
                double baseScale = child.getAttributeBaseValue(Attributes.SCALE);
                double sizeFactor = childGeneration == 1 ? 0.52D : 0.27D;
                child.getAttribute(Attributes.SCALE).setBaseValue(Math.max(0.1D, baseScale * sizeFactor));
            }

            // Each split generation is weaker, just like a smaller slime.
            double healthFactor = childGeneration == 1 ? 0.65D : 0.40D;
            double damageFactor = childGeneration == 1 ? 0.75D : 0.55D;
            if (child.getAttribute(Attributes.MAX_HEALTH) != null) {
                double base = child.getAttributeBaseValue(Attributes.MAX_HEALTH);
                child.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.max(1.0D, base * healthFactor));
                child.setHealth(child.getMaxHealth());
            }
            if (child.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                double base = child.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                child.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(Math.max(0.5D, base * damageFactor));
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
