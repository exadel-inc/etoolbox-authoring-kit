const fs = require('fs');

const setDepthStructure = (path) => {
  const res = [];
  const dive = (path) => {
    const files = fs.readdirSync(path);
    files.forEach(file=>{
      if(file.match(/.+\.md/)){
        res.push(path.replace("./views","") + "/" + file)
      }
      else if(file === 'index.html') {
        return
      }
      else {
        dive(path+"/" + file)
      }
    })
  }
  dive(path);
  return res;
}

module.exports.setDepthStructure = setDepthStructure;
