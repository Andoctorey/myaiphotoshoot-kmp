# https://youtrack.jetbrains.com/issue/KT-73907/Wasm-Duplication-of-files-in-browser-distribution#focus=Comments-27-11344610.0-0
./gradlew clean
# doesn't work from first time sometimes
./gradlew clean
rm -rf composeApp/build/dist/wasmJs/productionExecutable
./gradlew wasmJsBrowserDistribution
wrangler pages deploy composeApp/build/dist/wasmJs/productionExecutable --project-name myaiphotoshoot --commit-dirty=true
# purge cache - no need already, keeping for edge cases
# open https://dash.cloudflare.com/1e70ec035798594e4af14687e54fc268/myaiphotoshoot.com/caching/configuration
