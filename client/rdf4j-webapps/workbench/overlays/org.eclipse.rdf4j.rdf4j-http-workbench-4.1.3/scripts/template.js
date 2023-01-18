// WARNING: Do not edit the *.js version of this file. Instead, always edit the
// corresponding *.ts source in the ts subfolder, and then invoke the
// compileTypescript.sh bash script to generate new *.js and *.js.map files.
var workbench;
(function (workbench) {
    // The following is to allow composed XSLT style sheets to each add
    // functions to the window.onload event.
    function chain(args) {
        return function () {
            for (var i = 0; i < args.length; i++) {
                args[i]();
            }
        };
    }
    // Note that the way this is currently constructed, functions added with
    // addLoad() will be executed in the order that they were added.
    //
    // @see
    // http://onwebdevelopment.blogspot.com/2008/07/chaining-functions-in-javascript.html
    // @param fn
    // function to add
    function addLoad(fn) {
        window.onload = typeof (window.onload) == 'function' ? chain([
            window.onload, fn]) : fn;
    }
    workbench.addLoad = addLoad;
    /**
     * Retrieves the value of the cookie with the given name.
     *
     * @param {String} name The name of the cookie to retrieve.
     * @returns {String} The value of the given cookie, or an empty string if it
     *          doesn't exist.
     */
    function getCookie(name) {
        var cookies = document.cookie.split(';');
        var rval = '';
        for (var i = 0; i < cookies.length; i++) {
            var cookie = cookies[i];
            var eq = cookie.indexOf('=');
            if (name == cookie.substr(0, eq).replace(/^\s+|\s+$/g, '')) {
                rval = decodeURIComponent(cookie.substr(eq + 1).replace(/\+/g, '%20'));
                break;
            }
        }
        return rval;
    }
    workbench.getCookie = getCookie;
    /**
     * Parses workbench URL query strings into processable arrays.
     *
     * @returns an array of the 'name=value' substrings of the URL query string
     */
    function getQueryStringElements() {
        var href = document.location.href;
        return href.substring(href.indexOf('?') + 1).split(decodeURIComponent('%26'));
    }
    workbench.getQueryStringElements = getQueryStringElements;
    /**
     * Utility method for assembling the query string for a request URL.
     *
     * @param sb
     *            string buffer, actually an array of strings to be joined later
     * @param id
     *            name of parameter to add, also the id of the document element
     *            to get the value from
     */
    function addParam(sb, id) {
        sb[sb.length] = id + '=';
        var tag = document.getElementById(id);
        sb[sb.length] = tag.type == 'checkbox' ? String(tag.checked) :
            encodeURIComponent(tag.value);
        sb[sb.length] = '&';
    }
    workbench.addParam = addParam;
})(workbench || (workbench = {}));
/**
 * Code to run when the document loads: eliminate the 'noscript' warning
 * message, and display an unauthenticated user properly.
 */
workbench
    .addLoad(function () {
    document.getElementById('noscript-message').style.display = 'none';
    var encoded = workbench.getCookie("server-user-password");
    var decoded = encoded && window.atob ? window.atob(encoded) : encoded;
    var user = decoded && decoded.substring(0, decoded.indexOf(':'));
    if (!user || user == '""') {
        user = '<span class="disabled">None</span>';
    }
    var selectedUser = document.getElementById('selected-user');
    selectedUser.innerHTML = user;
});
//# sourceMappingURL=template.js.map