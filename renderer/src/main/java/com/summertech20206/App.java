package com.summertech20206;

import javafx.event.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.animation.*;
import javafx.application.Application;
import javafx.scene.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.PhongMaterial;
import org.fxyz3d.shapes.primitives.*;

import eu.mihosoft.jcsg.ext.openjfx.importers.Importer3D;
import eu.mihosoft.vrl.v3d.Text3d;
import org.fxyz3d.shapes.*;
import javafx.geometry.*;

import java.io.IOException;

import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.transform.*;


/**
 * JavaFX App
*/
public class App extends Application {
    
    private Image brickTexture = new Image("https://usbrick.com/wp-content/uploads/2024/11/Ivory-Mortar-1.jpg");
    
    private static final double CONTROL_MULTIPLIER = 0.1;
    private static final double SHIFT_MULTIPLIER = 10.0;
    private static final double MOUSE_SPEED = 0.2;
    private static final double ROTATION_SPEED = 2.0;
    private static final double TRACK_SPEED = 0.3;
    
    RotateTransition rTransition;
    
    private final Group root = new Group();
    private final Group world = new Group();
    private final PerspectiveCamera cam = new PerspectiveCamera(true);
    private final Xform cameraXForm1 = new Xform();
    private final Xform cameraXForm2 = new Xform();
    private final Xform cameraXForm3 = new Xform();
    private final Xform objectGroup = new Xform();
    
    final PhongMaterial monkeyFur = new PhongMaterial();
    final PhongMaterial blueMaterial = new PhongMaterial(Color.DEEPSKYBLUE);
    final PhongMaterial redMaterial = new PhongMaterial();
    final PhongMaterial whiteMaterial = new PhongMaterial(Color.WHITE);
    final PhongMaterial greyMaterial = new PhongMaterial(Color.GREY);
    final PhongMaterial blackMaterial = new PhongMaterial(Color.BLACK);

    private String mode = "rotate"; // default, rotate, or scale
    private Scene mainScene;

    private Xform currentXform = new Xform();
    private int cubeNum;
    private int monkeyNum;
    private int sphereNum;
    
    private Button currentButton = new Button();
    private TilePane scrollTilePane;

    public double mousePosX;
    public double mousePosY;
    public double mouseOldX;
    public double mouseOldY;
    public double mouseDeltaX;
    public double mouseDeltaY;

    private void handleKeyboard(Scene scene, Node group) {
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @SuppressWarnings("incomplete-switch")
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case Z:
                        cameraXForm2.t.setX(0);
                        cameraXForm2.t.setY(0);
                        cameraXForm2.t.setZ(0);
                        cam.setTranslateZ(-450);
                        cameraXForm1.ry.setAngle(0);
                        cameraXForm1.rx.setAngle(10);
                        break;

                    case X:
                        currentXform.setVisible(!currentXform.isVisible());
                        break;

                    case W:
                        currentXform.setTranslateY(currentXform.getTranslateY() + 5);
                        break;

                    case S:
                        currentXform.setTranslateY(currentXform.getTranslateY() - 5);
                        break;

                    case A:
                        currentXform.setTranslateX(currentXform.getTranslateX() + 5);
                        break;

                    case D:
                        currentXform.setTranslateX(currentXform.getTranslateX() - 5);
                        break;

                    case E:
                        currentXform.setTranslateZ(currentXform.getTranslateZ() + 5);
                        break;

                    case Q:
                        currentXform.setTranslateZ(currentXform.getTranslateZ() - 5);
                        break;

                    case DELETE:
                    case BACK_SPACE:
                        currentXform.getChildren().clear();
                        scrollTilePane.getChildren().remove(currentButton);
                        break;

                    case P:
                        if (rTransition.getStatus() == Animation.Status.RUNNING) {
                            rTransition.pause();
                            System.out.println(rTransition.getStatus());
                        } else if (rTransition.getStatus() == Animation.Status.PAUSED) {
                            rTransition.play();
                            System.out.println(rTransition.getStatus());
                        } else {
                            rTransition.play();
                            System.out.println(rTransition.getStatus());
                        }

                }
            }
        });
    }

    private void handleMouse(Node root, Scene scene) {
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent m) {
                mousePosX = m.getSceneX();
                mousePosY = m.getSceneY();
                mouseOldX = m.getSceneX();
                mouseOldY = m.getSceneY();
            }
        });
        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent m) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = m.getSceneX();
                mousePosY = m.getSceneY();

                mouseDeltaX = mousePosX - mouseOldX;
                mouseDeltaY = mousePosY - mouseOldY;

                double modifier = 1.0;

                if (m.isControlDown()) {
                    modifier = CONTROL_MULTIPLIER;
                } else if (m.isShiftDown()) {
                    modifier = SHIFT_MULTIPLIER;
                }

                if (m.isPrimaryButtonDown()) {

                    cameraXForm1.ry.setAngle(cameraXForm1.ry.getAngle() - mouseDeltaX * modifier * ROTATION_SPEED);
                    cameraXForm1.rx.setAngle(cameraXForm1.rx.getAngle() + mouseDeltaY * modifier * ROTATION_SPEED);
                } else if (m.isSecondaryButtonDown()) {

                    cam.setTranslateZ(cam.getTranslateZ() + mouseDeltaX * MOUSE_SPEED * modifier);
                } else if (m.isMiddleButtonDown()) {

                    if (mode == "scale") {
                        currentXform.s.setX(currentXform.s.getY() + mouseDeltaX * MOUSE_SPEED * modifier * TRACK_SPEED);
                        currentXform.s.setY(currentXform.s.getY() + mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED);
                        currentXform.s.setZ(currentXform.s.getY() + mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED);
                    } else if (mode == "rotate") {
                        currentXform.rx
                                .setAngle(
                                        currentXform.rx.getAngle()
                                                + mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED);
                        currentXform.ry
                                .setAngle(
                                        currentXform.ry.getAngle()
                                                + mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED);
                        currentXform.rz
                                .setAngle(
                                        currentXform.rz.getAngle()
                                                + mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED);

                    } else if (mode == "default") {
                        // System.out.println("default mode");
                        // System.out.println(mode);
                        cameraXForm2.t.setX(cameraXForm2.t.getX() + mouseDeltaX * MOUSE_SPEED * modifier * TRACK_SPEED);
                        cameraXForm2.t.setY(cameraXForm2.t.getY() + mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED);
                    } else {
                        System.out.println("something else");
                    }
                }
            }
        });
    }


    @Override
    public void start(Stage stage) throws IOException {

        System.out.println(whiteMaterial.toString());
        monkeyFur.setDiffuseMap(new Image(
                "https://img.magnific.com/free-photo/buffalo-fur-texture-background_1373-289.jpg?semt=ais_hybrid&w=740&q=80"));

        root.getChildren().add(world);
        world.getChildren().addAll(objectGroup);

        stage.setTitle("Mixer 3D");

        TilePane sPane = new TilePane();
        AnchorPane pane = new AnchorPane();
        mainScene = new Scene(pane);
        SubScene scene = new SubScene(root, 1024, 768, true, SceneAntialiasing.BALANCED);
        pane.setPrefHeight(768);
        pane.setPrefWidth(1024);

        Button makeCube = new Button("New Cube");
        Spinner<Integer> cubeSpinner = new Spinner<Integer>(1, 500, 1);

        Button makeSphere = new Button("New Sphere");
        Spinner<Integer> sphereSpinner = new Spinner<Integer>(1, 500, 1);
        sphereSpinner.setEditable(true);

        mode = "default";

        Button changeMode = new Button("Pan Mode");
        Button helpButton = new Button("Help Menu");
        helpButton.setTextFill(Color.RED);
        Button makeMonkey = new Button("New Monkey");

        cubeSpinner.setEditable(true);
        sPane.setPrefHeight(768);
        sPane.setPrefWidth(1024);
        sPane.setPrefTileHeight(60);
        sPane.setPrefColumns(2);
        // BorderStroke[] bStroke = { new BorderStroke(Paint.valueOf("123456"),
        // BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
        // BorderWidths.DEFAULT) };
        // sPane.setBorder(new Border(bStroke));
        sPane.getChildren().add(helpButton);
        sPane.getChildren().addAll(new Rectangle(80, 10, Paint.valueOf("ffffff00")));
        sPane.getChildren().add(changeMode);
        sPane.getChildren().addAll(new Rectangle(80, 10, Paint.valueOf("ffffff00")));
        sPane.getChildren().add(makeMonkey);
        sPane.getChildren().addAll(new Rectangle(80, 10, Paint.valueOf("ffffff00")));
        sPane.getChildren().add(makeCube);
        sPane.getChildren().add(cubeSpinner);
        sPane.getChildren().add(makeSphere);
        sPane.getChildren().add(sphereSpinner);
        pane.getChildren().add(scene);
        pane.getChildren().add(sPane);

        ScrollBar bar = new ScrollBar();
        ScrollPane scrollPane = new ScrollPane(bar);
        scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        scrollPane.setFitToWidth(false);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setMaxHeight(100);
        sPane.getChildren().add(scrollPane);

        scrollTilePane = new TilePane();
        scrollPane.setContent(scrollTilePane);

        cubeNum = 0;

        EventHandler<ActionEvent> makeCubeButton = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                System.out.println("Running make cube method");
                Box box = new Box(cubeSpinner.getValue(), cubeSpinner.getValue(), cubeSpinner.getValue());
                Xform boxXform = new Xform();
                // redMaterial.setDiffuseColor(Color.DARKRED);
                // redMaterial.setSpecularColor(Color.RED);
                box.setMaterial(redMaterial);
                box.setRotationAxis(Rotate.Z_AXIS);
                box.setTranslateX(0);
                box.setTranslateY(0);
                box.setTranslateZ(0);

                boxXform.getChildren().add(box);
                objectGroup.getChildren().add(boxXform);
                System.out.println(objectGroup.getChildren().toString());
                Button cubeSelectButton = new Button("Cube" + cubeNum);
                scrollTilePane.getChildren().add(cubeSelectButton);
                boxXform.setId("BoxXform" + cubeNum);
                cubeSelectButton.setId("Box Button" + cubeNum);

                cubeNum++;

                EventHandler<ActionEvent> selectCubeButton = new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent e) {
                        String idString = e.getSource().toString();
                        int numberOfDigits = idString.length() / 2 - 23;
                        String id = idString.substring(45 + numberOfDigits, idString.length() - 1);
                        System.out.println("Box" + id);
                        currentXform = (Xform) objectGroup.lookup("#BoxXform" + id);
                        currentButton = (Button) e.getSource();

                    }

                };
                cubeSelectButton.setOnAction(selectCubeButton);

            }

        };
        EventHandler<ActionEvent> makeMonkeyButton = new EventHandler<ActionEvent>() {

            public void handle(ActionEvent e) {
                Xform monkey = new Xform();


                Box mainHead = new Box(50, 65, 20);
                Box ear1 = new Box(25, 20, 10);
                Box ear2 = new Box(25, 20, 10);
                Cylinder eyeIris1 = new Cylinder(10, 5);
                Cylinder eyePupil1 = new Cylinder(5, 5);
                Cylinder eyeIris2 = new Cylinder(10, 5);
                Cylinder eyePupil2 = new Cylinder(5, 5);
                Cylinder nostril1 = new Cylinder(3, 5);
                Cylinder nostril2 = new Cylinder(3, 5);
                ConeMesh partyHat = new ConeMesh(32, 10, 20);
                Sphere partyHatTop = new Sphere(3);
                Box mouth = new Box(25, 1, 3);
                mainHead.setMaterial(monkeyFur);
                mainHead.setRotationAxis(Rotate.Z_AXIS);
                mainHead.setTranslateX(0);
                mainHead.setTranslateY(0);
                mainHead.setTranslateZ(0);
                ear1.setMaterial(monkeyFur);
                ear1.setRotationAxis(Rotate.Z_AXIS);
                ear1.setTranslateX(-25);
                ear1.setTranslateY(15);
                ear1.setTranslateZ(0);
                ear2.setMaterial(monkeyFur);
                ear2.setRotationAxis(Rotate.Z_AXIS);
                ear2.setTranslateX(25);
                ear2.setTranslateY(15);
                ear2.setTranslateZ(0);
                eyeIris1.setMaterial(whiteMaterial);
                eyeIris1.setRotationAxis(Rotate.X_AXIS);
                eyeIris1.setRotate(90);
                eyeIris1.setTranslateX(-15);
                eyeIris1.setTranslateY(15);
                eyeIris1.setTranslateZ(-10);
                eyePupil1.setMaterial(blackMaterial);
                eyePupil1.setRotationAxis(Rotate.X_AXIS);
                eyePupil1.setRotate(90);
                eyePupil1.setTranslateX(-15);
                eyePupil1.setTranslateY(15);
                eyePupil1.setTranslateZ(-12);
                eyeIris2.setMaterial(whiteMaterial);
                eyeIris2.setRotationAxis(Rotate.X_AXIS);
                eyeIris2.setRotate(90);
                eyeIris2.setTranslateX(15);
                eyeIris2.setTranslateY(15);
                eyeIris2.setTranslateZ(-10);
                eyePupil2.setMaterial(blackMaterial);
                eyePupil2.setRotationAxis(Rotate.X_AXIS);
                eyePupil2.setRotate(90);
                eyePupil2.setTranslateX(15);
                eyePupil2.setTranslateY(15);
                eyePupil2.setTranslateZ(-12);
                nostril2.setMaterial(blackMaterial);
                nostril2.setRotationAxis(Rotate.X_AXIS);
                nostril2.setRotate(90);
                nostril2.setTranslateX(3);
                nostril2.setTranslateY(5);
                nostril2.setTranslateZ(-10);
                nostril1.setMaterial(blackMaterial);
                nostril1.setRotationAxis(Rotate.X_AXIS);
                nostril1.setRotate(90);
                nostril1.setTranslateX(-3);
                nostril1.setTranslateY(5);
                nostril1.setTranslateZ(-10);
                mouth.setMaterial(blackMaterial);
                mouth.setRotationAxis(Rotate.X_AXIS);
                mouth.setTranslateX(0);
                mouth.setTranslateY(-15);
                mouth.setTranslateZ(-10);
                partyHat.setMaterial(whiteMaterial);
                partyHat.setRotationAxis(Rotate.X_AXIS);
                partyHat.setRotate(180);
                partyHat.setTranslateX(0);
                partyHat.setTranslateY(32);
                partyHat.setTranslateZ(0);
                partyHatTop.setMaterial(blueMaterial);
                partyHatTop.setTranslateX(0);
                partyHatTop.setTranslateY(50);
                partyHatTop.setTranslateZ(0);

                monkey.getChildren().add(mainHead);
                monkey.getChildren().add(ear2);
                monkey.getChildren().add(ear1);
                monkey.getChildren().add(partyHat);
                monkey.getChildren().add(partyHatTop);
                monkey.getChildren().add(eyeIris1);
                monkey.getChildren().add(eyePupil1);
                monkey.getChildren().add(eyeIris2);
                monkey.getChildren().add(eyePupil2);
                monkey.getChildren().add(mouth);
                monkey.getChildren().add(nostril1);
                monkey.getChildren().add(nostril2);

                objectGroup.getChildren().add(monkey);

                Button monkeySelectButton = new Button("Monkey" + monkeyNum);
                scrollTilePane.getChildren().add(monkeySelectButton);
                monkey.setId("Monkey" + monkeyNum);
                monkeySelectButton.setId("Monkey Button" + monkeyNum);

                monkeyNum++;

                EventHandler<ActionEvent> selectMonkeyButton = new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent e) {
                        String idString = e.getSource().toString();
                        int numberOfDigits = idString.length() / 2 - 23;
                        String id = idString.substring(45 + numberOfDigits, idString.length() - 1);

                        System.out.println(idString);
                        System.out.println(id);
                        currentXform = (Xform) objectGroup.lookup("#Mon" + id);
                        currentButton = (Button) e.getSource();
                    }

                };
                monkeySelectButton.setOnAction(selectMonkeyButton);

            }

        };
        EventHandler<ActionEvent> changeModeButton = new EventHandler<ActionEvent>() {

            public void handle(ActionEvent e) {
                if (mode == "default") {
                    mode = "scale";
                    changeMode.setText("Scale");
                } else if (mode == "scale") {
                    mode = "rotate";
                    changeMode.setText("Rotate");
                } else if (mode == "rotate") {
                    mode = "default";
                    changeMode.setText("Pan");
                }
            }
        };
        EventHandler<ActionEvent> makeSphereButton = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                System.out.println("Running make sphere method");
                Sphere sphere = new Sphere(sphereSpinner.getValue());
                Xform sphereXform = new Xform();
                // redMaterial.setDiffuseColor(Color.DARKRED);
                // redMaterial.setSpecularColor(Color.RED);
                sphere.setMaterial(redMaterial);
                sphere.setRotationAxis(Rotate.Z_AXIS);
                sphere.setTranslateX(0);
                sphere.setTranslateY(0);
                sphere.setTranslateZ(0);

                sphereXform.getChildren().add(sphere);
                objectGroup.getChildren().add(sphereXform);
                System.out.println(objectGroup.getChildren().toString());
                Button sphereSelectButton = new Button("Sphere" + sphereNum);
                scrollTilePane.getChildren().add(sphereSelectButton);
                sphereXform.setId("SphereXform" + sphereNum);
                sphereSelectButton.setId("Sphere Button" + sphereNum);

                sphereNum++;

                EventHandler<ActionEvent> selectSphereButton = new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent e) {
                        String idString = e.getSource().toString();
                        int numberOfDigits = idString.length() / 2 - 23;
                        String id = idString.substring(45 + numberOfDigits, idString.length() - 1);
                        System.out.println("Sph" + id);
                        currentXform = (Xform) objectGroup.lookup("#SphereXform" + id.substring(3));
                        System.out.println("#SphereXform" + id.substring(3));

                        currentButton = (Button) e.getSource();
                    }

                };
                sphereSelectButton.setOnAction(selectSphereButton);

            }

        };
        EventHandler<ActionEvent> openHelpMenu = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                createHelpMenu();
            }
        };

        changeMode.setOnAction(changeModeButton);
        makeCube.setOnAction(makeCubeButton);
        makeMonkey.setOnAction(makeMonkeyButton);
        makeSphere.setOnAction(makeSphereButton);
        helpButton.setOnAction(openHelpMenu);

        redMaterial.setDiffuseMap(brickTexture);

        System.out.println(objectGroup.getChildren().toString());

        System.out.println(objectGroup.getChildren().toString());

        root.setDepthTest(DepthTest.ENABLE);
        makeCamera();

        System.out.println(objectGroup.getChildren().toString());

        rTransition = new RotateTransition(Duration.seconds(1),
                objectGroup);
        rTransition.setFromAngle(0);
        rTransition.setToAngle(360);
        rTransition.setCycleCount(RotateTransition.INDEFINITE);
        rTransition.setInterpolator(Interpolator.EASE_BOTH);
        rTransition.pause();
        System.out.println(rTransition.getStatus());

        System.out.println(objectGroup.getChildren().toString());

        scene.setFill(Color.WHITESMOKE);

        handleMouse(world, mainScene);
        handleKeyboard(mainScene, world);

        stage.setResizable(false);
        stage.setScene(mainScene);
        stage.show();
        scene.setCamera(cam);

        // System.out.println(world.getChildren().toString());
        // makeMonkey.fire();
        // ConeMesh torus = new ConeMesh(32, 25, 25);
        // torus.setMaterial(redMaterial);
        // torus.setDrawMode(DrawMode.FILL);
        // torus.setCullFace(CullFace.BACK);
        Text3DMesh text = new Text3DMesh("hello");
        text.setTextureModeImage("https://usbrick.com/wp-content/uploads/2024/11/Ivory-Mortar-1.jpg");
        objectGroup.getChildren().add(text);
        // Node loadedModel = Importer3D.load("src/main/resources/com/summertech20206/monkey.obj");
        // objectGroup.getChildren().add(loadedModel);

    }

    public void createHelpMenu() {
        Stage stage = new Stage();
        BorderPane pane = new BorderPane();
        Scene scene = new Scene(pane);
        Label title = new Label("Help Menu");
        HBox titleBox = new HBox();
        Label keyControls = new Label();
        Label mouseControls = new Label();
        HBox controlsBox = new HBox();

        keyControls.setText(
                "W - Move Selected Object Up \n S - Move Selected Object Down \n A - Move Selected Object Left \n D - Move Selected Object Right \n E - Move Selected Object Forward \n Q - Move Selected Object Backward \n X - Hide Selected Object \n Z - Reset Camera Angle \n Delete / Backspace - Remove Selected Object \n P - Play / Pause \n Shift - Speed Up Mouse Controls \n Control - Slow Down Mouse Controls");
        mouseControls.setText(" \n LMB - Rotate Camera \n RMB - Zoom Camera \n MMB - Rotate, Scale, Or Pan");

        title.setFont(new Font(50));

        titleBox.setAlignment(Pos.CENTER);
        controlsBox.setAlignment(Pos.TOP_CENTER);

        Label warning = new Label("KEEP ONE MONKEY");
        HBox warningBox = new HBox();

        warning.setFont(new Font(50));
        warning.setTextFill(Color.RED);

        warningBox.getChildren().add(warning);
        warningBox.setAlignment(Pos.CENTER);

        controlsBox.getChildren().add(keyControls);
        controlsBox.getChildren().add(mouseControls);
        titleBox.getChildren().add(title);
        pane.setCenter(controlsBox);
        pane.setTop(titleBox);
        pane.setBottom(warningBox);

        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Help Menu");
        stage.setWidth(512);
        stage.setHeight(768);
        stage.show();

    }

    // Xform boxXform = new Xform();

    public void makePlane(int x, int y, int z, int w, int d) {

        Box plane = new Box(w, 0.01, d);
        PhongMaterial planeMaterial = new PhongMaterial();

        planeMaterial.setDiffuseColor(new Color(0.501, 0.501, 0.501, 0.35));

        plane.setMaterial(planeMaterial);
        plane.setRotationAxis(Rotate.Z_AXIS);
        plane.setTranslateX(x);
        plane.setTranslateY(y);
        plane.setTranslateZ(z);

        objectGroup.getChildren().add(plane);

    }

    public void makeCamera() {
        root.getChildren().add(cameraXForm1);
        cameraXForm1.getChildren().add(cameraXForm2);
        cameraXForm2.getChildren().add(cameraXForm3);
        cameraXForm3.getChildren().add(cam);
        cameraXForm3.setRotateZ(180);

        cam.setNearClip(0.1);
        cam.setFarClip(10000);
        cam.setTranslateZ(-450);

        cameraXForm1.ry.setAngle(0);
        cameraXForm1.rx.setAngle(10);
    }

    public static void main(String[] args) {
        launch(args);
    }

}