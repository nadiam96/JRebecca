package ir.ac.ut;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.file.Folder;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 *
 * @see ir.ac.ut.Start#main(String[])
 */
public class WicketApplication extends WebApplication {

    private Folder uploadFolder = null;

    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<? extends WebPage> getHomePage() {
        return HomePage.class;
    }

    /**
     * @see org.apache.wicket.Application#init()
     */
    @Override
    public void init() {
        super.init();
        getResourceSettings().setThrowExceptionOnMissingResource(false);

        uploadFolder = new Folder(System.getProperty("java.io.tmpdir"), "wicket-uploads");
        // Ensure folder exists
        uploadFolder.mkdirs();
        getApplicationSettings().setUploadProgressUpdatesEnabled(true);
        // add your configuration here
    }

    public Folder getUploadFolder() {
        return uploadFolder;
    }
}
