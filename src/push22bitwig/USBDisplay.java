package push22bitwig;

import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;


/**
 * Connects to the display of the Push 2 via USB.
 * 
 * Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class USBDisplay
{
    /** The pixel width of the display. */
    private static final int     WIDTH            = 960;
    /** The pixel height of the display. */
    private static final int     HEIGHT           = 160;

    /** The size of the display header. */
    private static final int     HDR_SZ           = 0x10;
    /** The size of the display content. */
    private static final int     DATA_SZ          = 20 * 0x4000;

    private static final byte [] DISPLAY_HEADER   =
    {
            (byte) 0xef,
            (byte) 0xcd,
            (byte) 0xab,
            (byte) 0x89,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0
    };

    /** Push 2 USB Vendor ID. */
    private static final short   VENDOR_ID        = 0x2982;
    /** Push 2 USB Product ID. */
    private static final short   PRODUCT_ID       = 0x1967;
    /** Push 2 USB Interface for the display. */
    private static final int     INTERFACE_NUMBER = 0;

    private DeviceHandle         handle;


    /**
     * Connect to the USB port and claim the display interface.
     */
    public void connect ()
    {
        int result = LibUsb.init (null);
        if (result != LibUsb.SUCCESS)
            throw new LibUsbException ("Unable to initialize libusb.", result);

        this.handle = LibUsb.openDeviceWithVidPid (null, VENDOR_ID, PRODUCT_ID);
        if (this.handle == null)
            throw new LibUsbException ("Unable to open USB device", result);

        result = LibUsb.claimInterface (this.handle, INTERFACE_NUMBER);
        if (result != LibUsb.SUCCESS)
            throw new LibUsbException ("Unable to claim interface", result);
    }


    /**
     * Send the image to the screen.
     *
     * @param image An image of size 960 x 160 pixel
     */
    public void send (final Image image)
    {
        if (this.handle == null)
            return;

        final ByteBuffer header = ByteBuffer.allocateDirect (HDR_SZ);
        final ByteBuffer buffer = ByteBuffer.allocateDirect (DATA_SZ);
        final IntBuffer transfered = IntBuffer.allocate (1);

        header.put (DISPLAY_HEADER);

        final PixelReader reader = image.getPixelReader ();
        for (int y = 0; y < HEIGHT; y++)
        {
            for (int x = 0; x < WIDTH; x++)
            {
                final Color color = reader.getColor (x, y);

                // 3b(low) green - 5b red / 5b blue - 3b (high) green, e.g. gggRRRRR BBBBBGGG

                final int red = (int) Math.round (color.getRed () * 31);
                final int green = (int) Math.round (color.getGreen () * 63);
                final int blue = (int) Math.round (color.getBlue () * 31);
                buffer.put ((byte) ((green & 0x07) << 5 | red & 0x1F));
                buffer.put ((byte) ((blue & 0x1F) << 3 | (green & 0x38) >> 3));
            }
            for (int x = 0; x < 128; x++)
                buffer.put ((byte) 0x00);
        }

        LibUsb.bulkTransfer (this.handle, (byte) 0x01, header, transfered, 1000L);
        LibUsb.bulkTransfer (this.handle, (byte) 0x01, buffer, transfered, 1000L);
    }


    /**
     * Send the buffered image to the screen.
     *
     * @param image An image of size 960 x 160 pixel
     */
    public void send (final BufferedImage image)
    {
        if (this.handle == null)
            return;

        final ByteBuffer header = ByteBuffer.allocateDirect (HDR_SZ);
        final ByteBuffer buffer = ByteBuffer.allocateDirect (DATA_SZ);
        final IntBuffer transfered = IntBuffer.allocate (1);

        header.put (DISPLAY_HEADER);

        final int [] pixels = ((DataBufferInt) image.getRaster ().getDataBuffer ()).getData ();

        for (int y = 0; y < HEIGHT; y++)
        {
            for (int x = 0; x < WIDTH; x++)
            {
                final int pixel = pixels[x + y * WIDTH];
                final int red = ((pixel & 0x00FF0000) >> 16) * 31 / 255;
                final int green = ((pixel & 0x0000FF00) >> 8) * 63 / 255;
                final int blue = (pixel & 0x000000FF) * 31 / 255;

                // 3b(low) green - 5b red / 5b blue - 3b (high) green, e.g. gggRRRRR BBBBBGGG
                buffer.put ((byte) ((green & 0x07) << 5 | red & 0x1F));
                buffer.put ((byte) ((blue & 0x1F) << 3 | (green & 0x38) >> 3));
            }
            for (int x = 0; x < 128; x++)
                buffer.put ((byte) 0x00);
        }

        LibUsb.bulkTransfer (this.handle, (byte) 0x01, header, transfered, 1000L);
        LibUsb.bulkTransfer (this.handle, (byte) 0x01, buffer, transfered, 1000L);
    }


    /**
     * Disconnect from the USB device.
     */
    public void disconnect ()
    {
        if (this.handle == null)
            return;

        // Prevent further sending
        final DeviceHandle h = this.handle;
        this.handle = null;

        final int result = LibUsb.releaseInterface (h, INTERFACE_NUMBER);
        if (result != LibUsb.SUCCESS)
            throw new LibUsbException ("Unable to release interface", result);

        LibUsb.close (h);
        LibUsb.exit (null);
    }
}
