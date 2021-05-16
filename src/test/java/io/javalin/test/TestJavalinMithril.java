/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.javalin.test;

import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.rendering.mithril.JavalinMithril;
import io.javalin.plugin.rendering.mithril.MithrilComponent;
import java.util.Collections;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;

/**
 *
 * @author tareq
 */
public class TestJavalinMithril {

    @Before
    public void setupTest() {
        JavalinMithril.isDev(true);
        JavalinMithril.stateFunction = ctx -> Collections.singletonMap("test", "var");
        JavalinMithril.rootDirectory("src/test/resources/mithril", Location.EXTERNAL);
    }

    @Test
    public void resolveSingleComponent() {
        String resolved = JavalinMithril.resolver().resolve("io.javalin.test.SingleComponent");
        assertTrue(resolved.contains("io_javalin_test_SingleComponent"));
    }

    @Test
    public void resolveImportComponent() {
        String resolved = JavalinMithril.resolver().resolve("io.javalin.test.ImportComponent");
        assertTrue(resolved.contains("io_javalin_test_SingleComponent"));
        assertTrue(resolved.contains("io_javalin_test_ImportComponent"));
    }

    @Test
    public void resolveTransativeComponent() {
        String resolved = JavalinMithril.resolver().resolve("io.javalin.test.NameSpaceComponent");
        assertTrue(resolved.contains("io_javalin_test_SingleComponent"));
        assertTrue(resolved.contains("io_javalin_test_ImportComponent"));
        assertTrue(resolved.contains("io_javalin_test2_SimpleComponent"));
        assertTrue(resolved.contains("io_javalin_test_NameSpaceComponent"));

    }

    @Test
    public void mithrilComponentTest() throws Exception {
        Context ctx = getMockedContext();
        new MithrilComponent("io.javalin.test.ImportComponent").handle(ctx);

    }

    private Context getMockedContext() {
        return new Context(mock(HttpServletRequest.class), mock(HttpServletResponse.class), new HashMap<>());
    }
}
