
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressChanger {

    static Matcher matcher;

    public static void changeAddress(String oldAddress, String newAddress, String array, String Tag) {
        matcher = Pattern.compile("\\[(.*?)\\];").matcher("[" + array);
        System.out.println(array);
        while (matcher.find()) {
            System.out.println(matcher.group(1));
        }

    }
}
