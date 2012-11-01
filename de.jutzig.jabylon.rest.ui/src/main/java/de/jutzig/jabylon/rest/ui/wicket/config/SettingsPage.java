/**
 * 
 */
package de.jutzig.jabylon.rest.ui.wicket.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.emf.cdo.CDOState;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CommitException;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.osgi.service.prefs.Preferences;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import de.jutzig.jabylon.common.util.AttachablePreferences;
import de.jutzig.jabylon.common.util.DelegatingPreferences;
import de.jutzig.jabylon.common.util.PreferencesUtil;
import de.jutzig.jabylon.common.util.config.DynamicConfigUtil;
import de.jutzig.jabylon.properties.PropertiesFactory;
import de.jutzig.jabylon.properties.PropertiesPackage;
import de.jutzig.jabylon.properties.Resolvable;
import de.jutzig.jabylon.rest.ui.Activator;
import de.jutzig.jabylon.rest.ui.model.AttachableWritableModel;
import de.jutzig.jabylon.rest.ui.model.IEObjectModel;
import de.jutzig.jabylon.rest.ui.model.WritableEObjectModel;
import de.jutzig.jabylon.rest.ui.security.CDOAuthenticatedSession;
import de.jutzig.jabylon.rest.ui.wicket.GenericPage;
import de.jutzig.jabylon.rest.ui.wicket.components.BootstrapTabbedPanel;
import de.jutzig.jabylon.users.User;


/**
 * @author Johannes Utzig (jutzig.dev@googlemail.com)
 * 
 */
public class SettingsPage extends GenericPage<Resolvable<?, ?>> {

	private static final long serialVersionUID = 1L;
	
	public static final String QUERY_PARAM_CREATE = "create";

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SettingsPage(PageParameters parameters) {
		super(parameters);
		StringValue value = parameters.get(QUERY_PARAM_CREATE);
		if(value!=null && !value.isEmpty())
		{
			EClassifier eClassifier = PropertiesPackage.eINSTANCE.getEClassifier(value.toString());
			if (eClassifier instanceof EClass) {
				EClass eclass = (EClass) eClassifier;
				setModel(new AttachableWritableModel(eclass, getModel()));
//				EObject eObject = PropertiesFactory.eINSTANCE.create(eclass);
//				if (eObject instanceof Resolvable<?,?>) {
//					Resolvable<?,?> newChild = (Resolvable<?,?>) eObject;
//					
//					CDOTransaction transaction = Activator.getDefault().getRepositoryConnector().openTransaction();
//					Resolvable<?, ?> parent = transaction.getObject(getModel().getObject());
//					List children = parent.getChildren();
//					newChild.setName("<New>");
//					children.add(newChild);
//					try {
//						transaction.commit();
//						newChild = getModel().getObject().cdoView().getObject(newChild);
//						setModel(createModel(newChild));
//					} catch (CommitException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//						getSession().error(e.getMessage());
//					}finally{
//						transaction.close();
//						
//					}
//					
//				}
//				
			}
		}
		List<ITab> extensions = loadTabExtensions();
		
		BootstrapTabbedPanel<ITab> tabContainer = new BootstrapTabbedPanel<ITab>("tabs", extensions);
		add(tabContainer);
		tabContainer.setOutputMarkupId(true);
	}

	@Override
	protected IEObjectModel<Resolvable<?, ?>> createModel(Resolvable<?, ?> object) {
		return new WritableEObjectModel<Resolvable<?, ?>>(object);
	}
	

	private User getUser() {
		User user = null;
		if (getSession() instanceof CDOAuthenticatedSession) {
			CDOAuthenticatedSession session = (CDOAuthenticatedSession) getSession();
			user = session.getUser();
		}
		return user;
	}

	private List<ITab> loadTabExtensions() {
	
		List<IConfigurationElement> configurationElements = DynamicConfigUtil.getConfigTabs();
		ListMultimap<String, ConfigSection<?>> sections = ArrayListMultimap.create(configurationElements.size(), 5);		
		
		List<IConfigurationElement> elements = DynamicConfigUtil.getApplicableElements(getModelObject(), getUser());
		for (IConfigurationElement element : elements) {
			String id = element.getAttribute("tab");
			ConfigSection<?> extension;
			try {
				extension = (ConfigSection<?>) element.createExecutableExtension("section");
				sections.put(id, extension);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		List<ITab> extensions = new ArrayList<ITab>();
		Resolvable<?, ?> modelObject = getModelObject();
		boolean isNew = modelObject.cdoState()==CDOState.NEW || modelObject.cdoState()==CDOState.TRANSIENT;
		Preferences preferences = isNew ? new AttachablePreferences() : new DelegatingPreferences(PreferencesUtil.scopeFor(modelObject));
		for (IConfigurationElement element : configurationElements) {
			String name = element.getAttribute("name");
			String id = element.getAttribute("tabID");
			ConfigTab tab = new ConfigTab(name, sections.removeAll(id),getModel(), preferences);
			if(tab.isVisible())
				extensions.add(tab);
		}
		if(!sections.isEmpty())
			//TODO: logging
			System.out.println("unmapped elements left");
		return extensions;
	}

	//
	// private Map<String, ConfigSection<T>> sections;
	// private DelegatingPreferences rootNode;
	// private CDOTransaction transaction;
	// private CDOObject domainElement;
	//
	//
	//
	//
	// private DelegatingPreferences initializePreferences(CDOObject
	// domainElement2) {
	//
	// return new
	// DelegatingPreferences(PreferencesUtil.scopeFor(domainElement2));
	// }
	//
	// private void initSections(IModel<T> model) {
	// for (Entry<String, ConfigSection<T>> entry : sections.entrySet()) {
	// String id = entry.getKey();
	// entry.getValue().init(model, rootNode);
	// }
	//
	// }
	//
	// private void createContents(Object domainElement) {
	//
	// layout = new VerticalLayout() {
	// @Override
	// public void detach() {
	// super.detach();
	// transaction.close();
	// }
	// };
	// layout.setMargin(true);
	// layout.setSpacing(true);
	// // layout.setSizeFull();
	// List<IConfigurationElement> configSections =
	// DynamicConfigUtil.getApplicableElements(domainElement);
	// Map<String, IConfigurationElement> visibleTabs =
	// computeVisibleTabs(configSections);
	//
	// TabSheet sheet = new TabSheet();
	// Map<String, VerticalLayout> tabs = fillTabSheet(visibleTabs, sheet);
	// layout.addComponent(sheet);
	// layout.setExpandRatio(sheet, 0);
	// for(int i=configSections.size()-1;i>=0;i--)
	// {
	// //go in reverse order, because they are computed in reverse order
	// IConfigurationElement child = configSections.get(i);
	// try {
	//
	// ConfigSection section = (ConfigSection)
	// child.createExecutableExtension("section");
	// String title = child.getAttribute("title");
	// VerticalLayout parent = tabs.get(child.getAttribute("tab"));
	// parent.setSpacing(true);
	// parent.setMargin(true);
	// if (title != null && title.length() > 0) {
	// Section sectionWidget = new Section();
	// sectionWidget.setCaption(title);
	// sectionWidget.addComponent(section.createContents());
	// parent.addComponent(sectionWidget);
	// } else {
	// parent.addComponent(section.createContents());
	// }
	// sections.put(child.getAttribute("id"), section);
	//
	// } catch (CoreException e) {
	// Activator.error("Failed to initialze config extension " +
	// child.getAttribute("id"), e);
	// }
	//
	// }
	//
	// Button safe = new Button();
	// safe.setCaption("OK");
	// safe.addListener(new ClickListener() {
	//
	// @Override
	// public void buttonClick(ClickEvent event) {
	// for (Entry<String, ConfigSection> entry : sections.entrySet()) {
	// entry.getValue().apply(rootNode);
	// }
	// try {
	// //flush once, so clients using the preferences during 'commit' see the
	// changes
	// rootNode.flush();
	// for (Entry<String, ConfigSection> entry : sections.entrySet()) {
	// entry.getValue().commit(rootNode);
	// }
	// //flush twice if commit changed something
	// rootNode.flush();
	// transaction.commit();
	// MainDashboard.getCurrent().getBreadcrumbs().goBack();
	// // layout.getWindow().showNotification("Saved");
	// } catch (BackingStoreException e) {
	// Activator.error("Failed to persist settings of " +
	// MainDashboard.getCurrent().getBreadcrumbs().currentPath(), e);
	// layout.getWindow().showNotification("Failed to persist changes",
	// e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
	//
	// } catch (CommitException e) {
	// Activator.error("Commit failed", e);
	// }
	//
	// }
	// });
	// layout.addComponent(safe);
	// Label spacer = new Label();
	// layout.addComponent(spacer);
	// layout.setExpandRatio(spacer, 1);
	// }
	//
	// private Map<String, VerticalLayout> fillTabSheet(final Map<String,
	// IConfigurationElement> visibleTabs, TabSheet sheet) {
	// Map<String, VerticalLayout> result = new HashMap<String,
	// VerticalLayout>();
	//
	// for (Entry<String, IConfigurationElement> entry : visibleTabs.entrySet())
	// {
	// IConfigurationElement element = entry.getValue();
	// VerticalLayout layout = new VerticalLayout();
	// sheet.addTab(layout, element.getAttribute("name"));
	// result.put(entry.getKey(), layout);
	// }
	// return result;
	// }
	//
	private Map<String, IConfigurationElement> computeVisibleTabs(List<IConfigurationElement> configSections) {
		// linked hashmap to retain the precendence order
		Map<String, IConfigurationElement> tabs = new LinkedHashMap<String, IConfigurationElement>();
		List<IConfigurationElement> tabList = DynamicConfigUtil.getConfigTabs();
		for (IConfigurationElement tab : tabList) {
			tabs.put(tab.getAttribute("tabID"), tab);
		}
		Set<String> neededTabs = new HashSet<String>();
		for (IConfigurationElement element : configSections) {
			neededTabs.add(element.getAttribute("tab"));
		}
		tabs.keySet().retainAll(neededTabs);

		return tabs;

	}
	//
	// @Override
	// public boolean isDirty() {
	// return transaction.isDirty() || rootNode.isDirty();
	// }
	//
	// @Override
	// public Component createContents() {
	// transaction = (CDOTransaction)
	// domainElement.cdoView().getSession().openTransaction();
	// CDOObject writable = transaction.getObject(domainElement);
	// createContents(writable);
	// initSections(writable);
	// return layout;
	//
	// }

}