/*
 *  Copyright 2021 Tareq Kirresh
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.javalin.plugin.rendering.mithril;

import io.javalin.http.Context;
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
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author tareq
 */
public class JavalinMithril {

    private static Set<Path> cachedPaths;
    private static MithrilDependencyResolver cachedResolver;
    private static Path rootDirectory = null;
    private static Boolean isDev = false;
    protected static Function<Context, Map> stateFunction = (ctx) -> emptyMap();
    protected static String cacheControl = "no-cache, no-store, must-revalidate";
    private static String indexPage;

    public static class JavalinMithrilConfig {

        public JavalinMithrilConfig() {

        }

        /**
         * Sets the root directory to a classpath path
         *
         * @param path the classpath path
         * @return the config object
         */
        public JavalinMithrilConfig classpathPath(String path) {
            JavalinMithril.rootDirectory = PathMaster.instance().classpathPath(path);
            return this;
        }

        /**
         * Sets the root directory to a filesystem path
         *
         * @param path the filesystem path
         * @return the config object
         */
        public JavalinMithrilConfig filePath(String path) {
            JavalinMithril.rootDirectory = Paths.get(path);
            return this;
        }

        /**
         * Sets the cache control header value. Default is "no-cache, no-store,
         * must-revalidate"
         *
         * @param cacheControl the cache control header value
         * @return the config object
         */
        public JavalinMithrilConfig cacheControl(String cacheControl) {
            JavalinMithril.cacheControl = cacheControl;
            return this;
        }

        /**
         * Sets whether the running environment is dev or not.
         *
         * @param dev whether the running environment is dev or not
         * @return the config object
         */
        public JavalinMithrilConfig isDev(boolean dev) {
            JavalinMithril.isDev = dev;
            return this;
        }

        /**
         * Sets the state function to be injected into all components
         *
         * @param stateFunction the state function to apply on all components
         * @return the config object
         */
        public JavalinMithrilConfig stateFunction(Function<Context, Map> stateFunction) {
            JavalinMithril.stateFunction = stateFunction;
            return this;
        }

    }

    /**
     * Configure JavalinMithril
     *
     * @param config the configuration object
     */
    public static void configure(Consumer<JavalinMithrilConfig> config) {
        config.accept(new JavalinMithrilConfig());
    }

    protected static Set<Path> paths() {
        if (cachedPaths == null || isDev()) {
            cachedPaths = walkPaths();
        }
        return cachedPaths;
    }

    protected static Boolean isDev() {
        return isDev;
    }

    protected static Path rootDirectory() {
        if (rootDirectory == null) {
            rootDirectory = PathMaster.instance().defaultLocation(isDev);
        }
        return rootDirectory;
    }

    /**
     * Gets the current dependency resolver
     * @return the current dependency resolver
     */
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
