package com.puggicorn.perseverance.brewing.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.puggicorn.perseverance.brewing.effect.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class ClimbingEffectMixin{

    @Shadow
    private Optional<BlockPos> lastClimbablePos;

    @Unique
    private boolean perseverance_brewing$isMoving;

    @Inject(method = "travel", at = @At("HEAD"))
    private void perseverance_brewing$captureInputs(Vec3 travelVector, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        this.perseverance_brewing$isMoving = entity.zza != 0.0F || entity.xxa != 0.0F;
    }

    @ModifyReturnValue(method = "onClimbable", at = @At("RETURN"))
    private boolean perseverance_brewing$modifyClimbing(boolean original) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (original) {
            return true;
        }

        if (entity.isSpectator() || !entity.hasEffect(ModEffects.CLIMBING)) {
            return false;
        }

        AABB currentBox = entity.getBoundingBox();
        AABB expandedBox = currentBox.inflate(0.15D, 0.0D, 0.15D);

        boolean isNextToWall = !entity.level().noCollision(entity, expandedBox);
        if (!isNextToWall) {
            return false;
        }

        this.lastClimbablePos = Optional.of(entity.blockPosition());
        entity.fallDistance = 0.0F;

        if (entity instanceof Player player && player.isShiftKeyDown()) {
            Vec3 movement = player.getDeltaMovement();

            if (!this.perseverance_brewing$isMoving && movement.y <= 0.0D) {
                player.setDeltaMovement(movement.x, 0.0D, movement.z);
            }
        }

        return true;
    }
}
