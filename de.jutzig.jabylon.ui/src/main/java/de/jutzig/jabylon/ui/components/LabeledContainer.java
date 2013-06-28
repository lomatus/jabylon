package de.jutzig.jabylon.ui.components;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class LabeledContainer extends CustomComponent implements ComponentContainer {

    @AutoGenerated
    private VerticalLayout mainLayout;
    @AutoGenerated
    private Panel contentPanel;
    @AutoGenerated
    private VerticalLayout content;
    @AutoGenerated
    private HorizontalLayout header;

    /*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */



    /*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */



    /*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */



    /*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */



    /*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

    /**
     * The constructor should first build the main layout, set the
     * composition root and then do any custom initialization.
     *
     * The constructor will not be automatically regenerated by the
     * visual editor.
     */
    public LabeledContainer() {
        buildMainLayout();
        setCompositionRoot(mainLayout);
        // TODO add user code here
    }

    @AutoGenerated
    private VerticalLayout buildMainLayout() {
        // common part: create layout
        mainLayout = new VerticalLayout();
        mainLayout.setStyleName("jabylon-container"); //$NON-NLS-1$
        mainLayout.setImmediate(false);
//		mainLayout.setWidth("100%");
//		mainLayout.setHeight("100%");
        mainLayout.setMargin(false);

        // top-level component properties
//		setWidth("100.0%");
//		setHeight("100.0%");

        // header
        header = new HorizontalLayout();
        header.setStyleName("jabylon-container-header"); //$NON-NLS-1$
        header.setImmediate(false);
        header.setWidth("100.0%"); //$NON-NLS-1$
        header.setHeight("-1px"); //$NON-NLS-1$
        header.setMargin(false);
        mainLayout.addComponent(header);

        // contentPanel
        contentPanel = buildContentPanel();
        mainLayout.addComponent(contentPanel);
        mainLayout.setExpandRatio(contentPanel, 1.0f);

        return mainLayout;
    }

    @AutoGenerated
    private Panel buildContentPanel() {
        // common part: create layout
        contentPanel = new Panel();
        contentPanel.setStyleName("jabylon-container-content"); //$NON-NLS-1$
        contentPanel.setImmediate(false);
        contentPanel.setWidth("100.0%"); //$NON-NLS-1$
        contentPanel.setHeight("100.0%"); //$NON-NLS-1$

        // content
        content = new VerticalLayout();
        content.setImmediate(false);
        content.setWidth("100.0%"); //$NON-NLS-1$
        content.setHeight("100.0%"); //$NON-NLS-1$
        content.setMargin(false);
        contentPanel.setContent(content);

        return contentPanel;
    }

    public void setHeadClient(Component component) {
        header.addComponent(component);
    }

    public void setBody(Component component) {
        content.removeAllComponents();
        content.addComponent(component);
    }
}
