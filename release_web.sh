./gradlew wasmJsBrowserDistribution
git push origin HEAD
wrangler pages deploy composeApp/build/dist/wasmJs/productionExecutable --project-name myaiphotoshoot
