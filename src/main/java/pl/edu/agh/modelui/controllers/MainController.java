package pl.edu.agh.modelui.controllers;


import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;


import javafx.fxml.FXML;
import pl.edu.agh.OpinionSimulation;
import pl.edu.agh.models.config.ModelConfig;

import pl.edu.agh.models.enums.AgentSelection;
import pl.edu.agh.models.enums.ModelType;
import pl.edu.agh.models.enums.UpdatingStrategy;
import pl.edu.agh.modelui.enums.TimePeriod;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;


import java.util.*;

public class MainController {
    @FXML
    private GridPane grid;
    @FXML
    private ChoiceBox<String> gridSize;
    @FXML
    private Slider majorityModelSlider;
    @FXML
    private Label majorityModelLabel;
    @FXML
    private Slider slider;
    @FXML
    private Label sliderLabel;
    @FXML
    private RadioButton sznajdModelChoice;
    @FXML
    private RadioButton majorityModelChoice;
    @FXML
    private RadioButton voterModelChoice;
    @FXML
    private ChoiceBox<String> agentSelectionChoice;
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button resetButton;
    @FXML
    private ChoiceBox<String> updatingStrategy;
    @FXML
    private ChoiceBox<String> timePeriodChoiceBox;
    @FXML
    private LineChart<String, Number> chart;
    @FXML
    private Axis<Number> yAxis;


    //Helper variables
    private XYChart.Series<String, Number> series;
    private ModelType currentModelType;
    private ModelConfig modelConfig;
    private SimpleGraph<Integer, DefaultEdge> network = new SimpleGraph<>(DefaultEdge.class);
    private Map<Integer, Boolean> opinions = new HashMap<>();
    private OpinionSimulation simulation;
    private boolean simulationOn = false;
    private TimePeriod timePeriod;
    private EnumMap<TimePeriod, Integer> periodMapper = new EnumMap<>(TimePeriod.class);
    private Thread simulationThread;
    private Map<AgentSelection, String> agentSelectionToStringMapper;
    private Map<UpdatingStrategy, String> updatingStrategyToStringMapper;

    private double scale = 1.0;
    private final double minScale = 1.0;
    private final double maxScale = 2.0;
    private double lastMouseX;
    private double lastMouseY;
    private double translateX = 0;
    private double translateY = 0;

    @FXML
    protected void initialize() {
        initilizeMappers();
        initializeUpdatingStrategy();
        initializeButtons();
        initializeModelChoice();
        initializeMajorityModelCoefficient();
        initializeGridSize();
        initializeSlider();
        initializeAgentSelectionChoice();
        initializeSimulation();
        initializeChart();
        initializeGrid();
        initializeTimePeriod();

        drawGrid();
    }

    private void initilizeMappers() {
        agentSelectionToStringMapper = new EnumMap<>(AgentSelection.class);
        agentSelectionToStringMapper.put(AgentSelection.RANDOM, "Losowy");
        agentSelectionToStringMapper.put(AgentSelection.SEQUENTIAL, "Sekwencyjny");

        updatingStrategyToStringMapper = new EnumMap<>(UpdatingStrategy.class);
        updatingStrategyToStringMapper.put(UpdatingStrategy.N_TIMES, "N razy na krok");
        updatingStrategyToStringMapper.put(UpdatingStrategy.ONE_STEP, "1 raz na krok");

    }

    private void initializeTimePeriod() {
        timePeriodChoiceBox.getItems().addAll(
                Arrays.stream(TimePeriod.values())
                        .map(TimePeriod::getValue)
                        .toList()
        );


        timePeriodChoiceBox.setValue(TimePeriod.HUNDRED_MILIS.getValue());
        timePeriod = TimePeriod.HUNDRED_MILIS;

        timePeriodChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            timePeriod = TimePeriod.fromString(newValue);
        });

        for(var value : TimePeriod.values()){
            String[] temp = value.getValue().split(" ");
            if(temp[1].equals("sekundy") || temp[1].equals("sekunda")){
                periodMapper.put(value,Integer.parseInt(temp[0])*1000);
            }
            else{
                periodMapper.put(value,Integer.parseInt(temp[0]));
            }
        }
    }

    private void initializeMajorityModelCoefficient() {
        if(!majorityModelChoice.isSelected()) majorityModelSlider.setDisable(true);

        majorityModelSlider.setValue(0.05);
        majorityModelLabel.setText(String.format("%.2f", majorityModelSlider.getValue()));

        majorityModelSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            majorityModelLabel.setText(String.format("%.2f", newValue));

            simulation.setMajorityModelCoeff(newValue.doubleValue());
        });
    }


    private void initializeGrid() {
        double containerWidth = grid.getPrefWidth();
        double containerHeight = grid.getPrefHeight();

        grid.setOnScroll(event -> {
            double delta = event.getDeltaY();
            if (delta > 0) {
                scale = Math.min(scale + 0.1, maxScale);
            } else {
                scale = Math.max(scale - 0.1, minScale);
            }
            grid.setScaleX(scale);
            grid.setScaleY(scale);

            limitTranslation(containerWidth, containerHeight);
        });

        grid.setOnMousePressed(event -> {
            lastMouseX = event.getSceneX();
            lastMouseY = event.getSceneY();
        });

        grid.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - lastMouseX;
            double deltaY = event.getSceneY() - lastMouseY;

            translateX += deltaX;
            translateY += deltaY;

            limitTranslation(containerWidth, containerHeight);

            grid.setTranslateX(translateX);
            grid.setTranslateY(translateY);

            lastMouseX = event.getSceneX();
            lastMouseY = event.getSceneY();
        });
    }

    private void limitTranslation(double containerWidth, double containerHeight) {
        double gridWidth = grid.getPrefWidth() * scale;
        double gridHeight = grid.getPrefHeight() * scale;

        double maxTranslateX = (gridWidth - containerWidth) / 2;
        double maxTranslateY = (gridHeight - containerHeight) / 2;

        translateX = Math.max(-maxTranslateX, Math.min(maxTranslateX, translateX));
        translateY = Math.max(-maxTranslateY, Math.min(maxTranslateY, translateY));
    }

    private void initializeChart() {
        if (yAxis instanceof NumberAxis numberAxis) {
            numberAxis.setAutoRanging(false);
            numberAxis.setLowerBound(0);
            numberAxis.setUpperBound(100);
            numberAxis.setTickUnit(25);
        }


        series = new XYChart.Series<>();
        series.setName("Wypełnienie niebieskiego");
        chart.getData().add(series);

        //chart.getStylesheets().add(getClass().getResource("/chartStyles.css").toExternalForm());
    }

    private void initializeUpdatingStrategy() {
        updatingStrategy.getItems().addAll(updatingStrategyToStringMapper.values());
        updatingStrategy.setValue(updatingStrategyToStringMapper.get(UpdatingStrategy.N_TIMES));
        updatingStrategy.setOnAction(event -> {
            if(updatingStrategy.getValue().equals("N razy na krok")){
                modelConfig.setUpdatingStrategy(UpdatingStrategy.N_TIMES);
            }
            else{
                modelConfig.setUpdatingStrategy(UpdatingStrategy.ONE_STEP);
            }
            simulation = new OpinionSimulation(modelConfig);
        });
    }

    private void initializeButtons() {
        stopButton.setDisable(true);

        startButton.setOnAction(event -> {
            stopButton.setDisable(false);
            startButton.setDisable(true);

            if (simulationOn) return;
            simulationOn = true;
            series.getData().clear();

            simulationThread = new Thread(() -> {
                disbleOptionsButtons();
                int step = 0;
                while (simulationOn) {
                    try {
                        double falsePercentage = calculatePercentage();
                        int finalStep = step;

                        Platform.runLater(() -> series.getData().add(
                                new XYChart.Data<>(String.valueOf(finalStep), falsePercentage)
                        ));

                        simulation.simulate();
                        Platform.runLater(this::drawGrid);
                        step++;
                        //250
                        Thread.sleep(periodMapper.get(timePeriod));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });

            simulationThread.setDaemon(true);
            simulationThread.start();
        });
        stopButton.setOnAction(event -> {
            enableOptionsButtons();
            startButton.setDisable(false);
            stopButton.setDisable(true);
            simulationOn = false;
            if (simulationThread != null) {
                simulationThread.interrupt();
            }
            simulation.setCurrentNodeForSequential(0);
        });

        resetButton.setOnAction(event -> {
            initializeSimulation();
            drawGrid();
        });

    }

    private void disbleOptionsButtons() {
        slider.setDisable(true);
        resetButton.setDisable(true);
        agentSelectionChoice.setDisable(true);
        updatingStrategy.setDisable(true);
        gridSize.setDisable(true);
        sznajdModelChoice.setDisable(true);
        majorityModelChoice.setDisable(true);
        voterModelChoice.setDisable(true);
        grid.setDisable(true);
        majorityModelSlider.setDisable(true);
        timePeriodChoiceBox.setDisable(true);
    }

    private void enableOptionsButtons() {
        slider.setDisable(false);
        resetButton.setDisable(false);
        agentSelectionChoice.setDisable(false);
        updatingStrategy.setDisable(false);
        gridSize.setDisable(false);
        sznajdModelChoice.setDisable(false);
        majorityModelChoice.setDisable(false);
        voterModelChoice.setDisable(false);
        grid.setDisable(false);
        majorityModelSlider.setDisable(false);
        timePeriodChoiceBox.setDisable(false);
    }

    private void initializeSimulation() {
        modelConfig = new ModelConfig();

        network = new SimpleGraph<>(DefaultEdge.class);

        opinions = new HashMap<>();

        int rows = Integer.parseInt(gridSize.getValue().split("x")[0]);
        int cols = rows;

        int N = rows * cols;

        for (int i = 0; i < N; i++) {
            opinions.put(i, true);
        }


        for (int i = 0; i < rows * cols; i++) {
            network.addVertex(i);
        }

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int current = row * cols + col;

                if (col < cols - 1) {
                    int right = current + 1;
                    network.addEdge(current, right);
                }

                if (row < rows - 1) {
                    int down = current + cols;
                    network.addEdge(current, down);
                }
            }
        }


        modelConfig.setNetwork(network);
        modelConfig.setOpinions(opinions);
        modelConfig.setType(currentModelType);

        if(agentSelectionChoice.getValue().equals("Losowy")){
            modelConfig.setAgentSelection(AgentSelection.RANDOM);
        }
        else{
            modelConfig.setAgentSelection(AgentSelection.SEQUENTIAL);
        }

        if(updatingStrategy.getValue().equals("N razy na krok")){
            modelConfig.setUpdatingStrategy(UpdatingStrategy.N_TIMES);
        }
        else{
            modelConfig.setUpdatingStrategy(UpdatingStrategy.ONE_STEP);
        }
        simulation = new OpinionSimulation(modelConfig);
        simulation.randomizeWithBlueCoefficient(slider.getValue() / 100.0);
    }

    private void initializeAgentSelectionChoice() {
        agentSelectionChoice.getItems().addAll(agentSelectionToStringMapper.values());
        agentSelectionChoice.setValue(agentSelectionToStringMapper.get(AgentSelection.RANDOM));
        agentSelectionChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case "Losowy":
                    modelConfig.setAgentSelection(AgentSelection.RANDOM);
                    break;
                case "Sekwencyjny":
                    modelConfig.setAgentSelection(AgentSelection.SEQUENTIAL);
                    break;
            }
            simulation = new OpinionSimulation(modelConfig);
        });
    }

    private void initializeModelChoice() {
        ToggleGroup toggleGroup = new ToggleGroup();
        sznajdModelChoice.setToggleGroup(toggleGroup);
        majorityModelChoice.setToggleGroup(toggleGroup);
        voterModelChoice.setToggleGroup(toggleGroup);

        sznajdModelChoice.setSelected(true);
        currentModelType = ModelType.SZNAJD;

        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == sznajdModelChoice) {
                currentModelType = ModelType.SZNAJD;
                modelConfig.setType(ModelType.SZNAJD);
                majorityModelSlider.setDisable(true);
            } else if (newValue == majorityModelChoice) {
                currentModelType = ModelType.MAJORITY;
                modelConfig.setType(ModelType.MAJORITY);
                majorityModelSlider.setDisable(false);
            } else if (newValue == voterModelChoice) {
                currentModelType = ModelType.VOTER;
                modelConfig.setType(ModelType.VOTER);
                majorityModelSlider.setDisable(true);
            }
            simulation = new OpinionSimulation(modelConfig);
        });
    }

    private void initializeGridSize() {
        gridSize.getItems().add("5x5");
        gridSize.getItems().add("10x10");
        gridSize.getItems().add("20x20");
        gridSize.getItems().add("30x30");
        gridSize.setValue("30x30");

        gridSize.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                initializeSimulation();
                drawGrid();
            }
        });
    }

    private void initializeSlider() {
        slider.setMax(100.0);
        slider.setMin(0.0);
        slider.setValue(50.0);

        slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                sliderLabel.textProperty().setValue(newValue.intValue() + "%");
                simulation.randomizeWithBlueCoefficient(newValue.doubleValue() / 100.0);
                drawGrid();
            }
        });
    }

    private void drawGrid() {
        String temp = gridSize.getValue().split("x")[0];
        int size = Integer.parseInt(temp);

        int ROWS = size;
        int COLS = size;

        grid.getChildren().clear();

        grid.getRowConstraints().clear();
        grid.getColumnConstraints().clear();

        grid.setGridLinesVisible(true);


        double cellWidth = grid.getPrefWidth() / COLS;
        double cellHeight = grid.getPrefHeight() / ROWS;
        double CELL_SIZE = Math.min(cellWidth, cellHeight);

        for (int i = 0; i < ROWS * 2 - 1; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(100.0 / (ROWS * 2 - 1));
            rowConstraints.setMinHeight(0.0);
            grid.getRowConstraints().add(rowConstraints);
        }

        for (int i = 0; i < COLS * 2 - 1; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(100.0 / (COLS * 2 - 1));
            columnConstraints.setMinWidth(0.0);
            grid.getColumnConstraints().add(columnConstraints);
        }


        for (int row = 0; row < ROWS * 2 - 1; row++) {
            for (int col = 0; col < COLS * 2 - 1; col++) {
                final int currentRow = row;
                final int currentCol = col;
                if (row % 2 == 0 && col % 2 == 0) {
                    Circle node = new Circle(CELL_SIZE / 4);
                    node.setId(row + "," + col);
                    int agent = (row * ROWS) / 2 + col / 2;
                    node.setFill(opinions.get(agent) ? Color.RED : Color.BLUE);
                    grid.add(node, col, row);

                    node.setOnMouseClicked(event -> {
                        System.out.println(node.getId());
                        if (node.getFill() == Color.RED) {
                            node.setFill(Color.BLUE);
                            opinions.put((currentRow * ROWS) / 2 + currentCol / 2, false);
                        } else {
                            node.setFill(Color.RED);
                            opinions.put((currentRow * ROWS) / 2 + currentCol / 2, true);
                        }
                    });
                } else if (row % 2 == 0 || col % 2 == 0) {
                    Line edge = new Line();
                    if (row % 2 == 0) {
                        edge.setStartX(0);
                        edge.setEndX(CELL_SIZE / 2);
                        edge.setStartY(CELL_SIZE / 8);
                        edge.setEndY(CELL_SIZE / 8);
                    } else {
                        edge.setStartX(CELL_SIZE / 8);
                        edge.setEndX(CELL_SIZE / 8);
                        edge.setStartY(0);
                        edge.setEndY(CELL_SIZE / 2);

                        double centerX = (CELL_SIZE - edge.getEndX()) / 4;

                        edge.setTranslateX(centerX);
                    }
                    edge.setStroke(Color.BLACK);
                    edge.setStrokeWidth(4);

                    grid.add(edge, col, row);

                    int v1, v2;
                    if (currentCol % 2 == 0) {
                        v1 = (currentRow / 2) * ROWS + currentCol / 2;
                        v2 = v1 + ROWS;
                    } else {
                        v1 = (currentRow / 2) * ROWS + currentCol / 2;
                        v2 = v1 + 1;
                    }

                    if (network.containsEdge(v1, v2)) {
                        edge.setOpacity(1.0);
                    } else {
                        edge.setOpacity(0);
                    }

                    edge.setOnMouseClicked(event -> {
                        int vertex1, vertex2;
                        if (currentCol % 2 == 0) {
                            vertex1 = (currentRow / 2) * ROWS + currentCol / 2;
                            vertex2 = vertex1 + ROWS;

                        } else {
                            vertex1 = (currentRow / 2) * ROWS + currentCol / 2;
                            vertex2 = vertex1 + 1;
                        }

                        if (edge.getOpacity() == 1.0) {
                            edge.setOpacity(0);
                            network.removeEdge(vertex1, vertex2);
                            System.out.println("Krawędź usunięta: " + vertex1 + " -> " + vertex2);
                        } else {
                            edge.setOpacity(1.0);
                            network.addEdge(vertex1, vertex2);
                            System.out.println("Krawędź dodana: " + vertex1 + " -> " + vertex2);
                        }
                    });


                }
            }
        }
    }

    private double calculatePercentage() {
        long falseCount = opinions.values().stream().filter(x -> !x).count();

        return (double) falseCount / (opinions.values().size()) * 100;
    }
}