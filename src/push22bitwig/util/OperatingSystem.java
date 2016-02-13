package push22bitwig.util;

import java.util.Locale;


/**
 * Determine the operating system we are running.
 *
 * Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public enum OperatingSystem
{
    /** Windows. */
    WINDOWS,
    /** Several *NIXes. */
    UNIX,
    /** The Sun brands. */
    POSIX_UNIX,
    /** Any Mac. */
    MAC,
    /** No idea. */
    OTHER;

    private static OperatingSystem os = OTHER;


    static
    {
        String osName = System.getProperty ("os.name");
        if (osName != null)
        {
            osName = osName.toLowerCase (Locale.ENGLISH);
            if (osName.indexOf ("windows") != -1)
                os = WINDOWS;
            else if (osName.indexOf ("linux") != -1 || osName.indexOf ("mpe/ix") != -1 || osName.indexOf ("freebsd") != -1 || osName.indexOf ("irix") != -1 || osName.indexOf ("digital unix") != -1 || osName.indexOf ("unix") != -1)
                os = UNIX;
            else if (osName.indexOf ("mac os x") != -1)
                os = MAC;
            else if (osName.indexOf ("sun os") != -1 || osName.indexOf ("sunos") != -1 || osName.indexOf ("solaris") != -1)
                os = POSIX_UNIX;
            else if (osName.indexOf ("hp-ux") != -1 || osName.indexOf ("aix") != -1)
                os = POSIX_UNIX;
        }
    }


    /**
     * Get the OS we are running on.
     *
     * @return The OS enum
     */
    public static OperatingSystem get ()
    {
        return os;
    }
}
