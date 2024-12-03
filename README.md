# DropHeads-OG

 A fork of [DropHeads](https://github.com/EvModder/DropHeads) that builds (TM).
 
 Maintained for 1.19.4 by [TrueOG Network](https://true-og.net).
 
## Other changes from DropHeads:

- Uses a custom version of EvLib, [EvLib-OG](https://github.com/true-og/EvLib-OG).

- Removed plugin updater.

- Removed bStats.

- Replace proprietary Mojang AuthLib API.

- Replace netty code with ProtocolLib.

- Replace APIs deprecated in Purpur 1.19.4.

**Main configuration file:**<br>
[config.yml](./config.yml)
<br>
<br>
**Default drop chances:**<br>
[head-drop-rates.txt](./head-drop-rates.txt)
<br>
<br>
**Drop chance adjustments due to SpawnReason** (chunk_gen, breeding, spawners, etc.)**:**<br>
[spawn-cause-modifiers.txt](./spawn-cause-modifiers.txt)
<br>
<br>
**Mob head textures** (from [minecraft-heads](https://minecraft-heads.com/))**:**<br>
[head-textures.txt](./head-textures.txt)

**Javadocs** ([available here](https://evmodder.github.io/DropHeads/net/evmodder/DropHeads/events/package-summary.html))**:**<br>
For plugin developers interested in hooking into the DropHeads API

## License:

[LGPLv3](https://github.com/true-og/DropHeads-OG/blob/master/LICENSE)