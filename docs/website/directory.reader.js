const fs = require('fs');
const path = require('path')
const {setNjkTemplate} = require('./directory.helpers')
const INIT_PATH = "../md"

let tags = [];
let paths = ["../../README.md"];
let structure = {};

const readMdDir = async(initPath, parentDirectory = "")=>{
    let dirToWrite;
fs.readdir(initPath, (err, files)=>{
    if(err) throw err;
    if(files.includes(".content")){
        const contentPath = initPath + "/.content"
        const data = fs.readFileSync(contentPath, "utf-8").toString().split("\n").join("").split("\r").filter(elem=> elem !== "");
        data.forEach(elem=>{
            if(elem.match(/dirToWrite=.+/)) dirToWrite = elem.replace(/dirToWrite=/, "");
            return
        })
        const mdFiles = data.filter((elem)=>elem.match(/.+\.md/));
        const mdArr = getMetaData(mdFiles, initPath)
        fs.writeFile('./paths.json', `{"pathsArr":[${paths.map(elem=>`"${elem}"`)}]}`, (err)=>{
            if(err) throw err;
            console.log("paths updated")
        })
        if(dirToWrite) {
            fs.mkdir(path.join( `./views/${parentDirectory && parentDirectory}`, dirToWrite), (err) => {
            if (err) {
                return console.error(err);
            }
            console.log('Directory created successfully!');
        })
            tags.push(dirToWrite)
            fs.writeFile('./views/_data/tagsList.json', `{
                "tags":[${tags.map(elem=> `"${elem}"`)}]
            }`, (err)=>{
                if(err) throw err;
                console.log("JSON is updated")
            })
            structure = Object.assign(structure, {[dirToWrite]: mdFiles})
            fs.writeFile('./structure.json', JSON.stringify(structure), (err)=>{
                if(err) throw err;
                console.log('Structure updated')
            })
        };
        mdArr.map((elem, idx)=>{
            console.log(elem.njkPath)
             fs.writeFile(`./views/${elem.njkPath.replace(".md", ".njk")}`,setNjkTemplate(elem, initPath, dirToWrite, idx), (err)=>{
                if(err) throw err;
                console.log('File is created successfully.')
            })
        })
    }
    files.forEach((elem)=>{
            if(!elem.match(/.+\.md/) && !elem.match(/\.content/)){
                const newPath = initPath + "/" + elem
                const parent = dirToWrite ? parentDirectory + "/" + dirToWrite : parentDirectory;
                console.log(parent)
                console.log(newPath)
                readMdDir(newPath, parent)
            }
        })
    return
})}

const getMetaData = (arr,initPath) => {
    const mdArr = arr.map((elem)=> {
        const filePath = initPath + '/' + elem;
        paths.push(filePath);
        const file = fs.readFileSync(filePath, 'utf-8').toString().split('-->');
        const filesMeta = file[0].replace("<!--", "");
        const metaToArr = filesMeta.split(",");
        const metaObj = {};
        const removeExtraSymbols = (str) => {
            return str.split("\r").join("").split("\n").join("");
        }
        metaToArr.forEach(elem=>{
            if(elem.includes("navTitle:")) metaObj.navTitle = removeExtraSymbols(elem.replace("navTitle: ", ""));
            if(elem.includes("title:")) metaObj.title = removeExtraSymbols(elem.replace("title: ", ""));
            if(elem.includes("description:")) metaObj.description = removeExtraSymbols(elem.replace("description: ", ""));
            if(elem.includes("keywords:")) metaObj.keywords = removeExtraSymbols(elem.replace("keywords: ", ""));
        });
        return {
            fileName: elem,
            metaData: metaObj,
            njkPath: filePath.replace("../md/","")
        };
    });
    return mdArr;
};


readMdDir(INIT_PATH, "")


