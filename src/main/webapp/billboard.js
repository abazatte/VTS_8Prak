function $(id) {
    return document.getElementById(id);
}

async function getHttpRequest(url, firstGet) {
    var urlWithParam = url + '?firstGet=' + firstGet;
    var warning = "";
    console.log('GET',urlWithParam);
    let response = await fetch(urlWithParam);
    if (response.status != 200) {
        warning = "(Letzte Synchronisierung fehlgeschlagen)";
    } else {
        var jsonObj = await response.json();
        var ip = jsonObj.owner;
        console.log(jsonObj);
        for (var i = 0; i < 10; i++) {
            var id = jsonObj.entries[i]['elementID'];
            $(id).value = jsonObj.entries[i]['text'];
            if (ip == jsonObj.entries[i]['owner']) {
                $(id).style.backgroundColor = "white";
                $(id).readOnly = false;
                $('put' + i).style.display = "block";
                $('delete' + i).style.display = "block";
            } else {
                $(id).style.backgroundColor = "#eeeeee";
                $(id).readOnly = true;
                $('put' + i).style.display = "none";
                $('delete' + i).style.display = "none";
            }
        }
    }
    $('timestamp').innerHTML = 'Letzer poll: ' + new Date().toString() + warning;
    await getHttpRequest(url, false);
}
function postHttpRequest(url) {
    var eintrag = 'name=';
    eintrag += document.getElementById("contents").value;
    console.log("POST", eintrag);
    fetch(url,{
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: eintrag
    });
}

function putHttpRequest(url, id) {
    var plakatName = 'name=';
    var entryID = 'id=' + id;
    var eintrag =  document.getElementById('input_field_' + id).value;
    plakatName += eintrag;
    console.log("PUT", plakatName, entryID);
    var params = entryID + '&' + plakatName;
    fetch(url,{
        method: 'PUT',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: params
    });
}


function deleteHttpRequest(url, id) {
    var entryID = 'id=' + id;
    console.log("DELETE", entryID, url);
    fetch(url,{
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: entryID
    });
}

getHttpRequest('BillBoardServer', true);