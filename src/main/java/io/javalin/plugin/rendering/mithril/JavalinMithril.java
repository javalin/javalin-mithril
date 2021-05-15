/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.javalin.plugin.rendering.mithril;

import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Collections;
import static java.util.Collections.emptyMap;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author tareq
 */
public class JavalinMithril {

    private static Set<Path> cachedPaths;
    private static MithrilDependencyResolver cachedResolver;
    private static Path rootDirectory = null;
    private static Boolean isDev = null;
    public static Function<Context, Boolean> isDevFunction = (ctx) -> ctx.req.getRequestURL().toString().contains("localhost");
    public static Function<Context, Map> stateFunction = (ctx) -> emptyMap();
    private static String indexPage;

    protected static Set<Path> paths() {
        if (cachedPaths == null || isDev()) {
            cachedPaths = walkPaths();
        }
        return cachedPaths;
    }

    protected static Boolean isDev() {
        return isDev;
    }

    public static void isDev(boolean isDev) {
        JavalinMithril.isDev = isDev;
    }

    protected static Path rootDirectory() {
        if (rootDirectory == null) {
            rootDirectory = PathMaster.instance().defaultLocation(isDev);
        }
        return rootDirectory;
    }

    public static void rootDirectory(String path, Location location) {
        if (location == Location.CLASSPATH) {
            rootDirectory = PathMaster.instance().classpathPath(path);
        } else {
            rootDirectory = Paths.get(path);
        }

    }

    public static MithrilDependencyResolver resolver() {
        if (cachedResolver == null || isDev()) {

            cachedResolver = new MithrilDependencyResolver(paths());
        }
        return cachedResolver;
    }

    protected static Set<Path> walkPaths() {
        try {
            return Files.walk(rootDirectory(), 20).filter(path -> path.endsWith("layout.html") || path.toString().endsWith(".js")).collect(Collectors.toSet());
        } catch (IOException ex) {
            return new HashSet<>();
        }
    }

    protected static String layoutPage() {
        try {
            if (indexPage == null || isDev()) {
                indexPage = new String(Files.readAllBytes(paths().stream().filter(path -> path.endsWith("mithril/layout.html")).findFirst().orElse(null)));
            }
            return indexPage;
        } catch (IOException ex) {
            return null;
        }
    }

    protected static class PathMaster {

        private FileSystem fileSystem;
        private static PathMaster instance = instance();

        protected static PathMaster instance() {
            if (instance == null) {
                instance = new PathMaster();
            }
            return instance;
        }

        private PathMaster() {

        }

        private FileSystem fileSystem() {
            if (fileSystem == null) {
                try {
                    fileSystem = FileSystems.newFileSystem(this.getClass().getResource("").toURI(), Collections.emptyMap());
                } catch (URISyntaxException | IOException ex) {
                    throw new RuntimeException(ex);
                }

            }
            return fileSystem;
        }

        protected Path classpathPath(String path) {
            try {
                return this.getClass().getResource(path).toURI().getScheme().equals("jar") ? fileSystem().getPath(path) : Paths.get(this.getClass().getResource(path).toURI()); // we're not in jar (probably running from IDE)
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        }

        protected Path defaultLocation(boolean isDev) {
            return isDev ? Paths.get("src/main/resources/mithril") : classpathPath("/mithril");
        }
    }
}
