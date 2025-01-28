const neostandard = require('neostandard');

module.exports = [
    ...neostandard(),
    {
        files: ['**/*.js'],
        rules: {
            'no-new-func': 'off',
            'no-useless-call': 'off'
        },
        languageOptions: {
            ecmaVersion: 2017,
            sourceType: 'module',
            parserOptions: {
                projectService: true
            },
            globals: {
                '$': 'readonly',
                'jQuery': 'readonly',
                'Granite': 'writable',
                'Coral': 'readonly'
            }
        },
        linterOptions: {
            reportUnusedDisableDirectives: 'warn'
        }
    },
    {
        rules: {
            '@stylistic/indent': [
                'error', 4, {
                    'SwitchCase': 1
                }
            ],
            '@stylistic/operator-linebreak': [2, 'after'],
            '@stylistic/semi': [1, 'always'],
            '@stylistic/space-before-function-paren': [
                'error',
                {
                    'anonymous': 'always',
                    'named': 'never',
                    'asyncArrow': 'never'
                }
            ]
        }
    }
];
