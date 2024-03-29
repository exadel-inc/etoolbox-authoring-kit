const fs = require('fs');
const path = require('path');
const del = require('del');
const {glob} = require('glob');
const chokidar = require('chokidar');

const INPUT_DIR = path.resolve(__dirname, '../../content');
const INPUT_GLOB = '**/*.{md,html,njk}';
const OUTPUT_DIR = path.resolve(__dirname, '../views/content');
const TIMESTAMP_PATH = path.join(OUTPUT_DIR, '/timestamp.tmp');

const DELAY = 2000;

console.log(`Searching for files in '${INPUT_DIR}' :`);

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

const deleteFile = async (outputPath) => fs.promises.unlink(outputPath);

const updateTimestamp = async () => {
  const time = (new Date()).toISOString();
  return fs.promises.writeFile(TIMESTAMP_PATH, time);
};

(async () => {
  await del(OUTPUT_DIR);

  const filePaths = glob.sync(INPUT_GLOB, {cwd: INPUT_DIR});
  for (const filePath of filePaths) {
    const { inputPath, outputPath } = getPaths(filePath);
    await createFileCopy(inputPath, outputPath);
    console.log(`\t - ${filePath} - copied`);
  }
})();

if (process.argv.includes('watch')) {
  const pathsToUpdate = new Set();
  const updateData = async () => {
    for (const filePath of pathsToUpdate) {
      const { inputPath, outputPath } = getPaths(filePath);
      const isDeleted = !fs.existsSync(inputPath);

      if (isDeleted) {
        await deleteFile(outputPath);
        console.log(`\t - ${filePath} - deleted`)
      } else {
        await createFileCopy(inputPath, outputPath);
        console.log(`\t - ${filePath} - updated`);
      }
    }
    pathsToUpdate.clear();
    await updateTimestamp();
  };

  // Debounced watch task
  let timer;
  const deferredSync = async (fileName) => {
    pathsToUpdate.add(fileName);
    clearTimeout(timer);
    timer = setTimeout(() => updateData(), DELAY);
  };

  // Run watch process
  const watch = chokidar.watch('../content');
  watch.on('change', deferredSync);
  watch.on('add', deferredSync);
  watch.on('unlink', deferredSync);
}
