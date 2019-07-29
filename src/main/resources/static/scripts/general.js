// Mainenance Functions
function reboot() {
    var result = confirm("Do you really want to reboot the server?");
    if (result == true) {
        alert("Rebooting Server...");
        location.href = "/maintenance/reboot"
    } else {
        alert("Reboot cancelled");
    }
}

function shutdown() {
    var result = confirm("Do you really want to shutdown the server?");
    if (result == true) {
        alert("Shutting down the Server...");
        location.href = "/maintenance/shutdown"
    } else {
        alert("Shutdown cancelled");
    }
}

function rebootDB() {
    var result = confirm("Do you really want to reboot the database?");
    if (result == true) {
        alert("Rebooting Database...");
        location.href = "/maintenance/rebootdb"
    } else {
        alert("Database Reboot cancelled");
    }
}

function shutdownDB() {
    var result = confirm("Do you really want to shutdown the database?");
    if (result == true) {
        alert("Shutting down Database...");
        location.href = "/maintenance/shutdowndb"
    } else {
        alert("Database shutdown cancelled");
    }
}

function startDB() {
    alert("Starting Database...");
    location.href = "/maintenance/startdb"
}

//Tax console functions
function newTaxClass() {
    var result = confirm("Leave this page and create tax class?");
    if (result == true) {
        location.href = "/inventory/taxclasses/create";
    } else {

    }
}

//Tax creator
function hoverDescription() {
    var elem = document.getElementById("hover");
    elem.innerHTML = "This how your users will see and reference tax classes.";
}

function hoverRate() {
    var elem = document.getElementById("hover");
    elem.innerHTML = "This the the tax rate as a decimal. Ex 9% tax would be .09";
}