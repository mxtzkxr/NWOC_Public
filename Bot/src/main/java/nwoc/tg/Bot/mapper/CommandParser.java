package nwoc.tg.Bot.mapper;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CommandParser {
    public static ParsedCommand parse(String text) {
        if (text == null || text.trim().isEmpty() || !text.startsWith("/")) {
            return null;
        }

        String withoutSlash = text.substring(1).trim();

        String[] parts = withoutSlash.split("\\s+");

        if (parts.length == 0) {
            return null;
        }

        String commandName = parts[0];
        List<String> params = new ArrayList<>(Arrays.asList(parts).subList(1, parts.length));

        return new ParsedCommand(commandName, params);
    }


    @Getter
    public static class ParsedCommand {
        private final String command;
        private final List<String> params;

        public ParsedCommand(String command, List<String> params) {
            this.command = command;
            this.params = params;
        }

        public String getParam(int index) {
            return (index >= 0 && index < params.size()) ? params.get(index) : null;
        }

        @Override
        public String toString() {
            return "Command: " + command + ", params: " + params;
        }
    }
}
