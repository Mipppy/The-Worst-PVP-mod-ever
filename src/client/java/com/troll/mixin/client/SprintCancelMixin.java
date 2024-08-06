package com.troll.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ClientPlayerEntity.class)
public class SprintCancelMixin {
    private static final Random RANDOM = new Random();
    private static final float BASE_CHANCE = 0.01F; // 15% chance to cancel sprint
    private static final float CHANCE_INCREMENT = 0.02F; // Incremental increase per second
    private static final int MAX_CHANCE = 1; // 100% maximum chance to cancel

    private int sprintTime = 0; // Time in ticks (20 ticks = 1 second)
    private boolean isSprinting = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player != null) {
            if (player.isSprinting()) {
                if (!isSprinting) {
                    // Start tracking sprint time
                    isSprinting = true;
                    sprintTime = 0;
                }
                sprintTime++;
                // Check if the sprint should be cancelled
                float chance = Math.min(BASE_CHANCE + (CHANCE_INCREMENT * (sprintTime / 20)), MAX_CHANCE); // Convert sprintTime from ticks to seconds
                if (RANDOM.nextFloat() < chance) {
                    // Cancel sprint
                    player.setSprinting(false);
                    isSprinting = false;
                    System.out.println("Sprint canceled.");
                }
            } else {
                // Reset when not sprinting
                if (isSprinting) {
                    isSprinting = false;
                    sprintTime = 0;
                }
            }
        }
    }
}
