package com.troll.mixin.client;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mouse.class)
public interface MouseAccessor {
    @Accessor("cursorDeltaX")
    void setCursorDeltaX(double cursorDeltaX);

    @Accessor("cursorDeltaY")
    void setCursorDeltaY(double cursorDeltaY);
}
