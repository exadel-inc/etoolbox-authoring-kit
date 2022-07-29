const fs = require('fs');
const path = require('path');
const del = require('del');
const glob = require('glob');
const chokidar = require('chokidar');

const INPUT_DIR = path.resolve(__dirname, '../../content');
const INPUT_GLOB = '**/*.{md,html,njk}';
const OUTPUT_DIR = path.resolve(__dirname, '../views/content');
const DELETE_PATH = path.join(OUTPUT_DIR, '/', 'delete.tmp');

const DELAY = 5000;

console.log(`Searching for files in ${INPUT_DIR}`);

const getContent = async (inputPath) => {
  const file = await fs.promises.readFile(inputPath);
  const content = file.toString();
  return content.replace(/(^)(<!--|-->)/gm, '$1---');
}

const getPaths = (fileName) => {
  const fileInitPath = fileName.replace('\.\.\\content\\', '');
  const inputPath = path.join(INPUT_DIR, '/', fileInitPath);
  const outputPath = path.join(OUTPUT_DIR, '/', fileInitPath);
  return { inputPath, outputPath };
}

const createFileCopy = async (inputPath, outputPath) => {
  const parsedContent = await getContent(inputPath);
  await fs.promises.mkdir(path.dirname(outputPath), { recursive: true });
  await fs.promises.writeFile(outputPath, parsedContent);
}

const deleteFile = async (outputPath) => {
  await fs.promises.writeFile(DELETE_PATH, '');
  await fs.promises.unlink(outputPath);
}

(async () => {
  await del(OUTPUT_DIR);

  glob(INPUT_GLOB, { cwd: INPUT_DIR }, async (err, files) => {
    await Promise.all(
      files.map(async (fileName) => {
        const { inputPath, outputPath } = getPaths(fileName);
        createFileCopy(inputPath, outputPath);
      })
    );
  });
})();

if (process.argv.includes('watch')) {
  const watch = chokidar.watch('../content');
  const pathsToUpdate = new Set();
  let timer;

  const updateData = async () => {
    pathsToUpdate.forEach(async (path) => {
      const { inputPath, outputPath } = getPaths(path);

      const isDeleted = !fs.existsSync(inputPath);

      if (isDeleted) {
        await deleteFile(outputPath);
        console.log(`\t - ${path} - deleted`)
      } else {
        await createFileCopy(inputPath, outputPath);
        console.log(`\t - ${path} - updated`);
      }
    })
    pathsToUpdate.clear();
  }

  const deferredSync = async (fileName) => {
    pathsToUpdate.add(fileName);
    clearTimeout(timer);
    timer = setTimeout(() => updateData(), DELAY);
  }

  watch.on('change', deferredSync);
  watch.on('add', deferredSync);
  watch.on('unlink', deferredSync);
}
