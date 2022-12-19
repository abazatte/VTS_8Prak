function $(id) {
    return document.getElementById(id);
}

function getXMLHttpRequest() {
    // XMLHttpRequest for Firefox, Opera, Safari
    if (window.XMLHttpRequest) {
        return new XMLHttpRequest();
    }
    if (window.ActveObject) { // Internet Explorer
        try { // for IE new
            return new ActiveXObject("Msxml2.XMLHTTP");
        }
        catch (e)  {  // for IE old
            try {
                return new ActiveXObject("Microsoft.XMLHTTP");
            }
            catch (e)  {
                alert("Your browser does not support AJAX!");
                return null;
            }
        }
    }
    return null;
}

function getHtmlHttpRequest(url) {
    var xmlhttp = getXMLHttpRequest();
    console.log("GET via HTML");
    xmlhttp.open("GET", url + "?rtype=HTML", true); //Get-Anfrage wird an Website  und in xmlhttp object gespeichert.
    xmlhttp.onreadystatechange = function() {
        if(xmlhttp.readyState !== 4) {
            $('posters').innerHTML = 'Seite wird geladen ...';
        }
        if(xmlhttp.readyState === 4 && xmlhttp.status === 200) { //readyState = DONE
            $('posters').innerHTML = xmlhttp.responseText;  //die zuvor gestartete Get-Anfrage wird innerhalb des Posters-div geschrieben.
        }
        $('timestamp').innerHTML = new Date().toString(); //die aktuelle Zeit wird ins timestamp-div geschrieben.
    }
    xmlhttp.send(null);
}


function postHttpRequest(url) {
    var xmlhttp = getXMLHttpRequest();
    var content = document.getElementById('contents').value
    xmlhttp.open("POST", url);
    xmlhttp.setRequestHeader("Content-Type", "application/json");
    const java = {"input": content};
    xmlhttp.onreadystatechange = function() {
        if(xmlhttp.readyState === 4 && xmlhttp.status === 200) { //readyState = DONE
            getHtmlHttpRequest(url)
        }
    }
    xmlhttp.send(JSON.stringify(java));
    console.log("POST: " + JSON.stringify(java));
}

function putHttpRequest(url, id) {
    var xmlhttp = getXMLHttpRequest();
    var content = document.getElementById("input_field_" + id).value
    xmlhttp.open("PUT", url);
    xmlhttp.setRequestHeader("Content-Type", "application/json");
    const java = {"text": content, "id": id.toString()};
    xmlhttp.onreadystatechange = function() {
        if(xmlhttp.readyState === 4 && xmlhttp.status === 200) { //readyState = DONE
            getHtmlHttpRequest(url)
        }
    }
    xmlhttp.send(JSON.stringify(java));
    console.log("UPDATE: " + JSON.stringify(java));
}

function deleteHttpRequest(url, id) {
    let xmlhttp = getXMLHttpRequest();
    xmlhttp.open("DELETE", url);
    xmlhttp.setRequestHeader("Content-Type", "application/json");
    var content = document.getElementById("input_field_" + id.toString()).value;
    const java = {"content": content, "id": id.toString()};
    xmlhttp.onreadystatechange = function() {
        if(xmlhttp.readyState === 4 && xmlhttp.status === 200) {
            getHtmlHttpRequest(url);
        }
    }
    xmlhttp.send(JSON.stringify(java));
    console.log("DELETE: " + JSON.stringify(java));
}

/*
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
*/