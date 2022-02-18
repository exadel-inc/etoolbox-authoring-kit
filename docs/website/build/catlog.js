/** ========================
 * It's too boring without any fun ;)
 * =========================*/
const MIN_WIDTH = 30;
const ANSI_CATS = [
  ' ((      /|_/|\n  \\\\.._.\'  , ,\\\n  /\\ | \'.__ v /\n (_ .   /   "        \n  ) _)._  _ /\n \'.\\ \\|( / ( mur\n   \'\' \'\'\\\\ \\\\',
  ' _._     _,-\'""`-._\n(,-.`._,\'(       |\\`-/|\n    `-.-\' \\ )-`( , o o)\n          `-    \\`_`"\'-',
  '  /\\_/\\  (\n ( ^.^ ) _)\n   \\"/  (\n ( | | )\n(__d b__)',
  '      /^--^\\     /^--^\\     /^--^\\\n\\____/     \\____/     \\____/\n     /      \\   /      \\   /      \\\n    |        | |        | |        |\n     \\__  __/   \\__  __/   \\__  __/\n|^|^|^|^\\ \\^|^|^|^/ /^|^|^|^|^\\ \\^|^|^|^|^|\n| | | | |\\ \\| | |/ /| | | | | | \\ \\ | | | |\n| | | | / / | | |\\ \\| | | | | |/ /| | | | |\n| | | | \\/| | | | \\/| | | | | |\\/ | | | | |\n###########################################'
];

function center(text, width, textWidth = text.length) {
  const pad = Math.max(0, Math.floor(.5 * (width - textWidth) ));
  const padString = ''.padEnd(pad, ' ');
  return padString + text + padString;
}

function catlog(text) {
  const index = Math.floor(Math.random() * ANSI_CATS.length);
  const catLines = ANSI_CATS[index].split('\n');
  const catWidth = Math.max(...catLines.map((line) => line.length));
  const width = Math.max(text.length, catWidth, MIN_WIDTH) + 2;
  const result = catLines.map((line) => center(line, width, catWidth));
  result.push('#' + center(text, width - 2) + '#');
  console.log(result.join('\n'));
}

const [text] = process.argv.slice(2);
text && catlog(text);
