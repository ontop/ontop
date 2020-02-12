// Functions for the buffer/loading spinner that's displayed while the fetch call loads the data
function showSpinner(spinner) {
    $(spinner).css("visibility", "visible");
    // setTimeout(() => {
    //     $(spinner).css("visibility", "hidden");
    // }, 5000);
}

function hideSpinner(spinner) {
    $(spinner).css("visibility", "hidden");
}


async function selectGroup(index, numGroups, elem) {
    $("div").removeClass("chosen");
    $(elem).addClass("chosen");

    const yId = 'yasgui' + index;
    x = document.getElementById(yId);
    if (x) {
        x.style.display = "block";
    } else {
        const $main = $("#main");
        $main.css("visibility","hidden");
        showSpinner("#spinner");
        await createYasgui(index);
        $(yId).addClass("chosen");
        hideSpinner("#spinner");
        $main.css("visibility","visible")
    }

    for (i = 0; i <= numGroups; i++) {
        x = document.getElementById('yasgui' + i);
        if (x) {
            if (i === index) {
                x.style.display = "block";
            } else {
                x.style.display = "none";
            }
        }
    }
}

async function createYasgui(i) {
    let group = window.portalConfig.tabGroups[i - 1];
    const yId = `yasgui${i}`;
    $('#yasguis').append(`<div id='${yId}' class="predefined"></div>`);
    const y = YASGUI(document.getElementById(yId), {
        yasqe: {
            sparql: {endpoint: window.endpointUrl},
            persistent: null
        }
    });

    for (let tabId in y.tabs) {
        y.closeTab(tabId);
    }
    for (let t of group.tabs) {
        let tab = y.addTab();
        tab.rename(t.name);
        tab.setQuery(t.query);
        // console.log(t.name, new Date().toLocaleTimeString());
    }
    y.selectTab(Object.keys(y.tabs)[0]);
    return yId;
}

$(document).ready(function () {
    // console.log("enter $(document).ready()", new Date().toLocaleTimeString());

    // hide main content until data is loaded
    $("#main").css("visibility", "hidden");

    // show buffering spinner
    spinner = $("#spinner");
    showSpinner(spinner);

    // fetch data form the toml file
    fetch('ontop/portalConfig')
        .then(response => response.json())
        .then(
            config => {
                window.portalConfig = config;
                // console.log("ontop/portalConfig fetched", new Date().toLocaleTimeString());
                const $switcher = $("#switcher");
                if ($.isEmptyObject(config)) {
                    // show main content and then hide+remove the spinner after data has been loaded.
                    // Also we hide the switcher since in this case the endpoint was not initialized with a .toml file
                    $switcher.hide();
                    hideSpinner(spinner);
                    $("header").css("visibility", "visible");
                    $("#main").css("visibility", "visible");
                    //$(spinner).remove();
                } else {
                    if (config.title) $("#title").text(config.title);

                    if (config.tabGroups) {
                        let numGroups = config.tabGroups.length;
                        $switcher.append(`<div class="choice-option" id="select0" onclick='selectGroup(0, ${numGroups}, this)'>Playground</div>`);
                        //numGroups=0;
                        for (let i = 0; i < numGroups; i++) {
                            let group = config.tabGroups[i];
                            $switcher.append(`<div class="choice-option" id='select${i + 1}' onclick='selectGroup(${i + 1}, ${numGroups}, this)'> ${group.name} </div>`)
                        }
                        // for (let i = 0; i < numGroups; i++) {
                        //     let group = config.tabGroups[i];
                        //     let {tabId, t} = createYasgui(i, endpointUrl, group);
                        // }
                        selectGroup(0, numGroups, "#select0");
                    }
                }

                // show main content and then hide+remove the spinner after data has been loaded.
                //hideSpinner(spinner);
                $("header").css("visibility", "visible");
                $("#main").css("visibility", "visible");
                //$(spinner).remove();
            }
        );

    const endpointUrl = new Request('sparql').url;
    window.endpiontUrl = endpointUrl;
    $('#endpoint').text(endpointUrl);
    var yasgui = YASGUI(document.getElementById("yasgui0"), {
        yasqe: {sparql: {endpoint: endpointUrl}}
    });

});
