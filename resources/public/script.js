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


    function post(uri, params, cb) {
      var r = new XMLHttpRequest();
      r.open("POST", uri, true);
      r.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
      r.onreadystatechange = function () {
        if (r.readyState != 4 || r.status != 200) {
          return;
        }
        cb(JSON.parse(r.responseText));
      };

      if (typeof params !== "string") {
        var res = [];
        for (var p in params) {
          if (params.hasOwnProperty(p)) {
            res.push(p+'='+encodeURIComponent(params[p]));
          }
        }
        params = res.join('&');
      }

      r.send(params);
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


    function textBox(el, config) {
        var t = d.createElement("textarea");

        for (var prop in config) {
          if (config.hasOwnProperty(prop) && typeof t[prop] != "undefined") {
            t[prop] = config[prop];
          }
        }

        var offset= !window.opera ? (t.offsetHeight - t.clientHeight) : (t.offsetHeight + parseInt(window.getComputedStyle(t, null).getPropertyValue('border-top-width'))) ;

        var resize 	= function(t) {
          t.style.height = 'auto';
          t.style.height = (t.scrollHeight  + offset ) + 'px';
        }

        t.addEventListener && t.addEventListener('input', function(event) {
          resize(t);
        });

        t['attachEvent']  && t.attachEvent('onkeyup', function() {
          resize(t);
        });


        el.appendChild(t);
        return el;
    }

    //
    // app

    function analyzeHandler(e) {

        var delimiter = ';'

        function getUrls(id) {
            var t = d.getElementById(id);
            t.value = t.value.trim();
            return t.value.trim().split('\n');
        }

        function urlsToDivs(id, urls) {
            var divs = d.createElement("div");
            for (var i = 0; i < urls.length; i++) {
                var el = d.createElement("div");
                el.appendChild(d.createTextNode(urls[i]));

                var statusEl = d.createElement("span");
                statusEl.setAttribute("class", "url-status");
                statusEl.appendChild(d.createTextNode("..."));

                el.appendChild(statusEl);

                el.setAttribute("id", "css-"+1);
                //el.setClass()


                divs.appendChild(el);
            }

            var textBox = d.getElementById(id);
            textBox.style.display = 'none';


            if (!d.getElementById(id+'-toggle')) {
              var el = d.createElement("div");
              el.setAttribute("id", id+'-toggle');
              textBox.parentNode.appendChild(el);
            }
            var container = d.getElementById(id+'-toggle');
            container.innerHTML = "";
            container.appendChild(divs);
        }

        var cssUrls = getUrls('css-urls');
        urlsToDivs('css-urls', cssUrls);

        var htmlUrls = getUrls('html-urls');
        urlsToDivs('html-urls', htmlUrls);

        var paramsMap = {
          'css': cssUrls.join(delimiter),
          'html': htmlUrls.join(delimiter)
        };

        // uncomment me
        //return;

        post("analyze", paramsMap, function(data) {
            //console.warn(data);

            var block = d.getElementById("results");

            var r = d.getElementById("resultsTab");
            r.innerHTML = '';

            var resultsUi = tabView(d.createElement("section"), [
                ["Foo",
                    function (el) {
                        el.innerHTML = "<pre>" + JSON.stringify(data) + "</pre>";
                    }
                ],
                ["Bar",
                    function (el) {
                        el.innerHTML = "Bar";
                    }
                ]
            ]);
            r.appendChild(resultsUi);

            block.style.display = "block";
        });


        /*


         */
    }


    function init() {
        var css = d.getElementById("css");
        var cssUi = textBox(d.createElement("section"), {
          id: 'css-urls',
          value: 'http://yoursite/style.css\n...'
        });
        css.appendChild(cssUi);


        var html = d.getElementById("html");
        var htmlUi = textBox(d.createElement("section"), {
          id: 'html-urls',
          value: 'html://yoursite/\nhtml://yoursite/page\n...'
        })
        html.appendChild(htmlUi);

        var results = d.getElementById("results");
        results.style.display = 'none';

        d.getElementById("analyze-btn").onclick = analyzeHandler;

    }

    d.addEventListener("DOMContentLoaded", init);

})();
