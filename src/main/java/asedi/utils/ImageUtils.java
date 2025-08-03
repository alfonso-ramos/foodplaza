package asedi.utils;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ImageUtils {
    
    /**
     * Creates a default user avatar with the user's initials.
     * @param name The user's name to generate initials from
     * @return A circular avatar image
     */
    public static Image createDefaultAvatar(String name) {
        // Create a circle for the avatar
        Circle circle = new Circle(25);
        circle.setFill(Color.LIGHTGRAY);
        
        // Create text with the user's initials
        Text text = new Text(getInitials(name));
        text.setFont(Font.font(16));
        text.setFill(Color.WHITE);
        
        // Center the text in the circle
        text.setX(25 - text.getLayoutBounds().getWidth() / 2);
        text.setY(25 + text.getLayoutBounds().getHeight() / 4);
        
        // Create a snapshot of the circle and text
        javafx.scene.Group group = new javafx.scene.Group(circle, text);
        return group.snapshot(null, null);
    }
    
    /**
     * Extracts initials from a name.
     * @param name The full name
     * @return The initials (first letter of first and last name)
     */
    private static String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 0) {
            return "?";
        } else if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        } else {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
    }
    
    /**
     * Gets a default user image, either from resources or generates one.
     * @param userName The user's name for generating initials
     * @return A default user image
     */
    public static Image getDefaultUserImage(String userName) {
        try {
            // First try to load from resources
            return new Image("/images/default-user.png");
        } catch (Exception e) {
            // If not found, generate a default avatar
            return createDefaultAvatar(userName);
        }
    }
}
