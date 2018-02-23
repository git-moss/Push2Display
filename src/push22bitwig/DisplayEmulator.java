package push22bitwig;

import push22bitwig.model.DisplayModel;
import push22bitwig.protocol.UDPReceiver;
import push22bitwig.util.FontCache;
import push22bitwig.util.OperatingSystem;
import push22bitwig.util.PropertiesEx;
import push22bitwig.util.TextInputValidator;

import org.usb4java.LibUsbException;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;


/**
 * Main window which provides the user interface.
 *
 * Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class DisplayEmulator extends Application
{
    private static final String          TAG_TEXT_FONT               = "TEXT_FONT";
    private static final String          TAG_TEXT_COLOR              = "TEXT_COLOR";
    private static final String          TAG_BACKGROUND_COLOR        = "BACKGROUND_COLOR";
    private static final String          TAG_BORDER_COLOR            = "TAG_BORDER_COLOR";
    private static final String          TAG_FADER_COLOR             = "TAG_FADER_COLOR";
    private static final String          TAG_VU_COLOR                = "TAG_VU_COLOR";
    private static final String          TAG_EDIT_COLOR              = "TAG_EDIT_COLOR";
    private static final String          TAG_PORT                    = "PORT";
    private static final String          TAG_PREVIEW                 = "PREVIEW";
    private static final String          TAG_BITWIG_COMMAND          = "BITWIG_COMMAND";
    private static final String          TAG_RUN_AUTOMATICALLY       = "RUN_AUTOMATICALLY";

    private static final String          DEFAULT_BITWIG_PATH_WINDOWS = "C:\\Program Files (x86)\\Bitwig Studio\\Bitwig Studio.exe";
    private static final String          DEFAULT_BITWIG_PATH_MAC     = "/Applications/Bitwig Studio.app";
    private static final String          DEFAULT_BITWIG_PATH_UNIX    = "/opt/bitwig-studio/bitwig-studio";

    private static final int             MIN_WIDTH_LEFT              = 200;

    protected final SimpleStringProperty title                       = new SimpleStringProperty ();
    protected final DisplayModel         displayModel                = new DisplayModel ();
    protected GridPane                   portPane;
    protected GridPane                   centerGridPane;
    protected StackPane                  loggingContainer;

    private File                         configFile                  = null;

    private final UDPReceiver            udpReceiver                 = new UDPReceiver (this.displayModel);
    private final LayoutSettings         layoutSettings              = new LayoutSettings ();
    private final VirtualDisplay         virtualDisplay              = new VirtualDisplay (this.displayModel, this.layoutSettings);
    private final USBDisplay             usbDisplay                  = new USBDisplay ();
    private final Canvas                 canvas                      = new Canvas ();
    private final TextArea               loggingTextArea             = new TextArea ();
    private final TextField              applicationCommand          = new TextField ();
    private final CheckBox               runAutomatically            = new CheckBox ();
    private final ComboBox<String>       fontBox                     = new ComboBox<> ();
    private Stage                        stage;

    private int                          port                        = 7000;
    private boolean                      enablePreview               = true;

    private double                       minWidth                    = 960;
    private double                       minHeight                   = 160;

    protected final PropertiesEx         properties                  = new PropertiesEx ();


    /** {@inheritDoc} */
    @Override
    public void start (final Stage stage)
    {
        this.stage = stage;
        this.setTitle ();
        this.loadConfig ();
        final Scene scene = this.createUI ();
        this.displayModel.addShutdownListener ((ChangeListener<Boolean>) (observable, oldValue, newValue) -> this.exit ());
        this.showStage (stage, scene);
        this.startup ();
    }


    /**
     * Startup the display cycle, the UDP receiption and connect to the display.
     */
    protected void startup ()
    {
        this.startDisplayUpdate ();
        this.startUDPReceiver ();
        this.connectToDisplay ();
    }


    /**
     * Start update display cycle.
     */
    protected void startDisplayUpdate ()
    {
        new AnimationTimer ()
        {
            @Override
            public void handle (final long now)
            {
                DisplayEmulator.this.updateDisplay ();
            }
        }.start ();
    }


    /**
     * Connect to display via USB.
     */
    protected void connectToDisplay ()
    {
        try
        {
            this.usbDisplay.connect ();
        }
        catch (final LibUsbException ex)
        {
            this.displayModel.addLogMessage (ex.getLocalizedMessage ());
        }
    }


    /**
     * Start the UDP receiver.
     */
    protected void startUDPReceiver ()
    {
        this.udpReceiver.start (this.port);
    }


    /**
     * Create the UI of the application.
     *
     * @return The scene
     */
    protected Scene createUI ()
    {
        this.canvas.widthProperty ().set (this.minWidth);
        this.canvas.heightProperty ().set (this.minHeight);

        // The main UI layout
        final StackPane canvasContainer = new StackPane (this.canvas);
        canvasContainer.getStyleClass ().add ("display");
        this.loggingContainer = new StackPane (this.loggingTextArea);
        this.loggingContainer.getStyleClass ().add ("logging");

        // The drawing options

        final VBox leftGridPane = new VBox ();
        leftGridPane.getStyleClass ().add ("vbox");
        final ObservableList<Node> leftChildren = leftGridPane.getChildren ();
        final Label fontBoxLabel = new Label ("Text Font:");
        fontBoxLabel.setLabelFor (this.fontBox);
        leftChildren.add (fontBoxLabel);
        this.fontBox.getItems ().setAll (GraphicsEnvironment.getLocalGraphicsEnvironment ().getAvailableFontFamilyNames ());
        leftChildren.add (this.fontBox);
        this.fontBox.setMinWidth (MIN_WIDTH_LEFT);

        final ColorPicker backgroundColorButton = addColorPicker ("Background Color:", leftChildren);
        final ColorPicker borderColorButton = addColorPicker ("Border Color:", leftChildren);
        final ColorPicker textColorButton = addColorPicker ("Text Color:", leftChildren);
        final ColorPicker faderColorButton = addColorPicker ("Fader Color:", leftChildren);
        final ColorPicker vuColorButton = addColorPicker ("VU Meter Color:", leftChildren);
        final ColorPicker editColorButton = addColorPicker ("Edit Color:", leftChildren);

        final Button resetButton = new Button ("Reset");
        resetButton.setOnAction (e -> this.layoutSettings.reset ());
        leftChildren.add (resetButton);
        resetButton.setMinWidth (200);

        // The DAW executable path
        this.centerGridPane = new GridPane ();
        this.centerGridPane.getStyleClass ().add ("grid");
        final Label dawPathLabel = new Label ("DAW Path:");
        dawPathLabel.setLabelFor (this.applicationCommand);
        this.applicationCommand.setPrefWidth (400);
        this.centerGridPane.add (dawPathLabel, 0, 0);
        final Label runAutomaticallyLabel = new Label ("Run automatically");
        runAutomaticallyLabel.setLabelFor (this.runAutomatically);
        this.centerGridPane.add (new HBox (this.runAutomatically, runAutomaticallyLabel), 2, 0);
        final Button selectFileButton = new Button ("...");
        selectFileButton.setOnAction (e -> this.selectDAWExecutable ());
        final Button runButton = new Button ("Run");
        runButton.setOnAction (e -> this.runDAW ());
        this.centerGridPane.add (new BorderPane (this.applicationCommand, null, new BorderPane (null, null, runButton, null, selectFileButton), null, null), 0, 1, 3, 1);

        // The display port configuration
        final TextField portField = new TextField (Integer.toString (this.port));
        portField.setPrefWidth (80);
        TextInputValidator.limitToNumbers (portField);
        final Button applyButton = new Button ("Apply");
        applyButton.setOnAction (e -> {
            final int newPort = Integer.parseInt (portField.getText ());
            if (newPort == this.port)
                return;
            this.port = newPort;
            this.startUDPReceiver ();
        });
        this.portPane = new GridPane ();
        this.portPane.getStyleClass ().add ("grid");
        final Label displayPortLabel = new Label ("Display Port:");
        displayPortLabel.setLabelFor (portField);
        this.portPane.add (displayPortLabel, 0, 0);
        this.portPane.add (new BorderPane (portField, null, applyButton, null, null), 0, 1);

        // All options
        final BorderPane upperPane = new BorderPane (this.centerGridPane, null, this.portPane, null, null);
        upperPane.getStyleClass ().add ("upperPane");
        final BorderPane centerPart = new BorderPane (this.loggingContainer, upperPane, null, null, null);
        final BorderPane optionsPane = new BorderPane (centerPart, null, null, null, leftGridPane);

        final CheckBox enablePreviewBox = new CheckBox ();
        enablePreviewBox.setOnAction (e -> this.enablePreview = enablePreviewBox.isSelected ());
        enablePreviewBox.setSelected (this.enablePreview);
        final Label previewLabel = new Label ("Preview");
        previewLabel.setLabelFor (enablePreviewBox);
        final HBox previewBoxPane = new HBox (enablePreviewBox, previewLabel);
        previewBoxPane.getStyleClass ().add ("preview");

        final BorderPane root = new BorderPane (previewBoxPane, canvasContainer, null, optionsPane, null);
        final Scene scene = new Scene (root, javafx.scene.paint.Color.TRANSPARENT);
        scene.getStylesheets ().add ("css/DefaultStyles.css");

        // Set and link drawing options in both directions
        textColorButton.valueProperty ().addListener ((ChangeListener<Color>) (observable, oldValue, newValue) -> {
            this.layoutSettings.setTextColor (toAWTColor (newValue));
        });
        backgroundColorButton.valueProperty ().addListener ((ChangeListener<Color>) (observable, oldValue, newValue) -> {
            this.layoutSettings.setBackgroundColor (toAWTColor (newValue));
        });
        borderColorButton.valueProperty ().addListener ((ChangeListener<Color>) (observable, oldValue, newValue) -> {
            this.layoutSettings.setBorderColor (toAWTColor (newValue));
        });
        faderColorButton.valueProperty ().addListener ((ChangeListener<Color>) (observable, oldValue, newValue) -> {
            this.layoutSettings.setFaderColor (toAWTColor (newValue));
        });
        vuColorButton.valueProperty ().addListener ((ChangeListener<Color>) (observable, oldValue, newValue) -> {
            this.layoutSettings.setVuColor (toAWTColor (newValue));
        });
        editColorButton.valueProperty ().addListener ((ChangeListener<Color>) (observable, oldValue, newValue) -> {
            this.layoutSettings.setEditColor (toAWTColor (newValue));
        });

        this.layoutSettings.getTextColorProperty ().addListener ((ChangeListener<java.awt.Color>) (observable, oldValue, newValue) -> {
            textColorButton.valueProperty ().set (toFXColor (newValue));
            SVGImage.clearCache ();
        });
        this.layoutSettings.getBackgroundColorProperty ().addListener ((ChangeListener<java.awt.Color>) (observable, oldValue, newValue) -> {
            backgroundColorButton.valueProperty ().set (toFXColor (newValue));
        });
        this.layoutSettings.getBorderColorProperty ().addListener ((ChangeListener<java.awt.Color>) (observable, oldValue, newValue) -> {
            borderColorButton.valueProperty ().set (toFXColor (newValue));
        });
        this.layoutSettings.getFaderColorProperty ().addListener ((ChangeListener<java.awt.Color>) (observable, oldValue, newValue) -> {
            faderColorButton.valueProperty ().set (toFXColor (newValue));
        });
        this.layoutSettings.getVUColorProperty ().addListener ((ChangeListener<java.awt.Color>) (observable, oldValue, newValue) -> {
            vuColorButton.valueProperty ().set (toFXColor (newValue));
        });
        this.layoutSettings.getEditColorProperty ().addListener ((ChangeListener<java.awt.Color>) (observable, oldValue, newValue) -> {
            editColorButton.valueProperty ().set (toFXColor (newValue));
        });

        this.fontBox.getSelectionModel ().selectedItemProperty ().addListener ((ChangeListener<String>) (observable, oldValue, newValue) -> {
            this.layoutSettings.setTextFont (newValue);
        });
        final Font textFont = this.layoutSettings.getTextFont ();
        this.fontBox.getSelectionModel ().select (textFont.getFamily ());

        this.layoutSettings.getTextFontProperty ().addListener ((ChangeListener<FontCache>) (observable, oldValue, newValue) -> {
            this.fontBox.getSelectionModel ().select (newValue.getBaseFont ().getFamily ());
        });

        textColorButton.valueProperty ().set (toFXColor (this.layoutSettings.getTextColor ()));
        backgroundColorButton.valueProperty ().set (toFXColor (this.layoutSettings.getBackgroundColor ()));
        borderColorButton.valueProperty ().set (toFXColor (this.layoutSettings.getBorderColor ()));
        faderColorButton.valueProperty ().set (toFXColor (this.layoutSettings.getFaderColor ()));
        vuColorButton.valueProperty ().set (toFXColor (this.layoutSettings.getVuColor ()));
        editColorButton.valueProperty ().set (toFXColor (this.layoutSettings.getEditColor ()));

        this.loggingTextArea.textProperty ().bind (this.displayModel.getLogMessageProperty ());
        return scene;
    }


    /** {@inheritDoc} */
    @Override
    public void stop () throws Exception
    {
        this.displayModel.addLogMessage ("Storing configuration...");
        this.saveConfig ();

        this.displayModel.addLogMessage ("Stopping UDP...");
        this.udpReceiver.stop ();
        this.usbDisplay.disconnect ();

        super.stop ();
    }


    /**
     * Update the virtual and real display.
     */
    void updateDisplay ()
    {
        final BufferedImage image = this.virtualDisplay.getImage ();
        this.usbDisplay.send (image);
        if (!this.enablePreview)
            return;
        final GraphicsContext gc = this.canvas.getGraphicsContext2D ();
        gc.drawImage (SwingFXUtils.toFXImage (image, null), 0, 0, this.canvas.getWidth (), this.canvas.getWidth () / 6);
    }


    /**
     * Exits the program and consumes the event.
     *
     * @param event The event to consume
     */
    protected void exit (final WindowEvent event)
    {
        this.displayModel.addLogMessage ("Window exit...");
        event.consume ();
        this.exit ();
    }


    /**
     * Exits the application.
     */
    public void exit ()
    {
        this.displayModel.addLogMessage ("Exiting platform...");
        Platform.exit ();
        this.displayModel.addLogMessage ("Exiting platform... Done");
    }


    /**
     * Set the applications title.
     */
    protected void setTitle ()
    {
        final StringBuilder title = new StringBuilder ("Push 2 Display");
        final Package p = Package.getPackage ("push22bitwig");
        if (p != null)
        {
            final String implementationVersion = p.getImplementationVersion ();
            if (implementationVersion != null)
                title.append (" v").append (implementationVersion);
        }
        this.title.set (title.toString ());
    }


    /**
     * Configures and shows the stage.
     *
     * @param stage The stage to start
     * @param scene The scene to set
     */
    protected void showStage (final Stage stage, final Scene scene)
    {
        stage.titleProperty ().bind (this.title);
        stage.setResizable (false);

        final InputStream rs = ClassLoader.getSystemResourceAsStream ("images/AppIcon.gif");
        if (rs != null)
            stage.getIcons ().add (new Image (rs));

        stage.setScene (scene);

        stage.setOnCloseRequest (event -> this.exit (event));
        stage.show ();
    }


    /**
     * Load the settings from the config file.
     */
    protected void loadConfig ()
    {
        this.configFile = new File (this.getConfigurationFilename ());

        if (this.configFile.exists ())
        {
            try (final FileReader reader = new FileReader (this.configFile))
            {
                this.properties.load (reader);

                this.properties.restoreStagePlacement (this.stage);

                // Text font
                final String textFont = this.properties.getString (TAG_TEXT_FONT);
                if (textFont != null)
                    this.layoutSettings.setTextFont (textFont);

                // Text color
                final int textColor = this.properties.getInt (TAG_TEXT_COLOR);
                if (textColor != -1)
                    this.layoutSettings.setTextColor (new java.awt.Color (textColor));

                // Background color
                final int backgroundColor = this.properties.getInt (TAG_BACKGROUND_COLOR);
                if (backgroundColor != -1)
                    this.layoutSettings.setBackgroundColor (new java.awt.Color (backgroundColor));

                // Border color
                final int borderColor = this.properties.getInt (TAG_BORDER_COLOR);
                if (borderColor != -1)
                    this.layoutSettings.setBorderColor (new java.awt.Color (borderColor));

                // Fader color
                final int faderColor = this.properties.getInt (TAG_FADER_COLOR);
                if (faderColor != -1)
                    this.layoutSettings.setFaderColor (new java.awt.Color (faderColor));

                // VU color
                final int vuColor = this.properties.getInt (TAG_VU_COLOR);
                if (vuColor != -1)
                    this.layoutSettings.setVuColor (new java.awt.Color (vuColor));

                // Edit color
                final int editColor = this.properties.getInt (TAG_EDIT_COLOR);
                if (editColor != -1)
                    this.layoutSettings.setEditColor (new java.awt.Color (editColor));

                // Other settings
                this.port = this.properties.getInt (TAG_PORT, 7000);
                this.enablePreview = this.properties.getBoolean (TAG_PREVIEW, true);
                this.applicationCommand.setText (this.properties.getString (TAG_BITWIG_COMMAND, this.getDefaultApplicationPath ()));
                this.runAutomatically.setSelected (this.properties.getBoolean (TAG_RUN_AUTOMATICALLY, true));

                SVGImage.clearCache ();
            }
            catch (final IOException ex)
            {
                this.displayModel.addLogMessage (ex.getLocalizedMessage ());
            }
        }
        else
        {
            this.applicationCommand.setText (this.getDefaultApplicationPath ());
            this.runAutomatically.setSelected (true);
        }

        if (this.runAutomatically.isSelected ())
            this.runDAW ();
    }


    /**
     * Get the name of the configuration file.
     *
     * @return The name of the configuration file.
     */
    protected String getConfigurationFilename ()
    {
        return "Push2Display.config";
    }


    /**
     * Get the UDP receiver.
     *
     * @return The UDP receiver
     */
    protected UDPReceiver getUdpReceiver ()
    {
        return this.udpReceiver;
    }


    /**
     * Get the default full path to the application to start.
     *
     * @return The application path depending on the operating system
     */
    protected String getDefaultApplicationPath ()
    {
        switch (OperatingSystem.get ())
        {
            case WINDOWS:
                return DEFAULT_BITWIG_PATH_WINDOWS;
            case MAC:
                return DEFAULT_BITWIG_PATH_MAC;
            default:
                return DEFAULT_BITWIG_PATH_UNIX;
        }
    }


    /**
     * Save the settings from the config file.
     */
    protected void saveConfig ()
    {
        this.properties.storeStagePlacement (this.stage);

        this.properties.putString (TAG_TEXT_FONT, this.layoutSettings.getTextFont ().getFamily ());
        this.properties.putInt (TAG_TEXT_COLOR, this.layoutSettings.getTextColor ().getRGB ());
        this.properties.putInt (TAG_BACKGROUND_COLOR, this.layoutSettings.getBackgroundColor ().getRGB ());
        this.properties.putInt (TAG_BORDER_COLOR, this.layoutSettings.getBorderColor ().getRGB ());
        this.properties.putInt (TAG_FADER_COLOR, this.layoutSettings.getFaderColor ().getRGB ());
        this.properties.putInt (TAG_VU_COLOR, this.layoutSettings.getVuColor ().getRGB ());
        this.properties.putInt (TAG_EDIT_COLOR, this.layoutSettings.getEditColor ().getRGB ());

        this.properties.putInt (TAG_PORT, this.port);
        this.properties.putBoolean (TAG_PREVIEW, this.enablePreview);
        this.properties.putString (TAG_BITWIG_COMMAND, this.applicationCommand.getText ());
        this.properties.putBoolean (TAG_RUN_AUTOMATICALLY, this.runAutomatically.isSelected ());

        try (final FileWriter writer = new FileWriter (this.configFile))
        {
            this.properties.store (writer, "");
        }
        catch (final IOException ex)
        {
            final String message = new StringBuilder ("Could not store to: ").append (this.configFile.getAbsolutePath ()).append ('\n').append (ex.getLocalizedMessage ()).toString ();
            this.displayModel.addLogMessage (message);
            this.message (message);
        }
    }


    /**
     * Adds a color picker.
     *
     * @param labelText The label text for the color picker
     * @param children Where to add
     * @return The picker
     */
    private static ColorPicker addColorPicker (final String labelText, final ObservableList<Node> children)
    {
        final ColorPicker picker = new ColorPicker ();
        final Label label = new Label (labelText);
        label.setLabelFor (picker);
        children.add (label);
        children.add (picker);
        picker.setMinWidth (MIN_WIDTH_LEFT);
        return picker;
    }


    static java.awt.Color toAWTColor (final Color color)
    {
        return new java.awt.Color ((int) (color.getRed () * 255), (int) (color.getGreen () * 255), (int) (color.getBlue () * 255));
    }


    static Color toFXColor (final java.awt.Color color)
    {
        return Color.rgb (color.getRed (), color.getGreen (), color.getBlue ());
    }


    /**
     * Select the Bitwig executable.
     */
    private void selectDAWExecutable ()
    {
        final FileChooser chooser = new FileChooser ();
        chooser.setTitle ("Select the DAW executable file");

        if (OperatingSystem.get () == OperatingSystem.WINDOWS)
        {
            final ExtensionFilter filter = new ExtensionFilter ("Executable", "*.exe");
            chooser.getExtensionFilters ().add (filter);
        }

        final File file = chooser.showOpenDialog (this.stage);
        if (file != null)
            this.applicationCommand.setText (file.getAbsolutePath ());
    }


    /**
     * Start the DAW.
     */
    private void runDAW ()
    {
        Platform.runLater ( () -> {
            try
            {
                final String text = this.applicationCommand.getText ();
                switch (OperatingSystem.get ())
                {
                    case MAC:
                        final String [] cmd = new String []
                        {
                            "open",
                            text
                        };
                        Runtime.getRuntime ().exec (cmd);
                        break;
                    default:
                        Runtime.getRuntime ().exec (text);
                        break;
                }
            }
            catch (final IOException ex)
            {
                this.displayModel.addLogMessage (ex.getLocalizedMessage ());
            }
        });
    }


    /**
     * Shows a message dialog. If the message starts with a '@' the message is interpreted as a
     * identifier for a string located in the resource file.
     *
     * @param message The message to display or a resource key
     * @see ResourceBundle#getString
     */
    private void message (final String message)
    {
        final Alert alert = new Alert (AlertType.INFORMATION);
        alert.setTitle (null);
        alert.setHeaderText (null);
        alert.setContentText (message);
        alert.initOwner (this.stage);
        alert.showAndWait ();
    }
}
