module.exports = (config) => {
  config.addFilter("replaceDist", (string)=>{
    return string.replace("dist", "")
  })
}
