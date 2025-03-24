const fs = require('fs');
const path = require('path');
const { SourceMapGenerator } = require('source-map');

/**
 * A custom transformer for Jest to support the `require` directive for a clientlib descriptor file.
 * Usage: `require('path/to/clientlib/js.txt')`
 * Note: it does not resolve the clientlib dependencies, it just emulates concatenation of the files.
 */
module.exports = {
    process(src, filename) {
        const baseDir = path.dirname(filename);
        const files = src.split('\n')
            .map((line) => line.trim())
            .filter(line => line && !line.startsWith('#'))
            .map(file => path.resolve(baseDir, file.trim()));

        let code = '';
        const map = new SourceMapGenerator({ file: filename });
        for (const file of files) {
            try {
                const content = fs.readFileSync(file, 'utf8');
                const lines = content.split('\n');
                for (let i = 0; i < lines.length; i++) {
                    const line = lines[i];
                    code += line + '\n';
                    map.addMapping({
                        source: file,
                        original: { line: i + 1, column: 0 },
                        generated: { line: code.split('\n').length, column: 0 }
                    });
                }
            } catch (e) {
                console.error(`Cant read file ${file}: `, e);
            }
        }
        return { code, map: map.toString() };
    }
};
