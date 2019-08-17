var context_path = "${pageContext.request.contextPath}";
var socket = new SockJS('/ws');
var stompClient = Stomp.over(socket);
var sessionId = "";

var username = "null";

var first = 0, firstt = 0;

var tableName = "";

var create = "";

var createCount = 0;

var currentindex = 0;

function showQuery() {
    showTabS("#query");
}

function setUname(Uname) {
    username = Uname;
}

function queryTable() {
    //alert("A work in progress");
    var request = "";
    var first = true;
    table = document.getElementById("tablefilter");
    var rowCount = table.rows.length;
    if (rowCount > 0) {
        for (var x = 0; x < rowCount; x++) {
            var input = document.getElementsByClassName('filterInput')[x].value;
            if (input != "") {
                var row = table.rows[x];
                var value = row.cells[0].innerText;
                if (first) {
                    request = value + ";" + input;
                    first = false;
                } else {
                    request += "%" + value + ";" + input;
                }
            }
        }
        if (request == "") {
            request = "all";
        } else {

        }
        tableRequest(request);
    }
}

function addField() {
    table = document.getElementById("tableCreate");
    if(createCount == 0){
        tableName = table.rows[0].cells[1].children[0].value;
        create = table.rows[1].cells[1].children[0].value + "-" + table.rows[1].cells[2].children[0].value;
        var newRow = table.insertRow(2);
        newRow.innerHTML = ' <td>\n' +
            '                                        <p>Column</p>\n' +
            '                                    </td>\n' +
            '                                    <td>\n' +
            '                                        <input id=\'field1\' type="text" value="" style="" placeholder="Column Name"/>\n' +
            '                                    </td>\n' +
            '                                    <td>\n' +
            '                                        <input id=\'type1\' type="text" value="" style="" placeholder="Column Type(i = integer, d = decimal, s = string, b = boolean)"/>\n' +
            '                                    </td>\n' +
            '                                    <td>\n' +
            '                                        <button onclick="addField()">\n' +
            '                                            Add Column\n' +
            '                                        </button>\n' +
            '                                    </td>';
        createCount++;
    }else{
        create+= ";" + table.rows[createCount + 1].cells[1].children[0].value + "-" + table.rows[1].cells[2].children[0].value;
        var newRow = table.insertRow(createCount + 2);
        newRow.innerHTML = ' <td>\n' +
            '                                        <p>Column</p>\n' +
            '                                    </td>\n' +
            '                                    <td>\n' +
            '                                        <input id=\'field1\' type="text" value="" style="" placeholder="Column Name"/>\n' +
            '                                    </td>\n' +
            '                                    <td>\n' +
            '                                        <input id=\'type1\' type="text" value="" style="" placeholder="Column Type(i = integer, d = decimal, s = string, b = boolean)"/>\n' +
            '                                    </td>\n' +
            '                                    <td>\n' +
            '                                        <button onclick="addField()">\n' +
            '                                            Add Column\n' +
            '                                        </button>\n' +
            '                                    </td>';
        createCount++;
    }
}

function editRow(index){
    document.getElementById("prompt").innerText = "";
    currentindex = index;
    table = document.getElementById("tableEdit");
    deleteTable(table);
    var request = "index;" + index;
    rowRequest(request);
}

function popEdit(results) {
    showTabS("#edit");
    table = document.getElementById("tableEdit");
    var rows = results.split(";");
    var fields = rows[0].split(":");
    var vals = rows[1].split(":");
    for (var i = 0; i < fields.length; i++) {
        var newRow = table.insertRow(i);
        var cell = newRow.insertCell(0);
        cell.innerText = fields[i];
        cell = newRow.insertCell(1);
        if(fields[i] == "index"){
            cell.innerText = vals[i];
        }else{
            cell.innerHTML= '<input class="filterInput" type="text" value=""/>'
            table.rows[i].cells[1].children[0].value = vals[i];
        }

    }
}

function commitChange() {
    table = document.getElementById("tableEdit");
    var request;
    var first = true;
    for(var i = 1; i < table.rows.length; i++){
        if(first){
            var temp = table.rows[i].cells[0].innerText;
            if(temp == "index"){
                request = temp + ":" + table.rows[i].cells[1].innerText;
            }else{
                request = temp + ":" + table.rows[i].cells[1].children[0].value;
            }
            first = false;
        }else{
            var temp = table.rows[i].cells[0].innerText;
            if(temp == "index"){
                request+= ";" + temp + ":" + table.rows[i].cells[1].innerText;
            }else{
                request+= ";" + temp + ":" + table.rows[i].cells[1].children[0].value;
            }
        }
    }
    deleteTable(document.getElementById("tableresult"));
    commit(request);
}

function createTable() {
    if(tableName != "" && create != ""){
        sendNewTable();
    }else{
        alert("Invalid Parameters");
    }
}

function populateResultTable(results) {
    table = document.getElementById("tableresult");
    deleteTable(table);
    var index
    var record
    var rows = results.split(";");
    var count;
    for (var i = 0; i < rows.length; i++) {
        var newRow = table.insertRow(i);
        var cells = rows[i].split(':');
        for (var k = 0; k < cells.length; k++) {
            var cell = newRow.insertCell(k);
            cell.innerText = cells[k];
            if(i == 0 && cells[k] == "index"){
                index = k;
            }
            if(i > 0 && k == index){
                record = cells[k];
            }
            count = k;
        }
        if(i != 0){
            var cell = newRow.insertCell(count + 1);
            cell.innerHTML= '<button type="button" onclick="editRow(' + record + ')">Edit Row</button>';
        }else{
            var cell = newRow.insertCell(count + 1);
            cell.innerText = "Edit Record";
        }

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

function deleteTables() {
    deleteTable(document.getElementById("tablefilter"));
    deleteTable(document.getElementById("tableresult"));
}

function filterTable(fields) {
    table = document.getElementById("tablefilter");
    var strings = fields.split(" - ");
    if (tableName == "Transactions" || tableName == "Users") {
        deleteTable(table);
        var newRow = table.insertRow(0);
        var cell = newRow.insertCell(0);
        cell.innerText = "You cannot view this table here.";
    } else {
        deleteTable(table);
        for (var i = 0; i < strings.length; i++) {
            var string = strings[i];
            var row = string.split("-");
            var newRow = table.insertRow(i);
            var cell = newRow.insertCell(0);
            cell.innerText = row[0];
            var cel2 = newRow.insertCell(1);
            cel2.innerHTML = '<input class="filterInput" type="text" value=""/>';
        }
    }

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
    cell.innerHTML = '<button type="button" onclick="showQuery()">Query Table</button>';
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
        if (msg[0] == "result") {
            populateResultTable(msg[1]);
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

function tableRequest(request) {
    stompClient.send("/app/table/query", {},
        JSON.stringify({'username': username, 'session': sessionId, 'tablename': tableName, 'query': request}));
}

function rowRequest(request) {
    stompClient.send("/app/table/query/record", {},
        JSON.stringify({'username': username, 'session': sessionId, 'tablename': tableName, 'query': request}));
}

function commit(request) {
    stompClient.send("/app/table/commit/record", {},
        JSON.stringify({'username': username, 'session': sessionId, 'tablename': tableName, 'query': request}));
}

function sendNewTable() {
    stompClient.send("/app/table/create", {},
        JSON.stringify({'username': username, 'session': sessionId, 'tablename': tableName, 'query': create}));
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