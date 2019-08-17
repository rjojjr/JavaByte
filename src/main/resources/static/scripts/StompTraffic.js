var context_path = "${pageContext.request.contextPath}";
var socket = new SockJS('/ws');
var stompClient = Stomp.over(socket);
var sessionId = "";

var username = "null";

var users = '';

var mfirst = true;

function setUname(Uname){
    username = Uname;
}

function send() {
    var msgBox = document.getElementById("msgbox");
    var msg = msgBox.value;
    //alert(msg);
    users = users + '%' + msg;
    sendUserMesg(users);
}

function deleteTable(){
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
    deleteTable();
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
                var temp = cells[cellS].split(":");
                var cell = newRow.insertCell(ccount);
                cell.innerText = temp[1];
                ccount++;
            }

        }
    }
}

function searchLogs() {
    var first = true;
    var searchTerms = "";
    var table = document.getElementById("filterTable");
    var usernameEl = document.getElementById("username");
    var username = usernameEl.value;
    var ipEl = document.getElementById("ipad");
    var ip = ipEl.value;
    var pageEl = document.getElementById("page");
    var page = pageEl.value;
    var sdateEl = document.getElementById("sdate");
    var sdate = sdateEl.value;
    var edateEl = document.getElementById("edate");
    var edate = edateEl.value;
    if(username !== ""){
        if(first){
            searchTerms = "username:" + username;
            first = false;
        }else{
            searchTerms+= ",username:" + username;
        }
    }
    if(ip !== ""){
        if(first){
            searchTerms = "ip:" + ip;
            first = false;
        }else{
            searchTerms+= ",ip:" + ip;
        }
    }
    if(page !== ""){
        if(first){
            searchTerms = "page:" + page;
            first = false;
        }else{
            searchTerms+= ",page:" + page;
        }
    }
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
            populateIPSearch(msg[1]);
        }
        if (msg[0] == "deny") {
            var msg = 'Message From: ' + msg[1] + '\n\n' + msg[2];
            alert(msg);
            location.href = "/";
        }
        if (msg[0] == "num") {
            document.getElementById("num").innerText = "Number of logs: " + msg[1];
        }
        if (msg[0] == "dump") {
            getNum();
        }
    });
    sendUsername();
    getNum();
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

function getNum() {
    stompClient.send("/app/traffic/count", {},
        JSON.stringify({'username':username, 'session':sessionId}));
}

function dumpLogs() {
    var conf = confirm("Are you sure that you want to dump traffic logs?");
    if(conf){
        stompClient.send("/app/traffic/dump", {},
            JSON.stringify({'username':username, 'session':sessionId}));
    }else{
        alert("Dump cancelled");
    }
}

function sendDeviceCert(cert) {
    stompClient.send("/app/device/cert", {},
        JSON.stringify({'cert':cert, 'user':username}));
}

function sendSearch(cert) {
    stompClient.send("/app/traffic/search", {},
        JSON.stringify({'request':cert, 'username':username, 'stompID':sessionId}));
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