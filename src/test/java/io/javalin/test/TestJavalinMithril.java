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
package io.javalin.test;

import io.javalin.http.Context;
import io.javalin.plugin.rendering.mithril.JavalinMithril;
import io.javalin.plugin.rendering.mithril.MithrilComponent;
import static java.util.Collections.singletonMap;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;

/**
 *
 * @author tareq
 */
public class TestJavalinMithril {

    @Before
    public void setupTest() {
        JavalinMithril.configure(config -> {
            config.isDev(true)
                    .stateFunction(ctx -> singletonMap("test", "var"))
                    .filePath("src/test/resources/mithril");
        });
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
        assertTrue(resolved.contains("io_javalin_test2_SingleComponent"));
        assertTrue(resolved.contains("io_javalin_test_NameSpaceComponent"));
    }

    @Test
    public void mithrilComponentDuplicateDepndencyTest() {
        Context ctx = getMockedContext();
        try {
            new MithrilComponent("io.javalin.test.ImportComponent3").handle(ctx);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        assertThat(ctx.resultString().replaceAll("\\s", "")).containsPattern("(io_javalin_test_SingleComponent.*?){3}");
    }

    @Test
    public void mithrilComponentImportTest() {

        Context ctx = getMockedContext();
        try {
            new MithrilComponent("io.javalin.test.ImportComponent").handle(ctx);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        assertThat(ctx.resultString()).contains("io_javalin_test_ImportComponent");
        assertThat(ctx.resultString()).contains("io_javalin_test_SingleComponent");

    }

    @Test
    public void mithrilComponentCacheTest() {
        Context ctx = getMockedContext();
        try {
            new MithrilComponent("io.javalin.test.ImportComponent").handle(ctx);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        Context ctx2 = getMockedContext();
        try {
            new MithrilComponent("io.javalin.test.ImportComponent").handle(ctx2);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        assertThat(ctx2.resultString()).isEqualTo(ctx.resultString());
    }

    @Test
    public void mithrilComponentTrasativeImportTest() {

        Context ctx = getMockedContext();
        try {
            new MithrilComponent("io.javalin.test.NameSpaceComponent").handle(ctx);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        assertThat(ctx.resultString()).contains("io_javalin_test_SingleComponent");
        assertThat(ctx.resultString()).contains("io_javalin_test_ImportComponent");
        assertThat(ctx.resultString()).contains("io_javalin_test2_SingleComponent");
        assertThat(ctx.resultString()).contains("io_javalin_test_NameSpaceComponent");

    }

    @Test
    public void mithrilComponentCdnReplaceTestDev() {
        JavalinMithril.configure(config -> {
            config.isDev(true)
                    .stateFunction(ctx -> singletonMap("test", "var"))
                    .filePath("src/test/resources/mithril");
        });
        Context ctx = getMockedContext();
        try {
            new MithrilComponent("io.javalin.test.SingleComponent").handle(ctx);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        assertThat(ctx.resultString()).contains("webjar/test");
    }

    @Test
    public void mithrilComponentCdnReplaceTestNonDev() {
        JavalinMithril.configure(config -> {
            config.isDev(false)
                    .stateFunction(ctx -> singletonMap("test", "var"))
                    .filePath("src/test/resources/mithril");
        });
        Context ctx = getMockedContext();
        try {
            new MithrilComponent("io.javalin.test.SingleComponent").handle(ctx);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        assertThat(ctx.resultString()).contains("webjar/test");
    }

    @Test
    public void mithrilComponentSingleQueryParameterTest() {
        Context ctx = getMockedContext();
        Mockito.when(ctx.req.getQueryString()).thenReturn("foo=bar");
        try {
            new MithrilComponent("io.javalin.test.SingleComponent").handle(ctx);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        assertThat(ctx.resultString()).contains("\"queryParams\":{\"foo\":\"bar\"}");

    }

    @Test
    public void mithrilComponentGlobalStateTest() {
        Context ctx = getMockedContext();

        try {
            new MithrilComponent("io.javalin.test.SingleComponent").handle(ctx);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        assertThat(ctx.resultString()).contains("\"state\":{\"test\":\"var\"}");

    }

    @Test
    public void mithrilComponentLocalStateTestOverride() {
        Context ctx = getMockedContext();

        try {
            new MithrilComponent("io.javalin.test.SingleComponent", (context) -> singletonMap("test", "var2")).handle(ctx);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        assertThat(ctx.resultString()).contains("\"state\":{\"test\":\"var2\"}");

    }

    @Test
    public void mithrilComponentLocalStateTestAppend() {
        Context ctx = getMockedContext();

        try {
            new MithrilComponent("io.javalin.test.SingleComponent", (context) -> singletonMap("test2", "var2")).handle(ctx);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        assertThat(ctx.resultString()).contains("\"state\":{\"test\":\"var\",\"test2\":\"var2\"}");

    }

    @Test
    public void mithrilComponentMultipleQueryParameterTest() {
        Context ctx = getMockedContext();
        Mockito.when(ctx.req.getQueryString()).thenReturn("foo=bar&foo=baz");
        try {
            new MithrilComponent("io.javalin.test.SingleComponent").handle(ctx);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        assertThat(ctx.resultString()).contains("\"queryParams\":{\"foo\":[\"bar\",\"baz\"]}");
    }

    @Test
    public void emptyQueryParameterTest() {
        Context ctx = getMockedContext();
        Mockito.when(ctx.req.getQueryString()).thenReturn("foo=");
        try {
            new MithrilComponent("io.javalin.test.SingleComponent").handle(ctx);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        assertThat(ctx.resultString()).contains("\"queryParams\":{\"foo\":\"\"}");
    }

    @Test
    public void mithrilComponentSingleTest() {
        Context ctx = getMockedContext();
        try {
            new MithrilComponent("io.javalin.test.SingleComponent").handle(ctx);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        assertThat(ctx.resultString()).contains("io_javalin_test_SingleComponent");
    }

    @Test
    public void mithrilComponentNonExistentTest() {
        Context ctx = getMockedContext();
        try {
            new MithrilComponent("Does not exist").handle(ctx);
        } catch (Exception ex) {
            assertThat(ex.getMessage()).contains("Component Class Does not exist not found");
        }
    }

    @Test
    public void mithrilPathParameterTest() {

    }

    @Test
    public void mithrilComponentSelfDependentTest() {
        Context ctx = getMockedContext();
        try {
            new MithrilComponent("io.javalin.test.SelfDependent").handle(ctx);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        assertThat(ctx.resultString().replaceAll("\\s", "")).containsPattern("(io_javalin_test_SelfDependent.*?){2}");
    }

    private Context getMockedContext() {
        return new Context(mock(HttpServletRequest.class), mock(HttpServletResponse.class), new HashMap<>());
    }
}
