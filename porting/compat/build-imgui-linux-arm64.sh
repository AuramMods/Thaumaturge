#!/usr/bin/env bash
set -euo pipefail

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
output_dir=${1:-build/compat}
mkdir -p -- "$output_dir"
output_dir=$(cd -- "$output_dir" && pwd)
build_dir=$(mktemp -d)
trap 'rm -rf -- "$build_dir"' EXIT

revision=2cef96d9936fdfcbedb3f9327d2463c6bf6f280e
source_dir=$build_dir/imgui-java
git init --quiet "$source_dir"
git -C "$source_dir" fetch --quiet --depth=1 https://github.com/SpaiR/imgui-java.git "$revision"
git -C "$source_dir" checkout --quiet --detach "$revision"
git -C "$source_dir" tag v1.86.11 "$revision"
git -C "$source_dir" submodule update --init --recursive --depth=1
git -C "$source_dir" apply --unidiff-zero "$script_dir/imgui-java-1.86.11-linux-arm64.patch"
(
    cd -- "$source_dir"
    ./gradlew :imgui-binding:generateLibs -Denvs=linux -Dlocal -Dfreetype=true --no-daemon --console=plain
)

staging=$build_dir/staging
mkdir -p -- "$staging/io/imgui/java/native-bin" "$staging/META-INF/licenses"
cp -- "$source_dir/imgui-binding/build/libsNative/linuxarm64/libimgui-java64.so" "$staging/io/imgui/java/native-bin/"
cp -- "$source_dir/LICENSE" "$staging/META-INF/licenses/imgui-java.txt"
while IFS=: read -r project license; do
    cp -- "$source_dir/include/$project/$license" "$staging/META-INF/licenses/$project.txt"
done <<'LICENSES'
ImGuiColorTextEdit:LICENSE
ImGuiFileDialog:LICENSE
imgui:LICENSE.txt
imgui-node-editor:LICENSE
imgui_club:LICENSE.txt
imguizmo:LICENSE
imnodes:LICENSE.md
implot:LICENSE
LICENSES
cp -- "$source_dir/include/ImGuiFileDialog/dirent/LICENSE" "$staging/META-INF/licenses/dirent.txt"
cp -- "$source_dir/include/ImGuiFileDialog/stb/LICENSE" "$staging/META-INF/licenses/stb.txt"
cat > "$build_dir/manifest.mf" <<'MANIFEST'
Manifest-Version: 1.0
Automatic-Module-Name: thaumaturge.imgui.natives.linux.arm64
FMLModType: GAMELIBRARY
Implementation-Version: 1.86.11

MANIFEST
artifact=imgui-java-natives-linux-arm64-1.86.11.jar
jar --create --file "$build_dir/$artifact" --manifest "$build_dir/manifest.mf" -C "$staging" .
mv -- "$build_dir/$artifact" "$output_dir/$artifact"
printf '%s\n' "$output_dir/$artifact"
