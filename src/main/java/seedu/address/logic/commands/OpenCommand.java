package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.MainApp;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataConversionException;
import seedu.address.logic.CommandHistory;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.AddressBookStorage;
import seedu.address.storage.InOutAddressBookStorage;
import seedu.address.storage.StorageManager;

/**
 * Opens data from a text file.
 */
public class OpenCommand extends Command {

    public static final String COMMAND_WORD = "open";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Opens a .json file in the \"data\" folder and overwrites the current data\n"
            + "Parameters: FILEPATH\n"
            + "Example: " + COMMAND_WORD + " data1.json";

    public static final String MESSAGE_SUCCESS = "File opened!";

    private final File file;

    public OpenCommand(File file) {
        this.file = file;
    }

    @Override
    public CommandResult execute(Model model, CommandHistory history) {
        requireNonNull(model);
        readFile(model);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        model.commitAddressBook();
        return new CommandResult(MESSAGE_SUCCESS);
    }

    /**
     * readFile() overwrites the current address book with the contents of the file.
     */
    private void readFile(Model model) {
        AddressBookStorage openStorage = new InOutAddressBookStorage(file.toPath());

        StorageManager openStorageManager = new StorageManager(openStorage, null);

        final Logger logger = LogsCenter.getLogger(MainApp.class);

        Optional<ReadOnlyAddressBook> addressBookOptional;
        ReadOnlyAddressBook openData;

        try {
            addressBookOptional = openStorageManager.readAddressBook();
            if (!addressBookOptional.isPresent()) {
                logger.info("Data file not found. Will be starting with a sample AddressBook");
            }
            openData = addressBookOptional.orElseGet(SampleDataUtil::getSampleAddressBook);
        } catch (DataConversionException e) {
            logger.warning("Data file not in the correct format. "
                               + "Will be starting with an empty AddressBook");
            openData = new AddressBook();
        } catch (IOException e) {
            logger.warning("Problem while reading from the file. "
                               + "Will be starting with an empty AddressBook");
            openData = new AddressBook();
        }

        model.setAddressBook(openData);
    }
}
