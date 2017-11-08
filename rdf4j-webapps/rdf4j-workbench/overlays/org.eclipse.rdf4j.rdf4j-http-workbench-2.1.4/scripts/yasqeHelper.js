/// <reference path="template.ts" />
/// <reference path="jquery.d.ts" />
/// <reference path="yasqe.d.ts" />
// WARNING: Do not edit the *.js version of this file. Instead, always edit the
// corresponding *.ts source in the ts sub-folder, and then invoke the
// compileTypescript.sh bash script to generate new *.js and *.js.map files.
var workbench;
(function (workbench) {
    var yasqeHelper;
    (function (yasqeHelper) {
        function setupCompleters(namespaces) {
            var newPrefixCompleterName = "customPrefixCompleter";
            //take the current prefix completer as base, to present our own namespaces for prefix autocompletion
            YASQE.registerAutocompleter(newPrefixCompleterName, function (yasqe, name) {
                //also, auto-append prefixes if needed
                yasqe.on("change", function () {
                    YASQE.Autocompleters.prefixes.appendPrefixIfNeeded(yasqe, name);
                });
                return {
                    bulk: true,
                    async: false,
                    autoShow: true,
                    get: function () {
                        var completerArray = [];
                        for (var key in namespaces) {
                            completerArray.push(key + " <" + namespaces[key] + ">");
                        }
                        return completerArray;
                    },
                    isValidCompletionPosition: function () {
                        return YASQE.Autocompleters.prefixes.isValidCompletionPosition(yasqe);
                    },
                    preProcessToken: function (token) {
                        return YASQE.Autocompleters.prefixes.preprocessPrefixTokenForCompletion(yasqe, token);
                    }
                };
            });
            //i.e., disable the property/class autocompleters
            YASQE.defaults.autocompleters = [newPrefixCompleterName, "variables"];
        }
        yasqeHelper.setupCompleters = setupCompleters;
    })(yasqeHelper = workbench.yasqeHelper || (workbench.yasqeHelper = {}));
})(workbench || (workbench = {}));
//# sourceMappingURL=yasqeHelper.js.map