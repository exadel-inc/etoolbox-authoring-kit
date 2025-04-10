import {lang, strict} from '@exadel/eslint-config-esl';
import {recommended as eslRecommended} from '@exadel/eslint-plugin-esl';

export default [
  {
    ignores: [
      // Common configuration
      'eslint.config.js',
      // Common directories
      'node_modules/**',
      'dist/**',
    ]
  },
  {
    files: ['**/*.js','**/*.ts', '**/*.tsx'],
    linterOptions: {
      reportUnusedDisableDirectives: 'warn'
    }
  },

  // Using shared ESL ESLint Config
  ...lang.js,
  ...lang.ts,
  ...strict,

  // ESL ESLint Plugin
  ...eslRecommended
];
