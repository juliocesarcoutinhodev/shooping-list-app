require('dotenv').config();

module.exports = {
  expo: {
    name: 'Shopping List',
    slug: 'shopping-list',
    version: '1.0.0',
    orientation: 'portrait',
    icon: './assets/images/icon.png',
    scheme: 'shoppinglist',
    userInterfaceStyle: 'automatic',
    newArchEnabled: true,
    ios: {
      bundleIdentifier: 'com.shoppinglist.app',
      supportsTablet: true,
    },
    android: {
      package: 'com.shoppinglist.app',
      adaptiveIcon: {
        backgroundColor: '#E6F4FE',
        foregroundImage: './assets/images/android-icon-foreground.png',
        backgroundImage: './assets/images/android-icon-background.png',
        monochromeImage: './assets/images/android-icon-monochrome.png',
      },
      edgeToEdgeEnabled: true,
      predictiveBackGestureEnabled: false,
    },
    web: {
      output: 'static',
      favicon: './assets/images/favicon.png',
    },
    splash: {
      image: './assets/images/splash-icon.png',
      resizeMode: 'contain',
      backgroundColor: '#E6F4FE',
    },
    plugins: [
      'expo-router',
      [
        'expo-splash-screen',
        {
          image: './assets/images/splash-icon.png',
          resizeMode: 'contain',
          backgroundColor: '#E6F4FE',
          dark: {
            image: './assets/images/splash-icon.png',
            resizeMode: 'contain',
            backgroundColor: '#1A1A1A',
          },
        },
      ],
    ],
    experiments: {
      typedRoutes: true,
      reactCompiler: true,
    },
    extra: {
      API_URL: process.env.API_URL || 'http://localhost:3000/api',
      API_TIMEOUT: process.env.API_TIMEOUT || '30000',
      APP_NAME: process.env.APP_NAME || 'Shopping List',
      APP_ENV: process.env.APP_ENV || 'development',
      ENABLE_MOCK_API: process.env.ENABLE_MOCK_API || 'true',
      ENABLE_DEBUG_LOGS: process.env.ENABLE_DEBUG_LOGS || 'false',
      GOOGLE_CLIENT_ID: process.env.GOOGLE_CLIENT_ID || '',
      GOOGLE_REDIRECT_URI: process.env.GOOGLE_REDIRECT_URI || 'exp://192.168.10.5:8081',
    },
  },
};
