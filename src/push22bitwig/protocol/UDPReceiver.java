package push22bitwig.protocol;

import push22bitwig.model.DisplayModel;
import push22bitwig.model.grid.GridElement;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;


/**
 * Receives data from Bitwig via UDP.
 * 
 * Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class UDPReceiver
{
    private static final int     DISPLAY_COMMAND_SHUTDOWN = -1;
    private static final int     DISPLAY_COMMAND_GRID     = 10;

    private DisplayModel         model;
    private DatagramSocket       socket;
    private final ProtocolParser parser                   = new ProtocolParser ();

    private final Object         socketLock               = new Object ();


    /**
     * Constructor.
     *
     * @param model Where to write the received data
     */
    public UDPReceiver (final DisplayModel model)
    {
        this.model = model;
    }


    /**
     * Start receiving on a port.
     *
     * @param port The number of the port on which to receive
     */
    public void start (final int port)
    {
        this.stop ();

        this.model.addLogMessage ("Starting UDP server on port " + port + ".");

        new Thread ( () -> {
            synchronized (this.socketLock)
            {
                try
                {
                    this.socket = new DatagramSocket (port);

                    while (true)
                    {
                        // Wait for request
                        final DatagramPacket packet = new DatagramPacket (new byte [1024], 1024);
                        this.socket.receive (packet);

                        // Read packet
                        UDPReceiver.this.handleData (packet.getData (), packet.getLength ());
                    }
                }
                catch (final SocketException ex)
                {
                    if (this.socket == null || !this.socket.isClosed ())
                        this.model.addLogMessage (ex.getLocalizedMessage ());
                }
                catch (final IOException ex)
                {
                    this.model.addLogMessage (ex.getLocalizedMessage ());
                }
            }
        }).start ();
    }


    /**
     * Stop the UDP server.
     */
    public void stop ()
    {
        if (this.socket == null)
            return;

        this.model.addLogMessage ("Stopping UDP server.");
        this.socket.close ();

        // Wait till thread has stopped
        synchronized (this.socketLock)
        {
            this.model.addLogMessage ("UDP server stopped.");
        }
    }


    /**
     * Handle the received data.
     *
     * @param data The data buffer with the received data
     * @param length The length of usable data in the buffer
     */
    void handleData (final byte [] data, final int length)
    {
        // -16 == 0xF0, -9 == 0xF7
        if (data[0] != -16 || data[length - 1] != -9)
        {
            this.model.addLogMessage ("Unformatted messaged received.");
            return;
        }

        try
        {
            switch (data[1])
            {
                case DISPLAY_COMMAND_GRID:
                    try (final ByteArrayInputStream in = new ByteArrayInputStream (data, 2, length - 3))
                    {
                        final List<GridElement> elements = this.parser.parse (in);
                        if (elements != null)
                            this.model.setGridElements (elements);
                    }
                    catch (final IOException ex)
                    {
                        this.model.addLogMessage ("Unparsable grid element message: " + ex.getLocalizedMessage ());
                    }
                    break;

                // Shutdown
                case DISPLAY_COMMAND_SHUTDOWN:
                    // TODO Requires Bitwig fix
                    // this.model.shutdown ();
                    break;

                default:
                    this.model.addLogMessage ("Unknown display command: " + data[1]);
                    break;
            }
        }
        catch (final RuntimeException ex)
        {
            this.model.addLogMessage ("Error in command array: " + ex.getLocalizedMessage ());
        }
    }
}
