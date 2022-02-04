/******/ (() => { // webpackBootstrap
/******/ 	"use strict";
/******/ 	var __webpack_modules__ = ({

/***/ "./node_modules/@exadel/esl/modules/esl-alert/core/esl-alert.js":
/*!**********************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-alert/core/esl-alert.js ***!
  \**********************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ESLAlert": () => (/* binding */ ESLAlert)
/* harmony export */ });
/* harmony import */ var _esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! ../../esl-utils/environment/export-ns */ "./node_modules/@exadel/esl/modules/esl-utils/environment/export-ns.js");
/* harmony import */ var _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! ../../esl-utils/decorators/bind */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/bind.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/attr.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/json-attr.js");
/* harmony import */ var _esl_toggleable_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../esl-toggleable/core */ "./node_modules/@exadel/esl/modules/esl-toggleable/core/esl-toggleable.js");
/* harmony import */ var _esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../esl-utils/environment/device-detector */ "./node_modules/@exadel/esl/modules/esl-utils/environment/device-detector.js");
/* harmony import */ var _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../../esl-utils/dom/class */ "./node_modules/@exadel/esl/modules/esl-utils/dom/class.js");
/* harmony import */ var _esl_utils_fixes_ie_fixes__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../esl-utils/fixes/ie-fixes */ "./node_modules/@exadel/esl/modules/esl-utils/fixes/ie-fixes.js");
/* harmony import */ var _esl_traversing_query_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../../esl-traversing-query/core */ "./node_modules/@exadel/esl/modules/esl-traversing-query/core/esl-traversing-query.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var ESLAlert_1;








/**
 * ESLAlert component
 *
 * @author Julia Murashko
 *
 * ESLAlert is a component to show small notifications on your pages. ESLAlert can have multiple instances on the page.
 */
let ESLAlert = ESLAlert_1 = class ESLAlert extends _esl_toggleable_core__WEBPACK_IMPORTED_MODULE_0__.ESLToggleable {
    static get observedAttributes() {
        return ['target'];
    }
    /** Creates global alert instance (using body element as a base) */
    static init(options) {
        let alert = document.querySelector(`body > ${ESLAlert_1.is}`);
        if (!alert) {
            alert = document.createElement(ESLAlert_1.is);
            options && Object.assign(alert, options);
            document.body.appendChild(alert);
        }
        return alert;
    }
    mergeDefaultParams(params) {
        const type = this.constructor;
        return Object.assign({}, type.defaultConfig, this.defaultParams || {}, params || {});
    }
    attributeChangedCallback(attrName, oldVal, newVal) {
        if (!this.connected)
            return;
        if (attrName === 'target') {
            this.$target = _esl_traversing_query_core__WEBPACK_IMPORTED_MODULE_1__.TraversingQuery.first(this.target);
        }
    }
    connectedCallback() {
        super.connectedCallback();
        this.setAttribute('role', this.getAttribute('role') || 'alert');
        this.$content = document.createElement('div');
        this.$content.className = 'esl-alert-content';
        this.innerHTML = '';
        this.appendChild(this.$content);
        if (_esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_2__.DeviceDetector.isIE)
            this.appendChild((0,_esl_utils_fixes_ie_fixes__WEBPACK_IMPORTED_MODULE_3__.createZIndexIframe)());
        if (this.target) {
            this.$target = _esl_traversing_query_core__WEBPACK_IMPORTED_MODULE_1__.TraversingQuery.first(this.target, this);
        }
    }
    unbindEvents() {
        super.unbindEvents();
        this.unbindTargetEvents();
    }
    /** Target element to listen to activation events */
    get $target() {
        return this._$target;
    }
    set $target($el) {
        this.unbindTargetEvents();
        this._$target = $el;
        this.bindTargetEvents();
    }
    bindTargetEvents() {
        if (!this.$target || !this.connected)
            return;
        this.$target.addEventListener(`${ESLAlert_1.eventNs}:show`, this._onTargetEvent);
        this.$target.addEventListener(`${ESLAlert_1.eventNs}:hide`, this._onTargetEvent);
    }
    unbindTargetEvents() {
        if (!this.$target)
            return;
        this.$target.removeEventListener(`${ESLAlert_1.eventNs}:show`, this._onTargetEvent);
        this.$target.removeEventListener(`${ESLAlert_1.eventNs}:hide`, this._onTargetEvent);
    }
    onShow(params) {
        if (this._clearTimeout)
            window.clearTimeout(this._clearTimeout);
        if (params.html || params.text) {
            this.render(params);
            super.onShow(params);
        }
        this.hide(params);
    }
    onHide(params) {
        super.onHide(params);
        this._clearTimeout = window.setTimeout(() => this.clear(), params.hideTime);
    }
    render({ text, html, cls }) {
        _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_4__.CSSClassUtils.remove(this, this.activeCls);
        _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_4__.CSSClassUtils.add(this, this.activeCls = cls);
        if (html)
            this.$content.innerHTML = html;
        if (text)
            this.$content.textContent = text;
    }
    clear() {
        this.$content.innerHTML = '';
        _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_4__.CSSClassUtils.remove(this, this.activeCls);
    }
    _onTargetEvent(e) {
        if (e.type === `${ESLAlert_1.eventNs}:show`) {
            const params = Object.assign({}, e.detail, { force: true });
            this.show(params);
        }
        if (e.type === `${ESLAlert_1.eventNs}:hide`) {
            const params = Object.assign({}, { hideDelay: 0 }, e.detail, { force: true });
            this.hide(params);
        }
        e.stopPropagation();
    }
};
ESLAlert.is = 'esl-alert';
ESLAlert.eventNs = 'esl:alert';
/** Default show/hide params for all ESLAlert instances */
ESLAlert.defaultConfig = {
    hideTime: 300,
    hideDelay: 2500
};
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_5__.attr)({ defaultValue: '::parent' })
], ESLAlert.prototype, "target", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_6__.jsonAttr)()
], ESLAlert.prototype, "defaultParams", void 0);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_7__.bind
], ESLAlert.prototype, "_onTargetEvent", null);
ESLAlert = ESLAlert_1 = __decorate([
    (0,_esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_8__.ExportNs)('Alert')
], ESLAlert);



/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-base-element/core/esl-base-element.js":
/*!************************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-base-element/core/esl-base-element.js ***!
  \************************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ESLBaseElement": () => (/* binding */ ESLBaseElement)
/* harmony export */ });
/* harmony import */ var _esl_utils_dom_events__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../esl-utils/dom/events */ "./node_modules/@exadel/esl/modules/esl-utils/dom/events.js");

/**
 * Base class for ESL custom elements.
 * Allows to define custom element with the optional custom tag name.
 */
class ESLBaseElement extends HTMLElement {
    constructor() {
        super(...arguments);
        this._connected = false;
    }
    connectedCallback() {
        this._connected = true;
        this.classList.add(this.constructor.is);
    }
    disconnectedCallback() {
        this._connected = false;
    }
    /** Check that the element is connected and `connectedCallback` has been executed */
    get connected() {
        return this._connected;
    }
    /**
     * Dispatch component custom event.
     * Uses 'esl:' prefix for event name, overridable to customize event namespaces.
     * @param eventName - event name
     * @param eventInit - custom event init. See {@link CustomEventInit}
     */
    $$fire(eventName, eventInit) {
        return _esl_utils_dom_events__WEBPACK_IMPORTED_MODULE_0__.EventUtils.dispatch(this, 'esl:' + eventName, eventInit);
    }
    /**
     * Register component in the {@link customElements} registry
     * @param tagName - custom tag name to register custom element
     */
    static register(tagName) {
        tagName = tagName || this.is;
        if (!tagName)
            throw new Error('Can not define custom element');
        const constructor = customElements.get(tagName);
        if (constructor) {
            if (constructor.is !== tagName)
                throw new Error('Element declaration tag inconsistency');
            return;
        }
        if (this.is !== tagName) {
            this.is = tagName;
        }
        customElements.define(tagName, this);
    }
    static get registered() {
        return customElements.whenDefined(this.is);
    }
}
/** Custom element tag name */
ESLBaseElement.is = '';


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/attr.js":
/*!******************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-base-element/decorators/attr.js ***!
  \******************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "attr": () => (/* binding */ attr)
/* harmony export */ });
/* harmony import */ var _esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../esl-utils/misc/format */ "./node_modules/@exadel/esl/modules/esl-utils/misc/format.js");

function buildSimpleDescriptor(attrName, readOnly, defaultValue) {
    function get() {
        const value = this.getAttribute(attrName);
        return typeof value === 'string' ? value : defaultValue;
    }
    function set(value) {
        if (value === undefined || value === null || value === false) {
            this.removeAttribute(attrName);
        }
        else {
            this.setAttribute(attrName, value === true ? '' : value);
        }
    }
    return readOnly ? { get } : { get, set };
}
const buildAttrName = (propName, dataAttr) => dataAttr ? `data-${(0,_esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_0__.toKebabCase)(propName)}` : (0,_esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_0__.toKebabCase)(propName);
/**
 * Decorator to map current property to element attribute value.
 * Maps string type property.
 * @param config - mapping configuration. See {@link AttrDescriptor}
 */
const attr = (config = {}) => {
    config = Object.assign({ defaultValue: '' }, config);
    return (target, propName) => {
        const attrName = buildAttrName(config.name || propName, !!config.dataAttr);
        Object.defineProperty(target, propName, buildSimpleDescriptor(attrName, !!config.readonly, config.defaultValue));
    };
};


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/bool-attr.js":
/*!***********************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-base-element/decorators/bool-attr.js ***!
  \***********************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "boolAttr": () => (/* binding */ boolAttr)
/* harmony export */ });
/* harmony import */ var _esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../esl-utils/misc/format */ "./node_modules/@exadel/esl/modules/esl-utils/misc/format.js");

function buildConditionalDescriptor(attrName, readOnly) {
    function get() {
        return this.hasAttribute(attrName);
    }
    function set(value) {
        this.toggleAttribute(attrName, value);
    }
    return readOnly ? { get } : { get, set };
}
const buildAttrName = (propName, dataAttr) => dataAttr ? `data-${(0,_esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_0__.toKebabCase)(propName)}` : (0,_esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_0__.toKebabCase)(propName);
/**
 * Decorator to map current property to element boolean (marker) attribute state.
 * Maps boolean type property.
 * @param config - mapping configuration. See {@link BoolAttrDescriptor}
 */
const boolAttr = (config = {}) => {
    return (target, propName) => {
        const attrName = buildAttrName(config.name || propName, !!config.dataAttr);
        Object.defineProperty(target, propName, buildConditionalDescriptor(attrName, !!config.readonly));
    };
};


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/json-attr.js":
/*!***********************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-base-element/decorators/json-attr.js ***!
  \***********************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "jsonAttr": () => (/* binding */ jsonAttr)
/* harmony export */ });
/* harmony import */ var _esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../esl-utils/misc/format */ "./node_modules/@exadel/esl/modules/esl-utils/misc/format.js");

function buildJsonAttrDescriptor(attrName, readOnly, defaultValue) {
    function get() {
        const attrContent = (this.getAttribute(attrName) || '').trim();
        return (0,_esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_0__.evaluate)(attrContent, defaultValue);
    }
    function set(value) {
        if (typeof value !== 'object') {
            console.error('Can not set json value: value should be object');
        }
        try {
            if (value) {
                const serializedValue = JSON.stringify(value);
                this.setAttribute(attrName, serializedValue);
            }
            else {
                this.removeAttribute(attrName);
            }
        }
        catch (e) {
            console.error('[ESL] jsonAttr: Can not set json value ', e);
        }
    }
    return readOnly ? { get } : { get, set };
}
const buildAttrName = (propName, dataAttr) => dataAttr ? `data-${(0,_esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_0__.toKebabCase)(propName)}` : (0,_esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_0__.toKebabCase)(propName);
/**
 * Decorator to map current property to element attribute value using JSON (de-)serialization rules.
 * Maps object type property.
 * @param config - mapping configuration. See {@link JsonAttrDescriptor}
 */
const jsonAttr = (config = {}) => {
    config = Object.assign({ defaultValue: {} }, config);
    return (target, propName) => {
        const attrName = buildAttrName(config.name || propName, !!config.dataAttr);
        Object.defineProperty(target, propName, buildJsonAttrDescriptor(attrName, !!config.readonly, config.defaultValue));
    };
};


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-media-query/core/common/env-shortcuts.js":
/*!***************************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-media-query/core/common/env-shortcuts.js ***!
  \***************************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ESLEnvShortcuts": () => (/* binding */ ESLEnvShortcuts)
/* harmony export */ });
/* harmony import */ var _esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../../../esl-utils/environment/device-detector */ "./node_modules/@exadel/esl/modules/esl-utils/environment/device-detector.js");
/* harmony import */ var _esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../../esl-utils/environment/export-ns */ "./node_modules/@exadel/esl/modules/esl-utils/environment/export-ns.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};


const shortcuts = new Map();
/**
 * Static shortcuts preprocessor. Used to store device related shortcuts.
 * @author Alexey Stsefanovich (ala'n)
 *
 * @implements IMediaQueryPreprocessor statically
 */
let ESLEnvShortcuts = class ESLEnvShortcuts {
    /**
     * Add mapping
     * @param shortcut - term to find in query
     * @param value - media query string or boolean result (that represents `all` or `not all` conditions)
     */
    static add(shortcut, value) {
        if (!['boolean', 'string'].includes(typeof value))
            value = false;
        shortcuts.set(shortcut.toLowerCase(), value);
    }
    /** Remove mapping for passed shortcut term */
    static remove(shortcut) {
        return shortcuts.delete(shortcut.toLowerCase());
    }
    /** Replaces shortcut to registered result */
    static process(match) {
        if (shortcuts.has(match))
            return shortcuts.get(match);
    }
};
// For debug purposes
ESLEnvShortcuts._shortcuts = shortcuts;
ESLEnvShortcuts = __decorate([
    (0,_esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_0__.ExportNs)('EnvShortcuts')
], ESLEnvShortcuts);

// Touch check
ESLEnvShortcuts.add('touch', _esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_1__.DeviceDetector.isTouchDevice);
// Basic device type shortcuts
ESLEnvShortcuts.add('bot', _esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_1__.DeviceDetector.isBot);
ESLEnvShortcuts.add('mobile', _esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_1__.DeviceDetector.isMobile);
ESLEnvShortcuts.add('desktop', !_esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_1__.DeviceDetector.isMobile);
ESLEnvShortcuts.add('android', _esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_1__.DeviceDetector.isAndroid);
ESLEnvShortcuts.add('ios', _esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_1__.DeviceDetector.isMobileIOS);
// Basic browser shortcuts
ESLEnvShortcuts.add('ie', _esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_1__.DeviceDetector.isIE);
ESLEnvShortcuts.add('edge', _esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_1__.DeviceDetector.isEdgeHTML);
ESLEnvShortcuts.add('gecko', _esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_1__.DeviceDetector.isGecko);
ESLEnvShortcuts.add('blink', _esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_1__.DeviceDetector.isBlink);
ESLEnvShortcuts.add('safari', _esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_1__.DeviceDetector.isSafari);
ESLEnvShortcuts.add('safari-ios', _esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_1__.DeviceDetector.isMobileSafari);


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-media-query/core/common/screen-breakpoint.js":
/*!*******************************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-media-query/core/common/screen-breakpoint.js ***!
  \*******************************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ESLScreenBreakpoints": () => (/* binding */ ESLScreenBreakpoints),
/* harmony export */   "ESLScreenBreakpoint": () => (/* binding */ ESLScreenBreakpoint)
/* harmony export */ });
/* harmony import */ var _esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../../esl-utils/environment/export-ns */ "./node_modules/@exadel/esl/modules/esl-utils/environment/export-ns.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var ESLScreenBreakpoints_1;

const registry = new Map();
/**
 * ESL Screen Breakpoints registry
 * @author Yuliya Adamskaya, Alexey Stsefanovich (ala'n)
 *
 * Screen Breakpoint registry is used to provide custom breakpoints for {@link ESLMediaQuery}
 *
 * @implements IMediaQueryPreprocessor statically
 */
let ESLScreenBreakpoints = ESLScreenBreakpoints_1 = class ESLScreenBreakpoints {
    /**
     * Add or replace breakpoint shortcut that can be used inside ESLMediaQuery
     * @param name - name of shortcut
     * @param minWidth - min width for breakpoint
     * @param maxWidth - max width for breakpoint
     */
    static add(name, minWidth, maxWidth) {
        name = name.toLowerCase();
        if (ESLScreenBreakpoints_1.BP_NAME_REGEXP.test(name)) {
            const current = registry.get(name);
            registry.set(name, new ESLScreenBreakpoint(name, minWidth, maxWidth));
            return current;
        }
        throw new Error('The shortcut should consist only of Latin characters and be at least one character long.');
    }
    /** Removes screen breakpoint */
    static remove(name) {
        return registry.delete(name.toLowerCase());
    }
    /** @returns known breakpoint shortcut instance */
    static get(name) {
        return registry.get((name || '').toLowerCase());
    }
    /** All available breakpoints names */
    static get names() {
        const keys = [];
        registry.forEach((value, key) => keys.push(key));
        return keys;
    }
    /** @returns breakpoints shortcut replacement */
    static process(term) {
        const [, sign, bp] = term.match(ESLScreenBreakpoints_1.BP_REGEXP) || [];
        const shortcut = ESLScreenBreakpoints_1.get(bp);
        if (!shortcut)
            return;
        if (sign === '+')
            return shortcut.mediaQueryGE;
        if (sign === '-')
            return shortcut.mediaQueryLE;
        return shortcut.mediaQuery;
    }
};
ESLScreenBreakpoints.BP_REGEXP = /^([+-]?)([a-z]+)/i;
ESLScreenBreakpoints.BP_NAME_REGEXP = /^[a-z]+/i;
ESLScreenBreakpoints = ESLScreenBreakpoints_1 = __decorate([
    (0,_esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_0__.ExportNs)('ScreenBreakpoints')
], ESLScreenBreakpoints);

/** ESL Screen Breakpoint description */
class ESLScreenBreakpoint {
    constructor(name, min, max) {
        this.name = name;
        this.min = min;
        this.max = max;
    }
    get mediaQuery() {
        return `(min-width: ${this.min}px) and (max-width: ${this.max}px)`;
    }
    get mediaQueryGE() {
        return `(min-width: ${this.min}px)`;
    }
    get mediaQueryLE() {
        return `(max-width: ${this.max}px)`;
    }
    toString() {
        return `[${this.name}]: ${this.min} to ${this.max}`;
    }
}
// Defaults
ESLScreenBreakpoints.add('xs', 1, 767);
ESLScreenBreakpoints.add('sm', 768, 991);
ESLScreenBreakpoints.add('md', 992, 1199);
ESLScreenBreakpoints.add('lg', 1200, 1599);
ESLScreenBreakpoints.add('xl', 1600, 999999);


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-media-query/core/common/screen-dpr.js":
/*!************************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-media-query/core/common/screen-dpr.js ***!
  \************************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ESLScreenDPR": () => (/* binding */ ESLScreenDPR)
/* harmony export */ });
/* harmony import */ var _esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../../../esl-utils/environment/export-ns */ "./node_modules/@exadel/esl/modules/esl-utils/environment/export-ns.js");
/* harmony import */ var _esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../../esl-utils/environment/device-detector */ "./node_modules/@exadel/esl/modules/esl-utils/environment/device-detector.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var ESLScreenDPR_1;


/**
 * DPR preprocessor. Used to replace DPR shortcuts.
 * @author Alexey Stsefanovich (ala'n)
 *
 * @implements IMediaQueryPreprocessor statically
 */
let ESLScreenDPR = ESLScreenDPR_1 = class ESLScreenDPR {
    static toDPI(dpr) {
        return (96 * dpr).toFixed(1);
    }
    static process(match) {
        if (!ESLScreenDPR_1.VALUE_REGEXP.test(match))
            return;
        const dpr = parseFloat(match);
        if (dpr < 0 || isNaN(dpr))
            return;
        if (ESLScreenDPR_1.ignoreBotsDpr && _esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_0__.DeviceDetector.isBot && dpr > 1)
            return 'not all';
        if (_esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_0__.DeviceDetector.isSafari)
            return `(-webkit-min-device-pixel-ratio: ${dpr})`;
        return `(min-resolution: ${ESLScreenDPR_1.toDPI(dpr)}dpi)`;
    }
};
ESLScreenDPR.VALUE_REGEXP = /(\d(\.\d)?)x/;
/** Option to exclude dpr greater then 2 for bots */
ESLScreenDPR.ignoreBotsDpr = false;
ESLScreenDPR = ESLScreenDPR_1 = __decorate([
    (0,_esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_1__.ExportNs)('ScreenDPR')
], ESLScreenDPR);



/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-media-query/core/conditions/media-query-base.js":
/*!**********************************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-media-query/core/conditions/media-query-base.js ***!
  \**********************************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ALL": () => (/* binding */ ALL),
/* harmony export */   "NOT_ALL": () => (/* binding */ NOT_ALL)
/* harmony export */ });
/**
 * Const media condition implementation
 * @author Alexey Stsefanovich (ala'n)
 *
 * Ignores listeners always return the same result.
 * Have only two instances: {@link ALL} and {@link NOT_ALL}
 */
class MediaQueryConstCondition {
    constructor(_matches) {
        this._matches = _matches;
    }
    get matches() {
        return this._matches;
    }
    addListener(cb) { }
    removeListener(cb) { }
    optimize() {
        return this;
    }
    toString() {
        return this._matches ? 'all' : 'not all';
    }
    /** Compare const media condition with the passed query instance or string */
    eq(val) {
        return val.toString().trim() === this.toString();
    }
}
const ALL = new MediaQueryConstCondition(true);
const NOT_ALL = new MediaQueryConstCondition(false);


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-media-query/core/conditions/media-query-condition.js":
/*!***************************************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-media-query/core/conditions/media-query-condition.js ***!
  \***************************************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "MediaQueryCondition": () => (/* binding */ MediaQueryCondition)
/* harmony export */ });
/* harmony import */ var _media_query_base__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./media-query-base */ "./node_modules/@exadel/esl/modules/esl-media-query/core/conditions/media-query-base.js");

/**
 * Simple media condition implementation
 * @author Alexey Stsefanovich (ala'n)
 *
 * Wraps matchMedia instance
 */
class MediaQueryCondition {
    constructor(query, inverted = false) {
        this._inverted = inverted;
        this._mq = matchMedia(query.trim() || 'all');
    }
    get matches() {
        return this._inverted ? !this._mq.matches : this._mq.matches;
    }
    addListener(listener) {
        if (typeof this._mq.addEventListener === 'function') {
            this._mq.addEventListener('change', listener);
        }
        else {
            this._mq.addListener(listener);
        }
    }
    removeListener(listener) {
        if (typeof this._mq.removeEventListener === 'function') {
            this._mq.removeEventListener('change', listener);
        }
        else {
            this._mq.removeListener(listener);
        }
    }
    /** Optimize query. Can simplify query to {@link MediaQueryConstCondition} */
    optimize() {
        if (_media_query_base__WEBPACK_IMPORTED_MODULE_0__.ALL.eq(this))
            return this._inverted ? _media_query_base__WEBPACK_IMPORTED_MODULE_0__.NOT_ALL : _media_query_base__WEBPACK_IMPORTED_MODULE_0__.ALL;
        if (_media_query_base__WEBPACK_IMPORTED_MODULE_0__.NOT_ALL.eq(this))
            return this._inverted ? _media_query_base__WEBPACK_IMPORTED_MODULE_0__.ALL : _media_query_base__WEBPACK_IMPORTED_MODULE_0__.NOT_ALL;
        return this;
    }
    toString() {
        const query = this._mq.media;
        const inverted = this._inverted;
        const complex = inverted && /\)[\s\w]+\(/.test(query);
        return (inverted ? 'not ' : '') + (complex ? `(${query})` : query);
    }
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-media-query/core/conditions/media-query-containers.js":
/*!****************************************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-media-query/core/conditions/media-query-containers.js ***!
  \****************************************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "MediaQueryConjunction": () => (/* binding */ MediaQueryConjunction),
/* harmony export */   "MediaQueryDisjunction": () => (/* binding */ MediaQueryDisjunction)
/* harmony export */ });
/* harmony import */ var _esl_utils_abstract_observable__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../../esl-utils/abstract/observable */ "./node_modules/@exadel/esl/modules/esl-utils/abstract/observable.js");
/* harmony import */ var _media_query_base__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./media-query-base */ "./node_modules/@exadel/esl/modules/esl-media-query/core/conditions/media-query-base.js");


/**
 * Abstract multiple media conditions container
 * @author Alexey Stsefanovich (ala'n)
 *
 * Observe all child items. Dispatch changes when the whole condition result is changed
 */
class MediaQueryContainer extends _esl_utils_abstract_observable__WEBPACK_IMPORTED_MODULE_0__.Observable {
    constructor(items = []) {
        super();
        this.items = items;
        this._matches = this.matches;
        this._onChildChange = this._onChildChange.bind(this);
    }
    addListener(listener) {
        super.addListener(listener);
        if (this._listeners.size > 1)
            return;
        this.items.forEach((item) => item.addListener(this._onChildChange));
    }
    removeListener(listener) {
        super.removeListener(listener);
        if (this._listeners.size)
            return;
        this.items.forEach((item) => item.removeListener(this._onChildChange));
    }
    get matches() {
        return false;
    }
    /** Exclude const conditions. Unwrap empty or trivial (with one item) containers */
    optimize() {
        return this;
    }
    /** Handle query change and dispatch it on top level in case result value is changed */
    _onChildChange() {
        const { matches } = this;
        if (this._matches === matches)
            return;
        this.fire(this._matches = matches);
    }
}
/** Conjunction (AND) group of media conditions */
class MediaQueryConjunction extends MediaQueryContainer {
    get matches() {
        return this.items.every((item) => item.matches);
    }
    optimize() {
        const optimizedItems = this.items.map((item) => item.optimize());
        if (optimizedItems.some((item) => _media_query_base__WEBPACK_IMPORTED_MODULE_1__.NOT_ALL.eq(item)))
            return _media_query_base__WEBPACK_IMPORTED_MODULE_1__.NOT_ALL;
        const items = optimizedItems.filter((item) => !_media_query_base__WEBPACK_IMPORTED_MODULE_1__.ALL.eq(item));
        if (items.length === 0)
            return _media_query_base__WEBPACK_IMPORTED_MODULE_1__.ALL;
        if (items.length === 1)
            return items[0];
        return new MediaQueryConjunction(items);
    }
    toString() {
        return this.items.join(' and ');
    }
}
/** Disjunction (OR) group of media conditions */
class MediaQueryDisjunction extends MediaQueryContainer {
    get matches() {
        return this.items.some((item) => item.matches);
    }
    optimize() {
        const optimizedItems = this.items.map((item) => item.optimize());
        if (optimizedItems.some((item) => _media_query_base__WEBPACK_IMPORTED_MODULE_1__.ALL.eq(item)))
            return _media_query_base__WEBPACK_IMPORTED_MODULE_1__.ALL;
        const items = optimizedItems.filter((item) => !_media_query_base__WEBPACK_IMPORTED_MODULE_1__.NOT_ALL.eq(item));
        if (items.length === 0)
            return _media_query_base__WEBPACK_IMPORTED_MODULE_1__.NOT_ALL;
        if (items.length === 1)
            return items[0];
        return new MediaQueryDisjunction(items);
    }
    toString(pretty = false) {
        return this.items.join(pretty ? ' or ' : ', ');
    }
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-media-query/core/esl-media-query.js":
/*!**********************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-media-query/core/esl-media-query.js ***!
  \**********************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ESLMediaQuery": () => (/* binding */ ESLMediaQuery)
/* harmony export */ });
/* harmony import */ var _esl_utils_decorators_memoize__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../esl-utils/decorators/memoize */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/memoize.js");
/* harmony import */ var _esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../../esl-utils/environment/export-ns */ "./node_modules/@exadel/esl/modules/esl-utils/environment/export-ns.js");
/* harmony import */ var _common_screen_dpr__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ./common/screen-dpr */ "./node_modules/@exadel/esl/modules/esl-media-query/core/common/screen-dpr.js");
/* harmony import */ var _common_screen_breakpoint__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ./common/screen-breakpoint */ "./node_modules/@exadel/esl/modules/esl-media-query/core/common/screen-breakpoint.js");
/* harmony import */ var _common_env_shortcuts__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! ./common/env-shortcuts */ "./node_modules/@exadel/esl/modules/esl-media-query/core/common/env-shortcuts.js");
/* harmony import */ var _conditions_media_query_base__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./conditions/media-query-base */ "./node_modules/@exadel/esl/modules/esl-media-query/core/conditions/media-query-base.js");
/* harmony import */ var _conditions_media_query_condition__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./conditions/media-query-condition */ "./node_modules/@exadel/esl/modules/esl-media-query/core/conditions/media-query-condition.js");
/* harmony import */ var _conditions_media_query_containers__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./conditions/media-query-containers */ "./node_modules/@exadel/esl/modules/esl-media-query/core/conditions/media-query-containers.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var ESLMediaQuery_1;








/**
 * ESL Media Query
 * Provides special media condition syntax - ESLMediaQuery
 * @author Alexey Stsefanovich (ala'n), Yuliya Adamskaya, Natallia Harshunova
 *
 * Utility to support extended MediaQuery features
 * Supports
 * - CSS MediaQuery matching check
 * - DPR display queries (`@x1|@x2|@x3`)
 * - Registered screen default sizes (breakpoints) shortcuts (`@[-|+](XS|SM|MD|LG|XL)`)
 * - Device and browser shortcuts (`@MOBILE|@DESKTOP|@IE`)
 * - Custom static shortcuts and custom query preprocessors
 * - `not` logic operation (can have multiple not operators before any term of the query)
 * - `or` or `,` logical operator (have a lowest priority)
 * - Query matching change listeners
 *
 * Building query process:
 *
 * [Building query logical tree] - [preprocess nodes queries] - [building native MediaQueryList nodes] - [query tree optimization]
 */
let ESLMediaQuery = ESLMediaQuery_1 = class ESLMediaQuery {
    /** Add {@link IMediaQueryPreprocessor} instance for query preprocessing step */
    static use(preprocessor) {
        this._preprocessors.unshift(preprocessor);
        return this;
    }
    /** Cached method to create {@link ESLMediaQuery} condition instance from query string */
    static for(query) {
        return ESLMediaQuery_1.from(query);
    }
    /** Creates {@link ESLMediaQuery} condition instance from query string */
    static from(query) {
        const conjunctions = query.split(/\sor\s|,/).map((term) => {
            const conditions = term.split(/\sand\s/).map(ESLMediaQuery_1.parseSimpleQuery);
            return new _conditions_media_query_containers__WEBPACK_IMPORTED_MODULE_0__.MediaQueryConjunction(conditions);
        });
        return new _conditions_media_query_containers__WEBPACK_IMPORTED_MODULE_0__.MediaQueryDisjunction(conjunctions).optimize();
    }
    /** Preprocess simple query term by applying replacers and shortcuts rules */
    static preprocess(term) {
        if (!this.SHORTCUT_PATTERN.test(term))
            return term;
        const shortcut = term.trim().substring(1).toLowerCase();
        for (const replacer of this._preprocessors) {
            const result = replacer.process(shortcut);
            if (typeof result === 'string')
                return result;
            if (typeof result === 'boolean')
                return result ? 'all' : 'not all';
        }
        return term;
    }
    /** Creates simple {@link ESLMediaQuery} condition */
    static parseSimpleQuery(term) {
        const query = term.replace(/^\s*not\s+/, '');
        const queryInverted = query !== term;
        const processedQuery = ESLMediaQuery_1.preprocess(query);
        const sanitizedQuery = processedQuery.replace(/^\s*not\s+/, '');
        const resultInverted = processedQuery !== sanitizedQuery;
        const invert = queryInverted !== resultInverted;
        if (_conditions_media_query_base__WEBPACK_IMPORTED_MODULE_1__.ALL.eq(sanitizedQuery))
            return invert ? _conditions_media_query_base__WEBPACK_IMPORTED_MODULE_1__.NOT_ALL : _conditions_media_query_base__WEBPACK_IMPORTED_MODULE_1__.ALL;
        if (_conditions_media_query_base__WEBPACK_IMPORTED_MODULE_1__.NOT_ALL.eq(sanitizedQuery))
            return invert ? _conditions_media_query_base__WEBPACK_IMPORTED_MODULE_1__.ALL : _conditions_media_query_base__WEBPACK_IMPORTED_MODULE_1__.NOT_ALL;
        return new _conditions_media_query_condition__WEBPACK_IMPORTED_MODULE_2__.MediaQueryCondition(sanitizedQuery, invert);
    }
};
/** Always true condition */
ESLMediaQuery.ALL = _conditions_media_query_base__WEBPACK_IMPORTED_MODULE_1__.ALL;
/** Always false condition */
ESLMediaQuery.NOT_ALL = _conditions_media_query_base__WEBPACK_IMPORTED_MODULE_1__.NOT_ALL;
ESLMediaQuery.SHORTCUT_PATTERN = /@([a-z0-9.+-]+)/i;
ESLMediaQuery._preprocessors = [];
__decorate([
    (0,_esl_utils_decorators_memoize__WEBPACK_IMPORTED_MODULE_3__.memoize)()
], ESLMediaQuery, "for", null);
ESLMediaQuery = ESLMediaQuery_1 = __decorate([
    (0,_esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_4__.ExportNs)('MediaQuery')
], ESLMediaQuery);

// Register otb preprocessors
ESLMediaQuery.use(_common_screen_dpr__WEBPACK_IMPORTED_MODULE_5__.ESLScreenDPR);
ESLMediaQuery.use(_common_screen_breakpoint__WEBPACK_IMPORTED_MODULE_6__.ESLScreenBreakpoints);
ESLMediaQuery.use(_common_env_shortcuts__WEBPACK_IMPORTED_MODULE_7__.ESLEnvShortcuts);


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-media-query/core/esl-media-rule-list.js":
/*!**************************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-media-query/core/esl-media-rule-list.js ***!
  \**************************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ESLMediaRuleList": () => (/* binding */ ESLMediaRuleList)
/* harmony export */ });
/* harmony import */ var _esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../../esl-utils/environment/export-ns */ "./node_modules/@exadel/esl/modules/esl-utils/environment/export-ns.js");
/* harmony import */ var _esl_utils_abstract_observable__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../esl-utils/abstract/observable */ "./node_modules/@exadel/esl/modules/esl-utils/abstract/observable.js");
/* harmony import */ var _esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../esl-utils/misc/format */ "./node_modules/@exadel/esl/modules/esl-utils/misc/format.js");
/* harmony import */ var _esl_utils_misc_object__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../esl-utils/misc/object */ "./node_modules/@exadel/esl/modules/esl-utils/misc/object.js");
/* harmony import */ var _esl_media_rule__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./esl-media-rule */ "./node_modules/@exadel/esl/modules/esl-media-query/core/esl-media-rule.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var ESLMediaRuleList_1;





/**
 * ESLMediaRuleList - {@link ESLMediaRule} observable collection
 * @author Yuliya Adamskaya
 *
 * Represents observable object that wraps environment to value mapping
 */
let ESLMediaRuleList = ESLMediaRuleList_1 = class ESLMediaRuleList extends _esl_utils_abstract_observable__WEBPACK_IMPORTED_MODULE_0__.Observable {
    constructor(rules) {
        super();
        this._rules = rules;
        this._default = rules.filter((rule) => rule.default)[0];
        this._onMatchChanged = this._onMatchChanged.bind(this);
    }
    static parse(query, parser = ESLMediaRuleList_1.STRING_PARSER) {
        const rules = [];
        query.split('|').forEach((part) => {
            const lex = part.trim();
            const rule = _esl_media_rule__WEBPACK_IMPORTED_MODULE_1__.ESLMediaRule.parse(lex, parser);
            if (!rule)
                return;
            rule.default ? rules.unshift(rule) : rules.push(rule);
        });
        return new ESLMediaRuleList_1(rules);
    }
    /**
     * Creates `ESLMediaRuleList` from two strings with a value  and conditions tuple
     *
     * @param values - values tuple string (uses '|' as separator)
     * @param mask - media conditions tuple string (uses '|' as separator)
     *
     * @example
     * ```ts
     * ESLMediaRuleList.fromTuple('1|2|3|4|5', '@XS|@SM|@MD|@LG|@XL')
     * ```
     */
    static parseTuple(values, mask) {
        const valueList = values.split('|');
        const conditions = mask.split('|');
        if (valueList.length !== conditions.length)
            throw new Error('Value doesn\'t correspond to mask');
        const rules = conditions.map((query, i) => new _esl_media_rule__WEBPACK_IMPORTED_MODULE_1__.ESLMediaRule(valueList[i], query));
        return new ESLMediaRuleList_1(rules);
    }
    /** Subscribes to the instance active rule change */
    addListener(listener) {
        super.addListener(listener);
        if (this._listeners.size > 1)
            return;
        this._rules.forEach((rule) => rule.addListener(this._onMatchChanged));
    }
    /** Unsubscribes from the instance active rule change */
    removeListener(listener) {
        super.removeListener(listener);
        if (this._listeners.size)
            return;
        this._rules.forEach((rule) => rule.removeListener(this._onMatchChanged));
    }
    /** List of inner {@link ESLMediaRule}s */
    get rules() {
        return this._rules;
    }
    /** Cached active {@link ESLMediaRule} */
    get active() {
        if (!this._active || !this._listeners.size) {
            this._active = this.activeRule;
        }
        return this._active;
    }
    /** Returns last active rule in the list */
    get activeRule() {
        const satisfiedRules = this.rules.filter((rule) => rule.matches);
        return satisfiedRules.length > 0 ? satisfiedRules[satisfiedRules.length - 1] : _esl_media_rule__WEBPACK_IMPORTED_MODULE_1__.ESLMediaRule.empty();
    }
    /** Active rule payload value */
    get activeValue() {
        const value = this.active.payload;
        if ((0,_esl_utils_misc_object__WEBPACK_IMPORTED_MODULE_2__.isPrimitive)(value) || !this.default || (0,_esl_utils_misc_object__WEBPACK_IMPORTED_MODULE_2__.isPrimitive)(this.default.payload))
            return value;
        return Object.assign({}, this.default.payload || {}, value);
    }
    /** {@link ESLMediaRule} that is used as a default rule */
    get default() {
        return this._default;
    }
    /** Handle inner rules state change */
    _onMatchChanged() {
        const rule = this.activeRule;
        if (this._active === rule)
            return;
        this.fire(this._active = rule, this);
    }
};
/**
 * String value parser (used as a default)
 * @returns value string as it is
 */
ESLMediaRuleList.STRING_PARSER = (val) => val;
/**
 * Object value parser. Uses {@link evaluate} to parse value
 * @returns value - parsed JS Object
 */
ESLMediaRuleList.OBJECT_PARSER = (val) => (0,_esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_3__.evaluate)(val);
ESLMediaRuleList = ESLMediaRuleList_1 = __decorate([
    (0,_esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_4__.ExportNs)('MediaRuleList')
], ESLMediaRuleList);



/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-media-query/core/esl-media-rule.js":
/*!*********************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-media-query/core/esl-media-rule.js ***!
  \*********************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ESLMediaRule": () => (/* binding */ ESLMediaRule)
/* harmony export */ });
/* harmony import */ var _esl_media_query__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./esl-media-query */ "./node_modules/@exadel/esl/modules/esl-media-query/core/esl-media-query.js");

/**
 * ESL Media Rule
 * @author Yuliya Adamskaya
 *
 * Helper class to wrap {@link ESLMediaQuery} with the payload value
 * @see ESLMediaQuery
 * @see ESLMediaRuleList
 */
class ESLMediaRule {
    constructor(payload, query = '') {
        this._query = _esl_media_query__WEBPACK_IMPORTED_MODULE_0__.ESLMediaQuery["for"](query);
        this._default = !query;
        this._payload = payload;
    }
    toString() {
        return `${this._query} => ${this._payload}`;
    }
    /** Subscribes on inner {@link ESLMediaQuery} changes */
    addListener(listener) {
        this._query.addListener(listener);
    }
    /** Unsubscribes from inner {@link ESLMediaQuery} changes */
    removeListener(listener) {
        this._query.removeListener(listener);
    }
    /** Check if the inner {@link ESLMediaQuery} is matching current device configuration */
    get matches() {
        return this._query.matches;
    }
    /** @returns wrapped payload value */
    get payload() {
        return this._payload;
    }
    /**
     * Check if the rule was created with an empty query
     * @see ESLMediaRuleList
     */
    get default() {
        return this._default;
    }
    /** Parse the rule string to the {@link ESLMediaRule} instance */
    static parse(lex, parser) {
        const parts = lex.split('=>');
        const query = parts.length === 2 ? parts[0] : '';
        const payload = parts.length === 2 ? parts[1] : parts[0];
        const payloadValue = parser(payload.trim());
        if (typeof payloadValue === 'undefined')
            return undefined;
        return new ESLMediaRule(payloadValue, query.trim());
    }
    /** Shortcut to create always active {@link ESLMediaRule} with passed value */
    static all(payload) {
        return new ESLMediaRule(payload, 'all');
    }
    /** Shortcut to create condition-less {@link ESLMediaRule} */
    static default(payload) {
        return new ESLMediaRule(payload);
    }
    /** Shortcut to create always inactive {@link ESLMediaRule} */
    static empty() {
        return new ESLMediaRule(undefined, 'all');
    }
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-panel-group/core/esl-panel-group.js":
/*!**********************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-panel-group/core/esl-panel-group.js ***!
  \**********************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ESLPanelGroup": () => (/* binding */ ESLPanelGroup)
/* harmony export */ });
/* harmony import */ var _esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_11__ = __webpack_require__(/*! ../../esl-utils/environment/export-ns */ "./node_modules/@exadel/esl/modules/esl-utils/environment/export-ns.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/core/esl-base-element.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/attr.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/json-attr.js");
/* harmony import */ var _esl_utils_async_raf__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ../../esl-utils/async/raf */ "./node_modules/@exadel/esl/modules/esl-utils/async/raf.js");
/* harmony import */ var _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! ../../esl-utils/decorators/bind */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/bind.js");
/* harmony import */ var _esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../esl-utils/misc/format */ "./node_modules/@exadel/esl/modules/esl-utils/misc/format.js");
/* harmony import */ var _esl_utils_decorators_memoize__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../../esl-utils/decorators/memoize */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/memoize.js");
/* harmony import */ var _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! ../../esl-utils/dom/class */ "./node_modules/@exadel/esl/modules/esl-utils/dom/class.js");
/* harmony import */ var _esl_media_query_core__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../../esl-media-query/core */ "./node_modules/@exadel/esl/modules/esl-media-query/core/esl-media-rule-list.js");
/* harmony import */ var _esl_traversing_query_core__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../esl-traversing-query/core */ "./node_modules/@exadel/esl/modules/esl-traversing-query/core/esl-traversing-query.js");
/* harmony import */ var _esl_panel_core__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ../../esl-panel/core */ "./node_modules/@exadel/esl/modules/esl-panel/core/esl-panel.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var ESLPanelGroup_1;










/**
 * ESLPanelGroup component
 * @author Julia Murashko
 *
 * ESLPanelGroup is a custom element that is used as a container for a group of {@link ESLPanel}s
 */
let ESLPanelGroup = ESLPanelGroup_1 = class ESLPanelGroup extends _esl_base_element_core__WEBPACK_IMPORTED_MODULE_0__.ESLBaseElement {
    constructor() {
        super(...arguments);
        /** Height of previous active panel */
        this._previousHeight = 0;
        /** Fallback setTimeout timer */
        this._fallbackTimer = 0;
    }
    static get observedAttributes() {
        return ['mode', 'accordion-group'];
    }
    connectedCallback() {
        super.connectedCallback();
        this.bindEvents();
        this.modeRules.addListener(this._onModeChange);
        this.updateMode();
    }
    disconnectedCallback() {
        super.disconnectedCallback();
        this.modeRules.removeListener(this._onModeChange);
        this.unbindEvents();
    }
    attributeChangedCallback(attrName, oldVal, newVal) {
        if (!this.connected || oldVal === newVal)
            return;
        if (attrName === 'mode') {
            this.modeRules.removeListener(this._onModeChange);
            _esl_utils_decorators_memoize__WEBPACK_IMPORTED_MODULE_1__.memoize.clear(this, 'modeRules');
            this.modeRules.addListener(this._onModeChange);
            this.updateMode();
        }
        if (attrName === 'accordion-group') {
            if (newVal !== 'single' && newVal !== 'multiple') {
                this.accordionGroup = oldVal;
                return;
            }
            this.reset();
        }
    }
    bindEvents() {
        this.addEventListener('esl:before:show', this._onBeforeShow);
        this.addEventListener('esl:show', this._onShow);
        this.addEventListener('esl:before:hide', this._onBeforeHide);
        this.addEventListener('transitionend', this._onTransitionEnd);
    }
    unbindEvents() {
        this.removeEventListener('esl:before:show', this._onBeforeShow);
        this.removeEventListener('esl:show', this._onShow);
        this.removeEventListener('esl:before:hide', this._onBeforeHide);
        this.removeEventListener('transitionend', this._onTransitionEnd);
    }
    /** Updates element state according to current mode */
    updateMode() {
        const prevMode = this.getAttribute('current-mode');
        const currentMode = this.currentMode;
        this.setAttribute('current-mode', currentMode);
        // TODO: @deprecated will be removed with the 4th esl version
        this.setAttribute('view', currentMode);
        this.updateModeCls();
        this.reset();
        if (prevMode !== currentMode) {
            this.$$fire('change:mode', { detail: { prevMode, currentMode } });
        }
    }
    /** Updates mode class marker */
    updateModeCls() {
        const { modeCls, currentMode } = this;
        if (!modeCls)
            return;
        const $target = _esl_traversing_query_core__WEBPACK_IMPORTED_MODULE_2__.TraversingQuery.first(this.modeClsTarget, this);
        if (!$target)
            return;
        ESLPanelGroup_1.supportedModes.forEach((mode) => {
            const className = (0,_esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_3__.format)(modeCls, { mode });
            $target.classList.toggle(className, currentMode === mode);
        });
    }
    /** @returns ESLMediaRuleList instance of the mode mapping */
    get modeRules() {
        return _esl_media_query_core__WEBPACK_IMPORTED_MODULE_4__.ESLMediaRuleList.parse(this.mode);
    }
    /** @returns current mode */
    get currentMode() {
        return this.modeRules.activeValue || '';
    }
    /** @returns panels that are processed by the current panel group */
    get $panels() {
        const els = Array.from(this.querySelectorAll(_esl_panel_core__WEBPACK_IMPORTED_MODULE_5__.ESLPanel.is));
        return els.filter((el) => this.includesPanel(el));
    }
    /** @returns panels that are active */
    get $activePanels() {
        return this.$panels.filter((el) => el.open);
    }
    /** @returns whether the collapse/expand animation should be handheld by the group */
    get shouldCollapse() {
        const noCollapseModes = this.noCollapse.split(',').map((mode) => mode.trim());
        return !noCollapseModes.includes('all') && !noCollapseModes.includes(this.currentMode);
    }
    /** @returns action params config that's used (inherited) by controlled {@link ESLPanel}s */
    get panelConfig() {
        return {
            noCollapse: !this.shouldCollapse || (this.currentMode === 'tabs')
        };
    }
    /** @returns merged panel action params for show/hide requests from the group */
    mergeActionParams(...params) {
        return Object.assign({ initiator: 'group', activator: this }, ...params);
    }
    /** Condition-guard to check if the passed target is a Panel that should be controlled by the Group */
    includesPanel(target) {
        if (!(target instanceof _esl_panel_core__WEBPACK_IMPORTED_MODULE_5__.ESLPanel))
            return false;
        return target.$group === this;
    }
    /** Shows all panels besides excluded ones */
    showAll(excluded = [], params = {}) {
        this.$panels.forEach((el) => !excluded.includes(el) && el.show(this.mergeActionParams(params)));
    }
    /** Hides all active panels besides excluded ones */
    hideAll(excluded = [], params = {}) {
        this.$activePanels.forEach((el) => !excluded.includes(el) && el.hide(this.mergeActionParams(params)));
    }
    /** Toggles all panels by predicate */
    toggleAllBy(shouldOpen, params = {}) {
        this.$panels.forEach((panel) => panel.toggle(shouldOpen(panel), this.mergeActionParams(params)));
    }
    /** Resets to default state applicable to the current mode */
    reset() {
        _esl_panel_core__WEBPACK_IMPORTED_MODULE_5__.ESLPanel.registered.then(() => {
            if (this.currentMode === 'open')
                this.toggleAllBy(() => true, this.transformParams);
            if (this.currentMode === 'tabs' || (this.currentMode === 'accordion' && this.accordionGroup === 'single')) {
                const $activePanel = this.$panels.find((panel) => panel.initiallyOpened);
                this.toggleAllBy((panel) => panel === $activePanel, this.transformParams);
            }
            if (this.currentMode === 'accordion' && this.accordionGroup === 'multiple') {
                this.toggleAllBy((panel) => panel.initiallyOpened, this.transformParams);
            }
        });
    }
    /** Animates the height of the component */
    onAnimate(from, to) {
        const hasCurrent = this.style.height && this.style.height !== 'auto';
        if (hasCurrent) {
            this.style.height = `${to}px`;
            this.fallbackAnimate();
        }
        else {
            // set initial height
            this.style.height = `${from}px`;
            // make sure that browser apply initial height to animate
            (0,_esl_utils_async_raf__WEBPACK_IMPORTED_MODULE_6__.afterNextRender)(() => {
                this.style.height = `${to}px`;
                this.fallbackAnimate();
            });
        }
    }
    /** Pre-processing animation action */
    beforeAnimate() {
        _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_7__.CSSClassUtils.add(this, this.animationClass);
    }
    /** Post-processing animation action */
    afterAnimate() {
        this.style.removeProperty('height');
        _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_7__.CSSClassUtils.remove(this, this.animationClass);
    }
    /** Inits a fallback timer to call post-animate action */
    fallbackAnimate() {
        const time = +this.fallbackDuration;
        if (isNaN(time) || time < 0)
            return;
        if (this._fallbackTimer)
            clearTimeout(this._fallbackTimer);
        this._fallbackTimer = window.setTimeout(() => this.afterAnimate(), time);
    }
    /** Process {@link ESLPanel} pre-show event */
    _onBeforeShow(e) {
        const panel = e.target;
        if (!this.includesPanel(panel))
            return;
        if (this.currentMode === 'accordion' && this.accordionGroup === 'multiple')
            return;
        this.hideAll([panel]);
    }
    /** Process {@link ESLPanel} show event */
    _onShow(e) {
        const panel = e.target;
        if (!this.includesPanel(panel))
            return;
        if (this.currentMode !== 'tabs')
            return;
        this.beforeAnimate();
        if (this.shouldCollapse) {
            this.onAnimate(this._previousHeight, panel.initialHeight);
        }
        else {
            (0,_esl_utils_async_raf__WEBPACK_IMPORTED_MODULE_6__.afterNextRender)(() => this.afterAnimate());
        }
    }
    /** Process {@link ESLPanel} pre-hide event */
    _onBeforeHide(e) {
        // TODO: refactor
        if (this.currentMode === 'open') {
            e.preventDefault();
            return;
        }
        const panel = e.target;
        if (!this.includesPanel(panel))
            return;
        this._previousHeight = this.offsetHeight;
    }
    /** Catches CSS transition end event to start post-animate processing */
    _onTransitionEnd(e) {
        if (!e || e.propertyName === 'height') {
            this.afterAnimate();
        }
    }
    /** Handles mode change */
    _onModeChange() {
        this.updateMode();
    }
};
ESLPanelGroup.is = 'esl-panel-group';
/** List of supported modes */
ESLPanelGroup.supportedModes = ['tabs', 'accordion', 'open'];
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)({ defaultValue: 'accordion' })
], ESLPanelGroup.prototype, "mode", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)({ defaultValue: 'esl-{mode}-view' })
], ESLPanelGroup.prototype, "modeCls", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)({ defaultValue: '' })
], ESLPanelGroup.prototype, "modeClsTarget", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)({ defaultValue: 'animate' })
], ESLPanelGroup.prototype, "animationClass", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)({ defaultValue: 'auto' })
], ESLPanelGroup.prototype, "fallbackDuration", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)()
], ESLPanelGroup.prototype, "noCollapse", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)({ defaultValue: 'single' })
], ESLPanelGroup.prototype, "accordionGroup", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_9__.jsonAttr)({ defaultValue: { noCollapse: true } })
], ESLPanelGroup.prototype, "transformParams", void 0);
__decorate([
    (0,_esl_utils_decorators_memoize__WEBPACK_IMPORTED_MODULE_1__.memoize)()
], ESLPanelGroup.prototype, "modeRules", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_10__.bind
], ESLPanelGroup.prototype, "_onBeforeShow", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_10__.bind
], ESLPanelGroup.prototype, "_onShow", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_10__.bind
], ESLPanelGroup.prototype, "_onBeforeHide", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_10__.bind
], ESLPanelGroup.prototype, "_onTransitionEnd", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_10__.bind
], ESLPanelGroup.prototype, "_onModeChange", null);
ESLPanelGroup = ESLPanelGroup_1 = __decorate([
    (0,_esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_11__.ExportNs)('PanelGroup')
], ESLPanelGroup);



/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-panel/core/esl-panel.js":
/*!**********************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-panel/core/esl-panel.js ***!
  \**********************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ESLPanel": () => (/* binding */ ESLPanel)
/* harmony export */ });
/* harmony import */ var _esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! ../../esl-utils/environment/export-ns */ "./node_modules/@exadel/esl/modules/esl-utils/environment/export-ns.js");
/* harmony import */ var _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../esl-utils/dom/class */ "./node_modules/@exadel/esl/modules/esl-utils/dom/class.js");
/* harmony import */ var _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! ../../esl-utils/decorators/bind */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/bind.js");
/* harmony import */ var _esl_utils_async_raf__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../esl-utils/async/raf */ "./node_modules/@exadel/esl/modules/esl-utils/async/raf.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/attr.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/json-attr.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/bool-attr.js");
/* harmony import */ var _esl_toggleable_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../esl-toggleable/core */ "./node_modules/@exadel/esl/modules/esl-toggleable/core/esl-toggleable.js");
/* harmony import */ var _esl_panel_group_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../../esl-panel-group/core */ "./node_modules/@exadel/esl/modules/esl-panel-group/core/esl-panel-group.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};







/**
 * ESLPanel component
 * @author Julia Murashko
 *
 * ESLPanel is a custom element that is used as a wrapper for content that can be shown or hidden.
 * Can use collapsing/expanding animation (smooth height change).
 * Can be used in conjunction with {@link ESLPanelGroup} to control a group of ESLPopups
 */
let ESLPanel = class ESLPanel extends _esl_toggleable_core__WEBPACK_IMPORTED_MODULE_0__.ESLToggleable {
    constructor() {
        super(...arguments);
        /** Inner height state that updates after show/hide actions but before show/hide events triggered */
        this._initialHeight = 0;
        /** Inner timer to cleanup animation styles */
        this._fallbackTimer = 0;
    }
    /** @returns Previous active panel height at the start of the animation */
    get initialHeight() {
        return this._initialHeight;
    }
    /** @returns Closest panel group or null if not presented */
    get $group() {
        if (this.groupName === 'none' || this.groupName)
            return null;
        return this.closest(_esl_panel_group_core__WEBPACK_IMPORTED_MODULE_1__.ESLPanelGroup.is);
    }
    bindEvents() {
        super.bindEvents();
        this.addEventListener('transitionend', this._onTransitionEnd);
    }
    unbindEvents() {
        super.unbindEvents();
        this.removeEventListener('transitionend', this._onTransitionEnd);
    }
    /** Process show action */
    onShow(params) {
        this._initialHeight = this.scrollHeight;
        super.onShow(params);
        this.beforeAnimate();
        if (params.noCollapse) {
            (0,_esl_utils_async_raf__WEBPACK_IMPORTED_MODULE_2__.afterNextRender)(() => this.afterAnimate());
        }
        else {
            this.onAnimate('show');
        }
    }
    /** Process hide action */
    onHide(params) {
        this._initialHeight = this.scrollHeight;
        super.onHide(params);
        this.beforeAnimate();
        if (params.noCollapse) {
            (0,_esl_utils_async_raf__WEBPACK_IMPORTED_MODULE_2__.afterNextRender)(() => this.afterAnimate());
        }
        else {
            this.onAnimate('hide');
        }
    }
    /** Pre-processing animation action */
    beforeAnimate() {
        this.toggleAttribute('animating', true);
        _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_3__.CSSClassUtils.add(this, this.animateClass);
        this.postAnimateClass && (0,_esl_utils_async_raf__WEBPACK_IMPORTED_MODULE_2__.afterNextRender)(() => _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_3__.CSSClassUtils.add(this, this.postAnimateClass));
    }
    /** Process animation */
    onAnimate(action) {
        // set initial height
        this.style.setProperty('max-height', `${action === 'hide' ? this._initialHeight : 0}px`);
        // make sure that browser apply initial height for animation
        (0,_esl_utils_async_raf__WEBPACK_IMPORTED_MODULE_2__.afterNextRender)(() => {
            this.style.setProperty('max-height', `${action === 'hide' ? 0 : this._initialHeight}px`);
            this.fallbackAnimate();
        });
    }
    /** Post-processing animation action */
    afterAnimate() {
        this.clearAnimation();
        this.$$fire(this.open ? 'after:show' : 'after:hide');
    }
    /** Clear animation properties */
    clearAnimation() {
        this.toggleAttribute('animating', false);
        this.style.removeProperty('max-height');
        _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_3__.CSSClassUtils.remove(this, this.animateClass);
        _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_3__.CSSClassUtils.remove(this, this.postAnimateClass);
    }
    /** Init a fallback timer to call post-animate action */
    fallbackAnimate() {
        const time = +this.fallbackDuration;
        if (isNaN(time) || time < 0)
            return;
        if (this._fallbackTimer)
            clearTimeout(this._fallbackTimer);
        this._fallbackTimer = window.setTimeout(() => this.afterAnimate(), time);
    }
    /** Catching CSS transition end event to start post-animate processing */
    _onTransitionEnd(e) {
        if (!e || e.propertyName === 'max-height') {
            this.afterAnimate();
        }
    }
    /** Merge params that are used by panel group for actions */
    mergeDefaultParams(params) {
        var _a;
        const stackConfig = ((_a = this.$group) === null || _a === void 0 ? void 0 : _a.panelConfig) || {};
        return Object.assign({}, stackConfig, this.defaultParams, params || {});
    }
};
ESLPanel.is = 'esl-panel';
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_4__.attr)({ defaultValue: 'open' })
], ESLPanel.prototype, "activeClass", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_4__.attr)({ defaultValue: 'animate' })
], ESLPanel.prototype, "animateClass", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_4__.attr)({ defaultValue: 'post-animate' })
], ESLPanel.prototype, "postAnimateClass", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_4__.attr)({ defaultValue: '1000' })
], ESLPanel.prototype, "fallbackDuration", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_5__.jsonAttr)({ defaultValue: { force: true, initiator: 'init' } })
], ESLPanel.prototype, "initialParams", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_6__.boolAttr)({ readonly: true })
], ESLPanel.prototype, "animating", void 0);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_7__.bind
], ESLPanel.prototype, "_onTransitionEnd", null);
ESLPanel = __decorate([
    (0,_esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_8__.ExportNs)('Panel')
], ESLPanel);



/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-popup/core/esl-popup-position.js":
/*!*******************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-popup/core/esl-popup-position.js ***!
  \*******************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "isMajorAxisHorizontal": () => (/* binding */ isMajorAxisHorizontal),
/* harmony export */   "calcPopupPosition": () => (/* binding */ calcPopupPosition)
/* harmony export */ });
/* harmony import */ var _esl_utils_dom_rect__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../esl-utils/dom/rect */ "./node_modules/@exadel/esl/modules/esl-utils/dom/rect.js");

/**
 * Checks that the position along the horizontal axis
 * @param position - name of position
 */
function isMajorAxisHorizontal(position) {
    return ['left', 'right'].includes(position);
}
/**
 * Calculates the position of the popup on the minor axis
 * @param cfg - popup position config
 * @param centerPosition - position of the center of the trigger on the minor axis
 * @param dimensionName - the name of dimension (height or width)
 */
function calcPopupPositionByMinorAxis(cfg, centerPosition, dimensionName) {
    return centerPosition - cfg.arrow[dimensionName] / 2 - cfg.marginArrow - calcUsableSizeForArrow(cfg, dimensionName) * cfg.offsetArrowRatio;
}
/**
 * TODO: optimize switch
 * Calculate Rect for given popup position config.
 * @param cfg - popup position config
 * */
function calcPopupBasicRect(cfg) {
    let x = calcPopupPositionByMinorAxis(cfg, cfg.inner.cx, 'width');
    let y = cfg.inner.y - cfg.element.height;
    switch (cfg.position) {
        case 'left':
            x = cfg.inner.x - cfg.element.width;
            y = calcPopupPositionByMinorAxis(cfg, cfg.inner.cy, 'height');
            break;
        case 'right':
            x = cfg.inner.right;
            y = calcPopupPositionByMinorAxis(cfg, cfg.inner.cy, 'height');
            break;
        case 'bottom':
            x = calcPopupPositionByMinorAxis(cfg, cfg.inner.cx, 'width');
            y = cfg.inner.bottom;
            break;
    }
    return new _esl_utils_dom_rect__WEBPACK_IMPORTED_MODULE_0__.Rect(x, y, cfg.element.width, cfg.element.height);
}
/**
 * Get opposite position.
 * @param position - name of position
 * */
function getOppositePosition(position) {
    return ({
        top: 'bottom',
        left: 'right',
        right: 'left',
        bottom: 'top'
    }[position] || position);
}
/**
 * TODO: move the actionsToFit definition outside the function and optimize
 * Update popup and arrow positions to fit on major axis.
 * @param cfg - popup position config
 * @param rect - popup position rect
 * @param arrow - arrow position value
 * */
function fitOnMajorAxis(cfg, rect, arrow) {
    if (cfg.behavior !== 'fit' && cfg.behavior !== 'fit-on-major')
        return cfg.position;
    let isMirrored = false;
    const actionsToFit = {
        'bottom': () => {
            if (cfg.intersectionRatio.bottom || cfg.outer.bottom < rect.bottom) {
                rect.y = cfg.inner.top - cfg.element.height;
                isMirrored = true;
            }
        },
        'left': () => {
            if (cfg.intersectionRatio.left || rect.x < cfg.outer.x) {
                rect.x = cfg.inner.right;
                isMirrored = true;
            }
        },
        'right': () => {
            if (cfg.intersectionRatio.right || cfg.outer.right < rect.right) {
                rect.x = cfg.inner.x - cfg.element.width;
                isMirrored = true;
            }
        },
        'top': () => {
            if (cfg.intersectionRatio.top || rect.y < cfg.outer.y) {
                rect.y = cfg.inner.bottom;
                isMirrored = true;
            }
        }
    };
    actionsToFit[cfg.position]();
    return isMirrored ? getOppositePosition(cfg.position) : cfg.position;
}
/**
 * TODO: rethink fitOnMinorAxisHorizontal and fitOnMinorAxisVertical to simplify code
 * Update popup and arrow positions to fit on minor horizontal axis.
 * @param cfg - popup position config
 * @param rect - popup position rect
 * @param arrow - arrow position value
 * */
function fitOnMinorAxisHorizontal(cfg, rect, arrow) {
    if (cfg.outer.width < cfg.element.width || // cancel fit mode if the popup width is greater than the outer limiter width
        cfg.trigger.x < cfg.outer.x || // or the trigger is outside the outer limiting element
        cfg.trigger.right > cfg.outer.right)
        return;
    let arrowAdjust = 0;
    if (rect.x < cfg.outer.x) {
        arrowAdjust = rect.x - cfg.outer.x;
        rect.x = cfg.outer.x;
    }
    if (rect.right > cfg.outer.right) {
        arrowAdjust = rect.right - cfg.outer.right;
        rect.x -= arrowAdjust;
    }
    arrow.x += arrowAdjust;
}
/**
 * TODO: see Idea warning regarding duplication
 * Update popup and arrow positions to fit by minor vertical axis.
 * @param cfg - popup position config
 * @param rect - popup position rect
 * @param arrow - arrow position value
 * */
function fitOnMinorAxisVertical(cfg, rect, arrow) {
    if (cfg.outer.height < cfg.element.height || // cancel fit mode if the popup height is greater than the outer limiter height
        cfg.trigger.y < cfg.outer.y || // or the trigger is outside the outer limiting element
        cfg.trigger.bottom > cfg.outer.bottom)
        return;
    let arrowAdjust = 0;
    if (rect.y < cfg.outer.y) {
        arrowAdjust = rect.y - cfg.outer.y;
        rect.y = cfg.outer.y;
    }
    if (rect.bottom > cfg.outer.bottom) {
        arrowAdjust = rect.bottom - cfg.outer.bottom;
        rect.y -= arrowAdjust;
    }
    arrow.y += arrowAdjust;
}
/**
 * Update popup and arrow positions to fit on minor axis.
 * @param cfg - popup position config
 * @param rect - popup position rect
 * @param arrow - arrow position value
 * */
function fitOnMinorAxis(cfg, rect, arrow) {
    if (cfg.behavior !== 'fit' && cfg.behavior !== 'fit-on-minor')
        return;
    if (isMajorAxisHorizontal(cfg.position)) {
        fitOnMinorAxisVertical(cfg, rect, arrow);
    }
    else {
        fitOnMinorAxisHorizontal(cfg, rect, arrow);
    }
}
/**
 * Calculate the usable size available for the arrow
 * @param cfg - popup position config
 * @param dimensionName - the name of dimension (height or width)
 */
function calcUsableSizeForArrow(cfg, dimensionName) {
    return cfg.element[dimensionName] - cfg.arrow[dimensionName] - 2 * cfg.marginArrow;
}
/**
 * Calculates the position of the arrow on the minor axis
 * @param cfg - popup position config
 * @param dimensionName - the name of dimension (height or width)
 */
function calcArrowPosition(cfg, dimensionName) {
    return cfg.marginArrow + calcUsableSizeForArrow(cfg, dimensionName) * cfg.offsetArrowRatio;
}
/**
 * Calculate popup and arrow popup positions.
 * @param cfg - popup position config
 * */
function calcPopupPosition(cfg) {
    const popup = calcPopupBasicRect(cfg);
    const arrow = {
        x: calcArrowPosition(cfg, 'width'),
        y: calcArrowPosition(cfg, 'height'),
        position: cfg.position
    };
    const placedAt = fitOnMajorAxis(cfg, popup, arrow);
    fitOnMinorAxis(cfg, popup, arrow);
    return {
        popup,
        placedAt,
        arrow
    };
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-popup/core/esl-popup.js":
/*!**********************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-popup/core/esl-popup.js ***!
  \**********************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ESLPopup": () => (/* binding */ ESLPopup)
/* harmony export */ });
/* harmony import */ var _esl_utils_misc_array__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! ../../esl-utils/misc/array */ "./node_modules/@exadel/esl/modules/esl-utils/misc/array.js");
/* harmony import */ var _esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_16__ = __webpack_require__(/*! ../../esl-utils/environment/export-ns */ "./node_modules/@exadel/esl/modules/esl-utils/environment/export-ns.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/attr.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_11__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/bool-attr.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_12__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/json-attr.js");
/* harmony import */ var _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_15__ = __webpack_require__(/*! ../../esl-utils/decorators/bind */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/bind.js");
/* harmony import */ var _esl_utils_decorators_memoize__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ../../esl-utils/decorators/memoize */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/memoize.js");
/* harmony import */ var _esl_utils_decorators_ready__WEBPACK_IMPORTED_MODULE_14__ = __webpack_require__(/*! ../../esl-utils/decorators/ready */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/ready.js");
/* harmony import */ var _esl_utils_decorators_prop__WEBPACK_IMPORTED_MODULE_13__ = __webpack_require__(/*! ../../esl-utils/decorators/prop */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/prop.js");
/* harmony import */ var _esl_utils_async_raf__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../esl-utils/async/raf */ "./node_modules/@exadel/esl/modules/esl-utils/async/raf.js");
/* harmony import */ var _esl_toggleable_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../../esl-toggleable/core */ "./node_modules/@exadel/esl/modules/esl-toggleable/core/esl-toggleable.js");
/* harmony import */ var _esl_utils_dom_rect__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! ../../esl-utils/dom/rect */ "./node_modules/@exadel/esl/modules/esl-utils/dom/rect.js");
/* harmony import */ var _esl_utils_dom_rtl__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../../esl-utils/dom/rtl */ "./node_modules/@exadel/esl/modules/esl-utils/dom/rtl.js");
/* harmony import */ var _esl_utils_dom_scroll__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ../../esl-utils/dom/scroll */ "./node_modules/@exadel/esl/modules/esl-utils/dom/scroll.js");
/* harmony import */ var _esl_utils_dom_window__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! ../../esl-utils/dom/window */ "./node_modules/@exadel/esl/modules/esl-utils/dom/window.js");
/* harmony import */ var _esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../esl-utils/misc/format */ "./node_modules/@exadel/esl/modules/esl-utils/misc/format.js");
/* harmony import */ var _esl_popup_position__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./esl-popup-position */ "./node_modules/@exadel/esl/modules/esl-popup/core/esl-popup-position.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};















const INTERSECTION_LIMIT_FOR_ADJACENT_AXIS = 0.7;
const DEFAULT_OFFSET_ARROW = 50;
const scrollOptions = { passive: true };
const parsePercent = (value, nanValue = 0) => {
    const rawValue = (0,_esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_0__.parseNumber)(value, nanValue);
    return Math.max(0, Math.min(rawValue !== undefined ? rawValue : nanValue, 100));
};
let ESLPopup = class ESLPopup extends _esl_toggleable_core__WEBPACK_IMPORTED_MODULE_1__.ESLToggleable {
    constructor() {
        super(...arguments);
        this._deferredUpdatePosition = (0,_esl_utils_async_raf__WEBPACK_IMPORTED_MODULE_2__.rafDecorator)(() => this._updatePosition());
        this._intersectionRatio = {};
        this.closeOnEsc = true;
        this.closeOnOutsideAction = true;
    }
    connectedCallback() {
        super.connectedCallback();
        this.$arrow = this.querySelector('span.esl-popup-arrow');
    }
    /** Checks that the position along the horizontal axis */
    get _isMajorAxisHorizontal() {
        return (0,_esl_popup_position__WEBPACK_IMPORTED_MODULE_3__.isMajorAxisHorizontal)(this.position);
    }
    /** Checks that the position along the vertical axis */
    get _isMajorAxisVertical() {
        return !(0,_esl_popup_position__WEBPACK_IMPORTED_MODULE_3__.isMajorAxisHorizontal)(this.position);
    }
    /** Get offsets arrow ratio */
    get _offsetArrowRatio() {
        const ratio = parsePercent(this.offsetArrow, DEFAULT_OFFSET_ARROW) / 100;
        return _esl_utils_dom_rtl__WEBPACK_IMPORTED_MODULE_4__.RTLUtils.isRtl(this) ? 1 - ratio : ratio;
    }
    /**
     * Actions to execute on show popup.
     * Inner state and 'open' attribute are not affected and updated before `onShow` execution.
     * Adds CSS classes, update a11y and fire esl:refresh event by default.
     */
    onShow(params) {
        super.onShow(params);
        if (params.position) {
            this.position = params.position;
        }
        if (params.behavior) {
            this.behavior = params.behavior;
        }
        if (params.disableActivatorObservation) {
            this.disableActivatorObservation = params.disableActivatorObservation;
        }
        if (params.marginArrow) {
            this.marginArrow = params.marginArrow;
        }
        if (params.offsetArrow) {
            this.offsetArrow = params.offsetArrow;
        }
        this._offsetTrigger = params.offsetTrigger || 0;
        this._offsetWindow = params.offsetWindow || 0;
        this.style.visibility = 'hidden'; // eliminates the blinking of the popup at the previous position
        (0,_esl_utils_async_raf__WEBPACK_IMPORTED_MODULE_2__.afterNextRender)(() => this.afterOnShow()); // running as a separate task solves the problem with incorrect positioning on the first showing
    }
    /**
     * Actions to execute on hide popup.
     * Inner state and 'open' attribute are not affected and updated before `onShow` execution.
     * Removes CSS classes and updates a11y by default.
     */
    onHide(params) {
        this.beforeOnHide();
        super.onHide(params);
        this._stopUpdateLoop();
        this.activator && this._removeActivatorObserver(this.activator);
        // clear all memoize data
        _esl_utils_decorators_memoize__WEBPACK_IMPORTED_MODULE_5__.memoize.clear(this, '_isMajorAxisHorizontal');
        _esl_utils_decorators_memoize__WEBPACK_IMPORTED_MODULE_5__.memoize.clear(this, '_isMajorAxisVertical');
        _esl_utils_decorators_memoize__WEBPACK_IMPORTED_MODULE_5__.memoize.clear(this, '_offsetArrowRatio');
    }
    /**
     * Actions to execute after showing of popup.
     */
    afterOnShow() {
        this._updatePosition();
        this.style.visibility = 'visible';
        this.activator && this._addActivatorObserver(this.activator);
        this._startUpdateLoop();
    }
    /**
     * Actions to execute before hiding of popup.
     */
    beforeOnHide() { }
    /**
     * Checks activator intersection for adjacent axis.
     * Hides the popup if the intersection ratio exceeds the limit.
     */
    _checkIntersectionForAdjacentAxis(isAdjacentAxis, intersectionRatio) {
        if (isAdjacentAxis && intersectionRatio < INTERSECTION_LIMIT_FOR_ADJACENT_AXIS) {
            this.hide();
        }
    }
    /** Actions to execute on activator intersection event. */
    onActivatorIntersection(entries, observer) {
        const entry = entries[0];
        this._intersectionRatio = {};
        if (!entry.isIntersecting) {
            this.hide();
            return;
        }
        if (entry.intersectionRect.y !== entry.boundingClientRect.y) {
            this._intersectionRatio.top = entry.intersectionRect.height / entry.boundingClientRect.height;
            this._checkIntersectionForAdjacentAxis(this._isMajorAxisHorizontal, this._intersectionRatio.top);
        }
        if (entry.intersectionRect.bottom !== entry.boundingClientRect.bottom) {
            this._intersectionRatio.bottom = entry.intersectionRect.height / entry.boundingClientRect.height;
            this._checkIntersectionForAdjacentAxis(this._isMajorAxisHorizontal, this._intersectionRatio.bottom);
        }
        if (entry.intersectionRect.x !== entry.boundingClientRect.x) {
            this._intersectionRatio.left = entry.intersectionRect.width / entry.boundingClientRect.width;
            this._checkIntersectionForAdjacentAxis(this._isMajorAxisVertical, this._intersectionRatio.left);
        }
        if (entry.intersectionRect.right !== entry.boundingClientRect.right) {
            this._intersectionRatio.right = entry.intersectionRect.width / entry.boundingClientRect.width;
            this._checkIntersectionForAdjacentAxis(this._isMajorAxisVertical, this._intersectionRatio.right);
        }
    }
    /** Actions to execute on activator scroll event. */
    onActivatorScroll(e) {
        if (this._updateLoopID)
            return;
        this._updatePosition();
    }
    /** Creates listeners and observers to observe activator after showing popup */
    _addActivatorObserver(target) {
        const scrollParents = (0,_esl_utils_dom_scroll__WEBPACK_IMPORTED_MODULE_6__.getListScrollParents)(target);
        this._activatorObserver = {
            unsubscribers: scrollParents.map(($root) => {
                $root.addEventListener('scroll', this.onActivatorScroll, scrollOptions);
                return () => {
                    $root && $root.removeEventListener('scroll', this.onActivatorScroll, scrollOptions);
                };
            })
        };
        if (!this.disableActivatorObservation) {
            const options = {
                rootMargin: '0px',
                threshold: (0,_esl_utils_misc_array__WEBPACK_IMPORTED_MODULE_7__.range)(9, (x) => x / 8)
            };
            const observer = new IntersectionObserver(this.onActivatorIntersection, options);
            observer.observe(target);
            this._activatorObserver.observer = observer;
        }
        window.addEventListener('resize', this._deferredUpdatePosition);
        window.addEventListener('scroll', this.onActivatorScroll, scrollOptions);
        document.body.addEventListener('transitionstart', this._startUpdateLoop);
    }
    /** Removes activator listeners and observers after hiding popup */
    _removeActivatorObserver(target) {
        var _a, _b;
        window.removeEventListener('resize', this._deferredUpdatePosition);
        window.removeEventListener('scroll', this.onActivatorScroll, scrollOptions);
        (_a = this._activatorObserver.observer) === null || _a === void 0 ? void 0 : _a.disconnect();
        this._activatorObserver.observer = undefined;
        (_b = this._activatorObserver.unsubscribers) === null || _b === void 0 ? void 0 : _b.forEach((cb) => cb());
        this._activatorObserver.unsubscribers = [];
        document.body.removeEventListener('transitionstart', this._startUpdateLoop);
    }
    /**
     * Starts loop for update position of popup.
     * The loop ends when the position and size of the activator have not changed
     * for the last 2 frames of the animation.
     */
    _startUpdateLoop() {
        if (this._updateLoopID)
            return;
        let same = 0;
        let lastRect = new _esl_utils_dom_rect__WEBPACK_IMPORTED_MODULE_8__.Rect();
        const updateLoop = () => {
            if (!this.activator) {
                this._stopUpdateLoop();
                return;
            }
            const newRect = _esl_utils_dom_rect__WEBPACK_IMPORTED_MODULE_8__.Rect.from(this.activator.getBoundingClientRect());
            if (!_esl_utils_dom_rect__WEBPACK_IMPORTED_MODULE_8__.Rect.isEqual(lastRect, newRect)) {
                same = 0;
                lastRect = newRect;
            }
            if (same++ > 2) {
                this._stopUpdateLoop();
                return;
            }
            this._updatePosition();
            this._updateLoopID = requestAnimationFrame(updateLoop);
        };
        this._updateLoopID = requestAnimationFrame(updateLoop);
    }
    /**
     * Stops loop for update position of popup.
     * Also cancels the animation frame request.
     */
    _stopUpdateLoop() {
        if (!this._updateLoopID)
            return;
        cancelAnimationFrame(this._updateLoopID);
        this._updateLoopID = 0;
    }
    /** Updates position of popup and its arrow */
    _updatePosition() {
        if (!this.activator)
            return;
        const triggerRect = this.activator.getBoundingClientRect();
        const popupRect = this.getBoundingClientRect();
        const arrowRect = this.$arrow ? this.$arrow.getBoundingClientRect() : new _esl_utils_dom_rect__WEBPACK_IMPORTED_MODULE_8__.Rect();
        const trigger = new _esl_utils_dom_rect__WEBPACK_IMPORTED_MODULE_8__.Rect(triggerRect.left, triggerRect.top + window.pageYOffset, triggerRect.width, triggerRect.height);
        const innerMargin = this._offsetTrigger + arrowRect.width / 2;
        const config = {
            position: this.position,
            behavior: this.behavior,
            marginArrow: +this.marginArrow,
            offsetArrowRatio: this._offsetArrowRatio,
            intersectionRatio: this._intersectionRatio,
            arrow: arrowRect,
            element: popupRect,
            trigger,
            inner: _esl_utils_dom_rect__WEBPACK_IMPORTED_MODULE_8__.Rect.from(trigger).grow(innerMargin),
            outer: (0,_esl_utils_dom_window__WEBPACK_IMPORTED_MODULE_9__.getWindowRect)().shrink(this._offsetWindow)
        };
        const { placedAt, popup, arrow } = (0,_esl_popup_position__WEBPACK_IMPORTED_MODULE_3__.calcPopupPosition)(config);
        this.setAttribute('placed-at', placedAt);
        // set popup position
        this.style.left = `${popup.x}px`;
        this.style.top = `${popup.y}px`;
        // set arrow position
        if (this.$arrow) {
            this.$arrow.style.left = this._isMajorAxisVertical ? `${arrow.x}px` : '';
            this.$arrow.style.top = this._isMajorAxisHorizontal ? `${arrow.y}px` : '';
        }
    }
};
ESLPopup.is = 'esl-popup';
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_10__.attr)({ defaultValue: 'top' })
], ESLPopup.prototype, "position", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_10__.attr)({ defaultValue: 'fit' })
], ESLPopup.prototype, "behavior", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_11__.boolAttr)()
], ESLPopup.prototype, "disableActivatorObservation", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_10__.attr)({ defaultValue: '5' })
], ESLPopup.prototype, "marginArrow", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_10__.attr)({ defaultValue: `${DEFAULT_OFFSET_ARROW}` })
], ESLPopup.prototype, "offsetArrow", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_12__.jsonAttr)({ defaultValue: {
            offsetTrigger: 3,
            offsetWindow: 15
        } })
], ESLPopup.prototype, "defaultParams", void 0);
__decorate([
    (0,_esl_utils_decorators_prop__WEBPACK_IMPORTED_MODULE_13__.prop)()
], ESLPopup.prototype, "closeOnEsc", void 0);
__decorate([
    (0,_esl_utils_decorators_prop__WEBPACK_IMPORTED_MODULE_13__.prop)()
], ESLPopup.prototype, "closeOnOutsideAction", void 0);
__decorate([
    _esl_utils_decorators_ready__WEBPACK_IMPORTED_MODULE_14__.ready
], ESLPopup.prototype, "connectedCallback", null);
__decorate([
    (0,_esl_utils_decorators_memoize__WEBPACK_IMPORTED_MODULE_5__.memoize)()
], ESLPopup.prototype, "_isMajorAxisHorizontal", null);
__decorate([
    (0,_esl_utils_decorators_memoize__WEBPACK_IMPORTED_MODULE_5__.memoize)()
], ESLPopup.prototype, "_isMajorAxisVertical", null);
__decorate([
    (0,_esl_utils_decorators_memoize__WEBPACK_IMPORTED_MODULE_5__.memoize)()
], ESLPopup.prototype, "_offsetArrowRatio", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_15__.bind
], ESLPopup.prototype, "onActivatorIntersection", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_15__.bind
], ESLPopup.prototype, "onActivatorScroll", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_15__.bind
], ESLPopup.prototype, "_startUpdateLoop", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_15__.bind
], ESLPopup.prototype, "_stopUpdateLoop", null);
ESLPopup = __decorate([
    (0,_esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_16__.ExportNs)('Popup')
], ESLPopup);



/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-scrollbar/core/esl-scrollbar.js":
/*!******************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-scrollbar/core/esl-scrollbar.js ***!
  \******************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ESLScrollbar": () => (/* binding */ ESLScrollbar)
/* harmony export */ });
/* harmony import */ var _esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! ../../esl-utils/environment/export-ns */ "./node_modules/@exadel/esl/modules/esl-utils/environment/export-ns.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/core/esl-base-element.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/bool-attr.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/attr.js");
/* harmony import */ var _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! ../../esl-utils/decorators/bind */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/bind.js");
/* harmony import */ var _esl_utils_decorators_ready__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! ../../esl-utils/decorators/ready */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/ready.js");
/* harmony import */ var _esl_utils_async_raf__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../../esl-utils/async/raf */ "./node_modules/@exadel/esl/modules/esl-utils/async/raf.js");
/* harmony import */ var _esl_utils_dom_events__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../../esl-utils/dom/events */ "./node_modules/@exadel/esl/modules/esl-utils/dom/events.js");
/* harmony import */ var _esl_utils_dom_traversing__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ../../esl-utils/dom/traversing */ "./node_modules/@exadel/esl/modules/esl-utils/dom/traversing.js");
/* harmony import */ var _esl_traversing_query_core__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../esl-traversing-query/core */ "./node_modules/@exadel/esl/modules/esl-traversing-query/core/esl-traversing-query.js");
/* harmony import */ var _esl_utils_dom_rtl__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../esl-utils/dom/rtl */ "./node_modules/@exadel/esl/modules/esl-utils/dom/rtl.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};









/**
 * ESLScrollbar is a reusable web component that replaces the browser's default scrollbar with
 * a custom scrollbar implementation.
 *
 * @author Yuliya Adamskaya
 */
let ESLScrollbar = class ESLScrollbar extends _esl_base_element_core__WEBPACK_IMPORTED_MODULE_0__.ESLBaseElement {
    constructor() {
        super(...arguments);
        this._deferredDrag = (0,_esl_utils_async_raf__WEBPACK_IMPORTED_MODULE_1__.rafDecorator)((e) => this._onPointerDrag(e));
        this._deferredRefresh = (0,_esl_utils_async_raf__WEBPACK_IMPORTED_MODULE_1__.rafDecorator)(() => this.refresh());
        this._scrollTimer = 0;
        this._resizeObserver = new ResizeObserver(this._deferredRefresh);
        this._mutationObserver = new MutationObserver((rec) => this.updateContentObserve(rec));
    }
    static get observedAttributes() {
        return ['target', 'horizontal'];
    }
    connectedCallback() {
        super.connectedCallback();
        this.findTarget();
        this.render();
        this.bindEvents();
    }
    disconnectedCallback() {
        this.unbindEvents();
        this._scrollTimer && window.clearTimeout(this._scrollTimer);
    }
    attributeChangedCallback(attrName, oldVal, newVal) {
        if (!this.connected && oldVal === newVal)
            return;
        if (attrName === 'target')
            this.findTarget();
        if (attrName === 'horizontal')
            this.refresh();
    }
    findTarget() {
        this.$target = this.target ?
            _esl_traversing_query_core__WEBPACK_IMPORTED_MODULE_2__.TraversingQuery.first(this.target, this) :
            null;
    }
    /** Target element to observe and scroll */
    get $target() {
        return this._$target || null;
    }
    set $target(content) {
        this.unbindTargetEvents();
        this._$target = content;
        this.bindTargetEvents();
        this._deferredRefresh();
    }
    render() {
        this.innerHTML = '';
        this.$scrollbarTrack = document.createElement('div');
        this.$scrollbarTrack.className = this.trackClass;
        this.$scrollbarThumb = document.createElement('div');
        this.$scrollbarThumb.className = this.thumbClass;
        this.$scrollbarTrack.appendChild(this.$scrollbarThumb);
        this.appendChild(this.$scrollbarTrack);
    }
    bindEvents() {
        window.MouseEvent && this.addEventListener('mousedown', this._onPointerDown);
        window.TouchEvent && this.addEventListener('touchstart', this._onPointerDown);
        window.addEventListener('esl:refresh', this._onRefresh);
    }
    bindTargetEvents() {
        if (!this.$target)
            return;
        if (document.documentElement === this.$target) {
            window.addEventListener('resize', this._onRefresh, { passive: true });
            window.addEventListener('scroll', this._onRefresh, { passive: true });
        }
        else {
            this._resizeObserver.observe(this.$target);
            this._mutationObserver.observe(this.$target, { childList: true });
            Array.from(this.$target.children).forEach((el) => this._resizeObserver.observe(el));
            this.$target.addEventListener('scroll', this._onRefresh, { passive: true });
        }
    }
    updateContentObserve(recs = []) {
        if (!this.$target)
            return;
        const contentChanges = recs.filter((rec) => rec.type === 'childList');
        contentChanges.forEach((rec) => {
            Array.from(rec.addedNodes)
                .filter((el) => el instanceof Element)
                .forEach((el) => this._resizeObserver.observe(el));
            Array.from(rec.removedNodes)
                .filter((el) => el instanceof Element)
                .forEach((el) => this._resizeObserver.unobserve(el));
        });
        if (contentChanges.length)
            this._deferredRefresh();
    }
    unbindEvents() {
        window.MouseEvent && this.removeEventListener('mousedown', this._onPointerDown);
        window.TouchEvent && this.removeEventListener('touchstart', this._onPointerDown);
        this.unbindTargetEvents();
        window.removeEventListener('esl:refresh', this._onRefresh);
    }
    unbindTargetEvents() {
        if (!this.$target)
            return;
        if (document.documentElement === this.$target) {
            window.removeEventListener('resize', this._onRefresh);
            window.removeEventListener('scroll', this._onRefresh);
        }
        else {
            this._resizeObserver.disconnect();
            this._mutationObserver.disconnect();
            this.$target.removeEventListener('scroll', this._onRefresh);
        }
    }
    /** @readonly Scrollable distance size value (px) */
    get scrollableSize() {
        if (!this.$target)
            return 0;
        return this.horizontal ?
            this.$target.scrollWidth - this.$target.clientWidth :
            this.$target.scrollHeight - this.$target.clientHeight;
    }
    /** @readonly Track size value (px) */
    get trackOffset() {
        return this.horizontal ? this.$scrollbarTrack.offsetWidth : this.$scrollbarTrack.offsetHeight;
    }
    /** @readonly Thumb size value (px) */
    get thumbOffset() {
        return this.horizontal ? this.$scrollbarThumb.offsetWidth : this.$scrollbarThumb.offsetHeight;
    }
    /** @readonly Relative thumb size value (between 0.0 and 1.0) */
    get thumbSize() {
        // behave as native scroll
        if (!this.$target || !this.$target.scrollWidth || !this.$target.scrollHeight)
            return 1;
        const areaSize = this.horizontal ? this.$target.clientWidth : this.$target.clientHeight;
        const scrollSize = this.horizontal ? this.$target.scrollWidth : this.$target.scrollHeight;
        return Math.min((areaSize + 1) / scrollSize, 1);
    }
    /** Relative position value (between 0.0 and 1.0) */
    get position() {
        if (!this.$target)
            return 0;
        const scrollOffset = this.horizontal ? _esl_utils_dom_rtl__WEBPACK_IMPORTED_MODULE_3__.RTLUtils.normalizeScrollLeft(this.$target) : this.$target.scrollTop;
        return this.scrollableSize ? (scrollOffset / this.scrollableSize) : 0;
    }
    set position(position) {
        this.scrollTargetTo(this.scrollableSize * this.normalizePosition(position));
        this.update();
    }
    /** Normalizes position value (between 0.0 and 1.0) */
    normalizePosition(position) {
        const relativePosition = Math.min(1, Math.max(0, position));
        if (this.$target && !_esl_utils_dom_rtl__WEBPACK_IMPORTED_MODULE_3__.RTLUtils.isRtl(this.$target))
            return relativePosition;
        return _esl_utils_dom_rtl__WEBPACK_IMPORTED_MODULE_3__.RTLUtils.scrollType === 'negative' ? (relativePosition - 1) : (1 - relativePosition);
    }
    /** Scrolls target element to passed position */
    scrollTargetTo(pos) {
        if (!this.$target)
            return;
        this.$target.scrollTo({
            [this.horizontal ? 'left' : 'top']: pos,
            behavior: this.dragging ? 'auto' : 'smooth'
        });
    }
    /** Updates thumb size and position */
    update() {
        this.$$fire('change:scroll', { bubbles: false });
        if (!this.$scrollbarThumb || !this.$scrollbarTrack)
            return;
        const thumbSize = this.trackOffset * this.thumbSize;
        const thumbPosition = (this.trackOffset - thumbSize) * this.position;
        const style = {
            [this.horizontal ? 'left' : 'top']: `${thumbPosition}px`,
            [this.horizontal ? 'width' : 'height']: `${thumbSize}px`
        };
        Object.assign(this.$scrollbarThumb.style, style);
    }
    /** Updates auxiliary markers */
    updateMarkers() {
        const { position, thumbSize } = this;
        this.toggleAttribute('at-start', thumbSize < 1 && position <= 0);
        this.toggleAttribute('at-end', thumbSize < 1 && position >= 1);
        this.toggleAttribute('inactive', thumbSize >= 1);
    }
    /** Refreshes scroll state and position */
    refresh() {
        this.update();
        this.updateMarkers();
    }
    /** Returns position from MouseEvent coordinates (not normalized) */
    toPosition(event) {
        const { horizontal, thumbOffset, trackOffset } = this;
        const point = _esl_utils_dom_events__WEBPACK_IMPORTED_MODULE_4__.EventUtils.normalizeCoordinates(event, this.$scrollbarTrack);
        const pointPosition = horizontal ? point.x : point.y;
        const freeTrackArea = trackOffset - thumbOffset; // size of free track px
        const clickPositionNoOffset = pointPosition - thumbOffset / 2;
        return clickPositionNoOffset / freeTrackArea;
    }
    // Event listeners
    /** Handles `mousedown` / `touchstart` event to manage thumb drag start and scroll clicks */
    _onPointerDown(event) {
        this._initialPosition = this.position;
        this._pointerPosition = this.toPosition(event);
        const point = _esl_utils_dom_events__WEBPACK_IMPORTED_MODULE_4__.EventUtils.normalizeTouchPoint(event);
        this._initialMousePosition = this.horizontal ? point.x : point.y;
        if (event.target === this.$scrollbarThumb) {
            this._onThumbPointerDown(event); // Drag start handler
        }
        else {
            this._onPointerDownTick(true); // Continuous scroll and click handler
        }
        // Subscribe inverse handlers
        _esl_utils_dom_events__WEBPACK_IMPORTED_MODULE_4__.EventUtils.isMouseEvent(event) && window.addEventListener('mouseup', this._onPointerUp);
        _esl_utils_dom_events__WEBPACK_IMPORTED_MODULE_4__.EventUtils.isTouchEvent(event) && window.addEventListener('touchend', this._onPointerUp, { passive: false });
        // Prevents default text selection, etc.
        event.preventDefault();
    }
    /** Handles a scroll click / continuous scroll*/
    _onPointerDownTick(first) {
        this._scrollTimer && window.clearTimeout(this._scrollTimer);
        const position = this.position;
        const allowedOffset = (first ? 1 : 1.5) * this.thumbSize;
        this.position = Math.min(position + allowedOffset, Math.max(position - allowedOffset, this._pointerPosition));
        if (this.position === this._pointerPosition || this.noContinuousScroll)
            return;
        this._scrollTimer = window.setTimeout(this._onPointerDownTick, 400);
    }
    /** Handles thumb drag start */
    _onThumbPointerDown(event) {
        var _a;
        this.toggleAttribute('dragging', true);
        (_a = this.$target) === null || _a === void 0 ? void 0 : _a.style.setProperty('scroll-behavior', 'auto');
        // Attach drag listeners
        window.addEventListener('click', this._onBodyClick, { capture: true });
        _esl_utils_dom_events__WEBPACK_IMPORTED_MODULE_4__.EventUtils.isMouseEvent(event) && window.addEventListener('mousemove', this._onPointerMove);
        _esl_utils_dom_events__WEBPACK_IMPORTED_MODULE_4__.EventUtils.isTouchEvent(event) && window.addEventListener('touchmove', this._onPointerMove, { passive: false });
    }
    /** Sets position on drag */
    _onPointerDrag(event) {
        const point = _esl_utils_dom_events__WEBPACK_IMPORTED_MODULE_4__.EventUtils.normalizeTouchPoint(event);
        const mousePosition = this.horizontal ? point.x : point.y;
        const positionChange = mousePosition - this._initialMousePosition;
        const scrollableAreaHeight = this.trackOffset - this.thumbOffset;
        const absChange = scrollableAreaHeight ? (positionChange / scrollableAreaHeight) : 0;
        this.position = this._initialPosition + absChange;
        this.updateMarkers();
    }
    /** `mousemove` document handler for thumb drag event. Active only if drag action is active */
    _onPointerMove(event) {
        if (!this.dragging)
            return;
        // Request position update
        this._deferredDrag(event);
        // Prevents default text selection, etc.
        event.preventDefault();
        event.stopPropagation();
    }
    /** `mouseup` short-time document handler for drag end action */
    _onPointerUp(event) {
        var _a;
        this._scrollTimer && window.clearTimeout(this._scrollTimer);
        this.toggleAttribute('dragging', false);
        (_a = this.$target) === null || _a === void 0 ? void 0 : _a.style.removeProperty('scroll-behavior');
        // Unbind drag listeners
        if (_esl_utils_dom_events__WEBPACK_IMPORTED_MODULE_4__.EventUtils.isMouseEvent(event)) {
            window.removeEventListener('mousemove', this._onPointerMove);
            window.removeEventListener('mouseup', this._onPointerUp);
        }
        if (_esl_utils_dom_events__WEBPACK_IMPORTED_MODULE_4__.EventUtils.isTouchEvent(event)) {
            window.removeEventListener('touchmove', this._onPointerMove);
            window.removeEventListener('touchend', this._onPointerUp);
        }
    }
    /** Body `click` short-time handler to prevent clicks event on thumb drag. Handles capture phase */
    _onBodyClick(event) {
        event.stopImmediatePropagation();
        window.removeEventListener('click', this._onBodyClick, { capture: true });
    }
    /**
     * Handler for refresh events to update the scroll.
     * @param event - instance of 'resize' or 'scroll' or 'esl:refresh' event.
     */
    _onRefresh(event) {
        const target = event.target;
        if (event.type === 'scroll' && this.dragging)
            return;
        if (event.type === 'esl:refresh' && !(0,_esl_utils_dom_traversing__WEBPACK_IMPORTED_MODULE_5__.isRelativeNode)(target.parentNode, this.$target))
            return;
        this._deferredRefresh();
    }
};
ESLScrollbar.is = 'esl-scrollbar';
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_6__.boolAttr)()
], ESLScrollbar.prototype, "horizontal", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_6__.boolAttr)()
], ESLScrollbar.prototype, "noContinuousScroll", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_7__.attr)({ defaultValue: '::parent' })
], ESLScrollbar.prototype, "target", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_7__.attr)({ defaultValue: 'scrollbar-thumb' })
], ESLScrollbar.prototype, "thumbClass", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_7__.attr)({ defaultValue: 'scrollbar-track' })
], ESLScrollbar.prototype, "trackClass", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_6__.boolAttr)({ readonly: true })
], ESLScrollbar.prototype, "dragging", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_6__.boolAttr)({ readonly: true })
], ESLScrollbar.prototype, "inactive", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_6__.boolAttr)({ readonly: true })
], ESLScrollbar.prototype, "atStart", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_6__.boolAttr)({ readonly: true })
], ESLScrollbar.prototype, "atEnd", void 0);
__decorate([
    _esl_utils_decorators_ready__WEBPACK_IMPORTED_MODULE_8__.ready
], ESLScrollbar.prototype, "connectedCallback", null);
__decorate([
    _esl_utils_decorators_ready__WEBPACK_IMPORTED_MODULE_8__.ready
], ESLScrollbar.prototype, "disconnectedCallback", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_9__.bind
], ESLScrollbar.prototype, "_onPointerDown", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_9__.bind
], ESLScrollbar.prototype, "_onPointerDownTick", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_9__.bind
], ESLScrollbar.prototype, "_onThumbPointerDown", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_9__.bind
], ESLScrollbar.prototype, "_onPointerMove", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_9__.bind
], ESLScrollbar.prototype, "_onPointerUp", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_9__.bind
], ESLScrollbar.prototype, "_onBodyClick", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_9__.bind
], ESLScrollbar.prototype, "_onRefresh", null);
ESLScrollbar = __decorate([
    (0,_esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_10__.ExportNs)('Scrollbar')
], ESLScrollbar);



/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-tab/core/esl-tab.js":
/*!******************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-tab/core/esl-tab.js ***!
  \******************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ESLTab": () => (/* binding */ ESLTab)
/* harmony export */ });
/* harmony import */ var _esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../esl-utils/environment/export-ns */ "./node_modules/@exadel/esl/modules/esl-utils/environment/export-ns.js");
/* harmony import */ var _esl_trigger_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../esl-trigger/core */ "./node_modules/@exadel/esl/modules/esl-trigger/core/esl-trigger.js");
/* harmony import */ var _esl_base_element_decorators_attr__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../../esl-base-element/decorators/attr */ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/attr.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};



/**
 * ESlTab component
 * @author Julia Murashko
 *
 * Tab trigger item, usually used in conjunction with a {@link ESLTabs}.
 * Can control any {@link ESLToggleable} instance but is usually used in conjunction with {@link ESLPanel}
 */
let ESLTab = class ESLTab extends _esl_trigger_core__WEBPACK_IMPORTED_MODULE_0__.ESLTrigger {
    initA11y() {
        const target = this.$a11yTarget;
        if (!target)
            return;
        if (target.hasAttribute('role'))
            return;
        target.setAttribute('role', 'tab');
    }
    updateA11y() {
        const target = this.$a11yTarget;
        if (!target)
            return;
        target.setAttribute('aria-selected', String(this.active));
        target.setAttribute('tabindex', this.active ? '0' : '-1');
        if (this.$target && this.$target.id) {
            this.setAttribute('aria-controls', this.$target.id);
        }
    }
};
ESLTab.is = 'esl-tab';
__decorate([
    (0,_esl_base_element_decorators_attr__WEBPACK_IMPORTED_MODULE_1__.attr)({ defaultValue: 'show' })
], ESLTab.prototype, "mode", void 0);
__decorate([
    (0,_esl_base_element_decorators_attr__WEBPACK_IMPORTED_MODULE_1__.attr)({ defaultValue: 'active' })
], ESLTab.prototype, "activeClass", void 0);
ESLTab = __decorate([
    (0,_esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_2__.ExportNs)('Tab')
], ESLTab);



/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-tab/core/esl-tabs.js":
/*!*******************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-tab/core/esl-tabs.js ***!
  \*******************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ESLTabs": () => (/* binding */ ESLTabs)
/* harmony export */ });
/* harmony import */ var _esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! ../../esl-utils/environment/export-ns */ "./node_modules/@exadel/esl/modules/esl-utils/environment/export-ns.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/core/esl-base-element.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/attr.js");
/* harmony import */ var _esl_utils_async_raf__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../esl-utils/async/raf */ "./node_modules/@exadel/esl/modules/esl-utils/async/raf.js");
/* harmony import */ var _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! ../../esl-utils/decorators/bind */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/bind.js");
/* harmony import */ var _esl_utils_decorators_memoize__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../../esl-utils/decorators/memoize */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/memoize.js");
/* harmony import */ var _esl_utils_dom_rtl__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ../../esl-utils/dom/rtl */ "./node_modules/@exadel/esl/modules/esl-utils/dom/rtl.js");
/* harmony import */ var _esl_utils_async_debounce__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../../esl-utils/async/debounce */ "./node_modules/@exadel/esl/modules/esl-utils/async/debounce.js");
/* harmony import */ var _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! ../../esl-utils/dom/class */ "./node_modules/@exadel/esl/modules/esl-utils/dom/class.js");
/* harmony import */ var _esl_media_query_core_esl_media_rule_list__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../esl-media-query/core/esl-media-rule-list */ "./node_modules/@exadel/esl/modules/esl-media-query/core/esl-media-rule-list.js");
/* harmony import */ var _esl_tab__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ./esl-tab */ "./node_modules/@exadel/esl/modules/esl-tab/core/esl-tab.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var ESLTabs_1;










/**
 * ESlTabs component
 * @author Julia Murashko
 *
 * Tabs container component for Tabs trigger group.
 * Uses {@link ESLTab} as an item.
 * Each individual {@link ESLTab} can control {@link ESLToggleable} or, usually, {@link ESLPanel}
 */
let ESLTabs = ESLTabs_1 = class ESLTabs extends _esl_base_element_core__WEBPACK_IMPORTED_MODULE_0__.ESLBaseElement {
    constructor() {
        super(...arguments);
        this._deferredUpdateArrows = (0,_esl_utils_async_debounce__WEBPACK_IMPORTED_MODULE_1__.debounce)(this.updateArrows, 100, this);
        this._deferredFitToViewport = (0,_esl_utils_async_debounce__WEBPACK_IMPORTED_MODULE_1__.debounce)(this.fitToViewport, 100, this);
        // TODO: is the raf decorator needed?
        this._onResize = (0,_esl_utils_async_raf__WEBPACK_IMPORTED_MODULE_2__.rafDecorator)(() => this._deferredFitToViewport(this.$current, 'auto'));
    }
    static get observedAttributes() {
        return ['scrollable'];
    }
    /** ESLMediaRuleList instance of the scrollable type mapping */
    get scrollableTypeRules() {
        return _esl_media_query_core_esl_media_rule_list__WEBPACK_IMPORTED_MODULE_3__.ESLMediaRuleList.parse(this.scrollable);
    }
    /** @returns current scrollable type */
    get currentScrollableType() {
        return this.scrollableTypeRules.activeValue || '';
    }
    connectedCallback() {
        super.connectedCallback();
        this.scrollableTypeRules.addListener(this._onScrollableTypeChange);
        this.updateScrollableType();
    }
    disconnectedCallback() {
        super.disconnectedCallback();
        this.scrollableTypeRules.removeListener(this._onScrollableTypeChange);
        this.unbindScrollableEvents();
    }
    attributeChangedCallback(attrName, oldVal, newVal) {
        if (!this.connected || oldVal === newVal)
            return;
        if (attrName === 'scrollable') {
            this.scrollableTypeRules.removeListener(this._onScrollableTypeChange);
            _esl_utils_decorators_memoize__WEBPACK_IMPORTED_MODULE_4__.memoize.clear(this, 'scrollableTypeRules');
            this.scrollableTypeRules.addListener(this._onScrollableTypeChange);
            this.updateScrollableType();
        }
    }
    bindScrollableEvents() {
        var _a;
        this.addEventListener('esl:change:active', this._onTriggerStateChange);
        this.addEventListener('click', this._onClick, false);
        this.addEventListener('focusin', this._onFocus);
        (_a = this.$scrollableTarget) === null || _a === void 0 ? void 0 : _a.addEventListener('scroll', this._onScroll, { passive: true });
        window.addEventListener('resize', this._onResize);
    }
    unbindScrollableEvents() {
        var _a;
        this.removeEventListener('esl:change:active', this._onTriggerStateChange);
        this.removeEventListener('click', this._onClick, false);
        this.removeEventListener('focusin', this._onFocus);
        (_a = this.$scrollableTarget) === null || _a === void 0 ? void 0 : _a.removeEventListener('scroll', this._onScroll);
        window.removeEventListener('resize', this._onResize);
    }
    /** Collection of inner {@link ESLTab} items */
    get $tabs() {
        const els = this.querySelectorAll(_esl_tab__WEBPACK_IMPORTED_MODULE_5__.ESLTab.is);
        return els ? Array.from(els) : [];
    }
    /** Active {@link ESLTab} item */
    get $current() {
        return this.$tabs.find((el) => el.active) || null;
    }
    /** Container element to scroll */
    get $scrollableTarget() {
        return this.querySelector(this.scrollableTarget);
    }
    /** Is the scrollable mode enabled ? */
    get isScrollable() {
        return this.currentScrollableType !== 'disabled';
    }
    /** Move scroll to the next/previous item */
    moveTo(direction, behavior = 'smooth') {
        const $scrollableTarget = this.$scrollableTarget;
        if (!$scrollableTarget)
            return;
        let left = $scrollableTarget.offsetWidth;
        left = _esl_utils_dom_rtl__WEBPACK_IMPORTED_MODULE_6__.RTLUtils.isRtl(this) && _esl_utils_dom_rtl__WEBPACK_IMPORTED_MODULE_6__.RTLUtils.scrollType !== 'reverse' ? -left : left;
        left = direction === 'left' ? -left : left;
        $scrollableTarget.scrollBy({ left, behavior });
    }
    /** Scroll tab to the view */
    fitToViewport($trigger, behavior = 'smooth') {
        this.updateMarkers();
        const $scrollableTarget = this.$scrollableTarget;
        if (!$scrollableTarget || !$trigger)
            return;
        const areaRect = $scrollableTarget.getBoundingClientRect();
        const itemRect = $trigger.getBoundingClientRect();
        $scrollableTarget.scrollBy({
            left: this.calcScrollOffset(itemRect, areaRect),
            behavior
        });
        this.updateArrows();
    }
    /** Get scroll offset position from the selected item rectangle */
    calcScrollOffset(itemRect, areaRect) {
        const isReversedRTL = _esl_utils_dom_rtl__WEBPACK_IMPORTED_MODULE_6__.RTLUtils.isRtl(this) && _esl_utils_dom_rtl__WEBPACK_IMPORTED_MODULE_6__.RTLUtils.scrollType === 'reverse';
        if (this.currentScrollableType === 'center') {
            const shift = itemRect.left + itemRect.width / 2 - (areaRect.left + areaRect.width / 2);
            return isReversedRTL ? -shift : shift;
        }
        // item is out of area from the right side
        // else item out is of area from the left side
        if (itemRect.right > areaRect.right) {
            return isReversedRTL ? Math.floor(areaRect.right - itemRect.right) : Math.ceil(itemRect.right - areaRect.right);
        }
        else if (itemRect.left < areaRect.left) {
            return isReversedRTL ? Math.ceil(areaRect.left - itemRect.left) : Math.floor(itemRect.left - areaRect.left);
        }
    }
    updateArrows() {
        const $scrollableTarget = this.$scrollableTarget;
        if (!$scrollableTarget)
            return;
        const swapSides = _esl_utils_dom_rtl__WEBPACK_IMPORTED_MODULE_6__.RTLUtils.isRtl(this) && _esl_utils_dom_rtl__WEBPACK_IMPORTED_MODULE_6__.RTLUtils.scrollType === 'default';
        const scrollStart = Math.abs($scrollableTarget.scrollLeft) > 1;
        const scrollEnd = Math.abs($scrollableTarget.scrollLeft) + $scrollableTarget.clientWidth + 1 < $scrollableTarget.scrollWidth;
        const $rightArrow = this.querySelector('[data-tab-direction="right"]');
        const $leftArrow = this.querySelector('[data-tab-direction="left"]');
        $leftArrow && $leftArrow.toggleAttribute('disabled', !(swapSides ? scrollEnd : scrollStart));
        $rightArrow && $rightArrow.toggleAttribute('disabled', !(swapSides ? scrollStart : scrollEnd));
    }
    updateMarkers() {
        const $scrollableTarget = this.$scrollableTarget;
        if (!$scrollableTarget)
            return;
        const hasScroll = this.isScrollable && ($scrollableTarget.scrollWidth > this.clientWidth);
        this.toggleAttribute('has-scroll', hasScroll);
    }
    /** Update element state according to scrollable type */
    updateScrollableType() {
        ESLTabs_1.supportedScrollableTypes.forEach((type) => {
            _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_7__.CSSClassUtils.toggle(this, `${type}-alignment`, this.currentScrollableType === type);
        });
        this._deferredFitToViewport(this.$current);
        if (this.currentScrollableType === 'disabled') {
            this.unbindScrollableEvents();
        }
        else {
            this.bindScrollableEvents();
        }
    }
    _onTriggerStateChange({ detail }) {
        if (!detail.active)
            return;
        this._deferredFitToViewport(this.$current);
    }
    _onClick(event) {
        const eventTarget = event.target;
        const target = eventTarget.closest('[data-tab-direction]');
        const direction = target && target.dataset.tabDirection;
        if (!direction)
            return;
        this.moveTo(direction);
    }
    _onFocus(e) {
        const target = e.target;
        if (target instanceof _esl_tab__WEBPACK_IMPORTED_MODULE_5__.ESLTab)
            this._deferredFitToViewport(target);
    }
    _onScroll() {
        this._deferredUpdateArrows();
    }
    /** Handles scrollable type change */
    _onScrollableTypeChange() {
        this.updateScrollableType();
    }
};
ESLTabs.is = 'esl-tabs';
/** List of supported scrollable types */
ESLTabs.supportedScrollableTypes = ['disabled', 'side', 'center'];
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)({ defaultValue: 'disabled' })
], ESLTabs.prototype, "scrollable", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)({ defaultValue: '.esl-tab-container' })
], ESLTabs.prototype, "scrollableTarget", void 0);
__decorate([
    (0,_esl_utils_decorators_memoize__WEBPACK_IMPORTED_MODULE_4__.memoize)()
], ESLTabs.prototype, "scrollableTypeRules", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_9__.bind
], ESLTabs.prototype, "_onTriggerStateChange", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_9__.bind
], ESLTabs.prototype, "_onClick", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_9__.bind
], ESLTabs.prototype, "_onFocus", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_9__.bind
], ESLTabs.prototype, "_onScroll", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_9__.bind
], ESLTabs.prototype, "_onScrollableTypeChange", null);
ESLTabs = ESLTabs_1 = __decorate([
    (0,_esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_10__.ExportNs)('Tabs')
], ESLTabs);



/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-toggleable/core/esl-toggleable-dispatcher.js":
/*!*******************************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-toggleable/core/esl-toggleable-dispatcher.js ***!
  \*******************************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ESLToggleableDispatcher": () => (/* binding */ ESLToggleableDispatcher)
/* harmony export */ });
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/core/esl-base-element.js");
/* harmony import */ var _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../esl-utils/decorators/bind */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/bind.js");
/* harmony import */ var _esl_utils_dom_events__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../esl-utils/dom/events */ "./node_modules/@exadel/esl/modules/esl-utils/dom/events.js");
/* harmony import */ var _esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../../esl-utils/environment/export-ns */ "./node_modules/@exadel/esl/modules/esl-utils/environment/export-ns.js");
/* harmony import */ var _esl_toggleable__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./esl-toggleable */ "./node_modules/@exadel/esl/modules/esl-toggleable/core/esl-toggleable.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};





/**
 * ESLToggleableDispatcher
 * @author Julia Murashko, Alexey Stsefanovich (ala'n)
 *
 * ESLToggleableDispatcher - plugin component, that prevents activation of multiple ESLToggleable instances in bounds of managed container.
 */
let ESLToggleableDispatcher = class ESLToggleableDispatcher extends _esl_base_element_core__WEBPACK_IMPORTED_MODULE_0__.ESLBaseElement {
    constructor() {
        super(...arguments);
        this._popups = new Map();
    }
    /**
     * Initialize ToggleableGroupDispatcher
     * Uses esl-toggleable-dispatcher tag and document body root by default
     */
    static init(root = document.body, tagName = this.is) {
        if (!root)
            throw new Error('Root element should be specified');
        const instances = root.getElementsByTagName(tagName);
        if (instances.length)
            return;
        this.register(tagName);
        root.insertAdjacentElement('afterbegin', document.createElement(tagName));
    }
    connectedCallback() {
        super.connectedCallback();
        this.root = this.parentElement;
    }
    disconnectedCallback() {
        super.disconnectedCallback();
        this.root = null;
    }
    bindEvents() {
        if (!this.root)
            return;
        this.root.addEventListener('esl:before:show', this._onBeforeShow);
        this.root.addEventListener('esl:show', this._onShow);
        this.root.addEventListener('esl:hide', this._onHide);
        this.root.addEventListener('esl:change:group', this._onChangeGroup);
    }
    unbindEvents() {
        if (!this.root)
            return;
        this.root.removeEventListener('esl:before:show', this._onBeforeShow);
        this.root.removeEventListener('esl:show', this._onShow);
        this.root.removeEventListener('esl:hide', this._onHide);
        this.root.removeEventListener('esl:change:group', this._onChangeGroup);
    }
    /** Observed element */
    get root() {
        return this._root;
    }
    set root(root) {
        this.unbindEvents();
        this._root = root;
        this.bindEvents();
    }
    /** Guard-condition for targets */
    isAcceptable(target) {
        if (!(target instanceof _esl_toggleable__WEBPACK_IMPORTED_MODULE_1__.ESLToggleable))
            return false;
        return !!target.groupName && target.groupName !== 'none';
    }
    /** Hide active element in group */
    hideActive(groupName, activator) {
        const active = this.getActive(groupName);
        if (!active || active === activator)
            return;
        active.hide({
            initiator: 'dispatcher',
            dispatcher: this,
            activator
        });
    }
    /** Set active element in group */
    setActive(groupName, popup) {
        if (!groupName)
            return;
        this.hideActive(groupName, popup);
        this._popups.set(groupName, popup);
    }
    /** Get active element in group or undefined if group doesn't exist */
    getActive(groupName) {
        return this._popups.get(groupName);
    }
    /** Delete element from the group if passed element is currently active */
    deleteActive(groupName, popup) {
        if (this.getActive(groupName) !== popup)
            return;
        this._popups.delete(groupName);
    }
    /** Hide active element before e.target will be shown */
    _onBeforeShow(e) {
        const target = _esl_utils_dom_events__WEBPACK_IMPORTED_MODULE_2__.EventUtils.source(e);
        if (!this.isAcceptable(target))
            return;
        this.hideActive(target.groupName, target);
    }
    /** Update active element after a new element is shown */
    _onShow(e) {
        const target = _esl_utils_dom_events__WEBPACK_IMPORTED_MODULE_2__.EventUtils.source(e);
        if (!this.isAcceptable(target))
            return;
        this.setActive(target.groupName, target);
    }
    /** Update group state after active element is hidden */
    _onHide(e) {
        const target = _esl_utils_dom_events__WEBPACK_IMPORTED_MODULE_2__.EventUtils.source(e);
        if (!this.isAcceptable(target))
            return;
        this.deleteActive(target.groupName, target);
    }
    /** Update active elements */
    _onChangeGroup(e) {
        const target = _esl_utils_dom_events__WEBPACK_IMPORTED_MODULE_2__.EventUtils.source(e);
        if (!this.isAcceptable(target))
            return;
        const { oldGroupName, newGroupName } = e.detail;
        this.deleteActive(oldGroupName, target);
        this.setActive(newGroupName, target);
    }
};
ESLToggleableDispatcher.is = 'esl-toggleable-dispatcher';
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_3__.bind
], ESLToggleableDispatcher.prototype, "_onBeforeShow", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_3__.bind
], ESLToggleableDispatcher.prototype, "_onShow", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_3__.bind
], ESLToggleableDispatcher.prototype, "_onHide", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_3__.bind
], ESLToggleableDispatcher.prototype, "_onChangeGroup", null);
ESLToggleableDispatcher = __decorate([
    (0,_esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_4__.ExportNs)('ToggleableDispatcher')
], ESLToggleableDispatcher);



/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-toggleable/core/esl-toggleable.js":
/*!********************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-toggleable/core/esl-toggleable.js ***!
  \********************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ESLToggleable": () => (/* binding */ ESLToggleable)
/* harmony export */ });
/* harmony import */ var _esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_11__ = __webpack_require__(/*! ../../esl-utils/environment/export-ns */ "./node_modules/@exadel/esl/modules/esl-utils/environment/export-ns.js");
/* harmony import */ var _esl_utils_dom_keys__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ../../esl-utils/dom/keys */ "./node_modules/@exadel/esl/modules/esl-utils/dom/keys.js");
/* harmony import */ var _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ../../esl-utils/dom/class */ "./node_modules/@exadel/esl/modules/esl-utils/dom/class.js");
/* harmony import */ var _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! ../../esl-utils/decorators/bind */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/bind.js");
/* harmony import */ var _esl_utils_misc_object__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../../esl-utils/misc/object */ "./node_modules/@exadel/esl/modules/esl-utils/misc/object.js");
/* harmony import */ var _esl_utils_misc_uid__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../esl-utils/misc/uid */ "./node_modules/@exadel/esl/modules/esl-utils/misc/uid.js");
/* harmony import */ var _esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../esl-utils/environment/device-detector */ "./node_modules/@exadel/esl/modules/esl-utils/environment/device-detector.js");
/* harmony import */ var _esl_utils_async_delayed_task__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../../esl-utils/async/delayed-task */ "./node_modules/@exadel/esl/modules/esl-utils/async/delayed-task.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/core/esl-base-element.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/attr.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/bool-attr.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/json-attr.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var ESLToggleable_1;









const activators = new WeakMap();
/**
 * ESLToggleable component
 * @author Julia Murashko, Alexey Stsefanovich (ala'n)
 *
 * ESLToggleable - a custom element, that is used as a base for "Popup-like" components creation
 */
let ESLToggleable = ESLToggleable_1 = class ESLToggleable extends _esl_base_element_core__WEBPACK_IMPORTED_MODULE_0__.ESLBaseElement {
    constructor() {
        super(...arguments);
        /** Inner state */
        this._open = false;
        /** Inner show/hide task manager instance */
        this._task = new _esl_utils_async_delayed_task__WEBPACK_IMPORTED_MODULE_1__.DelayedTask();
        /** Marker for current hover listener state */
        this._trackHover = false;
    }
    static get observedAttributes() {
        return ['open', 'group'];
    }
    connectedCallback() {
        super.connectedCallback();
        if (!this.id && !this.noAutoId) {
            const tag = this.constructor.is;
            this.id = (0,_esl_utils_misc_uid__WEBPACK_IMPORTED_MODULE_2__.sequentialUID)(tag, tag + '-');
        }
        this.initiallyOpened = this.hasAttribute('open');
        this.bindEvents();
        this.setInitialState();
    }
    disconnectedCallback() {
        super.disconnectedCallback();
        this.unbindEvents();
        activators.delete(this);
    }
    attributeChangedCallback(attrName, oldVal, newVal) {
        if (!this.connected || newVal === oldVal)
            return;
        switch (attrName) {
            case 'open':
                if (this.open === this.hasAttribute('open'))
                    return;
                this.toggle(this.open, { initiator: 'attribute', showDelay: 0, hideDelay: 0 });
                break;
            case 'group':
                this.$$fire('change:group', {
                    detail: { oldGroupName: oldVal, newGroupName: newVal }
                });
                break;
        }
    }
    /** Set initial state of the Toggleable */
    setInitialState() {
        if (this.initialParams) {
            this.toggle(this.initiallyOpened, this.initialParams);
        }
    }
    bindEvents() {
        this.addEventListener('click', this._onClick);
        this.addEventListener('keydown', this._onKeyboardEvent);
        this.addEventListener('esl:show:request', this._onShowRequest);
    }
    unbindEvents() {
        this.removeEventListener('click', this._onClick);
        this.removeEventListener('keydown', this._onKeyboardEvent);
        this.removeEventListener('esl:show:request', this._onShowRequest);
        this.bindOutsideEventTracking(false);
        this.bindHoverStateTracking(false);
    }
    /** Bind outside action event listeners */
    bindOutsideEventTracking(track) {
        document.body.removeEventListener('keydown', this._onOutsideAction, true);
        document.body.removeEventListener('mouseup', this._onOutsideAction, true);
        document.body.removeEventListener('touchend', this._onOutsideAction, true);
        if (track) {
            document.body.addEventListener('keydown', this._onOutsideAction, true);
            document.body.addEventListener('mouseup', this._onOutsideAction, true);
            document.body.addEventListener('touchend', this._onOutsideAction, true);
        }
    }
    /** Bind hover events listeners for the Toggleable itself */
    bindHoverStateTracking(track, hideDelay) {
        if (!_esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_3__.DeviceDetector.hasHover)
            return;
        this._trackHoverDelay = track && hideDelay !== undefined ? +hideDelay : undefined;
        if (this._trackHover === track)
            return;
        this._trackHover = track;
        this.removeEventListener('mouseenter', this._onMouseEnter);
        this.removeEventListener('mouseleave', this._onMouseLeave);
        if (this._trackHover) {
            this.addEventListener('mouseenter', this._onMouseEnter);
            this.addEventListener('mouseleave', this._onMouseLeave);
        }
    }
    /** Function to merge the result action params */
    mergeDefaultParams(params) {
        return Object.assign({}, this.defaultParams, (0,_esl_utils_misc_object__WEBPACK_IMPORTED_MODULE_4__.copyDefinedKeys)(params));
    }
    /** Toggle the element state */
    toggle(state = !this.open, params) {
        return state ? this.show(params) : this.hide(params);
    }
    /** Change the element state to active */
    show(params) {
        params = this.mergeDefaultParams(params);
        this._task.put(this.showTask.bind(this, params), (0,_esl_utils_misc_object__WEBPACK_IMPORTED_MODULE_4__.defined)(params.showDelay, params.delay));
        this.bindOutsideEventTracking(this.closeOnOutsideAction);
        this.bindHoverStateTracking(!!params.trackHover, (0,_esl_utils_misc_object__WEBPACK_IMPORTED_MODULE_4__.defined)(params.hideDelay, params.delay));
        return this;
    }
    /** Change the element state to inactive */
    hide(params) {
        params = this.mergeDefaultParams(params);
        this._task.put(this.hideTask.bind(this, params), (0,_esl_utils_misc_object__WEBPACK_IMPORTED_MODULE_4__.defined)(params.hideDelay, params.delay));
        this.bindOutsideEventTracking(false);
        this.bindHoverStateTracking(!!params.trackHover, (0,_esl_utils_misc_object__WEBPACK_IMPORTED_MODULE_4__.defined)(params.hideDelay, params.delay));
        return this;
    }
    /** Actual show task to execute by toggleable task manger ({@link DelayedTask} out of the box) */
    showTask(params) {
        if (!params.force && this.open)
            return;
        if (!params.silent && !this.$$fire('before:show', { detail: { params } }))
            return;
        this.activator = params.activator;
        this.open = true;
        this.onShow(params);
        if (!params.silent)
            this.$$fire('show', { detail: { params }, cancelable: false });
    }
    /** Actual hide task to execute by toggleable task manger ({@link DelayedTask} out of the box) */
    hideTask(params) {
        if (!params.force && !this.open)
            return;
        if (!params.silent && !this.$$fire('before:hide', { detail: { params } }))
            return;
        this.open = false;
        this.onHide(params);
        this.bindOutsideEventTracking(false);
        if (!params.silent)
            this.$$fire('hide', { detail: { params }, cancelable: false });
    }
    /**
     * Actions to execute on show toggleable.
     * Inner state and 'open' attribute are not affected and updated before `onShow` execution.
     * Adds CSS classes, update a11y and fire esl:refresh event by default.
     */
    onShow(params) {
        _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_5__.CSSClassUtils.add(this, this.activeClass);
        _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_5__.CSSClassUtils.add(document.body, this.bodyClass, this);
        this.updateA11y();
        this.$$fire('esl:refresh'); // To notify other components about content change
    }
    /**
     * Actions to execute on hide toggleable.
     * Inner state and 'open' attribute are not affected and updated before `onShow` execution.
     * Removes CSS classes and update a11y by default.
     */
    onHide(params) {
        _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_5__.CSSClassUtils.remove(this, this.activeClass);
        _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_5__.CSSClassUtils.remove(document.body, this.bodyClass, this);
        this.updateA11y();
    }
    /** Active state marker */
    get open() {
        return this._open;
    }
    set open(value) {
        this.toggleAttribute('open', this._open = value);
    }
    /** Last component that has activated the element. Uses {@link ToggleableActionParams.activator}*/
    get activator() {
        return activators.get(this);
    }
    set activator(el) {
        el ? activators.set(this, el) : activators.delete(this);
    }
    /** Returns the element to apply a11y attributes */
    get $a11yTarget() {
        const target = this.getAttribute('a11y-target');
        if (target === 'none')
            return null;
        return target ? this.querySelector(target) : this;
    }
    /** Called on show and on hide actions to update a11y state accordingly */
    updateA11y() {
        const targetEl = this.$a11yTarget;
        if (!targetEl)
            return;
        targetEl.setAttribute('aria-hidden', String(!this._open));
    }
    /** @returns if the passed event should trigger hide action */
    isOutsideAction(e) {
        const target = e.target;
        // target is inside current toggleable
        if (this.contains(target))
            return false;
        // target is inside last activator
        if (this.activator && this.activator.contains(target))
            return false;
        // Event is not a system command key
        return !(e instanceof KeyboardEvent && _esl_utils_dom_keys__WEBPACK_IMPORTED_MODULE_6__.SYSTEM_KEYS.includes(e.key));
    }
    _onClick(e) {
        const target = e.target;
        if (this.closeTrigger && target.closest(this.closeTrigger)) {
            this.hide({ initiator: 'close', activator: target, event: e });
        }
    }
    _onOutsideAction(e) {
        if (!this.isOutsideAction(e))
            return;
        // Used 0 delay to decrease priority of the request
        this.hide({ initiator: 'outsideaction', hideDelay: 0, event: e });
    }
    _onKeyboardEvent(e) {
        if (this.closeOnEsc && e.key === _esl_utils_dom_keys__WEBPACK_IMPORTED_MODULE_6__.ESC) {
            this.hide({ initiator: 'keyboard', event: e });
        }
    }
    _onMouseEnter(e) {
        const hideDelay = this._trackHoverDelay;
        const baseParams = { initiator: 'mouseenter', trackHover: true, activator: this.activator, event: e, hideDelay };
        this.show(Object.assign(baseParams, this.trackHoverParams));
    }
    _onMouseLeave(e) {
        const hideDelay = this._trackHoverDelay;
        const baseParams = { initiator: 'mouseleave', trackHover: true, activator: this.activator, event: e, hideDelay };
        this.hide(Object.assign(baseParams, this.trackHoverParams));
    }
    /** Actions to execute on show request */
    _onShowRequest() {
        this.show();
    }
};
ESLToggleable.is = 'esl-toggleable';
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_7__.attr)()
], ESLToggleable.prototype, "bodyClass", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_7__.attr)({ defaultValue: 'open' })
], ESLToggleable.prototype, "activeClass", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_7__.attr)({ name: 'group' })
], ESLToggleable.prototype, "groupName", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_7__.attr)({ name: 'close-on' })
], ESLToggleable.prototype, "closeTrigger", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.boolAttr)()
], ESLToggleable.prototype, "noAutoId", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.boolAttr)()
], ESLToggleable.prototype, "closeOnEsc", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.boolAttr)()
], ESLToggleable.prototype, "closeOnOutsideAction", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_9__.jsonAttr)({ defaultValue: { force: true, initiator: 'init' } })
], ESLToggleable.prototype, "initialParams", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_9__.jsonAttr)({ defaultValue: {} })
], ESLToggleable.prototype, "defaultParams", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_9__.jsonAttr)({ defaultValue: {} })
], ESLToggleable.prototype, "trackHoverParams", void 0);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_10__.bind
], ESLToggleable.prototype, "_onClick", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_10__.bind
], ESLToggleable.prototype, "_onOutsideAction", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_10__.bind
], ESLToggleable.prototype, "_onKeyboardEvent", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_10__.bind
], ESLToggleable.prototype, "_onMouseEnter", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_10__.bind
], ESLToggleable.prototype, "_onMouseLeave", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_10__.bind
], ESLToggleable.prototype, "_onShowRequest", null);
ESLToggleable = ESLToggleable_1 = __decorate([
    (0,_esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_11__.ExportNs)('Toggleable')
], ESLToggleable);



/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-traversing-query/core/esl-traversing-query.js":
/*!********************************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-traversing-query/core/esl-traversing-query.js ***!
  \********************************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "TraversingQuery": () => (/* binding */ TraversingQuery)
/* harmony export */ });
/* harmony import */ var _esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../esl-utils/environment/export-ns */ "./node_modules/@exadel/esl/modules/esl-utils/environment/export-ns.js");
/* harmony import */ var _esl_utils_misc_array__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../../esl-utils/misc/array */ "./node_modules/@exadel/esl/modules/esl-utils/misc/array.js");
/* harmony import */ var _esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../esl-utils/misc/format */ "./node_modules/@exadel/esl/modules/esl-utils/misc/format.js");
/* harmony import */ var _esl_utils_dom_traversing__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../esl-utils/dom/traversing */ "./node_modules/@exadel/esl/modules/esl-utils/dom/traversing.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var TraversingQuery_1;




/**
 * Traversing Query utility to find element via extended selector query
 * Extended query supports
 * - plain CSS selectors
 * - relative selectors (selectors that don't start from a plain selector will use passed base Element as a root)
 * - ::next and ::prev sibling pseudo-selectors
 * - ::parent and ::child pseudo-selectors
 * - ::find pseudo-selector
 * - ::first, ::last and :nth(#) limitation pseudo-selectors
 * - ::filter, ::not filtration pseudo-selectors
 *
 * @example
 * - `#id .class [attr]` - find by CSS selector in a current document
 * - ` ` - get current base element
 * - `::next` - get next sibling element
 * - `::prev` - get previous sibling element
 * - `::parent` - get base element parent
 * - `::parent(#id .class [attr])` - find the closest parent matching passed selector
 * - `::child(#id .class [attr])` - find direct child element(s) that match passed selector
 * - `::find(#id .class [attr])` - find child element(s) that match passed selector
 * - `::find(buttons, a)::not([hidden])` - find all buttons and anchors that are not have hidden attribute
 * - `::find(buttons, a)::filter(:first-child)` - find all buttons and anchors that are first child in container
 * - `::parent::child(some-tag)` - find direct child element(s) that match tag 'some-tag' in the parent
 * - `#id .class [attr]::parent` - find parent of element matching selector '#id .class [attr]' in document
 * - `::find(.row)::last::parent` - find parent of the last element matching selector '.row' from the base element subtree
 */
let TraversingQuery = TraversingQuery_1 = class TraversingQuery {
    /**
     * @returns RegExp that selects all known processors in query string
     * e.g. /(::parent|::child|::next|::prev)/
     */
    static get PROCESSORS_REGEX() {
        const keys = Object.keys(this.ELEMENT_PROCESSORS).concat(Object.keys(this.COLLECTION_PROCESSORS));
        return new RegExp(`(${keys.join('|')})`, 'g');
    }
    static isCollectionProcessor([name]) {
        return !!name && (name in this.COLLECTION_PROCESSORS);
    }
    static processElement(el, [name, selString]) {
        const sel = (0,_esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_0__.unwrapParenthesis)(selString || '');
        if (!name || !(name in this.ELEMENT_PROCESSORS))
            return [];
        return (0,_esl_utils_misc_array__WEBPACK_IMPORTED_MODULE_1__.wrap)(this.ELEMENT_PROCESSORS[name](el, sel));
    }
    static processCollection(els, [name, selString]) {
        const sel = (0,_esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_0__.unwrapParenthesis)(selString || '');
        if (!name || !(name in this.COLLECTION_PROCESSORS))
            return [];
        return (0,_esl_utils_misc_array__WEBPACK_IMPORTED_MODULE_1__.wrap)(this.COLLECTION_PROCESSORS[name](els, sel));
    }
    static traverseChain(collection, processors, findFirst) {
        if (!processors.length || !collection.length)
            return collection;
        const [processor, ...rest] = processors;
        if (this.isCollectionProcessor(processor)) {
            const processedItem = this.processCollection(collection, processor);
            return this.traverseChain(processedItem, rest, findFirst);
        }
        const result = [];
        for (const target of collection) {
            const processedItem = this.processElement(target, processor);
            const resultCollection = this.traverseChain(processedItem, rest, findFirst);
            if (!resultCollection.length)
                continue;
            if (findFirst)
                return resultCollection.slice(0, 1);
            result.push(...resultCollection);
        }
        return (0,_esl_utils_misc_array__WEBPACK_IMPORTED_MODULE_1__.uniq)(result);
    }
    static traverse(query, findFirst, base, scope = document) {
        const parts = query.split(this.PROCESSORS_REGEX).map((term) => term.trim());
        const rootSel = parts.shift();
        const baseCollection = base ? [base] : [];
        const initial = rootSel ? Array.from(scope.querySelectorAll(rootSel)) : baseCollection;
        return this.traverseChain(initial, (0,_esl_utils_misc_array__WEBPACK_IMPORTED_MODULE_1__.tuple)(parts), findFirst);
    }
    /** @returns first matching element reached via {@link TraversingQuery} rules */
    static first(query, base, scope) {
        return TraversingQuery_1.traverse(query, true, base, scope)[0] || null;
    }
    /** @returns Array of all matching elements reached via {@link TraversingQuery} rules */
    static all(query, base, scope) {
        return TraversingQuery_1.traverse(query, false, base, scope);
    }
};
TraversingQuery.ELEMENT_PROCESSORS = {
    '::find': _esl_utils_dom_traversing__WEBPACK_IMPORTED_MODULE_2__.findAll,
    '::next': _esl_utils_dom_traversing__WEBPACK_IMPORTED_MODULE_2__.findNext,
    '::prev': _esl_utils_dom_traversing__WEBPACK_IMPORTED_MODULE_2__.findPrev,
    '::child': _esl_utils_dom_traversing__WEBPACK_IMPORTED_MODULE_2__.findChildren,
    '::parent': _esl_utils_dom_traversing__WEBPACK_IMPORTED_MODULE_2__.findParent
};
TraversingQuery.COLLECTION_PROCESSORS = {
    '::first': (list) => list.slice(0, 1),
    '::last': (list) => list.slice(-1),
    '::nth': (list, sel) => {
        const index = sel ? +sel : NaN;
        return (0,_esl_utils_misc_array__WEBPACK_IMPORTED_MODULE_1__.wrap)(list[index - 1]);
    },
    '::not': (list, sel) => list.filter((el) => !el.matches(sel || '')),
    '::filter': (list, sel) => list.filter((el) => el.matches(sel || ''))
};
TraversingQuery = TraversingQuery_1 = __decorate([
    (0,_esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_3__.ExportNs)('TraversingQuery')
], TraversingQuery);



/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-trigger/core/esl-trigger.js":
/*!**************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-trigger/core/esl-trigger.js ***!
  \**************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ESLTrigger": () => (/* binding */ ESLTrigger)
/* harmony export */ });
/* harmony import */ var _esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_11__ = __webpack_require__(/*! ../../esl-utils/environment/export-ns */ "./node_modules/@exadel/esl/modules/esl-utils/environment/export-ns.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/core/esl-base-element.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/bool-attr.js");
/* harmony import */ var _esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! ../../esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/attr.js");
/* harmony import */ var _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! ../../esl-utils/decorators/bind */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/bind.js");
/* harmony import */ var _esl_utils_decorators_ready__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! ../../esl-utils/decorators/ready */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/ready.js");
/* harmony import */ var _esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../../esl-utils/misc/format */ "./node_modules/@exadel/esl/modules/esl-utils/misc/format.js");
/* harmony import */ var _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ../../esl-utils/dom/class */ "./node_modules/@exadel/esl/modules/esl-utils/dom/class.js");
/* harmony import */ var _esl_utils_dom_keys__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ../../esl-utils/dom/keys */ "./node_modules/@exadel/esl/modules/esl-utils/dom/keys.js");
/* harmony import */ var _esl_traversing_query_core__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../esl-traversing-query/core */ "./node_modules/@exadel/esl/modules/esl-traversing-query/core/esl-traversing-query.js");
/* harmony import */ var _esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../../esl-utils/environment/device-detector */ "./node_modules/@exadel/esl/modules/esl-utils/environment/device-detector.js");
/* harmony import */ var _esl_media_query_core__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../esl-media-query/core */ "./node_modules/@exadel/esl/modules/esl-media-query/core/esl-media-query.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};










let ESLTrigger = class ESLTrigger extends _esl_base_element_core__WEBPACK_IMPORTED_MODULE_0__.ESLBaseElement {
    static get observedAttributes() {
        return ['target'];
    }
    attributeChangedCallback(attrName) {
        if (!this.connected)
            return;
        if (attrName === 'target')
            return this.updateTargetFromSelector();
    }
    /** Target observable Toggleable */
    get $target() {
        return this._$target;
    }
    set $target(newPopupInstance) {
        this.unbindEvents();
        this._$target = newPopupInstance;
        this.bindEvents();
        this._onTargetStateChange();
    }
    /** Element target to setup aria attributes */
    get $a11yTarget() {
        return this.a11yTarget ? this.querySelector(this.a11yTarget) : this;
    }
    /** Marker to allow track hover */
    get allowHover() {
        return _esl_utils_environment_device_detector__WEBPACK_IMPORTED_MODULE_1__.DeviceDetector.hasHover && _esl_media_query_core__WEBPACK_IMPORTED_MODULE_2__.ESLMediaQuery["for"](this.trackHover).matches;
    }
    /** Marker to allow track clicks */
    get allowClick() {
        return _esl_media_query_core__WEBPACK_IMPORTED_MODULE_2__.ESLMediaQuery["for"](this.trackClick).matches;
    }
    connectedCallback() {
        super.connectedCallback();
        this.updateTargetFromSelector();
        this.initA11y();
    }
    disconnectedCallback() {
        this.unbindEvents();
    }
    bindEvents() {
        if (!this.$target)
            return;
        this.$target.addEventListener('esl:show', this._onTargetStateChange);
        this.$target.addEventListener('esl:hide', this._onTargetStateChange);
        this.addEventListener('click', this._onClick);
        this.addEventListener('keydown', this._onKeydown);
        this.addEventListener('mouseenter', this._onMouseEnter);
        this.addEventListener('mouseleave', this._onMouseLeave);
    }
    unbindEvents() {
        if (!this.$target)
            return;
        this.$target.removeEventListener('esl:show', this._onTargetStateChange);
        this.$target.removeEventListener('esl:hide', this._onTargetStateChange);
        this.removeEventListener('click', this._onClick);
        this.removeEventListener('keydown', this._onKeydown);
        this.removeEventListener('mouseenter', this._onMouseEnter);
        this.removeEventListener('mouseleave', this._onMouseLeave);
    }
    /** Update `$target` Toggleable  from `target` selector */
    updateTargetFromSelector() {
        if (!this.target)
            return;
        this.$target = _esl_traversing_query_core__WEBPACK_IMPORTED_MODULE_3__.TraversingQuery.first(this.target, this);
    }
    /** Check if the event target should be ignored */
    isTargetIgnored(target) {
        if (!target || !(target instanceof HTMLElement) || !this.ignore)
            return false;
        const $ignore = target.closest(this.ignore);
        // Ignore only inner elements (but do not ignore the trigger itself)
        return !!$ignore && $ignore !== this && this.contains($ignore);
    }
    /** Merge params to pass to the toggleable */
    mergeToggleableParams(...params) {
        return Object.assign({
            initiator: 'trigger',
            activator: this
        }, ...params);
    }
    /** Show target toggleable with passed params */
    showTarget(params = {}) {
        const actionParams = this.mergeToggleableParams({
            delay: (0,_esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_4__.parseNumber)(this.showDelay)
        }, params);
        if (this.$target && typeof this.$target.show === 'function') {
            this.$target.show(actionParams);
        }
    }
    /** Hide target toggleable with passed params */
    hideTarget(params = {}) {
        const actionParams = this.mergeToggleableParams({
            delay: (0,_esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_4__.parseNumber)(this.hideDelay)
        }, params);
        if (this.$target && typeof this.$target.hide === 'function') {
            this.$target.hide(actionParams);
        }
    }
    /** Toggles target toggleable with passed params */
    toggleTarget(params = {}, state = !this.active) {
        state ? this.showTarget(params) : this.hideTarget(params);
    }
    /**
     * Updates trigger state according to toggleable state
     * Does not produce `esl:change:active` event
     */
    updateState() {
        var _a;
        const isActive = !!((_a = this.$target) === null || _a === void 0 ? void 0 : _a.open);
        const wasActive = this.active;
        this.toggleAttribute('active', isActive);
        const clsTarget = _esl_traversing_query_core__WEBPACK_IMPORTED_MODULE_3__.TraversingQuery.first(this.activeClassTarget, this);
        clsTarget && _esl_utils_dom_class__WEBPACK_IMPORTED_MODULE_5__.CSSClassUtils.toggle(clsTarget, this.activeClass, isActive);
        this.updateA11y();
        return isActive !== wasActive;
    }
    /** Handles ESLToggleable state change */
    _onTargetStateChange(originalEvent) {
        if (!this.updateState())
            return;
        const detail = { active: this.active, originalEvent };
        this.$$fire('change:active', { detail });
    }
    /** Handles `click` event */
    _onClick(event) {
        if (!this.allowClick || this.isTargetIgnored(event.target))
            return;
        event.preventDefault();
        switch (this.mode) {
            case 'show':
                return this.showTarget({ event });
            case 'hide':
                return this.hideTarget({ event });
            default:
                return this.toggleTarget({ event });
        }
    }
    /** Handles `keydown` event */
    _onKeydown(event) {
        if (![_esl_utils_dom_keys__WEBPACK_IMPORTED_MODULE_6__.ENTER, _esl_utils_dom_keys__WEBPACK_IMPORTED_MODULE_6__.SPACE].includes(event.key) || this.isTargetIgnored(event.target))
            return;
        event.preventDefault();
        switch (this.mode) {
            case 'show':
                return this.showTarget({ event });
            case 'hide':
                return this.hideTarget({ event });
            default:
                return this.toggleTarget({ event });
        }
    }
    /** Handles hover `mouseenter` event */
    _onMouseEnter(event) {
        if (!this.allowHover)
            return;
        const delay = (0,_esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_4__.parseNumber)(this.hoverShowDelay);
        this.toggleTarget({ event, delay }, this.mode !== 'hide');
        event.preventDefault();
    }
    /** Handles hover `mouseleave` event */
    _onMouseLeave(event) {
        if (!this.allowHover)
            return;
        if (this.mode === 'show' || this.mode === 'hide')
            return;
        const delay = (0,_esl_utils_misc_format__WEBPACK_IMPORTED_MODULE_4__.parseNumber)(this.hoverHideDelay);
        this.hideTarget({ event, delay, trackHover: true });
        event.preventDefault();
    }
    /** Set initial a11y attributes. Do nothing if trigger contains actionable element */
    initA11y() {
        if (this.$a11yTarget !== this)
            return;
        if (!this.hasAttribute('role'))
            this.setAttribute('role', 'button');
        if (this.getAttribute('role') === 'button' && !this.hasAttribute('tabindex')) {
            this.setAttribute('tabindex', '0');
        }
    }
    /** Update aria attributes */
    updateA11y() {
        const target = this.$a11yTarget;
        if (!target)
            return;
        target.setAttribute('aria-expanded', String(this.active));
        if (this.$target && this.$target.id) {
            target.setAttribute('aria-controls', this.$target.id);
        }
    }
};
ESLTrigger.is = 'esl-trigger';
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_7__.boolAttr)({ readonly: true })
], ESLTrigger.prototype, "active", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)({ defaultValue: '' })
], ESLTrigger.prototype, "activeClass", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)({ defaultValue: '' })
], ESLTrigger.prototype, "activeClassTarget", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)({ defaultValue: 'a[href]' })
], ESLTrigger.prototype, "ignore", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)({ defaultValue: '::next' })
], ESLTrigger.prototype, "target", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)({ defaultValue: 'toggle' })
], ESLTrigger.prototype, "mode", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)({ defaultValue: 'all' })
], ESLTrigger.prototype, "trackClick", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)({ defaultValue: 'not all' })
], ESLTrigger.prototype, "trackHover", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)({ defaultValue: '' })
], ESLTrigger.prototype, "a11yTarget", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)({ defaultValue: 'none' })
], ESLTrigger.prototype, "showDelay", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)({ defaultValue: 'none' })
], ESLTrigger.prototype, "hideDelay", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)({ defaultValue: '0' })
], ESLTrigger.prototype, "hoverShowDelay", void 0);
__decorate([
    (0,_esl_base_element_core__WEBPACK_IMPORTED_MODULE_8__.attr)({ defaultValue: '0' })
], ESLTrigger.prototype, "hoverHideDelay", void 0);
__decorate([
    _esl_utils_decorators_ready__WEBPACK_IMPORTED_MODULE_9__.ready
], ESLTrigger.prototype, "connectedCallback", null);
__decorate([
    _esl_utils_decorators_ready__WEBPACK_IMPORTED_MODULE_9__.ready
], ESLTrigger.prototype, "disconnectedCallback", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_10__.bind
], ESLTrigger.prototype, "_onTargetStateChange", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_10__.bind
], ESLTrigger.prototype, "_onClick", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_10__.bind
], ESLTrigger.prototype, "_onKeydown", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_10__.bind
], ESLTrigger.prototype, "_onMouseEnter", null);
__decorate([
    _esl_utils_decorators_bind__WEBPACK_IMPORTED_MODULE_10__.bind
], ESLTrigger.prototype, "_onMouseLeave", null);
ESLTrigger = __decorate([
    (0,_esl_utils_environment_export_ns__WEBPACK_IMPORTED_MODULE_11__.ExportNs)('Trigger')
], ESLTrigger);



/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/abstract/observable.js":
/*!***************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/abstract/observable.js ***!
  \***************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "Observable": () => (/* binding */ Observable)
/* harmony export */ });
/**
 * Abstract Observable implementation
 * @author Yuliya Adamskaya
 */
class Observable {
    constructor() {
        this._listeners = new Set();
    }
    addListener(listener) {
        this._listeners.add(listener);
    }
    removeListener(listener) {
        this._listeners.delete(listener);
    }
    fire(...args) {
        this._listeners.forEach((listener) => {
            try {
                listener.apply(this, args);
            }
            catch (e) {
                console.error(e);
            }
        });
    }
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/async/debounce.js":
/*!**********************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/async/debounce.js ***!
  \**********************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "debounce": () => (/* binding */ debounce)
/* harmony export */ });
/* harmony import */ var _promise__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./promise */ "./node_modules/@exadel/esl/modules/esl-utils/async/promise.js");

/**
 * Creates a debounced function that implements {@link Debounced}.
 * Debounced function delays invoking func until after wait milliseconds have elapsed
 * since the last time the debounced function was invoked.
 * The func is invoked with the last arguments provided to the debounced function.
 * @param fn - function to decorate
 * @param wait - time to debounce
 * @param thisArg - optional context to call original function, use debounced method call context if not defined
 */
// eslint-disable-next-line @typescript-eslint/ban-types
function debounce(fn, wait = 10, thisArg) {
    let timeout = null;
    let deferred = null;
    function debouncedSubject(...args) {
        deferred = deferred || (0,_promise__WEBPACK_IMPORTED_MODULE_0__.createDeferred)();
        (typeof timeout === 'number') && clearTimeout(timeout);
        timeout = window.setTimeout(() => {
            timeout = null;
            // fn.apply to save call context
            deferred.resolve(fn.apply(thisArg || this, args));
            deferred = null;
        }, wait);
    }
    function cancel() {
        (typeof timeout === 'number') && clearTimeout(timeout);
        timeout = null;
        deferred === null || deferred === void 0 ? void 0 : deferred.reject();
        deferred = null;
    }
    Object.defineProperty(debouncedSubject, 'promise', {
        get: () => deferred ? deferred.promise : Promise.resolve()
    });
    Object.defineProperty(debouncedSubject, 'cancel', {
        writable: false,
        enumerable: false,
        value: cancel
    });
    return debouncedSubject;
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/async/delayed-task.js":
/*!**************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/async/delayed-task.js ***!
  \**************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "DelayedTask": () => (/* binding */ DelayedTask)
/* harmony export */ });
/**
 * Task placeholder with a single place for executing deferred task.
 * Only one task can be planed per DelayedTask instance.
 * @see put DelayedTask.put behaviour description.
 */
class DelayedTask {
    constructor() {
        this._fn = null;
        this._timeout = null;
        /** Execute deferred task immediately */
        this.run = () => {
            this._timeout = null;
            this._fn && this._fn();
        };
    }
    /** @returns Function of currently deferred (planned) task */
    get fn() {
        return this._fn;
    }
    /**
     * Cancel deferred task and planning passed
     * @param task - task function
     * @param delay - time to delay task execution
     *  - pass negative or false to execute task immediately
     *  - pass 0 to plan task to the macrotask
     *  - pass positive number x to delay task on x ms.
     * */
    put(task, delay = false) {
        const prev = this.cancel();
        if (typeof task === 'function') {
            if (delay && typeof delay === 'string')
                delay = +delay;
            if (typeof delay === 'number' && delay >= 0) {
                this._fn = task;
                this._timeout = window.setTimeout(this.run, delay);
            }
            else {
                task();
            }
        }
        return prev;
    }
    /** Cancel deferred (planned) task */
    cancel() {
        const prev = this._fn;
        (typeof this._timeout === 'number') && clearTimeout(this._timeout);
        this._fn = this._timeout = null;
        return prev;
    }
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/async/promise.js":
/*!*********************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/async/promise.js ***!
  \*********************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "promisifyTimeout": () => (/* binding */ promisifyTimeout),
/* harmony export */   "promisifyEvent": () => (/* binding */ promisifyEvent),
/* harmony export */   "promisifyMarker": () => (/* binding */ promisifyMarker),
/* harmony export */   "tryUntil": () => (/* binding */ tryUntil),
/* harmony export */   "repeatSequence": () => (/* binding */ repeatSequence),
/* harmony export */   "createDeferred": () => (/* binding */ createDeferred),
/* harmony export */   "resolvePromise": () => (/* binding */ resolvePromise),
/* harmony export */   "rejectPromise": () => (/* binding */ rejectPromise),
/* harmony export */   "PromiseUtils": () => (/* binding */ PromiseUtils)
/* harmony export */ });
/**
 * @returns Promise that will be resolved in `timeout` with optional `payload`
 */
function promisifyTimeout(timeout, payload) {
    return new Promise((resolve) => setTimeout(resolve.bind(null, payload), timeout));
}
/**
 * @returns Promise that will be resolved by dispatching `event` on `target`
 * Or it will be rejected in `timeout` if it's specified
 * Optional `options` for addEventListener can be also specified
 */
function promisifyEvent(target, event, timeout, options) {
    return new Promise((resolve, reject) => {
        function eventCallback(e) {
            target.removeEventListener(event, eventCallback, options);
            resolve(e);
        }
        target.addEventListener(event, eventCallback, options);
        if (typeof timeout === 'number' && timeout >= 0) {
            setTimeout(() => reject(new Error('Rejected by timeout')), timeout);
        }
    });
}
/**
 * Short helper to make Promise from element state marker
 * @returns Promise that will be resolved if the target `marker` property is truthful or `event` is dispatched
 * @example
 * `const imgReady = promisifyMarker(eslImage, 'ready');`
 */
function promisifyMarker(target, marker, event = marker) {
    if (target[marker])
        return Promise.resolve(target);
    return promisifyEvent(target, event).then(() => target);
}
/**
 * Call `callback` limited by `tryCount` amount of times with interval in `timeout` ms
 * @returns Promise that will be resolved as soon as callback returns truthy value, or reject it by limit.
 */
function tryUntil(callback, tryCount = 2, timeout = 100) {
    return new Promise((resolve, reject) => {
        (function check() {
            let result;
            try {
                result = callback();
            }
            catch (_a) {
                result = undefined;
            }
            if (result || (tryCount--) < 0) {
                result ? resolve(result) : reject(new Error('Rejected by limit of tries'));
            }
            else {
                setTimeout(check, timeout);
            }
        })();
    });
}
/**
 * Call async callback in a sequence passed number of times
 * Initial call starts as a microtask
 * @param callback - async chain function
 * @param count - count o calls
 * @returns sequence end promise
 */
function repeatSequence(callback, count = 1) {
    if (count < 1)
        return Promise.reject();
    if (count === 1)
        return Promise.resolve().then(callback);
    return repeatSequence(callback, count - 1).then(callback);
}
/**
 * Create Deferred Object that wraps promise and its resolve and reject callbacks
 */
function createDeferred() {
    let reject;
    let resolve;
    // Both reject and resolve will be assigned anyway while the Promise constructing.
    const promise = new Promise((res, rej) => {
        resolve = res;
        reject = rej;
    });
    return { promise, resolve, reject };
}
/**
 * Safe wrap for Promise.resolve to use in Promise chain
 * @example
 * `const resolvedPromise = rejectedPromise.catch(resolvePromise);`
 */
function resolvePromise(arg) {
    return Promise.resolve(arg);
}
/**
 * Safe wrap for Promise.reject to use in Promise chain
 * @example
 * `const rejectedPromise = resolvedPromise.then(rejectPromise);`
 */
function rejectPromise(arg) {
    return Promise.reject(arg);
}
/**
 * Promise utils helper class
 * Note: use individual methods in case you need correct "tree shaking"
 */
class PromiseUtils {
}
PromiseUtils.fromTimeout = promisifyTimeout;
PromiseUtils.fromEvent = promisifyEvent;
PromiseUtils.fromMarker = promisifyMarker;
PromiseUtils.repeat = repeatSequence;
PromiseUtils.tryUntil = tryUntil;
PromiseUtils.deferred = createDeferred;
PromiseUtils.resolve = resolvePromise;
PromiseUtils.reject = rejectPromise;


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/async/raf.js":
/*!*****************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/async/raf.js ***!
  \*****************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "afterNextRender": () => (/* binding */ afterNextRender),
/* harmony export */   "rafDecorator": () => (/* binding */ rafDecorator)
/* harmony export */ });
/**
 * Postpone action after next render
 */
const afterNextRender = (callback) => requestAnimationFrame(() => requestAnimationFrame(callback));
/**
 * Decorate function to schedule execution after next render
 * @returns decorated function
 */
const rafDecorator = (fn) => {
    let lastArgs = null; // null if no calls requested
    return function (...args) {
        if (lastArgs === null) {
            requestAnimationFrame(() => {
                lastArgs && fn.call(this, ...lastArgs);
                lastArgs = null;
            });
        }
        lastArgs = args;
    };
};


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/decorators/bind.js":
/*!***********************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/decorators/bind.js ***!
  \***********************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "bind": () => (/* binding */ bind)
/* harmony export */ });
const BINDINGS_STORE_KEY = '__fnBindings__';
/** Decorator "bind" allows to bind prototype method context to class instance */
// eslint-disable-next-line @typescript-eslint/ban-types
function bind(target, propertyKey, descriptor) {
    // Validation check
    if (!descriptor || (typeof descriptor.value !== 'function')) {
        throw new TypeError('Only class methods can be decorated via @bind');
    }
    // Original function
    const fn = descriptor.value;
    return {
        enumerable: descriptor.enumerable,
        configurable: descriptor.configurable,
        get() {
            // Accessing via prototype returns original function
            // If the constructor property is in the context then it's not an instance
            if (!this || this === target || Object.hasOwnProperty.call(this, 'constructor')) {
                return fn;
            }
            // Bounded functions store
            let bindings = this[BINDINGS_STORE_KEY];
            if (!bindings) {
                bindings = this[BINDINGS_STORE_KEY] = new WeakMap();
            }
            // Store binding if it does not exist
            if (!bindings.has(fn)) {
                bindings.set(fn, fn.bind(this));
            }
            // Return binding
            return bindings.get(fn);
        },
        set(value) {
            Object.defineProperty(this, propertyKey, {
                writable: true,
                enumerable: false,
                configurable: true,
                value
            });
        }
    };
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/decorators/memoize.js":
/*!**************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/decorators/memoize.js ***!
  \**************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "memoize": () => (/* binding */ memoize)
/* harmony export */ });
/* harmony import */ var _misc_memoize__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../misc/memoize */ "./node_modules/@exadel/esl/modules/esl-utils/misc/memoize.js");
/* harmony import */ var _misc_object__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../misc/object */ "./node_modules/@exadel/esl/modules/esl-utils/misc/object.js");


/**
 * Memoization decorator helper.
 * @see memoizeFn Original memoizeFn function decorator.
 */
function memoize(hashFn = _misc_memoize__WEBPACK_IMPORTED_MODULE_0__.defaultArgsHashFn) {
    return function (target, prop, descriptor) {
        if (!descriptor || typeof (descriptor.value || descriptor.get) !== 'function') {
            throw new TypeError('Only get accessors or class methods can be decorated via @memoize');
        }
        if ((0,_misc_object__WEBPACK_IMPORTED_MODULE_1__.isPrototype)(target)) {
            // Object members
            (typeof descriptor.get === 'function') && (descriptor.get = memoizeGetter(descriptor.get, prop));
            (typeof descriptor.value === 'function') && (descriptor.value = memoizeMethod(descriptor.value, prop, hashFn));
        }
        else {
            // Static members
            (typeof descriptor.get === 'function') && (descriptor.get = (0,_misc_memoize__WEBPACK_IMPORTED_MODULE_0__.memoizeFn)(descriptor.get));
            (typeof descriptor.value === 'function') && (descriptor.value = (0,_misc_memoize__WEBPACK_IMPORTED_MODULE_0__.memoizeFn)(descriptor.value, hashFn));
        }
    };
}
// Lock storage to prevent cache logic for some key
const locks = new WeakMap();
const defineOwnKeySafe = (obj, prop, value) => {
    locks.set(obj, prop); // IE try to get key with the prototype instance call, so we lock it
    Object.defineProperty(obj, prop, { value, writable: true, configurable: true });
    locks.delete(obj); // Free property key
};
/** Cache getter result as an object own property */
function memoizeGetter(originalMethod, prop) {
    return function () {
        if (locks.get(this) === prop)
            return originalMethod;
        const value = originalMethod.call(this);
        defineOwnKeySafe(this, prop, value);
        return value;
    };
}
/** Cache method memo function in the current context on call */
function memoizeMethod(originalMethod, prop, hashFn) {
    return function (...args) {
        if (locks.get(this) === prop)
            return originalMethod;
        const memo = (0,_misc_memoize__WEBPACK_IMPORTED_MODULE_0__.memoizeFn)(originalMethod, hashFn);
        defineOwnKeySafe(this, prop, memo);
        return memo.apply(this, args);
    };
}
/**
 * Clear memoization cache for passed target and property.
 * Accepts not own properties.
 * Note: be sure that you targeting memoized property or function.
 * Clear utility has no 100% check to prevent modifying incorrect (not memoized) property keys
 */
memoize.clear = function (target, property) {
    const desc = (0,_misc_object__WEBPACK_IMPORTED_MODULE_1__.getPropertyDescriptor)(target, property);
    if (!desc)
        return;
    if (typeof desc.get === 'function' && typeof desc.get.clear === 'function')
        return desc.get.clear();
    if (typeof desc.value === 'function' && typeof desc.value.clear === 'function')
        return desc.value.clear();
    if (Object.hasOwnProperty.call(target, property))
        delete target[property];
};
/** Check if property has cache for the passed params */
memoize.has = function (target, property, ...params) {
    const desc = (0,_misc_object__WEBPACK_IMPORTED_MODULE_1__.getPropertyDescriptor)(target, property);
    if (!desc)
        return false;
    if (typeof desc.get === 'function' && typeof desc.get.has === 'function')
        return desc.get.has(...params);
    if (typeof desc.value === 'function' && typeof desc.value.has === 'function')
        return desc.value.has(...params);
    return Object.hasOwnProperty.call(target, property);
};


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/decorators/prop.js":
/*!***********************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/decorators/prop.js ***!
  \***********************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "prop": () => (/* binding */ prop)
/* harmony export */ });
/**
 * `@prop` is auxiliary decorator to define a field on the prototype level.
 *` @prop` can be used to override decorated property from the parent level
 *
 * You can also use an @override decorator in combination with ECMA Script class property definition:
 * `@prop() public field: any = initial value;`
 *
 * The class property initial value is a part of object creation, so it goes to the object itself,
 * while the @override value is defined on the prototype level.
 *
 * @param prototypeConfig - prototype property configuration
 */
function prop(prototypeConfig = {}) {
    return function (obj, name) {
        if (Object.hasOwnProperty.call(obj, name)) {
            throw new TypeError('Can\'t override own property');
        }
        Object.defineProperty(obj, name, {
            value: prototypeConfig.value,
            writable: !prototypeConfig.readonly,
            enumerable: true,
            configurable: true
        });
    };
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/decorators/ready.js":
/*!************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/decorators/ready.js ***!
  \************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ready": () => (/* binding */ ready)
/* harmony export */ });
/* harmony import */ var _dom_ready__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../dom/ready */ "./node_modules/@exadel/esl/modules/esl-utils/dom/ready.js");

/** Defer method execution to the next task with dom ready state precondition */
function ready(target, propertyKey, descriptor) {
    if (!descriptor || typeof descriptor.value !== 'function') {
        throw new TypeError('Only class methods can be decorated via document ready decorator');
    }
    const fn = descriptor.value;
    descriptor.value = function (...arg) {
        (0,_dom_ready__WEBPACK_IMPORTED_MODULE_0__.onDocumentReady)(() => fn.call(this, ...arg));
    };
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/dom/api.js":
/*!***************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/dom/api.js ***!
  \***************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "getDocument": () => (/* binding */ getDocument),
/* harmony export */   "getNodeName": () => (/* binding */ getNodeName),
/* harmony export */   "getParentNode": () => (/* binding */ getParentNode)
/* harmony export */ });
/**
 * Get the Element that is the root element of the document.
 * @param element - element for which to get the document element
 * */
const getDocument = (element = window) => {
    return (element instanceof Window ? element.document : element.ownerDocument).documentElement;
};
/**
 * Get the name of node.
 * @param element - element for which to get the name
 */
const getNodeName = (element) => {
    return element && !(element instanceof Window) ? (element.nodeName).toLowerCase() : '';
};
/**
 * Get the parent of the specified element in the DOM tree.
 * @param element - element for which to get the parent
 */
const getParentNode = (element) => {
    if (getNodeName(element) === 'html')
        return element;
    return (window.ShadowRoot
        ? element instanceof ShadowRoot
            ? element.host
            : element.assignedSlot || element.parentNode
        : element.parentNode) || getDocument(element);
};


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/dom/class.js":
/*!*****************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/dom/class.js ***!
  \*****************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "CSSClassUtils": () => (/* binding */ CSSClassUtils)
/* harmony export */ });
/* harmony import */ var _misc_array__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../misc/array */ "./node_modules/@exadel/esl/modules/esl-utils/misc/array.js");

/** Store locks for key element classes*/
const lockStore = new WeakMap();
/** Mange className lock for the element */
const lock = (el, className, locker) => {
    const elLocks = lockStore.get(el) || new Map();
    const classLocks = elLocks.get(className) || new Set();
    classLocks.add(locker);
    elLocks.set(className, classLocks);
    lockStore.set(el, elLocks);
};
/**
 * Manage className unlock for the element
 * @returns true if className have no locks
 */
const unlock = (el, className, locker) => {
    const elLocks = lockStore.get(el);
    if (!elLocks)
        return true;
    const classLocks = elLocks.get(className);
    if (!classLocks)
        return true;
    classLocks.delete(locker);
    return !classLocks.size;
};
/**
 * Add single class to the element.
 * Supports inversion and locker management.
 */
const add = (el, className, locker) => {
    if (className[0] === '!')
        return CSSClassUtils.remove(el, className.substring(1), locker);
    if (locker)
        lock(el, className, locker);
    el.classList.add(className);
};
/**
 * Remove single class from the element.
 * Supports inversion and locker management.
 */
const remove = (el, className, locker) => {
    if (className[0] === '!')
        return CSSClassUtils.add(el, className.substring(1), locker);
    if (locker && !unlock(el, className, locker))
        return;
    if (!locker)
        CSSClassUtils.unlock(el, className);
    el.classList.remove(className);
};
/**
 * CSS class manipulation utilities.
 *
 * Allows to manipulate with CSS classes with the following set of sub-features:
 * - JQuery-like enumeration - you can pass multiple tokens separated by space
 * - safe checks - empty or falsy token sting will be ignored without throwing an error
 * - inversion syntax - tokens that start from '!' will be processed with inverted action
 * (e.g. addCls(el, '!class') - will remove 'class' from the element, while removeCls(el, '!class') adds 'class' to the element)
 * - class locks - you can manipulate with classes using `locker` option that takes into account the modification initiator.
 * That means the class added in 'locker' mode will not be removed until all initiators that requested add class have requested its removal.
 * */
class CSSClassUtils {
    /** Splitting passed token string into CSS class names array. */
    static splitTokens(tokenString) {
        return (tokenString || '').split(' ').filter((str) => !!str);
    }
    /**
     * Add all classes from the class token string to the element.
     * @see CSSClassUtils
     * */
    static add(els, cls, locker) {
        const tokens = CSSClassUtils.splitTokens(cls);
        (0,_misc_array__WEBPACK_IMPORTED_MODULE_0__.wrap)(els).forEach((el) => tokens.forEach((className) => add(el, className, locker)));
    }
    /**
     * Remove all classes from the class token string to the element.
     * @see CSSClassUtils
     * */
    static remove(els, cls, locker) {
        const tokens = CSSClassUtils.splitTokens(cls);
        (0,_misc_array__WEBPACK_IMPORTED_MODULE_0__.wrap)(els).forEach((el) => tokens.forEach((className) => remove(el, className, locker)));
    }
    /**
     * Toggle all classes from the class token string on the element to the passed state.
     * @see CSSClassUtils
     * */
    static toggle(els, cls, state, locker) {
        (state ? CSSClassUtils.add : CSSClassUtils.remove)(els, cls, locker);
    }
    /** Remove all lockers for the element or passed element className */
    static unlock(els, className) {
        if (className) {
            (0,_misc_array__WEBPACK_IMPORTED_MODULE_0__.wrap)(els).forEach((el) => { var _a; return (_a = lockStore.get(el)) === null || _a === void 0 ? void 0 : _a.delete(className); });
        }
        else {
            (0,_misc_array__WEBPACK_IMPORTED_MODULE_0__.wrap)(els).forEach((el) => lockStore.delete(el));
        }
    }
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/dom/events.js":
/*!******************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/dom/events.js ***!
  \******************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "EventUtils": () => (/* binding */ EventUtils)
/* harmony export */ });
class EventUtils {
    /**
     * Dispatch custom event.
     * Event bubbles and is cancelable by default, use `eventInit` to override that.
     * @param el - element target
     * @param eventName - event name
     * @param eventInit - custom event init. See {@link CustomEventInit}
     */
    static dispatch(el, eventName, eventInit) {
        const init = Object.assign({
            bubbles: true,
            composed: true,
            cancelable: true
        }, eventInit || {});
        return el.dispatchEvent(new CustomEvent(eventName, init));
    }
    /** Get original CustomEvent source */
    static source(e) {
        const targets = (e.composedPath && e.composedPath());
        return targets ? targets[0] : e.target;
    }
    /** Check if the passed event is {@link MouseEvent} */
    static isMouseEvent(event) {
        return window.MouseEvent && event instanceof MouseEvent;
    }
    /** Check if the passed event is {@link TouchEvent} */
    static isTouchEvent(event) {
        return window.TouchEvent && event instanceof TouchEvent;
    }
    /** Normalize TouchEvent or PointerEvent */
    static normalizeTouchPoint(event) {
        const source = EventUtils.isTouchEvent(event) ? event.changedTouches[0] : event;
        return {
            x: source.pageX,
            y: source.pageY
        };
    }
    /** Normalize MouseEvent */
    static normalizeCoordinates(event, elem) {
        const source = EventUtils.isTouchEvent(event) ? event.changedTouches[0] : event;
        const props = elem.getBoundingClientRect();
        const top = props.top + window.scrollY;
        const left = props.left + window.scrollX;
        return {
            x: source.pageX - left,
            y: source.pageY - top
        };
    }
    /** Stub method to prevent event from bubbling out of target */
    static stopPropagation(e) {
        e === null || e === void 0 ? void 0 : e.stopPropagation();
    }
    /** Stub method to prevent default event behaviour */
    static preventDefault(e) {
        e === null || e === void 0 ? void 0 : e.preventDefault();
    }
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/dom/keys.js":
/*!****************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/dom/keys.js ***!
  \****************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "TAB": () => (/* binding */ TAB),
/* harmony export */   "ENTER": () => (/* binding */ ENTER),
/* harmony export */   "ESC": () => (/* binding */ ESC),
/* harmony export */   "SPACE": () => (/* binding */ SPACE),
/* harmony export */   "BACKSPACE": () => (/* binding */ BACKSPACE),
/* harmony export */   "ALT": () => (/* binding */ ALT),
/* harmony export */   "SHIFT": () => (/* binding */ SHIFT),
/* harmony export */   "CONTROL": () => (/* binding */ CONTROL),
/* harmony export */   "PAUSE": () => (/* binding */ PAUSE),
/* harmony export */   "END": () => (/* binding */ END),
/* harmony export */   "HOME": () => (/* binding */ HOME),
/* harmony export */   "DELETE": () => (/* binding */ DELETE),
/* harmony export */   "PAGE_UP": () => (/* binding */ PAGE_UP),
/* harmony export */   "PAGE_DOWN": () => (/* binding */ PAGE_DOWN),
/* harmony export */   "ARROW_LEFT": () => (/* binding */ ARROW_LEFT),
/* harmony export */   "ARROW_UP": () => (/* binding */ ARROW_UP),
/* harmony export */   "ARROW_RIGHT": () => (/* binding */ ARROW_RIGHT),
/* harmony export */   "ARROW_DOWN": () => (/* binding */ ARROW_DOWN),
/* harmony export */   "SYSTEM_KEYS": () => (/* binding */ SYSTEM_KEYS)
/* harmony export */ });
const TAB = 'Tab';
const ENTER = 'Enter';
const ESC = 'Escape';
const SPACE = ' ';
const BACKSPACE = 'Backspace';
const ALT = 'Alt';
const SHIFT = 'Shift';
const CONTROL = 'Control';
const PAUSE = 'Pause';
const END = 'End';
const HOME = 'Home';
const DELETE = 'Delete';
const PAGE_UP = 'PageUp';
const PAGE_DOWN = 'PageDown';
const ARROW_LEFT = 'ArrowLeft';
const ARROW_UP = 'ArrowUp';
const ARROW_RIGHT = 'ArrowRight';
const ARROW_DOWN = 'ArrowDown';
const SYSTEM_KEYS = [TAB, ALT, SHIFT, CONTROL, PAGE_UP, PAGE_DOWN, ARROW_LEFT, ARROW_UP, ARROW_RIGHT, ARROW_DOWN];


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/dom/ready.js":
/*!*****************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/dom/ready.js ***!
  \*****************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "onDocumentReady": () => (/* binding */ onDocumentReady)
/* harmony export */ });
/**
 * Execute callback in bounds of the next task with dom ready state precondition
 */
function onDocumentReady(callback) {
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function fn() {
            document.removeEventListener('DOMContentLoaded', fn);
            setTimeout(() => callback());
        });
    }
    else {
        setTimeout(() => callback());
    }
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/dom/rect.js":
/*!****************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/dom/rect.js ***!
  \****************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "Rect": () => (/* binding */ Rect)
/* harmony export */ });
/**
 * A Rect describes the size and position of a rectangle.
 */
class Rect {
    constructor(x, y, width, height) {
        /** The X coordinate of the Rect's origin (top-left corner of the rectangle). */
        this.x = 0;
        /** The Y coordinate of the Rect's origin (top-left corner of the rectangle). */
        this.y = 0;
        /** The width of the Rect. */
        this.width = 0;
        /** The height of the Rect. */
        this.height = 0;
        this.x = x || 0;
        this.y = y || 0;
        this.width = width || 0;
        this.height = height || 0;
    }
    /**
     * The static method creates a new Rect instance from a rect-like object.
     * @param rect - rect-like object
     */
    static from(rect = {}) {
        return new this(rect.x || rect.left, rect.y || rect.top, rect.width, rect.height);
    }
    /**
     * The static method checks the equality of the two Rect instances.
     * @param rect1 - first instance of Rect
     * @param rect2 - second instance of Rect
     */
    static isEqual(rect1, rect2) {
        return rect1.x === rect2.x && rect1.y === rect2.y && rect1.width === rect2.width && rect1.height === rect2.height;
    }
    /**
     * Get the top coordinate value of the Rect (has the same value as y).
     */
    get top() {
        return this.y;
    }
    /**
     * Get the left coordinate value of the Rect (has the same value as x).
     */
    get left() {
        return this.x;
    }
    /**
     * Get the right coordinate value of the DOMRect.
     */
    get right() {
        return this.x + this.width;
    }
    /**
     * Get the bottom coordinate value of the Rect.
     */
    get bottom() {
        return this.y + this.height;
    }
    /**
     * Get the center X coordinate value of the Rect.
     */
    get cx() {
        return this.x + this.width / 2;
    }
    /**
     * Get the center Y coordinate value of the Rect.
     */
    get cy() {
        return this.y + this.height / 2;
    }
    /**
     * Grow the Rect by the specified increment in pixels.
     * It increases the size of the Rect by moving each point on the edge of the Rect to a certain distance further away from the center of the Rect.
     * @param increment - distance to grow in pixels
     */
    grow(increment) {
        this.y -= increment;
        this.x -= increment;
        this.height += 2 * increment;
        this.width += 2 * increment;
        return this;
    }
    /**
     * Shrink the Rect by the specified decrement in pixels.
     * It reduces the size of the Rect by moving each point on the edge of the Rect to a certain distance closer to the center of the Rect.
     * @param decrement - distance to shrink in pixels
     */
    shrink(decrement) {
        return this.grow(-decrement);
    }
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/dom/rtl.js":
/*!***************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/dom/rtl.js ***!
  \***************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "RTLUtils": () => (/* binding */ RTLUtils)
/* harmony export */ });
/* harmony import */ var _decorators_memoize__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../decorators/memoize */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/memoize.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};

// TODO: functional
class RTLUtils {
    /** Check if the element in a RTL direction context */
    static isRtl(el = document.body) {
        return getComputedStyle(el).direction === 'rtl';
    }
    /**
     * @returns RTL scroll type (lazy, memoized)
     */
    static get scrollType() {
        let scrollType = 'default';
        const el = createDummyEl();
        document.body.appendChild(el);
        if (el.scrollLeft <= 0) {
            el.scrollLeft = 2;
            scrollType = el.scrollLeft < 2 ? 'negative' : 'reverse';
        }
        document.body.removeChild(el);
        return scrollType;
    }
    static normalizeScrollLeft(el, value = null, isRtl = RTLUtils.isRtl(el)) {
        value = (value === null) ? el.scrollLeft : value;
        switch (isRtl ? RTLUtils.scrollType : '') {
            case 'negative':
                return el.scrollWidth - el.clientWidth + value;
            case 'reverse':
                return el.scrollWidth - el.clientWidth - value;
            default:
                return value;
        }
    }
}
__decorate([
    (0,_decorators_memoize__WEBPACK_IMPORTED_MODULE_0__.memoize)()
], RTLUtils, "scrollType", null);
/** Creates the dummy test element with a horizontal scroll presented */
function createDummyEl() {
    const el = document.createElement('div');
    el.appendChild(document.createTextNode('ESL!'));
    el.dir = 'rtl';
    Object.assign(el.style, {
        position: 'absolute',
        top: '-1000px',
        width: '4px',
        height: '1px',
        fontSize: '14px',
        overflow: 'scroll'
    });
    return el;
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/dom/scroll.js":
/*!******************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/dom/scroll.js ***!
  \******************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ScrollUtils": () => (/* binding */ ScrollUtils),
/* harmony export */   "getListScrollParents": () => (/* binding */ getListScrollParents),
/* harmony export */   "getScrollParent": () => (/* binding */ getScrollParent),
/* harmony export */   "isScrollParent": () => (/* binding */ isScrollParent),
/* harmony export */   "scrollIntoView": () => (/* binding */ scrollIntoView)
/* harmony export */ });
/* harmony import */ var _async_promise__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../async/promise */ "./node_modules/@exadel/esl/modules/esl-utils/async/promise.js");
/* harmony import */ var _api__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./api */ "./node_modules/@exadel/esl/modules/esl-utils/dom/api.js");


const $html = document.documentElement;
const initiatorSet = new Set();
// TODO: functional
class ScrollUtils {
    /** Check vertical scroll based on content height */
    static hasVerticalScroll(target = $html) {
        return target.scrollHeight > target.clientHeight;
    }
    /**
     * Disable scroll on the page.
     * @param strategy - to make scroll visually disabled
     * */
    static lock(strategy) {
        const hasScroll = ScrollUtils.hasVerticalScroll();
        if (strategy && strategy !== 'none' && hasScroll) {
            $html.classList.add(`esl-${strategy}-scroll`);
        }
        $html.classList.add('esl-disable-scroll');
    }
    /**
     * Enable scroll on the page.
     * */
    static unlock() {
        $html.classList.remove('esl-disable-scroll', 'esl-pseudo-scroll', 'esl-native-scroll');
    }
    /**
     * Disable scroll on the page.
     * @param initiator - object to associate request with
     * @param strategy - to make scroll visually disabled
     *
     * TODO: currently requests with different strategy is not taken into account
     * */
    static requestLock(initiator, strategy) {
        initiator && initiatorSet.add(initiator);
        (initiatorSet.size > 0) && ScrollUtils.lock(strategy);
    }
    /**
     * Enable scroll on the page in case it was requested with given initiator.
     * @param initiator - object to associate request with
     * @param strategy - to make scroll visually disabled
     */
    static requestUnlock(initiator, strategy) {
        initiator && initiatorSet.delete(initiator);
        (initiatorSet.size === 0) && ScrollUtils.unlock();
    }
}
/**
 * Get the list of all scroll parents, up the list of ancestors until we get to the top window object.
 * @param element - element for which you want to get the list of all scroll parents
 * @param list - array of elements to concatenate with the list of all scroll parents of element (optional)
 */
function getListScrollParents(element, list = []) {
    var _a;
    const scrollParent = getScrollParent(element);
    const isBody = scrollParent === ((_a = element.ownerDocument) === null || _a === void 0 ? void 0 : _a.body);
    const target = isBody
        ? isScrollParent(scrollParent) ? scrollParent : []
        : scrollParent;
    const updatedList = list.concat(target);
    return isBody
        ? updatedList
        : updatedList.concat(getListScrollParents((0,_api__WEBPACK_IMPORTED_MODULE_0__.getParentNode)(scrollParent)));
}
/**
 * Get the scroll parent of the specified element in the DOM tree.
 * @param node - element for which to get the scroll parent
 */
function getScrollParent(node) {
    var _a;
    if (['html', 'body', '#document'].indexOf((0,_api__WEBPACK_IMPORTED_MODULE_0__.getNodeName)(node)) >= 0) {
        return (_a = node.ownerDocument) === null || _a === void 0 ? void 0 : _a.body;
    }
    if (node instanceof HTMLElement && isScrollParent(node)) {
        return node;
    }
    return getScrollParent((0,_api__WEBPACK_IMPORTED_MODULE_0__.getParentNode)(node));
}
/**
 * Check that element is scroll parent.
 * @param element - element for checking
 * */
function isScrollParent(element) {
    // Firefox wants us to check `-x` and `-y` variations as well
    const { overflow, overflowX, overflowY } = getComputedStyle(element);
    return /auto|scroll|overlay|hidden/.test(overflow + overflowY + overflowX);
}
/**
 * This is a promise-based version of scrollIntoView().
 * Method scrolls the element's parent container such that the element on which
 * scrollIntoView() is called is visible to the user. The promise is resolved when
 * the element became visible to the user and scrolling stops.
 *
 * Note: Please, use the native element.scrollIntoView() if you don't need a promise
 * to detect the moment when the scroll is finished or you don't use smooth behavior.
 * @param element - element to be made visible to the user
 * @param options - scrollIntoView options
 */
function scrollIntoView(element, options) {
    let same = 0;
    let lastLeft;
    let lastTop;
    const check = () => {
        const { top, left } = element.getBoundingClientRect();
        if (top !== lastTop || left !== lastLeft) {
            same = 0;
            lastTop = top;
            lastLeft = left;
        }
        return same++ > 2;
    };
    element.scrollIntoView(options);
    return (0,_async_promise__WEBPACK_IMPORTED_MODULE_1__.tryUntil)(check, 333, 30); // will check top position every 30ms, but not more than 250 times (10s)
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/dom/traversing.js":
/*!**********************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/dom/traversing.js ***!
  \**********************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "isRelativeNode": () => (/* binding */ isRelativeNode),
/* harmony export */   "createSequenceFinder": () => (/* binding */ createSequenceFinder),
/* harmony export */   "findNext": () => (/* binding */ findNext),
/* harmony export */   "findPrev": () => (/* binding */ findPrev),
/* harmony export */   "findParent": () => (/* binding */ findParent),
/* harmony export */   "findAll": () => (/* binding */ findAll),
/* harmony export */   "findChildren": () => (/* binding */ findChildren),
/* harmony export */   "findClosestBy": () => (/* binding */ findClosestBy),
/* harmony export */   "TraversingUtils": () => (/* binding */ TraversingUtils)
/* harmony export */ });
/** Check that `nodeA` and `nodeB` are from the same tree path */
const isRelativeNode = (nodeA, nodeB) => {
    return !!(nodeA && nodeB) && (nodeA.contains(nodeB) || nodeB.contains(nodeA));
};
/** Create function that finds next dom element, that matches selector, in the sequence declared by `next` function */
const createSequenceFinder = (next) => {
    return function (base, sel) {
        for (let target = next(base); target; target = next(target)) {
            if (!sel || target.matches(sel))
                return target;
        }
        return null;
    };
};
/** @returns first matching next sibling or null*/
const findNext = createSequenceFinder((el) => el.nextElementSibling);
/** @returns first matching previous sibling or null*/
const findPrev = createSequenceFinder((el) => el.previousElementSibling);
/** @returns first matching parent or null*/
const findParent = createSequenceFinder((el) => el.parentElement);
/** @returns Array of all matching elements in subtree or empty array */
const findAll = (base, sel) => {
    return sel ? Array.from(base.querySelectorAll(sel)) : [base];
};
/** @returns Array of all matching children or empty array */
const findChildren = (base, sel) => {
    return Array.from(base.children).filter((el) => !sel || el.matches(sel));
};
/**
 * Find closest parent node of `node` by `predicate`.
 * Optional `skipSelf` to skip initial node
 */
const findClosestBy = (node, predicate, skipSelf = false) => {
    let current = skipSelf && node ? node.parentNode : node;
    while (current) {
        if (predicate(current))
            return current;
        current = current.parentNode;
    }
    return null;
};
/** @deprecated Cumulative traversing utility set */
class TraversingUtils {
}
TraversingUtils.isRelative = isRelativeNode;
TraversingUtils.closestBy = findClosestBy;
TraversingUtils.createSequenceFinder = createSequenceFinder;
TraversingUtils.findNext = findNext;
TraversingUtils.findPrev = findPrev;
TraversingUtils.findParent = findParent;
TraversingUtils.findAll = findAll;
TraversingUtils.findChildren = findChildren;


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/dom/window.js":
/*!******************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/dom/window.js ***!
  \******************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "getWindow": () => (/* binding */ getWindow),
/* harmony export */   "getWindowRect": () => (/* binding */ getWindowRect)
/* harmony export */ });
/* harmony import */ var _rect__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./rect */ "./node_modules/@exadel/esl/modules/esl-utils/dom/rect.js");

/**
 * Get the window object associated with a document of the specified element.
 * @param node - element for which to get window
 * */
function getWindow(node) {
    if (node === null)
        return window;
    if (node instanceof Window)
        return node;
    const ownerDocument = node.ownerDocument;
    return ownerDocument ? ownerDocument.defaultView || window : window;
}
/**
 * Get the size and position of the window.
 * @returns
 */
function getWindowRect(wnd = window) {
    return _rect__WEBPACK_IMPORTED_MODULE_0__.Rect.from({
        x: wnd.scrollX,
        y: wnd.scrollY,
        width: wnd.innerWidth || wnd.document.documentElement.clientWidth,
        height: wnd.innerHeight || wnd.document.documentElement.clientHeight
    });
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/environment/device-detector.js":
/*!***********************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/environment/device-detector.js ***!
  \***********************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "isTrident": () => (/* binding */ isTrident),
/* harmony export */   "isIE": () => (/* binding */ isIE),
/* harmony export */   "isEdgeHTML": () => (/* binding */ isEdgeHTML),
/* harmony export */   "isBlinkEdge": () => (/* binding */ isBlinkEdge),
/* harmony export */   "isEdge": () => (/* binding */ isEdge),
/* harmony export */   "isGecko": () => (/* binding */ isGecko),
/* harmony export */   "isFirefox": () => (/* binding */ isFirefox),
/* harmony export */   "isOpera": () => (/* binding */ isOpera),
/* harmony export */   "isChrome": () => (/* binding */ isChrome),
/* harmony export */   "isWebkit": () => (/* binding */ isWebkit),
/* harmony export */   "isSafari": () => (/* binding */ isSafari),
/* harmony export */   "isBlink": () => (/* binding */ isBlink),
/* harmony export */   "isBot": () => (/* binding */ isBot),
/* harmony export */   "isAndroid": () => (/* binding */ isAndroid),
/* harmony export */   "isMobileIOS13": () => (/* binding */ isMobileIOS13),
/* harmony export */   "isMobileIOS": () => (/* binding */ isMobileIOS),
/* harmony export */   "isLegacyMobile": () => (/* binding */ isLegacyMobile),
/* harmony export */   "isMobile": () => (/* binding */ isMobile),
/* harmony export */   "isMobileSafari": () => (/* binding */ isMobileSafari),
/* harmony export */   "isTouchDevice": () => (/* binding */ isTouchDevice),
/* harmony export */   "hasHover": () => (/* binding */ hasHover),
/* harmony export */   "TOUCH_EVENTS": () => (/* binding */ TOUCH_EVENTS),
/* harmony export */   "DeviceDetector": () => (/* binding */ DeviceDetector)
/* harmony export */ });
/* harmony import */ var _export_ns__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./export-ns */ "./node_modules/@exadel/esl/modules/esl-utils/environment/export-ns.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};

const { userAgent, vendor, platform } = window.navigator;
// IE Detection
const isTrident = /trident/i.test(userAgent);
const isIE = isTrident;
// Edge Detection
const isEdgeHTML = /edg([ea]|ios)/i.test(userAgent);
const isBlinkEdge = /\sedg\//i.test(userAgent);
const isEdge = isEdgeHTML || isBlinkEdge;
// Gecko
const isGecko = /gecko/i.test(userAgent) && !/like gecko/i.test(userAgent);
const isFirefox = /firefox|iceweasel|fxios/i.test(userAgent);
// Opera / Chrome
const isOpera = /(?:^opera.+?version|opr)/.test(userAgent);
const isChrome = !isOpera && /google inc/.test(vendor);
// Webkit
const isWebkit = /(apple)?webkit/i.test(userAgent);
// Safari
const isSafari = isWebkit && /^((?!chrome|android).)*safari/i.test(userAgent);
// Blink
const isBlink = isWebkit && !isSafari;
// Bot detection
const isBot = /Chrome-Lighthouse|Google Page Speed Insights/i.test(userAgent);
// Mobile
const isAndroid = /Android/i.test(userAgent);
const isMobileIOS13 = /* iOS 13+ detection */ (platform === 'MacIntel' && window.navigator.maxTouchPoints > 1);
const isMobileIOS = /iPad|iPhone|iPod/.test(platform) || isMobileIOS13;
const isLegacyMobile = /webOS|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
const isMobile = isMobileIOS || isAndroid || isLegacyMobile;
const isMobileSafari = isMobileIOS && isWebkit && /CriOS/i.test(userAgent);
// Touch Detection
const isTouchDevice = (() => {
    const navApi = window.navigator;
    if (navApi.maxTouchPoints || navApi.msMaxTouchPoints)
        return true;
    return ('ontouchstart' in window) || ('DocumentTouch' in window && document instanceof Touch);
})();
// Hover check
// Note: always true for IE
const hasHover = !matchMedia('(hover: none)').matches;
/** @deprecated TODO: needs to be reworked or moved away from device detector */
const TOUCH_EVENTS = (() => {
    const isTouch = isTouchDevice;
    return {
        START: isTouch ? 'touchstart' : 'pointerdown',
        MOVE: isTouch ? 'touchmove' : 'pointermove',
        END: isTouch ? 'touchend' : 'pointerup'
    };
})();
/**
 * Device detection utility
 * @readonly
 */
let DeviceDetector = class DeviceDetector {
};
// IE Detection
DeviceDetector.isTrident = isTrident;
DeviceDetector.isIE = isIE;
// Edge Detection
DeviceDetector.isEdgeHTML = isEdgeHTML;
DeviceDetector.isBlinkEdge = isBlinkEdge;
DeviceDetector.isEdge = isEdge;
// Gecko
DeviceDetector.isGecko = isGecko;
DeviceDetector.isFirefox = isFirefox;
// Opera / Chrome
DeviceDetector.isOpera = isOpera;
DeviceDetector.isChrome = isChrome;
// Webkit
DeviceDetector.isWebkit = isWebkit;
// Safari
DeviceDetector.isSafari = isSafari;
// Blink
DeviceDetector.isBlink = isBlink;
// Bot detection
DeviceDetector.isBot = isBot;
// Mobile
DeviceDetector.isAndroid = isAndroid;
DeviceDetector.isMobileIOS13 = isMobileIOS13;
DeviceDetector.isMobileIOS = isMobileIOS;
DeviceDetector.isLegacyMobile = isLegacyMobile;
DeviceDetector.isMobile = isMobile;
DeviceDetector.isMobileSafari = isMobileSafari;
// Touch Detection
DeviceDetector.isTouchDevice = isTouchDevice;
// Hover check
// Note: always true for IE
DeviceDetector.hasHover = hasHover;
/** @deprecated TODO: needs to be reworked or moved away from device detector */
DeviceDetector.TOUCH_EVENTS = TOUCH_EVENTS;
DeviceDetector = __decorate([
    (0,_export_ns__WEBPACK_IMPORTED_MODULE_0__.ExportNs)('DeviceDetector')
], DeviceDetector);



/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/environment/export-ns.js":
/*!*****************************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/environment/export-ns.js ***!
  \*****************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "exportNs": () => (/* binding */ exportNs),
/* harmony export */   "ExportNs": () => (/* binding */ ExportNs)
/* harmony export */ });
const NS_NAME = 'ESL';
/**
 * Nested declaration helper
 */
function define(root, name, value) {
    name.split('.').reduce((obj, key, index, parts) => {
        if (parts.length === index + 1) {
            return (obj[key] = obj[key] || value);
        }
        const type = typeof obj[key];
        if (type !== 'undefined' && type !== 'object' && type !== 'function') {
            throw new Error(`Can not define ${value} on ${name}`);
        }
        return (obj[key] = obj[key] || {});
    }, root);
}
/**
 * Method to manually declare key in library namespace
 * See {@link ExportNs} decorator for details
 */
const exportNs = (name, module) => {
    if (!(NS_NAME in window))
        return;
    define(window[NS_NAME], name, module);
};
/**
 * Decorator to declare function or class in a global ns
 * @param name - key path to declare in ESL global ns
 * NOTE: path parts should be separated by dots
 * @example
 * ```ts
 * @Export('Package.Component')
 * ```
 * NOTE: in case declaration contains components-packages, their origins will be mixed with declaration in a Runtime
 */
// eslint-disable-next-line @typescript-eslint/ban-types
function ExportNs(name) {
    return (module) => exportNs(name || module.name, module);
}
/** Declare ESL global */
ExportNs.declare = () => {
    if ('ESL' in window)
        return;
    Object.defineProperty(window, 'ESL', { value: {} });
};


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/fixes/ie-fixes.js":
/*!**********************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/fixes/ie-fixes.js ***!
  \**********************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "createZIndexIframe": () => (/* binding */ createZIndexIframe)
/* harmony export */ });
/** Fix IE browser to allow to display alert under iframe */
function createZIndexIframe() {
    const iframe = document.createElement('iframe');
    iframe.className = 'ie-zindex-fix';
    iframe.src = 'about:blank';
    return iframe;
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/misc/array.js":
/*!******************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/misc/array.js ***!
  \******************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "tuple": () => (/* binding */ tuple),
/* harmony export */   "flat": () => (/* binding */ flat),
/* harmony export */   "wrap": () => (/* binding */ wrap),
/* harmony export */   "unwrap": () => (/* binding */ unwrap),
/* harmony export */   "uniq": () => (/* binding */ uniq),
/* harmony export */   "range": () => (/* binding */ range),
/* harmony export */   "groupBy": () => (/* binding */ groupBy)
/* harmony export */ });
/* harmony import */ var _functions__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./functions */ "./node_modules/@exadel/esl/modules/esl-utils/misc/functions.js");
/* harmony import */ var _object__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./object */ "./node_modules/@exadel/esl/modules/esl-utils/misc/object.js");


/** Split array into tuples */
const tuple = (arr) => arr.reduce((acc, el) => {
    if (acc.length === 0 || acc[acc.length - 1].length >= 2)
        acc.push([]);
    acc[acc.length - 1].push(el);
    return acc;
}, []);
/** Flat array - unwraps one level of nested arrays */
const flat = (arr) => arr.reduce((acc, el) => el ? acc.concat(el) : acc, []);
/** Wraps passed object or primitive to array */
const wrap = (arr) => {
    if (arr === undefined || arr === null)
        return [];
    if (Array.isArray(arr))
        return arr;
    return [arr];
};
function unwrap(value) {
    return (0,_object__WEBPACK_IMPORTED_MODULE_0__.isArrayLike)(value) ? value[0] : value;
}
/** Makes array values unique */
const uniq = (arr) => {
    const result = [];
    const set = new Set();
    arr.forEach((item) => set.add(item));
    set.forEach((item) => result.push(item));
    return result;
};
function range(n, filler = _functions__WEBPACK_IMPORTED_MODULE_1__.identity) {
    const arr = Array(n);
    let i = 0;
    while (i < n)
        arr[i] = filler(i++);
    return arr;
}
/**
 * @returns object with a criteria value as a key and an array of original items that belongs to the current criteria value
 */
const groupBy = (array, group) => {
    return array.reduce((obj, el) => {
        const key = group(el);
        obj[key] ? obj[key].push(el) : obj[key] = [el];
        return obj;
    }, {});
};


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/misc/format.js":
/*!*******************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/misc/format.js ***!
  \*******************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "toKebabCase": () => (/* binding */ toKebabCase),
/* harmony export */   "toCamelCase": () => (/* binding */ toCamelCase),
/* harmony export */   "capitalize": () => (/* binding */ capitalize),
/* harmony export */   "unwrapParenthesis": () => (/* binding */ unwrapParenthesis),
/* harmony export */   "parseNumber": () => (/* binding */ parseNumber),
/* harmony export */   "parseAspectRatio": () => (/* binding */ parseAspectRatio),
/* harmony export */   "evaluate": () => (/* binding */ evaluate),
/* harmony export */   "DEF_FORMAT_MATCHER": () => (/* binding */ DEF_FORMAT_MATCHER),
/* harmony export */   "format": () => (/* binding */ format)
/* harmony export */ });
/* harmony import */ var _object__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./object */ "./node_modules/@exadel/esl/modules/esl-utils/misc/object.js");

/** Converts string to kebab-case notation */
const toKebabCase = (str) => {
    return str.replace(/([a-z])([A-Z])/g, '$1-$2').replace(/[\s_]+/g, '-').toLowerCase();
};
/** Converts string to camelCase notation */
const toCamelCase = (str) => {
    return str.trim().replace(/[\s-,_]+([a-zA-Z0-9]?)/g, (match, word) => word.toUpperCase());
};
/** Makes the first non-indent (space, tab, newline) letter in the string capitalized */
const capitalize = (str) => {
    let i = 0;
    while (i < str.length && (str[i] === ' ' || str[i] === '\t' || str[i] === '\n'))
        i++;
    return str.slice(0, i) + str.charAt(i).toUpperCase() + str.slice(i + 1);
};
/** Unwraps string from parenthesis */
const unwrapParenthesis = (str) => {
    return str.trim().replace(/^\((.*)\)$/, '$1').trim();
};
/**
 * Parses number with the ability to pass an alternative fallback for NaN.
 * Note: falsy values except 0 are treated as NaN
 */
const parseNumber = (str, nanValue) => {
    if (str === 0)
        return 0;
    const value = +(str || NaN);
    return isNaN(value) ? nanValue : value;
};
/**
 * Common function that returns coefficient aspect ratio
 * Supported formats: w:h, w/h, coefficient
 * @example
 * `16:9`, `16/9`, `1.77`
 * @param str - string to parse
 * @returns aspect ratio coefficient
 */
function parseAspectRatio(str) {
    const [w, h] = str.split(/[:/]/);
    if (typeof h !== 'undefined')
        return +w / +h;
    return +w || 0;
}
/** Evaluates passed string or returns `defaultValue` */
function evaluate(str, defaultValue) {
    try {
        // eslint-disable-next-line @typescript-eslint/no-implied-eval
        return str ? (new Function(`return ${str}`))() : defaultValue;
    }
    catch (e) {
        console.warn('Cannot parse value ', str, e);
        return defaultValue;
    }
}
/** Default RegExp to match replacements in the string for the {@link format} function */
const DEF_FORMAT_MATCHER = /{[{%]?([\w.]+)[%}]?}/g;
/** Replaces `{key}` patterns in the string from the source object */
function format(str, source, matcher = DEF_FORMAT_MATCHER) {
    return str.replace(matcher, (match, key) => {
        const val = (0,_object__WEBPACK_IMPORTED_MODULE_0__.get)(source, key);
        return val === undefined ? match : val;
    });
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/misc/functions.js":
/*!**********************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/misc/functions.js ***!
  \**********************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "noop": () => (/* binding */ noop),
/* harmony export */   "identity": () => (/* binding */ identity)
/* harmony export */ });
/** Function that does nothing */
const noop = () => undefined;
/** Function that returns the first argument */
const identity = (arg) => arg;


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/misc/memoize.js":
/*!********************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/misc/memoize.js ***!
  \********************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "memoizeFn": () => (/* binding */ memoizeFn),
/* harmony export */   "defaultArgsHashFn": () => (/* binding */ defaultArgsHashFn)
/* harmony export */ });
/**
 * Memoization decorator function. Caches the original function result according to hash generated from arguments.
 * In case the hash function returns `undefined` value will not be memoized.
 * @see MemoHashFn Hash function signature.
 */
function memoizeFn(fn, hashFn = defaultArgsHashFn) {
    function memo(...args) {
        const key = hashFn(...args);
        if (key !== null && typeof key !== 'string') {
            console.warn(`Can't cache value for ${fn.name} call.`);
            return fn.apply(this, args);
        }
        if (!memo.cache.has(key)) {
            memo.cache.set(key, fn.apply(this, args));
        }
        return memo.cache.get(key);
    }
    memo.cache = new Map();
    memo.clear = () => memo.cache.clear();
    memo.has = (...args) => {
        const key = hashFn(...args);
        return key === undefined ? false : memo.cache.has(key);
    };
    return memo;
}
/**
 * Default arguments hash function.
 * Supports only 0-1 arguments with a primitive type.
 */
function defaultArgsHashFn(...args) {
    if (args.length === 0)
        return null;
    if (args.length > 1)
        return;
    if (typeof args[0] !== 'string' && typeof args[0] !== 'number' && typeof args[0] !== 'boolean')
        return;
    return String(args[0]);
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/misc/object.js":
/*!*******************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/misc/object.js ***!
  \*******************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "isObject": () => (/* binding */ isObject),
/* harmony export */   "isObjectLike": () => (/* binding */ isObjectLike),
/* harmony export */   "isPrimitive": () => (/* binding */ isPrimitive),
/* harmony export */   "isPrototype": () => (/* binding */ isPrototype),
/* harmony export */   "isArrayLike": () => (/* binding */ isArrayLike),
/* harmony export */   "deepCompare": () => (/* binding */ deepCompare),
/* harmony export */   "getPropertyDescriptor": () => (/* binding */ getPropertyDescriptor),
/* harmony export */   "defined": () => (/* binding */ defined),
/* harmony export */   "copy": () => (/* binding */ copy),
/* harmony export */   "copyDefinedKeys": () => (/* binding */ copyDefinedKeys),
/* harmony export */   "omit": () => (/* binding */ omit),
/* harmony export */   "set": () => (/* binding */ set),
/* harmony export */   "get": () => (/* binding */ get),
/* harmony export */   "deepMerge": () => (/* binding */ deepMerge)
/* harmony export */ });
/** Checks that passed value is object, but not a callable-object (function) */
const isObject = (obj) => !!obj && typeof obj === 'object';
/** Checks that passed value is an object or function */
const isObjectLike = (obj) => isObject(obj) || typeof obj === 'function';
/** Checks if the passed value is primitive */
const isPrimitive = (obj) => obj === null ||
    typeof obj === 'undefined' ||
    typeof obj === 'string' ||
    typeof obj === 'number' ||
    typeof obj === 'boolean' ||
    typeof obj === 'symbol';
/** Checks that passed object is prototype of some class */
const isPrototype = (obj) => Object.hasOwnProperty.call(obj, 'constructor');
/** Checks that passed object is array-like */
const isArrayLike = (value) => {
    if (!value || typeof value !== 'object')
        return false;
    if (Array.isArray(value))
        return true;
    if (typeof value.length !== 'number' || value.length < 0)
        return false;
    return !value.length || Object.hasOwnProperty.call(value, value.length - 1);
};
/** Deep object compare */
function deepCompare(obj1, obj2) {
    if (Object.is(obj1, obj2))
        return true;
    if (typeof obj1 !== typeof obj2)
        return false;
    if (isObject(obj1) && isObject(obj2)) {
        const keys1 = Object.keys(obj1);
        const keys2 = Object.keys(obj2);
        if (keys1.length !== keys2.length)
            return false;
        return !keys1.some((key) => !deepCompare(obj1[key], obj2[key]));
    }
    return false;
}
/** Find the closest property descriptor */
function getPropertyDescriptor(o, prop) {
    let proto = o;
    while (proto) {
        const desc = Object.getOwnPropertyDescriptor(proto, prop);
        if (desc)
            return desc;
        proto = Object.getPrototypeOf(proto);
    }
}
/** @returns first defined param */
function defined(...params) {
    for (const param of params) {
        if (param !== undefined)
            return param;
    }
}
/** Makes a plain copy of obj with properties satisfying the predicate
 * If no predicate provided copies all own properties */
function copy(obj, predicate = () => true) {
    const result = Object.assign({}, obj || {});
    Object.keys(result).forEach((key) => {
        (!predicate(key, result[key])) && delete result[key];
    });
    return result;
}
/** Makes a flat copy without undefined keys */
function copyDefinedKeys(obj) {
    return copy(obj || {}, (key, value) => value !== void 0);
}
/** Omit copying provided properties from object */
function omit(obj, keys) {
    return copy(obj, key => !keys.includes(key));
}
/**
 * Set object property using "path" key
 *
 * @param target - object
 * @param path - key path, use '.' as delimiter
 * @param value - value of property
 */
const set = (target, path, value) => {
    const parts = (path || '').split('.');
    const depth = parts.length - 1;
    parts.reduce((cur, key, index) => {
        if (index === depth)
            return cur[key] = value;
        return cur[key] = isObjectLike(cur[key]) ? cur[key] : {};
    }, target);
};
/**
 * Gets object property using "path" key
 * Creates empty object if sub-key value is not presented.
 *
 * @param data - object
 * @param path - key path, use '.' as delimiter
 * @param defaultValue - default
 */
const get = (data, path, defaultValue) => {
    const parts = (path || '').split('.');
    const result = parts.reduce((curr, key) => {
        if (isObjectLike(curr))
            return curr[key];
        return undefined;
    }, data);
    return typeof result === 'undefined' ? defaultValue : result;
};
function deepMerge(...objects) {
    return objects.reduce((res, obj, index) => {
        if (index === 0 && Array.isArray(obj))
            res = [];
        isObject(obj) && Object.keys(obj).forEach((key) => {
            const resultVal = res[key];
            const objectVal = obj[key];
            let mergeResult = objectVal;
            if (isObject(objectVal)) {
                if (typeof resultVal === 'undefined')
                    mergeResult = deepMerge(objectVal);
                else if (isObject(resultVal))
                    mergeResult = deepMerge(resultVal, objectVal);
            }
            res[key] = mergeResult;
        });
        return res;
    }, {});
}


/***/ }),

/***/ "./node_modules/@exadel/esl/modules/esl-utils/misc/uid.js":
/*!****************************************************************!*\
  !*** ./node_modules/@exadel/esl/modules/esl-utils/misc/uid.js ***!
  \****************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "sequentialUID": () => (/* binding */ sequentialUID),
/* harmony export */   "randUID": () => (/* binding */ randUID),
/* harmony export */   "generateUId": () => (/* binding */ generateUId)
/* harmony export */ });
const ns = window || __webpack_require__.g;
const sequences = ns.__esl_sequences__ || new Map();
ns.__esl_sequences__ = sequences;
/** Create and return sequential id */
const sequentialUID = (name, prefix = name) => {
    const uid = (sequences.get(name) || 0) + 1;
    sequences.set(name, uid);
    return prefix + uid;
};
/** Return random unique identifier */
const randUID = (prefix = '') => {
    const time = Date.now().toString(32);
    const rand = Math.round(Math.random() * 1024 * 1024).toString(32);
    return prefix + time + '-' + rand;
};
/**
 * Generate unique id
 * @deprecated Alias for {@link randUID}
 */
const generateUId = randUID;


/***/ }),

/***/ "./src/navigation/navigation.ts":
/*!**************************************!*\
  !*** ./src/navigation/navigation.ts ***!
  \**************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ESLDemoSidebar": () => (/* reexport safe */ _sidebar_sidebar__WEBPACK_IMPORTED_MODULE_0__.ESLDemoSidebar)
/* harmony export */ });
/* harmony import */ var _sidebar_sidebar__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./sidebar/sidebar */ "./src/navigation/sidebar/sidebar.ts");



/***/ }),

/***/ "./src/navigation/sidebar/sidebar.ts":
/*!*******************************************!*\
  !*** ./src/navigation/sidebar/sidebar.ts ***!
  \*******************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "ESLDemoSidebar": () => (/* binding */ ESLDemoSidebar)
/* harmony export */ });
/* harmony import */ var _exadel_esl__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @exadel/esl */ "./node_modules/@exadel/esl/modules/esl-toggleable/core/esl-toggleable.js");
/* harmony import */ var _exadel_esl__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @exadel/esl */ "./node_modules/@exadel/esl/modules/esl-media-query/core/esl-media-query.js");
/* harmony import */ var _exadel_esl__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @exadel/esl */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/prop.js");
/* harmony import */ var _exadel_esl__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @exadel/esl */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/ready.js");
/* harmony import */ var _exadel_esl__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @exadel/esl */ "./node_modules/@exadel/esl/modules/esl-utils/decorators/bind.js");
/* harmony import */ var _exadel_esl_modules_esl_base_element_core__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @exadel/esl/modules/esl-base-element/core */ "./node_modules/@exadel/esl/modules/esl-base-element/decorators/attr.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};


class ESLDemoSidebar extends _exadel_esl__WEBPACK_IMPORTED_MODULE_0__.ESLToggleable {
    constructor() {
        super(...arguments);
        this.closeOnEsc = true;
        this.closeOnOutsideAction = true;
        this.submenus = '.sidebar-nav-secondary';
        this.activeMenuAttr = 'data-open';
    }
    get $submenus() {
        return Array.from(this.querySelectorAll(this.submenus));
    }
    connectedCallback() {
        super.connectedCallback();
        _exadel_esl__WEBPACK_IMPORTED_MODULE_1__.ESLMediaQuery["for"]('@+MD').addListener(this.onBreakpointChange);
    }
    disconnectedCallback() {
        super.disconnectedCallback();
        _exadel_esl__WEBPACK_IMPORTED_MODULE_1__.ESLMediaQuery["for"]('@+MD').removeListener(this.onBreakpointChange);
    }
    storeState() {
        this.open ? localStorage.removeItem('sidebar-collapsed') : localStorage.setItem('sidebar-collapsed', 'true');
    }
    setInitialState() {
        const isDesktop = _exadel_esl__WEBPACK_IMPORTED_MODULE_1__.ESLMediaQuery["for"]('@+MD').matches;
        const isStoredOpen = !localStorage.getItem('sidebar-collapsed');
        this.toggle(isDesktop && isStoredOpen, { force: true, initiator: 'init', immediate: true });
    }
    collapseAll() {
        this.$submenus.forEach((menu) => menu.hide({ activator: this }));
    }
    expandActive(noCollapse = false) {
        this.$submenus
            .filter((menu) => menu.hasAttribute('data-open'))
            .forEach((menu) => menu.show({ noCollapse, activator: this }));
    }
    updateA11y() {
        const targetEl = this.$a11yTarget;
        if (!targetEl)
            return;
        targetEl.setAttribute('aria-expanded', String(this.open));
    }
    onShow(params) {
        this._animation = !params.immediate;
        super.onShow(params);
        this.expandActive(params.initiator === 'init');
        if (params.activator && params.activator.hasAttribute('data-store')) {
            this.storeState();
        }
    }
    onHide(params) {
        this._animation = !params.immediate;
        super.onHide(params);
        this.collapseAll();
        if (params.activator && params.activator.hasAttribute('data-store')) {
            this.storeState();
        }
    }
    onBreakpointChange() {
        const isDesktop = _exadel_esl__WEBPACK_IMPORTED_MODULE_1__.ESLMediaQuery["for"]('@+MD').matches;
        const isStoredOpen = !localStorage.getItem('sidebar-collapsed');
        this.toggle(isDesktop && isStoredOpen, { force: true, initiator: 'bpchange', immediate: !isDesktop });
    }
    _onOutsideAction(e) {
        if (_exadel_esl__WEBPACK_IMPORTED_MODULE_1__.ESLMediaQuery["for"]('@+MD').matches)
            return;
        super._onOutsideAction(e);
    }
}
ESLDemoSidebar.is = 'esl-d-sidebar';
__decorate([
    (0,_exadel_esl__WEBPACK_IMPORTED_MODULE_2__.prop)()
], ESLDemoSidebar.prototype, "closeOnEsc", void 0);
__decorate([
    (0,_exadel_esl__WEBPACK_IMPORTED_MODULE_2__.prop)()
], ESLDemoSidebar.prototype, "closeOnOutsideAction", void 0);
__decorate([
    (0,_exadel_esl__WEBPACK_IMPORTED_MODULE_2__.prop)()
], ESLDemoSidebar.prototype, "submenus", void 0);
__decorate([
    (0,_exadel_esl__WEBPACK_IMPORTED_MODULE_2__.prop)()
], ESLDemoSidebar.prototype, "activeMenuAttr", void 0);
__decorate([
    (0,_exadel_esl_modules_esl_base_element_core__WEBPACK_IMPORTED_MODULE_3__.attr)({ name: 'animation' })
], ESLDemoSidebar.prototype, "_animation", void 0);
__decorate([
    _exadel_esl__WEBPACK_IMPORTED_MODULE_4__.ready
], ESLDemoSidebar.prototype, "connectedCallback", null);
__decorate([
    _exadel_esl__WEBPACK_IMPORTED_MODULE_5__.bind
], ESLDemoSidebar.prototype, "onBreakpointChange", null);
__decorate([
    _exadel_esl__WEBPACK_IMPORTED_MODULE_5__.bind
], ESLDemoSidebar.prototype, "_onOutsideAction", null);


/***/ })

/******/ 	});
/************************************************************************/
/******/ 	// The module cache
/******/ 	var __webpack_module_cache__ = {};
/******/ 	
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/ 		// Check if module is in cache
/******/ 		var cachedModule = __webpack_module_cache__[moduleId];
/******/ 		if (cachedModule !== undefined) {
/******/ 			return cachedModule.exports;
/******/ 		}
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = __webpack_module_cache__[moduleId] = {
/******/ 			// no module.id needed
/******/ 			// no module.loaded needed
/******/ 			exports: {}
/******/ 		};
/******/ 	
/******/ 		// Execute the module function
/******/ 		__webpack_modules__[moduleId](module, module.exports, __webpack_require__);
/******/ 	
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/ 	
/************************************************************************/
/******/ 	/* webpack/runtime/define property getters */
/******/ 	(() => {
/******/ 		// define getter functions for harmony exports
/******/ 		__webpack_require__.d = (exports, definition) => {
/******/ 			for(var key in definition) {
/******/ 				if(__webpack_require__.o(definition, key) && !__webpack_require__.o(exports, key)) {
/******/ 					Object.defineProperty(exports, key, { enumerable: true, get: definition[key] });
/******/ 				}
/******/ 			}
/******/ 		};
/******/ 	})();
/******/ 	
/******/ 	/* webpack/runtime/global */
/******/ 	(() => {
/******/ 		__webpack_require__.g = (function() {
/******/ 			if (typeof globalThis === 'object') return globalThis;
/******/ 			try {
/******/ 				return this || new Function('return this')();
/******/ 			} catch (e) {
/******/ 				if (typeof window === 'object') return window;
/******/ 			}
/******/ 		})();
/******/ 	})();
/******/ 	
/******/ 	/* webpack/runtime/hasOwnProperty shorthand */
/******/ 	(() => {
/******/ 		__webpack_require__.o = (obj, prop) => (Object.prototype.hasOwnProperty.call(obj, prop))
/******/ 	})();
/******/ 	
/******/ 	/* webpack/runtime/make namespace object */
/******/ 	(() => {
/******/ 		// define __esModule on exports
/******/ 		__webpack_require__.r = (exports) => {
/******/ 			if(typeof Symbol !== 'undefined' && Symbol.toStringTag) {
/******/ 				Object.defineProperty(exports, Symbol.toStringTag, { value: 'Module' });
/******/ 			}
/******/ 			Object.defineProperty(exports, '__esModule', { value: true });
/******/ 		};
/******/ 	})();
/******/ 	
/************************************************************************/
var __webpack_exports__ = {};
// This entry need to be wrapped in an IIFE because it need to be isolated against other modules in the chunk.
(() => {
/*!*************************!*\
  !*** ./src/localdev.js ***!
  \*************************/
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _exadel_esl__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @exadel/esl */ "./node_modules/@exadel/esl/modules/esl-tab/core/esl-tabs.js");
/* harmony import */ var _exadel_esl__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @exadel/esl */ "./node_modules/@exadel/esl/modules/esl-tab/core/esl-tab.js");
/* harmony import */ var _exadel_esl__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @exadel/esl */ "./node_modules/@exadel/esl/modules/esl-toggleable/core/esl-toggleable-dispatcher.js");
/* harmony import */ var _exadel_esl__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @exadel/esl */ "./node_modules/@exadel/esl/modules/esl-popup/core/esl-popup.js");
/* harmony import */ var _exadel_esl__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @exadel/esl */ "./node_modules/@exadel/esl/modules/esl-panel-group/core/esl-panel-group.js");
/* harmony import */ var _exadel_esl__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! @exadel/esl */ "./node_modules/@exadel/esl/modules/esl-panel/core/esl-panel.js");
/* harmony import */ var _exadel_esl__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! @exadel/esl */ "./node_modules/@exadel/esl/modules/esl-trigger/core/esl-trigger.js");
/* harmony import */ var _exadel_esl__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! @exadel/esl */ "./node_modules/@exadel/esl/modules/esl-scrollbar/core/esl-scrollbar.js");
/* harmony import */ var _exadel_esl__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! @exadel/esl */ "./node_modules/@exadel/esl/modules/esl-alert/core/esl-alert.js");
/* harmony import */ var _navigation_navigation__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./navigation/navigation */ "./src/navigation/navigation.ts");



_navigation_navigation__WEBPACK_IMPORTED_MODULE_0__.ESLDemoSidebar.register();

_exadel_esl__WEBPACK_IMPORTED_MODULE_1__.ESLTabs.register();
_exadel_esl__WEBPACK_IMPORTED_MODULE_2__.ESLTab.register();

_exadel_esl__WEBPACK_IMPORTED_MODULE_3__.ESLToggleableDispatcher.init();
_exadel_esl__WEBPACK_IMPORTED_MODULE_4__.ESLPopup.register();

_exadel_esl__WEBPACK_IMPORTED_MODULE_5__.ESLPanelGroup.register();
_exadel_esl__WEBPACK_IMPORTED_MODULE_6__.ESLPanel.register();

_exadel_esl__WEBPACK_IMPORTED_MODULE_7__.ESLTrigger.register();

_exadel_esl__WEBPACK_IMPORTED_MODULE_8__.ESLScrollbar.register();

_exadel_esl__WEBPACK_IMPORTED_MODULE_9__.ESLAlert.init({
  closeOnOutsideAction: true
});

})();

/******/ })()
;
//# sourceMappingURL=localdev.js.map