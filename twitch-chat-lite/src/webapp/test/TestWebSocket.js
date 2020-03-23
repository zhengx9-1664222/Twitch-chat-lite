// import React from 'react'
// import ReactDOM from 'react-dom'
// import WebApp from './WebApp'
//
// ReactDOM.render(<WebApp/>, document.getElementById('root'));

import IRCMessage from './IRCMessage.js';

    // new WebSocket("ws://localhost:65533");
const CHANNEL_END = "[";
const HEADER_END = "]";
const COMMAND_PREFIX = "/";

let socket;

const msgerForm = qs(".msger-inputarea");
const msgerInput = qs(".msger-input");
const msgerChat = qs(".msger-chat");

var messages = document.getElementById("messages");
var textbox = document.getElementById("textbox");
var button = document.getElementById("button");

var currentUser = "";
var currentChannel = "";

id("connect").addEventListener("click", function() {
    let address = id("address").value;
    socket = new WebSocket(address);

    socket.onopen = function (e) {
        alert("Connection established");
        id("server").classList.add("hidden");
        id("userInfo").classList.remove("hidden");
        // let sender = id("username").value;
        // let channel = id("roomname").value;
        // const message = new IRCMessage(channel, sender, "join", null);
        // socket.send(message.toString());
    };

    socket.onerror = function (error) {
        alert(`[error] ${error.message}`);
    };

    socket.onmessage = function (event) {
        // const message = new IRCMessage(event.data);
        // console.log(message);
        if (id("chat").classList.contains("hidden")) {
            const user = parseUserInfo(event.data);
            alert(`[message]` + user[1] + ` has joined Channel ` + user[0]);
            id("chat").classList.remove("hidden");
            id("userInfo").classList.add("hidden");
            id('c').textContent = user[0];
        } else {
            const chatInfo = parseData(event.data);
            console.log(chatInfo);
            if (chatInfo[1] !== currentUser) {
                if (messages.childNodes.length === 7) {
                    messages.removeChild(messages.childNodes[0]);
                }
                appendMessage(chatInfo[1], "left", chatInfo[3]);
                // let newMessage = document.createElement("li");
                // newMessage.textContent = chatInfo[1] + ": \n" + chatInfo[3];
                // newMessage.classList.add("left-msg");
                // messages.appendChild(newMessage);
            }
        }
    };
});

// socket.onopen = function (e) {
//     alert("Connection established");
//     id("server").classList.add("hidden");
//     id("userInfo").classList.remove("hidden");
//     // let sender = id("username").value;
//     // let channel = id("roomname").value;
//     // const message = new IRCMessage(channel, sender, "join", null);
//     // socket.send(message.toString());
// };
//
// socket.onerror = function (error) {
//     alert(`[error] ${error.message}`);
// };
//
// socket.onmessage = function (event) {
//     // const message = new IRCMessage(event.data);
//     // console.log(message);
//     if (id("chat").classList.contains("hidden")) {
//         const user = parseUserInfo(event.data);
//         alert(`[message]` + user[1] + ` has joined Channel ` + user[0]);
//         id("chat").classList.remove("hidden");
//         id("userInfo").classList.add("hidden");
//         id('c').textContent = user[0];
//     } else {
//         const chatInfo = parseData(event.data);
//         console.log(chatInfo);
//         if (chatInfo[1] !== currentUser) {
//             if (messages.childNodes.length === 7) {
//                 messages.removeChild(messages.childNodes[0]);
//             }
//             appendMessage(chatInfo[1], "left", chatInfo[3]);
//             // let newMessage = document.createElement("li");
//             // newMessage.textContent = chatInfo[1] + ": \n" + chatInfo[3];
//             // newMessage.classList.add("left-msg");
//             // messages.appendChild(newMessage);
//         }
//     }
// };

let submit = id("submit");
submit.addEventListener("click", function() {
    let sender = id("u").value;
    let channel = id("p").value;
    currentUser = sender;
    currentChannel = channel;
    const message = new IRCMessage(channel, sender, "join", null);
    socket.send(message.toString());
});

function appendMessage(name, side, text) {
    //   Simple solution for small apps
    const msgHTML = `
    <div class="msg ${side}-msg">
      <div class="msg-bubble">
        <div class="msg-info">
          <div class="msg-info-name">${name}</div>
          <div class="msg-info-time">${formatDate(new Date())}</div>
        </div>

        <div class="msg-text">${text}</div>
      </div>
    </div>`;

    msgerChat.insertAdjacentHTML("beforeend", msgHTML);
    msgerChat.scrollTop += 500;
}

function formatDate(date) {
    const h = "0" + date.getHours();
    const m = "0" + date.getMinutes();

    return `${h.slice(-2)}:${m.slice(-2)}`;
}

function parseUserInfo(line) {
    let channel = parseChannel(line);
    let sender = parseUser(line);
    return [channel, sender];
}

function parseUser(line) {
    const endHeaderIndex = line.indexOf(HEADER_END);
    const endSenderIndex = line.indexOf(" ");

    if (endHeaderIndex === -1) {
        throw "Invalid IRC message - sender malformed";
    }
    return line.substring(endHeaderIndex + 1, endSenderIndex);
}

function parseData(line) {
    let channel = parseChannel(line);
    let sender = parseSender(line);
    line = removeHeader(line);
    let command = parseCommand(line);
    let content = parseContent(line);
    return [channel, sender, command, content];
};

function parseChannel(line) {
    const endChannelIndex = line.indexOf(CHANNEL_END);
    if (endChannelIndex === -1) {
        throw "Invalid IRC message - channel malformed";
    }
    return line.substring(0, endChannelIndex);
}

function parseSender(line) {
    const endChannelIndex = line.indexOf(CHANNEL_END);
    const endHeaderIndex = line.indexOf(HEADER_END);

    if (endChannelIndex === -1 || endHeaderIndex === -1) {
        throw "Invalid IRC message - sender malformed";
    }
    return line.substring(endChannelIndex + 1, endHeaderIndex);
}

function removeHeader(line) {
    const endHeaderIndex = line.indexOf(HEADER_END);

    if (endHeaderIndex === -1) {
        throw "Invalid IRC message - header malformed";
    }

    return line.substring(endHeaderIndex + 1);
}

function parseCommand(line) {
    if (!isCommand(line)) {
        return null;
    }
    let commandEndIndex = line.indexOf(" ");
    if (commandEndIndex === -1) {
        commandEndIndex = line.length();
    }
    return line.substring(1, commandEndIndex);
}

function isCommand(input) {
    return input.startsWith(COMMAND_PREFIX);
}

function parseContent(line) {
    if (!isCommand(line)) {
        return line;
    } else {
        const commandEndIndex = line.indexOf(" ");

        if (commandEndIndex === -1) {
            return "";
        }
        return line.substring(commandEndIndex + 1);
    }
}

button.addEventListener("click", function() {
    if (id("textbox").value === "/leave") {
        const message = new IRCMessage(currentChannel, currentUser, "leave", null);
        socket.send(message.toString());
        id("chat").classList.add("hidden");
        id("userInfo").classList.remove("hidden");
        id("p").value = "";
        id("textbox").value = "";
        msgerChat.innerHTML = "";
    } else {
        appendMessage(currentUser, "right", id("textbox").value);
        // let newMessage = document.createElement("li");
        // newMessage.innerHTML = id("textbox").value;
        // newMessage.classList.add("right-msg");
        // messages.appendChild(newMessage);
        const message = new IRCMessage(currentChannel, currentUser, null, textbox.value);
        socket.send(message.toString());
        textbox.value = "";
    }
});

function id(name) {
    return document.getElementById(name);
}

function gen(name) {
    return document.createElement(name);
}

function qs(query) {
    return document.querySelector(query);
}