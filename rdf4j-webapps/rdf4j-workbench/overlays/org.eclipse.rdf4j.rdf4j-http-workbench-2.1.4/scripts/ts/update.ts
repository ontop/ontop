/// <reference path="template.ts" />
/// <reference path="jquery.d.ts" />
/// <reference path="yasqeHelper.ts" />

// WARNING: Do not edit the *.js version of this file. Instead, always edit the
// corresponding *.ts source in the ts subfolder, and then invoke the
// compileTypescript.sh bash script to generate new *.js and *.js.map files.

module workbench {

    export module update {

        // Need to declare YASQE library for typescript compilation.
        declare var YASQE:any;
        declare var namespaces:{string:string};
        var yasqe:any = null;

        export function initYasqe() {
            workbench.yasqeHelper.setupCompleters(namespaces);
            yasqe = YASQE.fromTextArea(document.getElementById('update'), {
                createShareLink: function () {
                    return {update: yasqe.getValue()};
                },
                consumeShareLink: function (yasqe:any, args:any) {
                    if (args.update) yasqe.setValue(args.update)
                },

                // This way, we don't conflict with the YASQE editor of the
                // regular query interface, and we show the most recent
                // -update- query.
                persistent: "update"

            });

            // Some styling conflicts. Could add my own css file, but not a
            // lot of things need changing, so just do this programmatically.
            // First, set the font size (otherwise font is as small as menu,
            // which is too small). Second, set the width. YASQE normally
            // expands to 100%, but the use of a table requires us to set a
            // fixed width.
            $(yasqe.getWrapperElement()).css({
                "fontSize": "14px",
                "width": "900px"
            });

            // We made a change to the css wrapper element (and did so after
            // initialization). So, force a manual update of the yasqe
            // instance.
            yasqe.refresh();

            // If the text area we instantiated YASQE on has no query val,
            // then show a regular default update query.
            if (yasqe.getValue().trim().length == 0) {
                yasqe.setValue('INSERT DATA {\n\t<http://exampleSub> '+
                    '<http://examplePred> <http://exampleObj> .\n}');
            }
        }

        /**
         * Invoked upon form submission.
         *
         * @returns {boolean} true, always
         */
        export function doSubmit() {
            // Save yasqe content to text area.
            if (yasqe) {
                yasqe.save();
            }
            return true;
        }
    }
}

workbench.addLoad(function updatePageLoaded() {
    workbench.update.initYasqe();
});
