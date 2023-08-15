// https://stackoverflow.com/questions/30987218/update-progressbar-in-each-loop/31654481
function delayedLoop(collection, delay, callback, context) {
    context = context || null;

    let i = 0;
    const nextIteration = function () {
        if (i === collection.length) {
            return;
        }
        callback.call(context, collection[i], i);
        i++;
        setTimeout(nextIteration, delay);
    };

    nextIteration();
}


function selectGroup(index, numGroups, elem) {
    $("div").removeClass("chosen");
    $(elem).addClass("chosen");

    const yId = 'yasgui' + index;
    let x = document.getElementById(yId);
    if (x) {
        x.style.display = "block";
    } else {
        delayedLoop([0, 1, 2], 10, (_, idx) => {
            if (idx === 0) {
                $("#spinner").css("visibility", "visible");
            } else if (idx === 1) {
                createYasgui(index);
            } else /* idx === 2*/ {
                $("#spinner").css("visibility", "hidden");
            }
        })
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

function createYasgui(i) {
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

    delayedLoop(group.tabs, 1, (t, _) => {
        let tab = y.addTab();
        tab.rename(t.name);
        tab.setQuery(t.query);
        //console.log(t.name, new Date().toLocaleTimeString());
    });

    y.selectTab(Object.keys(y.tabs)[0]);
    return yId;
}

$(() => {
    // fetch data form the toml file
    fetch('ontop/portalConfig')
        .then(response => response.json())
        .then(
            config => {
                window.portalConfig = config;
                // console.log("ontop/portalConfig fetched", new Date().toLocaleTimeString());
                const $switcher = $("#switcher");
                if ($.isEmptyObject(config)) {
                    // we hide the switcher since in this case the endpoint was not initialized with a .toml file
                    $switcher.hide();
                } else {
                    if (config.title) $("#title").text(config.title);

                    // If a portal file is provided with default query provided, set the default tab
                    if (config.defaultTab && config.defaultTab.query) 
                        YASGUI.YASQE.defaults.value = config.defaultTab.query;

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
                window.yasgui = YASGUI(document.getElementById("yasgui0"), {
                    yasqe: {sparql: {endpoint: endpointUrl}}
                });
            }
        );
});


