/// <reference path="template.ts" />
/// <reference path="jquery.d.ts" />
// WARNING: Do not edit the *.js version of this file. Instead, always edit the
// corresponding *.ts source in the ts subfolder, and then invoke the
// compileTypescript.sh bash script to generate new *.js and *.js.map files.
var workbench;
(function (workbench) {
    var namespaces;
    (function (namespaces) {
        /**
         * Invoked by #prefix-select element in namespaces.xsl.
         */
        function updatePrefix() {
            var select = $('#prefix-select');
            $('#prefix').val(select.find('option:selected').text());
            $('#namespace').val(select.val());
        }
        namespaces.updatePrefix = updatePrefix;
    })(namespaces = workbench.namespaces || (workbench.namespaces = {}));
})(workbench || (workbench = {}));
//# sourceMappingURL=namespaces.js.map