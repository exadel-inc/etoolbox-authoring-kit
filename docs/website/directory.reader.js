const fs = require('fs');
// const glob = require('glob');

// const setNjkTemplate = (mdPath) => {
//   const withoutMd = mdPath.replace(".md", "");
//   const splitPath = withoutMd.split("/");
//   const value = splitPath.length - 1;
//   const withoutPath = splitPath[value];
//   const UpperCaseHeading = withoutPath[0].toUpperCase() + withoutPath.substring(1);
//   const prettifyHeading = UpperCaseHeading.split("-").join(" ")
//   return `---
// layout: content
// title: EAK ${prettifyHeading}
// seoTitle: EAK ${prettifyHeading}
// name: ${prettifyHeading}
// val: ${withoutPath}
// ${mdPath.match(/\.\.\/\.\.\/docs\/md\/.+/) ? "tags: components" : ""}
// ---

// {% mdRender '${mdPath}'  %}`
// }

// const setNjkPathsTemplate = (path) => {
//     if(path.match(/\.\.\/\.\.\/docs\/md\/.+/)){
//         const njkFileName = path.split("/");
//         const value = njkFileName.length - 1
//         return `views/components/${njkFileName[value]}`;
//     }
//     if(path.match(/\.\.\/\.\.\/samples\/README\.njk/) || path.match(/\.\.\/\.\.\/README\.njk/)){
//         const njkFileName = path.match(/\.\.\/\.\.\/samples\/README\.njk/) ? "samples.njk" : "installation.njk";
//         return `views/introduction/${njkFileName}`;
//     }
//     else {
//         const njkFileName = path.split("/");
//         const value = njkFileName.length - 1
//         return `views/temporarily/${njkFileName[value]}`;
//     }
// }

// let ignorePaths = ["../../CLA.md", "../../CONTRIBUTING.md"]
// let paths = []
// for(let i = 0; i<=10; i++){
//     if(i === 0) {
//         paths.push("../../*.md");
//         ignorePaths.push("../../*/*/node_modules/*/*.md");
//     };
//     paths.push(paths[paths.length-1].replace("*.md", "*/*.md"));
//     ignorePaths.push(ignorePaths[ignorePaths.length - 1].replace("node_modules", "node_modules/*"))

// }

// const globOptions = {
//     ignore: ignorePaths
// }

// paths.forEach((elem)=>{
//     return glob(elem,globOptions,function(err,files){
//         if(err) throw err
//         files.forEach(mdPath => {
//             const replaceMd = mdPath.replace(".md", ".njk");
//             fs.writeFile(setNjkPathsTemplate(replaceMd),setNjkTemplate(mdPath),function (err) {
//                 if (err) throw err;
//                 console.log('File is created successfully.');
//               });
//         });
//    });
// })


const setNjkTemplate = (fileName) => {
    const UpperCaseHeading = fileName[0].toUpperCase() + fileName.substring(1);
    const prettifyHeading = UpperCaseHeading.split("-").join(" ")
    return `---
layout: content
title: EAK ${prettifyHeading}
seoTitle: EAK ${prettifyHeading}
name: ${prettifyHeading}
val: ${fileName}
tags: components
---

{% mdRender '../md/${fileName}.md'  %}`
}



fs.readdir("../md", (err, files) => {
    if (err)throw err;
      files.forEach(mdFileName => {
        console.log(mdFileName)
        const withoutMd = mdFileName.replace(".md", "");
        fs.writeFile(`views/components/${withoutMd}.njk`,setNjkTemplate(withoutMd),function (err) {
            if (err) throw err;
            console.log('File is created successfully.');
          });
    });
});
