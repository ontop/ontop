/// <reference path="template.ts" />
/// <reference path="jquery.d.ts" />
/// <reference path="paging.ts" />

// WARNING: Do not edit the *.js version of this file. Instead, always edit the
// corresponding *.ts source in the ts subfolder, and then invoke the
// compileTypescript.sh bash script to generate new *.js and *.js.map files.

workbench.addLoad(function() {
    function removeDuplicates(self:string) {
        function textContent(element:HTMLElement) {
            return $.trim(element.innerText || element.textContent);
        }

        var lists = document.getElementsByTagName('ul');
        for (var i = lists.length - 1; i + 1; i--) {
            var items = lists[i].getElementsByTagName('li');
            for (var j = items.length - 1; j; j--) {
                var text = textContent(items[j]);
                if (items[j].innerHTML == items[j - 1].innerHTML || text == self) {
                    items[j].parentNode.removeChild(items[j]);
                }
            }

            text = textContent(items[0]);
            if (text == self) {
                items[0].parentNode.removeChild(items[0]);
            }

            if (items.length == 0) {
                lists[i].parentNode.parentNode.removeChild(lists[i].parentNode);
            }
        }
    }

    // Populate parameters
    var elements = workbench.getQueryStringElements();
    var resource = $('#resource');
    var suffix = '_explore';
    var limit_param = workbench.paging.LIMIT + suffix;
    var limit_id = workbench.paging.LIM_ID + suffix;
    var limit_param_found = false;
    for (var i = 0; elements.length - i; i++) {
        var pair = elements[i].split('=');
        var value = decodeURIComponent(pair[1]).replace(/\+/g, ' ');
        if ('resource' == pair[0]) {
            resource.val(value);
        }
        else if (limit_param == pair[0]) {
            $(limit_id).val(value);
            limit_param_found = true;
        }
    }
    if (!limit_param_found){
        var limit_cookie = workbench.getCookie(limit_param);
        if (limit_cookie) {
            $(limit_id).val(limit_cookie);
        }
    }
    var explore = 'explore';
    workbench.paging.correctButtons(explore);
    var content = document.getElementById('content');
    var h1 = content.getElementsByTagName('h1')[0];
    var rvalue=resource.val();
    if (rvalue) {
        h1.appendChild(document.createTextNode(' (' + rvalue + ')'));
        removeDuplicates(rvalue);
        var limit = workbench.paging.getLimit(explore);

        // Modify title to reflect total_result_count cookie
        var total_result_count = workbench.paging.getTotalResultCount();
        var have_total_count = (total_result_count > 0);
        var offset = limit == 0 ? 0 : workbench.paging.getOffset();
        var first = offset + 1;
        var last = limit == 0 ? total_result_count : offset + limit;

        // Truncate range if close to end.
        last = have_total_count ? Math.min(total_result_count, last) : last;
        var newHTML = '(' + first + '-' + last;
        if (have_total_count) {
            newHTML = newHTML + ' of ' + total_result_count;
        }
        h1.appendChild(document.createTextNode(newHTML + ')'));
    }
    workbench.paging.setShowDataTypesCheckboxAndSetChangeEvent();
});