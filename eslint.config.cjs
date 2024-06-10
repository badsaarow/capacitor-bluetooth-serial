const parser = require('@typescript-eslint/parser');
const pluginObject = require('@typescript-eslint/eslint-plugin');

module.exports = {
  files: ['**/*.ts'],
  ignores: ['.history/**/*'],
  languageOptions: {
    parser: parser,
  },
  plugins: {
    '@typescript-eslint': pluginObject,
  },
  rules: {
    // add TypeScript-specific rules here
  },
};