package seedu.address.ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.datetime.DateCustom;
import seedu.address.model.task.Task;

/**
 *  Task Calendar Window. Provides a graphical interface for task viewing
 *  with a built in Calendar element for the user. Elements are automatically
 *  added in on construction
 */
public class CalendarWindow extends UiPart<Stage> {

    private static final String FXML = "CalendarWindow.fxml";
    private static DatePicker datePicker;
    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static boolean runningCommand;

    private final Logger logger = LogsCenter.getLogger(getClass());




    private Stage primaryStage;
    private Logic logic;

    private TaskListPanel taskListPanel;

    private ReadOnlyAddressBook readOnlyTaskList;
    private HashMap<LocalDate, Integer> markedDates;

    @FXML
    private StackPane taskListPanelPlaceholder;
    @FXML
    private StackPane calendarPanel;
    @FXML
    private StackPane commandBoxPlaceholder;


    public CalendarWindow(Stage primaryStage, Logic logic, LocalDate givenDate) {
        super(FXML, primaryStage);

        this.primaryStage = primaryStage;
        this.logic = logic;
        this.readOnlyTaskList = logic.getAddressBook();

        generateMarkedDates();
        this.readOnlyTaskList.addListener((observable -> {
            generateMarkedDates();
            LocalDate temp = datePicker.getValue();
            datePicker.setValue(null);
            datePicker.setValue(temp);
        }));
        taskListPanel = new TaskListPanel(logic.getFilteredTaskList());
        taskListPanel.setForCalender();
        taskListPanel.getRoot().getStylesheets().addAll("view/Calendar.css", "view/Extensions.css");
        taskListPanelPlaceholder.getStylesheets().addAll("view/Calendar.css", "view/Extensions.css");
        taskListPanelPlaceholder.getChildren().add(taskListPanel.getRoot());

        VBox vb = new VBox();
        CommandBox commandBox = new CommandBox(this::executeCommand, this.logic.getHistory(), true);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());

        this.calendarPanel = new StackPane();
        createCalender(givenDate);
        vb.getChildren().addAll(calendarPanel, commandBoxPlaceholder);
        SplitPane sp = new SplitPane();
        sp.getItems().addAll(taskListPanelPlaceholder, vb);

        Scene scene = new Scene(sp);
        this.primaryStage.setScene(scene);
        scene.getStylesheets().add("view/Calendar.css");
        this.primaryStage.setTitle("Task Calendar");
    }

    /**
     *  Executes the date given to generate a new calendar if valid
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            this.runningCommand = true;
            if (DateCustom.isValidDate(commandText.trim())) {
                commandText = "taskcal " + commandText;
            }
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            runningCommand = false;
            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("Invalid command: " + commandText);
            logger.info(e.getMessage());
            throw e;
        }
    }
    /**
     *  Closes the stage to exit the window.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        primaryStage.hide();
    }

    public void show() {
        primaryStage.show();
    }
    public boolean isShowing() {
        return primaryStage.isShowing();
    }
    public void focus() {
        primaryStage.requestFocus();
    }
    public void close() {
        primaryStage.close();
    }

    /**
     *  Generates a hashmap to store dates that should be marked with task priority
     */
    private void generateMarkedDates() {
        this.markedDates = new HashMap<>();
        for (Task t : logic.getAddressBook().getTaskList()) {

            if (markedDates.containsKey(t.getStartDate().getDate())) {
                if (t.getPriority().getPriorityLevel() > markedDates.get(t.getStartDate().getDate())) {
                    markedDates.put(t.getStartDate().getDate(), t.getPriority().getPriorityLevel());
                }
            } else {
                markedDates.put(t.getStartDate().getDate(), t.getPriority().getPriorityLevel());
            }

            if (markedDates.containsKey(t.getEndDate().getDate())) {
                if (t.getPriority().getPriorityLevel() > markedDates.get(t.getEndDate().getDate())) {
                    markedDates.put(t.getEndDate().getDate(), t.getPriority().getPriorityLevel());
                }
            } else {
                markedDates.put(t.getEndDate().getDate(), t.getPriority().getPriorityLevel());
            }
        }
    }

    /**
     * Creates a Calendar popup node from Datepicker using a given localDate
     * @param localDate Date to be given to datepicker to generate calendar
     */
    public void createCalender(LocalDate localDate) {
        this.datePicker = new DatePicker(localDate);
        datePicker.setDayCellFactory(new Callback<>() {
            @Override
            public DateCell call(DatePicker param) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);

                        } else {

                            StackPane dayCellPane = new StackPane();
                            Circle circle = new Circle(15);

                            Label label = new Label();
                            label.setText(getText());
                            label.setTextFill(Color.WHITE);
                            label.setLabelFor(circle);
                            label.setStyle("-fx-font-weight: bold; -fx-border-color: transparent");
                            label.setFont(Font.font("Helvetica", 15));

                            if (markedDates.containsKey(item)) {
                                switch (markedDates.get(item)) {
                                case 3:
                                    circle.setFill(Color.web("#FF0000"));
                                    break;

                                case 2:
                                    circle.setFill(Color.web("#FF9A00"));
                                    break;

                                case 1:
                                    circle.setFill(Color.web("#FFE600"));
                                    break;

                                default:
                                    circle.setFill(Color.GREEN);
                                }
                            } else {
                                circle.setFill(Color.TRANSPARENT);
                            }
                            if (item.getDayOfWeek().getValue() == 7 || item.getDayOfWeek().getValue() == 6) {
                                label.setTextFill(Color.web("#29F6E8"));
                            }
                            dayCellPane.getChildren().addAll(circle, label);
                            dayCellPane.setPrefSize(50, 50);
                            setGraphic(dayCellPane);
                            setText("");
                        }
                    }
                };
            }
        });
        DatePickerSkin datepopup = new DatePickerSkin(datePicker);
        Node out = datepopup.getPopupContent();
        for (Node node : out.lookupAll(".day-name-cell")) {
            if (node.toString().contains("Sun") || node.toString().contains("Sat")) {
                node.setStyle("-fx-text-fill: #29F6E8");
            }
        }
        for (Node node : out.lookupAll(".button")) {
            node.setFocusTraversable(false);
        }
        for (Node node : out.lookupAll(".day-cell")) {
            node.setOnMouseClicked(event -> {
                try {
                    DateCell dateCell = (DateCell) node;
                    logic.execute("taskcal " + dateCell.getItem().format(format));
                } catch (CommandException | ParseException e) {
                    logger.info("Invalid date");
                }
            });
        }
        this.calendarPanel.getChildren().addAll(out);
    }

    /**
     * Returns true if a command is currently being ran from CalendarWindow
     */
    public static boolean isRunningCommand() {
        return runningCommand;
    }
    public static void setDate(String newDate) {
        datePicker.setValue(LocalDate.parse(newDate, format));
    }
    public static String getDateInput(String input) {
        return input.substring(input.lastIndexOf(" ") + 1);
    }

}
