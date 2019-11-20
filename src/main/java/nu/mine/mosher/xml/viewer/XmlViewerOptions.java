package nu.mine.mosher.xml.viewer;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static nu.mine.mosher.xml.viewer.file.FileUtil.asUrl;

@SuppressWarnings({"WeakerAccess", "OptionalUsedAsFieldOrParameterType"})
public class XmlViewerOptions {
    public boolean help;
    public boolean dump;
    public Optional<URL> xml = Optional.empty();
    public final Set<URL> schemata = new HashSet<>();

    public void help(Optional<String> value) {
        this.help = true;
    }

    public void dump(Optional<String> value) {
        this.dump = true;
    }

    public void __(Optional<String> value) throws IOException {
        if (!value.isPresent()) {
            return;
        }
        final URL url = asUrl(value.get());

        if (!xml.isPresent()) {
            this.xml = Optional.of(url);
        } else {
            schemata.add(url);
        }
    }
}
