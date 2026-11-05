# Challenge Mods (Fabric, Minecraft 1.21.1)

Beim Welt-Erstellen gibt es oben links einen Button **"Modus"**. Dort kannst du jeden Modus einzeln an-/ausschalten (auch kombinierbar):

| Modus | Was passiert |
|---|---|
| Red Light, Green Light | Ampel oben (Rot/Orange/Gruen, zufaellige Dauer). Bewegung bei Rot = Tod. |
| Block-Randomizer | Jeder Block droppt pro Welt ein festes zufaelliges Item (neue Welt = neue Zuordnung). |
| Chunk-Tausch (10s) | Alle 10 s wird dein Chunk komplett zu EINEM zufaelligen Block (Luft bleibt, Portale/Obsidian/Bedrock bleiben). |
| Welt-Remix | Jeder Block der Weltgenerierung wird durch einen festen zufaelligen anderen Block ersetzt (Gras -> X, Eiche -> Y ...). Portale, Endportalrahmen, Obsidian, Bedrock bleiben. |

## Bauen
1. JDK 21 installieren.
2. Im Projektordner: `gradle build` (Gradle 8.10+)  
   ODER: die offizielle Fabric-Example-Mod-Vorlage (https://github.com/FabricMC/fabric-example-mod, Branch 1.21) herunterladen und `src/` + `gradle.properties` + `build.gradle` von hier hineinkopieren, dann `./gradlew build`.
3. Fertige Datei: `build/libs/challengemods-1.0.0.jar` -> in den `mods`-Ordner (Fabric Loader 0.16+ und Fabric API fuer 1.21.1 noetig).

## Hinweise
- Einzelspieler/LAN (Auswahl ueber das Welt-Erstellen-Menue). Der Mod muss beim Spieler installiert sein.
- Die Auswahl wird in `challengemods.json` im Weltordner gespeichert.
- Konfig-Stellschrauben (Ampel-Zeiten etc.) stehen oben in `RedLight.java`, `ChunkSwap.java`, `ModeManager.java`.
