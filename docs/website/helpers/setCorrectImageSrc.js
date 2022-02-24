const setCorrectImageSrc = (md)=>{
  md.renderer.rules.image = (tokens, idx) => {
    const token =  tokens[idx];
    const src = token.attrGet('src');
    if(src && src.startsWith('../img') || src.startsWith('docs/img')){
      const correctSrc = src.replace('../img', '/assets/components');
      return `<${tokens[idx].tag} src='${correctSrc}' alt='${tokens[idx].content}'>`;
    };
    return `<${tokens[idx].tag} src='${src}' alt='${tokens[idx].content}'>`;
  };
}

module.exports.setCorrectImageSrc = setCorrectImageSrc;
