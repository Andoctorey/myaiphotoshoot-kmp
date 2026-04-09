# My AI Photo Shoot

**Turn Your Selfies Into Stunning AI-Generated Photos!**

My AI Photo Shoot is a cross-platform mobile and web application built
with [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) that transforms your
selfies into breathtaking, AI-enhanced images. Powered by a [Supabase](https://supabase.com/)
backend and the advanced [Flux.1](https://replicate.com/ostris/flux-dev-lora-trainer/) AI model, the
app supports iOS, Android, and Web (WASM) platforms, offering a seamless and creative experience for
generating professional-grade photos.

🌐 **Website**: [myaiphotoshoot.com](https://myaiphotoshoot.com)

## ✨ Features

- **AI-Powered Selfie Transformation**: Upload 10–20 selfies to generate thousands of unique,
  personalized photos.
- **Cross-Platform Support**: Available on iOS, Android, and Web (WASM) using Compose Multiplatform.
- **Advanced AI Technology**: Leverages Flux.1 for exceptional photo realism, creativity, and
  detail.
- **Custom Photo Prompts & Styles**: Use the "Surprise Me!" feature or enhance your own prompts for
  tailored results.
- **Privacy First**: Switch between public and private galleries, with full control to delete your
  data anytime.
- **Transparent Pricing**: One-time AI training cost and $0.03 per generated photo, with no hidden
  fees.
- **Seamless User Experience**:
    - Easy selfie uploads and social sharing.
    - Real-time balance updates and promo code support.
    - Reliable connectivity checks and quick account verification via email.
    - "Report a Problem" feature for prompt support.
- **Supabase Backend**: Secure and scalable data handling for user accounts, galleries, and photo
  storage.

## 📸 How It Works

1. **Upload Selfies**: Take 10–20 selfies in portrait mode or upload existing photos.
2. **AI Magic**: Our Flux.1 model processes your selfies to create thousands of unique photos.
3. **Customize & Create**: Use "Surprise Me!" for inspiration or craft your own prompts.
4. **Share or Save**: Download your photos, share them on social media, or keep them private.

## 🛠️ Tech Stack

- **Frontend**: Compose Multiplatform (Kotlin) for iOS, Android, and Web (WASM)
- **Backend**: Supabase (PostgreSQL, Authentication, Storage, Realtime)
- **AI Model**: Flux.1 for AI-powered photo generation
- **APIs**: Supabase REST API for data management
- **Image Hosting**: BunnyCDN for fast and reliable image delivery
- **Web Hosting**: Cloudflare for secure and scalable web hosting
- **Other Tools**: Ktor (for networking), Gradle, Firebase (for analytics), and more.

## 🚀 Getting Started

### Prerequisites

- [Kotlin 1.9+](https://kotlinlang.org/)
- [JetBrains Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)
- [Android Studio](https://developer.android.com/studio) (for Android development)
- [Xcode](https://developer.apple.com/xcode/) (for iOS development)
- [Supabase Account](https://supabase.com/) with a configured project

### Installation

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/yourusername/my-ai-photo-shoot.git
   cd my-ai-photo-shoot
   ```

2. **Set Up Supabase**:
    - Create a Supabase project and note your `Project URL` and `API Key`.
    - Configure authentication, storage, and database tables as per the schema in
      `/docs/supabase-schema.sql`.
    - Update the Supabase configuration in `commonMain/kotlin/config/SupabaseConfig.kt`:
      ```kotlin
      object SupabaseConfig {
          const val URL = "https://your-project.supabase.co"
          const val API_KEY = "your-anon-key"
      }
      ```

3. **Configure Flux.1 API**:
    - Obtain a Flux.1 API key from [fal.ai](https://fal.ai/).
    - Add the key to `commonMain/kotlin/config/AIConfig.kt`:
      ```kotlin
      object AIConfig {
          const val FLUX_API_KEY = "your-flux-api-key"
      }
      ```

4. **Build and Run**:
    - **Android**: Open the project in Android Studio, sync Gradle, and run on an emulator or
      device.
    - **iOS**: Run in Android Studio. Or open the `iosApp` directory in Xcode and run on a simulator
      or device.
    - **Web (WASM)**: Run the following command to build and serve the Web app:
      ```bash
      ./gradlew wasmJsBrowserDevelopmentRun -t --quiet
      ```

5. **Verify Setup**:
    - Ensure the app connects to Supabase and Flux.1 APIs.
    - Test selfie uploads and photo generation.

## 📂 Project Structure

```
my-ai-photo-shoot/
├── iosApp/                  # iOS-specific code
├── androidApp/              # Android application entry module
├── composeApp/src           # Shared Compose Multiplatform code
│   ├── commonMain/          # Platform-agnostic logic (UI, business logic)
│   ├── androidMain/         # Android-specific actual implementations
│   ├── desktopMain/         # Desktop-specific implementations
│   ├── iosMain/             # iOS-specific implementations
│   ├── wasmJsMain/          # Web/WASM-specific implementations
├── build.gradle.kts         # Gradle configuration
└── README.md                # This file
```

## 🤝 Contributing

We welcome contributions to enhance My AI Photo Shoot! To contribute:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature`).
3. Make your changes and commit (`git commit -m "Add your feature"`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a Pull Request.

Please ensure your code follows
the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) and includes
relevant tests.

## 🐞 Reporting Issues

Encounter a bug or have a suggestion? Use the app's "Report a Problem" feature or create an issue on
the [GitHub Issues page](https://github.com/yourusername/my-ai-photo-shoot/issues).

## 🔗 Follow Us

- 🌐 [myaiphotoshoot.com](https://myaiphotoshoot.com)
- 📱 [Download on the App Store](https://apps.apple.com/app/id6744860178)
- 📱 [Get it on Google Play](https://play.google.com/store/apps/details?id=com.myaiphotoshoot)
- 📱 Join our community on social media and share your AI-generated photos!

## 📜 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

**My AI Photo Shoot** – Your AI-powered selfie revolution awaits! 🚀
