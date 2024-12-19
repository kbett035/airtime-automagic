import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.airtimebot.app',
  appName: 'Airtime Bot',
  webDir: 'dist',
  server: {
    androidScheme: 'https',
    cleartext: true
  },
  android: {
    buildOptions: {
      keystorePath: 'release.keystore',
      keystoreAlias: 'release',
    },
    backgroundColor: '#ffffff',
    allowMixedContent: true,
    captureInput: true,
    webContentsDebuggingEnabled: false
  },
  plugins: {
    Permissions: {
      sms: true,
      phone: true
    }
  }
};

export default config;