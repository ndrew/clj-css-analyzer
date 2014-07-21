(function () {
    "use strict";

    //
    // helpers

    var d = document;

    function nextTo(el, predicate) {
        if (el.length) {
            for (var i = 0; i < el.length; i++) {
                if (predicate(el[i], i)) {
                  return el[i];
                }
            }
        } else {
            for (var p in el) {
                if (predicate(el[p], p)) {
                    return el[p];
                }
            }
        }
    }

    function hasClass(el, cls) {
        return (' ' + el.className + ' ').indexOf(' ' + cls + ' ') > -1;
    }

    function triggerEvent(el, etype) {
        if (el.fireEvent) {
            el.fireEvent('on' + etype);
        } else {
            var evObj = document.createEvent('Events');
            evObj.initEvent(etype, true, false);
            el.dispatchEvent(evObj);
        }
    }


    //
    // ui 'framework'

    function tabView(el, config) {

        var currentTab;

        /**
         * returns a 'click' handler function for a tab that passes tab viewport into callback.
         * If callback returns false the tab won't be changed.
         */
        function clickHandler(cb) {
            return function (e) {
                if (e.target !== currentTab) {
                    var tabView = nextTo(e.target.parentNode.parentNode.childNodes, function (el) { // ugly as a fuck
                        return hasClass(el, "tab-view");
                    });

                    if (cb(tabView, e) !== false) {
                        if (currentTab) {
                            currentTab.setAttribute("href", "#");
                        }

                        currentTab = e.target;
                        currentTab.removeAttribute("href");

                    }
                }
                e.preventDefault();
            };
        }

        function tab(caption, cb) {
            var el = d.createElement("a");
            el.appendChild(d.createTextNode(caption));
            el.setAttribute("href", "#");
            el.addEventListener("click", clickHandler(cb));
            return el;
        }


        var viewport = d.createElement("div");
        viewport.setAttribute("class", "tab-view");

        var menu = d.createElement("div");
        menu.setAttribute("class", "tab-menu");

        for (var i = 0; i < config.length; i++) {
            var tuple = config[i];
            menu.appendChild(tab(tuple[0], tuple[1]));
        }

        el.appendChild(menu);
        el.appendChild(viewport);

        if (config.length) {
            triggerEvent(menu.firstChild, 'click');
        }

        return el;
    }


    //
    // app

    function init() {
        var css = d.getElementById("css");
        var html = d.getElementById("html");

        var cssUi = tabView(d.createElement("section"), [
            ["URL",
                function (el) {
                    el.innerHTML = "Foo";
                }
            ],
            ["Text",
                function (el) {
                    el.innerHTML = "Bar";
                }
            ],
            ["Upload",
                function (el) {
                    el.innerHTML = "Buzz";
                }
            ]
        ]);

        css.appendChild(cssUi);


        var htmlUi = tabView(d.createElement("section"), [
            ["foo",
                function () {}
            ]
        ]);

        html.appendChild(htmlUi);

    }
    d.addEventListener("DOMContentLoaded", init);

})();