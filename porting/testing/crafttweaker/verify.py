"""Exercise the recipe manager fixtures on a fresh Forge server using Auram's JARs."""

import argparse
import json
from pathlib import Path
import re
import shutil
import socket
import subprocess
import time


def prepare(profile, auram, jar, standalone):
    mods = profile / "mods"
    mods.mkdir(parents=True)
    (profile / "libraries").symlink_to(auram / "server/libraries", target_is_directory=True)
    dependencies = [
        "curios-forge-5.14.1+1.20.1.jar",
        "lithostitched-forge-1.20.1-1.4.11.jar",
    ]
    if not standalone:
        dependencies.extend([
            "CraftTweaker-forge-1.20.1-14.0.59.jar",
            "Bookshelf-Forge-1.20.1-20.2.15.jar",
            "GameStages-Forge-1.20.1-15.0.2.jar",
            "ItemStages-Forge-1.20.1-8.0.3.jar",
            "RecipeStages-8.0.0.2.jar",
        ])
        scripts = profile / "scripts"
        scripts.mkdir()
        fixtures = Path(__file__).resolve().parent
        for script in fixtures.glob("*.zs"):
            shutil.copy2(script, scripts)
        shutil.copytree(fixtures / "datapack", profile / "world/datapacks/thaumaturge-script-fixtures")
    for filename in dependencies:
        (mods / filename).symlink_to(auram / "minecraft/mods" / filename)
    shutil.copy2(jar, mods)
    with socket.socket() as listener:
        listener.bind(("127.0.0.1", 0))
        port = listener.getsockname()[1]
    (profile / "eula.txt").write_text("eula=true\n")
    (profile / "server.properties").write_text(
        f"server-ip=127.0.0.1\nserver-port={port}\nonline-mode=false\nenforce-secure-profile=false\n"
        "view-distance=3\nsimulation-distance=3\nlevel-type=minecraft:flat\n"
        'generator-settings={"layers":[{"block":"minecraft:bedrock","height":1},'
        '{"block":"minecraft:grass_block","height":1}],"biome":"minecraft:plains"}\n'
    )


def read(file):
    return file.read_text(errors="replace") if file.exists() else ""


def wait_for(process, logfile, pattern, count=1):
    deadline = time.monotonic() + 150
    while time.monotonic() < deadline:
        log = read(logfile)
        if log.count(pattern) >= count:
            return
        if process.poll() is not None:
            break
        time.sleep(0.2)
    raise RuntimeError(f"{logfile}: expected {count} occurrences of {pattern!r}\n{read(logfile)[-5000:]}")


def verify_recipes(log):
    errors = [line for line in log.splitlines() if "[ERROR]" in line]
    assert not errors, errors
    for family, rules in (("thaumatorium", 1), ("arcane_workbench", 2), ("crucible", 1), ("dust_trigger", 3),
                          ("research", 1), ("scan", 2), ("essentia_crystalizer", 1), ("spa", 2),
                          ("pattern_crafter", 1), ("infernal_furnace", 1), ("infusion", 1),
                          ("alchemical_furnace", 1), ("advanced_alchemical_furnace", 1),
                          ("deconstruction_table", 1), ("golem_assembly", 1),
                          ("golem_deployment", 1), ("golem_seal", 1), ("focus_inscription", 1),
                          ("spell_cast", 1), ("potion_sprayer", 1)):
        assert log.count(f"Adding a GameStages rule for thaumaturge:{family}") == 2 * rules, family
    assert log.count("Editing Thaumaturge data map thaumaturge:aspect/thaumaturge:crystallization") == 2
    for mode in ("mixing", "placement"):
        assert log.count(f"Editing Thaumaturge data map minecraft:fluid/thaumaturge:spa_{mode}") == 2
    for registry, machine in (("minecraft:block", "pattern_crafter"), ("minecraft:item", "infernal_bonus"),
                              ("minecraft:block", "infusion_matrix_upgrade"), ("minecraft:block", "smelter_stats"),
                              ("minecraft:item", "distillation"), ("minecraft:item", "advanced_distillation"),
                              ("minecraft:item", "deconstruction"), ("thaumaturge:golem_material", "golem_assembly"),
                              ("minecraft:item", "spell_focus"), ("minecraft:item", "focus_inscription"),
                              ("minecraft:potion", "potion_sprayer")):
        assert log.count(f"Editing Thaumaturge data map {registry}/thaumaturge:{machine}") == 2
    for marker in ("CRUCIBLE_REMOVED", "SPECIAL_REMOVED", "SPECIAL_SALIS", "SPECIAL_LABEL", "SPECIAL_PEARL",
                   "TAG_ASPECT", "TAG_MATERIAL"):
        assert log.count(f"TT_{marker}=true") == 2, marker

    def snapshots(marker):
        values = [json.loads(value) for value in re.findall(rf"TT_{marker}=(\{{.*\}})", log)]
        assert len(values) == 2 and values[0] == values[1], (marker, values)
        return values[0]

    for marker, result, amount in (
        ("STOCK", "thaumaturge:nitor_yellow", 10),
        ("ADD", "minecraft:diamond", 3),
        ("REPLACE", "minecraft:emerald", 5),
        ("READD", "minecraft:gold_ingot", 7),
    ):
        value = snapshots("CRUCIBLE_" + marker)
        assert value["result"]["id"] == result and value["aspects"][0]["amount"] == amount, value

    shaped = snapshots("PROCESS_SHAPED")
    assert shaped["pattern"] == [" I", "S "] and shaped["vis"] == 25
    assert snapshots("PROCESS_SHAPELESS")["type"] == "thaumaturge:arcane_workbench_shapeless"
    assert len(snapshots("PROCESS_INFUSION")["components"]) == 2
    assert snapshots("PROCESS_PATCH")["catalyst_patch"] == {"thaumaturge:fortress_mask": 2}
    assert snapshots("PROCESS_ENCHANT")["enchantment"] == "collector"
    assert snapshots("PROCESS_RUNIC")["research"]["negate"] is True
    assert snapshots("PROCESS_DUST_BLOCK")["target"] == "minecraft:iron_block"
    assert snapshots("PROCESS_DUST_TAG")["target_tag"] == "minecraft:gold_ores"
    assert snapshots("PROCESS_DUST_BLUEPRINT")["blueprint"] == "thaumaturge:infusion_altar_ancient"
    assert snapshots("TAGGED_COST")["aspects"] == [
        {"aspect": "thaumaturge:terra"},
        {"tag": "crafttweaker:flexible_cost", "amount": 2},
        {"tag": "crafttweaker:probe_aspects", "amount": 3},
    ]
    assert snapshots("TAGGED_MATRIX")["aspects"] == [
        {"aspect": "thaumaturge:terra", "amount": 2},
        {"tag": "crafttweaker:flexible_cost", "amount": 4},
        {"tag": "crafttweaker:probe_aspects", "amount": 2},
    ]
    assert snapshots("TAGGED_ARCANE")["crystals"] == [
        {"aspect": "thaumaturge:terra"},
        {"tag": "crafttweaker:flexible_cost", "amount": 2},
        {"tag": "crafttweaker:arcane_primal", "amount": 3},
    ]

    assert snapshots("REPLACER_JSON_COPY") == shaped
    shaped["key"]["I"] = {"item": "minecraft:copper_ingot"}
    assert snapshots("REPLACER_SHAPED") == shaped


def verify_distillation_input(profile):
    index = json.loads((profile / "config/thaumaturge/aspect_index.json").read_text())
    assert index["aspects"]["minecraft:dirt"] == [{"aspect": "thaumaturge:terra", "amount": 71}]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("profile", type=Path, help="New directory for an isolated server")
    parser.add_argument("--auram", required=True, type=Path, help="Auram instance root")
    parser.add_argument("--jar", required=True, type=Path, help="Reobfuscated Thaumaturge JAR")
    parser.add_argument("--java", required=True, type=Path, help="Java 17 executable")
    parser.add_argument("--standalone", action="store_true", help="Start without CraftTweaker or GameStages")
    args = parser.parse_args()
    prepare(args.profile, args.auram.resolve(), args.jar, args.standalone)
    server_log = args.profile / "console.log"
    ct_log = args.profile / "logs/crafttweaker.log"
    command = [str(args.java.resolve()), "-Xmx4G", "-XX:ActiveProcessorCount=4", "-Djava.awt.headless=true",
               "@libraries/net/minecraftforge/forge/1.20.1-47.4.10/unix_args.txt", "nogui"]
    with server_log.open("w") as console:
        server = subprocess.Popen(command, cwd=args.profile, stdin=subprocess.PIPE,
                                  stdout=console, stderr=subprocess.STDOUT, text=True)
        try:
            wait_for(server, server_log, 'For help, type "help"')
            if not args.standalone:
                server.stdin.write("reload\n")
                server.stdin.flush()
                wait_for(server, ct_log, "Execution for loader 'crafttweaker' completed successfully", 2)
                verify_recipes(read(ct_log))
        finally:
            if server.poll() is None:
                server.stdin.write("stop\n")
                server.stdin.flush()
                try:
                    server.wait(timeout=45)
                except subprocess.TimeoutExpired:
                    server.kill()
                    server.wait(timeout=10)
    assert server.returncode == 0 and "All dimensions are saved" in read(server_log), server.returncode
    if not args.standalone:
        verify_distillation_input(args.profile)
    print("Headless Forge startup and world save passed." if args.standalone else
          "CraftTweaker recipe fixtures passed at startup and after reload.")


if __name__ == "__main__":
    main()
