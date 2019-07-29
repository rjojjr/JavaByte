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

function createUser() {
    window.open('/users/create','_blank');
}

function userSummary(value) {
    var url = "/users/summary?id=" + value;
    window.open(url, '_blank');
}

function kickUser(value) {
    var result = confirm("Do you really want to kick this user?");
    if (result == true) {
        location.href = "/users/kick?id=" + value;
    } else {

    }
}

function sendUserMesg(msg) {
    var table = document.getElementById("selected");
    var tableRows = table.getElementsByTagName('tr');
    var rowCount = tableRows.length;
     //alert("Row Count = " + rowCount);
    for (var x=rowCount-1; x>=0; x--) {
        table.deleteRow(tableRows[x]);
    }
    stompClient.send("/app/usermessage", {},
        JSON.stringify({'page': msg, 'index': username}));
    users = '';
    mfirst = 'true';
}

function addUser(username) {
    var count = 0;
    var table = document.getElementById("selected");
    var row = table.insertRow(count);
    var cell1 = row.insertCell(count);
    cell1.innerHTML = username;
    if (mfirst) {
        users = username;
        mfirst = 'false';
    } else {
        users = users + ',' + username;
    }
    count++;
}

function send() {
    var msgBox = document.getElementById("msgbox");
    var msg = msgBox.value;
    //alert(msg);
    users = users + '%' + msg;
    sendUserMesg(users);
}

function deleteTable() {
    var table = document.getElementById("userResult");
    var rowCount = table.rows.length;
    for (var x = rowCount - 1; x > 0; x--) {
        table.deleteRow(x);
    }
}

function populateUserSearch(result) {
    var table = document.getElementById("userResult");
   var rowCount = table.rows.length;
    var strings = result.split(",");
    deleteTable()
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
            var result = true;
            for(cellS in cells){
                var temp = cells[cellS].split(":")
                var cell = newRow.insertCell(ccount);
                // add value to the cell
                if(ccount == 3){
                    userid = temp[1];
                }
                cell.innerText = temp[1];
                ccount++;
            }
            if(result){
                var cel2 = newRow.insertCell(ccount);
                // add value to the cell
                cel2.innerHTML = '<button type="button" onclick="userSummary(' + userid + ')">Summary</button>';
            }
        }
    }
}

function searchUsers() {
    var first = true;
    var searchTerms = "";
    var table = document.getElementById("filterTable");
    var usernameEl = document.getElementById("username");
    var username = usernameEl.value;
    var firstnameEl = document.getElementById("firstname");
    var firstname = firstnameEl.value;
    var lastnameEl = document.getElementById("lastname");
    var lastname = lastnameEl.value;
    var useridEl = document.getElementById("userid");
    var userid = useridEl.value;
    var adminEl = document.getElementById("admin");
    var admin = adminEl.value;
    var indexEl = document.getElementById("index");
    var index = indexEl.value;
    var ipEl = document.getElementById("ip");
    var ip = ipEl.value;
    var sessionEl = document.getElementById("session");
    var session = sessionEl.value;
    var deviceEl = document.getElementById("device");
    var device = deviceEl.value;
    if(username !== ""){
        if(first){
            searchTerms = "username:" + username;
            first = false;
        }else{
            searchTerms+= ",username:" + username;
        }
    }
    if(firstname !== ""){
        if(first){
            searchTerms = "firstname:" + firstname;
            first = false;
        }else{
            searchTerms+= ",firstname:" + firstname;
        }
    }
    if(lastname !== ""){
        if(first){
            searchTerms = "lastname:" + lastname;
            first = false;
        }else{
            searchTerms+= ",lastname:" + lastname;
        }
    }
    if(userid !== ""){
        if(first){
            searchTerms = "userid:" + userid ;
            first = false;
        }else{
            searchTerms+= ",userid:" + userid;
        }
    }
    if(admin !== ""){
        if(first){
            searchTerms = "admin:" + admin;
            first = false;
        }else{
            searchTerms+= ",admin:" + admin;
        }
    }
    if(index !== ""){
        if(first){
            searchTerms = "index:" + index;
            first = false;
        }else{
            searchTerms+= ",index:" + index;
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
    if(session !== ""){
        if(first){
            searchTerms = "session:" + session ;
            first = false;
        }else{
            searchTerms+= ",session:" + session;
        }
    }
    if(device !== ""){
        if(first){
            searchTerms = "device:" + device;
            first = false;
        }else{
            searchTerms+= ",device:" + device;
        }
    }
    sendUserSearch(searchTerms);
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
            location.href = "/users?user=" + username;
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
            populateUserSearch(msg[1]);
        }
        if (msg[0] == "deny") {
            var msg = 'Message From: ' + msg[1] + '\n\n' + msg[2];
            alert(msg);
            location.href = "/";
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

function sendUserSearch(cert) {
    stompClient.send("/app/users/search", {},
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