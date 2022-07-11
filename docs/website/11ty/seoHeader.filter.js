function kebabCaseToTitleCase(words) {
  return words
    .toLowerCase()
    .split('-')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
};

function seoHeader(url){
  const parts = url.slice(1,-1).split('/'); // remove first and last slash to get accurate amount of parts
  if(parts.length > 3) return kebabCaseToTitleCase(parts[2]); //ignore non-nested elements, then pick header
}

module.exports = (config) => {
  config.addFilter('seoHeader',seoHeader);
};
