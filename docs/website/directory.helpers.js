const setNjkTemplate = (elem, path, dirTag, idx) => {
    const elemWithoutMd = elem.fileName.replace(".md", "");
    const replaceDash = elemWithoutMd.replace("-", " ");
    const ElemUpperCase= replaceDash[0].toUpperCase() + replaceDash.substring(1)

    return `---
layout: content
title: EAK ${elem.metaData.title || ElemUpperCase}
seoTitle: EAK ${ElemUpperCase}
nav-title: ${elem.metaData.navTitle || ElemUpperCase}
description: ${elem.metaData.description || ElemUpperCase}
keywords: ${elem.metaData.keywords || dirTag}
name: ${ElemUpperCase}
val: ${elemWithoutMd}
tags: ${dirTag ? dirTag : "components"}
orderValue: ${idx+1}
---

{% mdRender '${path + "/" + elem.fileName}' %}`
}

const objDive = (obj, keyToFind, data) => {
    if(keyToFind){
    let isFound = false;
    const dive = (obj, keyToFind) => {
      for (let key in obj) {
        if (key === keyToFind) {
          obj[key] = data;
          isFound = true;
        }
        if(Array.isArray(obj[key])){
            obj[key].forEach(elem=>{
                dive(elem,keyToFind)
            })
        }
        if (typeof obj[key] === "object") {
          dive(obj[key], keyToFind);
        }
      }
    };
    dive(obj, keyToFind)
    if (!isFound) {
      obj[keyToFind] = data;
    }}
    return
  };

module.exports.setNjkTemplate = setNjkTemplate
module.exports.objDive = objDive

