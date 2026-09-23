package kr.slimemobs.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {
 @Unique private int slimeMobs$hopCooldown=0;

 @Inject(method="aiStep", at=@At("TAIL"))
 private void slimeMobs$hopLikeSlime(CallbackInfo ci) {
   Mob self=(Mob)(Object)this;
   if (self instanceof net.minecraft.world.entity.monster.Slime) return;
   if (slimeMobs$hopCooldown>0) slimeMobs$hopCooldown--;
   if (self.onGround() && slimeMobs$hopCooldown<=0 && !self.isPassenger()) {
     Vec3 look=self.getLookAngle();
     Vec3 v=self.getDeltaMovement();
     double horizontal=0.23D;
     self.setDeltaMovement(v.x + look.x*horizontal, 0.42D, v.z + look.z*horizontal);
     self.hasImpulse=true;
     slimeMobs$hopCooldown=8 + self.getRandom().nextInt(8);
   }
 }

 @Inject(method="remove", at=@At("HEAD"))
 private void slimeMobs$split(Entity.RemovalReason reason, CallbackInfo ci) {
   Mob self=(Mob)(Object)this;
   if (self.level().isClientSide()) return;
   if (reason != Entity.RemovalReason.KILLED) return;
   if (!(self.level() instanceof ServerLevel level)) return;
   if (self instanceof net.minecraft.world.entity.monster.Slime) return;

   int generation=self.getPersistentData().getIntOr("SlimeMobsGeneration",0);
   if (generation>=2) return;
   EntityType<?> type=self.getType();
   int count=2+self.getRandom().nextInt(3);
   for(int i=0;i<count;i++) {
     Entity made=type.create(level, EntitySpawnReason.MOB_SUMMONED);
     if (!(made instanceof Mob child)) continue;
     child.getPersistentData().putInt("SlimeMobsGeneration",generation+1);
     double ox=(self.getRandom().nextDouble()-0.5D)*1.2D;
     double oz=(self.getRandom().nextDouble()-0.5D)*1.2D;
     child.setPos(self.getX()+ox,self.getY()+0.15D,self.getZ()+oz);
     child.setDeltaMovement(ox*0.25D,0.35D,oz*0.25D);
     level.addFreshEntity(child);
   }
 }
}
