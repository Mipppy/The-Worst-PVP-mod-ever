package com.troll;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.Random;
import java.util.function.Predicate;
import java.util.Optional;

public class EpicpvpmodClient implements ClientModInitializer {
	private static final double DETECTION_DISTANCE = 15.0;
	private static final float SMOOTHING_FACTOR = 0.1F; // Adjust the smoothing factor as needed
	private static final float ADJUSTMENT_AMOUNT = 0.3F; // Adjust this value for how much to move away
	private static final Random RANDOM = new Random(); // Random instance for direction selection

	@Override
	public void onInitializeClient() {
	}

	private Entity getEntityLookingAt(ClientPlayerEntity player, double maxDistance) {
		Vec3d start = player.getCameraPosVec(1.0F);
		Vec3d direction = player.getRotationVec(1.0F);
		Vec3d end = start.add(direction.multiply(maxDistance));

		// Perform the raycast
		Box box = player.getBoundingBox().stretch(direction.multiply(maxDistance)).expand(1.0);
		EntityHitResult entityHitResult = raycastEntities(player, start, end, box, entity -> true, maxDistance);

		if (entityHitResult != null && entityHitResult.getType() == HitResult.Type.ENTITY) {
			return entityHitResult.getEntity();
		}

		return null;
	}

	private EntityHitResult raycastEntities(Entity origin, Vec3d start, Vec3d end, Box box, Predicate<Entity> predicate, double distance) {
		double closestDistance = distance;
		Entity closestEntity = null;
		Vec3d hitPos = null;

		for (Entity entity : origin.getWorld().getOtherEntities(origin, box, predicate)) {
			Box entityBox = entity.getBoundingBox().expand(entity.getTargetingMargin());
			Optional<Vec3d> optionalHitPos = entityBox.raycast(start, end);

			if (optionalHitPos.isPresent()) {
				Vec3d hitPosition = optionalHitPos.get();
				double entityDistance = start.squaredDistanceTo(hitPosition);

				if (entityDistance < closestDistance) {
					closestDistance = entityDistance;
					closestEntity = entity;
					hitPos = hitPosition;
				}
			}
		}

		return closestEntity == null ? null : new EntityHitResult(closestEntity, hitPos);
	}

	private void adjustPlayerView(ClientPlayerEntity player, Entity target) {
		Vec3d playerPos = player.getPos();
		Vec3d targetPos = target.getPos();
		Vec3d directionToTarget = targetPos.subtract(playerPos).normalize();

		// Calculate the right and left vectors perpendicular to the direction to the target
		Vec3d rightVector = new Vec3d(-directionToTarget.z, 0, directionToTarget.x).normalize();
		Vec3d leftVector = new Vec3d(directionToTarget.z, 0, -directionToTarget.x).normalize();

		// Randomly choose to adjust the view to the right or left
		Vec3d adjustedDirection;
		if (RANDOM.nextBoolean()) {
			adjustedDirection = directionToTarget.add(rightVector.multiply(ADJUSTMENT_AMOUNT)).normalize();
		} else {
			adjustedDirection = directionToTarget.add(leftVector.multiply(-ADJUSTMENT_AMOUNT)).normalize();
		}

		// Convert the adjusted direction to yaw and pitch
		float[] newYawPitch = directionToYawPitch(adjustedDirection);

		// Smoothly adjust the player's yaw and pitch
		player.setYaw(lerp(player.getYaw(), newYawPitch[0], SMOOTHING_FACTOR));
		player.setPitch(lerp(player.getPitch(), newYawPitch[1], SMOOTHING_FACTOR));
	}

	private float[] directionToYawPitch(Vec3d direction) {
		double yaw = Math.atan2(direction.x, direction.z);
		double pitch = Math.atan2(-direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z));

		// Convert from radians to degrees
		yaw = Math.toDegrees(yaw) + 90.0;
		pitch = Math.toDegrees(pitch);

		// Normalize yaw to be in the range of 0-360 degrees
		if (yaw < 0) {
			yaw += 360.0;
		}

		// Pitch should be clamped to [-90, 90] degrees
		pitch = Math.max(-90.0, Math.min(90.0, pitch));

		return new float[]{(float) yaw, (float) pitch};
	}

	private float lerp(float start, float end, float t) {
		return start + t * (end - start);
	}
}
