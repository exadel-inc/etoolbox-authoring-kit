'use strict';

// Dependecies
const path = require('path');
const log = require('fancy-log');
const colors = require('ansi-colors');
const index = require('aemsync');

//-----------------------------------
// Environment Settings
//-----------------------------------
const workingDirs = [];

["./src/main/content/jcr_root"].forEach(function(item) {
    let dirPath = path.resolve(__dirname, item);
    workingDirs.push(dirPath);
});

const targets = [
    'http://admin:admin@localhost:4502', // Author
    'http://admin:admin@localhost:4503' // Publish
];

const exclude = '**/*.orig'; // Skip merge files.
const pushInterval = 2000;


//-----------------------------------
// Push Messages
//-----------------------------------
const onPushEnd = function (err, host) {
  if (err) {
    return log(colors.red(`Error when pushing package to ${host}.`, err));
  }
  log(colors.green.bold(`Package pushed to ${host}.`));
};

//-----------------------------------
// Add Watchers
//-----------------------------------
workingDirs.forEach(function (dir) {
  index(dir, {
    targets: targets,
    exclude: exclude,
    interval: pushInterval,
    onPushEnd: onPushEnd
  });
  log(colors.green.bold(`Watching ${dir}...`));
});
