module.exports = {
    testEnvironment: 'jsdom',
    roots: ['src/main/content/jcr_root/apps/etoolbox-authoring-kit'],
    testRegex: '/tests/(.+)\\.test\\.js$',
    moduleFileExtensions: ['js', 'json', 'html'],
    setupFiles: [
        './jest/granite.mocks.js'
    ],
    transform: {
        // Clientlib importer
        '^.+js\\.txt$': './jest/clientlib.transform.js'
    }
};
