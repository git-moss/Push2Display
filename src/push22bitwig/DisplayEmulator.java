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


/**
 * Main window which provides the user interface.
 * 
 * Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class DisplayEmulator extends Application
{
    private static final String        TAG_TEXT_FONT               = "TEXT_FONT";
    private static final String        TAG_TEXT_COLOR              = "TEXT_COLOR";
    private static final String        TAG_BACKGROUND_COLOR        = "BACKGROUND_COLOR";
    private static final String        TAG_PORT                    = "PORT";
    private static final String        TAG_PREVIEW                 = "PREVIEW";
    private static final String        TAG_BITWIG_COMMAND          = "BITWIG_COMMAND";
    private static final String        TAG_RUN_AUTOMATICALLY       = "RUN_AUTOMATICALLY";

    private static final String        DEFAULT_BITWIG_PATH_WINDOWS = "C:\\Program Files (x86)\\Bitwig Studio\\Bitwig Studio.exe";
    private static final String        DEFAULT_BITWIG_PATH_MAC     = "/Applications/Bitwig Studio.app";
    private static final String        DEFAULT_BITWIG_PATH_UNIX    = "/opt/bitwig-studio/bitwig-studio";

    private static final int           MIN_WIDTH_LEFT              = 200;

    private final File                 configFile                  = new File ("Push2Display.config");
    private final PropertiesEx         properties                  = new PropertiesEx ();

    private final DisplayModel         model                       = new DisplayModel ();
    private final UDPReceiver          udpReceiver                 = new UDPReceiver (this.model);
    private final LayoutSettings       layoutSettings              = new LayoutSettings ();
    private final VirtualDisplay       virtualDisplay              = new VirtualDisplay (this.model, this.layoutSettings);
    private final USBDisplay           usbDisplay                  = new USBDisplay ();
    private final SimpleStringProperty title                       = new SimpleStringProperty ();
    private final Canvas               canvas                      = new Canvas ();
    private final TextArea             loggingTextArea             = new TextArea ();
    private final TextField            bitwigCommand               = new TextField ();
    private final CheckBox             runAutomatically            = new CheckBox ();
    private final ComboBox<String>     fontBox                     = new ComboBox<> ();
    private Stage                      stage;

    private int                        port                        = 7000;
    private boolean                    enablePreview               = true;

    private double                     minWidth                    = 960;
    private double                     minHeight                   = 160;


    /**
     * Constructor.
     */
    public DisplayEmulator ()
    {
        final StringBuilder title = new StringBuilder ("Push 2 Display");
        final Package p = Package.getPackage ("push22bitwig");
        if (p != null)
        {
            final String implementationVersion = p.getImplementationVersion ();
            if (implementationVersion != null)
                title.append (' ').append (implementationVersion);
        }
        this.title.set (title.toString ());
    }


    /** {@inheritDoc} */
    @Override
    public void start (final Stage stage)
    {
        this.stage = stage;

        this.loadConfig ();

        this.canvas.widthProperty ().set (this.minWidth);
        this.canvas.heightProperty ().set (this.minHeight);

        // The main UI layout
        final StackPane canvasContainer = new StackPane (this.canvas);
        canvasContainer.getStyleClass ().add ("display");
        final StackPane loggingContainer = new StackPane (this.loggingTextArea);
        loggingContainer.getStyleClass ().add ("logging");
        final BorderPane root = new BorderPane (null, canvasContainer, null, null, null);
        final Scene scene = new Scene (root, javafx.scene.paint.Color.TRANSPARENT);
        scene.getStylesheets ().add ("css/DefaultStyles.css");

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

        // The Bitwig executable path
        final GridPane centerGridPane = new GridPane ();
        centerGridPane.getStyleClass ().add ("grid");
        final Label bitwigPathLabel = new Label ("Bitwig Path:");
        bitwigPathLabel.setLabelFor (this.bitwigCommand);
        this.bitwigCommand.setPrefWidth (400);
        centerGridPane.add (bitwigPathLabel, 0, 0);
        final Label runAutomaticallyLabel = new Label ("Run automatically");
        runAutomaticallyLabel.setLabelFor (this.runAutomatically);
        centerGridPane.add (new HBox (this.runAutomatically, runAutomaticallyLabel), 2, 0);
        final Button selectFileButton = new Button ("...");
        selectFileButton.setOnAction (e -> this.selectBitwigFile ());
        final Button runButton = new Button ("Run");
        runButton.setOnAction (e -> this.runBitwig ());
        centerGridPane.add (new BorderPane (this.bitwigCommand, null, new BorderPane (null, null, runButton, null, selectFileButton), null, null), 0, 1, 3, 1);

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
            this.udpReceiver.start (this.port);
        });
        final GridPane rightGridPane = new GridPane ();
        rightGridPane.getStyleClass ().add ("grid");
        final Label displayPortLabel = new Label ("Display Port:");
        displayPortLabel.setLabelFor (portField);
        rightGridPane.add (displayPortLabel, 0, 0);
        rightGridPane.add (new BorderPane (portField, null, applyButton, null, null), 0, 1);

        // All options
        final BorderPane upperPane = new BorderPane (centerGridPane, null, rightGridPane, null, null);
        rightGridPane.getStyleClass ().add ("upperPane");
        final BorderPane centerPart = new BorderPane (loggingContainer, upperPane, null, null, null);
        final BorderPane optionsPane = new BorderPane (centerPart, null, null, null, leftGridPane);

        final CheckBox enablePreviewBox = new CheckBox ();
        enablePreviewBox.setOnAction (e -> this.enablePreview = enablePreviewBox.isSelected ());
        enablePreviewBox.setSelected (this.enablePreview);
        final Label previewLabel = new Label ("Preview");
        previewLabel.setLabelFor (enablePreviewBox);
        final HBox previewBoxPane = new HBox (enablePreviewBox, previewLabel);
        previewBoxPane.getStyleClass ().add ("preview");
        root.setCenter (previewBoxPane);
        root.setBottom (optionsPane);

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

        this.loggingTextArea.textProperty ().bind (this.model.getLogMessageProperty ());

        this.model.addShutdownListener ((ChangeListener<Boolean>) (observable, oldValue, newValue) -> this.exit ());

        this.showStage (stage, scene);

        // Update & render loop
        new AnimationTimer ()
        {
            @Override
            public void handle (final long now)
            {
                DisplayEmulator.this.updateDisplay ();
            }
        }.start ();

        try
        {
            this.udpReceiver.start (this.port);
            this.usbDisplay.connect ();
        }
        catch (final LibUsbException ex)
        {
            this.model.addLogMessage (ex.getLocalizedMessage ());
        }
    }


    /** {@inheritDoc} */
    @Override
    public void stop () throws Exception
    {
        this.udpReceiver.stop ();
        this.usbDisplay.disconnect ();

        this.saveConfig ();

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
    private void exit (final WindowEvent event)
    {
        event.consume ();
        this.exit ();
    }


    /**
     * Exits the application.
     */
    public void exit ()
    {
        Platform.exit ();
    }


    /**
     * Configures and shows the stage.
     *
     * @param stage The stage to start
     * @param scene The scene to set
     */
    private void showStage (final Stage stage, final Scene scene)
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
    private void loadConfig ()
    {
        if (this.configFile.exists ())
        {

            try (final FileReader reader = new FileReader (this.configFile))
            {
                this.properties.load (reader);

                final String textFont = this.properties.getString (TAG_TEXT_FONT);
                if (textFont != null)
                    this.layoutSettings.setTextFont (textFont);

                final int textColor = this.properties.getInt (TAG_TEXT_COLOR, java.awt.Color.ORANGE.getRGB ());
                this.layoutSettings.setTextColor (new java.awt.Color (textColor));
                SVGImage.clearCache ();

                final int backgroundColor = this.properties.getInt (TAG_BACKGROUND_COLOR, java.awt.Color.BLACK.getRGB ());
                this.layoutSettings.setBackgroundColor (new java.awt.Color (backgroundColor));

                this.port = this.properties.getInt (TAG_PORT, 7000);
                this.enablePreview = this.properties.getBoolean (TAG_PREVIEW, true);

                this.bitwigCommand.setText (this.properties.getString (TAG_BITWIG_COMMAND, getDefaultBitwigPath ()));
                this.runAutomatically.setSelected (this.properties.getBoolean (TAG_RUN_AUTOMATICALLY, true));
            }
            catch (final IOException ex)
            {
                this.model.addLogMessage (ex.getLocalizedMessage ());
            }
        }
        else
        {
            this.bitwigCommand.setText (getDefaultBitwigPath ());
            this.runAutomatically.setSelected (true);
        }

        if (this.runAutomatically.isSelected ())
            this.runBitwig ();
    }


    private static String getDefaultBitwigPath ()
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
    private void saveConfig ()
    {
        this.properties.putString (TAG_TEXT_FONT, this.layoutSettings.getTextFont ().getFamily ());
        this.properties.putInt (TAG_TEXT_COLOR, this.layoutSettings.getTextColor ().getRGB ());
        this.properties.putInt (TAG_BACKGROUND_COLOR, this.layoutSettings.getBackgroundColor ().getRGB ());
        this.properties.putInt (TAG_PORT, this.port);
        this.properties.putBoolean (TAG_PREVIEW, this.enablePreview);
        this.properties.putString (TAG_BITWIG_COMMAND, this.bitwigCommand.getText ());
        this.properties.putBoolean (TAG_RUN_AUTOMATICALLY, this.runAutomatically.isSelected ());

        try (final FileWriter writer = new FileWriter (this.configFile))
        {
            this.properties.store (writer, "");
        }
        catch (final IOException ex)
        {
            this.model.addLogMessage (ex.getLocalizedMessage ());
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
    private void selectBitwigFile ()
    {
        final FileChooser chooser = new FileChooser ();
        chooser.setTitle ("Select the Bitwig Studio executable file");

        if (OperatingSystem.get () == OperatingSystem.WINDOWS)
        {
            final ExtensionFilter filter = new ExtensionFilter ("Executable", "*.exe");
            chooser.getExtensionFilters ().add (filter);
        }

        final File file = chooser.showOpenDialog (this.stage);
        if (file != null)
            this.bitwigCommand.setText (file.getAbsolutePath ());
    }


    /**
     * Start Bitwig.
     */
    private void runBitwig ()
    {
        Platform.runLater ( () -> {
            try
            {
                final String text = this.bitwigCommand.getText ();
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
                this.model.addLogMessage (ex.getLocalizedMessage ());
            }
        });
    }
}
