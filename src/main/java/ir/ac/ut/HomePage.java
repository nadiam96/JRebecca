package ir.ac.ut;

import ir.ac.ut.graph.StateGraphFactory;
import ir.ac.ut.graph.VisGraphPanel;
import ir.ac.ut.rebbeca.core.StateFactory;
import ir.ac.ut.rebbeca.core.domain.State;
import ir.ac.ut.uploadManager.SingleUploadPanel;
import java.io.File;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;

public class HomePage extends WebPage {

    private static final long serialVersionUID = 1L;
    private String depthModel;
    private State initialState;
    private AjaxButton startButton;

    public HomePage(final PageParameters parameters) {
        super(parameters);

        depthModel = "10";
        Form form = new Form("form");
        form.setOutputMarkupId(true);
        this.add(form);

        form.add(new SingleUploadPanel("upload") {
            @Override
            protected void afterSubmit(File rf) {
                startButton.setVisible(true);
                StateFactory factory = new StateFactory() {
                    @Override
                    protected int getDepth() {
                        return (depthModel != null && !depthModel.isEmpty()) ? Integer.parseInt(depthModel) : 10;
                    }
                };
                initialState = factory.generateState(rf);
                form.addOrReplace(new DownloadLink("downloadLink", factory.getZippedSources()));
            }
        });

        form.add(new DownloadLink("downloadLink", new File("")).setVisible(false));
        
        VisGraphPanel graph = new VisGraphPanel("graph");
        graph.setOutputMarkupId(true);
        form.add(graph);

        form.add(new TextField("depth", new IModel<String>() {

            @Override
            public String getObject() {
                return depthModel;
            }

            @Override
            public void setObject(String object) {
                depthModel = object;
            }
        }));

        startButton = new AjaxButton("start") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                if (initialState == null) {
                    error("No rebeca file uploaded.");
                    target.add(form);
                    return;
                }
                graph.renderGraph(target, new StateGraphFactory().generateStateGraph(initialState));
                target.add(form);
            }
        };
        form.add(startButton.setVisible(false));
        form.add(new AjaxButton("next") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                if (initialState == null) {
                    error("No rebeca file uploaded.");
                    target.add(form);
                    return;
                }
                for (State leave : initialState.getLeaves()) {
                    leave.generateNextStep();
                }
                graph.renderGraph(target, new StateGraphFactory().generateStateGraph(initialState));
                target.add(form);
            }
        });
        form.add(new AjaxButton("fast-forward") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {

            }
        });
    }
}
