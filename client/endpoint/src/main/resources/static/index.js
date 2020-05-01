// https://stackoverflow.com/questions/30987218/update-progressbar-in-each-loop/31654481
function delayedLoop(collection, delay, callback, context) {
    context = context || null;

    var i = 0,
        nextInteration = function() {
            if (i === collection.length) {
                return;
            }
            callback.call(context, collection[i], i);
            i++;
            setTimeout(nextInteration, delay);
        };

    nextInteration();
}


async function selectGroup(index, numGroups, elem) {

    //delayedLoop(["dummy","dummy"], 1, function(t, idx) {
        $("div").removeClass("chosen");
        $(elem).addClass("chosen");
    //});


    const yId = 'yasgui' + index;
    x = document.getElementById(yId);
    if (x) {
        x.style.display = "block";
    } else {
        const $main = $("#main");
        //$main.css("visibility","hidden");
        //showSpinner("#spinner");
        $(yId).addClass("chosen");
        await createYasgui(index);
        //hideSpinner("#spinner");
     //   $main.css("visibility","visible")
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
    //const nTabs = group.tabs.length;
    //const p = document.getElementsByTagName("progress")[0];

    delayedLoop(group.tabs, 1, function(t, idx) {
        //p.max = nTabs;
        //console.log(idx)
        //p.value = idx+1;
        //$(yId).addClass("chosen");
        let tab = y.addTab();
        tab.rename(t.name);
        tab.setQuery(t.query);
        //console.log(t.name, new Date().toLocaleTimeString());
    });

    y.selectTab(Object.keys(y.tabs)[0]);
    return yId;
}

$(document).ready(function () {
    // fetch data form the toml file
    fetch('ontop/portalConfig')
        .then(response => response.json())
        .then(
            config => {
                window.portalConfig = config;
                // console.log("ontop/portalConfig fetched", new Date().toLocaleTimeString());
                const $switcher = $("#switcher");
                if ($.isEmptyObject(config)) {
                    // Also we hide the switcher since in this case the endpoint was not initialized with a .toml file
                    $switcher.hide();
                } else {
                    if (config.title) $("#title").text(config.title);

                    if (config.tabGroups) {
                        let numGroups = config.tabGroups.length;
                        $switcher.append(`<div class="choice-option" id="select0" onclick='selectGroup(0, ${numGroups}, this)'>Playground</div>`);
                        for (let i = 0; i < numGroups; i++) {
                            let group = config.tabGroups[i];
                            $switcher.append(`<div class="choice-option" id='select${i + 1}' onclick='selectGroup(${i + 1}, ${numGroups}, this)'> ${group.name} </div>`)
                        }

                        selectGroup(0, numGroups, "#select0");
                    }
                }

                const endpointUrl = new Request('sparql').url;
                window.endpointUrl = endpointUrl;
                $('#endpoint').text(endpointUrl);
                var yasgui = YASGUI(document.getElementById("yasgui0"), {
                    yasqe: {sparql: {endpoint: endpointUrl}}
                });
            }
        );



});


