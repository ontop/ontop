const TAB_OWNER = 'ontop-portal';

function waitForComponent(el) {
    if (el && typeof el.componentOnReady === 'function') {
        return el.componentOnReady();
    }
    return Promise.resolve();
}

function buildComponentConfig(suffix, overrides) {
    const baseConfig = {
        endpoint: window.endpointUrl,
        componentId: `ontop-${suffix}`,
        clearState: true,
        copyEndpointOnNewTab: true,
        method: 'POST',
        headers: () => ({})
    };
    return Object.assign(baseConfig, overrides || {});
}

function createHost(index, extraClasses) {
    const host = document.createElement('div');
    host.id = `yasgui${index}`;
    host.style.display = 'none';
    host.classList.add('yasgui-host');
    if (Array.isArray(extraClasses)) {
        extraClasses.forEach(cls => host.classList.add(cls));
    }
    document.getElementById('yasguis').appendChild(host);
    return host;
}

function getGroupDefinition(index) {
    if (!window.portalConfig || !Array.isArray(window.portalConfig.tabGroups)) {
        return null;
    }
    return window.portalConfig.tabGroups[index - 1] || null;
}

async function seedPredefinedTabs(component, tabs) {
    await waitForComponent(component);
    if (!Array.isArray(tabs) || tabs.length === 0) {
        return;
    }

    for (const tab of tabs) {
        await component.openTab({
            queryName: tab.name,
            query: tab.query,
            owner: TAB_OWNER
        });
    }

    const first = tabs[0];
    await component.openTab({
        queryName: first.name,
        query: first.query,
        owner: TAB_OWNER
    });
}

async function ensureGroupLoaded(index) {
    const hostId = `yasgui${index}`;
    const isPlayground = index === 0;
    let host = document.getElementById(hostId);
    const alreadyInitialized = host && host.querySelector('ontotext-yasgui');
    if (alreadyInitialized) {
        return;
    }

    if (!host) {
        host = createHost(index, isPlayground ? [] : ['predefined']);
    }

    const component = document.createElement('ontotext-yasgui');
    component.classList.add('yasgui-component');

    const overrides = {};
    if (isPlayground && window.portalConfig && window.portalConfig.defaultTab && window.portalConfig.defaultTab.query) {
        overrides.initialQuery = window.portalConfig.defaultTab.query;
    }

    component.config = buildComponentConfig(isPlayground ? 'playground' : `group-${index}`, overrides);
    host.appendChild(component);

    if (isPlayground) {
        await waitForComponent(component);
        if (overrides.initialQuery) {
            await component.setQuery(overrides.initialQuery);
        }
        return;
    }

    const group = getGroupDefinition(index);
    await seedPredefinedTabs(component, group ? group.tabs : []);
}

function withSpinner(promise) {
    $("#spinner").css("visibility", "visible");
    return promise.finally(() => $("#spinner").css("visibility", "hidden"));
}

async function selectGroup(index, numGroups, elem) {
    $(".choice-option").removeClass("chosen");
    $(elem).addClass("chosen");

    if (!document.getElementById(`yasgui${index}`)) {
        try {
            await withSpinner(ensureGroupLoaded(index));
        } catch (error) {
            console.error('Failed to initialize ontotext YASGUI component', error);
        }
    }

    for (let i = 0; i <= numGroups; i++) {
        const container = document.getElementById(`yasgui${i}`);
        if (container) {
            container.style.display = i === index ? 'block' : 'none';
        }
    }
}

$(async () => {
    window.portalConfig = {};
    const endpointUrl = new Request('sparql').url;
    window.endpointUrl = endpointUrl;
    $('#endpoint').text(endpointUrl);

    try {
        const response = await fetch('ontop/portalConfig');
        if (!response.ok) {
            throw new Error('Failed to fetch portal configuration');
        }
        const config = await response.json();
        window.portalConfig = config || {};
        const $switcher = $("#switcher");
        const hasGroups = Array.isArray(window.portalConfig.tabGroups) && window.portalConfig.tabGroups.length > 0;

        if (config && config.title) {
            $("#title").text(config.title);
        }

        await withSpinner(ensureGroupLoaded(0));

        if (hasGroups) {
            const numGroups = window.portalConfig.tabGroups.length;
            $switcher.append(`<div class="choice-option" id="select0" onclick='selectGroup(0, ${numGroups}, this)'>Playground</div>`);
            window.portalConfig.tabGroups.forEach((group, idx) => {
                $switcher.append(`<div class="choice-option" id='select${idx + 1}' onclick='selectGroup(${idx + 1}, ${numGroups}, this)'> ${group.name} </div>`);
            });
            selectGroup(0, numGroups, "#select0");
        } else {
            $switcher.hide();
            const host = document.getElementById('yasgui0');
            if (host) {
                host.style.display = 'block';
            }
        }
    } catch (error) {
        console.error('Unable to load portal configuration; falling back to playground only.', error);
        await withSpinner(ensureGroupLoaded(0));
        const host = document.getElementById('yasgui0');
        if (host) {
            host.style.display = 'block';
        }
        $("#switcher").hide();
    }
});


