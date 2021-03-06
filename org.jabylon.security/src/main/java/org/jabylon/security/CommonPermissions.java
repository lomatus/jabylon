/**
 * (C) Copyright 2013 Jabylon (http://www.jabylon.org) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
/**
 *
 */
package org.jabylon.security;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.jabylon.properties.PropertiesPackage;
import org.jabylon.properties.Resolvable;
import org.jabylon.properties.Workspace;
import org.jabylon.users.Permission;
import org.jabylon.users.Role;
import org.jabylon.users.User;
import org.jabylon.users.UserManagement;
import org.jabylon.users.UsersFactory;

/**
 *
 * common permission strings based on shiro syntax
 * http://shiro.apache.org/permissions.html
 * @author jutzig.dev@googlemail.com
 *
 */
public class CommonPermissions {

    public static final String WILDCARD = "*";

    public static final String ROLE_ANONYMOUS = "Anonymous";
    public static final String ROLE_REGISTERED = "Registered";
    public static final String ROLE_LDAP_REGISTERED = "LdapRegistered";
    public static final String ROLE_ADMINISTRATOR = "Administrator";

    public static final String USER_ANONYMOUS = ROLE_ANONYMOUS;


    public static final String PROJECT = "Project";
    public static final String WORKSPACE = "Workspace";
    public static final String USER = "User";
    public static final String SYSTEM = "System";

    /**
     * enables to edit resources
     */
    public static final String ACTION_EDIT = "edit";

    /**
     * grants read-only access
     */
    public static final String ACTION_VIEW = "view";

    /**
     * grants access to configuration
     */
    public static final String ACTION_CONFIG = "config";

    /**
     * permission to not directly edit translations, but at least make suggestions
     */
    public static final String ACTION_SUGGEST = "suggest";


    public static final String PROJECT_GLOBAL_CONFIG = PROJECT + ":" + WILDCARD + ":" + ACTION_CONFIG;
    public static final String PROJECT_GLOBAL_VIEW = PROJECT + ":" + WILDCARD + ":" + ACTION_VIEW;
    public static final String PROJECT_GLOBAL_EDIT = PROJECT + ":" + WILDCARD + ":" + ACTION_EDIT;

    public static final String WORKSPACE_CONFIG = WORKSPACE + ":" + ACTION_CONFIG;

    public static final String AUTH_TYPE_LDAP = "LDAP";
    public static final String AUTH_TYPE_DB = "DB";

    /**
     * right to edit any configuration
     */
    public static final String SYSTEM_GLOBAL_CONFIG = "System:*:config";
    public static final String USER_GLOBAL_CONFIG = "User:*:config";

    /**
     * right to register as a new user
     */
    public static final String USER_REGISTER = "User:register";

    /**
     * basic right to access configuration in general
     * Deprecation: do we still need this?
     * Settings page no longer requires it
     */
    @Deprecated
    public static final String SYSTEM_GENERAL_CONFIG = "System:config";

    private static final String PERMISSION_PATTERN = "{0}:{1}:{2}";
    private static final String WORKSPACE_PERMISSION_PATTERN = "{0}:{1}";

	private static final Set<EClass> KNOWN_TARGETS;
	static {
		KNOWN_TARGETS = new HashSet<EClass>();
		KNOWN_TARGETS.add(PropertiesPackage.Literals.WORKSPACE);
		KNOWN_TARGETS.add(PropertiesPackage.Literals.PROJECT);
	}

    /**
     * computes something known to us that we can use to construct a proper permission.
     * e.g. we don't have permissions on per descriptor level, so we need to walk up the
     * hierarchy until we find something known
     * @param target
     * @return
     */
    private static Resolvable<?, ?> getActualTarget(Resolvable<?, ?> target) {
    	Resolvable<?, ?> current = target;
    	while(current!=null && !KNOWN_TARGETS.contains(current.eClass()))
    		current = current.getParent();
		return current;
	}

    public static String constructPermissionName(String kind, String scope, String action){
        return MessageFormat.format(PERMISSION_PATTERN, kind,scope,action);
    }

    public static String constructPermissionName(Resolvable<?, ?> r, String action){
    	Resolvable<?, ?> rightsContainer = getActualTarget(r);
    	if (rightsContainer instanceof Workspace) {
			// in that case the name pattern is a bit different
    		// TODO: unify it
    		return MessageFormat.format(WORKSPACE_PERMISSION_PATTERN,r.eClass().getName(),action);
		}
        return constructPermissionName(rightsContainer.eClass().getName(), rightsContainer.getName(), action);
    }

    public static boolean hasPermission(User user, String permission) {
        return user.hasPermission(permission);
    }

    public static boolean hasPermission(User user, Resolvable<?, ?> r, String action) {
        return user.hasPermission(constructPermissionName(r, action));
    }

    public static boolean hasEditPermission(User user, Resolvable<?, ?> r) {
        return user.hasPermission(constructPermissionName(r, ACTION_EDIT));
    }

    public static boolean hasSuggestPermission(User user, Resolvable<?, ?> r) {
        return user.hasPermission(constructPermissionName(r, ACTION_SUGGEST));
    }

    
    public static boolean hasViewPermission(User user, Resolvable<?, ?> r) {
        return user.hasPermission(constructPermissionName(r, ACTION_VIEW));
    }

    public static boolean hasConfigPermission(User user, Resolvable<?, ?> r) {
        return user.hasPermission(constructPermissionName(r, ACTION_CONFIG));
    }

    public static String constructPermission(String... parts) {
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            builder.append(part);
            builder.append(":");
        }
        if(builder.length()!=0)
            builder.setLength(builder.length()-1);
        return builder.toString();
    }

    public static boolean isEditRequest(String permission) {
        return permission.contains(":"+ACTION_EDIT);
    }

    /**
     * adds the default permissions and roles for a new user
     * @param userManagement
     * @param user
     */
    public static void addDefaultPermissions(UserManagement userManagement, User user) {
        String name = user.getName();
        String selfEdit = constructPermission(USER,name,ACTION_CONFIG);
        Permission selfEditPermission = userManagement.findPermissionByName(selfEdit);
        if(selfEditPermission==null) {
            selfEditPermission = UsersFactory.eINSTANCE.createPermission();
            selfEditPermission.setName(selfEdit);
            userManagement.getPermissions().add(selfEditPermission);
        }
        user.getPermissions().add(selfEditPermission);

        Role registeredRole = userManagement.findRoleByName(ROLE_REGISTERED);
        if(registeredRole==null)
            throw new RuntimeException("Registered role must always exist");
        user.getRoles().add(registeredRole);
        Role anonymousRole = userManagement.findRoleByName(ROLE_ANONYMOUS);
        if(anonymousRole==null)
            throw new RuntimeException("Anonymous role must always exist");
        user.getRoles().add(anonymousRole);
    }
}
