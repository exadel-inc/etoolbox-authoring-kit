import Prism from 'prismjs';

import 'prismjs/components/prism-java.js';
import 'prismjs/components/prism-javascript.js';

export const highlight = (str, lang) => {
  try {
    lang = lang || 'text';
    if (!Prism.languages[lang]) return `<!-- Error: Unsupported language '${lang}' -->`;
    return Prism.highlight(str, Prism.languages[lang], lang);
  } catch (e) {
    return `<!-- Error while processing code block ${e.toString()} -->`;
  }
};

export const getOffset = (lines) => {
  if (!lines.length) return 0;
  return lines.reduce((min, str) => {
    if (!str) return min;
    let i = 0;
    while (i < str.length && i < min && str.charAt(i) === ' ') i++;
    return i;
  }, Number.MAX_SAFE_INTEGER);
};

export const normalize = (str) => {
  const lines = str.split('\n');
  // Exclude first empty line if exists
  if (lines.length && !lines[0].trim()) lines.shift();
  // Exclude last empty line if presented
  if (lines.length && !lines[lines.length - 1].trim()) lines.pop();
  // Get minimum offset from the passed lines
  const offset = getOffset(lines);
  // Remove offset from the lines
  return lines.map((str) => str.substr(offset)).join('\n');
};

export const highlightNormalized = (str, lang) => {
  const text = normalize(str);
  const code = highlight(text, lang);
  return `<pre><code class="language-${lang}">${code}</code></pre>`;
};

export default (config) => {
  config.addPairedNunjucksShortcode('code', highlightNormalized);
};
