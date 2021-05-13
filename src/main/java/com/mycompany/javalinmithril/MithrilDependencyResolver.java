/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.javalinmithril;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author tareq
 */
public class MithrilDependencyResolver {

    public static final Map<String, String> fullclassnameToClass = new HashMap<>();
    public static final Map<String, String> fullclassnameToClassWithDependencies = new HashMap<>();
    public static final Map<String, String> fullclassNameToFile = new HashMap<>();
    private static final Pattern IMPORT_PATTERN = Pattern.compile("@import (\\S+);?");
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("@package (\\S+);?");
    private static final Pattern CLASS_PATTERN = Pattern.compile("(class (\\S+)\\s*{.*})");

    public MithrilDependencyResolver(Set<Path> paths) {
        paths.stream().forEach(path -> {
            try {
                String fileContent = new String(Files.readAllBytes(path));
                String packageName = "";
                Matcher packageMatcher = PACKAGE_PATTERN.matcher(fileContent);
                if (packageMatcher.matches()) {
                    packageName = packageMatcher.group(1);
                }
                packageName = packageName.replaceAll(".", "_");
                Matcher classNameMatcher = CLASS_PATTERN.matcher(fileContent);

                while (classNameMatcher.matches()) {
                    String shortClassName = classNameMatcher.group(2);
                    String fullClassName = packageName.concat("_").concat(shortClassName);
                    String classContent = classNameMatcher.group(1).replaceAll(shortClassName, fullClassName);
                    fullclassnameToClass.put(fullClassName, fileContent.replaceAll("@import .*", "").replaceAll("@package .*", ""));
                    fullclassNameToFile.put(fullClassName, fileContent);
                }
            } catch (IOException ex) {

            }
        });

    }

    /**
     * Build the HTML of components needed for this component
     *
     * @param componentId the component-id to build the HTMl for.
     * @return a HTML string of the components needed for this page/view if the
     * component is found, an error string otherwise.
     */
    public String resolve(final String componentId) {
        if (!fullclassnameToClass.containsKey(componentId)) {
            throw new IllegalArgumentException(String.format("Component Class %s not found ", componentId));
        }
        if (fullclassnameToClassWithDependencies.containsKey(componentId)) {
            return fullclassnameToClassWithDependencies.get(componentId);
        }
        Set<String> dependencies = resolveTransitiveDependencies(componentId);

        StringBuilder builder = new StringBuilder();
        dependencies.forEach(dependency -> {
            builder.append("<!-- ").append(dependency).append(" -->\n");
            builder.append(fullclassnameToClass.get(dependency));
            builder.append("\n");
        });
        String allDependencies = builder.toString();
        fullclassnameToClassWithDependencies.put(componentId, allDependencies);
        return allDependencies;
    }

    /**
     * Resolve the dependencies for a required component based on the contents
     * of its file
     *
     * @param componentId the name of the component, without tags
     * @return a Set of dependencies needed to render this component
     */
    private Set<String> resolveTransitiveDependencies(final String componentId) {
        Set<String> requiredComponents = new HashSet<>();
        requiredComponents.add(componentId);// add it to the dependency list
        Set<String> directDependencies = resolveDirectDependencies(componentId); // get its dependencies
        requiredComponents.addAll(directDependencies); // add all its dependencies  to the required components list
        directDependencies.forEach(dependency -> {
            // resolve each dependency
            requiredComponents.addAll(resolveTransitiveDependencies(dependency));
        });
        return requiredComponents;
    }

    /**
     * Resolve the direct dependencies for a component
     *
     * @param componentId the component to resolve dependencies for.
     * @return a set of dependencies.
     */
    private Set<String> resolveDirectDependencies(final String componentId) {
        Set<String> dependencies = new HashSet<>();
        String componentContent = fullclassNameToFile.get(componentId);
        Matcher matcher = IMPORT_PATTERN.matcher(componentContent); // match for HTML tags
        while (matcher.find()) {
            String match = matcher.group(1).replaceAll(".", "_");
            if (!match.equals(componentId) && fullclassNameToFile.containsKey(match)) { // if it isn't the component itself, and its in the component map
                dependencies.add(match); // add it to the list of dependencies
            }
        }
        return dependencies;
    }
}
