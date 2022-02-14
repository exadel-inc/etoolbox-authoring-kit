const setNjkTemplate = (elem, path, dirTag, idx) => {
    const elemWithoutMd = elem.fileName.replace(".md", "");
    const replaceDash = elemWithoutMd.replace("-", " ");
    const ElemUpperCase= replaceDash[0].toUpperCase() + replaceDash.substring(1)

    return `---
layout: content
title: EAK ${elem.metaData.title || ElemUpperCase}
seoTitle: EAK ${ElemUpperCase}
navTitle: ${elem.metaData.navTitle || ElemUpperCase}
description: ${elem.metaData.description || ElemUpperCase}
keywords: ${elem.metaData.keywords || dirTag}
name: ${ElemUpperCase}
val: ${elemWithoutMd}
tags: ${dirTag ? dirTag : "components"}
orderValue: ${idx+1}
---

{% mdRender '${path + "/" + elem.fileName}' %}`
}

module.exports.setNjkTemplate = setNjkTemplate

