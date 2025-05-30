import { initializeApp } from "firebase/app";
import { getAnalytics } from "firebase/analytics";

// Firebase configuration
// Note: Firebase web API keys are safe to expose publicly as they're designed for client-side use.
// Security is enforced through Firebase Security Rules, not by hiding the API key.
const firebaseConfig = {
  apiKey: "AIzaSyD6dEX501AFA1D0oVRfs2IHf0aFmElzvVM",
  authDomain: "photocreateai-9bc4b.firebaseapp.com",
  databaseURL: "https://photocreateai-9bc4b-default-rtdb.firebaseio.com",
  projectId: "photocreateai-9bc4b",
  storageBucket: "photocreateai-9bc4b.firebasestorage.app",
  messagingSenderId: "740105515147",
  appId: "1:740105515147:web:3579c958df06314b9e648e",
  measurementId: "G-0T00P896RF"
};

const app = initializeApp(firebaseConfig);
const analytics = getAnalytics(app);

window.myFirebase = {
  logEvent: (eventName, params) => {
    analytics.logEvent(eventName, params);
  }
};