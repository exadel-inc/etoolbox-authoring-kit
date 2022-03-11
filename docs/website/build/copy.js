const fs = require('fs');
const path = require('path');
const del = require('del');
const glob = require('glob');

const INPUT_DIR = path.resolve(__dirname, '../../content');
const INPUT_GLOB = '**/*.{md,html,njk}';
const OUTPUT_DIR = path.resolve(__dirname, '../views/content');

console.log(`Searching for files in ${INPUT_DIR}`);

(async () => {
  await del(OUTPUT_DIR);

  glob(INPUT_GLOB, {cwd: INPUT_DIR}, async (err, files) => {
    await Promise.all(
      files.map(async (fileName) => {
        const filePath = path.join(INPUT_DIR, '/', fileName);
        const outputPath = path.join(OUTPUT_DIR, '/', fileName);

        const file = await fs.promises.readFile(filePath);
        const content = file.toString();
        const parsedContent = content.replace(/(^)(<!--|-->)/gm, '$1---');

        await fs.promises.mkdir(path.dirname(outputPath), { recursive: true });
        await fs.promises.writeFile(outputPath, parsedContent);

        console.log(`\t - File "${fileName}" processed`);
      })
    );
  });
})();

if (process.argv.includes('watch')) {
    // TODO: chokidar + build single file
}
