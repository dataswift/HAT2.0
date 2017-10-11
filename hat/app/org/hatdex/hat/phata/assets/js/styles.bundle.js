webpackJsonp(["styles"],{

/***/ "../../../../../src/styles.scss":
/***/ (function(module, exports, __webpack_require__) {

// style-loader: Adds some css to the DOM by adding a <style> tag

// load the styles
var content = __webpack_require__("../../../../css-loader/index.js?{\"sourceMap\":false,\"importLoaders\":1}!../../../../postcss-loader/index.js?{\"ident\":\"postcss\"}!../../../../sass-loader/lib/loader.js?{\"sourceMap\":false,\"precision\":8,\"includePaths\":[]}!../../../../../src/styles.scss");
if(typeof content === 'string') content = [[module.i, content, '']];
// add the styles to the DOM
var update = __webpack_require__("../../../../style-loader/addStyles.js")(content, {});
if(content.locals) module.exports = content.locals;
// Hot Module Replacement
if(false) {
	// When the styles change, update the <style> tags
	if(!content.locals) {
		module.hot.accept("!!../node_modules/css-loader/index.js??ref--9-1!../node_modules/postcss-loader/index.js??postcss!../node_modules/sass-loader/lib/loader.js??ref--9-3!./styles.scss", function() {
			var newContent = require("!!../node_modules/css-loader/index.js??ref--9-1!../node_modules/postcss-loader/index.js??postcss!../node_modules/sass-loader/lib/loader.js??ref--9-3!./styles.scss");
			if(typeof newContent === 'string') newContent = [[module.id, newContent, '']];
			update(newContent);
		});
	}
	// When the module is disposed, remove the <style> tags
	module.hot.dispose(function() { update(); });
}

/***/ }),

/***/ "../../../../css-loader/index.js?{\"sourceMap\":false,\"importLoaders\":1}!../../../../postcss-loader/index.js?{\"ident\":\"postcss\"}!../../../../sass-loader/lib/loader.js?{\"sourceMap\":false,\"precision\":8,\"includePaths\":[]}!../../../../../src/styles.scss":
/***/ (function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__("../../../../css-loader/lib/css-base.js")(false);
// imports


// module
exports.push([module.i, "/*\n * Copyright (C) 2016 HAT Data Exchange Ltd - All Rights Reserved\n * This Source Code Form is subject to the terms of the Mozilla Public\n * License, v. 2.0. If a copy of the MPL was not distributed with this\n * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n * Written by Augustinas Markevicius <augustinas.markevicius@hatdex.org> 2016\n */\n/* Global styles */\n/*\n * Copyright (C) 2017 HAT Data Exchange Ltd - All Rights Reserved\n * This Source Code Form is subject to the terms of the Mozilla Public\n * License, v. 2.0. If a copy of the MPL was not distributed with this\n * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n * Written by Augustinas Markevicius <augustinas.markevicius@hatdex.org> 2, 2017\n */\n/*!\n * Copyright (C) 2017 HAT Data Exchange Ltd - All Rights Reserved\n * This Source Code Form is subject to the terms of the Mozilla Public\n * License, v. 2.0. If a copy of the MPL was not distributed with this\n * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n * Written by Augustinas Markevicius <augustinas.markevicius@hatdex.org> 4, 2017\n */\n/*!\n * Copyright (C) 2017 HAT Data Exchange Ltd - All Rights Reserved\n * This Source Code Form is subject to the terms of the Mozilla Public\n * License, v. 2.0. If a copy of the MPL was not distributed with this\n * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n * Written by Augustinas Markevicius <augustinas.markevicius@hatdex.org> 4, 2017\n */\nbody {\n  font-size: 12px;\n  font-family: \"Roboto\", sans-serif;\n  min-width: 320px;\n  background-color: #EEEEEE; }\n\nh1, h2, h3, h4, h5, h6, div, p, a, ul, ol, li, blockquote, span .tile-header,\n.section-title {\n  font-family: \"Roboto\", sans-serif; }\n\ni {\n  -webkit-user-select: none;\n     -moz-user-select: none;\n      -ms-user-select: none;\n          user-select: none; }\n\n@-webkit-keyframes showNotifs {\n  from {\n    margin-top: -100px; }\n  to {\n    margin-top: 0px; } }\n\n@keyframes showNotifs {\n  from {\n    margin-top: -100px; }\n  to {\n    margin-top: 0px; } }\n\n.notifications-wrapper {\n  background-color: #6297b1;\n  height: 100px;\n  width: 100%;\n  top: -100px; }\n\n.content-main-authenticated {\n  margin-left: 345px;\n  top: 106px;\n  position: relative; }\n  @media screen and (max-width: 1113px) {\n    .content-main-authenticated {\n      margin-left: 0px; } }\n  .content-main-authenticated .content-grid {\n    overflow: hidden;\n    color: white;\n    padding-left: 0;\n    padding-right: 0; }\n  .content-main-authenticated .grid {\n    width: 100%;\n    height: auto;\n    margin-right: 0;\n    margin-left: 0; }\n\n.content-main-not-authenticated {\n  margin-left: 0px !important; }\n\n.responsive {\n  position: relative;\n  float: left;\n  width: 100%;\n  min-height: 1px;\n  padding-left: 15px;\n  padding-right: 15px;\n  position: relative;\n  min-height: 1px;\n  padding-left: 15px;\n  padding-right: 15px;\n  position: relative;\n  min-height: 1px;\n  padding-left: 15px;\n  padding-right: 15px; }\n  @media (min-width: 768px) {\n    .responsive {\n      float: left;\n      width: 83.33333333%; } }\n  @media (min-width: 768px) {\n    .responsive {\n      margin-left: 8.33333333%; } }\n  @media (min-width: 992px) {\n    .responsive {\n      float: left;\n      width: 50%; } }\n  @media (min-width: 992px) {\n    .responsive {\n      margin-left: 25%; } }\n\n.login-form {\n  text-align: center;\n  font-family: \"Roboto\", sans-serif;\n  font-weight: 300; }\n  .login-form .logo {\n    height: 6em;\n    margin-top: 2em;\n    margin-right: auto;\n    margin-left: auto; }\n  .login-form p {\n    color: white;\n    font-size: 1.8em;\n    margin-top: 1.5em; }\n    .login-form p.subtext {\n      font-size: 1.4em; }\n  .login-form .highlight {\n    font-weight: 600; }\n\nbutton.btn-view-header {\n  background-color: transparent;\n  margin: 0;\n  padding: 0;\n  border: 2px solid white;\n  outline: none;\n  float: left; }\n  button.btn-view-header label {\n    font-weight: normal;\n    margin-bottom: 0;\n    cursor: pointer;\n    padding: 0.75em 2em; }\n  button.btn-view-header.empty {\n    padding: 0.75em 2em;\n    color: #4a556b; }\n  button.btn-view-header:hover {\n    background-color: rgba(255, 255, 255, 0.3); }\n  button.btn-view-header.selected {\n    background-color: white;\n    color: #37474F;\n    border: 2px solid #CFD8DC; }\n\n.view-header {\n  min-height: 5em;\n  background-color: #37474F;\n  color: #EEEEEE;\n  padding: 1em;\n  font-size: 1em; }\n  .view-header input[type=\"radio\"],\n  .view-header input[type=\"checkbox\"] {\n    display: none; }\n  .view-header h2 {\n    text-transform: uppercase;\n    display: inline-block;\n    font-size: 1.75em;\n    margin: 0.4em 0; }\n  .view-header .inline-btn-group {\n    float: right; }\n  .view-header .view-controls {\n    margin-left: 3em; }\n\n.timeline {\n  background-color: rgba(0, 0, 0, 0.7);\n  position: absolute;\n  height: 73em;\n  top: 0;\n  right: 0;\n  z-index: 5550;\n  overflow: auto; }\n\n.btn {\n  font-family: \"Roboto\", sans-serif;\n  border-radius: 0;\n  box-shadow: none !important; }\n  .btn:active, .btn:focus, .btn:hover {\n    box-shadow: none !important; }\n\n.btn-rump {\n  background-color: #6297b1;\n  border: 2px solid #6297b1;\n  color: white; }\n  .btn-rump:active, .btn-rump:focus, .btn-rump:hover {\n    background-color: #73a2b9;\n    border: 2px solid #73a2b9;\n    color: white;\n    outline: none; }\n  .btn-rump.sm {\n    font-size: 1em;\n    margin: 0.75em 0;\n    padding: 0.5em 2em; }\n  .btn-rump.md {\n    font-size: 1.5em;\n    margin: 1em 0;\n    padding: 0.75em 4em; }\n  .btn-rump.fading {\n    transition-duration: 0.25s;\n    transition-property: color, background-color; }\n\n.btn-cancel {\n  background-color: transparent;\n  border: 2px solid #4a556b;\n  color: #4a556b; }\n  .btn-cancel.sm {\n    font-size: 1em;\n    margin: 0.75em 0;\n    padding: 0.5em 2em; }\n  .btn-cancel.md {\n    font-size: 1.5em;\n    margin: 1em 0;\n    padding: 0.75em 4em; }\n\n.btn-white {\n  float: right;\n  border: 1px solid white; }\n  .btn-white.sm {\n    padding: 0.5em 0.7em;\n    margin: 0 0.5em; }\n\n.input-rump {\n  border-radius: 0;\n  border: 1px solid #c7c7c7;\n  outline: none;\n  transition: all 0.30s ease-in-out; }\n  .input-rump::-webkit-input-placeholder, .input-rump::-moz-placeholder, .input-rump:-ms-input-placeholder, .input-rump:-moz-placeholder {\n    font-weight: normal; }\n  .input-rump:focus {\n    box-shadow: 0 0 5px #6297b1; }\n  .input-rump.lg {\n    font-size: 1.6em;\n    height: 50px; }\n\n.rump-lg {\n  font-size: 1.5em;\n  height: 3em; }\n\n.rump-md {\n  font-size: 1.25em;\n  height: 3em; }\n\n.rump-sm {\n  font-size: 0.75em;\n  height: 1.5em; }\n\n.bulletless-list {\n  list-style: none;\n  margin: 0;\n  padding: 0; }\n\n.icon-text-valign {\n  display: inline-block; }\n  .icon-text-valign span {\n    vertical-align: middle; }\n\n.badge {\n  background-color: #F0C400;\n  color: #4a556b;\n  float: right;\n  margin-top: 4px;\n  font-weight: normal;\n  min-width: 24px; }\n\n.notif-badge {\n  min-width: 24px;\n  min-height: 24px;\n  padding: 3px;\n  position: relative;\n  top: -50px;\n  right: 0px;\n  border: 3px solid #4a556b;\n  border-radius: 12px; }\n\n.popover {\n  z-index: 9000; }\n\n.popover-content {\n  color: #4a556b;\n  z-index: 9999; }\n\n.content-bg {\n  background-color: #FAFAFA; }\n  @media screen and (max-width: 768px) {\n    .content-bg {\n      background-color: unset; } }\n\n.content-header {\n  background-color: #DEE9EE;\n  color: #6297b1;\n  border-top-right-radius: 8px;\n  border-top-left-radius: 8px; }\n  @media screen and (max-width: 768px) {\n    .content-header {\n      border-bottom-right-radius: 8px;\n      border-bottom-left-radius: 8px; } }\n  .content-header .col-md-12 {\n    padding: 0; }\n\n.view-header {\n  background-color: unset; }\n  .view-header h4 {\n    color: #6297b1;\n    text-transform: none;\n    font-family: \"Roboto\", sans-serif;\n    display: inline-block;\n    margin-left: 5px; }\n  .view-header .info-button {\n    color: #6297b1;\n    margin-left: 10px;\n    top: 6px;\n    position: relative;\n    margin-right: 5px; }\n    .view-header .info-button:focus {\n      outline: none; }\n    .view-header .info-button:hover {\n      color: #6297b1; }\n\n.switch-label {\n  color: #6297b1;\n  text-transform: none;\n  font-family: \"Roboto\", sans-serif; }\n\n.no-scroll {\n  overflow-y: hidden;\n  position: fixed;\n  width: 100%;\n  height: 100%; }\n\nrump-offer-modal {\n  position: relative;\n  z-index: 2000; }\n\nrump-confirm-box, rump-info-box, rump-dialog-box {\n  position: relative;\n  z-index: 3000; }\n\n.popover-content {\n  font-size: 12px; }\n\n/* General header styles */\n/*\n * Copyright (C) 2017 HAT Data Exchange Ltd - All Rights Reserved\n * This Source Code Form is subject to the terms of the Mozilla Public\n * License, v. 2.0. If a copy of the MPL was not distributed with this\n * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n * Written by Augustinas Markevicius <augustinas.markevicius@hatdex.org> 2, 2017\n */\n.navbar {\n  background-color: #4a556b;\n  margin: 0;\n  border-radius: 0;\n  margin-bottom: 42px;\n  width: 100%;\n  height: 64px;\n  position: fixed;\n  top: 0px;\n  left: 0px;\n  z-index: 1020; }\n  .navbar .navbar-nav {\n    font-size: 1.2em;\n    font-family: \"Roboto\", sans-serif;\n    padding: 13px; }\n    .navbar .navbar-nav li {\n      float: left; }\n    .navbar .navbar-nav li > a {\n      margin: 0;\n      padding: 0.3em 0.5em;\n      color: white; }\n    .navbar .navbar-nav .user-nav > p {\n      margin: 0;\n      color: white;\n      line-height: 20px; }\n    .navbar .navbar-nav.navbar-right {\n      padding-left: 0.5em;\n      padding-right: 0.5em;\n      float: right;\n      margin: 0px -15px 0px 0px; }\n      .navbar .navbar-nav.navbar-right li a:hover {\n        background-color: transparent; }\n  .navbar .navbar-header .navbar-brand {\n    color: white;\n    font-size: 4em;\n    height: 1em; }\n  .navbar .navbar-header img {\n    height: 1.25em; }\n  .navbar .navbar-header a {\n    padding: 0;\n    padding-left: 1em; }\n\n.notif button {\n  background-color: transparent;\n  border: none; }\n  .notif button .new-notifications {\n    height: 1.5em;\n    width: 1.5em;\n    position: absolute;\n    font-weight: bold;\n    font-size: 1em;\n    background-color: red;\n    top: 0.25em;\n    right: 2em;\n    border-radius: 50%; }\n  .notif button:focus {\n    outline: 0; }\n\n.help {\n  border-right: solid 1px rgba(135, 135, 135, 0.5);\n  font-size: 12px;\n  margin-top: 0px;\n  margin-bottom: 7px;\n  padding-top: 3px;\n  padding-bottom: 3px;\n  padding-right: 14px;\n  cursor: pointer; }\n  .help i {\n    vertical-align: bottom;\n    margin-right: 8px; }\n  @media screen and (max-width: 510px) {\n    .help {\n      margin-right: 0px;\n      padding-right: 0px; } }\n  .help span {\n    margin-bottom: 2px;\n    display: inline-block; }\n    @media screen and (max-width: 510px) {\n      .help span {\n        display: none; } }\n\n.account {\n  margin-top: 3px;\n  margin-right: 20px; }\n  @media screen and (max-width: 510px) {\n    .account {\n      margin-top: 6px; } }\n  .account .user-photo {\n    display: inline-block;\n    padding-left: 14px;\n    width: 62px;\n    cursor: pointer; }\n    .account .user-photo img {\n      width: 36px;\n      height: 36px;\n      margin-top: -20px;\n      margin-right: 5px;\n      border-radius: 50%; }\n    .account .user-photo .notlogged {\n      margin-top: -13px; }\n  .account h6 {\n    color: white;\n    font-family: \"Roboto\", sans-serif;\n    font-weight: 400;\n    display: inline-block;\n    font-size: 14px;\n    letter-spacing: 0.5px;\n    margin-top: 0; }\n    .account h6 span {\n      font-weight: 200;\n      font-size: 11px;\n      color: #EEEEEE;\n      letter-spacing: 0.5px;\n      opacity: 0.7; }\n  .account .account-options {\n    color: white;\n    display: inline-block; }\n  .account .account-actions {\n    display: inline-block;\n    cursor: pointer; }\n    .account .account-actions .welcome {\n      min-width: 100px; }\n      @media screen and (max-width: 510px) {\n        .account .account-actions .welcome {\n          display: none; } }\n\n.notifications-wrapper {\n  position: fixed;\n  z-index: 1000; }\n\n.account-btn {\n  background-color: unset;\n  text-transform: none;\n  text-align: left;\n  padding-top: 0;\n  padding-left: 0;\n  padding-right: 0;\n  box-shadow: none; }\n  .account-btn:active, .account-btn:focus {\n    box-shadow: none; }\n\n.btn-group .dropdown-menu {\n  top: 0px;\n  width: 280px !important;\n  background-color: white !important;\n  position: absolute !important;\n  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.175) !important;\n  padding: 0;\n  right: 0;\n  left: auto;\n  opacity: 0;\n  transition: opacity 0.5s, top 0.5s;\n  display: block;\n  visibility: hidden;\n  max-height: 0px; }\n  .btn-group .dropdown-menu span {\n    font-size: 12px; }\n  .btn-group .dropdown-menu .dropdown-item {\n    cursor: pointer;\n    padding: 10px 25px;\n    display: -webkit-box;\n    display: -ms-flexbox;\n    display: flex;\n    -webkit-box-pack: justify;\n        -ms-flex-pack: justify;\n            justify-content: space-between;\n    -webkit-box-align: center;\n        -ms-flex-align: center;\n            align-items: center;\n    color: #9a9a9a;\n    opacity: 0.9;\n    font-family: \"Roboto\", sans-serif;\n    font-size: 12px;\n    letter-spacing: 0.4px;\n    height: 43px; }\n    .btn-group .dropdown-menu .dropdown-item:first-of-type {\n      margin-top: 10px; }\n    .btn-group .dropdown-menu .dropdown-item:last-of-type {\n      height: 53px; }\n    .btn-group .dropdown-menu .dropdown-item span i {\n      position: relative;\n      top: 2px;\n      color: rgba(135, 135, 135, 0.3); }\n    .btn-group .dropdown-menu .dropdown-item p {\n      margin-bottom: 0; }\n    .btn-group .dropdown-menu .dropdown-item:hover {\n      color: #6297b1;\n      text-decoration: none; }\n  .btn-group .dropdown-menu .accountDetails {\n    display: -webkit-box;\n    display: -ms-flexbox;\n    display: flex;\n    -webkit-box-align: center;\n        -ms-flex-align: center;\n            align-items: center;\n    padding: 28px 25px 18px;\n    width: 100%;\n    cursor: auto; }\n    .btn-group .dropdown-menu .accountDetails h6 {\n      color: #4a556b;\n      margin-bottom: 0;\n      display: block;\n      font-size: 16px;\n      letter-spacing: 0.7px; }\n      .btn-group .dropdown-menu .accountDetails h6 span {\n        color: #4a556b;\n        font-size: 10px;\n        letter-spacing: 0.7px;\n        font-weight: 400; }\n    .btn-group .dropdown-menu .accountDetails img {\n      width: 70px;\n      height: 70px;\n      margin-top: 0;\n      margin-right: 15px; }\n  .btn-group .dropdown-menu .accountUsage {\n    padding: 0 25px;\n    cursor: auto; }\n    .btn-group .dropdown-menu .accountUsage .bar-container {\n      height: 5px;\n      border-radius: 5px;\n      background-color: #DEE9EE;\n      display: block;\n      width: 100%;\n      padding: 0; }\n      .btn-group .dropdown-menu .accountUsage .bar-container .bar {\n        background-color: #6297b1;\n        display: block;\n        height: 100%;\n        border-radius: 5px;\n        width: 50%; }\n    .btn-group .dropdown-menu .accountUsage h6 {\n      color: #9a9a9a;\n      font-size: 10px;\n      letter-spacing: 0;\n      font-family: \"Roboto\", sans-serif;\n      margin-bottom: 25px;\n      opacity: 0.9;\n      font-weight: 200; }\n  .btn-group .dropdown-menu .dropdown-divider {\n    margin: 0px;\n    border-top: 1px solid #878787;\n    opacity: 0.1; }\n    .btn-group .dropdown-menu .dropdown-divider:last-of-type {\n      margin-top: 10px; }\n\n.btn-group.open .dropdown-menu {\n  opacity: 1;\n  top: 45px;\n  visibility: visible;\n  max-height: 800px; }\n\n/* General side-menu styles */\n/*\n * Copyright (C) 2017 HAT Data Exchange Ltd - All Rights Reserved\n * This Source Code Form is subject to the terms of the Mozilla Public\n * License, v. 2.0. If a copy of the MPL was not distributed with this\n * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n * Written by Augustinas Markevicius <augustinas.markevicius@hatdex.org> 2, 2017\n */\n/*!\n * Copyright (C) 2017 HAT Data Exchange Ltd - All Rights Reserved\n * This Source Code Form is subject to the terms of the Mozilla Public\n * License, v. 2.0. If a copy of the MPL was not distributed with this\n * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n * Written by Augustinas Markevicius <augustinas.markevicius@hatdex.org> 4, 2017\n */\n/*!\n * Copyright (C) 2017 HAT Data Exchange Ltd - All Rights Reserved\n * This Source Code Form is subject to the terms of the Mozilla Public\n * License, v. 2.0. If a copy of the MPL was not distributed with this\n * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n * Written by Augustinas Markevicius <augustinas.markevicius@hatdex.org> 4, 2017\n */\n.menubar-left {\n  width: 345px;\n  height: 100%;\n  position: fixed;\n  top: 106px;\n  left: 0px;\n  z-index: 1020;\n  color: #939393;\n  font-family: \"Roboto\", sans-serif;\n  background-color: #EEEEEE; }\n  @media screen and (max-width: 1113px) {\n    .menubar-left {\n      left: -345px; } }\n  .menubar-left .menu-scroll {\n    overflow-x: hidden;\n    overflow-y: auto;\n    height: calc(100% - 200px); }\n\n.burger {\n  color: white;\n  position: fixed;\n  top: 10px;\n  left: 35px;\n  z-index: 1030;\n  display: block;\n  width: 44px;\n  height: 44px;\n  padding: 10px;\n  cursor: pointer;\n  border-radius: 50%;\n  background-color: rgba(255, 255, 255, 0); }\n\n@-webkit-keyframes burger-pulse {\n  from {\n    background-color: rgba(255, 255, 255, 0.3); }\n  to {\n    background-color: rgba(255, 255, 255, 0); } }\n\n@keyframes burger-pulse {\n  from {\n    background-color: rgba(255, 255, 255, 0.3); }\n  to {\n    background-color: rgba(255, 255, 255, 0); } }\n\n.burger-pulse-animation {\n  -webkit-animation-name: burger-pulse;\n          animation-name: burger-pulse;\n  -webkit-animation-duration: 1s;\n          animation-duration: 1s; }\n\n.data-plugs-title {\n  text-transform: uppercase;\n  font-weight: lighter;\n  font-family: \"Roboto\", sans-serif;\n  font-size: 12px;\n  color: #bdbdbd;\n  margin: 35px 0px 10px 45px; }\n\nul.center {\n  list-style: none;\n  padding: 0;\n  font-family: \"Roboto\", sans-serif; }\n  ul.center li {\n    position: relative;\n    width: 100%;\n    height: 50px;\n    font-size: 1.2em; }\n    ul.center li .icon, ul.center li .icon img {\n      font-size: 1.6em;\n      display: inline-block;\n      width: 24px;\n      height: 24px;\n      vertical-align: bottom;\n      margin-right: 20px;\n      position: relative;\n      top: 1px; }\n    ul.center li .icon img {\n      vertical-align: baseline; }\n    ul.center li a {\n      text-decoration: none;\n      display: block;\n      color: #939393;\n      padding: 13px 15px 13px 45px;\n      letter-spacing: 0.5px;\n      cursor: pointer; }\n      ul.center li a:hover {\n        color: #939393; }\n    ul.center li.selected {\n      background-color: #E8E8E8; }\n      ul.center li.selected a {\n        color: #6297b1; }\n    ul.center li:hover {\n      background-color: #E8E8E8; }\n    ul.center li.data-plugs-list a {\n      text-transform: capitalize;\n      cursor: pointer; }\n      ul.center li.data-plugs-list a .plus-icon {\n        color: #ddd;\n        float: right; }\n      ul.center li.data-plugs-list a .icon-grey {\n        display: inline-block; }\n      ul.center li.data-plugs-list a .icon-color {\n        display: none; }\n      ul.center li.data-plugs-list a:hover .plus-icon {\n        color: #939393; }\n      ul.center li.data-plugs-list a:hover .icon-grey {\n        display: none; }\n      ul.center li.data-plugs-list a:hover .icon-color {\n        display: inline-block; }\n  ul.center .disable {\n    position: absolute;\n    text-transform: uppercase;\n    top: 0;\n    bottom: 0;\n    right: 0;\n    left: 0;\n    background-color: rgba(0, 0, 0, 0.7); }\n    ul.center .disable .text {\n      margin-top: 2em; }\n\n.data-plugs-more em {\n  color: #bbb; }\n\n.poweredBy {\n  position: absolute;\n  left: 45px;\n  bottom: 100px;\n  border-top: 1px solid #e4e4e4;\n  width: 300px;\n  padding-top: 22px;\n  padding-bottom: 22px;\n  display: block;\n  background-color: #EEEEEE; }\n  .poweredBy p {\n    display: inline-block;\n    letter-spacing: 0.5px; }\n  .poweredBy img {\n    width: 24px;\n    height: 24px;\n    display: inline-block;\n    margin-right: 20px; }\n\n.plug-menu {\n  overflow-x: hidden;\n  overflow-y: auto;\n  max-height: 200px; }\n\n.badge {\n  padding: 2px 7px 4px; }\n\n.client-logo {\n  background-image: url(\"/assets/icons/rumpel.svg\");\n  background-repeat: no-repeat;\n  background-size: auto 60px;\n  border-bottom: 1px solid #e4e4e4;\n  margin: 20px 0px 30px 45px;\n  height: 90px;\n  display: none; }\n\n/* General footer styles */\n/*\n * Copyright (C) 2017 HAT Data Exchange Ltd - All Rights Reserved\n * This Source Code Form is subject to the terms of the Mozilla Public\n * License, v. 2.0. If a copy of the MPL was not distributed with this\n * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n * Written by Augustinas Markevicius <augustinas.markevicius@hatdex.org> 2, 2017\n */\n.footer {\n  background-color: #000000;\n  color: #868686;\n  min-height: 3em;\n  clear: both;\n  padding: 3em 1em 2em 7em; }\n\n.list-unstyled {\n  padding-left: 0;\n  list-style: none; }\n\n.list-inline, .copyrights ul {\n  padding-left: 0;\n  list-style: none;\n  margin-left: -5px; }\n  .list-inline > li, .copyrights ul > li {\n    display: inline-block;\n    padding-left: 5px;\n    padding-right: 5px; }\n\nh4 {\n  text-transform: uppercase;\n  font-size: 1.5em;\n  color: white; }\n\nul {\n  list-style: none;\n  font-family: 'Open Sans Regular', sans-serif;\n  font-size: 1em; }\n\na {\n  text-decoration: none;\n  color: #868686; }\n  a:hover {\n    color: white; }\n\n.copyrights {\n  padding-top: 2em; }\n  .copyrights ul li {\n    border-left: solid 1px #868686; }\n    .copyrights ul li:first-child {\n      border-left: none; }\n\n/* Styling of modals */\n/*\n * Copyright (C) 2017 HAT Data Exchange Ltd - All Rights Reserved\n * This Source Code Form is subject to the terms of the Mozilla Public\n * License, v. 2.0. If a copy of the MPL was not distributed with this\n * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n * Written by Augustinas Markevicius <augustinas.markevicius@hatdex.org> 2, 2017\n */\n/*!\n * Copyright (C) 2017 HAT Data Exchange Ltd - All Rights Reserved\n * This Source Code Form is subject to the terms of the Mozilla Public\n * License, v. 2.0. If a copy of the MPL was not distributed with this\n * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n * Written by Augustinas Markevicius <augustinas.markevicius@hatdex.org> 4, 2017\n */\n/*!\n * Copyright (C) 2017 HAT Data Exchange Ltd - All Rights Reserved\n * This Source Code Form is subject to the terms of the Mozilla Public\n * License, v. 2.0. If a copy of the MPL was not distributed with this\n * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n * Written by Augustinas Markevicius <augustinas.markevicius@hatdex.org> 4, 2017\n */\n.modal-overlay {\n  z-index: 1500;\n  background-color: rgba(0, 0, 0, 0.8);\n  position: fixed;\n  top: 0;\n  left: 0;\n  width: 100%;\n  height: 100%;\n  cursor: pointer;\n  opacity: 0;\n  transition: all 0.5s; }\n  .modal-overlay.fadein {\n    opacity: 1; }\n\n.rump-modal {\n  position: fixed;\n  top: 50%;\n  left: 50%;\n  -webkit-transform: translate(-50%, -50%);\n          transform: translate(-50%, -50%);\n  border-radius: 10px;\n  width: 600px;\n  max-width: 90%;\n  height: auto;\n  max-height: 90%;\n  background-color: #EEEEEE;\n  z-index: 1510;\n  opacity: 0;\n  transition: all 0.5s; }\n  .rump-modal.animateIn {\n    -webkit-transform: translate(-50%, -50%);\n            transform: translate(-50%, -50%);\n    opacity: 1; }\n  .rump-modal .bold-message {\n    text-align: center;\n    font-weight: 600;\n    color: #6297b1;\n    display: block; }\n  .rump-modal .rump-modal-content {\n    position: relative;\n    color: black;\n    font-size: 1.5em; }\n    .rump-modal .rump-modal-content .close-modal {\n      position: absolute;\n      cursor: pointer;\n      top: 10px;\n      right: 8px;\n      padding: 10px;\n      color: white; }\n\n.rump-modal-header {\n  background-color: #4a556b;\n  padding: 0;\n  border-top-left-radius: 10px;\n  border-top-right-radius: 10px; }\n  .rump-modal-header h3 {\n    font-family: \"Roboto\", sans-serif;\n    margin-top: 0;\n    margin-bottom: 0;\n    font-size: 14px;\n    font-weight: 200;\n    text-transform: uppercase;\n    color: white;\n    padding: 25px;\n    letter-spacing: 0.5px; }\n\n.rump-modal-body {\n  font-size: 0.75em;\n  background-color: white;\n  margin: 30px 55px 20px;\n  padding: 35px;\n  border: 2px solid #e7e7e7; }\n  .rump-modal-body h3 {\n    font-family: \"Roboto\", sans-serif;\n    margin-top: 0;\n    margin-bottom: 0;\n    font-size: 14px;\n    font-weight: 200;\n    text-transform: uppercase;\n    color: white;\n    padding: 25px;\n    letter-spacing: 0.5px; }\n  .rump-modal-body p {\n    margin-top: 10px;\n    margin-bottom: 10px; }\n    .rump-modal-body p a {\n      color: #6297b1; }\n      .rump-modal-body p a:hover {\n        color: #6297b1;\n        -webkit-filter: brightness(110%);\n                filter: brightness(110%); }\n  .rump-modal-body.white-modal-body {\n    margin: 0;\n    border: 2px solid #e7e7e7;\n    background: white;\n    padding: 50px; }\n    .rump-modal-body.white-modal-body img {\n      width: 55px;\n      height: 55px;\n      margin-right: 15px;\n      margin-bottom: 20px; }\n    .rump-modal-body.white-modal-body h3 {\n      color: #4a556b;\n      padding-left: 0;\n      font-weight: 600;\n      padding-top: 10px;\n      display: inline-block;\n      letter-spacing: 0.4px;\n      top: -8px;\n      position: relative; }\n    .rump-modal-body.white-modal-body p {\n      color: #999999;\n      font-size: 13px;\n      font-weight: 300;\n      font-family: \"Roboto\", sans-serif;\n      letter-spacing: 0.4px; }\n    .rump-modal-body.white-modal-body .close-modal i {\n      color: #9a9a9a; }\n    .rump-modal-body.white-modal-body .actions {\n      display: -webkit-box;\n      display: -ms-flexbox;\n      display: flex;\n      -webkit-box-pack: center;\n          -ms-flex-pack: center;\n              justify-content: center;\n      -ms-flex-wrap: wrap;\n          flex-wrap: wrap;\n      margin-top: 30px; }\n      .rump-modal-body.white-modal-body .actions button {\n        text-align: center;\n        color: white;\n        background-color: #4a556b;\n        font-size: 11px;\n        outline: none;\n        min-width: 110px;\n        padding: 10px 20px;\n        font-weight: 300;\n        margin: 20px 10px 0px;\n        text-transform: uppercase;\n        display: inline-block;\n        border-radius: 4px; }\n        .rump-modal-body.white-modal-body .actions button:hover, .rump-modal-body.white-modal-body .actions button:active, .rump-modal-body.white-modal-body .actions button:focus {\n          outline: none;\n          -webkit-filter: brightness(110%);\n                  filter: brightness(110%);\n          background-color: #4a556b;\n          color: white; }\n\n.rump-modal-footer {\n  text-align: center;\n  padding: 0px 30px 20px; }\n  .rump-modal-footer button {\n    background-color: #6297b1;\n    color: white;\n    padding: 10px 20px;\n    margin: 0.5em 1em 1em;\n    border-radius: 8px;\n    font-family: \"Roboto\", sans-serif; }\n    .rump-modal-footer button:hover {\n      -webkit-filter: brightness(110%);\n      /* Safari */\n      filter: brightness(110%);\n      color: white; }\n    .rump-modal-footer button:focus {\n      outline: none; }\n    .rump-modal-footer button.accept {\n      background-color: #6297b1;\n      color: white; }\n  .rump-modal-footer a {\n    color: white;\n    text-decoration: none; }\n    .rump-modal-footer a:hover {\n      color: white; }\n\n/* Customises appearance of libraries' components */\n/*\n * Copyright (C) 2017 HAT Data Exchange Ltd - All Rights Reserved\n * This Source Code Form is subject to the terms of the Mozilla Public\n * License, v. 2.0. If a copy of the MPL was not distributed with this\n * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n * Written by Augustinas Markevicius <augustinas.markevicius@hatdex.org> 2, 2017\n */\n/*!\n * Copyright (C) 2017 HAT Data Exchange Ltd - All Rights Reserved\n * This Source Code Form is subject to the terms of the Mozilla Public\n * License, v. 2.0. If a copy of the MPL was not distributed with this\n * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n * Written by Augustinas Markevicius <augustinas.markevicius@hatdex.org> 4, 2017\n */\n/*!\n * Copyright (C) 2017 HAT Data Exchange Ltd - All Rights Reserved\n * This Source Code Form is subject to the terms of the Mozilla Public\n * License, v. 2.0. If a copy of the MPL was not distributed with this\n * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n * Written by Augustinas Markevicius <augustinas.markevicius@hatdex.org> 4, 2017\n */\n/* General styling for popovers */\n.popover {\n  z-index: 600;\n  width: 250px; }\n\n.navbar .popover {\n  width: unset; }\n\n/* Customised styling for Simple MD Editor */\n.editor-toolbar.fullscreen,\n.editor-toolbar {\n  border-radius: 0;\n  border: none;\n  background-color: #6297b1;\n  opacity: 1; }\n  .editor-toolbar.fullscreen:hover,\n  .editor-toolbar:hover {\n    opacity: 1; }\n\n.editor-toolbar.disabled-for-preview a:not(.no-disable) {\n  background-color: #393939;\n  opacity: 1; }\n\n.editor-toolbar .fa {\n  color: white !important;\n  border-radius: 0; }\n\n.editor-toolbar .fa:hover,\n.editor-toolbar a.active {\n  background-color: rgba(255, 255, 255, 0.3); }\n\n.CodeMirror {\n  border: none;\n  border-radius: 0; }\n\n.customIntroTooltip {\n  background-color: #6297b1;\n  color: white; }\n\n.customIntroTooltip .inline-link {\n  color: #cacaca;\n  text-decoration: underline; }\n\n.customIntroTooltip .inline-link:hover {\n  color: white;\n  text-decoration: none; }\n\n.customIntroHighlight {\n  background-color: #6297b1; }\n\n.CodeMirror pre {\n  font-size: 1.5em; }\n", ""]);

// exports


/***/ }),

/***/ "../../../../css-loader/lib/css-base.js":
/***/ (function(module, exports) {

/*
	MIT License http://www.opensource.org/licenses/mit-license.php
	Author Tobias Koppers @sokra
*/
// css base code, injected by the css-loader
module.exports = function(useSourceMap) {
	var list = [];

	// return the list of modules as css string
	list.toString = function toString() {
		return this.map(function (item) {
			var content = cssWithMappingToString(item, useSourceMap);
			if(item[2]) {
				return "@media " + item[2] + "{" + content + "}";
			} else {
				return content;
			}
		}).join("");
	};

	// import a list of modules into the list
	list.i = function(modules, mediaQuery) {
		if(typeof modules === "string")
			modules = [[null, modules, ""]];
		var alreadyImportedModules = {};
		for(var i = 0; i < this.length; i++) {
			var id = this[i][0];
			if(typeof id === "number")
				alreadyImportedModules[id] = true;
		}
		for(i = 0; i < modules.length; i++) {
			var item = modules[i];
			// skip already imported module
			// this implementation is not 100% perfect for weird media query combinations
			//  when a module is imported multiple times with different media queries.
			//  I hope this will never occur (Hey this way we have smaller bundles)
			if(typeof item[0] !== "number" || !alreadyImportedModules[item[0]]) {
				if(mediaQuery && !item[2]) {
					item[2] = mediaQuery;
				} else if(mediaQuery) {
					item[2] = "(" + item[2] + ") and (" + mediaQuery + ")";
				}
				list.push(item);
			}
		}
	};
	return list;
};

function cssWithMappingToString(item, useSourceMap) {
	var content = item[1] || '';
	var cssMapping = item[3];
	if (!cssMapping) {
		return content;
	}

	if (useSourceMap && typeof btoa === 'function') {
		var sourceMapping = toComment(cssMapping);
		var sourceURLs = cssMapping.sources.map(function (source) {
			return '/*# sourceURL=' + cssMapping.sourceRoot + source + ' */'
		});

		return [content].concat(sourceURLs).concat([sourceMapping]).join('\n');
	}

	return [content].join('\n');
}

// Adapted from convert-source-map (MIT)
function toComment(sourceMap) {
	// eslint-disable-next-line no-undef
	var base64 = btoa(unescape(encodeURIComponent(JSON.stringify(sourceMap))));
	var data = 'sourceMappingURL=data:application/json;charset=utf-8;base64,' + base64;

	return '/*# ' + data + ' */';
}


/***/ }),

/***/ "../../../../style-loader/addStyles.js":
/***/ (function(module, exports) {

/*
	MIT License http://www.opensource.org/licenses/mit-license.php
	Author Tobias Koppers @sokra
*/
var stylesInDom = {},
	memoize = function(fn) {
		var memo;
		return function () {
			if (typeof memo === "undefined") memo = fn.apply(this, arguments);
			return memo;
		};
	},
	isOldIE = memoize(function() {
		return /msie [6-9]\b/.test(self.navigator.userAgent.toLowerCase());
	}),
	getHeadElement = memoize(function () {
		return document.head || document.getElementsByTagName("head")[0];
	}),
	singletonElement = null,
	singletonCounter = 0,
	styleElementsInsertedAtTop = [];

module.exports = function(list, options) {
	if(typeof DEBUG !== "undefined" && DEBUG) {
		if(typeof document !== "object") throw new Error("The style-loader cannot be used in a non-browser environment");
	}

	options = options || {};
	// Force single-tag solution on IE6-9, which has a hard limit on the # of <style>
	// tags it will allow on a page
	if (typeof options.singleton === "undefined") options.singleton = isOldIE();

	// By default, add <style> tags to the bottom of <head>.
	if (typeof options.insertAt === "undefined") options.insertAt = "bottom";

	var styles = listToStyles(list);
	addStylesToDom(styles, options);

	return function update(newList) {
		var mayRemove = [];
		for(var i = 0; i < styles.length; i++) {
			var item = styles[i];
			var domStyle = stylesInDom[item.id];
			domStyle.refs--;
			mayRemove.push(domStyle);
		}
		if(newList) {
			var newStyles = listToStyles(newList);
			addStylesToDom(newStyles, options);
		}
		for(var i = 0; i < mayRemove.length; i++) {
			var domStyle = mayRemove[i];
			if(domStyle.refs === 0) {
				for(var j = 0; j < domStyle.parts.length; j++)
					domStyle.parts[j]();
				delete stylesInDom[domStyle.id];
			}
		}
	};
}

function addStylesToDom(styles, options) {
	for(var i = 0; i < styles.length; i++) {
		var item = styles[i];
		var domStyle = stylesInDom[item.id];
		if(domStyle) {
			domStyle.refs++;
			for(var j = 0; j < domStyle.parts.length; j++) {
				domStyle.parts[j](item.parts[j]);
			}
			for(; j < item.parts.length; j++) {
				domStyle.parts.push(addStyle(item.parts[j], options));
			}
		} else {
			var parts = [];
			for(var j = 0; j < item.parts.length; j++) {
				parts.push(addStyle(item.parts[j], options));
			}
			stylesInDom[item.id] = {id: item.id, refs: 1, parts: parts};
		}
	}
}

function listToStyles(list) {
	var styles = [];
	var newStyles = {};
	for(var i = 0; i < list.length; i++) {
		var item = list[i];
		var id = item[0];
		var css = item[1];
		var media = item[2];
		var sourceMap = item[3];
		var part = {css: css, media: media, sourceMap: sourceMap};
		if(!newStyles[id])
			styles.push(newStyles[id] = {id: id, parts: [part]});
		else
			newStyles[id].parts.push(part);
	}
	return styles;
}

function insertStyleElement(options, styleElement) {
	var head = getHeadElement();
	var lastStyleElementInsertedAtTop = styleElementsInsertedAtTop[styleElementsInsertedAtTop.length - 1];
	if (options.insertAt === "top") {
		if(!lastStyleElementInsertedAtTop) {
			head.insertBefore(styleElement, head.firstChild);
		} else if(lastStyleElementInsertedAtTop.nextSibling) {
			head.insertBefore(styleElement, lastStyleElementInsertedAtTop.nextSibling);
		} else {
			head.appendChild(styleElement);
		}
		styleElementsInsertedAtTop.push(styleElement);
	} else if (options.insertAt === "bottom") {
		head.appendChild(styleElement);
	} else {
		throw new Error("Invalid value for parameter 'insertAt'. Must be 'top' or 'bottom'.");
	}
}

function removeStyleElement(styleElement) {
	styleElement.parentNode.removeChild(styleElement);
	var idx = styleElementsInsertedAtTop.indexOf(styleElement);
	if(idx >= 0) {
		styleElementsInsertedAtTop.splice(idx, 1);
	}
}

function createStyleElement(options) {
	var styleElement = document.createElement("style");
	styleElement.type = "text/css";
	insertStyleElement(options, styleElement);
	return styleElement;
}

function createLinkElement(options) {
	var linkElement = document.createElement("link");
	linkElement.rel = "stylesheet";
	insertStyleElement(options, linkElement);
	return linkElement;
}

function addStyle(obj, options) {
	var styleElement, update, remove;

	if (options.singleton) {
		var styleIndex = singletonCounter++;
		styleElement = singletonElement || (singletonElement = createStyleElement(options));
		update = applyToSingletonTag.bind(null, styleElement, styleIndex, false);
		remove = applyToSingletonTag.bind(null, styleElement, styleIndex, true);
	} else if(obj.sourceMap &&
		typeof URL === "function" &&
		typeof URL.createObjectURL === "function" &&
		typeof URL.revokeObjectURL === "function" &&
		typeof Blob === "function" &&
		typeof btoa === "function") {
		styleElement = createLinkElement(options);
		update = updateLink.bind(null, styleElement);
		remove = function() {
			removeStyleElement(styleElement);
			if(styleElement.href)
				URL.revokeObjectURL(styleElement.href);
		};
	} else {
		styleElement = createStyleElement(options);
		update = applyToTag.bind(null, styleElement);
		remove = function() {
			removeStyleElement(styleElement);
		};
	}

	update(obj);

	return function updateStyle(newObj) {
		if(newObj) {
			if(newObj.css === obj.css && newObj.media === obj.media && newObj.sourceMap === obj.sourceMap)
				return;
			update(obj = newObj);
		} else {
			remove();
		}
	};
}

var replaceText = (function () {
	var textStore = [];

	return function (index, replacement) {
		textStore[index] = replacement;
		return textStore.filter(Boolean).join('\n');
	};
})();

function applyToSingletonTag(styleElement, index, remove, obj) {
	var css = remove ? "" : obj.css;

	if (styleElement.styleSheet) {
		styleElement.styleSheet.cssText = replaceText(index, css);
	} else {
		var cssNode = document.createTextNode(css);
		var childNodes = styleElement.childNodes;
		if (childNodes[index]) styleElement.removeChild(childNodes[index]);
		if (childNodes.length) {
			styleElement.insertBefore(cssNode, childNodes[index]);
		} else {
			styleElement.appendChild(cssNode);
		}
	}
}

function applyToTag(styleElement, obj) {
	var css = obj.css;
	var media = obj.media;

	if(media) {
		styleElement.setAttribute("media", media)
	}

	if(styleElement.styleSheet) {
		styleElement.styleSheet.cssText = css;
	} else {
		while(styleElement.firstChild) {
			styleElement.removeChild(styleElement.firstChild);
		}
		styleElement.appendChild(document.createTextNode(css));
	}
}

function updateLink(linkElement, obj) {
	var css = obj.css;
	var sourceMap = obj.sourceMap;

	if(sourceMap) {
		// http://stackoverflow.com/a/26603875
		css += "\n/*# sourceMappingURL=data:application/json;base64," + btoa(unescape(encodeURIComponent(JSON.stringify(sourceMap)))) + " */";
	}

	var blob = new Blob([css], { type: "text/css" });

	var oldSrc = linkElement.href;

	linkElement.href = URL.createObjectURL(blob);

	if(oldSrc)
		URL.revokeObjectURL(oldSrc);
}


/***/ }),

/***/ 2:
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__("../../../../../src/styles.scss");


/***/ })

},[2]);
//# sourceMappingURL=styles.bundle.js.map