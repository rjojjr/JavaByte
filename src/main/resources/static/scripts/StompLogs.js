var context_path = "${pageContext.request.contextPath}";
var socket = new SockJS('/ws');
var stompClient = Stomp.over(socket);
var sessionId = "";

var username = "null";

var users = '';

var mfirst = true;

var tablename = "";

function setUname(Uname){
    username = Uname;
}

function setTablename(table){
    tablename = table;
    showTabS("#filters");
    var fHead = document.getElementById("filter");
    var rHead = document.getElementById("result");
    fHead.innerText = table + " Log Filters";
    rHead.innerText = table + " Results";
}

function deleteResults(){
    var table = document.getElementById("userResult");
    var rowCount = table.rows.length;
    for (var x = rowCount - 1; x > 0; x--) {
        table.deleteRow(x);
    }
}

function populateIPSearch(result) {
    var table = document.getElementById("userResult");
    var rowCount = table.rows.length;
    var strings = result.split(",");
    for (var x = rowCount - 1; x > 0; x--) {
        table.deleteRow(x);
    }

    if(result == "No Result"){
        var newRow = table.insertRow(1);
        var cell = newRow.insertCell(ccount);
        cell.innerText = result;
    }else{
        var count = 1
        var userid = "";
        for (var i = 0; i < strings.length; i++) {
            var string = strings[i];
            var cells = string.split(";");
            //var row = string.split(":");
            var newRow = table.insertRow(i +  1);
            var ccount = 0;
            var cellS;
            for(cellS in cells){
                var temp = cells[cellS];
                var cell = newRow.insertCell(ccount);
                cell.innerText = temp;
                ccount++;
            }
        }
    }
}

function searchLogs() {
    var first = true;
    var searchTerms = "";
    var table = document.getElementById("filterTable");
    var sdateEl = document.getElementById("sdate");
    var sdate = sdateEl.value;
    var edateEl = document.getElementById("edate");
    var edate = edateEl.value;
    if(sdate !== ""){
        if(first){
            searchTerms = "sdate:" + sdate ;
            first = false;
        }else{
            searchTerms+= ",sdate:" + sdate;
        }
    }
    if(edate !== ""){
        if(first){
            searchTerms = "edate:" + edate;
            first = false;
        }else{
            searchTerms+= ",edate:" + edate;
        }
    }
    sendSearch(searchTerms);
    showTabS("#ip");
}

var connect_callback = function() {
    // called back after the client is connected and authenticated to the STOMP server
    stompClient.subscribe('/user/queue/notify', function (msgOut) {
        var msg = msgOut.body;
        msg = msg.split("%");
        //0 = kick, 1 = device cert, 3 = display msg
        if(msg[0] == 1){
            writeCert(msg[1]);
        }
        if(msg[0] == 2){
            location.href = "/?user=" + username;
        }
        if (msg[0] == 3) {
            var msg = 'Message From: ' + msg[1] + '\n\n' + msg[2];
            alert(msg);
        }
        if(msg[0] == 0){
            alert("You have been kicked from your session by administrator");
            location.href = "/"
        }
        if(msg[0] == 10){
            if(msg[1] == "No Result"){
                document.getElementById("empty").innerText = "No Results";
            }else{
                populateIPSearch(msg[1]);
            }
        }
        if(msg[0] == 11){
            document.getElementById("empty").innerText = msg[1];
        }
        if (msg[0] == "deny") {
            var msg = 'Message From: ' + msg[1] + '\n\n' + msg[2];
            alert(msg);
            location.href = "/";
        }
        if (msg[0] == "dump") {
            location.href = "/logs";
        }
    });
    sendUsername();
};

stompClient.connect({}, function (frame) {
    var suffix = frame.headers['queue-suffix'];
    var url = stompClient.ws._transport.url;
    sessionId = /\/([^\/]+)\/websocket/.exec(socket._transport.url)[1];
    //sessionId = /\/([^\/]+)\/websocket/.exec(socket._transport.url)[1];
    url = url.replace(
        "/ws://localhost:8080/messagesocket/specific/message//ig",  "");
    url = url.replace("//websocket/ig", "");
    url = url.replace(/^[0-9]+\//, "");
    console.log("Your current session is: " + sessionId);
    connect_callback()
});

function disconnect() {
    if(stompClient != null) {
        stompClient.disconnect();
    }
    console.log("Disconnected");
}

function sendUsername() {
    stompClient.send("/app/register", {},
        JSON.stringify({'username':username, 'session':sessionId}));
}

function sendDeviceCert(cert) {
    stompClient.send("/app/device/cert", {},
        JSON.stringify({'cert':cert, 'user':username}));
}

function sendSearch(cert) {
    stompClient.send("/app/logs/search", {},
        JSON.stringify({'query':cert, 'username':username, 'sessionid':sessionId, 'tablename':tablename}));
}

function dumpLog(cert) {
    var dump = confirm("Are you sure you want to dump these logs?");
    if(dump){
        stompClient.send("/app/logs/dump", {},
            JSON.stringify({'query':"", 'username':username, 'sessionid':sessionId, 'tablename':cert}));
    }else{
        alert("Dump cancelled")
    }
}

function writeCert(cert) {
    if (typeof (Storage) !== "undefined") {
        // Code for localStorage
        localStorage.setItem("cert", cert);
    }
}

function checkStatus() {
    stompClient.send("/app/httpstatus", {},
        JSON.stringify({'username':username, 'session':sessionId}));
}

function getCert() {
    if (typeof (Storage) !== "undefined") {
        // Code for localStorage
        if (localStorage.length == 0) {
            sendDeviceCert(0)
        } else {
            if (localStorage.getItem("cert") > "1") {
                sendDeviceCert(localStorage.getItem("cert"))
            }
        }
    } else {
        sendDeviceCert(-1);
    }
}