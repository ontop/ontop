

function reformulate() {
    let current = yasgui.current();
    let yasqe = current.yasqe;
    let sparql = yasqe.getQueryWithValues();

    const requestBody = "query=" + encodeURIComponent(sparql);
    console.log(requestBody);

    fetch("http://localhost:8080/ontop/reformulate", {
        method: 'POST',
        mode: 'cors',
        headers: new Headers({
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
        }),
        body: requestBody,
    })
        .then(response => response.text())
        .then(console.log);
}

btnGroup = document.querySelector(".yasr_btnGroup");
btnGroup.innerHTML += `<button id="ontop" type="button" class="yasr_btn select_rawResponse">Ontop Query Reformulation</button>`;
ontop = document.getElementById("ontop");
ontop.addEventListener('click', reformulate);
