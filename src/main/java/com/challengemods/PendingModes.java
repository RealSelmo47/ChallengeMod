package com.challengemods;

import java.util.EnumSet;
import java.util.Set;

/** Auswahl aus dem Welt-Erstellen-Bildschirm. Wird beim Start der neuen Welt uebernommen. */
public final class PendingModes {
    public static final Set<Mode> selected = EnumSet.noneOf(Mode.class);
    public static volatile boolean armed = false;

    private PendingModes() {}

    public static void reset() {
        selected.clear();
        armed = false;
    }
}
