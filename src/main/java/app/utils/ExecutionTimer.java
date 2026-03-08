package app.utils;

public class ExecutionTimer
{
    private static long start;

    public static void start()
    {
        start = System.currentTimeMillis();
    }

    public static String split(String message)
    {
        return formatElapsed(message, System.currentTimeMillis());
    }

    public static String finish()
    {
        return formatElapsed("Total runtime", System.currentTimeMillis());
    }

    private static String formatElapsed(String label, long endTime)
    {
        long elapsed = endTime - start;
        long minutes = (elapsed / 1000) / 60;
        long seconds = (elapsed / 1000) % 60;
        long hundredths = (elapsed % 1000) / 10;

        return String.format("%s: %02d:%02d.%02d%n", label, minutes, seconds, hundredths);
    }
}
