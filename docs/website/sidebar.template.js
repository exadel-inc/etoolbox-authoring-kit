const fs = require("fs");
const structure = require('./structure.json');

const ucFirst = (str)=>{
    return str[0].toUpperCase() + str.slice(1)
}


const setInfiniteTemplate = (elem, folder)=>{
    if(elem.hasOwnProperty("fileName") && elem.hasOwnProperty("metaData") && elem.hasOwnProperty("njkPath")){
        return `<li class="sidebar-nav-secondary-item {{ 'active' if isActive }} {{ 'draft' if isDraft }}"
        {% if isActive %}aria-selected="true"{% endif %}>
      <a class="sidebar-nav-secondary-link"{% if isActive %}aria-current="page"{% endif %} href="/${folder}/${elem.fileName.replace(".md","")}">
      ${elem.fileName}
      </a>
    </li>`
    } else if(!elem.hasOwnProperty("fileName") && !elem.hasOwnProperty("metaData") && !elem.hasOwnProperty("njkPath") && Object.keys(elem).length === 1){
        const key = Object.keys(elem)[0]
        const content = elem[key].map(el=>{
            return setInfiniteTemplate(el, folder+"/"+key)
        }).join("")
        return `
        <div class="sidebar-nav-item-heading">
        <esl-trigger class="sidebar-nav-item-trigger sidebar-nav-item-arrow"target="::parent::next">
        ${ucFirst(key)}
        </esl-trigger></div>
        <esl-panel id="sidebar-${key}-menu">
            ${content}
        </esl-panel>`
    }
}

const setSidebarTemplate = () =>{
    const structureFolders = Object.keys(structure);
    return `
<esl-d-sidebar id="sidebar" class="open" aria-label="Site Navigation">
  <nav class="sidebar-nav">
    <a class="sidebar-heading" href="{{ '/' | url }}">
      <span class="sidebar-logo fill-light">{% include "static/assets/helpers/icon.svg" %}</span>
      <span class="sidebar-title">{{ site.name }}</span>
    </a>
    <div class="sidebar-content">
      <div class="sidebar-nav-list esl-scrollable-content">
        <li class="sidebar-nav-item">
            <div class="sidebar-nav-item-heading">
                <a class="sidebar-nav-link" href="/introduction/installation">Introduction</a>
            </div>
        </li>
        <li class="sidebar-nav-item">
        ${structureFolders.map(folder => {
            return `
            <div class="sidebar-nav-item-heading {{ 'active' if isPrimaryActive }}">
                <esl-trigger class="sidebar-nav-item-trigger sidebar-nav-item-arrow" target="::parent::next"
                {% if isPrimaryActive %}active{% endif %}>
                ${ucFirst(folder)}
                </esl-trigger>
            </div>
            <esl-panel id="sidebar-${folder}-menu"
             class="sidebar-nav-secondary">
            <ul class="sidebar-nav-secondary-list">
            ${structure[folder].map(element=>{return setInfiniteTemplate(element, folder)}).join("")}
            </ul>
            </esl-panel>`}).join("")}
        </li>
      </div>
      <esl-scrollbar class="sidebar-scrollbar" target="::prev"></esl-scrollbar>
    </div>
    <esl-trigger class="sidebar-trigger-btn sidebar-arrows-icon" target="#sidebar" data-store></esl-trigger>
  </nav>
</esl-d-sidebar>`
}

fs.writeFileSync("./views/_includes/navigation/sidebar.njk", setSidebarTemplate());


module.exports.setSidebarTemplate = setSidebarTemplate;
