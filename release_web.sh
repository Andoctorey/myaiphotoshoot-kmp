# https://slack-chats.kotlinlang.org/t/26904919/i-run-wasmjsbrowserdistribution-and-there-are-4-wasm-files-s
./gradlew clean
rm -rf composeApp/build/dist/wasmJs/productionExecutable
./gradlew wasmJsBrowserDistribution
git push origin HEAD
wrangler pages deploy composeApp/build/dist/wasmJs/productionExecutable --project-name myaiphotoshoot
# purge
# https://dash.cloudflare.com/1e70ec035798594e4af14687e54fc268/myaiphotoshoot.com/caching/configuration
