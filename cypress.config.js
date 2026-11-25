const { defineConfig } = require('cypress');

module.exports = defineConfig({
  e2e: {
    baseUrl: 'http://localhost:8080',
    viewportWidth: 1440,
    viewportHeight: 1080,
    defaultCommandTimeout: 30000,
    video: false,
    supportFile: false,
    screenshotsFolder: 'cypress/screenshots',
    setupNodeEvents(on) {
      on('before:browser:launch', (browser, launchOptions) => {
        if (browser.family === 'chromium') {
          launchOptions.args.push('--window-size=1600,1200');
          launchOptions.args.push('--force-device-scale-factor=1');
        }
        return launchOptions;
      });
    },
  },
});
