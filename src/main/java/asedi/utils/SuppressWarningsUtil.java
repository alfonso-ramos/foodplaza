package asedi.utils;

/**
 * Utility class to suppress "unused parameter" warnings.
 * Use this class to document intentionally unused parameters.
 */
public final class SuppressWarningsUtil {
    private SuppressWarningsUtil() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Use this method to suppress "unused parameter" warnings.
     * @param <T> The type of the parameter
     * @param parameter The parameter that is intentionally unused
     */
    @SafeVarargs
    @SuppressWarnings("unused")
    public static <T> void unused(T... parameter) {
        // Intentionally empty - used to suppress warnings
    }
}
