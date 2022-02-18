const fs = require('fs');
const path = require('path');
const {setNjkTemplate, objDive} = require('./directory.helpers');
const INIT_PATH = "../md"

let paths = ["../../README.md"];

let structure = {};

const readMdDir = async(initPath, folderPath = "/")=>{
    fs.readdir(initPath, async(err, files)=>{
        if(err) throw err;
        if(files.includes(".content")){
            const contentPath = initPath + "/.content";
            const data = fs.readFileSync(contentPath, "utf-8").toString().split("\n").join("").split("\r").filter(elem=> elem !== "");
            const contentObj ={};
            data.forEach(elem=>{
                if(elem.match(/title\:.+/)){
                    contentObj.title = elem.replace("title: ","");
                }
                if(elem.match(/files\:.+/)){
                    contentObj.files = elem.replace("files: ","").split(", ");
                }
            })
            writeFolders(contentObj, folderPath);
            const metaData = getMetaData(contentObj.files, initPath);
            const structureArr = metaData.map(elem=>{
                if(typeof elem === 'string' && elem.match(/.+\_folder/)){
                    return {
                        [elem.replace("_folder","")]:[]
                    };
                } else {
                    return elem;
                }
            })
            objDive(structure,contentObj.title, structureArr);
            await metaData.forEach((file,idx)=>{
                if(typeof file === "object" && file.fileName.match(/.+\.md/)){
                    fs.writeFile(`./views/${file.njkPath}`, setNjkTemplate(file, initPath, contentObj.title, idx), (err)=>{
                        if(err) throw err;
                        //console.log(`File ${file.fileName} is created`);
                    });
                };
                if(typeof file === "string" && file.match(/.+\_folder/)){
                    const newInitPath = initPath + "/" + file.replace("_folder", "");
                    if(contentObj.title){
                        readMdDir(newInitPath, folderPath + "/"+ contentObj.title);
                    } else {
                        readMdDir(newInitPath);
                    }
                }
            });
            fs.writeFileSync('./structure.json', JSON.stringify(structure));
        };
});
};
function writeFolders(contentObj, folderPath){
    if(contentObj.title){
        const files = fs.readdirSync(`./views${folderPath}`)
        if(files.includes(contentObj.title)){
            return;
        } else {
            fs.mkdirSync(path.join(`./views${folderPath}`, contentObj.title));
        };

    };
    return;
}

const getMetaData = (arr,initPath) => {
    const mdArr = arr.map((elem)=> {
        if(elem.match(/.+\.md/)){
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
                if(elem.includes("nav-title:")) metaObj.navTitle = removeExtraSymbols(elem.replace("nav-title: ", ""));
                if(elem.includes("title:")) metaObj.title = removeExtraSymbols(elem.replace("title: ", ""));
                if(elem.includes("description:")) metaObj.description = removeExtraSymbols(elem.replace("description: ", ""));
                if(elem.includes("keywords:")) metaObj.keywords = removeExtraSymbols(elem.replace("keywords: ", ""));
            });
            return {
                fileName: elem,
                metaData: metaObj,
                njkPath: filePath.replace("../md/","").replace(".md", ".njk")
            };}
            if(elem.match(/.+\_folder/)){
                return elem;
            }
    });
    return mdArr;
};

readMdDir(INIT_PATH)
