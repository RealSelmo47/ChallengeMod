package com.challengemods.mixin.client;

import com.challengemods.PendingModes;
import com.challengemods.client.ModeSelectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {
    protected CreateWorldScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void challengemods$reset(CallbackInfo ci) {
        PendingModes.reset();
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void challengemods$addModeButton(CallbackInfo ci) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Modus"),
                        b -> this.client.setScreen(new ModeSelectScreen((Screen) (Object) this)))
                .dimensions(6, 6, 60, 20).build());
    }

    @Inject(method = "createLevel", at = @At("HEAD"))
    private void challengemods$arm(CallbackInfo ci) {
        PendingModes.armed = true;
    }
}
