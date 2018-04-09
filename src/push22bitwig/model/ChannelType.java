package push22bitwig.model;

/**
 * The different types of channels.
 *
 * Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public enum ChannelType
{
    /** A Track of unknown type. */
    UNKNOWN,
    /** Audio Track */
    AUDIO,
    /** Instrument Track */
    INSTRUMENT,
    /** Hybrid Track (Audio + Midi) */
    HYBRID,
    /** Group Track */
    GROUP, // isGroup
    /** Effect Track */
    EFFECT,
    /** Master Track */
    MASTER,
    /** A device layer */
    LAYER
}
