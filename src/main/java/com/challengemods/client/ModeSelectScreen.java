package com.challengemods.client;

import com.challengemods.Mode;
import com.challengemods.PendingModes;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ModeSelectScreen extends Screen {
    private final Screen parent;

    public ModeSelectScreen(Screen parent) {
        super(Text.literal("Challenge-Modi"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = 240;
        int x = this.width / 2 - w / 2;
        int y = this.height / 6 + 10;
        for (Mode m : Mode.values()) {
            addDrawableChild(ButtonWidget.builder(label(m), b -> {
                        if (!PendingModes.selected.remove(m)) PendingModes.selected.add(m);
                        b.setMessage(label(m));
                    })
                    .dimensions(x, y, w, 20)
                    .tooltip(Tooltip.of(Text.literal(m.description)))
                    .build());
            y += 26;
        }
        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> close())
                .dimensions(x, y + 10, w, 20).build());
    }

    private static Text label(Mode m) {
        boolean on = PendingModes.selected.contains(m);
        return Text.literal(m.title + ": " + (on ? "AN" : "AUS"))
                .formatted(on ? Formatting.GREEN : Formatting.WHITE);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Gilt fuer die Welt, die du gleich erstellst."), this.width / 2, 34, 0xAAAAAA);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
