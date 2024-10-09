module.exports = [
  {
    files: ['**/*.ts', '**/*.js'],
    linterOptions: {
      reportUnusedDisableDirectives: 'warn'
    }
  },

  ...require('./eslint.config.ignore'),
  ...require('@exadel/eslint-config-esl').typescript,
  ...require('@exadel/eslint-config-esl').recommended,
  ...require('@exadel/eslint-plugin-esl').recommended
];
