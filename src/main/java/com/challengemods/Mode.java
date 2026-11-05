package com.challengemods;

public enum Mode {
    RED_LIGHT("Red Light, Green Light",
            "Ampel oben: Gruen = laufen, Orange = Warnung, Rot = stehen bleiben. Wer sich bei Rot bewegt, stirbt."),
    RANDOM_DROPS("Block-Randomizer",
            "Jeder Block droppt pro Welt ein festes, zufaelliges anderes Item."),
    CHUNK_SWAP("Chunk-Tausch (10s)",
            "Alle 10 Sekunden wird dein Chunk komplett zu einem zufaelligen Block. Portale bleiben heil."),
    WORLD_REMIX("Welt-Remix",
            "Alle Bloecke der Weltgenerierung werden durch zufaellige andere Bloecke ersetzt. Portale bleiben heil.");

    public final String title;
    public final String description;

    Mode(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
