// Functions for the buffer/loading spinner that's displayed while the fetch call loads the data
function showSpinner(spinner) {
    $(spinner).css("visibility", "visible");
    setTimeout(() => {
      $(spinner).css("visibility", "hidden");
    }, 5000);
  }
  
  function hideSpinner(spinner) {
      $(spinner).css("visibility", "hidden");
  }
  
  
  function selectGroup(index, numGroups, elem) {
      $("div").removeClass("chosen");
      $(elem).addClass("chosen")
      for (i = 0; i <= numGroups; i++) {
          x = document.getElementById('yasgui' + i);
          if (i === index) {
              x.style.display = "block";
          } else {
              x.style.display = "none";
          }
      }
  }
  
  $(document).ready(function() {
      // hide main content until data is loaded
      $("#main").css("visibility", "hidden");
  
      // show buffering spinner
      spinner = $("#spinner")
      showSpinner(spinner);
  
      // fetch data form the toml file
      fetch('ontop/portalConfig').then(response => response.json()).then(
          
          config => {
              const $switcher = $("#switcher");
              if ($.isEmptyObject(config)) {
                  // show main content and then hide+remove the spinner after data has been loaded.
                  // Also we hide the switcher since in this case the endpoint was not initialized with a .toml file
                  $switcher.hide();
                  hideSpinner(spinner);
                  $("header").css("visibility", "visible");
                  $("#main").css("visibility", "visible");
                  $(spinner).remove();
              } else {
                  if (config.title) $("#title").text(config.title);
  
                  if (config.tabGroups) {
                      const numGroups = config.tabGroups.length;
                      $switcher.append(`<div class="choice-option" value="0" onclick='selectGroup(0, ${numGroups}, this)'>Playground</div>`)
  
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
                          $switcher.append(`<div class="choice-option" value="0" onclick='selectGroup(${i + 1}, ${numGroups}, this)'> ${group.name} </div>`)
      
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
              // show main content and then hide+remove the spinner after data has been loaded.
              hideSpinner(spinner);
              $("header").css("visibility", "visible");
              $("#main").css("visibility", "visible");
              $(spinner).remove();
          }
          
      );
  
      
  
      const endpointUrl = new Request('sparql').url;
      $('#endpoint').text(endpointUrl);
      var yasgui = YASGUI(document.getElementById("yasgui0"), {
          yasqe: {sparql: {endpoint: endpointUrl}}
      });
  
      
  });