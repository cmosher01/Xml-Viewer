package nu.mine.mosher.xml.viewer;

import org.owasp.encoder.Encode;

import java.util.Objects;

public class Util {
    public static String filter(String s) {
        if (Objects.isNull(s)) {
            return "<null>";
        }
        return Encode.forXml(s)
            .replace(" ", ".")
            .replace("\t", "&x09;")
            .replace("\n", "&x0a;")
            .replace("\r", "&x0d");
    }
}
