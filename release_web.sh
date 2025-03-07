# search and increment composeApp.js?v= manually

# https://youtrack.jetbrains.com/issue/KT-73907/Wasm-Duplication-of-files-in-browser-distribution#focus=Comments-27-11344610.0-0
./gradlew clean
rm -rf composeApp/build/dist/wasmJs/productionExecutable
./gradlew wasmJsBrowserDistribution
wrangler pages deploy composeApp/build/dist/wasmJs/productionExecutable --project-name myaiphotoshoot --commit-dirty=true
# purge cache
open https://dash.cloudflare.com/1e70ec035798594e4af14687e54fc268/myaiphotoshoot.com/caching/configuration
