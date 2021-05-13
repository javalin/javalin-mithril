/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.javalinmithril;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author tareq
 */
public class JavalinMithril {


    protected static Set<Path> paths = walkPaths();
    protected static MithrilDependencyResolver resolver = new MithrilDependencyResolver(paths);

    private static Set<Path> walkPaths() {
        try {
            return Files.walk(Paths.get("mithril"), 20).collect(Collectors.toSet());
        } catch (IOException ex) {
            return new HashSet<>();
        }
    }
}
