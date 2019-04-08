package seedu.address.logic.parser;

import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.PatientSelectCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new PatientSelectCommand object
 */
public class PatientSelectCommandParser implements Parser<PatientSelectCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the PatientSelectCommand
     * and returns an PatientSelectCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public PatientSelectCommand parse(String args) throws ParseException {
        try {
            Index index = ParserUtil.parseIndex(args);
            return new PatientSelectCommand(index);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, PatientSelectCommand.MESSAGE_USAGE), pe);
        }
    }
}
