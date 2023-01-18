/// <reference path="template.ts" />
/// <reference path="jquery.d.ts" />

// WARNING: Do not edit the *.js version of this file. Instead, always edit the
// corresponding *.ts source in the ts subfolder, and then invoke the
// compileTypescript.sh bash script to generate new *.js and *.js.map files.

/**
 * Invoked by the change server form in server.xsl.
 */
function changeServer(event) {
    event.preventDefault();
    var form = $(event.target).closest('form')[0];
    var user = $('#server-user').prop('value');
    var password = $('#server-password').prop('value');
    if (user && password) {
        var decoded = user + ':' + password;
        var encoded = window.btoa ? window.btoa(decoded) : decoded;
        $('#server-password').attr('name', 'server-user-password').prop('value', encoded);
    }
    form.submit();
}
