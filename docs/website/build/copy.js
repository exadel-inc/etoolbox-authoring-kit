const fs = require('fs');
const path = require('path');
const del = require('del');
const glob = require('glob');
const chokidar = require('chokidar');

const INPUT_DIR = path.resolve(__dirname, '../../content');
const INPUT_GLOB = '**/*.{md,html,njk}';
const OUTPUT_DIR = path.resolve(__dirname, '../views/content');
const DELETE_PATH = path.join(OUTPUT_DIR, '/', 'delete.yml');

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
  return {inputPath, outputPath};
}

const createFile = async (inputPath, outputPath, fileName, exists) => {
  const parsedContent = await getContent(inputPath);
  await fs.promises.mkdir(path.dirname(outputPath), { recursive: true });
  await fs.promises.writeFile(outputPath, parsedContent);
  console.log(`\t - ${fileName} - ${exists?'updated':'added'}`);
}

const deleteFile = async (outputPath, fileName) => {
  await fs.promises.writeFile(DELETE_PATH, '');
  await fs.promises.unlink(outputPath);
  console.log(`\t - ${fileName} - deleted`);
}

(async () => {
  await del(OUTPUT_DIR);

  glob(INPUT_GLOB, {cwd: INPUT_DIR}, async (err, files) => {
    await Promise.all(
      files.map(async (fileName) => {
        const {inputPath, outputPath} = getPaths(fileName);
        createFile(inputPath, outputPath, fileName);
      })
    );
  });
})();

if(process.argv.includes('watch')){
    const watch = chokidar.watch('../content');
    let pathsToUpdate = [];

    const updateData = async() => {
      pathsToUpdate.forEach(async (path, idx) => {
        const {inputPath, outputPath} = getPaths(path);

        const isDeleted = !fs.existsSync(inputPath);
        const exists = fs.existsSync(outputPath);

        if(isDeleted) {
          await deleteFile(outputPath, path);
        } else {
          createFile(inputPath, outputPath, path, exists);
        }

        if(pathsToUpdate.length - 1 === idx) pathsToUpdate = [];
      })
    }
    const deferredsync = async (fileName) => {
      !pathsToUpdate.includes(fileName) && pathsToUpdate.push(fileName)
      let timer
      clearTimeout(timer);
      timer = setTimeout(()=>updateData(), DELAY);
    }

    watch.on('change', deferredsync);
    watch.on('add', deferredsync);
    watch.on('unlink', deferredsync);
}
