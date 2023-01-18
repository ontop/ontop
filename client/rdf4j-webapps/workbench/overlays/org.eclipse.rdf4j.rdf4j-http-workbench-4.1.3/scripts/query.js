/// <reference path="template.ts" />
/// <reference path="jquery.d.ts" />
/// <reference path="yasqe.d.ts" />
/// <reference path="yasqeHelper.ts" />
// WARNING: Do not edit the *.js version of this file. Instead, always edit the
// corresponding *.ts source in the ts subfolder, and then invoke the
// compileTypescript.sh bash script to generate new *.js and *.js.map files.
var workbench;
(function (workbench) {
    var query;
    (function (query_1) {
        /**
         * Holds the current selected query language.
         */
        var currentQueryLn = '';
        var yasqe = null;
        /**
         * Populate reasonable default name space declarations into the query text area.
         * The server has provided the declaration text in hidden elements.
         */
        function loadNamespaces() {
            function toggleNamespaces() {
                workbench.query.setQueryValue(namespaces.text());
                currentQueryLn = queryLn;
            }
            var query = workbench.query.getQueryValue();
            var queryLn = $('#queryLn').val();
            var namespaces = $('#' + queryLn + '-namespaces');
            var last = $('#' + currentQueryLn + '-namespaces');
            if (namespaces.length) {
                if (!query || query.trim().length == 0) {
                    toggleNamespaces();
                }
                if (last.length && (query == last.text())) {
                    toggleNamespaces();
                }
            }
        }
        query_1.loadNamespaces = loadNamespaces;
        /**
         *Fires when the query language is changed
         */
        function onQlChange() {
            workbench.query.loadNamespaces();
            workbench.query.updateYasqe();
        }
        query_1.onQlChange = onQlChange;
        /**
         * Invoked by the "clear" button. After confirming with the user,
         * clears the query text and loads the current repository and query
         * language name space declarations.
         */
        function resetNamespaces() {
            if (confirm('Click OK to clear the current query text and replace' +
                'it with the ' + $('#queryLn').val() +
                ' namespace declarations.')) {
                workbench.query.setQueryValue('');
                workbench.query.loadNamespaces();
            }
        }
        query_1.resetNamespaces = resetNamespaces;
        /**
         * Clear any contents of the save feedback field.
         */
        function clearFeedback() {
            $('#save-feedback').removeClass().text('');
        }
        query_1.clearFeedback = clearFeedback;
        /**
         * Clear the save feedback field, and look at the contents of the query name
         * field. Disables the save button if the field doesn't satisfy a given regular
         * expression. With a delay of 200 msec, to give enough time after
         * the event for the document to have changed. (Workaround for annoying browser
         * behavior.)
         */
        function handleNameChange() {
            setTimeout(function disableSaveIfNotValidName() {
                $('#save').prop('disabled', !/^[- \w]{1,32}$/.test($('#query-name').val()));
                workbench.query.clearFeedback();
            }, 0);
        }
        query_1.handleNameChange = handleNameChange;
        /**
         * Send a background HTTP request to save the query, and handle the
         * response asynchronously.
         *
         * @param overwrite
         *            if true, add a URL parameter that tells the server we wish
         *            to overwrite any already saved query
         */
        function ajaxSave(overwrite) {
            var feedback = $('#save-feedback');
            var url = [];
            url[url.length] = 'query';
            if (overwrite) {
                url[url.length] = document.all ? ';' : '?';
                url[url.length] = 'overwrite=true&';
            }
            var href = url.join('');
            var form = $('form[action="query"]');
            $.ajax({
                url: href,
                type: 'POST',
                dataType: 'json',
                data: form.serialize(),
                timeout: 5000,
                error: function (jqXHR, textStatus, errorThrown) {
                    feedback.removeClass().addClass('error');
                    if (textStatus == 'timeout') {
                        feedback.text('Timed out waiting for response. Uncertain if save occured.');
                    }
                    else {
                        feedback.text('Save Request Failed: Error Type = ' +
                            textStatus + ', HTTP Status Text = "' + errorThrown + '"');
                    }
                },
                success: function (response) {
                    if (response.accessible) {
                        if (response.written) {
                            feedback.removeClass().addClass('success');
                            feedback.text('Query saved.');
                        }
                        else {
                            if (response.existed) {
                                if (confirm('Query name exists. Click OK to overwrite.')) {
                                    ajaxSave(true);
                                }
                                else {
                                    feedback.removeClass().addClass('error');
                                    feedback.text('Cancelled overwriting existing query.');
                                }
                            }
                        }
                    }
                    else {
                        feedback.removeClass().addClass('error');
                        feedback.text('Repository was not accessible (check your permissions).');
                    }
                }
            });
        }
        /**
         * Invoked by form submission.
         *
         * @returns {boolean} true if a form POST is performed, false if
         *          a GET is instead performed
         */
        function doSubmit() {
            //if yasqe is instantiated, make sure we save the value to the textarea
            if (yasqe)
                yasqe.save();
            var allowPageToSubmitForm = false;
            var save = ($('#action').val() == 'save');
            if (save) {
                ajaxSave(false);
            }
            else {
                var url = [];
                url[url.length] = 'query';
                if (document.all) {
                    url[url.length] = ';';
                }
                else {
                    url[url.length] = '?';
                }
                workbench.addParam(url, 'action');
                workbench.addParam(url, 'queryLn');
                workbench.addParam(url, 'query');
                workbench.addParam(url, 'limit_query');
                workbench.addParam(url, 'infer');
                var href = url.join('');
                var loc = document.location;
                var currentBaseLength = loc.href.length - loc.pathname.length
                    - loc.search.length;
                var pathLength = href.length;
                var urlLength = pathLength + currentBaseLength;
                // Published Internet Explorer restrictions on URL length, which are the
                // most restrictive of the major browsers.
                if (pathLength > 2048 || urlLength > 2083) {
                    alert("Due to its length, your query will be posted in the request body. "
                        + "It won't be possible to use a bookmark for the results page.");
                    allowPageToSubmitForm = true;
                }
                else {
                    // GET using the constructed URL, method exits here
                    document.location.href = href;
                }
            }
            // Value returned to form submit event. If not true, prevents normal form
            // submission.
            return allowPageToSubmitForm;
        }
        query_1.doSubmit = doSubmit;
        function setQueryValue(queryString) {
            yasqe.setValue(queryString.trim());
        }
        query_1.setQueryValue = setQueryValue;
        function getQueryValue() {
            return yasqe.getValue().trim();
        }
        query_1.getQueryValue = getQueryValue;
        function getYasqe() {
            return yasqe;
        }
        query_1.getYasqe = getYasqe;
        function updateYasqe() {
            if ($("#queryLn").val() == "SPARQL") {
                initYasqe();
            }
            else {
                closeYasqe();
            }
        }
        query_1.updateYasqe = updateYasqe;
        function initYasqe() {
            workbench.yasqeHelper.setupCompleters(sparqlNamespaces);
            yasqe = YASQE.fromTextArea(document.getElementById('query'), {
                consumeShareLink: null //don't try to parse the url args. this is already done by the addLoad function below
            });
            //some styling conflicts. Could add my own css file, but not a lot of things need changing, so just do this programmatically
            //first, set the font size (otherwise font is as small as menu, which is too small)
            //second, set the width. YASQE normally expands to 100%, but the use of a table requires us to set a fixed width
            $(yasqe.getWrapperElement()).css({ "fontSize": "14px", "width": "900px" });
            //we made a change to the css wrapper element (and did so after initialization). So, force a manual update of the yasqe instance
            yasqe.refresh();
        }
        function closeYasqe() {
            if (yasqe) {
                //store yasqe value in text area (not sure whether this is desired, but it mimics current behavior)
                //it closes the yasqe instance as well
                yasqe.toTextArea();
                yasqe = null;
            }
        }
    })(query = workbench.query || (workbench.query = {}));
})(workbench || (workbench = {}));
workbench.addLoad(function queryPageLoaded() {
    /**
     * Gets a parameter from the URL or the cookies, preferentially in that
     * order.
     *
     * @param param
     *            the name of the parameter
     * @returns the value of the given parameter, or something that evaluates
                  as false, if the parameter was not found
     */
    function getParameterFromUrlOrCookie(param) {
        var href = document.location.href;
        var elements = href.substring(href.indexOf('?') + 1).substring(href.indexOf(';') + 1).split(decodeURIComponent('%26'));
        var result = '';
        for (var i = 0; elements.length - i; i++) {
            var pair = elements[i].split('=');
            var value = decodeURIComponent(pair[1]).replace(/\+/g, ' ');
            if (pair[0] == param) {
                result = value;
            }
        }
        if (!result) {
            result = workbench.getCookie(param);
        }
        return result;
    }
    function getQueryTextFromServer(queryParam, refParam) {
        $.getJSON('query', {
            action: "get",
            query: queryParam,
            ref: refParam
        }, function (response) {
            if (response.queryText) {
                workbench.query.setQueryValue(response.queryText);
            }
        });
    }
    //Start with initializing our YASQE instance, given that 'SPARQL' is the selected query language
    //(all the following 'set' and 'get' SPARQL query functions require an instantiated yasqe instance
    workbench.query.updateYasqe();
    // Populate the query text area with the value of the URL query parameter,
    // only if it is present. If it is not present in the URL query, then 
    // looks for the 'query' cookie, and sets it from that. (The cookie
    // enables re-populating the text field with the previous query when the
    // user returns via the browser back button.)
    var query = getParameterFromUrlOrCookie('query');
    if (query) {
        var ref = getParameterFromUrlOrCookie('ref');
        if (ref == 'id' || ref == 'hash') {
            getQueryTextFromServer(query, ref);
        }
        else {
            workbench.query.setQueryValue(query);
        }
    }
    workbench.query.loadNamespaces();
    // Trim the query text area contents of any leading and/or trailing 
    // whitespace.
    workbench.query.setQueryValue($.trim(workbench.query.getQueryValue()));
    // Add click handlers identifying the clicked element in a hidden 'action' 
    // form field.
    var addHandler = function (id) {
        $('#' + id).click(function setAction() { $('#action').val(id); });
    };
    addHandler('exec');
    addHandler('save');
    // Add event handlers to the save name field to react to changes in it.
    $('#query-name').bind('keydown cut paste', workbench.query.handleNameChange);
    // Add event handlers to the query text area to react to changes in it.
    $('#query').bind('keydown cut paste', workbench.query.clearFeedback);
    if (workbench.query.getYasqe()) {
        workbench.query.getYasqe().on('change', workbench.query.clearFeedback);
    }
    // Detect if there is no current authenticated user, and if so, disable
    // the 'save privately' option.
    if ($('#selected-user>span').is('.disabled')) {
        $('#save-private').prop('checked', false).prop('disabled', true);
    }
});
//# sourceMappingURL=query.js.map