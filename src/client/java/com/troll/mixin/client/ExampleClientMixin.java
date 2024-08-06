package com.troll.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Mixin(Mouse.class)
public class ExampleClientMixin {
	private static final float MIN_ADJUSTMENT_AMOUNT = 0.1F;
	private static final float MAX_ADJUSTMENT_AMOUNT = 0.3F;
	private static final Random RANDOM = new Random();
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	// Flag to enable additional checks
	private static final boolean ENABLE_ADDITIONAL_CHECKS = false;

	@Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
	private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
		System.out.println("onMouseButton injected.");  // Initial log to confirm injection

		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;

		if (player != null && button == 0 && action == 1) {
			if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
				EntityHitResult entityHitResult = (EntityHitResult) client.crosshairTarget;
				Entity target = entityHitResult.getEntity();

				// Check additional conditions if enabled
				if (ENABLE_ADDITIONAL_CHECKS) {
					if (!isValidTarget(client, target)) {
						System.out.println("Target is not valid based on additional checks.");
						ci.cancel();
						return;
					}
				}

				if (target != null) {
					// Decide action based on random chance
					double chance = RANDOM.nextDouble() * 100;

					if (chance < 7) {
						System.out.println("Action A: Move cursor then immediately click.");
						adjustCrosshairPosition(client);
						performImmediateClick(client, window, button);
						ci.cancel();
					} else if (chance < 14) {
						System.out.println("Action B: Wait 0.5 seconds then click.");
						scheduler.schedule(() -> performDelayedClick(client, window, button), 500, TimeUnit.MILLISECONDS);
						ci.cancel();
					} else if (chance < 20) {
						System.out.println("Action C: Move cursor then click after 0.5 seconds.");
						adjustCrosshairPosition(client);
						scheduler.schedule(() -> {
							performDelayedClick(client, window, button);
						}, 500, TimeUnit.MILLISECONDS);
						ci.cancel();
					} else {
						System.out.println("Action D: Nothing.");
					}
				}
			}
		}
	}

	private boolean isValidTarget(MinecraftClient client, Entity target) {
		// Check if the target is a player
		if (target instanceof PlayerEntity) {
			PlayerEntity targetPlayer = (PlayerEntity) target;
			// Get the position of the client player and target player
			Vec3d clientPos = client.player.getPos();
			Vec3d targetPos = targetPlayer.getPos();

			// Check distance to see if the target is within 35 blocks
			if (client.player.squaredDistanceTo(targetPos) < 35 * 35) {
				// Check the number of nearby players
				List<PlayerEntity> nearbyPlayers = client.world.getEntitiesByClass(PlayerEntity.class, client.player.getBoundingBox().expand(35), p -> p != client.player);
				if (nearbyPlayers.size() < 3) {
					return true; // Valid target
				}
			}
		}
		return false; // Invalid target
	}

	private void adjustCrosshairPosition(MinecraftClient client) {
		// Calculate small adjustment
		double deltaX = (RANDOM.nextDouble() * (MAX_ADJUSTMENT_AMOUNT - MIN_ADJUSTMENT_AMOUNT) + MIN_ADJUSTMENT_AMOUNT) * (RANDOM.nextBoolean() ? 1 : -1);
		double deltaY = (RANDOM.nextDouble() * (MAX_ADJUSTMENT_AMOUNT - MIN_ADJUSTMENT_AMOUNT) + MIN_ADJUSTMENT_AMOUNT) * (RANDOM.nextBoolean() ? 1 : -1);

		Mouse mouse = client.mouse;
		MouseAccessor accessor = (MouseAccessor) mouse;

		accessor.setCursorDeltaX(mouse.getX() + deltaX);
		accessor.setCursorDeltaY(mouse.getY() + deltaY);

		System.out.println("Adjusted crosshair position: deltaX=" + deltaX + ", deltaY=" + deltaY);
	}

	private void performImmediateClick(MinecraftClient client, long window, int button) {
		try {
			Mouse mouse = client.mouse;
			Method onMouseButton = Mouse.class.getDeclaredMethod("onMouseButton", long.class, int.class, int.class, int.class);
			onMouseButton.setAccessible(true);

			// Simulate mouse press and release
			onMouseButton.invoke(mouse, window, button, 1, 0);
			onMouseButton.invoke(mouse, window, button, 0, 0);

			System.out.println("Performed immediate click.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void performDelayedClick(MinecraftClient client, long window, int button) {
		try {
			Mouse mouse = client.mouse;
			Method onMouseButton = Mouse.class.getDeclaredMethod("onMouseButton", long.class, int.class, int.class, int.class);
			onMouseButton.setAccessible(true);

			// Simulate mouse press and release
			onMouseButton.invoke(mouse, window, button, 1, 0);
			onMouseButton.invoke(mouse, window, button, 0, 0);

			System.out.println("Performed delayed click.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
