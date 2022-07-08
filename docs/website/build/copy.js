const fs = require('fs');
const path = require('path');
const del = require('del');
const glob = require('glob');
const chokidar = require('chokidar');

const INPUT_DIR = path.resolve(__dirname, '../../content');
const INPUT_GLOB = '**/*.{md,html,njk}';
const OUTPUT_DIR = path.resolve(__dirname, '../views/content');

console.log(`Searching for files in ${INPUT_DIR}`);

const getContent = async (filePath) => {
  const file = await fs.promises.readFile(filePath);
  const content = file.toString();
  return content.replace(/(^)(<!--|-->)/gm, '$1---');
}
const getPaths = (fileName) => {
  const fileInitPath = fileName.replace('\.\.\\content\\', '');
  const filePath = path.join(INPUT_DIR, '/', fileInitPath);
  const outputPath = path.join(OUTPUT_DIR, '/', fileInitPath);
  return {filePath, outputPath};
}
const createFile = async (parsedContent, outputPath) => {
  await fs.promises.mkdir(path.dirname(outputPath), { recursive: true });
  await fs.promises.writeFile(outputPath, parsedContent);
}

(async () => {
  await del(OUTPUT_DIR);

  glob(INPUT_GLOB, {cwd: INPUT_DIR}, async (err, files) => {
    await Promise.all(
      files.map(async (fileName) => {
        const {filePath, outputPath} = getPaths(fileName);
        const parsedContent = await getContent(filePath);

        createFile(parsedContent, outputPath);

        console.log(`\t - File "${fileName}" processed`);
      })
    );
  });
})();

if(process.argv.includes('watch')){
    const watch = chokidar.watch('../content', {usePolling:true, interval:2000});

    watch.on('change', async (fileName) => {
      const {filePath, outputPath} = getPaths(fileName);

      const parsedContent = await getContent(filePath);
      await createFile(parsedContent, outputPath)

      console.log(`\t - ${fileName} - updated`);
    })

    watch.on('add', async (fileName) => {
      const {outputPath} = getPaths(fileName);

      createFile('', outputPath);

      console.log(`\t - ${fileName} - added`);
    })

    watch.on('unlink', async (fileName) => {
      const {outputPath} = getPaths(fileName);
      await createFile('', outputPath.replace(/\.(html|njk|md)/, '.yml'))
      await fs.promises.unlink(outputPath);

      console.log(`\t - ${fileName} - deleted`);
    })
}
