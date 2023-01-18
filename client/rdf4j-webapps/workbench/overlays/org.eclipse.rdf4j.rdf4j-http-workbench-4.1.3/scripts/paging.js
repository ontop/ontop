/// <reference path="template.ts" />
/// <reference path="jquery.d.ts" />
// WARNING: Do not edit the *.js version of this file. Instead, always edit the
// corresponding *.ts source in the ts subfolder, and then invoke the
// compileTypescript.sh bash script to generate new *.js and *.js.map files.
var workbench;
(function (workbench) {
    var paging;
    (function (paging) {
        var KT = 'know_total';
        var OFFSET = 'offset';
        paging.LIMIT = 'limit';
        paging.LIM_ID = '#' + paging.LIMIT;
        var AMP = decodeURIComponent('%26');
        function addCookieToUrlQueryIfPresent(url, name) {
            var value = workbench.getCookie(name);
            if (value) {
                url = url + AMP + name + '=' + value;
            }
            return url;
        }
        /**
         * Invoked in graph.xsl and tuple.xsl for download functionality. Takes a
         * document element by name, and creates a request with it as a parameter.
         */
        function addGraphParam(name) {
            var value = encodeURIComponent($('#' + name).val());
            var url = document.location.href;
            var ref = workbench.getCookie('ref');
            if (url.match(/query$/)) {
                if ('id' == ref) {
                    url = url + ';ref=id' + AMP + 'action=exec';
                    url = addCookieToUrlQueryIfPresent(url, 'query');
                    url = addCookieToUrlQueryIfPresent(url, 'queryLn');
                    url = addCookieToUrlQueryIfPresent(url, 'infer');
                    url = addCookieToUrlQueryIfPresent(url, 'limit_query');
                }
                else {
                    alert("Can't put query in URL, since it might be too long for your browser.\n" +
                        "Save your query on the server, then execute it from the 'Saved Queries' page.");
                    return;
                }
            }
            if (url.indexOf('?') + 1 || url.indexOf(';') + 1) {
                document.location.href = url + AMP + name + '=' + value;
            }
            else {
                document.location.href = url + ';' + name + '=' + value;
            }
        }
        paging.addGraphParam = addGraphParam;
        var StringMap = (function () {
            function StringMap() {
            }
            return StringMap;
        })();
        /**
         * Scans the given URI for duplicate query parameter names, and removes
         * all but the last occurrence for any duplicate case.
         *
         * @param {String} href The URI to simplify.
         * @returns {String} The URI with only the last occurrence of any
         *          given parameter name remaining.
         */
        function simplifyParameters(href) {
            var params = {};
            var rval = '';
            var queryString = getQueryString(href);
            var start = href.substring(0, href.indexOf(queryString));
            var elements = queryString.split(decodeURIComponent('%26'));
            for (var i = 0; elements.length - i; i++) {
                var pair = elements[i].split('=');
                params[pair[0]] = pair[1];
            }
            for (var name in params) {
                // use hasOwnProperty to filter out keys from the
                // Object.prototype
                if (params.hasOwnProperty(name)) {
                    rval += name + '=' + params[name] + AMP;
                }
            }
            rval = start + rval.substring(0, rval.length - 1);
            return rval;
        }
        /**
         * First, adds the given parameter to the URL query string. Second,
         * adds a 'know_total' parameter if its current value is 'false' or
         * non-existent. Third, simplifies the URL. Fourth, sends the browser
         * to the modified URL.
         *
         * @param {String} name The name of the query parameter.
         * @param {number} value The value of the query parameter.
         */
        function addPagingParam(name, value) {
            var url = document.location.href;
            var hasParams = (url.indexOf('?') + 1 || url.indexOf(';') + 1);
            var sep = hasParams ? AMP : ';';
            url = url + sep + name + '=' + value;
            if (!hasQueryParameter(KT) || 'false' == getQueryParameter(KT)) {
                url += AMP + KT + '=' + getTotalResultCount();
            }
            if (!hasQueryParameter('query')) {
                url += AMP + 'query=' + encodeURIComponent(workbench.getCookie('query'));
                url += AMP + 'ref=' + encodeURIComponent(workbench.getCookie('ref'));
            }
            document.location.href = simplifyParameters(url);
        }
        paging.addPagingParam = addPagingParam;
        /**
         * Invoked in tuple.xsl and explore.xsl. Changes the limit query
         * parameter and navigates to the new URL.
         */
        function addLimit(page) {
            var suffix = '_' + page;
            addPagingParam(paging.LIMIT + suffix, $(paging.LIM_ID + suffix).val());
        }
        paging.addLimit = addLimit;
        /**
         * Invoked in tuple.xsl and explore.xsl. Increments the offset query
         * parameter, and navigates to the new URL.
         */
        function nextOffset(page) {
            addPagingParam(OFFSET, getOffset() + getLimit(page));
        }
        paging.nextOffset = nextOffset;
        /**
         * Invoked in tuple.xsl and explore.xsl. Decrements the offset query
         * parameter and navigates to the new URL.
         */
        function previousOffset(page) {
            addPagingParam(OFFSET, Math.max(0, getOffset() - getLimit(page)));
        }
        paging.previousOffset = previousOffset;
        /**
         * @returns {number} The value of the offset query parameter.
         */
        function getOffset() {
            var offset = getQueryParameter(OFFSET);
            return ('' == offset) ? 0 : parseInt(offset, 10);
        }
        paging.getOffset = getOffset;
        /**
         * @returns {number} The value of the limit query parameter.
         */
        function getLimit(page) {
            return parseInt($(paging.LIM_ID + '_' + page).val(), 10);
        }
        paging.getLimit = getLimit;
        /**
         * Retrieves the URL query parameter with the given name.
         *
         * @param {String} name The name of the parameter to retrieve.
         * @returns {String} The value of the given parameter, or an empty
         *          string if it doesn't exist.
         */
        function getQueryParameter(name) {
            var rval = '';
            var elements = getQueryString(document.location.href).split(decodeURIComponent('%26'));
            for (var i = 0; elements.length - i; i++) {
                var pair = elements[i].split('=');
                if (name != pair[0]) {
                    continue;
                }
                rval = pair[1];
            }
            return rval;
        }
        paging.getQueryParameter = getQueryParameter;
        /**
         * Gets whether a URL query parameter with the given name is present.
         *
         * @param {String} name The name of the parameter to retrieve.
         * @returns {Boolean} True, if a parameter with the given name is in
         *                    the URL. Otherwise, false.
         */
        function hasQueryParameter(name) {
            var rval = false;
            var elements = getQueryString(document.location.href).split(decodeURIComponent('%26'));
            for (var i = 0; elements.length - i; i++) {
                var pair = elements[i].split('=');
                if (name == pair[0]) {
                    rval = true;
                    break;
                }
            }
            return rval;
        }
        paging.hasQueryParameter = hasQueryParameter;
        /**
         * Convenience function for returning the tail of a string after a
         * given character.
         *
         * @param {String} value The string to get the tail of.
         * @param split
         *            character to give tail after
         * @returns The substring after the 'split' character, or the original
         *          string if 'split' is not found.
         */
        function tailAfter(value, split) {
            return value.substring(value.indexOf(split) + 1);
        }
        function getQueryString(href) {
            return tailAfter(tailAfter(href, '?'), ';');
        }
        paging.getQueryString = getQueryString;
        /**
         * Using the value of the 'limit' query parameter, correct the text of the
         * Next and Previous buttons. Makes use of RegExp to preserve any
         * localization.
         */
        function correctButtons(page) {
            var buttonWordPattern = /^[A-z]+\s+/;
            var nextButton = $('#nextX');
            var oldNext = nextButton.val();
            var count = parseInt(/\d+$/.exec(oldNext)[0], 10);
            var limit = workbench.paging.getLimit(page);
            nextButton.val(buttonWordPattern.exec(oldNext)[0] + limit);
            var previousButton = $('#previousX');
            previousButton
                .val(buttonWordPattern.exec(previousButton.val())[0] + limit);
            var offset = workbench.paging.getOffset();
            previousButton.prop('disabled', (offset <= 0 || limit <= 0));
            nextButton.prop('disabled', (count < limit || limit <= 0 || (offset + count) >= getTotalResultCount()));
        }
        paging.correctButtons = correctButtons;
        /**
         * Gets the total result count, preferably from the 'know_total' query
         * parameter. If the parameter doesn't exist, get it from the
         * 'total_result_count' cookie.
         *
         * @returns {Number} The given total result count, or zero if it isn't
         *          given.
         */
        function getTotalResultCount() {
            var total_result_count = 0;
            var s_trc = workbench.paging.getQueryParameter(KT);
            if (s_trc.length == 0) {
                s_trc = workbench.getCookie('total_result_count');
            }
            if (s_trc.length > 0) {
                total_result_count = parseInt(s_trc, 10);
            }
            return total_result_count;
        }
        paging.getTotalResultCount = getTotalResultCount;
        var DataTypeVisibility;
        (function (DataTypeVisibility) {
            function setCookie(c_name, value, exdays) {
                var exdate = new Date();
                exdate.setDate(exdate.getDate() + exdays);
                document.cookie = c_name + "=" + value +
                    ((exdays == null) ? "" :
                        "; expires=" + exdate.toUTCString());
            }
            function setShow(show) {
                setCookie('show-datatypes', show, 365);
                var data = show ? 'data-longform' : 'data-shortform';
                $('div.resource[' + data + ']').each(function () {
                    var me = $(this);
                    me.find('a:first').text(decodeURIComponent(me.attr(data)));
                });
            }
            DataTypeVisibility.setShow = setShow;
        })(DataTypeVisibility || (DataTypeVisibility = {}));
        function setShowDataTypesCheckboxAndSetChangeEvent() {
            var hideDataTypes = (workbench.getCookie('show-datatypes') == 'false');
            var showDTcb = $("input[name='show-datatypes']");
            if (hideDataTypes) {
                showDTcb.prop('checked', false);
                DataTypeVisibility.setShow(false);
            }
            showDTcb.on('change', function () {
                DataTypeVisibility.setShow(showDTcb.prop('checked'));
            });
        }
        paging.setShowDataTypesCheckboxAndSetChangeEvent = setShowDataTypesCheckboxAndSetChangeEvent;
    })(paging = workbench.paging || (workbench.paging = {}));
})(workbench || (workbench = {}));
//# sourceMappingURL=paging.js.map