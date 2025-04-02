import globals from 'globals';
import standard from 'neostandard';

export default [
    ...standard(),
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
                // Default global variables
                ...globals.browser,
                ...globals.jquery,
                ...globals.jest,
                // Custom global variables
                Granite: 'writable',
                Coral: 'readonly'
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
                    SwitchCase: 1
                }
            ],
            '@stylistic/operator-linebreak': [2, 'after'],
            '@stylistic/semi': [1, 'always'],
            '@stylistic/space-before-function-paren': [
                'error',
                {
                    anonymous: 'always',
                    named: 'never',
                    asyncArrow: 'never'
                }
            ]
        }
    }
];
