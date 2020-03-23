const CHANNEL_END = "[";
const HEADER_END = "]";
const COMMAND_PREFIX = "/";

class IRCMessage {

    constructor(channel, sender, command, content) {
        this.channel = channel;
        this.sender = sender;
        this.command = command;
        this.content = content;
        if (this.content === null) {
            this.content = "";
        }
    }

    fromLine = function(line) {
        this.channel = IRCMessage.parseChannel(line);
        this.sender = IRCMessage.parseSender(line);
        line = IRCMessage.removeHeader(line);
        this.command = IRCMessage.parseCommand(line);
        this.content = IRCMessage.parseContent(line);
    };

    getChannel() {
        return this.channel;
    }

    getSender() {
        return this.sender;
    }

    getCommand() {
        return this.command;
    }

    getContent() {
        return this.content;
    }

    isCommand() {
        return this.command != null;
    }

    toString() {
        let string = "";

        string += this.channel;
        string += CHANNEL_END;
        string += this.sender;
        string += HEADER_END;

        if (this.isCommand()) {
            string += COMMAND_PREFIX;
            string += this.command;

            if (this.content === "") {
                return string;
            }

            string += " ";
        }

        string += this.content;
        return string;
    }

    static parseChannel(line) {
        const endChannelIndex = line.indexOf(CHANNEL_END);
        if (endChannelIndex === -1) {
            throw "Invalid IRC message - channel malformed";
        }
        return line.substring(0, endChannelIndex);
    }

    static parseSender(line) {
        const endChannelIndex = line.indexOf(CHANNEL_END);
        const endHeaderIndex = line.indexOf(HEADER_END);

        if (endChannelIndex === -1 || endHeaderIndex === -1) {
            throw "Invalid IRC message - sender malformed";
        }
        return line.substring(endChannelIndex + 1, endHeaderIndex);
    }

    static removeHeader(line) {
        const endHeaderIndex = line.indexOf(HEADER_END);

        if (endHeaderIndex === -1) {
            throw "Invalid IRC message - header malformed";
        }

        return line.substring(endHeaderIndex + 1);
    }

    static parseCommand(line) {
        if (!this.isCommand(line)) {
            return null;
        }
        let commandEndIndex = line.indexOf(" ");
        if (commandEndIndex === -1) {
            commandEndIndex = line.length();
        }
        return line.substring(1, commandEndIndex);
    }

    static parseContent(line) {
        if (!this.isCommand(line)) {
            return line;
        } else {
            const commandEndIndex = line.indexOf(" ");

            if (commandEndIndex === -1) {
                return "";
            }
            return line.substring(commandEndIndex + 1);
        }
    }

    static isCommand(input) {
        return input.startsWith(COMMAND_PREFIX);
    }
}

export default IRCMessage;