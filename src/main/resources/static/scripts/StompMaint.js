var context_path = "${pageContext.request.contextPath}";
var socket = new SockJS('/ws');
var stompClient = Stomp.over(socket);
var sessionId = "";

var username = "null";

var varName = "";

var first = 0, firstt = 0;

function setUname(Uname) {
    username = Uname;
}

function populateSysTable(strings) {

    var count = 1;

    if (first == 0) {
        table = document.getElementById("system");
        first = 1;
        for (var i = 0; i < strings.length; i++) {
            var string = strings[i];
            var row = string.split(":");
            if (row[0] == undefined || row[1] == undefined) {

            } else {
                var newRow = table.insertRow(count);
                var cell = newRow.insertCell(0);
                cell.innerText = row[1];
                var cel2 = newRow.insertCell(0);
                cel2.innerText = row[0];
                count++;
            }
        }
    } else {
        for (var i = 0; i < strings.length; i++) {
            var string = strings[i];
            var row = string.split(":");
            if (row[0] == undefined || row[1] == undefined) {

            } else {
                var cel = document.getElementById("system").rows[count].cells;
                var cell = cel[0];
                cell.innerText = row[0];
                var cel2 = cel[1];
                cel2.innerText = row[1];
                count++;
            }
        }
    }
    count = 1;
}

function populateTabTable(strings) {

    var count = 1;
    if (firstt == 0) {
        table = document.getElementById("table");
        firstt = 1;
        for (var i = 0; i < strings.length; i++) {
            var string = strings[i];
            var row = string.split(":");
            if (row[0] == undefined || row[1] == undefined) {

            } else {
                var newRow = table.insertRow(count);
                var cell = newRow.insertCell(0);
                cell.innerText = row[1];
                var cel2 = newRow.insertCell(0);
                cel2.innerText = row[0];
                count++;
            }
        }
    } else {
        for (var i = 0; i < strings.length; i++) {
            var string = strings[i];
            var row = string.split(":");
            if (row[0] == undefined || row[1] == undefined) {

            } else {
                var cel = document.getElementById("table").rows[count].cells;
                var cell = cel[0];
                cell.innerText = row[0];
                var cel2 = cel[1];
                cel2.innerText = row[1];
                count++;
            }
        }
    }
    count = 1;
}

function varUpdate() {
    if (varName != "") {
        var query = "";
        table = document.getElementById("vartable");
        var rows = table.rows;
        for (var i = 0; i < rows.length; i++) {
            var row = rows[i];
            var cell1 = row.cells[0];
            var cell1s = cell1.innerText;
            var cell2 = row.cells[1];
            var cell2s = row.cells[1].getElementsByTagName("input")[0];
            var val = cell2s.value;
            if (query == "") {
                query = cell1s + ":" + val
            } else {
                query += "," + cell1s + ":" + val
            }
        }
        sendVars(query);
    } else {
        alert("Select a configuration...");
    }
}

function populateVarTable(results) {
    table = document.getElementById("vartable");
    deleteTable(table);
    var rows = results.split(",");
    for (var i = 0; i < rows.length; i++) {
        var newRow = table.insertRow(i);
        var cells = rows[i].split(':');
        var cell1 = newRow.insertCell(0);
        cell1.innerText = cells[0];
        var cell2 = newRow.insertCell(1);
        cell2.innerHTML = '<input class="filterInput" type="text" value="' + cells[1] + '"/>';
    }
}

function deleteTable(table) {
    var rowCount = table.rows.length;
    if (rowCount > 0) {
        for (var x = 0; x < rowCount; x++) {
            table.deleteRow(0);
        }
    }
}

function populateSocket(stat){
    var strings = stat.split(",");
    var p = document.getElementById("running");
    var p2 = document.getElementById("port");
    var but = document.getElementById("socBut");
    if(strings[0] == "true"){
        p.innerText = "Socket Server is running";
        p2.innerText = "Port: " + strings[1];
        but.innerHTML = '<button type="button" onclick="socketShutdown()">Shutdown Socket Server</button>';
    }else{
        p.innerText = "Socket Server is not running";
        but.innerHTML = '<button type="button" onclick="socketStart()">Start Socket Server</button>';
    }
}

function updatePing(ping) {
    var pingBox = document.getElementById("ping").innerText = "Ping = " + ping + " milliseconds";
}

var connect_callback = function () {
    // called back after the client is connected and authenticated to the STOMP server
    stompClient.subscribe('/user/queue/notify', function (msgOut) {
        msg = msgOut.body.split("%")
        //0 = kick, 1 = device cert, 3 = display msg, 2 revalidate page
        if (msg[0] == 1) {
            writeCert(msg[1]);
        }
        if (msg[0] == 2) {
            location.href = "/?user=" + username;
        }
        if (msg[0] == 3) {
            var msg = 'Message From: ' + msg[1] + '\n' + msg[2];
            alert(msg);
        }
        if (msg[0] == 4) {
            //handle
        }
        if (msg[0] == 0) {
            alert("You have been kicked from your session by administrator");
            location.href = "/"
        }
        if (msg[0] == 10) {
            document.getElementById("benchStatus").innerText = "CPU Bench Finished";
            document.getElementById("benchResult").innerText = "Score: " + msg[1];
        }
        if (msg[0] == "stat") {
            var rowst = "";
            for (var i = 1; i < msg.length; i++) {
                if (i == 1) {
                    rowst = msg[i];
                } else {
                    rowst += "%" + msg[i];
                }
            }
            populateSysTable(rowst.split(","));
        }
        if (msg[0] == "change") {
            document.getElementById("changeOut").innerText = msg[1];
        }
        if (msg[0] == "doc") {
            document.getElementById("docOut").innerText = msg[1];
        }
        if (msg[0] == "var") {
            populateVarTable(msg[1]);
        }
        if (msg[0] == "deny") {
            var msg = 'Message From: ' + msg[1] + '\n\n' + msg[2];
            alert(msg);
            location.href = "/";
        }
        if (msg[0] == "socket") {
            populateSocket(msg[1]);
        }
    });
    stompClient.subscribe('/maint/table', function (msgOut) {
        var msg = msgOut;
        //alert(msg);
        var rowst = msgOut.body.split(",");
        populateTabTable(rowst);
    });
    stompClient.subscribe('/maint/bench/score', function (msgOut) {
        document.getElementById("benchStatus").innerText = "CPU Bench Finished";
        document.getElementById("benchResult").innerText = "Score: " + msgOut.body;
    });
    stompClient.subscribe('/maint/bench/stat', function (msgOut) {
        var prev = document.getElementById("benchStatus").innerText;
        document.getElementById("benchStatus").innerText = prev + '\r\n' + msgOut.body;
    });
    stompClient.subscribe('/maint/ping/init', function (msgOut) {
        pingFin()
    });
    stompClient.subscribe('/maint/ping/fin', function (msgOut) {
        updatePing(msgOut.body)
    });
    sendUsername();
    pingInit();
    getSysStats();
    tableRefresh();
    getChangelog();
    getDoc();
    socketStat();
};

stompClient.connect({}, function (frame) {
    var suffix = frame.headers['queue-suffix'];
    var url = stompClient.ws._transport.url;
    sessionId = /\/([^\/]+)\/websocket/.exec(socket._transport.url)[1];
    //sessionId = /\/([^\/]+)\/websocket/.exec(socket._transport.url)[1];
    url = url.replace(
        "/ws://localhost:8080/messagesocket/specific/message//ig", "");
    url = url.replace("//websocket/ig", "");
    url = url.replace(/^[0-9]+\//, "");
    console.log("Your current session is: " + sessionId);
    connect_callback()
});

function disconnect() {
    if (stompClient != null) {
        stompClient.disconnect();
    }
    console.log("Disconnected");
}

function getVars(request) {
    varName = request;
    stompClient.send("/app/maint/var/get", {},
        JSON.stringify({'username': username, 'session': sessionId, 'tablename': "", 'query': request}));
}

function sendVars(request) {
    stompClient.send("/app/maint/var/set", {},
        JSON.stringify({'username': username, 'session': sessionId, 'tablename': varName, 'query': request}));
}

function sendUsername() {
    stompClient.send("/app/register", {},
        JSON.stringify({'username': username, 'session': sessionId}));
}

function stopBench() {
    document.getElementById("benchStatus").innerText = "Stopping CPU Bench";
    stompClient.send("/app/maint/bench/stop", {},
        JSON.stringify({'username': username, 'session': sessionId}));
}

function getChangelog() {
    stompClient.send("/app/maint/changelog", {},
        JSON.stringify({'username': username, 'session': sessionId}));
}

function getDoc() {
    stompClient.send("/app/maint/doc", {},
        JSON.stringify({'username': username, 'session': sessionId}));
}

function tableRefresh() {
    stompClient.send("/app/maint/tab", {},
        JSON.stringify({'username': username, 'session': sessionId}));
}

function pingInit() {
    stompClient.send("/app/maint/pinginit", {},
        JSON.stringify({'username': username, 'session': sessionId}));
}

function benchRequest() {
    document.getElementById("benchStatus").innerText = "CPU Bench is running...";
    stompClient.send("/app/maint/bench", {},
        JSON.stringify({'username': username, 'session': sessionId}));
}

window.setInterval(getSysStats, 4000);

window.setInterval(pingInit, 2000);

function getSysStats() {
    stompClient.send("/app/maint/get/sys", {},
        JSON.stringify({'username': username, 'session': sessionId}));
}

function reboot() {
    var result = confirm("Do you really want to reboot this application server?");
    if (result == true) {
        stompClient.send("/app/maint/reboot", {},
            JSON.stringify({'username': username, 'session': sessionId}));
    } else {
        alert("Reboot aborted...");
    }
}

function shutdown() {
    var result = confirm("Do you really want to shutdown this application server?");
    if (result == true) {
        stompClient.send("/app/maint/shutdown", {},
            JSON.stringify({'username': username, 'session': sessionId}));
    } else {
        alert("Shutdown aborted...");
    }
}

function socketStat() {
    stompClient.send("/app/maint/socket/stat", {},
        JSON.stringify({'username': username, 'session': sessionId}));
}

function socketShutdown() {
    var result = confirm("Do you really want to shutdown the socket server?");
    if (result == true) {
        stompClient.send("/app/maint/socket/shutdown", {},
            JSON.stringify({'username': username, 'session': sessionId}));
    } else {
        alert("Socket server shutdown aborted...");
    }
}

function socketStart() {
    var result = confirm("Do you really want to start the socket server?");
    if (result == true) {
        stompClient.send("/app/maint/socket/start", {},
            JSON.stringify({'username': username, 'session': sessionId}));
    } else {
        alert("Socket server startup aborted...");
    }
}

function pingFin() {
    stompClient.send("/app/maint/pingfin", {},
        JSON.stringify({'username': username, 'session': sessionId}));
}

function sendDeviceCert(cert) {
    stompClient.send("/app/device/cert", {},
        JSON.stringify({'cert': cert, 'user': username}));
}

function writeCert(cert) {
    if (typeof (Storage) !== "undefined") {
        // Code for localStorage
        localStorage.setItem("cert", cert);
    }
}

function checkStatus() {
    stompClient.send("/app/httpstatus", {},
        JSON.stringify({'username': username, 'session': sessionId}));
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