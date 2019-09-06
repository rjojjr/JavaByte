var context_path = "${pageContext.request.contextPath}";
var socket = new SockJS('/ws');
var stompClient = Stomp.over(socket);
var sessionId = "";

var username = "null";

var first = 0, firstt = 0;

var tableName = "";

var create = "";

var selectTable = "";

function showQuery() {
    showTabS("#query");
}

function setUname(Uname) {
    username = Uname;
}

function deleteTable(table) {
    var rowCount = table.rows.length;
    if (rowCount > 0) {
        for (var x = 0; x < rowCount; x++) {
            table.deleteRow(0);
        }
    }
}

function deleteTables() {
    deleteTable(document.getElementById("tablefilter"));
    deleteTable(document.getElementById("tableresult"));
}

function selectTable(){
    var table = document.getElementById("tname");
    table.innerText = "Selected Table: " + tableName;
}

function populateTabTable(strings) {
    var filters = "";
    var count = 0;
    table = document.getElementById("table");
    deleteTable(table);
    for (var i = 0; i < strings.length; i++) {
        var string = strings[i];
        var row = string.split(":");
        if (count == 1) {
            filters = row[1];
        }
        if (count == 0) {
            tableName = row[1];
        }
        var newRow = table.insertRow(count);
        var cell = newRow.insertCell(0);
        cell.innerText = row[1];
        var cel2 = newRow.insertCell(0);
        cel2.innerText = row[0];
        count++;
    }

    var newRow = table.insertRow(count);
    var cell = newRow.insertCell(0);
    cell.innerHTML = '<button type="button" onclick="selectTable()">Select Table</button>';
    filterTable(filters);
    count = 0;
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
            location.href = "/tables"
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
            populateTabTable(rowst.split("%"));
        }
        if (msg[0] == "status") {
            var tstat = document.getElementById("tstat");
            tstat.innerText = msg[1];
            var stat = document.getElementById("stat");
            stat.innerText = msg[1];
        }
        if (msg[0] == "deny") {
            var msg = 'Message From: ' + msg[1] + '\n\n' + msg[2];
            alert(msg);
            location.href = "/";
        }
        if (msg[0] == "record") {
            popEdit(msg[1]);
        }
    });
    sendUsername();
};

stompClient.connect({}, function (frame) {
    var suffix = frame.headers['queue-suffix'];
    var url = stompClient.ws._transport.url;
    sessionId = /\/([^\/]+)\/websocket/.exec(socket._transport.url)[1];
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

function sendUsername() {
    stompClient.send("/app/register", {},
        JSON.stringify({'username': username, 'session': sessionId}));
}

function tableSummary(tablename) {
    tableName = tablename;
    table = document.getElementById("tableresult");
    deleteTable(table);
    document.getElementById("tabHead").innerText = tablename + " Status";
    stompClient.send("/app/table/info", {},
        JSON.stringify({'username': username, 'session': sessionId, 'tablename': tablename}));
}

function backupTable() {
    if(selectTable == ""){
        alert("Please select a table...");
    }else{
        var tstat = document.getElementById("tstat");
        tstat.innerText = "Submitting task...";
        var stat = document.getElementById("stat");
        stat.innerText = "Submitting table task...";
        stompClient.send("/app/backup/table", {},
            JSON.stringify({'username': username, 'session': sessionId, 'tablename': selectTable}));
    }
}

function backupAll() {
    var tstat = document.getElementById("tstat");
    tstat.innerText = "Submitting task...";
    var stat = document.getElementById("stat");
    stat.innerText = "Submitting table task...";
    stompClient.send("/app/backup/all", {},
        JSON.stringify({'username': username, 'session': sessionId, 'tablename': selectTable}));
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