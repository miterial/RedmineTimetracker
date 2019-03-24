package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.taskadapter.redmineapi.RedmineAuthenticationException;
import com.taskadapter.redmineapi.RedmineException;
import database.UserInfo;
import javafx.animation.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.util.Duration;
import lombok.Getter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tracker.TrackerModel;
import tracker.timestamp.TimeStampMongoDB;
import utils.Utils;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TrackerController implements Initializable {

    private static Logger log = LoggerFactory.getLogger(TrackerController.class);

    @FXML
    JFXComboBox cbIssues;
    @FXML
    JFXComboBox cbProjects;
    @FXML
    JFXButton btnTracking;
    @FXML
    Label labelIssueTime;
    @FXML
    Label labelTimeToday;
    @FXML
    Label labelTimeMonth;
    @FXML
    JFXTextArea textComment;

    private Service saveTimeEntryService;
    private Runnable timerRunnable;
    private ScheduledExecutorService timerService;

    @Getter
    private boolean isTracking = false;

    private Map<String, Integer> issuesmap;
    private int currentIssueId = -1;
    private int currentProjectId = -1;

    private Timeline minuteTimeline;    // Timeline to animate issue time
    private Timeline todayTimeline;     // Timeline to animate today time
    private String startIssueTime;      // start issue time; used when toggle state changes

    private String username;

    private TrackerModel trackerModel = new TrackerModel();

    public TrackerController(String name, String pass) throws RedmineException {
        this.username = name;

        trackerModel.connectWithLoginRedmine(name, pass);
    }

    /**
     * Initialize controller
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        log.info("Init projects' combobox");
        initProjectsCombobox();

        log.info("Init issues' combobox");
        initIssuesCombobox();

        log.info("Init database");
        trackerModel.initDatabase();

        log.info("Init time labels");
        initTimelabels();

        initTask();

        log.info("Init tracker button");
        btnTracking.setOnAction((e) -> trackerButtonEvent());
    }

    /**
     * Inititalize time label with time spent on this issue
     */
    private void initTimelabels() {

        float spentTime = trackerModel.getRedmineInfo().getSpentTimeIssue(currentIssueId);   //from redmine
        startIssueTime = Utils.convertToString(String.valueOf(spentTime));  // convert float to human-readable string
        labelIssueTime.setText(startIssueTime);

        // TODO: check new month/day (from Danil)

        String spentTimeToday = "00:00";
        String spentTimeMonth = "00:00";
        JSONObject json;

        String userInfoFromDb = trackerModel.getMongoDB().getUserInfo(); // from mongo

        if(userInfoFromDb == null) {
            log.info("Creating new user in DB...");
            UserInfo userInfo = trackerModel.getRedmineInfo().getUserInfo();
            spentTimeMonth = userInfo.getSpentMonth();
            spentTimeToday = userInfo.getSpentToday();

            final String ZERO_TIME = "00:00";

            //TODO: incapsulate
            TimeStampMongoDB timeStampMongo = new TimeStampMongoDB(username, spentTimeToday, spentTimeMonth,
                    ZERO_TIME, ZERO_TIME, ZERO_TIME);

            trackerModel.getMongoDB().markTimeToDB(timeStampMongo.toJson());
            log.info("New user in DB created");
        }

        try {
            json = new JSONObject(userInfoFromDb);
            spentTimeToday = json.getString("spentTimeToday");
            spentTimeMonth = json.getString("spentTimeMonth");
        } catch (JSONException | NullPointerException e) {
            log.error("No user with name <" + username  + "> found in DB!\n" + e.getStackTrace());
        }

        labelTimeToday.setText(spentTimeToday);
        labelTimeMonth.setText(spentTimeMonth);
    }

    /**
     * Animate time label to change values every minute
     * TODO: every 5 minutes?
     *
     * @param timeline new time settings
     */
    private void animateTimelabel(Timeline timeline) {

        timeline.setCycleCount( Animation.INDEFINITE );
        timeline.play();
    }

    /**
     * Initialize button and set actions onclick depending on state
     */
    public void trackerButtonEvent() {

        if (!isTracking) {

            startTracking();

        } else {

            final ContextMenu commentValidator = new ContextMenu();

            if (textComment.getText().equals("")) {

                commentValidator.setAutoHide(true);
                commentValidator.getItems().clear();
                commentValidator.getItems().add(
                        new MenuItem("Comment is necessary"));
                commentValidator.show(textComment, Side.TOP,0,0);
                textComment.setOnMouseClicked(event -> {
                    commentValidator.hide();
                });

            } else {
                stopTracking();
            }
        }
    }

    /**
     * Start tracking time: update labels, save in-between values to mongo
     */
    private void startTracking() {

        btnTracking.setText("STOP");
        btnTracking.setStyle("-fx-background-color: #80b3ff");

        startIssueTime = labelIssueTime.getText();

        // schedules the task to be run in an interval
        timerService = Executors.newSingleThreadScheduledExecutor();
        timerService.scheduleAtFixedRate(timerRunnable, 0, 5, TimeUnit.SECONDS);

        //TODO: optimize; how to dynamically pass initial time to keyframe?
        AtomicReference<Float> newTime = new AtomicReference<>(Utils.convertToFloat(startIssueTime));

        minuteTimeline = new Timeline(new KeyFrame(
                Duration.seconds(1),
                event -> {
                    newTime.updateAndGet(v -> v + Utils.convertToFloat("00:01"));
                    labelIssueTime.setText(Utils.convertToString(String.valueOf(newTime.get())));
                }
        ));

        animateTimelabel(minuteTimeline);


        AtomicReference<Float> newTimeToday = new AtomicReference<>(Utils.convertToFloat(labelTimeToday.getText()));

        todayTimeline = new Timeline(new KeyFrame(
                Duration.seconds(1),
                event -> {
                    newTimeToday.updateAndGet(v -> v + Utils.convertToFloat("00:01"));
                    labelTimeToday.setText(Utils.convertToString(String.valueOf(newTimeToday.get())));
                }
        ));

        animateTimelabel(todayTimeline);

        isTracking = true; //start tracking time
    }

    /**
     * Stop tracking time: stop timers, save time entry to redmine/mongo
     */
    private void stopTracking() {

        timerService.shutdown();

        btnTracking.setText("START");
        btnTracking.setStyle("-fx-background-color: #4d94ff");

        minuteTimeline.stop();
        todayTimeline.stop();

        // execute saving time entries to redmine/mongo
        if (!saveTimeEntryService.isRunning()) {
            saveTimeEntryService.reset();
            saveTimeEntryService.start();
        }

    }

    /**
     * Initialize async tasks
     */
    private void initTask() {

        // runnable task for timer: save in-between time to mongo
        timerRunnable = () -> {

            String stopIssueTime = labelIssueTime.getText();
            log.info("Stop issue time: " + stopIssueTime);

            String timeDifference = Utils.getTimeDifference(startIssueTime, stopIssueTime);
            log.info("Difference time: " + timeDifference);

            // send spent time to redmine ONLY
            trackerModel.createMongoTimeEntry(timeDifference, labelTimeToday.getText());
        };

        // saveTimeEntryService task on stop tracking: save time entry to redmone/mongo
        saveTimeEntryService = new Service<Boolean>() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Void call() throws Exception {

                        String stopIssueTime = labelIssueTime.getText();
                        log.info("Stop issue time: " + stopIssueTime);

                        String timeDifference = Utils.getTimeDifference(startIssueTime, stopIssueTime);
                        log.info("Difference time: " + timeDifference);

                        if(!timeDifference.equals("00:00"))
                            // send spent time to redmine & mongo
                            trackerModel.createTimestamp(currentIssueId, currentProjectId, timeDifference,
                                labelTimeToday.getText(), textComment.getText());

                        isTracking = false; //stop tracking time

                        return null;
                    }
                };
            }
        };

    }

    /**
     * Initialize combobox with all projects where current user participates
     */
    private void initProjectsCombobox() {

        Map<String, Integer> map = trackerModel.getRedmineInfo().getProjects();
        ObservableList items = FXCollections.observableArrayList(map.keySet());

        // populate checkbox with items (current user projectsMap)
        cbProjects.setItems(items);
        cbProjects.getSelectionModel().selectFirst();
        currentProjectId = (Integer) map.get(cbProjects.getValue());   // get project ID by name

        // update cbIssues
        cbProjects.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {

            if(minuteTimeline != null && minuteTimeline.getStatus() == Animation.Status.RUNNING) {
                btnTracking.fire();     // stop tracking time for this issue
            }

            currentProjectId = map.get(cbProjects.getValue());   // get project ID by name
            initIssuesCombobox();   //get issues for new projects

            initTimelabels();
        });
    }
    /**
     * Initialize combobox with assigned to current user in chosen project issues
     */
    private void initIssuesCombobox() {

        ChangeListener cl = (ChangeListener<Number>) (observable, oldValue, newValue) -> {

            currentIssueId = (Integer) issuesmap.get(cbIssues.getValue());   // get issue ID by subject

            if (minuteTimeline != null && minuteTimeline.getStatus() == Animation.Status.RUNNING) {
                btnTracking.fire();     // stop tracking time for this issue
            }

            initTimelabels();

        };

        issuesmap = trackerModel.getRedmineInfo().getIssues(currentProjectId);
        ObservableList items = FXCollections.observableArrayList(issuesmap.keySet());

        // populate checbox with items (current user projectsMap)
        cbIssues.getSelectionModel().selectedIndexProperty().removeListener(cl);
        cbIssues.getItems().clear();
        cbIssues.setItems(items);
        cbIssues.getSelectionModel().selectFirst();

        currentIssueId = (Integer) issuesmap.get(cbIssues.getValue());   // get issue ID by subject

        // write spent time on click
        cbIssues.getSelectionModel().selectedIndexProperty().addListener(cl);
    }

}
