/**
 * (C) Copyright 2013 Jabylon (http://www.jabylon.org) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jabylon.rest.api;


import java.util.Dictionary;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Reference;
import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.view.CDOView;
import org.jabylon.cdo.connector.RepositoryConnector;
import org.jabylon.cdo.server.ServerConstants;
import org.jabylon.properties.Workspace;
import org.jabylon.resources.persistence.PropertyPersistenceService;
import org.jabylon.security.auth.AuthenticationService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component(policy = ConfigurationPolicy.OPTIONAL)
public class APIComponent
{

    private static final String PROPERTY_SERVLET_ALIAS = "servlet.context";

    @Reference
    private HttpService httpService;

    @Reference
    private RepositoryConnector repositoryConnector;

    @Reference
    private PropertyPersistenceService persistenceService;
    
    @Reference
    private AuthenticationService authService;
    
    private CDOView view;

    private String webContext;

    private static final Logger logger = LoggerFactory.getLogger(APIComponent.class);


    public void bindHttpService(HttpService httpService)
    {
        this.httpService = httpService;
    }


    public void unbindHttpService(HttpService httpService)
    {
        logger.info("Http Service deactivated. Shutting down Jabylon REST API");
        this.httpService = null;
    }


    @Activate
    protected void startup(final ComponentContext context)
    {
        try
        {
            webContext = getWebContext(context.getProperties());
            view = getRepositoryConnector().openView();
            CDOResource resource = view.getResource(ServerConstants.WORKSPACE_RESOURCE);

            logger.info("Starting up Jabylon REST API servlet at " + webContext);
            ApiServlet servlet = new ApiServlet((Workspace)resource.getContents().get(0), authService, persistenceService);
            httpService.registerServlet(webContext, servlet, null, null);
        }
        catch (ServletException e)
        {
            logger.error("Failed to register Jabylon REST API servlet", e);
        }
        catch (NamespaceException e)
        {
            logger.error("Failed to register Jabylon REST API servlet", e);
        }
    }


    @Deactivate
    protected void shutdown(final ComponentContext context)
    {
        httpService.unregister(getWebContext(context.getProperties()));
        logger.info("Shutting down Jabylon REST API");
        view.close();
    }


    @Modified
    protected void modify(final ComponentContext context)
    {
        String oldContext = webContext;
        webContext = getWebContext(context.getProperties());
        logger.info("Switching Jabylon REST API from {} to new context {}", oldContext, webContext);
        try
        {
            CDOResource resource = view.getResource(ServerConstants.WORKSPACE_RESOURCE);
            ApiServlet servlet = new ApiServlet((Workspace)resource.getContents().get(0), authService, persistenceService);
            httpService.registerServlet(webContext, servlet, null, null);
            httpService.unregister(getWebContext(context.getProperties()));
        }
        catch (ServletException e)
        {
            logger.error("Failed to register Jabylon REST API servlet", e);
        }
        catch (NamespaceException e)
        {
            logger.error("Failed to register Jabylon REST API servlet", e);
        }
    }


    public void bindRepositoryConnector(RepositoryConnector repositoryConnector)
    {
        this.repositoryConnector = repositoryConnector;
    }


    public void unbindRepositoryConnector(RepositoryConnector repositoryConnector)
    {
        this.repositoryConnector = null;
    }


    public RepositoryConnector getRepositoryConnector()
    {
        return repositoryConnector;
    }


    public PropertyPersistenceService getPersistenceService()
    {
        return persistenceService;
    }


    public void bindPersistenceService(PropertyPersistenceService persistenceService)
    {
        this.persistenceService = persistenceService;
    }


    public void unbindPersistenceService(PropertyPersistenceService persistenceService)
    {
        this.persistenceService = null;
    }
    
    public void bindAuthService(AuthenticationService service)
    {
        this.authService = service;
    }


    public void unbindAuthService(AuthenticationService service)
    {
        this.authService = null;
    }
    


    private String getWebContext(Dictionary< ? , ? > properties)
    {
        Object result = properties.get(PROPERTY_SERVLET_ALIAS);
        if (result instanceof String)
        {
            String context = (String)result;
            return context;

        }
        return "/translator/api";
    }
}
