const paths = require('../paths.json')
const {markdown} = require('./markdown.lib');
const {JSDOM} = require('jsdom');
const fsAsync = require('fs').promises;

class Headings {
    static async getAllHeadings(){
        const res = {};
        const mdPaths = this.getAbsolutePath();
        for(const elem of mdPaths){
            const headingsArr = await this.parseFile(elem);
            Object.assign(res, {[elem.name]:headingsArr})
        }
        return res;
    }
    static async getHeadings(body){
        const headings = Array.from(body.querySelectorAll("h1,h2,h3,h4,h5,h6"));
        const res = headings.map( elem => {
            return {content: elem.textContent, tagName:elem.tagName};
        });
        return res;
    };
    static async readFiles(){
        const mdArr = this.getAbsolutePath();
        return mdArr.reduce( (acc,item) => {
            Object.assign(acc, this.parseFile(item));
        },{});
    };
    static async parseFile(item){
        const data = await fsAsync.readFile(item.path);
        const content = data.toString();
        const res = markdown.render(content);
        const {window} = new JSDOM();
        window.document.body.innerHTML += res;
        const result = await this.getHeadings(window.document.body, item.name);
        return result;
    };
    static getAbsolutePath(){
        const res = paths.pathsArr.map( elem => {
            const splitedElem = elem.split("/")
            const lastVal = splitedElem.length - 1
            if(elem === "../../README.md") return { path:elem, name: "installation"}
            return { path:elem, name:splitedElem[lastVal].replace(".md", "") }
        });
        return res;
    };
};

module.exports = function(config){
    config.addGlobalData('headings', async () => {
        const res = await Headings.getAllHeadings();
        return res
    });
};
