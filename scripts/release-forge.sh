#!/usr/bin/env bash
set -euo pipefail

usage="Usage: $0 [--publish]"
if (( $# > 1 )); then
    printf '%s\n' "$usage" >&2
    exit 2
fi

case "${1:-}" in
    '') publish=false ;;
    --publish) publish=true ;;
    -h|--help) printf '%s\n' "$usage"; exit 0 ;;
    *) printf '%s\n' "$usage" >&2; exit 2 ;;
esac

cd "$(dirname "${BASH_SOURCE[0]}")/.."

if [[ "$publish" == true ]]; then
    revision=$(git rev-parse HEAD)
    changes=$(git status --porcelain --untracked-files=normal)
    if [[ -n "$changes" ]]; then
        printf '%s\n' 'Commit the release source before publishing.' >&2
        exit 1
    fi
fi

mod_id=$(sed -n 's/^mod_id=//p' gradle.properties)
mod_version=$(sed -n 's/^mod_version=//p' gradle.properties)
minecraft_version=$(sed -n 's/^minecraft_version=//p' gradle.properties)
artifact="build/libs/${mod_id}-${mod_version}-mc${minecraft_version}-forge.jar"
gradle_properties=("-Pmod_id=$mod_id" "-Pmod_version=$mod_version" "-Pminecraft_version=$minecraft_version")

./gradlew runData "${gradle_properties[@]}"
./gradlew jarJar "${gradle_properties[@]}"
shasum -a 256 "$artifact"

if [[ "$publish" == true ]]; then
    changes=$(git status --porcelain --untracked-files=normal)
    if [[ "$(git rev-parse HEAD)" != "$revision" || -n "$changes" ]]; then
        printf '%s\n' 'The release checkout changed during the build. Review the changes before publishing.' >&2
        exit 1
    fi
    repository=AuramMods/Thaumaturge
    tag="v${mod_version}"
    git push "https://github.com/${repository}.git" "${revision}:refs/tags/${tag}"
    gh release create "$tag" "$artifact" --repo "$repository" --verify-tag \
        --title "Thaumaturge ${mod_version} for Forge ${minecraft_version}" --generate-notes
fi
