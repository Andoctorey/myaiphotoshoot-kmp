# https://youtrack.jetbrains.com/issue/KT-73907/Wasm-Duplication-of-files-in-browser-distribution#focus=Comments-27-11344610.0-0
./gradlew clean
rm -rf composeApp/build/dist/wasmJs/productionExecutable
./gradlew wasmJsBrowserDistribution
wrangler pages deploy composeApp/build/dist/wasmJs/productionExecutable --project-name myaiphotoshoot --commit-dirty=true
