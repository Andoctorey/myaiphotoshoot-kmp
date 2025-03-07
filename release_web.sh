#!/bin/zsh

while true; do
    echo -n "Did you increment composeApp.js?v= manually? (y/n): "
    read answer\?
    case $answer in
        [Yy]* )
            echo "Proceeding..."
            ./gradlew clean
            rm -rf composeApp/build/dist/wasmJs/productionExecutable
            ./gradlew wasmJsBrowserDistribution
            wrangler pages deploy composeApp/build/dist/wasmJs/productionExecutable --project-name myaiphotoshoot --commit-dirty=true
            break
            ;;
        [Nn]* )
            echo "Operation canceled."
            exit
            ;;
        * )
            echo "Invalid input. Please enter 'y' or 'n'."
            ;;
    esac
done
