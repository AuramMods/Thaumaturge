package com.aurammods.thaumaturge.testing.launcher;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import net.minecraftforge.fml.loading.VersionInfo;
import net.minecraftforge.fml.loading.targets.ForgeServerUserdevLaunchHandler;

/** Native Forge transformations and server distribution, with a JUnit entry point. */
public final class ForgePortTestLaunchHandler extends ForgeServerUserdevLaunchHandler {
    @Override public String name() { return "thaumaturgeporttestserver"; }

    @Override
    protected void processStreams(String[] classpath, VersionInfo version, Stream.Builder<Path> minecraft,
            Stream.Builder<List<Path>> mods) {
        super.processStreams(classpath, version, minecraft, mods);
        for (String path : System.getProperty("thaumaturge.portTestPaths").split(File.pathSeparator)) {
            minecraft.add(Path.of(path));
        }
    }

    @Override
    public void devService(String[] args, ModuleLayer layer) throws Throwable {
        runTarget("com.aurammods.thaumaturge.testing.runtime.ForgePortTestRunner", args, layer);
    }
}
