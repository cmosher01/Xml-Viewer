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
        if (this.xml.isPresent()) {
            throw new IllegalArgumentException("only one xml file is allowed");
        }
        final URL url = asUrl(value.get());
        this.xml = Optional.of(url);
    }
}
