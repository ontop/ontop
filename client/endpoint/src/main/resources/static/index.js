const endpointUrl = new Request('sparql').url;
$('#endpoint').text(endpointUrl);
var yasgui = YASGUI(document.getElementById("yasgui0"), {
    yasqe: {sparql: {endpoint: endpointUrl}}
});

fetch('ontop/portalConfig')
    .then(response => response.json())
    .then(
        config => {
            const $switcher = $("#switcher");
            if ($.isEmptyObject(config)) {
                $switcher.hide();
            } else {
                if (config.title) $("#title").text(config.title);

                if (config.tabGroups) {
                    const numGroups = config.tabGroups.length;
                    $switcher.append(`<label><input type='radio' name='group' value='0' onclick='selectGroup(0, ${numGroups})' checked/> Playground </label>`);
                    $switcher.append("<span> | Predefined Queries: </span>");

                    for (let i = 0; i < numGroups; i++) {
                        const yId = `yasgui${i + 1}`;
                        $('#yasguis').append(`<div id='${yId}' class="predefined"></div>`);
                        const y = YASGUI(document.getElementById(yId), {
                            yasqe: {
                                sparql: {endpoint: endpointUrl},
                                persistent: null
                            }
                        });
                        let group = config.tabGroups[i];
                        $switcher.append(
                            `<label>  <input type='radio' name='group' value='0' onclick='selectGroup(${i + 1}, ${numGroups})'/>  ${group.name} </label>`);
                        for (let tabId in y.tabs) {
                            y.closeTab(tabId);
                        }
                        for (let t of group.tabs) {
                            let tab = y.addTab();
                            tab.rename(t.name);
                            tab.setQuery(t.query)
                        }
                        y.selectTab(Object.keys(y.tabs)[0]);
                    }
                    selectGroup(0, numGroups);
                }
            }
        }
    );


function selectGroup(index, numGroups) {
    for (i = 0; i <= numGroups; i++) {
        x = document.getElementById('yasgui' + i);
        if (i === index) {
            x.style.display = "block";
        } else {
            x.style.display = "none";
        }
    }
}
