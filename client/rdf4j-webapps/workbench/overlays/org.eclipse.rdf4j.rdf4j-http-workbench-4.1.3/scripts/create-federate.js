/// <reference path="template.ts" />
/// <reference path="jquery.d.ts" />
// WARNING: Do not edit the *.js version of this file. Instead, always edit the
// corresponding *.ts source in the ts subfolder, and then invoke the
// compileTypescript.sh bash script to generate new *.js and *.js.map files.
workbench.addLoad(function createFederatePageLoaded() {
    function respondToFormState() {
        var memberID = $('input.memberID');
        var enoughMembers = memberID.filter(':checked').length >= 2;
        if (enoughMembers) {
            $('#create-feedback').hide();
        }
        else {
            $('#create-feedback').show();
        }
        var fedID = $('#id').val();
        var validID = /.+/.test(fedID);
        var disable = !(validID && enoughMembers);
        var matchExisting = false;
        // test that fedID not equal any existing id
        memberID.each(function () {
            if (fedID == $(this).attr('value')) {
                disable = true;
                matchExisting = true;
                return false;
            }
        });
        var recurseMessage = $('#recurse-message');
        if (matchExisting) {
            recurseMessage.show();
        }
        else {
            recurseMessage.hide();
        }
        $('input#create').prop('disabled', disable);
    }
    /**
     * Calls another function with a delay of 0 msec. (Workaround for annoying
     * browser behavior.)
     */
    function timeoutRespond() {
        setTimeout(respondToFormState, 0);
    }
    respondToFormState();
    $('input.memberID').on('change', respondToFormState);
    $("input[name='type']").on('change', respondToFormState);
    $('#id').off().on('keydown paste cut', timeoutRespond);
});
//# sourceMappingURL=create-federate.js.map